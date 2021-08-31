/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

function  SalesCommissionDimensionReportLoad(params) {
    var isprofitreport = params.isprofitreport;
    var title = isprofitreport ? Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.jobdateprofit.title")) : Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.salescommission.title"));
    var tabtitle = isprofitreport ? WtfGlobal.getLocaleText("acc.jobdateprofit.title") : WtfGlobal.getLocaleText("acc.salescommission.title");
    /*
     *Function to add this report in widget report.
     **/
    if(params.isCustomWidgetReport){ 
        panel = new Wtf.account.SalesCommissionReport({
            border: false,
            isprofitreport: isprofitreport,
            isCustomWidgetReport : params.isCustomWidgetReport
        });
        if(params.callbackFn){
            /*
             *call callback function to add this report to widget.
             **/
            params.callbackFn.call(this, panel);
        }
    }else{
        var winid = isprofitreport ? "profit" : "";
        var panel = Wtf.getCmp("commissionreport" + winid);
        if (panel == null) {
            panel = new Wtf.account.SalesCommissionReport({
                title: title,
                tabTip: tabtitle,
                id: 'commissionreport' + winid,
                closable: true,
                border: false,
                iconCls: 'accountingbase vendor',
                activeTab: 0,
                isprofitreport: isprofitreport
            });
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        //    showAdvanceSearch(panel, searchStr, filterAppend);
        Wtf.getCmp('as').doLayout();
    }
}

