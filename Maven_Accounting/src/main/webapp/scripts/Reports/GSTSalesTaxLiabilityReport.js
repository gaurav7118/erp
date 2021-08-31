/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * Authot - Rahul Bhawar
 *
 */
/**
 *  ERP-33784
 * Create GST Sales Tax Liability Report report Component Object
 */
function  callGSTSalesTaxLiabilityReportDynamicLoad(searchStr, filterAppend) {
    var panel = Wtf.getCmp("GSTSalesTaxLiabilityReport");
    if (panel == null) {
        /**
         * Report For Tax wise details 
         */
        var SubReport_1 = new Wtf.account.GSTSalesTaxLiabilityReport({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.gst.salestax.liability.tabTitle")),
            tabTip: WtfGlobal.getLocaleText("acc.gst.salestax.liability.tabTitle"), // "GST Sales Tax Liability Report",
            id: 'GSTSalesTaxLiabilityReport_SubReport_1',
            closable: false,
            border: false,
            iconCls:'accountingbase balancesheet'
        });
         /**
         * Report For Location wise details 
         */
        var SubReport_2 = new Wtf.account.GSTSalesTaxLiabilityDetailReport({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.gst.salestax.liability.detail.tabTitle")),
            tabTip: WtfGlobal.getLocaleText("acc.gst.salestax.liability.detail.tabTitle"), // "GST Sales Tax Liability Detail Report",
            id: 'GSTSalesTaxLiabilityReport_SubReport_2',
            closable: false,
            border: false,
            iconCls:'accountingbase balancesheet'
        });
        panel = new Wtf.TabPanel({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.gst.salestax.liability.tabTitle")),
            tabTip: WtfGlobal.getLocaleText("acc.gst.salestax.liability.tabTitle"), // "GST Sales Tax Liability Report",
            id: 'GSTSalesTaxLiabilityReport',
            closable: true,
            border: false,
            iconCls:'accountingbase balancesheet',
            activeTab: 0
        });
        panel.add(SubReport_1);
        panel.add(SubReport_2);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}


/**
 * 
 * Create GST Sales Tax Liability Report report Component and Its items details
 */

Wtf.account.GSTSalesTaxLiabilityReport = function (config) {
    this.fetchButton = new Wtf.Button({
        text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
        tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
        iconCls: 'accountingbase fetch',
        scope: this,
        handler: this.loadStore
    });
    this.startDate = new Wtf.ExDateFieldQtip({
        name: 'stdate',
        format: WtfGlobal.getOnlyDateFormat(),
        value: this.getDates(true)
    });
    this.endDate = new Wtf.ExDateFieldQtip({
        name: 'enddate',
        format: WtfGlobal.getOnlyDateFormat(),
        //readOnly:true,
        value: this.getDates(false)
    });
    //********* From Entity **************************
    this.EntityComboRec = new Wtf.data.Record.create([
        {name: 'id'},
        {name: 'name'}
    ]);

    this.EntityComboStore = new Wtf.data.Store({
        url: "AccEntityGST/getFieldComboDataForModule.do",
        baseParams: {
            moduleid: Wtf.Acc_EntityGST,
            isMultiEntity: true
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.EntityComboRec)
    });

    this.EntityCombo = new Wtf.form.ComboBox({
        fieldLabel: WtfGlobal.getLocaleText("Entity"),
        hiddenName: 'id',
        name: 'id',
        store: this.EntityComboStore,
        valueField: 'id',
        displayField: 'name',
        mode: 'local',
        typeAhead: true,
        triggerAction: 'all',
        emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseselectentity"),
        width: 170,
        listWidth: 170
    });

    this.EntityComboStore.on('load', function() {
        var count = this.EntityComboStore.getCount();
        if (count == 1) {
            var seqRec = this.EntityComboStore.getAt(0);
            this.EntityCombo.setValue(seqRec.data.id);
//            this.fetchStatement();
        }
    }, this);
    this.EntityComboStore.load();

    this.resetBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec',
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        disabled: false
    });
    this.resetBttn.on('click', this.handleResetClick, this);

    this.printButton = new Wtf.exportButton({
        text: WtfGlobal.getLocaleText("acc.common.print"), //"Print",
        obj: this,
        tooltip: WtfGlobal.getLocaleText("acc.common.printTT"), //"Print Report details.",   
        filename: WtfGlobal.getLocaleText("acc.gst.salestax.liability.tabTitle"),
        menuItem: {
            print: true
        },
        get: Wtf.autoNum.gstSalesTaxLiabilityReport
    });

    this.printButton.on("click", function () {
        this.printButton.setParams({
            startDate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            endDate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            entityId : this.EntityCombo.getValue(),
            reportId: Wtf.autoNum.gstSalesTaxLiabilityReport
        });
    }, this);
    this.exportButton = new Wtf.exportButton({
        obj: this,
        id: "exportReports" + this.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
        //        disabled: true,
        scope: this,
        filename: WtfGlobal.getLocaleText("acc.gst.salestax.liability.tabTitle") + "_v1",
        menuItem: {
            csv: true,
            pdf: true,
            rowPdf: false,
            xls: true
        },
        get: Wtf.autoNum.gstSalesTaxLiabilityReport
    });
    this.exportButton.on("click", function () {
        this.exportButton.setParams({
            startDate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            endDate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            entityId : this.EntityCombo.getValue(),
            reportId: Wtf.autoNum.gstSalesTaxLiabilityReport,
        });
    }, this);
    /**
     * Exapnd All collapse records 
     *  Expand Records details , Invoice Number , Inv. Amount, Product ID , Product Name, Tax Amount , Quantity , Unit price , Tax Rate
     */
    this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Expand"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
        scope: this,
        handler: function () {
            this.expandCollapseGrid(this.expandCollpseButton.getText());
        }
    });
    this.bottomBtnArr = [];
    this.bottomBtnArr.push('-', this.exportButton, "-", this.printButton);
    
    

    this.rec = new Wtf.data.Record.create([
        {name: "taxid"},
        {name: "taxName"},
        {name: "totalSales"},
        {name: "nonTaxableSales"},
        {name: "taxableSales"},
        {name: "taxCharged"},
        {name: "ExpanderData"}
    ]);
    
    this.groupStore = new Wtf.data.GroupingStore({
        url: "AccEntityGST/getGSTSalesTaxLiabilityReportDetails.do",
        sortInfo: {
            field: "taxid",
            direction: "ASC"
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: "totalCount"
        }, this.rec),
        scope: this
    });
    
    this.groupStore.on('beforeload', function (s, o) {
        if (!o.params)
            o.params = {};
        var currentBaseParams = this.groupStore.baseParams;
        currentBaseParams.startDate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        currentBaseParams.endDate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        currentBaseParams.reportId = Wtf.autoNum.gstSalesTaxLiabilityReport;
        currentBaseParams.entityId = this.EntityCombo.getValue();
        currentBaseParams.isDetailedReport = false;
        this.groupStore.baseParams = currentBaseParams;
    }, this);
    
    this.groupStore.on('load', function () {
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
    }, this);
    
    
    
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText: WtfGlobal.getLocaleText("acc.taxReport.search"),
        width: 150,
        field: 'taxid',
        Store: this.groupStore
    });
    
    this.tbar1 = [];
    this.tbar1.push(this.quickPanelSearch, "-",WtfGlobal.getLocaleText("Entity"),'-', this.EntityCombo,'-', WtfGlobal.getLocaleText("acc.common.from"), '-', this.startDate, "-", WtfGlobal.getLocaleText("acc.common.to"), this.endDate, this.fetchButton, "-", this.resetBttn, "-", this.expandCollpseButton);
    
    this.sm = new Wtf.grid.RowSelectionModel({
        singleSelect: true
    });
    
    this.expander = new Wtf.grid.RowExpander({});
    this.expander.on("expand", this.onRowexpand, this);
    this.expander.on("collapse", function () {
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
    }, this);
    
    this.cm = new Wtf.grid.ColumnModel([
        this.expander,
        {
            header: WtfGlobal.getLocaleText("acc.balanceSheet.particulars"),
            dataIndex: "taxName",
            width: 300,
            pdfwidth: 150,
            sortable: true
        },
        {
            align: 'right',
            header: WtfGlobal.getLocaleText("acc.gst.sales.liability.reprot.totalamount"),
            dataIndex: "totalSales",
            width: 200,
            pdfwidth: 150,
            renderer: WtfGlobal.currencyRenderer
        },
        {
            align: 'right',
            header: WtfGlobal.getLocaleText("acc.gst.sales.liability.reprot.nontaxableamount"),
            dataIndex: "nonTaxableSales",
            width: 200,
            pdfwidth: 150,
            renderer: WtfGlobal.currencyRenderer
        },
        {
            align: 'right',
            header: WtfGlobal.getLocaleText("acc.gst.sales.liability.reprot.taxableamount"),
            dataIndex: "taxableSales",
            width: 200,
            pdfwidth: 150,
            renderer: WtfGlobal.currencyRenderer
        },
        {
            align: 'right',
            header: WtfGlobal.getLocaleText("acc.gst.sales.liability.reprot.taxamount"),
            dataIndex: "taxCharged",
            width: 200,
            pdfwidth: 150,
            renderer: WtfGlobal.currencyRenderer
        }
    ]);
    
    this.gridView = new Wtf.grid.GridView({
        forceFit: true,
        startCollapsed: true,
//        emptyText: "<div class='grid-empty-text'>" + WtfGlobal.getLocaleText("acc.common.norec") + "</div>"
        emptyText:'<div class="emptyGridText">' + WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText('acc.common.norec')  + ' <br>' + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn"))+'</div>'
    });
    
    this.grid = new Wtf.grid.GridPanel({
        store: this.groupStore,
        scope: this,
        cm: this.cm,
        border: false,
        sm: this.sm,
        displayInfo: true,
        view: this.gridView,
        disabledClass: "newtripcmbss",
        autoScroll: true,
        layout: 'fit',
        height: 680,
        plugins: [this.expander]
    });
    this.grid.on("render", function() {
        this.grid.getView().applyEmptyText();
    }, this);
    this.reportPanel = new Wtf.Panel({
        layout: 'border',
        border: false,
        attachDetailTrigger: true,
        items: [
            {
                region: 'center',
                border: false,
                layout: "fit",
                autoScroll: true,
                tbar: this.tbar1,
                items: [this.grid],
                bbar: this.bottomBtnArr
            }]
    });

    Wtf.apply(this, {
        border: false,
        layout: "fit",
        items: [this.reportPanel]
    });
    
    Wtf.account.GSTSalesTaxLiabilityReport.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.GSTSalesTaxLiabilityReport, Wtf.Panel, {
    onRender: function (config) {
        Wtf.account.GSTSalesTaxLiabilityReport.superclass.onRender.call(this, config);
//        this.loadStore(); Stop auto load
    },
    handleResetClick: function () {
        this.startDate.setValue(this.getDates(true));
        this.endDate.setValue(this.getDates(false));
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
        }
        this.groupStore.removeAll();
        if (this.groupStore.getTotalCount.length == 0) {
            this.grid.getView().emptyText = '<div class="emptyGridText">' + WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText('acc.common.norec') + ' <br>' + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn")) + '</div>';
            this.grid.getView().refresh();
        }
        var count = this.EntityComboStore.getCount();
        if (count == 1) {
            var seqRec = this.EntityComboStore.getAt(0);
            this.EntityCombo.setValue(seqRec.data.id);
        } else{
            this.EntityCombo.setValue("");
        }
//        this.loadStore();
    },
    getDates: function (start) {
        var d = new Date();
        var monthDateStr = d.format('M d');
        if (Wtf.account.companyAccountPref.fyfrom) {
            monthDateStr = Wtf.account.companyAccountPref.fyfrom.format('M d');
        }
        var fd = new Date(monthDateStr + ', ' + d.getFullYear() + ' 12:00:00 AM');
        if (d < fd) {
            fd = new Date(monthDateStr + ', ' + (d.getFullYear() - 1) + ' 12:00:00 AM');
        }
        if (start) {
            return fd;
        }
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    onRowexpand: function (scope, record, body) {
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        var expandDetails = record['data']['ExpanderData'];
        var header = "";
        var HeaderName = "<span class='gridHeader'>Details</span>";
        var arr = [
            WtfGlobal.getLocaleText("acc.MailWin.msg7"), // Invoice Number
            WtfGlobal.getLocaleText("acc.prList.invAmt"), // Invoice Amount
            WtfGlobal.getLocaleText("acc.invoiceList.expand.PID"), //Prooduct ID
            WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"), //Prooduct Name
            WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice"), // Unit Price
            WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"), // Quantity
            WtfGlobal.getLocaleText("acc.gst.sales.liability.reprot.taxRate"), // Tax rate
            WtfGlobal.getLocaleText("acc.invoice.gridTaxAmount") // Tax Amount
        ];
        var count = 0;
        for (var i = 0; i < arr.length; i++) {
            if (arr[i] != "") {
                count++;
            }
        }
        //count++; // from grid no
        var widthInPercent = 100 / count;
        var minWidth = count * 100;
        header += "<div style='width: 100%;min-width:" + minWidth + "px'>";
        // header += "<span class='gridNo' style='font-weight:bold;'>" + WtfGlobal.getLocaleText("acc.cnList.Sno") + "</span>";
        for (var arrI = 0; arrI < arr.length; arrI++) {
            if (arr[arrI] != undefined)
                header += "<span class='headerRow' style='width:" + widthInPercent + "% ! important;'>" + arr[arrI] + "</span>";
        }
        header += "</div><div style='width: 100%;min-width:" + minWidth + "px'><span class='gridLine'></span></div>";
        header += this.getExpandDetailsData(expandDetails, widthInPercent, minWidth);
        var disHtml = "<div class='expanderContainer1'>" + HeaderName + header + "</div>";
        body.innerHTML = disHtml;
    },
    getExpandDetailsData: function (expandDetails, widthInPercent, minWidth) {
        var dataHTMLContent = "";
        if (expandDetails != undefined && expandDetails != '') {
            for (var detailsCount = 0; detailsCount < expandDetails.length; detailsCount++) {
                var rowDetails = expandDetails[detailsCount];
                dataHTMLContent += "<div style='width: 100%;min-width:" + minWidth + "px'>";
                dataHTMLContent += "<span class='gridRow ' style='width: " + widthInPercent + "% ! important;'>" + "<a  class='jumplink' href='#' onClick='javascript:invRecLinkNew(\"" + rowDetails['invoiceid'] + "\",\"" + true + "\")'>" + rowDetails['invoiceNumber'] + "</a>" + "</span>";
                dataHTMLContent += "<span class='gridRow'  style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.currencyRenderer(rowDetails['invoiceAmount'], [true]) + "</span>";
                dataHTMLContent += "<span class='gridRow'  style='width: " + widthInPercent + "% ! important;'><a class='jumplink' wtf:qtip='" + rowDetails['productName'] + "' href='#' onClick='javascript:Wtf.onCellClickProductDetails(\"" + rowDetails['productid'] + "\")'>" + Wtf.util.Format.ellipsis(rowDetails['productCode'], 10) + "</a></span>";
                dataHTMLContent += "<span class='gridRow'  style='width: " + widthInPercent + "% ! important;'><a class='jumplink' wtf:qtip='" + rowDetails['productName'] + "' href='#' onClick='javascript:Wtf.onCellClickProductDetails(\"" + rowDetails['productid'] + "\")'>" + Wtf.util.Format.ellipsis(rowDetails['productName'], 10) + "</a></span>";
                dataHTMLContent += "<span class='gridRow'  style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.currencyRenderer(rowDetails['unitPrice'], [true]) + "</span>";
                dataHTMLContent += "<span class='gridRow'  style='width: " + widthInPercent + "% ! important;'>" + (getRoundofValue(rowDetails['quantity']).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)) + "</span>";
                dataHTMLContent += "<span class='gridRow'  style='width: " + widthInPercent + "% ! important;'>" + rowDetails['taxRate'] + " %</span>";
                dataHTMLContent += "<span class='gridRow'  style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.currencyRenderer(rowDetails['taxAmount'], [true]) + "</span>";
                dataHTMLContent += "</div>";
            }
        }
        return dataHTMLContent;
    },
    expandCollapseGrid: function (btntext) {
        if (btntext == WtfGlobal.getLocaleText("acc.field.Collapse")) {
            /*If button text is collapse then collapse all rows*/
            this.collapseAndExpandGridRecords(true);
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if (btntext == WtfGlobal.getLocaleText("acc.field.Expand")) {
            /*If button text is expand then expand all rows*/
            this.collapseAndExpandGridRecords(false);
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    },
    collapseAndExpandGridRecords: function (isCollapse) {
        for (var storeCount = 0; storeCount < this.grid.getStore().getCount(); storeCount++) {
            if (isCollapse) {
                this.expander.collapseRow(storeCount);
            } else {
                this.expander.expandRow(storeCount);
            }
        }
    },
    loadStore: function () {
        var sDate = this.startDate.getValue();
        var eDate = this.endDate.getValue();
        if (sDate > eDate || (sDate == '' || eDate == '')) {
            WtfComMsgBox(1, 2);
            return;
        }
        this.groupStore.load();
    }
});


