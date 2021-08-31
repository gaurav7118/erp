

Wtf.account.CreditNoteDebitNoteGrid=function(config){
    this.isCustomer=config.isCustomer;
    this.isNoteAlso=(config.isNoteAlso)?config.isNoteAlso:false;
    this.isCnDnForInvoice=(config.isCnDnForInvoice!=undefined)?config.isCnDnForInvoice:false;
    this.productComboStore=this.isCustomer?Wtf.productStoreSales:Wtf.productStore;
    this.currencyid=config.currencyid;
    this.productID=null;
    this.id=config.id;
    this.record=config.record;
    this.billDate=new Date();
    this.dateChange=false;
    this.pronamearr=[];
    this.fromPO=config.fromPO;
    this.readOnly=config.readOnly;
    this.editTransaction=config.editTransaction;
    this.editLinkedTransactionQuantity= Wtf.account.companyAccountPref.editLinkedTransactionQuantity;
    this.editLinkedTransactionPrice= Wtf.account.companyAccountPref.editLinkedTransactionPrice;
    this.UomSchemaType=Wtf.account.companyAccountPref.UomSchemaType;
    this.isEdit=config.isEdit;
    this.copyTrans=config.copyTrans;
    this.noteTemp=config.noteTemp;
    this.fromOrder=config.fromOrder;
    this.moduleid = config.moduleid;
    this.forCurrency="";
    this.CUSTOM_KEY = "customfield";
    this.createStore();
    this.createComboEditor();
    this.createColumnModel();
    this.loadPriceStore();
    this.heplmodeid = config.heplmodeid;
    this.parentid=config.parentid;
    var colModelArray = [];
    colModelArray = GlobalColumnModel[this.moduleid];
    WtfGlobal.updateStoreConfig(colModelArray,this.store);
    Wtf.account.CreditNoteDebitNoteGrid.superclass.constructor.call(this,config);
    this.addEvents({
        'datachanged':true,
        'pricestoreload':true
    });
}
Wtf.extend(Wtf.account.CreditNoteDebitNoteGrid,Wtf.grid.EditorGridPanel,{
    clicksToEdit:1,
    stripeRows :true,
    rate:1,
    symbol:null,
    layout:'fit',
    viewConfig:{forceFit:true},
    forceFit:true,
    loadMask:true,
    onRender:function(config){
         Wtf.account.CreditNoteDebitNoteGrid.superclass.onRender.call(this,config);
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
         this.hideShowCustomizeLineFields();
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
            {name:'currencysymbol',defValue:this.symbol}
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
            {name:'quantity'},
            {name:'dquantity'},
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
            {name: 'changedQuantity'},
            {name:'producttype'},
            {name:'permit'},
            {name:'linkto'},
            {name:'batchdetails'},
            {name:'linkid'},
            {name:'linktype'},
            {name:'customfield'},
            {name:'rate'},
            {name:'amount',defValue:0},
            {name:'amounttoadjust',defValue:0},
            {name:'taxamounttoadjust'},
            {name:'adjustedamount',defValue:0},
            {name:'linkflag'},
            {name:'prtaxid'},
            {name:'taxamount'},
            {name:'reason'},
            {name: 'customer'}, // added in record due to set auto populate value of customer in add price in master window
            {name: 'vendor'} // added in record due to set auto populate value of vendor in add price in master window
        ]);
        var url=Wtf.req.account+((this.fromOrder||this.readOnly)?((this.isCustomer)?'CustomerManager.jsp':'VendorManager.jsp'):((this.isCN)?'CustomerManager.jsp':'VendorManager.jsp'));
        if(this.fromOrder)
           url=Wtf.req.account+(this.isCustomer?'CustomerManager.jsp':'VendorManager.jsp');
        this.store = new Wtf.data.Store({
            url:url,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.storeRec),
            baseParams : {
                srflag : true
            }
        });
        
    },
    
    createComboEditor:function(){

        this.productEditor=new Wtf.form.ExtFnComboBox({
            name:'productname',
            store:this.productComboStore,
            typeAhead: true,
            selectOnFocus:true,
            maxHeight:250,
            listAlign:"bl-tl?",//[ERP-5149] To expand list of combobox always on top. 
            valueField:'productid',
            displayField:'productname',
            extraFields:['pid','type'],
            listWidth:400,
            scope:this,
            hirarchical:true,
            forceSelection:true
        });
        //if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.edit))
            this.productEditor.addNewFn=this.openProductWindow.createDelegate(this);

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
            addNoneRecord: true,
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
                includeDeactivatedTax: this.isEdit != undefined ? (this.copyTrans ? false : this.isEdit) : false
           }
        });
        
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
//        if(!WtfGlobal.EnableDisable(Wtf.UPerm.tax, Wtf.Perm.tax.edit))
//            this.transTax.addNewFn=this.addTax.createDelegate(this);
        
        this.transTaxAmount=new Wtf.form.NumberField({
            allowBlank: true,
            allowNegative: false,
            defaultValue:0,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
        });
        this.amountToAdjust=new Wtf.form.NumberField({
            allowBlank: true,            
            defaultValue:0,
            allowNegative: false,
            maxLength:10,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
        });
        
        this.inventoryStores = new Wtf.form.ComboBox({
            store: Wtf.inventoryStore,
            name:'storeid',
            displayField:'storedescription',
            valueField:'storeid',
            mode: 'local',
//            fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.12"),
            valueField:'id',
            displayField:'name',
            store:Wtf.inventoryStore,
            anchor:'90%',
            typeAhead: true,
            forceSelection: true,
            name:'location',
            hiddenName:'location'
        });
