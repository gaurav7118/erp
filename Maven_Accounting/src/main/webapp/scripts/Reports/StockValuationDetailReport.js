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

function getStockValuationDetailsReportDynamicLoad(params) {
    var record = params.record;
    var type = params.type;

    /*
     *Function to add this report in widget report.
     **/
    if (params.isCustomWidgetReport) {
        var panel = new Wtf.account.TransactionListPanelViewStockValuationDetailReport({

            border: false,
            record: record,
            layout: 'fit',
            closable : true,
            isCustomWidgetReport: params.isCustomWidgetReport,
            iconCls:getButtonIconCls(Wtf.etype.inventoryval)
        });
        if (params.callbackFn) {
            /*
             *call callback function to add this report to widget.
             **/
            params.callbackFn.call(this, panel);
        }
    }else{
            
            var reportPanel = Wtf.getCmp('stockvaluationdetailsreportnew');
            if(reportPanel == null){
            reportPanel = new Wtf.account.TransactionListPanelViewStockValuationDetailReport({
            id : 'stockvaluationdetailsreportnew',
            border : false,
                title: WtfGlobal.getLocaleText("acc.stockValuationDetail.tabtitle"), //"Stock Valuation Detail Report",
                tabTip: WtfGlobal.getLocaleText("acc.stockValuationDetail.tabtitle"), //"Stock Valuation Detail Report",
            record:record,
                layout: 'fit',
            closable : true,
            iconCls:getButtonIconCls(Wtf.etype.inventoryval)
            });
            Wtf.getCmp('as').add(reportPanel);
        }
        Wtf.getCmp('as').setActiveTab(reportPanel);
        Wtf.getCmp('as').doLayout();
    }
}

//****************************************************************************************

