

Wtf.account.WorkOrderProductDetailsGrid=function(config){
    this.isCustomer=config.isCustomer;
    this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
    if(this.productOptimizedFlag!= undefined || this.productOptimizedFlag==Wtf.Show_all_Products){
        this.productComboStore=this.isCustomer?Wtf.productStoreSales:Wtf.productStore;
    }else if(this.productOptimizedFlag==Wtf.Products_on_type_ahead || this.productOptimizedFlag==Wtf.Products_on_Submit){
        this.productComboStore=this.isCustomer?Wtf.productStoreSalesOptimized:Wtf.productStoreOptimized;
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
//    this.copyInv=config.copyInv;
    this.copyTrans=config.copyTrans;
    this.editTransaction=config.editTransaction;
    this.heplmodeid = config.heplmodeid;
    this.parentid=config.parentid;
    this.noteTemp=config.noteTemp;
    this.editLinkedTransactionQuantity= Wtf.account.companyAccountPref.editLinkedTransactionQuantity;
    this.editLinkedTransactionPrice= Wtf.account.companyAccountPref.editLinkedTransactionPrice;
    this.isEdit=config.isEdit;
    this.forCurrency="";
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
    this.loadPriceStore();
    var colModelArray = [];
    colModelArray = GlobalColumnModel[this.moduleid];
    if(colModelArray) {
        colModelArray.concat(GlobalColumnModelForProduct[this.moduleid]);
    }
    WtfGlobal.updateStoreConfig(colModelArray, this.store);
    Wtf.account.WorkOrderProductDetailsGrid.superclass.constructor.call(this,config);
    this.addEvents({
        'datachanged':true,
        'pricestoreload':true,
        'productdeleted':true
    });
}
Wtf.extend(Wtf.account.WorkOrderProductDetailsGrid,Wtf.grid.EditorGridPanel,{
    clicksToEdit:1,
    stripeRows :true,
    rate:1,
    symbol:null,
    layout:'fit',
    viewConfig:{forceFit:true},
    forceFit:true,
    loadMask:true,
    onRender:function(config){
         Wtf.account.WorkOrderProductDetailsGrid.superclass.onRender.call(this,config);
         this.isValidEdit = true;
         this.on('render',this.addBlankRow,this);
         if(this.productOptimizedFlag!=Wtf.Products_on_Submit){
             this.on('afteredit',this.updateRow,this);
         } else {
             this.on('afteredit',this.callupdateRowonProductLoad,this);
         }
         this.on('validateedit',this.checkRow,this);
         this.on('rowclick',this.handleRowClick,this);
         this.on('beforeedit',function(e){
             
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
            if(e.field == "rate" && isRateFieldEditable){	// rate editable for product type "Service"
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
             
        //         if(this.isDeferredRevenueRecognition){ // Fixed Bug[13888]: Overlaping text box on validation alert messages. [on TAB key navigation]
        //                 e.cancel= true;                 
        //             }    
             
        },this);       
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
            {name:'supplierpartnumber'}
        ]);

        this.priceStore = new Wtf.data.Store({        
            url:"ACCProduct/getProductsForCombo.do",
            baseParams:{mode:22
                },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.priceRec)
        });
        this.priceStore.on('load',this.setGridProductValues,this);
        
        this.storeRec = Wtf.data.Record.create([
            {name:'rowid',defValue:null},
            {name:'productname',mapping:(this.isViewCNDN)?'productdetail':null},
            {name:'billid'},
            {name:'billno'},
            {name:'bomid'},
            {name:'bomcode'},
            {name:'Cust_billno'},
            {name:'productid'},
            {name:'description'},
            {name:'shelfLocation'},
            {name:'partno'},
            {name:'quantity'},
            {name:'dquantity'},
            {name:'showquantity' ,defValue:0},
            {name:'parentid'},
	   {name:'parentname'},
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
            {name:'rateIncludingGst'},
            {name:'gstCurrencyRate',defValue:'0.0'},
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
            {name:'copybaseuomrate',mapping:'baseuomrate'},//for handling inventory updation
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
            {name: 'changedQuantity'},
            {name:'producttype'},
            {name:'permit'},
            {name:'linkto'},
            {name:'linkid'},
            {name:'linktype'},
            {name:'savedrowid'},
            {name:'docrowid'},
            {name:'batchdetails'},
            {name:'recTermAmount'},
            {name:'lockquantity'},
            {name:'OtherTermNonTaxableAmount'},
            {name:'LineTermdetails'},
            {name:'uncheckedTermdetails'},
            {name: 'ProductTermdetails'},
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
            {name: 'dealerExciseTerms'}
        ]);
        var url=Wtf.req.account+((this.fromOrder||this.readOnly)?((this.isCustomer)?'CustomerManager.jsp':'VendorManager.jsp'):((this.isCN)?'CustomerManager.jsp':'VendorManager.jsp'));
        if(this.fromOrder)
           url=Wtf.req.account+(this.isCustomer?'CustomerManager.jsp':'VendorManager.jsp');
        this.store = new Wtf.data.Store({
            url:url,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.storeRec)
        });
        this.store.on('load',this.loadPOProduct,this);
 //       chkProductPriceload();                    
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

                    }else
                        this.updateRow(this.obj);
                },this);
             }else{
                 this.updateRow(this.obj);
                 
             }
         }
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
                currentBaseParams.searchProductString = (this.productOptimizedFlag==Wtf.Products_on_Submit && !this.isProductLoad)? this.productId.getValue():"";
                currentBaseParams.isFreeTextSearching = (this.productOptimizedFlag==Wtf.Products_on_Submit);
                this.productComboStore.baseParams=currentBaseParams;        
            },this); 
        this.isCustomer ? chkproductSalesload() : chkproductload() ;
        if(this.productOptimizedFlag== undefined || this.productOptimizedFlag==Wtf.Show_all_Products){
            if (this.isCustomer) {
                chkproductSalesload();
            }
            else {
                chkproductload();
            }
            this.productEditor = new Wtf.form.ExtFnComboBox({
                name: 'pid',
                store: this.productComboStore, //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
                typeAhead: true,
                isProductCombo: true,
                selectOnFocus: true,
                maxHeight: 250,
                listAlign: "bl-tl?", //[ERP-5149] To expand list of combobox always on top. 
                valueField: 'productid',
                displayField: 'pid',
                extraFields: ['productname', 'type'],
                listWidth: 450,
                extraComparisionField: 'pid', // type ahead search on acccode as well.
                extraComparisionFieldArray: ['pid', 'productname'],
                lastQuery: '',
                //editable:false,
                scope: this,
                hirarchical: true,
                // addNewFn:this.openProductWindow.createDelegate(this),
                forceSelection: true,
                hideAddButton: true
            });
        } else {
            this.productEditor = new Wtf.form.ExtFnComboBox({
                name: 'pid',
                store: this.productComboStore, //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
                typeAhead: true,
                selectOnFocus: true,
                isProductCombo: true,
                maxHeight: 250,
                listAlign: "bl-tl?", //[ERP-5149] To expand list of combobox always on top. 
                valueField: 'productid', //productid
                displayField: 'pid',
                extraFields: ['productname', 'type'],
                listWidth: 450,
                extraComparisionField: 'pid', // type ahead search on acccode as well.
                mode: 'remote',
                //editable:false,
                hideTrigger: true,
                scope: this,
                triggerAction: 'all',
                editable: true,
                minChars: 2,
                hirarchical: true,
                hideAddButton: true, //Added this Flag to hide AddNew  Button  
                addNewFn: this.openProductWindow.createDelegate(this),
                forceSelection: true,
                hideAddButton:true
            });
        }
        
        this.productEditor.on('beforeselect', function(combo, record, index) {
            return validateSelection(combo, record, index);
        }, this);
        
        /*
         *SDP-4553
         *Set Product Id When user scans barcode for product id field.
         *After scanning barcode by barcode reader press Enter or Tab button.
         *So we are handling specialkey event to set product id to product id combo.
         **/
        this.productEditor.on('specialkey', function(field , e) {
            if(e.keyCode == e.ENTER|| e.keyCode == e.TAB){
                if(field.getRawValue() !="" && field.getValue()==""){
                    var value = field.getRawValue();
                    
                    if(this.productOptimizedFlag== undefined || this.productOptimizedFlag==Wtf.Show_all_Products){
                    /*
                     *This block will execute when Show all product or product as free text is selected.
                     *In this case we will search pid in productComboStore and set value accordingly. 
                     **/
                        var index = WtfGlobal.searchRecordIndex(this.productComboStore,value,'pid');
                        if(index!=-1){
                            var prorec=this.productComboStore.getAt(index); 
                            var dataObj = prorec.data;
                            this.setPIDForBarcode(dataObj,field,false);
                    
                        }
                    }else{
                        
                    /*
                     *This block will execute when Show product on type ahead is selected.
                     *In this case we will fetch data from backend.
                     **/
                        var params = this.productComboStore.baseParams;
                        params.query = field.getRawValue();
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
            }
        },this);
        
        if(this.productOptimizedFlag!=Wtf.Products_on_Submit){
                    
            this.productEditor.on("blur",function(e,a,b){
                if(Wtf.account.companyAccountPref.invAccIntegration && !this.isCustomer && !this.isQuotation){                
                    e.store=this.productComboStore;
                }    
            },this);
           // if(!this.isRFQ)
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

        this.productComboStore.on("load",this.loadPriceAfterProduct,this);
        this.remark= new Wtf.form.TextField({
            name:'remark'
        });        
        
        this.inventoryStores = new Wtf.form.ComboBox({  //location
            triggerAction:'all',
            mode: 'local',
            valueField:'id',
            displayField:'name',
            store:Wtf.inventoryStore,
            anchor:'90%',
            typeAhead: true,
            forceSelection: true,
            name:'location',
            hiddenName:'location'
        });
//     chkinventoryWarehouse();
         Wtf.inventoryStore.load();
        this.inventoryLocation = new Wtf.form.ComboBox({   //warehouse
            triggerAction:'all',
            mode: 'local',
//            fieldLabel:WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),
            valueField:'id',
            displayField:'name',
            store:Wtf.inventoryLocation,
            anchor:'90%',
            typeAhead: true,
            forceSelection: true,
            name:'warehouse',
            hiddenName:'warehouse'
        });
//        chkinventoryLocation();
    Wtf.inventoryLocation.load();

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
        },{                                                                 //added sequence arrows - refer ticket ERP-13781
            header: WtfGlobal.getLocaleText("acc.invoice.lineItemSequence"),//"Sequence",
            width:100,
            align:'center',
            name:'srno',
            renderer: Wtf.applySequenceRenderer
        });
        
        if(this.productOptimizedFlag!=Wtf.Products_on_Submit){
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.product.gridProductID"),//"Product ID",
                width:200,
                dataIndex:(this.readOnly ||this.productOptimizedFlag==Wtf.Products_on_type_ahead)? 'pid' : 'productid',
                renderer:(this.productOptimizedFlag==Wtf.Products_on_type_ahead)?(this.readOnly?"":this.getComboNameRenderer(this.productEditor)):(this.readOnly?"":Wtf.comboBoxRenderer(this.productEditor)),
                editor:(this.isNote||this.readOnly||this.isViewTemplate)?"":this.productEditor
                
            });
        }else{
             columnArr.push({
                header: WtfGlobal.getLocaleText("acc.product.gridProductID"),//"Product ID",
                dataIndex: 'pid',
                editor:(this.isViewTemplate||this.isNote||this.readOnly||this.productOptimizedFlag!=Wtf.Products_on_Submit)?"":this.productId,
                width:200
               
            });
        }
        
        columnArr.push(
         {
             header:WtfGlobal.getLocaleText("acc.invoice.gridDescription"),//"Description",
             dataIndex:"description",
             hidden:this.isNote,
             width:250,
             editor:(this.isNote||this.readOnly)?"":this.remark,
             renderer:function(val){
                val = val.replace(/(<([^>]+)>)/ig,"");
                if(val.length<50)
                    return val;   
                else
                    return val.substring(0,50)+" ...";   
            }
         });
         columnArr.push(
         {
             header:WtfGlobal.getLocaleText("acc.field.ActualQuantity"),
             dataIndex:"quantity",             
             align:'right',
             hidden:true,
             width:200,
             editor:this.actQuantity,
             renderer:this.quantityRenderer
//             renderer:this.storeRenderer(this.productComboStore,"productid","uomname")
        },{
             header:WtfGlobal.getLocaleText("acc.procduct.trans.header.5"),//this.isCustomer ? WtfGlobal.getLocaleText("acc.accPref.deliQuant") : WtfGlobal.getLocaleText("acc.field.ReceivedQuantity"),
             dataIndex:"dquantity",
             align:'right',
             width:100,
             editor:this.deliQuantity,
             renderer:this.quantityRenderer
//             renderer:this.storeRenderer(this.productComboStore,"productid","uomname")
         },{
             header: '',
             align:'center',
             renderer: this.serialRenderer.createDelegate(this),
             hidden:!(Wtf.account.companyAccountPref.showprodserial),
             width:40
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
             align:'right',
//             hidden:true,
             width:50,
             renderer:this.storeRenderer(this.productComboStore,"productid","uomname")
//             editor:(this.isNote||this.readOnly)?"":this.transQuantity
         },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice"), // "Unit Price",
            dataIndex: "rate",
            align:'right',
            width:150,
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            editor:(this.isNote||this.readOnly) ? "" : this.editprice,
            editable:true
//            hidden: !Wtf.account.companyAccountPref.unitPriceConfiguration
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridAmount"), // "Amount",
             dataIndex:"amount",
             align:'right',
             width:200,
             renderer:(this.isNote||this.readOnly?WtfGlobal.withoutRateCurrencySymbol:this.calAmount.createDelegate(this))
//             hidden: !Wtf.account.companyAccountPref.unitPriceConfiguration
        },{
                header:WtfGlobal.getLocaleText("acc.field.Remarks"),  //"Remark",
                dataIndex:"remark",
                editor:this.Description=new Wtf.form.TextArea({
                    maxLength:200,
                    allowBlank: false,
                    xtype:'textarea'
                })
        },{   //added the add serial icon at last of grid
             header: '',
             align:'center',
             renderer: this.serialRenderer.createDelegate(this),
             hidden: !(Wtf.account.companyAccountPref.isBatchCompulsory) && !(Wtf.account.companyAccountPref.isSerialCompulsory),
             width:40
        });