//       chkinventoryWarehouse();
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
//      chkinventoryLocation();
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
            maxLength:10,
            decimalPrecision: Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT
        });
        this.editprice = new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength:14,
            decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
        });
    },
    showUom:function(){
       callUOM('uomReportWin');
       Wtf.getCmp('uomReportWin').on('update', function(){
           Wtf.uomStore.reload();
       }, this);
    },
    
    addReason:function(){
        addMasterItemWindow('29');
        Wtf.getCmp('masterconfiguration').on('update', function(){Wtf.reasonStore.reload();}, this);
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
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridProduct"),//"Product",
            width:200,
            dataIndex:this.readOnly?'productname':'productid',
            renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.productEditor),
            editor:(this.isNote||this.readOnly)?"":this.productEditor
        },{
            header:this.isCN?WtfGlobal.getLocaleText("acc.invoice.gridInvNo"):WtfGlobal.getLocaleText("acc.invoice.gridVenInvNo"),//"Invoice No.":"Vendor Invoice No.",
            width:150,
            dataIndex:this.noteTemp?'transectionno':'billno',
            hidden:!this.isNote
        },{
             header:WtfGlobal.getLocaleText("acc.do.partno"),//"Part No",
             dataIndex:"partno",
             width:250,
             editor:this.partno,
             hidden:this.isCnDnForInvoice
         },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridDescription"),//"Description",
             dataIndex:"description",
             hidden:this.isNote,
             width:250,
             editor:(this.isNote||this.readOnly)?"":this.remark
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
             header:WtfGlobal.getLocaleText("acc.masterConfig.12"),   //location
             dataIndex:'invlocation',
              hidden:true,
//             hidden:!(Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel),                 
             width:150,
             renderer:Wtf.comboBoxRenderer(this.inventoryLocation),
             editor:(this.readOnly)?"":this.inventoryLocation
         },{
             header:WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),  //warehouse
             dataIndex:'invstore',
             hidden:true,
