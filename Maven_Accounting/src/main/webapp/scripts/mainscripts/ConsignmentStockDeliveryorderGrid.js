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
Wtf.account.ConsignmentStockDeliveryorderGrid=function(config){
    this.isFixedAsset=(config.isFixedAsset)?config.isFixedAsset:false;
    this.isLeaseFixedAsset=(config.isLeaseFixedAsset)?config.isLeaseFixedAsset:false;
    this.isConsignment = (config.isConsignment)?config.isConsignment:false;
    this.isGRCreatedByLinkingWithPI=false;
    this.isCustomer=config.isCustomer;
    this.movmentType=config.movmentType;
    this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
    if(this.productOptimizedFlag != undefined && this.productOptimizedFlag==Wtf.Show_all_Products){
        this.productComboStore=(this.isFixedAsset)?Wtf.FixedAssetStore:(this.isCustomer?((this.isLeaseFixedAsset)?Wtf.FixedAssetAndProductLeaseStore:Wtf.productStoreSales):Wtf.productStore);      
    }else if(this.productOptimizedFlag==Wtf.Products_on_type_ahead || this.productOptimizedFlag==Wtf.Products_on_Submit){
        this.productComboStore=(this.isFixedAsset)?Wtf.FixedAssetStoreOptimized:(this.isCustomer?((this.isLeaseFixedAsset)?Wtf.FixedAssetAndProductLeaseStoreOptimized:Wtf.productStoreSalesOptimized):Wtf.productStoreOptimized);      
    }
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
    this.pronamearr=[];
    this.fromPO=config.fromPO;
    this.readOnly=config.readOnly;
    this.gridConfigId="";
//    this.copyInv=config.copyInv;
    this.editTransaction=config.editTransaction;
    this.copyTrans=config.copyTrans;
    this.noteTemp=config.noteTemp;
    this.editLinkedTransactionQuantity= Wtf.account.companyAccountPref.editLinkedTransactionQuantity;
    this.editLinkedTransactionPrice= Wtf.account.companyAccountPref.editLinkedTransactionPrice;
    this.isLinkedTransaction= (config.isLinkedTransaction == null || config.isLinkedTransaction == undefined)? false : config.isLinkedTransaction;
    this.UomSchemaType=Wtf.account.companyAccountPref.UomSchemaType;
    this.isEdit=config.isEdit;
    this.fromOrder=config.fromOrder;
    this.moduleid = config.moduleid;
    this.CUSTOM_KEY = "customfield";
      this.isDeferredRevenueRecognition=Wtf.account.companyAccountPref.isDeferredRevenueRecognition&&this.isCustomer;
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
    //this.loadPriceStore();
    var colModelArray = [];
    colModelArray = GlobalColumnModel[this.moduleid];
    if(colModelArray) {
        colModelArray.concat(GlobalColumnModelForProduct[this.moduleid]);
    }
    WtfGlobal.updateStoreConfig(colModelArray, this.store);
    Wtf.account.ConsignmentStockDeliveryorderGrid.superclass.constructor.call(this,config);
    this.addEvents({
        'datachanged':true,
        'pricestoreload':true,
        'productdeleted':true
    });
    this.on('populateDimensionValue',this.populateDimensionValueingrid,this);
}
Wtf.extend(Wtf.account.ConsignmentStockDeliveryorderGrid,Wtf.grid.EditorGridPanel,{
    clicksToEdit:1,
    stripeRows :true,
    rate:1,
    symbol:null,
    layout:'fit',
    viewConfig:{forceFit:true},
    forceFit:true,
    loadMask:true,
    onRender:function(config){
        Wtf.account.ConsignmentStockDeliveryorderGrid.superclass.onRender.call(this,config);
        this.isValidEdit = true;
        WtfGlobal.getGridConfig(this,this.moduleid,true,false);
        if(Wtf.userds)
            Wtf.userds.load();
        if(Wtf.locationStore)
            Wtf.locationStore.load();
        if(Wtf.detartmentStore)
            Wtf.detartmentStore.load();


//         this.on('render',this.addBlankRow,this);
        if(this.productOptimizedFlag!=Wtf.Products_on_Submit){
        this.on('afteredit',this.updateRow,this);
        } else {
         this.on('afteredit',this.callupdateRowonProductLoad,this);
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
              
//            if(this.isLeaseFixedAsset){
              var dataMsg="";
            if(this.isCustomer){// linked from SO in consignment DO
                if((this.isLinkedTransaction == undefined || this.isLinkedTransaction == false)){
                    dataMsg = WtfGlobal.getLocaleText("acc.Consignment.asset.do.cr");
                }
            } else{
                dataMsg = WtfGlobal.getLocaleText("acc.Consignment.asset.gr.cr");
            }
            //ERM-766 adding consign GRN module id 57 as mandatory check needs to be removed while saving consign GRN
            if(this.isConsignment && dataMsg != "" && this.moduleid!==57){ 
                if(this.fromPO == undefined || this.fromPO == null || !this.fromPO){
                    e.cancel=true;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),dataMsg], 4);
                    return;
                }
            }
            //code for do not allow to make changes in Consignment DOacc.field.ProductQuantityenteredinDOisexceedsfromoriginal
//            if(this.isConsignment &&this.fromOrder){
//                  e.cancel=true;
//                  WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.Consignment.do.changes") ], 4);
//                return;
//            }
              
        if((this.isFixedAsset || this.isLeaseFixedAsset || this.isConsignment) && this.fromPO){
            if(this.store.getCount()-1==e.row){// if last row then don't allow edition, and appending row in case of linking'
                e.cancel=true;
                return;
            }
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
         if(e.field == "uomid" && e.record.data.productid !="" && this.UomSchemaType == Wtf.PackegingSchema ){
               var prorec=undefined;
                if(this.productOptimizedFlag== undefined || this.productOptimizedFlag==Wtf.Show_all_Products){  
                    prorec = WtfGlobal.searchRecord(this.productComboStore, e.record.data.productid, 'productid');
                }else{
                    prorec = WtfGlobal.searchRecord(this.store, e.record.data.productid, 'productid');
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
             if(e.field == "rate" && isRateFieldEditable){	// rate editable for product type "Service"
           	 var beforeEditRecord=this.productComboStore.getAt(this.productComboStore.find('productid',e.record.data.productid));
            	 if(beforeEditRecord == undefined || beforeEditRecord == null){
            		 e.cancel = true;
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
             if(e.field == "quantity" && isQuantityFieldEditable){
                 var beforeEditRecord=this.productComboStore.getAt(this.productComboStore.find('productid',e.record.data.productid));
            	 if(beforeEditRecord == undefined || beforeEditRecord == null){
            		if(this.productOptimizedFlag== undefined || this.productOptimizedFlag==Wtf.Show_all_Products){  
               		e.cancel = true;
                    }else{
                        if(e.record.data ==undefined || e.record.data.productid == undefined || e.record.data.productid ==""){ 
                            e.cancel = true;
                        }
                    }
            	 }
             }
             //Not allowing to edit the product,uom,quantity,delivered quantity and baseuom rate if it is linked in transaction
             if((this.isLinkedTransaction !=undefined && this.isLinkedTransaction) && (e.field == "productid" || e.field == "uomid" || e.field == "rate" || e.field == "baseuomrate" || e.field == "quantity" || e.field == "dquantity")){
                 e.cancel=true;
                 return;
             }
             
//         if(this.isDeferredRevenueRecognition){ // Fixed Bug[13888]: Overlaping text box on validation alert messages. [on TAB key navigation]
//                 e.cancel= true;                 
//             }    
             
         },this); 
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
    saveGridStateHandler: function(grid,state){
        if(!this.readOnly){
            WtfGlobal.saveGridStateHandler(this,grid,state,this.moduleid,this.gridConfigId,true);
        }
    },
    populateDimensionValueingrid: function(rec) {
        WtfGlobal.populateDimensionValueingrid(this.moduleid, rec, this);
    },
    checkDetails: function(grid) {
        var v = WtfGlobal.checkValidItems(this.moduleid, grid);
        return v;
    },
    RitchTextBoxSetting:function(grid, rowIndex, columnIndex, e){
        var v=WtfGlobal.RitchTextBoxSetting(grid, rowIndex, columnIndex, e, this.readOnly);
        return v;
    },
    descriptionRenderer :function(val, meta, rec, row, col, store) {
        var regex = /(<([^>]+)>)/ig;
//        val = val.replace(/(<([^>]+)>)/ig,"");
        var tip = val.replace(/"/g,'&rdquo;');
        meta.attr = 'wtf:qtip="'+tip+'"'+'" wtf:qtitle="'+WtfGlobal.getLocaleText("acc.gridproduct.discription")+'"';
        return val;
    },
     callupdateRowonProductLoad: function(obj) {
         if (obj != undefined || obj != null) {
             this.obj=obj;
             if(this.obj.field=='pid'){
                    this.productComboStore.load();
                    this.productComboStore.on('load',function(){
                    if(this.productComboStore.getCount()<=0){
                         if(this.store.getCount()> this.obj.row && this.obj.originalValue !="" && this.obj.originalValue != this.obj.value && this.obj.record.data.productid!=""){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.common.productWithSpecifiedId")+" "+this.obj.value+" "+WtfGlobal.getLocaleText("acc.common.productDoesNotExistsOrInDormantState")], 2);              
                                this.obj.record.set(this.obj.field, this.obj.originalValue);
                                obj.cancel=true;   
                                return;
                            }else{
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.common.productWithSpecifiedId")+" "+this.obj.value+" "+WtfGlobal.getLocaleText("acc.common.productDoesNotExistsOrInDormantState")], 2);              
                                obj.cancel=true;   
                                return;
                            }

                    }else
                        this.updateRow(this.obj);
                },this);
             }else{
                 this.updateRow(this.obj);
                 
             }
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
            {name:'uomname'},
            {name:'salesuomname'},
            {name:'purchaseuomname'},
            {name:'salesuom'},
            {name:'purchaseuom'},
            {name:'stocksalesuomvalue'},
            {name:'stockpurchaseuomvalue'},
            {name:'caseuom'},
            {name:'inneruom'},
            {name:'caseuomvalue'},
            {name:'inneruomvalue'},
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
            {name:'location'},
            {name:'warehouse'},
            {name: 'level'},
            {name: 'initialquantity',mapping:'initialquantity'},
            {name: 'initialprice'},
            {name: 'producttype'},
            {name:'shelfLocation'},
            {name:'supplierpartnumber'},
            {name:'isAsset',type:'boolean'}
        ]);

        this.priceStore = new Wtf.data.Store({        
            url:"ACCProduct/getProductsForCombo.do",
            baseParams:{mode:22,isFixedAsset:this.isFixedAsset
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
            {name:'dorowid'},
            {name:'productid'},
            {name:'description'},
            {name:'shelfLocation'},
            {name:'partno'},
            {name:'quantity',defValue:1},
            {name:'dquantity',defValue:1},
            {name:'baseuomquantity',defValue:1.00},
            {name:'availableQtyInSelectedUOM'},
            {name:'rate',defValue:0},
            {name:'amount',defValue:0},
            {name:'uomname'},
            {name:'baseuomname'},
            {name:'uomid'},
            {name:'stockuom'},
            {name:'caseuom'},
            {name:'inneruom'},
            {name:'caseuomvalue'},
            {name:'inneruomvalue'},
            {name:'salesuomname'},
            {name:'purchaseuomname'},
            {name:'salesuom'},
            {name:'purchaseuom'},
            {name:'stocksalesuomvalue'},
            {name:'stockpurchaseuomvalue'},
            {name:'baseuomrate',defValue:1.00},
            {name:'copyquantity'},
            {name:'invstore'},
            {name:'invlocation'},
            {name:'copybaseuomrate',mapping:'baseuomrate'},//for handling inventory updation 
//            {name:'copyquantity',mapping:'quantity'},
//            {name:'rate'},
//            {name:'rateinbase'},
//            {name:'discamount'},
//            {name:'discount'},
//            {name:'prdiscount'},
//            {name:'prtaxid'},
//            {name:'prtaxname'},
//            {name:'prtaxpercent'},
//            {name:'taxamount'},
//            {name:'amount'},
//            {name:'amountwithtax'},
//            {name:'taxpercent'},
            {name:'remark'},
            {name:'Custom_Remark'},
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
            {name:'linkDate'},//Created for taking date of linked documents
            {name:'linktype'},
            {name:'savedrowid'},
            {name:'docrowid'},
            {name:'originalTransactionRowid'},
            {name:'batchdetails'},
            {name:'replacebatchdetails'},
            {name:'lockquantity'},
            {name:'islockQuantityflag'},
            {name:'customfield'},
            {name:'productcustomfield'},
            {name:'assetDetails'},
            {name:'isAsset',type:'boolean'},
            {name:'supplierpartnumber'},
            {name:'pid'},
            {name: 'isLocationForProduct'},
            {name: 'isRowForProduct'},
            {name: 'isRackForProduct'},
            {name: 'isBinForProduct'},
            {name: 'isWarehouseForProduct'},
            {name: 'isBatchForProduct'},
            {name: 'isSerialForProduct'},
            {name: 'isAsset'},
            {name: 'location'},
            {name: 'warehouse'},
            {name: 'requestLocation'},
            {name: 'requestWarehouse'},
            {name: 'type'},
            {name: 'prodavlqty'},
            {name: 'srno', isForSequence:true}
           // {name:'linkid'}
//            {name:'deliveredquantity'},
                          
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
        this.productId= new Wtf.form.TextField({
            name:'pid'
//            readOnly:true
        });
        this.productComboStore.on('beforeload',function(s,o){
                if(!o.params)o.params={};
                var currentBaseParams = this.productComboStore.baseParams;
                currentBaseParams.getSOPOflag=true;
                currentBaseParams.startdate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true));
                currentBaseParams.enddate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false));         
                currentBaseParams.searchProductString = this.productOptimizedFlag==Wtf.Products_on_Submit?this.productId.getValue():""; 
                currentBaseParams.isFreeTextSearching = (this.productOptimizedFlag==Wtf.Products_on_Submit);
                this.productComboStore.baseParams=currentBaseParams;   
                 if(this.ProductloadingMask==undefined){
                this.ProductloadingMask = new Wtf.LoadMask(document.body,{
                    msg : WtfGlobal.getLocaleText("acc.msgbox.57")
                });
                this.ProductloadingMask.show();
            }
            },this); 
        
        this.productComboStore.on("load",function(){
            if(this.ProductloadingMask){
               this.ProductloadingMask.hide();   
            }
            //ERM-766 adding consign GRN moduleid as mandatory checks are removed for consign GRN hence grid should not refresh
            if (this.moduleid !== 57) {
                this.getView().refresh();
            }
        },this);
        this.productComboStore.on("loadexception",function(){
            if(this.ProductloadingMask)
                this.ProductloadingMask.hide();
        },this);
        
        if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Show_all_Products){
            this.isCustomer ? chkproductSalesload() : chkproductload() ;

            this.productEditor=new Wtf.form.ExtFnComboBox({
                name:'pid',
                store:this.productComboStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
                typeAhead: true,
                selectOnFocus:true,
                isProductCombo: true,
                maxHeight:250,
                listAlign:"bl-tl?",//[ERP-5149] To expand list of combobox always on top. 
                valueField:'productid',
                displayField:'pid',
                extraFields:['productname','type'],
                listWidth:400,
                lastQuery:'',
                extraComparisionField:'pid',// type ahead search on acccode as well.
                //editable:false,
                scope:this,
                hirarchical:true,
            // addNewFn:this.openProductWindow.createDelegate(this),
                forceSelection:true,
                isProductCombo:true
            });
         }else{
             this.productEditor=new Wtf.form.ExtFnComboBox({
                    name:'pid',
                    store:this.productComboStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
                    typeAhead: true,
                    selectOnFocus:true,
                    isProductCombo: true,
                    maxHeight:250,
                    listAlign:"bl-tl?",//[ERP-5149] To expand list of combobox always on top. 
                    valueField:'productid',//productid
                    displayField:'pid',
                    extraFields:['productname','type'],
                    listWidth:450,
                    extraComparisionField:'pid',// type ahead search on acccode as well.
                    mode:'remote',
                    //editable:false,
                    hideTrigger:true,
                    scope:this,
                    triggerAction : 'all',
                    editable : true,
                    minChars : 2,
                    hirarchical:true,
                    //hideAddButton : true,//Added this Flag to hide AddNew  Button  
                    //addNewFn:this.openProductWindow.createDelegate(this),
                    forceSelection:true,
                    isProductCombo:true
                });
          }
          this.productEditor.on('beforeselect', function(combo, record, index) {
                return validateSelection(combo, record, index);
        }, this);
          if(this.productOptimizedFlag!=Wtf.Products_on_Submit){
                this.productEditor.on("blur",function(e,a,b){
                    if(Wtf.account.companyAccountPref.invAccIntegration && !this.isCustomer){
                        e.store=this.productComboStore;
                    }    
                },this);
          }  
//        if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.edit))
//            this.productEditor.addNewFn=this.openProductWindow.createDelegate(this);
        
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

        this.productComboStore.on("load",this.loadPriceAfterProduct,this);
        this.remark= new Wtf.form.TextField({
            name:'remark'
        });        
        
        this.inventoryStores = new Wtf.form.ComboBox({
            store: Wtf.inventoryStore,
            name:'storeid',
            displayField:'storedescription',
            valueField:'storeid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
        });
        
        this.inventoryLocation = new Wtf.form.ComboBox({
            store: Wtf.inventoryLocation,
            name:'locationid',
            displayField:'locationname',
            valueField:'locationid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
        });
        
        this.partno= new Wtf.form.TextField({
            name:'partno',
            maxLength : 255
        });

        this.actQuantity=new Wtf.form.NumberField({
            allowBlank: false,            
            defaultValue:0,
            allowNegative: false,
            maxLength:10,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
        });
        
        this.deliQuantity=new Wtf.form.NumberField({
            allowBlank: false,            
            defaultValue:0,
            allowNegative: false,
            maxLength:10,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
        });                                 
        this.transBaseuomrate=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength:10
        });
        this.editprice = new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
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

    createColumnModel:function(){                
        this.summary = new Wtf.ux.grid.GridSummary();
        this.rowno=(this.isNote)?new Wtf.grid.CheckboxSelectionModel():new Wtf.grid.RowNumberer();
        var columnArr =[];
        if(!this.readOnly){
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
//        ,{
//            header:(this.isLeaseFixedAsset ||this.isConsignment)?WtfGlobal.getLocaleText("acc.product.gridProductID"):WtfGlobal.getLocaleText("acc.invoice.gridAssetGroup"),//"Asset Group",
//            width:200,
//            dataIndex:this.readOnly?'pid':'productid',
//            renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.productEditor),
//            editor:(this.isNote||this.readOnly)?"":this.productEditor
//        },

        //added sequence arrows - refer ticket ERP-13781
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
                header:(this.isLeaseFixedAsset ||this.isConsignment)?WtfGlobal.getLocaleText("acc.product.gridProductID"):WtfGlobal.getLocaleText("acc.invoice.gridAssetGroup"), // "Produt ID" : "Asset Group",
                width:200,
                 //dataIndex:(this.readOnly ||this.productOptimizedFlag==Wtf.Products_on_type_ahead)? 'pid' : 'productid',
                dataIndex:(this.isLeaseFixedAsset)? (this.readOnly?'pid':'productid') : (this.readOnly?'pid':'productid'),
                renderer:(this.productOptimizedFlag==Wtf.Products_on_type_ahead)?(this.readOnly?"":this.getComboNameRenderer(this.productEditor)):(this.readOnly?"":Wtf.comboBoxRenderer(this.productEditor)),
                editor:(this.isNote||this.readOnly)?"":this.productEditor
            });
        }else{
           columnArr.push({
                header: WtfGlobal.getLocaleText("acc.product.gridProductID"),//"Product ID",
                dataIndex: 'pid',
                editor:(this.isNote||this.readOnly||this.productOptimizedFlag!=Wtf.Products_on_Submit)?"":this.productId,
                width:200
            });    
        }
        
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"), // "Product Name",
            dataIndex: 'productname',
            hidden: this.isFixedAsset
        },{
            header:this.isCN?WtfGlobal.getLocaleText("acc.invoice.gridInvNo"):WtfGlobal.getLocaleText("acc.invoice.gridVenInvNo"),//"Invoice No.":"Vendor Invoice No.",
            width:150,
            hidden:(!this.isNote ||this.isConsignment),
            dataIndex:this.noteTemp?'transectionno':'billno'
           },{
             header:WtfGlobal.getLocaleText("acc.do.partno"),//"Part No",
             dataIndex:"partno",
             width:250,
             hidden:true,
             editor:this.partno
         },{
             header:(this.isLeaseFixedAsset ||this.isConsignment)?WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"):WtfGlobal.getLocaleText("erp.field.AssetDesciption"),//"Description",
             dataIndex:"description",
             hidden:this.isNote,
             width:250,
             editor:(this.isNote||this.readOnly)?"":this.remark,
             renderer:this.descriptionRenderer
//                 function(val){
//                val = val.replace(/(<([^>]+)>)/ig,"");
//                return val;   
////                if(val.length<50)
////                    return val;   
////                else
////                    return val.substring(0,50)+" ...";   
//            }
         });
        columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModelForProduct[this.moduleid],undefined,undefined,this.readOnly);
        columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModel[this.moduleid],undefined,undefined,this.readOnly);
         columnArr.push({
             header:WtfGlobal.getLocaleText("acc.product.supplier"),//"Supplier Part Number",
             dataIndex:"supplierpartnumber",
             hidden:!(Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.partNumber && !this.isCustomer),
             width:150
          },{
             header:WtfGlobal.getLocaleText("acc.field.InventoryStore"), 
             dataIndex:'invstore',
             hidden:!(Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel),
                 //&& (Wtf.account.companyAccountPref.withinvupdate && (this.isCashType||this.isCN))),
             width:150,
             renderer:Wtf.comboBoxRenderer(this.inventoryStores),
             editor:(this.readOnly)?"":this.inventoryStores
         },{
             header:WtfGlobal.getLocaleText("acc.field.ShelfLocation"),
             dataIndex:"shelfLocation",
             hidden:!(!this.isCustomer&&Wtf.account.companyAccountPref.invAccIntegration),
             width:250,
             editor:(this.readOnly)?"":new Wtf.form.TextField()        
         },{
             header:WtfGlobal.getLocaleText("acc.field.InventoryLocation"), 
             dataIndex:'invlocation',
             hidden:!(Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel),
                 //&& (Wtf.account.companyAccountPref.withinvupdate && (this.isCashType||this.isCN))),
             width:150,
             renderer:Wtf.comboBoxRenderer(this.inventoryLocation),
             editor:(this.readOnly)?"":this.inventoryLocation
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
             width:100,
             editor:(this.readOnly)?"":this.deliQuantity,
             renderer:this.quantityRenderer
//             renderer:this.storeRenderer(this.productComboStore,"productid","uomname")
         
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridUOM"),//"UOM",
            width:100,
            hidden:this.isFixedAsset,
            dataIndex:this.readOnly?'uomname':'uomid',
            renderer:this.readOnly?"":Wtf.comboBoxRendererwithClearFilter(this.uomEditor),
            editor:(this.isNote||this.readOnly)?"":this.uomEditor
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridRateToBase"),//Base UOM Rate
             dataIndex:"baseuomrate",
             hidden:this.isFixedAsset,
             align:'left',
             width:100,
             renderer:this.conversionFactorRenderer(this.productComboStore,"productid","uomname",this.store),
             editor:(this.isNote||this.readOnly||this.UomSchemaType==Wtf.PackegingSchema)?"":this.transBaseuomrate
         },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridQtyInBase"),//Base UOM Quantity
             dataIndex:"baseuomquantity",
             align:'right',
             hidden:this.isFixedAsset,
             width:50,
             renderer:this.storeRenderer(this.productComboStore,"productid","uomname",this.store)
//             editor:(this.isNote||this.readOnly)?"":this.transQuantity
         },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice"), // "Unit Price",
            dataIndex: "rate",
            align:'right',
            width:150,
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            editor:(this.isNote||this.readOnly) ? "" : this.editprice,
            editable:true,
            hidden: !(this.isCustomer?Wtf.account.companyAccountPref.unitPriceInDO:Wtf.account.companyAccountPref.unitPriceInGR)
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridAmount"), // "Amount",
             dataIndex:"amount",
             align:'right',
             width:200,
             renderer:(this.isNote?WtfGlobal.withoutRateCurrencySymbol:this.calAmount.createDelegate(this)),
             hidden: !(this.isCustomer?Wtf.account.companyAccountPref.unitPriceInDO:Wtf.account.companyAccountPref.unitPriceInGR)
        },{
                header:WtfGlobal.getLocaleText("acc.field.Remarks"),  //"Remark",
                dataIndex:"remark",
                width:150,
                editor:this.readOnly? "":this.Description=new Wtf.form.TextArea({
                    maxLength:200,
//                    allowBlank: false,
                    xtype:'textarea'
                })
     },{
             header: '',
            align:'center',
            renderer:function(a,b,c){
                 
                if(this.isFixedAsset){
                    return viewRenderer();
                }else if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.companyAccountPref_isLocationCompulsory || Wtf.companyAccountPref_isWarehouseCompulsory){
                    return serialRenderer();  
                }
                
            },
          width:40           
        });
        if(!this.isNote && !this.readOnly && !this.isLinkedTransaction) {
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),//"Action",
                align:'center',
                width:40,
                hidden:this.readOnly,
                renderer: this.deleteRenderer.createDelegate(this)
            });
        }
       /* if(this.isFixedAsset || this.isLeaseFixedAsset) {
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.fixed.asset.view"),
                align:'center',
                width:40,
                renderer: this.viewRenderer.createDelegate(this)
            });
        }*/
        this.cm=new Wtf.grid.ColumnModel(columnArr);
    },
    quantityRenderer:function(val,m,rec){
      return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
    },
    serialRenderer:function(v,m,rec){
        return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.serial.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.serial.desc.title")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
    },
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    viewRenderer:function(v,m,rec){
        return "<div class='view pwnd view-gridrow'  title='View Asset Details '></div>";
    },
    handleRowClick:function(grid,rowindex,e){
        if(!this.readOnly && e.target.className == "pwndBar2 shiftrowupIcon") {
            moveSelectedRowFormasterItems(grid,0,rowindex);
        }
        if(!this.readOnly && e.target.className == "pwndBar2 shiftrowdownIcon") {
            moveSelectedRowFormasterItems(grid,1,rowindex);
        }
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.nee.48"), function(btn){
                if(btn!="yes") return;
                var store=grid.getStore();
                var total=store.getCount();
                var record = store.getAt(rowindex);
                if(record.data.copyquantity!=undefined){                    
                     var deletedData=[];
                     var newRec=new this.deleteRec({
                                productid:record.data.productid,
                                productname:record.data.productname,    
                                productquantity:record.data.dquantity,
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
                if(rowindex==total-1){
                    this.addBlankRow();
                }
                this.fireEvent('productdeleted',this);
                this.fireEvent('datachanged',this);
            }, this);
        } else if((this.isFixedAsset || this.isLeaseFixedAsset) && e.getTarget(".view-gridrow")){
            var store=grid.getStore(); 
            var total=store.getCount();
            if(rowindex==total-1){
                return;
            }
            var record = store.getAt(rowindex);
            if(this.isFixedAsset || this.isLeaseFixedAsset){
                var productid = record.get('productid');
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
                    if(proRecord.get('isAsset') && record.get("quantity") != 0)
                        this.callFixedAssetDetailsWindow(record,proRecord);
                }
                
            }

        } else if(e.getTarget(".serialNo-gridrow") && this.isLinkedTransaction !=undefined && !this.isLinkedTransaction){
             var store=grid.getStore();
            var record = store.getAt(rowindex);
//            if(this.isFixedAsset || this.isLeaseFixedAsset){
                var productid = record.get('productid');
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
                    if(proRecord.get('isAsset')==false && record.get("quantity") != 0)
                    if(proRecord.data.type!='Service' && proRecord.data.type!='Non-Inventory Part'){  //serial no for only inventory type of product
                        if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.companyAccountPref_isLocationCompulsory || Wtf.companyAccountPref_isWarehouseCompulsory){ //if company level option is on then only check batch and serial details
                            if(proRecord.data.isBatchForProduct || proRecord.data.isSerialForProduct || proRecord.data.isLocationForProduct || proRecord.data.isWarehouseForProduct){ 
                                this.callSerialNoWindow(record);
                            }else{
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.batchserial.Functinality")],2);   //Batch and serial no details are not valid.
                                return;
                            }
                        }
                    }else{
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.batchserial.funforInventoryitems")],2);   //Batch and serial no details are not valid.
                        return;
                    }        
                }
                
