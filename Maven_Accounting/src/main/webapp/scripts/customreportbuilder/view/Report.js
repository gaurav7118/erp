/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.define('ReportBuilder.view.Report', {
    extend: 'Ext.panel.Panel',
    xtype: 'report',
    layout: 'border',
    requires: [
        'ReportBuilder.view.AdvanceSearch',
        'ReportBuilder.extension.CustomListFilter',
        'ReportBuilder.extension.LockedCheckBoxSelModel',
        'ReportBuilder.store.ReportStore'
    ],
    
    initComponent: function() {
        var me = this;
        me.createAdvanceSearchComponent();
        me.createReportGrid();
        me.createExpandStore();
        
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
    
//    onDestroy : function(){
//        
//        this.advanceSearchGrid.destroy();
//        this.reportGrid.destroy();
//        this.expandReportStore.destroy();
//        
//        this.callParent(arguments);
//    },
    
    createReportGrid: function() {
        this.enableRevertStatusBtn=false;
        if(this.isDefault == "T" || this.moduleCategory === Ext.moduleCategoryType.Reports){
            var params = getURLAndParamsForDefaultReport(this.reportId);
            this.reportStore = Ext.create('Ext.data.Store', {
                model: 'ReportBuilder.model.CommonModel',
                autoLoad: false,
                pageSize: 25,
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
                this.reportStore.proxy.extraParams.isChartRequest = false;
            }, this);
            
            if(this.moduleCategory === Ext.moduleCategoryType.Reports) {
                this.reportStore.proxy.url = this.reportUrl;
                if(this.params != undefined) {
                    this.reportStore.proxy.extraParams = this.params;
                }
            }
        }else{
            this.reportStore = Ext.create('ReportBuilder.store.ReportStore');
        
            this.reportStore.on("beforeload", function() {
                
                if(this.reportGrid!=undefined && this.reportGrid!="undefined" && this.reportGrid!=null && this.reportGrid.filters!=null&& this.reportGrid.filters!=undefined){
                    this.filterArray = [];
                    if (this.reportGrid.getStore().isFiltered()) {//check whether the store is filtered or not
                        var recordheaders = this.reportGrid.store.getFilters().items;
                        for (var j = 0; j < recordheaders.length; j++) {
                            var value = "";
                            var recordsdetails = this.reportStore.getProxy().getReader().rawData.columns;
                            var recordsdetail=recordsdetails.getIemtByParam({
                                id:recordheaders[j].getProperty()
                            })
                            if (recordsdetail.xtype == Ext.fieldType.dateField) {
                                value = ExtGlobal.convertToGenericDate(recordheaders[j].getValue());
                            } else {
                                value = recordheaders[j].getValue();
                            }
                            var recordjson = {
                                "property": recordheaders[j].getProperty(),
                                "value": value,
                                "operator": recordheaders[j].getOperator(),
                                "allowcrossmodule": recordsdetail.allowcrossmodule,
                                "crossJoinMainTable": recordsdetail.crossJoinMainTable
                            };
                            this.filterArray.push(recordjson);                                
                        }
                    }//end of filtered check
                    this.reportStore.proxy.extraParams.filter=JSON.stringify(this.filterArray)
                }
                
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
                this.reportStore.proxy.extraParams.isreportloaded=this.isreportloaded
                this.reportStore.proxy.extraParams.isChartRequest = false;
                this.reportStore.proxy.extraParams.isEWayReportValidation=false;
                if(this.advanceSearchGrid.advSearch && !this.advanceSearchGrid.hidden && this.advanceSearchGrid.getFilterJson().root.length!=0){
                this.reportStore.proxy.extraParams.searchJson = Ext.encode(this.advanceSearchGrid.getFilterJson());
                }
                if (this.statusFilter) {
                    this.reportStore.proxy.extraParams.ewayFilter = this.statusFilter.getValue();
                    if (this.statusFilter.getValue() == 'T') {
                        this.enableRevertStatusBtn = true;
                    } else {
                        this.enableRevertStatusBtn = false;
                    }
                }

            }, this);
        }
        if(this.moduleCategory === Ext.moduleCategoryType.Reports) {
            this.customizedReportStore = Ext.create('Ext.data.Store', {
                model: 'ReportBuilder.model.CommonModel',
                autoLoad: false,
                pageSize: 25,
                proxy: {
                    type: 'ajax',
                    timeout : 1800000,
                    url : 'ACCCreateCustomReport/executeCustomizedReport.do',
                    actionMethods : getStoreActionMethods(),
                    reader: {
                        type: 'json',
                        rootProperty: "data",
                        keepRawData: true,
                        totalProperty: 'totalCount'
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
                if(this.advanceSearchGrid.advSearch && !this.advanceSearchGrid.hidden && this.advanceSearchGrid.getFilterJson().root.length !=0 ){
                this.customizedReportStore.proxy.extraParams.searchJson = Ext.encode(this.advanceSearchGrid.getFilterJson());
                }

            }, this);            
        }
        

        
        this.reportStore.on("load", function() {
            if(this.moduleCategory === Ext.moduleCategoryType.Reports){
                this.customizedReportStore.load({
                    params: {
                        start: 0,
                        limit: 25
                    }
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

        btnArr.push(this.fromDate, '-', this.toDate);
        
        if(this.isEWayReport){
            this.statusFilter = new Ext.form.ComboBox({
                typeAhead: true,
                value: 'F',
                store: [
                    ['All', 'All'],
                    ['T', 'Generated'],
                    ['F', 'Pending'],
                ]
            });
            btnArr.push("-", "E-way Status:", this.statusFilter);
        }
        
        this.fetchBtn = new Ext.create('Ext.Button', {
            xtype: 'button',
            text: ExtGlobal.getLocaleText("acc.common.fetch"),
            iconCls: 'accountingbase fetch',
            scope: this,
            tooltip: ExtGlobal.getLocaleText("acc.invReport.fetchTT"),
            handler: function () {
                this.reportStore.reload();
                var normalGridView = this.reportGrid.normalGrid.view;
                normalGridView.emptyText = "<div class='x-grid-empty'>" + "<div style='text-align:center;font-size:18px;'>" + ExtGlobal.getLocaleText("account.common.nodatadisplay") + "</div>" + "</div>";
            }
        });

        btnArr.push("-", this.fetchBtn);
        
        this.resetBttn = new Ext.Button({
            text:ExtGlobal.getLocaleText("acc.common.reset") ,
            scope: this,
            iconCls: "pwnd reset",
            tooltip: ExtGlobal.getLocaleText("acc.common.reset")+" date",
            disabled: false,
            handler: function() {
                this.fromDate.setValue(financialYearFromDate);
                this.toDate.setValue(financialYearToDate);
                if (this.isEWayReport) {
                    this.statusFilter.setValue('F');
                }
                this.reportStore.reload();
            }
        });
        btnArr.push("-",this.resetBttn);
        if(this.isDefault == "F"){
        btnArr.push("-",{
            text: ExtGlobal.getLocaleText("acc.common.clearFilters"),
            tooltip: ExtGlobal.getLocaleText("acc.common.clearFilterToolTip"),
            iconCls: "pwnd remove-filter",
            handler: 'onClearFilters'
        });
        if(this.moduleCategory != Ext.moduleCategoryType.Reports) {
                this.AdvanceSearchBtn = new Ext.Button({
                    text: ExtGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
                    scope: this,
                    tooltip: ExtGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
                    handler: this.configurAdvancedSearch,
                    iconCls: "advanceSearchButton"
                });
        
                this.expandCollpseButton = Ext.create('Ext.Button',{
                    text: ExtGlobal.getLocaleText("acc.field.Expand"),
                    tooltip: ExtGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
                    iconCls: 'pwnd toggleButtonIcon',
                    scope: this,
                    handler: function() {
                        if (this.expandCollpseButton.getText() == ExtGlobal.getLocaleText("acc.field.Expand")) {
                            this.expandButtonClicked = true;
                        }
                        expandCollapse(this.reportGrid,this.expandReportStore,this);
                    }
                });
        
                btnArr.push("-",this.expandCollpseButton);
                btnArr.push("-",this.AdvanceSearchBtn);            
            }
            if(this.isEWayReport){
                this.ValidateJSON = new Ext.create('Ext.Button', {
//                    xtype: 'button',
                    text: ExtGlobal.getLocaleText("acc.customreport.Button.ValidateJSON"),
                    iconCls: 'eWayValidateJSON',
                    scope: this,
                    tooltip: ExtGlobal.getLocaleText("acc.customreport.Button.ValidateJSONTT"),
                    handler: function () {
                        this.reportGrid.getEl().mask(ExtGlobal.getLocaleText("acc.msgbox.50"));
                        this.validateJsonBttnHandler();
                    }
                });
                btnArr.push("-", this.ValidateJSON);


                this.revertStatus = new Ext.create('Ext.Button', {
//                    xtype: 'button',
                    text: ExtGlobal.getLocaleText("acc.customreport.Button.RevertStatus"),
                    iconCls: "eWayRevertStatus",
                    scope: this,
                    disabled: true,
                    tooltip: ExtGlobal.getLocaleText("acc.customreport.Button.RevertStatusTT"),
                    handler: function () {
                        var reportIds = "";
                        var selectedRecords = this.reportGrid.getSelectionModel().getSelected().items;
                        for (var count = 0; count < selectedRecords.length; count++) {
                            reportIds += selectedRecords[count].data.billid + ",";
                        }
                        reportIds = reportIds.substring(0, reportIds.length - 1);
                        Ext.Ajax.request({
                            url: 'ACCCreateCustomReport/revertEWayStatus.do',
                            method: "POST",
                            scope: this,
                            params: {
                                reportIds: reportIds,
                                moduleId:this.moduleid
                            },
                            success: function (res, req) {
                                this.reportStore.reload();
                                var normalGridView = this.reportGrid.normalGrid.view;
                                normalGridView.emptyText = "<div class='x-grid-empty'>" + "<div style='text-align:center;font-size:18px;'>" + ExtGlobal.getLocaleText("account.common.nodatadisplay") + "</div>" + "</div>";
                            },
                            failure: function () {
                                Ext.CustomMsg('Error', ExtGlobal.getLocaleText("acc.common.errorOccuratServerSide"), Ext.Msg.ERROR);
                            }
                        });
                    }
                });
                btnArr.push("-", this.revertStatus);
            }
        }
        
        this.chartListBtn =  this.createChartBtn();
        
        if (this.chartListBtn != undefined) {
            btnArr.push(this.chartListBtn);
        }
        
        var reportGridExpander;
        var reportGridPluginsArr = [];
        if(this.moduleCategory != Ext.moduleCategoryType.Reports) {
            reportGridExpander = {
                ptype: 'rowexpander',//Ext.grid.plugin.RowExpander
                pluginId: 'rowex',
                rowBodyTpl : ['<div id="ux-report-row-expander-box-{billid}-{reportID}-{recordCount}" style = "margin-left:25px;"></div>']
            };
            reportGridPluginsArr.push(reportGridExpander);
        }

        //Export  Button
        this.exportButton = new Ext.exportReportButton({
            obj:this,
            isEntrylevel:false,
            text:ExtGlobal.getLocaleText("acc.common.export"),
            disabled :false,
            moduleId:this.moduleid,
            reportId:this.reportId,
            iconCls: 'pwnd export',
            get:1, 
            menuItem:{
                csv:true,
                pdf: true, 
                xlsx:true
            }
        });
        
        //Print Button
        
        this.printButton =new Ext.exportReportButton({
            obj:this,
            isEntrylevel:false,
            text:ExtGlobal.getLocaleText("acc.common.print"),
            tooltip :ExtGlobal.getLocaleText("acc.common.printTT"),
            disabled :false,
            moduleId:this.moduleid,
            reportId:this.reportId,
            iconCls: 'pwnd printButtonIcon',
            get:1, 
            handler: function() {
                   
                    this.exportwithCsvXlsx("print", this.config,this.get,true);
            }
           
        });
        
        this.sortOrderNotePanel = Ext.create('Ext.Panel',{
            border:false,
            bodyCls:'note-panel', 
            bodyStyle : 'padding: 1px 0 0 2px;',
            height : 20
        });
        var reportStoreToBeUsed = (this.moduleCategory === Ext.moduleCategoryType.Reports) ? this.customizedReportStore : this.reportStore;
        var dockedContent = [
            {
            xtype:"toolbar",
            dock : "top",
            items: btnArr
        },{
            xtype: 'toolbar',
            name:"sortOrderNotePanelContainer",
            border:true,
            hidden : true,
            dock: 'top',
            items: [this.sortOrderNotePanel]
        },{
                xtype: 'pagingtoolbar',
                store: reportStoreToBeUsed,
                dock: 'bottom',
                displayInfo: true,
                animateShadow: true,
                items: ["-",this.exportButton,"-",this.printButton],
                plugins: [new Ext.ux.grid.PageSize(), new Ext.ux.ProgressBarPager()]
            }]
        reportGridPluginsArr.push('gridfilters');
        this.reportGrid = Ext.create('Ext.grid.Panel', {
            columns: [{width:1}],
            bufferedRenderer:false,
            viewConfig: {
                deferEmptyText: false,
                emptyText: "<div style='text-align:center;font-size:18px;'>"+ ExtGlobal.getLocaleText("acc.common.norec.click.fetchbtn")+"</div>"
            },
            selModel: Ext.create('ReportBuilder.extension.LockedCheckBoxSelModel',{
                injectCheckbox:'first',
                lock:false
            }),
            plugins: reportGridPluginsArr,
            pluginColumns : 3,     //Specify this config to lock/unlock columns like row expander ,row numberer etc.
            border : false,
             //            frame: true,
            enableLocking: true,
            defaultListenerScope: true,
            dockedItems: dockedContent,
            store: reportStoreToBeUsed,
            features: [{
                groupHeaderTpl: '{columnName}: {name}{defaultHeader} ({rows.length} Item{[values.rows.length > 1 ? "s" : ""]})',
                ftype: 'groupingsummary'
            },{ 
                ftype: 'summary',
                dock: 'bottom'
            }],
//            dockedItems: [{
//                xtype: 'pagingtoolbar',
//                store: this.reportStore,
//                dock: 'bottom',
//                displayInfo: true,
//                animateShadow: true,
//                items: ["-",this.exportButton],
//                plugins: [new Ext.ux.grid.PageSize(), new Ext.ux.ProgressBarPager()]
//            }],
            onClearFilters: function() {
                this.filters.clearFilters();                
                this.store.clearFilter(true);
                this.store.proxy.extraParams.isclearfilter=true;
            }
        });
        
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
//                    start: 0,
//                    limit: 25
//                }
//            });
                }
        
        this.reportGrid.on('lockcolumn',ExtGlobal.lockPluginColumns);
        this.reportGrid.on('unlockcolumn',ExtGlobal.unlockPluginColumns);
        this.reportGrid.on("selectionchange", this.enableDisableButtons, this);
        
        this.isReportReconfigure = false;
        if(this.moduleCategory === Ext.moduleCategoryType.Reports) {
            this.customizedReportStore.on("load", this.handlegridonload, this);            
        } else {
            this.reportStore.on("load", this.handlegridonload, this);
        }
//        this.store.on("load", this.handlegridonload, this);
        this.reportReportGridview = this.reportGrid.getView();
        this.reportReportGridview.on('expandbody', this.expandRow, this);
        
    },
    validateJsonBttnHandler: function () {
        var params = this.reportStore.proxy.extraParams ? this.reportStore.proxy.extraParams : {};
        params.reportID = this.reportId;
        params.fromDate = ExtGlobal.convertToGenericDate(this.fromDate.getValue());
        params.toDate = ExtGlobal.convertToGenericDate(this.toDate.getValue());
        params.gcurrencyid = Ext.pref.Currencyid;
        params.consolidateFlag = false;
        params.deleted = false;
        params.nondeleted = false;
        params.pendingapproval = false;
        params.showRowLevelFieldsflag = false;
        params.moduleid = this.moduleid;
        params.isreportloaded = this.isreportloaded
        params.isChartRequest = false;
        params.isEWayReportValidation = true;
        params.companyName = companyName
        if(this.advanceSearchGrid.advSearch && !this.advanceSearchGrid.hidden && this.advanceSearchGrid.getFilterJson().root.length!=0){
        params.searchJson = Ext.encode(this.advanceSearchGrid.getFilterJson());
        this.searchJson = Ext.encode(this.advanceSearchGrid.getFilterJson());
        }
        if (this.statusFilter) {
        params.ewayFilter =this.statusFilter.getValue();
        }
        
        
        this.validationStore = Ext.create('Ext.data.Store', {
            model: 'ReportBuilder.model.CommonModel',
            autoLoad: false,
            pageSize: 25,
            proxy: {
                type: 'ajax',
                url: 'ACCCreateCustomReport/executeCustomReport.do',
                timeout : 1800000,
                actionMethods : getStoreActionMethods(),
                extraParams : params,
                reader: {
                    type: 'json',
//                    rootProperty: "data[\"data\"]",
                    rootProperty: "data",
                    keepRawData: true,
//                    totalProperty: "data[\"count\"]"
                    totalProperty: "totalCount"
                }
            }
        });
        
        
        this.validationStore.load({
            params: {
                isWholeData: true
            }
        });
        this.validationStore.on("load", this.createValidationReportWin, this);
    },
    
    createValidationReportWin: function (validationStore, record, success, opts) {

        var resObj = eval("(" + opts._response.responseText + ")");
        if (resObj.success == true) {
            this.reportGrid.getEl().unmask();
            if (!this.validationWin) {
                this.validationWin = Ext.create('ReportBuilder.view.ValidationWin', {
                    parentObj: this,
                    store: this.validationStore
                });
                this.validationWin.on('destroy', function () {
                    this.validationWin = null;
                }, this);
            }
            this.validationWin.show();
        } else{
             Ext.CustomMsg('Error',resObj.msg, Ext.Msg.ERROR);
             this.reportGrid.getEl().unmask();
        }
    },
    
    createExpandStore:function(){
        this.expandReportStore = Ext.create('ReportBuilder.store.ReportStore',{
            storeId: "expandStore"
        });
        this.expandReportStore.on("beforeload", function(){
            this.reportGrid.getEl().mask(ExtGlobal.getLocaleText("acc.msgbox.50"));
        }, this);
        this.expandReportStore.on("load", this.fillExpanderBody, this);
    },
    handlegridonload: function(store, record, success, opts) {
        this.store = (this.moduleCategory === Ext.moduleCategoryType.Reports) ? this.customizedReportStore : this.reportStore;
        var rawData = this.store.getProxy().getReader().rawData;
        if (rawData.columns != undefined || (this.isDefault == "T" && rawData.data.columns != undefined )) {
            var columnConfig = rawData.columns
            this.columns = rawData.columns;
            
            if(this.isDefault == "F"){               
                // Commenting below lines as it causing FromDate to set to financialYearFromDate even if date is previous than Financial Year 
//                var userPreferences = this.reportStore.getProxy().getReader().rawData.userPreferences;
//                if (financialYearFromDate == undefined && financialYearToDate == undefined) {
//                    financialYearFromDate = userPreferences.fromdate;
//                    financialYearToDate = userPreferences.todate;
//
//                }
//                this.fromDate.setValue(financialYearFromDate);
//                this.toDate.setValue(financialYearToDate);
                this.columns = createColumns(this.columns,this.moduleid);
                if(this.moduleCategory != Ext.moduleCategoryType.Reports) {
                    this.expandCollpseButton.setText(ExtGlobal.getLocaleText("acc.field.Expand"));
                    this.expandButtonClicked = false;                    
                }
            }else{
                this.columns = rawData.data.columns;
                for(var i=0;i < this.columns.length;i++){
                    if(this.columns[i].renderer){
                        this.columns[i].renderer = eval(this.columns[i].renderer);
                    }
                    if(this.columns[i].summaryRenderer){
                        this.columns[i].summaryRenderer = eval(this.columns[i].summaryRenderer);
                    }
                }
            }

            
            if (this.isReportReconfigure == false) {
                this.store.group(getGroupingFields(this.columns));
//                this.reportStore.sort(this.reportStore.getProxy().getReader().rawData.sortConfigArray); 
                this.reportGrid.reconfigure(this.store, this.columns);
                this.isReportReconfigure = true;
            }
            this.isReportStoreRefresh = true;
            this.isreportloaded=false;
            if (rawData.filter != undefined && rawData.filter.length > 0) {
                 this.isreportloaded=true;
             } 
            if(this.store.getProxy().getReader().rawData.sortConfigArray != undefined && this.store.getProxy().getReader().rawData.sortConfigArray.length>0) {
                 var sortOrderNotePanelContainer = this.reportGrid.dockedItems.items.getIemtByParam({name: "sortOrderNotePanelContainer"})
                 var sortNote = ExtGlobal.getLocaleText("acc.common.sortOrderNotePanelhtml")+"&nbsp";
                 for(var cnt=0;cnt < this.store.getProxy().getReader().rawData.sortConfigArray.length ; cnt++){ 
                    var sortConfig = this.store.getProxy().getReader().rawData.sortConfigArray[cnt];
                    if(sortConfig.isMeasureItem == false) {
                    sortNote+=sortConfig.sortedfield;
                    if(sortConfig.direction=="ASC"){
                        sortNote+="<img class=\"x-grid3-sort-icon\" src=\"../../lib/resources/images/default/grid/sort_asc.gif\"> &nbsp ";
                    } else if(sortConfig.direction=="DESC"){
                        sortNote+="<img class=\"x-grid3-sort-icon\" src=\"../../lib/resources/images/default//grid/sort_desc.gif\"> &nbsp ";
                    }
                } else {
                    sortNote="";
                    sortNote = ExtGlobal.getLocaleText("acc.common.sortOrderNotePanelhtml")+"&nbsp";
                    sortNote+=sortConfig.sortedfield;
                    if(sortConfig.direction=="ASC"){
                        sortNote+="<img class=\"x-grid3-sort-icon\" src=\"../../lib/resources/images/default/grid/sort_asc.gif\"> &nbsp ";
                    } else if(sortConfig.direction=="DESC"){
                        sortNote+="<img class=\"x-grid3-sort-icon\" src=\"../../lib/resources/images/default//grid/sort_desc.gif\"> &nbsp ";
                    }
                    
                }
                    
                 }
                 this.sortOrderNotePanel.update(sortNote);
                 sortOrderNotePanelContainer.show();
            }
            
        } else {                             
            Ext.CustomMsg('Error', rawData.msg, Ext.Msg.ERROR);
//         Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), this.store.getProxy().getReader().rawData.msg, Ext.Msg.INFO);
        }
    },
    expandRow: function(rowNode, record, body) {
        if(!this.expandButtonClicked){
            var billid = record.data.billid;
            var linkedbillid = record.data.linkedbillid;
            var recordCount=record.data.recordCount.toString();
        
            var storeid = billid+"-"+this.reportId+"-"+recordCount;
        
            var targetId = 'ux-report-row-expander-box-' + storeid;
            var targetEl = Ext.get(targetId).dom;
            var isStoreLoaded = targetEl.hasChildNodes();
        
        
            if(targetEl.offsetParent){
                targetEl.offsetParent.colSpan +=1;
            }
        
            if(!isStoreLoaded){
                this.expandReportStore.load({
                    scope: this,
                    params: {
                        reportID: this.reportId,
                        deleted: false,
                        nondeleted: false,
                        pendingapproval: false,
                        showRowLevelFieldsflag: true,
                        billid: billid,
                        linkedbillid : linkedbillid,
                        recordCount : recordCount,
                        moduleid : this.moduleid
                    }
                });
            }
        }
    },
    fillExpanderBody : function(store, record, success, opts){
        if (success) {
            var reportID= this.reportId;
            var resObj = eval("(" + opts.getResponse().responseText + ")");
            var lineLevelColumns = resObj.columns ? resObj.columns:[];
            var expander = this.reportGrid.normalGrid.getPlugin('rowex');
            var billidArr = opts.getRequest()._params.billid.split(",");
            var linkedbillidArr ;
            if(opts.getRequest()._params.linkedbillid != undefined){
                linkedbillidArr = opts.getRequest()._params.linkedbillid.split(",");               
            }
            var recordCountArr = opts.getRequest()._params.recordCount.split(",");
            var storeData = this.expandReportStore.data.items;
            
            if(this.expandButtonClicked){
                expander.expandAll();
            }
            
            for(var cnt=0;cnt < billidArr.length ; cnt++){
                var billid = billidArr[cnt];
                var linkedbillid;
                if(linkedbillidArr != undefined && linkedbillidArr.length > 0) {
                    linkedbillid = linkedbillidArr[cnt];
                }
//                var linkedbillid = linkedbillidArr[cnt];
                var header="";
                var columnHeader = "";
                var isDataPresent = false;
                var recordCount = Number(recordCountArr[cnt]);
                
                var rec = this.getRecordByBillId(storeData,billid);
                var targetId = 'ux-report-row-expander-box-' + billid+"-"+reportID+"-"+recordCount;
                
//                console.log("targetId : "+targetId);
                if(lineLevelColumns.length > 0)  {   //if row level column exist
                   
                  //header=setRowExpanderHTML(lineLevelColumns,rec,this.columns);
                  if(this.moduleid == Ext.Acc_Debit_Note_ModuleId || this.moduleid == Ext.Acc_Credit_Note_ModuleId){
//                        header = setRowExpanderForNotesHTML(lineLevelColumns, rec,resObj);
                        if (lineLevelColumns[0].accountColumns != undefined && lineLevelColumns[0].accountColumns.length > 0) {
//                            var accountRec;
//                            if(linkedbillid == undefined){
//                                accountRec = this.getRecordByBillId(storeData[0].data.accountData,billid);
//                            } else {
//                                accountRec = this.getRecordByLinkedBillId(storeData[0].data.accountData,linkedbillid);
//                            }
                            var accountRec = this.getRecordByBillId(storeData[0].data.accountData,billid);
                            columnHeader = this.moduleid == Ext.Acc_Debit_Note_ModuleId ? "Account Details" : "Account Details";
                            if (storeData[0].data.accountData != undefined && storeData[0].data.accountData.length > 0) {
                                header += setRowExpanderHTML(lineLevelColumns[0].accountColumns, accountRec, columnHeader, this.moduleid);
                            }
                            isDataPresent = true;
                        }
                        if (lineLevelColumns[0].invoiceColumns != undefined && lineLevelColumns[0].invoiceColumns.length > 0) {
//                            var invoiceRec = this.getRecordByLinkedBillId(storeData[0].data.invoiceData,billid);
                            var invoiceRec ;
                            if(linkedbillid == undefined){
                                invoiceRec = this.getRecordByBillId(storeData[0].data.invoiceData,billid);
                            } else {
                                if(linkedbillid != "") {
                                    invoiceRec = this.getRecordByBillIdAndLinkedBillId(storeData[0].data.invoiceData,billid,linkedbillid);
                                } else {
                                    invoiceRec = this.getRecordByBillId(storeData[0].data.invoiceData,billid);
                                }
                            }
                            columnHeader = this.moduleid == Ext.Acc_Debit_Note_ModuleId ? "Invoice Details" : "Invoice Details";
                            if (storeData[0].data.invoiceData != undefined && storeData[0].data.invoiceData.length > 0) {
                                header += setRowExpanderHTML(lineLevelColumns[0].invoiceColumns, invoiceRec, columnHeader, this.moduleid);
                            }
                            isDataPresent = true;
                        }
                    } else if(this.moduleid == Ext.Acc_Make_Payment_ModuleId || this.moduleid == Ext.Acc_Receive_Payment_ModuleId){
                        if(lineLevelColumns[0].invoiceColumns.length > 0){
                            var invoiceRec = this.getRecordByBillId(storeData[0].data.invoiceData,billid);
                            columnHeader = this.moduleid == Ext.Acc_Make_Payment_ModuleId ? "Payment Against Vendor Invoice":"Payment Against Customer Invoice";
                            if(!this.isEmptyPaymentRec(invoiceRec,lineLevelColumns[0].invoiceColumns)){
                                header += setRowExpanderHTML(lineLevelColumns[0].invoiceColumns, invoiceRec , columnHeader , this.moduleid);
                                isDataPresent = true;
                            }
                        }
                        if(lineLevelColumns[0].GLColumns.length > 0){
                            var GLRec = this.getRecordByBillId(storeData[0].data.GLData,billid);
                            if(!this.isEmptyPaymentRec(GLRec,lineLevelColumns[0].GLColumns)){
                                header += setRowExpanderHTML(lineLevelColumns[0].GLColumns, this.getRecordByBillId(storeData[0].data.GLData,billid) , "Payment Against GL",this.moduleid);
                                isDataPresent = true;
                            }
                        }
                        if(lineLevelColumns[0].creditNoteColumns.length > 0){
                            var creditNoteRec = this.getRecordByBillId(storeData[0].data.creditNoteData,billid);
                            columnHeader = this.moduleid == Ext.Acc_Make_Payment_ModuleId ? "Payment Against Credit Note":"Payment Against Debit Note";
                            if(!this.isEmptyPaymentRec(creditNoteRec,lineLevelColumns[0].creditNoteColumns)){
                                header += setRowExpanderHTML(lineLevelColumns[0].creditNoteColumns,creditNoteRec, columnHeader,this.moduleid);
                                isDataPresent = true;
                            }
                        }
                        if(lineLevelColumns[0].loanColumns.length > 0){
                            var loanRec = this.getRecordByBillId(storeData[0].data.loanData,billid);
                            if(!this.isEmptyPaymentRec(loanRec,lineLevelColumns[0].loanColumns)){
                                header += setRowExpanderHTML(lineLevelColumns[0].loanColumns, loanRec , "Payment Against Disbursement",this.moduleid);
                                isDataPresent = true;
                            }
                        }
                        if(lineLevelColumns[0].advancePaymentColumns.length > 0){
                            var advancePaymentRec = this.getRecordByBillId(storeData[0].data.advancePaymentData,billid);
                            if(!this.isEmptyPaymentRec(advancePaymentRec,lineLevelColumns[0].advancePaymentColumns)){
                                header += setRowExpanderHTML(lineLevelColumns[0].advancePaymentColumns, advancePaymentRec, "Advance Payment",this.moduleid);
                                isDataPresent = true;
                            }
                        }
                        if(lineLevelColumns[0].linkedInvoiceColumns.length > 0){
                            var linkedInvRec = this.getRecordByBillId(storeData[0].data.linkedInvoiceData,billid);
                            if(!this.isEmptyPaymentRec(linkedInvRec,lineLevelColumns[0].linkedInvoiceColumns)){
                                header += setRowExpanderHTML(lineLevelColumns[0].linkedInvoiceColumns, linkedInvRec , "Used Advance Payment Against Invoices",this.moduleid);
                                isDataPresent = true;
                            }
                        }
                        if(lineLevelColumns[0].linkedCreditNoteColumns.length > 0){
                            var linkedCNRec = this.getRecordByBillId(storeData[0].data.linkedCreditNoteData,billid);
                            columnHeader = this.moduleid == Ext.Acc_Make_Payment_ModuleId ? "Used Advance Payment Against Credit Note":"Used Advance Payment Against Debit Note";
                            if(!this.isEmptyPaymentRec(linkedCNRec,lineLevelColumns[0].linkedCreditNoteColumns)){
                                header += setRowExpanderHTML(lineLevelColumns[0].linkedCreditNoteColumns, linkedCNRec ,columnHeader,this.moduleid);
                                isDataPresent = true;
                            }
                        }
                        if(lineLevelColumns[0].refundColumns.length > 0){
                            var refundRec = this.getRecordByBillId(storeData[0].data.refundData,billid);
                            if(!this.isEmptyPaymentRec(refundRec,lineLevelColumns[0].refundColumns)){
                                header += setRowExpanderHTML(lineLevelColumns[0].refundColumns, refundRec , "Refund/ Deposit",this.moduleid);
                                isDataPresent = true;
                            }
                        }
                        if(lineLevelColumns[0].linkedRefundColumns.length > 0){
                            var linkedRefundRec = this.getRecordByBillId(storeData[0].data.linkedRefundData,billid);
                            if(!this.isEmptyPaymentRec(linkedRefundRec,lineLevelColumns[0].linkedRefundColumns)){
                                header += setRowExpanderHTML(lineLevelColumns[0].linkedRefundColumns, linkedRefundRec , "Used Refund/ Deposit Against Advance Payment",this.moduleid);
                                isDataPresent = true;
                            }
                        }
                        if(!isDataPresent){
                            header = "<div style='width: 100%;min-width:1900px;margin-left:50px'><span class='gridHeader'>"+ExtGlobal.getLocaleText("account.common.nodatadisplay")+".</span></div>";
                        }
                        
                    } else {
                        header=setRowExpanderHTML(lineLevelColumns, rec , ExtGlobal.getLocaleText("acc.common.ProductList") , this.moduleid);
                    }
                    //Increasing the colspan of offsetparent of row expander div inorder to render properly.
                    // After data set to Grid Row, expand row forcefully.
                    if(Ext.get(targetId).dom.offsetParent){
                        Ext.get(targetId).dom.offsetParent.colSpan +=1;
                    }
                    Ext.get(targetId).setHtml(header);//setting html in rowbodytpl
                        
                }else {
                    noRecordMsg(billid,header,"",reportID,recordCount);
                } 
            }
            this.reportGrid.getEl().unmask();
        }
    },
    getRecordByBillId :function (storeData,billid){
        var billidArr = [];
        storeData.forEach(function(record) {
            var recordData = record.data != undefined ? record.data : record;
            if(recordData.billid === billid )
                billidArr.push(record);   
        });
        return billidArr;
    },
     getRecordByBillIdAndLinkedBillId :function (storeData,billid,linkedbillid){
        var linkedbillidArr = [];
        storeData.forEach(function(record) {
            var recordData = record.data != undefined ? record.data : record;
            if(recordData.billid === billid && recordData.linkedbillid === linkedbillid )
                linkedbillidArr.push(record);   
        });
        return linkedbillidArr;
    },
    createAdvanceSearchComponent: function () {
        this.advanceSearchGrid = Ext.create('ReportBuilder.view.AdvanceSearch', {
            hidden: true,
            moduleid: this.moduleid,
            reportId: this.reportId,
            advSearch: false,
            customerCustomFieldFlag: this.showCustomerCustomFieldFlag(this.moduleid),
            vendorCustomFieldFlag: this.showVendorCustomField(this.moduleid),
            lineLevelSearch: false,
            isAvoidRedundent: this.isAvoidRedundent(this.moduleid),
            isEWayReport: this.isEWayReport
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
        this.store.proxy.extraParams.moduleid = this.moduleid;
        this.store.proxy.extraParams.filterConjuctionCriteria = null;
        
        this.store.loadPage(1);
        this.advanceSearchGrid.hide();
        this.AdvanceSearchBtn.enable();
    },
    filterStore: function(json, filterConjuctionCriteria) {
        this.store = this.reportStore;
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.store.proxy.extraParams.searchJson = this.searchJson;
        this.store.proxy.extraParams.moduleid = this.moduleid;
        this.store.proxy.extraParams.filterConjuctionCriteria = filterConjuctionCriteria;
        
        this.store.loadPage(1);
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
    //Function to check empty payment rec at line level in expand all case
    isEmptyPaymentRec : function(rec,columns){
        var isEmpty = true;
        
        if(rec.length == 0){ //return true if rec is empty
            isEmpty = true;
        }else if(rec.length == 1){//check if rec contains one rec
            var newRec = rec[0];
            
            for (var key in newRec) {
                if (newRec.hasOwnProperty(key) && key!="recordCount") {
                    var val = newRec[key];
                    var recColumn = columns.getIemtByParam({"id" : key});
                    /*
                     *Check if value is not empty for column having xtype other than 2 i.e number field
                     *If column have xtype 2 then check whether it have value greater than 0
                     **/
                    if(recColumn != undefined) {
                        if((val != "" && val != undefined && recColumn.xtype != 2) || (recColumn.xtype == 2 && parseInt(val)>0)){
                            isEmpty = false;
                            break;
                        }
                    }
                }
            }
        }else{
            isEmpty = false;
        }
        return isEmpty;
    },
    
    createChartBtn: function () {
        if (this.reportRec !== undefined && this.reportRec.get("charts") !== undefined && this.reportRec.get("charts") !== "[]") {
            var chartsData = this.reportRec.get("charts");
            var barMenuArr = [];
            var lineMenuArr = [];
            var pieMenuArr = [];
            for (var i = 0; i < chartsData.length; i++) {
                var type = chartsData[i].chartType;
                switch (type) {
                    case "Bar":
                        barMenuArr.push({
                            text : chartsData[i].name,
                            chartData : chartsData[i],
                            iconCls: 'x-chart-barchartwizard',
                            scope: this,
                            handler: this.chartButtonHandler
                        })
                        break;
                    case "Line":
                        lineMenuArr.push({
                            text : chartsData[i].name,
                            chartData : chartsData[i],
                            iconCls: 'x-chart-linechartwizard',
                            scope: this,
                            handler: this.chartButtonHandler
                        })
                        break;
                    case "Pie":
                        pieMenuArr.push({
                            text : chartsData[i].name,
                            chartData : chartsData[i],
                            iconCls: 'x-chart-piechartwizard',
                            scope: this,
                            handler: this.chartButtonHandler
                        })
                        break;
                }
            }
            var chartListBtnArr = [];
            if(barMenuArr.length > 0) {
                chartListBtnArr.push({
                    text: ExtGlobal.getLocaleText("acc.common.BarChart"),
                    iconCls: 'x-chart-barchartwizard',
                    scope: this,
                    menu: barMenuArr
                });
            }
            if(lineMenuArr.length > 0) {
                chartListBtnArr.push({
                    text: ExtGlobal.getLocaleText("acc.common.LineChart"),
                    iconCls: 'x-chart-linechartwizard',
                    scope: this,
                    menu: lineMenuArr
                });
            }
            if(pieMenuArr.length > 0) {
                chartListBtnArr.push({
                    text: ExtGlobal.getLocaleText("acc.common.PieChart"),
                    iconCls: 'x-chart-piechartwizard',
                    scope: this,
                    menu: pieMenuArr
                });
            }
            var chartListBtn = undefined;
            if(chartListBtnArr.length > 0) {
                chartListBtn = Ext.create('Ext.Button', {
                    text: ExtGlobal.getLocaleText("acc.common.Chart"),
                    tooltip: ExtGlobal.getLocaleText("acc.CustomReport.Chart.TT.ChartListBtn"),
                    scope: this,
                    iconCls: "accountingbase chart",
                    menu: chartListBtnArr
                });
            }
        }
        return chartListBtn;
    },
    
    chartButtonHandler: function (button) {
        var chartsData = button.chartData;
        chartsData.iconCls = button.iconCls;
        chartsData.url = this.reportStore.proxy.url;
        chartsData.params = this.reportStore.proxy.extraParams;
        chartsData.params.isChartRequest = true;
        getChartPanelContainer(chartsData);
    },
    enableDisableButtons:function(grid, selected, eOpts){
        if (selected.length > 0 && this.isEWayReport && this.enableRevertStatusBtn) {
            this.revertStatus.enable();
        } else {
            this.revertStatus.disable();
        }

    }
});
