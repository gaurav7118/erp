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
Wtf.account.ProductDetailsGridOptimized=function(config){
    this.parentCmpID=config.parentCmpID;
    this.isCustomer=config.isCustomer;
    this.productComboStore=this.isCustomer?Wtf.productStoreSalesOptimized:Wtf.productStoreOptimized;
    this.currencyid=config.currencyid;
    this.productID=null;
    this.soLinkFlag = null;
    this.updaterowtax = true;//used in conjunction with this.soLinkFlag
    this.id=config.id;
    this.isOrder=config.isOrder;
    this.record=config.record;
    this.isFromGrORDO=(config.isFromGrORDO != null || config.isFromGrORDO != undefined)?config.isFromGrORDO:false;
    this.billDate=new Date();
    this.dateChange=false;
    this.pronamearr=[];
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
    Wtf.account.ProductDetailsGridOptimized.superclass.constructor.call(this,config);
    this.addEvents({
        'datachanged':true, //Event fired when grid data is changed. Mostly used to calculate subtotal
        'pricestoreload':true,
        'productselect' : true,//Event fired to load data for collapsible panel store
        'productdeleted' : true,//Event fired to remove data for collapsible panel store
        'customerchangepriceload' : true //Event fired when customer is changed
    });
}
Wtf.extend(Wtf.account.ProductDetailsGridOptimized,Wtf.grid.EditorGridPanel,{
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
         Wtf.account.ProductDetailsGridOptimized.superclass.onRender.call(this,config);
         this.isValidEdit = true;
         this.on('render',this.addBlankRow,this);
         this.on('afteredit',this.updateRow,this);
         this.on('validateedit',this.checkRow,this);
         this.on('rowclick',this.handleRowClick,this);
         this.on('beforeedit',function(e){
             if(!this.isValidEdit){ // Fixed Bug[13888]: Overlaping text box on validation alert messages. [on TAB key navigation]
                 e.cancel= true;
                 this.isValidEdit = true;
             }    
             
             var isRateFieldEditable = true;
             
             if((e.field == "rate")&& ((e.record.data.isNewRecord =="" && !this.isEdit ) ||(e.record.data.linkid !="" && this.isEdit))){//isNewRecord for nornal records is "1"
                 if(this.editLinkedTransactionPrice && (((this.fromPO||(this.isEdit && this.fromPO==false)) && !this.isCustomer && (this.soLinkFlag==false||(this.isEdit && this.soLinkFlag==null )) )||((this.isOrder||this.isQuotation) && this.isCustomer && (this.soLinkFlag==false||(this.isEdit && this.soLinkFlag==null)) )||(!this.isOrder && this.isCustomer && (this.soLinkFlag==false||(this.isEdit && this.soLinkFlag==null))))&& !this.isRequisition ){  //|| (this.fromOrder && this.isCustomer)
                   //((this.fromPO||(this.isEdit && this.fromPO==false)) && !this.isCustomer && (this.soLinkFlag==false||(this.isEdit && this.soLinkFlag==null )) )for  vendor  
                   //((this.isOrder||this.isQuotation) && this.isCustomer && (this.soLinkFlag==false||(this.isEdit && this.soLinkFlag==null)) ) for Customer
                   e.cancel = true;
                   isRateFieldEditable = false;
                 }
             }
             else if(e.field == "desc" && Wtf.account.companyAccountPref.ishtmlproddesc){
                 e.cancel=true;
                if(e.record.data.productid!="")
                    this.getPostTextEditor(e);
                   return; 
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
            {name:'quantity',defValue:1.00},
            {name:'copyquantity',mapping:'quantity'},
            {name:'rate',defValue:0},
            {name:'rateinbase'},
            {name:'partamount',defValue:0},
            {name:'discamount'},
            {name:'discount'},
            {name:'discountispercent',defValue:1},
            {name:'prdiscount',defValue:0},
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
            {name:'uomname'},
            {name:'linktype'},
            {name:'savedrowid'},
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
            {name:'type'}
            
            
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
//        chkproductload();
            this.productComboStore.on('beforeload',function(s,o){
                if(!o.params)o.params={};
                var currentBaseParams = this.productComboStore.baseParams;
                currentBaseParams.getSOPOflag=true;
                currentBaseParams.onlyProduct=this.isOrder?false:true
                currentBaseParams.startdate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true));
                currentBaseParams.enddate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false));                  
                this.productComboStore.baseParams=currentBaseParams;        
            },this); 
                               
        this.productEditor=new Wtf.form.ExtFnComboBox({
            name:'productname',
            store:this.productComboStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
            typeAhead: true,
            selectOnFocus:true,
            isProductCombo: true,
            maxHeight:250,
            listAlign:"bl-tl?",//[ERP-5149] To expand list of combobox always on top. 
            valueField:'productid',//productid
            displayField:'productname',
            extraFields:['pid','productname','type'],
            listWidth:400,
            mode:'remote',
            extraComparisionField:'pid',// type ahead search on pid as well.
            //editable:false,
            hideTrigger:true,
            scope:this,
            triggerAction : 'all',
            editable : true,
            minChars : 2,
            hirarchical:true,
//            listeners : {
//                'beforeselect' : function(combo, record, index){
//                     if(combo.searchStoreCombo) {
//                        if(this.EditorGrid.getSelectionModel().getSelectedCells().length!=0){
//                            combo.originalOldId = this.EditorGrid.getStore().getAt(this.EditorGrid.getSelectionModel().getSelectedCells()[0][0]).data[combo.comboFieldDataIndex+'id'];
//                         }
//                     }  
//                        else
//                                combo.originalOldId = record.data.productid;                                              
//               }
//            },
           // addNewFn:this.openProductWindow.createDelegate(this),
            forceSelection:true
        });
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
        if((this.isNote || (this.noteTemp!=undefined && ! this.noteTemp))){
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
        if((this.isNote || (this.noteTemp!=undefined || !this.noteTemp))){
            this.accountStore.load();
        }
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
            defaultValue:0
        });
        this.transTaxAmount=new Wtf.form.NumberField({
            allowBlank: true,
            allowNegative: false,
            defaultValue:0
        });
        
        this.partAmount=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            defaultValue:0
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
            maxLength:10
        });
        this.editprice=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
