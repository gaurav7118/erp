/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Wtf.Chart = function(config){
    Wtf.apply(this, config);
    WtfGlobal.setAjaxTimeOut();
    Wtf.Ajax.requestEx({
        url: this.dataUrl,
        params :this.params
    }, this, function(resData,response) {
        WtfGlobal.resetAjaxTimeOut();
        this.loadChart(resData);
    }, function() {
        WtfGlobal.resetAjaxTimeOut();
    });
    Wtf.Chart.superclass.constructor.call(this,config);
};

Wtf.extend(Wtf.Chart,Wtf.Panel,{
//     initComponent : function(){
//        Wtf.Chart.superclass.initComponent.call(this);
//
//        this.store = Wtf.StoreMgr.lookup(this.store);
//
//    },

    loadChart : function(resData){
        if (resData.data != undefined) {
            this.chartdata = resData.data;
        } else {
            this.chartdata = resData;
        }
        
        if(this.rendered){
            this.createChart();
        }else{
            this.on("render",function(){
                this.createChart();
            },this);
        }
    },
    createChart : function(){
        var chartDiv = this.el.dom.children[1] !=undefined ? this.el.dom.children[1]:this.el.dom.children[0];
        var chartId = this.body.id;
        
        if (this.chartConfig.dataProvider == undefined) {
            this.chartConfig.dataProvider = this.chartdata;
        }
        
        if(this.chartConfig.dataProvider!= undefined && this.chartConfig.dataProvider.length > 0){
            this.chart = AmCharts.makeChart( chartId, this.chartConfig);
        }else{
           this.body.dom.innerHTML =  "<div class='grid-empty-text disabled-record' style = 'margin-top: 25px;'>" + WtfGlobal.getLocaleText("acc.common.norec") + "</div>";
        }
        
        this.fireEvent("render", this);
    }
});

Wtf.ChartPanel = function(config){
    Wtf.ChartPanel.superclass.constructor.call(this,config);
    config.store.on("load",this.loadChart,this);
};

Wtf.extend(Wtf.ChartPanel,Wtf.Panel,{
     initComponent : function(){
        Wtf.ChartPanel.superclass.initComponent.call(this);

        this.store = Wtf.StoreMgr.lookup(this.store);

    },

    loadChart : function(){
        this.chartdata=[];
        this.store.each(function(rec){
            this.chartdata.push(rec.data);
        },this);
        
        if(this.rendered){
            this.createChart();
        }else{
            this.on("render",function(){
                this.createChart();
            },this);
        }
    },
    createChart : function(){
        var chartDiv = this.el.dom.children[1] !=undefined ? this.el.dom.children[1]:this.el.dom.children[0];
        var chartId = chartDiv.children[0].id;
        
//        if (this.chartdata.length > 0) {
            this.chartConfig.dataProvider = this.chartdata;
//        }
        
        this.chart = AmCharts.makeChart( chartId, this.chartConfig);
    }
});

Wtf.RawChart = function (config) {
    Wtf.RawChart.superclass.constructor.call(this, config);
    this.on("render", function () {
        this.createChart();
    }, this);
};

Wtf.extend(Wtf.RawChart,Wtf.Panel,{
//     initComponent : function(){
//        Wtf.RawChart.superclass.initComponent.call(this);
//
//        this.store = Wtf.StoreMgr.lookup(this.store);
//
//    },

//    loadChart : function(){
//        this.chartdata=[];
//        this.store.each(function(rec){
//            this.chartdata.push(rec.data);
//        },this);
//        
//        if(this.rendered){
//            this.createChart();
//        }else{
//            this.on("render",function(){
//                this.createChart();
//            },this);
//        }
//    },
    createChart : function(){
        var chartDiv = this.el.dom.children[1] !=undefined ? this.el.dom.children[1]:this.el.dom.children[0];
        var chartId = chartDiv.children[0].id;
        
//        if (this.chartdata.length > 0) {
//            this.chartConfig.dataProvider = this.chartdata;
//        }
        
        this.chart = AmCharts.makeChart( chartId, this.chartConfig);
    }
});

/*
 *Get Chart Panel for report
 *@params
 * chartConfig (required) : chartConfig for report
 * url (required) : url to fetch data
 * params (required) : required params to fetch data
 * id (optional)
 * title (optional)
 * height (optional) : If not given layout fit will apply
 **/

function getReportChartPanel(chartParams) {
    var id = chartParams.id;
    var panel;
    if(id!=undefined){
        panel = Wtf.getCmp(id);
    }
    if (panel == undefined) {
        var chartConfig = chartParams.chartConfig;
        if(chartConfig == undefined){
            chartConfig = {};
        }
        var tabTipParams = [];
        tabTipParams.push(chartParams.tabTipParams);
        tabTipParams.push(chartParams.params.chartType);
        panel = new Wtf.Chart({
            title : chartParams.title,
            id : id,
            border : false,
            layout : chartParams.height ? undefined : "fit",
            closable : true,
            dataUrl : chartParams.url,
            params : chartParams.params,
            chartConfig : chartConfig,
            height : chartParams.height,
            tabTip: WtfGlobal.getLocaleText({key:"acc.common.TT.ChartPanel", params:tabTipParams})
        })

    }
    return panel;
}

