/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

Wtf.account.ForecastingWidgetReport = function (config) {
    Wtf.apply(this, config);
    Wtf.account.ForecastingWidgetReport.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.ForecastingWidgetReport, Wtf.Panel, {
    onRender: function (config) {
        // Common Top Bar
        this.startDateFR = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), //'From',
            name: 'stdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.getDates(true)
        });
        this.endDateFR = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), //'To',
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'enddate',
            value: this.getDates(false)
        });


        this.globalDimensionFRRec = new Wtf.data.Record.create([
            {name: "fieldid"},
            {name: "fieldlabel"},
            {name: "column_number"},
            {name: "refcolumn_number"}
        ]);

        this.globalDimensionFRReader = new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.globalDimensionFRRec);

        this.globalDimensionFRStore = new Wtf.data.Store({
            url: "ACCAccountCMN/getFieldParams.do",
            reader: this.globalDimensionFRReader,
            baseParams: {
                ignoreDefaultFields: true,
                isActivated: 1,
                isAdvanceSearch: false,
                isAvoidRedundent: true,
                iscustomdimension: true,
                iscustomcolumn: false
            }
        });
        this.globalDimensionFRStore.load();
        this.globalDimensionFR = new Wtf.form.ComboBox({
            store: this.globalDimensionFRStore,
            name: 'typeid',
            displayField: 'fieldlabel',
            valueField: 'fieldid',
            mode: 'local',
            width: 150,
            listWidth: 150,
            emptyText: WtfGlobal.getLocaleText("acc.report.budgetvscost.selectGblDim"),
            triggerAction: 'all',
            typeAhead: true,
            selectOnFocus: true
        });


        this.globalDimensionMasterItemRec = new Wtf.data.Record.create([
            {name: "id"},
            {name: "name"}
        ]);
        this.globalDimensionMasterItemReader = new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.globalDimensionMasterItemRec);

        this.globalDimensionMasterItemStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItemsForCustomFoHire.do",
            reader: this.globalDimensionMasterItemReader,
            baseParams: {
                mode: 112
            }
        });
        this.globalDimensionMasterItemStore.load();
        this.globalDimensionMasterItemFR = new Wtf.form.ComboBox({
            store: this.globalDimensionMasterItemStore,
            name: 'typeid',
            displayField: 'name',
            valueField: 'id',
            mode: 'local',
            width: 200,
            listWidth: 200,
            emptyText: WtfGlobal.getLocaleText("acc.report.budgetvscost.selectGblDimCombodata"),
            triggerAction: 'all',
            typeAhead: true,
            selectOnFocus: true
        });

        this.globalDimensionFR.on('select', function (a, b) {
            this.globalDimensionMasterItemFR.reset();
            var fieldLabelGlobal = "";
            var idGlobal = "";
            var globalDimensionRec = WtfGlobal.searchRecord(this.globalDimensionFRStore, this.globalDimensionFR.getValue(), 'fieldid');
            if (globalDimensionRec != undefined && globalDimensionRec.data != undefined) {
                fieldLabelGlobal = globalDimensionRec.data.fieldlabel;
                idGlobal = globalDimensionRec.data.fieldid;
            }
            this.globalDimensionMasterItemStore.load({
                params: {
                    fieldlabel: fieldLabelGlobal,
                    groupid: idGlobal
                }
            });
        }, this);

        this.fetchFR = new Wtf.Toolbar.Button({
            scope: this,
            text: WtfGlobal.getLocaleText("acc.common.fetch"), //"Fetch",
            iconCls: "accountingbase fetch",
            handler: function () {
                if (this.startDateFR.getValue() > this.endDateFR.getValue()) {
                    WtfComMsgBox(1, 2);
                    return;
                }
                this.setGlobalBaseParam();
                this.loadAllStore();
            }
        });
        // Common Bottom Bar
        this.exportCSV = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.common.exportToCSV"),//'Export to CSV File',
            scope: this,
            disabled: true,
            iconCls: 'pwnd exportcsv',
            handler: function () {
                var header = [];
                var title = [];
                var width = [];
                var align = [];
                var URL = "ACCReports/exportForecastingReport.do?";
                URL += "startDate=" + this.startDateFR.getValue().format('Y-m-d') + "&";
                URL += "endDate=" + this.endDateFR.getValue().format('Y-m-d') + "&";
                URL += "startdate=" + this.startDateFR.getValue().format('Y-m-d') + "&";
                URL += "enddate=" + this.endDateFR.getValue().format('Y-m-d') + "&";
                URL += "globaldimension=" + this.globalDimensionFR.getValue() + "&";
                URL += "globaldimensionCombodate=" + this.globalDimensionMasterItemFR.getValue() + "&";
                URL += "globalDimensioncombodataname=" + this.globalDimensionMasterItemFR.getRawValue() + "&";
                URL += "globaldimensionName=" + this.globalDimensionFR.getRawValue() + "&";
                URL += "filetype=csv&";
                URL += "filename=Forecasting Report_v1&";
                for (var cntVal = 0; cntVal < this.JobForecastStatementgridcmodel.getColumnCount(); cntVal++) {
                    var rec = this.JobForecastStatementgridcmodel.config[cntVal];
                    if (rec) {
                        header.push(rec.dataIndex);
                        title.push(encodeURIComponent(rec.header));
                        align.push(!Wtf.isEmpty(rec.align) ? "currency" : '');
                        width.push(40);
                    }
                }
                URL += "header=" + header + "&title=" + encodeURIComponent(title) + "&width=" + width + "&align=" + align;
                Wtf.get('downloadframe').dom.src = URL;
            }
        });
        this.exportXls = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.common.exportToxls"),//'Export to XLS file',
            scope: this,
            disabled: true,
            iconCls: 'pwnd exportcsv',
            handler: function () {
                var header = [];
                var title = [];
                var width = [];
                var align = [];
                var URL = "ACCReports/exportForecastingReport.do?";
                URL += "startDate=" + this.startDateFR.getValue().format('Y-m-d') + "&";
                URL += "endDate=" + this.endDateFR.getValue().format('Y-m-d') + "&";
                URL += "startdate=" + this.startDateFR.getValue().format('Y-m-d') + "&";
                URL += "enddate=" + this.endDateFR.getValue().format('Y-m-d') + "&";
                URL += "globaldimension=" + this.globalDimensionFR.getValue() + "&";
                URL += "globaldimensionCombodate=" + this.globalDimensionMasterItemFR.getValue() + "&";
                URL += "globalDimensioncombodataname=" + this.globalDimensionMasterItemFR.getRawValue() + "&";
                URL += "globaldimensionName=" + this.globalDimensionFR.getRawValue() + "&";
                URL += "filetype=xls&";
                URL += "filename=Forecasting Report_v1&";
                for (var cntVal = 0; cntVal < this.JobForecastStatementgridcmodel.getColumnCount(); cntVal++) {
                    var rec = this.JobForecastStatementgridcmodel.config[cntVal];
                    if (rec) {
                        header.push(rec.dataIndex);
                        title.push(encodeURIComponent(rec.header));
                        align.push(!Wtf.isEmpty(rec.align) ? "currency" : '');
                        width.push(cntVal == 1 ? 150 : 40);
                    }
                }
                URL += "header=" + header + "&title=" + encodeURIComponent(title) + "&width=" + width + "&align=" + align;
                Wtf.get('downloadframe').dom.src = URL;
            }
        });
        this.expButtonFR = {
            text: WtfGlobal.getLocaleText("acc.common.export"),
            iconCls: 'pwnd export',
            scope: this,
            menu: [this.exportCSV, this.exportXls]
        };

        this.Forcastpan = new Wtf.Panel({
            border: false,
            layout: 'border',
            frame: false,
            bbar: [this.expButtonFR],
            tbar: ["From :",this.startDateFR, '-',"To :", this.endDateFR, '-','Global Dimension :', this.globalDimensionFR, '-','Master Item :', this.globalDimensionMasterItemFR, '-', this.fetchFR],
            items: [{
                    id: 'forecast_widget_container',
                    region: 'center',
                    xtype: 'portal',
                    bodyStyle: "background:white;",
                    border: false,
                    items: [{
                            columnWidth: .5,
                            style: 'padding:5px 1px 5px 5px',
                            cls: 'portletcls',
                            id: 'forecast_widget_container_box1',
                            border: false
                        }, {
                            columnWidth: .5,
                            style: 'padding:5px 5px 5px 5px',
                            cls: 'portletcls',
                            id: 'forecast_widget_container_box2',
                            border: false
                        }]
                }]

        });
        this.add(this.Forcastpan);
        Wtf.account.ForecastingWidgetReport.superclass.onRender.call(this, config);
        this.setGlobalBaseParam();
        this.createreportList();
        Wtf.getCmp('forecast_widget_container').doLayout();
    },
    setGlobalBaseParam: function () {
        this.GlobalBaseParam = {
            startDate: this.startDateFR.getValue().format('Y-m-d'),
            endDate: this.endDateFR.getValue().format('Y-m-d'),
            globaldimension: this.globalDimensionFR.getValue(),
            globaldimensionName: this.globalDimensionFR.getRawValue(),
            globaldimensionCombodate: this.globalDimensionMasterItemFR.getValue(),
            globalDimensioncombodataname: this.globalDimensionMasterItemFR.getRawValue()
        }
    },
    createreportList: function () {
        var panel;
        var reportcount = 1;

        // Report 1
        panel = this.JobForecastStatementSummary();
        var columnCount = ((reportcount) % 2) ? 1 : 2;
        this.addWidget(panel, columnCount);
        reportcount++;

        // Report 2
        panel = this.ChangeOrderStatus();
        var columnCount = ((reportcount) % 2) ? 1 : 2;
        this.addWidget(panel, columnCount);
        reportcount++;

        // Report 3
        panel = this.Billing();
        var columnCount = ((reportcount) % 2) ? 1 : 2;
        this.addWidget(panel, columnCount);
        reportcount++;

        // Report 4
        panel = this.ReceivablesInventory();
        var columnCount = ((reportcount) % 2) ? 1 : 2;
        this.addWidget(panel, columnCount);
        reportcount++;

        // Report 5
        panel = this.FeeSummary();
        var columnCount = ((reportcount) % 2) ? 1 : 2;
        this.addWidget(panel, columnCount);
        reportcount++;

        // Report 6
        panel = this.ProfitLossBacklog();
        var columnCount = ((reportcount) % 2) ? 1 : 2;
        this.addWidget(panel, columnCount);
        reportcount++;

        this.loadAllStore();
        Wtf.getCmp('forecast_widget_container').doLayout();
    },
    loadAllStore: function () {
        this.loadingMaskFR = new Wtf.LoadMask(document.body, {
            msg: "Loading Widgets..."
        });
        this.loadingMaskFR.show();
        Wtf.Ajax.requestEx({// request to fetch Report data and amount as per each report
            url: "ACCReports/getForecastingReport.do",
            params: this.GlobalBaseParam,
        }, this, function (resp) {
            if (resp.success) {
                true
                var enableButtonFlag = 0;
                if (resp.jobforcastsummaryreport) {
                    this.JobForecastStatementStore.loadData(resp);
                    enableButtonFlag++;
                } else {
                    this.JobForecastStatementStore.removeAll();
                }
                if (resp.changeorderstatus) {
                    this.ChangeOrderStatusStore.loadData(resp);
                    enableButtonFlag++;
                } else {
                    this.ChangeOrderStatusStore.removeAll();
                }
                if (resp.billing) {
                    this.BillingStore.loadData(resp);
                    enableButtonFlag++;
                } else {
                    this.BillingStore.removeAll();
                }
                if (resp.ReceivablesInventory) {
                    this.ReceivablesInventoryStore.loadData(resp);
                    enableButtonFlag++;
                } else {
                    this.ReceivablesInventoryStore.removeAll();
                }
                if (resp.FeeSummary) {
                    this.FeeSummaryStore.loadData(resp);
                    enableButtonFlag++;
                } else {
                    this.FeeSummaryStore.removeAll();
                }
                if (resp.ProfitLossBacklog) {
                    this.ProfitLossBacklogStore.loadData(resp);
                    enableButtonFlag++;
                } else {
                    this.ProfitLossBacklogStore.removeAll();
                }
                if (enableButtonFlag > 0) {
                    this.exportCSV.setDisabled(false);
                    this.exportXls.setDisabled(false);
                } else {
                    this.exportCSV.setDisabled(true);
                    this.exportXls.setDisabled(true);
                }
            }
            this.loadingMaskFR.hide();
        }, function (resp) {
            this.loadingMaskFR.hide();
        });
    },
    createForecastingWidgets: function (grid, reportTitle, reportid, height) {

//        Create a panel
        var Forcast_panel = new Wtf.Panel({
            id: "Forcast_panelwidgetPanel" + reportid,
            title: reportTitle,
            layout: 'fit',
            draggable: {
                onDrag: function (e) {
                    var pel = this.proxy.getEl();
                    this.x = pel.getLeft(true);
                    this.y = pel.getTop(true);

                    var s = this.panel.getEl().shadow;
                    if (s) {
                        s.realign(this.x, this.y, pel.getWidth(), pel.getHeight());
                    }
                },
                endDrag: function (e) {
                    this.panel.setPosition(this.x, this.y);
                    Wtf.getCmp('forecast_widget_container').doLayout();
                }
            },
            height: height,
            items: [grid]
        });

        return Forcast_panel;
    },
    addWidget: function (panel, colCount) {
        var _ID = 'forecast_widget_container_box';
        var pl = Wtf.getCmp(_ID + colCount);
        if (pl != null) {
            pl.add(panel);
            pl.doLayout();
            panel.doLayout();
        }
    },
    JobForecastStatementSummary: function () {
        //Record Box 1
        this.JobForecastStatementRec = new Wtf.data.Record.create([
            {name: 'accounts'},
            {name: 'spentthisperiod'},
            {name: 'spenttodate'},
            {name: 'spentcommitted'},
            {name: 'cost'},
            {name: 'costforcast'},
            {name: 'currentRevenuebudget'},
            {name: 'anticipatedchangeorder'},
            {name: 'revenueforecast'},
            {name: 'projectedgainloss'},
            {name: 'onemonthvariance'},
            {name: 'costcomplete'}

        ]);
        //Store Box 1
        this.JobForecastStatementStore = new Wtf.data.GroupingStore({
            reader: new Wtf.data.KwlJsonReader({
                root: "jobforcastsummaryreport.data",
                totalProperty: 'count'
            }, this.JobForecastStatementRec),
            url: "ACCReports/getForecastingReport.do"
        });
        this.JobForecastStatementStore.on('beforeload', function (s, o) {
            this.JobForecastStatementStore.baseParams = this.GlobalBaseParam;
        }, this);

        //Column Box 1
        var recordArray = [];
        recordArray.push(
        {
            header: WtfGlobal.getLocaleText("acc.report.forecasting.acocunts"),//"Accounts",
            dataIndex: 'accounts',
            width: 180,
            pdfwidth: 180,
            fixed: true,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.accounts == "Total") {
                    return '<b>' + val + '</b>';
                } else {
                    return val;
                }
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.forecasting.spentthisperiod"),//"Spent This Period",
            dataIndex: 'spentthisperiod',
            align: 'right',
            width: 180,
            pdfwidth: 180,
            fixed: true,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.accounts == "Total") {
                    return '<b>' + WtfGlobal.currencyRenderer(val) + '</b>';
                } else {
                    return WtfGlobal.currencyRenderer(val);
                }
            },
            summaryType: 'sum',
            summaryRenderer: function (value, m, rec) {
                var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                return retVal;
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.forecasting.spenttodate"),//"Spent To Date",
            dataIndex: 'spenttodate',
            align: 'right',
            width: 180,
            pdfwidth: 180,
            fixed: true,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.accounts == "Total") {
                    return '<b>' + WtfGlobal.currencyRenderer(val) + '</b>';
                } else {
                    return WtfGlobal.currencyRenderer(val);
                }
            },
            summaryType: 'sum',
            summaryRenderer: function (value, m, rec) {
                var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                return retVal;
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.forecasting.spentcommitted"),//"Spent/Committed",
            dataIndex: 'spentcommitted',
            align: 'right',
            pdfwidth: 180,
            width: 180,
            fixed: true,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.accounts == "Total") {
                    return '<b>' + WtfGlobal.currencyRenderer(val) + '</b>';
                } else {
                    return WtfGlobal.currencyRenderer(val);
                }
            },
            summaryType: 'sum',
            summaryRenderer: function (value, m, rec) {
                var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                return retVal;
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.forecasting.costtocomplt"),//"Cost to Complt inc.",
            dataIndex: 'cost',
            width: 180,
            align: 'right',
            pdfwidth: 180,
            fixed: true,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.accounts == "Total") {
                    return '<b>' + WtfGlobal.currencyRenderer(val) + '</b>';
                } else {
                    return WtfGlobal.currencyRenderer(val);
                }
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.forecasting.costforecast"),//"Cost Forecast",
            dataIndex: 'costforcast',
            width: 180,
            align: 'right',
            pdfwidth: 180,
            fixed: true,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.accounts == "Total") {
                    return '<b>' + WtfGlobal.currencyRenderer(val) + '</b>';
                } else {
                    return WtfGlobal.currencyRenderer(val);
                }
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.forecasting.currentrevenuebudget"),//"Current Revenue Budget",
            dataIndex: 'currentRevenuebudget',
            width: 180,
            align: 'right',
            pdfwidth: 180,
            fixed: true,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.accounts == "Total") {
                    return '<b>' + WtfGlobal.currencyRenderer(val) + '</b>';
                } else {
                    return WtfGlobal.currencyRenderer(val);
                }
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.forecasting.anticipatedchangeorder"),//"Anticipated Change Order",
            dataIndex: 'anticipatedchangeorder',
            width: 180,
            align: 'right',
            pdfwidth: 180,
            fixed: true,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.accounts == "Total") {
                    return '<b>' + WtfGlobal.currencyRenderer(val) + '</b>';
                } else {
                    return WtfGlobal.currencyRenderer(val);
                }
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.forecasting.revenueforecast"),//"Revenue Forecast",
            dataIndex: 'revenueforecast',
            width: 180,
            align: 'right',
            pdfwidth: 180,
            fixed: true,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.accounts == "Total") {
                    return '<b>' + WtfGlobal.currencyRenderer(val) + '</b>';
                } else {
                    return WtfGlobal.currencyRenderer(val);
                }
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.forecasting.projectgainloss"),//"Projected Gain/Loss",
            dataIndex: 'projectedgainloss',
            width: 180,
            align: 'right',
            pdfwidth: 180,
            fixed: true,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.accounts == "Total") {
                    return '<b>' + WtfGlobal.currencyRenderer(val) + '</b>';
                } else {
                    return WtfGlobal.currencyRenderer(val);
                }
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.forecasting.onemonthvariance"),//"One Month Variance",
            dataIndex: 'onemonthvariance',
            width: 180,
            align: 'right',
            pdfwidth: 180,
            fixed: true,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.accounts == "Total") {
                    return '<b>' + WtfGlobal.currencyRenderer(val) + '</b>';
                } else {
                    return WtfGlobal.currencyRenderer(val);
                }
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.forecasting.costcomplete"),//"% Cost Complete",
            dataIndex: 'costcomplete',
            width: 180,
            align: 'right',
            pdfwidth: 180,
            fixed: true,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.accounts == "Total") {
                    return '<b>' + WtfGlobal.currencyRenderer(val) + '</b>';
                } else {
                    return WtfGlobal.currencyRenderer(val);
                }
            }
        });
        var gridSummary = new Wtf.grid.GroupSummary({});
        this.JobForecastStatementgridcmodel = new Wtf.grid.ColumnModel(recordArray);
        //Grid Box 1
        this.JobForecastStatementGrid = new Wtf.grid.GridPanel({
            stripeRows: true,
            store: this.JobForecastStatementStore,
            cm: this.JobForecastStatementgridcmodel,
            border: false,
            id: "JobForecastStatementReport",
            loadMask: true,
            plugins: [gridSummary],
            viewConfig: {
                emptyText: '<div style="font-size:15px; text-align:center; color:#808080; font-weight:bold; margin-top:1%;">' + WtfGlobal.getLocaleText("acc.report.budgetvscost.selectRectoLoad") + '</div>'
            }
        });
        this.grid = this.JobForecastStatementGrid;
        var panel = this.createForecastingWidgets(this.JobForecastStatementGrid, "Job Forecast Statement Summary", "JobForecastStatementSummaryID", 340);
        return panel;
    },
    ChangeOrderStatus: function () {
        //Record Box 2
        this.ChangeOrderStatusRec = new Wtf.data.Record.create([
            {name: 'parametertitles'},
            {name: 'no'},
            {name: 'currentamount'}
        ]);
        //Store Box 2
        this.ChangeOrderStatusStore = new Wtf.data.GroupingStore({
            reader: new Wtf.data.KwlJsonReader({
                root: "changeorderstatus.data",
                totalProperty: 'count'
            }, this.ChangeOrderStatusRec),
            url: "ACCReports/getForecastingReport.do"
        });
        //Column Box 2
        var recordArray = [];
        recordArray.push(
        {
            header: " ",
            dataIndex: 'parametertitles',
            pdfwidth: 180,
            Cls: 'wrap-text',
            renderer: function (val, metadata) {
                metadata.attr = 'style="white-space: normal;"';
                return "<div  wtf:qtip=\"" + val + "\">" + val + "</div>"
            }
        }, {
            header: "No.",
            dataIndex: 'no',
            align: 'right',
            pdfwidth: 180
        }, {
            header: WtfGlobal.getLocaleText("acc.report.forecasting.currentamount"),//"Current Amount",
            dataIndex: 'currentamount',
            align: 'right',
            pdfwidth: 180,
            renderer: WtfGlobal.currencyRenderer
        });
        this.ChangeOrderStatuscmodel = new Wtf.grid.ColumnModel(recordArray);
        //Grid Box 2
        this.ChangeOrderStatusGrid = new Wtf.grid.GridPanel({
            stripeRows: true,
            store: this.ChangeOrderStatusStore,
            cm: this.ChangeOrderStatuscmodel,
            border: false,
            id: "ChangeOrderStatusReport",
            loadMask: true,
            viewConfig: {
                emptyText: '<div style="font-size:15px; text-align:center; color:#808080; font-weight:bold; margin-top:1%;">' + WtfGlobal.getLocaleText("acc.report.budgetvscost.selectRectoLoad") + '</div>',
                forceFit: true
            }
        });
        var panel = this.createForecastingWidgets(this.ChangeOrderStatusGrid, "Change Order Status - External PCIs", "ChangeOrderStatusID", 340);
        return panel;
    },
    Billing: function () {
        //Record Box 3
        this.BillingRec = new Wtf.data.Record.create([
            {name: 'parametertitles'},
            {name: 'current'}
        ]);
        //Store Box 3
        this.BillingStore = new Wtf.data.GroupingStore({
            reader: new Wtf.data.KwlJsonReader({
                root: "billing.data",
                totalProperty: 'count'
            }, this.BillingRec),
            url: "ACCReports/getForecastingReport.do"
        });
        //Column Box 3
        var recordArray = [];
        recordArray.push(
        {
            header: " ",
            dataIndex: 'parametertitles',
            pdfwidth: 180,
            Cls: 'wrap-text',
            renderer: function (val, metadata) {
                metadata.attr = 'style="white-space: normal;"';
                return "<div  wtf:qtip=\"" + val + "\">" + val + "</div>"
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.forecasting.current"),//"Current",
            dataIndex: 'current',
            align: 'right',
            pdfwidth: 180,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.parametertitles == "GM %") {
                    return '<div class="currency">' + WtfGlobal.conventInDecimal(val, "") + "%" + '</div>'
                } else {
                    return WtfGlobal.currencyRenderer(val)

                }
            }
        });
        this.billingcmodel = new Wtf.grid.ColumnModel(recordArray);
        //Grid Box 3
        this.BillingGrid = new Wtf.grid.GridPanel({
            stripeRows: true,
            store: this.BillingStore,
            cm: this.billingcmodel,
            border: false,
            id: "billingcmodelReport",
            loadMask: true,
            viewConfig: {
                emptyText: '<div style="font-size:15px; text-align:center; color:#808080; font-weight:bold; margin-top:1%;">' + WtfGlobal.getLocaleText("acc.report.budgetvscost.selectRectoLoad") + '</div>',
                forceFit: true
            }
        });
        var panel = this.createForecastingWidgets(this.BillingGrid, "Billings", "BillingID", 230);
        return panel;
    },
    ReceivablesInventory: function () {
        //Record Box 4
        this.ReceivablesInventoryRec = new Wtf.data.Record.create([
            {name: 'parametertitles'},
            {name: 'current'}
        ]);
        //Store Box 4
        this.ReceivablesInventoryStore = new Wtf.data.GroupingStore({
            reader: new Wtf.data.KwlJsonReader({
                root: "ReceivablesInventory.data",
                totalProperty: 'count'
            }, this.ReceivablesInventoryRec),
            url: "ACCReports/getForecastingReport.do"
        });
        //Column Box 4
        var recordArray = [];
        recordArray.push(
        {
            header: " ",
            dataIndex: 'parametertitles',
            pdfwidth: 180,
            Cls: 'wrap-text',
            renderer: function (val, metadata) {
                metadata.attr = 'style="white-space: normal;"';
                return "<div  wtf:qtip=\"" + val + "\">" + val + "</div>"
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.forecasting.current"),//"Current",
            dataIndex: 'current',
            align: 'right',
            pdfwidth: 180,
            renderer: WtfGlobal.currencyRenderer
        });
        this.ReceivablesInventorycmodel = new Wtf.grid.ColumnModel(recordArray);
        //Grid Box 4
        this.ReceivablesInventoryGrid = new Wtf.grid.GridPanel({
            stripeRows: true,
            store: this.ReceivablesInventoryStore,
            cm: this.ReceivablesInventorycmodel,
            border: false,
            id: "ReceivablesInventoryReport",
            loadMask: true,
            viewConfig: {
                emptyText: '<div style="font-size:15px; text-align:center; color:#808080; font-weight:bold; margin-top:1%;">' + WtfGlobal.getLocaleText("acc.report.budgetvscost.selectRectoLoad") + '</div>',
                forceFit: true
            }
        });
        var panel = this.createForecastingWidgets(this.ReceivablesInventoryGrid, "Receivables and Inventory", "ReceivablesInventoryID", 230);
        return panel;
    },
    FeeSummary: function () {
        //Record Box 5
        this.FeeSummaryRec = new Wtf.data.Record.create([
            {name: 'parametertitles'},
            {name: 'current'}
        ]);
        //Store Box 5
        this.FeeSummaryStore = new Wtf.data.GroupingStore({
            reader: new Wtf.data.KwlJsonReader({
                root: "FeeSummary.data",
                totalProperty: 'count'
            }, this.FeeSummaryRec),
            url: "ACCReports/getForecastingReport.do"
        });
        //Column Box 5
        var recordArray = [];
        recordArray.push(
        {
            header: " ",
            dataIndex: 'parametertitles',
            pdfwidth: 180,
            Cls: 'wrap-text',
            renderer: function (val, metadata) {
                metadata.attr = 'style="white-space: normal;"';
                return "<div  wtf:qtip=\"" + val + "\">" + val + "</div>"
            }
        }, {
            header: "Amount",
            dataIndex: 'current',
            align: 'right',
            pdfwidth: 180,
            renderer: WtfGlobal.currencyRenderer
        });
        this.FeeSummarycmodel = new Wtf.grid.ColumnModel(recordArray);
        //Grid Box 5
        this.FeeSummaryGrid = new Wtf.grid.GridPanel({
            stripeRows: true,
            store: this.FeeSummaryStore,
            cm: this.FeeSummarycmodel,
            border: false,
            id: "FeeSummaryReport",
            loadMask: true,
            viewConfig: {
                emptyText: '<div style="font-size:15px; text-align:center; color:#808080; font-weight:bold; margin-top:1%;">' + WtfGlobal.getLocaleText("acc.report.budgetvscost.selectRectoLoad") + '</div>',
                forceFit: true
            }
        });
        var panel = this.createForecastingWidgets(this.FeeSummaryGrid, "Fee Summary", "FeeSummaryID", 200);
        return panel;
    },
    ProfitLossBacklog: function () {
        //Record Box 6
        this.ProfitLossBacklogRec = new Wtf.data.Record.create([
            {name: 'parametertitles'},
            {name: 'yeartodate'},
            {name: 'inceptiontodate'}
        ]);
        //Store Box 6
        this.ProfitLossBacklogStore = new Wtf.data.GroupingStore({
            reader: new Wtf.data.KwlJsonReader({
                root: "ProfitLossBacklog.data",
                totalProperty: 'count'
            }, this.ProfitLossBacklogRec),
            url: "ACCReports/getForecastingReport.do"
        });
        //Column Box 6
        var recordArray = [];
        recordArray.push(
        {
            header: " ",
            dataIndex: 'parametertitles',
            pdfwidth: 180,
            Cls: 'wrap-text',
            renderer: function (val, metadata) {
                metadata.attr = 'style="white-space: normal;"';
                return "<div  wtf:qtip=\"" + val + "\">" + val + "</div>"
            }
        }, {
            header: "YTD",
            dataIndex: 'yeartodate',
            align: 'right',
            pdfwidth: 180,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.parametertitles == "GM %") {
                    return '<div class="currency">' + WtfGlobal.conventInDecimal(val, "") + "%" + '</div>'
                } else {
                    return WtfGlobal.currencyRenderer(val)

                }
            }
        }, {
            header: "ITD",
            dataIndex: 'inceptiontodate',
            align: 'right',
            pdfwidth: 180,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.parametertitles == "GM %") {
                    return '<div class="currency">' + WtfGlobal.conventInDecimal(val, "") + "%" + '</div>'
                } else {
                    return WtfGlobal.currencyRenderer(val)

                }
            }
        });
        this.ProfitLossBacklogcmodel = new Wtf.grid.ColumnModel(recordArray);
        //Grid Box 6
        this.ProfitLossBacklogGrid = new Wtf.grid.GridPanel({
            stripeRows: true,
            store: this.ProfitLossBacklogStore,
            cm: this.ProfitLossBacklogcmodel,
            border: false,
            id: "ProfitLossBacklogReport",
            loadMask: true,
            viewConfig: {
                emptyText: '<div style="font-size:15px; text-align:center; color:#808080; font-weight:bold; margin-top:1%;">' + WtfGlobal.getLocaleText("acc.report.budgetvscost.selectRectoLoad") + '</div>',
                forceFit: true
            }
        });
        var panel = this.createForecastingWidgets(this.ProfitLossBacklogGrid, "Profit & Loss and Backlog", "ProfitLossBacklogID", 200);
        return panel;
    },
    getDates: function (start) {
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
    }
});