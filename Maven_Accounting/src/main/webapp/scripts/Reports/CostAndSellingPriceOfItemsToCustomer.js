/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function getCostAndSellingPriceOfItemsToCustomerDynamicLoad() {
    var panel = Wtf.getCmp("costAndSellingPriceOfItemsToCustomer");
    if (panel == null) {
        panel = new Wtf.account.CostAndSellingPriceOfItemsToCustomer({
            title: WtfGlobal.getLocaleText("acc.report.costandsellingpriceofanitemtocustomer"),
            tabTip: WtfGlobal.getLocaleText("acc.report.costandsellingpriceofanitemtocustomer.tooltip"),
            id: "costAndSellingPriceOfItemsToCustomer",
            border: false,
            layout: 'fit',
            closable: true,
            isvatReports: true,
            iconCls: 'accountingbase invoicelist'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}


Wtf.account.CostAndSellingPriceOfItemsToCustomer = function(config) {
    Wtf.apply(this, config);

    var buttonArray = new Array();
    this.productRec = Wtf.data.Record.create([
        {name: 'productid'},
        {name: 'productname'},
        {name: 'desc'},
        {name: 'producttype'}
    ]);
    this.productStore = new Wtf.data.Store({
        url: "ACCProduct/getProductsForCombo.do",
        baseParams: {mode: 22,
            onlyProduct: true
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.productRec)
    });
    this.productStore.on("load", function() {
        var record = new Wtf.data.Record({
            productid: "",
            productname: "All Products"
        });
        this.productStore.insert(0, record);
        this.productname.setValue("");
    }, this);
    this.productStore.load();
    this.productname = new Wtf.form.ComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.productList.gridProduct") + '*',
        hiddenName: 'productid',
        name: 'productid',
        store: this.productStore,
        valueField: 'productid',
        displayField: 'productname',
        mode: 'local',
        typeAhead: true,
        triggerAction: 'all',
        hideLabel: true,
        emptyText: WtfGlobal.getLocaleText("acc.msgbox.17"),
        width: 100,
        listWidth: 150
    });
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText: WtfGlobal.getLocaleText("acc.coa.accountSearchText"), // "Search by Document Account Name
        width: 150,
        id: "quickSearch" + this.id
    });

    this.startDate = new Wtf.ExDateFieldQtip({
        fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), //'From',
        name: 'startdate' + this.id,
        format: WtfGlobal.getOnlyDateFormat(),
        value: WtfGlobal.getDates(true)
    });

    this.endDate = new Wtf.ExDateFieldQtip({
        fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), //'To',
        format: WtfGlobal.getOnlyDateFormat(),
        name: 'enddate' + this.id,
        value: WtfGlobal.getDates(false)
    });


    this.fetchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.agedPay.fetch"), //'Fetch',           
        scope: this,
        id: 'fetchBtn' + this.id,
        tooltip: WtfGlobal.getLocaleText("acc.invReport.fetchTT"),
        handler: this.fetchData,
        iconCls: 'accountingbase fetch'
    });
    this.resetBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        disabled: false
    });
    this.resetBttn.on('click', this.handleResetClickNew, this);
    this.expButton = new Wtf.exportButton({
        obj: this,
        tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"),
        filename: WtfGlobal.getLocaleText("acc.report.costandsellingpriceofanitemtocustomer") + "_v1",
        menuItem: {
            csv: true,
            pdf: true,
            rowPdf: false,
            xls: true
        },
        label: WtfGlobal.getLocaleText("acc.report.costandsellingpriceofanitemtocustomer"),
        get: Wtf.autoNum.costandsellingpriceforanitemstocustomer
    });

    this.printButton = new Wtf.exportButton({
        text: WtfGlobal.getLocaleText("acc.common.print"), //"Print",
        filename: WtfGlobal.getLocaleText("acc.report.costandsellingpriceofanitemtocustomer"),
        obj: this,
        tooltip: WtfGlobal.getLocaleText("acc.common.printTT"), //"Print Report details.",   
        menuItem: {
            print: true
        },
        label: WtfGlobal.getLocaleText("acc.report.costandsellingpriceofanitemtocustomer"),
        get: Wtf.autoNum.costandsellingpriceforanitemstocustomer
    });
    this.helpText = "<p>This report gives the information on the cost price of a product and the selling price of a product to customers.</p><p>Following are the default filter values which are applied to this report.</p><ul><li><b>- Start Date : </b>Beginning Date of Financial Year. ("+WtfGlobal.getDates(true).format(WtfGlobal.getOnlyDateFormat())+")</li><li><b>- End Date : </b>Ending Date of Financial Year. ("+WtfGlobal.getDates(false).format(WtfGlobal.getOnlyDateFormat())+")</li><li><li><b>- Product : </b>All Products.</li><li></p>";
    this.helpButton = new Wtf.Toolbar.Button({
        scope: this,
        iconCls: 'helpButton',
        tooltip: {text: WtfGlobal.getLocaleText("acc.rem.2")}, //{text:'Get started by clicking here!'},
        mode: id,
        handler: function(e, target, panel) {
            var tmp = e.getEl().getXY();
            var we = new Wtf.CostAndSellingPricehelpDetails();
            we.showHelpWindow(tmp[0], tmp[1], WtfGlobal.getLocaleText("acc.report.costandsellingpriceofanitemtocustomer"), this.helpText);
        }
    });
    buttonArray.push(WtfGlobal.getLocaleText("acc.common.from"), this.startDate, WtfGlobal.getLocaleText("acc.common.to"),
            this.endDate, "-", WtfGlobal.getLocaleText("acc.invReport.prod"), this.productname, "-", this.fetchBtn, this.resetBttn, this.expButton, this.printButton, "->", this.helpButton);

    this.record = new Wtf.data.Record.create([
        {name: "invoiceid"},
        {name: "donumber"},
        {name: "poid"},
        {name: 'productid'},
        {name: 'productname'},
        {name: 'desc'},
        {name: 'invoiceno'},
        {name: 'date'},
        {name: 'quantity'},
        {name: 'quantitywithuom'},
        {name: 'unitname'},
        {name: 'costprice'},
        {name: 'unitprice'},
        {name: 'amountcost'},
        {name: 'amountsales'},
        {name: 'profitmargin'},
        {name: 'percentageprofitmargin'},
        {name: 'customername'},
        {name: 'ponumber'},
        {name: 'podate'},
        {name: 'vendorname'}
    ]);

    this.store = new Wtf.data.Store({
        url: "ACCReports/getDataForCostAndSellingPriceReport.do",
        reader: new Wtf.data.KwlJsonReader({
            totalProperty: "totalCount",
            root: "data"
        }, this.record),
        sortInfo: {field: 'productname', direction: "ASC"}
    });
