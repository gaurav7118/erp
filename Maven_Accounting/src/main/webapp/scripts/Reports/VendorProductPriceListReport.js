/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/* -------------------------------------------- Price List/Band Cost Report ------------------------------*/

function callVendorProductPriceReport() {    
    var mainTabId = Wtf.getCmp("as");
    var newTab = Wtf.getCmp("callVendorProductPriceReport");
    if (newTab == null) {
        newTab = new Wtf.account.ProductPriceReportCustVen({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.report.vendorproductpricelistreport"), Wtf.TAB_TITLE_LENGTH),
            fullTitle:WtfGlobal.getLocaleText("acc.report.vendorproductpricelistreport"),
            tabTip:WtfGlobal.getLocaleText("acc.report.vendorproductpricelistreport.toolTip"),
            id:"callVendorProductPriceReport",
            isCust : false,
            layout:'fit',
            closable:true,
            iconCls:'accountingbase pricelistreport',
            border:false
        });
        mainTabId.add(newTab);
    }
    mainTabId.setActiveTab(newTab);
    mainTabId.doLayout();
}
Wtf.account.ProductPriceReportCustVen = function(config){
    Wtf.apply(this, config);
    
    this.arr = [];
    this.dmflag = 1;
    this.createTBar();
    this.createGrid();
    Wtf.account.PriceReportCustVen.superclass.constructor.call(this,config);
}

