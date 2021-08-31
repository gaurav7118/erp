/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**********************Monthly P&L Report and Monthly Budget Report in P&L Custom Layout Dynamic js Load***********************/
function callMonthlyCustomLayoutDynamicLoad(templateid, templatetitle, statementType, reportid, searchStr, filterAppend) {
    //We are creating separate ID's for monthly layout and for Dimension Based Monthly layout. This is done because both Dimension Based Monthly PL and Monthly PL are called from same component.
    var reportCmpId = (reportid === Wtf.autoNum.dimensionBasedMonthlyPLCustomLayout) ? "monthlydimensionbasedcustomlayout" + templateid : "monthlycustomlayout" + templateid;
    var panel = Wtf.getCmp(reportCmpId);
    if (panel == null) {
        panel = new Wtf.account.monthlyCustomLayout({
            id: reportCmpId,
            title: Wtf.util.Format.ellipsis(templatetitle, Wtf.TAB_TITLE_LENGTH),
            tabTip: templatetitle,
            topTitle: '<center><font size=4>' + Wtf.util.Format.ellipsis(templatetitle, Wtf.TAB_TITLE_LENGTH) + '</font></center>',
            moduleid: 101,
            searchJson: "",
            filterConjuctionCrit: "",
            templateid: templateid,
            templatetitle: templatetitle,
            statementType: statementType,
            reportid: reportid,
            border: false,
            closable: true,
            layout: 'fit',
            iconCls: 'accountingbase financialreport'
        });
        Wtf.getCmp('as').add(panel);
        panel.on('account', callLedger);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    showAdvanceSearch(panel, searchStr, filterAppend);
    Wtf.getCmp('as').doLayout();
}

/**********************Monthly P&L Report and Monthly Budget Report in P&L Custom Layout***********************/
Wtf.account.monthlyCustomLayout = function (config) {
    this.templatetitle = config.templatetitle;
    this.statementType = config.statementType;//ype of report. For Monthly PL and Dimension Based Monthly PL report - "TradingAndProfitLoss", For Monthly BS report - "BalanceSheet"
    this.moduleid = config.moduleid;//moduleid - 101
    this.templateid = config.templateid;
    /*
     * reportid is different for all three reports implemeted by this component.
     * Wtf.autoNum.profitAndLossMonthlyCustomLayout : Monthly PnL
     * Wtf.autoNum.balanceSheetMonthlyCustomLayout : Monthly BS
     * Wtf.autoNum.dimensionBasedMonthlyPLCustomLayout : Dimension Based Monthly PnL
     */
    this.reportid = config.reportid;

    this.store = new Wtf.data.Store({//report store
        reader: new Wtf.data.KwlJsonReader2({
            totalProperty: 'totalcount',
            root: "left"
        }),
        baseParams: {
            nondeleted: true
        },
        url: Wtf.req.account + 'CompanyManager.jsp'
    });

    this.columnArr = [{//Dummy column array to be used to initialize the grid's columns config. We can't use empty array in palce fo this, so inserted one column in the array
            header: '<b>' + WtfGlobal.getLocaleText("acc.balanceSheet.particulars") + '</b>',
            dataIndex: 'accountname',
            renderer: WtfGlobal.formatAccountName,
            width: 200,
            align: 'left',
            hidden: false,
            pdfwidth: 200,
            summaryRenderer: function () {
                return WtfGlobal.summaryRenderer(WtfGlobal.getLocaleText("acc.common.total"));
            }.createDelegate(this)
        }];
    this.grid = new Wtf.grid.HirarchicalGridPanel({//report grid
        autoScroll: true,
        store: this.store,
        hirarchyColNumber: 0,
        columns: this.columnArr,
        border: false,
        loadMask: true,
        viewConfig: {
            forceFit: false,
            //Empty text to be shown in Dimension Based Monthly PL report when there is no data in grid
            emptyText: (this.reportid === Wtf.autoNum.dimensionBasedMonthlyPLCustomLayout) ? ("<br>" + WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.field.pleseselectdimension1"))) : ('<div class="emptyGridText">' + WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText('acc.common.norec')  + ' <br>' + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn"))+'</div>'),//Message for user after opening report to fectch data//WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec")),
            deferEmptyText: false
        }
    });
    this.grid.on("render", WtfGlobal.autoApplyHeaderQtip);
    this.grid.on('rowclick', this.onRowClickLGrid, this);
    this.grid.on('render', function () {
        this.grid.getView().applyEmptyText();
        this.grid.getView().getRowClass = this.getRowClass.createDelegate(this, [this.grid], 1);
        if (this.reportid === Wtf.autoNum.dimensionBasedMonthlyPLCustomLayout) {  //Reset grid
            this.store.removeAll();
        }
    }, this);

    this.grid.getStore().on("load", function () {
        this.grid.getView().refresh();
        for (var i = 0; i < this.grid.getStore().data.length; i++) {
            this.grid.collapseRow(this.grid.getView().getRow(i));
        }
    }, this);

    var btnArr = [];

    this.monthStore = new Wtf.data.SimpleStore({
        fields: [
            {name: 'monthid', type: 'int'},
            'name'
        ],
        data: [
            [0, "January"],
            [1, "February"],
            [2, "March"],
            [3, "April"],
            [4, "May"],
            [5, "June"],
            [6, 'July'],
            [7, 'August'],
            [8, "September"],
            [9, "October"],
            [10, "November"],
            [11, "December"]
        ]
    });

    var data = WtfGlobal.getBookBeginningYear(true);
    this.yearStore = new Wtf.data.SimpleStore({
        fields: [
            {name: 'id', type: 'int'},
            'yearid'
        ],
        data: data
    });

    var tempStartDate = WtfGlobal.getDates(true);//Start date for financial year to set as initial start-date
    var tempEndDate = WtfGlobal.getDates(false);//End date for financial year to set as initial end-date
    var tempStartMonth = tempStartDate.getMonth();
    var tempStartYear = tempStartDate.getFullYear();
    var tempEndMonth = tempEndDate.getMonth();
    var tempEndYear = tempEndDate.getFullYear();

    this.startMonth = new Wtf.form.ComboBox({
        store: this.monthStore,
        fieldLabel: WtfGlobal.getLocaleText("acc.accPref.month"), //'Month',
        name: 'endMonth',
        displayField: 'name',
        valueField: 'monthid',
        value: tempStartMonth,
        forceSelection: true,
        anchor: '95%',
        mode: 'local',
        width: 90,
        triggerAction: 'all',
        selectOnFocus: true
    });
    this.startYear = new Wtf.form.ComboBox({
        store: this.yearStore,
        fieldLabel: WtfGlobal.getLocaleText("acc.accPref.year"), //'Year',
        name: 'endYear',
        displayField: 'yearid',
        valueField: 'yearid',
        value: tempStartYear,
        anchor: '95%',
        forceSelection: true,
        mode: 'local',
        width: 90,
        triggerAction: 'all',
        selectOnFocus: true
    });
    this.endMonth = new Wtf.form.ComboBox({
        store: this.monthStore,
        fieldLabel: WtfGlobal.getLocaleText("acc.accPref.month"), //'Month',
        name: 'endMonth',
        displayField: 'name',
        valueField: 'monthid',
        value: tempEndMonth,
        forceSelection: true,
        anchor: '95%',
        mode: 'local',
        width: 90,
        triggerAction: 'all',
        selectOnFocus: true
    });
    this.endYear = new Wtf.form.ComboBox({
        store: this.yearStore,
        fieldLabel: WtfGlobal.getLocaleText("acc.accPref.year"), //'Year',
        name: 'endYear',
        displayField: 'yearid',
        valueField: 'yearid',
        value: tempEndYear,
        anchor: '95%',
        forceSelection: true,
        mode: 'local',
        width: 90,
        triggerAction: 'all',
        selectOnFocus: true
    });
    btnArr.push(WtfGlobal.getLocaleText("acc.common.from"), this.startMonth, this.startYear, WtfGlobal.getLocaleText("acc.common.to"), this.endMonth, this.endYear);


    var reportViewTypeDefaultValue = "";//Default Columns which will be shown in the report when no columns are selected by user from 'Columns To Show' combo-box
    if (this.reportid === Wtf.autoNum.dimensionBasedMonthlyPLCustomLayout) { //Create combo-box store-data to select/deselect column to be shown in monthyl Dimension Based PnL
        var reportViewTypeStoreData = [
            ['Monthly Amount(s)', '0'],
            ['Total Amount(s)', '1'],
            ['Monthly Total Amount(s)', '7'],
            ['YTD Amount(s)', '4']
        ];
        reportViewTypeDefaultValue = "Monthly Amount(s),Total Amount(s)";
    } else if (this.reportid === Wtf.autoNum.balanceSheetMonthlyCustomLayout) { //Create combo-box store-data to select/deselect column to be shown in monthyl BS
        var reportViewTypeStoreData = [
            ['Monthly Amount(s)', '0'],
            ['Total Amount', '1'],
            ['YTD Amount', '4']
        ];
        reportViewTypeDefaultValue = "Monthly Amount(s),Total Amount";
    } else if (this.reportid === Wtf.autoNum.profitAndLossMonthlyCustomLayout) {//For Monthly P&L and Budget report, create combobox store-data to select/deselect columns to be shown in report
        var reportViewTypeStoreData = [
            ['Monthly Actual Amount(s)', '0'],
            ['Total Actual Amount', '1'],
            ['Monthly Budget Amount(s)', "2"],
            ['Total Budget Amount', "3"],
            ['YTD Actual Amount', "4"],
            ['YTD Budget Amount', "5"],
            ['YTD Variance (Actual - Budget)', "6"]
        ];
        reportViewTypeDefaultValue = "Monthly Actual Amount(s),Total Actual Amount";
    }
    this.reportViewTypeStore = new Wtf.data.SimpleStore({
        fields: [{
                name: 'name'
            }, {
                name: 'id'
            }],
        data: reportViewTypeStoreData
    });
    this.reportViewTypeComboConfig = {
        store: this.reportViewTypeStore,
        valueField: 'id',
        hideLabel: true,
        displayField: 'name',
        emptyText: reportViewTypeDefaultValue, //default value
        mode: 'local',
        typeAhead: true,
        selectOnFocus: true,
        triggerAction: 'all',
        scope: this
    };
    this.reportViewTypeCombo = new Wtf.common.Select(Wtf.applyIf({
        multiSelect: true,
        fieldLabel: "Filter Type",
        forceSelection: true,
        value: "0,1", //default value Period Amounts
        width: 200
    }, this.reportViewTypeComboConfig));
    btnArr.push('-', "Columns To Show", this.reportViewTypeCombo);

    if (this.reportid !== Wtf.autoNum.dimensionBasedMonthlyPLCustomLayout) {
        btnArr.push('-', {//Fetch Button, not added if report is Dimension Based
            xtype: 'button',
            text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
    }

    var grid = this.grid;
    if (this.reportid === Wtf.autoNum.dimensionBasedMonthlyPLCustomLayout) {  //Advanced Search for Dimension Based monthly PL report
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.grid.colModel,
            moduleid: this.moduleid,
            dimensionBasedComparisionReport: true,
            dimBasedSkipRequestParams: true,
            advSearch: false,
            reportid: this.reportid,
            templateid: this.templateid,
            templatetitle: this.templatetitle,
//            isCustomLayout: true
        });
    } else {    //Advanced Search for Monthly PL or BS report
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: grid.colModel,
            moduleid: this.moduleid,
            advSearch: false,
            reportid: this.reportid,
            templateid: this.templateid,
            templatetitle: this.templatetitle,
//            isCustomLayout: true
        });
    }
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);

    //Advanced search button (for "Monthly PnL and Budget" or "Monthly BS") or Dimension Selection button (For monthly Dimension based PnL report),
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: (this.reportid === Wtf.autoNum.dimensionBasedMonthlyPLCustomLayout) ? WtfGlobal.getLocaleText("erp.report.SelectDimensions") : WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"),
        scope: this,
        tooltip: (this.reportid === Wtf.autoNum.dimensionBasedMonthlyPLCustomLayout) ? WtfGlobal.getLocaleText("erp.report.SelectDimensionsTT") : WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"),
        handler: this.configurAdvancedSearch,
        iconCls: (this.reportid === Wtf.autoNum.dimensionBasedMonthlyPLCustomLayout) ? getButtonIconCls(Wtf.etype.add) : "advanceSearchButton"
    });
    btnArr.push('-', this.AdvanceSearchBtn);

    //Reset button to be pushed in Dimension Based monthly PL
    if (this.reportid === Wtf.autoNum.dimensionBasedMonthlyPLCustomLayout) {
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"),
            scope: this,
            handler: this.handleResetClick,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        });
        btnArr.push('-', this.resetBttn);
    }

    var btnArr1 = [];

    //Costcenter combo-box
    chkCostCenterload();
    if (Wtf.CostCenterStore.getCount() == 0) {
        Wtf.CostCenterStore.on("load", this.setCostCenter, this);
    }
    this.costCenter = new Wtf.form.ComboBox({
        store: Wtf.CostCenterStore,
        name: 'costCenterId',
        width: 140,
        displayField: 'name',
        valueField: 'id',
        triggerAction: 'all',
        mode: 'local',
        typeAhead: true,
        value: "",
        selectOnFocus: true,
        forceSelection: true,
        emptyText: WtfGlobal.getLocaleText("acc.rem.9")  //,"Select a Cost Center"
    });
    btnArr1.push(WtfGlobal.getLocaleText("acc.common.costCenter"), this.costCenter);

    //Export Button
    this.exportButton = new Wtf.exportButton({
        obj: this,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip: WtfGlobal.getLocaleText("acc.agedPay.exportTT"), //'Export report details',
        filename: this.templatetitle,
        params: {
            searchJson: this.searchJson,
            mode: 65,
            nondeleted: true,
            costcenter: this.costCenter.getValue(),
            reportView: this.statementType,
            templateid: this.templateid,
            reportid: this.reportid,
        },
        menuItem: {
            csv: true,
            pdf: true,
            xls: true
        },
        //'get' value is used to fetch export URL while exporting. We are using same URL to export all three reports.
        //So we could provide any one here and put the URL. In our case we are using Wtf.autoNum.profitAndLossMonthlyCustomLayout
        get: Wtf.autoNum.profitAndLossMonthlyCustomLayout
    });
    btnArr1.push('-', this.exportButton);

    //Print Button
    this.printButton = new Wtf.exportButton({
        obj: this,
        text: WtfGlobal.getLocaleText("acc.common.print"), //"Print",
        tooltip: WtfGlobal.getLocaleText("acc.common.printTT"), //"Print Report details",
        filename: this.templatetitle,
        menuItem: {
            print: true
        },
        params: {
            searchJson: this.searchJson,
            mode: 65,
            nondeleted: true,
            costcenter: this.costCenter.getValue(),
            reportView: this.statementType,
            templateid: this.templateid,
            reportid: this.reportid,
        },
        //'get' value is used to fetch print URL while exporting. We are using same URL to print all three reports.
        //So we could provide any one here and put the URL. In our case we are using Wtf.autoNum.profitAndLossMonthlyCustomLayout
        get: Wtf.autoNum.profitAndLossMonthlyCustomLayout
    });
    btnArr1.push('-', this.printButton);

    //Expand-Collapse Button
    this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Expand"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
        scope: this,
        handler: function () {
            this.expandCollapseGrid(this.expandCollpseButton.getText());
        }
    });
    btnArr1.push('-', this.expandCollpseButton);

    this.wrapperPanel = new Wtf.Panel({
        border: false,
        layout: "border",
        scope: this,
        items: [this.objsearchComponent,
            this.westPanel = new Wtf.Panel({
                width: '100%',
                region: 'center',
                layout: 'fit',
                border: false,
                items: this.grid
            })],
        tbar: btnArr1
    });

    Wtf.apply(this, {
        defaults: {border: false, bodyStyle: "background-color:white;"},
        saperate: true,
        statementType: "TradingAndProfitLoss",
        items: this.wrapperPanel,
        tbar: btnArr
    }, config);

    Wtf.account.monthlyCustomLayout.superclass.constructor.call(this, config);
    this.addEvents({
        'account': true
    });
    if (this.reportid !== Wtf.autoNum.dimensionBasedMonthlyPLCustomLayout) {//Fetch data and Populate grid in case of monthly PnL or Monthly BS report, for Dimension Based Monthly PL report grid is not to be populated initially
        //this.fetchStatement();
    }
}

