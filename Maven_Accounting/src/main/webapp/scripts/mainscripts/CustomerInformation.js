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
Wtf.account.BusinessContactWindow = function(config){
    this.perAccID=null;
    this.isClosable=true;
    this.businessPerson=(config.isCustomer?"Customer":"Vendor");
    this.uPermType=config.isCustomer?Wtf.UPerm.customer:Wtf.UPerm.vendor;
    this.permType=config.isCustomer?Wtf.Perm.customer:Wtf.Perm.vendor;
    this.enableCurrency=config.enableCurrency;
    this.modeName=config.modeName;
    this.ibgReceivingDetailsArr=new Array();
    this.ibgReceivingDetailsArr=null;
    this.CIMBbank=null;
    this.DBSbank=null;
    this.readOnly=config.readOnly==undefined?false:config.readOnly;
    this.currencyExchangeWinId = "SetCurrencyExchangeWin";
    this.personId="";
    this.heplmodeid=config.helpmodeid;
    var recordid="";
    this.ibgReceivingDetails="";
    this.ispropagatetochildcompanyflag=false;
    this.isPropagatedPersonalDetails=false;
    this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
    this.custVenOptimizedFlag = Wtf.account.companyAccountPref.custvenloadtype;
    //ERM -735 Associate default payment with customer
    this.mapDefaultPmtMethod = CompanyPreferenceChecks.mapDefaultPaymentMethod();
    var buttonArray = new Array();
    this.requestTaxIDBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.requestTaxIDBtn"),  //'Request Tax ID from Vendor.',
        scope: this,
        hidden:true,            // ERP-22346
        handler: this.sendMail.createDelegate(this),
        disabled :!config.isEdit
    });
    
    this.deductionComboId= {
        Non_Deduction_or_Lower_Deduction:'1',
        Non_Deduction_Declaration:'2',
        Deduction_Transporter:'3',
        Basic_Exemption_Reached:'4'
    }
    buttonArray.push(this.requestTaxIDBttn);
    
    this.save=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.saveBtn"), 
        id: "save" + config.id,
        scope: this,
        hidden:this.readOnly,
        handler:this.confirmBeforeSave.createDelegate(this),
        iconCls: 'pwnd save'
    });
    buttonArray.push(this.save);
    
    Wtf.apply(this,config);
    Wtf.apply(this, {
        bbar: buttonArray
    });
    Wtf.account.BusinessContactWindow.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true,
        'cancel':true,
        'loadingcomplete':true
    });
     this.detailPanel = new Wtf.DetailPanel({
        modulename:this.isCustomer?'Customer':'Vendor',
        keyid:this.isCustomer?'customerid':'vendorid',
        height:200,
        mapid:1,
        id2:this.id,
        readOnly:this.readOnly,
        moduleID:this.isCustomer?Wtf.Acc_Customer_ModuleId:Wtf.Acc_Vendor_ModuleId,
        moduleName:this.isCustomer?'Customer':'Vendor',
        mainTab:this.mainTab,
        leadDetailFlag:true,
        moduleScope:this//,
    });
        
}
Wtf.extend( Wtf.account.BusinessContactWindow, Wtf.Panel, {
    loadRecord:function(){
        WtfGlobal.resetAjaxTimeOut();
        this.parentStore.isLoaded = true;
        if(this.record!=null){
            if(this.record.data['parentid']){
                this.parentAccount.setValForRemoteStore(this.record.data.parentid,this.record.data.parentname);
                this.issubFieldset.expand();
            }
            var data = this.record.data;
            if(typeof data.creationDate === "string" && data.creationDate !=""){
                data.creationDate = new Date(data.creationDate);
            }
            if(typeof data.selfBilledFromDate === "string" && data.selfBilledFromDate !=""){
                data.selfBilledFromDate = new Date(data.selfBilledFromDate);
            }
            if(typeof data.gstVerifiedDate === "string" && data.gstVerifiedDate !=""){
                data.gstVerifiedDate = new Date(data.gstVerifiedDate);
            }
            if (typeof data.sezfromdate === "string" && data.sezfromdate != "") {
                data.sezfromdate = new Date(data.sezfromdate);
            }
            if (typeof data.seztodate === "string" && data.seztodate != "") {
                data.seztodate = new Date(data.seztodate);
            }
            if(typeof data.selfBilledToDate === "string" && data.selfBilledToDate !=""){
                data.selfBilledToDate = new Date(data.selfBilledToDate);
            }
            if(typeof data.vatregdate === "string" && data.vatregdate !=""){
                data.vatregdate = new Date(data.vatregdate);
            }
            if(typeof data.cstregdate === "string" && data.cstregdate !=""){
                data.cstregdate = new Date(data.cstregdate);
            }
            if(typeof data.deductionFromDate === "string" && data.deductionFromDate !=""){
                data.deductionFromDate = new Date(data.deductionFromDate);
            }
            if(typeof data.deductionToDate === "string" && data.deductionToDate !=""){
                data.deductionToDate = new Date(data.deductionToDate);
            }
           
            this.CustomerInfoForm.getForm().loadRecord(this.record);
            if (this.record.data.paymentmethod != "") //EdIt and view case
            {                                                                           /* Display payment method data in View Edit Copy Case*/
                this.pmtStore.on("load", function () {
                    this.pmtMethod.setValue(this.record.data.paymentmethod);
                }, this);
                this.pmtStore.load();

            }
            this.creationdate.setValue(this.record.data.creationDate);
            if(this.record.data.residentialstatus==0){
                this.residentialstatus0.setValue(true);
            }else if(this.record.data.residentialstatus==1){
                this.residentialstatus1.setValue(true);
            }
            if(this.creationdate.getValue() == null || this.creationdate.getValue() == undefined || this.creationdate.getValue() == ""){
//            	this.creationdate.setValue(Wtf.serverDate);
            	this.creationdate.setValue(new Date());
            }
            this.recordid=this.record.data.accid;
            Wtf.apply(this.detailPanel, {accid:this.recordid});
            Wtf.apply(this.detailPanel, {acccode:this.record.data.acccode});
            this.Category.setValue(this.record.data.categoryid);
            if(this.record.data.isIBGActivated){
                this.isActivateIBG.toggleCollapse();
            }    
            this.ibgReceivingDetailsArr=this.record.data.ibgReceivingDetails!=undefined||this.record.data.ibgReceivingDetails!=""?this.record.data.ibgReceivingDetails:new Array();
            this.CIMBbank=this.record.data.CIMBbank!=undefined||this.record.data.CIMBbank!=""?this.record.data.CIMBbank:null;
            this.DBSbank=this.record.data.DBSbank!=undefined||this.record.data.DBSbank!=""?this.record.data.DBSbank:null;
            if(this.record.data.isPermOrOnetime != undefined)
                this.isPermOrOnetime.setValue(this.record.data.isPermOrOnetime);
            this.InterCompany.setValue(this.record.data.intercompany);
            this.InterCompanyType.setValue(this.record.data.intercompanytypeid);
            this.requestTaxIDBttn.setDisabled(!this.record.data.taxeligible);
            this.selfBilledFromDate.setValue(this.record.data.selfBilledFromDate);
            this.gstVerifiedDate.setValue(this.record.data.gstVerifiedDate);
            this.sezFromDate.setValue(this.record.data.sezfromdate);
            this.sezToDate.setValue(this.record.data.seztodate);
            this.selfBilledToDate.setValue(this.record.data.selfBilledToDate);
            this.EmploymentStatus.setValue(this.record.data.employmentStatus);
            this.employerName.setValue(this.record.data.employerName);
            this.companyAddress.setValue(this.record.data.companyAddress);
            this.occupationAndYears.setValue(this.record.data.occupationAndYears);
            this.monthlyIncome.setValue(this.record.data.monthlyIncome);
            this.noofActiveCreditLoans.setValue(this.record.data.noofActiveCreditLoans);
            if(this.panStatusCombo != undefined && this.record.data.panStatusId){
                this.panStatusCombo.setValue(this.record.data.panStatusId);
                this.panStatusCombo.fireEvent('select',this);
            }
            if(this.panStatusCombo != undefined && !Wtf.isEmpty(this.record.data.panno)){
                this.panStatusCombo.disable();
            }
            if(this.deducteeType != undefined && this.record.data.deducteeTypeId){
                this.deducteeType.setValue(this.record.data.deducteeTypeId);
            }
            if(this.deducteeCode != undefined && this.record.data.deducteeCode){
                this.deducteeCode.setValue(this.record.data.deducteeCode);
            }
            if(this.dealerTypeCombo != undefined && this.record.data.dealertype){
                this.dealerTypeCombo.setValue(this.record.data.dealertype);
            }
            if(this.dealerTypeCombo != undefined && !Wtf.isEmpty(this.record.data.isUsedInTransactions) && this.record.data.isUsedInTransactions){
                this.dealerTypeCombo.disable();
            }
            if(this.vendorBranchCombo != undefined && this.record.data.vendorbranch){
                this.vendorBranchCombo.setValue(this.record.data.vendorbranch);
            }
            if(this.interStateParty != undefined && this.record.data.interstateparty){
                this.interStateParty.setValue(this.record.data.interstateparty);
            }
            if (Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA  && this.isTDSapplicableonvendor != undefined) {
                if (this.isCustomer && this.record.data.isTDSapplicableoncust != undefined) {
                    if (this.record.data.isTDSapplicableoncust) {
                        this.isTDSapplicableonvendor.expand();
                    } else {
                        this.isTDSapplicableonvendor.collapse();
                    }
                } else if (this.record.data.isTDSapplicableonvendor != undefined) {
                    if (this.record.data.isTDSapplicableonvendor) {
                        this.isTDSapplicableonvendor.expand();
                    } else {
                        this.isTDSapplicableonvendor.collapse();
                    }
                }
            }
            if (Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && this.isTDSapplicableonvendor != undefined && this.record.data.isVendorUsedInTDSTransactions != undefined) {
                //If Vendor is used in TDS Transactions then do not allow to modify mandatory fields.
                if (this.record.data.isVendorUsedInTDSTransactions) {
//                    this.isTDSapplicableonvendor.checkbox.dom.disabled = true;
                    this.natureOfPayment.clearTrigger = false;
                    this.PANNo.disable();
                    this.panStatusCombo.disable();
                    this.natureOfPayment.disable();
                    this.higherRate.disable();
                    this.considerExemptLimit.setDisabled(true);
                } else if (!this.record.data.isVendorUsedInTDSTransactions) {
//                    this.isTDSapplicableonvendor.checkbox.dom.disabled = false;
                    this.natureOfPayment.clearTrigger = true;
                    this.PANNo.enable();
                    this.panStatusCombo.enable();
                    this.natureOfPayment.enable();
                    this.higherRate.enable();
                    this.considerExemptLimit.setDisabled(false);
                }
            }
            if(this.interStateParty != undefined && this.record.data.isInterstatepartyEditable!=undefined && !this.record.data.isInterstatepartyEditable){
                this.interStateParty.setDisabled(!this.record.data.isInterstatepartyEditable);
            }
            if(!Wtf.isEmpty(this.deducteeType) && !Wtf.isEmpty(this.record.data.deducteeTypeId) && this.record.data.isVendorUsedInTDSTransactions!=undefined && this.record.data.isVendorUsedInTDSTransactions){
                this.deducteeType.setDisabled(this.record.data.isVendorUsedInTDSTransactions);
                this.deducteeCode.setDisabled(this.record.data.isVendorUsedInTDSTransactions);
            }
            if(!Wtf.isEmpty(this.residentialstatus0) &&!Wtf.isEmpty(this.residentialstatus1) && !Wtf.isEmpty(this.record.data.residentialstatus) && this.record.data.isInterstatepartyEditable!=undefined && !this.record.data.isInterstatepartyEditable){
              this.residentialstatus0.setDisabled(this.record.data.isVendorUsedInTDSTransactions);
              this.residentialstatus1.setDisabled(this.record.data.isVendorUsedInTDSTransactions);
                if(!Wtf.isEmpty(this.higherRate) &&!Wtf.isEmpty(this.higherRate) && !Wtf.isEmpty(this.record.data.higherTDSRate) && this.record.data.isInterstatepartyEditable!=undefined && !this.record.data.isInterstatepartyEditable){
                    this.higherRate.setDisabled(this.record.data.isVendorUsedInTDSTransactions);
                }
            }
            if(this.cFormApplicable != undefined && this.record.data.cformapplicable){
                this.cFormApplicable.setValue(this.record.data.cformapplicable);
            }
            if(!this.isCustomer && Wtf.account.companyAccountPref.blockPOcreationwithMinValue && this.record.data.minPriceValueForVendor!=undefined && this.record.data.minPriceValueForVendor!==""){
                this.minPriceValueForVendor.setValue(this.record.data.minPriceValueForVendor);
            }
            if(this.cFormApplicable != undefined && this.record.data.isInterstatepartyEditable!=undefined && !this.record.data.isInterstatepartyEditable){
                this.cFormApplicable.setDisabled(!this.record.data.isInterstatepartyEditable);
            }
            if(this.gtaApplicable != undefined && this.record.data.gtaapplicable){//  GTA Applicable  ERP-25539
                this.gtaApplicable.setValue(this.record.data.gtaapplicable);
            }
            if(this.vatRegDate != undefined && this.record.data.vatregdate){
                this.vatRegDate.setValue(this.record.data.vatregdate);
            }
            if(this.CSTRegDate != undefined && this.record.data.cstregdate){
                this.CSTRegDate.setValue(this.record.data.cstregdate);
            }
            /*
             * Commented below code, because we have now implemented paging with multiselect option
             * 
             */
           //If product typeahead then set Remotely for Multiselect else set by loading the product-ERP-20116
//            if( this.productOptimizedFlag==Wtf.Show_all_Products){
//                this.ProductMapping.setValue(this.record.data.productid);
//            }else {
                this.ProductMapping.setValForRemoteStore(this.record.data.productid,this.record.data.prodname);
            //}
            if(this.isCopy){
                if(this.sequenceFormatCombobox.getValue()=="NA"){
                    this.code.enable();
                    this.code.setValue("");
                }else{
                    this.getNextSequenceNumber(this.sequenceFormatCombobox);
                }
                if (Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && this.GSTIN) {
                    this.GSTIN.setValue("");
                }
                
                /*
                 *  Field - set empty in copy case, Tax Identification Number (TIN) for philippines 
                 * 
                 */
                if (Wtf.account.companyAccountPref.countryid==Wtf.Country.PHILIPPINES && this.TIN != undefined) { 
                    this.TIN.setValue("");
                }
            }
            /*
             * Field - set value in edit case, Tax Identification Number (TIN) for philippines
             */
            if (Wtf.account.companyAccountPref.countryid==Wtf.Country.PHILIPPINES && this.TIN != undefined&& this.isEdit) { 
                    this.TIN.setValue(this.record.data.gstin);
            }
             if(this.isEdit || this.isCopy){
                if(this.ProductMapping.getValue()!=""){
                    this.addPreferredProduct.enable();
                    this.ProductMapping.customJSONString = JSON.stringify(this.record.json.customJSONString);
                }
            }
            var bal=this.record.data.openbalance;
            this.balTypeEditor.setValue(bal>0);
            if(this.isCopy) {
                bal = 0;
                this.openingBal.setValue(0);
            } else{
                if(bal!==0){
                    this.openingBal.setValue(Math.abs(bal));
                    this.balTypeEditor.enable();
                }
            }
            if(bal==0 && this.isCustomer)
            	this.balTypeEditor.setValue(true);
            this.setPersonTitle();
            var limit=this.record.data.limit;
            if(limit!=0)
                this.limit.setValue(Math.abs(limit));
            this.country.setValue(this.record.data.country);
            this.aliasname.setValue(this.record.data.aliasname);
            if(this.isCustomer){
                if(this.record.data.isavailableonlytosalespersons){
                    //this.copyCustomer.disable();  //SDP-11708
                    this.customerVenodorAvailableToSalespersonAgent.setValue(this.record.data.isavailableonlytosalespersons);
                }
            }
            Wtf.agentStore.on("load", this.setAgents, this);
            Wtf.agentStore.load();
            if(!this.isCustomer){
                if(this.record.data.isvendoravailabletoagent){
                    //this.copyCustomer.disable();  //SDP-11708
                    this.venodorAvailableToAgentCheck.setValue(this.record.data.isvendoravailabletoagent);
                }
            }
            if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                if(this.manufacturerTypeCombo != undefined && this.record.data.manufacturertype && !Wtf.isEmpty(this.record.data.manufacturertype)){
                    this.manufacturerTypeCombo.setValue(this.record.data.manufacturertype);
                }
            }
            this.multiSalesPerson.setValue(this.record.data.mappedMultiSalesPersonId);
            this.mappingReceivedFrom.setValue(this.record.data.mappedReceivedFromId);
            this.mappingPaidTo.setValue(this.record.data.mappedPaidToId);
            /**
             *  GST details - GSTIN Registration Type , Customer/ Vendor Type
             */
            if(this.GSTINRegistrationTypeCombo != undefined && this.record.data.GSTINRegistrationTypeId){
                if(this.GSTINRegistrationTypeStore.getCount()<=0){
                    this.GSTINRegistrationTypeStore.on('load',function(){
                        this.GSTINRegistrationTypeCombo.setValue(this.record.data.GSTINRegistrationTypeId);
                        this.validateGSTINNumber();
                    },this);
                    this.GSTINRegistrationTypeStore.load();
                }else{
                    this.GSTINRegistrationTypeCombo.setValue(this.record.data.GSTINRegistrationTypeId);
                    this.validateGSTINNumber();
                }
            }
             /**
             *  GST details - Customer/Vendor Type , Customer/ Vendor Type
             */
            if(this.CustomerVendorTypeCombo != undefined && this.record.data.CustomerVendorTypeId){
                if(this.CustomerVendorTypeStore.getCount()<=0){
                    this.CustomerVendorTypeStore.on('load',function(){
                        this.CustomerVendorTypeCombo.setValue(this.record.data.CustomerVendorTypeId);
                        this.validateSEZDates(); // Validate SEZ dates
                    },this);
                    this.CustomerVendorTypeStore.load();
                }else{
                    this.CustomerVendorTypeCombo.setValue(this.record.data.CustomerVendorTypeId);
                    this.validateSEZDates(); // Validate SEZ dates
                }
            }
            /*
             *  For Philippines country set value to Customer/Vendor Type Combo ERP-41394,ERP-41499
             */
            if(this.CustVenTypeCombo != undefined && this.CustVenTypeStore != undefined && this.record.data.CustomerVendorTypeId && (Wtf.account.companyAccountPref.countryid == Wtf.Country.PHILIPPINES || Wtf.Countryid == Wtf.Country.INDONESIA)){
                if(this.CustVenTypeStore.getCount()<=0){
                    this.CustVenTypeStore.on('load',function(){
                        this.CustVenTypeCombo.setValue(this.record.data.CustomerVendorTypeId);
                    },this);
                    this.CustVenTypeStore.load();
                }else{
                    this.CustVenTypeCombo.setValue(this.record.data.CustomerVendorTypeId);
                }
            }
            //this.GSTINRegistrationTypeCombo.setValForRemoteStore(this.record.data.GSTINRegistrationTypeId,this.record.data.GSTINRegistrationTypeName);
            //this.CustomerVendorTypeCombo.setValForRemoteStore(this.record.data.CustomerVendorTypeId, this.record.data.CustomerVendorTypeName);
            
            this.salesPerson.setValue(this.record.data.mappedSalesPersonId);
            if ((!this.isCustomer && Wtf.account.companyAccountPref.productPricingOnBands) || (this.isCustomer && Wtf.account.companyAccountPref.productPricingOnBandsForSales)) {
            this.pricingBandMasterStore.on('load', function() {
                this.pricingBand.setValue(this.record.data.pricingBandID);
            }, this);
                this.pricingBandMasterStore.load();
            }
            
            this.vehicleNo.setValForRemoteStore(this.record.data.vehicleNoID, this.record.data.vehicleNo);
            this.driver.setValForRemoteStore(this.record.data.driverID, this.record.data.driver);
            if(this.natureOfPayment != undefined && this.record.data.natureOfPayment){
                this.natureofPaymentStore.on('load',function(){
                    this.natureOfPayment.setValue(this.record.data.natureOfPayment);
                },this);
                this.natureofPaymentStore.load();
                if(!Wtf.isEmpty(this.record.data.isVendorUsedInTDSTransactions) && this.record.data.isVendorUsedInTDSTransactions){
                    this.natureOfPayment.disable();
                }
            }
            
            if(this.considerExemptLimit != undefined && this.record.data.considerExemptLimit){
                this.considerExemptLimit.setValue(this.record.data.considerExemptLimit)
            }

            if(this.tdsInterestPayableAccount != undefined && this.record.data.tdsInterestPayableAccount){
                this.tdsPayableAccountStore.on('load',function(){
                    this.tdsInterestPayableAccount.setValue(this.record.data.tdsInterestPayableAccount);
                },this);
                this.tdsPayableAccountStore.load();
            }
            if (!Wtf.isEmpty(this.tdsInterestPayableAccount) && !this.readOnly && !Wtf.isEmpty(this.record.data.tdsInterestPayableAccount) && this.record.data.isVendorUsedInTDSTransactions) {
                this.tdsInterestPayableAccount.setDisabled(this.record.data.istdsInterestPayableAccountisUsed);
            }
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
                            this.exchangeRate.disable();
                            this.exchangeRate.setValue("");
                        }
                    });
                }else{
                    this.exchangeRate.setValue(1);
                    this.exchangeRate.disable();
                }
                this.getDetails();          
            Wtf.MessageBox.hide(); //hiding message after loading data in edit case  
            if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA) {
                this.loadTDSRecords();// India Complieance TDS field Hide-Show and set value
            }
        }
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
            Wtf.deducteeTypeStore.load();
            Wtf.deducteeTypeStore.on("load", this.setDeducteeType, this);
        }
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
            Wtf.defaultNatureOfPurchaseStore.load();
            Wtf.defaultNatureOfPurchaseStore.on("load", this.setDefaultNatureOfPurchase, this);
        }

        var dataArr=this.parentStore.getRange(); // ERP-10835 
        for (var i =0; i< dataArr.length;i++) { // using local store for checking vendor/customer exists
            this.localParentStore.add(dataArr[i]); // Adding records of parentStore to localParentStore
        }
    },
    onRender: function(config){
        var image="../../images/accounting_image/"+this.businessPerson+".gif";
        Wtf.account.BusinessContactWindow.superclass.onRender.call(this, config);
        this.createStore();
        this.createFields();
        this.createForm();
        Wtf.termds.on('load',this.setPDM,this);
        chktermload();
        this.centerPanel=new Wtf.Panel({
            border: false,
            region: 'center',
            id: 'centerpan'+this.id,
            autoScroll:true,
            bodyStyle:' background: none repeat scroll 0 0 #DFE8F6;',
            layout: 'border',
            items:[this.CustomerInfoForm,this.pan]
        });
        this.southPanel=new Wtf.Panel({
             region:'south',
                    height:200,
                    title:WtfGlobal.getLocaleText("acc.editors.otherdetailregion"),//'Other Details',
                    hidden:!Wtf.account.companyAccountPref.viewDetailsPerm,
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
        })        
        this.add(this.centerPanel,this.southPanel);
        this.tagsFieldset.on("add",function(){
            if(Wtf.getCmp("as")){
                Wtf.getCmp("as").doLayout();
            }
        },this)
        this.CustomerInfoForm.cascade(function(comp){
            if(comp.isXType('field')){
                comp.on('change', function(){this.isClosable=false;},this);
            }
        },this);
        if(this.readOnly){
            this.disableComponent();
        }
//        this.email.on('change',this.checkDuplicateEmail ,this);
//        this.code.on('change',this.checkDuplicateCode ,this);
//        this.createASCustOrVenCode.on('change',this.checkDuplicateCustOrVenCode ,this);
        this.copyAddress.on('check',this.setAddress ,this);
        this.hideFormFields();
    },
    hideFormFields:function(){
     this.isCustomer ? this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.customer) : this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.vendor);
    },
                            /*
     *  Function to hide/show formfields for personal details window 
     */
    hideTransactionFormFields:function(array){
        if(array){
            for(var i=0;i<array.length;i++){
                var fieldArray = array[i];
                if(Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id)){
                    if(fieldArray.fieldId=="ShowOnlyOneTime" && ((this.isEdit !=undefined ?this.isEdit:false) || (this.copyInv !=undefined ?this.copyInv:false) || (this.isCopyFromTemplate !=undefined ?this.isCopyFromTemplate:false) || (this.isTemplate !=undefined ?this.isTemplate:false))){
                        continue;
                    }
                    if(fieldArray.isHidden){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hideLabel = fieldArray.isHidden;
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hidden = fieldArray.isHidden;
                    }
                    /*
                     *'Add Opening Balance Sign' will be shown only if opening balance 
                     *field is not set hidden from company preferences
                     **/
                    if(fieldArray.fieldId =="openbalance" && !fieldArray.isHidden){
                        this.openingBal.addNewFn=this.addOpeningBalance.createDelegate(this);
                    }
                    if(fieldArray.isReadOnly){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).disabled = fieldArray.isReadOnly;
                    }
                    if(fieldArray.isUserManadatoryField && Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel != undefined){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).allowBlank = !fieldArray.isUserManadatoryField;
                        var fieldLabel="";
                        if(fieldArray.fieldLabelText!="" && fieldArray.fieldLabelText!=null && fieldArray.fieldLabelText!=undefined){
                            fieldLabel= fieldArray.fieldLabelText+" *";
                        }else{
                            fieldLabel=(Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel) + " *";
                        }
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel = fieldLabel;
                    }else{
                        if( fieldArray.fieldLabelText!=null && fieldArray.fieldLabelText!=undefined && fieldArray.fieldLabelText!=""){
                            if(fieldArray.isManadatoryField && fieldArray.isFormField )
                                Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel=fieldArray.fieldLabelText +"*";
                            else
                                Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel=fieldArray.fieldLabelText;
                        }
                  }
                        }
                    }
                }
    },
   createStore:function(){
        Wtf.countryStore.load();
        
       //If product typeahead then set Remotely for Multiselect else set by loading the product-ERP-20116
        if (this.isEdit && (this.productOptimizedFlag==Wtf.Show_all_Products))     
        {
             Wtf.productStore.load();//loading the product store to recognise & setting the product  in editing vendor
        }
        Wtf.countryStore.on('load',function() {
        	if(this.record!=null)
        		this.country.setValue(this.record.data.country);
        },this);

        this.parentRec = Wtf.data.Record.create ([
            {name:'parentid',mapping:'accid'},
            {name:'parentname',mapping:'accname'},
            {name: 'code',mapping:'acccode'},
            {name: 'groupname',mapping:'groupname'},
            {name:'email'},
            {name:'deleted'},
            {name: 'acccode'},
        ]);
        
        this.accountRec=new Wtf.data.Record.create([
            {name: 'parentid',mapping:'accid'},
            {name: 'groupid'},
            {name: 'nature'},
            {name: 'naturename'},
            {name: 'mastergroupid'},
            {name: 'parentname',mapping:'accname'},
            {name: 'deleted',type: 'bool'},
            {name: 'code',mapping:'acccode'},
            {name: 'isOnlyAccount'}
        ]);
        this.RPPaidToRec=new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'name'
        },

        {
            name: 'mappedReceivedFromId'
        }
        ]);
        this.parentStore = new Wtf.data.Store({
            url:"ACC"+this.businessPerson+"/get"+this.businessPerson+"sIdNameForCombo.do",
            baseParams:{
                mode:2,
                /*
                 * ERP-39110
                 * When new customer or copy customer is created at that time accountid should be null
                 */
                accountid:(this.record==null?null:this.isCopy ? null:this.record.data['accid']),
//                group:(this.isCustomer?[10,1,11,18,19]:[13,3,14,20,21]),
                receivableAccFlag:true//True to load normal recevebale and payables account in customer / vendor dropdown
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.parentRec)
        });
        this.parentStore.on('beforeload',function(){
            WtfGlobal.setAjaxTimeOut();
        },this);
        this.RPReceivedFromStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.RPPaidToRec),
            url:"ACCMaster/getMasterItems.do",
            baseParams:{
                mode:112,
                groupid:18
            }
        });

        this.MPPaidToRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);

        this.MPPaidToStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.MPPaidToRec),
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 17
            }
        });
        // using local store for checking vendor/customer exists (ERP-10385)
        this.localParentStore = new Wtf.data.SimpleStore({
          fields:this.parentRec
        });
//        this.AccountStore = new Wtf.data.Store({
////            url:"ACCAccountCMN/getAccountsForCombo.do",
//            url:"",
//            baseParams:{
//                mode:2,
//                ignoreGLAccounts:true,  
//                ignoreCashAccounts:true,
//                ignoreBankAccounts:true,
//                ignoreGSTAccounts:true,  
//                ignorecustomers:this.isCustomer?null:true,  
//                ignorevendors:this.isCustomer?true:null,  
//                accountid:(this.record==null?null:this.record.data['accid'])
//                //                group:(this.isCustomer?[10]:[13])
//            },
//            sortInfo : {
//                field : 'code',
//                direction : 'ASC'
//            },
//            reader: new Wtf.data.KwlJsonReader({
//                root: "data"
//            },this.accountRec)
//        });
//        this.AccountStoreForCopyCustomer = new Wtf.data.Store({      
////            url: "ACCAccountCMN/getAccountsForCombo.do",
//            url: "",
//            baseParams: {
//                mode: 2,
//                ignoreGLAccounts: true,
//                ignoreCashAccounts: true,
//                ignoreBankAccounts: true,
//                ignoreGSTAccounts: true,
//                ignorecustomers: !this.isCustomer ? null : true,
//                ignorevendors: !this.isCustomer ? true : null,
//                accountid: (this.record == null ? null : this.record.data['accid'])
//            },
//            reader: new Wtf.data.KwlJsonReader({
//                root: "data"
//            }, this.accountRec)
//        });
         this.currencyRec = new Wtf.data.Record.create([
            {name: 'currencyid',mapping:'tocurrencyid'},
            {name: 'symbol'},
            {name: 'currencyname',mapping:'tocurrency'},
            {name: 'exchangerate'},
            {name: 'htmlcode'}
        ]);
        this.currencyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.currencyRec),
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
        
        this.sequenceFormatStoreRec = new Wtf.data.Record.create([
        {name: 'id'},
        {name: 'value'}
        ]);
        
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.sequenceFormatStoreRec),
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:this.modeName,
                isEdit:this.isEdit
            }
        });
        
        this.sequenceFormatStoreVenCus = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.sequenceFormatStoreRec),
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:this.businessPerson=="Customer"?"autovendorid":"autocustomerid"//reverse mode is given for create as venor and create as customer1
            }
        });
        
        this.currencyStore.on("load",function(store){
            if(store.getCount()==0){
                callCurrencyExchangeWindow(this.currencyExchangeWinId);
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.common.currency")],2);     //'Alert',"Please set Currency Exchange Rates"
                Wtf.getCmp(this.currencyExchangeWinId).on("update", function(){
                    this.currencyStore.reload();
                    this.defaultCurrencyStore.reload();
                },this);
            }
        },this)
        this.hideLoading(true);
        if(this.isEdit)WtfComMsgBox(29,4,true);//showing message while loading data in edit case
        this.defaultCurrencyStore.load();
        var selectedCustomerIds = "";
        if(this.isEdit && this.custVenOptimizedFlag==1 && this.isCustomer){//Typeahead case sending only customerid
            /*
             *In case of type ahead no need to load all customers. If parent is selected parentid will send or account id will send to load records.
             *If User wants to edit parent customer he new request will be send when he edits parent combo
             **/
            selectedCustomerIds=this.record != null ? this.record.data['parentid'] ? this.record.data['parentid'] : this.record.data['accid']:null;
        } else if (this.isEdit && this.isCustomer) {
            selectedCustomerIds = this.record != null ? this.record.data['accid'] : null;
        }
        this.parentStore.load({
            params : {
                selectedCustomerIds : selectedCustomerIds
            }
        });
//        this.AccountStore.load();
        this.RPReceivedFromStore.load();
        this.MPPaidToStore.load();
        var mapcustomervendor=false;
        if(this.record!=null && this.record.data['mapcustomervendor']!=undefined && this.record.data['mapcustomervendor']!='undefined'){
            mapcustomervendor=this.record.data['mapcustomervendor'];
        }
//        if (this.isEdit && mapcustomervendor) {//loading accounts of vendor when it is create as vendor true.
//            this.AccountStoreForCopyCustomer.load();
//        }
        // In edit case the creation date should be persons creation date for currency store
        this.currencyStore.load({params:{mode:201,transactiondate: this.isEdit ? WtfGlobal.convertToGenericDate(this.record.data.creationDate) : WtfGlobal.convertToGenericDate(new Date())}});
        chktitleload();
        if(this.businessPerson=="Customer"){
            chkCustomerCategoryload();
        } else {
            chkVendorCategoryload();
        }
        chkInterCompanyTypeload();
        this.parentStore.on('load',this.loadRecord,this);
        Wtf.TitleStore.on('load',this.setPersonTitle,this);
        this.currencyStore.on('load', function(){
            if(this.isEdit || this.isCopy){
                this.Currency.setValue(this.record.data.currencyid);
            } else if(Wtf.account.companyAccountPref.currencyid){
                // Set to Base Currency if currency is not assigned.
                if(this.Currency.getValue() ==""|| this.Currency.getValue() == null|| this.Currency.getValue() == undefined)
                    this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
                if(this.Currency.getValue() !="" && WtfGlobal.searchRecord(this.currencyStore,this.Currency.getValue(),"currencyid") == null){ 
                    // If set currency do not have exchange rate then reset currency
                    this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
                    callCurrencyExchangeWindow();
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invoice.msg1")],2);
                }
            }
            this.hideLoading(false);
        },this);
        Wtf.CustomerCategoryStore.on("load", this.setCategory, this);
        Wtf.VendorCategoryStore.on("load", this.setCategory, this);
        Wtf.InterCompanyTypeStore.on("load", this.setInterCompanyType, this);

        Wtf.termds.on('loadexception',this.hideLoading.createDelegate(this,[false]),this);
        this.currencyStore.on('loadexception',this.hideLoading.createDelegate(this,[false]),this);
        this.parentStore.on('loadexception',this.hideLoading.createDelegate(this,[false]),this);

    },
    createFields:function(){
        this.Title= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.title"),  //'Title',
            name:'title',
            id: "title" + this.heplmodeid+ this.id,
            forceSelection: true,
            width:200,
            store:Wtf.TitleStore,
            valueField:'id',
            displayField:'name'//,
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.create))
            this.Title.addNewFn=this.addMaster.createDelegate(this,[6,Wtf.TitleStore])

//        this.Category= new Wtf.form.FnComboBox({
//            fieldLabel:WtfGlobal.getLocaleText("acc.cust.category")+WtfGlobal.addLabelHelp((this.isCustomer?WtfGlobal.getLocaleText("acc.cust.msg1"):WtfGlobal.getLocaleText("acc.ven.msg1"))),
//            name:'category',
//            width:200,
//            forceSelection: true,
//            store: (this.businessPerson=="Customer")?Wtf.CustomerCategoryStore:Wtf.VendorCategoryStore,
//            valueField:'id',
//            displayField:'name',
//            addNoneRecord: true
//        });
 //product mapping-Neeraj D

        this.preferredProductRec = Wtf.data.Record.create([
            {name: 'productid'},
            {name: 'productname'},
            {name: 'producttype'},
            {name: 'pid'},
            {name: 'type'},
            {name: 'hasAccess'},
        ]);

        this.preferredProductStore = new Wtf.data.Store({
            url: "ACCProductCMN/getProductsForDropdownOptimised.do",
            baseParams: {
                mode: 22
            },
            reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: "totalCount"
            }, this.preferredProductRec)
        });
        this.ProductMapping = new Wtf.common.SelectPaging({
            multiSelect:true,
            fieldLabel:"<span wtf:qtip='"+(this.isCustomer?WtfGlobal.getLocaleText("acc.customer.preferedprod.tt"):WtfGlobal.getLocaleText("acc.vendor.preferedprod.tt") )+"'>"+  WtfGlobal.getLocaleText("acc.field.PreferredProduct(s)")  +"</span>",// WtfGlobal.getLocaleText("acc.field.PreferredProduct(s)"),
            forceSelection:true,
            isProductCombo:true,
            name:'productmapping',
            id: "productmapping" + this.heplmodeid+ this.id,
            width:200,
            maxHeight:250,    //ERP-9826
            store:this.preferredProductStore,
            extraFields:['pid','type'],
            extraComparisionField:'pid',// type ahead search on product id as well.
            listWidth:Wtf.ProductComboListWidth,
            listAlign:"bl-tl?", //ERP-9826
            valueField:'productid',
            displayField:'productname',
            addNoneRecord: true,
            selectOnFocus:true,
            clearTrigger: this.readOnly ? false : true,
            editable:true,
            triggerAction:'all',
            mode: 'remote',
            pageSize:Wtf.ProductCombopageSize
//            addNewFn:this.openProductWindow.createDelegate(this),
//            addCreateOpt:false
        });   
        
        this.addPreferredProduct = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.button.prefProductButton"),
            tooltip: WtfGlobal.getLocaleText("acc.button.setCustomFieldForProducts"),
            id: "saveasdraft" + this.id,
            disabled:true,
            scope: this,
            style:'margin-left:165px;margin-top:7px;margin-bottom:10px;',
            handler: function() {
               this.openProductWindow();
            },
            iconCls: 'pwnd save'
        });
      
        this.ProductMapping.on('beforeselect', function(combo, record, index) {
                return validateSelection(combo, record, index);
        }, this);
        this.ProductMapping.on('select', function (combo, record, index) {
            /**
             * remove comma which appended beggining. 
             */
            if (combo.getValue().charAt(0) === ',') {
                combo.setValue(combo.getValue().substr(1));
            }
                 this.addPreferredProduct.enable();
        }, this);
         this.ProductMapping.on('clearval', function(combo, record, index) {
                 this.addPreferredProduct.disable();
        }, this);
          this.ProductMapping.on('unselect', function(combo, record, index) {
              if(this.ProductMapping.getValue()!=""){
                  this.addPreferredProduct.enable();
              } else{
                  this.addPreferredProduct.disable();
              }
        }, this);
        this.fillIBGDetails = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.fillibgDetais"),
            name:'fillIBGDetails',
            tooltip: WtfGlobal.getLocaleText("acc.fillibgDetais"),
            scope: this,
            hidden:!Wtf.account.companyAccountPref.activateIBG || this.isCustomer,
            handler:this.fillIBGDetailsValues
        });
        
        this.isActivateIBG=new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.ibg.bank.info"),
            checkboxToggle: true,
            autoHeight: true,
            disabledClass:"newtripcmbss",
            hidden:true,//!Wtf.account.companyAccountPref.activateIBG || this.isCustomer,// hide if IBG is not activated for this company
            hideLabel:true,//!Wtf.account.companyAccountPref.activateIBG || this.isCustomer,
            autoWidth: true,
            checkboxName: 'activateIBG',
            style: 'margin-right:30px',
            collapsed: true,
            items:[this.fillIBGDetails]
        });

        
        this.pmtRec = new Wtf.data.Record.create([
            {name: 'methodid'},
            {name: 'methodname'},
            {name: 'accountid'},
            {name: 'acccurrency'},
            {name: 'accountname'},
            {name: 'isIBGBankAccount', type: 'boolean'},
            {name: 'isdefault'},
            {name: 'detailtype', type: 'int'},
            {name: 'acccustminbudget'},
            {name: 'autopopulate'}
        ]);
        
        this.pmtStore=new Wtf.data.Store({
            reader:new Wtf.data.KwlJsonReader({
                root:"data"
            },this.pmtRec),
            url : "ACCPaymentMethods/getPaymentMethods.do",
            baseParams:{
                mode:51
            }
        });
        this.pmtMethod= new Wtf.form.ComboBox({
           fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.defaultPaymentSelection.ttip") +"'>"+WtfGlobal.getLocaleText("acc.mp.defaultPayMehod"),
           name:'paymentmethod',
           id:"paymentMethod"+this.heplmodeid+this.id,
           store:this.pmtStore,
           valueField:'methodid',
           displayField:'methodname',
           hidden:(this.mapDefaultPmtMethod && this.isCustomer)?false:true,
           hideLabel:(this.mapDefaultPmtMethod && this.isCustomer)?false:true,
        //   allowBlank:false,
           triggerAction:'all',
           emptyText:WtfGlobal.getLocaleText("acc.mp.selpayacc"),
           width:200,
           mode:'remote',
           typeAhead:true,
           forceSelection:true
        });
        
        this.lifoFifoStore = new Wtf.data.SimpleStore({
            fields : ['id', 'name'],
            data: [
                ['1','NA'],['2','LIFO'],['3','FIFO']
            ]
        });
        
        this.lifoFifoCombo = new Wtf.form.FnRefreshBtn({
            fieldLabel: WtfGlobal.getLocaleText("acc.filed.paymentCriteria"), // "Payment Criteria",
            name: "paymentCriteria",
            id: "paymentCriteria" + this.heplmodeid+ this.id,
            store: this.lifoFifoStore,
            valueField: 'id',
            displayField: 'name',
            hideLabel: false,
            mode: 'local',
            triggerAction: 'all',
            width:200,
            emptyText: WtfGlobal.getLocaleText("acc.filed.selectapaymentCriteria"), // "Select a Payment Criteria",
            forceSelection: true
        });
        
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
        this.defaultNatureOfPurchase= new Wtf.form.FnComboBox({
            fieldLabel: "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.TypeofSales") +"'>"+ WtfGlobal.getLocaleText("acc.field.TypeofSales") +"</span>",
            hidden: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ),
            hideLabel: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA),
            name:'defaultnatureofpurchase',
            //width:200,
            anchor :'80%',
            store:Wtf.defaultNatureOfPurchaseStore,
            emptyText:WtfGlobal.getLocaleText("acc.field.TypeofSalesSel"),//'Select a nature of purchase',
            valueField:'id',
            displayField:'name'
        });
        this.manufacturerTypeCombo=new Wtf.form.ComboBox({
            fieldLabel:this.businessPerson=='Customer' ?WtfGlobal.getLocaleText("acc.field.india.typeofmanufacturer"):WtfGlobal.getLocaleText("acc.field.india.typeofmanufacturer"),//'Type of Manufacturer',
            store:Wtf.manufactureTypeStore,
            name:'manufacturerType',
            id:'manufacturerType'+this.id,
//            width:200,
//            listWidth:200,
            anchor :'80%',
            hiddenName:'manufacturerType',
            valueField:'id',
            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.isExciseApplicable || Wtf.account.companyAccountPref.registrationType == "Dealer",
            hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.isExciseApplicable || Wtf.account.companyAccountPref.registrationType == "Dealer",
            disabled : this.businessPerson=='Customer' ? true:false,
            mode:'local',
            displayField:'name',
            forceSelection: true,
            triggerAction: 'all',
            value:this.businessPerson=="Customer"?"":Wtf.manufacturerType,
            selectOnFocus:true
//            allowBlank: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.isExciseApplicable || this.businessPerson=="Customer" || Wtf.account.companyAccountPref.registrationType == "Dealer") 
        });
        this.ECCNo =new Wtf.form.TextField({
            fieldLabel: this.isCustomer ? WtfGlobal.getLocaleText("acc.field.Customerecc") : WtfGlobal.getLocaleText("acc.field.Vendorecc")  ,// "Customer ECC" : "Vendor ECC"
            name: 'eccno',
            //width:200,
            anchor :'80%',
            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            maxLength:15,
            invalidText :'Alphabets and numbers only',
            vtype : "alphanum"
        });
        
        this.importerECCNo =new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.ImporterECCNo"),//'Importer ECC No.',
            name: 'importereccno',
            //width:200,
            anchor :'80%',
            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            maxLength:15,
            invalidText :'Alphabets and numbers only',
            vtype : "alphanum"
        });
        

        this.IECno =new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.IECNumber"),//'IEC Number',
            hidden: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ),
            hideLabel: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA),
            name:"iecno",
            maxLength:10,
            //width:200,
            anchor :'80%',
            maskRe: /[0-9]/
        });
        
        this.rangeCode =new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Range"),//'Range',
            name:"range",
            hidden: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ),
            hideLabel: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA),
            //width:200,
            anchor :'80%'
        });
        
        this.divisionCode =new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Division"),//'Division',
            name:"division",
            hidden: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ),
            hideLabel: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA),
            //width:200,
            anchor :'80%'
        });
        
        this.commissionerateName =new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Commissionerate"),//'Commissionerate',
            name:"commissionerate",
            hidden: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ),
            hideLabel: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA),
            //width:200
            anchor :'80%'
        });
        
        this.exiceDetailsFieldSet=new Wtf.form.FieldSet({
            border:false,
           // xtype:'fieldset',
            autoWidth:true,
            width:250,
            hidden: WtfGlobal.GSTApplicableForCompany()==Wtf.GSTStatus.OLDNEW ? !Wtf.isExciseApplicable : true,
            autoHeight:true,
            checkboxToggle: true,
            checkboxName:"isEXICEapplicableoncompany",           
            disabledClass:"newtripcmbss", 
            title:WtfGlobal.getLocaleText("acc.field.ExiceDetails"),//'Exice Details',
            defaults:{border:false},
            items:[this.manufacturerTypeCombo,this.defaultNatureOfPurchase,this.ECCNo,this.importerECCNo,this.IECno,this.rangeCode,this.divisionCode,this.commissionerateName]
        });
        this.pricingBand = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.pricingBandMaster"), // "Pricing Band",
            id: "bandNameCombo" + this.id,
            store: this.pricingBandMasterStore,
            displayField: 'name',
            valueField: 'id',
            emptyText: WtfGlobal.getLocaleText("acc.field.selectBandName"), // 'Select Band Name',
            mode: 'local',
            width: 200,
            name: 'pricingBand',
            hiddenName:'pricingBand',
            triggerAction: 'all',
            forceSelection: true,
            selectOnFocus: true,
            scope: this,
            hidden: (!this.isCustomer && !Wtf.account.companyAccountPref.productPricingOnBands) || (this.isCustomer && !Wtf.account.companyAccountPref.productPricingOnBandsForSales),
            hideLabel: (!this.isCustomer && !Wtf.account.companyAccountPref.productPricingOnBands) || (this.isCustomer && !Wtf.account.companyAccountPref.productPricingOnBandsForSales)
        });
        
        this.UENNo= new Wtf.form.TextField({
           fieldLabel: this.isCustomer ? WtfGlobal.getLocaleText("acc.field.CustomerUEN") : WtfGlobal.getLocaleText("acc.field.VendorUEN"),// "Customer UEN" : "Vendor UEN"
           name: 'uenno',
           id: "uenno" + this.heplmodeid+ this.id,
           hidden: Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA,
           hideLabel: Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA,
           width:200
        });
        
        this.minPriceValueForVendor= new Wtf.form.NumberField({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.BlockPOcreationfield") ,// block the PO Creation if it is less than given price value for selected vendor
        name: 'minpricevalueforvendor',
        id: "minpricevalueforvendor" + this.heplmodeid+ this.id,
        disabled:this.readOnly,
        hidden:this.isCustomer || !Wtf.account.companyAccountPref.blockPOcreationwithMinValue,
        hideLabel:this.isCustomer || !Wtf.account.companyAccountPref.blockPOcreationwithMinValue,
//        anchor: '65%',
        width: 200,
        maxLength: 10,
        invalidText: 'numbers only',
        allowDecimals:true,
        allowNegative:false,
        decimalPrecision: Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
    });
        
      this.VATTINNo= new Wtf.form.TextField({
           fieldLabel: this.isCustomer ? WtfGlobal.getLocaleText("acc.field.CustomerVATTINNO"): WtfGlobal.getLocaleText("acc.field.VendorVATTINNO") ,// "Customer VTN" : "Vendor VTN"
           name: 'vattinno',
           //width:200,
           anchor : '80%',
           hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
           hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
           maxLength:11,
           regex:/\d{10}[a-z | A-Z | 0-9 ]{1}/, 
           invalidText :'The value in this field is invalid'
        });
        this.VATTINNo.on('blur',function(){
            this.funVATDealerType();
        },this);
        this.vatRegDate= new Wtf.form.FnDateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.VATRegDate"),//'VAT Reg Date',
            name: 'vatregdate',
            //width:200,
            anchor : '80%',
            hidden: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            hideLabel: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            format:WtfGlobal.getOnlyDateFormat(),
            maxValue : new Date() // VAT reg. Date Should not be greater than current date.
        });
        this.CSTTINNo= new Wtf.form.TextField({
           fieldLabel: this.isCustomer ? WtfGlobal.getLocaleText("acc.field.CustomerCSTTINNO") : WtfGlobal.getLocaleText("acc.field.VendorCSTTINNO"),// "Customer VTN" : "Vendor VTN"
           name: 'csttinno',
           //width:200,
           anchor : '80%',
           hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
           hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
           maxLength:11,
           regex:/\d{10}[a-z | A-Z | 0-9 ]{1}/, 
           invalidText :'The value in this field is invalid'
        });
        this.CSTTINNo.on('blur',function(){
            this.funVATDealerType();
        },this);
        
        /*CST Reg. Date filed in Customer & Vendor Master is required for Form 402.
         *this filed is enable only when VAT/CST check enable from company master.
         *CST reg. Date Should not be greater than current date.
         **/
        this.CSTRegDate= new Wtf.form.FnDateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.CSTRegDate"),//'CST Reg. Date',
            name: 'cstregdate',
            anchor : '80%',
            hidden: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            hideLabel: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            format:WtfGlobal.getOnlyDateFormat(),
            maxValue : new Date() 
        });
        
        // Store For Dealer Type to Maharashtra
        // Ticket :ERP-26370   field use for MVAT Annexure Form  Indian Company only for Maharashtra
        var dealerStoreData = [];
        dealerStoreData.push(['1',WtfGlobal.getLocaleText("acc.field.registerDealer")]);
        dealerStoreData.push(['2',WtfGlobal.getLocaleText("acc.field.unregisterDealer")]);
        if( Wtf.Stateid == Wtf.StateName.MAHARASHTRA){
            dealerStoreData.push(['3',WtfGlobal.getLocaleText("acc.field.CompositionDealeru/s42(3),(3A)&(4)")]);
            dealerStoreData.push(['4',WtfGlobal.getLocaleText("acc.field.CompositionDealeru/s42(1),(2)")]);
        }
        this.typeofDealerStore = new Wtf.data.SimpleStore({
            fields: [{
                name:'id'
            },{
                name:'name'
            }],
            data:dealerStoreData
        });
        
        this.dealerTypeCombo= new Wtf.form.FnComboBox({
            fieldLabel: (Wtf.account.companyAccountPref.enablevatcst?WtfGlobal.getLocaleText("acc.field.VatDealerType")+"*":WtfGlobal.getLocaleText("acc.field.VatDealerType")) + WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.customerInformation.VATDealerMsg")),//"Vat Dealer Type", 
            name: "dealertype",
            hidden: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            hideLabel: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            store: this.typeofDealerStore,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            triggerAction: 'all',
            //width:200,
            anchor : '80%',
            emptyText: 'Select a type',
            forceSelection: true,
            allowBlank : (WtfGlobal.GSTApplicableForCompany()==Wtf.GSTStatus.OLDNEW && Wtf.account.companyAccountPref.enablevatcst)?false:true
        });
        this.dealerTypeCombo.on('select',this.funVATDealerType,this);
        this.interStateParty= new Wtf.form.Checkbox({
            name:'interstateparty',
            hidden: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            hideLabel: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            fieldLabel: WtfGlobal.getLocaleText("acc.field.InterstateParty"),//'Interstate Party',
            checked:false,
            cls : 'custcheckbox',
            width: 10
        });
        this.interStateParty.on('check',this.enabledisableCForm,this);
        this.cFormApplicable= new Wtf.form.Checkbox({
            name:'cformapplicable',
            hidden: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            hideLabel: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            fieldLabel:WtfGlobal.getLocaleText("acc.field.CFormApplicable"),//'C Form Applicable',
            checked:false,
            disabled:true,
            cls : 'custcheckbox',
            width: 10
        });
        this.gtaApplicable= new Wtf.form.Checkbox({ // GTA Applicable  ERP-25539
            name:'gtaapplicable',
            hidden: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || this.isCustomer ),
            hideLabel: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || this.isCustomer),
            fieldLabel:WtfGlobal.getLocaleText("acc.compref.india.rcm.applicable"),
            checked:false,
            disabled:false,
            cls : 'custcheckbox',
            width: 10
        });
        this.vendorBranchStatus = new Wtf.data.SimpleStore({
            fields : ['id', 'name'],
            data: [
              ['1','Head Office'],['2','Branch'],['3','Agent'],['4','Principal'] 
            ]
        });
        // vendor Branch Status
        this.vendorBranchCombo = new Wtf.form.FnRefreshBtn({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.india.vendor.branch"),
            name: "vendorbranch",
            hidden: ( Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.account.companyAccountPref.stateid==Wtf.StateName.DELHI )?false:true,
            hideLabel: ( Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.account.companyAccountPref.stateid==Wtf.StateName.DELHI )?false:true,
            store: this.vendorBranchStatus,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            triggerAction: 'all',
            anchor: '80%',
            forceSelection: true
        });
        this.isVATCSTapplicableOnCompanyFieldSet=new Wtf.form.FieldSet({
            border:false,
            //xtype:'fieldset',
            autoWidth:true,
            width:250,   
            hidden:(WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW ||!Wtf.account.companyAccountPref.enablevatcst),
            autoHeight:true,
            checkboxToggle: true,
            checkboxName:"isVATCSTapplicableoncompany",           
            disabledClass:"newtripcmbss", 
            title:WtfGlobal.getLocaleText("acc.product.VatDetails"),//' VAT Details',
            defaults:{
                border:false
            },
            items:[this.VATTINNo,this.CSTTINNo,this.CSTRegDate,this.vatRegDate,this.dealerTypeCombo,this.vendorBranchCombo,this.interStateParty,this.cFormApplicable]
        });
        this.isVATCSTapplicableOnCompanyFieldSet.on('collapse',function(){this.funEnableDisableCombo(this.isEdit,true)},this);
        this.isVATCSTapplicableOnCompanyFieldSet.on('expand',function(){this.funEnableDisableCombo(this.isEdit,false)},this);
        
        /*India Compliance GST details - START*/
        
        this.GSTIN= new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText('acc.india.vecdorcustomer.column.gstin')+WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.india.vecdorcustomer.column.gstin.invalid")),
            name: 'gstin',
            anchor : '80%',
            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hideLabel:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            maxLength:15,
            disabled : true,
            minLength:15,
            //regex:/\d{2}[A-Za-z]{5}\d{4}[A-Za-z]{1}\d[Zz]{1}[A-Za-z\d]{1}/, 
           // regex:Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA?/\d{2}[A-Z]{5}\d{4}[A-Z]{1}\d[Zz]{1}[A-Za-z\d]{1}/:null,
            invalidText : 'Invalid GSTIN.' //WtfGlobal.getLocaleText('acc.india.vecdorcustomer.column.gstin.invalid')
        });
        this.GSTIN.on('change',this.validateGSTIN,this);
        // GSTIN Registration Type *********************************
         this.GSTINRegistrationTypeRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
            {name: 'defaultMasterItem'} // Added default master key in records
        ]);
        this.GSTINRegistrationTypeStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.GSTINRegistrationTypeRec),
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 62
            }
        });
        this.GSTINRegistrationTypeStore.load();
        this.GSTINRegistrationTypeCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.masterConfig.62") + "*", //GSTIN Registration Type
            hiddenName: "mappingPaidToCmb",
            id: "GSTINRegistrationType" + this.heplmodeid + this.id,
            store: this.GSTINRegistrationTypeStore,
            valueField: 'id',
            displayField: 'name',
            allowBlank: (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && WtfGlobal.GSTApplicableForCompany()==Wtf.GSTStatus.NEW) ? false : true, //ERP-35237
            disabled: this.readOnly|| this.isEdit?true:false,
            emptyText: WtfGlobal.getLocaleText('acc.common.select') + " " + WtfGlobal.getLocaleText("acc.masterConfig.62"), //Select GSTIN Registration Type
            minChars: 1,
            extraFields: '',
            anchor : '80%',
            extraComparisionField: 'name', // type ahead search 
            mode: 'local',
          //  addNewFn: this.addPaidTo.createDelegate(this),
            triggerAction: 'all',
            typeAhead: true,
            forceSelection: true
        });
        // Customer & Vendor Registration Type *********************************
         this.CustomerVendorTypeRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
            {name: 'defaultMasterItem'} // Added default master key in records
        ]);
        this.CustomerVendorTypeStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.CustomerVendorTypeRec),
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 63
            }
        });
        this.CustomerVendorTypeStore.load();
        this.CustomerVendorTypeCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: this.isCustomer ? WtfGlobal.getLocaleText("acc.customer.GST.type") + "*" : WtfGlobal.getLocaleText("acc.vendor.GST.type") + "*", //Customer/ Vendor Type
            hiddenName: "mappingPaidToCmb",
            id: "CustomerVendorType" + this.heplmodeid + this.id,
            store: this.CustomerVendorTypeStore,
            valueField: 'id',
            displayField: 'name',
            allowBlank: (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && WtfGlobal.GSTApplicableForCompany()==Wtf.GSTStatus.NEW) ? false : true, //ERP-35237
            emptyText: WtfGlobal.getLocaleText('acc.common.select') + " " + (this.isCustomer ? WtfGlobal.getLocaleText("acc.customer.GST.type") : WtfGlobal.getLocaleText("acc.vendor.GST.type")), //Select Customer/ Vendor Type
            minChars: 1,
            extraFields: '',
            anchor : '80%',
            disabled: this.readOnly|| (this.isEdit && !(Wtf.account.companyAccountPref.countryid == Wtf.Country.US))?true:false,
            extraComparisionField: 'name', // type ahead search 
            mode: 'local',
          //  addNewFn: this.addPaidTo.createDelegate(this),
            triggerAction: 'all',
            typeAhead: true,
            forceSelection: true
        });
        this.gstHistory = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.india.vecdorcustomer.column.gstdetails"),
            tooltip: WtfGlobal.getLocaleText("acc.gsthistory.mastertoolip"),
            id: "saveasdraft" + this.id,
            disabled: this.isEdit?false:true,
            scope: this,
            style: 'margin-left:165px;margin-top:7px;margin-bottom:10px;',
            handler: function() {
                this.openHistoryWindow();
            },
            iconCls: 'pwnd save'
        });
        /**
         * ERP-35801
         * In Customer Master: Deemed Export, SEZ have to shown, Remove Import from Customer Master
         * In Vendor Master: SEZ have to shown & remove Deemed Export from Vendor Master
         */
        if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
            this.CustomerVendorTypeStore.on('load', function (store) {
                /**
                 * Hide GST Customer/ Vendor type
                 * ERP-35464
                 */
                removeGSTDetailsNotUsedFromStore(this.CustomerVendorTypeStore,true,this.isCustomer);
            }, this);
            /**
             * FOR India GST Details No need to Show GST Registration type as Consumer in Vendor Master
             * ERP-35832
             */
            this.GSTINRegistrationTypeStore.on('load', function (store) {
                /**
                 * Hide GST Registration type
                 * ERP-35464
                 */
                removeGSTDetailsNotUsedFromStore(this.GSTINRegistrationTypeStore,false,this.isCustomer);
            }, this);
            }
        /*
         * FOR US GST Details Only need to Show GST Registration type as NA and Tax Exempt in Vendor Master
         * ERM-697
         */
        if(WtfGlobal.isUSCountryAndGSTApplied()){
            this.CustomerVendorTypeStore.on('load', function (store) {
                this.CustomerVendorTypeStore.each(function (record) {
                    var recdata = record.data;
                    if (this.isCustomer && recdata.defaultMasterItem == Wtf.GSTCUSTVENTYPE.Import ||recdata.defaultMasterItem == Wtf.GSTCUSTVENTYPE.DEEMED_EXPORT || recdata.defaultMasterItem == Wtf.GSTCUSTVENTYPE.Export || recdata.defaultMasterItem == Wtf.GSTCUSTVENTYPE.ExportWOPAY || recdata.defaultMasterItem == Wtf.GSTCUSTVENTYPE.SEZ || recdata.defaultMasterItem == Wtf.GSTCUSTVENTYPE.SEZWOPAY) {
                        this.CustomerVendorTypeStore.remove(record);
                    }
                }, this);
            }, this);
        }
    /**
     * GST Details Changes for Customer vendor. IF customer/ Vendor used in Transaction then show confirm message.
     * GST details - GSTIN Registration Type , Customer/ Vendor Type
     * */
        if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && WtfGlobal.GSTApplicableForCompany() == Wtf.GSTStatus.NEW) {
            this.GSTINRegistrationTypeCombo.on('select', function (combo, record, index) {
                this.validateGSTINNumber();
                this.CustomerVendorTypeCombo.clearValue(); // Reset Csutomer/ Vendor type on GST Registration combo select
                this.validateSEZDates(); // Validate SEZ dates
            }, this);
            /*
             * ERP-35464
             * For Customer And Vendor Type Validation 
             */
            this.CustomerVendorTypeCombo.on('select', this.validateCustomerVendorType, this);
            if (this.isEdit) {
                this.CustomerVendorTypeCombo.on('change', this.GSTDetailsBeforeSelect, this);
                this.GSTINRegistrationTypeCombo.on('change', this.GSTDetailsBeforeSelect, this);
            }
        }
        this.sezFromDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText('acc.GST.sezFromDate'),
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'sezfromdate',
            anchor : '80%',
            disabled : true
        });
        this.sezToDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText('acc.GST.sezToDate'),
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'seztodate',
            anchor : '80%',
            disabled : true
        });
        this.itemsArr = [];                    
        if (Wtf.account.companyAccountPref.countryid == Wtf.Country.US) {
            this.itemsArr.push(this.CustomerVendorTypeCombo);
        } else {
            this.itemsArr.push(this.GSTINRegistrationTypeCombo, this.GSTIN, this.CustomerVendorTypeCombo,this.gstHistory);
        }
        this.GSTdetailsFieldSet=new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.india.vecdorcustomer.column.gstdetails"), //GST Details
            id:'gstdetail_id',
            disabledClass:"newtripcmbss", 
            autoHeight: true,
            hidden: ((Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA||Wtf.account.companyAccountPref.countryid==Wtf.Country.US) && !Wtf.account.companyAccountPref.avalaraIntegration)?false:true,
            autoWidth: true,
            width : 250,
            items:this.itemsArr//this.sezFromDate,this.sezToDate] // Changes Sequences of this.GSTIN
        });
       
        /*India Compliance GST details - END*/
        
        this.PANNo= new Wtf.form.TextField({
           fieldLabel: this.isCustomer ? WtfGlobal.getLocaleText("acc.field.CustomerPANNO")+WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.customerInformation.paninfo")) : WtfGlobal.getLocaleText("acc.field.VendorPANNO")+'*'+WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.customerInformation.paninfo")) ,// "Customer PAN" : "Vendor PAN"
           name: 'panno',
           width: '200',
           hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
           hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
           maxLength:10,
           invalidText :'Alphabets and numbers only',
           vtype : "alphanum",
           regex:/[A-Z]{5}\d{4}[A-Z]/, //[a-z]{3}[cphfatblj][a-z]\d{4}[a-z]/i,
           regexText:'Invalid PAN eg."AAAAA1234A"',
           allowBlank : (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isTDSApplicable && !this.isCustomer )?false:true
        });
        this.npwp =new Wtf.form.TextField({
            fieldLabel: this.isCustomer ? WtfGlobal.getLocaleText("acc.field.CustomerNPWPNO"): WtfGlobal.getLocaleText("acc.field.VendorNPWPNO"),// "Customer NPWP" : "Vendor NPWP"
            name:"npwp",
            width:'200',
            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDONESIA,
            hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDONESIA,
            maxLength: 20,
            invalidText :'Numbers only',
            regex:/\d{2}\.\d{3}\.\d{3}\.\d{1}[-.]\d{3}\.\d{3}/,
            regexText:'Invalid NPWP No. (eg."01.567.505.1-056.000")'
        });
        
        this.PANNo.on('blur',function(){
            this.panStatusFun();
        },this);
        
        this.gtaApplicable.on('change',function(o,newval,oldval){
            if(Wtf.isEmpty(Wtf.GTAKKCPaybleAccount)|| Wtf.isEmpty(Wtf.GTASBCPaybleAccount) || Wtf.isEmpty(Wtf.STPayableAcc)){
                o.setValue(oldval)
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Please select service tax payble account first"], 2);
            }
        },this);
        
        this.panStatusStore = new Wtf.data.SimpleStore({
            fields : ['id', 'name'],
            data: [
              ['2','PANNOTAVBL'],['3','APPLIEDFOR'] 
            ]
        });
        this.deducteeCodeStore = new Wtf.data.SimpleStore({
            fields : ['id', 'name'],
            data: [
              ['1','Company'],['2','Other Than Company'] 
            ]
        });
        // Pan Status
        this.panStatusCombo = new Wtf.form.FnRefreshBtn({
            fieldLabel:this.businessPerson=='Customer'?WtfGlobal.getLocaleText("acc.field.CustomerPAN")+WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.customerInformation.panstatusinfo")):WtfGlobal.getLocaleText("acc.field.VendorPAN")+WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.customerInformation.panstatusinfo")),
            name: "panstatus",
            hidden: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.isTDSApplicable),
            hideLabel: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.isTDSApplicable),
            store: this.panStatusStore,
            valueField: 'id',
//            disabled : this.businessPerson=='Customer' ? true:false,
            displayField: 'name',
            mode: 'local',
            triggerAction: 'all',
            width:'200',
            emptyText: WtfGlobal.getLocaleText("acc.field.VendorPANStatus"),
            forceSelection: true
        });
        // Deductee Type Combo
        this.deducteeType= new Wtf.form.FnComboBox({
            fieldLabel: this.businessPerson=='Customer' ? "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.DeducteeType.tt") +"'>"+ WtfGlobal.getLocaleText("acc.field.DeducteeType") +"</span>":WtfGlobal.getLocaleText("acc.field.DeducteeType"),//'Deductee Type', 
            hidden: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.isTDSApplicable|| this.isCustomer),
            hideLabel: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.isTDSApplicable|| this.isCustomer),
            disabled : this.businessPerson=='Customer' ? true:false,
            name:'deducteetype',
            width:'200',
            store:Wtf.deducteeTypeStore,
            emptyText:'Select a deductee type',
            valueField:'id',
            displayField:'name'
        });
        this.deducteeCode= new Wtf.form.FnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.DeducteeCode"),//'Deductee Type', 
            hidden: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.isTDSApplicable || this.businessPerson=='Customer'),
            hideLabel: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.isTDSApplicable || this.businessPerson=='Customer'),
            name:'deducteeCode',
            width: '200',
            disabled:true,//ERP-28618
            store:this.deducteeCodeStore,
//            emptyText:'Select a deductee code',
            valueField:'id',
            displayField:'name'
        });
        
        this.residentialstatus0=new Wtf.form.Radio({
            boxLabel:WtfGlobal.getLocaleText("acc.vendor.Resident.text"),
            id: "residential"+this.id,
            name:'residentialstatus',
            checked:true,
            width: '200',
            labelSeparator:'',
//            disabled:this.businessPerson=="Customer" ? true : false,
            labelWidth:0,
            value:0
        });
        this.residentialstatus1=new Wtf.form.Radio({
            boxLabel:WtfGlobal.getLocaleText("acc.vendor.NonResident.text"),
            id: "nonresidential"+this.id,
            name:'residentialstatus',
            width: '200',
//            disabled:this.businessPerson=="Customer" ? true : false,
            labelSeparator:'',
            value:1
        });
        this.residentialFieldset = new Wtf.form.FieldSet({
            xtype:'fieldset',
            autoHeight:true,
            width: '200',
            hidden:this.isCustomer,
            hideLabel:this.isCustomer,
            autoWidth : true,
            style: 'border: none; margin-left: -10px',
            items :[{
                xtype: 'radiogroup',
                flex: 8,
                vertical: true,
                columns: 1,
                labelWidth: 50,
                hidden: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ||  !Wtf.isTDSApplicable),
                hideLabel: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA  || !Wtf.isTDSApplicable),
                id: 'residentialstatus_radiogroup'+this.id,
                fieldLabel: this.businessPerson=="Customer" ?WtfGlobal.getLocaleText('acc.field.CustomerResidentialStatus'):WtfGlobal.getLocaleText('acc.field.VendorResidentialStatus'),
                items: [
                    this.residentialstatus0,
                    this.residentialstatus1
                ]
            }]
        });
        this.YesNoStore = new Wtf.data.SimpleStore({
            fields: [{
                    name: 'id'
                }, {
                    name: 'name'
                }],
            data: [['1', 'Yes'], ['2', 'No']]
        });
        this.DTAAApplicable = new Wtf.form.FnComboBox({
            fieldLabel: 'DTAA Applicable', //'Deductee Type', 
            hidden: (Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA || !Wtf.isTDSApplicable),
            hideLabel: (Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA || !Wtf.isTDSApplicable),
            name: 'DTAAApplicable',
            width: '200',
            listWidth: '200',
            store: this.YesNoStore,
            emptyText: 'DTAA Applicable ?',
            valueField: 'id',
            displayField: 'name',
            hiddenField:'DTAAApplicable'
        });
        this.DTAAFromDate = new Wtf.form.FnDateField({
            fieldLabel: "DTAA From Date",
            name: 'DTAAFromDate ',
            width: '200',
            hidden: (Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA || !Wtf.isTDSApplicable),
            hideLabel: (Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA || !Wtf.isTDSApplicable),
            format: WtfGlobal.getOnlyDateFormat()
        });
        this.DTAAToDate = new Wtf.form.FnDateField({
            fieldLabel: 'DTAA To Date',
            name: 'DTAAToDate',
            width: '200',
            hidden: (Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA || !Wtf.isTDSApplicable),
            hideLabel: (Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA || !Wtf.isTDSApplicable),
            format: WtfGlobal.getOnlyDateFormat()
        });
        this.DTAASpecialRate = new Wtf.form.NumberField({
            fieldLabel: 'Special Rate',
            name: 'DTAASpecialRate',
            width: '200',
            hidden: (Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA || !Wtf.isTDSApplicable),
            hideLabel: (Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA || !Wtf.isTDSApplicable),
            maxLength: 10,
            invalidText: 'Alphabets and numbers only',
            allowDecimals:true,
            allowNegative:false,
            decimalPrecision: Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
        });
        this.DTAAPanelYes = new Wtf.Panel({
            id: 'DTAAPanelYes' + this.id,
            autoScroll: true,
            hidden: true,
            border: false,
            bodyStyle: ' background: none repeat scroll 0 0 #f1f1f1;',
            layout: 'form',
            items: [ this.DTAAFromDate, this.DTAAToDate,this.DTAASpecialRate]
        });
        this.DTAAPanel = new Wtf.Panel({
            id: 'DTAAPanel' + this.id,
            autoScroll: true,
            hidden: !this.residentialstatus0.getValue(),
            border: false,
            bodyStyle: ' background: none repeat scroll 0 0 #f1f1f1;',
            layout: 'form',
            items: [this.DTAAApplicable,this.DTAAPanelYes]
        });
        var natureofPaymentRec=new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
            {name: 'salespersoncode'},
        ]);
        this.natureofPaymentStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },natureofPaymentRec),
            url : "ACCMaster/getMasterItems.do",
            baseParams:{
                groupid:33,
                mode:112,
                moduleIds:""
            },
            sortInfo:{
                field:'salespersoncode',
                direction:'ASC'
            }
        });
        this.natureofPaymentStore.load();
        this.natureOfPayment = new Wtf.form.ExtFnComboBox({
            fieldLabel:(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.isTDSApplicable)?(WtfGlobal.getLocaleText("acc.field.natureOfPayment")+"*"):WtfGlobal.getLocaleText("acc.field.natureOfPayment"),//'Default Nature of payment',
            hiddenName:'natureofpayment',
            store:this.natureofPaymentStore,
            minChars:1,
            valueField:'id',
            displayField:'name',
            forceSelection:true,
            hirarchical:true,
            width: '200',
            allowBlank:(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.isTDSApplicable && !this.isCustomer)?false:true,
            hideLabel:!Wtf.isTDSApplicable||this.isCustomer,
            hidden:!Wtf.isTDSApplicable || this.isCustomer,
            disabled:this.isCustomer,
            extraFields:['salespersoncode'],
            mode: 'local',
            extraComparisionField:'salespersoncode',
            listWidth:400
        });
        
        this.accRec = Wtf.data.Record.create([
        {
            name:'accountname',
            mapping:'accname'
        },

        {
            name:'accountid',
            mapping:'accid'
        },

        {
            name:'productaccountid', 
            mapping:'accid'
        },

        {
            name:'acccode'
        },

        {
            name:'groupname'
        },

        {
            name:'natureOfPayment'
        }
        //            {name:'level',type:'int'}
        ]);
        this.tdsPayableAccountStore = new Wtf.data.Store({  
            url:"ACCAccountCMN/getAccountsIdNameForCombo.do",
            //            url: Wtf.req.account+'CompanyManager.jsp',
            baseParams:{
                mode:2,
                ignorecustomers:true,  
                ignorevendors:true,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        this.tdsPayableAccountStore.load();
        this.tdsInterestPayableAccount = new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.tdsinterestpayableaccount"),//'Interest On Late Payment of TDS Account',
            hiddenName:'tdsinterestpayableaccountid',
            store:this.tdsPayableAccountStore,
            minChars:1,
            valueField:'accountid',
            displayField:'accountname',
            forceSelection:true,
            hirarchical:true,
            width: '200',
            disabled:this.readOnly,
            hideLabel:!Wtf.isTDSApplicable || this.isCustomer,
            hidden:!Wtf.isTDSApplicable || this.isCustomer,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
            mode: 'local',
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400
        });
        this.SERVICENo =new Wtf.form.TextField({
//            fieldLabel: this.isCustomer ? WtfGlobal.getLocaleText("acc.field.Customerservice")+"*" : WtfGlobal.getLocaleText("acc.field.Vendorservice")+"*"  ,// "Customer Service TAX" : "Vendor Service Tax"
            fieldLabel: this.isCustomer ? WtfGlobal.getLocaleText("acc.field.Customerservice") : WtfGlobal.getLocaleText("acc.field.Vendorservice") ,// "Customer Service TAX" : "Vendor Service Tax"
            name: 'servicetaxno',
            width:200,
            hidden: WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW || !Wtf.isSTApplicable,
            hideLabel: WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW || !Wtf.isSTApplicable ,
//            allowBlank:(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.isSTApplicable)?false:true,
//            allowBlank:true,
            maxLength:15,
            invalidText :'Alphabets and numbers only',
            vtype : "alphanum"
        });      
        this.TANNo =new Wtf.form.TextField({
            fieldLabel: this.isCustomer ? WtfGlobal.getLocaleText("acc.field.Customertan") : WtfGlobal.getLocaleText("acc.field.Vendortan")  ,// "Customer TAN" : "Vendor TAN"
            name: 'tanno',
            width:200,
            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            maxLength:10,
            invalidText :'Alphabets and numbers only',
            vtype : "alphanum"
        });
        this.scenariosStore = new Wtf.data.SimpleStore({
            fields: [{
                    name: 'id'
                }, {
                    name: 'name'
                }],
            data: [['1', WtfGlobal.getLocaleText("acc.vendor.tds.NonOrLowerdeduction")], 
                ['2', WtfGlobal.getLocaleText("acc.vendor.tds.NonAccDeclairation")],
                ['3', WtfGlobal.getLocaleText("acc.vendor.tds.TransporterOwning")],
                ['4', WtfGlobal.getLocaleText("acc.vendor.tds.BasicExemptionReached")]]
        });
        this.typeOfDeducation= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.vendor.tds.NonOrLowerdeductionApplicable"),
            hidden: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.isTDSApplicable),
            hideLabel: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.isTDSApplicable),
            disabled : this.businessPerson=='Customer' ? true:false,
            name:'typeOfDeducation',
            width: '200',
            listWidth: '200',
            store:this.YesNoStore,
            valueField:'id',
            displayField:'name'
        });
        this.non_lowerDeducation= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.29"),
            disabled : this.businessPerson=='Customer' ? true:false,
            name:'non_lowerDeducation',
            width: '200',
            listWidth: '200',
            id:"non_lowerDeducation"+this.id,
            store:this.scenariosStore,
            valueField:'id',
            displayField:'name'
        });

        this.CertificateNo = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.vendor.tds.certificateno"),
            name: 'CertificateNo',
            anchor: '80%',
            hidden: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ||  !Wtf.isTDSApplicable),
            hideLabel:  (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ||  !Wtf.isTDSApplicable),
            maxLength: 50,
            invalidText: 'Alphabets and numbers only',
            vtype: "alphanum"
        });
        this.certiFromDate = new Wtf.form.FnDateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.vendor.tds.fromDate"),
            name: 'certiFromDate',
            anchor: '80%',
            hidden:  (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ||  !Wtf.isTDSApplicable),
            hideLabel:  (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ||  !Wtf.isTDSApplicable),
            format: WtfGlobal.getOnlyDateFormat()
        });
        this.certiToDate = new Wtf.form.FnDateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.vendor.tds.toDate"),
            name: 'certiToDate',
            anchor: '80%',
            hidden:  (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ||  !Wtf.isTDSApplicable),
            hideLabel:  (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ||  !Wtf.isTDSApplicable),
            format: WtfGlobal.getOnlyDateFormat()
        });
        this.certiFromDate.on('change',function(){
            this.sdate=this.certiFromDate.getValue();
            if(this.certiToDate.getValue()){
                this.edate=this.certiToDate.getValue();
                if(this.sdate > this.edate){
                    WtfComMsgBox(1,2);
                    this.certiFromDate.reset();
                }
            }
        },this);
        this.certiToDate.on('change',function(){
            this.sdate=this.certiFromDate.getValue();
            this.edate=this.certiToDate.getValue();
            if(this.sdate > this.edate){
                WtfComMsgBox(1,2);
                this.certiToDate.reset();
            }
        },this);
        this.lowerRate = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.vendor.tds.lowerRate"),
            name: 'lowerRate',
            anchor: '80%',
            hidden:  (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ||  !Wtf.isTDSApplicable),
            hideLabel:  (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ||  !Wtf.isTDSApplicable),
            maxLength: 10,
            allowDecimals:true,
            allowNegative:false,
            decimalPrecision: Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
        });
        this.certiFieldset = new Wtf.Panel({
            id: 'certiFieldset' + this.id,
            autoScroll: true,
            hidden:true,
            border:false,
            bodyStyle: ' background: none repeat scroll 0 0 #f1f1f1;',
            layout: 'form',
            items: [this.CertificateNo,this.certiFromDate,
                this.certiToDate,this.lowerRate
            ]
        });
        this.declareFromDate = new Wtf.form.FnDateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.vendor.tds.fromDate"),
            name: 'declareFromDate',
            anchor: '80%',
            hidden:  (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ||  !Wtf.isTDSApplicable),
            hideLabel: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ||  !Wtf.isTDSApplicable),
            format: WtfGlobal.getOnlyDateFormat()
        });
        this.declareToDate = new Wtf.form.FnDateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.vendor.tds.toDate"),
            name: 'declareToDate',
            anchor: '80%',
            hidden:  (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ||  !Wtf.isTDSApplicable),
            hideLabel:  (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ||  !Wtf.isTDSApplicable),
            format: WtfGlobal.getOnlyDateFormat()
        });
        this.declareFromDate.on('change',function(){
            this.sdate=this.declareFromDate.getValue();
            if(this.declareToDate.getValue()){
                this.edate=this.declareToDate.getValue();
                if(this.sdate > this.edate){
                    WtfComMsgBox(1,2);
                    this.declareFromDate.reset();
                }
            }
        },this);
        this.declareToDate.on('change',function(){
            this.sdate=this.declareFromDate.getValue();
            this.edate=this.declareToDate.getValue();
            if(this.sdate > this.edate){
                WtfComMsgBox(1,2);
                this.declareToDate.reset();
            }
        },this);
        this.declareRefNo = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.vendor.tds.declarationRefNumb"),
            name: 'declareRefNo',
            anchor: '80%',
            hidden: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA,
            hideLabel: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA,
            maxLength: 50,
            invalidText: 'Alphabets and numbers only',
            vtype: "alphanum"
        });
        this.transportFromDate = new Wtf.form.FnDateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.vendor.tds.fromDate"),
            name: 'transportFromDate',
            anchor: '80%',
            hidden:  (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ||  !Wtf.isTDSApplicable),
            hideLabel: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ||  !Wtf.isTDSApplicable),
            format: WtfGlobal.getOnlyDateFormat()
        });
        this.transportToDate = new Wtf.form.FnDateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.vendor.tds.toDate"),
            name: 'transportToDate',
            anchor: '80%',
            hidden:  (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ||  !Wtf.isTDSApplicable),
            hideLabel:  (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ||  !Wtf.isTDSApplicable),
            format: WtfGlobal.getOnlyDateFormat()
        });
        this.transportFromDate.on('change',function(){
            this.sdate=this.transportFromDate.getValue();
            if(this.transportToDate.getValue()){
                this.edate=this.transportToDate.getValue();
                if(this.sdate > this.edate){
                    WtfComMsgBox(1,2);
                    this.transportFromDate.reset();
                }
            }
        },this);
        this.transportToDate.on('change',function(){
            this.sdate=this.transportFromDate.getValue();
            this.edate=this.transportToDate.getValue();
            if(this.sdate > this.edate){
                WtfComMsgBox(1,2);
                this.transportToDate.reset();
            }
        },this);
        this.transportRefNo = new Wtf.form.TextField({
            fieldLabel:  WtfGlobal.getLocaleText("acc.vendor.tds.declarationRefNumb"),
            name: 'transportRefNo',
            anchor: '80%',
            hidden: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA,
            hideLabel: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA,
            maxLength: 50,
            invalidText: 'Alphabets and numbers only',
            vtype: "alphanum"
        });
        this.declareFieldset = new Wtf.Panel({
            id: 'declareFieldset' + this.id,
            autoScroll: true,
            hidden:true,
            border:false,
            bodyStyle:  ' background: none repeat scroll 0 0 #f1f1f1;',
            layout: 'form',
            items: [this.declareRefNo, this.declareFromDate, this.declareToDate
            ]
        });
        this.transportPanel = new Wtf.Panel({
            id: 'transportPanel' + this.id,
            autoScroll: true,
            hidden:true,
            border:false,
            bodyStyle:  ' background: none repeat scroll 0 0 #f1f1f1;',
            layout: 'form',
            items: [this.transportRefNo, this.transportFromDate, this.transportToDate
            ]
        });
        this.lowerRatePanelYorN = new Wtf.Panel({
            id: 'lowerRatePanelYorN' + this.id,
            autoScroll: true,
            border:false,
            hidden:true,
            bodyStyle: ' background: none repeat scroll 0 0 #f1f1f1;',
            layout: 'form',
            items: [
                this.non_lowerDeducation,
                this.certiFieldset,this.declareFieldset,this.transportPanel
            ]
        });
        this.lowerRatePanel = new Wtf.Panel({
            id: 'lowerRatePanel' + this.id,
            autoScroll: true,
            hidden:true,
            border:false,
            bodyStyle: ' background: none repeat scroll 0 0 #f1f1f1;',
            layout: 'form',
            items: [
                this.typeOfDeducation,this.lowerRatePanelYorN
            ]
        });
        this.lowerRatePanel.doLayout();
        this.higherRatePanel = new Wtf.Panel({
            id: 'higherRatePanel' + this.id,
            autoScroll: true,
            hidden:this.isEdit || this.isViewTemplate || this.readOnly || this.isCustomer,
            border:false,
            bodyStyle: ' background: none repeat scroll 0 0 #f1f1f1;',
            layout: 'form',
            items: [
                this.higherRate = new Wtf.form.NumberField({
                    fieldLabel: WtfGlobal.getLocaleText("acc.vendor.tds.higherRate")+WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.customerInformation.higherRateInfo")),
                    name: 'higherRate',
                    id: 'higherRate' + this.id,
                    width: '200',
                    hidden: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA,
                    hideLabel: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA,
                    maxLength: 10,
//                    invalidText: 'numbers only',
                    allowDecimals:true,
                    allowNegative:false,
                    decimalPrecision: Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
                })
            ]
        });
        this.lowerRatePanel.doLayout();
        this.considerExemptLimit = new Wtf.form.Checkbox({
            name: 'considerExemptLimit',
            id: "considerExemptLimit"+ this.id,
            fieldLabel: "Consider Exempt Limit",
            checked: false,
            cls: 'custcheckbox',
            width: 10
        });
        this.isTDSapplicableonvendor=new Wtf.form.FieldSet({
            border:false,
            xtype:'fieldset',
            autoWidth:true,
            width:250, 
            hidden:!Wtf.isTDSApplicable,
            autoHeight:true,
            checkboxToggle: true,
            checkboxName: this.isCustomer ? "isTDSapplicableoncust" : "isTDSapplicableonvendor",
            id: "isTDS" + this.heplmodeid + this.id,
            disabledClass:"newtripcmbss", 
            title:WtfGlobal.getLocaleText("acc.vendor.tdsdetails.text"),
            defaults:{border:false},
            items:[this.PANNo,this.panStatusCombo,this.TANNo,this.deducteeType,this.deducteeCode,this.natureOfPayment,this.tdsInterestPayableAccount,
                   this.considerExemptLimit,this.residentialFieldset,this.DTAAPanel,this.lowerRatePanel,this.higherRatePanel]

        });
        this.deducteeType.on('select',this.deducteeTypeOnSelect,this)
        this.isTDSapplicableonvendor.on('collapse',function(){
            this.funEnableDisablePANandPANStatus(this.isEdit,true);
            },this);
        this.isTDSapplicableonvendor.on('expand',function(){
            this.funEnableDisablePANandPANStatus(this.isEdit,false)
            },this);
        this.panStatusCombo.on('select',this.funAllowBlankPANfun,this);
        
        this.typeOfDeducation.on('select', function (obj) {// It ask Yes or No - if Yes, Ask for deduction
            if (obj.getValue() == '1' && !this.isCustomer) {
                this.lowerRatePanelYorN.show();
                this.lowerRatePanelYorN.doLayout();
            } else {
                this.lowerRatePanelYorN.hide();
            }
        }, this);
        this.non_lowerDeducation.on('select', function (obj) {// if Lower rate is applicable- show more details of lower deduction
            if (!this.isCustomer) {
                if (obj.getValue() == this.deductionComboId.Non_Deduction_or_Lower_Deduction) {
                    this.certiFieldset.show();
                    this.certiFieldset.doLayout();
                    this.declareFieldset.hide();
                    this.transportPanel.hide();
                } else if (obj.getValue() == this.deductionComboId.Non_Deduction_Declaration) {
                    this.certiFieldset.hide();
                    this.declareFieldset.show();
                    this.declareFieldset.doLayout();
                    this.transportPanel.hide();
                } else if (obj.getValue() == this.deductionComboId.Deduction_Transporter) {
                    this.certiFieldset.hide();
                    this.declareFieldset.hide();
                    this.transportPanel.show();
                    this.transportPanel.doLayout();
                }else if (obj.getValue() == this.deductionComboId.Basic_Exemption_Reached) {
                    this.certiFieldset.hide();
                    this.declareFieldset.hide();
                    this.transportPanel.hide();
                    this.transportPanel.doLayout();
                }
            }
        }, this);

        this.residentialstatus0.on('change', function (obj) {// On Residential status change to Resident
            if (!this.isCustomer) {
                if (obj.getValue()) {
                    this.DTAAPanel.hide();
                    if (!Wtf.isEmpty(this.PANNo.getValue())) {
                        this.higherRatePanel.hide();
                        //If PAN is given, Higer Rate will be non-mandatory.
                        this.higherRate.allowBlank = true;
                        this.higherRate.minValue = 0;
                        this.lowerRatePanel.show();
                        this.lowerRatePanel.doLayout();
                    } else {
                        this.higherRatePanel.show();
                        //If PAN status is "PANNOTAVBL" or "APPLIEDFOR", Higer Rate will be mandatory.
                        this.higherRate.allowBlank = false;
                        this.higherRate.minValue = 1;
                        this.lowerRatePanel.hide();
                        this.higherRatePanel.doLayout();
                    }
                } else {
                    this.DTAAPanel.show();
                    this.DTAAPanel.doLayout();
                    this.higherRatePanel.hide();
                    //If PAN is given, Higer Rate will be non-mandatory.
                    this.higherRate.allowBlank = true;
                    this.higherRate.minValue = 0;
                    this.lowerRatePanel.hide();
                }
            }
        }, this);
        this.residentialstatus1.on('change', function (obj) {// On Residential status change to Resident 
            if (!this.isCustomer) {
                if (obj.getValue()) {
                    this.DTAAPanel.show();
                    this.DTAAPanel.doLayout();
                    this.higherRatePanel.hide();
                    //If PAN is given, Higer Rate will be non-mandatory.
                    this.higherRate.allowBlank = true;
                    this.higherRate.minValue = 0;
                    this.lowerRatePanel.hide();
                } else {
                    this.DTAAPanel.hide();
                    if (!Wtf.isEmpty(this.PANNo.getValue())) {
                        this.higherRatePanel.hide();
                        //If PAN is given, Higer Rate will be non-mandatory.
                        this.higherRate.allowBlank = true;
                        this.higherRate.minValue = 0;
                        this.lowerRatePanel.show();
                        this.lowerRatePanel.doLayout();
                    } else {
                        this.higherRatePanel.show();
                        //If PAN status is "PANNOTAVBL" or "APPLIEDFOR", Higer Rate will be mandatory.
                        this.higherRate.allowBlank = false;
                        this.higherRate.minValue = 1;
                        this.lowerRatePanel.hide();
                        this.higherRatePanel.doLayout();
                    }
                }
            }
        }, this);
        this.DTAAApplicable.on('select', function (obj) {// if DTAA is Applicable - show more details of DTAA
            if (!this.isCustomer) {
                if (obj.getValue() == '1') {
                    this.DTAAPanelYes.show();
                    this.DTAAPanelYes.doLayout();
                } else {
                    this.DTAAPanelYes.hide();
            }
            }
        }, this);  
        
        // Multiselect Pricing Band
//        this.pricingBand = new Wtf.common.Select(Wtf.apply({
//            multiSelect: true,
//            fieldLabel: WtfGlobal.getLocaleText("acc.field.pricingBandMaster"), // "Pricing Band",
//            forceSelection: true
//        },{
//            name: 'pricingBand',
//            width: 200,
//            store: this.pricingBandMasterStore,
//            valueField:'id',
//            displayField:'name',
//            addNoneRecord: true,
//            hidden: !Wtf.account.companyAccountPref.productPricingOnBands,
//            hideLabel: !Wtf.account.companyAccountPref.productPricingOnBands
//        }));
        
        // Multiselect Category
        this.Category = new Wtf.common.Select(Wtf.apply({
            multiSelect:true,
            fieldLabel:(this.isCustomer?WtfGlobal.getLocaleText("acc.cust.Customercategory"):WtfGlobal.getLocaleText("acc.cust.vendorcategory"))+WtfGlobal.addLabelHelp((this.isCustomer?WtfGlobal.getLocaleText("acc.cust.msg1"):WtfGlobal.getLocaleText("acc.ven.msg1"))),
            forceSelection:true
        },{
            name:'category',
            id: "category" + this.heplmodeid+ this.id,
            width:200,
            store: (this.businessPerson=="Customer")?Wtf.CustomerCategoryStore:Wtf.VendorCategoryStore,
            valueField:'id',
            displayField:'name',
            addNoneRecord: true
        }));
        
        this.InterCompany= new Wtf.form.Checkbox({
            name:'intercompanyflag',
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.intercompanyflag"),
            checked:false,
            cls : 'custcheckbox',
            width: 10,
            hidden:true,
            hideLabel:true
        });
        this.InterCompany.on('check', this.interCustomerCheckHandler, this);
        this.InterCompanyType= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.intercompanytype")+WtfGlobal.addLabelHelp((this.isCustomer?WtfGlobal.getLocaleText("acc.cust.msg4"):WtfGlobal.getLocaleText("acc.ven.msg4"))),
            name:'intercompanytype',
            width:200,
            forceSelection: true,
            store: Wtf.InterCompanyTypeStore,
            valueField:'id',
            displayField:'name',
            addNoneRecord: true,
            disabled:true,
            hidden:true,
            hideLabel:true
        });
        
         this.accRec = Wtf.data.Record.create([
        {
            name:'accountname',
            mapping:'accname'
        },

        {
            name:'accountid',
            mapping:'accid'
        },

        {
            name:'acccode'
        },

        {
            name:'groupname'
        },
        {
            name:'hasAccess'
        }
        ]);
        
this.defaultCustomerAccountStore = new Wtf.data.Store({                  
        url : "ACCAccountCMN/getAccountsIdNameForCombo.do",
          baseParams:{
                mode:2,
                nature : Wtf.account.nature.Asset ,
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
    this.defaultCustomerAccountStore.load();
    this.defaultCustomerAccountStore.on("load",function(){
        var mappingcusacc;
        if(this.record!=undefined)
            mappingcusacc=this.record.data.mappingcusaccid;
        if(mappingcusacc!="" && mappingcusacc!=undefined){
            this.defaultCustomerAccount.setValue(mappingcusacc);
        }else{
            this.defaultCustomerAccount.setValue(Wtf.account.companyAccountPref.customerdefaultaccount);
        }
      
    },this);
     
    this.defaultVendorAccountStore = new Wtf.data.Store({ 
        url:"ACCAccountCMN/getAccountsIdNameForCombo.do",
         baseParams:{ 
                    ignoreCashAccounts:true,
                    ignoreBankAccounts:true,
                    ignoreGSTAccounts:true,  
                    ignorecustomers:true, 
//                    nature :Wtf.account.nature.Liability,
                    ignorevendors:true,
//              nature:[0],
              nondeleted:true
          },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.accRec)
    });
    this.defaultVendorAccountStore.load();
    this.defaultVendorAccountStore.on("load",function(){
        var mappingvenacc;
        if(this.record!=undefined)
            mappingvenacc=this.record.data.mappingvenaccid;
        if(mappingvenacc!="" && mappingvenacc!=undefined){
            this.defaultVendorAccount.setValue(mappingvenacc);
        }else{
            this.defaultVendorAccount.setValue(Wtf.account.companyAccountPref.vendordefaultaccount);
        }
        
    },this);
    /**
     * change store if show limilted accounts flag is activated and contains 1 or more than 1 mappin
     */
    var customerAccountStore = this.defaultCustomerAccountStore;
    var vendorAccountStore = this.defaultVendorAccountStore;
    if(Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref != null){
        if(Wtf.account.companyAccountPref.columnPref.isLimitedCustomerAccounts != undefined && Wtf.account.companyAccountPref.columnPref.isLimitedCustomerAccounts != null && Wtf.account.companyAccountPref.columnPref.isLimitedCustomerAccounts){
            customerAccountStore = Wtf.customerLimitedAccStore != undefined && Wtf.customerLimitedAccStore.data.length > 0 ? Wtf.customerLimitedAccStore : this.defaultCustomerAccountStore;
        }
        if(Wtf.account.companyAccountPref.columnPref.isLimitedVendorAccounts != undefined && Wtf.account.companyAccountPref.columnPref.isLimitedVendorAccounts != null && Wtf.account.companyAccountPref.columnPref.isLimitedVendorAccounts){
            vendorAccountStore = Wtf.vendorLimitedAccStore != undefined && Wtf.vendorLimitedAccStore.data.length > 0 ? Wtf.vendorLimitedAccStore : this.defaultVendorAccountStore;
        }
    }
    
    this.defaultCustomerAccount=new Wtf.form.ExtFnComboBox({
        hiddenName:'mappingcusaccid',
        name:'mappingcusaccid',
        id: "mappingcusaccid" + this.heplmodeid+ this.id,
        store: customerAccountStore,
        fieldLabel:"<span wtf:qtip='"+(this.isCustomer?WtfGlobal.getLocaleText("acc.customer.account.tt"):WtfGlobal.getLocaleText("acc.vendor.account.tt") )+"'>"+ WtfGlobal.getLocaleText("acc.je.acc")+'*'  +"</span>",
        minChars:1,
        listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
        allowBlank:false,
        valueField:'accountid',
        displayField:'accountname',
        isAccountCombo:true,
        forceSelection:true,
        hirarchical:true,
        extraFields:Wtf.account.companyAccountPref.accountsWithCode ?['acccode','groupname']:[],
        width:200,
        mode: 'local',
        typeAheadDelay:30000,
        extraComparisionField:'acccode',
        disabled:(this.businessPerson=="Customer")?false:true
    });
    
    this.defaultCustomerAccount.on('beforeselect',function(combo,record,index){
        return validateSelection(combo,record,index);
    },this);
    
    this.defaultVendorAccount=new Wtf.form.ExtFnComboBox({
        hiddenName:'mappingvenaccid',
        name:'mappingvenaccid',
        id: "mappingvenaccid" + this.heplmodeid+ this.id,
        store:vendorAccountStore,
        fieldLabel:"<span wtf:qtip='"+(this.isCustomer?WtfGlobal.getLocaleText("acc.customer.account.tt"):WtfGlobal.getLocaleText("acc.vendor.account.tt") )+"'>"+ WtfGlobal.getLocaleText("acc.je.acc")+'*'  +"</span>",
        minChars:1,
        listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
        allowBlank:false,
        valueField:'accountid',
        displayField:'accountname',
        forceSelection:true,
        hirarchical:true,
        isAccountCombo:true,
        extraFields: Wtf.account.companyAccountPref.accountsWithCode ?['acccode','groupname']:[],
        mode: 'local',
        width:200,
        typeAheadDelay:30000,
        extraComparisionField:'acccode',
        disabled:(this.businessPerson=="Customer")?true:false
    });
    
    this.defaultVendorAccount.on('beforeselect',function(combo,record,index){
        return validateSelection(combo,record,index);
    },this);
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.create))
            this.InterCompanyType.addNewFn= this.addMaster.createDelegate(this,[14,Wtf.InterCompanyTypeStore])
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.create))
            this.Category.addNewFn= (this.businessPerson=="Customer")?this.addMaster.createDelegate(this,[7,Wtf.CustomerCategoryStore]):this.addMaster.createDelegate(this,[8,Wtf.VendorCategoryStore])        
        this.name= new Wtf.form.ExtendedTextField({
        //    fieldLabel:"<span wtf:qtip='"+(this.isCustomer?WtfGlobal.getLocaleText("acc.customer.custname.tt"):WtfGlobal.getLocaleText("acc.vendor.venname.tt") )+"'>"+ this.isCustomer?WtfGlobal.getLocaleText("acc.cust.name"):WtfGlobal.getLocaleText("acc.ven.name") +"</span>",//this.isCustomer?WtfGlobal.getLocaleText("acc.cust.name"):WtfGlobal.getLocaleText("acc.ven.name"),  //this.businessPerson+" Name *",
            fieldLabel:this.isCustomer?"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.customer.custname.tt") +"'>"+  WtfGlobal.getLocaleText("acc.customer.Cname")  +"</span>":"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.vendor.venname.tt") +"'>"+ WtfGlobal.getLocaleText("acc.customer.Vname") +"</span>",
            name: 'accname',
            id:'accname'+this.heplmodeid+this.id,
            allowBlank:false,
            width:200,
            maskRe: /^[^"]*$/,
            maxLength:100,
            listeners:{
                scope:this,
                focus:function(){
                     // using local store for checking vendor/customer exists (ERP-10385)
//                    this.searchText(this.localParentStore,this.pan,'accname'+this.heplmodeid+this.id,'parentname',this.isCustomer);
                    this.searchText(this.pan,'accname'+this.heplmodeid+this.id,'parentname',this.isCustomer,this.businessPerson);
                }
            }
        });
        this.sequenceFormatCombobox = new Wtf.form.ComboBox({            
             triggerAction:'all',
             mode: 'local',
             fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.Sequenceformat.tip")+"'>"+ WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat")+"</span>",
             valueField:'id',
             displayField:'value',
             store:this.sequenceFormatStore,  
             width:200,
             id: "sequenceformat" + this.heplmodeid+ this.id,
             typeAhead: true,
             forceSelection: true,
             name:'sequenceformat',
             hiddenName:'sequenceformat',
             allowBlank:false
        });
        
        this.sequenceFormatStore.load();
        this.sequenceFormatStore.on('load',this.setNextNumber,this);
        this.sequenceFormatCombobox.on('select',this.getNextSequenceNumber,this);
        this.code = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip='"+ ((this.businessPerson=="Customer")?WtfGlobal.getLocaleText("acc.customer.custcode.tt"):WtfGlobal.getLocaleText("acc.vendor.vendorcode.tt")) +"'>"+((this.businessPerson=="Customer")?WtfGlobal.getLocaleText("acc.agedPay.cus"):WtfGlobal.getLocaleText("acc.invoice.vendor"))+" "+WtfGlobal.getLocaleText("acc.field.Code")+"*"+"</span>",//((this.businessPerson=="Customer")?WtfGlobal.getLocaleText("acc.agedPay.cus"):WtfGlobal.getLocaleText("acc.invoice.vendor"))+" "+WtfGlobal.getLocaleText("acc.field.Code")+"*", //"(this.businessPerson=="Customer")?"Customer":"Vendor") Code",
            name: 'acccode',
            id: 'acccode'+this.heplmodeid+this.id,
            allowBlank:false,
            //hideLabel : this.isFixedAsset,
            //hidden : this.isFixedAsset,
            width:200,
            maxLength:50,
            listeners:{
                scope:this,
                focus:function(){
                    
                    this.searchText(this.pan,'acccode'+this.heplmodeid+this.id,'code',this.isCustomer,this.businessPerson);
                }
            }
        });
        this.aliasname= new Wtf.form.TextField({
            fieldLabel:this.isCustomer?"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.customer.custaliasname.tt") +"'>"+  WtfGlobal.getLocaleText("acc.cust.aliasname")  +"</span>":"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.vendor.venaliasname.tt") +"'>"+ WtfGlobal.getLocaleText("acc.ven.aliasname") +"</span>",
            name: 'aliasname',
            id:'aliasname'+this.heplmodeid+this.id,
            width:200,
            maxLength:100
        });
        this.creationdate= new Wtf.form.FnDateField({
            fieldLabel:"<span wtf:qtip='"+(this.isCustomer?WtfGlobal.getLocaleText("acc.customer.creationdate.tt"):WtfGlobal.getLocaleText("acc.vendor.creationdate.tt") )+"'>"+ WtfGlobal.getLocaleText("acc.cust.creationDate")  +"</span>",   //'Creation Date',
            name: 'creationDate',
            id: "creationDate" + this.heplmodeid+ this.id,
            width:200,
            format:WtfGlobal.getOnlyDateFormat(),
            disabled:this.isEdit,
//            value: Wtf.serverDate,
            value: new Date(),
            allowBlank:false
        });
        
        this.creationdate.on('dateselect',function(){
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
                            callCurrencyExchangeWindow(this.currencyExchangeWinId);
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.common.currency")],2);     //'Alert',"Please set Currency Exchange Rates"
                            Wtf.getCmp(this.currencyExchangeWinId).on("update", function(){
                                this.currencyStore.reload();
                                this.defaultCurrencyStore.reload();
                                this.currencyStore.on("load",function(){
                                    this.Currency.reset();
                                },this);
                            },this);
                        }
                    });
                }else{
                    this.exchangeRate.setValue(1);
                    this.exchangeRate.disable();
                }
        },this);
        // On date change Currency Loads
        this.creationdate.on('change',this.onDateChange,this);
        this.balTypeStore = new Wtf.data.SimpleStore({
            fields: [{name:'typeid',type:'boolean'}, 'name'],
            data :[[true,'Debit'],[false,'Credit']]
        });
        this.balTypeEditor = new Wtf.form.ComboBox({
            store: this.balTypeStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.openType"),  //'Opening Balance Type',
            name:'debitType',
            displayField:'name',
            disabled:true,
            forceSelection: true,
            allowBlank:false,
            width:200,
            valueField:'typeid',
            mode: 'local',
            value:this.isCustomer,
            triggerAction: 'all',
            typeAhead:true,
            selectOnFocus:true
        });
//        this.openingBal= new Wtf.form.NumberField({
//            fieldLabel: this.isCustomer?WtfGlobal.getLocaleText("acc.cust.open"):WtfGlobal.getLocaleText("acc.ven.open"),  //   //"Opening Balance in "+WtfGlobal.getCurrencySymbolForForm(),
//            name: 'openbalance',
//            width:200,
//            maxLength:15,
//            decimalPrecision:2,
//            value:0,
//            allowBlank:false,
//            allowNegative:false,
//            xtype:'numberfield'
//        });
        this.openingBal= new Wtf.form.NumberFieldAddNewBtn({
            fieldLabel:"<span wtf:qtip='"+(this.isCustomer?WtfGlobal.getLocaleText("acc.customer.openingbalence.tt"):WtfGlobal.getLocaleText("acc.vendor.openingbalence.tt") )+"'>"+ WtfGlobal.getLocaleText("acc.open.balance")  +"</span>",// WtfGlobal.getLocaleText("acc.open.balance"),//:WtfGlobal.getLocaleText("acc.ven.open"),  //   //"Opening Balance in "+WtfGlobal.getCurrencySymbolForForm(),
            name: 'openbalance',
            id: "openbalance" + this.heplmodeid+ this.id,
            disabled:!this.isEdit, 
            readOnly:true,
            width:200,
            maxLength:15,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            value:0,
            allowBlank:false,
            allowNegative:false,
            xtype:'numberfield'
        });  
        
//        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.create))
//            this.openingBal.addNewFn=this.addOpeningBalance.createDelegate(this);

        this.limit= new Wtf.form.NumberField({
          //  fieldLabel:this.isCustomer?WtfGlobal.getLocaleText("acc.cust.creditLimit"):WtfGlobal.getLocaleText("acc.cust.debitLimit"),  // limit for customer/vendor
            fieldLabel:this.isCustomer?"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.customer.creditsaleslimit.tt")  +"'>"+  WtfGlobal.getLocaleText("acc.cust.creditLimit")+ " *"+"</span>":"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.vendor.creditpurchaselimit.tt")  +"'>"+  WtfGlobal.getLocaleText("acc.cust.debitLimit") +" *" +"</span>",//this.isCustomer?WtfGlobal.getLocaleText("acc.cust.term") +" *":WtfGlobal.getLocaleText("acc.ven.term") +" *",  //'Credit Term*':'Debit Term*',
            name: 'limit',
            id: "limit" + this.heplmodeid + this.id,
            width:200,
            maxLength:10,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            value:0,
            allowBlank:false,
            allowNegative:false,
            xtype:'numberfield'
        });

        this.limit.on('blur',function(field){if(field.getValue() == ""){field.setValue(0);}},this);
        this.limit.on('focus',function(field){if(field.getValue() == 0){field.setValue("");}},this);

        this.openingBal.on('blur',function(field){if(field.getValue() == ""){field.setValue(0);}},this);
        this.openingBal.on('focus',function(field){if(field.getValue() == 0){field.setValue("");}},this);
        
        this.taxIDNo= new Wtf.form.TextField({
           fieldLabel: WtfGlobal.getLocaleText("acc.cust.taxID"),  //'Tax ID Number',
           name: 'taxidnumber',
           width:200,
           maxLength:20,
           hideLabel:this.isCustomer||Wtf.account.companyAccountPref.withouttax1099,
           hidden:this.isCustomer||Wtf.account.companyAccountPref.withouttax1099
        });
        
        this.companyRegNo= new Wtf.form.TextField({
           fieldLabel: WtfGlobal.getLocaleText("acc.person.company.regnumber"),//Company Registration Number
           name: 'companyRegistrationNumber',
           width:200,
//           maxLength:30,
           hideLabel:(Wtf.account.companyAccountPref.countryid!='137'),// only for malasian company
           hidden:(Wtf.account.companyAccountPref.countryid!='137')// only for malasian company
        });
        
        this.gstRegNo= new Wtf.form.TextField({
           fieldLabel: WtfGlobal.getLocaleText("acc.person.gst.regnumber"),//GST Registration Number
           name: 'gstRegistrationNumber',
           width:200,
//           maxLength:30,
           hideLabel:(Wtf.account.companyAccountPref.countryid!='137'),// only for malasian company
           hidden:(Wtf.account.companyAccountPref.countryid!='137')// only for malasian company
        });
        
        this.rmcdApprovalNo= new Wtf.form.TextField({
           fieldLabel: WtfGlobal.getLocaleText("acc.vendor.rmcd.approval.no"),//RMCD Approval No.
           name: 'rmcdApprovalNumber',
           width:200,
//           maxLength:30,
           hideLabel:this.isCustomer || (Wtf.account.companyAccountPref.countryid!='137'),// only for malasian company
           hidden: this.isCustomer || (Wtf.account.companyAccountPref.countryid!='137')// only for malasian company
        });
        /*
         *Only for malaysian company
         */
        this.gstVerifiedDate= new Wtf.form.DateField({                                           
           fieldLabel:WtfGlobal.getLocaleText("acc.vendor.gstverifiedDate"),//selfbilled approval from date
           format:WtfGlobal.getOnlyDateFormat(),
           name: 'gstVerifiedDate',
           id:'gstVerifiedDate'+this.heplmodeid+this.id,
           width:200,
           hideLabel:this.isCustomer || (Wtf.account.companyAccountPref.countryid!='137'),// only for malasian company
           hidden: this.isCustomer || (Wtf.account.companyAccountPref.countryid!='137')// only for malasian company
        });
        this.selfBilledFromDate= new Wtf.form.DateField({                                           
           fieldLabel:WtfGlobal.getLocaleText("acc.vendor.selfbilled.approval.from.date"),//selfbilled approval from date
           format:WtfGlobal.getOnlyDateFormat(),
           name: 'selfBilledFromDate',
           width:200,
           hideLabel:this.isCustomer || (Wtf.account.companyAccountPref.countryid!='137'),// only for malasian company
           hidden: this.isCustomer || (Wtf.account.companyAccountPref.countryid!='137')// only for malasian company
        });
        
        this.selfBilledToDate= new Wtf.form.DateField({
           fieldLabel:WtfGlobal.getLocaleText("acc.vendor.selfbilled.approval.to.date"),//selfbilled approval to date
           format:WtfGlobal.getOnlyDateFormat(),
           name: 'selfBilledToDate',
           width:200,
           hideLabel:this.isCustomer || (Wtf.account.companyAccountPref.countryid!='137'),// only for malasian company
           hidden: this.isCustomer || (Wtf.account.companyAccountPref.countryid!='137')// only for malasian company
        });
        
        this.Eligible1099= new Wtf.form.Checkbox({
            name:'taxeligible',
            fieldLabel:WtfGlobal.getLocaleText("acc.ven.1099"),  //'Eligible for 1099',
            checked:false,
            hideLabel:true,//this.isCustomer||Wtf.account.companyAccountPref.withouttax1099,       ERP-5754
            hidden:true,//this.isCustomer||Wtf.account.companyAccountPref.withouttax1099,
            style: 'padding:0px 0px 10px 0px;',
            width: 10
        })
        
         this.employmentStatusStore = new Wtf.data.SimpleStore({
            fields:[{
                name:'id'
            },{
                name:'name'
            }],
            data:[['0','Employed'],['1','Un-Employed']]
        });
        
         this.EmploymentStatus = new Wtf.form.ComboBox({
            store: this.employmentStatusStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.customer.loan.field.employmentstatus"),  //'Opening Balance Type',
            name:'employmentstatus',
            id: "employmentstatus" + this.heplmodeid + this.id,
            displayField:'name',
//            disabled:true,
            forceSelection: true,
            allowBlank:true,
            width:250,
            valueField:'id',
            mode: 'local',
//            value:this.isCustomer,
            triggerAction: 'all',
            typeAhead:true,
            selectOnFocus:true
        });
        this.employerName= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.customer.loan.field.employername"),
            name: 'employername',
            id:'employername'+this.heplmodeid+this.id,  
            allowBlank:true,
            width:250,
            maskRe: /^[^"]*$/,
            maxLength:100                                 
        });
        this.companyAddress=new Wtf.form.TextArea({
            fieldLabel:WtfGlobal.getLocaleText("acc.customer.loan.field.companyAddress"),
            name: 'companyaddress',
            id:'companyaddress'+this.heplmodeid+this.id,
            maxLength:250,
            height:60,
            allowBlank:true,
            allowNegative:false,
            width:250
        });
           
        this.occupationAndYears= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.customer.loan.field.occupation"),
            name: 'occupationandyears',
            id:'occupationandyears'+this.heplmodeid+this.id,
            allowBlank:true,
            width:250,
            maskRe: /^[^"]*$/,
            maxLength:100
        });
        this.monthlyIncome= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.customer.loan.field.annualIncome"),
            name: 'monthlyincome',
            id:'monthlyincome'+this.heplmodeid+this.id,
            allowBlank:true,
            width:250,
            maskRe: /^[^"]*$/,
            maxLength:100 
        });
        this.noofActiveCreditLoans= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.customer.loan.field.activecreditloan"),
            name: 'noofactivecreditloans',
            id:'noofactivecreditloans'+this.heplmodeid+this.id,
            allowBlank:true,
            width:250,
            maskRe: /^[^"]*$/,
            maxLength:100
        });
        
        this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId:this.id,
            autoHeight: true,
            autoWidth:true,
            disabledClass:"newtripcmbss",
            parentcompId:this.id,
            moduleid:this.moduleid,
            isEdit: this.isEdit || this.isCopy,
            record: this.record
        });

        var UENHelp = WtfGlobal.addLabelHelp("UEN NO. must be in following format:<br/><br/>"+
            "NNNNNNNNP, if UEN Type is Business<br/>"+
            "YYYYNNNNNP, if UEN Type is Local Company<br/>"+
            "TYYPQNNNNP or SYYPQNNNNP, if UEN Type is Others <br/><br/>"+
            "where<br/>"+
            "N = Numeric<br/>"+
            "P = Alphabet<br/>"+
            "Q = Alphanumeric<br/>"+
            "YYYY / TYY / SYY = Year of issuance<br/><br/>"+
            "Note : 'T' represents '20' and 'S' represents '19'. Eg. T08 means year 2008 and S99 means year 1999.");
        
        this.bankaccountno = new Wtf.form.TextField({
           fieldLabel:"<span wtf:qtip='"+(this.isCustomer?WtfGlobal.getLocaleText("acc.customer.bankaccno.tt"):WtfGlobal.getLocaleText("acc.vendor.bankaccno.tt") )+"'>"+ WtfGlobal.getLocaleText("acc.cust.bankAcc")  +"</span>",// WtfGlobal.getLocaleText("acc.cust.bankAcc"),  //'Bank Account No',
           width:200,
           maxLength:30,
           name: 'bankaccountno',
           id: "bankaccountno" + this.heplmodeid+ this.id
        });

        if(Wtf.account.companyAccountPref.countryid != '203') {
	        this.other= new Wtf.form.TextArea({
	            fieldLabel:WtfGlobal.getLocaleText("acc.cust.otherInfo"),  //'Other Information',
	            name:'other',
	            id:this.id+'other',
	            width:200,
	            height:40,
	            xtype:'textarea',
	            maxLength:200
	        });
        } else {
	        this.other= new Wtf.form.TextField({
	        	//fieldLabel: WtfGlobal.getLocaleText("acc.field.GSTNumber"),
	             fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.custGST.number.tip")+"'>"+WtfGlobal.getLocaleText("acc.field.GSTNumber")+"</span>",// You can choose a Dashboard view as Flow Diagram View or Widget View
                     maxLength:30,
	             width:200,
                     name:'other',
	             id:'other'+this.heplmodeid+this.id
	        });
        }
        
        var comboConfig = {
            anchor: '90%',
            minChars: 0,
            allowBlank: true
        };
        
        if (this.isCustomer) {
            this.parentAccount = CommonERPComponent.createCustomerPagingComboBox(200, Wtf.account.companyAccountPref.accountsWithCode ? 500 : 400, 30, this, comboConfig);
        } else {
            this.parentAccount = CommonERPComponent.createVendorPagingComboBox(200, Wtf.account.companyAccountPref.accountsWithCode ? 500 : 400, 30, this, comboConfig);
        }
            
        if (this.parentAccount && this.parentAccount.store) {
            this.parentAccount.store.on('beforeload', function (store) {
                store.baseParams = store.baseParams || {};
                store.baseParams.mode = 2;
                store.baseParams.accountid = (this.record == null ? null : this.record.data['accid']);
                store.baseParams.receivableAccFlag = true//True to load normal recevebale and payables account in customer / vendor dropdown
            }, this);
        }

        this.exchangeRate = new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+(this.isCustomer?WtfGlobal.getLocaleText("acc.customer.exchangerate.tt"):WtfGlobal.getLocaleText("acc.vendor.exchangerate.tt") )+"'>"+ WtfGlobal.getLocaleText("acc.setupWizard.curEx")+"*"  +"</span>", // WtfGlobal.getLocaleText("acc.setupWizard.curEx")+"*",
            name: 'exchangeRate',
            id: "exchangeRate" + this.heplmodeid+ this.id,
            allowBlank:false,
            width:200,
            maxLength:15,
            minvalue : 0,
            decimalPrecision:15,
            value:1,
            validator:function(val){
                if(val>0)
                    return true;
                else
                    return WtfGlobal.getLocaleText("acc.field.Exchangeratemustbegreaterthanzero.");
            }
        });

        this.Currency= new Wtf.form.FnComboBox({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.currency.tt")+"'>"+ WtfGlobal.getLocaleText("acc.cust.currency") +" *"  +"</span>",//WtfGlobal.getLocaleText("acc.cust.currency") +" *",  //'Currency*',
            hiddenName:'currencyid',
            name:'currencyid',
            id: "currencyid" + this.heplmodeid+ this.id,
            disabled:this.isEdit ? (this.enableCurrency==undefined ? this.isEdit : !this.enableCurrency):this.isEdit,
            width:200,
            listAlign:'bl-tl',
            allowBlank:false,
            store: this.currencyStore,
            valueField:'currencyid',
            listAlign:"bl-tl?",
            emptyText:WtfGlobal.getLocaleText("acc.cust.currencyTT"),  //'Please select Currency...',
            forceSelection: true,
            maxHeight:300,
            displayField:'currencyname',
            scope:this,
            selectOnFocus:true
        });
       this.isPermOrOnetime= new Wtf.form.Checkbox({
            name:'isPermOrOnetime',
            id: "isPermOrOnetime" + this.heplmodeid+ this.id,
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.customer.onetime.tt") +"'>"+ WtfGlobal.getLocaleText("acc.cust.onetime")  +"</span>",//WtfGlobal.getLocaleText("acc.cust.onetime"),
            hidden:!this.isCustomer,
            hideLabel:!this.isCustomer,
            checked:false,            
            style: 'padding:0px 0px 10px 0px;',
            width: 10
        })
        this.Currency.on('select',function(combo,record,index){
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
                            callCurrencyExchangeWindow(this.currencyExchangeWinId);
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.common.currency")],2);     //'Alert',"Please set Currency Exchange Rates"
                            Wtf.getCmp(this.currencyExchangeWinId).on("update", function(){
                                this.currencyStore.reload();
                                this.defaultCurrencyStore.reload();
                                this.currencyStore.on("load",function(){
                                    this.Currency.reset();
                                },this);
                            },this);
                        }
                    });
                }else{
                    this.exchangeRate.setValue(1);
                    this.exchangeRate.disable();
                }
        },this);

// TODO       this.modeStore=new Wtf.data.SimpleStore({
//            fields:[{name:"id"},{name:"name"}],
//            data:[[1,"Email"],[2,"Print"]]
//        });
//        this.pdm=new Wtf.form.ComboBox({
//            fieldLabel:'Preferred Delivery Mode*',
//            hiddenName:'pdm',
//            store:this.modeStore,
//            valueField:'id',
//            displayField:'name',
//            mode: 'local',
//            typeAhead: true,
//            allowBlank:false,
//            forceSelection:true,
//            triggerAction:'all'
//       });
       this.CreditTerm= new Wtf.form.FnComboBox({
           fieldLabel:this.isCustomer?"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.customer.term.tt")  +"'>"+  WtfGlobal.getLocaleText("acc.cust.term") +" *" +"</span>":"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.vendor.term.tt")  +"'>"+  WtfGlobal.getLocaleText("acc.ven.term") +" *" +"</span>",//this.isCustomer?WtfGlobal.getLocaleText("acc.cust.term") +" *":WtfGlobal.getLocaleText("acc.ven.term") +" *",  //'Credit Term*':'Debit Term*',
           hiddenName:'termid',
           id: "creditTerm" + this.heplmodeid + this.id,
           allowBlank:false,
           width:200,
           store:Wtf.termds,
           valueField:'termid',
           displayField:'termname',
           selectOnFocus:true,
           forceSelection:true
        });
        this.CreditTerm.on('render',function() {
            this.setPDM();
        },this);
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.creditterm, Wtf.Perm.creditterm.edit))
            this.CreditTerm.addNewFn= this.callCreditTerm.createDelegate(this)

        this.issubFieldset=new Wtf.form.FieldSet({
            border:false,
            xtype:'fieldset',
            autoWidth:true,
            width:250,
            autoHeight:true,
            checkboxName:"issub",
            id: "issub" + this.heplmodeid + this.id,
            checkboxToggle:true,
            disabledClass:"newtripcmbss",
            title:this.isCustomer?WtfGlobal.getLocaleText("acc.cust.sub"):WtfGlobal.getLocaleText("acc.ven.sub"),  //'Is a sub'+this.businessPerson.toLowerCase()+"?",
            collapsed:true,
            defaults:{border:false},
            items:[this.parentAccount]
        });
        
    this.deliveryDateStore = new Wtf.data.SimpleStore({
        fields: [{name:'id', type:'int'}, 'name'],
        data :[[7,'Next Day'], [0,'Every Sunday'], [1,'Every Monday'], [2,'Every Tuesday'], [3,'Every Wednesday'], [4,'Every Thursday'], [5,'Every Friday'], [6,'Every Saturday']]
    });
    this.deliveryDate = new Wtf.form.ComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.deliveryDate"), // "Delivery Date",
        store: this.deliveryDateStore,
        width: 200,
        name: 'deliveryDate',
        hiddenName: 'deliveryDate',
        displayField: 'name',
        valueField: 'id',
        mode: 'local',
        triggerAction: 'all',
        selectOnFocus: true,
        hidden: !(Wtf.account.companyAccountPref.deliveryPlanner && this.isCustomer),
        hideLabel: !(Wtf.account.companyAccountPref.deliveryPlanner && this.isCustomer)
    });
    
    this.deliveryTime = new Wtf.form.TextField({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.deliveryTime"), // "Delivery Time";
        name: 'deliveryTime',
        hiddenName: 'deliveryTime',
        id: "deliveryTime" + this.heplmodeid + this.id,
        width: 200,
        maxLength: 255,
        scope: this,
        hidden: !(Wtf.account.companyAccountPref.deliveryPlanner && this.isCustomer),
        hideLabel: !(Wtf.account.companyAccountPref.deliveryPlanner && this.isCustomer)
    });
        
    this.vehicleNo = new Wtf.form.ExtFnComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.vehicleNo"), // "Vehicle No.",
        name: 'vehicleNoID',
        hiddenName: 'vehicleNoID',
        store: Wtf.vehicleStore,
        mode: 'remote',
        valueField: 'id',
        displayField: 'name',
        width: 200,
        extraFields: '',
        triggerAction: 'all',
        editable: false,
        forceSelection: true,
        selectOnFocus: true,
        scope: this,
        hidden: !(Wtf.account.companyAccountPref.deliveryPlanner && this.isCustomer),
        hideLabel: !(Wtf.account.companyAccountPref.deliveryPlanner && this.isCustomer)
    });
        
    this.driver = new Wtf.form.ExtFnComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.driver"), // "Driver",
        name: 'driverID',
        hiddenName: 'driverID',
        store: Wtf.driverStore,
        mode: 'remote',
        valueField: 'id',
        displayField: 'name',
        width: 200,
        extraFields: '',
        triggerAction: 'all',
        editable: false,
        forceSelection: true,
        selectOnFocus: true,
        scope: this,
        hidden: !(Wtf.account.companyAccountPref.deliveryPlanner && this.isCustomer),
        hideLabel: !(Wtf.account.companyAccountPref.deliveryPlanner && this.isCustomer)
    });
        
        this.copyAddress= new Wtf.form.Checkbox({
            name:'copyadress',
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.copyAdd"),  //'Copy Address',
            checked:false,
            hideLabel:!this.isCustomer,
            hidden:!this.isCustomer,
            cls : 'custcheckbox',
            width: 10
        });
        
        this.overseas= new Wtf.form.Checkbox({
            name:'overseas',
            id: "overseas" + this.heplmodeid + this.id,
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.customer.fullpayment.tt") +"'>"+ WtfGlobal.getLocaleText("acc.cust.fullpayment") +"</span>",  //'Copy Address',
            checked:false,
            hideLabel:!this.isCustomer,
            hidden:!this.isCustomer,
            cls : 'custcheckbox',
            width: 10
        });
        
        this.copyCustomer= new Wtf.form.Checkbox({
            name:'mapcustomervendor',
            id: "mapcustomervendor" + this.heplmodeid + this.id,
            fieldLabel:this.isCustomer?"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.customer.createasvendor.tt")  +"'>"+  WtfGlobal.getLocaleText("acc.field.CreateasVendor") +" *" +"</span>":"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.vendor.createascustomer.tt")  +"'>"+  WtfGlobal.getLocaleText("acc.field.CreateasCustomer") +" *" +"</span>", //this.isCustomer?WtfGlobal.getLocaleText("acc.field.CreateasVendor"): WtfGlobal.getLocaleText("acc.field.CreateasCustomer"),
            checked:false,
            cls : 'custcheckbox',
            width: 10
        });
        
        this.sequenceFormatComboVenCus = new Wtf.form.ComboBox({            
             triggerAction:'all',
             mode: 'local',
             fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.Sequenceformat.tip")+"'>"+ WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat")+"</span>",
             valueField:'id',
             displayField:'value',
             store:this.sequenceFormatStoreVenCus,  
             width:200,
             typeAhead: true,
             forceSelection: true,
             name:'sequenceformatvencus',
             id: "sequenceformatvencus" + this.heplmodeid + this.id,
             hiddenName:'sequenceformatvencus',  
             disabled:true
        });
        
        this.sequenceFormatComboVenCus.on('select',this.getNextSequenceNumberForVenCus,this);
        
        this.createASCustOrVenCode = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip='"+((this.businessPerson=="Customer")?WtfGlobal.getLocaleText("acc.vendor.vendorcode.tt"):WtfGlobal.getLocaleText("acc.customer.custcode.tt") )+"'>"+ ((this.businessPerson=="Customer")?WtfGlobal.getLocaleText("acc.invoice.vendor"):WtfGlobal.getLocaleText("acc.invoice.customer"))+" "+WtfGlobal.getLocaleText("acc.field.Code")  +" * </span>",//((this.businessPerson=="Customer")?WtfGlobal.getLocaleText("acc.invoice.vendor"):WtfGlobal.getLocaleText("acc.invoice.customer"))+" "+WtfGlobal.getLocaleText("acc.field.Code"), //ERP-10888
            name: 'custorvenacccode',
            id:'custorvenacccode'+this.heplmodeid+this.id,
            hideLabel : this.isFixedAsset,
            hidden : this.isFixedAsset,
            scope:this,
            disabled:true,
            width:200,
            maxLength:50,
            listeners:{
                scope:this,
                focus:function(){
//                    this.searchText(this.AccountStoreForCopyCustomer,this.pan,'custorvenacccode'+this.heplmodeid+this.id,'code',!this.isCustomer);
                    this.searchText(this.pan,'custorvenacccode'+this.heplmodeid+this.id,'code',!this.isCustomer,this.businessPerson);
                }
            }
        });
         this.customerVenodorAvailableToSalespersonAgent= new Wtf.form.Checkbox({
            name:'customervenodoravailabletosalespersonagent',
            id: "customervenodoravailabletosalespersonagent" + this.heplmodeid + this.id,
//            fieldLabel:WtfGlobal.getLocaleText("acc.field.CustomerAvailableOnlyToSalesPersons"),
            hideLabel:!this.isCustomer ,
            hidden:!this.isCustomer,
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.CustomerAvailableOnlyToSalesPersons.ttip")  +"'>"+  WtfGlobal.getLocaleText("acc.field.CustomerAvailableOnlyToSalesPersons") +" " +"</span>", 
            checked:false,
            cls : 'custcheckbox',
            width: 10
        });
           
         this.venodorAvailableToAgentCheck= new Wtf.form.Checkbox({
            name:'venodorAvailableToAgentCheck',
            id: "venodorAvailableToAgentCheck" + this.heplmodeid + this.id,
            hideLabel:this.isCustomer ,
            hidden:this.isCustomer,
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.VendorAvailableOnlyToAgents.ttip")  +"'>"+  WtfGlobal.getLocaleText("acc.field.CustomerAvailableOnlyToAgents") +" " +"</span>", 
            checked:false,
            cls : 'custcheckbox',
            width: 10
        });
        
        this.agentCombo = new Wtf.common.Select(Wtf.apply({
            multiSelect:true,
            fieldLabel:"<span wtf:qtip='"+  WtfGlobal.getLocaleText("acc.masterConfig.20") +"'>"+  WtfGlobal.getLocaleText("acc.masterConfig.20") +"</span>",
            forceSelection:true
        },{
            name:'agent',
            id: "agent" + this.heplmodeid+ this.id,
            width:200,
            hideLabel:this.isCustomer,
            hidden:this.isCustomer,
            emptyText:WtfGlobal.getLocaleText("acc.field.agentfield.vendorform"),
            store:Wtf.agentStore,
            valueField:'id',
            addCreateOpt:false,
            addNewFn:this.addMaster.createDelegate(this,[20,Wtf.salesPersonStore]),
            clearTrigger: this.readOnly ? false : true,
            displayField:'name'
        }));
        Wtf.agentStore.load();
        this.agentRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);

        this.agentStore = new Wtf.data.SimpleStore({
            fields: this.agentRec,
            data: []
        });
         this.agentSingleSelectCombo= new Wtf.form.FnComboBox({ // Default Agent
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.vendor.mapagent.tt") +"'>"+ WtfGlobal.getLocaleText("acc.field.MapDefaultAgent") +"</span>",//WtfGlobal.getLocaleText("acc.field.MapSalesPerson"),
            name: 'defaultAgent',
            id: "defaultAgent" + this.heplmodeid + this.id,
            hideLabel:this.isCustomer,
            hidden:this.isCustomer,
            disabled:true,
            forceSelection: true,
            store: this.agentStore,
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectDefaultAgent"),
            valueField: 'id',
            displayField: 'name',
            width:200,
            addNoneRecord: true
        });
        this.multiSalesPerson = new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.customer.mapmultisalesperson.tt") + "'>" + WtfGlobal.getLocaleText("acc.field.MapmultiSalesPerson") + "</span>",
            forceSelection: true
        }, {
            name: 'mapMultiSalesPerson',
            id: "mapMultiSalesPerson" + this.heplmodeid + this.id,
            width: 200,
            hideLabel: !this.isCustomer,
            hidden: !this.isCustomer,
            emptyText: WtfGlobal.getLocaleText("acc.field.SelectmultiSalesPerson"),
            store: Wtf.salesPersonStore,
            valueField: 'id',
            displayField: 'name',
            addCreateOpt:false,
            addNewFn:this.addMaster.createDelegate(this,[15,Wtf.salesPersonStore]),
            addNoneRecord: true,
            activated:this.isCustomer ? true : false
        }));
        
        if (Wtf.account.companyAccountPref.enablesalespersonAgentFlow && (!this.isEdit || this.isCopy)) {
            this.customerVenodorAvailableToSalespersonAgent.setValue(true);
            this.venodorAvailableToAgentCheck.setValue(true);
            if (this.isCustomer) {
                this.multiSalesPerson.allowBlank = false;
            } else {
                this.agentCombo.allowBlank = false;
            }
        }
        this.mappingReceivedFrom = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.mp.mapreceivedFrom"), //'Received From'
            hiddenName: "mappingReceivedFromCmb",
            id: "mapRecFrom" + this.heplmodeid + this.id,
            store: this.RPReceivedFromStore,
            valueField: 'id',
            displayField: 'name',
            allowBlank: true,
            disabled: this.readOnly,
            emptyText: WtfGlobal.getLocaleText("acc.rp.selreceivedfrom"), //'Select Received From...'
            minChars: 1,
            extraFields: '',
            width: 200,
//        listWidth: 200,
            extraComparisionField: 'name', // type ahead search on acccode as well.
            mode: 'remote',
            addNewFn:this.addReceivedFrom.createDelegate(this),
            triggerAction: 'all',
            typeAhead: true,
            forceSelection: true
        });
    
        this.mappingPaidTo = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.mp.mapPaidTo"), //Map Paid To
            hiddenName: "mappingPaidToCmb",
            id: "mapPaidTo" + this.heplmodeid + this.id,
            store: this.MPPaidToStore,
            valueField: 'id',
            displayField: 'name',
            allowBlank: true,
            disabled: this.readOnly,
            emptyText: WtfGlobal.getLocaleText("acc.mp.selpaidto"), //Select Paid To...
            minChars: 1,
            extraFields: '',
            width: 200,
            extraComparisionField: 'name', // type ahead search on acccode as well.
            mode: 'remote',
            addNewFn: this.addPaidTo.createDelegate(this),
            triggerAction: 'all',
            typeAhead: true,
            forceSelection: true
        });
        
         this.multiSalesPerson.on('beforeselect', function (combo, record, index) {
            return validateSelection(combo, record, index);
        }, this);
        
        this.salesPersonRec = Wtf.data.Record.create([
            {name:'id'},
            {name:'name'}
        ]);
        
        this.salesPersonStore = new Wtf.data.SimpleStore({
            fields:this.salesPersonRec,
            data:[]
        });
      
        this.salesPerson= new Wtf.form.ExtFnComboBox({ // Default Sales Person
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.customer.mapsalesperson.tt") +"'>"+ WtfGlobal.getLocaleText("acc.field.MapSalesPerson") +"</span>",//WtfGlobal.getLocaleText("acc.field.MapSalesPerson"),
            name: 'mapsalesperson',
            id: "mapsalesperson" + this.heplmodeid + this.id,
            hideLabel:!this.isCustomer,
            hidden:!this.isCustomer,
            disabled:true,
            forceSelection: true,
            store: this.salesPersonStore,
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectaSalesPerson"),
            valueField: 'id',
            displayField: 'name',
            width:200,
            addNoneRecord: true,
            extraFields:[],//it is required when  ExtFnComboBox component
            activated:this.isCustomer ? true : false
        });
         this.salesPerson.on('beforeselect', function (combo, record, index) {
            return validateSelection(combo, record, index);
        }, this);
        
        
//        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.create))
//            this.salesPerson.addNewFn= this.addMaster.createDelegate(this,[15,Wtf.salesPersonStore])
        chkSalesPersonload();
        Wtf.salesPersonStore.on("load", this.setSalesPerson, this);
        this.RPReceivedFromStore.on("load", this.setReceivedFromStore, this);
        this.MPPaidToStore.on("load", this.setMappingPaidTo, this);
        //this.GSTINRegistrationTypeStore.on("load", this.setGSTINRegistrationType, this);
        
        this.copyCustomer.on('check', this.copyCustomerCheckHandler, this);
        this.customerVenodorAvailableToSalespersonAgent.on('change', this.makeSalesPersonAgentMandatory, this);
        this.venodorAvailableToAgentCheck.on('change', this.makeAgentMandatory, this);
        this.salesPerson.on('select', this.checkDefaultSalesPerson, this);
        this.multiSalesPerson.on('change',this.checkDefaultSalesPerson,this);
        this.multiSalesPerson.on('clearval',this.checkDefaultSalesPerson,this);
        this.multiSalesPerson.on('select',this.checkSalesPersonMappedWithUser,this);
        this.agentSingleSelectCombo.on('select',this.checkDefaultAgent, this);
        this.agentCombo.on('change',this.checkDefaultAgent,this);
        this.agentCombo.on('clearval',this.checkDefaultAgent,this);
        this.agentCombo.on('select',this.checkAgentMappedWithUser,this);
        this.taxNo=new Wtf.form.TextField({
           fieldLabel: WtfGlobal.getLocaleText("acc.cust.taxID"),  //'Tax Id',
           name: 'taxno',
           hideLabel:this.isCustomer,
           hidden:this.isCustomer,
           width:200,
           maxLength:255
        });


    	this.country = new Wtf.form.ComboBox({
            store: Wtf.countryStore,
            width:200,
            id:'countrycombo',
            labelWidth:80,
            hidden:((this.isCustomer && Wtf.account.companyAccountPref.countryid == '203')?false:true),
            hideLabel:((this.isCustomer && Wtf.account.companyAccountPref.countryid == '203')?false:true),
            fieldLabel:WtfGlobal.getLocaleText("acc.rem.200"),
            displayField:'name',
            valueField:'id',
            triggerAction: 'all',
            mode: 'local',
            typeAhead:true,
            emptyText: WtfGlobal.getLocaleText("acc.rem.203"),
            selectOnFocus:true,
            forceSelection: true
        });    
        /*
         * For Philippines country - create Customer/Vendor Type Combo ERP-41394,ERP-41499
         * For Indonesia Country - Tax Type
         */
        if (Wtf.account.companyAccountPref.countryid==Wtf.Country.PHILIPPINES || Wtf.Countryid == Wtf.Country.INDONESIA){
            this.CustVenTypeRec = new Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'},
                {name: 'code'},
                {name: 'defaultMasterItem'} // Added default master key in records
            ]);
            this.CustVenTypeStore = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.CustVenTypeRec),
                url: "ACCMaster/getMasterItems.do",
                baseParams: {
                    mode: 112,
                    groupid: Wtf.Countryid == Wtf.Country.INDONESIA ? 65 : 63
                }
            });
            this.CustVenTypeStore.load();
            this.CustVenTypeCombo = new Wtf.form.ExtFnComboBox({
                fieldLabel:  Wtf.Countryid == Wtf.Country.INDONESIA ? WtfGlobal.getLocaleText("acc.common.taxType") : this.isCustomer ? WtfGlobal.getLocaleText("acc.customer.GST.type"): WtfGlobal.getLocaleText("acc.vendor.GST.type"), // For Indonesia - Tax Type & For Philippines -  Customer/ Vendor Type
                hiddenName: "CustVenTypeCombo",
                id: "CustVenType" + this.heplmodeid + this.id,
                store: this.CustVenTypeStore,
                valueField: 'id',
                displayField: 'name',
                emptyText: WtfGlobal.getLocaleText('acc.common.select') + " " + (Wtf.Countryid == Wtf.Country.INDONESIA ? WtfGlobal.getLocaleText("acc.common.taxType") : (this.isCustomer ? WtfGlobal.getLocaleText("acc.customer.GST.type") : WtfGlobal.getLocaleText("acc.vendor.GST.type"))), //For Indonesia - Select Tax Type & For Philippines - Select Customer/ Vendor Type
                minChars: 1,
                extraFields: Wtf.Countryid == Wtf.Country.INDONESIA ? ['code'] : [],
                hideLabel:!(Wtf.account.companyAccountPref.countryid == Wtf.Country.PHILIPPINES || Wtf.Countryid == Wtf.Country.INDONESIA),
                hidden:!(Wtf.account.companyAccountPref.countryid == Wtf.Country.PHILIPPINES || Wtf.Countryid == Wtf.Country.INDONESIA),
                disabled: this.readOnly ? true : false,
                extraComparisionField: 'name', // type ahead search 
                mode: 'remote',
                triggerAction: 'all',
                typeAhead: true,
                forceSelection: true,
//                searchOnField: true,
                width: 200
            });
        
         /*
         * For Philippines Customer/Vendor Type "NA" should not be visible
         * ERP-41394
         */
        
            this.CustVenTypeStore.on('load', function (store) {
                this.CustVenTypeStore.each(function (record) {
                    var recdata = record.data;
                    if (recdata.defaultMasterItem == Wtf.GSTCUSTVENTYPE.NA) {
                        this.CustVenTypeStore.remove(record);
                    }
                }, this);
            }, this);        
        }
        if (Wtf.account.companyAccountPref.countryid == Wtf.Country.PHILIPPINES) {
            /*
             * For Philippines country- create field Tax Identification Number(TIN) ERP-41394,ERP-41499
             */
            this.TIN = new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText("acc.customer.philipines.tin"),
                name: 'TIN',
                id: "TIN" + this.heplmodeid + this.id,
                disabled: this.readOnly ? true : false,
                hideLabel: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.PHILIPPINES),
                hidden: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.PHILIPPINES),
                width: 200,
                maxLength: 14,
                allowNegative: false
            });
        }
        this.taxRec = new Wtf.data.Record.create([
           {name: 'prtaxid',mapping:'taxid'},
           {name: 'prtaxname',mapping:'taxname'},
           {name: 'taxdescription'},
           {name: 'percent',type:'float'},
           {name: 'taxcode'},
           {name: 'accountid'},
           {name: 'accountname'},
           {name: 'hasAccess'},
           {name: 'applydate', type:'date'}

        ]);
        this.taxStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.taxRec),
            url : "ACCTax/getTax.do",
            baseParams:{
                mode:33,
                moduleid :this.moduleid,
                includeDeactivatedTax: this.isEdit!=undefined? this.isEdit : false
            }
        });
        
        this.Tax= new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+(this.isCustomer?WtfGlobal.getLocaleText("acc.customer.tax.tt"):WtfGlobal.getLocaleText("acc.vendor.tax.tt") )+"'>"+ WtfGlobal.getLocaleText("acc.invoice.Tax")  +"</span>", //WtfGlobal.getLocaleText("acc.invoice.Tax"),  //'Tax',
            id:"tax"+this.heplmodeid+this.id,
            disabled:false,
            hiddenName:'tax',
            hideLabel:(WtfGlobal.isIndiaCountryAndGSTApplied() || WtfGlobal.isUSCountryAndGSTApplied()) || (Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// hide if company is malaysian and GST is not enabled for it +//Hide if new GST enabled for US and INDIA
            hidden: (WtfGlobal.isIndiaCountryAndGSTApplied() || WtfGlobal.isUSCountryAndGSTApplied()) || (Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// hide if company is malaysian and GST is not enabled for it +//Hide if new GST enabled for US and INDIA
            width:200,
            store:this.taxStore,
            valueField:'prtaxid',
            forceSelection: true,
            displayField:'prtaxname',
            typeAhead: true,
            mode: 'remote',
            minChars:0,
//            hidden:this.isCustomer,
//            hideLabel:this.isCustomer,
            scope:this,
            selectOnFocus:true,
            addNoneRecord: true,         //For 'None' option in Tax Combo.
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
        
        
        this.taxStore.on('load', function(){
            if(this.isEdit || this.isCopy){
                this.Tax.setValue(this.record.data.taxId);
            }
//            if (this.isCopy) {
//                if (isTaxActivate(this.taxStore, this.record.data.taxId, "prtaxid")) {
//                    this.Tax.setValue(this.record.data.taxId);
//                }
//            }
//            var storeNewRecord = new this.taxRec({
//                prtaxid: '-1',
//                prtaxname: 'None'
//            });
//            this.taxStore.insert(this.taxStore.getCount()+1, storeNewRecord);
        },this);
        
        this.taxStore.load();
        
//        if(!WtfGlobal.EnableDisable(Wtf.UPerm.tax, Wtf.Perm.tax.view))
//            this.Tax.addNewFn=this.addTax.createDelegate(this);
        
        this.issubFieldset.on("beforeexpand",this.checkParent,this)
        this.issubFieldset.on("beforecollapse", function () {
            this.parentAccount.allowBlank=true;
            this.parentAccount.reset();
        }, this);
        this.openingBal.on('change',function(a,newval){if(newval==0)this.balTypeEditor.disable();else this.balTypeEditor.enable();},this)
    },
//    addTax:function(){
//         var p= callTax("taxwin");
//         Wtf.getCmp("taxwin").on('update', function(){this.Grid.taxStore.reload();}, this);
//    },

    
fillIBGDetailsValues: function(){        
    if(this.ibgReceivingDetails != undefined || this.ibgReceivingDetails!=""){
        this.gridRec = new Wtf.data.Record.create([
        {
            name:'ibgId'
        },

        {
            name:'receivingBankCode'
        },

        {
            name:'receivingBankName'
        },

        {
            name:'receivingBranchCode'
        },

        {
            name:'receivingAccountNumber'
        },

        {
            name:'receivingAccountName'
        },
        {
            name:'collectionAccNo'
        },

        {
            name:'collectionAccName'
        },

        {
            name:'giroBICCode'
        },

        {
            name:'refNumber'
        },
        {
            name:'cimbReceivingBankDetailId'
        }
        ]);
    
        var ibgDetailslen = 0;
        if(this.ibgReceivingDetails && this.ibgReceivingDetails !=''){
            var parsed = JSON.parse(this.ibgReceivingDetails);
            this.ibgReceivingDetailsArr=new Array(parsed);
        }
        if(this.ibgReceivingDetailsArr!=undefined){
            if(this.ibgReceivingDetailsArr.length>0){
                ibgDetailslen = this.ibgReceivingDetailsArr.length;
            }
            for(var i=0;i<ibgDetailslen;i++){
                var ibgReceivingDetails=new this.gridRec({
                    ibgId: this.ibgReceivingDetailsArr[0].ibgId!=undefined?this.ibgReceivingDetailsArr[0].ibgId:"",
                    cimbReceivingBankDetailId: this.ibgReceivingDetailsArr[0].cimbReceivingBankDetailId!=undefined?this.ibgReceivingDetailsArr[0].cimbReceivingBankDetailId:"",
                    receivingBankCode:this.ibgReceivingDetailsArr[0].receivingBankCode!=undefined?this.ibgReceivingDetailsArr[0].receivingBankCode:"",
                    receivingBankName:this.ibgReceivingDetailsArr[0].receivingBankName!=undefined?this.ibgReceivingDetailsArr[0].receivingBankName:"",
                    receivingBranchCode: this.ibgReceivingDetailsArr[0].receivingBranchCode!=undefined?this.ibgReceivingDetailsArr[0].receivingBranchCode:"",
                    receivingAccountNumber:this.ibgReceivingDetailsArr[0].receivingAccountNumber!=undefined?this.ibgReceivingDetailsArr[0].receivingAccountNumber:"",
                    collectionAccNo:this.ibgReceivingDetailsArr[0].collectionAccNo!=undefined?this.ibgReceivingDetailsArr[0].collectionAccNo:"",
                    receivingAccountName:this.ibgReceivingDetailsArr[0].receivingAccountName!=undefined?this.ibgReceivingDetailsArr[0].receivingAccountName:"",
                    collectionAccName:this.ibgReceivingDetailsArr[0].collectionAccName!=undefined?this.ibgReceivingDetailsArr[0].collectionAccName:"",
                    giroBICCode:this.ibgReceivingDetailsArr[0].giroBICCode!=undefined?this.ibgReceivingDetailsArr[0].giroBICCode:"",
                    refNumber:this.ibgReceivingDetailsArr[0].refNumber!=undefined?this.ibgReceivingDetailsArr[0].refNumber:""
                });
            }   
        }
    }  
    
    this.ibgDetails = new Wtf.account.VendorIBGDetails({
        title:'IBG-Receiving Bank Details',
        iconCls :getButtonIconCls(Wtf.etype.deskera),
        resizable:false,
        isEdit: true,
        layout:'border',
        ibgReceivingDetails:ibgReceivingDetails,
        CIMBbank:this.CIMBbank,
        DBSbank:this.DBSbank,
        modal:true,
        height:500,
        width:550
    });
        
    this.ibgDetails.on('datasaved',function(ibgForm, ibgFormJsonObj){
        this.ibgReceivingDetails = ibgFormJsonObj;
        this.DBSbank = (ibgForm.DBSbank)?ibgForm.DBSbank.getValue():false;
        this.CIMBbank = (ibgForm.CIMBbank)?ibgForm.CIMBbank.getValue():false;
    },this);
        
    this.ibgDetails.show();        
},

setNextNumber:function(store){
    if(this.sequenceFormatStore.getCount()>0){
        if(this.isEdit || this.isCopy){
            var index=this.sequenceFormatStore.find('id',this.record.data.sequenceformat);
                if (index != -1) {
                    this.sequenceFormatCombobox.setValue(this.record.data.sequenceformat);
                    if(this.isCopy){
                        this.sequenceFormatCombobox.enable();
                    } else{
                        this.sequenceFormatCombobox.disable();
                    }
                    
                    if (this.record.data.sequenceformat == "NA") {
                        if (this.readOnly || this.record.data.synchedfromotherapp || (!this.enableCurrency) && !Wtf.account.companyAccountPref.allowCustVenCodeEditing && !this.isCopy) {          //this.enableCurrency=true when customer/vendor used in any transaction
                            this.code.disable();
                        } else {
                            this.code.enable();
                        }
                    } else {
                        this.code.disable();
                    }

                } else {
                    this.sequenceFormatCombobox.setValue("NA");
                    if(this.isCopy){
                        this.sequenceFormatCombobox.enable();
                    } else{
                        this.sequenceFormatCombobox.disable();
                    }
                    if (this.readOnly || this.record.data.synchedfromotherapp || (!this.enableCurrency) && !Wtf.account.companyAccountPref.allowCustVenCodeEditing) {          //this.enableCurrency=true when customer/vendor used in any transaction
                        this.code.disable();
                    } else {
                        this.code.enable();
                    }
                }
        }else{
            var count=this.sequenceFormatStore.getCount();
            for(var i=0;i<count;i++){
                var seqRec=this.sequenceFormatStore.getAt(i)
                if(seqRec.json.isdefaultformat=="Yes"){
                    this.sequenceFormatCombobox.setValue(seqRec.data.id) 
                    break;
                }
            }
            if(this.sequenceFormatCombobox.getValue()!=""){
                this.getNextSequenceNumber(this.sequenceFormatCombobox);   
            } else{
                 this.code.setValue("");  
                 this.code.disable();
            }
        }
    }
},
/**
 * GST Details Changes for Customer vendor. IF customer/ Vendor used in Transaction then show confirm message.
 * GST details - GSTIN Registration Type , Customer/ Vendor Type
 * @param {type} combo
 * @param {type} newValue
 * @param {type} _oldValue
 * @returns {undefined}
 */
GSTDetailsBeforeSelect: function (combo, newValue , _oldValue ) {
    var fieldLabel = combo.fieldLabel.replace('*', '');
    Wtf.Ajax.requestEx({
        url: "ACCVendorCMN/isCustomerVendorUsedInTransacton.do",
        params: {
            isCustomer: this.isCustomer,
            accid: this.record.data.accid
        }
    }, this, function (successResponse) {
        if (successResponse != undefined && successResponse.isUsed) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.confirm"),
                msg: WtfGlobal.getLocaleText({key: "acc.common.gst.detailchange.customervendor", params: [fieldLabel]}),
                buttons: Wtf.MessageBox.YESNO,
                icon: Wtf.MessageBox.QUESTION,
                width: 300,
                scope: {
                    scopeObject: this
                },
                scope :this,
                fn: function (btn) {
                    if (btn != "yes") {
                        combo.setValue(_oldValue);
                    }
                   this.validateGSTINNumber();
                }
            }, this);
        }
    },
            function (failureResponse) {
            });
},
getNextSequenceNumber:function(combo){
    if(combo.getValue()=="NA"){
        this.code.reset();
        this.code.enable();
    }else{
        Wtf.Ajax.requestEx({
            url:"ACCCompanyPref/getNextAutoNumber.do",
            params:{
                from:this.businessPerson=="Customer"?Wtf.autoNum.customer:Wtf.autoNum.vendor,
                sequenceformat:combo.getValue()
            }
        }, this, function(resp){
            if(resp.data=="NA"){
                this.code.reset();
                this.code.enable();
            }else {
                this.code.setValue(resp.data);  
                this.code.disable();
            }
        });
    }
},

 setNextNumberCusVen:function(store){
    var count=this.sequenceFormatStoreVenCus.getCount();
    if(this.sequenceFormatStoreVenCus.getCount()>0 && !this.isEdit ){
            for(var i=0;i<count;i++){
                var seqRec=this.sequenceFormatStoreVenCus.getAt(i)
                if(seqRec.json.isdefaultformat=="Yes"){
                    this.sequenceFormatComboVenCus.setValue(seqRec.data.id) 
                    break;
                }
                   
            }
            if(this.sequenceFormatComboVenCus.getValue()!=""){
                this.getNextSequenceNumberForVenCus(this.sequenceFormatComboVenCus);
            } else{
                this.createASCustOrVenCode.setValue("");  
                this.createASCustOrVenCode.disable();
            }
    }else{
         this.getCustOrVenCode(); 
    }   
 },
 getNextSequenceNumberForVenCus:function(combo){
      if(combo.getValue()=="NA"){
           this.createASCustOrVenCode.reset();
           this.createASCustOrVenCode.enable();
      }else{
          Wtf.Ajax.requestEx({
            url:"ACCCompanyPref/getNextAutoNumber.do",
            params:{
                from:this.businessPerson=="Customer"?Wtf.autoNum.vendor:Wtf.autoNum.customer,//reverse mode is given for "create as venor" and "create as customer1"
                sequenceformat:combo.getValue()
            }
        }, this, function(resp){
            if(resp.data=="NA"){
                this.createASCustOrVenCode.reset();
                this.createASCustOrVenCode.enable();
            }else {
                this.createASCustOrVenCode.setValue(resp.data);  
                this.createASCustOrVenCode.disable();
            }
        });
      }  
 },
addReceivedFrom:function(){
        addMasterItemWindow(18);// 18 is ID of receive from
    },
    addPaidTo:function () {
        addMasterItemWindow(17);// 17 is ID of Paid To
    },
    openProductWindow: function() {
//        callProductWindow(false, null, "productWin");

        this.productSelWin = new Wtf.account.PreferredProductSelectionWindow({
            renderTo: document.body,
            height : 600,
            width : 700,
            title:WtfGlobal.getLocaleText("acc.preferredproductselection.window.title"),
            layout : 'fit',
            modal : true,
            resizable : false,
            id:this.id+'ProductSelectionWindow',
            moduleid:this.moduleid,
            heplmodeid:this.heplmodeid,
            isJobWorkOrderReciever:this.isJobWorkOrderReciever,
            parentCmpID:this.parentCmpID,
            parentCombo:this.ProductMapping,
            isCustomer : this.isCustomer,
            mappedProducts: this.ProductMapping.getValue(),
            customerid : this.detailPanel.accid
        });
        this.productSelWin.show();
        
    },
    openHistoryWindow: function() {
        var config = {};
        config.masterid = this.recordid;
        config.isCustomer = this.isCustomer;
        config.gsthistorydetails = false;
        config.parentObj = this;
        callGSTHistoryInput(config);
    },
     copyCustomerCheckHandler : function(){
        if(this.copyCustomer.getValue()){
            this.sequenceFormatComboVenCus.enable();
            
             if (!this.readOnly) {
                this.createASCustOrVenCode.enable();
            }
            
            this.createASCustOrVenCode.allowBlank=false;//ERP-10888
            this.sequenceFormatStoreVenCus.load();
            
            if (!this.readOnly) {
                this.businessPerson == "Customer" ? this.defaultVendorAccount.enable() : this.defaultCustomerAccount.enable();
            }
            if (!this.readOnly) {
                this.businessPerson == "Customer" ? this.residentialstatus0.enable() : "";
                this.businessPerson == "Customer" ? this.residentialstatus1.enable() : "";
            }
            
             this.businessPerson=="Customer"?this.defaultVendorAccount.allowBlank=false:this.defaultCustomerAccount.allowBlank=false;
            if(this.businessPerson=="Customer" && (this.defaultVendorAccount.getValue()==undefined ||this.defaultVendorAccount.getValue()=="")){
                this.defaultVendorAccount.setValue(Wtf.account.companyAccountPref.vendordefaultaccount);
            }else if(this.defaultCustomerAccount.getValue()==undefined || this.defaultCustomerAccount.getValue()==""){
                this.defaultCustomerAccount.setValue(Wtf.account.companyAccountPref.customerdefaultaccount);
            }
            this.sequenceFormatStoreVenCus.on('load',this.setNextNumberCusVen,this);
//            if (!this.isEdit) {
//                this.AccountStoreForCopyCustomer.load();
//            }
            if(this.businessPerson=="Customer"){
                this.deducteeType.enable();
                if (!this.readOnly) {
                    this.manufacturerTypeCombo.enable();
                }
                WtfGlobal.updateFormLabel(this.manufacturerTypeCombo,WtfGlobal.getLocaleText("acc.field.india.typeofmanufacturer")+"*");
                if(!Wtf.isEmpty(Wtf.manufacturerType) && Wtf.isExciseApplicable){
                    this.manufacturerTypeCombo.setValue(Wtf.manufacturerType);
                }
                this.panStatusCombo.enable();
            }
        }else{
            this.sequenceFormatComboVenCus.reset();
            this.sequenceFormatComboVenCus.disable();
            this.createASCustOrVenCode.disable();
             this.createASCustOrVenCode.allowBlank=true;//ERP-10888
            this.createASCustOrVenCode.reset();
            if (!this.readOnly) {
                this.businessPerson == "Customer" ? this.residentialstatus0.disable() : this.residentialstatus0.enable();
                this.businessPerson == "Customer" ? this.residentialstatus1.disable() : this.residentialstatus1.enable();
            }
            this.businessPerson=="Customer"?this.defaultVendorAccount.disable():this.defaultCustomerAccount.disable();
            this.businessPerson=="Customer"?this.defaultVendorAccount.allowBlank=true:this.defaultCustomerAccount.allowBlank=true;
            this.businessPerson=="Customer"?this.defaultVendorAccount.reset():this.defaultCustomerAccount.reset();
            if(this.businessPerson=="Customer"){
                this.deducteeType.reset();
                this.deducteeType.disable();
                this.manufacturerTypeCombo.reset();
                this.manufacturerTypeCombo.disable();
                WtfGlobal.updateFormLabel(this.manufacturerTypeCombo,WtfGlobal.getLocaleText("acc.field.india.typeofmanufacturer"));
                this.panStatusCombo.reset();
                this.panStatusCombo.disable();
                if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.isTDSApplicable){
                    this.tdsInterestPayableAccount.disable();
                    this.natureOfPayment.disable();
                    this.natureOfPayment.allowBlank = true;
                }
            }
        }
    },
    makeAgentMandatory:function(){
        if(this.venodorAvailableToAgentCheck.getValue()){
//            this.copyCustomer.setValue(false);    //SDP-11708
//            this.copyCustomer.disable();
            this.agentCombo.allowBlank=false;
        }else{
            this.agentCombo.allowBlank=true;
            this.copyCustomer.enable();
        }
        
    },
   makeSalesPersonAgentMandatory : function(){
        if(this.customerVenodorAvailableToSalespersonAgent.getValue()){
//            this.copyCustomer.setValue(false);    //SDP-11708
//            this.copyCustomer.disable();
            this.multiSalesPerson.allowBlank=false;
        }else{
            this.multiSalesPerson.allowBlank=true;
            this.copyCustomer.enable();
        }
    },
    setSalesPerson: function(){
        if(this.isEdit && Wtf.salesPersonStore.getCount()>0){
            this.multiSalesPerson.setValue(this.record.data.mappedMultiSalesPersonId);
            this.salesPerson.setValue(this.record.data.mappedSalesPersonId);
        }
        Wtf.salesPersonStore.un("load", this.setSalesPerson, this);
    },
    setReceivedFromStore: function(){
        if((this.isEdit || this.isCopy) && this.RPReceivedFromStore.getCount()>0){
            this.mappingReceivedFrom.setValue(this.record.data.mappedReceivedFromId);
        }
        this.RPReceivedFromStore.un("load", this.setReceivedFromStore, this);
    },
    setMappingPaidTo: function () {
        if ((this.isEdit || this.isCopy) && this.MPPaidToStore.getCount() > 0) {
            this.mappingPaidTo.setValue(this.record.data.mappedPaidToId);
        }
        this.MPPaidToStore.un("load", this.setMappingPaidTo, this);
    },
    setGSTINRegistrationType: function () {
        if ((this.isEdit || this.isCopy) && this.GSTINRegistrationTypeStore.getCount() > 0) {
            this.GSTINRegistrationTypeCombo.setValForRemoteStore(this.record.data.GSTINRegistrationTypeId,this.record.data.GSTINRegistrationTypeName);
        }
        this.GSTINRegistrationTypeStore.un("load", this.setGSTINRegistrationType, this);
    },
    setCustomerVendorType: function () {
        if ((this.isEdit || this.isCopy) && this.CustomerVendorTypeStore.getCount() > 0) {
            this.CustomerVendorTypeCombo.setValForRemoteStore(this.record.data.CustomerVendorTypeId,this.record.data.CustomerVendorTypeName);
        }
        this.CustomerVendorTypeStore.un("load", this.setCustomerVendorType, this);
    },
    /*
     * 
     * Set value to Customer/Vendor type combo for Philipines country ERP-41499
     */
    setCustVenType: function () {
      if((Wtf.account.companyAccountPref.countryid == Wtf.Country.PHILIPPINES || Wtf.Countryid == Wtf.Country.INDONESIA) && this.CustVenTypeCombo!=undefined){
        if ((this.isEdit || this.isCopy) && this.CustVenTypeStore.getCount() > 0) {
            this.CustVenTypeCombo.setValForRemoteStore(this.record.data.CustomerVendorTypeId,this.record.data.CustomerVendorTypeName);
        }
        this.CustVenTypeStore.un("load", this.setCustVenType, this);
      }
    },
  setAgents : function(){
       if((this.isEdit || this.isCopy) && Wtf.agentStore.getCount()>0){
            this.agentCombo.setValue(this.record.data.agentsmappedwithvendor);
            this.agentSingleSelectCombo.setValue(this.record.data.defaultagentmappingid);
        }else{
            Wtf.agentStore.load();
        }
         Wtf.agentStore.un("load", this.setAgents, this);
  },
    setDeducteeType : function(){
        if(this.isEdit && Wtf.deducteeTypeStore.getCount()>0){
            if(this.deducteeType != undefined && this.record.data.deducteeTypeId){
                this.deducteeType.setValue(this.record.data.deducteeTypeId);
            }
        }else{
            Wtf.deducteeTypeStore.load();
        }
        Wtf.deducteeTypeStore.un("load", this.setDeducteeType, this);
    },
    setDefaultNatureOfPurchase : function(){
        if(this.isEdit && Wtf.defaultNatureOfPurchaseStore.getCount()>0){
            if(this.defaultNatureOfPurchase != undefined && this.record.data.defaultnatureofpurchase){
                this.defaultNatureOfPurchase.setValue(this.record.data.defaultnatureofpurchase);
            }
        }else{
            Wtf.defaultNatureOfPurchaseStore.load();
        }
        Wtf.defaultNatureOfPurchaseStore.un("load", this.setDefaultNatureOfPurchase, this);
    },
    interCustomerCheckHandler : function(){
        if(this.InterCompany.getValue()){
            this.InterCompanyType.enable();
        }else{
            this.InterCompanyType.disable();
            this.InterCompanyType.reset();
        }
    },
    panStatusFun: function () {
        if (Wtf.isEmpty(this.PANNo.getValue())) {
            this.panStatusCombo.enable();
            if(!Wtf.isEmpty(this.isCustomer) && !this.isCustomer) {
                //If PAN status is "PANNOTAVBL" or "APPLIEDFOR", Higer Rate will be mandatory.
                this.higherRate.allowBlank = false;
                this.higherRate.minValue = 1;
            }
        } else {
            this.panStatusCombo.reset();
            this.panStatusCombo.disable();
            if(!Wtf.isEmpty(this.isCustomer) && !this.isCustomer) {
                //If PAN is given, Higer Rate will be non-mandatory.
                this.higherRate.allowBlank = true;
                this.higherRate.minValue = 0;
            }
            this.PANNo.allowBlank=false;
        }
        if (!Wtf.isEmpty(this.residentialstatus0) && this.residentialstatus0.getValue() && !this.isCustomer) {
            if (Wtf.isEmpty(this.PANNo.getValue())) {
                this.higherRatePanel.show();
                //If PAN status is "PANNOTAVBL" or "APPLIEDFOR", Higer Rate will be mandatory.
                this.higherRate.allowBlank = false;
                this.higherRate.minValue = 1;
                this.higherRatePanel.doLayout();
                this.lowerRatePanel.hide();
            } else {
                this.higherRatePanel.hide();
                //If PAN is given, Higer Rate will be non-mandatory.
                this.higherRate.allowBlank = true;
                this.higherRate.minValue = 0;
                this.lowerRatePanel.show();
                this.lowerRatePanel.doLayout();
            }
        } else {
            this.higherRatePanel.hide();
            //If PAN is given, Higer Rate will be non-mandatory.
            this.higherRate.allowBlank = true;
            this.higherRate.minValue = 0;
            this.lowerRatePanel.hide();
            this.lowerRatePanel.doLayout();
        }

    },
    funEnableDisableCombo:function(isEdit,allowBlank){        
            this.dealerTypeCombo.allowBlank=allowBlank;
    },
    funEnableDisablePANandPANStatus: function (isEdit, allowBlank) {
        //If TDS is not applicable on selected Vendor then remove mandatory mark else add it.
        if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isTDSApplicable) {
            this.PANNo.allowBlank = allowBlank;
            this.panStatusCombo.allowBlank = allowBlank;
            if (!this.isCustomer) {
                this.natureOfPayment.allowBlank = allowBlank;
                this.higherRate.allowBlank = allowBlank;
                if (allowBlank) {
                    this.higherRate.minValue = 0;
                } else {
                    this.higherRate.minValue = 1;
                }
            }
        }
    },
    funAllowBlankPANfun: function () {
        if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isTDSApplicable) {
            if(!Wtf.isEmpty(this.isCustomer) && !this.isCustomer) {
                //To Change the FieldLabel of "Higher Rate" at runtime
                var dd_textfield = Wtf.getCmp('higherRate' + this.id);
                if (dd_textfield != undefined && dd_textfield.el != undefined) {
                    var ct = dd_textfield.el.findParent('.x-form-item');
                }
            }
            if (!Wtf.isEmpty(this.panStatusCombo.getValue())) {
                this.PANNo.allowBlank = true;
                this.PANNo.clearInvalid();
                if (!Wtf.isEmpty(this.isCustomer) && !this.isCustomer) {
                    //If PAN status is "PANNOTAVBL" or "APPLIEDFOR", Higer Rate will be mandatory.
                    this.higherRate.allowBlank = false;
                    this.higherRate.minValue = 1;
                    if (ct != null) {
                        //To Change the FieldLabel of "Higher Rate" at runtime
                        ct.firstChild.innerHTML = WtfGlobal.getLocaleText("acc.vendor.tds.higherRate") + WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.customerInformation.higherRateInfo")) + "*:";
                    }
                }
            } else if(!Wtf.isEmpty(this.isCustomer) && !this.isCustomer) {
                //If PAN is given, Higer Rate will be non-mandatory.
                this.higherRate.allowBlank = true;
                this.higherRate.minValue = 0;
                if (ct != null) {
                    //To Change the FieldLabel of "Higher Rate" at runtime
                    ct.firstChild.innerHTML = WtfGlobal.getLocaleText("acc.vendor.tds.higherRate") + WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.customerInformation.higherRateInfo")) + ":";
                }
            }
        }
    },
    deducteeTypeOnSelect:function(a, b, c){      
        if (!Wtf.isEmpty(b)) {
            if (b.data.defaultMasterItem != Wtf.DeducteeType_Unknown) {
                if(b.data.typeofdeducteetype=="0"){// Corporate
                    this.deducteeCode.setValue("1")// Company
                }else{//Non-Corporate
                    this.deducteeCode.setValue("2")// Non-Company
                }
            }else{
                this.deducteeCode.setValue("");
            }
        }
    },
    funVATDealerType : function(){
    if(!this.isEdit){
        if((!Wtf.isEmpty(this.VATTINNo.getValue())|| !Wtf.isEmpty(this.CSTTINNo.getValue())) && this.dealerTypeCombo.getValue()==2){ //2 Unregistered Dealer
            this.dealerTypeCombo.reset();
        }
        if(Wtf.isEmpty(this.CSTTINNo.getValue()) && Wtf.isEmpty(this.VATTINNo.getValue()) && this.dealerTypeCombo.getValue()==1){ //1 Registered Dealer
            this.dealerTypeCombo.reset();
        }
    }else if(this.isEdit && !Wtf.isEmpty(this.record.data.isUsedInTransactions) && !this.record.data.isUsedInTransactions){
        if((!Wtf.isEmpty(this.VATTINNo.getValue())|| !Wtf.isEmpty(this.CSTTINNo.getValue())) && this.dealerTypeCombo.getValue()==2){ //2 Unregistered Dealer
            this.dealerTypeCombo.reset();
        }
        if(Wtf.isEmpty(this.CSTTINNo.getValue()) && Wtf.isEmpty(this.VATTINNo.getValue()) && this.dealerTypeCombo.getValue()==1){ //1 Registered Dealer
            this.dealerTypeCombo.reset();
        }
    }
    },
    enabledisableCForm:function(){
        if(!Wtf.isEmpty(this.interStateParty.getValue()) && this.interStateParty.getValue()!= true){
            this.cFormApplicable.disable();
            this.cFormApplicable.setValue(false);
        }else{
            this.cFormApplicable.enable();
        }
    },
    funVATNo : function(){
        if(Wtf.isEmpty(this.VATTINNo.getValue()) && this.dealerTypeCombo.getValue()!=2){
            this.dealerTypeCombo.reset();
        }
    },
    validateGSTIN: function () {
        var gstin = this.GSTIN.getValue();
        Wtf.Ajax.requestEx({
            method: 'POST',
            url: "ACCCustomerCMN/validateCustomerGSTIN.do",
            params: {
                gstin: gstin
            }
        }, this, function (response, request) {
            if (response.success) {
                var isValid = response.Valid;
                if (isValid == false) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.cust.gstinInvalid") + WtfGlobal.addLabelHelp('') + WtfGlobal.getLocaleText("acc.cust.gstinInvalid1")], 2);
                   // this.GSTIN.reset();
                    return;
                }
            }
        }, function (response) {
            WtfGlobal.resetAjaxTimeOut();
            Wtf.MessageBox.hide();
            var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            if (response.msg) {
                msg = response.msg;
            }
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        });

    },
    getCustOrVenCode : function(){
        if((this.isEdit || this.isCopy) && this.record.data.mapcustomervendor){
            Wtf.Ajax.requestEx({
                url:"ACCVendorCMN/getCustOrVenCode.do",
                params: {
                    accid : this.record.data.accid,
                    isCustomer : this.isCustomer
                }
            },this,
            function(response, request){
                if(this.createASCustOrVenCode){
                    if(this.parentStore != null || this.parentStore != undefined && (response.custOrVenAccCode !== "" && response.custOrVenAccCode != undefined )){
                        var index =  this.parentStore.find('code', response.custOrVenAccCode);
                        var rec = this.parentStore.getAt(index);
                        this.parentStore.remove(rec);
                    }

                    var index=this.sequenceFormatStoreVenCus.find('id',response.custVensequenceformatid);   
                    if(index!=-1){
                        this.sequenceFormatComboVenCus.setValue(response.custVensequenceformatid); 
                        this.sequenceFormatComboVenCus.disable();
                         this.createASCustOrVenCode.disable();
                     }
                   else {
                        this.sequenceFormatComboVenCus.setValue("NA"); 
                        this.sequenceFormatComboVenCus.disable();
                                if (!this.readOnly) {
                                    this.createASCustOrVenCode.enable();
                                }
                    }
                   if(!this.isCopy || this.sequenceFormatComboVenCus.getValue()!="NA"){
                        this.createASCustOrVenCode.setValue(response.custOrVenAccCode);
                    }
                }
            });
        }
    },
    
    createForm:function(){ 
         this.employeePrefrences = new Wtf.form.FieldSet({
            layout:'column',
            xtype:'fieldset',
            autoHeight:true,
            border:false,
            hidden:!(Wtf.account.companyAccountPref.activateLoanManagementFlag && this.isCustomer),
//            baseCls:'northFormFormat',
            cls:"visibleDisabled",
            title:WtfGlobal.getLocaleText("acc.customer.loan.title"), //Business / Employment History 
            //labelWidth:200,
            defaults:{
                border:false,
                anchor:'80%'
            },  
            items:[{
                layout:'form',
                labelWidth:100,
                columnWidth:0.50,
                items:[
                this.EmploymentStatus,
                this.employerName,
                this.companyAddress
                ]
            },{
                columnWidth:0.50,
                labelWidth:100,
                layout:'form',
                items:[
                this.occupationAndYears,
                this.monthlyIncome,
                this.noofActiveCreditLoans
                ]
            }]
        });        
        /*
         *  Preparing an array of items to push country specific fields inside it.
         */
        this.formItemsArray = [];

        this.formItemsArray.push(
                this.overseas,
                this.copyCustomer,
                this.sequenceFormatComboVenCus,
                this.createASCustOrVenCode,
                (this.copyCustomer.getValue()) ? this.businessPerson == "Customer" ? this.defaultCustomerAccount : this.defaultVendorAccount : this.businessPerson == "Customer" ? this.defaultVendorAccount : this.defaultCustomerAccount,
                this.customerVenodorAvailableToSalespersonAgent,
                this.venodorAvailableToAgentCheck,
                this.agentCombo,
                this.agentSingleSelectCombo,
                this.multiSalesPerson,
                this.salesPerson,
                this.mappingReceivedFrom,
                this.mappingPaidTo,
                this.other,
                this.bankaccountno,
                this.CreditTerm,
                this.Category
                );
        if (Wtf.account.companyAccountPref.countryid == Wtf.Country.PHILIPPINES) {
            this.formItemsArray.push(this.CustVenTypeCombo,this.TIN);
        }
        if (Wtf.Countryid == Wtf.Country.INDONESIA) {
            this.formItemsArray.push(this.CustVenTypeCombo);
        }        
        this.formItemsArray.push(
                this.Tax,
                this.limit,
                this.InterCompany,
                this.InterCompanyType,
                this.ProductMapping,
                this.addPreferredProduct,
                this.isActivateIBG,
                this.pmtMethod, //ERM-735
                this.lifoFifoCombo,
                this.selfBilledFromDate,
                this.selfBilledToDate,             
                this.GSTdetailsFieldSet


                );
        this.CustomerInfoForm=new Wtf.form.FormPanel({
        region: 'center',
        id:"CustomerInfoForm"+this.id,
        border:false,
        autoheight:true,
        //width:"100%",        
        width : 200,
        autoScroll:true,
            items:[{
            defaults:{border:false},
            baseCls:'northFormFormat',
                cls:"visibleDisabled",
            labelWidth:160,
            items:[{
            layout:'column',
            defaults:{border:false},
            items:[{
                layout:'form',
                columnWidth:0.50,
                items:[ {xtype:'hidden',name:'accid'},
                    this.Title,
                    this.sequenceFormatCombobox,
                    this.code,
                    this.name,
                    this.aliasname,
                    (this.copyCustomer.getValue())?this.businessPerson=="Customer"?this.defaultVendorAccount:this.defaultCustomerAccount:this.businessPerson=="Customer"?this.defaultCustomerAccount:this.defaultVendorAccount,
                    this.openingBal,
                    this.creationdate,
                    this.exchangeRate,
                    this.Currency,
                    this.isPermOrOnetime,
                    this.pricingBand,
                    this.UENNo,
                    this.minPriceValueForVendor,
                    this.npwp,
//                    this.VATTINNo,
//                    this.CSTTINNo,
//                    this.vatRegDate,
//                    this.PANNo,
//                    this.panStatusCombo,
                    this.SERVICENo,
//                    this.gtaApplicable,
//                    this.TANNo,
//                    this.ECCNo,
//                    this.IncomTaxNo,
                    this.issubFieldset,
                    this.deliveryDate,
                    this.deliveryTime,
                    this.vehicleNo,
                    this.driver,
                    this.taxIDNo,                    
                    this.Eligible1099,
                    this.companyRegNo,
                    this.gstRegNo,
                    this.rmcdApprovalNo,
                    this.gstVerifiedDate,
                    this.isTDSapplicableonvendor
//                    this.isVATCSTapplicableOnCompanyFieldSet
                 ]
            },{
                layout:'form',
                columnWidth:0.50,
                bodyStyle:"padding-left:10px",
                items:this.formItemsArray
            }]
            },this.employeePrefrences,this.tagsFieldset]
            }]
        });
        this.exchangeRate.disable();
         this.pan=new Wtf.Panel({
            width:175,
            layout:'fit',
            region:'east',
            style:"padding:10px 13px 30px 0px;",
            title:this.isCustomer?WtfGlobal.getLocaleText("acc.cust.exist"):WtfGlobal.getLocaleText("acc.ven.exist")
        });
        
    },
    searchText:function(pan,cmpName,cmpRecordField,isCustomer,businessPerson){
        document.getElementById(cmpName).onkeyup = function () {
            this.text = Wtf.getCmp(cmpName).getValue();
            if (this.text != "" && this.text.length > 2) {
                var url, params;
                if (businessPerson == "Customer") {
                    url =isCustomer?"ACC"+ businessPerson +"/get" + businessPerson +"sIdNameForCombo.do":"ACCAccountCMN/getAccountsForCombo.do";
                    params = {
                        mode: 2,
                        ignoreGLAccounts: true,
                        ignoreCashAccounts: true,
                        ignoreBankAccounts: true,
                        ignoreGSTAccounts: true,
                        ignorecustomers: isCustomer ? null : true,
                        ignorevendors: isCustomer ? true : null,
                        start: 0,
                        limit: 23,
                        cmpRecordField:cmpRecordField,
                        searchstartwith: this.text,
                        receivableAccFlag: isCustomer ? true : null
            }
                    
                }else{
                    url =isCustomer?"ACCAccountCMN/getAccountsForCombo.do":"ACC"+ businessPerson +"/get" + businessPerson +"sIdNameForCombo.do";
                    params = {
                        mode: 2,
                        ignoreGLAccounts: true,
                        ignoreCashAccounts: true,
                        ignoreBankAccounts: true,
                        ignoreGSTAccounts: true,
                        ignorecustomers: !isCustomer ? true:null ,
                        ignorevendors: !isCustomer ? null : true,
                        start: 0,
                        limit: 23,
                        cmpRecordField:cmpRecordField,
                        searchstartwith: this.text,
                        receivableAccFlag: !isCustomer ? null : true
                    }
                }
                var delayInMilliseconds = 2000; //1 second

                setTimeout(function () {
                    Wtf.Ajax.requestEx({
                        method: 'POST',
                        url: url,
                        params: params
                    }, this,
                            function (response) {
                                //var res = response[0];
                                if (response.success) {

                                    if (cmpRecordField == "parentname")
                {
                                        pan.setTitle((isCustomer ? WtfGlobal.getLocaleText("acc.cust.exist") : WtfGlobal.getLocaleText("acc.ven.exist")) + WtfGlobal.getLocaleText("acc.userAdmin.name"));

                                    } else if (cmpRecordField == "code")
                {
                                        pan.setTitle((isCustomer ? WtfGlobal.getLocaleText("acc.cust.exist") : WtfGlobal.getLocaleText("acc.ven.exist")) + WtfGlobal.getLocaleText("acc.field.Code"));

                }

                                    this.len = response.data.length;
                                    if (this.len == 0 && this.tpl != undefined) {
                                        this.tpl.overwrite(pan.body, "");
                }
                                    for (var i = 0; i < this.len; i++) {
                                        if (cmpRecordField == "parentname")
                                        {
                                            this.presentAcc = response.data[i].accname;
                    }
                                        else if (cmpRecordField == "code")
                                        {
                                            this.presentAcc = response.data[i].acccode;
                                        }
                                        this.tdata = {
                                            paname: this.presentAcc
                                        }
                                        this.tpl = new Wtf.Template('<font size=2> <p>{paname}</p></font>');
                                        if (i == 0)
                                            this.tpl.overwrite(pan.body, this.tdata);
                    else
                                            this.tpl.append(pan.body, this.tdata);

                    pan.show();
                }

            }
                            },
                            function (response) {
                                var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";        
                                Wtf.MessageBox.alert('Status', 'Message:', msg);

                            });
                }, delayInMilliseconds);
                
            } else {
                if (this.tpl != undefined) {
                    this.tpl.overwrite(pan.body, "");
                }
            }
        };
    },
    sendMail:function(){
        if(!this.isCustomer){
            if(this.record.data.taxidnumber!=""){
                 Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"),  //'Warning',
                    msg: WtfGlobal.getLocaleText("acc.ven.msg2") +   //"You have already filled TAX ID NUMBER for this Vendor."+
                    WtfGlobal.getLocaleText("acc.ven.msg3") +   //"Requesting TAX ID NUMBER from Vendor will result in overriding the existing TAX ID NUMBER by the one filled by Vendor."
                    WtfGlobal.getLocaleText("acc.ven.msg4"),   //+"Do You want to continue?",
                    buttons: Wtf.MessageBox.YESNO,
                    animEl: 'mb9',
                    fn:function(btn){
                        if(btn!="yes")return;
                        callTaxEmailWin("vendoremailwin",this.record,true);
                    },
                    scope:this,
                    icon: Wtf.MessageBox.QUESTION
                },this);
            }else{
                callTaxEmailWin("vendoremailwin",this.record,true);
            }
        }
    },
    
    onDateChange:function(a,val,oldval){          
        this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.creationdate.getValue())}});
    },
    
    setAddress:function(o,newval,oldval){
            var val1=newval?this.address.getValue():"";
            var val2=newval?this.address2.getValue():"";
            var val3=newval?this.address3.getValue():"";
            if(this.isCustomer){
                this.SAddress.setValue(val1);
                this.SAddress2.setValue(val2);
                this.SAddress3.setValue(val3);
            }                
    },
    checkParent:function(){
        this.parentAccount.allowBlank=false;
        if(this.parentStore.getCount()==0 && (this.parentStore.isLoaded == true || this.isEdit || this.isCopy))
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),this.isCustomer?WtfGlobal.getLocaleText("acc.cust.msg3"):WtfGlobal.getLocaleText("acc.ven.msg5")],2);     //"Alert","No Parent "+this.businessPerson+" exist"
    },
    setPersonTitle:function(){
        if(Wtf.TitleStore.getCount()>0){
            if(this.isEdit){
                this.Title.setValue(this.record.data.title);
            }
        }
//        Wtf.TitleStore.un("load", this.setPersonTitle, this);
    },
    setCategory: function(){
        if(this.isEdit){
            if(this.businessPerson=="Customer") {
                if(Wtf.CustomerCategoryStore.getCount()>0){
                    this.Category.setValue(this.record.data.categoryid);
                }
            } else {
                if(Wtf.VendorCategoryStore.getCount()>0){
                    this.Category.setValue(this.record.data.categoryid);
                }
            }
        }
        Wtf.CustomerCategoryStore.un("load", this.setCategory, this);
        Wtf.VendorCategoryStore.un("load", this.setCategory, this);
    },
    setInterCompanyType: function(){
        if(this.isEdit){            
            if(Wtf.InterCompanyTypeStore.getCount()>0){
                this.InterCompanyType.setValue(this.record.data.intercompanytypeid);
            }
        }
        Wtf.InterCompanyTypeStore.un("load", this.setInterCompanyType, this);
    },

    hideLoading:function(val){
        WtfGlobal.resetAjaxTimeOut();
        if(!val){
            this.fireEvent("loadingcomplete",this);
            this.name.focus();      //To set focus on Name on form load.
        }
    },
    setPDM:function(store){
        if(Wtf.termds.getCount()>0){
            if(this.isEdit || this.isCopy){
                this.CreditTerm.setValue(this.record.data.termid);
            } else {
                var defaultCreditTerm = '';
                for(var i=0 ; i<Wtf.termds.getCount() ; i++){
                    if(Wtf.termds.getAt(i).get('isdefaultcreditterm')==true){
                        defaultCreditTerm = Wtf.termds.getAt(i).get('termid');
                        break;
                    }
                }
                if(this.CreditTerm){
                    this.CreditTerm.setValue(defaultCreditTerm);
                }
            }
        }
        if(store) {
            Wtf.termds.un("load", this.setPDM, this);
        }
    },
    addMaster:function(id,store){
        addMasterItemWindow(id);
        Wtf.getCmp('masterconfiguration').on('update', function(){
            store.reload();
        }, this);
    },
    
addOpeningBalance:function(){
    callOpeningBalaneWindow(this.record, this.isCustomer);
},
    
    closeForm:function(){
        if(this.isClosable!==true){
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.warning"),  //'Warning',
                msg: WtfGlobal.getLocaleText("acc.common.msg2")+"<br>"+WtfGlobal.getLocaleText("acc.common.msg3")+"</br>",  //"The data you have entered is unsaved.<br>Are you sure you want to close the window?",
                buttons: Wtf.MessageBox.YESNO,
                animEl: 'mb9',
                fn:function(btn){
                    if(btn!="yes")return;

                     this.fireEvent('cancel',this);
                     this.close();
                },
                scope:this,
                icon: Wtf.MessageBox.QUESTION
            });
        } else {
            this.fireEvent('cancel',this);
            this.close();
        }
    },
     checkDuplicateCode:function(o,newval,oldval){

        var FIND = this.code.getValue().trim().toLowerCase();
        FIND =FIND.replace(/\s+/g, '');        
         var index=this.parentStore.findBy(function(record) {
                    var recCode=record.data['code'].trim().toLowerCase();
                    if(recCode !="" && recCode==FIND)
                        return true;
                    else
                        return false;
                });        
        if(index>=0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.ThiscodealreadyexistsPlease")],2);
            this.code.setValue(oldval);
            return;
        }
    },
     getDetails:function(){
        var commentlist = getDocsAndCommentList('', this.moduleid,this.id,undefined,this.isCustomer?'Customer':'Vendor',undefined,"email",'leadownerid',this.contactsPermission,0,this.recordid);
    },
//     checkDuplicateCustOrVenCode:function(o,newval,oldval){
//
//        var FIND = this.createASCustOrVenCode.getValue().trim().toLowerCase();
//        FIND =FIND.replace(/\s+/g, '');        
//         var index=this.AccountStoreForCopyCustomer.findBy(function(record) {
//                    var recCode=record.data['code'].trim().toLowerCase();
//                    if(recCode !="" && recCode==FIND)
//                        return true;
//                    else
//                        return false;
//                });        
//        if(index>=0){
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.ThiscodealreadyexistsPlease")],2);
//            this.createASCustOrVenCode.setValue(oldval);
//            return;
//        }
//    },

//    checkDuplicateEmail:function(o,newval,oldval){
//      if(this.email.getValue() != ""){
//    	this.parentStore.clearFilter(true)
//	    var FIND = this.email.getValue().trim().toLowerCase();
//	    FIND =FIND.replace(/\s+/g, '');
//	    var index=this.parentStore.findBy( function(rec){
//	    var parentname=rec.data['email'].trim().toLowerCase();
//	    parentname=parentname.replace(/\s+/g, '');
//	    if(parentname===FIND)
//	        return true;
//	    else
//	        return false
//	    })
//	    if(index>=0){
//	        if(this.isCustomer)
//	            WtfComMsgBox(43,2);
//	        else
//	            WtfComMsgBox(44,2);
//	        this.email.setValue(oldval)
//	     }
//      }
//    },
    confirmBeforeSave: function () {
        if (Wtf.account.companyAccountPref.propagateToChildCompanies) {

            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.confirm"),
                msg: WtfGlobal.getLocaleText({key: "acc.savecustomer.propagate.confirmmessage", params: [this.businessPerson == "Customer" ? "customer" : "vendor"]}),
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
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.confirm"),
                msg: WtfGlobal.getLocaleText("acc.invoice.msg7"),
                buttons: Wtf.MessageBox.YESNO,
                icon: Wtf.MessageBox.QUESTION,
                width: 300,
                scope: {
                    scopeObject: this
                },
                fn: function (btn) {
                    if (btn != "yes") {
                        return;
                    }
                    this.scopeObject.saveForm();
                }
            }, this);
            
        }
    },
    /**
     * GST Details Changes for Customer vendor. IF customer/ Vendor used in Transaction then show confirm message.
     * GST details - GSTIN Registration Type , Customer/ Vendor Type
     * */
    validateGSTINNumber : function(){
        if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && WtfGlobal.GSTApplicableForCompany() == Wtf.GSTStatus.NEW) {
            var GSTIndex = this.GSTINRegistrationTypeCombo.store.find('id', this.GSTINRegistrationTypeCombo.getValue());
            if (GSTIndex != -1) {
                var record = this.GSTINRegistrationTypeCombo.store.getAt(GSTIndex);
                if (record != undefined && record.data!=undefined && record.data.defaultMasterItem !=undefined && record.data.defaultMasterItem !='') {
                    if (record.data.defaultMasterItem == Wtf.GSTRegMasterDefaultID.Unregistered || record.data.defaultMasterItem == Wtf.GSTRegMasterDefaultID.Consumer) {
                        this.GSTIN.allowBlank = true;
                        this.GSTIN.setDisabled(true); // Disable GSTIN number for Consumer and Unregistered Customer/ Vendor
                        this.GSTIN.reset();
                    } else if(!this.isEdit){
                        this.GSTIN.allowBlank = false;
                        this.GSTIN.setDisabled(false);// Enable GSTIN number for Consumer and Unregistered Customer/ Vendor
                    }
                    this.GSTIN.validate();
                }else{
                    this.GSTIN.allowBlank = true;
                }
            }
        } 
    },
    /**
     * Validation of Customer/ Vendor type on  GST registration type 
     * Customer/Vendor Type, GST Registration type )
     * 
     * GSTReg. Type 	        GSTIN 	        Customer Type 
     * -----------------------------------------------------------------------------------
     * Composition 	        Required 	NA
     * Registered 	        Required 	NA  SEZ (WPAY),SEZ (WOPAY)
     * Unregistered 	        Not Required 	NA, Export (WPAY), Export (WOPAY)
     * 
     * GSTReg. Type 	        GSTIN 	        Vendor Type 	
     * -----------------------------------------------------------------------------------
     * Composition 	        Required 	NA
     * Registered 	        Required 	NA, SEZ (WPAY),SEZ (WOPAY)
     * Unregistered 	        Not Required 	NA, Import
     * 
     * Method params - isValidateFromGSTHistoryWindow, editObj, GSTINRegistrationTypeId - passed in GST History window only 
     * @param {type} combo
     * @param {type} record
     * @param {type} index
     */
    validateCustomerVendorType : function(combo, CustVendTypeRecord, index , isValidateFromGSTHistoryWindow, editObj , GSTINRegistrationTypeId){
        if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
            this.validateSEZDates();
            var GSTIndex = this.GSTINRegistrationTypeCombo.store.find('id', (isValidateFromGSTHistoryWindow!=undefined && isValidateFromGSTHistoryWindow ? GSTINRegistrationTypeId : this.GSTINRegistrationTypeCombo.getValue()));
            if (GSTIndex != -1) {
                var record = this.GSTINRegistrationTypeCombo.store.getAt(GSTIndex);
                if (record != undefined && record.data!=undefined && record.data.defaultMasterItem !=undefined && record.data.defaultMasterItem !='') {
                      /**
                       * Validate GST Registration for Customer/ Vendor if type Composition 
                       */
                      if(record.data.defaultMasterItem == Wtf.GSTRegMasterDefaultID.Composition 
                              && !(CustVendTypeRecord.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.NA)){
                          if(isValidateFromGSTHistoryWindow){
                             this.isValidateFromGSTHistoryWindow(combo, CustVendTypeRecord, isValidateFromGSTHistoryWindow, editObj)
                          }
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText({key: "acc.india.vecdorcustomer.column.type", params: (this.isCustomer ? ['Customer','NA'] : ['Vendor','NA'])})],2);
                          combo.clearValue();
                      }
                     /**
                      * Validate GST Registration for Customer/ Vendor if type Registered 
                      */
                      if(record.data.defaultMasterItem == Wtf.GSTRegMasterDefaultID.Regular 
                              && !(CustVendTypeRecord.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.NA 
                              || CustVendTypeRecord.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.SEZ 
                              || CustVendTypeRecord.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.SEZWOPAY)){
                          if(isValidateFromGSTHistoryWindow){
                             this.isValidateFromGSTHistoryWindow(combo, CustVendTypeRecord, isValidateFromGSTHistoryWindow, editObj)
                          }
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText({key: "acc.india.vecdorcustomer.column.type", params: (this.isCustomer ? ['Customer','NA, SEZ (WPAY),SEZ (WOPAY)'] : ['Vendor','NA, SEZ (WPAY)'])})],2);
                          combo.clearValue();
                      }
                    /**
                     * Validate GST Registration for Customer/ Vendor if type Unregistered 
                     */
                    if(record.data.defaultMasterItem == Wtf.GSTRegMasterDefaultID.Unregistered 
                              && !(CustVendTypeRecord.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.NA 
                              || CustVendTypeRecord.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.Import 
                              || CustVendTypeRecord.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.Export
                              || CustVendTypeRecord.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.ExportWOPAY)){
                       if(isValidateFromGSTHistoryWindow){
                            this.isValidateFromGSTHistoryWindow(combo, CustVendTypeRecord, isValidateFromGSTHistoryWindow, editObj)
                       }
                       WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText({key: "acc.india.vecdorcustomer.column.type", params: (this.isCustomer ? ['Customer','NA,Export (WPAY), Export (WOPAY)'] : ['Vendor','NA, Import'])})],2);
                       combo.clearValue();
                    }
                }
            }
            if (isValidateFromGSTHistoryWindow == undefined) {
                this.changeAddressFields();
            }
        }
    },
    /**
     * IF GST History window grid data not valid then reset to previous data
     * @param {type} combo
     * @param {type} CustVendTypeRecord
     * @param {type} isValidateFromGSTHistoryWindow
     * @param {type} editObj
     * @returns {undefined}
     */
    isValidateFromGSTHistoryWindow: function(combo, CustVendTypeRecord, isValidateFromGSTHistoryWindow, editObj){
        if(isValidateFromGSTHistoryWindow && editObj!=undefined){
           // this.CustomerVendorTypeCombo.setValue(editObj.originalValue);
            if(editObj.record){
                editObj.record.set('CustomerVendorTypeId', editObj.originalValue);
            }
        }
    },
    /*
     * 
     * To Hide/Show State as combo when Customer / Vendor type dropdown is selected
     * Hide State as dropdown - Indian country and Customer type = Export (WPAY),Export (WOPAY) and Vendor type= Import
     */
    changeAddressFields: function () {
        var tabid = this.isCustomer ? "contactDetailCustomerTab" : "contactDetailVendorTab";

        if (this.isEdit != undefined && this.isEdit) {
            tabid = "edit-" + tabid;
        } else if (this.isCopy != undefined && this.isCopy) {
            tabid = "copy-" + tabid;
        }
        if (Wtf.getCmp(tabid) != undefined) {
            var custVentypeIndex = this.CustomerVendorTypeCombo.store.find('id', this.CustomerVendorTypeCombo.getValue());
            if (custVentypeIndex != -1) {
                var custVenType = this.CustomerVendorTypeCombo.store.getAt(custVentypeIndex);

                if (custVenType.data.defaultMasterItem== Wtf.GSTCUSTVENTYPE.Export|| custVenType.data.defaultMasterItem== Wtf.GSTCUSTVENTYPE.ExportWOPAY|| custVenType.data.defaultMasterItem== Wtf.GSTCUSTVENTYPE.Import) {
                    if(Wtf.getCmp(tabid).billingStateCombo){
                        WtfGlobal.hideFormElement(Wtf.getCmp(tabid).billingStateCombo);
                        Wtf.getCmp(tabid).billingStateCombo.allowBlank=true;
                    }
                    if(Wtf.getCmp(tabid).billingState){
                        WtfGlobal.showFormElement(Wtf.getCmp(tabid).billingState);
                    }
                    if(Wtf.getCmp(tabid).shippingStateCombo){
                        WtfGlobal.hideFormElement(Wtf.getCmp(tabid).shippingStateCombo);
                        Wtf.getCmp(tabid).shippingStateCombo.allowBlank=true;
                    }
                    if(Wtf.getCmp(tabid).shippingState){
                        WtfGlobal.showFormElement(Wtf.getCmp(tabid).shippingState);
                    }

                } else {
                    if(Wtf.getCmp(tabid).billingStateCombo){
                        WtfGlobal.showFormElement(Wtf.getCmp(tabid).billingStateCombo);
                    }
                    if(Wtf.getCmp(tabid).billingState){
                        WtfGlobal.hideFormElement(Wtf.getCmp(tabid).billingState);
                        Wtf.getCmp(tabid).billingState.allowBlank=true;
                    }
                    if(Wtf.getCmp(tabid).shippingStateCombo){
                        WtfGlobal.showFormElement(Wtf.getCmp(tabid).shippingStateCombo);
                    }
                    if(Wtf.getCmp(tabid).shippingState){
                        WtfGlobal.hideFormElement(Wtf.getCmp(tabid).shippingState);
                        Wtf.getCmp(tabid).shippingState.allowBlank=true;
                    }
                }
            }
        }
    },
    validateSEZDates: function () {
        var GSTIndex = this.CustomerVendorTypeCombo.store.find('id', this.CustomerVendorTypeCombo.getValue());
        if (GSTIndex != -1) {
            var record = this.CustomerVendorTypeCombo.store.getAt(GSTIndex);
            // Enable Disable sez Start and End dates on Customer/ Vendor Basis.
            if (record.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.SEZ) {
                this.sezFromDate.setDisabled(false);
                this.sezToDate.setDisabled(false);
            } else {
                this.sezFromDate.reset();
                this.sezToDate.reset();
                this.sezFromDate.setDisabled(true);
                this.sezToDate.setDisabled(true);
            }
        }else{
            this.sezFromDate.reset();
            this.sezToDate.reset();
            this.sezFromDate.setDisabled(true);
            this.sezToDate.setDisabled(true);
        }
    },
    saveForm:function(){
        this.name.setValue(this.name.getValue().trim());
        this.code.setValue(this.code.getValue().trim());
        this.createASCustOrVenCode.setValue(this.createASCustOrVenCode.getValue().trim());
        this.validateGSTINNumber(); // Validate GSTIn number : Conditional validation
        var isValid = this.CustomerInfoForm.getForm().isValid();
        var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
            
        if(!this.exchangeRate.isValid() || this.exchangeRate.getValue()==""){
            isValid = false;
        }
        if (isValid && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDONESIA && !this.npwp.isValid()) {
                return;
        }
        if(!isValid || !isValidCustomFields){
            WtfComMsgBox(2,2);
        }else{
            this.msg= WtfComMsgBox(27,4,true);
            if(!this.isEdit){
                var notAccessAccountsList="";
                var hasAccessFlag=false;
                if(this.businessPerson=="Customer"){
                    if(!checkForAccountActivate(this.defaultCustomerAccountStore,this.defaultCustomerAccount.getValue(),"accountid")){
                        hasAccessFlag=true;
                        notAccessAccountsList = notAccessAccountsList + WtfGlobal.getLocaleText("acc.customer.CustomerAccount")+ ", ";
                    }
                    if(this.copyCustomer.getValue()){
                        if(!checkForAccountActivate(this.defaultVendorAccountStore,this.defaultVendorAccount.getValue(),"accountid")){
                            hasAccessFlag=true;
                            notAccessAccountsList = notAccessAccountsList + WtfGlobal.getLocaleText("acc.customer.VendorAccount")+ ", ";
                        }
                    }
                }else{
                    if(!checkForAccountActivate(this.defaultVendorAccountStore,this.defaultVendorAccount.getValue(),"accountid")){
                        hasAccessFlag=true;
                        notAccessAccountsList = notAccessAccountsList + WtfGlobal.getLocaleText("acc.customer.VendorAccount")+ ", ";
                    }
                    if(this.copyCustomer.getValue()){
                        if(!checkForAccountActivate(this.defaultCustomerAccountStore,this.defaultCustomerAccount.getValue(),"accountid")){
                            hasAccessFlag=true;
                            notAccessAccountsList = notAccessAccountsList + WtfGlobal.getLocaleText("acc.customer.CustomerAccount")+ ", ";
                        }
                    }
                }
                if(notAccessAccountsList!=""){
                    notAccessAccountsList = notAccessAccountsList.substring(0, notAccessAccountsList.length-2);
                }
                if(hasAccessFlag){
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                        msg: WtfGlobal.getLocaleText("acc.field.Pleaseselectactivatedaccount")+notAccessAccountsList+".",
                        width:370,
                        buttons: Wtf.MessageBox.OK,
                        icon: Wtf.MessageBox.WARNING,
                        scope: this
                    });
                    return;
                }
            }
            
            var custFieldArr=this.tagsFieldset.createFieldValuesArray();
            var rec=this.CustomerInfoForm.getForm().getValues();
            rec.currencyid=this.Currency.getValue();
            rec.creationDate=WtfGlobal.convertToGenericDate(this.creationdate.getValue());
            rec.category= this.Category.getValue();
            rec.title=this.Title.getValue();
//            rec.intercompanyflag = this.InterCompany.getValue();
            
            //  Check valid Products Selected or Not
            if(!this.isEdit){
                rec.accid="";
            }
            var isInvalidProductsSelected = WtfGlobal.isInvalidProductsSelected(this.ProductMapping);
            if(isInvalidProductsSelected){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.select.validproduct")],3);
                return; 
            }
            
            rec.mappingcusaccid=this.defaultCustomerAccount.getValue();
            if(this.businessPerson=="Customer"){
                 rec.accountid=this.defaultCustomerAccount.getValue();
            }else{
                 rec.accountid=this.defaultVendorAccount.getValue();
            }
            rec.mappingvenaccid=this.defaultVendorAccount.getValue();
            rec.productmapping=this.ProductMapping.getValue();
            rec.customJSONString = this.ProductMapping.customJSONString ;
            rec.intercompanytype = this.InterCompanyType.getValue();
            rec.mode=1;
            rec.parentname=this.parentAccount.getRawValue();
            rec.parentid=this.parentAccount.getValue();
            if (custFieldArr.length > 0){
                    rec.customfield = JSON.stringify(custFieldArr);
            }
            rec.mapsalesperson= this.salesPerson.getValue();
            rec.mapreceivedfrom= this.mappingReceivedFrom.getValue();
            rec.mappingPaidTo= this.mappingPaidTo.getValue();
            rec.GSTINRegistrationTypeId= this.GSTINRegistrationTypeCombo.getValue();
            rec.CustomerVendorTypeId= this.CustomerVendorTypeCombo.getValue();
            if(Wtf.account.companyAccountPref.countryid == Wtf.Country.PHILIPPINES){
                rec.gstin=this.TIN.getValue();
            }
            if (Wtf.account.companyAccountPref.countryid == Wtf.Country.PHILIPPINES || Wtf.Countryid == Wtf.Country.INDONESIA) {
                rec.CustomerVendorTypeId= this.CustVenTypeCombo.getValue();
            }
            if(WtfGlobal.isIndiaCountryAndGSTApplied()){
            if(this.gstapplieddate==undefined){
                this.gstapplieddate=this.creationdate.getValue();
            }
            rec.gstin=this.GSTIN.getValue();            
            rec.isgstdetailsupdated=this.isgstdetailsupdated;
            rec.gstapplieddate=WtfGlobal.convertToGenericDate(this.gstapplieddate);
            }
            rec.mappingSalesPersonId=this.salesPerson.getValue();
            rec.sezfromdate = WtfGlobal.convertToGenericDate(this.sezFromDate.getValue());
            rec.seztodate = WtfGlobal.convertToGenericDate(this.sezToDate.getValue());
            //If default sales person is selected then it is passed through available multi selected sales person.
//            rec.mapmultisalesperson= this.salesPerson.getValue()!= "" ?this.multiSalesPerson.getValue() + ","+this.salesPerson.getValue():this.multiSalesPerson.getValue();
            rec.iscutomeravailableonlytosalespersons=this.customerVenodorAvailableToSalespersonAgent.getValue();
            rec.isvendoravailabletoagent=this.venodorAvailableToAgentCheck.getValue();
            
            rec.defaultagentmapping=this.agentSingleSelectCombo.getValue();
            rec.agentmapping=this.agentCombo.getValue();
            rec.mapmultisalesperson= this.multiSalesPerson.getValue();
            rec.taxeligible=this.Eligible1099.getValue()//=='on'?true:false);
            rec.debitType=this.balTypeEditor.getValue();
            rec.country=this.country.getValue();
            rec.companycountry= Wtf.account.companyAccountPref.countryid;
            rec.taxId=this.Tax.getValue();
            /*
             * In copy case, deactivated tax not shown.Hence, empty taxid set in record.          
             */
            if (rec.taxId != '' && this.isCopy) {
                var taxActivatedRec = WtfGlobal.searchRecord(this.taxStore, rec.taxId, "prtaxid");
                if (taxActivatedRec == null || taxActivatedRec == undefined || taxActivatedRec == "") {
                    rec.taxId = '';
                }
            }            
            rec.sequenceformat=this.sequenceFormatCombobox.getValue();
            rec.acccode=this.code.getValue();
            rec.aliasname=this.aliasname.getValue();
            rec.custorvenacccode=this.createASCustOrVenCode.getValue();
            rec.from=this.businessPerson=="Customer"?Wtf.autoNum.customer:Wtf.autoNum.vendor;
            rec.fromVenCus=this.businessPerson=="Customer"?Wtf.autoNum.vendor:Wtf.autoNum.customer;
            rec.sequenceformatvencus=this.sequenceFormatComboVenCus.getValue();
            rec.isEdit=this.isEdit;
            rec.deducteeTypeId = this.deducteeType != undefined?this.deducteeType.getValue():"";
            rec.deducteeCode = this.deducteeCode != undefined?this.deducteeCode.getValue():"";
            if(this.residentialstatus0.getValue()==true){
                rec.residentialstatus=0;
            }
            if(this.residentialstatus1.getValue()==true){
                rec.residentialstatus=1;
            }
            rec.panStatusId = this.panStatusCombo != undefined?this.panStatusCombo.getValue():"";
            rec.manufacturerType = this.manufacturerTypeCombo != undefined?this.manufacturerTypeCombo.getValue():"";
            rec.dealertype = this.dealerTypeCombo != undefined?this.dealerTypeCombo.getValue():"";
            rec.vendorbranch = this.vendorBranchCombo?this.vendorBranchCombo.getValue():"";
            rec.defaultnatureofpurchase = this.defaultNatureOfPurchase != undefined?this.defaultNatureOfPurchase.getValue():"";
            rec.vatregdate=WtfGlobal.convertToGenericDate(this.vatRegDate.getValue());
            rec.cstregdate=WtfGlobal.convertToGenericDate(this.CSTRegDate.getValue());
            if(this.ibgReceivingDetailsArr != undefined && this.ibgReceivingDetailsArr != [] && this.isEdit){
                if(!this.isActivateIBG.collapsed){
                    this.gridRec = new Wtf.data.Record.create([
                    {
                        name:'ibgId'
                    },{
                        name:'receivingBankDetailId'  
                    },{
                        name:'receivingBankCode'
                    },{
                        name:'receivingBankName'
                    },{
                        name:'receivingBranchCode'
                    },{
                        name:'receivingAccountNumber'
                    }, {
                        name:'receivingAccountName'
                    },{name:'collectionAccNo'
                    },{name:'collectionAccName'
                    },{name:'giroBICCode'
                    },{name:'refNumber'
                    },{name:'cimbReceivingBankDetailId'}
                ]);

                    var ibgDetailslen = 0;
                    if(this.ibgReceivingDetails && this.ibgReceivingDetails !=''){
                        var parsed = JSON.parse(this.ibgReceivingDetails);
                        this.ibgReceivingDetailsArr=new Array(parsed);
                    }
                    if(this.ibgReceivingDetailsArr!=undefined){
                        if(this.ibgReceivingDetailsArr.length>0){
                            ibgDetailslen = this.ibgReceivingDetailsArr.length;
                        }
                        for(var i=0;i<ibgDetailslen;i++){
                            this.ibgReceivingDetails=new this.gridRec({
                                ibgId: this.ibgReceivingDetailsArr[0].ibgId!=undefined?this.ibgReceivingDetailsArr[0].ibgId:"",
                                receivingBankDetailId: this.ibgReceivingDetailsArr[0].receivingBankDetailId!=undefined?this.ibgReceivingDetailsArr[0].receivingBankDetailId:"",
                                cimbReceivingBankDetailId: this.ibgReceivingDetailsArr[0].cimbReceivingBankDetailId!=undefined?this.ibgReceivingDetailsArr[0].cimbReceivingBankDetailId:"",
                                receivingBankCode:this.ibgReceivingDetailsArr[0].receivingBankCode!=undefined?this.ibgReceivingDetailsArr[0].receivingBankCode:"",
                                receivingBankName:this.ibgReceivingDetailsArr[0].receivingBankName!=undefined?this.ibgReceivingDetailsArr[0].receivingBankName:"",
                                receivingBranchCode: this.ibgReceivingDetailsArr[0].receivingBranchCode!=undefined?this.ibgReceivingDetailsArr[0].receivingBranchCode:"",
                                receivingAccountNumber:this.ibgReceivingDetailsArr[0].receivingAccountNumber!=undefined?this.ibgReceivingDetailsArr[0].receivingAccountNumber:"",
                                collectionAccNo:this.ibgReceivingDetailsArr[0].collectionAccNo!=undefined?this.ibgReceivingDetailsArr[0].collectionAccNo:"",
                                collectionAccName:this.ibgReceivingDetailsArr[0].collectionAccName!=undefined?this.ibgReceivingDetailsArr[0].collectionAccName:"",
                                giroBICCode:this.ibgReceivingDetailsArr[0].giroBICCode!=undefined?this.ibgReceivingDetailsArr[0].giroBICCode:"",
                                refNumber:this.ibgReceivingDetailsArr[0].refNumber!=undefined?this.ibgReceivingDetailsArr[0].refNumber:""
                            });
                        }   
                    }
                }
            }
            if(this.ibgReceivingDetails != undefined && this.ibgReceivingDetails != ""){
                if(!this.isActivateIBG.collapsed){
                    rec.activateIBG = true;
                    rec.ibgReceivingDetails = this.ibgReceivingDetails;
                    rec.CIMBbank = (this.CIMBbank);
                    rec.DBSbank = (this.DBSbank);
                }
            }else{
                rec.activateIBG = false;
                rec.ibgReceivingDetails = "";
            }
            
            rec.paymentCriteria = this.lifoFifoCombo.getValue();
            rec.pricingBand = this.pricingBand.getValue();
            rec.minpricevalueforvendor = this.minPriceValueForVendor.getValue();
            rec.deliveryDate = this.deliveryDate.getValue();
            rec.deliveryTime = this.deliveryTime.getValue();
            rec.vehicleNo = this.vehicleNo.getValue();
            rec.driver = this.driver.getValue();
            rec.paymentmethod = this.pmtMethod.getValue();               // ERM-735
            rec.isPermOrOnetime=this.isPermOrOnetime.getValue();
            
            rec.employmentStatus=this.EmploymentStatus.getValue().trim();
            rec.employerName=this.employerName.getValue().trim();
            rec.companyAddress=this.companyAddress.getValue().trim();
            rec.occupationAndYears=this.occupationAndYears.getValue().trim();
            rec.monthlyIncome=this.monthlyIncome.getValue().trim();
            rec.noofActiveCreditLoans=this.noofActiveCreditLoans.getValue().trim();
            rec.companyRegistrationNumber=this.companyRegNo.getValue().trim();
            rec.gstRegistrationNumber=this.gstRegNo.getValue().trim();
            rec.rmcdApprovalNumber=this.rmcdApprovalNo.getValue().trim();
            rec.selfBilledFromDate=WtfGlobal.convertToGenericDate(this.selfBilledFromDate.getValue());
            rec.gstVerifiedDate=WtfGlobal.convertToGenericDate(this.gstVerifiedDate.getValue());
            rec.selfBilledToDate=WtfGlobal.convertToGenericDate(this.selfBilledToDate.getValue());
            rec.ispropagatetochildcompanyflag=this.ispropagatetochildcompanyflag;
            rec.creditLimit= this.limit.getValue();
            if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA) {
                rec.natureofpayment=this.natureOfPayment?this.natureOfPayment.getValue():"";
                rec.panno=this.PANNo? this.PANNo.getValue():"";
                rec.higherRate=this.higherRate? this.higherRate.getValue():"";
                rec.DTAAApplicable = !Wtf.isEmpty(this.DTAAApplicable) ? this.DTAAApplicable.getValue() : "";
                rec.DTAAFromDate = !Wtf.isEmpty(this.DTAAFromDate) ? WtfGlobal.convertToGenericDate(this.DTAAFromDate.getValue()) : "";
                rec.DTAAToDate = !Wtf.isEmpty(this.DTAAToDate) ? WtfGlobal.convertToGenericDate(this.DTAAToDate.getValue()) : "";
                rec.DTAASpecialRate = !Wtf.isEmpty(this.DTAASpecialRate) ? this.DTAASpecialRate.getValue() : "";
                rec.nonLowerDedutionApplicable = !Wtf.isEmpty(this.typeOfDeducation) ? this.typeOfDeducation.getValue() : "";
                rec.deductionReason = !Wtf.isEmpty(this.non_lowerDeducation) ? this.non_lowerDeducation.getValue() : "";

                rec.certiFromDate = !Wtf.isEmpty(this.certiFromDate) ? WtfGlobal.convertToGenericDate(this.certiFromDate.getValue()) : "";
                rec.certiToDate = !Wtf.isEmpty(this.certiToDate) ? WtfGlobal.convertToGenericDate(this.certiToDate.getValue()) : "";

                rec.declareFromDate = !Wtf.isEmpty(this.declareFromDate) ? WtfGlobal.convertToGenericDate(this.declareFromDate.getValue()) : "";
                rec.declareToDate = !Wtf.isEmpty(this.declareToDate) ? WtfGlobal.convertToGenericDate(this.declareToDate.getValue()) : "";

                rec.transportFromDate = !Wtf.isEmpty(this.transportFromDate) ? WtfGlobal.convertToGenericDate(this.transportFromDate.getValue()) : "";
                rec.transportToDate = !Wtf.isEmpty(this.transportToDate) ? WtfGlobal.convertToGenericDate(this.transportToDate.getValue()) : "";
                if (this.interStateParty != undefined) {
                    rec.interstateparty = this.interStateParty.getValue();
                }
                if (this.cFormApplicable != undefined) {
                    rec.cformapplicable = this.cFormApplicable.getValue();
                }
                if(this.isTDSapplicableonvendor && !this.isTDSapplicableonvendor.collapsed){
                    rec.isTDSapplicableonvendor = true; 
                }else{
                    rec.isTDSapplicableonvendor = false; 
                }
                if(this.considerExemptLimit && this.considerExemptLimit.checked){
                    rec.considerExemptLimit = true; 
                }else{
                    rec.considerExemptLimit = false; 
                }
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
                url:"ACC"+this.businessPerson+"CMN"+"/save"+this.businessPerson+".do",
                params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);
//            this.save.disable();
         }
    },
    callCreditTerm:function(){
        callCreditTerm('creditTermReportWin');
    },
    genSuccessResponse:function(response){
         this.save.enable();
        if(response.success){
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.success"),
                msg: response.msg,
                buttons: Wtf.MessageBox.OK,
                icon: Wtf.MessageBox.INFO,
                scope: this,
                fn: function(btn) {
                    if (btn == "ok") {
                        this.personId = response.perAccID;//this.personId used in Wtf.account.CusVenAddressDetail component 
                        this.isPropagatedPersonalDetails = response.isPropagatedPersonalDetails;//this.isPropagatedPersonalDetails used in Wtf.account.CusVenAddressDetail component 
                        this.fireEvent('update', this, response.perAccID);

                        var tabid = 'ledger';
                        if (Wtf.getCmp(tabid) != undefined) {
                            Wtf.getCmp(tabid).accStore.reload();
                        }

                        //On updation of category reload 'by category report grid'
                        if (!this.isEdit || (this.isEdit && this.record.data.categoryid != this.Category.getValue())) {
                            var panel = (this.businessPerson == "Customer") ? Wtf.getCmp("CustomerByCategoryDetails") : Wtf.getCmp("VendorByCategoryDetails");
                            if (panel != null || panel != undefined) {
                                panel.isCategoryUpdated = true;
                            }
                        }
                        WtfGlobal.loadpersonacc(this.isCustomer);
                        WtfGlobal.loadpersonacc(!this.isCustomer);
                        this.disableComponent();
                        this.recordid = response.perAccID;
                        Wtf.apply(this.detailPanel, {accid: this.recordid});
                        Wtf.apply(this.detailPanel, {acccode: this.code.getValue()});
                        this.getDetails();
                    }
                }
            });
//            if(this.isCustomer){
//               Wtf.getCmp('mainCustomerPanel').setActiveTab(Wtf.getCmp('contactDetailCustomerTab'));
//            }else{
//               Wtf.getCmp('mainVendorPanel').setActiveTab(Wtf.getCmp('contactDetailVendorTab'));
//            }
            var maintabid = this.isCustomer?"mainCustomerPanel":"mainVendorPanel";
            if( this.isEdit != undefined && this.isEdit){
                maintabid = "edit-"+maintabid;
            }else if(this.isCopy != undefined && this.isCopy){
                maintabid = "copy-"+maintabid;
            }
            if (this.isCustomer) {
                Wtf.getCmp(maintabid).isClosable = true
            } else {
                Wtf.getCmp(maintabid).isClosable = true
            }
        }else if (response.isDuplicateExe) {
            Wtf.MessageBox.hide();
            var label = "";
            switch (response.customervendor) {
                case Wtf.Acc_Vendor_ModuleId:
                    label = WtfGlobal.getLocaleText("acc.vendor.newvendorcode");
                    break;
                case Wtf.Acc_Customer_ModuleId:
                    label = WtfGlobal.getLocaleText("acc.customer.newcustomercode");
                    break;
            }
            this.newnowin = new Wtf.Window({
                title: WtfGlobal.getLocaleText("acc.common.success"),
                closable: true,
                iconCls: getButtonIconCls(Wtf.etype.deskera),
                width: 330,
                autoHeight: true,
                modal: true,
                bodyStyle: "background-color:#f1f1f1;",
                closable:false,
                        buttonAlign: 'right',
                items: [new Wtf.Panel({
                        border: false,
                        html: (response.msg.length > 60) ? response.msg : "<br>" + response.msg,
                        height: 50,
                        bodyStyle: "background-color:white; padding: 7px; font-size: 11px; border-bottom: 1px solid #bfbfbf;"
                    }),
                    this.newdoForm = new Wtf.form.FormPanel({
                        labelWidth: 190,
                        border: false,
                        autoHeight: true,
                        bodyStyle: 'padding:10px 5px 3px; ',
                        autoWidth: true,
                        defaultType: 'textfield',
                        items: [this.newdono = new Wtf.form.TextField({
                                fieldLabel: label,
                                allowBlank: false,
                                labelSeparator: '',
                                width: 90,
                                itemCls: 'nextlinetextfield',
                                name: 'newdono',
                                id: 'newdono'
                            })],
                        buttons: [{
                                text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                                handler: function () {
                                    if (this.newdono.validate()) {
                                        if (this.moduleid == Wtf.Acc_Vendor_ModuleId) {
                                            if (response.customervendor == Wtf.Acc_Vendor_ModuleId) {
                                                this.code.setValue(this.newdono.getValue());
                                            } else {
                                                this.createASCustOrVenCode.setValue(this.newdono.getValue());
                                            }
                                        }else {
                                            if (response.customervendor == Wtf.Acc_Customer_ModuleId) {
                                                this.code.setValue(this.newdono.getValue());
                                            } else {
                                                this.createASCustOrVenCode.setValue(this.newdono.getValue());
                                            }
                                        }
                                        this.saveForm();
                                        this.newnowin.close();
                                    }
                                },
                                scope: this
                            }, {
                                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"), //"Cancel",
                                scope: this,
                                handler: function () {
                                    this.newnowin.close();
                                }
                            }]
                    })]
            });
            this.newnowin.show();
        } else if (response.isTaxDeactivated) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), response.msg], 2);
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),response.msg],response.success*2+1);
        }

    },
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.save.enable();
    },
     checkAgentMappedWithUser:function(combo,record,index){
       var data=record.data;
       if(this.venodorAvailableToAgentCheck.getValue() && (record.json.userid==undefined || record.json.userid=='')){
            Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                    msg: WtfGlobal.getLocaleText("acc.field.agentusermapping"),//Please note that there is no user associated with the selected agent. Please associate a user with this agent before you can create documents for this vendor
                    width:450,
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.WARNING,
                    scope: this
                });
       }
    },
    checkSalesPersonMappedWithUser:function(combo,record,index){
        
       var data=record.data;
       if(this.customerVenodorAvailableToSalespersonAgent.getValue() && (record.json.userid==undefined || record.json.userid=='')){
            Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                    msg: WtfGlobal.getLocaleText("acc.field.salespersonusermapping"),//Please note that there is no user associated with the selected salesperson. Please associate a user with this salesperson before you can create documents for this customer
                    width:450,
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.WARNING,
                    scope: this
                });
       }
    },
        
    checkDefaultSalesPerson : function(combo,oldVal,newVal){
        if(this.multiSalesPerson.getValue()==null || this.multiSalesPerson.getValue()=="" || this.multiSalesPerson.getValue()==undefined){
            this.salesPerson.disable();
            if(this.salesPerson.getValue()!=null && this.salesPerson.getValue()!="" && this.salesPerson.getValue()!=undefined){
                this.salesPersonStore.removeAll();
                this.salesPerson.setValue("");
//                Wtf.MessageBox.show({
//                    title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
//                    msg: WtfGlobal.getLocaleText("acc.field.DefaultSalesPersonisnotpresent")+WtfGlobal.getLocaleText("acc.field.SelectValidDefaultSalesPerson"),
//                    width:450,
//                    buttons: Wtf.MessageBox.OK,
//                    icon: Wtf.MessageBox.WARNING,
//                    scope: this
//                });
            }
        }else{
            var isSalesPersonPresent = false;
            this.salesPerson.enable();
            var multiSalesPersonStr=this.multiSalesPerson.getValue();
            var multiSalesPersonArr=multiSalesPersonStr.split(',');
            this.addRecordsInDefaultSalesPersonStore(multiSalesPersonArr);
            for(var cnt=0;cnt<multiSalesPersonArr.length;cnt++){
                if(this.salesPerson.getValue()==multiSalesPersonArr[cnt]){
                    isSalesPersonPresent=true;
                    break;
                }
            }
            if(!isSalesPersonPresent){
                if(this.salesPerson.getValue()!=null && this.salesPerson.getValue()!="" && this.salesPerson.getValue()!=undefined){
                    this.salesPerson.setValue("");
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                        msg: WtfGlobal.getLocaleText("acc.field.DefaultSalesPersonisnotpresent")+WtfGlobal.getLocaleText("acc.field.SelectValidDefaultSalesPerson"),
                        width:450,
                        buttons: Wtf.MessageBox.OK,
                        icon: Wtf.MessageBox.WARNING,
                        scope: this
                    });
                }
            }
        }
        
         if(this.readOnly){
             //In view case - due to control coming in this method , default salesperson field was getting enabled. to keep salesperson field disabledin view mode this check added.
            this.disableComponent();
        }
    },
    checkDefaultAgent: function(combo, oldVal, newVal) {
        if (this.agentCombo.getValue() == null || this.agentCombo.getValue() == "" || this.agentCombo.getValue() == undefined) {
            this.agentSingleSelectCombo.disable();
            if (this.agentSingleSelectCombo.getValue() != null && this.agentSingleSelectCombo.getValue() != "" && this.agentSingleSelectCombo.getValue() != undefined) {
                this.agentStore.removeAll();
                this.agentSingleSelectCombo.setValue("");
            }
        } else {
            if (this.readOnly) {
                this.agentSingleSelectCombo.disable();
            } else {
                this.agentSingleSelectCombo.enable();
            }
            var multiAgentStr = this.agentCombo.getValue();
            var multiAgentArr = multiAgentStr.split(',');
            this.addRecordsInDefaultAgentStore(multiAgentArr);
        }
    },
    
    addRecordsInDefaultSalesPersonStore:function(multiSalesPersonArr){
        this.salesPersonStore.removeAll();
        var newRec=new this.salesPersonRec({
            id:'',
            name:'None'    
        });
        this.salesPersonStore.add(newRec);
        for(var cnt=0;cnt<multiSalesPersonArr.length;cnt++){
            var recId = multiSalesPersonArr[cnt];
            var rec=WtfGlobal.searchRecord(Wtf.salesPersonStore,recId,"id");
            if(rec!=null){
                this.salesPersonStore.add(rec);
            }
        }
    },
    addRecordsInDefaultAgentStore:function(multiAgentArr){
        this.agentStore.removeAll();
        var newRec=new this.agentRec({
            id:'',
            name:'None'    
        });
        this.agentStore.add(newRec);
        for(var cnt=0;cnt<multiAgentArr.length;cnt++){
            var recId = multiAgentArr[cnt];
            var rec=WtfGlobal.searchRecord(Wtf.agentStore,recId,"id");
            if(rec!=null && rec.data.name != this.agentStore.data.items[0].data.name){
                this.agentStore.add(rec);
            }
        }
    },
    
    disableComponent:function(){
        this.save.disable();
        this.Title.disable(),
        this.minPriceValueForVendor.disable(),
        this.sequenceFormatCombobox.disable(),
        this.code.disable(),
        this.name.disable(),
        this.manufacturerTypeCombo.disable(), 
        this.aliasname.disable(),
        this.defaultCustomerAccount.disable(),
        this.defaultVendorAccount.disable(),
        this.openingBal.disable(),
        this.creationdate.disable(),
        this.exchangeRate.disable(),
        this.Currency.disable(),
        this.taxIDNo.disable(),                    
        this.Eligible1099.disable(),
        this.overseas.disable(),
        this.copyCustomer.disable(),
        this.sequenceFormatComboVenCus.disable(),
        this.createASCustOrVenCode.disable(),
        this.customerVenodorAvailableToSalespersonAgent.disable();
        this.venodorAvailableToAgentCheck.disable();
        this.agentCombo.disable();
        this.agentSingleSelectCombo.disable();
        this.salesPerson.disable(),
        this.mappingReceivedFrom.disable(),
        this.mappingPaidTo.disable(),
        this.GSTINRegistrationTypeCombo.disable(),
        this.CustomerVendorTypeCombo.disable();
        if (this.CustVenTypeCombo != undefined) {
            this.CustVenTypeCombo.disable();
        }
        this.multiSalesPerson.disable();
        this.salesPerson.disable();
        this.other.disable(),
        this.bankaccountno.disable(),
        this.CreditTerm.disable(),
        this.Category.disable(),
        this.Tax.disable(),
        this.limit.disable(),
        this.InterCompany.disable(),
        this.InterCompanyType.disable() ,
        this.ProductMapping.disable(),
        this.addPreferredProduct.disable(),
        this.isActivateIBG.disable(),
        this.isPermOrOnetime.disable(),
        this.companyRegNo.disable(),
        this.gstRegNo.disable(),
        this.rmcdApprovalNo.disable(),
        this.selfBilledFromDate.disable(),
        this.selfBilledToDate.disable(),
        this.gstVerifiedDate.disable(),
        this.sezFromDate.disable(),
        this.sezToDate.disable(),
        this.UENNo.disable();
        this.VATTINNo.disable();
        this.CSTTINNo.disable();
        this.GSTIN.disable();
        if (this.TIN != undefined) {
            this.TIN.disable();
        }
        this.CSTRegDate.disable();
        this.vatRegDate.disable();
        this.PANNo.disable();
        this.natureOfPayment.disable();
        this.tdsInterestPayableAccount.disable();
        this.vendorBranchCombo.disable();
        this.SERVICENo.disable();
        this.TANNo.disable();
        this.ECCNo.disable();
        this.pmtMethod.disable();            //ERM-735 disabled in view case. 
        this.lifoFifoCombo.disable();
        this.dealerTypeCombo.disable();
        this.interStateParty.disable();
        this.cFormApplicable.disable();
        this.gtaApplicable.disable();  // GTA Applicable  ERP-25539
        this.residentialstatus0.disable();
        this.residentialstatus1.disable();
        this.pricingBand.disable();
        this.issubFieldset.disable();
        this.parentAccount.disable();
        this.deducteeType.disable();
        this.deducteeCode.disable();
        this.panStatusCombo.disable();
        this.EmploymentStatus.disable();
        this.employerName.disable();
        this.companyAddress.disable();
        this.occupationAndYears.disable();
        this.monthlyIncome.disable();
        this.noofActiveCreditLoans.disable();
        this.npwp.disable();
        if (!Wtf.isEmpty(this.isTDSapplicableonvendor) && !Wtf.isEmpty(this.isTDSapplicableonvendor.checkbox) && !Wtf.isEmpty(this.isTDSapplicableonvendor.checkbox.dom)) {
            this.isTDSapplicableonvendor.checkbox.dom.disabled = true;
        }
        if (!Wtf.isEmpty(this.isVATCSTapplicableOnCompanyFieldSet) && !Wtf.isEmpty(this.isVATCSTapplicableOnCompanyFieldSet.checkbox) && !Wtf.isEmpty(this.isTDSapplicableonvendor.checkbox.dom)) {
            this.isVATCSTapplicableOnCompanyFieldSet.checkbox.dom.disabled = true;
        }
        this.defaultNatureOfPurchase.disable();
        this.importerECCNo.disable();
        this.IECno.disable();
        this.rangeCode.disable();
        this.divisionCode.disable();
        this.commissionerateName.disable();
        this.exiceDetailsFieldSet.disable();
        this.GSTdetailsFieldSet.disable();
        if(this.southPanel){
            this.southPanel.disable();
        }
        if(this.tagsFieldset){
            this.tagsFieldset.disable();
        }
        /*
         * Disable below fields in View Case When Delivery Planner option is 'True'
        */
        if (Wtf.account.companyAccountPref.deliveryPlanner && this.isCustomer) {
            this.deliveryDate.disable();
            this.deliveryTime.disable();
            this.vehicleNo.disable();
            this.driver.disable();
        }
    },
    
    loadTDSRecords: function(){
        if (Wtf.isTDSApplicable && !this.isCustomer) {
            if (this.record.data.residentialstatus == 0) {
                if (!Wtf.isEmpty(this.record.data.panno)) {
                    this.lowerRatePanel.show();
                    this.lowerRatePanel.doLayout();
                    this.higherRatePanel.hide();
                    //If PAN is given, Higer Rate will be non-mandatory.
                    if (!Wtf.isEmpty(this.record.data.nonLowerDedutionApplicable)) {
                        this.typeOfDeducation.setValue(this.record.data.nonLowerDedutionApplicable);
                        if (this.record.data.nonLowerDedutionApplicable == "1") { // YES
                            this.lowerRatePanelYorN.show();
                            if (!Wtf.isEmpty(this.record.data.deductionReason)) {
                                this.non_lowerDeducation.setValue(this.record.data.deductionReason);
                                if (this.record.data.deductionReason == this.deductionComboId.Non_Deduction_or_Lower_Deduction) { // Non Deduction or Lower Deduction is on account of a Certificate under Section 197
                                    this.certiFieldset.show();
                                    this.certiFieldset.doLayout();
                                    this.declareFieldset.hide();
                                    this.transportPanel.hide();
                                    this.CertificateNo.setValue(this.record.data.certificateNo);
                                    this.certiFromDate.setValue(this.record.data.deductionFromDate);
                                    this.certiToDate.setValue(this.record.data.deductionToDate);
                                    if (!Wtf.isEmpty(this.record.data.lowerRate)) {
                                        this.lowerRate.setValue(this.record.data.lowerRate);
                                    }
                                } else if (this.record.data.deductionReason == this.deductionComboId.Non_Deduction_Declaration) {//Non Deduction is on account of a Declaration under Section 197 A
                                    this.declareFieldset.show();
                                    this.declareFieldset.doLayout();
                                    this.certiFieldset.hide();
                                    this.transportPanel.hide();
                                    this.declareRefNo.setValue(this.record.data.referenceNumberNo);
                                    this.declareFromDate.setValue(this.record.data.deductionFromDate);
                                    this.declareToDate.setValue(this.record.data.deductionToDate);
                                } else if (this.record.data.deductionReason == this.deductionComboId.Deduction_Transporter) {//Transporter owning not more than 10 goods carriages at any time during the previous year
                                    this.declareFieldset.hide();
                                    this.certiFieldset.hide();
                                    this.transportPanel.show();
                                    this.transportPanel.doLayout();
                                    this.transportRefNo.setValue(this.record.data.referenceNumberNo);
                                    this.transportFromDate.setValue(this.record.data.deductionFromDate);
                                    this.transportToDate.setValue(this.record.data.deductionToDate);
                                } else {
                                    this.certiFieldset.hide();
                                    this.declareFieldset.hide();
                                    this.transportPanel.hide();
                                }
                            } else {
                                this.certiFieldset.hide();
                                this.declareFieldset.hide();
                                this.transportPanel.hide();
                            }
                        } else {
                            this.lowerRatePanelYorN.hide();
                        }
                    }
                } else {
                    this.higherRatePanel.show();
                    this.higherRatePanel.doLayout();
                    if (!Wtf.isEmpty(this.record.data.higherTDSRate)) {
                        this.higherRate.setValue(this.record.data.higherTDSRate);
                    }
                    this.lowerRatePanel.hide();
                }
            } else {
                this.higherRatePanel.hide();
                this.lowerRatePanel.hide();
                this.DTAAPanel.show();
                this.DTAAPanel.doLayout();
                this.DTAAApplicable.setValue(this.record.data.dtaaApplicable)
                if (this.record.data.dtaaApplicable == "1") {
                    this.DTAAPanelYes.show();
                    this.DTAAPanelYes.doLayout();
                    this.DTAAFromDate.setValue(this.record.data.dtaaFromDate);
                    this.DTAAToDate.setValue(this.record.data.dtaaToDate);
                    if (!Wtf.isEmpty(this.record.data.dtaaSpecialRate)) {
                        this.DTAASpecialRate.setValue(this.record.data.dtaaSpecialRate);
                    }
                }
            }
        }
                }
 });
 
 
/************************************** Contact Detail Component*************************************
 *This component used for
 *1. Customer Address Details
 *2. Vendor Address Details
 *3. Company Address Details
 */

Wtf.account.CusVenAddressDetail = function(config){
    this.perAccID=null;
    this.ispropagatetochildcompanyflag=false;
    this.isClosable=true;
    this.businessPerson=(config.isCustomer?"Customer":"Vendor");
    this.uPermType=config.isCustomer?Wtf.UPerm.customer:Wtf.UPerm.vendor;
    this.permType=config.isCustomer?Wtf.Perm.customer:Wtf.Perm.vendor;
    this.enableCurrency=config.enableCurrency;
    this.modeName=config.modeName;
    this.readOnly=config.readOnly==undefined?false:config.readOnly;
    this.isCompany=config.isCompany?config.isCompany:false;
    this.addAddressFromTransactions=config.addAddressFromTransactions?config.addAddressFromTransactions:false;
    this.personId="";
    this.isAddressDetailSaved=false;   
    this.defaultBillingAddressID="";
    this.defaultShippingAddressID="";
    this.billingAliasNameArr = new Array();   
    this.shippingAliasNameArr = new Array();   
    this.addressDetailArr = new Array(); 
    this.billingComboValueBeforeSelect="";
    this.heplmodeid=config.helpmodeid;
    this.shippingComboValueBeforeSelect="";
    this.achorValue=this.isCompany?'96%':'98%',
    this.custVenId=config.stateAsComboFlag;
    //Flag to indicate whether avalara address validation is enabled for customer or not
    this.avalaraAddressValidation = Wtf.account.companyAccountPref.avalaraIntegration && Wtf.account.companyAccountPref.avalaraAddressValidation && config.isCustomer;
    var buttonArray = new Array();
    this.loadMask = new Wtf.LoadMask(document.body, {
        msg: WtfGlobal.getLocaleText("acc.msgbox.50")//loadMask Message -> Loading...
    });
    Wtf.apply(this,config);
    this.save=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
        toolTip: WtfGlobal.getLocaleText("acc.rem.175"),
        id: "save" + config.id,
        hidden: this.isViewTemplate || this.readOnly,
        scope: this,
        handler:this.validateAndSave.createDelegate(this,[true]),
        iconCls: 'pwnd save'
    });
     this.deletebttn=new Wtf.Toolbar.Button({
//        text: WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"), //'Delete',
//        toolTip: WtfGlobal.getLocaleText("acc.field.allowsyoutodeletetherecord"),
        text: WtfGlobal.getLocaleText("acc.setupWizard.gridDeleteaddress"), //'Delete Address ...',
        tooltip: WtfGlobal.getLocaleText("acc.vendormanagement.deletetooltip")+" " + ((config.isCustomer)?WtfGlobal.getLocaleText("acc.vendormanagement.Customeraddresses"):WtfGlobal.getLocaleText("acc.vendormanagement.Vendoraddresses") ) +WtfGlobal.getLocaleText("acc.vendormanagement.todelete"),
        id: "deletebttn" + config.id,
        hidden: this.isViewTemplate || this.readOnly,
        scope: this,
        handler:this.DeleteAdderssWindow.createDelegate(this),
        iconCls: getButtonIconCls(Wtf.etype.menudelete),
        disabled:true        
    });

    if(!this.isCompany){//in case of call from company preferences we do not need any button of this panel so applying check
        if(this.addAddressFromTransactions){
            buttonArray.push(this.save);
        } else{
            buttonArray.push(this.save,'-',this.deletebttn);
        }
        
        //If Avalara Integration is enabled and address validation is enabled, then we add a button for user to validate addresses
        if (this.avalaraAddressValidation) {
            this.validateAddressBttn = new Wtf.Toolbar.Button({//Button to validate address with Avalara REST service
                text: WtfGlobal.getLocaleText("acc.common.validateAddresses"),
                tooltip: WtfGlobal.getLocaleText("acc.integration.validateAddressesWithAvalara"),
                scope: this,
                handler: this.validateAddressWithAvalara.createDelegate(this,[false]),
                iconCls: 'pwnd validate'
            });
            buttonArray.push('-', this.validateAddressBttn);
        }
        
        Wtf.apply(this, {
            bbar:buttonArray
        }); 
    }
    Wtf.account.CusVenAddressDetail.superclass.constructor.call(this,config);
    this.addEvents({
        'update':true
    });
}

Wtf.extend( Wtf.account.CusVenAddressDetail, Wtf.Panel, {
    /**
     * Function to perform any validation/checks before saving address
     * @param {type} isCallFromSave
     * @returns {undefined}
     */
    validateAndSave: function (isCallFromSave) {
        if (this.avalaraAddressValidation) {
            this.validateAddressWithAvalara(isCallFromSave);//Validate the addresses before save only if Avalara Address Validation is enabled
        } else {
            this.saveForm();
        }
    },
    /**
     * Function to validate addresses with Avalara REST API
     * Used only when Avalara Integration is enabled
     * @param {type} isCallFromSave
     * @returns {undefined}
     */
    validateAddressWithAvalara: function (isCallFromSave) {//isCallFromSave is true when the call to function comes from Save button's handler
        this.loadMask.show();
        Wtf.Ajax.requestEx({
            url: "Integration/validateAddress.do",
            method: "POST",
            params: {
                addressesForValidationWithAvalara: JSON.stringify(this.addressForm.getForm().getValues()),
                integrationPartyId: Wtf.integrationPartyId.AVALARA,//Identifier for Integration Service owner party. 2 -> Avalara REST Service
                integrationOperationId: Wtf.integrationOperationId.avalara_addressValidation//Identifier for Integration operation which is to be performed
            }
        }, this, function (res, req) {
            this.loadMask.hide();
            if (res.success) {
                if (isCallFromSave) {//If address validation is successful, then save addresses
                    this.saveForm();
                } else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), res.msg], 0);
                }
            } else {
                if (isCallFromSave) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.integration.addressValidationFailure") + "<br><br><b>NOTE: </b>" + WtfGlobal.getLocaleText("acc.integration.addressValidationSettingsInfo")], 1);
                } else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), res.msg], 1);
                }
            }
        }, function () {
            this.loadMask.hide();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.field.Anerroroccurredwhileconnectingtoservice")], 1)
        });
    },
    loadRecord:function(){
        if(this.record!=null && this.record.data.addressDetails!=null && this.record.data.addressDetails != undefined ){
            var counter=0;
            var billingAddrCount=0;
            var shippingAddrCount=0;
            /*
             * 
             * @type getCustomerVendorType returns defaultMasterItemId of customer/Vendor
             */
            var customerVendorType=this.getCustomerVendorType();
            
            
            var stateAsComboFlag = WtfGlobal.isIndiaCountryAndGSTApplied() && (customerVendorType == undefined || !(customerVendorType.data.defaultMasterItem== Wtf.GSTCUSTVENTYPE.Export|| customerVendorType.data.defaultMasterItem== Wtf.GSTCUSTVENTYPE.ExportWOPAY|| customerVendorType.data.defaultMasterItem== Wtf.GSTCUSTVENTYPE.Import));
            var addressArray = this.record.data.addressDetails;   
            if(addressArray.length>=1){
                this.deletebttn.enable();
            }
            for(counter=0;counter<addressArray.length;counter++)
            {
                var addresses=addressArray[counter];
//                if(addresses.isBillingAddress){
//                    billingAddrCount++;
//                } else {
//                    shippingAddrCount++;
//                }
                var addrRec={
                    aliasNameID:addresses.aliasName,
                    aliasName:addresses.aliasName,
                    address:addresses.address,
                    county:addresses.county,
                    city:addresses.city,
                    state:addresses.state,
                    country:addresses.country,
                    postalCode:addresses.postalCode,
                    phone:addresses.phone,
                    mobileNumber:addresses.mobileNumber,
                    fax:addresses.fax,
                    emailID:addresses.emailID,
                    recipientName:addresses.recipientName,
                    contactPerson:addresses.contactPerson,
                    contactPersonNumber:addresses.contactPersonNumber,
                    contactPersonDesignation:addresses.contactPersonDesignation,
                    website:addresses.website,
                    shippingRoute:addresses.shippingRoute,
                    isBillingAddress:addresses.isBillingAddress
                }
                this.addressDetailArr.push(addrRec); //pushing address in address array
               
                if(addresses.isBillingAddress){     
                    this.billingAliasNameArr.push(addresses.aliasName);//pushing alias name in array. this array is used to check unique name for alias name
                    var storeRec=new Wtf.data.Record({
                        id:addresses.aliasName,
                        name:addresses.aliasName
                    });                    
                    this.billingAddrsStore.add(storeRec);//adding alias name in store data   
                    this.billingAddrsStore.commitChanges();
                    
                    if (addresses.isDefaultAddress) {
                        this.defaultBillingAddressID = addresses.aliasName;
                        this.billingAddrsCombo.setValue(addresses.aliasName);
                        this.billingAliasName.setValue(addresses.aliasName);
                        this.billingAddress.setValue(addresses.address);
                        if ( WtfGlobal.isUSCountryAndGSTApplied()) {
                            if(addresses.county!=undefined && addresses.county!='' && addresses.county!=null)
                            this.billingCounty.setValForRemoteStore(addresses.county, addresses.county);
                        } else {
                            this.billingCounty.setValue(addresses.county);
                        }
                       if ( WtfGlobal.isUSCountryAndGSTApplied()) {
                           if(addresses.city!=undefined && addresses.city!='' && addresses.city!=null)
                            this.billingCity.setValForRemoteStore(addresses.city, addresses.city);
                        } else {
                            this.billingCity.setValue(addresses.city);
                        }
                        if ((WtfGlobal.isIndiaCountryAndGSTApplied() && stateAsComboFlag) ||  WtfGlobal.isUSCountryAndGSTApplied()) {
                            if(addresses.state!=undefined && addresses.state!='' && addresses.state!=null)
                            this.billingStateCombo.setValForRemoteStore(addresses.state, addresses.state);
                        } else {
                            this.billingState.setValue(addresses.state);
                        }
                        this.billingStateCode.setValue(addresses.stateCode);
                        this.billingCountry.setValue(addresses.country);
                        this.billingPostal.setValue(addresses.postalCode);
                        this.billingPhone.setValue(addresses.phone);
                        this.billingMobile.setValue(addresses.mobileNumber);
                        this.billingFax.setValue(addresses.fax);
                        this.billingEmail.setValue(addresses.emailID);
                        this.billingRecipientName.setValue(addresses.recipientName);
                        this.billingContactPerson.setValue(addresses.contactPerson);
                        this.billingContactNumber.setValue(addresses.contactPersonNumber);
                        this.billingContactDesignation.setValue(addresses.contactPersonDesignation);
                        this.billingWebsite.setValue(addresses.website);
                        this.defaultBillingAddress.setValue(true);
                    }
                    
                } else {  
                    this.shippingAliasNameArr.push(addresses.aliasName);//pushing alias name in array. this array is used to check unique name for alias name
                    storeRec=new Wtf.data.Record({
                        id:addresses.aliasName,
                        name:addresses.aliasName
                    });                    
                    this.ShippingAddrsStore.add(storeRec);//adding alias name in store data    
                    this.ShippingAddrsStore.commitChanges();
                    
                    if(addresses.isDefaultAddress){   
                        this.defaultShippingAddressID=addresses.aliasName;
                        this.shippingAddrsCombo.setValue(addresses.aliasName);
                        this.shippingAliasName.setValue(addresses.aliasName);
                        this.shippingAddress.setValue(addresses.address);
                        if ( WtfGlobal.isUSCountryAndGSTApplied()) {  //Coutry specific address field for US
                            if(addresses.county!=undefined && addresses.county!='' && addresses.county!=null)
                            this.shippingCounty.setValForRemoteStore(addresses.county, addresses.county);
                        } else {
                            this.shippingCounty.setValue(addresses.county);
                        }
                        if ( WtfGlobal.isUSCountryAndGSTApplied()) { //Coutry specific address field for US
                            if(addresses.city!=undefined && addresses.city!='' && addresses.city!=null)
                            this.shippingCity.setValForRemoteStore(addresses.city, addresses.city);
                        } else {
                            this.shippingCity.setValue(addresses.city);
                        }
                        if ((WtfGlobal.isIndiaCountryAndGSTApplied() && stateAsComboFlag) ||  WtfGlobal.isUSCountryAndGSTApplied()) { //Coutry specific address field for US or INDIA
                            if(addresses.state!=undefined && addresses.state!='' && addresses.state!=null)
                            this.shippingStateCombo.setValForRemoteStore(addresses.state, addresses.state);
                        } else {
                            this.shippingState.setValue(addresses.state);
                        }
                        this.shippingStateCode.setValue(addresses.stateCode);
                        this.shippingCountry.setValue(addresses.country);
                        this.shippingPostal.setValue(addresses.postalCode);
                        this.shippingPhone.setValue(addresses.phone);
                        this.shippingMobile.setValue(addresses.mobileNumber);
                        this.shippingFax.setValue(addresses.fax);
                        this.shippingEmail.setValue(addresses.emailID);
                        this.shippingRecipientName.setValue(addresses.recipientName);
                        this.shippingContactPerson.setValue(addresses.contactPerson);
                        this.shippingContactNumber.setValue(addresses.contactPersonNumber);            
                        this.shippingContactDesignation.setValue(addresses.contactPersonDesignation); 
                        this.shippingWebsite.setValue(addresses.website);
                        this.shippingRoute.setValue(addresses.shippingRoute);
                        this.defaultShippingAddress.setValue(true);
                    }
                }                                 
            }
            if(!this.readOnly){ 
                var count=1;
                var recordIndex=0;
                do{
                    recordIndex=WtfGlobal.searchRecordIndex(this.billingAddrsStore,"Billing Address"+count,"name");
                    if(recordIndex!=-1){
                        count++;
                    }
                }while(recordIndex!=-1);
                storeRec=new Wtf.data.Record({
                    id:"Billing Address"+count,
                    name:"Billing Address"+count
                });                    
                this.billingAddrsStore.add(storeRec);//adding new Billing Address    
                this.billingAddrsStore.commitChanges();
                this.billingAliasNameArr.push("Billing Address"+count);
                
                count=1;
                do{
                    recordIndex=WtfGlobal.searchRecordIndex(this.ShippingAddrsStore,"Shipping Address"+count,"name");
                    if(recordIndex!=-1){
                        count++;
                    }
                }while(recordIndex!=-1);              
                storeRec=new Wtf.data.Record({
                    id:"Shipping Address"+count,
                    name:"Shipping Address"+count
                });                    
                this.ShippingAddrsStore.add(storeRec);//adding new Shipping Address    
                this.ShippingAddrsStore.commitChanges();    
                this.shippingAliasNameArr.push("Shipping Address"+count);
                if(this.ShippingAddrsStore.getCount()==1){
                    this.defaultShippingAddress.setValue(true);
                }
                if(this.billingAddrsStore.getCount()==1){
                    this.defaultBillingAddress.setValue(true);
                }
            }
            if(this.addAddressFromTransactions && this.record.data.addressDetails!="" && this.record.data.addressDetails!=undefined){
                this.addAliasNameToBillingStore();//these method get called to open form in add address mode
                this.addAliasNameToShippingStore();
            }
        }
    },
    onRender:function(config){
        Wtf.account.CusVenAddressDetail.superclass.onRender.call(this, config);        
        this.createStore();
        this.createFields();
        var centerPanel = new Wtf.Panel({
            id:'addresscenterpanel'+this.id,
            autoScroll : true,
            bodyStyle:' background: none repeat scroll 0 0 #DFE8F6;',
            layout: 'fit',
            items:this.addressForm
        });
        this.add(centerPanel);
        
        if(this.isCompany){
            if(this.record==null || this.record==""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.accPref.errorOccuredWhileLoadingAddress")],2); 
            } else {
                this.loadRecord(); 
            }
        } else if(this.isEdit || this.isCopy || this.addAddressFromTransactions){
            this.loadRecord(); 
        }
        if(this.readOnly){
            this.applyViewMode();            
        }
        
        this.shippingAddrsCombo.on('select',this.onShippingComboSelect,this);
        this.shippingAddrsCombo.on('beforeselect',function(combo){this.shippingComboValueBeforeSelect=combo.getValue();},this);
        this.shippingAddrsCombo.addNewFn=this.addAliasNameToShippingStore.createDelegate(this);
        this.billingAddrsCombo.on('select',this.onBillingComboSelect,this);
        this.billingAddrsCombo.on('beforeselect',function(combo){this.billingComboValueBeforeSelect=combo.getValue();},this);
        this.billingAddrsCombo.addNewFn=this.addAliasNameToBillingStore.createDelegate(this);
        this.copyAddress.on('check',this.copyBillingAddress ,this);    
        this.shippingAliasName.on('change',this.validateShippingAliasName, this);
        this.billingAliasName.on('change',this.validateBillingAliasName, this);
        this.defaultShippingAddress.on('change',this.checkDefaultShippingAddress, this);
        this.defaultBillingAddress.on('change',this.checkDefaultBillingAddress, this);  
        this.hideFormFields();
    }
    ,hideFormFields:function(){
     this.isCustomer ? this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.customer) : this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.vendor);
    },
    /*
     *  Function to hide/show formfields for Contact details window 
     */
    hideTransactionFormFields:function(array){
        if(array){
            for(var i=0;i<array.length;i++){
                var fieldArray = array[i];
                if(Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id)){
                    if(fieldArray.fieldId=="ShowOnlyOneTime" && ((this.isEdit !=undefined ?this.isEdit:false) || (this.copyInv !=undefined ?this.copyInv:false) || (this.isCopyFromTemplate !=undefined ?this.isCopyFromTemplate:false) || (this.isTemplate !=undefined ?this.isTemplate:false))){
                        continue;
                    }
                    if(fieldArray.isHidden){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hideLabel = fieldArray.isHidden;
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hidden = fieldArray.isHidden;
                    }
                    /*
                     *'Email note' is set hidden if the Email field is set hidden from company preferences
                     **/
                    if(fieldArray.fieldId =="billingEmail" && fieldArray.isHidden){
                       this.messagePanelBilling.hidden=true;                  
                    }
                    if(fieldArray.fieldId =="shippingEmail" && fieldArray.isHidden){
                       this.messagePanelShipping.hidden=true; 
                    }
                    if(fieldArray.isReadOnly){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).disabled = fieldArray.isReadOnly;
                    }
                    if(fieldArray.isUserManadatoryField && Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel != undefined){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).allowBlank = !fieldArray.isUserManadatoryField;
                        var fieldLabel="";
                        if(fieldArray.fieldLabelText!="" && fieldArray.fieldLabelText!=null && fieldArray.fieldLabelText!=undefined){
                            fieldLabel= fieldArray.fieldLabelText+" *";
                        }else{
                            fieldLabel=(Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel) + " *";
                        }
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel = fieldLabel;
                    }else{
                        if( fieldArray.fieldLabelText!=null && fieldArray.fieldLabelText!=undefined && fieldArray.fieldLabelText!=""){
                            if(fieldArray.isManadatoryField && fieldArray.isFormField )
                                Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel=fieldArray.fieldLabelText +"*";
                            else
                                Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel=fieldArray.fieldLabelText;
                        }
                  }
                }
            }
        }
    },
    applyViewMode:function(){
       this.disableComponent();
       this.copyAddress.disable();
       this.shippingAddrsCombo.enable();
       this.billingAddrsCombo.enable();
    },
    createStore:function(){  
        if(this.isCustomer){
           Wtf.ShippingRouteStore.load();   
        }        
        this.billingAddrsStore=new Wtf.data.SimpleStore({
            fields:[{
                name:"id"
            },{
                name:"name"
            }],
           /*
            * ERP-40068
            * copy case added for duplicate address.
            */
            data:(this.isEdit || this.isCompany || this.isCopy || this.addAddressFromTransactions)?[]:[["Billing Address1","Billing Address1"]]
        }); 
        this.ShippingAddrsStore=new Wtf.data.SimpleStore({
            fields:[{
                name:"id"
            },{
                name:"name"
            }],
           /*
            * ERP-40068
            * copy case added for duplicate address.
            */
            data:(this.isEdit || this.isCompany || this.isCopy || this.addAddressFromTransactions)?[]:[["Shipping Address1","Shipping Address1"]]
        }); 
    },
    createFields:function(){
        /*
         * Check whether Tax calculation is based on shipping address
         */
        this.isShipping=CompanyPreferenceChecks.getGSTCalCulationType();
        this.billingAliasName = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.AliasName")+"*",
            name:"billingAliasName",
            id:'billingAliasName'+this.heplmodeid+this.id,
            allowBlank:false,
            maxLength:49,
            allowNegative:false,
            anchor: this.achorValue,
            value:'Billing Address1'
        });
        this.billingAddress = new Wtf.form.TextArea({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.Address")+"*",
            name:"billingAddress",
            id: 'billingAddress'+this.heplmodeid+this.id,
            maxLength:250,
            height:60,
            allowBlank:false,
            allowNegative:false,
            anchor: this.achorValue
        });
        //Coutry specific address field for US and INDIA
        if ( WtfGlobal.isUSCountryAndGSTApplied()) {
            this.CountyComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);

            this.CountyComboStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'county'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.CountyComboRec)
            });

            this.billingCounty = new Wtf.form.ExtFnComboBox({
                name:"billingCounty",
                id:'billingCounty'+this.heplmodeid+this.id,
                fieldLabel: WtfGlobal.getLocaleText("acc.address.County"),
                store: this.CountyComboStore,
                valueField: 'name',
                displayField: 'name',
                extraFields:'',
                allowBlank:(Wtf.account.companyAccountPref.countryid == Wtf.Country.US && !this.isCustomer)?true:this.isShipping?true:false,  // Making changes for SDP-13213 - tax not applied at vendor side, so making this field optional. 
                extraComparisionField: 'name',
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode:'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth:true
            });
        } else {
            this.billingCounty = new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText("acc.address.County"),
                name: "billingCounty",
                id: 'billingCounty' + this.heplmodeid + this.id,
                maxLength: 49,
                allowNegative: false,
                anchor: this.achorValue,
                hidden: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.US),
                hideLabel: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.US)
            });
        }
        //Coutry specific address field for US 
        if ( WtfGlobal.isUSCountryAndGSTApplied()) {
            this.CityComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);

            this.CityComboStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'city'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.CityComboRec)
            });

             this.billingCity = new Wtf.form.ExtFnComboBox({
                name:"billingCity",
                id:'billingCity'+this.heplmodeid+this.id,
                fieldLabel: WtfGlobal.getLocaleText("acc.address.City"),
                store: this.CityComboStore,
                valueField: 'name',
                displayField: 'name',
                extraFields:'',
                allowBlank:(Wtf.account.companyAccountPref.countryid == Wtf.Country.US && !this.isCustomer)?true:this.isShipping?true:false, //// Making changes for SDP-13213 - tax not applied at vendor side, so making this field optional. 
                extraComparisionField: 'name',
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode:'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth:true
            });
        } else {
           this.billingCity = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.City"),
            name:"billingCity",
            id:'billingCity'+this.heplmodeid+this.id,
//            allowBlank:false,
            maxLength:49,
            allowNegative:false,
            anchor: this.achorValue
        });
        
        }    
        //Coutry specific address field for US and INDIA
     
            this.StateComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);

            this.StateComboStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'state'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.StateComboRec)
            });

            this.billingStateCombo = new Wtf.form.ExtFnComboBox({
//            hiddenName: 'id',
                name:"billingStateCombo",
                id:'billingStateCombo'+this.heplmodeid+this.id,
                fieldLabel: WtfGlobal.getLocaleText("acc.address.State"),
                store: this.StateComboStore,
                valueField: 'name',
                displayField: 'name',
                extraFields:'',
                allowBlank:(Wtf.account.companyAccountPref.countryid == Wtf.Country.US && !this.isCustomer)?true:this.isShipping?true:false,  //// Making changes for SDP-13213 - tax not applied at vendor side, so making this field optional. 
                extraComparisionField: 'name',
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode:'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth:true
            });
       
                this.billingState = new Wtf.form.TextField({
                fieldLabel:WtfGlobal.getLocaleText("acc.address.State"),
                name:"billingState",
                id:'billingState'+this.heplmodeid+this.id,
    //            allowBlank:false,
                maxLength:49,
                allowNegative:false,
                    anchor: this.achorValue
                });
        
        this.billingStateCode = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.india.address.StateCode"),
            name:"billingStateCode",
            id: 'billingStateCode'+this.heplmodeid+this.id,
            maxLength:2,
            allowNegative:false,
            regex:/[A-Z]{2}/, 
            hideLabel: !Wtf.IndianGST,
            hidden: !Wtf.IndianGST,
            anchor: this.achorValue,
            invalidText :'Invalid '+WtfGlobal.getLocaleText("acc.india.address.StateCode")+' Eg. MH,GJ'
        });
        
        this.billingCountry = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.Country"),
            name:"billingCountry",
            id: 'billingCountry'+this.heplmodeid+this.id,
//            allowBlank:false,
            maxLength:49,
            allowNegative:false,
            anchor: this.achorValue
        });
        
        this.billingPostal = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.PostalCode"),
            name:"billingPostal",
            id: 'billingPostal'+this.heplmodeid+this.id,
//            allowBlank:false,
            maxLength:50,
            allowNegative:false,
            anchor: this.achorValue
        });
        
        this.billingPhone= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.Phone"), 
            name: 'billingPhone',
            id:'billingPhone'+this.heplmodeid+this.id,
            maxLength:250,
            anchor: this.achorValue
        });
        
        this.billingMobile= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.Mobile"),
            name: 'billingMobile',
            id:'billingMobile'+this.heplmodeid+this.id,
//            allowBlank:false,
            maxLength:250,
            anchor: this.achorValue
        });
        
        this.billingFax=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.Fax"),
            name: 'billingFax',
            id:'billingFax'+this.heplmodeid+this.id,
            maxLength:250,
            anchor: this.achorValue
        });
        
        this.billingEmail= new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Email"),  
            name: 'billingEmail',
            id:'billingEmail'+this.heplmodeid+this.id,
//            allowBlank:false,
            maxLength:254,
            validator:WtfGlobal.validateMultipleEmail,
            anchor: this.achorValue
        });
        
        this.messagePanelBilling = new Wtf.Panel({
            xtype: 'panel', 
            border: false,
            cls: 'emailfieldInfoInContactDetails',
            html: WtfGlobal.getLocaleText("acc.mail.seperator.comma")
        });
        
        this.billingRecipientName=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.RecipientName"),
            name: 'billingRecipientName',
            id: 'billingRecipientName'+this.heplmodeid+this.id,
            maxLength:200,
            anchor: this.achorValue
        });
        
        this.billingContactPerson=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.ContactPerson"),
            name: 'billingContactPerson',
            id:'billingContactPerson'+this.heplmodeid+this.id,
//            allowBlank:false,
            maxLength:200,
            anchor: this.achorValue
        });
        
        this.billingContactNumber= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.ContactPersonNumber"),
            name: 'billingContactPersonNumber',
            id:'billingContactNumber'+this.heplmodeid+this.id,
            maxLength:250,
            anchor: this.achorValue
        });
        this.billingContactDesignation = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.ContactPersonDesignation"),
            name: 'billingContactPersonDesignation',
            id: 'billingContactDesignation'+this.heplmodeid+this.id,
            maxLength: 250,
            anchor: this.achorValue
        }); 
        this.billingWebsite = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.website"),
            name: 'billingWebsite',
            id: 'billingWebsite'+this.heplmodeid+this.id,
            maxLength: 250,
            anchor: this.achorValue
        }); 
         this.defaultShippingAddress= new Wtf.form.Checkbox({
            name:'defaultshippingaddress',
            id:'defaultshippingaddress'+this.heplmodeid+this.id,
            fieldLabel: WtfGlobal.getLocaleText("acc.address.setasdefault"),
            checked:(this.isEdit || this.isCompany || this.addAddressFromTransactions)?false:true,
            cls : 'custcheckbox',
            width: 10
        });
        
        this.copyAddress= new Wtf.form.Checkbox({
            name:'copyadress',
            id:'copyadress'+this.heplmodeid+this.id,
            fieldLabel: WtfGlobal.getLocaleText("acc.cust.sameasbillingadd"),//WtfGlobal.getLocaleText("acc.cust.copyAdd"),  //'Copy Address',
            checked:false,
            cls : 'custcheckbox',
            width: 10
        });
        
        this.shippingAliasName = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.AliasName")+"*",
            name:"shippingAliasName",
            id:'shippingAliasName'+this.heplmodeid+this.id,
            allowBlank:false,
            maxLength:49,
            allowNegative:false,
            anchor: this.achorValue,
            value:'Shipping Address1'
        });
        
        this.shippingAddress = new Wtf.form.TextArea({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.Address")+"*",
            name:"shippingAddress",
            id:'shippingAddress'+this.heplmodeid+this.id,
            maxLength:250,
            height:60,
            allowBlank:false,
            allowNegative:false,
            anchor: this.achorValue
        });
        //Coutry specific address field for US 
        if ( WtfGlobal.isUSCountryAndGSTApplied()) {
            this.ShippingCountyComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);

            this.ShippingCountyStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'county'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.ShippingCountyComboRec)
            });

              this.shippingCounty = new Wtf.form.ExtFnComboBox({
                name:"shippingCounty",
                id:'shippingCounty'+this.heplmodeid+this.id,
                fieldLabel: WtfGlobal.getLocaleText("acc.address.County"),
                store: this.ShippingCountyStore,
                valueField: 'name',
                displayField: 'name',
                extraFields:'',
                allowBlank:(Wtf.account.companyAccountPref.countryid == Wtf.Country.US && !this.isCustomer)?true:this.isShipping?false:true,
                extraComparisionField: 'name',
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode:'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth:true
            });
        } else {
            this.shippingCounty = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.County"),
            name:"shippingCounty",
            id: 'shippingCounty'+this.heplmodeid+this.id,
            maxLength:49,
            allowNegative:false,
                anchor: this.achorValue,
                hidden: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.US),
                hideLabel: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.US)
            });
        }
        //Coutry specific address field for US 
        if ( WtfGlobal.isUSCountryAndGSTApplied()) {
            this.ShippingCityComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);

            this.ShippingCityStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'city'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.ShippingCityComboRec)
            });

             this.shippingCity = new Wtf.form.ExtFnComboBox({
                name:"shippingCity",
                id:'shippingCity'+this.heplmodeid+this.id,
                fieldLabel: WtfGlobal.getLocaleText("acc.address.City"),
                store: this.ShippingCityStore,
                valueField: 'name',
                displayField: 'name',
                extraFields:'',
                allowBlank:((Wtf.account.companyAccountPref.countryid == Wtf.Country.US) && !this.isCustomer)?true:this.isShipping?false:true,
                extraComparisionField: 'name',
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode:'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth:true
            });
        } else {
            this.shippingCity = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.City"),
            name:"shippingCity",
            id: 'shippingCity'+this.heplmodeid+this.id,
            maxLength:49,
            allowNegative:false,
            anchor: this.achorValue
            });
        }  
        //Coutry specific address field for US and INDIA
        
            this.ShippingStateComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);
            this.ShippingStateComboStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'state'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.ShippingStateComboRec)
            });
             this.shippingStateCombo= new Wtf.form.ExtFnComboBox({
                name:"shippingStateCombo",
                id:'shippingStateCombo'+this.heplmodeid+this.id,
                fieldLabel: WtfGlobal.getLocaleText("acc.address.State"),
                store: this.ShippingStateComboStore,
                valueField: 'name',
                displayField: 'name',
                extraFields:'',
                allowBlank:((Wtf.account.companyAccountPref.countryid == Wtf.Country.US) && !this.isCustomer)?true:this.isShipping?false:true,
                extraComparisionField: 'name',
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode:'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth:true
            });
       
         this.shippingState = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.State"),
            name:"shippingState",
            id:'shippingState'+this.heplmodeid+this.id,
            maxLength:49,
            allowNegative:false,
            anchor: this.achorValue
        });
        
        this.shippingStateCode = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.india.address.StateCode"),
            name:"shippingStateCode",
            id: 'shippingStateCode'+this.heplmodeid+this.id,
            maxLength:2,
            allowNegative:false,
            regex:/[A-Z]{2}/, 
            hideLabel: !Wtf.IndianGST,
            hidden: !Wtf.IndianGST,
            anchor: this.achorValue,
            invalidText :'Invalid '+WtfGlobal.getLocaleText("acc.india.address.StateCode")+' Eg. MH,GJ'
        });
        this.shippingCountry = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.Country"),
            name:"shippingCountry",
            id: 'shippingCountry'+this.heplmodeid+this.id,
            maxLength:49,
            allowNegative:false,
            anchor: this.achorValue
        });
        
        this.shippingPostal = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.PostalCode"),
            name:"shippingPostal",
            id: 'shippingPostal'+this.heplmodeid+this.id,
            maxLength:50,
            allowNegative:false,
            anchor: this.achorValue
        });
        
        this.shippingPhone= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.Phone"), 
            name: 'shippingPhone',
            id: 'shippingPhone'+this.heplmodeid+this.id,
            maxLength:250,
            anchor: this.achorValue
        });
        
        this.shippingMobile= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.Mobile"), 
            name: 'shippingMobile',
            id: 'shippingMobile'+this.heplmodeid+this.id,
            maxLength:250,
            anchor: this.achorValue
        });
        
        this.shippingFax=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.Fax"),
            name:'shippingFax',
            id: 'shippingFax'+this.heplmodeid+this.id,
            maxLength:250,
            anchor: this.achorValue
        });
        
        this.shippingEmail= new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Email"),  
            name: 'shippingEmail',
            id: 'shippingEmail'+this.heplmodeid+this.id,          
            maxLength:254,
            anchor: this.achorValue,
            validator:WtfGlobal.validateMultipleEmail
        });
        
        this.messagePanelShipping = new Wtf.Panel({
            xtype: 'panel', border: false,
            cls: 'emailfieldInfoInContactDetails',
            html: WtfGlobal.getLocaleText("acc.mail.seperator.comma")
        });
        
        this.shippingRecipientName=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.RecipientName"),
            name: 'shippingRecipientName',
            id: 'shippingRecipientName'+this.heplmodeid+this.id,
            maxLength:200,
            anchor: this.achorValue
        });
        
        this.shippingContactPerson=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.ContactPerson"),
            name: 'shippingContactPerson',
            id: 'shippingContactPerson'+this.heplmodeid+this.id,
            maxLength:200,
            anchor: this.achorValue
        });
        
        this.shippingContactNumber= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.ContactPersonNumber"),
            name: 'shippingContactPersonNumber',
            id: 'shippingContactNumber'+this.heplmodeid+this.id,
            maxLength:250,
            anchor: this.achorValue
        });
        this.shippingContactDesignation = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.ContactPersonDesignation"),
            name: 'shippingContactPersonDesignation',
            id: 'shippingContactDesignation'+this.heplmodeid+this.id,
            maxLength: 250,
            anchor: this.achorValue
        });
        this.shippingWebsite = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.website"),
            name: 'shippingWebsite',
            id: 'shippingWebsite'+this.heplmodeid+this.id,
            maxLength: 250,
            anchor: this.achorValue
        }); 
        this.shippingRoute= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.mrp.field.route"),
            name: 'shippingRoute',
            id: 'shippingroute'+this.heplmodeid+this.id,
            store:Wtf.ShippingRouteStore,
            displayField:'name',
            valueField:'id',
            triggerAction:'all',
            mode: 'local',
            anchor: this.achorValue,
            typeAhead: true,
            hideLabel: !this.isCustomer,
            hidden: !this.isCustomer
        });
        
        this.defaultBillingAddress= new Wtf.form.Checkbox({
            name:'defaultbillingaddress',
            id: 'defaultbillingaddress'+this.heplmodeid+this.id,
            fieldLabel: WtfGlobal.getLocaleText("acc.address.setasdefault"),
            checked:(this.isEdit || this.isCompany || this.addAddressFromTransactions)?false:true,
            cls : 'custcheckbox',
            width: 10
        });
        
        this.billingAddrsCombo = new Wtf.form.FnComboBox({
            triggerAction:'all',
            mode: 'local',
            valueField:'id',
            displayField:'name',
            id: "billingAddr" + this.heplmodeid + this.id,
            store:this.billingAddrsStore,
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectBillingAddress..."), 
            fieldLabel:WtfGlobal.getLocaleText("acc.address.Billing")+" "+WtfGlobal.getLocaleText("acc.address.Address")+"*",
            anchor: this.achorValue,
            typeAhead: true,
            forceSelection: true,
            value:"Billing Address1",
            btnToolTip:WtfGlobal.getLocaleText("acc.address.addbutton.tooltip")
        });
        
        this.shippingAddrsCombo= new Wtf.form.FnComboBox({
            triggerAction:'all',
            mode: 'local',
            valueField:'id',
            displayField:'name',
            id: "shippingAddr" + this.heplmodeid + this.id,
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectShippingAddress..."),  
            store:this.ShippingAddrsStore,            
            fieldLabel:WtfGlobal.getLocaleText("acc.address.Shipping")+" "+WtfGlobal.getLocaleText("acc.address.Address")+"*",
            anchor: this.achorValue,
            typeAhead: true,
            forceSelection: true,
            value:"Shipping Address1",
            btnToolTip:WtfGlobal.getLocaleText("acc.address.addbutton.tooltip")
        });
        
        this.billing=new Wtf.form.FieldSet({
            title:WtfGlobal.getLocaleText("acc.address.Billing")+" "+WtfGlobal.getLocaleText("acc.address.Address"),
            id:'billing'+this.heplmodeid+this.id,
            bodyStyle:'padding:5px',
            autoHeight:true,
            width : this.isCompany?510:500,
            items: [this.billingAddrsCombo, this.billingAliasName, this.billingAddress, this.billingCounty, this.billingCity,this.billingStateCombo,this.billingState, this.billingStateCode, this.billingCountry, this.billingPostal, this.billingPhone, this.billingMobile, this.billingFax, this.billingEmail, this.messagePanelBilling, this.billingRecipientName, this.billingContactPerson, this.billingContactNumber, this.billingContactDesignation, this.billingWebsite, this.defaultBillingAddress]
        });
        
        this.shipping=new Wtf.form.FieldSet({
            title:WtfGlobal.getLocaleText("acc.address.Shipping")+" "+WtfGlobal.getLocaleText("acc.address.Address"), 
            id:'shipping'+this.heplmodeid+this.id,
            bodyStyle:'padding:5px',
            autoHeight:true,
            width : this.isCompany?510:500,
            items: [this.shippingAddrsCombo, this.shippingAliasName, this.shippingAddress, this.shippingCounty, this.shippingCity,this.shippingStateCombo,this.shippingState, this.shippingStateCode, this.shippingCountry, this.shippingPostal, this.shippingPhone, this.shippingMobile, this.shippingFax, this.shippingEmail, this.messagePanelShipping, this.shippingRecipientName, this.shippingContactPerson, this.shippingContactNumber, this.shippingContactDesignation, this.shippingWebsite, this.shippingRoute, this.defaultShippingAddress]
        });
        
        this.addressForm=new Wtf.form.FormPanel({
            autoHeight:true,
            autowidth:true,
            id:"addressForm"+this.id,
            border:false,
            items:[{
                layout:'form',
                baseCls:'northFormFormat',
                cls:"visibleDisabled",
                labelWidth:110,
                items:[{
                    layout:'column',
                    border:false,
                    defaults:{
                        border:false
                    },
                    items:[{
                        layout:'form',
                        columnWidth:0.50,
                        bodyStyle:'padding-top:38px',
                        items:[this.billing]
                    },{
                        layout:'form',
                        columnWidth:0.50,
                        items:[this.copyAddress,this.shipping]
                    }]
                }]
            }]
        });
        
        this.addressForm.on("afterlayout", function () {
            this.AddressFieldHideShow();
        }, this);
    },
    /*
     *  To Hide/Show the State field as dropdown
     *  Hide State as Combo and show as textfield- In Case Indian Country and Customer type= Export (WPAY)/Export(WOPAY),Vendor type=Import
     */
    AddressFieldHideShow:function(){

        if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
            var stateAsComboFlag;
            var customerVendorType = this.getCustomerVendorType();
            stateAsComboFlag = WtfGlobal.isIndiaCountryAndGSTApplied() && (customerVendorType == undefined || !(customerVendorType.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.Export || customerVendorType.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.ExportWOPAY || customerVendorType.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.Import));
            if (this.custVenId != undefined) {
                 stateAsComboFlag = this.custVenId;
            }
            if (stateAsComboFlag) {
                this.billingStateCombo.on("render", function () {
                    WtfGlobal.showFormElement(this.billingStateCombo);
                }, this);
                this.billingState.on("render", function () {
                    WtfGlobal.hideFormElement(this.billingState);
                    this.billingState.allowBlank = true;
                }, this);
                this.shippingStateCombo.on("render", function () {
                    WtfGlobal.showFormElement(this.shippingStateCombo);
                }, this);
                this.shippingState.on("render", function () {
                    WtfGlobal.hideFormElement(this.shippingState);
                    this.shippingState.allowBlank = true;
                }, this);

            } else{
                this.billingState.on("render", function () {
                    WtfGlobal.showFormElement(this.billingState);
                }, this);
                this.shippingState.on("render", function () {
                    WtfGlobal.showFormElement(this.shippingState);
                }, this);
                this.billingStateCombo.on("render", function () {
                    WtfGlobal.hideFormElement(this.billingStateCombo);
                    this.billingStateCombo.allowBlank = true;
                }, this);
                this.shippingStateCombo.on("render", function () {
                    WtfGlobal.hideFormElement(this.shippingStateCombo);
                    this.shippingStateCombo.allowBlank = true;
                }, this);
            }
        } else if (WtfGlobal.isUSCountryAndGSTApplied()) {
            this.billingStateCombo.on("render", function () {
                WtfGlobal.showFormElement(this.billingStateCombo);
            }, this);
            this.shippingStateCombo.on("render", function () {
                WtfGlobal.showFormElement(this.shippingStateCombo);
            }, this);
            this.billingState.on("render", function () {
                WtfGlobal.hideFormElement(this.billingState);
                 this.billingState.allowBlank = true;
            }, this);
            this.shippingState.on("render", function () {
                WtfGlobal.hideFormElement(this.shippingState);
                 this.shippingState.allowBlank = true;
            }, this);


        }else {
            this.billingState.on("render", function () {
                WtfGlobal.showFormElement(this.billingState);
            }, this);
            this.shippingState.on("render", function () {
                WtfGlobal.showFormElement(this.shippingState);
            }, this);
            this.billingStateCombo.on("render", function () {
                WtfGlobal.hideFormElement(this.billingStateCombo);
                this.billingStateCombo.allowBlank = true;
            }, this);
            this.shippingStateCombo.on("render", function () {
                WtfGlobal.hideFormElement(this.shippingStateCombo);
                this.shippingStateCombo.allowBlank = true;
            }, this);
        }
    },
    getCustomerVendorType:function(){
         var maintabid = this.isCustomer ? "personalDetailCustomerTab" : "personalDetailVendorTab";
            if (this.isEdit != undefined && this.isEdit) {
                maintabid = "edit-" + maintabid;
            } else if (this.isCopy != undefined && this.isCopy) {
                maintabid = "copy-" + maintabid;
            }
            var custVenType;
            var typeIndex =3;
        if (Wtf.getCmp(maintabid) != undefined) {
            typeIndex = Wtf.getCmp(maintabid).CustomerVendorTypeCombo.store.find('id', Wtf.getCmp(maintabid).CustomerVendorTypeCombo.getValue());

            custVenType = Wtf.getCmp(maintabid).CustomerVendorTypeStore.getAt(typeIndex);

        }
        return custVenType;
    },
    validateShippingAliasName:function(){
        var shippingAliasNameDuplicateArr = [];
        for ( var index = 0; index < this.shippingAliasNameArr.length; index++ ) {
            shippingAliasNameDuplicateArr[index] = this.shippingAliasNameArr[index].toLowerCase();
        }
        if(shippingAliasNameDuplicateArr.indexOf(this.shippingAliasName.getValue().toLowerCase()) >-1){//true means current alias name already exists.So giving alert here to enter unique alias name 
            this.shippingAliasName.setValue("");
            var addressMsg=WtfGlobal.getLocaleText("acc.address.aliasnameduplicate");
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),addressMsg],2); 
        }
    },
    validateBillingAliasName:function(){
        var billingAliasNameDuplicateArr = [];
        for ( var index = 0; index < this.billingAliasNameArr.length; index++ ) {
            billingAliasNameDuplicateArr[index] =this.billingAliasNameArr[index].toLowerCase();
        }
        if(billingAliasNameDuplicateArr.indexOf(this.billingAliasName.getValue().toLowerCase()) >-1){//true means current alias name already exists.So giving alert here to enter unique alias name 
            this.billingAliasName.setValue("");
            var addressMsg=WtfGlobal.getLocaleText("acc.address.aliasnameduplicate");
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),addressMsg],2); 
        }
    },
    checkDefaultShippingAddress:function(checkBox,value){
        if(value==false && (this.defaultShippingAddressID=="" || this.defaultShippingAddressID==this.shippingAddrsCombo.getValue())){ 
            var addressMsg=WtfGlobal.getLocaleText("acc.address.defaultInfo");
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),addressMsg],2);
            checkBox.setValue(true);
        }        
    },
    checkDefaultBillingAddress:function(checkBox,value){
        if(value==false && (this.defaultBillingAddressID=="" || this.defaultBillingAddressID==this.billingAddrsCombo.getValue())){
            var addressMsg=WtfGlobal.getLocaleText("acc.address.defaultInfo");
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),addressMsg],2);
            checkBox.setValue(true);
        }
    },
    addAliasNameToShippingStore:function(){
        if(this.readOnly){
            return;
        }
        var isValid=true;
        if(!this.shippingAliasName.isValid()){
            isValid=false;
            this.shippingAliasName.markInvalid();
        } 
        if(!this.shippingAddress.isValid()){
            isValid=false;
            this.shippingAddress.markInvalid();
        }
        if(isValid){
            var totalRecord=this.ShippingAddrsStore.getCount();
            var aliasName=this.shippingAliasName.getValue();
            var comboRecIndex=WtfGlobal.searchRecordIndex(this.ShippingAddrsStore,this.shippingAddrsCombo.getValue(),"id");
             if(comboRecIndex>=0){
                var prevAliasName="";
                for(var i=0;i<this.addressDetailArr.length;i++){ //updating  this.addressDetailArr with modified addresses
                    var addrRec=this.addressDetailArr[i];
                    if(addrRec!="" && addrRec!=undefined && addrRec.aliasNameID==this.shippingAddrsCombo.getValue() && !addrRec.isBillingAddress){
                        prevAliasName=addrRec.aliasName;
                        this.addressDetailArr.splice(i,1);
                        this.addAddressDetails(false,this.shippingAddrsCombo.getValue());
                        break; //once the record found no need to iterate
                    }
                }
                
                if(prevAliasName!=aliasName){//if Alias name also modified in this case we need to to update ShippingAddrsStore and billingAliasNameArr
                    for(i=0;i<this.shippingAliasNameArr.length;i++){
                        if(this.shippingAliasNameArr[i]==prevAliasName ){
                            this.shippingAliasNameArr.splice(i,1);
                            this.shippingAliasNameArr.push(aliasName);
                            break;
                        }
                    } 
                    
                    for(var i=0;i<this.addressDetailArr.length;i++){//removed entry for revious alise name from address array
                        var addrRec=this.addressDetailArr[i];
                         if(addrRec!="" && addrRec!=undefined && addrRec.aliasNameID==prevAliasName){
                              this.addressDetailArr.splice(i,1);
                              break;
                         }
                    }
                   
                    this.ShippingAddrsStore.getAt(comboRecIndex).set("name",aliasName);//updating store with new alias name
                    this.ShippingAddrsStore.getAt(comboRecIndex).set("id",aliasName);//updating store with new alias name
                    this.ShippingAddrsStore.commitChanges();
                    
                    this.shippingAliasNameArr.push(aliasName);
                    this.addAddressDetails(false,aliasName);
                } 
                
                   var count=1;
                   var isBalnkAddress=false;
                   var recordIndex=-1;
                   do{
                        recordIndex=WtfGlobal.searchRecordIndex(this.ShippingAddrsStore,"Shipping Address"+count,"name");
                        if(recordIndex>=0){
                            var i=0;
                            for(i=0;i<this.addressDetailArr.length;i++){ //updating  this.addressDetailArr with modified addresses
                                var addrRec=this.addressDetailArr[i];
                                if(addrRec!="" && addrRec!=undefined && addrRec.aliasNameID=="Shipping Address"+count && !addrRec.isBillingAddress){
                                    count++;
                                    break;
                                }
                                
                            }
                            if(this.addressDetailArr.length==i && isBalnkAddress==false){
                                    isBalnkAddress=true;
                                    recordIndex=-1;
                            }
                        }
                    }while(recordIndex!=-1);
                        if(isBalnkAddress==false){
                            var rec=new Wtf.data.Record({
                            id:"Shipping Address"+count,
                            name:"Shipping Address"+count
                        });

                        this.ShippingAddrsStore.add(rec);//inserting new Billing Address     
                        this.ShippingAddrsStore.commitChanges();
                        this.shippingAddrsCombo.setValue("Shipping Address"+count);
                        this.resetShipping();
                        this.shippingAliasName.setValue("Shipping Address"+count);
                    }else{
                        this.shippingAddrsCombo.setValue("Shipping Address"+count);
                        this.resetShipping();
                        this.shippingAliasName.setValue("Shipping Address"+count);
                    }
                    
                    
                
             }    
        } else {
            WtfComMsgBox(2, 2);  
        }
    },
    
    addAliasNameToBillingStore:function(){
        if(this.readOnly){
            return;
        }
        var isValid=true;
        if(!this.billingAliasName.isValid()){
            isValid=false;
            this.billingAliasName.markInvalid();
        }
        if(!this.billingAddress.isValid()){
            isValid=false;
            this.billingAddress.markInvalid();
        }
        if(isValid){
            var totalRecord=this.billingAddrsStore.getCount();
            var aliasName=this.billingAliasName.getValue();
            

            var comboRecIndex=WtfGlobal.searchRecordIndex(this.billingAddrsStore,this.billingAddrsCombo.getValue(),"id");
            if(comboRecIndex>=0){
                var prevAliasName="";
                for(var i=0;i<this.addressDetailArr.length;i++){ //updating  this.addressDetailArr with modified addresses
                    var addrRec=this.addressDetailArr[i];
                    if(addrRec!="" && addrRec!=undefined && addrRec.aliasNameID==this.billingAddrsCombo.getValue() && addrRec.isBillingAddress){
                        prevAliasName=addrRec.aliasName;
                        this.addressDetailArr.splice(i,1);
                        this.addAddressDetails(true,this.billingAddrsCombo.getValue());
                        break; //once the record found no need to iterate
                    }
                }
                
                if(prevAliasName!=aliasName){//if Alias name also modified in this case we need to to update billingAddrsStore and billingAliasNameArr
                    for(i=0;i<this.billingAliasNameArr.length;i++){
                        if(this.billingAliasNameArr[i]==prevAliasName ){
                            this.billingAliasNameArr.splice(i,1);
                            this.billingAliasNameArr.push(aliasName);
                            break;
                        }
                    } 
                    for(var i=0;i<this.addressDetailArr.length;i++){//removed entry for revious alise name from address array
                        var addrRec=this.addressDetailArr[i];
                         if(addrRec!="" && addrRec!=undefined && addrRec.aliasNameID==prevAliasName){
                              this.addressDetailArr.splice(i,1);
                              break;
                         }
                    }
                    this.billingAddrsStore.getAt(comboRecIndex).set("name",aliasName);//updating store with new alias name
                    this.billingAddrsStore.getAt(comboRecIndex).set("id",aliasName);//updating store with new alias name
                    this.billingAddrsStore.commitChanges();

                    this.billingAliasNameArr.push(aliasName);//pushing alias name in array. this array is used to check unique name for alias name
                    this.addAddressDetails(true,aliasName);

                }

                   var count=1;
                   var isBalnkAddress=false;
                   var recordIndex=-1;
                   do{
                        recordIndex=WtfGlobal.searchRecordIndex(this.billingAddrsStore,"Billing Address"+count,"name");
                        if(recordIndex>=0){
                            var i=0;
                            for(i=0;i<this.addressDetailArr.length;i++){ //updating  this.addressDetailArr with modified addresses
                                var addrRec=this.addressDetailArr[i];
                                if(addrRec!="" && addrRec!=undefined && addrRec.aliasNameID=="Billing Address"+count && addrRec.isBillingAddress){
                                    //recordIndex=0;
                                    count++;
                                    break;
                                }
                                
                            }
                            if(this.addressDetailArr.length==i && isBalnkAddress==false){
                                    isBalnkAddress=true;
                                    recordIndex=-1;
                            }
                        }
                    }while(recordIndex!=-1);
                        if(isBalnkAddress==false){
                            var rec=new Wtf.data.Record({
                            id:"Billing Address"+count,
                            name:"Billing Address"+count
                        });

                        this.billingAddrsStore.add(rec);//inserting new Billing Address  
                        this.billingAddrsStore.commitChanges();
                        this.billingAddrsCombo.setValue("Billing Address"+count);
                        this.resetBilling();
                        this.billingAliasName.setValue("Billing Address"+count);
                    }else{
                        this.billingAddrsCombo.setValue("Billing Address"+count);
                        this.resetBilling();
                        this.billingAliasName.setValue("Billing Address"+count);
                    }
                    
            }
        } else {
            WtfComMsgBox(2, 2);
        }
    },
    addAddressDetails:function(isBillingAddr,aliasNameID){    
        /*
         * Check for State field as Combobox or Textfield
         */
          var stateAsComboFlag;
            var customerVendorType = this.getCustomerVendorType();
            stateAsComboFlag = WtfGlobal.isIndiaCountryAndGSTApplied() && (customerVendorType == undefined || !(customerVendorType.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.Export || customerVendorType.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.ExportWOPAY || customerVendorType.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.Import));
            if (this.custVenId != undefined) {
                 stateAsComboFlag = this.custVenId;
            }
        if(isBillingAddr){
             if(this.defaultBillingAddress.getValue()){
                 this.defaultBillingAddressID=aliasNameID;
             }     
            var billingRec={
                aliasNameID:aliasNameID,
                aliasName:this.billingAliasName.getValue(),
                address:this.billingAddress.getValue(),
                county:WtfGlobal.isUSCountryAndGSTApplied()?this.billingCounty.getRawValue():this.billingCounty.getValue(),
                city:WtfGlobal.isUSCountryAndGSTApplied()?this.billingCity.getRawValue():this.billingCity.getValue(),
                state:stateAsComboFlag||WtfGlobal.isUSCountryAndGSTApplied()?this.billingStateCombo.getRawValue():this.billingState.getValue(),
                stateCode:this.billingStateCode.getValue(),
                country:this.billingCountry.getValue(),
                postalCode:this.billingPostal.getValue(),
                phone:this.billingPhone.getValue(),
                mobileNumber:this.billingMobile.getValue(),
                fax:this.billingFax.getValue(),
                emailID:this.billingEmail.getValue(),
                recipientName:this.billingRecipientName.getValue(),
                contactPerson:this.billingContactPerson.getValue(),
                contactPersonNumber:this.billingContactNumber.getValue(),               
                contactPersonDesignation:this.billingContactDesignation.getValue(),
                website:this.billingWebsite.getValue(),
                isBillingAddress:isBillingAddr
            }
            this.addressDetailArr.push(billingRec);
        } else {
               if(this.defaultShippingAddress.getValue()){ //updating default address variable 
                 this.defaultShippingAddressID=aliasNameID; 
               }                
             var shippingRec={
                aliasNameID:aliasNameID,
                aliasName:this.shippingAliasName.getValue(),
                address:this.shippingAddress.getValue(),
                county:WtfGlobal.isUSCountryAndGSTApplied()?this.shippingCounty.getRawValue():this.shippingCounty.getValue(),
                city:WtfGlobal.isUSCountryAndGSTApplied()?this.shippingCity.getRawValue():this.shippingCity.getValue(),
                state:stateAsComboFlag||WtfGlobal.isUSCountryAndGSTApplied()?this.shippingStateCombo.getRawValue():this.shippingState.getValue(),
                stateCode:this.shippingStateCode.getValue(),
                country:this.shippingCountry.getValue(),
                postalCode:this.shippingPostal.getValue(),
                phone:this.shippingPhone.getValue(),
                mobileNumber:this.shippingMobile.getValue(),
                fax:this.shippingFax.getValue(),
                emailID:this.shippingEmail.getValue(),
                recipientName:this.shippingRecipientName.getValue(),
                contactPerson:this.shippingContactPerson.getValue(),
                contactPersonNumber:this.shippingContactNumber.getValue(),
                contactPersonDesignation:this.shippingContactDesignation.getValue(),
                website:this.shippingWebsite.getValue(),
                shippingRoute:this.shippingRoute.getValue(),
                isBillingAddress:isBillingAddr
            }
            this.addressDetailArr.push(shippingRec);
        }        
    },    
    onShippingComboSelect:function(combo,record,index){
       if(combo.getValue()==this.shippingComboValueBeforeSelect){ //If same name selected no need to do any action 
           return;
       }
         /*
         * Check for State field as Combobox or Textfield
         */
         var stateAsComboFlag;
            var customerVendorType = this.getCustomerVendorType();
            stateAsComboFlag = WtfGlobal.isIndiaCountryAndGSTApplied() && (customerVendorType == undefined || !(customerVendorType.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.Export || customerVendorType.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.ExportWOPAY || customerVendorType.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.Import));
           if (this.custVenId != undefined) {
                 stateAsComboFlag = this.custVenId;
            }
        var isRecordFound=false;
        for(var i=0;i<this.addressDetailArr.length;i++){
            var rec=this.addressDetailArr[i];
            if(rec!="" && rec!=undefined && rec.aliasNameID==combo.getValue() && !rec.isBillingAddress){
                this.shippingAliasName.setValue(rec.aliasName);
                this.shippingAddress.setValue(rec.address);
                 if ( WtfGlobal.isUSCountryAndGSTApplied()) {
                    this.shippingCounty.setValForRemoteStore(rec.county, rec.county);
                } else {
                    this.shippingCounty.setValue(rec.county);
                }
                 if ( WtfGlobal.isUSCountryAndGSTApplied()) {
                    this.shippingCity.setValForRemoteStore(rec.city, rec.city);
                } else {
                    this.shippingCity.setValue(rec.city);
                }
                 if ((WtfGlobal.isIndiaCountryAndGSTApplied() && stateAsComboFlag) ||  WtfGlobal.isUSCountryAndGSTApplied()) {
                    this.shippingStateCombo.setValForRemoteStore(rec.state, rec.state);
                } else {
                    this.shippingState.setValue(rec.state);
                }
                    
                this.shippingStateCode.setValue(rec.stateCode);
                this.shippingCountry.setValue(rec.country);
                this.shippingPostal.setValue(rec.postalCode);
                this.shippingPhone.setValue(rec.phone);
                this.shippingMobile.setValue(rec.mobileNumber);
                this.shippingFax.setValue(rec.fax);
                this.shippingEmail.setValue(rec.emailID);
                this.shippingRecipientName.setValue(rec.recipientName);
                this.shippingContactPerson.setValue(rec.contactPerson);                
                this.shippingContactNumber.setValue(rec.contactPersonNumber);
                this.shippingContactDesignation.setValue(rec.contactPersonDesignation);
                this.shippingWebsite.setValue(rec.website);
                this.shippingRoute.setValue(rec.shippingRoute);
                this.defaultShippingAddress.setValue(this.defaultShippingAddressID==rec.aliasNameID?true:false);
                isRecordFound=true;
                break;
            }
            if(!isRecordFound){
                this.resetShipping();
                this.shippingAliasName.setValue(record.data.name);
            }
        }        
    },
        
    
    onBillingComboSelect:function(combo,record,index){   
        if(combo.getValue()==this.billingComboValueBeforeSelect){ //If same name selected no need to do any action 
            return;
        }
        /*
         * Check for State field as Combobox or Textfield
         */
         var stateAsComboFlag;
            var customerVendorType = this.getCustomerVendorType();
            stateAsComboFlag = WtfGlobal.isIndiaCountryAndGSTApplied() && (customerVendorType == undefined || !(customerVendorType.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.Export || customerVendorType.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.ExportWOPAY || customerVendorType.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.Import));
            if (this.custVenId != undefined) {
                 stateAsComboFlag = this.custVenId;
            }
        var isRecordFound=false;
        for(var i=0;i<this.addressDetailArr.length;i++){
            var rec=this.addressDetailArr[i];
            if(rec!="" && rec!=undefined && rec.aliasNameID==combo.getValue() && rec.isBillingAddress){
                this.billingAliasName.setValue(rec.aliasName);
                this.billingAddress.setValue(rec.address);
                if ( WtfGlobal.isUSCountryAndGSTApplied()) {
                    this.billingCounty.setValForRemoteStore(rec.county, rec.county);
                } else {
                    this.billingCounty.setValue(rec.county);
                }
                 if ( WtfGlobal.isUSCountryAndGSTApplied()) {
                    this.billingCity.setValForRemoteStore(rec.city, rec.city);
                } else {
                    this.billingCity.setValue(rec.city);
                }
                if ((WtfGlobal.isIndiaCountryAndGSTApplied() && stateAsComboFlag)||  WtfGlobal.isUSCountryAndGSTApplied()) {
                    this.billingStateCombo.setValForRemoteStore(rec.state, rec.state);
                } else {
                    this.billingState.setValue(rec.state);
                }
                this.billingStateCode.setValue(rec.state);
                this.billingCountry.setValue(rec.country);
                this.billingPostal.setValue(rec.postalCode);
                this.billingPhone.setValue(rec.phone);
                this.billingMobile.setValue(rec.mobileNumber);
                this.billingFax.setValue(rec.fax);
                this.billingEmail.setValue(rec.emailID);
                this.billingRecipientName.setValue(rec.recipientName);
                this.billingContactPerson.setValue(rec.contactPerson);                
                this.billingContactNumber.setValue(rec.contactPersonNumber);
                this.billingContactDesignation.setValue(rec.contactPersonDesignation);
                this.billingWebsite.setValue(rec.website);
                this.defaultBillingAddress.setValue(this.defaultBillingAddressID==rec.aliasNameID?true:false);  
                isRecordFound=true;
                break;
            }
        }
        if(!isRecordFound){
            this.resetBilling();
            this.billingAliasName.setValue(record.data.name);
         }
    },
    
    resetBilling:function(){
        this.billingAliasName.setValue("");
        this.billingAliasName.clearInvalid();
        this.billingAddress.setValue("");
        this.billingAddress.clearInvalid();
        var stateAsComboFlag;
        var customerVendorType = this.getCustomerVendorType();
        stateAsComboFlag = WtfGlobal.isIndiaCountryAndGSTApplied() && (customerVendorType == undefined || !(customerVendorType.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.Export || customerVendorType.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.ExportWOPAY || customerVendorType.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.Import));
        if (this.custVenId != undefined) {
            stateAsComboFlag = this.custVenId;
        }
        if (WtfGlobal.isUSCountryAndGSTApplied()) {
            this.billingCounty.setValForRemoteStore(" ", " ");
        } else {
            this.billingCounty.setValue("");
        }
        if (WtfGlobal.isUSCountryAndGSTApplied()) {
            this.billingCity.setValForRemoteStore(" ", " ");
        } else {
            this.billingCity.setValue("");
        }
        if ((WtfGlobal.isIndiaCountryAndGSTApplied() && stateAsComboFlag) || WtfGlobal.isUSCountryAndGSTApplied()) {
            this.billingStateCombo.setValForRemoteStore(" ", " ");
        } else {
            this.billingState.setValue("");
        }
        this.billingStateCode.setValue("");
        this.billingCountry.setValue("");
        this.billingPostal.setValue("");
        this.billingPhone.setValue("");
        this.billingMobile.setValue("");
        this.billingFax.setValue("");
        this.billingEmail.setValue("");
        this.billingRecipientName.setValue("");
        this.billingContactPerson.setValue("");
        this.billingContactNumber.setValue("");
        this.billingContactDesignation.setValue("");
        this.billingWebsite.setValue("");
        this.defaultBillingAddress.setValue(false);        
    },
    resetShipping:function(){
        this.shippingAliasName.setValue("");
        this.shippingAliasName.clearInvalid();
        this.shippingAddress.setValue("");
        this.shippingAddress.clearInvalid();
        /*
         * Check for State field as Combobox or Textfield
         */
        var stateAsComboFlag;
        var customerVendorType = this.getCustomerVendorType();
        stateAsComboFlag = WtfGlobal.isIndiaCountryAndGSTApplied() && (customerVendorType == undefined || !(customerVendorType.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.Export || customerVendorType.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.ExportWOPAY || customerVendorType.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.Import));
        if (this.custVenId != undefined) {
            stateAsComboFlag = this.custVenId;
        }
        if (WtfGlobal.isUSCountryAndGSTApplied()) {
            this.shippingCounty.setValForRemoteStore(" ", " ");
        } else {
            this.shippingCounty.setValue("");
        }
        if (WtfGlobal.isUSCountryAndGSTApplied()) {
            this.shippingCity.setValForRemoteStore(" ", " ");
        } else {
            this.shippingCity.setValue("");
        }
        if ((WtfGlobal.isIndiaCountryAndGSTApplied()&& stateAsComboFlag)|| WtfGlobal.isUSCountryAndGSTApplied()) {
            this.shippingStateCombo.setValForRemoteStore(" ", " ");
        } else {
            this.shippingState.setValue("");
        }
        this.shippingStateCode.setValue("");
        this.shippingCountry.setValue("");
        this.shippingPostal.setValue("");
        this.shippingPhone.setValue("");
        this.shippingMobile.setValue("");
        this.shippingFax.setValue("");
        this.shippingEmail.setValue("");
        this.shippingRecipientName.setValue("");
        this.shippingContactPerson.setValue("");
        this.shippingContactNumber.setValue("");
        this.shippingContactDesignation.setValue("");
        this.shippingWebsite.setValue("");
        this.shippingRoute.setValue("");
        this.defaultShippingAddress.setValue(false);        
    },
    copyBillingAddress:function(checkBox,value){
         /*
         * Check for State field as Combobox or Textfield
         */
        var stateAsComboFlag;
        var customerVendorType = this.getCustomerVendorType();
        stateAsComboFlag = WtfGlobal.isIndiaCountryAndGSTApplied() && (customerVendorType == undefined || !(customerVendorType.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.Export || customerVendorType.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.ExportWOPAY || customerVendorType.data.defaultMasterItem == Wtf.GSTCUSTVENTYPE.Import));
        if (this.custVenId != undefined) {
            stateAsComboFlag = this.custVenId;
        }
        if (value == true) {
            this.shippingAliasName.setValue(this.billingAliasName.getValue());
            this.shippingAddress.setValue(this.billingAddress.getValue());
            if (WtfGlobal.isUSCountryAndGSTApplied()) {
                this.shippingCounty.setValForRemoteStore(this.billingCounty.getRawValue(), this.billingCounty.getRawValue());
            } else {
                this.shippingCounty.setValue(this.billingCounty.getValue());
            }
            if (WtfGlobal.isUSCountryAndGSTApplied()) {
                this.shippingCity.setValForRemoteStore(this.billingCity.getRawValue(), this.billingCity.getRawValue());
            } else {
                this.shippingCity.setValue(this.billingCity.getValue());
            }
            if ((WtfGlobal.isIndiaCountryAndGSTApplied() && stateAsComboFlag) || WtfGlobal.isUSCountryAndGSTApplied()) {
                this.shippingStateCombo.setValForRemoteStore(this.billingStateCombo.getRawValue(), this.billingStateCombo.getRawValue());
            } else {
                this.shippingState.setValue(this.billingState.getValue());
            }
            this.shippingStateCode.setValue(this.billingStateCode.getValue());
            this.shippingCountry.setValue(this.billingCountry.getValue());
            this.shippingPostal.setValue(this.billingPostal.getValue());
            this.shippingPhone.setValue(this.billingPhone.getValue());
            this.shippingMobile.setValue(this.billingMobile.getValue());
            this.shippingFax.setValue(this.billingFax.getValue());
            this.shippingEmail.setValue(this.billingEmail.getValue());
            this.shippingRecipientName.setValue(this.billingRecipientName.getValue());
            this.shippingContactPerson.setValue(this.billingContactPerson.getValue());
            this.shippingContactNumber.setValue(this.billingContactNumber.getValue()); 
            this.shippingContactDesignation.setValue(this.billingContactDesignation.getValue()); 
            this.shippingWebsite.setValue(this.billingWebsite.getValue()); 
        } else if(value==false){
            this.shippingAliasName.setValue("");
            this.shippingAliasName.clearInvalid();
            this.shippingAddress.setValue("");
            this.shippingAddress.clearInvalid();
            this.shippingCounty.setValue("");
            this.shippingCity.setValue("");
            this.shippingState.setValue("");
            this.shippingStateCode.setValue("");
            this.shippingCountry.setValue("");
            this.shippingPostal.setValue("");
            this.shippingPhone.setValue("");
            this.shippingMobile.setValue("");
            this.shippingFax.setValue("");
            this.shippingEmail.setValue("");
            this.shippingRecipientName.setValue("");
            this.shippingContactPerson.setValue("");
            this.shippingContactNumber.setValue("");           
            this.shippingContactDesignation.setValue(""); 
            this.shippingWebsite.setValue(""); 
        } 
    },
    saveForm:function(){
        var isValid=this.addressForm.getForm().isValid(); 
        if(isValid){ 
            if(!this.isCompany){//Below code checks wheather customer/vendor personal detail saved or not, if not saved then it give alert. So it is not required in case of company address details. 
                if(this.isEdit || this.addAddressFromTransactions){
                    this.personId=this.record.data.accid;
                    this.isPropagatedPersonalDetails=this.record.data.isPropagatedPersonalDetails;
                }else{
                    var maintabid = this.isCustomer ?"personalDetailCustomerTab":"personalDetailVendorTab";
                    if( this.isEdit != undefined && this.isEdit){
                        maintabid = "edit-"+maintabid ;
                    }else if(this.isCopy != undefined && this.isCopy){
                        maintabid = "copy-"+maintabid;
                    }
                    this.isPropagatedPersonalDetails=this.isCustomer?Wtf.getCmp(maintabid).isPropagatedPersonalDetails :Wtf.getCmp(maintabid).isPropagatedPersonalDetails ; 
                    this.personId=this.isCustomer?Wtf.getCmp(maintabid).personId :Wtf.getCmp(maintabid).personId ;  
                } 
                if(this.personId=="" || this.personId==undefined){
                    var addressMsg=WtfGlobal.getLocaleText("acc.field.PleaseenterthePersonaldetailsfirst");
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),addressMsg], 2);
                    return;
                }  
            }
            var URL="";
            if(this.isCompany){
                URL="ACCCompanyPref/saveCompanyAddressDetails.do";
            } else if(this.isCustomer){
                 URL="ACCCustomerCMN/saveCustomerAddresses.do";
            } else {
                 URL="ACCVendorCMN/saveVendorAddresses.do";
            }
            var jsonArray=this.getFinalAddressJsonArray();
                        Wtf.Ajax.requestEx({
                url:URL,
                params:{
                    addressDetail:jsonArray,
                    accid:this.personId,
                    isaddresses:true,
                    ispropagatetochildcompanyflag:this.isPropagatedPersonalDetails//if personal details are propagated then propagate address details also in child companies.
                            }
            },this,this.genSuccessResponse,this.genFailureResponse);
        } else{
            WtfComMsgBox(2,2);
            return;
        } 
//        this.save.disable();
    },
    
    getFinalAddressJsonArray:function(){
        var totalRecord=this.billingAddrsStore.getCount();
        var comboRecIndex=WtfGlobal.searchRecordIndex(this.billingAddrsStore,this.billingAddrsCombo.getValue(),"id");
        if(comboRecIndex==(totalRecord-1)){//It will be true if currently selected record in combo is new record i.e. (Billing Address1,Billing Address2 etc)                
            this.addAddressDetails(true,this.billingAddrsCombo.getValue());
        } else {
            for(var i=0;i<this.addressDetailArr.length;i++){ //updating  this.addressDetailArr with modified addresses
                var addrRec=this.addressDetailArr[i];
                if(addrRec!="" && addrRec!=undefined && addrRec.aliasNameID==this.billingAddrsCombo.getValue() && addrRec.isBillingAddress){                     
                    this.addressDetailArr.splice(i,1);
                    this.addAddressDetails(true,this.billingAddrsCombo.getValue());
                    break; //once the record found no need to iterate
                }
            }
        }
        
        totalRecord=this.ShippingAddrsStore.getCount();       
        comboRecIndex=WtfGlobal.searchRecordIndex(this.ShippingAddrsStore,this.shippingAddrsCombo.getValue(),"id");
        if(comboRecIndex==(totalRecord-1)){//It will be true if currently selected record in combo is new record i.e. (Shipping Address1,Shipping Address2 etc)                
            this.addAddressDetails(false,this.shippingAddrsCombo.getValue());
        } else {
            for(i=0;i<this.addressDetailArr.length;i++){ //updating  this.addressDetailArr with modified addresses
                addrRec=this.addressDetailArr[i];
                if(addrRec!="" && addrRec!=undefined && addrRec.aliasNameID==this.shippingAddrsCombo.getValue() && !addrRec.isBillingAddress){                        
                    this.addressDetailArr.splice(i,1);
                    this.addAddressDetails(false,this.shippingAddrsCombo.getValue());
                    break; //once the record found no need to iterate
                }
            }
        }  
        for(i=0;i<this.addressDetailArr.length;i++){
            if(this.addressDetailArr[i].isBillingAddress){ //setting default billing address
                if(this.addressDetailArr[i].aliasNameID==this.defaultBillingAddressID){
                    this.addressDetailArr[i].isDefaultAddress=true;
                } else {
                    this.addressDetailArr[i].isDefaultAddress=false;
                }
            } else { //setting default shipping address
                if(this.addressDetailArr[i].aliasNameID==this.defaultShippingAddressID){
                    this.addressDetailArr[i].isDefaultAddress=true;
                } else {
                    this.addressDetailArr[i].isDefaultAddress=false;
                }
            }            
        }
      var jsonArray = JSON.stringify(this.addressDetailArr);
      return jsonArray;
    },

    genSuccessResponse:function(response){
        if(!this.isCompany && !this.addAddressFromTransactions){// Below code is for Closable after saving customer/vendor, but it not required when componet call from company and add from transactions
            var maintabid = this.isCustomer?"mainCustomerPanel":"mainVendorPanel";
            if( this.isEdit != undefined && this.isEdit){
                maintabid = "edit-"+maintabid;
            }else if(this.isCopy != undefined && this.isCopy){
                maintabid = "copy-"+maintabid;
            }
            if (this.isCustomer) {
                Wtf.getCmp(maintabid).isClosable = true
            } else {
                Wtf.getCmp(maintabid).isClosable = true
            }
        }
        
        this.disableComponent();
            var perAccID=this.personId;
            this.isAddressDetailSaved=true; 
            this.fireEvent('update',this,perAccID);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),response.msg],response.success*2+1);
    },
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.save.enable();
    },
    disableComponent:function(){
        this.save.disable();
        this.deletebttn.disable();
        if (this.validateAddressBttn) {
            this.validateAddressBttn.disable()
        };
        this.billingAddrsCombo.disable();
        this.billingAliasName.disable();
        this.billingAddress.disable();
        this.billingCounty.disable();
        this.billingCity.disable();
        this.billingStateCombo.disable();
        this.billingState.disable();
        this.billingStateCode.disable();
        this.billingCountry.disable();
        this.billingPostal.disable();
        this.billingPhone.disable();
        this.billingMobile.disable();
        this.billingFax.disable();
        this.billingEmail.disable();
        this.billingRecipientName.disable();
        this.billingContactPerson.disable();
        this.billingContactNumber.disable();  
        this.billingContactDesignation.disable();  
        this.billingWebsite.disable();  
        this.defaultBillingAddress.disable();
        this.copyAddress.disable();
        this.shippingAddrsCombo.disable();
        this.shippingAliasName.disable();
        this.shippingAddress.disable();
        this.shippingCounty.disable();
        this.shippingCity.disable();
        this.shippingStateCombo.disable();
        this.shippingState.disable();
        this.shippingStateCode.disable();
        this.shippingCountry.disable();
        this.shippingPostal.disable();
        this.shippingPhone.disable();
        this.shippingMobile.disable();
        this.shippingFax.disable();
        this.shippingEmail.disable();
        this.shippingRecipientName.disable();
        this.shippingContactPerson.disable();
        this.shippingContactNumber.disable();
        this.shippingContactDesignation.disable();
        this.shippingWebsite.disable();
        this.shippingRoute.disable();
        this.defaultShippingAddress.disable();
    },
   DeleteAdderssWindow: function(){
        this.addressDeleteWindow = new Wtf.account.AddressDeleteWindow({
            id: 'DeleteAdderssWindow',
            title: WtfGlobal.getLocaleText("acc.deleteaddress"),
            border: false,
            isCustomer:this.isCustomer,
            record:this.record,
            addressDetails:this.record.data.addressDetails,
            addressDetailArr:this.addressDetailArr,
            scope:this,
            closable: true,
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deletebutton),
            resizable: false,
            renderTo: document.body
        });
        this.addressDeleteWindow.show(); 
        } 
});

//**********************************************************************************************************
//                   Address Delete Component
//**********************************************************************************************************
Wtf.account.AddressDeleteWindow = function(config) {
    this.label=config.isCustomer?"Customer address":"Vendor address";
    this.record=config.record;
    this.isCustomer=config.isCustomer;
    this.addressDetails = config.addressDetails;
    this.addressDetailArr=config.addressDetailArr;
    this.butnArr = new Array();
    this.isSubmitBtnClicked = false;  
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"), //'Delete',
        scope: this,
        handler: function() {
            this.isSubmitBtnClicked = true;
            this.deleteRecords();
        }
    }, {
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"), //'Cancel',
        scope: this,
        handler: function() {
            this.isSubmitBtnClicked = false;
            this.close();
        }
    });
    Wtf.apply(this, {
        buttons: this.butnArr
    }, config);
    Wtf.account.AddressDeleteWindow.superclass.constructor.call(this, config);  
}
Wtf.extend(Wtf.account.AddressDeleteWindow, Wtf.Window, {
    height: 450,
    width: 800,
    modal: true,
    iconCls : getButtonIconCls(Wtf.etype.deletebutton),
    onRender: function(config) {
     Wtf.account.AddressDeleteWindow.superclass.onRender.call(this, config);
        this.createDisplayGrid();
        this.add(
        {
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.grid
        });   
        
    },
    createDisplayGrid: function() {
       
      this.sm = new Wtf.grid.CheckboxSelectionModel({
           singleSelect :false
        });
        this.cm = new Wtf.grid.ColumnModel([this.sm,{
                dataIndex:'rowid',
                hidelabel:true,
                hidden:true
            },{
                dataIndex:'addressType',
                hidelabel:true,
                hidden:true
            },{
               header: WtfGlobal.getLocaleText("acc.deleteaddress.AliasName"), //"Alias Name"
                dataIndex: 'aliasName',
                width: 150
            },{
               header: WtfGlobal.getLocaleText("acc.deleteaddress.defaultAddress"), //"Default Address"
                dataIndex: 'defaultAddress',
                width: 100
            },{
                  header: WtfGlobal.getLocaleText("acc.deleteaddress.Address"),//"Address",
                  dataIndex:'address',
                width: 300
            },{
               header: WtfGlobal.getLocaleText("acc.deleteaddress.City"), //"City"
                dataIndex: 'city',
                width: 100
            },{
               header: WtfGlobal.getLocaleText("acc.deleteaddress.State"), //"State"
                dataIndex: 'state',
                width: 100
            },{
                  header: WtfGlobal.getLocaleText("acc.deleteaddress.Country"),//"Country",
                  dataIndex:'country',
                width: 100
            },{
               header: WtfGlobal.getLocaleText("acc.deleteaddress.Postal"), //"Postal"
                dataIndex: 'postal',
                width: 100
            },{
                  header: WtfGlobal.getLocaleText("acc.deleteaddress.Phone"),//"Phone",
                  dataIndex:'phone',
                  width: 100
            },{
               header: WtfGlobal.getLocaleText("acc.deleteaddress.Mobile"), //"Mobile"
                dataIndex: 'mobile',
                width: 100
            },{
                  header: WtfGlobal.getLocaleText("acc.deleteaddress.Fax"),//"Fax",
                  dataIndex:'fax',
                width: 100
            },{
               header: WtfGlobal.getLocaleText("acc.deleteaddress.Email"), //"Email"
                dataIndex: 'email',
                width: 100
            },{
                  header: WtfGlobal.getLocaleText("acc.deleteaddress.RecipientName"),//"RecipientName",
                  dataIndex:'recipientName',
                  width: 100
            },{
               header: WtfGlobal.getLocaleText("acc.deleteaddress.ContactPerson"), //"Contact Person"
                dataIndex: 'contactPerson',
                width: 100
            },{
                  header: WtfGlobal.getLocaleText("acc.deleteaddress.ContactPersonNumber"),//"Contact Person Number",
                  dataIndex:'contactPersonNumber',
                  width: 150
            },{
                  header: WtfGlobal.getLocaleText("acc.deleteaddress.ContactPersonDesignation"),//"Contact Person Designation",
                  dataIndex:'contactPersonDesignation',
                  width: 150
            },{
                  header: WtfGlobal.getLocaleText("acc.deleteaddress.Website"),//"ContactPersonNumber",
                  dataIndex:'website',
                  width: 150
            }]);
       
        this.Rec = Wtf.data.Record.create([
            {name: 'rowid'},
            {name: 'aliasName'},
            {name: 'address'},
            {name: 'city'},
            {name: 'state'},
            {name: 'country'},
            {name: 'postal'},
            {name: 'phone'},
            {name: 'mobile'},
            {name: 'fax'},
            {name: 'email'},
            {name: 'recipientName'},
            {name: 'contactPerson'},
            {name: 'contactPersonNumber'},
            {name: 'contactPersonDesignation'},
            {name: 'website'},
            {name: 'isBillingAddress'},
            {name: 'addressType'},
            {name: 'defaultAddress'},
            {name: 'isDefaultAddress'},
            {name: 'shippingRoute'},
            
        ]);
        this.gridStore = new Wtf.data.GroupingStore({
            sortInfo : {
                field : 'addressType',
                direction : 'ASC'
            },
            groupField :'addressType',
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            }, this.Rec)
            
        });
       
       this.gridView1 = new Wtf.grid.GroupingView({
            forceFit:false,
            showGroupName: false,
            enableNoGroups:true, // REQUIRED!
            hideGroupedColumn: false,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        });
          var gridSummary = new Wtf.grid.GroupSummary({});
        this.grid = new Wtf.grid.GridPanel({
            stripeRows :true,
            store: this.gridStore,
            height: 385,
            autoScroll: true,
            autoWidth:true,
            cm: this.cm,
            sm: this.sm,
            border: false,
            loadMask: true,
            viewConfig: this.gridView1,
            plugins: [gridSummary],
            forceFit:false
        });
        
        this.sm.on("selectionchange",this.selectRecords.createDelegate(this),this);
        this.loadRecord();
    },
    selectRecords: function(){
        if(this.sm.getCount()>=1){
            //         var rec = this.sm.getSelections();
            this.recArr = this.grid.getSelectionModel().getSelections();
            for(var i=0;i<this.recArr.length;i++){
                var rec = this.recArr[i];
                if(rec.data.isDefaultAddress!=undefined && rec.data.isDefaultAddress==true){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.defaultaddresscannotbedeleted")], 2);
                    var recIndex=-1;
                    for(var count=0;count<this.gridStore.getCount();count++){
                        var addRec= this.gridStore.getAt(count);
                        if(addRec.data.aliasName==rec.data.aliasName && addRec.data.isBillingAddress==rec.data.isBillingAddress){
                            recIndex = count;
                            break;
                        }
                    }
                    if(recIndex >=0){
                        this.sm.deselectRow(recIndex);
                        return;
                    }
                }
            
            } 
            
        }   
      
    },
    deleteRecords: function(){
        if(this.grid.getSelectionModel().hasSelection()==false){
            WtfComMsgBox(34,2);
            return;
        }
        var data=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        var idData = "",defalutaddress="";
        for(var i=0;i<this.recArr.length;i++){
            var rec = this.recArr[i];
            if(rec.data.isDefaultAddress!=undefined && rec.data.isDefaultAddress==false){
                idData += "{\"rowid\":\""+rec.get('rowid')+"\",\"aliasName\":\""+rec.get('aliasName')+"\"},";
            }else{
                defalutaddress +=defalutaddress.length>1? (","+rec.get('aliasName')):rec.get('aliasName');
            }
            
        }
        if(idData.length>1){
            idData=idData.substring(0,idData.length-1);
        }
        data="["+idData+"]";
        var msg=defalutaddress.length>1 ? " except Default Address  "+defalutaddress:"";
        if(idData.length>1){
            this.deleteAddressCallAjax(data,msg);
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.address.noaddresstoUpdate")], 2);
        }
        
        

    },
     loadRecord:function(){
        if(this.record!=null){
          var addressRecords=[];  
          var counter=0,billingAddrCount=0,shippingAddrCount=0;
       var addressArray = this.record.data.addressDetails;             
       for(counter=0;counter<addressArray.length;counter++){
                var addresses=addressArray[counter];
                if(addresses.isBillingAddress){
                    billingAddrCount++;
                } else {
                    shippingAddrCount++;
                }
                var addrRec=new this.Rec({
                    aliasNameID:addresses.isBillingAddress?billingAddrCount:shippingAddrCount,
                    aliasName:addresses.aliasName,
                    rowid:addresses.id,
                    address:addresses.address,
                    city:addresses.city,
                    state:addresses.state,
                    country:addresses.country,
                    postal:addresses.postalCode,
                    phone:addresses.phone,
                    mobile:addresses.mobileNumber,
                    fax:addresses.fax,
                    email:addresses.emailID,
                    recipientName:addresses.recipientName,
                    contactPerson:addresses.contactPerson,
                    contactPersonNumber:addresses.contactPersonNumber,
                    contactPersonDesignation:addresses.contactPersonDesignation,
                    website:addresses.website,
                    shippingRoute:addresses.shippingRoute,
                    isBillingAddress:addresses.isBillingAddress,
                    isDefaultAddress:addresses.isDefaultAddress,
                    defaultAddress:addresses.isDefaultAddress?"Yes":"No",
                    addressType:addresses.isBillingAddress?"Billing Address":"Shipping Address"
                });
                addressRecords.push(addrRec);
              
            }
            this.gridStore.add(addressRecords);//adding new Shipping Address    
            this.gridStore.sort("addressType","ASC");
                    
        }
    },
    deleteAddressCallAjax: function(data,msg){
        if(data.length>1){//formRecord.data.isuserallowtoapprove!=undefined && formRecord.data.isuserallowtoapprove==true       
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttodeleteselected") +" "+ (this.isCustomer?WtfGlobal.getLocaleText("acc.vendormanagement.Customeraddress"):WtfGlobal.getLocaleText("acc.vendormanagement.Vendoraddress"))+ msg +  "?", function(btn) {//nonaccessbillno.length>1 ? (" except orders "+nonaccessbillno+"?"):
                if (btn == "yes") {
                    var URL = "";
                    if(this.isCustomer){
                       URL="ACCCustomerCMN/deleteCustomerAdressDetails.do";
                    } else {
                        URL="ACCVendorCMN/deleteVendorAdressDetails.do";
                    }
                    
                    Wtf.Ajax.requestEx({
                        url: URL,
                        params: {
                            accountid:this.record.data.accid,
                            accountcode:this.record.data.acccode,
                            accountname:this.record.data.accname,
                            data:data
                        }
                    }, this, this.genSuccessRespdeleteAddress, this.genFailureRespdeleteAddress);
                }
            }, this)
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.address.noaddresstoUpdate")], 2);
        }
    },
    genSuccessRespdeleteAddress: function(response) {
        if(response.success){
            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), response.msg, function(){
                var tabid=this.isCustomer?'CustomerDetails':'VendorDetails';
                Wtf.getCmp(tabid).Store.reload();
                var maintabid = this.isCustomer?"mainCustomerPanel":"mainVendorPanel";
                if( this.isEdit != undefined && this.isEdit){
                    maintabid = "edit-"+maintabid;
                }else if(this.isCopy != undefined && this.isCopy){
                    maintabid = "copy-"+maintabid;
                }
                var panel = this.isCustomer?Wtf.getCmp(maintabid):Wtf.getCmp(maintabid);
                if(panel!=null){
                    Wtf.getCmp('as').remove(panel);
                    panel = null;
                }
            }, this);
            
            var addressArrayaftedel = this.record.data.addressDetails;
            for (var i = 0; i < this.recArr.length; i++) {
                var rec = this.recArr[i];
                for (var count = 0; count < this.addressDetailArr.length; count++) {
                    var addRec = this.addressDetailArr[count];
                    if (addRec.aliasNameID == rec.data.aliasName) {
                       this.addressDetailArr.splice(count,1);
                       addressArrayaftedel.splice(count,1);
                       break;
                    } 
                }
            }
            this.record.data.addressDetails = addressArrayaftedel;
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 2);
        }
        this.close();
    },
    
    genFailureRespdeleteAddress: function(response) {
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    }
});

