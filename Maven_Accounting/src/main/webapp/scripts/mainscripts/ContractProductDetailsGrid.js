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
Wtf.account.ContractProductDetailsGrid=function(config){
    this.isLinkedFromTransaction = (config.isLinkedFromTransaction == null || config.isLinkedFromTransaction == undefined)? false : config.isLinkedFromTransaction;;
    this.parentCmpID=config.parentCmpID;
    this.isCustomer=config.isCustomer;
    this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
    this.currencyid=config.currencyid;
    this.productID=null;
    this.soLinkFlag = null;
    this.updaterowtax = true;//used in conjunction with this.soLinkFlag
    this.id=config.id;
    this.isOrder=config.isOrder;
    this.isFromGrORDO=(config.isFromGrORDO != null || config.isFromGrORDO != undefined)?config.isFromGrORDO:false;
    this.isNormalContract=(config.isNormalContract != null || config.isNormalContract != undefined)?config.isNormalContract:false;
    this.record=config.record;
    this.billDate=new Date();
    this.dateChange=false;
    this.pronamearr=[];
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
    this.noteTemp=config.noteTemp;
    this.fromOrder=config.fromOrder;
    this.affecteduser = "";
    this.forCurrency="";
    
    if(this.productOptimizedFlag!= undefined || this.productOptimizedFlag==Wtf.Show_all_Products){
        this.productComboStore=this.isCustomer?Wtf.FixedAssetAndProductLeaseStore:Wtf.productStore;
    }else if(this.productOptimizedFlag==Wtf.Products_on_type_ahead || this.productOptimizedFlag==Wtf.Products_on_Submit){
        this.productComboStore=this.isCustomer?Wtf.FixedAssetAndProductLeaseStoreOptimized:Wtf.productStoreOptimized;
    }
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
    this.isRFQ =config.isRFQ;
    this.createStore();
    this.createComboEditor();
    this.createColumnModel();
    var colModelArray = [];
    colModelArray = GlobalColumnModel[this.moduleid];
    if(colModelArray) {
        colModelArray.concat(GlobalColumnModelForProduct[this.moduleid]);
    }
    WtfGlobal.updateStoreConfig(colModelArray, this.store);
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
    Wtf.account.ContractProductDetailsGrid.superclass.constructor.call(this,config);
    this.addEvents({
        'datachanged':true, //Event fired when grid data is changed. Mostly used to calculate subtotal
        'pricestoreload':true,
        'productselect' : true,//Event fired to load data for collapsible panel store
        'productdeleted' : true,//Event fired to remove data for collapsible panel store
        'customerchangepriceload' : true //Event fired when customer is changed
    });
}
Wtf.extend(Wtf.account.ContractProductDetailsGrid,Wtf.grid.EditorGridPanel,{
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
         Wtf.account.ContractProductDetailsGrid.superclass.onRender.call(this,config);
         this.isValidEdit = true;
         //this.on('render',this.addBlankRow,this);
//         this.on('afteredit',this.updateRow,this);
        if(this.productOptimizedFlag!=Wtf.Products_on_Submit){
             this.on('afteredit',this.updateRow,this);
         } else {
             this.on('afteredit',this.callupdateRowonProductLoad,this);
         }
        this.on('validateedit',this.checkRow,this);
        this.on('rowclick',this.handleRowClick,this);
        this.on('cellclick',this.RitchTextBoxSetting,this);
        this.on('beforeedit',function(e){
         
         
         if(this.isLinkedFromTransaction){// if linked from SO don't let it edit'
             e.cancel = true;
             return;
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
              Wtf.Ajax.requestEx({
                url: "ACCProduct/getIndividualProductPrice.do",
                params: {
                    productid: e.record.data.productid,
                    affecteduser: this.affecteduser,
                    transactiondate: WtfGlobal.convertToGenericDate(this.billDate),
                    carryin: (this.isCustomer) ? false : true
                }
            }, this, function(response) {

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


            }, this);
             
             
         },this);
//         if(!this.isNote && !this.readOnly){
//	         if(this.record == null || this.record == undefined && this.getColumnModel().getColumnById(this.id+"prtaxid").hidden == undefined && this.getColumnModel().getColumnById(this.id+"taxamount").hidden == undefined){
//	        	 
//                         this.getColumnModel().setHidden(this.getColumnModel().getIndexById(this.id+"prtaxid"), true);				// 21241   If statement added bcos could not use the event destroy for column model
//	        	 this.getColumnModel().setHidden(this.getColumnModel().getIndexById(this.id+"taxamount"), true);							// and also could not call the createColumnModel() method from onRender
//	         }
//         }     
        this.hideShowCustomizeLineFields();
        /*
         * Hide column P.tax class,Tax amount for India country
         */
         if(WtfGlobal.isIndiaCountryAndGSTApplied()){
//	         if(this.record == null || this.record == undefined && this.getColumnModel().getColumnById(this.id+"prtaxid").hidden == undefined && this.getColumnModel().getColumnById(this.id+"taxamount").hidden == undefined){
	         if(this.isEdit||this.record == null || this.record == undefined && this.getColumnModel().isHidden(this.getColumnModel().findColumnIndex("prtaxid")) == undefined && this.getColumnModel().isHidden(this.getColumnModel().findColumnIndex("taxamount")) == undefined){
                         this.getColumnModel().setHidden(this.getColumnModel().findColumnIndex("prtaxid"), true);				// 21241   If statement added bcos could not use the event destroy for column model
	        	 this.getColumnModel().setHidden(this.getColumnModel().findColumnIndex("taxamount"), true);							// and also could not call the createColumnModel() method from onRender
	         }
         } 
     },
    hideShowCustomizeLineFields: function() {
        if (this.moduleid == Wtf.Acc_Contract_ModuleId || this.moduleid == Wtf.Acc_Lease_Contract) {
            Wtf.Ajax.requestEx({
                url: "ACCAccountCMN/getCustomizedReportFields.do",
                params: {
                    flag: 34,
                    moduleid: this.moduleid,
                    reportId: 1,
                    isFormField: true,
                    isLineField: true
                }
            }, this, function(action, response) {
                if (action.success && action.data != undefined) {
                    this.customizeData = action.data;
                    var cm = this.getColumnModel();
                    for (var i = 0; i < action.data.length; i++) {
                        for (var j = 0; j < cm.config.length; j++) {
                            if (cm.config[j].dataIndex == action.data[i].fieldDataIndex) {
                                cm.setHidden(j, action.data[i].hidecol);
                                cm.setEditable(j, !action.data[i].isreadonlycol);
                                if (action.data[i].fieldlabeltext != null && action.data[i].fieldlabeltext != undefined && action.data[i].fieldlabeltext != "") {
                                    cm.setColumnHeader(j, action.data[i].fieldlabeltext);
                                }
                            }
                        }
                    }
                    this.reconfigure(this.store, cm);
                }
            });
        }
    },
    descriptionRenderer :function(val, meta, rec, row, col, store) {
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
            {name:'location'},
            {name:'warehouse'},
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
            {name:'availableQtyInSelectedUOM',defValue:0.00},
            {name:'isAnotherUOMSelected'},
            {name:'blockLooseSell'},
            {name:'pocountinselecteduom'},
            {name:'socountinselecteduom'},
            {name:'baseuomname'},
            {name:'multiuom'},
            {name:'uomname'},
            {name:'baseuomid'},
            {name:'uomid'},
            {name:'stockuom'},
            {name:'caseuom'},
            {name:'inneruom'},
            {name:'caseuomvalue'},
            {name:'inneruomvalue'},
            {name:'baseuomrate',defValue:1.00},
            {name:'copyquantity',mapping:'quantity'},
            {name:'uomschematypeid'},
            {name:'rate'},
            {name:'unitPricePerInvoice',defValue:0},
            {name:'rateIncludingGst'},
            {name:'isRateIncludingGstEnabled',defValue:'0'},
            {name:'rateinbase'},
            {name:'partamount',defValue:0},
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
            {name:'typeid',defValue:0},
            {name:'isNewRecord',defValue:'1'},
            {name:'producttype'},
            {name:'permit'},
            {name:'linkto'},
            {name:'linkid'},
            {name:'linktype'},
            {name:'savedrowid'},
            {name:'batchdetails'},
            {name:'islockQuantityflag'},
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
            {name:'copybaseuomrate',mapping:'baseuomrate'},  //for handling inventory updation 
            {name: 'price'}, // added in record due to set auto populate value of price in add price in master window
            {name: 'customer'}, // added in record due to set auto populate value of customer in add price in master window
            {name: 'vendor'}, // added in record due to set auto populate value of vendor in add price in master window
            {name:'pid'},
            {name:'priceSource'},
            {name: 'isLocationForProduct'},
            {name: 'isRowForProduct'},
            {name: 'isRackForProduct'},
            {name: 'isBinForProduct'},
            {name: 'isWarehouseForProduct'},
            {name: 'isBatchForProduct'},
            {name: 'isSerialForProduct'},
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
            {name: 'socount'},
            {name: 'sicount'},
            {name:'hasAccess'},
            {name:'assetDetails'},
            {name:'isAsset',type:'boolean',defValue:false},
            {name:'savedrowid'},
            {name:'originalTransactionRowid'},
            {name:'lineleveltermamount',defValue:0},
            {name:'recTermAmount'},
            {name:'OtherTermNonTaxableAmount'},
            {name:'LineTermdetails'}
        ]);
        var url=Wtf.req.account+((this.fromOrder||this.readOnly)?((this.isCustomer)?'CustomerManager.jsp':'VendorManager.jsp'):((this.isCN)?'CustomerManager.jsp':'VendorManager.jsp'));
        if(this.fromOrder)
           url=Wtf.req.account+(this.isCustomer?'CustomerManager.jsp':'VendorManager.jsp');
        this.store = new Wtf.data.Store({
            url:url,
            pruneModifiedRecords:true,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.storeRec)
        });
        this.store.on('load',this.loadPOProduct,this);      
 //       chkProductPriceload();
    },
