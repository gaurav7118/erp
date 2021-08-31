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
Wtf.account.FixedAssetProductDetailsGrid=function(config){
    this.isFixedAsset=(config.isFixedAsset)?config.isFixedAsset:false;
    this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
    this.isLeaseFixedAsset=(config.isLeaseFixedAsset)?config.isLeaseFixedAsset:false;
    this.isLinkedFromReplacementNumber=(config.isLinkedFromReplacementNumber)?config.isLinkedFromReplacementNumber:false;
    this.isLinkedFromCustomerQuotation=(config.isLinkedFromCustomerQuotation)?config.isLinkedFromCustomerQuotation:false;
    this.parentCmpID=config.parentCmpID;
    this.isCustomer=config.isCustomer;
    if(this.productOptimizedFlag!= undefined || this.productOptimizedFlag==Wtf.Show_all_Products){
        this.productComboStore=(this.isFixedAsset)?Wtf.FixedAssetStore:(this.isCustomer?((this.isLeaseFixedAsset)?Wtf.FixedAssetAndProductLeaseStore:Wtf.productStoreSales):Wtf.productStore);
    }else if(this.productOptimizedFlag==Wtf.Products_on_type_ahead || this.productOptimizedFlag==Wtf.Products_on_Submit){
        this.productComboStore=(this.isFixedAsset)?Wtf.FixedAssetStoreOptimized:(this.isCustomer?((this.isLeaseFixedAsset)?Wtf.FixedAssetAndProductLeaseStore:Wtf.productStoreSales):Wtf.productStore);
    }
    this.currencyid=config.currencyid;
    this.productID=null;
    this.soLinkFlag = null;
    this.updaterowtax = true;//used in conjunction with this.soLinkFlag
    this.id=config.id;
    this.isOrder=config.isOrder;
    this.isEdit=config.isEdit;
    this.isGRLinkedInPI=config.isGRLinkedInPI?config.isGRLinkedInPI:false;
//    this.isGST=true;//config.isGST;   // ERP-32829 
    this.isGST=WtfGlobal.GSTApplicableForCompany()==Wtf.GSTStatus.NEW?true:false;   //ERP-32829 
    this.isFromGrORDO=(config.isFromGrORDO != null || config.isFromGrORDO != undefined)?config.isFromGrORDO:false;
    this.record=config.record;
    this.billDate=(this.isEdit && this.record.data.date != undefined)? this.record.data.date :new Date();
    this.dateChange=false;
    this.pronamearr=[];
    this.isCashType=config.isCash;
    this.gridConfigId="";
    this.isInvoice=config.isInvoice;
    this.fromPO=config.fromPO;          
    this.linkedFromOtherTransactions=false; // if this transaction is being linked to any other transaction
    this.readOnly=config.readOnly;
    this.copyInv=config.copyInv;
    this.editTransaction=config.editTransaction;
    this.editLinkedTransactionQuantity= Wtf.account.companyAccountPref.editLinkedTransactionQuantity;
    this.editLinkedTransactionPrice= Wtf.account.companyAccountPref.editLinkedTransactionPrice;
    this.noteTemp=config.noteTemp;
    this.fromOrder=config.fromOrder;
    this.affecteduser = "";
    if(config.isNote!=undefined)
        this.isNote=config.isNote;
    else
        this.isNote=false;
    this.isCN=config.isCN;
    this.moduleid = config.moduleid;
    this.isViewCNDN=config.isViewCNDN;
    this.isQuotation=config.isQuotation;
    this.isRequisition =config.isRequisition;
    this.isQuotationFromPR =config.isQuotationFromPR;
    this.isPIFromPO = config.isPIFromPO;
    this.isPIFromVQ = config.isPIFromVQ;
    this.isPOfromVQ = config.isPOfromVQ;
    this.isRFQ =config.isRFQ;
    this.isLinkedTransaction= (config.parentObj.isLinkedTransaction == null || config.parentObj.isLinkedTransaction == undefined)? false : config.parentObj.isLinkedTransaction;
    this.createStore();
    this.createComboEditor();
    this.createColumnModel();
    var colModelArray = [];
    colModelArray = GlobalColumnModel[this.moduleid];
    if(colModelArray) {
        colModelArray.concat(GlobalColumnModelForProduct[this.moduleid]);
    }
    WtfGlobal.updateStoreConfigStringDate(colModelArray, this.store);
    this.duplicateStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },Wtf.productRec)
    });
    Wtf.account.FixedAssetProductDetailsGrid.superclass.constructor.call(this,config);
    this.addEvents({
        'datachanged':true, //Event fired when grid data is changed. Mostly used to calculate subtotal
        'pricestoreload':true,
        'productselect' : true,//Event fired to load data for collapsible panel store
        'productdeleted' : true,//Event fired to remove data for collapsible panel store
        'customerchangepriceload' : true, //Event fired when customer is changed
        'gridconfigloaded':true//Event fire when WtfGlobal.getGridConfig() called and config get applied to grid column
    });
    this.on('populateDimensionValue',this.populateDimensionValueingrid,this);
}
Wtf.extend(Wtf.account.FixedAssetProductDetailsGrid,Wtf.grid.EditorGridPanel,{
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
         Wtf.account.FixedAssetProductDetailsGrid.superclass.onRender.call(this,config);
         this.isValidEdit = true;
        
        if(Wtf.userds)
            Wtf.userds.load();
        if(Wtf.locationStore)
            Wtf.locationStore.load();
        if(Wtf.detartmentStore)
            Wtf.detartmentStore.load();
         
//        this.on('render',this.addBlankRow,this);
        WtfGlobal.getGridConfig(this,this.moduleid,true,true);
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
            
           /*
             * IF Document is linked in other document then don't allow edit below fields.
             */
            if(this.isLinkedTransaction && (e.field == "productid" || e.field == "quantity" || e.field == "rate" || e.field == "prdiscount" || e.field == "discountispercent" || e.field=="taxamount" || e.field=="prtaxid")){
                e.cancel=true;
            }
            
         if(this.isLeaseFixedAsset){
             if(this.isInvoice && this.isCustomer){// you can create lease sales invoice only after selecting LDO
                 if(this.fromPO == undefined || this.fromPO == null || !this.fromPO){
                    e.cancel=true;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.fixed.asset.invoice.lease") ], 4);
                    return;
                }
             }
         }
         
         if((this.isFixedAsset || this.isLeaseFixedAsset) && this.linkedFromOtherTransactions){
            if(this.store.getCount()-1==e.row){// if last row then don't allow edition, and appending row in case of linking'
                e.cancel=true;
                return;
            }
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
            if (e.field == "prtaxid") {
                //SDP-15131
                if (this.parentObj.capitalGoodsAcquired && this.parentObj.capitalGoodsAcquired.getValue()) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.cgamsg")], 2);
                    e.cancel = true;
                    return false;
                }
            }
        if(e.field == "taxamount" && this.parentObj && this.parentObj.includingGST && this.parentObj.includingGST.getValue()){
            e.cancel=true;
        }
         var isRateFieldEditable = true;
        
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
                    }else if(this.isOrder && !this.isQuotation){
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
                    if(this.productOptimizedFlag== undefined || this.productOptimizedFlag==Wtf.Show_all_Products){  
                        e.cancel = true;
                    }else{
                        if(e.record.data ==undefined || e.record.data.productid == undefined || e.record.data.productid ==""){ 
                            e.cancel = true;
                        }
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
            
            var isQuantityFieldEditable = true;
            if((e.field == "quantity")&& ((e.record.data.isNewRecord =="" && !this.isEdit ) ||(e.record.data.linkid !="" && this.isEdit))){//isNewRecord for normal records is "1"
                 if(this.editLinkedTransactionQuantity && (((this.fromPO||(this.isEdit && this.fromPO==false)) && !this.isCustomer && (this.soLinkFlag==false||(this.isEdit && this.soLinkFlag==null )) )||((this.isOrder||this.isQuotation) && this.isCustomer && (this.soLinkFlag==false||(this.isEdit && this.soLinkFlag==null)) )||(!this.isOrder && this.isCustomer && (this.soLinkFlag==false||(this.isEdit && this.soLinkFlag==null))))&& !this.isRequisition ){  //|| (this.fromOrder && this.isCustomer)
                      e.cancel = true;
                      isQuantityFieldEditable = false;
                 }
             }
             if(e.field == "quantity" && !this.isFromGrORDO && isQuantityFieldEditable){
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
            if(e.record.data !=undefined && e.record.data.productid != undefined && e.record.data.productid !=""){   
            Wtf.Ajax.requestEx({
                url: "ACCProduct/getIndividualProductPrice.do",
                params: {
                    productid: e.record.data.productid,
                    affecteduser: this.affecteduser,
                    currency: this.parentObj.Currency.getValue(),
                    quantity: e.record.data.quantity,
                    transactiondate: WtfGlobal.convertToGenericDate(this.billDate),
                    carryin: (this.isCustomer) ? false : true
                }
            }, this, function(response) {
               if(response.data){
                for (var i = 1; i < response.data.length; i++) {
                    var dataObj = response.data[i];
                    var key = dataObj.key;
                    var custValue = dataObj[key];
                    for (var k = 0; k < e.grid.colModel.config.length; k++) {

                        if (e.field == key) {
                            var store = e.grid.colModel.config[6].editor.field.store;
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
             
            }
         },this);
         if(!this.isNote && !this.readOnly){
	         if(this.record == null || this.record == undefined && this.getColumnModel().getColumnById(this.id+"prtaxid").hidden == undefined && this.getColumnModel().getColumnById(this.id+"taxamount").hidden == undefined){
	        	 
                         this.getColumnModel().setHidden(this.getColumnModel().getIndexById(this.id+"prtaxid"), true);				// 21241   If statement added bcos could not use the event destroy for column model
	        	 this.getColumnModel().setHidden(this.getColumnModel().getIndexById(this.id+"taxamount"), true);							// and also could not call the createColumnModel() method from onRender
	         }
         }
         if(this.isLinkedTransaction){
                this.productEditor.setDisabled(true);
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
         }
         WtfGlobal.hideShowCustomizeLineFields(this,this.moduleid);
     },
    saveGridStateHandler: function(grid,state){
        if(!this.readOnly){
            WtfGlobal.saveGridStateHandler(this,grid,state,this.moduleid,this.gridConfigId,true);
        }
    }, 
    populateDimensionValueingrid: function(rec) {
        WtfGlobal.populateDimensionValueingrid(this.moduleid, rec, this);
    },
     getPostTextEditor: function(e)
    {
        var _tw=new Wtf.EditorWindowQuotation({
            val:e.record.data.desc,
            id:"abcd"
        });
    //	this.remark.focus.defer(150,true);
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
            {name: 'leaf'},
            {name: 'type'},
            {name:'prtaxid'},
            {name:'taxamount'},
            {name:'prtaxpercent'},
            {name:'prtaxname'},
        //        {name: 'currencysymbol'},
        //        {name: 'currencyrate'},
            {name: 'level'},
            {name: 'initialquantity',mapping:'initialquantity'},
            {name: 'initialprice'},
            {name:'shelfLocation'},
            {name: 'producttype'}
        ]);

        this.priceStore = new Wtf.data.Store({
        //        url:Wtf.req.account+'CompanyManager.jsp',
            url:"ACCProduct/getProductsForCombo.do",
            baseParams:{mode:22
//            	loadInventory:this.isCustomer
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
            {name:'Cust_billno'},
            {name:'productid'},
            {name:'desc'},
            {name:'quantity'},
            {name:'baseuomquantity',defValue:1.00},
            {name:'uomname'},
            {name:'uomid'},
            {name:'baseuomrate',defValue:1.00},
            {name:'copyquantity',mapping:'quantity'},
            {name:'rate',defValue:0},
            {name:'rateIncludingGst'},
            {name:'isRateIncludingGstEnabled',defValue:'0'},
            {name:'unitPricePerInvoice',defValue:0},
            {name:'rateinbase'},
            {name:'partamount',defValue:0},
            {name:'discamount'},
            {name:'discount'},
            {name:'discountispercent',defValue:1},
            {name:'prdiscount',defValue:0},
            {name:'invstore'},
            {name:'invlocation'},
            {name:'taxclass'},
            {name:'taxclasshistoryid'},
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
            {name:'typeid',defValue:0},
            {name:'isNewRecord',defValue:'1'},
            {name:'producttype'},
            {name:'permit'},
            {name:'linkto'},
            {name:'linkid'},
            {name:'linkDate'},//To compare date for future document when linked.
            {name:'linktype'},
            {name:'savedrowid'},
            {name:'originalTransactionRowid'},
            {name:'changedQuantity'},
            {name:'approvedcost'},
            {name:'approverremark'},
            {name:'customfield'},
            {name:'gridRemark'},
            {name:'productcustomfield'},
            {name:'accountId'},
            {name:'salesAccountId'},
            {name:'discountAccountId'},
            {name:'rowTaxAmount'},
            {name:'type'},                        
            {name:'shelfLocation'},
            {name:'productcustomfield'},
            {name:'supplierpartnumber'},
            {name:'assetDetails'},
            {name:'profitLossAmt'},
            {name:'copybaseuomrate',mapping:'baseuomrate'},  //for handling inventory updation 
            {name:'priceSource'},
            {name:'pricingbandmasterid'},
            {name:'recTermAmount'},
            {name:'LineTermdetails'},
            {name:'pid'},
            {name:'isAsset',type:'boolean',defValue:false},
            {name: 'srno', isForSequence:true},
            {name:'hasAccess'},
            {name:'israteIncludingGst'},
            {name:'lineleveltaxtermamount',defValue:0.0},
            {name: 'isUserModifiedTaxAmount', defValue: false},
            {name:'prodtype'},
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
        this.productId= new Wtf.form.TextField({
            name:'pid'
        });
        this.poProductStore = new Wtf.data.Store({
            //url:Wtf.req.account+(this.isCustomer?'CustomerManager.jsp':'VendorManager.jsp'),
            url:this.isCustomer?'ACCSalesOrderCMN/getSalesOrderRows.do':'ACCPurchaseOrderCMN/getPurchaseOrderRows.do',
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.poProductRec)
        });
//        chkproductload();
            this.productComboStore.on('beforeload',function(s,o){
                if(!o.params)o.params={};
                var currentBaseParams = this.productComboStore.baseParams;
                currentBaseParams.getSOPOflag=true;
                currentBaseParams.onlyProduct=this.isOrder?false:true
                currentBaseParams.startdate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true));
                currentBaseParams.enddate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false));
                currentBaseParams.searchProductString = (this.productOptimizedFlag==Wtf.Products_on_Submit && !this.isProductLoad)? this.productId.getValue():"";
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
                if(this.ProductloadingMask)
                    this.ProductloadingMask.hide();
        },this);
        this.productComboStore.on("loadexception",function(){
            if(this.ProductloadingMask)
                this.ProductloadingMask.hide();
        },this);
        
        if(this.productOptimizedFlag== undefined || this.productOptimizedFlag==Wtf.Show_all_Products){
            if(this.isCustomer)
                chkproductSalesload();
            else
                chkproductload();                                        
            this.productEditor=new Wtf.form.ExtFnComboBox({
                name:(this.isLeaseFixedAsset)? 'pid' : 'productname',
                store:this.productComboStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
                typeAhead: true,
                isProductCombo: true,
                selectOnFocus:true,
                maxHeight:250,
                listAlign:"bl-tl?",//[ERP-5149] To expand list of combobox always on top. 
                valueField:'productid',
                displayField:'pid',
                extraFields:(this.isLeaseFixedAsset)?['productname','type']:['productname','type'],
                extraComparisionField:'pid',// type ahead search on acccode as well.
                extraComparisionFieldArray:['pid','productname'], //search on both pid and name
                listWidth:400,
                lastQuery:'',
//              extraComparisionField:'pid',// type ahead search on pid as well.
                //editable:false,
                scope:this,
                hirarchical:true,
                // addNewFn:this.openProductWindow.createDelegate(this),
                forceSelection:true,
                isProductCombo:this.isLeaseFixedAsset
            });
        }else{
            this.productEditor=new Wtf.form.ExtFnComboBox({
                name:(this.isLeaseFixedAsset)? 'pid' : 'productname',
                store:this.productComboStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
                typeAhead: true,
                isProductCombo: true,
                selectOnFocus:true,
                maxHeight:250,
//                lastQuery: '',
                listAlign:"bl-tl?",//[ERP-5149] To expand list of combobox always on top. 
                valueField:'productid',
                displayField:(this.isLeaseFixedAsset)? 'pid' : 'productname',
                extraFields:(this.isLeaseFixedAsset)?['productname','type']:['pid','type'],
                extraComparisionField:'pid',// type ahead search on acccode as well.
                extraComparisionFieldArray:['pid','productname'], //search on both pid and name
                listWidth:400,
                scope:this,
                hirarchical:true,
                mode:'remote',
                hideTrigger:true,
                triggerAction : 'all',
                editable : true,
                minChars : 2,
                hideAddButton : true,//Added this Flag to hide AddNew  Button  
                addNewFn:!(this.isLeaseFixedAsset)?this.openProductWindow.createDelegate(this):undefined,
                forceSelection:true,
                isProductCombo:this.isLeaseFixedAsset
            });
        }
            this.productEditor.on('beforeselect', function(combo, record, index) {
                return validateSelection(combo, record, index);
            }, this);
        if(this.productOptimizedFlag!=Wtf.Products_on_Submit){
            this.productEditor.on("blur",function(e,a,b){
                if(Wtf.account.companyAccountPref.invAccIntegration && !this.isCustomer && !this.isQuotation){                
                    e.store=this.productComboStore;
                }    
            },this);
        }
//        if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.edit) && !this.isRequisition && !this.isRFQ)
//            this.productEditor.addNewFn=this.openProductWindow.createDelegate(this);
        chkUomload();
        this.uomEditor=new Wtf.form.FnComboBox({
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
        this.noteTypeRec = new Wtf.data.Record.create([
           {name: 'typeid'},
           {name: 'name'},
        ]);

//        this.typeStore = new Wtf.data.SimpleStore({
//            fields: [{name:'typeid',type:'int'}, 'name'],
//            data :[[0,'Normal'],[1,'Defective'],[2,'Return'],[3,'Defective Return'] ]
//        });

        this.typeStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.noteTypeRec),
//            url: Wtf.req.account + 'CompanyManager.jsp',
            url: "ACCCreditNote/getNoteType.do",
            baseParams:{
                mode:31,
                combineData:-1  //Send For Seprate Request
            }
        });
        this.typeStore.load();

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
            {name:'acccode'},
            {name:'groupname'}
//            {name:'level',type:'int'}
        ]);
        this.accountStore = new Wtf.data.Store({
            url:"ACCAccountCMN/getAccountsForCombo.do",
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
        WtfGlobal.setAjaxTimeOut();
        this.accountStore.load();
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
//        this.typeStore.on('load', function(combo){
//           var recordSelected = combo.getStore().getAt(0);                     
//                combo.setValue(recordSelected);
//        },this);
        
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
            decimalPrecision:2
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
                includeDeactivatedTax: this.isEdit!=undefined? this.isEdit : false
            }
        });
        this.taxStore.on("load", function() {
            var record = new Wtf.data.Record({
                prtaxid: 'None',
                prtaxname: 'None'
            });
            this.taxStore.insert(this.taxStore.getCount()+1, record);
        }, this);
        if(this.readOnly)
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
        this.transQuantity=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength:10,
            maskRe: /[0-9]+(\.[0-9]+)?$/,
