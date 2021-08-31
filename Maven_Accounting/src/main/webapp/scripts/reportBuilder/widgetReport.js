/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
function loadCustomWidgetReport(record){
    var panelid = "custom-widget-report-"+record.get("id");
    var panel = Wtf.getCmp(panelid);
    if(panel==null){
        panel = new Wtf.CustomWidgetReport({
            border: false,
            title : record.get("reportname"),
            id :panelid,
            autoScroll: true,
            closable : true,
            frame: false,
            record : record,
            iconCls:'pwnd widget-report'
        });
        
        panel.on("activate",function(){
            this.doLayout();
        })

    }
    Wtf.getCmp("as").add(panel);
    Wtf.getCmp("as").setActiveTab(panel);
}

Wtf.CustomWidgetReport = Wtf.extend(Wtf.Panel,{
    storeContainer : {},
    storeIdPrefix : "widget-report-store-",
    widgetIdPrefix : "custom-widget-",
    initComponent:function() {
        this.widgetIdPrefix = "custom-widget-"+this.id+"-";
        this.createNorthRegion();   
        this.createCenterRegion();   
        
        Wtf.apply(this, {
            items :[this.searchContainer,this.widgetContainer]
        }); 

        Wtf.CustomWidgetReport.superclass.initComponent.apply(this, arguments);
        this.on("resize", function () {
            this.doLayout();
        });

    },
    onDestroy : function(){
        Wtf.CustomWidgetReport.superclass.onDestroy.call(this);
    },
    onRender : function(){
        Wtf.CustomWidgetReport.superclass.onRender.apply(this, arguments);
        this.storeContainer = {};  // Container for store of report
        this.deletedStoreContainer = {};  // Container for store of deleted/Closed report
        this.createWidgets(); 
        this.searchcriteria = eval("(" + decodeURIComponent(this.record.get("searchcriteria")) + ")");
        if(this.searchcriteria != undefined && this.searchcriteria != "" && JSON.parse(this.searchcriteria).data && JSON.parse(this.searchcriteria).data.length > 0){
            var appendCase = this.record.get("filterappend") == 0 ? "OR" : "AND";
            showAdvanceSearch(this,this.searchcriteria,appendCase);
        }
        
    },
    createCenterRegion : function(){
        var tbarArray =[];
        
        this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
            name:'stdate' + this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            value:WtfGlobal.getDates(true)
        });
        tbarArray.push(WtfGlobal.getLocaleText("acc.common.from")," ",this.startDate);
   
        this.endDate=new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
            format:WtfGlobal.getOnlyDateFormat(),
            name:'enddate' + this.id,
            value:WtfGlobal.getDates(false)
        });
        tbarArray.push("-",WtfGlobal.getLocaleText("acc.common.to")," ",this.endDate);
        
        var fetchBtn = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.agedPay.fetch"),
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: function(){
                this.filterStore({
                    startDate : this.startDate.getValue(),
                    endDate :  this.endDate.getValue()
                });
            }
        });
        tbarArray.push("-"," ",fetchBtn);
        
        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });
        
        tbarArray.push("-"," ",this.AdvanceSearchBtn);
        
        this.column1 = new Wtf.Panel({
            columnWidth: .495,
            style:'padding:10px 1px 10px 10px;',
            cls: 'portletcls',
            border: false
        });
        
        this.column2 = new Wtf.Panel({
            columnWidth: .495,
            style:'padding:10px 0px 10px 10px',
            cls: 'portletcls',
            border: false
        });
        
        this.widgetContainer = new Wtf.ux.Portal({
            bodyStyle:"background:white;",
            tbar :tbarArray,
            border: false,
            items: [this.column1,this.column2]
        });
        this.widgetContainer.on("resize", function () {
            this.doLayout();
        }); 
    },
    
    createWidgets : function(){
        var selectedReports = this.record.get("customreports");
        if(selectedReports){
            this.selectedReportsJSON = JSON.parse(selectedReports);
            
            for(var i=0;i<this.selectedReportsJSON.length;i++){
                var widgetParent = i%2==0 ? this.column1:this.column2;
                if(this.selectedReportsJSON[i].isdefault){
                    this.addDefaultReportWidget(this.selectedReportsJSON[i],widgetParent,i); 
                }else{
                    this.addCustomReportWidget(this.selectedReportsJSON[i],widgetParent,i);
                }
            }
        }
        this.widgetContainer.doLayout();
        this.doLayout();
    },
    
    addDefaultReportWidget : function(reportRec,parentPanel,index){
        var storeContainer = this.storeContainer;
        var storeIdPrefix = this.storeIdPrefix;
        var widgetIdPrefix = this.widgetIdPrefix;
        
        var callbackFn = function(reportGrid){
            var reportWidget = Wtf.getCmp( widgetIdPrefix + reportRec.id );
            if(reportWidget){
                
                reportGrid.on("render",function(){
                   if(this.getBarChartConfig == undefined){
                       reportWidget.tools.barchartwizard.dom.style.display = "none";
                   }
                   
                   if(this.getPieChartConfig == undefined){
                       reportWidget.tools.piechartwizard.dom.style.display = "none";
                   }
                   
                   if(this.getLineChartConfig == undefined){
                       reportWidget.tools.linechartwizard.dom.style.display = "none";
                   }
                   
                   if(this.getBarChartConfig == undefined && this.getPieChartConfig == undefined && this.getLineChartConfig == undefined){
                       reportWidget.tools.reportwizard.dom.style.display = "none";
                   }
                });
                
                reportWidget.add(reportGrid);
                reportWidget.doLayout();
                var reportStore = reportGrid.store || reportGrid.Store;
                if(reportStore){
                    reportStore.isdefault = true;
                }else if(reportGrid.grid !== undefined ){
                    /*
                 * If reportStore is still undefined then get it from grid if grid is not undefined.
                 */
                    reportStore = reportGrid.grid.store;
                    reportStore.isdefault = true;
                }
                storeContainer[storeIdPrefix + index] = reportStore;
            }
        
        }
        
        this.addReportToWidget(undefined,reportRec,parentPanel,index);
        var methodName = reportRec.methodName;
        var panelId= reportRec.id+"-panel-"+index;
        var params = "";
        var origParams = "{";
        if (methodName.indexOf("{") > -1) {
            origParams = methodName.substring(methodName.indexOf("{"),methodName.indexOf("}"))+",";
        }
        params = origParams + " \"isCustomWidgetReport\" :true,\"callbackFn\":" + callbackFn + ",\"id\":\"" + panelId + "\"}";
        if(methodName){
            var methodName1st = methodName.substring(0,(methodName.indexOf("(")+1));
            var methodName2nd = methodName.substr(methodName.indexOf(")"),methodName.length);
            methodName = methodName1st + params + methodName2nd;
        }

        eval(methodName);
    },
    
    addCustomReportWidget : function (reportRec,parentPanel,index){
        
        var customReport = new Wtf.reportBuilder.CustomReport({
           reportRec :  reportRec
        });
        this.storeContainer[this.storeIdPrefix + index] = customReport.store;
        this.addReportToWidget(customReport,reportRec,parentPanel,index);
        
    },
    
    addReportToWidget : function(reportGrid , reportRec , parentPanel,index){
        
        var toolsArray = this.getToolsArray(reportRec);
        
        //        Create a panel
        var panel = new Wtf.Panel({
            tools: toolsArray,
            title: reportRec.reportname,
            panelNo : index,
            id : this.widgetIdPrefix + reportRec.id,
            layout:'card',
            draggable:{
                onDrag : function(e){
                    var pel = this.proxy.getEl();
                    this.x = pel.getLeft(true);
                    this.y = pel.getTop(true);

                    var s = this.panel.getEl().shadow;
                    if (s) {
                        s.realign(this.x, this.y, pel.getWidth(), pel.getHeight());
                    }
                },
                endDrag : function(e){
                    this.panel.setPosition(this.x, this.y);
                    this.panel.ownerCt.ownerCt.doLayout();
                }
            },
            height: 350,
            activeItem: 0,
            items : reportGrid ? [reportGrid] : undefined
        });
        
        parentPanel.add(panel);
        parentPanel.doLayout();
        panel.doLayout();
    },
    
    getToolsArray : function(reportRec){
        var toolsArray = [];
        
        //tools for default reports
        if(reportRec.isdefault){
            toolsArray.push({
                id:'reportwizard',
                qtip:'Report',
                handler: this.showReport
            });
            toolsArray.push({
                id:'barchartwizard',
                qtip:'Bar Chart',
                scope : this,
                handler: function(e, target, panel){
                    this.showChart({
                        panel : panel,
                        isBarChart : true,
                        reportRec : reportRec
                    });
                }
            });
            toolsArray.push({
                id:'linechartwizard',
                qtip:'Line Chart',
                scope : this,
                handler: function(e, target, panel){
                    this.showChart({
                        panel : panel,
                        isLineChart : true,
                        reportRec : reportRec
                    });
                }
            });
            toolsArray.push({
                id:'piechartwizard',
                qtip:'Pie Chart',
                scope : this,
                handler: function(e, target, panel){
                    this.showChart({
                        panel : panel,
                        isPieChart : true,
                        reportRec : reportRec
                    });
                }
            });
            
            toolsArray.push({
                id:'detailwizardlink',
                qtip:'Open into new Tab',
                handler: function(){
                    eval(reportRec.methodName);
                }
            });
        }
        
        toolsArray.push({
            id:'close',
            qtip:'Close',
            handler: this.removeWidget.createDelegate(this)
        });
        
        return toolsArray;
    },
    
    removeWidget : function(e, target, panel){
        panel.ownerCt.remove(panel, true);
        this.deletedStoreContainer[this.storeIdPrefix + panel.panelNo] = this.storeContainer[this.storeIdPrefix + panel.panelNo];
        delete this.storeContainer[this.storeIdPrefix + panel.panelNo];
    },
    
    showReport : function(e, target, panel){
        panel.getLayout().setActiveItem(0);
    },
    
    showChart : function(params){
        var panel = params.panel;
        var reportRec = params.reportRec;
        var reportPanel =  panel.getComponent(0);
                
        /*
         *Get chart params as per type of chart
         **/    
        var chartParams = {};
        if(params.isBarChart){
            chartParams = reportPanel.getBarChartParams();
            chartParams.url = reportPanel.getBarChartUrl();
            chartParams.chartConfig = reportPanel.getBarChartConfig(chartParams);
        }else if(params.isPieChart){
            chartParams = reportPanel.getPieChartParams(chartParams);
            chartParams.url = reportPanel.getPieChartUrl();
            chartParams.chartConfig = reportPanel.getPieChartConfig(chartParams);
        }else if(params.isLineChart){
            chartParams = reportPanel.getLineChartParams(chartParams);
            chartParams.url = reportPanel.getLineChartUrl();
            chartParams.chartConfig = reportPanel.getLineChartConfig(chartParams);
        }
        chartParams.reportId = reportRec.id;
                
        //get chart for report
        var chart = getReportChartPanel(chartParams);
            
        var component_index = 1;
            
            
        var chartPanel = panel.getComponent(component_index);
        if(chartPanel){
            panel.remove(chartPanel,true);
        }
        panel.insert(component_index,chart);
                
        panel.getLayout().setActiveItem(component_index);
    },
    
    createNorthRegion : function(){
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            moduleid: 102 ,
            hideRememberSerch : true,
//            hideCloseBttn : true,
            ignoreDefaultFields : true,
            height : 200,
            advSearch: false
        });
        this.objsearchComponent.advGrid.on("filterStore", function(json,filterConjuctionCriteria){
            this.filterStore({
                json : json,
                filterConjuctionCriteria : filterConjuctionCriteria
            });
        }, this);
        
        this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
        
        this.searchContainer = new Wtf.Panel({
            border: false,
            items: [this.objsearchComponent]
        });
        
        this.searchContainer.on("resize", function () {
            this.doLayout();
        });
    },
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
    },
    
    clearStoreFilter: function() {
        this.filterStore({
            json : "",
            filterConjuctionCriteria : ""
        });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },
    
    filterStore : function (params){
        var storeContainer = this.storeContainer;
        for (var storeId in storeContainer) {
            if (storeContainer.hasOwnProperty(storeId)) {
                
                var store = storeContainer[storeId];
                /*
                 * If store present then set baseParameters and load it else log error in console.
                 */
                if (store) {
                    if (params.json != undefined && params.filterConjuctionCriteria != undefined) {
                        store.baseParams.searchJson = params.json;
                        store.baseParams.filterConjuctionCriteria = params.filterConjuctionCriteria;
                    } else if (params.startDate != undefined && params.endDate != undefined) {
                        if (store.isdefault) {
                            store.baseParams.startdate = WtfGlobal.convertToGenericStartDate(params.startDate);
                            store.baseParams.enddate = WtfGlobal.convertToGenericEndDate(params.endDate);
                        } else {
                            store.baseParams.fromDate = WtfGlobal.convertToDateOnly(params.startDate);
                            store.baseParams.toDate = WtfGlobal.convertToDateOnly(params.endDate);
                        }
                    }
                    store.load({
                        params: {
                            start: 0,
                            limit: 30
                        }
                    });
                }
            }
        }
    }
});