createComboEditor:function(){
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
        });
//        chkproductload();
            this.productComboStore.on('beforeload',function(s,o){
                if(!o.params)o.params={};
                var currentBaseParams = this.productComboStore.baseParams;
                currentBaseParams.getSOPOflag=true;
                currentBaseParams.affecteduser=this.affecteduser,
                currentBaseParams.onlyProduct=this.isOrder?false:true
                currentBaseParams.startdate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true));
                currentBaseParams.enddate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false));  
                currentBaseParams.moduleid = this.moduleid;         // Passing Moduleid
                currentBaseParams.searchProductString = (this.productOptimizedFlag==Wtf.Products_on_Submit && !this.isProductLoad)? this.productId.getValue():"";
                currentBaseParams.isFreeTextSearching = (this.productOptimizedFlag==Wtf.Products_on_Submit);
                this.productComboStore.baseParams=currentBaseParams;        
            },this); 
     this.productComboStore.load();
     if(this.productOptimizedFlag== undefined || this.productOptimizedFlag==Wtf.Show_all_Products){
//        if(this.isCustomer)
//        	chkproductSalesload();
//        else
//        	chkproductload();                                        
       this.productEditor=new Wtf.form.ExtFnComboBox({
            name:'pid',
            store:this.productComboStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
            typeAhead: true,
            isProductCombo: true,
            selectOnFocus:true,
            maxHeight:250,
            listAlign:"bl-tl?",//[ERP-5149] To expand list of combobox always on top. 
           valueField:'productid',
            displayField:'pid',
            extraFields:['productname','type'],
            extraComparisionField:'pid',// type ahead search on acccode as well.
            listWidth:450,
            extraComparisionFieldArray:['pid','productname'],
            lastQuery:'',
            //editable:false,
            scope:this,
            hirarchical:true,
          // addNewFn:this.openProductWindow.createDelegate(this),
            forceSelection:true,
            isProductCombo:true
        });
     } else{
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
            hideAddButton : true,//Added this Flag to hide AddNew  Button  
            addNewFn:this.openProductWindow.createDelegate(this),
            forceSelection:true,
            isProductCombo:true
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
            //if(!this.isRFQ)
                this.productEditor.addNewFn=this.openProductWindow.createDelegate(this);
        }
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
           {name: 'applydate', type:'date'}

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
                includeDeactivatedTax: this.isEdit != undefined ? this.isEdit : false
            }
        });
//        if(this.readOnly)
            this.taxStore.load();

        this.transTax= new Wtf.form.FnComboBox({
            hiddenName:'prtaxid',
            anchor: '100%',
            store:this.taxStore,
            valueField:'prtaxid',
            forceSelection: true,
            displayField:'prtaxname',
//            addNewFn:this.addTax.createDelegate(this),
            scope:this,
            displayDescrption:'taxdescription',
            selectOnFocus:true
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.tax, Wtf.Perm.tax.edit))
            this.transTax.addNewFn=this.addTax.createDelegate(this);
        this.transQuantity=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength:10,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
        });
        this.transBaseuomrate=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength:10
        });
        this.editprice=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,    
            maxLength:14
        });                        
        this.editPriceIncludingGST=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,    
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
        },{
            header:WtfGlobal.getLocaleText("acc.common.add"),
            dataIndex:"add",
            align:'center',
            width:40,
            renderer: this.addProductList.createDelegate(this)

        });

        if(this.productOptimizedFlag!=Wtf.Products_on_Submit){
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.product.gridProductID"),//"Product ID",
                width:200,
                dataIndex:(this.readOnly ||this.productOptimizedFlag==Wtf.Products_on_type_ahead)? 'pid' : 'productid',
                renderer:(this.productOptimizedFlag==Wtf.Products_on_type_ahead)?(this.readOnly?"":this.getComboNameRenderer(this.productEditor)):(this.readOnly?"":Wtf.comboBoxRenderer(this.productEditor)),
                editor:(this.readOnly||this.isViewTemplate)?"":this.productEditor
                
            });
        }else{
             columnArr.push({
                header: WtfGlobal.getLocaleText("acc.product.gridProductID"),//"Product ID",
                dataIndex:'pid',
                editor:(this.isViewTemplate||this.readOnly||this.productOptimizedFlag!=Wtf.Products_on_Submit)?"":this.productId,
                width:200
            });
        }
        columnArr.push(
        {
           header: WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),//"Product Name",
           dataIndex: 'productname',
           width:180
        },
//        {
//            header:WtfGlobal.getLocaleText("acc.invoice.gridProduct"),//"Product",
//            width:180,
//            dataIndex:this.readOnly?'productname':'productid',
//            renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.productEditor),
//            editor:(this.isNote||this.readOnly)?"":this.productEditor
//        },
        {
            header: WtfGlobal.getLocaleText("acc.je.acc"),
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
             header:WtfGlobal.getLocaleText("acc.invoice.gridDescription"),//"Description",
             dataIndex:"desc",
             hidden:this.isNote,
             width:200,
             editor:(this.isNote||this.readOnly)?"":this.remark,
             renderer:this.descriptionRenderer
//                     function(val){
//                 var regex = /(<([^>]+)>)/ig;
//                val = val.replace(/(<([^>]+)>)/ig,"");
//                if(val.length<50)
//                    return val;   
//                else
//                    return val.substring(0,50)+" ...";   
//            }
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
             hidden:((Wtf.account.companyAccountPref.countryid == '203')?((!this.isCustomer && !this.isQuotation && !this.isNote)?false:true):true),
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
            dataIndex:this.readOnly?'uomname':'uomid',
            renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.uomEditor),
            editor:(this.isNote||this.readOnly)?"":this.uomEditor
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridRateToBase"),//Base UOM Rate
             dataIndex:"baseuomrate",
             align:'left',
             width:100,
             renderer:this.conversionFactorRenderer(this.productComboStore,"productid","uomname",this.store),
             editor:(this.isNote||this.readOnly)?"":this.transBaseuomrate
         },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridQtyInBase"),//Base UOM Quantity
             dataIndex:"baseuomquantity",
