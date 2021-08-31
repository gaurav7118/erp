/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.define('ReportBuilder.view.PivotReport', {
    extend: 'Ext.panel.Panel',
    xtype: 'report',
    layout: 'border',
    requires: [
    'ReportBuilder.view.AdvanceSearch',
    'ReportBuilder.extension.ExportableGrid',
    'ReportBuilder.store.ReportStore'
    ],
    
    initComponent: function() {
        this.isCollapsed = true;    //to handle expand-collapse button
        var me = this;
        me.createAdvanceSearchComponent();
        me.createReportGrid();
        
        Ext.apply(me, {
            items: [{
                region: 'north',
                layout:"fit",
                border:false,
                items : [this.advanceSearchGrid]
            }, {
                region: 'center',
                layout:"fit",
                items: [this.reportGrid]
            }]
        });
        
        this.on("beforeclose",function(){
           var reportListGrid = Ext.getCmp("idreportlistgrid");
           if(reportListGrid){
               reportListGrid.store.reload();
           }
        },this);
        this.callParent(arguments);
    },
    
    createReportGrid: function() {
        
        if(this.isDefault == "T" || this.moduleCategory === Ext.moduleCategoryType.Reports){
            
            var params = getURLAndParamsForDefaultReport(this.reportId);
            
            this.reportStore = Ext.create('Ext.data.Store', {
                model: 'ReportBuilder.model.CommonModel',
                autoLoad: false,
                pageSize: 0,
                proxy: {
                    type: 'ajax',
                    timeout : 1800000,
                    actionMethods : getStoreActionMethods(),
                    extraParams : {},
                    reader: {
                        type: 'json',
                        rootProperty: "data[\"data\"]",
                        keepRawData: true,
                        totalProperty: "data[\"count\"]"
                    }
                }
            });
            this.reportStore.on("beforeload", function() {
                if(this.moduleCategory != Ext.moduleCategoryType.Reports) {
                    this.reportStore.proxy.url = params.url;
                    this.reportStore.proxy.extraParams = params.params;                    
                }
                var startDate=ExtGlobal.convertToGenericStartDate(this.fromDate.getValue());
                var endDate=ExtGlobal.convertToGenericEndDate(this.toDate.getValue());
                this.reportStore.proxy.extraParams.startdate = startDate;
                this.reportStore.proxy.extraParams.enddate = endDate;
                this.reportStore.proxy.extraParams.asofdate = endDate;
                this.reportStore.proxy.extraParams.curdate = endDate;
                this.reportStore.proxy.extraParams.nondeleted = true
                this.reportStore.proxy.extraParams.isAged = true
            }, this);
            
            if(this.moduleCategory === Ext.moduleCategoryType.Reports) {
                this.reportStore.proxy.url = this.reportUrl;
                this.reportStore.proxy.extraParams = this.params != undefined ? this.params : {};
            }
        }else{
            this.reportStore = Ext.create('ReportBuilder.store.ReportStore');
            this.reportStore.setPageSize(0);    //set page-size as zero for no paging
         
            this.reportStore.on("beforeload", function() {
                this.reportStore.proxy.extraParams.reportID = this.reportId;
                this.reportStore.proxy.extraParams.fromDate = ExtGlobal.convertToGenericDate(this.fromDate.getValue());
                this.reportStore.proxy.extraParams.toDate = ExtGlobal.convertToGenericDate(this.toDate.getValue());
                this.reportStore.proxy.extraParams.gcurrencyid = Ext.pref.Currencyid;
                this.reportStore.proxy.extraParams.consolidateFlag = false;
                this.reportStore.proxy.extraParams.deleted = false;
                this.reportStore.proxy.extraParams.nondeleted = false;
                this.reportStore.proxy.extraParams.pendingapproval = false;
                this.reportStore.proxy.extraParams.showRowLevelFieldsflag = false;
                this.reportStore.proxy.extraParams.moduleid = this.moduleid;
                this.reportStore.proxy.extraParams.isreportloaded=true;
                if(this.advanceSearchGrid.advSearch && !this.advanceSearchGrid.hidden && this.advanceSearchGrid.getFilterJson().root.length!=0){
                this.reportStore.proxy.extraParams.searchJson = Ext.encode(this.advanceSearchGrid.getFilterJson());
                }

            }, this);
        }
        
        if(this.moduleCategory === Ext.moduleCategoryType.Reports) {
            this.customizedReportStore = Ext.create('Ext.data.Store', {
                model: 'ReportBuilder.model.CommonModel',
                autoLoad: false,
                pageSize: 0,
                proxy: {
                    type: 'ajax',
                    timeout : 1800000,
                    url : 'ACCCreateCustomReport/executeCustomizedReport.do',
                    actionMethods : getStoreActionMethods(),
                    reader: {
                        type: 'json',
                        rootProperty: "data",
                        keepRawData: true,
                        totalProperty: "totalCount"
                    }
                }
            });
            this.customizedReportStore.on("beforeload", function() {
                this.customizedReportStore.proxy.extraParams = {};
                this.customizedReportStore.proxy.extraParams.reportID = this.reportId;
                this.customizedReportStore.proxy.extraParams.reportData = JSON.stringify(this.reportStore.getProxy().getReader().rawData.data.data);
                this.customizedReportStore.proxy.extraParams.reportDataCount = this.reportStore.getProxy().getReader().rawData.data.count;
                this.customizedReportStore.proxy.extraParams.fromDate = ExtGlobal.convertToGenericDate(this.fromDate.getValue());
                this.customizedReportStore.proxy.extraParams.toDate = ExtGlobal.convertToGenericDate(this.toDate.getValue());
                this.customizedReportStore.proxy.extraParams.gcurrencyid = Ext.pref.Currencyid;
                this.customizedReportStore.proxy.extraParams.consolidateFlag = false;
                this.customizedReportStore.proxy.extraParams.deleted = false;
                this.customizedReportStore.proxy.extraParams.nondeleted = false;
                this.customizedReportStore.proxy.extraParams.pendingapproval = false;
                this.customizedReportStore.proxy.extraParams.showRowLevelFieldsflag = false;
                this.customizedReportStore.proxy.extraParams.parentReportId = this.parentReportId;
                this.customizedReportStore.proxy.extraParams.isreportloaded=true;     
                if(this.advanceSearchGrid.advSearch && !this.advanceSearchGrid.hidden && this.advanceSearchGrid.getFilterJson().root.length!=0 ){
                this.customizedReportStore.proxy.extraParams.searchJson = Ext.encode(this.advanceSearchGrid.getFilterJson());
                }
            }, this);
            
            this.store = this.customizedReportStore;
            
        } else {
            this.store = this.reportStore;            
        }
//        
//        this.reportStore.load({
//            params: {
//                reportID: this.reportId
//            }
//        });
//        
        this.reportStore.on("load", function() {
            if(this.moduleCategory === Ext.moduleCategoryType.Reports){
                this.customizedReportStore.load({
                    params: {}
                });
            }
        }, this);
        
        this.fromDate = new Ext.form.field.Date({
            fieldLabel:ExtGlobal.getLocaleText("acc.common.from") ,
            id: this.id + 'startdt',
            name: 'startdt',
            labelWidth: 30,
            width: 145,
            format: ExtGlobal.getOnlyDateFormat(),
            value: financialYearFromDate,
            vtype: 'daterange',
            endDateField: this.id + 'enddt'
        });
        this.toDate = new Ext.form.field.Date({
            fieldLabel: ExtGlobal.getLocaleText("acc.common.to") ,
            id: this.id + 'enddt',
            name: 'enddt',
            labelWidth: 20,
            width: 145,
            format: ExtGlobal.getOnlyDateFormat(),
            value: financialYearToDate,
            vtype: 'daterange',
            startDateField: this.id + 'startdt'
        });

        var btnArr = [];

        btnArr.push(this.fromDate, '-', this.toDate, '-', {
            xtype: 'button',
            text: ExtGlobal.getLocaleText("acc.common.fetch") ,
            iconCls: 'accountingbase fetch',
            scope: this,
            tooltip:ExtGlobal.getLocaleText("acc.invReport.fetchTT"),
            handler: function() {
                this.isReportReconfigure = false;  //to reconfigure pivot axes configuration
                this.reportStore.reload();
                this.expandCollapseBttn.setText('Expand all');  //to reset expand-collapse button
                this.isCollapsed = true;
                var normalGridView =  this.reportGrid.view;
                normalGridView.emptyText = "<div class='x-grid-empty'>" + "<div style='text-align:center;font-size:18px;'>" + ExtGlobal.getLocaleText("account.common.nodatadisplay") + "</div>" + "</div>";
            }
        });
        this.resetBttn = new Ext.Button({
            text:ExtGlobal.getLocaleText("acc.common.reset") ,
            scope: this,
            iconCls: "pwnd reset",
            tooltip: ExtGlobal.getLocaleText("acc.common.reset")+" date",
            disabled: false,
            handler: function() {
                this.fromDate.setValue(financialYearFromDate);
                this.toDate.setValue(financialYearToDate);
                this.reportStore.reload();
                this.expandCollapseBttn.setText('Expand all');  //to reset expand-collapse button
                this.isCollapsed = true;
            }
        });
        btnArr.push(this.resetBttn);
        this.exportBttn = new Ext.Button({
            text:ExtGlobal.getLocaleText("acc.common.export") ,
            scope: this,
            iconCls: "pwnd export",           
            menu: [{
                text: 'Export as XLSX',
                iconCls: "pwnd exportcsv",
                scope: this,
                handler: function() {
                    this.reportGrid.exportFile(this.title, this.isCollapsed);
                }
            }]
        });
        btnArr.push(this.exportBttn);
        
        this.AdvanceSearchBtn = new Ext.Button({
            text: ExtGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
            scope: this,
            tooltip: ExtGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });
                
        btnArr.push(this.AdvanceSearchBtn);
        
        this.expandCollapseBttn = new Ext.Button({
            text: 'Expand all',
            scope: this,
            handler: function() {
                this.expandCollapseHandler();
            }
        });
        
        this.SubtotalspositionBttn = new Ext.Button({
            text: 'Subtotals position',
            menu: {
                defaults: {
                    xtype: 'menucheckitem',
                    group: 'subtotals',
                    checkHandler: this.subtotalsHandler.bind(this)
                },
                items: [{
                    text: 'First',
                    checked: true
                }, {
                    text: 'Last'
                }, {
                    text: 'None'
                }]
            }
        });
        
        this.TotalspositionBttn = new Ext.Button({
            text: 'Totals position',
            menu: {
                defaults: {
                    xtype: 'menucheckitem',
                    group: 'totals',
                    checkHandler: this.totalsHandler.bind(this)
                },
                items: [{
                    text: 'First'
                }, {
                    text: 'Last',
                    checked: true
                }, {
                    text: 'None'
                }]
            }
        });
        
        btnArr.push("->",this.expandCollapseBttn, this.SubtotalspositionBttn, this.TotalspositionBttn);
        
//        this.store = (this.moduleCategory === Ext.moduleCategoryType.Reports) ? this.customizedReportStore : this.reportStore;
        
        this.reportGrid = Ext.create('ReportBuilder.extension.ExportableGrid', {
            leftAxis: [],
            topAxis: [],
            tbar : btnArr,
            rowGrandTotalsPosition: 'none',
            viewLayoutType: 'outline',
            //            startRowGroupsCollapsed: false,
            multiSelect: true,
            //            plugins: [{
            //                ptype:      'pivotconfigurator',
            //                pluginId:   'configurator',
            //                // It is possible to configure a list of fields that can be used to configure the pivot grid
            //                // If no fields list is supplied then all fields from the Store model are fetched automatically
            //                fields: []
            //            }],
            viewConfig: {
                deferEmptyText: false,
                emptyText: "<div style='text-align:center;font-size:18px;'>"+ ExtGlobal.getLocaleText("acc.common.norec.click.fetchbtn")+"</div>",
                trackOver: true,
                stripeRows: false
            },
            store: this.store,
            selModel: {
                type: 'rowmodel'
            }
        });
        this.isReportReconfigure = false;
        if(this.moduleCategory === Ext.moduleCategoryType.Reports) {
            this.customizedReportStore.on("load", this.handlegridonload, this);            
        } else {
            this.reportStore.on("load", this.handlegridonload, this);
        }
        if(this.reportRec.get("savedSearch")!=undefined && this.reportRec.get("savedSearch")!=""){
            var savedSearch = this.reportRec.get("savedSearch");
            var filterAppend = this.reportRec.get("filterAppend") == 1 ? "AND" : "OR";
            var savedSearchId = this.reportRec.get("savedSearchId");
            var json = eval("(" + decodeURIComponent(savedSearch) + ")");
            
            if(json.root.length > 0){
                this.configurAdvancedSearch();
                this.advanceSearchGrid.loadSearchRecords(json,filterAppend,savedSearchId);
            }
//            this.filterStore(this.reportRec.get("savedSearch"),filterAppend);
        }else{
//            this.reportStore.load({
//                params: {
//                reportID: this.reportId
//                }
//            });
        }
    },
    handlegridonload: function(store, record, success, opts) {
        this.store = (this.moduleCategory === Ext.moduleCategoryType.Reports) ? this.customizedReportStore : this.reportStore;
        this.reportGrid.setStore(this.store);
        if (this.store.getProxy().getReader().rawData.columns != undefined) {
            var userPreferences = this.store.getProxy().getReader().rawData.userPreferences;
            if (financialYearFromDate == undefined && financialYearToDate == undefined) {
                financialYearFromDate = userPreferences.fromdate;
                financialYearToDate = userPreferences.todate;

                this.fromDate.setValue(financialYearFromDate);
                this.toDate.setValue(financialYearToDate);
            }

            this.pivotConfig = this.store.getProxy().getReader().rawData.pivotConfig;
            this.pivotConfig.aggregate = ExtGlobal.setRenderersToPivotColumns(this.pivotConfig.aggregate);
            this.pivotConfig.topAxis = ExtGlobal.setRenderersToPivotColumns(this.pivotConfig.topAxis);
            this.pivotConfig.leftAxis = ExtGlobal.setRenderersToPivotColumns(this.pivotConfig.leftAxis);
            
            if (this.isReportReconfigure == false) {
                this.reportGrid.reconfigurePivot({
                    topAxis: this.pivotConfig.topAxis,
                    leftAxis: this.pivotConfig.leftAxis,
                    aggregate: this.pivotConfig.aggregate
                });
                this.isReportReconfigure = true;
            }
            this.isReportStoreRefresh = true;
            
        } else {
            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), this.store.getProxy().getReader().rawData.msg, Ext.Msg.INFO);
        }
        
        Ext.Function.defer(ExtGlobal.getReportConfig, 500, this, [this, this.reportId]);
        new Ext.util.DelayedTask().delay(2000, function () {
            this.reportGrid.on('columnresize', this.saveReportConfig, this);
            this.reportGrid.on('sortchange', this.saveReportConfigOnSortChange, this);
        }, this);
    },
    
    subtotalsHandler: function(button, checked){
        if(!checked) {
            return;
        }
        this.expandCollapseBttn.setText('Expand all');  //to reset expand-collapse button
        this.isCollapsed = true;
        // reconfigure the pivot grid with new settings
        this.reportGrid.reconfigurePivot({
            rowSubTotalsPosition: button.text.toLowerCase(),
            colSubTotalsPosition: button.text.toLowerCase()
        });
        this.saveReportConfig();
    },

    totalsHandler: function(button, checked){
        if(!checked) {
            return;
        }
        this.expandCollapseBttn.setText('Expand all');  //to reset expand-collapse button
        this.isCollapsed = true;
        // reconfigure the pivot grid with new settings
        this.reportGrid.reconfigurePivot({
            rowGrandTotalsPosition: button.text.toLowerCase(),
            colGrandTotalsPosition: button.text.toLowerCase()
        });
        this.saveReportConfig();
    },
    
    createAdvanceSearchComponent : function(){
        var customerCustomFieldFlag = false;
        var vendorCustomFieldFlag = false;
        var isAvoidRedundent = false;
        this.moduleIdForAdvSearch = this.moduleid;
        if(this.moduleCategory !== Ext.moduleCategoryType.Reports) {
            customerCustomFieldFlag = this.showCustomerCustomFieldFlag(this.moduleid);
            vendorCustomFieldFlag = this.showVendorCustomField(this.moduleid);
            isAvoidRedundent = this.isAvoidRedundent(this.moduleid);
        } else {
            if(this.parentReportId === Ext.Report_List_Ids.Sales_By_Product) {
                this.moduleIdForAdvSearch = Ext.Acc_Invoice_ModuleId;
            }
        }
        this.advanceSearchGrid =  Ext.create('ReportBuilder.view.AdvanceSearch',{
            hidden : true,
            moduleid : this.moduleIdForAdvSearch,
            advSearch: false,
            reportId : this.reportId,
            customerCustomFieldFlag: customerCustomFieldFlag,
            vendorCustomFieldFlag: vendorCustomFieldFlag,
            lineLevelSearch:false,
            isAvoidRedundent : isAvoidRedundent
        });
         this.advanceSearchGrid.on("filterStore", this.filterStore, this);
        this.advanceSearchGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    },
    
    configurAdvancedSearch: function() {
        this.advanceSearchGrid.show();
        this.advanceSearchGrid.advSearch = true;
        this.advanceSearchGrid.getComboData();
        this.AdvanceSearchBtn.disable();
    },
    
    clearStoreFilter: function() {
        this.store = this.reportStore;
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.store.proxy.extraParams.searchJson = null;
        this.store.proxy.extraParams.moduleid = this.moduleIdForAdvSearch;
        this.store.proxy.extraParams.filterConjuctionCriteria = null;
        
        this.store.load();
        this.expandCollapseBttn.setText('Expand all');  //to reset expand-collapse button
        this.isCollapsed = true;
        this.advanceSearchGrid.hide();
        this.AdvanceSearchBtn.enable();
    },
    filterStore: function(json, filterConjuctionCriteria) {
        this.store = this.reportStore;
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.store.proxy.extraParams.searchJson = this.searchJson;
        this.store.proxy.extraParams.moduleid = this.moduleIdForAdvSearch;
        this.store.proxy.extraParams.filterConjuctionCriteria = filterConjuctionCriteria;
        
        this.store.load();
        this.expandCollapseBttn.setText('Expand all');  //to reset expand-collapse button
        this.isCollapsed = true;
    },
    
    showCustomerCustomFieldFlag: function(moduleid){
        var moduleId = parseInt(moduleid);
        var customerCustomFieldFlag = false;
        if(moduleId===Ext.Acc_Invoice_ModuleId || moduleId===Ext.Acc_Sales_Order_ModuleId || moduleId===Ext.Acc_Customer_Quotation_ModuleId 
            || moduleId=== Ext.Acc_Sales_Return_ModuleId || moduleId=== Ext.Acc_Delivery_Order_ModuleId || moduleId=== Ext.Acc_Debit_Note_ModuleId || moduleId=== Ext.Acc_Credit_Note_ModuleId 
            || moduleid=== Ext.Acc_Receive_Payment_ModuleId  ){            
            customerCustomFieldFlag = true;
        }
        return customerCustomFieldFlag;
    },
    
    showVendorCustomField: function(moduleid){
        var moduleId = parseInt(moduleid);
        var vendorCustomFieldFlag = false;
        if(moduleId===Ext.Acc_Vendor_Invoice_ModuleId || moduleId===Ext.Acc_Purchase_Order_ModuleId || moduleId===Ext.Acc_Vendor_Quotation_ModuleId
            || moduleId===Ext.Acc_Purchase_Requisition_ModuleId || moduleId=== Ext.Acc_Debit_Note_ModuleId || moduleId===Ext.Acc_Goods_Receipt_ModuleId || moduleId=== Ext.Acc_Credit_Note_ModuleId){
            vendorCustomFieldFlag = true;
        }
        return vendorCustomFieldFlag;
    },
    
    isAvoidRedundent: function(moduleid){
        var moduleId = parseInt(moduleid);
        var isAvoidRedundentFlag = false;
        if(moduleId===Ext.Acc_Make_Payment_ModuleId || moduleId=== Ext.Acc_Receive_Payment_ModuleId ){
            isAvoidRedundentFlag = true;
        }
        return isAvoidRedundentFlag;
    },
    
    expandCollapseHandler: function(isSetGridConfigCall) {
        if(this.isCollapsed == true) {
            this.expandAll();
            this.expandCollapseBttn.setText('Collapse all');
            this.isCollapsed = false;
        } else {
            this.collapseAll();
            this.expandCollapseBttn.setText('Expand all');
            this.isCollapsed = true;
        }
        if(!isSetGridConfigCall) {
            this.saveReportConfig();
        }
    },
    
    expandAll: function(){
        this.reportGrid.expandAll();
    },

    collapseAll: function(){
        this.reportGrid.collapseAll();
    },
    
    saveReportConfig: function () {
        if (this.reportId) {
            ExtGlobal.saveReportConfig(this, true, this.gridConfigId, this.reportGrid.getState(), this.reportId);
        }
    },
    
    saveReportConfigOnSortChange: function (ct, column, direction) {
        if (this.reportId) {
            ExtGlobal.saveReportConfig(this, true, this.gridConfigId, this.reportGrid.getState(), this.reportId, column, direction);
        }
    },
    
    updateTotalPositions: function (state) {
        if(state.rowSubTotalsPosition.toLowerCase() == "first") {
            this.SubtotalspositionBttn.getMenu().items.items[0].checked = true;
            this.SubtotalspositionBttn.getMenu().items.items[1].checked = false;
            this.SubtotalspositionBttn.getMenu().items.items[2].checked = false;
        } else if(state.rowSubTotalsPosition.toLowerCase() == "last") {
            this.SubtotalspositionBttn.getMenu().items.items[0].checked = false;
            this.SubtotalspositionBttn.getMenu().items.items[1].checked = true;
            this.SubtotalspositionBttn.getMenu().items.items[2].checked = false;
        } else if(state.rowSubTotalsPosition.toLowerCase() == "none") {
            this.SubtotalspositionBttn.getMenu().items.items[0].checked = false;
            this.SubtotalspositionBttn.getMenu().items.items[1].checked = false;
            this.SubtotalspositionBttn.getMenu().items.items[2].checked = true;
        }
        
        if(state.rowGrandTotalsPosition.toLowerCase() == "first") {
            this.TotalspositionBttn.getMenu().items.items[0].checked = true;
            this.TotalspositionBttn.getMenu().items.items[1].checked = false;
            this.TotalspositionBttn.getMenu().items.items[2].checked = false;
        } else if(state.rowGrandTotalsPosition.toLowerCase() == "last") {
            this.TotalspositionBttn.getMenu().items.items[0].checked = false;
            this.TotalspositionBttn.getMenu().items.items[1].checked = true;
            this.TotalspositionBttn.getMenu().items.items[2].checked = false;
        } else if(state.rowGrandTotalsPosition.toLowerCase() == "none") {
            this.TotalspositionBttn.getMenu().items.items[0].checked = false;
            this.TotalspositionBttn.getMenu().items.items[1].checked = false;
            this.TotalspositionBttn.getMenu().items.items[2].checked = true;
        }
        
        if(state.isCollapsed != undefined && !state.isCollapsed) {
            this.isCollapsed = true;
            Ext.Function.defer(this.expandCollapseHandler, 500, this, [true]);
        }
    },
    
    sortGridData: function (sortedColumn) {
        if (sortedColumn.dataIndex) {
            var column = this.reportGrid.getColumnManager().getHeaderByDataIndex(sortedColumn.dataIndex);
            if ((column.leftAxis || column.topAxis) && !Ext.isEmpty(column.dataIndex)) {
                if (this.reportGrid.getMatrix().leftAxis.sortTreeByField(column.dataIndex, sortedColumn.direction)) {
                    this.reportGrid.refreshView();
                    this.reportGrid.updateColumnSortState(column, sortedColumn.direction);
                }
            }
        }
    },
    
    setReportStateConfig: function (state, isTotalPositionChange) {
        this.savedState = state = Ext.decode(state);
        if(state) {
            this.reportGrid.reconfigurePivot({
                rowSubTotalsPosition: state.rowSubTotalsPosition,
                colSubTotalsPosition: state.colSubTotalsPosition,
                rowGrandTotalsPosition: state.rowGrandTotalsPosition,
                colGrandTotalsPosition: state.colGrandTotalsPosition
            });
            
            if(state.pivotcolumns) {
                for(var i = 0; i < this.reportGrid.getView().getGridColumns().length; i++) {
                    var column = this.reportGrid.getView().getGridColumns()[i];
                    if(state.pivotcolumns[column.dataIndex]) {
                        column.width = state.pivotcolumns[column.dataIndex].width;
                    }
                }
                this.reportGrid.getView().refresh();
            }
            var grandTotalsPosition = 'last';
                 if (this.reportStore.getTotalCount() === 0)
                 {
                      grandTotalsPosition = 'none';
                 }
            this.reportGrid.getMatrix().rowGrandTotalsPosition = grandTotalsPosition;
            
            this.updateTotalPositions(state);
            
            if(state.sortedColumn) {
                Ext.Function.defer(this.sortGridData, 500, this, [state.sortedColumn]);
            }
        }
    }
});