//            minValue:0.01,
            maxLength:14
        });
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
            header:WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),//"Product",
            width:200,
            dataIndex:'productname',//dataIndex:this.readOnly?'productname':'productid',
            renderer:this.readOnly?"":this.getComboNameRenderer(this.productEditor),
            editor:(this.isNote||this.readOnly)?"":this.productEditor
        },{
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
             header:WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"),//"Description",
             dataIndex:"desc",
             hidden:this.isNote,
             width:250,
             editor:(this.isNote||this.readOnly)?"":this.remark,
             renderer:function(val){
                 var regex = /(<([^>]+)>)/ig;
                val = val.replace(/(<([^>]+)>)/ig,"");
                return val;   
//                if(val.length<50)
//                    return val;   
//                else
//                    return val.substring(0,50)+" ...";   
            }
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
             header:WtfGlobal.getLocaleText("acc.invoice.gridQty"),//"Quantity",
             dataIndex:"quantity",
             align:'right',
             width:100,
             renderer:this.storeRenderer(this.productComboStore,"productid","uomname"),
             editor:(this.isNote||this.readOnly)?"":this.transQuantity
         },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridRemQty"),//"Remaining Quantity",
             dataIndex:"remainingquantity",
             align:'right',
             hidden:!this.isNote||this.noteTemp,
             width:150,
             renderer:this.storeRenderer(this.productComboStore,"productid","uomname"),
             editor:(this.isNote||this.readOnly)?"":this.transQuantity
        },{
             header:"<b> "+ WtfGlobal.getLocaleText("acc.invoice.gridEnterQty") +" </b>",//"<b>Enter Quantity</b>",
             dataIndex:"remquantity",
             align:'right',
             hidden:!this.isNote||this.noteTemp,
             width:180,
             renderer:this.storeRenderer(this.productComboStore,"productid","uomname"),
             editor:this.readOnly?"":this.transQuantity
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice"),// "Unit Price",
             dataIndex: "rate",
             align:'right',
             width:150,
             renderer:(this.isNote||this.readOnly?WtfGlobal.withoutRateCurrencySymbol:WtfGlobal.withoutRateCurrencySymbol),
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
             renderer:function(v){return'<div class="currency">'+v+'%</div>';},
             editor:this.partAmount
        },{
            header: WtfGlobal.getLocaleText("acc.field.DiscountType"),
            width:200,
            dataIndex:'discountispercent',
            hidden:this.isQuotation?false:(this.isRequisition || this.noteTemp ||this.isRFQ),
            renderer:Wtf.comboBoxRenderer(this.rowDiscountTypeCmb),
            editor:(this.isNote||this.readOnly)?"":this.rowDiscountTypeCmb
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridDiscount"),//"Discount",
             dataIndex:"prdiscount",
             align:'right',
             width:150,
             hidden:this.isQuotation?false:(this.isRequisition || this.noteTemp ||this.isRFQ),
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
             hidden: this.isQuotationFromPR ? false : (!(this.isNote||this.readOnly) || this.noteTemp),
             renderer:(this.isNote||this.readOnly?WtfGlobal.withoutRateCurrencySymbol:WtfGlobal.currencyRendererSymbol)
        },{
             header:this.isRequisition ? (this.editTransaction ? WtfGlobal.getLocaleText("acc.field.BudgetedCost") : WtfGlobal.getLocaleText("acc.field.EstimatedCost") ): this.isNote?WtfGlobal.getLocaleText("acc.invoice.gridCurAmt"):WtfGlobal.getLocaleText("acc.invoice.gridAmount"),//"Current Amount ":"Amount",
             dataIndex:"amount",
             hidden:this.readOnly || this.isRFQ,
             align:'right',
             width:200,
             renderer:(this.isNote||this.readOnly?WtfGlobal.withoutRateCurrencySymbol:this.calAmountWithoutExchangeRate.createDelegate(this))

//    },{
//             header:"Amount (Including Tax)",
//             dataIndex:"amountwithtax",
//             align:'right',
//             hidden:!this.isNote,
//             width:200,
//             renderer:WtfGlobal.withoutRateCurrencySymbol

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
            header:WtfGlobal.getLocaleText("acc.invoice.gridNoteType"),//"Note Type",
            width:200,
            dataIndex:'typeid',
            hidden:(!this.isNote ||this.noteTemp),
            renderer:Wtf.comboBoxRenderer(this.typeEditor),
            editor:this.readOnly?"":this.typeEditor
        },
        {
            header:WtfGlobal.getLocaleText("acc.invoice.gridRemark"),//"Remark",
            width:200,
            hidden:(!this.isNote ||this.noteTemp),
            dataIndex:'gridRemark',
            name:'gridRemark',
            editor:(this.readOnly)?"":this.cndnRemark
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
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    handleRowClick:function(grid,rowindex,e){
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
                                productquantity:record.data.quantity    
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
        } else {
            this.fireEvent("productselect", grid.getStore().getAt(rowindex).get("productid"));
        }
    },

    storeRenderer:function(store, valueField, displayField) {
        return function(value, meta, record) { 
            if((value==1 || value=="") && record.data['uomname']==""){
                return parseFloat(getRoundofValue(1)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            }
            return parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+" "+record.data['uomname'];
        }
    },

    checkRow:function(obj){
        var rec=obj.record;
        
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
                            copyquantity = copyquantity + rec.data.copyquantity;                            
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
                                    quantity = quantity + rec.data.quantity;
                                }
                            }     
                        }
                    },this);
                    quantity = quantity + obj.record.data['quantity'];  
                    
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
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                        }
                    }
                //WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.76")+" "+rec.data['productname']], 2);
                //obj.cancel=true;                
                }else if(!Wtf.account.companyAccountPref.withinvupdate&&this.isCustomer&&availableQuantity<obj.record.data['quantity']&&prorec.data.type!='Service'&&!this.isQuotation&&!this.isOrder){
                    this.isValidEdit = false;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+' is '+availableQuantity], 2);
                    obj.cancel=true;
                } //for new cash sales but not in if or else in this else loop and for edit no case satifies
            }else{ //New transaction case... In normal Case Check product quantity is greater than available quantity when selecting product
                //
                if(!Wtf.account.companyAccountPref.withinvupdate&&this.isCustomer&&this.store.find("productid",obj.value)>-1 && prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation&&!this.isOrder){
                    var quantity = 0;                 
                    this.store.each(function(rec){
                        if(rec.data.productid == obj.value){
                            var ind=this.store.indexOf(rec);
                            if(ind!=-1){
                                if(ind!=obj.row){                            
                                    quantity = quantity + rec.data.quantity;
                                }
                            }     
                        }
                    },this);
                    quantity = quantity + obj.record.data['quantity'];
                    if(prorec.data['quantity']<quantity){
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+' is '+prorec.data['quantity']], 2);
//                        obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCS/CIareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+WtfGlobal.getLocaleText("acc.field.is")+prorec.data['quantity']+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIaretheexceedingwishtoproceed")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",obj.originalValue);
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                        }
                    }
                //WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.76")+" "+rec.data['productname']], 2);
                //obj.cancel=true;
                //for Invoice and Cash sales in  with Inventory And Without trading Flow  
                }else if(!Wtf.account.companyAccountPref.withinvupdate&&this.isCustomer&&prorec.data['quantity']<obj.record.data['quantity']&&prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation&&!this.isOrder){
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
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                        }
                }else if(this.isCash&&this.isCustomer&&prorec.data['quantity']<obj.record.data['quantity']&&prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation&&!this.isOrder){
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
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                        }
                }
            }  
        //EDIT case in without trading flow and with inventory
        }else if(!Wtf.account.companyAccountPref.withinvupdate&&this.isCustomer&&obj.field=="quantity"&&obj.record.data['productid'].length>0&&!this.isQuotation&&!this.isOrder){  
            prorec=this.productComboStore.getAt(this.productComboStore.find('productid',obj.record.data.productid));
            if(prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part') {
                var availableQuantity = prorec.data.quantity;
                var quantity = 0;
                if(this.editTransaction){    //In Edit Case Check product quantity is greater than available quantity when selecting quantity                                  
                    var copyquantity = 0;                    
                    this.store.each(function(rec){
                        if(rec.data.productid == prorec.data.productid){
                            copyquantity = copyquantity + rec.data.copyquantity;
                            var ind=this.store.indexOf(rec);
                            if(ind!=-1){
                                if(ind!=obj.row){                            
                                    quantity = quantity + rec.data.quantity;
                                }   
                            }                            
                        }
                    },this);
                    quantity = quantity + obj.value;
                    availableQuantity = availableQuantity + copyquantity;   
                    if(this.editTransaction&&!this.copyInv&&availableQuantity < quantity) {
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' is '+availableQuantity], 2);
//                        obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCS/CIareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' is '+availableQuantity+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",obj.originalValue);
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
                                    quantity = quantity + rec.data.quantity;
                                }
                            }                               
                        }
                    },this);
                    quantity = quantity + obj.value;                    
                    if((!this.editTransaction||this.copyInv) &&  availableQuantity < quantity) {
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableSoyou")+'</center>'], 2);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",obj.originalValue);
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                        }
                        
                        
         //               WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' is '+availableQuantity], 2);
          //              obj.cancel=true;
                    }
                    
                }
            }                    
        } else if(this.isCash &&this.isCustomer&&obj.field=="quantity"&&obj.record.data['productid'].length>0&&!this.isQuotation&&!this.isOrder){
            prorec=this.productComboStore.getAt(this.productComboStore.find('productid',obj.record.data.productid));
            if(prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part') {
                var availableQuantity = prorec.data.quantity;
                var quantity = 0;
                if(this.editTransaction){    //In Edit Case Check product quantity is greater than available quantity when selecting quantity                                  
                    var copyquantity = 0;                    
                    this.store.each(function(rec){
                        if(rec.data.productid == prorec.data.productid){
                            copyquantity = copyquantity + rec.data.copyquantity;
                            var ind=this.store.indexOf(rec);
                            if(ind!=-1){
                                if(ind!=obj.row){                            
                                    quantity = quantity + rec.data.quantity;
                                }   
                            }                            
                        }
                    },this);
                    quantity = quantity + obj.value;
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
                                  rec.set("quantity",obj.originalValue);
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
                                    quantity = quantity + rec.data.quantity;
                                }
                            }                               
                        }
                    },this);
                    quantity = quantity + obj.value;                    
                    if((!this.editTransaction||this.copyInv) &&  availableQuantity < quantity) {
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCSareexceedingthequantityavailableSoyoucannotproceed")+'</center>'], 2);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCSareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn){
                               if(btn=="yes"){
                                   obj.cancel=false;
                               }else{
                                  rec.set("quantity",obj.originalValue);
                                  obj.cancel=true;
                                  return false;
                               }
                            },this); 
                        }
                        
                    }
                    
                }
            }                    
        }
        /*Code on product select starts*/
