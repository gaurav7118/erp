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
//function editInvoiceExchangeRates(winid,basecurrency,foreigncurrency,exchangerate,exchangeratetype){
//    function showInvoiceExternalExchangeRate(btn,txt){
//        if(btn == 'ok'){
//             if(txt.indexOf('.')!=-1)
//                 var decLength=(txt.substring(txt.indexOf('.'),txt.length-1)).length;
//            if(isNaN(txt)||txt.length>15||decLength>7||txt==0){
//                Wtf.MessageBox.show({
//                    title: WtfGlobal.getLocaleText("acc.setupWizard.curEx"), //'Exchange Rate',
//                    msg: WtfGlobal.getLocaleText("acc.nee.55")+
//                    "<br>"+WtfGlobal.getLocaleText("acc.nee.56")+
//                    "<br>"+WtfGlobal.getLocaleText("acc.nee.57"),
//                    buttons: Wtf.MessageBox.OK,
//                    icon: Wtf.MessageBox.WARNING,
////                    width: 300,
//                    scope: this,
//                    fn: function(){
//                        if(btn=="ok"){
//                            editInvoiceExchangeRates(winid,basecurrency,foreigncurrency,exchangerate,exchangeratetype);
//                        }
//                    }
//                });
//            }else{
//                if(exchangeratetype!=undefined)
//                    Wtf.getCmp(winid).exchangeratetype=exchangeratetype
//                if(exchangeratetype!=undefined&&exchangeratetype=='foreigntobase'){
//                    if((txt*1)>0) {
//                        Wtf.getCmp(winid).revexternalcurrencyrate=txt;
//                        var exchangeRateNormal = 1/((txt*1)-0);
//                        exchangeRateNormal = (Math.round(exchangeRateNormal*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
//                        Wtf.getCmp(winid).externalcurrencyrate=exchangeRateNormal;
//                    } 
//                }else{
//                    Wtf.getCmp(winid).externalcurrencyrate=txt;
//                }
//                Wtf.getCmp(winid).updateFormCurrency();
//            }
//        }
//    }
//    Wtf.MessageBox.prompt(WtfGlobal.getLocaleText("acc.setupWizard.curEx"),'<b>'+WtfGlobal.getLocaleText("acc.nee.58")+'</b> 1 '+basecurrency+' = '+exchangerate+' '+foreigncurrency +
//        '<br><b>'+WtfGlobal.getLocaleText("acc.nee.59")+'</b>', showInvoiceExternalExchangeRate);
//}
// function Showproductdetails(productid,productname,so){
//       callViewProductDetails(productid,'View Product',so,productname);
//}
/*< COMPONENT USED FOR >
 *      1.Credit Vendor Invoice
 *          callGoodsReceipt(isEdit,rec,winid) --- <  >
 *          [isEdit=true/false, isCustomer=false, record]
 *      2.Invoice
 *          callInvoice(isEdit,rec,winid) --- < Create Invoice >
 *          [isEdit=true/false, isCustomer=true, record]
 *      3.Invoice
 *          callBillingInvoice(isEdit,rec,winid) --- < Create Invoice >
 *          [isEdit=true/false, isCustomer=true, isCustBill:true, record]
 *      4.Sales Receipt
 *          callBillingSalesReceipt(isEdit,rec,winid) --- < Create Cash Sales >
 *          [isEdit=true/false, isCustomer=true, isCustBill:true, cash:true, record]
 *      5.Cash Sales
 *          callSalesReceipt(isEdit,rec,winid) --- < Credit Cash Sales >
 *          [isEdit=true/false, isCustomer=true, cash:true, record]
 *      6.Cash Purchase
 *          callPurchaseReceipt(isEdit,rec,winid) --- <  >
 *          [isEdit=true/false, isCustomer=false, cash:true, record]
 *      7.Sales Order
 *          callSalesOrder(isEdit,rec,winid) --- < Create sales Order >
 *          [isEdit=true/false, isCustomer=true, isOrder=true, record]
 *      8.Puchase Order
 *          callPurchaseOrder(isEdit,rec,winid) --- <  >
 *          [isEdit=true/false, isCustomer=false, isOrder=true, record]
 *		9.Quotation
 *          callQuotation() --- < Create Quotation >
 *          [quotation=true, isCustomer=false, isOrder=true]
 *
 *      10.this.appendId --- It is used when this.id is appended in the id of component. This is useful for displaying help.
 */

Wtf.account.FixedAssetTransactionPanel=function(config){
    this.quotation = (config.quotation!=null && config.quotation!=undefined)?config.quotation:false;
    this.isFixedAsset = (config.isFixedAsset!=null && config.isFixedAsset!=undefined)?config.isFixedAsset:false;
    this.isLeaseFixedAsset = (config.isLeaseFixedAsset!=null && config.isLeaseFixedAsset!=undefined)?config.isLeaseFixedAsset:false;
    this.DefaultVendor = config.DefaultVendor;
	this.id=config.id;
	this.titlel = config.title!=undefined?config.title:"null";
    this.dataLoaded=false;
    this.isViewTemplate = (config.isViewTemplate!=undefined?config.isViewTemplate:false);
    this.isTemplate = (config.isTemplate!=undefined?config.isTemplate:false);
    this.createTransactionAlso = false;
    this.transactionType = 0;
    this.recordId = "";
    this.isCopyFromTemplate = (config.isCopyFromTemplate!=undefined?config.isCopyFromTemplate:false);
    this.isOpeningBalanceOrder = (config.isOpeningBalanceOrder!=undefined?config.isOpeningBalanceOrder:false);
    this.templateId = config.templateId;
//    this.isOnTemplateSelect = undefined;
    this.sendMailFlag = false;
    this.isGST=WtfGlobal.GSTApplicableForCompany()==Wtf.GSTStatus.NEW?true:false;   //ERP-32829 
     /**
     * Below variable is used to keep term details of source document 
     * in linking case and edit case when source document contains flat tax value
     * i.e. he has changed tax value manually
     */
    this.keepTermDataInLinkCase=false;  // Used to keep term term details as it is i.e. dont recalculate
    this.CustomerVendorTypeId="";
    this.GSTINRegistrationTypeId="";
    this.gstin="";
    this.gstdochistoryid="";
    this.ignoreHistory=false;
    this.checkgststatus=false;
    this.isIndiaGST=WtfGlobal.isIndiaCountryAndGSTApplied();   //ERP-32829 
    this.isShipping=CompanyPreferenceChecks.getGSTCalCulationType();
    this.uniqueCase=0; 
    this.sezfromdate=0; 
    this.seztodate=0; 
    this.transactiondateforgst=null;
    this.mailFlag = false;
    this.isExpenseInv=false;
    this.isEdit=config.isEdit;
    this.ispurchaseReq=config.ispurchaseReq;
    this.isFromGrORDO = false;
    this.label=config.label;
    this.copyInv=config.copyInv;
    this.isInvoice=config.isInvoice;
    this.billid=null;
    this.custChange=false;
    this.record=config.record;
    this.termsincludegst = Wtf.account.companyAccountPref.termsincludegst;
    this.handleEmptyText=false; //To handle empty text after clicking on save buttonsa
    this.isMultiSelectFlag=false; // keep this flag for allow multiselection
    this.isPOfromSO=config.isPOfromSO;
    this.GENERATE_PO=config.isPOfromSO;
    this.isSOfromPO=config.isSOfromPO;   //This flag is used for creating so from po
    this.GENERATE_SO=config.isSOfromPO;    
    this.datechange=0;
    this.oldval="";this.val="";this.pronamearr=[];
    this.changeGridDetails=true;
    this.appendID = true;
    this.heplmodeid = config.heplmodeid;
    this.response = "";
    this.request = "";
    this.amountdue=0;
    this.CustomStore = "";
    this.termid="";
    this.currentAddressDetailrec="";
    var help=getHelpButton(this,config.heplmodeid);
    this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
    this.custUPermType=config.isCustomer?Wtf.UPerm.customer:Wtf.UPerm.vendor;
    this.custPermType=config.isCustomer?Wtf.Perm.customer:Wtf.Perm.vendor;
    this.soUPermType=(config.isCustomer?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
    this.soPermType=(config.isCustomer?Wtf.Perm.invoice.createso:Wtf.Perm.vendorinvoice.createpo);
    this.isFromProjectStatusRep = (config.isFromProjectStatusRep!=null&&config.isFromProjectStatusRep!=undefined)?config.isFromProjectStatusRep:false;
    var isbchlFields1=(!config.isCustomer && config.isOrder);
    this.isWithInvUpdate = config.isWithInvUpdate;
    this.GRDOSettings=config.GRDOSettings;
    this.uPermType=config.isLeaseFixedAsset?Wtf.UPerm.leaseorder:config.isCustomer?Wtf.UPerm.assetsales:Wtf.UPerm.assetpurchase;
    this.permType=config.isLeaseFixedAsset?Wtf.Perm.leaseorder:config.isCustomer?Wtf.Perm.assetsales:Wtf.Perm.assetpurchase;
    this.exportPermType=config.isLeaseFixedAsset?(this.quotation?this.permType.exportlqt:config.isOrder?this.permType.exportlor:this.permType.exportlinv):config.isCustomer?this.permType.exportdispinv:(config.quotation?this.permType.exportavq:config.isOrder?this.permType.exportapo:this.permType.exportacqinv);
    var buttonArray = new Array();
    var moduleId=this.getModuleId();// to show terms in all SO,CQ,VQ,PO
    this.IsInvoiceTerm = (config.isCustomer && (config.moduleid==Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId || config.moduleid=='2' || moduleId==22)) || moduleId== Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || moduleId==6 || moduleId==23 ;
    this.originallyLinkedDocuments = '';
    this.isbilldateChanged = false;
    this.isGRLinkedInPI=false;
//     this.IsInvoiceTerm = (config.isCustomer && config.moduleid=='2') || config.moduleid==6;
     this.modeName = config.modeName;
     this.ignoreHistory=false;
     this.readOnly=config.readOnly;
     this.islockQuantityflag=config.islockQuantityflag;
     this.pendingapprovalTransaction = (config.pendingapproval == null || config.pendingapproval == undefined)? false : config.pendingapproval;//To identify whether edited document is pending document
     this.isLinkedTransaction = (config.isLinkedTransaction == null || config.isLinkedTransaction == undefined)? false : config.isLinkedTransaction;
     this.includeDeactivatedTax = this.isEdit != undefined? (this.copyInv ? false : this.isEdit): false
     this.saveOnlyFlag = false;
     buttonArray.push({
        text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
        tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
        id: "save" + config.heplmodeid + this.id,
        hidden:this.isViewTemplate,
        scope: this,
        handler: function() {
            this.mailFlag = true;
            this.saveOnlyFlag = true;
            if (this.isTemplate) {
                if (this.moduleTemplateName.getValue() == '') {
                    WtfComMsgBox(["Error", "Please Enter Template Name First."], 1);
                    return;
                }
                this.saveTemplate();
            } else if (this.isLinkedTransaction) {
                this.update();
            } else {
                this.save();
            }
        },
        iconCls: 'pwnd save'
    });
     
    buttonArray.push({
        text: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNew"),
        tooltip: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNewToolTip"),
        id: "savencreate" + config.heplmodeid + this.id,
        hidden : (this.isEdit&&!this.isCopyFromTemplate) || (this.copyInv&&!this.isCopyFromTemplate) || this.isTemplate || this.isViewTemplate,
        scope: this,
        handler: function(){
            this.mailFlag = false;
            this.saveOnlyFlag = false;
            this.save();
        },
        iconCls: 'pwnd save'
    });
    
    buttonArray.push({
        text:WtfGlobal.getLocaleText("acc.common.email"),  // "Email",
        tooltip : WtfGlobal.getLocaleText("acc.common.emailTT"),  //"Email",
        id: "emailbut" + this.id,
        hidden : this.isTemplate||this.isViewTemplate,
        scope: this,
        disabled : true,
        handler: function(){this.callEmailWindowFunction(this.response, this.request)},
        iconCls: "accountingbase financialreport"
    });
   
    this.invoiceRec = {};
    this.data={};
    var tranType=null;
    if(this.isCustBill)
        tranType=config.isCustomer?(config.isOrder?Wtf.autoNum.BillingSalesOrder:Wtf.autoNum.BillingInvoice):(config.isOrder?Wtf.autoNum.BillingPurchaseOrder:Wtf.autoNum.BillingGoodsReceipt);
    else if(config.moduleid==90||config.moduleid==20||config.moduleid==36){ //ERP-9293
        if(config.moduleid==90){    //PO Moduleid = 18, Fixed PO Moduleid = 90
            tranType=Wtf.autoNum.PurchaseOrder;
        }else{
            tranType=Wtf.autoNum.SalesOrder;
        }   
    }else if(config.moduleid==22||config.moduleid==23){
        if(config.moduleid==22){
            tranType=Wtf.autoNum.Quotation;
        }else{
            tranType=Wtf.autoNum.Venquotation;
        } 
    }else if(config.moduleid==2 || config.moduleid==Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId){
        tranType=Wtf.autoNum.Invoice;
    }else if(config.moduleid==Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId){ //Send mode for Asset Vendor Quotation, Used in Export Single PDF
           tranType = Wtf.autoNum.Venquotation;
    } else if(config.moduleid==Wtf.Acc_Lease_Quotation) {
           tranType = Wtf.autoNum.Quotation;
    }
     else if(config.moduleid==Wtf.LEASE_INVOICE_MODULEID) {
           tranType = Wtf.autoNum.Invoice;
    }else { 
           tranType=Wtf.autoNum.GoodsReceipt;
    }
    if(this.isRequisition) {
        tranType= Wtf.autoNum.Requisition;
    } else if(this.isRFQ) {
        tranType= Wtf.autoNum.RFQ;
    }
    	
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType) ||this.isLeaseFixedAsset){    
    var singlePDFtext = null;
    if(this.isQuotation)
    	singlePDFtext = WtfGlobal.getLocaleText("acc.accPref.autoQN");
    else
    	singlePDFtext = config.isCustomer?(config.isOrder?WtfGlobal.getLocaleText("acc.accPref.autoSO"):WtfGlobal.getLocaleText("acc.accPref.autoInvoice")):(config.isOrder?WtfGlobal.getLocaleText("acc.accPref.autoPO"):WtfGlobal.getLocaleText("acc.accPref.autoVI"));

    this.singlePrint=new Wtf.exportButton({
         obj:this,
         id:"exportpdf" + this.id,
         iconCls: 'pwnd exportpdfsingle',
         text:WtfGlobal.getLocaleText("acc.field.ExportPDF"),// + " "+ singlePDFtext,
         tooltip :WtfGlobal.getLocaleText("acc.rem.39.single"),  //'Export Single Record details',
         disabled :true,
         isEntrylevel:true,
         exportRecord:this.exportRecord,
         hidden:this.isRequisition || this.isRFQ || this.isSalesCommissionStmt ||this.readOnly,
         menuItem:{rowPdf:(this.isSalesCommissionStmt)?false:true,rowPdfTitle:WtfGlobal.getLocaleText("acc.rem.39") + " "+ singlePDFtext},
         get:tranType,
         moduleid:config.moduleid
     });
     buttonArray.push(this.singlePrint);
    }  
    buttonArray.push({
//        xtype: 'button',
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
    });
       this.outstandingreportflag=false;
    buttonArray.push({
        text: (config.isCustomer?WtfGlobal.getLocaleText("acc.field.ShowOutstandigSO"):WtfGlobal.getLocaleText("acc.field.ShowOutstandigPO")),
        cls: 'pwnd add',
        id: "posttext" + this.id,        
        hidden:!(config.moduleid==Wtf.Acc_Purchase_Order_ModuleId || config.moduleid==Wtf.Acc_Lease_Order),
        tooltip : (config.isCustomer?WtfGlobal.getLocaleText("acc.field.ShowsOutstandigSalesOrder"):WtfGlobal.getLocaleText("acc.field.ShowsOutstandigPurchaseOrder")),
        style:" padding-left: 15px;",
        scope: this,
               
        handler: function() {
            this.outstandingreportflag=true;
             this.person= this.Name.getValue();
           if(config.moduleid==Wtf.Acc_Lease_Order)
            {
              
               callSalesOrderList(false,false,this.outstandingreportflag,this.person);  //for showing Outstanding SO and Po while creating one flag passed
            }
            else
            {
               callPurchaseOrderList(false,false,this.outstandingreportflag,this.person);
            }
        }
    });
     
     buttonArray.push(this.recurringInvoice = new Wtf.Toolbar.Button({
        text:(config.moduleid==Wtf.Acc_Invoice_ModuleId  || config.moduleid==Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId || Wtf.LEASE_INVOICE_MODULEID) ? 'Set Recurring Invoice':'Set Recurring SO' ,
        iconCls:getButtonIconCls(Wtf.etype.copy),
        id:'RecurringSO',        
        hidden:!(this.isInvoice && config.isCustomer) || (config.moduleid==Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId) || this.readOnly,
        tooltip :'Create Recurring Sales Invoice',
        style:" padding-left: 15px;",
        scope: this,
        disabled : true,
        handler: function() {
            var termDays="";
            if(this.Term.getValue()!=null && this.Term.getValue() != ""){
                        var rec = this.Term.store.getAt(this.Term.store.find('termid',this.Term.getValue()));
                        if(rec!=null && rec!="" && rec!=undefined)
                            termDays = rec.data.termdays;
                    }
                 callRepeatedInvoicesWindow(true,this.invoiceRec,false,false,true,this.RecordID,termDays);//set Forth Variable to false for Invoice  and true for sales order 
        }
    }));
    
    buttonArray.push({
        text: WtfGlobal.getLocaleText("acc.field.ShowAddress"),  //WtfGlobal.getLocaleText("acc.template.posttext") , //'<b>Post Text</b>',
        cls: 'pwnd add',
        id: "showaddress" + this.id,                
        tooltip : WtfGlobal.getLocaleText("acc.field.UseShowAddressoptiontoinsertAddresses"),       
        style:" padding-left: 15px;",
        scope: this,
        disabled : true, 
        handler: function() {
            var addressRecord="";
            var isCopy=this.copyInv;
            var isEdit=this.isEdit;
            if(this.isQuotationFromPR){//this.isQuotationFromPR is true only when we Vendor Quotation from PR Report  by clicking on button Record Vendor Quotation
                addressRecord=null;    //This case treated like new case
                isCopy=false;
                isEdit=false;
            } else if (this.linkRecord && this.singleLink) {    //when user link single record
                addressRecord = this.linkRecord;
            } else {
                addressRecord=this.record;
            }
            if (this.isVenOrCustSelect) {
                isEdit = false;
                isCopy = false;
            }
            this.stateAsComboFlag = false;
            /*
             For India GST State As Combo in customer and vendor masters if Customer/Vendor type is Export (WPAY),Export (WOPAY),Import
             */
            if (WtfGlobal.isIndiaCountryAndGSTApplied() || WtfGlobal.isUSCountryAndGSTApplied()) {
                this.stateAsComboFlag = true;
                if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
                    this.custVenId = Wtf.GSTCUSTVENTYPE.NA;
                    var index = this.Name.store.find('accid', this.Name.getValue());
                    if (index != -1) {
                        var storerec = this.Name.store.getAt(index);
                        this.custVenId = storerec.data.CustVenTypeDefaultMstrID;
                    }
                    this.stateAsComboFlag = (this.custVenId == undefined || !(this.custVenId == Wtf.GSTCUSTVENTYPE.Export || this.custVenId == Wtf.GSTCUSTVENTYPE.ExportWOPAY || this.custVenId == Wtf.GSTCUSTVENTYPE.Import)) ? true : false
                }
            }
            callAddressDetailWindow(addressRecord,isEdit,isCopy,this.Name.getValue(),this.currentAddressDetailrec,config.isCustomer,this.readOnly,"",this.singleLink,undefined,WtfGlobal.getModuleId(this),null,null,null,null,this.stateAsComboFlag);             
            Wtf.getCmp('addressDetailWindow').on('update',function(config){
                this.currentAddressDetailrec=config.currentaddress;
                /**
                 * auto poulate dimension values
                 */
                if (this.isGST) {
                    /**
                     * ERP-32829 
                     * code for New GST 
                     */
                    this.ignoreHistory=true;
                    this.addressDetailRecForGST = this.currentAddressDetailrec;
                        if (!Wtf.account.companyAccountPref.avalaraIntegration) {
                            var obj = {};
                            obj.tagsFieldset = this.tagsFieldset;
                            obj.currentAddressDetailrec = this.addressDetailRecForGST;
                            obj.mappingRec = this.addressMappingRec;
                            obj.isCustomer = this.isCustomer;
                            obj.isShipping = this.isShipping;
                            var invalid = populateGSTDimensionValues(obj)
                            if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
                                /**
                                 * On Address Changes done then need to make getGSTForProduct Request to update tax Details
                                 */
                                var isAddressChanged = true;
                                this.applyGSTFieldsBasedOnDate(isAddressChanged);
                            } else {
                                processGSTRequest(this, this.Grid);
                            }
                        }
                    }
            },this);   
        }
    });
    
    this.printRecords = new Wtf.exportButton({
        obj: this,
        id: "printSingleRecord"+ this.id,
        iconCls: 'pwnd printButtonIcon',
        text: WtfGlobal.getLocaleText("acc.rem.236"),
        tooltip: WtfGlobal.getLocaleText("acc.rem.236.single"), //'Print Single Record Details',
        disabled: this.readOnly?false:true,
        isEntrylevel: false,
        exportRecord:this.exportRecord,
        menuItem: {
            rowPrint: true
        },
        get: tranType,
        moduleid:config.moduleid
    });
    
    buttonArray.push(this.printRecords);
    
    /*
     * Assign the value to the optimized flag as per System preferences.
     */
    this.custVenOptimizedFlag = Wtf.account.companyAccountPref.custvenloadtype;
    buttonArray.push('->');
    if(!this.isEdit){
        buttonArray.push(help);
    }
    Wtf.apply(this, config);
    Wtf.apply(this, {
        bbar: buttonArray
    });

    Wtf.apply(this, config);
    Wtf.apply(this, {
        bbar:buttonArray
      });
    Wtf.account.FixedAssetTransactionPanel.superclass.constructor.call(this,config);
    this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.FixedAssetTransactionPanel,Wtf.account.ClosablePanel,{
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
    loadRecord:function(){
        if(this.record!=null&&!this.dataLoaded){
            var data=this.record.data;
            this.NorthForm.getForm().loadRecord(this.record);
            if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA || Wtf.isExciseApplicable){
                this.exciseFormPanel.getForm().loadRecord(this.record);
            }
            if((this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  || this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId) && !this.cash && !this.isFixedAsset) {
                this.InvoiceStore.on("load", function(){
                    this.invoiceList.setValue(data.landedInvoiceNumber);
                }, this);
                this.InvoiceStore.load();
            }
            
            if((this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId ||this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Lease_Quotation ||this.moduleid == Wtf.Acc_Lease_Order || this.moduleid==Wtf.LEASE_INVOICE_MODULEID) && !this.cash) {
                this.termds.on("load", function(){
                    this.Term.setValue(data.termid);
                }, this);
                this.termds.load();
            }
            this.sequenceFormatStore.load();
            if(!this.copyInv&&!this.isPOfromSO&&!this.isSOfromPO && !(this.quotation && !this.isCustomer && this.ispurchaseReq)) {
                this.Number.setValue(data.billno);
            }
            this.externalcurrencyrate=this.record.data.externalcurrencyrate;
            if(this.isPOfromSO||this.isSOfromPO || (this.quotation && !this.isCustomer && this.ispurchaseReq)){ // for showing link number in number field in case of creating PO from SO or creating SO from PO
                this.fromPO.setValue(true);
                
                if (this.quotation && !this.isCustomer && this.ispurchaseReq) {
                    this.fromLinkCombo.setValue(5);
                } else {
                    this.fromLinkCombo.setValue(0);
                }
                                
                this.POStore.proxy.conn.url = (this.isPOfromSO)? "ACCSalesOrderCMN/getSalesOrders.do" : (this.isSOfromPO)? "ACCPurchaseOrderCMN/getPurchaseOrders.do" : "ACCPurchaseOrderCMN/getRequisitions.do";
                this.POStore.on("load", function(){
                    if (this.isPOfromSO||this.isSOfromPO || this.quotation && !this.isCustomer && this.ispurchaseReq) {
                        if (!(this.quotation && !this.isCustomer && this.ispurchaseReq)) { // In case of 'Vendor quotation generated from purchase requisition' , this.po and this.fromPO will not be disabled
                            this.PO.disable();
                            this.fromPO.disable();
                        }
                        this.setTransactionNumber();
                        if (this.isPOfromSO || this.isSOfromPO) {
                            this.PO.setValue(data.billid);
                        } else {
                            this.PO.setValue(this.PR_IDS);
                        }
                    }
                    if(this.isPOfromSO){
                        this.isPOfromSO = false;
                    }
                    if(this.isSOfromPO){
                        this.isSOfromPO = false;
                    }
                }, this);
                this.POStore.load();                        
                
            }else{ // for showing multiple link numbers in number field
                this.Grid.getStore().on("load",function(){
                if(this.Grid.getStore().data.items.length>0){
                    var linkType=-1;
                    var storeData = [],linkNumbers=[],linkIDS=[];
                    this.POStore.removeAll();
                    this.Grid.getStore().each(function(rec){
                        if(this.copyInv) { 
                            rec.data.linkid=""; 
                            rec.data.rowid=""; 
                            rec.data.linktype="";
                            rec.data.linkto="";
                        } else {
                            if((rec.data.linkto!=""&&rec.data.linkto!=undefined) && (rec.data.linktype!=-1 && rec.data.linktype!=undefined)){
                                var isExistFlag=false;
                                for(var count=0;count<linkNumbers.length;count++){
                                    if(rec.data.linkto==linkNumbers[count]){
                                        isExistFlag=true;
                                        break;
                                    }
                                }
                                if(isExistFlag==false){
                                    linkNumbers.push(rec.data.linkto);
                                    linkIDS.push(rec.data.linkid);
                                }                                                        
                                linkType=rec.data.linktype;                            
                                var newRec=new this.PORec({
                                    billid:rec.data.linkid,
                                    billno:rec.data.linkto ,   
                                    gstIncluded:rec.data.israteIncludingGst,
                                    /*
                                    *	ERM-1037
                                    *	For date of linked document to restrict linking of future dated document
                                    */
                                    date:rec.data.linkDate   
                                });
                                storeData.push(newRec);
                            }
                        }
                    },this);
                    if(storeData.length>0){
                        this.POStore.add(storeData);
                    }
                    if(linkIDS.length>0){
                        
                        if(this.Grid){
                            this.Grid.fromPO=true;
                            this.Grid.linkedFromOtherTransactions=true;
                            
                            if (this.quotation && !this.isCustomer && this.isFixedAsset) {
                                this.Grid.isQuotationFromPR = true;
                            } else if (this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId) {
                                this.Grid.isPOfromVQ = true;
                            } else if (this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId) {
                                if (linkType == 0) {
                                    this.Grid.isPIFromPO = true;
                                } else if (linkType == 2) {
                                    this.Grid.isPIFromVQ = true;
                                } else if(linkType == 1){
                                    //linkType : 1 = Goods Receipt
                                    this.isGRLinkedInPI=true;
                                }
                            }
                        }
                        
                        this.Name.disable();
                        this.Currency.disable();
                        this.fromPO.disable();
                        this.fromLinkCombo.disable();
                        this.PO.disable();
                        this.fromPO.setValue(true);                
                        this.PO.setValue(linkIDS);
                    }
                    if(linkType!=-1){
                        this.fromLinkCombo.setValue(linkType);
                    }
                }
                },this);
            }
            if(this.copyInv || this.isEdit){
                if(Wtf.getCmp("showaddress" + this.id)){
                    Wtf.getCmp("showaddress" + this.id).enable(); 
                }   
            }
            this.template.setValue(data.templateid);
            this.Currency.setValue(data.currencyid);
             if(data.islockQuantityflag)
             {
                 this.lockQuantity.setValue(true);
            }
            else
            {
                 this.lockQuantity.setValue(false);
            }
           /*
            * In Edit and view case set value to the customer combo-No need to load store 
           */
           this.Name.setValForRemoteStore(data.personid, data.personname,data.hasAccess);
            this.Memo.setValue(data.memo);
            this.postText = data.posttext;
            this.DueDate.setValue(data.duedate);
            if(this.isOrder && data.isOpeningBalanceTransaction){
                this.isOpeningBalanceOrder = data.isOpeningBalanceTransaction;
                this.billDate.maxValue=this.getFinancialYRStartDatesMinOne(true);
            }
              if (this.isIndiaGST) {
                    if (data.CustomerVendorTypeId != undefined) {
                        this.CustomerVendorTypeId = data.CustomerVendorTypeId;
                    }
                    if (data.gstin != undefined) {
                        this.gstin = data.gstin;
                    }
                    if (data.GSTINRegistrationTypeId != undefined) {
                        this.GSTINRegistrationTypeId = data.GSTINRegistrationTypeId;
                    }
                    if (data.gstdochistoryid != undefined) {
                        this.gstdochistoryid = data.gstdochistoryid;
                    }
                }
            this.billDate.setValue(data.date);
            this.perDiscount.setValue(data.ispercentdiscount);
            this.Discount.setValue(data.discountval);
            this.isTaxable.setValue(data.taxincluded);
            this.PORefNo.setValue(data.porefno);
            this.CostCenter.setValue(data.costcenterid);
            this.dataLoaded=true;
            if(this.IsInvoiceTerm) {
                this.setTermValues(data.termdetails);
            }
            if(this.isPOfromSO||this.isSOfromPO){
            	this.billDate.setValue(Wtf.serverDate);
            	this.updateDueDate();
            }
            if(this.isCustomer && this.record.data.partialinv){
                var id=this.Grid.getId();
                var rowindex=this.Grid.getColumnModel().getIndexById(id+"partdisc");
                this.Grid.getColumnModel().setHidden( rowindex,false) ;
            }
            var gridID=this.Grid.getId();
            var taxColumnIndex=this.Grid.getColumnModel().getIndexById(gridID+"prtaxid");
            var taxAmtColumnIndex=this.Grid.getColumnModel().getIndexById(gridID+"taxamount");
            if(this.record.data.includeprotax){
                this.Grid.getColumnModel().setHidden( taxColumnIndex,false) ;
                this.Grid.getColumnModel().setHidden( taxAmtColumnIndex,false) ;
                
                this.isTaxable.setValue(false);
                this.isTaxable.disable();
                this.Tax.setValue("");
                this.Tax.disable();
                
            }else{
                this.Grid.getColumnModel().setHidden( taxColumnIndex,true) ;
                this.Grid.getColumnModel().setHidden( taxAmtColumnIndex,true) ;
                
                    this.isTaxable.reset();
                    this.isTaxable.enable();
                }
            if(data.taxid == ""){
                this.isTaxable.setValue(false);
                this.Tax.setValue("");
                this.Tax.disable();
            }else{
                this.Tax.setValue(data.taxid);
                this.isTaxable.setValue(true);
            }
            this.loadTransStore();
            if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Show_all_Products){
                this.ProductGrid.productComboStore.load({params:{mappingProduct:true,customerid:this.Name.getValue(),common:'1', loadPrice:true,mode:22}}) ;           
            } else {
                this.loadDetailsGrid();
            }
            
            this.includingGST.reset();
            if(this.isViewTemplate){
                this.includingGST.disable();
            }else if(this.isTaxShouldBeEnable){
                this.includingGST.enable();
            }
            if(this.record.data.gstIncluded!=undefined){
                this.includingGST.setValue(this.record.data.gstIncluded);
                if(this.record.data.gstIncluded){
                    this.includeProTax.setValue(true);
                    this.includeProTax.disable();
                    this.isTaxable.setValue(false);
                    this.isTaxable.disable();
                    this.Tax.setValue("");
                    this.Tax.disable();
                    var rowRateIncludingGstAmountIndex=this.Grid.getColumnModel().findColumnIndex("rateIncludingGst");
                    if(this.includingGST.getValue()){
                            this.Grid.getColumnModel().setHidden(rowRateIncludingGstAmountIndex,!this.includingGST.getValue());
                    }
                }
            }
            var prodTaxSet = (this.record.data.includeprotax != undefined && this.record.data.includeprotax != "") ? this.record.data.includeprotax : false;
            if (prodTaxSet) {
                this.applyTaxToTermsChk.getEl().up('.x-form-item').setDisplayed(true);
                this.applyTaxToTermsChk.setValue(data.isapplytaxtoterms);
            } else {
                this.applyTaxToTermsChkHideShow(false);
            }
            
            if(this.isTaxable != undefined && this.isTaxable.getValue()){
                /*This block will execute only when Global Level tax is applied
                *True means Hidden False
                *This function written in CommonERPComponent.js
                */
                this.HideShowLinkedTermTaxAndTermTaxAmountCol(true);
            }
            if(this.readOnly){
                this.isTaxable.disable();
                this.Tax.disable();
            }
            if (this.Grid) {
                this.Grid.affecteduser = data.personid;
            }
            
            if (this.isLinkedTransaction) {
                if (this.NorthForm.getForm().items != undefined && this.NorthForm.getForm().items != null) {
                    for (var i = 0; i < this.NorthForm.getForm().items.length; i++) {
                        this.NorthForm.getForm().items.item(i).disable();
                    }
                }
                this.isTaxable.disable();
                this.Tax.disable();

                /*Enabling the required fields*/
                this.CostCenter.enable();
                this.Memo.enable();
                this.validTillDate.enable();
                this.shipDate.enable();
                this.shipvia.enable();
                this.fob.enable();
                this.users.enable();
            }
        }
        this.populateGSTDataOnEditCopy();
    },
    onRender:function(config){        
        var centerPanel = new Wtf.Panel({
                region : 'center',
                border : false,
                autoScroll : true
            });
        if(Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.isExciseApplicable){     
            if(this.isCustomer||this.isCustBill||this.isOrder||this.isEdit && !this.isExpenseInv||this.copyInv && !this.isExpenseInv || this.isTemplate) {
                centerPanel.add(this.NorthForm,this.Grid,this.southPanel);
            } else if((this.isEdit && this.isExpenseInv) || (this.copyInv && this.isExpenseInv)) {
                centerPanel.add(this.NorthForm,this.ExpenseGrid,this.southPanel);
            //            this.Tax.store=this.ExpenseGrid.taxStore;
            } else {
                centerPanel.add(this.NorthForm,this.GridPanel,this.southPanel);
            }
        }else{
//            Wtf.stateStore.load({
//                params:{
//                    countryid: Wtf.account.companyAccountPref.countryid 
//                }
//            });
            if(this.isCustomer||this.isCustBill||this.isOrder||this.isEdit && !this.isExpenseInv||this.copyInv && !this.isExpenseInv || this.isTemplate) {
                centerPanel.add(this.NorthForm,this.exciseFormPanel,this.Grid,this.southPanel);
            } else if((this.isEdit && this.isExpenseInv) || (this.copyInv && this.isExpenseInv)) {
                centerPanel.add(this.NorthForm,this.exciseFormPanel,this.ExpenseGrid,this.southPanel);
            //            this.Tax.store=this.ExpenseGrid.taxStore;
            } else {
                centerPanel.add(this.NorthForm,this.exciseFormPanel,this.GridPanel,this.southPanel);
            }  

        }
        this.add(centerPanel);
        this.add({
                border: true,
                id: 'south' + this.id,
                region: 'south',
                //split: true,
                hidden : true,//this.isCustBill,
                layout: 'fit',
                height:130 ,
                plugins : new Wtf.ux.collapsedPanelTitlePlugin(),
                collapsibletitle : WtfGlobal.getLocaleText("acc.common.recentrec") + " " +this.businessPerson + " for the Product",
                title : WtfGlobal.getLocaleText("acc.common.recentrec") + " " +this.businessPerson + " for the Product",
                collapsible: true,
                collapsed: true,
                items : [
                    this.lastTransPanel
                ]
            });
        Wtf.account.FixedAssetTransactionPanel.superclass.onRender.call(this, config);
        
        if(this.isViewTemplate && !this.readOnly){
            this.Number.hideLabel = true;
            this.Number.hide();
            this.sequenceFormatCombobox.hideLabel = true;
            this.sequenceFormatCombobox.hide();
            this.billDate.hideLabel = true;
            this.billDate.hide();
        }
        
        if(this.isTemplate){
            this.Number.setValue("");
            this.Number.disable();
            this.sequenceFormatCombobox.disable();
            this.billDate.disable();
            this.autoGenerateDO.disable();
            this.templateModelCombo.hideLabel = true;
        }
        this.initForClose();
        if(this.isFromProjectStatusRep){
            
            this.selectedCustomerStore.on('load',function(){
                var rec = this.selectedCustomerStore.getAt(0);
                if(this.Name)
                    this.Name.setValue(rec.get('accid'));
//                if(this.billingAddrsStore)
//                    this.billingAddrsStore.load({params:{customerid:rec.get('accid')}});
//                if(this.ShippingAddrsStore)
//                    this.ShippingAddrsStore.load({params:{customerid:rec.get('accid')}}); 
            },this);
            this.selectedCustomerStore.load({
                params:{
                    selectedCustomerIds:this.selectedCustomerIds
                }
            });
            
            this.setPOLinks();
        }
        
        // hide form fields
            this.hideFormFields();
        this.on("activate", function () {
            if (this.includeProTax && this.includeProTax.getValue()) {
                this.applyTaxToTermsChkHideShow(true);
            }else if (!this.isEdit) {
                this.applyTaxToTermsChkHideShow(false);
            }
        }, this);
    },
    
    hideFormFields:function(){
        if(this.isCustomer){
            if(this.isInvoice){
                if (this.moduleid == Wtf.LEASE_INVOICE_MODULEID) {
                    this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.leaseInvoice);
                } else {
                    this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.customerInvoice);
                }

            } else if(this.cash){

                this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.CS);

            } else if(this.isOrder && !this.quotation){
                if (this.moduleid == Wtf.Acc_Lease_Order) {
                    this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.leaseorder);
                } else {
                    this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.salesOrder);
                }

            } else if (this.quotation) {
                if (this.moduleid == Wtf.Acc_Lease_Quotation) {
                    this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.leasequotation);
                } else {
                    this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.customerQuotation);
                }
            }
        }else{
            if(this.isInvoice){

                this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.vendorInvoice);

            } else if(this.cash){

                this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.CP);

            } else if(this.isOrder && !this.quotation){

                this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.purchaseOrder);

            } else if(this.quotation){

                this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.vendorQuotation);
            }
        }
    },
    
  
    showPONumbersGrid: function (url) {
        this.PONumberSelectionWin = new Wtf.account.PONumberSelectionWindow({
            renderTo: document.body,
            height: 500,
            id: this.id + 'PONumbersSelectionWindowDO',
            width: 600,
            title: 'Document Selection Window',
            layout: 'fit',
            modal: true,
            resizable: false,
            url: url,
            columnHeader: this.fromLinkCombo.getRawValue(),
            moduleid: this.moduleid,
            columnHeader:this.fromLinkCombo.getRawValue(),
                    invoice: this,
            storeBaseParams: this.POStore.baseParams,
            storeParams: this.POStore.lastOptions.params,
            PORec: this.PORec
        });
        this.PONumberSelectionWin.show();
    },
    hideTransactionFormFields:function(array){
        if(array){
            for(var i=0;i<array.length;i++){
                var fieldArray = array[i];
                if(Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id)){
                    Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hideLabel = fieldArray.isHidden;
                    Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hidden = fieldArray.isHidden;
                    if(fieldArray.isUserManadatoryField && Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel != undefined){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).allowBlank = !fieldArray.isUserManadatoryField;
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel = Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel + " *";
                    }
                }
            }
        }
    },
    
setNextNumber:function(config){
    if(this.sequenceFormatStore.getCount()>0){
        if (this.isEdit && !this.copyInv && !(this.quotation && !this.isCustomer && this.ispurchaseReq)) { // only edit case
            var index=this.sequenceFormatStore.find('id',this.record.data.sequenceformatid);   
            if(index!=-1){
                this.sequenceFormatCombobox.setValue(this.record.data.sequenceformatid); 
                this.sequenceFormatCombobox.disable();
                this.Number.disable();   
            } else {
                this.sequenceFormatCombobox.setValue("NA"); 
                this.sequenceFormatCombobox.disable();
                    if (this.isViewTemplate || this.isLinkedTransaction) {
                        this.Number.disable();
                    } else {
                        this.Number.enable();
                    }  
                }
         } else if (!this.isEdit || this.copyInv|| this.GENERATE_PO||this.GENERATE_SO || (this.quotation && !this.isCustomer && this.ispurchaseReq)) { // create new,copy,generate so and po case
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
                this.Number.setValue("");  
                WtfGlobal.hideFormElement(this.Number);
            }
        }
    }
},
    initComponent:function(config){
        Wtf.account.FixedAssetTransactionPanel.superclass.initComponent.call(this,config);
        this.businessPerson=(this.isCustomer?'Customer':'Vendor');
        this.loadCurrFlag = true;
        if(!this.isCustBill){
            this.isCustBill = false;
        }
//        this.term=0;
        
        this.tplSummary=new Wtf.XTemplate(
            '<div class="currency-view">',
            '<table width="100%">',
//            '<tpl if="'+(!this.isOrder || this.quotation)+'">',
            '<tr><td><b>'+WtfGlobal.getLocaleText("acc.invoice.subTotal")+' </b></td><td text-align=right>{subtotal}</td></tr>',
//            '<tr><td><b>- '+WtfGlobal.getLocaleText("acc.invoice.discount")+' </b></td><td align=right>{discount}</td></tr>',
            '</table>',
//            '<hr class="templineview">',
//            '</tpl>',
            '<table width="100%">',
//            '<tr><td><b>'+WtfGlobal.getLocaleText("acc.invoice.amt")+' </b></td><td align=right>{totalamount}</td></tr>',
            '<tr><td><b>'+WtfGlobal.getLocaleText("acc.field.AmountBeforeTax")+' </b></td><td align=right>{amountbeforetax}</td></tr>',
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
//                '<tpl if="'+(!this.isOrder || this.quotation)+'">',
//                '<tr><td><b>- '+WtfGlobal.getLocaleText("acc.invoice.discount")+' </b></td><td align=right>{discount}</td></tr>',
//                '</tpl>',
                '</table>',
//                '<hr class="templineview">',
                '<table width="100%">',
//                '<tr><td><b>'+WtfGlobal.getLocaleText("acc.invoice.amt")+' </b></td><td align=right>{totalamount}</td></tr>',
                '<tr><td><b>+ Invoice Term: </b></td><td align=right>{termtotal}</td></tr>',
                '<tr><td><b>'+WtfGlobal.getLocaleText("acc.field.AmountBeforeTax")+' </b></td><td align=right>{amountbeforetax}</td></tr>',
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
                 '</table>',
                '<table width="100%">',
//                '<tpl if="'+(!this.isOrder || this.quotation)+'">',
                '<tr><td ><b>'+WtfGlobal.getLocaleText("acc.inv.amountdue")+' </b></td><td align=right>{amountdue}</td></tr>',
                '</table>',
                '<hr class="templineview">',
//                '</tpl>',
                '<hr class="templineview">',
                '</div>'
            );
        }
        
        this.GridRec = Wtf.data.Record.create ([
            {name:'id'},
            {name:'number'}
        ]);

        this.termRec = new Wtf.data.Record.create([
            {name: 'termname'},
            {name: 'termdays'},
            {name: 'termid'}
        ]);
        this.termds = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.termRec),
    //        url: Wtf.req.account + 'CompanyManager.jsp',
            url : "ACCTerm/getTerm.do",
            baseParams:{
                mode:91
            }
         });         
         
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
                mode:this.modeName
            }
        });
//          this.sequenceFormatStore.on('load',function(){
//             if(this.sequenceFormatStore.getCount()>0){
//                 var seqRec=this.sequenceFormatStore.getAt(0)
//                this.sequenceFormatCombobox.setValue(seqRec.data.id);
//                if((!this.isEdit&&!this.copyInv) || this.isPOfromSO || this.isSOfromPO){
//                    this.getNextSequenceNumber(this.sequenceFormatCombobox);
//                 }
//             }
//         },this);
        this.sequenceFormatStore.load();
        this.sequenceFormatStore.on('load',this.setNextNumber,this);
         this.currencyRec = new Wtf.data.Record.create([
            {name: 'currencyid',mapping:'tocurrencyid'},
            {name: 'symbol'},
            {name: 'currencyname',mapping:'tocurrency'},
            {name: 'exchangerate'},
            {name: 'htmlcode'},
            {name: 'currencycode'}
         ]);
         this.currencyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.currencyRec),
    //        url:Wtf.req.account+'CompanyManager.jsp'
            url:"ACCCurrency/getCurrencyExchange.do"
         });
         
         this.currencyStoreCMB = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.currencyRec),
    //        url:Wtf.req.account+'CompanyManager.jsp'
            url:"ACCCurrency/getCurrencyExchange.do"
         });
         
         this.currencyStoreCMB.load();
         
         var transdate=(this.isEdit?WtfGlobal.convertToGenericDate(this.record.data.date):WtfGlobal.convertToGenericDate(new Date()));

         this.selectedCustomerStore = new Wtf.data.Store({
        //    url:Wtf.req.account+'CustomerManager.jsp',
            url:"ACCCustomer/getCustomersForCombo.do",
            baseParams:{
                mode:2,
                group:10,
                deleted:false,
                nondeleted:true,
                common:'1'
            },
            reader: new  Wtf.data.KwlJsonReader({
                root: "data",
                autoLoad:false
            },Wtf.personRec)
        });

         this.Currency= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.currency.cur"),  //'Currency',
            hiddenName:'currencyid',
            id:"currency"+this.heplmodeid+this.id,
//            anchor: '94%',
            width : 240,
            //disabled:true,
            store:this.currencyStore,
            valueField:'currencyid',
            allowBlank : false,
            forceSelection: true,
            displayField:'currencyname',
            scope:this,
            selectOnFocus:true
        });

        this.Currency.on('select', function(){
            this.externalcurrencyrate=0; 
            this.currencychanged = true;            
            if(this.includingGST.getValue()!=false){
                this.includingGST.setValue(false);   
            }
            var customer="",currency="";
            if(this.Name.getValue() != undefined && this.Name.getValue() != ""){
                customer= this.Name.getValue();
            }    
            if(this.Currency.getValue() != undefined && this.Currency.getValue() != ""){
                currency= this.Currency.getValue();        
            }
//            if(Wtf.account.companyAccountPref.currid != this.Currency.getValue()) {
//                this.applyTaxToTermsChkHideShow(false);
//            }
            if(!this.GENERATE_PO&&!this.GENERATE_SO){
                this.onCurrencyChangeOnly();
                this.Name.setValue(customer);
                this.Currency.setValue(currency);
            }    
            this.updateFormCurrency();
//            this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.billDate.getValue()),tocurrencyid:this.Currency.getValue()}});
        }, this);
        
        this.Term= new Wtf.form.FnComboBox({
            fieldLabel:(this.isCustomer?"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.invoice.creditTerm.tip")+"'>"+ WtfGlobal.getLocaleText("acc.invoice.creditTerm")+"</span>":"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.invoice.debitTerm.tip")+"'>"+ WtfGlobal.getLocaleText("acc.invoice.debitTerm")+"</span>")+' *',
            itemCls : (this.cash)?"hidden-from-item":"",  //||this.isOrder
            hideLabel:this.cash,    //||this.isOrder
            id:"creditTerm"+this.heplmodeid+this.id,
            hidden:this.cash,                //||this.isOrder,
            hiddenName:'term',
//            anchor: '93.5%',
            width : 240,
            store:this.termds,
            valueField:'termid',
            allowBlank:this.cash,               //||this.isOrder,
            emptyText:(this.isCustomer?WtfGlobal.getLocaleText("acc.inv.ct"):WtfGlobal.getLocaleText("acc.inv.dt")),
            forceSelection: true,
            displayField:'termname',
//            addNewFn:this.addCreditTerm.createDelegate(this),
            scope:this,
            listeners:{
                'select':{
                    fn:this.updateDueDate,
                    scope:this
                }
            },
            selectOnFocus:true
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.creditterm, Wtf.Perm.creditterm.edit))
            this.Term.addNewFn=this.addCreditTerm.createDelegate(this);
        
        this.moduleTemplateSection();
        this.postText=(this.record)?this.record.data.posttext:"";
        
        
//        if(!this.isFromProjectStatusRep){
//            if(this.isCustomer){
//                Wtf.customerAccStore.reload();
//            }else{
//                Wtf.vendorAccStore.reload();
//            }
//        }
        
        var comboConfig = {
            fieldLabel:this.isCustomer?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven") , //this.businessPerson+"*",
            hiddenName:this.businessPerson.toLowerCase(),
            id:"customer"+this.heplmodeid+this.id,
            store: this.isFromProjectStatusRep?this.selectedCustomerStore:(this.isCustomer? Wtf.customerAccStore:Wtf.vendorAccStore),
            valueField:'accid',
            displayField:'accname',
            minChars:1,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            allowBlank:false,
            hirarchical:true,
            emptyText:this.isCustomer?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven") , //'Select a '+this.businessPerson+'...',
            mode: 'remote',
            typeAhead: true,
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            forceSelection: true,
            selectOnFocus:true,
            isVendor:!(this.isCustomer),
            isCustomer:this.isCustomer,
            width : 240,
            triggerAction:'all',
            scope:this            
        };   
        
        if (this.custVenOptimizedFlag) {
            comboConfig['ctCls'] = 'optimizedclass';
            comboConfig['hideTrigger'] = true;
        } 
        this.Name = new Wtf.form.ExtFnComboBox(comboConfig);
        
        this.Name.on('select',this.onNameSelect,this);
        this.Name.on('beforeselect', function(combo, record, index) {
                return validateSelection(combo, record, index);
        }, this);
        
        // Neeraj
        if(!(this.DefaultVendor==null || this.DefaultVendor==undefined) && !this.isCustomer){
        	this.Name.value = this.DefaultVendor;
        	this.updateData();
        }

        if(!WtfGlobal.EnableDisable(this.custUPermType,this.custPermType.create))
            this.Name.addNewFn=this.addPerson.createDelegate(this,[false,null,this.businessPerson+"window",this.isCustomer],true);
        this.allAccountRec = new Wtf.data.Record.create([
            {name: 'accid'},
            {name: 'accname'},
            {name: 'groupid'},
            {name: 'groupname'},
            {name: 'level'},
            {name: 'leaf'},
            {name: 'openbalance'},
            {name: 'parentid'},
            {name: 'parentname'}
        ]);
        this.allAccountStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.allAccountRec),
    //        url: Wtf.req.account +'CompanyManager.jsp',
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,  
               ignorecustomers:true,  
                ignorevendors:true,
                nondeleted:true
            }
        });
        this.creditTo= new Wtf.form.FnComboBox({
            fieldLabel:(this.isCustomer?"Credit Account*":"Debit Account*"),
            hiddenName:"creditoraccount",
//            anchor:"50%",
            width : 240,
            store: this.allAccountStore,
            valueField:'accid',
            displayField:'accname',
            hidden:!this.isCustBill,
            hideLabel:!this.isCustBill,
            itemCls : (!this.isCustBill)?"hidden-from-item":"",
            allowBlank:!this.isCustBill||this.isOrder,
            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText"),  //'Select an Account...',
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            triggerAction:'all',
//            addNewFn: this.addAccount.createDelegate(this,[this.allAccountStore],true),
            scope:this
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.createcoa))
            this.creditTo.addNewFn=this.addAccount.createDelegate(this,[this.allAccountStore],true);
        
        this.perDiscountStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['Percentage',true],['Flat',false]]
        });
        this.fromPOStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['Yes',true],['No',false]]
        });
        
        var arrfromLink = new Array();
        
        if(this.isCustomer) {
//            arrfromLink.push(['Sales Order',0]);
            if(this.isOrder && this.isCustomer && !this.quotation){
                arrfromLink.push(['Lease Quotation',0]); // this value is used for setting flag -- isLinkedFromCustomerQuotation in record
                arrfromLink.push(['Replacement Number',1]); // this value is used for setting flag -- isLinkedFromReplacementNumber in record
            } else if(this.quotation){
//                arrfromLink.push(['Vendor Quotation',0]);  Commented Because of No Use of Vendor Quotation in Lease Order 
                arrfromLink.push(['Replacement Number',1]); // this value is used for setting flag -- isLinkedFromReplacementNumber in record
            }else{
                if(Wtf.account.companyAccountPref.withinvupdate){
                    arrfromLink.push([this.isLeaseFixedAsset?'Lease Delivery Order':'Asset Delivery Order',1]);    
                }
            }
                        
//            arrfromLink.push(['Customer Quotation',2]);                            
        } else {
            if (this.moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId) {
                arrfromLink.push(['Asset Purchase Requisition',5]);
            } else if (this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId) {
                arrfromLink.push(['Asset Vendor Quotation',2]);
            } else if(this.isOrder){
                arrfromLink.push(['Vendor Quotation',2]);
                arrfromLink.push(['Sales Order',0]);
            } else {
                arrfromLink.push(['Purchase Order',0]);
                if(Wtf.account.companyAccountPref.withinvupdate){
                    arrfromLink.push(['Goods Receipt',1]);    
                }
                arrfromLink.push(['Vendor Quotation',2]);
            }
        }
        
        
        this.fromlinkStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value'}],
            data:arrfromLink
        });
        
        this.vendorInvoice=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.inv.invno"),  //'Vendor Invoice Number*',
            name: 'vendorinvoice',
            id:"vendorInvoiceNo"+this.heplmodeid+this.id,
            hidden:this.label=='Vendor Invoice'?false:true,
//            anchor:'50%',
            width : 240,
            maxLength:50,
            scope:this,
            allowBlank:this.checkin
        });      

        
this.sequenceFormatCombobox = new Wtf.form.ComboBox({            
//        labelSeparator:'',
//        labelWidth:0,
        triggerAction:'all',
        mode: 'local',
        id:'sequenceFormatCombobox'+this.heplmodeid+this.id,
       fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.Sequenceformat.tip")+"'>"+ WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat")+"</span>",
        valueField:'id',
        displayField:'value',
        store:this.sequenceFormatStore,
        disabled: (this.isEdit&&!this.copyInv&&!this.isPOfromSO&&!this.isSOfromPO && !(this.quotation && !this.isCustomer && this.ispurchaseReq)? true : false),  
        width:240,
        typeAhead: true,
        forceSelection: true,
        name:'sequenceformat',
        hiddenName:'sequenceformat',
        allowBlank:false,
        listeners:{
            'select':{
                fn:this.getNextSequenceNumber,
                scope:this
            }
        }
            
    });
        this.Number=new Wtf.form.TextField({
            fieldLabel:(this.isPOfromSO?WtfGlobal.getLocaleText("acc.accPref.autoPO"):(this.isSOfromPO)?WtfGlobal.getLocaleText("acc.accPref.autoSO"):this.label) + " " + ((this.isTemplate)?'Number':WtfGlobal.getLocaleText("acc.common.number")),  //,  //this.label+' Number*',
            name: 'number',
            disabled:(this.isEdit&&!this.copyInv&&!this.isPOfromSO&&!this.isSOfromPO?true:false),
            id:"invoiceNo"+this.heplmodeid+this.id,
//            anchor:'50%',
            width : 240,
            maxLength:50,
            scope:this,
            allowBlank:this.checkin || (this.isTemplate&&!this.createTransactionAlso),
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseenternumber")
        });
        this.PORefNo=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.invoice.POrefNo"),  //PO Reference Number',
            name: 'porefno',
            id:"porefno"+this.heplmodeid+this.id,
            hidden:this.isOrder||!this.isCustomer,
            hideLabel:this.isOrder||!this.isCustomer,
            itemCls : (this.isOrder||!this.isCustomer)?"hidden-from-item":"",
            width : 240,//ERP-17584
            maxLength:45,
            scope:this

        });
        this.Memo=new Wtf.form.TextArea({
            fieldLabel:Wtf.account.companyAccountPref.descriptionType,  //'Memo/Note',
            name: 'memo',
            id:"memo"+this.heplmodeid+this.id,
            height:40,
//            anchor:'94%',
            width : 240,
//            allowBlank:false,
            maxLength:2048,
            readOnly:this.isViewTemplate,
            disabled:this.readOnly,
            qtip:(this.record==undefined)?' ':this.record.data.memo,
            listeners: {
                render: function(c){
                    Wtf.QuickTips.register({
                        target: c.getEl(),
                        text: c.qtip
                    });
                }
            }
        });
        this.Discount=new Wtf.form.NumberField({
            allowNegative:false,
            hidden:true,
            defaultValue:0,
            hideLabel:true,
            allowBlank:this.isOrder,
            maxLength: 10,
            width:100,
            fieldLabel:WtfGlobal.getLocaleText("acc.invoiceList.discount"),  //'Discount',
            name:'discount',
            id:"discount"+this.heplmodeid+this.id,
            listeners:{
                'change':{
                    fn:this.updateSubtotal,
                    scope:this
                }
            }
        });
        this.perDiscount= new Wtf.form.ComboBox({
            labelSeparator:'',
            labelWidth:0,
            triggerAction:'all',
            mode: 'local',
            valueField:'value',
            displayField:'name',
            store:this.perDiscountStore,
            hidden:true,
            hideLabel:true,
            allowBlank:this.isOrder,
            value:false,
            width:100,
            typeAhead: true,
            forceSelection: true,
            name:'perdiscount',
            hiddenName:'perdiscount',
            listeners:{
                'select':{
                    fn:this.updateSubtotal,
                    scope:this
                }
            }
        });
         this.sequenceFormatStoreRecDo = new Wtf.data.Record.create([
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
  this.sequenceFormatStoreDo = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.sequenceFormatStoreRecDo),
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:this.isCustomer ?"autodo":"autogro"
            }
        });
          this.sequenceFormatStoreDo.on('load',function(){
             if(this.sequenceFormatStoreDo.getCount()>0){
                 var seqRec=this.sequenceFormatStoreDo.getAt(0)
                this.sequenceFormatComboboxDo.setValue(seqRec.data.id);
                this.getNextSequenceNumberDo(this.sequenceFormatComboboxDo);
             }
         },this);
      if((this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId  ||this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  ))//only load when customer Invoice
          this.sequenceFormatStoreDo.load();
         
       this.sequenceFormatComboboxDo = new Wtf.form.ComboBox({            
        triggerAction:'all',
        mode: 'local',
        fieldLabel:WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
        valueField:'id',
        displayField:'value',
        store:this.sequenceFormatStoreDo,
        width:240,
        maxLength:2048,
        typeAhead: true,
        forceSelection: true,
        name:this.isCustomer ?'sequenceformatDo':'sequenceformatGR',
        hiddenName:this.isCustomer ?'sequenceformatDo':'sequenceformatGR',
        hideLabel:!(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId  ||this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  ) || this.isFixedAsset || this.isLeaseFixedAsset,
        hidden: !(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId  ||this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  ) || this.isFixedAsset || this.isLeaseFixedAsset,
        listeners:{
            'select':{
                fn:this.getNextSequenceNumberDo,
                scope:this
            }
        }
            
    });
    
     this.no=new Wtf.form.TextField({
            fieldLabel:this.isCustomer ?"Delivery Order Number*" :"Goods Receipt Number*" ,
            name:this.isCustomer ? 'numberDo' : 'numberGR',
            scope:this,
            maxLength:45,
            width : 240,
            maxLength:2048,
            hiddenName:this.isCustomer ?'numberDo':'numberGR',
            hideLabel:!(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId  ||this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId ) || this.isFixedAsset || this.isLeaseFixedAsset,
            hidden: !(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId  ||this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId ) || this.isFixedAsset || this.isLeaseFixedAsset,
//            hideLabel:true,
//            hidden:true,
     //       anchor:'90%',
            allowBlank:false
        });
         this.PORec = Wtf.data.Record.create ([
            {name:'billid'},
            {name:'journalentryid'},
            {name:'entryno'},
            {name:'billto'},
            {name:'discount'},
            {name:'shipto'},
            {name:'mode'},
            {name:'billno'},
            {name:'date', type:'date'},
            {name:'duedate', type:'date'},
            {name:'shipdate', type:'date'},
            {name:'personname'},
            {name:'creditoraccount'},
            {name:'personid'},
            {name:'shipping'},
            {name:'othercharges'},
            {name:'taxid'},
            {name:'discounttotal'},
            {name:'discountispertotal',type:'boolean'},
            {name:'currencyid'},
            {name:'amount'},
            {name:'amountinbase'},
            {name:'amountdue'},
            {name:'costcenterid'},
            {name:'costcenterName'},
            {name:'memo'},
            {name:'shipvia'},
            {name:'fob'},
            {name:'includeprotax',type:'boolean'},
            {name:'gstIncluded'},
            {name:'salesPerson'},
            {name:'islockQuantityflag'},
            {name:'termdetails'},
            {name:'replacementQuantity'},
            {name:'isAsset'},
            {name:'contractstatus'},
            {name:'contract'},
            {name:'batchSerialId'},
            {name:'contractId'},
            {name:'agent'},
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
            {name: 'vendcustShippingAddressType'},
            /**
             * If Show Vendor Address in purchase side document and India country 
             * then this Fields used to store Vendor Billing Address
             */
            {name: 'vendorbillingAddressTypeForINDIA'},
            {name: 'vendorbillingAddressForINDIA'},
            {name: 'vendorbillingCountryForINDIA'},
            {name: 'vendorbillingStateForINDIA'},
            {name: 'vendorbillingPostalForINDIA'},
            {name: 'vendorbillingEmailForINDIA'},
            {name: 'vendorbillingFaxForINDIA'},
            {name: 'vendorbillingMobileForINDIA'},
            {name: 'vendorbillingPhoneForINDIA'},
            {name: 'vendorbillingContactPersonForINDIA'},
            {name: 'vendorbillingRecipientNameForINDIA'},
            {name: 'vendorbillingContactPersonNumberForINDIA'},
            {name: 'vendorbillingContactPersonDesignationForINDIA'},
            {name: 'vendorbillingWebsiteForINDIA'},
            {name: 'vendorbillingCountyForINDIA'},
            {name: 'vendorbillingCityForINDIA'},
            {name: 'isapplytaxtoterms'},
            {name:'CustomerVendorTypeId'},
            {name:'GSTINRegistrationTypeId'},
            {name:'gstin'},
            {name:'gstdochistoryid'},
            {name:'externalcurrencyrate'},
        ]);
        this.POStoreUrl = "";
        var closeFlag = true;
        if(this.businessPerson=="Customer"){
            //mode:(this.isCustBill?52:42)
            if(this.quotation){                
                this.POStoreUrl = "ACCPurchaseOrderCMN/getQuotations.do"
            }else if(this.isOrder) {
                  if(this.isLeaseFixedAsset){
                      this.POStoreUrl = "ACCSalesOrderCMN/getQuotations.do";
                  }else{
                      this.POStoreUrl = "ACCSalesOrderCMN/getQuotations.do"
                      if(this.isSOfromPO){
                          closeFlag = false;
                          this.POStoreUrl = this.isCustBill?"ACCPurchaseOrderCMN/getBillingPurchaseOrders.do":"ACCPurchaseOrderCMN/getPurchaseOrders.do";
                      }
                  }
            } else {
                this.POStoreUrl = this.isCustBill?"ACCSalesOrderCMN/getBillingSalesOrders.do":"ACCSalesOrderCMN/getSalesOrders.do";
            }
            
            
        }else if(this.businessPerson=="Vendor"){
            if(this.isOrder) {
                if(this.isPOfromSO){
                    closeFlag = false;
                }    
                this.POStoreUrl = this.isCustBill?"ACCSalesOrderCMN/getBillingSalesOrders.do":"ACCSalesOrderCMN/getSalesOrders.do";
            } else {
                this.POStoreUrl = this.isCustBill?"ACCPurchaseOrderCMN/getBillingPurchaseOrders.do":"ACCPurchaseOrderCMN/getPurchaseOrders.do";
            }
        }
        this.POStore = new Wtf.data.Store({
            url:this.POStoreUrl,
    //        url: Wtf.req.account+this.businessPerson+'Manager.jsp',
            baseParams:{
                mode:(this.isCustBill?52:42),
                closeflag:closeFlag,
                isFixedAsset: this.isFixedAsset,
                requestModuleid:this.moduleid
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.PORec)
        });
        var colModelArray = GlobalDimensionCustomFieldModel[this.moduleid];
        if(colModelArray){
           for(var cnt = 0;cnt < colModelArray.length;cnt++){
               var fieldname = colModelArray[cnt].fieldname;
               var newField = new Wtf.data.Field({
                   name:fieldname.replace(".",""),
//                   sortDir:'ASC',
                   type:colModelArray[cnt].fieldtype == 3 ?  'date' : (colModelArray[cnt].fieldtype == 2?'float':'auto'),
                   format:colModelArray[cnt].fieldtype == 3 ?  'time' : undefined
               });
               this.POStore.fields.items.push(newField);
               this.POStore.fields.map[fieldname]=newField;
               this.POStore.fields.keys.push(fieldname);
           }
           this.POStore.reader = new Wtf.data.KwlJsonReader(this.POStore.reader.meta, this.POStore.fields.items);
       }
        this.fromPO= new Wtf.form.ComboBox({
            triggerAction:'all',
            hideLabel:this.cash|| (this.isOrder && this.isCustBill) || this.isTemplate,
            hidden:this.cash|| (this.isOrder && this.isCustBill) || this.isTemplate,
            mode: 'local',
            valueField:'value',
            displayField:'name',
            disabled:this.isEdit?false:true,
            store:this.fromPOStore,
            id: "linkToOrder"+this.heplmodeid+this.id,
            fieldLabel:((!this.isCustBill && !this.isOrder && !this.cash)?WtfGlobal.getLocaleText("acc.field.Link"):(this.isOrder && this.isCustomer)? (this.isSOfromPO)?"Link to Purchase Order":(WtfGlobal.getLocaleText("acc.field.Link")) :(this.isOrder && !this.isCustomer)?WtfGlobal.getLocaleText("acc.field.Link"): (this.isCustomer?WtfGlobal.getLocaleText("acc.invoice.linkToSO"):WtfGlobal.getLocaleText("acc.invoice.linkToPO"))) ,  //"Link to "+(this.isCustomer?"Sales":"Purchase")+" Order",
            allowBlank:this.isOrder,
            value:false,
            width:50,
            typeAhead: true,
            forceSelection: true,
            name:'prdiscount',
            hiddenName:'prdiscount',
            listeners:{
                'select':{
                    fn:this.enablePO,
                    scope:this
                }
            }
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
     Wtf.salesPersonStore.on('beforeload',this.onBeforesalesPersonLoad,this);
     Wtf.agentStore.on('beforeload',this.onBeforeAgentLoad,this);
    if(!(this.isOrder)){ //
         this.isCustomer ? Wtf.salesPersonStore.load() : Wtf.agentStore.load();    
    }

//    this.salesPersonStore

    this.users= new Wtf.form.FnComboBox({            
            triggerAction:'all',
            mode: 'remote',
//            selectOnFocus:true,
            valueField:'id',
            displayField:'name',
            id:"salesperson"+this.heplmodeid+this.id,
            store:this.isCustomer ? Wtf.salesPersonStore : Wtf.agentStore,
            addNoneRecord: true,
//            anchor: '94%',
            width : 240,
//            typeAhead: true,
            forceSelection: true,
            fieldLabel: this.isCustomer ? WtfGlobal.getLocaleText("acc.invoiceList.salesPerson") : WtfGlobal.getLocaleText("acc.masterConfig.20"),
            emptyText: this.isCustomer ? WtfGlobal.getLocaleText("acc.field.SelectSalesPerson") : WtfGlobal.getLocaleText("acc.field.SelectAgent"),
//            hideLabel:(this.isOrder && this.quotation),
//            hidden:( this.isOrder && this.quotation),
            name:this.isCustomer ? 'salesPerson' : 'agent',
            hiddenName:this.isCustomer ? 'salesPerson' : 'agent'            
        });
        
            this.users.addNewFn=this.addSalesPerson.createDelegate(this);
      
      this.lockQuantity= new Wtf.form.Checkbox({
            name:'lockQuantity',
             id:'lockQuantitySO'+this.heplmodeid+this.id,
            hiddeName:'lockQuan',
            fieldLabel:WtfGlobal.getLocaleText("Block Quantity"),  
            checked:false,
            hideLabel:(!this.isCustomer || (this.isCustomer && (!this.isOrder || this.quotation))),
            hidden:( !this.isCustomer || (this.isCustomer && (!this.isOrder || this.quotation))),                        
            cls : 'custcheckbox',
            width: 10
        }); 
        
        this.copyAddress= new Wtf.form.Checkbox({
            name:'copyadress',
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.sameasbillingadd"),  //'Copy Address',
            checked:false,
            hideLabel:(this.quotation || !this.isCustomer || this.isOrder),
            hidden:(this.quotation || !this.isCustomer || this.isOrder),
            cls : 'custcheckbox',
            width: 10
        });
        
        this.generateReceipt= new Wtf.form.Checkbox({
            name:'generateReceipt',
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.generateReceipt"),  //'Generate Receipt',
            checked:false,
            hideLabel:(this.quotation || this.isOrder || !this.cash || !this.isCustomer),//|| !this.isCustomer || this.isOrder),
            hidden:(this.quotation || this.isOrder || !this.cash || !this.isCustomer),//|| !this.isCustomer || this.isOrder),
            cls : 'custcheckbox',
            width: 10
        });
        
        this.capitalGoodsAcquired= new Wtf.form.Checkbox({
            name:'isCapitalGoodsAcquired',
            id:"isCapitalGoodsAcquired"+this.heplmodeid+this.id,
            fieldLabel:WtfGlobal.getLocaleText("acc.capital.goods.acquired"),  //'Capital Goods Acquired',
            checked: false,
            hideLabel: !(!this.isExpenseInv && (this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId ) && Wtf.account.companyAccountPref.countryid=='137'),// if country is Malasia and this is an vendor Invoice and not an expense invoice then only it will be showns
            hidden: !((this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId ) && Wtf.account.companyAccountPref.countryid=='137'),
            cls : 'custcheckbox',
            width: 10
        });

        this.capitalGoodsAcquired.on('check',this.capitalGoodsAcquiredHandler,this);
        
        this.autoGenerateDO= new Wtf.form.Checkbox({
            name:'autogenerateDO',
            id:"autogenerateDO"+this.heplmodeid+this.id,
            fieldLabel:this.isCustomer ? WtfGlobal.getLocaleText("acc.cust.generateDO") : WtfGlobal.getLocaleText("acc.vend.generateGR"),  //'Generate Delivery Order',
            checked:this.GRDOSettings != null ? this.GRDOSettings : false,
            hideLabel:(this.isWithInvUpdate == null? true: !this.isWithInvUpdate) || !(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId  ||this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId ) || this.isFixedAsset || this.isLeaseFixedAsset,
            hidden:(this.isWithInvUpdate == null? true: !this.isWithInvUpdate) || !(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId  ||this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId ) || this.isFixedAsset || this.isLeaseFixedAsset,
            cls : 'custcheckbox',
            width: 10
        });  
        
        this.autoGenerateDO.on('check',function(o,newval,oldval){
            if(this.autoGenerateDO.getValue()){
              this.showDO();
            }else{
               this.hideDO();
            }            
        },this);
                 
        this.templateRec = new Wtf.data.Record.create([
            {name: 'tempid'},
            {name: 'tempname'}
        ]);
        
        this.templateStore = new Wtf.data.Store({
            url : "ExportPDF/getAllReportTemplate.do",
            
            method: 'GET',
            baseParams : {
                templatetype : this.doctype
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.templateRec)
        });
         this.templateStore.load();
         this.templateStore.on("load", function() {
            if (!this.isEdit)
                this.template.setValue(Wtf.Acc_Basic_Template_Id);
        }, this);
        this.template= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.invoice.grid.header.template")+"*",
            hiddenName:"template",
//            anchor:"94%",
            width : 240,
            store: this.templateStore,
            valueField:'tempid',
            displayField:'tempname',
//            itemCls : (!this.isCustBill)?"hidden-from-item":"",
//            allowBlank:!this.isCustBill||this.isOrder,
//            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.invoice.grid.template.emptyText"),
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            allowBlank:true, 
            hidden:true, 
            hideLabel:true,      
            selectOnFocus:true,
           // value : this.isEdit ? this.record.data.templatename :'',
            triggerAction:'all',
//            addNewFn: this.addAccount.createDelegate(this,[this.allAccountStore],true),
            scope:this,
            listeners:{
                'change':{
                    fn:this.setTemplateID,
                    scope:this
                }
            }
        });
        this.templateID=new Wtf.form.Hidden({
        	scope:this,
        	value: this.isEdit ? this.record.data.templateid : ''
        });
//        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.create))
        this.template.addNewFn=this.addInvoiceTemplate.createDelegate(this,[this.templateStore],true);
        this.billDate = new Wtf.form.DateField({
            fieldLabel: (this.isPOfromSO ? WtfGlobal.getLocaleText("acc.accPref.autoPO") : (this.isSOfromPO) ? WtfGlobal.getLocaleText("acc.accPref.autoSO") : this.label) + ' ' + WtfGlobal.getLocaleText("acc.invoice.date"),
            id: "invoiceDate" + this.heplmodeid + this.id,
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'billdate',
            maxValue: this.isOpeningBalanceOrder ? this.getFinancialYRStartDatesMinOne(true) : null,
//            anchor:'50%',
            width: 240,
            listeners: {
                'change': {
                    fn: this.updateDueDate,
                    scope: this
                }
            },
            allowBlank: (this.isTemplate && !this.createTransactionAlso)
        });
        this.includeTaxStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['Yes',true],['No',false]]
        });
        
        this.partialInvoiceStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['Yes',true],['No',false]]
        });
        
        this.isTaxShouldBeEnable=true;
        if (Wtf.account.companyAccountPref.countryid === Wtf.CountryID.MALAYSIA) { 
        this.isTaxShouldBeEnable = WtfGlobal.isTaxShouldBeEnable(new Date(this.billDate.getValue()).clearTime());
            if(this.importService && this.importService.getValue()){
                this.isTaxShouldBeEnable = false;
            }
        }
        
        /*
         * added Include GST Related field
         */
        this.includingGST= new Wtf.form.Checkbox({
            name:'includingGST',
            id:"includingGST"+this.heplmodeid+this.id,
            hideLabel: (SATSCOMPANY_ID==companyid || Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA) ?true:(Wtf.account.companyAccountPref.countryid == Wtf.Country.MALAYSIA && (!Wtf.account.companyAccountPref.enableGST || !this.shouldTaxBeHidden())),
            hidden: (SATSCOMPANY_ID==companyid || Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA) ?true:(Wtf.account.companyAccountPref.countryid == Wtf.Country.MALAYSIA && (!Wtf.account.companyAccountPref.enableGST || !this.shouldTaxBeHidden())),
            fieldLabel:this.checkToIncludeGstORVatORTax(this),
            cls : 'custcheckbox',
            width: 10
        });
        
        this.includingGST.on('focus',function(o,newval,oldval){
            var includeGstCount=0;
            var excludeGstCount=0;
            var selectedids=this.PO.getValue();
            var selectedValuesArr = selectedids.split(',');
            for(var cntGst=0;cntGst<selectedValuesArr.length;cntGst++){
                var rec=this.POStore.getAt(this.POStore.find('billid',selectedValuesArr[cntGst]));
                if(rec!=undefined){
                    if(rec.data["gstIncluded"]){
                        includeGstCount++;
                    }else if(!rec.data["gstIncluded"]){
                        excludeGstCount++;
                    }
                }
            }
            if(this.fromPO.getValue()!=undefined && this.fromPO.getValue()==true && this.PO.getValue()!=""){
                var message="";
                if(selectedValuesArr.length==includeGstCount && this.includingGST.getValue()){
                    message=WtfGlobal.getLocaleText("acc.inclidingGST");
                }else if(selectedValuesArr.length==excludeGstCount&&!this.includingGST.getValue()){
                    message=WtfGlobal.getLocaleText("acc.excludingGST");
                }
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"),message,function(btn){
                    if(btn!="yes") {
                        return;
                    }
    //                    this.fromLinkCombo.emptyText = "";
    //                    this.fromLinkCombo.clearValue();
                    if(!((selectedValuesArr.length==includeGstCount&&!this.includingGST.getValue())||(selectedValuesArr.length==excludeGstCount&&this.includingGST.getValue()))){
                        this.PO.clearValue();
                    }
                    var value=this.includingGST.getValue()
                    this.includingGST.setValue(!value);
                },this);
            }
        },this);
        
        this.includingGST.on('change',function(o,newval,oldval){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.gridDataWillCleared"),function(btn){
                if(btn=="yes") {
                    this.Grid.getStore().removeAll();
                    this.Grid.addBlankRow();
                    if(this.termgrid != undefined && this.termgrid != null){
                        this.termgrid.getStore().rejectChanges();
                    }
                    if(this.applyGlobalDiscount){
                        this.applyGlobalDiscount.setValue(false);
                    }
                    this.Grid.fireEvent('datachanged', this);
                    if(newval){
                        this.includeProTax.disable();
                        this.includeProTax.setValue(true);
                        /*True means Hidden False*/
                        /*This function written in CommonERPComponent.js*/
//                        this.HideShowColFromInvoiceTermGrid(true);
//                        this.setSingleMappedTaxToInvoiceTerm(true);
                    } else {
                        this.isViewTemplate==true?this.includeProTax.disable():this.includeProTax.enable();
                        this.includeProTax.enable();
                        /*False means Hidden True*/
                        /*This function written in CommonERPComponent.js*/
                        this.HideShowTermAmountExcludingTaxCol(false);
//                        this.setSingleMappedTaxToInvoiceTerm(false);
//                        this.resetLinkedTaxNameAndTermTaxAmount(false);

                        /*
                            * If product tax is disabled then global level tax should be enabled and in view mode it should be disabled ERP-32672
                            */
                        this.isViewTemplate==true?this.isTaxable.disable():this.isTaxable.enable();
                        this.isViewTemplate==true?this.Tax.disable():this.Tax.enable();
                    }
                    var rec=WtfGlobal.searchRecord(this.includeProTax.store, true, 'value');
                    if(rec!=null){
                        this.includeProTaxHandler(this.includeProTax,rec,!this.includeProTax.getValue());            
                    }
                } else {
                    o.setValue(oldval);
                }
            },this);
        },this);
        
        this.includeProTax= new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            valueField:'value',
            displayField:'name',
            store:this.includeTaxStore,
           // id: "linkToOrder"+this.id+this.heplmodeid,
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.includeprodtax.tip")+"'>"+WtfGlobal.getLocaleText("acc.invoice.productTax") +"</span>",//"Include Product Tax",
            id:"includeprotax"+this.heplmodeid+this.id,
          //  allowBlank:this.isOrder,
            value:(this.isEdit?true:false),
//            anchor:'94%',
            width : 240,
            typeAhead: true,
            forceSelection: true,
            hideLabel:(Wtf.account.companyAccountPref.countryid == '137' && (!Wtf.account.companyAccountPref.enableGST || !this.shouldTaxBeHidden())) || Wtf.account.companyAccountPref.isLineLevelTermFlag==1,// hide if company is malaysian and GST is not enabled for it
            hidden:(Wtf.account.companyAccountPref.countryid == '137' && (!Wtf.account.companyAccountPref.enableGST || !this.shouldTaxBeHidden())) || Wtf.account.companyAccountPref.isLineLevelTermFlag==1, // hide if company is malaysian and GST is not enabled for it
            name:'includeprotax',
            hiddenName:'includeprotax',
            listeners:{
                'select':{
                    fn:this.includeProTaxHandler,
                    scope:this
                }
            }
        });
        
        this.applyTaxToTermsChk = new Wtf.form.Checkbox({
            fieldLabel:"Apply Tax to Terms"
        }),
//        this.applyTaxToTermsChk.on("check", function () {
//            this.updateSubtotal();
//        }, this);
        
        this.validTillDate = new Wtf.form.DateField({
            fieldLabel : WtfGlobal.getLocaleText("acc.common.validTill"),  //"Valid Till",
            format : WtfGlobal.getOnlyDateFormat(),
            name : 'validdate',
            id : "validdate"+this.heplmodeid+this.id,
            width : 240,
            hidden :!(this.moduleid==65 || this.moduleid==22 || this.moduleid==23),
            hideLabel : !(this.moduleid==65 || this.moduleid==22 || this.moduleid==23)
//            anchor:'94%'
        });
        /*
         * Hidden true for Asset and Lease Modules
         */
        this.partialInvoiceCmb= new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            valueField:'value',
            hidden : true,
            hideLabel : true,
            displayField:'name',
            store:this.partialInvoiceStore,
           // id: "linkToOrder"+this.id+this.heplmodeid,
            fieldLabel:WtfGlobal.getLocaleText("acc.field.IsPartialInvoice"),
            id:"isPartialInv"+this.heplmodeid+this.id,
          //  allowBlank:this.isOrder,
            value:false,
//            anchor:'94%',
            width : 240,
            disabled : true,
            typeAhead: true,
            forceSelection: true,
            name:'partialinv',
            hiddenName:'partialinv',
            listeners:{
                'select':{
                    fn:this.showPartialDiscount,
                    scope:this
                }
            }
        });
        /*
         *hideLabel:(!this.isCustBill && !(this.isOrder&&this.isCustomer) && !this.cash )?false:true,
            hidden:(!this.isCustBill && !(this.isOrder&&this.isCustomer) && !this.cash )?false:true,
         **/
        var emptyText = "Select VQ/SO";
        if(!this.isCustBill){
            if(this.isOrder && !this.isCustomer) {
                emptyText = "Select VQ/SO";
            } else if(this.isOrder&&this.isCustomer && !this.quotation){
                emptyText = "Select LQ/RN";
            } else if(this.isCustomer && this.quotation){
                emptyText = "Select RN";//VQ/RN
            } else {
                emptyText = Wtf.account.companyAccountPref.withinvupdate? (this.isCustomer? (this.isLeaseFixedAsset?WtfGlobal.getLocaleText("acc.field.SelectLeaseDO"):WtfGlobal.getLocaleText("acc.field.SelectAssetDO")) : WtfGlobal.getLocaleText("acc.field.SelectPO/GR/VQ")) : (this.isCustomer? "Select SO/CQ" : "Select PO/VQ");
            }
        }
        
        if (this.moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId) {
            emptyText = WtfGlobal.getLocaleText("acc.field.selectAssetPR");
        } else if (this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId) {
            emptyText = WtfGlobal.getLocaleText("acc.field.selectAssetVQ");
        }
        
        this.fromLinkCombo= new Wtf.form.ComboBox({
            name:'fromLinkCombo',
            triggerAction:'all',
            hideLabel:(this.isCustBill  || this.cash || this.isTemplate)?true:false,
            hidden:(this.isCustBill || this.cash || this.isTemplate)?true:false,
            mode: 'local',
            valueField:'value',
            id:'fromLinkComboId'+this.heplmodeid+this.id,
            displayField:'name',
            disabled:true,
            store:this.fromlinkStore,                        
            emptyText: emptyText,//Wtf.account.companyAccountPref.withinvupdate? (this.isCustomer? "Select SO/DO" : "Select PO/GR") : (this.isCustomer? "Select SO" : "Select PO"),
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Linkto"),  //"Link to "+(this.isCustomer?"Sales":"Purchase")+" Order",
            allowBlank:false,            
//            value:false,            
            typeAhead: true, 
            width:130,
            forceSelection: true,                        
            selectOnFocus:true,           
            scope:this,
            listeners:{
                'select':{
                    fn:this.enableNumber,
                    scope:this
                }
            }
        });
        
//        this.PO= new Wtf.form.FnComboBox({
//            fieldLabel:"Number",//(!this.isCustBill && !this.isOrder && !this.cash && this.isCustomer)?"Number ":((this.isOrder && this.isCustomer)? WtfGlobal.getLocaleText("acc.invoice.QO") : (this.isCustomer?WtfGlobal.getLocaleText("acc.invoice.SO"):WtfGlobal.getLocaleText("acc.invoice.PO"))) ,  //(this.isCustomer?"SO":"PO")+" Number",
//            hiddenName:"ordernumber",
//            id:"orderNumber"+this.heplmodeid+this.id,
//            store: this.POStore,
//            valueField:'billid',
//            hideLabel:this.cash|| this.quotation|| (this.isOrder && this.isCustBill),
//            hidden:this.cash||this.quotation|| (this.isOrder && this.isCustBill),
//            displayField:'billno',
//            disabled:true,
//            emptyText:this.isOrder ? (( this.isCustomer)? WtfGlobal.getLocaleText("acc.inv.QOe") : WtfGlobal.getLocaleText("acc.inv.SOe")) : (Wtf.account.companyAccountPref.withinvupdate ? (this.isCustomer?"Select SO/DO":"Select PO/GR") : (this.isCustomer?WtfGlobal.getLocaleText("acc.inv.SOe"):WtfGlobal.getLocaleText("acc.inv.POe"))),
//            mode: 'local',
//            typeAhead: true,
//            forceSelection: true,
//            selectOnFocus:true,
//            anchor:"50%",
//            triggerAction:'all',
////            addNewFn:this.addOrder.createDelegate(this,[false,null,this.businessPerson+"PO"],true),
//            scope:this,
//            listeners:{
//                'select':{
//                    fn:this.populateData,
//                    scope:this
//                }
//            }
//        });

           this.MSComboconfig = {
                hiddenName:"ordernumber",
                //id:"orderNumber"+this.heplmodeid+this.id,         
                store: this.POStore,
                valueField:'billid',
                hideLabel:this.cash|| (this.isOrder && this.isCustBill) || this.isTemplate,
                hidden:this.cash|| (this.isOrder && this.isCustBill) || this.isTemplate,
                displayField:'billno',
                disabled:true,
                emptyText:this.isOrder ? (( this.isCustomer)? ((this.isLeaseFixedAsset)?((this.quotation)?WtfGlobal.getLocaleText("acc.inv.VQOrReplacement"):WtfGlobal.getLocaleText("acc.inv.QOeOrReplacement")):WtfGlobal.getLocaleText("acc.inv.QOe")) : this.isFixedAsset? ((this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId)? WtfGlobal.getLocaleText("acc.field.selectAssetVQ") : WtfGlobal.getLocaleText("acc.field.selectAssetPR")) : "Select VQ/SO") : (Wtf.account.companyAccountPref.withinvupdate ? (this.isCustomer?(this.isLeaseFixedAsset?WtfGlobal.getLocaleText("acc.field.SelectLeaseDeliveryOrder"):WtfGlobal.getLocaleText("acc.field.SelectAssetDeliveryOrder")):"Select PO/GR/VQ") : (!this.isCustBill)?(this.isCustomer?"Select SO/CQ":"Select PO/VQ"):(this.isCustomer?WtfGlobal.getLocaleText("acc.inv.SOe"):WtfGlobal.getLocaleText("acc.inv.POe"))),
                mode: 'local',
                typeAhead: true,
                selectOnFocus:true,
                clearTrigger:this.isEdit ? false : true,
                allowBlank:false,
                triggerAction:'all',
                scope:this
//                listeners:{                       
//                    'blur':{
//                        fn:this.populateData,
//                        scope:this
//                    }
//                }
            };

        this.PO = new Wtf.common.Select(Wtf.applyIf({
             multiSelect:true,
             fieldLabel:WtfGlobal.getLocaleText("acc.field.Number") ,
             id:"poNumberID"+this.heplmodeid+this.id ,
             forceSelection:true,
             width:240
        },this.MSComboconfig));
        
        this.PO.on("clearval",function(){
            if(this.PO.getValue()=="" && !this.isEdit && !this.handleEmptyText){            
                this.Grid.getStore().removeAll();            
                this.Grid.addBlankRow();      
                var fieldArr = this.POStore.fields.items;
                for(var fieldCnt=0; fieldCnt < fieldArr.length; fieldCnt++) {
                    var fieldN = fieldArr[fieldCnt];
                    if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id)) {
                        Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue('');
                    }
                }
            }
            this.handleEmptyText=false;
        },this);

        if (Wtf.account.companyAccountPref.enableLinkToSelWin && (this.moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId || this.moduleid==Wtf.Acc_Lease_Order || this.moduleid==Wtf.LEASE_INVOICE_MODULEID)) {
            this.POStore.on('load', function(){addMoreOptions(this.PO,this.PORec)}, this);            
            this.POStore.on('datachanged', function(){addMoreOptions(this.PO,this.PORec)}, this);            
            this.PO.on("select", function () {
                var billid = this.PO.getValue();
                if (billid.indexOf("-1") != -1) {
                    var url = "";
                    if(this.fromLinkCombo.getValue() == 5){
                        url = "ACCPurchaseOrderCMN/getRequisitions.do";
                    }else if(this.fromLinkCombo.getValue() == 2){
                        url = this.isCustomer ? "ACCSalesOrderCMN/getQuotations.do" : "ACCPurchaseOrderCMN/getQuotations.do";
                    }else if(this.fromLinkCombo.getValue() == 0){
                        if (this.isLeaseFixedAsset && this.isOrder && this.isCustomer && !this.quotation) {
                            url = "ACCSalesOrderCMN/getQuotations.do"
                            if (this.isSOfromPO) {
                                url = this.isCustBill ? "ACCPurchaseOrderCMN/getBillingPurchaseOrders.do" : "ACCPurchaseOrderCMN/getPurchaseOrders.do";
                            }
                           
                        } else if (this.isLeaseFixedAsset && this.isCustomer && this.quotation) {
                            url = "ACCPurchaseOrderCMN/getQuotations.do"
                        } else {
                            if (this.isOrder) {
                                url = "ACCSalesOrderCMN/getSalesOrders.do";
                            } else {
                                url = this.isCustomer ? "ACCSalesOrderCMN/getSalesOrders.do" : "ACCPurchaseOrderCMN/getPurchaseOrders.do";
                            }
                        }
                    }else if (this.fromLinkCombo.getValue() == 1){
                        if (this.isLeaseFixedAsset && this.isOrder && this.isCustomer && !this.quotation) {
                            url = "ACCSalesOrderCMN/getReplacementRequests.do";
                        } else if (this.isLeaseFixedAsset && this.isCustomer && this.quotation) {
                            url = "ACCSalesOrderCMN/getReplacementRequests.do";
                        } else {
                            url = this.isCustomer ? "ACCInvoiceCMN/getDeliveryOrdersMerged.do" : "ACCGoodsReceiptCMN/getGoodsReceiptOrdersMerged.do";
                        }
                    }
                    this.PO.collapse();
                    this.PO.clearValue();
                    this.showPONumbersGrid(url);
                }
            }, this);
        }
        if(!WtfGlobal.EnableDisable(this.soUPermType, this.soPermType))
            this.PO.addNewFn=this.addOrder.createDelegate(this,[false,null,this.businessPerson+"PO"],true)
        this.DueDate= new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.invoice.dueDate"),//'Due Date*',
            name: 'duedate',
            id: "duedate"+this.heplmodeid+this.id,
            itemCls : (this.cash||this.isOrder)?"hidden-from-item":"",
            hideLabel:this.cash||this.isOrder,
            hidden:this.cash||this.isOrder,
            format:WtfGlobal.getOnlyDateFormat(),
            allowBlank:false,
            width : 240
//            anchor:'94%'
        });

       
        this.shipDate= new Wtf.form.DateField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.ShipDate.tip") +"'>"+ WtfGlobal.getLocaleText("acc.field.ShipDate")+"</span>",//WtfGlobal.getLocaleText("acc.field.ShipDate"),
            format:WtfGlobal.getOnlyDateFormat(),
            id:"shipdate"+this.heplmodeid+this.id,
            name: 'shipdate',
            width : 240,
            listeners:{
                'change':{
                    fn:this.updateDueDate,
                    scope:this
                }
            }
//            anchor:'94%'
        });
        this.shipvia = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.ShipVia"),
            name: 'shipvia',
            id:"shipvia"+this.heplmodeid+this.id,
//            anchor: '94%',
            width : 240,
            maxLength: 255,
            scope: this
        });
        
        this.fob = new Wtf.form.TextField({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.fob.tip")+"'>"+WtfGlobal.getLocaleText("acc.field.FOB") +"</span>",
            name: 'fob',
            id:"fob"+this.heplmodeid+this.id,
//            anchor: '94%',
            width : 240,
            maxLength: 255,
            scope: this
        });
        
        var isbchlFields=(this.quotation||this.isCustomer || !this.isOrder ||this.isCustBill || (BCHLCompanyId.indexOf(companyid) == -1));
        this.youtReftxt = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.yourref.label"), //'Vendor Invoice Number*',
            name: 'poyourref',
            hidden: isbchlFields ,
            hideLabel: isbchlFields ,
            id: "poyourref" + this.heplmodeid + this.id,
//            anchor: "50%",
            width : 240,
            maxLength: 255,
            scope: this
        });
        this.delydatetxt = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.delydate.label"), //'Vendor Invoice Number*',
            name: 'delydate',
            hidden: isbchlFields ,
            hideLabel: isbchlFields ,
            id: "delydate" + this.heplmodeid + this.id,
//            anchor: '94%',
            width : 240,
            maxLength: 255,
            scope: this
        });
        this.delytermtxt = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.delyterm.label"), //'Vendor Invoice Number*',
            name: 'delyterm',
            hidden: isbchlFields,
            hideLabel:isbchlFields ,
            id: "delyterm" + this.heplmodeid + this.id,
//            anchor: "50%",
            width : 240,
            height: 40,
            maxLength: 255,
            scope: this
        });
        this.invoiceTotxt = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.invoiceto.label"), //'Vendor Invoice Number*',
            name: 'invoiceto',
            hidden:isbchlFields ,
            hideLabel: isbchlFields ,
            id: "invoiceto" + this.heplmodeid + this.id,
//            anchor: '50%',
            width : 240,
            maxLength: 255,
            scope: this
        });
        this.projecttxt = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.project.label"), //'Vendor Invoice Number*',
            name: 'project',
            hidden: isbchlFields ,
            hideLabel: isbchlFields ,
            id: "project" + this.heplmodeid + this.id,
//            anchor: '94%',
            width : 240,
            maxLength: 255,
            scope: this
        });
        this.depttxt = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.dept.label"), //'Vendor Invoice Number*',
            name: 'podept',
            hidden:isbchlFields ,
            hideLabel: isbchlFields ,
            id: "podept" + this.heplmodeid + this.id,
//            anchor: '94%',
            width : 240,
            maxLength: 255,
            scope: this
        });
        this.requestortxt = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.requestor.label"), //'Vendor Invoice Number*',
            name: 'requestor',
            hidden: isbchlFields ,
            hideLabel:isbchlFields ,
            id: "requestor" + this.heplmodeid + this.id,
//            anchor: '94%',
            width : 240,
            maxLength: 255,
            scope: this
        });
        this.mernotxt = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.merno.label"), //'Vendor Invoice Number*',
            name: 'merno',
            hidden: isbchlFields,
            hideLabel: isbchlFields ,
            id: "merno" + this.heplmodeid + this.id,
//            anchor: '94%',
            width : 240,
            maxLength: 255,
            scope: this
        });
        chkFormCostCenterload();
        this.CostCenter=  new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.costCenter.tip") +"'>"+ WtfGlobal.getLocaleText("acc.common.costCenter")+"</span>",//WtfGlobal.getLocaleText("acc.common.costCenter"),//"Cost Center",
            hiddenName:"costcenter",
            id:"costcenter"+this.heplmodeid+this.id,
            store: Wtf.FormCostCenterStore,
            valueField:'id',
            displayField:'name',
            extraComparisionField:'ccid', 
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['ccid']:[],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
            isProductCombo:true,
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
//            anchor:"50%",
            width : 240,
            triggerAction:'all',
            addNewFn:this.addCostCenter,
            scope:this,
            hidden: this.quotation,
            hideLabel: this.quotation
        });

        
        var itemArr=[];
        if(this.isTemplate){
            itemArr.push(this.moduleTemplateName,this.createAsTransactionChk);
        }
            itemArr.push(this.templateModelCombo, this.Name, this.Currency,{
                layout:'column',
                border:false,
                defaults:{border:false},items:[ {
                    layout:'form',
                    ctCls : (this.cash||this.isOrder)?"hidden-from-item":"",
                    width:215,
                    items:this.fromPO
                },{
                    width:210,
                    layout:'form',
                    labelWidth:50,
                    items:this.fromLinkCombo
               }]},this.PO,this.sequenceFormatCombobox,this.Number,this.billDate,
            this.PORefNo, this.CostCenter,this.youtReftxt,this.delytermtxt,this.invoiceTotxt);
          this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId:"northForm"+this.id,
            autoHeight: true,
            parentcompId:this.id,
            moduleid: this.moduleid,
            isEdit: this.isEdit,
            record: this.record,
            isViewMode:this.isTemplate || this.isViewTemplate
        });
       var ht=(this.isOrder?(Wtf.isIE?260:360):(Wtf.isIE?360:300));
       if(this.isCustBill)ht+=25;
       if(!(this.quotation || !this.isCustomer || this.isOrder)) {
           ht+=160;
       }
       
       if(this.isTemplate){
           ht+=(this.isOrder?100:10);
       }
       
       if(this.createTransactionAlso){
           ht+=50;
       }
       //if(!this.isCustBill && !this.isOrder && !this.cash && this.isCustomer)ht+=25;
       
       /*if(this.moduleid=='2' || this.moduleid=='3')
           ht+=90;
       */
       
       // For link the Vendor Invoice Number (landed invoice)
       this.InvoiceRec = Wtf.data.Record.create ([
           {name:'billid'},
           {name:'billno'}
       ]);
       this.InvoiceStoreUrl = "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
       this.InvoiceStore = new Wtf.data.Store({
           url:this.InvoiceStoreUrl,
           baseParams: {
               cashonly:false,
               consolidateFlag:false,
               creditonly:true,
               isOutstanding:false,
               isfavourite:false,
               ispendingpayment:false,
               nondeleted:true,
               excludeInvoiceId : (this.isEdit && !this.copyInv)? this.record.data.billno : "",
               report:true,
               companyids:companyids,
               gcurrencyid:gcurrencyid,
               userid:loginid
           },
           reader: new Wtf.data.KwlJsonReader({
               root: "data",
               totalProperty:'count'
           },this.InvoiceRec)
       });
       if((this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId ) && !this.cash) {           
           this.InvoiceStore.load();           
       }
       this.invoiceList = new Wtf.form.ComboBox({
           fieldLabel:WtfGlobal.getLocaleText("acc.invoice.consignmentNumber"), // "Consignment Number"
           id: "consignmentnumber" + this.heplmodeid + this.id,
           store: this.InvoiceStore,
           displayField:'billno',
           valueField:'billid',
           emptyText:WtfGlobal.getLocaleText("acc.invoice.consignmentNumberEmptyText"), // 'Select Consignment Number',
           mode: 'local',
           width: 240,
           name:'landedInvoiceNumber',
           hiddenName:'landedInvoiceNumber',
           hidden: true,//!(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash),
           hideLabel: true,//!(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash),
           triggerAction: 'all',
           forceSelection: true,
           selectOnFocus:true
       });
       var ArrItemsMain=[];
       ArrItemsMain.push({
                    layout:'column',
                    border:false,
                    defaults:{border:false},
                    items:[{
                        layout:'form',
                        columnWidth:0.55,
                        border:false,
                        items:itemArr
                    },{
                        layout:'form',
                        columnWidth:0.45,
                        border:false,
                        items:[this.shipDate,this.Term,this.DueDate,{
                            itemCls : "",
                            layout:'column',
                            border:false,
                            defaults:{border:false},
                            items:[{
                                layout:'form',
                                columnWidth:0.55,
                                items:this.Discount
                            },{
                                columnWidth:0.45,
                                layout:'form',
                                items:this.perDiscount
                           }]
                        },this.Memo,this.shipvia, this.fob, this.includingGST, this.includeProTax,this.applyTaxToTermsChk, this.validTillDate, this.template,this.templateID,this.users,this.invoiceList,this.generateReceipt,this.capitalGoodsAcquired,this.autoGenerateDO,this.sequenceFormatComboboxDo,this.no,this.delydatetxt,this.projecttxt,this.depttxt,this.requestortxt,this.mernotxt]
                    }]
            });
       // append CUSTOMFIELDS in form     
       if((this.moduleid == Wtf.Acc_Lease_Order && this.isLeaseFixedAsset) || this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId ||this.moduleid == Wtf.LEASE_INVOICE_MODULEID
               || this.moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId
               || this.moduleid==Wtf.Acc_Lease_Quotation){
            ArrItemsMain.push(this.tagsFieldset);
       }
       this.NorthForm=new Wtf.form.FormPanel({
            region:'north',
            autoHeight:true,
            id:"northForm"+this.id,
            border:false,
            disabledClass:"newtripcmbss",
            //disabled:this.isViewTemplate,
            items:[{
                layout:'form',
                baseCls:'northFormFormat',
                labelWidth:155,
                cls:"visibleDisabled",
                items:ArrItemsMain
            }]
        });
        this.NorthForm.on('render',function(){
            this.termds.load({
                params: {               
                    cash_Invoice:this.cash
                }
            });
            if(this.isViewTemplate){
                this.setdisabledbutton();
            }
        this.termds.on("load",function(){
            if(this.autoGenerateDO.getValue() ){
                this.showDO();
            }else{
                this.hideDO();
                WtfGlobal.hideFormElement(this.autoGenerateDO);
            }  
            if((this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId )&&this.isFixedAsset ){
                WtfGlobal.hideFormElement(this.invoiceList);
            }
        },this);


        },this);
        this.productDetailsTplSummary=new Wtf.XTemplate(
            '<div style="padding: 5px; border: 1px solid rgb(153, 187, 232);">',            
            '<div><hr class="templineview"></div>',
            '<div>',
            '<table width="100%">'+
            '<tr>'+
            '<td style="width:25%;"><b>Product Name:</b></td><td style="width:55%;"><span wtf:qtip="{productname}">'+Wtf.util.Format.ellipsis('{productname}',60)+'</span></td>'+                   
            '</tr>'+
            '<tr>'+
            '<td><b>In Stock: </b></td><td style="width:10%;">{qty}</td>'+
            "<td><b>Open PO: </b></td><td style='width:10%;'><a href='#' onclick='Showproductdetails(\"{productid}\",\"{productname}\",false)'>{poqty}</a></td>"+  
            "<td><b>Open SO: </b></td><td style='width:30%;'><a href='#' onclick='Showproductdetails(\"{productid}\",\"{productname}\",true)'>{soqty}</a></td>"+         //provided link on wich we will get product quantity details
            '</tr>'+
            '</table>'+
            '</div>',            
            '<div><hr class="templineview"></div>',                        
            '</div>'
        );
        var blockSpotRateLink_first = "";
        var blockSpotRateLink_second = "";
        if(!Wtf.account.companyAccountPref.activateToBlockSpotRate){ // If activateToBlockSpotRate is set then block the Spot Rate Links
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
            //id:'productDetailsTpl',
            border:false,
            baseCls:'tempbackgroundview',
            width:'95%',
            hidden:true,//(this.isCustBill)?true:false,
            html:this.productDetailsTplSummary.apply({productname:"&nbsp;&nbsp;&nbsp;&nbsp;",productid:0,qty:0,soqty:0,poqty:0})
        });    
        this.southCenterTpl=new Wtf.Panel({
            border:false,
            html:this.southCenterTplSummary.apply({basecurrency:WtfGlobal.getCurrencyName(),exchangerate:'x',foreigncurrency:"Foreign Currency", editable:false})
        });
        this.southCalTemp=new Wtf.Panel({  
            border:false,
            baseCls:'tempbackgroundview',
            html:this.tplSummary.apply({subtotal:WtfGlobal.currencyRenderer(0),discount:WtfGlobal.currencyRenderer(0),totalamount:WtfGlobal.currencyRenderer(0),tax:WtfGlobal.currencyRenderer(0),aftertaxamt:WtfGlobal.currencyRenderer(0),totalAmtInBase:WtfGlobal.currencyRenderer(0),termtotal:WtfGlobal.currencyRenderer(0),amountbeforetax:WtfGlobal.currencyRenderer(0),amountdue:WtfGlobal.currencyRenderer(0)})
        });
        this.helpMessage= new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.dashboard.help"),  //'Help',
            handler:this.helpmessage,
            scope:this,
            hidden:this.readOnly,
            tooltip: WtfGlobal.getLocaleText("acc.common.click"),  //'Click for help',
            iconCls: 'help'
        });
        this.addGrid();
        this.isTaxable= new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            valueField:'value',
            displayField:'name',
            id:"includetax"+this.heplmodeid+this.id,
            store:this.fromPOStore,
            listWidth:50,
            fieldLabel:WtfGlobal.getLocaleText("acc.inv.totax"),  //"Include Total Tax",
            allowBlank:this.isOrder,
            value:false,
            width:50,
            typeAhead: true,
            forceSelection: true,
            hideLabel:(Wtf.account.companyAccountPref.countryid == '137' && (!Wtf.account.companyAccountPref.enableGST || !this.shouldTaxBeHidden()))|| Wtf.account.companyAccountPref.isLineLevelTermFlag==1,// hide if company is malaysian and GST is not enabled for it
            hidden:(Wtf.account.companyAccountPref.countryid == '137' && (!Wtf.account.companyAccountPref.enableGST || !this.shouldTaxBeHidden())) || Wtf.account.companyAccountPref.isLineLevelTermFlag==1,// hide if company is malaysian and GST is not enabled for it
            name:'includetax',
            hiddenName:'includetax',
            listeners:{
                'select':{
                    fn:this.enabletax,
                    scope:this
                }
            }
        });
        this.Tax= new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.invoice.Tax"),  //'Tax',
            id:"tax"+this.heplmodeid+this.id,
            disabled:!this.isEdit,
            hiddenName:'tax',
            anchor: '97%',
            store:this.Grid.taxStore,
            valueField:'prtaxid',
            forceSelection: true,
            displayField:'prtaxname',
            hideLabel:(Wtf.account.companyAccountPref.countryid == '137' && (!Wtf.account.companyAccountPref.enableGST || !this.shouldTaxBeHidden())) || Wtf.account.companyAccountPref.isLineLevelTermFlag==1,// hide if company is malaysian and GST is not enabled for it
            hidden:(Wtf.account.companyAccountPref.countryid == '137' && (!Wtf.account.companyAccountPref.enableGST || !this.shouldTaxBeHidden())) || Wtf.account.companyAccountPref.isLineLevelTermFlag==1,// hide if company is malaysian and GST is not enabled for it
//            addNewFn:this.addTax.createDelegate(this),
            typeAhead: true,
            mode: 'remote',
            minChars:0,
            addNoneRecord: false,         //For 'None' option in Tax Combo.
            scope:this,
            extraFields: [],
            isTax: true,
            listeners:{
                'select':{
                    fn:this.updateSubtotal,
                    scope:this
                },
                'beforeselect': {
                    fn: function (combo, record, index) {
                        return validateSelection(combo, record, index);
                    },
                    scope: this
                }
            },
            selectOnFocus:true
        });
        
        /*-----If checked CGA then you can not change tax.It will be set as per company preferences selected tax.-----*/
        this.Tax.on('change', function (o, newval, oldval) {
            this.capitalGoodsAcquiredHandler();
        }, this);
        this.isTaxable.on('change', function (o, newval, oldval) {
            this.capitalGoodsAcquiredHandler();
        }, this);
        
        var prodDetailSouthItems = [this.productDetailsTpl,this.southCenterTpl];
        if(this.IsInvoiceTerm) {
            this.termgrid = CommonERPComponent.addInvoiceTermGrid(this);
            this.termgrid.on('render', function(){
            this.termgrid.getView().getRowClass = WtfGlobal.getRowClass.createDelegate(this, [this.termgrid], 1);
                }, this);
            prodDetailSouthItems.push(this.termgrid);
        }
        this.LineLevelTermTplSummary = new Wtf.XTemplate(
                '<div> &nbsp;</div>',
                '<div style="padding: 5px; border: 1px solid rgb(153, 187, 232);">',
                '<div><hr class="templineview"></div>',
                '<div class ="currency-view">',
                '<table width="95%">',
                '<tpl for="lineLevelArray">',
                '<tr><td><b>{name} Amount </b></td><td text-align=right>{taxAmount}</td></tr>',
                '</tpl>',
                '</table>',
                '<div><hr class="templineview"></div>',
                '<table width="95%">',
                '<tr><td><b>' + WtfGlobal.getLocaleText("acc.invoice.TotalTaxAmt") + ' </b></td><td text-align=right>{TotalTaxAmt}</td></tr>',
                '</table>',
                '<div><hr class="templineview"></div>',
                '</div>'
                );
        var lineLevelArray = [];
        if (WtfGlobal.GSTApplicableForCompany() == Wtf.GSTStatus.OLDNEW) {
            for (var i = 0; i < Wtf.LineTermsMasterStore.getRange().length; i++) {
                var temp = Wtf.LineTermsMasterStore.getRange()[i].data;
                temp['taxAmount'] = WtfGlobal.currencyRenderer(0);
                lineLevelArray.push(temp);
            }
        }
        this.LineLevelTermTpl = new Wtf.Panel({
            border: false,
            width: '95%',
            hidden: !Wtf.account.companyAccountPref.isLineLevelTermFlag,
            baseCls: 'tempbackgroundview',
            html: this.LineLevelTermTplSummary.apply({
                lineLevelArray: lineLevelArray,
                TotalTaxAmt: WtfGlobal.currencyRenderer(0)
            })
        });
        prodDetailSouthItems.push(this.LineLevelTermTpl);
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.tax, Wtf.Perm.tax.view))
            this.Tax.addNewFn=this.addTax.createDelegate(this);

        this.southPanel=new Wtf.Panel({
            region:'center',
            border:false,
            disabledClass:"newtripcmbss",
//            disabled:this.isViewTemplate,
            style:'padding:0px 10px 10px 10px',
            layout:'column',//layout:'border',//Bug Fixed: 14871[SK] Scrolling issue : changed layout from border to column
            height:(Wtf.isIE?210:150) + (prodDetailSouthItems.length>2 ? 100 : 50),
            items:[{
                columnWidth: .45,// width: 570,//region:'center',
                border:false,
                items:prodDetailSouthItems
            },{
//                region:'east',
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
            }]
        });
        
        var lastTransPanelId = "";
        if(this.quotation) {
            lastTransPanelId = "quotation";
        } else if(this.isOrder) {
            lastTransPanelId = this.isCustomer ? "salesorder" : "purchaseorder";
        } else if(this.cash){
            lastTransPanelId = this.isCustomer ? "cashsales": "cashpurchase";
        } else {
            lastTransPanelId = this.isCustomer ? "CInvoiceList": "VInvoiceList";
        }

        this.exciseFormPanel=new Wtf.form.FormPanel({
            region:'north',
            autoHeight:true,
//            id:"exciseFormPanel1"+this.id,
            hidden: (Wtf.account.companyAccountPref.countryid!= Wtf.Country.INDIA && !Wtf.isExciseApplicable) ,
            border:false,
//            disabled:this.isViewTemplate,
            bodyStyle: {
                background:"#DFE8F6"
            },
            items:[{
                layout:'form',
                baseCls:'northFormFormat',
                defaults:{
                    labelWidth:180
                },
                cls:"visibleDisabled",
                items:[           
                this.exciseFieldsetSupplier=new Wtf.form.FieldSet({
                    xtype: 'fieldset',
                    title: this.isCustomer?WtfGlobal.getLocaleText("acc.field.buyer.details"):WtfGlobal.getLocaleText("acc.field.supplier.buyer.details"),//'Supplier/Dealer Details',
                    checkboxToggle: true,
                    collapsed: !Wtf.isExciseApplicable,
                    checkboxName: 'isExciseFieldsetSupplier',
                    height:'165',
                    hidden: (Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA), 
                    items:[
                    {
                        layout:'column',
                        border:false,
                        defaults:{
                            border:false
                        },
                        items:[{
                            layout:'form',
                            columnWidth:0.50,
                            border:false,
                            items:[
                            this.Supplier= new Wtf.form.TextField({
                                fieldLabel:this.isCustomer?WtfGlobal.getLocaleText("acc.agedPay.cus"):WtfGlobal.getLocaleText("acc.field.supplier"),// "Supplier",
                                name:"suppliers",
                                width:240,
//                                disabled:this.isViewTemplate,
//                                emptyText:'Supplier Name',
                                maxLength:200          
                            }),
                            this.supplierTINSalesTAXNo= new Wtf.form.TextField({
                                fieldLabel: WtfGlobal.getLocaleText("acc.field.tinsalsetaxno"),//"TIN/Sales Tax No.",
                                name:"supplierTINSalesTAXNo",
                                width:240,
//                                disabled:this.isViewTemplate,
                                vtype : "alphanum",
                                invalidText :'Alphabets and numbers only',
//                                emptyText:"TIN/Sales Tax Number",
                                maxLength:11            
                            }),
                            this.supplierExciseRegnNo= new Wtf.form.TextField({
                                fieldLabel: WtfGlobal.getLocaleText("acc.field.exciseregnno"),//"Excise Regn No.",
                                name:"supplierExciseRegnNo",
//                                disabled:this.isViewTemplate,
                                width:240,
                                vtype : "alphanum",
                                invalidText :'Alphabets and numbers only',
//                                emptyText:"Excise Registration Number",
                                maxLength:15            
                            }),
                            this.CSTNumber= new Wtf.form.TextField({
                                fieldLabel: WtfGlobal.getLocaleText("acc.field.cstnumber"),//"CST Number",
                                name:"cstnumber",
//                                disabled:this.isViewTemplate,
                                width:240,
                                vtype : "alphanum",
                                invalidText :'Alphabets and numbers only',
//                                emptyText:"Excise Registration Number",
                                maxLength:11            
                            }),
                            this.supplierRange= new Wtf.form.TextField({
                                fieldLabel: WtfGlobal.getLocaleText("acc.field.Range"),//"Range",
                                name:"supplierRange",
//                                disabled:this.isViewTemplate,
                                width:240,
//                                emptyText:"Range",
                                maxLength:200            
                            }),
                            this.Commissionerate= new Wtf.form.TextField({
                                fieldLabel:WtfGlobal.getLocaleText("acc.field.commissionerate"),// "Commissionerate",
                                name:"supplierCommissionerate",
                                width:240,          
//                                disabled:this.isViewTemplate,
//                                emptyText:"Commissionerate",
                                maxLength:200            
                            })
                            ]
                        },{
                            layout:'form',
                            columnWidth:0.50,
                            border:false,
                            items:[
                            this.SupplierAddress= new Wtf.form.TextArea({
                                fieldLabel: WtfGlobal.getLocaleText("acc.cust.add"),//"Address",
                                name:"supplierAddress",
                                width:240, 
//                                disabled:this.isViewTemplate,
                                maxLength:1000
                                
//                                emptyText:"Address"
                            }),
                            this.state = new Wtf.form.TextField({
                                width:240,
                                name:'supplierState',
                                labelWidth:80,
                                disabled:this.isViewTemplate,
                                fieldLabel:"State"
                            }),
//                            this.state = new Wtf.form.ComboBox({
//                                store: Wtf.stateStore,
//                                width:240,
//                                name:'supplierState',
//                                listWidth:240,
//                                labelWidth:80,
//                                disabled:this.isViewTemplate,
//                                fieldLabel:"State",
//                                displayField:'name',
//                                valueField:'id',
//                                triggerAction: 'all',
//                                mode: 'local',
//                                typeAhead:true,
//                                emptyText: WtfGlobal.getLocaleText("acc.rem.254"),
//                                selectOnFocus:true,
//                                forceSelection: true
//                            }),
                            this.supplierImporterExporterCode= new Wtf.form.TextField({
                                fieldLabel: WtfGlobal.getLocaleText("acc.field.importexportcode"),//"Importer Exporter Code",
                                name:"supplierImporterExporterCode",
//                                emptyText: "Importer Exporter Code",
                                width:240,           
//                                disabled:this.isViewTemplate,
//                                vtype : "nonDecimalNumber",
//                                maskRe: /[0-9.]/,  
                                invalidText :'Numbers only',
                                maxLength:10,
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
                            this.supplierDivision= new Wtf.form.TextField({
                                fieldLabel: WtfGlobal.getLocaleText("acc.field.division"),//"Division",
                                name:"supplierDivision",
//                                emptyText: "Division",
                                width:240,
//                                disabled:this.isViewTemplate,
                                maxLength:200            
                            })
                            ]
                        }]
                    }
                    ]                   
                }),
                
                
                this.exciseFieldsetManufacture=new Wtf.form.FieldSet({
                    xtype: 'fieldset',
                    title: WtfGlobal.getLocaleText("acc.field.consignee.details"),//'Manufacturer/Importer/Consigner Details',
                    checkboxToggle: true,
                    collapsed: !Wtf.isExciseApplicable,
                    checkboxName: 'isExciseFieldsetManufacture',
                    height:'165',
                    hidden: (Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA), 
                    items:[
                    {
                        layout:'column',
                        border:false,
                        defaults:{
                            border:false
                        },
                        items:[{
                            layout:'form',
                            columnWidth:0.50,
                            border:false,
                            items:[
                            this.manufactureName= new Wtf.form.TextField({
                                fieldLabel: WtfGlobal.getLocaleText("acc.field.manufacturer.name"),//"Name",
//                                emptyText: "Name",
                                name:"manufacturername",
                                width:240,           
//                                disabled:this.isViewTemplate,
                                maxLength:200        
                            }),
                            this.manufacturerExciseRegnNo= new Wtf.form.TextField({
                                fieldLabel:  WtfGlobal.getLocaleText("acc.field.exciseregnno"),//"Excise Regn No.",
//                                emptyText: "Excise Regn No.",
                                name:"manufacturerExciseRegnNo",
                                width:240,           
//                                disabled:this.isViewTemplate,
                                vtype : "alphanum",
                                invalidText :'Alphabets and numbers only',
                                maxLength:15            
                            }),
                            this.manufacturerRange= new Wtf.form.TextField({
                                fieldLabel: WtfGlobal.getLocaleText("acc.field.Range"),//"Range",
//                                emptyText: "Range",
                                name:"manufacturerRange",
                                width:240,           
//                                disabled:this.isViewTemplate,
                                maxLength:200            
                            }),
                            this.manufacturerCommissionerate= new Wtf.form.TextField({
                                fieldLabel: WtfGlobal.getLocaleText("acc.field.commissionerate"),// "Commissionerate",
//                                emptyText: "Commissionerate",
                                name:"manufacturerCommissionerate",
                                width:240,           
//                                disabled:this.isViewTemplate,
                                maxLength:200            
                            }),
                            this.manufacturerDivision= new Wtf.form.TextField({
                                fieldLabel: WtfGlobal.getLocaleText("acc.field.division"),//"Division",
//                                emptyText: "Division",
                                name:"manufacturerDivision",
                                width:240,           
//                                disabled:this.isViewTemplate,
                                maxLength:200            
                            }),
                            ]
                        },{
                            layout:'form',
                            columnWidth:0.50,
                            border:false,
                            items:[
                            this.manufacturerAddress= new Wtf.form.TextArea({
                                fieldLabel: WtfGlobal.getLocaleText("acc.cust.add"),//"Address",
                                name:"manufacturerAddress",
                                width:240,
//                                disabled:this.isViewTemplate,
                                maxLength:1000
//                                emptyText:"Address"
                            }),
                            
                            this.manufacturerExporterCode= new Wtf.form.TextField({
                                fieldLabel:  WtfGlobal.getLocaleText("acc.field.importexportcode"),//"Importer Exporter Code",
//                                emptyText: "Importer Exporter Code",
                                name:"manufacturerImporterExporterCode",
                                width:240, 
//                                disabled:this.isViewTemplate,
//                                vtype : "nonDecimalNumber",
//                                maskRe: /[0-9.]/,  
                                invalidText :'Numbers only',
                                maxLength:10 ,
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
                            this.InvoicenoManuFacture= new Wtf.form.TextField({
                                fieldLabel:WtfGlobal.getLocaleText("acc.invoice.gridInvNo"),// "Invoice No.",
                                name:"InvoicenoManuFacture",
                                width:240,   
//                                disabled:this.isViewTemplate,
                                maxLength:200            
                            }),
                            this.InvoiceDateManuFacture= new Wtf.form.DateField({
                                fieldLabel:WtfGlobal.getLocaleText("acc.agedPay.invoiceDate"),// "Invoice Date",
//                                emptyText: "Invoice Date",
                                name:"InvoiceDateManuFacture",
                                width:240,   
//                                disabled:this.isViewTemplate,
                                maxLength:200,
                                format:WtfGlobal.getOnlyDateFormat()
                            })
                            ]
                        }]
                    }
                    ]                   
                })
  
                ]
            }]
        });
        this.lastTransPanel = this.isCustomer ? getCustInvoiceTabView(false, lastTransPanelId, '', undefined, true) : getVendorInvoiceTabView(false, lastTransPanelId, '', undefined, true) ;
        
        this.NorthForm.doLayout();
        this.NorthForm.doLayout();
        this.exciseFormPanel.doLayout();
        this.southPanel.doLayout();
        this.POStore.on('load',this.updateSubtotal,this)
        this.DueDate.on('blur',this.dueDateCheck,this);
        this.billDate.on('change',this.onDateChange,this);


        this.setTransactionNumber();
        WtfComMsgBox(29,4,true);
        if (!this.custVenOptimizedFlag) {
            this.isCustomer ? chkcustaccload() : chkvenaccload();
        }
        this.ajxUrl = "CommonFunctions/getInvoiceCreationJson.do";
        var params={
            transactiondate:transdate,
            loadtaxstore:true,
            moduleid :this.moduleid,
//            loadpricestore: false,//!(this.isCustBill||this.isExpenseInv),
            loadcurrencystore:true,
            loadtermstore:true,
            includeDeactivatedTax : this.includeDeactivatedTax
//            loadInventory:this.isCustomer
        }
        Wtf.Ajax.requestEx({url:this.ajxUrl,params:params}, this, this.successCallback, this.failureCallback);
       this.currencyStore.on('load',this.changeTemplateSymbol,this);
       if(!this.isCustBill&&!this.isCustomer&&!this.isOrder&&!this.isEdit&&!this.copyInv){
           this.ProductGrid.on('pricestoreload',function(arr){//alert("1111"+arr.length)
               if(!this.isExpenseInv){
                    this.datechange=1;
                    this.changeCurrencyStore(arr);
               }
           },this);//.createDelegate(this)
       }else if(!this.isCustBill&&!this.isExpenseInv){//alert("2222"+arr.length)
           this.Grid.on('pricestoreload',function(arr){
                this.datechange=1;
                this.changeCurrencyStore(arr);
       }.createDelegate(this),this);}   
    },
    checkToIncludeGstORVatORTax: function () {  // Function used for check if Company Country is Indonesia Or India return False else true  - ERP-21052
        var fieldLabel = "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.includeGST.tooltip") + "'>" + WtfGlobal.getLocaleText("acc.cust.includingGST") + "</span>";
        if (this.isIndonesian) {
            fieldLabel = "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.includeVAT.tooltip") + "'>" + WtfGlobal.getLocaleText("acc.cust.includingVAT") + "</span>";
        }
        if (this.isIndian) {
            fieldLabel = "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.includeTAX.tooltip") + "'>" + WtfGlobal.getLocaleText("acc.cust.includingTax") + "</span>";
        }
        return fieldLabel;
    },
    shouldTaxBeHidden: function() {
        var date = this.billDate.getValue() === "" ? new Date() : new Date(this.billDate.getValue());
        var isTaxShouldBeEnable = WtfGlobal.isTaxShouldBeEnable(date.clearTime());
        return isTaxShouldBeEnable;
    },
      onBeforesalesPersonLoad: function(store, obj) {
        if (!obj.params) {
            obj.params = {};
        }
            obj.params.customerid = this.Name.getValue();
    },
     onBeforeAgentLoad: function(store, obj) {
        if (!obj.params) {
            obj.params = {};
        }
            obj.params.vendorid = this.Name.getValue();
    },
    
   setdisabledbutton:function(){
      
    this.templateModelCombo.setDisabled(true);
    this.Name.setDisabled(true);
    this.Currency.setDisabled(true);
    this.fromPO.setDisabled(true);
    this.fromLinkCombo.setDisabled(true);
    this.PO.setDisabled(true);
    this.sequenceFormatCombobox.setDisabled(true);
    this.Number.setDisabled(true);
    this.billDate.setDisabled(true);
    this.PORefNo.setDisabled(true);
    this.CostCenter.setDisabled(true);
    this.shipvia.setDisabled(true); 
    this.fob.setDisabled(true); 
    this.includeProTax.setDisabled(true); 
    this.applyTaxToTermsChk.setDisabled(true);
    this.users.setDisabled(true); 
    this.lockQuantity.setDisabled(true); 
    this.invoiceList.setDisabled(true); 
    this.generateReceipt.setDisabled(true); 
    this.capitalGoodsAcquired.setDisabled(true); 
    this.autoGenerateDO.setDisabled(true); 
    this.sequenceFormatComboboxDo.setDisabled(true); 
    this.no.setDisabled(true); 
    this.shipDate.setDisabled(true); 
    this.validTillDate.setDisabled(true);
    this.DueDate.setDisabled(true);
    this.Term.setDisabled(true);
    this.vendorInvoice.setDisabled(true);
    this.includingGST.setDisabled(true);
},
/**
 * Apply term tax check is shown when we apply product tax othewise it is hidden.
 * @param {type} show
 * @returns {undefined}
 */
 applyTaxToTermsChkHideShow : function(show){
       if(show) {
           //this.applyTaxToTermsChk.setValue(true);
            this.applyTaxToTermsChk.getEl().up('.x-form-item').setDisplayed(true);
       } else {
           this.applyTaxToTermsChk.setValue(false);
           this.applyTaxToTermsChk.getEl().up('.x-form-item').setDisplayed(false);
       }
       
   }, 
   /**
    * Apply the term tax on product tax.
    * @returns {Number}.
    */
   addTermAmountInTax: function () {
        var taxamount = 0;
//        var termAmountMapping = {};
//        if (this.applyTaxToTermsChk.getValue()==true && this.includeProTax && this.includeProTax.getValue() == true) {
//            for (var i = 0; i < this.termStore.getCount(); i++) {
//                var recdata = this.termStore.getAt(i).data;
//                if (typeof recdata.termamount == 'number' && (recdata.termamount != 0 || recdata.termamount != 0)) {
//                    termAmountMapping[recdata.id] = recdata.termamount;
//                }
//            }
//            var store = this.Grid.store;
//            var totalCnt = store.getCount();
//            var lineleveltaxtermamount = 0;
//            var alreadyCalucalatedTaxIds = '';
//            for (var cnt = 0; cnt < totalCnt; cnt++) {
//                var productRec1 = store.getAt(cnt);
//                var productRec = productRec1.data;
//                var productTaxId = productRec.prtaxid;
//                if (productTaxId) {
//                    if (Object.keys(termAmountMapping).length > 0 && alreadyCalucalatedTaxIds.indexOf(productTaxId) == -1) {
//                        alreadyCalucalatedTaxIds += productTaxId + ",";
//                        var productTaxRec = this.Grid.taxStore.getAt(this.Grid.taxStore.find('prtaxid', productTaxId));
//                        if (productTaxRec && productTaxRec.data.termid) {//termid problem
//                            var productTaxTermIds = productTaxRec.data.termid.split(',');
//                            var productTaxTermIdsAmount = 0;
//                            for (var productTaxTermIds_cnt = 0; productTaxTermIds_cnt < productTaxTermIds.length; productTaxTermIds_cnt++) {
//                                var productTaxTermId = productTaxTermIds[productTaxTermIds_cnt];
//                                if (termAmountMapping[productTaxTermId]) {
//                                    productTaxTermIdsAmount += termAmountMapping[productTaxTermId];
//                                }
//                            }
//                            var individualtaxAmount = ((productTaxTermIdsAmount) * productTaxRec.data.percent / 100);
//                            lineleveltaxtermamount += individualtaxAmount;
//                            productRec1.set('lineleveltaxtermamount',individualtaxAmount);
//                        }
//                    }
//                }
//                        if (lineleveltaxtermamount == 0) {
//                            productRec1.set('lineleveltaxtermamount', lineleveltaxtermamount);
//                        }
//            } 
//            taxamount += lineleveltaxtermamount;
//        }
        
        for (var term = 0; term < this.termStore.getCount(); term++) {
            var termRec = this.termStore.getAt(term);
            var termData = this.termStore.getAt(term).data;
            if (typeof termData.termamount == 'number') {
                var individualTermTaxAmount = 0;
                if (this.includingGST && this.includingGST.getValue()) {
                    /*
                    * Reverse calculate tax in case of Including GST
                    */
                    individualTermTaxAmount = getRoundedAmountValue((termData.termamount) * termData.linkedtaxpercentage / (100 + termData.linkedtaxpercentage));
                    termRec.set('termAmountExcludingTax',getRoundedAmountValue(termData.termamount - individualTermTaxAmount));
                    termRec.set('termAmountExcludingTaxInBase',getRoundedAmountValue((termData.termamount - individualTermTaxAmount) * this.getExchangeRate()));
                } else {
                    individualTermTaxAmount = getRoundedAmountValue((termData.termamount) * termData.linkedtaxpercentage / 100);
                    termRec.set('termAmountExcludingTax',getRoundedAmountValue(termData.termamount));
                    termRec.set('termAmountExcludingTaxInBase',getRoundedAmountValue(termData.termamount * this.getExchangeRate()));
                }
                taxamount += individualTermTaxAmount;
                termRec.set('termamountinbase',getRoundedAmountValue(termData.termamount * this.getExchangeRate()));
                termRec.set('termtaxamount',individualTermTaxAmount);
                termRec.set('termtaxamountinbase',getRoundedAmountValue(individualTermTaxAmount * this.getExchangeRate()));
            }
        }           
        return taxamount;
    },
    
    addTermAmountInTaxForMALAYSIA: function(){
        var taxamount = 0;
        for (var i = 0; i < this.termStore.getCount(); i++) {
            var termRec = this.termStore.getAt(i);
            var termData = this.termStore.getAt(i).data;
            var indexes=[];
            if (typeof termData.termamount == 'number' && (termData.termamount != 0)) {
                this.Grid.taxStore.each(function(rec){
                    var termids=rec.data.termid != undefined ? rec.data.termid.split(',') : '';
                    for (var termIds_cnt = 0; termIds_cnt < termids.length; termIds_cnt++) {
                        if(termids[termIds_cnt]==termData.id){
                            indexes.push(rec);
                        }
                    }
                });
                if(indexes.length == 1){
                    var individualTermTaxAmount = ((termData.termamount) * indexes[0].data.percent / 100);
                    taxamount += individualTermTaxAmount;
                    termRec.set('termtaxamount',individualTermTaxAmount);
                    termRec.set('termtaxamountinbase',getRoundedAmountValue(individualTermTaxAmount*this.getExchangeRate()));
                    termRec.set('termtax',indexes[0].data.prtaxid);
                }
                if(indexes.length > 1){
                    var store = this.Grid.store;
                    var lineLevelTotalCnt = store.getCount();
                    var alreadyCalucalatedTaxIds = '';
                    var isTermTaxAvailAtLineLevel = 0;
                    var index;
                    for (var termTax_cnt = 0; termTax_cnt < indexes.length; termTax_cnt++) {
                        for (var cnt = 0; cnt < lineLevelTotalCnt; cnt++) {
                            var productRec = store.getAt(cnt);
                            var productData = productRec.data;
                            var productTaxId = productData.prtaxid;
                            if (productTaxId && (indexes[termTax_cnt].data.prtaxid === productTaxId) && alreadyCalucalatedTaxIds.indexOf(productTaxId) == -1) {
                                alreadyCalucalatedTaxIds += productTaxId + "," ;
                                isTermTaxAvailAtLineLevel++;
                                index = termTax_cnt;
                            }
                        }
                    }
                    if(isTermTaxAvailAtLineLevel==1){
                        var individualTermTaxAmt = ((termData.termamount) * indexes[index].data.percent / 100);
                        taxamount += individualTermTaxAmt;
                        termRec.set('termtaxamount',individualTermTaxAmt);
                        termRec.set('termtaxamountinbase',getRoundedAmountValue(individualTermTaxAmt*this.getExchangeRate()));
                        termRec.set('termtax',indexes[index].data.prtaxid);
                    }
                }
            }
        }
        return taxamount;
    },
  
    onNameSelect:function(combo,rec,index){        
        this.singleLink = false;
        if (this.isEdit || this.isCopy) {
            this.isVenOrCustSelect = true;
        }
        this.currentAddressDetailrec="";//If customer/vendor change in this case,previously stored addresses in this.currentAddressDetailrec will be clear    
       if(combo.getValue()==combo.startValue){ //If same name selected no need to do any action 
           return;
       } 
        if (this.isGST) {
            this.addressMappingRec = rec.data.addressMappingRec;
            /**
             * ERP-32829 
             * code for New GST  i.e. populate dimension using dimension
             */
            if (rec.data.currentAddressDetailrec != undefined) {
                this.addressDetailRecForGST = rec.data.currentAddressDetailrec[0];
                if (this.isIndiaGST) {
                    this.applyGSTFieldsBasedOnDate();
                } else {
                    if (rec.data.uniqueCase != undefined) {
                        this.uniqueCase = rec.data.uniqueCase;
                        this.sezfromdate = rec.data.sezfromdate;
                        this.seztodate = rec.data.seztodate;
                        this.transactiondateforgst = this.billDate.getValue();
                    }
                }
              
                if (!Wtf.account.companyAccountPref.avalaraIntegration) {
                    var obj = {};
                obj.tagsFieldset = this.tagsFieldset;
                obj.currentAddressDetailrec = this.addressDetailRecForGST;
                obj.mappingRec = this.addressMappingRec;
                obj.isCustomer = this.isCustomer;
                obj.isShipping = this.isShipping;
                populateGSTDimensionValues(obj);
            }
        } 
        } 
       if(this.isEdit || this.copyInv){                                             
               Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),this.isCustomer?WtfGlobal.getLocaleText("acc.invoice.alertoncustomerchange"):WtfGlobal.getLocaleText("acc.invoice.alertonvendorchange"),function(btn){
               if(btn=="yes"){
                   this.doOnNameSelect(combo,rec,index); 
               } else{         
                   this.Name.setValue(combo.startValue);
                    return false;
               }
           },this);           
       } else {
           this.doOnNameSelect(combo,rec,index);
       }
//       if(Wtf.account.companyAccountPref.currid != this.Currency.getValue()) {
//                this.applyTaxToTermsChkHideShow(false);
//        }
       if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
        if(Wtf.isExciseApplicable && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA ){
            this.Supplier.setValue(rec.data.name);
            this.supplierImporterExporterCode.setValue(rec.data.iecnumber);
            this.supplierExciseRegnNo.setValue(rec.data.eccno);
            this.CSTNumber.setValue(rec.data.csttinno);
            this.supplierRange.setValue(rec.data.range);
            this.Commissionerate.setValue(rec.data.commissionerate);
            this.supplierDivision.setValue(rec.data.division);
            this.supplierTINSalesTAXNo.setValue(rec.data.vattinno);
            this.state.setValue(rec.data.billingState);
            this.SupplierAddress.setValue(rec.data.addressExciseBuyer);
        }  
       }
       
       
    },
       applyGSTFieldsBasedOnDate:function(isAddressChanged){
        if (this.Name.getValue() == undefined || this.Name.getValue() == ''){
           return;
        }
            Wtf.Ajax.requestEx({
            url: this.isCustomer?"ACCCustomerCMN/getCustomerGSTHistory.do":"ACCVendorCMN/getVendorGSTHistory.do",
            params:{
                customerid:this.Name.getValue(),
                vendorid:this.Name.getValue(),
                returnalldata:true,
                isfortransaction:true,
                transactiondate:WtfGlobal.convertToGenericDate(this.billDate.getValue())
                
            }
        }, this, function(response) {
            if (response.success) {
            /**
             * Validate GST details
             */
            isGSTDetailsPresnetOnTransactionDate(response,this, this.Grid,this.Name);
            this.ignoreHistory=true;
            this.GSTINRegistrationTypeId=response.data[0].GSTINRegistrationTypeId;
            this.gstin=response.data[0].gstin;
            this.CustomerVendorTypeId=response.data[0].CustomerVendorTypeId;
            this.uniqueCase=response.data[0].uniqueCase;
            this.transactiondateforgst=this.billDate.getValue();
            this.CustVenTypeDefaultMstrID=response.data[0].CustVenTypeDefaultMstrID;
            this.GSTINRegTypeDefaultMstrID=response.data[0].GSTINRegTypeDefaultMstrID;
            var cust_Vendparams = {};
            var record={};
            record.data=response.data[0];
            cust_Vendparams.rec = record;
            cust_Vendparams.isCustomer = this.isCustomer;
            checkAndAlertCustomerVendor_GSTDetails(cust_Vendparams);
                if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA) {
                if (!this.isCustomer) {
                    if (response.data[0].GSTINRegTypeDefaultMstrID != undefined && response.data[0].GSTINRegTypeDefaultMstrID !== "" && response.data[0].GSTINRegTypeDefaultMstrID === Wtf.GSTRegMasterDefaultID.Unregistered) {
                        this.purchaseFromURD = true;
                    } else {
                        this.purchaseFromURD = false;
                    }
                }
            }
            /**
             * On Address Changes done then need to make getGSTForProduct Request to update tax Details
             */
            if(this.keepTermDataInLinkCase==false || isAddressChanged){
                processGSTRequest(this, this.Grid);
            }
            }
        });
    }, 
    doOnNameSelect:function(combo,rec,index){ 
       var customer= this.Name.getValue();
          if (this.isCustomer) {
            Wtf.salesPersonStore.load();
        } else {
            Wtf.agentStore.load();
        }
       if(this.ispurchaseReq){
          this.Name.setValue(customer);
       }else{
          if(!this.GENERATE_PO&&!this.GENERATE_SO){
             if(this.isEdit || this.copyInv){  //edit case when user retain exchange rate setting is true
                this.loadStoreOnNameSelect();
             }else {
                this.loadStore();   
             }                                
             this.Name.setValue(customer);
           }  
       }
       this.setTerm(combo,rec,index);
        if (this.isCustomer){
           this.setSalesPerson(combo, rec, index);
        } else {
            this.setAgent(combo, rec, index);
        }    
            this.updateData();
       if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Show_all_Products){
           this.ProductGrid.productComboStore.load({params:{mappingProduct:true,customerid:this.Name.getValue(),common:'1', loadPrice:true,mode:22}})         
       }
            this.tagsFieldset.resetCustomComponents();
            var moduleid = this.isCustomer ? Wtf.Acc_Customer_ModuleId : Wtf.Acc_Vendor_ModuleId;
            this.tagsFieldset.setValuesForCustomer(moduleid, customer);
    },
    
    addSalesPerson:function(){
        this.isCustomer ? addMasterItemWindow('15') : addMasterItemWindow('20');
    },

    moduleTemplateSection:function(){
        this.moduleTemplateRecord = new Wtf.data.Record.create([
            {
                name: 'templateId'
            },
            {
                name: 'templateName'
            },
            {
                name: 'moduleRecordId'
            }
        ]);

        this.moduleTemplateStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.moduleTemplateRecord),
            url : "ACCCommon/getModuleTemplate.do",
            baseParams:{
                moduleId:this.getModuleId()
            }
        });
        
        this.moduleTemplateStore.on('load', function(store){
            if(this.isCopyFromTemplate && this.templateId!= undefined ){
                this.templateModelCombo.setValue(this.templateId);
            }     
        },this);
        
        
        this.templateModelCombo= new Wtf.form.FnComboBox({
            fieldLabel:(this.isViewTemplate?'Template Name': WtfGlobal.getLocaleText("acc.field.SelectTemplate")),
            id:"templateModelCombo"+this.heplmodeid+this.id,
            store: this.moduleTemplateStore,
            valueField:'templateId',
            displayField:'templateName',
            hideTrigger:this.isViewTemplate,
            hirarchical:true,
            emptyText:'Select a Template',
            mode: 'local',
            typeAhead: true,
            hidden:this.isTemplate || this.quotation || this.isFixedAsset || this.isLeaseFixedAsset,
            hideLabel:this.isTemplate || this.quotation || this.isFixedAsset || this.isLeaseFixedAsset,
            forceSelection: true,
            selectOnFocus:true,
            addNoneRecord: true,
            width : 240,
            triggerAction:'all',
            scope:this,
            listeners:{
                'select':{
                    fn:function(){
                        if(this.templateModelCombo.getValue() != ""){
                            this.loadingMask = new Wtf.LoadMask(document.body,{
                                msg : 'Loading...'
                            });
                            this.loadingMask.show();
                            var templateId = this.templateModelCombo.getValue();
                            var recNo = this.moduleTemplateStore.find('templateId', templateId);
                            var rec = this.moduleTemplateStore.getAt(recNo);
                            var moduleId = rec.get('moduleRecordId');
                            this.SelectedTemplateStore.load({
                                params:{
                                    billid:moduleId,
                                    isForTemplate:true
                                }
                            }); 
                      }else{
                          this.loadStore();
                      }
                    },
            scope:this            
                }
            }
        });
        
        this.moduleTemplateName = new Wtf.form.TextField({
            fieldLabel:'Template Name',
            name: 'moduletempname',
            hidden:!this.isTemplate,
            hideLabel:!this.isTemplate,
            id:"moduletempname"+this.id,
            width : 240,
            maxLength:50,
            scope:this,
            allowBlank:!this.isTemplate
        });
        
        this.createAsTransactionChk = new Wtf.form.Checkbox({
            fieldLabel : 'Create Transaction Also',
            name:'createAsTransactionChkbox',
            hidden:!this.isTemplate,
            hideLabel:!this.isTemplate,
            cls : 'custcheckbox',
            width : 10
        });
        
        this.createAsTransactionChk.on('check', function(){
            if(this.createAsTransactionChk.getValue()){
                this.createTransactionAlso = true;
                this.Number.enable();
                this.sequenceFormatCombobox.enable();
                this.billDate.enable();
                this.autoGenerateDO.enable();
                this.setTransactionNumber();
                this.billDate.setValue(Wtf.serverDate);
            }else{
                this.createTransactionAlso = false;
                this.Number.disable();
                this.sequenceFormatCombobox.disable();                
                this.billDate.disable();
                this.autoGenerateDO.setValue(false);
                this.autoGenerateDO.disable();
                this.Number.setValue('');
            }
        },this);
        
        this.SelectedTemplateRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'billto'},
        {name:'discount'},
        {name:'currencysymbol'},
        {name:'orderamount'},
        {name:'isexpenseinv'},
        {name:'currencyid'},
        {name:'shipto'},
        {name:'mode'},
        {name:'billno'},
        {name:'date', type:'date'},
        {name:'duedate', type:'date'},
        {name:'shipdate', type:'date'},
        {name:'personname'},
        {name:'personemail'},
        {name:'personid'},
        {name:'shipping'},
        {name:'othercharges'},
        {name:'partialinv',type:'boolean'},
        {name:'amount'},
        {name:'amountdue'},
        {name:'termdays'},
        {name:'termname'},
        {name:'incash'},
        {name:'taxamount'},
        {name:'taxid'},
        {name:'orderamountwithTax'},
        {name:'taxincluded',type:'boolean'},
        {name:'taxname'},
        {name:'deleted'},
        {name:'amountinbase'},
        {name:'memo'},
        {name:'externalcurrencyrate'},
        {name:'ispercentdiscount'},
        {name:'discountval'},
        {name:'crdraccid'},
        {name:'creditDays'},
        {name:'isRepeated'},
        {name:'porefno'},
        {name:'costcenterid'},
        {name:'costcenterName'},
        {name:'interval'},
        {name:'intervalType'},
        {name:'startDate', type:'date'},
        {name:'nextDate', type:'date'},
        {name:'expireDate', type:'date'},
        {name:'repeateid'},
        {name:'status'},
        {name:'archieve', type:'int'},
        {name:'withoutinventory',type:'boolean'},
        {name:'rowproductname'},
        {name:'rowquantity'},
        {name:'rowrate'},
        {name:'rowprdiscount'},
        {name:'rowprtaxpercent'},
        {name:'shipvia'},
        {name:'fob'},
        {name:'salesPerson'},
        {name:'agent'}
        
    ]);
    
    this.SelectedTemplateStoreUrl = "";
    
    if(this.isOrder && !this.quotation){
        this.SelectedTemplateStoreUrl= this.businessPerson=="Customer" ? "ACCSalesOrderCMN/getSalesOrdersMerged.do":"ACCPurchaseOrderCMN/getPurchaseOrdersMerged.do"  
    }else if(this.quotation){
        this.SelectedTemplateStoreUrl = this.isCustomer? "ACCSalesOrderCMN/getQuotations.do" : "ACCPurchaseOrderCMN/getQuotations.do";
    }else{
        this.SelectedTemplateStoreUrl= this.businessPerson=="Customer" ? "ACCInvoiceCMN/getInvoicesMerged.do" : "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
    }
    
    this.SelectedTemplateStore = new Wtf.data.Store({
            url:this.SelectedTemplateStoreUrl,
            scope:this,
           baseParams:{
                archieve:0,
                deleted:false,
                nondeleted:false,
                cashonly:(this.cash == undefined)?false:this.cash,
                creditonly:false,
                consolidateFlag:false,
                companyids:companyids,
                enddate:'',
                gcurrencyid:gcurrencyid,
                userid:loginid,
                isfavourite:false,
                startdate:''
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.SelectedTemplateRec)
        });
        
        this.SelectedTemplateStore.on('load', this.fillData,this);
        this.SelectedTemplateStore.on('loadexception', function(){
            this.loadingMask.hide();
        },this);
        this.moduleTemplateStore.load();
        
    },
    
    onDateChange:function(a,val,oldval){
        if(val != oldval){
            this.isbilldateChanged = true;
            /*
            *	ERM-1037
            *	On date change send id of linked document to function to restrict linking of future dated document
            */
            var selectedBillIds = this.PO.getValue().toString();
            if (selectedBillIds != "") {
                var selectedValuesArr = selectedBillIds.split(',');
                WtfGlobal.checkForFutureDate(this, selectedValuesArr)
            }
        }
        if (this.isIndiaGST) {
            this.transactiondateforgst = this.billDate.getValue();
            if ((this.isEdit && !this.copyInv && !this.checkgststatus)) {
                /**
                 * Show pop up in edit case
                 */
                checkGSTDataOnDateCase(this, this.Grid, oldval);
            } else if (this.PO.getValue() != undefined && this.PO.getValue() != "" && !this.checkgststatus) {
                /**
                 * Get link document date in Show pop up
                 */
                getLinkDateTocheckGSTDataOnDateCase(this, this.Grid);
            } else {
                /**
                 * calulate GST based on date
                 */
                this.applyGSTFieldsBasedOnDate();
            }
            this.checkgststatus = false;
        }
        /*
         *On date change we are setting Purchase Date and Installation Date.
         *Only for without link cases.
         */
        if(!(this.isGRLinkedInPI || this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId) && (this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId)){
            for(var i=0;i<this.Grid.getStore().getCount()-1;i++){
                var rec =  this.Grid.getStore().getAt(i);
                var assetDetails =rec.get('assetDetails');
            
                if(assetDetails != "" && assetDetails != undefined){
                    var assetDetailArray = eval('(' + assetDetails + ')');
                    for(var j=0;j<assetDetailArray.length;j++){
                        if( assetDetailArray[j].purchaseDate != ""){
                            assetDetailArray[j].purchaseDate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
                        }
                        if( assetDetailArray[j].installationDate != ""){
                            if(Wtf.account.companyAccountPref.depreciationCalculationType != 0){
                                assetDetailArray[j].installationDate = WtfGlobal.convertToGenericDate(this.billDate.getValue());
                            }
                        }
                    }
                    rec.set('assetDetails', JSON.stringify(assetDetailArray));
                    rec.commit();
                }
            }
        }
        this.transactiondateforgst=this.billDate.getValue();
        if(this.Currency.getValue()==WtfGlobal.getCurrencyID()){ //when tranaction in base currency for all cases (edit,copy, create new)
            this.doOnDateChanged(val,oldval);
        } else if((this.isEdit && !this.copyInv) && Wtf.account.companyAccountPref.retainExchangeRate){ //edit case: when user want to retain exchange rate        
            return;                                       
        } else if(this.isEdit || this.copyInv) { //1.Edit case when user do not want to retain exchange rate 2.copy case
             Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.invoice.exchangeRateMsg"),function(btn){
                if(btn=="yes"){
                    this.doOnDateChanged(val,oldval); 
                } else{
                    this.billDate.setValue(oldval);
                    return;
                }
             },this);
        } else { //Normal Create New Case           
            this.doOnDateChanged(val,oldval);        
        }        
        
        if (Wtf.account.companyAccountPref.countryid === Wtf.CountryID.MALAYSIA) { // for malaysian company
            var isTaxShouldBeEnable = WtfGlobal.isTaxShouldBeEnable(new Date(this.billDate.getValue()).clearTime());
            if(this.importService && this.importService.getValue()){
                isTaxShouldBeEnable = false;
            }
            if (!isTaxShouldBeEnable) { // check if tax should be disable or not
                this.Grid.getStore().removeAll();
                this.Grid.addBlankRow();
                this.includingGST.setValue(false);
                this.includingGST.disable();
                this.isTaxable.setValue(false);
                this.isTaxable.disable();
                this.Tax.setValue("");
                this.Tax.disable();
                this.includeProTax.setValue(false);
                this.includeProTax.disable();
                var taxColumnIndex = this.Grid.getColumnModel().findColumnIndex("prtaxid");
                var taxAmtColumnIndex = this.Grid.getColumnModel().findColumnIndex("taxamount");
                this.Grid.getColumnModel().setHidden(taxColumnIndex, true);
                this.Grid.getColumnModel().setHidden(taxAmtColumnIndex, true);
            } else {
                this.includingGST.enable();
                this.isTaxable.enable();
                this.includeProTax.enable();
            }
        }
   },
   
   doOnDateChanged:function(val,oldval){
        this.val=val;
        this.oldval=oldval;
//        this.loadTax(val);
        this.externalcurrencyrate=0;
        this.custdatechange=true;
        this.datechange=1;
        this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.billDate.getValue())}});
        if(!(this.isCustBill||this.isExpenseInv)) {
            var affecteduser = this.Name.getValue();
//            this.Grid.loadPriceStoreOnly(val,this.Grid.priceStore, affecteduser);
//            this.Grid.setGridProductValues(true, false);
        }
            
        else{
            this.changeCurrencyStore();
            this.updateSubtotal();
            this.applyCurrencySymbol();
            
            var subtotal=0.00;
            var tax=0.00;
            var taxAndSubtotal=this.Grid.calLineLevelTax();
            if(this.includeProTax.getValue()){
                subtotal=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[0]-taxAndSubtotal[1],this.symbol)
                tax = taxAndSubtotal[1];
                if(this.includeProTax.getValue() && this.applyTaxToTermsChk.getValue()){
                    tax += this.addTermAmountInTax();
                }
                tax=WtfGlobal.addCurrencySymbolOnly(tax,this.symbol);
            }else{
                subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol)
                tax=WtfGlobal.addCurrencySymbolOnly(this.caltax()+this.addTermAmountInTax(),this.symbol);
            }
            
//            var subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol)
            var discount=WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol)
            var totalamount=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol)
//            var tax=WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol);
            var aftertaxamt=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax()+this.addTermAmountInTax()+this.findTermsTotal(),this.symbol);
            var totalAmtInBase=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol());
            this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:subtotal,discount:discount,totalamount:totalamount,tax:tax,aftertaxamt:aftertaxamt,totalAmtInBase:totalAmtInBase,amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())});
        }
   },
    loadTax:function(val){
        this.Grid.taxStore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(val)}});
        this.Tax.setValue("");
        this.Grid.getStore().each(function(rec){
            rec.set('prtaxid','')
            rec.set('taxamount',0)
        },this);
    },
    successCallback:function(response){
        if(response.success){
            if(!this.isCustBill&&!this.isCustomer&&!this.isOrder&&!this.isEdit&&!this.copyInv&&this.isQuotation){
                this.ProductGrid.taxStore.loadData(response.taxdata);
                this.ExpenseGrid.taxStore.loadData(response.taxdata);
            }
            else
                this.Grid.taxStore.loadData(response.taxdata);
                this.termds.loadData(response.termdata);
                this.currencyStore.loadData(response.currencydata);
//            if(!(this.isCustBill||this.isExpenseInv)){
//                this.Grid.priceStore.loadData(response.productdata);}
             if(this.currencyStore.getCount()<1){
                callCurrencyExchangeWindow();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invoice.msg1")],2);
            }
            else{
                this.isCurrencyLoad=true;
                this.applyTemplate(this.currencyStore,0);
            }
            if(this.cash)
                this.Term.setValue(0);

            if(this.isEdit && this.record!=null) {
                    this.Tax.setValue(this.record.data.taxid);
                }
            if(!((this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId && !this.cash) || (this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId && !this.cash)) || !((this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) || (this.moduleid == Wtf.Acc_Invoice_ModuleId && !this.cash))) {
                if(this.isEdit || this.copyInv)this.getTerm();
            }
            if(this.isEdit || this.copyInv)this.loadRecord();            
            this.hideLoading();
        if(this.isExpenseInv){
            if(this.Grid.accountStore.getCount()<=1){
                this.Grid.accountStore.on("load",function(){
                    this.loadDetailsGrid();
                },this);
            }else{
                this.loadDetailsGrid();
            }
                        
        }else{
            if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Show_all_Products){
                this.Grid.productComboStore.on("load",function(){
                        if (!(this.isEdit && this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId)) {
                            this.loadDetailsGrid();
                        }
                },this);
            }
            if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag!= Wtf.Show_all_Products ){
                this.loadDetailsGrid();
            }
            var loadDetailsGrid=false;
            if(this.isCustomer){
                if(Wtf.StoreMgr.containsKey("productstoresales")){
                    loadDetailsGrid=true;
                }
            }
            else{
                if(Wtf.StoreMgr.containsKey("productstore")){
                    loadDetailsGrid=true;
                }
            }
            if(loadDetailsGrid){
                this.loadDetailsGrid();
            }
        } 
        
    }
    },
/**
 * ERP-34199 ,ERM-886 Indian GST Calculation in Lease Sub Module
 * On Edit/ Copy case set GST details
 * Populate Customer/ Vendor GST details in Edit/   Copy Case
 */    
    populateGSTDataOnEditCopy: function () {
        if (this.isGST && (this.isEdit || this.copyInv)) {
            //  var perStore = this.isCustomer ? Wtf.customerAccRemoteStore : Wtf.vendorAccRemoteStore;
            this.individualPersonDetails = new Wtf.data.Store({
                url: this.isCustomer ? "ACCCustomer/getCustomersForCombo.do" : "ACCVendor/getVendorsForCombo.do",
                baseParams: {
                    mode: 2,
                    group: this.isCustomer ? 10 : 13,
                    deleted: false,
                    nondeleted: true,
                    common: '1'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, Wtf.personRec)
            });
            this.individualPersonDetails.on('load', function (storeObj, recArr) {
                var index = this.individualPersonDetails.find('accid', this.record.data.personid);
                if (index != -1) {
                    var record = this.individualPersonDetails.getAt(index);
                    this.setGSTDetailsOnEditCase(record);
                }
            }, this);
            if (this.isCustomer) {
                this.individualPersonDetails.load({
                    params: {
                        selectedCustomerIds: this.record.data.personid
                    },
                    scope: this
                });
            } else {
                this.individualPersonDetails.load({
                    params: {
                        vendorid: this.record.data.personid
                    },
                    scope: this
                });
            }
        }
    },
    /**
     * ERP-34199,ERM-886 Indian GST in Lease module
     * On Edit/ Copy case set GST details
     * Populate Customer/ Vendor GST details in Edit/   Copy Case
     */
    setGSTDetailsOnEditCase: function (record) {
        this.addressMappingRec = record.data.addressMappingRec;

    },
    loadDetailsGrid:function(){
            if(this.isEdit && !this.isOrder &&((this.PO== undefined || this.PO.getValue()==""))){
                this.loadEditableGrid();     
            }else if(this.isEdit && this.isOrder && !this.quotation){
                this.loadEditableGridisOrder();
            }else if(this.quotation){
                this.loadEditableGridForQuotation();
            }
            if(this.isEdit && this.isOrder && !this.isCustomer && (BCHLCompanyId.indexOf(companyid) != -1)){
                this.loadOtherOrderdetails();
            }
},
    failureCallback:function(response){
         this.hideLoading();
         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Fail to load the record(s). "+response.msg], 2);
    },
    hideLoading:function(){Wtf.MessageBox.hide();},
    applyTemplate:function(store,index){
        var editable=this.Currency.getValue()!=WtfGlobal.getCurrencyID()&&this.Currency.getValue()!=""//&&!this.isOrder;
        var exchangeRate = store.getAt(index).data['exchangerate'];
        if(this.externalcurrencyrate>0) {
            exchangeRate = this.externalcurrencyrate;
        } else if(this.isEdit && this.record.data.externalcurrencyrate && !(this.custdatechange || this.currencychanged)){
            var externalCurrencyRate = this.record.data.externalcurrencyrate-0;//??[PS]
            if(externalCurrencyRate>0){
                exchangeRate = externalCurrencyRate;
            }
        }
        this.externalcurrencyrate = exchangeRate;
        var revExchangeRate = 1/(exchangeRate-0);
        if(this.exchangeratetype!=undefined&&this.exchangeratetype=="foreigntobase"&&this.revexternalcurrencyrate!=undefined&&this.revexternalcurrencyrate!=0)
            {
                revExchangeRate=this.revexternalcurrencyrate
                this.revexternalcurrencyrate=0;
            }
        revExchangeRate = (Math.round(revExchangeRate*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
        this.southCenterTplSummary.overwrite(this.southCenterTpl.body,{foreigncurrency:store.getAt(index).data['currencyname'],exchangerate:exchangeRate,basecurrency:WtfGlobal.getCurrencyName(),editable:editable,revexchangerate:revExchangeRate         
            });
    },

    changeCurrencyStore:function(pronamearr){
        this.pronamearr=pronamearr;
        var currency=this.Currency.getValue();
        if(this.val=="")this.val=this.billDate.getValue();
        //Commented following code because only one currency loaded on selection of customer 
//        if(currency!=""||this.custChange)
//            this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.val),tocurrencyid:this.Currency.getValue()}});
//        else
            this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.val)}});
    },
    changeTemplateSymbol:function(){ 

        if(this.loadCurrFlag && Wtf.account.companyAccountPref.currencyid){
            this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
            this.loadCurrFlag = false;
        }
        
     /*if date of without inventory changes. price store will not be loaded in this case.[PS]*/
        if(this.isCustBill||this.isExpenseInv){
            if(this.currencyStore.getCount()==0){
                callCurrencyExchangeWindow();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+" <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                this.billDate.setValue("");
            }
            else
                this.updateFormCurrency();
        }


     /*if date of withinventory changes. After price store load. [PS]   */
//     alert(this.datechange+"---"+this.pronamearr.length)
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
                    str=" and price of <b>"+str+"</b>";
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Please set the currency rate"+str+" for the selected date: <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                    this.billDate.setValue("");
                    //                if(this.oldval!=""||this.oldval!=undefined){
                    //                    if(!this.isCustBill)
                    //                        this.Grid.loadPriceStoreOnly(this.oldval,this.Grid.priceStore);
                    //                    this.Grid.taxStore.load({params:{transactiondate:this.oldval}});
                    //                }
            } else {
                    this.updateFormCurrency();
                    if(this.pronamearr!=undefined&&this.pronamearr.length>0){
                        str=" price of <b>"+str+"</b>";
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Please set the "+str+" for the selected date: <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);}
                    this.Grid.pronamearr=[];
                    this.updateFormCurrency();
            }
            this.datechange=0;
            this.updateSubtotal();
            this.applyCurrencySymbol();
            
            var subtotal=0.00;
            var subtotalValue=0.00;
            var tax=0.00;
            var taxAndSubtotal=this.Grid.calLineLevelTax();
            if(this.includeProTax.getValue()){
                subtotal=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[0]-taxAndSubtotal[1],this.symbol);
                tax = taxAndSubtotal[1];
                if(this.includeProTax.getValue() && this.applyTaxToTermsChk.getValue()){
                    tax += this.addTermAmountInTax();
                }
                tax=WtfGlobal.addCurrencySymbolOnly(tax,this.symbol);
                subtotalValue=taxAndSubtotal[0]-taxAndSubtotal[1];
            }else{
                subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol)
                subtotalValue=this.Grid.calSubtotal();
                tax=WtfGlobal.addCurrencySymbolOnly(this.caltax()+this.addTermAmountInTax(),this.symbol);
            }
            
//            var subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol)
            var discount=WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol)
            var totalamount=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol)
//            var tax=WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol);
            var calTermTotal = WtfGlobal.addCurrencySymbolOnly(this.findTermsTotal(),this.symbol);
            var aftertaxamt=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax()+this.addTermAmountInTax()+this.findTermsTotal(),this.symbol);
            var amountbeforetax=WtfGlobal.addCurrencySymbolOnly(subtotalValue+this.findTermsTotal(),this.symbol);
            var totalAmtInBase=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol());
            this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:subtotal,discount:discount,totalamount:totalamount,tax:tax,termtotal:calTermTotal,amountbeforetax:amountbeforetax,aftertaxamt:aftertaxamt,totalAmtInBase:totalAmtInBase,amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())});
        }
        
        if(this.currencychanged){
            if(this.currencyStore.getCount()<1){
                    callCurrencyExchangeWindow();
                    
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Please set the currency rate for the selected date: <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                     this.Currency.setValue("");   
            } else {
                this.updateFormCurrency();
            }
            this.currencychanged = false;
        }
        
    /*when customer/vendor name changes [PS]*/
        if(this.custChange){
            if(this.currencyStore.getCount()==0){
                callCurrencyExchangeWindow();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+" <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                this.Name.setValue("");
        } else{
            this.Currency.setValue(this.currencyid)
                this.updateFormCurrency();}
            this.custChange=false;
        }
        this.Grid.pronamearr=[];
    },
    updateFormCurrency:function(){
       this.applyCurrencySymbol();
       
        var subtotal=0.00;
        var subtotalValue=0.00;
        var tax=0.00;
        var taxAndSubtotal=this.Grid.calLineLevelTax();
        if(this.includeProTax.getValue() && Wtf.account.companyAccountPref.isLineLevelTermFlag==0){
            subtotal=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[0]-taxAndSubtotal[1],this.symbol);
            subtotalValue = taxAndSubtotal[0]-taxAndSubtotal[1];
            tax = taxAndSubtotal[1];
            if(this.includeProTax.getValue() && this.applyTaxToTermsChk.getValue()){
                tax += this.addTermAmountInTax();
            }
            tax=WtfGlobal.addCurrencySymbolOnly(tax,this.symbol);
        }else{
            subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol);
            subtotalValue = this.Grid.calSubtotal();
            tax=WtfGlobal.addCurrencySymbolOnly(this.caltax()+this.addTermAmountInTax(),this.symbol);
        }
       
       var calTermTotal = WtfGlobal.addCurrencySymbolOnly(this.findTermsTotal(),this.symbol);
       var amountbeforetax = WtfGlobal.addCurrencySymbolOnly((subtotalValue+this.findTermsTotal()),this.symbol);
       this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:subtotal,discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:tax,termtotal:calTermTotal,amountbeforetax:amountbeforetax,aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax()+this.addTermAmountInTax()+this.findTermsTotal(),this.symbol),totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol()),amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())});
    },

    getCurrencySymbol:function(){
        var index=null;
//        this.currencyStore.clearFilter(true); //ERP-9962
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

    applyCurrencySymbol:function(){
        var index=this.getCurrencySymbol();
        var rate=this.externalcurrencyrate;
        if(index>=0){
           rate=(rate==""?this.currencyStore.getAt(index).data.exchangerate:rate);
            this.symbol=  this.currencyStore.getAt(index).data.symbol;
            this.Grid.setCurrencyid(this.currencyid,rate,this.symbol,index);
            this.applyTemplate(this.currencyStore,index);
       }
       return this.symbol;
    },
 getPostTextEditor: function(posttext)
    {
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
    loadEditableGrid:function(){
    
    var isEditAndLinkingWithGRO = false;
    
    if(this.isEdit){
    }
    this.StoreUrl = "";
        this.subGridStoreUrl = "";
        if (this.quotation) {
         if (this.businessPerson=='Customer') {
            this.storeMode = this.isCustBill?16:12;
            this.StoreUrl = this.isCustBill?"ACCInvoiceCMN/getBillingInvoices.do":"ACCInvoiceCMN/getQuotations.do";
            this.subGridStoreUrl = this.isCustBill?"ACCInvoiceCMN/getQuotationRows.do":"ACCInvoiceCMN/getQuotationRows.do";
        } else{
            this.storeMode = this.isCustBill?16:12;
            this.StoreUrl = this.isCustBill?"ACCGoodsReceiptCMN/getBillingGoodsReceipts.do":"ACCGoodsReceiptCMN/getQuotations.do";
            this.subGridStoreUrl = this.isCustBill?"ACCGoodsReceiptCMN/getQuotationRows.do":"ACCGoodsReceiptCMN/getQuotationRows.do";
        }               
        }
        else
        {
        if (this.businessPerson=='Customer') {
            this.storeMode = this.isCustBill?16:12;
            this.StoreUrl = this.isCustBill?"ACCInvoiceCMN/getBillingInvoices.do":"ACCInvoiceCMN/getInvoices.do";
            this.subGridStoreUrl = this.isCustBill?"ACCInvoiceCMN/getBillingInvoiceRows.do":"ACCInvoiceCMN/getInvoiceRows.do";
            //this.subGridStoreUrl = this.isCustBill?"ACCInvoiceCMN/getBillingInvoiceRows.do":"ACCInvoiceCMN/getQuotationRows.do";
        } else{
            this.storeMode = this.isCustBill?16:12;
            this.StoreUrl = this.isCustBill?"ACCGoodsReceiptCMN/getBillingGoodsReceipts.do":"ACCGoodsReceiptCMN/getGoodsReceipts.do";
            this.subGridStoreUrl = this.isCustBill?"ACCGoodsReceiptCMN/getBillingGoodsReceiptRows.do":"ACCGoodsReceiptCMN/getGoodsReceiptRows.do";
        }
        }
        this.billid=this.record.data.billid;
        var mode=this.isCustBill?17:14;
        this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;
        this.Grid.getStore().load({params:{bills:this.billid,mode:mode,isexpenseinv:this.isExpenseInv,isLeaseFixedAsset:this.isLeaseFixedAsset}});
        this.EditisAutoCreateDO=false;
      if(this.isEdit && (this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId  ||this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId ) && !this.copyInv)
         {
           Wtf.Ajax.requestEx({
                url: "ACCInvoiceCMN/getDOFromInvoice.do",
                params: {
                    invoiceId: this.billid,
                    CallFromCI:this.isCustomer ? true : false//true:get DO from invoice and false : get GRO from Vendor Invoice
                }
            }, this, function(response) {
                if (response.data && response.data.length > 0) {
                  // WtfGlobal.hideFormElement(this.sequenceFormatComboboxDo);
                    this.autoGenerateDO.disable();
                    this.EditisAutoCreateDO=true;
                    this.sequenceFormatComboboxDo.setValue(response.data[0].sequenceformatDo)
                    this.no.setValue(response.data[0].SequenceNumDO);
                    this.DeliveryOrderid=response.data[0].DeliveryOrderID;
                    WtfGlobal.showFormElement(this.no);
                }
            }, function(response) {
                });
               
         }
    },

    loadEditableGridisOrder:function(){
        this.subGridStoreUrl = "";
            if (!this.isCustomer) {
            	if(!this.isCustBill){
            		if(this.isPOfromSO){                         
                            this.subGridStoreUrl ="ACCSalesOrderCMN/getSalesOrderRows.do";     
                            this.Grid.soLinkFlag = true;
                        }else{
                            this.subGridStoreUrl ="ACCPurchaseOrderCMN/getPurchaseOrderRows.do";
                        }
            	}else{
            		this.subGridStoreUrl = "ACCPurchaseOrderCMN/getBillingPurchaseOrderRows.do";
            	}
            } else{                
            	if(!this.isCustBill){
                    if(this.isSOfromPO){                         
                         this.subGridStoreUrl ="ACCPurchaseOrderCMN/getPurchaseOrderRows.do";     
                         this.Grid.soLinkFlag = true;
                    }else{
                         this.subGridStoreUrl = "ACCSalesOrderCMN/getSalesOrderRows.do";
                    }     
            	}else{
            		this.subGridStoreUrl = "ACCSalesOrderCMN/getBillingSalesOrderRows.do";
            	}
            }
            this.billid=this.record.data.billid;
            this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;
            
            this.Grid.getStore().load({params:{bills:this.billid,isLeaseFixedAsset:this.isLeaseFixedAsset,isFixedAsset:this.isFixedAsset}});
            
    },
    
    loadEditableGridForQuotation:function(){
        if (!this.isCustomer) {
            this.subGridStoreUrl = this.isVersion?"ACCPurchaseOrderCMN/getQuotationVersionRows.do":"ACCPurchaseOrderCMN/getQuotationRows.do";
        }else{
            this.subGridStoreUrl =this.isVersion ?"ACCSalesOrderCMN/getQuotationVersionRows.do": "ACCSalesOrderCMN/getQuotationRows.do";
        }
        if(!this.isCustomer && this.PR_IDS) {
            this.Grid.getStore().proxy.conn.url = "ACCPurchaseOrderCMN/getRequisitionRows.do";
            this.Grid.getStore().load({params:{bills: this.PR_IDS, linkingFlag: true, closeflag: true, isFixedAsset: this.isFixedAsset}});
            
            // reset to original config
            this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;  
            this.Grid.getStore().params = {bills:this.billid}
        } else {
            this.billid=this.record.data.billid;
            this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;  
            this.Grid.getStore().load({params:{bills:this.billid,copyInvoice:this.copyInv,isFixedAsset:this.isFixedAsset,isLeaseFixedAsset:this.isLeaseFixedAsset}});
        }
    },
    
    loadOtherOrderdetails: function() {
        Wtf.Ajax.requestEx({
            url: "ACCPurchaseOrderCMN/getPurchaseOrderOtherDetails.do",
            params: {
                poid: this.record.data.billid
            }
        }, this, function(response) {
            if (response.data && response.data.length > 0) {
                this.youtReftxt.setValue(response.data[0].poyourref);
                this.delydatetxt.setValue(response.data[0].podelydate);
                this.delytermtxt.setValue(response.data[0].podelyterm);
                this.invoiceTotxt.setValue(response.data[0].poinvoiceto);
                this.depttxt.setValue(response.data[0].podept);
                this.projecttxt.setValue(response.data[0].poproject);
                this.requestortxt.setValue(response.data[0].porequestor);
                this.mernotxt.setValue(response.data[0].pomerno);
            }
        }, function(response) {
        });

    },
    addGrid:function(){
//        if(Optimized_CompanyIds.indexOf(companyid)!= -1){
//           this.ProductGrid=new Wtf.account.ProductDetailsGridOptimized({
//                height: 200,//region:'center',//Bug Fixed: 14871[SK]
//                layout:'fit',
//                title: 'Asset Group',//WtfGlobal.getLocaleText("acc.invoice.inventory"),  //'Inventory',
//                border:true,
//                //cls:'gridFormat',
//                helpedit:this.heplmodeid,
//                moduleid: this.moduleid,
//                id:this.id+"editproductdetailsgrid",
//                viewConfig:{forceFit:false},
//                isCustomer:this.isCustomer,
//                currencyid:this.currencyid,
//                disabledClass:"newtripcmbss",
//                fromOrder:true,
//                isEdit:this.isEdit,
//                isOrder:this.isOrder,
//                isInvoice:this.isInvoice,
//                isQuotation:this.quotation,
//                isFromGrORDO:this.isFromGrORDO,
//                readOnly:this.readOnly ||this.isViewTemplate,
//                forceFit:true,
//                isCash:this.cash,
//                loadMask : true,
//                parentObj :this
//            }); 
//        }else
        {
           this.ProductGrid=new Wtf.account.FixedAssetProductDetailsGrid({
                height: 300,//region:'center',//Bug Fixed: 14871[SK]
                layout:'fit',
                title: 'Asset Group',//WtfGlobal.getLocaleText("acc.invoice.inventory"),  //'Inventory',
                border:true,
                //cls:'gridFormat',
                helpedit:this.heplmodeid,
                moduleid: this.moduleid,
                id:this.id+"editproductdetailsgrid",
                viewConfig:{forceFit:false},
                isCustomer:this.isCustomer,
                currencyid:this.currencyid,
                disabledClass:"newtripcmbss",
                isFromGrORDO:this.isFromGrORDO,
                record:this.record,
                isGRLinkedInPI:this.isGRLinkedInPI,
//                isbilldateChanged : this.isbilldateChanged,
                fromOrder:true,
                readOnly:this.readOnly ||this.isViewTemplate,
                isEdit:this.isEdit,
                isOrder:this.isOrder,
                isInvoice:this.isInvoice,
                isGST:this.isGST,
                isQuotation:this.quotation,
                forceFit:true,
                isCash:this.cash,
                loadMask : true,
                isFixedAsset:this.isFixedAsset,
                isLeaseFixedAsset:this.isLeaseFixedAsset,
                isLinkedFromReplacementNumber:false,
                isLinkedFromCustomerQuotation:false,
                parentObj :this
            }); 
        }
        // TO-DO need to remove following 3 lines after completion of ticket  ERP-6351
//        this.ProductGrid.productComboStore.on('beforeload',this.productComboStoreOnBeforeLoad,this);
//        this.ProductGrid.productComboStore.on('load',this.productComboStoreOnLoad,this);
//        this.ProductGrid.productComboStore.on('loadexception',this.productComboStoreOnLoadException,this);
        
        if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Show_all_Products){
            this.ProductGrid.productComboStore.load();
        }
        this.ProductGrid.on("productselect", this.loadTransStore, this);
        this.ProductGrid.on("productdeleted", this.removeTransStore, this);

        if(this.isCustBill){  //Without Inventory.[PS]
//            if(this.isCustomer){
//                 this.Grid=new Wtf.account.BillingProductDetailsGrid({
//                    //region:'center',//Bug Fixed: 14871[SK]
//                    height: 200,//region:'center',//Bug Fixed: 14871[SK]
//                    cls:'gridFormat',
//                    layout:'fit',
//                    moduleid: this.moduleid,
//                    viewConfig:{forceFit:false},
//                    disabledClass:"newtripcmbss",
//                    readOnly:this.readOnly ||this.isViewTemplate,
//                    isCustomer:this.isCustomer,
//                    editTransaction:this.isEdit,
//                    isCustBill:this.isCustBill,
//                    id:this.id+"billingproductdetailsgrid",
//                    currencyid:this.Currency.getValue(),
//                    fromOrder:true,
//                    isOrder:this.isOrder,
//                    isInvoice:this.isInvoice,
//                    forceFit:true,
//                    loadMask : true
//                });
//            }else{ //Add this code if ixpense invoice needed in non-inventory[PS]
//                this.ProductGrid=new Wtf.account.BillingProductDetailsGrid({
//                    height: 200,//region:'center',//Bug Fixed: 14871[SK]
//                    border:true,
//                    title: 'Inventory',
//                    viewConfig:{forceFit:true},
//                    isCustomer:this.isCustomer,
//                    editTransaction:this.isEdit,
//                    isCustBill:this.isCustBill,
//                    id:this.id+"billingproductdetailsgrid",
//                    currencyid:this.Currency.getValue(),
//                    fromOrder:true,
//                    isOrder:this.isOrder,
//                    closable: false,
//                    forceFit:true,
//                    loadMask : true
//                });
//               this.ExpenseGrid=new Wtf.account.ExpenseInvoiceGrid({
//                    height: 200,
//                    border:true,
//                    title: 'Expense',
//                    viewConfig:{forceFit:true},
//                    isCustomer:this.isCustomer,
//                    editTransaction:this.isEdit,
//                    isCustBill:this.isCustBill,
//                    id:this.id+"expensegrid",
//                    currencyid:this.Currency.getValue(),
//                    fromOrder:true,
//                    closable: false,
//                    isOrder:this.isOrder,
//                    forceFit:true,
//                    loadMask : true
//                });
//                this.GridPanel= new Wtf.TabPanel({
//                    id : this.id+'invoicegrid',
//                    iconCls:'accountingbase coa',
//                    border:false,
//                    style:'margin:10px;',
//                    cls:'invgrid',
//                    activeTab:0,
//                    height: 200,
//                    items: [this.ExpenseGrid,this.ProductGrid]
//                });
//                this.Grid = Wtf.getCmp(this.id+"expensegrid");
//                this.ProductGrid.on('datachanged',this.updateSubtotal,this);
//                this.ProductGrid.on("activate", function(){//alert("A")
//                    this.Grid = Wtf.getCmp(this.id+"billingproductdetailsgrid");
//                    this.isExpenseInv=false;
//                    this.applyCurrencySymbol();
//                    if(this.southCalTemp.body!=undefined)
//                        this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol),discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol),aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax(),this.symbol)});
//                }, this);
//                this.ExpenseGrid.on("activate", function(){//alert("B")
//                    this.Grid = Wtf.getCmp(this.id+"expensegrid");
//                    this.isExpenseInv=true
//                    this.applyCurrencySymbol();
//                    if(this.southCalTemp.body!=undefined)
//                        this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol),discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol),aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax(),this.symbol)});
//                }, this);
//            }
        }else
        {    //With Inventory[PS]
            if(this.isEdit && !this.isOrder){
                if(this.isExpenseInv){
                    this.ExpenseGrid=new Wtf.account.ExpenseInvoiceGrid({
                        height: 300,
                        //layout : 'fit',
                        border:true,
                        cls:'gridFormat',
//                        title: WtfGlobal.getLocaleText("acc.invoice.gridExpenseTab"),//'Expense',
                        viewConfig:{forceFit:false},
                        isCustomer:this.isCustomer,
                        editTransaction:this.isEdit,
                        moduleid: this.moduleid,
                        isCustBill:this.isCustBill,
                        disabledClass:"newtripcmbss",
                   
                        id:this.id+"expensegrid",
                        currencyid:this.Currency.getValue(),
                        fromOrder:true,
                        isOrder:this.isOrder,
                        readOnly:this.readOnly ||this.isViewTemplate,
                        isInvoice:this.isInvoice,
                        forceFit:true,
                        loadMask : true,
                        parentObj :this
                    });
                    this.ExpenseGrid.on('datachanged',this.updateSubtotal,this);
                    this.Grid = this.ExpenseGrid; 
                    
                }else{
//                    if(Optimized_CompanyIds.indexOf(companyid)!= -1){
//                        this.Grid=new Wtf.account.ProductDetailsGridOptimized({
//                            //region:'center',//Bug Fixed: 14871[SK]
//                            height: 200,//region:'center',//Bug Fixed: 14871[SK]
//                            cls:'gridFormat',
//                            layout:'fit',
//                            moduleid: this.moduleid,
//                            id:this.id+"productdetailsgrid",
//                            isCash:this.cash,
//                            viewConfig:{forceFit:false},
//                            autoScroll:true,
//                            editTransaction:true,
//                            disabledClass:"newtripcmbss",
//                            readOnly:this.isViewTemplate ||this.readOnly,
//                            record:this.record,
//                            copyInv:this.copyInv,
//                            fromPO:false,
//                            //readOnly:true,
//                            isEdit:this.isEdit,
//                            isFromGrORDO:this.isFromGrORDO,
//                            isOrder:this.isOrder,
//                            isInvoice:this.isInvoice,
//                            isCN:false,
//                            isCustomer:this.isCustomer,
//                            loadMask : true,
//                            parentObj :this
//                        });
//                    }else
                    {
                        this.Grid=new Wtf.account.FixedAssetProductDetailsGrid({
                            //region:'center',//Bug Fixed: 14871[SK]
                            height: 300,//region:'center',//Bug Fixed: 14871[SK]
                            cls:'gridFormat',
                            layout:'fit',
                            moduleid: this.moduleid,
                            id:this.id+"productdetailsgrid",
                            isCash:this.cash,
                            viewConfig:{forceFit:false},
                            autoScroll:true,
                            editTransaction:true,
                            disabledClass:"newtripcmbss",
//                            disabled:this.isViewTemplate,
                            isFromGrORDO:this.isFromGrORDO,
                            record:this.record,
                            copyInv:this.copyInv,
                            fromPO:false,
                            readOnly:this.isViewTemplate ||this.readOnly,
                            isEdit:this.isEdit,
                            isCN:false,
                            isCustomer:this.isCustomer,
                            isOrder:this.isOrder,
                            isInvoice:this.isInvoice,
                            isQuotation:this.quotation,
                            loadMask : true,
                            isFixedAsset:this.isFixedAsset,
                            isLeaseFixedAsset:this.isLeaseFixedAsset,
                            isLinkedFromReplacementNumber:false,
                            isLinkedFromCustomerQuotation:false,
                            isViewTemplate:this.isViewTemplate,
                            parentObj :this
                        });
                    }
                    this.Grid.on("productselect", this.loadTransStore, this);
                    this.Grid.on("productdeleted", this.removeTransStore, this);
                }
            }
            else{
                if(this.isCustomer||this.isOrder){
//                    if(Optimized_CompanyIds.indexOf(companyid)!= -1){
//                        this.Grid=new Wtf.account.ProductDetailsGridOptimized({
//                            //region:'center',//Bug Fixed: 14871[SK]
//                            height: 200,//region:'center',//Bug Fixed: 14871[SK]
//                            cls:'gridFormat',
//                            layout:'fit',
//                            parentCmpID:this.id,
//                            moduleid: this.moduleid,
//                            id:this.id+"editproductdetailsgrid",
//                            isCash:this.cash,
//                            viewConfig:{forceFit:false},
//                            record:this.record,
//                            isQuotation:this.quotation,
//                            isQuotationFromPR : this.isQuotationFromPR,
//                            isPIFromPO : this.isPIFromPO,
//                            isPIFromVQ : this.isPIFromVQ,
//                            isPOfromVQ : this.isPOfromVQ,
//                            isCustomer:this.isCustomer,
//                            currencyid:this.currencyid,
//                            disabledClass:"newtripcmbss",
//                            readOnly:this.isViewTemplate ||this.readOnly,
//                            isFromGrORDO:this.isFromGrORDO,
//                            fromPO:this.isOrder,
//                            fromOrder:true,
//                            isEdit:this.isEdit,
//                            isOrder:this.isOrder,
//                            forceFit:true,
//                            editTransaction: this.isEdit,
//                            loadMask : true,
//                            parentObj :this
//                        });
//                    }else
                    {
                        this.Grid=new Wtf.account.FixedAssetProductDetailsGrid({
                            //region:'center',//Bug Fixed: 14871[SK]
                            height: 300,//region:'center',//Bug Fixed: 14871[SK]
                            cls:'gridFormat',
                            layout:'fit',
                            parentCmpID:this.id,
                            moduleid: this.moduleid,
                            id:this.id+"editproductdetailsgrid",
                            isCash:this.cash,
                            viewConfig:{forceFit:false},
                            record:this.record,
                            isQuotation:this.quotation,
                            isQuotationFromPR : this.isQuotationFromPR,
                            isPIFromPO : this.isPIFromPO,
                            isPIFromVQ : this.isPIFromVQ,
                            isPOfromVQ : this.isPOfromVQ,
                            isCustomer:this.isCustomer,
                            currencyid:this.currencyid,
                            disabledClass:"newtripcmbss",
                            readOnly:this.isViewTemplate||this.readOnly,
                            fromPO:this.isOrder,
                            fromOrder:true,
                            isEdit:this.isEdit,
                            isFromGrORDO:this.isFromGrORDO,
                            isOrder:this.isOrder,
                            isInvoice:this.isInvoice,
                            forceFit:true,
                            editTransaction: this.isEdit,
                            loadMask : true,
                            isFixedAsset:this.isFixedAsset,
                            isLeaseFixedAsset:this.isLeaseFixedAsset,
                            isLinkedFromReplacementNumber:false,
                            isLinkedFromCustomerQuotation:false,
                            isViewTemplate:this.isViewTemplate,
                            parentObj :this
                        });
                    }
                    this.Grid.on("productselect", this.loadTransStore, this);
                    this.Grid.on("productdeleted", this.removeTransStore, this);
                  }else{
//                      this.ProductGrid=new Wtf.account.FixedAssetProductDetailsGrid({
//                        height: 200,//region:'center',//Bug Fixed: 14871[SK]
//                        layout:'fit',
//                        title: 'Inventory',
//                        border:true,
//                        id:this.id+"editproductdetailsgrid",
//                        viewConfig:{forceFit:true},
//                        isCustomer:this.isCustomer,
//                        currencyid:this.currencyid,
//                        fromOrder:true,
//                        isOrder:this.isOrder,
//                        forceFit:true,
//                        loadMask : true
//                    });
                   this.ExpenseGrid=new Wtf.account.ExpenseInvoiceGrid({
                        height: 300,
                        //layout : 'fit',
                        border:true,
                        title: WtfGlobal.getLocaleText("acc.invoice.gridExpenseTab"),//'Expense',
                        viewConfig:{forceFit:false},
                        isCustomer:this.isCustomer,
                        editTransaction:this.isEdit,
                        moduleid: this.moduleid,
                        isCustBill:this.isCustBill,
                        disabledClass:"newtripcmbss",
                        id:this.id+"expensegrid",
                        currencyid:this.Currency.getValue(),
                        fromOrder:true,
                        closable: false,
                        isOrder:this.isOrder,
                        isInvoice:this.isInvoice,
                        forceFit:true,
                        readOnly:this.readOnly ||this.isViewTemplate,
                        loadMask : true,
                        parentObj :this
                    });
                    this.GridPanel= new Wtf.TabPanel({
                        id : this.id+'invoicegrid',
                        iconCls:'accountingbase coa',
                        readOnly:this.isViewTemplate ||this.readOnly,
                        border:false,
                        style:'padding:10px;',
                        disabledClass:"newtripcmbss",
                        cls:'invgrid',
                        //cls:'gridFormat',
                        activeTab:0,
                        height: 300,
                        //region : 'center',
                        //layout : 'fit',
                        
                        items: (this.isFixedAsset || this.isLeaseFixedAsset)?[this.ProductGrid]:[this.ProductGrid,this.ExpenseGrid]
                    });
                    this.Grid = Wtf.getCmp(this.id+"editproductdetailsgrid");                    
                    this.ExpenseGrid.on('datachanged',this.updateSubtotal,this);
                    this.ProductGrid.on('datachanged',this.updateSubtotal,this);
                    if(this.symbol==undefined)this.symbol=WtfGlobal.getCurrencySymbol();
                    this.isExpenseInv=false; //work fine in case of 2 tabs
                    this.GridPanel.on('beforetabchange', this.beforeTabChange,this);
                }
            }
        }
       
        this.NorthForm.on('render',this.setDate,this);
        this.Grid.on('datachanged',this.updateSubtotal,this);
        this.Grid.on('gridconfigloaded',function(){
            this.showGridTax(null,null,!this.includeProTax.getValue()); 
        },this);
        this.Grid.getStore().on('load',function(store, recArr){
            if(!this.isOrder && !this.quotation && this.isCustomer && this.copyInv && !this.isViewTemplate){
            this.confirmMsg = "";
            if(!Wtf.account.companyAccountPref.withinvupdate){
                for(var i=0; i<recArr.length; i++){
                    if(recArr[i].data.productid !== undefined){
                        var index=this.ProductGrid.productComboStore.find('productid',recArr[i].data.productid);
                        var prorec=this.ProductGrid.productComboStore.getAt(index);
                        if(recArr[i].data['quantity'] > this.ProductGrid.productComboStore.getAt(index).data['quantity'] && prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part'){
                            this.confirmMsg += WtfGlobal.getLocaleText("acc.field.MaximumavailableQuantityforProduct")+" "+this.ProductGrid.productComboStore.getAt(index).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.is")+" "+this.ProductGrid.productComboStore.getAt(index).data['quantity']+".<br>";
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.msgbox.CONFIRMTITLE"),this.confirmMsg+WtfGlobal.getLocaleText("acc.recurringMP.doYouWantToContinue"),function(btn){
                                if(btn=="yes") {
                                
                                }else{
                                    this.ownerCt.remove(this);
                                }
                            }, this);
                            recArr[i].set('quantity', 0);
                            recArr[i].set('amount', 0);
                        }
                    }
                }
            }
        }
                    if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
                    updateTermDetails(this.Grid);
            }
            this.updateSubtotal();
            this.Grid.addBlank(store);
            if(this.isEdit){
                if(this.record.data.externalcurrencyrate!=undefined){
                    this.externalcurrencyrate=this.record.data.externalcurrencyrate;
                    this.updateFormCurrency();
                }
            }
        }.createDelegate(this),this);
        this.Grid.getStore().on('update',function(store,record,opr){
            var isExpensive = (this.isExpenseInv != null && this.isExpenseInv != undefined)?this.isExpenseInv:false;
            if(!this.isCustBill && !isExpensive){
                var index=this.Grid.productComboStore.findBy(function(rec){
                    if(rec.data.productid==record.data.productid)
                        return true;
                    else
                        return false;
                });
                var prorec=this.Grid.productComboStore.getAt(index);
                if(prorec!=undefined&&prorec!=-1&&prorec!=""){
                    this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body,{
                        productid:prorec.data['productid'],
                        productname:prorec.data['productname'],
                        qty:parseFloat(getRoundofValue(prorec.data['quantity'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"  "+prorec.data['uomname'],
                        soqty:parseFloat(getRoundofValue(prorec.data['socount'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"  "+prorec.data['uomname'],
                        poqty:parseFloat(getRoundofValue(prorec.data['pocount'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"  "+prorec.data['uomname']
                        });
                }
            }                            
        },this);
    },        
    beforeTabChange:function(a,newTab,currentTab){
    	if(currentTab!=null && newTab!=currentTab){
            
            if(this.capitalGoodsAcquired){
                this.capitalGoodsAcquired.reset();
            }
            
             Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),this.isExpenseInv?WtfGlobal.getLocaleText("acc.invoice.msg4"):WtfGlobal.getLocaleText("acc.invoice.msg5"),function(btn){ ///"Switching to "+(this.isExpenseInv?"Inventory":"Expense")+" section will empty the data filled so far in "+(this.isExpenseInv?"Expense":"Inventory")+" section. Do you wish to continue?",function(btn){
              if(btn=="yes") {                
                (this.productDetailsTpl.isVisible())?this.productDetailsTpl.setVisible(false):this.productDetailsTpl.setVisible(true);
                a.suspendEvents();
                a.activate(newTab);
                this.Discount.setValue(0);
            var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
            var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";
            if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null ){
                        this.isTaxable.setValue(true);
                        this.Tax.enable();
                        this.Tax.setValue(actualTaxId);
                }else{
                    this.isTaxable.setValue(false);
                    this.Tax.setValue("");
                    this.Tax.disable();   
                }
                a.resumeEvents();
                this.onGridChange(newTab,currentTab);
                this.showGridTax(null,null,!this.includeProTax.getValue());       // Show/hide Product tax and Tax Amount for Inventory/Expense Tab               
              }            
             }.createDelegate(this),this)
            return false;
        }
        else{
            return true;
        }
   },

    onGridChange:function(newTab){
        this.Grid.getStore().removeAll();
        this.Grid.addBlankRow();
            this.Grid = newTab;
            this.Tax.store=this.Grid.taxStore;
            this.isExpenseInv=!this.isExpenseInv; //work fine in case of 2 tabs
//            if(!this.isCustBill&&!this.isExpenseInv&&this.Grid.priceStore.getCount()==0)
//                this.Grid.priceStore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(this.billDate.getValue())}});
            this.applyCurrencySymbol();
            if(this.southCalTemp.body!=undefined){
                var subtotal=0.00;
                var tax=0.00;
                var taxAndSubtotal=this.Grid.calLineLevelTax();
                if(this.includeProTax.getValue()){
                    subtotal=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[0]-taxAndSubtotal[1],this.symbol)
                    tax = taxAndSubtotal[1];
                    if(this.includeProTax.getValue() && this.applyTaxToTermsChk.getValue()){
                        tax += this.addTermAmountInTax();
                    }
                    tax=WtfGlobal.addCurrencySymbolOnly(tax,this.symbol);
                }else{
                    subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol)
                    tax=WtfGlobal.addCurrencySymbolOnly(this.caltax()+this.addTermAmountInTax(),this.symbol);
                }
            this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:subtotal,discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:tax,aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax()+this.addTermAmountInTax()+this.findTermsTotal(),this.symbol),totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol()),amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())});
        }
    },
    caltax:function(){
//        var totalamount=this.calTotalAmount();
        var rec= this.Grid.taxStore.getAt(this.Grid.taxStore.find('prtaxid',this.Tax.getValue()));
//        var totalterm = 0;
//        var taxamount= 0;
//        if (rec != null && rec.data.prtaxid != "None") {
//            for (var i = 0; i < this.termStore.getCount(); i++) {
//                var recdata = this.termStore.getAt(i).data; //  var recdata = obj.record.data;
//                var store = this.termStore;
//                var subtotal = this.calProdSubtotalWithoutDiscount();
//                var formula = recdata.formulaids.split(",");
//                var termtotal = 0;
//                var terms = rec.data.termid;
//                terms = terms.split(",");
//                terms = "[" + terms.join(',') + "]";
//                if (terms.indexOf(recdata.id)!=-1) {
//                    for (var cnt = 0; cnt < formula.length; cnt++) {
//                        if (formula[cnt] == 'Basic') {
//                            termtotal += (subtotal);
//                        }
//                        var record = store.queryBy(function (record) {
//                            return (record.get('id') == formula[cnt]);
//                        }, this).items[0];
//                        if (record && (typeof record.data.termamount == 'number')) {
//                            //                termtotal +=(record.data.termamount*(record.data.sign==1 ? 1 : -1 ));
//                            termtotal += (record.data.termamount);
//                        }
//                    }
//                    var this_termTotal = 0;
//                    if (typeof (recdata.termpercentage) != "string" && parseInt(recdata.termpercentage) >=0) {
//                        var opmod = recdata.sign == 0 ? -1 : 1;
//                        this_termTotal = ((Math.abs(termtotal) * recdata.termpercentage * 1) / 100) * opmod;
//                    }else if (typeof (recdata.termamount) != "string") {     // for term charges 
//                        parseInt(recdata.termamount)
//                        this_termTotal = recdata.termamount;
//                    }
//                    totalterm = totalterm + this_termTotal;
//                }
//            }
//        }
        
        var taxamount=0;
        var totalamount=0;
        if(this.isTaxable != undefined && this.isTaxable.getValue()){
            totalamount=getRoundedAmountValue(this.calTotalAmount());
            if (rec != null && rec.data.prtaxid != "None"){
                taxamount = ((totalamount)*rec.data["percent"])/100;
            }
//            taxamount += this.addTermAmountInTax();
        } 
//        if(this.includeProTax.getValue() && this.applyTaxToTermsChk.getValue()){
//           taxamount += this.addTermAmountInTax();
//        }
        if (Wtf.account.companyAccountPref.isLineLevelTermFlag == 1) {   // Term Amount of line item showing as tax in  Line level terms Company.
            var taxamount = 0;
              var store = this.Grid.store;
                var totalCnt = store.getCount();
            for (var cnt = 0; cnt < totalCnt; cnt++) {
                 var lineAmt = store.getAt(cnt).data.recTermAmount;
                if (typeof lineAmt == 'number')
                      taxamount += getRoundedAmountValue(lineAmt);
               }             
          }
        return getRoundedAmountValue(taxamount);
     },
    addAccount: function(store){
        callCOAWindow(false,null,"coaWin",this.isCustomer,false,false,false,false,false,true);
        Wtf.getCmp("coaWin").on('update',function(){store.reload();},this);
    },
        addOrder:function(){
        var tabid = "ordertab";
        if(this.isCustomer){
            if(this.quotation){
                tabid = 'vendorquotation';
                callVendorQuotation(false, tabid);
            } else if(this.isOrder){
                tabid = 'quotation';
                callQuotation(false, tabid);
            } else {
//                if(this.isCustBill) {
//                    tabid = "bsalesorder";
//                    callBillingSalesOrder(false,null,tabid);
//                } else 
                {
                    if(this.fromLinkCombo.getValue() == 1) {
                        callDeliveryOrder(false,null, "deliveryorder");
                    } else {
                        tabid = "salesorder";
                        callSalesOrder(false,null,tabid);
                    }
                   
                }
            }
        }else{
//            if(this.isCustBill) {
//                tabid = "bpurchaseorder";
//                callBillingPurchaseOrder(false,null,tabid);
//            } else 
            {
                if(this.isOrder){
                    if(this.fromLinkCombo.getValue() == 0) {
                        tabid = "salesorder";
                        callSalesOrder(false,null,tabid);
                    } else if(this.fromLinkCombo.getValue() == 2){
                        tabid = 'vendorquotation';
                       callVendorQuotation(false, tabid);
                    }
                    
                } else if(this.fromLinkCombo.getValue() == 1) {
                        callGoodsReceiptDelivery(false,null, "goodsreceiptdelivery");
                    } else {
                        tabid = "purchaseorder";
                        callPurchaseOrder(false,null,tabid);
                    }
            }
        }
        if(Wtf.getCmp(tabid)!=undefined) {
            Wtf.getCmp(tabid).on('update',function(){
                this.POStore.reload();
            },this);
        }
    },
    showGridTax:function(c,rec,val){
        var hide=(val==null||undefined?!rec.data['value']:val) ;
        var id=this.Grid.getId()
        var rowtaxindex=this.Grid.getColumnModel().getIndexById(id+"prtaxid");
        var rowtaxamountindex=this.Grid.getColumnModel().getIndexById(id+"taxamount");
            this.Grid.getColumnModel().setHidden( rowtaxindex,hide) ;
            this.Grid.getColumnModel().setHidden( rowtaxamountindex,hide) ;
        var rowprDiscountIndex=this.Grid.getColumnModel().findColumnIndex("prdiscount");
        var rowDiscountIsPercentIndex=this.Grid.getColumnModel().findColumnIndex("discountispercent");
        
        var rowRateIncludingGstAmountIndex="";
        if(this.isExpenseInv){
            var rowDiscountIsAmountIndex=this.Grid.getColumnModel().findColumnIndex("discountamount");
            rowRateIncludingGstAmountIndex=this.Grid.getColumnModel().findColumnIndex("rateIncludingGstEx");
        }else{
            rowRateIncludingGstAmountIndex=this.Grid.getColumnModel().findColumnIndex("rateIncludingGst");
        }
        
        var rowRateAmountIndex=this.Grid.getColumnModel().findColumnIndex("rate");
        if(rowprDiscountIndex!=-1&&rowDiscountIsPercentIndex!=-1&&rowRateIncludingGstAmountIndex!=-1){
            if(this.includingGST.getValue()){
                this.Grid.getColumnModel().setHidden(rowRateIncludingGstAmountIndex,!this.includingGST.getValue());
                    this.Grid.getColumnModel().setEditable(this.Grid.getColumnModel().findColumnIndex("rate"), false);
                if(this.isExpenseInv){
                    this.Grid.getColumnModel().setEditable(this.Grid.getColumnModel().findColumnIndex("rate"), true);
                    this.Grid.getColumnModel().setHidden(rowDiscountIsAmountIndex,!hide);
                }else{
                    this.Grid.getColumnModel().setEditable(this.Grid.getColumnModel().findColumnIndex("rate"), false);
                }
            }else if(!this.Grid.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden){
                    this.Grid.getColumnModel().setEditable(this.Grid.getColumnModel().findColumnIndex("rate"), true);
                this.Grid.getColumnModel().setHidden(rowRateIncludingGstAmountIndex,!this.includingGST.getValue());
                if(this.isExpenseInv){
                    this.Grid.getColumnModel().setHidden(rowDiscountIsAmountIndex,hide);
                }
            }
        }
        
        if(Wtf.account.companyAccountPref.countryid== Wtf.Country.INDIA && !this.isExpenseInv ){
            this.Grid.getColumnModel().setHidden( rowtaxindex,true) ;
               this.Grid.getColumnModel().setHidden( rowtaxamountindex,true) ;
            if(this.includingGST.getValue()){
                this.Grid.getColumnModel().setHidden( rowRateAmountIndex,true) ;
                this.Grid.getColumnModel().setHidden( rowRateIncludingGstAmountIndex,false) ;
            } else {
                this.Grid.getColumnModel().setHidden( rowRateIncludingGstAmountIndex,true) ;
                this.Grid.getColumnModel().setHidden( rowRateAmountIndex,false) ;
            }
        }
        
        var isCapitalGoodsAcquired = (this.capitalGoodsAcquired && this.capitalGoodsAcquired.getValue()) ? this.capitalGoodsAcquired.getValue() :false;
        this.Grid.getStore().each(function(rec){
            if(this.includeProTax && this.includeProTax.getValue() == true
                && (rec.data.prtaxid == "" || rec.data.prtaxid == undefined || rec.data.prtaxid == "None")) {//In Edit, values are resetting after selection Product level Tax value as No
                var taxid = "";
                var taxamount = 0;
                var isLastRow = false;
                /*
                 * for excluding last empty row
                 */
                if (this.isExpenseInv) {
                    if(rec.data.accountid == "" || rec.data.accountid == undefined) {
                        isLastRow = true;
                    }
                } else {
                    if(rec.data.productid == "" || rec.data.productid == undefined) {
                        isLastRow = true;
                    }
                }
                
                if(!(isLastRow)){// for excluding last empty row
                    if (isCapitalGoodsAcquired) {
                        /**-----If Capital Goods Acquired then set Product Tax Which is Mapped in System Control.-------------*/
                        if (Wtf.account.companyAccountPref.taxCgaMalaysian != "") {
                            taxid = Wtf.account.companyAccountPref.taxCgaMalaysian;
                            rec.set('prtaxid', taxid);
                            taxamount = this.Grid.setTaxAmountAfterSelection(rec);
                            rec.set('taxamount', taxamount);
                        }
                    } else if(taxid == ""){// if tax is mapped to customer or vendor then it will come default populated
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
                rec.set('isUserModifiedTaxAmount', false);
            } else if(this.includeProTax && this.includeProTax.getValue() != true){
                rec.set('prtaxid','')
                rec.set('taxamount',0)
            }
            
            if(this.includingGST&&this.includingGST.getValue()){
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
                rec.set('rateIncludingGst',0);
            }
             
            // Except Product Combo Edit, For All Edit below code will be used for Multiple Term level Tax calculation.
            if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
                var rowAmountIndex=this.Grid.getColumnModel().findColumnIndex("amount");            
                if(rec.get('LineTermdetails') != undefined && rec.get('LineTermdetails') != ''){
                    var termStore = this.Grid.getTaxJsonOfIndia(rec);
                    if(this.includingGST && this.includingGST.getValue() == true) {
                        this.Grid.getColumnModel().setRenderer(rowAmountIndex,WtfGlobal.withoutRateCurrencySymbol);
                        termStore = this.Grid.calculateTermLevelTaxesInclusive(termStore, rec);
                    } else {
                        this.Grid.getColumnModel().setRenderer(rowAmountIndex,this.Grid.calAmountWithoutExchangeRate.createDelegate(this.Grid));
                        termStore = this.Grid.calculateTermLevelTaxes(termStore, rec);
                    }

                    rec.set('LineTermdetails',JSON.stringify(termStore));
                    this.Grid.updateTermDetails();
                }
            }
         },this);

        if (this.includeProTax && this.includeProTax.getValue() == true) {
            WtfGlobal.calculateTaxAmountUsingAdaptiveRoundingAlgo(this.Grid, false);
        }
//         if(hide)
             this.updateSubtotal();
    },
    
    
    includeProTaxHandler : function(c,rec,val){
        if(this.includeProTax.getValue() == true){
            this.isTaxable.setValue(false);
            this.isTaxable.disable();
            this.Tax.setValue("");
            this.Tax.disable();
            /*
             * if product tax is enabled global level tax should be disabled ERP-32672
             */
            this.isViewTemplate==true?this.includingGST.disable():this.includingGST.enable();
            if(this.isExpenseInv){
                this.applyTaxToTermsChkHideShow(false);
            } else{
                this.applyTaxToTermsChkHideShow(true);
            }

        }else{
            this.isTaxable.reset();
            this.isTaxable.enable();
            this.applyTaxToTermsChkHideShow(false);
            this.capitalGoodsAcquiredHandler();
        }
        this.showGridTax(c,rec,val);
    },
    
    capitalGoodsAcquiredHandler: function () {
        if (this.capitalGoodsAcquired && this.capitalGoodsAcquired.getValue()){
            if (!this.includeProTax.getValue()) {
                if (this.Tax.getValue() != Wtf.account.companyAccountPref.taxCgaMalaysian || this.Tax.getValue() == "") {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.cgamsg")], 2);
                }
                if (Wtf.account.companyAccountPref.taxCgaMalaysian != "") {
                    this.isTaxable.setValue(true);
                    this.Tax.enable();
                    this.Tax.setValue(Wtf.account.companyAccountPref.taxCgaMalaysian);
                    this.updateSubtotal();
                }
            } else if (this.includeProTax.getValue()) {
                this.Grid.getStore().each(function (rec) {
                    /**-----If Capital Goods Acquired then set Product Tax Which is Mapped in System Control.-------------*/
                    if (rec.data.productid != "" && rec.data.productid != undefined && Wtf.account.companyAccountPref.taxCgaMalaysian != "") {
                        rec.set('prtaxid', Wtf.account.companyAccountPref.taxCgaMalaysian);
                        rec.set('taxamount', this.Grid.setTaxAmountAfterSelection(rec));
                    }
                }, this);
                this.updateSubtotal();
            }
        }
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
    
     enableNumber:function(c,rec){
        this.PO.clearValue();
        this.fromLinkCombo.enable();
        this.fromPO.setValue(true);
        if(this.Grid){
            this.Grid.isFromGrORDO=false;
        }
        if(rec.data['value']==0){          // 0 for Sales Order
            this.PO.multiSelect=true;
            this.isMultiSelectFlag=true;
            this.PO.removeListener("select",this.populateData,this);  // for selection of multiple sales order 
            this.PO.addListener("blur",this.populateData,this);
            
            if(this.isLeaseFixedAsset && this.isOrder && this.isCustomer && !this.quotation){
                this.POStore.proxy.conn.url = "ACCSalesOrderCMN/getQuotations.do"
                      if(this.isSOfromPO){
//                          closeFlag = false;
                          this.POStore.proxy.conn.url = this.isCustBill?"ACCPurchaseOrderCMN/getBillingPurchaseOrders.do":"ACCPurchaseOrderCMN/getPurchaseOrders.do";
                      }
                this.POStore.load({
                    params:{
                        id:this.Name.getValue(),
                        currencyid:this.Currency.getValue(),
                        validflag:true,
                        isLeaseFixedAsset:this.isLeaseFixedAsset,
                        billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue()),
                        newcustomerid:this.Name.getValue(),
                        linkFlagInSO:true
                    }
                });
                this.PO.enable();
            }else if(this.isLeaseFixedAsset && this.isCustomer && this.quotation){
                this.POStore.proxy.conn.url = "ACCPurchaseOrderCMN/getQuotations.do"
                
                this.POStore.load({
                    params:{
                        id:this.Name.getValue(),
                        currencyid:this.Currency.getValue(),
                        validflag:true,
                        isLeaseFixedAsset:this.isLeaseFixedAsset,
                        billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue()),
                        newcustomerid:this.Name.getValue()
                    }
                });
                this.PO.enable();
            }else{
                this.fromLinkCombo.setValue(0); 
                this.autoGenerateDO.enable();
                if(this.isOrder){
                    this.POStore.proxy.conn.url = "ACCSalesOrderCMN/getSalesOrders.do";
                    this.POStore.load({params:{currencyfilterfortrans:this.Currency.getValue()}});        
                } else {
                    this.POStore.proxy.conn.url = this.isCustomer ? "ACCSalesOrderCMN/getSalesOrders.do" : "ACCPurchaseOrderCMN/getPurchaseOrders.do";
                    this.POStore.load({params:{id:this.Name.getValue(),exceptFlagINV:true,currencyfilterfortrans:this.Currency.getValue(),isFixedAsset:true}});        
                }
                //this.POStore.proxy.conn.url = this.isCustomer ? "ACCSalesOrderCMN/getSalesOrders.do" : "ACCPurchaseOrderCMN/getPurchaseOrders.do";
            //this.POStore.load({params:{id:this.Name.getValue(),exceptFlagINV:true}});            
                this.PO.enable();
                if(this.partialInvoiceCmb){
                    this.partialInvoiceCmb.disable();
                    var id=this.Grid.getId();
                    var rowindex=this.Grid.getColumnModel().getIndexById(id+"partdisc");
                    this.Grid.getColumnModel().setHidden( rowindex,true) ;
            }
            }
            //this.POStore.proxy.conn.url = this.isCustomer ? "ACCSalesOrderCMN/getSalesOrders.do" : "ACCPurchaseOrderCMN/getPurchaseOrders.do";
           //this.POStore.load({params:{id:this.Name.getValue(),exceptFlagINV:true}});            
            this.PO.enable();
            if(this.partialInvoiceCmb){
                this.partialInvoiceCmb.disable();
                var id=this.Grid.getId();
                var rowindex=this.Grid.getColumnModel().getIndexById(id+"partdisc");
                this.Grid.getColumnModel().setHidden( rowindex,true) ;
            }
            if (this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId) {
                this.Grid.isPIFromPO = true;
            }
            this.includingGST.enable();
        }
        else if(rec.data['value']==1){     // 1 for Delivery  
            if(this.isLeaseFixedAsset && this.isOrder && this.isCustomer && !this.quotation){
                this.PO.multiSelect=false;
                this.isMultiSelectFlag=false;
                this.PO.addListener("select",this.populateData,this);
                this.PO.removeListener("blur",this.populateData,this);
                
                this.POStore.proxy.conn.url = "ACCSalesOrderCMN/getReplacementRequests.do";
                
                this.POStore.load({params:{
                        id:this.Name.getValue(),
                        currencyid:this.Currency.getValue(),
                        validflag:true,
                        billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue())
                    }
                });
                this.PO.enable();
                
            }else if(this.isLeaseFixedAsset && this.isCustomer && this.quotation){
                this.PO.multiSelect=false;
                this.isMultiSelectFlag=false;
                this.PO.addListener("select",this.populateData,this);
                this.PO.removeListener("blur",this.populateData,this);
                
                this.POStore.proxy.conn.url = "ACCSalesOrderCMN/getReplacementRequests.do";
                
                this.POStore.load({params:{
                        id:this.Name.getValue(),
                        currencyid:this.Currency.getValue(),
                        validflag:true,
                        isForQuotation:this.quotation,
                        billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue())
                    }
                });
                this.PO.enable();
                
            }else{
                this.isFromGrORDO=true;
                this.Grid.isFromGrORDO=true;
                this.PO.multiSelect=true;
                this.isMultiSelectFlag=true;
                this.isGRLinkedInPI=true;
                this.PO.removeListener("select",this.populateData,this);
                this.PO.addListener("blur",this.populateData,this);                
                this.fromLinkCombo.setValue(1);
                this.autoGenerateDO.setValue(false);
                this.autoGenerateDO.disable();            
                this.POStore.proxy.conn.url = this.isCustomer ? "ACCInvoiceCMN/getDeliveryOrdersMerged.do" : "ACCGoodsReceiptCMN/getGoodsReceiptOrdersMerged.do";
                this.POStore.load({params:{currencyfilterfortrans:this.Currency.getValue(),id:this.Name.getValue(),isFixedAsset:this.isFixedAsset,isLeaseFixedAsset:this.isLeaseFixedAsset,nondeleted:true,avoidRecursiveLink:true,linkFlag:true,isDisposalINV:true}});        
                this.PO.enable(); 
                if(this.partialInvoiceCmb){
                    this.partialInvoiceCmb.disable();
                    var id=this.Grid.getId();
                    var rowindex=this.Grid.getColumnModel().getIndexById(id+"partdisc");
                    this.Grid.getColumnModel().setHidden( rowindex,true) ;
                }
            }
            this.includingGST.disable();
        } else if(rec.data['value']==2){ //2 for Quotation                        
            this.PO.multiSelect=true;
            this.isMultiSelectFlag=true;
            this.autoGenerateDO.enable();
            this.PO.removeListener("select",this.populateData,this);
            this.PO.addListener("blur",this.populateData,this);                            
            this.fromLinkCombo.setValue(2);
            if (this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId) {
               this.isPOfromVQ = true;
               this.linkFlagInPO=true;
            }
            if (this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId) {
               this.isFA_VQtoPI = true;
               this.linkFlagInGR=true;
            }
          
            this.POStore.proxy.conn.url = this.isCustomer ? "ACCSalesOrderCMN/getQuotations.do" : "ACCPurchaseOrderCMN/getQuotations.do";
            this.POStore.load({params:{id:this.Name.getValue(),currencyid:this.Currency.getValue(),isPOfromVQ:this.isPOfromVQ,validflag:true,billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue()),nondeleted:true,newvendorid:this.isCustomer?"":this.Name.getValue(),isFA_VQtoPI:this.isFA_VQtoPI,linkFlagInPO:this.linkFlagInPO,linkFlagInGR:this.linkFlagInGR}});        
            this.PO.enable();
            if (this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId) {
                this.Grid.isPOfromVQ = true;
            } else if (this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId) {
                this.Grid.isPIFromVQ = true;
            }
            this.includingGST.enable();
        } else if(rec.data['value'] == 5) { // For Linking Purchase Requisition in VQ
            this.PO.multiSelect = true;
            this.isMultiSelectFlag = true;
            this.PO.removeListener("select",this.populateData,this);
            this.PO.addListener("blur",this.populateData,this);                
            this.fromLinkCombo.setValue(5);
            this.POStore.proxy.conn.url = "ACCPurchaseOrderCMN/getRequisitions.do";
            this.POStore.load({
                params: {
                    currencyfilterfortrans: this.Currency.getValue(),
                    nondeleted: true,
                    onlyApprovedRecords: true,
                    isPRLinktoVQ:true
                }
            });
            this.PO.enable();
            this.fromPO.enable();
            this.Grid.isQuotationFromPR = true;
        }
    },
    
    enablePO:function(c,rec){
        this.autoGenerateDO.enable();
        if(rec.data['value']==true){
            if(!this.isCustBill&&!this.isCustomer&&!this.isEdit&&!this.copyInv&&!(this.isOrder&&(!this.isCustomer))){//this.isExpenseInv=false;
                this.GridPanel.setActiveTab(this.ProductGrid);
                this.ExpenseGrid.disable();

            }
            if(!(this.isCustBill ||this.cash)){
                this.fromLinkCombo.enable();
            }else{
                if(!this.isCustBill && this.isOrder && this.isCustomer && this.quotation)   //loading vendor Quotations in Customer Quotations
                    this.POStore.load({params:{validflag:true,billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue())}});
                else if(!this.isCustBill && this.isOrder && this.isCustomer && !this.quotation){   //loading Quotations in sales order
//                    if(this.isLeaseFixedAsset){
//                        this.POStore.load({
//                            params:{
//                                id:this.Name.getValue(),
//                                currencyid:this.Currency.getValue(),
//                                validflag:true,
//                                billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue())
//                            }
//                        });
//                    }else{
//                        this.POStore.load({params:{id:this.Name.getValue(),currencyid:this.Currency.getValue(),validflag:true,billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue())}});
//                    }
                    this.fromLinkCombo.enable();
                }
                else if(!this.isCustomer && !this.isCustBill && this.isOrder) //loading for Sales Orders in Purchase Orders
                    this.POStore.load();    
                else                                                         //loading for so and po in CI and VI in With/Without inventory mode and but not in trading flow
                    this.POStore.load({params:{id:this.Name.getValue()}});                
                this.PO.enable();
            }                                                      
            this.fromOrder=true;
            if(!this.isCustBill && this.isOrder && this.isCustomer && !this.quotation){
                if(this.isLeaseFixedAsset){
                    this.PO.multiSelect=false;
                    this.isMultiSelectFlag=false;
                    this.PO.removeListener("blur",this.populateData,this);
                    this.PO.addListener("select",this.populateData,this);
                }else{
                    this.PO.multiSelect=true;
                    this.isMultiSelectFlag=true;
                    this.PO.removeListener("select",this.populateData,this);
                    this.PO.addListener("blur",this.populateData,this);
                }
            }else if(this.isCustBill && !this.isOrder && !this.quotation){// this.isCustBill ||this.isCustomer
                this.PO.multiSelect=true;
                this.isMultiSelectFlag=true;
                this.PO.removeListener("select",this.populateData,this);
                this.PO.addListener("blur",this.populateData,this);     
            }else {
                this.PO.multiSelect=false;
                this.isMultiSelectFlag=false;
                this.PO.removeListener("blur",this.populateData,this);
                this.PO.addListener("select",this.populateData,this);
            }                        
        }
        else{
            if(!this.isCustBill && !this.isOrder && !this.cash && this.isCustomer)
            {
                this.fromLinkCombo.disable();
                this.PO.disable();
            }    
            this.loadStore();
            if(!this.isCustBill&&!this.isCustomer&&!this.isEdit&&!this.copyInv){//this.isExpenseInv=false;
                this.ExpenseGrid.enable();
            }    
            this.setDate();
            
            if(this.partialInvoiceCmb){
                this.partialInvoiceCmb.disable();
                var id=this.Grid.getId();
                var rowindex=this.Grid.getColumnModel().getIndexById(id+"partdisc");
                this.Grid.getColumnModel().setHidden( rowindex,true) ;
            }
            this.isGRLinkedInPI=false;
        }
        this.currencyStore.load(); 	       // Currency id issue 20018
        this.Grid.linkedFromOtherTransactions=false;
    },
    enabletax:function(c,rec){
        if(rec.data['value']==true){
            this.Tax.enable();
            this.includingGST.setValue(false);
            this.includingGST.disable();
            this.includeProTax.setValue(false);
            this.includeProTax.disable();
        }else{
            this.Tax.disable();
            this.Tax.setValue("");
            this.includingGST.enable();
            this.includeProTax.enable();
        }
        /*If rec.data['value'] = True means Hidden False
        *If rec.data['value'] = False means Hidden True
        *This function written in CommonERPComponent.js
        */
        this.HideShowLinkedTermTaxAndTermTaxAmountCol(rec.data['value']);
        this.setSingleMappedTaxToInvoiceTerm(rec.data['value']);
        if(rec.data['value']==false){
            this.resetLinkedTaxNameAndTermTaxAmount(rec.data['value']);
        }
        this.updateSubtotal();
    },
    /*
     * This method is called For Project Status Report- For PM-Accounting Integration
     */
    setPOLinks:function(){
        this.fromPO.enable();
        this.fromPO.setValue(true); 
        this.fromLinkCombo.enable();
        this.fromLinkCombo.setValue(0);
        this.PO.enable()
//        this.POStore.on('load', function(){
//            alert('kk');
//        if(this.isFromProjectStatusRep){
//            if(this.SOLinkedArr.length>0){
//                this.PO.setValue(this.SOLinkedArr);
//            }
//        this.populateData("","");
//        }
//        },this);
        this.POStore.load(); 
        
    },
    populateData:function(c,rec) {
        this.singleLink = false;
         if(this.PO.getValue()!=""){
             
            // if you are creating Sales Order by linking with Quotation && Quotation is containing Contract(if it is created from Replacement Number)
            //OR - you are creating Invoice through linking with delivery order and DO is containing Contract
            if(this.isCustomer && this.isLeaseFixedAsset && !this.quotation){
                var doIdsArray = this.PO.getValue().split(",");
                var isMultipleContractsSelected = WtfGlobal.isMultipleContractsSelected(doIdsArray,this.PO.store);
             
                if(isMultipleContractsSelected){
                    if(this.isOrder){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.linking.cq.selection.msg") ], 3);
                    }else{
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.linking.do.selection.msg") ], 3);
                    }
                    
                    return;
                }
            }
             
          var billid=this.PO.getValue();
          this.clearComponentValues();
          this.Grid.fromPO=true; 
          this.Grid.linkno=this.PO.getValue();
          this.Grid.linkedFromOtherTransactions=true; 
          if(this.isLeaseFixedAsset && this.isOrder){// in case of Lease Sales Order
              if(this.fromLinkCombo.getValue()==1){
                  this.Grid.isLinkedFromReplacementNumber=true;
              }
          }else{
              this.Grid.isLinkedFromReplacementNumber=false;
          }
        
        if(this.isLeaseFixedAsset && (this.isOrder || this.quotation)){
            if(this.isOrder && this.fromLinkCombo && this.fromLinkCombo.getValue() == 0){// if SO linked with CQ
                this.Grid.isLinkedFromCustomerQuotation=true;
            }    
        } else{
            this.Grid.isLinkedFromCustomerQuotation=false;
        }
          
          var multipleSelectProdIncludeFlag=false;
          if(this.isMultiSelectFlag){ //For MultiSelection 
            var selectedids=this.PO.getValue();
            var selectedValuesArr = selectedids.split(',');
            if(selectedValuesArr.length==1){  // Load value of Include product tax according to PO
                rec=this.POStore.getAt(this.POStore.find('billid',selectedValuesArr[0]));
                var isapplytaxtoterms = (rec.data["isapplytaxtoterms"] != undefined && rec.data["isapplytaxtoterms"]!="")?rec.data["isapplytaxtoterms"]:false; 
                if (rec != undefined) {
                    /*
                    *	ERM-1037
                    *	Send id of linked document to function to restrict linking of future dated document
                    */
                   var isFutureDatedDocumentLinked=WtfGlobal.checkForFutureDate(this, selectedValuesArr);
                   if(isFutureDatedDocumentLinked){
                       return;
                   }
                }
                if(WtfGlobal.getModuleId(this)!=Wtf.Acc_Vendor_Quotation_ModuleId){//In VQ we can link only Preq and in Purchase Req there is no address. So we cannot link address
                    this.linkRecord = this.POStore.getAt(this.POStore.find('billid',selectedValuesArr[0]));
                    this.singleLink = true;
                }
                if(rec.data["includeprotax"]){
                    this.includeProTax.setValue(true);
                    this.showGridTax(null,null,false);
                    this.isTaxable.disable();
                    this.Tax.setValue("");
                    this.Tax.disable();
                    /**
                     *Handle linking case for apply term tax.
                     */
                    if(rec.data["gstIncluded"]){
                         this.includingGST.setValue(true);
                    }
                    this.applyTaxToTermsChk.getEl().up('.x-form-item').setDisplayed(true);
                    this.applyTaxToTermsChk.setValue(isapplytaxtoterms); 
                }else{
                    this.includeProTax.setValue(false);
                    this.showGridTax(null,null,true);
                    this.Tax.enable();//required because when selected multiple records & changing to select single record.Before it was getting disabled.
                    this.isTaxable.enable();
                }
                
                var rowRateIncludingGstAmountIndex=-1;
                if(this.isExpenseInv){
                    rowRateIncludingGstAmountIndex=this.Grid.getColumnModel().findColumnIndex("rateIncludingGstEx");
                } else{
                    rowRateIncludingGstAmountIndex=this.Grid.getColumnModel().findColumnIndex("rateIncludingGst");
                }
                if(rowRateIncludingGstAmountIndex!=-1){
                    if (rec.data["gstIncluded"]&&!this.includingGST.getValue()){
                        this.includingGST.setValue(true);
                        this.includeProTax.setValue(true);   //disabling product level tax combo when including GST is applied while linking other document ERP-32672
                        this.includeProTax.disable();   //disabling product level tax combo when including GST is applied while linking other document ERP-32672
                        this.Grid.getColumnModel().setHidden(rowRateIncludingGstAmountIndex,false);
                    }else if(!rec.data["gstIncluded"]&&this.includingGST.getValue()){
                        this.includingGST.setValue(false);
                        this.includeProTax.setValue(false);
                        this.includeProTax.enable();    //enabling product level tax combo when including GST is not applied linking other document ERP-32672
                        this.Grid.getColumnModel().setHidden(rowRateIncludingGstAmountIndex,true);
                    }                        
                }
                if(this.IsInvoiceTerm) {
                    this.setTermValues(rec.data.termdetails);
                }
                var linkedRecordExternalCurrencyRate=rec.data["externalcurrencyrate"];
                if (this.Currency.getValue()!=WtfGlobal.getCurrencyID && linkedRecordExternalCurrencyRate!="" && linkedRecordExternalCurrencyRate!=undefined) { // If selected currency is foreign currency then currency exchange rate will be exchange rate of linked document except in cross link case
                    this.externalcurrencyrate=linkedRecordExternalCurrencyRate;
                }
                
            } else if(selectedValuesArr.length>1){
                
                var productLevelTax=false;  
                var isGSTTax=false;
                var isInvoiceLevelTax=false;
                var withoutTax=false;
                this.previusTaxId="";
                var isInvoiceTaxDiff=false;
                var invoiceLevelTaxRecords=0;
                var reccustomerporefno='';
                for(var cnt=0;cnt<selectedValuesArr.length;cnt++){
                    rec=this.POStore.getAt(this.POStore.find('billid',selectedValuesArr[cnt]));
                    if(rec.data.contract!=undefined && rec.data.contract!=""){   // check for quoatation with contract
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.alert.quotationtype")], 2);
                        this.PO.clearValue();
                        return; 
                    }
                    
                    
                     if(rec.data["gstIncluded"]){ //checks for GST Tax
                        isGSTTax=true;
                    }else if(rec.data["includeprotax"]){ //checks for product level tax
                        productLevelTax=true;
                    }else if(rec.data["taxid"]!="" && rec.data["taxid"]!=undefined && rec.data["taxid"] != "None"){ //checks for invoice level tax 
                        isInvoiceLevelTax=true;                        
                        if(invoiceLevelTaxRecords!=0 && this.previusTaxId!=rec.data["taxid"]){
                            isInvoiceTaxDiff=true;
                        }
                        this.previusTaxId=rec.data["taxid"];
                        this.includeProTax.setValue(false);
                        this.showGridTax(null,null,true);//updating include product tax
                        invoiceLevelTaxRecords++;
                    }else{
                        withoutTax=true;//applicable for both no tax and diff tax
                    }
                    
                }
                /*
                *	ERM-1037
                *	Send id of linked document to function to restrict linking of future dated document
                */
                WtfGlobal.checkForFutureDate(this,selectedValuesArr);                    
                    if(isGSTTax){ //case when any linked record have GST Tax
                    var includeGstCount=0;
                    var excludeGstCount=0;
                    for(var cntGst=0;cntGst<selectedValuesArr.length;cntGst++){
                        rec=this.POStore.getAt(this.POStore.find('billid',selectedValuesArr[cntGst]));
                        if(rec.data["gstIncluded"]){
                            includeGstCount++;
                        }else if(!rec.data["gstIncluded"]){
                            excludeGstCount++;
                        }
                    }

                    if(!((selectedValuesArr.length==includeGstCount)||(selectedValuesArr.length==excludeGstCount))){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.alert.includingGST")], 2);
                        this.PO.clearValue();
                        return;
                    }else{
                        if(selectedValuesArr.length==includeGstCount){
                            this.includeProTax.setValue(true);
                            this.includingGST.setValue(true);
                        }else if(selectedValuesArr.length==excludeGstCount){
                            this.includeProTax.setValue(false);
                            this.includingGST.setValue(false);
                        }
                    }
                }else if(productLevelTax){//case when any linked record have product tax without GST Tax
                    if(isInvoiceLevelTax){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.alert.includingProductTax")], 2);
                        this.PO.clearValue();
                        return;
                    }else{//no tax and producttax
                        this.includeProTax.setValue(true);
                        this.showGridTax(null,null,false); 
                        this.isTaxable.setValue(false);//when selcting record with product tax.Tax should get disabled.
                        this.isTaxable.disable();
                        this.Tax.setValue("");
                        this.Tax.disable();
                    }                   
                }else if(isInvoiceLevelTax ){
                    if(withoutTax || isInvoiceTaxDiff){//for different tax and empty tax
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.alert.includingDifferentTax")], 2); 
                        this.PO.clearValue();
                        return;
                    }else{
                        if(this.fromLinkCombo.getValue()!==2 && ((!this.isCustomer)?this.fromLinkCombo.getValue()==0:this.fromLinkCombo.getValue()!==0)){
                            WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.alert'),WtfGlobal.getLocaleText('acc.invoiceform.linkTax')],2);  
                        }
                        this.Tax.enable();
                        this.isTaxable.enable();
                        this.isTaxable.setValue(true);
                        this.Tax.setValue(this.previusTaxId);                            
                    }
                    this.includeProTax.setValue(false); //update include product tax
                    this.showGridTax(null,null,true);
                }else {//for goodsreceiptorder and deliveryorder
                    this.Tax.disable();
                    this.isTaxable.enable();
                    this.isTaxable.setValue(false);
                    this.Tax.setValue("");
                    this.includeProTax.setValue(false); //update include product tax
                    this.showGridTax(null,null,true);
                }
                
                
                
                var isLinkedDocumentHaveSameER=true;           
                var linkedExternalRate=0;
                if(this.Currency.getValue()!=WtfGlobal.getCurrencyID){ // Foreign currency linking case. In this case we have to borrow Linked document Exchange Rate in current document.                  
                    for(var count=0;count<selectedValuesArr.length;count++){
                        var tempRec =WtfGlobal.searchRecord(this.POStore,selectedValuesArr[count],"billid");                        
                        if(count==0){
                            linkedExternalRate = tempRec.data["externalcurrencyrate"]; // taking externalcurrencyrate of first record and then comparing it with other records external currency rate
                        } else if(tempRec.data["externalcurrencyrate"]!=linkedExternalRate) {
                            isLinkedDocumentHaveSameER =false;  
                            break;
                        }
                    } 
                    if(isLinkedDocumentHaveSameER){ //if exchange rate same for all linked document then applying it for current record by assigning here 
                        this.externalcurrencyrate=linkedExternalRate;
                    } else { //if exchange rate different then reassigning exchange rate of that date and giving below information message 
                        var index=this.getCurrencySymbol();
                        var exchangeRate = this.currencyStore.getAt(index).data['exchangerate'];
                        this.externalcurrencyrate=exchangeRate;
                        var msg = WtfGlobal.getLocaleText("acc.invoiceform.exchangeratemessage1")+"<b> "+this.externalcurrencyrate+" </b>"+WtfGlobal.getLocaleText("acc.invoiceform.exchangeratemessage2");                        
                        WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.information'),msg],3);
                    }
                }
                    
//                    if(rec.data["includeprotax"]){
//                        this.includeProTax.setValue(true);
//                        this.showGridTax(null,null,false);
//                        multipleSelectProdIncludeFlag=true;
//                        break;
//                    }
//                }
//                if(!multipleSelectProdIncludeFlag){
//                    this.includeProTax.setValue(false);
//                    this.showGridTax(null,null,true);
//                }
            } else{
                this.includeProTax.setValue(false);
                this.showGridTax(null,null,true);
            }
            
            //this.isTaxable.setValue(false);
            //this.isTaxable.disable();
            //this.Tax.setValue("");
            //this.Tax.disable();
            
//            this.showGridTax(null,null,false);        
            //this.Tax.disable();
            this.isTaxable.reset();
            //this.updateData();
            this.Tax.reset();            
//            if(!(!this.isCustomer || (this.isOrder&&!this.isCustomer))){
//                    if(this.Name.getValue()!=""){
//                      this.billingAddrsStore.load({params:{customerid:this.Name.getValue()}});
//                      this.ShippingAddrsStore.load({params:{customerid:this.Name.getValue()}});         
//                    }                
//                }              
            this.setValues(billid);//In MultiSelection if the user select only one                 
            rec=this.PO.getValue();
            var selectedValuesArr = rec.split(',');
            if(selectedValuesArr.length==1){
                var record=this.POStore.getAt(this.POStore.find('billid',billid));
                this.isCustomer ? this.users.setValue(record.data["salesPerson"]) : this.users.setValue(record.data["agent"]); 
                if(this.partialInvoiceCmb){
                    this.partialInvoiceCmb.enable();
                }
                this.resetCustomFields();
                var fieldArr = this.POStore.fields.items;
                for(var fieldCnt=0; fieldCnt < fieldArr.length; fieldCnt++) {
                    var fieldN = fieldArr[fieldCnt];
                   
                    if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id) && record.data[fieldN.name] !="") {
                          if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).getXType()=='datefield'){
                             Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(record.data[fieldN.name]);
                          }else if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).getXType()=='fncombo'){
                                var ComboValue=record.data[fieldN.name];
//                                var ComboValueID="";
//                                var recCustomCombo =WtfGlobal.searchRecord(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).store,ComboValue,"name");
                                if(ComboValue){
//                                    ComboValueID=recCustomCombo.data.id;
                                    Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(ComboValue);
                                    var  childid= Wtf.getCmp(fieldN.name+this.tagsFieldset.id).childid;
                                    if(childid.length>0){
                                        var childidArray=childid.split(",");
                                        for(var i=0;i<childidArray.length;i++){
                                            var currentBaseParams = Wtf.getCmp(childidArray[i]+this.tagsFieldset.id).store.baseParams;
                                            currentBaseParams.parentid=ComboValue;
                                            Wtf.getCmp(childidArray[i]+this.tagsFieldset.id).store.baseParams=currentBaseParams;
                                            Wtf.getCmp(childidArray[i]+this.tagsFieldset.id).store.load();
                                        }
                                    }  
                                }
                          }else{
                            Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(record.data[fieldN.name]);
                          }
                    }
                    if(fieldN.name.indexOf("Custom_")==0){
                        var fieldname=fieldN.name.substring(7,fieldN.name.length);
                        if(Wtf.getCmp(fieldname+this.tagsFieldset.id) && record.data[fieldN.name] !="") {
                            if(Wtf.getCmp(fieldname+this.tagsFieldset.id).getXType()=='fieldset'){
                                    var ComboValue=record.data[fieldN.name];
                                    var ComboValueArrya=ComboValue.split(',');
                                    var ComboValueID="";
                                    var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray; 
                                    for(var i=0 ;i < ComboValueArrya.length ; i++){
                                        for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
                                            if(checkListCheckBoxesArray[checkitemcnt].id.indexOf(ComboValueArrya[i]) != -1 )
                                                if (Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id) != undefined) {
                                                    Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id).setValue(true);
                                                }
                                        } 
                                    }
                            }else if(Wtf.getCmp(fieldname+this.tagsFieldset.id).getXType()=='select'){
                                    var ComboValue=record.data[fieldN.name];
//                                    var ComboValueArrya=ComboValue.split(',');
//                                    var ComboValueID="";
//                                    for(var i=0 ;i < ComboValueArrya.length ; i++){
//                                        var recCustomCombo =WtfGlobal.searchRecord(Wtf.getCmp(fieldname+this.tagsFieldset.id).store,ComboValueArrya[i],"name");
//                                        ComboValueID+=recCustomCombo.data.id+","; 
//                                    }
//                                    if(ComboValueID.length > 1){
//                                        ComboValueID=ComboValueID.substring(0,ComboValueID.length - 1);
//                                    }
                                    if(ComboValue!="" && ComboValue!=undefined)
                                    Wtf.getCmp(fieldname+this.tagsFieldset.id).setValue(ComboValue);
                            }

                        }
                    }
                }
            }else{
                this.users.reset();
                if(this.partialInvoiceCmb){
                    this.partialInvoiceCmb.reset();
                    this.partialInvoiceCmb.disable();
                    var id=this.Grid.getId();
                    var rowindex=this.Grid.getColumnModel().getIndexById(id+"partdisc");
                    if(rowindex>=0){    
                        this.Grid.getColumnModel().setHidden( rowindex,true) ;
                    }
                }
            }
        }else{                
            rec=this.POStore.getAt(this.POStore.find('billid',billid));
            this.resetCustomFields();
            var fieldArr = this.POStore.fields.items;
            for(var fieldCnt=0; fieldCnt < fieldArr.length; fieldCnt++) {
                var fieldN = fieldArr[fieldCnt];
                   
                if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id) && (record!=undefined && record.data[fieldN.name] !="")) {
                    if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).getXType()=='datefield'){
                        Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(record.data[fieldN.name]);
                    }else if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).getXType()=='fncombo'){
                        var ComboValue=record.data[fieldN.name];
//                                var ComboValueID="";
//                                var recCustomCombo =WtfGlobal.searchRecord(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).store,ComboValue,"name");
                                if(ComboValue){
//                                    ComboValueID=recCustomCombo.data.id;
                                    Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(ComboValue);
                                    var  childid= Wtf.getCmp(fieldN.name+this.tagsFieldset.id).childid;
                                    if(childid.length>0){
                                        var childidArray=childid.split(",");
                                        for(var i=0;i<childidArray.length;i++){
                                            var currentBaseParams = Wtf.getCmp(childidArray[i]+this.tagsFieldset.id).store.baseParams;
                                            currentBaseParams.parentid=ComboValue;
                                            Wtf.getCmp(childidArray[i]+this.tagsFieldset.id).store.baseParams=currentBaseParams;
                                            Wtf.getCmp(childidArray[i]+this.tagsFieldset.id).store.load();
                                        }
                                    }  
                                }
                    }else{
                        Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(record.data[fieldN.name]);
                    }
                }
                if(fieldN.name.indexOf("Custom_")==0){
                    var fieldname=fieldN.name.substring(7,fieldN.name.length);
                    if(Wtf.getCmp(fieldname+this.tagsFieldset.id) && record.data[fieldN.name] !="") {
                        if(Wtf.getCmp(fieldname+this.tagsFieldset.id).getXType()=='fieldset'){
                            var ComboValue=eval("record.json['"+fieldN.name + "_Values']");
                            var ComboValueArrya=ComboValue.split(',');
                            var ComboValueID="";
                            var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray; 
                            for(var i=0 ;i < ComboValueArrya.length ; i++){
                                for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
                                    if(checkListCheckBoxesArray[checkitemcnt].id.indexOf(ComboValueArrya[i]) != -1 )
                                        if (Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id) != undefined) {
                                            Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id).setValue(true);
                                        }
                                } 
                            }
                        }else if(Wtf.getCmp(fieldname+this.tagsFieldset.id).getXType()=='select'){
                            var ComboValue=eval("record.json['"+fieldN.name + "_Values']");
//                                    var ComboValueArrya=ComboValue.split(',');
//                                    var ComboValueID="";
//                                    for(var i=0 ;i < ComboValueArrya.length ; i++){
//                                        var recCustomCombo =WtfGlobal.searchRecord(Wtf.getCmp(fieldname+this.tagsFieldset.id).store,ComboValueArrya[i],"name");
//                                        ComboValueID+=recCustomCombo.data.id+","; 
//                                    }
//                                    if(ComboValueID.length > 1){
//                                        ComboValueID=ComboValueID.substring(0,ComboValueID.length - 1);
//                                    }
                                    if(ComboValue!="" && ComboValue!=undefined)
                                    Wtf.getCmp(fieldname+this.tagsFieldset.id).setValue(ComboValue);
                        }

                    }
                }
            }
            // if(this.isOrder && this.isCustomer && !this.isCustBill){//Temporary check to hide/display product tax for order. Need to fix for Invoices also
                if(rec.data["includeprotax"]){
                    this.includeProTax.setValue(true);
                    
                    this.isTaxable.setValue(false);
                    this.isTaxable.disable();
                    this.Tax.setValue("");
                    this.Tax.disable();
                    if(rec.data["gstIncluded"]){
                        this.includingGST.setValue(true);
                    }
                    this.applyTaxToTermsChk.getEl().up('.x-form-item').setDisplayed(true);
                    this.applyTaxToTermsChk.setValue(rec.data["isapplytaxtoterms"]);
                    
                    this.showGridTax(null,null,false);
                } else {
                    this.includeProTax.setValue(false);
                    this.isTaxable.reset();
                    this.isTaxable.enable();
                    this.showGridTax(null,null,true);
                }
                if (rec.data["gstIncluded"] && !this.includingGST.getValue()) {
                    this.includingGST.setValue(true);
                } else if (!rec.data["gstIncluded"] && this.includingGST.getValue()) {
                    this.includingGST.setValue(false);
                }
    //        } else {
    //            this.includeProTax.setValue(true);
    //            this.showGridTax(null,null,false);
    //        }
              if(!this.isCustBill && !this.isOrder && !this.cash && this.isCustomer){
               if(this.fromLinkCombo.getValue()==1){
                    this.includeProTax.setValue(false);
                    this.showGridTax(null,null,true);            
                }
            }
            this.Memo.setValue(rec.data['memo']);
            this.shipDate.setValue(rec.data['shipdate']);
            this.validTillDate.setValue(rec.data['validdate']);
            if((this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId||this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId) && !this.cash &&!this.isFixedAsset) { // set value only in VI module
                this.invoiceList.setValue(rec.data['landedInvoiceNumber']);
            }
            if(((this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId ) && !this.cash) || (this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId && !this.cash)) {
               this.Term.setValue(rec.data['termid']);
            }
            this.postText=rec.data['posttext'];
            this.shipvia.setValue(rec.data['shipvia']);
            this.fob.setValue(rec.data['fob']);
            if(this.users != null && this.users != undefined){
                this.isCustomer ? this.users.setValue(rec.data['salesPerson']) : this.users.setValue(rec.data['agent']);
            }
            //this.Name.setValue(rec.data['personid']);
            this.loadTransStore();
            
            if(rec.data["discounttotal"] && this.Discount){
            this.Discount.setValue(rec.data["discounttotal"]);
            this.perDiscount.setValue(rec.data["discountispertotal"]);
        }
        
            if(rec.data['taxid']!=""){
                this.Tax.enable();
                this.isTaxable.setValue(true);
                this.Tax.setValue(rec.data['taxid']);
                this.HideShowLinkedTermTaxAndTermTaxAmountCol(this.isTaxable.getValue());
            }else{
                this.Tax.disable();
                this.isTaxable.reset();
                this.Tax.reset();
            }
            this.getCreditTo(rec.data.creditoraccount);            
//            if(this.fromLinkCombo.getValue()==1){
//                this.updateData();              
//            }else{
//                this.Currency.setValue(rec.data['currencyid']);
//            }
            var perstore=this.isCustomer? Wtf.customerAccStore:Wtf.vendorAccStore
            var index = perstore.find('accid',this.Name.getValue());
            if(index != -1){
                var storerec=perstore.getAt(index);
//                if(!(!this.isCustomer || (this.isOrder&&!this.isCustomer))){
//                    if(this.Name.getValue()!=""){
//                      this.billingAddrsStore.load({params:{customerid:this.Name.getValue()}});
//                      this.ShippingAddrsStore.load({params:{customerid:this.Name.getValue()}});         
//                    }                
//                }
                this.Term.setValue(storerec.data['termid']);
            }        
            this.CostCenter.setValue(rec.data.costcenterid);
            rec=rec.data['billid'];
        }                                       
        this.updateDueDate();
        var url = "";
		//(this.isCustBill?53:43)
        var soLinkFlag = false;        
        var VQtoCQ = false;
        var isLinkingOfReplacementRequest = false;
        var isPRlinktoVQ = false;
        var FA_VQlinkToFA_PO = false;
        var FA_POlinkToFA_PI = false;
        var linkDisposalINV = false;
        var isFA_VQlinkToPI = false;
        var linkingFlag = false; //For removing cross reference of DO-CI or GR-VI
        var sopolinkflag=false;
        if(!this.isCustBill && !this.isOrder && !this.cash ){
            if(this.fromLinkCombo.getValue()==0){
                url = this.isCustomer ? 'ACCSalesOrderCMN/getSalesOrderRows.do' : 'ACCPurchaseOrderCMN/getPurchaseOrderRows.do';
                linkingFlag = true;
                FA_POlinkToFA_PI = true;
            } else if(this.fromLinkCombo.getValue()==1){
                url = this.isCustomer ? "ACCInvoiceCMN/getDeliveryOrderRows.do" : "ACCGoodsReceiptCMN/getGoodsReceiptOrderRows.do";
                linkingFlag=true;
                linkDisposalINV=true;
            }else if(this.fromLinkCombo.getValue()==2){
                url = this.isCustomer ? "ACCSalesOrderCMN/getQuotationRows.do" : "ACCPurchaseOrderCMN/getQuotationRows.do";
                VQtoCQ = true;//Linking Quotation when creating invoice, we need to display Unit Price excluding row discount
                linkingFlag = true;
                isFA_VQlinkToPI = true;
            }
        } else {
            if(this.isCustomer){
                if(this.quotation){
                    if(this.fromLinkCombo.getValue()==0){
                        soLinkFlag = true;
                        url = "ACCPurchaseOrderCMN/getQuotationRows.do";
                        VQtoCQ = true;
                    }else{
                        url = "ACCSalesOrderCMN/getReplacementRequestRows.do";
                    }
                } else if(this.isOrder){
                    if(this.isLeaseFixedAsset){
                        if(this.fromLinkCombo.getValue()==0){
                            url = "ACCSalesOrderCMN/getQuotationRows.do";
                            VQtoCQ = true;
                            soLinkFlag:true
                        }else{
                            url = "ACCSalesOrderCMN/getReplacementRequestRows.do";
                        }
                    }else{
                        url = "ACCSalesOrderCMN/getQuotationRows.do";
                        VQtoCQ = true;
                    }
                } else {
                    url = this.isCustBill?"ACCSalesOrderCMN/getBillingSalesOrderRows.do":'ACCSalesOrderCMN/getSalesOrderRows.do';  
                }
            } else {
                if(this.isOrder){
                    if (this.quotation) {
                        url = 'ACCPurchaseOrderCMN/getRequisitionRows.do';
                        linkingFlag = true;
                        isPRlinktoVQ = true;
                    } else {
                        url = 'ACCSalesOrderCMN/getSalesOrderRows.do';
                    }
                    if(this.isCustBill) {
                        url = "ACCSalesOrderCMN/getBillingSalesOrderRows.do";
                    } else {
                        if(this.fromLinkCombo.getValue()==0){
                            url = 'ACCSalesOrderCMN/getSalesOrderRows.do';
                            soLinkFlag = true;
                        } else if(this.fromLinkCombo.getValue()==2){
                            url = 'ACCPurchaseOrderCMN/getQuotationRows.do';
                            VQtoCQ = true;
                            linkingFlag = true;
                            FA_VQlinkToFA_PO = true;
                            sopolinkflag=true;
                        }
                    }
                } else {
                    url = this.isCustBill?"ACCPurchaseOrderCMN/getBillingPurchaseOrderRows.do":'ACCPurchaseOrderCMN/getPurchaseOrderRows.do';
                }
            }
        }                
	this.Grid.getStore().proxy.conn.url = url;
            this.Grid.loadPOGridStore(rec, soLinkFlag, VQtoCQ,linkingFlag,this.isInvoice,isPRlinktoVQ,FA_VQlinkToFA_PO,FA_POlinkToFA_PI,linkDisposalINV,isFA_VQlinkToPI,sopolinkflag);          
        }
    },
    
    loadDataForProjectStatusReport:function(){
        this.isFromProjectStatusRep = false;
        var url = "ACCSalesOrderCMN/getSalesOrderRows.do";
        var rec = "";
        for(var i=0;i<this.SOLinkedArr.length;i++){
            rec+=this.SOLinkedArr[i]+',';
        }
        if(rec !=""){
            rec = rec.substring(0,rec.length-1);
        }
        this.Grid.getStore().proxy.conn.url = url;
        this.Grid.loadPOGridStore(rec, false, false,false,this.isInvoice); 
    },
    //Below method is called on select event of Customer combo box
    setTerm:function(c,rec,ind){
        this.Term.setValue(rec.data['termid']);
        this.updateDueDate();
    },
    
    setSalesPerson:function(c,rec,ind){
        this.users.setValForRemoteStore(rec.data['masterSalesPerson'],rec.data['masterSalesPersonName'],rec.data['hasAccess']);
    },
    setAgent:function(c,rec,ind){
         this.users.setValForRemoteStore(rec.data['masteragent'],rec.data['masteragentname'],rec.data['hasAccess']);
    },
    updateSubtotal:function(a,termAmountAlreadyChanged){
        if(this.calDiscount())return;
        this.isClosable=false; // Set Closable flag after updating grid data
        this.applyCurrencySymbol();
        if(!termAmountAlreadyChanged){
            this.updateSubtotalOnTermChange(false);
        }
        var subtotal=0.00;
        var subtotalValue=0.00;
        var tax=0.00;
        var taxAndSubtotal=this.Grid.calLineLevelTax();
        if(this.includeProTax.getValue() && Wtf.account.companyAccountPref.isLineLevelTermFlag==0){ 
            subtotal=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[0]-taxAndSubtotal[1],this.symbol)
            subtotalValue=taxAndSubtotal[0]-taxAndSubtotal[1];
            tax = taxAndSubtotal[1];
            if(this.includeProTax.getValue() && this.applyTaxToTermsChk.getValue()){
                tax += this.addTermAmountInTax();
            }
            tax=WtfGlobal.addCurrencySymbolOnly(tax,this.symbol);
        }else{
            subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol)
            subtotalValue=this.Grid.calSubtotal();
            tax=WtfGlobal.addCurrencySymbolOnly(this.caltax()+this.addTermAmountInTax(),this.symbol);
        }
          
        var calTermTotal = WtfGlobal.addCurrencySymbolOnly(this.findTermsTotal(),this.symbol);
        var amountbeforetax = WtfGlobal.addCurrencySymbolOnly(subtotalValue+this.findTermsTotal(),this.symbol);
        /**
         *While changing the any field from grid, termStore is get reload and the invoice term grid is get reset
         *this should not be happen so commneted the code.
         */
        if(a!=undefined && a.id!=undefined && a.id.indexOf("editproductdetailsgrid") != -1){
//            this.termStore.reload();
            this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:subtotal,discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:tax,termtotal:calTermTotal,amountbeforetax:amountbeforetax,aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.findTermsTotal()+this.caltax()+this.addTermAmountInTax(),this.symbol),totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol()),amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())});
        }
        else{
            this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:subtotal,discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:tax,termtotal:calTermTotal,amountbeforetax:amountbeforetax,aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.findTermsTotal()+this.caltax()+this.addTermAmountInTax(),this.symbol),totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol()),amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())});
        }
        
        if(this.isFromProjectStatusRep){
            if(this.SOLinkedArr.length>0){
                this.PO.setValue(this.SOLinkedArr);
            }
        this.isMultiSelectFlag = true;    
        this.loadDataForProjectStatusReport();
        }
    },
    
    getNextSequenceNumber:function(a,val){
       if(!(a.getValue()=="NA")){
         WtfGlobal.hideFormElement(this.Number);
         this.setTransactionNumber(true);
         var rec=WtfGlobal.searchRecord(this.sequenceFormatStore, a.getValue(), 'id');
         var oldflag=rec!=null?rec.get('oldflag'):true;
         Wtf.Ajax.requestEx({
            url:"ACCCompanyPref/getNextAutoNumber.do",
            params:{
                from:this.fromnumber,
                sequenceformat:a.getValue(),
                oldflag:oldflag
            }
        }, this,function(resp){
            if(resp.data=="NA"){
                WtfGlobal.showFormElement(this.Number);
                this.Number.reset();
                this.Number.enable();
            }else {
                this.Number.setValue(resp.data);  
                this.Number.disable();
                WtfGlobal.hideFormElement(this.Number);
            }
            
        });
       } else {
           WtfGlobal.showFormElement(this.Number);
           this.Number.reset();
           this.Number.enable();
       }
    },
     getNextSequenceNumberDo:function(a,val){
      if(!(a.getValue()=="NA")){
        this.setTransactionNumberDo(true);
         var rec=WtfGlobal.searchRecord(this.sequenceFormatStoreDo, a.getValue(), 'id');
         var oldflag=rec!=null?rec.get('oldflag'):true;
         Wtf.Ajax.requestEx({
            url:"ACCCompanyPref/getNextAutoNumber.do",
            params:{
                from:this.fromnumberDo,
                sequenceformat:a.getValue(),
                oldflag:oldflag
            }
        }, this,function(resp){
            if(resp.data=="NA"){
                this.no.reset();
                this.no.enable();
            }else {
                    this.no.setValue(resp.data);
                    this.no.disable();
            }
            
        });
        } else {
            this.no.reset();
            this.no.enable();
        }
    },
    
    getDiscount:function(){
        var disc = 0;
        var per = 1;
//        if(!(this.isOrder && !this.quotation)){
            disc=this.Discount.getValue();
            per=this.perDiscount.getValue();
//        }
        var subtotalAfterTerm = this.Grid.calSubtotal() + this.findTermsTotal();
        return isNaN(parseFloat(disc))?0:(per?(disc*subtotalAfterTerm)/100:disc);
    },
    calDiscount:function(){
        var disc=this.Discount.getValue();
        var per=this.perDiscount.getValue();
        if(per && disc > 100){
            WtfComMsgBox(28,2);
            this.NorthForm.getForm().setValues({perdiscount:false});
            return true;
        }
        else
            return false;
    },
    calTotalAmount:function(){
        var subtotal=this.Grid.calSubtotal();
        var discount=this.getDiscount();
//        return subtotal-discount + this.findTermsTotal();
        return subtotal-discount;
    },
    calTotalAmountInBase:function(){
        var subtotal=this.Grid.calSubtotal(); 
        var discount=this.getDiscount();   
        var taxVal = this.caltax()+this.addTermAmountInTax();
        var returnValInOriginalCurr = subtotal-discount + this.findTermsTotal()+taxVal;
        returnValInOriginalCurr = getRoundedAmountValue(returnValInOriginalCurr*this.getExchangeRate());
        return returnValInOriginalCurr; 
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
    save:function(){
    var incash=false;
    this.Number.setValue(this.Number.getValue().trim()); 
        var isValidCustomFields=true;//this.tagsFieldset.checkMendatoryCombo();
        if (this.tagsFieldset && (this.moduleid==39 ||this.moduleid==38)) {
            isValidCustomFields = this.tagsFieldset.checkMendatoryCombo();
        }
        if(this.NorthForm.getForm().isValid() && this.exciseFormPanel.getForm().isValid() && isValidCustomFields){
            if(this.isCustBill){
                for(var datacount=0;datacount<this.Grid.getStore().getCount();datacount++){
                    var creditoracc=this.Grid.getStore().getAt(datacount).data['creditoraccount'];                    
                    if(creditoracc==undefined||creditoracc==""){
                        if(this.Grid.getStore().getAt(datacount).data['productdetail'].length>0){
                            var account=(this.isCustomer)?"Credit account":"Debit account";
                            WtfComMsgBox(["Warning","Please select "+account], 2);
                            return;
                        } 
                    }            
                }
            }
            /*
             * Validate GST dimension values present or Not
             */
            if (WtfGlobal.isIndiaCountryAndGSTApplied() && Wtf.isShowAlertOnDimValueNotPresent.indexOf(parseInt(this.moduleid))> -1) {
                if (!isGSTDimensionValuePresent(this, this.Grid)) {
                    return false;
                }
                /**
                 * Show alert on Save document if GST details not presnet 
                 * ERP-39257
                 */
                if (!isGSTHistoryPresentOnDocumentCreation(this)) {
                    return false;
                 }
            }
            // Checking for deactivated products
            var inValidProducts=this.checkForDeActivatedProductsAdded();
            if(inValidProducts!=''){
                inValidProducts = inValidProducts.substring(0, inValidProducts.length-2);
                Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.warning"), 
                        msg: WtfGlobal.getLocaleText("acc.common.followingProductsAreDeactivated")+'</br>'+'<b>'+inValidProducts+'<b>',
                        buttons: Wtf.MessageBox.OK,
                        icon: Wtf.MessageBox.WARNING,
                        scope: this,
                        scopeObj :this,
                        fn: function(btn){
                            if(btn=="ok"){
                                return;
                            }
                        }
                    });
                    return;
            }
            for(var i=0;i<this.Grid.getStore().getCount()-1;i++){
                var quantity=this.Grid.getStore().getAt(i).data['quantity'];
                var rate=this.Grid.getStore().getAt(i).data['rate'];
                if(quantity<=0){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.QuantityforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.shouldbegreaterthanZero")], 2);
                    return;
                } 
                if(rate===""||rate==undefined||rate<0){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Rate for Product "+this.Grid.getStore().getAt(i).data['productname']+" cannot be empty."], 2);
                    return;
                }
            }
            
            // In Case of Fixed Asset OR Lease Fixed Asset Check external and internal quantities are equal or not
            if(this.isFixedAsset || this.isLeaseFixedAsset){
                var arr=[];
                var key="";
                var isDuplicateAssetPresent=false;
                for(var i=0;i<this.Grid.getStore().getCount()-1;i++){// exclude last row
                    var quantity=this.Grid.getStore().getAt(i).data['quantity'];
                    
                    var productId = this.Grid.getStore().getAt(i).data['productid'];
                    key=productId;
                    var proRecord = (WtfGlobal.searchRecord(this.Grid.productComboStore,productId,'productid')!=null)?WtfGlobal.searchRecord(this.Grid.productComboStore,productId,'productid'):WtfGlobal.searchRecord(this.Grid.getStore(),productId,'productid');
                    if (proRecord.get('isAsset') && (!this.quotation || this.moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId)) {
                    
                    var assetDetails = this.Grid.getStore().getAt(i).data['assetDetails'];
                    
                    if(assetDetails == "" || assetDetails == undefined){
                        WtfComMsgBox(['Information',WtfGlobal.getLocaleText("acc.field.PleaseProvideAssetDetailsforAssetGroup")+" "+"<b>"+this.Grid.getStore().getAt(i).data['productname']+"</b>"+"."],0);
                        return;
                    }
                    
//                        var rateIntoQuantityVal = rate*quantity - assetDetailTotalSellingAmount;
//                        rateIntoQuantityVal = (rateIntoQuantityVal<0)?(-1)*rateIntoQuantityVal:rateIntoQuantityVal;

                    var assetDetailArray = eval('(' + assetDetails + ')');
                    
                    if(assetDetailArray == null || assetDetailArray == undefined){
                        WtfComMsgBox(['Information',WtfGlobal.getLocaleText("acc.field.PleaseProvideAssetDetailsforAssetGroup")+" "+"<b>"+this.Grid.getStore().getAt(i).data['productname']+"</b>"+"."],0);
                        return;
                    }
                    
                    if(quantity != assetDetailArray.length){
                        WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.information'), WtfGlobal.getLocaleText('acc.field.assetQuantityNotMatchInfo') + " " + this.Grid.getStore().getAt(i).data['productname'] + "."], 0);
                        return;
                    }
                    
                    var rate=this.Grid.getStore().getAt(i).data['rate'];
//                        if(!this.isLeaseFixedAsset){
                            if(this.isCustomer){
                                var assetDetailTotalSellingAmount = 0;

                                for (var j = 0; j < assetDetailArray.length; j++) {
                                    if (assetDetailArray[j].sellAmount !== "") {
                                    assetDetailTotalSellingAmount += parseFloat(assetDetailArray[j].sellAmount);
                                    }
                                if (this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId) {
                                    if (assetDetailArray[j].assetId !== "" && assetDetailArray[j].assetId !== undefined) {
                                        if (arr.indexOf(key + assetDetailArray[j].assetId) >= 0) {
                                            isDuplicateAssetPresent = true;
                                            break;
                                        } else {
                                            arr.push(key + assetDetailArray[j].assetId);
                                }
                                    }
                                }
                            }
                            
                                if (isDuplicateAssetPresent) {//If duplicate asset id's are present in Disposal Invoice then we are showing warning message.
                                     break;
                                }
                                var rateIntoQuantityVal = rate*quantity - assetDetailTotalSellingAmount;
                                rateIntoQuantityVal = (rateIntoQuantityVal<0)?(-1)*rateIntoQuantityVal:rateIntoQuantityVal;

                                if(rateIntoQuantityVal > Wtf.decimalLimiterValue){// due to java script rounding off problem
                                    WtfComMsgBox(['Information',WtfGlobal.getLocaleText("acc.field.RateenteredisnotequaltoAssetDetailstotalSellAmountvalueforAssetGroup")+'<b>'+this.Grid.getStore().getAt(i).data['productname']+'</b>'],0);
                                    return;
                                }
                            }else{
                                var assetDetailTotalCost = 0;

                                for(var j=0;j<assetDetailArray.length;j++){
                                     if(assetDetailArray[j].costInForeignCurrency!==""){
                                        assetDetailTotalCost+=parseFloat(assetDetailArray[j].costInForeignCurrency);
                                      }
                                }


                                var rateQuantityVal = rate*quantity - assetDetailTotalCost;
                                rateQuantityVal = (rateQuantityVal<0)?(-1)*rateQuantityVal:rateQuantityVal;

                                if(rateQuantityVal > Wtf.decimalLimiterValue){// due to java script rounding off problem
                                    WtfComMsgBox(['Information','Rate entered is not equal to Asset Details total Cost value  for Asset Group <b>'+this.Grid.getStore().getAt(i).data['productname']+'</b>'],0);
                                    return;
                                }
                            }
//                        }
                    
                }
            }
            
                if (isDuplicateAssetPresent) {
                   WtfComMsgBox(["Warning", "Duplicate Asset ID's are given for same Asset Group."], 2);
                    return;
            }
            }
            
            
            if(!this.isCustBill && (Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel && ((Wtf.account.companyAccountPref.withinvupdate && (!this.isInvoice && !this.isOrder)) || (!Wtf.account.companyAccountPref.withinvupdate && !this.isOrder)))){
                
                var validstore=WtfGlobal.isValidInventoryInfo(this.Grid.getStore(),'invstore');
                if(!validstore){
                    WtfComMsgBox(["Warning","Please select valid inventory store."], 2);
                    return;
                }
                
                var validloc=WtfGlobal.isValidInventoryInfo(this.Grid.getStore(),'invlocation');
                if(!validloc){
                    WtfComMsgBox(["Warning","Please select valid inventory location."], 2);
                    return;
                }
            }
            
            var count=this.Grid.getStore().getCount();
            if(count<=1){
                WtfComMsgBox(33, 2);
                return;
            }                        
            if(this.getDiscount()>this.Grid.calSubtotal()){
                WtfComMsgBox(12, 2);
                return;
            }
            // ERM-616 Below if condition is for checking 'Allow zero unit price in Lease Module' setting activated or not in system controls.
            if ((!CompanyPreferenceChecks.allowZeroUntiPriceInLeaseModule() && this.isLeaseFixedAsset) || this.isFixedAsset) { 
                if(this.Grid.calSubtotal()<=0){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.TotalamountshouldbegreaterthanZero")], 2);
                    return;
                }
            }
//            var datediff=new Date(this.billDate.getValue()).getElapsed(this.DueDate.getValue());
//            if(datediff==0)
//                  incash=true;
//              else
                  incash=this.cash;
            var rec=this.NorthForm.getForm().getValues();
            rec.GSTApplicable=this.isGST;
            rec['termsincludegst'] = this.termsincludegst;
            if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.isExciseApplicable){
                var indiaExcise=this.exciseFormPanel.getForm().getValues();
                if(!Wtf.isEmpty(indiaExcise)){
                    if(!Wtf.isEmpty(this.record)){
                        indiaExcise.id= !Wtf.isEmpty(this.record.data.assetExciseid)?this.record.data.assetExciseid:"";
                    }
                    indiaExcise.supplierState=!Wtf.isEmpty(this.state.getValue())?this.state.getValue():"";
                    indiaExcise.InvoiceDateManuFacture=!Wtf.isEmpty(this.InvoiceDateManuFacture.getValue())?WtfGlobal.convertToGenericDate(this.InvoiceDateManuFacture.getValue()):"";
                    rec.countryid=Wtf.account.companyAccountPref.countryid;
                    rec.indiaExcise=Wtf.encode(indiaExcise);
                }
            }
            if(rec.vendor==undefined&&this.linkIDSFlag!=undefined&&this.linkIDSFlag){
                rec.vendor=this.Name.getValue();
            }
            
            if (rec.vendor==undefined && !this.isCustomer && (this.isInvoice || (this.quotation && !this.isCustomer && this.isFixedAsset) || (this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId)) && this.isEdit) { // In case of edition of purchase invoice some times vendor is being undefined
                rec.vendor=this.Name.getValue();
            }
            
            if(rec.customer==undefined&&this.linkIDSFlag!=undefined&&this.linkIDSFlag){
                rec.customer=this.Name.getValue();
            }
            
            this.isGenerateReceipt = this.generateReceipt.getValue();
            this.isAutoCreateDO = this.autoGenerateDO.getValue();
            rec.islockQuantity =this.lockQuantity.getValue();
            rec.billdate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
            if(this.cash){
                   var index = this.termds.find('termdays','-1');
                    if(index != -1){
                        var storerec=this.termds.getAt(index);                        
                       this.termid=storerec.get("termid");
                    }          
            } else {
                 this.updateDueDate("","",true);
            }                      
            rec.termid=this.termid;
            this.ajxurl = ""; 
              var leaselectValue=this.PO.getValue();
        if(leaselectValue!=undefined&& leaselectValue!=""){
            var poindex=this.POStore.findBy( function(rec){
                var parentname=rec.data['billid'];
                if(parentname==leaselectValue)
                    return true;
                else
                    return false;
            })
            if(poindex>=0) {
                var leaseRec= this.POStore.getAt(poindex);
                if(leaseRec.data.contractstatus==2){
                    WtfComMsgBox(114, 2);
                    return false;
                } 
            }
                
        }
            if(this.businessPerson=="Customer"){
    //(this.isOrder?(this.isCustBill?51:41):(this.isCustBill?13:11))
                    if(this.quotation)
                            this.ajxurl = "ACCSalesOrder/saveQuotation.do";
                    else
                            this.ajxurl = "ACC" + (this.isOrder?(this.isCustBill?"SalesOrder/saveBillingSalesOrder":"SalesOrder/saveSalesOrder"):(this.isCustBill?"Invoice/saveBillingInvoice":"Invoice/saveInvoice")) + ".do";
        }else if(this.businessPerson=="Vendor"){
                if(this.quotation){
                    this.ajxurl = "ACCPurchaseOrder/saveQuotation.do";
                } else {
                    this.ajxurl = "ACC"+ (this.isOrder?(this.isCustBill?"PurchaseOrder/saveBillingPurchaseOrder":"PurchaseOrder/savePurchaseOrder"):(this.isCustBill?"GoodsReceipt/saveBillingGoodsReceipt":"GoodsReceipt/saveGoodsReceipt")) +".do";
                }
                
            }
            var currencychange=this.Currency.getValue()!=WtfGlobal.getCurrencyID()&&this.Currency.getValue()!=""&&!this.isOrder;
            var msg=currencychange?"Currency rate you have applied cannot be changed again. ":"";           
            var detail = this.Grid.getProductDetails();
            var validLineItem = this.Grid.checkDetails(this.Grid);
            if (validLineItem != "" && validLineItem != undefined) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), (WtfGlobal.getLocaleText("acc.msgbox.lineitem") + validLineItem)], 2);            
                return;
            }
            if(detail == undefined || detail == "[]"){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg12")],2);   //"Product(s) details are not valid."
                return;
                }
           var prodLength=this.Grid.getStore().data.items.length;
            for(var i=0;i<prodLength-1;i++)
           { 
            var isLockQuantity=this.lockQuantity.getValue();   
            var prodID=this.Grid.getStore().getAt(i).data['productid'];
//            var prorec=this.Grid.productComboStore.getAt(this.Grid.productComboStore.find('productid',prodID));
            var prorec = (WtfGlobal.searchRecord(this.Grid.productComboStore,prodID,'productid')!=null)?WtfGlobal.searchRecord(this.Grid.productComboStore,prodID,'productid'):WtfGlobal.searchRecord(this.Grid.getStore(),prodID,'productid');
            if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.account.companyAccountPref.islocationcompulsory || Wtf.account.companyAccountPref.iswarehousecompulsory || Wtf.account.companyAccountPref.isRowCompulsory || Wtf.account.companyAccountPref.isRackCompulsory || Wtf.account.companyAccountPref.isBinCompulsory){ //if company level option is on then only check batch and serial details
                if(prorec.data.isBatchForProduct || prorec.data.isSerialForProduct || prorec.data.isLocationForProduct || prorec.data.isWarehouseForProduct || prorec.data.isRowForProduct || prorec.data.isRackForProduct  || prorec.data.isBinForProduct){ 
                    if(prorec.data.type!='Service' && !prorec.get('isAsset') && !this.isOrder && !this.quotation && !this.isInvoice){
                        var batchDetail= this.Grid.getStore().getAt(i).data['batchdetails'];
                        if(batchDetail == undefined || batchDetail == ""){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.bsdetail")],2);   //Batch and serial no details are not valid.
                            return;
                        }
                    }
//                    else if(this.isOrder && isLockQuantity){
//                        var batchDetail= this.Grid.getStore().getAt(i).data['batchdetails'];
//                        if(batchDetail == undefined || batchDetail == ""){
//                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.bsdetail")],2);   //Batch and serial no details are not valid.
//                            return;
//                        }
//                    }
                }
            }
         }  
        if(Wtf.account.companyAccountPref.memo== true && (rec.memo==""))    //memo related setting wether option is true
            {
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText({
                    key:"acc.common.memoempty",
                    params:[Wtf.account.companyAccountPref.descriptionType]
                }),function(btn){
                    if(btn!="yes") {
                        return;
                    }
                    this.checkMemo(rec,detail,incash)                              
                },this);  
            }else {
                this.checkMemo(rec,detail,incash);     
            }
            
        }else{
            WtfComMsgBox(2, 2);
        }
    }, 
     update: function() {
        this.mailFlag = true;
        var incash = false;
        var rec = this.NorthForm.getForm().getValues();
        rec['termsincludegst'] = this.termsincludegst;
        Wtf.MessageBox.confirm(this.isDraft ? WtfGlobal.getLocaleText("acc.common.saveasdraft") : WtfGlobal.getLocaleText("acc.common.savdat"), this.isDraft ? WtfGlobal.getLocaleText("acc.invoice.msg14") : WtfGlobal.getLocaleText("acc.invoice.msg7"), function(btn) {
            if (btn != "yes") {
                return;
            }
            this.ajxurl = "";
            if (this.businessPerson == "Customer") {
                if (this.quotation)
                    this.ajxurl = "ACCSalesOrder/updateLinkedQuotation.do";
                else
                    this.ajxurl = "ACC" + (this.isOrder ? ("SalesOrder/updateLinkedSalesOrder") : ("Invoice/updateLinkedInvoice")) + ".do";
            } else if (this.businessPerson == "Vendor") {
                if (this.quotation) {
                    this.ajxurl = "ACCPurchaseOrder/updateLinkedQuotation.do";
                } else {
                    this.ajxurl = "ACC" + (this.isOrder ? ("PurchaseOrder/updateLinkedPurchaseOrder") : ("GoodsReceipt/updateLinkedGoodsReceipt")) + ".do";
                }

            }
            var detail = this.Grid.getProductDetails();
            this.msg = WtfComMsgBox(27, 4, true);
            rec.detail = detail;
            rec.billdate = WtfGlobal.convertToGenericDate(this.billDate.getValue());
            rec.costcenter = this.CostCenter.getValue();
            rec.validdate = WtfGlobal.convertToGenericDate(this.validTillDate.getValue());
            rec.shipdate = WtfGlobal.convertToGenericDate(this.shipDate.getValue());
            var custFieldArr = this.tagsFieldset.createFieldValuesArray();
            rec.posttext = this.postText;
            if (custFieldArr.length > 0) {
                rec.customfield = JSON.stringify(custFieldArr);
            }
            rec.invoiceid = this.billid;
            rec.mode = (this.isOrder ? (this.isCustBill ? 51 : 41) : (this.isCustBill ? 13 : 11));
            rec.incash = incash;
            rec.isLinkedTransaction = this.isLinkedTransaction;
            rec.isFixedAsset=this.isFixedAsset;
            rec.isLeaseFixedAsset=this.isLeaseFixedAsset;
            var isCopy = this.copyInv;
            var isEdit = this.isEdit;
            rec = WtfGlobal.getAddressRecordsForSave(rec, this.record, this.linkRecord, this.currentAddressDetailrec, this.isCustomer, this.singleLink, isEdit, isCopy, false, false, this.isQuotationFromPR);
            WtfGlobal.setAjaxTimeOut();
            Wtf.Ajax.requestEx({
                url: this.ajxurl,
                params: rec
            }, this, this.genSuccessResponse, this.genFailureResponse);
        }, this);

    },
    checkMemo:function(rec,detail,incash){
               if(this.businessPerson=="Vendor" && !this.isOrder){//Only for cash purchase and vendor invoice
                    Wtf.Ajax.requestEx({
                        url:"ACCReports/getAccountsExceedingBudget.do",
                        params: {
                            detail: detail,
                            stdate: this.getDates(true).format("M d, Y h:i:s A"),
                            enddate: this.getDates(false).format("M d, Y h:i:s A"),
                            isExpenseInv: this.isExpenseInv,
                            billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue())
                        }
                    },this,function(response){
                        if(response.data && response.data.length > 0){
                            var accMsg = "Following Accounts are exceeding their Monthly Budget Limit.<br><br><center>";
                            var budgetMsg = "";
                            for(var i=0; i< response.data.length; i++){
                                var recTemp = response.data[i];
                                if (!this.isCustBill)
                                    budgetMsg = (recTemp.productName == "" ? "" : "<b>Product: </b>" + recTemp.productName + ",") + " <b>Account: </b>" + recTemp.accountName + ", <b>Balance: </b>" + recTemp.accountBalance + ", <b>Budget: </b>" + recTemp.accountBudget;
                                else
                                    budgetMsg = (recTemp.productName == "" ? "" : "<b>Job Description: </b>" + recTemp.productName + ",") + " <b>Account: </b>" + recTemp.accountName + ", <b>Balance: </b>" + recTemp.accountBalance + ", <b>Budget: </b>" + recTemp.accountBudget;
                                accMsg += budgetMsg + "<br>";
                            }

                            accMsg += "<br>Do you wish to proceed?</center>";
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),accMsg,function(btn){
                                if(btn!="yes") {return;}
                                this.checkLimit(rec,detail,incash);
                            },this);
                        }else{
                            this.checkLimit(rec,detail,incash);
                        }
                    },function(response){
                        this.checkLimit(rec,detail,incash);
                    });

                }else{
                    this.checkLimit(rec,detail,incash);
                } 
             
   },
    
    saveTemplate : function(){
        if(this.createTransactionAlso){
            this.transactionType = 1;
            this.save();
            Wtf.getCmp("emailbut" + this.id).show();
            Wtf.getCmp("exportpdf" + this.id).show();
            Wtf.getCmp("printSingleRecord" + this.id).show();
        }else{
            if(this.Name.getValue() == ''){
                    var fieldLabel = this.isCustomer?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven") ; //this.businessPerson+"*",
                    WtfComMsgBox(["Warning","Please select "+fieldLabel], 2);
                    return;
            }
            if(this.isCustBill){
                for(var datacount=0;datacount<this.Grid.getStore().getCount();datacount++){
                    var creditoracc=this.Grid.getStore().getAt(datacount).data['creditoraccount'];                    
                    if(creditoracc==undefined||creditoracc==""){
                        if(this.Grid.getStore().getAt(datacount).data['productdetail'].length>0){
                            var account=(this.isCustomer)?"Credit account":"Debit account";
                            WtfComMsgBox(["Warning","Please select "+account], 2);
                            return;
                        } 
                    }            
                }
                var count=this.Grid.getStore().getCount();
                if(count<=1){
                    WtfComMsgBox(33, 2);
                    return;
                }  
             }
            this.transactionType = 2;
            var rec=this.NorthForm.getForm().getValues();
            rec.billdate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
            var detail = this.Grid.getProductDetails();
            var incash=this.cash;
            this.ajxurl = "";
            if(this.businessPerson=="Customer"){
                if(this.quotation)
                    this.ajxurl = "ACCSalesOrder/saveQuotation.do";
                else
                    this.ajxurl = "ACC" + (this.isOrder?(this.isCustBill?"SalesOrder/saveBillingSalesOrder":"SalesOrder/saveSalesOrder"):(this.isCustBill?"Invoice/saveBillingInvoice":"Invoice/saveInvoice")) + ".do";
            }else if(this.businessPerson=="Vendor"){
                if(this.quotation){
                    this.ajxurl = "ACCPurchaseOrder/saveQuotation.do";
                } else {
                    this.ajxurl = "ACC"+ (this.isOrder?(this.isCustBill?"PurchaseOrder/saveBillingPurchaseOrder":"PurchaseOrder/savePurchaseOrder"):(this.isCustBill?"GoodsReceipt/saveBillingGoodsReceipt":"GoodsReceipt/saveGoodsReceipt")) +".do";
                }
                
            }
            this.showConfirmAndSave(rec,detail,incash);
       }
    },
    
    
    checkLimit:function(rec,detail,incash){
        if(!this.quotation && !this.isOrder && !this.cash){
            if(rec!=null&&rec!=undefined&&this.calTotalAmount()!=null)
            {
                if(rec.customer!=null&&rec.customer!="")
                    {
                        rec.customerid=rec.customer;
                    }
                rec.totalSUM=this.calTotalAmount()+this.caltax();
                  var custFieldArr=[];
            if(this.moduleid == Wtf.LEASE_INVOICE_MODULEID 
                        || this.moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId){
                custFieldArr=this.tagsFieldset.createFieldValuesArray();
            }; 
            if (custFieldArr != undefined && custFieldArr.length > 0)
                rec.customfield = JSON.stringify(custFieldArr);
                Wtf.Ajax.requestEx({
                    url:"ACC"+this.businessPerson+"CMN/get"+this.businessPerson+"Exceeding"+(this.businessPerson=="Vendor"?"Debit":"Credit")+"Limit.do",
                    params:rec                                                                                                                                            
                },this,function(response){
                    if(response.data && response.data.length > 0){
                        var msg = (this.businessPerson=="Vendor"?WtfGlobal.getLocaleText("acc.cust.debitLimit"):WtfGlobal.getLocaleText("acc.cust.creditLimit"))+" "+WtfGlobal.getLocaleText("acc.field.forthis")+" "+(this.businessPerson=="Vendor"?WtfGlobal.getLocaleText("acc.agedPay.ven"):WtfGlobal.getLocaleText("acc.agedPay.cus"))+" "+WtfGlobal.getLocaleText("acc.field.hasreached")+"<br><br><center>";
                        var limitMsg = "";
                        for(var i=0; i< response.data.length; i++){
                            var recTemp = response.data[i];
                            limitMsg = (recTemp.name == "" ? "" : "<b>"+(this.businessPerson=="Vendor"? WtfGlobal.getLocaleText("acc.agedPay.ven") : WtfGlobal.getLocaleText("acc.agedPay.cus"))+": </b>" + recTemp.name + ", ") +"<b>"+WtfGlobal.getLocaleText("acc.field.AmountDue1")+"</b>" + WtfGlobal.conventInDecimalWithoutSymbol(recTemp.amountDue) + ", <b>"+(this.businessPerson=="Vendor"?WtfGlobal.getLocaleText("acc.cust.debitLimit"):WtfGlobal.getLocaleText("acc.cust.creditLimit"))+": </b>" + recTemp.limit;
                            msg += limitMsg + "<br>";
                        }
                        var limitControlType="";
                        if(this.isCustomer){
                            limitControlType=Wtf.account.companyAccountPref.custcreditlimit;
                        } else {
                            limitControlType=Wtf.account.companyAccountPref.vendorcreditcontrol;
                        }
                        if(limitControlType == Wtf.controlCases.BLOCK){//block
                            msg += "<br>You cannot proceed.</center>";
                            WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.information'),msg],3);
                            return;
                        }else if(limitControlType == Wtf.controlCases.WARN){//warn
                           
                            msg += "<br>"+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+"</center>";
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),msg,function(btn){
                                if(btn!="yes") {
                                    return;
                                }
                                this.showConfirmAndSave(rec,detail,incash);
                            },this);
                        }else{//ignore
                            this.showConfirmAndSave(rec,detail,incash);
                        }

                    }else{
                            this.showConfirmAndSave(rec,detail,incash);
                        }
                },function(response){
                        this.showConfirmAndSave(rec,detail,incash);
                });                
            }else{
                    this.showConfirmAndSave(rec,detail,incash);
                }
        }else{
            if(this.moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId || this.moduleid == Wtf.Acc_Lease_Quotation){
                custFieldArr=this.tagsFieldset.createFieldValuesArray();
            }; 
            if (custFieldArr != undefined && custFieldArr.length > 0)
                rec.customfield = JSON.stringify(custFieldArr);
            this.showConfirmAndSave(rec,detail,incash);
        }
    },
    
    showConfirmAndSave: function(rec,detail,incash){
        
        if(this.EditisAutoCreateDO && (this.isFixedAsset || this.isLeaseFixedAsset)){
            if(this.isCustomer)
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.invoice.msg18") ], 3);
            else
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.invoice.msg17") ], 3);
            return;
        }
        
        var promptmessage = this.EditisAutoCreateDO ?  WtfGlobal.getLocaleText("acc.invoice.msg16"):WtfGlobal.getLocaleText("acc.invoice.msg7");
        if (Wtf.Countryid == Wtf.CountryID.MALAYSIA && WtfGlobal.isNonZeroRatedTaxCodeUsedInTransaction(this)) {
            promptmessage = this.EditisAutoCreateDO ? WtfGlobal.getLocaleText("acc.customerInvoice.nonZeroTaxcode.alert") : WtfGlobal.getLocaleText("acc.tax.nonZeroTaxcode.alert");
        }
        
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),promptmessage,function(btn){
                if(btn!="yes") {return;}
                rec.taxid=this.Tax.getValue();
                /*
                 * ERP-40242 : In linking case, deactivated tax not shown.Hence, empty taxid set in record.          
                 */
                if (rec.taxid != '' && (this.fromPO != undefined && this.fromPO.getValue())) {
                    var taxActivatedRec = WtfGlobal.searchRecord(this.Grid.taxStore, this.Tax.getValue(), "prtaxid");
                    if (taxActivatedRec == null || taxActivatedRec == undefined || taxActivatedRec == "") {
                        rec.taxid = "";
                   }
               }
                rec.isfavourite = false;
                if(!this.copyInv){
                    if((this.record && this.record !== undefined) && (this.record.get('isfavourite') !== null || this.record.get('isfavourite') !== undefined)){
                        rec.isfavourite = this.record.get('isfavourite');
                    }
                }
                rec.taxamount=this.caltax();
                if(this.isExpenseInv){
                    rec.expensedetail=detail;
                    rec.isExpenseInv=this.isExpenseInv;
                }
                else
                    rec.detail=detail;
                
                var custFieldArr=[];
                if((this.moduleid == Wtf.Acc_Lease_Order && this.isLeaseFixedAsset) || this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId ){
                    custFieldArr=this.tagsFieldset.createFieldValuesArray();
                }
                
                if(this.copyInv && this.record && this.record.data.contract){
                     rec.contractId=this.record.data.contract;
                }
                
                this.msg= WtfComMsgBox(27,4,true);
                rec.subTotal=this.Grid.calSubtotal()
                this.applyCurrencySymbol();
                rec.perdiscount=this.perDiscount.getValue();
                rec.isOpeningBalanceOrder=this.isOpeningBalanceOrder;
                rec.currencyid=this.Currency.getValue();
                rec.externalcurrencyrate=this.externalcurrencyrate;
                rec.discount=this.Discount.getValue();
                rec.posttext=this.postText;
                rec.istemplate=this.transactionType;
                rec.moduletempname=this.isTemplate;
                rec.templatename=this.moduleTemplateName.getValue();
                rec.isFixedAsset=this.isFixedAsset;
                rec.isLeaseFixedAsset=this.isLeaseFixedAsset;
                rec.transType=this.moduleid;
//                rec.vendorinvoice = this.vendorInvoice!=null?this.vendorInvoice.getValue():'';
                if (custFieldArr.length > 0)
                    rec.customfield = JSON.stringify(custFieldArr);
                rec.invoicetermsmap = this.getInvoiceTermDetails();
                if(this.Grid.deleteStore!=undefined && this.Grid.deleteStore.data.length>0)
                    rec.deletedData=this.getJSONArray(this.Grid.deleteStore,false,0);
                rec.number=this.Number.getValue();
                rec.isEdit=this.isEdit;
                rec.linkNumber=(this.PO != undefined && this.PO.getValue()!="")?this.PO.getValue():"";
                if(this.isLeaseFixedAsset && (this.isOrder || this.quotation)){
                    if(this.fromLinkCombo && this.fromLinkCombo.getValue() == 1){
                        rec.isLinkedFromReplacementNumber=true;
                        rec.replacementId=this.PO.getValue();
                    }
                    if(this.isOrder && this.fromLinkCombo && this.fromLinkCombo.getValue() == 0){// if SO linked with CQ
                        rec.isLinkedFromCustomerQuotation=true;
                    }
                    
                }
            if (this.isIndiaGST) {
                rec.CustomerVendorTypeId = this.CustomerVendorTypeId;
                rec.GSTINRegistrationTypeId = this.GSTINRegistrationTypeId;
                rec.gstin = this.gstin;
                if (this.isEdit && !this.copyInv) {
                    rec.gstdochistoryid = this.gstdochistoryid;
                }
            }
                rec.fromLinkCombo=this.fromLinkCombo.getRawValue();
                rec.duedate=WtfGlobal.convertToGenericDate(this.DueDate.getValue());
                rec.billdate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
                rec.shipdate=WtfGlobal.convertToGenericDate(this.shipDate.getValue());
                rec.validdate=WtfGlobal.convertToGenericDate(this.validTillDate.getValue());
                rec.invoiceid=this.copyInv?"":this.billid;
                rec.doid=this.DeliveryOrderid;
                rec.mode=(this.isOrder?(this.isCustBill?51:41):(this.isCustBill?13:11));
                rec.incash=incash;
                rec.includingGST = (this.includingGST)? this.includingGST.getValue() : false;
                rec.partialinv = (this.partialInvoiceCmb)? this.partialInvoiceCmb.getValue() : false;
                this.totalAmount = rec.subTotal + rec.taxamount - this.getDiscount();
                rec.includeprotax = (this.includeProTax)? this.includeProTax.getValue() : false;
                rec.landedInvoiceNumber = this.invoiceList.getValue();
                rec.customer = this.Name.getValue();
                rec.isApplyTaxToTerms = (this.includeProTax && this.includeProTax.getValue()==true && this.applyTaxToTermsChk.getValue());
                if(this.autoGenerateDO.getValue() ||  this.EditisAutoCreateDO){
                    var seqFormatRecDo=WtfGlobal.searchRecord(this.sequenceFormatStoreDo, this.sequenceFormatComboboxDo.getValue(), 'id');
                    rec.seqformat_oldflagDo=seqFormatRecDo!=null?seqFormatRecDo.get('oldflag'):false;
                    rec.numberDo = this.no.getValue();
                    rec.sequenceformatDo=this.sequenceFormatComboboxDo.getValue();
                }
                var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                rec.sequenceformat=this.sequenceFormatCombobox.getValue();
                rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):false;
            var isCopy = this.copyInv;
            var isEdit = this.isEdit;
            if (this.isVenOrCustSelect) {
                isEdit = false;
                isCopy = false;
            }
                rec=WtfGlobal.getAddressRecordsForSave(rec,this.record,this.linkRecord,this.currentAddressDetailrec,this.isCustomer,this.singleLink,isEdit,isCopy,false,false,this.isQuotationFromPR);
                if(this.isAutoCreateDO ||  this.EditisAutoCreateDO){
                    rec.isAutoCreateDO = this.EditisAutoCreateDO ? this.EditisAutoCreateDO : this.isAutoCreateDO;
                    rec.fromLinkComboAutoDO =this.isCustomer ? "Customer Invoice" : "Vendor Invoice";
                }
                if(this.capitalGoodsAcquired){
                    rec.isCapitalGoodsAcquired = this.capitalGoodsAcquired.getValue();
                }
                
            /*-----If Pending document is edited----------  */
            if (this.pendingapprovalTransaction) {//sent from Asset VQ & Asset PO
                rec.isEditedPendingDocument = true;
            }
                Wtf.Ajax.requestEx({
                    url:this.ajxurl,
//                    url: Wtf.req.account+this.businessPerson+'Manager.jsp',
                    params: rec
                },this,this.genSuccessResponse,this.genFailureResponse);

            },this);
    },
    
    getDates:function(start){
        var d=new Date();
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;

        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    
getFinancialYRStartDatesMinOne:function(start){
    var d=Wtf.serverDate;
    //        if(this.statementType=='BalanceSheet'&&start)
    //             return new Date('January 1, 1970 00:00:00 AM');
    var monthDateStr=d.format('M d');
    if(Wtf.account.companyAccountPref.bbfrom)
        monthDateStr=Wtf.account.companyAccountPref.bbfrom.format('M d');
    var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
    if(d<fd)
        fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
    if(start)
        return fd.add(Date.YEAR, 0).add(Date.DAY, -1);
    return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
},

    loadTransStore : function(productid){
        if(this.Name.getValue() != ""){
            var customer= (this.businessPerson=="Vendor")? "" : this.Name.getValue();
            var vendor= (this.businessPerson=="Vendor")? this.Name.getValue() : "" ;
            if((productid == undefined || productid == "") && this.Grid.getStore().getCount() > 0){
                productid = this.Grid.getStore().getAt(0).get("productid");
            }
            this.lastTransPanel.Store.on('load', function(){
                Wtf.getCmp('south' + this.id).doLayout();
            }, this);
            if(productid) {
                this.lastTransPanel.productid = productid;
                this.lastTransPanel.Store.load({
                    params:{
                        start:0,
                        limit:5, 
                        prodfiltercustid:customer,
                        prodfilterventid:vendor,
                        productid : productid
                    }
                });
            }
        }
    },
    
    removeTransStore : function(){
        this.lastTransPanel.Store.removeAll();
        this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body,{productname:"&nbsp;&nbsp;&nbsp;&nbsp;",productid:0,qty:0,soqty:0,poqty:0});
    },
    
    updateData:function(){
        var customer= this.Name.getValue();
       if(Wtf.getCmp("showaddress" + this.id)){
          Wtf.getCmp("showaddress" + this.id).enable(); 
       } 
        var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
         var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";
            if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null ){
                this.isTaxable.setValue(true);
                this.Tax.enable();
                this.Tax.setValue(actualTaxId);
            } else {
                this.isTaxable.setValue(false);
                this.Tax.setValue('');
                this.Tax.disable();
            }
        if(this.Grid) {
            this.Grid.affecteduser = this.Name.getValue();
        }
        
        this.loadTransStore();
        if(!(this.isCustBill||this.isExpenseInv|| this.isEdit)) {
            if(!(this.fromPO && this.fromPO.getValue())){
                var val = this.billDate.getValue();
//                this.Grid.loadPriceStoreOnly(val,this.Grid.priceStore, customer);
            }
            
        }
        Wtf.Ajax.requestEx({
            url:"ACC"+this.businessPerson+"CMN/getCurrencyInfo.do",
//            url:Wtf.req.account+this.businessPerson+'Manager.jsp',
            params:{
                mode:4,
                customerid:customer,
                isBilling : this.isCustBill
            }
        }, this,this.setCurrencyInfo);       
        if(this.fromPO){
            this.fromPO.enable();
        }
    },
    
    setCurrencyInfo:function(response){
        if(response.success){
            this.custdatechange=true;
            if(!(this.isEdit || this.copyInv )){
            this.externalcurrencyrate=0;
            this.Currency.setValue(response.currencyid);
            this.currencyid=response.currencyid;
            this.symbol = response.currencysymbol;
            }
            var taxid = response.taxid
            var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
            var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";
                if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null ){
                this.isTaxable.setValue(true);
                this.Tax.enable();
                this.Tax.setValue(actualTaxId);
            } else {
                this.isTaxable.setValue(false);
                this.Tax.setValue('');
                this.Tax.disable();
            }
            this.custChange=true;
            if(!(this.isEdit || this.copyInv)){ 
            this.changeCurrencyStore();
//            if(this.fromPO)					// Currency id issue 20018
//            	this.currencyStore.load();                      // No need to load Currency Store
            }            
            this.amountdue=0;
            this.amountdue=response.amountdue;
            this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:WtfGlobal.currencyRenderer(0),discount:WtfGlobal.currencyRenderer(0),totalamount:WtfGlobal.currencyRenderer(0),tax:WtfGlobal.currencyRenderer(0),aftertaxamt:WtfGlobal.currencyRenderer(0),totalAmtInBase:WtfGlobal.currencyRenderer(0),termtotal:WtfGlobal.currencyRenderer(0),amountbeforetax:WtfGlobal.currencyRenderer(0),amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,this.symbol)})
        }
    },
    getTerm:function(val1,val2){
            if((this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) && !this.cash) {
                val1=new Date(this.record.data.shipdate);
            } else {
                val1=new Date(this.record.data.date);
            }
        val2=new Date(this.record.data.duedate);
        var msPerDay = 24 * 60 * 60 * 1000
        var termdays = Math.floor((val2-val1)/ msPerDay) ;
        var FIND =termdays;
        var index=this.termds.findBy( function(rec){
             var parentname=rec.data.termdays;
            if(parentname==FIND)
                return true;
             else
                return false
            })
            if(index>=0){
                var  rec=this.termds.getAt(index)
                this.Term.setValue(rec.data.termid);
            } else{                
                termdays = Math.ceil((val2-val1)/ msPerDay) ;
                FIND =termdays;
                index=this.termds.findBy( function(rec){
                    var parentname=rec.data.termdays;
                    if(parentname==FIND)
                        return true;
                    else
                        return false
                });
                if(index>=0){
                    rec=this.termds.getAt(index)
                    this.Term.setValue(rec.data.termid);
                }
                
            }
    },
    getCreditTo:function(val){
        var index=this.allAccountStore.findBy( function(rec){
             var name=rec.data.accid;
            if(name==val)
                return true;
             else
                return false
            },this)
            if(index>=0)
                this.creditTo.setValue(val);
    },
    
    productComboStoreOnBeforeLoad:function(){
        WtfGlobal.setAjaxTimeOut();
    },
    
    productComboStoreOnLoad:function(){
        WtfGlobal.resetAjaxTimeOut();
    },
    
    productComboStoreOnLoadException:function(){
        WtfGlobal.resetAjaxTimeOut();
    },
    
    updateDueDate:function(a,val,isSave){
        var term=null;
        var rec=null;
        if (this.Term != undefined && this.Term.getValue() != "" && isNaN(this.Term.getValue()) == false) {
            term = new Date(this.billDate.getValue()).add(Date.DAY, this.Term.getValue());
        }
        
        if (this.Term != undefined && this.Term.getValue() != "" && this.Term.getValue() != null && this.Term.getValue() != undefined) {
            rec = this.Term.store.getAt(this.Term.store.find('termid', this.Term.getValue()));
            if (rec != null && rec != undefined) {
                term = new Date(this.billDate.getValue()).add(Date.DAY, rec.data.termdays);
            }
        } else {
            term = this.billDate.getValue();
        }
        
        if(Wtf.account.companyAccountPref.shipDateConfiguration) {
            if((this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId ) && !this.cash) {
                if(this.shipDate.getValue() != "") {
                    if(this.Term.getValue()!="" && isNaN(this.Term.getValue()) == false) {
                        term = new Date(this.shipDate.getValue()).add(Date.DAY, this.Term.getValue());
                    } else {
                        term = this.shipDate.getValue();
                    }
                } else {
                    term = null;
                }
            }
        }
        
        if(term != null) {
            if(!(isSave != undefined && isSave != "" && isSave==true) ){
                this.NorthForm.getForm().setValues({duedate:term});
            }
        }
        if(this.Grid){
            this.Grid.billDate = this.billDate.getValue()
        }
        rec = this.Term.store.getAt(this.Term.store.find('termid',this.Term.getValue()));
        if(rec != null && rec != undefined){
            this.termid=rec.data.termid;
        }
    },

  genSuccessResponse:function(response, request){
        var editinv=this.isEdit;
        var notInvDuplicateExe=true
        if(this.copyInv){
            editinv=false;
        }
        if(response.isDuplicateNoExe!=undefined){
            notInvDuplicateExe=response.isDuplicateNoExe;
        }
        this.RecordID=response.SOID!=undefined?response.SOID : response.invoiceid;
        if(!response.isAccountingExe){
        this.recurringInvoice.enable();
    }
        if(response.success && this.GENERATE_PO) {
            if(response.pendingapproval == 1) {
                WtfComMsgBox([this.titlel,"Purchase Order successfully generated but pending for Approval. PO Number : " + this.Number.getValue()],response.success*2+1);
            } else {
                WtfComMsgBox([this.titlel,"Purchase Order successfully generated. PO Number : " + this.Number.getValue()],response.success*2+1);
            }
            
        }if(response.success && this.GENERATE_SO) {
              if(response.pendingapproval == 1) {
                WtfComMsgBox([this.titlel,"Sales Order successfully generated but pending for Approval. SO Number : " + this.Number.getValue()],response.success*2+1);
            } else {
                WtfComMsgBox([this.titlel,"Sales Order successfully generated. SO Number : " + this.Number.getValue()],response.success*2+1);
            }
            
        } else {
            
        if (this.moduleid == Wtf.LEASE_INVOICE_MODULEID && (editinv?(this.autoGenerateDO.getValue()?false:true):true) && response.isAccountingExe && notInvDuplicateExe) {
            Wtf.MessageBox.hide();
            var label=WtfGlobal.getLocaleText("acc.INV.newdinvoiceno");
            
            this.newdowin = new Wtf.Window({
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
                    html: (response.msg.length>60)?response.msg:"<br>"+response.msg,
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
                                Wtf.getCmp("invoiceNo"+this.heplmodeid+this.id).setValue(this.newdono.getValue());
                                this.save();
                                this.newdowin.close();
                            }
                        },
                        scope: this
                    }, {
                        text: WtfGlobal.getLocaleText("acc.common.cancelBtn"), //"Cancel",
                        scope: this,
                        handler: function () {
                            this.newdowin.close();
                        }
                    }]
                })]
            });
            this.newdowin.show();    
        }    
        }
        var rec=this.NorthForm.getForm().getValues();
        this.exportRecord=rec;
        this.exportRecord['billid']=response.billid||response.invoiceid;
        this.exportRecord['billno']=response.billno||response.invoiceNo;
        this.exportRecord['amount']=(this.moduleid==22||this.moduleid==23 || this.moduleid==89)?this.totalAmount:response.amount;      //To get total amount in Fixed Asset Vendor Quotation, added module id check for Vendor Quotation.  
        
        if (this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId) {
            this.exportRecord['fixedAssetInvoice'] = this.isFixedAsset;
        }
        
        if(this.singlePrint){
            this.singlePrint.exportRecord=this.exportRecord;
        }
        if(this.printRecords){
            this.printRecords.exportRecord=this.exportRecord;
        }
        var recurringData=this.NorthForm.getForm().getValues();
        WtfGlobal.onFormSumbitGetDisableFieldValues(this.NorthForm.form.items, recurringData);
        this.data = recurringData;
        //this.data['repeateid']=response.repeatedid;
        this.data['billno']=this.data.number;
        this.data['interval']=response.intervalUnit;
        this.data['intervalType']=response.intervalType;
        this.data['sdate']=response.sdate;
        this.data['nextDate']=response.nextdate;
        this.data['expdate']=response.expdate;
        this.data['NoOfpost']=response.noOfInvPost;
        this.data['noOfInvRemainPost']=response.noOfInvRemainPost;
        if(this.recurringInvoice){
                this.invoiceRec['data'] = this.data;
                this.recurringInvoice.invoiceRec=this.invoiceRec;        
        }
        
        var msgTitle = this.titlel;
        if (response.success && Wtf.isAutoRefershReportonDocumentSave) {
            if (this.moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId && Wtf.getCmp('assetVendorQuotationListEntry') !== undefined) {
                Wtf.getCmp('assetVendorQuotationListEntry').Store.on('load', function () {
                    WtfComMsgBox([msgTitle, response.msg], response.success * 2 + 1);
                }, Wtf.getCmp('assetVendorQuotationListEntry').Store, {single: true})
                Wtf.getCmp('assetVendorQuotationListEntry').loadStoreGrid(); //To reload the grid
            } else if (this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId && Wtf.getCmp("assetPurchaseOrderListEntry") !== undefined) {
                Wtf.getCmp("assetPurchaseOrderListEntry").Store.on('load', function () {
                    WtfComMsgBox([msgTitle, response.msg], response.success * 2 + 1);
                }, Wtf.getCmp("assetPurchaseOrderListEntry").Store, {
                    single: true
                });
            }else if (this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId && Wtf.getCmp("FixedAssetInvoiceListEntry") !== undefined) {
                Wtf.getCmp("FixedAssetInvoiceListEntry").Store.on('load', function () {
                    WtfComMsgBox([msgTitle, response.msg], response.success * 2 + 1);
                }, Wtf.getCmp("FixedAssetInvoiceListEntry").Store, {
                    single: true
                });
            }else if (this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId && Wtf.getCmp("FixedAssetGRListEntry") !== undefined) {
                Wtf.getCmp("FixedAssetGRListEntry").Store.on('load', function () {
                    WtfComMsgBox([msgTitle, response.msg], response.success * 2 + 1);
                }, Wtf.getCmp("FixedAssetGRListEntry").Store, {
                    single: true
                });
            }else{
                WtfComMsgBox([this.titlel, response.msg], response.success * 2 + 1);   
            }
        } else {
            WtfComMsgBox([this.titlel, response.msg], response.success * 2 + 1);
        }
              
         if(response.success){
//             if(!(Optimized_CompanyIds.indexOf(companyid)!= -1)){
               if(!this.isCustBill){
                 Wtf.productStoreSales.reload();
                Wtf.productStore.reload();   //Reload all product information to reflect new quantity, price etc                
               }            	
//             }
            if(this.isGenerateReceipt){
                var mode = "";                
                if(this.businessPerson=="Customer"){
                    if(!this.quotation && !this.isOrder){
                        mode = (this.isCustBill?23:22);
                    }
                }
                var fileName="Cash Sales Payment Recieved "+response.invoiceNo;
                var selRec = "&amount="+this.totalAmount+"&bills="+response.invoiceid+"&customer=Cash&accname="+response.accountName+"&personid="+response.accountid;//+"&address="+recData.address;
                Wtf.get('downloadframe').dom.src = "ACCExportRecord/exportRecords.do?mode="+mode+"&rec="+selRec+"&personid="+response.accountid+"&filename="+fileName+"&filetype=pdf"
            }
            
            if(this.isTemplate){
                this.ownerCt.remove(this);
            }
            
        if(this.mailFlag || this.saveOnlyFlag){
            this.loadUserStoreForInvoice(response, request);
            this.disableComponent();
            this.response = response;
            this.request = request;
            return;
        }
        this.currentAddressDetailrec="";
            this.singleLink = false;
            this.isVenOrCustSelect=false;
            this.lastTransPanel.Store.removeAll();
            this.symbol = WtfGlobal.getCurrencySymbol();
            this.currencyid = WtfGlobal.getCurrencyID();
            this.loadStore();
            this.fromPO.disable();
            this.currencyStore.load();                  
            this.Currency.setValue(WtfGlobal.getCurrencyID()); // Reset to base currency 
            this.externalcurrencyrate=0; //Reset external exchange rate for new Transaction.
            this.isClosable= true;       //Reset Closable flag to avoid unsaved Message.
            Wtf.dirtyStore.product = true;
            this.Grid.linkedFromOtherTransactions=false;
            var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
         var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";
                if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null ){
                this.isTaxable.setValue(true);
                this.Tax.enable();
                this.Tax.setValue(actualTaxId);
            }else{
                this.isTaxable.setValue(false);
                this.Tax.setValue('');
                this.Tax.disable();
            }
            this.postText="";
            var customFieldArray = this.tagsFieldset.customFieldArray;
        if(customFieldArray!=null && customFieldArray!=undefined && customFieldArray!="" ){
            for (var itemcnt = 0; itemcnt < customFieldArray.length; itemcnt++) {
                var fieldId = customFieldArray[itemcnt].id
                if (Wtf.getCmp(fieldId) != undefined && customFieldArray[itemcnt].getXType()!='fieldset') {
                    Wtf.getCmp(fieldId).reset();
                }
            }
        }
        var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray;  //Reset Check List
        if(checkListCheckBoxesArray!=null && checkListCheckBoxesArray!=undefined && checkListCheckBoxesArray!="" ){
            for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
                var checkfieldId = checkListCheckBoxesArray[checkitemcnt].id
                if (Wtf.getCmp(checkfieldId) != undefined) {
                    Wtf.getCmp(checkfieldId).reset();
                }
            } 
        }
        var customDimensionArray = this.tagsFieldset.dimensionFieldArray;  //Reset Custom Dimension
        if(customDimensionArray!=null && customDimensionArray!=undefined && customDimensionArray!="" ){
            for (var itemcnt1 = 0; itemcnt1 < customDimensionArray.length; itemcnt1++) {
                var fieldId1 = customDimensionArray[itemcnt1].id
                if (Wtf.getCmp(fieldId1) != undefined) {
                    Wtf.getCmp(fieldId1).reset();
                }
            } 
        }
            this.fireEvent('update',this);
            this.amountdue=0;
        } else if (response.isTaxDeactivated) {
            WtfComMsgBox([this.label, response.msg], 2);
        }else if(response.msg && response.msg!=""){
                if (this.isEdit && ((this.moduleid == Wtf.Acc_Lease_Order && !response.isAccountingExe) || (this.moduleid == Wtf.Acc_Lease_Quotation))) {
                    WtfComMsgBox([this.label, response.msg], !response.success * 2 + 0);
                } else if ((this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId) && response.isAccountingExe) { //if any accounting exeception throws then we are showing alert message.
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 2);
                } else {
                    WtfComMsgBox([this.label, response.msg], response.success * 2 + 1);
                }
            }
       
    },
    genFailureResponse:function(response){
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    
callEmailWindowFunction : function(response, request){
    
    if(response.pendingApproval){
        var titleMsg = this.getLables();
        WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.information'),titleMsg+' is pending for approval, So you cannot send mail right now.'],3);
        return;
    }
    if(this.CustomStore != null){
//        var rec = this.CustomStore.getAt(0);
         /*
         * ERP-33648-In Mail body wrong invoic number showing
         */
        var rec="";
        if(response.billid!=undefined && response.billid!=''){
            rec = this.CustomStore.getAt(this.CustomStore.find('billid',response.billid));
        }else if(response.invoiceid!=undefined && response.invoiceid!=''){
            rec = this.CustomStore.getAt(this.CustomStore.find('billid',response.invoiceid));
        }
        var label = "";
        if(this.cash){
            if(this.isCustomer){
                label = "Cash Sales Receipt";
                if(rec.data.withoutinventory){
                    callEmailWin("emailwin",rec,label,11,true,false,false,true);
                }else{
                    callEmailWin("emailwin",rec,label,2,true,false,false,true);
                }
            }else{
                label = "Cash Purchase Receipt";
                if(rec.data.withoutinventory){
                    callEmailWin("emailwin",rec,label,15,false,false,false,true);
                }else{
                    callEmailWin("emailwin",rec,label,6,false,false,false,true);
                }
            }
        } else if(this.isOrder && !this.quotation){
            if(this.isCustomer){
                label = "Sales Order";
                if(rec.data.withoutinventory){
                    callEmailWin("emailwin",rec,label,17,true,false,false,false,true);
                }else{
                    callEmailWin("emailwin",rec,label,1,true,false,false,false,true);
                }
            }else{
                
                label =this.isFixedAsset?"Asset Purchase Order": "Purchase Order ";
                if(rec.data.withoutinventory){
                   callEmailWin("emailwin",rec,label,18,false,false,false,false,true);
                }else{
                    callEmailWin("emailwin",rec,label,5,false,false,false,false,true,false,false,false,false,false,true);
                }
            }
        } else if(this.quotation){
            if(this.isCustomer){
                label = WtfGlobal.getLocaleText("acc.accPref.autoCQN");
                callEmailWin("emailwin",rec,label,50,true,false,true);
            }else{
                label =  WtfGlobal.getLocaleText("acc.dimension.module.30");
                callEmailWin("emailwin",rec,label,57,false,false,true);
            }
        }else{
            if(this.isCustomer){
                label = this.isFixedAsset? WtfGlobal.getLocaleText("erp.navigate.AssetDisposalInvoice"):"Sales Invoice";
                if(rec.data.withoutinventory){
                    callEmailWin("emailwin",rec,label,11,true,true);
                }else{
                    callEmailWin("emailwin",rec,label,2,true,true);
                }
            }else{
                label = "Purchase Invoice";
                if(rec.data.withoutinventory){
                    callEmailWin("emailwin",rec,label,15,false,true);
                }else{
                    callEmailWin("emailwin",rec,label,6,false,true);
                }
            }
        }
    }
},

getLables : function(){
    var label = "";
    if(this.cash){
        if(this.isCustomer){
            label = "Cash Sales Receipt";
        }else{
            label = "Cash Purchase Receipt";
        }
    }else if(this.isOrder && !this.quotation){
        if(this.isCustomer){
            label = "Sales Order";
        }else{
            label = "Purchase Order";
        }
    }else if(this.quotation){
        if(this.isCustomer){
            label = "Customer Quotation";
        }else{
            label = "Vendor Quotation";
        }
    }else{
        if(this.isCustomer){
            label = "Customer Invoice";
        }else{
            label = "Vendor Invoice";
        }
    }
    
    return label;
},

disableComponent: function(){ // disable following component in case of save button press.
   
    if(this.fromLinkCombo && this.fromLinkCombo.getValue() === ''){
       // this.fromLinkCombo.emptyText = "";
        this.fromLinkCombo.clearValue();
    }
    
    if(this.PO && this.PO.getValue() === ''){
        this.handleEmptyText=true;
     //   this.PO.emptyText = "";
        this.PO.clearValue();        
    }

    if(Wtf.getCmp("savencreate" + this.heplmodeid + this.id)){
        Wtf.getCmp("savencreate" + this.heplmodeid + this.id).disable();
    }
    if(Wtf.getCmp("save" + this.heplmodeid + this.id)){
        Wtf.getCmp("save" + this.heplmodeid + this.id).disable();
    }
    
    if(Wtf.getCmp("posttext" + this.id)){
        Wtf.getCmp("posttext" + this.id).disable();
    }
    
    if(Wtf.getCmp("showaddress" + this.id)){
        Wtf.getCmp("showaddress" + this.id).disable(); 
    } 
       
    if(this.Grid){
        var GridStore = this.Grid.getStore();
        var count2 = GridStore.getCount();
        var lastRec2 = GridStore.getAt(count2-1);
        GridStore.remove(lastRec2);
    }

    if(this.GridPanel){
        this.GridPanel.disable();
    }else{
        this.Grid.purgeListeners();
    }

    if(this.NorthForm){
        this.NorthForm.disable();
    }
    if((Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA || Wtf.isExciseApplicable) && this.exciseFormPanel){
        if(this.exciseFormPanel.getForm().items != undefined && this.exciseFormPanel.getForm().items != null){
            for(var i=0; i<this.exciseFormPanel.getForm().items.length;i++){
                this.exciseFormPanel.getForm().items.item(i).disable();
            }
        }
    }
    if(this.southPanel){
        this.southPanel.disable();
    }
},


fillData : function(store){
    this.loadingMask.hide();
    var rec = store.getAt(0);
    this.openModuleTab(rec);
    this.ownerCt.remove(this);
},

openModuleTab:function(formrec){
    var templateId = this.templateModelCombo.getValue();
    var copyInv = true;
    WtfGlobal.openModuleTab(this, this.isCustomer, formrec.get("isQuotation"), this.isOrder, copyInv, templateId, formrec)
},

getModuleId : function(){
    var moduleId = "";
    if(this.cash){
            if(this.isCustomer){
//                if(Wtf.account.companyAccountPref.withoutinventory){
//                    moduleId = Wtf.Acc_Billing_Cash_Sales_ModuleId;
//                }else
                {
                    moduleId = Wtf.Acc_Cash_Sales_ModuleId;
                }
            }else{
//                if(Wtf.account.companyAccountPref.withoutinventory){
//                    moduleId = Wtf.Acc_BillingCash_Purchase_ModuleId;
//                }else
                {
                    moduleId = Wtf.Acc_Cash_Purchase_ModuleId;
                }
            }
        } else if(this.isOrder && !this.quotation){
            if(this.isCustomer){
//                if(Wtf.account.companyAccountPref.withoutinventory){
//                    moduleId = Wtf.Acc_BillingSales_Order_ModuleId;
//                }else
                {
                    moduleId = Wtf.Acc_Lease_Order;
                }
            }else{
//                if(Wtf.account.companyAccountPref.withoutinventory){
//                    moduleId = Wtf.Acc_BillingPurchase_Order_ModuleId;
//                }else
                {
                    moduleId = Wtf.Acc_Purchase_Order_ModuleId;
                }
            }
        } else if(this.quotation){
            if(this.isCustomer){
                moduleId = Wtf.Acc_Customer_Quotation_ModuleId;
            }else{
                moduleId = Wtf.Acc_Vendor_Quotation_ModuleId;
            }
        }else{
            if(this.isCustomer){
//                if(Wtf.account.companyAccountPref.withoutinventory){
//                    moduleId = Wtf.Acc_BillingInvoice_ModuleId;
//                }else
                {
                    if(this.isLeaseFixedAsset){
                         moduleId = Wtf.Acc_Invoice_ModuleId;
                    }else{
                         moduleId = Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId;
                    }
                }
            }else{
//                if(Wtf.account.companyAccountPref.withoutinventory){
//                    moduleId = Wtf.Acc_Vendor_BillingInvoice_ModuleId;
//                }else
                {
                    if(this.isLeaseFixedAsset){
                          moduleId = Wtf.Acc_Vendor_Invoice_ModuleId;
                    }else{  
                          moduleId = Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId;
                    }
                }
            }
        }
        return moduleId;
},


loadSelectedTemplateRecord : function(recordId){
    
    this.SelectedTemplateStore = new Wtf.data.Store({
            url:this.SelectedTemplateStoreUrl,
            scope:this,
            baseParams:{
                archieve:0,
//                costCenterId: this.CostCenter.getValue(),
                deleted:false,
                nondeleted:false,
                cashonly:(this.cash == undefined)?false:this.cash,
                creditonly:false,
                consolidateFlag:false,
                companyids:companyids,
                enddate:'',
//                pendingapproval:response.pendingApproval,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                isfavourite:false,
                startdate:'',
                moduleRecordId:recordId
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.SelectedTemplateRec)
        });
        this.SelectedTemplateStore.load();
},

                
loadUserStoreForInvoice : function(response, request){
    
   var customRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'billto'},
        {name:'discount'},
        {name:'currencysymbol'},
        {name:'orderamount'},
        {name:'isexpenseinv'},
        {name:'currencyid'},
        {name:'shipto'},
        {name:'mode'},
        {name:'billno'},
        {name:'date', type:'date'},
        {name:'duedate', type:'date'},
        {name:'shipdate', type:'date'},
        {name:'personname'},
        {name:'personemail'},
        {name:'personid'},
        {name:'shipping'},
        {name:'othercharges'},
        {name:'partialinv',type:'boolean'},
        {name:'amount'},
        {name:'amountdue'},
        {name:'termdays'},
        {name:'termname'},
        {name:'incash'},
        {name:'taxamount'},
        {name:'taxid'},
        {name:'orderamountwithTax'},
        {name:'taxincluded',type:'boolean'},
        {name:'taxname'},
        {name:'deleted'},
        {name:'amountinbase'},
        {name:'memo'},
        {name:'externalcurrencyrate'},
        {name:'ispercentdiscount'},
        {name:'discountval'},
        {name:'crdraccid'},
        {name:'creditDays'},
        {name:'isRepeated'},
        {name:'porefno'},
        {name:'costcenterid'},
        {name:'costcenterName'},
        {name:'interval'},
        {name:'intervalType'},
        {name:'startDate', type:'date'},
        {name:'nextDate', type:'date'},
        {name:'expireDate', type:'date'},
        {name:'repeateid'},
        {name:'status'},
        {name:'archieve', type:'int'},
        {name:'withoutinventory',type:'boolean'},
        {name:'rowproductname'},
        {name:'rowquantity'},
        {name:'rowrate'},
        {name:'rowprdiscount'},
        {name:'rowprtaxpercent'},
        {name:'agent'}
        
    ]);
    
    var customStoreUrl = "";
    
    if(this.isOrder && !this.quotation){
        customStoreUrl= this.businessPerson=="Customer" ? "ACCSalesOrderCMN/getSalesOrdersMerged.do":"ACCPurchaseOrderCMN/getPurchaseOrdersMerged.do"  
    }else if(this.quotation){
        customStoreUrl = this.isCustomer? "ACCSalesOrderCMN/getQuotations.do" : "ACCPurchaseOrderCMN/getQuotations.do";
    }else{
        customStoreUrl= this.businessPerson=="Customer" ? "ACCInvoiceCMN/getInvoicesMerged.do" : "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
    }
    this.CustomStore = new Wtf.data.GroupingStore({
            url:customStoreUrl,
            scope:this,
            baseParams:{
                archieve:0,
                costCenterId: this.CostCenter.getValue(),
                deleted:false,
                nondeleted:false,
                cashonly:(this.cash == undefined)?false:this.cash,
                creditonly:false,
                consolidateFlag:false,
                companyids:companyids,
                enddate:'',
                pendingapproval:response.pendingApproval,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                isfavourite:false,
                startdate:'',
                ss:request.params.number,
                isFixedAsset:this.isFixedAsset,
                isLeaseFixedAsset:this.isLeaseFixedAsset
            },
            sortInfo : {
                field : 'companyname',
                direction : 'ASC'
            },
            groupField : 'companyname',
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },customRec)
        });
        
        this.CustomStore.on('load', this.enableButtons(), this);
        this.CustomStore.load();
        
},

enableButtons : function(){
    Wtf.getCmp("emailbut" + this.id).enable();
    Wtf.getCmp("exportpdf" + this.id).enable();
    Wtf.getCmp("printSingleRecord" + this.id).enable();
    if(Wtf.getCmp("RecurringSO"))
    Wtf.getCmp("RecurringSO").enable(); 
},

exportPdfFunction : function(response, request){
    if(this.CustomStore != null){
        var rec = this.CustomStore.getAt(0);
        var recData = rec.data;
        var billno = recData.billno;
        var selRec = "&amount="+recData.amount+"&isexpenseinv="+recData.isexpenseinv+"&bills="+recData.billid;
        var fileName = this.label+" "+billno; 
        var mode = "";
        if(this.cash){
            if(this.isCustomer){
                if(recData.withoutinventory){
                    mode = 11;
                }else{
                    mode = 2;
                }
            }else{
                if(recData.withoutinventory){
                    mode = 15;
                }else{
                    mode = 6;
                }
            }
        } else if(this.isOrder && !this.quotation){
            if(this.isCustomer){
                fileName="Sales Order "+recData.billno;
                if(recData.withoutinventory){
                    mode = 17;
                }else{
                    mode = 1;
                }
            }else{
                fileName="Purchase Order "+recData.billno;
                if(recData.withoutinventory){
                    mode = 18;
                }else{
                    mode = 5;
                }
            }
        } else if(this.quotation){
            fileName="Quotation "+recData.billno;
            if(this.isCustomer){
                mode = 50;
            }else{
                mode = 57;
            }
        }else{
            if(this.isCustomer){
                fileName="Invoice "+recData.billno;
                if(recData.withoutinventory){
                    mode = 11;
                }else{
                    mode = 2;
                }
            }else{
                fileName="Vendor Invoice "+recData.billno;
                if(recData.withoutinventory){
                    mode = 15;
                }else{
                    mode = 6;
                }
            }
        }
         if (mode==50 && Wtf.templateflag == 2) {
            Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportSenwanGroupSingleCustomerQuotation.do?moduleid=2&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf";
        }
        else if ((mode==2||mode==11)&&!rec.data.isadvancepayment && Wtf.templateflag == 2) {
            Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportSenwanCommercialInvoiceJasper.do?moduleid=2&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf";
        }
        else if((mode==2||mode==11)&&!rec.data.isadvancepayment && Wtf.templateflag == 1){
              Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportCustomerInvoiceReport.do?moduleid=12&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf";
        }
        else if((mode==2||mode==11)&&!rec.data.isadvancepayment && Wtf.templateflag == 4){
              Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportLSHCustomerInvoiceReport.do?moduleid=12&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf";
        }
        else{
             Wtf.get('downloadframe').dom.src = "ACCExportRecord/exportRecords.do?mode="+mode+"&rec="+selRec+"&personid="+recData.personid+"&filename="+fileName+"&filetype=pdf";
        }
    }
},



    loadStore:function(){
//        if(!(this.isCustBill||this.isExpenseInv))
//            this.Grid.priceStore.purgeListeners();
        if(!this.isEdit && !this.copyInv){  //in edit and copy case do not remove record from store
                this.Grid.getStore().removeAll();
            }
//        this.setTransactionNumber();
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
                WtfGlobal.hideFormElement(this.autoGenerateDO);
//                WtfGlobal.hideFormElement(this.invoiceList);
        }else{
                this.autoGenerateDO.reset();
                if(!this.isTemplate) {
                    this.autoGenerateDO.enable();
                }
                this.includingGST.reset();
                this.includingGST.enable();
        }
        
        this.NorthForm.getForm().reset();
        this.sequenceFormatStore.load();
        if(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId  ||this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId)
        this.sequenceFormatStoreDo.load();
        if(this.isTemplate){
            this.moduleTemplateName.setValue(this.oldTempNameVal);
            if(this.createTransactionAlsoOldVal){
                this.createAsTransactionChk.setValue(true);
                this.Number.enable();
                this.sequenceFormatCombobox.enable();
            }
        }
        this.setTransactionNumber();
        if(this.fromPO){
            //this.PO.enable();
            this.fromPO.enable();
        }
        if(this.fromLinkCombo){
            this.fromLinkCombo.setDisabled(true);
            this.fromLinkCombo.clearValue();
        }
        this.fromPO.setValue(false); 
//        this.POStore.reload();			Code Optimizing :)  Unnecessary Reload removed
        if(!this.isEdit && !this.copyInv){   //in edit and copy case do not remove record from store
                    this.Grid.getStore().removeAll();
                }
        if(this.partialInvoiceCmb){
            this.partialInvoiceCmb.disable();
            var id=this.Grid.getId();
            var rowindex=this.Grid.getColumnModel().getIndexById(id+"partdisc");
            if(rowindex != -1){
                this.Grid.getColumnModel().setHidden( rowindex,true);
            }
            
        }
//         var currentTaxItems=this.Name.store.data.items;
//         var currentTaxId="";
//         var customer= this.Name.getValue();
//            for(var i=0;i<currentTaxItems.length;i++)
//                {
//                     if(this.Name.store.data.items[i].json.accid==customer)
//                         {
//                             currentTaxId=this.Name.store.data.items[i].json.taxId;
//                         }
//                }
        this.showGridTax(null,null,true);
        this.Grid.symbol=undefined; // To reset currency symbol. BUG Fixed #16202
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
                        this.Tax.setDisabled(true);				// 20148 fixed
                        this.isTaxable.setValue(false);
                }
        
        this.template.setValue(Wtf.Acc_Basic_Template_Id);
//        var date=WtfGlobal.convertToGenericDate(new Date())
//        if(!(this.isCustBill||this.isExpenseInv)) {
//            var affecteduser = this.Name.getValue();
////            this.Grid.loadPriceStoreOnly(new Date(),this.Grid.priceStore, affecteduser);
//        } else
        this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(new Date())}});
        this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body,{productname:"&nbsp;&nbsp;&nbsp;&nbsp;",productid:0,qty:0,soqty:0,poqty:0});
        this.currencyStore.on("load",function(store){
            if(this.resetForm){
                if(this.currencyStore.getCount()<1){
                    callCurrencyExchangeWindow();
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invoice.msg1")],2); //"Please set Currency Exchange Rates"
                } else {
                    this.isCurrencyLoad=true;
//                    this.Currency.setValue(WtfGlobal.getCurrencyID());
//                    this.currencyid=WtfGlobal.getCurrencyID();
                    this.applyCurrencySymbol();
                     /*
                    * If IncludingGST = true in GR and Generating PI then flag=false
                    */
                    var flag=this.includingGST.getValue();
                    this.showGridTax(null,null,flag==true ? false :true);
                    if(flag==true)
                    {
                    this.includeProTax.disable();
                    }
                    if(this.isEdit){  
                        if(this.record.data.includeprotax){
                            this.includeProTax.setValue(true);
                            this.showGridTax(null,null,false);
                        }else{
                            this.includeProTax.setValue(false);
                            this.showGridTax(null,null,true);
                        }
                    }
                    var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
                     var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";
                if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null ){
                        this.isTaxable.setValue(true);
                        this.Tax.enable();
                        this.Tax.setValue(actualTaxId);
                    } else {
                        this.isTaxable.setValue(false);
                        this.Tax.setValue('');
                        this.Tax.disable();
                    }
                    //                    this.applyTemplate(this.currencyStore,0);
                    this.resetForm = false;
        }
    }
},this);
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
                this.Number.enable();
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
        this.Grid.symbol=undefined; // To reset currency symbol. BUG Fixed #16202
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
                        this.Tax.setDisabled(true);				// 20148 fixed
                        this.isTaxable.setValue(false);
                }
        
        this.template.setValue(Wtf.Acc_Basic_Template_Id);
        this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body,{productname:"&nbsp;&nbsp;&nbsp;&nbsp;",productid:0,qty:0,soqty:0,poqty:0});    
},    
    setDate:function(){
        var height = 0;
        if((this.isOrder&&!this.isCustomer)||(this.isOrder&&this.isCustomer)){
            height=430;
        }
        if(!this.quotation&&this.isOrder&&!this.isCustomer && !this.isCustBill && ((BCHLCompanyId.indexOf(companyid) != -1)))
            height=485;
        if(!this.isCustomer&&!this.isCash&&!this.isOrder&&!this.quotation)
            height=400;
        if(!this.isCustomer&&this.quotation)
            height=310;
        if(this.isCustBill){
        	if(this.isEdit)
                this.allAccountStore.on('load',this.getCreditTo.createDelegate(this,[this.record.data.crdraccid]),this)
            this.allAccountStore.load();
            height+=20;
        }
        if(height>=178) this.NorthForm.setHeight(height);

        if(!this.isEdit || this.isCopyFromTemplate){
            this.Discount.setValue(0);
            if(this.isOpeningBalanceOrder){
                this.billDate.setValue(this.getFinancialYRStartDatesMinOne(true));
            }else{
                this.billDate.setValue(new Date());//(new Date());
            }
//            this.DueDate.setValue(Wtf.serverDate);
        }
    },
    addTax:function(){
         var p= callTax("taxwin");
         Wtf.getCmp("taxwin").on('update', function(){this.Grid.taxStore.reload();}, this);
    },
    addCreditTerm:function(){
        callCreditTerm('credittermwin');
        Wtf.getCmp('credittermwin').on('update', function(){this.termds.reload();}, this);
    },
    addPerson:function(isEdit,rec,winid,isCustomer){
        callBusinessContactWindow(isEdit, rec, winid, isCustomer);
        var tabid=this.isCustomer?'contactDetailCustomerTab':'contactDetailVendorTab';
        Wtf.getCmp(tabid).on('update', function(){
           this.isCustomer?Wtf.customerAccStore.reload():Wtf.vendorAccStore.reload();
        }, this);
    },
    
    addCostCenter:function(){
        callCostCenter('addCostCenterWin');
    },
setTransactionNumberDo:function(isSelectNoFromCombo){
    var format= this.isCustomer ? Wtf.account.companyAccountPref.autodo : Wtf.account.companyAccountPref.autogro;
    var temp2=this.isCustomer ? Wtf.autoNum.DeliveryOrder : Wtf.autoNum.GoodsReceiptOrder;
    if(isSelectNoFromCombo){
        this.fromnumberDo = temp2;
    } else if(format&&format.length>0){
        WtfGlobal.fetchAutoNumber(temp2, function(resp){
            if(this.isEdit)this.no.setValue(resp.data)
                }, this);
    }
},   
  
resetCustomFields : function(){ // For reset Custom Fields, Check List and Custom Dimension
    var customFieldArray = this.tagsFieldset.customFieldArray;  // Reset Custom Fields
    if(customFieldArray!=null && customFieldArray!=undefined && customFieldArray!="" ) {
        for (var itemcnt = 0; itemcnt < customFieldArray.length; itemcnt++) {
            var fieldId = customFieldArray[itemcnt].id
            if (Wtf.getCmp(fieldId) != undefined && customFieldArray[itemcnt].getXType()!='fieldset') {
                Wtf.getCmp(fieldId).reset();
            }
        }
    }
    var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray;  // Reset Check List
    if(checkListCheckBoxesArray!=null && checkListCheckBoxesArray!=undefined && checkListCheckBoxesArray!="" ) {
        for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
            var checkfieldId = checkListCheckBoxesArray[checkitemcnt].id
            if (Wtf.getCmp(checkfieldId) != undefined) {
                Wtf.getCmp(checkfieldId).reset();
            }
        }
    }
    
    var customDimensionArray = this.tagsFieldset.dimensionFieldArray;  // Reset Custom Dimension
    if(customDimensionArray!=null && customDimensionArray!=undefined && customDimensionArray!="" ) {
        for (var itemcnt1 = 0; itemcnt1 < customDimensionArray.length; itemcnt1++) {
            var fieldId1 = customDimensionArray[itemcnt1].id
            if (Wtf.getCmp(fieldId1) != undefined) {
                Wtf.getCmp(fieldId1).reset();
            }
        }
    }
},
hideDO:function(){
    WtfGlobal.hideFormElement(this.sequenceFormatComboboxDo);
    WtfGlobal.hideFormElement(this.no);
    this.no.allowBlank = true;
},
showDO:function(){
    WtfGlobal.showFormElement(this.sequenceFormatComboboxDo);
    WtfGlobal.showFormElement(this.no);
    this.no.allowBlank = false;
      
},
setTransactionNumber:function(isSelectNoFromCombo){
    	if(this.quotation==null || this.quotation==undefined)
    		this.quotation = false;
        if(!this.isEdit||this.copyInv||this.isPOfromSO||this.isSOfromPO){
            var temp=this.isCustBill*1000+this.isCustomer*100+this.isOrder*10+this.cash*1+this.quotation*1;
            var temp2=0;
            var format="";
            switch(temp){
                case 0:format=Wtf.account.companyAccountPref.autogoodsreceipt;
                    temp2=Wtf.autoNum.GoodsReceipt;
                    break;
                case 1:format=Wtf.account.companyAccountPref.autocashpurchase;
                    temp2=Wtf.autoNum.CashPurchase;
                    break;
                case 10:format=Wtf.account.companyAccountPref.autopo;
                    temp2=Wtf.autoNum.PurchaseOrder;
                    break;
                case 100:format=Wtf.account.companyAccountPref.autoinvoice;
                    temp2=Wtf.autoNum.Invoice;
                    break;
                case 101:format=Wtf.account.companyAccountPref.autocashsales;
                    temp2=Wtf.autoNum.CashSale;
                    break;
                case 110:format=Wtf.account.companyAccountPref.autoso;
                    temp2=Wtf.autoNum.SalesOrder;
                    break;
                case 1000:format=Wtf.account.companyAccountPref.autobillinggoodsreceipt;
                    temp2=Wtf.autoNum.BillingGoodsReceipt;
                    break;
                case 1001:format=Wtf.account.companyAccountPref.autobillingcashpurchase;
                    temp2=Wtf.autoNum.BillingCashPurchase;
                    break;
                case 1010:format=Wtf.account.companyAccountPref.autobillingpo;
                    temp2=Wtf.autoNum.BillingPurchaseOrder;
                    break;
                case 1100:format=Wtf.account.companyAccountPref.autobillinginvoice;
                    temp2=Wtf.autoNum.BillingInvoice;
                    break;
                case 1101:format=Wtf.account.companyAccountPref.autobillingcashsales;
                    temp2=Wtf.autoNum.BillingCashSale;
                    break;
                case 1110:format=Wtf.account.companyAccountPref.autobillingso;
                    temp2=Wtf.autoNum.BillingSalesOrder;
                    break;
                case 111:format=Wtf.account.companyAccountPref.autoquotation;
                	temp2=Wtf.autoNum.Quotation;
                	break;
                case 11:format=Wtf.account.companyAccountPref.autovenquotation;
                	temp2=Wtf.autoNum.Venquotation;
                	break;         
            }

            if(isSelectNoFromCombo){
                this.fromnumber = temp2;
            } else if(format&&format.length>0 && (!this.isTemplate || this.createTransactionAlso)){
                WtfGlobal.fetchAutoNumber(temp2, function(resp){if(this.isEdit){this.Number.setValue(resp.data)}}, this);
            }
        }
    },
    dueDateCheck:function(){
        if(this.DueDate.getValue().getTime()<this.billDate.getValue().getTime()){
           WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.msg11")], 2);    //"The Due Date should be greater than the Order Date."
           this.DueDate.setValue(this.billDate.getValue());
        }
    },

    initForClose:function(){
        this.cascade(function(comp){
            if(comp.isXType('field')){
                comp.on('change', function(){this.isClosable=false;},this);
            }
        },this);
    },
     loadTemplateStore : function() {
        this.templateStore.load();
    },
    addInvoiceTemplate : function(isCreatedNow,tempid) {
        if(isCreatedNow===true){
            this.loadTemplateStore();
            this.templateStore.on("load",function(){
            	this.template.setValue(tempid);
            	this.templateID.setValue(tempid);
            },this)
            	
        }else{
        new Wtf.selectNewTempWin({
            isreport : false,
            tabObj : this,
            templatetype : this.doctype 
        });
        }
    },
    getJSONArray:function(store, includeLast, idxArr){
        var indices="";
        if(idxArr)
            indices=":"+idxArr.join(":")+":";        
        var arr=[];
        var fields=store.fields;
        var len=store.getCount();
        //if(includeLast)len++;
        
        for(var i=0;i<len;i++){
            if(idxArr&&indices.indexOf(":"+i+":")<0) continue;
            var rec=store.getAt(i);
            var recarr=[];
            for(var j=0;j<fields.length;j++){
                var value=rec.data[fields.get(j).name];
                switch(fields.get(j).type){
                    case "auto":if(value!=undefined){value=(value+"").trim();}value=encodeURI(value);value="\""+value+"\"";break;
                    case "date":value="'"+WtfGlobal.convertToGenericDate(value)+"'";break;
    }
                recarr.push(fields.get(j).name+":"+value);
            }
            recarr.push("modified:"+rec.dirty);
            arr.push("{"+recarr.join(",")+"}");
        }
        return "["+arr.join(',')+"]";
    },
    setValues:function(billid){
            if(billid.indexOf(",")==-1){  //In MultiSelection if the user select only one                              
                    var rec=this.POStore.getAt(this.POStore.find('billid',billid));
                    if(!this.isCustBill && !this.isOrder && !this.cash && this.isCustomer){
                        if(this.fromLinkCombo.getValue()==1 && !rec.data['includeprotax']){
                            this.includeProTax.setValue(false);
                            this.showGridTax(null,null,true);            
                        }
                    }
                    this.Memo.setValue(rec.data['memo']);
                    this.shipDate.setValue(rec.data['shipdate']);
                    this.validTillDate.setValue(rec.data['validdate']);
                    if((this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId ) && !this.cash && !this.isFixedAsset) { // set value only in VI module
                        this.invoiceList.setValue(rec.data['landedInvoiceNumber']);
                    }
                    if(((this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) && !this.cash) || ((this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId) && !this.cash)) {
                        this.Term.setValue(rec.data['termid']);
                    }
                    this.postText=rec.data['posttext'];
                    this.shipvia.setValue(rec.data['shipvia']);
                    this.fob.setValue(rec.data['fob']);  
                    this.isCustomer ? this.users.setValue(rec.data['salesPerson']) : this.users.setValue(rec.data['agent']);

                    if(rec.data["discounttotal"] && this.Discount){
                        this.Discount.setValue(rec.data["discounttotal"]);
                        this.perDiscount.setValue(rec.data["discountispertotal"]);
                    }

                    if(rec.data['taxid']!=""){
                        this.Tax.enable();
                        this.isTaxable.setValue(true);
//                        if (isTaxActivate(this.Grid.taxStore, rec.data['taxid'], "prtaxid")) {
                            this.Tax.setValue(rec.data['taxid']);
//                        } else {
//                            this.Tax.setValue("");
//                        }
                        this.includingGST.setValue(false);
                        this.includingGST.disable();
                        this.HideShowLinkedTermTaxAndTermTaxAmountCol(this.isTaxable.getValue());
                    }else{
                        this.Tax.disable();
                        this.isTaxable.reset();
                        this.Tax.reset();
                    }
                    this.getCreditTo(rec.data.creditoraccount);                                
                    var perstore=this.isCustomer? Wtf.customerAccStore:Wtf.vendorAccStore
                    var index = perstore.find('accid',this.Name.getValue());
                    if(index != -1){
                        var storerec=perstore.getAt(index);                        
                        this.Term.setValue(storerec.data['termid']);
                    }        
                    this.CostCenter.setValue(rec.data.costcenterid);
                }else{ //if the user select multiple values
                    this.clearComponentValues();
                }
    },
    clearComponentValues:function(){
        this.Memo.setValue('');
        this.shipDate.setValue('');
        this.validTillDate.setValue('');
        this.shipvia.setValue('');
        this.fob.setValue('');
        this.loadTransStore();
        this.Discount.setValue(0);
        this.perDiscount.setValue(false);
        this.Tax.disable();
        this.isTaxable.reset();
        this.Tax.reset();                    
        this.CostCenter.setValue('');
        if((this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) && !this.cash && !this.isFixedAsset) { // set value only in VI module
            this.invoiceList.setValue('');
        }
    },
    onCurrencyChangeOnly:function(){
        this.fromPO.reset();
        this.fromLinkCombo.setRawValue('');
        this.fromLinkCombo.applyEmptyText();
        this.fromLinkCombo.setDisabled(true);
        this.PO.reset();this.PO.setDisabled(true);                                       
        if(this.partialInvoiceCmb){
            this.partialInvoiceCmb.reset();
            this.partialInvoiceCmb.disable();
            var id=this.Grid.getId();
            var rowindex=this.Grid.getColumnModel().getIndexById(id+"partdisc");
            if(rowindex != -1){
                this.Grid.getColumnModel().setHidden( rowindex,true);
            }
        }                                
        this.Discount.setValue(0);
        this.perDiscount.setValue(false);
        var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
        var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";
            if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null ){
                this.isTaxable.setValue(true);
                this.Tax.enable();
                this.Tax.setValue(actualTaxId);
            }else{     
                this.Tax.disable();
                this.isTaxable.reset();
                this.Tax.reset();
            }
        this.includeProTax.setValue(false);
        this.showGridTax(null,null,true);
        this.Grid.getStore().removeAll();
        this.Grid.addBlankRow(); 
        this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body,{productname:"&nbsp;&nbsp;&nbsp;&nbsp;",qty:0,soqty:0,poqty:0});                
    },

    setTermValues : function(termDetails)  {
        if( this.termgrid!=undefined && this.termgrid!=null){   
            if(termDetails!=""&&termDetails!=null&&termDetails!=undefined){
                var detailArr = eval(termDetails);
                for(var cnt=0; cnt<detailArr.length; cnt++ ){
                    var jObj = detailArr[cnt];

                    var record = this.termStore.queryBy(function(record){
                        return (record.get('id') == jObj.id);
                    }, this).items[0];
                    if(record) {
                        record.set('termamount',jObj.termamount);
                        record.set('termpercentage',jObj.termpercentage==0?"":jObj.termpercentage);
                        /*
                         * ERP-40242 : The below check is to avoid load tax and tax amount in copy case if tax is  deactivated.
                         */
                        if (jObj.isActivated || !(this.copyInv || (this.fromPO != undefined && this.fromPO.getValue()))) {
                        if (!this.readOnly) {
                            this.linkedTaxDummyStore.insert(cnt, new this.linkedTaxDummyStore.recordType({
                                termtax:jObj.termtax,
                                linkedtaxname:jObj.linkedtaxname,
                                linkedtaxpercentage:jObj.linkedtaxpercentage
                            }));
                            record.set('termtax',jObj.termtax);
                        } else {
                            record.set('termtax',jObj.linkedtaxname);
                        }
                        record.set('linkedtaxpercentage',jObj.linkedtaxpercentage);
                        record.set('termtaxamount',jObj.termtaxamount);
                        }
                   }
                
                }
            }
             WtfGlobal.fliterNonUsedDeactivatedTerms(this);
     }
    },

//    addInvoiceTermGrid : function() {
//        this.termcm=[{
//            header: 'Term',
//            dataIndex: 'term'
//        },{
//            header: 'Percentage',
//            dataIndex: 'termpercentage',
//            editor:new Wtf.form.NumberField({
//                xtype : "numberfield", 
//                maxLength : 15,
//                allowNegative : false,
//                minValue : 0,
//                maxValue: 100,
//                regexText:Wtf.MaxLengthText+"15"
//            })
//        },{
//            header: 'Amount',
//            dataIndex: 'termamount',
//            renderer : function(val, meta, rec) {
//                if(typeof val=='number' && val>=0 && rec.data.sign==0) {
//                    rec.set('termamount',val*(-1));
//                    return val*(-1)
//                } else 
//                    return val;
//            },
//            editor:new Wtf.form.NumberField({
//                xtype : "numberfield", 
//                maxLength : 15,
//                allowNegative : true,
//                regexText:Wtf.MaxLengthText+"15"
//            })
//        }];
//        
//        this.termRec =new Wtf.data.Record.create([
//        {name: 'id'},
//        {name: 'term'},
//        {name: 'glaccount'},
//        {name: 'sign'
////        },{ name: 'category'
////        }, {name: 'includegst'
////        },{ name: 'includeprofit'
////        },{name: 'suppressamnt'
//        },{name: 'formula'
//        },{name: 'formulaids'
//        },{name: 'termamount'
//        },{name: 'termpercentage'
//        },{name: 'termtaxamount'
//        },{name: 'termtax'
//        },{name: 'termtaxamountinbase'}
//        ]);
//        this.termStore = new Wtf.data.Store({
//            reader: new Wtf.data.KwlJsonReader({
//                root: "data"
//            },this.termRec),
////            url:Wtf.req.account+'CompanyManager.jsp',
//            url: 'ACCAccount/getInvoiceTermsSales.do',
//            baseParams:{
//                isSalesOrPurchase:this.isCustomer?true:false
//        }
//        });
////        if(!this.isEdit) {
//            this.termStore.load();
//            this.termStore.on('load',this.closeTermGrid,this);
////        }
////        this.calInvoiceTermBtn= new Wtf.Toolbar.Button({
////            text:"Apply Terms",
////            scope:this,
////            tooltip:{text:'Apply Terms'},
////            handler:function() {this.updateSubtotal()}
////        });
//        this.termgrid = new Wtf.grid.EditorGridPanel({
////            layout:'fit',
//            clicksToEdit:1,
//            store: this.termStore,
//            height:100,
//            autoScroll : true,
//            disabledClass:"newtripcmbss",
////            style:'padding-top:10px;',
//            cm: new Wtf.grid.ColumnModel(this.termcm),
//            border : false,
//            loadMask : true,
////            tbar : this.calInvoiceTermBtn,
//            viewConfig: {
//                forceFit:true,
//                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
//            }
//        });
//        this.termgrid.on('afteredit',function(obj) {
//            if (obj.field == 'termamount') {
//                obj.record.set('termpercentage', '');
//            } else if (obj.field == 'termpercentage' && obj.value == 0) {
//                obj.record.set('termpercentage', '');
//            }
//            this.updateSubtotalOnTermChange(true,(obj.field=='termamount'));  
//        },this); 
//        this.termgrid.on('cellclick',function(grid, rowIndex, columnIndex, e) {
//            var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
//            if(this.isViewTemplate || (this.isLinkedTransaction && this.isEdit)){
//                if(fieldName=='termamount' || fieldName=='termpercentage') {
//                    return false;
//                }
//           }
//        },this);
//    },
//    
//    closeTermGrid : function(obj){
//        var store = this.termgrid.store;
////        if(this.termStore.data.length==0||this.cash)
//            if(this.termStore.data.length==0)
//            {
//                this.termgrid.hide();
//            }
//            
//    },  
    updateSubtotalOnTermChange : function(updateTotalSummary,termAmountChanged) {
        for(var i=0; i<this.termStore.getCount(); i++) {
            var recdata = this.termStore.getAt(i).data; //  var recdata = obj.record.data;
            var store = this.termStore;
            var subtotal = this.calProdSubtotalWithoutDiscount();
            var formula = recdata.formulaids.split(",");
            var termtotal = 0;
            for(var cnt=0; cnt<formula.length; cnt++){
                if(formula[cnt]=='Basic') {
                    termtotal +=(subtotal);
                }
                var record = store.queryBy(function(record){
                    return (record.get('id') == formula[cnt]);
                }, this).items[0];
                if(record && (typeof record.data.termamount=='number')) {
                    //                termtotal +=(record.data.termamount*(record.data.sign==1 ? 1 : -1 ));
                    termtotal +=(record.data.termamount);
                }
            }
            if(typeof (recdata.termpercentage) != "string" && parseInt(recdata.termpercentage) >= 0) {
                //            obj.record.set('termamount',0);
                var opmod = recdata.sign==0 ? -1 : 1;
                var this_termTotal = ((Math.abs(termtotal) * recdata.termpercentage*1) / 100)*opmod;
                //                if(this.termStore.getAt(i).get('termpercentage') != '') {
                this.termStore.getAt(i).set('termamount',getRoundedAmountValue(this_termTotal));
            //                }
            }
        }
        if(this.includeProTax.getValue() && this.applyTaxToTermsChk.getValue()){
            this.addTermAmountInTax();
        }
        if (updateTotalSummary) {
            this.updateSubtotal(this,termAmountChanged);
        }
//        else if(obj.field=="termamount") {
//            
//        } 
    },
    
    findTermsTotal : function() {
        var termTotal = 0;
        if(this.termgrid) {
            var store = this.termgrid.store;
            var totalCnt = store.getCount();
            for(var cnt=0; cnt<totalCnt; cnt++) {
                var lineAmt = store.getAt(cnt).data.termamount;
                if(typeof lineAmt=='number')
                    termTotal += lineAmt;
            }
        }
        /*
        * In case of "Inclusive of Tax" Term Amount is considered as Including Tax.
        * So in case of Including GST we are calculating term amount as below,
        * Total Term Amount minus Total tax calculate on term amount.
        */
        if(this.includingGST && this.includingGST.getValue()){
            termTotal = termTotal - this.addTermAmountInTax();
        }
        return termTotal;
    },
    
    getInvoiceTermDetails : function() {
        var arr=[];
        if(this.termgrid) {
            var store = this.termgrid.store;
            store.each(function(rec){
                if(rec.data.termtax == "None"){
                   rec.set("termtax", "");
                }
                var lineAmt = rec.data.termamount;
                if(typeof lineAmt=='number' && lineAmt !=0) {
                    arr.push(store.indexOf(rec));
                }            
            }, this);
            return WtfGlobal.getJSONArray(this.termgrid,true,arr)
        }
    },
    
    calProdSubtotalWithoutDiscount:function(){
        var subtotal=0;
        var count=this.Grid.store.getCount();
        var store = this.Grid.store;
        for(var i=0;i<count;i++){
            var total=store.getAt(i).data.amountwithouttax;
//            if(this.editTransaction&&!this.fromPO){
//                    total=total/this.store.getAt(i).data['oldcurrencyrate'];
//                }
            subtotal+=total;
        }
        return subtotal;
    },
    checkForDeActivatedProductsAdded:function(){
    var invalidProducts='';
    if(this.isEdit && !this.copyInv){ // Edit case
        var linkedDocuments = this.PO.getValue();
        var linkedDocsArray=[];
        if(linkedDocuments != ''){
            linkedDocsArray = linkedDocuments.split(',');
            var areDocumentsChanged = false;
            for(var x=0;x<linkedDocsArray.length;x++){
                var docId = linkedDocsArray[x];
                if(this.originallyLinkedDocuments.indexOf(docId) == -1){
                    areDocumentsChanged = true;
                    break;
                }
            }
            if(areDocumentsChanged){
                invalidProducts = this.checkDeactivatedProductsInGrid();
            }
        }
    } else { // Cretae New and Copy
        invalidProducts = this.checkDeactivatedProductsInGrid();
    }
    return invalidProducts;
},
    checkDeactivatedProductsInGrid :function(){
        var inValidProducts=''
        var rec = null;
        var productId = null;
        var productRec = null;
        for(var count=0;count<this.Grid.store.getCount();count++){
            rec = this.Grid.store.getAt(count);
            productId = rec.data.productid;
            if(productId!= undefined && productId != null && productId != ''){
                if(!this.fromPO.getValue() && !this.copyInv){
                    productRec = WtfGlobal.searchRecord(this.Grid.productComboStore, productId, "productid");
                } else {
                    productRec = this.Grid.store.getAt(count);
                }
                if(productRec && (productRec.data.hasAccess === false)){
                    inValidProducts+=productRec.data.productname+', ';
                }
            }    
        }
        return inValidProducts; // List of deactivated products
    }
})