function getPieChartConfig(chartParams){
    var chartConfig={
        "type": "pie",
        "titleField": chartParams.titleField,
        "valueField": chartParams.valueField,
        "colorField": chartParams.colorField,
        "labelsEnabled": false,
        "innerRadius": "40%",
        "theme": 'light',
        "colors": [chartParams.chartColor],
        "color": chartParams.textColor,
        "prefixesOfBigNumbers": [
//            {
//                "number": 1000,
//                "prefix": "k"
//            },
            {
                "number": 1000000,
                "prefix": "M"
            },
            {
                "number": 1000000000,
                "prefix": "G"
            }
        ],
        "usePrefixes": true,
        "defs": {
            "filter": [{
                "id": "shadow",
                "width": "200%",
                "height": "200%",
                "feOffset": {
                    "result": "offOut",
                    "in": "SourceAlpha",
                    "dx": 0,
                    "dy": 0
                },
                "feGaussianBlur": {
                    "result": "blurOut",
                    "in": "offOut",
                    "stdDeviation": 2
                },
                "feBlend": {
                    "in": "SourceGraphic",
                    "in2": "blurOut",
                    "mode": "normal"
                }
            }]
        },
        "legend": {
            "enabled": true,
            "position": chartParams.legendPosition || "right",
            "marginRight": 20,
            "autoMargins": false,
            "unit": chartParams.unit ||  "",
            "unitPosition": "left"
        },
        "export": {
            "enabled": false
        },
        "titles": [{
            "text": chartParams.title,
            "size": 15
        }]
    }
        
    if(chartParams.subTitle){
        chartConfig.titles[1] = {
            "text" : chartParams.subTitle,
            bold : false,
            size:11
        }
    }
    return chartConfig;
}
    
    
function getBarChartConfig(chartParams){
    var chartConfig ={
        "type": "serial",
        "theme": "light",
        "autoMargins": false,
        "marginTop": 100,
        "marginBottom": 100,
        "marginLeft": 130,
        "marginRight": 75,
        "colors": [chartParams.chartColor],
        "color": chartParams.textColor,
        "valueAxes": [{
            "gridColor": "#FFFFFF",
            "gridAlpha": 0.2,
            "dashLength": 0,
            "unitPosition": "left",
            "title": chartParams.valueTitle
        }],
        "titles": [
        {
            "size": 18,
            "text": chartParams.title
        }],
        "gridAboveGraphs": true,
        "startDuration": 1,
        "graphs": [{
            "balloonText": "[[category]]: <b>"+chartParams.unit +" [[value]]</b>",
            "fillAlphas": 1,
            "lineAlpha": 0.2,
            "type": "column",
            "valueField": chartParams.valueField
        }],
        "chartCursor": {
            "categoryBalloonEnabled": false,
            "cursorAlpha": 0,
            "zoomable": false
        },
        "categoryField": chartParams.titleField,
        "categoryAxis": {
            "gridPosition": "start",
            "gridAlpha": 0,
            "tickPosition": "start",
            "tickLength": 20
        },
        "prefixesOfBigNumbers": [
//            {
//                "number": 1000,
//                "prefix": "k"
//            },
            {
                "number": 1000000,
                "prefix": "M"
            },
            {
                "number": 1000000000,
                "prefix": "G"
            }
        ],
        "usePrefixes": true,
        "export": {
            "enabled": false
        }

    }
        
    if(chartParams.subTitle){
        chartConfig.titles[1] = {
            "text" : chartParams.subTitle,
            bold : false,
            size:11
        }
    }
    return chartConfig;
}

function getLineChartConfig(chartParams) {
    var chartConfig = {
        "type": "serial",
        "autoResize": true,
        "autoMargins": false,
        "marginTop": 100,
        "marginBottom": 100,
        "marginLeft": 130,
        "marginRight": 75,
        "categoryField": chartParams.titleField,
        "colors": [chartParams.chartColor],
        "color": chartParams.textColor,
        "categoryAxis": {
            "gridPosition": "start",
            "labelFrequency": 1,
            "title" : chartParams.categoryAxisTitle
        },
        "graphs": [
            {
                "balloonText": "[[category]] " + chartParams.subTitle + " : <b>" + chartParams.unit + " [[value]]</b>",
                "bullet": "round",
                "id": "dailySalesChart",
                "title": "Daily Sales Report",
                "type": "smoothedLine",
                "valueField": chartParams.valueField
            }
        ],
        "guides": [],
        "valueAxes": [
            {
                "id": "ValueAxis",
                "title": chartParams.valueTitle
            }
        ],
        "allLabels": [],
        "titles": [
            {
                "id": "Title",
                "size": 15,
                "text": chartParams.title
            }
        ],
        "prefixesOfBigNumbers": [
//            {
//                "number": 1000,
//                "prefix": "k"
//            },
            {
                "number": 1000000,
                "prefix": "M"
            },
            {
                "number": 1000000000,
                "prefix": "G"
            }
        ],
        "usePrefixes": true,
        "export": {
            "enabled": false
        }
    }
    
    if(chartParams.subTitle){
        chartConfig.titles[1] = {
            "text" : chartParams.subTitle,
            bold : false,
            size:11
        }
    }
    
    return chartConfig;
}