//             hidden:!(Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel),                 
             width:150,
             renderer:Wtf.comboBoxRenderer(this.inventoryStores),
             editor:(this.readOnly)?"":this.inventoryStores
         },{
             header:WtfGlobal.getLocaleText("acc.field.ActualQuantity"),
             dataIndex:"quantity",             
             align:'right',
             width:200,
             editor:this.actQuantity,
             renderer:this.quantityRenderer
//             renderer:this.storeRenderer(this.productComboStore,"productid","uomname")
        },{
             header:WtfGlobal.getLocaleText("acc.accPref.returnQuant"),
             dataIndex:"dquantity",
             align:'right',
             width:150,
             editor:this.deliQuantity,
             renderer:this.quantityRenderer,
             hidden:this.isCnDnForInvoice
//             renderer:this.storeRenderer(this.productComboStore,"productid","uomname")
         },{
             header: '',
             align:'center',
             renderer: this.serialRenderer.createDelegate(this),
              hidden:this.isCnDnForInvoice,
             width:40
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridUOM"),//"UOM",
            width:150,
            dataIndex:this.readOnly?'uomname':'uomid',
            renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.uomEditor),
            editor:(this.isNote||this.readOnly||this.UomSchemaType==Wtf.PackegingSchema)?"":this.uomEditor
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridRateToBase"),//Base UOM Rate
             dataIndex:"baseuomrate",
             align:'left',
             width:150,
             renderer:this.conversionFactorRenderer(this.productComboStore,"productid","uomname"),
             editor:(this.isNote||this.readOnly||this.UomSchemaType==Wtf.PackegingSchema)?"":this.transBaseuomrate
         },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridQtyInBase"),//Base UOM Quantity
             dataIndex:"baseuomquantity",
             align:'right',
             width:150,
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
            hidden: false//!Wtf.account.companyAccountPref.unitPriceConfiguration
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.proTax"),//"Product Tax",
             dataIndex:"prtaxid",
             id:this.id+"prtaxid",
             fixed:true,
             width:150,
             hidden:!this.isNoteAlso,
             renderer:Wtf.comboBoxRenderer(this.transTax),
             editor:""//this.readOnly?"":this.transTax  //this.transTax
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridTaxAmount"),//"Tax Amount",
             dataIndex:"taxamount",
             id:this.id+"taxamount",
              fixed:true,
             //align:'right',
             width:150,
             editor:"",//this.transTaxAmount,
             hidden:!this.isNoteAlso,
             renderer:this.setTaxAmountWithotExchangeRate.createDelegate(this)
            },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridAmount"), // "Amount",
             dataIndex:"amount",
             align:'right',
             width:200,
             renderer:(this.isNote?WtfGlobal.withoutRateCurrencySymbol:this.calAmountWithoutExchangeRate.createDelegate(this)),
             hidden: false//!Wtf.account.companyAccountPref.unitPriceConfiguration
            },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridAmountadusted"), // "Amount to adjust",
             dataIndex:"amounttoadjust",
             align:'right',
             width:200,
             editor:this.amountToAdjust,
//             renderer:(this.isNote||this.readOnly?WtfGlobal.withoutRateCurrencySymbol:this.calAmountWithoutExchangeRate.createDelegate(this)),
             hidden: false//!Wtf.account.companyAccountPref.unitPriceConfiguration
            },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridTaxAmountadusted"),//"Tax Amount adjusted",
             dataIndex:"taxamounttoadjust",
             id:this.id+"taxamount1",
              fixed:true,
             //align:'right',
             width:150,
             editor:"",//this.transTaxAmount,
