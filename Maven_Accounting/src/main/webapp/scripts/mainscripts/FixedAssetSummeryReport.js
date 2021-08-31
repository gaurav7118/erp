/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

Wtf.account.AssetSummeryReport = function(config) {

    this.arr = [];
    this.id = config.id;
    this.btnArr = [];
    this.btnArr2 = [];
    this.bottombtnArr=[];
    
    this.depreciationCalculationType = Wtf.account.companyAccountPref.depreciationCalculationType;
    Wtf.apply(this, config);

    this.createGrid();
    this.CreateReportButtons();
    this.addButtonInArray();
    this.Store.on('beforeload', this.setFitleringParametersBeforeLoadingStore, this);
    this.Store.on('loadexception', this.callLoadException, this);
    Wtf.account.AssetSummeryReport.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.AssetSummeryReport, Wtf.Panel, {
    onRender: function(config) {

        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [this.objsearchComponent,{
                            region: 'center',
                            layout: 'fit',
                            border: false,
                            tbar: [this.firstToolBar],
                            items:[this.grid],
                            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                                pageSize: 30,
                                id: "pagingtoolbar" + this.id,
                                store: this.Store,
//                                searchField: this.quickPanelSearch,
                                displayInfo: true,
                                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No results to display",
                                plugins: this.pP = new Wtf.common.pPageSize({
                                    id: "pPageSize_" + this.id
                                })
                            })                           
                        }
            ]
        });
        this.add(this.leadpan);
        this.loadStore(Wtf.isFirstTimeLoad);
        Wtf.account.AssetSummeryReport.superclass.onRender.call(this, config);
    },
    handleStoreOnLoad: function(store) {
        WtfGlobal.resetAjaxTimeOut();
        this.loadMask.hide();
        if (!this.isConfigLoaded) {
        var columns = [];
        columns.push(this.sm);
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if (column.dataIndex == "cost_openingbalance" || column.dataIndex == "cost_addtions" || column.dataIndex == "cost_disposals" || column.dataIndex == "cost_closingbalance" 
            || column.dataIndex == "depreciation_openingbalance" || column.dataIndex == "depreciation_current" || column.dataIndex == "depreciation_disposals" || column.dataIndex == "depreciation_closingbalance"
            || column.dataIndex == "nbv") {
                column.renderer = WtfGlobal.withoutRateCurrencyDeletedSymbol;
                column.header  = column.header+"("+WtfGlobal.getCurrencyName()+")";
            } else if (column.renderer!=undefined){
                column.renderer=eval('(' + column.renderer+ ')');
            }
//            else if (column.dataIndex !== "dateofpurchase" || column.dataIndex !== "dateofinstallation") {
//                column.renderer = WtfGlobal.deletedRenderer;
//            }
            columns.push(column);
        });


        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();
        this.getMyConfig();
        this.isConfigLoaded = true;
    }
        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.asset.depreciation.alert.msg"));
            this.grid.getView().refresh();
        }
        
        if(store.getCount()==0){
            if(this.exportButton)this.exportButton.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.exportButton)this.exportButton.enable();
            if(this.printButton)this.printButton.enable();
        }
        Wtf.MessageBox.hide();
