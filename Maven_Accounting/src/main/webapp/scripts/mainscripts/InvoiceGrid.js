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
Wtf.account.ProductDetailsGrid=function(config){
    config.enableColumnMove = !config.readOnly,
    config.enableColumnResize = !config.readOnly,
    this.isViewTemplate = config.isViewTemplate ? config.isViewTemplate : false;
    this.parentCmpID=config.parentCmpID;
    this.isCustomer=config.isCustomer;
    this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
    this.currencyid=config.currencyid;
    this.productID=null;
    this.soLinkFlag = null;
    this.updaterowtax = true;//used in conjunction with this.soLinkFlag
    this.id=config.id;
    this.heplmodeid=config.heplmodeid;//ERP-11098 [SJ]
    this.isOrder=config.isOrder;
    this.isFromGrORDO=(config.isFromGrORDO != null || config.isFromGrORDO != undefined)?config.isFromGrORDO:false;
    this.isJobWorkOrderReciever = (config.isJobWorkOrderReciever)?config.isJobWorkOrderReciever:false;
    this.record=config.record;
    this.billDate=new Date();
    this.dateChange=false;
    this.pronamearr=[];
    this.isGST=config.isGST;   // ERP-32829 
    this.isCashType=config.isCash;
    this.isInvoice=config.isInvoice;
    this.fromPO=config.fromPO;          
    this.readOnly=config.readOnly;
    this.copyInv=config.copyInv;
    this.editTransaction=config.editTransaction;
    this.editLinkedTransactionQuantity= Wtf.account.companyAccountPref.editLinkedTransactionQuantity;
    this.editLinkedTransactionPrice= Wtf.account.companyAccountPref.editLinkedTransactionPrice;
    this.UomSchemaType=Wtf.account.companyAccountPref.UomSchemaType;
    this.isEdit=config.isEdit;
    this.originalQty = 0;
    this.isLinkedTransaction= (config.isLinkedTransaction == null || config.isLinkedTransaction == undefined)? false : config.isLinkedTransaction;
    /*
     * isjobWorkWitoutGrn is true if sales invoice is creating from Aged order work report.
     */
    this.isjobWorkWitoutGrn= (config.isjobWorkWitoutGrn == null || config.isjobWorkWitoutGrn == undefined)? false : config.isjobWorkWitoutGrn;
     this.isAutoFilledBatchDetails=(Wtf.account.companyAccountPref.isAutoFillBatchDetails == undefined || Wtf.account.companyAccountPref.isAutoFillBatchDetails == false ) ? false : Wtf.account.companyAccountPref.isAutoFillBatchDetails;
    this.noteTemp=config.noteTemp;
    this.fromOrder=config.fromOrder;
    this.affecteduser = "";
    this.forCurrency="";
    this.gridConfigId = "";
    this.isExciseTab=config.isExciseTab;
    this.isTemplate=config.isTemplate;
    this.isCallFromSalesOrderTransactionForms=config.isCallFromSalesOrderTransactionForms!=undefined ? config.isCallFromSalesOrderTransactionForms:false;//isCallFromSalesOrderTransactionForms->True if PO is created from SO transaction Form
    if(config.isNote!=undefined)
        this.isNote=config.isNote;
    else
        this.isNote=false;
    this.isCN=config.isCN;
    this.moduleid = config.moduleid;
    //Flag to indicate whether Avalara integration is enabled and module is enabled for Avalara Integration or not
    this.isModuleForAvalara = (Wtf.account.companyAccountPref.avalaraIntegration && (config.moduleid == Wtf.Acc_Invoice_ModuleId || config.moduleid == Wtf.Acc_Cash_Sales_ModuleId || config.moduleid == Wtf.Acc_Sales_Order_ModuleId || config.moduleid == Wtf.Acc_Customer_Quotation_ModuleId || config.moduleid == Wtf.Acc_Sales_Return_ModuleId)) ? true : false;
    this.isViewCNDN=config.isViewCNDN;
    this.isQuotation=config.isQuotation;
    this.isRequisition =config.isRequisition;
    this.isQuotationFromPR =config.isQuotationFromPR;
    this.isRFQ =config.isRFQ;
    this.showWaringMsg = true;
    if(this.isCashType){
        if(this.isCustomer){
            this.moduleid=Wtf.Acc_Cash_Sales_ModuleId;
            config.moduleid=Wtf.Acc_Cash_Sales_ModuleId;
        }else{
            this.moduleid=Wtf.Acc_Cash_Purchase_ModuleId;
            config.moduleid=Wtf.Acc_Cash_Purchase_ModuleId;
        }
    }
    this.commonproductStore="";
    this.productComboStore=this.isCustomer?Wtf.productStoreSalesOptimized:Wtf.productStoreOptimized;
    this.sModel = new Wtf.grid.CheckboxSelectionModel({
            singleSelect :false
     });  
    this.sModel.on('selectionchange',function(){this.fireEvent('onselection',this);},this);
    this.sModel.on("beforerowselect", this.checkSelections, this);
    this.createStore();
    this.createComboEditor();
    this.createColumnModel();
    var colModelArray;
    if(this.moduleid ==Wtf.Acc_Cash_Sales_ModuleId){
        colModelArray = GlobalColumnModel[Wtf.Acc_Invoice_ModuleId ];
    }else if( this.moduleid==Wtf.Acc_Cash_Purchase_ModuleId){
        colModelArray = GlobalColumnModel[Wtf.Acc_Vendor_Invoice_ModuleId];
    }else{
        colModelArray = GlobalColumnModel[this.moduleid];
    }
    WtfGlobal.updateStoreConfig(colModelArray, this.store);
    colModelArray = [];
    if(this.moduleid ==Wtf.Acc_Cash_Sales_ModuleId){
        colModelArray = GlobalColumnModelForProduct[Wtf.Acc_Invoice_ModuleId ];
    }else if( this.moduleid==Wtf.Acc_Cash_Purchase_ModuleId){
        colModelArray = GlobalColumnModelForProduct[Wtf.Acc_Vendor_Invoice_ModuleId];
    }else{
        colModelArray = GlobalColumnModelForProduct[this.moduleid];
    }
    WtfGlobal.updateStoreConfig(colModelArray, this.store);
    if (GlobalProductmasterFieldsArr[this.moduleid] != undefined) {
        WtfGlobal.updateStoreConfig(GlobalProductmasterFieldsArr[this.moduleid], this.store);
    }
    this.duplicateStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },Wtf.productRec)
    });
    this.ProductMappedStore = new Wtf.data.Store({
        url:"ACCProductCMN/getUserMappedProducts.do",
        baseParams:{mode:22,common:'1',loadPrice:true,isCustomer:this.isCustomer},
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },Wtf.productRec)
    });
//    if (config.copyInv && this.moduleid==Wtf.Acc_Sales_Order_ModuleId) {
//        var loadingMask = new Wtf.LoadMask(document.body,{
//            msg : WtfGlobal.getLocaleText("acc.msgbox.50")
//        });
////        this.productComboStore.load();
//        this.productComboStore.on("beforeload", function() {
//            loadingMask.show();
//        }, this);
//        this.productComboStore.on("load", function() {
//            loadingMask.hide();
//        }, this);
//        
//    }
    Wtf.account.ProductDetailsGrid.superclass.constructor.call(this,config);
    this.addEvents({
        'datachanged':true, //Event fired when grid data is changed. Mostly used to calculate subtotal
        'pricestoreload':true,
        'onselection':true,//Event fired to enable Set warehouse/location
        'productselect' : true,//Event fired to load data for collapsible panel store
        'productdeleted' : true,//Event fired to remove data for collapsible panel store
        'customerchangepriceload' : true, //Event fired when customer is changed
        'vendorselect': true, // Event fire when vendor is selected at line level
        'gridconfigloaded':true//// Event fire when WtfGlobal.getGridConfig() called and config get applied to grid column
    });
    this.on('populateDimensionValue',this.populateDimensionValueingrid,this);
}
Wtf.extend(Wtf.account.ProductDetailsGrid,Wtf.grid.EditorGridPanel,{
    clicksToEdit:1,
    stripeRows :true,
    rate:1,
    symbol:null,
    disabledClass:"newtripcmbss",
    layout:'fit',
    viewConfig:{forceFit:true},
    forceFit:true,
    loadMask:true,
    onRender:function(config){
         Wtf.account.ProductDetailsGrid.superclass.onRender.call(this,config);
        this.isValidEdit = true;
        WtfGlobal.getGridConfig(this,this.moduleid,true,true);
        if (this.isRequisition || this.isRFQ) {
            this.on('render', this.addBlankRow, this);
        }
//         if(this.productOptimizedFlag!=Wtf.Products_on_Submit){
         this.on('afteredit', this.callUpdateRowOnProductComboStoreLoad, this);
//         } else {
//             this.on('afteredit',this.callupdateRowonProductLoad,this);
//         }
         if(Wtf.account.companyAccountPref.isLineLevelTermFlag && ((this.isEdit!=undefined && this.isEdit) || (this.copyInv != undefined && this.copyInv)) ){
             /**
              * - In Code Optimization added new personstore But in RFQ and PR modules  there no changes or not added any personstore,
              * so in edit, View and Copy case is not working for RFQ and PR
              * - personstore is undefiend for RFQ and PR
              * - issue occurred due to - ERP-35017
              * - issue resolved in  -  ERP-35440
              */
             if (this.isRequisition || this.isRFQ) {
                 this.parentObj.Name.store.load();
             }else{
                 this.parentObj.personstore.load(); // On edit case we need vendor/customer interstate property so this code is only for india
             }
         }
        this.on('validateedit',this.checkRow,this);
        this.on('rowclick',this.handleRowClick,this);
        this.on('cellclick',this.fetchBOMCodes,this);
        this.on('cellclick',this.RitchTextBoxSetting,this);
        this.on('columnmove', this.saveGridStateHandler, this);
        this.on('columnresize', this.saveGridStateHandler, this);
        this.on('afteredit', this.outstandingSOCheck, this);
        //After changing inspection template remove inspection form details saved at product level
        if(this.moduleid == Wtf.Acc_Sales_Order_ModuleId){
            this.on('afteredit', this.inspectionTemplateChange, this);
        }
        this.on('beforeedit',function(e){
            
//            /*------Tax fields at line level will remain Non-editable for Product mapped with tax ------  */
//            if (CompanyPreferenceChecks.mapTaxesAtProductLevel() && e.field == "prtaxid") {//Check for Malaysian Company
//                if (this.isCustomer) {
//                    if ((e.record.data.salestaxId != undefined && e.record.data.salestaxId != "")) {
//                        return false;
//                    }
//                } else {
//                    if ((e.record.data.purchasetaxId != undefined && e.record.data.purchasetaxId != "")) {
//                        return false;
//                    }
//                }
//            }
           
            if(this.disableRowEditing && e.row==this.disabledRow){
                return false;
            }
            if( e.field == "bomcode" ) {
                this.bomStore.on('beforeload',function(){
                    this.bomStore.baseParams.productid = e.record.get("productid");
                },this);
            }
         
            if(e.field == "productid" && e.grid.colModel.config[3].dataIndex=="productid"){                
                if(Wtf.account.companyAccountPref.invAccIntegration && !this.isCustomer && !this.isQuotation){                
                    var store = e.grid.colModel.config[3].editor.field.store;
                    if(store!=undefined && store.data.length>0){                    
                        this.duplicateStore.removeAll();
                        this.duplicateStore.add(store.getRange());                
                        this.duplicateStore.each(function(record){
                            if(record.data.isStopPurchase==true){
                                this.duplicateStore.remove(record);                                    
                            }
                        },this);                                
                        e.grid.colModel.config[3].editor.field.store=this.duplicateStore;
                    }                
                }
            }  
         
            if(e.field == "uomid" && this.UomSchemaType==Wtf.UOMSchema){ //Does not allow to change UOM in case product is not MULTIUOM Type ERP-8319
                var beforeEditRecord=undefined;
                    beforeEditRecord = WtfGlobal.searchRecord(this.store, e.record.data.productid, 'productid');
                    if(beforeEditRecord != undefined && beforeEditRecord.data.multiuom != true ){ 
                        e.cancel = true;
                        return; 
                    }
            } else if(e.field == "uomid" && e.record.data.productid !="" && this.UomSchemaType == Wtf.PackegingSchema ){
                var prorec=undefined;
                    prorec = WtfGlobal.searchRecord(this.store, e.record.data.productid, 'productid');
                    if (prorec != undefined && prorec.data.multiuom != true) {
                        e.cancel = true;
                        return;
                    }
                if(prorec!=undefined){
                    for (var k = 0; k < e.grid.colModel.config.length; k++) {
                        if(e.grid.colModel.config[k].editor && e.grid.colModel.config[k].editor.field.store && e.grid.colModel.config[k].dataIndex=='uomid'){ 
                            var store = e.grid.colModel.config[k].editor.field.store;                          
                            store.clearFilter();
                            store.filterBy(function(rec) {
                                if ((prorec.data.caseuom != undefined && prorec.data.caseuom == rec.data.uomid )||(prorec.data.inneruom !=undefined  && prorec.data.inneruom == rec.data.uomid) ||(prorec.data.stockuom!=undefined && prorec.data.stockuom == rec.data.uomid))
                                    return true
                                else 
                                    return false
                            }, this);
                        }
                    }
                }                 
            }  
            if (e.field == "uomid") {
                this.uomStoreBasedOnUOMSchema.load({params: {productid: e.record.data.productid,isSalesModule:this.isCustomer}});
            }
            if(this.isLinkedTransaction && (e.field == "quantity" || e.field == "prdiscount" || e.field == "discountispercent" || e.field == "rate" || e.field == "productid" || e.field=="taxamount" || e.field=="prtaxid")){
                e.cancel=true;
            }
            
            /*
             * isjobWorkWitoutGrn is true if sales invoice is creating from Aged order work report.
             */
             if(this.isjobWorkWitoutGrn   && (e.field == "quantity")){
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.jobwork.editQuantity")], 2);
                e.cancel=true;
            }
            if(e.field == "taxamount" && this.parentObj && this.parentObj.includingGST && this.parentObj.includingGST.getValue()){
                e.cancel=true;
            }
         var isRateFieldEditable = true;
        
            /**
             * On Window and Firefox browser rate column editable if Including GST check ON
             * This Problem is for Core ERP also ERP-34717
             */
            if (e.field == "rate" && this.parentObj.includingGST && this.parentObj.includingGST.getValue()) {
                 e.cancel=true;
            }
            if(!this.isValidEdit){ // Fixed Bug[13888]: Overlaping text box on validation alert messages. [on TAB key navigation]
                e.cancel= true;
                this.isValidEdit = true;
            }
            if((e.field == "rate")&& ((e.record.data.isNewRecord =="" && !this.isEdit ) ||(e.record.data.linkid !="" && this.isEdit))){//isNewRecord for nornal records is "1"
                if(this.editLinkedTransactionPrice && (((this.fromPO||(this.isEdit && this.fromPO==false)) && !this.isCustomer && (this.soLinkFlag==false||(this.isEdit && this.soLinkFlag==null )) )||((this.isOrder||this.isQuotation) && this.isCustomer && (this.soLinkFlag==false||(this.isEdit && this.soLinkFlag==null)) )||(!this.isOrder && this.isCustomer && (this.soLinkFlag==false||(this.isEdit && this.soLinkFlag==null))))&& !this.isRequisition ){  //|| (this.fromOrder && this.isCustomer)
                    e.cancel = true;
                    isRateFieldEditable = false;
                }
            }
             
            if(e.field == "rate" && !this.isFromGrORDO && isRateFieldEditable){// Product rate will be editable only if  1) it has edit permission set in Account preferences and{ 2) transaction is being created by linking GR OR DO or 3) login user has price amending permission to edit it) .                 
                if(this.isCustomer){
                    if(this.isInvoice){
                        if(!Wtf.productPriceEditPerm.priceEditPerm.invoice){
                            e.cancel = true;
                        }
                    }else if(this.isCash){
                        if(!Wtf.productPriceEditPerm.priceEditPerm.invoice){
                            e.cancel = true;
                        }
                    }else if(this.isOrder && !this.isQuotation){
                        if(!Wtf.productPriceEditPerm.priceEditPerm.salesOrder){
                            e.cancel = true;
                        }
                    }else if(this.isQuotation){
                        if(!Wtf.productPriceEditPerm.priceEditPerm.customerQuotation){
                            e.cancel = true;
                        }
                    }
                }else{
                    if(this.isInvoice){
                        if(!Wtf.productPriceEditPerm.priceEditPerm.goodsReceipt){
                            e.cancel = true;
                        }
                    }else if(this.isCash){
                        if(!Wtf.productPriceEditPerm.priceEditPerm.goodsReceipt){
                            e.cancel = true;
                        }
                    }else if(this.isOrder && !this.isQuotation && !this.isRequisition){
                        if(!Wtf.productPriceEditPerm.priceEditPerm.purchaseOrder){
                            e.cancel = true;
                        }
                    }else if(this.isQuotation){
                        if(!Wtf.productPriceEditPerm.priceEditPerm.vendorQuotation){
                            e.cancel = true;
                        }
                    }
                }
                var beforeEditRecord=this.productComboStore.getAt(this.productComboStore.find('productid',e.record.data.productid));
                if(beforeEditRecord == undefined || beforeEditRecord == null){
                        if(e.record.data ==undefined || e.record.data.productid == undefined || e.record.data.productid ==""){ 
                            e.cancel = true;
                        }
                }//else{
            //            		   if(beforeEditRecord.data.producttype != Wtf.producttype.service){
            //	               	   e.cancel = true;
            //	            	 }
            //            	 }
            }
            else if(e.field == "desc" && Wtf.account.companyAccountPref.ishtmlproddesc){
                e.cancel=true;
                if(e.record.data.productid!="")
                    this.getPostTextEditor(e);
                return; 
            }
            
            if(e.field == "vendorid"){ // Not allow to select vendor if product is not selected
                if(e.record.data ==undefined || e.record.data.productid == undefined || e.record.data.productid ==""){ 
                    e.cancel = true;
                }
            }
            if(e.field == "vendorunitcost"){ // Not allow to edit vendorunitcost if product && vendor is not selected 
                if(e.record.data ==undefined || e.record.data.productid == undefined || e.record.data.productid ==""){ 
                    e.cancel = true;
                }else if(e.record.data.vendorid == undefined || e.record.data.vendorid ==""){ 
                    e.cancel = true;
                }
            }
            if(e.field == "vendorcurrexchangerate"){ // Not allow to edit vendorcurrexchangerate if product && vendor is not selected 
                if(e.record.data ==undefined || e.record.data.productid == undefined || e.record.data.productid ==""){ 
                    e.cancel = true;
                }else if(e.record.data.vendorid == undefined || e.record.data.vendorid ==""){ 
                    e.cancel = true;
                }
            }
            
            var isQuantityFieldEditable = true;
            if((e.field == "quantity")&& ((e.record.data.isNewRecord =="" && !this.isEdit ) ||(e.record.data.linkid !="" && this.isEdit))){//isNewRecord for normal records is "1"
                if(this.editLinkedTransactionQuantity && (((this.fromPO||(this.isEdit && this.fromPO==false)) && !this.isCustomer && (this.soLinkFlag==false||(this.isEdit && this.soLinkFlag==null )) )||((this.isOrder||this.isQuotation) && this.isCustomer && (this.soLinkFlag==false||(this.isEdit && this.soLinkFlag==null)) )||(!this.isOrder && this.isCustomer && (this.soLinkFlag==false||(this.isEdit && this.soLinkFlag==null))))&& !this.isRequisition ){  //|| (this.fromOrder && this.isCustomer)
                    e.cancel = true;
                    isQuantityFieldEditable = false;
                }
            }
            if(e.field == "quantity" && !this.isFromGrORDO && isQuantityFieldEditable){
                var beforeEditRecord=this.productComboStore.getAt(this.productComboStore.find('productid',e.record.data.productid));
                if (beforeEditRecord == undefined || beforeEditRecord == null) {
                    if (e.record.data == undefined || e.record.data.productid == undefined || e.record.data.productid == "") {
                        e.cancel = true;
                    }
                }
            }
            if (e.field.indexOf("Custom_") == 0) {
                if (this.readOnly || this.isViewTemplate) {
                    return false;
                }
             }
            if(this.parentObj && this.parentObj.Currency != undefined){
                this.forCurrency=this.parentObj.Currency.getValue();
            } 
            if(e.record.data !=undefined && e.record.data.productid != undefined && e.record.data.productid !=""){   
                Wtf.Ajax.requestEx({
                    url: "ACCProduct/getIndividualProductPrice.do",
                    params: {
                        productid: e.record.data.productid,
                        affecteduser: this.affecteduser,
                        forCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency:"",
                        currency: this.parentObj.Currency.getValue(),
                        quantity: e.record.data.quantity,
                        transactiondate: WtfGlobal.convertToGenericDate(this.billDate),
                        carryin: (this.isCustomer) ? false : true,
                        uomid: e.record.data.uomid
                    }
                }, this, function(response) {

                    for (var i = 1; i < response.data.length; i++) {
                        var dataObj = response.data[i];
                        var key = dataObj.key;
                     if(key != undefined){
                        var custValue = dataObj[key];
                        for (var k = 0; k < e.grid.colModel.config.length; k++) {
                            if (e.grid.colModel.config[k].editor && e.grid.colModel.config[k].editor.field.store && e.grid.colModel.config[k].dataIndex == key) {

                                var store = e.grid.colModel.config[k].editor.field.store;
                                store.clearFilter();
                                store.filterBy(function(rec) {
                                    var recId = rec.data.id;
                                    if ((custValue.indexOf(recId) !== -1))
                                        return true;
                                    else
                                        return false;
                                }, this);
                            }
                        }

                    }
                }
                }, this);
                if(SATSCOMPANY_ID==companyid){
                    if (e.field == "dependentType" && Wtf.account.companyAccountPref.dependentField) {               
                        var beforeEditRecordNew=this.productComboStore.getAt(this.productComboStore.find('productid',e.record.data.productid));
                        if(beforeEditRecordNew){
                            var custValue=beforeEditRecordNew.data.dependentType;
                            if(custValue==""){  
                                return false;
                            }  
                        }
                    }else if (e.field == "timeinterval" && Wtf.account.companyAccountPref.dependentField){
                        var beforeEditRecordNew=this.productComboStore.getAt(this.productComboStore.find('productid',e.record.data.productid));
                        if(beforeEditRecordNew){
                            var custValue=beforeEditRecordNew.data.timeintervalChk;
                            if(custValue=="")  
                                return false;
                        }
                    }
                    if (e.field == "dependentType" && Wtf.account.companyAccountPref.dependentField) {          
                        var beforeEditRecordNew=this.productComboStore.getAt(this.productComboStore.find('productid',e.record.data.productid));
                        if(beforeEditRecordNew){
                            var custValue=beforeEditRecordNew.data.parentDependentType;
                            for (var k = 0; k < e.grid.colModel.config.length; k++) {   
                                if(e.grid.colModel.config[k].editor && e.grid.colModel.config[k].editor.field.store && e.grid.colModel.config[k].dataIndex=='dependentType'){ 
                                    var store = e.grid.colModel.config[k].editor.field.store;
                                    store.clearFilter();
                                    store.filterBy(function(rec) {
                                        var recId = rec.data.type;
                                        if ((custValue.indexOf(recId) !== -1))
                                            return true;
                                        else
                                            return false;
                                    }, this);
                                }     
                            }
                        }
                    }else if (e.field == "showquantity" && e.record.data.productid !="" && Wtf.account.companyAccountPref.dependentField) {               
                        if(e.record.data.isparentproduct){
                            return false;    
                        }
                        var beforeEditRecordNew=this.productComboStore.getAt(this.productComboStore.find('productid',e.record.data.productid));
                        if(beforeEditRecordNew){
                            var custValue=beforeEditRecordNew.data.timeintervalChk;
                            if(custValue){     
                                e.cancel=true;
                                this.createSATSIntervalWindow(e);
                                return; 
                            }else{
                                custValue=beforeEditRecordNew.data.noofquqntity;
                                var qtyValue=beforeEditRecordNew.data.noofqtyvalue;
                                if(custValue!=""){
                                    e.cancel=true;
                                    this.createSATSExtraQuantityWindow(e,custValue,qtyValue)
                                    return; 
                                }
                            }
                        }
                    }else if (e.field == "productid" && Wtf.account.companyAccountPref.dependentField) {
                        if(e.record.data.issubproduct){
                            return false;
                        }else{
                            for (var k = 0; k < e.grid.colModel.config.length; k++) {
                                if(e.grid.colModel.config[k].editor && e.grid.colModel.config[k].editor.field.store && e.grid.colModel.config[k].dataIndex=='productid'){ 
                                    var store = e.grid.colModel.config[k].editor.field.store;
                                    store.clearFilter();
                                    store.filterBy(function(rec) {
                                        var recId = rec.data.parentid;
                                        if (recId == "")
                                            return true;
                                        else
                                            return false;
                                    }, this);
                                }
                            }
                        }
                    }else if (e.field == "rate" && e.record.data.productid !="" && Wtf.account.companyAccountPref.dependentField) {
                        if(e.record.data.isparentproduct){
                            return false;    
                        }
                    }
                }
             
            } 
        },this);
         if(!this.isNote && !this.readOnly){
//	         if(this.record == null || this.record == undefined && this.getColumnModel().getColumnById(this.id+"prtaxid").hidden == undefined && this.getColumnModel().getColumnById(this.id+"taxamount").hidden == undefined){
	         if(this.record == null || this.record == undefined && this.getColumnModel().isHidden(this.getColumnModel().findColumnIndex("prtaxid")) == undefined && this.getColumnModel().isHidden(this.getColumnModel().findColumnIndex("taxamount")) == undefined){
                         this.getColumnModel().setHidden(this.getColumnModel().findColumnIndex("prtaxid"), true);				// 21241   If statement added bcos could not use the event destroy for column model
	        	 this.getColumnModel().setHidden(this.getColumnModel().findColumnIndex("taxamount"), true);							// and also could not call the createColumnModel() method from onRender
	         }
         } 
         if(this.isLinkedTransaction){
            this.productEditor.setDisabled(true);
            this.vendorEditor.setDisabled(true);
            this.uomEditor.setDisabled(true);
            this.typeEditor.setDisabled(true);
            this.cmbAccount.setDisabled(true);
            this.rowDiscountTypeCmb.setDisabled(true);
            this.permiteditor.setDisabled(true);
            this.transDiscount.setDisabled(true);
            this.transTaxAmount.setDisabled(true);
            this.partAmount.setDisabled(true);
            this.transTax.setDisabled(true);
            this.transQuantity.setDisabled(true);
            this.transBaseuomrate.setDisabled(true);
            this.editprice.setDisabled(true);
            this.editPriceIncludingGST.setDisabled(true);
            this.editCost.setDisabled(true);
            this.editExchangeRate.setDisabled(true);
         }
         this.hideShowCustomizeLineFields();
         
        /*----Showing tax field at line level for Malaysian country when "Map taxes at product level" check is ON----- */
        if (CompanyPreferenceChecks.mapTaxesAtProductLevel() && this.parentObj!=undefined && this.parentObj.includeProTax && this.parentObj.includeProTax.getValue()) {

            var rowtaxindex = this.getColumnModel().findColumnIndex("prtaxid");
            var rowtaxamountindex = this.getColumnModel().findColumnIndex("taxamount");
            this.getColumnModel().setHidden(rowtaxindex, false);
            this.getColumnModel().setHidden(rowtaxamountindex, false);
        }
    },
    
    saveGridStateHandler: function() {
        var grid = this;
        var state = grid.getState();
        if(!this.readOnly){
            WtfGlobal.saveGridStateHandler(this,grid,state,this.moduleid,this.gridConfigId,true);
        }
    },
    
     callUpdateRowOnProductComboStoreLoad: function(obj) {
         if (obj != undefined || obj != null) {
             this.obj=obj;
             if(this.obj.field=='pid'){
                 if (this.productOptimizedFlag != Wtf.Products_on_Submit) {
                     if (this.obj.record.data != undefined && this.obj.record.data != null) {
                        var rec = this.obj.record.data;
                        if (rec.productid != undefined && rec.productid != null && rec.productid != "") {
                            if(!(this.store.getCount()>0&&this.store.getAt(this.store.getCount()-1).data['productid'].length<=0)){
                                this.addBlankRow();
                            }
                            var productidarray = [];
                            productidarray.push(rec.productid);
                            this.productComboStore.load({
                                params: {
                                    ids: productidarray
                                },
                                callback: this.updateRow.createDelegate(this, [this.obj])
                            });
                        }
                    }
                 } else {
                    if (this.obj.record && this.obj.record.data != undefined && this.obj.record.data != null) {
                        var rec = this.obj.record.data;
                        if (rec.pid == undefined || rec.pid == null || rec.pid == "") {// If Product ID removed we reset it. not allow to remove product id text 
                            if (this.store.getCount() > this.obj.row && this.obj.originalValue != "" && this.obj.originalValue != this.obj.value) {
                                this.obj.record.set(this.obj.field, this.obj.originalValue);
                                obj.cancel = true;
                                return;
                            } else {
                                obj.cancel = true;
                                return;
                            }
                        }
                    }
                    this.productComboStore.on('load',this.callupdateRowonProductSubmit,this);
                    this.productComboStore.load();
                 }
             }else{
                 this.updateRow(this.obj);
                 
             }
         }
     },
    callupdateRowonProductSubmit: function () {
        var obj = this.obj;
        if (this.productComboStore.getCount() <= 0) {
            if (this.store.getCount() > this.obj.row && this.obj.originalValue != "" && this.obj.originalValue != this.obj.value && this.obj.record.data.productid != "") {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.common.productWithSpecifiedId") + " " + this.obj.value + " " + WtfGlobal.getLocaleText("acc.common.productDoesNotExistsOrInDormantState")], 2);
                this.obj.record.set(this.obj.field, this.obj.originalValue);
                obj.cancel = true;
                return;
            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.common.productWithSpecifiedId") + " " + this.obj.value + " " + WtfGlobal.getLocaleText("acc.common.productDoesNotExistsOrInDormantState")], 2);
                this.obj.record.set(this.obj.field, "");
                obj.cancel = true;
                return;
            }
        } else {
            this.updateRow(this.obj);
            this.productComboStore.un('load',this.callupdateRowonProductSubmit,this);
        }
    },
     getPostTextEditor: function(e)
    {
        var _tw=new Wtf.EditorWindowQuotation({
            val:e.record.data.desc,
            id:"abcd"
        });
    	this.remark.focus.defer(150,true);
        _tw.on("okClicked", function(obj){
            var postText = obj.getEditorVal().textVal;
            var styleExpression  =  new RegExp("<style.*?</style>");
            postText=postText.replace(styleExpression,"");
            e.record.set("desc",postText);
        }, this);         
        _tw.show();
        
         
        
        
    },
     createStore:function(){
        this.deleteRec = new Wtf.data.Record.create([
        {
            name: 'productid'
        },

        {
            name: 'productname'
        },

        {
            name: 'productquantity'
        },
        {
            name: 'productbaseuomrate'
        },
        {
            name: 'productbaseuomquantity'
        },
        {
            name: 'productuomid'
        },
        {
            name: 'productinvstore'
        },
        {
            name: 'productinvlocation'
        },
        {
            name: 'productrate'
        }
        ]);
        this.deleteStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.deleteRec)                
        });
         this.priceRec = Wtf.data.Record.create ([
            {name:'productid'},
            {name:'productname'},
            {name:'desc'},
            {name:'uomid'},
            {name:'uomname'},
            {name:'displayUoMid'},
            {name:'displayUoMName'},
            {name:'displayuomrate'},
            {name: 'multiuom'},
            {name: 'blockLooseSell'},
            {name:'parentid'},
            {name:'parentname'},
            {name:'purchaseaccountid'},
            {name:'salesaccountid'},
            {name:'purchaseretaccountid'},
            {name:'salespricedatewise'},
            {name:'purchasepricedatewise'},
            {name:'salesretaccountid'},
            {name:'reorderquantity'},
            {name:'pricedatewise'},
            {name:'amendpurchaseprice'},
            {name:'isamendpurchasepricenotavail'},
            {name:'quantity'},
            {name:'reorderlevel'},
            {name:'leadtime'},
            {name:'purchaseprice'},
            {name:'saleprice'},
            {name: 'leaf'},
            {name: 'type'},
            {name:'prtaxid'},
            {name:'taxamount'},
            {name:'prtaxpercent'},
            {name:'prtaxname'},
            {name:'location'},
            {name:'warehouse'},
         //        {name: 'currencysymbol'},
        //        {name: 'currencyrate'},
            {name: 'level'},
            {name: 'initialquantity',mapping:'initialquantity'},
            {name: 'initialprice'},
            {name:'shelfLocation'},
            {name: 'producttype'},
            {name:'productpurchaseaccountid'},
            {name:'productaccountid'},
            {name:'productsalesaccountid'}
        ]);

        this.priceStore = new Wtf.data.Store({
        //        url:Wtf.req.account+'CompanyManager.jsp',
            url:"ACCProduct/getProductsForCombo.do",
            baseParams:{mode:22,
            termSalesOrPurchaseCheck:this.isCustomer
                },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.priceRec)
        });

//        this.productComboStore = new Wtf.data.Store({
//            //        url:Wtf.req.account+'CompanyManager.jsp',
//                url:"ACCProduct/getProducts.do",
//                baseParams:{
//                	loadInventory:this.isCustomer
//                    },
//                reader: new Wtf.data.KwlJsonReader({
//                    root: "data"
//                },this.priceRec)
//            });
        //this.productComboStore.load();

        this.priceStore.on('load',this.setGridProductValues,this);
//       this.priceStore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(this.billDate)}});
        this.storeRec = Wtf.data.Record.create([
            {name:'rowid',defValue:null},
            {name:'productname',mapping:(this.isViewCNDN)?'productdetail':null},
            {name:'billid'},
            {name:'billno'},
            {name:'bomid'},
            {name:'selectedJobStockOutid'},
            {name:'bomcode'},
            {name:'Cust_billno'},
            {name:'productid'},
            {name:'desc'},
            {name:'quantity'},
            {name:'showquantity' ,defValue:0},
            {name:'parentid'},
	   {name:'parentname'},
	   {name:'availablequantity'},
            {name:'baseuomquantity',defValue:1.00},
            {name:'availableQtyInSelectedUOM',defValue:0.00},
            {name:'isAnotherUOMSelected'},
            {name:'blockLooseSell'},
            {name:'pocountinselecteduom'},
            {name:'socountinselecteduom'},
            {name:'reservestock'},
            {name:'lockquantity'},
            {name:'baseuomname'},
            {name:'multiuom'},
            {name:'uomname'},
            {name:'displayUoMName'},
            {name:'displayuomrate',defValue:1.00},
            {name:'displayuomvalue',defValue:""},
            {name:'baseuomid'},
            {name:'uomid'},
            {name:'displayUoMid'},
            {name:'inspectionTemplate'},
            {name:'inspectionForm'},
            {name:'inspectionAreaDetails'},
            {name:'stockuom'},
            {name:'caseuom'},
            {name:'inneruom'},
            {name:'caseuomvalue'},
            {name:'inneruomvalue'},
            {name:'baseuomrate',defValue:1.00},
            {name:'copyquantity',mapping:'quantity'},
            {name:'uomschematypeid'},
            {name:'rate'},
            {name:'rateIncludingGst'},
            {name:'gstCurrencyRate',defValue:'0.0'},
            {name:'isRateIncludingGstEnabled',defValue:'0'},
            {name:'rateinbase'},
            {name:'partamount',defValue:0},
            {name:'sumOfAllLastInvDiscount',defValue:0},
            {name:'differnceInDisct',defValue:0},
            {name:'partialDiscount',defValue:0},
            {name:'discamount'},
            {name:'discount'},
            {name:'discountispercent',defValue:1},
            {name:'prdiscount',defValue:0},
            {name:'invstore'},
            {name:'invlocation'},
            {name:'prtaxid'},
            {name:'prtaxname'},
            {name:'prtaxpercent',defValue:0},
            {name:'taxamount',defValue:0},
            {name:'amount',defValue:0},
            {name:'amountwithtax',defValue:0},
            {name:'amountwithouttax',defValue:0},// used this field for Invoice Terms - rate*qty-discount
            {name:'taxpercent'},
            {name:'remark'},
            {name:'transectionno'},
            {name:'remquantity'},
            {name:'remainingquantity'},
            {name:'oldcurrencyrate',defValue:1},
            {name: 'currencysymbol',defValue:this.symbol},
            {name: 'currencyrate',defValue:1},
            {name: 'externalcurrencyrate'},
            {name:'orignalamount'},
            {name:'lineleveltermamount',defValue:0},
            {name:'typeid',defValue:0},
            {name:'isNewRecord',defValue:'1'},
            {name:'producttype'},
            {name:'permit'},
            {name:'linkto'},
            {name:'isJobWorkGrn'},
            {name:'linkid'},
            {name:'linktype'},
            {name:'savedrowid'},
            {name:'docrowid'},
            {name:'batchdetails'},
            {name:'recTermAmount'},
            {name:'OtherTermNonTaxableAmount'},
            {name:'LineTermdetails'},
            {name:'taxclass'},
            {name:'taxclasshistoryid'},
            {name:'uncheckedTermdetails'},
            {name: 'ProductTermdetails'},
            {name:'islockQuantityflag'},
            {name:'changedQuantity'},
            {name:'approvedcost'},
            {name:'approverremark'},
            /*For SATS*/
            {name:'timeintervalChk'},
            {name:'addshiplentheithqty'},
            {name:'timeinterval'},
            {name:'istimeinterval'},
            {name:'isopenedfirsttime',defValue:true},
            {name:'inouttime'},
            {name:'parentDependentType'},
            {name:'dependentType'},
            {name:'dependentTypeNo'},
            {name:'dependentTypeQty'},
            {name:'hourtimeinterval'},
            /**********/
            {name:'customfield'},
            /*For SATS*/
            {name:'issubproduct',defValue:false},
            {name:'isparentproduct',defValue:false},
            /**********/
            {name:'gridRemark'},
            {name:'productcustomfield'},
            {name:'accountId'},
            {name:'salesAccountId'},
            {name:'discountAccountId'},
            {name:'rowTaxAmount'},
            {name:'type'},                        
            {name:'typeid'},
            {name:'shelfLocation'},
            {name:'productcustomfield'},
            {name:'supplierpartnumber'},
            {name:'copybaseuomrate',mapping:'baseuomrate'},  //for handling inventory updation 
            {name: 'price'}, // added in record due to set auto populate value of price in add price in master window
            {name: 'customer'}, // added in record due to set auto populate value of customer in add price in master window
            {name: 'vendor'}, // added in record due to set auto populate value of vendor in add price in master window
            {name:'pid'},
            {name:'priceSource'},
            {name:'pricingbandmasterid'},
            {name:'pricingbandmastername'},
            {name:'volumdiscountid'},//added in record to get volumediscount
            //{name:'oldunitprice'},//added in record to get unit price of product given by user while creating document
            {name:'oldunitpriceFlag'},//added in record to set older price when user changes qty.
            {name: 'isLocationForProduct'},
            {name: 'isRowForProduct'},
            {name: 'isRackForProduct'},
            {name: 'isBinForProduct'},
            {name: 'isWarehouseForProduct'},
            {name: 'isBatchForProduct'},
            {name: 'isSerialForProduct'},
            {name: 'isSKUForProduct'},
            {name: 'isWarehouseLocationsetCopyCase'},
            {name: 'location'},
            {name: 'warehouse'},
            {name: 'vendorid'},
            {name: 'vendorunitcost'},
            {name: 'vendorcurrexchangerate'},
            {name: 'vendorcurrencyid'},
            {name: 'vendorcurrencysymbol'},
            {name: 'totalcost'},
            {name: 'profitmargin'},
            {name: 'profitmarginpercent'},
            {name: 'srno', isForSequence:true},
            {name: 'bomValuationArray'},        
            {name: 'isAutoAssembly'},
            {name: 'orderrate'},
            {name: 'productpurchaseaccountid'},
            {name: 'productsalesaccountid'},
            {name: 'productaccountid'},
            {name: 'socount'},
            {name: 'sicount'},
            {name:'compairwithUOM'},
            {name:'compairwithUOMVAT'},
            {name:'productMRP'},
            {name:'valuationType'},
            {name:'valuationTypeVAT'},
            {name:'quantityInReportingUOM'},
            {name:'quantityInReportingUOMVAT'},
            {name:'reortingUOMExcise'},
            {name:'reortingUOMSchemaExcise'},
            {name:'reportingUOMVAT'},
            {name:'reportingUOMSchemaVAT'},
            {name: 'hasAccess'},
            {name: 'productweightperstockuom'},
            {name: 'productweightincludingpakagingperstockuom'},
            {name: 'productvolumeperstockuom'},
            {name: 'productvolumeincludingpakagingperstockuom'},
            {name: 'marginCost'},
            {name: 'marginExchangeRate'},
            {name: 'dealerExciseDetails'},
            {name: 'supplierExciseDetails'},
            {name: 'dealerExciseTerms'},
            {name: 'joborderitem'},
            {name:'maxorderingquantity',defValue:0.0},
            {name:'minorderingquantity',defValue:0.0},
            {name: 'isPOfromSO'},
            {name: 'interstateparty'},
            {name: 'cformapplicable'},
            {name: 'customeridforshippingaddress'},
            {name: 'opensocount'},
            {name: 'openpoocount'},
            {name:'lineleveltaxtermamount',defValue:0.0},
            {name: 'termids'},//SDP-12509
            {name:'appliedTDS', type:'string'},// Contain TDS details record for TDS calculation window
            {name:'tdsamount', defValue:0.0},
            {name: 'qtipdiscountstr'},
            {name: 'discountjson'},
            {name: 'discountData'},            
            {name: 'individualproductprice'},
            {name: 'tdsjemappingID'},
            {name: 'billblockstatus'},
            {name: 'israteIncludingGst'},
            {name: 'barcodetype'},
            {name: 'grorowid'},
            {name:'joborderdetails'},
            {name:'joborderdetail'},
            {name: 'isJobWorkOutProd'},
            {name :'purchasetaxId'},
            {name: 'salestaxId'},
            {name:'amendpurchaseprice'},
            {name:'isamendpurchasepricenotavail'},
            {name: 'sourcepurchaseorderdetailid'},
            {name:'replacebatchdetails'},
            {name:'isUserModifiedTaxAmount', defValue:false},            
            {name:'linkDate'},//For date of linked document
            {name:'itctype',defValue:'1'}
        ]);
        var url=Wtf.req.account+((this.fromOrder||this.readOnly)?((this.isCustomer)?'CustomerManager.jsp':'VendorManager.jsp'):((this.isCN)?'CustomerManager.jsp':'VendorManager.jsp'));
        if(this.fromOrder)
           url=Wtf.req.account+(this.isCustomer?'CustomerManager.jsp':'VendorManager.jsp');
        this.store = new Wtf.data.Store({
            url:url,
            pruneModifiedRecords:true,
//            sortInfo:{
//                field:'srno',
//                direction:'ASC'
//            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.storeRec)
        });
        this.store.on('load',this.loadPOProduct,this);
 //       chkProductPriceload();
    },
    /*For SATS*/ 
    createSATSExtraQuantityWindow:function(e,no,value){
        this.extraQuantity = new Wtf.Panel({
            autoHeight: true,
            width:'97%',
            layout:'form',
            border: false
        });
        this.AddEditForm = new Wtf.form.FormPanel({
            region:"south",
            border:false,
            bodyStyle:"background-color:#f1f1f1;padding: 20px",
            items:[this.extraQuantity]
        });
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:90,
            border:false,
            bodyStyle:"background:white;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml("Manage Multiple Quantity","Manage multiple quantity for '"+e.record.data.productname  ,'../../images/createuser.png',false,'0px 0px 0px 0px')
        });
        this.addIntervalWindow = new Wtf.Window({
            modal: true,
            title: "Manage Quantity",
            bodyStyle: 'padding:5px;',
            buttonAlign: 'right',
            width: 425,
            scope: this,
            items: [{
                region: 'center',
                border: false,
                bodyStyle: 'background:#f1f1f1;font-size:10px;',
                autoScroll: true,
                items: [this.northPanel,this.AddEditForm]
            }],
            buttons: [{
                text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                scope: this,
                handler: function(button) {
                    if(!this.AddEditForm.getForm().isValid()) {
                        return;
                    }else{
                        var val=1;
                        var valStr="";
                        for(var i=0;i<qtyArray.length;i++){
                            var uomName="";
                            var idx = Wtf.uomStore.find("uomid", qtyArray[i]);
                            if(idx != -1){               
                                var rec =  Wtf.uomStore.getAt(idx);
                                uomName=rec.get("uomname");
                                val*= Wtf.getCmp("combofield"+(i+1)).getValue();
                                if(valStr.length>0)
                                    valStr+="X"+ Wtf.getCmp("combofield"+(i+1)).getValue()+" "+uomName
                                else
                                    valStr= Wtf.getCmp("combofield"+(i+1)).getValue()+" "+uomName
                                    
                            }
                        }
                        e.record.set("quantity",val);
                        e.record.set("showquantity",valStr);
                        this.fireEvent('datachanged',this);
                        this.addIntervalWindow.close();
                    }
                  
                }
            }, {
                text: 'Cancel',
                scope: this,
                handler: function() {
                    this.addIntervalWindow.close();
                }
            }]
        });
        this.addIntervalWindow.show();
        var qtyArray=value.split(","); 
        var qtyStr=e.record.data.showquantity;
        var qtyStrArray=qtyStr.toString().split("X");
        for(var i=0;i<qtyArray.length;i++){
            var uomName="";
            var idx = Wtf.uomStore.find("uomid", qtyArray[i]);
            if(idx != -1){               
                var rec =  Wtf.uomStore.getAt(idx);
                uomName=rec.get("uomname");
            }
            var unitVal=1;
            if(qtyStrArray.length>0 && qtyStrArray[i]){
                var unitValArray=qtyStrArray[i].split(" ");
                unitVal=unitValArray[0];
            }
            this.extraQuantity.add({
                id : "filefield"+i,
                layout:'form',
                labelWidth:130,
                //            bodyStyle:"padding:10px,0px,10px,0px",
                border :false,
                items:[{
                    layout : 'column',
                    border : false,
                    items: [{
                        columnWidth: .80,
                        layout : 'form',
                        border : false,
                        items:[new Wtf.form.NumberField({
                            fieldLabel:"Enter Quantity "+(i+1),
                            hiddenName:'qtyuom'+(i+1),
                            id : "combofield"+(i+1),
                            allowBlank:true,
                            anchor:'20%',
                            value:unitVal,
                            allowBlank:false
                        })]
                    },{
                        bodyStyle:"font-size: 12px !important;",    
                        html:uomName,
                        style:" padding: 7px 0;",
                        border : false,
                        layout:'fit',
                        columnWidth: .20
                    }]
                }]

            });
            this.extraQuantity.doLayout();
        }
    },
    /**********/
    /*For SATS*/
    createSATSIntervalWindow:function(e){
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:90,
            border:false,
            bodyStyle:"background:white;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml("Manage Interval","Manage Interval time for '"+e.record.data.productname+"'<br> Please enter time in 24 hours format." ,'../../images/createuser.png',false,'0px 0px 0px 0px')
        });
        
        
        this.hourStore = new Wtf.data.SimpleStore({
            fields: [{
                name:'id',
                type:'string'
            }, 'name'],
            data :[[0,"00"],[1,"01"],[2,"02"],[3,"03"],[4,"04"],[5,"05"],[6,"06"],[7,"07"],[8,"08"],[9,"09"],[10,"10"],[11,"11"],
            [12,"12"],[13,"13"],[14,"14"],[15,"15"],[16,"16"],[17,"17"],[18,"18"],[19,"19"],[20,"20"],[21,"21"],[22,"22"],[23,"23"],
            ]
        });
        this.minuteStore = new Wtf.data.SimpleStore({
            fields: [{
                name:'id',
                type:'string'
            }, 'name'],
            data :[[0,"00"],[1,"01"],[2,"02"],[3,"03"],[4,"04"],[5,"05"],[6,"06"],[7,"07"],[8,"08"],[9,"09"],[10,"10"],
            [11,"11"],[12,"12"],[13,"13"],[14,"14"],[15,"15"],[16,"16"],[17,"17"],[18,"18"],[19,"19"],[20,"20"],
            [21,"21"],[22,"22"],[23,"23"],[24,"24"],[25,"25"],[26,"26"],[27,"27"],[28,"28"],[29,"29"],[30,"30"],
            [31,"31"],[32,"32"],[33,"33"],[34,"34"],[35,"35"],[36,"36"],[37,"37"],[38,"38"],[39,"39"],[40,"40"],
            [41,"41"],[42,"42"],[43,"43"],[44,"44"],[45,"45"],[46,"46"],[47,"47"],[48,"48"],[49,"49"],[50,"50"],
            [51,"51"],[52,"52"],[53,"53"],[54,"54"],[55,"55"],[56,"56"],[57,"57"],[58,"58"],[59,"59"]
                       
            ]
        });
            
        var date = new Date();
        var inoutTime= e.record.data.inouttime;
        var inoutTimeArray=inoutTime.split(","); 
        var inHour=""
        var inDate=""
        var outDate=""
        var inTime=""
        var outTime=""
        var inMinutes=""
        var outHour=""
        var outMinutes=""
        if(inoutTimeArray.length>1){
            inDate=inoutTimeArray[0].split(" ")[0]
            inTime=inoutTimeArray[0].split(" ")[1]
            inHour=inTime.split(":")[0]
            inMinutes=inTime.split(":")[1]
            
            outDate=inoutTimeArray[1].split(" ")[0]
            outTime=inoutTimeArray[1].split(" ")[1]
            outHour=outTime.split(":")[0]
            outMinutes=outTime.split(":")[1]
            
            
        }
        
        this.startDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
            name:'stdate',
            width:180,
            format:WtfGlobal.getOnlyDateFormat(),
            readOnly:true,
            value:(inDate=="")?new Date():inDate
        });
        this.endDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
            format:WtfGlobal.getOnlyDateFormat(),
            readOnly:true,
            width:180,
            name:'enddate',
            value:(outDate=="")?new Date():outDate
        });    
        var minutes = date.getMinutes();
        var hour = date.getHours();
        this.inhourCombo = new Wtf.form.ComboBox({
            triggerAction: 'all',
            hidden: false,
            mode: 'local',
            valueField: 'id',
            width:80,
            value:(inHour=="")?hour:inHour,
            hideLabel:true,
            displayField: 'name',
            store: this.hourStore,
            fieldLabel: '',
            typeAhead: true,
            forceSelection: true,
            hiddenName: 'inhour'
        });    
        this.outhourCombo = new Wtf.form.ComboBox({
            triggerAction: 'all',
            hidden: false,
            mode: 'local',
            width:80,
            value:(outHour=="")?hour+1:outHour,
            valueField: 'id',
            hideLabel:true,
            displayField: 'name',
            store: this.hourStore,
            fieldLabel: '',
            typeAhead: true,
            forceSelection: true,
            hiddenName: 'outhour'
        });   
            
        this.inminuteCombo = new Wtf.form.ComboBox({
            triggerAction: 'all',
            hidden: false,
            mode: 'local',
            width:85,
            valueField: 'id',
            value:(inMinutes=="")?minutes:inMinutes,
            hideLabel:true,
            displayField: 'name',
            store: this.minuteStore,
            fieldLabel: '',
            typeAhead: true,
            forceSelection: true,
            hiddenName: 'inminute'
        });    
        this.outminuteCombo = new Wtf.form.ComboBox({
            triggerAction: 'all',
            hidden: false,
            mode: 'local',
            valueField: 'id',
            width:85,
            value:(outMinutes=="")?minutes:outMinutes,
            hideLabel:true,
            displayField: 'name',
            store: this.minuteStore,
            fieldLabel: '',
            typeAhead: true,
            forceSelection: true,
            hiddenName: 'outminute'
        });    
        
        this.isAddDesc= new Wtf.form.Checkbox({
            name:'addProdDesc',
            labelSeparator:'',
            boxLabel:WtfGlobal.getLocaleText("acc.product.addToProductDesc"),//'Make available in CRM',
            checked:false,         
            style:'margin-left: -40px;',
            itemCls:"chkboxalign"
        })
        this.isAddInterval= new Wtf.form.Checkbox({
            name:'addProdInterval',
            labelSeparator:'',
            boxLabel:WtfGlobal.getLocaleText("acc.product.addToProductInterval"),//'Add Time Interval',
            checked:(e.record.data.hourtimeinterval>1)?true:false,                     
            style:'margin-left: -40px;',
            itemCls:"chkboxalign"
        })
       
        this.titlePanel = new Wtf.Panel({
            border:false,
            bodyStyle:"padding-bottom: 5px;text-align: center;margin-left: 20px;font-size: 12px !important;",
            html:"<div style='font-size: 12px !important;'><b>Hours&nbsp; &nbsp; &nbsp;&nbsp; &nbsp; &nbsp;&nbsp; &nbsp; &nbsp;&nbsp; &nbsp; &nbsp;&nbsp; &nbsp; &nbsp; Minutes</b></div>"
        });
        this.AddEditForm = new Wtf.form.FormPanel({
            region:"south",
            border:false,
            //            height:100,
            bodyStyle:"background-color:#f1f1f1;padding: 20px",
            items:[this.startDate,{
                layout : 'column',
                border : false,
                items: [{
                    bodyStyle:"font-size: 12px !important;",    
                    html:'In Time*:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;',//WtfGlobal.getLocaleText("crm.spredsheet.customcolumn.existingrecs")+':&nbsp&nbsp',//'Existing Records:&nbsp&nbsp',
                    //                    width: 235,
                    style:" padding: 7px 0;",
                    border : false,
                    layout:'fit',
                    columnWidth: .29
                },{
                    columnWidth: .27,
                    border : false,
                    items:[this.inhourCombo]
                },{
                    columnWidth: .30,
                    border : false,
                    items:[this.inminuteCombo]
                }]
            },this.endDate,{
                layout : 'column',
                //                style:"padding-top: 10px;",
                border : false,
                items: [{
                    bodyStyle:"font-size: 12px !important;",        
                    html:'Out Time*:&nbsp',//WtfGlobal.getLocaleText("crm.spredsheet.customcolumn.existingrecs")+':&nbsp&nbsp',//'Existing Records:&nbsp&nbsp',
                    width: 235,
                    style:" padding: 7px 0;",
                    border : false, 
                    layout:'fit',
                    columnWidth: .29
                },{
                    columnWidth: .27,
                    border : false, 
                    items:[this.outhourCombo]
                },{
                    columnWidth: .30,
                    border : false, 
                    items:[this.outminuteCombo]
                }]
            },this.isAddDesc,this.isAddInterval]
        });
        this.addIntervalWindow = new Wtf.Window({
            modal: true,
            title: "Interval Time Settings",
            bodyStyle: 'padding:5px;',
            buttonAlign: 'right',
            width: 425,
            scope: this,
            items: [{
                region: 'center',
                border: false,
                bodyStyle: 'background:#f1f1f1;font-size:10px;',
                autoScroll: true,
                items: [this.northPanel,this.AddEditForm]
            }],
            buttons: [{
                text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                scope: this,
                handler: function(button) {
                    var inHour=  this.inhourCombo.getValue();
                    var inMinutes=  this.inminuteCombo.getValue();
                    var outHour=  this.outhourCombo.getValue();
                    var outMinutes=  this.outminuteCombo.getValue();
                    
                    var inDate = this.startDate.getValue();
                    var outDate=this.endDate.getValue();
                    inDate.setHours(inHour); 
                    inDate.setMinutes(inMinutes); 
                    inDate.setSeconds(00); 
                    
                    outDate.setHours(outHour); 
                    outDate.setMinutes(outMinutes); 
                    outDate.setSeconds(00); 
                    if(inDate.getTime()<outDate.getTime()){
                        e.record.set("quantity",(outDate-inDate)/3600000 +" Hrs");
                        var interValtime=(outDate-inDate)/3600000;
                        if(this.isAddDesc.getValue()){ 
                             var desc="";
//                            desc=e.record.data.desc;
//                            desc =desc.substring(desc.indexOf("Arrival time") ,desc.length);
                            var startDateOnly=WtfGlobal.onlyDateRendererForGridHeader(this.startDate.getValue());
                            var endDateOnly=WtfGlobal.onlyDateRendererForGridHeader(this.endDate.getValue());
                            desc=desc+"\nArrival Date: "+startDateOnly+" Arrival time: "+inHour+":"+inMinutes+"\nDeparture Date: "+endDateOnly+" Departure time: "+outHour+":"+outMinutes+"\nTotal duration: "+interValtime.toFixed(2)+" Hrs"
                            e.record.set("desc",desc);
                             desc="";
                        } 
                        var inDateFormat=inDate.format('Y-m-d');
                        var outDateFormat=outDate.format('Y-m-d');
                        var interValtime=(outDate-inDate)/3600000;
                        if(this.isAddInterval.getValue()){ 
                            e.record.set("istimeinterval", this.isAddInterval.getValue());
                            interValtime=interValtime*e.record.data.hourtimeinterval;
                            var interValtimeround= Math.round(interValtime); 
                            if(interValtime>interValtimeround){
                                interValtime=interValtimeround+1;
                            }else{
                                interValtime=interValtimeround;
                            }
                        }else{
                            e.record.set("istimeinterval", false);
                        }
                        var timeDiff = Math.abs(outDate-inDate);
                        var hh = Math.floor(timeDiff / 1000 / 60 / 60);
                        if(hh < 10) {
                            hh = '0' + hh;
                        }
                        timeDiff -= hh * 1000 * 60 * 60;
                        var mm = Math.floor(timeDiff / 1000 / 60);
                        if(mm < 10) {
                            mm = '0' + mm;
                        }
                         
                        if(e.record.data.addshiplentheithqty){
                            var shipLength=Wtf.getCmp(this.parentCmpID).shipLength.getValue();
                            e.record.set("quantity",interValtime*shipLength);
                            e.record.set("showquantity",hh+"."+mm);
                        }else{
                            e.record.set("quantity",interValtime);
                            e.record.set("showquantity",hh+"."+mm);
                        }
                        e.record.set("inouttime",inDateFormat+" "+inHour+":"+inMinutes+","+outDateFormat+" "+outHour+":"+outMinutes);
                        this.fireEvent('datachanged',this);
                        this.addIntervalWindow.close();
                    }else{
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Out Date Should not less than or equal to In date"], 2);
                    }
                }
            }, {
                text: 'Cancel',
                scope: this,
                handler: function() {
                    this.addIntervalWindow.close();
                }
            }]
        });

        this.addIntervalWindow.show();
        if(e.record.data.hourtimeinterval==1 || e.record.data.hourtimeinterval=="" || e.record.data.hourtimeinterval==0)
            WtfGlobal.hideFormElement(this.isAddInterval);
          
        if (e.record.data.isopenedfirsttime && e.record.data.addshiplentheithqty) {
            e.record.set("isopenedfirsttime", false);
            this.isAddInterval.setValue(true);
        } else {
            if (e.record.data.istimeinterval) {
                if (e.record.data.addshiplentheithqty) {
                    this.isAddInterval.setValue(e.record.data.istimeinterval);
                } else {
                    this.isAddInterval.setValue(false);
                }
            } else {
                this.isAddInterval.setValue(false);
            }
        }
    },
    /**********/
    createComboEditor:function(){
        this.pricingBandMasterRec = Wtf.data.Record.create([
            {name: 'pricingbandmasterid', mapping: 'id'},
            {name: 'pricingbandmastername', mapping: 'name'},
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

        this.pricingBandMasterEditor = new Wtf.form.FnComboBox({
            hiddenName: 'pricingbandmaster',
            triggerAction: 'all',
            mode: 'local',
            lastQuery: '',
            name: 'pricingbandmaster',
            store: this.pricingBandMasterStore, //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
            typeAhead: true,
            selectOnFocus: true,
            valueField: 'pricingbandmasterid',
            displayField: 'pricingbandmastername',
            scope: this,
            forceSelection: true
        });
        this.poProductRec = Wtf.data.Record.create ([
            {name:'productid'},
            {name:'quantity'},
            {name:'prtaxid'}
        ]);
        this.poProductStore = new Wtf.data.Store({
            //url:Wtf.req.account+(this.isCustomer?'CustomerManager.jsp':'VendorManager.jsp'),
            url:this.isCustomer?'ACCSalesOrderCMN/getSalesOrderRows.do':'ACCPurchaseOrderCMN/getPurchaseOrderRows.do',
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.poProductRec)
        });
          this.productId= new Wtf.form.TextField({
            name:'pid'
//            readOnly:true
        });
//        chkproductload();
            this.productComboStore.on('beforeload',function(s,o){
                if(!o.params)o.params={};
                var currentBaseParams = this.productComboStore.baseParams;
            if(Wtf.account.companyAccountPref.negativeStockFormulaSO==1){
                currentBaseParams.getSOPOflag=true;
            }else{
                currentBaseParams.getSOPOflag=false;
            }
                
                currentBaseParams.isForBarcode=(this.obj != undefined && this.obj.isForBarcode != undefined && this.obj.isForBarcode ) ? true : false;
                currentBaseParams.module_name="INVMODULE";
                currentBaseParams.termSalesOrPurchaseCheck = this.isCustomer;
                /**
                 * this isDefault flag was sent to fetch only default set term
                 */
                currentBaseParams.isDefault = true;
                currentBaseParams.isGST = this.isGST;
                currentBaseParams.affecteduser=this.affecteduser,
                currentBaseParams.onlyProduct = (this.isOrder || this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId)? false : true
                currentBaseParams.startdate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true));
                currentBaseParams.enddate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false));  
                currentBaseParams.moduleid = this.moduleid;         // Passing Moduleid
                //ERP-8199 : this.isProductLoad flag required for the case of Product selection window functionality to load multiple product at a time
                currentBaseParams.searchProductString = (this.productOptimizedFlag==Wtf.Products_on_Submit && !this.isProductLoad)? this.productId.getValue():""; 
                this.productComboStore.baseParams=currentBaseParams;        
                
                if(this.ProductloadingMask==undefined){
                this.ProductloadingMask = new Wtf.LoadMask(document.body,{
                    msg : WtfGlobal.getLocaleText("acc.msgbox.50")
                    });
                    this.ProductloadingMask.show();
                }
                currentBaseParams.isFreeTextSearching = (this.productOptimizedFlag==Wtf.Products_on_Submit);
            },this); 
            
             this.productComboStore.on("load",function(){
            if(this.ProductloadingMask)
                this.ProductloadingMask.hide();
//            if (this.isJobWorkOrderReciever) {
//                this.bomStore.load({
//                    params: {
//                        productid:this.productEditor.getValue()
//                    }
//                })
//            }
            
             },this);
            this.productComboStore.on("loadexception",function(){
                if(this.ProductloadingMask)
                    this.ProductloadingMask.hide();
            },this);
            
        var baseParams = {};
        if (this.isCustomer) {
            baseParams = {
                loadInventory: true,
                onlyProduct: true,
                excludeParent: true
            };
        } else {
            baseParams = {
                loadPrice: true,
                onlyProduct: true,
                excludeParent: true
            };
        }
        if (this.productOptimizedFlag == Wtf.Show_all_Products) {
            var configforcombo = {displayField: 'pid', extraFields: ['productname', 'type']}; //Passing extra config for combo
            this.productEditor = CommonERPComponent.createProductPagingComboBox(100, 450, 30, this, baseParams, false, false, configforcombo);// For All Product with Paging
        } else {
            this.productEditor = CommonERPComponent.createProductPagingComboBox(100, 450, 30, this, baseParams, true);// Type Ahead With and without paging
        }
        this.commonproductStore = this.productEditor.store;
        this.commonproductStore.on('beforeload', function (s, o) {
            if (!o.params)
                o.params = {};
            var currentBaseParams = this.commonproductStore.baseParams;
            currentBaseParams.isForBarcode = false;
            currentBaseParams.module_name = "INVMODULE";
            currentBaseParams.termSalesOrPurchaseCheck = this.isCustomer;
            /**
             * this isDefault flag was sent to fetch only default set term
             */
            currentBaseParams.isDefault = true;
            currentBaseParams.isGST = this.isGST;
            currentBaseParams.affecteduser = this.affecteduser,
                    currentBaseParams.onlyProduct = (this.isOrder || this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId) ? false : true
            currentBaseParams.startdate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true));
            currentBaseParams.enddate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false));
            currentBaseParams.moduleid = this.moduleid;         // Passing Moduleid
            //ERP-8199 : this.isProductLoad flag required for the case of Product selection window functionality to load multiple product at a time
            currentBaseParams.searchProductString = (this.productOptimizedFlag == Wtf.Products_on_Submit && !this.isProductLoad) ? this.productId.getValue() : "";
            this.commonproductStore.baseParams = currentBaseParams;
            currentBaseParams.isFreeTextSearching = (this.productOptimizedFlag == Wtf.Products_on_Submit);
        }, this);
            
        this.productEditor.on('beforeselect', function (combo, record, index) {
            if (this.productOptimizedFlag == Wtf.Products_on_type_ahead || this.productOptimizedFlag == Wtf.Show_all_Products) {// Load Selected Product
                if (record.data != undefined && record.data != null) {
                    var rec = record.data;
                    if (rec.productid != undefined && rec.productid != null && rec.productid != "") {
                        var productidarray = [];
                        productidarray.push(rec.productid);
                        this.productComboStore.load({
                            params: {
                                ids: productidarray
                            }
                        });
                    }
                }
            }
            return validateSelection(combo, record, index);
        }, this);
        /*
         *SDP-4553
         *Set Product Id When user scans barcode for product id field.
         *After scanning barcode by barcode reader press Enter or Tab button.
         *So we are handling specialkey event to set product id to product id combo.
         **/
        this.productEditor.on("specialkey", this.onSpecialKey, this);
        this.productId.on("specialkey", this.onSpecialKey, this);
        
        /*For SATS*/
        this.dependentTypeRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'dependentType'},
            {name: 'value'},
            {name: 'price'},
            {name: 'type'}
        ]);
        this.dependentTypeStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"

            },this.dependentTypeRec),
            url : "ACCMaster/getMasterItemPrice.do"
        });       

        this.dependentType= new Wtf.form.ComboBox({
            hiddenName:'dependentType',
            store:this.dependentTypeStore,
            valueField:'id',
            hidden:SATSCOMPANY_ID==companyid?true:false,
            hideLabel:SATSCOMPANY_ID==companyid?true:false,
            displayField:'value',
            triggerAction:'all',
            mode: 'local',
            lastQuery: '',
            allowBlank: false,
            anchor:'85%',
            typeAhead: true,
            forceSelection: true,
            name:'dependentType'
        });   
        this.dependentTypeStore.load();  
        /*For SATS*/

        this.vendorEditor=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.invoiceList.ven"),  //'Vendor',
            id:"selectVendor"+this.helpmodeid,
            hiddenName:'accid',
            store:Wtf.vendorAccStore,
            valueField:'accid',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
            mode: 'local',
            typeAheadDelay:30000,
            extraComparisionField:'acccode',
            minChars:1,
            scope:this,
            anchor:'90%',
            displayField:'accname',
            forceSelection: true,
            hirarchical:true
        });
        /* Loaded Vendor Store only when "Activate Profit Margin" check from Master Configuratio is true */
        if((Wtf.account.companyAccountPref.activateProfitMargin  && (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId))){
            Wtf.vendorAccStore.load();
        }
        if(this.productOptimizedFlag!=Wtf.Products_on_Submit){
                    
            this.productEditor.on("blur",function(e,a,b){
                if(Wtf.account.companyAccountPref.invAccIntegration && !this.isCustomer && !this.isQuotation){                
                    e.store=this.productComboStore;
                }    
            },this);
                this.productEditor.addNewFn=this.openProductWindow.createDelegate(this);
        }
        chkUomload();
        this.uomStoreBasedOnUOMSchema = new Wtf.data.Store({
            url: "ACCUoM/getUnitOfMeasureOfProductUOMSchema.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, Wtf.uomRec)
        });
        
        this.uomStoreBasedOnUOMSchema.on('load',function(store, rec){
            this.uomdisplay();
        },this);
        
        this.uomEditor=new Wtf.form.FnComboBox({
            hiddenName:'uomname',
            triggerAction:'all',
            mode: 'local',
            lastQuery:'',
            name:'uomname',
            store:Wtf.uomStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
            typeAhead: true,
            selectOnFocus:true,
            valueField:'uomid',
            displayField:'uomname',
            scope:this,
            forceSelection:true
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.uom, Wtf.Perm.uom.edit))
            this.uomEditor.addNewFn=this.showUom.createDelegate(this);

        this.noteTypeRec = new Wtf.data.Record.create([
           {name: 'typeid'},
           {name: 'name'},
        ]);
        this.typeStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.noteTypeRec),
            url: "ACCCreditNote/getNoteType.do",
            baseParams:{
                mode:31,
                combineData:-1  //Send For Seprate Request
            }
        });
        /* Loaded Type Store only when required */
        if(!(!this.isNote ||this.noteTemp)){
            this.typeStore.load();
        }

        this.typeEditor = new Wtf.form.ComboBox({
            store: this.typeStore,
            name:'typeid',
            displayField:'name',
            valueField:'typeid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true,
            
            listeners: {
                afterrender: function(combo) {
                    var recordSelected = combo.getStore().getAt(0);                     
                    combo.setValue(recordSelected.get("typeid"));
                }
            }
        });
        
        this.accRec = Wtf.data.Record.create([
            {name:'accountname',mapping:'accname'},
            {name:'accountid',mapping:'accid'},
            {name:'productaccountid', mapping:'accid'},
            {name:'acccode'},
            {name:'groupname'}
        ]);
        this.accountStore = new Wtf.data.Store({
            url:"ACCAccountCMN/getAccountsForCombo.do",
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
        this.materialRec = Wtf.data.Record.create ([
            {name:'id'},
            {name:'bomid'},
            {name:'type'},
            {name:'name'},
            {name:'bomcode'},
        ]);
        this.bomStore = new Wtf.data.Store({
            url: "ACCProductCMN/getBOMforCombo.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.materialRec)
        });
        WtfGlobal.setAjaxTimeOut();
        /* Loaded account Store only when required */
        if(!(!this.isNote ||this.noteTemp) || Wtf.account.companyAccountPref.AllowToMapAccounts){
            this.accountStore.load();
        }
        this.accountStore.on('load',function(store, rec){
             WtfGlobal.resetAjaxTimeOut();
        },this);
    this.cmbAccount=new Wtf.form.ExtFnComboBox({
                    hiddenName:'accountid',
                    store:this.accountStore,
                    minChars:1,
                    valueField:'accountid',
                    displayField:'accountname',
                    forceSelection:true,
                    hirarchical:true,
//                    addNewFn:this.openCOAWindow.createDelegate(this),
                    extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
                    mode: 'local',
                    typeAheadDelay:30000,
                    extraComparisionField:'acccode',// type ahead search on acccode as well.
                    listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400
        });
        this.bomcombo = new Wtf.form.ExtFnComboBox({
            store: this.bomStore,
            valueField: 'bomid',
            displayField: 'bomcode',
            forceSelection: true,
            hirarchical: true,
            extraFields: [],
            mode: 'remote'
        });

        this.rowDiscountTypeStore = new Wtf.data.SimpleStore({
            fields: [{name:'typeid',type:'int'}, 'name'],
            data :[[1,'Percentage'],[0,'Flat']]
        });
        this.rowDiscountTypeCmb = new Wtf.form.ComboBox({
            store: this.rowDiscountTypeStore,
            name:'typeid',
            displayField:'name',
            valueField:'typeid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
        });
        this.remark= new Wtf.form.TextArea({
            name:'remark'
//            readOnly:true
        });
        this.cndnRemark= new Wtf.form.TextArea({
            name:'remark'
        });
        this.approverremark= new Wtf.form.TextField({
            name:'approverremark'
        });
        this.permiteditor= new Wtf.form.TextField({
            name:'permit',
            maxLength:50
        });
        this.transDiscount=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            defaultValue:0,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
        });
        this.transTaxAmount=new Wtf.form.NumberField({
            allowBlank: true,
            allowNegative: false,
            defaultValue:0,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
        });
        
        this.partAmount=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            defaultValue:0,
            decimalPrecision:2,
            maxValue:100
        });
        this.taxRec = new Wtf.data.Record.create([
           {name: 'prtaxid',mapping:'taxid'},
           {name: 'prtaxname',mapping:'taxname'},
           {name: 'taxdescription'},
           {name: 'percent',type:'float'},
           {name: 'taxcode'},
           {name: 'accountid'},
           {name: 'accountname'},
           {name: 'applydate', type:'date'},
           {name: 'hasAccess'},
           {name: 'termid'}

        ]);
        this.taxStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.taxRec),
    //        url: Wtf.req.account + 'CompanyManager.jsp',
            url : "ACCAccountCMN/getTax.do",
            baseParams:{
                mode:33,
                moduleid :this.moduleid,
                includeDeactivatedTax: this.isEdit != undefined ? (this.copyInv ? false : this.isEdit ) : false
            }
        });
        this.taxStore.on("load", function() {
            var record = new Wtf.data.Record({
                prtaxid: 'None',
                prtaxname: 'None'
            });
            this.taxStore.insert(this.taxStore.getCount()+1, record);
        }, this);
//        if(this.readOnly)
        if(!(!(this.editTransaction||this.readOnly) || this.noteTemp)){
            this.taxStore.load();
        }

        this.transTax= new Wtf.form.ExtFnComboBox({
            hiddenName:'prtaxid',
            anchor: '100%',
            store:this.taxStore,
            valueField:'prtaxid',
            forceSelection: true,
            displayField:'prtaxname',
//            addNewFn:this.addTax.createDelegate(this),
            scope:this,
            displayDescrption: 'taxdescription',
            extraFields:[],
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
        /**
         * Create store for inspection template
         */
        this.inspectionTemplateRec = new Wtf.data.Record.create([
           {name: 'templateId'},
           {name: 'templateName'},
           {name: 'templateDescription'}

        ]);
        this.inspectionTemplateStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.inspectionTemplateRec),
            url : 'INVTemplate/getInspectionTemplateList.do',
            baseParams:{}
        });
        this.inspectionTemplateStore.on("load", function() {
            var record = new Wtf.data.Record({
                templateId: 'None',
                templateName: 'None'
            });
            this.inspectionTemplateStore.insert(this.inspectionTemplateStore.getCount()+1, record);
        }, this);
        /**
         * load inspection template store in Create,Edit,View case
         */
        
            this.inspectionTemplateStore.load();
        
        /**
         * Combo for selection of inspection template
         */
        this.inspectionTemplateCombo= new Wtf.form.ExtFnComboBox({
            hiddenName:'inspectiontemplate',
            anchor: '100%',
            store:this.inspectionTemplateStore,
            valueField:'templateId',
            forceSelection: true,
            displayField:'templateName',
            scope:this,
            extraFields:[],
            listeners: {
                'beforeselect': {
                    fn: function (combo, record, index) {
                        return validateSelection(combo, record, index);
                    },
                    scope: this
                }
            }
        });
        this.productAccount= new Wtf.form.FnComboBox({
            hiddenName:'productaccountid',
            anchor: '100%',
            store:this.accountStore,
            valueField:'productaccountid',
            forceSelection: true,
            displayField:'accountname',
            scope:this,
            selectOnFocus:true
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.tax, Wtf.Perm.tax.edit))
            this.transTax.addNewFn=this.addTax.createDelegate(this);
        this.transQuantity=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength:15,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
        });
        
        var firstTimeChange = true; //In edit case, Hold the original value to check SO Outstanding. ERP-37525
        this.transQuantity.on('change', function (field, newval, oldval) {
            if (this.isEdit && field.getValue() != '' && this.transQuantity.getValue()!= '' && this.transQuantity.getValue()>0 && firstTimeChange && this.moduleid == Wtf.Acc_Sales_Order_ModuleId) {
                this.originalQty = oldval;
                firstTimeChange = false;
            }
        }, this);
        
        /*For SATS*/
        this.dependentTypeNo=new Wtf.form.NumberField({
            allowBlank: true,
            allowNegative: false,
            maxLength:10
        });
        this.timeIntervalTxt= new Wtf.form.TextField({
            name:'timeinterval',
            width:150
        });
        this.timeIntervalTxt.addNewFn=this.openProductWindow.createDelegate(this);
        /**********/

        this.transBaseuomrate=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength:10,
            decimalPrecision: Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT
        });
        this.editprice=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false, 
           decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,    
            maxLength:14
        });  
        this.editPriceIncludingGST=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,    
            maxLength:14
        }); 
        this.editCost=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,    
            maxLength:14
        });  
        this.editExchangeRate=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            decimalPrecision:10,
            validator: function(val) {
                if (val!=0) {
                    return true;
                } else {
                    return false;
                }
            }
        }); 
        this.productWeightEditor=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            decimalPrecision:3,
            maxLength:14
        }); 
        this.productWeightPackagingEditor=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            decimalPrecision:3,
            maxLength:14
        }); 
        
        this.productVolumeEditor=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            decimalPrecision:3,
            maxLength:14
        }); 
        this.productVolumePackagingEditor=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            decimalPrecision:3,
            maxLength:14
        }); 
    },
    showUom:function(){
       callUOM('uomReportWin');
       Wtf.getCmp('uomReportWin').on('update', function(){
           Wtf.uomStore.reload();
       }, this);
    },
    addTax:function(){
         this.stopEditing();
         var p= callTax("taxwin");
         Wtf.getCmp("taxwin").on('update', function(){this.taxStore.reload();}, this);
    },
    openProductWindow:function(){
        this.stopEditing();
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.create)){
            callProductWindow(false, null, "productWin");
       // this.productStore.on('load',function(){this.productStore.})
            Wtf.getCmp("productWin").on("update",function(obj,productid){this.productID=productid;},this);
        }
        else{
              WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.creating")+" "+WtfGlobal.getLocaleText("acc.create.products")); 
        }
    },
    /**
     * Function is written to catch 'Enter', 'TAB', and Barcode scanner.
     */
    onSpecialKey : function(field, e){
        if(e.keyCode == e.ENTER|| e.keyCode == e.TAB){
            if(field.getRawValue() !="" ){
                var value = field.getRawValue();
                /*
                 *This block will execute when Show product on type ahead is selected.
                 *In this case we will fetch data from backend.
                 **/
                    var params = JSON.clone(this.productComboStore.baseParams);
                    if (this.productOptimizedFlag != Wtf.Products_on_Submit) {
                        params.query = field.getRawValue();
                    } else {
                        params.searchProductString = field.getRawValue();
                    }
                    params.isForBarcode = true;

                    Wtf.Ajax.requestEx({
                        url: this.productComboStore.url,
                        params:params
                    }, this, function(response) {
                        var prorec = response.data[0];
                        if(prorec){
                            this.setPIDForBarcode(prorec,field,true);
                        }
                    }, function() {});
            }
        }
    },

    createColumnModel:function(){
        this.summary = new Wtf.ux.grid.GridSummary();
        this.rowno=(this.isNote)?new Wtf.grid.CheckboxSelectionModel():new Wtf.grid.RowNumberer();
        var columnArr =[];
        if(!this.readOnly){
            columnArr.push(this.sModel);
            columnArr.push(this.rowno);
        }     
        columnArr.push({
            dataIndex:'rowid',
            hidelabel:true,
            hidden:true
        },{
            dataIndex:'billid',
            hidelabel:true,
            hidden:true
        },{//ERP-8199 :
                header:WtfGlobal.getLocaleText("acc.common.add"),
                align:'center',
                width:40,
                renderer: this.addProductList.createDelegate(this)
            });
            
        //added sequence arrows - refer ticket ERP-13781
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.invoice.lineItemSequence"),//"Sequence",
            width:65,
            align:'center',
//            dataIndex:'srno',
            name:'srno',
            renderer: Wtf.applySequenceRenderer
            });
            /**
             * Job Order Item column should be shown in SO, Invoice and Cash Sales if Job Order Flow Enable
             */
        if ((this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId) && Wtf.account.companyAccountPref.jobOrderItemFlow) {
            var jobitemheader = WtfGlobal.getLocaleText("acc.field.joborderitem");
            var jobOrderItem = new Wtf.CheckColumnComponent({
                dataIndex: 'joborderitem',
                header: "<div  wtf:qtip=\"" + jobitemheader + "\">" + jobitemheader + "<div>",
                width: 200,
                align: 'center',
                scope: this,
                id:'joborderitem'
            });
            columnArr.push(jobOrderItem);
        }
        if(this.productOptimizedFlag!=Wtf.Products_on_Submit){
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.product.gridProductID"),//"Product ID",
                width:200,
                dataIndex: 'pid',
                id:"productid"+this.heplmodeid+this.id,
                renderer:this.readOnly?"":this.getComboNameRenderer(this.productEditor),
                editor:(this.isNote||this.readOnly||this.isViewTemplate)?"":this.productEditor
                
            });
        }else{
             columnArr.push({
                header: WtfGlobal.getLocaleText("acc.product.gridProductID"),//"Product ID",
                dataIndex: 'pid',
                id:"pid"+this.heplmodeid+this.id,
                editor:(this.isViewTemplate||this.isNote||this.readOnly)?"":this.productId,
                width:200
               
            });
        }
        columnArr.push({
             header: WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),//"Product Name",
             dataIndex: 'productname',
             id:"productname"+this.heplmodeid+this.id
             
            },
            {
            header:WtfGlobal.getLocaleText("acc.mrp.field.bomcode"),
            width:150,
            dataIndex:"bomcode",
            renderer:this.bomComboRenderer(this.bomcombo),
            hidden:!this.isJobWorkOrderReciever,
            editor:this.readOnly?"":this.bomcombo
        },{
            header:this.isCN?WtfGlobal.getLocaleText("acc.invoice.gridInvNo"):WtfGlobal.getLocaleText("acc.invoice.gridVenInvNo"),//"Invoice No.":"Vendor Invoice No.",
            width:150,
            dataIndex:this.noteTemp?'transectionno':'billno',
            hidden:!this.isNote
        },
        
        {
             header:WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"),//"Description",
             dataIndex:"desc",
             id:"desc"+this.heplmodeid+this.id,
             hidden:this.isNote,
             width:250,
             editor:(this.isNote||this.readOnly||this.isViewTemplate)?"":this.remark,
             renderer:this.descriptionRenderer
//                 function(val){
//                 var regex = /(<([^>]+)>)/ig;
//                val = val.replace(/(<([^>]+)>)/ig,"");
//                   return "<div   wtf:qtip=\"<div style='word-wrap: break-word;'>"+val+"</div>"+val+"</div>";      
////                if(val.length<50)
////                    return val;   
////                else
////                    return val.substring(0,50)+" ...";   
//            }
            },
        {
                header:WtfGlobal.getLocaleText("acc.productGrid.MultiCompany.shippingAddress"),//"Shipping Address",
                dataIndex:"originalSourceShippingAddress",
                //             disabled:true,
                hidden:!(Wtf.account.companyAccountPref.activateGroupCompaniesFlag && Wtf.account.companyAccountPref.isMultiGroupCompanyParentFlag && this.moduleid==Wtf.Acc_Purchase_Order_ModuleId) ,
                width:200,
                renderer: function(v,m,rec) {
                    return "<a class='tbar-link-text'>"+WtfGlobal.getLocaleText("acc.productGrid.MultiCompany.clickToViewShippingAddress")+"</a>";
                }
            });
         if(Wtf.account.companyAccountPref.AllowToMapAccounts && Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA){
         columnArr.push({
             //Product Sales Account / Product Purchase Account
             header:this.isCustomer ? WtfGlobal.getLocaleText("acc.saleByItem.gridProdSalesAccount"): WtfGlobal.getLocaleText("acc.saleByItem.gridProdPurchaseAccount"),
             dataIndex:"productaccountid",
//             hidden:!Wtf.account.companyAccountPref.AllowToMapAccounts || Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA,
             width:150,
             renderer:Wtf.comboBoxRenderer(this.productAccount),
             editor:this.readOnly?"":this.productAccount  //this.productAccount
         });  
         }
         columnArr.push({
             header:WtfGlobal.getLocaleText("acc.product.supplier"),//"Supplier Part Number",
             dataIndex:"supplierpartnumber",
             hidden:!(Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.partNumber && !this.isCustomer && !this.isQuotation && !this.isNote),
             width:150
         },{
             header:WtfGlobal.getLocaleText("acc.field.ShelfLocation"),
             dataIndex:"shelfLocation",
             hidden:!(Wtf.account.companyAccountPref.invAccIntegration &&  this.isOrder && !this.isCustomer && !this.isQuotation),
             width:250,
             editor:(this.readOnly||this.isViewTemplate)?"":new Wtf.form.TextField()       
         });
        if (GlobalProductmasterFieldsArr[this.moduleid] != undefined) {
            var productdefaultfieldsarr = GlobalProductmasterFieldsArr[this.moduleid];

            for (var i = 0; i < productdefaultfieldsarr.length; i++) {

                var column = productdefaultfieldsarr[i];
                columnArr.push({
                    header: column.header,
                    dataIndex: column.fieldname,
                    width: 100,
                    xtype: 1
                });
            }
        }
       if(this.moduleid ==Wtf.Acc_Cash_Sales_ModuleId){
            columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModelForProduct[Wtf.Acc_Invoice_ModuleId],undefined,undefined,this.readOnly,this.isViewTemplate);
            columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModel[Wtf.Acc_Invoice_ModuleId],undefined,undefined,this.readOnly,this.isViewTemplate);
        }else if( this.moduleid==Wtf.Acc_Cash_Purchase_ModuleId){
            columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModelForProduct[Wtf.Acc_Vendor_Invoice_ModuleId],undefined,undefined,this.readOnly,this.isViewTemplate);
            columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModel[Wtf.Acc_Vendor_Invoice_ModuleId],undefined,undefined,this.readOnly,this.isViewTemplate);
        }else{
            columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModelForProduct[this.moduleid],undefined,undefined,this.readOnly,this.isViewTemplate);
            columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModel[this.moduleid],undefined,undefined,this.readOnly,this.isViewTemplate);
        }
        
        /* Hide/Show flag for Product Weight/Volumetric measurement */
        this.hideShowFlag=true;
        if((this.moduleid== Wtf.Acc_Invoice_ModuleId || this.moduleid== Wtf.Acc_Cash_Sales_ModuleId) && Wtf.account.companyAccountPref.calculateproductweightmeasurment){
           this.hideShowFlag=false; 
        }
        if(this.moduleid!=Wtf.Acc_Security_Gate_Entry_ModuleId){
            columnArr.push({
                header: WtfGlobal.getLocaleText("acc.field.PermitNo."),
                dataIndex:"permit",
                hidden:(this.isCustomer || Wtf.account.companyAccountPref.countryid != '203' || this.moduleid==Wtf.Acc_Vendor_Quotation_ModuleId || this.moduleid==Wtf.Acc_RFQ_ModuleId || this.moduleid==Wtf.Acc_Purchase_Requisition_ModuleId),    //Permit No. column will be shown when company is singaporian in Purchase modules. i.e PI/PO/CP/VQ
                width:100,
                editor:(this.readOnly||this.isViewTemplate)?"":this.permiteditor
            })
        }
         columnArr.push({
             header:WtfGlobal.getLocaleText("acc.invoice.gridQty"),//"Quantity",
             dataIndex:"quantity",
             id:"quantity"+this.heplmodeid+this.id,
             align:'right',
             width:150,
             hidden:(SATSCOMPANY_ID==companyid)?true:false,
             renderer:this.quantityRenderer,
//             renderer:this.storeRenderer(this.productComboStore,"productid","uomname"),
             editor:(this.isNote||this.readOnly||this.isViewTemplate)?"":this.transQuantity
        },{
             /*For SATS*/
             header:WtfGlobal.getLocaleText("acc.invoice.gridQty"),//"Quantity",
             dataIndex:"showquantity",
             align:'right',
             width:100,
             hidden:(SATSCOMPANY_ID!=companyid)?true:false,
             renderer:this.quantityRenderer,
             editor:(this.isNote||this.readOnly)?"":this.transQuantity
         },{
             /*For SATS*/
             header:WtfGlobal.getLocaleText("acc.invoice.dependentTypeNo"),//"Product",
             width:200,
             hidden:!Wtf.account.companyAccountPref.dependentField && (this.moduleid!=Wtf.Acc_Invoice_ModuleId || this.moduleid!=Wtf.Acc_Vendor_Invoice_ModuleId),
             dataIndex:this.readOnly?'value':'dependentType',
             renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.dependentType),
             editor:(this.isNote||this.readOnly)?"":this.dependentType
         },{   //added the add serial icon at last of grid
             header: '',
             align:'center',
             renderer: this.serialRenderer.createDelegate(this),
             dataIndex:'serialwindow',
             id:this.id+'serialwindow',
             hidden:((SATSCOMPANY_ID==companyid?true:false) || !(this.moduleid==Wtf.Acc_Cash_Sales_ModuleId  || this.moduleid==Wtf.Acc_Sales_Order_ModuleId || this.moduleid==Wtf.Acc_Cash_Purchase_ModuleId || this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId)),
             width:40
          } ,{
            header:WtfGlobal.getLocaleText("acc.invoice.gridUOM"),//"UOM",
            width:150,
            dataIndex:this.readOnly?'uomname':'uomid',
            id:"uomid"+this.heplmodeid+this.id,
            hidden:(SATSCOMPANY_ID==companyid)?true:false,
            renderer:this.readOnly?"":Wtf.comboBoxRendererwithClearFilter(this.uomEditor),
            editor:(this.isNote||this.readOnly||this.isViewTemplate)?"":this.uomEditor //||this.UomSchemaType==Wtf.PackegingSchema
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridRateToBase"),//Base UOM Rate
             dataIndex:"baseuomrate",
             id:"baseuomrate"+this.heplmodeid+this.id,
             align:'left',
             hidden:(SATSCOMPANY_ID==companyid)?true:false,
             width:150,
//             hidden:this.isNote,
             renderer:this.conversionFactorRenderer(this.productComboStore,"productid","uomname",this.store),
             editor:(this.isViewTemplate||this.isNote||this.readOnly||this.UomSchemaType==Wtf.PackegingSchema)?"":(Wtf.account.companyAccountPref.UomSchemaType===0  && Wtf.account.companyAccountPref.isBaseUOMRateEdit) ?this.transBaseuomrate : ""     //Does allow to user to change conversion factor
         },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridQtyInBase"),//Base UOM Quantity
             dataIndex:"baseuomquantity",
             id:"baseuomquantity"+this.heplmodeid+this.id,
//             hidden:true,
             align:'right',
             hidden:(SATSCOMPANY_ID==companyid)?true:false,
             width:150,
             renderer:this.storeRenderer(this.productComboStore,"productid","uomname",this.store)
//             editor:(this.isNote||this.readOnly)?"":this.transQuantity
        },{
            header:WtfGlobal.getLocaleText("acc.productList.unitWeight"),//Unit Weight
            width:150,
            align:'right',
            dataIndex:'productweightperstockuom',
            renderer: WtfGlobal.weightRenderer,
            editor : this.productWeightEditor,
            hidden :this.hideShowFlag 
        },{
            header:WtfGlobal.getLocaleText("acc.productList.unitWeightWithPackaging"),//'Unit Weight with Packaging',
            width:150,
            align:'right',
            dataIndex:'productweightincludingpakagingperstockuom',
            renderer: WtfGlobal.weightRenderer,
            editor : this.productWeightPackagingEditor,
            hidden :this.hideShowFlag
        },{
            header:WtfGlobal.getLocaleText("acc.productList.unitVolume"),//Unit volumetric
            width:150,
            align:'right',
            dataIndex:'productvolumeperstockuom',
            renderer: WtfGlobal.volumeRenderer,
            editor : this.productVolumeEditor,
            hidden :this.hideShowFlag 
        },{
            header:WtfGlobal.getLocaleText("acc.productList.unitVolumeWithPackaging"),//'Unit volumetric with Packaging',
            width:150,
            align:'right',
            dataIndex:'productvolumeincludingpakagingperstockuom',
            renderer: WtfGlobal.volumeRenderer,
            editor : this.productVolumePackagingEditor,
            hidden :this.hideShowFlag
        },{
             header:(Wtf.account.companyAccountPref.countryid!= Wtf.Country.INDONESIA)?WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingGST"):WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingVAT"),// "Unit Price Including GST",
             dataIndex: "rateIncludingGst",
             align:'right',
//             id:this.id+"rateIncludingGst",
             fixed:true,
             width:150,
             renderer:this.unitPriceRendererWithPermissionCheck.createDelegate(this),
             hidden:true,
             editor:(this.isNote||this.readOnly||this.isViewTemplate || (this.isCustomer?!Wtf.dispalyUnitPriceAmountInSales:!Wtf.dispalyUnitPriceAmountInPurchase))?"":this.editPriceIncludingGST,
             editable:true
//             hidden: this.noteTemp || this.isRFQ
        },{
            header: WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice"), // "Unit Price",
            dataIndex: "rate",
//             id:this.id+"rate",
             id:"rate"+this.heplmodeid+this.id,
             align:'right',
             width:150,
             renderer:this.unitPriceRendererWithPermissionCheck.createDelegate(this),
             editor:(this.isNote||this.readOnly||this.isViewTemplate || (this.isCustomer?!Wtf.dispalyUnitPriceAmountInSales:!Wtf.dispalyUnitPriceAmountInPurchase))?"":this.editprice,
             editable:true,
            hidden: this.noteTemp || this.isRFQ
        });
        if ((Wtf.account.companyAccountPref.productPricingOnBands || Wtf.account.companyAccountPref.productPricingOnBandsForSales)) {
            columnArr.push({
                header: WtfGlobal.getLocaleText("acc.field.priceSource"), // "Price Source",
                dataIndex: "priceSource",
                align: 'left',
                width: 250,
                renderer: WtfGlobal.deletedRenderer
//                hidden: (!this.isCustomer && !Wtf.account.companyAccountPref.productPricingOnBands) || (this.isCustomer && !Wtf.account.companyAccountPref.productPricingOnBandsForSales)
            }, {
                header: WtfGlobal.getLocaleText("acc.field.priceBand"),
                width: 250,
                align: 'left',
                dataIndex: this.readOnly ? 'pricingbandmastername' : 'pricingbandmasterid',
//                hidden: (!this.isCustomer && !Wtf.account.companyAccountPref.productPricingOnBands) || (this.isCustomer && !Wtf.account.companyAccountPref.productPricingOnBandsForSales),
                renderer: this.readOnly ? "" : Wtf.comboBoxRendererwithClearFilter(this.pricingBandMasterEditor),
                editor: (this.isNote || this.readOnly || this.isViewTemplate || (this.isEdit && this.isLinkedTransaction)) ? "" : this.pricingBandMasterEditor //||this.UomSchemaType==Wtf.PackegingSchema
            });
        }
        
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.field.PartialAmount(%)"),
            dataIndex: "partamount",
            align: 'right',
//             id:this.id+"partdisc",
             hidden:!this.isNote || this.noteTemp || !this.isCustomer,
             width:120,
             fixed:true,
             renderer:function(v){return'<div class="currency">'+parseFloat(v).toFixed(2)+'%</div>';}
            //editor:this.partAmount
        },{
            header: WtfGlobal.getLocaleText("acc.field.DiscountType"),
            width:150,
            dataIndex:'discountispercent',
//            id:this.id+"discountispercent",
//            fixed:true,
            hidden:SATSCOMPANY_ID==companyid?true:(this.isQuotation?false:(this.isRequisition || this.noteTemp ||this.isRFQ)),
            renderer:Wtf.comboBoxRenderer(this.rowDiscountTypeCmb),
            editor:(this.isNote||this.readOnly||this.isViewTemplate)?"":this.rowDiscountTypeCmb
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridDiscount"),//"Discount",
             dataIndex:"prdiscount",
//             id:this.id+"prdiscount",
             align:'right',
//             fixed:true,
             width:150,
             hidden:SATSCOMPANY_ID==companyid?true:(this.isQuotation?false:(this.isRequisition || this.noteTemp ||this.isRFQ)),
             renderer:function(v,m,rec){
                 if(rec.data.discountispercent) {
                     v= v + "%";
                } else {
                    var symbol = WtfGlobal.getCurrencySymbol();
                     if(rec.data['currencysymbol']!=undefined && rec.data['currencysymbol']!=""){
                        symbol = rec.data['currencysymbol'];
                    }

                     v= WtfGlobal.conventInDecimal(v,symbol)
                }
//                 return'<div class="currency">'+v+'</div>';
                var returnStr =discountRenderer(v,rec);
                return returnStr;
            },
             editor:this.readOnly||this.isNote||this.isViewTemplate?"":this.transDiscount
         },{
             header: WtfGlobal.getLocaleText("acc.invoice.proTax"),//"Product Tax",
             dataIndex:"prtaxid",
//             id:this.id+"prtaxid",
             fixed:true,
             width:150,
             hidden:!(this.editTransaction||this.readOnly) || this.noteTemp,// || this.isOrder,
             renderer:Wtf.comboBoxRenderer(this.transTax),
             editor:this.readOnly||this.isNote||this.isViewTemplate?"":this.transTax  //this.transTax
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridTaxAmount"),//"Tax Amount",
             dataIndex:"taxamount",
//             id:this.id+"taxamount",
              fixed:true,
            //align:'right',
             width:150,
             editor:(this.readOnly||this.isViewTemplate || (this.isCustomer?!Wtf.dispalyUnitPriceAmountInSales:!Wtf.dispalyUnitPriceAmountInPurchase))?"":this.transTaxAmount,
             hidden:!(this.editTransaction||this.readOnly)|| this.noteTemp, // || !this.isOrder,
             renderer:this.setTaxAmountWithotExchangeRate.createDelegate(this)
        },{
             header:this.isRequisition ? (WtfGlobal.getLocaleText("acc.field.EstimatedCost")): this.isNote?WtfGlobal.getLocaleText("acc.invoice.gridCurAmt"):WtfGlobal.getLocaleText("acc.invoice.gridAmount"),//"Current Amount ":"Amount",
             dataIndex:this.moduleid == Wtf.Acc_Purchase_Requisition_ModuleId ? "estcost" : "amount",
             id:"estcost"+this.heplmodeid+this.id,
            hidden: this.isRFQ,
             align:'right',
             width:200,
             renderer:(this.isNote?WtfGlobal.withoutRateCurrencySymbol:this.calAmountWithoutExchangeRate.createDelegate(this))
        },{
            header: 'Line Level Term Amount',
             dataIndex:"lineleveltermamount",
            hidden: true,
             hideLabel : true,
             align:'right',
             width:200
        });
        if(Wtf.account.companyAccountPref.activateProfitMargin  && (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId)) { 
         columnArr.push({
            header:WtfGlobal.getLocaleText("acc.invoiceList.ven"),//"Vendor",
            width:200,
            dataIndex:'vendorid',
            hidden:!(Wtf.account.companyAccountPref.activateProfitMargin  && (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId)),
            renderer:Wtf.comboBoxRenderer(this.vendorEditor),
            editor:this.vendorEditor
        },{
             header:WtfGlobal.getLocaleText("acc.field.UnitCost"),// "Unit Cost",
            dataIndex: "vendorunitcost",
//             id:this.id+"vendorunitcost",
             align:'right',
             width:150,
             renderer:WtfGlobal.withVendorCurrencyUnitCostRenderer,
             editor:this.editCost,
             editable:true,
             hidden: !(Wtf.account.companyAccountPref.activateProfitMargin  && (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId))
        },{
             header:WtfGlobal.getLocaleText("acc.field.VendorCurrencyExchangeRate"),// "Vendor Currency Exchange Rate",
            dataIndex: "vendorcurrexchangerate",
//             id:this.id+"vendorcurrexchangerate",
             align:'right',
             width:150,
             renderer:this.exchangeRateRenderer,
             editor:this.editExchangeRate,
             editable:true,
             hidden: !(Wtf.account.companyAccountPref.activateProfitMargin  && (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId))
        },{
             header:WtfGlobal.getLocaleText("acc.field.TotalCostInBase"),//"Total Cost In Base",
             dataIndex:"totalcost",
             hidden: !(Wtf.account.companyAccountPref.activateProfitMargin  && (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId)),
             align:'right',
             width:200,
             renderer:this.calTotalCostWithoutExchangeRate.createDelegate(this)
        },{
             header:WtfGlobal.getLocaleText("acc.field.ProfitMarginInBase"),//"Profit Margin In Base",
             dataIndex:"profitmargin",
             hidden: !(Wtf.account.companyAccountPref.activateProfitMargin  && (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId)),
             align:'right',
             width:200,
            renderer: this.lineLevelProfitMarginRenderer.createDelegate(this)
        },{
             header:WtfGlobal.getLocaleText("acc.field.ProfitMargin(%)"),//"Profit Margin(%)",
             dataIndex:"profitmarginpercent",
             hidden: !(Wtf.account.companyAccountPref.activateProfitMargin  && (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId)),
             align:'right',
             width:200,
            renderer: this.lineLevelProfitMarginPercentRenderer.createDelegate(this)
        });
        }
          if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.invoicegrid.TaxAmount"),//"Total Tax Amount",
                dataIndex:"recTermAmount",
                hidden : this.isRFQ || this.isRequisition ? true : false,
                align:'right',
                width:100,
                renderer: function (value, m, rec) {
                    var hideUnitPriceAmount = this.isCustomer ? !Wtf.dispalyUnitPriceAmountInSales : !Wtf.dispalyUnitPriceAmountInPurchase;
                    return WtfGlobal.withoutRateCurrencySymbolWithPermissionCheck(value, m, rec, hideUnitPriceAmount);
                }.createDelegate(this)
            },{
                header:WtfGlobal.getLocaleText("acc.master.invoiceterm.nontaxablecharges"),//"Other Term Non Taxable Amount",
                dataIndex:"OtherTermNonTaxableAmount",
                hidden :  true
            },{
                header: WtfGlobal.getLocaleText("acc.invoicegrid.tax"), 
                align: 'center',                
                width: 40,
                dataIndex:"LineTermdetails",
                renderer: this.isRFQ || this.isRequisition ? "" : this.addRenderer.createDelegate(this),
                //hidden:  (Wtf.Countryid != Wtf.Country.INDIA) ?  true : this.isRFQ || this.isRequisition ? true : false 
                hidden:  this.isRFQ || this.isRequisition ? true : false 
            });
        }
        if (CompanyPreferenceChecks.displayUOMCheck()) {
            columnArr.push({
                header: WtfGlobal.getLocaleText("acc.product.displayUoMLabel"),
                width: 140,
                align: 'right',
                dataIndex: 'displayuomvalue',
                renderer: WtfGlobal.displayUoMRenderer(this.productComboStore, "productid", "displayUoMName", this.store)
            });
        }
        /**
         * add Inspection Template and Inspection Template editor icon columns
         * if MRP QA flow is activated
         */
        if (Wtf.account.companyAccountPref.activateMRPManagementFlag && this.moduleid==Wtf.Acc_Sales_Order_ModuleId && Wtf.account.companyAccountPref.columnPref.isQaApprovalFlowInMRP) {
            columnArr.push({
                header: WtfGlobal.getLocaleText("acc.lp.inspectiontemplate"),
                width: 140,
                dataIndex: 'inspectionTemplate',
                renderer: Wtf.comboBoxRenderer(this.inspectionTemplateCombo),
                editor:this.readOnly?"":this.inspectionTemplateCombo
            });
            columnArr.push({//add inspection template editor icon
                header: '',
                align:'center',
                renderer: this.inspectionTemplateRenderer.createDelegate(this),
                dataIndex:'inspectionTemplateWindow',
                id:this.id+'inspectionTemplateWindow',
                width:40
            });
        }
        if(Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.account.companyAccountPref.registrationType==Wtf.registrationTypeValues.DEALER){
         if(this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId && this.isExciseTab ){
            columnArr.push({
                    header: WtfGlobal.getLocaleText("acc.invoice.grid.dealerExcisedetails"),//"Dealer Excise Details",
                    align:'center',
    //                hidden:!(this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId),
                    width:200,
                    renderer:function(v){
                        return "<div class='" + getButtonIconCls(Wtf.etype.exciseDetailWindow) + "'></div>";
                    }
                });
         }else if(this.moduleid==Wtf.Acc_Invoice_ModuleId && this.isExciseTab){
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.invoice.grid.supplierExcisedetails"),//"Supplier Excise Details",
                align:'center',
//                hidden:!(this.moduleid==Wtf.Acc_Invoice_ModuleId),
                width:200,
                renderer:function(v){
                    return "<div class='" + getButtonIconCls(Wtf.etype.supplierDetailWindow) + "'></div>";
                }
            });
         }
        }
        if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isTDSApplicable && this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId && !this.isTemplate) {// TDS is only for INDIA Country and - On this amount TDS is Applied
            columnArr.push({
                header: "TDS Calculation",
                align: 'center',
                width: 40,
                renderer: this.addRendererTDS.createDelegate(this),
                hidden: !Wtf.isTDSApplicable
            });
        }

        if(!this.isNote && !this.readOnly && !this.isViewTemplate && !this.isLinkedTransaction) {
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),//"Action",
                align:'center',
                width:80,
                renderer: this.deleteRenderer.createDelegate(this)
            });
        }
        /*
         * add Ingradient details button
         */
        if (Wtf.account.companyAccountPref.jobWorkOutFlow && (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId )) {
            columnArr.push({
                header: '',
                dataIndex: "ingradientrenderer",
                align: 'center',
                renderer: this.jobworkRenderer.createDelegate(this),
                hidden: (!Wtf.account.companyAccountPref.jobWorkOutFlow),
                width: 40
            })
        }
        if (CompanyPreferenceChecks.discountMaster() && (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId || this.moduleid==Wtf.Acc_Invoice_ModuleId)) {
            columnArr.push({//added the view or edit discount details icon in grid.
                header: WtfGlobal.getLocaleText("acc.discountdetails.title"),
                align: 'center',
                renderer: this.discountdetailsRenderer.createDelegate(this),
                dataIndex: 'discountdetailswindow',
                id: this.id + 'discountdetailswindow',
                hidden: false,
                width: 200
            });
        }
        /**
         * Put column for ITC type for Vendor invoice module
         * This is applicable for India only
         */
        if (WtfGlobal.isIndiaCountryAndGSTApplied() && Wtf.isitcapplicable && (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId)) {
            var typeArr = new Array();
            typeArr.push([Wtf.GSTITCTYPEID.DEFAULT, Wtf.GSTITCTYPE.DEFAULT]);
            typeArr.push([Wtf.GSTITCTYPEID.BLOCKEDITC, Wtf.GSTITCTYPE.BLOCKEDITC]);
            typeArr.push([Wtf.GSTITCTYPEID.ITCREVERSAL, Wtf.GSTITCTYPE.ITCREVERSAL]);
            this.itcType = new Wtf.data.SimpleStore({
                fields: ['typeid', 'name'],
                data: typeArr
            });
            this.itcTypeCmb = new Wtf.form.ComboBox({
                store: this.itcType,
                name: 'typeid',
                displayField: 'name',
                valueField: 'typeid',
                mode: 'local',
                triggerAction: 'all',
                selectOnFocus: true
            });
            columnArr.push({
                header: WtfGlobal.getLocaleText("acc.India.ITC.dropdown"),
                width: 150,
                dataIndex: 'itctype',
                renderer: Wtf.comboBoxRenderer(this.itcTypeCmb),
                editor: (this.readOnly || this.isViewTemplate) ? "" : this.itcTypeCmb
            });
        }
        this.cm=new Wtf.grid.ColumnModel(columnArr); 
        this.sm=this.sModel;
    },
    checkSelections:function( scope, rowIndex, keepExisting, record){
        if(rowIndex== (this.store.getCount()-1)){
            return false;
        }else{
            return true;
        }
       
    },
    serialRenderer:function(v,m,rec){
        return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.serial.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.serial.desc.title")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
    },
    inspectionTemplateRenderer:function(v,m,rec){
        return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.inspection.area.edit.tooltip")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.inspection.area.edit")+"' class='"+getButtonIconCls(Wtf.etype.inspectiontemplategridrow)+"'></div>";
    },
    discountdetailsRenderer:function(v,m,rec){                                  //Icon Renderer for discount details 
        return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.discountdetails.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.discountdetails.title")+"' class='"+getButtonIconCls(Wtf.etype.discountdetails)+"'></div>";
    },
     jobworkRenderer: function(v, m, rec) {
        return "<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.JobWorkOut.ingradientdaetils") + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.JobWorkOut.ingradientdaetils") + "' class='" + getButtonIconCls(Wtf.etype.doDetails) + "'></div>";
    },
    conversionFactorRenderer:function(store, valueField, displayField,gridStore) {
        return function(value, meta, record) {
            if(value != "") {
                value = (parseFloat(getRoundofValue(value)).toFixed(Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT)=="NaN")?parseFloat(0).toFixed(Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT):parseFloat(getRoundofValueWithValues(value,Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT)).toFixed(Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT);
            }
            var idx = Wtf.uomStore.find("uomid", record.data["uomid"]);            
            if(idx == -1)
                return value;
            var uomname = Wtf.uomStore.getAt(idx).data["uomname"];
            if (uomname == "N/A") {
                return value;
            }
            var rec="";
            idx = store.find(valueField, record.data[valueField]);
            if(idx == -1){
                idx = gridStore.find(valueField, record.data[valueField]);
                if(idx == -1)
                    return value;
                rec = gridStore.getAt(idx);
                return "1 "+ uomname +" = "+ +value+" "+rec.data["baseuomname"];
            }else{
                 rec = store.getAt(idx);
                 return "1 "+ uomname +" = "+ +value+" "+rec.data[displayField];
            }  
            
        }
    },
    bomComboRenderer: function(combo) {
        return function(value, metadata, record, row, col, store) {
            var idx = WtfGlobal.searchRecordIndex(combo.store, value, combo.valueField);
            var fieldIndex = "bomcode";
            if (idx == -1) {
                if (record.data["bomcode"] && record.data[fieldIndex].length > 0) {
                    return record.data[fieldIndex];
                } else {
                    return "";
                }
            }
            var rec = combo.store.getAt(idx);
            var displayField = rec.get("bomcode");
            record.set("bomid", value);
            record.set("bomcode", displayField);
            return displayField;
        }
    },
    descriptionRenderer :function(val, meta, rec, row, col, store) {
        var regex = /(<([^>]+)>)/ig;
//        val = val.replace(/(<([^>]+)>)/ig,"");
        var tip = val.replace(/"/g,'&rdquo;');
        meta.attr = 'wtf:qtip="'+tip+'"'+'" wtf:qtitle="'+WtfGlobal.getLocaleText("acc.gridproduct.discription")+'"';
        return val;
    },
    
    //To fetch The available bom codes for selected Product ID
    fetchBOMCodes:function(grid, rowIndex, columnIndex, e){
        var record = grid.getStore().getAt(rowIndex);  // Get the Record
        var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
        var type = record.get("type");
        var bomid = record.get("bomid");
        if(fieldName == "bomcode" && (type!="Inventory Assembly" && type!="Job Work Assembly" )){
            this.bomcombo.disable();
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                msg: WtfGlobal.getLocaleText("acc.field.bomcode.dogrid.errormsg"),  //This is not a Assembly Product. You cannot select BOM Code.
                buttons: Wtf.MessageBox.OK,
                animEl: 'mb9',
                icon: Wtf.MessageBox.QUESTION
            });            
        } else if(fieldName == "bomcode" && type=="Inventory Assembly" && (bomid!=undefined && bomid!=null && bomid!="" && (this.editTransaction && !this.copyTrans))){  //Edit Case
            this.bomcombo.disable();
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                msg: WtfGlobal.getLocaleText("acc.field.bomcode.dogrid.edit.errormsg"),  //You cannot change the BOM Code.
                buttons: Wtf.MessageBox.OK,
                animEl: 'mb9',
                icon: Wtf.MessageBox.QUESTION
            });
        } else if(fieldName == "bomcode" && (type=="Inventory Assembly" || type=="Job Work Assembly") && (bomid!=undefined && bomid!=null && bomid!="" && this.copyTrans)){   //Copy Case
            this.bomcombo.enable();
        } else {
            this.bomcombo.enable();
        }
    },
    // Used as exchange rate renderer eg. 1 $ = 1.1223403456 SGD
    exchangeRateRenderer:function(value,meta,record) {
        var currencysymbol=WtfGlobal.getCurrencySymbol();
        var currencysymboltransaction=((record==undefined||record.data.vendorcurrencysymbol==null||record.data['vendorcurrencysymbol']==undefined||record.data['vendorcurrencysymbol']=="")?currencysymbol:record.data['vendorcurrencysymbol']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
        return "1 "+ currencysymboltransaction +" = " +value+" "+currencysymbol;
    },
    
    quantityWithUOMRenderer:function(store, valueField, displayField) {
        return function(value, meta, record) {
            value=(parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            var idx = Wtf.uomStore.find("uomid", record.data["uomid"]);            
            if(idx == -1)
                return value;
            var uomname = Wtf.uomStore.getAt(idx).data["uomname"];
            return value+" "+uomname;
//            idx = store.find(valueField, record.data[valueField]);
//            if(idx == -1)
//                return value;
//            var rec = store.getAt(idx);
//            return "1 "+ uomname +" = "+ +value+" "+rec.data[displayField];
        }
    },
    quantityRenderer:function(val,m,rec){
        if(val == ""){
            return val;
        }else{
            /*For SATS*/
            if(SATSCOMPANY_ID==companyid){
                var newVal="";
                newVal=val.toString();
                if(newVal.indexOf("X")!=-1){
                    return newVal;
                }
            }
            var v = (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            v= WtfGlobal.convertInDecimalWithDecimalDigit(v,"",Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            /**********/
            return v;
        }
    },
    deleteRenderer:function(v,m,rec){
        return "<div style='margin: auto;' class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    addRenderer: function(v, m, rec) {
        var hideUnitPriceAmount = this.isCustomer ? !Wtf.dispalyUnitPriceAmountInSales : !Wtf.dispalyUnitPriceAmountInPurchase;
        if (this.isModuleForAvalara) {
            return getToolTipOfAvalaraTerms(v, m, rec, hideUnitPriceAmount);
        } else {
            return getToolTipOfTermsfun(v, m, rec, hideUnitPriceAmount);
        }
    },
    addRendererTDS: function(v, m, rec) { // Onclick icon TDS window will open
//          return getToolTipOfTDSfun(v, m, rec)  
        return "<div class='" + getButtonIconCls(Wtf.etype.tdswinproductgrid) + "'></div>";
    },
    /*For SATS*/
    handleSATSDeleteSubProducts:function(grid,rowindex){
        var store=grid.getStore();
        var record = store.getAt(rowindex);
        var parentid=record.data.productid;
        while(true){
            var record = store.getAt(rowindex+1);
            if(record && record.data.parentid==parentid){
                var deletedData=[];
                var newRec=new this.deleteRec({
                    productid:record.data.productid,
                    productname:record.data.productname,    
                    productquantity:record.data.quantity    
                });                            
                deletedData.push(newRec);
                this.deleteStore.add(deletedData);  
                store.remove(store.getAt(rowindex+1));
            } else{
                break;
            }  
        }
        this.fireEvent('datachanged',this);
    },
    /**********/
    addProductList:function(){//ERP-8199 :
        return "<div class='pwnd add-gridrow' wtf:qtip=\"Click to add products\"></div>";
    },
    outstandingSOCheck: function (obj) {    //Call this function when user enter the quantity       //ERP-37555
        if (obj != undefined || obj != null) {
            this.obj = obj;
            if (this.obj.field == 'quantity' && this.moduleid == Wtf.Acc_Sales_Order_ModuleId) {    //Check the Outstanding quntity for SO only
            var prodid = this.obj.record.data.productid;    //Assembly Product ID
            var prodQty = this.obj.record.data.quantity;    //Entered assembly product quantity
            var prodBomId = this.obj.record.data.bomid;     //Assembly Product's BOM ID
            var isAutoAssemblyProd = this.obj.record.data.isAutoAssembly;   //Check auto-Build Assembly is true or not
            var productType = this.obj.record.data.type;   //Check auto-Build Assembly is true or not
            if(productType!=undefined && productType!=null && productType!="" && productType!="Service"){
                if (prodQty != "" && prodQty > 0 && isAutoAssemblyProd) { //Check balance quantity of BOM Product if Assembly Product's auto-Build Assembly is true
                    Wtf.Ajax.requestEx({
                        url: "ACCProductCMN/getOutstandingPOSOCount.do",
                        params: {
                            ids: prodid,
                            getSOPOflag: true,
                            moduleid: this.moduleid,
                            startdate: WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true)),
                            enddate: WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false)),
                            assemblyqty: prodQty,
                            prodbomid: prodBomId,
                            originalQty : (this.isEdit!=undefined && this.isEdit) ? this.originalQty : 0,
                            isEdit : this.isEdit
                        }
                    }, this, function (res, req) {
                        var qtymsg = res.qtymsg;
                        if (qtymsg != undefined && qtymsg != "") {  //On Response if message is not empty mean enough BOM quantity is not available to build auto build assembly
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), qtymsg], 2);  //Show ERROR message for insufficient BOM quantity.
                            this.obj.record.set('quantity', 0); //Set Quantity zero if enough BOM is not available.
                            return false;
                        } else {
                            return;
                        }
                    }, function (res, req) {
                        return false;
                    });
                }
            }//end of product type
        }
    }
    },
    inspectionTemplateChange: function(obj){
        /**
         * remove old json of inspection form details from product level
         */
        if (obj != undefined || obj != null) {
            this.obj = obj;
            if (this.obj.field == 'inspectionTemplate' && this.moduleid == Wtf.Acc_Sales_Order_ModuleId) {
                this.obj.record.data.inspectionAreaDetails = "";
            }
        }
    },
    RitchTextBoxSetting:function(grid, rowIndex, columnIndex, e){
        var record = grid.getStore().getAt(rowIndex);
        var fieldName= grid.getColumnModel().getDataIndex(columnIndex);

    //fieldName here is dataIndex
    if (fieldName === 'originalSourceShippingAddress' && Wtf.account.companyAccountPref.activateGroupCompaniesFlag && Wtf.account.companyAccountPref.isMultiGroupCompanyParentFlag) {
        var value = record.get(fieldName);
        if(record.data.productid !=undefined && record.data.productid !=""){
            this.shippingDescTextArea = new Wtf.form.TextArea({// Just make new Wtf.form.TextArea to new Wtf.form.HtmlEditor to fix ERP-8675
                name: 'shippingTextArea',
//                cls:"ux-calc-equals",
                readOnly :true
            });
            
//            this.shippingDescTextArea=  new Wtf.RichTextArea({
//                rec:record,
//                fieldName:'shippingTextArea',
//                id: 'shippingTextAreaId',
//                val: '',
//                readOnly:true
//            });
            
            var val=record.data.desc;
            var aurl=null;
            if(this.parentObj.fromLinkCombo!=undefined && this.parentObj.fromLinkCombo!=null){
                if(this.moduleid==Wtf.Acc_Purchase_Order_ModuleId && this.parentObj.fromLinkCombo.getValue()===0){//Linked to SalesOrder
                    aurl="ACCPurchaseOrderCMN/fetchShippingAddressOfPO.do";
                }
                Wtf.Ajax.requestEx({
                    url:aurl,
                    params: {
                        sodetailid:record.data.rowid,
                        productid:record.data.productid,
                        moduleid:this.moduleid
                    }
                }, this, function(response) {
                    if(response.success){
                        this.shippingDescTextArea.setValue(response.shippingAddress);
                    }
                }, function() {

                    });
            
                var descWindow=Wtf.getCmp(this.id+'ShippingWindow')
                if(descWindow==null){
                    var win = new Wtf.Window
                    ({
                        width: 560,
                        height:310,
                        title:WtfGlobal.getLocaleText("acc.productGrid.MultiCompany.shippingAddress"),
                        layout: 'fit',
                        id:this.id+'ShippingWindow',
                        bodyBorder: false,
                        closable:   true,
                        resizable:  false,
                        modal:true,
                        items:[this.shippingDescTextArea],
                        bbar:
                        [{
                            text: 'Cancel',
                            handler: function()
                            {
                                win.close();   
                            }
                        }]
                    });
                }
                win.show(); 
            }
        }
    }else{
            /**
            * Below check is implemented to check weather the User had enabled Allow to edit Products Custom field in various documents where product can be used if it is disabled then Displaying popup message
            * ERM-177 / ERP-34804
            */
            var isCustomColumn=grid.getColumnModel().config[columnIndex].iscustomcolumn;
            var isRelatedModuleAllowEdit=grid.getColumnModel().config[columnIndex].relatedModuleIsAllowEditid;
            if(isCustomColumn && !isRelatedModuleAllowEdit && isRelatedModuleAllowEdit!=undefined && !this.readOnly){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.dimension.productmodule.editmessages")]);
                return false;
            }                        
            if (e.getTarget(".richtext")) {

                value = record.get(fieldName);
                if (value == "" && record.data.productid != "" && (!this.isEdit || (this.isEdit && record.modified.pid != undefined)) && !Wtf.getCmp(record.id + record.data.pid + fieldName)) {

                    /*
                     * Here Data will be fetched for Rich Text Area.
                     */
                    Wtf.Ajax.requestEx({
                        url: "ACCProduct/getRichTextArea.do",
                        params: {
                            fieldName: fieldName,
                            productid: record.data.productid
                        }
                    },
                    this,
                            function (response) {
                                if (response.success) {
                                    value = response.data;
                                    record.set(fieldName, value);
                                }

                                this.richText = new Wtf.RichTextArea({
                                    rec: record,
                                    fieldName: fieldName,
                                    val: value ? value : "",
                                    readOnly: this.readOnly,
                                    id: record.id + record.data.pid + fieldName
                                });

                                this.richText.win.on('hide', function () {
                                    /*
                                     * Here Flag is set so value for This field will not be fetched at back end
                                     */
                                    record.set("richText" + fieldName, "changed");
                                }, this);

                            },
                            function (response) {
                                var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                            });
                } else {
                    /*
                     * If Data for Rich Text Area is already present and ajax call not required then value will be assigned here..
                     */
                    this.richText = new Wtf.RichTextArea({
                        rec: record,
                        fieldName: fieldName,
                        val: value ? value : "",
                        readOnly: this.readOnly,
                        id: record.id + record.data.pid + fieldName
                    });

                    this.richText.win.on('hide', function () {
                        record.set("richText" + fieldName, "changed");
                    }, this);
                }
            }
            /**
            * Code to check and uncheck column
            */
            var data = record.get(fieldName);
            if (fieldName == "joborderitem") {
                record.set("joborderitem", !data);
            }
            if(Wtf.account.companyAccountPref.proddiscripritchtextboxflag!=0 && !this.readOnly ){
                if(fieldName == "desc"){
                    if (Wtf.account.companyAccountPref.proddiscripritchtextboxflag==1) {
                        this.prodDescTextArea = new Wtf.form.TextArea({// Just make new Wtf.form.TextArea to new Wtf.form.HtmlEditor to fix ERP-8675
                            name: 'remark',
                            id: 'descriptionRemarkTextAreaId'
                        });
                    } else if (Wtf.account.companyAccountPref.proddiscripritchtextboxflag==2){
                        this.prodDescTextArea = new Wtf.form.HtmlEditor({// Just make new Wtf.form.TextArea to new Wtf.form.HtmlEditor to fix ERP-8675
                            name: 'remark',
                            id: 'descriptionRemarkTextAreaId'
                        });
                    }

                    var val=record.data.desc;
                    //                val = val.replace(/(<([^>]+)>)/ig,""); // Just comment this line to fix ERP-8675
                    this.prodDescTextArea.setValue(val);
                    if(record.data.productid !=undefined && record.data.productid !=""){
                        var descWindow=Wtf.getCmp(this.id+'DescWindow')
                        if(descWindow==null){
                            var win = new Wtf.Window
                            ({
                                width: 560,
                                height:310,
                                title:record.data.productname +" "+WtfGlobal.getLocaleText("acc.gridproduct.discription"),
                                layout: 'fit',
                                id:this.id+'DescWindow',
                                bodyBorder: false,
                                closable:   true,
                                resizable:  false,
                                modal:true,
                                items:[this.prodDescTextArea],
                                bbar:
                                [{
                                    text: 'Save',
                                    iconCls: 'pwnd save',
                                    handler: function()
                                    {
                                        record.set('desc',  Wtf.get('descriptionRemarkTextAreaId').getValue());
                                        win.close();   
                                    }
                                },{
                                    text: 'Cancel',
                                    handler: function()
                                    {
                                        win.close();   
                                    }
                                }]
                            });
                        }
                        win.show(); 
                    }
                    return false;
                }
            }
        }//end of rich text else part
            
    },
    handleRowClick:function(grid,rowindex,e){
        this.rowIndexForSpecialKey = rowindex;
        
        if(!(this.isViewTemplate || this.readOnly) && e.target.className == "pwndBar2 shiftrowupIcon") {
            moveSelectedRowFormasterItems(grid,0,rowindex);
        }
        if(!(this.isViewTemplate || this.readOnly) && e.target.className == "pwndBar2 shiftrowdownIcon") {
            moveSelectedRowFormasterItems(grid,1,rowindex);
        }
        if(e.getTarget(".delete-gridrow")){
            if(Wtf.Acc_RFQ_ModuleId == this.moduleid || this.moduleid==Wtf.Acc_Vendor_Quotation_ModuleId || this.moduleid==Wtf.Acc_Purchase_Order_ModuleId || this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId){
                var store=grid.getStore();
                var total=store.getCount();
                var record = store.getAt(rowindex);
                /* Link to  Combo*/
                var linkToComponent = Wtf.getCmp(this.linkTo);
                /* Link combo*/
                var linkComponent = Wtf.getCmp(this.link);
                /*For SATS*/
                if(SATSCOMPANY_ID==companyid){
                    this.handleSATSDeleteSubProducts(grid,rowindex);
                }
                /**********/
                /* Function is used to return ID of linked Document If it is Unlinked through Editing*/
                var lastProductDeleted = isLastProductDeleted(store, record);
                if (lastProductDeleted) {
                    var message = "Link Information of "
                    if (Wtf.Acc_RFQ_ModuleId == this.moduleid) {
                        message += "Purchase Requisition <b>";
                    } else {

                        if (linkToComponent) {
                            message += linkToComponent.lastSelectionText + " <b>";
                        }
                    }
                    
                        if(record && record.data && record.data.linkto){
                            message+= record.data.linkto;
                        }
                        message += "</b> will be Removed. </br>"+WtfGlobal.getLocaleText("acc.nee.48")

                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), message, function (btn) {
                            if (btn != "yes")
                                return;

                        if (record && record.data && record.data.linkid) {
                            if (this.prComboId) {
                                var component = Wtf.getCmp(this.prComboId);//linked document combo
                                if (component) {
                                    var value = component.getValue();
                                    if (value) {
                                        var arr = value.split(",");
                                       /* Block is used to remove linked document from combo */
                                            if (arr.length > 1) { 
                                            this.parentObj.linkedDocumentId += record.data.linkid + ",";//appending ID of removed document
                                            arr.remove(record.data.linkid);
                                            component.setValue(arr);
                                        } else if (this.isEdit) {
                                           /* Reseting Link &  Link to combo if all linked document is getting removed*/
                                                if (!(Wtf.Acc_RFQ_ModuleId == this.moduleid)) {
                                                this.parentObj.linkedDocumentId += record.data.linkid + ",";
                                                arr.remove(record.data.linkid);
                                                component.disable();
                                                linkToComponent.disable();
                                                component.setValue(arr);
                                                linkToComponent.setValue("");
                                                linkComponent.setValue("");
                                                linkComponent.enable();
                                            } else {
                                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.report.requestforquotation.delete.last")], 2);
                                                return false;
                                            }

                                        } else {
                                            arr.remove(record.data.linkid);
                                            component.setValue(arr);
                                        }
                                    }
                                }
                            }
                        }
                            var qty = record.data.quantity;
                            qty = (qty == "NaN" || qty == undefined || qty == null) ? 0 : qty;

                            if (record.data.copyquantity != undefined) {
                                var deletedData = [];
                                var newRec = new this.deleteRec({
                                    productid: record.data.productid,
                                    productname: record.data.productname,
                                    productquantity: qty,
                                    productbaseuomrate: record.data.baseuomrate,
                                    productbaseuomquantity: record.data.baseuomquantity,
                                    productuomid: record.data.uomid,
                                    //                                productinvstore:record.data.invstore,
                                    //                                productinvlocation:record.data.invlocation,
                                    productrate: record.data.rate
                                            //To do - Need to check this for multi UOM change
                                });
                                deletedData.push(newRec);
                                this.deleteStore.add(deletedData);
                            }
                            store.remove(store.getAt(rowindex));
                            if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
                                this.updateTermDetails();
                            }
                            if (rowindex == total - 1) {
                                this.addBlankRow();
                            }
                            this.fireEvent('datachanged', this);
                            this.fireEvent('productdeleted', this);
                        }, this);
                    } else{
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.nee.48"), function(btn){
                            if(btn!="yes") return;
                            var store=grid.getStore();
                            var total=store.getCount();
                            var record = store.getAt(rowindex);

                            var qty = record.data.quantity;
                            qty = (qty == "NaN" || qty == undefined || qty == null)?0:qty;

                            if(record.data.copyquantity!=undefined){                    
                                 var deletedData=[];
                                 var newRec=new this.deleteRec({
                                            productid:record.data.productid,
                                            productname:record.data.productname,    
                                            productquantity:qty, 
                                            productbaseuomrate:record.data.baseuomrate,
                                            productbaseuomquantity:record.data.baseuomquantity,
                                            productuomid:record.data.uomid,
            //                                productinvstore:record.data.invstore,
            //                                productinvlocation:record.data.invlocation,
                                            productrate:record.data.rate
                                            //To do - Need to check this for multi UOM change
                                        });                            
                                        deletedData.push(newRec);
                                        this.deleteStore.add(deletedData);                            
                            }
                            store.remove(store.getAt(rowindex));
                            if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
                                this.updateTermDetails();
                            }
                            if(rowindex==total-1){
                                this.addBlankRow();
                            }
                            this.fireEvent('datachanged',this);
                            this.fireEvent('productdeleted',this);
                        }, this);
                    }
            } else{
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.nee.48"), function(btn){
                    if(btn!="yes") return;
                    var store=grid.getStore();
                    var total=store.getCount();
                    var record = store.getAt(rowindex);

                    var qty = record.data.quantity;
                    qty = (qty == "NaN" || qty == undefined || qty == null)?0:qty;

                    if(record.data.copyquantity!=undefined){                    
                         var deletedData=[];
                         var newRec=new this.deleteRec({
                                    productid:record.data.productid,
                                    productname:record.data.productname,    
                                    productquantity:qty, 
                                    productbaseuomrate:record.data.baseuomrate,
                                    productbaseuomquantity:record.data.baseuomquantity,
                                    productuomid:record.data.uomid,
    //                                productinvstore:record.data.invstore,
    //                                productinvlocation:record.data.invlocation,
                                    productrate:record.data.rate
                                    //To do - Need to check this for multi UOM change
                                });                            
                                deletedData.push(newRec);
                                this.deleteStore.add(deletedData);                            
                    }
                    store.remove(store.getAt(rowindex));
                    this.addorRemoveBomCodeColumn(record,isAutoGenerateDO,store);
                    if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
                        this.updateTermDetails();
                    }
                    if(rowindex==total-1){
                        this.addBlankRow();
                    }
                    this.fireEvent('datachanged',this);
                    this.fireEvent('productdeleted',this);
                }, this);
            }
          } else if(e.getTarget(".serialNo-gridrow")){//serial no window
             var store=grid.getStore();
            var record = store.getAt(rowindex);
            var productid = record.get('productid');
            var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, productid, 'productid');
            var isAutoGenerateDO=false;
            var isAutFillBatchDetails=false;
            var blockQuantityCheck=false;
            this.blockQtyFlag = false;
            if(Wtf.getCmp(this.parentCmpID) != undefined  && Wtf.getCmp(this.parentCmpID).autoGenerateDO != undefined ){
                if(Wtf.getCmp(this.parentCmpID).autoGenerateDO.checked == true){
                    isAutoGenerateDO=true;
                }
            }
            if(Wtf.getCmp(this.parentCmpID) != undefined  && Wtf.getCmp(this.parentCmpID).isAutoFillBatchDetails != undefined ){
                if(Wtf.getCmp(this.parentCmpID).isAutoFillBatchDetails == true){
                    isAutFillBatchDetails=true;
                }
            }
            
             if(Wtf.getCmp(this.parentCmpID) != undefined  && Wtf.getCmp(this.parentCmpID).lockQuantity != undefined ){
                if(Wtf.getCmp(this.parentCmpID).lockQuantity.checked == true){
                    	
                    blockQuantityCheck=true;
                }
            }
            var linkValue =  parseInt(Wtf.getCmp(this.parentCmpID).fromLinkCombo.getValue());
            if(!isNaN(linkValue) && linkValue===0 && !this.isEdit){ // If Linked SO
                this.blockQtyFlag = true;
            }
            if(productComboRecIndex==-1){
                productComboRecIndex=WtfGlobal.searchRecordIndex(store, productid, 'productid');
            }
            if(productComboRecIndex >=0){
                var proRecord = this.productComboStore.getAt(productComboRecIndex);
                var recIndex = WtfGlobal.searchRecordIndex(this.productComboStore, productid, 'productid');
                if(recIndex==-1){
                    proRecord=this.getStore().getAt(productComboRecIndex);
                }
//                if(!this.readOnly){
                if(proRecord.data.type!='Service' && proRecord.data.type!='Non-Inventory Part'){  //serial no for only inventory type of product
                if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.account.companyAccountPref.isLocationCompulsory || Wtf.account.companyAccountPref.isWarehouseCompulsory || Wtf.account.companyAccountPref.isRowCompulsory || Wtf.account.companyAccountPref.isRackCompulsory || Wtf.account.companyAccountPref.isBinCompulsory){    
                   if(isAutoGenerateDO || blockQuantityCheck){
                        if(proRecord.data.isLocationForProduct || proRecord.data.isWarehouseForProduct || proRecord.data.isBatchForProduct || proRecord.data.isSerialForProduct  || proRecord.data.isRowForProduct || proRecord.data.isRackForProduct  || proRecord.data.isBinForProduct)
                        {
                           if((proRecord.data.isLocationForProduct || proRecord.data.isWarehouseForProduct) && !(proRecord.data.isBatchForProduct || proRecord.data.isSerialForProduct  || proRecord.data.isRowForProduct || proRecord.data.isRackForProduct  || proRecord.data.isBinForProduct) && isAutFillBatchDetails && (record.data.batchdetails == undefined || record.data.batchdetails == "" || record.data.batchdetails=="[]")){
                                        WtfGlobal.setDefaultWarehouseLocation(null, record,false,grid,rowindex);
                                
                             }
                        this.callSerialNoWindow(record);
                       }else{
                           WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.batchserial.Functinality")],2);   //Batch and serial no details are not valid.
                          return;
                       }
                    }else{
                        (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId)?WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.enableGRoptioninPI")],2):(this.moduleid == Wtf.Acc_Sales_Order_ModuleId ?WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.enableBlockoptioninSO")],2):WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.enableDOoptioninSI")],2));   
                        return;
                    }
                }
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.batchserial.funforInventoryitems")],2);   //Batch and serial no details are not valid.
                    return;
                }
//                }
            }
        } else if(e.getTarget(".add-gridrow")){//ERP-8199 :
            if(this.readOnly !=undefined && !this.readOnly && this.isLinkedTransaction !=undefined && !this.isLinkedTransaction &&!this.isViewTemplate){
                if (this.parentObj && this.parentObj.Currency != undefined) {
                    this.forCurrency = this.parentObj.Currency.getValue();
                }
                this.showProductGrid();
            }else{
                return;
            }
        } else if(e.getTarget(".termCalc-gridrow")){
            if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
                this.showTermWindow(grid.getStore().getAt(rowindex),grid,rowindex);
            }else{
                return;
            }
        } else if(e.getTarget(".exciseDealer-gridrow")){
            if(Wtf.Countryid == Wtf.Country.INDIA){
                if(!this.isEdit){
                    this.showExciseDealerDetailWindow(grid.getStore().getAt(rowindex),grid,rowindex);
                }else{
                    this.showExciseDealerDetailWindow(grid.getStore().getAt(rowindex),grid,rowindex,grid.getStore().getAt(0));
                }
            }else{
                return;
            }
        } else if(e.getTarget(".exciseSupplier-gridrow")){
            if(Wtf.Countryid == Wtf.Country.INDIA){
                this.showExciseSupplierDetailWindow(grid.getStore().getAt(rowindex),grid,rowindex);
            }else{
                return;
            }
        }else if (e.getTarget(".pwnd.doDetails-gridrow")){
            var isAutoGenerateDO=false;
            if (Wtf.getCmp(this.parentCmpID).autoGenerateDO.checked == true) {
                isAutoGenerateDO = true;
            }
            if (isAutoGenerateDO && (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId)) {
                /**
                 * Job work details for GRN and Auto GRN case
                 */
                var store = grid.getStore();
                var total = store.getCount();
                var record = store.getAt(rowindex);
                this.callJobOrderDetails(record);
            }else{
                if((this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId)){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.enableGRoptioninPI")],2) 
                }
            }
            
        }
        
        if (e.getTarget(".tdsCalc-productInvoicegridrow")) { // For Call TDS window
            var gridStoreDetails = grid.getStore();
            var jsonrecord = gridStoreDetails.getAt(rowindex);
            
            var businesspersoninfo = WtfGlobal.searchRecord(this.parentObj.personstore, this.parentObj.Name.getValue(), 'accid');
            if (!Wtf.isEmpty(businesspersoninfo) && !Wtf.isEmpty(businesspersoninfo.data.isTDSapplicableonvendor) && !businesspersoninfo.data.isTDSapplicableonvendor && Wtf.isEmpty(jsonrecord.data.appliedTDS)) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.vendorpaymentcontrollercmn.TDSIsNotAppliedForSelectedVendor")], 2);
                return false;
            }else if (!Wtf.isEmpty(businesspersoninfo) && Wtf.isEmpty(businesspersoninfo.data.deducteetypename) && Wtf.isEmpty(jsonrecord.data.appliedTDS)) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.vendorpaymentcontrollercmn.deducteeTypeNotSet")], 2);
                return false;
            }
            var alertMsg = "Do you want to calculate TDS ?";
            if (this.readOnly || this.isLinkedTransaction || !Wtf.isEmpty(jsonrecord.data.tdsjemappingID)) {
                if (!Wtf.isEmpty(jsonrecord.data.appliedTDS)) {
                    this.openWindowForSelectingTDS(jsonrecord, rowindex);
                    return true;
                } else {
                    return false;
                }
            } else if (this.editTransaction) {
                alertMsg = "Do you want to edit TDS ?";
            }
            Wtf.MessageBox.confirm("TDS Calculation", alertMsg, function (btn) {
                if (btn != "yes") {
                    return;
                }
                this.openWindowForSelectingTDS(jsonrecord, rowindex);
            }, this);

        }
        
        if(e.getTarget(".discountDetails-gridrow")){
            var store = grid.getStore();
            var record = store.getAt(rowindex);
            var productid = record.get('productid');
//            this.callDiscountDetails(record);                           //calls discount details window in which user can delete the discounts at the time of sales invoice creation which are mapped to product 
            var paramObj = {
                record:record,
                readOnly: this.readOnly,
                parentObj: this.parentObj,
                parentCmpScope:this,
                isLinkedTransaction:this.isLinkedTransaction
            };
            callDiscountDetailsDynamic(paramObj);                           //calls discount details window in which user can delete the discounts at the time of sales invoice creation which are mapped to product 
        }else {
                this.fireEvent("productselect", grid.getStore().getAt(rowindex).get("productid"));
        }
        /**
         * Open inspection template editor window if user click on inspection template edit icon
         */
        if(e.getTarget(".inspectionTemplate-gridrow")){
            var store = grid.getStore();
            var record = store.getAt(rowindex);
            //If no any template selected then show alert
            if(record.get("inspectionTemplate") == "None" || record.get("inspectionTemplate") == undefined || record.get("inspectionTemplate") == ""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("Please select Inspection Template first.")], 2);
            } else{
                var config = {'productgrid': grid, 'isTempSave': true, 'rowindex': rowindex};
                inspectionTab(record, "salesorder", store, record.data.rowid, false, config);
            }
        }
    },
    afterDeletingDiscount:function(recordArr, record){
        var jsonArr = createDiscountString(recordArr, record, record.data.rate, record.data.quantity); 
        var jsonObj;
        var jsonStr = ""
        if (jsonArr != "" && jsonArr != undefined) {
            jsonObj = {"data": jsonArr};
            jsonStr = JSON.stringify(jsonObj);
        }
        record.set("discountjson", jsonStr);
        this.parentObj.updateSubtotal();
    },
      callJobOrderDetails : function(obj){
        var joborderdetail="";
        if(obj.data!=undefined){
            if(!obj.data.isJobWorkOutProd || obj.data.isJobWorkOutProd==false){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.JobWorkOut.notjobworkoutprod")],2);
                return;
            }
            joborderdetail=obj.data.joborderdetail;
        }
        var index = this.productComboStore.findBy(function(rec) {
            if (rec.data.productid == obj.data.productid)
                return true;
            else
                return false;
        })
        var firstRow = index;
        if (index == -1) {
            index = this.store.findBy(function(rec) {
                if (rec.data.productid == obj.data.productid)
                    return true;
                else
                    return false;
            })
        }
        if (index != -1) {
            var prorec = this.productComboStore.getAt(index);
            if (firstRow == -1) {
                prorec = this.store.getAt(index);
            }
            if (prorec == undefined) {
                prorec = obj;
            }
        }
        
        this.joborderdetailsWin = new Wtf.account.JobWorkOutProductWindow({
            renderTo: document.body,
            title: WtfGlobal.getLocaleText("acc.JobWorkOut.ingradientdaetils"),
            isLocationForProduct: prorec.data.isLocationForProduct,
            isWarehouseForProduct: prorec.data.isWarehouseForProduct,
            productName: prorec.data.productname,
            prodId: prorec.data.productid,
            quantity:obj.data.quantity,
            isRowForProduct: prorec.data.isRowForProduct,
            isRackForProduct: prorec.data.isRackForProduct,
            isBinForProduct: prorec.data.isBinForProduct,
            isShowStockType: (this.isCustomer) ? true : false,
            defaultWarehouse: prorec.data.warehouse,
            documentid: (this.isEdit || obj.data.linkflag) ? obj.data.rowid : "",
            batchDetails: obj.data.batchdetails,
            warrantyperiod: prorec.data.warrantyperiod,
            warrantyperiodsal: prorec.data.warrantyperiodsal,
            isBatchForProduct: prorec.data.isBatchForProduct,
            isSerialForProduct: prorec.data.isSerialForProduct,
            isSKUForProduct: prorec.data.isSKUForProduct,
            isIsLocWarehouseForProduct: prorec.data.isIsLocWarehouseForProduct,
            width: 950,
            height: 400,
            joborderdetails:obj.data.joborderdetails,
            joborderdetail: joborderdetail,
            resizable: false,
            modal: true,
            parentGrid: this
        });
        this.joborderdetailsWin.on("beforeclose",function(){
            this.joborderdetails=this.joborderdetailsWin.getJobWorkItemDetails();
            var isfromSubmit=this.joborderdetailsWin.isfromSubmit;
                if(isfromSubmit){  //as while clicking on the cancel icon it was adding the reocrd in batchdetail json 
                    obj.set("joborderdetails",this.joborderdetails);
                }
              },this);
        this.joborderdetailsWin.show();  
    },
    showProductGrid : function() {//ERP-8199 :
        
        this.productSelWin = new Wtf.account.ProductSelectionWindow({
            renderTo: document.body,
            height : 600,
            width : 750,
            title:WtfGlobal.getLocaleText("acc.productselection.window.title"),
            layout : 'fit',
            modal : true,
            resizable : false,
            id:this.id+'ProductSelectionWindow',
            moduleid:this.moduleid,
            heplmodeid:this.heplmodeid,
            isJobWorkOrderReciever:this.isJobWorkOrderReciever,
            parentCmpID:this.parentCmpID,
            invoiceGrid:this,
            isCustomer : this.isCustomer,
            affecteduser: this.affecteduser,
            forCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency:"",
            currency: this.parentObj.Currency.getValue(),
//            quantity: obj.record.data.quantity,
            transactiondate : this.isCallFromSalesOrderTransactionForms ? WtfGlobal.convertToGenericDate(new Date()) :WtfGlobal.convertToGenericDate(this.billDate),//isCallFromSalesOrderTransactionForms->True if PO is created from SO transaction Form
            carryin : (this.isCustomer)? false : true,
            getSOPOflag :true,
            startdate : WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true)),
            enddate : WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false)),
            tempSOPOLinkFlag: this.tempSOPOLinkFlag,
            isIndividualProductPrice:!(this.tempSOPOLinkFlag && Wtf.account.companyAccountPref.carryForwardPriceForCrossLinking)
        });
        this.productSelWin.show();
        
    },
    showTermWindow : function(record,grid,rowindex) {
        var deliveredprodquantity = record.data.quantity;
        deliveredprodquantity = (deliveredprodquantity == "NaN" || deliveredprodquantity == undefined || deliveredprodquantity == null)?0:deliveredprodquantity;
        if(deliveredprodquantity<=0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),WtfGlobal.getLocaleText("acc.Workorder.Quantityshouldbegreaterthanzero")], 2);
            return false;
        }
        var venderDetails =WtfGlobal.searchRecord(this.parentObj.personstore, this.parentObj.Name.getValue(), 'accid');
        if(rowindex!=-1){
            this.TermGrid = new Wtf.account.TermSelGrid({
                id: 'TermSelGrid',
                isReceipt: false,                        
                border: false, 
                layout:"fit",
                width: 900,
                height:500,
                rowindex:rowindex,
                autoScroll:true, 
                cls:'gridFormat',
                region: 'center',
                viewConfig:{
                    forceFit:true
                },
                isEdit:this.isEdit,
                isLineLevel : true,
                isLink:this.parentObj.fromPO.getValue(),
                invAmount: record.data.amount,
                parentObj : this.parentObj,
                hideUnitPriceAmount : this.isCustomer ? !Wtf.dispalyUnitPriceAmountInSales : !Wtf.dispalyUnitPriceAmountInPurchase,
                isGST:this.isGST,
                gridObj : this,
                invQuantity: record.data.baseuomquantity,
                record:record,
                currencySymbol:this.symbol,
                venderDetails:venderDetails,
                scope:this
            });
            this.Termwindow= new Wtf.Window({
                modal: true,
                id:'termselectionwindowtest',
                title: (this.isModuleForAvalara) ? WtfGlobal.getLocaleText("acc.termselgrid.taxdescription") : WtfGlobal.getLocaleText("acc.invoicegrid.TaxWindowTitle"),
                buttonAlign: 'right',
                border: false,
                layout:"fit",
                width: 900,
                height:510,
                resizable : false,
                items: [this.TermGrid],
                buttons:
                [{
                    text: 'Save',
                    iconCls: 'pwnd save',
                    /**
                     * hide Save Button when Avalara Integration is enabled
                     * this is because user must not be allowed to edit tax details in the window when Avalara Integration is on
                     */
                    hidden: WtfGlobal.isUSCountryAndGSTApplied() && !this.isModuleForAvalara ? false : true,
                    scope:this,
                    disabled:this.isViewTemplate,
                    handler: function()
                    {
                        this.BeforeTermSave();
                        this.Termwindow.close();
        }
                },{
                    text: 'Close', 
                    scope:this,
                    handler: function()
                    {
                        this.Termwindow.close();
                    }
                }]
            });
            this.Termwindow.show();
        }
    },
    showExciseDealerDetailWindow : function(record,grid,rowindex,firstRow) {
        var deliveredprodquantity = record.data.quantity;
        deliveredprodquantity = (deliveredprodquantity == "NaN" || deliveredprodquantity == undefined || deliveredprodquantity == null)?0:deliveredprodquantity;
        if(deliveredprodquantity<=0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.Workorder.Quantityshouldbegreaterthanzero")], 2);
            return false;
        }
        if(Wtf.isEmpty(this.parentObj.defaultNatureOfPurchase.getRawValue())){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invoice.grid.selecttypeofsale")], 2);
            return false;
        }
        var productIndex=WtfGlobal.searchRecordIndex(this.productComboStore, record.data.productid, 'productid');
        var productdetails ="";
        if(productIndex!=-1){
         productdetails = this.productComboStore.getAt(productIndex)
        }
        var venderDetails =WtfGlobal.searchRecord(this.parentObj.personstore, this.parentObj.Name.getValue(), 'accid');
        if(rowindex!=-1){
            this.dealerExciseDetailGrid = new Wtf.account.dealerExciseDetail({
                id: 'TermSelGrid',
                isReceipt: false,                        
                border: false, 
                layout:"fit",
                width: 900,
                height:500,
                rowindex:rowindex,
                autoScroll:true, 
                cls:'gridFormat',
                viewMode:this.isViewTemplate,
                region: 'center',
                viewConfig:{
                    forceFit:true
                },
                isEdit:this.isEdit,
                isCopy:this.copyInv,
                firstRow:firstRow,
                isLineLevel : true,
                isLink:this.parentObj.fromPO.getValue(),
                invAmount: record.data.amount,
                parentObj : this.parentObj,
                gridObj : this,
                invQuantity: record.data.baseuomquantity,
                record:record,
                venderDetails:venderDetails,
                productDetails:productdetails,
                scope:this
            });

        }
    },
    showExciseSupplierDetailWindow : function(record,grid,rowindex) {
        var deliveredprodquantity = record.data.quantity;
        deliveredprodquantity = (deliveredprodquantity == "NaN" || deliveredprodquantity == undefined || deliveredprodquantity == null)?0:deliveredprodquantity;
        if(deliveredprodquantity<=0){
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.Workorder.Quantityshouldbegreaterthanzero")], 2);
            return false;
        }
//        if(Wtf.isEmpty(this.parentObj.defaultNatureOfPurchase.getRawValue())){
//            WtfComMsgBox(["Alert","Please select <b>Type of Sales</b> for Dealer Excise Details"], 2);
//            return false;
//        }
        var productIndex=WtfGlobal.searchRecordIndex(this.productComboStore, record.data.productid, 'productid');
        var productdetails ="";
        if(productIndex!=-1){
         productdetails = this.productComboStore.getAt(productIndex)
        }
        var venderDetails =WtfGlobal.searchRecord(this.parentObj.personstore, this.parentObj.Name.getValue(), 'accid');
        if(rowindex!=-1){
            this.dealerExciseDetailGrid = new Wtf.account.supplierExciseDetail({
                id: 'TermSelGrid',
                isReceipt: false,                        
                border: false, 
                layout:"fit",
                width: 900,
                height:500,
                rowindex:rowindex,
                autoScroll:true, 
                cls:'gridFormat',
                region: 'center',
                viewConfig:{
                    forceFit:true
                },
                isEdit:this.isEdit,
                viewMode:this.isViewTemplate,
                isLineLevel : true,
                isLink:this.parentObj.fromPO.getValue(),
                invAmount: record.data.amount,
                parentObj : this.parentObj,
                gridObj : this,
                invQuantity: record.data.baseuomquantity,
                record:record,
                venderDetails:venderDetails,
                productDetails:productdetails,
                scope:this
            });

        }
    },
    
    
    BeforeTermSave:function(){
        /*this.getStore().getAt(this.TermGrid.rowindex).set("LineTermdetails", JSON.stringify(this.TermGrid.getTermDetails()));
        this.getStore().getAt(this.TermGrid.rowindex).set("recTermAmount", this.TermGrid.getTotalTermAmt());
        this.parentObj.updateSubtotal();*/
        var rec = this.getStore().getAt(this.TermGrid.rowindex)
        var termStore = eval(this.TermGrid.getTermDetails());

        if(this.parentObj && this.parentObj.includingGST && this.parentObj.includingGST.getValue() == true) {
            this.getColumnModel().setRenderer(this.getColumnModel().findColumnIndex("amount"),WtfGlobal.withoutRateCurrencySymbol);
            termStore = this.calculateTermLevelTaxesInclusive(termStore, rec);
        } else {
            this.getColumnModel().setRenderer(this.getColumnModel().findColumnIndex("amount"),this.calAmountWithoutExchangeRate.createDelegate(this));
            termStore = this.calculateTermLevelTaxes(termStore, rec);
        }
        rec.set("LineTermdetails", Wtf.encode(this.TermGrid.getTermDetails()))
        this.updateTermDetails();
        this.parentObj.updateSubtotal();
        
    },
    storeRenderer:function(store, valueField, displayField,gridStore) {
        return function(value, meta, record) {
            value=(parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            value = WtfGlobal.convertInDecimalWithDecimalDigit(value,"",Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            var rec="";
            var idx = store.find(valueField, record.data[valueField]);
            if(idx == -1){
                idx = gridStore.find(valueField, record.data[valueField]);
                if(idx == -1)
                    return value;
                rec = gridStore.getAt(idx);
                return value+" "+rec.data["baseuomname"];
            }else{
                 rec = store.getAt(idx);
                 return value+" "+rec.data[displayField];
            }                          
            
        }
    },
    /*For SATS*/
    storeRendererForQuantity:function(store, valueField, displayField) {
        return function(value, meta, record) {
            var idx = store.find(valueField, record.data[valueField]);
            if(idx == -1)
                return value;
            var rec = store.getAt(idx);
            var newVal="";
            newVal=value.toString();
            if(newVal.indexOf("X")!=-1)
                return newVal;
                return parseFloat(value).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+" "+rec.data[displayField];
        }
    },
    /**********/
    
//    getStockQuantity : function(productId){
//        if(productId == undefined || productId == null || productId == "" )
//            return 1;
//        var value=1;
//        var idx = this.productComboStore.find('productid',productId);
//        if(idx == -1)
//            return value;
//        var rec = this.productComboStore.getAt(idx); 
//        if(this.isCustomer){
//            value = rec.get('stocksalesuomvalue');
//        }else{
//            value = rec.get('stockpurchaseuomvalue');
//        }
//        return parseFloat(value);
//    },

    checkRow:function(obj){ 
        this.rowIndexForSpecialKey = obj.row; // rowindex
        var rec=obj.record;
        var assemblyproductflag = true;
        var proqty = obj.record.data['quantity'];
        proqty = (proqty == "NaN" || proqty == undefined || proqty == null)?0:proqty;
        
        var isAutoGenerateDO=false;
        if(Wtf.getCmp(this.parentCmpID) != undefined  && Wtf.getCmp(this.parentCmpID).autoGenerateDO != undefined ){
             if(Wtf.getCmp(this.parentCmpID).autoGenerateDO.checked == true){
                 isAutoGenerateDO=true;
             }
       }
       var lockQuantitySO=false;
        if(Wtf.getCmp('lockQuantitySO') != undefined)
        {
            if(Wtf.getCmp('lockQuantitySO').getValue()==true){
                lockQuantitySO=true;
            }

        }
        if(obj.field=="uomid"){
            var prorec = null;
            var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
            if(productComboIndex >=0){
                prorec = this.productComboStore.getAt(productComboIndex);
                if(prorec.data.type=='Service'){  //|| prorec.data.type=='Non-Inventory Part'
//                          WtfComMsgBox(["Warning","UOM can not be set for Service and Non-Inventory products. "], 2);
                    return false;
                } else if(this.UomSchemaType==Wtf.UOMSchema && !prorec.data.multiuom){//&& prorec.data.type!='Non-Inventory Part'
//                          WtfComMsgBox(["Warning","Multi UOM not allowed for the selected product. "], 2);
                    return false;
                }
            }else if(this.productOptimizedFlag!= undefined && prorec==undefined){
                prorec = obj.record;
                if(prorec.data.type=='Service'){//|| prorec.data.type=='Non-Inventory Part'
                    //                          WtfComMsgBox(["Warning","UOM can not be set for Service and Non-Inventory products. "], 2);
                    return false;
                } else if(this.UomSchemaType==Wtf.UOMSchema && !prorec.data.multiuom){ //&& prorec.data.type!='Non-Inventory Part'
                    //                          WtfComMsgBox(["Warning","Multi UOM not allowed for the selected product. "], 2);
                    return false;
                }
            }
        }
        /*Code on product select starts*/        
        if(obj.field=="productid" || obj.field=="pid"){            
            /**
             * ERP-32829 
             * Check all GST Dim value selected or not
             */
            if (this.isGST && !this.parentObj.tagsFieldset.checkGSTDimensionValues(this.parentObj) && !this.isModuleForAvalara) {
                if( this.uniqueCase != Wtf.GSTCustVenStatus.APPLY_IGST) // Wtf.GSTCustVenStatus.APPLY_IGST Customer/ Vendor IMPORT and EXPORT type only , For INDIA
                   WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.invalidgstdetailsdim") ], 2);
               // obj.cancel = true; // Remove Restriction in Product selection if address details not proper
            }
            var index=this.productComboStore.findBy(function(rec){
                if(rec.data.productid==obj.value)
                    return true;
                else
                    return false;
            })
            var prorec=this.productComboStore.getAt(index); 
            var useStoreRec=false;
            if (prorec == undefined || prorec==null) {
                prorec = rec;
                useStoreRec = true;
            }
            if (prorec.data.isAutoAssembly == true && isAutoGenerateDO && (this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId)) {
                assemblyproductflag = false;
            }
//            /* check negative stock for SO/SI
//             * if SI is not linked with DO check negative stock
//             **/
////            if (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || (this.moduleid == Wtf.Acc_Invoice_ModuleId && Wtf.getCmp(this.parentCmpID).fromLinkCombo.getValue() == "")) {
//            if ((this.moduleid == Wtf.Acc_Invoice_ModuleId && Wtf.getCmp(this.parentCmpID).fromLinkCombo.getValue() == "")) {
//                var quantity = 0;
//                var availableQuantity = 0;
//                if (prorec.data.quantity != undefined && prorec.data.quantity !== "") {
//                    availableQuantity = prorec.data.quantity;
//                }
//                if (useStoreRec && prorec.data.availablequantity != undefined && prorec.data.availablequantity != "") {
//                    availableQuantity = prorec.data.availablequantity;
//                }
//                if (prorec.data.type != 'Service' && prorec.data.type != 'Non-Inventory Part') {
//                    if (this.isOrder) {
//                        if (prorec.data.socount != undefined && prorec.data.socount !== "") {
//                            availableQuantity = parseFloat(availableQuantity - prorec.data.socount);
//                        }
//                    } else if (this.isInvoice) {
//                        if (prorec.data.sicount != undefined && prorec.data.sicount !== "") {
//                            availableQuantity = parseFloat(availableQuantity - prorec.data.sicount);
//                        }
//                    }
//                }
//                var copyquantity=0;
//                this.store.each(function (rec) {
//                    if (rec.data.productid == obj.value) {
//                        var ind = this.store.indexOf(rec);
//                        if (ind != -1) {
//                            copyquantity = copyquantity + rec.data.copyquantity;
//                            if (ind != obj.row) {
//                                quantity = quantity + (rec.data.quantity);
//                            }
//                        }
//                    }
//                }, this);
//                
//                if(this.editTransaction && !this.copyInv){
//                   availableQuantity= parseFloat(availableQuantity + copyquantity);
//                }
//                if (availableQuantity < quantity && prorec.data.type != 'Service' && prorec.data.type != 'Non-Inventory Part'&&assemblyproductflag) {
//                    if (Wtf.account.companyAccountPref.negativestock == 1) { // Block case
//                        var msg = this.isOrder ? WtfGlobal.getLocaleText("acc.field.QuantitygiveninSOareexceedingthequantityavailable") : WtfGlobal.getLocaleText("acc.field.QuantitygiveninCS/CIareexceedingthequantityavailable");
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),msg+' ' + WtfGlobal.getLocaleText("acc.nee.54") + ' ' + prorec.data.productname +" "+ WtfGlobal.getLocaleText("acc.field.is") + availableQuantity + '. ' +WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed") ], 2);
//                        obj.cancel = true;
//                    } else if (Wtf.account.companyAccountPref.negativestock == 2) {     // Warn Case
//                        var msg = this.isOrder ? WtfGlobal.getLocaleText("acc.field.QuantitygiveninSOareexceedingthequantityavailableDoyouwish") : WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableDoyouwish");
//                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), msg + '</center>', function (btn) {
//                            if (btn == "yes") {
//                                obj.cancel = false;
//                            } else {
//                                rec.set("quantity", obj.originalValue);
////                                rec.set("baseuomquantity", obj.originalValue * obj.record.data['baseuomrate']);
//                                obj.cancel = true;
//                                return false;
//                            }
//                        }, this);
//                    }
//                }
//            }
            //index=this.priceStore.find('productid',obj.value)
            //rec=this.priceStore.getAt(index);
             if(Wtf.account.companyAccountPref.isLineLevelTermFlag && this.moduleid != Wtf.Acc_Purchase_Requisition_ModuleId  && this.moduleid != Wtf.Acc_RFQ_ModuleId){
                if(prorec.data['LineTermdetails'] != undefined  && prorec.data['LineTermdetails'] != ""){
                    var termStore = this.getTaxJsonOfIndia(prorec);
                    rec.set('LineTermdetails',Wtf.encode(termStore));
                }                
            }
            
            if(this.editTransaction){ //In Edit Case Check product quantity is greater than available quantity when selecting product                
                var availableQuantity = prorec.data.quantity;   
                if(useStoreRec){
                    availableQuantity = prorec.data.availablequantity;
                }
                var copyquantity = 0;                    
                this.store.each(function(rec){
                    if(rec.data.productid == prorec.data.productid){
                        if(rec.data.copyquantity!=undefined && rec.data.copyquantity!=undefined)
                            copyquantity = copyquantity + (rec.data.copyquantity*rec.data.baseuomrate);                            
                    }
                },this);                
                availableQuantity = availableQuantity + copyquantity;                                
                if(!Wtf.account.companyAccountPref.withinvupdate&&this.isCustomer&&this.store.find("productid",obj.value)>-1 && prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation&&!this.isOrder){
                    var quantity = 0;                 
                    this.store.each(function(rec){
                        if(rec.data.productid == obj.value){
                            var ind=this.store.indexOf(rec);
                            if(ind!=-1){
                                if(ind!=obj.row){
                                    quantity = quantity + (rec.data.quantity*rec.data.baseuomrate);
                                }
                            }     
                        }
                    },this);
                    quantity = quantity + (proqty*obj.record.data['baseuomrate']);  
                    
                    if(availableQuantity<quantity&&assemblyproductflag){
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+' is '+availableQuantity], 2);
//                        obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCS/CIareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+WtfGlobal.getLocaleText("acc.field.is")+availableQuantity+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",obj.originalValue);
                                  /*For SATS*/
                                  if(SATSCOMPANY_ID==companyid){
                                      rec.set("showquantity",obj.originalValue);	
                                  }
                                  /**********/
                                  rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                        }
                    }
                //WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.76")+" "+rec.data['productname']], 2);
                //obj.cancel=true;                
                }else if(!Wtf.account.companyAccountPref.withinvupdate&&this.isCustomer&&availableQuantity<(proqty*obj.record.data['baseuomrate'])&&prorec.data.type!='Service'&&!this.isQuotation&&!this.isOrder){
                    this.isValidEdit = false;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+WtfGlobal.getLocaleText("acc.field.is")+availableQuantity], 2);
                    obj.cancel=true;
                } //for new cash sales but not in if or else in this else loop and for edit no case satifies
            }else{ //New transaction case... In normal Case Check product quantity is greater than available quantity when selecting product
                //
                if(!Wtf.account.companyAccountPref.withinvupdate&&this.isCustomer&&this.store.find("productid",obj.value)>-1 && prorec.data.type!="Service" && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation&&!this.isOrder){
                    var quantity = 0;                 
                    this.store.each(function(rec){
                        if(rec.data.productid == obj.value){
                            var ind=this.store.indexOf(rec);
                            if(ind!=-1){
                                if(ind!=obj.row){                                    
                                    quantity = quantity + (rec.data.quantity*rec.data.baseuomrate);
                                }
                            }     
                        }
                    },this);
                    quantity = quantity + (proqty*obj.record.data['baseuomrate']);
                    quantity = quantity + proqty;
                    if(prorec.data['quantity']<quantity &&assemblyproductflag){
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+' is '+prorec.data['quantity']], 2);
//                        obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCS/CIareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+' is '+prorec.data['quantity']+'<br><br><center>So you cannot proceed ?</center>'], 2);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIaretheexceedingquantityavailableinstock")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",obj.originalValue);
                                  /*For SATS*/
                                  if(SATSCOMPANY_ID==companyid){
                                      rec.set("showquantity",obj.originalValue);
                                  }
                                  /**********/
                                  rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                        }
                    }
                //WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.76")+" "+rec.data['productname']], 2);
                //obj.cancel=true;
                //for Invoice and Cash sales in  with Inventory And Without trading Flow  
                }else if(!Wtf.account.companyAccountPref.withinvupdate&&this.isCustomer&&prorec.data['quantity']<(proqty*obj.record.data['baseuomrate'])&&prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation&&!this.isOrder &&assemblyproductflag){
                    this.isValidEdit = false;
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+' is '+prorec.data['quantity']], 2);
//                    obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCS/CIareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+WtfGlobal.getLocaleText("acc.field.is")+prorec.data['quantity']+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",obj.originalValue);
                                  /*For SATS*/
                                  if(SATSCOMPANY_ID==companyid){
                                      rec.set("showquantity",obj.originalValue);
                                  }
                                  /**********/
                                  rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                        }
                }
//                else if( isAutoGenerateDO &&this.isCustomer&&prorec.data['quantity']<(proqty*obj.record.data['baseuomrate'])&&prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation&&!this.isOrder &&assemblyproductflag){
//                    this.isValidEdit = false;
////                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+' is '+prorec.data['quantity']], 2);
////                    obj.cancel=true;
//                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
//                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCSareexceedingthequantity")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+WtfGlobal.getLocaleText("acc.field.is")+prorec.data['quantity']+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
//                          obj.cancel=true;   
//                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
//                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCSareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn){
//                               if(btn=="yes"){
//                                   obj.cancel=false;
//                                }else{
//                                  rec.set("quantity",obj.originalValue);
//                                  rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
//                                  obj.cancel=true;
//                                  return false;
//                               }
//                            },this); 
//                        }
//                }
            }  
        //EDIT case in without trading flow and with inventory        
        }else if(lockQuantitySO || (!Wtf.account.companyAccountPref.withinvupdate&&this.isCustomer&&(obj.field=="quantity"||obj.field=="baseuomrate")&&obj.record.data['productid'].length>0&&!this.isQuotation&&!this.isOrder)){  
            if(obj.field=="quantity") {
                var originalQuantity = obj.originalValue;
                var newQuantity = obj.value;
                var originalBaseuomrate = obj.record.data['baseuomrate'];
                var newBaseuomrate = obj.record.data['baseuomrate'];
            } else if(obj.field=="baseuomrate") {
                var originalQuantity = proqty;
                var newQuantity = proqty;
                var originalBaseuomrate = obj.originalValue;
                var newBaseuomrate = obj.value;
            }
            prorec=this.productComboStore.getAt(this.productComboStore.find('productid',obj.record.data.productid));
            var useStoreRec=false;
                if(prorec==undefined || prorec==null){
                     prorec= rec;
                     useStoreRec=true;
            } 
            if (prorec.data.isAutoAssembly == true && isAutoGenerateDO && (this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId)) {
                assemblyproductflag = false;
            }
            if(prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part') {
                var availableQuantity = prorec.data.quantity;
                var lockquantity = prorec.data.lockquantity;
                  if(useStoreRec){
                    availableQuantity = prorec.data.availablequantity;
                    lockquantity = prorec.data.lockquantity; 
                }
                var quantity = 0;
                if(this.editTransaction){    //In Edit Case Check product quantity is greater than available quantity when selecting quantity                                  
                    var copyquantity = 0;                    
                    this.store.each(function(rec){
                        if(rec.data.productid == prorec.data.productid){
                            copyquantity = copyquantity + (rec.data.copyquantity*rec.data.baseuomrate);
                            var ind=this.store.indexOf(rec);
                            if(ind!=-1){
                                if(ind!=obj.row){                            
                                    quantity = quantity + (rec.data.quantity*rec.data.baseuomrate);
                                }   
                            }                            
                        }
                    },this);
                    quantity = quantity + (newQuantity*newBaseuomrate);
                    availableQuantity = availableQuantity + copyquantity;   
                    if(this.editTransaction&&!this.copyInv&&(availableQuantity-lockquantity) < quantity &&assemblyproductflag) {
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCS/CIareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+WtfGlobal.getLocaleText("acc.field.is")+availableQuantity+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",originalQuantity);
                                  /*For SATS*/
                                  if(SATSCOMPANY_ID==companyid){
                                      rec.set("showquantity",obj.originalValue);
                                  }
                                  /*********/
                                  rec.set("baseuomquantity",originalQuantity*originalBaseuomrate);
                                  rec.set("baseuomrate",originalBaseuomrate);
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                        }
                    } //for edit transaction in in without trading flow and with inventory
                } else {     //In normal Case Check product quantity is greater than available quantity when selecting quantity                                
                    this.store.each(function(rec){
                        if(rec.data.productid == prorec.data.productid){
                            var ind=this.store.indexOf(rec);
                            if(ind!=-1){
                                if(ind!=obj.row){
                                    quantity = quantity + (rec.data.quantity*rec.data.baseuomrate);
                                }
                            }                               
                        }
                    },this);
                    quantity = quantity + (newQuantity*newBaseuomrate);                    
                    if((!this.editTransaction||this.copyInv) &&  (availableQuantity-lockquantity) < quantity&&assemblyproductflag) {
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableSoyou")+'</center>'], 2);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",originalQuantity);
                                  /*For SATS*/
                                  if(SATSCOMPANY_ID==companyid){
                                      rec.set("showquantity",obj.originalValue);
                                  }
                                  /**********/
                                  rec.set("baseuomquantity",originalQuantity*originalBaseuomrate);
                                  rec.set("baseuomrate",originalBaseuomrate);                                  
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                        }
                    }
                    
                }
            }            
        } else if(isAutoGenerateDO &&this.isCustomer&&(obj.field=="quantity" || obj.field=="showquantity" || obj.field=="baseuomrate")&&obj.record.data['productid'].length>0&&!this.isQuotation&&!this.isOrder){
            if(obj.field=="quantity") {
                var originalQuantity = obj.originalValue;
                var newQuantity = obj.value;
                var originalBaseuomrate = obj.record.data['baseuomrate'];
                var newBaseuomrate = obj.record.data['baseuomrate'];
            } else if(obj.field=="baseuomrate") {
                var originalQuantity = proqty;
                var newQuantity = proqty;
                var originalBaseuomrate = obj.originalValue;
                var newBaseuomrate = obj.value;
            }
            prorec=this.productComboStore.getAt(this.productComboStore.find('productid',obj.record.data.productid));
            var useStoreRec=false;
            if(prorec==undefined || prorec==null){
                prorec= rec;
                useStoreRec=true;
            }            
            if (prorec.data.isAutoAssembly == true && isAutoGenerateDO && (this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId)) {//cash sales/sales invoice
                assemblyproductflag = false;
            }
            if (prorec.data.type != 'Service' && prorec.data.type != 'Non-Inventory Part') {
                var availableQuantity = prorec.data.quantity;
                if (useStoreRec && prorec != undefined && prorec != null && prorec.data != undefined && prorec.data.availablequantity != undefined && prorec.data.availablequantity != null) {
                    availableQuantity = prorec.data.availablequantity;
                } else if (useStoreRec && prorec != undefined && prorec != null && prorec.data != undefined && prorec.data.availableQtyInSelectedUOM != undefined && prorec.data.availableQtyInSelectedUOM != null) {
                    availableQuantity = prorec.data.availableQtyInSelectedUOM;
                }
                this.sicount=prorec.data.sicount;
                if(this.productPOSOCountStore!=undefined && this.productPOSOCountStore!=null){
                    var sosountRec = WtfGlobal.searchRecord(this.productPOSOCountStore, prorec.data.productid, 'productid');
                    if (sosountRec != undefined) {
                        this.socount=sosountRec.data.socount
                        this.sicount=sosountRec.data.sicount;
                    }
                }   
                
                if (this.sicount != undefined && this.sicount !== "" && this.moduleid == Wtf.Acc_Invoice_ModuleId) {
                    if(Wtf.account.companyAccountPref.negativeStockFormulaSI==1){
                        /*
                                 * Apply Formula for consider Outstanding SI Qty
                                 */
                        availableQuantity = parseFloat(availableQuantity - this.sicount);
                    }
                }
                var quantity = 0;
                if(this.editTransaction){    //In Edit Case Check product quantity is greater than available quantity when selecting quantity                                  
                    var copyquantity = 0;                    
                    this.store.each(function(rec){
                        if(rec.data.productid == prorec.data.productid){
                            copyquantity = copyquantity + (rec.data.copyquantity*rec.data.baseuomrate);
                            var ind=this.store.indexOf(rec);
                            if(ind!=-1){ 
                                if(ind!=obj.row){                            
                                    quantity = quantity + (rec.data.quantity*rec.data.baseuomrate);
                                }   
                            }
                        }
                    },this);
                    quantity = quantity + (newQuantity*newBaseuomrate);
                    availableQuantity = availableQuantity + copyquantity;   
                    if(this.editTransaction&&!this.copyInv&&availableQuantity < quantity &&assemblyproductflag) {
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' is '+availableQuantity], 2);
//                        obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativeStockSICS==1){ // Block case
                          var msg = this.isOrder ? WtfGlobal.getLocaleText("acc.field.QuantitygiveninSOareexceedingthequantityavailable") : WtfGlobal.getLocaleText("acc.field.QuantitygiveninCS/CIareexceedingthequantityavailable");
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),msg+' '+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' is '+availableQuantity+'. ' +WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed") ], 2);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativeStockSICS==2){     // Warn Case
                          Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",originalQuantity);
                                  /*For SATS*/
                                  if(SATSCOMPANY_ID==companyid){
                                      rec.set("showquantity",obj.originalValue);	
                                  }
                                  /**********/
                                  rec.set("baseuomquantity",originalQuantity*originalBaseuomrate);
                                  rec.set("baseuomrate",originalBaseuomrate);
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                        }
                    } //for edit transaction in in without trading flow and with inventory
                } else {     //In normal Case Check product quantity is greater than available quantity when selecting quantity                                
                    this.store.each(function(rec){
                        if(rec.data.productid == prorec.data.productid){
                            var ind=this.store.indexOf(rec);
                            if(ind!=-1){
                                if(ind!=obj.row){
                                    quantity = quantity + (rec.data.quantity*rec.data.baseuomrate);
                                }
                            }                               
                        }
                    },this);
                    quantity = quantity + (newQuantity*newBaseuomrate);                    
                    if((!this.editTransaction||this.copyInv) &&  availableQuantity < quantity &&assemblyproductflag) {
                        if(Wtf.account.companyAccountPref.negativeStockSICS==1 || (isAutoGenerateDO && Wtf.account.companyAccountPref.negativestock==1)){ // Block case
                          var msg = this.isOrder ? WtfGlobal.getLocaleText("acc.field.QuantitygiveninSOareexceedingthequantityavailable") : WtfGlobal.getLocaleText("acc.field.QuantitygiveninCS/CIareexceedingthequantityavailable");
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),msg+' ' +WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed") ], 2);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativeStockSICS==2){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCSareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",originalQuantity);
                                  /*For SATS*/
                                  if(SATSCOMPANY_ID==companyid){
                                      rec.set("showquantity",obj.originalValue);
                                  }
                                  /**********/
                                  rec.set("baseuomquantity",originalQuantity*originalBaseuomrate);
                                  rec.set("baseuomrate",originalBaseuomrate);
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                        }
                        
                    }
                    
                }
            }                    
        }else if(obj.field=="vendorid" && obj.value!=null){
            var index=WtfGlobal.searchRecordIndex(Wtf.vendorAccStore, obj.value, 'accid');
            var vendorrec= null;
            if(index>=0){
                vendorrec = Wtf.vendorAccStore.getAt(index); 
                if(vendorrec!=null){
                    rec.data.vendorcurrencysymbol = vendorrec.data.currencysymbol;
                    rec.data.vendorcurrencyid = vendorrec.data.currencyid;
                    this.fireEvent("vendorselect", vendorrec , rec);
                } 
            }
        }else if(obj.field=="vendorunitcost" && obj.value!=null && rec.data.baseuomrate!=null && rec.data.quantity!=null && rec.data.quantity!=""){
            if(rec.data.vendorcurrexchangerate!=null){
                var quantity = rec.data.quantity * rec.data.baseuomrate ;
                var totalCost = quantity * obj.value * rec.data.vendorcurrexchangerate;
                rec.data.totalcost = totalCost              
            }
        }else if (obj.field == "quantity" && this.isCustomer && ((this.isOrder && this.moduleid == Wtf.Acc_Sales_Order_ModuleId) ||this.moduleid == Wtf.Acc_Invoice_ModuleId|| (this.isInvoice && Wtf.getCmp(this.parentCmpID).fromLinkCombo.getValue() == "") || this.moduleid==Wtf.Acc_Cash_Sales_ModuleId)) {
            /* check negative stock for SO/SI
             * if SI is not linked with DO check negative stock
             **/
            var quantity = obj.value;
            var productid = obj.record.data['productid'];
//            var index = this.productComboStore.findBy(function (rec) {
//                if (rec.data.productid == productid)
//                    return true;
//                else
//                    return false;
//            });
//            var prorec = this.productComboStore.getAt(index);
            var prorec = WtfGlobal.searchRecord(this.productComboStore,productid, 'productid');
            var useStoreRec = false;
            if (prorec == undefined || prorec==null) {
                prorec = obj.record;
                useStoreRec = true;
            }
            if (prorec.data.isAutoAssembly == true && isAutoGenerateDO && (this.moduleid == Wtf.Acc_Invoice_ModuleId  || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId)) {
                assemblyproductflag = false;
            }
           if (prorec.data.type != 'Service' && prorec.data.type != 'Non-Inventory Part') {
            var availableQuantity = 0;
            if(prorec!=undefined && prorec.data.quantity!=undefined && prorec.data.quantity!==""){
                availableQuantity=prorec.data.quantity;
            }
                if (useStoreRec && prorec.data.availablequantity != undefined && prorec.data.availablequantity != "") {
                    availableQuantity = prorec.data.availablequantity;
                } else if (useStoreRec && prorec != undefined && prorec != null && prorec.data != undefined && prorec.data.availableQtyInSelectedUOM != undefined && prorec.data.availableQtyInSelectedUOM != null) {
                    availableQuantity = prorec.data.availableQtyInSelectedUOM;
                }
                var copyquantity = 0;

                this.store.each(function(rec) {
                    if (rec.data.productid === productid) {
                        var ind = this.store.indexOf(rec);
                        if (ind != -1) {
                            copyquantity = copyquantity + rec.data.copyquantity;
                            if (ind != obj.row) {
                                quantity = quantity + (rec.data.quantity);
                            }
                        }
                    }
                }, this);
                if (this.editTransaction && !this.copyInv) {
                    availableQuantity = parseFloat(availableQuantity + copyquantity);
                }
                this.sicount=prorec.data.sicount;
                this.socount=prorec.data.socount;
                if ((this.moduleid == Wtf.Acc_Sales_Order_ModuleId && Wtf.account.companyAccountPref.negativeStockSO == 1) ||((this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId) && Wtf.account.companyAccountPref.negativeStockSICS == 1)) { // Block case
                //getting updated socount & sicount
                if(this.productPOSOCountStore!=undefined && this.productPOSOCountStore!=null){
                        var sosountRec = WtfGlobal.searchRecord(this.productPOSOCountStore, prorec.data.productid, 'productid');
                        if (sosountRec != undefined) {
                            this.socount=sosountRec.data.socount
                            this.sicount=sosountRec.data.sicount;
                        }
                    }
                }
                
            if (this.isOrder) {
                if (this.socount != undefined && this.socount !== "") {
                    if(Wtf.account.companyAccountPref.negativeStockFormulaSO==1){
                        /*
                         * Apply Formula for consider Outstanding SO Qty
                         */
                        availableQuantity = parseFloat(availableQuantity - this.socount);
                    }
                }
            } else if ((this.moduleid == Wtf.Acc_Invoice_ModuleId  || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId)) {
                if (this.sicount != undefined && this.sicount !== "") {
                    if(Wtf.account.companyAccountPref.negativeStockFormulaSI==1){
                        /*
                         * Apply Formula for consider Outstanding SI Qty
                         */
                        availableQuantity = parseFloat(availableQuantity - this.sicount);
                    }
                }
            }
            var showMinMaxWarnMsg = false;
//            if(Wtf.account.companyAccountPref.isMinMaxOrdering && this.moduleid == Wtf.Acc_Sales_Order_ModuleId && (prorec.data.minorderingquantity > quantity || quantity > prorec.data.maxorderingquantity)){
//                    /*
//                 *Check to Show only one warn message out of min/max ordering qty and negative stock Warn Msg.
//                 *If we are showing min/max ordering qty warn message then hiding negative stock warn message.
//                 *Please refer ERP-31404/SDP-7203 for more reference.
//                 **/
//                    showMinMaxWarnMsg = true;
//                }
         
            if (availableQuantity < quantity && assemblyproductflag) {
                if(availableQuantity=="") {
                    availableQuantity=0;   //In Prompt Message, it shows empty space instead of 0
                }    
                if ((this.moduleid == Wtf.Acc_Sales_Order_ModuleId && Wtf.account.companyAccountPref.negativeStockSO == 1 && !prorec.data.isAutoAssembly) ||(((this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId) && !prorec.data.isAutoAssembly) && Wtf.account.companyAccountPref.negativeStockSICS == 1) || ((isAutoGenerateDO && Wtf.account.companyAccountPref.negativestock==1))) { // Block case
                    var msg = this.isOrder ? WtfGlobal.getLocaleText("acc.field.QuantitygiveninSOareexceedingthequantityavailable") : WtfGlobal.getLocaleText("acc.field.QuantitygiveninCS/CIareexceedingthequantityavailable");
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),msg +' '+ WtfGlobal.getLocaleText("acc.nee.54") + ' ' + prorec.data.productname +" "+ WtfGlobal.getLocaleText("acc.field.is") + availableQuantity +'. ' +WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed") ], 2);
                    obj.cancel = true;
                } else if (((this.moduleid == Wtf.Acc_Sales_Order_ModuleId && Wtf.account.companyAccountPref.negativeStockSO == 2)) ||((this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId) && Wtf.account.companyAccountPref.negativeStockSICS == 2)) {     // Warn Case
                    var msg = this.isOrder ? WtfGlobal.getLocaleText("acc.field.QuantitygiveninSOareexceedingthequantityavailableDoyouwish") : WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableDoyouwish");
                        if (!showMinMaxWarnMsg) {
                            if (!this.isPriceListBand) {//In case of Price List Band ,we are not doing disable quantity if entered quantity exceeded from available quantity
//                                this.transQuantity.disable();
                            }
                            this.showWaringMsg = false;
                        }
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), msg + '</center>', function (btn) {
                        if (btn == "yes") {
//                            this.transQuantity.enable();
//                            this.transQuantity.setValue("");
                            obj.cancel = false;
                        } else {
//                            this.transQuantity.enable();
//                            this.transQuantity.setValue("");
                            rec.set("quantity", obj.originalValue);
                            rec.set("baseuomquantity", obj.originalValue * obj.record.data['baseuomrate']);
                            this.fireEvent('datachanged',this);
                            obj.cancel = true;
                            return false;
                        }
                    }, this);
                }
            }
        }
        } else if (obj.field == "prtaxid") {
            //SDP-15131
            if (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && this.parentObj.capitalGoodsAcquired && this.parentObj.capitalGoodsAcquired.getValue()) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.cgamsg")], 2);
                obj.cancel = true;
                return false;
            }
        }
        if(this.isNote){         
            if(obj.field=="typeid"&&(obj.value==0)){//Discount
                rec.set('remquantity',0);
                rec.set('discamount',0);
            }

            if(obj.field=="remquantity"){//Discount
                if(rec.data['typeid']==0){
                    obj.cancel=true;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.YoucannotenterquantitywhenDiscountnotetypeisselectedYouneed") ], 2);
                    rec.set('remquantity',0);
                    rec.set('discamount',0);
                }
                else{
                    //rec=this.store.getAt(this.store.find('rowid',obj.record.data['rowid']));
                    if(rec.data['remainingquantity']<obj.value){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productname']+ ' is '+rec.data.remainingquantity], 2);
                        obj.cancel=true;
                        rec.set('remquantity',0);
                        rec.set('discamount',0);
                    }else{
                        var qty = obj.value;
                        var rate = rec.data['rate'];
                        var prDiscount = rec.data['prdiscount'];
                        var prTax = rec.data['prtaxpercent'];
                        //To do - Need to check quantity checks for multi UOM change
                        var prTaxAmount = rec.data['taxamount'];
                        var amt = qty * rate;
                        if(rec.data['partamount']!= 0){
                            amt = amt * (rec.data['partamount']/100);
                        }
                        if(prDiscount > 0) {
                            if(rec.data['discountispercent'] == 1){
                                amt = amt - ((amt * prDiscount) / 100);
                            } else {
                                amt = amt - prDiscount;
                            }
                        }
                            
                        if(prTax > 0)
                            amt = amt + (prTaxAmount);//amt = amt + ((amt * prTax) / 100);
                        rec.set('discamount',amt);
                    }
                }
            }
            if(obj.field=="typeid"){
                //rec=this.store.getAt(this.store.find('productid',obj.record.data['productid']));
                if(rec.data['typeid']==0){//Discount
                    rec.set('remquantity',0);
                    rec.set('discamount',0);
                }
            }
            if(obj.field=="discamount"){
                if(rec.data['orignalamount']<obj.value){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Remainingamountfortheselectedproductis")+WtfGlobal.getCurrencySymbol()+" "+(rec.data['amount'])], 2);
                    
                    obj.cancel=true;
                    rec.set('discamount',0);
                }
            }
        }
    },
    populateDimensionValueingrid: function (rec) {
        WtfGlobal.populateDimensionValueingrid(this.moduleid, rec, this);
    },
    addBlankRow:function(){
        var Record = this.store.reader.recordType,f = Record.prototype.fields, fi = f.items, fl = f.length;
        var values = {},blankObj={};
        for(var j = 0;j < fl;j++){
            f = fi[j];
            if(f.name!='rowid') {
                blankObj[f.name]='';
                if(!Wtf.isEmpty(f.defValue))
                    blankObj[f.name]=f.convert((typeof f.defValue == "function"?f.defValue.call():f.defValue));
            }
                }
        var newrec = new Record(blankObj);
        if(this.symbol!=undefined)
        newrec.data.currencysymbol=this.symbol;
        this.store.add(newrec);
        this.getView().refresh();
    },   
    /*For SATS*/
    addSATSSubProducts:function(obj){
        var Record = this.store.reader.recordType,f = Record.prototype.fields, fi = f.items, fl = f.length;
        var values = {};
        this.recCnt=0;
        var isSubproduct=false;
        for(var storeitr=0;storeitr<this.productComboStore.getCount();storeitr++){
            var blankObj={};
            var prorec=this.productComboStore.getAt(storeitr); 
            if(prorec.data.parentid==obj.record.data["productid"]){ 
                isSubproduct=true;
                for(var j = 0; j < fl; j++){
                    f = fi[j];
                    if(f.name!='rowid') {
                        blankObj[f.name]='';
                        if(!Wtf.isEmpty(f.defValue))
                            blankObj[f.name]=f.convert((typeof f.defValue == "function"?f.defValue.call():f.defValue));
                        else if(prorec.data[f.name] !=undefined)  
                            blankObj[f.name]=prorec.data[f.name];
                    }
                }
                blankObj["issubproduct"]=true;
                 if(this.isCustomer)
                    blankObj["rate"]= (prorec.data.saleprice=="")?0:prorec.data.saleprice;
                else
                    blankObj["rate"]= (prorec.data.purchaseprice=="")?0:prorec.data.purchaseprice;
                       
                blankObj["issubproduct"]=true;
                var newrec = new Record(blankObj);
                this.store.insert(obj.grid.store.getCount()-1,newrec);
                this.recCnt++;
                this.fireEvent("productselect", prorec.data.productid);
                this.fireEvent('datachanged',this);
            //                  for(var storeitr1=0;storeitr1<100000;storeitr1++){
            //                      
            //                  }
                  
               
            }
        }
        
        this.store.each(function(rec) {
            if (rec.data.parentid !== "" && rec.data.parentid !== undefined) {
                Wtf.Ajax.requestEx({
                    url: "ACCProductCMN/getIndividualProductPrice.do",
                    params: {
                        uomschematypeid: rec.data.uomschematypeid,
                        startdate: WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true)),
                        enddate: WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false)),
                        currentuomid: obj.value,
                        carryin: (this.isCustomer) ? false : true,
                        productid: rec.data.productid,
                        affecteduser: this.affecteduser,
                        forCurrency: Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency : "",
                        currency: this.parentObj.Currency.getValue(),
                        transactiondate: WtfGlobal.convertToGenericDate(this.billDate),
                        skipRichTextArea:true
                    }
                }, this, function(response) {
                    var datewiseprice = response.data[0].price;
                    rec.set("rate", datewiseprice);
                 }, function() {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleasereselectproduct")], 2);
                });
            }
        }, this);

        if(isSubproduct)
        {
            obj.record.set("quantity",0);
            obj.record.set("showquantity",0); 
            obj.record.set("rate",0); 
            obj.record.set("isparentproduct",true); 
        }
    },
    /**********/
    updateRow:function(obj){
        this.tempSOPOLinkFlag = this.soLinkFlag; //ERP-11359
        Wtf.uomStore.clearFilter();
        if(obj!=null){
            this.productComboStore.clearFilter(); // Issue 22189  
            var rec=obj.record;
            var rowRateIncludingGstAmountIndex=this.getColumnModel().findColumnIndex("rateIncludingGst");   //instead of getIndexById() used findColumnIndex based on dataindex, refer ticket ERP-17718
            /**
             * if islinkingFlag true then price for that product will not be refresh or recalulate
             * only in linking case of any document. 
             */
            if (Wtf.isTDSApplicable) {// TDS assessable amount is same as line total amount
                obj.record.set("tdsAssessableAmount", obj.record.data.amount);
            }
            if (this.isEdit && this.parentObj != undefined && this.parentObj.PO.getValue() != undefined && this.parentObj.PO.getValue() != "") {
                rec.set('islinkingFlag', true);
            }
            var islinkingFlag = (rec.data.islinkingFlag != undefined && rec.data.islinkingFlag != "" )? rec.data.islinkingFlag : false;
            var quantity = obj.record.get("quantity");
            var isAutoGenerateDO=false;
            /**
             * oldunitpriceFlag- used to set the older unit price saved manually during save document 
             * i.e not to set the price band and volume discount price.
             */
            var oldunitpriceFlag=(rec.data !=undefined &&rec.data.oldunitpriceFlag!=undefined && rec.data.oldunitpriceFlag!="")?rec.data.oldunitpriceFlag:false;
            if(Wtf.getCmp(this.parentCmpID) != undefined  && Wtf.getCmp(this.parentCmpID).autoGenerateDO != undefined ){
                if(Wtf.getCmp(this.parentCmpID).autoGenerateDO.checked == true){
                    isAutoGenerateDO=true;
                }
            }

            /*----Code is written for adding product from window at line level----------*/
            if (obj.isAddProductsFromWindow != undefined && obj.isAddProductsFromWindow && CompanyPreferenceChecks.mapTaxesAtProductLevel() && this.moduleid != Wtf.Acc_Purchase_Requisition_ModuleId) {

                /* -------------Set Product tax if Product is mapped with any tax--------------- */              
                              
                   this.showMappedProductTax(rec);                     

            }
            
            //Clear custom combo store on type ahead
            if (obj.field.indexOf('Custom') != -1) {
                    for (var k = 0; k < obj.grid.colModel.config.length; k++) {
                        if (obj.grid.colModel.config[k].editor && obj.grid.colModel.config[k].editor.field.store && obj.grid.colModel.config[k].dataIndex.indexOf('Custom') != -1) {
                            var store = obj.grid.colModel.config[k].editor.field.store;
                            store.clearFilter();
                        }
                    }
            }
            quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?' ':quantity;
            /**
             * if  Amend fuctionalityuser has activated for that user and purchase should 
             * be greater that sales price,If not then system shows pop up msg.
             */
            //ERP-40390
            var includingGst=false;
            if(this.parentObj!=undefined && this.parentObj.includingGST!=undefined && this.parentObj.includingGST.getValue()){
                includingGst=this.parentObj.includingGST.getValue();
            }
            if ((obj.field == "rate"||obj.field == "rateIncludingGst") && this.isCustomer && Wtf.productPriceEditPerm.priceEditPerm.BlockAmendingPrice && this.getamendprice(rec,obj,false,includingGst)) {
                return;
            }
                
            if(obj.field=="prdiscount" && (rec.data.discountispercent == 1) && obj.value >100){
                
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Discountcannotbegreaterthan100")], 2);
                    rec.set("prdiscount",0);
            } else {
                /*For SATS*/
                if(SATSCOMPANY_ID==companyid){
                    if(obj.field=="showquantity")
                        rec.set("quantity",obj.record.data.showquantity)
                    //Added new rate according to Water tarrif
                    var productComboIndex = this.productComboStore.find('productid',obj.record.data.productid); 
                    if(productComboIndex >=0){
                        prorec = this.productComboStore.getAt(productComboIndex);
                    }
                    if(prorec && Wtf.account.companyAccountPref.dependentField){
                        var custValue1=prorec.data.dependentTypeQty;
                        if(custValue1!=""){
                            Wtf.Ajax.requestEx({
                                url: "ACCGoodsReceipt/getMasterItemPriceFormulaPrice.do",
                                params: {
                                    productId: obj.record.data.productid,
                                    item:(custValue1!="")?obj.record.data.quantity:1,
                                    iscalculatefromqty:(custValue1!="")
                                    
                                }
                            }, this, function(response) {
                                var datewiseprice1=obj.record.data.rate;
                                for (var i = 0; i < response.data.length; i++) {
                                    var dataObj = response.data[i];
                                    if(dataObj.pricevalue>0){
                                        datewiseprice1=dataObj.pricevalue;
                                    }
                                    obj.record.set("rate", datewiseprice1);
                                    var taxamount = this.setTaxAmountAfterSelection(obj.record);
                                    obj.record.set("taxamount",taxamount);
                                    obj.record.set("isUserModifiedTaxAmount", false);
                                    this.fireEvent('datachanged',this);
                                }
                            } , function(){
                
                                });
                        }  
               
                    }
                }
                /**********/
                this.fireEvent('datachanged',this);
            }
            
            if(obj.field=="discountispercent" && obj.value == 1 && (rec.data.prdiscount > 100)){
                
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Percentdiscountcannotbegreaterthan100")], 2);
                    rec.set("discountispercent",0);
            } else {
                this.fireEvent('datachanged',this);
            }
            if(obj.field=="baseuomrate"){
                  if(this.isCustomer)
                    rec.set("changedQuantity",(rec.data.copybaseuomrate-obj.value)*((rec.data.copyquantity==="")?1:rec.data.copyquantity));
                  else
                    rec.set("changedQuantity",(obj.value-rec.data.copybaseuomrate)*((rec.data.copyquantity==="")?1:rec.data.copyquantity));
                
                  var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
                  var productuomid = "",datewiseprice=0,rate=0;
                  if(productComboIndex >=0){
                      prorec = this.productComboStore.getAt(productComboIndex);
                      productuomid = prorec.data.uomid;
                       if(this.UomSchemaType==Wtf.UOMSchema){//for Schema type
                        Wtf.Ajax.requestEx({
                            url:"ACCProductCMN/getIndividualProductPrice.do",
                            params:{
                                displayUoMid :prorec.data.displayUoMid,
                                uomschematypeid:prorec.data.uomschematypeid,
                                productId:prorec.data.productid,
                                startdate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true)),
                                enddate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false)),
                                currentuomid:obj.value,
                                carryin : (this.isCustomer)? false : true,
                                productid:prorec.data.productid,
                                affecteduser: this.affecteduser,
                                forCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency:"",
                                currency: this.parentObj.Currency.getValue(),
                                quantity: obj.record.data.quantity,
                                transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                                uomid:obj.record.data.uomid,
                                skipRichTextArea:true
                            }
                        }, this,function(response){
                            datewiseprice =response.data[0].price;
                            /**
                             * Checking if price is mapped to UOM if yes the setting the unit price which is set in db else converting it as rule set in UOM schema.
                             * ERM-389 / ERP-35140
                             */
                            var modifiedRate,isPriceMappedToUOM=false;
                            if (response.data[0].isPriceMappedToUOM != undefined && response.data[0].isPriceMappedToUOM != "") {
                                isPriceMappedToUOM = response.data[0].isPriceMappedToUOM;
                            }
                            modifiedRate=WtfGlobal.getIndividualProductPriceInMultiCurrency(rec,datewiseprice);
                            if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                                obj.record.set("baseuomquantity", quantity*obj.value);
                                if (prorec.data.displayUoMid != undefined && prorec.data.displayUoMid != "" && prorec.data.displayUoMid != null && displayuomvalue != 0) {
                                        obj.record.set("displayuomvalue", (quantity * obj.record.get("baseuomrate")) / obj.record.get("displayuomrate"));
                                } else {
                                    obj.record.set("displayuomvalue", "");
                                }
                                rate = getRoundofValueWithValues(modifiedRate*obj.value,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                                modifiedRate = getRoundofValueWithValues(modifiedRate,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                                obj.record.set("rate", isPriceMappedToUOM?modifiedRate:rate);
                            }else {
                                obj.record.set("baseuomrate", 1);
                            } 
                            if(Wtf.account.companyAccountPref.isMinMaxOrdering && this.moduleid == Wtf.Acc_Purchase_Order_ModuleId){
                                this.checkMinMaxOrderingQuantity(obj,prorec);
                            }
//                            WtfGlobal.setDefaultWarehouseLocation(obj, rec,false);
                            this.fireEvent('datachanged',this);
                        }, function(){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleasereselectproduct")], 2);
                            });
                    }
//                    if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
//                        obj.record.set("baseuomquantity", quantity*obj.value);
//                        obj.record.set("rate", modifiedRate*obj.value);
//                    } else {
//                        obj.record.set("baseuomrate", 1);
//                    }                      
//                    if(Wtf.account.companyAccountPref.isMinMaxOrdering && (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Purchase_Order_ModuleId)){
//                        this.checkMinMaxOrderingQuantity(obj,prorec);
//                    }
                }else{
                    productuomid = rec.data.baseuomid;
                    // getIndividualProductPrice is added because in edit case if changing conversion rate then price is not changing as per conversion rate.
                    Wtf.Ajax.requestEx({
                        url: "ACCProductCMN/getIndividualProductPrice.do",
                        params: {
                            productId: rec.data.productid,
                            startdate: WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true)),
                            enddate: WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false)),
                            carryin: (this.isCustomer)? false : true,
                            productid: rec.data.productid,
                            affecteduser: this.affecteduser,
                            forCurrency: Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency : "",
                            currency: this.parentObj.Currency.getValue(),
                            quantity: obj.record.data.quantity,
                            transactiondate: WtfGlobal.convertToGenericDate(this.billDate),
                            uomid:obj.record.data.uomid,
                            skipRichTextArea:true
                        }
                    }, this,function(response){
                        datewiseprice = response.data[0].price;
                        /**
                         * Checking if price is mapped to UOM if yes the setting the unit price which is set in db else converting it as rule set in UOM schema.
                         * ERM-389 / ERP-35140
                         */
                        var isPriceMappedToUOM = false;
                        if (response.data[0].isPriceMappedToUOM != undefined && response.data[0].isPriceMappedToUOM != "") {
                            isPriceMappedToUOM = response.data[0].isPriceMappedToUOM;
                        }
                        var modifiedRate = WtfGlobal.getIndividualProductPriceInMultiCurrency(rec,datewiseprice);
                        rate = getRoundofValueWithValues(modifiedRate * obj.value, Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                        modifiedRate = getRoundofValueWithValues(modifiedRate, Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                        if (obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")) {
                            obj.record.set("baseuomquantity", quantity*obj.value);
                            obj.record.set("rate", isPriceMappedToUOM ? modifiedRate : rate);
                        } else {
                            obj.record.set("baseuomrate", 1);
                        } 
                        if (Wtf.account.companyAccountPref.isMinMaxOrdering && this.moduleid == Wtf.Acc_Purchase_Order_ModuleId) {
                            this.checkMinMaxOrderingQuantity(obj,prorec);
                        }
                        WtfGlobal.setDefaultWarehouseLocation(obj, rec,false);
                        this.fireEvent('datachanged',this);
                    }, function(){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleasereselectproduct")], 2);
                    });
                    
//                    productuomid = rec.data.baseuomid;
//                    if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
//                        obj.record.set("baseuomquantity", quantity*obj.value);
//                    } else {
//                        obj.record.set("baseuomrate", 1);
//                    }
//                    if(Wtf.account.companyAccountPref.isMinMaxOrdering && (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Purchase_Order_ModuleId)){
//                        this.checkMinMaxOrderingQuantity(obj,prorec);
//                    }
//                    WtfGlobal.setDefaultWarehouseLocation(obj, rec,false);
                }
//                  this.fireEvent('datachanged',this);
            }
            if(obj.field=="uomid"){
                  var prorec = null;
                  var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
                  var productuomid = "";
                  if(productComboIndex >=0){
                      var baseuomrate =1,rateperuom=0,datewiseprice=0,displayuomrate1 = 1, displayuomvalue = 0,isPriceMappedToUOM=false;
                      prorec = this.productComboStore.getAt(productComboIndex);
                      productuomid = prorec.data.uomid;
                     //To do - Need to take rate from new window
//                      this.showPriceWindow.createDelegate(this,[rec, obj],true);
                        if(this.UomSchemaType==Wtf.UOMSchema){//for Schema type
                            var selectedUOMRec = WtfGlobal.searchRecord(Wtf.uomStore, obj.value, 'uomid');
                            var uomschemaid="";
                        if(productuomid != obj.value){  
                            uomschemaid=prorec.data.uomschematypeid;
                        }
                            obj.record.set("uomname", selectedUOMRec.data['uomname']);
//                            if(productuomid != obj.value){
                                obj.record.set("isAnotherUOMSelected", false);
                                Wtf.Ajax.requestEx({
                                url:"ACCProductCMN/getIndividualProductPrice.do",
                                params:{
                                    displayUoMid :prorec.data.displayUoMid,
                                    uomschematypeid:uomschemaid,
                                productId:rec.data.productid,
                                    startdate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true)),
                                    enddate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false)),
                                    currentuomid:obj.value,
                                    carryin : (this.isCustomer)? false : true,
                                productid:rec.data.productid,
                                    affecteduser: this.affecteduser,
                                    forCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency:"",
                                    currency: this.parentObj.Currency.getValue(),
                                    quantity: obj.record.data.quantity,
                                    transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                                    uomid: rec.data.uomid,
                                    pricingbandmaster:obj.record.data.pricingbandmasterid,
                                    moduleid:this.moduleid,
                                    skipRichTextArea:true
                                }
                                }, this,function(response){
                                    baseuomrate =response.data[0].baseuomrate;
                                    rateperuom =response.data[0].rateperuom;
                                    datewiseprice =response.data[0].price;
                                    this.defaultPrice = datewiseprice;
                                    /**
                                     * Checking if price is mapped to UOM if yes the setting the unit price which is set in db else converting it as rule set in UOM schema.
                                     * ERM-389 / ERP-35140
                                     */
                                    if(response.data[0].isPriceMappedToUOM!=undefined && response.data[0].isPriceMappedToUOM!=""){
                                        isPriceMappedToUOM=response.data[0].isPriceMappedToUOM;
                                    }
                                    var availableQtyInSelectedUOM =response.data[0].availableQtyInSelectedUOM;
                                    var pocountinselecteduom =response.data[0].pocountinselecteduom;
                                    var socountinselecteduom =response.data[0].socountinselecteduom;
                                    displayuomrate1 = response.data[0].displayuomrate;
                                    displayuomvalue = response.data[0].displayuomvalue;
                                    obj.record.set("baseuomrate", baseuomrate);
                                    obj.record.set("displayuomrate", displayuomrate1);
                                    if(prorec.data.displayUoMid != undefined && prorec.data.displayUoMid  != "" && prorec.data.displayUoMid != null && displayuomvalue != 0){
                                        obj.record.set("displayuomvalue", (displayuomrate1 == 1) ? quantity : (baseuomrate*quantity)/displayuomrate1);
                                    }else{
                                        obj.record.set("displayuomvalue","");
                                    }
                                    obj.record.set("baseuomquantity", quantity*baseuomrate);
                                    obj.record.set("availableQtyInSelectedUOM", availableQtyInSelectedUOM);
                                    obj.record.set("isAnotherUOMSelected", true);
                                    obj.record.set("pocountinselecteduom", pocountinselecteduom);
                                    obj.record.set("socountinselecteduom", socountinselecteduom);
                                   // obj.record.set("displayUoMName", displayUoMName1);
                            var modifiedRate = this.defaultPrice;
                            /**
                             * No need to change unit price in foreign currency if price already present in 
                             * foreign currency. 
                             */
                            if (this.isbandPriceNotAvailable != undefined && this.isbandPriceNotAvailable) {
                                modifiedRate = WtfGlobal.getIndividualProductPriceInMultiCurrency(rec, datewiseprice);
                            }
                            if(productuomid == obj.value){
                                obj.record.set("baseuomrate", 1);
                                obj.record.set("baseuomquantity", quantity*obj.record.get("baseuomrate"));
                                obj.record.set("rate",modifiedRate*baseuomrate); 
                                obj.record.set("rateIncludingGst", modifiedRate*baseuomrate);
                            }else{
                                obj.record.set("rate", isPriceMappedToUOM ? modifiedRate : modifiedRate * baseuomrate);
                                obj.record.set("rateIncludingGst", isPriceMappedToUOM ? modifiedRate : modifiedRate * baseuomrate);
                            }    
                            if (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden) {
                                    if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                                        this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                                    } else {
                                        this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                                    }
                            } else {
                                /*
                                 * Tax Calculated here when includingGST Check Will be OFF
                                 */
                                if (this.isGST) {
                                    this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                                } else {
                                    var taxamount = this.setTaxAmountAfterSelection(obj.record);
                                    obj.record.set("taxamount", taxamount);
                                    obj.record.set("isUserModifiedTaxAmount", false);
                                }
                            }
//                            if(baseuomrate!=1){
//                                obj.record.set("rate",modifiedRate*quantity*baseuomrate);
//                                obj.record.set("rateIncludingGst", modifiedRate*quantity*baseuomrate);
//                            }
//                                    if(modifiedRate !=0){ //temp check for not clearing original value
//                                        obj.record.set("rate", modifiedRate);
//                                        obj.record.set("rateIncludingGst", modifiedRate);
//                                    }  
                            WtfGlobal.setDefaultWarehouseLocation(obj, rec,false);
                            this.fireEvent('datachanged',this);
                              }, function(){
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleasereselectproduct")], 2);
                                });
//                            } else {
//                                obj.record.set("baseuomrate", 1);
//                                obj.record.set("baseuomquantity", quantity*obj.record.get("baseuomrate"));
//                                obj.record.set("rate",price*baseuomrate);
//                            }    
                         } else{ //for packeging UOM type                             
                           Wtf.Ajax.requestEx({
                                    url:"ACCProduct/getIndividualProductPrice.do",
                                    params:{
                                        productid:prorec.data.productid,
                                        affecteduser: this.affecteduser,
                                        forCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency:"",
                                        currency: this.parentObj.Currency.getValue(),
                                        quantity: obj.record.data.quantity,
                                        transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                                        carryin : (this.isCustomer)? false : true
                                    }
                              }, this,function(response){
                                  var datewiseprice =response.data[0].price;
                                  if(!Wtf.account.companyAccountPref.productPriceinMultipleCurrency){ //If product in Multiple currency is not set in account preferences
                                var rate=((obj.record==undefined||obj.record.data['currencyrate']==undefined||obj.record.data['currencyrate']=="")?1:obj.record.data['currencyrate']);
                                var oldcurrencyrate=((obj.record==undefined||obj.record.data['oldcurrencyrate']==undefined||obj.record.data['oldcurrencyrate']=="")?1:obj.record.data['oldcurrencyrate']);
                                var modifiedRate;
                                if(rate!=0.0)
                                    modifiedRate=getRoundofValueWithValues(((parseFloat(datewiseprice)*parseFloat(rate))/parseFloat(oldcurrencyrate)),Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                                    else
                                        modifiedRate=getRoundofValueWithValues((parseFloat(datewiseprice)/parseFloat(oldcurrencyrate)),Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                                }else{
                                    modifiedRate=datewiseprice;
                                }
                                    if( obj.value == prorec.data.caseuom){
                                        obj.record.set("baseuomquantity", (quantity)*(prorec.data.caseuomvalue));
                                        obj.record.set("baseuomrate", (prorec.data.caseuomvalue));
                                        obj.record.set("rate", modifiedRate*prorec.data.caseuomvalue);
                                    } else if(obj.value == prorec.data.inneruom) {
                                        obj.record.set("baseuomquantity", (quantity)*(prorec.data.inneruomvalue));
                                        obj.record.set("baseuomrate", (prorec.data.inneruomvalue));
                                        obj.record.set("rate", modifiedRate*prorec.data.inneruomvalue);
                                    } else {
                                        obj.record.set("baseuomrate", 1);
                                        obj.record.set("baseuomquantity", quantity*1);
                                        obj.record.set("rate", modifiedRate);
                                    }  
                                    WtfGlobal.setDefaultWarehouseLocation(obj, rec,false);
                                     this.fireEvent('datachanged',this);
                              }, function(){
                                  WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleasereselectproduct")], 2);
                            });    
                      } 
                }else{
                    productuomid = rec.data.baseuomid;
                    
                    var selectedUOMRec = WtfGlobal.searchRecord(Wtf.uomStore, obj.value, 'uomid');
                      
                    obj.record.set("uomname", selectedUOMRec.data['uomname']);
                    obj.record.set("isAnotherUOMSelected", false);
                      
                   
                    //To do - Need to take rate from new window
                    //                      this.showPriceWindow.createDelegate(this,[rec, obj],true);
                    var uomschemaid;
                    if(this.UomSchemaType==Wtf.UOMSchema){//for Schema type
                        if(productuomid != obj.value){  
                            uomschemaid=rec.data.uomschematypeid;
                        }
                        
                        Wtf.Ajax.requestEx({
                            url:"ACCProductCMN/getIndividualProductPrice.do",
                            params:{
                                displayUoMid :rec.data.displayUoMid,
                                uomschematypeid:uomschemaid,
                                productId: rec.data.productid,
                                startdate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true)),
                                enddate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false)),
                                currentuomid:obj.value,
                                carryin : (this.isCustomer)? false : true,
                                productid: rec.data.productid,
                                affecteduser: this.affecteduser,
                                forCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency:"",
                                currency: this.parentObj.Currency.getValue(),
                                quantity: obj.record.data.quantity,
                                transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                                uomid: rec.data.uomid,
                                skipRichTextArea:true
                            }
                        }, this,function(response){
                            baseuomrate =response.data[0].baseuomrate;
                            displayuomrate1 =response.data[0].displayuomrate;
                            rateperuom =response.data[0].rateperuom;
                            datewiseprice =response.data[0].price;

                            var availableQtyInSelectedUOM =response.data[0].availableQtyInSelectedUOM;
                            var pocountinselecteduom =response.data[0].pocountinselecteduom;
                            var socountinselecteduom =response.data[0].socountinselecteduom;
                            /**
                             * Checking if price is mapped to UOM if yes the setting the unit price which is set in db else converting it as rule set in UOM schema.
                             * ERM-389 / ERP-35140
                             */
                            var isPriceMappedToUOM = false;
                            if (response.data[0].isPriceMappedToUOM != undefined && response.data[0].isPriceMappedToUOM != "") {
                                isPriceMappedToUOM = response.data[0].isPriceMappedToUOM;
                            }
                            obj.record.set("baseuomrate", baseuomrate);
                            var displayUoMid = response.data[0].displayUoMid;
                            if (displayUoMid != undefined && displayUoMid != "" && displayUoMid != null)
                                obj.record.set("displayuomvalue", (displayuomrate1 == 1) ? quantity : (baseuomrate * quantity) / displayuomrate1);
                            else
                                obj.record.set("displayuomvalue", "");
                            obj.record.set("baseuomquantity", quantity*baseuomrate);
                            obj.record.set("availableQtyInSelectedUOM", availableQtyInSelectedUOM);
                            obj.record.set("isAnotherUOMSelected", true);
                            obj.record.set("pocountinselecteduom", pocountinselecteduom);
                            obj.record.set("socountinselecteduom", socountinselecteduom);
                            var modifiedRate;
                            
                            modifiedRate=WtfGlobal.getIndividualProductPriceInMultiCurrency(rec,datewiseprice);
                                    
                            if(productuomid == obj.value){
                                obj.record.set("baseuomrate", 1);
                                obj.record.set("baseuomquantity", quantity*obj.record.get("baseuomrate"));
                                obj.record.set("rate",modifiedRate*baseuomrate); 
                                obj.record.set("rateIncludingGst", modifiedRate*baseuomrate);
                            }else{
                                obj.record.set("rate", isPriceMappedToUOM ? modifiedRate : modifiedRate * baseuomrate);
                                obj.record.set("rateIncludingGst", isPriceMappedToUOM ? modifiedRate : modifiedRate * baseuomrate);
                            }
                            if(baseuomrate!=1){
                                obj.record.set("rate",isPriceMappedToUOM ? modifiedRate : modifiedRate*quantity*baseuomrate);
                                obj.record.set("rateIncludingGst", isPriceMappedToUOM ? modifiedRate : modifiedRate*quantity*baseuomrate);
                            }
                            WtfGlobal.setDefaultWarehouseLocation(obj, rec,false);
                            this.fireEvent('datachanged',this);
                        }, function(){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleasereselectproduct")], 2);
                            });
                    //                            } else {
                    //                                obj.record.set("baseuomrate", 1);
                    //                                obj.record.set("baseuomquantity", quantity*obj.record.get("baseuomrate"));
                    //                            }
                    } else {//for packeging UOM type
                             Wtf.Ajax.requestEx({
                                    url:"ACCProduct/getIndividualProductPrice.do",
                                    params:{
                                        productid:prorec.data.productid,
                                        affecteduser: this.affecteduser,
                                        forCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency:"",
                                        currency: this.parentObj.Currency.getValue(),
                                        quantity: obj.record.data.quantity,
                                        transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                                        carryin : (this.isCustomer)? false : true
                                    }
                              }, this,function(response){
                                  var datewiseprice =response.data[0].price;
                                  if(!Wtf.account.companyAccountPref.productPriceinMultipleCurrency){ //If product in Multiple currency is not set in account preferences
                                var rate=((obj.record==undefined||obj.record.data['currencyrate']==undefined||obj.record.data['currencyrate']=="")?1:obj.record.data['currencyrate']);
                                var oldcurrencyrate=((obj.record==undefined||obj.record.data['oldcurrencyrate']==undefined||obj.record.data['oldcurrencyrate']=="")?1:obj.record.data['oldcurrencyrate']);
                                var modifiedRate;
                                if(rate!=0.0)
                                    modifiedRate=getRoundofValueWithValues(((parseFloat(datewiseprice)*parseFloat(rate))/parseFloat(oldcurrencyrate)),Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                                    else
                                        modifiedRate=getRoundofValueWithValues((parseFloat(datewiseprice)/parseFloat(oldcurrencyrate)),Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                                }else{
                                    modifiedRate=datewiseprice;
                                }
                                    if( obj.value == prorec.data.caseuom){
                                        obj.record.set("baseuomquantity", (quantity)*(prorec.data.caseuomvalue));
                                        obj.record.set("baseuomrate", (prorec.data.caseuomvalue));
                                        obj.record.set("rate", modifiedRate*prorec.data.caseuomvalue);
                                    } else if(obj.value == prorec.data.inneruom) {
                                        obj.record.set("baseuomquantity", (quantity)*(prorec.data.inneruomvalue));
                                        obj.record.set("baseuomrate", (prorec.data.inneruomvalue));
                                        obj.record.set("rate", modifiedRate*prorec.data.inneruomvalue);
                                    } else {
                                        obj.record.set("baseuomrate", 1);
                                        obj.record.set("baseuomquantity", quantity*1);
                                        obj.record.set("rate", modifiedRate);
                                    }  
                                    WtfGlobal.setDefaultWarehouseLocation(obj, rec,false);
                                    this.fireEvent('datachanged',this);
                              }, function(){
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleasereselectproduct")], 2);
                            });    
                      } 
                  
                    
                }
//                  this.fireEvent('datachanged',this);
            }
            if(obj.field=="productid" || obj.field=="pid" || obj.field=="productname" ){
                var customFieldArr = GlobalColumnModelForProduct[this.moduleid];
                if(customFieldArr !=null && customFieldArr != undefined && !obj.linkflag){
                    for(var k=0;k<customFieldArr.length;k++){
                        rec.set(customFieldArr[k].fieldname,"");
                    }
                }
                if(this.isCustomer)
                    rec.set("changedQuantity",(quantity*(-1))*rec.data.baseuomrate);
                else
                    rec.set("changedQuantity",(quantity)*rec.data.baseuomrate);
                
                if(this.parentObj && this.parentObj.Currency != undefined){
                    this.forCurrency=this.parentObj.Currency.getValue();
                } 
                
                var productid = "";
                if(this.productOptimizedFlag==Wtf.Products_on_Submit){
                    productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.value.trim(), 'pid');
                    if(productComboIndex >=0){
                        prorec = this.productComboStore.getAt(productComboIndex);
                        productid=prorec.data.productid;
                        rec.set("productid",productid);
                    }
                } 
                //set inspection template id in product record
                productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.value.trim(), 'productid');
                if(productComboIndex >=0){
                    prorec = this.productComboStore.getAt(productComboIndex);
                    var it = prorec.data.inspectionTemplate;
                    rec.set("inspectionTemplate", it);
                }
                //Tax Calculation for India
                // Tax Calculation at line level terms
                if(Wtf.account.companyAccountPref.isLineLevelTermFlag && this.moduleid != Wtf.Acc_Purchase_Requisition_ModuleId  && this.moduleid != Wtf.Acc_RFQ_ModuleId){
                    var productComboIndex_INDIA = WtfGlobal.searchRecordIndex(this.productComboStore, obj.value.trim(), 'productid');
                    if(productComboIndex_INDIA == -1){
                        productComboIndex_INDIA = WtfGlobal.searchRecordIndex(this.productComboStore, obj.value.trim(), 'pid');
                    }
                    if(productComboIndex_INDIA >=0){
                        prorec = this.productComboStore.getAt(productComboIndex_INDIA);
                        if(prorec!=undefined){
                            rec.set('valuationType',prorec.data['valuationType']);
                            rec.set('valuationTypeVAT',prorec.data['valuationTypeVAT']);
                            rec.set('productMRP',prorec.data['productMRP']);
                            rec.set('reortingUOMExcise',prorec.data['reortingUOMExcise']);
                            rec.set('reortingUOMSchemaExcise',prorec.data['reortingUOMSchemaExcise']);
                            rec.set('reportingUOMVAT',prorec.data['reportingUOMVAT']);
                            rec.set('reportingUOMSchemaVAT',prorec.data['reportingUOMSchemaVAT']);
                            rec.set('recTermAmount',prorec.data['recTermAmount'] == "" || prorec.data['recTermAmount'] == undefined ? 0 : prorec.data['recTermAmount'] );
                            rec.set('OtherTermNonTaxableAmount',prorec.data['OtherTermNonTaxableAmount'] == "" || prorec.data['OtherTermNonTaxableAmount'] == undefined ? 0 : prorec.data['OtherTermNonTaxableAmount'] );
                            if(prorec.data['compairwithUOM'] != undefined  && prorec.data['compairwithUOM'] != ""){
                                rec.set('compairwithUOM',prorec.data['compairwithUOM']);
                            }else{
                                rec.set('compairwithUOM',1);
                            } 
                            if(prorec.data['compairwithUOMVAT'] != undefined  && prorec.data['compairwithUOMVAT'] != ""){
                                rec.set('compairwithUOMVAT',prorec.data['compairwithUOMVAT']);
                            }else{
                                rec.set('compairwithUOMVAT',1);
                            } 
                        }else{
                            rec.set('recTermAmount',0);
                            rec.set('OtherTermNonTaxableAmount',0);
                            rec.set('valuationType',"");
                            rec.set('valuationTypeVAT',"");
                            rec.set('compairwithUOM',1);
                            rec.set('compairwithUOMVAT',1);
                            rec.set('productMRP',0);
                        }
                        if(!this.isGST && prorec.data['LineTermdetails'] != undefined  && prorec.data['LineTermdetails'] != ""){
                            var termStore = this.getTaxJsonOfIndia(prorec);
                            rec.set('uncheckedTermdetails',prorec.data['uncheckedTermdetails']);
                            rec.set('dealerExciseTerms',prorec.data['dealerExciseTerms']);
                            if(this.parentObj && this.parentObj.includingGST && this.parentObj.includingGST.getValue() == true) {
                                this.getColumnModel().setRenderer(this.getColumnModel().findColumnIndex("amount"),WtfGlobal.withoutRateCurrencySymbol);
                                termStore = this.calculateTermLevelTaxesInclusive(termStore, rec);
                            }else {
                                this.getColumnModel().setRenderer(this.getColumnModel().findColumnIndex("amount"),this.calAmountWithoutExchangeRate.createDelegate(this));
                                termStore = this.calculateTermLevelTaxes(termStore, rec,undefined,true);
                            } 
                            rec.set('LineTermdetails',Wtf.encode(termStore)); //ERP-31291                            
                            this.updateTermDetails(); 
                        }else if (this.isGST) {
                            if (this.isModuleForAvalara) {
                                getTaxFromAvalaraAndUpdateGrid(this, obj);
                            } 
                             else {
                        /**
                         * ERP-32829 
                         * code for New GST 
                         */
                        var extraparams = {};
                        extraparams.isProductIDSelect = true;
                        
                            if (this.parentObj.isPurchasesTransaction && this.parentObj.purchaseFromURD) {
                                /*** If purchases is from Unregistered dealer ***/
                                if (this.parentObj.isRCMApplicableInPreferences && this.parentObj.GTAApplicable.getValue()) {
                                        this.parentObj.uniqueCase = Wtf.GSTCustVenStatus.APPLYGST;
                            } else {
                                        this.parentObj.uniqueCase = Wtf.GSTCustVenStatus.NOGST;
                            }
                        }
                            processGSTRequest(this.parentObj, this, prorec.data.productid, extraparams);
                                if (WtfGlobal.isIndiaCountryAndGSTApplied() && Wtf.isitcapplicable && prorec.data['itctype']!=undefined && prorec.data['itctype']!="" 
                                        && (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId)) {
                                    rec.set('itctype', prorec.data['itctype']);
                                }
                    }
                    }
                    }

                }            
                
                if (!(this.tempSOPOLinkFlag && Wtf.account.companyAccountPref.carryForwardPriceForCrossLinking)) { // If crosslinking and carryForwardPriceForCrossLinking 'true' then do not update Unit Price of transaction keep crosslinking Unit Price as it is.'
                    if(obj.individualproductprice!=undefined && obj.individualproductprice!=""){
                        var individualproductprice = obj.individualproductprice;
                        this.setIndividualProductPriceDetails(obj, rec, individualproductprice, quantity, rowRateIncludingGstAmountIndex);
                    }else{
                        Wtf.Ajax.requestEx({
                            url:"ACCProductCMN/getIndividualProductPrice.do",
                            params:{
                                productid:this.productOptimizedFlag==Wtf.Products_on_Submit?productid:obj.value,
                                affecteduser: this.affecteduser,
                                forCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency:"",
                                currency: this.parentObj.Currency.getValue(),
                                quantity: obj.record.data.quantity,
                                transactiondate : this.isCallFromSalesOrderTransactionForms ? WtfGlobal.convertToGenericDate(new Date()) :WtfGlobal.convertToGenericDate(this.billDate),//isCallFromSalesOrderTransactionForms->True if PO is created from SO transaction Form
                                carryin : (this.isCustomer)? false : true,
                                getSOPOflag  :true,
                                startdate : WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true)),
                                enddate : WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false)),
                                skipRichTextArea:true
                            }
                        }, this,function(response){
                            var individualproductprice = response.data;
                            this.setIndividualProductPriceDetails(obj, rec, individualproductprice, quantity, rowRateIncludingGstAmountIndex);
                        }, function(){ 
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleasereselectproduct")], 2);
                                obj.grid.store.remove(obj.record);
                                        });
                    }
                    
                //            }
                }
                
                if((WtfGlobal.getModuleId(Wtf.getCmp(this.parentCmpID))==22 && !CompanyPreferenceChecks.mapTaxesAtProductLevel())||(WtfGlobal.getModuleId(Wtf.getCmp(this.parentCmpID))==18 &&  Wtf.getCmp(this.parentCmpID).fromLinkCombo.getValue()!="" && Wtf.getCmp(this.parentCmpID).fromLinkCombo.getValue()==0)){//FOR CROSS LINKING MODULE (SO IN PO AND VQ IN CQ) SET TAXAMOUNT,DISCOUNT TO ZERO
                    obj.record.set("prtaxid","");
                    obj.record.set("prtaxpercent",0);
                    obj.record.set("rowTaxAmount",0);
                    obj.record.set("prdiscount",0);
                    obj.record.set('discountispercent',1);
                }
                if((WtfGlobal.getModuleId(Wtf.getCmp(this.parentCmpID))==20 && Wtf.getCmp(this.parentCmpID).fromLinkCombo.getValue()==4)){//FOR CROSS LINKING MODULE (SO IN PO AND VQ IN CQ) SET TAXAMOUNT,DISCOUNT TO ZERO
                    obj.record.set("prtaxid","");
                    obj.record.set("prtaxpercent",0);
                    obj.record.set("rowTaxAmount",0);
                    obj.record.set("prdiscount",0);
                    obj.record.set('discountispercent',1);
                }
                if ((WtfGlobal.getModuleId(Wtf.getCmp(this.parentCmpID)) == 22 && Wtf.getCmp(this.parentCmpID).fromPO.getValue())) {//FOR CROSS LINKING MODULE (VQ IN CQ) SET TAXAMOUNT,DISCOUNT TO ZERO
                        obj.record.set("prtaxid", "");
                    obj.record.set("prtaxpercent", 0);
                    obj.record.set("rowTaxAmount", 0);
                    obj.record.set("prdiscount", 0);
                    obj.record.set('discountispercent', 1);
                }
                if (obj.record.data.quantity != "" && obj.record.data.type == "Inventory Assembly" && isAutoGenerateDO) {
                    this.setBOMValuationArrayToRecord(obj)
                }
                /**
                 * Code to populate Custom fields mapped to preferred product of selected customer/Vendor.
                 */
                var productComboIndex= WtfGlobal.searchRecordIndex(this.ProductMappedStore,obj.value,'productid');
                if (productComboIndex >= 0) {
                    prorec = this.ProductMappedStore.getAt(productComboIndex);
                    var jsonString = prorec.data['jsonString'];
                    var JSON1 = jsonString; //
                    for (var key in JSON1) {
                        if (JSON1.hasOwnProperty(key)) {
                            if (obj.grid) {
                                for (var k = 0; k < obj.grid.colModel.config.length; k++) {
                                    if (obj.grid.colModel.config[k].dataIndex == key) {
                                        var store = obj.grid.colModel.config[k].editor.field.store;
                                        if (store)
                                            store.clearFilter();
                                        obj.record.set(key, JSON1[key]);
                                    }
                                }
                            }
                        }
                    }
                }
                
            }else if((obj.field=="quantity" || obj.field=="pricingbandmasterid")){
                rec=obj.record;
                /**
                  * if there is linking case then populate the unit price from parent document and price will not be recalculate/refresh if Quantity is changed.
                  * But if user selects prce band from combo-box the price will be refresh according to price in price band -->as per action item in  SDP-14464.
                  */
                if (((!this.isCustomer && Wtf.account.companyAccountPref.productPricingOnBands) || (this.isCustomer && Wtf.account.companyAccountPref.productPricingOnBandsForSales))&& (obj.field=="pricingbandmasterid" || !islinkingFlag)) {
//                    if (this.loadingMask == undefined) {
//                        this.loadingMask = new Wtf.LoadMask(this.id, {
//                            msg: 'Loading' // WtfGlobal.getLocaleText("acc.msgbox.57")
//                        });
//                    }
//                    this.loadingMask.show();
                    if (this.parentObj) {
                        if( this.parentObj.saveBttn){
                            this.parentObj.saveBttn.disable();    
                        }
                        if( this.parentObj.savencreateBttn){
                            this.parentObj.savencreateBttn.disable();    
                        }
                        if( this.parentObj.saveAsDraftBttn){
                            this.parentObj.saveAsDraftBttn.disable();    
                        }
                    }
                    this.disableRowEditing = true;
                    this.disabledRow = obj.row;
                    
                    Wtf.Ajax.requestEx({
                        url:"ACCProduct/getIndividualProductPrice.do",
                        params: {
                            productid: obj.record.data.productid,
                            affecteduser: this.affecteduser,
                            forCurrency: Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency : "",
                            currency: this.parentObj.Currency.getValue(),
                            quantity: obj.record.data.quantity,
                            transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                            carryin : (this.isCustomer)? false : true,
                            pricingbandmaster:obj.record.data.pricingbandmasterid,
                            uomid:obj.record.data.uomid
                        }
                    }, this,function(response) {
//                        this.loadingMask.hide();
                        this.disableRowEditing = false;
                        this.disabledRow = undefined;
                        if (this.parentObj) {
                            if( this.parentObj.saveBttn){
                                this.parentObj.saveBttn.enable();    
                            }
                            if( this.parentObj.savencreateBttn){
                                this.parentObj.savencreateBttn.enable();    
                            }
                            if( this.parentObj.saveAsDraftBttn){
                                this.parentObj.saveAsDraftBttn.enable();    
                            }
                        }
                  
                        var datewiseprice =response.data[0].price ? response.data[0].price : 0;
                        this.isPriceListBand = response.data[0].isPriceListBand;
                        this.isPriceBandIncludingGst = response.data[0].isIncludingGst;
                        this.isVolumeDisocunt = response.data[0].isVolumeDisocunt;
                        this.priceSource = response.data[0].priceSource ? response.data[0].priceSource : "";
                        this.pricingbandmasterid=response.data[0].pricingbandmasterid ? response.data[0].pricingbandmasterid : "";
                        this.isPriceFromUseDiscount = response.data[0].isPriceFromUseDiscount;
                        this.priceSourceUseDiscount = response.data[0].priceSourceUseDiscount;
                        this.defaultPrice = datewiseprice;
                        this.purchaseprice=response.data[0].purchaseprice;
                        this.amendpurchaseprice=response.data[0].amendpurchaseprice;
                        this.isamendpurchasepricenotavail = response.data[0].isamendpurchasepricenotavail;
                        this.isVolumeDisocuntExist = response.data[0].isVolumeDisocuntExist;
                        this.isBandPriceConvertedFromBaseCurrency = response.data[0].isBandPriceConvertedFromBaseCurrency;
                        this.isbandPriceNotAvailable = response.data[0].isbandPriceNotAvailable;
                        var discountType = response.data[0].discountType;
                        var discountValue = response.data[0].discountValue;
                        baseuomrate =(response.data[0].baseuomrate!=undefined && response.data[0].baseuomrate!="")?response.data[0].baseuomrate:1;
                        this.matchvolumeDiscountid = response.data[0].matchvolumeDiscountid;//get matched volume discount
                        if((this.isPriceListBand || this.isVolumeDisocunt) && !(this.matchvolumeDiscountid != undefined && this.matchvolumeDiscountid != "" && obj.record.data.volumdiscountid != undefined && obj.record.data.volumdiscountid != "" && obj.record.data.volumdiscountid == this.matchvolumeDiscountid)){
                           if(rec && this.defaultPrice != rec.data.rate && rec.data.rate!=""){
                            Wtf.MessageBox.show({
                                title:WtfGlobal.getLocaleText("acc.common.info"),
                                msg: WtfGlobal.getLocaleText("acc.invoice.PricewillgetresettoBandpriceMsg"),
                                buttons: Wtf.MessageBox.OK,
                                icon: Wtf.MessageBox.INFO,
                                scope: this,
                                fn: function (btn) {
                                    if (btn == "ok") {
                                        this.transQuantity.enable();
                                    }
                                }
                            });
                        }
                    }
                        /**
                           * Checking if price is mapped to UOM if yes the setting the unit price which is set in db else converting it as rule set in UOM schema.
                           * ERM-389 / ERP-35140
                           */
                        var modifiedRate=this.defaultPrice,isPriceMappedToUOM = false;
                        if (response.data[0].isPriceMappedToUOM != undefined && response.data[0].isPriceMappedToUOM != "") {
                            isPriceMappedToUOM = response.data[0].isPriceMappedToUOM;
                        }
                         /**
                         * No need to change unit price in foreign currency if price already present in 
                         * foreign currency. 
                         */
                        if(this.isbandPriceNotAvailable!=undefined && this.isbandPriceNotAvailable) {
                            modifiedRate = WtfGlobal.getIndividualProductPriceInMultiCurrency(rec, datewiseprice);
                        }
                        /**
                         * below code creates a json of multiple discounts mapped to specific product and sets in record(discountjson)
                         */
                        if (CompanyPreferenceChecks.discountMaster()) {
                            var jsonArr = createDiscountString(response, obj.record, this.defaultPrice, obj.record.data.quantity); //after quantity change
                            var jsonObj;
                            var jsonStr = ""
                            if (jsonArr != "" && jsonArr != undefined) {
                                jsonObj = {"data": jsonArr};
                                jsonStr = JSON.stringify(jsonObj);
                            }
                            obj.record.set("discountjson", jsonStr);
                        } else {
                            obj.record.set("discountjson", "");
                        }
                        
                        obj.record.set("pricingbandmasterid", this.pricingbandmasterid);
                        if (discountType != undefined && discountValue != undefined) {
                            if (discountType == 0 || discountType == "0") {
                                obj.record.set("discountispercent", 0);
                            } else {
                                obj.record.set("discountispercent", 1);
                            }
                            obj.record.set("prdiscount", discountValue);
                        }
                        if (this.isVolumeDisocunt) {
                            if (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden) {
                                obj.record.set("rateIncludingGst", this.defaultPrice);
                                if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                                    this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                                } else {
                                    this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                                }
                            }else {
                                obj.record.set("rate", this.defaultPrice);
                            }
                            obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSourceUseDiscount + ": " + WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                        } else if (this.isPriceListBand) {
                            obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                            if (this.isPriceFromUseDiscount) {
                                if (obj.record.data.quantity != "") {
                                    /**
                                     * if block is used : when user changes qty the it match prevolume volume discount to current qty volume discount
                                     * if this is matched the set the price given by user manually for that matched volume discount,
                                     * if not matched the set default price.
                                     */
                                    if (this.matchvolumeDiscountid != undefined && this.matchvolumeDiscountid != "" && obj.record.data.volumdiscountid != undefined && obj.record.data.volumdiscountid != "" && obj.record.data.volumdiscountid == this.matchvolumeDiscountid) {
                                        /**
                                         * block used to store the previous unit price for match volume discount and
                                         * set  oldunitpriceFlag true for that record so that whenever qty will changed the manually price should not get change or update
                                         */
//                                        if (!oldunitpriceFlag) {
////                                            obj.record.set("oldunitprice", rec.data.rate);
//                                            obj.record.set("oldunitpriceFlag", true);
//                                        }
                                        if (oldunitpriceFlag) {//set manual price
//                                            obj.record.set("rate", rec.data.oldunitprice);
                                            obj.record.set("rate", this.defaultPrice);
                                        } else {//set price band price.
                                            obj.record.set("rate", rec.data.rate);
                                            
                                        }
                                    } else {
                                         /**
                                         * block used to store the previous unit price for match volume discount and
                                         * set  oldunitpriceFlag true for that record so that whenever qty will changed the manually price should not get change or update
                                         */
                                        if (!oldunitpriceFlag) {
//                                            obj.record.set("oldunitprice", rec.data.rate);
                                            obj.record.set("oldunitpriceFlag", true);
//                                            obj.record.set("rate", this.defaultPrice);
                                        }
                                        obj.record.set("rate", this.defaultPrice);
                                        if (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden) {
                                            obj.record.set("rateIncludingGst", this.defaultPrice);
                                            if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                                                this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                                            } else {
                                                this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                                            }
                                        } else {
                                            obj.record.set("rate", this.defaultPrice);
                                        }
                                    }
                                    obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSourceUseDiscount + ": " + WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                                } else {
                                    obj.record.set("rate", "");
                                    obj.record.set("priceSource", "");
                                }
                            } else {
//                                if (this.isVolumeDisocuntExist) {
                                    if (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden) {
                                          /**
                                           * Checking if price is mapped to UOM if yes the set the unit price which is set in db else converting it as rule set in UOM schema.
                                           * ERM-389 / ERP-35140
                                           */
                                        obj.record.set("rateIncludingGst", isPriceMappedToUOM ? this.defaultPrice : modifiedRate * baseuomrate);
                                    if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                                        this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                                    } else {
                                        this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                                    }
                                    } else {
                                        obj.record.set("rate", this.defaultPrice);
                                    }
                                      /**
                                       * Checking if price is mapped to UOM if yes the setting the unit price which is set in db else converting it as rule set in UOM schema.
                                       * ERM-389 / ERP-35140
                                       */
                                    obj.record.set("rate", isPriceMappedToUOM ? this.defaultPrice : modifiedRate * baseuomrate);
                                    obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
//                                }
                            }
                            this.checkForPricebandIncludingGst(obj) ; //ERP-17723
                            // alert if band price converted from base currency
                            if (this.isBandPriceConvertedFromBaseCurrency) {
                                var currencycode = "";
                                var index = this.parentObj.getCurrencySymbol();
                                if (index >= 0) {
                                    currencycode = this.parentObj.currencyStore.getAt(index).data.currencycode;
                                }
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), currencycode + " " + WtfGlobal.getLocaleText("acc.bandPriceForSelectedCurrencyNotAvailable.msg")], 2);
                            }
                        } else {
                            if (this.isPriceFromUseDiscount) {
                                if (obj.record.data.quantity != "") {
                                    if (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden) {
                                        obj.record.set("rateIncludingGst", this.defaultPrice);
                                        if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                                            this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                                        } else {
                                            this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                                        }
                                    } else {
                                        obj.record.set("rate", this.defaultPrice);
                                    }
                                    obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSourceUseDiscount);
                                } else {
                                    obj.record.set("rate", "");
                                    obj.record.set("priceSource", "");
                                }
                            } else {
                                /**
                                 * Case : when transaction currency and price currency is not matched then price is converted from price list currency to transaction currency.
                                 */
                                if (!Wtf.account.companyAccountPref.productPriceinMultipleCurrency && this.parentObj && this.parentObj.Currency.getValue() != WtfGlobal.getCurrencyID()) { // If product in Multiple currency is not set in account preferences and selected currency is in other than base currency
                                    var rate = ((rec == undefined || rec.data['currencyrate'] == undefined || rec.data['currencyrate'] == "") ? 1 : rec.data['currencyrate']);
                                    var oldcurrencyrate = ((rec == undefined || rec.data['oldcurrencyrate'] == undefined || rec.data['oldcurrencyrate'] == "") ? 1 : rec.data['oldcurrencyrate']);
                                    if (rate != 0.0) {
                                        modifiedRate = getRoundofValueWithValues(((parseFloat(datewiseprice) * parseFloat(rate)) / parseFloat(oldcurrencyrate)), Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                                    } else {
                                        modifiedRate = getRoundofValueWithValues((parseFloat(datewiseprice) / parseFloat(oldcurrencyrate)), Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                                    }
                                    if (isPriceMappedToUOM) {
                                        this.defaultPrice = modifiedRate;
                                    }
                                }
                                if (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden) {
                                    obj.record.set("rateIncludingGst", this.defaultPrice);
                                    if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                                        this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                                    } else {
                                        this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                                    }
                                } else {
                                    if (this.isVolumeDisocuntExist) {
                                        obj.record.set("rate", this.defaultPrice);
                                    }
                                }
                                if (this.isPriceListBand) {
                                    obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                                } else {
                                    obj.record.set("priceSource", "");
                                    obj.record.set("rate", isPriceMappedToUOM ? this.defaultPrice : modifiedRate * baseuomrate);
                                }
                                
                            }
                          /**
                            * alert is given if band price not available in band.
                            */
                            if (this.isbandPriceNotAvailable ) {
                                if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                                    if (!((this.moduleid == Wtf.Acc_Sales_Order_ModuleId && Wtf.account.companyAccountPref.negativeStockSO == 2) || ((this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId) && Wtf.account.companyAccountPref.negativeStockSICS == 2))) {
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.DefaultUnitPriceIsnotsetPriceband")], 2);
                                    }
                                } else{
                                    if (!((this.moduleid == Wtf.Acc_Sales_Order_ModuleId && Wtf.account.companyAccountPref.negativeStockSO == 2) || ((this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId) && Wtf.account.companyAccountPref.negativeStockSICS == 2))) {
                                        var rateIndex = this.getColumnModel().findColumnIndex("rate");
                                        if (this.getColumnModel().config[rateIndex] != undefined || this.getColumnModel().config[rateIndex] != null || this.getColumnModel().config[rateIndex] != "") {
                                            if (!this.getColumnModel().config[rateIndex].hidden) {
                                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.bandPriceNotAvailable.msg")], 2);
                                            }
                                        }                                        
                                     }
                                }
                            }
                        }
                         //Tax Calculation for India
                        // Tax Calculation at line level terms
                        if(Wtf.account.companyAccountPref.isLineLevelTermFlag && this.moduleid != Wtf.Acc_Purchase_Requisition_ModuleId  && this.moduleid != Wtf.Acc_RFQ_ModuleId){
                            var productComboIndex_INDIA = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.data.productid.trim(), 'productid');
                            if(productComboIndex_INDIA == -1){
                                productComboIndex_INDIA = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.data.productid.trim(), 'pid');
                            }
                            if(productComboIndex_INDIA >=0){
                                prorec = this.productComboStore.getAt(productComboIndex_INDIA);
                                if(prorec!=undefined){
                                    rec.set('valuationType',prorec.data['valuationType']);
                                    rec.set('valuationTypeVAT',prorec.data['valuationTypeVAT']);
                                    rec.set('productMRP',prorec.data['productMRP']);
                                    rec.set('reortingUOMExcise',prorec.data['reortingUOMExcise']);
                                    rec.set('reortingUOMSchemaExcise',prorec.data['reortingUOMSchemaExcise']);
                                    rec.set('reportingUOMVAT',prorec.data['reportingUOMVAT']);
                                    rec.set('reportingUOMSchemaVAT',prorec.data['reportingUOMSchemaVAT']);
                                    rec.set('recTermAmount',prorec.data['recTermAmount'] == "" || prorec.data['recTermAmount'] == undefined ? 0 : prorec.data['recTermAmount'] );
                                    rec.set('OtherTermNonTaxableAmount',prorec.data['OtherTermNonTaxableAmount'] == "" || prorec.data['OtherTermNonTaxableAmount'] == undefined ? 0 : prorec.data['OtherTermNonTaxableAmount'] );
                                    if(prorec.data['compairwithUOM'] != undefined  && prorec.data['compairwithUOM'] != ""){
                                        rec.set('compairwithUOM',prorec.data['compairwithUOM']);
                                    }else{
                                        rec.set('compairwithUOM',1);
                                    } 
                                    if(prorec.data['compairwithUOMVAT'] != undefined  && prorec.data['compairwithUOMVAT'] != ""){
                                        rec.set('compairwithUOMVAT',prorec.data['compairwithUOMVAT']);
                                    }else{
                                        rec.set('compairwithUOMVAT',1);
                                    } 
                                }else{
                                    rec.set('recTermAmount',0);
                                    rec.set('OtherTermNonTaxableAmount',0);
                                    rec.set('valuationType',"");
                                    rec.set('valuationTypeVAT',"");
                                    rec.set('compairwithUOM',1);
                                    rec.set('compairwithUOMVAT',1);
                                    rec.set('productMRP',0);
                                }
                                if(!this.isGST && prorec.data['LineTermdetails'] != undefined  && prorec.data['LineTermdetails'] != ""){
                                    var termStore = this.getTaxJsonOfIndia(rec);
                                    obj.record.set('uncheckedTermdetails',prorec.data['uncheckedTermdetails']);
                                    obj.record.set('dealerExciseTerms',prorec.data['dealerExciseTerms']);
                                    if(this.parentObj && this.parentObj.includingGST && this.parentObj.includingGST.getValue() == true) {
                                        this.getColumnModel().setRenderer(this.getColumnModel().findColumnIndex("amount"),WtfGlobal.withoutRateCurrencySymbol);
                                        termStore = this.calculateTermLevelTaxesInclusive(termStore, rec);
                                    }else {
                                        this.getColumnModel().setRenderer(this.getColumnModel().findColumnIndex("amount"),this.calAmountWithoutExchangeRate.createDelegate(this));
                                        termStore = this.calculateTermLevelTaxes(termStore, rec,undefined,true);
                                    }                           
                                    obj.record.set('LineTermdetails',Wtf.encode(termStore));
                                    this.updateTermDetails(); 
                                }else if (this.isGST) {
                                    if (this.isModuleForAvalara) {
                                        getTaxFromAvalaraAndUpdateGrid(this, obj);
                                    } // If terms are already present then calculate only tax values 
                                    else if (rec.data.LineTermdetails != '') {
                                        calculateUpdatedTaxes(this.parentObj, this, rec);
                                        this.updateTermDetails();
                                    } else {
                                    /**
                                        * ERP-32829 
                                        * code for New GST 
                                     */
                                    if (this.parentObj.isPurchasesTransaction && this.parentObj.purchaseFromURD) {
                                        /*** If purchases is from Unregistered dealer ***/
                                        if (this.parentObj.isRCMApplicableInPreferences && this.parentObj.GTAApplicable.getValue()) {
                                        this.parentObj.uniqueCase = Wtf.GSTCustVenStatus.APPLYGST;
                                    } else {
                                        this.parentObj.uniqueCase = Wtf.GSTCustVenStatus.NOGST;
                                    }
                                }
                                processGSTRequest(this.parentObj, this, prorec.data.productid);
                            }
                            }
                            }

                        }
                        this.fireEvent('datachanged',this);
                    }, function(response) {
//                        this.loadingMask.hide();
                        this.disableRowEditing = false;
                        this.disabledRow = undefined;
                        if (this.parentObj) {
                            if( this.parentObj.saveBttn){
                                this.parentObj.saveBttn.enable();    
                            }
                            if( this.parentObj.savencreateBttn){
                                this.parentObj.savencreateBttn.enable();    
                            }
                            if( this.parentObj.saveAsDraftBttn){
                                this.parentObj.saveAsDraftBttn.enable();    
                            }
                        }
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleasereselectproduct")], 2);
                    });
                }
                if (obj.record.data.quantity != "" && obj.record.data.type == "Inventory Assembly" &&  isAutoGenerateDO) {
                    this.setBOMValuationArrayToRecord(obj)
                }
                if(this.isCustomer)
                    rec.set("changedQuantity",(rec.data.copyquantity-obj.value)*((rec.data.copybaseuomrate==="")?1:rec.data.copybaseuomrate));
                else
                    rec.set("changedQuantity",(obj.value-rec.data.copyquantity)*((rec.data.copybaseuomrate==="")?1:rec.data.copybaseuomrate));
                rec.set("baseuomquantity", obj.record.get("quantity") * obj.record.get("baseuomrate"));
                var displayuomtest = obj.record.get("displayuomrate");
                var displayuomid = obj.record.get("displayUoMid");
                if (displayuomtest != "NaN" && displayuomtest != undefined && displayuomtest != "" && displayuomtest != null && displayuomid != undefined && displayuomid != "" && displayuomid != null){
                    rec.set("displayuomvalue", (displayuomtest == 1) ? obj.record.get("quantity") : (obj.record.get("quantity") * obj.record.get("baseuomrate")) / displayuomtest);
                }else{
                    rec.set("displayuomvalue", "");
                }
                if(Wtf.account.companyAccountPref.isLineLevelTermFlag){ // Valuation Type Quantity - India Compliance
                    if(obj.record.get("valuationType")==Wtf.excise.QUANTITY){
                        rec.set("quantityInReportingUOM", obj.record.get("baseuomquantity") * obj.record.get("compairwithUOM"));
                    }
                    if(obj.record.get("valuationTypeVAT")==Wtf.excise.QUANTITY){
                        rec.set("quantityInReportingUOMVAT", obj.record.get("baseuomquantity") * obj.record.get("compairwithUOMVAT"));
                    }
                }
                var isLinkedFromDO=false;
                if(this.parentObj.fromLinkCombo.getValue()==1){
                    isLinkedFromDO=true;
                }
                var islockQuantityflag=this.store.getAt(obj.row).data['islockQuantityflag']; //in linked case whether salesorder is locked or not
                if(islockQuantityflag==''){
                    islockQuantityflag=false;
                }
                var allowCrossLinking=false;
                
                if(WtfGlobal.getModuleId(Wtf.getCmp(this.parentCmpID))==22||(WtfGlobal.getModuleId(Wtf.getCmp(this.parentCmpID))==18 && Wtf.getCmp(this.parentCmpID).fromLinkCombo.getValue()==0) ){
                    allowCrossLinking=true;
                }
                if((WtfGlobal.getModuleId(Wtf.getCmp(this.parentCmpID))==20 && Wtf.getCmp(this.parentCmpID).fromLinkCombo.getValue()==4)){//FOR CROSS LINKING MODULE (SO IN PO AND VQ IN CQ) SET TAXAMOUNT,DISCOUNT TO ZERO
                    allowCrossLinking=true;
                }
                if ((WtfGlobal.getModuleId(Wtf.getCmp(this.parentCmpID)) == 22 && Wtf.getCmp(this.parentCmpID).fromPO.getValue())) {//FOR CROSS LINKING MODULE (VQ IN CQ) SET TAXAMOUNT,DISCOUNT TO ZERO
                    allowCrossLinking=true;
                }
                var allowExceeding = 0;
               if(Wtf.account.companyAccountPref.isAllowQtyMoreThanLinkedDoc && !islockQuantityflag && !allowCrossLinking){   // check condition for SO block quantity  as well as company preferences option
                    if(isLinkedFromDO){
                        allowExceeding=0;   //Not Allow to Exceeding   
                    }else{
                        allowExceeding=1;   //Allow to Exceeding   
                    }
                }
                if(Wtf.account.companyAccountPref.isAllowQtyMoreThanLinkedDocCross && !islockQuantityflag  && allowCrossLinking){   // check condition for SO block quantity  as well as company preferences option
                    allowExceeding=0;
                    if(isLinkedFromDO){
                        allowExceeding=0;   //Not Allow to Exceeding   
                    }else{
                        allowExceeding=1;   //Allow to Exceeding   
                    }
                }
                if(allowExceeding==0 && (((rec.data.isNewRecord=="" || rec.data.isNewRecord==undefined) && this.fromOrder&&!(this.editTransaction||this.copyInv))||(this.isEdit && rec.data.linkid !=""))) {  
                    if(obj.value > rec.data.copyquantity && !(this.copyInv)){
                        /*added following if condition to avoid only one case - User can Enter Quantity than Original Quantity present in PR. (ERP-16470) and (ERP-16994)*/
                       if(this.isQuotation && !this.isCustomer){
                           var msg = WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinVQisexceedsfromoriginalcontinue")
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"),msg,function(btn){
                                if(btn!="yes") {
                                    obj.record.set(obj.field, obj.originalValue);
                                    obj.record.set("baseuomquantity",obj.originalValue*obj.record.get("baseuomrate"));
                                }
                        },this)
                       }else{
                        var msg = WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinCIisexceedsfromoriginal")
                        if (this.isCustomer) {
                            if(this.isOrder && !this.isQuotation){
                                msg = WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinSOisexceedsfromoriginal")
                            } else if(this.isQuotation) {
                                msg = WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinCQisexceedsfromoriginal")
                            } else {
                                if(Wtf.account.companyAccountPref.withinvupdate){
                                    msg = WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinCIisexceedsfromoriginalquantitymentionedinSO/DO/CQ")
                                } else {
                                    msg = WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinCIisexceedsfromoriginalquantitymentionedinCQ/SO")
                                }
                                
                            }
                            
                        } else {
                            if(this.isOrder && !this.isQuotation){
                                if(this.moduleid==Wtf.Acc_Security_Gate_Entry_ModuleId){
                                    msg = WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinSGEisexceedsfromoriginalquantitymentionedinPO")
                                }else{
                                    msg = WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinPOisexceedsfromoriginalquantitymentionedinVQ/SO")
                                }
                            } else if(this.isQuotation) {
                                msg = WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinVQisexceedsfromoriginal")
                            }else {
                                if(Wtf.account.companyAccountPref.withinvupdate){
                                    msg = WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinVIisexceedsfromoriginalquantitymentionedselectedPO/GR/VQ")
                                } else {
                                    msg = WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinVIisexceedsfromoriginalquantitymentionedinPO/GR/VQ")                               
                                }
                                
                            }
                            
                        }
                        obj.record.set(obj.field, obj.originalValue);
                         Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.common.alert"),msg,function(btn){
                              if (btn == "ok") {
                                  this.transQuantity.enable();
                               }    
                        },this)
                               }
                    }else if(obj.value!=rec.data.copyquantity && !(this.copyInv)) {
                        var msg = WtfGlobal.getLocaleText("acc.field.ProductenteredInvoicedifferentoriginal")
                        if (this.isCustomer) {
                             if(this.isOrder && !this.isQuotation){
                                msg = WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinSOisdifferentfromoriginal")
                                if(!this.showWaringMsg && this.transQuantity){
                                    this.transQuantity.enable();
                                    this.showWaringMsg = true;
                                }
                            } else if(this.isQuotation) {
                                msg = WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinCQisdifferent")
                            }else {
                                if(Wtf.account.companyAccountPref.withinvupdate){
                                    msg = WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinCIisdifferentfromoriginal")
                                } else {
                                    msg = WtfGlobal.getLocaleText("acc.field.ProductinCIdifferentCQ/SOwanttocontinue")
                                }
                                
                            }
                            
                        } else {
                            if(this.isOrder && !this.isQuotation){
                               if(this.moduleid==Wtf.Acc_Security_Gate_Entry_ModuleId){
                                    msg = WtfGlobal.getLocaleText("acc.field.ProductinSGEdifferentPOcontinue")
                                }else{
                                    msg = WtfGlobal.getLocaleText("acc.field.ProductinPOdifferentSO/VQcontinue")
                                }
                            } else if(this.isQuotation) {
                                msg = WtfGlobal.getLocaleText("acc.field.ProductinVQdifferentPRcontinue")
                            } else {
                                if(Wtf.account.companyAccountPref.withinvupdate){
                                    msg = WtfGlobal.getLocaleText("acc.field.ProductinisquantityinselectedPO/GR/VQwanttocontinue")
                                } else {
                                    msg = WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredVIismentionedVQ/POwantcontinue")
                                }
                                
                            }
                            
                        }
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"),msg,function(btn){
                                if(btn!="yes") {
                                    obj.record.set(obj.field, obj.originalValue);
                                    obj.record.set("baseuomquantity",obj.originalValue*obj.record.get("baseuomrate"));
                                    /*
                                     * Fired event to update subtotal in invoice(invoice.js)
                                     */
                                    this.fireEvent('datachanged',this);
                                }
                        },this)
                    }
                }
                  if(this.isEdit){                          // In edit mode if user remove some products from product grid then to send that information to inventory deleteStore is used
                    if((obj.record.data["quantity"])==0 &&(obj.record.data["isNewRecord"])!='1'){
                        if(obj.record.data.copyquantity!=undefined){                    
                            var deletedData=[];
                            var newRec=new this.deleteRec({
                                productid:obj.record.data.productid,
                                productname:obj.record.data.productname,    
                                productquantity:obj.record.data.copyquantity,
                                productbaseuomrate:obj.record.data.baseuomrate                            
                            });                            
                            deletedData.push(newRec);
                            this.deleteStore.add(deletedData);                            
                        }
                        //this.store.remove(obj.record);//ERP-12142
                    }
                }                  
                var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
                var ComboIndex=0;
                  var productuomid = "";
                  if(this.productOptimizedFlag!= undefined && (this.productOptimizedFlag==Wtf.Products_on_type_ahead || this.productOptimizedFlag==Wtf.Show_all_Products) && productComboIndex==-1){
                    productComboIndex=1;
                    ComboIndex=-1;
                }
                  if(productComboIndex >=0){
                      prorec = this.productComboStore.getAt(productComboIndex);
                      if(ComboIndex==-1){
                          prorec=rec;
                      }
                      productuomid = prorec.data.uomid;
                      if( allowExceeding == 1){
                      if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                            obj.record.set("baseuomquantity", quantity*obj.record.get("baseuomrate"));
                      } else {
                          obj.record.set("baseuomrate", 1);
                          obj.record.set("baseuomquantity", quantity*obj.record.get("baseuomrate"));
                      }
                    }
                      
                      if (prorec.data.type != 'Service' && prorec.data.type != 'Non-Inventory Part') { // serial no for only inventory type of product
                          if (prorec.data.isSerialForProduct) {
                              var v = obj.record.data.quantity;
                              v = String(v);
                              var ps = v.split('.');
                              var sub = ps[1];
                              if (sub!=undefined && sub.length > 0) {
                                  WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.exceptionMsgForDecimalQty")], 2);
                                  obj.record.set("quantity", obj.originalValue);
                                  obj.record.set("baseuomquantity", obj.originalValue*obj.record.get("baseuomrate"));
                              }
//                          }else if(((prorec.data.isLocationForProduct && (prorec.data.location==undefined || prorec.data.location=="")) || (prorec.data.isWarehouseForProduct && (prorec.data.warehouse==undefined || prorec.data.warehouse==""))) && !prorec.data.isSerialForProduct && !prorec.data.isBatchForProduct){
//                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.exceptionMsgForlocware")], 2);
//                            obj.cancel=true;
//                            return;   ERP-16602
            
                        }else {
                            WtfGlobal.setDefaultWarehouseLocation(obj, prorec,false);
                        }
                      }
                      if(Wtf.account.companyAccountPref.isMinMaxOrdering && this.moduleid == Wtf.Acc_Purchase_Order_ModuleId){
                        this.checkMinMaxOrderingQuantity(obj,prorec);
                     } 
//                    else if(this.moduleid == Wtf.Acc_Sales_Order_ModuleId){
//                        //this.checkMinMaxOrderingQuantity(obj,prorec);
//                     var productID="";    
//                        productID = prorec.data.pid
//                        if (obj.record.get("baseuomquantity") < prorec.data.minorderingquantity) {
//                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.BaseQuantityislessthanMinimumOrderingQuantityforproduct") + "<b>" + productID + "</b><br/>" + WtfGlobal.getLocaleText("acc.msgbox.Doyouwanttoproceed"), function(btn) {
//                                if (btn != "yes") {
//                                    obj.record.set(obj.field, obj.originalValue);
//                                    obj.record.set("baseuomquantity", obj.record.get("quantity") * obj.record.get("baseuomrate"));
//                                }
//                            }, this)
//                        }
//                    } 
                }
                
                this.fireEvent('datachanged',this);
            }
            
            /*For SATS*/
            if(SATSCOMPANY_ID==companyid){
                if(obj.field=="dependentType"){
                    var datewiseprice=obj.record.data.rate;
                    if(Wtf.account.companyAccountPref.dependentField) 
                    {
                        var index=this.dependentTypeStore.find('dependentType',rec.data.dependentType);
                        if(index>=0){
                            var dependent=this.dependentTypeStore.getAt(index);
                            if(dependent.data.price>0){
                                datewiseprice=dependent.data.price;
                            }
                        }
                        for (var k = 0; k < obj.grid.colModel.config.length; k++) {   
                            if(obj.grid.colModel.config[k].editor && obj.grid.colModel.config[k].editor.field.store && obj.grid.colModel.config[k].dataIndex=='dependentType'){ 
                                var store = obj.grid.colModel.config[k].editor.field.store;
                                store.clearFilter();
                            }   
                            obj.record.set("rate", datewiseprice);
                            this.fireEvent('datachanged',this);                     
                        }   
                    }
                }
            }
            /**
             * while changing the unit price when linking GRN to Purchase invoice.
             * and given a pop up message "Since Goods Receipt is linked with Purchase Invoice, Document value of Purchase Invoice and Goods Receipt would become different to each other If you change the unit price/Exchange rate."
             */
            if ((obj.field == "rate" || obj.field == "rateIncludingGst") && this.parentObj != undefined && (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && this.parentObj.fromLinkCombo.getValue() == 1) && this.parentObj.PO.getValue() != "" && obj.record.json != undefined && obj.record.json.rate != obj.value) {
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"),
                    msg: WtfGlobal.getLocaleText("acc.invoicegrid.cannotchangeunitprice"),
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.WARNING,
                    scope: this,
                    scopeObj: this,
                    fn: function (btn) {
                        if (btn == "ok") {
                            this.updateunitpricewithGST(obj,islinkingFlag);
                        }
                    }
                });
            } else {
                   this.updateunitpricewithGST(obj,islinkingFlag);
            }
            
            if (obj.field=="rate" && obj.originalValue != obj.value) {
                if (!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.addprice) && !this.isRequisition) { // permissions
                    var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.data.productid, 'productid');
                    var productname = "";
                    var prorec = null;
                    if (productComboIndex >= 0) {
                        prorec = this.productComboStore.getAt(productComboIndex);
                        productname = prorec.data.productname;
                    }else{
                            prorec = obj.record;
                            productname = prorec.data.productname;
                        }
                    rec.set("productname", productname);
                    rec.set("price", obj.value);
                    if (this.isCustomer) {
                        rec.set("customer", this.parentObj.Name.getValue());
                    } else {
                        rec.set("vendor", this.parentObj.Name.getValue());
                    }
                    if (Wtf.account.companyAccountPref.priceConfigurationAlert) {
                        Wtf.Msg.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.youHaveChangedThePriceForProduct") + " <b>" + productname + "</b>." + " " + WtfGlobal.getLocaleText("acc.field.doYouWantToSaveInPriceMaster"),
                                this.showPriceWindow.createDelegate(this, [rec, obj], true), this);
                    }
                }
                
                if (obj.record != undefined && obj.record.data != undefined && obj.record.data.discountjson != undefined && obj.record.data.discountjson != "" && CompanyPreferenceChecks.discountMaster()) {
                    var jsonObj = JSON.parse(obj.record.data.discountjson);
                    calculateDiscount(jsonObj.data,obj.record,obj.record.data.rate,obj.record.data.quantity,true);
                }
            }
            if(obj.field=="vendorid" && obj.value != null){ // Set vendorcurrencysymbol and vendorcurrencyid and vendorcurrexchangerate on vendor select
                var index = WtfGlobal.searchRecordIndex(Wtf.vendorAccStore, obj.value, 'accid');
                var vendorrec = null;
                if(index >=0){
                    vendorrec = Wtf.vendorAccStore.getAt(index);
                    if(vendorrec!=null){
                        rec.data.vendorcurrencysymbol = vendorrec.data.currencysymbol;
                        rec.data.vendorcurrencyid = vendorrec.data.currencyid;
                        this.fireEvent("vendorselect", vendorrec , rec);
                    }
                }
            }
            
//            var rowRateIncludingGstAmountIndex=this.getColumnModel().getIndexById(this.id+"rateIncludingGst");
//            var rowRateIncludingGstAmountIndex=this.getColumnModel().findColumnIndex("rateIncludingGst");   //instead of getIndexById() used findColumnIndex based on dataindex, refer ticket ERP-17718
            if((obj.field=="prtaxid"||obj.field=="quantity")&&!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden){
                if(SATSCOMPANY_ID==companyid){/*For SATS*/
                    taxamount = this.setTaxAmountAfterIncludingGst(obj.record,1);
                    var amountwithOutGst = this.setTaxAmountAfterIncludingGst(obj.record,2);
                    var amountwithGst = this.setTaxAmountAfterIncludingGst(obj.record,3);
                    obj.record.set("taxamount",taxamount);
                    obj.record.set("isUserModifiedTaxAmount", false);
                    if(amountwithGst!=0){
                        obj.record.set("rateIncludingGst",amountwithGst);
                    }
                    if(amountwithOutGst!=0){
                        obj.record.set("rate",amountwithOutGst);
                    }
                }else{
                    if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                        this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                    } else {
                        this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                    }
                }
                this.fireEvent('datachanged',this);
            }
            if(obj.field=="rateIncludingGst"){
                if(SATSCOMPANY_ID==companyid){/*For SATS*/
                    taxamount = this.setTaxAmountAfterIncludingGst(obj.record,1);
                    var amountwithOutGst = this.setTaxAmountAfterIncludingGst(obj.record,2);
                    obj.record.set("taxamount",taxamount);
                    obj.record.set("isUserModifiedTaxAmount", false);
                    if(amountwithOutGst!=0){
                        obj.record.set("rate",amountwithOutGst);
                    }else{
                        obj.record.set("rate",obj.record.data.rateIncludingGst);
                    }
                }else{
                    if (obj.record != undefined && obj.record.data != undefined && obj.record.data.discountjson != undefined && obj.record.data.discountjson != "" && CompanyPreferenceChecks.discountMaster()) {
                        var jsonObj = JSON.parse(obj.record.data.discountjson);
                        calculateDiscount(jsonObj.data, obj.record, obj.record.data.rateIncludingGst, obj.record.data.quantity, true);
                    }
                    if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                        this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                    } else {
                        this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                    }
                }
                this.fireEvent('datachanged',this);
            }
            
            if(obj.field!=undefined&&obj.field=="partamount"){
                taxamount = this.setTaxAmountAfterSelection(obj.record);
                obj.record.set("taxamount",taxamount);
                obj.record.set("isUserModifiedTaxAmount", false);
                this.fireEvent('datachanged',this);
            }
            
             if(this.isNote){ 
                if(obj.field=="typeid"){
                    if(rec.data['typeid']!=0){
                        rec.set('accountId',rec.data['salesAccountId']);
                    }else{
                         rec.set('accountId',rec.data['discountAccountId']);
                    }
                }
             } 
             
            //Tax Calculation for India
            // Except Product Combo Edit, For All Edit below code will be used for Multiple Term level Tax calculation.
            if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
                if((obj.field=="quantity") 
                    || (obj.field=="rate" && obj.originalValue != obj.value)
                    || (obj.field=="rateIncludingGst")
                    || (obj.field=="discountispercent")
                    || (obj.field=="prdiscount")){
                
                            if(!this.isGST && rec.get('LineTermdetails') != undefined && rec.get('LineTermdetails') != ''){
                            var termStore = this.getTaxJsonOfIndia(rec);
                            if(this.parentObj && this.parentObj.includingGST && this.parentObj.includingGST.getValue() == true) {
                                this.getColumnModel().setRenderer(this.getColumnModel().findColumnIndex("amount"),WtfGlobal.withoutRateCurrencySymbol);
                                termStore = this.calculateTermLevelTaxesInclusive(termStore, rec);
                            } else {
                                this.getColumnModel().setRenderer(this.getColumnModel().findColumnIndex("amount"),this.calAmountWithoutExchangeRate.createDelegate(this));
                                termStore = this.calculateTermLevelTaxes(termStore, rec,undefined,true);
                            }
                           
                            rec.set('LineTermdetails',Wtf.encode(termStore));
                            this.updateTermDetails();
                        } else if (this.isGST) {
                        /**
                         * ERP-32829 
                         * code for New GST 
                         */
                        if ((prorec == null || prorec == undefined) && productComboIndex >= 0) {
                            var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.data.productid, 'productid');
                            prorec = this.productComboStore.getAt(productComboIndex);
                            productname = prorec.data.productname;
                        } else if ((prorec == null || prorec == undefined)) {
                            prorec = obj.record;
                            productname = prorec.data.productname;
                        }
                        
                        if (this.isModuleForAvalara) {
                            getTaxFromAvalaraAndUpdateGrid(this, obj);
                        }// If terms are already present then calculate only tax values 
                        else if (rec.data.LineTermdetails != '') {
                            calculateUpdatedTaxes(this.parentObj, this, rec);
                            this.updateTermDetails();
                        } else {
                            if (this.parentObj.isPurchasesTransaction && this.parentObj.purchaseFromURD) {
                                /*** If purchases is from Unregistered dealer ***/
                                if (this.parentObj.isRCMApplicableInPreferences && this.parentObj.GTAApplicable.getValue()) {
                                        this.parentObj.uniqueCase = Wtf.GSTCustVenStatus.APPLYGST;
                            } else {
                                        this.parentObj.uniqueCase = Wtf.GSTCustVenStatus.NOGST;
                            }
                        }
                            processGSTRequest(this.parentObj, this, obj.record.data.productid);
                        }
                    } else {
                            rec.set('recTermAmount', 0);
                            rec.set('OtherTermNonTaxableAmount', 0);
                        }
                        this.fireEvent('datachanged',this);
                }    
            }
            if(Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && this.isExciseTab && Wtf.isExciseApplicable && this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId && !Wtf.isEmpty(rec.data.baseuomquantity) && rec.data.baseuomquantity!=0 && !Wtf.isEmpty(rec.data.rate) && rec.data.rate!=0 && Wtf.isEmpty(rec.data.dealerExciseDetails) && Wtf.account.companyAccountPref.registrationType==Wtf.registrationTypeValues.DEALER){ 
                this.dealerExciseDetails(rec,obj);
            }
            /**
             * Below Code checks wether discount value is edited or not if yes then sets the record(discountjson) as empty string
             * as user entered discount value will should be used rather then mapped discount to product
             */
            if (obj.field == "prdiscount" && obj.originalValue != obj.value && rec.data.discountjson!="" && rec.data.discountjson !=undefined) {
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.discount.editdiscountpromptmsg"), function (btn) {
                    if (btn == "yes") {
                        obj.record.data.qtipdiscountstr = "";
                        obj.record.data.discountjson = "";
                    } else {
                        rec.set('prdiscount',obj.originalValue);
                        if (this.isModuleForAvalara) {
                            getTaxFromAvalaraAndUpdateGrid(this, obj);
                        }
                    }
                    obj.grid.getView().refresh();
                }, this);
            }
            /**
             * Below Code checks wether discount type is edited or not if yes then sets the record(discountjson) as empty string
             * as user entered discount type will should be used rather then mapped discount to product
             */
            if (obj.field == 'discountispercent' && obj.originalValue != obj.value && rec.data.discountjson!="" && rec.data.discountjson !=undefined) {
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.discount.editdiscountpromptmsg"), function (btn) {
                    if (btn == "yes") {
                        obj.record.data.qtipdiscountstr = "";
                        obj.record.data.discountjson = "";
                    } else {
                        rec.set('discountispercent',obj.originalValue);
                        if (this.isModuleForAvalara) {
                            getTaxFromAvalaraAndUpdateGrid(this, obj);
                        }
                    }
                    obj.grid.getView().refresh();
                }, this);
            }
            if (Wtf.Countryid == Wtf.Country.INDIA && obj.originalValue != obj.record.get("amount") && obj.field!="desc" && Wtf.isEmpty(obj.record.get("tdsjemappingID"))) { // Anything changed in line item reset TDS data
                obj.record.set("appliedTDS", "");
                obj.record.set("rowdetailid", "");
                obj.record.set("tdsamount", 0);
                this.fireEvent('datachanged',this);
            }
            
            if (obj.field == "taxamount") {
                /*
                 * If user changed the tax amount manually then isUserModifiedTaxAmount flag made true for Adaptive Rounding Algorithm calculataion.
                 * ERM-1085
                 */
                obj.record.set("isUserModifiedTaxAmount", true);
            }
            if (this.parentObj && this.parentObj.includeProTax && this.parentObj.includeProTax.getValue() == true) {
                WtfGlobal.calculateTaxAmountUsingAdaptiveRoundingAlgo(this, false);
                this.fireEvent('datachanged', this);
            }
        }
        /* when "Sync Unit Price from source document if document are Cross Linked" check is on from system preferences
         * and currency of vendor document is different from customer document
         */
        if (this.tempSOPOLinkFlag && Wtf.account.companyAccountPref.carryForwardPriceForCrossLinking && obj.record.data.vendorcurrencyid !="" && obj.record.data.vendorcurrencyid !=undefined && obj.record.data.vendorcurrencyid != this.currencyid) {
            if (obj.record.data.vendorcurrexchangerate != 1) {//VQ is not in base currency
                obj.record.set("rate", obj.record.data.vendorcurrexchangerate * obj.record.data.rate);
            } else {
                obj.record.set("rate", this.rate * obj.record.data.rate);
            }

        }
        if (this.parentObj.includeProTax && this.parentObj.includeProTax.getValue() && this.parentObj.capitalGoodsAcquired && this.parentObj.capitalGoodsAcquired.getValue()) {
            this.parentObj.showGridTax(null, null, false);
        }
        
        if(this.store.getCount()>0&&this.store.getAt(this.store.getCount()-1).data['productid'].length<=0)
            return;
        if(!this.isNote && (!this.soLinkFlag)) {
            this.addBlankRow();            
        }
        
          /* -------------Set Product tax if Product is mapped with any tax--------------- */

        if (CompanyPreferenceChecks.mapTaxesAtProductLevel() && this.moduleid != Wtf.Acc_Purchase_Requisition_ModuleId) {//Insert check of taxid of Product               
            this.showMappedProductTax(rec);            

        }                
    },
     /**
      * if  Amend fuctionalityuser has activated for that user and purchase should 
      * be greater that sales price,If not then system shows pop up msg.
      */
    getamendprice: function (record,prodObj,formSaveFlag,includingGstFlag) {
        if (this.isCustomer && Wtf.productPriceEditPerm.priceEditPerm.BlockAmendingPrice) {
            
            //Purchase Price is not available for  product
            if (record.data.isamendpurchasepricenotavail) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.productList.gridPurchasePrice") +" " +WtfGlobal.getLocaleText("acc.field.isnotset") + " " + WtfGlobal.getLocaleText("acc.field.forproduct") + " " + "<b>" + record.data.pid + "</b>"]);
                record.set("rate", prodObj.originalValue);
                //ERP-40390
                if(includingGstFlag){
                    record.set("rate", prodObj.originalValue);
                    record.set("rateIncludingGst", prodObj.originalValue);
                }
                return true;
            }
            /*
             * Add so correct amended purchase price should get compared to  Unit Price from Invoie Grid SDP-15318
             */
            var amendpurchaseprice = 0;
            if (record.data['amendpurchaseprice'] && record.data['currencyrate']) {
                amendpurchaseprice = getRoundofValueWithValues((parseFloat(record.data['amendpurchaseprice']) * parseFloat(record.data['currencyrate'])), Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
            } else {
                amendpurchaseprice = this.amendpurchaseprice;
            }
            // To check Unit Price from Invoie Grid.
            var gridRate = (record.data.rate !== undefined && record.data.rate !== "")  ? record.data.rate : 0 ;
           //ERP-40390
           if(includingGstFlag){
                gridRate = record.data.rateIncludingGst;
            }
            if (amendpurchaseprice > gridRate) {
                if (!formSaveFlag) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.blockamendingprice") + " For  Product name :- <b>" + record.data['productname'] + "</b>"]);
                }
                if(includingGstFlag){
                   record.set("rateIncludingGst", prodObj.originalValue);
                } else {//Rate should not get reset  
                    record.set("rate", prodObj.originalValue);
                }    
                return true;
            } else {
//                   record.set("rate", gridRate);
                if(includingGstFlag){
                    record.set("rateIncludingGst", gridRate);
                } else {//Rate should not get reset
                    record.set("rate", gridRate);
                }  
            }
            this.fireEvent('datachanged', this);
        }
    },
    showMappedProductTax: function(rec) {

        /* -------------Set Product tax if Product is mapped with any tax--------------- */
        if (this.productComboStore && this.productComboStore.getCount() > 0 && rec != undefined) {
            var taxId = "";
            var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, rec.data.productid, 'productid');
            if (productComboIndex >= 0) {
                var prorec = this.productComboStore.getAt(productComboIndex);
                taxId = this.isCustomer ? prorec.data['salestaxId'] : prorec.data['purchasetaxId'];
                if (taxId != undefined && taxId != "") {
                    this.isLineLevelTaxFieldEditable = false;
                    this.parentObj.showGridTax(null, null, false, true);
                } else {
                    this.isLineLevelTaxFieldEditable = true;
                }

            }
        }
    },
    
    setIndividualProductPriceDetails : function(obj, rec, individualproductprice, quantity, rowRateIncludingGstAmountIndex){
        if (obj.grid) {
            obj.grid.getSelectionModel().clearSelections();
            obj.grid.getSelectionModel().selectRow(obj.row,true);
        }
        var datewiseprice =individualproductprice[0].price;
        this.isPriceListBand = individualproductprice[0].isPriceListBand;
        this.isPriceBandIncludingGst = individualproductprice[0].isIncludingGst;
        this.isVolumeDisocunt = individualproductprice[0].isVolumeDisocunt;
        this.priceSource = individualproductprice[0].priceSource;
        this.pricingbandmasterid=individualproductprice[0].pricingbandmasterid;
        this.isPriceFromUseDiscount = individualproductprice[0].isPriceFromUseDiscount;
        this.priceSourceUseDiscount = individualproductprice[0].priceSourceUseDiscount;
        this.defaultPrice = datewiseprice;
        var pocountinselecteduom = individualproductprice[0].pocountinselecteduom;
        var socountinselecteduom = individualproductprice[0].socountinselecteduom;
        var producttype = individualproductprice[0].producttype;
        var purchaseprice = individualproductprice[0].purchaseprice;
        var amendpurchaseprice = individualproductprice[0].amendpurchaseprice;
        this.isamendpurchasepricenotavail = individualproductprice[0].isamendpurchasepricenotavail;
        var discountType = individualproductprice[0].discountType;
        var discountValue = individualproductprice[0].discountValue;
        this.isBandPriceConvertedFromBaseCurrency = individualproductprice[0].isBandPriceConvertedFromBaseCurrency;
        this.isbandPriceNotAvailable = individualproductprice[0].isbandPriceNotAvailable;
        var displayuomrate1= individualproductprice[0].displayuomrate;
        /**
         * below code creates a json of multiple discounts mapped to specific product and sets in record(discountjson)
         */
        if (CompanyPreferenceChecks.discountMaster()) {
            var jsonArr = createDiscountString(obj, obj.record, this.defaultPrice, obj.record.data.quantity);
            var jsonObj;
            if (jsonArr != "" && jsonArr != undefined) {
                jsonObj = {"data": jsonArr};
            }
            var jsonStr = JSON.stringify(jsonObj);
            obj.record.set("discountjson", jsonStr);
        }else{
            obj.record.set("discountjson", "");
        }
//                        var jsonStr = '{"data":' + JSON.stringify(jsonArr) + '}';
//                        alert(jsonStr);
//                        obj.record.set("discountjson", jsonStr);
//                        this.discountvaluesFlat = [];
//                        this.discountvaluesPercent = [];
//                        var cnt = 0;
//                        var isFlat = false;
//                        var sumFlat = 0;
//                        var sumPercentage = 0;
//                        this.qtipString = "Following are the discount applied : \n<br>";
//                        for (var i = 1; i < response.data.length; i++, cnt++) {
//                            if (response.data[i] != undefined && response.data[i]) {
//                                if (response.data[i].discounttype == 1) {
//                                    this.discountvaluesFlat[cnt] = this.defaultPrice * (response.data[i].discountvalue / 100);
//                                    this.discountvaluesPercent[cnt] = response.data[i].discountvalue;
//                                    sumFlat += this.discountvaluesFlat[cnt];
//                                    sumPercentage += this.discountvaluesPercent[cnt];
//                                } else {
//                                    isFlat = true;
//                                    sumFlat += response.data[i].discountvalue;
//                                }
//                                this.qtipString += getQtipString(response.data[i], i);
//                            }
//                        }
//                        obj.record.set("qtipdiscountstr",this.qtipString);
//                        if (isFlat) {
//                            obj.record.set("prdiscount", sumFlat);
//                            obj.record.set("discountispercent", 0);
//                        } else {
//                            obj.record.set("prdiscount", sumPercentage);
//                            obj.record.set("discountispercent", 1);
//                        }
//                        
        /*
            * set band of customer on product selection
            */
        obj.record.set("pricingbandmasterid", this.pricingbandmasterid);
        if (discountType != undefined && discountValue != undefined) {
            if (discountType == 0 || discountType == "0") {
                obj.record.set("discountispercent", 0);
            } else {
                obj.record.set("discountispercent", 1);
            }
            obj.record.set("prdiscount", discountValue);
        }

        obj.record.set("oldcurrencyrate",1);

        if (obj.grid) {
            for (var i = 1; i < individualproductprice.length; i++) {
                var dataObj = individualproductprice[i];
                var key = dataObj.key;
                if(key!=undefined){
                    for (var k = 0; k < obj.grid.colModel.config.length; k++) {
                        if (obj.grid.colModel.config[k].dataIndex == key) {
                            var editor=obj.grid.colModel.config[k].editor;
                            if(editor && editor.field.store){
                                var  store = editor.field.store;

                                if (store)
                                    store.clearFilter();
                            }
                            obj.record.set(key, dataObj[key]);  
                        }
                    }
                }
            }   
        }

        if(obj.record.data.productCode){
            obj.record.set("pid",obj.record.data.productCode);
        }
        //                  this.tempSOPOLinkFlag = this.soLinkFlag;  //used for cross linking
        var baseuomRate=1;
        var baseuomquantity=1;
        if(this.tempSOPOLinkFlag== null || this.tempSOPOLinkFlag== false){
            var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.value, 'productid');
            var productname = "";
            var proddescription = "";
            var prodparentid = "";
            var isparentproduct = false;
            var addshiplentheithqty = false;
            var prodparentname = "";
            var productuomid = undefined;
            var productsuppliernumber = "";
            var shelfLocation = "";
            var prorec = null;
            //                  var acctaxcode = (this.isCustomer)?"salesacctaxcode":"purchaseacctaxcode";
            //                  var protaxcode = "";
            var productLocation = "";
            var productWarehouse = "";
            var productpid = "";  
            var ComboIndex=0;
            if (this.productOptimizedFlag == Wtf.Products_on_Submit) {
                productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.value.trim(), 'pid');
            } else if (productComboIndex == -1) {
                productComboIndex = 1;
                ComboIndex = -1;
            } 
            if(productComboIndex >=0){
                prorec = this.productComboStore.getAt(productComboIndex);
                if(ComboIndex==-1){
                    prorec=rec;
                }
                productname = prorec.data.productname;
                proddescription = prorec.data.desc; 
                /*For SATS*/
                if(SATSCOMPANY_ID==companyid){
                    prodparentid = prorec.data.parentid;
                    prodparentname = prorec.data.parentname;
                    isparentproduct = prorec.data.isparentproduct;
                    addshiplentheithqty = prorec.data.addshiplentheithqty;
                }
                /**********/
                obj.record.set("baseuomrate", 1);
                if(this.UomSchemaType==Wtf.UOMSchema){//for Schema type
                    productuomid = prorec.data.uomid;
                    obj.record.set("baseuomid", prorec.data.uomid);
                    obj.record.set("baseuomname", prorec.data.uomname);
                    obj.record.set("displayUoMid", prorec.data.displayUoMid);
                    obj.record.set("displayUoMName", prorec.data.displayUoMName);
                    obj.record.set("uomschematypeid", prorec.data.uomschematypeid);
                }else{//for packeging UOM type
                    productuomid =this.isCustomer? prorec.data.salesuom:prorec.data.purchaseuom;
                    //                          productuomname =this.isCustomer? prorec.data.salesuomname:prorec.data.purchaseuomname;
                    baseuomRate=this.isCustomer? prorec.data.stocksalesuomvalue:prorec.data.stockpurchaseuomvalue;
                    if(obj.record.get("uomid")!=undefined && productuomid != prorec.data.uomid){
                        baseuomquantity=quantity*baseuomRate;
                        obj.record.set("baseuomquantity", baseuomquantity);
                        obj.record.set("baseuomrate", (baseuomRate));          
                        obj.record.set("displayuomrate", individualproductprice[0].displayuomrate);
                    } else {
                        obj.record.set("baseuomquantity", quantity);
                        obj.record.set("baseuomrate", 1);
                    } 
                    obj.record.set("caseuom", prorec.data['caseuom']);
                    obj.record.set("inneruom", prorec.data['inneruom']);
                    obj.record.set("stockuom", prorec.data['uomid']);
                    obj.record.set("caseuomvalue", prorec.data['caseuomvalue']);
                    obj.record.set("inneruomvalue", prorec.data['inneruomvalue']);
                }
                obj.record.set("displayuomrate",displayuomrate1) ;
                productsuppliernumber= prorec.data.supplierpartnumber;
                shelfLocation = prorec.data.shelfLocation;
                //                      protaxcode = prorec.data[acctaxcode];
                //                    productLocation = prorec.data.location;
                //                    productWarehouse = prorec.data.warehouse;
                //                    productpid = prorec.data.pid;
                obj.record.set("multiuom", prorec.data['multiuom']);
                if (!obj.ispreferredProducts) {
                    obj.record.set("availableQtyInSelectedUOM", prorec.data['quantity']);
                    obj.record.set("availablequantity", prorec.data['quantity']);
                }

                obj.record.set("uomname", prorec.data['uomname']);
                obj.record.set("displayUoMName", prorec.data['displayUoMName']);
                obj.record.set("displayUoMid", prorec.data['displayUoMid']);
                obj.record.set("blockLooseSell", prorec.data['blockLooseSell']);
                obj.record.set("pocountinselecteduom", pocountinselecteduom);
                obj.record.set("productaccountid", this.isCustomer ? prorec.data.productsalesaccountid : prorec.data.productpurchaseaccountid);
                obj.record.set("socountinselecteduom", socountinselecteduom);
                obj.record.set("isLocationForProduct", prorec.data['isLocationForProduct']);
                obj.record.set("isWarehouseForProduct", prorec.data['isWarehouseForProduct']);
                obj.record.set("isBatchForProduct", prorec.data['isBatchForProduct']);
                obj.record.set("isSerialForProduct", prorec.data['isSerialForProduct']);
                obj.record.set("isFromVendorConsign", prorec.data['isFromVendorConsign']);
                obj.record.set("isRowForProduct", prorec.data['isRowForProduct']);
                obj.record.set("bomcode", prorec.data['defaultbomcode']);
                obj.record.set("bomid", prorec.data['defaultbomid']);
                obj.record.set("isRackForProduct", prorec.data['isRackForProduct']);
                obj.record.set("isBinForProduct", prorec.data['isBinForProduct']);  
                obj.record.set("location", prorec.data["location"]);
                obj.record.set("warehouse", prorec.data["warehouse"]);
                obj.record.set("type",prorec.data["type"]);
                obj.record.set("purchaseprice", purchaseprice); 
                obj.record.set("amendpurchaseprice", amendpurchaseprice); 
                obj.record.set("isamendpurchasepricenotavail", this.isamendpurchasepricenotavail); 
                obj.record.set("isAutoAssembly", prorec.data["isAutoAssembly"]); 
                obj.record.set("sicount", prorec.data["sicount"]); 
                obj.record.set("displayUoMName",prorec.data["displayUoMName"])
                if(Wtf.account.companyAccountPref.calculateproductweightmeasurment){
                    obj.record.set("productweightperstockuom", prorec.data['productweightperstockuom']);
                    obj.record.set("productweightincludingpakagingperstockuom", prorec.data['productweightincludingpakagingperstockuom']);  
                    obj.record.set("productvolumeperstockuom", prorec.data['productvolumeperstockuom']);
                    obj.record.set("productvolumeincludingpakagingperstockuom", prorec.data['productvolumeincludingpakagingperstockuom']);  
                } else {
                    obj.record.set("productweightperstockuom", 0);
                    obj.record.set("productweightincludingpakagingperstockuom", 0);
                    obj.record.set("productvolumeperstockuom", 0);
                    obj.record.set("productvolumeincludingpakagingperstockuom", 0);
                }
            }
            if(!this.tempSOPOLinkFlag){//ERP-11359
                obj.record.set("desc","");
            } 
            obj.record.set("desc",obj.record.get("desc")!=""?obj.record.get("desc"):proddescription);//ERP-11359
            if (GlobalProductmasterFieldsArr[this.moduleid] != undefined) {
                var productdefaultfieldsarr = GlobalProductmasterFieldsArr[this.moduleid];
                for (var i = 0; i < productdefaultfieldsarr.length; i++) {
                    var column = productdefaultfieldsarr[i];
                    obj.record.set(column.fieldname, prorec.get(column.fieldname));
                }
            }
            obj.record.set("uomid", productuomid);
            obj.record.set("supplierpartnumber",productsuppliernumber);
            obj.record.set("shelfLocation",shelfLocation);
            //                  obj.record.set("pid",productpid);
            obj.record.set("productname",productname);
            if (producttype == Wtf.producttype.service && quantity == "") {
                obj.record.set("quantity", 1);
            }
        //                  obj.record.set("invlocation",productLocation);
        //                  obj.record.set("invstore", productWarehouse);
        }
        var isAutoGenerateDO=false;
        if(Wtf.getCmp(this.parentCmpID) != undefined  && Wtf.getCmp(this.parentCmpID).autoGenerateDO != undefined ){
                if(Wtf.getCmp(this.parentCmpID).autoGenerateDO.checked == true){
                    isAutoGenerateDO=true;
                }
        }
        this.addorRemoveBomCodeColumn(prorec,isAutoGenerateDO,this.store);
        /*For SATS*/
        var unitpriceaspertariff = -1;
        if(SATSCOMPANY_ID==companyid){
            obj.record.set("isparentproduct",isparentproduct);
            obj.record.set("parentid",prodparentid);
            obj.record.set("parentname",prodparentname);
            obj.record.set("addshiplentheithqty",addshiplentheithqty);
            if(prorec && Wtf.account.companyAccountPref.dependentField){
                var custValue=prorec.data.dependentType; //Added new rate according to Combo type value
                var hourtimeinterval=prorec.data.hourtimeinterval;
                if(custValue!=""){  
                    var parentDependent=prorec.data.parentDependentType;
                    this.dependentTypeStore.filterBy(function(rec) {
                        var recId = rec.data.type;
                        if ((parentDependent.indexOf(recId) !== -1))
                            return true;
                        else
                            return false;
                    }, this);
                    var depRecId=""
                    if(this.dependentTypeStore.getCount()>0){
                        var deprec=this.dependentTypeStore.getAt(0);
                        depRecId=deprec.data.id;
                        unitpriceaspertariff=deprec.data.price;
                    }
                    this.dependentTypeStore.clearFilter()
                    obj.record.set("dependentType",depRecId);
                } 
                obj.record.set("hourtimeinterval",hourtimeinterval);
                custValue=prorec.data.dependentTypeNo;
                var custValue1=prorec.data.dependentTypeQty;
                if(custValue!="" || custValue1!=""){// Added new rate according to Number and Water terrif type value
                    Wtf.Ajax.requestEx({
                        url: "ACCGoodsReceipt/getMasterItemPriceFormulaPrice.do",
                        params: {
                            productId: obj.record.data.productid,
                            item:(custValue1!="")?obj.record.data.quantity:Wtf.getCmp(this.parentCmpID).shipLength.getValue(),
                            iscalculatefromqty:(custValue1!="")
                        }
                    }, this, function(response) {
                        var datewiseprice1=obj.record.data.rate;
                        for (var i = 0; i < response.data.length; i++) {
                            var dataObj = response.data[i];
                            if(dataObj.pricevalue>0){
                                datewiseprice1=dataObj.pricevalue;
                            }
                            obj.record.set("rate", datewiseprice1);
                            this.fireEvent('datachanged',this);
                        }
                    } , function(){
                        });
                }
            }
        }
        /**********/

        if (this.isVolumeDisocunt) {
            if (obj.record.data.quantity != "") {
                if (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden) {
                    obj.record.set("rateIncludingGst", this.defaultPrice);
                    if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                        this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                    } else {
                        this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                    }
                } else {
                    obj.record.set("rate", this.defaultPrice);
                }
                obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSource);
                obj.record.set("pricingbandmasterid", this.pricingbandmasterid);
            } else {
                obj.record.set("rate", "");
                obj.record.set("priceSource", "");
            }
        } else if (this.isPriceListBand) {
            if (this.isPriceFromUseDiscount) {
                if (obj.record.data.quantity != "") {
                    if (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden) {
                        obj.record.set("rateIncludingGst", this.defaultPrice);
                        if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                            this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                        } else {
                            this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                        }
                    } else {
                        obj.record.set("rate", this.defaultPrice);
                    }
                    obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSourceUseDiscount + ": " + WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                    obj.record.set("pricingbandmasterid", this.pricingbandmasterid);
                } else {
                    obj.record.set("rate", "");
                    obj.record.set("priceSource", "");
                }
            } else {
                if (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden) {
                    obj.record.set("rateIncludingGst", this.defaultPrice);
                    if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                        this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                    } else {
                        this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                    }
                } else {
                    obj.record.set("rate", this.defaultPrice);
                }
                obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                obj.record.set("pricingbandmasterid", this.pricingbandmasterid);
            }
            this.checkForPricebandIncludingGst(obj) ;      //ERP-17723
            // alert if band price converted from base currency
            if (this.isBandPriceConvertedFromBaseCurrency) {
                var currencycode = "";
                var index = this.parentObj.getCurrencySymbol();
                if (index >= 0) {
                    currencycode = this.parentObj.currencyStore.getAt(index).data.currencycode;
                }
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), currencycode + " " + WtfGlobal.getLocaleText("acc.bandPriceForSelectedCurrencyNotAvailable.msg")], 2);
            }
    } else {
            if(datewiseprice==0){
                var rateIndex = this.getColumnModel().findColumnIndex("rate");
                if (!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.addprice)) { // permissions
                    if(productname!=undefined){
                        rec.set("productname",productname);
                    }
                    /*
                     * Hndled below case for SDP-13318 - If the unit price is hidden, the unit price i.e. rate is being saved as 0 now.
                     * This case is handled for not displaying the pop-up "Rate for Product --- cannot be empty" in case of Purchase Requisition
                     * if the unit price is hidden
                     */
                    if(this.isRequisition) {
                        if(this.getColumnModel().config[rateIndex] != undefined && this.getColumnModel().config[rateIndex] != null && this.getColumnModel().config[rateIndex] != "") {
                            if(this.getColumnModel().config[rateIndex].hidden) {
                                obj.record.set("rate", "0");
                            } else {
                    obj.record.set("rate", "");
                            }
                        }
                    } else {
                        obj.record.set("rate", "");
                    }
                    obj.record.set("priceSource", "");
                //                          Wtf.Msg.confirm(WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pricefortheproduct")+"<b>"+productname+"</b>"+" "+WtfGlobal.getLocaleText("acc.field.isnotsetDoyouwanttosetitnow"),
                //                                this.showPriceWindow.createDelegate(this,[rec, obj],true), this);
                } 
                if(this.getColumnModel().config[rateIndex] != undefined && this.getColumnModel().config[rateIndex] != null && this.getColumnModel().config[rateIndex] != "") {
                    if(!this.getColumnModel().config[rateIndex].hidden) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pricefortheproduct")+"<b>"+productname+"</b>"+" "+WtfGlobal.getLocaleText("acc.field.isnotset")], 2);
                    }
                }
                   
            } else {
                // setting datewise price according to currency exchange rate - 
                if (!Wtf.account.companyAccountPref.productPriceinMultipleCurrency && this.parentObj && this.parentObj.Currency.getValue() != WtfGlobal.getCurrencyID()) { // If product in Multiple currency is not set in account preferences and selected currency is in other than base currency
                    var rate=((rec==undefined||rec.data['currencyrate']==undefined||rec.data['currencyrate']=="")?1:rec.data['currencyrate']);
                    var oldcurrencyrate=((rec==undefined||rec.data['oldcurrencyrate']==undefined||rec.data['oldcurrencyrate']=="")?1:rec.data['oldcurrencyrate']);
                    var modifiedRate;
                    if(rate!=0.0)
                        modifiedRate=getRoundofValueWithValues(((parseFloat(datewiseprice)*parseFloat(rate))/parseFloat(oldcurrencyrate)),Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                    else
                        modifiedRate=getRoundofValueWithValues((parseFloat(datewiseprice)/parseFloat(oldcurrencyrate)),Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                }else{
                    modifiedRate=datewiseprice;
                    /*For SATS*/
                    if(Wtf.account.companyAccountPref.dependentField && unitpriceaspertariff!=-1){
                        modifiedRate = unitpriceaspertariff;
                    }
                /**********/
                }

                if (this.isPriceFromUseDiscount) {
                    if (obj.record.data.quantity != "") {
                        obj.record.set("rate", this.defaultPrice);
                        obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSourceUseDiscount);
                        obj.record.set("pricingbandmasterid", this.pricingbandmasterid);
                    } else {
                        obj.record.set("rate", "");
                        obj.record.set("priceSource", "");
                    }
                } else {
                    if(this.UomSchemaType==Wtf.UOMSchema){//if not matches means product has packageing uom
                        if(this.tempSOPOLinkFlag == null || this.tempSOPOLinkFlag == false){
                            obj.record.set("rate", modifiedRate);
                            obj.record.set("rateIncludingGst", modifiedRate);
                        }else{//PO Linkeed in So case where unit price is calculated for as Sales price * Base uom rate for PO
                            obj.record.set("rate", modifiedRate*obj.record.data.baseuomrate);
                            obj.record.set("rateIncludingGst", modifiedRate*obj.record.data.baseuomrate);
                        }

                    }else if(this.tempSOPOLinkFlag == true){//it will be true only in case of cross linking i.e PO link in SO, VQ link in CQ etc
                        obj.record.set("rate", modifiedRate*obj.record.data.baseuomrate);
                        obj.record.set("rateIncludingGst", modifiedRate*obj.record.data.baseuomrate);
                    } else{
                        obj.record.set("rate", modifiedRate*baseuomRate);
                        obj.record.set("rateIncludingGst", modifiedRate*baseuomRate);
                    }
                }

            }
            var rateIndex = this.getColumnModel().findColumnIndex("rate");
            // alert if band price not available
            if (this.isbandPriceNotAvailable && (rateIndex !=-1 && !this.getColumnModel().config[rateIndex].hidden)) {
                if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                    if (!((this.moduleid == Wtf.Acc_Sales_Order_ModuleId && Wtf.account.companyAccountPref.negativeStockSO == 2) || ((this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId) && Wtf.account.companyAccountPref.negativeStockSICS == 2))) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.DefaultUnitPriceIsnotsetPriceband")], 2);
                    }
                } else{
                    if (!((this.moduleid == Wtf.Acc_Sales_Order_ModuleId && Wtf.account.companyAccountPref.negativeStockSO == 2) || ((this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId) && Wtf.account.companyAccountPref.negativeStockSICS == 2))) {
                            var rateIndex = this.getColumnModel().findColumnIndex("rate");
                            if (this.getColumnModel().config[rateIndex] != undefined || this.getColumnModel().config[rateIndex] != null || this.getColumnModel().config[rateIndex] != "") {
                                if (!this.getColumnModel().config[rateIndex].hidden) {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.bandPriceNotAvailable.msg")], 2);
                                }
                            }
                        }
                    }
                }   
            }

        if(!(obj.soflag)){
            obj.record.set("baseuomquantity",baseuomquantity);
            //                        obj.record.set("quantity",1);
            if(this.parentObj && this.parentObj.includeProTax && this.parentObj.includeProTax.getValue() == true) {
                var taxid = "";
                var currentTaxItem=WtfGlobal.searchRecord(this.parentObj.Name.store, this.parentObj.Name.getValue(), 'accid');
                var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";

                if(actualTaxId== undefined || actualTaxId == "" ||  actualTaxId == null){// if customer/vendor is not mapped with tax then check that is their mapping account is mapped with tax or not, if it is mapped take account tax
                    actualTaxId=currentTaxItem!=null?currentTaxItem.get('mappedAccountTaxId'):"";
                }

                if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null){
                    taxid = actualTaxId;
                }
                
               /*--------If tax mapped at Product level then no need to set tax again here As already tax has been set ---------- */
                if (!CompanyPreferenceChecks.mapTaxesAtProductLevel() && !this.parentObj.includeProTax.getValue()) {
                    obj.record.set("prtaxid", taxid);
                }
                  
            } else {
                obj.record.set("prtaxid", "");
            }
            var taxamount = this.setTaxAmountAfterSelection(obj.record);
            obj.record.set("taxamount",taxamount);
            obj.record.set("isUserModifiedTaxAmount", false);
            if(!obj.isAddProductsFromWindow){
                this.fireEvent("productselect", obj.value);
            }
        }
        this.fireEvent('datachanged',this);
        /*For SATS*/
        if(SATSCOMPANY_ID==companyid){
            if(Wtf.account.companyAccountPref.dependentField){
                this.addSATSSubProducts(obj);
            }
        }

        /*
            Line Level terms are reloaded with the sales price while cross linking 
            document is line level terms are applied for company  
            */

        if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
            obj.record.set('recTermAmount',0);
            if(obj.record.get('LineTermdetails') != undefined && obj.record.get('LineTermdetails') != ''){
                var termStore = this.getTaxJsonOfIndia(obj.record);
                if(this.parentObj && this.parentObj.includingGST && this.parentObj.includingGST.getValue() == true) {
                    this.getColumnModel().setRenderer(this.getColumnModel().findColumnIndex("amount"),WtfGlobal.withoutRateCurrencySymbol);
                    termStore = this.calculateTermLevelTaxesInclusive(termStore, obj.record);
                } else {
                    this.getColumnModel().setRenderer(this.getColumnModel().findColumnIndex("amount"),this.calAmountWithoutExchangeRate.createDelegate(this));
                    termStore = this.calculateTermLevelTaxes(termStore, obj.record,undefined,true);
                }

                obj.record.set('LineTermdetails',Wtf.encode(termStore));
                /*
                Calculated other terms which are not taxable  
                */
                var OtherTermNonTaxableAmount = getRoundedAmountValue(0.0);
                for(var termCnt = 0; termCnt < termStore.length ; termCnt++){
                    var object = termStore[termCnt];
                    if(object && object.termtype==Wtf.term.Others && !object.IsOtherTermTaxable){
                        OtherTermNonTaxableAmount += getRoundedAmountValue(object.termamount);
                    }
                    }
                if(OtherTermNonTaxableAmount){
                    obj.record.set('OtherTermNonTaxableAmount',OtherTermNonTaxableAmount);
                }
                this.updateTermDetails();
                // updated subtotla xtemplate
                this.parentObj.updateSubtotalOnTermChange(true,true,obj.record.data.termamount);
            }

        }
        if (this.isModuleForAvalara && this.isGST) {
            getTaxFromAvalaraAndUpdateGrid(this, obj);
        }
    /**********/
    },
    
    updateTermDetails: function(){
        var  LineTermdetails;
        var  LineTermTypeJson = {};
        if(Wtf.isEmpty(this.symbol)){
            this.parentObj.applyCurrencySymbol();
        }
        var lineLevelArray = [];
        var TotalTaxAmt = 0;
        for(var m=0 ; m < (this.store.data.length) ; m++){
            LineTermdetails = eval(this.store.data.itemAt(m).data.LineTermdetails);
            if(LineTermdetails != undefined && LineTermdetails != ""){
                //Already Defined. [[1,'VAT'],[2,'Excise Duty'],[3,'CST'],[4,'Service Tax'],[5,'Swachh Bharat Cess'],[6,'Krishi Kalyan Cess']]
                for(var n = 0 ; n < LineTermdetails.length ; n++){
                    var prevAmt = 0;
                    if(LineTermdetails[n].termtype ==Wtf.term.Others && (LineTermdetails[n].IsOtherTermTaxable!=undefined && !LineTermdetails[n].IsOtherTermTaxable)){
                        continue;
                    }
                    if(LineTermTypeJson.hasOwnProperty(LineTermdetails[n].termtype)){
                        prevAmt = LineTermTypeJson[LineTermdetails[n].termtype];
                    }
                    LineTermTypeJson[LineTermdetails[n].termtype] = prevAmt + LineTermdetails[n].termamount;
                    /**
                     * Add GST master details 
                     * ERP-32829
                     */
                    if (LineTermdetails[n].termtype == Wtf.term.GST) {
                        var isAlreadyexist=false;
                        for(var arr=0;arr<lineLevelArray.length;arr++){
                            var arrrec=lineLevelArray[arr];
                            if(arrrec.name==LineTermdetails[n].term){
                                var tempamt=arrrec.preamount;
                                arrrec.taxAmount=WtfGlobal.addCurrencySymbolOnly(tempamt+LineTermdetails[n].termamount, this.symbol);
                                arrrec.preamount=tempamt+LineTermdetails[n].termamount;
                                TotalTaxAmt += LineTermdetails[n].termamount;
                                isAlreadyexist=true;
                            }
                        }
                        if (!isAlreadyexist) {
                            var temp = {};
                            temp.name = LineTermdetails[n].term;
                            temp.preamount=LineTermdetails[n].termamount;
                            TotalTaxAmt += LineTermdetails[n].termamount;
                            temp.taxAmount = WtfGlobal.addCurrencySymbolOnly(LineTermdetails[n].termamount, this.symbol);
                            lineLevelArray.push(temp);
                        }
                    }
                }
            }
        }
        // Below code is used in old taxaction India Compliance.
        /*var TotalTaxAmt = 0;
        for(var i=0; i<Wtf.LineTermsMasterStore.getRange().length; i++){
            var temp = Wtf.LineTermsMasterStore.getRange()[i].data;
            var forModule =(this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid==Wtf.Acc_Purchase_Order_ModuleId || this.moduleid==Wtf.Acc_Vendor_Quotation_ModuleId || this.moduleid== Wtf.Acc_Cash_Purchase_ModuleId);
            if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && !this.isExciseTab && !Wtf.isEmpty(this.parentObj.GTAApplicable) && this.parentObj.GTAApplicable.getValue() && ( temp.typeid==Wtf.term.Service_Tax|| temp.typeid==Wtf.term.Swachh_Bharat_Cess || temp.typeid==Wtf.term.Krishi_Kalyan_Cess || temp.typeid == Wtf.term.GST)  &&!Wtf.isEmpty(this.parentObj.Name.getValue()) &&  forModule){ // GTA For India country only 
                var gtaApplicable = this.parentObj.GTAApplicable.getValue();
                if(!this.isCustomer && gtaApplicable){// While changing in Vendor invoice's line grid, it update term details but it must bypass service taxes in case of GTA applicable
                    continue;
                }
            }
            if(LineTermTypeJson.hasOwnProperty(temp.typeid)){
                TotalTaxAmt += LineTermTypeJson[temp.typeid];
                temp['taxAmount'] = WtfGlobal.addCurrencySymbolOnly(LineTermTypeJson[temp.typeid],this.symbol);
            } else {
                temp['taxAmount'] = WtfGlobal.addCurrencySymbolOnly(0,this.symbol);
            }
            if (temp.typeid != Wtf.term.GST) {
                lineLevelArray.push(temp);
            }
        }*/
        this.parentObj.LineLevelTermTplSummary.overwrite(this.parentObj.LineLevelTermTpl.body,{
            lineLevelArray : lineLevelArray,
            TotalTaxAmt : WtfGlobal.addCurrencySymbolOnly(TotalTaxAmt,this.symbol)
        });
    },
    getComboNameRenderer : function(combo){
        return function(value,metadata,record,row,col,store) {
            var idx = WtfGlobal.searchRecordIndex(combo.store,value,combo.valueField);
            var fieldIndex = "pid";
            if(idx == -1) {
                if(record.data["pid"] && record.data[fieldIndex].length>0) {
                    return record.data[fieldIndex];
                }
                else
                    return "";
            }
            var rec = combo.store.getAt(idx);
            var displayField = rec.get(combo.displayField);
            record.set("productid", value);
            record.set("pid", displayField);
            return displayField;
        }
    },
    callSerialNoWindow:function(obj){//if autogenerate flag is true then show serial no
        var documentid = "";
        documentid = (this.isEdit)?obj.data.docrowid:obj.data.rowid
        var index=this.productComboStore.findBy(function(rec){
            if(rec.data.productid==obj.data.productid)
                return true;
            else
                return false;
        })
        var firstRow=index;
        if(index== -1){
            index=this.store.findBy(function(rec){
                if(rec.data.productid==obj.data.productid)
                    return true;
                else
                    return false;
            })
        }
        if(index!=-1){ 
            var deliveredprodquantity = obj.data.quantity;
            deliveredprodquantity = (deliveredprodquantity == "NaN" || deliveredprodquantity == undefined || deliveredprodquantity == null)?0:deliveredprodquantity;

            var prorec=this.productComboStore.getAt(index); 
            if(firstRow==-1){
                prorec=obj;
            }
            var islocationavailble=false;
          var productsDefaultLocation="";
        if(prorec.data.isLocationForProduct && prorec.data.location!="" && prorec.data.location!=undefined){
            islocationavailble=true;
            productsDefaultLocation=prorec.data.location;
        }else if(!prorec.data.isLocationForProduct){
            islocationavailble=true;
        }
        
        var iswarehouseavailble=false;
         var productsDefaultWarehouse="";
        if(prorec.data.isWarehouseForProduct && prorec.data.warehouse && prorec.data.warehouse!=undefined){
            iswarehouseavailble=true;
            productsDefaultWarehouse=prorec.data.warehouse;
        }else if(!prorec.data.isWarehouseForProduct){
            iswarehouseavailble=true;
        }
            var filterJson='[';
              filterJson+='{"location":"'+productsDefaultLocation+'","warehouse":"'+productsDefaultWarehouse+'","productid":"'+prorec.data.productid+'","documentid":"'+documentid+'","purchasebatchid":""},';
                    filterJson=filterJson.substring(0,filterJson.length-1);
             filterJson+="]";

      if(this.isCustomer && (islocationavailble || iswarehouseavailble) ){ //if salesside and either default location and warehouse  then checkit
                  Wtf.Ajax.requestEx({
                    url: "ACCInvoice/getBatchRemainingQuantity.do",
                    params: {
                        batchdetails:(this.isEdit && !this.copyTrans)?obj.data.batchdetails:filterJson,
                        transType:this.moduleid,
                        isEdit:this.isEdit,
                        linkflag:this.blockQtyFlag,
                        documentid:((this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId) && this.isEdit)?documentid : ""     
                    }
                },this,function(res,req){
                    this.AvailableQuantity=res.quantity;
                      this.CallSerialnoDetailsWindow(obj);
                      return;
                },function(res,req){
                    return false;
                });
            }else{
           this.CallSerialnoDetailsWindow(obj);
        }
       }
    },
    CallSerialnoDetailsWindow:function(obj){
          this.recAobj = obj;
          var index=this.productComboStore.findBy(function(rec){
            if(rec.data.productid==obj.data.productid)
                return true;
            else
                return false;
         })
        var firstRow=index;
        if(index== -1){
            index=this.store.findBy(function(rec){
                if(rec.data.productid==obj.data.productid)
                    return true;
                else
                    return false;
            })
        }
        var deliveredprodquantity = obj.data.quantity;
        deliveredprodquantity = (deliveredprodquantity == "NaN" || deliveredprodquantity == undefined || deliveredprodquantity == null)?0:deliveredprodquantity;

       if(deliveredprodquantity<=0){
            WtfComMsgBox(["Info","Quantity should be greater than zero. "], 2);
            return false;
        }
        if(index!=-1){ 
              var prorec=this.productComboStore.getAt(index); 
            if(firstRow==-1){
                prorec=obj;
            }
            
            var isOnlySerialForProduct= !( prorec.data.isLocationForProduct ||prorec.data.isWarehouseForProduct || prorec.data.isRowForProduct || prorec.data.isRackForProduct || prorec.data.isBinForProduct || prorec.data.isBatchForProduct ) &&  prorec.data.isSerialForProduct;
            this.isOnlyBatchForProduct= !(prorec.data.isRowForProduct || prorec.data.isRackForProduct || prorec.data.isBinForProduct || prorec.data.isSerialForProduct ) &&  prorec.data.isBatchForProduct;   //checking wether the product is only of batch type ERM-319
            this.allowUserToEditQuantity = false;
            var isQuantityLockedInSOLinkedInSI = false;
            var isLinkedFromSO = false;
            /**
             * Checking whether Invoice is linked to SO or not.ERM-319
             */
            if (this.parentObj && this.parentObj.fromLinkCombo && this.parentObj.fromLinkCombo.getValue() == 0) {
                isLinkedFromSO = true;
            }
            /**
             * If Variable Purchase/Sales UOM conversion rate check is enable in company preferences and inovice is linked to SO 
             * than checking whether Qty is blocked in SO or not if Qty is blocked in SO than we do not have to allow user 
             * to edit Qty in batch serial window.ERM-319
             */
            if (CompanyPreferenceChecks.differentUOM() && (!isLinkedFromSO || this.moduleid != Wtf.Acc_Invoice_ModuleId || (isLinkedFromSO && !obj.data.islockQuantityflag))) {
                this.allowUserToEditQuantity = true;
            }      
           if( ! (this.isAutoFilledBatchDetails) || isOnlySerialForProduct || !this.isCustomer ){
           this.batchDetailswin=new Wtf.account.SerialNoWindow({
                renderTo: document.body,
                title:WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber"),
                productName:prorec.data.productname,
                barcodetype:prorec.data.barcodetype,
                productcode:prorec.data.pid,
                uomName:prorec.data.uomname,
                productUomid:prorec.data.uomid,
                selectedProductUomid:obj.data.uomid,
                type:prorec.data.type,
                //quantity:obj.data.dquantity,
                quantity:(obj.data.baseuomrate)*(obj.data.quantity),
                defaultLocation:prorec.data.location,
                productid:prorec.data.productid,
                isJobWorkOrder: this.isJobWorkOrderReciever,
                isSales:this.isCustomer,
                moduleid:this.moduleid,//this.isCustomer?Wtf.Acc_Delivery_Order_ModuleId:Wtf.Acc_Goods_Receipt_ModuleId,
                transType:this.isCustomer?Wtf.Acc_Delivery_Order_ModuleId:Wtf.Acc_Goods_Receipt_ModuleId,
                defaultAvailbaleQty:this.AvailableQuantity,
                isEdit:this.isEdit,
                isDO:this.isCustomer?true:false,
                defaultWarehouse:prorec.data.warehouse,
                batchDetails:obj.data.batchdetails,
                warrantyperiod:prorec.data.warrantyperiod,
                warrantyperiodsal:prorec.data.warrantyperiodsal,  
                isLocationForProduct:prorec.data.isLocationForProduct,
                isWarehouseForProduct:prorec.data.isWarehouseForProduct,
                isRowForProduct:prorec.data.isRowForProduct,
                isRackForProduct:prorec.data.isRackForProduct,
                isBinForProduct:prorec.data.isBinForProduct,
                copyTrans:this.copyInv,
                isBatchForProduct:prorec.data.isBatchForProduct,
                isOnlyBatchForProduct:this.isOnlyBatchForProduct,
                isSerialForProduct:prorec.data.isSerialForProduct,
                isSKUForProduct:prorec.data.isSKUForProduct,
                isShowStockType:(this.isCustomer)?true:false,
                transactionid:(this.isCustomer)?4:5,
                width:950,
                readOnly:this.readOnly,
                height:400,
                parentObj: this.parentObj,
                resizable : false,
                modal : true,
                lineRec:obj,
                isSalesOrder:this.moduleid==Wtf.Acc_Sales_Order_ModuleId?true:false,
                parentGrid:this,
                linkflag:this.blockQtyFlag,
                allowUserToEditQuantity:this.allowUserToEditQuantity,        //Passing this parameter to check whether we have to allow user to edit Qty in batch serial window.ERM-319
                linkedFrom:this.parentObj.fromLinkCombo.getValue(),
                documentid:(this.isEdit && (this.moduleid== Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid==Wtf.Acc_Cash_Purchase_ModuleId))?obj.data.grorowid:obj.data.rowid,
                docrowid:obj.data.docrowid,
                bomid:this.bomcombo.value==undefined ? obj.data.bomid : this.bomcombo.value
            });
        }else{
                this.batchDetailswin=new Wtf.account.SerialNoAutopopulateWindow({
                    renderTo: document.body,
                    title:WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber"),
                    productName:prorec.data.productname,
                    barcodetype:prorec.data.barcodetype,
                    productcode:prorec.data.pid,
                    uomName:prorec.data.uomname,
                    productUomid:prorec.data.uomid,
                    selectedProductUomid:obj.data.uomid,
                    type:prorec.data.type,
                    //quantity:obj.data.dquantity,
                    quantity:(obj.data.baseuomrate)*(obj.data.quantity),
                    defaultLocation:prorec.data.location,
                    productid:prorec.data.productid,
                    isSales:this.isCustomer,
                    moduleid:this.moduleid,//this.isCustomer?Wtf.Acc_Delivery_Order_ModuleId:Wtf.Acc_Goods_Receipt_ModuleId,
                    transType:this.isCustomer?Wtf.Acc_Delivery_Order_ModuleId:Wtf.Acc_Goods_Receipt_ModuleId,
                    defaultAvailbaleQty:this.AvailableQuantity,
                    isEdit:this.isEdit,
                    isDO:this.isCustomer?true:false,
                    defaultWarehouse:prorec.data.warehouse,
                    batchDetails:obj.data.batchdetails,
                    warrantyperiod:prorec.data.warrantyperiod,
                    warrantyperiodsal:prorec.data.warrantyperiodsal,  
                    isLocationForProduct:prorec.data.isLocationForProduct,
                    isWarehouseForProduct:prorec.data.isWarehouseForProduct,
                    isRowForProduct:prorec.data.isRowForProduct,
                    isRackForProduct:prorec.data.isRackForProduct,
                    isBinForProduct:prorec.data.isBinForProduct,
                    copyTrans:this.copyInv,
                    isBatchForProduct:prorec.data.isBatchForProduct,
                    isOnlyBatchForProduct:this.isOnlyBatchForProduct,
                    isSerialForProduct:prorec.data.isSerialForProduct,
                    parentObj:this.parentObj,
                    isSKUForProduct:prorec.data.isSKUForProduct,
                    isShowStockType:(this.isCustomer)?true:false,
                    transactionid:(this.isCustomer)?4:5,
                    width:950,
                    readOnly:this.readOnly,
//                    height:400,
                    resizable : false,
                    allowUserToEditQuantity : this.allowUserToEditQuantity,      //Passing this parameter to check whether we have to allow user to edit Qty in batch serial window.ERM-319
                    modal : true,
                    lineRec:obj,
                    linkflag:this.blockQtyFlag,
                    parentGrid:this,
                    bomid:this.bomcombo.value==undefined ? obj.data.bomid : this.bomcombo.value,
                    documentid:((this.isEdit) && (this.moduleid== Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid==Wtf.Acc_Cash_Purchase_ModuleId))?obj.data.grorowid:obj.data.rowid,
                    docrowid:((this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId) && (this.isEdit || isLinkedFromSO))?obj.data.docrowid : "",
                    linkedFrom:this.parentObj.fromLinkCombo.getValue()
                   });  
            }
            this.batchDetailswin.on("beforeclose",function(){
                this.batchDetails=this.batchDetailswin.getBatchDetails();
                var isfromSubmit=this.batchDetailswin.isfromSubmit;
                if(isfromSubmit){  //as while clicking on the cancel icon it was adding the reocrd in batchdetail json 
                    obj.set("batchdetails",this.batchDetails);
                    if(obj.data.isSerialForProduct && obj.data.islockQuantityflag && this.moduleid == Wtf.Acc_Invoice_ModuleId && this.parentObj.fromLinkCombo.getValue()==0){
                 //getting Batch details of row for serial change
                  Wtf.Ajax.requestEx({
                    url: "ACCSalesOrderCMN/getSalesOrderRowBatchJSON.do",
                    params: {
                        productid:prorec.data.productid,
                        documentid:obj.data.rowid,
                        transType:this.moduleid,
                        moduleid:this.moduleid,
                        isEdit:this.isEdit,
                        linkingFlag:true,
                        isConsignment:false
                    }
                },this,this.genSuccessResponseReplaceSerial,this.genFailureResponseReplaceSerial);

                }
                }
            },this);
             /*
             * On close event of batch serial window calculating the average base uom quantity and base uom rate and setting it into grid record.
             * after average calculation fireing afteredit event to recalculate the price and amount.
             * ERM-319
             */
             this.batchDetailswin.on("close", function () {
                if (CompanyPreferenceChecks.differentUOM() &&  this.allowUserToEditQuantity && this.isOnlyBatchForProduct) {
                    this.batchDetails = this.batchDetailswin.getBatchDetails();
                    var isfromSubmit = this.batchDetailswin.isfromSubmit;
                    if (isfromSubmit) {  //as while clicking on the cancel icon it was adding the reocrd in batchdetail json 
                        if (this.batchDetails != undefined && this.batchDetails != "") {
                            var batchDetailsJSONObj = eval('(' + this.batchDetails + ')');
                            var totalQty = 0.0;
                            for (var cnt = 0; cnt < batchDetailsJSONObj.length; cnt++) {
                                if (batchDetailsJSONObj[cnt].quantity != undefined && batchDetailsJSONObj[cnt].quantity != "" && batchDetailsJSONObj[cnt].quantity != null) {
                                    totalQty += parseFloat(batchDetailsJSONObj[cnt].quantity);
                                }
                            }
                        }
                        var receivedQuantity=obj.get("quantity");
                        obj.set("baseuomrate", getRoundofValue(totalQty/receivedQuantity));
                        obj.set("baseuomquantity", getRoundofValue(obj.get("quantity") * obj.get("baseuomrate")));
                        this.fireEvent('afteredit', {
                            field: 'baseuomrate',
                            value: getRoundofValue(totalQty / receivedQuantity),
                            record: obj
                        });
                    }
                }
            }, this);
            
            this.batchDetailswin.show();
        }
    },
        genSuccessResponseReplaceSerial : function(response){
         var SerialReplaceArr=[];
         var presentCount=0;
         var serialscount = 0;
         var isfirst = true;
         var jsonBatchDetails= eval(this.batchDetails);
           if(this.isAutoFilledBatchDetails){
            for (var i = 0; i < response.data.length; i++) {
                var isSerialPresent = false;
                var responseserialid = response.data[i].purchaseserialid;                
                for (var k = 0; k < jsonBatchDetails.length; k++) {
                    var serialDetails = eval(jsonBatchDetails[k].serialDetails);
                    for (var l = 0; l < serialDetails.length; l++) {
                        if (isfirst) {
                            serialscount++;
                        }
                        if (serialDetails[l].purchaseserialid == responseserialid) {
                            isSerialPresent = true;
                            presentCount++;
                            break;
                        }
                    }
                }
                isfirst = false;
                if (isSerialPresent == false) {
                    SerialReplaceArr.push(response.data[i]);
                }
            }
         }else{
           for(var i=0;i<response.data.length;i++){
             var isSerialPresent=false;
             var responseserialid=response.data[i].purchaseserialid;             
                for(var k=0;k<jsonBatchDetails.length;k++){              
                if(responseserialid ==jsonBatchDetails[k].purchaseserialid){
                    isSerialPresent=true;
                            presentCount++;
                            break;
                        }
                    }
                if(isSerialPresent==false){
                SerialReplaceArr.push(response.data[i]);
            }
        }  
         }   
         var serialchangeCount=(jsonBatchDetails.length)-presentCount;
         var serial_change_Count=serialscount-presentCount;
         if(response.data.length > 0 && (serialchangeCount > 0 || serial_change_Count > 0) && SerialReplaceArr.length > 0){
             var serilReplaceWin = Wtf.getCmp('SerialReplaceWindow'); 
            if(serilReplaceWin == null){
                serilReplaceWin = new Wtf.SerialRepalceWindow({
                    id : 'SerialReplaceWindow',  
                    border : false,
                    title: WtfGlobal.getLocaleText("acc.replaceserialwin.title"),  
                    serialchangeCount:this.isAutoFilledBatchDetails ? serial_change_Count : serialchangeCount,
                    SerialReplaceArr:SerialReplaceArr,
                    moduleid:this.moduleid,
                    scope:this,
                    closable: false,
                    modal: true,
                    iconCls :getButtonIconCls(Wtf.etype.deskera),
                    resizable: false,
                    renderTo: document.body                
                });
                
                serilReplaceWin.on("onsubmit",function(scope,replacebatchdetails){
                    this.recAobj.set("replacebatchdetails",replacebatchdetails);
                },this);
                serilReplaceWin.show(); 
            }
         }
    },
    genFailureResponseReplaceSerial : function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    calTaxAmount:function(rec){
        
        var quantity = rec.data.quantity;
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
        
        var origionalAmount = rec.data.rate*quantity;
        if(rec.data.partamount != 0) {
            origionalAmount = origionalAmount * (rec.data.partamount/100);
        }
        var discount = 0;//origionalAmount*rec.data.prdiscount/100
        if(!(this.isNote||this.readOnly)) {
            origionalAmount = this.calAmountWithExchangeRate(origionalAmount, rec);
        }
        
        if(rec.data.prdiscount > 0) {
            if(rec.data.discountispercent == 1){
                discount = (origionalAmount * rec.data.prdiscount) / 100;
            } else {
                discount = rec.data.prdiscount;
            }
        }
//        var discount=origionalAmount*rec.data.prdiscount/100
        var val=(origionalAmount)-discount;
        var taxpercent=0;
            var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
            if(index>=0){
               var taxrec=this.taxStore.getAt(index);
                taxpercent=taxrec.data.percent;
            }
        return (val*taxpercent/100);

    },
    // ERP-13087 Removing the functions that are no longer required
//  calTaxAmountWithoutExchangeRate:function(rec){
//        
//        var quantity = rec.data.quantity;
//        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
//        
//        var origionalAmount = rec.data.rate*quantity;
//        if(rec.data.partamount != 0) {
//            origionalAmount = origionalAmount * (rec.data.partamount/100);
//        }
//        var discount = 0;//origionalAmount*rec.data.prdiscount/100
////        if(!(this.isNote||this.readOnly)) {
////            origionalAmount = this.calAmountWithExchangeRate(origionalAmount, rec);
////        }
//        
//        if(rec.data.prdiscount > 0) {
//            if(rec.data.discountispercent == 1){
//                discount = (origionalAmount * rec.data.prdiscount) / 100;
//            } else {
//                discount = rec.data.prdiscount;
//            }
//        }
////        var discount=origionalAmount*rec.data.prdiscount/100
//        var val=(origionalAmount)-discount;
//        var taxpercent=0;
//            var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
//            if(index>=0){
//               var taxrec=this.taxStore.getAt(index);
//                taxpercent=taxrec.data.percent;
//            }
//        return (val*taxpercent/100);
//
//    },
    setTaxAmountAfterSelection:function(rec) {
        
        var quantity = rec.data.quantity;
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
//        var rec=obj.record;
        var discount = 0;
        var lineTermAmount = (rec.data.lineleveltermamount == undefined || rec.data.lineleveltermamount == null || rec.data.lineleveltermamount == '')?0:rec.data.lineleveltermamount;
        var rate=getRoundofValueWithValues(rec.data.rate,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        var quantity=getRoundofValue(rec.data.quantity);
        var origionalAmount = getRoundedAmountValue(rate*quantity) ;
        if(rec.data.partamount != 0){
            var partamount=getRoundedAmountValue(rec.data.partamount);
            origionalAmount = getRoundedAmountValue(origionalAmount * (partamount/100));
        }
        
//        origionalAmount = this.calAmountWithExchangeRate(origionalAmount, rec);
        if(rec.data.prdiscount > 0) {
            var prdiscount=getRoundedAmountValue(rec.data.prdiscount);
            if(rec.data.discountispercent == 1){
                discount = getRoundedAmountValue(origionalAmount * prdiscount/ 100);
            } else {
                discount = prdiscount;
            }
        }
        //var discount=rec.data.rate*rec.data.quantity*rec.data.prdiscount/100
        var val=origionalAmount-discount+lineTermAmount ;
        var taxpercent=0;
        var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
        if(index>=0){
            var taxrec=this.taxStore.getAt(index);
            taxpercent=getRoundedAmountValue(taxrec.data.percent);
        }
        var taxamount= getRoundedAmountValue(val*taxpercent/100);
        // ERP-24202 - Recalculating Tax amountconsidering the discount in calculation
        if((this.parentObj.includingGST && this.parentObj.includingGST.getValue())){
            taxamount = this.recalculateTaxAmountWithDiscount(rec,taxamount,taxpercent);
        }
        return taxamount;
        
    },
    setBOMValuationArrayToRecord: function(obj) {
        Wtf.Ajax.requestEx({
            url: "ACCReports/getPriceCalculationForAsseblyProduct.do",
            params: {
                productid: obj.record.data.productid,
                buildquantity: obj.record.data.quantity
            }
        }, this, function(res,req) {
            if (res && res.success) {
                var bomValuationArray = [];
                for (var i=0; i<res.valuationArray.length; i++) {
                    var rowObject = new Object();
                    var bomRec = res.valuationArray[i];
                    rowObject['productid'] = bomRec.productid;
                    rowObject['buildcost'] = bomRec.buildcost;
                    bomValuationArray.push(rowObject);
                }
                obj.record.data.bomValuationArray = JSON.stringify(bomValuationArray);
            }
        });
    },
    
    checkMinMaxOrderingQuantity : function(obj,prorec){
        var productID="";
        productID = prorec.data.pid
        if(obj.record.get("baseuomquantity") < prorec.data.minorderingquantity){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.BaseQuantityislessthanMinimumOrderingQuantityforproduct")+"<b>"+productID+"</b><br/>"+WtfGlobal.getLocaleText("acc.msgbox.Doyouwanttoproceed"),function(btn){
                if(btn!="yes") {
                    obj.record.set(obj.field, obj.originalValue);
                    obj.record.set("baseuomquantity",obj.record.get("quantity")*obj.record.get("baseuomrate"));
                }
            },this)
        }else if(obj.record.get("baseuomquantity")  > prorec.data.maxorderingquantity){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.BaseQuantityisgreaterthanMaximumOrderingQuantityforproduct")+"<b>"+productID+"</b><br/>"+WtfGlobal.getLocaleText("acc.msgbox.Doyouwanttoproceed"),function(btn){
                if(btn!="yes") {
                    if (obj.originalValue == "") {
                        obj.record.set(obj.field, 0);
                    } else {
                        obj.record.set(obj.field, obj.originalValue);
                    }
                    obj.record.set("baseuomquantity",obj.record.get("quantity")*obj.record.get("baseuomrate"));
                }
            },this)
        }
    },
    /**
     * 
     * code moved in function
     */
    updateunitpricewithGST: function (obj,islinkingFlag) {
        /**
         * Price will does not change for linking cases.
         * !islinkingFlag is used so that setTaxAndRateAmountAfterIncludingGST will not be call in linking case while
         * changing the qty.
         */
        if (obj.field == "prtaxid" || obj.field == "rate" || obj.field == "quantity" || obj.field == "showquantity" || obj.field == "dependentType" || obj.field == "discountispercent" || obj.field == "prdiscount") {
            var taxamount = this.setTaxAmountAfterSelection(obj.record);
            obj.record.set("taxamount", taxamount);
            obj.record.set('isUserModifiedTaxAmount', false);
            if (!(obj.field == "quantity" && islinkingFlag)) {//SDP-14955
            if ((obj.field == "discountispercent" || obj.field == "prdiscount") && (this.parentObj.includingGST && this.parentObj.includingGST.getValue() == true)) {
                if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                    this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                } else {
                    this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                }
                } else if (!(this.parentObj.includingGST && this.parentObj.includingGST.getValue() == true)) {
                obj.record.set("rateIncludingGst", obj.record.data.rate);
            }
            if (obj.field == "prtaxid" && WtfGlobal.singaporecountry() && WtfGlobal.getCurrencyID() != Wtf.Currency.SGD && (this.isInvoice || this.isCash) && this.forCurrency != Wtf.Currency.SGD) {
                var record = WtfGlobal.searchRecord(this.parentObj.currencyStore, this.parentObj.Currency.getValue(), "currencyid");
                callGstCurrencyRateWin(this.id, record.data.currencyname + " ", obj, obj.record.get("gstCurrencyRate") * 1);
            }            
            }
            this.fireEvent('datachanged', this);
        }

        /*Committing below code as this is not required after ERP-38587*/
//        else if (this.parentObj && this.parentObj.includingGST && this.parentObj.includingGST.getValue() == true && obj.field == "rateIncludingGst" && this.parentObj.termgrid) {
//            this.parentObj.termgrid.getStore().rejectChanges();//reset the grid when includingGST unit price is changed.SDP-13373
//        }

        if ((Wtf.account.companyAccountPref.productPricingOnBands || Wtf.account.companyAccountPref.productPricingOnBandsForSales) && (obj.field == "rateIncludingGst" || obj.field == "rate")) {
            obj.record.set("priceSource", "Manual Entry");
            obj.record.set("pricingbandmasterid", "");
        }
    },
    setTaxAndRateAmountAfterIncludingGST : function (record) {
        if(record.data.prtaxid!="None"){
            var taxamount = this.setTaxAmountAfterIncludingGst(record,1);
            var amountwithOutGst = this.setTaxAmountAfterIncludingGst(record,2);
            record.set("taxamount",taxamount);
            record.set("isUserModifiedTaxAmount", false);
            if(amountwithOutGst!=0) {
                record.set("rate",amountwithOutGst);
            } else {
                record.set("rate",record.data.rateIncludingGst);
            }
        }
    },
    setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms : function (record) {
        record.set('recTermAmount', 0);
        if (record.get('LineTermdetails') != undefined && record.get('LineTermdetails') != '') {
            var termStore = this.getTaxJsonOfIndia(record);
            if (this.parentObj && this.parentObj.includingGST && this.parentObj.includingGST.getValue() == true) {
                this.getColumnModel().setRenderer(this.getColumnModel().findColumnIndex("amount"), WtfGlobal.withoutRateCurrencySymbol);
                termStore = this.calculateTermLevelTaxesInclusive(termStore,record);
            } else {
                this.getColumnModel().setRenderer(this.getColumnModel().findColumnIndex("amount"), this.calAmountWithoutExchangeRate.createDelegate(this));
                termStore = this.calculateTermLevelTaxes(termStore,record, undefined, true);
            }

            record.set('LineTermdetails', Wtf.encode(termStore));
            /*
             Calculated other terms which are not taxable  
             */
            var OtherTermNonTaxableAmount = getRoundedAmountValue(0.0);
            for (var termCnt = 0; termCnt < termStore.length; termCnt++) {
                var object = termStore[termCnt];
                if (object && object.termtype == Wtf.term.Others && !object.IsOtherTermTaxable) {
                    OtherTermNonTaxableAmount += getRoundedAmountValue(object.termamount);
                }
            }
            if (OtherTermNonTaxableAmount) {
                record.set('OtherTermNonTaxableAmount', OtherTermNonTaxableAmount);
            }
            this.updateTermDetails();
            // updated subtotla xtemplate
            this.parentObj.updateSubtotalOnTermChange(true, true, record.data.termamount);
        }

    },
    
    setTaxAmountAfterIncludingGst:function(rec,amountFlag) {//amountFlag=1 for taxamount and amountFlag=2 for actual amount with discount actualamount=3 amount with GST
        
        var quantity = rec.data.quantity;
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
//        var rec=obj.record;
        var discount = 0;
        var rateIncludingGst=getRoundofValueWithValues(rec.data.rateIncludingGst,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        quantity=getRoundofValue(rec.data.quantity);
        var lineTermAmount = (rec.data.lineleveltermamount == undefined || rec.data.lineleveltermamount == null || rec.data.lineleveltermamount == '')?0:rec.data.lineleveltermamount;
        var quantityAndAmount=0;
        quantityAndAmount=rateIncludingGst*quantity;
        var origionalAmount =quantityAndAmount ;
        if(rec.data.partamount != 0){
            var partamount=getRoundedAmountValue(rec.data.partamount);
            origionalAmount = getRoundedAmountValue(origionalAmount * (partamount/100));
        }
        
//        origionalAmount = this.calAmountWithExchangeRate(origionalAmount, rec);
        if(rec.data.prdiscount > 0) {
            var prdiscount=getRoundedAmountValue(rec.data.prdiscount);
            if(rec.data.discountispercent == 1){
                discount = getRoundedAmountValue(origionalAmount * prdiscount/ 100);
            } else {
                discount = prdiscount;
            }
        }
        //var discount=rec.data.rate*rec.data.quantity*rec.data.prdiscount/100
        var val=origionalAmount-discount+lineTermAmount;
        var taxpercent=0;
        var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
        if(index>=0){
            var taxrec=this.taxStore.getAt(index);
            taxpercent=getRoundedAmountValue(taxrec.data.percent);
        }
//        var amount=getRoundedAmountValue(val*100/(taxpercent+100));
        var amount=0.0;
        var taxamount=getRoundedAmountValue(val*taxpercent/(taxpercent+100));
//        var taxamount= getRoundedAmountValue(val-amount);
//        var taxamount= 0;
        var unitAmount= 0;
        var unitTax= 0;
        var unitVal= 0;
        if(quantity!=0){
             amount=getRoundofValueWithValues((val-taxamount),Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
             unitVal=getRoundofValueWithValues(val/quantity,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
             unitAmount=getRoundofValueWithValues(amount/quantity,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
             unitTax= getRoundedAmountValue(taxamount/quantity);
//             taxamount=getRoundedAmountValue(unitTax*quantity);
        }
        if(amountFlag==1){
            return taxamount;
        }else if(amountFlag==2){
            if(quantity!=0){
                val=unitAmount;
            }else{
                val=0;
            }
            return val;
            }
//        else if(amountFlag==3){
//            if(quantity!=0){
//                val=getRoundofValueWithValues((val/quantity),Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
//                
//            }else{
//                val=0;
//            }
//            return val;
//        }
        
    },
    // ERP-13087 Removing the functions that are no longer required
//    
//    setTaxAmount:function(v,m,rec){
//       var taxamount= this.calTaxAmount(rec);
//       rec.set("taxamount",taxamount);
////        if(this.isNote||this.readOnly)
//             return WtfGlobal.withoutRateCurrencySymbol(taxamount,m,rec);
////        return WtfGlobal.currencyRendererSymbol(taxamount,m,rec);
//    },
    unitPriceRendererWithPermissionCheck:function(v,m,rec){
        if (!isNaN(v)) {
            if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                return Wtf.UpriceAndAmountDisplayValue;
            } else {
                /*
                 * ERP-40242 : In copy case, deactivated tax not shown.Hence, empty taxid set in record.          
                 */
                if ((this.parentObj && this.parentObj.includingGST && this.parentObj.includingGST.getValue()) && (rec.data.prtaxid != '' && (this.copyInv || this.fromPO))) {
                    var taxActivatedRec = WtfGlobal.searchRecord(this.taxStore, rec.data.prtaxid, "prtaxid");
                    if (taxActivatedRec == null || taxActivatedRec == undefined || taxActivatedRec == "") {
                        rec.set("prtaxid", "");
                        rec.set("rate", rec.data.rateIncludingGst);
                    }
                }
                
                if (rec.data.prtaxid != "None") {
                    return WtfGlobal.withCurrencyUnitPriceRenderer(v,m,rec);
                } else {
                     /**
                     * In INDIA Including GST for rate column same value render whhich is presnet if rateIncludingGST
                     * column 
                     */
                    if((!WtfGlobal.isIndiaCountryAndGSTApplied() && !WtfGlobal.isUSCountryAndGSTApplied()) && this.parentObj!== null && this.parentObj.includingGST != undefined && this.parentObj.includingGST.checked == true){
                         return WtfGlobal.withCurrencyUnitPriceRenderer(rec.data.rateIncludingGst,m,rec);
                    }else{
                        return WtfGlobal.withCurrencyUnitPriceRenderer(v,m,rec);
                    }
                }
            }
        }
    },
    setTaxAmountWithotExchangeRate:function(v,m,rec){
        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)){//When permission not given to display 
            return Wtf.UpriceAndAmountDisplayValue;
        } else {
            var taxamount= 0;
            /*
             * ERP-40242 : In copy case, deactivated tax not shown.Hence, empty taxid set in record.          
             */
            if (rec.data.prtaxid != '' && (this.copyInv || this.fromPO)) {
                var taxActivatedRec = WtfGlobal.searchRecord(this.taxStore, rec.data.prtaxid, "prtaxid");
                if (taxActivatedRec == null || taxActivatedRec == undefined || taxActivatedRec == "") {
                    rec.set("prtaxid", "");
                }
            }
            if(v)
                taxamount= parseFloat(getRoundedAmountValue(v)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1;
            if(rec.data.prtaxid==null || rec.data.prtaxid == undefined || rec.data.prtaxid == "" || rec.data.prtaxid == "None"){
                taxamount = 0;
            }
            rec.set("taxamount",taxamount);
            return WtfGlobal.withoutRateCurrencySymbol(taxamount,m,rec);  
        }
    },
    // ERP-13087 Removing the functions that are no longer required
//    calAmount:function(v,m,rec){
//        
//        var rate=getRoundedAmountValue(rec.data.rate);
//        /*
//         * Check if rateincludegst is available or not
//         */
//        var rowRateIncludingGstAmountIndex=this.getColumnModel().getIndexById(this.id+"rateIncludingGst");
//        if(!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden) {
//            rate = rec.data.rateIncludingGst;
//        }
//        var quantity=getRoundofValue(rec.data.quantity);
//        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
//        var origionalAmount = getRoundedAmountValue(rate*quantity) ;
//        if(rec.data.partamount != 0){
//             var partamount=getRoundedAmountValue(rec.data.partamount);
//            origionalAmount = getRoundedAmountValue(origionalAmount * (partamount/100));
//        }
//        
//        var discount = 0;//origionalAmount*rec.data.prdiscount/100
//        origionalAmount = this.calAmountWithExchangeRate(origionalAmount, rec);
//        
//        
////        var stockQuantity = this.getStockQuantity(rec.get('productid'));
////        origionalAmount = stockQuantity*origionalAmount; // calculate amount with respect to purchase and sales uom
//        
//        if(rec.data.prdiscount > 0) {
//             var prdiscount=getRoundedAmountValue(rec.data.prdiscount);
//            if(rec.data.discountispercent == 1){
//                discount = getRoundedAmountValue((origionalAmount * prdiscount) / 100);
//            } else {
//                discount = prdiscount;
//            }
//        }
//        
//        var val=(origionalAmount)-discount;///rec.data.oldcurrencyrate
//        
//        rec.set("amountwithouttax",val);
//        
////        var taxamount= this.calTaxAmount(rec);
//        var taxamount = 0;
//        if(rec.data.taxamount){
//            taxamount= getRoundedAmountValue(rec.data.taxamount);
//        }
//        /*
//         * Check if rateincludegst is available or not. If yes then no need to add tax value in amount value
//         */
//        if(this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden) {
//         val+=taxamount;
//        }
//
//        rec.set("amount",val);
//        if(this.isQuotationFromPR && val!==0 && (rec.data.orignalamount==undefined || rec.data.orignalamount==""))
//            rec.set("orignalamount",val);
////       if(this.isNote||this.readOnly)
//             return WtfGlobal.withoutRateCurrencySymbol(val,m,rec);
////       return WtfGlobal.currencyRendererSymbol(val,m,rec);
//    },
    
    calAmountWithoutExchangeRate:function(v,m,rec){
        
            
        var rate=getRoundofValueWithValues(rec.data.rate,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        var isPartialInvoice = false;
        /*
         * Check if rateincludegst is available or not
         */
//          var rowRateIncludingGstAmountIndex=this.getColumnModel().getIndexById(this.id+"rateIncludingGst");
         // var rowRateIncludingGstAmountIndex=this.getColumnModel().findColumnIndex("rateIncludingGst");   //instead of getIndexById() used findColumnIndex based on dataindex, refer ticket ERP-17718
        if(this.parentObj !=undefined && this.parentObj.includingGST && this.parentObj.includingGST.getValue()) {
            rate = rec.data.rateIncludingGst;
        }
        var quantity=getRoundofValue(rec.data.quantity);
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
        
        
        var origionalAmount = getRoundedAmountValue(rate * quantity);
        
        /*Calculating Amount  If Invoice 
         * 
         * is linked partially with Sales Order 
         */

        if (rec.json != undefined && rec.json.remainingPartAmt!=undefined && rec.json.remainingPartAmt != 0) {

            /* If value entered in global partial field 
             * have exceeded from remaining part 
             * amount of SO to be linked in Invoice
             */
            if ((rec.data.partamount > rec.json.remainingPartAmt) || rec.data.partamount == "") {
                origionalAmount = getRoundedAmountValue(origionalAmount * (rec.json.remainingPartAmt / 100));//maximum amount in partial case

            } else {
                origionalAmount = getRoundedAmountValue(origionalAmount * (rec.data.partamount / 100));
                isPartialInvoice = true;
            }
            
        } else {
            
            /*When initially linked SO with partial amount 
             * i.e SO is not linked previously with any Invoice 
             */
            if(rec.data.partamount!=0){
              origionalAmount = getRoundedAmountValue(origionalAmount * (rec.data.partamount / 100)); 
              isPartialInvoice = true;
            }
            
        }
                     
        var discount = 0;//origionalAmount*rec.data.prdiscount/100   
        var differnceInDisct = 0;
        if(rec.data.prdiscount > 0) {
            var prdiscount=getRoundedAmountValue(rec.data.prdiscount);
            if(rec.data.discountispercent == 1){
                discount = getRoundedAmountValue((origionalAmount * prdiscount) / 100);
                /*calculated difference in discount and added in Last Partial Invoice*/
                if(rec.json != undefined && rec.json.remainingPartAmt!=undefined && (rec.json.sumOfAllPrevPartialAmt + rec.data.partamount) == 100){
                    differnceInDisct = (getRoundedAmountValue(rate * quantity) * prdiscount / 100) - (discount + rec.data.sumOfAllLastInvDiscount);
                }
            } else {
                /*In Flat Discount type discount amount is divided partially same like unit price*/
                if (isPartialInvoice) {
                    discount = getRoundedAmountValue(prdiscount * (rec.data.partamount / 100));
                    /*calculated difference in discount and added in Last Partial Invoice*/
                    if(rec.json != undefined && rec.json.remainingPartAmt!=undefined && (rec.json.sumOfAllPrevPartialAmt + rec.data.partamount) == 100){
                        differnceInDisct = prdiscount - (discount + rec.data.sumOfAllLastInvDiscount);
                    }
                } else {
                    discount = prdiscount;
                }
            }
        }
//        var val = (origionalAmount) - discount;///rec.data.oldcurrencyrate  
//        if (val < 0) {
//            rec.set("amountwithouttax", 0);
////            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.discount.amtisgreaterthanrate")], 2);
//        } else {
//            rec.set("amountwithouttax", val);
//        }
        var val=(origionalAmount)-(discount + differnceInDisct);///rec.data.oldcurrencyrate  
        
        if(isPartialInvoice && (this.readOnly || this.isEdit)){
            val = origionalAmount - rec.data.partialDiscount;
            rec.set("differnceInDisct",rec.data.partialDiscount - discount);
            rec.set("partialDiscount",rec.data.partialDiscount);
        }
//        rec.set("amountwithouttax",val);
        
        if(isPartialInvoice && !this.readOnly && !this.isEdit){
            rec.set("partialDiscount",(discount + differnceInDisct));
            rec.set("differnceInDisct",differnceInDisct);
        }
        var taxamount = 0;
        if(rec.data.taxamount){
            taxamount= getRoundedAmountValue(rec.data.taxamount);
        }
        if (this.parentObj != undefined && this.parentObj.includingGST && this.parentObj.includingGST.getValue()) {
            rec.set("amountwithouttax", val - taxamount);
        } else {
            rec.set("amountwithouttax", val);
        }
        /*
         * Check if rateincludegst is available or not. If yes then no need to add tax value in amount value
         */
        if (this.parentObj != undefined && this.parentObj.includingGST && !this.parentObj.includingGST.getValue() && Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA) {
            val = parseFloat(val) + parseFloat(taxamount);
        }
//        if(val>0){
//            rec.set("amount", (parseFloat(getRoundedAmountValue(val)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL) * 1));
//        } else {
//            rec.set("amount", 0);
//        }
//        if (this.isQuotationFromPR && val !== 0 && (rec.data.orignalamount == undefined || rec.data.orignalamount == ""))
//        rec.set("orignalamount", val);
//        
        
        rec.set("amount",(parseFloat(getRoundedAmountValue(val)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1));
//        if (this.parentObj != undefined && this.parentObj.includingGST && this.parentObj.includingGST.getValue()) {
//            rec.set("amountwithouttax", val - taxamount);
//        } else {
//            rec.set("amountwithouttax", val);
//        }
        if(this.isQuotationFromPR && val!==0 && (rec.data.orignalamount==undefined || rec.data.orignalamount==""))
        rec.set("orignalamount",val);
        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
            return Wtf.UpriceAndAmountDisplayValue;
        } else{
            return WtfGlobal.withoutRateCurrencySymbol(val,m,rec);
        }
    },
    dealerExciseDetails:function(rec,obj){
        if(Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isExciseApplicable){
            if(!this.isEdit ){
                Wtf.Ajax.requestEx({
                    url : "ACCCompanyPref/getSequenceFormatStore.do",
                    params:{
                        mode:Wtf.companyAccountPref_autoRG23EntryNumber,
                        isEdit:this.isEdit
                    }
                }, this,function(resp){
                    var sequenceformat="";
                    var sequenceformatPattern="";
                    for(var i=0;i<resp.count;i++){
                        if(resp.data[i].isdefaultformat=="Yes"){
                            sequenceformat=resp.data[i].id;
                            sequenceformatPattern=resp.data[i].value;
                            break;
                        }
                    }
                    if(!Wtf.isEmpty(sequenceformat)){
                        Wtf.Ajax.requestEx({
                            url:"ACCCompanyPref/getNextAutoNumber.do",
                            params:{
                                from:Wtf.autoNum.Dealer_Excise_RG23DEntry_No,
                                sequenceformat:sequenceformat,
                                oldflag:false
                            }
                        }, this,function(resp1){
                            if(resp1.success){

                                var json=[];
                                json.push({
                                    "RG23DEntryNumber":resp1.data,
                                    "RG23DseqFormat":sequenceformatPattern,
                                    "sequenceformat":sequenceformat,
                                    "AssessableValue":rec.data.amount,
                                    "ManuAssessableValue":rec.data.amount,
                                    "dealerExciseTerms":rec.data.dealerExciseTerms
                                });
                                rec.set("dealerExciseDetails", JSON.stringify(json)) 
                            }else{
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Error occurred while generating RG32D number"], 2);
                            }
                        }); 
                    }else{
                         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invoice.grid.seqnotcreated")], 2);
                    }
                });
            }else if(this.isEdit){
                var gridStore=obj.grid.getStore();
                if(gridStore.data.length>2){
                    var json=[];
                    var dealerExcsise = eval(gridStore.data.items[0].data.dealerExciseDetails)
                    json.push({
                        "RG23DEntryNumber":dealerExcsise[0].RG23DEntryNumber,
                        "RG23DseqFormat":dealerExcsise[0].RG23DseqFormat,
                        "sequenceformat":dealerExcsise[0].sequenceformat,
                        "seqnumber":dealerExcsise[0].seqnumber,
                        "datePreffixValue":dealerExcsise[0].datePreffixValue,
                        "dateSuffixValue":dealerExcsise[0].dateSuffixValue,
                        "AssessableValue":rec.data.amount,
                        "ManuAssessableValue":rec.data.amount,
                        "dealerExciseTerms":this.record.data.dealerExciseTerms
                    });
                    rec.set("dealerExciseDetails", JSON.stringify(json)); 
                }
            }
        }
    },
    calTotalCostWithoutExchangeRate:function(v,m,rec){
        var totalCostInBase = this.calLineLevelTotalCost(v,m,rec);
        rec.set("totalcost",totalCostInBase);
        return WtfGlobal.addCurrencySymbolOnly(totalCostInBase,WtfGlobal.getCurrencySymbol());
    },
    
    calLineLevelTotalCost: function(v,m,rec){
        var unitcost=rec.data.vendorunitcost;
        unitcost = (unitcost == "NaN" || unitcost == undefined || unitcost == null || unitcost == "") ? 0 : unitcost;
        var qty = rec.data.quantity; 
        qty = (qty == "NaN" || qty == undefined || qty == null || qty == "") ? 0 : qty;
        var baseUOMRate = rec.data.baseuomrate;
        baseUOMRate = (baseUOMRate == "NaN" || baseUOMRate == undefined || baseUOMRate == null || baseUOMRate == "") ? 1 : baseUOMRate;
        var quantity = qty * baseUOMRate;
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null || quantity == "")?0:quantity;
        var totalCostInVendorCurr = getRoundedAmountValue(unitcost*quantity) ;
        var vendExchangeRate=1;
        if(rec.data.vendorcurrexchangerate!=null){
            vendExchangeRate = rec.data.vendorcurrexchangerate;
        }
        var totalCostInBase = totalCostInVendorCurr * vendExchangeRate;
        totalCostInBase = parseFloat(getRoundedAmountValue(totalCostInBase)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1;
        return totalCostInBase;
    },
    
    calLineLevelTotalSellingPrice: function(v,m,rec){
        var totalSellingPrice=rec.data.amount;
        totalSellingPrice = (totalSellingPrice == "NaN" || totalSellingPrice == undefined || totalSellingPrice == null || totalSellingPrice == "" ) ? 0 : totalSellingPrice;
        var revExchangeRate = this.getExchangeRate();
        var totalSellingPriceInBase = getRoundedAmountValue(totalSellingPrice * revExchangeRate);
        return totalSellingPriceInBase;
    },
    
    calLineLevelProfitMargin:function(v,m,rec){
        var totalCostInBase = this.calLineLevelTotalCost(v,m,rec);
        var totalSellingPriceInBase = this.calLineLevelTotalSellingPrice(v,m,rec);
        var profitMargin = totalSellingPriceInBase - totalCostInBase;
        profitMargin = parseFloat(getRoundedAmountValue(profitMargin)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1;
        return profitMargin;
    },
    
    lineLevelProfitMarginRenderer:function(v,m,rec){
        var profitMargin = 0;
        var vendorid=rec.data.vendorid;
        if(vendorid!=null && vendorid!=undefined && vendorid!=""){
            profitMargin = this.calLineLevelProfitMargin(v,m,rec);
        }
        rec.set("profitmargin",profitMargin);
        return WtfGlobal.addCurrencySymbolOnly(profitMargin,WtfGlobal.getCurrencySymbol());
    },
    
    calLineLevelProfitMarginPercent:function(v,m,rec){
        var totalCostInBase = this.calLineLevelTotalCost(v,m,rec);
        var totalSellingPriceInBase = this.calLineLevelTotalSellingPrice(v,m,rec);
        var profitMargin= this.calLineLevelProfitMargin(v,m,rec);
        var profitMarginPercent = 0;
        if(totalSellingPriceInBase==0 || totalSellingPriceInBase=="" || totalSellingPriceInBase==undefined || totalSellingPriceInBase==null){
            profitMarginPercent="NA";
        }else{
            profitMarginPercent=(profitMargin/totalSellingPriceInBase)*100;
            profitMarginPercent = parseFloat(getRoundedAmountValue(profitMarginPercent)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1;
        }
        return profitMarginPercent;
    },
    
    lineLevelProfitMarginPercentRenderer:function(v,m,rec){
        var profitMarginPercent = 0;
        var vendorid=rec.data.vendorid;
        if(vendorid!=null && vendorid!=undefined && vendorid!=""){
            profitMarginPercent = this.calLineLevelProfitMarginPercent(v,m,rec);
        }else{
            profitMarginPercent="NA";
        }        
        rec.set("profitmarginpercent",profitMarginPercent);
        return profitMarginPercent=="NA"? "NA" : '<div class="currency">'+profitMarginPercent+'%</div>';
    },
    
    getExchangeRate:function(){
        var revExchangeRate = 0;
        if(this.parentObj!=null && this.parentObj!="" && this.parentObj!=undefined){
            var index=this.parentObj.getCurrencySymbol();
            var rate=this.parentObj.externalcurrencyrate;
            if(index>=0){
                var exchangeRate = this.parentObj.currencyStore.getAt(index).data['exchangerate'];
                if(this.parentObj.externalcurrencyrate>0) {
                    exchangeRate = this.parentObj.externalcurrencyrate;
                }
                revExchangeRate = 1/(exchangeRate);
                revExchangeRate = (Math.round(revExchangeRate*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
            }
        }
        return revExchangeRate;
    }, 
    
    calAmountWithExchangeRate: function(value,rec) {
        var rate=((rec==undefined||rec.data['currencyrate']==undefined||rec.data['currencyrate']=="")?1:rec.data['currencyrate']);
        var oldcurrencyrate=((rec==undefined||rec.data['oldcurrencyrate']==undefined||rec.data['oldcurrencyrate']=="")?1:rec.data['oldcurrencyrate']);
        var v;
        if(rate!=0.0)
            v=(parseFloat(value)*parseFloat(rate))/parseFloat(oldcurrencyrate);
        else
            v=(parseFloat(value)/parseFloat(oldcurrencyrate));
        if(isNaN(v)) return value;
            v= WtfGlobal.conventInDecimalWithoutSymbol(v)
        return v;

    },
    
    calSubtotal:function(){
        var subtotal=0;
        var total=0;
        var count=this.store.getCount();
        for(var i=0;i<count;i++){
            total=parseFloat(this.store.getAt(i).data['amount']);
            subtotal+=getRoundedAmountValue(total);
        }
        return getRoundedAmountValue(subtotal);
    },
    calTDSAssasableSubtotal: function () {
        var subtotal = 0;
        var total = 0;
        var count=this.store.getCount();
        for (var i = 0; i < count; i++){
            if (!Wtf.isEmpty(this.store.getAt(i).data['appliedTDS'])) {
                total = parseFloat(this.store.getAt(i).data.tdsamount);
                subtotal += getRoundedAmountValue(total);
            }
        }
        return getRoundedAmountValue(subtotal);
    },
    getTDSNOPArrayAppliedAtLineLevel: function (){
        var tdsNOPArray = [];
        var count=this.store.getCount();
        for (var i = 0; i < count; i++){
            if (!Wtf.isEmpty(this.store.getAt(i).data['appliedTDS'])) {
                var jsonArrayObj = eval(this.store.getAt(i).data.appliedTDS);
                for(var j=0;j<jsonArrayObj.length;j++){
                    var temp = {};
                    temp['natureofpayment'] = jsonArrayObj[j].natureofpaymentName;
                    temp['tdsrate'] = jsonArrayObj[j].tdspercentage + ' %';
                    /*
                     * If  in User Administration > Assign Permission > Display Unit Price & Amount in Purchase Document
                     * If it uncheck we will hide amount and show '*****',  
                     */
                    if(!Wtf.dispalyUnitPriceAmountInPurchase){
                        temp['tdsamount'] = Wtf.UpriceAndAmountDisplayValue;
                    }else{
                        temp['tdsamount'] = !Wtf.isEmpty(jsonArrayObj[j].tdsamount)?WtfGlobal.currencyRenderer(jsonArrayObj[j].tdsamount):WtfGlobal.currencyRenderer(0);
                    }
                    tdsNOPArray.push(temp);
                }
            }
        }
        return tdsNOPArray;
    },
     calLineLevelTax:function(){
        var subtotal=0;
        var total=0;
        var taxTotal=0;
        var taxAmount=0;
        var taxAndSubtotal=[];
        var count=this.store.getCount();
        for(var i=0;i<count;i++){
            total=parseFloat(this.store.getAt(i).data['amount']);
            subtotal+=getRoundedAmountValue(total);
            taxAmount=parseFloat(this.store.getAt(i).data['taxamount']);
            taxTotal+=getRoundedAmountValue(taxAmount);
        }
        taxAndSubtotal[0]=getRoundedAmountValue(subtotal);
        taxAndSubtotal[1]=getRoundedAmountValue(taxTotal);
        return taxAndSubtotal;
    },
    
    calLineLevelTermTaxAmount:function(){
        var lineleveltaxtermTotal = 0;
        var lineleveltaxtermamount=0;
        var count=this.store.getCount();
        for(var i=0;i<count;i++){
            lineleveltaxtermamount = this.store.getAt(i).data['lineleveltaxtermamount'];
            if(lineleveltaxtermamount != undefined && lineleveltaxtermamount!="") {
                lineleveltaxtermamount=parseFloat(lineleveltaxtermamount);
                lineleveltaxtermTotal+=getRoundedAmountValue(lineleveltaxtermamount);
            }
        }
        lineleveltaxtermTotal=getRoundedAmountValue(lineleveltaxtermTotal);
        return lineleveltaxtermTotal;
    },
    
    calSubtotalInBase:function(){
        var subtotalinbase=0;
        var total=0;
        var count=this.store.getCount();
        for(var i=0;i<count;i++){
            total=getRoundedAmountValue(parseFloat(this.store.getAt(i).data['amount']));
            subtotalinbase+=getRoundedAmountValue(total*this.getExchangeRate());
        }
        return getRoundedAmountValue(subtotalinbase);
    },
    
    addBlank:function(){
       //this.setGridDiscValues();
        this.addBlankRow();
    },
     // ERP-13087 Removing the functions that are no longer required
//    setGridDiscValues:function(){
//        this.store.each(function(rec){
//            if(!this.editTransaction)
//                rec.set('prdiscount',0)
//        },this);
//    },
    //setGridProductValues(datachangeflag, custchangeflag)
    setGridProductValues:function(datachangeflag, custchangeflag){
        var rate;
       this.pronamearr=[];
       var productid = "";
       if(this.store.getCount() > 0){
           this.store.each(function(record){
               var recproduct = record.data.productid;
               if(recproduct != undefined && recproduct != "") {
                   productid = productid + recproduct + ",";
               }
        });
        if(this.parentObj && this.parentObj.Currency != undefined){
             this.forCurrency=this.parentObj.Currency.getValue();
        } 
        productid = productid.substring(0, (productid.length - 1) )
            Wtf.Ajax.requestEx({
                    url:"ACCProduct/getIndividualProductPrice.do",
                    params:{
                        productid: productid,
                        affecteduser: this.affecteduser,
                        forCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency:"",
                        currency: this.parentObj.Currency.getValue(),
                        transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                        carryin : (this.isCustomer)? false : true
                    }
                }, this,function(response){
                    var obj = response.data;
                    for(var i=0;i < obj.length ;i++){
                        var datewisepriceResp =obj[i].price;
                        var productidResp =obj[i].productid;
                        var index = this.store.find('productid', productidResp);
                        
                        //Set rate, if not set array value with productname
                        if(index > -1){
                            var record = this.store.getAt(index);
                           
                            var proindex = this.productComboStore.find('productid', productidResp);
                            var prorec=this.productComboStore.getAt(proindex);
                            if(datewisepriceResp == 0){
                                this.pronamearr.push(prorec.get('productname'))
                            }
                            record.set("rate",datewisepriceResp);
                            
                            //Set old currency rate
                            if(this.editTransaction){
                                record.set('oldcurrencyrate',record.get('currencyrate'));
                            }
                            
                            var quantity = record.data.quantity;
                            quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;

                            //Case of copy invoice
                            if((this.copyInv&&prorec.data.quantity<(quantity*record.data.baseomrate)&&prorec.data.type!="Service")){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+prorec.data.productname+WtfGlobal.getLocaleText("acc.field.is")+prorec.data.quantity], 2);
                                record.set("quantity",0);
                                /*For SATS*/
                                if(SATSCOMPANY_ID==companyid){
                                    record.set("showquantity",0);
                                }
                                /**********/
                                record.set("baseuomquantity",0);
                            }
                        }
                    }
//                    if(datachangeflag){
                        this.fireEvent('pricestoreload', this.pronamearr,this);
//                    }
//                    if(custchangeflag){
//                        this.fireEvent('customerchangepriceload', this.pronamearr,this);
//                    }
                });
        
       } else {
           this.fireEvent('pricestoreload', this.pronamearr,this);
       }
    },
    getProductDetails:function(){
    /*    if(this.editTransaction && !this.isOrder){ 
            this.store.each(function(rec){//converting in home currency
                if(rec.data.rowid!=null){
                    var amount,rate;
                    if(this.record && this.record.data.externalcurrencyrate!=undefined&&this.record.data.externalcurrencyrate!=0){
                        amount=rec.get('amount')/this.record.data.externalcurrencyrate;
                        rate=rec.get('rate')/this.record.data.externalcurrencyrate;
                    }else{
                        amount=rec.get('amount')/rec.get('oldcurrencyrate');
                        rate=rec.get('rate')/rec.get('oldcurrencyrate');
                    }
                    rec.set('amount',amount);
                    rec.set('rate',rate);
                    rec.set('permit',(rec.get('permit') != undefined && rec.get('permit') != null)?(rec.get('permit')):"");
                }
            },this);*/
        var isAutoGenerateDO=false;
        if(Wtf.getCmp(this.parentCmpID) != undefined  && Wtf.getCmp(this.parentCmpID).autoGenerateDO != undefined ){
             if(Wtf.getCmp(this.parentCmpID).autoGenerateDO.checked == true){
                 isAutoGenerateDO=true;
             }
       }
        
        this.store.each(function(rec){//converting in home currency
                if(rec.data.rowid==undefined){
                    rec.data.rowid='';
                    
                }
            },this);
        
        var arr=[];
        this.store.each(function(rec){
            var taxpercent=0;
            var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
            rec.set('productaccountid',rec.data.productaccountid);
            if(index>=0){
               var taxrec=this.taxStore.getAt(index);
                taxpercent=taxrec.data.percent;
                rec.set('taxpercent',taxpercent);
            }
            rec.set('qtipdiscountstr',rec.data.qtipdiscountstr);
            rec.set('discountjson',rec.data.discountjson);
//           if((Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel && (this.moduleid==Wtf.Acc_Invoice_ModuleId ||this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId))){
//               if(isAutoGenerateDO){
//                    var storeid="";
//                    var locationid="";
//                    var productRec=WtfGlobal.searchRecord(this.productComboStore,rec.data.productid,'productid')
//                    if(productRec !=null){
//                        storeid=productRec.data.warehouse;
//                        locationid=productRec.data.location;
//                        rec.set('invstore',storeid);
//                        rec.set('invlocation',locationid);
//                    }
//                }
//           }
//            

            if(this.moduleid==Wtf.Acc_Cash_Purchase_ModuleId){
                rec.data[CUSTOM_FIELD_KEY]=Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, Wtf.Acc_Vendor_Invoice_ModuleId).substring(13));                
                rec.data[CUSTOM_FIELD_KEY_PRODUCT]=Wtf.decode(WtfGlobal.getCustomColumnDataForProduct(rec.data, Wtf.Acc_Vendor_Invoice_ModuleId, rec).substring(20));
            }else if(this.moduleid==Wtf.Acc_Cash_Sales_ModuleId){
                rec.data[CUSTOM_FIELD_KEY]=Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, Wtf.Acc_Invoice_ModuleId).substring(13));
                rec.data[CUSTOM_FIELD_KEY_PRODUCT]=Wtf.decode(WtfGlobal.getCustomColumnDataForProduct(rec.data, Wtf.Acc_Invoice_ModuleId, rec).substring(20));
            }else{
                rec.data[CUSTOM_FIELD_KEY]=Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, this.moduleid).substring(13));
                rec.data[CUSTOM_FIELD_KEY_PRODUCT]=Wtf.decode(WtfGlobal.getCustomColumnDataForProduct(rec.data, this.moduleid, rec).substring(20));
            }
            /*
            * Code for allowing zero qty product functionality is activated or not in company preferences
            */
           this.allowZeroQuantity = WtfGlobal.checkAllowZeroQuantityForProduct(this.moduleid);
            if((rec.data.quantity != undefined || this.allowZeroQuantity)&& ((rec.data.quantity!=0 || this.allowZeroQuantity)  || Wtf.account.companyAccountPref.dependentField)){
                arr.push(this.store.indexOf(rec));
            }
        }, this);
        var jarray=WtfGlobal.getJSONArray(this,false,arr);
        //converting back in person currency
 /*       this.store.each(function(rec){
            if(rec.data.rowid!=null && this.fromPO == false){
                var amount,rate;
                if(this.record && this.record.data.externalcurrencyrate!=undefined&&this.record.data.externalcurrencyrate!=0){
                    amount=rec.get('amount')*this.record.data.externalcurrencyrate;
                    rate=rec.get('rate')*this.record.data.externalcurrencyrate;
                }else{
                    amount=rec.get('amount')*rec.get('oldcurrencyrate');
                    rate=rec.get('rate')*rec.get('oldcurrencyrate');
                }
                rec.set('amount',amount);
                rec.set('rate',rate);
                rec.set('permit',(rec.get('permit') != undefined && rec.get('permit') != null)?(rec.get('permit')):"");
            }
        },this);*/
        return jarray;
    },
    checkDetails:function(grid){
        var v=WtfGlobal.checkValidItems(this.moduleid,grid);
        return v;
    },

    checkbatchDetails:function(grid){
        var v=WtfGlobal.checkBatchDetail(this.moduleid,grid);
        return v;
    },

    checkBatchDetailQty:function(grid){
        var v=WtfGlobal.checkBatchDetailQty(this.moduleid,grid);
        return v;
    },
    getCMProductDetails:function(){
        var arr=[];
        var selModel=  this.getSelectionModel();
        var len=this.productComboStore.getCount();
        for(var i=0;i<len;i++){
            if(selModel.isSelected(i)){
            var rec =selModel.getSelected()
            if(rec.data.typeid==2||rec.data.typeid==3)
                //To do - Need to check quantity checks for multi UOM change
                if(rec.data.remquantity==0 && rec.data.type!="Non-Inventory Part" && rec.data.type!="Service"){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleaseenterthequantityofproduct")+rec.data.productname+WtfGlobal.getLocaleText("acc.field.youwanttoreturn") ], 2);
                    return WtfGlobal.getLocaleText("acc.common.error");
                }
            rec.data[CUSTOM_FIELD_KEY]=Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, this.moduleid).substring(13));
            arr.push(i);
            }
            	// arr.push(i); moved to above line cos of issue no: 20258
        }
        return WtfGlobal.getJSONArray(this,true,arr);
    },
    loadPOProduct:function(){
      if(this.store.getCount() > 0){
        this.fireEvent("productselect", this.store.getAt(0).get("productid"));
      }
//        if (this.store.getCount() > 0 && this.store.data != undefined && this.store.data.items.length > 0) {
//            this.store.each(function (rec) {
//                var discountJObj = JSON.parse(this.store.data.items[0].data.discountjson);
//                var defaultprice=rec.data.rate;
//                var quantity=rec.data.quantity;
//                createDiscountString(discountJObj, this, defaultprice, quantity, true);
//            }, this);
//        }

        var setincludeprotaxflag=false; 
        if(this.fromPO)//Linking case true
            this.store.each(function(rec){
                
                if (this.moduleid == Wtf.Acc_Invoice_ModuleId) {
                    var isPartialInvCMB = (!Wtf.isEmpty(this.parentObj) && !Wtf.isEmpty(this.parentObj.partialInvoiceCmb) && this.parentObj.partialInvoiceCmb.getValue());
                    if (rec.json != undefined && rec.json.remainingPartAmt != undefined && rec.json.remainingPartAmt != 0 && isPartialInvCMB) {

                        /* Disabling following fields when Invoice is linking with Partial Sales Order */
                        var unitPrice = this.getColumnModel().findColumnIndex("rate")
                        this.getColumnModel().setEditable(unitPrice, false)
                        var discountType = this.getColumnModel().findColumnIndex("discountispercent")
                        this.getColumnModel().setEditable(discountType, false)
                        var discount = this.getColumnModel().findColumnIndex("prdiscount")
                        this.getColumnModel().setEditable(discount, false)
                        var productTax = this.getColumnModel().findColumnIndex("prtaxid")
                        this.getColumnModel().setEditable(productTax, false)
                        var quantity = this.getColumnModel().findColumnIndex("quantity")
                        this.getColumnModel().setEditable(quantity, false)
                        var taxamount = this.getColumnModel().findColumnIndex("taxamount")
                        this.getColumnModel().setEditable(taxamount, false)


                    } else {

                        /* Enabling following fields when Invoice is linking with  Sales Order normally*/
                        var unitPrice = this.getColumnModel().findColumnIndex("rate")
                        this.getColumnModel().setEditable(unitPrice, true)
                        var discountType = this.getColumnModel().findColumnIndex("discountispercent")
                        this.getColumnModel().setEditable(discountType, true)
                        var discount = this.getColumnModel().findColumnIndex("prdiscount")
                        this.getColumnModel().setEditable(discount, true)
                        var productTax = this.getColumnModel().findColumnIndex("prtaxid")
                        this.getColumnModel().setEditable(productTax, true)
                        var quantity = this.getColumnModel().findColumnIndex("quantity")
                        this.getColumnModel().setEditable(quantity, true)
                        var taxamount = this.getColumnModel().findColumnIndex("taxamount")
                        this.getColumnModel().setEditable(taxamount, true)
                    }
                }
             
                var taxamount= rec.get('rowTaxAmount');//this.calTaxAmount(rec);
                rec.set("taxamount",taxamount);
                //rec.set("prdiscount",0);
             
                /*This is called when in linking case-Written in case of Linking GRO IN VI.
              * If product taxid is present then enabling Include Total Tax through Parent Object this.parentObj-ERP-14230*/
                var producttaxid= rec.get('prtaxid');
                if(this.parentObj && this.parentObj.includeProTax && producttaxid!=null&&producttaxid!=undefined && producttaxid!="" && producttaxid!="None" && !setincludeprotaxflag){
                    setincludeprotaxflag=true;//if one product also has product taxid setting setincludeprotaxflag=true so that it will iterate only one time.
                    this.parentObj.includeProTax.setValue(true); 
                    this.parentObj.isTaxable.disable(); 
                    var id = this.getId();
                    var rowtaxindex=this.getColumnModel().findColumnIndex("prtaxid");
                    var rowtaxamountindex=this.getColumnModel().findColumnIndex("taxamount");
                    this.getColumnModel().setHidden( rowtaxindex,false) ;
                    this.getColumnModel().setHidden( rowtaxamountindex,false) ;
                    this.getView().refresh();//refreshing the grid
                }
                
                if(rec.data.rate===""){
                    var result = this.productComboStore.find('productid',rec.data.productid);
                    if(result >= 0){
                        var prorec=this.productComboStore.getAt(result);
                        rec.set("rate",prorec.data.initialprice);			
                    }                 
                }                
            },this);
        
         if ((this.moduleid == Wtf.Acc_Purchase_Order_ModuleId && Wtf.account.companyAccountPref.isCustShipAddressInPurchase) || ((this.moduleid == Wtf.Acc_Purchase_Order_ModuleId || this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId) &&  CompanyPreferenceChecks.activateDropShip())) {
             /*
              * In cross linking case we are taking customer id from getSalesOrdersRows that's why we are iterate the store Also id user add new product  
             */
            this.store.each(function(rec) {
                 /*
                 * In create case  if Purchase order created by SO then 'customeridforshippingaddress' taken from 'getSalesorderRows'
                 */
                if (this.parentObj !== undefined && rec.json.customeridforshippingaddress !== undefined && rec.json.customeridforshippingaddress !== '') {
                    this.parentObj.customeridforshippingaddress = rec.json.customeridforshippingaddress;
                }
            }, this);
        }
        
      var isNonSaleProduct = false;      
      if(this.isCustomer && this.fromOrder && !this.isNote && !this.readOnly && !this.isOrder)
    	  this.checkSOLinkedProducts();
      if(this.soLinkFlag && this.isOrder ){ //Allow soLinkFlag for Generate SO from PO,Generate PO from SO and Generate CQ using VQ for showing correct prices of products
        this.store.each(function(rec){
            if(this.isCustomer && rec.data.typeid==Wtf.producttype.inventoryNonSale) {
                isNonSaleProduct = true;
                this.store.remove(rec);
            } else {
                this.fireEvent('afteredit', {
                    field : 'productid',
                    value : rec.data.productid,
                    record : rec,
                    soflag : true,
                    linkflag:true,
                    grid:this
                });     
            }
        },this);
            if (isNonSaleProduct) {
                if (this.isQuotation) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.product.productType.nonsale.alert") + " " + WtfGlobal.getLocaleText("acc.accPref.autoCQN")], 2);
                } else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.product.productType.nonsale.alert") + " " + WtfGlobal.getLocaleText("acc.wtfTrans.so")], 2);
                }
            }
         this.soLinkFlag = false;
    }
       /*
        * While Edit case we are putting below two flag from JAVA side 'getPurchaseorderRows' method
       */
        if (this.moduleid == Wtf.Acc_Purchase_Order_ModuleId && this.isEdit && Wtf.account.companyAccountPref.isCustShipAddressInPurchase) {
            this.store.each(function(rec) {
                if (this.parentObj !== undefined && rec.json.customeridforshippingaddress != undefined && rec.json.isPOfromSO != undefined) {
                    this.parentObj.customeridforshippingaddress = rec.json.customeridforshippingaddress;
                    this.parentObj.isPOfromSO = rec.json.isPOfromSO;
                }

            }, this);

        }
       
        if(Wtf.account.companyAccountPref.isLineLevelTermFlag && this.moduleid != Wtf.Acc_Purchase_Requisition_ModuleId  && this.moduleid != Wtf.Acc_RFQ_ModuleId){
            /*
             * Linking cases Populate GST addrees details and get GST tax rule
             */
            if (this.parentObj != undefined && this.parentObj.fromLinkCombo != undefined && this.parentObj.fromPO != undefined
                    && !this.isEdit && !this.copyInv && !this.isViewTemplate) {
                var linkingType = this.parentObj.fromLinkCombo.getValue();
                var isLinking = this.parentObj.fromPO.getValue();
                /**
                 * PR and RFQ linking to VQ or PO
                 */
                if (isLinking && (linkingType == 5 || linkingType == 6)) {
                    this.parentObj.populateGSTDimension(true, true);
                }
                /**
                 * VQ linking to CQ - Cross Linking case
                 */
                if (isLinking && this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId) {
                    this.parentObj.populateGSTDimension(true, true);
                }
                /**
                 * SO linking to PO - Cross Linking case
                 */
                if (isLinking && this.moduleid == Wtf.Acc_Purchase_Order_ModuleId && linkingType == 0 ) {
                    this.parentObj.populateGSTDimension(true, true);
                }
                /**
                 * PO linking to SO - Cross Linking case
                 */
                if (isLinking && this.moduleid == Wtf.Acc_Sales_Order_ModuleId && linkingType == 4 ) {
                    this.parentObj.populateGSTDimension(true, true);
                }
            }
            this.store.each(function(rec){
                if(Wtf.account.companyAccountPref.isLineLevelTermFlag){ // Valuation Type Quantity - India Compliance
                    if(rec.data["valuationType"]==Wtf.excise.QUANTITY){
                        rec.set("quantityInReportingUOM", rec.data["baseuomquantity"] * rec.data["compairwithUOM"]);
                    }
                    if(rec.data["valuationTypeVAT"]==Wtf.excise.QUANTITY){
                        rec.set("quantityInReportingUOMVAT", rec.data["baseuomquantity"] * rec.data["compairwithUOMVAT"]);
                    }
                }
                if(rec.data['LineTermdetails'] != undefined  && rec.data['LineTermdetails'] != ""){
                    var termStore = [];
                    if (this.isModuleForAvalara) {
                        termStore = eval(rec.data['LineTermdetails']);
                    } else {
                        termStore = this.getTaxJsonOfIndia(rec);                   
                    if(this.parentObj && this.parentObj.includingGST && this.parentObj.includingGST.getValue() == true) {
                        this.getColumnModel().setRenderer(this.getColumnModel().findColumnIndex("amount"),WtfGlobal.withoutRateCurrencySymbol);
                        termStore = this.calculateTermLevelTaxesInclusive(termStore, rec);
                    } else {
                        this.getColumnModel().setRenderer(this.getColumnModel().findColumnIndex("amount"),this.calAmountWithoutExchangeRate.createDelegate(this));
                        termStore = this.calculateTermLevelTaxes(termStore, rec);
                    }   
                    }
                    rec.set('LineTermdetails',Wtf.encode(termStore));
                }
            },this);
        }
    },
    
    hideShowCustomizeLineFields:function(){ 
        if(this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId ||this.moduleid==Wtf.Acc_Cash_Sales_ModuleId||this.moduleid==Wtf.Acc_Cash_Purchase_ModuleId||
           this.moduleid==Wtf.Acc_Purchase_Order_ModuleId||this.moduleid==Wtf.Acc_Sales_Order_ModuleId||this.moduleid==Wtf.Acc_Customer_Quotation_ModuleId||this.moduleid==Wtf.Acc_Vendor_Quotation_ModuleId || this.moduleid == Wtf.Acc_Purchase_Requisition_ModuleId || Wtf.Acc_RFQ_ModuleId){
            Wtf.Ajax.requestEx({
                url: "ACCAccountCMN/getCustomizedReportFields.do",
                params: {
                    flag: 34,
                    moduleid:this.moduleid,
                    reportId:1,
                    isFormField:true,
                    isLineField:true
                }
            }, this, function(action, response){
                if(action.success && action.data!=undefined){
                    this.customizeData=action.data;
                    var cm=this.getColumnModel();
                    for(var i=0;i<action.data.length;i++){
                        for(var j=0;j<cm.config.length;j++){
                            if(cm.config[j].dataIndex == 'desc'){
                                if(cm.config[j].header==action.data[i].fieldname ){
                                    cm.setHidden(j, action.data[i].hidecol);
                                    cm.setEditable(j, !action.data[i].isreadonlycol);
                                }
                            } else {
                                if(cm.config[j].dataIndex==action.data[i].fieldDataIndex ){
                                  cm.setHidden(j,action.data[i].hidecol);       
                                  cm.setEditable(j,!action.data[i].isreadonlycol);
                                  if( action.data[i].fieldlabeltext!=null && action.data[i].fieldlabeltext!=undefined && action.data[i].fieldlabeltext!=""){
                                      cm.setColumnHeader(j,action.data[i].fieldlabeltext);
                                  }
                             }
                         }
                        }
                        /*
                         *Hiding the summary Fields('Total Amount' and 'Total Amount(In Base Currency)') on the basis of Unit price in Purchase Requisition
                         */
                        if (action.data[i].fieldDataIndex == 'rate' && action.data[i].hidecol) {
                            if(this.parentObj && this.parentObj.southEastPanel){
                                this.parentObj.southEastPanel.setVisible(!action.data[i].hidecol);
                            }
                            if (this.isRequisition) {
                                var priceSourceIndex = this.getColumnModel().findColumnIndex("priceSource");
                                var pricingbandIndex = this.getColumnModel().findColumnIndex("pricingbandmastername");
                                if (this.readOnly) {
                                    pricingbandIndex = this.getColumnModel().findColumnIndex("pricingbandmastername");
                                } else {
                                    pricingbandIndex = this.getColumnModel().findColumnIndex("pricingbandmasterid");
                                }
                                if (priceSourceIndex != -1 && pricingbandIndex != -1) {
                                    this.getColumnModel().setHidden(priceSourceIndex, true);
                                    this.getColumnModel().setHidden(pricingbandIndex, true);
                                }

                            }
                        }
                    }
                    this.reconfigure( this.store, cm);
                } else {
//                    Wtf.Msg.alert('Status', action.msg);
                }
            },function() {
            });
        }
    },
    
    loadMappedProduct:function(store){ 
        if(this.ProductMappedStore.data.length>0){
            store.each(function(rec){
                        rec.set("taxamount",0);
                        rec.set("quantity",0);
                    var result = this.ProductMappedStore.find('productid',rec.data.productid);
                    if(result >= 0){
                        var prorec=this.ProductMappedStore.getAt(result);
                        rec.set("rate",1);
                        rec.set("baseuomquantity",1);
                        rec.set("displayuomvalue",1);
                        rec.set("availablequantity",prorec.data.availableQtyInSelectedUOM);
                        rec.set("availableQtyInSelectedUOM",prorec.data.availableQtyInSelectedUOM);
                        rec.set("type",prorec.data.type);
                        rec.set("productid",prorec.data.productid);
                        rec.set("uomname",prorec.data.uomname);
                        rec.set("purchaseuom",prorec.data.purchaseuom);
                        rec.set("purchaseuomname",prorec.data.purchaseuomname);
                        rec.set("salesuom",prorec.data.salesuom);
                        rec.set("salesuomname",prorec.data.salesuomname);
                        rec.set("stockuom",prorec.data.stockuom);
                        rec.set("caseuom",prorec.data.caseuom);
                        rec.set("inneruom",prorec.data.inneruom);
                        rec.set("caseuomvalue",prorec.data.caseuomvalue);
                        rec.set("inneruomvalue",prorec.data.inneruomvalue);
                        rec.set("stockpurchaseuomvalue",prorec.data.stockpurchaseuomvalue);
                        rec.set("stocksalesuomvalue",prorec.data.stocksalesuomvalue);
                        rec.set("displayuomrate",1);
                        rec.set("baseuomrate",1);
                        rec.set("prdiscount",0);  
                        rec.set("discountispercent",1);    
                        rec.set("prtaxpercent",0);
                        rec.set("prtaxid","");
                        rec.set("batchdetails","");
                        rec.set("partamount",0);  
                        rec.set("isNewRecord",1);  
                        rec.set("linkid","");  
                        rec.set("gstCurrencyRate",0.0);  
                        rec.set("uomid",prorec.data.uomid);
                        this.isCustomer ? rec.set('salestaxId', prorec.data.salestaxId) : rec.set('purchasetaxId', prorec.data.purchasetaxId);
                        this.isCustomer ?  rec.set('prtaxid',prorec.data.salestaxId) :  rec.set('prtaxid',prorec.data.purchasetaxId);
                        }
                        var customFieldArr;
                if (this.moduleid == Wtf.Acc_Cash_Sales_ModuleId) {
                    customFieldArr = GlobalColumnModel[Wtf.Acc_Invoice_ModuleId ];
                } else if (this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId) {
                    customFieldArr = GlobalColumnModel[Wtf.Acc_Vendor_Invoice_ModuleId];
                } else {
                    customFieldArr = GlobalColumnModel[this.moduleid];
                }
                if (customFieldArr != null && customFieldArr != undefined) {
                    for (var k = 0; k < customFieldArr.length; k++) {
                        rec.set(customFieldArr[k].fieldname,(customFieldArr[k].fieldData!= undefined)?customFieldArr[k].fieldData:"");
                    }
                }

            }, this);
            this.checkMappedProducts(store);
            this.store.each(function(rec){
                this.fireEvent('afteredit', {
                    field : 'productid',
                    value : rec.data.productid,
                    record : rec,
                    soflag : true,
                    grid:this,
                    ispreferredProducts:true
                });                
            },this);
        }    
    },
    checkMappedProducts:function(store){ //checked records quantity and Type
        var msgBox = 0,msg="";
        if(store.data.length){ //Check Qty mentioned in SO/QO is greater than available quantity
            var storeData = [];
            var recordSet=[];
            storeData =store.data.items;
            this.store.removeAll();
            for(var count=0;count<storeData.length;count++){
                var record=storeData[count];
                recordSet[count]=record;
                var quantity = 1;

                var result = this.ProductMappedStore.find('productid',record.data.productid);
                if(result >= 0){
                    var prorec=this.ProductMappedStore.getAt(result);
                    if(!this.editTransaction && !Wtf.account.companyAccountPref.withinvupdate && prorec.data.type!='Service' && quantity > prorec.data.quantity){
                        if(msg==""){
                            msg=record.data.productname+" in "+record.data.billno;
                        }else{
                            msg=msg+","+record.data.productname+" in "+record.data.billno;
                        };
                        msgBox = 1;
                    }
                    else{
                        this.store.add(record);
                    }
                }
            }
        }
    },   
    
    checkSOLinkedProducts:function(){
        var msgBox = 0,msg="";
        if(this.store.data.length){ //Check Qty mentioned in SO/QO is greater than available quantity
            var storeData = [];
            var recordSet=[];
            storeData =this.store.data.items;
            this.store.removeAll();
            for(var count=0;count<storeData.length;count++){
                var record=storeData[count];
                recordSet[count]=record;
                var quantity = 0;
                this.store.each(function(rec){
                  if(rec.data.productid == record.data.productid){
                      
                      var qty = rec.data.quantity;
                      qty = (qty == "NaN" || qty == undefined || qty == null)?0:qty;

                        quantity = quantity + (qty*rec.data.baseuomrate);                                                     
                  }
                },this);
                
                var qty = record.data.quantity;
                qty = (qty == "NaN" || qty == undefined || qty == null)?0:qty;
                
                quantity = quantity + (qty*record.data.baseuomrate);
                var result = this.productComboStore.find('productid',record.data.productid);
                  var ComboIndex=0;
                if(this.productOptimizedFlag!= undefined && result==-1){     //ERP-14058 Added check - this.productOptimizedFlag==Wtf.Products_on_Submit
                    result=1;
                    ComboIndex=-1;
                }
                if(result >= 0){
                    var prorec=this.productComboStore.getAt(result);
                    if(ComboIndex==-1){
                        prorec=record;
                    }
                    if(!this.editTransaction && !Wtf.account.companyAccountPref.withinvupdate && prorec.data.type!='Service' && quantity > prorec.data.quantity){
                        if(msg==""){
                            msg=record.data.productname+" in "+record.data.billno;
                        }else{
                            msg=msg+","+record.data.productname+" in "+record.data.billno;
                        };
                        msgBox = 1;
                    }
                    else{
                        this.store.add(record);
                    }
                }
            }
        }
        
    	if(!Wtf.account.companyAccountPref.withinvupdate&&msgBox==1){
                if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableinstockQtyfor")+msg+'.<br>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                          this.store.removeAll(); 
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   this.store.removeAll();
                                   this.store.add(recordSet);
                                   this.addBlankRow();
                               }else{
                                 Wtf.getCmp(this.parentCmpID).loadStore(); 
                                  return false;
                               }
                            },this); 
                        }
                }
    },
      loadPOGridStore:function(recids, flag, VQtoCQ,linkingFlag,sopolinkflag, isForLinking, isForInvoice, prvqlinkflag,isExplodeAssemblyPrd,prpolinkflag,isMRPJOBWORKIN,productid, sotopolinkflag,dtype,isForSGELinking,dimArr,transactiondate,isJobWorkOutLinkedWithPI,isjobworkwitoutgrn,isJobWorkStockOut,isJobWorkOutRemain,ids){
        this.store.on('beforeload',function(){
            WtfGlobal.setAjaxTimeOut();
        }, this);
        this.store.on('loadexception',function(){
            WtfGlobal.resetAjaxTimeOut();
        },this);
        this.store.load({
            params:{
                bills:recids,
                mode:43,
                prodIds:ids,
                isjobworkwitoutgrn:isjobworkwitoutgrn,
                isJobWorkStockOut:isJobWorkStockOut,
                isJobWorkOutRemain:isJobWorkOutRemain,
                closeflag:true,
                dtype: dtype?"report" : "trans",
                linkingFlag:linkingFlag,
                isForInvoice:isForInvoice,
                isForLinking:isForLinking,
                sopolinkflag:sopolinkflag,
                sotopolinkflag:sotopolinkflag,
                vqtocqlinkflag:VQtoCQ,
                requestModuleid:this.moduleid,
                prvqlinkflag:prvqlinkflag,
                prpolinkflag:prpolinkflag,
                isMRPJOBWORKIN:isMRPJOBWORKIN,
                ids:productid,
                isForm:true,
                isExplodeAssemblyPrd:isExplodeAssemblyPrd?isExplodeAssemblyPrd:false,
                isForSGELinking:isForSGELinking, // flag in case of linking PO in SGE
                dimArr:dimArr,
                transactiondate:WtfGlobal.convertToGenericDate(transactiondate),
                isJobWorkOutLinkedWithPI:isJobWorkOutLinkedWithPI
            }
        });
        this.soLinkFlag = flag;
        /**
         *setting the islinkingFlag= true for the linked products so that
         * price will be refresh or recalculate.
         */
        this.store.on('load', function (store1, recArr) {
            WtfGlobal.resetAjaxTimeOut();
            for (var count = 0; count < recArr.length; count++) {
                var record = recArr[count];
                if (this.parentObj!=undefined && this.parentObj.PO.getValue()!=undefined && this.parentObj.PO.getValue() != "") {
                    record.set('islinkingFlag', true);
                }
            }
            if(this.isjobWorkWitoutGrn){
                if (this.parentObj.isPurchasesTransaction && this.parentObj.purchaseFromURD) {
                    /*** If purchases is from Unregistered dealer ***/
                    if (this.parentObj.isRCMApplicableInPreferences && this.parentObj.GTAApplicable.getValue()) {
                                        this.parentObj.uniqueCase = Wtf.GSTCustVenStatus.APPLYGST;
                } else {
                                        this.parentObj.uniqueCase = Wtf.GSTCustVenStatus.NOGST;
                }
            }
                            processGSTRequest(this.parentObj, this);
            }
            if (this.isModuleForAvalara) {
                var productRecordsArr = [];
                for (var count = 0; count < recArr.length; count++) {
                    var tempObj = recArr[count].data;
                    if (tempObj.pid && tempObj.quantity) { 
                        tempObj.rowIndex = count;
                        productRecordsArr.push(tempObj);
                    }
                }
                getTaxFromAvalaraAndUpdateGrid(this, undefined, productRecordsArr);
            }
            this.getView().refresh();
            if (this.parentObj.isIndiaGST) {
                /**
                 * Show pop up in linking case if mismatch found in GST fields on current date
                 */
                    getLinkDateTocheckGSTDataOnDateCase(this.parentObj, this);
                }
        }, this);
                
    },
    showPriceWindow:function(btn,text,rec, obj){
        if(btn!="yes")return;
        callPricelistWindow(rec,"pricewindow",!this.isCustomer,this.billDate);
   //     this.priceStore.reload();
        Wtf.getCmp("pricewindow").on('update',function(){
            this.fireEvent('afteredit', {
                field : 'productid',
                value : rec.data.productid,
                record : rec,
                soflag : true
            });
        },this);
    },
 // ERP-13087 Removing the functions that are no longer required
   /* setPrevProduct:function(rec,obj){
        obj.cancel=false;
        obj.ckeckProduct=false
        if(this.fireEvent("validateedit", obj) !== false && !obj.cancel){
            obj.record.set(obj.field, obj.value);
            delete obj.cancel;
            this.fireEvent("afteredit", obj);
        }
    },
    */
    setCurrencyid:function(currencyid,rate,symbol,rec,store){
        this.symbol=symbol;
        this.currencyid=currencyid;
        this.rate=rate;
        for(var i=0;i<this.store.getCount();i++){
            this.store.getAt(i).set('currencysymbol',this.symbol);
            this.store.getAt(i).set('currencyrate',this.rate);
        }
//        this.getView().refresh();
    //     this.store.commitChanges();

     },
      // ERP-13087 Removing the functions that are no longer required
  /*  setCurrencyAmount:function(amount){
//    if(this.isNote)
        return amount;
//          return (amount*this.rate)
    },*/
    isAmountzero:function(store){
        var amount;
        var selModel=  this.getSelectionModel();
        var len=this.productComboStore.getCount();
        for(var i=0;i<len;i++){
            if(selModel.isSelected(i)){
                amount=store.getAt(i).data["discamount"];
                if(amount<=0)
                    return true;
            }
        }
        return false;
    },
    checkForPricebandIncludingGst:function(obj){
        if(this.isPriceBandIncludingGst && this.parentObj.includingGST && !this.parentObj.includingGST.getValue()){
            if(this.parentObj.fromLinkCombo && this.parentObj.fromLinkCombo.getValue()!=1){
                /**
                 * Updating rateIncludingGst for All selected product if user clicks on on "YES" for below
                 * alert then show rateIncludingGst wih updated price, if user clicks on "NO" then hide the including gst and
                 * set to empty.
                 */
                var rowRateIncludingGstAmountIndex = this.getColumnModel().findColumnIndex("rateIncludingGst");
                this.getColumnModel().setHidden(rowRateIncludingGstAmountIndex, false);
                obj.record.set("rateIncludingGst", this.defaultPrice);
                Wtf.MessageBox.show({
                    title:WtfGlobal.getLocaleText("acc.common.confirm"),
                    msg:WtfGlobal.getLocaleText("acc.field.priceBandIncludingGstAlert"),
                    width:500,
                    buttons: Wtf.MessageBox.YESNO,
                    scope:this,
                    icon: Wtf.MessageBox.INFO,
                    fn: function(btn){
                        if(btn =="yes") {              
                            this.parentObj.includingGST.setValue(true);
                            this.parentObj.includeProTax.setValue(true);
                            this.parentObj.includeProTax.disable();
                            this.parentObj.isTaxable.setValue(false);
                            this.parentObj.isTaxable.disable();
                            this.parentObj.Tax.setValue("");
                            this.parentObj.Tax.disable();
                            this.getColumnModel().setEditable(this.getColumnModel().findColumnIndex("rate"), false);
                            var rowRateIncludingGstAmountIndex=this.getColumnModel().findColumnIndex("rateIncludingGst");
                            var rowtaxindex=this.getColumnModel().findColumnIndex("prtaxid");
                            var rowtaxamountindex=this.getColumnModel().findColumnIndex("taxamount");
                            this.getColumnModel().setHidden(rowRateIncludingGstAmountIndex,false);
                            this.getColumnModel().setHidden(rowtaxindex,false);
                            this.getColumnModel().setHidden(rowtaxamountindex,false);
                            obj.record.set("rateIncludingGst", this.defaultPrice);
                            if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                                this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                            } else {
                                this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                            }
                            this.parentObj.includingGST.fireEvent('check',this);
                        }else{
                            var rowRateIncludingGstAmountIndex = this.getColumnModel().findColumnIndex("rateIncludingGst");
                            this.getColumnModel().setHidden(rowRateIncludingGstAmountIndex, true);
                            return;
                        }
                    }
                }, this); 
            }   
        }
                },
            getTaxJsonOfIndia:function(prorec){
                var termStore =new Array();
                if(!WtfGlobal.isIndiaCountryAndGSTApplied() && !WtfGlobal.isUSCountryAndGSTApplied()){
                    this.venderDetails =WtfGlobal.searchRecord(this.parentObj.personstore, this.parentObj.Name.getValue(), 'accid');

                    var showOnlyCSTTax=this.venderDetails!=null?this.venderDetails.data.interstateparty:false;
                    var obj_CST =eval(prorec.data['LineTermdetails']);
                    for(var i_CST =0;i_CST<obj_CST.length;i_CST++){
                        if(showOnlyCSTTax && (obj_CST[i_CST].termtype == 1)){
                            if(!this.venderDetails.data.cformapplicable && obj_CST[i_CST].termtype == 1 && obj_CST[i_CST].taxCheck){
                                CSTCNotSubmitFormRate=obj_CST[i_CST].termpercentage;
                            }
                            continue;
                        } else if(showOnlyCSTTax != undefined && !showOnlyCSTTax && (obj_CST[i_CST].termtype == 3)){
                            continue;
                        }
                        if(this.venderDetails!=null && !this.venderDetails.data.cformapplicable && obj_CST[i_CST].termtype == 3 && obj_CST[i_CST].taxCheck){
                            obj_CST[i_CST].termpercentage = CSTCNotSubmitFormRate;
                        }
                        if (obj_CST[i_CST].termtype == 3 ) {
                            if (this.parentObj.FormType != undefined && this.parentObj.FormType.getValue() == obj_CST[i_CST].formType) {
                        termStore.push(obj_CST[i_CST]);
                            } else {
                                continue;
                    }
                        } else {
                            termStore.push(obj_CST[i_CST]);
                        }
                    }
                }else{
                    var obj_term =eval(prorec.data['LineTermdetails']);
                    if(obj_term!=undefined && obj_term!=''){
                        for(var count =0;count<obj_term.length;count++){
                            termStore.push(obj_term[count]);
                        }
                    }
                } 
                
                return termStore;
            },
    
    calculateTermLevelTaxes : function(termStore, rec, index,isNewProduct){
        
        var quantity = rec.data.quantity;
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
        var rate=getRoundofValueWithValues(rec.data.rate,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        quantity=getRoundofValue(rec.data.quantity);
        
        var amount=0;
        amount=rate*quantity;
        
        /*----This block will execute if we create Partial Invoice from SO----- */
        if (this.parentObj != undefined && this.parentObj.partialInvoiceCmb != undefined && this.parentObj.partialInvoiceCmb.getValue()) {
            if (this.parentObj.partialInvAmount.getValue() != 0) {
                amount = amount * (this.parentObj.partialInvAmount.getValue() / 100);  //Calculating Partial amount for the purpose of calculating Taxes in create new case for remaining amount   
            } else if (!this.isEdit && !this.copyInv && rec.json.remainingPartAmt != undefined && rec.json.remainingPartAmt != 0) {
                amount =amount*(rec.json.remainingPartAmt/100);
            }
        }
        var discount = 0;//origionalAmount*rec.data.prdiscount/100   
        if(rec.data.prdiscount > 0) {
            var prdiscount=getRoundedAmountValue(rec.data.prdiscount);
            if(rec.data.discountispercent == 1){
                discount = getRoundedAmountValue((amount * prdiscount) / 100);
            } else {
                discount = prdiscount;
            }
            amount-=discount;
        }      
                
        
        var finaltaxamount = 0;
        var FinalAmountNonTaxableTerm = 0;
        if(index == undefined){
            index = 0;
        }
        var finaltermStore = new Array();
        var uncheckedTerms = this.getUncheckedTermDetails(rec);
        var totalVatTax=0.0;
        var vatFound=false;
        var termSearchTerm="";
        var prorec="";
        var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, rec.data.productid, 'productid');
        if(productComboIndex >=0){
            var prorec = this.productComboStore.getAt(productComboIndex);
            termSearchTerm =eval(prorec.data['LineTermdetails']);
            if(termSearchTerm!=undefined){
            for(var i=0; i<termSearchTerm.length; i++){
                var termJsonCal = termSearchTerm[i];
                if(termJsonCal.termtype==1 && !termJsonCal.isIsAdditionalTax){
                    vatFound = true
                    totalVatTax+=termJsonCal.taxvalue;
                }
            }
        }

        }
        // Iterate List of Terms
        for(var i=index; i<termStore.length; i++){ // Do not change a single character from this line 
            var termJson = termStore[i];
            //In System Control, if Registration Type is "Dealer" then we do not need to show Excise Duty calculation in Invoice.
            if(Wtf.account.companyAccountPref.registrationType == Wtf.registrationTypeValues.DEALER && termJson.termtype == Wtf.LINELEVELTERMTYPE_Excise_DUTY ){ 
                continue;
            }
            var taxamount = 0;
            var assessablevalue = amount;
            
            var formula = termJson.formulaids.split(",");
            // Loop for Formula of Term
            for(var cnt=0; cnt<formula.length; cnt++){
                if(formula[cnt]!='Basic') {
                    var result = finaltermStore.filter(function (termJ) {
                        return termJ.termid == formula[cnt];
                    })[0];
                    if(result == undefined){
                        var tempJson = this.calculateTermLevelTaxes(termStore, rec, i+1);
                        
                        result = tempJson.filter(function (chain) {
                            return chain.termid == formula[cnt];
                        })[0];
                    }
                    if(result != undefined && result.termamount != undefined){
                        assessablevalue = assessablevalue + result.termamount;
                    }
                }
            }
            if(rec.data.valuationType==Wtf.excise.MRP ||rec.data.valuationTypeVAT==Wtf.excise.MRP){
                var calculateabatementonMRP = this.isCalculateAbatementIfMRP(termJson,rec);
                if(calculateabatementonMRP){
                    assessablevalue= rec.data.productMRP*rec.data.baseuomquantity; // if valueation type is MRP than assessablevalue value is MRP*QUANTITY
                }
            }
            // Abatement calculation on Assessable Value
            if(termJson.deductionorabatementpercent){
                if(termJson.termtype == Wtf.term.Service_Tax && termJson.taxtype == 1){ //1 for percentage
                    var termpercentage = termJson.originalTermPercentage - ((termJson.originalTermPercentage * termJson.deductionorabatementpercent)/100);
                    termJson.taxvalue =  termpercentage;
                }else{
                    assessablevalue = (100 - termJson.deductionorabatementpercent) * assessablevalue / 100; //As tax will apply on amount excluding abatement.
                }
            }else if(rec.objField != undefined && rec.objField == 'deductionorabatementpercent'){ //  for term tax value if abatement reset to 0(zero)
                if(termJson.termtype == Wtf.term.Service_Tax && termJson.taxtype == 1){ // For service tax  && 1 for percentage
                        termJson.taxvalue =  termJson.originalTermPercentage ;
                }
            }
            // Apply Tax on Asessable Value
            if (termJson.taxtype == 2) { // If Flat
                taxamount = termJson.termamount;
            } else if (termJson.taxtype == 1) { // If percentage
                var taxamountfun = assessablevalue * termJson.taxvalue / 100;
                taxamount = this.TaxCalculation(termJson, rec, taxamountfun, assessablevalue);
            }
            /**
             * Calculate CESS on Type for INDIA GST
             */
            var isCESSApplicable =true;
            if(WtfGlobal.isIndiaCountryAndGSTApplied() && termJson[Wtf.DEFAULT_TERMID]!=undefined && termJson[Wtf.DEFAULT_TERMID]!='' 
                    && (termJson[Wtf.DEFAULT_TERMID]==Wtf.GSTTerm.OutputCESS || termJson[Wtf.DEFAULT_TERMID]==Wtf.GSTTerm.InputCESS)){
                var params = {};
                var returnArray = calculateCESSONTypeAndValuationAmount(params, quantity, assessablevalue, termJson, taxamount);
                if(returnArray[0]!=undefined){
                    taxamount = returnArray[0];
                }
                if(returnArray[1]!=undefined && !returnArray[1]){
                    isCESSApplicable = returnArray[1];
                }
            }
            termJson.termamount = getRoundedAmountValue(taxamount);
            termJson.assessablevalue = getRoundedAmountValue(assessablevalue);
            
            var isPush = eval(this.addTermForCalculation(termStore,termJson,totalVatTax,termSearchTerm,taxamount,vatFound,rec,uncheckedTerms,isNewProduct));
            
            if (termJson.termtype == Wtf.term.Others && (termJson.IsOtherTermTaxable != undefined && !termJson.IsOtherTermTaxable)) {
                if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                    //we are first rounding taxamount & then sum it , because it causes 0.1 difference in Total Amount.
                    FinalAmountNonTaxableTerm += getRoundedAmountValue(taxamount);
                } else {
                    FinalAmountNonTaxableTerm += taxamount;
                }
            } else {
                var gtaFlag=true;
                if(this.parentObj.GTAApplicable.getValue() && !this.isExciseTab){// For GTA Applicable - Indian Compliance
                    if(termJson.termtype== Wtf.term.Service_Tax || termJson.termtype== Wtf.term.Swachh_Bharat_Cess || termJson.termtype== Wtf.term.Krishi_Kalyan_Cess || termJson.termtype== Wtf.term.GST){
                        gtaFlag=false;
                    }
                }
                if(gtaFlag){// Total amount of line bypass service taxes in case of service tax applicable
                    if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                        /*
                    * ERP-28050 
                    * While calculating Line level taxes if multiple CST taxes are set as default then 
                    * "addTermForCalculation" reconfigures term grid and taxes in the grid
                    *  and tax amount in product grid does not match. If isPush = true then add in taxamount
                    */
                        //we are first rounding taxamount & then sum it , because it causes 0.1 difference in Total Amount.
                        if(isPush){
                            finaltaxamount += getRoundedAmountValue(taxamount);
                        }
                    } else {
                        finaltaxamount += taxamount;
                    }
                }
            }
//            var isPush = eval(this.addTermForCalculation(termStore,termJson,totalVatTax,termSearchTerm,taxamount,vatFound,rec,uncheckedTerms,isNewProduct));
            if(isPush && isCESSApplicable){ // Condition Details - from TERM List which need to add in calculation and which is not.
                finaltermStore.push(termJson);     
            }
            if (termJson.termtype == Wtf.term.Others && (termJson.IsOtherTermTaxable != undefined && !termJson.IsOtherTermTaxable)) {
                if (FinalAmountNonTaxableTerm) {
                    rec.set('OtherTermNonTaxableAmount', getRoundedAmountValue(FinalAmountNonTaxableTerm));
                }else{ //SDP-4606
                    rec.set('OtherTermNonTaxableAmount', getRoundedAmountValue(0));
                }
            } else {
                rec.set('amount', getRoundedAmountValue(amount));
                if (finaltaxamount) {
                    rec.set('recTermAmount', getRoundedAmountValue(finaltaxamount));
                    rec.set('taxamount', getRoundedAmountValue(finaltaxamount));
                } else {
                    rec.set('recTermAmount', getRoundedAmountValue(0));
                    rec.set('taxamount', getRoundedAmountValue(0));
                }
            }
        }
        if (termStore.length < 1) {
            rec.set('amount', getRoundedAmountValue(amount));
            rec.set('recTermAmount', getRoundedAmountValue(0));
            rec.set('taxamount', getRoundedAmountValue(0));
        }
        if(this.checkTermExist(termStore,Wtf.term.CST,totalVatTax) && this.venderDetails.data.interstateparty &&(!Wtf.isEmpty(isNewProduct) && !isNewProduct)){ // Condition Details - It check CST term exist in term list, if yes both(issue form and term's form) have same issue form.
            if(!Wtf.isEmpty(this.parentObj.FormType.getRawValue())){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"CST term with form type <b>"+this.parentObj.FormType.getRawValue()+"</b> is not found/selected in product.<br>Discard this invoice create/select term"], 2);       
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Please select Form To Issue field first"], 2);
            } 
        }
                
        return finaltermStore;
    },
     GTAApplicableCheck : function (store){
        var count = store.getCount()-1;
        var totalServiceTax=0.0;
        for(var i=0;i<count;i++){
            if(!Wtf.isEmpty(this.store.getAt(i)) && !Wtf.isEmpty(this.store.getAt(i).data) && !Wtf.isEmpty(this.store.getAt(i).data.LineTermdetails)){
                var linelevelterm = eval(this.store.getAt(i).data.LineTermdetails)
                var lineleveltermCount = linelevelterm.length;
                for(var j=0;j<lineleveltermCount;j++){
                    if(linelevelterm[j].termtype==Wtf.term.Service_Tax || linelevelterm[j].termtype==Wtf.term.Swachh_Bharat_Cess || linelevelterm[j].termtype==Wtf.term.Krishi_Kalyan_Cess){
                        totalServiceTax+=linelevelterm[j].termamount;
                    }
                }
            }
        }
        return totalServiceTax;
    },
    getUncheckedTermDetails : function (prorec){ // it provide unchecked CST term list
        var uncheckedTerm =eval(prorec.data['uncheckedTermdetails']);
        var termStore =new Array();
        if(!Wtf.isEmpty(uncheckedTerm)){
            for(var i =0;i<uncheckedTerm.length;i++){
                if(uncheckedTerm.termtype=Wtf.term.CST){
                    termStore.push(uncheckedTerm[i]);
                }
            }
        }
        return termStore;
    },
    checkTermExist : function (termStore,term,totalVatTax){
        var formTypeCombo=this.parentObj.FormType.getValue();
        var termCheck=false;
        for(var i =0;i<termStore.length;i++){
            if(Wtf.account.companyAccountPref.enablevatcst && termStore[i].termtype==term){ // Conditon Details - Term of CST found and CST also enable so there must be a term with same form OR same amount
                termCheck=true;
                if(formTypeCombo==termStore[i].formType || totalVatTax==termStore[i].taxvalue){ // If form is same  || CST Term amount = VAT Term Amount is found
                    termCheck=false;
                    break;
                }
            }
        }
        return termCheck;
    },
    TaxCalculation : function (termJson,rec,taxamount,assessablevalue){
        if(Wtf.isExciseApplicable && termJson.termtype == Wtf.term.Excise){ // for special case excise duty | termtype=2 for Excise
            if(rec.data.valuationType==Wtf.excise.QUANTITY && termJson.taxtype==0){ // if valuation type is quentity than calculation on flat_rate*quentity
                if(!Wtf.isEmpty(rec.data.quantityInReportingUOM)){
                    taxamount = termJson.taxvalue*rec.data.quantityInReportingUOM;
                }else{
                    taxamount = 0;
                }
            }else if(rec.data.valuationType==Wtf.excise.MRP && termJson.taxtype==1){ //taxtype=1 for percentage | if valuation type is MRP than calculation on (Assessable Amount* TAX)/100
                taxamount = (assessablevalue*termJson.taxvalue)/100
            } 
        }
        if(Wtf.account.companyAccountPref.enablevatcst && termJson.termtype == Wtf.term.VAT){ // for special case VAT | termtype=1 for VAT
            if(rec.data.valuationTypeVAT==Wtf.excise.QUANTITY && termJson.taxtype==0){ // if valuation type is quentity than calculation on flat_rate*quentity
                if(!Wtf.isEmpty(rec.data.quantityInReportingUOMVAT)) {
                    taxamount = termJson.taxvalue*rec.data.quantityInReportingUOMVAT;
                }else{
                    taxamount = 0;
                }
            }else if(rec.data.valuationTypeVAT==Wtf.excise.MRP && termJson.taxtype==1){ //taxtype=1 for percentage | if valuation type is MRP than calculation on (Assessable Amount* TAX)/100
                taxamount = (assessablevalue*termJson.taxvalue)/100
            } 
        }   
        if(Wtf.account.companyAccountPref.enablevatcst && termJson.termtype == Wtf.term.CST){ // If VAT is on MRP than CST also on MRP
            if(rec.data.valuationTypeVAT==Wtf.excise.MRP && termJson.taxtype==1){ //taxtype=1 for percentage | if valuation type is MRP than calculation on (Assessable Amount* TAX)/100
                taxamount = (assessablevalue*termJson.taxvalue)/100
            } 
        }   
        
        var opmod = termJson.sign==0 ? -1 : 1;
        taxamount = opmod * taxamount;
        return taxamount;
    },
        isCalculateAbatementIfMRP : function (termJson,rec){
        var calculateabatementonMRP=false;
            
        if(Wtf.isExciseApplicable && termJson.termtype== Wtf.term.Excise &&rec.data.valuationType==Wtf.excise.MRP &&rec.data.productMRP!=undefined && rec.data.baseuomquantity!=undefined){
            calculateabatementonMRP=true; // if valueation type is MRP than assessablevalue value is MRP*QUANTITY
        }
        if(Wtf.account.companyAccountPref.enablevatcst && termJson.termtype== Wtf.term.VAT && rec.data.valuationTypeVAT==Wtf.excise.MRP && rec.data.productMRP!=undefined && rec.data.baseuomquantity!=undefined){
            calculateabatementonMRP=true; // if valueation type is MRP than assessablevalue value is MRP*QUANTITY
        }
        
        return calculateabatementonMRP;
    }, 
    addTermForCalculation : function(termStore,termJson,totalVatTax,termSearchTerm,taxamount,vatFound,rec,uncheckedTerms,isNewProduct){
        var addCST=true;
        var needChangeJson=false;
        var termChangeIndex=-1;
        var formTypeCombo="";
        var replaceTermStore="";
        // For CST only -- Start
        if(Wtf.account.companyAccountPref.enablevatcst && termJson.termtype==Wtf.term.CST && (isNewProduct||!this.isEdit)){// Condition Detail - for special case CST | This Logic Only for CST
            formTypeCombo=this.parentObj.FormType.getValue();
            if(Wtf.isEmpty(formTypeCombo)){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Please select Form To Issue field first"], 2);
                addCST=false;
            }
            if(this.venderDetails.data.interstateparty && !Wtf.isEmpty(formTypeCombo) && termJson.formType!=undefined  && vatFound){ //Condition Detail - check interstateParty, and params
                if(termJson.formType==formTypeCombo){ //Condition details - Check issue form and Term's issue form are same or not, if not descard term.
                    if(totalVatTax<termJson.taxvalue &&/* formTypeCombo!="1" &&*/ vatFound ){
                        var result = eval(this.searchCSTTerm(termSearchTerm,totalVatTax))[0]; // First find in checked terms
                        needChangeJson=result.needChangeJson;
                        addCST=result.addCST;
                        replaceTermStore=termSearchTerm;
                        termChangeIndex=result.termChangeIndex;
                        if(!addCST && needChangeJson && !Wtf.isEmpty(uncheckedTerms)){//Condition Detail - If CST term with VAT amount not found in checked term it will check in unchecked term.
                            replaceTermStore = uncheckedTerms; 
                            var result = eval(this.searchCSTTerm(uncheckedTerms,totalVatTax))[0];
                            needChangeJson=result.needChangeJson;
                            addCST=result.addCST;
                            termChangeIndex=result.termChangeIndex;
                        }
                    }
                }
            }    
            //        }
//            if((!termJson.specialTerm || totalVatTax!=termJson.taxvalue) && Wtf.isEmpty(termJson.specialTerm) && !Wtf.isEmpty(formTypeCombo) && termJson.formType!=formTypeCombo){
//                addCST=false;  
//            }
        }
        // For CST only -- End      
            var isPush =false;
            if(vatFound && termJson.termtype==Wtf.term.CST){ // if VAT not found
                if(addCST){
                    if(needChangeJson){
//                        replaceTermStore[termChangeIndex].specialTerm=true;
                        termStore.push(replaceTermStore[termChangeIndex]);
                    } else{
                        isPush=true;
                    }
                }else{
                    if(needChangeJson){ 
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"CST Rate is exceeds from Local Sales Tax Rate and CST with "+totalVatTax+"% rate is not found.<br>So, Discard this invoice and create the CST@"+totalVatTax+"% term"], 2);       
                    }
                }
            }else if(addCST){
                isPush=true;
            }
        
        return isPush;      
    },
    searchCSTTerm : function(termSearchTerm,totalVatTax){
        var addCST=true;
        var needChangeJson=false;
        var termChangeIndex=-1;
        for(var i=0; i<termSearchTerm.length; i++){ // Finding term equal value of VAT |  
            var termJsonCheck = termSearchTerm[i];
                needChangeJson=true;
                addCST=false;
            if(termJsonCheck.termtype==Wtf.term.CST && termJsonCheck.formType=="1" && totalVatTax==termJsonCheck.taxvalue){
                addCST=true;
                termChangeIndex=i;
                break;
            }
        }
        var json = '[{"addCST":'+addCST+',"needChangeJson":'+needChangeJson+',"termChangeIndex":'+termChangeIndex+'}]'; // is JSON
        return json;
    },
    calculateTermLevelTaxesInclusive : function(termStore, rec, index){
        //        var unitPriceIncludingTax = rec.data.amount;
        var finaltaxamount = 0;
        var FinalAmountNonTaxableTerm =0;
        var finaltermStore = new Array();
        
        var quantity = rec.data.quantity;
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
        var rateIncludingGst=getRoundofValueWithValues(rec.data.rateIncludingGst,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        quantity=getRoundofValue(rec.data.quantity);
                
        var quantityAndAmount=0;
        quantityAndAmount=rateIncludingGst*quantity;
        // SDP-4111 Calculate Inclusive Discount Amount At line level India .
        if(rec.data.prdiscount > 0) {        
            var prdiscount=getRoundedAmountValue(rec.data.prdiscount);
            if(rec.data['discountispercent'] == 1){
                quantityAndAmount = quantityAndAmount - ((quantityAndAmount * prdiscount) / 100);
            } else {
                quantityAndAmount = quantityAndAmount - prdiscount;
            }
        }
        var unitPriceIncludingTax = quantityAndAmount;
        
        
        sortArrOfObjectsByParam(termStore, "termsequence", false);
         /**
         * Calculate Total Perecntages 
         */
        var totalTaxPercentage = 0;
        for(var i=0; i<termStore.length; i++){
             var termJson = termStore[i];
             if(termJson.taxtype == 1){ // If percentage
                totalTaxPercentage = totalTaxPercentage + termJson.taxvalue;
            }
        }
        // Iterate List of Terms in Reverse Order
        for(var i=0; i<termStore.length; i++){
//        for(var i=termStore.length-1; i>=0; i--){
            var termJson = termStore[i];
            var taxamount = 0;
            
            // Apply Tax on Asessable Value
            if(termJson.taxtype == 2){ // If Flat
                taxamount = termJson.termamount;
            }else if(termJson.taxtype == 1){ // If percentage
                //taxamount = getRoundedAmountValue(unitPriceIncludingTax*termJson.taxvalue/(termJson.taxvalue+100));// assessablevalue * termJson.taxvalue / 100;
                taxamount = getRoundedAmountValue((unitPriceIncludingTax / ((100 + totalTaxPercentage) / 100) * termJson.taxvalue)/100);
            }
            /**
             * For Including GST tax assesble value is same for all taxes (GST India)
             */
           // unitPriceIncludingTax = unitPriceIncludingTax - taxamount;
            
            termJson.termamount = getRoundedAmountValue(taxamount);
            termJson.assessablevalue = getRoundedAmountValue(unitPriceIncludingTax);
            var RCMApplicable = this.parentObj && this.parentObj.GTAApplicable  ? this.parentObj.GTAApplicable.getValue() : false;
            if (termJson.termtype == Wtf.term.Others && (termJson.IsOtherTermTaxable != undefined && !termJson.IsOtherTermTaxable)) {
                //we are first rounding taxamount & then sum it , because it causes 0.1 difference in Total Amount.
                FinalAmountNonTaxableTerm += getRoundedAmountValue(taxamount);
            } else {
                finaltaxamount += taxamount;
            }
            finaltermStore.push(termJson);
        }
         /**
         * Update Assessable Value
         */
        for(var i=0; i<finaltermStore.length; i++){
            finaltermStore[i].assessablevalue = getRoundedAmountValue(unitPriceIncludingTax - finaltaxamount);
        }
        
        sortArrOfObjectsByParam(finaltermStore, "termsequence", true);
        if(finaltaxamount>=0){
           if(!RCMApplicable) {
                rec.set('recTermAmount', getRoundedAmountValue(finaltaxamount));
                rec.set('taxamount', getRoundedAmountValue(finaltaxamount));
            }else{
                rec.set('recTermAmount', getRoundedAmountValue(0));
                rec.set('taxamount', getRoundedAmountValue(0));
            }
           rec.set('amount', getRoundedAmountValue(quantityAndAmount- finaltaxamount));
            if (this.parentObj && this.parentObj.includingGST && this.parentObj.includingGST.getValue()) {
                rec.set('rate', getRoundedAmountValue((quantityAndAmount - finaltaxamount)/quantity));
            }
            rec.set('amountwithouttax', getRoundedAmountValue(quantityAndAmount- finaltaxamount));
        }
        if(FinalAmountNonTaxableTerm){
            rec.set('OtherTermNonTaxableAmount', getRoundedAmountValue(FinalAmountNonTaxableTerm));
        }
        
        return finaltermStore;
    },
    recalculateTaxAmountWithDiscount:function(rec,taxamount,taxpercent){
        var discountFigure = (rec.data.prdiscount != null || rec.data.prdiscount != undefined || rec.data.prdiscount != '')?rec.data.prdiscount:0;
        var discountType = (rec.data.discountispercent != null || rec.data.discountispercent != undefined || rec.data.discountispercent != '') ? rec.data.discountispercent:null;
        var newTaxAmount=taxamount;
        var taxableAmount=0;
        var lineTermAmount = (rec.data.lineleveltermamount == undefined || rec.data.lineleveltermamount == null || rec.data.lineleveltermamount == '')?0:rec.data.lineleveltermamount;
        if(discountType != null){
            var rate=getRoundofValueWithValues(rec.data.rateIncludingGst,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
            var quantity=getRoundofValue(rec.data.quantity);
            var origionalAmount = getRoundedAmountValue(rate*quantity);
            var discountedAmt = 0;
            if(discountType == 1){  // Percentage
                discountedAmt =  origionalAmount - getRoundedAmountValue(origionalAmount * discountFigure/ 100);
            } else {  // Flat
                discountedAmt = origionalAmount - discountFigure;
            }
            discountedAmt = discountedAmt+lineTermAmount;
            taxableAmount = getRoundedAmountValue((100*discountedAmt)/(100+taxpercent));
            newTaxAmount = getRoundedAmountValue((taxableAmount*taxpercent)/100);
        }
        return newTaxAmount;
    },
    setPIDForBarcode : function(prorec,field,isTypeAhead){
        var rawValue = field.getRawValue();
//        if (this.productOptimizedFlag != Wtf.Products_on_Submit) {    // Commenting this block. Reason: same product is getting populated on next row.
//            field.setValue(prorec.productid);
//        } else {
//            field.setValue(prorec.pid);
//        }
        var count = this.store.getCount();
        var rowIndex = this.rowIndexForSpecialKey != undefined ? this.rowIndexForSpecialKey : ( count-1 ) ;
        var rec = this.store.getAt(rowIndex);
        for (var key in prorec) {
            if (prorec.hasOwnProperty(key)) {
                rec.data[key] = prorec[key];
            }
        }
        rec.data.productid = prorec.productid;
        rec.data.productCode = prorec.pid;           // Replace rawValue with pid as rawValue might not be productCode always.
        rec.data.quantity = "";
           
        if(isTypeAhead){
            /*
             *In Case we are fetching data from backend so it takes time and afteredit event of grid is called before we get response from backend..
             *So manually firing afteredit event for product id.
             **/
            this.fireEvent('afteredit', {
                field : this.productOptimizedFlag != Wtf.Products_on_Submit  ? 'productid' : 'pid',
                value : this.productOptimizedFlag != Wtf.Products_on_Submit  ?  prorec.productid : prorec.pid,
                record : rec,
                isForBarcode:true,
                grid:this
            });
        }
    },
    openWindowForSelectingTDS: function (record, rowindex) { // call TDS window
//        var gstCodeSelected = record.data['accountid'];
        var businesspersoninfo = WtfGlobal.searchRecord(this.parentObj.personstore, this.parentObj.Name.getValue(), 'accid');
        if (!Wtf.isEmpty(businesspersoninfo) && businesspersoninfo != null) {
            businesspersoninfo = businesspersoninfo.data;
            var appliedTDS = record.data['appliedTDS']?record.data['appliedTDS']:'';
            var TDSJEMappring = !Wtf.isEmpty(record.data['tdsjemappingID']); // If TDS je Mapping you are not allow to edit TDS data
            var advancePaymentNop ="";
            var advancePaymentInfo = WtfGlobal.searchRecord(this.parentObj.AdjustAdvancePayments.store, this.parentObj.AdjustAdvancePayments.getValue(), 'AdvancePaymentID');
            if (!Wtf.isEmpty(advancePaymentInfo) && advancePaymentInfo != null) {
                advancePaymentNop = advancePaymentInfo.data.natureofpayment
            }
            this.TDSPaymentWindow = new Wtf.account.TDSPaymentWindow({
                id: 'tdstaxesexpancewindow',
                isReceipt: false,
                border: false,
                readOnly: this.readOnly || this.isLinkedTransaction || TDSJEMappring, // Send readOnly Proeprty to TDS payment window
                isEdit: this.editTransaction,
                accountId: '',
                appliedTDS: appliedTDS,
                parentObj: this,
                personInfo: businesspersoninfo,
                record: record,
                callFrom: 'invoice',// Call From Invoice
                basicExemptionExceeded: false,
                advancePaymentNop:advancePaymentNop
            });
            this.TDSPaymentWindow.on('beforeclose', function (winObj) {
                if (winObj.isSubmitBtnClicked) {
                    this.setTDSToSelectedRow(winObj.getSelectedRecords(), record, rowindex);
                }
            }, this);
            this.TDSPaymentWindow.show();
        }
    },
    setTDSToSelectedRow:function(jsonArray,record,rowindex){ // set TDS assessable amount on Submit TDS window
        var jsonArrayObj = eval(jsonArray);
        var totalTDSamount=0;
        for(var i=0;i<jsonArrayObj.length;i++){
            if(!Wtf.isEmpty(jsonArrayObj[i].tdsamount) && !Wtf.isEmpty(jsonArrayObj[i])){
                totalTDSamount+=parseFloat(jsonArrayObj[i].tdsamount);
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Please fill all the data"], 2);
                return false;
            }
        }
        var recordToSet=this.store.getAt(rowindex);
        var adjustedTotalAmount=0;
        for(var i=0;i<this.store.getCount();i++){
            var rec=this.store.getAt(i);
            if(i!=rowindex && !Wtf.isEmpty(rec.get("appliedTDS"))){
                var appliedTDS = eval(rec.get("appliedTDS"));
                if(!Wtf.isEmpty(appliedTDS[0].advancePaymentDetails)){
                    var advPayTDS = eval(appliedTDS[0].advancePaymentDetails);
                    adjustedTotalAmount += advPayTDS[0].adjustedAdvanceTDSamount;
                }
            }
        }
        var advPay= eval(jsonArrayObj[0].advancePaymentDetails);
        var advjsonArray = !Wtf.isEmpty(advPay)?advPay:[];
        var advjsonData = !Wtf.isEmpty(advPay)?advPay[0]:{};
        if(totalTDSamount>0 && this.parentObj.TotalAdvanceTDSAdjustmentAmt-adjustedTotalAmount>0){
            if(totalTDSamount>(this.parentObj.TotalAdvanceTDSAdjustmentAmt-adjustedTotalAmount)){
                advjsonData['adjustedAdvanceTDSamount'] = (this.parentObj.TotalAdvanceTDSAdjustmentAmt-adjustedTotalAmount);
            }else{
                advjsonData['adjustedAdvanceTDSamount'] = totalTDSamount;
            }
            advjsonData["goodsReceiptDetailsAdvancePaymentId"] = this.parentObj.AdjustAdvancePayments.getValue();
            advjsonData["paymentamount"] = this.parentObj.TotalAdvanceTDSAdjustmentAmt;
        }
        advjsonArray.push(advjsonData);
        jsonArrayObj[0].advancePaymentDetails = JSON.stringify(advjsonArray);
        jsonArray = JSON.stringify(jsonArrayObj);
        recordToSet.set('tdsamount',totalTDSamount);
        recordToSet.set('appliedTDS',jsonArray);
        this.fireEvent('datachanged',this);
    },
    uomdisplay: function () {
        if (this.uomStoreBasedOnUOMSchema.getCount() != 0) {
            Wtf.uomStore.filterBy(function (rec) {
                var isRecordFound = false;
                for (var cnt = 0; cnt < this.uomStoreBasedOnUOMSchema.getCount(); cnt++) {
                    var newrec = this.uomStoreBasedOnUOMSchema.getAt(cnt);
                    if (rec.data.uomid === newrec.data.uomid) {
                        isRecordFound = true;
                        break;
    }
                }
                return isRecordFound;
            }, this);
        }else{
            Wtf.uomStore.clearFilter();
        }
    },
    updateGridState: function (state) {
        if (state && state.columns) {
            for (var i = 0; i < state.columns.length; i++) {
                var column = Object.assign(new Object(), state.columns[i]);
                if (column.id == 'productid') {
                    column.id = 'pid';
                } else if (column.id == 'pricingbandmasterid') {
                    column.id = 'pricingbandmastername';
                } else if (column.id == 'uomid') {
                    column.id = 'uomname';
                } else if (column.id == 'dependentType') {
                    column.id = 'value';
                } else {
                    continue;
                }
                state.columns.splice(i+1, 0, column);
            }
        }
        return state;
    },
    addorRemoveBomCodeColumn: function (prorec, isAutoGenerateDO, store) {
        var assemblyProductInGrid = false;
      if (prorec!=undefined && prorec!=null&&(prorec.data.type == "Inventory Assembly" || prorec.data.type == "Job Work Assembly") && isAutoGenerateDO) {
            assemblyProductInGrid = true;
        } else {
            var storeitemarr = store.data, itemarr = storeitemarr.items, ia = itemarr.length

            for (var i = 0; i < ia; i++) {
                var itemdata = itemarr[i];
                var itemtype = itemdata.data;
                if ((itemtype.type == "Inventory Assembly" || itemtype.type == "Job Work Assembly") && isAutoGenerateDO) {
                    assemblyProductInGrid = true;
                    break;
                }
            }
        }
        if (assemblyProductInGrid) {
            var rowbomindex = this.getColumnModel().findColumnIndex("bomcode");
            this.getColumnModel().setHidden(rowbomindex, false);
        } else {
            var rowbomindex = this.getColumnModel().findColumnIndex("bomcode");
            this.getColumnModel().setHidden(rowbomindex, true);
        }
    }
});
function isLastProductDeleted(store,record){
    var deletedLastProductOfLinkedDocument = false;
    if(store && record){
            var linkedId = record.data.linkid;
        var recordCount = 0;
            store.each(function (rec) {
                if(rec.data.linkid == linkedId){
                    recordCount++;
                }
        }, this);
            if(recordCount== 1){
                 deletedLastProductOfLinkedDocument=true;
        }
    }
    return deletedLastProductOfLinkedDocument;
}

/*Function is used to check whether selected product is linked product or not*/
function isLinkedProduct(store, record, isEdit) {
    var isProductOfLinkedDocument = false;
    var linkedId = "";
    if (store && record) {

        if (isEdit) {
            linkedId = record.data.linkid;
        } else {
            linkedId = record.data.billid;
        }

        if (linkedId != "") {
            store.each(function(rec) {
                if (isEdit) {
                    if (rec.data.linkid == linkedId) {
                        isProductOfLinkedDocument = true;
                    }
                } else {
                    if (rec.data.billid == linkedId) {
                        isProductOfLinkedDocument = true;
                    }
                }
            }, this);
        }

    }
    return isProductOfLinkedDocument;
}

function sortArrOfObjectsByParam(arrToSort /* array */, strObjParamToSortBy /* string */, sortAscending /* bool(optional, defaults to true) */) {
    if(sortAscending == undefined) sortAscending = true;  // default to true
    
    if(sortAscending) {
        arrToSort.sort(function (a, b) {
            return a[strObjParamToSortBy] > b[strObjParamToSortBy];
        });
    }
    else {
        arrToSort.sort(function (a, b) {
            return a[strObjParamToSortBy] < b[strObjParamToSortBy];
        });
    }
}

function calculateDiscount(discountJsonArr, record, defaultPrice, quantity,isCallFromUpadteRow) {
    var jsonStr = "data : [";
    var isFlat = false;
    var sumFlat = 0;
    var sumPercentage = 0;
    var jsonArr = [];
    this.discountvaluesFlat = [];
    this.discountvaluesPercent = [];
    var quat = quantity == undefined || quantity == "" ? 0 : quantity;
    if (discountJsonArr != undefined && discountJsonArr != '') {
        for (var i = 0; i < discountJsonArr.length; i++) {
            if (discountJsonArr[i] != undefined && discountJsonArr[i]) {
                if (discountJsonArr[i].discounttype == 1) {
                    this.discountvaluesFlat[i] = (defaultPrice * quat) * (discountJsonArr[i].discountvalue / 100);
                    this.discountvaluesPercent[i] = discountJsonArr[i].discountvalue;
                    sumFlat += this.discountvaluesFlat[i];
                    sumPercentage += this.discountvaluesPercent[i];
                } else {
                    isFlat = true;
                    if (quat != 0) {
                        sumFlat += discountJsonArr[i].discountvalue;
                    }
                }
                this.qtipString += getQtipString(discountJsonArr[i], i + 1);

            }
            jsonStr += discountJsonArr[i];
            jsonArr.push(discountJsonArr[i]);
        }
    }
    jsonStr += ']';
    if (sumFlat > 0 || sumPercentage > 0)
    {
        if (!isCallFromUpadteRow) {
            record.set("qtipdiscountstr", this.qtipString);
        }
    } else {
        record.set("qtipdiscountstr", "");
    }
    if (isFlat) {
        sumFlat = getRoundedAmountValue(sumFlat);
        record.set("prdiscount", sumFlat);
        record.set("discountispercent", 0);
    } else {
        sumPercentage = getRoundedAmountValue(sumPercentage);
        record.set("prdiscount", sumPercentage);
        record.set("discountispercent", 1);
    }
    return jsonArr;
}

function createDiscountString(response,record,defaultPrice,quantity){
    var jsonArr = [];
    this.qtipString = "Following are the discount applied : \n<br>";
    jsonArr=calculateDiscount(response.discountData,record,defaultPrice,quantity);
    return jsonArr;
}

function generateDiscountString(jsonData) {
    if (jsonData != undefined && jsonData != "") {
        var discountStr = "Following are the discount applied : \n<br>";
        var discountJObj = JSON.parse(jsonData);
        if (discountJObj != undefined && discountJObj != "") {
            for (var i = 0; i < discountJObj.data.length; i++) {
                discountStr += getQtipString(discountJObj.data[i], i + 1);
            }
            if (discountJObj.data.length > 0) {
                return discountStr;
            } else {
                return "";
            }
        } else {
            return "";
        }
    } else {
        return "";
    }
}

function getQtipString(jsonObj,count){
    var str = count + ". Name : " + jsonObj.discountname + ", Value : " + jsonObj.discountvalue + ", Type : " + (jsonObj.discounttype == "1" ? "Percentage" : "Flat") + "<br>";    
    return str;
}
function discountRenderer(v, rec) {
    var returnStr = "";
    if (CompanyPreferenceChecks.discountMaster()) {
        var qtipStr = "";
        if (rec.data['qtipdiscountstr'] != undefined && rec.data['qtipdiscountstr'] != '') {
            qtipStr = rec.data['qtipdiscountstr'];
        } else if (rec.data.discountjson != undefined && rec.data.discountjson != '') {
            qtipStr = generateDiscountString(rec.data.discountjson);
        }
        returnStr = '<div wtf:qtip="' + qtipStr + '" wtf:qtitle="Discount Details" class="currency">' + v + '</div>';
    } else {
        returnStr = '<div class="currency">' + v + '</div>';
    }
    return returnStr;
}