function getMultiLineChartConfig(chartParams) {
    var chartConfig = {
        "type": "serial",
        "autoResize": true,
        "autoMargins": true,
        "marginTop": 50,
        "marginBottom": 50,
        "marginLeft": 70,
        "marginRight": 10,
        "categoryField": chartParams.titleField,
        "colors": [chartParams.chartColor],
        "color": chartParams.textColor,
        "categoryAxis": {
            "gridPosition": "start",
            "gridAlpha": 0,
            "labelFrequency": 1,
            "title" : chartParams.categoryAxisTitle
        },
        "graphs": [],
        "guides": [],
        "valueAxes": [
            {
                "id": "ValueAxis",
                "gridAlpha": 0,
                "title": chartParams.valueTitle
            }
        ],
        "allLabels": [],
        "titles": [
            {
                "id": "Title",
                "size": 15,
                "text": chartParams.title
            }
        ],
        "prefixesOfBigNumbers": [
//            {
//                "number": 1000,
//                "prefix": "k"
//            },
            {
                "number": 1000000,
                "prefix": "M"
            },
            {
                "number": 1000000000,
                "prefix": "G"
            }
        ],
        "usePrefixes": true,
        "legend": {
            "enabled" : true,
            "autoMargins": false,
            "align": "right",
            "marginLeft": 10,
            "marginRight": 0,
            "position" : "right",
            "labelText": "[[title]]",
            "useGraphSettings": true
        },
        "chartScrollbar": {
		"enabled": true
	},
//        "valueScrollbar": {
//		"enabled": true
//	},
        "gridAboveGraphs" : false,
        "export": {
            "enabled": false
        }
    }
    
    if(chartParams.valueField) {
        for(var i = 0; i < chartParams.valueField.length; i++) {
            chartConfig.graphs[i] = {
                "id": chartParams.valueField[i] + " graph",
                "title" : chartParams.valueField[i],
                "lineThickness": 2,
                "balloonText": "[[title]] : <b>" + chartParams.unit + " [[value]]</b>",
                "bullet": chartParams.ghraphBulletType[i],
                "lineColor": chartParams.lineColor[i],
                "type": "smoothedLine",
                "valueField": chartParams.valueField[i]
            }
        }
    }
    
    if(chartParams.subTitle){
        chartConfig.titles[1] = {
            "text" : chartParams.subTitle,
            bold : false,
            size:11
        }
    }
    
    return chartConfig;
}

function getClusteredBarChartConfig(chartParams) {
    var chartConfig ={
        "type": "serial",
        "theme": "light",
        "autoMargins": false,
        "marginTop": 100,
        "marginBottom": 100,
        "marginLeft": 130,
        "marginRight": 75,
        "colors": [chartParams.chartColor],
        "color": chartParams.textColor,
        "valueAxes": [{
            "gridColor": "#FFFFFF",
            "gridAlpha": 0.2,
            "dashLength": 0,
            "unitPosition": "left",
            "title": chartParams.valueTitle
        }],
        "titles": [
        {
            "size": 18,
            "text": chartParams.title
        }],
        "startDuration": 1,
        "graphs": [],
        "categoryField": chartParams.titleField,
        "categoryAxis": {
            "gridPosition": "start",
            "gridAlpha": 0,
            "title" : chartParams.categoryAxisTitle
        },
        "legend": {
            "enabled": true,
            "position": "bottom",
            "align": "top",
            "labelText": "[[title]]",
            "useGraphSettings": true
        },
        "prefixesOfBigNumbers": [
//            {
//                "number": 1000,
//                "prefix": "k"
//            },
            {
                "number": 1000000,
                "prefix": "M"
            },
            {
                "number": 1000000000,
                "prefix": "G"
            }
        ],
        "usePrefixes": true,
        "export": {
            "enabled": false
        }
    }
    
    if(chartParams.valueField) {
        for(var i = 0; i < chartParams.valueField.length; i++) {
            chartConfig.graphs[i] = {
                "id": chartParams.valueField[i] + " graph",
                "title": chartParams.legendTitle[i],
                "balloonText": "Amount " + chartParams.legendTitle[i] + " in <b>[[category]] : "+ chartParams.unit +" [[value]]</b>",
                "fillAlphas": 1,
                "lineAlpha": 0,
                "fillColors":chartParams.chartColor[i],
                "type": "column",
                "valueField": chartParams.valueField[i]
            }
        }
    }
    
    if(chartParams.subTitle){
        chartConfig.titles[1] = {
            "text" : chartParams.subTitle,
            bold : false,
            size:11
        }
    }
    
    return chartConfig;
}