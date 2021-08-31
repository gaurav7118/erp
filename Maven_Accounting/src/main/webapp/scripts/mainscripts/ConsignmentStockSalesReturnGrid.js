Wtf.account.ConsignmentStockSalesReturnGrid=function(config){
    this.isCustomer=config.isCustomer;
    this.isLeaseFixedAsset=config.isLeaseFixedAsset?config.isLeaseFixedAsset:false;
    this.isConsignment = (config.isConsignment)?config.isConsignment:false;    
    this.UomSchemaType=Wtf.account.companyAccountPref.UomSchemaType;
    this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
    if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Show_all_Products){
        this.productComboStore=Wtf.FixedAssetAndProductLeaseStore;//(this.isFixedAsset)?Wtf.FixedAssetStore:(this.isCustomer?((this.isLeaseFixedAsset)?Wtf.FixedAssetAndProductLeaseStore:Wtf.productStoreSales):Wtf.productStore);      
    }else if(this.productOptimizedFlag==Wtf.Products_on_type_ahead || this.productOptimizedFlag==Wtf.Products_on_Submit){
        this.productComboStore=Wtf.FixedAssetAndProductLeaseStoreOptimized;      
    }
//    this.productComboStore=this.isCustomer?Wtf.productStoreSales:Wtf.productStore;
    this.currencyid=config.currencyid;
    this.productID=null;
    this.id=config.id;
