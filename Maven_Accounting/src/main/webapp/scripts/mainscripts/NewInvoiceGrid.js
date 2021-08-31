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
Wtf.account.ProductDetailsGridSats=function(config){
    this.parentCmpID=config.parentCmpID;
    this.isCustomer=config.isCustomer;
    this.productComboStore=this.isCustomer?Wtf.productStoreSales:Wtf.productStore;
    this.currencyid=config.currencyid;
    this.productID=null;
    this.soLinkFlag = null;
    this.updaterowtax = true;//used in conjunction with this.soLinkFlag
    this.id=config.id;
    this.isOrder=config.isOrder;
    this.isFromGrORDO=(config.isFromGrORDO != null || config.isFromGrORDO != undefined)?config.isFromGrORDO:false;
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
    this.isEdit=config.isEdit;
    this.noteTemp=config.noteTemp;
    this.fromOrder=config.fromOrder;
    this.affecteduser = "";
    this.forCurrency="";
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
    Wtf.account.ProductDetailsGridSats.superclass.constructor.call(this,config);
    this.addEvents({
        'datachanged':true, //Event fired when grid data is changed. Mostly used to calculate subtotal
        'pricestoreload':true,
        'productselect' : true,//Event fired to load data for collapsible panel store
        'productdeleted' : true,//Event fired to remove data for collapsible panel store
        'customerchangepriceload' : true //Event fired when customer is changed
    });
}
Wtf.extend(Wtf.account.ProductDetailsGridSats,Wtf.grid.EditorGridPanel,{
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
         Wtf.account.ProductDetailsGridSats.superclass.onRender.call(this,config);
         this.isValidEdit = true;
         this.on('render',this.addBlankRow,this);
         this.on('afteredit',this.updateRow,this);
         this.on('validateedit',this.checkRow,this);
         this.on('rowclick',this.handleRowClick,this);
         this.on('beforeedit',function(e){
         
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
            		 e.cancel = true;
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
            		e.cancel = true;
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
                         this.createIntervalWindow(e);
                         return; 
                    }else{
                        custValue=beforeEditRecordNew.data.noofquqntity;
                        var qtyValue=beforeEditRecordNew.data.noofqtyvalue;
                        if(custValue!=""){
                            e.cancel=true;
                            this.createExtraQuantityWindow(e,custValue,qtyValue)
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
            }else if (e.field == "taxamount" && e.record.data.productid !="" && Wtf.account.companyAccountPref.dependentField) {
                if(e.record.data.isparentproduct){
                    return false;    
                }
            }else if (e.field == "prtaxid" && e.record.data.productid !="" && Wtf.account.companyAccountPref.dependentField) {
                if(e.record.data.isparentproduct){
                    return false;    
                }
            }
             
             
         },this);
         if(!this.isNote && !this.readOnly){
	         if(this.record == null || this.record == undefined && this.getColumnModel().getColumnById(this.id+"prtaxid").hidden == undefined && this.getColumnModel().getColumnById(this.id+"taxamount").hidden == undefined){
	        	 
                         this.getColumnModel().setHidden(this.getColumnModel().getIndexById(this.id+"prtaxid"), true);				// 21241   If statement added bcos could not use the event destroy for column model
	        	 this.getColumnModel().setHidden(this.getColumnModel().getIndexById(this.id+"taxamount"), true);							// and also could not call the createColumnModel() method from onRender
	         }
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
	    {name:'showquantity'},
            {name:'parentid'},
	    {name:'parentname'},
            {name:'baseuomquantity',defValue:1.00},
            {name:'uomname'},
            {name:'uomid'},
            {name:'baseuomrate',defValue:1.00},
            {name:'copyquantity',mapping:'quantity'},
            {name:'rate'},
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
            {name:'timeintervalChk'},
            {name:'addshiplentheithqty'},
            {name:'timeinterval'},
            {name:'inouttime'},
            {name:'parentDependentType'},
            {name:'dependentType'},
            {name:'dependentTypeNo'},
            {name:'dependentTypeQty'},
            {name:'hourtimeinterval'},
            {name:'customfield'},
            {name:'issubproduct',defValue:false},
            {name:'isparentproduct',defValue:false},
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
            {name:'copybaseuomrate',mapping:'baseuomrate'}  //for handling inventory updation 
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
    
    createExtraQuantityWindow:function(e,no,value){
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
    createIntervalWindow:function(e){
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
                            var desc=e.record.data.desc;
                            desc =desc.substring(desc.indexOf("Arrival time") ,desc.length); 
                            var startDateOnly=WtfGlobal.onlyDateRendererForGridHeader(this.startDate.getValue());
                            var endDateOnly=WtfGlobal.onlyDateRendererForGridHeader(this.endDate.getValue());
                            desc=desc+"\nArrival Date: "+startDateOnly+" Arrival time: "+inHour+":"+inMinutes+"\nDeparture Date: "+endDateOnly+" Departure time: "+outHour+":"+outMinutes+"\nTotal duration: "+interValtime.toFixed(2)+" Hrs"
                            e.record.set("desc",desc);
                        } 
                        var inDateFormat=inDate.format('Y-m-d');
                        var outDateFormat=outDate.format('Y-m-d');
                        var interValtime=(outDate-inDate)/3600000;
                         if(this.isAddInterval.getValue()){ 
                            interValtime=interValtime*e.record.data.hourtimeinterval;
                            var interValtimeround= Math.round(interValtime); 
                            if(interValtime>interValtimeround){
                                interValtime=interValtimeround+1;
                            }else{
                                interValtime=interValtimeround;
                            }
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
                this.productComboStore.baseParams=currentBaseParams;        
            },this); 
            
        if(this.isCustomer)
        	chkproductSalesload();
        else
        	chkproductload();                                        
        this.productEditor=new Wtf.form.ExtFnComboBox({
            name:'productname',
            store:this.productComboStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
            typeAhead: true,
            isProductCombo: true,
            selectOnFocus:true,
            maxHeight:250,
            listAlign:"bl-tl?",//[ERP-5149] To expand list of combobox always on top. 
            valueField:'productid',
            displayField:'productname',
            extraFields:['pid','productname','type'],
            extraComparisionField:'pid',// type ahead search on acccode as well.
            listWidth:450,
            lastQuery:'',
            extraComparisionField:'pid',// type ahead search on acccode as well.
            //editable:false,
            scope:this,
            hirarchical:true,
           // addNewFn:this.openProductWindow.createDelegate(this),
            forceSelection:true
        });

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
        
        this.productEditor.on("blur",function(e,a,b){
            if(Wtf.account.companyAccountPref.invAccIntegration && !this.isCustomer && !this.isQuotation){                
                e.store=this.productComboStore;
            }    
        },this);
        //if(!this.isRFQ)
            this.productEditor.addNewFn=this.openProductWindow.createDelegate(this);
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
                moduleid :this.moduleid
            }
        });
        if(this.readOnly)
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
        this.transBaseuomrate=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength:10,
            decimalPrecision: Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT
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
            header:WtfGlobal.getLocaleText("acc.invoice.gridProduct"),//"Product",
            width:200,
            dataIndex:this.readOnly?'productname':'productid',
            renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.productEditor),
            editor:(this.isNote||this.readOnly)?"":this.productEditor
        },{
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
             width:250,
             editor:(this.isNote||this.readOnly)?"":this.remark,
             renderer:function(val){
                 var regex = /(<([^>]+)>)/ig;
                val = val.replace(/(<([^>]+)>)/ig,"");
                if(val.length<50)
                    return val;   
                else
                    return val.substring(0,50)+" ...";   
            }
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
	     hidden:true,	
             renderer:this.quantityRenderer,
//             renderer:this.storeRenderer(this.productComboStore,"productid","uomname"),
             editor:(this.isNote||this.readOnly)?"":this.transQuantity
         },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridQty"),//"Quantity",
             dataIndex:"showquantity",
             align:'right',
             width:100,
             renderer:this.quantityRenderer,
             editor:(this.isNote||this.readOnly)?"":this.transQuantity
         },{
             header:WtfGlobal.getLocaleText("acc.invoice.dependentTypeNo"),//"Product",
             width:200,
             hidden:!Wtf.account.companyAccountPref.dependentField && (this.moduleid!=Wtf.Acc_Invoice_ModuleId || this.moduleid!=Wtf.Acc_Vendor_Invoice_ModuleId),
             dataIndex:this.readOnly?'value':'dependentType',
             renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.dependentType),
             editor:(this.isNote||this.readOnly)?"":this.dependentType
         },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridUOM"),//"UOM",
            width:100,
            hidden:true,
            dataIndex:this.readOnly?'uomname':'uomid',
            renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.uomEditor),
            editor:(this.isNote||this.readOnly)?"":this.uomEditor
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridRateToBase"),//Base UOM Rate
             dataIndex:"baseuomrate",
             align:'left',
             hidden:true,
             width:100,
             renderer:this.conversionFactorRenderer(this.productComboStore,"productid","uomname"),
             editor:(this.isNote||this.readOnly)?"":this.transBaseuomrate
         },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridQtyInBase"),//Base UOM Quantity
             dataIndex:"baseuomquantity",
