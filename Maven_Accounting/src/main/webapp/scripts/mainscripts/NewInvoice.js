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
/**
 *Moved this function to wtfmain-ex.js
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
 function Showproductdetails(productid,productname,so){
       callViewProductDetails(productid,'View Product',so,productname);
}
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

Wtf.account.TransactionPanelSats=function(config){
    this.quotation = (config.quotation!=null && config.quotation!=undefined)?config.quotation:false;
    this.DefaultVendor = config.DefaultVendor;
	this.id=config.id;
	this.titlel = config.title!=undefined?config.title:"null";
    this.dataLoaded=false;
    this.isViewTemplate = (config.isViewTemplate!=undefined?config.isViewTemplate:false);
    this.isTemplate = (config.isTemplate!=undefined?config.isTemplate:false);
    this.createTransactionAlso = false;
    this.transactionType = 0;
    this.recordId = "";
    this.isGeneratedRecurringInvoice=config.isGeneratedRecurringInvoice;
    this.onDate=config.onDate;
    this.isCopyFromTemplate = (config.isCopyFromTemplate!=undefined?config.isCopyFromTemplate:false);
    this.isOpeningBalanceOrder = (config.isOpeningBalanceOrder!=undefined?config.isOpeningBalanceOrder:false);
    this.isSelfBilledInvoice=(config.isSelfBilledInvoice!=undefined && config.isSelfBilledInvoice!=null)?config.isSelfBilledInvoice:false;
    this.templateId = config.templateId;
//    this.isOnTemplateSelect = undefined;
    this.sendMailFlag = false;
    this.saveOnlyFlag = false;
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
    this.handleEmptyText=false; //To handle empty text after clicking on save button
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
    this.currenctAddressDetailrec="";
    var help=getHelpButton(this,config.heplmodeid);
    this.autoPopulateMappedProduct=Wtf.account.companyAccountPref.autoPopulateMappedProduct!=undefined? Wtf.account.companyAccountPref.autoPopulateMappedProduct:false;
    this.custUPermType=config.isCustomer?Wtf.UPerm.customer:Wtf.UPerm.vendor;
    this.custPermType=config.isCustomer?Wtf.Perm.customer:Wtf.Perm.vendor;
    this.soUPermType=(config.isCustomer?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
    this.soPermType=(config.isCustomer?Wtf.Perm.invoice.createso:Wtf.Perm.vendorinvoice.createpo);
    this.isFromProjectStatusRep = (config.isFromProjectStatusRep!=null&&config.isFromProjectStatusRep!=undefined)?config.isFromProjectStatusRep:false;
    var isbchlFields1=(!config.isCustomer && config.isOrder);
    this.isWithInvUpdate = config.isWithInvUpdate;
    this.DOSettings=config.DOSettings;
    this.GRSettings=config.GRSettings;
    this.addressrec=null;
    var buttonArray = new Array();
    var moduleId=WtfGlobal.getModuleId(this); // to show terms in all SO,CQ,VQ,PO
     this.IsInvoiceTerm = (config.isCustomer && (config.moduleid=='2'|| moduleId==22)) || moduleId==6 || moduleId==23 ;
//     this.IsInvoiceTerm = (config.isCustomer && config.moduleid=='2') || config.moduleid==6;
     this.modeName = config.modeName;
     this.viewGoodReceipt = config.viewGoodReceipt;
     this.islockQuantityflag=config.islockQuantityflag;
     this.readOnly=config.readOnly;
     buttonArray.push({
        text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
        toolTip: WtfGlobal.getLocaleText("acc.rem.175"),
        id: "save" + this.heplmodeid+ this.id,
        hidden:this.isViewTemplate,
        scope: this,
        handler: function(){
            this.saveOnlyFlag = true;
            if(this.isTemplate){
                if(this.moduleTemplateName.getValue() == ''){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.field.PleaseEnterTemplateNameFirst")], 1);
                    return;
                }
                this.saveTemplate();
            }else{
                this.save();
            }
        },
        iconCls: 'pwnd save'
    });
     
    buttonArray.push({
        text: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNew"),
        toolTip: WtfGlobal.getLocaleText("acc.rem.175"),
        id: "savencreate" + config.heplmodeid + this.id,
        hidden : (this.isEdit&&!this.isCopyFromTemplate) || (this.copyInv&&!this.isCopyFromTemplate) || this.isTemplate || this.isViewTemplate,
        scope: this,
        handler: function(){
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
   
    var tranType=null;
    if(this.isCustBill)
        tranType=config.isCustomer?(config.isOrder?Wtf.autoNum.BillingSalesOrder:Wtf.autoNum.BillingInvoice):(config.isOrder?Wtf.autoNum.BillingPurchaseOrder:Wtf.autoNum.BillingGoodsReceipt);
    else if(config.moduleid==18||config.moduleid==20){
        if(config.moduleid==18){
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
    }else if(config.moduleid==2){
        tranType=Wtf.autoNum.Invoice;
    }else{
        tranType=Wtf.autoNum.GoodsReceipt;
    }
    if(this.isRequisition) {
        tranType= Wtf.autoNum.Requisition;
    } else if(this.isRFQ) {
        tranType= Wtf.autoNum.RFQ;
    }
   
  
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
         hidden:this.isRequisition || this.isRFQ || this.isSalesCommissionStmt,
         menuItem:{rowPdf:(this.isSalesCommissionStmt)?false:true,rowPdfTitle:WtfGlobal.getLocaleText("acc.rem.39") + " "+ singlePDFtext},
         get:tranType,
         moduleid:config.moduleid
     });
     buttonArray.push(this.singlePrint);
            
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
        hidden:!(config.moduleid==Wtf.Acc_Purchase_Order_ModuleId || config.moduleid==Wtf.Acc_Sales_Order_ModuleId),
        tooltip : (config.isCustomer?WtfGlobal.getLocaleText("acc.field.ShowsOutstandigSalesOrder"):WtfGlobal.getLocaleText("acc.field.ShowsOutstandigPurchaseOrder")),
        style:" padding-left: 15px;",
        scope: this,
        disabled:this.isViewTemplate,
               
        handler: function() {
            this.outstandingreportflag=true;
             this.person= this.Name.getValue();
           if(config.moduleid==Wtf.Acc_Sales_Order_ModuleId)
            {
              
               callSalesOrderList(false,false,this.outstandingreportflag,this.person);  //for showing Outstanding SO and Po while creating one flag passed
            }
            else
            {
               callPurchaseOrderList(false,false,this.outstandingreportflag,this.person);
            }
        }
    });
     buttonArray.push({
        text:config.moduleid==Wtf.Acc_Invoice_ModuleId ? WtfGlobal.getLocaleText("acc.field.SetRecurringInvoice"):WtfGlobal.getLocaleText("acc.field.SetRecurringSO") ,
        iconCls:getButtonIconCls(Wtf.etype.copy),
        id:'RecurringSO',        
        hidden:!(config.moduleid==Wtf.Acc_Sales_Order_ModuleId || config.moduleid==Wtf.Acc_Invoice_ModuleId),
        tooltip :config.moduleid==Wtf.Acc_Invoice_ModuleId ?WtfGlobal.getLocaleText("acc.field.CreateRecurringInvoice"):WtfGlobal.getLocaleText("acc.field.CreateRecurringSalesOrder"),
        style:" padding-left: 15px;",
        scope: this,
        disabled : true,     
        handler: function() {
            if( config.moduleid==Wtf.Acc_Invoice_ModuleId)
                 callRepeatedInvoicesWindow(true,undefined,false,false,true,this.RecordID,this.Term.getValue());//set Forth Variable to false for Invoice  and true for sales order 
            else if( config.moduleid==Wtf.Acc_Sales_Order_ModuleId )
                 callRepeatedInvoicesWindow(true,undefined,false,true,true,this.RecordID,this.Term.getValue());  
        }
    });
    
    buttonArray.push({
        text: WtfGlobal.getLocaleText("acc.field.ShowAddress"),  //WtfGlobal.getLocaleText("acc.template.posttext") , //'<b>Post Text</b>',
        cls: 'pwnd add',
        id: "showaddress" + this.id,                
        tooltip : WtfGlobal.getLocaleText("acc.field.UseShowAddressoptiontoinsertAddresses"),       
        style:" padding-left: 15px;",
        scope: this,
        disabled : true, 
        handler:this.getAddressWindow 
    });
     
//    if (isbchlFields1 && this.isEdit && !this.copyInv) {
//
//        buttonArray.push(this.exportButton = new Wtf.exportButton({
//            obj: config.POthisObj,
////            id: "exportReports" + config.helpmodeid + config.id,
//            text: WtfGlobal.getLocaleText("acc.common.export"),
//            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
//            disabled: false,
//            menuItem: {csv: false, pdf: false, rowPdf: (config.isSalesCommissionStmt) ? false : true, rowPdfTitle: WtfGlobal.getLocaleText("acc.rem.39")},
//            get: config.POnewtranType
//        }));
//
//    }
    if(!this.readOnly){
        buttonArray.push('->');
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
    Wtf.account.TransactionPanelSats.superclass.constructor.call(this,config);
    this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.TransactionPanelSats,Wtf.account.ClosablePanel,{
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
            this.loadProductGrid(data.invoicetype);   // in edit and copy case grid was coming default it should be according to invoicetype
            if(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) {
                this.InvoiceStore.on("load", function(){
                    var record = new Wtf.data.Record({
                        billid: data.landedInvoiceID,
                        billno: data.landedInvoiceNumber
                    });
                    this.InvoiceStore.insert(0, record);
                    this.invoiceList.setValue(data.landedInvoiceID);
                }, this);
                this.InvoiceStore.load();
            }
            
            if((this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) || (this.moduleid == Wtf.Acc_Invoice_ModuleId && !this.cash)) {
                this.termds.on("load", function(){
                    this.Term.setValue(data.termdays);
                }, this);
                this.termds.load();
            }
            
                if(!this.copyInv&&!this.isPOfromSO&&!this.isSOfromPO){
                    this.Number.setValue(data.billno);
                }else if(this.copyInv){
                    this.Number.setValue("");//copy case assign ""
                }
            this.externalcurrencyrate=this.record.data.externalcurrencyrate;
                
            if(this.isPOfromSO||this.isSOfromPO){ // for showing link number in number field in case of creating PO from SO or creating SO from PO
                this.fromPO.setValue(true);
                this.fromLinkCombo.setValue(0);                
                this.POStore.proxy.conn.url =(this.isPOfromSO)?"ACCSalesOrderCMN/getSalesOrders.do":(this.isSOfromPO)?"ACCPurchaseOrderCMN/getPurchaseOrders.do":"";
                this.POStore.on("load", function(){
                    if(this.isPOfromSO||this.isSOfromPO){
                        this.PO.disable();
                        this.fromPO.disable();
                        this.setTransactionNumber();
                        this.PO.setValue(data.billid);
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
                    this.linkIDSFlag=false;
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
                                    billno:rec.data.linkto    
                                });
                                storeData.push(newRec);
                            }
                        }
                    },this);
                    if(storeData.length>0){
                        this.POStore.add(storeData);
                    }
                    if(linkIDS.length>0){
                        this.linkIDSFlag=true;
                        this.Name.disable();
                        this.Currency.disable();
                        this.fromPO.disable();
                        this.fromLinkCombo.disable();
                        this.PO.disable();
                        this.fromPO.setValue(true);                
                        this.PO.setValue(linkIDS);
                        this.includingGST.disable();  
                    }
                    if(linkType!=-1){
                        this.fromLinkCombo.setValue(linkType);
                    }
                    if(this.isEdit  && linkType==1){
                        this.autoGenerateDO.setValue(false);
                        this.autoGenerateDO.disable();
                    }
                }
                },this);
            }
            if(this.copyInv || this.isEdit){
                if(Wtf.getCmp("showaddress" + this.id)){
                    Wtf.getCmp("showaddress" + this.id).enable(); 
                }   
            }
            if(this.viewGoodReceipt)
                {
                   Wtf.getCmp("exportpdf" + this.id).hide(); 
                   Wtf.getCmp("RecurringSO").hide(); 
                   Wtf.getCmp("posttext" + this.id).hide();  
                   Wtf.getCmp('south' + this.id).hide();   
                   Wtf.getCmp('productDetailsTpl'+this.id).hide();
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
            
            var store=(this.isCustomer?Wtf.customerAccStore:Wtf.vendorAccStore)
            var index=store.findBy( function(rec){
                var parentname=rec.data['accid'];
                if(parentname==data.personid)
                    return true;
                 else
                    return false;
            })
            if(index>=0)
                this.Name.setValue(data.personid);
            this.Memo.setValue(data.memo);
            if(data.shiplengthval!="" && data.shiplengthval!=null && data.shiplengthval!=undefined)
                this.shipLength.setValue(data.shiplengthval);
            this.postText = data.posttext;
            this.DueDate.setValue(data.duedate);
            if(this.isOrder && data.isOpeningBalanceTransaction){
                this.isOpeningBalanceOrder = data.isOpeningBalanceTransaction;
                this.billDate.maxValue=this.getFinancialYRStartDatesMinOne(true);
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
            if(this.Grid){
                this.Grid.forCurrency =data.currencyid;
                this.Grid.affecteduser=data.personid;
                this.Grid.billDate=data.date;
            }
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
                if(!this.isEdit && !this.isCopy)           //In edit case no need to reset Transaction Tax. - Amol D.
                    this.isTaxable.reset();
                this.isTaxable.enable();
            }
            if(this.isEdit && !this.templateId) {
               this.templateModelCombo.disable();
            }
            this.loadTransStore();
            this.ProductGrid.productComboStore.load({params:{mappingProduct:true,customerid:this.Name.getValue(),common:'1', loadPrice:true,mode:22}}) ;           
            if(this.isExpenseInv){
                this.includingGST.setValue(false);
                this.includingGST.disable();
             }
             else{
                this.includingGST.reset();
                this.includingGST.enable();
                if(this.record.data.gstIncluded!=undefined){
                    this.includingGST.setValue(this.record.data.gstIncluded);
                }
            }
            if((data.taxid == "")||(this.isSOfromPO && WtfGlobal.getModuleId(this)==20)||(this.isPOfromSO && WtfGlobal.getModuleId(this)==18)){//generate so or po it should not show taxid
                this.isTaxable.setValue(false);
                this.Tax.setValue("");
                this.Tax.disable();
            }else{
                this.Tax.setValue(data.taxid);
                 this.isTaxable.enable();
                this.Tax.enable();//enable the tax when taxid is present-for edit case it was not required but for copy its is required.
                this.isTaxable.setValue(true);
            }
        }
    },
    onRender:function(config){        
        var centerPanel = new Wtf.Panel({
                region : 'center',
                border : false,
                autoScroll : true
            });
        if(this.isCustomer||this.isCustBill||this.isOrder||this.isEdit && !this.isExpenseInv||this.copyInv && !this.isExpenseInv || this.isTemplate) {
            centerPanel.add(this.NorthForm,this.Grid,this.southPanel);
        } else if((this.isEdit && this.isExpenseInv) || (this.copyInv && this.isExpenseInv)) {
            centerPanel.add(this.NorthForm,this.ExpenseGrid,this.southPanel);
//            this.Tax.store=this.ExpenseGrid.taxStore;
        } else {
            centerPanel.add(this.NorthForm,this.GridPanel,this.southPanel);
        }
        
        this.add(centerPanel);
        this.add({
                border: true,
                id: 'south' + this.id,
                region: 'south',
                //split: true,
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
            });
        Wtf.account.TransactionPanelSats.superclass.onRender.call(this, config);
        
        if(this.isViewTemplate){
            this.Number.hideLabel = true;
            this.Number.hide();
            this.sequenceFormatCombobox.hideLabel = true;
            this.sequenceFormatCombobox.hide();
//            this.billDate.hideLabel = true;
//            this.billDate.hide();
        }
        
        if(this.viewGoodReceipt){
            this.Number.hideLabel = false;
            this.Number.show();
            this.sequenceFormatCombobox.hideLabel = false;
            this.sequenceFormatCombobox.show();
            this.billDate.hideLabel =false;
            this.billDate.show();
        }
        
        if(this.isTemplate){
            this.Number.setValue("");
            this.Number.disable();
            this.sequenceFormatCombobox.disable();
            this.billDate.disable();
            this.autoGenerateDO.disable();
            this.generateReceipt.disable();
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
        
    if(this.isEdit || this.copyInv){
        this.billid=this.record.data.billid;
        this.createAddressStore();
        this.transactionsAddressStore.load();
        this.transactionsAddressStore.on('load',function(store){
            this.addressrec=store.getAt(0);
        },this);
    }        
        // hide form fields
            this.hideFormFields();
        
    },
    
    hideFormFields:function(){
        if(this.isCustomer){
            if(this.isInvoice){

                this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.customerInvoice);

            } else if(this.cash){

                this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.CS);

            } else if(this.isOrder && !this.quotation){

                this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.salesOrder);

            } else if(this.quotation){

                this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.customerQuotation);

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
    
    
    hideTransactionFormFields:function(array){
        if(array){
            for(var i=0;i<array.length;i++){
                var fieldArray = array[i];
                if(Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id)){
                    if(fieldArray.fieldId=="ShowOnlyOneTime" && ((this.isEdit !=undefined ?this.isEdit:false) || (this.copyInv !=undefined ?this.copyInv:false) || (this.isCopyFromTemplate !=undefined ?this.isCopyFromTemplate:false) || (this.isTemplate !=undefined ?this.isTemplate:false))){
                        continue;
                    }
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
        if((this.isEdit || this.copyInv) && !this.GENERATE_PO && !this.GENERATE_SO){ //only edit case & copy
            var index=this.sequenceFormatStore.find('id',this.record.data.sequenceformatid);   
            if(index!=-1){
                this.sequenceFormatCombobox.setValue(this.record.data.sequenceformatid); 
                
                if(!this.copyInv){//edit
                    this.sequenceFormatCombobox.disable();
                    this.Number.disable(); 
                }else{//copy case if sequenceformat id hide number
                    this.Number.disable();
                    WtfGlobal.hideFormElement(this.Number);
                }
                
            } else {
                this.sequenceFormatCombobox.setValue("NA"); 
                this.sequenceFormatCombobox.disable();
                this.Number.enable();  
                
                if(this.copyInv){//copy case show number field 
                    this.getNextSequenceNumber(this.sequenceFormatCombobox);
                }
            }
        }else if(!this.isEdit || this.GENERATE_PO||this.GENERATE_SO){// create new,generate so and po case
                 var seqRec=this.sequenceFormatStore.getAt(0)
                this.sequenceFormatCombobox.setValue(seqRec.data.id);
            var count=this.sequenceFormatStore.getCount();
            for(var i=0;i<count;i++){
                var seqRec=this.sequenceFormatStore.getAt(i)
                if(seqRec.json.isdefaultformat=="Yes"){
                    this.sequenceFormatCombobox.setValue(seqRec.data.id) 
                    break;
                }
            }

                this.getNextSequenceNumber(this.sequenceFormatCombobox); 
            }
        }
    },
    initComponent:function(config){
        Wtf.account.TransactionPanelSats.superclass.initComponent.call(this,config);
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
                mode:this.modeName,
                isEdit:this.isEdit
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
        if(!this.isTemplate) {  // this check is added due to avoding issue of sequence number getting incremented in case of template not having create also transaction check (ERP-1518)
        this.sequenceFormatStore.on('load',this.setNextNumber,this);
        }
        
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
            fieldLabel: "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.currency.tt")+"'>"+ WtfGlobal.getLocaleText("acc.currency.cur") +" *"  +"</span>",//    WtfGlobal.getLocaleText("acc.currency.cur"),  //'Currency',
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
            if(!this.GENERATE_PO&&!this.GENERATE_SO){
                this.onCurrencyChangeOnly();
                this.Name.setValue(customer);
                this.Currency.setValue(currency);
            }    
            this.updateFormCurrency();
            if(this.Grid){
                this.Grid.forCurrency = this.Currency.getValue();
            }
                
//            this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.billDate.getValue()),tocurrencyid:this.Currency.getValue()}});
        }, this);
        
        this.Term= new Wtf.form.FnComboBox({
            fieldLabel:(this.isCustomer?"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.invoice.creditTerm.tip")+"'>"+ WtfGlobal.getLocaleText("acc.invoice.creditTerm")+"</span>":"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.invoice.debitTerm.tip")+"'>"+ WtfGlobal.getLocaleText("acc.invoice.debitTerm")+"</span>")+' *',
            itemCls : (this.cash)?"hidden-from-item1":"",  //||this.isOrder
            hideLabel:this.cash,    //||this.isOrder
            id:"creditTerm"+this.heplmodeid+this.id,
            hidden:this.cash,                //||this.isOrder,
            hiddenName:'term',
//            anchor: '93.5%',
            width : 240,
            store:this.termds,
            valueField:'termdays',
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
        
        var isEditORisCopy=(this.isEdit !=undefined ?this.isEdit:false) || (this.copyInv!=undefined ?this.copyInv:false) ||(this.isCopyFromTemplate !=undefined ?this.isCopyFromTemplate:false);
        Wtf.customerAccStore.on('beforeload', function(s,o){
            if(!o.params)o.params={};
            var currentBaseParams = Wtf.customerAccStore.baseParams;
            if(isEditORisCopy){
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

        this.moduleTemplateSection();
        this.postText=(this.record)?this.record.data.posttext:"";
        if(!this.isFromProjectStatusRep){
            if(this.isCustomer){
                Wtf.customerAccStore.load();
            }else{
                Wtf.vendorAccStore.reload();
            }
        }
        
        var isShowOneTime=(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Sales_Order_ModuleId  || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId) && !((this.isEdit !=undefined ?this.isEdit:false) || (this.copyInv !=undefined ?this.copyInv:false) || (this.isCopyFromTemplate !=undefined ?this.isCopyFromTemplate:false) || (this.isTemplate !=undefined ?this.isTemplate:false));      
        this.ShowOnlyOneTime= new Wtf.form.Checkbox({
            name:'ShowOnlyOneTime',
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.cust.ShowOnlyOneTime.tt") +"'>"+ WtfGlobal.getLocaleText("acc.cust.ShowOnlyOneTime")  +"</span>", 
            id:'ShowOnlyOneTime'+this.heplmodeid+this.id,
            checked:false,
            hideLabel:!isShowOneTime, // show only in new case
            hidden:!isShowOneTime,
            cls : 'custcheckbox',
            width: 10
        });    
   
        this.Name= new Wtf.form.ExtFnComboBox({
            fieldLabel:(this.isCustomer)?"<span wtf:qtip='"+  WtfGlobal.getLocaleText("acc.invoiceList.cust.tt") +"'>"+ WtfGlobal.getLocaleText("acc.invoiceList.cust") +"</span>":"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.invoiceList.ven.tt") +"'>"+ WtfGlobal.getLocaleText("acc.invoiceList.ven") +"</span>",//this.isCustomer?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven") , //this.businessPerson+"*",
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
            mode: 'local',
            typeAhead: true,
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            forceSelection: true,
            selectOnFocus:true,
            width : 240,
            triggerAction:'all',
            scope:this            
        });      
        this.Name.on('select',this.onNameSelect,this);
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
            fieldLabel:(this.isCustomer?WtfGlobal.getLocaleText("acc.het.101"):WtfGlobal.getLocaleText("acc.field.DebitAccount*")),
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
            arrfromLink.push(['Sales Order',0]);
            if(Wtf.account.companyAccountPref.withinvupdate){
                arrfromLink.push(['Delivery Order',1]);    
            }
            arrfromLink.push(['Customer Quotation',2]); 
        } else {
            if(this.isOrder){
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
        disabled:(this.isEdit&&!this.copyInv&&!this.isPOfromSO&&!this.isSOfromPO?true:false),  
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
            fieldLabel:(this.isPOfromSO?WtfGlobal.getLocaleText("acc.accPref.autoPO"):(this.isSOfromPO)?WtfGlobal.getLocaleText("acc.accPref.autoSO"):(this.isEdit?this.label:this.label)) + " " + ((this.isTemplate)?'Number':WtfGlobal.getLocaleText("acc.common.number")),  //,  //this.label+' Number*',
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
            anchor:'50%',
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
            allowBlank:true,
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
        this.shipLength=new Wtf.form.NumberField({
            allowNegative:false,
//            allowDecimals:false,
            hidden:!Wtf.account.companyAccountPref.dependentField && (this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId),
            hideLabel:!Wtf.account.companyAccountPref.dependentField && (this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId),
            defaultValue:1,
            id:"shiplengthval"+this.heplmodeid+this.id,
            allowBlank:this.isOrder,
            maxLength: 10,
            width:100,
            fieldLabel:WtfGlobal.getLocaleText("acc.invoiceList.shipLength"),  //'Discount',
            name:'shipLength'
        });
        this.shipLength.setValue(1);
        this.shipLength.on("change",function(){
        var store=this.Grid.getStore();
        var total=store.getCount();
        for(var k=0;k<total;k++){
            var record = store.getAt(k);
            var beforeEditRecordNew=this.Grid.productComboStore.getAt(this.Grid.productComboStore.find('productid',record.data.productid));
            if(beforeEditRecordNew){
                var custValue=beforeEditRecordNew.data.dependentTypeNo;
                if(custValue!="") {
                    Wtf.Ajax.requestEx({
                        url: "ACCGoodsReceipt/getMasterItemPriceFormulaPrice.do",
                        params: {
                            productId: record.data.productid,
                            item: this.shipLength.getValue(),
                            rowid: k
                        }
                    }, this, function(response,res) {
                        var datewiseprice=record.data.rate;
                        var rowidVal=res.params.rowid;
                        for (var i = 0; i < response.data.length; i++) {
                            var dataObj = response.data[i];
                            if(dataObj.pricevalue>0){
                                datewiseprice=dataObj.pricevalue;
                            }
                            this.Grid.getStore().getAt(rowidVal).set("rate", datewiseprice);
                            this.Grid.fireEvent('datachanged',this.Grid);
                        }
                    } , function(){
                
                        });
                } 
                        
            }
        }              
                    
        this.fireEvent('datachanged',this);
            
    },this); 
        this.perDiscount= new Wtf.form.ComboBox({
            labelSeparator:'',
            labelWidth:0,
            triggerAction:'all',
            mode: 'local',
            valueField:'value',
            displayField:'name',
            id: "perdiscount"+this.heplmodeid+this.id,
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
                var count=this.sequenceFormatStoreDo.getCount();
                for(var i=0;i<count;i++){
                    seqRec=this.sequenceFormatStoreDo.getAt(i)
                    if(seqRec.json.isdefaultformat=="Yes"){
                        this.sequenceFormatComboboxDo.setValue(seqRec.data.id) 
                        break;
                    }
                }
                this.getNextSequenceNumberDo(this.sequenceFormatComboboxDo);
             }
         },this);
      if((this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  ))//only load when customer Invoice
          this.sequenceFormatStoreDo.load();
         
       this.sequenceFormatComboboxDo = new Wtf.form.ComboBox({            
        triggerAction:'all',
        mode: 'local',
        fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.Sequenceformat.tip")+"'>"+ WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat")+"</span>",//WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
        valueField:'id',
        displayField:'value',
        store:this.sequenceFormatStoreDo,
        width:240,
        maxLength:2048,
        typeAhead: true,
        forceSelection: true,
        name:'sequenceformatDo',
        hiddenName:'sequenceformatDo',
        hideLabel:!(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  ),
        hidden: !(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  ),
        listeners:{
            'select':{
                fn:this.getNextSequenceNumberDo,
                scope:this
            }
        }
            
    });
    
     this.no=new Wtf.form.TextField({
            fieldLabel:this.isCustomer?"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.DeliveryOrderNumber*.tt") +"'>"+ WtfGlobal.getLocaleText("acc.field.DeliveryOrderNumber*")  +"</span>":"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.GoodsReceiptNumber*.tt") +"'>"+ WtfGlobal.getLocaleText("acc.field.GoodsReceiptNumber*") +"</span>",//this.isCustomer ?WtfGlobal.getLocaleText("acc.field.DeliveryOrderNumber*") :WtfGlobal.getLocaleText("acc.field.GoodsReceiptNumber*") ,
            name:this.isCustomer ? 'numberDo' : 'numberGR',
            scope:this,
            maxLength:45,
            width : 240,
            hiddenName:this.isCustomer ?'numberDo':'numberGR',
            hideLabel:!(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  ),
            hidden: !(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  ),
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
            {name:'amountdue'},
            {name:'costcenterid'},
            {name:'costcenterName'},
            {name:'memo'},
            {name:'shipvia'},
            {name:'fob'},
            {name:'includeprotax',type:'boolean'},
            {name:'salesPerson'},
            {name:'islockQuantityflag'},
            {name:'agent'},
            {name:'termdetails'},
            {name:'gstIncluded'},
            {name:'shiplengthval'},
            {name:'islockQuantityflag'}
        ]);
        this.POStoreUrl = "";
        var closeFlag = true;
        if(this.businessPerson=="Customer"){
            //mode:(this.isCustBill?52:42)
            if(this.quotation){                
                this.POStoreUrl = "ACCPurchaseOrderCMN/getQuotations.do"
            }else if(this.isOrder) {
                this.POStoreUrl = "ACCSalesOrderCMN/getQuotations.do"
                if(this.isSOfromPO){
                    closeFlag = false;
                    this.POStoreUrl = this.isCustBill?"ACCPurchaseOrderCMN/getBillingPurchaseOrders.do":"ACCPurchaseOrderCMN/getPurchaseOrders.do";
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
                closeflag:closeFlag
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.PORec)
        });
        var colModelArray = GlobalColumnModelForReports[this.moduleid];
        if(colModelArray){
           for(var cnt = 0;cnt < colModelArray.length;cnt++){
               var fieldname = colModelArray[cnt].fieldname;
               var newField = new Wtf.data.Field({
                   name:fieldname.replace(".",""),
//                   sortDir:'ASC',
                   type:colModelArray[cnt].fieldtype == 3 ?  'date' : (colModelArray[cnt].fieldtype == 2?'float':'auto'),
                   format:colModelArray[cnt].fieldtype == 3 ?  'y-m-d' : undefined
               });
               this.POStore.fields.items.push(newField);
               this.POStore.fields.map[fieldname]=newField;
               this.POStore.fields.keys.push(fieldname);
           }
           this.POStore.reader = new Wtf.data.KwlJsonReader(this.POStore.reader.meta, this.POStore.fields.items);
       }
        this.fromPO= new Wtf.form.ComboBox({
            triggerAction:'all',
            hideLabel:this.cash|| (this.quotation&&!this.isCustomer) || (this.isOrder && this.isCustBill) || this.isTemplate || (this.isViewTemplate && !this.readOnly),
            hidden:this.cash|| (this.quotation&&!this.isCustomer)|| (this.isOrder && this.isCustBill) || this.isTemplate || (this.isViewTemplate && !this.readOnly),
            mode: 'local',
            valueField:'value',
            displayField:'name',
            disabled:this.isEdit?false:true, 
            store:this.fromPOStore,
            id: "linkToOrder"+this.heplmodeid +this.id,
            fieldLabel:((!this.isCustBill && !this.isOrder && !this.cash)?WtfGlobal.getLocaleText("acc.field.Link"):(this.isOrder && this.isCustomer)? (this.isSOfromPO)?WtfGlobal.getLocaleText("acc.invoice.linkToPO"):(this.quotation ? WtfGlobal.getLocaleText("acc.field.LinktoVendorQuotation") : WtfGlobal.getLocaleText("acc.field.LinktoCustomerQuotation")) :(this.isOrder && !this.isCustomer)?WtfGlobal.getLocaleText("acc.field.Link"): (this.isCustomer?WtfGlobal.getLocaleText("acc.invoice.linkToSO"):WtfGlobal.getLocaleText("acc.invoice.linkToPO"))) ,  //"Link to "+(this.isCustomer?"Sales":"Purchase")+" Order",
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
    if(!(this.isOrder && this.quotation)){ //
         this.isCustomer ? Wtf.salesPersonStore.load() : Wtf.agentStore.load();    
    }

//    this.salesPersonStore

    this.users= new Wtf.form.FnComboBox({            
            triggerAction:'all',
            mode: 'local',
//            selectOnFocus:true,
            valueField:'id',
            displayField:'name',
            id:"salesperson"+this.heplmodeid+this.id,
            store:this.isCustomer ? Wtf.salesPersonStore : Wtf.agentStore,
            addNoneRecord: true,
//            anchor: '94%',
            hidden:true,
            hideLabel:true,
            width : 240,
//            typeAhead: true,
            forceSelection: true,
            fieldLabel: this.isCustomer ? WtfGlobal.getLocaleText("acc.masterConfig.15") : WtfGlobal.getLocaleText("acc.masterConfig.20"),
            emptyText: this.isCustomer ? WtfGlobal.getLocaleText("acc.field.SelectSalesPerson") : WtfGlobal.getLocaleText("acc.field.SelectAgent"),
//            hideLabel:(this.isOrder && this.quotation),
//            hidden:( this.isOrder && this.quotation),
            name:this.isCustomer ? 'salesPerson' : 'agent',
            hiddenName:this.isCustomer ? 'salesPerson' : 'agent'            
        });
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
        
        this.isMaintenanceOrderCheckBox= new Wtf.form.Checkbox({
            name:'isMaintenanceOrder',
            id:'isMaintenanceOrder'+this.heplmodeid+this.id,
            hiddeName:'isMaintenanceOrder',
            fieldLabel:WtfGlobal.getLocaleText("acc.so.ismaintenance"),  
            checked:false,
            hideLabel:!(this.isOrder && this.isCustomer && !this.quotation && Wtf.account.companyAccountPref.leaseManagementFlag),// hide if transaction is not a sales order
            hidden:!(this.isOrder && this.isCustomer && !this.quotation && Wtf.account.companyAccountPref.leaseManagementFlag),// hide if transaction is not a sales order
            cls : 'custcheckbox',
            width: 10,
            listeners:{
                'check':{
                    fn:function(obj,isChecked){
                        if(isChecked){
                            if(this.maintenanceNumberCombo){
                                this.maintenanceNumberComboStore.load();
                                WtfGlobal.showFormElement(this.maintenanceNumberCombo);
//                                this.maintenanceNumberCombo.enable();
                            }
                        }else{
                            if(this.maintenanceNumberCombo){
                                this.maintenanceNumberCombo.setValue("");
                                WtfGlobal.hideFormElement(this.maintenanceNumberCombo);
//                                this.maintenanceNumberCombo.disable();
                            }
                            this.Grid.productComboStore.load();// load normal products
                        }
                    },
                    scope:this
                }
            }
        });

        this.maintenanceNumberComboRecord = new Wtf.data.Record.create([
            {
                name: 'billid'
            },
            {
                name: 'billno'
            }
        ]);

        this.maintenanceNumberComboStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.maintenanceNumberComboRecord),
            url : "ACCSalesOrderCMN/getMaintenanceRequests.do",
            baseParams:{
//                moduleId:this.getModuleId()
            }
        });
        
        this.maintenanceNumberComboStore.on('beforeload',function(s,o){
            if(!o.params)o.params={};
            var currentBaseParams = this.maintenanceNumberComboStore.baseParams;
            currentBaseParams.id=this.Name.getValue();
            this.maintenanceNumberComboStore.baseParams=currentBaseParams;        
        },this); 
        
        if(this.isOrder && this.isCustomer && !this.quotation){// only in case of sales order
            this.maintenanceNumberComboStore.load();
        }
        
        this.maintenanceNumberCombo= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.maintenance.number"),
            id:"maintenanceNumberCombo"+this.heplmodeid+this.id,
            store: this.maintenanceNumberComboStore,
            valueField:'billid',
            displayField:'billno',
            //            disabled:true,
            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.maintenance.number.select"),
            mode: 'local',
            typeAhead: true,
            //            hideLabel:!(this.isOrder && this.isCustomer && !this.quotation),// hide if transaction is not a sales order
            //            hidden:!(this.isOrder && this.isCustomer && !this.quotation),// hide if transaction is not a sales order
            forceSelection: true,
            selectOnFocus:true,
            addNoneRecord: true,
            width : 240,
            triggerAction:'all',
            scope:this,
            listeners:{
                'select':{
                    fn:function(){
                        if(this.maintenanceNumberCombo.getValue() != ""){
                            this.Grid.productComboStore.load({
                                params:{
                                    type:Wtf.producttype.service
                                }
                            });
                        }else{
                            this.Grid.productComboStore.load();
                        }
                    },
                    scope:this            
                }
            }
        });
        
//        this.isMaintenanceOrderCheckBox.on(' check', function(obj,ischecked){
//            alert('sssgg');
//        },this);


          this.users.addNewFn=this.addSalesPerson.createDelegate(this);
        
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
            id:"generateReceipt"+this.heplmodeid+this.id,
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.generateReceipt"),  //'Generate Receipt',
            checked:false,
            hideLabel:(this.quotation || this.isOrder || !this.cash || !this.isCustomer),//|| !this.isCustomer || this.isOrder),
            hidden:(this.quotation || this.isOrder || !this.cash || !this.isCustomer),//|| !this.isCustomer || this.isOrder),
            cls : 'custcheckbox',
            width: 10
        });
        
        
        this.includingGST= new Wtf.form.Checkbox({
            name:'includingGST',
            hidden:true,
            hideLabel:true,
            id:"includingGST"+this.heplmodeid+this.id,
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.includeGST.tooltip")+"'>"+WtfGlobal.getLocaleText("acc.cust.includingGST")+"</span>",
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
        if(this.fromPO.getValue() !=undefined && this.fromPO.getValue()==true&&this.PO.getValue()!=""){
            var message=""
            if(selectedValuesArr.length==includeGstCount&&this.includingGST.getValue()){
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
            this.includingGST.on('check',function(o,newval,oldval){
                if(this.includingGST.getValue()){
                    this.includeProTax.setValue(true);
                    this.includeProTax.disable();
                }else{
                    this.includeProTax.enable();
//                    this.includeProTax.setValue(false);
                }
//                var includeGstCount=0;
//                var excludeGstCount=0;
//                var selectedids=this.PO.getValue();
//                var selectedValuesArr = selectedids.split(',');
//                for(var cntGst=0;cntGst<selectedValuesArr.length;cntGst++){
//                    rec=this.POStore.getAt(this.POStore.find('billid',selectedValuesArr[cntGst]));
//                    if(rec!=undefined){
//                        if(rec.data["gstIncluded"]){
//                            includeGstCount++;
//                        }else if(!rec.data["gstIncluded"]){
//                            excludeGstCount++;
//                        }
//                    }
//                }
////                    this.fromLinkCombo.emptyText = "";
////                    this.fromLinkCombo.clearValue();
//                if(!(selectedValuesArr.length==includeGstCount&&!this.includingGST.getValue())||(selectedValuesArr.length==excludeGstCount&&!this.includingGST.getValue())){
//                    this.PO.clearValue();
//                }
//                    this.PO.store.removeAll();
                var rec=WtfGlobal.searchRecord(this.includeProTax.store, true, 'value');
                if(rec!=null){
                    this.includeProTaxHandler(this.includeProTax,rec,!this.includeProTax.getValue());            
                }
            },this);

        
        
        this.autoGenerateDO= new Wtf.form.Checkbox({
            name:'autogenerateDO',
            id:"autogenerateDO"+this.heplmodeid+this.id,
            fieldLabel:this.isCustomer ? "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.cust.generateDO.tt") +"'>"+ WtfGlobal.getLocaleText("acc.cust.generateDO") +"</span>": "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.vend.generateGR.tt")+"'>"+WtfGlobal.getLocaleText("acc.vend.generateGR") +"</span>",  //'Generate Delivery Order',
            checked: this.isCustomer ? (this.DOSettings != null? this.DOSettings:false) :(this.DOSettings != null ? this.GRSettings:false),
//            hideLabel:(this.isWithInvUpdate == null? true: !this.isWithInvUpdate) || !( his.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  ),
//            hidden:(this.isWithInvUpdate == null? true: !this.isWithInvUpdate) || !( this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  ),
//            hideLabel: !( this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  ),
//            hidden: !( this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  ),
            hideLabel:true,
            hidden:true,    
            cls : 'custcheckbox',
            width: 10
        });        

        this.autoGenerateDO.on('check',function(o,newval,oldval){
        if(newval){ 
            Wtf.serialwindowflag=true;
        }else{
            Wtf.serialwindowflag=false;
        }
        if(Wtf.account.companyAccountPref.showprodserial){//checking in companypreferences
            this.showGridBatch(newval);
        }
         
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
        
        this.includeTaxStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['Yes',true],['No',false]]
        });
        
        this.partialInvoiceStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['Yes',true],['No',false]]
        });
        
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
            name:'includeprotax',
            hiddenName:'includeprotax',
            listeners:{
                'change':{
                    fn:this.includeProTaxHandler,
                    scope:this
                }
            }
        });
        
        this.validTillDate = new Wtf.form.DateField({
            fieldLabel : "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.common.validTill.tt") +"'>"+ WtfGlobal.getLocaleText("acc.common.validTill") +"</span>",  //"Valid Till",
            format : WtfGlobal.getOnlyDateFormat(),
            name : 'validdate',
            id : "validdate"+this.heplmodeid+this.id,
            width : 240,
            hidden : !(this.moduleid==22 || this.moduleid==23),
            hideLabel : !(this.moduleid==22 || this.moduleid==23)
//            anchor:'94%'
        });
        
        this.partialInvoiceCmb= new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            valueField:'value',
            hidden : (this.isCustomer && !this.isCustBill && !this.isOrder && !this.cash) ? false : true,
            hideLabel : (this.isCustomer && !this.isCustBill && !this.isOrder && !this.cash) ? false : true,
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
        var emptyText = WtfGlobal.getLocaleText("acc.field.SelectVQ/SO");
        if(!this.isCustBill){
            if(this.isOrder && !this.isCustomer) {
                emptyText = WtfGlobal.getLocaleText("acc.field.SelectVQ/SO");
            } else if(this.isOrder && this.isCustomer && !this.quotation){// for sales order
                emptyText = WtfGlobal.getLocaleText("acc.field.SelectCQ");
            } else {
                emptyText = Wtf.account.companyAccountPref.withinvupdate? (this.isCustomer? WtfGlobal.getLocaleText("acc.field.SelectSO/DO/CQ") : WtfGlobal.getLocaleText("acc.field.SelectPO/GR/VQ")) : (this.isCustomer? WtfGlobal.getLocaleText("acc.field.SelectSO/CQ") : WtfGlobal.getLocaleText("acc.field.SelectPO/VQ"));
            }
        }
        this.fromLinkCombo= new Wtf.form.ComboBox({
            name:'fromLinkCombo',
            triggerAction:'all',
            hideLabel:(this.isCustBill || (this.isOrder&&this.isCustomer) || this.cash ||this.quotation || this.isTemplate || (this.isViewTemplate&& !this.readOnly))?true:false,
            hidden:(this.isCustBill || (this.isOrder&&this.isCustomer) || this.cash || this.quotation || this.isTemplate || (this.isViewTemplate&& !this.readOnly))?true:false,
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
                hideLabel:this.cash|| (this.quotation&&!this.isCustomer)|| (this.isOrder && this.isCustBill) || this.isTemplate || (this.isViewTemplate&& !this.readOnly),
                hidden:this.cash||(this.quotation&&!this.isCustomer)|| (this.isOrder && this.isCustBill) || this.isTemplate || (this.isViewTemplate&& !this.readOnly),
                displayField:'billno',
                disabled:true,
                emptyText:this.isOrder ? (( this.isCustomer)?(this.quotation?WtfGlobal.getLocaleText("acc.inv.QOe/MN"):WtfGlobal.getLocaleText("acc.field.SelectCQ")) : WtfGlobal.getLocaleText("acc.field.SelectVQ/SO")) : (Wtf.account.companyAccountPref.withinvupdate ? (this.isCustomer?WtfGlobal.getLocaleText("acc.field.SelectSO/DO/CQ"):WtfGlobal.getLocaleText("acc.field.SelectPO/GR/VQ")) : (!this.isCustBill)?(this.isCustomer?WtfGlobal.getLocaleText("acc.field.SelectSO/CQ"):WtfGlobal.getLocaleText("acc.field.SelectPO/VQ")):(this.isCustomer?WtfGlobal.getLocaleText("acc.inv.SOe"):WtfGlobal.getLocaleText("acc.inv.POe"))),
                mode: 'local',
                typeAhead: true,
                selectOnFocus:true,                            
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

        this.billDate= new Wtf.form.DateField({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.he.12") +"'>"+(this.isPOfromSO?WtfGlobal.getLocaleText("acc.accPref.autoPO"):(this.isSOfromPO)?WtfGlobal.getLocaleText("acc.accPref.autoSO"):(this.isEdit?this.label:this.label)) +' '+WtfGlobal.getLocaleText("acc.invoice.date") +"</span>",//(this.isPOfromSO?WtfGlobal.getLocaleText("acc.accPref.autoPO"):(this.isSOfromPO)?WtfGlobal.getLocaleText("acc.accPref.autoSO"):(this.isEdit?this.label:this.label)) +' '+WtfGlobal.getLocaleText("acc.invoice.date"),
            id:"invoiceDate"+this.heplmodeid+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'billdate',
            maxValue:this.isOpeningBalanceOrder?this.getFinancialYRStartDatesMinOne(true):null,
//            anchor:'50%',
            width : 240,
            listeners:{
                'change':{
                    fn:this.updateDueDate,
                    scope:this
                }
            },
            allowBlank:(this.isTemplate&&!this.createTransactionAlso) 
        });
        this.shipDate= new Wtf.form.DateField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.ShipDate.tip") +"'>"+ WtfGlobal.getLocaleText("acc.field.ShipDate")+"</span>",
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
            //fieldLabel: WtfGlobal.getLocaleText("acc.field.ShipVia"),
             fieldLabel: (companyid == PacificTechCompanyId) ?  WtfGlobal.getLocaleText("acc.field.LeadTime"):"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.ShipVia.tt") +"'>"+ WtfGlobal.getLocaleText("acc.field.ShipVia") +"</span>",
            name: 'shipvia',
            id:"shipvia"+this.heplmodeid+this.id,
//            anchor: '94%',
            width : 240,
            maxLength: 255,
            scope: this
        });
     
        this.fob = new Wtf.form.TextField({
            //fieldLabel: WtfGlobal.getLocaleText("acc.field.FOB"),
            fieldLabel: (companyid == PacificTechCompanyId) ?  WtfGlobal.getLocaleText("acc.field.DeliveryTerm"):"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.fob.tip")+"'>"+WtfGlobal.getLocaleText("acc.field.FOB") +"</span>",
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
        this.CostCenter= new Wtf.form.FnComboBox({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.costCenter.tip") +"'>"+ WtfGlobal.getLocaleText("acc.common.costCenter")+"</span>",//WtfGlobal.getLocaleText("acc.common.costCenter"),//"Cost Center",
            hiddenName:"costcenter",
            id:"costcenter"+this.heplmodeid+this.id,
            store: Wtf.FormCostCenterStore,
            valueField:'id',
            displayField:'name',
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

        this.ShowOnlyOneTime.on('check',function(obj,isChecked){
                    this.Name.reset();
                    this.Name.store.load();
        },this); 

        var itemArr=[];
        if(this.isTemplate){
            itemArr.push(this.moduleTemplateName,this.createAsTransactionChk);
        }
            itemArr.push(this.templateModelCombo,this.ShowOnlyOneTime, this.Name,this.invoiceType, this.Currency,{
                layout:'column',
                border:false,
                defaults:{border:false},items:[ {
                    layout:'form',
                    ctCls : (this.cash)?"hidden-from-item1":"",
                    width:215,
                    items:this.fromPO
                },{
                    width:210,
                    layout:'form',
                    ctCls : (this.cash)?"hidden-from-item1":"",
                    labelWidth:50,
                    items:this.fromLinkCombo
               }]},this.PO,this.sequenceFormatCombobox,this.Number,this.billDate,
            this.PORefNo, this.CostCenter,this.isMaintenanceOrderCheckBox,this.maintenanceNumberCombo,this.youtReftxt,this.delytermtxt,this.invoiceTotxt);
          this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId:"northForm"+this.id,
            autoHeight: true,
            parentcompId:this.id,
            moduleid: this.moduleid,
            isEdit: this.isEdit || this.copyInv ,
            record: this.record,
            isViewMode:this.isViewTemplate
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
               excludeLinkedConsignments:true,
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
       if(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) {           
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
           name:'landedInvoiceID',
           hiddenName:'landedInvoiceID',
           hidden: true,
           hideLabel: true,
           triggerAction: 'all',
           forceSelection: true,
           selectOnFocus:true,
           scope:this,
           listeners:{
               'select':{
                   fn:this.handleProductTypeForConsignment,
                   scope:this
               }
           }
       });
       
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
                items:[{
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
                        },this.Memo,this.shipvia, this.fob,this.includingGST,this.includeProTax, this.validTillDate, this.partialInvoiceCmb,this.template,this.templateID,this.users,this.invoiceList,this.generateReceipt,this.autoGenerateDO,this.sequenceFormatComboboxDo,this.no,this.delydatetxt,this.projecttxt,this.depttxt,this.requestortxt,this.mernotxt,this.shipLength]
                    }]
            }, this.tagsFieldset]
            }]
        });
        this.NorthForm.on('render',function(){  
            this.termds.load({
                params: {               
                    cash_Invoice:this.cash
                }
            });
           this.termds.on("load",function(){
               if(this.autoGenerateDO.getValue() ){
                    this.showDO();
                }else{
                    this.hideDO();
                }
                if(this.maintenanceNumberCombo){
                    WtfGlobal.hideFormElement(this.maintenanceNumberCombo);
                }
                if (this.isTemplate && !this.createTransactionAlso) {
                    WtfGlobal.hideFormElement(this.sequenceFormatCombobox);
                    WtfGlobal.hideFormElement(this.Number);
                }
        },this);


        },this);
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
        this.southCalTemp=new Wtf.Panel({  
            border:false,
            baseCls:'tempbackgroundview',
            html:this.tplSummary.apply({subtotal:WtfGlobal.currencyRenderer(0),discount:WtfGlobal.currencyRenderer(0),totalamount:WtfGlobal.currencyRenderer(0),tax:WtfGlobal.currencyRenderer(0),aftertaxamt:WtfGlobal.currencyRenderer(0),totalAmtInBase:WtfGlobal.currencyRenderer(0),termtotal:WtfGlobal.currencyRenderer(0),amountdue:WtfGlobal.currencyRenderer(0)})
        });
        this.helpMessage= new Wtf.Button({
                text:WtfGlobal.getLocaleText("acc.dashboard.help"),  //'Help',
                handler:this.helpmessage,
                scope:this,
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
            name:'includetax',
            hiddenName:'includetax',
            listeners:{
                'select':{
                    fn:this.enabletax,
                    scope:this
                }
            }
        });
        this.Tax= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.invoice.Tax"),  //'Tax',
            id:"tax"+this.heplmodeid+this.id,
            disabled:!this.isEdit,
            hiddenName:'tax',
            anchor: '97%',
            store:this.Grid.taxStore,
            valueField:'prtaxid',
            forceSelection: true,
            displayField:'prtaxname',
//            addNewFn:this.addTax.createDelegate(this),
            scope:this,
            listeners:{
                'select':{
                    fn:this.updateSubtotal,
                    scope:this
                }
            },
            selectOnFocus:true
        });
        
        var prodDetailSouthItems = [this.productDetailsTpl,this.southCenterTpl];
        if(this.IsInvoiceTerm) {
            this.addInvoiceTermGrid(this.isEdit);
            prodDetailSouthItems.push(this.termgrid);
        }
            
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.tax, Wtf.Perm.tax.view))
            this.Tax.addNewFn=this.addTax.createDelegate(this);

        this.southPanel=new Wtf.Panel({
            region:'center',
            border:false,
            disabledClass:"newtripcmbss",
            disabled:this.isViewTemplate,
            style:'padding:0px 10px 10px 10px',
            layout:'column',//layout:'border',//Bug Fixed: 14871[SK] Scrolling issue : changed layout from border to column
            height:(Wtf.isIE?210:150) + (prodDetailSouthItems.length>2 ? 200 : 50),
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
        
        
        this.lastTransPanel = this.isCustomer ? getCustInvoiceTabView(false, lastTransPanelId, '', undefined, true) : getVendorInvoiceTabView(false, lastTransPanelId, '', undefined, true) ;
        
        this.NorthForm.doLayout();
        this.NorthForm.doLayout();
        this.southPanel.doLayout();
        this.POStore.on('load',this.updateSubtotal,this)
        this.DueDate.on('blur',this.dueDateCheck,this);
        this.billDate.on('change',this.onDateChange,this);


        this.setTransactionNumber();
        WtfComMsgBox(29,4,true);
        this.isCustomer?chkcustaccload():chkvenaccload();
        this.ajxUrl = "CommonFunctions/getInvoiceCreationJson.do";
        var params={
            transactiondate:transdate,
            loadtaxstore:true,
            moduleid :this.moduleid,
//            loadpricestore: false,//!(this.isCustBill||this.isExpenseInv),
            loadcurrencystore:true,
            loadtermstore:true
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
    
    onNameSelect:function(combo,rec,index){
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
    },
    
    doOnNameSelect:function(combo,rec,index){
       var customer= this.Name.getValue();
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
       if(WtfGlobal.getModuleId(this)==20){// only in case of sales order
          if(this.isMaintenanceOrderCheckBox.checked)
            this.maintenanceNumberComboStore.load();
       }
       this.setTerm(combo,rec,index);
       this.setSalesPerson(combo,rec,index);
       this.updateData(); 
       this.autoPopulateProducts();     
    },
    
    autoPopulateProducts:function(){
       if(!this.isEdit && !this.copyInv && this.autoPopulateMappedProduct && this.isExpenseInv==false ){  // in edit and copy case dont autopopulate mapped product untill user change the product manually 
        if(this.moduleid==Wtf.Acc_Sales_Order_ModuleId ||this.moduleid==Wtf.Acc_Purchase_Order_ModuleId  ||this.moduleid== Wtf.Acc_Cash_Purchase_ModuleId ||this.moduleid==Wtf.Acc_Cash_Sales_ModuleId ||this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId ||this.moduleid==Wtf.Acc_Invoice_ModuleId||this.moduleid==Wtf.Acc_Customer_Quotation_ModuleId||this.moduleid==Wtf.Acc_Vendor_Quotation_ModuleId){// this.cash ||this.isInvoice
            this.Grid.ProductMappedStore.on('beforeload',function(s,o){
                if(!o.params)o.params={};
                var currentBaseParams = this.Grid.ProductMappedStore.baseParams;
                currentBaseParams.mappedProductRequest=true;
                currentBaseParams.startdate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true));
                currentBaseParams.enddate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false));  
                currentBaseParams.moduleid = this.moduleid;         // Passing Moduleid
                this.Grid.ProductMappedStore.baseParams=currentBaseParams;        
            },this);  
            this.Grid.ProductMappedStore.load({
                params:{
                    mappingProduct:true,
                    affecteduser:this.Name.getValue(),
                    common:'1', 
                    loadPrice:true,
                    mode:22
                }
            })
            this.Grid.ProductMappedStore.on("load",function(){
                this.Grid.affecteduser=this.Name.getValue();
                this.Grid.loadMappedProduct(this.Grid.ProductMappedStore);
            },this);                           
        }else{
            this.ProductGrid.ProductMappedStore.on('beforeload',function(s,o){
                if(!o.params)o.params={};
                var currentBaseParams = this.ProductGrid.ProductMappedStore.baseParams;
                currentBaseParams.mappedProductRequest=true;
                currentBaseParams.startdate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true));
                currentBaseParams.enddate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false));  
                currentBaseParams.moduleid = this.moduleid;         // Passing Moduleid
                this.ProductGrid.ProductMappedStore.baseParams=currentBaseParams;        
            },this);  
            this.ProductGrid.ProductMappedStore.load({
                params:{
                    mappingProduct:true,
                    affecteduser:this.Name.getValue(),
                    common:'1', 
                    loadPrice:true,
                    mode:22
                }
            })
            this.ProductGrid.ProductMappedStore.on("load",function(){
                this.ProductGrid.affecteduser=this.Name.getValue();
                this.ProductGrid.loadMappedProduc(this.ProductGrid.ProductMappedStore);
            },this);                              
        }                                 
    }else{//Normal Case
        if(this.isOrder ||this.cash ||this.isInvoice){
            this.Grid.productComboStore.load({
                params:{
                    mappingProduct:true,
                    customerid:this.Name.getValue(),
                    common:'1', 
                    loadPrice:true,
                    mode:22
                }
            })
        }else{
            this.ProductGrid.productComboStore.load({
                params:{
                    mappingProduct:true,
                    customerid:this.Name.getValue(),
                    common:'1', 
                    loadPrice:true,
                    mode:22
                }
            })
        }
     } 
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
                moduleId:WtfGlobal.getModuleId(this)
            }
        });
        
        this.moduleTemplateStore.on('load', function(store){
            if(this.isCopyFromTemplate && this.templateId!= undefined ){
                this.templateModelCombo.setValue(this.templateId);
            }     
        },this);
        
        
        this.templateModelCombo= new Wtf.form.FnComboBox({
            fieldLabel:(this.isViewTemplate?WtfGlobal.getLocaleText("acc.designerTemplateName"): WtfGlobal.getLocaleText("acc.field.SelectTemplate")),
            id:"templateModelCombo"+this.heplmodeid+this.id,
            store: this.moduleTemplateStore,
            valueField:'templateId',
            displayField:'templateName',
            hideTrigger:this.isViewTemplate,
            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.invoice.grid.template.emptyText"),
            mode: 'local',
            typeAhead: true,
            hidden:this.isTemplate || this.quotation,
            hideLabel:this.isTemplate || this.quotation,
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
                                msg : WtfGlobal.getLocaleText("acc.msgbox.50")
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
                          this.resetCustomFields();
                          this.loadStore();
                      }
                    },
            scope:this            
                }
            }
        });
        
      
    this.invoiceTypeStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'id',
            type:'string'
        }, 'name'],
        data :[["ff808081434d75f2014351835fc70003","Ad-Hoc Invoice"],
        ["ff808081434d75f201435182a6270002","Marine Invoice"],
        ["ff808081434d75f20143518400630005","Retail Invoice-Fixed"],       
        ["ff808081434d75f20143518438fe0006","Retail Invoice-Variable"],
        ["ff808081434d75f201435183b3270004","Visitor's Pass Invoice"],
        ["ff808081434d75f201435183b3270007","Water Sale Invoice"],
        ["ff808081434d75f20143518400630008","Car Park Operator"],
        ["ff808081434d75f20143518400630009","Security Officer"],
        ["ff808081434d75f20143518400630010","Event"]]
    });
    var invoivetypeFieldLabel="";
    if(this.moduleid==Wtf.Acc_Customer_Quotation_ModuleId){  //customer quotation
        invoivetypeFieldLabel=WtfGlobal.getLocaleText("acc.customerquotation.type");
    }else if(this.moduleid==Wtf.Acc_Vendor_Quotation_ModuleId)  //  //vendor quotation
    {
        invoivetypeFieldLabel=WtfGlobal.getLocaleText("acc.vendorquotation.type");      
    }else if(this.moduleid==Wtf.Acc_Sales_Order_ModuleId)   //sales order
    {
        invoivetypeFieldLabel=WtfGlobal.getLocaleText("acc.salesorder.type");
        
    }else if(this.moduleid==Wtf.Acc_Purchase_Order_ModuleId)  //purchase order
    {
        invoivetypeFieldLabel=WtfGlobal.getLocaleText("acc.purchaseorder.type");
    }else {                                                  //invoice
        invoivetypeFieldLabel=WtfGlobal.getLocaleText("acc.invoice.type"); 
    }
    
    this.invoiceType=new Wtf.form.ComboBox({
        
        fieldLabel:invoivetypeFieldLabel, 
        name:'invoicetype',
        store:this.invoiceTypeStore,
        valueField:'id',
        displayField:'name',
        mode: 'local',
        width : 240,
        allowBlank:!Wtf.account.companyAccountPref.dependentField && (this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId),
        hidden:(!Wtf.account.companyAccountPref.dependentField && (this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId)),
        hideLabel:(!Wtf.account.companyAccountPref.dependentField && (this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId)),
        listWidth:400,
        hiddenName:'invoicetype',
        emptyText:'Select Invoice Type',
        forceSelection:true,
        triggerAction:'all'
    });
    
    this.invoiceType.on("select",function(store,rec){
        var invoicetype="";
        invoicetype=(rec.data.id!=null)?rec.data.id:"";
        this.loadProductGrid(invoicetype);
    },this);  
    
        this.moduleTemplateName = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.campaigndetails.campaigntemplate.templatename"),
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
            fieldLabel : WtfGlobal.getLocaleText("acc.field.CreateTransactionAlso"),
            name:'createAsTransactionChkbox',
            hidden:!this.isTemplate,
            hideLabel:!this.isTemplate,
            cls : 'custcheckbox',
            width : 10
        });
        
        this.createAsTransactionChk.on('check', function(){
            if(this.createAsTransactionChk.getValue()){
                this.createTransactionAlso = true;
                WtfGlobal.showFormElement(this.sequenceFormatCombobox);
                WtfGlobal.showFormElement(this.Number);
                var seqRec=this.sequenceFormatStore.getAt(0)
                this.sequenceFormatCombobox.setValue(seqRec.data.id);
                var count=this.sequenceFormatStore.getCount();
                for(var i=0;i<count;i++){
                    seqRec=this.sequenceFormatStore.getAt(i)
                    if(seqRec.json.isdefaultformat=="Yes"){
                        this.sequenceFormatCombobox.setValue(seqRec.data.id) 
                        break;
                    }
                }
                this.getNextSequenceNumber(this.sequenceFormatCombobox);
                this.Number.allowBlank = false;
                this.Number.enable();
                this.sequenceFormatCombobox.enable();
                this.billDate.enable();
                this.autoGenerateDO.enable();
                this.generateReceipt.enable();
                this.setTransactionNumber();
                this.billDate.setValue(Wtf.serverDate);
            }else{
                this.createTransactionAlso = false;
                this.Number.disable();
                this.sequenceFormatCombobox.disable();                
                this.billDate.disable();
                this.autoGenerateDO.setValue(false);
                this.autoGenerateDO.disable();
                this.generateReceipt.setValue(false);
                this.generateReceipt.disable();
                this.sequenceFormatCombobox.reset();
                this.Number.setValue('');
                this.Number.allowBlank = true;
                WtfGlobal.hideFormElement(this.sequenceFormatCombobox);
                WtfGlobal.hideFormElement(this.Number);
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
        {name:'includeprotax',type:'boolean'},
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
        {name:'agent'},
        {name:'shiplengthval'},
        {name:'termdetails'}
        
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
   },
   
   doOnDateChanged:function(val,oldval){
        this.val=val;
        this.oldval=oldval;
//        this.loadTax(val);
        this.externalcurrencyrate=0;
        this.custdatechange=true;
        this.datechange=1;
        this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.billDate.getValue()),tocurrencyid:this.Currency.getValue()}});
        if(!(this.isCustBill||this.isExpenseInv)) {
            var affecteduser = this.Name.getValue();
//            this.Grid.loadPriceStoreOnly(val,this.Grid.priceStore, affecteduser);
//            this.Grid.setGridProductValues(true, false);
        }
            
        else{
            this.changeCurrencyStore();
            this.updateSubtotal();
            this.applyCurrencySymbol();
            var subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol)
            var discount=WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol)
            var totalamount=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol)
            var tax=WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol);
            var aftertaxamt=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax()+this.findTermsTotal(),this.symbol);
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
             if(this.Currency.getValue() !="" && WtfGlobal.searchRecord(this.currencyStore,this.Currency.getValue(),"currencyid") == null){
//            if(this.currencyStore.getCount()<=1){
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
                if(this.record.data.taxid!=undefined && this.record.data.taxid!=null && this.record.data.taxid!="" ){
                    this.isTaxable.enable();
                    this.isTaxable.setValue(true);
                    this.Tax.setValue(this.record.data.taxid);
                }else {
                    this.isTaxable.setValue(false);
                    this.Tax.setValue("");
                }
            }
            if(!((this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) || (this.moduleid == Wtf.Acc_Invoice_ModuleId && !this.cash))) {
                if(this.isEdit || this.copyInv)this.getTerm();
            }
            if(this.isEdit || this.copyInv)this.loadRecord();            
            this.hideLoading();
        if(this.isExpenseInv){
            if(this.Grid.accountStore.getCount()<=1){
                this.Grid.accountStore.on("load",function(){
                    if(!this.saveOnlyFlag){                      
                        this.loadDetailsGrid();  
                    }
                },this);
            }else{
                this.loadDetailsGrid();
            }
                        
        }else{
            this.Grid.productComboStore.on("load",function(){
                this.loadDetailsGrid();
            },this);
            
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

    loadDetailsGrid:function(){
            if(this.isEdit && !this.isOrder && !this.quotation ){
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
         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Failtoloadtherecords")+" "+response.msg], 2);
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
        if(currency!=""||this.custChange)
            this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.val),tocurrencyid:this.Currency.getValue()}});
        else
            this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.val)}});
    },
    changeTemplateSymbol:function(){ 

        if(this.loadCurrFlag && Wtf.account.companyAccountPref.currencyid){
            this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
            this.invoiceType.setValue("ff808081434d75f2014351835fc70003");
            this.loadCurrFlag = false;
        }
        
     /*if date of without inventory changes. price store will not be loaded in this case.[PS]*/
        if(this.isCustBill||this.isExpenseInv){
             if(this.Currency.getValue() !="" && WtfGlobal.searchRecord(this.currencyStore,this.Currency.getValue(),"currencyid") == null){
//            if(this.currencyStore.getCount()==0){
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
//                if(this.currencyStore.getCount()==0){
                    callCurrencyExchangeWindow();
                    str= WtfGlobal.getLocaleText("acc.field.andpriceof")+" <b>"+str+"</b>";
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleasesetthecurrencyrate")+" "+str+WtfGlobal.getLocaleText("acc.field.fortheselecteddate")+"<b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                    this.billDate.setValue("");
                    //                if(this.oldval!=""||this.oldval!=undefined){
                    //                    if(!this.isCustBill)
                    //                        this.Grid.loadPriceStoreOnly(this.oldval,this.Grid.priceStore);
                    //                    this.Grid.taxStore.load({params:{transactiondate:this.oldval}});
                    //                }
            } else {
                    this.updateFormCurrency();
                    if(this.pronamearr!=undefined&&this.pronamearr.length>0){
                        str=WtfGlobal.getLocaleText("acc.field.priceof")+" <b>"+str+"</b>";
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleasesetthe")+" "+str+WtfGlobal.getLocaleText("acc.field.fortheselecteddate")+"<b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);}
                    this.Grid.pronamearr=[];
                    this.updateFormCurrency();
            }
            this.datechange=0;
            this.updateSubtotal();
            this.applyCurrencySymbol();
            var subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol)
            var discount=WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol)
            var totalamount=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol)
            var tax=WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol);
            var calTermTotal = WtfGlobal.addCurrencySymbolOnly(this.findTermsTotal(),this.symbol);
            var aftertaxamt=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax()+this.findTermsTotal(),this.symbol);
            var totalAmtInBase=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol());
            this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:subtotal,discount:discount,totalamount:totalamount,tax:tax,termtotal:calTermTotal,aftertaxamt:aftertaxamt,totalAmtInBase:totalAmtInBase,amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())});
        }
        
        if(this.currencychanged){
           if(this.Currency.getValue() !="" && WtfGlobal.searchRecord(this.currencyStore,this.Currency.getValue(),"currencyid") == null){
//            if(this.currencyStore.getCount()<1){
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
//            if(this.currencyStore.getCount()==0){
                callCurrencyExchangeWindow();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+" <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                this.Name.setValue("");
            } else{
                this.Currency.setValue(this.currencyid);
		this.invoiceType.setValue("ff808081434d75f2014351835fc70003");
                if(this.isEdit){
                    this.Number.setValue(this.number);           
                }
            
            
                this.updateFormCurrency();}
            this.custChange=false;
        }
        this.Grid.pronamearr=[];
    },
    updateFormCurrency:function(){
       this.applyCurrencySymbol();
       var calTermTotal = WtfGlobal.addCurrencySymbolOnly(this.findTermsTotal(),this.symbol);
       this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol),discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol),termtotal:calTermTotal,aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax()+this.findTermsTotal(),this.symbol),totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol()),amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())});
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
       if(this.Grid){
            this.Grid.billDate = this.billDate.getValue()
        }
        this.billid=this.record.data.billid;
        var mode=this.isCustBill?17:14;
        this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;
        this.Grid.getStore().load({params:{bills:this.billid,mode:mode,isexpenseinv:this.isExpenseInv,isCopyInvoice:this.copyInv}});
        this.EditisAutoCreateDO=false;
      if(this.isEdit && (this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  ) || this.copyInv)
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
                  if(this.copyInv){
                        this.autoGenerateDO.setValue(true);
                        var sequenceformatid=response.data[0].sequenceformatDo;
                        if(sequenceformatid=="NA" || sequenceformatid==undefined){
                            this.sequenceFormatComboboxDo.setValue("NA"); 
                            this.sequenceFormatComboboxDo.disable();
                            this.getNextSequenceNumberDo(this.sequenceFormatComboboxDo);
                            this.no.setValue("");
                        } else{
                            var index=this.sequenceFormatStoreDo.find('id',sequenceformatid);
                            if(index!=-1){
                                this.sequenceFormatComboboxDo.setValue(sequenceformatid);                                               
                            }else{  //sequence format get deleted then NA is set
                                this.sequenceFormatComboboxDo.setValue("NA");  
                            } 
                            this.getNextSequenceNumberDo(this.sequenceFormatComboboxDo);//need to show next number in number because it is not hidden.
                        }
                  }else if(this.isEdit){
                        this.EditisAutoCreateDO=true;
                        this.sequenceFormatComboboxDo.setValue(response.data[0].sequenceformatDo)
                        this.no.setValue(response.data[0].SequenceNumDO);
                        this.DeliveryOrderid=response.data[0].DeliveryOrderID;
                        if(this.DeliveryOrderid != "" && this.DeliveryOrderid != null && this.DeliveryOrderid!= undefined){
                            this.autoGenerateDO.setValue(true);
                        }else{
                            this.autoGenerateDO.setValue(false);
                        }
                        this.sequenceFormatComboboxDo.disable();
                        this.autoGenerateDO.disable();
                        WtfGlobal.showFormElement(this.sequenceFormatComboboxDo);
                        WtfGlobal.showFormElement(this.no);
                   }                    
                }else{
                    if(this.isEdit){
                        this.autoGenerateDO.setValue(false);
                         if(this.isExpenseInv){
                                this.autoGenerateDO.disable();
                            }
                    }
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
            
            this.Grid.getStore().load({params:{bills:this.billid}});
            
    },
    
    loadEditableGridForQuotation:function(){
        if (!this.isCustomer) {
            this.subGridStoreUrl = "ACCPurchaseOrderCMN/getQuotationRows.do";
        }else{
            this.subGridStoreUrl = "ACCSalesOrderCMN/getQuotationRows.do";
        }
        if(!this.isCustomer && this.PR_IDS) {
            this.Grid.getStore().proxy.conn.url = "ACCPurchaseOrderCMN/getRequisitionRows.do";
            this.Grid.getStore().load({params:{bills:this.PR_IDS}});
            
            // reset to original config
            this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;  
            this.Grid.getStore().params = {bills:this.billid}
        } else {
            if(this.record != undefined)
            this.billid=this.record.data.billid;
            this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;  
            this.Grid.getStore().load({params:{bills:this.billid,copyInvoice:this.copyInv}});
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
//                title: WtfGlobal.getLocaleText("acc.invoice.inventory"),  //'Inventory',
//                border:true,
//                //cls:'gridFormat',
//                helpedit:this.heplmodeid,
//                moduleid: this.moduleid,
//                id:this.id+"editproductdetailsgrid",
//                viewConfig:{forceFit:true},
//                isCustomer:this.isCustomer,
//                parentCmpID:this.id,
//                currencyid:this.currencyid,
//                disabledClass:"newtripcmbss",
//                fromOrder:true,
//                isEdit:this.isEdit,
//                isOrder:this.isOrder,
//                isInvoice:this.isInvoice,
//                isQuotation:this.quotation,
//                isFromGrORDO:this.isFromGrORDO,
//                forceFit:true,
//                isCash:this.cash,
//                loadMask : true,
//                parentObj :this
//            }); 
//        }else
        {
           this.ProductGrid=new Wtf.account.ProductDetailsGridSats({
                height: 300,//region:'center',//Bug Fixed: 14871[SK]
                layout:'fit',
                title: WtfGlobal.getLocaleText("acc.invoice.inventory"),  //'Inventory',
                border:true,
                //cls:'gridFormat',
                helpedit:this.heplmodeid,
                moduleid: this.moduleid,
                id:this.id+"editproductdetailsgrid",
                viewConfig:{forceFit:true},
                isCustomer:this.isCustomer,
                currencyid:this.currencyid,
                disabledClass:"newtripcmbss",
                isFromGrORDO:this.isFromGrORDO,
                parentCmpID:this.id,
                fromOrder:true,
                editTransaction:this.isEdit,
                isOrder:this.isOrder,
                isInvoice:this.isInvoice,
                isQuotation:this.quotation,
                isRequisition:this.isRequisition,
                forceFit:true,
                isCash:this.cash,
                loadMask : true,
                viewGoodReceipt: this.viewGoodReceipt,
                parentObj :this
            }); 
        }
        this.ProductGrid.productComboStore.load();
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
//                    viewConfig:{forceFit:true},
//                    disabledClass:"newtripcmbss",
//                    disabled:this.isViewTemplate,
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
        }else{    //With Inventory[PS]
            if(this.isEdit && !this.isOrder){
                if(this.isExpenseInv){
                    this.ExpenseGrid=new Wtf.account.ExpenseInvoiceGrid({
                        height: 200,
                        //layout : 'fit',
                        border:true,
                        cls:'gridFormat',
//                        title: WtfGlobal.getLocaleText("acc.invoice.gridExpenseTab"),//'Expense',
                        viewConfig:{forceFit:true},
                        isCustomer:this.isCustomer,
                        parentCmpID:this.id,
                        editTransaction:this.isEdit,
                        moduleid: this.moduleid,
                        isCustBill:this.isCustBill,
                        disabledClass:"newtripcmbss",
                        disabled:this.isViewTemplate,
                        id:this.id+"expensegrid",
                        currencyid:this.Currency.getValue(),
                        fromOrder:true,
                        isOrder:this.isOrder,
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
//                            viewConfig:{forceFit:true},
//                            parentCmpID:this.id,
//                            autoScroll:true,
//                            editTransaction:true,
//                            disabledClass:"newtripcmbss",
//                            disabled:this.isViewTemplate,
//                            record:this.record,
//                            copyInv:this.copyInv,
//                            fromPO:false,
//                            readOnly: this.readOnly,
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
                        this.Grid=new Wtf.account.ProductDetailsGridSats({
                            //region:'center',//Bug Fixed: 14871[SK]
                            height: 300,//region:'center',//Bug Fixed: 14871[SK]
                            cls:'gridFormat',
                            layout:'fit',
                            moduleid: this.moduleid,
                            id:this.id+"productdetailsgrid",
                            isCash:this.cash,
                            viewConfig:{forceFit:true},
                            autoScroll:true,
                            editTransaction:true,
                            disabledClass:"newtripcmbss",
                            disabled:false,//(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId)?false:this.isViewTemplate,
                            isFromGrORDO:this.isFromGrORDO,
                            record:this.record,
                            copyInv:this.copyInv,
                            parentCmpID:this.id,
                            fromPO:false,
                            readOnly: this.readOnly,
                            isEdit:this.isEdit,
                            isCN:false,
                            isCustomer:this.isCustomer,
                            isOrder:this.isOrder,
                            isInvoice:this.isInvoice,
                            isQuotation:this.quotation,
                            loadMask : true,
                            parentObj :this,
                            viewGoodReceipt: this.viewGoodReceipt
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
//                            viewConfig:{forceFit:true},
//                            record:this.record,
//                            isQuotation:this.quotation,
//                            isQuotationFromPR : this.isQuotationFromPR,
//                            isCustomer:this.isCustomer,
//                            currencyid:this.currencyid,
//                            disabledClass:"newtripcmbss",
//                            disabled:this.isViewTemplate,
//                            isFromGrORDO:this.isFromGrORDO,
//                            fromPO:this.isOrder,
//                            fromOrder:true,
//                            isEdit:this.isEdit,
//                            isOrder:this.isOrder,
//                            forceFit:true,
//                            editTransaction: this.isEdit,
//                            loadMask : true,
//                            readOnly:this.readOnly,
//                            parentObj :this
//                        });
//                    }else
                    {
                        this.Grid=new Wtf.account.ProductDetailsGridSats({
                            //region:'center',//Bug Fixed: 14871[SK]
                            height: 300,//region:'center',//Bug Fixed: 14871[SK]
                            cls:'gridFormat',
                            layout:'fit',
                            parentCmpID:this.id,
                            moduleid: this.moduleid,
                            id:this.id+"editproductdetailsgrid",
                            isCash:this.cash,
                            viewConfig:{forceFit:true},
                            record:this.record,
                            isQuotation:this.quotation,
                            isQuotationFromPR : this.isQuotationFromPR,
                            isCustomer:this.isCustomer,
                            currencyid:this.currencyid,
                            disabledClass:"newtripcmbss",
                            disabled:this.isViewTemplate,
                            fromPO:this.isOrder,
                            fromOrder:true,
                            isEdit:this.isEdit,
                            isFromGrORDO:this.isFromGrORDO,
                            isOrder:this.isOrder,
                            isInvoice:this.isInvoice,
                            forceFit:true,
                            editTransaction: this.isEdit,
                            loadMask : true,
                            readOnly:this.readOnly,
                            viewGoodReceipt: this.viewGoodReceipt,
                            parentObj :this
                        });
                    }
                    this.Grid.on("productselect", this.loadTransStore, this);
                    this.Grid.on("productdeleted", this.removeTransStore, this);
                  }else{
//                      this.ProductGrid=new Wtf.account.ProductDetailsGrid({
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
                        height: 200,
                        //layout : 'fit',
                        border:true,
                        title: WtfGlobal.getLocaleText("acc.invoice.gridExpenseTab"),//'Expense',
                        viewConfig:{forceFit:true},
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
                        loadMask : true,
                        parentObj :this
                    });
                    this.GridPanel= new Wtf.TabPanel({
                        id : this.id+'invoicegrid',
                        iconCls:'accountingbase coa',
                        disabled:this.isViewTemplate,
                        border:false,
                        style:'padding:10px;',
                        disabledClass:"newtripcmbss",
                        cls:'invgrid',
                        //cls:'gridFormat',
                        activeTab:0,
                        height: 200,
                        //region : 'center',
                        //layout : 'fit',
                        
                        items: [this.ProductGrid,this.ExpenseGrid]
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

  if(this.isEdit && this.record.data.invoicetype!=""){
                if(this.record.data.invoicetype=='ff808081434d75f20143518438fe0006'){
                    this.Grid.calculatePercentage=true;
                }
            } else{
                 this.Grid.calculatePercentage=false;
            }
                  
        this.NorthForm.on('render',this.setDate,this);
        if(this.isViewTemplate){
            this.setdisabledbutton();
        }
        this.Grid.on('datachanged',this.updateSubtotal,this);
        this.Grid.getStore().on('load',function(store, recArr){
            if(!this.isOrder && !this.quotation && this.isCustomer && this.copyInv && !this.isViewTemplate){
            this.confirmMsg = "";
            if(!Wtf.account.companyAccountPref.withinvupdate){
                for(var i=0; i<recArr.length; i++){
                    if(recArr[i].data.productid !== undefined){
                        var index=this.ProductGrid.productComboStore.find('productid',recArr[i].data.productid);
                        var prorec=this.ProductGrid.productComboStore.getAt(index);
                        if(recArr[i].data['quantity'] > this.ProductGrid.productComboStore.getAt(index).data['quantity'] && prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part'){
                            this.confirmMsg += WtfGlobal.getLocaleText("acc.field.MaximumavailableQuantityforProduct")+this.ProductGrid.productComboStore.getAt(index).data['productname']+WtfGlobal.getLocaleText("acc.field.is")+this.ProductGrid.productComboStore.getAt(index).data['quantity']+".<br>";
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"),this.confirmMsg+WtfGlobal.getLocaleText("acc.ven.msg4"),function(btn){
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
            this.updateSubtotal();
                this.Grid.addBlank(store);
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
                if(prorec.data.type=="Service"){
                    this.productDetailsTpl.hide();
                }else{
                    this.productDetailsTpl.show();
                }
                }
            }                            
        },this);
    },   
     setdisabledbutton:function(){   
        this.moduleTemplateName.setDisabled(true);
        this.templateModelCombo.setDisabled(true);
        this.ShowOnlyOneTime.enable();
        this.Currency.setDisabled(true);
        this.PO.setDisabled(true);
        this.sequenceFormatCombobox.setDisabled(true);
        this.Number.setDisabled(true);
        this.billDate.setDisabled(true);
        this.PORefNo.setDisabled(true);
        this.autoGenerateDO.setDisabled(true);
        this.CostCenter.setDisabled(true);
        this.youtReftxt.setDisabled(true);
        this.delytermtxt.setDisabled(true);
        this.invoiceTotxt.setDisabled(true);
        this.shipDate.setDisabled(true);
        this.Term.setDisabled(true);
        this.DueDate.setDisabled(true);
        this.shipvia.setDisabled(true);
        this.fob.setDisabled(true);
        this.includeProTax.setDisabled(true);
        this.validTillDate.setDisabled(true);
        this.partialInvoiceCmb.setDisabled(true);
        this.template.setDisabled(true);
        this.templateID.setDisabled(true);
        this.users.setDisabled(true);
        this.generateReceipt.setDisabled(true);
        this.autoGenerateDO.setDisabled(true);
        this.sequenceFormatComboboxDo.setDisabled(true);
        this.no.setDisabled(true);
        this.delydatetxt.setDisabled(true);
        this.projecttxt.setDisabled(true);
        this.depttxt.setDisabled(true);
        this.requestortxt.setDisabled(true);
        this.mernotxt.setDisabled(true);
        this.Name.setDisabled(true);
//        this.tagsFieldset.setDisabled(true);
//        this.pmtMethod.setDisabled(true);
//        this.pmtMethodAcc.setDisabled(true); 
        this.fromLinkCombo.setDisabled(true);    
        this.fromPO.setDisabled(true); 
        this.isMaintenanceOrderCheckBox.setDisabled(true);
        this.lockQuantity.setDisabled(true);
        this.invoiceList.setDisabled(true);
    },
        loadProductGrid : function(invoicetype){
        if(Wtf.account.companyAccountPref.dependentField){ 
            if(invoicetype=='ff808081434d75f20143518438fe0006'){
                var productGridCM = this.Grid.colModel.config;
                this.Grid.calculatePercentage=true;
                for(var cmcnt=0;cmcnt<productGridCM.length;cmcnt++){
                    if(productGridCM[cmcnt].dataIndex=='productid'){
                        productGridCM[cmcnt].header="Product";
                    }else if(productGridCM[cmcnt].dataIndex=='desc'){
                        productGridCM[cmcnt].header="Description";
                    }else if(productGridCM[cmcnt].dataIndex=='showquantity'){
                        productGridCM[cmcnt].header="%";
                    }else if(productGridCM[cmcnt].dataIndex=='rate'){
                        productGridCM[cmcnt].header="Gross sales";
                    }else if(productGridCM[cmcnt].dataIndex=='dependentType'){
                        productGridCM[cmcnt].hidden=true;
                    }
                }
         
            }else if(invoicetype=='ff808081434d75f201435183b3270007'){
                var productGridCM = this.Grid.colModel.config;
                this.Grid.calculatePercentage=true;
                for(var cmcnt=0;cmcnt<productGridCM.length;cmcnt++){
                    if(productGridCM[cmcnt].dataIndex=='productid'){
                        productGridCM[cmcnt].header="Product";
                    }else if(productGridCM[cmcnt].dataIndex=='desc'){
                        productGridCM[cmcnt].header="Description";
                    }else if(productGridCM[cmcnt].dataIndex=='showquantity'){
                        productGridCM[cmcnt].header="Volume of Water";
                    }else if(productGridCM[cmcnt].dataIndex=='rate'){
                        productGridCM[cmcnt].header="Price per kilo litre";
                    }else if(productGridCM[cmcnt].dataIndex=='dependentType'){
                        productGridCM[cmcnt].hidden=true;
                    }
                }
         
            }
             else  if(invoicetype=='ff808081434d75f20143518400630005' || invoicetype=='ff808081434d75f20143518400630008'){
                var productGridCM = this.Grid.colModel.config;
                for(var cmcnt=0;cmcnt<productGridCM.length;cmcnt++){
                    if(productGridCM[cmcnt].dataIndex=='productid'){
                        productGridCM[cmcnt].header="Product";
                    }else if(productGridCM[cmcnt].dataIndex=='desc'){
                        productGridCM[cmcnt].header="Description";
                    }else if(productGridCM[cmcnt].dataIndex=='showquantity'){
                        productGridCM[cmcnt].header="No. of month";
                    }else if(productGridCM[cmcnt].dataIndex=='rate'){
                        productGridCM[cmcnt].header="License Fee";
                    }else if(productGridCM[cmcnt].dataIndex=='dependentType'){
                        productGridCM[cmcnt].hidden=true;
                    }
                }
            }else  if(invoicetype=='ff808081434d75f20143518400630009'){
                var productGridCM = this.Grid.colModel.config;
                this.Grid.calculatePercentage=false;
                var productGridCM = this.Grid.colModel.config;
                for(var cmcnt=0;cmcnt<productGridCM.length;cmcnt++){
                    if(productGridCM[cmcnt].dataIndex=='productid'){
                        productGridCM[cmcnt].header="Item";
                    }if(productGridCM[cmcnt].dataIndex=='desc'){
                        productGridCM[cmcnt].header="Item Description";
                    }else if(productGridCM[cmcnt].dataIndex=='showquantity'){
                        productGridCM[cmcnt].header="No. of hours";
                    }else if(productGridCM[cmcnt].dataIndex=='rate'){
                        productGridCM[cmcnt].header="Cost per hour";
                    }else if(productGridCM[cmcnt].dataIndex=='dependentType'){
                        productGridCM[cmcnt].hidden=true;
                    }
                }
            }else  if(invoicetype=='ff808081434d75f20143518400630010'){
                var productGridCM = this.Grid.colModel.config;
                this.Grid.calculatePercentage=false;
                var productGridCM = this.Grid.colModel.config;
                for(var cmcnt=0;cmcnt<productGridCM.length;cmcnt++){
                    if(productGridCM[cmcnt].dataIndex=='showquantity'){
                        productGridCM[cmcnt].header="Quantity";
                    }else if(productGridCM[cmcnt].dataIndex=='productid'){
                        productGridCM[cmcnt].header="Item";
                    }else if(productGridCM[cmcnt].dataIndex=='desc'){
                        productGridCM[cmcnt].header="Item Description";
                    }else if(productGridCM[cmcnt].dataIndex=='rate'){
                        productGridCM[cmcnt].header="Unit Price";
                    }else if(productGridCM[cmcnt].dataIndex=='dependentType'){
                        productGridCM[cmcnt].hidden=true;
                    }
                }
            }else{
                var productGridCM = this.Grid.colModel.config;
                this.Grid.calculatePercentage=false;
                var productGridCM = this.Grid.colModel.config;
                for(var cmcnt=0;cmcnt<productGridCM.length;cmcnt++){
                     if(productGridCM[cmcnt].dataIndex=='productid'){
                        productGridCM[cmcnt].header="Product";
                    }else if(productGridCM[cmcnt].dataIndex=='desc'){
                        productGridCM[cmcnt].header="Description";
                    }else if(productGridCM[cmcnt].dataIndex=='showquantity'){
                        productGridCM[cmcnt].header="Quantity";
                    }else if(productGridCM[cmcnt].dataIndex=='rate'){
                        productGridCM[cmcnt].header="Unit Price";
                    }else if(productGridCM[cmcnt].dataIndex=='dependentType'){
                        productGridCM[cmcnt].hidden=false;
                    }
                }
            }
            this.Grid.getView().refresh(true);
        }
    },
    beforeTabChange:function(a,newTab,currentTab){
    	if(currentTab!=null && newTab!=currentTab){
            if(!this.isExpenseInv){
                this.autoGenerateDO.setValue(false);
                this.autoGenerateDO.disable();
                this.includingGST.setValue(false);
                this.includingGST.disable();
             }else{
                this.autoGenerateDO.reset();
                this.autoGenerateDO.enable();
                this.includingGST.reset();
                this.includingGST.enable();
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
            if(this.southCalTemp.body!=undefined)
                 this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol),discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol),aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax(),this.symbol),totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol()),amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())});
    },
    caltax:function(){
        var totalamount=this.calTotalAmount();
        var rec= this.Grid.taxStore.getAt(this.Grid.taxStore.find('prtaxid',this.Tax.getValue()));
        var taxamount=0;
        if(rec!=null){
            totalamount=getRoundedAmountValue(this.calTotalAmount());
            taxamount=(totalamount*rec.data["percent"])/100;
        }
//        var taxamount=(rec==null?0:(totalamount*rec.data["percent"])/100);
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
showGridBatch:function(newval){//written to hide & show Serial window Column-Neeraj D
    var hide=(newval)?0:1 ;
    var id=this.Grid.getId();
    var rowtaxindex=this.Grid.getColumnModel().getIndexById(id+'serialwindow');
    this.Grid.getColumnModel().setHidden( rowtaxindex,hide) ;
    this.Grid.getColumnModel().setHidden( rowtaxindex,hide) ;
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
//                if(this.ExpenseGrid && this.ExpenseGrid.isVisible()) {//(!this.isCustBill && !(this.isEdit && !this.isOrder) && !(this.isCustomer||this.isOrder))
//                    var index=this.ExpenseGrid.accountStore.find('accountid',rec.data.accountid);
//                    var taxid = index > 0 ? this.ExpenseGrid.accountStore.getAt(index).data["acctaxcode"]:"";
//                    var taxamount = this.ExpenseGrid.setTaxAmountAfterSelection(rec);
//                    rec.set('prtaxid',taxid);
//                    rec.set('taxamount',taxamount);
//                } else {
//                    index=this.ProductGrid.productComboStore.find('productid',rec.data.productid);
//                    var acctaxcode = (this.isCustomer)?"salesacctaxcode":"purchaseacctaxcode";
//                    taxid = index > 0 ? this.ProductGrid.productComboStore.getAt(index).data[acctaxcode]:"";
//                    rec.set('prtaxid',taxid);
//                    taxamount = this.ProductGrid.setTaxAmountAfterSelection(rec);
//                    rec.set('taxamount',taxamount);
//                }

//            if(rowRateIncludingGstAmountIndex!=-1&&this.Grid.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden){
                rec.set('discountispercent',1);
                rec.set('prdiscount',0);
                rec.set('rateIncludingGst',0);
//            }

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
             }else if(rowRateIncludingGstAmountIndex!=-1&&this.Grid.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden){
                 rec.set('discountispercent',1);
                 rec.set('prdiscount',0);
                 rec.set('rateIncludingGst',0);
             }
         },this);
//         if(hide)
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
            this.fromLinkCombo.setValue(0);            
            this.autoGenerateDO.enable();
            if(this.isOrder){
                this.POStore.proxy.conn.url = "ACCSalesOrderCMN/getSalesOrders.do";
                this.POStore.load({params:{currencyfilterfortrans:this.Currency.getValue()}});   
//                this.POStore.load({params:{currencyfilterfortrans:this.Currency.getValue(),includingGSTFilter:((this.includingGST)?this.includingGST.getValue() : false)}});
            } else {
                this.POStore.proxy.conn.url = this.isCustomer ? "ACCSalesOrderCMN/getSalesOrders.do" : "ACCPurchaseOrderCMN/getPurchaseOrders.do";
                this.POStore.load({params:{id:this.Name.getValue(),exceptFlagINV:true,currencyfilterfortrans:this.Currency.getValue()}});        
//                this.POStore.load({params:{id:this.Name.getValue(),exceptFlagINV:true,currencyfilterfortrans:this.Currency.getValue(),includingGSTFilter:((this.includingGST)?this.includingGST.getValue() : false)}});        
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
            this.includingGST.enable();
        }
        else if(rec.data['value']==1){     // 1 for Delivery   
            this.isFromGrORDO=true;
            this.Grid.isFromGrORDO=true;
            this.PO.multiSelect=true;
            this.isMultiSelectFlag=true;
            this.PO.removeListener("select",this.populateData,this);
            this.PO.addListener("blur",this.populateData,this);                
            this.fromLinkCombo.setValue(1);
            this.autoGenerateDO.setValue(false);
            this.autoGenerateDO.disable();            
            this.POStore.proxy.conn.url = this.isCustomer ? "ACCInvoiceCMN/getDeliveryOrdersMerged.do" : "ACCGoodsReceiptCMN/getGoodsReceiptOrdersMerged.do";
            this.POStore.load({params:{id:this.Name.getValue(),nondeleted:true,currencyfilterfortrans:this.Currency.getValue()}});
//            this.POStore.load({params:{id:this.Name.getValue(),nondeleted:true,includingGSTFilter:((this.includingGST)?this.includingGST.getValue() : false)}});
            this.PO.enable(); 
            if(this.partialInvoiceCmb){
                this.partialInvoiceCmb.disable();
                var id=this.Grid.getId();
                var rowindex=this.Grid.getColumnModel().getIndexById(id+"partdisc");
                this.Grid.getColumnModel().setHidden( rowindex,true) ;
            }
            this.includingGST.disable();
        } else if(rec.data['value']==2){ //2 for Quotation                        
            this.PO.multiSelect=true;
            this.isMultiSelectFlag=true;
            this.autoGenerateDO.enable();
            this.PO.removeListener("select",this.populateData,this);
            this.PO.addListener("blur",this.populateData,this);                            
            this.fromLinkCombo.setValue(2);
            this.POStore.proxy.conn.url = this.isCustomer ? "ACCSalesOrderCMN/getQuotations.do" : "ACCPurchaseOrderCMN/getQuotations.do";
//            this.POStore.load({params:{id:this.Name.getValue(),currencyid:this.Currency.getValue(),validflag:true,billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue()),nondeleted:true}});        

            if(this.isCustomer){
              this.POStore.load({params:{newcustomerid:this.Name.getValue(),linkFlagInInv:true,currencyid:this.Currency.getValue(),validflag:true,billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue()),nondeleted:true}});        
            }else{
              if(this.moduleid == Wtf.Acc_Purchase_Order_ModuleId){
                  this.POStore.load({params:{newvendorid:this.Name.getValue(),sopolinkflag:true,linkFlagInPO:true,currencyid:this.Currency.getValue(),validflag:true,billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue()),nondeleted:true}});        
              }else{
                this.POStore.load({params:{newvendorid:this.Name.getValue(),linkFlagInGR:true,currencyid:this.Currency.getValue(),validflag:true,billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue()),nondeleted:true}});        
              }
            }
            this.PO.enable();
            this.includingGST.enable();
        }       
    },
    
    enablePO:function(c,rec){
        this.autoGenerateDO.enable();
        if(rec.data['value']==true){
            if(!this.isCustBill&&!this.isCustomer&&!this.isEdit&&!this.copyInv&&!(this.isOrder&&(!this.isCustomer))){//this.isExpenseInv=false;
                this.GridPanel.setActiveTab(this.ProductGrid);
                this.ExpenseGrid.disable();

            }
            if(!(this.isCustBill || (this.isOrder&&this.isCustomer) ||this.cash)){
                this.fromLinkCombo.enable();
            }else{
                if(!this.isCustBill && this.isOrder && this.isCustomer && this.quotation)   //loading vendor Quotations in Customer Quotations
                    this.POStore.load({params:{validflag:true,billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue())}});
                else if(!this.isCustBill && this.isOrder && this.isCustomer && !this.quotation)   //loading Quotations in sales order
                    this.POStore.load({params:{newcustomerid:this.Name.getValue(),sopolinkflag:true,linkFlagInSO:true,currencyid:this.Currency.getValue(),validflag:true,billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue()),nondeleted:true}});
                else if(!this.isCustomer && !this.isCustBill && this.isOrder) //loading for Sales Orders in Purchase Orders
                    this.POStore.load();    
                else                                                         //loading for so and po in CI and VI in With/Without inventory mode and but not in trading flow
                    this.POStore.load({params:{id:this.Name.getValue()}});                
                this.PO.enable();
            }                                                      
            this.fromOrder=true;
            if(!this.isCustBill && this.isOrder && this.isCustomer && !this.quotation){
                this.PO.multiSelect=true;
                this.isMultiSelectFlag=true;
                this.PO.removeListener("select",this.populateData,this);
                this.PO.addListener("blur",this.populateData,this);                            
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
        }
        this.currencyStore.load(); 	       // Currency id issue 20018
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
         if(this.PO.getValue()!=""){
          var billid=this.PO.getValue();
          this.clearComponentValues();
          this.Grid.fromPO=true;  
          var multipleSelectProdIncludeFlag=false;          
          var hideTax=false;
          if(this.isMultiSelectFlag){ //For MultiSelection 
            var selectedids=this.PO.getValue();
            var selectedValuesArr = selectedids.split(',');
           var crosslink=false;
            if(WtfGlobal.getModuleId(this)==18 && this.fromLinkCombo.getValue()==0){//if salesorder is linked into purchaseorder 
//     (this.getModuleId()==20 && this.fromLinkCombo.getValue()==0)//this check is basically for linking po in so case-Generate SO case.It has to be further implemented.
                crosslink=true;   
            }
            if(selectedValuesArr.length==1){  // Load value of Include product tax according to PO
                 rec=this.POStore.getAt(this.POStore.find('billid',selectedValuesArr[0]));
                 if(rec.data["includeprotax"]){
                    this.includeProTax.setValue(true);
                    this.showGridTax(null,null,false);
                    this.isTaxable.setValue(false);//when selecting record with product tax.Tax should get disabled.
                    this.isTaxable.disable();
                    this.Tax.setValue("");
                    this.Tax.disable();
                 }else{
                    this.includeProTax.setValue(false);
                    this.showGridTax(null,null,true);
                    this.Tax.enable();//required because when selected multiple records & changing to select single record.Before it was getting disabled.
                    this.isTaxable.enable();
                 }
                  if(rec.data["gstIncluded"]&&!this.includingGST.getValue()){
                     this.includingGST.setValue(true);
                  }else if(!rec.data["gstIncluded"]&&this.includingGST.getValue()){
                     this.includingGST.setValue(false);
                  }
                  
                 if(this.IsInvoiceTerm) {
                    this.setTermValues(rec.data.termdetails);
                }
            }else if(selectedValuesArr.length>1){
                
                var productLevelTax=false;  
                var isGSTTax=false;
                var isInvoiceLevelTax=false;
                var withoutTax=false;
                this.previusTaxId="";
                var isInvoiceTaxDiff=false;
                var invoiceLevelTaxRecords=0;
                
                var invoiceLevelTaxRecords=0;
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
                    }else{
                        this.includeProTax.setValue(true);
                        this.showGridTax(null,null,false); 
                        this.isTaxable.setValue(false);//when selcting record with product tax.Tax should get disabled.
                        this.isTaxable.disable();
                        this.Tax.setValue("");
                        this.Tax.disable();
                    }                   
                }else if(isInvoiceLevelTax && !crosslink){
                    if(withoutTax || isInvoiceTaxDiff){//for different tax and empty tax
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.alert.includingDifferentTax")], 2); 
                        this.PO.clearValue();
                        return;
                    }else{
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
            
            }
           
            if(crosslink){//if salesorder is linked into purchaseorder-cross module
                    this.includeProTax.setValue(false);
                    this.showGridTax(null,null,true);
                    this.isTaxable.enable();
                    this.isTaxable.setValue(false);
                    this.Tax.disable();
                    this.Tax.setValue("");
                    this.updateData();
            }
            this.setValues(billid);//In MultiSelection if the user select only one
            rec=this.PO.getValue();
            selectedValuesArr = rec.split(',');
            if(selectedValuesArr.length==1){
                var record=this.POStore.getAt(this.POStore.find('billid',billid));
                this.isCustomer ? this.users.setValue(record.data["salesPerson"]) : this.users.setValue(record.data["agent"]); 
                if(this.partialInvoiceCmb){
                    this.partialInvoiceCmb.enable();
                }
                var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray;  //Reset Check List
                for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
                    var checkfieldId = checkListCheckBoxesArray[checkitemcnt].id
                    if (Wtf.getCmp(checkfieldId) != undefined) {
                        Wtf.getCmp(checkfieldId).reset();
                    }
                } 
                var fieldArr = this.POStore.fields.items;
                for(var fieldCnt=0; fieldCnt < fieldArr.length; fieldCnt++) {
                    var fieldN = fieldArr[fieldCnt];
                   
                    if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id) && record.data[fieldN.name] !="") {
                          if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).getXType()=='datefield'){
                        Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(record.data[fieldN.name]);
                          }else if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).getXType()=='fncombo'){
                                var ComboValue=record.data[fieldN.name];
                                var ComboValueID="";
                                var recCustomCombo =WtfGlobal.searchRecord(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).store,ComboValue,"name");
                                ComboValueID=recCustomCombo.data.id;
                                Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(ComboValueID);
                          }else{
                            Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(record.data[fieldN.name]);
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
                var fieldArr = this.POStore.fields.items;
                for(var fieldCnt=0; fieldCnt < fieldArr.length; fieldCnt++) {
                    var fieldN = fieldArr[fieldCnt];
                    if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id) && Wtf.getCmp(fieldN.name+this.tagsFieldset.id).getXType()!='fieldset') {
                        Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(rec.data[fieldN.name]);
                    }
                }
                // if(this.isOrder && this.isCustomer && !this.isCustBill){//Temporary check to hide/display product tax for order. Need to fix for Invoices also
                if(rec.data["includeprotax"]){
                    this.includeProTax.setValue(true);
                    
                    this.isTaxable.setValue(false);
                    this.isTaxable.disable();
                    this.Tax.setValue("");
                    this.Tax.disable();
                    
                    this.showGridTax(null,null,false);
                } else {
                    this.includeProTax.setValue(false);
                    this.isTaxable.reset();
                    this.isTaxable.enable();
                    this.showGridTax(null,null,true);
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
                this.shipLength.setValue(rec.data['shiplengthval']);
            if(this.isEdit && rec.data['invoicetype']!=""){
                this.invoiceType.setValue(rec.data['invoicetype']);  
                if(rec.data['invoicetype']=='ff808081434d75f20143518438fe0006'){
                    this.Grid.calculatePercentage=true;
                }
            } else{
                this.invoiceType.setValue("ff808081434d75f201435182a6270002");
            }
            this.shipDate.setValue(rec.data['shipdate']);
            this.validTillDate.setValue(rec.data['validdate']);
            if(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) { // set value only in VI module
                this.invoiceList.setValue(rec.data['landedInvoiceID']);
            }
            if((this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) || (this.moduleid == Wtf.Acc_Invoice_ModuleId && !this.cash)) {
                this.Term.setValue(rec.data['termdays']);
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
            }else{
                this.Tax.disable();
                this.isTaxable.reset();
                this.Tax.reset();
            }
            this.getCreditTo(rec.data.creditoraccount);            
            if(this.fromLinkCombo.getValue()==1||WtfGlobal.getModuleId(this)==22){
                if(WtfGlobal.getModuleId(this)==22){//if vendor quotation is linked in customer quotation then update tax details-erp2082
                    this.includeProTax.setValue(false);
                    this.showGridTax(null,null,true);
                    this.isTaxable.enable();
                    this.isTaxable.setValue(false);
                    this.Tax.disable();
                    this.Tax.setValue("");
                    this.updateData();
                }else{//for linkcombo
                    this.updateData();
                }
           
            }else{
                this.Currency.setValue(rec.data['currencyid']);
            }
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
                this.Term.setValue(storerec.data['termdays']);
            }        
            this.CostCenter.setValue(rec.data.costcenterid);
            rec=rec.data['billid'];
        }                                       
        this.updateDueDate();
        var url = "";
		//(this.isCustBill?53:43)
        var soLinkFlag = false;        
        var VQtoCQ = false;
        var sopolinkflag=false;
        var linkingFlag = false; //For removing cross reference of DO-CI or GR-VI
        if(!this.isCustBill && !this.isOrder && !this.cash ){
            if(this.fromLinkCombo.getValue()==0){
                url = this.isCustomer ? 'ACCSalesOrderCMN/getSalesOrderRows.do' : 'ACCPurchaseOrderCMN/getPurchaseOrderRows.do';
            } else if(this.fromLinkCombo.getValue()==1){
                url = this.isCustomer ? "ACCInvoiceCMN/getDeliveryOrderRows.do" : "ACCGoodsReceiptCMN/getGoodsReceiptOrderRows.do";
                linkingFlag=true;
            }else if(this.fromLinkCombo.getValue()==2){
                url = this.isCustomer ? "ACCSalesOrderCMN/getQuotationRows.do" : "ACCPurchaseOrderCMN/getQuotationRows.do";
                VQtoCQ = true;//Linking Quotation when creating invoice, we need to display Unit Price excluding row discount
            }
        } else {
            if(this.isCustomer){
                if(this.quotation){
                    soLinkFlag = true;
                    url = "ACCPurchaseOrderCMN/getQuotationRows.do";
                    VQtoCQ = true;
                } else if(this.isOrder){
                    url = "ACCSalesOrderCMN/getQuotationRows.do";
                    sopolinkflag=true;
                    VQtoCQ = true;
                } else {
                    url = this.isCustBill?"ACCSalesOrderCMN/getBillingSalesOrderRows.do":'ACCSalesOrderCMN/getSalesOrderRows.do';  
                }
            } else {
                if(this.isOrder){
                    url = 'ACCSalesOrderCMN/getSalesOrderRows.do';
                    if(this.isCustBill) {
                        url = "ACCSalesOrderCMN/getBillingSalesOrderRows.do";
                    } else {
                        if(this.fromLinkCombo.getValue()==0){
                            url = 'ACCSalesOrderCMN/getSalesOrderRows.do';
                            soLinkFlag = true;
                        } else if(this.fromLinkCombo.getValue()==2){
                            url = 'ACCPurchaseOrderCMN/getQuotationRows.do';
                            sopolinkflag=true;
                            VQtoCQ = true;
                        }
                    }
                } else {
                    url = this.isCustBill?"ACCPurchaseOrderCMN/getBillingPurchaseOrderRows.do":'ACCPurchaseOrderCMN/getPurchaseOrderRows.do';
                }
            }
        }                
	this.Grid.getStore().proxy.conn.url = url;
            this.Grid.loadPOGridStore(rec, soLinkFlag, VQtoCQ,linkingFlag,sopolinkflag);          
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
        this.Grid.loadPOGridStore(rec, false, false,false); 
    },

    setTerm:function(c,rec,ind){
        this.Term.setValue(rec.data['termdays']);
        this.updateDueDate();
    },
    
    setSalesPerson:function(c,rec,ind){
        this.users.setValue(rec.data['masterSalesPerson']);
    },
    updateSubtotal:function(a,val){
        
        if(this.calDiscount())return;
        this.isClosable=false; // Set Closable flag after updating grid data
        this.applyCurrencySymbol();
        var calTermTotal = WtfGlobal.addCurrencySymbolOnly(this.findTermsTotal(),this.symbol);
        if(a!=undefined && a.id!=undefined && a.id.indexOf("editproductdetailsgrid") != -1){
            this.termStore.removeAll();
            this.termStore.reload();
            this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol),discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol),termtotal:calTermTotal,aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.findTermsTotal()+this.caltax(),this.symbol),totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol()),amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())});
        }
        else{
            this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol),discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol),termtotal:calTermTotal,aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.findTermsTotal()+this.caltax(),this.symbol),totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol()),amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())});
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
        
