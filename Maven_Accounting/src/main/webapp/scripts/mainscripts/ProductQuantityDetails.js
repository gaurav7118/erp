/*
 * Component Used for showing Product quantities in Different-2 UOMs
 */

Wtf.account.ProductQuantityDetails = function(config){
    
    this.selectedUOMs = "";
    this.isProductQuantityDetails=true;
    this.isFromQuantityDetailsReport=config.isFromQuantityDetailsReport;
    chkUomload();

    this.newUomRec = Wtf.data.Record.create([
        {name: 'uomid'},
        {name: 'uomname'},
        {name: 'precision'}
    ]);
    this.newUomStore = new Wtf.data.Store({
        url: "ACCUoM/getUnitOfMeasure.do",
        baseParams: {
            mode: 31,
            common: '1'
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.newUomRec)
    });
    
    this.newUomStore.load();
    
    this.uomComboconfig = {
        //hiddenName:this.businessPerson.toLowerCase(),         
        store:this.newUomStore,
        valueField:'uomid',
        hideLabel:false,
        //hidden : iscustomer,
        displayField:'uomname',
        emptyText:WtfGlobal.getLocaleText("acc.field.selectUOM"),
        mode: 'local',
        typeAhead: true,
        selectOnFocus:true,
        triggerAction:'all',
        scope:this
    };
    
    this.uomEditor = new Wtf.common.Select(Wtf.applyIf({
         multiSelect:true,
         fieldLabel:WtfGlobal.getLocaleText("acc.field.selectUOM"),
         forceSelection:true,
         listWidth:240,
         width:240
    },this.uomComboconfig));
//    
//    this.uomEditor=new Wtf.form.FnComboBox({
//        name:'uomname',
//        store:Wtf.uomStore,
//        typeAhead: true,
//        selectOnFocus:true,
//        valueField:'uomid',
//        displayField:'uomname',
//        scope:this,
//        forceSelection:true
//    });
    
//    if(!WtfGlobal.EnableDisable(Wtf.UPerm.uom, Wtf.Perm.uom.edit))
//        this.uomEditor.addNewFn=this.showUom.createDelegate(this);
    
    
    
    this.createProductRecordArray();
    
    this.createStore();
    
//    this.productStore.on('datachanged', function() {
//        if(this.pageLimit.combo) {
//            var p = this.pageLimit.combo.value;
//            this.localSearch.setPage(p);
//        }
//    }, this);
    
    this.msgLmt = 30;
    
    this.loadingMask = new Wtf.LoadMask(document.body,{
        msg : WtfGlobal.getLocaleText("acc.msgbox.50")
    });
    
//    this.productStore.on('beforeload',function(){
//        this.loadingMask.show();
//    },this);
//    
//    this.productStore.on('loadexception',function(){
//        this.loadingMask.hide();
//    },this);
//    
//    this.productStore.on('load',function(){
//        this.loadingMask.hide();
//    },this);
    
//    
//    this.productStore.on('beforeload',function(){
//        this.productStore.baseParams.selectedUOMs=this.selectedUOMs;
//        this.productStore.baseParams.isForProductQuantityDetailsReport=true;
//    },this);
    
    this.productStore.load({
        params: {
            start: 0,
            selectedUOMs:this.selectedUOMs,
            limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt
        }
    });
    
    
    this.createColumnModelArray();
    
    var tbar= new Array();
    
//    this.localSearch = new Wtf.KWLTagSearch({
//        emptyText:WtfGlobal.getLocaleText("acc.productList.searchText"),//'Search by Product Name',
//        width: 130,
//        field: 'productname',
//        Store:this.productStore
//    });
//    
//    tbar.push(this.localSearch);


    this.productComboRec = Wtf.data.Record.create ([
        {
            name:'productid'
        },

        {
            name:'productname'
        },

        {
            name:'pid'
        },
        {
            name:'type'
        }, 
        {
            name:'desc'
        },

        {
            name:'uomid'
        },

        {
            name:'uomname'
        },

        {
            name:'parentid'
        },

        {
            name:'parentname'
        },

        {
            name:'purchaseaccountid'
        },

        {
            name:'salesaccountid'
        },

        {
            name:'purchaseretaccountid'
        },

        {
            name:'salesretaccountid'
        },

        {
            name:'reorderquantity'
        },

        {
            name:'quantity'
        },

        {
            name:'reorderlevel'
        },

        {
            name:'leadtime'
        },

        {
            name:'purchaseprice'
        },

        {
            name:'saleprice'
        },

        {
            name: 'leaf'
        },

        {
            name: 'level'
        },

        {
            name: 'producttype'
        }
        ]);

    this.productComboStore = new Wtf.data.Store({
//        url:Wtf.req.account+'CompanyManager.jsp',
        url:"ACCProduct/getProductsForCombo.do",
        baseParams:{
            mode:22,
            excludeParent:true
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.productComboRec)
    });
    this.productOptimizedFlag = Wtf.account.companyAccountPref.productOptimizedFlag;
    var baseParamsforCombo = this.productComboStore.baseParams;
    var configforCombo = {
        multiSelect: true,
        listWidth: Wtf.ProductComboListWidth
    }
    this.productname = CommonERPComponent.createProductMultiselectPagingComboBox(200, 300, 30, this, baseParamsforCombo, configforCombo);

    tbar.push(WtfGlobal.getLocaleText("acc.field.SelectProducts") + " : ", this.productname);
    tbar.push('-', WtfGlobal.getLocaleText("acc.field.selectUOM") + " : ", this.uomEditor);
    
    tbar.push(
    {
        xtype:'button',
        text:WtfGlobal.getLocaleText("acc.agedPay.fetch"),  //'Fetch',
        iconCls:'accountingbase fetch',
        scope : this,
        tooltip:WtfGlobal.getLocaleText("acc.commom.fetch.records"),
        handler:this.fetchHandler
    });
    
    this.exportButton=new Wtf.exportButton({
        obj:this,
        id:'productlistexporta',
        tooltip:WtfGlobal.getLocaleText("acc.common.exportTT"),  //"Export Report details.",  
        params:{name:WtfGlobal.getLocaleText("acc.productList.tabTitle")},
        menuItem:{xls:true,csv:true,pdf:true,rowPdf:false},
        get:198,
        label:WtfGlobal.getLocaleText("acc.field.ProductList1"),
        filename:WtfGlobal.getLocaleText("acc.productquantity.details")+"_v1"
    }),
    
    this.exportButton.setParams({
        mode:22
    });
    
    this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
        params:{name:WtfGlobal.getLocaleText("acc.productList.tabTitle")},
        menuItem:{print:true},
        get:198,
        label:WtfGlobal.getLocaleText("acc.cnList.prodList")
    });
         
    tbar.push('-',this.exportButton,'-',this.printButton);
    

    this.grid = new Wtf.grid.GridPanel({
        store : this.productStore,
        cm : new Wtf.grid.ColumnModel(this.columnArr),
        sm: new Wtf.grid.RowSelectionModel(),
        border : false,
//        autoHeight:true,
        stripeRows : true,
        layout : "fit",
        viewConfig : {
//            forceFit : true,
            emptyText:'<div style="font-size:15px; text-align:center; color:#CCCCCC; font-weight:bold; margin-top:8%;">No Record To Display<br></div>'
        }
    })
    
    this.pageLimit = new Wtf.forumpPageSize({
            ftree:this.grid,
            recordsLimit: Wtf.MaxPageSizeLimit
        });
        
    Wtf.apply(this, {
        border: false,
        scope: this,
        layout: "fit",
//        autoScroll:true,
        tbar: tbar,
        items: [this.grid],
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: this.msgLmt,
            id: "pagingtoolbar" + this.id,
            store: this.productStore,
//            searchField: this.localSearch,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.agedPay.norec"), //"No results to display",
//            plugins: this.pageLimit
            plugins: this.pP = new Wtf.common.pPageSize({
                id: "pPageSize_" + this.id
            }),
//            items:buttomBar
        })
    });
    
    Wtf.account.ProductQuantityDetails.superclass.constructor.call(this,config);
}