//    this.store = new Wtf.data.GroupingStore({
//        url: "ACCReports/getDataForCostAndSellingPriceReport.do",
//        reader: new Wtf.data.KwlJsonReader({
//            totalProperty: "totalCount",
//            root: "data"
//        }, this.record),
//        groupField : 'customername',
//        sortInfo: {field: 'productname', direction: "ASC"}
//    });

    this.store.on('load', function(store) {
        WtfGlobal.resetAjaxTimeOut(); 
        this.grid.getView().refresh();
    }, this);
    this.store.on('loadexception', function(store) {
         WtfGlobal.resetAjaxTimeOut(); 
        this.grid.getView().refresh();
    }, this);

    this.store.on('beforeload', function(s, o) {
         WtfGlobal.setAjaxTimeOut();
        if (!o.params) {
            o.params = {};
        }
        o.params.startdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        o.params.enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        if (this.productname != undefined && this.productname.getValue() != "") {
            o.params.productid = this.productname.getValue();
        }
    }, this);
    this.store.load({
        params: {
            start: 0,
            limit: 30
        }
    });
//    this.groupingview = new Wtf.grid.GroupingView({
//        forceFit: false,
//        showGroupName: true,
//        enableGroupingMenu: true,
//        hideGroupedColumn: false,
//        emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
//    });
    this.rowNo = new Wtf.grid.RowNumberer();
    this.grid = new Wtf.grid.GridPanel({
        store: this.store,
        sm: this.sm,
        border: false,
        layout: 'fit',
        viewConfig: {
            forceFit: false,
            emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
//        viewConfig: this.groupingview,
        loadMask: true, 
        columns: [this.rowNo, {
                header: WtfGlobal.getLocaleText("acc.contract.product.name"), // "Product Name",
                dataIndex: 'productname',
                align: 'left',
                width: 150,
                pdfwidth: 150,
                renderer: function(val, m, rec) {
                    val = val.replace(/(<([^>]+)>)/ig, "");
                    var oldVal = val;
                    if (rec.data.deleted) {
                        val = "<del  wtf:qtip='" + oldVal + "' >" + oldVal + "</del>";
                    }
                    return "<a class='hirarchical' wtf:qtip='" + oldVal + "' href='#' onClick='javascript:Wtf.onCellClickProductDetails(\"" + rec.data.productid + "\")'>" + val + "</a>";
                },
                sortable:true
            }, {
                header: WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"),
                dataIndex: 'desc',
                width: 100,
                pdfwidth: 100
            }, {
                header: WtfGlobal.getLocaleText("acc.agedPay.gridIno"),
                dataIndex: 'invoiceno',
                width: 100,
                pdfwidth: 100,
                autoWidth: true,
                sortable:true
//                ,
//                renderer: WtfGlobal.linkDeletedRenderer
            }, {
                header: WtfGlobal.getLocaleText("erp.DONumber"),
                dataIndex: "donumber",
                width: 100,
                pdfwidth: 100,
                autoWidth: true,
                sortable:true
//                ,
//                renderer: WtfGlobal.linkDeletedRenderer
            }, {
                header: WtfGlobal.getLocaleText("acc.agedPay.invoiceDate"), // "Invoice Date",
                dataIndex: "date",
                pdfwidth: 100,
                width: 100
            }, {
                header: WtfGlobal.getLocaleText("acc.product.gridQty") + ' (' + WtfGlobal.getLocaleText("acc.product.stockUoMLabel") + ')',
                dataIndex: 'quantitywithuom',
                align: 'center',
                width: 100,
                pdfwidth: 100
//                ,
//                renderer: function(value) {
//                    return parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
//                }
            }, {
                header: WtfGlobal.getLocaleText("acc.invoice.grid.costprice") + ' (' + WtfGlobal.getCurrencySymbol() + ')',
                dataIndex: "costprice",
                align: 'right',
                width: 100,
                pdfwidth: 100,
                renderer: WtfGlobal.currencyRendererSymbol
            }, {
                header: WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice") + ' (' + WtfGlobal.getCurrencySymbol() + ')',
                dataIndex: 'unitprice',
                align: 'right',
                width: 100,
                pdfwidth: 100,
                renderer: WtfGlobal.currencyRendererSymbol
            }, {
                header: WtfGlobal.getLocaleText("acc.invoice.grid.amountcost") + ' (' + WtfGlobal.getCurrencySymbol() + ')',
                dataIndex: "amountcost",
                align: "right",
                width: 100,
                pdfwidth: 100,
                renderer: WtfGlobal.currencyRendererSymbol
            }, {
                header: WtfGlobal.getLocaleText("acc.invoice.grid.amountsales") + ' (' + WtfGlobal.getCurrencySymbol() + ')',
                dataIndex: "amountsales",
                align: "right",
                width: 100,
                pdfwidth: 100,
                renderer: WtfGlobal.currencyRendererSymbol
            }, {
                header: WtfGlobal.getLocaleText("acc.saleByItem.gridProfitMargin") + ' (' + WtfGlobal.getCurrencySymbol() + ')',
                dataIndex: "profitmargin",
                align: "right",
                width: 100,
                pdfwidth: 100,
                renderer: WtfGlobal.currencyRendererSymbol
            }, {
                header: WtfGlobal.getLocaleText("acc.saleByItem.gridPercentageProfitMargin"),
                dataIndex: "percentageprofitmargin",
                align: "center",
                width: 100,
                pdfwidth: 100,
                renderer:this.percentageRenderer.createDelegate(this)
            },
            {
                header: WtfGlobal.getLocaleText("acc.cust.name"), // Customer Name
                dataIndex: 'customername',
                pdfwidth: 100,
                width: 100
            },
            {
                header: WtfGlobal.getLocaleText("acc.MailWin.pomsg7"),
                dataIndex: 'ponumber',
                autoWidth: true,
                width: 100,
                pdfwidth: 100
//                ,
//                renderer: WtfGlobal.linkDeletedRenderer
            },
            {
                header: "Purchase Order Date",
                dataIndex: 'podate',
                width: 100,
                pdfwidth: 100
            },
            {
                header: WtfGlobal.getLocaleText("acc.1099.gridVname"), //"Vendor Name",
                dataIndex: 'vendorname',
                width: 100,
                pdfwidth: 100
            }
        ]
    });

    Wtf.apply(this, {
        items: [{
                region: 'center',
                layout: 'fit',
                border: false,
                items: this.grid
            }],
        tbar: buttonArray,
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.store,
            displayInfo: true,
            searchField: this.quickPanelSearch,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"),
            plugins: this.pP = new Wtf.common.pPageSize({
                id: "pPageSize_" + this.id
            })
        })
    });
    this.grid.on("render", function(grid) {
        WtfGlobal.autoApplyHeaderQtip(grid);
    }, this);
    Wtf.account.CostAndSellingPriceOfItemsToCustomer.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.CostAndSellingPriceOfItemsToCustomer, Wtf.Panel, {
    onRender: function(config) {
        Wtf.account.CostAndSellingPriceOfItemsToCustomer.superclass.onRender.call(this, config);
    },
    fetchData: function() {
        var sDate = this.startDate.getValue();
        var eDate = this.endDate.getValue();
        if (sDate > eDate) {
            WtfComMsgBox(1, 2);
            return;
        }

        this.store.load({
            params: {
                start: 0,
                limit: this.pP.combo.value,
                startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                productid:this.productname.getValue()
            }
        });
    },
    handleResetClickNew: function() {
        this.quickPanelSearch.reset();
        this.startDate.reset();
        this.endDate.reset();
        this.productname.reset();
        this.fetchData();
    },
    percentageRenderer:function(val){
        if(val==="-"){
           return val; 
        } else{
           return'<div class="currency">'+WtfGlobal.conventInDecimal(val,"")+'%</div>';
        }
    }
});