//        return isNaN(parseFloat(disc))?0:(per?(disc*subtotalAfterTerm)/100:disc);
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
        var taxVal = this.caltax();
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
    if(this.checkBeforeProceed(this.Number.getValue()))
        {
    this.Number.setValue(this.Number.getValue().trim());   
        var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
        if(this.NorthForm.getForm().isValid() && isValidCustomFields){
            if(this.isCustBill){
                for(var datacount=0;datacount<this.Grid.getStore().getCount();datacount++){
                    var creditoracc=this.Grid.getStore().getAt(datacount).data['creditoraccount'];                    
                    if(creditoracc==undefined||creditoracc==""){
                        if(this.Grid.getStore().getAt(datacount).data['productdetail'].length>0){
                            var account=(this.isCustomer)?"Credit account":"Debit account";
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.fxexposure.warning"),WtfGlobal.getLocaleText("acc.field.Pleaseselect")+account], 2);
                            return;
                        } 
                    }            
                }
            }
            
            if(Wtf.account.companyAccountPref.showprodserial){    //check whether batch and serial no detail entered or not
                if(Wtf.serialwindowflag){
                    var prodLength=this.Grid.getStore().data.items.length;
                    for(var i=0;i<prodLength-1;i++)
                    { 
                        var prodID=this.Grid.getStore().getAt(i).data['productid'];
                        var prorec=this.Grid.productComboStore.getAt(this.Grid.productComboStore.find('productid',prodID));
                        if(prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part'){
                            var batchDetail= this.Grid.getStore().getAt(i).data['batchdetails'];
                            if(batchDetail == undefined || batchDetail == "" || batchDetail=="[]"){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.bsdetail")],2);   //Batch and serial no details are not valid.
                                return;
                            }
                        }
                    }
                }
            } 
            
                for(var i=0;i<this.Grid.getStore().getCount()-1;i++){// excluding last row
                var quantity=this.Grid.getStore().getAt(i).data['quantity'];
                var rate=this.Grid.getStore().getAt(i).data['rate'];
                var isparentproduct=this.Grid.getStore().getAt(i).data['isparentproduct'];
                if(!isparentproduct) 
                { 
                    if(!this.isExpenseInv && (quantity==""||quantity==undefined||quantity<=0)){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.QuantityforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.shouldbegreaterthanZero")], 2);
                        return;
                    } 
                    if(rate===""||rate==undefined||rate<0){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.RateforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.cannotbeempty")], 2);
                        return;
                    }
                }	
            }
            
            if(!this.isCustBill && (Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel && ((Wtf.account.companyAccountPref.withinvupdate && (!this.isInvoice && !this.isOrder)) || (!Wtf.account.companyAccountPref.withinvupdate && !this.isOrder)))){
                
                var validstore=WtfGlobal.isValidInventoryInfo(this.Grid.getStore(),'invstore');
                if(!validstore){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Pleaseselectvalidinventorystore")], 2);
                    return;
                }
                
                var validloc=WtfGlobal.isValidInventoryInfo(this.Grid.getStore(),'invlocation');
                if(!validloc){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Pleaseselectvalidinventorylocation")], 2);
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
           if(!(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId||this.moduleid == Wtf.Acc_Invoice_ModuleId||this.moduleid == Wtf.Acc_Cash_Sales_ModuleId||this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId||this.isExpenseInv==true))
             if(this.Grid.calSubtotal()<=0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.TotalamountshouldbegreaterthanZero")], 2);
                return;
            }
//            var datediff=new Date(this.billDate.getValue()).getElapsed(this.DueDate.getValue());
//            if(datediff==0)
//                  incash=true;
//              else
                  incash=this.cash;
            var rec=this.NorthForm.getForm().getValues();
            if(rec.vendor==undefined&&this.linkIDSFlag!=undefined&&this.linkIDSFlag){
                rec.vendor=this.Name.getValue();
            }
            if(rec.customer==undefined&&this.linkIDSFlag!=undefined&&this.linkIDSFlag){
                rec.customer=this.Name.getValue();
            }
            this.isGenerateReceipt = this.generateReceipt.getValue();
            this.isAutoCreateDO = this.autoGenerateDO.getValue();
            rec.islockQuantity =this.lockQuantity.getValue();
            rec.billdate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
            if(this.cash)
                {
                   var index = this.termds.find('termdays','-1');
                    if(index != -1){
                        var storerec=this.termds.getAt(index);                        
                       this.termid=storerec.get("termid");
                    }          
                }
             else
                      this.updateDueDate();
            rec.termid=this.termid;
        this.ajxurl = ""; 
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
            var msg=currencychange?WtfGlobal.getLocaleText("acc.field.Currencyrateyouhaveappliedcannotbechanged"):"";           
            var detail = this.Grid.getProductDetails();
            if(detail == undefined || detail == "[]"){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg12")],2);   //"Product(s) details are not valid."
                return;
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
                    this.checkiflinkdo(rec,detail,incash);                              
                },this);  
            }else {
                this.checkiflinkdo(rec,detail,incash);             
            }

        }else{
            WtfComMsgBox(2, 2);
        }
        }  
      else
          {
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.fxexposure.warning"),WtfGlobal.getLocaleText("acc.field.PleaseTryothervalueinInvoiceNumber")], 1); 
          } 
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
                            var accMsg = WtfGlobal.getLocaleText("acc.field.FollowingAccountsareexceedingtheirMonthlyBudgetLimit")+"<br><br><center>";
                            var budgetMsg = "";
                            for(var i=0; i< response.data.length; i++){
                                var recTemp = response.data[i];
                                if (!this.isCustBill)
                                    budgetMsg = (recTemp.productName == "" ? "" : "<b>"+WtfGlobal.getLocaleText("acc.field.Product")+"</b>" + recTemp.productName + ",") + " <b>"+WtfGlobal.getLocaleText("acc.field.Account")+" </b>" + recTemp.accountName + ", <b>"+WtfGlobal.getLocaleText("acc.field.Balance")+" </b>" + recTemp.accountBalance + ", <b>"+WtfGlobal.getLocaleText("acc.field.Budget")+"</b>" + recTemp.accountBudget;
                                else
                                    budgetMsg = (recTemp.productName == "" ? "" : "<b>"+WtfGlobal.getLocaleText("acc.field.JobDescription")+" </b>" + recTemp.productName + ",") + " <b>"+WtfGlobal.getLocaleText("acc.field.Account")+" </b>" + recTemp.accountName + ", <b>"+WtfGlobal.getLocaleText("acc.field.Balance")+"</b>" + recTemp.accountBalance + ", <b>"+WtfGlobal.getLocaleText("acc.field.Budget")+" </b>" + recTemp.accountBudget;
                                accMsg += budgetMsg + "<br>";
                            }

                            accMsg += "<br>"+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+"</center>";
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
        }else{
            if(this.Name.getValue() == ''){
                    var fieldLabel = this.isCustomer?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven") ; //this.businessPerson+"*",
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Pleaseselect")+fieldLabel], 2);
                    return;
            }
            var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
            if(this.NorthForm.getForm().isValid() && isValidCustomFields) {
                if(this.isCustBill){
                    for(var datacount=0;datacount<this.Grid.getStore().getCount();datacount++){
                        var creditoracc=this.Grid.getStore().getAt(datacount).data['creditoraccount'];                    
                        if(creditoracc==undefined||creditoracc==""){
                            if(this.Grid.getStore().getAt(datacount).data['productdetail'].length>0){
                                var account=(this.isCustomer)?"Credit account":"Debit account";
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Pleaseselect")+account], 2);
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
                
                for(var i=0;i<this.Grid.getStore().getCount()-1;i++) { // excluding last row
                    var quantity=this.Grid.getStore().getAt(i).data['quantity'];
                    var rate=this.Grid.getStore().getAt(i).data['rate'];
                    if(!this.isExpenseInv && (quantity==""||quantity==undefined||quantity<=0)){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.QuantityforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.shouldbegreaterthanZero")], 2);
                        return;
                    } 
                    if(rate===""||rate==undefined||rate<0){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.RateforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.cannotbeempty")], 2);
                        return;
                    }
                }
                
                var count=this.Grid.getStore().getCount();
                if(count<=1){
                    WtfComMsgBox(33, 2);
                    return;
                }
                
                this.transactionType = 2;
                var rec=this.NorthForm.getForm().getValues();
                rec.billdate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
                var detail = this.Grid.getProductDetails();
                var incash=this.cash;
                rec.termid=this.termid;
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
            } else{
                WtfComMsgBox(2, 2);
            }
            
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
                Wtf.Ajax.requestEx({
                    url:"ACC"+this.businessPerson+"CMN/get"+this.businessPerson+"Exceeding"+(this.businessPerson=="Vendor"?"Debit":"Credit")+"Limit.do",
                    params:rec                                                                                                                                            
                },this,function(response){
                    if(response.data && response.data.length > 0){
                        var msg = (this.businessPerson=="Vendor"?"<center>"+WtfGlobal.getLocaleText("acc.cust.debitLimit"):"<center>"+WtfGlobal.getLocaleText("acc.cust.creditLimit"))+" "+WtfGlobal.getLocaleText("acc.field.forthis")+this.businessPerson+" "+WtfGlobal.getLocaleText("acc.field.hasreached")+"<br><br>";
                        var limitMsg = "";
                        for(var i=0; i< response.data.length; i++){
                            var recTemp = response.data[i];
                            limitMsg = (recTemp.name == "" ? "" : "<b>"+this.businessPerson+": </b>" + recTemp.name + ", ") +"<b>"+WtfGlobal.getLocaleText("acc.customerList.gridAmountDue")+": </b>" + WtfGlobal.conventInDecimalWithoutSymbol(recTemp.amountDue) + ", <b>"+(this.businessPerson=="Vendor"?WtfGlobal.getLocaleText("acc.cust.debitLimit"):WtfGlobal.getLocaleText("acc.cust.creditLimit"))+": </b>" + recTemp.limit;
                            msg += limitMsg + "<br>";
                        }
                        var limitControlType="";
                        if(this.isCustomer){
                            limitControlType=Wtf.account.companyAccountPref.custcreditlimit;
                        } else {
                            limitControlType=Wtf.account.companyAccountPref.vendorcreditcontrol;
                        }
                        if(limitControlType == Wtf.controlCases.BLOCK){//block
                            msg += "<br>"+WtfGlobal.getLocaleText("acc.field.Youcannotproceed")+"</center>";
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
                this.showConfirmAndSave(rec,detail,incash);
            }
    },
    checkiflinkdo: function(rec,detail,incash){  // Use to check Only for autogernerate DO that Product quantity exceeds limit or not
     var flag =false
       if(this.autoGenerateDO.getValue() == true && Wtf.account.companyAccountPref.negativestock !=0 && (this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId )){
            var prodLength=this.Grid.getStore().data.length;
            for(var i=0;i<prodLength;i++)
            {
                var prodID=this.Grid.getStore().getAt(i).data['productid'];
                var prorec=this.Grid.productComboStore.getAt(this.Grid.productComboStore.find('productid',prodID));
                if(prorec != undefined){
                    var prodName=prorec.data.productname;
                    var availableQuantity = prorec.data.quantity;
                    var quantity= this.Grid.getStore().getAt(i).data['quantity'];
                    if(availableQuantity < quantity){  
                    if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninAutoDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prodName+" " +WtfGlobal.getLocaleText("acc.field.is")+(availableQuantity)+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                        return ;
                    }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninAutoDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                            if(btn=="yes"){
                                  this.checkMemo(rec,detail,incash);  
                                  return;
                             }else{
                               return ;
                            }
                        },this);
                        return;
                    }       
                   }else{
                          flag=true;    
                   }
               }
            }
        }else{
            this.checkMemo(rec,detail,incash);   
        }
        if(flag){
            this.checkMemo(rec,detail,incash);   
        }
    },
    showConfirmAndSave: function(rec,detail,incash){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),this.EditisAutoCreateDO ? ( this.businessPerson=="Customer" ? WtfGlobal.getLocaleText("acc.invoice.msg16") : WtfGlobal.getLocaleText("acc.invoice.msg19") ):WtfGlobal.getLocaleText("acc.invoice.msg7"),function(btn){
                if(btn!="yes") {
                    this.saveOnlyFlag=false;
                    return;
                }
                if( Wtf.account.companyAccountPref.isSalesOrderCreatedForCustomer && this.moduleid == Wtf.Acc_Sales_Order_ModuleId){
                   this.checkSOIsCreatedForCustomer(rec,detail,incash);      
                }else{
                    this.finalSave(rec,detail,incash);
                }
            },this);
    },
    
    finalSave: function (rec,detail,incash){
        rec.taxid=this.Tax.getValue();
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
        var custFieldArr=this.tagsFieldset.createFieldValuesArray();
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
        if(this.isGeneratedRecurringInvoice != undefined && this.isGeneratedRecurringInvoice == 1){
            rec.Oldinvoiceid=this.record.data.billid;
            rec.isGeneratedRecurringInvoice=1;
            rec.generatedDate=WtfGlobal.convertToGenericDate(this.onDate);//Use to Exclude Invoice from Outstanding order Report
        }
        //                if(this.isOrder && !this.quotation){
        //                    if(this.fromLinkCombo && this.fromLinkCombo.getValue() != "" && this.fromLinkCombo.getValue() == 3){
        //                        rec.isLinkedFromMaintenanceNumber=true;
        //                    }
        //                }
        if(this.isOrder && !this.quotation){
            if(this.fromLinkCombo && this.fromLinkCombo.getValue() != "" && this.fromLinkCombo.getValue() == 3){
                rec.isLinkedFromMaintenanceNumber=true;
            }
        }

        if(this.isMaintenanceOrderCheckBox && this.isMaintenanceOrderCheckBox.getValue()){
            if(this.maintenanceNumberCombo && this.maintenanceNumberCombo.getValue() != ""){
                rec.isLinkedFromMaintenanceNumber=true;
                rec.maintenanceId=this.maintenanceNumberCombo.getValue();
            }
        }

        //                rec.vendorinvoice = this.vendorInvoice!=null?this.vendorInvoice.getValue():'';
        if (custFieldArr.length > 0)
            rec.customfield = JSON.stringify(custFieldArr);
        rec.invoicetermsmap = this.getInvoiceTermDetails();
        if(this.Grid.deleteStore!=undefined && this.Grid.deleteStore.data.length>0)
            rec.deletedData=this.getJSONArray(this.Grid.deleteStore,false,0);
        rec.number=this.Number.getValue();
        rec.linkNumber=(this.PO != undefined && this.PO.getValue()!="")?this.PO.getValue():"";
        rec.fromLinkCombo=this.fromLinkCombo.getRawValue();
        rec.duedate=WtfGlobal.convertToGenericDate(this.DueDate.getValue());
        rec.billdate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
        rec.shipdate=WtfGlobal.convertToGenericDate(this.shipDate.getValue());
        rec.validdate=WtfGlobal.convertToGenericDate(this.validTillDate.getValue());
        rec.invoiceid=(this.copyInv||this.GENERATE_PO||this.GENERATE_SO)?"":this.billid;
        rec.doid=this.DeliveryOrderid;
        rec.mode=(this.isOrder?(this.isCustBill?51:41):(this.isCustBill?13:11));
        rec.incash=incash;
        rec.partialinv = (this.partialInvoiceCmb)? this.partialInvoiceCmb.getValue() : false;
        this.totalAmount = rec.subTotal + rec.taxamount - this.getDiscount();
        rec.includeprotax = (this.includeProTax)? this.includeProTax.getValue() : false;
        rec.includingGST = (this.includingGST)? this.includingGST.getValue() : false;
        rec.landedInvoiceNumber = this.invoiceList.getValue();
        if(this.autoGenerateDO.getValue() ||  this.EditisAutoCreateDO){
            var seqFormatRecDo=WtfGlobal.searchRecord(this.sequenceFormatStoreDo, this.sequenceFormatComboboxDo.getValue(), 'id');
            rec.seqformat_oldflagDo=seqFormatRecDo!=null?seqFormatRecDo.get('oldflag'):true;
            rec.numberDo = this.no.getValue();
            rec.sequenceformatDo=this.sequenceFormatComboboxDo.getValue();
        }
        var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
        rec.sequenceformat=this.sequenceFormatCombobox.getValue();
        rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):true;

        if(this.currenctAddressDetailrec!="" && this.currenctAddressDetailrec!=undefined){
            rec.billingAddress=this.currenctAddressDetailrec.billingAddress;
            rec.billingCounty=this.currenctAddressDetailrec.billingCounty;
            rec.billingCity=this.currenctAddressDetailrec.billingCity;
            rec.billingState=this.currenctAddressDetailrec.billingState;
            rec.billingCountry=this.currenctAddressDetailrec.billingCountry;
            rec.billingPostal=this.currenctAddressDetailrec.billingPostal;
            rec.billingPhone=this.currenctAddressDetailrec.billingPhone;
            rec.billingMobile=this.currenctAddressDetailrec.billingMobile;
            rec.billingFax=this.currenctAddressDetailrec.billingFax;
            rec.billingEmail=this.currenctAddressDetailrec.billingEmail;
            rec.billingContactPerson=this.currenctAddressDetailrec.billingContactPerson;
            rec.billingContactPersonNumber=this.currenctAddressDetailrec.billingContactPersonNumber;
            rec.billingContactPersonDesignation=this.currenctAddressDetailrec.billingContactPersonDesignation;
            rec.shippingAddress=this.currenctAddressDetailrec.shippingAddress;
            rec.shippingCounty=this.currenctAddressDetailrec.shippingCounty;
            rec.shippingCity=this.currenctAddressDetailrec.shippingCity;
            rec.shippingState=this.currenctAddressDetailrec.shippingState;
            rec.shippingCountry=this.currenctAddressDetailrec.shippingCountry;
            rec.shippingPostal=this.currenctAddressDetailrec.shippingPostal;
            rec.shippingPhone=this.currenctAddressDetailrec.shippingPhone;
            rec.shippingMobile=this.currenctAddressDetailrec.shippingMobile;
            rec.shippingFax=this.currenctAddressDetailrec.shippingFax;
            rec.shippingEmail=this.currenctAddressDetailrec.shippingEmail;
            rec.shippingContactPerson=this.currenctAddressDetailrec.shippingContactPerson;
            rec.shippingContactPersonNumber=this.currenctAddressDetailrec.shippingContactPersonNumber;             
            rec.shippingContactPersonDesignation=this.currenctAddressDetailrec.shippingContactPersonDesignation;             
            rec.billingAddressType=this.currenctAddressDetailrec.billingAddrsCombo;
            rec.shippingAddressType=this.currenctAddressDetailrec.shippingAddrsCombo;
        }else if(this.addressrec!=null &&(this.isEdit || this.copyInv)){
            rec.billingAddress=this.addressrec.data.billingAddress;
            rec.billingCounty=this.addressrec.data.billingCounty;
            rec.billingCity=this.addressrec.data.billingCity;
            rec.billingState=this.addressrec.data.billingState;
            rec.billingCountry=this.addressrec.data.billingCountry;
            rec.billingPostal=this.addressrec.data.billingPostal;
            rec.billingPhone=this.addressrec.data.billingPhone;
            rec.billingMobile=this.addressrec.data.billingMobile;
            rec.billingFax=this.addressrec.data.billingFax;
            rec.billingEmail=this.addressrec.data.billingEmail;
            rec.billingContactPerson=this.addressrec.data.billingContactPerson;
            rec.billingContactPersonNumber=this.addressrec.data.billingContactPersonNumber;
            rec.billingContactPersonDesignation=this.addressrec.data.billingContactPersonDesignation;
            rec.shippingAddress=this.addressrec.data.shippingAddress;
            rec.shippingCounty=this.addressrec.data.shippingCounty;
            rec.shippingCity=this.addressrec.data.shippingCity;
            rec.shippingState=this.addressrec.data.shippingState;
            rec.shippingCountry=this.addressrec.data.shippingCountry;
            rec.shippingPostal=this.addressrec.data.shippingPostal;
            rec.shippingPhone=this.addressrec.data.shippingPhone;
            rec.shippingMobile=this.addressrec.data.shippingMobile;
            rec.shippingFax=this.addressrec.data.shippingFax;
            rec.shippingEmail=this.addressrec.data.shippingEmail;
            rec.shippingContactPerson=this.addressrec.data.shippingContactPerson;
            rec.shippingContactPersonNumber=this.addressrec.data.shippingContactPersonNumber;
            rec.shippingContactPersonDesignation=this.addressrec.data.shippingContactPersonDesignation;
            rec.billingAddressType=this.addressrec.data.billingAddressType;
            rec.shippingAddressType=this.addressrec.data.shippingAddressType;
            rec.isEdit=this.isEdit; 
            rec.copyInv=this.copyInv; 
        } else{//Used for saving default address of customer/vendor on java side
            rec.defaultAdress=true;
        }                      
        if(this.isAutoCreateDO ||  this.EditisAutoCreateDO){
            rec.isAutoCreateDO = this.EditisAutoCreateDO ? this.EditisAutoCreateDO : this.isAutoCreateDO;
            rec.fromLinkComboAutoDO =this.isCustomer ? "Customer Invoice" : "Vendor Invoice";
        } 
        Wtf.Ajax.requestEx({
            url:this.ajxurl,
            //                    url: Wtf.req.account+this.businessPerson+'Manager.jsp',
            params: rec
        },this,this.genSuccessResponse,this.genFailureResponse);
    },
    
    checkSOIsCreatedForCustomer:function(rec,detail,incash){
        Wtf.Ajax.requestEx({
            url:"ACCSalesOrderCMN/getSalesOrdersMerged.do",
            params: {
                newcustomerid:this.Name.getValue(),
                billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue())
            }
        },this,function(response){
            if (response.data && response.data.length > 0) {
               Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.msgbox.SalesOrderIsAlreadyGenerated"),function(btn){
                    if(btn!="yes") {
                        return;
                    }
                    this.finalSave(rec,detail,incash);
                },this);
            } else {
                this.finalSave(rec,detail,incash);
            }
        },function(response){
            
        });
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

    handleProductTypeForConsignment: function(){
        // In Consignment link case select product of service type product reset and prompt msg
        if(this.invoiceList != undefined && this.invoiceList.getValue() != "") {
            var productid;
            if(this.Grid != undefined) {
                for(var i=0; i<this.Grid.getStore().getCount(); i++) {
                    productid = this.Grid.getStore().getAt(i).get("productid");

                    if((productid != undefined || productid != "") && (this.Grid != undefined && this.Grid.getStore().getCount() > 0)) {
                        var index = this.Grid.productComboStore.find('productid',productid);
                        if(index != -1) {
                            var productType = this.Grid.productComboStore.getAt(index).get("type");
                            if(productType == "Service") {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.consignmentCaseProductSelectMsg")], 2);

                                // For reset all related fields
                                var customer= this.Name.getValue();
                                if(!this.GENERATE_PO&&!this.GENERATE_SO){
                                    this.loadStore();
                                    this.Name.setValue(customer);
                                }
                                this.updateData();
                            }
                        }
                    }
                }
                
            }
        }
    },

    loadTransStore : function(productid){
        if(this.Name.getValue() != ""){
            var customer= (this.businessPerson=="Vendor")? "" : this.Name.getValue();
            var vendor= (this.businessPerson=="Vendor")? this.Name.getValue() : "" ;
            if((productid == undefined || productid == "") && this.Grid.getStore().getCount() > 0){
                productid = this.Grid.getStore().getAt(0).get("productid");
            }
            
            // In Consignment link case select product of service type product reset and prompt msg
            this.handleProductTypeForConsignment();
            
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
         
        if(actualTaxId== undefined || actualTaxId == "" ||  actualTaxId == null){// if customer/vendor is not mapped with tax then check that is their mapping account is mapped with tax or not, if it is mapped take account tax
            actualTaxId=currentTaxItem!=null?currentTaxItem.get('mappedAccountTaxId'):"";
        }

        if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null ){
            this.isTaxable.setValue(true);
            this.Tax.enable();
                this.isTaxable.enable();
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

            if(this.fromPO)					// Currency id issue 20018
            	this.currencyStore.load();
            }
            this.amountdue=0;
            this.amountdue=response.amountdue;
            this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:WtfGlobal.currencyRenderer(0),discount:WtfGlobal.currencyRenderer(0),totalamount:WtfGlobal.currencyRenderer(0),tax:WtfGlobal.currencyRenderer(0),aftertaxamt:WtfGlobal.currencyRenderer(0),totalAmtInBase:WtfGlobal.currencyRenderer(0),termtotal:WtfGlobal.currencyRenderer(0),amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,this.symbol)})
        }
    },
    getTerm:function(val1,val2){
            if(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) {
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
                this.Term.setValue(rec.data.termdays);
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
    updateDueDate:function(a,val){
        var term=null;
        var validTillDate=null;
        if(this.quotation){
            if(Wtf.account.companyAccountPref.noOfDaysforValidTillField!=-1){
                validTillDate=new Date(this.billDate.getValue()).add(Date.DAY, Wtf.account.companyAccountPref.noOfDaysforValidTillField);
            }
        }
        if(validTillDate!=null && this.quotation ){
            this.validTillDate.setValue(validTillDate)
        }
        if(this.Term.getValue()!=""&&isNaN(this.Term.getValue())==false){
            term=new Date(this.billDate.getValue()).add(Date.DAY, this.Term.getValue());
        }
        else
            term=this.billDate.getValue();
        
        if(Wtf.account.companyAccountPref.shipDateConfiguration) {
            if(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) {
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
            this.NorthForm.getForm().setValues({duedate:term});
        }
        if(this.Grid){
            this.Grid.billDate = this.billDate.getValue()
        }
        var rec = this.Term.store.getAt(this.Term.store.find('termdays',this.Term.getValue()));
        if(rec != null && rec != undefined)
            this.termid=rec.data.termid;
    },

    genSuccessResponse:function(response, request){
        this.RecordID=response.SOID!=undefined?response.SOID : response.invoiceid;
        if(response.success && this.GENERATE_PO) {
            if(response.pendingapproval == 1) {
                WtfComMsgBox([this.titlel,WtfGlobal.getLocaleText("acc.field.PurchaseOrdersuccessfullygeneratedbutpendingforApproval") + this.Number.getValue()],response.success*2+1);
            } else {
                WtfComMsgBox([this.titlel,WtfGlobal.getLocaleText("acc.field.PurchaseOrdersuccessfullygeneratedPONumber") + this.Number.getValue()],response.success*2+1);
            }
            
        }if(response.success && this.GENERATE_SO) {
              if(response.pendingapproval == 1) {
                WtfComMsgBox([this.titlel,response.msg],response.success*2+1);
            } else {
                WtfComMsgBox([this.titlel,response.msg],response.success*2+1);
            }
            
        } else {
            WtfComMsgBox([this.titlel,response.msg],response.success*2+1);
        }
        var rec=this.NorthForm.getForm().getValues();
        this.exportRecord=rec;
        this.exportRecord['billid']=response.billid||response.invoiceid;
        this.exportRecord['billno']=response.billno||response.invoiceNo;
        this.exportRecord['amount']=(this.moduleid==22||this.moduleid==23)?this.totalAmount:response.amount;
        this.singlePrint.exportRecord=this.exportRecord;
        
         if(response.success){
            if(!this.isCustBill){
                Wtf.productStoreSales.reload();
                Wtf.productStore.reload();   //Reload all product information to reflect new quantity, price etc                
            }            	
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
            
            if(this.saveOnlyFlag){
                this.loadUserStoreForInvoice(response, request);
                this.disableComponent();
//                Wtf.getCmp("emailbut" + this.id).enable();
//                Wtf.getCmp("exportpdf" + this.id).enable();
            if((this.record && this.record !== undefined)){  // in copy case of CS,when a product is aaded then saved copy,when the form becomes disable the added product disappers because id was not comming properly
                this.record.data.billid=this.response.invoiceid;
            }
                this.response = response;
                this.record.data.billid=this.response.invoiceid;
                this.request = request;
                return;
            }
            
            if(this.maintenanceNumberComboStore &&!this.isExpenseInv){
                this.maintenanceNumberComboStore.load();
            }
            
            this.lastTransPanel.Store.removeAll();
            this.symbol = WtfGlobal.getCurrencySymbol();
            this.currencyid = WtfGlobal.getCurrencyID();
            this.loadStore();
            this.fromPO.disable();
            this.currencyStore.load();  
            this.Currency.setValue(WtfGlobal.getCurrencyID()); // Reset to base currency 
            this.externalcurrencyrate=0; //Reset external exchange rate for new Transaction.
            this.isClosable= true;       //Reset Closable flag to avoid unsaved Message.
            if(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash){
               this.InvoiceStore.reload();
            }
          Wtf.dirtyStore.product = true;
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
            var customFieldArray = this.tagsFieldset.customFieldArray;  //Reset Custom Fields
            for (var itemcnt = 0; itemcnt < customFieldArray.length; itemcnt++) {
                var fieldId = customFieldArray[itemcnt].id
                if (Wtf.getCmp(fieldId) != undefined && customFieldArray[itemcnt].getXType()!='fieldset') {
                       Wtf.getCmp(fieldId).reset();
                }
            }    
            var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray;  //Reset Check List
            for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
                var checkfieldId = checkListCheckBoxesArray[checkitemcnt].id
                if (Wtf.getCmp(checkfieldId) != undefined) {
                       Wtf.getCmp(checkfieldId).reset();
                }
            } 
            var customDimensionArray = this.tagsFieldset.dimensionFieldArray;  //Reset Custom Dimension
            for (var itemcnt1 = 0; itemcnt1 < customDimensionArray.length; itemcnt1++) {
                var fieldId1 = customDimensionArray[itemcnt1].id
                if (Wtf.getCmp(fieldId1) != undefined) {
                       Wtf.getCmp(fieldId1).reset();
                }
            } 
            this.fireEvent('update',this);
            this.amountdue=0;
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
        WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.information'),titleMsg+WtfGlobal.getLocaleText("acc.field.ispendingforapprovalSoyoucannotsendmailrightnow")],3);
        return;
    }
    if(this.CustomStore != null){
        var rec = this.CustomStore.getAt(0);
        var label = "";
        if(this.cash){
            if(this.isCustomer){
                label = WtfGlobal.getLocaleText("acc.field.CashSalesReceipt");
                if(rec.data.withoutinventory){
                    callEmailWin("emailwin",rec,label,11,true,false,false,true);
                }else{
                    callEmailWin("emailwin",rec,label,2,true,false,false,true);
                }
            }else{
                label = WtfGlobal.getLocaleText("acc.field.CashPurchaseReceipt");
                if(rec.data.withoutinventory){
                    callEmailWin("emailwin",rec,label,15,false,false,false,true);
                }else{
                    callEmailWin("emailwin",rec,label,6,false,false,false,true);
                }
            }
        } else if(this.isOrder && !this.quotation){
            if(this.isCustomer){
                label = WtfGlobal.getLocaleText("acc.wtfTrans.so");
                if(rec.data.withoutinventory){
                    callEmailWin("emailwin",rec,label,17,true,false,false,false,true);
                }else{
                    callEmailWin("emailwin",rec,label,1,true,false,false,false,true);
                }
            }else{
                label = WtfGlobal.getLocaleText("acc.wtfTrans.po");
                if(rec.data.withoutinventory){
                    callEmailWin("emailwin",rec,label,18,false,false,false,false,true);
                }else{
                    callEmailWin("emailwin",rec,label,5,false,false,false,false,true);
                }
            }
        } else if(this.quotation){
            if(this.isCustomer){
                label = WtfGlobal.getLocaleText("acc.dimension.module.12");
                callEmailWin("emailwin",rec,label,50,true,false,true);
            }else{
                label = WtfGlobal.getLocaleText("acc.vend.createvendQ");
                callEmailWin("emailwin",rec,label,57,false,false,true);
            }
        }else{
            if(this.isCustomer){
                label = WtfGlobal.getLocaleText("acc.field.CustomerInvoice");
                if(rec.data.withoutinventory){
                    callEmailWin("emailwin",rec,label,11,true,true);
                }else{
                    callEmailWin("emailwin",rec,label,2,true,true);
                }
            }else{
                label = WtfGlobal.getLocaleText("acc.agedPay.venInv");
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
            label = WtfGlobal.getLocaleText("acc.field.CashSalesReceipt");
        }else{
            label = WtfGlobal.getLocaleText("acc.field.CashPurchaseReceipt");
        }
    }else if(this.isOrder && !this.quotation){
        if(this.isCustomer){
            label = WtfGlobal.getLocaleText("acc.accPref.autoSO");
        }else{
            label = WtfGlobal.getLocaleText("acc.accPref.autoPO");
        }
    }else if(this.quotation){
        if(this.isCustomer){
            label = WtfGlobal.getLocaleText("acc.accPref.autoCQN");
        }else{
            label = WtfGlobal.getLocaleText("acc.dimension.module.11");
        }
    }else{
        if(this.isCustomer){
            label = WtfGlobal.getLocaleText("acc.field.CustomerInvoice");
        }else{
            label = WtfGlobal.getLocaleText("acc.agedPay.venInv");
        }
    }
    
    return label;
},

disableComponent: function(){ // disable following component in case of save button press.
   
    if(this.fromLinkCombo && this.fromLinkCombo.getValue() === ''){
        this.fromLinkCombo.emptyText = "";
        this.fromLinkCombo.clearValue();
    }
    
    if(this.PO && this.PO.getValue() === ''){
        this.handleEmptyText=true;
//        this.PO.emptyText = "";	//Commented to avoid disappearing watermark after saving of quotation.
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
        this.Grid.disable();
    }

    if(this.NorthForm){
        this.NorthForm.disable();
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
    WtfGlobal.openModuleTab(this, this.isCustomer, this.isQuotation, this.isOrder, copyInv, templateId, formrec);
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
                ss:request.params.number
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
        
        if ((this.moduleid == Wtf.autoNum.Invoice||this.moduleid == Wtf.autoNum.GoodsReceipt) && Wtf.templateflag == 8 ){
            var url="";
            if(request.params.invoicetype=="ff808081434d75f2014351835fc70003"){    ///invoicetype id in the for adhoc invoice..
                //if(this.templateId==Wtf.Acc_Basic_Template_Id+"adHOC") {
                url="Ad-Hoc";   //for export Ad-Hoc Invoice type of Customer Invoice
            }
            else if(request.params.invoicetype=="ff808081434d75f201435182a6270002") {  // invoicetype id for marin invoice
                url="Marine";  //for export Marine Invoice type of Customer Invoice.
            }
            else if (request.params.invoicetype=="ff808081434d75f20143518400630005" || request.params.invoicetype=="ff808081434d75f20143518400630008") {
                url="RetailInFix";  //for export Retail Invoice-Fixed type of Customer Invoice.
            }
            else if (request.params.invoicetype=="ff808081434d75f20143518438fe0006") {
                url="RetailInvVar";  // for export Retail Invoice-Variable type of Customer Invoice
            }
            else if (request.params.invoicetype=="ff808081434d75f201435183b3270007") {
                url="WaterSale";  // for export Retail Invoice-Variable type of Customer Invoice
            }
            else if (request.params.invoicetype=="ff808081434d75f201435183b3270004") {
                url="VisitPassInv";  // for
            }
            else if (recData.invoicetype=="ff808081434d75f201435183b3270007") {
                url="WaterSale";  // for
            }
            else if (recData.invoicetype=="ff808081434d75f20143518400630009") {
                url="SecurityOfficer";  // for
            }
            else if (recData.invoicetype=="ff808081434d75f20143518400630010") {
                url="Event";  // for
            }
            if(this.moduleid == Wtf.autoNum.Invoice){
                Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportSatsTaxInvoiceJasper.do?moduleid=2&mode=" + this.moduleid + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&invoicetype=" +url; 
            }else if(this.moduleid == Wtf.autoNum.GoodsReceipt){
                Wtf.get('downloadframe').dom.src = "ACCGoodsReceiptCMN/exportSatsVendorTaxInvoiceJasper.do?moduleid=2&mode=" + this.moduleid + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&invoicetype=" +url; 
            }
        }else {
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
    }
},



    loadStore:function(){
//        if(!(this.isCustBill||this.isExpenseInv))
//            this.Grid.priceStore.purgeListeners();
        if(!this.isEdit && !this.copyInv){
            this.Grid.getStore().removeAll();
        }
//        this.setTransactionNumber();
        this.PO.setDisabled(true);
        this.fromLinkCombo.setDisabled(true);
        if(this.isTemplate){
            this.createTransactionAlsoOldVal = this.createTransactionAlso;
            this.oldTempNameVal = this.moduleTemplateName.getValue();
        }
        if(this.isEdit){//in edit case need to preserve some data befor resetall
            this.number=this.Number.getValue();                
        }
//        this.NorthForm.getForm().reset();
        this.resetField();
        this.Term.clearValue();
        if(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash){
             this.invoiceList.clearValue();
        }       
        if(this.isExpenseInv){
                this.autoGenerateDO.setValue(false);
                this.autoGenerateDO.disable();
        }else{
                this.autoGenerateDO.reset();
                this.autoGenerateDO.enable();
        }
        if(this.isEdit){//in edit case need to preserve some data befor resetall
            this.billDate.setValue(Wtf.serverDate);              
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
        if(this.isEdit){//in edit case need to preserve some data befor resetall
            this.billDate.setValue(Wtf.serverDate);              
        }
        this.sequenceFormatStore.load();
        if(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId )
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
        if(!this.isEdit && !this.copyInv){
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
        this.currencyStore.load({params:{tocurrencyid:WtfGlobal.getCurrencyID(),mode:201,transactiondate:WtfGlobal.convertToGenericDate(new Date())}});
        this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body,{productname:"&nbsp;&nbsp;&nbsp;&nbsp;",productid:0,qty:0,soqty:0,poqty:0});
        this.currencyStore.on("load",function(store){
            if(this.resetForm){
                if(this.Currency.getValue() !="" && WtfGlobal.searchRecord(this.currencyStore,this.Currency.getValue(),"currencyid") == null){
                    callCurrencyExchangeWindow();
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invoice.msg1")],2); //"Please set Currency Exchange Rates"
                } else {
                    this.isCurrencyLoad=true;
                    this.Currency.setValue(WtfGlobal.getCurrencyID());
//                    this.currencyid=WtfGlobal.getCurrencyID();
                    this.applyCurrencySymbol();
                    this.showGridTax(null,null,true);
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
                    if(this.isEdit){
                        this.setProductAndTransactionTaxValues();
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
       this.template.setValue(Wtf.Acc_Basic_Template_Id);            
},
resetField: function(){
    this.moduleTemplateName.reset();
    this.templateModelCombo.reset();
    this.ShowOnlyOneTime.enable();
    this.Currency.reset();
    this.PO.reset();
    this.sequenceFormatCombobox.reset();
    this.Number.reset();
    this.billDate.reset();
    this.PORefNo.reset();
    this.autoGenerateDO.reset();
    this.CostCenter.reset();
    this.youtReftxt.reset();
    this.delytermtxt.reset();
    this.invoiceTotxt.reset();
    this.shipDate.reset();
    this.Term.reset();
    this.DueDate.reset();
    this.Memo.reset();
    this.shipvia.reset();
    this.fob.reset();
    this.includeProTax.reset();
    this.validTillDate.reset();
    this.partialInvoiceCmb.reset();
    this.template.reset();
    this.templateID.reset();
    this.users.reset();
    this.generateReceipt.reset();
    this.autoGenerateDO.reset();
    this.sequenceFormatComboboxDo.reset();
    this.no.reset();
    this.delydatetxt.reset();
    this.projecttxt.reset();
    this.depttxt.reset();
    this.requestortxt.reset();
    this.mernotxt.reset();
    this.Name.reset();
},
    
    resetCustomFields : function(){ // For reset Custom Fields, Check List and Custom Dimension
        var customFieldArray = this.tagsFieldset.customFieldArray;  // Reset Custom Fields
        for (var itemcnt = 0; itemcnt < customFieldArray.length; itemcnt++) {
            var fieldId = customFieldArray[itemcnt].id
            if (Wtf.getCmp(fieldId) != undefined && customFieldArray[itemcnt].getXType()!='fieldset') {
                Wtf.getCmp(fieldId).reset();
            }
        }
        
        var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray;  // Reset Check List
        for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
            var checkfieldId = checkListCheckBoxesArray[checkitemcnt].id
            if (Wtf.getCmp(checkfieldId) != undefined) {
                Wtf.getCmp(checkfieldId).reset();
            }
        }
        
        var customDimensionArray = this.tagsFieldset.dimensionFieldArray;  // Reset Custom Dimension
        for (var itemcnt1 = 0; itemcnt1 < customDimensionArray.length; itemcnt1++) {
            var fieldId1 = customDimensionArray[itemcnt1].id
            if (Wtf.getCmp(fieldId1) != undefined) {
                Wtf.getCmp(fieldId1).reset();
            }
        }
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
                this.billDate.setValue(Wtf.serverDate);//(new Date());
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
           this.isCustomer?Wtf.customerAccStore.load():Wtf.vendorAccStore.reload();
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
                        if(this.fromLinkCombo.getValue()==1){
                            this.includeProTax.setValue(false);
                            this.showGridTax(null,null,true);            
                        }
                    }
                    this.Memo.setValue(rec.data['memo']);
                        this.shipLength.setValue(rec.data['shiplengthval']);
                    this.shipDate.setValue(rec.data['shipdate']);
                    this.validTillDate.setValue(rec.data['validdate']);
                    if(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) { // set value only in VI module
                        this.invoiceList.setValue(rec.data['landedInvoiceID']);
                    }
                    if((this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) || (this.moduleid == Wtf.Acc_Invoice_ModuleId && !this.cash)) {
                        this.Term.setValue(rec.data['termdays']);
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
                        this.Tax.setValue(rec.data['taxid']);
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
                        this.Term.setValue(storerec.data['termdays']);
                    }        
                    this.CostCenter.setValue(rec.data.costcenterid);
                }else{ //if the user select multiple values
                    this.clearComponentValues();
                }
    },
    clearComponentValues:function(){
        this.Memo.setValue('');
        this.shipLength.setValue(1);
        this.shipDate.setValue('');
        this.validTillDate.setValue('');
        this.shipvia.setValue('');
        this.fob.setValue('');
        this.loadTransStore();
        this.Discount.setValue(0);
        this.perDiscount.setValue(false);
//        this.Tax.disable();
//        this.isTaxable.reset();
//        this.Tax.reset();                     
        this.CostCenter.setValue('');
        if(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) { // set value only in VI module
            this.invoiceList.setValue('');
        }
    },
    onCurrencyChangeOnly:function(){
        this.fromPO.reset();
        this.fromLinkCombo.reset();this.fromLinkCombo.setDisabled(true);
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
        if(termDetails!=""&&termDetails!=null&&termDetails!=undefined){
        var detailArr = eval(termDetails);
        for(var cnt=0; cnt<detailArr.length; cnt++ ){
            var jObj = detailArr[cnt];
            
            var record = this.termStore.queryBy(function(record){
                return (record.get('id') == jObj.id);
            }, this).items[0];
            if(record) {
                record.set('termamount',jObj.termamount);
                record.set('termpercentage',jObj.termpercentage);
            }

        }
        }
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
    addInvoiceTermGrid : function() {
        this.termcm=[{
            header: WtfGlobal.getLocaleText("acc.field.Term"),
            dataIndex: 'term'
        },{
            header: WtfGlobal.getLocaleText("acc.field.Percentage"),
            dataIndex: 'termpercentage',
            editor:new Wtf.form.NumberField({
                xtype : "numberfield", 
                maxLength : 15,
                allowNegative : false,
                minValue : 0,
                maxValue: 100,
                regexText:Wtf.MaxLengthText+"15"
            })
        },{
            header: WtfGlobal.getLocaleText("acc.dnList.gridAmt"),
            dataIndex: 'termamount',
            renderer : function(val, meta, rec) {
                if(typeof val=='number' && val>=0 && rec.data.sign==0) {
                    rec.set('termamount',val*(-1));
                    return val*(-1)
                } else 
                    return val;
            },
            editor:new Wtf.form.NumberField({
                xtype : "numberfield", 
                maxLength : 15,
                allowNegative : true,
                regexText:Wtf.MaxLengthText+"15"
            })
        }];
        
        this.termRec =new Wtf.data.Record.create([
        {name: 'id'},
        {name: 'term'},
        {name: 'glaccount'},
        {name: 'sign'
//        },{ name: 'category'
//        }, {name: 'includegst'
//        },{ name: 'includeprofit'
//        },{name: 'suppressamnt'
        },{name: 'formula'
        },{name: 'formulaids'
        },{name: 'termamount'
        },{name: 'termpercentage'
        }
        ]);
        this.termStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.termRec),
//            url:Wtf.req.account+'CompanyManager.jsp',
            url: 'ACCAccount/getInvoiceTermsSales.do',
            baseParams:{
                isSalesOrPurchase:this.isCustomer?true:false
        }
        });
//        if(!this.isEdit) {
            this.termStore.load();
            this.termStore.on('load',this.closeTermGrid,this);
//        }
//        this.calInvoiceTermBtn= new Wtf.Toolbar.Button({
//            text:"Apply Terms",
//            scope:this,
//            tooltip:{text:'Apply Terms'},
//            handler:function() {this.updateSubtotal()}
//        });
        this.termgrid = new Wtf.grid.EditorGridPanel({
            layout:'fit',
            clicksToEdit:1,
            store: this.termStore,
            height:100,
            autoScroll : true,
            disabledClass:"newtripcmbss",
            style:'padding-top:10px;',
            cm: new Wtf.grid.ColumnModel(this.termcm),
            border : false,
            loadMask : true,
//            tbar : this.calInvoiceTermBtn,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.termgrid.on('afteredit',this.updateSubtotalOnTermChange,this);        
    },
    
    closeTermGrid : function(obj){
        var store = this.termgrid.store;
//        if(this.termStore.data.length==0||this.cash)
            if(this.termStore.data.length==0)
            {
                this.termgrid.hide();
            }
            
    },  
    updateSubtotalOnTermChange : function(obj) {
        var recdata=obj.record.data;            
        for(var i=0; i<this.termStore.getCount(); i++) {
            var recdata = this.termStore.getAt(i).data; //  var recdata = obj.record.data;
            var store = obj.grid.store;
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
            if(obj.field=="termpercentage"){
//            obj.record.set('termamount',0);
                var opmod = recdata.sign==0 ? -1 : 1;
                var this_termTotal = ((Math.abs(termtotal) * recdata.termpercentage*1) / 100)*opmod;
                if(this.termStore.getAt(i).get('termpercentage') != '') {
                    this.termStore.getAt(i).set('termamount',this_termTotal);
                }
            }
//        else if(obj.field=="termamount") {
//            
//        }
        }
        this.updateSubtotal();
    },
    
    findTermsTotal : function() {
        var termTotal = 0;
        if(this.termgrid) {
            var store = this.termgrid.store;
            var totalCnt = store.getCount();
            for(var cnt=0; cnt<totalCnt; cnt++) {
                var lineAmt = store.getAt(cnt).data.termamount;
                if(typeof lineAmt=='number')
                    termTotal += getRoundedAmountValue(lineAmt);
            }
        }
        return getRoundedAmountValue(termTotal);
    },
    
    getInvoiceTermDetails : function() {
        var arr=[];
        if(this.termgrid) {
            var store = this.termgrid.store;
            store.each(function(rec){
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
          if(this.isExpenseInv)
              {
                  var total=store.getAt(i).data.amount;
              }else{
                  var total=store.getAt(i).data.amountwithouttax;
              }
//            if(this.editTransaction&&!this.fromPO){
//                    total=total/this.store.getAt(i).data['oldcurrencyrate'];
//                }
            subtotal+=total;
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
    getAddressWindow:function(){
       var custvendorid=this.Name.getValue();
       callAddressDetailWindow(this.addressrec,this.isEdit,this.copyInv,custvendorid,this.currenctAddressDetailrec,this.isCustomer,this.viewGoodReceipt,this.isViewTemplate); 
       Wtf.getCmp('addressDetailWindow').on('update',function(config){
            this.currenctAddressDetailrec=config.currentaddress;
       },this);
    },
   createAddressStore:function(){
       this.tranAddrRecord=new Wtf.data.Record.create([
             {name:'billingAddress'},
             {name:'billingCountry'},
             {name:'billingState'},
             {name:'billingPostal'},
             {name:'billingEmail'},
             {name:'billingFax'},
             {name:'billingMobile'},
             {name:'billingPhone'},
             {name:'billingContactPerson'},
             {name:'billingContactPersonNumber'},
             {name:'billingContactPersonDesignation'},
             {name:'billingCounty'},
             {name:'billingCity'},
             {name:'billingAddressType'},
             {name:'shippingAddress'},
             {name:'shippingCountry'},
             {name:'shippingState'},
             {name:'shippingCounty'},
             {name:'shippingCity'},
             {name:'shippingEmail'},
             {name:'shippingFax'},
             {name:'shippingMobile'},
             {name:'shippingPhone'},
             {name:'shippingPostal'},
             {name:'shippingContactPersonNumber'},
             {name:'shippingContactPersonDesignation'},
             {name:'shippingContactPerson'},
             {name:'shippingAddressType'}
        ]);
        
        this.addrStoreUrl = this.isCustomer? "ACCInvoiceCMN/getInvoicesMerged.do" : "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
        if(this.isOrder && !this.quotation){            
             this.addrStoreUrl = this.isCustomer? "ACCSalesOrderCMN/getSalesOrdersMerged.do" : "ACCPurchaseOrderCMN/getPurchaseOrdersMerged.do";
        } else if(this.quotation){
            this.addrStoreUrl = this.isCustomer? "ACCSalesOrderCMN/getQuotations.do" : "ACCPurchaseOrderCMN/getQuotations.do";
        } 
        this.transactionsAddressStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
               root: "data",
               totalProperty:"count"  
               },this.tranAddrRecord),
            url:this.addrStoreUrl,
            baseParams:{
                billid:this.billid,
                CashAndInvoice:true                   
            }
        });
   }    
})

