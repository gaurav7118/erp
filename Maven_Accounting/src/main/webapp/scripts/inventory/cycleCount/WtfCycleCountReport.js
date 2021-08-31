
Wtf.CCReportTab = function (config){
    Wtf.apply(this,config);
    Wtf.CCReportTab.superclass.constructor.call(this);
}

Wtf.extend(Wtf.CCReportTab,Wtf.Panel,{
    onRender:function (config) {
        Wtf.CCReportTab.superclass.onRender.call(this,config);
        this.getTabpanel();
        this.add(this.tabPanel);
    },
    getTabpanel:function (){
        this.itemsarr = [];

        this.getCCStatusReport();
        this.getCCReport();
            
        this.itemsarr.push(this.ccReport, this.statusReport);
        
        this.tabPanel = new Wtf.TabPanel({
            activeTab:0,
            items:this.itemsarr,
            border:false
        });
    },
    getCCStatusReport:function(){
        this.statusReport =new Wtf.inventory.cycleCountStatusReport({
            layout:"fit",
            title:WtfGlobal.getLocaleText("acc.stockavailability.CycleCountStatus"),
            border:false
        });
    },
    getCCReport:function (){
        this.ccReport =new Wtf.inventory.cycleCountReport({
            layout:"fit",
            title:WtfGlobal.getLocaleText("acc.productList.cycleCountReport"),
            border:false
        });
    }
});

Wtf.inventory.cycleCountStatusReport = function(config){
    Wtf.inventory.cycleCountStatusReport.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.inventory.cycleCountStatusReport, Wtf.Panel, {
    onRender: function(config){
        Wtf.inventory.cycleCountStatusReport.superclass.onRender.call(this, config);
        this.createReportGrid();
        this.add(this.grid);
    },
    createReportGrid: function() {
        
        this.businessDate = new Wtf.form.DateField({
            fieldLabel: "Business Date*",
            emptyText: "Select a date...",
            format: 'Y-m-d l',
            allowBlank: false,
            name: "businessDate",
            value: new Date(),
            width:200
        });
        this.businessDate.on('change', function(){
            this.loadGrid()
        }, this)
        var gridRec = new Wtf.data.Record.create([
        {
            name:"storeCode"
        },
        {
            name:"storeDesc"
        },
        {
            name:"itemCount"
        }
        ]);
        
        var reader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },gridRec);
        
        this.gridStore = new Wtf.data.Store({
            url:"INVCycleCount/getCycleCountStatusReport.do",
            
            reader: reader
        });
        
        this.sm = new Wtf.grid.RowSelectionModel({
            width:25,
            singleSelect:true
        });
        var defaultWidth = 150
        this.cm = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header:WtfGlobal.getLocaleText("acc.product.threshold.grid.storecode"),
                dataIndex:"storeCode",
                width: defaultWidth,
                renderer: function(v){
                    return "<div wtf:qtip='"+v+"'>"+v+"</div>"
                }
            },{
                header:WtfGlobal.getLocaleText("acc.cosignmentloan.StoreDescription"),
                dataIndex:"storeDesc",
                width: defaultWidth,
                renderer: function(v){
                    return "<div wtf:qtip='"+v+"'>"+v+"</div>"
                }
            },{
                header:WtfGlobal.getLocaleText("acc.CycleCountStatus.ItemCount"),
                dataIndex:"itemCount",
                width: defaultWidth,
                renderer: function(v){
                    return "<div wtf:qtip='"+v+"'>"+v+"</div>"
                }
            },{
                header:WtfGlobal.getLocaleText("acc.GIRO.Status"),
                dataIndex:"status",
                width: defaultWidth,
                renderer: function(v, m, r){
                    var status = 'Pending'
                    if(r.get('itemCount') > 0){
                        status = "<div style= 'color:green'>Done</div>"
                    }
                    return status;
                }
            }]);
        var tbarArr = [];
        tbarArr.push(WtfGlobal.getLocaleText("acc.stock.BusinessDate")+": ", this.businessDate)
        this.pg = new Wtf.PagingSearchToolbar({
            pageSize:30,//Wtf.companyPref.recperpage,
            border: false,
            displayInfo: true,
            displayMsg: 'Displaying records {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
            store: this.gridStore,
            plugins: this.pPageSizeObj = new Wtf.common.pPageSize({
                id: "pPageSize_" + this.id
            })
        })
        
        this.grid=new Wtf.grid.EditorGridPanel({
            region: 'center',
            border: false,
            store: this.gridStore,
            cm: this.cm,
            sm:this.sm,
            loadMask : true,
            layout:'fit',
            scrollable: true,
            viewConfig: {
                forceFit: true
            },
            tbar:tbarArr,
            bbar: this.pg
        })
        this.loadGrid();
    },
    loadGrid: function(){
        this.gridStore.baseParams.businessDate  = this.businessDate.getValue().format('Y-m-d')
        this.gridStore.load({
            params:{
                start:0,
                limit:this.pg.pageSize
            }
        });
    }
});