//             hidden:!this.isNoteAlso,
             renderer: WtfGlobal.withoutRateCurrencySymbol
            },{
             header: "Adjusted Amount", // "Amount",
             dataIndex:"adjustedamount",
             align:'right',
             width:200,
             hidden: false//!Wtf.account.companyAccountPref.unitPriceConfiguration
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
                editor:this.Description=new Wtf.form.TextArea({
                    maxLength:200,
                    allowBlank: false,
                    xtype:'textarea'
                })
        },
       {
            header: '',
            align:'center',
            renderer: this.serialRenderer.createDelegate(this),
            hidden:this.isCnDnForInvoice,//(!Wtf.account.companyAccountPref.isBatchCompulsory && !Wtf.account.companyAccountPref.isSerialCompulsory && !Wtf.account.companyAccountPref.isLocationCompulsory && !Wtf.account.companyAccountPref.isWarehouseCompulsory),
            width:40
        });
        if(!this.isNote && !this.readOnly) {
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),//"Action",
                align:'center',
                hidden:this.readOnly,
                width:40,
                renderer: this.deleteRenderer.createDelegate(this)
            });
        }
        this.cm=new Wtf.grid.ColumnModel(columnArr);
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
     serialRenderer:function(v,m,rec){
        return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.serial.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.serial.desc.title")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
    },
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    quantityRenderer:function(val,m,rec){
        if(val == ""){
            return val;
        }else{
            return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        }
    },
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
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
                if(rowindex==total-1){
                    this.addBlankRow();
                }
                this.fireEvent('datachanged',this);
            }, this);
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
            if(productComboRecIndex >=0){
                var proRecord = this.productComboStore.getAt(productComboRecIndex);
                if(proRecord.data.type!='Service' && proRecord.data.type!='Non-Inventory Part'){  //serial no for only inventory type of product
                    if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.account.companyAccountPref.isLocationCompulsory || Wtf.account.companyAccountPref.isWarehouseCompulsory || Wtf.account.companyAccountPref.isRowCompulsory || Wtf.account.companyAccountPref.isRackCompulsory || Wtf.account.companyAccountPref.isBinCompulsory){ //if company level option is on then only check batch and serial details
                        if(proRecord.data.isLocationForProduct || proRecord.data.isWarehouseForProduct || proRecord.data.isBatchForProduct || proRecord.data.isSerialForProduct || proRecord.data.isRowForProduct || proRecord.data.isRackForProduct || proRecord.data.isBinForProduct) 
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
            
        }
    },  
    callSerialNoWindow:function(obj){
        var index=this.productComboStore.findBy(function(rec){
            if(rec.data.productid==obj.data.productid)
                return true;
            else
                return false;
        })
        if(index!=-1){
        var prorec=this.productComboStore.getAt(index); 
        this.batchDetailswin=new Wtf.account.SerialNoWindow({
            renderTo: document.body,
            title:WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber"),
            productName:prorec.data.productname,
            uomName:prorec.data.uomname,
            quantity:(obj.data.baseuomrate)*(obj.data.dquantity),
            billid:obj.data.billid,
	    defaultLocation:prorec.data.location,
            productid:prorec.data.productid,
            transactionType:(this.isCustomer)?3:2,
            transactionid:(this.isCustomer)?3:2,
            isSales:true,
            moduleid:this.moduleid,
            isLocationForProduct:prorec.data.isLocationForProduct,
            isWarehouseForProduct:prorec.data.isWarehouseForProduct,
            isRowForProduct:prorec.data.isRowForProduct,
            isRackForProduct:prorec.data.isRackForProduct,
            isBinForProduct:prorec.data.isBinForProduct,
            defaultWarehouse:prorec.data.warehouse,
            batchDetails:obj.data.batchdetails,
            warrantyperiod:prorec.data.warrantyperiod,
            warrantyperiodsal:prorec.data.warrantyperiodsal,  
            isBatchForProduct:prorec.data.isBatchForProduct,
            isSerialForProduct:prorec.data.isSerialForProduct,
            isIsLocWarehouseForProduct:prorec.data.isIsLocWarehouseForProduct,
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
                    this.reconfigure( this.store, cm);
                } else {
//                    Wtf.Msg.alert('Status', action.msg);
                }
            },function() {
            });
        
        }
    },
    updateRow:function(obj){
        if(obj!=null){
            this.productComboStore.clearFilter(); // Issue 22189
            var rec=obj.record;
            
            var proqty = obj.record.get("quantity");
            proqty = (proqty == "NaN" || proqty == undefined || proqty == null)?0:proqty;
            
            var deliveredproqty = obj.record.get("dquantity");
            deliveredproqty = (deliveredproqty == "NaN" || deliveredproqty == undefined || deliveredproqty == null)?0:deliveredproqty;
            
            
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
                            obj.record.set("baseuomquantity", proqty*obj.value);
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
                            obj.record.set("baseuomquantity", proqty*obj.record.get("baseuomrate"));
                      } else {
                          obj.record.set("baseuomrate", 1);
                          obj.record.set("baseuomquantity", proqty*obj.record.get("baseuomrate"));
                      }
                  }