//    this.isOrder=config.isOrder;
    this.record=config.record;
    this.billDate=new Date();
    this.dateChange=false;
    this.pronamearr=[];
    this.fromPO=config.fromPO;
    this.readOnly=config.readOnly;
    this.editTransaction=config.editTransaction;
    this.editLinkedTransactionQuantity= Wtf.account.companyAccountPref.editLinkedTransactionQuantity;
    this.editLinkedTransactionPrice= Wtf.account.companyAccountPref.editLinkedTransactionPrice;
    this.isLinkedTransaction= (config.isLinkedTransaction == null || config.isLinkedTransaction == undefined)? false : config.isLinkedTransaction;
    this.isEdit=config.isEdit;
    this.noteTemp=config.noteTemp;
    this.fromOrder=config.fromOrder;
    this.moduleid = config.moduleid;
    this.CUSTOM_KEY = "customfield";
    this.gridConfigId ="";
    this.createStore();
    this.createComboEditor();
    this.createColumnModel();
    //this.loadPriceStore();
    var colModelArray = [];
    colModelArray = GlobalColumnModel[this.moduleid];
    WtfGlobal.updateStoreConfig(colModelArray,this.store);
    this.parentCmpID=config.parentCmpID;
    
    Wtf.account.ConsignmentStockSalesReturnGrid.superclass.constructor.call(this,config);
    this.addEvents({
        'datachanged':true,
        'pricestoreload':true
    });
   this.on('populateDimensionValue',this.populateDimensionValueingrid,this);
}
Wtf.extend(Wtf.account.ConsignmentStockSalesReturnGrid,Wtf.grid.EditorGridPanel,{
    clicksToEdit:1,
    stripeRows :true,
    rate:1,
    symbol:null,
    layout:'fit',
    viewConfig:{forceFit:true},
    forceFit:true,
    loadMask:true,
    onRender:function(config){
        Wtf.account.ConsignmentStockSalesReturnGrid.superclass.onRender.call(this,config);
        this.isValidEdit = true;
//         this.on('render',this.addBlankRow,this);                  //   ERP-15992
        WtfGlobal.getGridConfig(this,this.moduleid,true,false);
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
             
             if(this.isLeaseFixedAsset){
                if(this.fromPO == undefined || this.fromPO == null || !this.fromPO){
                    e.cancel=true;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.fixed.asset.sr.lease") ], 4);
                    return;
                }
            }
            
             if(this.isConsignment){
               if(this.isCustomer){// you can create consignment sales invoice only after selecting customer warehouse
                if(this.warehouseselcted == undefined || this.warehouseselcted == null || !this.warehouseselcted){
                    e.cancel=true;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.Consignment.warehouse.select") ], 4);
                    return;
                }
              }
            }
            if(this.isLeaseFixedAsset && this.fromPO){
                if(this.store.getCount()-1==e.row){// if last row then don't allow edition, and appending row in case of linking'
                    e.cancel=true;
                    return;
                }
            }
           if(this.isConsignment && this.warehouseselcted){
            if(this.store.getCount()-1==e.row){// if last row then don't allow edition, and appending row in case of linking'
                e.cancel=true;
                return;
            }
        }
             
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
            var isRateFieldEditable = true;
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
            		e.cancel = true;
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
           //Not allowing to edit the product,uom,quantity,delivered quantity and baseuom rate if it is linked in transaction
            if((this.isLinkedTransaction !=undefined && this.isLinkedTransaction) && (e.field == "productid" || e.field == "uomid" || e.field == "rate" || e.field == "baseuomrate" || e.field == "quantity" || e.field == "dquantity")){
                e.cancel=true;
                return;
            }

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
    populateDimensionValueingrid: function (rec) {
        WtfGlobal.populateDimensionValueingrid(this.moduleid, rec, this);
    },
    checkDetails: function(grid) {
        var v = WtfGlobal.checkValidItems(this.moduleid, grid);
        return v;
    },
    RitchTextBoxSetting:function(grid, rowIndex, columnIndex, e){
        var v=WtfGlobal.RitchTextBoxSetting(grid, rowIndex, columnIndex, e,this.readOnly);
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
            {name: 'consignquantity'},
            {name: 'venconsignuomquantity'},
            {name:'currencysymbol',defValue:this.symbol},
            {name:'isAsset',type:'boolean'},
            {name:'hasAccess'}
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
            {name:'rowid'},
            {name:'productname'},
            {name:'billid'},
            {name:'billno'},
            {name:'productid'},
            {name:'description'},
            {name:'partno'},
            {name:'quantity',defValue:1},
            {name:'dquantity',defValue:1},
            {name:'copyquantity'},
            {name:'copybaseuomrate',mapping:'baseuomrate'},//for handling inventory updation 
            {name:'baseuomquantity',defValue:1.00},
            {name:'uomname'},
            {name:'baseuomname'},
            {name:'salesuomname'},
            {name:'purchaseuomname'},
            {name:'salesuom'},
            {name:'purchaseuom'},
            {name:'stocksalesuomvalue'},
            {name:'stockpurchaseuomvalue'},
            {name:'uomid'},
            {name:'stockuom'},
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
            {name:'changedQuantity'},
            {name:'producttype'},
            {name:'permit'},
            {name:'linkto'},
            {name:'originalTransactionRowid'},
            {name:'batchdetails'},
            {name:'linkid'},
            {name:'invcreationdate'},//added to get date of linked document
            {name:'linktype'},
            {name:'customfield'},
            {name:'rate',defValue:0},
            {name:'assetDetails'},
            {name:'isAsset',type:'boolean'},
            {name:'amount',defValue:0},
            {name:'pid'},
            {name:'totalissuecount'},
            {name:'isreusable',defValue:0},
            {name:'pid'},
            {name: 'isLocationForProduct'},
            {name: 'isRowForProduct'},
            {name: 'isRackForProduct'},
            {name: 'isBinForProduct'},
            {name: 'isWarehouseForProduct'},
            {name: 'isBatchForProduct'},
            {name: 'isSerialForProduct'},
            {name: 'isSKUForProduct'},
            {name: 'isAsset'},
            {name: 'location'},
            {name: 'warehouse'},
            {name: 'srno', isForSequence:true}
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
        
    },
    createComboEditor:function(){
        
//        this.productComboStore.on('beforeload',function(){
//            alert('beforeload');
//        },this);
//
//        this.productComboStore.on('loadexception',function(){
//            alert('loadexception');
//        },this);
        
        this.productId= new Wtf.form.TextField({
            name:'pid'
//            readOnly:true
        });
        
        this.productComboStore.on('beforeload',function(s,o){
                if(!o.params)o.params={};
                var currentBaseParams = this.productComboStore.baseParams;
                currentBaseParams.getSOPOflag=true;
                currentBaseParams.module_name="CONSIGNMENT_RETURN";
                currentBaseParams.onlyProduct=this.isOrder?false:true
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
            
        if(this.productOptimizedFlag== undefined || this.productOptimizedFlag==Wtf.Show_all_Products){

            this.productComboStore.load();

            this.productEditor=new Wtf.form.ExtFnComboBox({
                name:'pid',
                store:this.productComboStore,
                typeAhead: true,
                selectOnFocus:true,
                maxHeight:250,
                listAlign:"bl-tl?",//[ERP-5149] To expand list of combobox always on top. 
                valueField:'productid',
                displayField:'pid',
                extraFields:['productname','type'],
                extraComparisionField:['pid','productname'],// type ahead search on acccode as well.
                listWidth:400,
                scope:this,
                hirarchical:true,
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
                    extraComparisionField:['pid','productname'],// type ahead search on acccode as well.
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
//        if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.edit))
//            this.productEditor.addNewFn=this.openProductWindow.createDelegate(this);
        this.productEditor.on('beforeselect', function(combo, record, index) {
                return validateSelection(combo, record, index);
        }, this);
        this.productComboStore.on("load",this.loadPriceAfterProduct,this);
        
        this.productComboStore.on("loadexception",function(){
            if(this.ProductloadingMask){
                this.ProductloadingMask.hide();
            }
        },this);
        
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
        
        this.remark= new Wtf.form.TextField({
            name:'remark'
//            readOnly:true
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
//            header:WtfGlobal.getLocaleText("acc.product.gridProductID"),//"Product",
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
                header:(this.isLeaseFixedAsset ||this.isConsignment)?WtfGlobal.getLocaleText("acc.product.gridProductID"):WtfGlobal.getLocaleText("acc.invoice.gridAssetGroup"),//"Asset Group",
                width:200,
                dataIndex:(this.readOnly ||this.productOptimizedFlag==Wtf.Products_on_type_ahead)?'pid':'productid',
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
//        },{
//            header:this.isCN?WtfGlobal.getLocaleText("acc.invoice.gridInvNo"):WtfGlobal.getLocaleText("acc.invoice.gridVenInvNo"),//"Invoice No.":"Vendor Invoice No.",
//            width:150,
//            dataIndex:this.noteTemp?'transectionno':'billno',
//            hidden:!this.isNote
//        },{
//             header:WtfGlobal.getLocaleText("acc.do.partno"),//"Part No",
//             dataIndex:"partno",
//             width:250,
//             editor:this.readOnly?"":this.partno
         },{
             header:WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"),//"Description",
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
          columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModel[this.moduleid],undefined,undefined,this.readOnly);
          columnArr.push({
             header:WtfGlobal.getLocaleText("acc.field.InventoryStore"), 
             dataIndex:'invstore',
             hidden:!(Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel),                 
             width:150,
             renderer:Wtf.comboBoxRenderer(this.inventoryStores),
             editor:(this.readOnly)?"":this.inventoryStores
         },{
             header:WtfGlobal.getLocaleText("acc.field.InventoryLocation"), 
             dataIndex:'invlocation',
             hidden:!(Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel),                 
             width:150,
             renderer:Wtf.comboBoxRenderer(this.inventoryLocation),
             editor:(this.readOnly)?"":this.inventoryLocation
         },{
             header:WtfGlobal.getLocaleText("acc.field.ActualQuantity"),
             dataIndex:"quantity",             
             align:'right',
             width:200,
             editor:"",  //this.actQuantity, ERP-8988 (don't allow to change actual quantity)
             renderer:this.readOnly?"":this.quantityRenderer
//             renderer:this.storeRenderer(this.productComboStore,"productid","uomname")
        },{
             header:WtfGlobal.getLocaleText("acc.accPref.returnQuant"),
             dataIndex:"dquantity",
             align:'right',
             width:100,
             editor:this.deliQuantity,
             renderer:this.readOnly?"":this.quantityRenderer
//             renderer:this.storeRenderer(this.productComboStore,"productid","uomname")
         },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridUOM"),//"UOM",
            width:100,
            dataIndex:this.readOnly?'uomname':'uomid',
            renderer:this.readOnly?"":Wtf.comboBoxRendererwithClearFilter(this.uomEditor),
            editor:(this.isNote||this.readOnly)?"":this.uomEditor
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridRateToBase"),//Base UOM Rate
             dataIndex:"baseuomrate",
             align:'left',
             width:100,
             renderer:this.conversionFactorRenderer(this.productComboStore,"productid","uomname",this.store),
             editor:(this.isNote||this.readOnly||this.UomSchemaType==Wtf.PackegingSchema)?"":this.transBaseuomrate
         },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridQtyInBase"),//Base UOM Quantity
             dataIndex:"baseuomquantity",
             align:'right',
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
            hidden: !(this.isCustomer?Wtf.account.companyAccountPref.unitPriceInSR:Wtf.account.companyAccountPref.unitPriceInPR)
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridAmount"), // "Amount",
             dataIndex:"amount",
             align:'right',
             width:200,
             renderer:(this.isNote?WtfGlobal.withoutRateCurrencySymbol:this.calAmount.createDelegate(this)),
             hidden: !(this.isCustomer?Wtf.account.companyAccountPref.unitPriceInSR:Wtf.account.companyAccountPref.unitPriceInPR)
        },{
                header:WtfGlobal.getLocaleText("acc.field.Remarks"),  //"Remark",
                dataIndex:"remark",
                editor:this.readOnly?"":this.Description=new Wtf.form.TextArea({
                    maxLength:200,
                    allowBlank: false,
                    xtype:'textarea'
                })
        },{
            header: '',
            align:'center',
            renderer:function(a,b,c){
                var isAsset=c.data.isAsset;
                if(isAsset){
                    return viewRenderer();
               }else if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory) {
//             }else {
                    return serialRenderer();  
                    
                }
            },
//            hidden:!(Wtf.account.companyAccountPref.showprodserial),
            width:40
        });
        if(!this.isNote && !this.readOnly && !this.isLinkedTransaction) {
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),//"Action",
                align:'center',
                width:40,
                renderer: this.deleteRenderer.createDelegate(this)
            });
        }
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
    viewRenderer:function(v,m,rec){
        return "<div class='view pwnd view-gridrow'  title='View Asset Details '></div>";
    },
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    quantityRenderer:function(val,m,rec){
      return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
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
            var productid = record.get('productid');
              var customerID="";
              if(Wtf.getCmp(this.parentCmpID) != undefined  && Wtf.getCmp(this.parentCmpID).Name != undefined ){
                customerID=Wtf.getCmp(this.parentCmpID).Name.getValue();
                
            }
            var warehouseid="";
              if(Wtf.getCmp(this.warehouseid) != undefined){
                warehouseid=Wtf.getCmp(this.warehouseid).getValue();
                
            }
            var linkflag=false;//Sent flase as we don't want to send Batch details 
            if(Wtf.getCmp('linkToOrder'+this.heplmodeid+this.parentid) ) //&& this.moduleid==Wtf.Acc_ConsignmentPurchaseReturn_ModuleId
            { 
                linkflag=Wtf.getCmp('linkToOrder'+this.heplmodeid+this.parentid).getValue();
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
                if(proRecord.data.type!='Service' || proRecord.data.type!='Non-Inventory Part'){
                 if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory){ //if company level option is on then only check batch and serial details
                  if(proRecord.data.isBatchForProduct || proRecord.data.isSerialForProduct ||( proRecord.data.isLocationForProduct && proRecord.data.isWarehouseForProduct))
{
                            this.callSerialNoWindow(record,customerID,warehouseid);
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
     callSerialNoWindow:function(obj,customerID,warehouseid){
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
        this.batchDetailswin=new Wtf.account.SerialNoWindow({
            renderTo: document.body,
            title:WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber"),
            productName:prorec.data.productname,
            readOnly: this.readOnly,
            uomName:prorec.data.baseuomname,
            quantity:(obj.data.baseuomrate)*deliveredprodquantity,
            billid:obj.data.billid,
            defaultLocation:prorec.data.location,
            productid:prorec.data.productid,
            isLinkedFromGR:isLinkedFromGR,
            isLinkedFromPI:isLinkedFromPI,
            isLinkedFromDO:isLinkedFromDO,
            isLinkedFromSI:isLinkedFromSI,
            customerID:this.isCustomer?customerID:"",
            isSales:true,
            isForCustomer:this.isCustomer,
            moduleid:this.moduleid,
            transactionid:(this.isCustomer)?4:5,
            isDO:this.isCustomer?true:false,
            defaultWarehouse:prorec.data.warehouse,
//            batchDetails:(this.isEdit)?obj.data.batchdetails:obj.data.linkflag?obj.data.batchdetails:this.fromPO?obj.data.batchdetails:"",
            batchDetails:(this.isEdit)?obj.data.batchdetails:obj.data.linkflag?obj.data.batchdetails:"",  //as in return case companys store and data was poupating so while linking DO in return do not populate data
//            batchDetails:obj.data.batchdetails ,
            warrantyperiod:prorec.data.warrantyperiod,
            warrantyperiodsal:prorec.data.warrantyperiodsal,  
            isConsignment:this.isConsignment,
            isLocationForProduct:(this.isConsignment&&this.moduleid==Wtf.Acc_ConsignmentSalesReturn_ModuleId)?false:prorec.data.isLocationForProduct, //this.isConsignment?false:
            isWarehouseForProduct:prorec.data.isWarehouseForProduct,
            isBatchForProduct:prorec.data.isBatchForProduct,
            isSerialForProduct:prorec.data.isSerialForProduct,
            isSKUForProduct:prorec.data.isSKUForProduct,
            isRackForProduct:prorec.data.isRackForProduct,
            isRowForProduct:prorec.data.isRowForProduct,
            isBinForProduct:prorec.data.isBinForProduct,
            linkflag:(obj.data.linkflag)?obj.data.linkflag:this.fromPO,
            documentid:obj.data.rowid,
            isEdit:this.isEdit,
            isConsignReturn:this.isCustomer,
            warehouseid:warehouseid,
            isItemReusable:0,
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
        this.batchDetailswin.show();
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
                    rec.set("changedQuantity",(rec.data.copybaseuomrate-obj.value)*((rec.data.copyquantity==="" || rec.data.copyquantity==undefined)?1:rec.data.copyquantity));
                else
                    rec.set("changedQuantity",(obj.value-rec.data.copybaseuomrate)*((rec.data.copyquantity==="" || rec.data.copyquantity==undefined)?1:rec.data.copyquantity));
                
                  var productComboIndex = WtfGlobal.searchRecordIndex(this.priceStore, obj.record.get('productid'), 'productid');
                  var productuomid = "",datewiseprice=0;
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
                  
//                  this.fireEvent('datachanged',this);
            }
            if(obj.field=="uomid"){
                  var prorec = null;
                  var productComboIndex = WtfGlobal.searchRecordIndex(this.priceStore, obj.record.get('productid'), 'productid');
                  var productuomid = "";
                  if(productComboIndex >=0){
                      var baseuomrate =1,rateperuom=0,datewiseprice=0;
                      prorec = this.priceStore.getAt(productComboIndex);
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
            }
            if(obj.field=="productid" || obj.field=="pid"){
                rec=obj.record;
                var index=this.priceStore.find('productid',obj.value);
                if(this.isCustomer)
                    rec.set("changedQuantity",(rec.data.quantity*(-1))*((rec.data.baseuomrate===""||rec.data.baseuomrate==undefined)?1:rec.data.baseuomrate));
                else
                    rec.set("changedQuantity",(rec.data.quantity)*((rec.data.baseuomrate===""||rec.data.baseuomrate==undefined)?1:rec.data.baseuomrate));
                if(index>=0){
                    rec=this.priceStore.getAt(index);
                    obj.record.set("description",rec.data["desc"]);
                    obj.record.set("isAsset",rec.data["isAsset"]);
                    obj.record.set("prtaxid", "");
                    obj.record.set("taxamount","");
                    obj.record.set("quantity",1);
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
                    url:"ACCProductCMN/getIndividualProductPrice.do",
                    params:{
                        productid:this.productOptimizedFlag==Wtf.Products_on_Submit?productid:obj.value,
                        affecteduser: this.affecteduser,
                        transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                        carryin : (this.isCustomer)? false : true,
                        getSOPOflag  :true,
                        startdate : WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true)),
                        enddate : WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false))
                    }
                }, this,function(response){
                    var datewiseprice =response.data[0].price;
                    
                    var pocountinselecteduom = response.data[0].pocountinselecteduom;
                    var socountinselecteduom = response.data[0].socountinselecteduom;
                    
                    
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
                    var baseuomRate=1;
                    var prorec = null;
                    var acctaxcode = (this.isCustomer)?"salesacctaxcode":"purchaseacctaxcode";
                    var protaxcode = "";
                    if(productComboIndex >=0){
                        prorec = this.productComboStore.getAt(productComboIndex);
                        productname = prorec.data.productname;
                        proddescription = prorec.data.desc;
                        productuomid = prorec.data.uomid;
                        productsuppliernumber= prorec.data.supplierpartnumber;
                        shelfLocation = prorec.data.shelfLocation;
                        protaxcode = prorec.data[acctaxcode];
                        
                        obj.record.set("availableQtyInSelectedUOM", prorec.data['quantity']);
                        obj.record.set("uomname", prorec.data['uomname']);
                        obj.record.set("blockLooseSell", prorec.data['blockLooseSell']);
                        obj.record.set("pocountinselecteduom", pocountinselecteduom);
                        obj.record.set("socountinselecteduom", socountinselecteduom);
                        obj.record.set("isLocationForProduct", prorec.data['isLocationForProduct']);
                        obj.record.set("isWarehouseForProduct", prorec.data['isWarehouseForProduct']);
                        obj.record.set("isBatchForProduct", prorec.data['isBatchForProduct']);
                        obj.record.set("isSerialForProduct", prorec.data['isSerialForProduct']);
                        obj.record.set("isSKUForProduct", prorec.data['isSKUForProduct']);
                        obj.record.set("isFromVendorConsign", prorec.data['isFromVendorConsign']);
                        obj.record.set("isRowForProduct", prorec.data['isRowForProduct']);
                        obj.record.set("isRackForProduct", prorec.data['isRackForProduct']);
                        obj.record.set("isBinForProduct", prorec.data['isBinForProduct']);  
                        obj.record.set("isAsset", prorec.data['isAsset']);  
                        obj.record.set("location", prorec.data["location"]);
                        obj.record.set("warehouse", prorec.data["warehouse"]);
                    }
                    obj.record.set("desc",proddescription);
                    obj.record.set("supplierpartnumber",productsuppliernumber);
                    obj.record.set("shelfLocation",shelfLocation);
                    obj.record.set("productname", productname);
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
                                obj.record.set("uomid", productuomid);
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
                                if (Wtf.account.companyAccountPref.unitPriceInSR) {
                                    Wtf.Msg.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pricefortheproduct") + " <b>" + productname + "</b> " + ' ' + WtfGlobal.getLocaleText("acc.field.isnotsetDoyouwanttosetitnow"),
                                            this.showPriceWindow.createDelegate(this, [rec, obj], true), this);
                                }
                            } else {
                                if (Wtf.account.companyAccountPref.unitPriceInPR) {
                                    Wtf.Msg.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pricefortheproduct") + " <b>" + productname + "</b> " + ' ' + WtfGlobal.getLocaleText("acc.field.isnotsetDoyouwanttosetitnow"),
                                            this.showPriceWindow.createDelegate(this, [rec, obj], true), this);
                                }
                            }
                            obj.record.set("rate", 0);
                        }else{
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pricefortheproduct")+" <b>"+productname+"</b>"+' '+WtfGlobal.getLocaleText("acc.field.isnotset")], 2);
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
                        }else{//For Packaging Achema Type
                            obj.record.set("rate", modifiedRate*baseuomRate);
                        }
                    }
                    this.fireEvent('datachanged',this);
                }, function(){
                });
            }else if(obj.field=="quantity"){
                rec=obj.record;               
                //if((rec.data.isNewRecord=="" || rec.data.isNewRecord==undefined) && this.fromOrder&&!(this.editTransaction||this.copyInv)) {
                if(this.isCustomer)
                  rec.set("changedQuantity",(rec.data.copyquantity-obj.value)*((rec.data.baseuomrate===""||rec.data.baseuomrate==undefined)?1:rec.data.baseuomrate));
                  else
                    rec.set("changedQuantity",(obj.value-rec.data.copyquantity)*((rec.data.copybaseuomrate==="")?1:rec.data.copybaseuomrate));
                if(((rec.data.isNewRecord=="" || rec.data.isNewRecord==undefined) && this.fromOrder && !this.isConsignment &&!(this.editTransaction||this.copyInv))||(this.isEdit && rec.data.linkid !="")) {  
//                   if(obj.value >rec.data.copyquantity){  
//                        var msg=this.isCustomer?WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinSRisexceeds"):WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinPRisexceeds");
//                        obj.record.set(obj.field, obj.originalValue);
//                        
//                         Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.common.alert"),msg,{
//                                
//                        },this)
//                   }else
                       if(obj.value!=rec.data.copyquantity) {
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"),(this.isCustomer)?WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinSRisdifferent"):WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinPRisdifferent"),function(btn){
                            if(btn!="yes") {
                                obj.record.set(obj.field, obj.originalValue);
                                obj.record.set("baseuomquantity",obj.originalValue*obj.record.get("baseuomrate"));
                                }else{
                                    obj.record.set("dquantity", obj.value);
//                                    rec.set("changedQuantity",(rec.data.copyquantity-obj.value)*((rec.data.baseuomrate===""||rec.data.baseuomrate==undefined)?1:rec.data.baseuomrate));
                                }
                        },this)
                    }
                }
                if((obj.record.data["quantity"])==0){
                    this.store.remove(obj.record);
                }
                  var productComboIndex = WtfGlobal.searchRecordIndex(this.priceStore, obj.record.get('productid'), 'productid');
                  var productuomid = "";
                  var availableQuantity ="";
                  if(productComboIndex >=0){
                      prorec = this.priceStore.getAt(productComboIndex);
                      productuomid = prorec.data.uomid;
                      availableQuantity = prorec.data.consignquantity;
                      if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                            obj.record.set("baseuomquantity", obj.record.get("quantity")*obj.record.get("baseuomrate"));
                      } else {
                          if(this.isCustomer){
                              if((!this.editTransaction||this.copyInv) &&  availableQuantity < (obj.record.get("quantity")*obj.record.get("baseuomrate"))) {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.quantityinreturncannoybegreater") ], 2);
                                  obj.record.set("dquantity",availableQuantity);  
                                  obj.record.set("baseuomquantity",availableQuantity);  
                                  obj.record.set("quantity",availableQuantity);  
                                obj.cancel=true;
                            }else{
                                obj.record.set("baseuomrate", 1);
                                obj.record.set("baseuomquantity", obj.record.get("quantity")*obj.record.get("baseuomrate"));
                                obj.record.set("dquantity", obj.record.data.quantity);
                            }
                          }else{
                              if(obj.record.get("quantity")*obj.record.get("baseuomrate")>prorec.data.venconsignuomquantity){
                                   var msg = WtfGlobal.getLocaleText("acc.field.venQtyinPRareexceedingtheqty");
                                  WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg], 2);
                                    obj.record.set("baseuomquantity",prorec.data.venconsignuomquantity);
                                    obj.record.set("dquantity", prorec.data.venconsignuomquantity);
                                    obj.record.set("quantity", prorec.data.venconsignuomquantity);
                              }else{
                                    obj.record.set("baseuomquantity", obj.record.get("quantity")*obj.record.get("baseuomrate"));
                                    obj.record.set("dquantity", obj.record.data.quantity);
                              }
                              
                          }
                      }
                  }
              