Wtf.CostAndSellingPricehelpDetails = Wtf.extend(Wtf.Component, {
    tplMarkup: ['<div id="fcue-360" class="fcue-outer" style="position: absolute; z-index:2000000; left: 188px; top: 12px;">' +
                '<div class="fcue-inner">' +
                '<div class="fcue-t"></div>' +
                '<div class="fcue-content">' +
                '<a onclick="closeCue();" href="#" id="fcue-close"></a>' +
                '<div class="ft ftnux"><p>' +
                '</p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span>' +
                '<p></p>' +
                '<span id="titledesc">sssdd</span>' +
                '<div id="helpBttnContainerDiv"><p></p>' +
                '</div>' +
                '</div>' +
                '</div>' +
                '</div>' +
                '<div class="fcue-b">' +
                '<div></div>' +
                '</div>' +
                '<div class="fcue-pnt fcue-pnt-t-r">' +
                '</div>' +
                '</div>'],
    id: 'widgethelpdialog',
    initComponent: function(config) {
        Wtf.CostAndSellingPricehelpDetails.superclass.initComponent.call(this, config);
    },
    showHelpWindow: function(x, y, title, desc) {
        if (document.getElementById('fcue-360-mask'))
            document.getElementById('fcue-360-mask').style.display = "block";
        this.tpl = new Wtf.Template(this.tplMarkup[0]);
        this.tpl.append(document.body, {});
        document.getElementById('titlehelp').innerHTML = title;
        document.getElementById('titledesc').innerHTML = desc;
        Wtf.get('fcue-360').setXY([x - 330, y + 30]);
        document.getElementById('fcue-360').style.visibility = "visible";
    }
});