//            }
              

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
    storeRenderer:function(store, valueField, displayField,gridStore) {
        return function(value, meta, record) {
            value=(parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
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
    updateRow:function(obj){
        if(obj!=null){            
            this.productComboStore.clearFilter(); // Issue 22189
            var rec=obj.record;
            if(obj.field=="baseuomrate"){
                if(this.isCustomer)
                    rec.set("changedQuantity",(rec.data.copybaseuomrate-obj.value)*((rec.data.copyquantity==="")?1:rec.data.copyquantity));
                  else
                    rec.set("changedQuantity",(obj.value-rec.data.copybaseuomrate)*((rec.data.copyquantity==="")?1:rec.data.copyquantity));
                
                  var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
                  var productuomid = "",datewiseprice=0;
                  if(productComboIndex >=0){
                      prorec = this.productComboStore.getAt(productComboIndex);
                      productuomid = prorec.data.uomid;
                      if(this.UomSchemaType==Wtf.UOMSchema){//for Schema type
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
                                quantity: obj.record.data.quantity,
                                transactiondate : WtfGlobal.convertToGenericDate(this.billDate)
                            }
                        }, this,function(response){
                            datewiseprice =response.data[0].price;
                            var modifiedRate;
                            modifiedRate=WtfGlobal.getIndividualProductPriceInMultiCurrency(rec,datewiseprice);
                            if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                                obj.record.set("baseuomquantity", obj.record.get("quantity")*obj.value);
                                obj.record.set("rate", modifiedRate*obj.value);
                            }else {
                                obj.record.set("baseuomrate", 1);
                            } 
                            this.fireEvent('datachanged',this);
                        }, function(){

                            });
                    }
//                      if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
//                            obj.record.set("baseuomquantity", obj.record.get("quantity")*obj.value);
//                      } else {
//                          obj.record.set("baseuomrate", 1);
//                      }                      
                  }
//                  this.fireEvent('datachanged',this);
            }
            if(obj.field=="uomid"){
                  var prorec = null;
                  var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
                  var productuomid = "";
                  if(productComboIndex >=0){
                      var baseuomrate =1,rateperuom=0,datewiseprice=0;
                      prorec = this.productComboStore.getAt(productComboIndex);
                      productuomid = prorec.data.uomid;
                   if(this.UomSchemaType==Wtf.UOMSchema){//for Schema type
                        //for Schema type
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
                                    uomschematypeid:uomschemaid,
                                    productId:prorec.data.productid,
                                    startdate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true)),
                                    enddate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false)),
                                    currentuomid:obj.value,
                                    carryin : (this.isCustomer)? false : true,
                                    productid:prorec.data.productid,
                                    affecteduser: this.affecteduser,
                                    forCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency:"",
                                    quantity: obj.record.data.quantity,
                                    transactiondate : WtfGlobal.convertToGenericDate(this.billDate)
                                }
                                }, this,function(response){
                                    baseuomrate =response.data[0].baseuomrate;
                                    rateperuom =response.data[0].rateperuom;
                                    datewiseprice =response.data[0].price;

                                    var availableQtyInSelectedUOM =response.data[0].availableQtyInSelectedUOM;
                                    var pocountinselecteduom =response.data[0].pocountinselecteduom;
                                    var socountinselecteduom =response.data[0].socountinselecteduom;

                                    obj.record.set("baseuomrate", baseuomrate);
                                    obj.record.set("baseuomquantity", obj.record.get("quantity")*baseuomrate);
                                    obj.record.set("availableQtyInSelectedUOM", availableQtyInSelectedUOM);
                                    obj.record.set("isAnotherUOMSelected", true);
                                    obj.record.set("pocountinselecteduom", pocountinselecteduom);
                                    obj.record.set("socountinselecteduom", socountinselecteduom);

                                    var modifiedRate;
                                    modifiedRate=WtfGlobal.getIndividualProductPriceInMultiCurrency(rec,datewiseprice);
                                    
                            if(productuomid == obj.value){
                                obj.record.set("baseuomrate", 1);
                                obj.record.set("baseuomquantity", obj.record.get("quantity")*obj.record.get("baseuomrate"));
                                obj.record.set("rate",modifiedRate*baseuomrate); 
                                obj.record.set("rateIncludingGst", modifiedRate*baseuomrate);
                            }else{
                                obj.record.set("rate",modifiedRate*baseuomrate);
                                obj.record.set("rateIncludingGst", modifiedRate*baseuomrate);
                            }
//                            if(baseuomrate!=1){
//                                obj.record.set("rate",modifiedRate*quantity*baseuomrate);
//                                obj.record.set("rateIncludingGst", modifiedRate*quantity*baseuomrate);
//                            }
//                                    if(modifiedRate !=0){ //temp check for not clearing original value
//                                        obj.record.set("rate", modifiedRate);
//                                        obj.record.set("rateIncludingGst", modifiedRate);
//                                    }  

                            this.fireEvent('datachanged',this);
                              }, function(){

                                });
                        
                         }else{//for packeging UOM type
                       Wtf.Ajax.requestEx({
                                url:"ACCProduct/getIndividualProductPrice.do",
                                params:{
                                    productid:prorec.data.productid,
                                    affecteduser: this.affecteduser,
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
                                    obj.record.set("baseuomquantity", (obj.record.get("quantity"))*(prorec.data.caseuomvalue));
                                    obj.record.set("baseuomrate", (prorec.data.caseuomvalue));
                                    obj.record.set("rate", modifiedRate*prorec.data.caseuomvalue);
                                } else if(obj.value == prorec.data.inneruom) {
                                    obj.record.set("baseuomquantity", (obj.record.get("quantity"))*(prorec.data.inneruomvalue));
                                    obj.record.set("baseuomrate", (prorec.data.inneruomvalue));
                                    obj.record.set("rate", modifiedRate*prorec.data.inneruomvalue);
                                } else {
                                    obj.record.set("baseuomquantity", obj.record.get("quantity"));
                                    obj.record.set("baseuomrate", 1);
                                    obj.record.set("rate", modifiedRate);
                                }
                                
                            
                              }, function(){

                              });   
                      } 
                 }