Wtf.inventory.cycleCountReport = function(config){
    Wtf.inventory.cycleCountReport.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.inventory.cycleCountReport, Wtf.Panel, {
    onRender: function(config){
        Wtf.inventory.cycleCountReport.superclass.onRender.call(this, config);
        this.moduleid=Wtf.Acc_CycleCount_ModuleId;
        this.createReportGrid();
        this.addAdvanceSearchComponent();
        this.createPanel();
        this.add(this.leadpan);
    },
    createPanel: function() {
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [this.objsearchComponent,
                {
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid]
                }]
        });
    },
    createReportGrid: function() {
        var prevdt = new Date().add(Date.DAY, 0);
        var minDt="",maxDt="";
        this.fromDate = new Wtf.form.DateField({
            fieldLabel: "From Date*",
            emptyText: "Select from date...",
            format: 'Y-m-d l',
            allowBlank: false,
            name: "fromBusinessDate",
            value: WtfGlobal.getDates(true),
            minValue: Wtf.archivalDate,
            width:150
        });
        this.toDate = new Wtf.form.DateField({
            fieldLabel: "To Date*",
            emptyText: "Select to date...",
            format: 'Y-m-d l',
            allowBlank: false,
            name: "toBusinessDate",
            value: WtfGlobal.getDates(false),
            minValue: Wtf.archivalDate,
            width:150
        });

        this.search = new Wtf.Button({
            anchor: '90%',
            text: WtfGlobal.getLocaleText("acc.common.search"),
            iconCls: 'pwnd editicon',
            scope: this,
            handler:this.loadItemList
        });

        this.storeCmbRecord = new Wtf.data.Record.create([{
            name: 'store_id'
        },{
            name: 'abbrev'
        },{
            name: 'fullname'
        }]);

        this.storeCmbStore = new Wtf.data.Store({
            url:  'INVStore/getStoreListByUser.do',
            baseParams:{
//                isActive:true,
                excludeQARepair: true,
                includePickandPackStore:true
                
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:"count"
            },this.storeCmbRecord),
            sortInfo: {
                field: 'description',
                direction: 'ASC' 
            }
        });
        this.storeCmbStore.on('loadexception', function(){
            alert("In load exception");
        },this)
        
        this.storeCmb = new Wtf.form.ComboBox({
            fieldLabel : 'Store*',
            store : this.storeCmbStore,
            typeAhead:true,
            forceSelection:true,
            displayField:'fullname',
            valueField:'store_id',
            mode: 'local',
            width : 150,
            triggerAction: 'all',
            emptyText:WtfGlobal.getLocaleText("acc.CycleCountStatus.Selectcountstore"),
            value:'ALL',
            listWidth:300,
            tpl: new Wtf.XTemplate(
                '<tpl for=".">',
                '<div wtf:qtip = "{[values.fullname]}" class="x-combo-list-item">',
                '<div>{fullname}</div>',
                '</div>',
                '</tpl>')
        });
        this.storeCmb.store.on('load', function(s){
            if(s.getCount() > 0){
                if(s.getCount() >= 2){
                    var rec = new this.storeCmbRecord({
                        store_id : "",
                        abbrev : "ALL",
                        fullname : "ALL"
                    })
                    s.insert(0, [rec]);
                }
                this.storeCmb.setValue(s.getAt(0).get('store_id'))
                this.loadItemList();
            }
        }, this)
        this.storeCmb.store.load();

        this.reportRecord = Wtf.data.Record.create([{
            name: 'id'
        },{
            name:'transactionNo'
        },{
            name: 'storeId'
        },{
            name: 'storeCode'
        },{
            name: 'storeDesc'
        },{
            name: 'createdBy'
        },{
            name: 'businessDate'
        },{
            name: 'itemId'
        },{
            name: 'itemCode'
        },{
            name: 'itemName'
        },{
            name: 'itemDesc'
        },{
            name: 'packaging'
        },{
            name: 'casingUom'
        },{
            name: 'innerUom'
        },{
            name: 'looseUom'
        },{
            name: 'casingUomCnt'
        },{
            name: 'innerUomCnt'
        },{
            name: 'looseUomCnt'
        },{
            name: 'systemQty'
        },{
            name: 'actualQty'
        },{
            name: 'variance'
        },{
            name: 'variancePer'
        },{
            name: 'countedOn'
        },{
            name: 'reason'
        },{
            name: 'added',
            mapping:'extraItem'
       
        },{
            name:"isBatchForProduct"
        },
        {
            name:"isSerialForProduct"
        },
        {
            name:"isRowForProduct"
        },
        {
            name:"isRackForProduct"
        },
        {
            name:"isBinForProduct"
        },
        {
            name:'stockDetails'
        },
        {
            name:'customfield'
        }]);

        var itemlistReader = new Wtf.data.KwlJsonReader({
            root: 'data',
            totalProperty: 'count'
        }, this.reportRecord);

        this.tmplt =new Wtf.XTemplate(
            '<table cellspacing="1" cellpadding="0" style="margin-top:15px;width:100%;margin-bottom:40px;position:relative" border="0">',
            
            '<tr>',
            '<th style="padding-left:50px"><h2><b>No.</b></h2></th>',
            '<th ><h2><b>Location</b></h2></th>',
            
            '<tpl for="parent">',
            '<tpl if="this.isTrue(parent.isRowForProduct)==true">',  // batch
            '<th><h2><b>Row</b></h2></th>',
            '</tpl>',
            '</tpl>',
            
            '<tpl for="parent">',
            '<tpl if="this.isTrue(parent.isRackForProduct)==true">',  // batch
            '<th><h2><b>Rack</b></h2></th>',
            '</tpl>',
            '</tpl>',
            
            '<tpl for="parent">',
            '<tpl if="this.isTrue(parent.isBinForProduct)==true">',  // batch
            '<th><h2><b>Bin</b></h2></th>',
            '</tpl>',
            '</tpl>',
            
            '<tpl for="parent">',
            '<tpl if="this.isTrue(parent.isBatchForProduct)==true">',  // batch
            '<th><h2><b>Batch</b></h2></th>',
            '</tpl>',
            '</tpl>',
            
            '<tpl for="parent">',
            '<tpl if="this.isTrue(parent.isSerialForProduct)==true">',  // serial 
            '<th><h2><b>System Serials</b></h2></th>',
            '<th><h2><b>Counted Serials</b></h2></th>',
            '</tpl>',
            '</tpl>',
            
            '<th><h2><b>System Quantity</b></h2></th>',
            '<th><h2><b>Counted Quantity</b></h2></th>',
            '</tr>',
     
            '<tr><span  class="gridLine" style="width:94%;margin-left:45px;position: relative;top: 33px;"></span></tr>',
            
            '<tpl for="stockDetails">',
            '<tr>',
            '<td style="padding-left:50px"><p>{#}</p></td>',
            
            '<td ><p>{locationName}</p></td>',
            
            '<tpl if="this.isTrue(parent.isRowForProduct)==true">',  // Row
            '<td ><p>{rowName}</p></td>',
            '</tpl>',
            
            '<tpl if="this.isTrue(parent.isRackForProduct)==true">',  // Rack
            '<td ><p>{rackName}</p></td>',
            '</tpl>',
            
            '<tpl if="this.isTrue(parent.isBinForProduct)==true">',  // Bin
            '<td ><p>{binName}</p></td>',
            '</tpl>',
            
            '<tpl if="this.isTrue(parent.isBatchForProduct)==true">',  // batch
            '<td ><p>{batchName}</p></td>',
            '</tpl>',
            
            '<tpl if="this.isTrue(parent.isSerialForProduct)==true">',  //  serial 
            '<td ><p>{systemSerials}</p></td>',
            '<td ><p>{actualSerials}</p></td>',
            '</tpl>',
            
            //            '<tpl if="this.getQuantityDecimalPreciedValue(quantity)">', 
            '<td ><p>{[this.getQuantityDecimalPreciedValue(values.systemQty)]}</p></td>',
            '<td ><p>{[this.getQuantityDecimalPreciedValue(values.actualQty)]}</p></td>',
            //            '</tpl>',
             
            '</tr>',
            '</tpl>',
            '</table>',
            {  
                isTrue: function(isSerialForProduct){
                    return isSerialForProduct;
                },
                getQuantityDecimalPreciedValue : function (v){
                    //alert(parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
                    return parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                }
            }
            );    
        
        this.expander = new Wtf.grid.RowExpander({
            tpl :this.tmplt,
            renderer : function(v, p, record){
                if(record.get("stockDetails").length>0){ //means has stock detail data
                    return  '<div class="x-grid3-row-expander">&#160;</div>'
                }else{
                    return  ''
                }
            }
        });
        var customArr = [];
        customArr.push(       
            this.expander,
            new Wtf.KWLRowNumberer(),
            {
                header: WtfGlobal.getLocaleText("acc.reval.transaction"),
                dataIndex: 'transactionNo',
                pdfwidth: 50
            },{
                header: WtfGlobal.getLocaleText("acc.stock.BusinessDate"),
                dataIndex: 'businessDate',
                pdfwidth: 50
            },{
                header: WtfGlobal.getLocaleText("acc.product.threshold.grid.storecode"),
                dataIndex: 'storeCode',
                sortable: true,
                pdfwidth: 50
            },{
                header: WtfGlobal.getLocaleText("acc.cosignmentloan.StoreDescription"),
                dataIndex: 'storeDesc',
                sortable: true,
                pdfwidth: 100
            },{
                header: WtfGlobal.getLocaleText("acc.nee.69"),
                dataIndex: 'createdBy',
                pdfwidth: 100
            },{
                header: WtfGlobal.getLocaleText("acc.field.ProductCode"),
                dataIndex: 'itemCode',
                sortable: true,
                pdfwidth: 50
            },{
                header: WtfGlobal.getLocaleText("acc.contract.product.name"),
                dataIndex: 'itemName',
                sortable: true,
                pdfwidth: 100
            },{
                header: WtfGlobal.getLocaleText("acc.contractDetails.ItemDescription"),
                dataIndex: 'itemDesc',
                hidden:true,
                sortable: true,
                pdfwidth: 100
            },{
                header: WtfGlobal.getLocaleText("acc.product.packaging"),
                dataIndex: 'packaging',
                sortable: true,
                pdfwidth: 50,
                hidden : Wtf.account.companyAccountPref.UomSchemaType?false:true  // column is visible only when packaging schema is activated in sytem control
            },{ 
                header: WtfGlobal.getLocaleText("acc.CycleCountStatus.CasingUoMCount"),
                dataIndex: 'casingUomCnt',
                sortable: true,
                align:'right',
                pdfwidth: 50,
                hidden : Wtf.account.companyAccountPref.UomSchemaType?false:true,  // column is visible only when packaging schema is activated in sytem control
                renderer: function(a, b, c) {
                    if(c.data.casinguom != "-") { //&& Wtf.realroles[0] == 9
                        b.css = "starbucksEditableCell";
                    }
                    return a + "    " + c.get('casingUom');
                }
            },{
                header: WtfGlobal.getLocaleText("acc.CycleCountStatus.InnerUoMCount"),
                dataIndex: 'innerUomCnt',
                sortable: true,
                align:'right',
                pdfwidth: 50,
                hidden : Wtf.account.companyAccountPref.UomSchemaType?false:true,  // column is visible only when packaging schema is activated in sytem control
                renderer: function(a, b, c) {
                    if(c.data.inneruom != "-") { //&& Wtf.realroles[0] == 9
                        b.css = "starbucksEditableCell";
                    }
                    return a + "    " + c.get('innerUom');
                }
            },{
                header: WtfGlobal.getLocaleText("acc.CycleCountStatus.LooseUoMCount"),
                dataIndex: 'looseUomCnt',
                sortable: true,
                align:'right',
                pdfwidth: 50,
                renderer: function(a, b, c) {
                    if(c.data.looseuom != "-" ) {//&& Wtf.realroles[0] == 9
                        b.css = "starbucksEditableCell";
                    }
                    return a + "    " + c.get('looseUom');
                }
            },{
                header: WtfGlobal.getLocaleText("acc.field.ActualQuantity"),
                dataIndex: 'actualQty',
                sortable: true,
                scope: this,
                align:'right',
                pdfwidth: 50,
                renderer: function(a, b, c){
                    return a + " " + c.get('looseUom');
                }
            },{
                header: WtfGlobal.getLocaleText("acc.stock.SystemQuantity"),
                dataIndex: 'systemQty',
                sortable: true,
                align:'right',
                pdfwidth: 50,
                renderer: function(a, b, c){
                    return a + " " + c.get('looseUom');
                }
            },{
                header: WtfGlobal.getLocaleText("acc.field.Variance"),
                dataIndex: 'variance',
                sortable: true,
                pdfwidth: 50,
                renderer: function(a, b, c){
                    if(a != 0){
                        return "<div style='color:red'>Yes</div>"
                    }else{
                        return "No"
                    }
                }
            },{
                header: WtfGlobal.getLocaleText("acc.cc.5"),
                dataIndex: 'variance',
                sortable: true,
                align:'right',
                pdfwidth: 50,
                renderer: function(a, b, c){
                    return a + " " + c.get('looseUom');
                }
            },{
                header: WtfGlobal.getLocaleText("acc.CycleCountStatus.VariancePercentage"),
                dataIndex: 'variancePer',
                align:'right',
                pdfwidth: 50
            },{
                header: WtfGlobal.getLocaleText("acc.dnList.reason"),
                dataIndex: 'reason',
                pdfwidth: 100
            },{
                header: WtfGlobal.getLocaleText("acc.CycleCountStatus.CountedOn"),
                dataIndex: 'countedOn',
                pdfwidth: 50,
                renderer: function(v){
                    return '<div wtf:qtip ="'+v+'">'+v+'</div>'
                }
            });
        customArr = WtfGlobal.appendCustomColumn(customArr, GlobalColumnModel[this.moduleid], undefined, undefined, this.readOnly, this.isViewTemplate);
        this.itemlistCm = new Wtf.grid.ColumnModel(customArr);
        this.exportButton=new Wtf.exportButton({
            obj:this,
            tooltip:"Export Report",  //"Export Report details.",  
            params:{
                name: "Cycle Count Report"
            },
            menuItem:{
                csv:true,
                pdf:true,
                xls:true
            },
            get:Wtf.autoNum.CycleCountReport,
            label:"Export"
 
        })
        this.resetBtn = new Wtf.Button({
            anchor : '90%',
            text: WtfGlobal.getLocaleText("acc.inventory.QAAproval.ResetFilter"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.stock.ClicktoResetFilter")
            },
            iconCls:getButtonIconCls(Wtf.etype.resetbutton),
            scope:this,
            handler:function(){
                this.fromDate.setValue(WtfGlobal.getDates(true));
                this.toDate.setValue(WtfGlobal.getDates(false));
                this.storeCmb.setValue('');
                if(this.storeCmb.store.getCount > 0){
                    this.storeCmb.setValue(this.storeCmb.store.data.items[0].data.store_id);
                }
                
                this.grid.quickSearchTF.setValue("");
                this.loadItemList();
            }
        });
        this.itemlistStore = new Wtf.data.GroupingStore({
            url:  'INVCycleCount/getCycleCountReport.do',
            reader: itemlistReader,
            sortInfo: {
                field:'businessDate',
                direction:'DESC'
            },
            groupField:'businessDate'
        });
                
        var colModelArray = [];
        colModelArray = GlobalColumnModel[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray, this.itemlistStore);
        this.reportgenerate = new Wtf.Button({
            text:'Generate Report',
            scope:this,
            hidden: true,//Wtf.realroles[0] == 18,
            handler:function(){
                this.generateReport(77);
//                showReportStatus();
            }
        });
        var grpView = new Wtf.grid.GroupingView({
            forceFit: false,
            showGroupName: true,
            enableGroupingMenu: true
//            hideGroupedColumn: true
        });
        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), // "Advanced Search",
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), // 'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });
        this.grid = new Wtf.KwlEditorGridPanel({
            store: this.itemlistStore,
            cm: this.itemlistCm,
            loadMask : true,
            layout:'fit',
            border:false,
            view: grpView,
            plugins:[this.expander],
            searchLabel:WtfGlobal.getLocaleText("acc.dnList.searchText"),
            searchLabelSeparator:":",
            searchEmptyText:WtfGlobal.getLocaleText("acc.CycleCountStatus.SearchByProductCodeName"),
            serverSideSearch:true,
//            searchField:"itemcode",
            displayInfo:true,
            pazeSize:30,
            tbar: [WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.fromdate")+":", this.fromDate,WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.todate")+":", this.toDate, "-",WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore")+":", this.storeCmb, "-",this.search,"-",this.resetBtn,"-",this.AdvanceSearchBtn],
            bbar:[this.exportButton]
        });

    },
    addAdvanceSearchComponent: function() {
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.grid.colModel,
            moduleid: Wtf.Acc_CycleCount_ModuleId,
            advSearch: false,
            parentPanelSearch: this,
            ignoreDefaultFields: false,
            isAvoidRedundent: true
        });

        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
        this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    },
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
    },
    filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.itemlistStore.baseParams = {
            flag: 1,
            iscustomcolumndata: 1,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_CycleCount_ModuleId,
            filterConjuctionCriteria: this.filterConjuctionCrit,
            fromDate: this.fromDate.getValue().format('Y-m-d'),
            toDate: this.toDate.getValue().format('Y-m-d'),
            storeId: this.storeCmb.getValue()
        }
        this.itemlistStore.load({
            params: {
                start: 0,
                limit: Wtf.getCmp("paggintoolbar" + this.grid.id).pageSize
            }
        });
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.itemlistStore.baseParams = {
            flag: 1,
            iscustomcolumndata: 1,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_CycleCount_ModuleId,
            filterConjuctionCriteria: this.filterConjuctionCrit,
            fromDate: this.fromDate.getValue().format('Y-m-d'),
            toDate: this.toDate.getValue().format('Y-m-d'),
            storeId: this.storeCmb.getValue()
        }
        this.itemlistStore.load({
            params: {
                start: 0,
                limit: Wtf.getCmp("paggintoolbar" + this.grid.id).pageSize
            }
        });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },
    loadItemList: function(){
        this.itemlistStore.baseParams = {
            fromDate: this.fromDate.getValue().format('Y-m-d'),
            toDate: this.toDate.getValue().format('Y-m-d'),
            storeId:this.storeCmb.getValue()
        };
        this.itemlistStore.load({
            params:{
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize
            }
        });
    }

});