//        if(obj.field=="productid"){
//            
//            var index=this.productComboStore.findBy(function(rec){
//                if(rec.data.productid==obj.value)
//                    return true;
//                else
//                    return false;
//            })
//            var prorec=this.productComboStore.getAt(index);                        
//            index=this.priceStore.find('productid',obj.value)
//            rec=this.priceStore.getAt(index);
//            if(this.store.find("productid",obj.value)>=0){
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.76")+" "+prorec.data['productname']], 2);
  //              obj.cancel=true;
            /*}else if((this.isCustomer&&rec.data["salespricedatewise"]==0)||(!this.isCustomer&&rec.data["purchasepricedatewise"]==0)){
                 if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.addprice)){//permissions
                    Wtf.Msg.confirm(WtfGlobal.getLocaleText("acc.common.alert"),"Price for "+rec.data['productname']+" is not set. Do you want to set now?",
                    this.showPriceWindow.createDelegate(this,[rec, obj],true));
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Price for "+rec.data['productname']+" is not set"], 2);
                }
               obj.cancel=true;
            }else if(!Wtf.account.companyAccountPref.withinvupdate&&this.isCustomer&&prorec.data['quantity']<obj.record.data['quantity']&&prorec.data.type!='Service'&&!this.isQuotation&&!this.isOrder){
                this.isValidEdit = false;
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+' is '+prorec.data['quantity']], 2);
                obj.cancel=true;
            }*/
   //     }else if(!Wtf.account.companyAccountPref.withinvupdate&&this.isCustomer&&obj.field=="quantity"&&obj.record.data['productid'].length>0&&!this.isQuotation&&!this.isOrder){
            //prorec=this.productComboStore.getAt(this.productComboStore.find('productid',obj.record.data.productid));
            //if(((!this.editTransaction||this.copyInv) && prorec.data.quantity<obj.value && prorec.data.type!='Service')
            //|| ( this.editTransaction && !this.copyInv && obj.value>obj.record.data.copyquantity &&  (obj.value-obj.record.data.copyquantity)>prorec.data.quantity &&  prorec.data.type!='Service' ) ){
             //  alert(this.editTransaction+"a----"+this.copyInv+"b----"+obj.value+"c----"+obj.record.data.copyquantity+"d----"+prorec.data.quantity+"e----"+obj.value)
             //   this.isValidEdit = false;
               // var quantity=prorec.data.quantity;