//                    obj.record.set("invstore", this.batchDetailswin.defaultLocation);
//                    obj.record.set("invlocation", this.batchDetailswin.defaultWarehouse);
//                
            } else if(obj.field=="dquantity"){
                rec=obj.record;
                if(!this.isCustomer)
                    rec.set("changedQuantity",(rec.data.copyquantity-obj.value)*((rec.data.copybaseuomrate===""||rec.data.copybaseuomrate==undefined)?1:rec.data.copybaseuomrate));
                else
                    rec.set("changedQuantity",(obj.value-rec.data.copyquantity)*((rec.data.copybaseuomrate===""||rec.data.copybaseuomrate==undefined)?1:rec.data.copybaseuomrate));
                
                if(obj.record.data.dquantity > obj.record.data.quantity){
                    var msg = WtfGlobal.getLocaleText("acc.field.Returnquantityshouldnotbegreater");
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg], 2);
                    obj.record.set("dquantity", obj.record.data.quantity);
                } else if(obj.record.data.dquantity <= 0){
                    var msg = WtfGlobal.getLocaleText("acc.field.Returnquantityshouldnotbeequalorlessthanzero");
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg], 2);
                    obj.record.set("dquantity", obj.record.data.quantity);
                }
                  obj.record.set("baseuomquantity", obj.record.data.dquantity);
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
            isEdit:this.isEdit,
            copyTrans:this.copyTrans,
            isFixedAsset:this.isFixedAsset,
            isFromOrder:true,
            readOnly:this.readOnly,
            isFromSalesReturn:true,
            width:950,
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
            var index=this.productComboStore.findBy(function(rec){
                if(rec.data.productid==obj.value)
                    return true;
                else
                    return false;
            })
            prorec=this.productComboStore.getAt(index);
            var useStoreRec=false;
            if(prorec==undefined){
                prorec= rec;
                useStoreRec=true;
            }  
            index=this.priceStore.find('productid',obj.value)
            rec=this.priceStore.getAt(index);
            if(this.isConsignment){  //fot consignment do not allow to change the product
                obj.cancel=true;
            }
