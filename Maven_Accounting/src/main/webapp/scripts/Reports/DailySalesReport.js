/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

function dailySalesReportDynamicLoad(params){
    var consolidateFlag = params.consolidateFlag || false;
    var isBookingReport = params.isBookingReport || false;
    var component_id = isBookingReport ? 'dailyBookingReport':'dailySalesReport';
    var panel=Wtf.getCmp(component_id);
    if (params.isCustomWidgetReport) {
        /*
         *Implementation to add this report in custom widget report. 
         * */
        panel = new Wtf.account.DailySalesReport({
            consolidateFlag:consolidateFlag,
            isBookingReport:isBookingReport,
            border: false,
            helpmodeid:28,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            isCustomWidgetReport : params.isCustomWidgetReport
        });
        if (params.callbackFn) {
            /*
             *call callback function to add this report to widget.
             **/
            params.callbackFn.call(this, panel);
        }
    } else{
    if(panel==null){
            panel = new Wtf.account.DailySalesReport({
                id: component_id,
                consolidateFlag:consolidateFlag,
                isBookingReport:isBookingReport,
                border: false,
                helpmodeid:28,
                layout: 'fit',
                iconCls: 'accountingbase agedrecievable',
                title: Wtf.util.Format.ellipsis((isBookingReport ? WtfGlobal.getLocaleText("acc.dailyBookingReport.title"):WtfGlobal.getLocaleText("acc.dailySalesReport.title")),Wtf.TAB_TITLE_LENGTH),
                tabTip:isBookingReport ? WtfGlobal.getLocaleText("acc.dailyBookingReport.title"):WtfGlobal.getLocaleText("acc.dailySalesReport.title"),  //Daily Sales Report
                closable: true
            });
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    }
}


Wtf.account.DailySalesReport = function(config) {
    this.consolidateFlag = config.consolidateFlag!=undefined? config.consolidateFlag:false;
    this.isBookingReport = config.isBookingReport!=undefined? config.isBookingReport:false;
    this.isCustomWidgetReport=config.isCustomWidgetReport||false;
    this.startMonthDate;
    this.id=config.id;
    this.btnArr = [];
    this.taskProgressArray = [];
    Wtf.apply(this, config);

    this.createGrid();
    this.CreateReportButtons();
    this.addButtonInArray();

    Wtf.account.DailySalesReport.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.DailySalesReport, Wtf.Panel, {
    onRender: function(config) {

        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid],
                    tbar: this.btnArr,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.Store,
                        searchField: this.quickPanelSearch,
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
        this.loadStore();
        Wtf.account.DailySalesReport.superclass.onRender.call(this, config);
    },
    handleStoreOnLoad: function(store) {
        var columns = [];
        columns.push(this.rowNo);
        var scope = this;
        for ( var i=0; i<this.Store.reader.jsonData.columns.length; i++){
            var rec = this.Store.reader.jsonData.columns[i];
            if(rec.renderer){
                rec.renderer = eval('('+ rec.renderer +')');
            }
            columns.push(rec);
        };
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();

        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
       WtfGlobal.resetAjaxTimeOut();
    },
    
    showChart : function(param) {
        var chartPanelID;
        var title;
        var tabTipParams = [];
        if (this.isBookingReport) {
            chartPanelID = (param.chartType == Wtf.chartType.line) ? "DailyBookingLineChart" : "DailyBookingPieChart";
            title = (param.chartType == Wtf.chartType.line) ? WtfGlobal.getLocaleText("acc.common.DailyBooking.LineChart") : WtfGlobal.getLocaleText("acc.common.DailyBooking.PieChart");
            tabTipParams.push(WtfGlobal.getLocaleText("acc.common.TT.DailyBooking"));
        } else {
            chartPanelID = (param.chartType == Wtf.chartType.line) ? "DailySalesLineChart" : "DailySalesPieChart";
            title = (param.chartType == Wtf.chartType.line) ? WtfGlobal.getLocaleText("acc.common.DailySales.LineChart") : WtfGlobal.getLocaleText("acc.common.DailySales.PieChart");
            tabTipParams.push(WtfGlobal.getLocaleText("acc.common.TT.DailySales"));
        }
        var chartParams = (param.chartType == Wtf.chartType.line) ? this.getLineChartParams() : this.getPieChartParams();
        chartParams.id = chartPanelID;
        chartParams.title = title;
        chartParams.url = (param.chartType == Wtf.chartType.line) ? this.getLineChartUrl() : this.getPieChartUrl();
        chartParams.chartConfig = (param.chartType == Wtf.chartType.line) ? this.getLineChartConfig(chartParams) : this.getPieChartConfig(chartParams);
        chartParams.tabTipParams = tabTipParams;
        var chart = Wtf.getCmp(chartPanelID);
        if(chart){
            Wtf.getCmp('as').remove(chart,true);
        }
        chart = getReportChartPanel(chartParams);
        Wtf.getCmp('as').add(chart);
        Wtf.getCmp('as').setActiveTab(chart);
        Wtf.getCmp('as').doLayout();
    },
    
    getStoreBaseParams: function () {
        if (this.startMonth.getValue() == "" || this.startYear.getValue() == "") {
            this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
            this.startYear.setValue(this.yearStore.data.items[0].json[1]);
        }
        this.sDate = this.startMonth.getValue() + ", " + this.startYear.getValue();
        if (this.sDate == "") {
            WtfComMsgBox(42, 2);
            return;
        }
        this.startMonthDate = new Date(this.startMonth.getValue() + " 01, " + this.startYear.getValue());
        var currentBaseParams = this.Store.baseParams;
//        currentBaseParams.chartType = "";
        currentBaseParams.stdate = WtfGlobal.convertToGenericStartDate(this.startMonthDate),
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.startMonthDate)
        
        return currentBaseParams;
    },
    getLineChartParams: function () {
        var chartParams = {
            params: {}
        }
        chartParams.params = this.getStoreBaseParams();
        chartParams.params.chartType = Wtf.chartType.line;
        chartParams.params.forMonthAndYear = this.startMonth.getValue() + " " + this.startYear.getValue();
        return chartParams;
    },
    getLineChartUrl: function () {
        return this.grid.store.url;
    },
    
    getPieChartParams: function () {
        var chartParams = {
            params: {}
        }
        chartParams.params = this.getStoreBaseParams();
        chartParams.params.chartType = Wtf.chartType.pie;
        chartParams.params.forMonthAndYear = this.startMonth.getValue() + " " + this.startYear.getValue();
        return chartParams;
    },
    getPieChartUrl: function () {
        return this.grid.store.url;
    },
    
    getLineChartConfig :function(chartParams){
        var params = {};
        params.titleField = "day";
        params.valueField = "amountinbase";
        params.chartColor = (this.isBookingReport) ? "#CC0000" : "#009987";
        params.textColor = (this.isBookingReport) ? "#009987" : "#CC0000";
        params.valueTitle = (this.isBookingReport) ? "Total Booking" : "Total Sales ( " + Wtf.pref.CurrencySymbol + " )";
        params.title = (this.isBookingReport) ? "Daily Booking Report" : "Daily Sales Report";
        params.categoryAxisTitle = "Day";
        params.unit = Wtf.pref.CurrencySymbol;
        params.height = 520;
        
        if(chartParams.params.forMonthAndYear){
            params.subTitle = chartParams.params.forMonthAndYear;
        }
        var chartConfig = getLineChartConfig(params);
        return chartConfig;
    },
    getPieChartConfig :function(chartParams){
        
        var params = {};
        params.titleField = "customername";
        params.valueField = "total";
        params.chartColor = (this.isBookingReport) ? "#00FF00" : "#50EBEC";
//        params.textColor = (this.isBookingReport) ? "#00ADB5" : "#00ADB5";
        params.title = (this.isBookingReport) ? "Daily Booking Report Against Customer" : "Daily Sales Report Against Customer";
        params.legendPosition = "right";
        params.unit = Wtf.pref.CurrencySymbol;
        params.height = 520;
        
        if(chartParams.params.stdate && chartParams.params.enddate){
            params.subTitle = chartParams.params.forMonthAndYear;
        }
        
        var chartConfig = getPieChartConfig(params);
        return chartConfig;
    },
    
    loadStore: function() {

        // get date from month & year drop-down lists
        if (this.startMonth.getValue() == "" || this.startYear.getValue() == ""){
            this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
            this.startYear.setValue(this.yearStore.data.items[0].json[1]);
        }

        this.sDate = this.startMonth.getValue() + ", " + this.startYear.getValue();

        if(this.sDate=="") {
            WtfComMsgBox(42,2);
            return;
        }

        this.startMonthDate = new Date(this.startMonth.getValue() + " 01, " + this.startYear.getValue());
        this.startMonthString = this.startMonth.getValue() + " " +this.startYear.getValue();

        this.expButton.setParams({
            stdate: WtfGlobal.convertToGenericStartDate(this.startMonthDate),
            enddate: WtfGlobal.convertToGenericEndDate(this.startMonthDate),
            dateRange: this.startMonthString,
            isDailySalesReport: true
        });

        this.printButton.setParams({
            stdate: WtfGlobal.convertToGenericStartDate(this.startMonthDate),
            enddate: WtfGlobal.convertToGenericEndDate(this.startMonthDate),
            name:"Daily Sales Report"
        })


        this.Store.load({
            params: {
                start: 0,
                limit: this.pP.combo != undefined ? this.pP.combo.value : 30,
                ss: this.quickPanelSearch.getValue(),
                pagingFlag: true,
                stdate: WtfGlobal.convertToGenericStartDate(this.startMonthDate),
                enddate: WtfGlobal.convertToGenericEndDate(this.startMonthDate)
            }
        });


    },
    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
        }
        this.startYear.reset();
        this.startMonth.reset();
        this.loadStore();
    },

    createGrid: function() {
        this.rowNo=new Wtf.grid.RowNumberer();
        this.sm = new Wtf.grid.CheckboxSelectionModel({
        });
        this.Store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: "totalCount",
                root: "data"
            }),
            url: this.isBookingReport? "ACCSalesOrderCMN/getDailySalesReportByCustomer.do":"ACCInvoiceCMN/getDailySalesReport.do",
            baseParams:{
                mode: 18,
                creditonly:false,
                nondeleted:true,
                getRepeateInvoice: false,
                consolidateFlag:this.consolidateFlag,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                userid:loginid				
            }
        });
        this.grid = new Wtf.grid.GridPanel({
            store: this.Store,
            border: false,
            columns: [],
            layout: 'fit',
            sm: this.sm,
            loadMask: true
        });
    },
    CreateReportButtons: function() {
        this.Store.on('load', this.handleStoreOnLoad, this);
        this.Store.on('beforeload', function () {
            WtfGlobal.setAjaxTimeOut();
            var currentBaseParams = this.getStoreBaseParams();
            this.Store.baseParams = currentBaseParams;
        }, this);
        this.Store.on('loadexception', function() {
            WtfGlobal.resetAjaxTimeOut();
        }, this);

        this.expGet = this.isBookingReport? Wtf.autoNum.DailyBookingsReport:Wtf.autoNum.DailySalesReport;

        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.dailySalesReport.emptySearchText"), //'Search by Task Customer Name'
            width: 200,
            id: "quickSearch" + this.id,
            field: 'transactionNumber'

        });

        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"),
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false,
            handler: this.handleResetClick
        });

        this.monthStore = new Wtf.data.SimpleStore({
            fields: [{
                name:'monthid',
                type:'int'
                }, 'name'],
            data :[[0,'January'],[1,'February'],[2,'March'],[3,'April'],[4,'May'],[5,'June'],[6,'July'],[7,'August'],[8,'September'],[9,'October'],
            [10,'November'],[11,'December']]
        });

        var data=WtfGlobal.getBookBeginningYear(true);

        this.yearStore= new Wtf.data.SimpleStore({
            fields: [{
                name:'id',
                type:'int'
                }, 'yearid'],
            data :data
        });

        this.startMonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  //'Month',
            name:'startMonth',
            displayField:'name',
            forceSelection: true,
            width:90,
            valueField:'name',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
        });

        this.startYear = new Wtf.form.ComboBox({
            store: this.yearStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  //'Year',
            name:'startYear',
            width:90,
            displayField:'yearid',
            valueField:'yearid',
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
        });

        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"),
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.loadStore
        });

        this.expButton=new Wtf.exportButton({
            obj:this,
            filename: (this.isBookingReport ? WtfGlobal.getLocaleText("acc.dailyBookingReport.title"):WtfGlobal.getLocaleText("acc.dailySalesReport.title"))+"_v1",
            text:WtfGlobal.getLocaleText("acc.common.export"),
            hidden:this.isCustomWidgetReport,
            tooltip :WtfGlobal.getLocaleText("acc.sales.exportTT"),  //'Export report details',
            params:{ 
                stdate: WtfGlobal.convertToGenericStartDate(this.startMonthDate),
                startdate: WtfGlobal.convertToGenericStartDate(this.startMonthDate),
                enddate: WtfGlobal.convertToGenericEndDate(this.startMonthDate)
            },
            menuItem:{
                csv:true,
                pdf:true,
                rowPdf:false,
                xls:true
            },
            get:this.expGet
        })

        this.printButton=new Wtf.exportButton({
            obj:this,
            text:WtfGlobal.getLocaleText("acc.common.print"),
            tooltip :WtfGlobal.getLocaleText("acc.sales.printTT"),  //'Print report details',
            hidden:this.isCustomWidgetReport,
            filename : this.isBookingReport ? WtfGlobal.getLocaleText("acc.dailyBookingReport.title"):WtfGlobal.getLocaleText("acc.dailySalesReport.title"),
            params:{ 	
                stdate: WtfGlobal.convertToGenericStartDate(this.startMonthDate),
                enddate: WtfGlobal.convertToGenericEndDate(this.startMonthDate),
                name:this.isBookingReport ? WtfGlobal.getLocaleText("acc.dailyBookingReport.title"):WtfGlobal.getLocaleText("acc.dailySalesReport.title")
            },
            label: WtfGlobal.getLocaleText("acc.sales.tabTitle"),
            menuItem:{
                print:true
            },
            get:this.expGet
        })

        if (this.startMonth.getValue() == "" || this.startYear.getValue() == ""){
            var temp=new Date();
            var year1=temp.getFullYear();
            this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
            this.startYear.setValue(year1);
        }

        this.chartButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.chart"),
            iconCls: "accountingbase chart",
            scope: this,
            hidden: this.isCustomWidgetReport,
            menu: [
                {
                    xtype: "button",
                    text: WtfGlobal.getLocaleText("acc.common.LineChart"),
                    iconCls: "x-tool-linechartwizard",
                    scope: this,
                    handler: function () {
                        var params = {}
                        params.chartType = Wtf.chartType.line;
                        this.showChart(params);
                    }
                },
                {
                    xtype: "button",
                    text: WtfGlobal.getLocaleText("acc.common.PieChart"),
                    iconCls: "x-tool-piechartwizard",
                    scope: this,
                    handler: function () {
                        var params = {}
                        params.chartType = Wtf.chartType.pie;
                        this.showChart(params)
                    }
                }
            ]
        })

    },
    addButtonInArray:function(){
        this.btnArr.push(this.quickPanelSearch);
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.common.from"));
        this.btnArr.push(this.startMonth);
        this.btnArr.push(this.startYear);
        this.btnArr.push(this.fetchBttn);
        this.btnArr.push('-', this.resetBttn);
        this.btnArr.push('-', this.expButton);
        this.btnArr.push('-', this.printButton);
        this.btnArr.push('-', this.chartButton);
    }

});
