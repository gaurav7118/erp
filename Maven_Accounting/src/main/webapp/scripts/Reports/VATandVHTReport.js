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

function getInvoicesVatReportDynamicLoad(record) {
    var reportPanel = Wtf.getCmp('vatreportnew');
    if(reportPanel == null){
        reportPanel = new Wtf.account.VATReportTab({
            id : 'vatreportnew',
            border : false,
            title: WtfGlobal.getLocaleText("acc.VAT.tabtitle"), //"Stock Valuation Detail Report",
            tabTip: WtfGlobal.getLocaleText("acc.VAT.tabtitle"), //"Stock Valuation Detail Report",
            record:record,
            layout: 'fit',
            closable : true,
            iconCls:'accountingbase agedrecievable',
            isVHT:false
        });
        Wtf.getCmp('as').add(reportPanel);
    }
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}
function getInvoicesVhtReportDynamicLoad(record) {
    var reportPanel = Wtf.getCmp('vhtreportnew');
    if(reportPanel == null){
        reportPanel = new Wtf.account.VATReportTab({
            id : 'vhtreportnew',
            border : false,
            title: WtfGlobal.getLocaleText("acc.VHT.tabtitle"), //"Stock Valuation Detail Report",
            tabTip: WtfGlobal.getLocaleText("acc.VHT.tabtitle"), //"Stock Valuation Detail Report",
            record:record,
            layout: 'fit',
            closable : true,
            iconCls:'accountingbase agedrecievable',
            isVHT:true
        });
        Wtf.getCmp('as').add(reportPanel);
    }
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}

//*******************************************************************************