//            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
            allowDecimals:false
        });
        this.transBaseuomrate=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength:10
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
    loadPriceAfterProduct : function(){
        if(Wtf.getCmp(this.id)){ //Load price store if component exists
            this.loadPriceStore();
        } else {
//            this.productComboStore.un("load",this.loadPriceAfterProduct,this);//Remove event handler if Not exists
        }
    },
    loadPriceStore:function(val){
        this.billDate=(val==undefined?this.billDate:val);
        //if(this.editTransaction)
//         this.priceStore.on('load',this.setGridProductValues.createDelegate(this),this)
       // this.priceStore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(this.billDate)}});
    },
    loadPriceStoreOnly:function(val,pricestore, affecteduser){  //scope related issue
        this.dateChange=true;
        this.billDate=(val==undefined?this.billDate:val);        //if(this.editTransaction)
        affecteduser = (affecteduser == undefined)? "" : affecteduser;
        pricestore.load({
        params:{
            transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
            affecteduser : affecteduser
            }
        });
    },
    openProductWindow:function(){
        this.stopEditing();
        createFixedAsset();                // ERP-12520
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
         columnArr.push({
            header: WtfGlobal.getLocaleText("acc.invoice.lineItemSequence"),//"Sequence",
            width: 65,
            align: 'center',
            //dataIndex: 'srno',
            name: 'srno',
            renderer: Wtf.applySequenceRenderer
        });
        var headerData=(this.isLeaseFixedAsset)?WtfGlobal.getLocaleText("acc.product.gridProductID"):WtfGlobal.getLocaleText("erp.fixedasset.assetgroupid");
        if(this.productOptimizedFlag!=Wtf.Products_on_Submit){
            columnArr.push({
                header:headerData, // "Produt ID" : "Asset Group",
                width:250,
                dataIndex:(this.readOnly ||this.productOptimizedFlag==Wtf.Products_on_type_ahead)? 'pid' : 'productid',
//                dataIndex:(this.isLeaseFixedAsset)? (this.readOnly?'pid':'productid') : (this.readOnly?'productname':'productid'),
                editor:(this.isNote||this.readOnly)?"":this.productEditor,
                renderer:(this.productOptimizedFlag==Wtf.Products_on_type_ahead)?(this.readOnly?"":this.getComboNameRenderer(this.productEditor)):(this.readOnly?"":Wtf.comboBoxRenderer(this.productEditor))
            });
        } else {
            columnArr.push({
                header:headerData,
                width:250,
                dataIndex: 'pid',
                editor:(this.isNote||this.readOnly||this.productOptimizedFlag!=Wtf.Products_on_Submit)?"":this.productId
            });
        }
//        if(!this.isFixedAsset){
           columnArr.push(
            {
                header: this.isLeaseFixedAsset?WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"):WtfGlobal.getLocaleText("erp.fixedasset.assetgroupname"), // "Product Name",
                dataIndex: 'productname'
            });
//        }
        columnArr.push({
            header:WtfGlobal.getLocaleText("acc.je.acc"),
            width:200,
            dataIndex:'accountId',
            hidden:(!this.isNote ||this.noteTemp),
            renderer:Wtf.comboBoxRenderer(this.cmbAccount),
            editor:this.readOnly?"":this.cmbAccount
        },{
            header:this.isCN?WtfGlobal.getLocaleText("acc.invoice.gridInvNo"):WtfGlobal.getLocaleText("acc.invoice.gridVenInvNo"),//"Invoice No.":"Vendor Invoice No.",
            width:150,
            dataIndex:this.noteTemp?'transectionno':'billno',
            hidden:!this.isNote
        },{
             header:this.isLeaseFixedAsset?WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"):WtfGlobal.getLocaleText("erp.field.AssetDesciption"),//"Description",
             dataIndex:"desc",
             hidden:this.isNote,
             width:250,
             editor:(this.isNote||this.readOnly)?"":this.remark,
             renderer:this.descriptionRenderer   //ERP-13792 [SJ]
         },{
             header:WtfGlobal.getLocaleText("acc.product.supplier"),//"Supplier Part Number",
             dataIndex:"supplierpartnumber",
             hidden:!(Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.partNumber && !this.isCustomer && !this.isQuotation && !this.isNote),
             width:150         
         },{
             header:WtfGlobal.getLocaleText("acc.field.InventoryStore"), 
             dataIndex:'invstore',
             hidden:!(Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel && ((Wtf.account.companyAccountPref.withinvupdate && (!this.isInvoice && !this.isOrder)) || (!Wtf.account.companyAccountPref.withinvupdate && !this.isOrder))),                 
             width:150,
             renderer:Wtf.comboBoxRenderer(this.inventoryStores),
             editor:(this.readOnly)?"":this.inventoryStores
         },{
             header:WtfGlobal.getLocaleText("acc.field.ShelfLocation"),
             dataIndex:"shelfLocation",
             hidden:!(Wtf.account.companyAccountPref.invAccIntegration &&  this.isOrder && !this.isCustomer && !this.isQuotation),
             width:250,
             editor:(this.readOnly)?"":new Wtf.form.TextField()    
         },{
             header:WtfGlobal.getLocaleText("acc.field.InventoryLocation"), 
             dataIndex:'invlocation',
             hidden:!(Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel && ((Wtf.account.companyAccountPref.withinvupdate && (!this.isInvoice && !this.isOrder)) || (!Wtf.account.companyAccountPref.withinvupdate && !this.isOrder))),                 
             width:150,
             renderer:Wtf.comboBoxRenderer(this.inventoryLocation),
             editor:(this.readOnly)?"":this.inventoryLocation
         });
         columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModelForProduct[this.moduleid],undefined,undefined,this.readOnly);
         columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModel[this.moduleid],undefined,undefined,this.readOnly);
         columnArr.push({
             header: WtfGlobal.getLocaleText("acc.field.PermitNo."),
             dataIndex:"permit",
             hidden:true,
             width:100,
             editor:(this.readOnly)?"":this.permiteditor
         },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridNoteType"),//"Note Type",
            width:200,
            dataIndex:'typeid',
            hidden:(!this.isNote ||this.noteTemp),
            renderer:Wtf.comboBoxRenderer(this.typeEditor),
            editor:this.readOnly?"":this.typeEditor
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridQty"),//"Quantity",
             dataIndex:"quantity",
             align:'right',
             width:100,
             renderer:this.quantityRenderer,
//             renderer:this.storeRenderer(this.productComboStore,"productid","uomname"),
             editor:(this.isNote||this.readOnly)?"":this.transQuantity
         },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridUOM"),//"UOM",
            width:100,
            hidden:(this.isFixedAsset ||this.isLeaseFixedAsset),
            dataIndex:this.readOnly?'uomname':'uomid',
            renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.uomEditor),
            editor:(this.isNote||this.readOnly)?"":this.uomEditor
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridRateToBase"),//Base UOM Rate
             dataIndex:"baseuomrate",
             hidden:this.isFixedAsset,
             align:'left',
             width:100,
             renderer:this.conversionFactorRenderer(this.productComboStore,"productid","uomname"),
             editor:(this.isNote||this.readOnly)?"":this.transBaseuomrate
         },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridQtyInBase"),//Base UOM Quantity
             dataIndex:"baseuomquantity",
             hidden:this.isFixedAsset,
             align:'right',
             width:50,
             renderer:this.storeRenderer(this.productComboStore,"productid","uomname")