//                  this.fireEvent('datachanged',this);
            }
            if(obj.field=="productid"){
                rec=obj.record;
                var index=this.priceStore.find('productid',obj.value);
                if(this.isCustomer)
                    rec.set("changedQuantity",(proqty*(-1))*((rec.data.baseuomrate===""||rec.data.baseuomrate==undefined)?1:rec.data.baseuomrate));
                else
                    rec.set("changedQuantity",(proqty)*((rec.data.baseuomrate===""||rec.data.baseuomrate==undefined)?1:rec.data.baseuomrate));
                if(index>=0){
                    rec=this.priceStore.getAt(index);
                    obj.record.set("description",rec.data["desc"]);
                    obj.record.set("prtaxid", "");
                    obj.record.set("taxamount","");
//                    obj.record.set("quantity",1);
                    obj.record.set("baseuomquantity",1);
                    obj.record.set("baseuomrate",1);
                    obj.record.set("uomid", rec.data["uomid"]);
                    obj.record.set("invlocation", rec.data["location"]);
                    obj.record.set("invstore", rec.data["warehouse"]);
                    if(this.isCustomer)
                        obj.record.set("rate",rec.data["salespricedatewise"]);
                    else
                        obj.record.set("rate",rec.data["purchasepricedatewise"]);
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
                    var baseuomRate=1;
                    var prorec = null;
                    var acctaxcode = (this.isCustomer)?"salesacctaxcode":"purchaseacctaxcode";
                    var protaxcode = "";
                    var productLocation = "";
                    var productWarehouse = "";
                    if(productComboIndex >=0){
                        prorec = this.productComboStore.getAt(productComboIndex);
                        productname = prorec.data.productname;
                        proddescription = prorec.data.desc;
                        proddescription = prorec.data.desc;
                        if(this.UomSchemaType==0){//for Schema type
                          productuomid = prorec.data.uomid;
                        }else{//for packeging UOM type
                            productuomid =this.isCustomer? prorec.data.salesuom:prorec.data.purchaseuom;
    //                          productuomname =this.isCustomer? prorec.data.salesuomname:prorec.data.purchaseuomname;
                            baseuomRate=this.isCustomer? prorec.data.stocksalesuomvalue:prorec.data.stockpurchaseuomvalue;
                            if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                                    obj.record.set("baseuomquantity", proqty*(baseuomRate));
                                    obj.record.set("baseuomrate", (baseuomRate));
                            } else {
                                    obj.record.set("baseuomquantity", proqty);
                                    obj.record.set("baseuomrate", 1);
                            }     
                        }
                        productsuppliernumber= prorec.data.supplierpartnumber;
                        shelfLocation = prorec.data.shelfLocation;
                        protaxcode = prorec.data[acctaxcode];
                        productLocation = prorec.data.location;
                        productWarehouse = prorec.data.warehouse;
                    }
                    obj.record.set("desc",proddescription);
                    obj.record.set("uomid", productuomid);
                    obj.record.set("supplierpartnumber",productsuppliernumber);
                    obj.record.set("shelfLocation",shelfLocation);
                    obj.record.set("invlocation",productLocation);
                    obj.record.set("invstore", productWarehouse);
                 
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
                          if(this.UomSchemaType==Wtf.UOMSchema){//Need to Discuss with sir
                               obj.record.set("rate", modifiedRate);
                          }else{
                               obj.record.set("rate", modifiedRate*baseuomRate);
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
//                                    obj.record.set("dquantity", obj.value);    // to avoid link entry problem( if created vendor Invoice and in PR Change the actual qty then it shows warning msg and if we click on yes then it should not load changes qty in Deliv qty)
                                    rec.set("changedQuantity",(rec.data.copyquantity-obj.value)*((rec.data.baseuomrate===""||rec.data.baseuomrate==undefined)?1:rec.data.baseuomrate));
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
                            var msg = WtfGlobal.getLocaleText("acc.field.Returnquantityshouldnotbegreater");
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg], 2);
                            obj.record.set("dquantity", proqty);
                         } 
                   }         