Wtf.account.VATReportTab = function (config) {
    this.summary = new Wtf.ux.grid.GridSummary();
    this.isVHT = config.isVHT;
    this.id=config.id;

    var budgetRecord = Wtf.data.Record.create([
        {
            name: 'taxname'
        }, {
            name: 'taxcode'
        }, {
            name: 'totalsale'
        }, {
            name: 'taxrate'
        }, {
            name: 'taxamount'
        }, {
            name: 'taxcollected'
        }, {
            name: 'taxpayable'
        }, {
            name: 'mergedCategoryData'
        }, {
            name: 'mergedResourceData'
        }, {
            name: 'total'
        }, {
            name: 'totalcategorycost'
        }, {
            name: 'categoryName'
        }, {
            name: 'invdate'
        }, {
        }, {
            name: 'taxamount'
        }, {
        }, {
            name: 'invno'
        }, {
        }, {
            name: 'totalinvamt'
        }, {
        }, {
            name: 'invamt'
        }, {
        }, {
            name: 'invtaxamount'
        }, {
        }, {
            name: 'journalEntryNo'
        }, {
            name: 'invname'
        }
        , {
            name: 'currencyid'
        }
        , {
            name: 'currencysymbol'
        }
        , {
            name: 'currencyname'
        }
        , {
            name: 'currencycode'
        }
        , {
            name: 'gramtexcludingtax'
        }, {
            name: 'addAmountFlag'
        }
        , {
            name: 'totalgramtexcludingtax'
        },
        {
             name: 'amtindocCurrency'
        },
        {
             name: 'invsymbol'
        }
    ]);

    this.groupStore = new Wtf.ux.grid.MultiGroupingStore({
        url: this.isVHT ? "ACCReports/getVHTReportForGrid.do" : "ACCReports/getVATReportForGrid.do",
//            url: 'test.jsp',  
        baseParams: {
            isVHT: this.isVHT
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: "count"  //ERP-13640 & ERP-13641 [SJ]
        }, budgetRecord),
        autoLoad: false,
        groupField: ['mergedResourceData']
//         groupField: ['mergedCategoryData', 'mergedResourceData']
    });
    this.groupStore.on('load', function () {
        WtfGlobal.resetAjaxTimeOut();
        if (this.groupStore.data.items.length > 0 && this.groupStore.data.items[0].data != undefined && this.groupStore.data.items[0].data.currencysymbol != undefined)
            for (var i = 0; i < this.budgetCM.config.length; i++) {
                if (this.budgetCM.config[i].dataIndex == "invamt" && this.groupStore.data.items.length > 0 && this.groupStore.data.items[0].data != undefined && this.groupStore.data.items[0].data.currencysymbol != undefined && this.groupStore.data.items[0].data.currencysymbol != "") {
                    var header = this.isVHT ? "Amount With VHT" : "Amount With VAT";
                    this.budgetCM.setColumnHeader(i, header + " (" + this.groupStore.data.items[0].data.currencyname + ")")
                }
                if (this.budgetCM.config[i].dataIndex == "invtaxamount" && this.groupStore.data.items.length > 0 && this.groupStore.data.items[0].data != undefined && this.groupStore.data.items[0].data.currencysymbol != undefined && this.groupStore.data.items[0].data.currencysymbol != "") {
                    var header = this.isVHT ? "VHT Amount" : "VAT Amount";
                    this.budgetCM.setColumnHeader(i, header + " (" + this.groupStore.data.items[0].data.currencyname + ")")
                }
                if (this.budgetCM.config[i].dataIndex == "gramtexcludingtax" && this.groupStore.data.items.length > 0 && this.groupStore.data.items[0].data != undefined && this.groupStore.data.items[0].data.currencysymbol != undefined && this.groupStore.data.items[0].data.currencysymbol != "") {
                    var header = this.isVHT ? "Amount Without VHT" : "Amount Without VAT";
                    this.budgetCM.setColumnHeader(i, header + " (" + this.groupStore.data.items[0].data.currencyname + ")")
                }
            }
        this.storeloaded(this.groupStore);

        mainPanel.loadMask.hide();
        this.grid.getView().refresh();
    }, this);
    this.groupStore.on('loadexception', function () {
        mainPanel.loadMask.hide();
        this.grid.getView().refresh();
    }, this);

    this.groupStore.on('beforeload', function () {
        WtfGlobal.setAjaxTimeOut();
        mainPanel.loadMask.show();
    }, this);

    this.budgetCM = new Wtf.grid.ColumnModel([
        {
            hidden: true,
            header: WtfGlobal.getLocaleText("acc.bankBook.gridDate"),
            //fixed: true,
            dataIndex: 'taxcode',
            pdfwidth: 100
        }, {
            header: this.isVHT ? WtfGlobal.getLocaleText("acc.termName") : WtfGlobal.getLocaleText("acc.setupWizard.taxnam"),
            //fixed: true,
            dataIndex: 'taxcode',
            width: 150,
            pdfwidth: 100
        }, {
            header: WtfGlobal.getLocaleText("acc.bankBook.gridDate"),
            //fixed: true,
            dataIndex: 'invdate',
            width: 100,
            pdfwidth: 100//,
        }, {
            header: WtfGlobal.getLocaleText("acc.field.TransactionID"),
            //fixed: true,
            dataIndex: 'invno',
            width: 150,
            pdfwidth: 100
        }, {
            header: WtfGlobal.getLocaleText("acc.prList.JEno"),
            //fixed: true,
            dataIndex: 'journalEntryNo',
            width: 150,
            pdfwidth: 100
        }, {
            header: WtfGlobal.getLocaleText("acc.userAdmin.name"),
            //fixed: true,
            dataIndex: 'invname',
            width: 150,
            pdfwidth: 100,
            summaryRenderer: function () {
                return '<div class="grid-summary-common">' + WtfGlobal.getLocaleText("acc.common.total") + '</div>'
            }
        },
        {
            header: (this.isVHT ? WtfGlobal.getLocaleText("acc.invoice.amountWithouVht") : WtfGlobal.getLocaleText("acc.invoice.amountWithouVat")) + " (" + WtfGlobal.getCurrencyName() + ")",
            //fixed: true,
            dataIndex: 'gramtexcludingtax',
            width: 250,
            pdfwidth: 100,
            renderer: this.formatMoney,
            summaryType: 'sum',
            pdfrenderer: "rowcurrency",
            summaryRenderer: function (value, m, rec) {
                var v = parseFloat(value);
                if (isNaN(v))
                    return value;
                var retVal = WtfGlobal.withoutRateCurrencySymbol(value, m, rec)
                return '<b>' + retVal + '</b>';
            }
        },
        {
            header: (this.isVHT ? WtfGlobal.getLocaleText("acc.invoice.gridVhtAmount") : WtfGlobal.getLocaleText("acc.invoice.gridVatAmount")) + " (" + WtfGlobal.getCurrencyName() + ")",
            //fixed: true,
            dataIndex: 'invtaxamount',
            width: 250,
            pdfwidth: 100,
            renderer: this.formatMoney,
            summaryType: 'sum',
            pdfrenderer: "rowcurrency",
            summaryRenderer: function (value, m, rec) {
                var v = parseFloat(value);
                if (isNaN(v))
                    return value;
                var retVal = WtfGlobal.withoutRateCurrencySymbol(value, m, rec)
                return '<b>' + retVal + '</b>';
            }
        },
        {
            header: (this.isVHT ? WtfGlobal.getLocaleText("acc.field.PurchasesSalesValueIncludingVht") : WtfGlobal.getLocaleText("acc.field.PurchasesSalesValueIncludingVat")) + " (" + WtfGlobal.getCurrencyName() + ")",
            //fixed: true,
            dataIndex: 'invamt',
            width: 250,
            renderer: this.formatMoney,
            summaryType: 'sum',
            pdfwidth: 100,
            pdfrenderer: "rowcurrency",
            summaryRenderer: function (value, m, rec) {
                var v = parseFloat(value);
                if (isNaN(v))
                    return value;
                var retVal = WtfGlobal.withoutRateCurrencySymbol(value, m, rec)
                return '<b>' + retVal + '</b>';
            }
        },
        {
            header: (this.isVHT ? WtfGlobal.getLocaleText("acc.field.PurchasesSalesValueIncludingVhtdoc") : WtfGlobal.getLocaleText("acc.field.PurchasesSalesValueIncludingVhtdoc")),
            //fixed: true,
            dataIndex: 'amtindocCurrency',
            width: 250,
            renderer: this.formatMoneyInDoc,
            pdfwidth: 100,
            pdfrenderer: "rowcurrency"

        },
        {
            header: WtfGlobal.getLocaleText("acc.field.mergedcategory"),
            //fixed: true,
            dataIndex: 'mergedResourceData'
        }]);

    var groupView = new Wtf.ux.grid.MultiGroupingView({
        forceFit:false,
        showGroupName: false,
        enableNoGroups: false,
        isGrandTotal: false,
        isGroupTotal: true,
        hideGroupedColumn: true,
        emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec")),
        groupTextTpl: '{group} '
    });
    var gridSummary = new Wtf.grid.GroupSummary({});
    this.grid = new Wtf.ux.grid.MultiGroupingGrid({
        id: 'mGrid' + this.id,
        store: this.groupStore,
        cm: this.budgetCM,
        cls: 'colWrap',
        plugins: [gridSummary],
        view: groupView,
        sm: new Wtf.grid.RowSelectionModel({
            singleSelect: true
        })
    });

    this.startDate = new Wtf.ExDateFieldQtip({
        name: 'stdate',
        format: WtfGlobal.getOnlyDateFormat(),
        // readOnly:true,
        value: this.getDates(true)
    });

    this.endDate = new Wtf.ExDateFieldQtip({
        name: 'enddate',
        format: WtfGlobal.getOnlyDateFormat(),
        //readOnly:true,
        value: this.getDates(false)
    });

    this.fetchButton = new Wtf.Button({
        text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
        tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
        iconCls: 'accountingbase fetch',
        scope: this,
        handler: this.loadStore
    });
    /*
     * Provided button to expand or collapse all row details. 
     */
    this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Collapse"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
        scope: this,
        handler: function () {
            this.expandCollapseGrid(this.expandCollpseButton.getText());
        }
    });
    this.exportPdf = new Wtf.Button({
        anchor: '90%',
        text: WtfGlobal.getLocaleText("acc.field.ExportPDF"),
        iconCls: 'pwnd exportpdf1',
        handler: this.exportPDT

    });


    this.exportPdfButton=new Wtf.exportButton({
        obj:this,
        id:"exportReports"+this.id,
       text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details.',
//        disabled :true,
        scope : this,
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,
            xls:true
        },
        params:{
            name:"Custom Column Summary Report",
            startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericEndDate(this.endDate.getValue())

        },
        filename :this.isVHT? WtfGlobal.getLocaleText("acc.VHT.tabtitle")+"_v1":WtfGlobal.getLocaleText("acc.VAT.tabtitle")+"_v1",
        get:this.isVHT?224:223
    });
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText: WtfGlobal.getLocaleText("acc.field.SearchbyTaxNameTransactionIDJournalEntryNoName"),
        width: 200,
        field: 'taxname'
    });
    this.resetBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        disabled: false
    });
    this.resetBttn.on('click', this.handleResetClick, this);

    this.tbar = [this.quickPanelSearch, this.resetBttn, WtfGlobal.getLocaleText("acc.common.from"), this.startDate, "-", WtfGlobal.getLocaleText("acc.common.to"), this.endDate, "-", this.fetchButton, '-', this.exportPdfButton, '-', this.expandCollpseButton]


     this.pg = new Wtf.PagingSearchToolbar({ //ERP-13640 & ERP-13641 [SJ]
                id: 'pgTbarModule' + this.id,
                pageSize: 20,
                store: this.groupStore,
                displayInfo: true,
                displayMsg: 'Displaying records {0} - {1} of {2}',
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
                plugins: this.pP3 = new Wtf.common.pPageSize({})
            });
     this.groupStore.on('datachanged', function(){        
            var p = this.pP3.combo?this.pP3.combo.value:20;     
            this.quickPanelSearch.setPage(p);
        }, this);

    Wtf.apply(this, {
        border: false,
        layout: "fit",
        tbar: this.tbar,
        items: [this.grid],
        bbar:this.pg           //ERP-13640 & ERP-13641 [SJ]
    });
    Wtf.account.VATReportTab.superclass.constructor.call(this, config);
    this.addEvents({
        'journalentry': true
    });

    this.groupStore.on("beforeload", function (s, o) {
        o.params.ss = this.quickPanelSearch.getValue();
        o.params.stdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        o.params.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        o.params.withoutinventory = Wtf.account.companyAccountPref.withoutinventory;
    }, this);

    this.groupStore.load({
        params: {
            ss: this.quickPanelSearch.getValue(),
            start: 0,
            limit: this.pP3.combo?this.pP3.combo.value:20         //ERP-13640 & ERP-13641 [SJ]
        }
    });
}