Wtf.extend( Wtf.account.ProductQuantityDetails,Wtf.Panel,{
    unitRenderer:function(value,metadata,record,rowIndex, colIndex){
        var dataindex = this.grid.getColumnModel().getDataIndex(colIndex);
        var uomRec = WtfGlobal.searchRecord(this.newUomStore, dataindex, 'uomid');
        var unit="";
            if(uomRec){
                unit = uomRec.get('uomname');
            }else{
                unit=record.data['uomname'];
            }          
            if(record.data['type'] == "Service" || record.data['type'] == "Non-Inventory Part"){
                    return "N/A";
            }
    	
       value=parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+" "+unit;
       if(record.data.deleted)
                value='<del>'+value+'</del>';    
        return value;
    },
    
    fetchHandler:function(){
        
        //  Check valid Products Selected or Not
            
        var isInvalidProductsSelected = WtfGlobal.isInvalidProductsSelected(this.productname);
        if(isInvalidProductsSelected){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.select.validproduct")],3);
            return;
        }
        
        var productIds = [];
        var productIdsCommaSeprated ="";
        
        if(this.productname.getValue() && this.productname.getValue().length>0){
            productIds = this.productname.getValue().split(',');
            productIdsCommaSeprated = this.productname.getValue();
        }
        
        
        this.createProductRecordArray();// resetting record array
            
        this.createColumnModelArray();// resetting column array
        
        var selectedUOMs = this.uomEditor.getValue();
        if(selectedUOMs.length>0){
            
            var selectedUOMsArray = selectedUOMs.split(",");
            var uomRecConfigArr =[];
            for (var i = 0; i < selectedUOMsArray.length; i++) {
                var uomId = selectedUOMsArray[i];

                var uomRec = WtfGlobal.searchRecord(this.newUomStore, uomId, 'uomid');

                if(uomRec){
                    var uomName = uomRec.get('uomname');

                    this.productRecArray.push({
                        name:uomId
                    });
                    uomRecConfigArr.push({
                        fieldname: uomId
                    });

                    this.columnArr.push({
                        header:WtfGlobal.getLocaleText("acc.productList.gridAvailableQty")+" In "+uomName,
                        dataIndex:uomId,
                        align:'right',
                        renderer:this.unitRenderer.createDelegate(this),
                        hidden:false,
                        width:120,
                        pdfwidth:75
                    });
                }

            }
            WtfGlobal.updateStoreConfig(uomRecConfigArr,this.productStore);
        }
        
//        this.createStore();
        
        
        
        this.grid.reconfigure(this.productStore,new Wtf.grid.ColumnModel(this.columnArr));
        
        this.selectedUOMs = selectedUOMs;
        
        var params = {};
        
        params.start = 0;
        params.limit = (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt;
        
        if(productIds.length>0){
            params.ids=productIdsCommaSeprated;
        }
        
        this.productStore.load({
            params:params
        });
    },
    
    createProductRecordArray:function(){
        
        this.productRecArray = new Array();
        
        this.productRecArray.push({name:'productid'},
            {name:'productname'},
            {name:'desc'},
            {name:'pid'},
            {name:'vendor'},
            {name:'producttype'},
            {name:'type'},
            {name:'initialsalesprice'},
            {name:'warrantyperiod'},
            {name:'warrantyperiodsal'},
            {name:'deleted'},
            {name:'quantity'},
            {name:'uomid'},
            {name: 'productweight'},
            {name: 'netproductweight'},
            {name:'uomname'}
        );
    },
    
    createStore:function(){
        
        this.productRec = Wtf.data.Record.create (this.productRecArray);

        this.jReader = new Wtf.data.KwlJsonReader({
            totalProperty: 'totalCount',
            root: "data"
        }, this.productRec);

        this.productStore = new Wtf.data.Store({
            url:"ACCProduct/getProducts.do",
            remoteSort:true,
            baseParams:{
                mode:22
            },
            reader: this.jReader
        });
        
        this.productStore.on('beforeload',function(){
             this.loadingMask.show();
            var currentBaseParams = this.productStore.baseParams;
            currentBaseParams.selectedUOMs = this.selectedUOMs;
            currentBaseParams.isForProductQuantityDetailsReport = true;
            currentBaseParams.isFromQuantityDetailsReport = this.isFromQuantityDetailsReport;
            this.productStore.baseParams=currentBaseParams;        
        },this);
        
        this.productStore.on('loadexception',function(){
            this.loadingMask.hide();
        },this);

        this.productStore.on('load',this.setPageSize,this);

    },
    
        
    createColumnModelArray:function(){
        this.columnArr =[];
        this.columnArr.push({
            header:WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),//"Product",acc.productList.gridProduct
            dataIndex:'productname',
            sortable: true,
            pdfwidth:150,
            width:175,
            renderer:WtfGlobal.deletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridProductID"),
            dataIndex:'pid',
            align:'left',
            pdfwidth:150,
            width:175,
            sortable: true,
            renderer:WtfGlobal.deletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridProductDescription"),// Product Description",
            dataIndex:'desc',
            sortable: true,
            width:175,
            renderer : function(val,m,rec) {
                val = val.replace(/(<([^>]+)>)/ig,"");
                if(rec.data.deleted)
                    val='<del>'+val+'</del>';
                return "<div wtf:qtip=\""+val+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.productList.gridProductDescription")+"'>"+val+"</div>";
            },
            pdfwidth:150
        },{
            hidden:true,
            header:WtfGlobal.getLocaleText("acc.field.ProductUUID"),
            dataIndex:'productid',
            width:175,
            renderer : WtfGlobal.deletedRenderer            
        },{
            header:WtfGlobal.getLocaleText("acc.masterConfig.uom"),
            dataIndex:'uomname',
            hidden:true,
            width:175,
            renderer:WtfGlobal.deletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridProductType"),//"Product Type",
            dataIndex:'type',
            pdfwidth:150,
            width:200,
            sortable: true,
            renderer: function(val,m,rec){
                if(rec.data.deleted)
                    val='<del>'+val+'</del>';
                return val;
            }
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridAvailableQty")+" ("+WtfGlobal.getLocaleText("acc.commom.unit.Base.UOM") +")",//"Available Quantity",
            dataIndex:"quantity",
            align:'right',
            sortable: true,
            renderer:this.unitRenderer.createDelegate(this),
            width:175,
            pdfwidth:150
        },{
            header: WtfGlobal.getLocaleText("acc.erp.ProductWeightPerBaseUom"), //"Product Weight (Kg) per base UOM",
            dataIndex: 'productweight',
            pdfwidth: 150,
            align:'right',
            width: 150,
            renderer: function (value, m, rec) {
                value = parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                if (rec.data.deleted)
                    value = '<del>' + value + '</del>';
                return value;
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.productList.gridProductNetWeight"), //"Product Net Weight (Kg)",
            dataIndex: 'netproductweight',
            pdfwidth: 150,
            align:'right',
            width: 150,
            renderer: function (value, m, rec) {
                value = parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                if (rec.data.deleted)
                    value = '<del>' + value + '</del>';
                return value;
            }
        });
    },
    
    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.productStore.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value
                }
            });
        }
    },
    setPageSize: function(store, rec, opt) {
        this.loadingMask.hide();
        this.pageLimit.totalSize = this.jReader.jsonData['totalCount'];
        this.grid.getView().refresh();
    }
})