//                    obj.record.set("invstore", this.batchDetailswin.defaultLocation);
//                    obj.record.set("invlocation", this.batchDetailswin.defaultWarehouse);
//                
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
            } else if(obj.field=="dquantity"){
                rec=obj.record;
                if(!this.isCustomer)
                    rec.set("changedQuantity",(rec.data.copyquantity-obj.value)*((rec.data.copybaseuomrate===""||rec.data.copybaseuomrate==undefined)?1:rec.data.copybaseuomrate));
                else
                    rec.set("changedQuantity",(obj.value-rec.data.copyquantity)*((rec.data.copybaseuomrate===""||rec.data.copybaseuomrate==undefined)?1:rec.data.copybaseuomrate));
                
                if(deliveredproqty > proqty){
                    var msg = WtfGlobal.getLocaleText("acc.field.Returnquantityshouldnotbegreater");
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg], 2);
                    obj.record.set("dquantity", proqty);
                } else if(deliveredproqty <= 0){
                    var msg = WtfGlobal.getLocaleText("acc.field.Returnquantityshouldnotbeequalorlessthanzero");
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
                }
                
                if(obj.field=="prtaxid" || obj.field=="rate" || obj.field=="dquantity" || obj.field=="quantity"){
                    var taxamount = this.setTaxAmountAfterSelection(obj.record);
                    obj.record.set("taxamount",taxamount);
                    this.fireEvent('datachanged',this);
                }
                
                if (obj.field=="rate" && obj.originalValue != obj.value) {
                    if (!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.addprice) && !this.isRequisition) { // permissions
                        var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, this.productEditor.getValue(), 'productid');
                        var productname = "";
                        var prorec = null;
                        if (productComboIndex >= 0) {
                            prorec = this.productComboStore.getAt(productComboIndex);
                            productname = prorec.data.productname;
                        }
                        rec.set("productname",productname);
                        rec.set("price", obj.value);
                        if (this.isCustomer) {
                            rec.set("customer", this.parentObj.Name.getValue());
                        } else {
                            rec.set("vendor", this.parentObj.Name.getValue());
                        }
//                        if (Wtf.account.companyAccountPref.unitPriceConfiguration) {
                            Wtf.Msg.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.youHaveChangedThePriceForProduct") + " <b>" + productname + "</b>." + ' ' + WtfGlobal.getLocaleText("acc.field.doYouWantToSaveInPriceMaster"),
                                this.showPriceWindow.createDelegate(this,[rec, obj],true), this);