Wtf.extend( Wtf.account.ProductPriceReportCustVen,Wtf.Panel,{
    hideMsg: function(){
        if(this.Store.getCount()==0){
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        Wtf.MessageBox.hide();
    },
    onRender:function(config){
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            items: [{
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar: this.tbarArray,
                bbar:this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.Store,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({
                        id : "pPageSize_"+this.id
                    }),
                    scope:this,
                    items: this.bBarBtnArr
                })
                    
            }]
        });

        this.add(this.leadpan);
        this.fetchData();
        Wtf.account.ProductPriceReportCustVen.superclass.onRender.call(this,config);
        
    },
    createGrid:function(){
        this.Store = new Wtf.data.GroupingStore({
            url: "ACCCombineReports/getProductPriceReportCustVen.do",
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty: 'count'
            }
            )
        });   
        this.groupStore = new Wtf.data.GroupingStore({
            groupField: ['productcode']
        });
        this.sm = new Wtf.grid.RowSelectionModel({
            });
        var cmDefaultWidth = 106;
        var colArr = [];

        this.cm = new Wtf.grid.ColumnModel(colArr);
        this.summary = new Wtf.grid.GroupSummary();
        this.gridSummary = new Wtf.grid.GridSummary();
        var grpView = new Wtf.grid.GroupingView({
            startCollapsed :true,
            forceFit : false,
            showGroupName : true,
            enableGroupingMenu : true,
            hideGroupedColumn : false
        });
        this.grid = new Wtf.grid.GridPanel({
            columns: [],
            store: this.groupStore,
            displayInfo: true,
            sm: this.sm,
            loadMask: true,        
            view: grpView       
        });
        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
        this.Store.on("load", this.storeLoaded, this); 
        this.Store.on("dataChanged", this.storeLoaded, this);   
    },
    createTBar: function() {
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
    
        this.MSComboconfig = {
            fieldLabel:WtfGlobal.getLocaleText("acc.product.productName"),
            hiddenName:'productid',
            name: 'productid',
            store: this.productComboStore,
            valueField:'productid',
            displayField:'productname',
            mode: 'local',
            allowBlank:true,
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectProducts"),
            typeAhead: true,
            selectOnFocus:true,                            
            triggerAction:'all',
            scope:this
        };
        this.productname = new Wtf.common.Select(Wtf.applyIf({
            multiSelect:true,
            forceSelection:true,
            extraFields:['pid','type'],
            extraComparisionField:'pid',// type ahead search on product id as well.
            listWidth:Wtf.ProductComboListWidth,
            width:240
        },this.MSComboconfig));
         this.productComboStore.on("load", function(store){
            var storeNewRecord=new this.custRec({
                productname:'All',
                pid:'All',
                productid:'All',
                type:''
            });
            this.productname.store.insert( 0,storeNewRecord);
            this.productname.setValue("All");
        },this);  
        this.productComboStore.load();
    
       this.custRec = Wtf.data.Record.create ([{
            name:'accountname',
            mapping:'accname'
        },{
            name:'accountid',
            mapping:'accid'
        },{
            name:'acccode'
        }]);
        this.custStore =  new Wtf.data.Store({
            url:this.isCustomer?"ACCCustomer/getCustomersForCombo.do":"ACCVendor/getVendorsForCombo.do",
            baseParams:{
                mode:2,
                group:this.isCustomer?10:13,
                deleted:false,
                nondeleted:true,
                combineData:this.isCustomer?1:-1  //Send For Seprate Request
            },
            reader: new  Wtf.data.KwlJsonReader({
                root: "data"
            },this.custRec)
        });
        this.CustomerMSComboconfig={
            hiddenName:'accountmulselectcombo',         
            store: this.custStore,
            valueField:'accountid',
            hideLabel:false,
            hidden : false,
            displayField:'accountname',
            emptyText:this.isCustomer?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven") ,
            mode: 'local',
            typeAhead: true,
            selectOnFocus:true,
            triggerAction:'all',
            scope:this
        };
        
        this.cmbAccount = new Wtf.common.Select(Wtf.applyIf({
            multiSelect:true,
            fieldLabel:WtfGlobal.getLocaleText("acc.ledger.accName"),  //'Account Name',,
            forceSelection:true,   
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:250,
            width:240
        },this.CustomerMSComboconfig));
            
        this.custStore.on("load", function(store){
            var storeNewRecord=new this.custRec({
                accountname:'All',
                accountid:'All',
                acccode:''
            });
            this.cmbAccount.store.insert( 0,storeNewRecord);
            this.cmbAccount.setValue("All");
        },this);         
        this.custStore.load();
        this.tbarArray= new Array();
        this.tbarArray.push(WtfGlobal.getLocaleText("acc.field.SelectProducts")+" : ",this.productname);
        this.tbarArray.push(WtfGlobal.getLocaleText("acc.vppl.SelectVendor(s)")+" : ",this.cmbAccount);
        /*
         * Provided button to expand or collapse all row details. 
         */
        this.expandCollpseButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.Collapse"),
            tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
            iconCls: 'pwnd toggleButtonIcon',
            scope: this,
            handler: function () {
                this.expandCollapseGrid(this.expandCollpseButton.getText());
            }
        });
        this.tbarArray.push(
        {
            xtype:'button',
            text:WtfGlobal.getLocaleText("acc.agedPay.fetch"),  //'Fetch',
            iconCls:'accountingbase fetch',
            scope : this,
            tooltip:WtfGlobal.getLocaleText("acc.commom.fetch.records"),
            handler:this.fetchHandler
                }, '-', this.expandCollpseButton);
        
        this.exportButton=new Wtf.exportButton({
            obj:this,
            scope:this,
            id:'prodlistbycategoryexport',
            tooltip:WtfGlobal.getLocaleText("acc.common.exportTT"),  //"Export Report details.",  
            params:{
                mode:12,
                productPriceinMultipleCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency,
                reportType:"productPriceListReport",
                carryin : !this.isCust
            },
            menuItem:{
                csv:true,
                pdf:true,
                rowPdf:false,
                xls:true
            },
            get:829,
            filename:this.fullTitle+"_v1",
            label:this.fullTitle+"_v1"
 
        });
        
        this.printButton = new Wtf.exportButton({
            text: WtfGlobal.getLocaleText("acc.common.print"), //"Print",
            obj: this,
            tooltip: WtfGlobal.getLocaleText("acc.common.printTT"), //"Print Report details.",   
            params: {
                mode:12,
                productPriceinMultipleCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency,
                reportType:"productPriceListReport",
                carryin : !this.isCust
            },
            filename: this.fullTitle,
            menuItem: {
                print: true
            },
            get: 829
        });
        
        this.exportButton.on("click", function() {
            this.exportButton.params.pid=this.productname.getValue();
            if (this.cmbAccount.getValue() != 'All') {
                this.exportButton.params.vids = this.cmbAccount.getValue();
            } else {
                this.exportButton.params.vids = "";
            }
        }, this);
        this.bBarBtnArr= new Array();
        this.bBarBtnArr.push(this.exportButton,"-",this.printButton);         
    },
    storeLoaded: function() {
        var columns = [];
       
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if (column.dataIndex == "applydate") {
                column.renderer = WtfGlobal.onlyDateLeftRenderer;
            } 
            columns.push(column);
        });
        var Arr = [];
        Wtf.each(this.Store.reader.jsonData.metaData.fields, function(column) {
            Arr.push(column);
        });
        this.groupStore.removeAll();
        this.groupStore.fields = Arr;
        this.grid.getColumnModel().setConfig(columns);
        this.groupStore.add(this.Store.getRange(0, (this.Store.data.items.length - 1)));       
        this.grid.getView().refresh();        
        if(this.Store.getCount() < 1) {
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        this.expandButtonClicked = false;
    },
    fetchData: function() {
        this.Store.load({
            params: {
                start: 0,
                limit: 30,
                mode:12,
                productPriceinMultipleCurrency:Wtf.account.companyAccountPref.productPriceinMultipleCurrency,
                reportType:"productPriceListReport",
                carryin : !this.isCust
            }
        });
    },
    CarryInRenderer:function(value){
        var temptxt="";
        if(value=="false")
            temptxt=WtfGlobal.getLocaleText("acc.productList.gridSalesPrice");  //"Sale Price";
        else
            temptxt=WtfGlobal.getLocaleText("acc.productList.gridPurchasePrice");  //"Purchase Price";
        return temptxt;
    }, 
    fetchHandler:function(){
        this.Store.load();
    },
    handleStoreBeforeLoad: function() {
        var productIds = [];
        
        
        if(this.productname.getValue()!='All' && this.productname.getValue().length>0){
            productIds = this.productname.getValue().split(',');
        }
        var vendorCMB = [];
        
        if(this.cmbAccount.getValue()!='All' && this.cmbAccount.getValue().length>0){
            vendorCMB = this.cmbAccount.getValue().split(',');
        }
        
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.mode=12;
        currentBaseParams.productPriceinMultipleCurrency=Wtf.account.companyAccountPref.productPriceinMultipleCurrency;
        currentBaseParams.reportType="productPriceListReport";
        currentBaseParams.carryin=!this.isCust;  
       if(productIds.length>0){
            currentBaseParams.ids=productIds.toString();
        }else{
             currentBaseParams.ids='';
        }
        
        if(vendorCMB.length>0){
            currentBaseParams.vids=vendorCMB.toString();
        }else{
            currentBaseParams.vids='';
        }
        this.Store.baseParams = currentBaseParams;
    },
    /*
     * ExpandCollapse button handler
     * To expand or collapse all row details
     * If grid rows are already in expand mode then collapse rows and vise versa
     */
    expandCollapseGrid: function (btntext) {
        if (btntext == WtfGlobal.getLocaleText("acc.field.Collapse")) {
            /*If button text is collapse then collapse all rows*/
            this.grid.getView().collapseAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if (btntext == WtfGlobal.getLocaleText("acc.field.Expand")) {
            /*If button text is expand then expand all rows*/
            this.grid.getView().expandAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    }
});