//             hidden:true,
             align:'right',
             hidden:true,
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
            width:150,
            dataIndex:'discountispercent',
            id:this.id+"discountispercent",
            fixed:true,
            hidden:true,
//            hidden:this.isQuotation?false:(this.isRequisition || this.noteTemp),
            renderer:Wtf.comboBoxRenderer(this.rowDiscountTypeCmb),
            editor:(this.isNote||this.readOnly)?"":this.rowDiscountTypeCmb
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridDiscount"),//"Discount",
             dataIndex:"prdiscount",
             id:this.id+"prdiscount",
             align:'right',
             fixed:true,
             width:100,
             hidden:true,
//             hidden:this.isQuotation?false:(this.isRequisition || this.noteTemp),
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
             width:120,
             hidden:!(this.editTransaction||this.readOnly) || this.noteTemp,// || this.isOrder,
             renderer:Wtf.comboBoxRenderer(this.transTax),
             editor:this.readOnly||this.isNote?"":this.transTax  //this.transTax
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridTaxAmount"),//"Tax Amount",
             dataIndex:"taxamount",
             id:this.id+"taxamount",
              fixed:true,
             //align:'right',
             width:150,
             editor:this.transTaxAmount,
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
             renderer: this.serialRenderer.createDelegate(this),
             dataIndex:'serialwindow',
             id:this.id+'serialwindow',
             hidden:!(Wtf.account.companyAccountPref.showprodserial),
             width:40
        });
        if(!this.isNote && !this.readOnly) {
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),//"Action",
                align:'center',
                width:40,
                renderer: this.deleteRenderer.createDelegate(this)
            });
        }
        this.cm=new Wtf.grid.ColumnModel(columnArr);                
    },
      serialRenderer:function(v,m,rec){
        return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.serial.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.serial.desc.title")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
    },
    conversionFactorRenderer:function(store, valueField, displayField) {
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
            idx = store.find(valueField, record.data[valueField]);
            if(idx == -1)
                return value;
            var rec = store.getAt(idx);
            return "1 "+ uomname +" = "+ +value+" "+rec.data[displayField];
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
            var newVal="";
            newVal=val.toString();
            if(newVal.indexOf("X")!=-1)
                return newVal;
            return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        }
    },
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    handledeleteSubProducts:function(grid,rowindex){
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
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.nee.48"), function(btn){
                if(btn!="yes") return;
                var store=grid.getStore();
                var total=store.getCount();
                var record = store.getAt(rowindex);
                this.handledeleteSubProducts(grid,rowindex);
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
                  if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory){ //if company level option is on then only check batch and serial details
                        if(proRecord.data.isBatchForProduct || proRecord.data.isSerialForProduct)
                        {
                            this.callSerialNoWindow(record);
                        }
                    }
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.batchserial.funforInventoryitems")],2);   //Batch and serial no details are not valid.
                    return;
                }

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
                } else if(!prorec.data.multiuom){
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
				  rec.set("showquantity",obj.originalValue);	
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
				  rec.set("showquantity",obj.originalValue);
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
				  rec.set("showquantity",obj.originalValue);
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
				  rec.set("showquantity",obj.originalValue);	
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
				  rec.set("showquantity",obj.originalValue);
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
				  rec.set("showquantity",obj.originalValue);
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
        } else if(isAutoGenerateDO &&this.isCustomer&&(obj.field=="quantity"|| obj.field=="showquantity" || obj.field=="baseuomrate")&&obj.record.data['productid'].length>0&&!this.isQuotation&&!this.isOrder){
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
				  rec.set("showquantity",obj.originalValue);	
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
				  rec.set("showquantity",obj.originalValue);
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
    addSubproducts:function(obj){
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
                this.store.insert(obj.row+this.recCnt+1,newrec);
                this.recCnt++;
                this.fireEvent("productselect", prorec.data.productid);
                this.fireEvent('datachanged',this);
            //                  for(var storeitr1=0;storeitr1<100000;storeitr1++){
            //                      
            //                  }
                  
               
            }
        }
        if(isSubproduct)
        {
            obj.record.set("quantity",0);
            obj.record.set("showquantity",0); 
            obj.record.set("rate",0); 
            obj.record.set("isparentproduct",true); 
        }
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
                                     this.fireEvent('datachanged',this);
                                }
                            } , function(){
                
                                });
                        }  
               
                    }
                
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
                            obj.record.set("baseuomquantity", quantity*obj.value);
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
                            obj.record.set("baseuomquantity", quantity*obj.record.get("baseuomrate"));
                      } else {
                          obj.record.set("baseuomrate", 1);
                          obj.record.set("baseuomquantity", quantity*obj.record.get("baseuomrate"));
                      }
                  }
