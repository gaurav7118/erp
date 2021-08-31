/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

function monthlyBookingsReportDynamicLoad(params){
   
    var consolidateFlag = params.consolidateFlag || false;
    var isMonthlyBookings = params.isMonthlyBookings || false;
    var component_id = isMonthlyBookings ? 'monthlyBookingsReport':'yearlyBookingsReport';
    var panel=Wtf.getCmp(component_id);
     if (params.isCustomWidgetReport) {
        /*
         *Implementation to add this report in custom widget report. 
         * */
        panel = new Wtf.account.MonthlyBookingsReport({
            consolidateFlag:consolidateFlag,
            isMonthlyBookings:isMonthlyBookings,
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
        panel = new Wtf.account.MonthlyBookingsReport({
            id: component_id,
            consolidateFlag:consolidateFlag,
            isMonthlyBookings:isMonthlyBookings,
            border: false,
            helpmodeid:28,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            title: Wtf.util.Format.ellipsis((isMonthlyBookings ? WtfGlobal.getLocaleText("acc.monthlyBookingReport.title"):WtfGlobal.getLocaleText("acc.yearlyBookingReport.title")),Wtf.TAB_TITLE_LENGTH),
            tabTip:isMonthlyBookings ? WtfGlobal.getLocaleText("acc.monthlyBookingReport.title"):WtfGlobal.getLocaleText("acc.yearlyBookingReport.title"),
            closable: true
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    }
}


Wtf.account.MonthlyBookingsReport = function(config) {
    this.consolidateFlag = config.consolidateFlag!=undefined? config.consolidateFlag:false;
    this.isMonthlyBookings = config.isMonthlyBookings!=undefined? config.isMonthlyBookings:false;
    this.isCustomWidgetReport=config.isCustomWidgetReport||false;
    this.startMonthDate;
    this.id=config.id;
    this.btnArr = [];
    this.taskProgressArray = [];
    Wtf.apply(this, config);

    this.createGrid();
    this.CreateReportButtons();
    this.addButtonInArray();
    
    Wtf.account.MonthlyBookingsReport.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.MonthlyBookingsReport, Wtf.Panel, {
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
        Wtf.account.MonthlyBookingsReport.superclass.onRender.call(this, config);
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
    },
    
    showChart : function(param) {
        var chartPanelID;
        var title;
        var tabTipParams = [];
        if(this.isMonthlyBookings) {
            chartPanelID = (param.chartType == Wtf.chartType.bar) ? "monthlyBookingBarChart" : "monthlyBookingPieChart";
            title = (param.chartType == Wtf.chartType.bar) ? WtfGlobal.getLocaleText("acc.common.MonthlyBooking.BarChart") : WtfGlobal.getLocaleText("acc.common.MonthlyBooking.PieChart");
            tabTipParams.push(WtfGlobal.getLocaleText("acc.common.TT.MonthlyBooking"));
        } else {
            chartPanelID = (param.chartType == Wtf.chartType.bar) ? "yearlyBookingBarChart" : "yearlyBookingPieChart";
            title = (param.chartType == Wtf.chartType.bar) ? WtfGlobal.getLocaleText("acc.common.YearlyBooking.BarChart") : WtfGlobal.getLocaleText("acc.common.YearlyBooking.PieChart");
            tabTipParams.push(WtfGlobal.getLocaleText("acc.common.TT.YearlyBooking"));
        }
        var chartParams = (param.chartType == Wtf.chartType.bar) ? this.getBarChartParams() : this.getPieChartParams();
        chartParams.id = chartPanelID;
        chartParams.title = title;
        chartParams.url = (param.chartType == Wtf.chartType.bar) ? this.getBarChartUrl() : this.getPieChartUrl();
        chartParams.chartConfig = (param.chartType == Wtf.chartType.bar) ? this.getBarChartConfig(chartParams) : this.getPieChartConfig(chartParams);
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
        /*
         * Set start date and end date before loading report.
         */
        var startMonthName = this.startMonth.getValue();
        var startYear = this.startYear.getValue();
        var startDate = new Date(Date.parse(startMonthName + " 01" + ", " + startYear));

        var endMonthName = this.endMonth.getValue();
        var endYearName = this.endYear.getValue();
        var endMonthId = this.monthStore.getAt(this.monthStore.find('name', this.endMonth.getValue())).get('monthid');
        var isLeap = ((endYearName % 4) == 0 && ((endYearName % 100) != 0 || (endYearName % 400) == 0));
        var daysInMonth = [31, (isLeap ? 29 : 28), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][endMonthId];
        var endDate = new Date(Date.parse(endMonthName + " " + daysInMonth + ", " + endYearName));

        var currentBaseParams = this.Store.baseParams;

        currentBaseParams.ss = this.quickPanelSearch.getValue();
        currentBaseParams.pagingFlag = true;
        currentBaseParams.stdate = WtfGlobal.convertToGenericStartDate(startDate);
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(endDate);
        currentBaseParams.startDate = startDate
        currentBaseParams.endDate = endDate;
        currentBaseParams.chartType = "";

        return currentBaseParams;
    },
    getBarChartParams: function () {
        var chartParams = {
            params: {}
        }
        chartParams.params = this.getStoreBaseParams();
        chartParams.params.chartType = Wtf.chartType.bar;
        return chartParams;
    },
    getBarChartUrl: function () {
        return this.grid.store.url;
    },
    
    getPieChartParams: function () {
        var chartParams = {
            params: {}
        }
        chartParams.params = this.getStoreBaseParams();
        chartParams.params.chartType = Wtf.chartType.pie;
        return chartParams;
    },
    getPieChartUrl: function () {
        return this.grid.store.url;
    },
    
    getBarChartConfig :function(chartParams){
        var params = {};
        params.titleField = (this.isMonthlyBookings) ? "monthname" : "year";
        params.valueField = (this.isMonthlyBookings) ? "amountinbase" : "total";
        params.chartColor = (this.isMonthlyBookings) ? "#008080" : "#009987";
        params.textColor = (this.isMonthlyBookings) ? "#800000" : "#990012";
        params.valueTitle = "Total Sales ( " + Wtf.pref.CurrencySymbol + " )";
        params.title = (this.isMonthlyBookings) ? "Monthly Booking Report" : "Yearly Booking Report";
        params.unit = Wtf.pref.CurrencySymbol;
        params.height = 520;
        
        if(chartParams.params.stdate && chartParams.params.enddate){
            params.subTitle = "From : " + this.startMonth.getValue() + " " + this.startYear.getValue() + "\nTo : " + this.endMonth.getValue() + " " + this.endYear.getValue();
        }
        var chartConfig = getBarChartConfig(params);
        return chartConfig;
    },
    getPieChartConfig :function(chartParams){
        
        var params = {};
        params.titleField = "customername";
        params.valueField = "total";
        params.chartColor = (this.isMonthlyBookings) ? "#FFDB58" : "#597DFF";
//        params.textColor = (this.isMonthlyBookings) ? "#00ADB5" : "#00ADB5";
        params.title = (this.isMonthlyBookings) ? "Monthly Booking Report Against Customer" : "Yearly Booking Report Against Customer";
        params.legendPosition = "right";
        params.unit = Wtf.pref.CurrencySymbol;
        params.height = 520;
        
        if(chartParams.params.stdate && chartParams.params.enddate){
            params.subTitle = "From : " + this.startMonth.getValue() + " " + this.startYear.getValue() + "\nTo : " + this.endMonth.getValue() + " " + this.endYear.getValue();
        }
        
        var chartConfig = getPieChartConfig(params);
        return chartConfig;
    },

    loadStore: function() {
               
        // get date from month & year drop-down lists
        if (this.startMonth.getValue() == "" || this.startYear.getValue() == ""){
            this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
            this.startYear.setValue(this.yearStore.data.items[0].json[1]);
            this.endMonth.setValue(this.monthStore.data.items[this.monthStore.data.items.length-1].json[1]);  
            this.endYear.setValue(this.yearStore.data.items[0].json[1]);
        }

        this.sDate = this.startMonth.getValue() + ", " + this.startYear.getValue();
        this.eDate = this.endMonth.getValue() + ", " + this.endYear.getValue();

        if(this.sDate=="" || this.eDate=="") {
           WtfComMsgBox(42,2);
           return;
        }
        
        var startMonthDate = new Date(this.startMonth.getValue() + " 01, " + this.startYear.getValue());
        var endMonthDate = new Date(this.endMonth.getValue() + " 01, " + this.endYear.getValue());        

        if (this.startYear.getValue() >= this.endYear.getValue() && startMonthDate.getMonth() > endMonthDate.getMonth()){
            WtfComMsgBox(1,2);
            return;            
        }
        
        var months;
        months = (endMonthDate.getFullYear() - startMonthDate.getFullYear()) * 12;
        months -= startMonthDate.getMonth();
        months += endMonthDate.getMonth();
        if (months<0)
            months=0;

        if (this.isMonthlyBookings && months>18){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Onlymaximum18monthsaresupported") ], 2);
            return;            
        }

        var startMonthName = this.startMonth.getValue();
        var startYear = this.startYear.getValue();
        var startDate = new Date(Date.parse(startMonthName + " 01" + ", "+ startYear));
        
        var endMonthName = this.endMonth.getValue();
        var endYearName = this.endYear.getValue();
        var endMonthId = this.monthStore.getAt(this.monthStore.find('name', this.endMonth.getValue())).get('monthid');
        var isLeap = ((endYearName % 4) == 0 && ((endYearName % 100) != 0 || (endYearName % 400) == 0));
        var daysInMonth = [31, (isLeap ? 29 : 28), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][endMonthId];
        var endDate = new Date(Date.parse(endMonthName + " " + daysInMonth  + ", "+ endYearName));

        this.startMonthString = this.startMonth.getValue() + " " +this.startYear.getValue();
        
        this.expButton.setParams({
            stdate: WtfGlobal.convertToGenericStartDate(startDate),
            startdate: WtfGlobal.convertToGenericStartDate(startDate),
            enddate: WtfGlobal.convertToGenericEndDate(endDate),
            dateRange: this.startMonthString,
            isDailySalesReport: true
        });

        this.printButton.setParams({
            stdate: WtfGlobal.convertToGenericStartDate(startDate),
            startdate: WtfGlobal.convertToGenericStartDate(startDate),
            enddate: WtfGlobal.convertToGenericEndDate(endDate),
            name:"Daily Sales Report"
        })
        
        
        this.Store.load({
            params: {
                start: 0,
                limit: this.pP.combo != undefined ? this.pP.combo.value : 30,
                ss: this.quickPanelSearch.getValue(),
                pagingFlag: true,
                stdate: WtfGlobal.convertToGenericStartDate(startDate),
                enddate: WtfGlobal.convertToGenericEndDate(endDate)			
            }
        });


    },
    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
        }
        this.startMonth.reset();
        this.startYear.reset();
        this.endMonth.reset();
        this.endYear.reset();
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
            url: this.isMonthlyBookings? "ACCSalesOrderCMN/getMonthlySalesOrdesByCustomer.do":"ACCSalesOrderCMN/getYearlySalesOrdersByCustomer.do",
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
        this.Store.on("beforeload", function () {
            this.getStoreBaseParams()
        }, this);
        
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
        this.expGet = this.isMonthlyBookings? Wtf.autoNum.MonthlyBookingsReport:Wtf.autoNum.YearlyBookingsReport;
        
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
            hidden:this.isCustomWidgetReport,  //Hide this button from widget view.
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
            selectOnFocus:true,
            hidden:!this.isMonthlyBookings
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
        
        this.endMonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  //'Month',
            name:'endMonth',
            displayField:'name',
            forceSelection: true,
            anchor:'95%',
            valueField:'name',
            mode: 'local',
            triggerAction: 'all',
            width:90,
            selectOnFocus:true,
            hidden:!this.isMonthlyBookings
        }); 

        this.endYear = new Wtf.form.ComboBox({
            store: this.yearStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  //'Year',
            name:'endYear',
            displayField:'yearid',
            anchor:'95%',
            valueField:'yearid',
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            width:90,
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
            filename: this.isMonthlyBookings ? WtfGlobal.getLocaleText("acc.monthlyBookingReport.title"):WtfGlobal.getLocaleText("acc.yearlyBookingReport.title"),
            text:WtfGlobal.getLocaleText("acc.common.export"),
            hidden:this.isCustomWidgetReport,   //Hide this button from widget view.
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
            hidden:this.isCustomWidgetReport,  //Hide this button from widget view.
            filename : this.isMonthlyBookings ? WtfGlobal.getLocaleText("acc.monthlyBookingReport.title"):WtfGlobal.getLocaleText("acc.yearlyBookingReport.title"),
            params:{ 	
                stdate: WtfGlobal.convertToGenericStartDate(this.startMonthDate),
                startdate: WtfGlobal.convertToGenericStartDate(this.startMonthDate),
                enddate: WtfGlobal.convertToGenericEndDate(this.startMonthDate),			
                name: this.isMonthlyBookings ? WtfGlobal.getLocaleText("acc.monthlyBookingReport.title"):WtfGlobal.getLocaleText("acc.yearlyBookingReport.title")
            },
            label: this.isMonthlyBookings ? WtfGlobal.getLocaleText("acc.monthlyBookingReport.title"):WtfGlobal.getLocaleText("acc.yearlyBookingReport.title"),
            menuItem:{
                print:true
            },
            get:this.expGet
        })
        this.chartButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.chart"),
            iconCls: "accountingbase chart",
            scope: this,
            hidden: this.isCustomWidgetReport,
            menu: [
                {
                    xtype: "button",
                    text: WtfGlobal.getLocaleText("acc.common.BarChart"),
                    iconCls: "x-tool-barchartwizard",
                    scope: this,
                    handler: function () {
                        var params = {}
                        params.chartType = Wtf.chartType.bar;
                        this.showChart(params)
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
        
        if (this.startMonth.getValue() == "" || this.startYear.getValue() == ""){
            var temp=new Date();
            var year1=temp.getFullYear();
            if(!this.isMonthlyBookings){
                year1= this.yearStore.data.items[0].data.yearid;
            }
            this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
            this.startYear.setValue(year1);
        }
        if (this.endMonth.getValue() == "" || this.endYear.getValue() == ""){
            var temp1=new Date();
            var year2=temp1.getFullYear();
            this.endMonth.setValue(this.monthStore.data.items[this.monthStore.data.items.length-1].json[1]);
            this.endYear.setValue(year2);
        }
        
    },
    addButtonInArray: function () {
        if (!this.isCustomWidgetReport) { //Hide this button from widget view.
            this.btnArr.push(this.quickPanelSearch);
        }
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.common.from"));
        this.btnArr.push(this.startMonth);
        this.btnArr.push(this.startYear);
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.common.to"));
        this.btnArr.push(this.endMonth);
        this.btnArr.push(this.endYear);
        this.btnArr.push(this.fetchBttn);
        this.btnArr.push('-',this.resetBttn);
        this.btnArr.push('-',this.expButton);
        this.btnArr.push('-',this.printButton);
        this.btnArr.push('-',this.chartButton);
    }
    
});

