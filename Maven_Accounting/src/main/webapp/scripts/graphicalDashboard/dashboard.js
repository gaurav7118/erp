/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

function openDefaultDashboard(){
    var style = "background:white;";
    //if (Wtf.DashBoardImageFlag) {
    //style = 'background: url("../../images/graphicalDashboard/square.gif");';
    //style = 'background-color: #E7E7E7;';
    style = 'background-color: #f0f0f0;';
    //}
    var column1Arr = [];
    var column2Arr = [];
    var panelArr = [];

    var searchRecord = Wtf.data.Record.create([{
        name: 'accname'
    }, {
        name: 'colorpaid'
    }, {
        name: 'colorreceived'
    }, {
        name: 'colordue'
    }, {
        name: 'salevalue'
    }, {
        name: 'purchasevalue'
    }, {
        name: 'name'
    }, {
        name: 'value'
    }, {
        name: 'amountreceived'
    }, {
        name: 'amountdue'
    }, {
        name: 'monthname'
    }, {
        name: 'profit'
    }, {
        name: 'loss'
    }, {
        name: 'amountpaid'

    }, {
        name: 'date'
    }, {
        name: 'color'
    }
    ]);

    var jsonReader = new Wtf.data.JsonReader({
        root: "data",
        totalProperty: 'count'
    }, searchRecord);

    var totalSalesstore = new Wtf.data.Store({
        reader: jsonReader,
        url: "ACCInvoiceCMN/getSalesReportRepGraphical.do",
        baseParams: {
            companyids: companyid,
            consolidateFlag: false,
            creditonly: false,
            dir: 'ASC',
            enddate: 'December, 2016',
            gcurrencyid: Wtf.account.companyAccountPref.currencyid,
            getRepeateInvoice: false,
            limit: 5,
            mode: 18,
            nondeleted: true,
            start: 0,
            stdate: 'January, 2016'
        }
    });
    totalSalesstore.load();
    var monthlySalesstore = new Wtf.data.Store({
        reader: jsonReader,
        url: "ACCInvoiceCMN/getMonthlySalesReportGraphical.do",
        baseParams: {
            companyids: companyid,
            consolidateFlag: false,
            creditonly: false,
            dir: 'ASC',
            enddate: 'December, 2016',
            gcurrencyid: Wtf.account.companyAccountPref.currencyid,
            getRepeateInvoice: false,
            limit: 5,
            mode: 18,
            nondeleted: true,
            start: 0,
            stdate: 'January, 2016'
        }
    });
    monthlySalesstore.load();

    var agedPayableStore = new Wtf.data.Store({
        reader: jsonReader,
        url: "ACCGoodsReceiptCMN/getAccountPayableChartGraphical.do",
        baseParams: {
            personlimit: 1,
            creditonly: true,
            persongroup: false,
            isagedgraph: true,
            withinventory: true,
            nondeleted: true,
            deleted: false,
            cid: companyid
        }
    });
    agedPayableStore.load();

    var agedReceivableStore = new Wtf.data.Store({
        reader: jsonReader,
        url: "ACCGoodsReceiptCMN/getAccountReceivedChartGraphical.do",
        baseParams: {
            personlimit: 1,
            creditonly: true,
            persongroup: false,
            isagedgraph: true,
            withinventory: true,
            nondeleted: true,
            deleted: false,
            cid: companyid
        }
    });
    agedReceivableStore.load();


    var monthlyTradingStore = new Wtf.data.Store({
        reader: jsonReader,
        url: "ACCReports/getMonthlyTradingAndProfitLossGraphical.do",
        baseParams: {
            enddate: 'December, 2016',
            isHierachicalGrid: true,
            isWidgetRequest: true,
            mode: 65,
            nondeleted: true,
            reportView: 'MonthlyTradingAndProfitLoss',
            singleGrid: true,
            stdate: 'January, 2016'
        }
    });
    monthlyTradingStore.load();

    var salesTrendPanel = new Wtf.ChartPanel({
        store: monthlySalesstore,
        style:"background-color:white;",
        chartConfig: {
            dataDateFormat: "YYYY-MM-dd",
            "theme": "",
            "type": "serial",
            "marginRight": 80,
            "autoMarginOffset": 20,
            "marginTop": 20,
            "valueAxes": [{
                gridThickness: 0,
                unit: "$",
                unitPosition: "left"
            }],
            "graphs": [{
                title:'Sale',
                "balloonText": "[[category]]<br><b>Sale Value: [[value]]</b>",
                "bullet": "round",
                "bulletBorderAlpha": 2,
                "hideBulletsCount": 50,
                "lineThickness": 2,
                "valueField": "salevalue"
            },{
                title:'Purchase',
                "balloonText": "[[category]]<br><b>Purchase Value: [[value]]</b>",
                "bullet": "round",
                "bulletBorderAlpha": 5,
                "hideBulletsCount": 50,
                "lineThickness": 2,
                "valueField": "purchasevalue"
            }],
            "categoryField": "date",
            "categoryAxis": {
                "parseDates": true,
                gridThickness: 0
            },
            "export": {
                "enabled": true,
                "fileName": "Monthly_Trend"
            },
            "legend": {
                labelText:"[[title]]",
                "useGraphSettings": true
            },
            "titles": [
            {
                "text": "Monthly Trend",
                "size": 15
            }]
        },
        height: 300
    });

    var totalSaleRepStore = new Wtf.data.Store({
        reader: jsonReader,
        url: "ACCInvoiceCMN/getSalesReportRepGraphical.do",
        baseParams: {
            companyids: companyid,
            consolidateFlag: false,
            creditonly: false,
            dir: 'ASC',
            enddate: 'December, 2016',
            gcurrencyid: Wtf.account.companyAccountPref.currencyid,
            getRepeateInvoice: false,
            limit: 5,
            mode: 18,
            nondeleted: true,
            start: 0,
            stdate: 'January, 2016'
        }
    });
    totalSaleRepStore.load();
    var topProductsStore = new Wtf.data.Store({
        reader: jsonReader,
        url: "ACCInvoiceCMN/getTopProductsGraphical.do",
        baseParams: {
            startdate: Wtf.account.companyAccountPref.fyfrom,
            stdate: Wtf.account.companyAccountPref.fyfrom,
            isTopProducts: true,
            isTopCustomers: false,
            isTopAgents: false,
            enddate: new Date(),
            countNumber: 5,
            isForChart: true
        }
    });
    topProductsStore.load();
    var topCustomersStore = new Wtf.data.Store({
        reader: jsonReader,
        url: "ACCInvoiceCMN/getTopCustomersGraphical.do",
        baseParams: {
            startdate: Wtf.account.companyAccountPref.fyfrom,
            stdate: Wtf.account.companyAccountPref.fyfrom,
            isTopProducts: true,
            isTopCustomers: false,
            isTopAgents: false,
            enddate: new Date(),
            countNumber: 5,
            isForChart: true
        }
    });
    topCustomersStore.load();

    var topProducts = new Wtf.ChartPanel({
        store: topProductsStore,
        style:"background-color:white;",
        chartConfig: {
            "type": "pie",
            "titleField": "name",
            "valueField": "value",
            "colorField": "color",
            labelsEnabled: false,
            "innerRadius": "40%",
            theme: 'light',
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
                "position": "bottom",
                "marginRight": 20,
                "autoMargins": false,
                unit: "$",
                unitPosition: "left"
            },
            "export": {
                "enabled": true,
                "fileName": "Top_Products"
            },
            "titles": [
            {
                "text": "Top Products",
                "size": 15
            }
            ]
        },
        height: 400
    });
    var topCustomers = new Wtf.ChartPanel({
        store: topCustomersStore,
        style:"background-color:white;",
        chartConfig: {
            "type": "pie",
            "titleField": "name",
            "valueField": "value",
            "colorField": "color",
            labelsEnabled: false,
            "innerRadius": "40%",
            theme: 'light',
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
                "position": "bottom",
                "marginRight": 20,
                "autoMargins": false
            },
            "export": {
                "enabled": true,
                "fileName": "Top_Customers"
            },
            "titles": [
            {
                "text": "Top Customers",
                "size": 15
            }
            ]
        },
        height: 400
    });
    var topSalesRep = new Wtf.ChartPanel({
        store: totalSaleRepStore,
        style:"background-color:white;",
        chartConfig: {
            "type": "pie",
            "titleField": "name",
            "valueField": "value",
            labelsEnabled: false,
            "innerRadius": "40%",
            theme: 'light',
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
                "position": "bottom",
                "marginRight": 20,
                "autoMargins": false
            },
            "export": {
                "enabled": true,
                "fileName": "Top_Sales_Representative"
            },
            "titles": [
            {
                "text": "Top Sales Representative",
                "size": 15
            }
            ]
        },
        height: 400
    });


    var salesRepPanel = new Wtf.ChartPanel({
        store: totalSalesstore,
        border: false,
        chartConfig: {
            "theme": "light",
            "type": "serial",
            "startDuration": 2,
            "graphs": [{
                "balloonText": "[[category]]: <b>[[value]]</b>",
                //        "fillColorsField": "color",
                "fillAlphas": 1,
                "lineAlpha": 0.1,
                "type": "column",
                "valueField": "value"
            }],
            "chartCursor": {
                "categoryBalloonEnabled": false,
                "cursorAlpha": 0,
                "zoomable": false
            },
            "categoryField": "name",
            "categoryAxis": {
                "gridPosition": "start",
                "labelRotation": 90
            },
            "export": {
                "enabled": true,
                fileName: "Top_products"
            },
            "titles": [
            {
                "text": "Top Products",
                "size": 15
            }
            ]
        },
        height: 420
    });

    var recordsEntry = new Wtf.Panel({
        height: 145
    });

    var summary = new Wtf.Panel({
        title: "Key Performance Indicators",
        height: 479,
        style:"background-color:white",
        //    border : false,
        html:
        //Payables
        "<div>"
    + "<div style='background-color: #757575;color: white;font-weight: bold;padding: 3px;font: bold 11px tahoma,arial,helvetica;'>Payables</div>"
    + "<div>"
    + "<table style='padding-left: 10px;width: 100%;margin-bottom: 5px;margin-top: 3px;'>"
    + "<tr>"
    + "<td style='font: 11px tahoma,arial,helvetica;width: 60%;'>Today</td>"
    + "<td style='font: 11px tahoma,arial,helvetica;padding-right: 5px;text-align: right;'>$46,953</td>"
    + "</tr>"
    + "<tr>"
    + "<td style='font: 11px tahoma,arial,helvetica;width: 60%;'>Same day last month</td>"
    + "<td style='font: 11px tahoma,arial,helvetica;padding-right: 5px;text-align: right;'>$21,017</td>"
    + "</tr>"
    + "<tr>"
    + "<td style='font: 11px tahoma,arial,helvetica;width: 60%;'>Change</td>"
    + "<td style='color: red;padding-right: 5px;text-align: right;'><img src='../../images/graphicalDashboard/downword.png' style='width:10px;height:8px;'/> 123%</td>"
    + "</tr>"
    + "</table>"
    + "</div>"
    + "</div>"
    //Profit
    + "<div>"
    + "<div style='background-color: #757575;color: white;font-weight: bold;padding: 3px;font: bold 11px tahoma,arial,helvetica;'>Profit</div>"
    + "<div>"
    + "<table style='padding-left: 10px;width: 100%;margin-bottom: 5px;margin-top: 3px;'>"
    + "<tr>"
    + "<td style='font: 11px tahoma,arial,helvetica;width: 60%;'>Last Period</td>"
    + "<td style='font: 11px tahoma,arial,helvetica;padding-right: 5px;text-align: right;'>$182,418</td>"
    + "</tr>"
    + "<tr>"
    + "<td style='font: 11px tahoma,arial,helvetica;width: 60%;'>1 FQtr Ago</td>"
    + "<td style='font: 11px tahoma,arial,helvetica;padding-right: 5px;text-align: right;'>$104,567</td>"
    + "</tr>"
    + "<tr>"
    + "<td style='font: 11px tahoma,arial,helvetica;width: 60%;'>Change</td>"
    + "<td style='color: green;padding-right: 5px;text-align: right;'><img src='../../images/graphicalDashboard/upword.png' style='width:10px;height:8px;'/> 74.5%</td>"
    + "</tr>"
    + "</table>"
    + "</div>"
    + "</div>"
    //Receivables
    + "<div>"
    + "<div style='background-color: #757575;color: white;font-weight: bold;padding: 3px;font: bold 11px tahoma,arial,helvetica;'>Receivables</div>"
    + "<div>"
    + "<table style='padding-left: 10px;width: 100%;margin-bottom: 5px;margin-top: 3px;'>"
    + "<tr>"
    + "<td style='font: 11px tahoma,arial,helvetica;width: 60%;'>Today</td>"
    + "<td style='font: 11px tahoma,arial,helvetica;padding-right: 5px;text-align: right;'>$141,891</td>"
    + "</tr>"
    + "<tr>"
    + "<td style='font: 11px tahoma,arial,helvetica;width: 60%;'>Same day last month</td>"
    + "<td style='font: 11px tahoma,arial,helvetica;padding-right: 5px;text-align: right;'>$318,153</td>"
    + "</tr>"
    + "<tr>"
    + "<td style='font: 11px tahoma,arial,helvetica;width: 60%;'>Change</td>"
    + "<td style='color: green;padding-right: 5px;text-align: right;'><img src='../../images/graphicalDashboard/upword.png' style='width:10px;height:8px;'/> 55.4%</td>"
    + "</tr>"
    + "</table>"
    + "</div>"
    + "</div>"
    //Sales
    + "<div>"
    + "<div style='background-color: #757575;color: white;font-weight: bold;padding: 3px;font: bold 11px tahoma,arial,helvetica;'>Sales</div>"
    + "<div>"
    + "<table style='padding-left: 10px;width: 100%;margin-bottom: 5px;margin-top: 3px;'>"
    + "<tr>"
    + "<td style='font: 11px tahoma,arial,helvetica;width: 60%;'>Last Month</td>"
    + "<td style='font: 11px tahoma,arial,helvetica;padding-right: 5px;text-align: right;'>$227,897</td>"
    + "</tr>"
    + "<tr>"
    + "<td style='font: 11px tahoma,arial,helvetica;width: 60%;'>1 FYear Ago</td>"
    + "<td style='font: 11px tahoma,arial,helvetica;padding-right: 5px;text-align: right;'>$183,096</td>"
    + "</tr>"
    + "<tr>"
    + "<td style='font: 11px tahoma,arial,helvetica;width: 60%;'>Change</td>"
    + "<td style='color: green;padding-right: 5px;text-align: right;'><img src='../../images/graphicalDashboard/upword.png' style='width:10px;height:8px;'/> 24.5%</td>"
    + "</tr>"
    + "</table>"
    + "</div>"
    + "</div>"
    //Bank Balance
    + "<div>"
    + "<div style='background-color: #757575;color: white;font-weight: bold;padding: 3px;font: bold 11px tahoma,arial,helvetica;'>Bank Balance</div>"
    + "<div>"
    + "<table style='padding-left: 10px;width: 100%;margin-bottom: 5px;margin-top: 3px;'>"
    + "<tr>"
    + "<td style='font: 11px tahoma,arial,helvetica;width: 60%;'>This Period</td>"
    + "<td style='font: 11px tahoma,arial,helvetica;padding-right: 5px;text-align: right;'>$19,825,761</td>"
    + "</tr>"
    + "<tr>"
    + "<td style='font: 11px tahoma,arial,helvetica;width: 60%;'>Last Period</td>"
    + "<td style='font: 11px tahoma,arial,helvetica;padding-right: 5px;text-align: right;'>$19,798,401</td>"
    + "</tr>"
    + "<tr>"
    + "<td style='font: 11px tahoma,arial,helvetica;width: 60%;'>Change</td>"
    + "<td style='color: green;padding-right: 5px;text-align: right;'><img src='../../images/graphicalDashboard/upword.png' style='width:10px;height:8px;'/> 0.1%</td>"
    + "</tr>"
    + "</table>"
    + "</div>"
    + "</div>"
    //Expense
    + "<div>"
    + "<div style='background-color: #757575;color: white;font-weight: bold;padding: 3px;font: bold 11px tahoma,arial,helvetica;'>Expense</div>"
    + "<div>"
    + "<table style='padding-left: 10px;width: 100%;margin-bottom: 5px;margin-top: 3px;'>"
    + "<tr>"
    + "<td style='font: 11px tahoma,arial,helvetica;width: 60%;'>Last Quarter</td>"
    + "<td style='font: 11px tahoma,arial,helvetica;padding-right: 5px;text-align: right;'>$14,687</td>"
    + "</tr>"
    + "<tr>"
    + "<td style='font: 11px tahoma,arial,helvetica;width: 60%;'>Period Before Last</td>"
    + "<td style='font: 11px tahoma,arial,helvetica;padding-right: 5px;text-align: right;'>$19,523</td>"
    + "</tr>"
    + "<tr>"
    + "<td style='font: 11px tahoma,arial,helvetica;width: 60%;'>Change</td>"
    + "<td style='color: green;padding-right: 5px;text-align: right;'><img src='../../images/graphicalDashboard/upword.png' style='width:10px;height:8px;'/> 175%</td>"
    + "</tr>"
    + "</table>"
    + "</div>"
    + "</div>"

    });

    var salesGaugePanel = new Wtf.RawChart({
        style: " margin-bottom: 0;background-color:white;",
        chartConfig: {
            "type": "gauge",
            theme: '',
            "arrows": [
            {
                "alpha": 1,
                "innerRadius": "35%",
                "nailRadius": 0,
                "radius": "100%",
                "value": 70
            }
            ],
            "titles": [
            {
                "text": "Sales",
                color: '#005F7F',
                "size": 20
            }
            ],
            "axes": [
            {
                "bottomText": "70%",
                bottomTextFontSize: 22,
                bottomTextColor: 'green',
                "endValue": 150,
                "valueInterval": 200,
                startValue: -50,
                "axisColor": "#31d6ea",
                "axisThickness": 0,
                "tickColor": "#67b7dc",
                "bandOutlineAlpha": 0,
                "startAngle": -90,
                "endAngle": 90,
                unit: '%',
                "bands": [{
                    "color": " #FF0000",
                    "endValue": 0,
                    "innerRadius": "85%",
                    "radius": "100%",
                    //                        "gradientRatio": [0.2, 0, -0.2],
                    "startValue": -50
                }, {
                    "color": " #ffbf00",
                    "endValue": 50,
                    "innerRadius": "85%",
                    "radius": "100%",
                    //                        "gradientRatio": [0.2, 0, -0.2],
                    "startValue": 0
                }, {
                    "color": " #00FF00",
                    "endValue": 150,
                    "innerRadius": "85%",
                    "radius": "100%",
                    //                        "gradientRatio": [0.2, 0, -0.2],
                    "startValue": 50
                }]

            }
            ]
        },
        height: 170

    });
    var incomeGaugePanel = new Wtf.RawChart({
        style: " margin-bottom: 0;background-color:white;",
        chartConfig: {
            "type": "gauge",
            theme: '',
            "arrows": [
            {
                "alpha": 1,
                "innerRadius": "35%",
                "nailRadius": 0,
                "radius": "100%",
                "value": 50.1
            }
            ],
            "titles": [
            {
                "text": "Income",
                color: '#005F7F',
                "size": 20
            }
            ],
            "axes": [
            {
                "bottomText": "50.1%",
                bottomTextFontSize: 22,
                bottomTextColor: 'green',
                "endValue": 100,
                "valueInterval": 100,
                startValue: 0,
                "axisColor": "#31d6ea",
                "axisThickness": 0,
                "tickColor": "#67b7dc",
                "bandOutlineAlpha": 0,
                "startAngle": -90,
                "endAngle": 90,
                unit: '%',
                "bands": [{
                    "color": " #FF0000",
                    "endValue": 20,
                    "innerRadius": "85%",
                    "radius": "100%",
                    //                        "gradientRatio": [0.2, 0, -0.2],
                    "startValue": 0
                }, {
                    "color": " #ffbf00",
                    "endValue": 50,
                    "innerRadius": "85%",
                    "radius": "100%",
                    //                        "gradientRatio": [0.2, 0, -0.2],
                    "startValue": 20
                }, {
                    "color": " #00FF00",
                    "endValue": 100,
                    "innerRadius": "85%",
                    "radius": "100%",
                    //                        "gradientRatio": [0.2, 0, -0.2],
                    "startValue": 50
                }]

            }
            ]
        },
        height: 170

    });
    var expenseGaugePanel = new Wtf.RawChart({
        style: " margin-bottom: 0;background-color:white;",
        chartConfig: {
            "type": "gauge",
            theme: '',
            "arrows": [
            {
                "alpha": 1,
                "innerRadius": "35%",
                "nailRadius": 0,
                "radius": "100%",
                "value": 7.4
            }
            ],
            "titles": [
            {
                "text": "Expenses",
                color: '#005F7F',
                "size": 20
            }
            ],
            "axes": [
            {
                "bottomText": "7.4%",
                bottomTextFontSize: 22,
                bottomTextColor: 'green',
                "endValue": 100,
                "valueInterval": 100,
                startValue: 0,
                "axisColor": "#31d6ea",
                "axisThickness": 0,
                "tickColor": "#67b7dc",
                "bandOutlineAlpha": 0,
                "startAngle": -90,
                "endAngle": 90,
                unit: '%',
                "bands": [{
                    "color": " #00FF00",
                    "endValue": 20,
                    "innerRadius": "85%",
                    "radius": "100%",
                    //                        "gradientRatio": [0.2, 0, -0.2],
                    "startValue": 0
                }, {
                    "color": "#ffbf00",
                    "endValue": 50,
                    "innerRadius": "85%",
                    "radius": "100%",
                    //                        "gradientRatio": [0.2, 0, -0.2],
                    "startValue": 20
                }, {
                    "color": " #FF0000",
                    "endValue": 100,
                    "innerRadius": "85%",
                    "radius": "100%",
                    //                        "gradientRatio": [0.2, 0, -0.2],
                    "startValue": 50
                }]

            }
            ]
        },
        height: 170

    });
    var percentagePanel = new Wtf.Panel({
        layout: 'column',
        border: false,
        items: [{
            columnWidth: 0.333,
            //            cls: 'portletcls',
            style: "margin-bottom:0;margin-top:0;padding-right:10px;",
            border: false,
            items: incomeGaugePanel
        //            items: [{
        //                    height: 100,
        //                    html: "<div style='font-family: icon;font-size: 19px;margin-top: 10px;text-align: center;color:#005F7F;font-weight: bold;'>Sale</div>"
        //                            + "<div style='color: green;font-size: 29px;font-weight: bold;margin-top: 9px;text-align: center;'>61.3%</div>"
        //                }]
        }, {
            columnWidth: 0.333,
            border: false,
            //            cls: 'portletcls',
            style: "margin-bottom:0;margin-top:0;padding-right:10px;",
            items: salesGaugePanel
        //            items: [{
        //                    height: 100,
        //                    html: "<div style='font-family: icon;font-size: 19px;margin-top: 10px;text-align: center;color:#005F7F;font-weight: bold;'>Income</div>"
        //                            + "<div style='color: green;font-size: 29px;font-weight: bold;margin-top: 9px;text-align: center;'>50.1%</div>"
        //                }]
        }, {
            columnWidth: 0.333,
            border: false,
            //            cls: 'portletcls',
            style: "margin-bottom:0;margin-top:0;",
            items: expenseGaugePanel
        //            items: [{
        //                    height: 100,
        //                    html: "<div style='font-family: icon;font-size: 19px;margin-top: 10px;text-align: center;color:#005F7F;font-weight: bold;'>Expenses</div>"
        //                            + "<div style='color: green;font-size: 29px;font-weight: bold;margin-top: 9px;text-align: center;'>7.4%</div>"
        //                }]
        }]
    });


    var agedPanel = new Wtf.ChartPanel({
        store: agedReceivableStore,
        style:"background-color:white;",
        height: 236,
        chartConfig: {
            "theme": "light",
            "type": "serial",
            "startDuration": 0,
            "valueAxes": [{
                gridThickness: 0,
                unit: "$",
                unitPosition: "left"

            }],        
     
            "graphs": [{
                "balloonText": "Amount Received in [[category]]: <b>[[value]]</b>",
                "fillAlphas": 0.9,
                "lineAlpha": 0.2,
                "type": "column",
                "valueField": "amountreceived",
                "fillColorsField": "colorreceived"
            //                plotAreaFillColors:["#CC586B", "#F8BAC7"]
                
            }, {
                "balloonText": "Amount Due in [[category]]: <b>[[value]]</b>",
                "fillAlphas": 0.9,
                "lineAlpha": 0.2,
                "type": "column",
                "fillColorsField": "colordue",
                "valueField": "amountdue"
            }],
            //        "plotAreaFillAlphas": 0.1,
            "categoryField": "monthname",
            "categoryAxis": {
                "gridPosition": "start",
                gridThickness: 0
            },
            "export": {
                "enabled": true,
                "fileName": "Amount_received"
            },
            "titles": [
            {
                "text": "Amount Receivable",
                "size": 15
            }
            ]

        }
    });

    var agedPaidPanel = new Wtf.ChartPanel({
        store: agedPayableStore,
        style:"background-color:white;",
        height: 235,
        chartConfig: {
            "theme": "light",
            "type": "serial",
            "startDuration": 0,
            "valueAxes": [{
                gridThickness: 0,
                unit: "$",
                unitPosition: "left"

            }],        
            "graphs": [{
                "balloonText": "Amount Paid in [[category]]: <b>[[value]]</b>",
                "fillAlphas": 0.9,
                "lineAlpha": 0.2,
                "type": "column",
                "valueField": "amountreceived",
                "fillColorsField": "colorreceived"
            }, {
                "balloonText": "Amount Due in [[category]]: <b>[[value]]</b>",
                "fillAlphas": 0.9,
                "lineAlpha": 0.2,
                "type": "column",
                "valueField": "amountdue",
                "fillColorsField": "colordue"
            }],
            "plotAreaFillAlphas": 0.1,
            "categoryField": "monthname",
            "categoryAxis": {
                "gridPosition": "start",
                gridThickness: 0            
            },
            "export": {
                "enabled": true,
                "fileName": "Amount_paid"
            },
            "titles": [
            {
                "text": "Amount Payable",
                "size": 15
            }
            ]
        }
    });

    var column3Arr = [];
    column2Arr.push(percentagePanel);
    column2Arr.push(salesTrendPanel);
    column1Arr.push(summary);
    column3Arr.push(agedPanel);
    column3Arr.push(agedPaidPanel);

    var columnPanel = new Wtf.Panel({
        layout: 'column',
        style:"margin-bottom:0",
        id: 'portal_container',
        border: false,
        items: [{
            columnWidth: 0.2,
            cls: 'portletcls',
            style: "margin-bottom:0",
            id: 'portal_container_box1',
            border: false,
            items: column1Arr
        }, {
            columnWidth: 0.49,
            border: false,
            cls: 'portletcls',
            style: "margin-bottom:0",
            id: 'portal_container_box2',
            items: column2Arr
        }, {
            columnWidth: 0.3,
            border: false,
            cls: 'portletcls',
            style: "margin-bottom:0",
            //            id: 'portal_container_box2',
            items: column3Arr
        }]
    });
    var topItemsPanel = new Wtf.Panel({
        layout: 'column',
        border: false,
    
        items: [{
            columnWidth: 0.33,
            cls: 'left-portletcls',
            style: "margin-bottom:0",
            //            style: "margin-bottom:0;margin-top:0;padding-right:10px;",
            border: false,
            items: topProducts
        }, {
            columnWidth: 0.33,
            border: false,
            cls: 'left-portletcls',
            style: "margin-bottom:0",
            //            style: "margin-bottom:0;margin-top:0;padding-right:10px;",
            items: topCustomers
        }, {
            columnWidth: 0.33,
            border: false,
            cls: 'left-portletcls',
            style: "margin-bottom:0",
            //            style: "margin-bottom:0;margin-top:0;padding-right:10px;",
            items: topSalesRep
        }]
    });
    panelArr.push(columnPanel);
    panelArr.push(topItemsPanel);


    var paneltop = new Wtf.Panel({
        border: false,
        id :"dashboard",
        autoScroll: true,
        frame: false,
        style:style,
        items: panelArr
    });

    paneltop.on("resize", function () {
        this.doLayout();
    });
    
    Wtf.getCmp("tabdashboard").on("activate",function(){
        paneltop.doLayout(); 
    });

    Wtf.getCmp("tabdashboard").add(paneltop);
    Wtf.getCmp("tabdashboard").doLayout();
}