Wtf.account.SalesCommissionReport = function(config) {
    this.arr = [];
    Wtf.apply(this, config);
    /*
     * Create Tool Bar Buttons
     */
    this.createTBar();
    /*
     * Create Grid 
     */
    this.createGrid();
    
        this.addAdvanceSearchComponent();

    this.createPanel();

    Wtf.apply(this, {
        border: false,
        layout: "fit",
        items: [this.leadpan]
    });
    /*
     * Add Advance Search
     */
//    this.addAdvanceSearchComponent();


    Wtf.account.SalesCommissionReport.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.SalesCommissionReport, Wtf.Panel, {
    onRender: function(config) {
        /*
         * create panel to show grid
         */
//        this.createPanel();
//        this.add(this.leadpan);
        /*
         * fetch data in report
         */
        this.fetchStatement();
        Wtf.account.SalesCommissionReport.superclass.onRender.call(this, config);
    },
    createPanel: function() {
        var panelItems = [];
        if(this.isCustomWidgetReport !=true){
            panelItems.push(this.objsearchComponent);
        }
        panelItems.push(this.grid);
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: panelItems

        });
    },
    createTBar: function() {
        this.btnArr = [];
        this.bbarBtnArr = [];
        this.startDate = new Wtf.ExDateFieldQtip({
            name: 'stdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.getDates(true)
        });
        this.endDate = new Wtf.ExDateFieldQtip({
            name: 'enddate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.getDates(false)
        });
        if(this.isCustomWidgetReport !=true){ // hide start date and end date from widget view.
            this.btnArr.push('-', 'From', this.startDate);
            this.btnArr.push('-', 'To', this.endDate);
        }

        /**
         * Commission type report
         */
        this.reporttypestore = new Wtf.data.SimpleStore({
            fields: [{name: 'id'}, {name: 'value'}],
            data: [[1, "Margin"],[2, "Amount"],[3, "Payment Term"],[4, "Brand/Product Category"]]
        });
        this.reporttype = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.salescommission.reporttype"),
            hiddenName: 'id',
            name: 'id',
            store: this.reporttypestore,
            valueField: 'id',
            displayField: 'value',
            mode: 'local',
            typeAhead: true,
            triggerAction: 'all',
            hideLabel: true,
            value: 1,
            width: 100,
            listWidth: 150
        });

        /**
         * 1st Option
         */
        this.SchemaDimensionRec = Wtf.data.Record.create([
            {name: 'fieldid'},
            {name: 'fieldlabel'},
        ]);
        var moduleArr = "" + Wtf.Acc_Vendor_Invoice_ModuleId + "," + Wtf.Acc_Invoice_ModuleId + "";
        this.SchemaDimensionStore = new Wtf.data.Store({
            url: "ACCAccountCMN/getFieldParams.do",
            baseParams: {
//                moduleidarray: moduleArr,
                isAvoidRedundent: true,
                ignoreDefaultFields: true,
                isActivated: 1,
                isAdvanceSearch: true,
                reportid: Wtf.autoNum.SalesCommissionSchemaReport
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.SchemaDimensionRec)
        });
        this.SchemaDimensionStore.load();
        this.SchemaDimension = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.salescommission.dimension"),
            emptyText: WtfGlobal.getLocaleText("acc.salescommission.dimension.emptyText"),
            hiddenName: 'fieldid',
            name: 'fieldid',
            store: this.SchemaDimensionStore,
            valueField: 'fieldid',
            displayField: 'fieldlabel',
            mode: 'local',
            typeAhead: true,
            triggerAction: 'all',
            hideLabel: true,
            width: 150,
            listWidth: 150
        });

        this.SchemaDimension.on('select', this.onSchemaSelection, this);
        /**
         * 2 nd Option
         */

        this.SchemaValueRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
        ]);
        this.SchemaValueStore = new Wtf.data.Store({
            url: "ACCAccountCMN/getCustomCombodata.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.SchemaValueRec)
        });
        
        this.SchemaValueStore.on("load",function(){
            var rec = new this.SchemaValueRec({
                id : "all",
                name : "All"
            });
            this.SchemaValueStore.insert(0,rec);
            this.SchemaValue.setValue("all");
        },this);
        
        this.SchemaValue = new Wtf.common.Select({
            fieldLabel: WtfGlobal.getLocaleText("acc.salescommission.dimension"),
            multiSelect:true,
            hiddenName: 'id',
            name: 'id',
            store: this.SchemaValueStore,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            typeAhead: true,
            triggerAction: 'all',
            hideLabel: true,
            width: 150,
            listWidth: 150,
            disabled: true
        });
        
        this.SchemaValue.on('select',function(combo,rec){
            if(rec.get('id')=='all'){
                combo.clearValue();
                combo.setValue('all');
            }else if(combo.getValue().indexOf('all')>=0){
                combo.clearValue();
                combo.setValue(rec.get('id'));
            }
        }, this);

        /**
         * # 3rd option
         */

        this.dimRec = Wtf.data.Record.create([
            {name: 'fieldid'},
            {name: 'fieldlabel'},
        ]);
        var moduleArr = "" + Wtf.Acc_Vendor_Invoice_ModuleId;
        this.dimensionStore = new Wtf.data.Store({
            url: "ACCAccountCMN/getFieldParams.do",
            baseParams: {
                moduleidarray: moduleArr,
                isAvoidRedundent: true,
                ignoreDefaultFields: true,
                isActivated: 1,
                isAdvanceSearch: true,
                iscustomdimension: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.dimRec)
        });
        this.dimensionStore.load();
        this.Dimension = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.salescommission.dimension"),
            hiddenName: 'fieldid',
            name: 'fieldid',
            store: this.dimensionStore,
            valueField: 'fieldid',
            displayField: 'fieldlabel',
            mode: 'local',
            typeAhead: true,
            triggerAction: 'all',
            hideLabel: true,
            width: 100,
            listWidth: 150
        });
        
        this.invoiceFilterStore = new Wtf.data.SimpleStore({
            fields: [{name: 'id'}, {name: 'value'}],
            data: [['all', "All"],['fullypaid', "Full Paid Only"]]
        });
        
        this.paidInvoiceFilter = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.salescommission.filter"),
            hiddenName: 'paidInvoiceFilter',
            hideLabel: true,
            name: 'paidInvoiceFilter',
            store: this.invoiceFilterStore,
            valueField: 'id',
            displayField: 'value',
            value: 'all',
            mode: 'local',
            typeAhead: true,
            triggerAction: 'all',
            forceSelection: true,
            width: 110,
            listWidth: 110
        });
        
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
        if (!this.isprofitreport) {
            this.btnArr.push('-', WtfGlobal.getLocaleText("acc.salescommission.reporttype"), this.reporttype);
            this.btnArr.push('-', WtfGlobal.getLocaleText("acc.salescommission.dimension"), this.SchemaDimension);
            this.btnArr.push('-', this.SchemaValue);
        }
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.salescommission.dim3"), this.Dimension);
        if (!this.isprofitreport) {
            this.btnArr.push('-', WtfGlobal.getLocaleText("acc.salescommission.filter"), this.paidInvoiceFilter);
        }
        this.btnArr.push('-', this.fetchBttn);
        if(this.isCustomWidgetReport !=true){
            this.resetBttn = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
                tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
                id: 'btnRec' + this.id,
                scope: this,
                iconCls: getButtonIconCls(Wtf.etype.resetbutton),
                disabled: false
            });
            this.btnArr.push('-', this.resetBttn);
            this.resetBttn.on('click', this.handleResetClickNew, this);
        }

        /**
         * Advance Search component
         */

        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
            scope: this,
            hidden: !this.isprofitreport,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });

        if (this.isprofitreport && this.isCustomWidgetReport !=true) {
            this.btnArr.push('-', this.AdvanceSearchBtn);
        }

        var title = this.isprofitreport ? Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.jobdateprofit.title")) : Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.salescommission.title"));
        this.exportButton = new Wtf.exportButton({
            obj: this,
            id: "exportReports" + this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
            scope: this,
            filename: title + "_v1",
            menuItem: {
                csv: true,
                pdf: true,
                rowPdf: false,
                xls: true
            },
            get: Wtf.autoNum.SalesCommissionSchemaReport
        });
        this.exportButton.on("click", function() {
            this.exportButton.setParams({
                SchemaDimension: this.SchemaDimension.getValue(),
                Dimension: this.Dimension.getValue(),
                DimensionLable: this.Dimension.getRawValue(),
                SchemaValue: this.SchemaValue.getValue(),
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericStartDate(this.endDate.getValue()),
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value,
                start: 0,
                isprofitreport: this.isprofitreport
            });
        }, this);
        if(this.isCustomWidgetReport !=true){
            this.btnArr.push('-', this.exportButton);
        }
        
    },
    addAdvanceSearchComponent: function() {
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.grid.colModel,
            moduleidarray: '2,6'.split(','),
            advSearch: false,
            parentPanelSearch: this,
            ignoreDefaultFields: false,
            isAvoidRedundent: true
        });

        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
        this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    },
    onSchemaSelection: function() {
        this.SchemaValue.enable();
        this.SchemaValueStore.load({
            params: {
                mode: 2,
                flag: 1,
                fieldid: this.SchemaDimension.getValue()
            }
        })
    },
    fetchStatement: function () {
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value,
            }
        });
    },
    createGrid: function() {
        this.Store = new Wtf.data.GroupingStore({
            url: this.isprofitreport ? "ACCOtherReports/getProfitabilityReport.do" : "ACCOtherReports/getSalesCommissionReport.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            }),
            sortInfo: {field: 'schemaValue',direction: "ASC"}
        });
//        this.sm = new Wtf.grid.CheckboxSelectionModel({
//        });
        this.summary =  new Wtf.grid.GroupSummary(); 
        this.emptytextKey = this.isprofitreport ? "acc.jobtodate.alert":"acc.salescommission.alert";
        this.grid = new Wtf.grid.GridPanel({
            layout: 'fit',
            region: "center",
            store: this.Store,
            columns: [],
            border: false,
            loadMask: true,
//            sm: this.sm,
            tbar: this.btnArr,
            view: new Wtf.grid.GroupingView({
                groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})',
                forceFit: false,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText(this.emptytextKey))
            }),
            plugins: [this.summary],
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.Store,
                displayInfo: true,
                scope:this,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({
                    id: "pPageSize_" + this.id
                }),
                items: this.bbarBtnArr
                })
        });

        this.Store.on('load', this.handleStoreOnLoad, this);
        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
    },
    handleStoreBeforeLoad: function(s, o) {
        this.Store.groupBy(undefined);
        if (!o.params)
            o.params = {};
        o.params.SchemaDimension = this.SchemaDimension.getValue();
        o.params.Dimension = this.Dimension.getValue();
        o.params.DimensionLable = this.Dimension.getRawValue();
        o.params.SchemaValue = this.SchemaValue.getValue();
        o.params.ReportType = this.reporttype.getValue();
        o.params.filterby = this.paidInvoiceFilter.getValue();
        if(this.startDate){
            o.params.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        }
        if(this.endDate){
            o.params.enddate = WtfGlobal.convertToGenericStartDate(this.endDate.getValue());
        }
        o.params.isprofitreport = this.isprofitreport
    },
    handleResetClickNew: function() {
        this.SchemaDimension.reset();
        this.Dimension.reset();
        this.SchemaValue.reset();
        this.SchemaValue.disable();
        this.startDate.reset();
        this.endDate.reset();
        this.Store.load();
    },
    handleStoreOnLoad: function(store) {
        var columns = [];
//        columns.push(new Wtf.grid.RowNumberer({
//            width: 30
//        }));
//        columns.push(this.sm);
            Wtf.each(this.Store.reader.jsonData.columns, function(column) {
                if (column.renderer) {
                    column.renderer = eval('(' + column.renderer + ')');
                }
                if (column.summaryRenderer) {
                    column.summaryRenderer = eval('(' + column.summaryRenderer + ')');
                }
                if (column.hidden) {
                    column.hidden = eval('(' + column.hidden + ')');
                }
                columns.push(column);
            });
            this.grid.getColumnModel().setConfig(columns);
            this.grid.getView().refresh();

            if (this.Store.getCount() < 1) {
                this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText(this.emptytextKey));
                this.grid.getView().refresh();
            }
            if(columns.length > 1 && !this.isprofitreport){
                this.Store.sortInfo = {
                    field: 'schemaValue',
                    direction: "ASC"
                };
                this.Store.groupBy("schemaValue");
            }
//        this.quickPanelSearch.StorageChanged(store);
    },
    getDates: function(start) {
        var d = new Date();
        var monthDateStr = d.format('M d');
        if (Wtf.account.companyAccountPref.fyfrom)
            monthDateStr = Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd = new Date(monthDateStr + ', ' + d.getFullYear() + ' 12:00:00 AM');
        if (d < fd)
            fd = new Date(monthDateStr + ', ' + (d.getFullYear() - 1) + ' 12:00:00 AM');
        if (start)
            return fd;

        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
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
            pendingApproval: this.pendingApproval,
            flag: 1,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_GENERAL_LEDGER_ModuleId,
            filterConjuctionCriteria: filterConjuctionCriteria,
            startDate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            endDate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
        }
        this.Store.load({
            params: {
                start: 0,
                limit: 30
            }
        });
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_GENERAL_LEDGER_ModuleId,
            isFixedAsset: false,
            isLeaseFixedAsset: false,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.Store.load({
            params: {
                start: 0,
                limit: 30
            }
        });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    }
});