//                  this.fireEvent('datachanged',this);
                 
            }
            if(obj.field=="productid" || obj.field=="pid" ){
                rec=obj.record;
                var index=this.productComboStore.find('productid',obj.value);
               if(this.isCustomer)
                    rec.set("changedQuantity",(rec.data.quantity*(-1))*rec.data.baseuomrate);
                else
                    rec.set("changedQuantity",(rec.data.quantity)*rec.data.baseuomrate);
                if(index>=0){
                    rec=this.productComboStore.getAt(index);
                    obj.record.set("description",rec.data["desc"]);
                    obj.record.set("isAsset",rec.data["isAsset"]);
                    obj.record.set("supplierpartnumber",rec.data["supplierpartnumber"]);
                    obj.record.set("shelfLocation",rec.data["shelfLocation"]);
                    obj.record.set("prtaxid", "");
                    obj.record.set("taxamount","");
//                    if(this.isFixedAsset){
//                        obj.record.set("quantity",0);
//                    }else{
                        obj.record.set("quantity",1);
//                    }
                    obj.record.set("baseuomquantity",1);
                    obj.record.set("baseuomrate",1);
                    obj.record.set("uomid", rec.data["uomid"]);
                    if(this.isCustomer)
                        obj.record.set("rate",rec.data["salespricedatewise"]);
                    else
                        obj.record.set("rate",rec.data["purchasepricedatewise"]);
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
                Wtf.Ajax.requestEx({
                    url:"ACCProduct/getIndividualProductPrice.do",
                    params:{
                        productid:this.productOptimizedFlag==Wtf.Products_on_Submit?productid:obj.value,
                        affecteduser: this.affecteduser,
                        transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                        carryin : (this.isCustomer)? false : true
                    }
                }, this,function(response){
                    var datewiseprice =response.data[0].price;
                    obj.record.set("oldcurrencyrate",1);
                    for(var i=1;i<response.data.length;i++){
                        var dataObj=response.data[i];
                        var key=dataObj.key;
                      for(var k=0;k<obj.grid.colModel.config.length;k++){
                            if(obj.grid.colModel.config[k].dataIndex!==undefined && obj.grid.colModel.config[k].dataIndex==key){
                                var store=obj.grid.colModel.config[k].editor.field.store;
                                if(store)
                                    store.clearFilter();
                                 obj.record.set(key,dataObj[key]);
                            }
                        }     
                    }
                    
                    var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.value, 'productid');
                    var productname = "";
                    var proddescription = "";
                    var productuomid = undefined;
                    var productsuppliernumber = "";
                    var shelfLocation = "";
                    var baseuomRate=1;
                    var prorec = null;
                    var acctaxcode = (this.isCustomer)?"salesacctaxcode":"purchaseacctaxcode";
                    var protaxcode = "";
                    var ComboIndex=0;
                    if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Products_on_type_ahead && productComboIndex==-1){
                        productComboIndex=1;
                        ComboIndex=-1;
                    }else if(this.productOptimizedFlag==Wtf.Products_on_Submit){
                        productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.value.trim(), 'pid');
                    }
                    if(productComboIndex >=0){
                        prorec = this.productComboStore.getAt(productComboIndex);
                        productname = prorec.data.productname;
                        proddescription = prorec.data.desc;
                        productuomid = prorec.data.uomid;
                        productsuppliernumber= prorec.data.supplierpartnumber;
                        shelfLocation = prorec.data.shelfLocation;
                        protaxcode = prorec.data[acctaxcode];
                    }
                    obj.record.set("desc",proddescription);
                    obj.record.set("uomid", productuomid);
                    obj.record.set("supplierpartnumber",productsuppliernumber);
                    obj.record.set("shelfLocation",shelfLocation);
                    obj.record.set("productname", productname);
                    obj.record.set("availableQtyInSelectedUOM", prorec.data['quantity']);
                    obj.record.set("uomname", prorec.data['uomname']);
                    obj.record.set("blockLooseSell", prorec.data['blockLooseSell']);
                    obj.record.set("pocountinselecteduom", prorec.data['pocountinselecteduom']);
                    obj.record.set("socountinselecteduom", prorec.data['socountinselecteduom']);
                    obj.record.set("isLocationForProduct", prorec.data['isLocationForProduct']);
                    obj.record.set("isWarehouseForProduct", prorec.data['isWarehouseForProduct']);
                    obj.record.set("isBatchForProduct", prorec.data['isBatchForProduct']);
                    obj.record.set("isSerialForProduct", prorec.data['isSerialForProduct']);
                    obj.record.set("isFromVendorConsign", prorec.data['isFromVendorConsign']);
                    obj.record.set("isRowForProduct", prorec.data['isRowForProduct']);
                    obj.record.set("isRackForProduct", prorec.data['isRackForProduct']);
                    obj.record.set("isBinForProduct", prorec.data['isBinForProduct']);  
                    obj.record.set("isAsset", prorec.data['isAsset']);  
                    obj.record.set("location", prorec.data["location"]);
                    obj.record.set("warehouse", prorec.data["warehouse"]);
                    
                    if(this.UomSchemaType==Wtf.UOMSchema){//for Schema type
                          productuomid = prorec.data.uomid;
                          obj.record.set("uomid", productuomid);
                          obj.record.set("baseuomname", prorec.data.uomname);
                   }else{//for packeging UOM type
                          productuomid =this.isCustomer? prorec.data.salesuom:prorec.data.purchaseuom;
//                          productuomname =this.isCustomer? prorec.data.salesuomname:prorec.data.purchaseuomname;
                           baseuomRate=this.isCustomer? prorec.data.stocksalesuomvalue:prorec.data.stockpurchaseuomvalue;
                           if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                                obj.record.set("uomid", productuomid);
                                obj.record.set("baseuomquantity", (obj.record.get("quantity"))*(baseuomRate));
                                obj.record.set("baseuomrate", (baseuomRate));
                            } else {
                                obj.record.set("baseuomquantity", (obj.record.get("quantity")));
                                obj.record.set("baseuomrate", 1);
                            }  
                            obj.record.set("caseuom", prorec.data['caseuom']);
                            obj.record.set("inneruom", prorec.data['inneruom']);
                            obj.record.set("stockuom", prorec.data['uomid']);
                            obj.record.set("caseuomvalue", prorec.data['caseuomvalue']);
                            obj.record.set("inneruomvalue", prorec.data['inneruomvalue']);
                  } 
                  
                    if(datewiseprice==0){
                        if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.addprice) && !this.isRequisition){//permissions
                            rec.set("productname",productname);
                            if (this.isCustomer) {
                                if (Wtf.account.companyAccountPref.unitPriceInDO) {
                                    Wtf.Msg.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pricefortheproduct") + "<b>" + productname + "</b>" + ' ' + WtfGlobal.getLocaleText("acc.field.isnotsetDoyouwanttosetitnow"),
                                            this.showPriceWindow.createDelegate(this, [rec, obj], true), this);
                                }
                            } else {
                                if (Wtf.account.companyAccountPref.unitPriceInGR) {
                                    Wtf.Msg.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pricefortheproduct") + "<b>" + productname + "</b>" + ' ' + WtfGlobal.getLocaleText("acc.field.isnotsetDoyouwanttosetitnow"),
                                            this.showPriceWindow.createDelegate(this, [rec, obj], true), this);
                                }
                            }
                            obj.record.set("rate", 0);
                                
                        }else{
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pricefortheproduct")+"<b>"+productname+"</b>"+' '+WtfGlobal.getLocaleText("acc.field.isnotset")], 2);
                        }
                    } else {
                        // setting datewise price according to currency exchange rate - 
                        
                        var rate=((obj.record==undefined||obj.record.data['currencyrate']==undefined||obj.record.data['currencyrate']=="")?1:obj.record.data['currencyrate']);
                        var oldcurrencyrate=((obj.record==undefined||obj.record.data['oldcurrencyrate']==undefined||obj.record.data['oldcurrencyrate']=="")?1:obj.record.data['oldcurrencyrate']);
                        var modifiedRate;
                        if(rate!=0.0)
                            modifiedRate=(parseFloat(datewiseprice)*parseFloat(rate))/parseFloat(oldcurrencyrate);
                        else
                            modifiedRate=(parseFloat(datewiseprice)/parseFloat(oldcurrencyrate));
                        
                        if(this.UomSchemaType==Wtf.UOMSchema){//for Schema type
                            obj.record.set("rate", modifiedRate);
                        }else{
                            obj.record.set("rate", modifiedRate*baseuomRate);
                        }
                    }
                    this.fireEvent('datachanged',this);
            }, function(){
                
            });   