//        if(!this.isNote && !this.readOnly) {
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),//"Action",
                align:'center',
                width:40,
                renderer: this.deleteRenderer.createDelegate(this)
            });
//        }
        this.cm=new Wtf.grid.ColumnModel(columnArr);
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
    quantityRenderer:function(val,m,rec){
        if(val == ""){
            return val;
        }else{
            return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        }
    },
    serialRenderer:function(v,m,rec){
        return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.serial.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.serial.desc.title")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
    },
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
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
                
                var deliverdproqty = record.data.dquantity;
                deliverdproqty = (deliverdproqty == "NaN" || deliverdproqty == undefined || deliverdproqty == null)?0:deliverdproqty;
                
                if(record.data.copyquantity!=undefined){                    
                     var deletedData=[];
                     var newRec=new this.deleteRec({
                                productid:record.data.productid,
                                productname:record.data.productname,    
                                productquantity:deliverdproqty,
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
        } else if(e.getTarget(".serialNo-gridrow")){
             var store=grid.getStore();
            var record = store.getAt(rowindex);
            var productid = record.get('productid');
             var linkflag=false;
            if(Wtf.getCmp('linkToOrder'+this.heplmodeid+this.parentid))
            {
                linkflag=Wtf.getCmp('linkToOrder'+this.heplmodeid+this.parentid).getValue();
            }
           record.data.linkflag=linkflag;
           var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, productid, 'productid');
            if(productComboRecIndex >=0){
                var proRecord = this.productComboStore.getAt(productComboRecIndex);
                if(proRecord.data.type!='Service' && proRecord.data.type!='Non-Inventory Part'){  //serial no for only inventory type of product
                    if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.account.companyAccountPref.isLocationCompulsory || Wtf.account.companyAccountPref.isWarehouseCompulsory  || Wtf.account.companyAccountPref.isRowCompulsory || Wtf.account.companyAccountPref.isRackCompulsory || Wtf.account.companyAccountPref.isBinCompulsory){ //if company level option is on then only check batch and serial details
                        if(proRecord.data.isLocationForProduct || proRecord.data.isWarehouseForProduct || proRecord.data.isBatchForProduct || proRecord.data.isSerialForProduct  || proRecord.data.isRowForProduct || proRecord.data.isRackForProduct  || proRecord.data.isBinForProduct) 
                        {
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
    storeRenderer:function(store, valueField, displayField) {
        return function(value, meta, record) {
            var idx = store.find(valueField, record.data[valueField]);
            if(idx == -1)
                return value;
            var rec = store.getAt(idx);
            return value+" "+rec.data[displayField];
        }
    },
    updateRow:function(obj){
        if(obj!=null){            
            this.productComboStore.clearFilter(); // Issue 22189
            var rec=obj.record;
            var deliveredproqty = obj.record.get("dquantity");
            deliveredproqty = (deliveredproqty == "NaN" || deliveredproqty == undefined || deliveredproqty == null)?0:deliveredproqty;
            var proqty = deliveredproqty;
            if(obj.field=="baseuomrate"){
                if(this.isCustomer)
                    rec.set("changedQuantity",(rec.data.copybaseuomrate-obj.value)*((rec.data.copyquantity==="")?1:rec.data.copyquantity));
                  else
                    rec.set("changedQuantity",(obj.value-rec.data.copybaseuomrate)*((rec.data.copyquantity==="")?1:rec.data.copyquantity));
                
                  var productComboIndex = WtfGlobal.searchRecordIndex(this.priceStore, obj.record.get('productid'), 'productid');
                  var productuomid = "";
                  if(productComboIndex >=0){
                      prorec = this.priceStore.getAt(productComboIndex);
                      productuomid = prorec.data.uomid;
                      if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                            obj.record.set("baseuomquantity", proqty*obj.value);
                      } else {
                          obj.record.set("baseuomrate", 1);
                      }
                  }
            }
            if(obj.field=="uomid"){
                  var prorec = null;
                  var productComboIndex = WtfGlobal.searchRecordIndex(this.priceStore, obj.record.get('productid'), 'productid');
                  var productuomid = "";
                  if(productComboIndex >=0){
                      prorec = this.priceStore.getAt(productComboIndex);
                      productuomid = prorec.data.uomid;
                      if(productuomid != obj.value){
                          //To do - Need to take rate from new window
//                      this.showPriceWindow.createDelegate(this,[rec, obj],true);
                            obj.record.set("baseuomquantity", proqty*obj.record.get("baseuomrate"));
                      } else {
                          obj.record.set("baseuomrate", 1);
                          obj.record.set("baseuomquantity", proqty*obj.record.get("baseuomrate"));
                      }
                  }
            }
            if(obj.field=="productid" || obj.field=="pid" || obj.field=="productname"){
                rec=obj.record;
                var index=this.priceStore.find('productid',obj.value);
               if(this.isCustomer)
                    rec.set("changedQuantity",(proqty*(-1))*rec.data.baseuomrate);
                else
                    rec.set("changedQuantity",(proqty)*rec.data.baseuomrate);
                if(index>=0){
                    rec=this.priceStore.getAt(index);
                    obj.record.set("description",rec.data["desc"]);
                    obj.record.set("supplierpartnumber",rec.data["supplierpartnumber"]);
                    obj.record.set("shelfLocation",rec.data["shelfLocation"]);
                    obj.record.set("prtaxid", "");
                    obj.record.set("taxamount","");
//                    obj.record.set("quantity",1);
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
                        productid: obj.value,
                        affecteduser: this.affecteduser,
                        transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                        forCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency:"",
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
                    var productuomid = undefined;
                    var productsuppliernumber = "";
                    var shelfLocation = "";
                    var prorec = null;
                    var acctaxcode = (this.isCustomer)?"salesacctaxcode":"purchaseacctaxcode";
                    var protaxcode = "";
                    
                    var ComboIndex = 0;
                    if (this.productOptimizedFlag != undefined && this.productOptimizedFlag == Wtf.Products_on_type_ahead && productComboIndex == -1) {
                        productComboIndex = 1;
                        ComboIndex = -1;
                    } else if (this.productOptimizedFlag == Wtf.Products_on_Submit) {
                        productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.value.trim(), 'pid');
                    }
                    if(productComboIndex >=0){
                        prorec = this.productComboStore.getAt(productComboIndex);
                        if (ComboIndex == -1) {
                            prorec = rec;
                        }
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
                  
                     if(datewiseprice==0){
                                if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.addprice) && !this.isRequisition){//permissions
                                    rec.set("productname",productname);
                                    if(Wtf.account.companyAccountPref.unitPriceConfiguration) {
                                Wtf.Msg.confirm(WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pricefortheproduct")+"<b>"+productname+"</b>"+' '+WtfGlobal.getLocaleText("acc.field.isnotsetDoyouwanttosetitnow"),
                                    this.showPriceWindow.createDelegate(this,[rec, obj],true), this);
                            }
                            if(!Wtf.account.companyAccountPref.unitPriceConfiguration){// if Wtf.account.companyAccountPref.unitPriceConfiguration is off and rate for that product is not set then it will be zero by default.
                                obj.record.set("rate", 0);
                            }
                                
                                }else{
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pricefortheproduct")+"<b>"+productname+"</b>"+' '+WtfGlobal.getLocaleText("acc.field.isnotset")], 2);
                                }  
                    } else {
                             // setting datewise price according to currency exchange rate - 
                          if(!Wtf.account.companyAccountPref.productPriceinMultipleCurrency){ //If product in Multiple currency is not set in account preferences
                                var rate=((obj.record==undefined||obj.record.data['currencyrate']==undefined||obj.record.data['currencyrate']=="")?1:obj.record.data['currencyrate']);
                                var oldcurrencyrate=((obj.record==undefined||obj.record.data['oldcurrencyrate']==undefined||obj.record.data['oldcurrencyrate']=="")?1:obj.record.data['oldcurrencyrate']);
                                var modifiedRate;
                                if(rate!=0.0)
                                    modifiedRate=(parseFloat(datewiseprice)*parseFloat(rate))/parseFloat(oldcurrencyrate);
                                else
                                    modifiedRate=(parseFloat(datewiseprice)/parseFloat(oldcurrencyrate));
                            }else{
                                modifiedRate=datewiseprice;
                            }
                                obj.record.set("rate", modifiedRate);
                        }
                    this.fireEvent('datachanged',this);
            }, function(){
                
            });       
            }else if(obj.field=="quantity"){
                rec=obj.record;               
                  if(((rec.data.isNewRecord=="" || rec.data.isNewRecord==undefined) && this.fromOrder&&!(this.editTransaction||this.copyInv))||(this.isEdit && rec.data.linkid !="")) {  
                   if(((obj.value > rec.data.remainingquantity && rec.data.remainingquantity!="")||(obj.value > rec.data.copyquantity && rec.data.copyquantity !="")) ||(obj.value > rec.data.copyquantity && this.isEdit)){  
                        var msg=this.isCustomer?WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinDOisexceedsfromoriginal"):WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinGRisexceedsfromoriginal");
                        obj.record.set(obj.field, obj.originalValue);
                         Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.common.alert"),msg,{
                                
                        },this)
                   }else if(((obj.value != rec.data.remainingquantity && rec.data.remainingquantity!="")||(obj.value != rec.data.copyquantity && rec.data.copyquantity !="")) ||(obj.value != rec.data.copyquantity && this.isEdit)) {
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"),this.isCustomer?WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinDOisdifferentfromoriginal"):WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinGRisdifferentfromoriginal"),function(btn){
                            if(btn!="yes") {
                                obj.record.set(obj.field, obj.originalValue);
                                obj.record.set("baseuomquantity",obj.originalValue*obj.record.get("baseuomrate"));
                                }else{
                                    obj.record.set("baseuomquantity", obj.value);
                                }
                        },this)
                    }
                }
                if((proqty)==0){
                    this.store.remove(obj.record);
                }
                var productComboIndex = WtfGlobal.searchRecordIndex(this.priceStore, obj.record.get('productid'), 'productid');
                  var productuomid = "";
                  if(productComboIndex >=0){
                      prorec = this.priceStore.getAt(productComboIndex);
                      productuomid = prorec.data.uomid;
                      if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                            obj.record.set("baseuomquantity", proqty*obj.record.get("baseuomrate"));
                      } else {
                          obj.record.set("baseuomrate", 1);
                          obj.record.set("baseuomquantity", proqty*obj.record.get("baseuomrate"));
                      }
                  }
              if(deliveredproqty!=""){
                 if(deliveredproqty > proqty){
                    var msg = this.isCustomer ? WtfGlobal.getLocaleText("acc.field.Deliveredquantityshouldnotbegreaterthanactualquantity") : WtfGlobal.getLocaleText("acc.field.Receiptquantityshouldnotbegreaterthanactualquantity");
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg], 2);
                    obj.record.set("dquantity", proqty);
                } 
              }    
                
                var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
                if (productComboRecIndex >= 0) {
                    var proRecord = this.productComboStore.getAt(productComboRecIndex);
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
                    }
                }
            }else if(obj.field=="dquantity"){
                
                rec=obj.record;
                
                rec.set("quantity",obj.value);
                
                if(this.isCustomer)
                    rec.set("changedQuantity",(rec.data.copyquantity-obj.value)*((rec.data.copybaseuomrate==="")?1:rec.data.copybaseuomrate));
                else
                    rec.set("changedQuantity",(obj.value-rec.data.copyquantity)*((rec.data.copybaseuomrate==="")?1:rec.data.copybaseuomrate));                

                if(deliveredproqty<= 0){
                    var msg = this.isCustomer ? WtfGlobal.getLocaleText("acc.field.qtycannotzero") : WtfGlobal.getLocaleText("acc.field.qtycannotzero");
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg], 2);
                    obj.record.set("dquantity", proqty);
                }
                
                var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
                if (productComboRecIndex >= 0) {
                    var proRecord = this.productComboStore.getAt(productComboRecIndex);
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
                
                var productComboIndex = WtfGlobal.searchRecordIndex(this.priceStore, obj.record.get('productid'), 'productid');
                var productuomid = "";
                if(productComboIndex >=0){
                    prorec = this.priceStore.getAt(productComboIndex);
                    productuomid = prorec.data.uomid;
                    if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                        obj.record.set("baseuomquantity", proqty*obj.record.get("baseuomrate"));
                    } else {
                        obj.record.set("baseuomrate", 1);
                        obj.record.set("baseuomquantity", proqty*obj.record.get("baseuomrate"));
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
    calAmount:function(v,m,rec){
        var quantity = rec.data.dquantity;
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
        
        var origionalAmount = rec.data.rate * quantity;
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
        if(index!=-1){ 
            
        var deliveredprodquantity = obj.data.dquantity;
        deliveredprodquantity = (deliveredprodquantity == "NaN" || deliveredprodquantity == undefined || deliveredprodquantity == null)?0:deliveredprodquantity;

         if(deliveredprodquantity<1){
              WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),WtfGlobal.getLocaleText("acc.Workorder.Quantityshouldbegreaterthanzero")], 2);
              return false;
         }   
        var prorec=this.productComboStore.getAt(index); 
        this.batchDetailswin=new Wtf.account.SerialNoWindow({
            renderTo: document.body,
            title:WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber"),
            productName:prorec.data.productname,
            //quantity:obj.data.dquantity,
            quantity:(obj.data.baseuomrate)*deliveredprodquantity,
            billid:obj.data.billid,
            defaultLocation:prorec.data.location,
            productid:prorec.data.productid,
            isSales:this.isCustomer,
            moduleid:this.moduleid,
            transactionid:(this.isCustomer)?4:5,
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
            isBatchForProduct:prorec.data.isBatchForProduct,
            isSerialForProduct:prorec.data.isSerialForProduct,
            linkflag:obj.data.linkflag,
            isEdit:this.isEdit,
            copyTrans:this.copyTrans,
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
    checkRow:function(obj){
        var rec=obj.record;
        var proqty = obj.record.data['quantity'];
        proqty = (proqty == "NaN" || proqty == undefined || proqty == null)?0:proqty;
        var deliveredproqty = obj.record.data['dquantity'];
        deliveredproqty = (deliveredproqty == "NaN" || deliveredproqty == undefined || deliveredproqty == null)?0:deliveredproqty;
        if(obj.field=="uomid"){
            var prorec = null;
            var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
            if(productComboIndex >=0){
                prorec = this.productComboStore.getAt(productComboIndex);
                if(prorec.data.type=='Service'){
                    return false;
                } else if(!prorec.data.multiuom){
                    return false;
                }
            }else if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag!=Wtf.Show_all_Products && prorec==undefined){
                prorec = obj.record;
                if(prorec.data.type=='Service'){//|| prorec.data.type=='Non-Inventory Part'
                    return false;
                } else if(this.UomSchemaType==Wtf.UOMSchema && !prorec.data.multiuom){ //&& prorec.data.type!='Non-Inventory Part'
                    return false;
                }
            }
        }
        if(obj.field=="productid" || obj.field=="pid"){
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
                    quantity = quantity + (deliveredproqty*obj.record.data['baseuomrate']);
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
                }else if(this.isCustomer&&availableQuantity<(deliveredproqty*obj.record.data['baseuomrate'])&& prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation){
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
                    quantity = quantity + deliveredproqty;
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
                var originalDquantity = deliveredproqty;
                var newDquantity = deliveredproqty;
                var originalBaseuomrate = obj.originalValue;
                var newBaseuomrate = obj.value;
            }
            prorec=this.productComboStore.getAt(this.productComboStore.find('productid',obj.record.data.productid));
            if(prorec==undefined || prorec==null){
                prorec= rec;
            } 
            if(prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part') {
                var availableQuantity = prorec.data.quantity;
                 var lockQuantity = prorec.data.lockquantity; 
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
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' is '+availableQuantity], 2);
//                        obj.cancel=true;
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
                if(result >= 0){
                    var prorec=this.productComboStore.getAt(result);////if product type is of Inventory then check otherwise no need to check 
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
    loadPOGridStore:function(recids,linkingFlag){                
        this.store.load({params:{bills:recids,mode:43,closeflag:true,doflag:true,linkingFlag:linkingFlag}});
        this.store.on('load',function(store1, recArr){     
            var tempStore=this.productComboStore;
            this.store.removeAll();
            if(!this.isNegativeStock){
            for(var count=0;count<recArr.length;count++){
                var record=recArr[count];
                if(record.data.dquantity==""){
                    
                    var quantity = record.data.quantity;
                    quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;

                    record.data.dquantity=quantity;
                }
                if(record.data.description==""){
                    var prorec=tempStore.getAt(tempStore.find('productid',record.data.productid));                                        
                    record.set('description',prorec.data.desc);
                }
                    this.store.add(record);   
            } 
          } 
           this.addBlankRow();   
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
//        this.priceStore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(this.billDate)}});
    },
        loadPriceAfterProduct : function(){
        if(Wtf.getCmp(this.id)){ //Load price store if component exists
//            this.loadPriceStore();
        } else {
            this.productComboStore.un("load",this.loadPriceAfterProduct,this);//Remove event handler if Not exists
        }
    },
     loadPriceStoreOnly:function(val,pricestore){  //scope related issue
        this.dateChange=true;
        this.billDate=(val==undefined?this.billDate:val);        //if(this.editTransaction)
        pricestore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(this.billDate)}});
    },
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
        if(this.currencyid && this.currencyid != undefined){
             this.forCurrency=currencyid;
        } 
        productid = productid.substring(0, (productid.length - 1) )
            Wtf.Ajax.requestEx({
                    url:"ACCProduct/getIndividualProductPrice.do",
                    params:{
                        productid: productid,
                        affecteduser: this.affecteduser,
                        forCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency:"",
                        currency: this.currencyid,
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
    }
});


