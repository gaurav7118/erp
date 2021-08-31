/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


function CustomColumnLineDetail() {  //Detail Report

    var panel = Wtf.getCmp("CustomDetailReport");
    if (panel == null) {
        panel = new Wtf.account.CustomDetailReport({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.CustomColumnLineDetailReport.Report"), Wtf.TAB_TITLE_LENGTH), //"Finance Details"
            tabTip: WtfGlobal.getLocaleText("acc.CustomColumnLineDetailReport.Reporttooltip"),
            id: 'CustomDetailReport',
            border: false,
            isFromSummaryReport: false,
            isLinedetailReport: true,
            searchJson: "",
            filterConjuctionCrit: "",
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            closable: true

        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();

}
Wtf.account.CustomDetailReport = function(config) {
    Wtf.apply(this, config);
    this.arr = [];
    this.isFromSummaryReport = config.isFromSummaryReport;
    this.isLinedetailReport  = config.isLinedetailReport;
    this.checkRecXtype = config.checkRecXtype;
    this.reportid = Wtf.autoNum.customLineDetailsReport,
            this.moduleid = 102;

    this.startDate = new Wtf.ExDateFieldQtip({
        fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), //'From',
        name: 'stdate' + this.id,
        format: WtfGlobal.getOnlyDateFormat(),
        value: WtfGlobal.getDates(true)
    });
    this.endDate = new Wtf.ExDateFieldQtip({
        fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), //'To',
        format: WtfGlobal.getOnlyDateFormat(),
        name: 'enddate' + this.id,
        value: WtfGlobal.getDates(false)
    });
    this.resetBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
        hidden: this.isSummary,
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        disabled: false
    });
    this.resetBttn.on('click', this.handleResetClickNew, this);
    this.StoreUrl = "ACCOtherReports/getCustomColumnDetails.do";
    this.customColumnDetailLoadMask = new Wtf.LoadMask(document.body, {
        msg: WtfGlobal.getLocaleText("acc.msgbox.527")
    });
    this.Store = new Wtf.data.GroupingStore({
        url: this.StoreUrl,
        baseParams: {
            deleted: false,
            nondeleted: false,
            CashAndInvoice: true,
            companyids: companyids,
            gcurrencyid: gcurrencyid,
            userid: loginid,
            startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate: WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            reportId: Wtf.autoNum.customLineDetailsReport,
            filterConjuctionCriteria: this.filterConjuctionCrit == undefined ? "" : this.filterConjuctionCrit,
            searchJson: this.searchJson == undefined ? "" : this.searchJson,
        },
//        sortInfo: {
//            field: 'group',
//            direction: 'ASC'
//        },
//        groupField: 'group',
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'totalCount'
        })
    });

    this.Store.on('loadexception', this.hideLoading, this);
    this.pagingToolbar = new Wtf.PagingSearchToolbar({
        pageSize: 15,
        id: "pagingtoolbar" + this.id,
        store: this.Store,
        displayInfo: true,
        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
        plugins: this.pP = new Wtf.common.pPageSize({
            id: "pPageSize_" + this.id
        })
    });

    this.Store.on('datachanged', function() {
        var p = this.pP.combo.value;
    }, this);

    this.Store.on('load', function() {
        this.loadStoreData();
        this.hideLoading();
        this.exportButton.enable()
    }, this);

    this.Store.on('beforeload', function() {
        this.customColumnDetailLoadMask.show();
        WtfGlobal.setAjaxTimeOut();
        var startDate = "", endDate = "";
        startDate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        endDate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        this.Store.baseParams = {
            deleted: false,
            nondeleted: false,
            CashAndInvoice: true,
            companyids: companyids,
            gcurrencyid: gcurrencyid,
            userid: loginid,
            startdate: startDate,
            enddate: endDate,
            filterConjuctionCriteria: this.filterConjuctionCrit == undefined ? "" : this.filterConjuctionCrit,
            searchJson: this.searchJson == undefined ? "" : this.searchJson,
            reportId: Wtf.autoNum.customLineDetailsReport,
        }
        if (this.pP != undefined && this.pP.combo != undefined) {
            if (this.pP.combo.value == "All") {
                var count
                if (this.store != undefined) {
                    count = this.store.getTotalCount()
                }
                var rem = count % 5;
                if (rem == 0) {
                    count = count;
                } else {
                    count = count + (5 - rem);
                }
            }
        }

    }, this);
    /*
     * expander code
     */
    this.expander = new Wtf.grid.RowExpander({});
    this.expander.on("expand", this.onRowexpand, this);
    this.summary = new Wtf.grid.GroupSummary({});
    this.rowNo = new Wtf.grid.RowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.gridView1 = new Wtf.grid.GroupingView({
        forceFit: false,
        showGroupName: true,
        enableGroupingMenu: true,
        hideGroupedColumn: false,
        emptyText: "<div class='grid-empty-text'>" + WtfGlobal.getLocaleText("acc.common.norec") + "</div>"
    });
    var columnArr = [];
    this.groupStore = new Wtf.data.GroupingStore({
        groupField: ['group']
//         groupField:['group','Company']
    });
    this.cm = new Wtf.grid.ColumnModel(columnArr);
    var gridSummary = new Wtf.grid.GroupSummary({});
    this.grid = new Wtf.grid.GridPanel({
        stripeRows: true,
        store: this.groupStore,
        sm: this.sm,
        border: false,
        region: "center",
        viewConfig: this.gridView1,
//        forceFit: true,
        layout: 'fit',
        plugins: [gridSummary, this.expander],
        columns: [{
                dataIndex: "group"
            }],
        columns: [],
                view: this.gridView1,
//        cm: new Wtf.grid.ColumnModel(columnArr),
//        listeners: {
//            "reloadexternalgrid": function() {
//                if (!this.searchparam)
//                    this.loaddata.defer(10, this);
//                else
//                    this.showAdvanceSearch.defer(10, this);
//            },
//            scope: this
//        }
    });
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: this.moduleid,
        isCustomDetailReport: true,
        checkRecXtype: this.checkRecXtype,
        hideRememberSerch: true,
        advSearch: false,
        lineLevelSearch: true,
        isLinedetailReport: this.isLinedetailReport
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);

    this.exportButton = new Wtf.exportButton({
        obj: this,
        filename: WtfGlobal.getLocaleText("acc.CustomColumnLineDetailReport.Report") + "_v1",
        id: "exportReports" + this.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
        usePostMethod:true,
        disabled: true,
        scope: this,
        menuItem: {
            csv: true,
            pdf: true,
            rowPdf: false,
            xls: true,
            detailedXls: true
        },
        params: {
            name: "Custom Column Detail Report",
            startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate: WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            reportId: Wtf.autoNum.customLineDetailsReport
        },
        get: Wtf.autoNum.customLineDetailsReport
    });
    this.exportButton.on("click", function() {
        this.exportButton.setParams({
            startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate: WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            filterConjuctionCriteria: this.filterConjuctionCrit == undefined ? "" : this.filterConjuctionCrit,
            searchJson: this.searchJson == undefined ? "" : this.searchJson,
            reportId: Wtf.autoNum.customLineDetailsReport
        });
    }, this);
    this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Expand"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
        hidden: false,
        scope: this,
        handler: this.expandAllHandler
    });
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        scope: this,
        hidden: (this.moduleid == undefined) ? true : false,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });

    this.fetchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.ra.fetch"),
        iconCls: 'accountingbase fetch',
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.invReport.fetchTT"),
        handler: this.fetchData
    });
    this.customReportViewBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
        handler: this.customizeView,
        iconCls: 'accountingbase fetch'
    });
    var buttonArray = new Array();
    buttonArray.push(WtfGlobal.getLocaleText("acc.common.from"), this.startDate, WtfGlobal.getLocaleText("acc.common.to"), this.endDate);   //this.quickPanelSearch,
    buttonArray.push('-', this.fetchBtn, this.resetBttn);
    buttonArray.push('-', this.AdvanceSearchBtn, '-', this.exportButton, '-', this.expandCollpseButton);     //'-',this.resetBttn,, '-', this.printButton
    buttonArray.push('->', this.customReportViewBtn);

    this.leadpan = new Wtf.Panel({
        layout: 'border',
        border: false,
        attachDetailTrigger: true,
        items: [this.objsearchComponent,
            {
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar: buttonArray,
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.Store,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"),
                    plugins: this.pP = new Wtf.common.pPageSize({
                        id: "pPageSize_" + this.id
                    })
                })
            }]

    });
    this.loaddata();
    Wtf.apply(this, {
        border: false,
        layout: "fit",
        items: [this.leadpan]
    });
    Wtf.account.CustomDetailReport.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.CustomDetailReport, Wtf.Panel, {
    hideLoading: function() {
        WtfGlobal.resetAjaxTimeOut();
        if (this.customColumnDetailLoadMask)
            this.customColumnDetailLoadMask.hide();
        Wtf.MessageBox.hide();
    },
    loaddata: function() {

        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo != undefined) ? this.pP.combo.getValue() : 30
            }
        });
        this.exportButton.enable();
    },
    loadStoreData: function() {
        var columns = [];
        this.Store.sort("group","ASC");
        this.rowNo = new Wtf.grid.RowNumberer();
        this.sm = new Wtf.grid.CheckboxSelectionModel();
        columns.push(this.expander);
        columns.push(this.sm);
        columns.push(this.rowNo);
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
        var Arr = [];
        Wtf.each(this.Store.reader.jsonData.metaData.fields, function(column) {
            Arr.push(column);
        });
        this.groupStore.removeAll();
        this.groupStore.fields = Arr;
        this.grid.getColumnModel().setConfig(columns);
        this.groupStore.add(this.Store.getRange(0, (this.Store.data.items.length - 1)));

//        this.grid.getColumnModel().setConfig(columns);

        this.grid.getView().refresh();
    },
    showAdvanceSearch: function() {
        showAdvanceSearch(this, this.searchparam, this.filterAppend);
    },
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();

    },
    expandAllHandler: function() {
        if (this.expandCollpseButton.getText() == WtfGlobal.getLocaleText("acc.field.Expand")) {
            this.expandButtonClicked = true;
        }
        if (this.expandCollpseButton.getText() == WtfGlobal.getLocaleText("acc.field.Collapse")) {
            for (var i = 0; i < this.expander.grid.store.data.length - 1; i++) {
                this.expander.collapseRow(i);
            }
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if (this.expandCollpseButton.getText() == WtfGlobal.getLocaleText("acc.field.Expand")) {
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
            for (var j = 0; j < this.expander.grid.store.data.length - 1; j++) {
                if (this.expandButtonClicked) {
                    this.expander.expandRow(j);                // After data set to Grid Row, expand row forcefully.
                }
            }
        }
    },
    filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;

        this.Store.load({
            params: {
                flag: 1,
                searchJson: this.searchJson,
                moduleid: this.moduleId,
                filterConjuctionCriteria: filterConjuctionCriteria,
                deleted: false,
                nondeleted: false,
                CashAndInvoice: true,
                start: 0,
                limit: this.pP.combo.value,
                reportId: Wtf.autoNum.customLineDetailsReport,
            }
        });
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.load({
            params: {
                flag: 1,
                searchJson: this.searchJson,
                moduleid: this.moduleId,
                filterConjuctionCriteria: this.filterConjuctionCrit,
                start: 0,
                limit: this.pP.combo.value,
                reportId: Wtf.autoNum.customLineDetailsReport,
            }
        });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },
    handleResetClickNew: function()
    {
        var startDate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        var endDate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        this.startDate.reset();
        this.endDate.reset();
        this.Store.load({
            deleted: false,
            nondeleted: false,
            CashAndInvoice: true,
            companyids: companyids,
            gcurrencyid: gcurrencyid,
            userid: loginid,
            startdate: startDate,
            enddate: endDate,
            start: 0,
            limit: 30,
            reportId: Wtf.autoNum.customLineDetailsReport,
        })

    },
    fetchData: function() {
        var sDate = this.startDate.getValue();
        var eDate = this.endDate.getValue();
        if (sDate > eDate) {
            WtfComMsgBox(1, 2);
            return;
        }
        this.Store.load({
            params: {
                start: 0,
                limit: this.pP.combo.value,
                stdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                reportId: Wtf.autoNum.customLineDetailsReport,
            }
        });
    },
    customizeView: function() {
        this.customizeViewWin = new Wtf.CustomizeReportView({
            title: WtfGlobal.getLocaleText("acc.field.CreateCustomizeView"),
            parentPanel: this,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            grid: this.grid,
            reportId: Wtf.autoNum.customLineDetailsReport,
            modules: '' + Wtf.Acc_Invoice_ModuleId + ',' + Wtf.Acc_Vendor_Invoice_ModuleId + ',' + Wtf.Acc_GENERAL_LEDGER_ModuleId,
        });
        this.customizeViewWin.show();
    },
    onRowexpand: function(scope, record, body, rowIndex) {
        this.expanderBody = body;
        this.fillExpanderBody(record);
    },
    fillExpanderBody: function(record) {
        var disHtml = "";
        var arr = [];
        var arrHeader = [];
        arrHeader = [WtfGlobal.getLocaleText("acc.prList.invNo"),
            WtfGlobal.getLocaleText("acc.prList.creDate"),
            WtfGlobal.getLocaleText("acc.prList.dueDate"),
            WtfGlobal.getLocaleText("acc.prList.invAmt"),
            WtfGlobal.getLocaleText("acc.prList.amtDue")];
        var gridHeaderText = WtfGlobal.getLocaleText("acc.field.InvoiceDetails");//(rec.data.withoutinventory)?WtfGlobal.getLocaleText("acc.invoiceList.expand.pListNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pList");
        var invHeader = "<span class='gridHeader'>" + gridHeaderText + "</span>"; //Product List

        arr = [WtfGlobal.getLocaleText("acc.invoiceList.expand.PID"), WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"), WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridDescription"),
            WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"), WtfGlobal.getLocaleText("acc.invoiceList.expand.unitPrice"), WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc"), WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"), WtfGlobal.getLocaleText("acc.invoiceList.expand.amt")];
        var header = "<span class='gridHeader'>" + WtfGlobal.getLocaleText("acc.invoiceList.expand.pList") + "</span>";
        var custArr = [];
        var GLType = false;
        var isExpense = false;
        var forCreditNote = false;
        var forDebitNote = false;
        if (record.data.group == "Accounts Payable (AP)") {           // add custom columns for manual,party,fund trasfer JE records only.
            if (record.data.isExpense) {
                isExpense = true;
                arr = [WtfGlobal.getLocaleText("acc.invoiceList.accName"), WtfGlobal.getLocaleText("acc.invoiceList.expand.description"), WtfGlobal.getLocaleText("acc.invoiceList.expand.amt"), WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc"), WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"), WtfGlobal.getLocaleText("acc.invoiceList.totAmt"), '                  '];
                header = "<span class='gridHeader'>" + WtfGlobal.getLocaleText("acc.invoiceList.expand.accList") + "</span>";   //Account List
            }
            custArr = WtfGlobal.appendCustomColumn(custArr, GlobalColumnModel[Wtf.Acc_Vendor_Invoice_ModuleId]);
            if(record.data.isDebitNote != "" && record.data.isDebitNote){
                arr = [WtfGlobal.getLocaleText("acc.jeList.expandJE.accName"), WtfGlobal.getLocaleText("acc.invoiceList.expand.tax"), WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"), WtfGlobal.getLocaleText("acc.invoiceList.expand.amt"), WtfGlobal.getLocaleText("acc.jeList.expandJE.desc")];
                header = "<span class='gridHeader'>" + WtfGlobal.getLocaleText("acc.invoiceList.expand.accList") + "</span>";   //Account List
                forDebitNote = true;
                custArr = [];
                custArr = WtfGlobal.appendCustomColumn(custArr, GlobalColumnModel[Wtf.Acc_Debit_Note_ModuleId]);
            }
            if(record.data.isCreditNote != "" && record.data.isCreditNote){
                arr = [WtfGlobal.getLocaleText("acc.jeList.expandJE.accName"), WtfGlobal.getLocaleText("acc.invoiceList.expand.tax"), WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"), WtfGlobal.getLocaleText("acc.invoiceList.expand.amt"), WtfGlobal.getLocaleText("acc.jeList.expandJE.desc")];
                header = "<span class='gridHeader'>" + WtfGlobal.getLocaleText("acc.invoiceList.expand.accList") + "</span>";   //Account List
                forCreditNote = true;
                custArr = [];
                custArr = WtfGlobal.appendCustomColumn(custArr, GlobalColumnModel[Wtf.Acc_Credit_Note_ModuleId]);
            }
        } else if (record.data.group == "Accounts Receivable (AR)") {
            if (record.data.isCreditNote != "" && record.data.isCreditNote) {
                custArr = WtfGlobal.appendCustomColumn(custArr, GlobalColumnModel[Wtf.Acc_Credit_Note_ModuleId]);
                arr = [WtfGlobal.getLocaleText("acc.jeList.expandJE.accName"), WtfGlobal.getLocaleText("acc.invoiceList.expand.tax"), WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"), WtfGlobal.getLocaleText("acc.invoiceList.expand.amt"), WtfGlobal.getLocaleText("acc.jeList.expandJE.desc")];
                header = "<span class='gridHeader'>" + WtfGlobal.getLocaleText("acc.invoiceList.expand.accList") + "</span>";   //Account List
                forCreditNote = true;
            } else if (record.data.isDebitNote != "" && record.data.isDebitNote){
                custArr = WtfGlobal.appendCustomColumn(custArr, GlobalColumnModel[Wtf.Acc_Debit_Note_ModuleId]);
                arr = [WtfGlobal.getLocaleText("acc.jeList.expandJE.accName"), WtfGlobal.getLocaleText("acc.invoiceList.expand.tax"), WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"), WtfGlobal.getLocaleText("acc.invoiceList.expand.amt"), WtfGlobal.getLocaleText("acc.jeList.expandJE.desc")];
                header = "<span class='gridHeader'>" + WtfGlobal.getLocaleText("acc.invoiceList.expand.accList") + "</span>";   //Account List
                forDebitNote = true;
            }else
                custArr = WtfGlobal.appendCustomColumn(custArr, GlobalColumnModel[Wtf.Acc_Invoice_ModuleId]);
        } else if (record.data.group == "GL") {
            GLType = true;
            header = "<span class='gridHeader'>" + WtfGlobal.getLocaleText("acc.jeList.expandJE.transList") + "</span>";
            custArr = WtfGlobal.appendCustomColumn(custArr, GlobalColumnModel[Wtf.Acc_GENERAL_LEDGER_ModuleId]);
            arr = [WtfGlobal.getLocaleText("acc.jeList.expandJE.accName"), WtfGlobal.getLocaleText("acc.jeList.expandJE.desc"), WtfGlobal.getLocaleText("acc.jeList.expandJE.amtCredit")];
        } else {
            return;
        }
        var arrayLength = arr.length;
        for (i = 0; i < custArr.length; i++) {
            if (custArr[i].header != undefined)
                arr[arrayLength + i] = custArr[i].header;
        }
        var count = 0;
        for (var i = 0; i < arr.length; i++) {
            if (arr[i] != "") {
                count++;
            }
        }
        count = count + 2;
        var widthInPercent = 100 / count;
        var minWidth = count * 100;
        header += "<div style='width: 100%;min-width:" + minWidth + "px'>";
        for (var i = 0; i < arr.length; i++) {
            header += "<span class='headerRow' style='width:" + widthInPercent + "% ! important;'>" + arr[i] + "</span>";
        }
        for (var j = 0; j < arrHeader.length; j++) {
            invHeader += "<span class='headerRow' style='width:" + widthInPercent + "% ! important;'>" + arrHeader[j] + "</span>";
        }
        invHeader += "<span class='gridLine'></span>";
        header += "</div><div style='width: 100%;min-width:" + minWidth + "px'><span class='gridLineforJE'></span></div>";
//        header += "<div style='width: 100%;min-width:" + minWidth + "px'>";
        var details = record.data.details;
        var invcnt = 0;
        for (i = 0; i < details.length; i++) {
            var rec = details[i];
            var accname = "";
            if (!GLType && !isExpense && !forCreditNote && !forDebitNote) {
                if (rec["productId"] != "" && rec["productId"] != undefined) {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + rec["productId"] + "'>" + Wtf.util.Format.ellipsis(rec["productId"], 15) + "</span>";
                } else {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                }
                if (rec["productName"] != "" && rec["productName"] != undefined) {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + rec["productName"] + "'>" + Wtf.util.Format.ellipsis(rec["productName"], 15) + "</span>";
                } else {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                }

                if (rec['description'] != "" && rec["description"] != undefined) {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + rec['description'] + "'>" + Wtf.util.Format.ellipsis(rec['description'], 15) + "</span>";
                } else {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                }
                if (rec["quantity"] != undefined) {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + parseFloat(getRoundofValue(rec['quantity'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + " " + rec['unitname'] + "</span>";
                } else {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                }
                if (rec["rate"] != undefined) {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.withCurrencyUnitPriceRenderer(rec["rate"], true, record) + "</span>";
                } else {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                }
                if (rec["discountamt"] != undefined) {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.withCurrencyUnitPriceRenderer(rec["discountamt"], true, record) + "</span>";
                } else {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                }
                if (rec["taxamt"] != undefined) {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.withCurrencyUnitPriceRenderer(rec["taxamt"], true, record) + "</span>";
                } else {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                }
                if (rec["amount"] != undefined) {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.withCurrencyUnitPriceRenderer(rec["amount"], true, record) + "</span>";
                } else {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                }
            }

            if (GLType || isExpense) {
                if (rec["accountname"] != "" && rec["accountname"] != undefined) {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + rec["accountname"] + "'>" + Wtf.util.Format.ellipsis(rec["accountname"], 15) + "</span>";
                } else {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                }
                if (rec['description'] != "" && rec["description"] != undefined) {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + rec['description'] + "'>" + Wtf.util.Format.ellipsis(rec['description'], 15) + "</span>";
                } else {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                }
                if (rec["amount"] != undefined) {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.withCurrencyUnitPriceRenderer(rec["amount"], true, record) + "</span>";
                } else {
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                }
                if (isExpense) {
                    if (rec["discountamt"] != undefined) {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.withCurrencyUnitPriceRenderer(rec["discountamt"], true, record) + "</span>";
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }
                    if (rec["taxamt"] != undefined) {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.withCurrencyUnitPriceRenderer(rec["taxamt"], true, record) + "</span>";
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }
                    if (rec["totalamt"] != undefined) {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.withCurrencyUnitPriceRenderer(rec["totalamt"], true, record) + "</span>";
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }
                }

            } else if (forCreditNote || forDebitNote) {
                if (rec["detailType"] == "CNAccountDetails" || rec["detailType"] == "DNAccountDetails") {
                    if (rec["accountname"] != "" && rec["accountname"] != undefined) {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + rec["accountname"] + "'>" + Wtf.util.Format.ellipsis(rec["accountname"], 15) + "</span>";
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }
                    if (rec["taxpercent"] != undefined) {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + rec["taxpercent"] + "</span>";
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }
                    if (rec["taxamount"] != undefined) {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.withCurrencyUnitPriceRenderer(rec["taxamount"], true, record) + "</span>";
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }
                    if (rec["amount"] != undefined) {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.withCurrencyUnitPriceRenderer(rec["amount"], true, record) + "</span>";
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }
                    if (rec['description'] != "" && rec["description"] != undefined) {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + rec['description'] + "'>" + Wtf.util.Format.ellipsis(rec['description'], 15) + "</span>";
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }

                } else {
                    invHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;' wtf:qtip='" + rec["invoiceno"] + "'>" + Wtf.util.Format.ellipsis(rec["invoiceno"], 15) + "</span>";
                    invHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;' wtf:qtip='" + rec["invoicecreationdate"] + "'>" + rec['invoicecreationdate'] + "</span>";
                    invHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;' wtf:qtip='" + rec["invduedate"] + "'>" + rec['invduedate'] + "</span>";
                    invHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.withCurrencyUnitPriceRenderer(rec["invamount"], true, record) + "</span>";
                    invHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.withCurrencyUnitPriceRenderer(rec["invamountdue"], true, record) + "</span>";
                    invcnt++;
                }
            }

            for (var j = 0; j < custArr.length; j++) {
                if (rec[custArr[j].dataIndex] != undefined && rec[custArr[j].dataIndex] != "null" && rec[custArr[j].dataIndex] != "")
                    header += "<span class='gridRow'style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + rec[custArr[j].dataIndex] + "'>" + Wtf.util.Format.ellipsis(rec[custArr[j].dataIndex], 15) + "&nbsp;</span>";
                else
                    header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;&nbsp;</span>";
            }
            header += "<br>";
            invHeader += "<br>";
        }
//        header += "</div>";
        if ((forCreditNote || forDebitNote) && invcnt > 0) {
            disHtml += "<div class='expanderContainer1'>" + header + invHeader + "</div>";
        } else
            disHtml += "<div class='expanderContainer1'>" + header + "</div>";
        this.expanderBody.innerHTML = disHtml;
    },
});