//            if(this.isFixedAsset || this.isLeaseFixedAsset){
//                var productid = rec.get('productid');
//                var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, productid, 'productid');
//                if(productComboRecIndex >=0){
//                    var proRecord = this.productComboStore.getAt(productComboRecIndex);
//                    if(proRecord.get('isAsset') && obj.record.get("dquantity") != 0)
//                        this.callFixedAssetDetailsWindow(obj.record,proRecord);
//                }
//
//            }
            }else if(obj.field=="quantity"){
                rec=obj.record;               
                  if(((rec.data.isNewRecord=="" || rec.data.isNewRecord==undefined) && this.fromOrder&&!(this.editTransaction||this.copyInv))||(this.isEdit && rec.data.linkid !="")) {  
                   if(((obj.value > rec.data.remainingquantity && rec.data.remainingquantity!="")||(obj.value > rec.data.copyquantity && rec.data.copyquantity !="")) ||(obj.value > rec.data.copyquantity && this.isEdit)){  
                        var msg=this.isCustomer?WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinCRisexceedsfromoriginal"):WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinGRisexceedsfromoriginal");
                        obj.record.set(obj.field, obj.originalValue);
                         Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.common.alert"),msg,{
                                
                        },this)
                   }else if(((obj.value != rec.data.remainingquantity && rec.data.remainingquantity!="")||(obj.value != rec.data.copyquantity && rec.data.copyquantity !="")) ||(obj.value != rec.data.copyquantity && this.isEdit)) {
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"),this.isCustomer?WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinConDOisdifferentfromoriginal"):WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinGRisdifferentfromoriginal"),function(btn){
                            if(btn!="yes") {
                                obj.record.set(obj.field, obj.originalValue);
//                                obj.record.set("baseuomquantity",obj.originalValue*obj.record.get("baseuomrate"));
                                }else{
                                    obj.record.set(obj.field, obj.value);
                                    obj.record.set("dquantity", obj.value);
                                    obj.record.set("baseuomquantity",obj.value*obj.record.get("baseuomrate"));
                                }
                        },this)
                    }
                }
                if((obj.record.data["quantity"])==0){
                    this.store.remove(obj.record);
                }
