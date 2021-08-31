/**
 * Top and Dormant Users
 * */

Wtf.TopAndDormantUsersChartPanel  = function(config) {
    this.chartId=config.id;
    Wtf.apply(this, config); 
    this.grid=new Wtf.Panel({
        layout:"fit",
        html:"<div id='chartdiv"+config.id+"' style='width: 50%; height: 400px;float:left'></div><div id='chartdiv1"+config.id+"' style='width: 50%; height: 400px;'></div>"
    });
    Wtf.TopAndDormantUsersChartPanel.superclass.constructor.call(this, {
        autoDestroy:true,
        border: false,
        bodyStyle:"background:none;" ,
        iconCls:'accountingbase graphTabIcon',
        layout :'fit',
        items:this.grid
    });
    this.on("activate",this.runChart,this);
};

Wtf.extend(Wtf.TopAndDormantUsersChartPanel, Wtf.Panel, {
 
    onRender:function(config){
        Wtf.TopAndDormantUsersChartPanel.superclass.onRender.call(this, config);
        
    },
    runChart:function(){
        var chart;
        
        var fieldName=(this.isProduct)?'productname':'custname';
        
        /* --------------------------- Chart 1 -----------------------------*/
        var rec=this.rec.data.items;
        var chartData= new Array();        
        for(var i=0;i<(rec.length<10?rec.length:10);i++){
            chartData.push(rec[i].data);
        }
        chart = new AmCharts.AmSerialChart();
        chart.dataProvider =chartData;
        chart.categoryField = fieldName;
        chart.startDuration = 1;

        // AXES
        // category
        var categoryAxis = chart.categoryAxis;
        categoryAxis.labelRotation = 90;
        categoryAxis.gridPosition = "start";
        chart.rotate="true";
        // value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.dashLength = 5;
        valueAxis.title = "Top "+(this.isProduct?"Products":(this.isCustomer?"Customers":"Vendors"));
        valueAxis.axisAlpha = 0;
        chart.addValueAxis(valueAxis);
        // in case you don't want to change default settings of value axis,
        // you don't need to create it, as one value axis is created automatically.
        // GRAPH
        var graph = new AmCharts.AmGraph();
        graph.valueField = "quantity";
        graph.balloonText = "[["+fieldName+"]]: [[quantity]]";
        graph.type = "column";
        graph.lineAlpha = 0;
        graph.fillAlphas = 0.8;
        graph.fillColorsField = "fill";
        chart.addGraph(graph);
    
        chart.addListener("clickGraphItem", function (event) {
            event.item.dataContext.fill = "#0079DC";
            event.chart.validateData();
        });
        chart.creditsPosition = "top";
        chart.exportConfig={
            "menuTop":"20px",
            "menuRight":"20px",
            "menuItems": [{
                "icon": '../../images/export.png',
                "format": 'png'
            }]
        }
        chart.write("chartdiv"+this.chartId);
        /* --------------------------- Ends Chart 1 -----------------------------*/
        /* --------------------------- Chart 2 -----------------------------*/   
        var rec1=this.rec1.data.items;
        var chartData= new Array();
        for(var i=0;i<(rec1.length<10?rec1.length:10);i++){
            chartData.push(rec1[i].data);
        }
        chart = new AmCharts.AmSerialChart();
        chart.dataProvider =chartData;
        chart.categoryField = fieldName;
        chart.startDuration = 1;

        // AXES
        // category
        var categoryAxis = chart.categoryAxis;
        categoryAxis.labelRotation = 90;
        categoryAxis.gridPosition = "start";
        chart.rotate="true";
        // value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.dashLength = 5;
        valueAxis.title = "Dormant "+(this.isProduct?"Products":(this.isCustomer?"Customers":"Vendors"));
        valueAxis.axisAlpha = 0;
        chart.addValueAxis(valueAxis);
        // in case you don't want to change default settings of value axis,
        // you don't need to create it, as one value axis is created automatically.
        // GRAPH
        var graph = new AmCharts.AmGraph();
        graph.valueField = "quantity";
        graph.balloonText = "[["+fieldName+"]]: [[quantity]]";
        graph.type = "column";
        graph.lineAlpha = 0;
        graph.fillAlphas = 0.8;
        graph.fillColorsField = "fill";
        chart.addGraph(graph);
    
        chart.addListener("clickGraphItem", function (event) {
            event.item.dataContext.fill = "#0079DC";
            event.chart.validateData();
        });
        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.cursorAlpha = 0;
        chartCursor.zoomable = false;
        chartCursor.categoryBalloonEnabled = false;
        chart.addChartCursor(chartCursor);

        chart.creditsPosition = "top";
        chart.exportConfig={
            "menuTop":"20px",
            "menuRight":"20px",
            "menuItems": [{
                "icon": '../../images/export.png',
                "format": 'png'
            }]
        }
        chart.write("chartdiv1"+this.chartId);
    /* --------------------------- Ends Chart 2 -----------------------------*/
    }
});