//                        }
                    }
                }
        }
        if(obj.field=="amounttoadjust"){
            var val=this.amountToAdjust.getValue();
            val = val*proqty;
            var taxpercent=0;
            var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
            if(index>=0){
                var taxrec=this.taxStore.getAt(index);
                taxpercent=getRoundedAmountValue(taxrec.data.percent);
            }
            var taxamount1= getRoundedAmountValue(val*taxpercent/100);
            rec.set("taxamounttoadjust",taxamount1);
            var amount1=val+taxamount1;
            rec.set("adjustedamount",amount1);
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
//        var discount = 0;
        var rate=getRoundofValueWithValues(rec.data.rate,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        quantity=getRoundofValue(rec.data.dquantity);
        var origionalAmount = getRoundedAmountValue(rate*quantity) ;
//        if(rec.data.partamount != 0){
//            var partamount=getRoundedAmountValue(rec.data.partamount);
//            origionalAmount = getRoundedAmountValue(origionalAmount * (partamount/100));
//        }
        
//        origionalAmount = this.calAmountWithExchangeRate(origionalAmount, rec);
//        if(rec.data.prdiscount > 0) {
//            var prdiscount=getRoundedAmountValue(rec.data.prdiscount);
//            if(rec.data.discountispercent == 1){
//                discount = getRoundedAmountValue(origionalAmount * prdiscount/ 100);
//            } else {
//                discount = prdiscount;
//            }
//        }
        //var discount=rec.data.rate*rec.data.quantity*rec.data.prdiscount/100
        var val=origionalAmount;
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
        
//        var discount = 0;//origionalAmount*rec.data.prdiscount/100   
//        if(rec.data.prdiscount > 0) {
//            var prdiscount=getRoundedAmountValue(rec.data.prdiscount);
//            if(rec.data.discountispercent == 1){
//                discount = getRoundedAmountValue((origionalAmount * prdiscount) / 100);
//            } else {
//                discount = prdiscount;
//            }
//        }
        
        var val=(origionalAmount);///rec.data.oldcurrencyrate  
//        rec.set("amountwithouttax",val);
        var taxamount = 0;
        if(rec.data.taxamount){
            taxamount= getRoundedAmountValue(rec.data.taxamount);
        }
        val=parseFloat(val)+parseFloat(taxamount);

        rec.set("amount",(parseFloat(getRoundedAmountValue(val)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1));
//        if(this.isQuotationFromPR && val!==0 && (rec.data.orignalamount==undefined || rec.data.orignalamount==""))
//        rec.set("orignalamount",val);
        return WtfGlobal.withoutRateCurrencySymbol(val,m,rec);
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
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninPRareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+WtfGlobal.getLocaleText("acc.field.is")+' '+(availableQuantity-lockQuantity)+' <br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                          rec.set("quantity",obj.originalValue);
                          rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
                          obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
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
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninPRareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productname']+WtfGlobal.getLocaleText("acc.field.is")+' '+rec.data['quantity']+' <br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                            obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
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
                }else if(!this.isCustomer&&((rec.data['quantity'])-(rec.data['lockquantity']))<obj.record.data['dquantity']&& prorec.data.type!="Service" && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation){
                    this.isValidEdit = false;
        //                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productname']+' is '+rec.data['quantity']], 2);
        //                    obj.cancel=true;
                        if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninPRareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productname']+' '+WtfGlobal.getLocaleText("acc.field.is")+' '+((rec.data['quantity'])-(rec.data['lockquantity']))+' <br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                            rec.set("quantity",obj.originalValue);
                            obj.cancel=true;   
                        }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
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
                if(prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part') {
                    var availableQuantity = prorec.data.quantity;
                    var lockQuantity = prorec.data.lockquantity; 
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
                        availableQuantity = availableQuantity + copyquantity;
                        if((availableQuantity-lockQuantity) < quantity) {                      
                            if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninPRareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' '+WtfGlobal.getLocaleText("acc.field.is")+' '+(availableQuantity-lockQuantity)+' <br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                            rec.set("quantity",originalDquantity);
                            rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                            rec.set("baseuomrate",originalBaseuomrate);
                            obj.cancel=true;   
                            }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
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
                    if((availableQuantity-lockQuantity) < quantity) {  //for normal check for all products available quantity
    //                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' is '+availableQuantity], 2);
    //                        obj.cancel=true;
                            if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninPRareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' '+WtfGlobal.getLocaleText("acc.field.is")+' '+(availableQuantity-lockQuantity)+' <br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);                      
                            rec.set("quantity",originalDquantity);
                            rec.set("baseuomquantity",originalDquantity*originalBaseuomrate);
                            rec.set("baseuomrate",originalBaseuomrate);
                            obj.cancel=true;   
                            }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
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
        this.store.load({params:{bills:rec.data['billid'],mode:43,closeflag:true,doflag:true,linkingFlag:linkingFlag,moduleid:this.moduleid}});
        this.store.on('load',function(rec){
            this.store.each(function(rec){
                if(rec.data.dquantity==""){
                    
                    var quantity = rec.data.quantity;
                    quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
                    
                    rec.data.dquantity=quantity;
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