//                var productComboIndex = WtfGlobal.searchRecordIndex(this.priceStore, obj.record.get('productid'), 'productid');
//                  var productuomid = "";
//                  if(productComboIndex >=0){
//                      prorec = this.priceStore.getAt(productComboIndex);
//                      productuomid = prorec.data.uomid;
//                      if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
//                            obj.record.set("baseuomquantity", obj.record.get("quantity")*obj.record.get("baseuomrate"));
//                      } else {
//                          obj.record.set("baseuomrate", 1);
//                          obj.record.set("baseuomquantity", obj.record.get("quantity")*obj.record.get("baseuomrate"));
//                      }
//                  }
            } else if(obj.field=="dquantity"){
                rec=obj.record;
                if(this.isCustomer)
                    rec.set("changedQuantity",(rec.data.copyquantity-obj.value)*((rec.data.copybaseuomrate=="")?1:rec.data.copybaseuomrate));
                else
                    rec.set("changedQuantity",(obj.value-rec.data.copyquantity)*((rec.data.copybaseuomrate=="")?1:rec.data.copybaseuomrate));                
                if(obj.record.data.dquantity > obj.record.data.quantity){
                    var msg = this.isCustomer ? WtfGlobal.getLocaleText("acc.field.Deliveredquantityshouldnotbegreaterthanactualquantity") : WtfGlobal.getLocaleText("acc.field.Receiptquantityshouldnotbegreaterthanactualquantity");
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg], 2);
                    obj.record.set("dquantity", obj.record.data.quantity);
                } else if(obj.record.data.dquantity <= 0){
                    var msg = this.isCustomer ? WtfGlobal.getLocaleText("acc.field.Deliveredquantityshouldnotbeequalorlessthanzero") : WtfGlobal.getLocaleText("acc.field.Receiptquantityshouldnotbeequalorlessthanzero");
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg], 2);
                    obj.record.set("dquantity", obj.record.data.quantity);
                }
                 var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');                
                  var ComboIndex=0;
                  var productuomid = "";
                  if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Products_on_type_ahead && productComboIndex==-1){
                    productComboIndex=1;
                    ComboIndex=-1;
                }
                  if(productComboIndex >=0){
                      prorec = this.productComboStore.getAt(productComboIndex);
                      // prorec = this.priceStore.getAt(productComboIndex);
                      if(ComboIndex==-1){
                          prorec=rec;
                      }
                      productuomid = prorec.data.uomid;
                      if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                            obj.record.set("baseuomquantity", obj.record.get("dquantity")*obj.record.get("baseuomrate"));
                      } else {
                          obj.record.set("baseuomrate", 1);
                          obj.record.set("baseuomquantity", obj.record.get("dquantity")*obj.record.get("baseuomrate"));
                      }
                  }
                  
                if(this.isFixedAsset || this.isLeaseFixedAsset){
                    var productid = rec.get('productid');
                    var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, productid, 'productid');
                    if(productComboRecIndex >=0){
                        var proRecord = this.productComboStore.getAt(productComboRecIndex);
                        if(proRecord.get('isAsset') && obj.record.get("dquantity") != 0)
                            this.callFixedAssetDetailsWindow(obj.record,proRecord);
                    }
                }
            }
        }        
        this.fireEvent('datachanged',this);
        if(this.store.getCount()>0&&this.store.getAt(this.store.getCount()-1).data['productid'].length<=0)
            return;
        if(!this.isNote)
            this.addBlankRow();
    },
    
    callFixedAssetDetailsWindow:function(record,productRec){
        
        var quantity = record.get('dquantity');
        
        this.FADetailsGrid=new Wtf.account.FADetails({
            title:'Asset Details',
            quantity:quantity,
            modal:true,
            isCustomer:this.isCustomer,
            isLeaseFixedAsset:this.isLeaseFixedAsset,
            layout:'border',
            assetRec:productRec,
            lineRec:record,
            assetDetailsArray:record.get('assetDetails'),
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            fromPO:this.fromPO,
            isGRCreatedByLinkingWithPI:this.isGRCreatedByLinkingWithPI,
            isEdit:this.isEdit,
            isFixedAsset:this.isFixedAsset,
            isFromOrder:true,
            width:950,
            readOnly:this.readOnly,
            height:500,
            resizable : false
        });
        
        this.FADetailsGrid.show();
        
        this.FADetailsGrid.on('beforeclose',function(panel){
            if(panel.isFromSaveButton){
                record.set("assetDetails", panel.assetDetails);
            }
        }, this);
    },
    
    calAmount:function(v,m,rec){
        var origionalAmount = rec.data.rate * rec.data.dquantity;
        rec.set("amount",origionalAmount);
        return WtfGlobal.withoutRateCurrencySymbol(origionalAmount,m,rec);
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
                var defaultLocation="";
            var defaultWarehouse="";
            if(this.movmentType!= undefined && this.movmentType != ""){
                if(obj.data.requestLocation!=undefined &&  obj.data.requestLocation!="" && obj.data.requestWarehouse!=undefined &&  obj.data.requestWarehouse!=""){
                    defaultLocation=obj.data.requestLocation;
                    defaultWarehouse=obj.data.requestWarehouse;
                }else {
                    defaultLocation="";
                    defaultWarehouse="";
                }
            }else{
                defaultLocation=prorec.data.location;
                defaultWarehouse=prorec.data.warehouse;
            }
          var islocationavailble=false;
          var productsDefaultLocation="";
        if(prorec.data.isLocationForProduct && defaultLocation!="" && defaultLocation!=undefined){
            islocationavailble=true;
            productsDefaultLocation=prorec.data.location;
        }else if(!prorec.data.isLocationForProduct){
            islocationavailble=true;
        }
        
        var iswarehouseavailble=false;
         var productsDefaultWarehouse="";
        if(prorec.data.isWarehouseForProduct && defaultWarehouse!="" && defaultWarehouse!=undefined){
            iswarehouseavailble=true;
            productsDefaultWarehouse=prorec.data.warehouse;
        }else if(!prorec.data.isWarehouseForProduct){
            iswarehouseavailble=true;
        }
            var filterJson='[';
              filterJson+='{"location":"'+productsDefaultLocation+'","warehouse":"'+productsDefaultWarehouse+'","productid":"'+prorec.data.productid+'","documentid":"","purchasebatchid":""},';
                    filterJson=filterJson.substring(0,filterJson.length-1);
             filterJson+="]";

      if(this.isCustomer && (islocationavailble || iswarehouseavailble) ){ //if salesside and either default location and warehouse  then checkit
                  Wtf.Ajax.requestEx({
                    url: "ACCInvoice/getBatchRemainingQuantity.do",
                    params: {
                        batchdetails:(this.isEdit && !this.copyTrans)?obj.data.batchdetails:filterJson,
                        transType:this.moduleid,
                        isEdit:this.isEdit
                    }
                },this,function(res,req){
                    this.AvailableQuantity=res.quantity;
                      this.CallSerialnoDetailsWindow(obj,defaultLocation,defaultWarehouse);
                      return;
                },function(res,req){
                    return false;
                });
            }else{
           this.CallSerialnoDetailsWindow(obj,defaultLocation,defaultWarehouse);
        }
       }
    },
  
    CallSerialnoDetailsWindow:function(obj,defaultLocation,defaultWarehouse){  
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
        var prorec=this.productComboStore.getAt(index);
        if(firstRow==-1){
                prorec=obj;
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
       
     this.batchDetailswin=new Wtf.account.SerialNoWindow({
            renderTo: document.body,
            title:WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber"),
            productName:prorec.data.productname,
            uomName:prorec.data.uomname,
            readOnly: this.readOnly,
            //quantity:obj.data.dquantity,
            quantity:(obj.data.baseuomrate)*deliveredprodquantity,
            billid:obj.data.billid,
            defaultLocation:defaultLocation,
            productid:prorec.data.productid,
            isSales:this.isCustomer,
            isLinkedFromSO:isLinkedFromSO,
            isLinkedFromCI:isLinkedFromCI,
            moduleid:this.moduleid,
            transactionid:(this.isCustomer)?4:5,
            isDO:this.isCustomer?true:false,
            movmentType:this.movmentType,
            defaultWarehouse:defaultWarehouse,
            requestWarehouse:obj.data.requestWarehouse,
            requestLocation:obj.data.requestLocation,
            batchDetails:obj.data.batchdetails,
            warrantyperiod:prorec.data.warrantyperiod,
            warrantyperiodsal:prorec.data.warrantyperiodsal,  
            isLocationForProduct:prorec.data.isLocationForProduct,
            isWarehouseForProduct:prorec.data.isWarehouseForProduct,
            isBatchForProduct:prorec.data.isBatchForProduct,
            isSerialForProduct:prorec.data.isSerialForProduct,
            isSKUForProduct:prorec.data.isSKUForProduct,
            isRowForProduct:prorec.data.isRowForProduct,
            isRackForProduct:prorec.data.isRackForProduct,
            isBinForProduct:prorec.data.isBinForProduct,
            isConsignment:false,//as we not loading any any consignment type batch serial details in CDO. SO Need this as False
            linkflag:(obj.data.linkflag)?obj.data.linkflag:this.fromPO,
            defaultAvailbaleQty:this.AvailableQuantity,
            isblokedinso:obj.data.islockQuantityflag,
            documentid:obj.data.dorowid,
            isEdit:this.isEdit,
            isItemReusable:1,
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
                if(obj.data.isSerialForProduct && obj.data.islockQuantityflag && this.moduleid == Wtf.Acc_ConsignmentDeliveryOrder_ModuleId){
                 //getting Batch details of row for serial change
                  Wtf.Ajax.requestEx({
                    url: "ACCSalesOrderCMN/getSalesOrderRowBatchJSON.do",
                    params: {
                        productid:prorec.data.productid,
                        documentid:((((obj.data.linkflag)?obj.data.linkflag:this.fromPO)) && this.isEdit== false)?obj.data.dorowid:"",
                        transType:this.moduleid,
                        moduleid:this.moduleid,
                        isEdit:this.isEdit,
                        linkingFlag:obj.data.linkflag,
                        isConsignment:false
                    }
                },this,this.genSuccessResponseReplaceSerial,this.genFailureResponseReplaceSerial);

                }   
            }
             },this);                       
        this.batchDetailswin.show();       
        }
},
 genSuccessResponseReplaceSerial : function(response){
         var SerialReplaceArr=[];
         var presentCount=0;
         var reQty=0;
         var changeQty=0;
         var jsonBatchDetails= eval(this.batchDetails);
         for(var i=0;i<response.data.length;i++){
             var isSerialPresent=false;
             var serialName=response.data[i].serialno;
             reQty=response.data[0].quantity;
                for(var k=0;k<jsonBatchDetails.length;k++){
                   
                var srno=jsonBatchDetails[k].serialno;
                changeQty=jsonBatchDetails[0].quantity;
                srno=decodeURI(srno);
                if(serialName ==srno){
                    isSerialPresent=true;
                    presentCount++;
                    break;
                }
            }
                if(isSerialPresent==false){
                    SerialReplaceArr.push(response.data[i]);
                }    
         }
         var serialchangeCount=(jsonBatchDetails.length)-presentCount;
         if(response.data.length > 0 && serialchangeCount > 0 && SerialReplaceArr.length > 0){
             var serilReplaceWin = Wtf.getCmp('SerialReplaceWindow'); //+selectedRec[0].data.itemid
            if(serilReplaceWin == null){
                serilReplaceWin = new Wtf.SerialRepalceWindow({
                    id : 'SerialReplaceWindow',  //+selectedRec[0].data.itemid
                    border : false,
                    title: WtfGlobal.getLocaleText("acc.replaceserialwin.title"), // +" - "+selectedRec[0].data.itemcode,  
                    serialchangeCount:serialchangeCount,
                    SerialReplaceArr:SerialReplaceArr,
                    moduleid:this.moduleid,
                    scope:this,
                    closable: false,
                    modal: true,
                    iconCls :getButtonIconCls(Wtf.etype.deskera),
                    resizable: false,
                    renderTo: document.body
                //            iconCls:getButtonIconCls(Wtf.etype.inventoryval)
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
    checkRow:function(obj){
        var rec=obj.record;
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
            }else if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag!=Wtf.Show_all_Products && prorec==undefined){
                prorec = obj.record;
            }
        }
        if(obj.field=="productid" || obj.field=="pid"){
            
            var isProductAlreadySelected = false;
            
            this.getStore().each(function(recr){
                var prodId = recr.get('productid');
                if(prodId == obj.value){
                    isProductAlreadySelected = true;
                    return false;
                }
            },this);
            
            var productText = "Product";
            if(this.isFixedAsset){
                productText = "Asset Group";
            }
         
            if(isProductAlreadySelected){
                WtfComMsgBox(['Information',productText+' Already Selected'],0);
                obj.record.set('productid','');
                return false;
            }
            
            var index=this.productComboStore.findBy(function(rec){
                if(rec.data.productid==obj.value)
                    return true;
                else
                    return false;
            })
            var prorec=this.productComboStore.getAt(index);
            var useStoreRec=false;
            if(prorec==undefined){
                prorec= rec;
                useStoreRec=true;
            }
            index=this.productComboStore.find('productid',obj.value)
            rec=this.productComboStore.getAt(index);
            if(this.editTransaction){  //In Edit Case Check product quantity is greater than available quantity when selecting product
                var availableQuantity = prorec.data.quantity;    //This is in base UOM
                if(useStoreRec){
                    availableQuantity = prorec.data.availablequantity;
                }
                var lockQuantity = prorec.data.lockquantity; 
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
                    quantity = quantity + (obj.record.data['dquantity']*obj.record.data['baseuomrate']);
                if((availableQuantity-lockQuantity)<quantity){
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productname']+' is '+availableQuantity], 2);
//                        obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+WtfGlobal.getLocaleText("acc.field.is")+(availableQuantity-lockQuantity)+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                          rec.set("quantity",obj.originalValue);
                          rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
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
                //WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.76")+" "+rec.data['productname']], 2);
                //obj.cancel=true;
                }else if(this.isCustomer&&availableQuantity<(obj.record.data['dquantity']*obj.record.data['baseuomrate'])&& prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation){
                    this.isValidEdit = false;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productname']+' is '+availableQuantity], 2);
                    obj.cancel=true;
                } 
            }else{ //In normal Case Check product quantity is greater than available quantity when selecting product
                if(this.isCustomer&&this.store.find("productid",obj.value)>-1 && rec.data.type!='Service' && rec.data.type!='Non-Inventory Part' &&!this.isQuotation){
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
                    quantity = quantity + obj.record.data['dquantity'];
                    if(rec.data['quantity']<quantity){
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productname']+' is '+rec.data['quantity']], 2);
//                        obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productname']+WtfGlobal.getLocaleText("acc.field.is")+rec.data['quantity']+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
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
                //WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.76")+" "+rec.data['productname']], 2);
                //obj.cancel=true;
              }else if(this.isCustomer&&((rec.data['quantity'])-(rec.data['lockquantity']))<obj.record.data['dquantity']&& prorec.data.type!="Service" && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation){
                    this.isValidEdit = false;
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productname']+' is '+rec.data['quantity']], 2);
//                    obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productname']+WtfGlobal.getLocaleText("acc.field.is")+((rec.data['quantity'])-(rec.data['lockquantity']))+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                          rec.set("quantity",obj.originalValue);
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
        }else if(this.isCustomer&&(obj.field=="dquantity"||obj.field=="baseuomrate")&&obj.record.data['productid'].length>0&&!this.isQuotation){  
            if(obj.field=="dquantity") {
                var originalDquantity = obj.originalValue;
                var newDquantity = obj.value;
                var originalBaseuomrate = obj.record.data['baseuomrate'];
                var newBaseuomrate = obj.record.data['baseuomrate'];
            } else if(obj.field=="baseuomrate") {
                var originalDquantity = obj.record.data['dquantity'];
                var newDquantity = obj.record.data['dquantity'];
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
                    if(islockQuantityflag)   //if DO is lonked with SO then we will consider quantity (available quantity-lock quantity excluding its own quantity
                    {
                        if((availableQuantity-(lockQuantity-soLockQuantity)) < quantity) {
                            if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' '+WtfGlobal.getLocaleText("acc.field.is")+(availableQuantity-(lockQuantity-soLockQuantity))+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);                      
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

                    }else if((availableQuantity-lockQuantity) < quantity) {  //for normal check for all products available quantity
                    availableQuantity = availableQuantity + copyquantity;
                    if((availableQuantity-lockQuantity) < quantity) {
                 //                    if(availableQuantity < quantity) {
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' is '+availableQuantity], 2);
//                        obj.cancel=true;                        
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+WtfGlobal.getLocaleText("acc.field.is")+(availableQuantity-lockQuantity)+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
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
                      if(islockQuantityflag)   //if DO is lonked with SO then we will consider quantity (available quantity-lock quantity excluding its own quantity
                    {
                        if((availableQuantity-(lockQuantity-soLockQuantity)) < quantity) {
                            if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' '+WtfGlobal.getLocaleText("acc.field.is")+(availableQuantity-(lockQuantity-soLockQuantity))+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);                      
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

                    }else   if((availableQuantity-lockQuantity) < quantity) {  //for normal check for all products available quantity
//                    if(availableQuantity < quantity) {
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' is '+availableQuantity], 2);
//                        obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+WtfGlobal.getLocaleText("acc.field.is")+(availableQuantity-lockQuantity)+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);                      
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
    loadPOProduct:function(){              
                          
      if(this.isCustomer && this.fromOrder && !this.isNote && !this.readOnly && !this.isOrder && !this.editTransaction)
    	  this.checkSOLinkedProducts();
    },

    checkSOLinkedProducts:function(){      
    	var msgBox = 0,msg="";
         var recordSet=[];
          this.isNegativeStock=false;
        if(this.store.data.length){ //Check Qty mentioned in SO/CI is greater than available quantity
            //To do - Need to check quantity checks for multi UOM change
            var avaiableStockRowCount=0;
            var storeData = []; 
            storeData =this.store.data.items;
            this.store.removeAll();
            for(var count=0;count<storeData.length;count++){
                var record=storeData[count];
                  recordSet[count]=record;
                var quantity = 0;
                if(this.isConsignment && this.isCustomer){
                    record.data.remark = record.data.Custom_Remark;
                }
                this.store.each(function(rec){
                  if(rec.data.productid == record.data.productid&&rec.data.requestLocation==record.data.requestLocation){
                        quantity = quantity + (rec.data.dquantity*rec.data.baseuomrate);                                                     
                  }
                },this);
                quantity = quantity + (record.data.dquantity*record.data.baseuomrate);
                var result = this.productComboStore.find('productid',record.data.productid);
                if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag!= Wtf.Show_all_Products){ //&& result==-1
                  prorec=record;
                     if(Wtf.account.companyAccountPref.isnegativestockforlocwar && (prorec.data.isLocationForProduct || prorec.data.isWarehouseForProduct ) && !prorec.data.isBatchForProduct && !prorec.data.isSerialForProduct && !prorec.data.isRowForProduct && !prorec.data.isRackForProduct && !prorec.data.isBinForProduct){
                    // Skip the check for the quantity in cas either Location &  warehouse is on for the prodct provided rpw/rack/bim is not activated 
                            this.store.add(record);
                            avaiableStockRowCount++;    
                     }else if(!this.editTransaction && prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part'  && quantity > prorec.data.availableQtyInSelectedUOM && !Wtf.account.companyAccountPref.requestApprovalFlow){
                            if(msg==""){
                                msg=record.data.productname+" in "+record.data.billno+" is "+prorec.data.availableQtyInSelectedUOM+".";
                            }else{
                                msg=msg+","+record.data.productname+" in "+record.data.billno+" is "+prorec.data.availableQtyInSelectedUOM+".";
                            };
                            msgBox = 1;
                            }else if(!this.editTransaction && prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part'  && quantity  > record.data.prodavlqty && Wtf.account.companyAccountPref.requestApprovalFlow){
                             if(msg==""){
                                msg=record.data.productname+" in "+record.data.billno+" is "+record.data.prodavlqty+".";
                            }else{
                                msg=msg+","+record.data.productname+" in "+record.data.billno+" is "+record.data.prodavlqty+".";
                            };
                            msgBox = 1;
                        }
                        else{
                            this.store.add(record);
                            avaiableStockRowCount++;
                        }
                }else if(result >= 0){
                    var prorec=this.productComboStore.getAt(result);////if product type is of Inventory then check otherwise no need to check 
                          if(Wtf.account.companyAccountPref.isnegativestockforlocwar && (prorec.data.isLocationForProduct || prorec.data.isWarehouseForProduct ) && !prorec.data.isBatchForProduct && !prorec.data.isSerialForProduct && !prorec.data.isRowForProduct && !prorec.data.isRackForProduct && !prorec.data.isBinForProduct){
                          // Skip the check for the quantity in cas either Location &  warehouse is on for the prodct provided rpw/rack/bim is not activated 
                            this.store.add(record);
                            avaiableStockRowCount++;    
                     }else if(!this.editTransaction && prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part'  && quantity > prorec.data.quantity && !Wtf.account.companyAccountPref.requestApprovalFlow){
                            if(msg==""){
                                msg=record.data.productname+" in "+record.data.billno+" is "+prorec.data.quantity+".";
                            }else{
                                msg=msg+","+record.data.productname+" in "+record.data.billno+" is "+prorec.data.quantity+".";
                            };
                            msgBox = 1;
                        }else if(!this.editTransaction && prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part'  && quantity > record.data.prodavlqty && Wtf.account.companyAccountPref.requestApprovalFlow){
                             if(msg==""){
                                msg=record.data.productname+" in "+record.data.billno+" is "+record.data.prodavlqty+".";
                            }else{
                                msg=msg+","+record.data.productname+" in "+record.data.billno+" is "+record.data.prodavlqty+".";
                            };
                            msgBox = 1;
                        }
                        else{
                            this.store.add(record);
                            avaiableStockRowCount++;
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
              if(avaiableStockRowCount == 0 && this.isConsignment){
                info+=" "+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+"</center>"
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),info], 2);
                this.isNegativeStock=true;
                this.store.removeAll();                        
                         
            }else if(avaiableStockRowCount > 0 && this.isConsignment){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),info], 2);
                    this.isNegativeStock=false;
            }else {
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
        }
    },
    loadPOGridStore:function(recids,linkingFlag,isForDOGROLinking){                
        WtfGlobal.setAjaxTimeOut();
        this.store.load({params:{bills:recids,mode:43,closeflag:true,doflag:true,linkingFlag:linkingFlag,isForLinking:linkingFlag,isConsignment:this.isConsignment,moduleid:this.moduleid, isForDOGROLinking:isForDOGROLinking}});//,moduleid:this.moduleid
        
        this.store.on("load",function(){
        WtfGlobal.resetAjaxTimeOut();
    },this);
//        this.store.on('load',function(store1, recArr){     
//            var tempStore=this.productComboStore;
//            this.store.removeAll();
//            if(!this.isNegativeStock){
//            for(var count=0;count<recArr.length;count++){
//                var record=recArr[count];
//                if(record.data.dquantity==""){
//                    record.data.dquantity=record.data.quantity;
//                }
//                if(record.data.description==""){
//                    var prorec=tempStore.getAt(tempStore.find('productid',record.data.productid));                                        
//                    if(prorec!= undefined){
//                       record.set('description',prorec.data.desc);
//                    }
//                }
//                    this.store.add(record);   
//            } 
//          } 
//           this.addBlankRow();   
//        },this);
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
                rec.data[CUSTOM_FIELD_KEY_PRODUCT]=Wtf.decode(WtfGlobal.getCustomColumnDataForProduct(rec.data, this.moduleid).substring(20));
                arr.push(this.store.indexOf(rec));
            }
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
    }
});