//                  this.fireEvent('datachanged',this);
            }
            if(obj.field=="productid"){
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
           
                Wtf.Ajax.requestEx({
                    url:"ACCProduct/getIndividualProductPrice.do",
                    params:{
                        productid: obj.value,
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
                  if(productComboIndex >=0){
                      prorec = this.productComboStore.getAt(productComboIndex);
                      productname = prorec.data.productname;
                      proddescription = prorec.data.desc;
		      prodparentid = prorec.data.parentid;
                      prodparentname = prorec.data.parentname;
                      isparentproduct = prorec.data.isparentproduct;
                      addshiplentheithqty = prorec.data.addshiplentheithqty;	
                      productuomid = prorec.data.uomid;
                      productsuppliernumber= prorec.data.supplierpartnumber;
                      shelfLocation = prorec.data.shelfLocation;
//                      protaxcode = prorec.data[acctaxcode];
                  }
                  obj.record.set("desc",proddescription);
                  obj.record.set("uomid", productuomid);
                  obj.record.set("supplierpartnumber",productsuppliernumber);
                  obj.record.set("shelfLocation",shelfLocation);
                  
	            if(WtfGlobal.getModuleId(Wtf.getCmp(this.parentCmpID))==22||(WtfGlobal.getModuleId(Wtf.getCmp(this.parentCmpID))==18 && Wtf.getCmp(this.parentCmpID).fromLinkCombo.getValue()==0)){//FOR CROSS LINKING MODULE (SO IN PO AND VQ IN CQ) SET TAXAMOUNT,DISCOUNT TO ZERO
                        obj.record.set("prtaxid","");
                        obj.record.set("prtaxpercent",0);
                        obj.record.set("rowTaxAmount",0);
                        obj.record.set("prdiscount",0);
                        obj.record.set('discountispercent',1);
                    }	
                    
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
                            }
                            this.dependentTypeStore.clearFilter()
                            obj.record.set("dependentType",depRecId);
                             var priceindex=this.dependentTypeStore.find('dependentType',depRecId);
                            var dependentTypedatewiseprice=0;
                            if(priceindex>=0){
                                var dependentTypedependent=this.dependentTypeStore.getAt(priceindex);
                                if(dependentTypedependent.data.price>0){
                                    dependentTypedatewiseprice=dependentTypedependent.data.price;
                                }
                                obj.record.set("rate", dependentTypedatewiseprice);
                            }
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
                 
                  if(datewiseprice==0 && !this.calculatePercentage){
                       if(!isparentproduct){
                        if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.addprice) && !this.isRequisition){//permissions
                            rec.set("productname",productname);
                            Wtf.Msg.confirm(WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pricefortheproduct")+"<b>"+productname+"</b>"+" "+WtfGlobal.getLocaleText("acc.field.isnotsetDoyouwanttosetitnow"),
                                this.showPriceWindow.createDelegate(this,[rec, obj],true), this);
                        }else{
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pricefortheproduct")+"<b>"+productname+"</b>"+" "+WtfGlobal.getLocaleText("acc.field.isnotset")], 2);
                        }  
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
//			 obj.record.set("showquantity",1);	
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
		   if(Wtf.account.companyAccountPref.dependentField)
                        this.addSubproducts(obj);
                }, function(){

                });
            }else if(obj.field=="quantity"){
                rec=obj.record;
                if(this.isCustomer)
                    rec.set("changedQuantity",(rec.data.copyquantity-obj.value)*((rec.data.copybaseuomrate==="")?1:rec.data.copybaseuomrate));
                else
                    rec.set("changedQuantity",(obj.value-rec.data.copyquantity)*((rec.data.copybaseuomrate==="")?1:rec.data.copybaseuomrate));
                
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
                  if(productComboIndex >=0){
                      prorec = this.productComboStore.getAt(productComboIndex);
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
                        
            if(obj.field=="prtaxid" || obj.field=="rate" || obj.field=="quantity" || obj.field=="showquantity" || obj.field=="dependentType" || obj.field=="discountispercent" || obj.field=="prdiscount"){
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
                modal : true
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
         var interValTime=rec.data.timeinterval
        interValTime=interValTime.replace(" Hrs","");
        if(interValTime=="")
            interValTime=1;
        var rate=getRoundedAmountValue(rec.data.rate);
        var quantity=getRoundofValue(rec.data.quantity);
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
        var origionalAmount = getRoundedAmountValue(rate*quantity*interValTime) ;
        if(rec.data.partamount != 0){
             var partamount=getRoundedAmountValue(rec.data.partamount);
            origionalAmount = getRoundedAmountValue(origionalAmount * (partamount/100));
        }
        if(this.calculatePercentage){
            origionalAmount =  rec.data.rate*(rec.data.quantity/100)*interValTime;
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
                           if(!Wtf.account.companyAccountPref.dependentField) 
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
				record.set("showquantity",0);
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
             rec.set("taxamount",taxamount);
             //rec.set("prdiscount",0);
             if(rec.data.rate===""){
                 var result = this.productComboStore.find('productid',rec.data.productid);
                 if(result >= 0){
                    var prorec=this.productComboStore.getAt(result);
                    rec.set("rate",prorec.data.initialprice);			
    		}                 
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
    loadPOGridStore:function(recids, flag, VQtoCQ,linkingFlag,sopolinkflag){        
        this.store.load({
            params:{
                bills:recids,
                mode:43,
                closeflag:true,
                dtype: VQtoCQ?"report" : "trans",
                linkingFlag:linkingFlag,
                sopolinkflag:sopolinkflag
            }
        });
        this.soLinkFlag = flag;
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