//                if((this.editTransaction&&!this.copyInv&&obj.value>obj.record.data.copyquantity&&(obj.value-obj.record.data.copyquantity)>prorec.data.quantity&&prorec.data.type!='Service')){
//                    quantity=prorec.data.quantity+obj.record.data.copyquantity
//                }
//               WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' is '+quantity], 2);
//                obj.cancel=true;
//            }
//        } 
//        
        /*---------Code on product select ends----------*/
        if(this.isNote){         
            if(obj.field=="typeid"&&(obj.value==0||obj.value==1)){
                rec.set('remquantity',0);
                rec.set('discamount',0);
            }

            if(obj.field=="remquantity"){
                if(rec.data['typeid']==0||rec.data['typeid']==1){
                    obj.cancel=true;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.ChangetheNoteTypefirst") ], 2);
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
                if(rec.data['typeid']==0||rec.data['typeid']==1){
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
            
            if(obj.field=="productname"){
                rec.set("changedQuantity",rec.data.quantity);
                Wtf.Ajax.requestEx({
                    url:"ACCProduct/getIndividualProductPrice.do",
                    params:{
                        productid: obj.value,
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
                            if(obj.grid.colModel.config[k].dataIndex==key){
                                var store=obj.grid.colModel.config[k].editor.field.store;
                                if(store)
                                    store.clearFilter();
                                 obj.record.set(key,dataObj[key]);
                            }
                        }
                        
                        
                    }
                  var productComboIndex = this.productComboStore.find('productid',obj.value); 
                  var productname = "";
                  var proddescription = "";
                  var prodUOM = "";
                  var prorec = null;
                  var acctaxcode = (this.isCustomer)?"salesacctaxcode":"purchaseacctaxcode";
                  var protaxcode = "";
                  if(productComboIndex >=0){
                      prorec = this.productComboStore.getAt(productComboIndex);
                      productname = prorec.data.productname;
                      prodUOM = prorec.data.uomname;
                      proddescription = prorec.data.desc;
                      protaxcode = prorec.data[acctaxcode];
                  }
                  obj.record.set("desc",proddescription);
                  obj.record.set("uomname",prodUOM);
                  
                  if(datewiseprice==0){
                        if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.addprice) && !this.isRequisition){//permissions
                            rec.set("productname",productname);
                            Wtf.Msg.confirm(WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pricefortheproduct")+" <b>"+productname+"</b>"+' '+WtfGlobal.getLocaleText("acc.field.isnotsetDoyouwanttosetitnow"),
                                this.showPriceWindow.createDelegate(this,[rec, obj],true), this);
                        }else{
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pricefortheproduct")+"<b>"+productname+"</b>"+' '+WtfGlobal.getLocaleText("acc.field.isnotset")], 2);
                        }    
                    } else {
                        // setting datewise price according to currency exchange rate - 
                        
                        var rate=((rec==undefined||rec.data['currencyrate']==undefined||rec.data['currencyrate']=="")?1:rec.data['currencyrate']);
                        var oldcurrencyrate=((rec==undefined||rec.data['oldcurrencyrate']==undefined||rec.data['oldcurrencyrate']=="")?1:rec.data['oldcurrencyrate']);
                        var modifiedRate;
                        if(rate!=0.0)
                            modifiedRate=(parseFloat(datewiseprice)*parseFloat(rate))/parseFloat(oldcurrencyrate);
                        else
                            modifiedRate=(parseFloat(datewiseprice)/parseFloat(oldcurrencyrate));
                        
                        
                        obj.record.set("rate", modifiedRate);
                    }

                    if(!(obj.soflag)){
                        obj.record.set("quantity",1);
                        if(this.parentObj && this.parentObj.includeProTax && this.parentObj.includeProTax.getValue() == true) {
                            obj.record.set("prtaxid", protaxcode);
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
                
                /*if(index>=0){
                    rec=this.priceStore.getAt(index);
                    if(!this.soLinkFlag){
                    obj.record.set("desc",rec.data["desc"]);
                    if(this.parentObj && this.parentObj.includeProTax && this.parentObj.includeProTax.getValue() == true) {
                        var productComboIndex = this.productComboStore.find('productid',obj.value);
                        if(productComboIndex >=0) {
                            var acctaxcode = (this.isCustomer)?"salesacctaxcode":"purchaseacctaxcode";
                            obj.record.set("prtaxid", this.productComboStore.getAt(productComboIndex).data[acctaxcode]);
                        }
                    } else {
                        obj.record.set("prtaxid", "");
                    }
                    var taxamount = this.setTaxAmountAfterSelection(obj.record);
                    obj.record.set("taxamount",taxamount);

                        obj.record.set("quantity",1);
                        this.fireEvent("productselect", obj.value);
                    }
                    if(this.isCustomer)
                        obj.record.set("rate",rec.data["salespricedatewise"]);
                    else
                        obj.record.set("rate",rec.data["purchasepricedatewise"]);
                }*/
                
            }else if(obj.field=="quantity"){
                rec=obj.record;
                if(this.isCustomer)
                    rec.set("changedQuantity",(rec.data.copyquantity-obj.value));
                else
                    rec.set("changedQuantity",(obj.value-rec.data.copyquantity));
                
                if(((rec.data.isNewRecord=="" || rec.data.isNewRecord==undefined) && this.fromOrder&&!(this.editTransaction||this.copyInv))||(this.isEdit && rec.data.linkid !="")) {  
                    if(obj.value > rec.data.copyquantity){
                        var msg = WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinCIisexceedsfromoriginal")
                        if (this.isCustomer) {
                            if(this.isOrder && !this.isQuotation){
                                msg = WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinSOisexceedsfromoriginal")
                            } else if(this.isQuotation) {
                                msg =WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinCQisexceedsfromoriginal")
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
                         msg = WtfGlobal.getLocaleText("acc.field.ProductenteredInvoicedifferentoriginal")
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
                                if(btn!="yes") {obj.record.set(obj.field, obj.originalValue)}
                        },this)
                    }
                }
                if((obj.record.data["quantity"])==0){
                    if(obj.record.data.copyquantity!=undefined){                    
                     var deletedData=[];
                     var newRec=new this.deleteRec({
                                productid:obj.record.data.productid,
                                productname:obj.record.data.productname,    
                                productquantity:obj.record.data.copyquantity    
                            });                            
                            deletedData.push(newRec);
                            this.deleteStore.add(deletedData);                            
                }
                    this.store.remove(obj.record);
                }
                this.fireEvent('datachanged',this);
            }
            
            if(obj.field=="prtaxid" || obj.field=="rate" || obj.field=="quantity" || obj.field=="discountispercent" || obj.field=="prdiscount"){
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
        if(!this.isNote){
            if(obj != null || obj != undefined){
//                if(obj.field=="rate")
//                    if(obj.value == 0){
//                        //	         			Wtf.MessageBox.confirm(["Alert","Unit Price should be greater than Zero"],function(btn){
//                        //	                         obj.record.set(obj.field, obj.originalValue);
//                        //	                     },this);
//                        WtfComMsgBox(["Alert",'Unit Price should be greater than Zero' ], 2);
//                        obj.record.set(obj.field, obj.originalValue);
//                    }
            }
        }

        //         if(obj.field=="permit")
        //        	 obj.record.set("permit",rec.data["permit"]);
        
        if(this.store.getCount()>0&&this.store.getAt(this.store.getCount()-1).data['productid'].length<=0)
            return;
        if(!this.isNote && (!this.soLinkFlag)) {
            this.addBlankRow();            
        }
       
    },
    getComboNameRenderer : function(combo){
        return function(value,metadata,record,row,col,store) {
            var idx = WtfGlobal.searchRecordIndex(combo.store,value,combo.valueField);
            var fieldIndex = "productname";
            if(idx == -1) {
                if(record.data["productname"] && record.data[fieldIndex].length>0) {
                    return record.data[fieldIndex];
                }
                else
                    return "";
            }
            var rec = combo.store.getAt(idx);
            var displayField = rec.get(combo.displayField);
            record.set("productid", value);
            record.set("productname", displayField);
            return displayField;
        }
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
//        var rec=obj.record;
        var discount = 0;
        var origionalAmount = rec.data.rate*rec.data.quantity ;
        if(rec.data.partamount != 0){
            origionalAmount = origionalAmount * (rec.data.partamount/100);
        }
        
//        origionalAmount = this.calAmountWithExchangeRate(origionalAmount, rec);
        if(rec.data.prdiscount > 0) {
            if(rec.data.discountispercent == 1){
                discount = origionalAmount * rec.data.prdiscount/ 100;
            } else {
                discount = rec.data.prdiscount;
            }
        }
        //var discount=rec.data.rate*rec.data.quantity*rec.data.prdiscount/100
        var val=origionalAmount-discount;
        var taxpercent=0;
        var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
        if(index>=0){
            var taxrec=this.taxStore.getAt(index);
            taxpercent=taxrec.data.percent;
        }
        var taxamount= (val*taxpercent/100);
        return taxamount;
        
    },
    setTaxAmount:function(v,m,rec){
       var taxamount= this.calTaxAmount(rec);
       rec.set("taxamount",taxamount);
//        if(this.isNote||this.readOnly)
             return WtfGlobal.withoutRateCurrencySymbol(taxamount,m,rec);
//        return WtfGlobal.currencyRendererSymbol(taxamount,m,rec);
    },
    setTaxAmountWithotExchangeRate:function(v,m,rec){
       var taxamount= v;//this.calTaxAmountWithoutExchangeRate(rec);
       if(rec.data.prtaxid==null || rec.data.prtaxid == undefined || rec.data.prtaxid == ""){
           taxamount = 0;
       }
       rec.set("taxamount",taxamount);
//        if(this.isNote||this.readOnly)
             return WtfGlobal.withoutRateCurrencySymbol(taxamount,m,rec);
//        return WtfGlobal.currencyRendererSymbol(taxamount,m,rec);
    },
    calAmount:function(v,m,rec){
        var origionalAmount = rec.data.rate*rec.data.quantity;
        if(rec.data.partamount != 0){
            origionalAmount = origionalAmount * (rec.data.partamount/100);
        }
        
        var discount = 0;//origionalAmount*rec.data.prdiscount/100
        origionalAmount = this.calAmountWithExchangeRate(origionalAmount, rec);
        
        
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
        var origionalAmount = rec.data.rate*rec.data.quantity;
        if(rec.data.partamount != 0){
            origionalAmount = origionalAmount * (rec.data.partamount/100);
        }
        
        var discount = 0;//origionalAmount*rec.data.prdiscount/100
//        origionalAmount = this.calAmountWithExchangeRate(origionalAmount, rec);
        
        
        if(rec.data.prdiscount > 0) {
            if(rec.data.discountispercent == 1){
                discount = (origionalAmount * rec.data.prdiscount) / 100;
            } else {
                discount = rec.data.prdiscount;
            }
        }
        
        var val=(origionalAmount)-discount;///rec.data.oldcurrencyrate
        
        rec.set("amountwithouttax",val);
        
//        var taxamount= this.calTaxAmountWithoutExchangeRate(rec);
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
        var count=this.store.getCount();
        for(var i=0;i<count;i++){
            var total=this.setCurrencyAmount(this.store.getAt(i).data['amount']);
//            if(this.editTransaction&&!this.fromPO){
//                    total=total/this.store.getAt(i).data['oldcurrencyrate'];
//                }
            subtotal+=total;
        }
        return subtotal;
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
                            record.set("rate",datewisepriceResp);
                           
                            //Set old currency rate
                            if(this.editTransaction){
                                record.set('oldcurrencyrate',record.get('currencyrate'));
                            }

                            //Case of copy invoice
                           
                            if((this.copyInv&&prorec.data.quantity<record.data.quantity&&prorec.data.type!='Service')){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+WtfGlobal.getLocaleText("acc.field.is")+' '+prorec.data.quantity], 2);
                                record.set("quantity",0);

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
       
       /*
        this.store.each(function(record){
            var index=this.priceStore.find('productid',record.data.productid);
                if(index>=0){
                var rec=this.priceStore.getAt(index);
                    if(this.isCustomer){
                       rate=(record.data.rowid!=null?WtfGlobal.currencyRendererWithoutSymbol(rec.get('salespricedatewise'),null,record):rec.get('salespricedatewise'))
                   record.set("rate",rate);
                   if(rate==0)
                        this.pronamearr.push(rec.get('productname'))
                    }else{
                        rate=(record.data.rowid!=null?WtfGlobal.currencyRendererWithoutSymbol(rec.get('purchasepricedatewise'),null,record):rec.get('purchasepricedatewise'))
                    record.set("rate",rate);
                    if(rate==0)
                        this.pronamearr.push(rec.get('productname'))
                    record.set("permit",record.data.permit);
                }
                }
                if(this.editTransaction){
                   record.set('oldcurrencyrate',record.get('currencyrate'));
                }
                index=this.productComboStore.find('productid',record.data.productid);
                if(index>=0){
                    var prorec=this.productComboStore.getAt(index);
                    if((this.copyInv&&prorec.data.quantity<record.data.quantity&&prorec.data.producttype!=Wtf.producttype.service)){
                        WtfComMsgBox(["Alert",'Maximum available quantity for '+prorec.data.productname+' is '+prorec.data.quantity], 2);
                        record.set("quantity",0);
                    }
                }
            },this);
            if( this.dateChange){//alert(this.pronamearr.length);//>0&&
                 this.fireEvent('pricestoreload', this.pronamearr,this);}*/
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
            },this);
        } */
        
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

    getCMProductDetails:function(){
        var arr=[];
        var selModel=  this.getSelectionModel();
        var len=this.selModel.grid.store.getCount();
        for(var i=0;i<len;i++){
            if(selModel.isSelected(i)){
            var rec =selModel.getSelected()
            if(rec.data.typeid==2||rec.data.typeid==3)
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
                        quantity = quantity + rec.data.quantity;                                                     
                  }
                },this);
                quantity = quantity + record.data.quantity;
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
        
//    	var msgBox = 0;
//    	this.productComboStore.each(function(rec){
//    		var result = this.store.find('productid',rec.data.productid);
//
//    		if(result >= 0){
//    			var prorec=this.store.getAt(result);
//    			if(!Wtf.account.companyAccountPref.withinvupdate&&rec.data.type!='Service' && rec.data.quantity < prorec.data.quantity){
//    				this.store.remove(this.store.getAt(result));
//    				msgBox = 1;
//    			}
//    		}
//    	},this);
         
    	if(!Wtf.account.companyAccountPref.withinvupdate&&msgBox==1){
//                    WtfComMsgBox(["Alert",'Available Qty for '+msg+' is below than the Qty mentioned in SO/CQ, so Please Edit the Qty given in SO/CQ first and then proceed.'], 2);
//                    Wtf.getCmp(this.parentCmpID).loadStore();
    		//Wtf.getCmp('linkToOrder2Invoice').setValue(false);
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
    }
});