Wtf.extend(Wtf.account.monthlyCustomLayout, Wtf.Panel, {
    onRowClickLGrid: function (g, i, e) {
        e.stopEvent();
        var el = e.getTarget("a");
        if (el == null)
            return;
        var accid = this.grid.getStore().getAt(i).data['accountid'];
        this.fireEvent('account', accid, this.sDate, this.eDate, false);
    },
    expandCollapseGrid: function (btntext) {
        if (btntext == WtfGlobal.getLocaleText("acc.field.Collapse")) {
            for (var i = 0; i < this.grid.getStore().data.length; i++) {
                this.grid.collapseRow(this.grid.getView().getRow(i));
            }
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if (btntext == WtfGlobal.getLocaleText("acc.field.Expand")) {
            for (var i = 0; i < this.grid.getStore().data.length; i++) {
                this.grid.expandRow(this.grid.getView().getRow(i));
            }
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    },
    setCostCenter: function () {
        this.costCenter.setValue("");//Select Default Cost Center as None
        Wtf.CostCenterStore.un("load", this.setCostCenter, this);
    },
    collapseGrids: function () {
        for (var i = 0; i < this.grid.getStore().data.length; i++) {
            this.grid.collapseRow(this.grid.getView().getRow(i));

        }
    },
    getRowClass: function (record, grid) {
        var colorCss = "";
        switch (record.data["fmt"]) {
            case "T":
                colorCss = " grey-background";
                break;
            case "B":
                colorCss = " red-background";
                break;
            case "H":
                colorCss = " header-background";
                break;
            case "A":
                colorCss = " darkyellow-background";
                break;
        }
        return grid.getRowClass() + colorCss;
    },
    fetchStatement: function () {
        this.grid.getStore().removeAll();
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        WtfComMsgBox(29, 4, true); //Show loading mask
        this.sMonth = this.startMonth.getValue();
        this.eMonth = this.endMonth.getValue();
        this.sYear = this.startYear.getValue();
        this.eYear = this.endYear.getValue();
        var monthCount = (this.eMonth - this.sMonth) + 12 * (this.eYear - this.sYear) + 1;

        if (monthCount < 1) {//if start-date is greater than end-date, show alert
            WtfComMsgBox(1, 2);
            return;
        }
        if (monthCount > 12) { //if date range exceeds 12 months, show alert
            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.59"));
            return;
        }

        this.sDate = new Date(this.sYear, this.sMonth, 1, 0, 0, 0, 0);//Start-Date with first day of starting month
        this.eDate = new Date(this.eYear, this.eMonth + 1, 0, 23, 59, 59, 999);//End Date with list day of ending month

        this.sdate = WtfGlobal.convertToGenericStartDate(this.sDate);
        this.edate = WtfGlobal.convertToGenericEndDate(this.eDate);

        this.exportButton.setParams({
            startdate: this.sdate,
            enddate: this.edate,
            templatecode: '-1',
            periodView: false,
            excludePreviousYear: false,
            mode: 65,
            nondeleted: true,
            costcenter: this.costCenter.getValue(),
            reportView: this.statementType,
            reportid: this.reportid,
            templateid: this.templateid,
            singleGrid: true,
            monthlyCustomLayoutFlag: true, //This flag is to indicate that the report is monthly custom layout. It is used in controller to call service for monthly custom layout on this flag's check
            reportViewType: this.reportViewTypeCombo.getValue(),
            searchJson: this.searchJson != undefined ? this.searchJson : "",
            filterConjuctionCriteria: this.filterConjuctionCrit
        });

        this.printButton.setParams({
            startdate: this.sdate,
            enddate: this.edate,
            templatecode: '-1',
            periodView: false,
            excludePreviousYear: false,
            mode: 65,
            nondeleted: true,
            costcenter: this.costCenter.getValue(),
            reportView: this.statementType,
            reportid: this.reportid,
            templateid: this.templateid,
            singleGrid: true,
            monthlyCustomLayoutFlag: true, //This flag is to indicate that the report is monthly custom layout. It is used in controller to call service for monthly custom layout on this flag's check
            reportViewType: this.reportViewTypeCombo.getValue(),
            searchJson: this.searchJson != undefined ? this.searchJson : "",
            filterConjuctionCriteria: this.filterConjuctionCrit
        });

        var params = {
            stdate: this.sdate,
            enddate: this.edate,
            templatecode: '-1',
            periodView: false,
            excludePreviousYear: false
        }
        params.searchJson = this.searchJson != undefined ? this.searchJson : "";
        params.filterConjuctionCriteria = this.filterConjuctionCrit;

        this.ajxUrl = Wtf.req.account + 'CompanyManager.jsp';
        params.mode = 65;
        params.nondeleted = true;
        params.costcenter = this.costCenter.getValue();
        params.reportView = this.statementType;
        params.reportid = this.reportid;
        params.templateid = this.templateid;
        params.singleGrid = true;
        params.monthlyCustomLayoutFlag = true;//This flag is to indicate that the report is monthly custom layout. It is used in controller to call service for monthly custom layout on this flag's check
        params.reportViewType = this.reportViewTypeCombo.getValue();
        this.ajxUrl = "ACCReports/getBSorPL_CustomLayout.do";

        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({url: this.ajxUrl, params: params}, this, this.successCallback, this.failureCallback);
    },
    successCallback: function (response) {
        WtfGlobal.resetAjaxTimeOut();
        if (response.success) {
            this.grid.getStore().reader.read(response);//to read metaData (store fields and columns) from response
            this.grid.store.loadData(response.data);
            this.doLayout();

            var columns = [];
            Wtf.each(this.grid.getStore().reader.jsonData.columns, function (column) {
                if (column.editor) {
                    var editor = eval("(" + column.editor + ")");
                    column.editor = editor;
                }
                if (column.dataIndex == "accountname") {
                    column.renderer = this.grid.formatAccountName.createDelegate(this.grid);
                    var summaryRenderer = function () {
                        return WtfGlobal.summaryRenderer(WtfGlobal.getLocaleText("acc.common.total"));
                    }.createDelegate(column);
                    column.summaryRenderer = summaryRenderer;
                } else if (column.dataIndex != "accountcode" && column.dataIndex != "accountname") {
                    column.renderer = WtfGlobal.formatMoney;
                }
                columns.push(column);
            }, this);

            this.grid.getColumnModel().setConfig(columns);
            this.grid.getView().refresh(true);

            this.collapseGrids();
        }
        this.hideLoading();
    },
    failureCallback: function (response) {
        WtfGlobal.resetAjaxTimeOut();
        this.hideLoading();
    },
    hideLoading: function () {
        Wtf.MessageBox.hide();
    },
    configurAdvancedSearch: function () {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
    },
    filterStore: function (json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.fetchStatement();
    },
    clearStoreFilter: function () {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        if (this.reportid !== Wtf.autoNum.dimensionBasedMonthlyPLCustomLayout) {//Refresh grid if report is not dimension based PL report
            this.fetchStatement();
        }
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },
    handleResetClick: function () {
        this.startMonth.reset();
        this.startYear.reset();
        this.endMonth.reset();
        this.endYear.reset();
        this.costCenter.reset();
        this.clearStoreFilter();
        this.objsearchComponent.advGrid.cancelSearch();
        this.store.removeAll();
        this.grid.getColumnModel().setConfig(this.columnArr);
        this.grid.getView().refresh(true);
    }
});