//             hidden:true,
             align:'right',
             width:150,
             renderer:this.storeRenderer(this.productComboStore,"productid","uomname",this.store)
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
             header:WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingGST"),// "Unit Price Including GST",
             dataIndex: "rateIncludingGst",
             align:'right',
             id:this.id+"rateIncludingGst",
             fixed:true,
             width:150,
             renderer:WtfGlobal.withoutRateCurrencySymbol,
             hidden:true,
             editor:(this.isNote||this.readOnly)?"":this.editPriceIncludingGST,
             editable:true
//             hidden: this.noteTemp || this.isRFQ
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice"),// "Unit Price",
             dataIndex: "rate",
             id:this.id+"rate",
             align:'right',
             width:150,
             renderer:WtfGlobal.withoutRateCurrencySymbol,
             editor:(this.isNote||this.readOnly)?"":this.editprice,
             editable:true,
             hidden: this.noteTemp || this.isRFQ
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridUnitPricePerInvoice"),// "Unit Price Per Invoice",
             dataIndex: "unitPricePerInvoice",
             align:'right',
             width:170,
             renderer:WtfGlobal.withoutRateCurrencySymbol
        },{
             header:WtfGlobal.getLocaleText("acc.field.PartialAmount(%)"),
             dataIndex:"partamount",
             align:'right',
             id:this.id+"partdisc",
             hidden:!this.isNote || this.noteTemp || !this.isCustomer,
             width:120,
             fixed:true,
             renderer:function(v){return'<div class="currency">'+parseFloat(v).toFixed(2)+'%</div>';},
             editor:this.partAmount
        },{
            header: WtfGlobal.getLocaleText("acc.field.DiscountType"),
            width:100,
            dataIndex:'discountispercent',
            id:this.id+"discountispercent",
            fixed:true,
            hidden:this.isQuotation?false:(this.isRequisition || this.noteTemp),
            renderer:Wtf.comboBoxRenderer(this.rowDiscountTypeCmb),
            editor:(this.isNote||this.readOnly)?"":this.rowDiscountTypeCmb
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridDiscount"),//"Discount",
             dataIndex:"prdiscount",
             id:this.id+"prdiscount",
             align:'right',
             fixed:true,
             width:70,
             hidden:this.isQuotation?false:(this.isRequisition || this.noteTemp),
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
             fixed:true,
             width:80,
             hidden:false,//!(this.editTransaction||this.readOnly) || this.noteTemp,// || this.isOrder,
             renderer:Wtf.comboBoxRenderer(this.transTax),
             editor:this.readOnly||this.isNote?"":this.transTax  //this.transTax
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridTaxAmount"),//"Tax Amount",
             dataIndex:"taxamount",
             id:this.id+"taxamount",
              fixed:true,
             //align:'right',
             width:90,
             editor:this.transTaxAmount,
             hidden:false,//!(this.editTransaction||this.readOnly)|| this.noteTemp, // || !this.isOrder,
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
             hidden: (!this.isNote ||this.noteTemp),     //(this.isQuotationFromPR ? false : (!this.isNote ? this.noteTemp: true)) ||this.editTransaction,
             renderer:(this.isNote||this.readOnly?WtfGlobal.withoutRateCurrencySymbol:WtfGlobal.currencyRendererSymbol)
        },{
           header:this.isRequisition ? (WtfGlobal.getLocaleText("acc.field.EstimatedCost")): this.isNote?WtfGlobal.getLocaleText("acc.invoice.gridCurAmt"):WtfGlobal.getLocaleText("acc.invoice.gridAmount"),//"Current Amount ":"Amount",
             dataIndex:"amount",
             hidden: this.isRFQ,
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
             hidden: true, //(this.isRequisition && this.editTransaction) ? false : true,
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
          },{   //added the add serial icon at last of grid
             header: '',
             align:'center',
            align:'center',
                    renderer: function(a, b, c) {
                        var isAsset = c.data.isAsset;
                        if (isAsset) {
                            return viewRenderer();
                        } else if (Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory) {
                            return serialRenderer();
                        }

                    },
             dataIndex:'serialwindow',
             id:this.id+'serialwindow',
             hidden:(this.isNormalContract),
             width:40
          });
        if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.invoicegrid.TaxAmount"),//"Total Tax Amount",
                dataIndex:"recTermAmount",
               // hidden : this.isRFQ || this.isRequisition ? true : false,
                align:'right',
                width:100,
                renderer: WtfGlobal.withoutRateCurrencySymbol
            },{
                header: WtfGlobal.getLocaleText("acc.invoicegrid.tax"), 
                align: 'center',                
                width: 40,
                dataIndex:"LineTermdetails",
                renderer: this.addRenderer.createDelegate(this),
                //hidden:  this.isRFQ || this.isRequisition ? true : false 
            });
        }      
        if(!this.isNote && !this.readOnly) {
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),//"Action",
                align:'center',
                width:130,
                renderer: this.deleteRenderer.createDelegate(this)
            });
        }
        this.cm=new Wtf.grid.ColumnModel(columnArr);                
    },
    addRenderer: function(v, m, rec) {
        var hideUnitPriceAmount = this.isCustomer ? !Wtf.dispalyUnitPriceAmountInSales : !Wtf.dispalyUnitPriceAmountInPurchase;
        if (this.isModuleForAvalara) {
            return getToolTipOfAvalaraTerms(v, m, rec, hideUnitPriceAmount);
        } else {
            return getToolTipOfTermsfun(v, m, rec, hideUnitPriceAmount);
        }
    },
      serialRenderer:function(v,m,rec){
        return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.serial.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.serial.desc.title")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
    },
    viewRenderer: function(v, m, rec) {
        return "<div class='view pwnd view-gridrow'  title='View Asset Details '></div>";
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
            return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        }
    },
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    handleRowClick:function(grid,rowindex,e){
        var isAutoGenerateDO = false;
        if (this.moduleid==Wtf.Acc_Lease_Contract && Wtf.getCmp(this.parentCmpID) != undefined && Wtf.getCmp(this.parentCmpID).autoGenerateDO != undefined) {
            if (Wtf.getCmp(this.parentCmpID).autoGenerateDO.checked == true) {
                isAutoGenerateDO = true;
            }
        }
        if(e.getTarget(".delete-gridrow") && this.moduleid!=Wtf.Acc_Lease_Contract){
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
        } else if(e.getTarget(".serialNo-gridrow")){//serial no window
            var store=grid.getStore();
            var record = store.getAt(rowindex);
            var productid = record.get('productid');
            var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, productid, 'productid');
            if(productComboRecIndex >=0){
                var proRecord = this.productComboStore.getAt(productComboRecIndex);
                if(proRecord.data.type!='Service' && proRecord.data.type!='Non-Inventory Part'){  //serial no for only inventory type of product
                    if (Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.account.companyAccountPref.isLocationCompulsory || Wtf.account.companyAccountPref.isWarehouseCompulsory || Wtf.account.companyAccountPref.isRowCompulsory || Wtf.account.companyAccountPref.isRackCompulsory || Wtf.account.companyAccountPref.isBinCompulsory) {
                        if (isAutoGenerateDO) {
                            if (proRecord.data.isLocationForProduct || proRecord.data.isWarehouseForProduct || proRecord.data.isBatchForProduct || proRecord.data.isSerialForProduct || proRecord.data.isRowForProduct || proRecord.data.isRackForProduct || proRecord.data.isBinForProduct)
                            {
                                if (this.isCustomer) {
                                    var batchdetails = record.data.batchdetails;
                                    Wtf.Ajax.requestEx({
                                        url: "ACCInvoice/getBatchRemainingQuantity.do",
                                        params: {
                                            batchdetails: batchdetails,
                                            transType: Wtf.Acc_Lease_DO,
                                            isEdit: this.isEdit,
                                            linkflag: this.blockQtyFlag
                                        }
                                    }, this, function(res, req) {
                                        this.AvailableQuantity = res.quantity;
                                        this.callSerialNoWindow(record);
                                        return;
                                    }, function(res, req) {
                                        return false;
                                    });
                                }
                            } else {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.batchserial.Functinality")], 2);   //Batch and serial no details are not valid.
                                return;
                            }
                        } else {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msg.enableDOoptioninSI")], 2);
                            return;
                        }
                    }
                } else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.batchserial.funforInventoryitems")], 2);   //Batch and serial no details are not valid.
                    return;
                }

            }
        }else if(e.getTarget(".view-gridrow") && isAutoGenerateDO){
            /**
             * For Asset Details
             */
            var store=grid.getStore(); 
            var total=store.getCount();
            if(rowindex==total){
                return;
            }
            var record = store.getAt(rowindex);
                var productid = record.get('productid');
                var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, productid, 'productid');
                if(productComboRecIndex >=0){
                    var proRecord = this.productComboStore.getAt(productComboRecIndex);
                    if(proRecord.get('isAsset') && record.get("quantity") != 0)
                        this.callFixedAssetDetailsWindow(record,proRecord);
                
            }

        }else if(e.getTarget(".add-gridrow")){
            if(this.isLinkedFromTransaction){
                return;
            } else {
                this.showProductGrid();
            }
        }else {
            this.fireEvent("productselect", grid.getStore().getAt(rowindex).get("productid"));
        }
    },
    
    showProductGrid : function() {

        this.productSelWin = new Wtf.account.ProductSelectionWindow({
            renderTo: document.body,
            height : 600,
            width : 700,
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
            isCustomer : this.isCustomer
        });
        this.productSelWin.show();
        
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
        var rec=obj.record;
        
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
                if(prorec.data.type=='Service'){
//                          WtfComMsgBox(["Warning","UOM can not be set for Service and Non-Inventory products. "], 2);
                    return false;
                } else if(prorec.data.type!='Service' && !prorec.data.multiuom){
//                          WtfComMsgBox(["Warning","Multi UOM not allowed for the selected product. "], 2);
                    return false;
                }
            }else if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag!=Wtf.Show_all_Products && prorec==undefined){
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
        if(obj.field=="productid"){
            
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
                    quantity = quantity + (proqty*obj.record.data['baseuomrate']);  
                    
                    if(availableQuantity<quantity){
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
                    if(prorec.data['quantity']<quantity){
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
                }else if(!Wtf.account.companyAccountPref.withinvupdate&&this.isCustomer&&prorec.data['quantity']<(proqty*obj.record.data['baseuomrate'])&&prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation&&!this.isOrder){
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
                                  rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                        }
                }else if( isAutoGenerateDO &&this.isCustomer&&prorec.data['quantity']<(proqty*obj.record.data['baseuomrate'])&&prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation&&!this.isOrder){
                    this.isValidEdit = false;
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+' is '+prorec.data['quantity']], 2);
//                    obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCSareexceedingthequantity")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+WtfGlobal.getLocaleText("acc.field.is")+prorec.data['quantity']+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
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
            if(prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part') {
                var availableQuantity = prorec.data.quantity;
                var lockquantity = prorec.data.lockquantity;
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
                    if(this.editTransaction&&!this.copyInv&&(availableQuantity-lockquantity) < quantity) {
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCS/CIareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+WtfGlobal.getLocaleText("acc.field.is")+availableQuantity+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
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
                    if((!this.editTransaction||this.copyInv) &&  (availableQuantity-lockquantity) < quantity) {
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
        } else if(isAutoGenerateDO &&this.isCustomer&&(obj.field=="quantity"||obj.field=="baseuomrate")&&obj.record.data['productid'].length>0&&!this.isQuotation&&!this.isOrder){
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
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Remainingamountfortheselectedproductis")+WtfGlobal.getCurrencySymbol()+" "+(rec.data['amount'])], 2);
                    
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
        this.store.add(newrec);
    },   
    updateRow:function(obj){
        if(obj!=null){
            this.productComboStore.clearFilter(); // Issue 22189
            var rec=obj.record;
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
                                currency: this.parentObj.Currency.getValue(),
                                quantity: obj.record.data.quantity,
                                transactiondate : WtfGlobal.convertToGenericDate(this.billDate)
                            }
                        }, this,function(response){
                            datewiseprice =response.data[0].price;
                            var modifiedRate;
                            modifiedRate=WtfGlobal.getIndividualProductPriceInMultiCurrency(rec,datewiseprice);
                            if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                                obj.record.set("baseuomquantity", quantity*obj.value);
                                obj.record.set("rate", modifiedRate*obj.value);
                            }else {
                                obj.record.set("baseuomrate", 1);
                            } 
                            if(Wtf.account.companyAccountPref.isMinMaxOrdering && (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Purchase_Order_ModuleId)){
                                this.checkMinMaxOrderingQuantity(obj,prorec);
                            }
                            WtfGlobal.setDefaultWarehouseLocation(obj, rec,false);
                            this.fireEvent('datachanged',this);
                        }, function(){

                            });
                    }                     
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
                            transactiondate: WtfGlobal.convertToGenericDate(this.billDate)
                        }
                    }, this,function(response){
                        datewiseprice = response.data[0].price;
                        var modifiedRate = WtfGlobal.getIndividualProductPriceInMultiCurrency(rec,datewiseprice);
                        if (obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")) {
                            obj.record.set("baseuomquantity", quantity*obj.value);
                            obj.record.set("rate", modifiedRate*obj.value);
                        } else {
                            obj.record.set("baseuomrate", 1);
                        } 
                        WtfGlobal.setDefaultWarehouseLocation(obj, rec,false);
                        this.fireEvent('datachanged',this);
                    }, function(){});
                    
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
                      if (this.UomSchemaType == Wtf.UOMSchema) {//for Schema type
                        var selectedUOMRec = WtfGlobal.searchRecord(Wtf.uomStore, obj.value, 'uomid');
                        var uomschemaid = "";
                        if (productuomid != obj.value) {
                            uomschemaid = prorec.data.uomschematypeid;
                        }
                        obj.record.set("uomname", selectedUOMRec.data['uomname']);
//                            if(productuomid != obj.value){
                        obj.record.set("isAnotherUOMSelected", false);
                        Wtf.Ajax.requestEx({
                            url: "ACCProductCMN/getIndividualProductPrice.do",
                            params: {
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
                                transactiondate: WtfGlobal.convertToGenericDate(this.billDate)
                            }
                        }, this, function (response) {
                            baseuomrate = response.data[0].baseuomrate;
                            rateperuom = response.data[0].rateperuom;
                            datewiseprice = response.data[0].price;

                            var availableQtyInSelectedUOM = response.data[0].availableQtyInSelectedUOM;
                            var pocountinselecteduom = response.data[0].pocountinselecteduom;
                            var socountinselecteduom = response.data[0].socountinselecteduom;

                            obj.record.set("baseuomrate", baseuomrate);
                            obj.record.set("baseuomquantity", quantity * baseuomrate);
                            obj.record.set("availableQtyInSelectedUOM", availableQtyInSelectedUOM);
                            obj.record.set("isAnotherUOMSelected", true);
                            obj.record.set("pocountinselecteduom", pocountinselecteduom);
                            obj.record.set("socountinselecteduom", socountinselecteduom);
                            var modifiedRate;
                            modifiedRate = WtfGlobal.getIndividualProductPriceInMultiCurrency(rec, datewiseprice);
                            if (productuomid == obj.value) {
                                obj.record.set("baseuomrate", 1);
                                obj.record.set("baseuomquantity", quantity * obj.record.get("baseuomrate"));
                                obj.record.set("rate", modifiedRate * baseuomrate);
                                obj.record.set("rateIncludingGst", modifiedRate * baseuomrate);
                            } else {
                                obj.record.set("rate", modifiedRate * baseuomrate);
                                obj.record.set("rateIncludingGst", modifiedRate * baseuomrate);
                            }
//                            if(baseuomrate!=1){
//                                obj.record.set("rate",modifiedRate*quantity*baseuomrate);
//                                obj.record.set("rateIncludingGst", modifiedRate*quantity*baseuomrate);
//                            }
//                                    if(modifiedRate !=0){ //temp check for not clearing original value
//                                        obj.record.set("rate", modifiedRate);
//                                        obj.record.set("rateIncludingGst", modifiedRate);
//                                    }  
                            WtfGlobal.setDefaultWarehouseLocation(obj, rec, false);
                            this.fireEvent('datachanged', this);
                        }, function () {

                        });
//                            } else {
//                                obj.record.set("baseuomrate", 1);
//                                obj.record.set("baseuomquantity", quantity*obj.record.get("baseuomrate"));
//                                obj.record.set("rate",price*baseuomrate);
//                            }    
                    } else { //for packeging UOM type                             
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
                                obj.record.set("baseuomquantity", (quantity) * (prorec.data.caseuomvalue));
                                obj.record.set("baseuomrate", (prorec.data.caseuomvalue));
                                obj.record.set("rate", modifiedRate * prorec.data.caseuomvalue);
                            } else if (obj.value == prorec.data.inneruom) {
                                obj.record.set("baseuomquantity", (quantity) * (prorec.data.inneruomvalue));
                                obj.record.set("baseuomrate", (prorec.data.inneruomvalue));
                                obj.record.set("rate", modifiedRate * prorec.data.inneruomvalue);
                            } else {
                                obj.record.set("baseuomrate", 1);
                                obj.record.set("baseuomquantity", quantity * 1);
                                obj.record.set("rate", modifiedRate);
                            }
                            WtfGlobal.setDefaultWarehouseLocation(obj, rec, false);
                            this.fireEvent('datachanged', this);
                        }, function () {

                        });
                    }
                } else if (this.productOptimizedFlag != undefined && this.productOptimizedFlag != Wtf.Show_all_Products) {
                    productuomid = rec.data.uomid;

                    var selectedUOMRec = WtfGlobal.searchRecord(Wtf.uomStore, obj.value, 'uomid');

                    obj.record.set("uomname", selectedUOMRec.data['uomname']);
                    obj.record.set("isAnotherUOMSelected", false);


                    //To do - Need to take rate from new window
                    //                      this.showPriceWindow.createDelegate(this,[rec, obj],true);
                    var uomschemaid;
                    if (this.UomSchemaType == Wtf.UOMSchema) {//for Schema type
                        if (productuomid != obj.value) {
                            uomschemaid = prorec.data.uomschematypeid;
                        }

                        Wtf.Ajax.requestEx({
                            url: "ACCProductCMN/getIndividualProductPrice.do",
                            params: {
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
                                transactiondate: WtfGlobal.convertToGenericDate(this.billDate)
                            }
                        }, this, function (response) {
                            baseuomrate = response.data[0].baseuomrate;
                            rateperuom = response.data[0].rateperuom;
                            datewiseprice = response.data[0].price;

                            var availableQtyInSelectedUOM = response.data[0].availableQtyInSelectedUOM;
                            var pocountinselecteduom = response.data[0].pocountinselecteduom;
                            var socountinselecteduom = response.data[0].socountinselecteduom;

                            obj.record.set("baseuomrate", baseuomrate);
                            obj.record.set("baseuomquantity", quantity * baseuomrate);
                            obj.record.set("availableQtyInSelectedUOM", availableQtyInSelectedUOM);
                            obj.record.set("isAnotherUOMSelected", true);
                            obj.record.set("pocountinselecteduom", pocountinselecteduom);
                            obj.record.set("socountinselecteduom", socountinselecteduom);
                            var modifiedRate;

                            modifiedRate = WtfGlobal.getIndividualProductPriceInMultiCurrency(rec, datewiseprice);

                            if (productuomid == obj.value) {
                                obj.record.set("baseuomrate", 1);
                                obj.record.set("baseuomquantity", quantity * obj.record.get("baseuomrate"));
                                obj.record.set("rate", modifiedRate * baseuomrate);
                                obj.record.set("rateIncludingGst", modifiedRate * baseuomrate);
                            } else {
                                obj.record.set("rate", modifiedRate * baseuomrate);
                                obj.record.set("rateIncludingGst", modifiedRate * baseuomrate);
                            }
                            if (baseuomrate != 1) {
                                obj.record.set("rate", modifiedRate * quantity * baseuomrate);
                                obj.record.set("rateIncludingGst", modifiedRate * quantity * baseuomrate);
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
                                obj.record.set("baseuomquantity", (quantity) * (prorec.data.caseuomvalue));
                                obj.record.set("baseuomrate", (prorec.data.caseuomvalue));
                                obj.record.set("rate", modifiedRate * prorec.data.caseuomvalue);
                            } else if (obj.value == prorec.data.inneruom) {
                                obj.record.set("baseuomquantity", (quantity) * (prorec.data.inneruomvalue));
                                obj.record.set("baseuomrate", (prorec.data.inneruomvalue));
                                obj.record.set("rate", modifiedRate * prorec.data.inneruomvalue);
                            } else {
                                obj.record.set("baseuomrate", 1);
                                obj.record.set("baseuomquantity", quantity * 1);
                                obj.record.set("rate", modifiedRate);
                            }
                            WtfGlobal.setDefaultWarehouseLocation(obj, rec, false);
                            this.fireEvent('datachanged', this);
                        }, function () {

                        });
                    }

                  }