//        this.quickPanelSearch.StorageChanged(store);
    },
    loadStore: function(isFirstTimeLoad) {
        isFirstTimeLoad = isFirstTimeLoad ? Wtf.isFirstTimeLoad : false;
        this.Store.load({
            params: {
                start: 0,
                limit: this.pP.combo != undefined ? this.pP.combo.value : 30,
//                ss: this.quickPanelSearch.getValue(),
                pagingFlag: true,
                isFirstTimeLoad: isFirstTimeLoad
            }
        });

    },
    getMyConfig: function () {
        WtfGlobal.getGridConfig(this.grid, this.moduleid, false, true, false);
    },
    saveMyStateHandler: function (grid, state) {
        WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleid, grid.gridConfigId, false);
    }, 
    storeloaded:function(store){
        WtfGlobal.resetAjaxTimeOut();
        if(store.getCount()==0){
            if(this.exportButton)this.exportButton.disable();
        }else{
            if(this.exportButton)this.exportButton.enable();
        }
        Wtf.MessageBox.hide();
//        this.quickPanelSearch.StorageChanged(store);
    },
    fetchStatement: function() {
        if (!this.checkDates()) {
            return;
        }
        
        this.Store.load({
            params: {
                start: 0,
                limit: this.pP.combo != undefined ? this.pP.combo.value : 30,
//                ss: this.quickPanelSearch.getValue(),
                pagingFlag: true,
                assetGroupIds :this.assetGroupCombo.getValue()==""?"All":this.assetGroupCombo.getValue()
            }
        });
    },
    handleResetClick: function() {
//        if (this.quickPanelSearch.getValue()) {
//            this.quickPanelSearch.reset();
//            this.loadStore();
//        }
    },
    createGrid: function() {
      
        this.loadMask = new Wtf.LoadMask(document.body,{
            msg : 'Loading...'
        });

        this.sm = new Wtf.grid.CheckboxSelectionModel({
        });
        this.Store = new Wtf.data.Store({
            url: "ACCAsset/getAssetSummeryReportDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: "totalCount",
                root: "data"
            })
        });
        
        this.grid = new Wtf.grid.GridPanel({
            store: this.Store,
            border: false,
            columns: [],
            layout: 'fit',
            sm: this.sm,
            viewConfig: {
                forceFit: false,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.asset.depreciation.alert.msg"))
            }
//            loadMask: true
        });
        this.grid.on("render", function (grid) {
            WtfGlobal.autoApplyHeaderQtip(grid);
            new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
                this.grid.on('statesave', this.saveMyStateHandler, this);
            }, this);
        }, this);
    },
    CreateReportButtons: function() {

        this.Store.on('load', this.handleStoreOnLoad, this);
        
//        this.quickPanelSearch = new Wtf.KWLTagSearch({
//            emptyText: "Search by Asset Name,Asset ID.", 
//            width: 200,
//            id: "quickSearch" + this.id,
//            field: 'transactionNumber'
//
//        });

//        this.resetBttn = new Wtf.Toolbar.Button({
//            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
//            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"),
//            id: 'btnRec' + this.id,
//            scope: this,
//            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
//            disabled: false,
//            handler: this.handleResetClick
//        });
        
        this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), // 'From',
            name: 'startdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: WtfGlobal.getDates(true)
        });

        this.endDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), // 'To',
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'enddate',
            value: WtfGlobal.getDates(false)
        });
        
        this.checkDates();
        
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"),
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
        
    this.exportButton = new Wtf.exportButton({
        obj: this,
        id: "exportButton" + this.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
        filename:  WtfGlobal.getLocaleText("acc.field.FixedAssetSummeryReport"),
        disabled: false,
        scope: this,
         menuItem: {
            csv: true,
            pdf: true,
            rowPdf: false,
            xls: true
        },
        params: {
            enddate: WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            depreciationCalculationType: this.depreciationCalculationType
        },
        get: Wtf.autoNum.AssetSummeryReport 
    });

        this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
            disabled :true,
            label:"Print",
            menuItem:{
                print:true
            },
            get:Wtf.autoNum.AssetSummeryReport
        });
        
        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });
        
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.grid.colModel,
            moduleid: this.moduleid,
            advSearch: false,
            ignoreDefaultFields: true,
            linelevelfields: true,
            reportid: Wtf.autoNum.AssetSummeryReport
        });
        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
        this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    },
    
    showAdvanceSearch: function() {
        showAdvanceSearch(this, this.searchparam, this.filterAppend);
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
        this.Store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleid,
            filterConjuctionCriteria: filterConjuctionCriteria,
            isFirstTimeLoad: false
        }
        this.Store.load({
            params: {
//                ss: this.quickPanelSearch.getValue(),
                start: 0,
                limit: (this.pP!=undefined && this.pP.combo!=undefined) ? this.pP.combo.value : 30
            }
        });
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleid,
            filterConjuctionCriteria: this.filterConjuctionCrit,
            isFirstTimeLoad: false
        }
        this.Store.load({
            params: {
//                ss: this.quickPanelSearch.getValue(),
                start: 0,
                limit: (this.pP!=undefined && this.pP.combo!=undefined) ? this.pP.combo.value : 30
            }
        });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },
    
    checkDates: function(){
        this.sDate=this.startDate.getValue();
        this.eDate=this.endDate.getValue();

        if(this.sDate=="" || this.eDate=="") {
            WtfComMsgBox(42,2);
            return false;
        }

        this.sdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        this.edate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        if(this.sDate>this.eDate){
            WtfComMsgBox(1,2);
            return false;
        }
        return true;
    },
    addButtonInArray: function() {
//        this.btnArr.push(this.quickPanelSearch);
//        this.btnArr.push(this.resetBttn);
        this.btnArr.push(WtfGlobal.getLocaleText("acc.common.from"));
        this.btnArr.push(this.startDate);
        this.btnArr.push(WtfGlobal.getLocaleText("acc.common.to"));
        this.btnArr.push(this.endDate);
        
         //Add Asset Group Combo to select different Group
        this.assetGroupStore = Wtf.FixedAssetStore;
        
        this.assetGroupComboConfig = {  
            emptyText:WtfGlobal.getLocaleText("acc.fxexposure.all"),
            name: 'productname',
            store:this.assetGroupStore,       
            typeAhead: true,
            selectOnFocus:true,
            valueField:'productid',
            displayField: 'productname',
            extraFields:['pid'],
            extraComparisionField:'pid',// type ahead search on acccode as well.
            lastQuery:'',
            triggerAction: 'all',
            scope:this,
            hirarchical:true
        };
           
        this.assetGroupCombo = new Wtf.common.Select(Wtf.applyIf({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.filed.SelectAssetGroups"),
            forceSelection:true,   
            listWidth:300,
            width:180
        },this.assetGroupComboConfig));
            
        this.btnArr.push("-",WtfGlobal.getLocaleText("acc.filed.SelectAssetGroups"),this.assetGroupCombo,"-");
        
        this.btnArr.push(this.fetchBttn);
        this.btnArr.push(this.exportButton);
        this.btnArr.push(this.printButton);
        this.btnArr.push(this.AdvanceSearchBtn);
        this.firstToolBar=new Wtf.Toolbar(this.btnArr);
    },
    setFitleringParametersBeforeLoadingStore: function (s, o) {
        WtfGlobal.setAjaxTimeOut();
        if (!o.params){
         o.params = {};
        }
        o.params.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue()); 
        o.params.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        o.params.assetGroupIds = this.assetGroupCombo.getValue()==""?"All":this.assetGroupCombo.getValue();
        o.params.depreciationCalculationType = this.depreciationCalculationType;

        this.loadMask.show();
    },
    callLoadException: function () {
        WtfGlobal.resetAjaxTimeOut();
        this.loadMask.hide();
    }
});