/**************************************************************************** * Detail report Grouping in State/ City/ County, (Upto Level 5 Locations)************************************************
 ERP-33784
 * Create GST Sales Tax Liability Report report Component and Its items details
 */

Wtf.account.GSTSalesTaxLiabilityDetailReport = function (config) {
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
    
    this.fetchButton = new Wtf.Button({
        text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
        tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
        iconCls: 'accountingbase fetch',
        scope: this,
        handler: this.loadStore
    });
    
    //********* From Entity **************************
    this.EntityComboRec = Wtf.data.Record.create([
        {name: 'id'},
        {name: 'name'}
    ]);

    this.EntityComboStore = new Wtf.data.Store({
        url: "AccEntityGST/getFieldComboDataForModule.do",
        autoLoad: true,
        baseParams: {
            moduleid: Wtf.Acc_EntityGST,
            isMultiEntity: true
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.EntityComboRec)
    });

    this.EntityCombo = new Wtf.form.ComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.gst.header.entity"),
        hiddenName: 'id',
        name: 'id',
        store: this.EntityComboStore,
        valueField: 'id',
        displayField: 'name',
        mode: 'local',
        typeAhead: true,
        forceSelection: true,
        triggerAction: 'all',
        emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseselectentity"),
        width: 170,
        listWidth: 170
    });
    
    this.resetBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        disabled: false,
        handler: this.handleResetClick
    });
    
    this.groupingStoreRec = new Wtf.data.Record.create([
        {name: 'shippedloc1'},
        {name: 'shippedloc1Name'},
        {name: 'shippedloc2'},
        {name: 'shippedloc2Name'},
        {name: 'shippedloc3'},
        {name: 'shippedloc3Name'},
        {name: 'shippedloc4'},
        {name: 'shippedloc4Name'},
        {name: 'shippedloc5'},
        {name: 'shippedloc5Name'},
        {name: 'invoiceid'},
        {name: 'invoicenumber'},
        {name: 'totalSales', type:'float'},
        {name: 'nonTaxableSales', type:'float'},
        {name: 'taxableSales', type:'float'},
        {name: 'taxAmount', type:'float'},
    ]);
    
    var groupFields = null;
    if(Wtf.Countryid == Wtf.Country.INDIA) {
        groupFields = ['shippedloc1Name'];
    } else if(Wtf.Countryid == Wtf.Country.US) {
        groupFields = ['shippedloc1Name', 'shippedloc2Name', 'shippedloc3Name'];
    } else {
        groupFields = ['shippedloc1Name', 'shippedloc2Name', 'shippedloc3Name', 'shippedloc4Name', 'shippedloc5Name'];
    }

    this.multiGroupingStore = new Wtf.ux.grid.MultiGroupingStore({
        url: "AccEntityGST/getGSTSalesTaxLiabilityReportDetails.do",
        groupField: groupFields,
        sortInfo: {field: 'invoicenumber', direction: "ASC"},
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: "totalCount"
        }, this.groupingStoreRec)
    });

    this.multiGroupingStore.on('beforeload', function (store, optionsObject) {
        WtfGlobal.setAjaxTimeOut();
        if (!optionsObject.params) {
            optionsObject.params = {};
        }
        var currentBaseParams = this.multiGroupingStore.baseParams;
        currentBaseParams.startDate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        currentBaseParams.endDate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        currentBaseParams.reportId = Wtf.autoNum.gstSalesTaxLiabilityReport;
        currentBaseParams.entityId = this.EntityCombo.getValue();
        currentBaseParams.isDetailedReport = true;
        this.multiGroupingStore.baseParams = currentBaseParams;
    }, this);
    
     this.EntityComboStore.on('load', function() {
        var count = this.EntityComboStore.getCount();
        if (count == 1) {
            var seqRec = this.EntityComboStore.getAt(0);
            this.EntityCombo.setValue(seqRec.data.id);
//            this.fetchStatement();
        }
    }, this);
    this.multiGroupingStore.on('load', function (store, recordArr) {
        
        this.grid.getView().refresh();
    }, this);

    this.cm = new Wtf.grid.ColumnModel([
        {
            header: '<b>' + WtfGlobal.getLocaleText("acc.gst.sales.liability.reprot.loc1") + '</b>',
            dataIndex: 'shippedloc1',
            width: 200,
            align: 'left',
            hidden: true,
            pdfwidth: 200,
            sortable: true,
            groupable: true,
            hideable: false
        },
        {
            header: '<b>' + WtfGlobal.getLocaleText("acc.gst.sales.liability.reprot.loc1Name") + '</b>',
            dataIndex: 'shippedloc1Name',
            width: 200,
            align: 'left',
            hidden: true,
            pdfwidth: 200,
            sortable: true,
            groupable: true,
            hideable: false
        },
        {
            header: '<b>' + WtfGlobal.getLocaleText("acc.gst.sales.liability.reprot.loc2") + '</b>',
            dataIndex: 'shippedloc2',
            width: 200,
            align: 'left',
            hidden: true,
            pdfwidth: 200,
            sortable: true,
            groupable: true,
            hideable: false
        },
        {
            header: '<b>' + WtfGlobal.getLocaleText("acc.gst.sales.liability.reprot.loc2Name") + '</b>',
            dataIndex: 'shippedloc2Name',
            width: 200,
            align: 'left',
            hidden: true,
            pdfwidth: 200,
            sortable: true,
            groupable: true,
            hideable: false
        },
        {
            header: '<b>' + WtfGlobal.getLocaleText("acc.gst.sales.liability.reprot.loc3") + '</b>',
            dataIndex: 'shippedloc3',
            width: 200,
            align: 'left',
            hidden: true,
            pdfwidth: 200,
            sortable: true,
            groupable: true,
            hideable: false
        },
        {
            header: '<b>' + WtfGlobal.getLocaleText("acc.gst.sales.liability.reprot.loc3Name") + '</b>',
            dataIndex: 'shippedloc3Name',
            width: 200,
            hidden: true,
            pdfwidth: 200,
            sortable: true,
            groupable: true,
            hideable: false
        },
        {
            header: '<b>' + WtfGlobal.getLocaleText("acc.gst.sales.liability.reprot.loc4") + '</b>',
            dataIndex: 'shippedloc4',
            width: 200,
            align: 'left',
            hidden: true,
            pdfwidth: 200,
            sortable: true,
            groupable: true,
            hideable: false
        },
        {
            header: '<b>' + WtfGlobal.getLocaleText("acc.gst.sales.liability.reprot.loc4Name") + '</b>',
            dataIndex: 'shippedloc4Name',
            width: 200,
            align: 'left',
            hidden: true,
            pdfwidth: 200,
            sortable: true,
            groupable: true,
            hideable: false
        },
        {
            header: '<b>' + WtfGlobal.getLocaleText("acc.gst.sales.liability.reprot.loc5") + '</b>',
            dataIndex: 'shippedloc5',
            width: 200,
            align: 'left',
            hidden: true,
            pdfwidth: 200,
            sortable: true,
            groupable: true,
            hideable: false
        },
        {
            header: '<b>' + WtfGlobal.getLocaleText("acc.gst.sales.liability.reprot.loc5Name") + '</b>',
            dataIndex: 'shippedloc5Name',
            width: 200,
            align: 'left',
            hidden: true,
            pdfwidth: 200,
            sortable: true,
            groupable: true,
            hideable: false
        },
        {
            header: '<b>' + WtfGlobal.getLocaleText("acc.landedcost.grid.column.invoiceid") + '</b>',
            dataIndex: 'invoiceid',
            width: 200,
            align: 'left',
            hidden: true,
            pdfwidth: 200,
            sortable: true,
            groupable: true,
            hideable: false
        },
        {
            header: '<b>' + WtfGlobal.getLocaleText("acc.balanceSheet.particulars") + '</b>',
            dataIndex: 'invoicenumber',
            width: 400,
            hidden: false,
            pdfwidth: 200,
            sortable: true,
            groupable: true,
            renderer: WtfGlobal.linkRenderer,
            summaryRenderer: function () {
                return '<div class="grid-summary-common">' + WtfGlobal.getLocaleText("acc.common.total") + '</div>';
            }
        },
        {
            header: '<b>' + WtfGlobal.getLocaleText("acc.gst.sales.liability.reprot.totalamount") + '</b>',
            dataIndex: 'totalSales',
            width: 200,
            align: 'right',
            hidden: false,
            pdfwidth: 200,
            renderer: WtfGlobal.currencyRenderer,
            summaryType :'sum',
            summaryRenderer: WtfGlobal.currencySummaryRenderer
        },
        {
            header: '<b>' + WtfGlobal.getLocaleText("acc.gst.sales.liability.reprot.nontaxableamount") + '</b>',
            dataIndex: 'nonTaxableSales',
            width: 200,
            align: 'right',
            hidden: false,
            pdfwidth: 200,
            renderer: WtfGlobal.currencyRenderer,
            summaryType :'sum',
            summaryRenderer: WtfGlobal.currencySummaryRenderer
        },
        {
            header: '<b>' + WtfGlobal.getLocaleText("acc.gst.sales.liability.reprot.taxableamount") + '</b>',
            dataIndex: 'taxableSales',
            width: 200,
            align: 'right',
            hidden: false,
            pdfwidth: 200,
            renderer: WtfGlobal.currencyRenderer,
            summaryType :'sum',
            summaryRenderer: WtfGlobal.currencySummaryRenderer
        },
        {
            header: '<b>' + WtfGlobal.getLocaleText("acc.gst.sales.liability.reprot.taxamount") + '</b>',
            dataIndex: 'taxAmount',
            width: 200,
            align: 'right',
            hidden: false,
            pdfwidth: 200,
            renderer: WtfGlobal.currencyRenderer,
            summaryType :'sum',
            summaryRenderer: WtfGlobal.currencySummaryRenderer
        },
    ]);
    
    this.summary = new Wtf.ux.grid.GridSummary({});

    this.sm = new Wtf.grid.CheckboxSelectionModel({
        singleSelect: false
    });

    this.groupView = new Wtf.ux.grid.MultiGroupingView({
        forceFit: true,
        showGroupName: false,
        enableNoGroups: false,
        isGrandTotal: false,
        isGroupTotal: false,
        hideGroupedColumn: false,
        enableGroupingMenu :false,
        autoFill: true,
        emptyText:'<div class="emptyGridText">' + WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText('acc.common.norec')  + ' <br>' + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn"))+'</div>',
        groupTextTpl: '{group}'
    });

    this.grid = new Wtf.ux.grid.MultiGroupingGrid({
        stripeRows: true,
        store: this.multiGroupingStore,
        loadMask: false,
        border: false,
        view: this.groupView,
        ctCls:'agedsummary',
        cm: this.cm,
        sm: this.sm,
        plugins: [this.summary]
    });
    
    this.grid.on('cellclick', this.cellClickHandler, this);
    this.grid.on("render", function() {
        this.grid.getView().applyEmptyText();
    }, this);
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText: WtfGlobal.getLocaleText("acc.gst.sales.liability.reprot.search"),
        width: 150,
        field: 'tax',
        Store: this.multiGroupingStore
    });
    
    this.tbar1 = new Array(this.quickPanelSearch, "-",WtfGlobal.getLocaleText("Entity"),'-', this.EntityCombo,'-',WtfGlobal.getLocaleText("acc.common.from"), '-', this.startDate, "-", WtfGlobal.getLocaleText("acc.common.to"), this.endDate, "-", this.fetchButton, "-", this.resetBttn);
    
    this.printButton = new Wtf.exportButton({
        text: WtfGlobal.getLocaleText("acc.common.print"), //"Print",
        obj: this,
        tooltip: WtfGlobal.getLocaleText("acc.common.printTT"), //"Print Report details.",   
        filename: WtfGlobal.getLocaleText("acc.gst.salestax.liability.tabTitle"),
        menuItem: {
            print: true
        },
        get: Wtf.autoNum.gstSalesTaxLiabilityReport
    });

    this.printButton.on("click", function () {
        this.printButton.setParams({
            reportId: Wtf.autoNum.gstSalesTaxLiabilityReport,
            startDate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            endDate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            entityId : this.EntityCombo.getValue(),
            isDetailedReport: true,
        });
    }, this);
    
    this.exportButton = new Wtf.exportButton({
        obj: this,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
        scope: this,
        filename: WtfGlobal.getLocaleText("acc.gst.salestax.liability.tabTitle") + "_v1",
        menuItem: {
            csv: true,
            pdf: true,
            rowPdf: false,
            xls: true
        },
        get: Wtf.autoNum.gstSalesTaxLiabilityReport
    });
    
    this.exportButton.on("click", function () {
        this.exportButton.setParams({
            startDate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            endDate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            reportId: Wtf.autoNum.gstSalesTaxLiabilityReport,
            entityId : this.EntityCombo.getValue(),
            isDetailedReport: true
        });
    }, this);
    
    this.bottomBtnArr = new Array('-', this.exportButton, "-", this.printButton);

    this.reportPanel = new Wtf.Panel({
        layout: 'border',
        border: false,
        attachDetailTrigger: true,
        items: [
            {
                region: 'center',
                border: false,
                layout: "fit",
                autoScroll: true,
                tbar: this.tbar1,
                items: [this.grid],
                bbar: this.bottomBtnArr
            }
        ]
    });

    Wtf.apply(this, {
        border: false,
        layout: "fit",
        items: [this.reportPanel]
    });
    this.isActive = false;
    Wtf.account.GSTSalesTaxLiabilityDetailReport.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.GSTSalesTaxLiabilityDetailReport, Wtf.Panel, {
    onRender: function (config) {
        Wtf.account.GSTSalesTaxLiabilityDetailReport.superclass.onRender.call(this, config);
        this.on('activate', function () {
            if (!this.isActive) {
//                this.multiGroupingStore.load();
                this.isActive = true;
            }
        }, this);
    },
    
    getDates: function (start) {
        var d = new Date();
        var monthDateStr = d.format('M d');
        if (Wtf.account.companyAccountPref.fyfrom) {
            monthDateStr = Wtf.account.companyAccountPref.fyfrom.format('M d');
        }
        var fd = new Date(monthDateStr + ', ' + d.getFullYear() + ' 12:00:00 AM');
        if (d < fd) {
            fd = new Date(monthDateStr + ', ' + (d.getFullYear() - 1) + ' 12:00:00 AM');
        }
        if (start) {
            return fd;
        }
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    
    cellClickHandler: function (grid, rowIndex, columnIndex, event) {
        if (event.getTarget("a[class='jumplink']")) {
            viewDocumentTab({
                moduleid : Wtf.Acc_Invoice_ModuleId +"",
                billid : grid.getStore().getAt(rowIndex).data.invoiceid
            });
        }
    },
    
    handleResetClick: function () {
        this.startDate.setValue(this.getDates(true));
        this.endDate.setValue(this.getDates(false));
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
        }
        this.multiGroupingStore.removeAll();
        if (this.multiGroupingStore.getTotalCount.length == 0) {
            this.grid.getView().emptyText = '<div class="emptyGridText">' + WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText('acc.common.norec') + ' <br>' + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn")) + '</div>';
            this.grid.getView().refresh();
        }
         var count = this.EntityComboStore.getCount();
        if (count == 1) {
            var seqRec = this.EntityComboStore.getAt(0);
            this.EntityCombo.setValue(seqRec.data.id);
        } else{
            this.EntityCombo.setValue("");
        }
//        this.loadStore();
    },
    
    loadStore: function () {
        var sDate = this.startDate.getValue();
        var eDate = this.endDate.getValue();
        if (sDate > eDate || (sDate == '' || eDate == '')) {
            WtfComMsgBox(1, 2);
            return;
        }
        this.multiGroupingStore.load();
    }
});
