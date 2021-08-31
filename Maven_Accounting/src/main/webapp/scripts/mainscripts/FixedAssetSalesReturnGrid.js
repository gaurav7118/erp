Wtf.account.FixedAssetSalesReturnGrid=function(config){
    this.isCustomer=config.isCustomer;
    this.isFixedAsset = (config.isFixedAsset)? config.isFixedAsset : false;
    this.isLeaseFixedAsset=config.isLeaseFixedAsset?config.isLeaseFixedAsset:false;
    this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
    if(this.productOptimizedFlag!= undefined || this.productOptimizedFlag==Wtf.Show_all_Products){
        this.productComboStore = (this.isFixedAsset)? Wtf.FixedAssetStore : Wtf.FixedAssetAndProductLeaseStore;
    }else if(this.productOptimizedFlag==Wtf.Products_on_type_ahead || this.productOptimizedFlag==Wtf.Products_on_Submit){
        this.productComboStore = (this.isFixedAsset)? Wtf.FixedAssetStoreOptimized : Wtf.FixedAssetAndProductLeaseStoreOptimized;
    }
   //(this.isFixedAsset)?Wtf.FixedAssetStore:(this.isCustomer?((this.isLeaseFixedAsset)?Wtf.FixedAssetAndProductLeaseStore:Wtf.productStoreSales):Wtf.productStore);
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
    this.isEdit=config.isEdit;
    this.noteTemp=config.noteTemp;
    this.fromOrder=config.fromOrder;
    this.moduleid = config.moduleid;
    this.CUSTOM_KEY = "customfield";
    this.gridConfigId="";
    this.createStore();
    this.createComboEditor();
    this.createColumnModel();
    this.loadPriceStore();
    var colModelArray = GlobalColumnModel[this.moduleid];
    WtfGlobal.updateStoreConfigStringDate(colModelArray, this.store);
    colModelArray = [];
    if(colModelArray) {
        colModelArray=(GlobalColumnModelForProduct[this.moduleid]);        //ERP-12878
    }
    WtfGlobal.updateStoreConfigStringDate(colModelArray, this.store);
    this.readOnly=config.readOnly;
    Wtf.account.FixedAssetSalesReturnGrid.superclass.constructor.call(this,config);
    this.addEvents({
        'datachanged':true,
        'pricestoreload':true,
        'gridconfigloaded':true//// Event fire when WtfGlobal.getGridConfig() called and config get applied to grid column
    });
    this.on('populateDimensionValue',this.populateDimensionValueingrid,this);
}
Wtf.extend(Wtf.account.FixedAssetSalesReturnGrid,Wtf.grid.EditorGridPanel,{
    clicksToEdit:1,
    stripeRows :true,
    rate:1,
    symbol:null,
    layout:'fit',
    viewConfig:{forceFit:true},
    forceFit:true,
    loadMask:true,
    onRender:function(config){
        Wtf.account.FixedAssetSalesReturnGrid.superclass.onRender.call(this,config);
        this.isValidEdit = true;
        //         this.on('render',this.addBlankRow,this);                     // ERP-15992        
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
             
            if (this.isLeaseFixedAsset || (this.isFixedAsset && this.moduleid == Wtf.Acc_FixedAssets_Sales_Return_ModuleId)) {
                if(this.fromPO == undefined || this.fromPO == null || !this.fromPO){
                    e.cancel=true;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.fixed.asset.sr.lease") ], 4);
                    return;
                }
            }
            
            if (this.isFixedAsset) {
                if (this.fromPO == undefined || this.fromPO == null || !this.fromPO) {
                    e.cancel = true;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.fixed.asset.pr") ], 4);
                    return;
                }
            }
            
            if(this.isLeaseFixedAsset && this.fromPO){
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

         },this);   
         WtfGlobal.hideShowCustomizeLineFields(this,this.moduleid);
        /*
         * Hide column P.tax class,Tax amount for India country
         */
         if(WtfGlobal.isIndiaCountryAndGSTApplied()){
//	         if(this.record == null || this.record == undefined && this.getColumnModel().getColumnById(this.id+"prtaxid").hidden == undefined && this.getColumnModel().getColumnById(this.id+"taxamount").hidden == undefined){
	         if(this.record == null || this.record == undefined && this.getColumnModel().isHidden(this.getColumnModel().findColumnIndex("prtaxid")) == undefined && this.getColumnModel().isHidden(this.getColumnModel().findColumnIndex("taxamount")) == undefined){
                         this.getColumnModel().setHidden(this.getColumnModel().findColumnIndex("prtaxid"), true);				// 21241   If statement added bcos could not use the event destroy for column model
	        	 this.getColumnModel().setHidden(this.getColumnModel().findColumnIndex("taxamount"), true);							// and also could not call the createColumnModel() method from onRender
	         }
         }         
     },
    saveGridStateHandler: function(grid,state){
        if(!this.readOnly){
            WtfGlobal.saveGridStateHandler(this,grid,state,this.moduleid,this.gridConfigId,true);
        }
    }, 
    checkDetails: function(grid) {
        var v = WtfGlobal.checkValidItems(this.moduleid, grid);
        return v;
    },
    populateDimensionValueingrid: function(rec) {
        WtfGlobal.populateDimensionValueingrid(this.moduleid, rec, this);
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
            {name: 'level'},
            {name: 'initialquantity',mapping:'initialquantity'},
            {name: 'initialprice'},
            {name: 'producttype'},
            {name:'currencysymbol',defValue:this.symbol},
            {name:'isAsset',type:'boolean'}
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
            {name:'uomid'},
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
            {name:'invcreationdate'},//For date of linked documents
            {name:'linktype'},
            {name:'customfield'},
            {name:'rate',defValue:0},
            {name:'assetDetails'},
            {name:'isAsset',type:'boolean'},
            {name:'amount',defValue:0},
            {name:'linkflag'},
            {name:'discount'},
            {name:'discountispercent',defValue:1},
            {name:'prdiscount',defValue:0},
            {name:'prtaxid'},
            {name:'taxamount'},
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
            {name:'availablequantity'},
            {name:'warrantyperiod'},
            {name:'warrantyperiodsal'},
            {name: 'isFromVendorConsign'},
            {name: 'location'},
            {name: 'warehouse'},
            {name:'pricingbandmasterid'},
            {name: 'srno', isForSequence:true},
            {name:'hasAccess'},
            {name: 'isSerialForProduct'},           
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
//            sortInfo:{
//                field:'srno',
//                direction:'ASC'
//            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.storeRec),
            baseParams : {
                srflag : true,
                prflag: (this.isFixedAsset && this.moduleid == Wtf.Acc_FixedAssets_Purchase_Return_ModuleId)? true : false
            }
        });
        
    },
    descriptionRenderer :function(val, meta, rec, row, col, store) {//ERP-13792 [SJ]
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
        this.productComboStore.on('beforeload',function(s,o){
            if(!o.params)o.params={};
            var currentBaseParams = this.productComboStore.baseParams;
            currentBaseParams.searchProductString = (this.productOptimizedFlag==Wtf.Products_on_Submit && !this.isProductLoad)? this.productId.getValue():"";
            currentBaseParams.isFreeTextSearching = (this.productOptimizedFlag==Wtf.Products_on_Submit);
            this.productComboStore.baseParams=currentBaseParams; 
        },this);
//
//        this.productComboStore.on('loadexception',function(){
//            alert('loadexception');
//        },this);
        this.productId= new Wtf.form.TextField({
            name:'pid'
        });
        if(this.productOptimizedFlag== undefined || this.productOptimizedFlag==Wtf.Show_all_Products){    
             this.productComboStore.load();
        
        this.productEditor=new Wtf.form.ExtFnComboBox({
            name:(this.isLeaseFixedAsset)? 'pid' : 'productname',
            store:this.productComboStore,
            typeAhead: true,
            selectOnFocus:true,
            maxHeight:250,
            listAlign:"bl-tl?",//[ERP-5149] To expand list of combobox always on top. 
            valueField:'productid',
            displayField:(this.isLeaseFixedAsset)? 'pid' : 'productname',
            extraFields:(this.isLeaseFixedAsset)? ['productname','type']:['pid','type'],
            extraComparisionField:'pid',// type ahead search on acccode as well.
            extraComparisionField:'pid',// type ahead search on pid as well.
            listWidth:400,
            scope:this,
            hirarchical:true,
            forceSelection:true,
            isProductCombo:true
        });
        }else{
            this.productEditor=new Wtf.form.ExtFnComboBox({
                name:(this.isLeaseFixedAsset)? 'pid' : 'productname',
                store:this.productComboStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
                typeAhead: true,
                isProductCombo: true,
                selectOnFocus:true,
                maxHeight:250,
                lastQuery: '',
                listAlign:"bl-tl?",//[ERP-5149] To expand list of combobox always on top. 
                valueField:'productid',
                displayField:(this.isLeaseFixedAsset)? 'pid' : 'productname',
                extraFields:['pid','productname','type'],
                extraComparisionField:'pid',// type ahead search on acccode as well.
                listWidth:400,
                scope:this,
                hirarchical:true,
                mode:'remote',
                hideTrigger:true,
                triggerAction : 'all',
                editable : true,
                minChars : 2,
                hideAddButton : true,//Added this Flag to hide AddNew  Button  
                addNewFn:this.openProductWindow.createDelegate(this),
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
        
        this.remark= new Wtf.form.TextField({
            name:'remark'
//            readOnly:true
        });
        
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
        
        this.taxStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.taxRec),
    //        url: Wtf.req.account + 'CompanyManager.jsp',
            url : "ACCTax/getTax.do",
            baseParams:{
                mode:33,
                includeDeactivatedTax: this.isEdit!=undefined? this.isEdit : false
            }
        });
        
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
            maskRe: /[0-9]+(\.[0-9]+)?$/,
            //decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
             allowDecimals:false
        });
        
        this.deliQuantity=new Wtf.form.NumberField({
            allowBlank: false,            
            defaultValue:0,
            allowNegative: false,
            maxLength:10,
            maskRe: /[0-9]+(\.[0-9]+)?$/,
            //decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
            allowDecimals:false
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
    addTax: function () {
        this.stopEditing();
        var p = callTax("taxwin");
        Wtf.getCmp("taxwin").on('update', function () {
            this.taxStore.reload();
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
         columnArr.push({
            header:WtfGlobal.getLocaleText("acc.invoice.lineItemSequence"),//"Sequence",
            width:65,
            align:'center',
            //dataIndex:'srno',
            name:'srno',
            renderer: Wtf.applySequenceRenderer
        });
        if(this.productOptimizedFlag!=Wtf.Products_on_Submit){
            columnArr.push({
                header: (this.isLeaseFixedAsset)? WtfGlobal.getLocaleText("acc.product.gridProductID") : WtfGlobal.getLocaleText("acc.invoice.gridAssetGroup"), // "Produt ID" : "Asset Group",
                width:200,
                dataIndex:(this.isLeaseFixedAsset)? (this.readOnly||this.productOptimizedFlag==Wtf.Products_on_type_ahead?'pid':'productid') : (this.readOnly?'productname':'productid'),
                editor:(this.isNote||this.readOnly)?"":this.productEditor,
                renderer:(this.productOptimizedFlag==Wtf.Products_on_type_ahead)?(this.readOnly?"":this.getComboNameRenderer(this.productEditor)):(this.readOnly?"":Wtf.comboBoxRenderer(this.productEditor))
            });
        } else {
            columnArr.push({
                header: (this.isLeaseFixedAsset)? WtfGlobal.getLocaleText("acc.product.gridProductID") : WtfGlobal.getLocaleText("acc.invoice.gridAssetGroup"), // "Produt ID" : "Asset Group",
                width:200,
                dataIndex: 'pid',
                editor:(this.isNote||this.readOnly||this.productOptimizedFlag!=Wtf.Products_on_Submit)?"":this.productId
            });
        }
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"), // "Product Name",
            dataIndex: 'productname',
            hidden: this.isFixedAsset
        },{
            header:this.isCN?WtfGlobal.getLocaleText("acc.invoice.gridInvNo"):WtfGlobal.getLocaleText("acc.invoice.gridVenInvNo"),//"Invoice No.":"Vendor Invoice No.",
            width:150,
            dataIndex:this.noteTemp?'transectionno':'billno',
            hidden:!this.isNote
        },{
             header:WtfGlobal.getLocaleText("acc.do.partno"),//"Part No",
             dataIndex:"partno",
             width:250,
             editor:this.readOnly?"":this.partno
         },{
             header: this.isLeaseFixedAsset? WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc") : WtfGlobal.getLocaleText("erp.field.AssetDesciption"), // "Description",
             dataIndex:"description",
             hidden:this.isNote,
             width:250,
             editor:(this.isNote||this.readOnly)?"":this.remark,
             renderer:this.descriptionRenderer    //ERP-13792 [SJ]
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
             editor:this.readOnly?"":this.actQuantity,
             renderer:this.quantityRenderer
//             renderer:this.storeRenderer(this.productComboStore,"productid","uomname")
        },{
             header:WtfGlobal.getLocaleText("acc.accPref.returnQuant"),
             dataIndex:"dquantity",
             align:'right',
             width:100,
             editor:this.readOnly?"":this.deliQuantity,
             renderer:this.quantityRenderer
//             renderer:this.storeRenderer(this.productComboStore,"productid","uomname")
         },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridUOM"),//"UOM",
            width:100,
            hidden:this.isLeaseFixedAsset,
            dataIndex:this.readOnly?'uomname':'uomid',
            renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.uomEditor),
            editor:(this.isNote||this.readOnly)?"":this.uomEditor
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridRateToBase"),//Base UOM Rate
             dataIndex:"baseuomrate",
             align:'left',
             width:100,
             renderer:this.conversionFactorRenderer(this.productComboStore,"productid","uomname"),
             editor:(this.isNote||this.readOnly)?"":this.transBaseuomrate
         },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridQtyInBase"),//Base UOM Quantity
             dataIndex:"baseuomquantity",
             align:'right',
             width:50,
             renderer:this.storeRenderer(this.productComboStore,"productid","uomname")
//             editor:(this.isNote||this.readOnly)?"":this.transQuantity
         },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice"), // "Unit Price",
            dataIndex: "rate",
            align:'right',
            width:150,
            renderer:WtfGlobal.withCurrencyUnitPriceRenderer,
            editor:(this.isNote||this.readOnly) ? "" : this.editprice,
            editable:true,
            hidden: !(this.isCustomer?Wtf.account.companyAccountPref.unitPriceInSR:Wtf.account.companyAccountPref.unitPriceInPR)
        },{
            header: WtfGlobal.getLocaleText("acc.field.DiscountType"),
            width:150,
            dataIndex:'discountispercent',
            id:this.id+"discountispercent",
            fixed:true,
            hidden:!(this.isCustomer?Wtf.account.companyAccountPref.unitPriceInSR:Wtf.account.companyAccountPref.unitPriceInPR),
            renderer:Wtf.comboBoxRenderer(this.rowDiscountTypeCmb),
            editor:""
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridDiscount"),//"Discount",
             dataIndex:"prdiscount",
             id:this.id+"prdiscount",
             align:'right',
             fixed:true,
             width:150,
             hidden:!(this.isCustomer?Wtf.account.companyAccountPref.unitPriceInSR:Wtf.account.companyAccountPref.unitPriceInPR),
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
             editor:""
         },{
             header: WtfGlobal.getLocaleText("acc.invoice.proTax"),//"Product Tax",
             dataIndex:"prtaxid",
             id:this.id+"prtaxid",
             fixed:true,
             width:150,
             hidden:!(this.isCustomer?Wtf.account.companyAccountPref.unitPriceInSR:Wtf.account.companyAccountPref.unitPriceInPR),//!(this.isNoteAlso || (Wtf.account.companyAccountPref.countryid == '137' && Wtf.account.companyAccountPref.enableGST)),// this.isNoteAlso flag will be true if you are creating Sale/Purchase Return with Credit/Debit Note also// hide if company is malaysian and GST is not enabled for it
             renderer:Wtf.comboBoxRenderer(this.transTax),
             editor:""  //this.transTax
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridTaxAmount"),//"Tax Amount",
             dataIndex:"taxamount",
             id:this.id+"taxamount",
              fixed:true,
             //align:'right',
             width:150,
             editor:"",//this.transTaxAmount,
             hidden:!(this.isCustomer?Wtf.account.companyAccountPref.unitPriceInSR:Wtf.account.companyAccountPref.unitPriceInPR),//!(this.isNoteAlso || (Wtf.account.companyAccountPref.countryid == '137' && Wtf.account.companyAccountPref.enableGST)),// this.isNoteAlso flag will be true if you are creating Sale/Purchase Return with Credit/Debit Note also// hide if company is malaysian and GST is not enabled for it
             renderer:this.setTaxAmountWithotExchangeRate.createDelegate(this)
            },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridAmount"), // "Amount",
             dataIndex:"amount",
             align:'right',
             width:200,
             renderer:(this.isNote?WtfGlobal.withoutRateCurrencySymbol:this.calAmountWithoutExchangeRate.createDelegate(this)),
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
        if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.invoicegrid.TaxAmount"),//"Total Tax Amount",
                dataIndex:"recTermAmount",
                //hidden : this.isRFQ || this.isRequisition ? true : false,
                align:'right',
                width:100,
                renderer: WtfGlobal.withoutRateCurrencySymbol
            },{
                header: WtfGlobal.getLocaleText("acc.invoicegrid.tax"), 
                align: 'center',                
                width: 40,
                dataIndex:"LineTermdetails",
                renderer:this.addRenderer.createDelegate(this),
                //hidden:  this.isRFQ || this.isRequisition ? true : false 
            });
        }
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
    addRenderer: function(v, m, rec) {
        var hideUnitPriceAmount = this.isCustomer ? !Wtf.dispalyUnitPriceAmountInSales : !Wtf.dispalyUnitPriceAmountInPurchase;
        if (this.isModuleForAvalara) {
            return getToolTipOfAvalaraTerms(v, m, rec, hideUnitPriceAmount);
        } else {
            return getToolTipOfTermsfun(v, m, rec, hideUnitPriceAmount);
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
      return (val=="NaN"?0:val); //).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)
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
                //when creating asset Purchase Return with multiple Asset Group then productid found both 'productComboStore' and 'store' in type ahead setting in System pref.
                if(productComboRecIndex==-1){
                    productComboRecIndex=WtfGlobal.searchRecordIndex(store, productid, 'productid');
                    var proRecord = store.getAt(productComboRecIndex);
                } else if(productComboRecIndex >=0){
                    var proRecord = this.productComboStore.getAt(productComboRecIndex);
                }
                if (proRecord.get('isAsset') && record.get("quantity") != 0){
                    this.callFixedAssetDetailsWindow(record, proRecord, this.readOnly);
                }
                
            }

        } else if(e.getTarget(".serialNo-gridrow")){
            var store=grid.getStore();
            var record = store.getAt(rowindex);
            var productid = record.get('productid');
            var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, productid, 'productid');
            if (productComboRecIndex == -1) {
                productComboRecIndex = WtfGlobal.searchRecordIndex(store, productid, 'productid');
            }
            if (productComboRecIndex >= 0) {
                var proRecord = this.productComboStore.getAt(productComboRecIndex);
                var recIndex = WtfGlobal.searchRecordIndex(this.productComboStore, productid, 'productid');
                if (recIndex == -1) {
                    proRecord = this.getStore().getAt(productComboRecIndex);
                }
                if(proRecord.data.type!='Service' || proRecord.data.type!='Non-Inventory Part'){
                   if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.account.companyAccountPref.isLocationCompulsory || Wtf.account.companyAccountPref.isWarehouseCompulsory  || Wtf.account.companyAccountPref.isRowCompulsory || Wtf.account.companyAccountPref.isRackCompulsory || Wtf.account.companyAccountPref.isBinCompulsory){ //if company level option is on then only check batch and serial details
                        if(proRecord.data.isLocationForProduct || proRecord.data.isWarehouseForProduct || proRecord.data.isBatchForProduct || proRecord.data.isSerialForProduct || proRecord.data.isRowForProduct || proRecord.data.isRackForProduct  || proRecord.data.isBinForProduct) {
                            this.callSerialNoWindow(record);
                        }else{
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),this.isFixedAsset?WtfGlobal.getLocaleText("acc.batchserial.FunctinalityAsset"):WtfGlobal.getLocaleText("acc.batchserial.Functinality")],2);   //Batch and serial no details are not valid.
                            return;
                        }
                 }
                }

            }
        }
    },  
    callSerialNoWindow:function(obj){
        var index=this.productComboStore.findBy(function(rec){
            if(rec.data.productid==obj.data.productid)
                return true;
            else
                return false;
        })
        var firstRow = index;
        if (index == -1) {
            index = this.store.findBy(function (rec) {
                if (rec.data.productid == obj.data.productid)
                    return true;
                else
                    return false;
            })
        }
        if(index!=-1){
        var prorec=this.productComboStore.getAt(index); 
        if (firstRow == -1) {
                prorec = this.store.getAt(index);
            }
            if (prorec == undefined) {
                prorec = obj;
            }
        this.batchDetailswin=new Wtf.account.SerialNoWindow({
            renderTo: document.body,
            title:WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber"),
            productName:prorec.data.productname,
            quantity:obj.data.baseuomquantity,
	    defaultLocation:prorec.data.location,
            productid:prorec.data.productid,
            transactionType:(this.isCustomer)?3:2,
            isSales:true,
            fromPO:this.fromPO,
            defaultWarehouse:prorec.data.warehouse,
            batchDetails:obj.data.batchdetails,
            transactionid:(this.isCustomer)?4:5,
            isLocationForProduct:prorec.data.isLocationForProduct,
            isWarehouseForProduct:prorec.data.isWarehouseForProduct,
            isRowForProduct:prorec.data.isRowForProduct,
            isRackForProduct:prorec.data.isRackForProduct,
            isBinForProduct:prorec.data.isBinForProduct,
            isBatchForProduct:prorec.data.isBatchForProduct,
            isSerialForProduct:prorec.data.isSerialForProduct,
            warrantyperiod:prorec.data.warrantyperiod,
            linkflag:obj.data.linkflag,
            warrantyperiodsal:prorec.data.warrantyperiodsal,  
            isEdit:this.isEdit,
            copyTrans:this.copyTrans,
            width:950,
            readOnly:this.readOnly,
            height:400,
            resizable : false,
            modal : true,
            lineRec:obj,
            parentGrid:this,
            moduleid:this.moduleid
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
                  var productuomid = "";
                  if(productComboIndex >=0){
                      prorec = this.priceStore.getAt(productComboIndex);
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
                  var productComboIndex = WtfGlobal.searchRecordIndex(this.priceStore, obj.record.get('productid'), 'productid');
                  var productuomid = "";
                  if(productComboIndex >=0){
                      prorec = this.priceStore.getAt(productComboIndex);
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
            if(obj.field=="productid" || obj.field=="pid" ){
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
                    url:"ACCProduct/getIndividualProductPrice.do",
                    params:{
                        productid:this.productOptimizedFlag==Wtf.Products_on_Submit?productid:obj.value,
                        affecteduser: this.affecteduser,
                        currency: this.parentObj.Currency.getValue(),
                        quantity: obj.record.data.dquantity,
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
                        /*
                         * set band of customer on product selection
                         */
                    obj.record.set("pricingbandmasterid", this.pricingbandmasterid);
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
                        productsuppliernumber= prorec.data.supplierpartnumber;
                        shelfLocation = prorec.data.shelfLocation;
                        protaxcode = prorec.data[acctaxcode];
                    }
                    obj.record.set("description",proddescription);
                    obj.record.set("uomid", productuomid);
                    obj.record.set("supplierpartnumber",productsuppliernumber);
                    obj.record.set("shelfLocation",shelfLocation);
                    obj.record.set("productname", productname);
                    
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
                    } else {
                        if(datewiseprice==0){
                            if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.addprice) && !this.isRequisition){//permissions
                                rec.set("productname",productname);
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
                            
                            if (this.isPriceFromUseDiscount) {
                                if (obj.record.data.dquantity != "") {
                                    obj.record.set("rate", this.defaultPrice);
                                    obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSourceUseDiscount);
                                } else {
                                    obj.record.set("rate", "");
                                    obj.record.set("priceSource", "");
                                }
                            } else {
                                obj.record.set("rate", modifiedRate);
                            }
                        }
                    }
                    this.fireEvent('datachanged',this);
                }, function(){
                });
            }else if(obj.field=="quantity"){
                rec=obj.record;               
                //if((rec.data.isNewRecord=="" || rec.data.isNewRecord==undefined) && this.fromOrder&&!(this.editTransaction||this.copyInv)) {
                if(((rec.data.isNewRecord=="" || rec.data.isNewRecord==undefined) && this.fromOrder&&!(this.editTransaction||this.copyInv))||(this.isEdit && rec.data.linkid !="")) {  
                   if(obj.value >rec.data.copyquantity){  
                        var msg=this.isCustomer?WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinSRisexceeds"):WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinPRisexceeds");
                        obj.record.set(obj.field, obj.originalValue);
                        
                         Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.common.alert"),msg,{
                                
                        },this)
                   }else if(obj.value!=rec.data.copyquantity) {
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"),(this.isCustomer)?WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinSRisdifferent"):WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinPRisdifferent"),function(btn){
                            if(btn!="yes") {
                                obj.record.set(obj.field, obj.originalValue);
                                obj.record.set("baseuomquantity",obj.originalValue*obj.record.get("baseuomrate"));
                                }else{
                                    obj.record.set("dquantity", obj.value);
                                    obj.record.set("baseuomquantity", obj.record.get("quantity")*obj.record.get("baseuomrate"));
                                    rec.set("changedQuantity",(rec.data.copyquantity-obj.value)*((rec.data.baseuomrate===""||rec.data.baseuomrate==undefined)?1:rec.data.baseuomrate));
                                }
                                var taxamount = this.setTaxAmountAfterSelection(obj.record);
                                obj.record.set("taxamount",taxamount);
                                this.fireEvent('datachanged',this);
                        },this)
                    }
                }
                if((obj.record.data["quantity"])==0){
                    this.store.remove(obj.record);
                }
                  var productComboIndex = WtfGlobal.searchRecordIndex(this.priceStore, obj.record.get('productid'), 'productid');
                  var productuomid = "";
                  if(productComboIndex >=0){
                      prorec = this.priceStore.getAt(productComboIndex);
                      productuomid = prorec.data.uomid;
                      if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                            obj.record.set("baseuomquantity", obj.record.get("quantity")*obj.record.get("baseuomrate"));
                      } else {
                          obj.record.set("baseuomrate", 1);
                          obj.record.set("baseuomquantity", obj.record.get("quantity")*obj.record.get("baseuomrate"));
                      }
                  }
              