//            if(this.store.find("productid",obj.value)>=0&&obj.ckeckProduct==undefined){
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.76")+" "+rec.data['productname']], 2);
//                    obj.cancel=true;
//            }
            if(this.editTransaction){  //In Edit Case Check product quantity is greater than available quantity when selecting product
                var availableQuantity = prorec.data.quantity;    //This is in base UOM
                if(useStoreRec){
                    availableQuantity = prorec.data.availablequantity;
                }    //This is in base UOM
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
                }
            } 
         
          } else if(this.isCustomer && obj.field=="dquantity"){   
               prorec=this.productComboStore.getAt(this.productComboStore.find('productid',obj.record.data.productid));
               var newQuantity = obj.value;
               if(prorec==undefined){
                    prorec= rec;
                }
                if(prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part') {
                    var availableQuantity = prorec.data.consignquantity;
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
                        quantity = quantity + (newQuantity*obj.record.data['baseuomrate']);
                        availableQuantity = availableQuantity + copyquantity;   
                        if(this.editTransaction&&!this.copyInv&&availableQuantity < quantity) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.quantityininvoicecannoybegreater") ], 2);
                            rec.set("dquantity",availableQuantity);  
                            rec.set("baseuomquantity",availableQuantity);  
                            obj.cancel=true;
                        }
                    } else{
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
                        quantity = quantity + (newQuantity*obj.record.data['baseuomrate']);
                        if((!this.editTransaction||this.copyInv) &&  availableQuantity < quantity) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.quantityininvoicecannoybegreater") ], 2);
                            rec.set("dquantity",availableQuantity);  
                            rec.set("baseuomquantity",availableQuantity);  
                            obj.cancel=true;
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

    loadPOGridStore:function(rec,linkingFlag){        
        this.store.load({params:{bills:rec.data['billid'],mode:43,closeflag:true,doflag:true,linkingFlag:linkingFlag,isConsignment:true,moduleid:this.moduleid,isfromconsignreturn:true}});
        this.store.on('load',function(rec){
            this.store.each(function(rec){
                if(rec.data.dquantity==""){
                    rec.data.dquantity=rec.data.quantity;
                }
            });
        },this);
    },
    getProductDetails:function(){

        var arr=[];
        this.store.each(function(rec){
            if(rec.data.productid!=""){
                rec.data[CUSTOM_FIELD_KEY]=Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, this.moduleid).substring(13));
                arr.push(this.store.indexOf(rec));
            }
        },this)
        var jarray=WtfGlobal.getJSONArray(this,false,arr);
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
        this.priceStore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(this.billDate)}});           
    },
    loadPriceAfterProduct : function(){
        if(this.ProductloadingMask){
            this.ProductloadingMask.hide();
        }
        
        if(Wtf.getCmp(this.id)){ 
            this.loadPriceStore();
        } else {
            this.productComboStore.un("load",this.loadPriceAfterProduct,this);
        }
    },
     loadPriceStoreOnly:function(val,pricestore){  
        this.dateChange=true;
        this.billDate=(val==undefined?this.billDate:val);        
        pricestore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(this.billDate)}});
    }
});