//                  this.fireEvent('datachanged',this);
            }
            if(obj.field=="productid" || obj.field=="pid" || obj.field=="productname" ){
                var customFieldArr = GlobalColumnModelForProduct[this.moduleid];
                if(customFieldArr !=null && customFieldArr != undefined){
                    for(var k=0;k<customFieldArr.length;k++){
                        rec.set(customFieldArr[k].fieldname,"");
                    }
                }
                if(this.isCustomer)
                    rec.set("changedQuantity",(quantity*(-1))*rec.data.baseuomrate);
                else
                    rec.set("changedQuantity",(quantity)*rec.data.baseuomrate);
                
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
                        forCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency:"",
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
                            if(obj.grid.colModel.config[k].dataIndex==key){
                                var store=obj.grid.colModel.config[k].editor.field.store;
                                if(store)
                                    store.clearFilter();
                                 obj.record.set(key,dataObj[key]);
                            }
                        }
                        
                        
                    }
                   
                  var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.value, 'productid');
                  var baseuomRate=1;
                  var baseuomquantity=1;
                  var productname = "";
                  var proddescription = "";
                  var productuomid = undefined;
                  var productsuppliernumber = "";
                  var shelfLocation = "";
                  var prorec = null;
                  var ComboIndex=0;
                  if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Products_on_type_ahead && productComboIndex==-1){
                        productComboIndex=1;
                        ComboIndex=-1;
                  }else if(this.productOptimizedFlag==Wtf.Products_on_Submit){
                        productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.value.trim(), 'pid');
                  }