//                    obj.record.set("invstore", this.batchDetailswin.defaultLocation);
//                    obj.record.set("invlocation", this.batchDetailswin.defaultWarehouse);
//                
            } else if(obj.field=="dquantity" || obj.field=="pricingbandmasterid"){
                rec=obj.record;
                
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
                        this.isVolumeDisocuntExist = response.data[0].isVolumeDisocuntExist;
                        
                        /*
                         * set band of customer on product selection
                         */
                        obj.record.set("pricingbandmasterid", this.pricingbandmasterid);
                        if (this.isVolumeDisocunt) {
                            obj.record.set("rate", this.defaultPrice);
                            obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSource);
                        } else if (this.isPriceListBand) {
                            obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                            obj.record.set("rate", this.defaultPrice);
                            if (this.isPriceFromUseDiscount) {
                                if (obj.record.data.dquantity != "") {
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
                        }
                        this.fireEvent('datachanged',this);
                    }, function(response) {
                        
                    });
                }
                
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
                
                if(this.isFixedAsset || this.isLeaseFixedAsset){
                    var productid = rec.get('productid');
                    var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, productid, 'productid');
                    if(productComboRecIndex >=0){
                        var proRecord = this.productComboStore.getAt(productComboRecIndex);
                        if(proRecord.get('isAsset') && obj.record.get("dquantity") != 0)
                            this.callFixedAssetDetailsWindow(obj.record, proRecord);
                    }
                }
                var productComboIndex = WtfGlobal.searchRecordIndex(this.priceStore, obj.record.get('productid'), 'productid');
                var productuomid = "";
                if (productComboIndex >= 0) {
                    prorec = this.priceStore.getAt(productComboIndex);
                    productuomid = prorec.data.uomid;
                    if (obj.record.get("uomid") != undefined && productuomid != obj.record.get("uomid")) {
                        obj.record.set("baseuomquantity", obj.record.get("dquantity") * obj.record.get("baseuomrate"));
                    } else {
                        obj.record.set("baseuomrate", 1);
                        obj.record.set("baseuomquantity", obj.record.get("dquantity") * obj.record.get("baseuomrate"));
                    }
                }

            }
            
            if(obj.field=="prtaxid" || obj.field=="rate" || obj.field=="dquantity" || obj.field=="quantity" || obj.field=="discountispercent" || obj.field=="prdiscount"){
                var taxamount = this.setTaxAmountAfterSelection(obj.record);
                obj.record.set("taxamount",taxamount);
                this.fireEvent('datachanged',this);
            }
        }
         if (WtfGlobal.isIndiaCountryAndGSTApplied() && rec.data['LineTermdetails'] != undefined && rec.data['LineTermdetails'] != "") {
            var termStore = this.getTaxJsonOfIndia(rec);
            rec.set('uncheckedTermdetails', rec.data['uncheckedTermdetails']);
            this.getColumnModel().setRenderer(this.getColumnModel().findColumnIndex("amount"), this.calAmountWithoutExchangeRate.createDelegate(this));
            termStore = this.calculateTermLevelTaxes(termStore, rec, undefined, true);
            rec.set('LineTermdetails', Wtf.encode(termStore));                       
            updateTermDetails(this);
        }
        this.fireEvent('datachanged',this);
        if(this.store.getCount()>0&&this.store.getAt(this.store.getCount()-1).data['productid'].length<=0)
            return;
        
            this.addBlankRow();
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
        return taxamount;
        
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
    calAmountWithoutExchangeRate:function(v,m,rec){
        
        
        var rate=getRoundofValueWithValues(rec.data.rate,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        var quantity=getRoundofValue(rec.data.dquantity);
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
        

        var origionalAmount = getRoundedAmountValue(rate*quantity) ;
//        if(rec.data.partamount != 0){
//            origionalAmount = origionalAmount * (rec.data.partamount/100);
//        }
        
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
        if (!WtfGlobal.isIndiaCountryAndGSTApplied()) {
            val = parseFloat(val) + parseFloat(taxamount);
        }

        rec.set("amount",(parseFloat(getRoundedAmountValue(val)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1));
//        if(this.isQuotationFromPR && val!==0 && (rec.data.orignalamount==undefined || rec.data.orignalamount==""))
//        rec.set("orignalamount",val);
        return WtfGlobal.withoutRateCurrencySymbol(val,m,rec);
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
            fromPO: (this.moduleid == Wtf.Acc_FixedAssets_Purchase_Return_ModuleId)? true : this.fromPO,
            isFixedAssetPR: (this.moduleid == Wtf.Acc_FixedAssets_Purchase_Return_ModuleId)? true : false,
            isFixedAssetSR: (this.moduleid == Wtf.Acc_FixedAssets_Sales_Return_ModuleId)? true : false,
            isEdit:this.isEdit,
            copyTrans:this.copyTrans,
            isFixedAsset:this.isFixedAsset,
            isFromOrder:true,
            isFromSalesReturn:true,
            moduleid:this.moduleid,
            readOnly:this.readOnly,
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
        var quantity = rec.data.dquantity;
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
        var productRate= (rec.data.rate == 'NaN' || rec.data.rate == undefined || rec.data.rate ==null)?0:rec.data.rate;
        var origionalAmount = productRate * quantity;
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
                } else if(!prorec.data.multiuom){
//                          WtfComMsgBox(["Warning","Multi UOM not allowed for the selected product. "], 2);
                    return false;
                }
            }
        } else if(obj.field=="productid"){
            var index=this.productComboStore.findBy(function(rec){
                if(rec.data.productid==obj.value)
                    return true;
                else
                    return false;
            })
            prorec=this.productComboStore.getAt(index);
            index=this.priceStore.find('productid',obj.value)
            rec=this.priceStore.getAt(index);
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
        if (this.getView()) {
            this.getView().refresh();
        }
    },
     
    addBlank:function(){       
        this.addBlankRow();
    },            

    loadPOGridStore:function(rec,linkingFlag, FA_DOGRlinkFlag){        
        this.store.load({params:{bills:rec,mode:43,closeflag:true,doflag:true,linkingFlag:linkingFlag,isFixedAsset: this.isFixedAsset,isLeaseFixedAsset:this.isLeaseFixedAsset,FA_DOGRlinkFlag:FA_DOGRlinkFlag}});
        this.store.on('load',function(rec){
            this.store.each(function(rec){
                if(rec.data.dquantity==""){
                    rec.data.dquantity=rec.data.quantity;
                }
            });
            if ((WtfGlobal.isIndiaCountryAndGSTApplied())) {
                    var rowAmountIndex = this.getColumnModel().findColumnIndex("amount");
                    if (rec.get('LineTermdetails') != undefined && rec.get('LineTermdetails') != '') {
                        var termStore = this.getTaxJsonOfIndia(rec);
                        if (this.parentObj.includingGST && this.parentObj.includingGST.getValue() == true) {
                            this.getColumnModel().setRenderer(rowAmountIndex, WtfGlobal.withoutRateCurrencySymbol);
                            termStore = this.calculateTermLevelTaxesInclusive(termStore, rec);
                        } else {
                            this.getColumnModel().setRenderer(rowAmountIndex, this.calAmountWithoutExchangeRate.createDelegate(this));
                            termStore = this.calculateTermLevelTaxes(termStore, record);
                        }                        
                        rec.set('LineTermdetails', JSON.stringify(termStore));
                      
//                        record.set('LineTermdetails', termStore);
                        updateTermDetails(this);
                    }
//
                }                                    
        },this);        
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
        if(termJson.taxtype == 0){ // If Flat
                taxamount= this.TaxCalculation(termJson,rec,termJson.taxvalue,assessablevalue);              
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
        }else {
                if (Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                    finaltaxamount += getRoundedAmountValue(taxamount);
                }
            }
            if (isCESSApplicable) {
                finaltermStore.push(termJson);
            }
        }
        if (finaltaxamount >= 0) {
            rec.set('recTermAmount', getRoundedAmountValue(finaltaxamount));
            rec.set('taxamount', getRoundedAmountValue(finaltaxamount));
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
    TaxCalculation: function (termJson, rec, taxamount, assessablevalue) {
        if (Wtf.isExciseApplicable && termJson.termtype == Wtf.term.Excise) { // for special case excise duty | termtype=2 for Excise
            if (rec.data.valuationType == Wtf.excise.QUANTITY && termJson.taxtype == 0) { // if valuation type is quentity than calculation on flat_rate*quentity
                if (!Wtf.isEmpty(rec.data.quantityInReportingUOM)) {
                    taxamount = termJson.taxvalue * rec.data.quantityInReportingUOM;
                } else {
                    taxamount = 0;
                }
            } else if (rec.data.valuationType == Wtf.excise.MRP && termJson.taxtype == 1) { //taxtype=1 for percentage | if valuation type is MRP than calculation on (Assessable Amount* TAX)/100
                taxamount = (assessablevalue * termJson.taxvalue) / 100
            }
        }
        if (Wtf.account.companyAccountPref.enablevatcst && termJson.termtype == Wtf.term.VAT) { // for special case VAT | termtype=1 for VAT
            if (rec.data.valuationTypeVAT == Wtf.excise.QUANTITY && termJson.taxtype == 0) { // if valuation type is quentity than calculation on flat_rate*quentity
                if (!Wtf.isEmpty(rec.data.quantityInReportingUOMVAT)) {
                    taxamount = termJson.taxvalue * rec.data.quantityInReportingUOMVAT;
                } else {
                    taxamount = 0;
                }
            } else if (rec.data.valuationTypeVAT == Wtf.excise.MRP && termJson.taxtype == 1) { //taxtype=1 for percentage | if valuation type is MRP than calculation on (Assessable Amount* TAX)/100
                taxamount = (assessablevalue * termJson.taxvalue) / 100
            }
        }
        if (Wtf.account.companyAccountPref.enablevatcst && termJson.termtype == Wtf.term.CST) { // If VAT is on MRP than CST also on MRP
            if (rec.data.valuationTypeVAT == Wtf.excise.MRP && termJson.taxtype == 1) { //taxtype=1 for percentage | if valuation type is MRP than calculation on (Assessable Amount* TAX)/100
                taxamount = (assessablevalue * termJson.taxvalue) / 100
            }
        }

        var opmod = termJson.sign == 0 ? -1 : 1;
        taxamount = opmod * taxamount;
        return taxamount;
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
//        record.set("rate", 1000);
        return displayField;
    }
}
});