//             editor:(this.isNote||this.readOnly)?"":this.transQuantity
         },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridRemQty"),//"Remaining Quantity",
             dataIndex:"remainingquantity",
             align:'right',
             hidden:!this.isNote||this.noteTemp,
             width:150,
             renderer:this.quantityWithUOMRenderer(this.productComboStore,"productid","uomname"),
             editor:(this.isNote||this.readOnly)?"":this.transQuantity
        },{
             header:"<b> "+ WtfGlobal.getLocaleText("acc.invoice.gridEnterQty") +" </b>",//"<b>Enter Quantity</b>",
             dataIndex:"remquantity",
             align:'right',
             hidden:!this.isNote||this.noteTemp,
             width:180,
             renderer:this.quantityWithUOMRenderer(this.productComboStore,"productid","uomname"),
             editor:this.readOnly?"":this.transQuantity
        },{
             header:(Wtf.account.companyAccountPref.countryid!= Wtf.Country.INDONESIA)?WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingGST"):WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingVAT"),// "Unit Price Including GST",
             dataIndex: "rateIncludingGst",
             align:'right',
             fixed:true,
             width:150,
             renderer:this.unitPriceRendererWithPermissionCheck.createDelegate(this),
             hidden:true,
             editor:(this.isNote||this.readOnly||this.isViewTemplate || (this.isCustomer?!Wtf.dispalyUnitPriceAmountInSales:!Wtf.dispalyUnitPriceAmountInPurchase))?"":this.editPriceIncludingGST,
             editable:true
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice"),// "Unit Price",
             dataIndex: "rate",
             align:'right',
             width:150,
             renderer:this.unitPriceRendererWithPermissionCheck.createDelegate(this),
             editor:(this.isNote||this.readOnly)?"":this.editprice,
             editable:true,
             hidden: this.noteTemp || this.isRFQ
        },{
             header:WtfGlobal.getLocaleText("acc.field.PartialAmount(%)"),
             dataIndex:"partamount",
             align:'right',
             id:this.id+"partdisc",
             hidden:!this.isNote || this.noteTemp || !this.isCustomer,
             width:120,
             fixed:true,
             renderer:function(v){return'<div class="currency">'+parseFloat(v).toFixed(2)+'%</div>';},
             editor:this.readOnly?"":this.partAmount
        },{
            header: WtfGlobal.getLocaleText("acc.field.DiscountType"),
            width:200,
            dataIndex:'discountispercent',
//            hidden:true,//this.isQuotation?false:(this.isRequisition || this.noteTemp),
            renderer:Wtf.comboBoxRenderer(this.rowDiscountTypeCmb),
            editor:(this.isNote||this.readOnly)?"":this.rowDiscountTypeCmb
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridDiscount"),//"Discount",
             dataIndex:"prdiscount",
             align:'right',
             width:150,
//             hidden:true,//this.isQuotation?false:(this.isRequisition || this.noteTemp),
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
                 return'<div class="currency">'+v+'</div>';
             },
             editor:this.readOnly||this.isNote?"":this.transDiscount
         },{
             header: WtfGlobal.getLocaleText("acc.invoice.proTax"),//"Product Tax",
             dataIndex:"prtaxid",
             id:this.id+"prtaxid",
//             fixed:true,
             width:120,
             hidden:!(this.editTransaction||this.readOnly) || this.noteTemp,// || this.isOrder,
             renderer:Wtf.comboBoxRenderer(this.transTax),
             editor:this.readOnly||this.isNote?"":this.transTax  //this.transTax
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridTaxAmount"),//"Tax Amount",
             dataIndex:"taxamount",
             id:this.id+"taxamount",
//              fixed:true,
             //align:'right',
             width:150,
             editor:this.readOnly?"":this.transTaxAmount,
             hidden:!(this.editTransaction||this.readOnly)|| this.noteTemp, // || !this.isOrder,
             renderer:this.setTaxAmountWithotExchangeRate.createDelegate(this)
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.Tax"),//"Tax",
             dataIndex:"taxpercent",
             align:'right',

             hidden:!this.isNote || this.noteTemp,
             width:200,
             renderer:function(v){return'<div class="currency">'+v+'%</div>';}
        },{
             header:WtfGlobal.getLocaleText("acc.field.ProductTaxAmount"),
             dataIndex:"taxamount",
             align:'right',
             hidden:!this.isNote || this.noteTemp,
             width:200,
             renderer:WtfGlobal.withoutRateCurrencySymbol
        },{
             header:this.isQuotationFromPR ? WtfGlobal.getLocaleText("acc.field.BudgetedCost") : (this.isNote?WtfGlobal.getLocaleText("acc.invoice.gridOriginalAmt"):WtfGlobal.getLocaleText("acc.invoice.gridInvAmt")),//"Original Amount":"Invoice Amount",
             dataIndex:"orignalamount",
             align:'right',
             width:150,
             hidden: this.isQuotationFromPR ? false : (!(this.isNote) || this.noteTemp),
             renderer:(this.isNote||this.readOnly?WtfGlobal.withoutRateCurrencySymbol:WtfGlobal.currencyRendererSymbol)
        },{
             header:this.isRequisition ? (this.editTransaction ? WtfGlobal.getLocaleText("acc.field.BudgetedCost") : WtfGlobal.getLocaleText("acc.field.EstimatedCost") ): this.isNote?WtfGlobal.getLocaleText("acc.invoice.gridCurAmt"):WtfGlobal.getLocaleText("acc.invoice.gridAmount"),//"Current Amount ":"Amount",
             dataIndex:"amount",
             hidden:this.isRFQ,
             align:'right',
             width:200,
             renderer:(this.isNote?WtfGlobal.withoutRateCurrencySymbol:this.calAmountWithoutExchangeRate.createDelegate(this))
        },{
             header:(this.readOnly)?WtfGlobal.getLocaleText("acc.invoice.gridAmount"):"<b>"+ WtfGlobal.getLocaleText("acc.invoice.gridEnterAmt") + "</b>",//"Amount":"<b>Enter Amount</b>",
             dataIndex:this.noteTemp?'discount':'discamount',
             align:'right',
             width:200,
             hidden:!this.isNote,
             renderer:WtfGlobal.withoutRateCurrencySymbol,
             editor:(this.readOnly)?"":new Wtf.form.NumberField({
                allowBlank: false,
                allowNegative: false
             })
        },{
             header:WtfGlobal.getLocaleText("acc.field.ApproverRemark"),//"Approver Remark",
             dataIndex:"approverremark",
             hidden: (this.isRequisition && this.editTransaction) ? false : true,
             width:250,
             editor:(this.isRequisition && this.editTransaction)?this.approverremark:""
         },         
        {
            header:WtfGlobal.getLocaleText("acc.invoice.gridRemark"),//"Remark",
            width:200,
            hidden:(!this.isNote ||this.noteTemp),
            dataIndex:'gridRemark',
            name:'gridRemark',
            editor:(this.readOnly)?"":this.cndnRemark
            });
             if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.invoicegrid.TaxAmount"),//"Total Tax Amount",
                dataIndex:"recTermAmount",
                hidden : this.isRFQ || this.isRequisition ? true : false,
                align:'right',
                width:100,
                renderer: WtfGlobal.withoutRateCurrencySymbol
            },{
                header: WtfGlobal.getLocaleText("acc.invoicegrid.tax"), 
                align: 'center',                
                width: 40,
                dataIndex:"LineTermdetails",
                renderer: this.isRFQ || this.isRequisition ? "" : this.addRenderer.createDelegate(this),
                hidden:  this.isRFQ || this.isRequisition ? true : false 
            });
        }
      if(!this.isNote && !this.readOnly && !this.isLinkedTransaction) {
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),//"Action",
                align:'center',
                width:40,
                hidden:this.readOnly,
                renderer: this.deleteRenderer.createDelegate(this)
            });
        }
        if(this.isFixedAsset || (this.isLeaseFixedAsset && !this.isQuotation)) {
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.fixed.asset.view"),
                align:'center',
                width:40,
                renderer: this.viewRenderer.createDelegate(this)
            });
        }
        /**
         * Put column for ITC type for Vendor invoice module.
         * This is applicable for India only.
         */
        if (WtfGlobal.isIndiaCountryAndGSTApplied() && Wtf.isitcapplicable && (this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId)) {
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
    },
    unitPriceRendererWithPermissionCheck:function(v,m,rec){
        if (!isNaN(v)) {
            if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                return Wtf.UpriceAndAmountDisplayValue;
            } else {
                /*
                 * ERP-40242 : In linking case, deactivated tax not shown.Hence, empty taxid set in record.          
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
                    if(this.parentObj!== null && this.parentObj.includingGST != undefined && this.parentObj.includingGST.checked == true){
                         return WtfGlobal.withCurrencyUnitPriceRenderer(rec.data.rateIncludingGst,m,rec);
                    }else{
                        return WtfGlobal.withCurrencyUnitPriceRenderer(v,m,rec);
                    }
                }
            }
        }
    },
    conversionFactorRenderer:function(store, valueField, displayField) {
        return function(value, meta, record) {
            var idx = Wtf.uomStore.find("uomid", record.data["uomid"]);            
            if(idx == -1)
                return value;
            var uomname = Wtf.uomStore.getAt(idx).data["uomname"];
            if (uomname == "N/A") {
                return value;
            }
            idx = store.find(valueField, record.data[valueField]);
            if(idx == -1)
                return value;
            var rec = store.getAt(idx);
            return "1 "+ uomname +" = "+ +value+" "+rec.data[displayField];
        }
    },
        addRenderer: function(v, m, rec) {
        return getToolTipOfTermsfun(v, m, rec);
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
      return (val=="NaN"?0:val);  //return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
    },
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    viewRenderer:function(v,m,rec){
        return "<div class='view pwnd view-gridrow'  title="+WtfGlobal.getLocaleText("acc.fixed.asset.detailsTT")+"></div>";
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
                                productquantity:record.data.quantity, 
                                productbaseuomrate:record.data.baseuomrate,
                                productbaseuomquantity:record.data.baseuomquantity,
                                productuomid:record.data.uomid,
                                productinvstore:record.data.invstore,
                                productinvlocation:record.data.invlocation,
                                productrate:record.data.rate
                                //To do - Need to check this for multi UOM change
                            });                            
                            deletedData.push(newRec);
                            this.deleteStore.add(deletedData);                            
                }
                store.remove(store.getAt(rowindex));
                if(rowindex==total-1){
                    this.addBlankRow();
                }
                this.fireEvent('datachanged',this);
                this.fireEvent('productdeleted',this);
            }, this);
        } else if(e.getTarget(".view-gridrow")){
            var store=grid.getStore(); 
            var total=store.getCount();
            if(rowindex==total-1){
                return;
            }
            var record = store.getAt(rowindex);
            if(this.isFixedAsset || (this.isLeaseFixedAsset && !this.isQuotation)){
                var productid = record.get('productid');
                var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, productid, 'productid');
                if(productComboRecIndex==-1){
                    productComboRecIndex=WtfGlobal.searchRecordIndex(store, productid, 'productid');
                    var proRecord = store.getAt(productComboRecIndex);
                    if (proRecord.get('isAsset') && record.get("quantity") != 0)
                    this.callFixedAssetDetailsWindow(record, proRecord, this.readOnly);
                } else if(productComboRecIndex >= 0) {
                    var proRecord = this.productComboStore.getAt(productComboRecIndex);
                    if(proRecord.get('isAsset') && record.get("quantity") != 0)
                        this.callFixedAssetDetailsWindow(record,proRecord,this.readOnly);
                }
                
            }
        } else if(e.getTarget(".termCalc-gridrow")){
            if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
                this.showTermWindow(grid.getStore().getAt(rowindex),grid,rowindex);
            }else{
                return;
            }
        }else {
            this.fireEvent("productselect", grid.getStore().getAt(rowindex).get("productid"));
        }
    },

    storeRenderer:function(store, valueField, displayField) {
        return function(value, meta, record) {
            value=(parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            var idx = store.find(valueField, record.data[valueField]);
            if(idx == -1)
                return value;
            var rec = store.getAt(idx);
            return value+" "+rec.data[displayField];
        }
    },
    
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
    showTermWindow : function(record,grid,rowindex) {
        var deliveredprodquantity = record.data.quantity;
        deliveredprodquantity = (deliveredprodquantity == "NaN" || deliveredprodquantity == undefined || deliveredprodquantity == null)?0:deliveredprodquantity;
        if(deliveredprodquantity<=0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),WtfGlobal.getLocaleText("acc.Workorder.Quantityshouldbegreaterthanzero")], 2);
            return false;
        }
        var venderDetails =WtfGlobal.searchRecord(this.parentObj.Name.store, this.parentObj.Name.getValue(), 'accid');
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
                title: WtfGlobal.getLocaleText("acc.invoicegrid.TaxWindowTitle"),
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
                    hidden: true,
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
                } else if(!prorec.data.multiuom){
//                          WtfComMsgBox(["Warning","Multi UOM not allowed for the selected product. "], 2);
                    return false;
                }
            }
        }
        /*Code on product select starts*/        
        if(obj.field=="productid"){
            
//            var isProductAlreadySelected = false;
//            
//            this.getStore().each(function(recr){
//                var prodId = recr.get('productid');
//                if(prodId == obj.value){
//                    isProductAlreadySelected = true;
//                    return false;
//                }
//            },this);
//            
//            var productText = "Product";
//            if(this.isFixedAsset){
//                productText = "Asset Group";
//            }
//            
//            if(isProductAlreadySelected){
//                WtfComMsgBox(['Infornation',productText+' Already Selected'],0);
//                obj.record.set('productid','');
//                return false;
//            }
            
            var index=this.productComboStore.findBy(function(rec){
                if(rec.data.productid==obj.value)
                    return true;
                else
                    return false;
            })
            var prorec=this.productComboStore.getAt(index); 
            //index=this.priceStore.find('productid',obj.value)
            //rec=this.priceStore.getAt(index);
            if(this.editTransaction){ //In Edit Case Check product quantity is greater than available quantity when selecting product                
                var availableQuantity = prorec.data.quantity;    
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
                    quantity = quantity + (obj.record.data['quantity']*obj.record.data['baseuomrate']);  
                    
                    if(availableQuantity<quantity){
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+' is '+availableQuantity], 2);
//                        obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCS/CIareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' is '+availableQuantity+'<br><br><center>So you cannot proceed ?</center>'], 2);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",obj.originalValue);
                                  rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                        }
                    }
                //WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.76")+" "+rec.data['productname']], 2);
                //obj.cancel=true;                
                }else if(!Wtf.account.companyAccountPref.withinvupdate&&this.isCustomer&&availableQuantity<(obj.record.data['quantity']*obj.record.data['baseuomrate'])&&prorec.data.type!='Service'&&!this.isQuotation&&!this.isOrder){
                    this.isValidEdit = false;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+' is '+availableQuantity], 2);
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
                    quantity = quantity + (obj.record.data['quantity']*obj.record.data['baseuomrate']);
                    quantity = quantity + obj.record.data['quantity'];
                    if(prorec.data['quantity']<quantity){
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+' is '+prorec.data['quantity']], 2);
//                        obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCS/CIareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+' is '+prorec.data['quantity']+'<br><br><center>So you cannot proceed ?</center>'], 2);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIaretheexceedingwishtoproceed")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",obj.originalValue);
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
                }else if(!Wtf.account.companyAccountPref.withinvupdate&&this.isCustomer&&prorec.data['quantity']<(obj.record.data['quantity']*obj.record.data['baseuomrate'])&&prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation&&!this.isOrder){
                    this.isValidEdit = false;
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+' is '+prorec.data['quantity']], 2);
//                    obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCS/CIareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+' is '+prorec.data['quantity']+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",obj.originalValue);
                                  rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                        }
                }else if(this.isCash&&this.isCustomer&&prorec.data['quantity']<(obj.record.data['quantity']*obj.record.data['baseuomrate'])&&prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation&&!this.isOrder){
                    this.isValidEdit = false;
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+' is '+prorec.data['quantity']], 2);
//                    obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCSareexceedingthequantity")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+WtfGlobal.getLocaleText("acc.field.is")+' '+prorec.data['quantity']+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCSareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",obj.originalValue);
                                  rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                        }
                }
            }  
        //EDIT case in without trading flow and with inventory        
        }else if(!Wtf.account.companyAccountPref.withinvupdate&&this.isCustomer&&(obj.field=="quantity"||obj.field=="baseuomrate")&&obj.record.data['productid'].length>0&&!this.isQuotation&&!this.isOrder){  
            if(obj.field=="quantity") {
                var originalQuantity = obj.originalValue;
                var newQuantity = obj.value;
                var originalBaseuomrate = obj.record.data['baseuomrate'];
                var newBaseuomrate = obj.record.data['baseuomrate'];
            } else if(obj.field=="baseuomrate") {
                var originalQuantity = obj.record.data['quantity'];
                var newQuantity = obj.record.data['quantity'];
                var originalBaseuomrate = obj.originalValue;
                var newBaseuomrate = obj.value;
            }
            prorec=this.productComboStore.getAt(this.productComboStore.find('productid',obj.record.data.productid));
            if(prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part') {
                var availableQuantity = prorec.data.quantity;
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
                    if(this.editTransaction&&!this.copyInv&&availableQuantity < quantity) {
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCS/CIareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+WtfGlobal.getLocaleText("acc.field.is")+' '+availableQuantity+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",originalQuantity);
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
                    if((!this.editTransaction||this.copyInv) &&  availableQuantity < quantity) {
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableSoyou")+'</center>'], 2);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",originalQuantity);
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
        } else if(this.isCash &&this.isCustomer&&(obj.field=="quantity"||obj.field=="baseuomrate")&&obj.record.data['productid'].length>0&&!this.isQuotation&&!this.isOrder){
            if(obj.field=="quantity") {
                var originalQuantity = obj.originalValue;
                var newQuantity = obj.value;
                var originalBaseuomrate = obj.record.data['baseuomrate'];
                var newBaseuomrate = obj.record.data['baseuomrate'];
            } else if(obj.field=="baseuomrate") {
                var originalQuantity = obj.record.data['quantity'];
                var newQuantity = obj.record.data['quantity'];
                var originalBaseuomrate = obj.originalValue;
                var newBaseuomrate = obj.value;
            }
            prorec=this.productComboStore.getAt(this.productComboStore.find('productid',obj.record.data.productid));
            if(prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part') {
                var availableQuantity = prorec.data.quantity;
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
                    if(this.editTransaction&&!this.copyInv&&availableQuantity < quantity) {
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' is '+availableQuantity], 2);
//                        obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.common.stockCI")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' is '+availableQuantity+'<br><br><center>So you cannot proceed ?</center>'], 2);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",originalQuantity);
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
                    if((!this.editTransaction||this.copyInv) &&  availableQuantity < quantity) {
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCSareexceedingthequantityavailableSoyoucannotproceed")+'</center>'], 2);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCSareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",originalQuantity);
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
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Remainingamountfortheselectedproductis")+' '+WtfGlobal.getCurrencySymbol()+" "+(rec.data['amount'])], 2);
                    obj.cancel=true;
                    rec.set('discamount',0);
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
    descriptionRenderer :function(val, meta, rec, row, col, store) {   //ERP-13792 [SJ]
        var regex = /(<([^>]+)>)/ig;
//        val = val.replace(/(<([^>]+)>)/ig,"");
        var tip = val.replace(/"/g,'&rdquo;');
        meta.attr = 'wtf:qtip="'+tip+'"'+'" wtf:qtitle="'+WtfGlobal.getLocaleText("acc.gridproduct.discription")+'"';
        return val;
    },
    RitchTextBoxSetting:function(grid, rowIndex, columnIndex, e){
        var v=WtfGlobal.RitchTextBoxSetting(grid, rowIndex, columnIndex, e, this.readOnly);
        return v;
    },
    updateRow:function(obj){
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
            quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
            if(obj.field=="prdiscount" && (rec.data.discountispercent == 1) && obj.value >100){
                
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Discountcannotbegreaterthan100")], 2);
                    rec.set("prdiscount",0);
            } else {
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
                  var productuomid = "";
                  if(productComboIndex >=0){
                      prorec = this.productComboStore.getAt(productComboIndex);
                      productuomid = prorec.data.uomid;
                      if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                            obj.record.set("baseuomquantity", obj.record.get("quantity")*obj.value);
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
                      if(productuomid != obj.value){
                          //To do - Need to take rate from new window
//                      this.showPriceWindow.createDelegate(this,[rec, obj],true);
                            obj.record.set("baseuomquantity", obj.record.get("quantity")*obj.record.get("baseuomrate"));
                      } else {
                          obj.record.set("baseuomrate", 1);
                          obj.record.set("baseuomquantity", obj.record.get("quantity")*obj.record.get("baseuomrate"));
                      }
                  }
//                  this.fireEvent('datachanged',this);
            }
            if(obj.field=="productid"|| obj.field=="pid" ){
                var customFieldArr = GlobalColumnModelForProduct[this.moduleid];
                if(customFieldArr !=null && customFieldArr != undefined){
                    for(var k=0;k<customFieldArr.length;k++){
                        rec.set(customFieldArr[k].fieldname,"");
                    }
                }
                if(this.isCustomer)
                    rec.set("changedQuantity",(rec.data.quantity*(-1))*rec.data.baseuomrate);
                else
                    rec.set("changedQuantity",(rec.data.quantity)*rec.data.baseuomrate);
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
                        currency: this.parentObj.Currency.getValue(),
                        quantity: obj.record.data.quantity,
                        transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                        carryin : (this.isCustomer)? false : true
                    }
                }, this,function(response){
                    var datewiseprice =response.data[0].price;
                    this.isPriceListBand = response.data[0].isPriceListBand;
                    this.isVolumeDisocunt = response.data[0].isVolumeDisocunt;
                    this.priceSource = response.data[0].priceSource;
                    this.pricingbandmasterid=response.data[0].pricingbandmasterid;
                    this.isPriceFromUseDiscount = response.data[0].isPriceFromUseDiscount;
                    this.priceSourceUseDiscount = response.data[0].priceSourceUseDiscount;
                    this.defaultPrice = datewiseprice;
                    var producttype = response.data[0].producttype;
                    this.isBandPriceConvertedFromBaseCurrency = response.data[0].isBandPriceConvertedFromBaseCurrency;
                    this.isbandPriceNotAvailable = response.data[0].isbandPriceNotAvailable;
                    obj.record.set("oldcurrencyrate",1);
                        /*
                         * set band of customer on product selection
                         */
                    obj.record.set("pricingbandmasterid", this.pricingbandmasterid);
                    for(var i=1;i<response.data.length;i++){
                        var dataObj=response.data[i];
                        var key=dataObj.key;
                      for(var k=0;k<obj.grid.colModel.config.length;k++){
                            if(obj.grid.colModel.config[k].dataIndex==key){
                                var store=(obj.grid.colModel.config[k].editor!=undefined && obj.grid.colModel.config[k].editor!="")?obj.grid.colModel.config[k].editor.field.store:"";
                                if(store)
                                    store.clearFilter();
                                 obj.record.set(key,dataObj[key]);
                            }
                        }
                        
                        
                    }
                   
                  var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.value, 'productid');
                  var productname = "";
                  var isAsset = "";
                  var proddescription = "";
                  var productuomid = undefined;
                  var productsuppliernumber = "";
                  var shelfLocation = "";
                  var prorec = null;
                  var acctaxcode = (this.isCustomer)?"salesacctaxcode":"purchaseacctaxcode";
                  var protaxcode = "";
                  var baseuomRate=1;
                  var ComboIndex=0;
                  if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Products_on_type_ahead && productComboIndex==-1){
                        productComboIndex=1;
                        ComboIndex=-1;
                  }else if(this.productOptimizedFlag==Wtf.Products_on_Submit){
                        productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.value.trim(), 'pid');
                  }
                  if(productComboIndex >=0){
                      prorec = this.productComboStore.getAt(productComboIndex);
                      if(ComboIndex==-1){
                            prorec=rec;
                      } 
                      productname = prorec.data.productname;
                      proddescription = prorec.data.desc;
                      productuomid = prorec.data.uomid;
                      isAsset = prorec.data.isAsset;
                      obj.record.set("baseuomrate", 1);
                      if(this.UomSchemaType==Wtf.UOMSchema){//for Schema type
                          productuomid = prorec.data.uomid;
                          obj.record.set("baseuomname", prorec.data.uomname);
                      } else{//for packeging UOM type
                          productuomid =this.isCustomer? prorec.data.salesuom:prorec.data.purchaseuom;
//                          productuomname =this.isCustomer? prorec.data.salesuomname:prorec.data.purchaseuomname;
                           baseuomRate=this.isCustomer? prorec.data.stocksalesuomvalue:prorec.data.stockpurchaseuomvalue;
                           if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                                obj.record.set("baseuomquantity", quantity*(baseuomRate));
                                obj.record.set("baseuomrate", (baseuomRate));                                
                            } else {
                                obj.record.set("baseuomquantity", quantity);
                                obj.record.set("baseuomrate", 1);
                            } 
                            obj.record.set("caseuom", prorec.data['caseuom']);
                            obj.record.set("inneruom", prorec.data['inneruom']);
                            obj.record.set("stockuom", prorec.data['uomid']);
                            obj.record.set("caseuomvalue", prorec.data['caseuomvalue']);
                            obj.record.set("inneruomvalue", prorec.data['inneruomvalue']);
                            obj.record.set("pid", prorec.data['pid']);
                       }
                      productsuppliernumber= prorec.data.supplierpartnumber;
                      shelfLocation = prorec.data.shelfLocation;
                      protaxcode = prorec.data[acctaxcode];
                  }
                  obj.record.set("desc",proddescription);
                  obj.record.set("uomid", productuomid);
                  obj.record.set("supplierpartnumber",productsuppliernumber);
                  obj.record.set("shelfLocation",shelfLocation);
                  obj.record.set("productname", productname);
                  obj.record.set("isAsset", isAsset);
                  /*
                   * Set producttype for GST calculation in lease
                   */
                    if (prorec != undefined) {
                        obj.record.set("producttype", producttype);
                    }
                    if (this.isGST) {
                        if (rec.data.LineTermdetails != '') {
                                calculateUpdatedTaxes(this.parentObj, this, rec);
                                this.updateTermDetails();
                            } else {
                    
                        /**
                         * ERP-32829 
                         * code for New GST 
                         */
                        var extraparams = {};
                        extraparams.isProductIDSelect = true;
                        processGSTRequest(this.parentObj, this, prorec.data.productid,extraparams);
                            if (WtfGlobal.isIndiaCountryAndGSTApplied() && Wtf.isitcapplicable && prorec.data['itctype'] != undefined && prorec.data['itctype'] != ""
                                    && (this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId)) {
                                rec.set('itctype', prorec.data['itctype']);
                            }
                    }
                }
                  if (producttype == Wtf.producttype.service && quantity == "") {
                        obj.record.set("quantity", 1);
                    }
                if (this.isVolumeDisocunt) {
                        if (obj.record.data.quantity != "") {
                            obj.record.set("rate", this.defaultPrice);
                            obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSource);
                        } else {
                            obj.record.set("rate", "");
                            obj.record.set("priceSource", "");
                        }
                    } else if (this.isPriceListBand) {
                        if (this.isPriceFromUseDiscount) {
                            if (obj.record.data.quantity != "") {
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
                        if(datewiseprice==0){
                            if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.addprice) && !this.isRequisition){//permissions
                                rec.set("productname",productname);
                                // Wtf.Msg.confirm(WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pricefortheproduct")+" <b>"+productname+"</b> "+' '+WtfGlobal.getLocaleText("acc.field.isnotsetDoyouwanttosetitnow"),
                                   // this.showPriceWindow.createDelegate(this,[rec, obj],true), this);
                            //}else{
                               // WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pricefortheproduct")+" <b>"+productname+"</b>"+' '+WtfGlobal.getLocaleText("acc.field.isnotset")], 2);
                            }    
                        } else {
                            // setting datewise price according to currency exchange rate - 
                        
                            var rate=((rec==undefined||rec.data['currencyrate']==undefined||rec.data['currencyrate']=="")?1:rec.data['currencyrate']);
                            var oldcurrencyrate=((rec==undefined||rec.data['oldcurrencyrate']==undefined||rec.data['oldcurrencyrate']=="")?1:rec.data['oldcurrencyrate']);
                            var modifiedRate;
                            if (rate != 0.0) {
                                //modifiedRate=(parseFloat(datewiseprice)*parseFloat(rate))/parseFloat(oldcurrencyrate);
                                modifiedRate = getRoundofValueWithValues(((parseFloat(datewiseprice) * parseFloat(rate)) / parseFloat(oldcurrencyrate)), Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                            }
                            else {
                                //modifiedRate=(parseFloat(datewiseprice)/parseFloat(oldcurrencyrate));
                                modifiedRate = getRoundofValueWithValues((parseFloat(datewiseprice) / parseFloat(oldcurrencyrate)), Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                            }
                            if (this.isPriceFromUseDiscount) {
                                if (obj.record.data.quantity != "") {
                                    obj.record.set("rate", this.defaultPrice);
                                    obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSourceUseDiscount);
                                } else {
                                    obj.record.set("rate", "");
                                    obj.record.set("priceSource", "");
                                }
                            } else {
                                obj.record.set("rate", modifiedRate);
                                    obj.record.set("productname", productname);
                                }
                            }
                            // alert if band price not available
                           // if (this.isbandPriceNotAvailable) {
                             //   WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.bandPriceNotAvailable.msg")], 2);
                           // }
                        }

                    if(!(obj.soflag)){
                        obj.record.set("baseuomquantity",1);
//                        if(this.isFixedAsset || this.isLeaseFixedAsset){
//                            obj.record.set("quantity",0);
//                        }else{
                            //obj.record.set("quantity",1);
//                        }
                        if(this.parentObj && this.parentObj.includeProTax && this.parentObj.includeProTax.getValue() == true) {
                            obj.record.set("prtaxid", protaxcode);
                        } else {
                            obj.record.set("prtaxid", "");
                        }
                        var taxamount = this.setTaxAmountAfterSelection(obj.record);
                        obj.record.set("taxamount",taxamount);
                        obj.record.set('isUserModifiedTaxAmount', false);

                        this.fireEvent("productselect", obj.value);
                    }
                    
//                    if(this.isFixedAsset || this.isLeaseFixedAsset){
//                        var productid = rec.get('productid');
//                        var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, productid, 'productid');
//                        if(productComboRecIndex >=0){
//                            var proRecord = this.productComboStore.getAt(productComboRecIndex);
//                            if(proRecord.get('isAsset') && obj.record.get("quantity") != 0)
//                                this.callFixedAssetDetailsWindow(obj.record,proRecord);
//                        }
//                      
//                    }
                    
                    this.fireEvent('datachanged',this);
                }, function(){

                });
            }else if(obj.field=="quantity"  || obj.field=="pricingbandmasterid"){
                rec=obj.record;
                
                if ((!this.isCustomer && Wtf.account.companyAccountPref.productPricingOnBands) || (this.isCustomer && Wtf.account.companyAccountPref.productPricingOnBandsForSales) && this.parentObj && this.parentObj.PO && this.parentObj.PO.getValue() == "") {
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
                        this.isVolumeDisocuntExist = response.data[0].isVolumeDisocuntExist;
                        this.isBandPriceConvertedFromBaseCurrency = response.data[0].isBandPriceConvertedFromBaseCurrency;
                        this.isbandPriceNotAvailable = response.data[0].isbandPriceNotAvailable;
                        
                        obj.record.set("pricingbandmasterid", this.pricingbandmasterid);        
                        if (this.isVolumeDisocunt) {
                            obj.record.set("rate", this.defaultPrice);
                            obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSource);
                        } else if (this.isPriceListBand) {
                            obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                            obj.record.set("rate", this.defaultPrice);
                            if (this.isPriceFromUseDiscount) {
                                if (obj.record.data.quantity != "") {
                                    obj.record.set("rate", this.defaultPrice);
                                    obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSourceUseDiscount + ": " + WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                                } else {
                                    obj.record.set("rate", "");
                                    obj.record.set("priceSource", "");
                                }
                            } else {
                                if (this.isVolumeDisocuntExist) {
                                    obj.record.set("rate", this.defaultPrice);
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
                                if (obj.record.data.quantity != "") {
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
                           // if (this.isbandPriceNotAvailable) {
                             //   WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.bandPriceNotAvailable.msg")], 2);
                            //}
                        }
                        this.fireEvent('datachanged',this);
                    }, function(response) {
                        
                    });
                }
                
                if(this.isCustomer)
                    rec.set("changedQuantity",(rec.data.copyquantity-obj.value)*((rec.data.copybaseuomrate==="")?1:rec.data.copybaseuomrate));
                else
                    rec.set("changedQuantity",(obj.value-rec.data.copyquantity)*((rec.data.copybaseuomrate==="")?1:rec.data.copybaseuomrate));
                
                if(((rec.data.isNewRecord=="" || rec.data.isNewRecord==undefined) && this.fromOrder&&!(this.editTransaction||this.copyInv))||(this.isEdit && rec.data.linkid !="")) {  
                    if(obj.value > rec.data.copyquantity){
                        var msg = "Product Quantity entered in CI is exceeds from original quantity mentioned in SO. "
                        if (this.isCustomer) {
                            if(this.isOrder && !this.isQuotation){
                                msg = "Product Quantity entered in SO is exceeds from original quantity mentioned in PO/CQ. "
                            } else if(this.isQuotation) {
                                msg = "Product Quantity entered in CQ is exceeds from original quantity mentioned in VQ. "
                            } else {
                                if(Wtf.account.companyAccountPref.withinvupdate){
                                    msg = "Product Quantity entered in CI is exceeds from original quantity mentioned in SO/DO/CQ. "
                                } else {
                                    msg = "Product Quantity entered in CI is exceeds from original quantity mentioned in CQ/SO. "
                                }
                                
                            }
                            
                        } else {
                            if(this.isOrder && !this.isQuotation){
                                msg = "Product Quantity entered in PO is exceeds from original quantity mentioned in VQ/SO. "
                            } else if(this.isQuotation) {
                                msg = WtfGlobal.getLocaleText("acc.field.ProductVQexceedsfromoriginalquantitymentionedinPR");
                            } else {
                                if(Wtf.account.companyAccountPref.withinvupdate){
                                    if(this.moduleid==39) {//fixed asset PI moduleid - refer ERP-9707
                                        msg = "Product Quantity entered in Asset Acquired Invoice exceeds from original quantity mentioned in selected Asset PO/GR/VQ. ";
                                    } else {
                                        msg = "Product Quantity entered in VI is exceeds from original quantity mentioned in selected PO/GR/VQ. ";
                                    }
                                } else {
                                    msg = "Product Quantity entered in VI is exceeds from original quantity mentioned in PO/GR/VQ. "
                                }
                                
                            }
                            
                        }
                        obj.record.set(obj.field, obj.originalValue);
                         Wtf.MessageBox.alert("Alert",msg,{
                                
                        },this)
                        
                        /*
                         Used same logic of Sales Invoice. 
                         After changing quantity of product, price was not updating in subtotal.
                          */
                        this.fireEvent('datachanged',this);
                        return;
                        
                    }else if(obj.value!=rec.data.copyquantity) {
                        var msg = "Product Quantity entered in Invoice is different from original quantity mentioned in SO. Do you want to continue?"
                        if (this.isCustomer) {
                             if(this.isOrder && !this.isQuotation){
                                msg = "Product Quantity entered in SO is different from original quantity mentioned in PO/CQ. Do you want to continue?"
                            } else if(this.isQuotation) {
                                msg = "Product Quantity entered in CQ is different from original quantity mentioned in VQ. Do you want to continue?"
                            }else {
                                if(Wtf.account.companyAccountPref.withinvupdate){
                                    msg = "Product Quantity entered in CI is different from original quantity mentioned in SO/DO/CQ. Do you want to continue?"
                                } else {
                                    msg = "Product Quantity entered in CI is different from original quantity mentioned in CQ/SO. Do you want to continue?"
                                }
                                
                            }
                            
                        } else {
                            if(this.isOrder && !this.isQuotation){
                                msg = WtfGlobal.getLocaleText("acc.field.ProductinPOdifferentSO/VQcontinue")
                            } else if(this.isQuotation) {
                                msg = WtfGlobal.getLocaleText("acc.field.ProductVQdifferentPRcontinue");
                            } else {
                                if(Wtf.account.companyAccountPref.withinvupdate){
                                    msg = "Product Quantity entered in VI is different from original quantity mentioned in selected PO/GR/VQ. Do you want to continue?"
                                } else {
                                    msg = "Product Quantity entered in VI is different from original quantity mentioned in VQ/PO. Do you want to continue?"
                                }
                                
                            }
                            
                        }
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"),msg,function(btn){
                            if(btn!="yes") {
                                obj.record.set(obj.field, obj.originalValue);
                                obj.record.set("baseuomquantity",obj.originalValue*obj.record.get("baseuomrate"));
                            } else {
                                this.onQuantityChanged(obj,rec,prorec);
                            }
                        },this)
//                      return;
                    }
                } else {
                   this.onQuantityChanged(obj,rec,prorec); 
                }
                if((obj.record.data["quantity"])==0 && obj.record.data["isNewRecord"]!='1'){
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
//                    this.store.remove(obj.record);
                }
                this.fireEvent('datachanged',this);
            }
            
            if(obj.field=="prtaxid" || obj.field=="rate" || obj.field=="quantity" || obj.field=="discountispercent" || obj.field=="prdiscount"){
                taxamount = this.setTaxAmountAfterSelection(obj.record);
                obj.record.set("taxamount",taxamount);
                obj.record.set('isUserModifiedTaxAmount', false);
                this.fireEvent('datachanged',this);
                if (this.isGST) {
                    var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
                    var productuomid = "";
                    if (productComboIndex >= 0) {
                        prorec = this.productComboStore.getAt(productComboIndex);
                    }
                  /*
                   * Set producttype for GST calculation in lease
                   */
                    if (prorec != undefined) {
                        obj.record.set("producttype", prorec.data.producttype);
                    }
                    /*
                   * Re-calculate GST if terms are already present
                   */
                    if (WtfGlobal.isIndiaCountryAndGSTApplied() && rec.data.LineTermdetails != '') {
                                calculateUpdatedTaxes(this.parentObj, this, rec);
                                this.updateTermDetails();
                            } else {
                    
                        /**
                         * ERP-32829 
                         * code for New GST 
                         */
                        processGSTRequest(this.parentObj, this, prorec.data.productid);                    
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
            
            
            if((obj.field=="prtaxid"||obj.field=="quantity")&&!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden){
                if(SATSCOMPANY_ID==companyid){/*For SATS*/
                    taxamount = this.setTaxAmountAfterIncludingGst(obj.record,1);
                    var amountwithOutGst = this.setTaxAmountAfterIncludingGst(obj.record,2);
                    var amountwithGst = this.setTaxAmountAfterIncludingGst(obj.record,3);
                    obj.record.set("taxamount",taxamount);
                    obj.record.set('isUserModifiedTaxAmount', false);
                    if(amountwithGst!=0){
                        obj.record.set("rateIncludingGst",amountwithGst);
                    }
                    if(amountwithOutGst!=0){
                        obj.record.set("rate",amountwithOutGst);
                    }
                }else{
                    this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                }
                this.fireEvent('datachanged',this);
            }
            if(obj.field=="rateIncludingGst"){
                if(SATSCOMPANY_ID==companyid){/*For SATS*/
                    taxamount = this.setTaxAmountAfterIncludingGst(obj.record,1);
                    var amountwithOutGst = this.setTaxAmountAfterIncludingGst(obj.record,2);
                    obj.record.set("taxamount",taxamount);
                    obj.record.set('isUserModifiedTaxAmount', false);
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
                    this.setTaxAndRateAmountAfterIncludingGST(obj.record);
                }
                this.fireEvent('datachanged',this);
            }
            if (obj.field == "taxamount") {
                /*
                 * If user changed the tax amount manually then isUserModifiedTaxAmount flag made true for Adaptive Rounding Algorith calculataion.
                 * ERM-1085
                 */
                obj.record.set("isUserModifiedTaxAmount", true);
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
             obj.record.set("isAsset",rec!=undefined && rec.data!=undefined?rec.data.isAsset:true);
            if (this.parentObj.includeProTax && this.parentObj.includeProTax.getValue() && this.parentObj.capitalGoodsAcquired && this.parentObj.capitalGoodsAcquired.getValue()) {
                this.parentObj.showGridTax(null, null, false);
            }
            if (this.parentObj && this.parentObj.includeProTax && this.parentObj.includeProTax.getValue() == true) {
                WtfGlobal.calculateTaxAmountUsingAdaptiveRoundingAlgo(this, false);
            }
        }
        if(this.store.getCount()>0&&this.store.getAt(this.store.getCount()-1).data['productid'].length<=0)
            return;
        if(!this.isNote && (!this.soLinkFlag)) {
            this.addBlankRow();            
        }
       
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
        this.parentObj.LineLevelTermTplSummary.overwrite(this.parentObj.LineLevelTermTpl.body,{
            lineLevelArray : lineLevelArray,
            TotalTaxAmt : WtfGlobal.addCurrencySymbolOnly(TotalTaxAmt,this.symbol)
        });
    },
    updateunitpricewithGST: function (obj,islinkingFlag) {
        /**
         * Price will does not change for linking cases.
         * !islinkingFlag is used so that setTaxAndRateAmountAfterIncludingGST will not be call in linking case while
         * changing the qty.
         */
        if (obj.field == "prtaxid" || obj.field == "rate" || (obj.field == "quantity" && !islinkingFlag) || obj.field == "showquantity" || obj.field == "dependentType" || obj.field == "discountispercent" || obj.field == "prdiscount") {
            taxamount = this.setTaxAmountAfterSelection(obj.record);
            obj.record.set("taxamount", taxamount);
            obj.record.set('isUserModifiedTaxAmount', false);
            if ((obj.field == "discountispercent" || obj.field == "prdiscount") && (this.parentObj.includingGST && this.parentObj.includingGST.getValue() == true)) {
                this.setTaxAndRateAmountAfterIncludingGST(obj.record);
            }
            if (obj.field == "prtaxid" && WtfGlobal.singaporecountry() && WtfGlobal.getCurrencyID() != Wtf.Currency.SGD && (this.isInvoice || this.isCash) && this.forCurrency != Wtf.Currency.SGD) {
                var record = WtfGlobal.searchRecord(this.parentObj.currencyStore, this.parentObj.Currency.getValue(), "currencyid");
                callGstCurrencyRateWin(this.id, record.data.currencyname + " ", obj, obj.record.get("gstCurrencyRate") * 1);
            }
            this.fireEvent('datachanged', this);
        }
    },
    
     setTaxAndRateAmountAfterIncludingGST : function (record) {
        if(record.data.prtaxid!="None"){
            var taxamount = this.setTaxAmountAfterIncludingGst(record,1);
            var amountwithOutGst = this.setTaxAmountAfterIncludingGst(record,2);
            record.set("taxamount",taxamount);
            record.set('isUserModifiedTaxAmount', false);
            if(amountwithOutGst!=0) {
                record.set("rate",amountwithOutGst);
            } else {
                record.set("rate",record.data.rateIncludingGst);
            }
        }
    },
    
    setTaxAmountAfterIncludingGst: function(rec, amountFlag) {//amountFlag=1 for taxamount and amountFlag=2 for actual amount with discount actualamount=3 amount with GST

        var quantity = rec.data.quantity;
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null) ? 0 : quantity;
        var discount = 0;
        var rateIncludingGst = getRoundofValueWithValues(rec.data.rateIncludingGst, Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        quantity = getRoundofValue(rec.data.quantity);
        var lineTermAmount = (rec.data.lineleveltermamount == undefined || rec.data.lineleveltermamount == null || rec.data.lineleveltermamount == '') ? 0 : rec.data.lineleveltermamount;
        var quantityAndAmount = 0;
        quantityAndAmount = rateIncludingGst * quantity;
        var origionalAmount = getRoundedAmountValue(quantityAndAmount);
        if (rec.data.partamount != 0) {
            var partamount = getRoundedAmountValue(rec.data.partamount);
            origionalAmount = getRoundedAmountValue(origionalAmount * (partamount / 100));
        }

        if (rec.data.prdiscount > 0) {
            var prdiscount = getRoundedAmountValue(rec.data.prdiscount);
            if (rec.data.discountispercent == 1) {
                discount = getRoundedAmountValue(origionalAmount * prdiscount / 100);
            } else {
                discount = prdiscount;
            }
        }
        var val = origionalAmount - discount + lineTermAmount;
        var taxpercent = 0;
        var index = this.taxStore.find('prtaxid', rec.data.prtaxid);
        if (index >= 0) {
            var taxrec = this.taxStore.getAt(index);
            taxpercent = getRoundedAmountValue(taxrec.data.percent);
        }
        var amount = 0.0;
        var taxamount = getRoundedAmountValue(val * taxpercent / (taxpercent + 100));
        var unitAmount = 0;
         if(quantity!=0){
             amount=getRoundofValueWithValues((val-taxamount),Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
             unitAmount=getRoundofValueWithValues(amount/quantity,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        }
        if (amountFlag == 1) {
            return taxamount;
        } else if (amountFlag == 2) {
            if (quantity != 0) {
                val = unitAmount;
            } else {
                val = 0;
            }
            return val;
        }
    },
     callupdateRowonProductLoad: function(obj) {
        if (obj != undefined || obj != null) {
            this.obj=obj;
            this.dataObj=this.obj;
            if(this.obj.field=='pid'){
                this.productComboStore.load();
                this.productComboStore.on('load',function(){
                    /* Cretae New link has been removed from lease module so count come 1 on type ahead in Product Grid*/
                    if(this.productComboStore.getCount()<1){
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
                    }else{
                        this.updateRow(this.obj);
                    }
                },this);
            }else{
                this.updateRow(this.obj);
            }
        }
    },
    onQuantityChanged: function(obj,rec,prorec) {
        var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
        var productuomid = "";
        if(productComboIndex==-1){
           prorec = this.getStore().getAt(0); 
        }else if (productComboIndex >= 0) {
            prorec = this.productComboStore.getAt(productComboIndex);
        }
            productuomid = prorec.data.uomid;
            if (obj.record.get("uomid") != undefined && productuomid != obj.record.get("uomid")) {
                obj.record.set("baseuomquantity", obj.record.get("quantity")*obj.record.get("baseuomrate"));
            } else {
                obj.record.set("baseuomrate", 1);
                obj.record.set("baseuomquantity", obj.record.get("quantity")*obj.record.get("baseuomrate"));
            }
        
        var isnotdecimalvalue = true;
        if (prorec.data.type != 'Service' && prorec.data.type != 'Non-Inventory Part') { // serial no for only inventory type of product
            if (prorec.data.isSerialForProduct) {
                var v = obj.record.data.quantity;
                v = String(v);
                var ps = v.split('.');
                var sub = ps[1];
                if (sub!=undefined && sub.length > 0) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.exceptionMsgForDecimalQty")], 2);
                    isnotdecimalvalue = false;
                    obj.record.set("quantity", obj.originalValue);
                    obj.record.set("baseuomquantity", obj.originalValue*obj.record.get("baseuomrate"));
                }
            }
        }
        
        if ((this.isFixedAsset || (this.isLeaseFixedAsset && !this.isQuotation)) && isnotdecimalvalue) {
            var productid = rec.get('productid');
            var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, productid, 'productid');
            if (productComboRecIndex == -1) {
                var proRecord = this.getStore().getAt(0);
                if (proRecord.get('isAsset') && obj.record.get("quantity") != 0) {
                    this.callFixedAssetDetailsWindow(obj.record, proRecord);
                }
            }else if (productComboRecIndex >= 0) {
                var proRecord = this.productComboStore.getAt(productComboRecIndex);
                if (proRecord.get('isAsset') && obj.record.get("quantity") != 0) {
                    this.callFixedAssetDetailsWindow(obj.record,proRecord);
                }
            }
        }
    },
    
    callFixedAssetDetailsWindow:function(record,productRec,readOnly){
        
        var quantity = record.get('quantity');
        
        this.FADetailsGrid=new Wtf.account.FADetails({
            title:'Asset Details',
            quantity:quantity,
            billDate:this.billDate,
            isGRLinkedInPI:this.parentObj.isGRLinkedInPI,
            modal:true,
            isCustomer:this.isCustomer,
            isLeaseFixedAsset:this.isLeaseFixedAsset,
            isFixedAsset:this.isFixedAsset,
            isLinkedFromReplacementNumber:this.isLinkedFromReplacementNumber,
            isLinkedFromCustomerQuotation:this.isLinkedFromCustomerQuotation,
            isQuotationFromPR: this.isQuotationFromPR,
            isPIFromPO: this.isPIFromPO,
            isPIFromVQ: this.isPIFromVQ,
            isPOfromVQ: this.isPOfromVQ,
            layout:'border',
            assetRec:productRec,
            lineRec:record,
            assetDetailsArray:record.get('assetDetails'),
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            fromPO: ((this.moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId && !this.isQuotationFromPR) || (this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId && !this.isPOfromVQ))? false : this.fromPO,
            isFromSalesOrder:this.isOrder&&this.isCustomer,
            isEdit:this.isEdit,
            moduleid:this.moduleid,
            isInvoice:true,
            width:950,
            readOnly:(readOnly||this.isLinkedTransaction),
            height:500,
            resizable : false,
            parentGrid:this,
            tagsFieldset:this.parentObj.tagsFieldset,
            isbilldateChanged : this.parentObj.isbilldateChanged,
            parentObj : this.parentObj
        });
        
        this.FADetailsGrid.show();
        
//        this.FADetailsGrid.on('beforeclose',function(panel){
//            if(panel.isFromSaveButton){
//                record.set("assetDetails", panel.assetDetails);
//                record.set("profitLossAmt", panel.profitLossAmtOnSelling);
//            }
//        }, this);
    },

    
    calTaxAmount:function(rec){
        var origionalAmount = rec.data.rate*rec.data.quantity;
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
    calTaxAmountWithoutExchangeRate:function(rec){
        var origionalAmount = rec.data.rate*rec.data.quantity;
        if(rec.data.partamount != 0) {
            origionalAmount = origionalAmount * (rec.data.partamount/100);
        }
        var discount = 0;//origionalAmount*rec.data.prdiscount/100
//        if(!(this.isNote||this.readOnly)) {
//            origionalAmount = this.calAmountWithExchangeRate(origionalAmount, rec);
//        }
        
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
    calculateTermLevelTaxes : function(termStore, rec, index,isNewProduct){
        
        var quantity = rec.data.quantity;
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
        var rate=getRoundofValueWithValues(rec.data.rate,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        quantity=getRoundofValue(rec.data.quantity);
        
        var amount=0;
        amount=rate*quantity;
        
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
            if(termJson.taxtype == 0){ // If Flat
                taxamount= this.TaxCalculation(termJson,rec,termJson.taxvalue,assessablevalue);              
            }else if(termJson.taxtype == 1){ // If percentage
                var taxamountfun = assessablevalue * termJson.taxvalue / 100;
                taxamount= this.TaxCalculation(termJson,rec,taxamountfun,assessablevalue);   
            }
            
            termJson.termamount = getRoundedAmountValue(taxamount);
            termJson.assessablevalue = getRoundedAmountValue(assessablevalue);
            var isPush =true;
            //isPush = eval(this.addTermForCalculation(termStore,termJson,totalVatTax,termSearchTerm,taxamount,vatFound,rec,uncheckedTerms,isNewProduct));
            
            if (termJson.termtype == Wtf.term.Others && (termJson.IsOtherTermTaxable != undefined && !termJson.IsOtherTermTaxable)) {
                if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                    //we are first rounding taxamount & then sum it , because it causes 0.1 difference in Total Amount.
                    FinalAmountNonTaxableTerm += getRoundedAmountValue(taxamount);
                } else {
                    FinalAmountNonTaxableTerm += taxamount;
                }
            } else {
                var gtaFlag=true;
                if(this.parentObj.GTAApplicable!=undefined && this.parentObj.GTAApplicable.getValue() && !this.isExciseTab){// For GTA Applicable - Indian Compliance
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
            if(isPush){ // Condition Details - from TERM List which need to add in calculation and which is not.
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
//        if(this.checkTermExist(termStore,Wtf.term.CST,totalVatTax) && this.venderDetails.data.interstateparty &&(!Wtf.isEmpty(isNewProduct) && !isNewProduct)){ // Condition Details - It check CST term exist in term list, if yes both(issue form and term's form) have same issue form.
//            if(!Wtf.isEmpty(this.parentObj.FormType.getRawValue())){
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"CST term with form type <b>"+this.parentObj.FormType.getRawValue()+"</b> is not found/selected in product.<br>Discard this invoice create/select term"], 2);       
//            }else{
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Please select Form To Issue field first"], 2);
//            } 
//        }
                
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
        // Iterate List of Terms in Reverse Order
        for(var i=0; i<termStore.length; i++){
//        for(var i=termStore.length-1; i>=0; i--){
            var termJson = termStore[i];
            var taxamount = 0;
            
            // Apply Tax on Asessable Value
            if(termJson.taxtype == 0){ // If Flat
                taxamount = termJson.taxvalue;
            }else if(termJson.taxtype == 1){ // If percentage
                taxamount = getRoundedAmountValue(unitPriceIncludingTax*termJson.taxvalue/(termJson.taxvalue+100));// assessablevalue * termJson.taxvalue / 100;
            }
            
            unitPriceIncludingTax = unitPriceIncludingTax - taxamount;
            
            termJson.termamount = getRoundedAmountValue(taxamount);
            termJson.assessablevalue = getRoundedAmountValue(unitPriceIncludingTax);
            
            if (termJson.termtype == Wtf.term.Others && (termJson.IsOtherTermTaxable != undefined && !termJson.IsOtherTermTaxable)) {
                //we are first rounding taxamount & then sum it , because it causes 0.1 difference in Total Amount.
                FinalAmountNonTaxableTerm += getRoundedAmountValue(taxamount);
            } else {
                finaltaxamount += taxamount;
            }
            finaltermStore.push(termJson);
        }
        
        sortArrOfObjectsByParam(finaltermStore, "termsequence", true);
        if(finaltaxamount){
           rec.set('recTermAmount', getRoundedAmountValue(finaltaxamount));
           rec.set('taxamount', getRoundedAmountValue(finaltaxamount));
           rec.set('amount', getRoundedAmountValue(quantityAndAmount- finaltaxamount));
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
    setTaxAmount:function(v,m,rec){
       var taxamount= this.calTaxAmount(rec);
       rec.set("taxamount",taxamount);
//        if(this.isNote||this.readOnly)
             return WtfGlobal.withoutRateCurrencySymbol(taxamount,m,rec);
//        return WtfGlobal.currencyRendererSymbol(taxamount,m,rec);
    },
    setTaxAmountWithotExchangeRate:function(v,m,rec){
        
        /*
         * ERP-40242 : In copy case and linking case, deactivated tax not shown.Hence, empty taxid set in record.          
         */
        if (rec.data.prtaxid != '' && this.fromPO) {
            var taxActivatedRec = WtfGlobal.searchRecord(this.taxStore, rec.data.prtaxid, "prtaxid");
            if (taxActivatedRec == null || taxActivatedRec == undefined || taxActivatedRec == "") {
                rec.set("prtaxid", "");
            }
        }
       var taxamount= parseFloat(getRoundedAmountValue(v)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1;
       if(rec.data.prtaxid==null || rec.data.prtaxid == undefined || rec.data.prtaxid == "" || rec.data.prtaxid == "None"){
           taxamount = 0;
       }
        rec.set("taxamount",taxamount);
       return WtfGlobal.withoutRateCurrencySymbol(taxamount,m,rec);
    },
    calAmount:function(v,m,rec){
        var origionalAmount = rec.data.rate*rec.data.quantity;
        if(rec.data.partamount != 0){
            origionalAmount = origionalAmount * (rec.data.partamount/100);
        }
        
        var discount = 0;//origionalAmount*rec.data.prdiscount/100
        origionalAmount = this.calAmountWithExchangeRate(origionalAmount, rec);
        
        
//        var stockQuantity = this.getStockQuantity(rec.get('productid'));
//        origionalAmount = stockQuantity*origionalAmount; // calculate amount with respect to purchase and sales uom
        
        if(rec.data.prdiscount > 0) {
            if(rec.data.discountispercent == 1){
                discount = (origionalAmount * rec.data.prdiscount) / 100;
            } else {
                discount = rec.data.prdiscount;
            }
        }
        
        var val=(origionalAmount)-discount;///rec.data.oldcurrencyrate
        
        rec.set("amountwithouttax",val);
        
//        var taxamount= this.calTaxAmount(rec);
        var taxamount = 0;
        if(rec.data.taxamount){
            taxamount= rec.data.taxamount;
        }
        val+=taxamount;

        rec.set("amount",val);
        if(this.isQuotationFromPR && val!==0 && (rec.data.orignalamount==undefined || rec.data.orignalamount==""))
            rec.set("orignalamount",val);
//       if(this.isNote||this.readOnly)
             return WtfGlobal.withoutRateCurrencySymbol(val,m,rec);
//       return WtfGlobal.currencyRendererSymbol(val,m,rec);
    },
    
    calAmountWithoutExchangeRate:function(v,m,rec){
        var rate = getRoundofValueWithValues(rec.data.rate,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
         /*
         * Check if rateincludegst is available or not
         */
        if(this.parentObj.includingGST && this.parentObj.includingGST.getValue()) {
            rate = rec.data.rateIncludingGst;
        }
        var qty = getRoundofValue(rec.data.quantity);
        qty = (qty == "NaN" || qty == undefined || qty == null)?0:qty;
        var origionalAmount =  getRoundedAmountValue(rate*qty);
        if(rec.data.partamount != 0){
            origionalAmount = origionalAmount * (rec.data.partamount/100);
        }
        
        var discount = 0;//origionalAmount*rec.data.prdiscount/100   
        if(rec.data.prdiscount > 0) {
            if(rec.data.discountispercent == 1){
                discount =getRoundedAmountValue((origionalAmount * rec.data.prdiscount) / 100);
            } else {
                discount = getRoundedAmountValue(rec.data.prdiscount);
            }
        }
        
        var val=(origionalAmount)-discount;///rec.data.oldcurrencyrate  
        rec.set("amountwithouttax",val);
        var taxamount = 0;
        if(!isNaN(rec.data.taxamount)){
            taxamount= getRoundedAmountValue(rec.data.taxamount);
        }
        
         /*
         * Check if rateincludegst is available or not. If yes then no need to add tax value in amount value
         */
        if(this.parentObj.includingGST && !this.parentObj.includingGST.getValue() && Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA) {
        val=parseFloat(val)+parseFloat(taxamount);
        }
        rec.set("amount",(parseFloat(getRoundedAmountValue(val)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1));
        if(this.isQuotationFromPR && val!==0 && (rec.data.orignalamount==undefined || rec.data.orignalamount==""))
        rec.set("orignalamount",val);
        return WtfGlobal.withoutRateCurrencySymbol(val,m,rec);
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
            subtotal+=total;
        }
        return subtotal;
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
    addBlank:function(){
       //this.setGridDiscValues();
        this.addBlankRow();
    },
    setGridDiscValues:function(){
        this.store.each(function(rec){
            if(!this.editTransaction)
                rec.set('prdiscount',0)
        },this);
    },
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

        productid = productid.substring(0, (productid.length - 1) )
            Wtf.Ajax.requestEx({
                    url:"ACCProduct/getIndividualProductPrice.do",
                    params:{
                        productid: productid,
                        affecteduser: this.affecteduser,
                        currency: this.parentObj.Currency.getValue(),
                        transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                        isFixedAsset : true,
                        carryin : (this.isCustomer)? false : true
                    }
                }, this,function(response){
                    var obj = response.data;
                    for(var i=0; i < obj.length ; i++){
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

                            //Case of copy invoice
                            if((this.copyInv&&prorec.data.quantity<(record.data.quantity*record.data.baseomrate)&&prorec.data.type!="Service")){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+WtfGlobal.getLocaleText("acc.field.is")+' '+prorec.data.quantity], 2);
                                record.set("quantity",0);
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
        
        
        this.store.each(function(rec){//converting in home currency
                if(rec.data.rowid==undefined){
                    rec.data.rowid='';
                    
                }
            },this);
        
        var arr=[];
        this.store.each(function(rec){
            var taxpercent=0;
            var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
            if(index>=0){
               var taxrec=this.taxStore.getAt(index);
                taxpercent=taxrec.data.percent;
                rec.set('taxpercent',taxpercent);
            }
            rec.data[CUSTOM_FIELD_KEY]=Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, this.moduleid).substring(13));
            rec.data[CUSTOM_FIELD_KEY_PRODUCT]=Wtf.decode(WtfGlobal.getCustomColumnDataForProduct(rec.data, this.moduleid).substring(20));
            //if(rec.data.rate!=0){
                arr.push(this.store.indexOf(rec));
           // }            
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
    checkDetails: function (grid) {
        var v = WtfGlobal.checkValidItems(this.moduleid, grid);
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
                    return "Error";
                }
            rec.data[CUSTOM_FIELD_KEY]=Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, this.moduleid).substring(13));
            arr.push(i);
            }
            	// arr.push(i); moved to above line cos of issue no: 20258
        }
        return WtfGlobal.getJSONArray(this,true,arr);
    },
    loadPOProduct:function(){
//        a a;
      if(this.store.getCount() > 0){
        this.fireEvent("productselect", this.store.getAt(0).get("productid"));
      }
      
      if(this.fromPO)
         this.store.each(function(rec){
              var taxamount= rec.get('rowTaxAmount');//this.calTaxAmount(rec);
             rec.set("taxamount",taxamount);
             rec.set('isUserModifiedTaxAmount', false);
                if (WtfGlobal.isIndiaCountryAndGSTApplied()) { // Put product type for GST calculation in lease
                    var producttype = rec!=undefined && rec.get('prodtype')!=undefined ?rec.get('prodtype'):"";
                    rec.set("producttype", producttype);
                }
             //rec.set("prdiscount",0);
             if(rec.data.rate===""){
                 var result = this.productComboStore.find('productid',rec.data.productid);
                 if(result >= 0){
                    var prorec=this.productComboStore.getAt(result);
                    rec.set("rate",prorec.data.initialprice);			
    		}                 
             }  
              if(WtfGlobal.isIndiaCountryAndGSTApplied() && rec.data['LineTermdetails'] != undefined  && rec.data['LineTermdetails'] != ""){
                    var termStore = [];                   
                        termStore = this.getTaxJsonOfIndia(rec);                   
                    if(this.parentObj && this.parentObj.includingGST && this.parentObj.includingGST.getValue() == true) {
                        this.getColumnModel().setRenderer(this.getColumnModel().findColumnIndex("amount"),WtfGlobal.withoutRateCurrencySymbol);
                        termStore = this.calculateTermLevelTaxesInclusive(termStore, rec);
                    } else {
                        this.getColumnModel().setRenderer(this.getColumnModel().findColumnIndex("amount"),this.calAmountWithoutExchangeRate.createDelegate(this));
                        termStore = this.calculateTermLevelTaxes(termStore, rec);
                    }                       
                    rec.set('LineTermdetails',Wtf.encode(termStore));
                    this.updateTermDetails();
                }
        },this);

      if(this.isCustomer && this.fromOrder && !this.isNote && !this.readOnly && !this.isOrder)
    	  this.checkSOLinkedProducts();
      if(this.soLinkFlag && this.isOrder ){ //Allow soLinkFlag for Generate SO from PO,Generate PO from SO and Generate CQ using VQ for showing correct prices of products
        this.store.each(function(rec){
            this.fireEvent('afteredit', {
                field : 'productid',
                value : rec.data.productid,
                record : rec,
                soflag : true
            });                
        },this);
         this.soLinkFlag = false;
        }
    },
    getTaxJsonOfIndia: function (prorec) {
        var obj_CST = eval(prorec.data['LineTermdetails']);
        var termStore = new Array();
        for (var i_CST = 0; i_CST < obj_CST.length; i_CST++) {
            termStore.push(obj_CST[i_CST]);
        }
        return termStore;
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
                        quantity = quantity + (rec.data.quantity*rec.data.baseuomrate);                                                     
                  }
                },this);
                quantity = quantity + (record.data.quantity*record.data.baseuomrate);
                var result = this.productComboStore.find('productid',record.data.productid);
                var ComboIndex=0;
                if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Products_on_type_ahead && result==-1){
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
                         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableinstockQtyfor")+' '+msg+'.<br>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
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
    loadPOGridStore:function(recids, flag, VQtoCQ,linkingFlag, isForInvoice, isPRlinktoVQ, FA_VQlinkToFA_PO, FA_POlinkToFA_PI, linkDisposalINV, isFA_VQlinkToPI,sopolinkflag){        
        this.store.load({
            params:{
                bills:recids,
                mode:43,
                closeflag:true,
                dtype: VQtoCQ?"report" : "trans",
                linkingFlag:linkingFlag,
                isForInvoice:isForInvoice,
                isFixedAsset : this.isFixedAsset,
                isLeaseFixedAsset:this.isLeaseFixedAsset,
                isPRlinktoVQ:isPRlinktoVQ,
                FA_VQlinkToFA_PO:FA_VQlinkToFA_PO,
                FA_POlinkToFA_PI:FA_POlinkToFA_PI,
                linkDisposalINV:linkDisposalINV,
                FA_VQlinkToPI:isFA_VQlinkToPI,
                sopolinkflag:sopolinkflag
            }
        });
        this.soLinkFlag = flag;
        if (this.parentObj.isIndiaGST) {
            /**
             * Show pop up in linking case if mismatch found in GST fields on current date
             */
            getLinkDateTocheckGSTDataOnDateCase(this.parentObj, this);
        }
    },
    showPriceWindow:function(btn,text,rec, obj){
        if(btn!="yes")return;
        callPricelistWindow(rec,"pricewindow",!this.isCustomer,this.billDate);
   //     this.priceStore.reload();
       // Wtf.getCmp("pricewindow").on('update',function(){this.loadPriceStore()},this);
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

    setCurrencyid:function(currencyid,rate,symbol,rec,store){
        this.symbol=symbol;
        this.currencyid=currencyid;
        this.rate=rate;
        for(var i=0;i<this.store.getCount();i++){
            this.store.getAt(i).set('currencysymbol',this.symbol);
            this.store.getAt(i).set('currencyrate',this.rate);
        }
        this.getView().refresh();
    //     this.store.commitChanges();

     },
    setCurrencyAmount:function(amount){
//    if(this.isNote)
        return amount;
//          return (amount*this.rate)
    },
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
    getComboNameRenderer : function(combo){
        return function(value,metadata,record,row,col,store) {
            var idx = WtfGlobal.searchRecordIndex(combo.store,value,combo.valueField);
            var fieldIndex = "pid";
            if(idx == -1) {
                if(record.data["pid"] && record.data[fieldIndex].length>0) {
                    return record.data[fieldIndex];
                }else{
                    return "";
                }
            }
            var rec = combo.store.getAt(idx);
            var displayField = rec.get(combo.displayField);
            record.set("productid", value);
            record.set("pid", displayField);
//            record.set("rate", 1000);
            return displayField;
        }
    }   
});

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