Wtf.extend(Wtf.account.VATReportTab, Wtf.Panel, {
    loadStore: function () {
        if (this.startDate.getValue() > this.endDate.getValue()) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 3); // "From Date can not be greater than To Date."
            this.endDate.markInvalid();
            return;
        }
        this.groupStore.load({
            params: {
                ss: this.quickPanelSearch.getValue(),
                start: 0,
                limit: this.pP3.combo?this.pP3.combo.value:20        //ERP-13640 & ERP-13641 [SJ]
            }
        });
    },
    formatMoney: function (val, m, rec, i, j, s) {
        var fmtVal = WtfGlobal.withoutRateCurrencySymbolForGSTFM5(val, m, rec);
        return fmtVal;
    },
    formatMoneyInDoc: function (val, m, rec, i, j, s) {
       var symbol=((rec==undefined||rec.data.invsymbol==null||rec.data['invsymbol']==undefined||rec.data['invsymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['invsymbol']);
        var v=parseFloat(val);
        if(isNaN(v)) return val;
        v= WtfGlobal.conventInDecimal(v,symbol)
        return '<div class="currency">'+v+'</div>';
    },
    handleResetClick: function () {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.loadStore();
        }
    },
    storeloaded: function (store) {
        this.quickPanelSearch.StorageChanged(store);
        if (store.getCount() == 0) {
            if (this.exportButton)
                this.exportButton.disable();
            if (this.printButton)
                this.printButton.disable();
        } else {
            if (this.exportButton)
                this.exportButton.enable();
            if (this.printButton)
                this.printButton.enable();
        }
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
    exportPDFData: function (type) {
        var url = "ACCReports/exportGSTReport.do?stdate=" + WtfGlobal.convertToGenericDate(this.startDate.getValue()) + "&enddate=" + WtfGlobal.convertToGenericDate(this.endDate.getValue()) + "&reportType=" + type.reporttype + "&withoutinventory=" + Wtf.account.companyAccountPref.withoutinventory;
        Wtf.get('downloadframe').dom.src = url;
    },
    /*
     * ExpandCollapse button handler
     * To expand or collapse all row details
     * If grid rows are already in expand mode then collapse rows and vise versa
     */
    expandCollapseGrid: function (btntext) {
        if (btntext == WtfGlobal.getLocaleText("acc.field.Collapse")) {
            /*If button text is collapse then collapse all rows*/
            this.grid.getView().collapseAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if (btntext == WtfGlobal.getLocaleText("acc.field.Expand")) {
            /*If button text is expand then expand all rows*/
            this.grid.getView().expandAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    }
});

