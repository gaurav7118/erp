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
Wtf.account.DeliveryOrderGrid=function(config){
    this.isCustomer=config.isCustomer;
    this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
    this.currencyid=config.currencyid;
    this.productID=null;
    this.id=config.id;
    this.tempStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },Wtf.productRec)
    });
//    this.isOrder=config.isOrder;
    this.record=config.record;
    this.billDate=new Date();
    this.dateChange=false;
    this.isNegativeStock=false;
    this.isGST=config.isGST;   // ERP-32829 
    this.pronamearr=[];
    this.fromPO=config.fromPO;
    this.readOnly=config.readOnly;
//    this.copyInv=config.copyInv;
    this.copyTrans=config.copyTrans;
    this.editTransaction=config.editTransaction;
    this.heplmodeid = config.heplmodeid;
    this.parentid=config.parentid;
    this.isJobworkOrder=config.isJobworkOrder,
    this.jobworkorderid=config.jobworkorderid,
    this.noteTemp=config.noteTemp;
    this.editLinkedTransactionQuantity= Wtf.account.companyAccountPref.editLinkedTransactionQuantity;
    this.editLinkedTransactionPrice= Wtf.account.companyAccountPref.editLinkedTransactionPrice;
    this.isLinkedTransaction= (config.isLinkedTransaction == null || config.isLinkedTransaction == undefined)? false : config.isLinkedTransaction;
    this.UomSchemaType=Wtf.account.companyAccountPref.UomSchemaType;
    this.isAutoFillBatchDetails=(Wtf.account.companyAccountPref.isAutoFillBatchDetails == undefined || Wtf.account.companyAccountPref.isAutoFillBatchDetails == false ) ? false : Wtf.account.companyAccountPref.isAutoFillBatchDetails;
    this.isEdit=config.isEdit;
    this.ExciseAmt=0,this.VatAmt=0,this.ServiceAmt=0,this.TotalTaxAmt=0;
    this.CSTAmt=0,this.SBCAmt=0,this.KKCAmt=0,this.otherAmt=0;
    this.forCurrency="";
    this.gridConfigId="";
    this.fromOrder=config.fromOrder;
    this.moduleid = config.moduleid;
    //Flag to indicate whether Avalara integration is enabled and module is enabled for Avalara Integration or not
    this.isModuleForAvalara = (Wtf.account.companyAccountPref.avalaraIntegration && (config.moduleid == Wtf.Acc_Delivery_Order_ModuleId)) ? true : false;
    this.AvailableQuantity=0;
    this.CUSTOM_KEY = "customfield";
    this.commonproductStore="";
        this.productComboStore=this.isCustomer?Wtf.productStoreSalesOptimized:Wtf.productStoreOptimized;
      this.isDeferredRevenueRecognition=Wtf.account.companyAccountPref.isDeferredRevenueRecognition&&this.isCustomer;
     this.sModel = new Wtf.grid.CheckboxSelectionModel({
            singleSelect :false
//            hidden:!this.setValuesToMultipleRec
     });  
    this.sModel.on('selectionchange',function(){this.fireEvent('onselection',this);},this);
    this.sModel.on("beforerowselect", this.checkSelections, this);
//    if(config.isNote!=undefined)
//        this.isNote=config.isNote;
//    else
//        this.isNote=false;
//    this.isCN=config.isCN;
//    this.isViewCNDN=config.isViewCNDN;
//    this.isQuotation=config.isQuotation;
    this.createStore();
    this.createComboEditor();
    this.createColumnModel();
//    this.loadPriceStore();
    var colModelArray = [];
    colModelArray = GlobalColumnModel[this.moduleid];
    WtfGlobal.updateStoreConfig(colModelArray, this.store);//ERP-5454
    colModelArray = [];
    if(colModelArray) {
        colModelArray=(GlobalColumnModelForProduct[this.moduleid]);        //ERP-12878
    }
    WtfGlobal.updateStoreConfig(colModelArray, this.store);
    Wtf.account.DeliveryOrderGrid.superclass.constructor.call(this,config);
    this.addEvents({
        'datachanged':true,
        'pricestoreload':true,
        'onselection':true,
        'productdeleted':true,
        'gridconfigloaded':true//// Event fire when WtfGlobal.getGridConfig() called and config get applied to grid column
    });
    this.on('populateDimensionValue',this.populateDimensionValueingrid,this);
}
Wtf.extend(Wtf.account.DeliveryOrderGrid,Wtf.grid.EditorGridPanel,{
    clicksToEdit:1,
    stripeRows :true,
    rate:1,
    symbol:null,
    layout:'fit',
    viewConfig:{forceFit:true},
    forceFit:true,
    loadMask:true,
    onRender:function(config){
         Wtf.account.DeliveryOrderGrid.superclass.onRender.call(this,config);
         this.isValidEdit = true;
//         this.on('render',this.addBlankRow,this);                     //    ERP-15992
//         if(this.productOptimizedFlag!=Wtf.Products_on_Submit){
             this.on('afteredit', this.callUpdateRowOnProductComboStoreLoad, this);
//         } else {
//             this.on('afteredit',this.callupdateRowonProductLoad,this);
//         }
         if(Wtf.account.companyAccountPref.isLineLevelTermFlag && this.isEdit){
              this.parentObj.Name.store.load(); // On edit case we need vendor/customer interstate property so this code is only for india
         }
         WtfGlobal.getGridConfig(this,this.moduleid,true,true);
         this.on('validateedit',this.checkRow,this);
         this.on('rowclick',this.handleRowClick,this);
         this.on('cellclick',this.fetchBOMCodes,this);  //To fetch The available bom codes for selected Product ID
         //this.on('beforeedit',this.enableDisableBOMCombo,this);
         this.on('cellclick',this.RitchTextBoxSetting,this);
         this.on('render', function () {
            new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
                this.on('statesave', this.saveGridStateHandler, this);
            }, this);
        }, this);

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
            
            if( e.field == "bomCode" ) {
                this.bomStore.on('beforeload',function(){
                    this.bomStore.baseParams.productid = e.record.get("productid");
                },this);
            }
        if(e.field == "productid" && e.grid.colModel.config[3].dataIndex=="productid"){                
            if(Wtf.account.companyAccountPref.invAccIntegration && !this.isCustomer){                
                var store = e.grid.colModel.config[3].editor.field.store;
                if(store!=undefined && store.data.length>0){                    
                    this.tempStore.removeAll();
                    this.tempStore.add(store.getRange());                
                    this.tempStore.each(function(record){
                        if(record.data.isStopPurchase==true){
                            this.tempStore.remove(record);                                    
                        }
                    },this);                                
                    e.grid.colModel.config[3].editor.field.store=this.tempStore;
                }                
            }                        
         } 
            
             var isRateFieldEditable = true;
             if(!this.isValidEdit){ // Fixed Bug[13888]: Overlaping text box on validation alert messages. [on TAB key navigation]
                 e.cancel= true;
                 this.isValidEdit = true;
             }
             if((e.field == "rate")&& ((e.record.data.isNewRecord =="" && !this.isEdit ) ||(e.record.data.linkid !="" && this.isEdit))){//isNewRecord for nornal records is "0"
                 if(this.editLinkedTransactionPrice && (this.fromOrder||(this.isEdit && this.fromOrder==false))){  
                      e.cancel = true;
                      isRateFieldEditable = false;
                 }
             }
            if (e.field == "rate" && isRateFieldEditable) {	// rate editable for product type "Service"
                var beforeEditRecord = this.productComboStore.getAt(this.productComboStore.find('productid', e.record.data.productid));
                if (beforeEditRecord == undefined || beforeEditRecord == null) {
                    if (e.record.data == undefined || e.record.data.productid == undefined || e.record.data.productid == "") {
                        e.cancel = true;
                    }
                }
            }
             else  if(e.field == "description" && Wtf.account.companyAccountPref.ishtmlproddesc){
                e.cancel=true;
                if(e.record.data.productid!="")
                    this.getPostTextEditor(e);
                   return; 
             }
             
             var isQuantityFieldEditable = true;
             if((e.field == "quantity")&& ((e.record.data.isNewRecord =="" && !this.isEdit ) ||(e.record.data.linkid !="" && this.isEdit))){//isNewRecord for normal records is "0"
                 if(this.editLinkedTransactionQuantity && (this.fromOrder||(this.isEdit && this.fromOrder==false))){  
                      e.cancel = true;
                      isQuantityFieldEditable = false;
                 }
             }
            if (e.field == "quantity" && isQuantityFieldEditable) {
                var beforeEditRecord = this.productComboStore.getAt(this.productComboStore.find('productid', e.record.data.productid));
                if (beforeEditRecord == undefined || beforeEditRecord == null) {
                    if (e.record.data == undefined || e.record.data.productid == undefined || e.record.data.productid == "") {
                        e.cancel = true;
                    }
                }
            }
            if(e.field == "uomid" && this.UomSchemaType==Wtf.UOMSchema){ //Does not allow to change UOM in case product is not MULTIUOM Type
                    var beforeEditRecord=undefined;
                        beforeEditRecord = WtfGlobal.searchRecord(this.store, e.record.data.productid, 'productid');
                        if(beforeEditRecord != undefined && beforeEditRecord.data.multiuom != true ){ 
                               e.cancel = true;
                               return; 
                        }
         } else if(e.field == "uomid" && e.record.data.productid !="" && this.UomSchemaType == Wtf.PackegingSchema ){
                var prorec= WtfGlobal.searchRecord(this.store, e.record.data.productid, 'productid');
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
            if (e.field.indexOf("Custom_") == 0) {
                if (this.readOnly || this.isViewTemplate) {
                    return false;
                }
             }
//         if(this.isDeferredRevenueRecognition){ // Fixed Bug[13888]: Overlaping text box on validation alert messages. [on TAB key navigation]
//                 e.cancel= true;                 
//             }    

         },this); 
         if(this.isLinkedTransaction){
            this.productEditor.setDisabled(true);
            this.uomEditor.setDisabled(true);
            this.rowDiscountTypeCmb.setDisabled(true);
            this.transDiscount.setDisabled(true);
            this.transTaxAmount.setDisabled(true);
            this.transTax.setDisabled(true);
            this.transBaseuomrate.setDisabled(true);
            this.editprice.setDisabled(true);
            this.editPriceIncludingGST.setDisabled(true);
            this.Description.setDisabled(true);
            this.actQuantity.setDisabled(true);
            this.deliQuantity.setDisabled(true);
            
         }
         this.hideShowCustomizeLineFields();
         this.allowZeroQuantity = WtfGlobal.checkAllowZeroQuantityForProduct(this.moduleid);
         
        /*----Showing tax field at line level for Malaysian country when "Map taxes at product level" check is ON----- */
        if (CompanyPreferenceChecks.mapTaxesAtProductLevel() && this.parentObj!=undefined && this.parentObj.includeProTax && this.parentObj.includeProTax.getValue()) {

            var rowtaxindex = this.getColumnModel().findColumnIndex("prtaxid");
            var rowtaxamountindex = this.getColumnModel().findColumnIndex("taxamount");
            this.getColumnModel().setHidden(rowtaxindex, false);
            this.getColumnModel().setHidden(rowtaxamountindex, false);
        }
     },
     
    saveGridStateHandler: function(grid,state){
        if(!this.readOnly){
             WtfGlobal.saveGridStateHandler(this,grid,state,this.moduleid,this.gridConfigId,true);
        }
    },
    /*
     * 
     * @param {String} jwoid
     * @param {boolean} isJobworkOrder
     * @returns {void}
     * @author Sayed kausar Ali (ERP-28590)
     * This functions set isJobworkOrder and jobworkorderid when Job work order is selected while
     * linking in deliverorder.js
     */
    setJobworkorderid : function(jwoid,isJobworkOrder) {
        this.isJobworkOrder=isJobworkOrder;
        this.jobworkorderid=jwoid;  
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
    calSubtotalInBase:function(){
        var subtotalinbase=0;
        var total=0;
        var count=this.store.getCount();
        for(var i=0;i<count;i++){
            total=getRoundedAmountValue(parseFloat(this.store.getAt(i).data['amount']));
            subtotalinbase+=(total*this.getExchangeRate());
        }
        return getRoundedAmountValue(subtotalinbase);
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
                        if (rec.pid == undefined || rec.pid == null || rec.pid == "") {
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
                    this.productComboStore.on('load', this.callupdateRowonProductSubmit, this);
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
            this.productComboStore.un('load', this.callupdateRowonProductSubmit, this);
        }
    },
    
    descriptionRenderer :function(val, meta, rec, row, col, store) {
    var regex = /(<([^>]+)>)/ig;
    //        val = val.replace(/(<([^>]+)>)/ig,"");
    if(val!=undefined){
        var tip = val.replace(/"/g,'&rdquo;');
        meta.attr = 'wtf:qtip="'+tip+'"'+'" wtf:qtitle="'+WtfGlobal.getLocaleText("acc.gridproduct.discription")+'"';
        return val;
    }else{
        return "";
    }
},
    //To fetch The available bom codes for selected Product ID
    fetchBOMCodes:function(grid, rowIndex, columnIndex, e){
        var record = grid.getStore().getAt(rowIndex);  // Get the Record
        var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
        var type = record.get("type");
        var bomid = record.get("bomid");
        if(fieldName == "bomCode" && (type!="Inventory Assembly" && type!="Job Work Assembly" )){
            this.bomcodeCmb.disable();
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                msg: WtfGlobal.getLocaleText("acc.field.bomcode.dogrid.errormsg"),  //This is not a Assembly Product. You cannot select BOM Code.
                buttons: Wtf.MessageBox.OK,
                animEl: 'mb9',
                icon: Wtf.MessageBox.QUESTION
            });            
        } else if(fieldName == "bomCode" && type=="Inventory Assembly" && (bomid!=undefined && bomid!=null && bomid!="" && (this.editTransaction && !this.copyTrans))){  //Edit Case
            this.bomcodeCmb.disable();
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                msg: WtfGlobal.getLocaleText("acc.field.bomcode.dogrid.edit.errormsg"),  //You cannot change the BOM Code.
                buttons: Wtf.MessageBox.OK,
                animEl: 'mb9',
                icon: Wtf.MessageBox.QUESTION
            });
        } else if(fieldName == "bomCode" && type=="Inventory Assembly" && (bomid!=undefined && bomid!=null && bomid!="" && this.copyTrans)){   //Copy Case
            this.bomcodeCmb.enable();
        } else {
            this.bomcodeCmb.enable();
        }
    },

    RitchTextBoxSetting:function(grid, rowIndex, columnIndex, e){
    var record = grid.getStore().getAt(rowIndex);
    var fieldName= grid.getColumnModel().getDataIndex(columnIndex);
    //fieldName here is dataIndex
    if (fieldName === 'originalSourceShippingAddress' && Wtf.account.companyAccountPref.activateGroupCompaniesFlag && Wtf.account.companyAccountPref.isMultiGroupCompanyParentFlag) {
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
            
            this.linkedmodule="";
            var paramsValue={
                productid:record.data.productid,
                moduleid:this.moduleid,
                rowid:record.data.rowid
            }
            if(this.parentObj.fromLinkCombo!=undefined && this.parentObj.fromLinkCombo!=null){
                var linkedid=this.parentObj.fromLinkCombo.getValue();
                if(linkedid===0){
                    this.linkedmodule=Wtf.Acc_Purchase_Order_ModuleId;
                    paramsValue.podetailid=record.data.savedrowid;
                    paramsValue.linkedmodule=this.linkedmodule;
                }
            }
                Wtf.Ajax.requestEx({
                    url:"ACCPurchaseOrderCMN/fetchShippingAddressOfPO.do",
                    params: paramsValue
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
        var v=WtfGlobal.RitchTextBoxSetting(grid, rowIndex, columnIndex, e,this.readOnly);
        return v;
            
    }//end of else
},
       getPostTextEditor: function(e)
    {
        var _tw=new Wtf.EditorWindowQuotation({
            val:e.record.data.description
        });
    	
        _tw.on("okClicked", function(obj){
            var postText = obj.getEditorVal().textVal;
            var styleExpression  =  new RegExp("<style.*?</style>");
            postText=postText.replace(styleExpression,"");
            e.record.set("description",postText);
                 
             
        }, this);
        _tw.show();
    },
     createStore:function(){   
         
         this.taxRec = new Wtf.data.Record.create([
           {name: 'prtaxid',mapping:'taxid'},
           {name: 'prtaxname',mapping:'taxname'},
           {name: 'taxdescription'},
           {name: 'percent',type:'float'},
           {name: 'taxcode'},
           {name: 'accountid'},
           {name: 'accountname'},
           {name: 'applydate', type:'date'},
           {name: 'termid'},
           {name: 'hasAccess'},
           {name: 'termname'}

        ]);
        this.taxStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.taxRec),
    //        url: Wtf.req.account + 'CompanyManager.jsp',
            url : "ACCTax/getTax.do",
            baseParams:{
                mode:33,
                moduleid :this.moduleid,
                includeDeactivatedTax: this.isEdit != undefined ? (this.copyTrans ? false : this.isEdit) : false
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
            this.taxStore.load();

        this.transTax= new Wtf.form.ExtFnComboBox({
            hiddenName:'prtaxid',
            anchor: '100%',
            store:this.taxStore,
            valueField:'prtaxid',
            forceSelection: true,
            displayField:'prtaxname',
//            addNewFn:this.addTax.createDelegate(this),
            scope:this,
            displayDescrption:'taxdescription',
            selectOnFocus:true,
            typeAhead: true,
            mode: 'remote',
            minChars:0,
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
        
        this.transTaxAmount=new Wtf.form.NumberField({
            allowBlank: true,
            allowNegative: false,
            defaultValue:0,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
        });
         
        this.transDiscount=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            defaultValue:0,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
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
        
        this.bomRec = new Wtf.data.Record.create([
            {name: 'bomid'},
            {name: 'bomCode'},
            {name: 'bomName'},
            {name: 'isAutoAssembly'}
        ]);
        this.bomStore = new Wtf.data.Store({
            url: "ACCProduct/getBOMDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.bomRec)
        });
        this.bomcodeCmb = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.bomcode"),
            name: 'bomid',
            store: this.bomStore,
            valueField: 'bomid',
            displayField: 'bomCode',
            allowBlank: false,
            mode: 'remote',
            typeAhead: true,
            //disabled : true,
            anchor:"95%",
            triggerAction: 'all',
            //listWidth: 300,
            extraFields: ['bomName'],
            addNoneRecord: false
        });
         
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
            {name:'uomschematypeid'},
            {name:'isAutoAssembly'},
            {name:'uomname'},
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
            {name:'quantity'},
            {name:'reorderlevel'},
            {name:'leadtime'},
            {name:'purchaseprice'},
            {name:'saleprice'},
            {name:'lockquantity'},
            {name: 'leaf'},
            {name: 'type'},
            {name:'prtaxid'},
            {name:'taxamount'},
            {name:'prtaxpercent'},
            {name:'prtaxname'},        
            {name: 'level'},
            {name: 'initialquantity',mapping:'initialquantity'},
            {name: 'initialprice'},
            {name: 'producttype'},
            {name:'shelfLocation'},
            {name: 'location'},
            {name: 'warehouse'},
            {name:'displayUoMName'},
            {name:'displayUoMid'},
            {name:'displayuomrate',defValue:1.00},
            {name:'displayuomvalue',defValue:""},
            {name:'supplierpartnumber'},
            {name: 'isFromVendorConsign'},
            {name:'amendpurchaseprice'},
            {name:'isamendpurchasepricenotavail'}
        ]);

        this.priceStore = new Wtf.data.Store({        
            url:"ACCProduct/getProductsForCombo.do",
            baseParams:{mode:22,
                termSalesOrPurchaseCheck:this.isCustomer
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.priceRec)
        });
        this.priceStore.on('load',this.setGridProductValues,this);
        
        this.storeRec = Wtf.data.Record.create([
            {name:'rowid'},
            {name:'productname',mapping:(this.isViewCNDN)?'productdetail':null},
            {name:'billid'},
            {name:'billno'},
            {name:'bomCode'},
            {name:'autobuildnumber'},
            {name:'bomid'},
            {name:'isAutoAssembly'},
            {name:'productid'},
            {name:'description'},
            {name:'type'},
            {name:'shelfLocation'},
            {name:'partno'},
            {name:'quantity'},
            {name:'dquantity'},
            {name:'baseuomquantity',defValue:1.00},
            {name:'availableQtyInSelectedUOM',defValue:0.00},
            {name:'isAnotherUOMSelected'},
            {name:'blockLooseSell'},
            {name:'pocountinselecteduom'},
            {name:'socountinselecteduom'},
            {name:'rate'},
            {name:'amount',defValue:0},
            {name:'multiuom'},
            {name:'uomname'},
            {name:'baseuomname'},
            {name:'uomschematypeid'},
            {name:'uomid'},
            {name:'stockuom'},
            {name:'caseuom'},
            {name:'linkDate'},//Date comparison of GR/DO
            {name:'inneruom'},
            {name:'caseuomvalue'},
            {name:'inneruomvalue'},
            {name:'baseuomid'},
            {name:'productuomid'},
            {name:'baseuomrate',defValue:1.00},
            {name:'copyquantity'},
            {name:'invstore'},
            {name:'invlocation'},
            {name:'copybaseuomrate',mapping:'baseuomrate'},//for handling inventory updation 
//            {name:'copyquantity',mapping:'quantity'},
//            {name:'rate'},
//            {name:'rateinbase'},
//            {name:'discamount'},
            {name:'discount'},
            {name:'discountispercent',defValue:1},
            {name:'prdiscount',defValue:0},
            {name:'prtaxid'},
            {name:'prtaxname'},
            {name:'prtaxpercent'},
            {name:'taxamount',defValue:0},
//            {name:'amount'},
            {name:'amountwithtax'},
//            {name:'taxpercent'},
            {name:'remark'},
            {name:'transectionno'},
            {name:'remquantity'},
            {name:'remainingquantity'},
            {name:'oldcurrencyrate'},
            {name: 'currencysymbol',defValue:this.symbol},
            {name: 'currencyrate'},
            {name: 'externalcurrencyrate'},
            {name:'orignalamount'},
            {name:'typeid',defValue:0},
            {name:'isNewRecord',defValue:"0"},
            {name: 'changedQuantity'},
            {name:'producttype'},
            {name:'permit'},
            {name:'linkto'},
            {name:'linkid'},
            {name:'linktype'},
            {name:'savedrowid'},
            {name:'docrowid'},
            {name:'batchdetails'},
            {name:'lockquantity'},
            {name:'islockQuantityflag'},
            {name:'customfield'},
            {name:'productcustomfield'},
            {name:'supplierpartnumber'},
            {name:'linkflag'},
            {name: 'price'}, // added in record due to set auto populate value of price in add price in master window
            {name: 'customer'}, // added in record due to set auto populate value of customer in add price in master window
            {name: 'vendor'}, // added in record due to set auto populate value of vendor in add price in master window
           // {name:'linkid'}
//            {name:'deliveredquantity'},
              {
                 name:'pid' 
              },
            {name:'availablequantity'},
            {name:'warrantyperiod'},
            {name:'warrantyperiodsal'},
            {name: 'isLocationForProduct'},
            {name: 'isWarehouseForProduct'},
            {name: 'isBatchForProduct'},
            {name: 'isSerialForProduct'},
            {name: 'isFromVendorConsign'},
            {name: 'isRowForProduct'},
            {name: 'isRackForProduct'},
            {name: 'isBinForProduct'},
            {name: 'isWarehouseLocationsetCopyCase'},
            {name: 'location'},
            {name: 'warehouse'},
            {name: 'bomValuationArray'},
            {name: 'priceSource'},
            {name:'pricingbandmasterid'},
            {name:'pricingbandmastername'},
            {name: 'rowTaxPercent'},
            {name: 'amountWithoutTax'},
            {name: 'srno', isForSequence:true},
            {name: 'wastageDetails'},
            {name: 'isWastageApplicable', type: 'boolean'},
            {name:'recTermAmount'},
            {name:'OtherTermNonTaxableAmount'},
            {name:'LineTermdetails'},
            {name:'taxclass'},
            {name:'taxclasshistoryid'},
            {name:'hasAccess'},
            {name: 'productweightperstockuom'},
            {name: 'productweightincludingpakagingperstockuom'},
            {name: 'productvolumeperstockuom'},
            {name: 'productvolumeincludingpakagingperstockuom'},
            {name: 'stocktype'},
            {name: 'bomdetailid'},
            {name: 'rateIncludingGst'},
            {name: 'joborderdetail'},
            {name:'joborderdetails'},
            {name: 'displayUoMid'},
            {name: 'displayUoMName'},
            {name: 'displayuomvalue',defValue : ""},
            {name: 'displayuomrate',defValue:1},
            {name: 'isJobWorkOutProd'},
            {name: 'qtipdiscountstr'},
            {name: 'discountjson'},
            {name: 'barcodetype'},
            {name :'purchasetaxId'},
            {name: 'salestaxId'},
            {name: 'isQAEnable'},
            {name: 'amendpurchaseprice'},
            {name: 'isamendpurchasepricenotavail'},
            {name:'replacebatchdetails'},
            {name: 'isUserModifiedTaxAmount', defValue: false}//To calculate tax amount using Adaptive Rounding algo. ERM-1085.
        ]);
        var url=Wtf.req.account+((this.fromOrder||this.readOnly)?((this.isCustomer)?'CustomerManager.jsp':'VendorManager.jsp'):((this.isCN)?'CustomerManager.jsp':'VendorManager.jsp'));
        if(this.fromOrder)
           url=Wtf.req.account+(this.isCustomer?'CustomerManager.jsp':'VendorManager.jsp');
        this.store = new Wtf.data.Store({
            url:url,
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
          this.productId= new Wtf.form.TextField({
            name:'pid'
//            readOnly:true
        });
        
        this.productComboStore.on('beforeload',function(s,o){
                WtfGlobal.setAjaxTimeOut();
                if(!o.params)o.params={};
                var currentBaseParams = this.productComboStore.baseParams;
                currentBaseParams.getSOPOflag=true;
                currentBaseParams.termSalesOrPurchaseCheck = this.isCustomer;
                currentBaseParams.isDefault = true;
                currentBaseParams.startdate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true));
                currentBaseParams.enddate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false));    
                currentBaseParams.searchProductString = this.productOptimizedFlag==Wtf.Products_on_Submit?this.productId.getValue():""; 
                currentBaseParams.moduleid = this.moduleid;  
                currentBaseParams.module_name='DELIVERY_ORDER';
                this.productComboStore.baseParams=currentBaseParams;      
                currentBaseParams.isFreeTextSearching = (this.productOptimizedFlag==Wtf.Products_on_Submit);
                 if(this.ProductloadingMask==undefined){
                this.ProductloadingMask = new Wtf.LoadMask(document.body,{
                    msg : WtfGlobal.getLocaleText("acc.msgbox.57")
                });
                this.ProductloadingMask.show();
            }
            },this); 
            
             this.productComboStore.on("load",function(){
                WtfGlobal.resetAjaxTimeOut();
                if(this.ProductloadingMask)
                    this.ProductloadingMask.hide();
            },this);
            this.productComboStore.on("loadexception",function(){
                WtfGlobal.resetAjaxTimeOut();
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
            if (this.productOptimizedFlag == undefined || this.productOptimizedFlag == Wtf.Show_all_Products) {
                var configforcombo = {displayField: 'pid', extraFields: ['productname', 'type']}; //Passing extra config for combo
                this.productEditor = CommonERPComponent.createProductPagingComboBox(100, 450, 30, this, baseParams, false, false, configforcombo); // For All Product with Paging
            } else {
                this.productEditor = CommonERPComponent.createProductPagingComboBox(100, 450, 30, this, baseParams, true);  // Type Ahead With and without paging
            }
            this.commonproductStore = this.productEditor.store;
            this.commonproductStore.on('beforeload', function (s, o) {
                WtfGlobal.setAjaxTimeOut();
                if (!o.params)
                    o.params = {};
                var currentBaseParams = this.commonproductStore.baseParams;
                currentBaseParams.termSalesOrPurchaseCheck = this.isCustomer;
                currentBaseParams.isDefault = true;
                currentBaseParams.startdate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true));
                currentBaseParams.enddate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false));
                currentBaseParams.searchProductString = this.productOptimizedFlag == Wtf.Products_on_Submit ? this.productId.getValue() : "";
                currentBaseParams.moduleid = this.moduleid;
                currentBaseParams.module_name = 'DELIVERY_ORDER';
                this.commonproductStore.baseParams = currentBaseParams;
                currentBaseParams.isFreeTextSearching = (this.productOptimizedFlag == Wtf.Products_on_Submit);
            }, this); 
 
       this.productEditor.on('beforeselect', function(combo, record, index) {
            if(this.productOptimizedFlag==Wtf.Products_on_type_ahead || this.productOptimizedFlag == Wtf.Show_all_Products){// Load Selected Product
                if(record.data!=undefined &&record.data!=null){
                    var rec=record.data;
                    if(rec.productid!=undefined && rec.productid!=null &&rec.productid!="" ) {
                        var productidarray=[];
                        productidarray.push(rec.productid);
                        this.productComboStore.load({
                            params:{
                                ids : productidarray
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
        
        if(this.productOptimizedFlag!=Wtf.Products_on_Submit){
            this.productEditor.on("blur",function(e,a,b){
                if(Wtf.account.companyAccountPref.invAccIntegration && !this.isCustomer){
                    e.store=this.productComboStore;
                }    
            },this);
           // if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.edit))
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

//        this.productComboStore.on("load",this.loadPriceAfterProduct,this);
        this.remark= new Wtf.form.TextArea({
            name:'remark'
        });        
        
//        this.inventoryStores = new Wtf.form.ComboBox({  //location
//            triggerAction:'all',
//            mode: 'local',
//            valueField:'id',
//            displayField:'name',
//            store:Wtf.inventoryStore,
//            anchor:'90%',
//            typeAhead: true,
//            forceSelection: true,
//            name:'location',
//            hiddenName:'location'
//        });
////     chkinventoryWarehouse();
//         Wtf.inventoryStore.load();
//        this.inventoryLocation = new Wtf.form.ComboBox({   //warehouse
//            triggerAction:'all',
//            mode: 'local',
////            fieldLabel:WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),
//            valueField:'id',
//            displayField:'name',
//            store:Wtf.inventoryLocation,
//            anchor:'90%',
//            typeAhead: true,
//            forceSelection: true,
//            name:'warehouse',
//            hiddenName:'warehouse'
//        });
////        chkinventoryLocation();
//    Wtf.inventoryLocation.load();

this.partno= new Wtf.form.TextField({
            name:'partno',
            maxLength : 255
        });

        this.actQuantity=new Wtf.form.NumberField({
            allowBlank: false,            
            defaultValue:0,
            allowNegative: false,
            maxLength:15,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
        });
        
        this.deliQuantity=new Wtf.form.NumberField({
            allowBlank: false,            
            defaultValue:0,
            allowNegative: false,
            maxLength:15,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
        });                                 
        this.transBaseuomrate=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength:10,
            decimalPrecision: Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT
        });
        this.editprice = new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength:14,
            decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
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
       this.editPriceIncludingGST=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,    
            maxLength:14
        }); 
    },
    showUom:function(){
       callUOM('uomReportWin');
       Wtf.getCmp('uomReportWin').on('update', function(){
           Wtf.uomStore.reload();
       }, this);
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
     onSpecialKey: function (field, e) {
        if (e.keyCode == e.ENTER || e.keyCode == e.TAB) {
            if (field.getRawValue() != "") {
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
                    params: params
                }, this, function (response) {
                    var prorec = response.data[0];
                    if (prorec) {
                        var newrec = new this.productComboStore.reader.recordType(prorec);
                        var obj = {
                            field: this.productOptimizedFlag != Wtf.Products_on_Submit ? 'productid' : 'pid',
                            value: this.productOptimizedFlag != Wtf.Products_on_Submit ? prorec.productid : prorec.pid,
                            record: newrec,
                            row:this.rowIndexForSpecialKey
                        }
                        this.productComboStore.add(newrec);
                        this.checkRow(obj);
                        if (obj.cancel != true) {
                            this.setPIDForBarcode(prorec, field, true);
                        }
                    }
                }, function () {
                });
            }
        }

    },

    createColumnModel:function(){                
        this.summary = new Wtf.ux.grid.GridSummary();      
        this.rowno=new Wtf.grid.RowNumberer();
        var columnArr =[];
        /* Hide/Show flag for Product Weight/Volumetric measurement */
        this.hideShowFlag=true;
        if((this.moduleid== Wtf.Acc_Delivery_Order_ModuleId) && Wtf.account.companyAccountPref.calculateproductweightmeasurment){
           this.hideShowFlag=false; 
        }
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
        },{//ERP-12415 [SJ]
                header:WtfGlobal.getLocaleText("acc.common.add"),
                align:'center',
                width:40,
                renderer: this.addProductList.createDelegate(this)
        },{
            header: WtfGlobal.getLocaleText("acc.invoice.lineItemSequence"),//"Sequence",
            width:65,
            align:'center',
//            dataIndex:'srno',
            name:'srno',
            renderer: Wtf.applySequenceRenderer
        });
        if(this.productOptimizedFlag!=Wtf.Products_on_Submit){
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.product.gridProductID"),//"Product ID",
                width:200,
                dataIndex:'pid',
                renderer:this.readOnly?"":this.getComboNameRenderer(this.productEditor),
                editor:(this.isNote||this.readOnly)?"":this.productEditor
            });
        }else{
            columnArr.push({
                header: WtfGlobal.getLocaleText("acc.product.gridProductID"),//"Product ID",
                dataIndex: 'pid',
                editor:this.readOnly?"":this.productId,
                width:200
            });
        }
        columnArr.push({
             header: WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),//"Product Name",
             dataIndex:'productname'
            
        },{
            header:this.isCN?WtfGlobal.getLocaleText("acc.invoice.gridInvNo"):WtfGlobal.getLocaleText("acc.invoice.gridVenInvNo"),//"Invoice No.":"Vendor Invoice No.",
            width:150,
            dataIndex:this.noteTemp?'transectionno':'billno',
            hidden:!this.isNote
        },{
             header:WtfGlobal.getLocaleText("acc.do.partno"),//"Part No",
             dataIndex:"partno",
             width:250,
             hidden:true,  //made hidden as in GR and DO serial number column has no need to show [ERP-5034]
             editor:(this.readOnly)?"":this.partno
         },{
             header:WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"),//"Description",
             dataIndex:"description",
             hidden:this.isNote,
             width:250,
             editor:(this.isNote||this.readOnly)?"":this.remark,
             renderer:this.descriptionRenderer
//                 function(val){
//                val = val.replace(/(<([^>]+)>)/ig,"");
//                return val;
//                if(val.length<50)
//                    return val;   
//                else
//                    return val.substring(0,50)+" ...";   
            //}
         },
    {
            header:WtfGlobal.getLocaleText("acc.productGrid.MultiCompany.shippingAddress"),//"Shipping Address",
            dataIndex:"originalSourceShippingAddress",
            hidden:!(Wtf.account.companyAccountPref.activateGroupCompaniesFlag && Wtf.account.companyAccountPref.isMultiGroupCompanyParentFlag && this.moduleid==Wtf.Acc_Goods_Receipt_ModuleId) ,
            width:200,
            renderer: function(v,m,rec) {
                return "<a class='tbar-link-text'>"+WtfGlobal.getLocaleText("acc.productGrid.MultiCompany.clickToViewShippingAddress")+"</a>";
            }
        },{
            header: "Build Assembly No.",
            dataIndex:'autobuildnumber',
            id:this.id+'buildnumber',
            editor:"",
            width:200,
            hidden:(Wtf.Acc_Delivery_Order_ModuleId!=this.moduleid)
        });
        columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModelForProduct[this.moduleid],undefined,undefined,this.readOnly);
        columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModel[this.moduleid],undefined,undefined,this.readOnly);
         columnArr.push({
             header:WtfGlobal.getLocaleText("acc.product.supplier"),//"Supplier Part Number",
             dataIndex:"supplierpartnumber",
             hidden:!(Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.partNumber && !this.isCustomer),
             width:150
         },{
             header:WtfGlobal.getLocaleText("acc.field.ShelfLocation"),
             dataIndex:"shelfLocation",
             hidden:!(!this.isCustomer&&Wtf.account.companyAccountPref.invAccIntegration),
             width:250,
             editor:(this.readOnly)?"":new Wtf.form.TextField()
//         },{
//             header:WtfGlobal.getLocaleText("acc.masterConfig.12"), 
//             dataIndex:'invlocation',
//             hidden:true,
////             hidden:!(Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel),
//                 //&& (Wtf.account.companyAccountPref.withinvupdate && (this.isCashType||this.isCN))),
//             width:150,
//             renderer:Wtf.comboBoxRenderer(this.inventoryLocation),
//             editor:(this.readOnly)?"":this.inventoryLocation
//         },{
//             header:WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"), 
//             dataIndex:'invstore',
//             hidden:true,
////             hidden:!(Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel),
//                 //&& (Wtf.account.companyAccountPref.withinvupdate && (this.isCashType||this.isCN))),
//             width:150,
//             renderer:Wtf.comboBoxRenderer(this.inventoryStores),
//             editor:(this.readOnly)?"":this.inventoryStores
         },{
             header:WtfGlobal.getLocaleText("acc.field.ActualQuantity"),
             dataIndex:"quantity",             
             align:'right',
             width:200,
             editor:(this.readOnly)?"":this.actQuantity,
             renderer:this.quantityRenderer
//             renderer:this.storeRenderer(this.productComboStore,"productid","uomname")
        },{
             header:this.isCustomer ? WtfGlobal.getLocaleText("acc.accPref.deliQuant") : WtfGlobal.getLocaleText("acc.field.ReceivedQuantity"),
             dataIndex:"dquantity",
             align:'right',
             width:150,
             editor:(this.readOnly)?"":this.deliQuantity,
             renderer:this.quantityRenderer
//             renderer:this.storeRenderer(this.productComboStore,"productid","uomname")
         },{
            header: WtfGlobal.getLocaleText("acc.field.bomCode"),   //BOM Code
            width:150,
            dataIndex:'bomCode',
            id:this.id+"bomCode",
            hidden:(Wtf.Acc_Delivery_Order_ModuleId!=this.moduleid),
            renderer:this.bomComboRenderer(this.bomcodeCmb),
            editor:this.bomcodeCmb        //(this.isEdit && this.copyTrans) ? this.bomcodeCmb:((this.readOnly || this.isEdit) ? "" : this.bomcodeCmb)
        },{
//            header: WtfGlobal.getLocaleText("acc.field.wastageQuantity"), // "Wastage Quantity",
//            align: 'center',
//            dataIndex: 'wastageDetails',
//            hidden: !(this.moduleid == Wtf.Acc_Delivery_Order_ModuleId && Wtf.account.companyAccountPref.activateWastageCalculation),
//            renderer: this.wastageQuantityRenderer.createDelegate(this)
//        },{
             header: '',
             dataIndex:"serialrenderer",
             align:'center',
             renderer: this.serialRenderer.createDelegate(this),
             hidden:(!Wtf.account.companyAccountPref.isBatchCompulsory && !Wtf.account.companyAccountPref.isSerialCompulsory && !Wtf.account.companyAccountPref.isLocationCompulsory && !Wtf.account.companyAccountPref.isWarehouseCompulsory && !Wtf.account.companyAccountPref.isRowCompulsory && !Wtf.account.companyAccountPref.isRackCompulsory && !Wtf.account.companyAccountPref.isBinCompulsory),
             width:40
         },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridUOM"),//"UOM",
            width:150,
            dataIndex:this.readOnly?'uomname':'uomid',
             renderer:this.readOnly?"":Wtf.comboBoxRendererwithClearFilter(this.uomEditor),
            editor:(this.isNote||this.readOnly)?"":this.uomEditor  //||this.UomSchemaType==Wtf.PackegingSchema
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridRateToBase"),//Base UOM Rate
             dataIndex:"baseuomrate",
             align:'left',
             width:150,
             renderer:this.conversionFactorRenderer(this.productComboStore,"productid","uomname",this.store),
             editor:(this.isNote||this.readOnly||this.UomSchemaType==Wtf.PackegingSchema)?"":(Wtf.account.companyAccountPref.UomSchemaType===0  && Wtf.account.companyAccountPref.isBaseUOMRateEdit) ?this.transBaseuomrate : ""     //Does allow to user to change conversion factor
         },{
            header:WtfGlobal.getLocaleText("acc.productList.unitWeight"),//Unit Weight
            width:150,
            align:'right',
            dataIndex:'productweightperstockuom',
            renderer: WtfGlobal.weightRenderer,
            editor :(this.readOnly)?"": this.productWeightEditor,
            hidden : this.hideShowFlag
        },{
            header:WtfGlobal.getLocaleText("acc.productList.unitWeightWithPackaging"),//'Unit Weight with Packaging',
            width:150,
            align:'right',
            dataIndex:'productweightincludingpakagingperstockuom',
            renderer: WtfGlobal.weightRenderer,
            editor : (this.readOnly)?"":this.productWeightPackagingEditor,
            hidden : this.hideShowFlag
        },{
            header:WtfGlobal.getLocaleText("acc.productList.unitVolume"),//Unit volumetric
            width:150,
            align:'right',
            dataIndex:'productvolumeperstockuom',
            renderer: WtfGlobal.volumeRenderer,
            editor :(this.readOnly)?"": this.productVolumeEditor,
            hidden :this.hideShowFlag 
        },{
            header:WtfGlobal.getLocaleText("acc.productList.unitVolumeWithPackaging"),//'Unit volumetric with Packaging',
            width:150,
            align:'right',
            dataIndex:'productvolumeincludingpakagingperstockuom',
            renderer: WtfGlobal.volumeRenderer,
            editor :(this.readOnly)?"": this.productVolumePackagingEditor,
            hidden :this.hideShowFlag
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridQtyInBase"),//Base UOM Quantity
             dataIndex:"baseuomquantity",
             align:'right',
//             hidden:true,
             width:150,
             renderer:this.storeRenderer(this.productComboStore,"productid","uomname",this.store)
//             editor:(this.isNote||this.readOnly)?"":this.transQuantity
         });         
         /*CHECK TO HIDE SHOW "UNIT PRICE INCLUDING GST" COLUMN BASED ON COMPANY PREFERENCE SETTING FOR DO & GR*/
         if((this.isCustomer?Wtf.account.companyAccountPref.unitPriceInDO:Wtf.account.companyAccountPref.unitPriceInGR)){
            columnArr.push({
            header:(Wtf.account.companyAccountPref.countryid!= Wtf.Country.INDONESIA)?WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingGST"):WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingVAT"),// "Unit Price Including GST",
            dataIndex: "rateIncludingGst",
            align:'right',
            width:150,
            renderer:this.unitPriceRendererWithPermissionCheck.createDelegate(this),
            editor:(this.isNote||this.readOnly || (this.isCustomer?!Wtf.dispalyUnitPriceAmountInSales:!Wtf.dispalyUnitPriceAmountInPurchase)) ? "" : this.editPriceIncludingGST,
            editable:true,
            hidden: true
            });
        }
        columnArr.push({
            header:WtfGlobal.getLocaleText("acc.field.priceBand"),
            width:250,
            align: 'left',
            dataIndex:this.readOnly?'pricingbandmastername':'pricingbandmasterid',
            hidden: (!this.isCustomer && !Wtf.account.companyAccountPref.productPricingOnBands) || (this.isCustomer && !Wtf.account.companyAccountPref.productPricingOnBandsForSales),
            renderer:this.readOnly?"":Wtf.comboBoxRendererwithClearFilter(this.pricingBandMasterEditor),
            editor:(this.isNote||this.readOnly||this.isViewTemplate || (this.isEdit && this.isLinkedTransaction))?"":this.pricingBandMasterEditor //||this.UomSchemaType==Wtf.PackegingSchema
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice"), // "Unit Price",
            dataIndex: "rate",
            align:'right',
            width:150,
            renderer:this.unitPriceRendererWithPermissionCheck.createDelegate(this),
            editor:(this.isNote||this.readOnly || (this.isCustomer?!Wtf.dispalyUnitPriceAmountInSales:!Wtf.dispalyUnitPriceAmountInPurchase)) ? "" : this.editprice,
            editable:true,
            hidden: (!(this.isCustomer?Wtf.account.companyAccountPref.unitPriceInDO:Wtf.account.companyAccountPref.unitPriceInGR) || Wtf.account.companyAccountPref.jobWorkOutFlow)//!(Wtf.account.companyAccountPref.unitPriceConfiguration || this.isCustomer)// show rate column either Unit price configuration is on or company is malasian company and enabled for GST and it is DO
        },{
            header: WtfGlobal.getLocaleText("acc.field.priceSource"), // "Price Source",
            dataIndex: "priceSource",
            id: this.id + "priceSource",
            align: 'left',
            width: 250,
            renderer: WtfGlobal.deletedRenderer,
            hidden: (!this.isCustomer && !Wtf.account.companyAccountPref.productPricingOnBands) || (this.isCustomer && !Wtf.account.companyAccountPref.productPricingOnBandsForSales)
        },{
            header: WtfGlobal.getLocaleText("acc.field.DiscountType"),
            width:150,
            dataIndex:'discountispercent',
            id:this.id+"discountispercent",
//            fixed:true,
            hidden:!(this.isCustomer?Wtf.account.companyAccountPref.unitPriceInDO:Wtf.account.companyAccountPref.unitPriceInGR),
            renderer:Wtf.comboBoxRenderer(this.rowDiscountTypeCmb),
            editor:this.readOnly || (this.inputValue==Wtf.CNDN_TYPE_FOR_MALAYSIA ||this.inputValue==Wtf.NoteForOvercharge)?"":this.rowDiscountTypeCmb
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridDiscount"),//"Discount",
             dataIndex:"prdiscount",
             id:this.id+"prdiscount",
             align:'right',
//             fixed:true,
             width:150,
             hidden:!(this.isCustomer?Wtf.account.companyAccountPref.unitPriceInDO:Wtf.account.companyAccountPref.unitPriceInGR),
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
             editor:this.readOnly || (this.inputValue==Wtf.CNDN_TYPE_FOR_MALAYSIA||this.inputValue==Wtf.NoteForOvercharge)?"":this.transDiscount
         },{
             header: WtfGlobal.getLocaleText("acc.invoice.proTax"),//"Product Tax",
             dataIndex:"prtaxid",
             id:this.id+"prtaxid",
             fixed:true,
             width:150,
             hidden:!(this.isCustomer?Wtf.account.companyAccountPref.unitPriceInDO:Wtf.account.companyAccountPref.unitPriceInGR),// show this column inly for DO
             renderer:Wtf.comboBoxRenderer(this.transTax),
//             editor:(this.isCustomer && Wtf.account.companyAccountPref.countryid=='137'?this.transTax:"")// DO can be applied for tax in Malaysia after 21 days so editor is necessary at here
             editor:this.readOnly?"":this.transTax
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridTaxAmount"),//"Tax Amount",
             dataIndex:"taxamount",
             id:this.id+"taxamount",
           //  fixed:true,
             //align:'right',
             width:150,
             editor:"",//this.transTaxAmount,
             hidden:!(this.isCustomer?Wtf.account.companyAccountPref.unitPriceInDO:Wtf.account.companyAccountPref.unitPriceInGR),// show this column inly for DO
             renderer:this.setTaxAmountWithotExchangeRate.createDelegate(this)
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridAmount"), // "Amount",
             dataIndex:"amount",
             align:'right',
             width:200,
             renderer:(this.isNote?WtfGlobal.withoutRateCurrencySymbol:this.calAmountWithoutExchangeRate.createDelegate(this)),
             hidden: !(this.isCustomer?Wtf.account.companyAccountPref.unitPriceInDO:Wtf.account.companyAccountPref.unitPriceInGR)// show amount column either Unit price configuration is on or for DO
        },{
                header:WtfGlobal.getLocaleText("acc.field.Remarks"),  //"Remark",
                dataIndex:"remark",
                editor:(this.readOnly)?"":this.Description=new Wtf.form.TextArea({
                    maxLength:200,
                    allowBlank: true,
                    xtype:'textarea'
                })
        });
         if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.invoicegrid.TaxAmount"),//"Total Tax Amount",
                dataIndex:"recTermAmount",
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
                dataIndex:"LineTermdetails",
                width: 40,
                renderer: this.addRenderer.createDelegate(this)
                //hidden:  (Wtf.Countryid != Wtf.Country.INDIA) 
            });
        }
        if (CompanyPreferenceChecks.displayUOMCheck()) {
            columnArr.push({
                header: WtfGlobal.getLocaleText("acc.product.displayUoMLabel"),
                width: 140,
                align: 'center',
                dataIndex: 'displayuomvalue',
                renderer: WtfGlobal.displayUoMRenderer(this.productComboStore, "productid", "displayUoMName", this.store)
            });
        }
        if(!this.isNote && !this.readOnly && !this.isLinkedTransaction) {
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),//"Action",
                dataIndex:"gridaction",//data index is needed to find column by data index
                align:'center',
                width:40,
                hidden:this.readOnly,
                renderer: this.deleteRenderer.createDelegate(this)
            });
        }
        /*
         * add Ingradient details button
         */
        if (Wtf.account.companyAccountPref.jobWorkOutFlow && this.moduleid == Wtf.Acc_Goods_Receipt_ModuleId) {
            columnArr.push({
               header:"<span class='headerRow'>" + Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.invoice.BomDetails"),15)+ "</span>",
                dataIndex: "ingradientrenderer",
                align: 'center',
                renderer: this.jobworkRenderer.createDelegate(this),
                hidden: (!Wtf.account.companyAccountPref.jobWorkOutFlow),
                width: 40
            })
        }
        
        if (CompanyPreferenceChecks.discountMaster() && this.moduleid == Wtf.Acc_Delivery_Order_ModuleId) {
            columnArr.push({//added the view or edit discount details icon in grid.
                header: WtfGlobal.getLocaleText("acc.discountdetails.title"),
                align: 'center',
                renderer: function (v, m, rec) {//Icon Renderer for discount details 
                    return "<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.discountdetails.desc") + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.discountdetails.title") + "' class='" + getButtonIconCls(Wtf.etype.discountdetails) + "'></div>";
                },
                dataIndex: 'discountdetailswindow',
                id: this.id + 'discountdetailswindow',
                hidden: false,
                width: 200
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
    addProductList:function(){//ERP-12415[SJ]
        return "<div class='pwnd add-gridrow' wtf:qtip=\"Click to add products\"></div>";
    },
    quantityRenderer:function(val,m,rec){
        if(val == ""){
            return val;
        }else{
            var v = (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            v= WtfGlobal.convertInDecimalWithDecimalDigit(v,"",Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            return v;
        }
    },  
    serialRenderer:function(v,m,rec){
        return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.serial.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.serial.desc.title")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
    },
    jobworkRenderer: function(v, m, rec) {
        return "<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.JobWorkOut.ingradientdaetils") + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.JobWorkOut.ingradientdaetils") + "' class='" + getButtonIconCls(Wtf.etype.doDetails) + "'></div>";
    },
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    addRenderer: function(v, m, rec) {
        var hideUnitPriceAmount = this.isCustomer ? !Wtf.dispalyUnitPriceAmountInSales : !Wtf.dispalyUnitPriceAmountInPurchase;
        if (this.isModuleForAvalara) {
            return getToolTipOfAvalaraTerms(v, m, rec, hideUnitPriceAmount);
        } else {
            return getToolTipOfTermsfun(v, m, rec, hideUnitPriceAmount);
        }
    },
    handleRowClick:function(grid,rowindex,e){
        this.rowIndexForSpecialKey = rowindex; // rowindex
        if(!this.readOnly && e.target.className == "pwndBar2 shiftrowupIcon") {
            moveSelectedRowFormasterItems(grid,0,rowindex);
        }
        if(!this.readOnly && e.target.className == "pwndBar2 shiftrowdownIcon") {
            moveSelectedRowFormasterItems(grid,1,rowindex);
        }
        if(e.getTarget(".delete-gridrow")){
            
            var store = grid.getStore();
            var total = store.getCount();
            var record = store.getAt(rowindex);
           
            /* Function is used to return ID of linked Document If it is Unlinked through Editing*/
            var lastProductDeleted = isLastProductDeleted(store, record);
            if (lastProductDeleted) {
                /* Link to  Combo*/
                var linkToComponent = Wtf.getCmp(this.linkTo);
                
                /* Link combo*/
                var linkComponent = Wtf.getCmp(this.link);
                var message = "Link Information of "
                if (linkToComponent) {
                    message += linkToComponent.lastSelectionText + " <b>";
                }

                if (record && record.data && record.data.linkto) {
                    message += record.data.linkto;
                }
                message += "</b> will be Removed. </br>" + WtfGlobal.getLocaleText("acc.nee.48")


               Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), message, function(btn) {
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
                                        this.parentObj.linkedDocumentId += record.data.linkid + " , ";
                                        arr.remove(record.data.linkid);
                                        component.setValue(arr);
                                    } else if (this.isEdit) {
                                         
                                       /* Reseting Link &  Link to combo if all linked document is getting removed*/
                                       this.parentObj.linkedDocumentId += record.data.linkid + " , ";
                                        arr.remove(record.data.linkid);
                                        component.disable();
                                        linkToComponent.disable();
                                        component.setValue(arr);
                                        linkToComponent.setValue("");
                                        linkComponent.setValue("");
                                        linkComponent.enable();
                                        
                                    } else {
                                        arr.remove(record.data.linkid);
                                        component.setValue(arr);
                                    }
                                }
                            }
                        }
                    }
             
                    var deliverdproqty = record.data.dquantity;
                    deliverdproqty = (deliverdproqty == "NaN" || deliverdproqty == undefined || deliverdproqty == null) ? '' : deliverdproqty;
                    
                    if (record.data.copyquantity != undefined) {
                        var deletedData = [];
                        var newRec = new this.deleteRec({
                            productid: record.data.productid,
                            productname: record.data.productname,
                            productquantity: deliverdproqty,
                            productbaseuomrate: record.data.baseuomrate,
                            productbaseuomquantity: record.data.baseuomquantity,
                            productuomid: record.data.uomid,
                            productinvstore: record.data.invstore,
                            productinvlocation: record.data.invlocation,
                            productrate: record.data.rate
                        });
                        deletedData.push(newRec);
                        this.deleteStore.add(deletedData);
                    }
                    store.remove(store.getAt(rowindex));
                    if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
                        updateTermDetails(this);
                    }
                    if (rowindex == total - 1) {
                        this.addBlankRow();
                    }
                    this.fireEvent('productdeleted', this);
                    this.fireEvent('datachanged', this);
                }, this);
            } else {
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.nee.48"), function(btn) {
                    if (btn != "yes")
                        return;
                    var store = grid.getStore();
                    var total = store.getCount();
                    var record = store.getAt(rowindex);

                    var deliverdproqty = record.data.dquantity;
                    deliverdproqty = (deliverdproqty == "NaN" || deliverdproqty == undefined || deliverdproqty == null) ? 0 : deliverdproqty;

                    if (record.data.copyquantity != undefined) {
                        var deletedData = [];
                        var newRec = new this.deleteRec({
                            productid: record.data.productid,
                            productname: record.data.productname,
                            productquantity: deliverdproqty,
                            productbaseuomrate: record.data.baseuomrate,
                            productbaseuomquantity: record.data.baseuomquantity,
                            productuomid: record.data.uomid,
                            productinvstore: record.data.invstore,
                            productinvlocation: record.data.invlocation,
                            productrate: record.data.rate
                        });
                        deletedData.push(newRec);
                        this.deleteStore.add(deletedData);
                    }
                    store.remove(store.getAt(rowindex));
                    if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
                        updateTermDetails(this);
                    }
                    if (rowindex == total - 1) {
                        this.addBlankRow();
                    }
                    this.fireEvent('productdeleted', this);
                    this.fireEvent('datachanged', this);
                }, this);
            }
        } else if(e.getTarget(".serialNo-gridrow") && this.isLinkedTransaction !=undefined && !this.isLinkedTransaction){
             var store=grid.getStore();
            var record = store.getAt(rowindex);
            this.blockQtyFlag = false;
            var productid = record.get('productid');
             var linkflag=false;
            if(Wtf.getCmp('linkToOrder'+this.heplmodeid+this.parentid))
            {
                linkflag=Wtf.getCmp('linkToOrder'+this.heplmodeid+this.parentid).getValue();
            }
            var linkValue =  parseInt(Wtf.getCmp(this.parentid).fromLinkCombo.getValue());
             if(!isNaN(linkValue) && linkValue===0 && !this.isEdit){ // If Linked SO
                this.blockQtyFlag = true;
            }
           record.data.linkflag=linkflag;
           var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, productid, 'productid');
            if(productComboRecIndex==-1){
                productComboRecIndex=WtfGlobal.searchRecordIndex(store, productid, 'productid');
            }
            if(productComboRecIndex >=0){
                var proRecord = this.productComboStore.getAt(productComboRecIndex);
                var recIndex = WtfGlobal.searchRecordIndex(this.productComboStore, productid, 'productid');
                if(recIndex==-1){
                    proRecord=this.getStore().getAt(productComboRecIndex);
                }
                if(proRecord.data.type!='Service' && proRecord.data.type!='Non-Inventory Part'){  //serial no for only inventory type of product
                    if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.account.companyAccountPref.isLocationCompulsory || Wtf.account.companyAccountPref.isWarehouseCompulsory  || Wtf.account.companyAccountPref.isRowCompulsory  || Wtf.account.companyAccountPref.isRackCompulsory  || Wtf.account.companyAccountPref.isBinCompulsory){ //if company level option is on then only check batch and serial details
                        if(proRecord.data.isLocationForProduct || proRecord.data.isWarehouseForProduct || proRecord.data.isBatchForProduct || proRecord.data.isSerialForProduct || proRecord.data.isRowForProduct || proRecord.data.isRackForProduct || proRecord.data.isBinForProduct||(this.moduleid == Wtf.Acc_Delivery_Order_ModuleId)) // Do moduleid for showing stock type 
                        {
                            this.callSerialNoWindow(record);
                        } else {
                            if (proRecord.data.isWastageApplicable && this.moduleid == Wtf.Acc_Delivery_Order_ModuleId) {
                                this.callWastageQuantiyWindow(record,grid,rowindex);
                            } else {
                                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.batchserial.Functinality")],2);   //Batch and serial no details are not valid.
                                 return;
                             }
                         }
                    }
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.batchserial.funforInventoryitems")],2);   //Batch and serial no details are not valid.
                    return;
                }
            
        }
        } else if(e.getTarget(".add-gridrow")&& this.isLinkedTransaction !=undefined && !this.isLinkedTransaction){//ERP-12415[SJ] 
            if(this.readOnly !=undefined && !this.readOnly){
                if(this.parentObj && this.parentObj.Currency != undefined){
                    this.forCurrency=this.parentObj.Currency.getValue();
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
        } else if (e.getTarget(".pwnd.doDetails-gridrow")){
             if (this.isJobworkOrder) {
                var store = grid.getStore();
                var total = store.getCount();
                var record = store.getAt(rowindex);
                this.callJobOrderDetails(record);
            } else {
                return;
            }
        }else if (e.getTarget(".discountDetails-gridrow")) {
            var store = grid.getStore();
            var record = store.getAt(rowindex);
            var productid = record.get('productid');
//            this.callDiscountDetails(record);                           //calls discount details window in which user can delete the discounts at the time of sales invoice creation which are mapped to product 
            var paramObj = {
                record: record,
                readOnly: this.readOnly,
                parentObj: this.parentObj,
                parentCmpScope: this,
                isLinkedTransaction: this.isLinkedTransaction
            };
            callDiscountDetailsDynamic(paramObj);                           //calls discount details window in which user can delete the discounts at the time of sales invoice creation which are mapped to product 
        }
//        else if (e.getTarget(".wastageQuantity-gridrow")) {
//            var store = grid.getStore();
//            var record = store.getAt(rowindex);
//            var productid = record.get('productid');
//            var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, productid, 'productid');
//            if (productComboRecIndex >= 0) {
//                var proRecord = this.productComboStore.getAt(productComboRecIndex);
//                if (proRecord.data.isWastageApplicable) {
//                    this.callWastageQuantiyWindow(record,grid,rowindex);
//                } else {
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.product.warningWastageApplicableProduct")], 2);
//                    return;
//                }
//            }
//        }
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
            quantity:obj.data.dquantity,
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
    showProductGrid : function() {//ERP-12415[SJ]
         this.productSelWin = new Wtf.account.ProductSelectionWindow({
            renderTo: document.body,
            height : 600,
            width : 750,
            title:WtfGlobal.getLocaleText("acc.productselection.window.title"),
            layout : 'fit',
            modal : true,
            resizable : false,
            id:this.id+'ProductSelectionWindowDO',
            moduleid:this.moduleid,
            heplmodeid:this.heplmodeid,
            parentCmpID:this.parentCmpID,
            invoiceGrid:this,
            isCustomer : this.isCustomer,
            affecteduser: this.affecteduser,
            forCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency:"",
            currency: this.parentObj.Currency.getValue(),
            transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
            carryin : (this.isCustomer)? false : true,
            getSOPOflag :false,
            startdate : WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true)),
            enddate : WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false)),
            tempSOPOLinkFlag: false,
            isIndividualProductPrice:true
        });
        this.productSelWin.show();
        
    },
    
    bomComboRenderer: function(combo) {
        return function(value, metadata, record, row, col, store) {
            var idx = WtfGlobal.searchRecordIndex(combo.store, value, combo.valueField);
            var fieldIndex = "bomCode";
            if (idx == -1) {
                if (record.data["bomCode"] && record.data[fieldIndex].length > 0) {
                    return record.data[fieldIndex];
                } else {
                    return "";
                }
            }
            var rec = combo.store.getAt(idx);
            var displayField = rec.get("bomCode");
            record.set("bomid", value);
            record.set("bomCode", displayField);
            return displayField;
        }
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
    storeRenderer:function(store, valueField, displayField, gridStore) {
        return function(value, meta, record) {
            value=(parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            var idx = store.find(valueField, record.data[valueField]);
           var rec="";
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
    updateRow:function(obj){
        if(obj!=null){    
            var rowRateIncludingGstAmountIndex=this.getColumnModel().findColumnIndex("rateIncludingGst");  
            this.productComboStore.clearFilter(); // Issue 22189
            var rec=obj.record;
            
            /**
             * if islinkingFlag true then price for that product will not be refresh or recalulate
             * only in linking case of any document. 
             */
            if (this.isEdit && this.parentObj != undefined && this.parentObj.PO.getValue() != undefined && this.parentObj.PO.getValue() != "") {
                rec.set('islinkingFlag', true);
            }
            
            /*----Code is written for adding product from window at line level----------*/
            if (obj.isAddProductsFromWindow != undefined && obj.isAddProductsFromWindow && CompanyPreferenceChecks.mapTaxesAtProductLevel()) {

                /* -------------Set Product tax if Product is mapped with any tax--------------- */

                this.showMappedProductTax(rec);

            }
            
            var islinkingFlag = (rec.data.islinkingFlag!=undefined && rec.data.islinkingFlag!="") ?rec.data.islinkingFlag:false;
            var proqty = obj.record.get("quantity");
            proqty = (proqty == "NaN" || proqty == undefined || proqty == null)?0:proqty;
            
            var deliveredproqty = obj.record.get("dquantity");
            deliveredproqty = (deliveredproqty == "NaN" || deliveredproqty == undefined || deliveredproqty == null)?0:deliveredproqty;
             /**
             * if  Amend fuctionalityuser has activated for that user and purchase should 
             * be greater that sales price,If not then system shows pop up msg.
             */
            //ERP-40390
            var includingGst=false;
            if(this.parentObj!=undefined && this.parentObj.includingGST!=undefined && this.parentObj.includingGST.getValue()){
                includingGst=this.parentObj.includingGST.getValue();
            }
            if ((obj.field == "rate"||obj.field == "rateIncludingGst") && this.isCustomer && Wtf.productPriceEditPerm.priceEditPerm.BlockAmendingPrice && this.getamendprice(rec,obj,includingGst,false)) {
                return;
            }
            if((deliveredproqty == 0) && obj.field == "quantity" && Wtf.account.companyAccountPref.autoPopulateDeliveredQuantity){
                obj.record.set("dquantity", proqty);   // ERP-13968 Auto populate the delivery quantity when Actual is entered
                if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) {
                    if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                        this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                    } else {
                        this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                    }
                }
            }
            if(obj.field=="baseuomrate"){
                if(this.isCustomer)
                    rec.set("changedQuantity",(rec.data.copybaseuomrate-obj.value)*((rec.data.copyquantity==="")?1:rec.data.copyquantity));
                  else
                    rec.set("changedQuantity",(obj.value-rec.data.copybaseuomrate)*((rec.data.copyquantity==="")?1:rec.data.copyquantity));
                
                  var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
                  var productuomid = "";
                  if(productComboIndex >=0){
                      var datewiseprice=0,rate=0;
                      prorec = this.productComboStore.getAt(productComboIndex);
                      productuomid = prorec.data.uomid;
                       if(this.UomSchemaType==Wtf.UOMSchema){//for Schema type
                         WtfGlobal.setAjaxTimeOut();
                        Wtf.Ajax.requestEx({
                            url:"ACCProductCMN/getIndividualProductPrice.do",
                            params:{
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
                             WtfGlobal.resetAjaxTimeOut();
                            datewiseprice =response.data[0].price;
                            /**
                             * Checking if price is mapped to UOM if yes the setting the unit price which is set in db else converting it as rule set in UOM schema.
                             * ERM-389 / ERP-35140
                             */
                             var modifiedRate,isPriceMappedToUOM=false;
                            if (response.data[0].isPriceMappedToUOM != undefined && response.data[0].isPriceMappedToUOM != "") {
                                isPriceMappedToUOM = response.data[0].isPriceMappedToUOM;
                            }
                            var displayUoMid = response.data[0].displayUoMid;
                            var displayUoMName = response.data[0].displayUoMName;
                            var displayuomrate = response.data[0].displayuomrate;
                            var displayuomvalue = response.data[0].displayuomvalue;

                            datewiseprice = response.data[0].price;

                            obj.record.set("displayUoMid", displayUoMid);
                            obj.record.set("displayUoMName", displayUoMName);
                            obj.record.set("displayuomrate", displayuomrate);
                            obj.record.set("displayuomvalue", (deliveredproqty * baseuomrate) / displayuomrate);

                            modifiedRate=WtfGlobal.getIndividualProductPriceInMultiCurrency(rec,datewiseprice);
                            if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                                obj.record.set("baseuomquantity", deliveredproqty*obj.value);
                                if (prorec.data.displayUoMid != undefined && prorec.data.displayUoMid != "" && prorec.data.displayUoMid != null && displayuomvalue != 0) {
                                    obj.record.set("displayuomvalue", (deliveredproqty * obj.record.get("baseuomrate")) / obj.record.get("displayuomrate"));
                                }else{
                                    obj.record.set("displayuomvalue", "");
                                }
                                /**
                                * Checking if price is mapped to UOM if yes the setting the unit price which is set in db else converting it as rule set in UOM schema.
                                * ERM-389 / ERP-35140
                                */
                                rate = getRoundofValueWithValues(modifiedRate * obj.value, Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                                modifiedRate = getRoundofValueWithValues(modifiedRate, Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                                obj.record.set("rate", isPriceMappedToUOM ? modifiedRate : rate);
                                if(!(this.isAutoFillBatchDetails && this.isCustomer)){
                                    WtfGlobal.setDefaultWarehouseLocation(obj, rec,true);
                                }
                            }else {
                                obj.record.set("baseuomrate", 1);
                            } 
                            if (obj.record.data.productCode) {
                                obj.record.set("pid", obj.record.data.productCode);
                            }
                            this.fireEvent('datachanged',this);
                        }, function(){

                            });
                    }
//                    if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
//                        obj.record.set("baseuomquantity", quantity*obj.value);
//                        obj.record.set("rate", modifiedRate*obj.value);
//                    } else {
//                        obj.record.set("baseuomrate", 1);
//                    }                      
                }else{
                       productuomid = rec.data.uomid;
                      if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                            obj.record.set("baseuomquantity", deliveredproqty*obj.value);
                            if (rec.data.displayUoMid != undefined && rec.data.displayUoMid != "" && rec.data.displayUoMid != null){
                                obj.record.set("displayuomvalue", (deliveredproqty * obj.record.get("baseuomrate")) / obj.record.get("displayuomrate"));
                            } else {
                                obj.record.set("displayuomvalue", "");
                            }
                            if(!(this.isAutoFillBatchDetails && this.isCustomer)){
                                WtfGlobal.setDefaultWarehouseLocation(obj, rec,true);
                            }
                      } else {
                          obj.record.set("baseuomrate", 1);
                  }
                  }
//                  this.fireEvent('datachanged',this);
            }
            if(obj.field=="uomid"){
                  var prorec = null;
                  var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
                  var productuomid = "";
                  if(productComboIndex >=0){
                      var baseuomrate =1,rateperuom=0,datewiseprice=0,isPriceMappedToUOM=false;
                      prorec = this.productComboStore.getAt(productComboIndex);
                      productuomid = prorec.data.uomid;
                      
                      // getting uom editor record
                      
                    var selectedProRec=this.productComboStore.getAt(this.productComboStore.find('productid',obj.record.data.productid));
                
                    if(selectedProRec==undefined){
                        selectedProRec=obj.record;
                    }
                        
                     var isblockLooseSell = selectedProRec.get('blockLooseSell');
                     
                     
                     
                          //To do - Need to take rate from new window
//                      this.showPriceWindow.createDelegate(this,[rec, obj],true);
                         if(this.UomSchemaType==Wtf.UOMSchema){//for Schema type
                             var selectedUOMRec = WtfGlobal.searchRecord(Wtf.uomStore, obj.value, 'uomid');
                             var uomschemaid;
                            if(productuomid != obj.value){
                                uomschemaid=prorec.data.uomschematypeid;
                            }
                                WtfGlobal.setAjaxTimeOut();
                                obj.record.set("isAnotherUOMSelected", false);
                                Wtf.Ajax.requestEx({
                                url:"ACCProductCMN/getIndividualProductPrice.do",
                                params:{
                                    uomschematypeid:uomschemaid,
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
                                    uomid: rec.data.uomid,
                                    skipRichTextArea:true
                                }
                                }, this,function(response){
                                     WtfGlobal.resetAjaxTimeOut();
                                    baseuomrate =response.data[0].baseuomrate;
                                    rateperuom =response.data[0].rateperuom;
                                    var availableQtyInSelectedUOM =response.data[0].availableQtyInSelectedUOM;
                                    var lockQuantityInSelectedUOM =response.data[0].lockQuantityInSelectedUOM;
                                    var pocountinselecteduom =response.data[0].pocountinselecteduom;
                                    var socountinselecteduom =response.data[0].socountinselecteduom;
                                    datewiseprice =response.data[0].price;
                                    this.defaultPrice=datewiseprice;
                                    /**
                                     * Checking if price is mapped to UOM if yes the setting the unit price which is set in db else converting it as rule set in UOM schema.
                                     * ERM-389 / ERP-35140
                                     */
                                    if(response.data[0].isPriceMappedToUOM!=undefined && response.data[0].isPriceMappedToUOM!=""){
                                        isPriceMappedToUOM=response.data[0].isPriceMappedToUOM;
                                    }
                                    var displayUoMid = response.data[0].displayUoMid;
                                    var displayUoMName = response.data[0].displayUoMName;
                                    var displayuomrate = response.data[0].displayuomrate;
                                    obj.record.set("displayUoMid", displayUoMid);
                                    obj.record.set("displayUoMName", displayUoMName);
                                    obj.record.set("displayuomrate", displayuomrate);
                                    obj.record.set("displayuomvalue", (deliveredproqty * baseuomrate) / displayuomrate);
                                    if(isblockLooseSell && this.moduleid != Wtf.Acc_Goods_Receipt_ModuleId){// product whose loose selling is blocked, need to validate at here
                                        var returnFlag = this.checkAvailableQtyONSelectedUOM(obj, availableQtyInSelectedUOM, lockQuantityInSelectedUOM, baseuomrate)
                                        if(returnFlag){
                                            this.setGridObjectValues(obj, availableQtyInSelectedUOM, baseuomrate, pocountinselecteduom, socountinselecteduom);
                                        }else{
                                            obj.cancel = true;
                                            return false;
                                        }
                                    }else{
                                        obj.record.set("baseuomrate", baseuomrate);
                                        obj.record.set("availableQtyInSelectedUOM", availableQtyInSelectedUOM);
                                        obj.record.set("isAnotherUOMSelected", true);
                                        obj.record.set("baseuomquantity", deliveredproqty*baseuomrate);
                                        if (prorec!=null && prorec.data.displayUoMid != undefined && prorec.data.displayUoMid != "" && prorec.data.displayUoMid != null){// && displayuomvalue != 0) {
                                            obj.record.set("displayuomvalue", (deliveredproqty * obj.record.get("baseuomrate")) / obj.record.get("displayuomrate"));
                                        } else {
                                            obj.record.set("displayuomvalue", "");
                                        }
                                        obj.record.set("pocountinselecteduom", pocountinselecteduom);
                                        obj.record.set("socountinselecteduom", socountinselecteduom);
                                    }
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
                                            obj.record.set("baseuomquantity", deliveredproqty*obj.record.get("baseuomrate"));
                                            obj.record.set("rate",modifiedRate*baseuomrate); 
                                            obj.record.set("rateIncludingGst", modifiedRate*baseuomrate);
                                        }else{
                                            /**
                                             * Checking if price is mapped to UOM if yes the setting the unit price which is set in db else converting it as rule set in UOM schema.
                                             * ERM-389 / ERP-35140
                                             */
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
                                                    obj.record.set("taxamount",taxamount);
                                                    obj.record.set("isUserModifiedTaxAmount", false);
                                            }
                                        }
                                        if (obj.record.data.productCode) {
                                            obj.record.set("pid", obj.record.data.productCode);
                                        }
                                        if(!(this.isAutoFillBatchDetails && this.isCustomer)){
                                            WtfGlobal.setDefaultWarehouseLocation(obj, rec,true);
                                        }
                                        this.fireEvent('datachanged',this);
                                }, function(){

                                });
//                             } else {
//                                obj.record.set("uomname", selectedUOMRec.data['uomname']);
//                                obj.record.set("baseuomrate", 1);
//                                obj.record.set("baseuomquantity", deliveredproqty*obj.record.get("baseuomrate"));
//                            }
//                                });
//                             } else {
//                                obj.record.set("uomname", selectedUOMRec.data['uomname']);
//                                obj.record.set("baseuomrate", 1);
//                                obj.record.set("baseuomquantity", deliveredproqty*obj.record.get("baseuomrate"));
//                            }   
                        }else{ //for packeging UOM type   
                             WtfGlobal.setAjaxTimeOut();
                              Wtf.Ajax.requestEx({
                                    url:"ACCProduct/getIndividualProductPrice.do",
                                    params:{
                                        productid:prorec.data.productid,
                                        affecteduser: this.affecteduser,
                                        transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                                        forCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency:"",
                                        currency: this.parentObj.Currency.getValue(),
                                        quantity: obj.record.data.dquantity,
                                        carryin : (this.isCustomer)? false : true
                                    }
                              }, this,function(response){
                                   WtfGlobal.resetAjaxTimeOut();
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
                                        obj.record.set("baseuomquantity", (deliveredproqty)*(prorec.data.caseuomvalue));
                                        obj.record.set("baseuomrate", (prorec.data.caseuomvalue));
                                        obj.record.set("rate", modifiedRate*(prorec.data.caseuomvalue));
                                     } else if(obj.value == prorec.data.inneruom) {
                                        obj.record.set("baseuomquantity", (deliveredproqty)*(prorec.data.inneruomvalue));
                                        obj.record.set("baseuomrate", (prorec.data.inneruomvalue));
                                        obj.record.set("rate", modifiedRate*(prorec.data.inneruomvalue));
                                     } else {
                                        obj.record.set("baseuomrate", 1);
                                        obj.record.set("baseuomquantity", deliveredproqty*1);
                                        obj.record.set("rate", modifiedRate);
                                     }
                                    if(!(this.isAutoFillBatchDetails && this.isCustomer)){ 
                                        WtfGlobal.setDefaultWarehouseLocation(obj, rec,true);
                                    }
                                    this.fireEvent('datachanged',this);
                              }, function(){

                            });                  
                        }
                  }else if(this.productOptimizedFlag!= undefined){
                    productuomid = rec.data.baseuomid;
                  
                        //To do - Need to take rate from new window
                        //                      this.showPriceWindow.createDelegate(this,[rec, obj],true);
                        
                        var prorec = obj.record;
                        
                        // getting uom editor record
                      
                        var selectedProRec=this.productComboStore.getAt(this.productComboStore.find('productid',obj.record.data.productid));

                        if(selectedProRec==undefined){
                            selectedProRec=obj.record;
                        }

                        var isblockLooseSell = selectedProRec.get('blockLooseSell');

                        if(this.UomSchemaType==Wtf.UOMSchema){//for Schema type
                             var selectedUOMRec = WtfGlobal.searchRecord(Wtf.uomStore, obj.value, 'uomid');

                            
                             if(productuomid != obj.value){
                                uomschemaid=rec.data.uomschematypeid; 
                             }
                                    obj.record.set("isAnotherUOMSelected", false);
                                    WtfGlobal.setAjaxTimeOut();
                                    Wtf.Ajax.requestEx({
                                    url:"ACCProductCMN/getIndividualProductPrice.do",
                                    params:{
                                    uomschematypeid:uomschemaid,
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
                                    uomid: rec.data.uomid,
                                    skipRichTextArea:true
                                    }
                                    }, this,function(response){
                                         WtfGlobal.resetAjaxTimeOut();
                                        baseuomrate =response.data[0].baseuomrate;
                                        rateperuom =response.data[0].rateperuom;
                                        var availableQtyInSelectedUOM =response.data[0].availableQtyInSelectedUOM;
                                        var lockQuantityInSelectedUOM =response.data[0].lockQuantityInSelectedUOM;
                                        var pocountinselecteduom =response.data[0].pocountinselecteduom;
                                        var socountinselecteduom =response.data[0].socountinselecteduom;
                                        var displayUoMid =response.data[0].displayUoMid;
                                        var displayUoMName =response.data[0].displayUoMName;
                                        var displayuomrate =response.data[0].displayuomrate;
                                        datewiseprice =response.data[0].price;
                                        obj.record.set("displayUoMid", displayUoMid);
                                        obj.record.set("displayUoMName", displayUoMName);
                                        obj.record.set("displayuomrate", displayuomrate);
                                        obj.record.set("displayuomvalue", (deliveredproqty * baseuomrate) / displayuomrate);
                                        
                                        if(isblockLooseSell && this.moduleid != Wtf.Acc_Goods_Receipt_ModuleId){// product whose loose selling is blocked, need to validate at here
                                            var returnFlag = this.checkAvailableQtyONSelectedUOM(obj, availableQtyInSelectedUOM, lockQuantityInSelectedUOM, baseuomrate)
                                            if(returnFlag){
                                                this.setGridObjectValues(obj, availableQtyInSelectedUOM, baseuomrate, pocountinselecteduom, socountinselecteduom);
                                            }else{
                                                obj.cancel = true;
                                                return false;
                                            }
                                        }else{
                                            obj.record.set("baseuomrate", baseuomrate);
                                            obj.record.set("availableQtyInSelectedUOM", availableQtyInSelectedUOM);
                                            obj.record.set("isAnotherUOMSelected", true);
                                            obj.record.set("baseuomquantity", deliveredproqty*baseuomrate);
                                            obj.record.set("pocountinselecteduom", pocountinselecteduom);
                                            obj.record.set("socountinselecteduom", socountinselecteduom);
                                        }

                                            var modifiedRate;
                                            modifiedRate=WtfGlobal.getIndividualProductPriceInMultiCurrency(rec,datewiseprice);
                                            /**
                                             * Checking if price is mapped to UOM if yes the setting the unit price which is set in db else converting it as rule set in UOM schema.
                                             * ERM-389 / ERP-35140
                                             */
                                            var isPriceMappedToUOM = false;
                                            if (response.data[0].isPriceMappedToUOM != undefined && response.data[0].isPriceMappedToUOM != "") {
                                                isPriceMappedToUOM = response.data[0].isPriceMappedToUOM;
                                            }
                                        if(productuomid == obj.value){
                                            obj.record.set("baseuomrate", 1);
                                            obj.record.set("baseuomquantity", deliveredproqty*obj.record.get("baseuomrate"));
                                            obj.record.set("rate",modifiedRate*baseuomrate); 
                                            obj.record.set("rateIncludingGst", modifiedRate*baseuomrate);
                                        }else{
                                            obj.record.set("rate", isPriceMappedToUOM ? modifiedRate : modifiedRate * baseuomrate);
                                            obj.record.set("rateIncludingGst", isPriceMappedToUOM ? modifiedRate : modifiedRate * baseuomrate);
                                        }
                                        if(!(this.isAutoFillBatchDetails && this.isCustomer)){
                                            WtfGlobal.setDefaultWarehouseLocation(obj, rec,true);
                                        }
                                        this.fireEvent('datachanged',this);//Fired datachanged event because Sub Total not getting updated in GR Edit case with Multi UOM.
                                    }, function(){

                                    });
//                                  } else {
//                                        obj.record.set("uomname", selectedUOMRec.data['uomname']);
//                                        obj.record.set("baseuomrate", 1);
//                                        obj.record.set("baseuomquantity", deliveredproqty*obj.record.get("baseuomrate"));
//                                  }     
                        } else{ //for packeging UOM type 
                              WtfGlobal.setAjaxTimeOut();
                            Wtf.Ajax.requestEx({
                                    url:"ACCProduct/getIndividualProductPrice.do",
                                    params:{
                                        productid:prorec.data.productid,
                                        affecteduser: this.affecteduser,
                                        transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                                        forCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency:"",
                                        currency: this.parentObj.Currency.getValue(),
                                        quantity: obj.record.data.dquantity,
                                        carryin : (this.isCustomer)? false : true
                                    }
                              }, this,function(response){
                                   WtfGlobal.resetAjaxTimeOut();
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
                                        obj.record.set("baseuomquantity", (deliveredproqty)*(prorec.data.caseuomvalue));
                                        obj.record.set("baseuomrate", (prorec.data.caseuomvalue));
                                        obj.record.set("rate", modifiedRate*prorec.data.caseuomvalue);
                                    } else if(obj.value == prorec.data.inneruom) {
                                        obj.record.set("baseuomquantity", (deliveredproqty)*(prorec.data.inneruomvalue));
                                        obj.record.set("baseuomrate", (prorec.data.inneruomvalue));
                                        obj.record.set("rate", modifiedRate*prorec.data.inneruomvalue);
                                    } else {
                                        obj.record.set("baseuomrate", 1);
                                        obj.record.set("baseuomquantity", deliveredproqty*1);
                                        obj.record.set("rate", modifiedRate);
                                    }
                                    if(!(this.isAutoFillBatchDetails && this.isCustomer)){
                                        WtfGlobal.setDefaultWarehouseLocation(obj, rec,true);
                                    }
                              }, function(){

                              });  
                         } 
                }
//                  this.fireEvent('datachanged',this);
            }
            if(obj.field=="productid" || obj.field=="pid" || obj.field=="productname"){
                rec=obj.record;
                var productid = "";
                var index=this.productComboStore.find('productid',obj.value);
                var customFieldArr = GlobalColumnModelForProduct[this.moduleid];
                if(customFieldArr !=null && customFieldArr != undefined){
                    for(var k=0;k<customFieldArr.length;k++){
                        rec.set(customFieldArr[k].fieldname,"");
                    }
                }
               if(this.isCustomer)
                    rec.set("changedQuantity",(proqty*(-1))*rec.data.baseuomrate);
                else
                    rec.set("changedQuantity",(proqty)*rec.data.baseuomrate);
                
                var prorec = this.productComboStore.getAt(index);
                 if(!this.isGST && Wtf.account.companyAccountPref.isLineLevelTermFlag){

                    if(!Wtf.isEmpty(prorec.data['LineTermdetails'])){                   
                        var termStore = this.getTaxJsonOfIndia(prorec);
                        termStore = this.calculateTermLevelTaxes(termStore, rec);
                        
                        rec.set('LineTermdetails',JSON.stringify(termStore));
                        updateTermDetails(this);
                    }
                } else if (this.isGST) {
                    if (this.isModuleForAvalara) {
                        getTaxFromAvalaraAndUpdateGrid(this, obj);
                    } else {
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
                        processGSTRequest(this.parentObj, this, prorec.data.productid, extraparams);
                    } 
                    else  {
                        processGSTRequest(this.parentObj, this, prorec.data.productid, extraparams);
                    }
                    } 
                } 
                if(index>=0){
                    rec=this.productComboStore.getAt(index);
                     obj.record.set("description","");
                     obj.record.set("isQAEnable",rec.data["isQAEnable"]);
                    obj.record.set("description",(obj.record.data.description=="" || obj.record.data.description== "undefined")? rec.data["desc"]:obj.record.data.description);//ERP-12415 [SJ]: product description auto if desc populate
                    obj.record.set("supplierpartnumber",rec.data["supplierpartnumber"]);
                    obj.record.set("shelfLocation",rec.data["shelfLocation"]);
                   
                    /*--------If tax mapped at Product level then no need to set tax again here As already tax has been set ---------- */
                    if (!(CompanyPreferenceChecks.mapTaxesAtProductLevel() && obj.record.data.prtaxid != undefined && obj.record.data.prtaxid != "")) {
                        obj.record.set("prtaxid", "");
                    }
                    obj.record.set("taxamount","");
                    obj.record.set("baseuomquantity",1);
                    obj.record.set("baseuomrate",1);
                    obj.record.set("availableQtyInSelectedUOM", rec.data['quantity']);
                    obj.record.set("multiuom", rec.data['multiuom']);
                    obj.record.set("uomname", rec.data['uomname']);
                    obj.record.set("baseuomname", rec.data['uomname']);
                    obj.record.set("displayUoMid", rec.data['uomname']);
                    obj.record.set("displayUoMName", rec.data['displayUoMName']);
                    obj.record.set("displayuomrate", rec.data['displayuomrate']);
                    obj.record.set("displayuomvalue", "");
//                    obj.record.set("blockLooseSell", rec.data['blockLooseSell']);
//                    obj.record.set("pocountinselecteduom", rec.data['pocountinselecteduom']);
//                    obj.record.set("socountinselecteduom", rec.data['socountinselecteduom']);
                    obj.record.set("isLocationForProduct", rec.data['isLocationForProduct']);
                    obj.record.set("isWarehouseForProduct", rec.data['isWarehouseForProduct']);
                    obj.record.set("isBatchForProduct", rec.data['isBatchForProduct']);
                    obj.record.set("isSerialForProduct", rec.data['isSerialForProduct']);
                    obj.record.set("isFromVendorConsign", rec.data['isFromVendorConsign']);
                    obj.record.set("isRowForProduct", rec.data['isRowForProduct']);
                    obj.record.set("isRackForProduct", rec.data['isRackForProduct']);
                    obj.record.set("isBinForProduct", rec.data['isBinForProduct']);
                    obj.record.set("location", rec.data["location"]);
                    obj.record.set("warehouse", rec.data["warehouse"]);
                    obj.record.set("uomid", rec.data["uomid"]);
                    obj.record.set("invlocation", rec.data["location"]);
                    obj.record.set("invstore", rec.data["warehouse"]);
                    obj.record.set("type", rec.data["type"]);
                    obj.record.set("batchdetails","");
                    obj.record.set("isAutoAssembly", rec.data["isAutoAssembly"]);
                    if(this.isCustomer)
                        obj.record.set("rate",rec.data["salespricedatewise"]);
                    else
                        obj.record.set("rate",rec.data["purchasepricedatewise"]);
                    }else if(this.productOptimizedFlag!= undefined){                 
                    if(this.productOptimizedFlag==Wtf.Products_on_Submit){
                        productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.value.trim(), 'pid');
                        if(productComboIndex >=0){
                            rec = this.productComboStore.getAt(productComboIndex);
                            productid=rec.data.productid;
                            obj.record.set("productid",productid);
                        }else if(rec.data.productid != ""){
                            productid=rec.data.productid;
                            obj.record.set("productid",productid);
                        }
                    }   
                    obj.record.set("description",(obj.record.data.description=="" || obj.record.data.description== "undefined")? rec.data["desc"]:obj.record.data.description); //ERP-12415[SJ]
                    obj.record.set("supplierpartnumber",rec.data["supplierpartnumber"]);
                    obj.record.set("shelfLocation",rec.data["shelfLocation"]);
                    obj.record.set("prtaxid", "");
                    obj.record.set("taxamount","");
                    obj.record.set("baseuomquantity",1);
                    obj.record.set("baseuomrate",1);
                    obj.record.set("availableQtyInSelectedUOM", rec.data['quantity']);
                    obj.record.set("multiuom", rec.data['multiuom']);
                    obj.record.set("uomname", rec.data['uomname']);
                    obj.record.set("baseuomname", rec.data['uomname']);
//                    obj.record.set("blockLooseSell", rec.data['blockLooseSell']);
//                    obj.record.set("pocountinselecteduom", rec.data['pocountinselecteduom']);
//                    obj.record.set("socountinselecteduom", rec.data['socountinselecteduom']);
                    obj.record.set("isLocationForProduct", rec.data['isLocationForProduct']);
                    obj.record.set("isWarehouseForProduct", rec.data['isWarehouseForProduct']);
                    obj.record.set("isBatchForProduct", rec.data['isBatchForProduct']);
                    obj.record.set("isSerialForProduct", rec.data['isSerialForProduct']);
                    obj.record.set("isFromVendorConsign", rec.data['isFromVendorConsign']);
                    obj.record.set("isRowForProduct", rec.data['isRowForProduct']);
                    obj.record.set("isRackForProduct", rec.data['isRackForProduct']);
                    obj.record.set("isBinForProduct", rec.data['isBinForProduct']);  
                    obj.record.set("location", rec.data["location"]);
                    obj.record.set("warehouse", rec.data["warehouse"]);
                    obj.record.set("uomid", rec.data["uomid"]);
                    obj.record.set("invlocation", rec.data["location"]);
                    obj.record.set("invstore", rec.data["warehouse"]);  
                    obj.record.set("type", rec.data["type"]);
                    obj.record.set("batchdetails","");    //on product selection batchdetails made empty            
                    obj.record.set("isAutoAssembly", rec.data["isAutoAssembly"]);
                    if(this.isCustomer)
                        obj.record.set("rate",rec.data["salespricedatewise"]);
                    else
                        obj.record.set("rate",rec.data["purchasepricedatewise"]);
                    }
                    obj.record.set("isWastageApplicable", rec.data['isWastageApplicable']);
                    if(Wtf.account.companyAccountPref.calculateproductweightmeasurment){
                      obj.record.set("productweightperstockuom", rec.data['productweightperstockuom']);
                      obj.record.set("productweightincludingpakagingperstockuom", rec.data['productweightincludingpakagingperstockuom']);  
                      obj.record.set("productvolumeperstockuom", rec.data['productvolumeperstockuom']);
                      obj.record.set("productvolumeincludingpakagingperstockuom", rec.data['productvolumeincludingpakagingperstockuom']);  
                      
                    } else {
                      obj.record.set("productweightperstockuom", 0);
                      obj.record.set("productweightincludingpakagingperstockuom", 0);
                      obj.record.set("productvolumeperstockuom", 0);
                      obj.record.set("productvolumeincludingpakagingperstockuom", 0);
                    }
                    
               if(this.parentObj && this.parentObj.Currency != undefined){
                    this.forCurrency=this.parentObj.Currency.getValue();
                } 
//                WtfGlobal.setAjaxTimeOut();
                if(obj.individualproductprice!=undefined && obj.individualproductprice!=""){
                    var individualproductprice = obj.individualproductprice;
                    this.setIndividualProductPriceDetails(obj, rec, individualproductprice, rowRateIncludingGstAmountIndex, deliveredproqty, proqty);
                }else{                    
                    Wtf .Ajax.requestEx({
                        url:"ACCProduct/getIndividualProductPrice.do",
                        params:{
                            productid:this.productOptimizedFlag==Wtf.Products_on_Submit?productid:obj.value,
                            affecteduser: this.affecteduser,
                            transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                            forCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency:"",
                            currency: this.parentObj.Currency.getValue(),
                            quantity: obj.record.data.dquantity,
                            carryin : (this.isCustomer)? false : true
                        }
                    }, this,function(response){
//                        WtfGlobal.resetAjaxTimeOut();
                            var individualproductprice = response.data;
                            this.setIndividualProductPriceDetails(obj, rec, individualproductprice, rowRateIncludingGstAmountIndex, deliveredproqty, proqty);
                        this.fireEvent('datachanged',this);
                    }, function(){

                    });           
                 }
                
            if (obj.record.data.dquantity != "" && obj.record.data.type == "Inventory Assembly") {
                this.setBOMValuationArrayToRecord(obj)
            }
            }else if(obj.field=="quantity"){
                rec=obj.record;   
               var isLinkedFromCI=false;
                if(this.parentObj.fromLinkCombo.getValue()==1){
                    isLinkedFromCI=true;
                }
                var islockQuantityflag=this.store.getAt(obj.row).data['islockQuantityflag']; //in linked case whether salesorder is locked or not
                if(islockQuantityflag==''){
                    islockQuantityflag=false;
                }
                var allowExceeding=0;
                if(Wtf.account.companyAccountPref.isAllowQtyMoreThanLinkedDoc && !islockQuantityflag){     // check condition for SO block quantity  as well as company preferences option
                    if(isLinkedFromCI){
                        allowExceeding=0;     //Not Allow to Exceeding   
                    }else{
                        allowExceeding=1;       //Allow to Exceeding 
                    }
                }
                
                  if(((rec.data.isNewRecord=="" || rec.data.isNewRecord==undefined) && this.fromOrder&&!(this.editTransaction||this.copyInv))||(!this.copyTrans && this.isEdit && rec.data.linkid !="")) {  
                   if(allowExceeding==0 && (((obj.value > rec.data.remainingquantity && rec.data.remainingquantity!="")||(obj.value > rec.data.copyquantity && rec.data.copyquantity !="")) ||(obj.value > rec.data.copyquantity && this.isEdit))){  
                    var msg=this.isCustomer?WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinDOisexceedsfromoriginal"):WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinGRisexceedsfromoriginal");
                    obj.record.set(obj.field, obj.originalValue);
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg,this],2);//To display alert with icon.
                   }else if(((obj.value != rec.data.remainingquantity && rec.data.remainingquantity!="")||(obj.value != rec.data.copyquantity && rec.data.copyquantity !="")) ||(obj.value != rec.data.copyquantity && this.isEdit)) {
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"),this.isCustomer?WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinDOisdifferentfromoriginal"):WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinGRisdifferentfromoriginal"),function(btn){
                            if(btn!="yes") {
                                obj.record.set(obj.field, obj.originalValue);
//                                obj.record.set("baseuomquantity",obj.originalValue*obj.record.get("baseuomrate"));
//                                }else{
//                                    obj.record.set("baseuomquantity", obj.value);
                                }
                        },this)
                    }
                }
                if((proqty)==0){
                    this.store.remove(obj.record);
                }
              if(deliveredproqty!=""){
                 if(deliveredproqty > proqty){
                    var msg = this.isCustomer ? WtfGlobal.getLocaleText("acc.field.Deliveredquantityshouldnotbegreaterthanactualquantity") : WtfGlobal.getLocaleText("acc.field.Receiptquantityshouldnotbegreaterthanactualquantity");
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg], 2);
                    obj.record.set("dquantity", proqty);
                    obj.record.set("baseuomquantity", proqty * obj.record.get("baseuomrate"));
                    this.onDeliveredQuantityChange(obj, proqty, proqty);
                    /**
                     * deliveredproqty > proqty
                     * Need to update taxe also, Actual Quantiy and Delivery Quantiy is reset 
                     */
                    if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                        this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                    }
                    if (this.isEdit && rec.data.linkto != "") {
                            this.fromPO = true;
                     }
                    if (((!this.isCustomer && Wtf.account.companyAccountPref.productPricingOnBands) || (this.isCustomer && Wtf.account.companyAccountPref.productPricingOnBandsForSales))&&!this.fromPO) {
                          WtfGlobal.setAjaxTimeOut();
                            Wtf.Ajax.requestEx({
                            url:"ACCProduct/getIndividualProductPrice.do",
                            params: {
                                productid: obj.record.data.productid,
                                affecteduser: this.affecteduser,
                                forCurrency: Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency : "",
                                currency: this.parentObj.Currency.getValue(),
                                quantity: obj.record.data.dquantity,
                                transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                                carryin : (this.isCustomer)? false : true,
                                pricingbandmaster:obj.record.data.pricingbandmasterid
                            }
                        }, this,function(response) {
                             WtfGlobal.resetAjaxTimeOut();
                            var datewiseprice =response.data[0].price;
                            this.isPriceListBand = response.data[0].isPriceListBand;
                            this.isVolumeDisocunt = response.data[0].isVolumeDisocunt;
                            this.priceSource = response.data[0].priceSource;
                            this.pricingbandmasterid=response.data[0].pricingbandmasterid;
                            this.isPriceFromUseDiscount = response.data[0].isPriceFromUseDiscount;
                            this.priceSourceUseDiscount = response.data[0].priceSourceUseDiscount;
                            this.defaultPrice = datewiseprice;
                            this.purchaseprice=response.data[0].purchaseprice;
                            this.isVolumeDisocuntExist = response.data[0].isVolumeDisocuntExist;
                            this.isBandPriceConvertedFromBaseCurrency = response.data[0].isBandPriceConvertedFromBaseCurrency;
                            this.isbandPriceNotAvailable = response.data[0].isbandPriceNotAvailable;
                            
                         /*
                         * set band of customer on product selection
                         */
                            obj.record.set("pricingbandmasterid", this.pricingbandmasterid);
                            if (this.isVolumeDisocunt) {
                              if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) {
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
                            } else if (this.isPriceListBand) {
                                    obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                                    obj.record.set("rate", this.defaultPrice);
                                if (this.isPriceFromUseDiscount) {
                                        if (obj.record.data.dquantity != "") {
                                            if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) {
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
                                        } else {
                                            obj.record.set("rate", "");
                                            obj.record.set("priceSource", "");
                                        }
                                } else {
                                        if (this.isVolumeDisocuntExist) {
                                            if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) {
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
                                    }
                                }
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
                                    if (obj.record.data.dquantity != "") {
                                        obj.record.set("rate", this.defaultPrice);
                                        obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSourceUseDiscount);
                                    } else {
                                        obj.record.set("rate", "");
                                        obj.record.set("priceSource", "");
                                    }
                                } else {
                                    if (this.isVolumeDisocuntExist) {
                                        obj.record.set("rate", this.defaultPrice);
                                        obj.record.set("priceSource", "");
                                    }
                                        if (this.isPriceListBand) {
                                            obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                                        } else {
                                            obj.record.set("priceSource", "");
                                            obj.record.set("rate", this.defaultPrice);
                                        }
                                }
                                // alert if band price not available
                                    if (this.isbandPriceNotAvailable) {
                                        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.DefaultUnitPriceIsnotsetPriceband")], 2);
                                        } else{
                                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.bandPriceNotAvailable.msg")], 2);
                                        }
                                    }
                            }
                            this.fireEvent('datachanged',this);
                        }, function(response) {

                        });
                    }
                    
                    if (obj.record.data.dquantity != "" && obj.record.data.type == "Inventory Assembly") {
                        this.setBOMValuationArrayToRecord(obj)
                    }
                 }
              }
                
                var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
                 if(productComboRecIndex==-1 &&  (obj.record.data ==undefined || obj.record.data.productid == undefined || obj.record.data.productid =="")){ 
                    productComboRecIndex = WtfGlobal.searchRecordIndex(this.store, obj.record.get('productid'), 'productid');
                }
                if (productComboRecIndex >= 0) {
                    var proRecord = this.productComboStore.getAt(productComboRecIndex);
                    if(proRecord==undefined){
                        proRecord=obj.record;
                    }
                    if (proRecord.data.type != 'Service' && proRecord.data.type != 'Non-Inventory Part') { // serial no for only inventory type of product
                        if(proRecord.data.isSerialForProduct) {
                            var v = obj.record.data.quantity;
                            v = String(v);
                            var ps = v.split('.');
                            var sub = ps[1];
                            if (sub!=undefined && sub.length > 0) {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.exceptionMsgForDecimalQty")], 2);
                                obj.record.set("quantity", obj.originalValue);
                                obj.record.set("baseuomquantity", obj.originalValue*obj.record.get("baseuomrate"));
                            }
                        }
                        if(Wtf.account.companyAccountPref.autoPopulateDeliveredQuantity && !proRecord.data.isBatchForProduct && !proRecord.data.isSerialForProduct){
                            if(!(this.isAutoFillBatchDetails && this.isCustomer)){
                                WtfGlobal.setDefaultWarehouseLocation(obj,proRecord,true);
                            }
                    }
                }
               }
            }else if((obj.field=="dquantity" || obj.field=="pricingbandmasterid")){
                rec=obj.record;
                
                var selectedProRec=this.productComboStore.getAt(this.productComboStore.find('productid',obj.record.data.productid));
                
                if(selectedProRec==undefined){
                    selectedProRec=obj.record;
                }
                
                var isblockLooseSell = selectedProRec.get('blockLooseSell');
                if(this.isEdit&&rec.data.linkto!=""){
                    this.fromPO=true;
                }
            /**
              * if there is linking case then populate the unit price from parent document and price will not be recalculate/refresh if Quantity is changed.
              * But if user selects prce band from combo-box the price will be refresh according to price in price band -->as per action item in  SDP-14464.
              */       
        if (((!this.isCustomer && Wtf.account.companyAccountPref.productPricingOnBands) || (this.isCustomer && Wtf.account.companyAccountPref.productPricingOnBandsForSales)) && (obj.field=="pricingbandmasterid" || !islinkingFlag)) {
            WtfGlobal.setAjaxTimeOut();
            Wtf.Ajax.requestEx({
                        url:"ACCProduct/getIndividualProductPrice.do",
                        params: {
                            productid: obj.record.data.productid,
                            affecteduser: this.affecteduser,
                            forCurrency: Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency : "",
                            currency: this.parentObj.Currency.getValue(),
                            quantity: obj.record.data.dquantity,
                            transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                            carryin : (this.isCustomer)? false : true,
                            pricingbandmaster:obj.record.data.pricingbandmasterid,
                            uomid:obj.record.data.uomid
                        }
                    }, this,function(response) {
                         WtfGlobal.resetAjaxTimeOut();
                        var datewiseprice =response.data[0].price ? response.data[0].price : 0;
                        this.isPriceListBand = response.data[0].isPriceListBand;
                        this.isVolumeDisocunt = response.data[0].isVolumeDisocunt;
                        this.priceSource = response.data[0].priceSource ? response.data[0].priceSource : "";
                        this.pricingbandmasterid=response.data[0].pricingbandmasterid ? response.data[0].pricingbandmasterid : "";
                        this.isPriceFromUseDiscount = response.data[0].isPriceFromUseDiscount;
                        this.priceSourceUseDiscount = response.data[0].priceSourceUseDiscount;
                        this.defaultPrice = datewiseprice;
                        this.purchaseprice=response.data[0].purchaseprice;
                        this.isVolumeDisocuntExist = response.data[0].isVolumeDisocuntExist;
                        this.isBandPriceConvertedFromBaseCurrency = response.data[0].isBandPriceConvertedFromBaseCurrency;
                        this.isbandPriceNotAvailable = response.data[0].isbandPriceNotAvailable;
                        this.amendpurchaseprice=response.data[0].amendpurchaseprice;
                        this.isamendpurchasepricenotavail = response.data[0].isamendpurchasepricenotavail;
                        baseuomrate =(response.data[0].baseuomrate!=undefined && response.data[0].baseuomrate!="")?response.data[0].baseuomrate:1;
                        
                        /**
                         * below code creates a json of multiple discounts mapped to specific product and sets in record(discountjson)
                         */
                        if (CompanyPreferenceChecks.discountMaster()) {
                            var jsonArr = createDiscountString(response, obj.record, this.defaultPrice, obj.record.data.dquantity);
                            var jsonObj;
                            if (jsonArr != "" && jsonArr != undefined) {
                                jsonObj = {"data": jsonArr};
                            }
                            var jsonStr = JSON.stringify(jsonObj);
                            obj.record.set("discountjson", jsonStr);
                        } else {
                            obj.record.set("discountjson", "");
                        }
                        /**
                         * Checking if price is mapped to UOM if yes the setting the unit price which is set in db else converting it as rule set in UOM schema.
                         * ERM-389 / ERP-35140
                         */
                        var isPriceMappedToUOM = false,modifiedRate=this.defaultPrice;
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
                        /*
                         * set band of customer on product selection
                         */
                        obj.record.set("pricingbandmasterid", this.pricingbandmasterid);
                        if (this.isVolumeDisocunt) {
                            if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) {
                                obj.record.set("rateIncludingGst", this.defaultPrice);
                                if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                                    this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                                } else {
                                    this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                                }
                            }else {
                                obj.record.set("rate", this.defaultPrice);
                            }
                            obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSource);
                        } else if (this.isPriceListBand) {
                               obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                               /**
                                * Checking if price is mapped to UOM if yes the setting the unit price which is set in db else converting it as rule set in UOM schema.
                                * ERM-389 / ERP-35140
                                */
                            obj.record.set("rate", isPriceMappedToUOM ? this.defaultPrice : modifiedRate * baseuomrate);
                            if (this.isPriceFromUseDiscount) {
                                if (obj.record.data.dquantity != "") {
                                    if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) {
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
                                } else {
                                    obj.record.set("rate", "");
                                    obj.record.set("priceSource", "");
                                }
                            } else {
//                                if (this.isVolumeDisocuntExist) {
                                    if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) {
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
//                                }
                            }
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
                                if (obj.record.data.dquantity != "") {
                                   if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) {
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
                                if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) { // ref Invoice Module
                                    obj.record.set("rateIncludingGst", this.defaultPrice);
                                    if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                                        this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                                    } else {
                                        this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                                    }
                                } else  if (this.isVolumeDisocuntExist) {
                                    obj.record.set("rate", this.defaultPrice);
                                    obj.record.set("priceSource", "");
                                }
                                if (this.isPriceListBand) {
                                    obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                                } else {
                                    obj.record.set("priceSource", "");
                                    obj.record.set("rate", isPriceMappedToUOM ? this.defaultPrice : modifiedRate * baseuomrate);
                                }
                            }
                            // alert if band price not available
                            if (this.isbandPriceNotAvailable) {
                                if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.DefaultUnitPriceIsnotsetPriceband")], 2);
                                } else{
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.bandPriceNotAvailable.msg")], 2);
                                }
                            }
                        }
                        this.fireEvent('datachanged',this);
                    }, function(response) {
                        
                    });
                }
                
                
                    // for "loose sell blocked Product" need to validation of entered value at here as 
                // validation of such product is not being at checkRow method fired on validateedit event
                
                
                
                    if(this.moduleid==Wtf.Acc_Delivery_Order_ModuleId && isblockLooseSell && obj.record.data['productid'].length>0){
                        var originalDquantity = obj.originalValue;
                        var newDquantity = obj.value;
                        var originalBaseuomrate = obj.record.data['baseuomrate'];
                        
                        if(selectedProRec==undefined){
                            selectedProRec=obj.record;
                        }
                        
                        if(selectedProRec.data.type!='Service' && selectedProRec.data.type!='Non-Inventory Part') {
                            // Send Ajax Request for getting available quantity in selected UOM

//                            var quantity = 0;
//
//                            var islockQuantityflag=this.store.getAt(obj.row).data['islockQuantityflag']; //in linked case whether salesorder is locked or not
//                            var soLockQuantityInSelectedUOM=this.store.getAt(obj.row).data['lockQuantityInSelectedUOM'];
//

                            Wtf.Ajax.requestEx({
                                url:"ACCProduct/getProductAvailableQuantiyInSelectedUOM.do",
                                params:{
                                    productId:selectedProRec.data.productid,
                                    currentuomid:obj.record.get('uomid')
                                }
                            }, this,function(response){
                                
                                // Update Row Method Code at here for Product which are barred for Loose Sell
                                
                                var availableQtyInSelectedUOM =response.data[0].availableQtyInSelectedUOM;
                                var lockQuantityInSelectedUOM =response.data[0].lockQuantityInSelectedUOM;
                                
                                var returnFlag = this.checkAvailableQtyONSelectedUOM(obj, availableQtyInSelectedUOM, lockQuantityInSelectedUOM)
                                
                                if(returnFlag){
                                    this.setGridObjectValues(obj, availableQtyInSelectedUOM);
                                }else{
                                    obj.cancel = true;
                                    return false;
                                }

                            }, function(){

                                });

                        }else{
                            this.onDeliveredQuantityChange(obj, deliveredproqty, proqty);
                        }

                    } else {
                        this.onDeliveredQuantityChange(obj, deliveredproqty, proqty);
                    }
                    
                    if (obj.record.data.dquantity != "" && obj.record.data.type == "Inventory Assembly") {
                        this.setBOMValuationArrayToRecord(obj)
                    }
                }
                /**
                 * When user clicks on unit price or rate including GST then
                 * price band combo should be reset and price source should be manual entry.
                 */
                if ((Wtf.account.companyAccountPref.productPricingOnBands || Wtf.account.companyAccountPref.productPricingOnBandsForSales) && (obj.field == "rateIncludingGst" || obj.field == "rate")) {
                    obj.record.set("priceSource", "Manual Entry");
                    obj.record.set("pricingbandmasterid", "");
                }
                /**
                 * Price will does not change for linking cases.
                 * !islinkingFlag is used so that setTaxAndRateAmountAfterIncludingGST will not be call in linkin case while
                 * changing the qty.
                 */
                if(obj.field=="prtaxid" || obj.field=="rate" || (obj.field=="dquantity" && !islinkingFlag) || obj.field=="quantity" || obj.field=="discountispercent" || obj.field=="prdiscount"){
                    var taxamount = this.setTaxAmountAfterSelection(obj.record);
                    obj.record.set("taxamount",taxamount);
                    obj.record.set("isUserModifiedTaxAmount", false);
                    if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) { //As discussed with Vijay J - ERP-28208
                        if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                            this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                        } else {
                            this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                        }
                    }
                    this.fireEvent('datachanged',this);
                }
               if(obj.field=="rateIncludingGst"){
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
                if (obj.field=="rate" && obj.originalValue != obj.value) {
                    if (!this.isRequisition) { // permissions
                        var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore,obj.record.data.productid, 'productid');
                        var productname = "";
                        var prorec = null;
                        if (productComboIndex >= 0) {
                            prorec = this.productComboStore.getAt(productComboIndex);
                            productname = prorec.data.productname;
                        }else{
                             prorec = obj.record;
                             productname = prorec.data.productname;
                        }
                        rec.set("productname",productname);
                        rec.set("price", obj.value);
                        if (this.isCustomer) {
                            rec.set("customer", this.parentObj.Name.getValue());
                        } else {
                            rec.set("vendor", this.parentObj.Name.getValue());
                        }
                       
                    if (this.isCustomer) {
                        if (Wtf.account.companyAccountPref.unitPriceInDO && Wtf.account.companyAccountPref.priceConfigurationAlert) {
                            Wtf.Msg.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.youHaveChangedThePriceForProduct") + " <b>" + productname + "</b>." + ' ' + WtfGlobal.getLocaleText("acc.field.doYouWantToSaveInPriceMaster"),
                                    this.showPriceWindow.createDelegate(this, [rec, obj], true), this);
                        }
                    } else {
                        if (Wtf.account.companyAccountPref.unitPriceInGR && Wtf.account.companyAccountPref.priceConfigurationAlert) {
                            Wtf.Msg.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.youHaveChangedThePriceForProduct") + " <b>" + productname + "</b>." + ' ' + WtfGlobal.getLocaleText("acc.field.doYouWantToSaveInPriceMaster"),
                                    this.showPriceWindow.createDelegate(this, [rec, obj], true), this);
                        }
                    }
                }
                    if (obj.record != undefined && obj.record.data != undefined && obj.record.data.discountjson != undefined && obj.record.data.discountjson != "" && CompanyPreferenceChecks.discountMaster()) {
                       var jsonObj = JSON.parse(obj.record.data.discountjson);
                        calculateDiscount(jsonObj.data, obj.record, obj.record.data.rate, obj.record.data.quantity, true);
                    }
                    
                }
                /**
                 * Below Code checks wether discount value is edited or not if yes then sets the record(discountjson) as empty string
                 * as user entered discount value will should be used rather then mapped discount to product
                 */
                if (obj.field == "prdiscount" && obj.originalValue != obj.value && rec.data.discountjson != "" && rec.data.discountjson != undefined) {
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.discount.editdiscountpromptmsg"), function (btn) {
                        if (btn == "yes") {
                            obj.record.data.qtipdiscountstr = "";
                            obj.record.data.discountjson = "";
                        } else {
                            rec.set('prdiscount', obj.originalValue);
                        }
                        obj.grid.getView().refresh();
                    }, this);
                }
                /**
                 * Below Code checks wether discount type is edited or not if yes then sets the record(discountjson) as empty string
                 * as user entered discount type will should be used rather then mapped discount to product
                 */
                if (obj.field == 'discountispercent' && obj.originalValue != obj.value && rec.data.discountjson != "" && rec.data.discountjson != undefined) {
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.discount.editdiscountpromptmsg"), function (btn) {
                        if (btn == "yes") {
                            obj.record.data.qtipdiscountstr = "";
                            obj.record.data.discountjson = "";
                        } else {
                            rec.set('discountispercent', obj.originalValue);
                        }
                        obj.grid.getView().refresh();
                    }, this);
                }
                //Tax Calculation for India
                // Except Product Combo Edit, For All Edit below code will be used for Multiple Term level Tax calculation.
                if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
                    if ((obj.field=="dquantity") 
                        || (obj.field=="quantity") 
                        || (obj.field=="rate" && obj.originalValue != obj.value)
                        || (obj.field=="rateIncludingGst" && obj.originalValue != obj.value) // If GST Amount changes 
                        || (obj.field=="discountispercent")
                        || (obj.field=="prdiscount")) {
                    
                            if(!this.isGST && rec.get('LineTermdetails') != undefined && rec.get('LineTermdetails') != ''){
                                var termStore = this.getTaxJsonOfIndia(rec);
                                termStore = this.calculateTermLevelTaxes(termStore, rec);

                                rec.set('LineTermdetails',JSON.stringify(termStore));
                                updateTermDetails(this);
                            }else if (this.isGST) {
                                if (this.isModuleForAvalara) {
                                    if (obj.record.data.dquantity) {
                                        getTaxFromAvalaraAndUpdateGrid(this, obj);
                                    }
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
                            processGSTRequest(this.parentObj, this, rec.data.productid);
                        }// If terms are already present then calculate only tax values   
                        else if (rec.data.LineTermdetails != ''&& rec.data.LineTermdetails != undefined) {
                                calculateUpdatedTaxes(this.parentObj, this, rec);
                                updateTermDetails(this);
                            } else {
                            processGSTRequest(this.parentObj, this, rec.data.productid);
                        }
                    }
                }
            }
        }
            if (obj.field == "taxamount") {
                /*
                 * If user changed the tax amount manually then isUserModifiedTaxAmount flag made true for Adaptive Rounding Algorith calculataion.
                 * ERM-1085
                 */
                obj.record.set("isUserModifiedTaxAmount", true);
            }
            if (this.parentObj.includeProTax && this.parentObj.includeProTax.getValue()) {
                WtfGlobal.calculateTaxAmountUsingAdaptiveRoundingAlgo(this, false);
            }
        }
        this.fireEvent('datachanged',this);
        if(this.store.getCount()>0&&this.store.getAt(this.store.getCount()-1).data['productid'].length<=0)
            return;
        if(!this.isNote)
            this.addBlankRow();
        
          /* -------------Set Product tax if Product is mapped with any tax--------------- */

        if (CompanyPreferenceChecks.mapTaxesAtProductLevel()) {//Insert check of taxid of Product               
            this.showMappedProductTax(rec);

        } 
    },
    /**
      * if  Amend fuctionalityuser has activated for that user and purchase should 
      * be greater that sales price,If not then system shows pop up msg.
      */
    getamendprice: function (record,prodObj,includingGstFlag,saveFlag) {
        if (this.isCustomer && Wtf.productPriceEditPerm.priceEditPerm.BlockAmendingPrice) {
            if (record.data.isamendpurchasepricenotavail) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.productList.gridPurchasePrice") + WtfGlobal.getLocaleText("acc.field.isnotset") + " " + WtfGlobal.getLocaleText("acc.field.forproduct") + " " + "<b>" + record.data.pid + "</b>"]);
                record.set("rate", prodObj.originalValue);
             //ERP-40390
             if(includingGstFlag){
                record.set("rate", prodObj.originalValue);
                record.set("rateIncludingGst", prodObj.originalValue);
              }
                return true;
            }
            var amendpurchaseprice = ((this.isPriceListBand || this.isVolumeDisocunt) && this.amendpurchaseprice) ? this.amendpurchaseprice : getRoundofValueWithValues((parseFloat(record.data['amendpurchaseprice']) * parseFloat(record.data['currencyrate'])), Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
            // To check Unit Price from delivery order grid.
            var gridRate = (record.data.rate !== undefined && record.data.rate !== "")  ? record.data.rate : 0 ;
           //ERP-40390
           if(includingGstFlag){
                gridRate = record.data.rateIncludingGst;
            }
            if (amendpurchaseprice > gridRate) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.blockamendingprice")])
                record.set("rate", prodObj.originalValue);      //obj.record.set("rate", prorec.data.saleprice);    
                if(includingGstFlag){
                    record.set("rateIncludingGst", prodObj.originalValue);
                }    
                return true;
            } else {
                record.set("rate", gridRate);
                if(includingGstFlag){
                    record.set("rateIncludingGst", gridRate);
                }
            }
            if(!saveFlag){
                this.fireEvent('datachanged', this);
            }
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

    setIndividualProductPriceDetails: function(obj, rec, individualproductprice, rowRateIncludingGstAmountIndex, deliveredproqty, proqty){
        var datewiseprice =individualproductprice[0].price;
        var producttype = individualproductprice[0].producttype;
        this.isPriceListBand = individualproductprice[0].isPriceListBand;
        this.isVolumeDisocunt = individualproductprice[0].isVolumeDisocunt;
        this.priceSource = individualproductprice[0].priceSource;
        this.pricingbandmasterid=individualproductprice[0].pricingbandmasterid;
        this.isPriceFromUseDiscount = individualproductprice[0].isPriceFromUseDiscount;
        this.priceSourceUseDiscount = individualproductprice[0].priceSourceUseDiscount;
        this.defaultPrice = datewiseprice;
        var discountType = individualproductprice[0].discountType;
        var discountValue = individualproductprice[0].discountValue;
        this.isBandPriceConvertedFromBaseCurrency = individualproductprice[0].isBandPriceConvertedFromBaseCurrency;
        this.isbandPriceNotAvailable = individualproductprice[0].isbandPriceNotAvailable;
        this.isPriceBandIncludingGst = individualproductprice[0].isIncludingGst;
        var amendpurchaseprice = individualproductprice[0].amendpurchaseprice;
        this.isamendpurchasepricenotavail = individualproductprice[0].isamendpurchasepricenotavail;

        /*
        * set band of customer on product selection
        */
        obj.record.set("pricingbandmasterid", this.pricingbandmasterid);
        obj.record.set("amendpurchaseprice", amendpurchaseprice); 
        obj.record.set("isamendpurchasepricenotavail", this.isamendpurchasepricenotavail);
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
            for(var i=1;i<individualproductprice.length;i++){
                var dataObj=individualproductprice[i];
                var key=dataObj.key;
                if(key!=undefined){
                    for (var k = 0; k < obj.grid.colModel.config.length; k++) {
                        if (obj.grid.colModel.config[k].dataIndex == key) {
                            var editor = obj.grid.colModel.config[k].editor;
                            if (editor && editor.field.store) {
                                var store = editor.field.store;
                                if (store)
                                    store.clearFilter();
                            }
                            obj.record.set(key, dataObj[key]);
                        }
                    }     
                }
            }
       }
        var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.value, 'productid');
        if (this.productOptimizedFlag == Wtf.Products_on_Submit) {
            productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.value.trim(), 'pid');
        } else if (productComboIndex == -1) {
            productComboIndex = 1;
        } 
        var productname = ""; 
        var proddescription = "";
        var productuomid = undefined;
        var productsuppliernumber = "";
        var shelfLocation = "";
        var baseuomRate=1;
        var prorec = null;
        var acctaxcode = (this.isCustomer)?"salesacctaxcode":"purchaseacctaxcode";
        var protaxcode = "";
        var productLocation = "";
        var productWarehouse = "";
        var productpid="";
        if(productComboIndex >=0){
            prorec = this.productComboStore.getAt(productComboIndex);
            if(prorec==undefined){
                prorec=obj.record;
            }
            productname = prorec.data.productname;
            proddescription = prorec.data.desc;
            if(this.UomSchemaType==Wtf.UOMSchema){//for Schema type
                productuomid = prorec.data.uomid;
                obj.record.set("baseuomid", prorec.data.uomid);
                obj.record.set("baseuomname", prorec.data.uomname);
                obj.record.set("uomschematypeid", prorec.data.uomschematypeid);
            }else{//for packeging UOM type
                productuomid =this.isCustomer? prorec.data.salesuom:prorec.data.purchaseuom;
                // productuomname =this.isCustomer? prorec.data.salesuomname:prorec.data.purchaseuomname;
                baseuomRate=this.isCustomer? prorec.data.stocksalesuomvalue:prorec.data.stockpurchaseuomvalue;
                if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                    obj.record.set("baseuomquantity", deliveredproqty*(baseuomRate));
                    obj.record.set("baseuomrate", (baseuomRate));
                } else {
                    obj.record.set("baseuomquantity", deliveredproqty);
                    obj.record.set("baseuomrate", 1);
                }
                obj.record.set("stockuom", prorec.data['stockuom']);
                obj.record.set("caseuom", prorec.data['caseuom']);
                obj.record.set("inneruom", prorec.data['inneruom']);
                obj.record.set("caseuomvalue", prorec.data['caseuomvalue']);
                obj.record.set("inneruomvalue", prorec.data['inneruomvalue']);
            }
            productsuppliernumber= prorec.data.supplierpartnumber;
            shelfLocation = prorec.data.shelfLocation;
            protaxcode = prorec.data[acctaxcode];
            productLocation = prorec.data.location;
            productWarehouse = prorec.data.warehouse;
            productpid = prorec.data.pid;
        }
        obj.record.set("desc",proddescription);
        obj.record.set("description",proddescription);
        obj.record.set("uomid", productuomid);
        obj.record.set("supplierpartnumber",productsuppliernumber);
        obj.record.set("shelfLocation",shelfLocation);
        obj.record.set("invlocation",productLocation);
        obj.record.set("purchaseprice", rec.data["purchaseprice"]);  
        obj.record.set("invstore", productWarehouse);
//                    obj.record.set("pid",productpid);
        obj.record.set("productname",productname);
        if (producttype == Wtf.producttype.service && proqty == "" && deliveredproqty == "") {
            obj.record.set("quantity", 1);
            obj.record.set("dquantity", 1);
        }
        if (this.isVolumeDisocunt) {
                if (obj.record.data.dquantity != "") {
                    if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) {
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
                } else {
                    obj.record.set("rate", "");
                    obj.record.set("priceSource", "");
                }
        } else if (this.isPriceListBand) {
            if (this.isPriceFromUseDiscount) {
                if (obj.record.data.dquantity != "") {
                        if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) {
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
                } else {
                    obj.record.set("rate", "");
                    obj.record.set("priceSource", "");
                }
            } else {
                    if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) {
                        obj.record.set("rateIncludingGst", this.defaultPrice);
                        if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                            this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                        } else {
                            this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                        }
                    } else{
                        obj.record.set("rate", this.defaultPrice);
                    }
                    obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                }
                this.checkForPricebandIncludingGst(obj) ; 
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
                } else {
                    obj.record.set("discountjson", "");
                }
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
            if (datewiseprice == 0) {
                    if (!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.addprice) && !this.isRequisition) { // permissions
                        rec.set("productname",productname);
                        //                            if (Wtf.account.companyAccountPref.unitPriceConfiguration) {
                        //                                Wtf.Msg.confirm(WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pricefortheproduct")+"<b>"+productname+"</b>"+' '+WtfGlobal.getLocaleText("acc.field.isnotsetDoyouwanttosetitnow"),
                        //                                    this.showPriceWindow.createDelegate(this,[rec, obj],true), this);
                        //                            }
                        if (this.isCustomer) {
                            if (!Wtf.account.companyAccountPref.unitPriceInDO) { // if Wtf.account.companyAccountPref.unitPriceConfiguration is off and rate for that product is not set then it will be zero by default.
                                obj.record.set("rate", 0);
                            } else {
                                obj.record.set("rate", "");
                            }
                        } else {
                            if (!Wtf.account.companyAccountPref.unitPriceInGR) { // if Wtf.account.companyAccountPref.unitPriceConfiguration is off and rate for that product is not set then it will be zero by default.
                                obj.record.set("rate", 0);
                            } else {
                                obj.record.set("rate", "");
                            }
                        }
                    } else {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pricefortheproduct")+"<b>"+productname+"</b>"+' '+WtfGlobal.getLocaleText("acc.field.isnotset")], 2);
                    }
                } else {
                    // setting datewise price according to currency exchange rate - 
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

                    if (this.isPriceFromUseDiscount) {
                        if (obj.record.data.dquantity != "") {
                            obj.record.set("rate", this.defaultPrice);
                            obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSourceUseDiscount);
                        } else {
                            obj.record.set("rate", "");
                            obj.record.set("priceSource", "");
                        }
                    } else {
                        if(this.UomSchemaType==Wtf.UOMSchema){//Need to Discuss with sir
                            obj.record.set("rate", modifiedRate);
                            obj.record.set("rateIncludingGst", modifiedRate);
                        }else{
                            obj.record.set("rate", modifiedRate*baseuomRate);
                            obj.record.set("rateIncludingGst", modifiedRate*baseuomRate);
                        }
                    }

                }
                // alert if band price not available
                if (this.isbandPriceNotAvailable) {
                    if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.DefaultUnitPriceIsnotsetPriceband")], 2);
                    } else{
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.bandPriceNotAvailable.msg")], 2);
                    }
                }
        }
        /**
         * Recalculate Tax after price set to product in case of NewGST
         */
        if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
            obj.record.set('recTermAmount', 0);
            if (obj.record.get('LineTermdetails') != undefined && obj.record.get('LineTermdetails') != '') {
                var termStore = this.getTaxJsonOfIndia(obj.record);
                if (this.parentObj && this.parentObj.includingGST && this.parentObj.includingGST.getValue() == true) {
                    this.getColumnModel().setRenderer(this.getColumnModel().findColumnIndex("amount"), WtfGlobal.withoutRateCurrencySymbol);
                    termStore = this.calculateTermLevelTaxesInclusive(termStore, obj.record);
                } else {
                    this.getColumnModel().setRenderer(this.getColumnModel().findColumnIndex("amount"), this.calAmountWithoutExchangeRate.createDelegate(this));
                    termStore = this.calculateTermLevelTaxes(termStore, obj.record, undefined, true);
                }

                obj.record.set('LineTermdetails', Wtf.encode(termStore));
                updateTermDetails(this);
            }
        }
        if (this.isModuleForAvalara && this.isGST) {
            getTaxFromAvalaraAndUpdateGrid(this, obj);
        }
    },
    /*
     * Checking if pricelistband is price including GST if yes the showing confirm message to user do you want to apply including GST if tes then setting the price in unit price including gst column.
     * @param {type} obj
     * @returns {undefined}
     */
    checkForPricebandIncludingGst: function (obj) {
        if (this.isPriceBandIncludingGst && this.parentObj.includingGST && !this.parentObj.includingGST.getValue()) {
               /**
                 * Updating rateIncludingGst for All selected product if user clicks on on "YES" for below
                 * alert then show rateIncludingGst wih updated price, if user clicks on "NO" then hide the including gst and
                 * set to empty.
                 */
                var rowRateIncludingGstAmountIndex = this.getColumnModel().findColumnIndex("rateIncludingGst");
                this.getColumnModel().setHidden(rowRateIncludingGstAmountIndex, false);
                obj.record.set("rateIncludingGst", this.defaultPrice);
            if (this.parentObj.fromLinkCombo && this.parentObj.fromLinkCombo.getValue() != 1) {
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.confirm"),
                    msg: WtfGlobal.getLocaleText("acc.field.priceBandIncludingGstAlert"),
                    width: 500,
                    buttons: Wtf.MessageBox.YESNO,
                    scope: this,
                    icon: Wtf.MessageBox.INFO,
                    fn: function (btn) {
                        if (btn == "yes") {
                            this.parentObj.includingGST.setValue(true);
                            this.parentObj.includeProTax.setValue(true);
                            this.parentObj.includeProTax.disable();
                            this.parentObj.isTaxable.setValue(false);
                            this.parentObj.isTaxable.disable();
                            this.parentObj.Tax.setValue("");
                            this.parentObj.Tax.disable();
                            this.getColumnModel().setEditable(this.getColumnModel().findColumnIndex("rate"), false);
                            var rowRateIncludingGstAmountIndex = this.getColumnModel().findColumnIndex("rateIncludingGst");
                            var rowtaxindex = this.getColumnModel().findColumnIndex("prtaxid");
                            var rowtaxamountindex = this.getColumnModel().findColumnIndex("taxamount");
                            this.getColumnModel().setHidden(rowRateIncludingGstAmountIndex, false);
                            this.getColumnModel().setHidden(rowtaxindex, false);
                            this.getColumnModel().setHidden(rowtaxamountindex, false);
                            obj.record.set("rateIncludingGst", this.defaultPrice);
                            if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                                this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                            } else {
                                this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                            }
                            this.parentObj.includingGST.fireEvent('check', this);
                        } else {
                            var rowRateIncludingGstAmountIndex = this.getColumnModel().findColumnIndex("rateIncludingGst");
                            this.getColumnModel().setHidden(rowRateIncludingGstAmountIndex, true);
                            return;
                        }
                    }
                }, this);
            }
        }
    },
    setBOMValuationArrayToRecord: function(obj) {
        Wtf.Ajax.requestEx({
            url: "ACCReports/getPriceCalculationForAsseblyProduct.do",
            params: {
                productid: obj.record.data.productid,
                buildquantity: obj.record.data.dquantity
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
    
    checkAvailableQtyONSelectedUOM:function(obj, availableQtyInSelectedUOM, lockQuantityInSelectedUOM, selectedProductBaseUOMRate){
        
        var returnFlag = true;
        
        var rec=obj.record;
        
        var selectedProRec=this.productComboStore.getAt(this.productComboStore.find('productid',obj.record.data.productid));
        
        if(selectedProRec==undefined){
            selectedProRec=obj.record;
        }
        
        var availableQtyInSelectedUOMOriginal = obj.record.data['availableQtyInSelectedUOM'];
        var venconsignuomquantity = selectedProRec.data.venconsignuomquantity;
        
        var originalUOMName = obj.record.data['uomname'];
        
        
//        var isAnotherUOMSelected = false;
        
        if(obj.field=="dquantity") {
            var originalDquantity = obj.originalValue;
            var newDquantity = obj.value;
            var originalBaseuomrate = obj.record.data['baseuomrate'];
            var originalUOMId = obj.record.data['uomid'];
            var newBaseuomrate = obj.record.data['baseuomrate'];
            
//            if(selectedProRec.get("uomid") != obj.record.data['uomid']){
//                isAnotherUOMSelected = true;
//            }
            
        } else if(obj.field=="uomid") {
            var originalDquantity = obj.record.data['dquantity'];
            var newDquantity = obj.record.data['dquantity'];
            var originalBaseuomrate = obj.record.data['baseuomrate'];
            var newBaseuomrate = obj.record.data['baseuomrate'];
            
            var originalUOMId = obj.originalValue;
            
//            if(selectedProRec.get("uomid") != obj.value){
//                isAnotherUOMSelected = true;
//            }
            
            var newUOMId = obj.value;
            
            
        }
        
        var quantity = 0;

        var islockQuantityflag=this.store.getAt(obj.row).data['islockQuantityflag']; //in linked case whether salesorder is locked or not
        var soLockQuantityInSelectedUOM=this.store.getAt(obj.row).data['lockQuantityInSelectedUOM'];
        
        //        var availableQtyInSelectedUOM =response.data[0].availableQtyInSelectedUOM;
        //        var lockQuantityInSelectedUOM =response.data[0].lockQuantityInSelectedUOM;
//        obj.record.set("availableQtyInSelectedUOM", availableQtyInSelectedUOM);
        //                                obj.record.set("isAnotherUOMSelected", true);
                                
        if(this.editTransaction){  //In Edit Case Check product quantity is greater than available quantity when selecting quantity                  
                                    
            var copyquantity = 0;                    
            this.store.each(function(rec){
                if(rec.data.productid == selectedProRec.data.productid){
                    copyquantity = copyquantity + rec.data.copyquantity;
                    var ind=this.store.indexOf(rec);
                    if(ind!=-1){
                        if(ind!=obj.row){
                            quantity = quantity + rec.data.dquantity;
                        }
                    }
                }
            },this);
            quantity = quantity + newDquantity;
            if(islockQuantityflag && selectedProRec.data.isAutoAssembly != true){   //if DO is lonked with SO then we will consider quantity (available quantity-lock quantity excluding its own quantity

                if((availableQtyInSelectedUOM-(lockQuantityInSelectedUOM-soLockQuantityInSelectedUOM)) < quantity) {
                    if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+selectedProRec.data.productname+' '+WtfGlobal.getLocaleText("acc.field.is")+(availableQtyInSelectedUOM-(lockQuantityInSelectedUOM-soLockQuantityInSelectedUOM))+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);                      
                        rec.set("quantity",originalDquantity);
                        rec.set("dquantity",originalDquantity);
                        rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                        rec.set("baseuomrate",originalBaseuomrate);
                        rec.set("uomid",originalUOMId);
//                        
//                        var isAnotherUOMSelected = false;
//
//                        if(selectedProRec.get("uomid") != originalUOMId){
//                            isAnotherUOMSelected = true;
//                        }
//
//                        rec.set("availableQtyInSelectedUOM", availableQtyInSelectedUOMOriginal);
//                        rec.set("isAnotherUOMSelected", isAnotherUOMSelected);
//                        rec.set("uomname", originalUOMName);
                        
                        obj.cancel=true;
                        returnFlag = false;
                    }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                            if(btn=="yes"){
                                obj.cancel=false;
                            }else{
                                rec.set("quantity",originalDquantity);
                                rec.set("dquantity",originalDquantity);
                                rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                                rec.set("baseuomrate",originalBaseuomrate);
                                rec.set("uomid",originalUOMId);
                                
                                var isAnotherUOMSelected = false;

                                if(selectedProRec.get("uomid") != originalUOMId){
                                    isAnotherUOMSelected = true;
                                }
                                
                                rec.set("availableQtyInSelectedUOM", availableQtyInSelectedUOMOriginal);
                                rec.set("isAnotherUOMSelected", isAnotherUOMSelected);
                                rec.set("uomname", originalUOMName);
                                
                                obj.cancel=true;
                                returnFlag = false;
                                return false;
                            }
                        },this); //for Ignore Case no any Restriction on user 
                    }
                }

            } else if((availableQtyInSelectedUOM-lockQuantityInSelectedUOM) < quantity && selectedProRec.data.isAutoAssembly != true ) {  //for normal check for all products available quantity
                availableQtyInSelectedUOM = availableQtyInSelectedUOM + copyquantity;
                if((availableQtyInSelectedUOM-lockQuantityInSelectedUOM) < quantity) {

                    if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+' '+WtfGlobal.getLocaleText("acc.nee.54")+' '+selectedProRec.data.productname+WtfGlobal.getLocaleText("acc.field.is")+(availableQtyInSelectedUOM-lockQuantityInSelectedUOM)+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                        rec.set("quantity",originalDquantity);
                        rec.set("dquantity",originalDquantity);
                        rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                        rec.set("baseuomrate",originalBaseuomrate);
                        rec.set("uomid",originalUOMId);
                        
//                        var isAnotherUOMSelected = false;
//
//                        if(selectedProRec.get("uomid") != originalUOMId){
//                            isAnotherUOMSelected = true;
//                        }
//
//                        rec.set("availableQtyInSelectedUOM", availableQtyInSelectedUOMOriginal);
//                        rec.set("isAnotherUOMSelected", isAnotherUOMSelected);
//                        rec.set("uomname", originalUOMName);
                        
                        obj.cancel=true;   
                        returnFlag = false;
                    }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                            if(btn=="yes"){
                                obj.cancel=false;
                            }else{
                                rec.set("quantity",originalDquantity);
                                rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                                rec.set("baseuomrate",originalBaseuomrate);
                                rec.set("dquantity",originalDquantity);
                                rec.set("uomid",originalUOMId);
                                
                                var isAnotherUOMSelected = false;
                                
                                if(selectedProRec.get("uomid") != originalUOMId){
                                    isAnotherUOMSelected = true;
                                }
                                
                                rec.set("availableQtyInSelectedUOM", availableQtyInSelectedUOMOriginal);
                                rec.set("isAnotherUOMSelected", isAnotherUOMSelected);
                                rec.set("uomname", originalUOMName);
                                
                                obj.cancel=true;
                                returnFlag = false;
                                return false;
                            }
                        },this); //for Ignore Case no any Restriction on user 
                    }
                }
            }

        }else{//In normal Case Check product quantity is greater than available quantity when selecting quantity
            this.store.each(function(rec){
                if(rec.data.productid == selectedProRec.data.productid){// itereating details grid store for getting entered quantity if product is already selected in grid
                    var ind=this.store.indexOf(rec);
                    if(ind!=-1){
                        if(ind!=obj.row){
                            quantity = quantity + rec.data.dquantity;
                        }
                    }                               
                }
            },this);
            quantity = quantity + newDquantity;

            if(islockQuantityflag){   //if DO is lonked with SO then we will consider quantity (available quantity-lock quantity excluding its own quantity
                if((availableQtyInSelectedUOM-(lockQuantityInSelectedUOM-soLockQuantityInSelectedUOM)) < quantity && selectedProRec.data.isAutoAssembly != true) {
                    if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+selectedProRec.data.productname+' '+WtfGlobal.getLocaleText("acc.field.is")+(availableQtyInSelectedUOM-(lockQuantityInSelectedUOM-soLockQuantityInSelectedUOM))+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);                      
                        rec.set("quantity",originalDquantity);
                        rec.set("dquantity",originalDquantity);
                        rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                        rec.set("baseuomrate",originalBaseuomrate);
                        rec.set("uomid",originalUOMId);
//                        
//                        var isAnotherUOMSelected = false;
//
//                        if(selectedProRec.get("uomid") != originalUOMId){
//                            isAnotherUOMSelected = true;
//                        }
//                            
//                        rec.set("availableQtyInSelectedUOM", availableQtyInSelectedUOMOriginal);
//                        rec.set("isAnotherUOMSelected", isAnotherUOMSelected);
//                        rec.set("uomname", originalUOMName);
                        
                        obj.cancel=true;   
                        returnFlag = false;
                    }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                            if(btn=="yes"){
                                obj.cancel=false;
                            }else{
                                rec.set("quantity",originalDquantity);
                                rec.set("dquantity",originalDquantity);
                                rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                                rec.set("baseuomrate",originalBaseuomrate);
                                rec.set("uomid",originalUOMId);
                                
                                var isAnotherUOMSelected = false;
                                
                                if(selectedProRec.get("uomid") != originalUOMId){
                                    isAnotherUOMSelected = true;
                                }
                                
                                rec.set("availableQtyInSelectedUOM", availableQtyInSelectedUOMOriginal);
                                rec.set("isAnotherUOMSelected", isAnotherUOMSelected);
                                rec.set("uomname", originalUOMName);
                                
                                obj.cancel=true;
                                returnFlag = false;
                                return false;
                            }
                        },this); //for Ignore Case no any Restriction on user 
                    }
                }

            }else if((availableQtyInSelectedUOM-lockQuantityInSelectedUOM) < quantity && selectedProRec.data.isAutoAssembly != true) {//for normal check for all products available quantity
                if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+' '+WtfGlobal.getLocaleText("acc.nee.54")+' '+selectedProRec.data.productname+' '+WtfGlobal.getLocaleText("acc.field.is")+(availableQtyInSelectedUOM-lockQuantityInSelectedUOM)+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);                      
                    rec.set("quantity",originalDquantity);
                    rec.set("dquantity",originalDquantity);
                    rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                    rec.set("baseuomrate",originalBaseuomrate);
                    rec.set("uomid",originalUOMId);
                    
//                    var isAnotherUOMSelected = false;
//                    
//                    if(selectedProRec.get("uomid") != originalUOMId){
//                        isAnotherUOMSelected = true;
//                    }
//
//                    rec.set("availableQtyInSelectedUOM", availableQtyInSelectedUOMOriginal);
//                    rec.set("isAnotherUOMSelected", isAnotherUOMSelected);
//                    rec.set("uomname", originalUOMName);
//                    
                    obj.cancel=true;   
                    returnFlag = false;
                }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                        if(btn=="yes"){
                            obj.cancel=false;
                        }else{
                            rec.set("quantity",originalDquantity);
                            rec.set("dquantity",originalDquantity);
                            rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                            rec.set("baseuomrate",originalBaseuomrate);
                            rec.set("uomid",originalUOMId);
                            
                            var isAnotherUOMSelected = false;

                            if(selectedProRec.get("uomid") != originalUOMId){
                                isAnotherUOMSelected = true;
                            }
                            
                            rec.set("availableQtyInSelectedUOM", availableQtyInSelectedUOMOriginal);
                            rec.set("isAnotherUOMSelected", isAnotherUOMSelected);
                            rec.set("uomname", originalUOMName);
                            
                            obj.cancel=true;
                            returnFlag = false;
                            return false;
                        }
                    },this); //for Ignore Case no any Restriction on user 
                }
            }
        }
        return returnFlag;
    },
    setTaxAndRateAmountAfterIncludingGST : function (record) {
        if(record.data.prtaxid!="None"){
        var taxamount = this.setTaxAmountAfterIncludingGst(record,1);
        var amountwithOutGst = this.setTaxAmountAfterIncludingGst(record,2);
        record.set("taxamount",taxamount);
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
            updateTermDetails(this);
            // updated subtotla xtemplate
            this.parentObj.updateSubtotalOnTermChange(true);
        }
    },
    setTaxAmountAfterIncludingGst:function(rec,amountFlag) {//amountFlag=1 for taxamount and amountFlag=2 for actual amount with discount actualamount=3 amount with GST
        
        var quantity = rec.data.dquantity;
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
        var discount = 0;
        var rateIncludingGst=getRoundofValueWithValues(rec.data.rateIncludingGst,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        quantity=getRoundofValue(rec.data.dquantity);
        var lineTermAmount = (rec.data.lineleveltermamount == undefined || rec.data.lineleveltermamount == null || rec.data.lineleveltermamount == '')?0:rec.data.lineleveltermamount;
        var quantityAndAmount=0;
        quantityAndAmount=rateIncludingGst*quantity;
        var origionalAmount = getRoundedAmountValue(quantityAndAmount) ;
        
        if(rec.data.prdiscount > 0) {
            var prdiscount=getRoundedAmountValue(rec.data.prdiscount);
            if(rec.data.discountispercent == 1){
                discount = getRoundedAmountValue(origionalAmount * prdiscount/ 100);
            } else {
                discount = prdiscount;
            }
        }

        var val=origionalAmount-discount+lineTermAmount;
        var taxpercent=0;
        var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
        if(index>=0){
            var taxrec=this.taxStore.getAt(index);
            taxpercent=getRoundedAmountValue(taxrec.data.percent);
        }
        var amount=0.0;
        var taxamount=getRoundedAmountValue(val*taxpercent/(taxpercent+100));

        var unitAmount= 0;
        var unitTax= 0;
        var unitVal= 0;
        if(quantity!=0){
             amount=getRoundedAmountValue((val-taxamount));
             unitVal=getRoundedAmountValue(val/quantity);
             unitAmount=getRoundedAmountValue(amount/quantity);
             unitTax= getRoundedAmountValue(taxamount/quantity);
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
    },
    setGridObjectValues:function(obj, availableQtyInSelectedUOM, selectedProductBaseUOMRate, pocountinselecteduom, socountinselecteduom){
        
        var proqty = obj.record.get("quantity");
        proqty = (proqty == "NaN" || proqty == undefined || proqty == null)?0:proqty;

        var deliveredproqty = obj.record.get("dquantity");
        deliveredproqty = (deliveredproqty == "NaN" || deliveredproqty == undefined || deliveredproqty == null)?0:deliveredproqty;
        
        if(obj.field=="dquantity"){
            this.onDeliveredQuantityChange(obj, deliveredproqty, proqty);
        }else if(obj.field=="uomid"){
            var selectedUOMRec = WtfGlobal.searchRecord(Wtf.uomStore, obj.value, 'uomid');
            obj.record.set("uomname", selectedUOMRec.data['uomname']);
            obj.record.set("baseuomrate", selectedProductBaseUOMRate);
            obj.record.set("availableQtyInSelectedUOM", availableQtyInSelectedUOM);
            obj.record.set("isAnotherUOMSelected", true);
            obj.record.set("baseuomquantity", deliveredproqty*selectedProductBaseUOMRate);
            obj.record.set("pocountinselecteduom", pocountinselecteduom);
            obj.record.set("socountinselecteduom", socountinselecteduom);
        }
    },
    
    onDeliveredQuantityChange:function(obj, deliveredproqty, proqty){
        var rec=obj.record;
        /**
        *this method check the allow zero quantity functionality is activated
        * or nor in system preferences
        **/
        this.allowZeroQuantity = WtfGlobal.checkAllowZeroQuantityForProduct(this.moduleid);
        if(this.isCustomer)
            rec.set("changedQuantity",(rec.data.copyquantity-obj.value)*((rec.data.copybaseuomrate==="")?1:rec.data.copybaseuomrate));
        else
            rec.set("changedQuantity",(obj.value-rec.data.copyquantity)*((rec.data.copybaseuomrate==="")?1:rec.data.copybaseuomrate));                
        if(deliveredproqty > proqty){// If delivered qty is gretaor than Actual qty
            var msg = this.isCustomer ? WtfGlobal.getLocaleText("acc.field.Deliveredquantityshouldnotbegreaterthanactualquantity") : WtfGlobal.getLocaleText("acc.field.Receiptquantityshouldnotbegreaterthanactualquantity");
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg], 2);
            obj.record.set("dquantity", proqty);
            obj.record.set("baseuomquantity", proqty*obj.record.get("baseuomrate"));
            var displayuomid = obj.record.get("displayUoMid");
            var displayuomtest = obj.record.get("displayuomrate");
            if (displayuomtest != "NaN" && displayuomtest != undefined && displayuomtest != "" && displayuomtest != null && displayuomid != undefined && displayuomid != "" && displayuomid != null)
                obj.record.set("displayuomvalue", (proqty * obj.record.get("baseuomrate")) / obj.record.get("displayuomrate"));
            else
                obj.record.set("displayuomvalue", "")
            obj.cancel=true;
            return;
        } else if(deliveredproqty<= 0 && !this.allowZeroQuantity){
            var msg = this.isCustomer ? WtfGlobal.getLocaleText("acc.field.Deliveredquantityshouldnotbeequalorlessthanzero") : WtfGlobal.getLocaleText("acc.field.Receiptquantityshouldnotbeequalorlessthanzero");
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg], 2);
            obj.record.set("dquantity", proqty);
            obj.record.set("baseuomquantity", proqty*obj.record.get("baseuomrate"));
            var displayuomid = obj.record.get("displayUoMid");
            var displayuomtest = obj.record.get("displayuomrate");
            if (displayuomtest != "NaN" && displayuomtest != undefined && displayuomtest != "" && displayuomtest != null && displayuomid != undefined && displayuomid != "" && displayuomid != null)
                obj.record.set("displayuomvalue", (proqty * obj.record.get("baseuomrate")) / obj.record.get("displayuomrate"));
            else
                obj.record.set("displayuomvalue", "")
            obj.cancel=true;
            return;
        }
        
        /**
         * ERP-36531
         * Discount should be calculated in linking case.
         */
        if (obj.record.data.discountjson != undefined && obj.record.data.discountjson != '') {
            var jsonObj = JSON.parse(obj.record.data.discountjson);
            calculateDiscount(jsonObj.data, rec, obj.record.data.rate, obj.record.data.dquantity, true);
        }
        var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
        if(productComboRecIndex==-1 &&  (obj.record.data ==undefined || obj.record.data.productid == undefined || obj.record.data.productid =="")){ 
            productComboRecIndex = WtfGlobal.searchRecordIndex(this.store, obj.record.get('productid'), 'productid');
        }
        if (productComboRecIndex >= 0) {
            var proRecord = this.productComboStore.getAt(productComboRecIndex);
            if(proRecord==undefined){
                proRecord=obj.record;
            }
            
            var productuomid = proRecord.data.uomid;
            if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                obj.record.set("baseuomquantity", deliveredproqty*obj.record.get("baseuomrate"));
                var displayuomid = obj.record.get("displayUoMid");
                var displayuomtest = obj.record.get("displayuomrate");
                if (displayuomtest != "NaN" && displayuomtest != undefined && displayuomtest != "" && displayuomtest != null && displayuomid != undefined && displayuomid != "" && displayuomid != null)
                    obj.record.set("displayuomvalue", (deliveredproqty * obj.record.get("baseuomrate")) / obj.record.get("displayuomrate"));
                else
                    obj.record.set("displayuomvalue", "")
            } else {
                obj.record.set("baseuomrate", 1);
                obj.record.set("baseuomquantity", deliveredproqty*obj.record.get("baseuomrate"));
                var displayuomid = obj.record.get("displayUoMid");
                var displayuomtest = obj.record.get("displayuomrate");
                if (displayuomtest != "NaN" && displayuomtest != undefined && displayuomtest != "" && displayuomtest != null && displayuomid != undefined && displayuomid != "" && displayuomid != null)
                    obj.record.set("displayuomvalue", (deliveredproqty * obj.record.get("baseuomrate")) / obj.record.get("displayuomrate"));
                else
                    obj.record.set("displayuomvalue", "")
            }
            
            
            if (proRecord.data.type != 'Service' && proRecord.data.type != 'Non-Inventory Part') { // serial no for only inventory type of product
                if(proRecord.data.isSerialForProduct) {
                    var v = obj.record.data.dquantity;
                    v = String(v);
                    var ps = v.split('.');
                    var sub = ps[1];
                    if (sub!=undefined && sub.length > 0) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.exceptionMsgForDecimalQty")], 2);
                        obj.record.set("dquantity", obj.originalValue);
                        obj.cancel=true;
                        return;
                    }
              //check only for location and warehouse if default location and warehouse is not present then show prompt message      
//            }else if(((proRecord.data.isLocationForProduct && (proRecord.data.location==undefined || proRecord.data.location=="")) || (proRecord.data.isWarehouseForProduct && (proRecord.data.warehouse==undefined || proRecord.data.warehouse==""))) && !proRecord.data.isSerialForProduct && !proRecord.data.isBatchForProduct){
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.exceptionMsgForlocware")], 2);
//                        obj.cancel=true;
//                        return; ERP-16602
            
            }else if(!this.isEdit || this.copyTrans){
                if(!(this.isAutoFillBatchDetails && this.isCustomer)){
                    WtfGlobal.setDefaultWarehouseLocation(obj,proRecord,true);   
                }
            }
            }
        } else if(this.productOptimizedFlag!= undefined ){
            var proRecord = obj.record;
            var productbaseuomid = proRecord.data.baseuomid;
            if(obj.record.get("uomid")!=undefined && productbaseuomid != obj.record.get("uomid")){
                obj.record.set("baseuomquantity", deliveredproqty*obj.record.get("baseuomrate"));
                var displayuomid = obj.record.get("displayUoMid");
                var displayuomtest = obj.record.get("displayuomrate");
                if (displayuomtest != "NaN" && displayuomtest != undefined && displayuomtest != "" && displayuomtest != null && displayuomid != undefined && displayuomid != "" && displayuomid != null)
                    obj.record.set("displayuomvalue", (deliveredproqty * obj.record.get("baseuomrate")) / obj.record.get("displayuomrate"));
                else
                    obj.record.set("displayuomvalue", "")
            } else {
                obj.record.set("baseuomrate", 1);
                obj.record.set("baseuomquantity", deliveredproqty*obj.record.get("baseuomrate"));                
                var displayuomid = obj.record.get("displayUoMid");
                var displayuomtest = obj.record.get("displayuomrate");
                if (displayuomtest != "NaN" && displayuomtest != undefined && displayuomtest != "" && displayuomtest != null && displayuomid != undefined && displayuomid != "" && displayuomid != null)
                    obj.record.set("displayuomvalue", (deliveredproqty * obj.record.get("baseuomrate")) / obj.record.get("displayuomrate"));
                else
                    obj.record.set("displayuomvalue", "")              
            }
            
            if (proRecord.data.type != 'Service' && proRecord.data.type != 'Non-Inventory Part') { // serial no for only inventory type of product
                if(proRecord.data.isSerialForProduct) {
                    var v = obj.record.data.dquantity;
                    v = String(v);
                    var ps = v.split('.');
                    var sub = ps[1];
                    if (sub!=undefined && sub.length > 0) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.exceptionMsgForDecimalQty")], 2);
                        obj.record.set("dquantity", obj.originalValue);
                        obj.cancel=true;
                        return;
                    }
                }
//                else if(!this.isEdit || this.copyTrans ){
//                    if(!(this.isAutoFillBatchDetails && this.isCustomer)){
//                        WtfGlobal.setDefaultWarehouseLocation(obj,proRecord,true);   
//                    }
//                }
            }

        }   
    },
    
    
    setTaxAmountAfterSelection:function(rec) {
        
        var quantity = rec.data.dquantity;
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
//        var rec=obj.record;
        var discount = 0;
        var rate=getRoundofValueWithValues(rec.data.rate,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        quantity=getRoundofValue(rec.data.dquantity);
        var origionalAmount = getRoundedAmountValue(rate*quantity) ;
//        if(rec.data.partamount != 0){
//            var partamount=getRoundedAmountValue(rec.data.partamount);
//            origionalAmount = getRoundedAmountValue(origionalAmount * (partamount/100));
//        }
        
//        origionalAmount = this.calAmountWithExchangeRate(origionalAmount, rec);
        if(rec.data.prdiscount > 0) {
            var prdiscount=getRoundedAmountValue(rec.data.prdiscount);
            if(rec.data.discountispercent == 1){
                discount = getRoundedAmountValue(origionalAmount * prdiscount/ 100);
            } else {
                discount = prdiscount;
            }
        }
//        var discount=rec.data.rate*rec.data.quantity*rec.data.prdiscount/100
        var val=origionalAmount-discount;
        var taxpercent=0;
        var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
        if(index>=0){
            var taxrec=this.taxStore.getAt(index);
            taxpercent=getRoundedAmountValue(taxrec.data.percent);
        }
        var taxamount= getRoundedAmountValue(val*taxpercent/100);
        return taxamount;
        
    },
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
                    return WtfGlobal.withCurrencyUnitPriceRenderer(v, m, rec);
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
             * In copy case, deactivated tax not shown.Hence, empty taxid set in record.          
             */
            if (rec.data.prtaxid != "" && (this.copyInv || this.fromPO)) {
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
    calAmountWithoutExchangeRate:function(v,m,rec){
        
        
        var rate=getRoundofValueWithValues(rec.data.rate,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        
         /*
         * Check if rateincludegst is available or not
         */
         if(this.parentObj.includingGST && this.parentObj.includingGST.getValue()) {
            rate = rec.data.rateIncludingGst;
          }
        
        var quantity=getRoundofValue(rec.data.dquantity);
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;


        var origionalAmount = getRoundedAmountValue(rate*quantity) ;
//        if(rec.data.partamount != 0){
//            origionalAmount = origionalAmount * (rec.data.partamount/100);
//        }

        /* When DO is created by linking with partial Invoice*/
        if (rec.json != undefined && rec.json.partamount != undefined && rec.json.partamount != 0) {

            /*Calculating Partial amount of Invoice that should be load in DO at line level*/

            origionalAmount = getRoundedAmountValue(origionalAmount * (rec.json.partamount / 100));

        }

        var discount = 0;//origionalAmount*rec.data.prdiscount/100   
        if(rec.data.prdiscount > 0) {
            var prdiscount=getRoundedAmountValue(rec.data.prdiscount);
            if(rec.data.discountispercent == 1){
                discount = getRoundedAmountValue((origionalAmount * prdiscount) / 100);
            } else {
                discount = prdiscount;
            }
        }
        
        var val=(origionalAmount)-discount;///rec.data.oldcurrencyrate  
//        rec.set("amountwithouttax",val);
        var taxamount = 0;
        if(rec.data.taxamount){
            taxamount= getRoundedAmountValue(rec.data.taxamount);
        }
        if (this.parentObj != undefined && this.parentObj.includingGST && this.parentObj.includingGST.getValue()) {
            rec.set("amountWithoutTax", (parseFloat(getRoundedAmountValue(val - taxamount)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1));
        } else {
            rec.set("amountWithoutTax", (parseFloat(getRoundedAmountValue(val)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1));
        }
       /*
        *Check If the rete Including GST THEN no need to add tax amount ElSE If not then add the tax amount to amount
        */
       if(this.parentObj.includingGST && !this.parentObj.includingGST.getValue()) {
            val=parseFloat(val)+parseFloat(taxamount);
        }
        rec.set("amount",(parseFloat(getRoundedAmountValue(val)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1));
//        if(this.isQuotationFromPR && val!==0 && (rec.data.orignalamount==undefined || rec.data.orignalamount==""))
//        rec.set("orignalamount",val);

        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
            return Wtf.UpriceAndAmountDisplayValue;
        } else{
            return WtfGlobal.withoutRateCurrencySymbol(val,m,rec);
        }
    },
    calAmount:function(v,m,rec){
        var quantity = rec.data.dquantity;
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
        
        var origionalAmount = rec.data.rate * quantity;
        rec.set("amount",origionalAmount);
        return WtfGlobal.withoutRateCurrencySymbol(origionalAmount,m,rec);
    },
    
    calSubtotal:function(){
        var subtotal=0;
        var total=0;
        var count=this.store.getCount();
        for(var i=0;i<count;i++){
            total=(parseFloat(this.store.getAt(i).data['amount'])-parseFloat(this.store.getAt(i).data['taxamount']));
            subtotal+=getRoundedAmountValue(total);
        }
        return getRoundedAmountValue(subtotal);
    },
    
    calTaxtotal:function(){
        var subtotal=0;
        var total=0;
        var count=this.store.getCount();
        for(var i=0;i<count;i++){
            total=parseFloat(this.store.getAt(i).data['taxamount']);
            subtotal+=getRoundedAmountValue(total);
        }
        return getRoundedAmountValue(subtotal);
    },
    calLineLevelTaxNew:function(){
        var subtotal=0;
        var taxpercent=0;
        var amount=0;
        var quantity=0;
        var taxAndSubtotal=[];
        var count=this.store.getCount();
        for(var i=0;i<count;i++){
            if (this.store.getAt(i).data['rowTaxPercent']) {
                taxpercent=parseFloat(this.store.getAt(i).data['rowTaxPercent']);
            } else {
                taxpercent=0;
            }
            if (this.store.getAt(i).data['amountWithoutTax']) {
                amount=parseFloat(this.store.getAt(i).data['amountWithoutTax']);
            } else {
                amount=0;
            }
            subtotal+=(getRoundedAmountValue(amount)*taxpercent)/100;
        }
        return getRoundedAmountValue(subtotal);
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
    
    hideShowCustomizeLineFields:function(){ 
        if(this.moduleid==Wtf.Acc_Delivery_Order_ModuleId || this.moduleid==Wtf.Acc_Goods_Receipt_ModuleId ){
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
                              if(cm.config[j].header==action.data[i].fieldDataIndex||(cm.config[j].dataIndex==action.data[i].fieldDataIndex && cm.config[j].header==action.data[i].fieldname)){
                                  cm.setHidden(j,action.data[i].hidecol);       
                                  cm.setEditable(j,!action.data[i].isreadonlycol);
                                  if( action.data[i].fieldlabeltext!=null && action.data[i].fieldlabeltext!=undefined && action.data[i].fieldlabeltext!=""){
                                    cm.setColumnHeader(j,action.data[i].fieldlabeltext);
                                  }
                             }
                        }
                    }
                    if (this.isCustomer) {
                        if (!Wtf.account.companyAccountPref.unitPriceInDO) {
                            cm.setHidden(cm.findColumnIndex("rate"), true);
                            cm.setHidden(cm.findColumnIndex("amount"), true);
                            cm.setHidden(cm.findColumnIndex("discountispercent"), true);
                            cm.setHidden(cm.findColumnIndex("prdiscount"), true);
                            cm.setHidden(cm.findColumnIndex("prtaxid"), true);
                            cm.setHidden(cm.findColumnIndex("taxamount"), true);
                        }
                    } else {
                        if (!Wtf.account.companyAccountPref.unitPriceInGR) {
                            cm.setHidden(cm.findColumnIndex("rate"), true);
                            cm.setHidden(cm.findColumnIndex("amount"), true);
                            cm.setHidden(cm.findColumnIndex("discountispercent"), true);
                            cm.setHidden(cm.findColumnIndex("prdiscount"), true);
                            cm.setHidden(cm.findColumnIndex("prtaxid"), true);
                            cm.setHidden(cm.findColumnIndex("taxamount"), true);
                        }
                    }
                    this.reconfigure( this.store, cm);
                } else {
                    var cm=this.getColumnModel();
                    if (this.isCustomer) {
                        if (!Wtf.account.companyAccountPref.unitPriceInDO) {
                            cm.setHidden(cm.findColumnIndex("rate"), true);
                            cm.setHidden(cm.findColumnIndex("amount"), true);
                            cm.setHidden(cm.findColumnIndex("discountispercent"), true);
                            cm.setHidden(cm.findColumnIndex("prdiscount"), true);
                            cm.setHidden(cm.findColumnIndex("prtaxid"), true);
                            cm.setHidden(cm.findColumnIndex("taxamount"), true);
                        }else {
                            cm.setHidden(cm.findColumnIndex("rate"), false);
                            cm.setHidden(cm.findColumnIndex("amount"), false);
                            cm.setHidden(cm.findColumnIndex("discountispercent"), false);
                            cm.setHidden(cm.findColumnIndex("prdiscount"), false);
                            /**
                             * Product tax Combo and Tax Amount not used for Line level GST Terms tax
                             */
                            if(!(WtfGlobal.isIndiaCountryAndGSTApplied() || WtfGlobal.isUSCountryAndGSTApplied())){
                                if (this.includeProTax != null && this.includeProTax.getValue()) {
                                    cm.setHidden(cm.findColumnIndex("prtaxid"), false);
                                }
                                cm.setHidden(cm.findColumnIndex("taxamount"), false);
                            }
                        }
                    }else{
                        if (!Wtf.account.companyAccountPref.unitPriceInGR) {
                            cm.setHidden(cm.findColumnIndex("rate"), true);
                            cm.setHidden(cm.findColumnIndex("amount"), true);
                            cm.setHidden(cm.findColumnIndex("discountispercent"), true);
                            cm.setHidden(cm.findColumnIndex("prdiscount"), true);
                            cm.setHidden(cm.findColumnIndex("prtaxid"), true);
                            cm.setHidden(cm.findColumnIndex("taxamount"), true);
                        }else {
                            cm.setHidden(cm.findColumnIndex("rate"), false);
                            cm.setHidden(cm.findColumnIndex("amount"), false);
                            cm.setHidden(cm.findColumnIndex("discountispercent"), false);
                            cm.setHidden(cm.findColumnIndex("prdiscount"), false);
                             /**
                             * Product tax Combo and Tax Amount not used for Line level GST Terms tax
                             */
                            if(!(WtfGlobal.isIndiaCountryAndGSTApplied() || WtfGlobal.isUSCountryAndGSTApplied())){
                                if (this.includeProTax != null && this.includeProTax.getValue()) {
                                    cm.setHidden(cm.findColumnIndex("prtaxid"), false);
                                }
                                cm.setHidden(cm.findColumnIndex("taxamount"), false);
                            }
                        }
                    }
                    if((!Wtf.account.companyAccountPref.isBatchCompulsory && !Wtf.account.companyAccountPref.isSerialCompulsory && !Wtf.account.companyAccountPref.isLocationCompulsory && !Wtf.account.companyAccountPref.isWarehouseCompulsory && !Wtf.account.companyAccountPref.isRowCompulsory && !Wtf.account.companyAccountPref.isRackCompulsory && !Wtf.account.companyAccountPref.isBinCompulsory)){
                        cm.setHidden(cm.findColumnIndex("serialrenderer"),true);                        
                    }else{
                        cm.setHidden(cm.findColumnIndex("serialrenderer"),false);
                    }
                    this.reconfigure( this.store, cm);
//                    Wtf.Msg.alert('Status', action.msg);
                }
            },function() {
            });
        
        }
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
    
    callSerialNoWindow:function(obj){
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
            
        var deliveredprodquantity = obj.data.dquantity;
        deliveredprodquantity = (deliveredprodquantity == "NaN" || deliveredprodquantity == undefined || deliveredprodquantity == null)?0:deliveredprodquantity;

     if(deliveredprodquantity<=0){
              WtfComMsgBox(["Info","Quantity should be greater than zero. "], 2);
              return false;
         }  
          var prorec=this.productComboStore.getAt(index); 
           if(firstRow==-1){
                prorec=this.store.getAt(index);
            }
            if(prorec == undefined){
                prorec=obj;
            }
          var islocationavailble=false;
          var productsDefaultLocation="";
        if(prorec.data.isLocationForProduct){
            islocationavailble=true;
            if(prorec.data.location!="" && prorec.data.location!=undefined){
                productsDefaultLocation=prorec.data.location;
            }
        }else if(!prorec.data.isLocationForProduct){
            islocationavailble=true;
        }
               
        var iswarehouseavailble=false;
         var productsDefaultWarehouse="";
        if(prorec.data.isWarehouseForProduct){
            iswarehouseavailble=true;
            if(prorec.data.warehouse && prorec.data.warehouse!=undefined){
                productsDefaultWarehouse=prorec.data.warehouse;
            }
        }else if(!prorec.data.isWarehouseForProduct){
            iswarehouseavailble=true;
        }
            var filterJson='[';
              filterJson+='{"location":"'+productsDefaultLocation+'","warehouse":"'+productsDefaultWarehouse+'","productid":"'+prorec.data.productid+'","documentid":"'+documentid+'","purchasebatchid":""},';
                    filterJson=filterJson.substring(0,filterJson.length-1);
             filterJson+="]";

      if(this.isCustomer && (islocationavailble || iswarehouseavailble) ){ //if salesside and either default location and warehouse  then checkit
          var batchdetails = ""; 
                if((this.isEdit && !this.copyTrans)&& obj.data.batchdetails!=""){//if batchdetails is set from option "Set Warehouse/Location"
                    batchdetails = obj.data.batchdetails;
                }else{
                    batchdetails = filterJson;
                }
                Wtf.Ajax.requestEx({
                    url: "ACCInvoice/getBatchRemainingQuantity.do",
                    params: {
                        batchdetails:batchdetails,
                        transType:this.moduleid,
                        isEdit:this.isEdit,
                        linkflag:this.blockQtyFlag,
                        readOnly:this.readOnly
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
        var deliveredprodquantity = obj.data.dquantity;
        deliveredprodquantity = (deliveredprodquantity == "NaN" || deliveredprodquantity == undefined || deliveredprodquantity == null)?0:deliveredprodquantity;

      if(deliveredprodquantity<=0){
            WtfComMsgBox(["Info","Quantity should be greater than zero. "], 2);
            return false;
        }
        if(index!=-1){ 
           
           var prorec=this.productComboStore.getAt(index);
            if(firstRow==-1){
                prorec=this.store.getAt(index);
            }
            if(prorec == undefined){
                prorec=obj;
            }
           
            if(prorec == undefined){
                prorec = obj;
            }
            
            var isblockLooseSell = prorec.get('blockLooseSell');
            
            var quantity = 0;
            
            if(isblockLooseSell){
                quantity = deliveredprodquantity;
            }else{
                quantity = (obj.data.baseuomrate)*deliveredprodquantity;
            }
            
            var isLinkFromPO=false;
            if(!this.isCustomer && this.parentObj.fromLinkCombo.getValue()==0){
                isLinkFromPO=true;
            }
            var isLinkedFromSO=false;
            var isLinkedFromCI=false;
            if(this.parentObj!= undefined  && this.parentObj.fromLinkCombo.getValue()!= undefined ){
            if(this.isCustomer){
                if(this.parentObj.fromLinkCombo.getValue()==0){
                    isLinkedFromSO=true;
                }
                else if(this.parentObj.fromLinkCombo.getValue()==1){
                    isLinkedFromCI=true;
                }
             }
            }
            var clearbatchdetailsforCopy=true;
            
           if(this.copyTrans != undefined && this.copyTrans ==true && prorec.data.isWarehouseForProduct && prorec.data.isLocationForProduct && !prorec.data.isBatchForProduct && !prorec.data.isSerialForProduct && obj.data.isWarehouseLocationsetCopyCase!=undefined && obj.data.isWarehouseLocationsetCopyCase == true){
              clearbatchdetailsforCopy=false;
           }
           
           if (prorec.data.type != 'Service' && prorec.data.type != 'Non-Inventory Part') { // serial no for only inventory type of product
                if(prorec.data.isSerialForProduct== true && prorec.data.isSerialForProduct != undefined) {
                    var v = quantity;
                    v = String(v);
                    var ps = v.split('.');
                    var sub = ps[1];
                    if (sub!=undefined && sub.length > 0) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.exceptionMsgForDecimalQty")], 2);
                        obj.cancel=true;
                        return;
                    }
                }
            }
           var isOnlySerialForProduct= !( prorec.data.isLocationForProduct ||prorec.data.isWarehouseForProduct || prorec.data.isRowForProduct || prorec.data.isRackForProduct || prorec.data.isBinForProduct || prorec.data.isBatchForProduct ) &&  prorec.data.isSerialForProduct;
           this.isOnlyBatchForProduct= !(prorec.data.isRowForProduct || prorec.data.isRackForProduct || prorec.data.isBinForProduct || prorec.data.isSerialForProduct ) &&  prorec.data.isBatchForProduct;       //checking wether the product is only of batch type ERM-319
           var documentId = "";
            if(this.isEdit && obj.json && (isLinkedFromCI || isLinkedFromSO) && this.moduleid==Wtf.Acc_Delivery_Order_ModuleId && obj.json.dorowid){
                documentId = obj.json.dorowid ;
            }else if(this.isEdit){
                documentId = obj.data.rowid;
            }else if(isLinkedFromSO && this.moduleid==Wtf.Acc_Delivery_Order_ModuleId && obj.json!=undefined && obj.json.dorowid && !this.isEdit){
                documentId = obj.json.dorowid ;
            }else if(obj.json!=undefined && isLinkedFromCI){
                documentId = obj.json.sorowid;
            }
            /**
             * If Variable Purchase/Sales UOM conversion rate check is enable in company preferences and if DO is linked to SI which 
             * is linked to SO and if Qty is blocked in SO than setting isQuantityLockedInSOLinkedInSI flag as true because 
             * if Qty is blocked in SO than we do not have to allow user to edit Qty in batch serial Window this will work 
             * as exsisting system. ERM-319  
             */
            this.allowUserToEditQuantity = false;
            var isQuantityLockedInSOLinkedInSI = false;
            if (CompanyPreferenceChecks.differentUOM() && isLinkedFromCI && obj.data.islockQuantityflag == true) {
                isQuantityLockedInSOLinkedInSI = true;
            }
            /**
             * If Variable Purchase/Sales UOM conversion rate check is enable in company preferences and if DO is linked to SI which 
             * is linked to SO and if Qty is blocked in SO than setting isQuantityLockedInSOLinkedInSI flag as true because 
             * if Qty is blocked in SO than we do not have to allow user to edit Qty in batch serial Window OR if DO is linked 
             * directly to SO and if Qty is blocked in SO than we do not have to allow user to edit Qty in DO this will work 
             * as exsisting system. ERM-319 
             */
            if (CompanyPreferenceChecks.differentUOM() && ((isLinkedFromCI && !isQuantityLockedInSOLinkedInSI) || this.moduleid != Wtf.Acc_Delivery_Order_ModuleId || (isLinkedFromSO && !obj.data.islockQuantityflag))) {
                this.allowUserToEditQuantity = true;
            } 
                if( ! (this.isAutoFillBatchDetails) || isOnlySerialForProduct || !this.isCustomer ){
                    this.batchDetailswin=new Wtf.account.SerialNoWindow({
                        renderTo: document.body,
                    title:WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber"),
                    productName:prorec.data.productname,
                    barcodetype:prorec.data.barcodetype,
                    productcode : prorec.data.pid,
                    type:prorec.data.type,
                    uomName:prorec.data.uomname,
                    productUomid:prorec.data.uomid,
                    selectedProductUomid:obj.data.uomid,
                        //quantity:obj.data.dquantity,
                    quantity:quantity,
                    billid:obj.data.billid,
                    defaultLocation:prorec.data.location,
                    productid:prorec.data.productid,
                    isSales:this.isCustomer,
                    isLinkedFromSO:isLinkedFromSO,
                    isLinkedFromCI:isLinkedFromCI,
                    moduleid:this.moduleid,
                    transactionid:(this.isCustomer)?4:5,
                    isDO:this.isCustomer?true:false,
                    documentid:(this.copyTrans && Wtf.account.companyAccountPref.pickpackship)?"":documentId,
                    defaultWarehouse:prorec.data.warehouse,
                    defaultAvailbaleQty:this.AvailableQuantity,
                    batchDetails:(this.copyTrans && clearbatchdetailsforCopy && this.moduleid!=Wtf.Acc_Goods_Receipt_ModuleId )?"":obj.data.batchdetails,// in copy case clear the batchdetails except for goods receipt
                    warrantyperiod:prorec.data.warrantyperiod,
                    warrantyperiodsal:prorec.data.warrantyperiodsal,  
                    isLocationForProduct:prorec.data.isLocationForProduct,
                    isWarehouseForProduct:prorec.data.isWarehouseForProduct,
                    isRowForProduct:prorec.data.isRowForProduct,
                    isRackForProduct:prorec.data.isRackForProduct,  
                    isBinForProduct:prorec.data.isBinForProduct,
                    isBatchForProduct:prorec.data.isBatchForProduct,
                    isOnlyBatchForProduct:this.isOnlyBatchForProduct,
                    isSKUForProduct:prorec.data.isSKUForProduct,
                    isSerialForProduct:prorec.data.isSerialForProduct,
                    isShowStockType:(this.isCustomer)?true:false,
                    linkflag:isLinkFromPO?false:obj.data.linkflag,//As their no batch details for PO So we Sending the Linking Flag false
                    isEdit:this.isEdit,
                    copyTrans:this.copyTrans,
                    readOnly:this.readOnly,
                    width:950,
                    height:400,
                    resizable : false,
                        modal: true,
                        isWastageApplicable: prorec.data.isWastageApplicable,
                    parentObj:this.parentObj,
                    lineRec:obj,
                    allowUserToEditQuantity:this.allowUserToEditQuantity,    //Passing this parameter to check whether we have to allow user to edit Qty in batch serial window.ERM-319
                    parentGrid:this,
                    bomid:this.bomcodeCmb.value==undefined ? obj.data.bomid : this.bomcodeCmb.value,
                    linkedFrom:this.parentObj.fromLinkCombo.getValue(),
                    docrowid:((this.moduleid == Wtf.Acc_Delivery_Order_ModuleId) && (this.isEdit || isLinkedFromSO))?obj.data.docrowid : "",
                    });
                } else {
                    this.batchDetailswin=new Wtf.account.SerialNoAutopopulateWindow({
                        renderTo: document.body,
                    title:WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber"),
                    productName:prorec.data.productname,
                    barcodetype:prorec.data.barcodetype,
                    productcode : prorec.data.pid,
                    uomName:prorec.data.uomname,
                    productUomid:prorec.data.uomid,
                    selectedProductUomid:obj.data.uomid,
                    type:prorec.data.type,
                        //quantity:obj.data.dquantity,
                    quantity:quantity,
                    billid:obj.data.billid,
                    defaultLocation:prorec.data.location,
                    productid:prorec.data.productid,
                    isSales:this.isCustomer,
                    isLinkedFromSO:isLinkedFromSO,
                    isLinkedFromCI:isLinkedFromCI,
                    moduleid:this.moduleid,
                    transactionid:(this.isCustomer)?4:5,
                    isDO:this.isCustomer?true:false,
                    documentid:(this.copyTrans && Wtf.account.companyAccountPref.pickpackship)?"":documentId,
                    defaultWarehouse:prorec.data.warehouse,
                    defaultAvailbaleQty:this.AvailableQuantity,
                    batchDetails:(this.copyTrans && clearbatchdetailsforCopy)?"":obj.data.batchdetails,// in copy case clear the batchdetails
                    warrantyperiod:prorec.data.warrantyperiod,
                    warrantyperiodsal:prorec.data.warrantyperiodsal,  
                    isLocationForProduct:prorec.data.isLocationForProduct,
                    isWarehouseForProduct:prorec.data.isWarehouseForProduct,
                    isRowForProduct:prorec.data.isRowForProduct,
                    isRackForProduct:prorec.data.isRackForProduct,
                    isBinForProduct:prorec.data.isBinForProduct,
                    isBatchForProduct:prorec.data.isBatchForProduct,
                    isOnlyBatchForProduct:this.isOnlyBatchForProduct,
                    isSKUForProduct:prorec.data.isSKUForProduct,
                    isSerialForProduct:prorec.data.isSerialForProduct,
                    isShowStockType:(this.isCustomer)?true:false,
                    linkflag:isLinkFromPO?false:obj.data.linkflag,//As their no batch details for PO So we Sending the Linking Flag false
                    isEdit:this.isEdit,
                    copyTrans:this.copyTrans,
                    readOnly:this.readOnly,
                    width:950,
//                    height:400,
                    resizable : false,
                    modal: true,
                    isWastageApplicable: prorec.data.isWastageApplicable,
                    parentObj:this.parentObj,
                    lineRec:obj,
                    allowUserToEditQuantity:this.allowUserToEditQuantity,    //Passing this parameter to check whether we have to allow user to edit Qty in batch serial window.ERM-319
                    parentGrid:this,
                    bomid:this.bomcodeCmb.value==undefined ? obj.data.bomid : this.bomcodeCmb.value,
                    linkedFrom:this.parentObj.fromLinkCombo.getValue(),
                    docrowid:((this.moduleid == Wtf.Acc_Delivery_Order_ModuleId) && (this.isEdit || isLinkedFromSO))?obj.data.docrowid : "",
                    });


                }
         this.batchDetailswin.on("beforeclose",function(){
            this.batchDetails=this.batchDetailswin.getBatchDetails();
            var isfromSubmit=this.batchDetailswin.isfromSubmit;
            if(isfromSubmit){  //as while clicking on the cancel icon it was adding the reocrd in batchdetail json 
                obj.set("batchdetails",this.batchDetails);
                if(obj.data.isSerialForProduct && obj.data.islockQuantityflag && this.moduleid == Wtf.Acc_Delivery_Order_ModuleId && (isLinkedFromSO || isLinkedFromCI)){
                 //getting Batch details of row for serial change
                  Wtf.Ajax.requestEx({
                    url: "ACCSalesOrderCMN/getSalesOrderRowBatchJSON.do",
                    params: {
                        productid:prorec.data.productid,
                        documentid:((((obj.data.linkflag)?obj.data.linkflag:false)) && this.isEdit== false && isLinkedFromSO)?obj.data.rowid:(((obj.data.linkflag)?obj.data.linkflag:false) && isLinkedFromCI && this.isEdit== false)?obj.json.sorowid:"",
                        transType:this.moduleid,
                        moduleid:this.moduleid,
                        isEdit:this.isEdit,
                        linkingFlag:obj.data.linkflag,
                        isConsignment:false
                    }
                },this,this.genSuccessResponseReplaceSerial,this.genFailureResponseReplaceSerial);

                }
               else if(obj.data.islockQuantityflag && this.moduleid == Wtf.Acc_Delivery_Order_ModuleId && (isLinkedFromSO || isLinkedFromCI)){
                 //getting Batch details of row for serial change
                  Wtf.Ajax.requestEx({
                    url: "ACCSalesOrderCMN/getSalesOrderRowBatchJSON.do",
                    params: {
                        productid:prorec.data.productid,
                        documentid:((((obj.data.linkflag)?obj.data.linkflag:false)) && this.isEdit== false && isLinkedFromSO)?obj.data.rowid:(((obj.data.linkflag)?obj.data.linkflag:false) && isLinkedFromCI && this.isEdit== false)?obj.json.sorowid:"",
                        transType:this.moduleid,
                        moduleid:this.moduleid,
                        isEdit:this.isEdit,
                        linkingFlag:obj.data.linkflag,
                        isConsignment:false
                    }
                },this,this.genFailureResponseReplacewarehouse);

                }
            }
         },this);
         /*
          * On close event of batch serial window calculating the average base uom quantity and base uom rate and setting it into grid record.
          * after average calculation fireing afteredit event to recalculate the price and amount.
          * ERM-319
          */
         this.batchDetailswin.on("close",function(){
            if (CompanyPreferenceChecks.differentUOM() && this.allowUserToEditQuantity && this.isOnlyBatchForProduct) {
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
                    var receivedQuantity=obj.get("dquantity");
                    obj.set("baseuomrate", getRoundofValue(totalQty/receivedQuantity));
                    obj.set("baseuomquantity", getRoundofValue(totalQty));
                    this.fireEvent('afteredit', {
                        field: 'baseuomrate',
                        value: getRoundofValue(totalQty/receivedQuantity),
                        record:obj
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
         if(this.isAutoFillBatchDetails){                  // in auto fill details case serials are taken from serial details
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
             var responseserialid=response.data[i].purchaseserialid;    // used serialid for comparing to avoid duplicacy         
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
         if(response.data.length > 0 && (serialchangeCount > 0 || serial_change_Count>0) && SerialReplaceArr.length > 0){
             var serilReplaceWin = Wtf.getCmp('SerialReplaceWindow'); 
            if(serilReplaceWin == null){
                serilReplaceWin = new Wtf.SerialRepalceWindow({
                    id : 'SerialReplaceWindow',  
                    border : false,
                    title: WtfGlobal.getLocaleText("acc.replaceserialwin.title"),  
                    serialchangeCount:this.isAutoFillBatchDetails ? serial_change_Count : serialchangeCount,
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
    genSuccessResponseReplacewarehouse : function(response){
        var WarehouseReplaceArr = [];
        var jsonBatchDetails= eval(this.batchDetails);
         for(var i=0;i<response.data.length;i++){
             var iswarehouselocationidmatch = false;
             for(var k=0;k<jsonBatchDetails.length;k++){       
             if(response.data[i].warehouse ==jsonBatchDetails[k].warehouse && response.data[i].location ==jsonBatchDetails[k].location && response.data[i].purchasebatchid && jsonBatchDetails[k].purchasebatchid && response.data[i].purchasebatchid == jsonBatchDetails[k].purchasebatchid){
                    if(response.data[i].quantity ==jsonBatchDetails[k].quantity){
                      iswarehouselocationidmatch=true;                   
                      break;    
                    }else{
                        response.data[i].quantity = response.data[i].quantity - jsonBatchDetails[i].quantity;
                    }                    
                }
            }
                if(iswarehouselocationidmatch==false){
                    WarehouseReplaceArr.push(response.data[i]);
                }    
         }
         this.recAobj.set("replacebatchdetails",WarehouseReplaceArr);
    },
    genFailureResponseReplaceSerial : function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    genFailureResponseReplacewarehouse : function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    checkRow:function(obj){
        this.rowIndexForSpecialKey = obj.row; // rowindex
        var rec=obj.record;
        
        var proqty = obj.record.data['quantity'];
        proqty = (proqty == "NaN" || proqty == undefined || proqty == null)?0:proqty;
        
        var deliveredproqty = obj.record.data['dquantity'];
        deliveredproqty = (deliveredproqty == "NaN" || deliveredproqty == undefined || deliveredproqty == null)?0:deliveredproqty;
        
        
        var isblockLooseSell = false;
        
        var selectedProRec=this.productComboStore.getAt(this.productComboStore.find('productid',obj.record.data.productid));
        
        if(selectedProRec){
            
            isblockLooseSell = selectedProRec.get('blockLooseSell');
        }
        
        
        if(obj.field=="uomid"){
            var prorec = null;
            var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
            if(productComboIndex >=0){
                prorec = this.productComboStore.getAt(productComboIndex);
                if(prorec.data.type=='Service'){
//                          WtfComMsgBox(["Warning","UOM can not be set for Service and Non-Inventory products. "], 2);
                    return false;
                } else if(this.UomSchemaType==Wtf.UOMSchema && !prorec.data.multiuom){
//                          WtfComMsgBox(["Warning","Multi UOM not allowed for the selected product. "], 2);
                    return false;
                }
            }
        }
        if(obj.field=="productid" || obj.field=="pid"){
                 /**
             * ERP-32829 
             * Check all GST Dim value selected or not
             */
            if (this.isGST && !this.parentObj.tagsFieldset.checkGSTDimensionValues(this.parentObj) && !this.isModuleForAvalara) {
               if(this.uniqueCase != Wtf.GSTCustVenStatus.APPLY_IGST) // Wtf.GSTCustVenStatus.APPLY_IGST Customer/ Vendor IMPORT and EXPORT type only , For INDIA
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
            var index=-1;
            if(obj.field=="productid"){
                index=this.productComboStore.find('productid',obj.value);
            }else if(obj.field=="pid"){
                index=this.productComboStore.find('pid',obj.value);
            }
            if(index!=-1){
                rec=this.productComboStore.getAt(index);
            }
            var useStoreRec=false;
                if(prorec==undefined){
                     prorec= rec;
                     useStoreRec=true;
                    }
             if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
                if(prorec.data['LineTermdetails'] != ""){                   
                    rec.set('LineTermdetails',prorec.data['LineTermdetails']);
                }
            }        
            if(this.editTransaction){  //In Edit Case Check product quantity is greater than available quantity when selecting product
                var availableQuantity = prorec.data.quantity;    //This is in base UOM
                var lockQuantity = prorec.data.lockquantity; 
                    if(useStoreRec){
                        availableQuantity = prorec.data.availablequantity;
                        lockQuantity = prorec.data.lockquantity; 
                    }
                var copyquantity = 0;
                this.store.each(function(rec){
                    if(rec.data.productid == prorec.data.productid){
                        if(rec.data.copyquantity!=undefined) {
                            copyquantity = copyquantity + (rec.data.copyquantity*rec.data.baseuomrate); 
                        }
                    }
                },this);                
                availableQuantity = availableQuantity + copyquantity;                
                if(this.isCustomer&&this.store.find("productid",obj.value)>-1 && rec.data.type!='Service' && rec.data.type!='Non-Inventory Part' &&!this.isQuotation){
                    var quantity = 0;
                    this.store.each(function(rec){
                        if(rec.data.productid == obj.value){
                            var ind=this.store.indexOf(rec);
                            if(ind!=-1){
                                if(ind!=obj.row){                            
                                    quantity = quantity + (rec.data.dquantity*rec.data.baseuomrate);                                    
                                }
                            }     
                        }
                    },this);
                    quantity = quantity + (deliveredproqty*obj.record.data['baseuomrate']);
                    if((availableQuantity-lockQuantity)<quantity && prorec.data.isAutoAssembly != true ){
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productname']+' is '+availableQuantity], 2);
//                        obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+WtfGlobal.getLocaleText("acc.field.is")+(availableQuantity-lockQuantity)+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                            // value for quantity should not be set as 'obj.originalValue'
//                          rec.set("quantity",obj.originalValue); 
//                          rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2 && !Wtf.account.companyAccountPref.isnegativestockforlocwar){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
//                                  rec.set("quantity",obj.originalValue);
//                                  rec.set("dquantity",obj.originalValue);
//                                  rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
                                    this.store.remove(obj.record);
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                    }
                    }
                //WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.76")+" "+rec.data['productname']], 2);
                //obj.cancel=true;
                }else if(this.isCustomer&&availableQuantity<(deliveredproqty*obj.record.data['baseuomrate'])&& prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation && prorec.data.isAutoAssembly != true){
                     this.isValidEdit = false;
                       if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' '+WtfGlobal.getLocaleText("acc.field.is")+(availableQuantity-lockQuantity)+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                          // value for quantity should not be set as 'obj.originalValue'
//                          rec.set("quantity",obj.originalValue);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",obj.originalValue);
                                  rec.set("dquantity",obj.originalValue);
                                  rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                }
            }
            }
               else{ //In normal Case Check product quantity is greater than available quantity when selecting product
                if(this.isCustomer&&this.store.find("productid",obj.value)>-1 && rec.data.type!="" &&rec.data.type!='Service' && rec.data.type!='Non-Inventory Part' &&!this.isQuotation){
                    var quantity = 0;                 
                    this.store.each(function(rec){
                        if(rec.data.productid == obj.value){
                            var ind=this.store.indexOf(rec);
                            if(ind!=-1){
                                if(ind!=obj.row){          
                                    //To do - Need to check this
//                                    quantity = quantity + (rec.data.dquantity*rec.data.baseuomrate);
                                    quantity = quantity + rec.data.dquantity;
                                }
                            }     
                        }
                    },this);
                    //To do - Need to check this
//                    quantity = quantity + (obj.record.data['dquantity']*obj.record.data['baseuomrate']);
                    quantity = quantity + deliveredproqty;
                    if(rec.data['quantity']<quantity && rec.data['isAutoAssembly'] != true){
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productname']+' is '+rec.data['quantity']], 2);
//                        obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+' '+WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productname']+' '+WtfGlobal.getLocaleText("acc.field.is")+((rec.data['quantity'])-(rec.data['lockquantity']))+'. '+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")], 2);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2 && !Wtf.account.companyAccountPref.isnegativestockforlocwar){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
//                                  rec.set("quantity",obj.originalValue);
//                                  rec.set("dquantity",obj.originalValue);
//                                  rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
                                    this.store.remove(obj.record);
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                    }
                    }
                //WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.76")+" "+rec.data['productname']], 2);
                //obj.cancel=true;
                }else if(this.isCustomer&&((rec.data['quantity'])-(rec.data['lockquantity']))<obj.record.data['dquantity']&& prorec.data.type!="Service" && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation && rec.data['isAutoAssembly'] != true){
                    this.isValidEdit = false;
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productname']+' is '+rec.data['quantity']], 2);
//                    obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+' '+WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productname']+' '+WtfGlobal.getLocaleText("acc.field.is")+((rec.data['quantity'])-(rec.data['lockquantity']))+'. '+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")], 2);
                          // value for quantity should not be set as 'obj.originalValue'
//                          rec.set("quantity",obj.originalValue);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2 && !Wtf.account.companyAccountPref.isnegativestockforlocwar){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
//                                  rec.set("quantity",obj.originalValue);
//                                  rec.set("dquantity",obj.originalValue);
//                                  rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
                                    this.store.remove(obj.record);
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                        }
                }
            } 
        }else if(this.isCustomer&& !isblockLooseSell && (obj.field=="dquantity"||obj.field=="baseuomrate")&&obj.record.data['productid'].length>0&&!this.isQuotation){// Validation for products which are blocked for loose selling will be on updateRow method not here
                if(obj.field=="dquantity") {
                    var originalDquantity = obj.originalValue;
                    var newDquantity = obj.value;
                    var originalBaseuomrate = obj.record.data['baseuomrate'];
                    var newBaseuomrate = obj.record.data['baseuomrate'];
                } else if(obj.field=="baseuomrate") {
                    var originalDquantity = deliveredproqty;
                    var newDquantity = deliveredproqty;
                    var originalBaseuomrate = obj.originalValue;
                    var newBaseuomrate = obj.value;
                }
            prorec=this.productComboStore.getAt(this.productComboStore.find('productid',obj.record.data.productid));
            var useStoreRec=false;
            if(prorec==undefined){
                prorec= rec;
                useStoreRec=true;
            }  
            if(prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part') {
                var availableQuantity = prorec.data.quantity;
                var venconsignuomquantity = prorec.data.venconsignuomquantity;
                 var lockQuantity = prorec.data.lockquantity; 
                if(useStoreRec){
                    availableQuantity = prorec.data.availablequantity;
                    lockQuantity = prorec.data.lockquantity; 
                }
                 var islockQuantityflag=this.store.getAt(obj.row).data['islockQuantityflag'];; //in linked case whether salesorder is locked or not
	         var soLockQuantity=this.store.getAt(obj.row).data['lockquantity'];
	
                    var quantity = 0;
                    if(this.editTransaction){  //In Edit Case Check product quantity is greater than available quantity when selecting quantity                  
                        var copyquantity = 0;                    
                        this.store.each(function(rec){
                            if(rec.data.productid == prorec.data.productid){
                                copyquantity = copyquantity + (rec.data.copyquantity*rec.data.baseuomrate);
                                var ind=this.store.indexOf(rec);
                                if(ind!=-1){
                                    if(ind!=obj.row){                            
                                        quantity = quantity + (rec.data.dquantity*rec.data.baseuomrate);
                                    }   
                                }                            
                            }
                        },this);
                        quantity = quantity + (newDquantity*newBaseuomrate);
                    if(venconsignuomquantity>0){     // Warn Case
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.field.venonsignmentQuantityAvailble")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtouseconsignmentstock")+'</center>' , function(btn){
                            if(btn=="yes"){
                                availableQuantity= prorec.data.venconsignuomquantity;
                                //obj.cancel=false;
                                rec.set("isFromVendorConsign",true);
                                this.isQuantityAvailbeOrNotinEditCase(quantity,islockQuantityflag,availableQuantity,copyquantity,lockQuantity,soLockQuantity,originalDquantity,originalBaseuomrate,rec,obj,prorec);
                                //return;
                            }else{
                                rec.set("isFromVendorConsign",false);
                                this.isQuantityAvailbeOrNotinEditCase(quantity,islockQuantityflag,availableQuantity,copyquantity,lockQuantity,soLockQuantity,originalDquantity,originalBaseuomrate,rec,obj,prorec);
                                //obj.cancel=false;
                                //return false;
                            }
                        },this); //for Ignore Case no any Restriction on user 
                    }else if (availableQuantity!==""){  //if vendor consignmentquantity is not greater then check normal quantity
                       this.isQuantityAvailbeOrNotinEditCase(quantity,islockQuantityflag,availableQuantity,copyquantity,lockQuantity,soLockQuantity,originalDquantity,originalBaseuomrate,rec,obj,prorec);
                    }
                } else {   //In normal Case Check product quantity is greater than available quantity when selecting quantity                  
                    this.store.each(function(rec){
                        if(rec.data.productid == prorec.data.productid){
                            var ind=this.store.indexOf(rec);
                            if(ind!=-1){
                                if(ind!=obj.row){
                                    quantity = quantity + (rec.data.dquantity*rec.data.baseuomrate);
                                }
                            }                               
                        }
                    },this);
                    quantity = quantity + (newDquantity*newBaseuomrate);
                  
                    if(venconsignuomquantity>0){     // Warn Case
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.field.venonsignmentQuantityAvailble")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtouseconsignmentstock")+'</center>' , function(btn){
                            if(btn=="yes"){
                                availableQuantity= prorec.data.venconsignuomquantity;
                                //obj.cancel=false;
                                rec.set("isFromVendorConsign",true);
                                this.isQuantityAvailbeOrNot(quantity,islockQuantityflag,availableQuantity,lockQuantity,soLockQuantity,originalDquantity,originalBaseuomrate,rec,obj,prorec);
                               // return;
                            }else{
                                rec.set("isFromVendorConsign",false);
                                this.isQuantityAvailbeOrNot(quantity,islockQuantityflag,availableQuantity,lockQuantity,soLockQuantity,originalDquantity,originalBaseuomrate,rec,obj,prorec);
                               // obj.cancel=true;
                              //  return false;
                            }
                        },this); //for Ignore Case no any Restriction on user 
                    }else if (availableQuantity!==""){  //if vendor consignmentquantity is not greater then check normal quantity
                        this.isQuantityAvailbeOrNot(quantity,islockQuantityflag,availableQuantity,lockQuantity,soLockQuantity,originalDquantity,originalBaseuomrate,rec,obj,prorec);
                    }
                    
                }
                }
        }                
    },
    isQuantityAvailbeOrNot:function(quantity,islockQuantityflag,availableQuantity,lockQuantity,soLockQuantity,originalDquantity,originalBaseuomrate,rec,obj,prorec){
                       
                    if(islockQuantityflag)   //if DO is lonked with SO then we will consider quantity (available quantity-lock quantity excluding its own quantity
                    {
                        if((availableQuantity-(lockQuantity-soLockQuantity)) < quantity && prorec.data.isAutoAssembly != true) {
                            if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+' '+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' '+WtfGlobal.getLocaleText("acc.field.is")+(availableQuantity-(lockQuantity-soLockQuantity))+'. '+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")], 2);                      
                                rec.set("quantity",originalDquantity);
                                rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                                rec.set("baseuomrate",originalBaseuomrate);
                                obj.cancel=true;   
                            }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                                    if(btn=="yes"){
                                        obj.cancel=false;
                                    }else{
                                        rec.set("quantity",originalDquantity);
                                        rec.set("dquantity",originalDquantity);
                                        rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                                        rec.set("baseuomrate",originalBaseuomrate);
                                        obj.cancel=true;
                                        return false;
                                    }
                                },this); //for Ignore Case no any Restriction on user 
                                }                               
                            }

                    }else   if((availableQuantity-lockQuantity) < quantity && prorec.data.isAutoAssembly != true) {  //for normal check for all products available quantity
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+' '+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' '+WtfGlobal.getLocaleText("acc.field.is")+(availableQuantity-lockQuantity)+'. '+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")], 2);                      
                          rec.set("quantity",originalDquantity);
                          rec.set("dquantity",originalDquantity);
                          rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                          rec.set("baseuomrate",originalBaseuomrate);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                          Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",originalDquantity);
                                  rec.set("dquantity",originalDquantity);
                                  rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                                  rec.set("baseuomrate",originalBaseuomrate);
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); //for Ignore Case no any Restriction on user 
                        }
                    }  
    },
    isQuantityAvailbeOrNotinEditCase:function(quantity,islockQuantityflag,availableQuantity,copyquantity,lockQuantity,soLockQuantity,originalDquantity,originalBaseuomrate,rec,obj,prorec){
                    if(islockQuantityflag)   //if DO is lonked with SO then we will consider quantity (available quantity-lock quantity excluding its own quantity
                    {
                        if((availableQuantity-(lockQuantity-soLockQuantity)) < quantity && prorec.data.isAutoAssembly != true) {
                            if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' '+WtfGlobal.getLocaleText("acc.field.is")+(availableQuantity-lockQuantity)+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);                      
                                rec.set("quantity",originalDquantity);
                                rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                                rec.set("baseuomrate",originalBaseuomrate);
                                obj.cancel=true;   
                            }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                                    if(btn=="yes"){
                                        obj.cancel=false;
                                    }else{
                                        rec.set("quantity",originalDquantity);
                                        rec.set("dquantity",originalDquantity);
                                        rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                                        rec.set("baseuomrate",originalBaseuomrate);
                                        obj.cancel=true;
                                        return false;
                                    }
                                },this); //for Ignore Case no any Restriction on user 
                            }
                        }
                    
                    }else if((availableQuantity-lockQuantity) < quantity && prorec.data.isAutoAssembly != true) {  //for normal check for all products available quantity
                    /*
                     * In copy case Block/Warn messages was not showing if quantity enter greater than available qty.
                     */
                    if (this.editTransaction && !this.copyTrans) { //ERP-34481
                        availableQuantity = availableQuantity + copyquantity;
                    }
                    if((availableQuantity-lockQuantity) < quantity) {
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+WtfGlobal.getLocaleText("acc.field.is")+(availableQuantity-lockQuantity)+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                          rec.set("quantity",originalDquantity);
                          rec.set("dquantity",originalDquantity);
                          rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                          rec.set("baseuomrate",originalBaseuomrate);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",originalDquantity);
                                  rec.set("dquantity",originalDquantity);
                                  rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                                  rec.set("baseuomrate",originalBaseuomrate);
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); //for Ignore Case no any Restriction on user 
                        }
                    }
                    }
    },
    addBlankRow:function(){
         var Record = this.store.reader.recordType,f = Record.prototype.fields, fi = f.items, fl = f.length;
        var values = {},blankObj={};
        for(var j = 0; j < fl; j++){
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
    },
     
    addBlank:function(){       
        this.addBlankRow();
    },            
    loadPOProduct:function(){              
                          
      if(this.isCustomer && this.fromOrder && !this.isNote && !this.readOnly && !this.isOrder && !this.editTransaction)
    	  this.checkSOLinkedProducts();
        if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
            this.store.each(function(rec){
                if(rec.data['LineTermdetails'] != undefined  && rec.data['LineTermdetails'] != ""){
                    var termStore = eval(rec.data['LineTermdetails']);
                    /**
                     * Added Including GST Calculation 
                     */
                    if(this.parentObj && this.parentObj.includingGST && this.parentObj.includingGST.getValue() == true) {
                        termStore = this.calculateTermLevelTaxesInclusive(termStore, rec);
                    } else {
                        termStore = this.calculateTermLevelTaxes(termStore, rec);
                    }      
                    rec.set('LineTermdetails',JSON.stringify(termStore));
                }
            },this);
        }
    },

    checkSOLinkedProducts:function(){      
    	var msgBox = 0,msg="";
         var recordSet=[];
          this.isNegativeStock=false;
        if(this.store.data.length){ //Check Qty mentioned in SO/CI is greater than available quantity
            //To do - Need to check quantity checks for multi UOM change
            var storeData = []; 
            storeData =this.store.data.items;
            this.store.removeAll();
            for(var count=0;count<storeData.length;count++){
                var record=storeData[count];
                  recordSet[count]=record;
                  
                var deliveredproqty = record.data.dquantity;
                deliveredproqty = (deliveredproqty == "NaN" || deliveredproqty == undefined || deliveredproqty == null)?0:deliveredproqty;
                  
                var quantity = 0;
                this.store.each(function(rec){
                  if(rec.data.productid == record.data.productid){
                        quantity = quantity + (rec.data.dquantity*rec.data.baseuomrate);                                                     
                  }
                },this);
                quantity = quantity + (deliveredproqty*record.data.baseuomrate);
                var result = this.productComboStore.find('productid',record.data.productid);
                
                var ComboIndex=0;
                if(this.productOptimizedFlag!= undefined && (this.productOptimizedFlag==Wtf.Products_on_type_ahead || this.productOptimizedFlag == Wtf.Show_all_Products) && result==-1){
                    result=1;
                    ComboIndex=-1;
                }
                if(result >= 0){
                    var prorec=this.productComboStore.getAt(result);////if product type is of Inventory then check otherwise no need to check 
                    if(ComboIndex==-1){
                        prorec=record;
                    }
                    if(!this.editTransaction && prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part'  && quantity > prorec.data.quantity){
                        if(msg==""){
                            msg=record.data.productname+" in "+record.data.billno+" is "+prorec.data.quantity;
                        }else{
                            msg=msg+","+record.data.productname+" in "+record.data.billno+" is "+prorec.data.quantity;
                        };
                        msgBox = 1;
                    }
                    else{
                        this.store.add(record);
                    }
                }
        }
        }
        
//    	this.productComboStore.each(function(rec){
//    		var result = this.store.find('productid',rec.data.productid);
//                
//    		if(!this.editTransaction && result >= 0){
//    			var prorec=this.store.getAt(result);
//    			if(rec.data.type!='Service' && rec.data.quantity < prorec.data.quantity){
//                            var productid=rec.data.productid;
//                            this.store.each(function(record){
//                                if(productid==record.data.productid){
//                                    this.store.remove(record);                                    
//                                }
//                            },this);
//                            msgBox = 1;
//                      }
//    		}
//    	},this);

    	if(msgBox==1){
  //  		WtfComMsgBox(["Alert",'Available Qty for '+msg+' is below than the Qty mentioned in SO/CI, so Please Edit the Qty given in SO/CI first and then proceed.'], 2);
//    		Wtf.getCmp('orderNumber2Invoice').setValue('');
//    		Wtf.getCmp('linkToOrder2Invoice').setValue(false);
                 var info=WtfGlobal.getLocaleText("acc.field.QuantitygiveninDOareexceedingquantity")+msg;
                 if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                         info+=" <br><br>"+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+"</center>"
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),info], 2);
                        this.isNegativeStock=true;
                        this.store.removeAll();                        
                         return false;
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                            info+=" <br><br>"+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+"</center>"
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),info ,function(btn){
                               if(btn=="yes"){
                                   this.store.removeAll();
                                   this.store.add(recordSet);
                                   this.addBlankRow();
                               }else{
                                 //Wtf.getCmp(this.parentCmpID).loadStore();
                                 this.isNegativeStock=true;
                                 this.store.removeAll();
                                  this.addBlankRow();
                                  return false;
                               }
                            },this); 
                        }
    	}
    },
    loadPOGridStore:function(recids,linkingFlag,isForDOGROLinking,isFromTransactionForms,isJobWorkOutLinkedWithGRN){                
        this.store.load({
            params:{
                bills:recids,
                mode:43,
                closeflag:true,
                doflag:true,
                linkingFlag:linkingFlag,
                isForDOGROLinking:isForDOGROLinking,
                moduleid:this.moduleid,
                isJobWorkOutLinkedWithGRN:isJobWorkOutLinkedWithGRN
                }
            });
        this.store.on('load',function(store1, recArr){     
            var tempStore=this.productComboStore;
            var arrayOfRecords = [];    //taken array to push all records at a time.
//            this.store.removeAll();
            var productIds="";
            if(!this.isNegativeStock){
            for(var count=0;count<recArr.length;count++){
                var record=recArr[count];
                    
                    if (this.moduleid==Wtf.Acc_Delivery_Order_ModuleId) {
                        if (record.json.notAllowInvoiceToLink) {
                            this.parentObj.PO.setValue("");
                             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.DOfromFirstPartialInvoice") + "<b>" + " " + record.json.firstInvoiceNumber + "</b>" + " " + WtfGlobal.getLocaleText("acc.field.createdFromSO")], 2);
                            return false;
                        }

                        if (record.json.partialInvoice) {

                            /* Disabling following fields when DO is linking with Partial Invoice apply check on moduleid DO*/
                            var unitPrice = this.getColumnModel().findColumnIndex("rate")
                            this.getColumnModel().setEditable(unitPrice, false)
                            var discountType = this.getColumnModel().findColumnIndex("discountispercent")
                            this.getColumnModel().setEditable(discountType, false)
                            var discount = this.getColumnModel().findColumnIndex("prdiscount")
                            this.getColumnModel().setEditable(discount, false)
                            var productTax = this.getColumnModel().findColumnIndex("prtaxid")
                            this.getColumnModel().setEditable(productTax, false)
                            var deliveredQuantity = this.getColumnModel().findColumnIndex("dquantity")
                            this.getColumnModel().setEditable(deliveredQuantity, false)
                            var actualQuantity = this.getColumnModel().findColumnIndex("quantity")
                            this.getColumnModel().setEditable(actualQuantity, false)
                            /**
                             * Calculate discount while assigning value
                             */
                            if (record.data.discountjson != undefined && record.data.discountjson != '' && record.data.quantity >= record.data.dquantity) {
                                var jsonObj = JSON.parse(record.data.discountjson);
                                calculateDiscount(jsonObj.data, record, record.data.rate, record.data.dquantity, true);
                                /**
                                 * for subtotal update
                                 */
                                this.fireEvent('datachanged',this);
                            }

                        } else {

                            /* Enabling following fields when DO is linking with Partial Invoice apply check on moduleid DO*/

                            var unitPrice = this.getColumnModel().findColumnIndex("rate")
                            this.getColumnModel().setEditable(unitPrice, true)
                            var discountType = this.getColumnModel().findColumnIndex("discountispercent")
                            this.getColumnModel().setEditable(discountType, true)
                            var discount = this.getColumnModel().findColumnIndex("prdiscount")
                            this.getColumnModel().setEditable(discount, true)
                            var productTax = this.getColumnModel().findColumnIndex("prtaxid")
                            this.getColumnModel().setEditable(productTax, true)
                            var deliveredQuantity = this.getColumnModel().findColumnIndex("dquantity")
                            this.getColumnModel().setEditable(deliveredQuantity, true)
                             var actualQuantity = this.getColumnModel().findColumnIndex("quantity")
                             this.getColumnModel().setEditable(actualQuantity, true)
                            /**
                             * Calculate discount while assigning value
                             */
                            if (record.data.discountjson != undefined && record.data.discountjson != '' && record.data.quantity >= record.data.dquantity) {
                                var jsonObj = JSON.parse(record.data.discountjson);
                                calculateDiscount(jsonObj.data, record, record.data.rate, record.data.dquantity, true);
                                this.fireEvent('datachanged',this);
                            }
                        }
                    }
                    
                    
                    /**
                     * setting the islinkingFlag= true for the linked products so that
                     * price will be refresh or recalculate.
                     */
                    if (this.parentObj!=undefined && this.parentObj.PO.getValue()!=undefined && this.parentObj.PO.getValue() != "") {
                        record.set('islinkingFlag', true);
                    }
                    if(record.data.dquantity=="" && !this.allowZeroQuantity){
                    
                    var quantity = record.data.quantity;
                    quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;

                    record.data.dquantity=quantity;
                }
                if(record.data.description==""){
                var prorec=tempStore.getAt(tempStore.find('productid',record.data.productid));                                        
                     if(prorec!=undefined)
                    record.set('description',prorec.data.desc);
                    else
                        record.set('description',record.data.description); 
                }
                    /* Code is executed when DO/GR is created From SO/PO transaction form */
                    if (isFromTransactionForms) {
                        record.data.amount = record.data.amount + record.json.taxamount;
                        record.data.prtaxid = record.json.prtaxid;
                        record.data.taxamount = record.json.taxamount;
                        record.data.rateIncludingGst=record.json.rateIncludingGst;
                    }
                                  
//                    this.store.add(record);       //commented because after adding a single record renderer is applying - refer ticket ERP-13781
                    arrayOfRecords.push(record);
                 /**
                 * Recalculate GST Term taxes on Grid store Load
                 */
                if ((WtfGlobal.isIndiaCountryAndGSTApplied() || WtfGlobal.isUSCountryAndGSTApplied())) {
                    var rowAmountIndex = this.getColumnModel().findColumnIndex("amount");
                    if (record.get('LineTermdetails') != undefined && record.get('LineTermdetails') != '') {
                        var termStore = this.getTaxJsonOfIndia(record);
                        if (this.parentObj.includingGST && this.parentObj.includingGST.getValue() == true) {
                            this.getColumnModel().setRenderer(rowAmountIndex, WtfGlobal.withoutRateCurrencySymbol);
                            termStore = this.calculateTermLevelTaxesInclusive(termStore, record);
                        } else {
                            this.getColumnModel().setRenderer(rowAmountIndex, this.calAmountWithoutExchangeRate.createDelegate(this));
                            termStore = this.calculateTermLevelTaxes(termStore, record);
                        }
                        record.set('LineTermdetails', JSON.stringify(termStore));
                        updateTermDetails(this);
                    }
//                    this.parentObj.updateSubtotal();
                }
                productIds += record.data.productid+",";
            }
            /**
             *  if in company preferences Show Products on type ahead or Product Id as free text is selected then we do not load 
             *  this.productComboStore in case of linking product was not available global product combo store so when 
             *  Variable Purchase/Sales UOM conversion rate check is enable in company preferences than loading the product records 
             *  in this.productComboStore. ERM-319
             */
            productIds.substring(0, (productIds.length - 1));
            if (CompanyPreferenceChecks.differentUOM() && productIds!="" && this.productOptimizedFlag!=Wtf.Show_all_Products) {
                this.productComboStore.load({
                    params:{
                        selectedProductIds: productIds,
                        ismultiselectProductids:true
                        
                    }
                });
            }
            if(this.parentObj.isIndiaGST){
                getLinkDateTocheckGSTDataOnDateCase(this.parentObj, this);
            }
            this.store.add(arrayOfRecords);
            
            //SDP-12918 Updating subtotal after grid store load
            this.parentObj.updateSubtotal();
          }
           this.addBlankRow();   
           this.getView().refresh();
            if (this.isModuleForAvalara) {
                var productRecordsArr = [];
                for (var count = 0; count < recArr.length; count++) {
                    var tempObj = recArr[count].data;
                    if ((tempObj.pid || tempObj.productid) && tempObj.quantity) {
                        tempObj.rowIndex = count;
                        productRecordsArr.push(tempObj);
                    }
                }
                getTaxFromAvalaraAndUpdateGrid(this, undefined, productRecordsArr);
            }
            if (Wtf.account.companyAccountPref.isLineLevelTermFlag && (this.parentObj.POdate!= undefined || this.parentObj.POdate != '')) {
                updateTermDetails(this);
            }
        },this);
    },  
    getProductDetails:function(){
//        if(this.editTransaction && !this.isOrder){
//            this.store.each(function(rec){//converting in home currency
//                if(rec.data.rowid!=null){
//                    var amount,rate;
//                    if(this.record.data.externalcurrencyrate!=undefined&&this.record.data.externalcurrencyrate!=0){
//                        amount=rec.get('amount')/this.record.data.externalcurrencyrate;
//                        rate=rec.get('rate')/this.record.data.externalcurrencyrate;
//                    }else{
//                        amount=rec.get('amount')/rec.get('oldcurrencyrate');
//                        rate=rec.get('rate')/rec.get('oldcurrencyrate');
//                    }
//                    rec.set('amount',amount);
//                    rec.set('rate',rate);
//                    rec.set('permit',(rec.get('permit') != undefined && rec.get('permit') != null)?(rec.get('permit')):"");
//                }
//            },this);
//        }
        var arr=[];
        this.store.each(function(rec){            
            if(rec.data.productid!=""){
                rec.data[CUSTOM_FIELD_KEY]=Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, this.moduleid).substring(13));
                rec.data[CUSTOM_FIELD_KEY_PRODUCT]=Wtf.decode(WtfGlobal.getCustomColumnDataForProduct(rec.data, this.moduleid, rec).substring(20));
                arr.push(this.store.indexOf(rec));
        }
        rec.set('qtipdiscountstr',rec.data.qtipdiscountstr);
        rec.set('discountjson',rec.data.discountjson);
        },this)
        var jarray=WtfGlobal.getJSONArray(this,false,arr);
        //converting back in person currency
//        this.store.each(function(rec){
//            if(rec.data.rowid!=null && this.fromPO == false){
//                var amount,rate;
//                if(this.record.data.externalcurrencyrate!=undefined&&this.record.data.externalcurrencyrate!=0){
//                    amount=rec.get('amount')*this.record.data.externalcurrencyrate;
//                    rate=rec.get('rate')*this.record.data.externalcurrencyrate;
//                }else{
//                    amount=rec.get('amount')*rec.get('oldcurrencyrate');
//                    rate=rec.get('rate')*rec.get('oldcurrencyrate');
//                }
//                rec.set('amount',amount);
//                rec.set('rate',rate);
//                rec.set('permit',(rec.get('permit') != undefined && rec.get('permit') != null)?(rec.get('permit')):"");
//            }
//        },this);
        return jarray;
    },
    checkDetails: function (grid) {
        var v = WtfGlobal.checkValidItems(this.moduleid, grid);
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
    populateDimensionValueingrid: function (rec) {
        WtfGlobal.populateDimensionValueingrid(this.moduleid, rec, this);
    },
    setCurrencyid:function(currencyid,rate,symbol,rec,store) {
        this.symbol=symbol;
        this.currencyid=currencyid;
        this.rate=rate;
        for(var i=0;i<this.store.getCount();i++){
            this.store.getAt(i).set('currencysymbol',this.symbol);
            this.store.getAt(i).set('currencyrate',this.rate);
        }
        this.getView().refresh();
     },
        showPriceWindow:function(btn,text,rec, obj){
        if(btn!="yes")return;
        callPricelistWindow(rec,"pricewindow",!this.isCustomer,this.billDate);
        this.priceStore.on('load',this.setPrevProduct.createDelegate(this,[rec,obj]), this);
        Wtf.getCmp("pricewindow").on('update',function(){this.loadPriceStore()},this);
    },
        setPrevProduct:function(rec,obj){
        obj.cancel=false;
        obj.ckeckProduct=false
        if(this.fireEvent("validateedit", obj) !== false && !obj.cancel){
            obj.record.set(obj.field, obj.value);
            delete obj.cancel;
            this.fireEvent("afteredit", obj);
        }
    },
     loadPriceStore:function(val){
        this.billDate=(val==undefined?this.billDate:val);
        //if(this.editTransaction)
//         this.priceStore.on('load',this.setGridProductValues.createDelegate(this),this)
        this.priceStore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(this.billDate)}});
    },
        loadPriceAfterProduct : function(){
        if(Wtf.getCmp(this.id)){ //Load price store if component exists
            this.loadPriceStore();
        } else {
            this.productComboStore.un("load",this.loadPriceAfterProduct,this);//Remove event handler if Not exists
        }
    },
     loadPriceStoreOnly:function(val,pricestore){  //scope related issue
        this.dateChange=true;
        this.billDate=(val==undefined?this.billDate:val);        //if(this.editTransaction)
        pricestore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(this.billDate)}});
    },
    
    wastageQuantityRenderer: function(v,m,rec) {
        return "<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.wastageQuantity.desc") + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.field.wastageQuantity") + "' class='" + getButtonIconCls(Wtf.etype.wastageQuantity) + "'></div>";
    },

    callWastageQuantiyWindow: function(obj,grid,rowindex) {
        var deliveredprodquantity = obj.data.dquantity;
        deliveredprodquantity = (deliveredprodquantity == "NaN" || deliveredprodquantity == undefined || deliveredprodquantity == null) ? 0 : deliveredprodquantity;

        if (deliveredprodquantity <= 0) {
            WtfComMsgBox(["Info","Quantity should be greater than zero."], 2);
            return false;
        }
            
        this.wastageQuantityWin = new Wtf.account.calculateWastageAtDO({
            title: WtfGlobal.getLocaleText("acc.field.calculateWastageAtDO.desc"),
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            record: obj,
            readOnly: this.readOnly,
            width: 970,
            height: 320,
            resizable: false,
            modal: true,
            scope: this,
            isWastageApplicable: obj.data.isWastageApplicable
        });

        this.wastageQuantityWin.on("submit", function() {
            this.wastageDetails = this.wastageQuantityWin.getWastageDetails();
            obj.set("wastageDetails", this.wastageDetails);
        },this);

        this.wastageQuantityWin.show();
    },
    showTermWindow : function(record,grid,rowindex) {
        var deliveredprodquantity = record.data.quantity;
        deliveredprodquantity = (deliveredprodquantity == "NaN" || deliveredprodquantity == undefined || deliveredprodquantity == null)?0:deliveredprodquantity;
        if(deliveredprodquantity<=0){
            WtfComMsgBox(["Info","Quantity should be greater than zero. "], 2);
            return false;
        }
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
                isGST:this.isGST,
                isLineLevel : true,
                parentObj : this.parentObj,
                hideUnitPriceAmount : this.isCustomer ? !Wtf.dispalyUnitPriceAmountInSales : !Wtf.dispalyUnitPriceAmountInPurchase,
                gridObj : this,
                invAmount: record.data.amount,
                invQuantity: record.data.baseuomquantity,
                record:record,
                scope:this
            });
            this.Termwindow= new Wtf.Window({
                modal: true,
                id:'termselectionwindowtest',
                title: WtfGlobal.getLocaleText("acc.invoicegrid.termWindowTitle"),
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
                    hidden: WtfGlobal.isUSCountryAndGSTApplied() && !this.isModuleForAvalara ? false : true,
                    disabled:this.isViewTemplate,
                    scope:this,
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
     BeforeTermSave:function(){
//         this.getStore().getAt(this.TermGrid.rowindex).set("LineTermdetails", "");
//        this.getStore().getAt(this.TermGrid.rowindex).set("LineTermdetails", JSON.stringify(this.TermGrid.getTermDetails()));
        var rec = this.getStore().getAt(this.TermGrid.rowindex)
        var termStore = eval(this.TermGrid.getTermDetails());

        if(this.parentObj && this.parentObj.includingGST && this.parentObj.includingGST.getValue() == true) {
            termStore = this.calculateTermLevelTaxesInclusive(termStore, rec);
        } else {
            termStore = this.calculateTermLevelTaxes(termStore, rec);
        }
        rec.set("LineTermdetails", JSON.stringify(this.TermGrid.getTermDetails()));
        updateTermDetails(this);
        if (this.parentObj!= undefined && typeof this.parentObj.updateSubtotal=='function') {
            this.parentObj.updateSubtotal();
        }
        
    },
    /**
     * Calcualted  GST terms tax with Including GST Option
     * @param {type} termStore
     * @param {type} rec
     * @param {type} index
     * @returns {Array}
     */
    calculateTermLevelTaxesInclusive : function(termStore, rec, index){
        //        var unitPriceIncludingTax = rec.data.amount;
        var finaltaxamount = 0;
        var FinalAmountNonTaxableTerm =0;
        var finaltermStore = new Array();
        
        /**
         * Tax Calculation on deliveryed quantity only for DO and GRN
         */
        var quantity = rec.data.dquantity;
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
        var rateIncludingGst=getRoundofValueWithValues(rec.data.rateIncludingGst,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        quantity=getRoundofValue(quantity);
                
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
        for (var i = 0; i < finaltermStore.length; i++) {
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
           rec.set('amountwithouttax', getRoundedAmountValue(quantityAndAmount- finaltaxamount));
            if (this.parentObj && this.parentObj.includingGST && this.parentObj.includingGST.getValue()) {
                rec.set('rate', getRoundedAmountValue((quantityAndAmount - finaltaxamount) / quantity));
            }
        }
        if(FinalAmountNonTaxableTerm){
            rec.set('OtherTermNonTaxableAmount', getRoundedAmountValue(FinalAmountNonTaxableTerm));
        }
        
        return finaltermStore;
    },
    getTaxJsonOfIndia:function(prorec){
          
        this.venderDetails =WtfGlobal.searchRecord(this.parentObj.Name.store, this.parentObj.Name.getValue(), 'accid');

        var showOnlyCSTTax=this.venderDetails.data.interstateparty;
        var obj_CST =eval(prorec.data['LineTermdetails']);
        var termStore =new Array();
        for(var i_CST =0;i_CST<obj_CST.length;i_CST++){
            if(showOnlyCSTTax && (obj_CST[i_CST].termtype == 1)){
                if(!this.venderDetails.data.cformapplicable && obj_CST[i_CST].termtype == 1 && obj_CST[i_CST].taxCheck){
                    CSTCNotSubmitFormRate=obj_CST[i_CST].termpercentage;
                }
                continue;
            } else if(showOnlyCSTTax != undefined && !showOnlyCSTTax && (obj_CST[i_CST].termtype == 3)){
                continue;
            }
            if(!this.venderDetails.data.cformapplicable && obj_CST[i_CST].termtype == 3 && obj_CST[i_CST].taxCheck){
                obj_CST[i_CST].termpercentage = CSTCNotSubmitFormRate;
            }
            termStore.push(obj_CST[i_CST]);
        }

        return termStore;
    },
    
calculateTermLevelTaxes : function(termStore, rec, index,isNewProduct){
    var quantity = rec.data.dquantity;
    quantity = (quantity == "NaN" || quantity == undefined || quantity == null) ? 0 : quantity;
    quantity = getRoundofValue(quantity);
        var rate = rec.data.rate;
        rate = (rate == "NaN" || rate == undefined || rate == null) ? 0 : rate;
        rate = getRoundofValue(rate);
        var amount = isNaN(rec.data.amount) ? rate * quantity : rec.data.amount;
        var finaltaxamount = 0;
        var FinalAmountNonTaxableTerm = 0;
    if(index == undefined){
        index = 0;
    }
    var finaltermStore = new Array();
    var totalVatTax=0;
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
            }else{ //  for term tax value if abatement reset to 0(zero)
                if(termJson.termtype == Wtf.term.Service_Tax && termJson.taxtype == 1){ // For service tax  && 1 for percentage
                      termJson.taxvalue =  termJson.originalTermPercentage ;
                }
            }
        // Apply Tax on Asessable Value
        if(termJson.taxtype == 2){ // If Flat
            taxamount = termJson.termamount;
        }else if(termJson.taxtype == 1){ // If percentage
            var taxamountfun = assessablevalue * termJson.taxvalue / 100;
            taxamount= this.TaxCalculation(termJson,rec,taxamountfun,assessablevalue);   
        }
        /**
         * Calculate CESS on Type for INDIA GST
         */
        var isCESSApplicable = true;
        if (WtfGlobal.isIndiaCountryAndGSTApplied() && termJson[Wtf.DEFAULT_TERMID] != undefined && termJson[Wtf.DEFAULT_TERMID] != ''
                && (termJson[Wtf.DEFAULT_TERMID] == Wtf.GSTTerm.OutputCESS || termJson[Wtf.DEFAULT_TERMID] == Wtf.GSTTerm.InputCESS)) {
            var params = {};
            var returnArray = calculateCESSONTypeAndValuationAmount(params, quantity, assessablevalue, termJson, taxamount);
            if (returnArray[0] != undefined) {
                taxamount = returnArray[0];
            }
            if (returnArray[1] != undefined && !returnArray[1]) {
                isCESSApplicable = returnArray[1];
            }
        } 
        termJson.termamount = getRoundedAmountValue(taxamount);
        termJson.assessablevalue = getRoundedAmountValue(assessablevalue);
            
        if (termJson.termtype == Wtf.term.Others && (termJson.IsOtherTermTaxable != undefined && !termJson.IsOtherTermTaxable)) {
            if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                //we are first rounding taxamount & then sum it , because it causes 0.1 difference in Total Amount.
                FinalAmountNonTaxableTerm += getRoundedAmountValue(taxamount);
            } else {
                FinalAmountNonTaxableTerm += taxamount;
            }
        } else {
                var rcmApplicable = false;
                if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA /*&& !this.isCustomer*/) {
                    if ((this.isEdit || this.copyInv) && this.parentObj.record != undefined && this.parentObj.record.data != undefined && this.parentObj.record.data.gtaapplicable != undefined && this.parentObj.record.data.gtaapplicable != "") {
                        if (this.parentObj.record.data.gtaapplicable) {
                            if (termJson.termtype == Wtf.term.GST) {
                                rcmApplicable = true;
                            }
                        }
                    } else {
                        if (this.parentObj.GTAApplicable.getValue()) {// For RCM Applicable - Indian Compliance
                            if (termJson.termtype == Wtf.term.GST) {
                                rcmApplicable = true;
                            }
                        }
                    }
                }
                if (!rcmApplicable) {// Total amount of line bypass service taxes in case of service tax applicable
                    if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                        //we are first rounding taxamount & then sum it , because it causes 0.1 difference in Total Amount.
                        finaltaxamount += getRoundedAmountValue(taxamount);
                    } else {
                        finaltaxamount += taxamount;
                    }
                }
        }
        if (isCESSApplicable) {
             finaltermStore.push(termJson);
         }
        }
            if (termStore.length < 1) {
            rec.set('amount', getRoundedAmountValue(amount));
            rec.set('recTermAmount', getRoundedAmountValue(0));
            rec.set('taxamount', getRoundedAmountValue(0));
        }
        /**
         * if store has no record then got exception here, now handdle it.
         */
        if (this.store && this.store.data && this.store.data.length > 0) {
            if (finaltaxamount) {
                rec.set('recTermAmount', getRoundedAmountValue(finaltaxamount));
                rec.set('taxamount', getRoundedAmountValue(finaltaxamount));
            } else {
                rec.set('recTermAmount', getRoundedAmountValue(0));
                rec.set('taxamount', getRoundedAmountValue(0));
            }
        }
    if(FinalAmountNonTaxableTerm){
        rec.set('OtherTermNonTaxableAmount', getRoundedAmountValue(FinalAmountNonTaxableTerm));
    }
                
    return finaltermStore;
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
    },addTermForCalculation : function(termStore,termJson,totalVatTax,termSearchTerm,taxamount,vatFound,rec,uncheckedTerms,isNewProduct){
        var addCST=true;
        var needChangeJson=false;
        var termChangeIndex=-1;
        var formTypeCombo="";
        var replaceTermStore="";
        // For CST only -- Start
        if(Wtf.account.companyAccountPref.enablevatcst && termJson.termtype==Wtf.term.CST && (isNewProduct||!this.isEdit) && rec.data.valuationTypeVAT!=Wtf.excise.QUANTITY){// Condition Detail - for special case CST | This Logic Only for CST
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
            if(vatFound && termJson.termtype==Wtf.term.CST && rec.data.valuationTypeVAT!=Wtf.excise.QUANTITY){ // if VAT not found
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
    },checkTermExist : function (termStore,term,totalVatTax){
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
    }, getUncheckedTermDetails : function (prorec){ // it provide unchecked CST term list
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
    setPIDForBarcode : function(prorec,field,isTypeAhead){
        var rawValue = field.getRawValue();
//        if (this.productOptimizedFlag != Wtf.Products_on_Submit) {     // Commenting this block. Reason: same product is getting populated on next row.
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
        rec.data.productCode = prorec.pid;          // Replace rawValue with pid as rawValue might not be productCode always.
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
    /**
     * Below function filters the global UOM  store and display only those UOM in drop down which are used in UOM schema of product.
     */
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
        } else {
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
                } else {
                    continue;
                }
                state.columns.splice(i+1, 0, column);
            }
        }
        return state;
    }
});

function updateTermDetails(config){
    if(config != "" && config != undefined){
        var  LineTermdetails;
        var  LineTermTypeJson = {};
        var lineLevelArray = [];
        var TotalTaxAmt = 0;
        for(var m=0 ; m < (config.store.data.length) ; m++){
            LineTermdetails = eval(config.store.data.itemAt(m).data.LineTermdetails);
            //Already Defined. [[1,'VAT'],[2,'Excise Duty'],[3,'CST'],[4,'Service Tax'],[5,'Swachh Bharat Cess'],[6,'Krishi Kalyan Cess']]
            if(LineTermdetails != undefined && LineTermdetails != ""){
                for(var n = 0 ; n < LineTermdetails.length ; n++){
                    var prevAmt = 0;
                    if(LineTermdetails[n].termtype ==Wtf.term.Others && (LineTermdetails[n].IsOtherTermTaxable!=undefined && !LineTermdetails[n].IsOtherTermTaxable)){
                        continue;
                    }
                    if(LineTermTypeJson.hasOwnProperty(LineTermdetails[n].termtype)){
                        prevAmt = LineTermTypeJson[LineTermdetails[n].termtype];
                    }
                    var rcmApplicable = ((config.parentObj != undefined && config.parentObj.GTAApplicable != undefined) ? config.parentObj.GTAApplicable.getValue() : false);
                    if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA /*&& !this.isCustomer*/ && rcmApplicable) {
                        /** While changing in Vendor invoice's line grid, it update term details but it must bypass service taxes in case of RCM applicable. */
                        continue;
                    }
                        LineTermTypeJson[LineTermdetails[n].termtype] = prevAmt + LineTermdetails[n].termamount;
                    /**
                     * Add GST master details 
                     * ERP-32829
                     */
                    if (LineTermdetails[n].termtype == Wtf.term.GST) {
                        var isAlreadyexist = false;
                        for (var arr = 0; arr < lineLevelArray.length; arr++) {
                            var arrrec = lineLevelArray[arr];
                            if (arrrec.name == LineTermdetails[n].term) {
                                var tempamt=arrrec.preamount;
                                arrrec.taxAmount=WtfGlobal.addCurrencySymbolOnly(tempamt+LineTermdetails[n].termamount, this.symbol);
                                arrrec.preamount=tempamt+LineTermdetails[n].termamount;
                                TotalTaxAmt += LineTermdetails[n].termamount;
                                isAlreadyexist = true;
                            }
                        }
                        if (!isAlreadyexist) {
                            var temp = {};
                            temp.name = LineTermdetails[n].term;
                            temp.preamount = LineTermdetails[n].termamount;
                            TotalTaxAmt += LineTermdetails[n].termamount;
                            temp.taxAmount = WtfGlobal.addCurrencySymbolOnly(LineTermdetails[n].termamount, this.symbol);
                            lineLevelArray.push(temp);
                        }
                    }
                }
            }
        }
       // Below code is used in old taxaction India Compliance.
      /*  var TotalTaxAmt = 0;
        for(var i=0; i<Wtf.LineTermsMasterStore.getRange().length; i++){
            var temp = Wtf.LineTermsMasterStore.getRange()[i].data;
            if(LineTermTypeJson.hasOwnProperty(temp.typeid)){
                TotalTaxAmt += LineTermTypeJson[temp.typeid];
                temp['taxAmount'] = WtfGlobal.addCurrencySymbolOnly(LineTermTypeJson[temp.typeid],this.symbol);
            } else {
                temp['taxAmount'] = WtfGlobal.addCurrencySymbolOnly(0,config.symbol);
            }
            if (temp.typeid != Wtf.term.GST) {
                lineLevelArray.push(temp);
            }
        }*/
        if (config.parentObj !== undefined && config.parentObj.LineLevelTermTplSummary!=undefined) {
            config.parentObj.LineLevelTermTplSummary.overwrite(config.parentObj.LineLevelTermTpl.body, {
                lineLevelArray: lineLevelArray,
                TotalTaxAmt: WtfGlobal.addCurrencySymbolOnly(TotalTaxAmt, config.symbol)
            });
        }
    }
}
/*GRID for Sales Return*/

Wtf.account.SalesReturnGrid=function(config){
    this.enableColumnMove = !config.readOnly;
    this.enableColumnResize = !config.readOnly;
    this.isCustomer=config.isCustomer;
    this.isNoteAlso=(config.isNoteAlso)?config.isNoteAlso:false;
    this.isPayment=(config.isPayment)?config.isPayment:false;
    this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
    this.currencyid=config.currencyid;
    this.productID=null;
    this.id=config.id;
//    this.isOrder=config.isOrder;
    this.record=config.record;
    this.billDate=new Date();
    this.dateChange=false;
    this.isGST=config.isGST;   // ERP-32829 
    this.pronamearr=[];
    this.fromPO=config.fromPO;
    this.inputValue=config.inputValue;
    this.readOnly=config.readOnly;
    this.editTransaction=config.editTransaction;
    this.editLinkedTransactionQuantity= Wtf.account.companyAccountPref.editLinkedTransactionQuantity;
    this.editLinkedTransactionPrice= Wtf.account.companyAccountPref.editLinkedTransactionPrice;
    this.isLinkedTransaction= (config.isLinkedTransaction == null || config.isLinkedTransaction == undefined)? false : config.isLinkedTransaction;
    this.UomSchemaType=Wtf.account.companyAccountPref.UomSchemaType;
    this.isEdit=config.isEdit;
    this.ExciseAmt=0,this.VatAmt=0,this.ServiceAmt=0,this.TotalTaxAmt=0;
    this.CSTAmt=0,this.SBCAmt=0,this.KKCAmt=0,this.otherAmt=0;
    this.copyTrans=config.copyTrans;
    this.noteTemp=config.noteTemp;
    this.fromOrder=config.fromOrder;
    this.moduleid = config.moduleid;
    //Flag to indicate whether Avalara integration is enabled and module is enabled for Avalara Integration or not
    this.isModuleForAvalara = (Wtf.account.companyAccountPref.avalaraIntegration && (config.moduleid == Wtf.Acc_Sales_Return_ModuleId)) ? true : false;
    this.forCurrency="";
    this.gridConfigId ="";
    this.CUSTOM_KEY = "customfield";
    this.commonproductStore="";
    this.productComboStore=this.isCustomer?Wtf.productStoreSalesOptimized:Wtf.productStoreOptimized;
    this.sModel = new Wtf.grid.CheckboxSelectionModel({
            singleSelect :false
//            hidden:!this.setValuesToMultipleRec
     });  
    this.sModel.on('selectionchange',function(){this.fireEvent('onselection',this);},this);
    this.sModel.on("beforerowselect", this.checkSelections, this);
    this.createStore();
    this.createComboEditor();
    this.createColumnModel();
//    this.loadPriceStore();
    this.heplmodeid = config.heplmodeid;
    this.parentid=config.parentid;
    var colModelArray = [];
    colModelArray = GlobalColumnModel[this.moduleid];
    WtfGlobal.updateStoreConfig(colModelArray,this.store);
    colModelArray = [];
    if(colModelArray) {
        colModelArray=(GlobalColumnModelForProduct[this.moduleid]);
    }
    WtfGlobal.updateStoreConfig(colModelArray, this.store);
    this.AvailableQuantity=0;
    Wtf.account.SalesReturnGrid.superclass.constructor.call(this,config);
    this.addEvents({
        'datachanged':true,
        'onselection':true,
        'pricestoreload':true,
        'gridconfigloaded':true//// Event fire when WtfGlobal.getGridConfig() called and config get applied to grid column
    });
    this.on('populateDimensionValue',this.populateDimensionValueingrid,this);
}
Wtf.extend(Wtf.account.SalesReturnGrid,Wtf.grid.EditorGridPanel,{
    clicksToEdit:1,
    stripeRows :true,
    rate:1,
    symbol:null,
    layout:'fit',
    viewConfig:{forceFit:true},
    forceFit:true,
    loadMask:true,
    onRender:function(config){
         Wtf.account.SalesReturnGrid.superclass.onRender.call(this,config);
         this.isValidEdit = true;
//         this.on('render',this.addBlankRow,this);
          this.getGridConfig();
//          if(this.productOptimizedFlag!=Wtf.Products_on_Submit){
             this.on('afteredit', this.callUpdateRowOnProductComboStoreLoad, this);
//         } else {
//             this.on('afteredit',this.callupdateRowonProductLoad,this);
//         }
         if(Wtf.account.companyAccountPref.isLineLevelTermFlag && this.isEdit){
              this.parentObj.Name.store.load(); // On edit case we need vendor/customer interstate property so this code is only for india
         }
         this.on('validateedit',this.checkRow,this);
         this.on('rowclick',this.handleRowClick,this);
         this.on('cellclick',this.RitchTextBoxSetting,this);
         this.on('render', function () {
            new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
                this.on('statesave', this.saveGridStateHandler, this);
            }, this);
        }, this);
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
             if(e.field == "rate" && this.parentObj.includingGST && this.parentObj.includingGST.getValue()){
                e.cancel=true;
             }
//             if(this.isPayment&&(e.field == "pid")){
//                var msg = WtfGlobal.getLocaleText("acc.salesreturn.withPaymentLinkCashSales");
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg], 2);
//                return false ;
//            }
             if(!this.isValidEdit){ // Fixed Bug[13888]: Overlaping text box on validation alert messages. [on TAB key navigation]
                 e.cancel= true;
                 this.isValidEdit = true;
             }
//             if(e.field == "description"){
//               e.cancel=true;
//                if(e.record.data.productid!="")
//                    this.getPostTextEditor(e);
//                   return; 
//            }
            if ((this.isNoteAlso && (this.isEdit && !this.copyTrans) && !this.readOnly) && (this.inputValue == Wtf.NoteForOvercharge || this.inputValue == Wtf.CNDN_TYPE_FOR_MALAYSIA)) {
                var msg = "";
                if (e.field == "pid" || this.store.getCount() - 1 == e.row) {
                    e.cancel = true;
                    if (this.inputValue == Wtf.CNDN_TYPE_FOR_MALAYSIA) {
                        msg = !this.isCustomer ? WtfGlobal.getLocaleText("acc.CN.linkedinvoiceedit") : WtfGlobal.getLocaleText("acc.DN.linkedinvoiceedit");
                    } else if (this.inputValue == Wtf.NoteForOvercharge) {
                        msg = this.isCustomer ? WtfGlobal.getLocaleText("acc.cn.overcharge.addProduct.alert") : WtfGlobal.getLocaleText("acc.dn.overcharge.addProduct.alert");
                    }else{
                        msg = this.isCustomer ? WtfGlobal.getLocaleText("acc.srCN.linkedinvoiceedit") : WtfGlobal.getLocaleText("acc.prCN.linkedinvoiceedit");
                    }
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), msg], 4);
                    return;
                }
            }
            if(this.isNoteAlso && Wtf.account.companyAccountPref.countryid ==Wtf.Country.MALAYSIA && this.fromPO){   // && this.fromPO){
                if(this.store.getCount()-1==e.row){// if last row then don't allow edition, and appending row in case of linking'
                    e.cancel=true;
                    return;
                }
            }
            var isRateFieldEditable = true;
            if((e.field == "rate")&& ((e.record.data.isNewRecord =="" && !this.isEdit ) ||(e.record.data.linkid !="" && this.isEdit))){//isNewRecord for nornal records is "0"
                 if(this.editLinkedTransactionPrice && (this.fromOrder||(this.isEdit && this.fromOrder==false))){  
                      e.cancel = true;
                      isRateFieldEditable = false;
                 }
             }
             //Commenting below code for ticket ERP-27270
//            if (e.field == "rate" && this.parentObj && this.parentObj.fromLinkCombo.getValue() == "1") {
//                e.cancel = true;
//                isRateFieldEditable = false;
//            }
             if(e.field == "rate" && isRateFieldEditable){	// rate editable for product type "Service"
                var beforeEditRecord = this.productComboStore.getAt(this.productComboStore.find('productid', e.record.data.productid));
                if (beforeEditRecord == undefined || beforeEditRecord == null) {
                    if (e.record.data == undefined || e.record.data.productid == undefined || e.record.data.productid == "") {
                        e.cancel = true;
                    }
                }
            }
            var isQuantityFieldEditable = true;
             if((e.field == "quantity")&& ((e.record.data.isNewRecord =="" && !this.isEdit ) ||(e.record.data.linkid !="" && this.isEdit))){//isNewRecord for normal records is "0"
                 if(this.editLinkedTransactionQuantity && (this.fromOrder||(this.isEdit && this.fromOrder==false))){  
                      e.cancel = true;
                      isQuantityFieldEditable = false;
                 }
             }
             if(e.field == "quantity" && isQuantityFieldEditable){
//                if(this.productOptimizedFlag== undefined || this.productOptimizedFlag==Wtf.Show_all_Products){    
//                 var beforeEditRecord=this.productComboStore.getAt(this.productComboStore.find('productid',e.record.data.productid));
//            	 if(beforeEditRecord == undefined || beforeEditRecord == null){
//                        if(this.productOptimizedFlag== undefined || this.productOptimizedFlag==Wtf.Show_all_Products){  
//            		e.cancel = true;
//                        }else{
//                            if(e.record.data ==undefined || e.record.data.productid == undefined || e.record.data.productid ==""){ 
//                                e.cancel = true;
//            	 }
//             }
//             }
//                } 
             }
             if(e.field == "uomid" && this.UomSchemaType==Wtf.UOMSchema){ //Does not allow to change UOM in case product is not MULTIUOM Type
                    var beforeEditRecord= WtfGlobal.searchRecord(this.store, e.record.data.productid, 'productid');
                        if(beforeEditRecord != undefined && beforeEditRecord.data.multiuom != true ){ 
                               e.cancel = true;
                               return; 
                        }
                
         } else if(e.field == "uomid" && e.record.data.productid !="" && this.UomSchemaType == Wtf.PackegingSchema ){
                var prorec= WtfGlobal.searchRecord(this.store, e.record.data.productid, 'productid');
                if(prorec!=undefined){
                    for (var k = 0; k < e.grid.colModel.config.length; k++) {
                        if(e.grid.colModel.config[k].editor && e.grid.colModel.config[k].editor.field.store && e.grid.colModel.config[k].dataIndex=='uomid'){ 
                            var store = e.grid.colModel.config[k].editor.field.store;                          
                            store.clearFilter();
                            store.filterBy(function(rec) {
                                if ((prorec.data.caseuom != undefined && prorec.data.caseuom == rec.data.uomid )||(prorec.data.inneruom !=undefined  && prorec.data.inneruom == rec.data.uomid) ||(prorec.data.uomid!=undefined && prorec.data.uomid == rec.data.uomid))
                                    return true
                                else 
                                    return false
                            }, this);
                        }
                    }
                }                  
         }
          if(e.field == "prdiscount" && this.moduleid==Wtf.Acc_Purchase_Return_ModuleId && this.parentObj.fromLinkCombo.getValue() != undefined
             &&this.parentObj.fromLinkCombo.getValue() !="" && this.parentObj.fromPO.getValue()==true){
             return false;
         }
         if(e.field == "prtaxid" && e.record.data.linkto!=""){
              e.cancel = true;
              return;
         }
            if (e.field.indexOf("Custom_") == 0) {
                if (this.readOnly || this.isViewTemplate) {
                    return false;
                }
            }
         },this);   
         this.hideShowCustomizeLineFields();
          /**
           *checks the allow zero quantity functionality is activated or not from  company preference
           */
        this.allowZeroQuantity = WtfGlobal.checkAllowZeroQuantityForProduct(this.moduleid);
        
          /*----Showing tax field at line level for Malaysian country when "Map taxes at product level" check is ON----- */
        if (CompanyPreferenceChecks.mapTaxesAtProductLevel() && this.parentObj!=undefined && this.parentObj.includeProTax && this.parentObj.includeProTax.getValue()) {

            var rowtaxindex = this.getColumnModel().findColumnIndex("prtaxid");
            var rowtaxamountindex = this.getColumnModel().findColumnIndex("taxamount");
            this.getColumnModel().setHidden(rowtaxindex, false);
            this.getColumnModel().setHidden(rowtaxamountindex, false);
        }
    },
    getGridConfig: function () {
        if (this.moduleid == Wtf.Acc_Credit_Note_ModuleId || this.moduleid == Wtf.Acc_Debit_Note_ModuleId) {
            WtfGlobal.getGridConfig(this, this.moduleid + "_" + this.inputValue, true, false);
        } else if (this.moduleid == Wtf.Acc_Sales_Return_ModuleId || this.moduleid == Wtf.Acc_Purchase_Return_ModuleId) {
            if(this.isPayment) {
                WtfGlobal.getGridConfig(this, this.moduleid + "_" + 3, true, true);
            } else if (this.isNoteAlso) {
                WtfGlobal.getGridConfig(this, this.moduleid + "_" + 2, true, true);
            } else {
                WtfGlobal.getGridConfig(this, this.moduleid + "_" + 1, true, true);
            }
        }
    },
    saveGridStateHandler: function (grid, state) {
        if(!this.readOnly){
            if (this.moduleid == Wtf.Acc_Credit_Note_ModuleId || this.moduleid == Wtf.Acc_Debit_Note_ModuleId) {
                WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleid + "_" + this.inputValue, this.gridConfigId, true);
            } else if (this.moduleid == Wtf.Acc_Sales_Return_ModuleId || this.moduleid == Wtf.Acc_Purchase_Return_ModuleId) {
                if(this.isPayment) {
                    WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleid + "_" + 3, this.gridConfigId, true);
                } else if (this.isNoteAlso) {
                    WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleid + "_" + 2, this.gridConfigId, true);
                } else {
                    WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleid + "_" + 1, this.gridConfigId, true);
                }
            }
        }
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
        calSubtotalInBase:function(){
        var subtotalinbase=0;
        var total=0;
        var count=this.store.getCount();
        for(var i=0;i<count;i++){
            total=getRoundedAmountValue(parseFloat(this.store.getAt(i).data['amount']));
            subtotalinbase+=(total*this.getExchangeRate());
        }
        return getRoundedAmountValue(subtotalinbase);
    },

      callUpdateRowOnProductComboStoreLoad: function(obj) {
         if (obj != undefined || obj != null) {
             this.obj=obj;
             if(this.obj.field=='pid'){                 
                 if (this.productOptimizedFlag != Wtf.Products_on_Submit) {
                     if (this.obj.record.data != undefined && this.obj.record.data != null) {
                        var rec = this.obj.record.data;
                        if (rec.productid != undefined && rec.productid != null && rec.productid != "") {
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
                    this.productComboStore.on('load', this.callupdateRowonProductSubmit, this); 
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
            this.productComboStore.un('load', this.callupdateRowonProductSubmit, this);// Once updateRow Called successfully we unload the onload process
        }
    },
      getPostTextEditor: function(e)
    {
        var _tw=new Wtf.EditorWindowQuotation({
            val:e.record.data.description
        });
    	
        _tw.on("okClicked", function(obj){
            var postText = obj.getEditorVal().textVal;
            var styleExpression  =  new RegExp("<style.*?</style>");
            postText=postText.replace(styleExpression,"");
            e.record.set("description",postText);
                 
             
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
            {name:'uomschematypeid'},
            {name:'isAutoAssembly'},
            {name:'uomname'},
            {name:'multiuom'},
            {name:'blockLooseSell'},
            {name:'parentid'},
            {name:'parentname'},
            {name:'reorderquantity'},
            {name:'quantity'},
            {name:'reorderlevel'},
            {name:'leadtime'},
            {name:'purchaseprice'},
            {name:'saleprice'},
            {name:'lockquantity'},
            {name: 'leaf'},
            {name: 'type'},
            {name:'prtaxid'},
            {name:'taxamount'},
            {name:'prtaxpercent'},
            {name:'prtaxname'},        
            {name: 'level'},
            {name: 'initialquantity',mapping:'initialquantity'},
            {name: 'initialprice'},
            {name: 'producttype'},
            {name: 'location'},
            {name: 'warehouse'},
            {name:'currencysymbol',defValue:this.symbol},
            {name:'recTermAmount'},
            {name:'OtherTermNonTaxableAmount'},
            {name:'displayUoMName'},
            {name:'displayUoMid'},
            {name:'displayuomrate',defValue:1.00},
            {name:'displayuomvalue',defValue:""},
            {name:'LineTermdetails'}
        ]);

        this.priceStore = new Wtf.data.Store({        
            url:"ACCProduct/getProductsForCombo.do",
            baseParams:{mode:22,
                termSalesOrPurchaseCheck:this.isCustomer
                },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.priceRec)
        });
        this.priceStore.on('load',this.setGridProductValues,this);
        
        this.storeRec = Wtf.data.Record.create([
            {name:'rowid'},
            {name:'productname'},
            {name:'billid'},
            {name:'billno'},
            {name:'isAutoAssembly'},
            {name:'productid'},
            {name:'description'},
            {name:'type'},
            {name:'partno'},
            {name:'quantity'},
            {name:"linkto"},
            {name:"linktype"},
            {name:"linkid"},
            {name:'lockquantity'},
            {name:'islockQuantityflag'},
            {name:'dquantity'},
            {name:'copyquantity'},
            {name:'copybaseuomrate',mapping:'baseuomrate'},//for handling inventory updation 
            {name:'baseuomquantity',defValue:1.00},
            {name:'multiuom'},
            {name:'uomname'},
            {name:'baseuomname'},
            {name:'uomid'},
            {name:'caseuom'},
            {name:'inneruom'},
            {name:'caseuomvalue'},
            {name:'inneruomvalue'},
            {name:'baseuomrate',defValue:1.00},
            {name:'remark'},
            {name:'invstore'},
            {name:'invlocation'},
            {name:'transectionno'},
            {name:'remquantity'},
            {name:'remainingquantity'},
            {name:'typeid',defValue:0},
            {name:'isNewRecord',defValue:"0"},
            {name: 'changedQuantity'},
            {name:'producttype'},
            {name:'permit'},
            {name:'linkto'},
            {name:'gstIncluded'},
            {name:'batchdetails'},
            {name:'linkid'},
            {name:'linktype'},
            {name:'invcreationdate'},
            {name:'linktransactionamountdue'},
            {name:'customfield'},
            {name:'productcustomfield'},
            {name:'rate'},
            {name:'amount',defValue:0},
            {name:'linkflag'},
            {name:'discount'},
            {name:'discountispercent',defValue:1},
            {name:'prdiscount',defValue:0},
            {name:'prtaxid'},
            {name:'taxamount'},
            {name:'taxamountforlinking'},
            {name:'reason'},
            {name: 'customer'}, // added in record due to set auto populate value of customer in add price in master window
            {name: 'vendor'}, // added in record due to set auto populate value of vendor in add price in master window
            {name:'pid'},
            {name: 'isLocationForProduct'},
            {name: 'isRowForProduct'},
            {name: 'isRackForProduct'},
            {name: 'isBinForProduct'},
            {name: 'isWarehouseForProduct'},
            {name: 'isBatchForProduct'},
            {name: 'isSerialForProduct'},
            {name: 'isWarehouseLocationsetCopyCase'},
            {name:'availablequantity'},
            {name:'availableQtyInSelectedUOM'},
            {name:'warrantyperiod'},
            {name:'warrantyperiodsal'},
            {name: 'isFromVendorConsign'},
            {name: 'location'},
            {name: 'warehouse'},
            {name: 'priceSource'},
            {name:'pricingbandmasterid'},
            {name: 'pricingbandmastername'},
            {name: 'srno', isForSequence:true},
            {name:'recTermAmount'},
            {name:'OtherTermNonTaxableAmount'},
            {name:'LineTermdetails'},
            {name:'taxclass'},
            {name:'taxclasshistoryid'},            
            {name:'hasAccess'},
            {name: 'productweightperstockuom'},
            {name: 'productweightincludingpakagingperstockuom'},
            {name: 'productvolumeperstockuom'},
            {name: 'productvolumeincludingpakagingperstockuom'},
            {name: 'originallyLoadedRate',mapping : 'rate'},
            {name: 'invjournalentryid'},
            {name: 'invbillid'},
            {name: 'invjeentryno'},
            {name: 'invamountinbase'},
            {name: 'invdiscount'},
            {name: 'invamountdue'},
            {name: 'uomschematypeid'},
            {name: 'displayUoMid'},
            {name: 'displayUoMName'},
            {name: 'displayuomvalue',defvalue : ""},
            {name: 'displayuomrate',defValue:1},
            {name: 'rateIncludingGst'},
            {name: 'invamount'},
            {name: 'qtipdiscountstr'},
            {name: 'discountjson'},
            {name:'appliedTDS', type:'string'},// Contain TDS details record for TDS calculation window
            {name:'tdsamount', defValue:0.0},
            {name :'purchasetaxId'},
            {name: 'isUserModifiedTaxAmount', defValue: false},
            {name: 'salestaxId'}
        ]);
        var url=Wtf.req.account+((this.fromOrder||this.readOnly)?((this.isCustomer)?'CustomerManager.jsp':'VendorManager.jsp'):((this.isCN)?'CustomerManager.jsp':'VendorManager.jsp'));
        if(this.fromOrder)
           url=Wtf.req.account+(this.isCustomer?'CustomerManager.jsp':'VendorManager.jsp');
        this.store = new Wtf.data.Store({
            url:url,
//            sortInfo:{
//                field:'srno',
//                direction:'ASC'
//            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.storeRec),
            baseParams : {
                srflag : true
            }
        });
        
        this.store.on("beforeload", function() {
            WtfGlobal.setAjaxTimeOut();// set ajax timeout to 15mins
        }, this);
        
        this.store.on("load", function() {
            WtfGlobal.resetAjaxTimeOut(); // sets ajax timeout to 30secs
            
            if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
                this.store.each(function(rec){
                    if(rec.data['LineTermdetails'] != undefined  && rec.data['LineTermdetails'] != ""){
                        var termStore = eval(rec.data['LineTermdetails']);
                    if (!this.isModuleForAvalara) {
                        termStore = this.calculateTermLevelTaxes(termStore, rec);
                    }
                           
                        rec.set('LineTermdetails',JSON.stringify(termStore));
                    }   
                },this);
            }
        }, this);
        
    },
      
    createComboEditor: function() {
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
        
         this.productId= new Wtf.form.TextField({
            name:'pid'
//            readOnly:true
        });
        
        this.productComboStore.on('beforeload',function(s,o){
                if(!o.params)o.params={};
                var currentBaseParams = this.productComboStore.baseParams;
                currentBaseParams.termSalesOrPurchaseCheck = this.isCustomer;
                currentBaseParams.moduleid = this.moduleid;         // Passing Moduleid
                currentBaseParams.termSalesOrPurchaseCheck = this.isCustomer;
                currentBaseParams.isDefault = true;
                currentBaseParams.searchProductString = this.productOptimizedFlag==Wtf.Products_on_Submit?this.productId.getValue():""; 
                currentBaseParams.isFreeTextSearching = (this.productOptimizedFlag==Wtf.Products_on_Submit);
                this.productComboStore.baseParams=currentBaseParams;        
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
        if (this.productOptimizedFlag == undefined || this.productOptimizedFlag == Wtf.Show_all_Products) {
            var configforcombo = {displayField: 'pid', extraFields: ['productname', 'type']}; //Passing extra config for combo
            this.productEditor = CommonERPComponent.createProductPagingComboBox(100, 450, 30, this, baseParams, false, false,configforcombo);
        } else {
            this.productEditor = CommonERPComponent.createProductPagingComboBox(100, 450, 30, this, baseParams, true);
        }
        this.commonproductStore = this.productEditor.store;
        this.commonproductStore.on('beforeload', function (s, o) {
            if (!o.params)
                o.params = {};
            var currentBaseParams = this.commonproductStore.baseParams;
            currentBaseParams.termSalesOrPurchaseCheck = this.isCustomer;
            currentBaseParams.moduleid = this.moduleid;         // Passing Moduleid
            currentBaseParams.termSalesOrPurchaseCheck = this.isCustomer;
            currentBaseParams.isDefault = true;
            currentBaseParams.searchProductString = this.productOptimizedFlag == Wtf.Products_on_Submit ? this.productId.getValue() : "";
            currentBaseParams.isFreeTextSearching = (this.productOptimizedFlag == Wtf.Products_on_Submit);
            this.commonproductStore.baseParams = currentBaseParams;
        }, this);

        this.productEditor.on('beforeselect', function (combo, record, index) {
            if (this.productOptimizedFlag == Wtf.Products_on_type_ahead || this.productOptimizedFlag == Wtf.Show_all_Products) {
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
        
         if(this.productOptimizedFlag!=Wtf.Products_on_Submit){  
            //if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.edit))
                this.productEditor.addNewFn=this.openProductWindow.createDelegate(this);
         }
        //this.productComboStore.on("load",this.loadPriceAfterProduct,this);
        
        chkUomload();
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
        
        this.remark= new Wtf.form.TextArea({
            name:'remark'
//            readOnly:true
        });
        
        
        Wtf.reasonStore.load();
        
        this.reason= new Wtf.form.FnComboBox({
            triggerAction:'all',
            mode: 'local',
//            selectOnFocus:true,
            valueField:'id',
            displayField:'name',
            id:"reason"+this.id,
            allowBlank:true,
            store:Wtf.reasonStore,
            addNoneRecord: false, //ERP-4699 [SJ]
//            anchor: '94%',
            width : 200,
//            typeAhead: true,
            forceSelection: true,
            fieldLabel: 'Reason',
            emptyText: 'Select Reason',
            name:'reason',
            hiddenName:'reason'            
        });
        
        this.reason.addNewFn=this.addReason.createDelegate(this);
        
        
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
        if(this.inputValue==Wtf.CNDN_TYPE_FOR_MALAYSIA||this.inputValue==Wtf.NoteForOvercharge){
            this.taxStore = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                },this.taxRec),
                //        url: Wtf.req.account + 'CompanyManager.jsp',
                url : "ACCTax/getTax.do",
                baseParams:{
                    mode:33,
                    
                }
            });
        }else{
            this.taxStore = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                },this.taxRec),
                //        url: Wtf.req.account + 'CompanyManager.jsp',
                url : "ACCTax/getTax.do",
                baseParams:{
                    mode:33,
                    moduleid:this.moduleid,                    
                    includeDeactivatedTax: this.isEdit != undefined ? (this.copyTrans ? false : this.isEdit) : false
                }
            });   
        }
        this.taxStore.on("load", function() {
            var record = new Wtf.data.Record({
                prtaxid: 'None',
                prtaxname: 'None'
            });
            this.taxStore.insert(this.taxStore.getCount()+1, record);
        }, this);
        
        this.taxStore.load();
        
        this.transTax= new Wtf.form.ExtFnComboBox({
            hiddenName:'prtaxid',
            anchor: '100%',
            store:this.taxStore,
            valueField:'prtaxid',
            forceSelection: true,
            displayField:'prtaxname',
//            addNewFn:this.addTax.createDelegate(this),
            scope:this,
            displayDescrption:'taxdescription',
            selectOnFocus:true,
            typeAhead: true,
            mode: 'remote',
            minChars:0,
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
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.tax, Wtf.Perm.tax.edit))
            this.transTax.addNewFn=this.addTax.createDelegate(this);

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
        
        this.transTaxAmount=new Wtf.form.NumberField({
            allowBlank: true,
            allowNegative: false,
            defaultValue:0,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
        });
        this.transDiscount=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            defaultValue:0,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
        });
//        this.inventoryStores = new Wtf.form.ComboBox({
//            store: Wtf.inventoryStore,
//            name:'storeid',
//            displayField:'storedescription',
//            valueField:'storeid',
//            mode: 'local',
////            fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.12"),
//            valueField:'id',
//            displayField:'name',
//            store:Wtf.inventoryStore,
//            anchor:'90%',
//            typeAhead: true,
//            forceSelection: true,
//            name:'location',
//            hiddenName:'location'
//        });
////       chkinventoryWarehouse();
//        Wtf.inventoryStore.load();
//        
//        this.inventoryLocation = new Wtf.form.ComboBox({   //warehouse
//            triggerAction:'all',
//            mode: 'local',
////            fieldLabel:WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),
//            valueField:'id',
//            displayField:'name',
//            store:Wtf.inventoryLocation,
//            anchor:'90%',
//            typeAhead: true,
//            forceSelection: true,
//            name:'warehouse',
//            hiddenName:'warehouse'
//        });
////      chkinventoryLocation();
//      Wtf.inventoryLocation.load();
        
        this.partno= new Wtf.form.TextField({
            name:'partno',
            maxLength : 255
        });

        this.actQuantity=new Wtf.form.NumberField({
            allowBlank: false,            
            defaultValue:0,
            allowNegative: false,
            maxLength:15,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
        });
        
        this.deliQuantity=new Wtf.form.NumberField({
            allowBlank: false,            
            defaultValue:0,
            allowNegative: false,
            maxLength:15,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
        });
        this.transBaseuomrate=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength:10,
            decimalPrecision: Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT
        });
        this.editprice = new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength:14,
            decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
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
        this.editPriceIncludingGST=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,    
            maxLength:14
        });
    },
    showUom:function(){
       callUOM('uomReportWin');
       Wtf.getCmp('uomReportWin').on('update', function(){
           Wtf.uomStore.reload();
       }, this);
    },
    addTax: function () {
        this.stopEditing();
        var p = callTax("taxwin");
        Wtf.getCmp("taxwin").on('update', function () {
            this.taxStore.reload();
        }, this);
    },
    addReason:function(){
        addMasterItemWindow('29');
        Wtf.getCmp('masterconfigurationonly').on('update', function(){Wtf.reasonStore.reload();}, this);
    },
    
    openProductWindow:function(){
        this.stopEditing();
       if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.create)){
            callProductWindow(false, null, "productWin");
            Wtf.getCmp("productWin").on("update",function(obj,productid){this.productID=productid;},this);
        }
        else{
              WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.creating")+" "+WtfGlobal.getLocaleText("acc.create.products")); 
        }
    },
    
    /**
     * Function is written to catch 'Enter', 'TAB', and Barcode scanner.
     */
     onSpecialKey: function (field, e) {
        if (e.keyCode == e.ENTER || e.keyCode == e.TAB) {
            if (field.getRawValue() != "") {
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
                    params: params
                }, this, function (response) {
                    var prorec = response.data[0];
                    if (prorec) {
                        var newrec = new this.productComboStore.reader.recordType(prorec);
                        var obj = {
                            field: this.productOptimizedFlag != Wtf.Products_on_Submit ? 'productid' : 'pid',
                            value: this.productOptimizedFlag != Wtf.Products_on_Submit ? prorec.productid : prorec.pid,
                            record: newrec,
                            row:this.rowIndexForSpecialKey
                        }
                        this.productComboStore.add(newrec);
                        this.checkRow(obj);
                        if (obj.cancel != true) {
                            this.setPIDForBarcode(prorec, field, true);
                        }
                    }
                }, function () {
                });
            }
        }
    },
    

    createColumnModel:function(){        
        this.summary = new Wtf.ux.grid.GridSummary();
        this.rowno=new Wtf.grid.RowNumberer();
       /* Hide/Show flag for Product Weight/Volumetric measurement */
        this.hideShowFlag=true;
        if((this.moduleid== Wtf.Acc_Delivery_Order_ModuleId) && Wtf.account.companyAccountPref.calculateproductweightmeasurment){
           this.hideShowFlag=false; 
        }
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
        });
        if(this.inputValue!=Wtf.CNDN_TYPE_FOR_MALAYSIA && this.inputValue!=Wtf.NoteForOvercharge){
            columnArr.push({
                header: "Add",
                align: 'center',
                width: 40,
                renderer: this.addProductList.createDelegate(this)
            });  
        }
        
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.invoice.lineItemSequence"),//"Sequence",
            width:65,
            align:'center',
//            dataIndex:'srno',
            name:'srno',
            renderer: Wtf.applySequenceRenderer
        });

        if(this.productOptimizedFlag!=Wtf.Products_on_Submit){
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.product.gridProductID"),//"Product ID",
                width:200,
                dataIndex:'pid',
                renderer: (this.readOnly?"":this.getComboNameRenderer(this.productEditor)),
                editor:(this.isNote||this.readOnly)?"":this.productEditor   
            });
        }else{
             columnArr.push({
                 header: WtfGlobal.getLocaleText("acc.product.gridProductID"),//"Product ID",
                 dataIndex: 'pid',
                 editor: this.readOnly?"":this.productId,
                 width:200
            });
        }
        
        
        columnArr.push({
             header: WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),//"Product Name",
          dataIndex:'productname'
            
            },
        {
            header:this.isCN?WtfGlobal.getLocaleText("acc.invoice.gridInvNo"):WtfGlobal.getLocaleText("acc.invoice.gridVenInvNo"),//"Invoice No.":"Vendor Invoice No.",
            width:150,
            dataIndex:this.noteTemp?'transectionno':'billno',
            hidden:!this.isNote
        },{
             header:WtfGlobal.getLocaleText("acc.do.partno"),//"Part No",
             dataIndex:"partno",
             width:250,
             hidden:true,  //made hidden as in SR and PR serial number column has no need to show 
             editor:(this.readOnly)?"":this.partno
         },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridDescription"),//"Description",
             dataIndex:"description",
             hidden:this.isNote,
             width:250,
             editor:(this.isNote||this.readOnly)?"":this.remark,
             renderer:this.descriptionRenderer
//             renderer:function(val){
//                val = val.replace(/(<([^>]+)>)/ig,"");
//                if(val.length<50)
//                    return val;   
//                else
//                    return val.substring(0,50)+" ...";   
//            }
         });
          columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModelForProduct[this.moduleid],undefined,undefined,this.readOnly);
          columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModel[this.moduleid],undefined,undefined,this.readOnly);
          columnArr.push(
          {
//             header:WtfGlobal.getLocaleText("acc.masterConfig.12"),   //location
//             dataIndex:'invlocation',
//              hidden:true,
////             hidden:!(Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel),                 
//             width:150,
//             renderer:Wtf.comboBoxRenderer(this.inventoryLocation),
//             editor:(this.readOnly)?"":this.inventoryLocation
//         },{
//             header:WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),  //warehouse
//             dataIndex:'invstore',
//             hidden:true,
////             hidden:!(Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel),                 
//             width:150,
//             renderer:Wtf.comboBoxRenderer(this.inventoryStores),
//             editor:(this.readOnly)?"":this.inventoryStores
//         },{
             header:WtfGlobal.getLocaleText("acc.field.ActualQuantity"),
             dataIndex:"quantity",             
             align:'right',
             width:200,
             editor:(this.readOnly || this.inputValue==Wtf.CNDN_TYPE_FOR_MALAYSIA||this.inputValue==Wtf.NoteForOvercharge)?"":this.actQuantity,
             renderer:this.quantityRenderer
//             renderer:this.storeRenderer(this.productComboStore,"productid","uomname")
        },{
             header:(this.inputValue==Wtf.CNDN_TYPE_FOR_MALAYSIA||this.inputValue==Wtf.NoteForOvercharge)?WtfGlobal.getLocaleText("acc.product.gridQty"):WtfGlobal.getLocaleText("acc.accPref.returnQuant"),
             dataIndex:"dquantity",
             align:'right',
             width:150,
             editor:(this.readOnly)?"":this.deliQuantity,
             renderer:this.quantityRenderer
//             renderer:this.storeRenderer(this.productComboStore,"productid","uomname")
         });
         if(this.inputValue!=Wtf.CNDN_TYPE_FOR_MALAYSIA && this.inputValue!=Wtf.NoteForOvercharge){
            columnArr.push(
            {
                header: '',
                dataIndex:'serialrenderer',//data index is needed to find column by data index
                align:'center',
                renderer: this.serialRenderer.createDelegate(this),
                hidden:!(Wtf.account.companyAccountPref.showprodserial),
                width:40
            },{
                header: '',
                dataIndex:'rowrackbinserialrenderer',//data index is needed to find column by data index
                align:'center',
                renderer: this.serialRenderer.createDelegate(this),
                hidden:(!Wtf.account.companyAccountPref.isBatchCompulsory && !Wtf.account.companyAccountPref.isSerialCompulsory && !Wtf.account.companyAccountPref.isLocationCompulsory && !Wtf.account.companyAccountPref.isWarehouseCompulsory && !Wtf.account.companyAccountPref.isRowCompulsory && !Wtf.account.companyAccountPref.isRackCompulsory && !Wtf.account.companyAccountPref.isBinCompulsory),
                width:40
            })
        }
        columnArr.push({
            header:WtfGlobal.getLocaleText("acc.invoice.gridUOM"),//"UOM",
            width:150,
            dataIndex:'uomid',
            renderer:Wtf.comboBoxRendererwithClearFilter(this.uomEditor),
            editor:(this.isNote||this.readOnly)?"":this.uomEditor  //||this.UomSchemaType==Wtf.PackegingSchema
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridRateToBase"),//Base UOM Rate
             dataIndex:"baseuomrate",
             align:'left',
             width:150,
             renderer:this.conversionFactorRenderer(this.productComboStore,"productid","uomname",this.store),
             editor:(this.isNote||this.readOnly||this.UomSchemaType==Wtf.PackegingSchema)?"":(Wtf.account.companyAccountPref.UomSchemaType===0  && Wtf.account.companyAccountPref.isBaseUOMRateEdit) ?this.transBaseuomrate : ""     //Does allow to user to change conversion factor
         },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridQtyInBase"),//Base UOM Quantity
             dataIndex:"baseuomquantity",
             align:'right',
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
            hidden : this.hideShowFlag 
        },{
            header:WtfGlobal.getLocaleText("acc.productList.unitWeightWithPackaging"),//Unit Weight with Packaging',
            width:150,
            align:'right',
            dataIndex:'productweightincludingpakagingperstockuom',
            renderer: WtfGlobal.weightRenderer,
            editor : this.productWeightPackagingEditor,
            hidden : this.hideShowFlag 
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
            header:WtfGlobal.getLocaleText("acc.field.priceBand"),
            width:250,
            align: 'left',
            dataIndex:this.readOnly?'pricingbandmastername':'pricingbandmasterid',
            hidden: (!this.isCustomer && !Wtf.account.companyAccountPref.productPricingOnBands) || (this.isCustomer && !Wtf.account.companyAccountPref.productPricingOnBandsForSales),
            renderer:this.readOnly?"":Wtf.comboBoxRendererwithClearFilter(this.pricingBandMasterEditor),
            editor:(this.isNote||this.readOnly||this.isViewTemplate)?"":this.pricingBandMasterEditor //||this.UomSchemaType==Wtf.PackegingSchema
        });
        
        /*CHECK TO HIDE SHOW "UNIT PRICE INCLUDING GST" COLUMN BASED ON COMPANY PREFERENCE SETTING FOR SR & PR*/
         if((this.isCustomer?Wtf.account.companyAccountPref.unitPriceInSR:Wtf.account.companyAccountPref.unitPriceInPR)){
            columnArr.push({
            header:(Wtf.account.companyAccountPref.countryid!= Wtf.Country.INDONESIA)?WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingGST"):WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingVAT"),// "Unit Price Including GST",
            dataIndex: "rateIncludingGst",
            align:'right',
            width:150,
            renderer:this.unitPriceRendererWithPermissionCheck.createDelegate(this),
            editor:(this.isNote||this.readOnly || (this.isCustomer?!Wtf.dispalyUnitPriceAmountInSales:!Wtf.dispalyUnitPriceAmountInPurchase)) ? "" : this.editPriceIncludingGST,
            editable:true,
            hidden: true
            });
        }
        
        columnArr.push({
//            header:WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice"), // "Unit Price",
            header: (this.isNoteAlso && (this.inputValue==Wtf.CNDN_TYPE_FOR_MALAYSIA || this.inputValue==Wtf.NoteForOvercharge)) ? WtfGlobal.getLocaleText("acc.common.adjustmentPrice") : WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice"), // Adjustment Price/Unit Price
            dataIndex: "rate",
            align:'right',
            width:150,
            renderer:this.unitPriceRendererWithPermissionCheck.createDelegate(this),
            editor:(this.isNote||this.readOnly) ? "" : this.editprice,
            editable:true,
            hidden: !(this.isCustomer?(Wtf.account.companyAccountPref.unitPriceInSR || this.isNoteAlso):(Wtf.account.companyAccountPref.unitPriceInPR || this.isNoteAlso)) // this.isNoteAlso flag will be true if you are creating Sale/Purchase Return with Credit/Debit Note also
        },{
            header: WtfGlobal.getLocaleText("acc.field.priceSource"), // "Price Source",
            dataIndex: "priceSource",
            id: this.id + "priceSource",
            align: 'left',
            width: 250,
            renderer: WtfGlobal.deletedRenderer,
            hidden: (!this.isCustomer && !Wtf.account.companyAccountPref.productPricingOnBands) || (this.isCustomer && !Wtf.account.companyAccountPref.productPricingOnBandsForSales)
        },{
            header: WtfGlobal.getLocaleText("acc.field.DiscountType"),
            width:150,
            dataIndex:'discountispercent',
            id:this.id+"discountispercent",
//            fixed:true,
            hidden:!(this.isCustomer?(Wtf.account.companyAccountPref.unitPriceInSR || this.isNoteAlso):(Wtf.account.companyAccountPref.unitPriceInPR || this.isNoteAlso)),
            renderer:Wtf.comboBoxRenderer(this.rowDiscountTypeCmb),
            editable:(!this.readOnly && this.isNoteAlso),
            editor:(!this.readOnly && this.isNoteAlso)?this.rowDiscountTypeCmb:""
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridDiscount"),//"Discount",
             dataIndex:"prdiscount",
             id:this.id+"prdiscount",
             align:'right',
//             fixed:true,
             width:150,
             hidden:!(this.isCustomer?(Wtf.account.companyAccountPref.unitPriceInSR || this.isNoteAlso):(Wtf.account.companyAccountPref.unitPriceInPR || this.isNoteAlso)),
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
             editable:(!this.readOnly && this.isNoteAlso),
             editor:(!this.readOnly && this.isNoteAlso)?this.transDiscount:""
         },{
             header: WtfGlobal.getLocaleText("acc.invoice.proTax"),//"Product Tax",
             dataIndex:"prtaxid",
             id:this.id+"prtaxid",
             fixed:true,
             width:150,
             hidden:this.inputValue==Wtf.CNDN_TYPE_FOR_MALAYSIA?true:!(this.isCustomer?(Wtf.account.companyAccountPref.unitPriceInSR || this.isNoteAlso):(Wtf.account.companyAccountPref.unitPriceInPR || this.isNoteAlso)),//!(this.isNoteAlso || (Wtf.account.companyAccountPref.countryid == '137' && Wtf.account.companyAccountPref.enableGST)),// this.isNoteAlso flag will be true if you are creating Sale/Purchase Return with Credit/Debit Note also// hide if company is malaysian and GST is not enabled for it
             renderer:Wtf.comboBoxRenderer(this.transTax),
             editor:(this.readOnly)?"":this.transTax  //this.transTax
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridTaxAmount"),//"Tax Amount",
             dataIndex:"taxamount",
             id:this.id+"taxamount",
//              fixed:true,
             //align:'right',
             width:150,
             editor:"",//this.transTaxAmount,
             hidden:!(this.isCustomer?(Wtf.account.companyAccountPref.unitPriceInSR || this.isNoteAlso):(Wtf.account.companyAccountPref.unitPriceInPR || this.isNoteAlso)),//!(this.isNoteAlso || (Wtf.account.companyAccountPref.countryid == '137' && Wtf.account.companyAccountPref.enableGST)),// this.isNoteAlso flag will be true if you are creating Sale/Purchase Return with Credit/Debit Note also// hide if company is malaysian and GST is not enabled for it
             renderer:this.setTaxAmountWithotExchangeRate.createDelegate(this)
            },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridAmount"), // "Amount",
             dataIndex:"amount",
             align:'right',
             width:200,
             renderer:(this.isNote?WtfGlobal.withoutRateCurrencySymbol:this.calAmountWithoutExchangeRate.createDelegate(this)),
             hidden: !(this.isCustomer?(Wtf.account.companyAccountPref.unitPriceInSR || this.isNoteAlso):(Wtf.account.companyAccountPref.unitPriceInPR || this.isNoteAlso))//!(Wtf.account.companyAccountPref.unitPriceConfiguration || this.isNoteAlso)// this.isNoteAlso flag will be true if you are creating Sale/Purchase Return with Credit/Debit Note also
            },{
             header:WtfGlobal.getLocaleText("acc.masterConfig.29"),   //location
             dataIndex:'reason',
//             hidden:!(Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel),                 
             width:150,
             renderer:Wtf.comboBoxRenderer(this.reason),
             editor:(this.readOnly)?"":this.reason
         },{
                header:WtfGlobal.getLocaleText("acc.field.Remarks"),  //"Remark",
                dataIndex:"remark",
                editor:(this.readOnly)?"":this.Description=new Wtf.form.TextArea({
                    maxLength:200,
                    allowBlank: true,
                    xtype:'textarea'
                })
        });
        if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.invoicegrid.TaxAmount"),//"Total Tax Amount",
                dataIndex:"recTermAmount",
                align:'right',
                width:100,
                renderer: function (value, m, rec) {
                    var hideUnitPriceAmount = this.isCustomer ? !Wtf.dispalyUnitPriceAmountInSales : !Wtf.dispalyUnitPriceAmountInPurchase;
                    return WtfGlobal.withoutRateCurrencySymbolWithPermissionCheck(value, m, rec, hideUnitPriceAmount);
                }.createDelegate(this)
            },{
                header:WtfGlobal.getLocaleText("acc.invoicegrid.TaxAmount"),//"Other Term Non Taxable Amount",
                dataIndex:"OtherTermNonTaxableAmount",
                hidden :  true
            },{
                header: WtfGlobal.getLocaleText("acc.invoicegrid.tax"), 
                align: 'center',
                dataIndex:"LineTermdetails",
                width: 40,
                renderer: this.addRenderer.createDelegate(this)
                //hidden:  (Wtf.Countryid != Wtf.Country.INDIA) 
            });
        }
        if (CompanyPreferenceChecks.displayUOMCheck()) {
            columnArr.push({
                header: WtfGlobal.getLocaleText("acc.product.displayUoMLabel"),
                width: 140,
                align: 'center',
                dataIndex: 'displayuomvalue',
                renderer: WtfGlobal.displayUoMRenderer(this.productComboStore, "productid", "displayUoMName", this.store)
            });
        }
        // Add TDS calculation column in grid
        if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isTDSApplicable && this.moduleid==Wtf.Acc_Purchase_Return_ModuleId && this.isNoteAlso) {// TDS is only for INDIA Country and - On this amount TDS is Applied
            columnArr.push({
                header: "TDS Calculation",
                align: 'center',
                width: 40,
                renderer: this.addRendererTDS.createDelegate(this),
                hidden: !Wtf.isTDSApplicable
            });
        }
        if(!this.isNote && !this.readOnly) {
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),//"Action",
                dataIndex:"gridaction",//data index is needed to find column by data index
                align:'center',
                width:40,
                renderer: this.deleteRenderer.createDelegate(this)
            });
        }
        if (CompanyPreferenceChecks.discountMaster() && (this.moduleid == Wtf.Acc_Sales_Return_ModuleId)) {
            columnArr.push({//added the view or edit discount details icon in grid.
                header: WtfGlobal.getLocaleText("acc.discountdetails.title"),
                align: 'center',
                renderer: this.discountdetailsRenderer.createDelegate(this),
//                renderer: function (v, m, rec) {//Icon Renderer for discount details 
//                    return "<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.discountdetails.desc") + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.discountdetails.title") + "' class='" + getButtonIconCls(Wtf.etype.discountdetails) + "'></div>";
//                },
                dataIndex: 'discountdetailswindow',
                id: this.id + 'discountdetailswindow',
                hidden: false,
                width: 200
            });
        }
        this.cm=new Wtf.grid.ColumnModel(columnArr);
        this.sm=this.sModel;
    },
//    conversionFactorRenderer:function(store, valueField, displayField) {
//        return function(value, meta, record) {
//            if(value != "") {
//                value = (parseFloat(getRoundofValue(value)).toFixed(Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT)=="NaN")?parseFloat(0).toFixed(Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT):parseFloat(getRoundofValueWithValues(value,Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT)).toFixed(Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT);
//            }
//            var idx = Wtf.uomStore.find("uomid", record.data["uomid"]);            
//            if(idx == -1)
//                return value;
//            var uomname = Wtf.uomStore.getAt(idx).data["uomname"];
//            idx = store.find(valueField, record.data[valueField]);
//            if(idx == -1)
//                return value;
//            var rec = store.getAt(idx);
//            return "1 "+ uomname +" = "+ +value+" "+rec.data[displayField];
//        }
//    },
     checkSelections:function( scope, rowIndex, keepExisting, record){
        if(rowIndex== (this.store.getCount()-1)){
            return false;
        }else{
            return true;
        }
       
    },
    addProductList:function(){
        return "<div class='pwnd add-gridrow' wtf:qtip=\"Click to add products\"></div>";
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
    serialRenderer:function(v,m,rec){
        return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.serial.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.serial.desc.title")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
    },
    discountdetailsRenderer: function (v, m, rec) {                                  //Icon Renderer for discount details 
        return "<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.discountdetails.desc") + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.discountdetails.title") + "' class='" + getButtonIconCls(Wtf.etype.discountdetails) + "'></div>";
    },
    descriptionRenderer: function(val, meta, rec, row, col, store) {
        var regex = /(<([^>]+)>)/ig;
//        val = val.replace(/(<([^>]+)>)/ig, "");
        if(val!=undefined){
            var tip = val.replace(/"/g, '&rdquo;');
            meta.attr = 'wtf:qtip="' + tip + '"' + '" wtf:qtitle="' + WtfGlobal.getLocaleText("acc.gridproduct.discription") + '"';
            return val;
        }else{
            return "";
        }
    },
    RitchTextBoxSetting: function(grid, rowIndex, columnIndex, e) {
            var v = WtfGlobal.RitchTextBoxSetting(grid, rowIndex, columnIndex, e, this.readOnly);
            return v;
    },
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
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
    quantityRenderer:function(val,m,rec){
        if(val == ""){
            return val;
        }else{
            return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        }
    },
     handleRowClick:function(grid,rowindex,e){
        this.rowIndexForSpecialKey = rowindex; // rowindex
        if(!this.readOnly && e.target.className == "pwndBar2 shiftrowupIcon") {
            moveSelectedRowFormasterItems(grid,0,rowindex);
        }
        if(!this.readOnly && e.target.className == "pwndBar2 shiftrowdownIcon") {
            moveSelectedRowFormasterItems(grid,1,rowindex);
        }
        if(e.getTarget(".delete-gridrow")){
           
                var store = grid.getStore();
                var total = store.getCount();
                var record = store.getAt(rowindex);
                  
               /* Function is used to return ID of linked Document If it is Unlinked through Editing*/
                var lastProductDeleted = isLastProductDeleted(store, record);
                if(lastProductDeleted){
               
                /* Link to  Combo*/
                var linkToComponent = Wtf.getCmp(this.linkTo);
               
                /* Link combo*/
                var linkComponent = Wtf.getCmp(this.link);
                var message = "Link Information of "
                if (linkToComponent) {
                    message += linkToComponent.lastSelectionText + " <b>";
                }

                if (record && record.data && record.data.linkto) {
                    message += record.data.linkto;
                }
                message += "</b> will be Removed. </br>" + WtfGlobal.getLocaleText("acc.nee.48")
                    
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), message, function(btn) {
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
                                        this.parentObj.linkedDocumentId += record.data.linkid + " , ";
                                        arr.remove(record.data.linkid);
                                        component.setValue(arr);
                                    } else if (this.isEdit) {
                                       
                                        /* Reseting Link &  Link to combo if all linked document is getting removed*/
                                        this.parentObj.linkedDocumentId += record.data.linkid + " , ";
                                        arr.remove(record.data.linkid);
                                        component.disable();
                                        linkToComponent.disable();
                                        component.setValue(arr);
                                        linkToComponent.setValue("");
                                        linkComponent.setValue("");
                                        linkComponent.enable();


                                    } else {
                                        arr.remove(record.data.linkid);
                                        component.setValue(arr);
                                    }
                                }
                            }
                        }
                    }
               
                if(record.data.copyquantity!=undefined){
                    
                    var deliveredproqty = record.data.dquantity;
                    deliveredproqty = (deliveredproqty == "NaN" || deliveredproqty == undefined || deliveredproqty == null)?0:deliveredproqty;
                    
                     var deletedData=[];
                     var newRec=new this.deleteRec({
                                productid:record.data.productid,
                                productname:record.data.productname,    
                                productquantity:deliveredproqty,
                                productbaseuomrate:record.data.baseuomrate,
                                productbaseuomquantity:record.data.baseuomquantity,                                
                                productuomid:record.data.uomid,
                                productinvstore:record.data.invstore,
                                productinvlocation:record.data.invlocation,
                                productrate:record.data.rate                                
                            });                            
                            deletedData.push(newRec);
                            this.deleteStore.add(deletedData);                            
                }
                store.remove(store.getAt(rowindex));
                if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
                    updateTermDetails(this);
                }
                if(rowindex==total-1){
                    this.addBlankRow();
                }
                this.fireEvent('datachanged',this);
            }, this);
        }else{
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.nee.48"), function(btn){
                if(btn!="yes") return;
                var store=grid.getStore();
                var total=store.getCount();
                var record = store.getAt(rowindex);
                if(record.data.copyquantity!=undefined){
                    
                    var deliveredproqty = record.data.dquantity;
                    deliveredproqty = (deliveredproqty == "NaN" || deliveredproqty == undefined || deliveredproqty == null)?0:deliveredproqty;
                    
                     var deletedData=[];
                     var newRec=new this.deleteRec({
                                productid:record.data.productid,
                                productname:record.data.productname,    
                                productquantity:deliveredproqty,
                                productbaseuomrate:record.data.baseuomrate,
                                productbaseuomquantity:record.data.baseuomquantity,                                
                                productuomid:record.data.uomid,
                                productinvstore:record.data.invstore,
                                productinvlocation:record.data.invlocation,
                                productrate:record.data.rate                                
                            });                            
                            deletedData.push(newRec);
                            this.deleteStore.add(deletedData);                            
                }
                store.remove(store.getAt(rowindex));
                if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
                    updateTermDetails(this);
                }
                if(rowindex==total-1){
                    this.addBlankRow();
                }
                this.fireEvent('datachanged',this);
            }, this);
        }
        } else if(e.getTarget(".serialNo-gridrow")){
            var store=grid.getStore();
            var record = store.getAt(rowindex);
            var productid = record.get('productid');
            var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, productid, 'productid');
            var linkflag=false;
            if(Wtf.getCmp('linkToOrder'+this.heplmodeid+this.parentid))
            {
                linkflag=Wtf.getCmp('linkToOrder'+this.heplmodeid+this.parentid).getValue();
            }
           record.data.linkflag=linkflag;
                    if(productComboRecIndex==-1){
                productComboRecIndex=WtfGlobal.searchRecordIndex(store, productid, 'productid');
            }
            if(productComboRecIndex >=0){
                var proRecord = this.productComboStore.getAt(productComboRecIndex);
                var recIndex = WtfGlobal.searchRecordIndex(this.productComboStore, productid, 'productid');
                if(recIndex==-1){
                    proRecord=record;
                }
                if(proRecord.data.type!='Service' && proRecord.data.type!='Non-Inventory Part'){  //serial no for only inventory type of product
                    if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.account.companyAccountPref.isLocationCompulsory || Wtf.account.companyAccountPref.isWarehouseCompulsory || Wtf.account.companyAccountPref.isRowCompulsory || Wtf.account.companyAccountPref.isRackCompulsory || Wtf.account.companyAccountPref.isBinCompulsory){ //if company level option is on then only check batch and serial details
                        if(proRecord.data.isLocationForProduct || proRecord.data.isWarehouseForProduct || proRecord.data.isBatchForProduct || proRecord.data.isSerialForProduct || proRecord.data.isRowForProduct || proRecord.data.isRackForProduct || proRecord.data.isBinForProduct || this.moduleid==Wtf.Acc_Sales_Return_ModuleId) 
                         {
                            this.callSerialNoWindow(record);
                        }
                        else{
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.batchserial.Functinality")],2);   //Batch and serial no details are not valid.
                            return;
                        }
                    }
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.batchserial.funforInventoryitems")],2);   //Batch and serial no details are not valid.
                    return;
                }
            }
            
        } else if(e.getTarget(".add-gridrow")){
            if(this.readOnly !=undefined && !this.readOnly){
                if (this.parentObj && this.parentObj.Currency != undefined) {
                    this.forCurrency = this.parentObj.Currency.getValue();
                }
                this.showProductGrid();
            }else{
                return;
            }
        }else if(e.getTarget(".termCalc-gridrow")){
            if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
                this.showTermWindow(grid.getStore().getAt(rowindex),grid,rowindex);
            }else{
                return;
            }
        } else if (e.getTarget(".discountDetails-gridrow")) {
            var store = grid.getStore();
            var record = store.getAt(rowindex);
            var productid = record.get('productid');
//            this.callDiscountDetails(record);                           //calls discount details window in which user can delete the discounts at the time of sales invoice creation which are mapped to product 
            var paramObj = {
                record: record,
                readOnly: this.readOnly,
                parentObj: this.parentObj,
                parentCmpScope: this,
                isLinkedTransaction: this.isLinkedTransaction
            };
            callDiscountDetailsDynamic(paramObj);                           //calls discount details window in which user can delete the discounts at the time of sales invoice creation which are mapped to product 
        } else if (e.getTarget(".tdsCalc-productInvoicegridrow")) { // For Call TDS window
            var gridStoreDetails = grid.getStore();
            var jsonrecord = gridStoreDetails.getAt(rowindex);
            if((jsonrecord.data.islinkingFlag || !Wtf.isEmpty(jsonrecord.data.linkid)) && !Wtf.isEmpty(jsonrecord.data.appliedTDS)){
                this.openWindowForSelectingTDS(jsonrecord, rowindex);
            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "TDS can only be calculated on line item fetched from invoice."], 2);
            }
        }
    },  
    
    afterDeletingDiscount: function (recordArr, record) {
        var jsonArr = createDiscountString(recordArr, record, record.data.rate, record.data.quantity);
        var jsonObj;
        var jsonStr = ""
        if (jsonArr != "" && jsonArr != undefined) {
            jsonObj = {"data": jsonArr};
            jsonStr = JSON.stringify(jsonObj);
        }
        record.set("discountjson", jsonStr);
    },
    
    showProductGrid : function() {
         this.productSelWin = new Wtf.account.ProductSelectionWindow({
            renderTo: document.body,
            height : 600,
            width : 750,
            title:WtfGlobal.getLocaleText("acc.productselection.window.title"),
            layout : 'fit',
            modal : true,
            resizable : false,
            id:this.id+'ProductSelectionWindowSR',
            moduleid:this.moduleid,
            heplmodeid:this.heplmodeid,
            parentCmpID:this.parentCmpID,
            invoiceGrid:this,
            isCustomer : this.isCustomer
        });
        this.productSelWin.show();
    },    
    
    callSerialNoWindow:function(obj){
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
              filterJson+='{"location":"'+productsDefaultLocation+'","warehouse":"'+productsDefaultWarehouse+'","productid":"'+prorec.data.productid+'","documentid":"","purchasebatchid":""},';
                    filterJson=filterJson.substring(0,filterJson.length-1);
             filterJson+="]";
             
      if(islocationavailble || iswarehouseavailble ){ //if salesside and either default location and warehouse  then checkit
           var batchdetails = ""; 
                if((this.isEdit && !this.copyTrans)||obj.data.batchdetails!=""){//if batchdetails is set from option "Set Warehouse/Location"
                    batchdetails = obj.data.batchdetails;
                }else{
                    batchdetails = filterJson;
                }
                  Wtf.Ajax.requestEx({
                    url: "ACCInvoice/getBatchRemainingQuantity.do",
                    params: {
                        batchdetails:batchdetails,
                        transType:this.moduleid,
                        linkflag:obj.data.linkflag,   //ERP-39168 : To display only link DO quantity to Sales Return Serial Window
                        isEdit:this.isEdit
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
         var deliveredprodquantity = obj.data.dquantity;
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
            var isLinkedFromPI = false;
            var isLinkedFromGR = false;
            var isLinkedFromSI = false;
            var isLinkedFromDO = false;
            if (this.parentObj != undefined && this.parentObj.fromLinkCombo.getValue() != undefined) {
                if (!this.isCustomer) {
                    if (this.parentObj.fromLinkCombo.getValue() == "0") {
                        isLinkedFromGR = true;
                    }
                    else if (this.parentObj.fromLinkCombo.getValue() == "1") {
                        isLinkedFromPI = true;
                    }
                }else{
                    if (this.parentObj.fromLinkCombo.getValue() == "0") {
                        isLinkedFromDO = true;
                    }
                    else if (this.parentObj.fromLinkCombo.getValue() == "1") {
                        isLinkedFromSI = true;
                    }
                }
            }
            var documentId = obj.data.rowid;
            if(obj.json && obj.json.isLinkedDoInSI && obj.json.linkedDoId && this.isEdit){
                documentId = obj.json.linkedDoId ;
            }
            this.isOnlyBatchForProduct= !(prorec.data.isRowForProduct || prorec.data.isRackForProduct || prorec.data.isBinForProduct || prorec.data.isSerialForProduct ) &&  prorec.data.isBatchForProduct;          //checking wether the product is only of batch type ERM-319
            this.allowUserToEditQuantity = true;
//            var isQuantityLockedInSOLinkedInSI = false;
//            if (CompanyPreferenceChecks.differentUOM() && isLinkedFromCI && obj.data.islockQuantityflag == true) {
//                isQuantityLockedInSOLinkedInSI = true;
//            }
//            if (!CompanyPreferenceChecks.differentUOM() || !isQuantityLockedInSOLinkedInSI || this.moduleid != Wtf.Acc_Delivery_Order_ModuleId || (CompanyPreferenceChecks.differentUOM() && (isLinkedFromSO && !obj.data.islockQuantityflag))) {
//                allowUserToEditQuantity = true;
//            }
    this.batchDetailswin=new Wtf.account.SerialNoWindow({
            renderTo: document.body,
            title:WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber"),
            productName:prorec.data.productname,
            barcodetype:prorec.data.barcodetype,
            productcode:prorec.data.pid,
            uomName:prorec.data.uomname,
            productUomid:prorec.data.uomid,
            selectedProductUomid:obj.data.uomid,
            quantity:(obj.data.baseuomrate)*(obj.data.dquantity),
            billid:obj.data.billid,
	    defaultLocation:prorec.data.location,
            productid:prorec.data.productid,
            transactionType:(this.isCustomer)?3:2,
            transactionid:(this.isCustomer)?3:2,
            isSales:true,
            isLinkedFromGR:isLinkedFromGR,
            isLinkedFromPI:isLinkedFromPI,
            isLinkedFromDO:isLinkedFromDO,
            isLinkedFromSI:isLinkedFromSI,
            moduleid:this.moduleid,
            defaultAvailbaleQty:this.AvailableQuantity,
            isLocationForProduct:prorec.data.isLocationForProduct,
            isWarehouseForProduct:prorec.data.isWarehouseForProduct,
            isRowForProduct:prorec.data.isRowForProduct,
            isRackForProduct:prorec.data.isRackForProduct,
            isBinForProduct:prorec.data.isBinForProduct,
            isShowStockType:(this.isCustomer)?true:false,
            defaultWarehouse:prorec.data.warehouse,
            documentid:(this.isEdit || obj.data.linkflag) ? documentId:"",
            batchDetails:obj.data.batchdetails,
            warrantyperiod:prorec.data.warrantyperiod,
            warrantyperiodsal:prorec.data.warrantyperiodsal,  
            isBatchForProduct:prorec.data.isBatchForProduct,
            isOnlyBatchForProduct:this.isOnlyBatchForProduct,
            allowUserToEditQuantity:this.allowUserToEditQuantity,        //Passing this parameter to check whether we have to allow user to edit Qty in batch serial window.ERM-319
            isSerialForProduct:prorec.data.isSerialForProduct,
            isSKUForProduct:prorec.data.isSKUForProduct,
            isIsLocWarehouseForProduct:prorec.data.isIsLocWarehouseForProduct,
            linkflag:obj.data.linkflag,
            isEdit:this.isEdit,
            readOnly:this.readOnly,
            copyTrans:this.copyTrans,
            width:950,
            height:400,
            resizable : false,
            modal : true,
            lineRec:obj,
            parentGrid:this
        });
        this.batchDetailswin.on("beforeclose",function(){
            this.batchDetails=this.batchDetailswin.getBatchDetails();
            var isfromSubmit=this.batchDetailswin.isfromSubmit;
                if(isfromSubmit){  //as while clicking on the cancel icon it was adding the reocrd in batchdetail json 
                    obj.set("batchdetails",this.batchDetails);
                }
              },this);
            /*
             * On close event of batch serial window calculating the average base uom quantity and base uom rate and setting it into grid record.
             * after average calculation fireing afteredit event to recalculate the price and amount.
             * ERM-319
             */
            this.batchDetailswin.on("close", function () {
                if (CompanyPreferenceChecks.differentUOM() && this.allowUserToEditQuantity && this.isOnlyBatchForProduct) {
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
                        var receivedQuantity = obj.get("dquantity");
                        obj.set("baseuomrate", getRoundofValue(totalQty / receivedQuantity));
                        obj.set("baseuomquantity", getRoundofValue(totalQty));
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
//    storeRenderer:function(store, valueField, displayField) {
//        return function(value, meta, record) {
//            value=(parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
//            var idx = store.find(valueField, record.data[valueField]);
//            if(idx == -1)
//                return value;
//            var rec = store.getAt(idx);
//            return value+" "+rec.data[displayField];
//        }
//    },
    storeRenderer:function(store, valueField, displayField, gridStore) {
        return function(value, meta, record) {
            value=(parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            var idx = store.find(valueField, record.data[valueField]);
           var rec="";
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
    hideShowCustomizeLineFields:function(){ 
        if(this.moduleid==Wtf.Acc_Sales_Return_ModuleId || this.moduleid==Wtf.Acc_Purchase_Return_ModuleId ){
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
                            if(cm.config[j].header==action.data[i].fieldDataIndex||(cm.config[j].dataIndex==action.data[i].fieldDataIndex && cm.config[j].header==action.data[i].fieldname)){
                                cm.setHidden(j,action.data[i].hidecol);       
                                cm.setEditable(j,!action.data[i].isreadonlycol);
                                if( action.data[i].fieldlabeltext!=null && action.data[i].fieldlabeltext!=undefined && action.data[i].fieldlabeltext!=""){
                                    cm.setColumnHeader(j,action.data[i].fieldlabeltext);
                                }
                            }
                        }
                    }
                    if (this.isCustomer) {
                        if (!Wtf.account.companyAccountPref.unitPriceInSR) { //SDP-2533
                            cm.setHidden(cm.findColumnIndex("rate"), true);
                            cm.setHidden(cm.findColumnIndex("amount"), true);
                            cm.setHidden(cm.findColumnIndex("discountispercent"), true);
                            cm.setHidden(cm.findColumnIndex("prdiscount"), true);
                            cm.setHidden(cm.findColumnIndex("prtaxid"), true);
                            cm.setHidden(cm.findColumnIndex("taxamount"), true);
                        }
                    } else {
                        if (!Wtf.account.companyAccountPref.unitPriceInPR) { //SDP-2533
                            cm.setHidden(cm.findColumnIndex("rate"), true);
                            cm.setHidden(cm.findColumnIndex("amount"), true);
                            cm.setHidden(cm.findColumnIndex("discountispercent"), true);
                            cm.setHidden(cm.findColumnIndex("prdiscount"), true);
                            cm.setHidden(cm.findColumnIndex("prtaxid"), true);
                            cm.setHidden(cm.findColumnIndex("taxamount"), true);
                        }
                    }
                    this.reconfigure( this.store, cm);
                } else {
                     var cm=this.getColumnModel();
                     if (this.isCustomer) {
                        if (!Wtf.account.companyAccountPref.unitPriceInSR) {//SDP-2533
                            cm.setHidden(cm.findColumnIndex("rate"), true);
                            cm.setHidden(cm.findColumnIndex("amount"), true);
                            cm.setHidden(cm.findColumnIndex("discountispercent"), true);
                            cm.setHidden(cm.findColumnIndex("prdiscount"), true);
                            cm.setHidden(cm.findColumnIndex("prtaxid"), true);
                            cm.setHidden(cm.findColumnIndex("taxamount"), true);
                        }
                        else {
                            cm.setHidden(cm.findColumnIndex("rate"), false);
                            cm.setHidden(cm.findColumnIndex("amount"), false);
                            cm.setHidden(cm.findColumnIndex("discountispercent"), false);
                            cm.setHidden(cm.findColumnIndex("prdiscount"), false);
                            var includeProTax  = (this.parentObj.includeProTax !=undefined && this.parentObj.includeProTax.getRawValue()!='' && this.parentObj.includeProTax.getRawValue()=='No')?false:true;
                            if (includeProTax != undefined && flag) {
                                cm.setHidden(cm.findColumnIndex("prtaxid"), false);
                                cm.setHidden(cm.findColumnIndex("taxamount"), false);
                            }
                        }
                    }else{
                        if (!Wtf.account.companyAccountPref.unitPriceInPR) {//SDP-2533
                            cm.setHidden(cm.findColumnIndex("rate"), true);
                            cm.setHidden(cm.findColumnIndex("amount"), true);
                            cm.setHidden(cm.findColumnIndex("discountispercent"), true);
                            cm.setHidden(cm.findColumnIndex("prdiscount"), true);
                            cm.setHidden(cm.findColumnIndex("prtaxid"), true);
                            cm.setHidden(cm.findColumnIndex("taxamount"), true);
                        }
                        else {
                            cm.setHidden(cm.findColumnIndex("rate"), false);
                            cm.setHidden(cm.findColumnIndex("amount"), false);
                            cm.setHidden(cm.findColumnIndex("discountispercent"), false);
                            cm.setHidden(cm.findColumnIndex("prdiscount"), false);
                            var includeProTax  = (this.parentObj.includeProTax !=undefined && this.parentObj.includeProTax.getRawValue()!='' && this.parentObj.includeProTax.getRawValue()=='No')?false:true;                                                                                                          
                            if (includeProTax  != undefined && flag) {
                                cm.setHidden(cm.findColumnIndex("prtaxid"), false);
                                cm.setHidden(cm.findColumnIndex("taxamount"), false);
                            }
                        }
                    }
                    this.reconfigure( this.store, cm);
//                    Wtf.Msg.alert('Status', action.msg);
                }
            },function() {
            });
        
        }
    },
    updateRow:function(obj){
        Wtf.uomStore.clearFilter();
        if(obj!=null){
            var rowRateIncludingGstAmountIndex=this.getColumnModel().findColumnIndex("rateIncludingGst");
            this.productComboStore.clearFilter(); // Issue 22189
            var rec=obj.record;
            
            /**
             * if islinkingFlag true then price for that product will not be refresh or recalulate
             * only in linking case of any document. 
             */
             if (this.isEdit && this.parentObj != undefined && this.parentObj.PO.getValue() != undefined && this.parentObj.PO.getValue() != "") {
                rec.set('islinkingFlag', true);
            }
            
            /*----Code is written for adding product from window at line level----------*/
            if (obj.isAddProductsFromWindow != undefined && obj.isAddProductsFromWindow && CompanyPreferenceChecks.mapTaxesAtProductLevel()) {

                /* -------------Set Product tax if Product is mapped with any tax--------------- */
                this.showMappedProductTax(rec);

            }
            
            var islinkingFlag = (rec.data.islinkingFlag!=undefined && rec.data.islinkingFlag!="") ?rec.data.islinkingFlag:false;
            var proqty = obj.record.get("quantity");
            proqty = (proqty == "NaN" || proqty == undefined || proqty == null)?0:proqty;
            
            var deliveredproqty = obj.record.get("dquantity");
            deliveredproqty = (deliveredproqty == "NaN" || deliveredproqty == undefined || deliveredproqty == null)?0:deliveredproqty;
           
           if((deliveredproqty == 0) && obj.field == "quantity" && Wtf.account.companyAccountPref.autoPopulateDeliveredQuantity){
                obj.record.set("dquantity", proqty);   // ERP-13968 Auto populate the delivery quantity when Actual is entered
                if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) {
                    if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                        this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                    } else {
                        this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                    }
                }
            }
            
            if(obj.field=="baseuomrate"){
                if(this.isCustomer)
                    rec.set("changedQuantity",(rec.data.copybaseuomrate-obj.value)*((rec.data.copyquantity==="" || rec.data.copyquantity==undefined)?1:rec.data.copyquantity));
                else
                    rec.set("changedQuantity",(obj.value-rec.data.copybaseuomrate)*((rec.data.copyquantity==="" || rec.data.copyquantity==undefined)?1:rec.data.copyquantity));
                
                  var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
                  var productuomid = "",rate=0;
                  if(productComboIndex >=0){
                      var datewiseprice=0;
                      prorec = this.productComboStore.getAt(productComboIndex);
                      productuomid = prorec.data.uomid;
                       if(this.UomSchemaType==Wtf.UOMSchema){//for Schema type
                            WtfGlobal.setAjaxTimeOut();
                        Wtf.Ajax.requestEx({
                            url:"ACCProductCMN/getIndividualProductPrice.do",
                            params:{
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
                            WtfGlobal.resetAjaxTimeOut();
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
                                obj.record.set("baseuomquantity", deliveredproqty*obj.value);
                                if (prorec.data.displayUoMid != undefined && prorec.data.displayUoMid != "" && prorec.data.displayUoMid != null){
                                        obj.record.set("displayuomvalue", (deliveredproqty * obj.record.get("baseuomrate")) / obj.record.get("displayuomrate"));
                                } else {
                                    obj.record.set("displayuomvalue", "");
                                }
                                rate = getRoundofValueWithValues(modifiedRate * obj.value, Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                                modifiedRate = getRoundofValueWithValues(modifiedRate, Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                                obj.record.set("rate", isPriceMappedToUOM ? modifiedRate : rate);
                            }else {
                                obj.record.set("baseuomrate", 1);
                            }
                            if (obj.record.data.productCode) {
                                obj.record.set("pid", obj.record.data.productCode);
                            }
                               WtfGlobal.setDefaultWarehouseLocation(obj, rec,true);
                            this.fireEvent('datachanged',this);
                        }, function(){

                            });
                    }
//                    if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
//                        obj.record.set("baseuomquantity", quantity*obj.value);
//                        obj.record.set("rate", modifiedRate*obj.value);
//                    } else {
//                        obj.record.set("baseuomrate", 1);
//                    }                      
                }else{
                       productuomid = rec.data.uomid;
                      if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                            obj.record.set("baseuomquantity", deliveredproqty*obj.value);
                                WtfGlobal.setDefaultWarehouseLocation(obj, rec,true);
                      } else {
                          obj.record.set("baseuomrate", 1);
                  }
                  }
//                  this.fireEvent('datachanged',this);
            }
            if(obj.field=="uomid"){
                  var prorec = null;
                  var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
                  var productuomid = "";
                  if(productComboIndex >=0){
                      prorec = this.productComboStore.getAt(productComboIndex);
                      productuomid = prorec.data.uomid;
                     
                           var baseuomrate =1,rateperuom=0,isPriceMappedToUOM=false;
                          //To do - Need to take rate from new window
//                      this.showPriceWindow.createDelegate(this,[rec, obj],true);
                            var uomschemaid="";
                          if(this.UomSchemaType==Wtf.UOMSchema){//for Schema type
                              if(productuomid != obj.value){
                            uomschemaid=prorec.data.uomschematypeid; 
                        } 
                                    Wtf.Ajax.requestEx({
                                    url:"ACCProductCMN/getIndividualProductPrice.do",
                                    params:{
                                    uomschematypeid:uomschemaid,
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
                                    uomid: rec.data.uomid,
                                    skipRichTextArea:true
                                    }
                                    }, this,function(response){
                                        baseuomrate =response.data[0].baseuomrate;
                                        rateperuom =response.data[0].rateperuom;
                                        datewiseprice =response.data[0].price;
                                        this.defaultPrice=datewiseprice;
                                        obj.record.set("baseuomrate", baseuomrate);
                                        obj.record.set("baseuomquantity", deliveredproqty*baseuomrate);
                                        var displayuomid =  obj.record.get("displayUoMid");
                                        var displayuomtest = obj.record.get("displayuomrate");
                                        if (displayuomtest != "NaN" && displayuomtest != undefined && displayuomtest != "" && displayuomtest != null && displayuomid != undefined && displayuomid != "" && displayuomid != null)
                                            obj.record.set("displayuomvalue", (deliveredproqty*baseuomrate)/obj.record.get("displayuomrate"));
                                        else
                                            obj.record.set("displayuomvalue","")
                                        var modifiedRate = this.defaultPrice;
                                       /**
                                         * No need to change unit price in foreign currency if price already present in 
                                         * foreign currency. 
                                         */
                                         if (this.isbandPriceNotAvailable != undefined && this.isbandPriceNotAvailable) {
                                             modifiedRate = WtfGlobal.getIndividualProductPriceInMultiCurrency(rec, datewiseprice);
                                        }
                                        if (response.data[0].isPriceMappedToUOM != undefined && response.data[0].isPriceMappedToUOM != "") {
                                            isPriceMappedToUOM = response.data[0].isPriceMappedToUOM;
                                        }
                                        if(productuomid == obj.value){
                                            obj.record.set("baseuomrate", 1);
                                            obj.record.set("baseuomquantity", deliveredproqty*obj.record.get("baseuomrate"));
                                            obj.record.set("rate",modifiedRate*baseuomrate); 
                                            obj.record.set("rateIncludingGst", modifiedRate*baseuomrate);
                                        }else{
                                            obj.record.set("rate",isPriceMappedToUOM?modifiedRate:modifiedRate*baseuomrate);
                                            obj.record.set("rateIncludingGst", isPriceMappedToUOM?modifiedRate:modifiedRate*baseuomrate);
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
                                                    obj.record.set("taxamount",taxamount);
                                            }
                                            obj.record.set("isUserModifiedTaxAmount", false);
                                        }
                                        if (obj.record.data.productCode) {
                                            obj.record.set("pid", obj.record.data.productCode);
                                        }
                                            WtfGlobal.setDefaultWarehouseLocation(obj, rec,true);
                                            this.parentObj.updateSubtotal();
                                    }, function(){

                                    });
//                               } else {
//                                    obj.record.set("baseuomrate", 1);
//                                    obj.record.set("baseuomquantity", deliveredproqty*obj.record.get("baseuomrate"));
//                               }     
                        }else{ //for packeging UOM type
                            Wtf.Ajax.requestEx({
                                    url:"ACCProduct/getIndividualProductPrice.do",
                                    params:{
                                        productid:prorec.data.productid,
                                        affecteduser: this.affecteduser,
                                        transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                                        forCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency:"",
                                        currency: this.parentObj.Currency.getValue(),
                                        quantity: obj.record.data.dquantity,
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
                                        obj.record.set("baseuomquantity", (deliveredproqty)*(prorec.data.caseuomvalue));
                                        obj.record.set("baseuomrate", (prorec.data.caseuomvalue));
                                        obj.record.set("rate", modifiedRate*(prorec.data.caseuomvalue));
                                    } else if(obj.value == prorec.data.inneruom) {
                                        obj.record.set("baseuomquantity", (deliveredproqty)*(prorec.data.inneruomvalue));
                                        obj.record.set("baseuomrate", (prorec.data.inneruomvalue));
                                        obj.record.set("rate", modifiedRate*(prorec.data.inneruomvalue));
                                    } else {
                                        obj.record.set("baseuomrate", 1);
                                        obj.record.set("baseuomquantity", deliveredproqty*1);
                                        obj.record.set("rate", modifiedRate);
                                    }
                                        WtfGlobal.setDefaultWarehouseLocation(obj, rec,true);
                            
                            
                              }, function(){

                             });
                          }  
//                } else if (this.productOptimizedFlag != undefined) {
//                    productuomid = rec.data.uomid;
//                    if (productuomid != obj.value) {
//                        //To do - Need to take rate from new window
//                        //                      this.showPriceWindow.createDelegate(this,[rec, obj],true);
//                        obj.record.set("baseuomquantity", deliveredproqty * obj.record.get("baseuomrate"));
//                    } else {
//                        obj.record.set("baseuomrate", 1);
//                        obj.record.set("baseuomquantity", deliveredproqty * obj.record.get("baseuomrate"));
//                    }
//                    WtfGlobal.setDefaultWarehouseLocation(obj, rec, true);
                  }else if (this.productOptimizedFlag != undefined) {
                      /**
                       * Below code is copied from invoicegrid.js, when user
                       * select the uom the request of getIndividualProductPrice should be hit so as to 
                       * get the uom conversion factors etc.
                       */
                    productuomid = rec.data.baseuomid;

                    var selectedUOMRec = WtfGlobal.searchRecord(Wtf.uomStore, obj.value, 'uomid');

                    obj.record.set("uomname", selectedUOMRec.data['uomname']);
                    obj.record.set("isAnotherUOMSelected", false);


                    //To do - Need to take rate from new window
                    //                      this.showPriceWindow.createDelegate(this,[rec, obj],true);
                    var uomschemaid;
                    if (this.UomSchemaType == Wtf.UOMSchema) {//for Schema type
                        if (productuomid != obj.value) {
                            uomschemaid = rec.data.uomschematypeid;
                        }

                        Wtf.Ajax.requestEx({
                            url: "ACCProductCMN/getIndividualProductPrice.do",
                            params: {
                                displayUoMid: rec.data.displayUoMid,
                                uomschematypeid: uomschemaid,
                                productId: rec.data.productid,
                                startdate: WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true)),
                                enddate: WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false)),
                                currentuomid: obj.value,
                                carryin: (this.isCustomer) ? false : true,
                                productid: rec.data.productid,
                                affecteduser: this.affecteduser,
                                forCurrency: Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency : "",
                                currency: this.parentObj.Currency.getValue(),
                                quantity: obj.record.data.quantity,
                                transactiondate: WtfGlobal.convertToGenericDate(this.billDate),
                                uomid: rec.data.uomid,
                                skipRichTextArea: true
                            }
                        }, this, function (response) {
                            baseuomrate = response.data[0].baseuomrate;
                            var displayuomrate1 = response.data[0].displayuomrate;
                            rateperuom = response.data[0].rateperuom;
                            datewiseprice = response.data[0].price;

                            var availableQtyInSelectedUOM = response.data[0].availableQtyInSelectedUOM;
                            var pocountinselecteduom = response.data[0].pocountinselecteduom;
                            var socountinselecteduom = response.data[0].socountinselecteduom;
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
                                obj.record.set("displayuomvalue", (displayuomrate1 == 1) ? rec.data.quantity : (baseuomrate * rec.data.quantity) / displayuomrate1);
                            else
                                obj.record.set("displayuomvalue", "");
                            obj.record.set("baseuomquantity", rec.data.quantity * baseuomrate);
                            obj.record.set("availableQtyInSelectedUOM", availableQtyInSelectedUOM);
                            obj.record.set("isAnotherUOMSelected", true);
                            obj.record.set("pocountinselecteduom", pocountinselecteduom);
                            obj.record.set("socountinselecteduom", socountinselecteduom);
                            var modifiedRate;

                            modifiedRate = WtfGlobal.getIndividualProductPriceInMultiCurrency(rec, datewiseprice);

                            if (productuomid == obj.value) {
                                obj.record.set("baseuomrate", 1);
                                obj.record.set("baseuomquantity", record.data.quantity * obj.record.get("baseuomrate"));
                                obj.record.set("rate", modifiedRate * baseuomrate);
                                obj.record.set("rateIncludingGst", modifiedRate * baseuomrate);
                            } else {
                                obj.record.set("rate", isPriceMappedToUOM ? modifiedRate : modifiedRate * baseuomrate);
                                obj.record.set("rateIncludingGst", isPriceMappedToUOM ? modifiedRate : modifiedRate * baseuomrate);
                            }
                            if (baseuomrate != 1) {
                                obj.record.set("rate", isPriceMappedToUOM ? modifiedRate : modifiedRate * rec.data.quantity * baseuomrate);
                                obj.record.set("rateIncludingGst", isPriceMappedToUOM ? modifiedRate : modifiedRate * rec.data.quantity * baseuomrate);
                            }
                            WtfGlobal.setDefaultWarehouseLocation(obj, rec, false);
                            this.fireEvent('datachanged', this);
                        }, function () {

                        });
                        //                            } else {
                        //                                obj.record.set("baseuomrate", 1);
                        //                                obj.record.set("baseuomquantity", quantity*obj.record.get("baseuomrate"));
                        //                            }
                    } else {//for packeging UOM type
                        Wtf.Ajax.requestEx({
                            url: "ACCProduct/getIndividualProductPrice.do",
                            params: {
                                productid: prorec.data.productid,
                                affecteduser: this.affecteduser,
                                forCurrency: Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency : "",
                                currency: this.parentObj.Currency.getValue(),
                                quantity: obj.record.data.quantity,
                                transactiondate: WtfGlobal.convertToGenericDate(this.billDate),
                                carryin: (this.isCustomer) ? false : true
                            }
                        }, this, function (response) {
                            var datewiseprice = response.data[0].price;
                            if (!Wtf.account.companyAccountPref.productPriceinMultipleCurrency) { //If product in Multiple currency is not set in account preferences
                                var rate = ((obj.record == undefined || obj.record.data['currencyrate'] == undefined || obj.record.data['currencyrate'] == "") ? 1 : obj.record.data['currencyrate']);
                                var oldcurrencyrate = ((obj.record == undefined || obj.record.data['oldcurrencyrate'] == undefined || obj.record.data['oldcurrencyrate'] == "") ? 1 : obj.record.data['oldcurrencyrate']);
                                var modifiedRate;
                                if (rate != 0.0)
                                    modifiedRate = getRoundofValueWithValues(((parseFloat(datewiseprice) * parseFloat(rate)) / parseFloat(oldcurrencyrate)), Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                                else
                                    modifiedRate = getRoundofValueWithValues((parseFloat(datewiseprice) / parseFloat(oldcurrencyrate)), Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                            } else {
                                modifiedRate = datewiseprice;
                            }
                            if (obj.value == prorec.data.caseuom) {
                                obj.record.set("baseuomquantity", (rec.data.quantity) * (prorec.data.caseuomvalue));
                                obj.record.set("baseuomrate", (prorec.data.caseuomvalue));
                                obj.record.set("rate", modifiedRate * prorec.data.caseuomvalue);
                            } else if (obj.value == prorec.data.inneruom) {
                                obj.record.set("baseuomquantity", (rec.data.quantity) * (prorec.data.inneruomvalue));
                                obj.record.set("baseuomrate", (prorec.data.inneruomvalue));
                                obj.record.set("rate", modifiedRate * prorec.data.inneruomvalue);
                            } else {
                                obj.record.set("baseuomrate", 1);
                                obj.record.set("baseuomquantity", rec.data.quantity * 1);
                                obj.record.set("rate", modifiedRate);
                            }
                            WtfGlobal.setDefaultWarehouseLocation(obj, rec, false);
                            this.fireEvent('datachanged', this);
                        }, function () {

                        });
                    }
                    WtfGlobal.setDefaultWarehouseLocation(obj, rec, true);
                }
//                  this.fireEvent('datachanged',this);
            }
            if(obj.field=="productid" || obj.field=="pid"){
                rec=obj.record;
                var customFieldArr = GlobalColumnModelForProduct[this.moduleid];
                if (customFieldArr != null && customFieldArr != undefined) {
                    for (var k = 0; k < customFieldArr.length; k++) {
                        rec.set(customFieldArr[k].fieldname, "");
                    }
                }
                var productid="";
                var index=this.productComboStore.find('productid',obj.value);
                

                if(this.isCustomer)
                    rec.set("changedQuantity",(proqty*(-1))*((rec.data.baseuomrate===""||rec.data.baseuomrate==undefined)?1:rec.data.baseuomrate));
                else
                    rec.set("changedQuantity",(proqty)*((rec.data.baseuomrate===""||rec.data.baseuomrate==undefined)?1:rec.data.baseuomrate));
                
                var prorec = this.productComboStore.getAt(index);
                if(!this.isGST && Wtf.account.companyAccountPref.isLineLevelTermFlag){
                    if(!Wtf.isEmpty(prorec.data['LineTermdetails'])){                   
                        var termStore = this.getTaxJsonOfIndia(prorec);
                        termStore = this.calculateTermLevelTaxes(termStore, rec);
                        rec.set('LineTermdetails',JSON.stringify(termStore));
                        updateTermDetails(this);
                    }
                } else if (this.isGST) {
                    if (this.isModuleForAvalara) {
                        getTaxFromAvalaraAndUpdateGrid(this, obj);
                    } 
                   else {
                        /**
                         * ERP-32829 
                         * code for New GST 
                         */
                        this.parentObj.ignoreHistory=true;  // In return case ignore history if product selected.
                        var extraparams = {};
                        extraparams.isProductIDSelect = true;
                        processGSTRequest(this.parentObj, this, prorec.data.productid,extraparams);
                    }
                } 
                if(index>=0){
                     var baseuomRate = 1;
                    var baseuomquantity = 1;
                    rec=this.productComboStore.getAt(index);
                    obj.record.set("description",rec.data["desc"]);
                    obj.record.set("type",rec.data["type"]);
                   
                    /*--------If tax mapped at Product level then no need to set tax again here As already tax has been set ---------- */
                    if (!(CompanyPreferenceChecks.mapTaxesAtProductLevel() && obj.record.data.prtaxid != undefined && obj.record.data.prtaxid != "")) {
                        obj.record.set("prtaxid", "");
                    }                    
                    obj.record.set("taxamount","");
//                    obj.record.set("quantity",1);
                    obj.record.set("baseuomquantity",1);
                    obj.record.set("baseuomrate",1);
                    obj.record.set("uomid", rec.data["uomid"]);
                    obj.record.set("baseuomname", rec.data['uomname']);
                    obj.record.set("displayUoMid", rec.data['displayUoMid']);
                    obj.record.set("displayUoMName", rec.data['displayUoMName']);
                    obj.record.set("displayuomvalue", "");
                    obj.record.set("displayuomrate", rec.data['displayuomrate']);
                    obj.record.set("invlocation", rec.data["location"]);
                    obj.record.set("invstore", rec.data["warehouse"]);
                    obj.record.set("isLocationForProduct", rec.data['isLocationForProduct']);
                    obj.record.set("isWarehouseForProduct", rec.data['isWarehouseForProduct']);
                    obj.record.set("isBatchForProduct", rec.data['isBatchForProduct']);
                    obj.record.set("isSerialForProduct", rec.data['isSerialForProduct']);
                    obj.record.set("isFromVendorConsign", rec.data['isFromVendorConsign']);
                    obj.record.set("isRowForProduct", rec.data['isRowForProduct']);
                    obj.record.set("isRackForProduct", rec.data['isRackForProduct']);
                    obj.record.set("isBinForProduct", rec.data['isBinForProduct']);  
                    obj.record.set("location", rec.data["location"]);
                    obj.record.set("warehouse", rec.data["warehouse"]);
                    obj.record.set("multiuom", rec.data['multiuom']);
                    obj.record.set("baseuomrate", 1);
                    if (this.UomSchemaType == Wtf.UOMSchema) {//for Schema type
                        productuomid = prorec.data.uomid;
                        obj.record.set("baseuomid", prorec.data.uomid);
                        obj.record.set("baseuomname", prorec.data.uomname);
                        obj.record.set("displayUoMid", prorec.data.displayUoMid);
                        obj.record.set("displayUoMName", prorec.data.displayUoMName);
                        obj.record.set("uomschematypeid", prorec.data.uomschematypeid);
                    } else {//for packeging UOM type
                        productuomid = this.isCustomer ? prorec.data.salesuom : prorec.data.purchaseuom;
                        //                          productuomname =this.isCustomer? prorec.data.salesuomname:prorec.data.purchaseuomname;
                        baseuomRate = this.isCustomer ? prorec.data.stocksalesuomvalue : prorec.data.stockpurchaseuomvalue;
                        if (obj.record.get("uomid") != undefined && productuomid != prorec.data.uomid) {
                            baseuomquantity = rec.data.quantity * baseuomRate;
                            obj.record.set("baseuomquantity", baseuomquantity);
                            obj.record.set("baseuomrate", (baseuomRate));
                            obj.record.set("displayuomrate", response.data[0].displayuomrate);
                        } else {
                            obj.record.set("baseuomquantity", rec.data.quantity);
                            obj.record.set("baseuomrate", 1);
                        }
                        obj.record.set("caseuom", prorec.data['caseuom']);
                        obj.record.set("inneruom", prorec.data['inneruom']);
                        obj.record.set("stockuom", prorec.data['uomid']);
                        obj.record.set("caseuomvalue", prorec.data['caseuomvalue']);
                        obj.record.set("inneruomvalue", prorec.data['inneruomvalue']);
                    }
                    if(Wtf.account.companyAccountPref.calculateproductweightmeasurment){
                      obj.record.set("productweightperstockuom", rec.data['productweightperstockuom']);
                      obj.record.set("productweightincludingpakagingperstockuom", rec.data['productweightincludingpakagingperstockuom']);  
                      obj.record.set("productvolumeperstockuom", rec.data['productvolumeperstockuom']);
                      obj.record.set("productvolumeincludingpakagingperstockuom", rec.data['productvolumeincludingpakagingperstockuom']); 
                    } else {
                      obj.record.set("productweightperstockuom", 0);
                      obj.record.set("productweightincludingpakagingperstockuom", 0);
                      obj.record.set("productvolumeperstockuom", 0);
                      obj.record.set("productvolumeincludingpakagingperstockuom", 0);
                    }
                    if(this.isCustomer){
                        obj.record.set("rate",rec.data["salespricedatewise"]);
                    } else {
                        obj.record.set("rate",rec.data["purchasepricedatewise"]);
                    }
                }else if(this.productOptimizedFlag!= undefined){                 
                    if(this.productOptimizedFlag==Wtf.Products_on_Submit){
                        productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.value.trim(), 'pid');
                        if(productComboIndex >=0){
                            rec = this.productComboStore.getAt(productComboIndex);
                            productid=rec.data.productid;
                            obj.record.set("productid",productid);
                        }
                    }   
                    obj.record.set("description",rec.data["desc"]);
                    obj.record.set("type",rec.data["type"]);
                    obj.record.set("prtaxid", "");
                    obj.record.set("taxamount","");
                    obj.record.set("baseuomquantity",1);
                    obj.record.set("baseuomrate",1);
                    obj.record.set("baseuomname", rec.data['uomname']);
                    obj.record.set("displayUoMid", rec.data['displayUoMid']);
                    obj.record.set("displayUoMName", rec.data['displayUoMName']);
                    obj.record.set("displayuomvalue", "");
                    obj.record.set("displayuomrate", rec.data['displayuomrate']);
                    obj.record.set("uomid", rec.data["uomid"]);
                    obj.record.set("invlocation", rec.data["location"]);
                    obj.record.set("invstore", rec.data["warehouse"]);
                    obj.record.set("isLocationForProduct", rec.data['isLocationForProduct']);
                    obj.record.set("isWarehouseForProduct", rec.data['isWarehouseForProduct']);
                    obj.record.set("isBatchForProduct", rec.data['isBatchForProduct']);
                    obj.record.set("isSerialForProduct", rec.data['isSerialForProduct']);
                    obj.record.set("isFromVendorConsign", rec.data['isFromVendorConsign']);
                    obj.record.set("isRowForProduct", rec.data['isRowForProduct']);
                    obj.record.set("isRackForProduct", rec.data['isRackForProduct']);
                    obj.record.set("isBinForProduct", rec.data['isBinForProduct']);  
                    obj.record.set("location", rec.data["location"]);
                    obj.record.set("warehouse", rec.data["warehouse"]);
                    obj.record.set("multiuom", rec.data['multiuom']);
                    if(Wtf.account.companyAccountPref.calculateproductweightmeasurment){
                      obj.record.set("productweightperstockuom", rec.data['productweightperstockuom']);
                      obj.record.set("productweightincludingpakagingperstockuom", rec.data['productweightincludingpakagingperstockuom']);  
                      obj.record.set("productvolumeperstockuom", rec.data['productvolumeperstockuom']);
                      obj.record.set("productvolumeincludingpakagingperstockuom", rec.data['productvolumeincludingpakagingperstockuom']);
                    } else {
                      obj.record.set("productweightperstockuom", 0);
                      obj.record.set("productweightincludingpakagingperstockuom", 0);
                      obj.record.set("productvolumeperstockuom", 0);
                      obj.record.set("productvolumeincludingpakagingperstockuom", 0);
                    }
                    if(this.isCustomer)
                        obj.record.set("rate",rec.data["salespricedatewise"]);
                    else
                        obj.record.set("rate",rec.data["purchasepricedatewise"]);
                }
             if(this.parentObj && this.parentObj.Currency != undefined){
                    this.forCurrency=this.parentObj.Currency.getValue();
                }    
          Wtf.Ajax.requestEx({
                    url:"ACCProduct/getIndividualProductPrice.do",
                    params:{
                        productid:this.productOptimizedFlag==Wtf.Products_on_Submit?productid:obj.value,
                        affecteduser: this.affecteduser,
                        transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                        forCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency:"",
                        currency: this.parentObj.Currency.getValue(),
                        quantity: obj.record.data.dquantity,
                        carryin : (this.isCustomer)? false : true,
                        skipRichTextArea:true
                    }
                }, this,function(response){
                    var datewiseprice =response.data[0].price;
                    var producttype = response.data[0].producttype;
                    this.isPriceListBand = response.data[0].isPriceListBand;
                    this.isVolumeDisocunt = response.data[0].isVolumeDisocunt;
                    this.priceSource = response.data[0].priceSource;
                    this.pricingbandmasterid=response.data[0].pricingbandmasterid;
                    this.isPriceFromUseDiscount = response.data[0].isPriceFromUseDiscount;
                    this.priceSourceUseDiscount = response.data[0].priceSourceUseDiscount;
                    this.defaultPrice = datewiseprice;
                    var discountType = response.data[0].discountType;
                    var discountValue = response.data[0].discountValue;
                    this.isBandPriceConvertedFromBaseCurrency = response.data[0].isBandPriceConvertedFromBaseCurrency;
                    this.isbandPriceNotAvailable = response.data[0].isbandPriceNotAvailable;
                    
                    /**
                     * below code creates a json of multiple discounts mapped to specific product and sets in record(discountjson)
                     */
                    if (CompanyPreferenceChecks.discountMaster()) {
                        var jsonArr = createDiscountString(response, obj.record, this.defaultPrice, obj.record.data.quantity);
                        var jsonObj;
                        if (jsonArr != "" && jsonArr != undefined) {
                            jsonObj = {"data": jsonArr};
                        }
                        var jsonStr = JSON.stringify(jsonObj);
                        obj.record.set("discountjson", jsonStr);
                    } else {
                        obj.record.set("discountjson", "");
                    }
                    
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
                        for (var i = 1; i < response.data.length; i++) {
                            var dataObj = response.data[i];
                            var key = dataObj.key;
                            for (var k = 0; k < obj.grid.colModel.config.length; k++) {
                                if (obj.grid.colModel.config[k].dataIndex == key) {
                                    var editor = obj.grid.colModel.config[k].editor;
                                    if (editor && editor.field.store) {
                                        var store = editor.field.store;
                                        if (store)
                                            store.clearFilter();
                                    }
                                    obj.record.set(key, dataObj[key]);
                                }
                            }
                        }
                    }
                    productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.value, 'productid');
                    if (this.productOptimizedFlag != undefined && this.productOptimizedFlag == Wtf.Products_on_Submit) {
                        productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.value.trim(), 'pid');
                    } else if (productComboIndex == -1) {
                        productComboIndex = 1;
                    }
                    var productname = "";
                    var proddescription = "";
                    var productuomid = undefined;
                    var productsuppliernumber = "";
                    var shelfLocation = "";
                    var baseuomRate=1;
                    var prorec = null;
                    var acctaxcode = (this.isCustomer)?"salesacctaxcode":"purchaseacctaxcode";
                    var protaxcode = "";
                    var productLocation = "";
                    var productWarehouse = "";
                    var productpid="";
                    if(productComboIndex >=0){
                        prorec = this.productComboStore.getAt(productComboIndex);
                        productname = prorec.data.productname;
                        proddescription = prorec.data.desc;
                        proddescription = prorec.data.desc;
                        if(this.UomSchemaType==Wtf.UOMSchema){//for Schema type
                          productuomid = prorec.data.uomid;
                        } else {//for packeging UOM type
                            productuomid =this.isCustomer? prorec.data.salesuom:prorec.data.purchaseuom;
    //                          productuomname =this.isCustomer? prorec.data.salesuomname:prorec.data.purchaseuomname;
                            baseuomRate=this.isCustomer? prorec.data.stocksalesuomvalue:prorec.data.stockpurchaseuomvalue;
                            if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                                    obj.record.set("baseuomquantity", deliveredproqty*(baseuomRate));
                                    obj.record.set("baseuomrate", (baseuomRate));
                            } else {
                                    obj.record.set("baseuomquantity", deliveredproqty);
                                    obj.record.set("baseuomrate", 1);
                            } 
                            obj.record.set("caseuom", prorec.data['caseuom']);
                            obj.record.set("inneruom", prorec.data['inneruom']);
                            obj.record.set("caseuomvalue", prorec.data['caseuomvalue']);
                            obj.record.set("inneruomvalue", prorec.data['inneruomvalue']);
                        }
                        productsuppliernumber= prorec.data.supplierpartnumber;
                        shelfLocation = prorec.data.shelfLocation;
                        protaxcode = prorec.data[acctaxcode];
                        productLocation = prorec.data.location;
                        productWarehouse = prorec.data.warehouse;
                        productpid = prorec.data.pid;
                    }
                    obj.record.set("desc",proddescription);
                    obj.record.set("uomid", productuomid);
                    obj.record.set("supplierpartnumber",productsuppliernumber);
                    obj.record.set("shelfLocation",shelfLocation);
                    obj.record.set("invlocation",productLocation);
                    obj.record.set("invstore", productWarehouse);
//                    obj.record.set("pid", productpid);
                    obj.record.set("productname",productname);
                     if (producttype == Wtf.producttype.service && proqty == "" && deliveredproqty == "") {
                        obj.record.set("quantity", 1);
                        obj.record.set("dquantity", 1);
                    }
                   if (this.isVolumeDisocunt) {
                    if (obj.record.data.dquantity != "") {
                                obj.record.set("rate", this.defaultPrice);
                        obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSource);
                    } else {
                        obj.record.set("rate", "");
                        obj.record.set("priceSource", "");
                    }
                } else if (this.isPriceListBand) {
                    if (this.isPriceFromUseDiscount) {
                        if (obj.record.data.dquantity != "") {
                                obj.record.set("rate", this.defaultPrice);
                            obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSourceUseDiscount + ": " + WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                        } else {
                            obj.record.set("rate", "");
                            obj.record.set("priceSource", "");
                        }
                    } else {
                            obj.record.set("rate", this.defaultPrice);
                        obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                    }
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
                    if (datewiseprice == 0) {
                            if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.addprice) && !this.isRequisition) { // permissions
                                rec.set("productname",productname);
                                //                            if (Wtf.account.companyAccountPref.unitPriceConfiguration) {
                                //                                Wtf.Msg.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pricefortheproduct") + " <b>" + productname + "</b> " + ' ' + WtfGlobal.getLocaleText("acc.field.isnotsetDoyouwanttosetitnow"),
                                //                                    this.showPriceWindow.createDelegate(this,[rec, obj],true), this);
                                //                            }
                                //                            if (!Wtf.account.companyAccountPref.unitPriceConfiguration) { // if Wtf.account.companyAccountPref.unitPriceConfiguration is off and rate for that product is not set then it will be zero by default.
                                obj.record.set("rate", 0);
                            //                            } else {
                            //                                obj.record.set("rate", "");
                            //                            }
                            } else {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pricefortheproduct")+" <b>"+productname+"</b>"+' '+WtfGlobal.getLocaleText("acc.field.isnotset")], 2);
                            }
                        } else {
                            // setting datewise price according to currency exchange rate - 
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
                            
                            if (this.isPriceFromUseDiscount) {
                                if (obj.record.data.dquantity != "") {
                                    obj.record.set("rate", this.defaultPrice);
                                    obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSourceUseDiscount);
                                } else {
                                    obj.record.set("rate", "");
                                    obj.record.set("priceSource", "");
                                }
                            } else {
                                if(this.UomSchemaType==Wtf.UOMSchema){//Need to Discuss with sir
                                    obj.record.set("rate", modifiedRate);
                                    obj.record.set("rateIncludingGst", modifiedRate);
                                }else{
                                    obj.record.set("rate", modifiedRate*baseuomRate);
                                    obj.record.set("rateIncludingGst", modifiedRate*baseuomRate);
                                }
                            }
                        }
                        // alert if band price not available
                        if (this.isbandPriceNotAvailable) {
                            if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.DefaultUnitPriceIsnotsetPriceband")], 2);
                            } else{
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.bandPriceNotAvailable.msg")], 2);
                            }
                        }
                }
                    /**
                     * Recalculate Tax after price set to product in case of NewGST
                     */
                    if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                        obj.record.set('recTermAmount', 0);
                        if (obj.record.get('LineTermdetails') != undefined && obj.record.get('LineTermdetails') != '') {
                            var termStore = this.getTaxJsonOfIndia(obj.record);
                            if (this.parentObj && this.parentObj.includingGST && this.parentObj.includingGST.getValue() == true) {
                                this.getColumnModel().setRenderer(this.getColumnModel().findColumnIndex("amount"), WtfGlobal.withoutRateCurrencySymbol);
                                termStore = this.calculateTermLevelTaxesInclusive(termStore, obj.record);
                            } else {
                                this.getColumnModel().setRenderer(this.getColumnModel().findColumnIndex("amount"), this.calAmountWithoutExchangeRate.createDelegate(this));
                                termStore = this.calculateTermLevelTaxes(termStore, obj.record, undefined, true);
                            }
                            obj.record.set('LineTermdetails', Wtf.encode(termStore));
                            updateTermDetails(this);
                        }
                    }
                    if (this.isModuleForAvalara && this.isGST) {
                        getTaxFromAvalaraAndUpdateGrid(this, obj);
                    }
                this.fireEvent('datachanged',this);
                }, function(){
                });
            }else if(obj.field=="quantity"){
                rec=obj.record;               
                //if((rec.data.isNewRecord=="" || rec.data.isNewRecord==undefined) && this.fromOrder&&!(this.editTransaction||this.copyInv)) {
                if(((rec.data.isNewRecord=="" || rec.data.isNewRecord==undefined) && this.fromOrder&&!(this.editTransaction||this.copyInv))||(this.isEdit && rec.data.linkid !="")) {  
                   if(obj.value >rec.data.copyquantity){  
                       var msg="";
                       /*
                        * Prompt is not required for view case
                        */
                       if(!this.isViewCNDN){
                            if(this.inputValue==Wtf.CNDN_TYPE_FOR_MALAYSIA || this.inputValue==Wtf.NoteForOvercharge){
                                msg=!this.isCustomer?WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinCNisexceeds"):WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinDNisexceeds");
                            }else{
                                msg=this.isCustomer?WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinSRisexceeds"):WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinPRisexceeds");
                            }
                        }
                        obj.record.set(obj.field, obj.originalValue);
                        
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);

                   }else if(obj.value!=rec.data.copyquantity) {
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"),(this.isCustomer)?WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinSRisdifferent"):WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinPRisdifferent"),function(btn){
                            if(btn!="yes") {
                                obj.record.set(obj.field, obj.originalValue);
//                                obj.record.set("baseuomquantity",obj.originalValue*obj.record.get("baseuomrate"));
                        }else{
//                                    obj.record.set("dquantity", obj.value);    // to avoid link entry problem( if created vendor Invoice and in PR Change the actual qty then it shows warning msg and if we click on yes then it should not load changes qty in Deliv qty)
                                    rec.set("changedQuantity",(rec.data.copyquantity-obj.value)*((rec.data.baseuomrate===""||rec.data.baseuomrate==undefined)?1:rec.data.baseuomrate));
                        }
                        },this)
                    }
                }
                if((proqty)==0){
                    this.store.remove(obj.record);
                }
//                  var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
//                  var productuomid = "";
//                  if(productComboIndex >=0){
//                      prorec = this.productComboStore.getAt(productComboIndex);
//                      productuomid = prorec.data.uomid;
//                      if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
//                            obj.record.set("baseuomquantity", proqty*obj.record.get("baseuomrate"));
//                      } else {
//                          obj.record.set("baseuomrate", 1);
//                          obj.record.set("baseuomquantity", proqty*obj.record.get("baseuomrate"));
//                      }
//                  }
                   if(deliveredproqty!=""){
                         if(deliveredproqty > proqty && this.inputValue!=Wtf.CNDN_TYPE_FOR_MALAYSIA && this.inputValue!=Wtf.NoteForOvercharge){
                            var msg = WtfGlobal.getLocaleText("acc.field.Returnquantityshouldnotbegreater");
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg], 2);
                            obj.record.set("dquantity", proqty);
                            obj.record.set("baseuomquantity", proqty * obj.record.get("baseuomrate"));
                    
                            if ((!this.isCustomer && Wtf.account.companyAccountPref.productPricingOnBands) || (this.isCustomer && Wtf.account.companyAccountPref.productPricingOnBandsForSales)) {
                                Wtf.Ajax.requestEx({
                                    url:"ACCProduct/getIndividualProductPrice.do",
                                    params: {
                                        productid: obj.record.data.productid,
                                        affecteduser: this.affecteduser,
                                        forCurrency: Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency : "",
                                        currency: this.parentObj.Currency.getValue(),
                                        quantity: obj.record.data.dquantity,
                                        transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                                        carryin : (this.isCustomer)? false : true,
                                        pricingbandmaster:obj.record.data.pricingbandmasterid
                                    }
                                }, this,function(response) {
                                    var datewiseprice =response.data[0].price;
                                    this.isPriceListBand = response.data[0].isPriceListBand;
                                    this.isVolumeDisocunt = response.data[0].isVolumeDisocunt;
                                    this.priceSource = response.data[0].priceSource;
                                    this.pricingbandmasterid=response.data[0].pricingbandmasterid;
                                    this.isPriceFromUseDiscount = response.data[0].isPriceFromUseDiscount;
                                    this.priceSourceUseDiscount = response.data[0].priceSourceUseDiscount;
                                    this.defaultPrice = datewiseprice;
                                    this.purchaseprice=response.data[0].purchaseprice;
                                    this.isVolumeDisocuntExist = response.data[0].isVolumeDisocuntExist;
                                    this.isBandPriceConvertedFromBaseCurrency = response.data[0].isBandPriceConvertedFromBaseCurrency;
                                    this.isbandPriceNotAvailable = response.data[0].isbandPriceNotAvailable;
                                    
                                /*
                                 * set band of customer on product selection
                                 */
                                obj.record.set("pricingbandmasterid", this.pricingbandmasterid);
                                    if (this.isVolumeDisocunt) {
                                        if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) {
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
                                    } else if (this.isPriceListBand) {
                                           obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                                           obj.record.set("rate", this.defaultPrice);
                                        if (this.isPriceFromUseDiscount) {
                                            if (obj.record.data.dquantity != "") {
                                                if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) {
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
                                            } else {
                                                obj.record.set("rate", "");
                                                obj.record.set("priceSource", "");
                                            }
                                        } else {
                                            if (this.isVolumeDisocuntExist) {
                                                if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) {
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
                                            }
                                        }
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
                                            if (obj.record.data.dquantity != "") {
                                                obj.record.set("rate", this.defaultPrice);
                                                obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSourceUseDiscount);
                                            } else {
                                                obj.record.set("rate", "");
                                                obj.record.set("priceSource", "");
                                            }
                                        } else {
                                            if (this.isVolumeDisocuntExist) {
                                                obj.record.set("rate", this.defaultPrice);
                                                obj.record.set("priceSource", "");
                                            }
                                        if (this.isPriceListBand) {
                                            obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                                        } else {
                                          if (this.inputValue != Wtf.CNDN_TYPE_FOR_MALAYSIA && this.inputValue!=Wtf.NoteForOvercharge) {
                                            obj.record.set("priceSource", "");
                                            obj.record.set("rate", this.defaultPrice);
                                          }
                                        }
                            }
                                        // alert if band price not available
                                        if (this.isbandPriceNotAvailable) {
                                            if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.DefaultUnitPriceIsnotsetPriceband")], 2);
                                            } else{
                                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.bandPriceNotAvailable.msg")], 2);
                                            }
                                        }
                                    }
                                    this.fireEvent('datachanged',this);
                                }, function(response) {

                                });
                            }
                         } 
                   }         
//                    obj.record.set("invstore", this.batchDetailswin.defaultLocation);
//                    obj.record.set("invlocation", this.batchDetailswin.defaultWarehouse);
//                
                var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
                if (productComboRecIndex >= 0) {
                    var proRecord = this.productComboStore.getAt(productComboRecIndex);
                    if(proRecord==undefined){
                        proRecord= obj.record;
                    }  
                    if (proRecord.data.type != 'Service' && proRecord.data.type != 'Non-Inventory Part') { // serial no for only inventory type of product
                        if(proRecord.data.isSerialForProduct) {
                            var v = obj.record.data.quantity;
                            v = String(v);
                            var ps = v.split('.');
                            var sub = ps[1];
                            if (sub!=undefined && sub.length > 0) {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.exceptionMsgForDecimalQty")], 2);
                                obj.record.set("quantity", obj.originalValue);
                                obj.record.set("baseuomquantity", obj.originalValue*obj.record.get("baseuomrate"));
                            }
                        }
                         if(Wtf.account.companyAccountPref.autoPopulateDeliveredQuantity && !proRecord.data.isBatchForProduct && !proRecord.data.isSerialForProduct){
                             WtfGlobal.setDefaultWarehouseLocation(obj,proRecord,true);
                    }
                }
               }
            } else if((obj.field=="dquantity" || obj.field=="pricingbandmasterid")){
                rec=obj.record;
                /**
                  * if there is linking case then populate the unit price from parent document and price will not be recalculate/refresh if Quantity is changed.
                  * But if user selects prce band from combo-box the price will be refresh according to price in price band -->as per action item in  SDP-14464.
                  */
                if (((!this.isCustomer && Wtf.account.companyAccountPref.productPricingOnBands) || (this.isCustomer && Wtf.account.companyAccountPref.productPricingOnBandsForSales))&& (obj.field=="pricingbandmasterid" || !islinkingFlag)) {
                    Wtf.Ajax.requestEx({
                        url:"ACCProduct/getIndividualProductPrice.do",
                        params: {
                            productid: obj.record.data.productid,
                            affecteduser: this.affecteduser,
                            forCurrency: Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency : "",
                            currency: this.parentObj.Currency.getValue(),
                            quantity: obj.record.data.dquantity,
                            transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                            carryin : (this.isCustomer)? false : true,
                            pricingbandmaster:obj.record.data.pricingbandmasterid,
                            uomid: rec.data.uomid
                        }
                    }, this,function(response) {
                        var datewiseprice =response.data[0].price ? response.data[0].price : 0;
                        this.isPriceListBand = response.data[0].isPriceListBand;
                        this.isVolumeDisocunt = response.data[0].isVolumeDisocunt;
                        this.priceSource = response.data[0].priceSource ? response.data[0].priceSource : "";
                        this.pricingbandmasterid=response.data[0].pricingbandmasterid ? response.data[0].pricingbandmasterid : "";
                        this.isPriceFromUseDiscount = response.data[0].isPriceFromUseDiscount;
                        this.priceSourceUseDiscount = response.data[0].priceSourceUseDiscount;
                        this.defaultPrice = datewiseprice;
                        this.isVolumeDisocuntExist = response.data[0].isVolumeDisocuntExist;
                        this.isBandPriceConvertedFromBaseCurrency = response.data[0].isBandPriceConvertedFromBaseCurrency;
                        this.isbandPriceNotAvailable = response.data[0].isbandPriceNotAvailable;
                        baseuomrate =(response.data[0].baseuomrate!=undefined && response.data[0].baseuomrate!="")?response.data[0].baseuomrate:1;
                        /**
                                 * below code creates a json of multiple discounts mapped to specific product and sets in record(discountjson)
                                 */
                                if (CompanyPreferenceChecks.discountMaster()) {
                                    var jsonArr = createDiscountString(response, obj.record, this.defaultPrice, obj.record.data.dquantity);
                                    var jsonObj;
                                    if (jsonArr != "" && jsonArr != undefined) {
                                        jsonObj = {"data": jsonArr};
                                    }
                                    var jsonStr = JSON.stringify(jsonObj);
                                    obj.record.set("discountjson", jsonStr);
                                } else {
                                    obj.record.set("discountjson", "");
                                }
                        /**
                         * Checking if price is mapped to UOM if yes the setting the unit price which is set in db else converting it as rule set in UOM schema.
                         * ERM-389 / ERP-35140
                         */
                        var isPriceMappedToUOM = false,modifiedRate=this.defaultPrice;
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
                        /*
                         * set band of customer on product selection
                         */
                        obj.record.set("pricingbandmasterid", this.pricingbandmasterid);
                        if (this.isVolumeDisocunt) {
                            if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) {
                                obj.record.set("rateIncludingGst", this.defaultPrice);
                                if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                                    this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                                } else {
                                    this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                                }
                            }else {
                                obj.record.set("rate", this.defaultPrice);
                            }
                            obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSource);
                        } else if (this.isPriceListBand) {
                               obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                               /**
                                * Checking if price is mapped to UOM if yes the setting the unit price which is set in db else converting it as rule set in UOM schema.
                                * ERM-389 / ERP-35140
                                */
                            obj.record.set("rate", isPriceMappedToUOM ? this.defaultPrice : modifiedRate * baseuomrate);
                            if (this.isPriceFromUseDiscount) {
                                if (obj.record.data.dquantity != "") {
                                    if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) {
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
                                } else {
                                    obj.record.set("rate", "");
                                    obj.record.set("priceSource", "");
                                }
                            } else {
                                if (this.isVolumeDisocuntExist) {
                                    if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) {
                                        obj.record.set("rateIncludingGst", this.defaultPrice);
                                        if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                                            this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                                        } else {
                                            this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                                        }
                                    }else {
                                        obj.record.set("rate", this.defaultPrice);
                                    }
                                    obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                                }
                            }
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
                                if (obj.record.data.dquantity != "") {
                                    if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) {
                                        obj.record.set("rateIncludingGst", this.defaultPrice);
                                        if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                                            this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                                        } else {
                                            this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                                        }
                                    }else {
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
                                if (this.isVolumeDisocuntExist) {
                                    if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) {
                                        obj.record.set("rateIncludingGst", this.defaultPrice);
                                        if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                                            this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                                        } else {
                                            this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                                        }
                                    }else {
                                        obj.record.set("rate", this.defaultPrice);
                                    }
                                    obj.record.set("priceSource", "");
                                }
                                if (this.isPriceListBand) {
                                    obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                                } else {
                                    obj.record.set("priceSource", "");
                                    obj.record.set("rate", isPriceMappedToUOM ? this.defaultPrice : modifiedRate * baseuomrate);
                                }
                            }
                            // alert if band price not available
                            if (this.isbandPriceNotAvailable) {
                                if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.DefaultUnitPriceIsnotsetPriceband")], 2);
                                } else{
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.bandPriceNotAvailable.msg")], 2);
                                }
                            }
                        }
                        this.fireEvent('datachanged',this);
                    }, function(response) {
                        
                    });
                       
                }
                 productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
                 productuomid = "";
                  if(productComboIndex >=0){
                      prorec = this.productComboStore.getAt(productComboIndex);
                      productuomid = prorec.data.uomid;
                      if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                            obj.record.set("baseuomquantity", deliveredproqty*obj.record.get("baseuomrate"));
                      } else {
                        obj.record.set("baseuomrate", 1);
                        obj.record.set("baseuomquantity", deliveredproqty * obj.record.get("baseuomrate"));
                        var displayuomid = obj.record.get("displayUoMid");
                        var displayuomtest = obj.record.get("displayuomrate");
                        if (displayuomtest != "NaN" && displayuomtest != undefined && displayuomtest != "" && displayuomtest != null && displayuomid != undefined && displayuomid != "" && displayuomid != null)
                            obj.record.set("displayuomvalue", (deliveredproqty * obj.record.get("baseuomrate")) / obj.record.get("displayuomrate"));
                        else
                            obj.record.set("displayuomvalue", "")
                      }
                  }else if(this.productOptimizedFlag!= undefined){
                      prorec = obj.record;
                      productuomid = prorec.data.uomid;
                      if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                            obj.record.set("baseuomquantity", deliveredproqty*obj.record.get("baseuomrate"));
                      } else {
                          obj.record.set("baseuomrate", 1);
                          obj.record.set("baseuomquantity", deliveredproqty*obj.record.get("baseuomrate"));
                  }
                      
                  }
                if(!this.isCustomer)
                    rec.set("changedQuantity",(rec.data.copyquantity-obj.value)*((rec.data.copybaseuomrate===""||rec.data.copybaseuomrate==undefined)?1:rec.data.copybaseuomrate));
                else
                    rec.set("changedQuantity",(obj.value-rec.data.copyquantity)*((rec.data.copybaseuomrate===""||rec.data.copybaseuomrate==undefined)?1:rec.data.copybaseuomrate));
                
                if(deliveredproqty > proqty && this.inputValue!=Wtf.CNDN_TYPE_FOR_MALAYSIA && this.inputValue!=Wtf.NoteForOvercharge){
                    var msg = this.inputValue!=Wtf.CNDN_TYPE_FOR_MALAYSIA?WtfGlobal.getLocaleText("acc.field.Returnquantityshouldnotbegreater"):WtfGlobal.getLocaleText("acc.field.quantityshouldnotbegreater");
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg], 2);
                    obj.record.set("dquantity", proqty);
                    obj.record.set("baseuomquantity", proqty*obj.record.get("baseuomrate"));
                    var displayuomid = obj.record.get("displayUoMid");
                    var displayuomtest = obj.record.get("displayuomrate");
                    if (displayuomtest != "NaN" && displayuomtest != undefined && displayuomtest != "" && displayuomtest != null && displayuomid != undefined && displayuomid != "" && displayuomid != null)
                        obj.record.set("displayuomvalue", (proqty * obj.record.get("baseuomrate")) / obj.record.get("displayuomrate"));
                    else
                        obj.record.set("displayuomvalue", "")
                } else if(deliveredproqty <= 0 && !this.allowZeroQuantity){
                    var msg = (this.inputValue!=Wtf.CNDN_TYPE_FOR_MALAYSIA && this.inputValue!=Wtf.NoteForOvercharge)?WtfGlobal.getLocaleText("acc.field.Returnquantityshouldnotbeequalorlessthanzero"):WtfGlobal.getLocaleText("acc.field.quantityshouldnotbeequalorlessthanzero");
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg], 2);
                    obj.record.set("dquantity", proqty);
                    obj.record.set("baseuomquantity", proqty*obj.record.get("baseuomrate"));
                    var displayuomid = obj.record.get("displayUoMid");
                    var displayuomtest = obj.record.get("displayuomrate");
                    if (displayuomtest != "NaN" && displayuomtest != undefined && displayuomtest != "" && displayuomtest != null && displayuomid != undefined && displayuomid != "" && displayuomid != null)
                        obj.record.set("displayuomvalue", (proqty * obj.record.get("baseuomrate")) / obj.record.get("displayuomrate"));
                    else
                        obj.record.set("displayuomvalue", "")
                } 
                /**
                 * ERP-36531
                 * Discount should be calculated in linking case.
                 */
                if (obj.record.data.discountjson != undefined && obj.record.data.discountjson != '' && islinkingFlag) {
                    var jsonObj = JSON.parse(obj.record.data.discountjson);
                    calculateDiscount(jsonObj.data, rec, obj.record.data.rate, obj.record.data.dquantity, true);
                }
                
                var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
                if (productComboRecIndex >= 0) {
                    var proRecord = this.productComboStore.getAt(productComboRecIndex);
                     if(proRecord==undefined){
                        proRecord= obj.record;
                    }  
                    if (proRecord.data.type != 'Service' && proRecord.data.type != 'Non-Inventory Part') { // serial no for only inventory type of product
                        if(proRecord.data.isSerialForProduct) {
                            var v = obj.record.data.dquantity;
                            v = String(v);
                            var ps = v.split('.');
                            var sub = ps[1];
                            if (sub!=undefined && sub.length > 0) {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.exceptionMsgForDecimalQty")], 2);
                                obj.record.set("dquantity", obj.originalValue);
                            }
                        }
                    }
                }
                var docId = obj.record.data.batchdetails !="" ? JSON.parse(obj.record.data.batchdetails)[0].documentid:"";
                WtfGlobal.setDefaultWarehouseLocation(obj, rec,true);
                
                if(islinkingFlag && this.moduleid==Wtf.Acc_Sales_Return_ModuleId) {
                    obj.record.data.batchdetails = obj.record.data.batchdetails.replace('"documentid":""','"documentid":"'+docId+'"');
                }
                    if(!this.isGST && Wtf.account.companyAccountPref.isLineLevelTermFlag){
                        if(rec.get('LineTermdetails') != undefined && rec.get('LineTermdetails') != ''){
                           // var termStore = eval(rec.data['LineTermdetails']);
                           var termStore = this.getTaxJsonOfIndia(rec);
                            termStore = this.calculateTermLevelTaxes(termStore, rec);

                            rec.set('LineTermdetails',JSON.stringify(termStore));
                            updateTermDetails(this);
                        }
                    }else if (this.isGST) {
                        if (this.isModuleForAvalara) {
                            getTaxFromAvalaraAndUpdateGrid(this, obj);
                        }// If terms are already present then calculate only tax values  
                        else if (rec.data.LineTermdetails != ''&& rec.data.LineTermdetails != undefined) {
                        calculateUpdatedTaxes(this.parentObj, this, rec);
                        updateTermDetails(this);
                    }else {
                            /**
                             * ERP-32829 
                             * code for New GST 
                             */
                            processGSTRequest(this.parentObj, this, prorec.data.productid);  
                        }
                    }
                    // If Return Quantity is changed then Re-calculate TDS at respective line level
                    if(Wtf.isTDSApplicable && this.moduleid==Wtf.Acc_Purchase_Return_ModuleId && this.isNoteAlso){
                        this.reCalculateTDSAtLineLevel(rec);
                    }
                }
                /**
                 * When user clicks on unit price or rate including GST then
                 * price band combo should be reset and price source should be manual entry.
                 */
                if ((Wtf.account.companyAccountPref.productPricingOnBands || Wtf.account.companyAccountPref.productPricingOnBandsForSales) && (obj.field == "rateIncludingGst" || obj.field == "rate")) {
                    obj.record.set("priceSource", "Manual Entry");
                    obj.record.set("pricingbandmasterid", "");
                }
                
                //ERP-27270 : allow unit price less than actual unit price in invoice
                if(this.inputValue!=Wtf.CNDN_TYPE_FOR_MALAYSIA && this.inputValue!=Wtf.NoteForOvercharge){
                    if(obj.field=="rate" && this.parentObj && this.parentObj.fromLinkCombo.getValue() == "1"){
                        if(obj.record.data.originallyLoadedRate != null && obj.record.data.originallyLoadedRate != undefined && obj.record.data.originallyLoadedRate != '' && obj.record.data.rate > obj.record.data.originallyLoadedRate){
                            obj.record.set('rate',obj.originalValue);
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.SR.rateCanNotBeGreaterThanOriginalRate")], 2);
                            return;
                        }
                    }
                }
                if(obj.field=="prtaxid" || obj.field=="rate" || obj.field=="dquantity" || obj.field=="quantity"){
                    var taxamount = this.setTaxAmountAfterSelection(obj.record);
                    obj.record.set("taxamount",taxamount);

                    if ((this.getColumnModel().config[rowRateIncludingGstAmountIndex]!=undefined) && (!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden)) {
                        if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                            this.setTaxAndRateAmountAfterIncludingGSTForLineLevelTerms(obj.record);
                        } else {
                            this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                        }
                    }

                    obj.record.set("isUserModifiedTaxAmount", false);
                    this.fireEvent('datachanged',this);
                }
                
                if(obj.field=="rateIncludingGst"){
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
                if (obj.field=="rate" && obj.originalValue != obj.value) {
                    if (!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.addprice) && !this.isRequisition) { // permissions
                        var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.data.productid, 'productid');
                        var productname = "";
                        var prorec = null;
                        if (productComboIndex >= 0) {
                            prorec = this.productComboStore.getAt(productComboIndex);
                            productname = prorec.data.productname;
                        }else if(this.productOptimizedFlag!= undefined && (this.productOptimizedFlag==Wtf.Products_on_type_ahead || this.productOptimizedFlag == Wtf.Show_all_Products)){
                            prorec = obj.record;
                            productname = prorec.data.productname;
                        }
                        rec.set("productname",productname);
                        rec.set("price", obj.value);
                        if (this.isCustomer) {
                            rec.set("customer", this.parentObj.Name.getValue());
                        } else {
                            rec.set("vendor", this.parentObj.Name.getValue());
                        }
                        if (this.isCustomer) {
                            if ((Wtf.account.companyAccountPref.unitPriceInSR || Wtf.account.companyAccountPref.countryid == '137') && Wtf.account.companyAccountPref.priceConfigurationAlert) {
                                Wtf.Msg.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.youHaveChangedThePriceForProduct") + " <b>" + productname + "</b>." + ' ' + WtfGlobal.getLocaleText("acc.field.doYouWantToSaveInPriceMaster"),
                                this.showPriceWindow.createDelegate(this, [rec, obj], true), this);
                            }
                        } else {
                            if ((Wtf.account.companyAccountPref.unitPriceInPR || Wtf.account.companyAccountPref.countryid == '137') && Wtf.account.companyAccountPref.priceConfigurationAlert) {
                                Wtf.Msg.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.youHaveChangedThePriceForProduct") + " <b>" + productname + "</b>." + ' ' + WtfGlobal.getLocaleText("acc.field.doYouWantToSaveInPriceMaster"),
                                this.showPriceWindow.createDelegate(this, [rec, obj], true), this);
                            }
                        }
                    }
                    
                    //Tax Calculation for India
                    if(!this.isGST && Wtf.account.companyAccountPref.isLineLevelTermFlag){
                        if(rec.get('LineTermdetails') != undefined && rec.get('LineTermdetails') != ''){
                            //var termStore = eval(rec.data['LineTermdetails']);
                            var termStore = this.getTaxJsonOfIndia(rec);
                            termStore = this.calculateTermLevelTaxes(termStore, rec);
                            
                            rec.set('LineTermdetails',JSON.stringify(termStore));
                            updateTermDetails(this);
                        }
                    }else if (this.isGST) {
                        if (this.isModuleForAvalara) {
                            getTaxFromAvalaraAndUpdateGrid(this, obj);
                        } // If terms are already present then calculate only tax values 
                        else if (rec.data.LineTermdetails != ''&& rec.data.LineTermdetails != undefined) {
                        calculateUpdatedTaxes(this.parentObj, this, rec);
                        updateTermDetails(this);
                    }else {
                            /**
                             * ERP-32829 
                             * code for New GST 
                             */
                            processGSTRequest(this.parentObj, this, prorec.data.productid);  
                        }
                    }
                    // If Unit Price is changed then Re-calculate TDS at respective line level
                    if(Wtf.isTDSApplicable && this.moduleid==Wtf.Acc_Purchase_Return_ModuleId && this.isNoteAlso){
                        this.reCalculateTDSAtLineLevel(rec);
                    }
                }
                
                if(obj.field=="discountispercent" ||obj.field=="prdiscount"){
                //Tax Calculation for India
                if(!this.isGST && Wtf.account.companyAccountPref.isLineLevelTermFlag){
                    if(rec.get('LineTermdetails') != undefined && rec.get('LineTermdetails') != ''){
                        //var termStore = eval(rec.data['LineTermdetails']);
                        var termStore = this.getTaxJsonOfIndia(rec);
                        termStore = this.calculateTermLevelTaxes(termStore, rec);
                            
                        rec.set('LineTermdetails',JSON.stringify(termStore));
                        updateTermDetails(this);
                    }
                }else if (this.isGST) {
                    if (this.isModuleForAvalara) {
                        getTaxFromAvalaraAndUpdateGrid(this, obj);
                    } // If terms are already present then calculate only tax values  
                    else if (rec.data.LineTermdetails != ''&& rec.data.LineTermdetails != undefined) {
                        calculateUpdatedTaxes(this.parentObj, this, rec);
                        updateTermDetails(this);
                    }else {
                        processGSTRequest(this.parentObj, this, rec.get('productid'));
                    }
                }
                // If Discount is applied/changed then Re-calculate TDS at respective line level
                if(Wtf.isTDSApplicable && this.moduleid==Wtf.Acc_Purchase_Return_ModuleId && this.isNoteAlso){
                    this.reCalculateTDSAtLineLevel(rec);
                }
            }
            /**
             * Below Code checks wether discount value is edited or not if yes then sets the record(discountjson) as empty string
             * as user entered discount value will should be used rather then mapped discount to product
             */
            if (obj.field == "prdiscount" && obj.originalValue != obj.value && rec.data.discountjson != "" && rec.data.discountjson != undefined) {
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.discount.editdiscountpromptmsg"), function (btn) {
                    if (btn == "yes") {
                        obj.record.data.qtipdiscountstr = "";
                        obj.record.data.discountjson = "";
                    } else {
                        rec.set('prdiscount', obj.originalValue);
                    }
                    obj.grid.getView().refresh();
                }, this);
            }
            /**
             * Below Code checks wether discount type is edited or not if yes then sets the record(discountjson) as empty string
             * as user entered discount type will should be used rather then mapped discount to product
             */
            if (obj.field == 'discountispercent' && obj.originalValue != obj.value && rec.data.discountjson != "" && rec.data.discountjson != undefined) {
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.discount.editdiscountpromptmsg"), function (btn) {
                    if (btn == "yes") {
                        obj.record.data.qtipdiscountstr = "";
                        obj.record.data.discountjson = "";
                    } else {
                        rec.set('discountispercent', obj.originalValue);
                    }
                    obj.grid.getView().refresh();
                }, this);
            }

            if ((obj.field == "prdiscount" || obj.field == 'discountispercent') && obj.originalValue != obj.value){
                var taxamount = this.setTaxAmountAfterSelection(obj.record);
                obj.record.set("taxamount",taxamount);
                obj.record.set("isUserModifiedTaxAmount", false);
            }
            
            if (obj.field == "taxamount") {
                /*
                 * If user changed the tax amount manually then isUserModifiedTaxAmount flag made true for Adaptive Rounding Algorith calculataion.
                 * ERM-1085
                 */
                obj.record.set("isUserModifiedTaxAmount", true);
            }
            if (this.parentObj.includeProTax && this.parentObj.includeProTax.getValue()) {
                WtfGlobal.calculateTaxAmountUsingAdaptiveRoundingAlgo(this, false);
            }
        }
        this.fireEvent('datachanged',this);
        if(this.store.getCount()>0&&this.store.getAt(this.store.getCount()-1).data['productid'].length<=0)
            return;
        
        this.addBlankRow();
        
        /* -------------Set Product tax if Product is mapped with any tax--------------- */

        if (CompanyPreferenceChecks.mapTaxesAtProductLevel()) {
            this.showMappedProductTax(rec);

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
     setTaxAmountAfterSelection:function(rec) {
        
        var quantity = rec.data.dquantity;
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
//        var rec=obj.record;
        var discount = 0;
        var rate=getRoundofValueWithValues(rec.data.rate,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        quantity=getRoundofValue(rec.data.dquantity);
        var origionalAmount = getRoundedAmountValue(rate*quantity) ;
//        if(rec.data.partamount != 0){
//            var partamount=getRoundedAmountValue(rec.data.partamount);
//            origionalAmount = getRoundedAmountValue(origionalAmount * (partamount/100));
//        }
        
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
        var val=origionalAmount-discount;
        var taxpercent=0;
        var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
        if(index>=0){
            var taxrec=this.taxStore.getAt(index);
            taxpercent=getRoundedAmountValue(taxrec.data.percent);
        }
        var taxamount= getRoundedAmountValue(val*taxpercent/100);
//        /*
//         * Apply Below formula as per SDP-2727
//         */
//        if (this.parentObj && this.parentObj.fromLinkCombo.getValue() == "1" && rec.data.taxamountforlinking!=undefined) {
//            taxamount = (rec.data.dquantity / rec.data.quantity) * rec.data.taxamountforlinking;
//        }
        return taxamount;
        
    },
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
                    return WtfGlobal.withCurrencyUnitPriceRenderer(v, m, rec);
                    }
                }
    },
    setTaxAmountWithotExchangeRate:function(v,m,rec){
        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)){//When permission not given to display 
            return Wtf.UpriceAndAmountDisplayValue;
        } else {
            var taxamount= 0;
            /*
             * In copy case, deactivated tax not shown.Hence, empty taxid set in record.          
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
    calAmountWithoutExchangeRate:function(v,m,rec){
        
        
        var rate=getRoundofValueWithValues(rec.data.rate,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        var quantity=getRoundofValue(rec.data.dquantity);
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
        
        if(this.parentObj !=undefined && this.parentObj.includingGST && this.parentObj.includingGST.getValue()) {
            rate = rec.data.rateIncludingGst;
        }

        var origionalAmount = getRoundedAmountValue(rate*quantity) ;
//        if(rec.data.partamount != 0){
//            origionalAmount = origionalAmount * (rec.data.partamount/100);
//        }

        /*  When Sales Return is created by linked with Partial Invoice*/
        if (rec.json != undefined && rec.json.partamount != undefined && rec.json.partamount != 0) {

            origionalAmount = getRoundedAmountValue(origionalAmount * (rec.json.partamount / 100));//
           
        }

        var discount = 0;//origionalAmount*rec.data.prdiscount/100   
        if(rec.data.prdiscount > 0) {
            var prdiscount=getRoundedAmountValue(rec.data.prdiscount);
            if(rec.data.discountispercent == 1){
                discount = getRoundedAmountValue((origionalAmount * prdiscount) / 100);
            } else {
                discount = prdiscount;
            }
        }
        
        var val=(origionalAmount)-discount;///rec.data.oldcurrencyrate  
//        rec.set("amountwithouttax",val);
        var taxamount = 0;
        if(rec.data.taxamount){
            taxamount= getRoundedAmountValue(rec.data.taxamount);
        }
        if (this.parentObj != undefined && this.parentObj.includingGST && this.parentObj.includingGST.getValue()) {
            rec.set("amountWithoutTax", (parseFloat(getRoundedAmountValue(val - taxamount)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1));
        } else {
            rec.set("amountWithoutTax", val);
        }

        if (this.parentObj != undefined && this.parentObj.includingGST && !this.parentObj.includingGST.getValue() && Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA) {
            val = parseFloat(val) + parseFloat(taxamount);
        }    

        rec.set("amount",(parseFloat(getRoundedAmountValue(val)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1));
//        if(this.isQuotationFromPR && val!==0 && (rec.data.orignalamount==undefined || rec.data.orignalamount==""))
//        rec.set("orignalamount",val);
        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
            return Wtf.UpriceAndAmountDisplayValue;
        } else{
            return WtfGlobal.withoutRateCurrencySymbol(val,m,rec);
        }
    },
    calAmount:function(v,m,rec){
        var quantity = rec.data.dquantity;
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
        var productRate= (rec.data.rate == 'NaN' || rec.data.rate == undefined || rec.data.rate ==null)?0:rec.data.rate;
        var origionalAmount = productRate * quantity;
        rec.set("amount",origionalAmount);
        return WtfGlobal.withoutRateCurrencySymbol(origionalAmount,m,rec);
    },
    
    calSubtotal:function(){
        var subtotal=0;
        var total=0;
        var count=this.store.getCount();
        for(var i=0;i<count;i++){
            total=(parseFloat(this.store.getAt(i).data['amount'])-parseFloat(this.store.getAt(i).data['taxamount']));
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
    calTaxtotal:function(){
        var subtotal=0;
        var total=0;
        var count=this.store.getCount();
        for(var i=0;i<count;i++){
            total=parseFloat(this.store.getAt(i).data['taxamount']);
            subtotal+=getRoundedAmountValue(total);
        }
        return getRoundedAmountValue(subtotal);
    },
    checkRow:function(obj){
        this.rowIndexForSpecialKey =  obj.row; // rowindex
        var rec=obj.record;
        
        var proqty = obj.record.data['quantity'];
        proqty = (proqty == "NaN" || proqty == undefined || proqty == null)?0:proqty;
        
        var deliveredproqty = obj.record.get("dquantity");
        deliveredproqty = (deliveredproqty == "NaN" || deliveredproqty == undefined || deliveredproqty == null)?0:deliveredproqty;
            
        
        if(obj.field=="uomid"){
            var prorec = null;
            var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
            if(productComboIndex >=0){
                prorec = this.productComboStore.getAt(productComboIndex);
                if(prorec.data.type=='Service'){
//                          WtfComMsgBox(["Warning","UOM can not be set for Service and Non-Inventory products. "], 2);
                    return false;
                } else if(this.UomSchemaType==Wtf.UOMSchema && !prorec.data.multiuom){
//                          WtfComMsgBox(["Warning","Multi UOM not allowed for the selected product. "], 2);
                    return false;
                }
            }else if(this.productOptimizedFlag!= undefined && (this.productOptimizedFlag==Wtf.Products_on_type_ahead || this.productOptimizedFlag == Wtf.Show_all_Products) && prorec==undefined){
                prorec = obj.record;
                if(prorec.data.type=='Service'){
                    //                          WtfComMsgBox(["Warning","UOM can not be set for Service and Non-Inventory products. "], 2);
                    return false;
                } else if(this.UomSchemaType==Wtf.UOMSchema && !prorec.data.multiuom){
                    //                          WtfComMsgBox(["Warning","Multi UOM not allowed for the selected product. "], 2);
                    return false;
            }
            }
        } else if(obj.field=="productid" || obj.field=="pid"){
            var index=this.productComboStore.findBy(function(rec){
                if(obj.field=="productid"?rec.data.productid==obj.value:rec.data.pid==obj.value)
                    return true;
                else
                    return false;
            })
            prorec=this.productComboStore.getAt(index);
             
            if(obj.field=="productid"){
                index=this.productComboStore.find('productid',obj.value)
            }else{
                index=this.productComboStore.find('pid',obj.value)
            }
            if(index!=-1){
                rec=this.productComboStore.getAt(index);
            }
            var useStoreRec=false;
            if(prorec==undefined){
                prorec= rec;
                useStoreRec=true;
            }
            
              //Tax Calculation for India
                if(!this.isGST && Wtf.account.companyAccountPref.isLineLevelTermFlag){
                    if(rec.get('LineTermdetails') != undefined && rec.get('LineTermdetails') != ''){
                        //var termStore = eval(rec.data['LineTermdetails']);
                        var termStore = this.getTaxJsonOfIndia(rec);
                        termStore = this.calculateTermLevelTaxes(termStore, rec);

                        rec.set('LineTermdetails',JSON.stringify(termStore));
                        this.fireEvent('datachanged',this);
                    }
                } else if (this.isGST) {
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
                        processGSTRequest(this.parentObj, this, prorec.data.productid,extraparams);
                    }
                }  
            if(this.inputValue==Wtf.CNDN_TYPE_FOR_MALAYSIA || this.inputValue==Wtf.NoteForOvercharge){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.changeofproduct.here") ], 4);
                obj.cancel=true;   
                return false;
            }
            if(this.isNoteAlso && Wtf.account.companyAccountPref.countryid == Wtf.Country.MALAYSIA && this.fromPO!= undefined && this.fromPO!= null && this.fromPO){  //while linking invoice
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.changeofproduct.here") ], 4);
                obj.cancel=true;   
                return false;
            }
            if(this.store.find("productid",obj.value)>=0&&obj.ckeckProduct==undefined){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.76")+" "+rec.data['productname']], 2);
                    obj.cancel=true;
            }
            if(this.editTransaction){  //In Edit Case Check product quantity is greater than available quantity when selecting product
                var availableQuantity = prorec.data.quantity;    //This is in base UOM
                var copyquantity = 0;
                this.store.each(function(rec){
                    if(rec.data.productid == prorec.data.productid){
                        if(rec.data.copyquantity!=undefined) {
                            copyquantity = copyquantity + (rec.data.copyquantity*rec.data.baseuomrate); 
                        }
                    }
                },this);                
                availableQuantity = availableQuantity + copyquantity;                
                if(!this.isCustomer&&this.store.find("productid",obj.value)>-1 && rec.data.type!='Service' && rec.data.type!='Non-Inventory Part' &&!this.isQuotation){
                    var quantity = 0;
                    this.store.each(function(rec){
                        if(rec.data.productid == obj.value){
                            var ind=this.store.indexOf(rec);
                            if(ind!=-1){
                                if(ind!=obj.row){                            
                                    quantity = quantity + (rec.data.dquantity*rec.data.baseuomrate);                                    
                                }
                            }     
                        }
                    },this);
                    quantity = quantity + (deliveredproqty*obj.record.data['baseuomrate']);
                     if(availableQuantity<quantity){
                        if(Wtf.account.companyAccountPref.negativeStockPR==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninPRareexceedingthequantityavailable")+''+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+WtfGlobal.getLocaleText("acc.field.is")+' '+(availableQuantity-lockQuantity)+'. '+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")], 2);
                          // value for quantity should not be set as 'obj.originalValue'
//                          rec.set("quantity",obj.originalValue);
//                          rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativeStockPR==2){     // Warn Case
                           if(rec.data.isBatchForProduct || rec.data.isSerialForProduct){
                               WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.negativeStockNotAllowedForBatchSerial")], 2);
                               obj.cancel=true;
                               return false;
                           }
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninPRareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",obj.originalValue);
                                  rec.set("dquantity",obj.originalValue);
                                  rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                        }
                    }
                      //WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.76")+" "+rec.data['productname']], 2);
                //obj.cancel=true;
                }else if(!this.isCustomer&&(availableQuantity-(rec.data['lockquantity']))<(deliveredproqty*obj.record.data['baseuomrate'])&& prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation){
                    this.isValidEdit = false;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productname']+' is '+availableQuantity], 2);
                    obj.cancel=true;
                } 
            }
            else{ //In normal Case Check product quantity is greater than available quantity when selecting product
                if(!this.isCustomer&&this.store.find("productid",obj.value)>-1 && rec.data.type!='Service' && rec.data.type!='Non-Inventory Part' &&!this.isQuotation){
                    var quantity = 0;                 
                    this.store.each(function(rec){
                        if(rec.data.productid == obj.value){
                            var ind=this.store.indexOf(rec);
                            if(ind!=-1){
                                if(ind!=obj.row){          
                                    //To do - Need to check this
        //                                    quantity = quantity + (rec.data.dquantity*rec.data.baseuomrate);
                                    quantity = quantity + rec.data.dquantity;
                                }
                            }     
                        }
                    },this);
                    //To do - Need to check this
        //                    quantity = quantity + (obj.record.data['dquantity']*obj.record.data['baseuomrate']);
                    quantity = quantity + deliveredproqty;
                    if(rec.data['quantity']<quantity){
        //                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productname']+' is '+rec.data['quantity']], 2);
        //                        obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativeStockPR==1){ // Block case
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninPRareexceedingthequantityavailable")+''+WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productname']+WtfGlobal.getLocaleText("acc.field.is")+' '+rec.data['quantity']+'. '+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                            obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativeStockPR==2){     // Warn Case
                            if(rec.data.isBatchForProduct || rec.data.isSerialForProduct){
                               WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.negativeStockNotAllowedForBatchSerial")], 2);
                               obj.cancel=true;
                               return false;
                            }
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninPRareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                                if(btn=="yes"){
                                    obj.cancel=false;
                                }else{
//                                    rec.set("quantity",obj.originalValue);
//                                    rec.set("dquantity",obj.originalValue);
//                                    rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
                                    this.store.remove(obj.record);
                                    obj.cancel=true;
                                    return false;
                                }
                            },this); 
                        }
                    }
                //WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.76")+" "+rec.data['productname']], 2);
                //obj.cancel=true;
                }else if(!this.isCustomer&&((rec.data['quantity'])-(rec.data['lockquantity']))<obj.record.data['dquantity']&& prorec.data.type!="Service" && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation){
                    this.isValidEdit = false;
        //                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productname']+' is '+rec.data['quantity']], 2);
        //                    obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativeStockPR==1){ // Block case
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninPRareexceedingthequantityavailable")+''+WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productname']+' '+WtfGlobal.getLocaleText("acc.field.is")+' '+((rec.data['quantity'])-(rec.data['lockquantity']))+'. '+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")], 2);
                            // value for quantity should not be set as 'obj.originalValue'
//                            rec.set("quantity",obj.originalValue);
                            obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativeStockPR==2 && !Wtf.account.companyAccountPref.isnegativestockforlocwar){     // Warn Case
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninPRareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                                if(btn=="yes"){
                                    obj.cancel=false;
                                }else{
//                                    rec.set("quantity",obj.originalValue);
//                                    rec.set("dquantity",obj.originalValue);
//                                    rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
                                    this.store.remove(obj.record);
                                    obj.cancel=true;
                                    return false;
                                }
                            },this); 
                        }  
                }   
            }     
//                  }else if(this.isCustomer&&(obj.field=="dquantity"||obj.field=="baseuomrate")&&obj.record.data['productid'].length>0&&!this.isQuotation){  
            }else if((obj.field=="dquantity"||obj.field=="baseuomrate")&&obj.record.data['productid'].length>0&&!this.isQuotation){  
                if(obj.field=="dquantity") {
                    var originalDquantity = obj.originalValue;
                    var newDquantity = obj.value;
                    var originalBaseuomrate = obj.record.data['baseuomrate'];
                    var newBaseuomrate = obj.record.data['baseuomrate'];
                } else if(obj.field=="baseuomrate") {
                    var originalDquantity = deliveredproqty;
                    var newDquantity = deliveredproqty;
                    var originalBaseuomrate = obj.originalValue;
                    var newBaseuomrate = obj.value;
                }
                prorec=this.productComboStore.getAt(this.productComboStore.find('productid',obj.record.data.productid));
                var useStoreRec=false;
                if(prorec==undefined){
                     prorec= rec;
                     useStoreRec=true;
                    }  
                if(prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part') {
                    var availableQuantity = prorec.data.quantity;
                    var lockQuantity = prorec.data.lockquantity; 
                    if (useStoreRec) {
                        /**
                         * SDP-14258-: prorec.data.availablequantity getting empty so
                         * added undefined and empty check.
                         */
                    availableQuantity = prorec.data.availablequantity==undefined || prorec.data.availablequantity==""?prorec.data.quantity:0;
                    if (this.moduleid == Wtf.Acc_Purchase_Return_ModuleId) {
                        availableQuantity = prorec.data.availableQtyInSelectedUOM==undefined || prorec.data.availableQtyInSelectedUOM==""?prorec.data.quantity:prorec.data.availableQtyInSelectedUOM;
                    }
                    lockQuantity = prorec.data.lockquantity;
                }
                    var quantity = 0;
                    if(this.editTransaction){  //In Edit Case Check product quantity is greater than available quantity when selecting quantity                  
                        var copyquantity = 0;                    
                        this.store.each(function(rec){
                            if(rec.data.productid == prorec.data.productid){
                                copyquantity = copyquantity + (rec.data.copyquantity*rec.data.baseuomrate);
                                var ind=this.store.indexOf(rec);
                                if(ind!=-1){
                                    if(ind!=obj.row){                            
                                        quantity = quantity + (rec.data.dquantity*rec.data.baseuomrate);
                                    }   
                                }                            
                            }
                        },this);
                    quantity = quantity + (newDquantity*newBaseuomrate);
                    if((availableQuantity-lockQuantity) < quantity) {  //for normal check for all products available quantity
                        /*
                         * In copy case Block/Warn messages was not showing if quantity enter greater than available qty.
                         */
                        if (this.editTransaction && !this.copyTrans) {
                            availableQuantity = availableQuantity + copyquantity;
                        }
                    if(!this.isCustomer && ((availableQuantity-lockQuantity) < quantity)) { 
    //                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' is '+availableQuantity], 2);
    //                        obj.cancel=true;                        
                            if(Wtf.account.companyAccountPref.negativeStockPR==1){ // Block case
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninPRareexceedingthequantityavailable")+''+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' '+WtfGlobal.getLocaleText("acc.field.is")+' '+(availableQuantity-lockQuantity)+'. '+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")], 2);
                            rec.set("quantity",originalDquantity);
                            rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                            rec.set("baseuomrate",originalBaseuomrate);
                            obj.cancel=true;   
                            }else if(Wtf.account.companyAccountPref.negativeStockPR==2){     // Warn Case
                            if(rec.data.isBatchForProduct || rec.data.isSerialForProduct){
                               WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.negativeStockNotAllowedForBatchSerial")], 2);
                               obj.cancel=true;
                               return false;
                           }
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninPRareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                                if(btn=="yes"){
                                    obj.cancel=false;
                                }else{
                                    rec.set("quantity",originalDquantity);
                                    rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                                    rec.set("baseuomrate",originalBaseuomrate);
                                    rec.set("dquantity",originalDquantity);
                                    obj.cancel=true;
                                    return false;
                                }
                                },this); //for Ignore Case no any Restriction on user 
                            }
                        }
                    } 
                }else {   //In normal Case Check product quantity is greater than available quantity when selecting quantity                  
                        this.store.each(function(rec){
                            if(rec.data.productid == prorec.data.productid){
                                var ind=this.store.indexOf(rec);
                                if(ind!=-1){
                                    if(ind!=obj.row){
                                        quantity = quantity + (rec.data.dquantity*rec.data.baseuomrate);
                                    }
                                }                               
                            }
                        },this);
                    quantity = quantity + (newDquantity*newBaseuomrate);
                    if(!this.isCustomer && ((availableQuantity-lockQuantity) < quantity)) {  //for normal check for all products available quantity
    //                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' is '+availableQuantity], 2);
    //                        obj.cancel=true;
                            if(Wtf.account.companyAccountPref.negativeStockPR==1){ // Block case
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninPRareexceedingthequantityavailable")+''+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' '+WtfGlobal.getLocaleText("acc.field.is")+' '+(availableQuantity-lockQuantity)+'. '+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")], 2);                      
                            rec.set("quantity",originalDquantity);
                            rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                            rec.set("baseuomrate",originalBaseuomrate);
                            obj.cancel=true;   
                            }else if(Wtf.account.companyAccountPref.negativeStockPR==2){     // Warn Case
                            if(rec.data.isBatchForProduct || rec.data.isSerialForProduct){
                               WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.negativeStockNotAllowedForBatchSerial")], 2);
                               obj.cancel=true;
                               return false;
                           }
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninPRareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                                if(btn=="yes"){
                                    obj.cancel=false;
                                }else{
                                    rec.set("quantity",originalDquantity);
                                    rec.set("dquantity",originalDquantity);
                                    rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                                    rec.set("baseuomrate",originalBaseuomrate);
                                    obj.cancel=true;
                                    return false;
                                }
                                },this); //for Ignore Case no any Restriction on user 
                        }
                    }
                    }

                }
                  //Tax Calculation for India
                if(!this.isGST && Wtf.account.companyAccountPref.isLineLevelTermFlag){
                    if(rec.get('LineTermdetails') != undefined && rec.get('LineTermdetails') != ''){
                        //var termStore = eval(rec.data['LineTermdetails']);
                        var termStore = this.getTaxJsonOfIndia(rec);
                        termStore = this.calculateTermLevelTaxes(termStore, rec);

                        rec.set('LineTermdetails',JSON.stringify(termStore));
                        this.fireEvent('datachanged',this);
                    }
                }else if (this.isGST) {
                    if (this.isModuleForAvalara) {
                        getTaxFromAvalaraAndUpdateGrid(this, obj);
                    } // If terms are already present then calculate only tax values 
                    else if (rec.data.LineTermdetails != '') {
                    calculateUpdatedTaxes(this.parentObj, this, rec);
                    updateTermDetails(this);
                }
                else {
                        /**
                         * ERP-32829 
                         * code for New GST 
                         */
                        processGSTRequest(this.parentObj, this, prorec.data.productid);  
                    }
                }
            }
         },   
            
    addBlankRow:function(){
            
        var Record = this.store.reader.recordType,f = Record.prototype.fields, fi = f.items, fl = f.length;
                var values = {},blankObj={};
                for(var j = 0; j < fl; j++){
                    f = fi[j];
                    if(f.name!='rowid') {
                        blankObj[f.name]='';
                        if(!Wtf.isEmpty(f.defValue))
                            blankObj[f.name]=f.convert((typeof f.defValue == "function"?f.defValue.call():f.defValue));
                    }
                }
                var newrec = new Record(blankObj);
        this.store.add(newrec);       
    },
     
    addBlank:function(){       
        this.addBlankRow();
    },            

    loadPOGridStore: function(rec, linkingFlag,islinkPItoCN) {
        var productids = "";
        this.store.load({params: {bills: rec, mode: 43, closeflag: true, doflag: true, linkingFlag: linkingFlag, moduleid: this.moduleid,islinkPItoCN: islinkPItoCN}});
        this.store.on('load', function(store1,record) {
            for (var count = 0; count < record.length; count++) {
                var rec = record[count];
               
                if (this.moduleid == Wtf.Acc_Sales_Return_ModuleId) {
                    /* Check undefined on notAllowInvoiceToLink or send from java side false */
                    if (rec.json.notAllowInvoiceToLink) {
                        this.parentObj.PO.setValue("");
                        this.store.removeAll();
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.SRfromFirstPartialInvoice") + "<b>" + " " + rec.json.firstInvoiceNumber + "</b>" + " " + WtfGlobal.getLocaleText("acc.field.createdFromSO")], 2);
                        return false;
                    }

                    if (rec.json.partialInvoice) {

                        /* Disabling following fields when DO is linking with Partial Invoice apply check on moduleid DO*/
                        var unitPrice = this.getColumnModel().findColumnIndex("rate")
                        this.getColumnModel().setEditable(unitPrice, false)
                        var discountType = this.getColumnModel().findColumnIndex("discountispercent")
                        this.getColumnModel().setEditable(discountType, false)
                        var discount = this.getColumnModel().findColumnIndex("prdiscount")
                        this.getColumnModel().setEditable(discount, false)
                        var productTax = this.getColumnModel().findColumnIndex("prtaxid")
                        this.getColumnModel().setEditable(productTax, false)
                        var returnQuantity = this.getColumnModel().findColumnIndex("dquantity")
                        this.getColumnModel().setEditable(returnQuantity, false)
                        var actualQuantity = this.getColumnModel().findColumnIndex("quantity")
                        this.getColumnModel().setEditable(actualQuantity, false)
                        /**
                         * Calculate discount while assigning value
                         */
                        if (rec.data.discountjson != undefined && rec.data.discountjson != '' && rec.data.quantity >= rec.data.dquantity) {
                            var jsonObj = JSON.parse(rec.data.discountjson);
                            calculateDiscount(jsonObj.data, rec, rec.data.rate, rec.data.dquantity, true);
                            this.fireEvent('datachanged',this);
                        }

                    } else {

                        /* enabling following fields when DO is linking with Partial Invoice apply check on moduleid DO*/

                        var unitPrice = this.getColumnModel().findColumnIndex("rate")
                        this.getColumnModel().setEditable(unitPrice, true)
                        var discountType = this.getColumnModel().findColumnIndex("discountispercent")
                        this.getColumnModel().setEditable(discountType, true)
                        var discount = this.getColumnModel().findColumnIndex("prdiscount")
                        this.getColumnModel().setEditable(discount, true)
                        var productTax = this.getColumnModel().findColumnIndex("prtaxid")
                        this.getColumnModel().setEditable(productTax, true)
                        var returnQuantity = this.getColumnModel().findColumnIndex("dquantity")
                        this.getColumnModel().setEditable(returnQuantity, true)
                        var actualQuantity = this.getColumnModel().findColumnIndex("quantity")
                        this.getColumnModel().setEditable(actualQuantity, true)
                        /**
                         * Calculate discount while assigning value
                         */
                        if (rec.data.discountjson != undefined && rec.data.discountjson != '' && rec.data.quantity >= rec.data.dquantity) {
                            var jsonObj = JSON.parse(rec.data.discountjson);
                            calculateDiscount(jsonObj.data, rec, rec.data.rate, rec.data.dquantity, true);
                            this.fireEvent('datachanged',this);
                        }
                        
                    }
                }
                
                /**
                 * setting the islinkingFlag= true for the linked products so that
                 * price will be refresh or recalculate.
                 */
                if (this.parentObj!=undefined && this.parentObj.PO.getValue()!=undefined && this.parentObj.PO.getValue() != "") {
                        rec.set('islinkingFlag', true);
                    }
                if (rec.data.dquantity == "" && !this.allowZeroQuantity) {

                    var quantity = record.data.quantity;
                    quantity = (quantity == "NaN" || quantity == undefined || quantity == null) ? 0 : quantity;

                    rec.data.dquantity = quantity;
                }
                if(Wtf.isTDSApplicable && this.moduleid==Wtf.Acc_Purchase_Return_ModuleId && this.isNoteAlso){
                    this.reCalculateTDSAtLineLevel(rec);
                    this.fireEvent('datachanged',this);
                }
                if(rec!=undefined && rec!=null && rec.data!=undefined && rec.data!=null && rec.data!=""){
                  productids = productids + rec.data.productid + ",";    
                }                
            }
            if (this.isModuleForAvalara) {
                var productRecordsArr = [];
                for (var count = 0; count < record.length; count++) {
                    var tempObj = record[count].data;
                    if ((tempObj.pid || tempObj.productid) && tempObj.quantity) {
                        tempObj.rowIndex = count;
                        productRecordsArr.push(tempObj);
                    }
                }
                getTaxFromAvalaraAndUpdateGrid(this, undefined, productRecordsArr);
            }

            if(this.moduleid == Wtf.Acc_Purchase_Return_ModuleId && linkingFlag && productids!=""  && this.productOptimizedFlag != Wtf.Show_all_Products){
                 var product_ids=productids.substring(0,productids.length-1);
                 this.productComboStore.load({params:{ismultiselectProductids : true, selectedProductIds:product_ids}}) ;                    
            }            

            if (CompanyPreferenceChecks.differentUOM()) {
                this.productComboStore.load();
            }
        }, this);
    },  
    getProductDetails:function(){

        var arr=[];
        this.store.each(function(rec){
            if(rec.data.productid!=""){
                rec.data[CUSTOM_FIELD_KEY]=Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, this.moduleid).substring(13));
                rec.data[CUSTOM_FIELD_KEY_PRODUCT]=Wtf.decode(WtfGlobal.getCustomColumnDataForProduct(rec.data, this.moduleid, rec).substring(20));
                arr.push(this.store.indexOf(rec));
            }
            rec.set('qtipdiscountstr',rec.data.qtipdiscountstr);
            rec.set('discountjson', rec.data.discountjson);
        },this)
        var jarray=WtfGlobal.getJSONArray(this,false,arr);
        return jarray;
    },
    checkDetails: function (grid) {
        var v = WtfGlobal.checkValidItems(this.moduleid, grid);
        return v;
    },  
    populateDimensionValueingrid: function (rec) {
        WtfGlobal.populateDimensionValueingrid(this.moduleid, rec, this);
    },
    setCurrencyid:function(currencyid,rate,symbol,rec,store) {
       this.symbol=symbol;
       this.currencyid=currencyid;
       this.rate=rate;
       for(var i=0;i<this.store.getCount();i++){
           this.store.getAt(i).set('currencysymbol',this.symbol);
           this.store.getAt(i).set('currencyrate',this.rate);
       }
       this.getView().refresh();
    },
        showPriceWindow:function(btn,text,rec, obj){
        if(btn!="yes")return;
        callPricelistWindow(rec,"pricewindow",!this.isCustomer,this.billDate);
        this.priceStore.on('load',this.setPrevProduct.createDelegate(this,[rec,obj]), this);
        Wtf.getCmp("pricewindow").on('update',function(){this.loadPriceStore()},this);
    },
        setPrevProduct:function(rec,obj){
        obj.cancel=false;
        obj.ckeckProduct=false
        if(this.fireEvent("validateedit", obj) !== false && !obj.cancel){
            obj.record.set(obj.field, obj.value);
            delete obj.cancel;
            this.fireEvent("afteredit", obj);
        }
    },
     loadPriceStore:function(val){
        this.billDate=(val==undefined?this.billDate:val);
        this.priceStore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(this.billDate)}});           
    },
        loadPriceAfterProduct : function(){
        if(Wtf.getCmp(this.id)){ 
            this.loadPriceStore();
        } else {
            this.productComboStore.un("load",this.loadPriceAfterProduct,this);
        }
    },
    calLineLevelTax: function () {
        var subtotal = 0;
        var total = 0;
        var taxTotal = 0;
        var taxAmount = 0;
        var taxAndSubtotal = [];
        var count = this.store.getCount();
        for (var i = 0; i < count; i++) {
            total = parseFloat(this.store.getAt(i).data['amount']);
            subtotal += getRoundedAmountValue(total);
            taxAmount = parseFloat(this.store.getAt(i).data['taxamount']);
            taxTotal += getRoundedAmountValue(taxAmount);
        }
        taxAndSubtotal[0] = getRoundedAmountValue(subtotal);
        taxAndSubtotal[1] = getRoundedAmountValue(taxTotal);
        return taxAndSubtotal;
    },
     loadPriceStoreOnly:function(val,pricestore){  
        this.dateChange=true;
        this.billDate=(val==undefined?this.billDate:val);        
        pricestore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(this.billDate)}});
    },
    showTermWindow : function(record,grid,rowindex) {
        var deliveredprodquantity = record.data.quantity;
        deliveredprodquantity = (deliveredprodquantity == "NaN" || deliveredprodquantity == undefined || deliveredprodquantity == null)?0:deliveredprodquantity;
        if(deliveredprodquantity<=0){
            WtfComMsgBox(["Info","Quantity should be greater than zero. "], 2);
            return false;
        }
        /**
         *In CN/DN Overcharge/Undercharge tax can't edit for US country
         */
        var isCNDNOverUnderCharge = false;
        if (Wtf.account.companyAccountPref.countryid == Wtf.Country.US &&(this.moduleid==Wtf.Acc_Debit_Note_ModuleId || this.moduleid==Wtf.Acc_Credit_Note_ModuleId) && this.parentObj!=undefined && this.parentObj.inputValue!=undefined && (this.parentObj.inputValue == Wtf.NoteForUnderCharge || this.parentObj.inputValue == Wtf.NoteForOvercharge)) {
            isCNDNOverUnderCharge = true;
        }
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
                isGST:this.isGST,
                isCNDNOverUnderCharge : isCNDNOverUnderCharge,
                invAmount: record.data.amount,
                parentObj : this.parentObj,
                hideUnitPriceAmount : this.isCustomer ? !Wtf.dispalyUnitPriceAmountInSales : !Wtf.dispalyUnitPriceAmountInPurchase,
                gridObj : this,
                invQuantity: record.data.baseuomquantity,
                record:record,
                scope:this
            });
            this.Termwindow= new Wtf.Window({
                modal: true,
                id:'termselectionwindowtest',
                title: WtfGlobal.getLocaleText("acc.invoicegrid.termWindowTitle"),
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
                    hidden: WtfGlobal.isUSCountryAndGSTApplied() && !this.isModuleForAvalara && !isCNDNOverUnderCharge ? false : true,
                    disabled:this.isViewTemplate,
                    scope:this,
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
    BeforeTermSave:function(){
//         this.getStore().getAt(this.TermGrid.rowindex).set("LineTermdetails", "");
//        this.getStore().getAt(this.TermGrid.rowindex).set("LineTermdetails", JSON.stringify(this.TermGrid.getTermDetails()));

        var rec = this.getStore().getAt(this.TermGrid.rowindex)
        var termStore = eval(this.TermGrid.getTermDetails());

        if(this.parentObj && this.parentObj.includingGST && this.parentObj.includingGST.getValue() == true) {
            termStore = this.calculateTermLevelTaxesInclusive(termStore, rec);
        } else {
            termStore = this.calculateTermLevelTaxes(termStore, rec);
        }
        rec.set("LineTermdetails", JSON.stringify(this.TermGrid.getTermDetails()));
        updateTermDetails(this);
         
        if (this.parentObj!= undefined && typeof this.parentObj.updateSubtotal=='function') {
            this.parentObj.updateSubtotal();
        }
        
    },
    getTaxJsonOfIndia:function(prorec){

        this.venderDetails =WtfGlobal.searchRecord(this.parentObj.Name.store, this.parentObj.Name.getValue(), 'accid');

        var showOnlyCSTTax=this.venderDetails.data.interstateparty;
        var obj_CST =eval(prorec.data['LineTermdetails']);
        var termStore =new Array();
        for(var i_CST =0;i_CST<obj_CST.length;i_CST++){
            if(showOnlyCSTTax && (obj_CST[i_CST].termtype == 1)){
                if(!this.venderDetails.data.cformapplicable && obj_CST[i_CST].termtype == 1 && obj_CST[i_CST].taxCheck){
                    CSTCNotSubmitFormRate=obj_CST[i_CST].termpercentage;
                }
                continue;
            } else if(showOnlyCSTTax != undefined && !showOnlyCSTTax && (obj_CST[i_CST].termtype == 3)){
                continue;
            }
            if(!this.venderDetails.data.cformapplicable && obj_CST[i_CST].termtype == 3 && obj_CST[i_CST].taxCheck){
                obj_CST[i_CST].termpercentage = CSTCNotSubmitFormRate;
            }
            termStore.push(obj_CST[i_CST]);
        }

        return termStore;
    },

    calculateTermLevelTaxes : function(termStore, rec, index,isNewProduct){
    var quantity = rec.data.dquantity;
    quantity = (quantity == "NaN" || quantity == undefined || quantity == null) ? 0 : quantity;
    quantity = getRoundofValue(quantity);
    var rate = rec.data.rate;
    rate = (rate == "NaN" || rate == undefined || rate == null) ? 0 : rate;
    rate = getRoundofValue(rate);
    var amount = isNaN(rec.data.amount) ? rate * quantity : rec.data.amount;
    var finaltaxamount = 0;
    var FinalAmountNonTaxableTerm = 0;
    if(index == undefined){
        index = 0;
    }
    var finaltermStore = new Array();
    var totalVatTax=0;
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
                if(termJson.termtype == Wtf.term.Service_Tax){
                    var termpercentage = termJson.originalTermPercentage - ((termJson.originalTermPercentage * termJson.deductionorabatementpercent)/100);
                    termJson.taxvalue =  termpercentage;
                }else{
                    assessablevalue = (100 - termJson.deductionorabatementpercent) * assessablevalue / 100; //As tax will apply on amount excluding abatement.
                }
            }else{ //  for term tax value if abatement reset to 0(zero)
                if(termJson.termtype == Wtf.term.Service_Tax){ // For service tax
                    termJson.taxvalue =  termJson.originalTermPercentage ;
                }
            }
        // Apply Tax on Asessable Value
        if(termJson.taxtype == 2){ // If Flat
            taxamount = termJson.termamount;
        }else if(termJson.taxtype == 1){ // If percentage
            var taxamountfun = assessablevalue * termJson.taxvalue / 100;
            taxamount= this.TaxCalculation(termJson,rec,taxamountfun,assessablevalue);   
        }
                /**
         * Calculate CESS on Type for INDIA GST
         */
        var isCESSApplicable = true;
        if (WtfGlobal.isIndiaCountryAndGSTApplied() && termJson[Wtf.DEFAULT_TERMID] != undefined && termJson[Wtf.DEFAULT_TERMID] != ''
                && (termJson[Wtf.DEFAULT_TERMID] == Wtf.GSTTerm.OutputCESS || termJson[Wtf.DEFAULT_TERMID] == Wtf.GSTTerm.InputCESS)) {
            var params = {};
            var returnArray = calculateCESSONTypeAndValuationAmount(params, quantity, assessablevalue, termJson, taxamount);
            if (returnArray[0] != undefined) {
                taxamount = returnArray[0];
            }
            if (returnArray[1] != undefined && !returnArray[1]) {
                isCESSApplicable = returnArray[1];
            }
        }     
        termJson.termamount = getRoundedAmountValue(taxamount);
        termJson.assessablevalue = getRoundedAmountValue(assessablevalue);
        /**
         * 
         *If RCM applicable transaction then no tax applicable
         */
        var RCMApplicable = this.parentObj && this.parentObj.GTAApplicable && this.isCustomer ? this.parentObj.GTAApplicable.getValue() : false;
        if(!RCMApplicable){
            if (termJson.termtype == Wtf.term.Others && (termJson.IsOtherTermTaxable != undefined && !termJson.IsOtherTermTaxable)) {
                if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                    //we are first rounding taxamount & then sum it , because it causes 0.1 difference in Total Amount.
                    FinalAmountNonTaxableTerm += getRoundedAmountValue(taxamount);
                } else {
                    FinalAmountNonTaxableTerm += taxamount;
                }
            } else {
                if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                    //we are first rounding taxamount & then sum it , because it causes 0.1 difference in Total Amount.
                    finaltaxamount += getRoundedAmountValue(taxamount);
                } else {
                    finaltaxamount += taxamount;
                }
            }
        }
        if (isCESSApplicable) {
            finaltermStore.push(termJson);
        }
    }
        if (termStore.length < 1) {
            rec.set('amount', getRoundedAmountValue(amount));
            rec.set('recTermAmount', getRoundedAmountValue(0));
            rec.set('taxamount', getRoundedAmountValue(0));
        }
        if(finaltaxamount){
            rec.set('recTermAmount', getRoundedAmountValue(finaltaxamount));
            rec.set('taxamount', getRoundedAmountValue(finaltaxamount));
        } else {
            rec.set('recTermAmount', getRoundedAmountValue(0));
            rec.set('taxamount', getRoundedAmountValue(0));
        }
    if(FinalAmountNonTaxableTerm){
        rec.set('OtherTermNonTaxableAmount', getRoundedAmountValue(FinalAmountNonTaxableTerm));
    }
                
    return finaltermStore;
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
    },addTermForCalculation : function(termStore,termJson,totalVatTax,termSearchTerm,taxamount,vatFound,rec,uncheckedTerms,isNewProduct){
        var addCST=true;
        var needChangeJson=false;
        var termChangeIndex=-1;
        var formTypeCombo="";
        var replaceTermStore="";
        // For CST only -- Start
        if(Wtf.account.companyAccountPref.enablevatcst && termJson.termtype==Wtf.term.CST && (isNewProduct||!this.isEdit) && rec.data.valuationTypeVAT!=Wtf.excise.QUANTITY){// Condition Detail - for special case CST | This Logic Only for CST
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
            if(vatFound && termJson.termtype==Wtf.term.CST && rec.data.valuationTypeVAT!=Wtf.excise.QUANTITY){ // if VAT not found
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
    },checkTermExist : function (termStore,term,totalVatTax){
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
    }, getUncheckedTermDetails : function (prorec){ // it provide unchecked CST term list
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
    setPIDForBarcode : function(prorec,field,isTypeAhead){
        var rawValue = field.getRawValue();
//        field.setValue(prorec.productid);
        var count = this.store.getCount();
        var rowIndex = this.rowIndexForSpecialKey != undefined ? this.rowIndexForSpecialKey : ( count-1 ) ;
        var rec = this.store.getAt(rowIndex);
                            
        for (var key in prorec) {
            if (prorec.hasOwnProperty(key)) {
                rec.data[key] = prorec[key];
            }
        }
        rec.data.productid = prorec.productid;
        rec.data.productCode = prorec.pid;               // Replace rawValue with pid as rawValue might not be productCode always.
        rec.data.quantity = "";
           
//        if(isTypeAhead){
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
//        }
    },
    openWindowForSelectingTDS: function (record, rowindex) { // call TDS window
//        var gstCodeSelected = record.data['accountid'];
        var businesspersoninfo = WtfGlobal.searchRecord(this.parentObj.Name.store, this.parentObj.Name.getValue(), 'accid');
        if (!Wtf.isEmpty(businesspersoninfo) && businesspersoninfo != null) {
            businesspersoninfo = businesspersoninfo.data;
            var appliedTDS = record.data['appliedTDS']?record.data['appliedTDS']:'';
            
            // In Edit Case, empty line level amount is fetched so replace with current line level amount before opening TDS window
            if(this.editTransaction && !Wtf.isEmpty(appliedTDS)){
                var jsonArrayObj = eval(appliedTDS);
                for(var i=0;i<jsonArrayObj.length;i++){
                    if(Wtf.isEmpty(jsonArrayObj[i].amount)){
                        jsonArrayObj[i]["amount"] = record.get("amount");
                        jsonArrayObj[i]["enteramount"] = record.get("amount");
                    }
                }
                appliedTDS = JSON.stringify(jsonArrayObj);
            }
            this.TDSPaymentWindow = new Wtf.account.TDSPaymentWindow({
                id: 'tdstaxesexpancewindow',
                isReceipt: false,
                border: false,
                readOnly: this.readOnly, // Send readOnly Proeprty to TDS payment window
                isEdit: this.editTransaction,
                accountId: '',
                appliedTDS: appliedTDS,
                parentObj: this,
                personInfo: businesspersoninfo,
                record: record,
                callFrom: 'purchasereturn',// Call From Purchase Return
                basicExemptionExceeded: false
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
            if(!Wtf.isEmpty(jsonArrayObj[i].tdsamount)){
                totalTDSamount+=parseFloat(jsonArrayObj[i].tdsamount);
            }
        }
        var recordToSet=this.store.getAt(rowindex);
        recordToSet.set('tdsamount',totalTDSamount);
        recordToSet.set('appliedTDS',jsonArray);
        this.fireEvent('datachanged',this);
    },
    checkbatchDetails:function(grid){
        var v=WtfGlobal.checkBatchDetail(this.moduleid,grid);
        return v;
    },
    checkBatchDetailQty:function(grid){
        var v=WtfGlobal.checkBatchDetailQty(this.moduleid,grid);
        return v;
    },
    reCalculateTDSAtLineLevel: function(rec){
        if((rec.get('islinkingFlag') || !Wtf.isEmpty(rec.get('linkid'))) && !Wtf.isEmpty(rec.get('appliedTDS')) && rec.get("amount")>0){
            var jsonArrayObj = eval(rec.get('appliedTDS'));
            var totalTDSamount=0;
            for(var i=0;i<jsonArrayObj.length;i++){
                if(!Wtf.isEmpty(jsonArrayObj[i].amount) && jsonArrayObj[i].amount!=rec.get("amount")){
                    jsonArrayObj[i]["amount"] = rec.get("amount");
                    jsonArrayObj[i]["enteramount"] = rec.get("amount");
                    jsonArrayObj[i]["tdsAssessableAmount"] = rec.get("amount");
                    jsonArrayObj[i]["tdsamount"] = (rec.get("amount") * jsonArrayObj[i].tdspercentage) / 100;
                    if(!Wtf.isEmpty(jsonArrayObj[i].tdsamount)){
                        totalTDSamount+=parseFloat(jsonArrayObj[i].tdsamount);
                    }
                }
            }
            if(totalTDSamount>0){
                rec.set('tdsamount',totalTDSamount);
                rec.set('appliedTDS',JSON.stringify(jsonArrayObj));
            }
//            this.fireEvent('datachanged',this);
        }
    },
    setTaxAndRateAmountAfterIncludingGST : function (record) {
        if(record.data.prtaxid!="None"){
        var taxamount = this.setTaxAmountAfterIncludingGst(record,1);
        var amountwithOutGst = this.setTaxAmountAfterIncludingGst(record,2);
        record.set("taxamount",taxamount);
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
            updateTermDetails(this);           
        }
    },
    setTaxAmountAfterIncludingGst:function(rec,amountFlag) {//amountFlag=1 for taxamount and amountFlag=2 for actual amount with discount actualamount=3 amount with GST
        
        var quantity = rec.data.dquantity;
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
        var discount = 0;
        var rateIncludingGst=getRoundofValueWithValues(rec.data.rateIncludingGst,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        quantity=getRoundofValue(rec.data.dquantity);
        var lineTermAmount = (rec.data.lineleveltermamount == undefined || rec.data.lineleveltermamount == null || rec.data.lineleveltermamount == '')?0:rec.data.lineleveltermamount;
        var quantityAndAmount=0;
        quantityAndAmount=rateIncludingGst*quantity;
        var origionalAmount = getRoundedAmountValue(quantityAndAmount) ;
        
        if(rec.data.prdiscount > 0) {
            var prdiscount=getRoundedAmountValue(rec.data.prdiscount);
            if(rec.data.discountispercent == 1){
                discount = getRoundedAmountValue(origionalAmount * prdiscount/ 100);
            } else {
                discount = prdiscount;
            }
        }

        var val=origionalAmount-discount+lineTermAmount;
        var taxpercent=0;
        var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
        if(index>=0){
            var taxrec=this.taxStore.getAt(index);
            taxpercent=getRoundedAmountValue(taxrec.data.percent);
        }
        var amount=0.0;
        var taxamount=getRoundedAmountValue(val*taxpercent/(taxpercent+100));

        var unitAmount= 0;
        var unitTax= 0;
        var unitVal= 0;
        if(quantity!=0){
             amount=getRoundedAmountValue((val-taxamount));
             unitVal=getRoundedAmountValue(val/quantity);
             unitAmount=getRoundedAmountValue(amount/quantity);
             unitTax= getRoundedAmountValue(taxamount/quantity);
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
        } else {
            Wtf.uomStore.clearFilter();
        }
    }
});