//                  var acctaxcode = (this.isCustomer)?"salesacctaxcode":"purchaseacctaxcode";
//                  var protaxcode = "";
                  if(productComboIndex >=0){
                      prorec = this.productComboStore.getAt(productComboIndex);
                      if(ComboIndex==-1){
                          prorec=rec;
                      }
                      productname = prorec.data.productname;
                      obj.record.set("productname", productname);
                          proddescription = prorec.data.desc; 
                            obj.record.set("baseuomrate", 1);
                            if(this.UomSchemaType==Wtf.UOMSchema){//for Schema type
                                productuomid = prorec.data.uomid;
                                obj.record.set("baseuomid", prorec.data.uomid);
                                obj.record.set("baseuomname", prorec.data.uomname);
                            }else{//for packeging UOM type
                                productuomid =this.isCustomer? prorec.data.salesuom:prorec.data.purchaseuom;
        //                          productuomname =this.isCustomer? prorec.data.salesuomname:prorec.data.purchaseuomname;
                                baseuomRate=this.isCustomer? prorec.data.stocksalesuomvalue:prorec.data.stockpurchaseuomvalue;
                                if(obj.record.get("uomid")!=undefined && productuomid != prorec.data.uomid){
                                        baseuomquantity=quantity*baseuomRate;
                                        obj.record.set("baseuomquantity", baseuomquantity);
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
                            }
                            productsuppliernumber= prorec.data.supplierpartnumber;
                            shelfLocation = prorec.data.shelfLocation;
//                      protaxcode = prorec.data[acctaxcode];
                  }
                    obj.record.set("desc",proddescription);
                    obj.record.set("uomid", productuomid);
                    obj.record.set("supplierpartnumber",productsuppliernumber);
                    obj.record.set("shelfLocation",shelfLocation);
                    obj.record.set("multiuom", prorec.data['multiuom']);
                    obj.record.set("availableQtyInSelectedUOM", prorec.data['quantity']);
                    obj.record.set("uomname", prorec.data['uomname']);
                    obj.record.set("blockLooseSell", prorec.data['blockLooseSell']);
//                    obj.record.set("pocountinselecteduom", pocountinselecteduom);
//                    obj.record.set("socountinselecteduom", socountinselecteduom);
                    obj.record.set("isLocationForProduct", prorec.data['isLocationForProduct']);
                    obj.record.set("isWarehouseForProduct", prorec.data['isWarehouseForProduct']);
                    obj.record.set("isBatchForProduct", prorec.data['isBatchForProduct']);
                    obj.record.set("isSerialForProduct", prorec.data['isSerialForProduct']);
                    obj.record.set("isFromVendorConsign", prorec.data['isFromVendorConsign']);
                    obj.record.set("isRowForProduct", prorec.data['isRowForProduct']);
                    obj.record.set("isRackForProduct", prorec.data['isRackForProduct']);
                    obj.record.set("isBinForProduct", prorec.data['isBinForProduct']);  
                    obj.record.set("location", prorec.data["location"]);
                    obj.record.set("warehouse", prorec.data["warehouse"]);
                    obj.record.set("type",prorec.data["type"]);
//                    obj.record.set("purchaseprice", purchaseprice); 
                    obj.record.set("isAutoAssembly", prorec.data["isAutoAssembly"]); 
                  
                  if(datewiseprice==0){
                        if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.addprice) && !this.isRequisition){//permissions
                            rec.set("productname",productname);
                            Wtf.Msg.confirm(WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pricefortheproduct")+"<b>"+productname+"</b>"+" "+WtfGlobal.getLocaleText("acc.field.isnotsetDoyouwanttosetitnow"),
                                this.showPriceWindow.createDelegate(this,[rec, obj],true), this);
                        }else{
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pricefortheproduct")+"<b>"+productname+"</b>"+" "+WtfGlobal.getLocaleText("acc.field.isnotset")], 2);
                        }    
                    } else {
                        // setting datewise price according to currency exchange rate - 
                        if(!Wtf.account.companyAccountPref.productPriceinMultipleCurrency){ //If product in Multiple currency is not set in account preferences
                            var rate=((rec==undefined||rec.data['currencyrate']==undefined||rec.data['currencyrate']=="")?1:rec.data['currencyrate']);
                            var oldcurrencyrate=((rec==undefined||rec.data['oldcurrencyrate']==undefined||rec.data['oldcurrencyrate']=="")?1:rec.data['oldcurrencyrate']);
                            var modifiedRate;
                            if(rate!=0.0)
                                modifiedRate=getRoundedAmountValue((parseFloat(datewiseprice)*parseFloat(rate))/parseFloat(oldcurrencyrate));
                            else
                                modifiedRate=getRoundedAmountValue((parseFloat(datewiseprice)/parseFloat(oldcurrencyrate)));
                        }else{
                            modifiedRate=datewiseprice;
                        }
                        obj.record.set("rate", modifiedRate);
                        obj.record.set("rateIncludingGst", modifiedRate);
                    }

                    if(!(obj.soflag)){
                        obj.record.set("baseuomquantity",1);
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
                            obj.record.set("prtaxid", taxid);
                        } else {
                            obj.record.set("prtaxid", "");
                        }
                        var taxamount = this.setTaxAmountAfterSelection(obj.record);
                        obj.record.set("taxamount",taxamount);

                        this.fireEvent("productselect", obj.value);
                    }
                    this.fireEvent('datachanged',this);
                }, function(){

                });
            }else if(obj.field=="quantity"){
                rec=obj.record;
                if(this.isCustomer)
                    rec.set("changedQuantity",(rec.data.copyquantity-obj.value)*((rec.data.copybaseuomrate==="")?1:rec.data.copybaseuomrate));
                else
                    rec.set("changedQuantity",(obj.value-rec.data.copyquantity)*((rec.data.copybaseuomrate==="")?1:rec.data.copybaseuomrate));
                rec.set("baseuomquantity", obj.record.get("quantity") * obj.record.get("baseuomrate"));
                if(((rec.data.isNewRecord=="" || rec.data.isNewRecord==undefined) && this.fromOrder&&!(this.editTransaction||this.copyInv))||(this.isEdit && rec.data.linkid !="")) {  
                    if(obj.value > rec.data.copyquantity){
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
                            if(this.isOrder){
                                msg = WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinPOisexceedsfromoriginalquantitymentionedinVQ/SO")
                            } else {
                                if(Wtf.account.companyAccountPref.withinvupdate){
                                    msg = WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinVIisexceedsfromoriginalquantitymentionedselectedPO/GR/VQ")
                                } else {
                                    msg = WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinVIisexceedsfromoriginalquantitymentionedinPO/GR/VQ")                               
                                }
                                
                            }
                            
                        }
                        obj.record.set(obj.field, obj.originalValue);
                         Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.common.alert"),msg,{
                                
                        },this)
                        
                    }else if(obj.value!=rec.data.copyquantity) {
                        var msg = WtfGlobal.getLocaleText("acc.field.ProductenteredInvoicedifferentoriginal")
                        if (this.isCustomer) {
                             if(this.isOrder && !this.isQuotation){
                                msg = WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinSOisdifferentfromoriginal")
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
                            if(this.isOrder){
                                msg = WtfGlobal.getLocaleText("acc.field.ProductinPOdifferentSO/VQcontinue")
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
                        this.store.remove(obj.record);
                    }
                }                  
                var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
                  var productuomid = "";
                var ComboIndex=0;
                if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Products_on_type_ahead && productComboIndex==-1){
                    productComboIndex=1;
                    ComboIndex=-1;
                }
                  if(productComboIndex >=0){
                      prorec = this.productComboStore.getAt(productComboIndex);
                      if(ComboIndex==-1){
                          prorec=rec;
                      }
                      productuomid = prorec.data.uomid;
                      if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                            obj.record.set("baseuomquantity", quantity*obj.record.get("baseuomrate"));
                      } else {
                          obj.record.set("baseuomrate", 1);
                          obj.record.set("baseuomquantity", quantity*obj.record.get("baseuomrate"));
                      }
                  } 
                this.fireEvent('datachanged',this);
            }
            
            if(obj.field=="prtaxid" || obj.field=="rate" || obj.field=="quantity" || obj.field=="discountispercent" || obj.field=="prdiscount"){
                taxamount = this.setTaxAmountAfterSelection(obj.record);
                obj.record.set("taxamount",taxamount);
                this.fireEvent('datachanged',this);
            }
            var rowRateIncludingGstAmountIndex=this.getColumnModel().getIndexById(this.id+"rateIncludingGst");
            if((obj.field=="prtaxid"||obj.field=="quantity")&&!this.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden){
                taxamount = this.setTaxAmountAfterIncludingGst(obj.record,1);
                var amountwithOutGst = this.setTaxAmountAfterIncludingGst(obj.record,2);
                var amountwithGst = this.setTaxAmountAfterIncludingGst(obj.record,3);
                obj.record.set("taxamount",taxamount);
                if(amountwithGst!=0)
                    obj.record.set("rateIncludingGst",amountwithGst);
                if(amountwithOutGst!=0)
                    obj.record.set("rate",amountwithOutGst);
                this.fireEvent('datachanged',this);
            }
            if(obj.field=="rateIncludingGst"){
                taxamount = this.setTaxAmountAfterIncludingGst(obj.record,1);
                var amountwithOutGst = this.setTaxAmountAfterIncludingGst(obj.record,2);
                obj.record.set("taxamount",taxamount);
                if(amountwithOutGst!=0)
                    obj.record.set("rate",amountwithOutGst);
                else
                    obj.record.set("rate",obj.record.data.rateIncludingGst);
                this.fireEvent('datachanged',this);
            }
            
            if(obj.field!=undefined&&obj.field=="partamount"){
                taxamount = this.setTaxAmountAfterSelection(obj.record);
                obj.record.set("taxamount",taxamount);
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
        }
        if(this.store.getCount()>0&&this.store.getAt(this.store.getCount()-1).data['productid'].length<=0)
            return;
        if(!this.isNote && (!this.soLinkFlag)) {
            this.addBlankRow();            
        }
       
    },
    callupdateRowonProductLoad: function(obj) {
         if (obj != undefined || obj != null) {
             this.obj=obj;
             if(this.obj.field=='pid'){
                    this.productComboStore.load();
                    this.productComboStore.on('load',function(){
                    if(this.productComboStore.getCount()<=1){
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
                        var index = WtfGlobal.searchRecordIndex(this.productComboStore,this.obj.value,'pid');
                        if(index != -1){
                            this.updateRow(this.obj);
                        } else{
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.common.productWithSpecifiedId")+" "+this.obj.value+" "+WtfGlobal.getLocaleText("acc.common.productDoesNotExistsOrInDormantState")], 2);              
                            obj.cancel=true;   
                            return;
                        }
                    }
                },this);
             }else{
                 this.updateRow(this.obj);
                 
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
    callSerialNoWindow:function(obj){//if autogenerate flag is true then show serial no
        var index=this.productComboStore.findBy(function(rec){
            if(rec.data.productid==obj.data.productid)
                return true;
            else
                return false;
        })
        if(index!=-1){ 
            var deliveredprodquantity = obj.data.dquantity;
            deliveredprodquantity = (deliveredprodquantity == "NaN" || deliveredprodquantity == undefined || deliveredprodquantity == null)?0:deliveredprodquantity;

            var prorec=this.productComboStore.getAt(index); 
            this.batchDetailswin=new Wtf.account.SerialNoWindow({
                renderTo: document.body,
                title:WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber"),
                productName:prorec.data.productname,
                uomName:prorec.data.uomname,
                //quantity:obj.data.dquantity,
                quantity:(obj.data.baseuomrate)*(obj.data.quantity),
                defaultLocation:prorec.data.location,
                productid:prorec.data.productid,
                isSales:this.isCustomer,
                isDO:this.isCustomer?true:false,
                defaultWarehouse:prorec.data.warehouse,
                batchDetails:obj.data.batchdetails,
                warrantyperiod:prorec.data.warrantyperiod,
                warrantyperiodsal:prorec.data.warrantyperiodsal,  
                width:950,
                height:400,
                resizable : false,
                modal : true,
                billid:obj.data.billid,
                productid:prorec.data.productid,
                isSales:true,
                moduleid:Wtf.Acc_Lease_DO,
                transactionid:4,
                isDO:true,
                defaultWarehouse:prorec.data.warehouse,
                defaultAvailbaleQty:this.AvailableQuantity,
                batchDetails:obj.data.batchdetails,
                warrantyperiod:prorec.data.warrantyperiod,
                warrantyperiodsal:prorec.data.warrantyperiodsal,
                isLocationForProduct: prorec.data.isLocationForProduct,
                isWarehouseForProduct: prorec.data.isWarehouseForProduct,
                isRowForProduct: prorec.data.isRowForProduct,
                isRackForProduct: prorec.data.isRackForProduct,
                isBinForProduct: prorec.data.isBinForProduct,
                isBatchForProduct: prorec.data.isBatchForProduct,
                isSerialForProduct: prorec.data.isSerialForProduct,
                linkflag: obj.data.linkflag,
                isEdit: this.isEdit,
            });
            this.batchDetailswin.on("beforeclose",function(){
                this.batchDetails=this.batchDetailswin.getBatchDetails();
                var isfromSubmit=this.batchDetailswin.isfromSubmit;
                if(isfromSubmit){  //as while clicking on the cancel icon it was adding the reocrd in batchdetail json 
                    obj.set("batchdetails",this.batchDetails);
                }
            },this);
            this.batchDetailswin.show();
        }
    },
    /**
     * Call Asset Details when auto generated DO is true and asset group is present
     */
    callFixedAssetDetailsWindow: function(record, productRec) {

        var quantity = record.get('quantity');
        var islinkedFromLeaseSo = false;
        if (Wtf.getCmp(this.parentCmpID) != undefined && Wtf.getCmp(this.parentCmpID).fromLinkCombo != undefined) {
            if (this.isCustomer && this.isLeaseFixedAsset) {
                if (Wtf.getCmp(this.parentCmpID).fromLinkCombo.getValue() == 0) {
                    islinkedFromLeaseSo = true;
                }
            }
        }

        this.FADetailsGrid = new Wtf.account.FADetails({
            title: 'Asset Details',
            quantity: quantity,
            modal: true,
            isCustomer: true,
            isLeaseFixedAsset: true,
            layout: 'border',
            assetRec: productRec,
            lineRec: record,
            assetDetailsArray: record.get('assetDetails'),
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            fromPO: this.fromPO,
            isGRCreatedByLinkingWithPI: this.isGRCreatedByLinkingWithPI,
            isEdit: this.isEdit,
            isFixedAsset: this.isFixedAsset,
            isFromOrder: false,
            isPILinkedInGR: this.isPILinkedInGR,
            moduleid: Wtf.Acc_Lease_DO,
            readOnly: this.readOnly,
            islinkedFromLeaseSo: islinkedFromLeaseSo,
            width: 950,
            height: 500,
            resizable: false,
            parentGrid: this,
            isFixedAssetGR: this.isCustomer ? false : true,
            tagsFieldset: this.parentObj.tagsFieldset
        });

        this.FADetailsGrid.show();

        this.FADetailsGrid.on('beforeclose', function(panel) {
            if (panel.isFromSaveButton) {
                record.set("assetDetails", panel.assetDetails);
            }
        }, this);
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
    calTaxAmountWithoutExchangeRate:function(rec){
        
        var quantity = rec.data.quantity;
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
        
        var origionalAmount = rec.data.rate*quantity;
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
        var rate=getRoundedAmountValue(rec.data.rate);
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
    setTaxAmountAfterIncludingGst:function(rec,amountFlag) {//amountFlag=1 for taxamount and amountFlag=2 for actual amount with discount actualamount=3 amount with GST
        
        var quantity = rec.data.quantity;
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
//        var rec=obj.record;
        var discount = 0;
        var rateIncludingGst=getRoundedAmountValue(rec.data.rateIncludingGst);
        var quantity=getRoundofValue(rec.data.quantity);
        
        var quantityAndAmount=0;
        quantityAndAmount=rateIncludingGst*quantity;
        var origionalAmount = getRoundedAmountValue(quantityAndAmount) ;
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
        var val=origionalAmount-discount;
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
             amount=getRoundedAmountValue((val-taxamount));
             unitVal=getRoundedAmountValue(val/quantity);
             unitAmount=getRoundedAmountValue(amount/quantity);
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
        }else if(amountFlag==3){
            if(quantity!=0){
                val=(val)/quantity;
            }else{
                val=0;
            }
            return val;
        }
        
    },
    setTaxAmount:function(v,m,rec){
       var taxamount= this.calTaxAmount(rec);
       rec.set("taxamount",taxamount);
//        if(this.isNote||this.readOnly)
             return WtfGlobal.withoutRateCurrencySymbol(taxamount,m,rec);
//        return WtfGlobal.currencyRendererSymbol(taxamount,m,rec);
    },
    setTaxAmountWithotExchangeRate:function(v,m,rec){
        var taxamount= 0;
        if(v)
            taxamount= parseFloat(getRoundedAmountValue(v)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1;
       if(rec.data.prtaxid==null || rec.data.prtaxid == undefined || rec.data.prtaxid == ""){
           taxamount = 0;
       }
       rec.set("taxamount",taxamount);
       return WtfGlobal.withoutRateCurrencySymbol(taxamount,m,rec);
    },
    calAmount:function(v,m,rec){
        
        var rate=getRoundedAmountValue(rec.data.rate);
        var quantity=getRoundofValue(rec.data.quantity);
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
        var origionalAmount = getRoundedAmountValue(rate*quantity) ;
        if(rec.data.partamount != 0){
             var partamount=getRoundedAmountValue(rec.data.partamount);
            origionalAmount = getRoundedAmountValue(origionalAmount * (partamount/100));
        }
        
        var discount = 0;//origionalAmount*rec.data.prdiscount/100
        origionalAmount = this.calAmountWithExchangeRate(origionalAmount, rec);
        
        
//        var stockQuantity = this.getStockQuantity(rec.get('productid'));
//        origionalAmount = stockQuantity*origionalAmount; // calculate amount with respect to purchase and sales uom
        
        if(rec.data.prdiscount > 0) {
             var prdiscount=getRoundedAmountValue(rec.data.prdiscount);
            if(rec.data.discountispercent == 1){
                discount = getRoundedAmountValue((origionalAmount * prdiscount) / 100);
            } else {
                discount = prdiscount;
            }
        }
        
        var val=(origionalAmount)-discount;///rec.data.oldcurrencyrate
        
        rec.set("amountwithouttax",val);
        
//        var taxamount= this.calTaxAmount(rec);
        var taxamount = 0;
        if(rec.data.taxamount){
            taxamount= getRoundedAmountValue(rec.data.taxamount);
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
        
        
        var rate=getRoundedAmountValue(rec.data.rate);
        var quantity=getRoundofValue(rec.data.quantity);
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
        

        var origionalAmount = getRoundedAmountValue(rate*quantity) ;
        if(rec.data.partamount != 0){
            origionalAmount = origionalAmount * (rec.data.partamount/100);
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
        rec.set("amountwithouttax",val);
        var taxamount = 0;
        if(rec.data.taxamount){
            taxamount= getRoundedAmountValue(rec.data.taxamount);
        }
        val=parseFloat(val)+parseFloat(taxamount);

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
            subtotal+=getRoundedAmountValue(total);
        }
        return getRoundedAmountValue(subtotal);
    },
    addBlank:function(){
       //this.setGridDiscValues();
       if(!this.isLinkedFromTransaction){
            this.addBlankRow();
       }
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
                        transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
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
                            
                            var quantity = record.data.quantity;
                            quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;

                            //Case of copy invoice
                            if((this.copyInv&&prorec.data.quantity<(quantity*record.data.baseomrate)&&prorec.data.type!="Service")){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+prorec.data.productname+WtfGlobal.getLocaleText("acc.field.is")+prorec.data.quantity], 2);
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
            if(index>=0){
               var taxrec=this.taxStore.getAt(index);
                taxpercent=taxrec.data.percent;
                rec.set('taxpercent',taxpercent);
            }
           if((Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel && (this.moduleid==Wtf.Acc_Invoice_ModuleId ||this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId))){
               if(isAutoGenerateDO){
                    var storeid="";
                    var locationid="";
                    var productRec=WtfGlobal.searchRecord(this.productComboStore,rec.data.productid,'productid')
                    if(productRec !=null){
                        storeid=productRec.data.warehouse;
                        locationid=productRec.data.location;
                        rec.set('invstore',storeid);
                        rec.set('invlocation',locationid);
                    }
                }
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
      
      if(this.fromPO)
         this.store.each(function(rec){
              var taxamount= rec.get('rowTaxAmount');//this.calTaxAmount(rec);
                if (!WtfGlobal.isIndiaCountryAndGSTApplied()) {
                    rec.set("taxamount", taxamount);// Set tax amount for other than Indian country
                }
             //rec.set("prdiscount",0);
             if(rec.data.rate===""){
                 var result = this.productComboStore.find('productid',rec.data.productid);
                 if(result >= 0){
                    var prorec=this.productComboStore.getAt(result);
                    rec.set("rate",prorec.data.initialprice);			
    		}                 
             }
             /*
              * Re-calculation of GST taxes 
              */
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
                        rec.set("productid",prorec.data.productid);
                        rec.set("baseuomrate",1);
                        rec.set("prdiscount",0);  
                        rec.set("discountispercent",1);    
                        rec.set("prtaxpercent",0);
                        rec.set("prtaxid","");
                        rec.set("partamount",0);  
                        rec.set("isNewRecord",1);  
                        rec.set("linkid","");  
                    }                                 
            },this);
            this.checkMappedProducts(store);
            this.store.each(function(rec){
                this.fireEvent('afteredit', {
                    field : 'productid',
                    value : rec.data.productid,
                    record : rec,
                    soflag : true
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
                if(result >= 0){
                    var prorec=this.productComboStore.getAt(result);
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
    loadPOGridStore:function(recids, flag, VQtoCQ,linkingFlag){        
        this.store.load({
            params:{
                bills:recids,
                mode:43,
                closeflag:true,
                dtype: VQtoCQ?"report" : "trans",
                linkingFlag:linkingFlag
            }
        });
        this.soLinkFlag = flag;                      
    },
     getTaxJsonOfIndia: function (prorec) {
        var obj_CST = eval(prorec.data['LineTermdetails']);
        var termStore = new Array();
        for (var i_CST = 0; i_CST < obj_CST.length; i_CST++) {
            termStore.push(obj_CST[i_CST]);
        }
        return termStore;
    },
    
calculateTermLevelTaxes : function(termStore, rec, index,isNewProduct){
    var quantity = rec.data.dquantity;
    quantity = (quantity == "NaN" || quantity == undefined || quantity == null) ? 0 : quantity;
    quantity = getRoundofValue(quantity);

    var amount = isNaN(rec.data.amount) ? 0.0 : rec.data.amount;
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
            
        var formula = termJson.formulaids!=undefined?termJson.formulaids.split(","):"";
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
                      termJson.taxvalue =  termJson.termamount ;
                }
            }
        // Apply Tax on Asessable Value
        if(termJson.taxtype == 0){ // If Flat
                taxamount= this.TaxCalculation(termJson,rec,termJson.taxvalue,assessablevalue);              
            }else if(termJson.taxtype == 1){ // If percentage
                var taxamountfun = assessablevalue * termJson.taxvalue / 100;
                taxamount= this.TaxCalculation(termJson,rec,taxamountfun,assessablevalue);   
            }
//        else if(termJson.taxtype == 1){ // If percentage
//            var taxamountfun = assessablevalue * termJson.taxvalue / 100;
//            taxamount= this.TaxCalculation(termJson,rec,taxamountfun,assessablevalue);   
//        }
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
    }
});