Wtf.account.TransactionListPanelViewStockValuationDetailReport = function(config) {
    Wtf.apply(this, config);
    this.record = config.record;
    this.type=config.type;

    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.stockValuationDetail.QuickSearchEmptyText"), // "Search by Product ID, Batch ...",
        width: this.isCustomWidgetReport ? 100 : 150,
//        id:"quickSearch"+config.helpmodeid,
        field: 'productid'
    });

    this.resetBttn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  // 'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  // 'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        hidden : this.isCustomWidgetReport,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);

    this.startDate = new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  // 'From',
        name:'startdate',
        format:WtfGlobal.getOnlyDateFormat(),
        // readOnly:true,        
        value:WtfGlobal.getDates(true)
    });

    this.endDate = new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  // 'To',
        format:WtfGlobal.getOnlyDateFormat(),
        //readOnly:true,
        name:'enddate',
        value:WtfGlobal.getDates(false)
    });

    this.locTypeRec = new Wtf.data.Record.create([
        {name:"levelId"},
        {name:"levelName"},
    ]);

    this.locTypeReader = new Wtf.data.KwlJsonReader({
        root:"data"
    },this.locTypeRec);

    this.locTypeStore = new Wtf.data.Store({
        url:"ACCMaster/getLevelsCombo.do",
        reader:this.locTypeReader
    });
    this.locTypeStore.load();
    this.locTypeEditor = new Wtf.form.ComboBox({
        triggerAction:'all',
        mode: 'local',
        fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.12"),
        valueField:'levelId',
        displayField:'levelName',
        store:this.locTypeStore,
        hidden : this.isCustomWidgetReport,
        anchor:'90%',
        width:100,
        emptyText:WtfGlobal.getLocaleText("acc.stockValuationDetail.LocEmptyText"),
        listWidth:200,
        value:this.type,
        typeAhead: true,
        forceSelection: true,
        name:'locationtp'
    });


    this.locItemRec = new Wtf.data.Record.create([
        {name:"id"},
        {name:"name"},
    ]);

    this.locItemReader = new Wtf.data.KwlJsonReader({
        root:"data"
    },this.locItemRec);

    this.locItemStore = new Wtf.data.Store({
        url:"ACCMaster/getLocItems.do",
        reader:this.locItemReader
    });

    this.locItemEditor = new Wtf.form.ComboBox({
        triggerAction:'all',
        id:'locItemEditor',
        mode: 'local',
        fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.12"),
        valueField:'id',
        displayField:'name',
        disabled:true,
        store:this.locItemStore,
        width:100,
         listWidth:200,
        anchor:'90%',
        emptyText:WtfGlobal.getLocaleText("acc.ra.value"),
        value:this.record ? this.record.data.locationid : '',
        typeAhead: true,
        hidden :  this.isCustomWidgetReport,
        forceSelection: true,
        name:'locitems'
    });

      this.locTypeEditor.on("select",function(f){
        var itemBox=Wtf.getCmp('locItemEditor') 
//        itemBox.clearValue();
        itemBox.enable();
        var batchStore=this.batchEditor.store
        var batchBox=this.batchEditor;
        batchStore.removeAll();
        if(Wtf.account.companyAccountPref.isBatchCompulsory){
            batchBox.clearValue();
        }
        var levelid=f.getValue();
        batchBox.disable();
        var transType=(levelid==2 ? 'location' :(levelid==1 ? 'warehouse': (levelid==3 ? 'row' :(levelid==4 ? 'rack' : 'bin'))));
        var store=Wtf.getCmp('locItemEditor').store;
        store.load({
            params:{
                levelid:levelid,
                transType:transType,
                includeQAAndRepairStore:true,
                includePickandPackStore:true
            }
        })
    },this);  

    this.locItemStore.on("load",function(){
        var record = new Wtf.data.Record({
            id: "",
            name: "All Records"
        });
        this.locItemStore.insert(0, record);
        this.locItemEditor.setValue("");
        
        if(this.record != undefined) {
            this.locItemEditor.setValue(this.record.data.locationid);
        }
        this.StockValuationDetailStore.removeAll();
//        this.StockValuationDetailStore.load({
//                params:{
//                    start:0,
//                    limit:30,
//                    type:this.locTypeEditor.getValue(),
//                    locationid :this.locItemEditor.getValue(),
//                    batchid :this.batchEditor.getValue()
//                }
//            });
        this.locItemEditor.fireEvent("select");
    },this);

    this.batchRec = new Wtf.data.Record.create([
        {name:"id"},
        {name:"name"}
    ]);

    this.batchReader = new Wtf.data.KwlJsonReader({
        root:"data"
    },this.batchRec);

    this.batchStore = new Wtf.data.Store({
        url:"ACCMaster/getNewBatches.do",
        reader:this.batchReader
    });

    this.batchEditor = new Wtf.form.ComboBox({
        triggerAction:'all',
        mode: 'local',
        fieldLabel:WtfGlobal.getLocaleText("acc.inventorysetup.batch"),
        valueField:'id',
        id:"batchid",
        displayField:'name',
        store:this.batchStore,
        disabled:true,
        anchor:'60%',
        width:100,
        listWidth:200,
        typeAhead: true,
        hidden : this.isCustomWidgetReport,
        forceSelection: true,
        name:'batch',
        hiddenName:'batch'
    });

    this.batchStore.load({
        params:{
            transType:2,
            isOnlyBatch:this.locTypeEditor.getValue()=="" && this.locItemEditor.getValue()==""?"true":"false"
        }
    });

    this.batchStore.on("load",function(){
        var record = new Wtf.data.Record({
            id: "",
            name: "All Records"
        });
    this.batchStore.each(function(record){
        if(record.data.name==undefined || record.data.name=="" ){
                this.batchStore.remove(record)
            }
    },this);
        this.batchStore.insert(0, record);
        this.batchEditor.setValue("");
    }, this);
    this.fetchBttn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  // 'Fetch',
        tooltip:WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"), // "Select a time period to view corresponding transactions.",
        style:"margin-left: 6px;",
        iconCls:'accountingbase fetch',
        scope:this,
        handler:this.fetchStatement
    });
    
     this.productCategoryRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
        this.productCategoryStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 19
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.productCategoryRec)
        });
        this.productCategory = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.masterConfig.19"),
            hiddenName: 'id',
            name: 'id',
            hidden: this.iscustreport,
            store: this.productCategoryStore,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            typeAhead: true,
            triggerAction: 'all',
            hideLabel: true,
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseselectaproductcategory"),
            width: 100,
            listWidth: 150
        });
        
    this.productCategoryStore.load();
    this.productCategoryStore.on("load", function() {
        var record = new Wtf.data.Record({
            id: "",
            name: "All Records"
        });
        this.productCategoryStore.insert(0, record);
        this.productCategory.setValue("");
    }, this);
        

    this.locItemEditor.on("select", function(combo, rec, index){
        if(this.locTypeEditor.getValue()==2){
            this.batchStore.baseParams.location = this.locItemEditor.getValue()
        }
        if(this.locTypeEditor.getValue()==1){
            this.batchStore.baseParams.warehouse = this.locItemEditor.getValue()
        }
        if(this.locTypeEditor.getValue()==3){
            this.batchStore.baseParams.row = this.locItemEditor.getValue()
        }
        if(this.locTypeEditor.getValue()==4){
            this.batchStore.baseParams.rack = this.locItemEditor.getValue()
        }
        if(this.locTypeEditor.getValue()==5){
            this.batchStore.baseParams.bin = this.locItemEditor.getValue()
        }
        if(this.locTypeEditor.getValue()!=""){
            this.batchStore.baseParams.isOnlyStkRprt="true"
        }else{
            this.batchStore.baseParams.isOnlyStkRprt="false"
        }
//        var batchBox=Wtf.getCmp('batchid');
        var batchBox=this.batchEditor;
        batchBox.enable();
        this.batchStore.load();
    },this);

    this.exportButton=new Wtf.exportButton({
        obj:this,
        id:"exportReports"+this.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
        disabled :true,
        scope : this,
        filename:this.title+"_v1",
        hidden : this.isCustomWidgetReport,
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,
            xls:true
        },
        params:{
//            enddate :  WtfGlobal.convertToGenericDate(this.endDate.getValue()),
//            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
////            start:0,
////            limit:this.pP.combo.value,
//            isStockValuationLoc : true
        },
        get:Wtf.autoNum.StockValuationDetails
    });
    this.exportButton.setParams({
            enddate :  WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            isStockValuationLoc : true
    });
    this.GridRec = Wtf.data.Record.create([
        {name:'productid'},
        {name:'productname'},
        {name:'productDesc'},
        {name:'productCategory'},
        {name:'productBatch'},
        {name:'quantity'},
        {name:'rate'},
        {name:'avglandedcost'},  //landed cost feature additional column
        {name:'value'},
        {name:'currencysymbol'},
        {name:'currencyname'},
        {name:'categoryName'},
        {name:'locationName'},
        {name:'serial'},
        {name:'totalcategorycost'},
        {name:'uom'}
    ]);
    
    this.StockValuationDetailStore = new Wtf.ux.grid.MultiGroupingStore({
        url:"ACCProductCMN/getStockValuationDetailReport.do",
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.GridRec),
        autoLoad: false,
        groupField:['productid']
        //sortInfo: {field: 'productCategory',direction: "ASC"}
    });
    var fromdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
    var todate = WtfGlobal.convertToGenericDate(this.endDate.getValue());

    this.StockValuationDetailStore.load({
        params:{
            start:0,
            limit:30,
            type:this.locTypeEditor.getValue(),
            locationid :this.locItemEditor.getValue(),
            batchid:Wtf.account.companyAccountPref.isBatchCompulsory?this.batchEditor.getValue():"",
            batchname:Wtf.account.companyAccountPref.isBatchCompulsory?(this.batchEditor.getRawValue()=='All Records'?"":this.batchEditor.getRawValue()):"",
            startdate:fromdate,
            enddate:todate,
            isactivatelandedinvamt:Wtf.account.companyAccountPref.isActiveLandingCostOfItem


        }
    });
    this.exportButton.enable();
    this.batchEditor.enable();
    this.StockValuationDetailStore.on('beforeload', function() {
        var fromdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        var todate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        this.StockValuationDetailStore.baseParams.startdate = fromdate;
        this.StockValuationDetailStore.baseParams.enddate = todate;
        this.StockValuationDetailStore.baseParams.productcategory = this.productCategory.getValue();
        this.StockValuationDetailStore.baseParams.locationid = this.locItemEditor.getValue(),
        this.StockValuationDetailStore.baseParams.batchid = Wtf.account.companyAccountPref.isBatchCompulsory?this.batchEditor.getValue():"",
        this.StockValuationDetailStore.baseParams.batchname = Wtf.account.companyAccountPref.isBatchCompulsory?(this.batchEditor.getRawValue()=='All Records'?"":this.batchEditor.getRawValue()):"",
        this.StockValuationDetailStore.baseParams.type=this.locTypeEditor.getValue()
        this.StockValuationDetailStore.baseParams.isactivatelandedinvamt=Wtf.account.companyAccountPref.isActiveLandingCostOfItem;

        this.exportButton.setParams({
            enddate :  WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            isStockValuationLoc : true, 
            locationid: this.locItemEditor.getValue(),
            batchid:this.batchEditor.getValue(),
            batchname : this.batchEditor.getRawValue()=='All Records'?"":this.batchEditor.getRawValue(),
            type:this.locTypeEditor.getValue(),
            productcategory:this.productCategory.getValue()
        });
        this.exportButton.enable();
    }, this);

    this.StockValuationDetailStore.on('load', function(store) {
        WtfGlobal.resetAjaxTimeOut();
        if(this.StockValuationDetailStore.getCount() < 1) {
//            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        this.quickPanelSearch.StorageChanged(store);

        if(this.StockValuationDetailStore.data.items.length>0&&this.StockValuationDetailStore.data.items[0].data!=undefined&&this.StockValuationDetailStore.data.items[0].data.currencysymbol!=undefined)
            for(var i=0;i<this.cm.config.length;i++){
                if(this.cm.config[i].dataIndex=="value"&&this.StockValuationDetailStore.data.items.length>0&&this.StockValuationDetailStore.data.items[0].data!=undefined&&this.StockValuationDetailStore.data.items[0].data.currencysymbol!=undefined&&this.StockValuationDetailStore.data.items[0].data.currencysymbol!=""){
                    this.cm.setColumnHeader(i, WtfGlobal.getLocaleText("acc.stockLedger.Value")+ " ("+this.StockValuationDetailStore.data.items[0].data.currencyname+")")
                }
            }
        this.grid.getView().refresh();
    }, this);


    this.totalValuationValue = new Wtf.Toolbar.TextItem(""); // total value
    this.bbar1 = new Array();
    this.bbar1.push('->',"<B> Total Valuation :</B>",this.totalValuationValue);
    this.StockValuationDetailStore.on('datachanged', function(store) {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
        var grandTotalInBaseCurrency= 0;
        var recordindex=store.data.length-1;
        for(var i=0;i <= recordindex;i++){
            if(store.getAt(i).data.value!=undefined){
                grandTotalInBaseCurrency=parseFloat(grandTotalInBaseCurrency) +  parseFloat(store.data.items[i].data.value);
            }
        }
        this.totalValuationValue.getEl().innerHTML ="<B>" + WtfGlobal.conventInDecimal(grandTotalInBaseCurrency,WtfGlobal.getCurrencySymbol()) +"</B>";
    }, this);

    var gridSummary = new Wtf.grid.GroupSummary({}); //new Wtf.ux.grid.GridSummary(); //new Wtf.grid.GroupSummary({});

    this.rowNo=new Wtf.grid.RowNumberer();

    this.selModel = new Wtf.grid.RowSelectionModel({
        singleSelect : true
    });

    var groupView = new Wtf.ux.grid.MultiGroupingView({
        //forceFit: true,
        showGroupName: false,
        enableNoGroups: false,
        isGrandTotal: false,
        isGroupTotal: false,
        hideGroupedColumn:false,
        emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec")),
        groupTextTpl: 'Product ID: {group} '
    });

    this.cm= new Wtf.grid.ColumnModel([{
            header:WtfGlobal.getLocaleText("acc.cust.Productcategory"), // "Category",
            dataIndex:"productCategory",
            pdfwidth:80,
            hidden:true
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridProductID"), // "Product ID",
            dataIndex:"productid",
            width : 200,
            pdfwidth:80
//            hidden:true
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"), // "Product Name",
            dataIndex:"productname",
            width : 200,
            pdfwidth:80
        },{
            header:WtfGlobal.getLocaleText("acc.product.uom"), // uom,
            dataIndex:"uom",
            width : 100,
            pdfwidth:80
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"), // "Product Desc",
            dataIndex:"productDesc",
            width : 200,
            pdfwidth:80
        },{
            header:WtfGlobal.getLocaleText("acc.masterConfig.12"), // "Location",
            pdfwidth:80,
            width : 150,
            dataIndex:"locationName"
        },{
            header:WtfGlobal.getLocaleText("acc.inventorysetup.batch"), // "Batch",
            pdfwidth:80,
            width : 120,
            dataIndex:"productBatch"
        },{
            header:"Serial", // "Batch",
            pdfwidth:80,
            width : 180,
            dataIndex:"serial"
        },{
            header:WtfGlobal.getLocaleText("acc.product.gridQty"), // "Quantity",
            dataIndex:"quantity",
            pdfwidth:80,
            width : 120,
            align:"right",
            renderer:this.unitRenderer
        },{
            header: Wtf.account.companyAccountPref.isActiveLandingCostOfItem ? WtfGlobal.getLocaleText("acc.stockLedger.averagecost") : WtfGlobal.getLocaleText("acc.field.Rate"), // "Rate",
            dataIndex:"rate",
            pdfwidth:80,
            renderer:WtfGlobal.withCurrencyUnitPriceRenderer,
            align:"right",
            summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
        },{
            header: WtfGlobal.getLocaleText("acc.landingcost.avglandedcost"),
            dataIndex: "avglandedcost",
            align: "right",
            pdfrenderer: "rowcurrency",
            hidden:!Wtf.account.companyAccountPref.isActiveLandingCostOfItem,
            renderer: WtfGlobal.withCurrencyUnitPriceRenderer,
            width: 120,
            pdfwidth: 80
        },{
            header:WtfGlobal.getLocaleText("acc.stockLedger.Value"), // "Value",
            dataIndex:"value",
            align:"right",
            summaryType: 'sum',
            width: 140,
            pdfwidth:80,
            summaryRenderer: function(value, m, rec) {
                var retVal = WtfGlobal.withoutRateCurrencySymbol(value)
                return '<b>'+retVal+'</b>';
            },
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        }]);

    this.grid = new Wtf.ux.grid.MultiGroupingGrid({
        store:this.StockValuationDetailStore,
        //        border:false,
        //        stripeRows :true,
        loadMask:true,
        bbar:this.bbar1,
        sm: this.selModel,
        cm:this.cm,
        cls : 'colWrap',
        view: groupView,
        plugins:[gridSummary]
    });

    Wtf.account.TransactionListPanelViewStockValuationDetailReport.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.TransactionListPanelViewStockValuationDetailReport,Wtf.Panel, {    
    onRender: function(config){
        var tBarArray = [];

        tBarArray.push("-", this.quickPanelSearch,"-",this.resetBttn,"-",WtfGlobal.getLocaleText("acc.common.from"),"-",this.startDate,"-", WtfGlobal.getLocaleText("acc.common.to"),"-",this.endDate,"-",WtfGlobal.getLocaleText("acc.cust.Productcategory"),this.productCategory);
        
        if(this.isCustomWidgetReport !== true){
            tBarArray.push("-", WtfGlobal.getLocaleText("acc.field.Select"),"-",this.locTypeEditor,"-",this.locItemEditor);
        }
        if(Wtf.account.companyAccountPref.isBatchCompulsory && this.isCustomWidgetReport !== true){
            tBarArray.push("-",WtfGlobal.getLocaleText("acc.inventorysetup.batch"),this.batchEditor);
        }
        tBarArray.push("-",this.fetchBttn,"-", this.exportButton);
        
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid],
                    tbar: tBarArray,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.StockValuationDetailStore,
                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({
                        id : "pPageSize_"+this.id
                        })
                    })
                }]
        });
        this.add(this.leadpan);
        this.grid.on("render", function (grid) {
            this.grid.getView().applyEmptyText();
            new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
                this.grid.on('statesave', this.saveGridStateHandler, this);
            }, this);
        }, this);
        this.getGridConfig();
        Wtf.account.TransactionListPanelViewStockValuationDetailReport.superclass.onRender.call(this,config);
    },
    saveGridStateHandler: function (grid, state) {//To save config details when we hide or show columns
        WtfGlobal.saveGridStateHandler(this, grid, state, Wtf.Stock_Valuation_Report_Grid_Id, grid.gridConfigId, false);
    },
    getGridConfig:function(){//To load config details
        WtfGlobal.getGridConfig(this.grid, Wtf.Stock_Valuation_Report_Grid_Id, false, false);
    },    
    handleResetClick:function() {
        if(this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.fetchStatement();
        }
    },
    
    unitRenderer:function(value,metadata,record){
    	var unit=record.data['uomname'];
            value=parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        return value;
    },

    fetchStatement:function() {
        this.sDate=this.startDate.getValue();
        this.eDate=this.endDate.getValue();
        
        if(this.sDate > this.eDate){
            WtfComMsgBox(1,2);
            return;
        }

        var fromdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        var todate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        if(Wtf.account.companyAccountPref.isBatchCompulsory&&this.batchEditor.getRawValue()==""){
             WtfComMsgBox(["Warning","Please select Batch"],0);
            return;
        }
        this.StockValuationDetailStore.on('beforeload', function() {
            WtfGlobal.setAjaxTimeOut();
        }, this);
        this.StockValuationDetailStore.load({
            params: {
                startdate:fromdate,
                enddate:todate,
                start:0,
                limit:this.pP.combo.value,
                type:this.locTypeEditor.getValue(),
                ss:this.quickPanelSearch.getValue(),
                locationid :this.locItemEditor.getValue(),
                productcategory:this.productCategory.getValue(),
                batchid :Wtf.account.companyAccountPref.isBatchCompulsory?(this.batchEditor.getValue()==""?"All Records":this.batchEditor.getValue()):"",
                isOnlyBatch:this.locTypeEditor.getValue()=="" && this.locItemEditor.getValue()==""?"true":"false"
            }
        });
          this.StockValuationDetailStore.on('loadexception', function() {
            WtfGlobal.resetAjaxTimeOut();
        }, this);
    }
});