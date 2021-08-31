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

function getSalesCommissionproductDetailTabView() {
    var panel = Wtf.getCmp("SalesCommissionproductDetailReport");
    if (panel == null) {
        panel = new Wtf.account.SalesCommissionproductDetailReport({
            id: "salesCommissionproductDetailReport",
            border: false,
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.viewSalesCommissionProductDetailReport"), Wtf.TAB_TITLE_LENGTH), // "Sales Commission product Detail Report",
            tabTip: WtfGlobal.getLocaleText("acc.field.viewSalesCommissionProductDetailReport"), // "View Sales Commission product Detail Report",
            layout: 'fit',
            closable: true,
            iconCls: 'accountingbase invoicelist'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.SalesCommissionproductDetailReport = function(config) {
    Wtf.apply(this, config);
    
    Wtf.account.SalesCommissionproductDetailReport.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.SalesCommissionproductDetailReport, Wtf.Panel, {
    onRender: function(config) {
        
        this.createBtnArr();
        this.createGrid();
        
        this.salesCommissionDetailStore.load({
            params:{
                start: 0,
                limit: 30
            }
        });
        
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [
                {
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid],
                    tbar: this.tbarArr,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.salesCommissionDetailStore,
                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({
                            id: "pPageSize_"+this.id
                        })
                    })
                }
            ]
        });
        this.add(this.leadpan);
        
        Wtf.account.SalesCommissionproductDetailReport.superclass.onRender.call(this,config);
    },
    
    createGrid: function() {
        this.GridRec = Wtf.data.Record.create([
            {name: 'invoiceID'},
            {name: 'invoiceNo'},
            {name: 'quantity'},
            {name: 'unitprice'},
            {name: 'productName'},
            {name:'total'},
            {name: 'uomname'},
            {name: 'jeNo'},
            {name: 'invoiceDate', type: 'date'},
            {name: 'customer'},
            {name: 'totalInvoiceAmount', type:'float'},
            {name: 'totalCommissionAmount', type:'float'},
            {name: 'totalInvoiceAmountInDoc', type:'float'},
            {name: 'totalCommissionAmountInDoc', type:'float'},
            {name: 'currencyid'},
            {name: 'currencysymbol'},
            {name: 'salesPerson'},
            {name: 'actualInvoiceamount'},
            {name: 'receivedAmount'},
            {name: 'commissionamount'},
            {name: 'paymentNo'},
            {name: 'actualInvoiceamountInDoc'},
            {name: 'receivedAmountInDoc'},
            {name: 'commissionamountInDoc'},
            {name: 'currencysymboltransaction'},
            {name: 'categoryName'},
            {name: 'grandtotalquantity'},
            {name: 'grandcommissionAmount'},
            {name: 'grandcommissionAmountInDoc'},
            {name: 'grandunitprice'},
            {name: 'grandgrouptotal'}
            
            
        ]);
        
        this.salesCommissionDetailStore = new Wtf.ux.grid.MultiGroupingStore({
            url: "ACCInvoiceCMN/getSalesCommissionProductDetailReport.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: "totalCount",
                root: "data"
            }, this.GridRec),
            groupField: ["salesPerson","productName"],
            sortInfo: [{
                field: 'salesPerson', 
                direction: "ASC"
            },{
                field: 'productName', 
                direction: "ASC"
            }]
        });
    
        this.salesCommissionDetailStore.on('beforeload', function() {
            this.salesCommissionDetailStore.baseParams.salesPersonID = this.salesPersonCombo.getValue();
            this.salesCommissionDetailStore.baseParams.productID = this.productCombo.getValue();
            this.salesCommissionDetailStore.baseParams.customerID = this.customerCombo.getValue();
            this.salesCommissionDetailStore.baseParams.startdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
            this.salesCommissionDetailStore.baseParams.enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
            this.salesCommissionDetailStore.baseParams.commissiontype = 4;
//            this.salesCommissionDetailStore.baseParams.isSalesCommissionDetail = true;
        },this);
    
        this.salesCommissionDetailStore.on('load', function(store) {
            if (this.exportButton) {
                this.exportButton.enable();
            }
            if (this.printButton) {
                this.printButton.enable();
            }
            if (this.salesCommissionDetailStore.getCount() < 1) {
                this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.grid.getView().refresh();
            }
            this.quickPanelSearch.StorageChanged(store);
        }, this);
    
        this.salesCommissionDetailStore.on('datachanged', function() {
            var p = this.pP.combo.value;
            this.quickPanelSearch.setPage(p);
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
            this.expandButtonClicked = false;
        }, this);
        
        var colArr = [];
        colArr.push({
                header: WtfGlobal.getLocaleText("acc.invoiceList.salesPerson"), // "Sales Person",
                dataIndex: "salesPerson",
                hidden: true,
                fixed: true,
                pdfwidth: 100
            },{
                header: WtfGlobal.getLocaleText("acc.invoice.gridInvNo"), // "Invoice No.",
                dataIndex: "invoiceNo",
                renderer: WtfGlobal.deletedRenderer,
               
                width: 150,
                pdfwidth: 100
            },
            {
                header: WtfGlobal.getLocaleText("acc.dnList.gridJEno"), // "Journal Entry No",
                dataIndex: "jeNo",
                renderer: WtfGlobal.deletedRenderer,
                width: 150,
                pdfwidth: 100
            },{
                header: WtfGlobal.getLocaleText("acc.agedPay.invoiceDate"), // "Invoice Date",
                dataIndex: "invoiceDate",
                renderer: WtfGlobal.onlyDateDeletedRenderer,
                width: 150,
                pdfwidth: 100
            },{
                header:"Product Name", // "Product Name",
                dataIndex: "productName",
                summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'},
                width: 150,
                pdfwidth: 100
            },{
                header: "Quantity", // "Qty",
                dataIndex: "quantity",
                summaryType: 'sum',
                renderer:this.quantityWithUOMRenderer(this.salesCommissionDetailStore,"quantity","uomname"),
                width: 150,
                pdfwidth: 100
            },{
                header:"Unit Price", // "Unit Price",
                dataIndex: "unitprice",
//                summaryType: 'sum',
                renderer: WtfGlobal.withoutRateCurrencySymbol,
                width: 150,
                pdfwidth: 100
            },
            {
                header:"Total Amount",
                dataIndex: "total",
//                summaryType: 'sum',
                renderer: WtfGlobal.withoutRateCurrencySymbol,
                width: 150,
                pdfwidth: 100
                
            },
            {
                header: WtfGlobal.getLocaleText("acc.invoice.customer"), // "Customer",
                dataIndex: "customer",
                renderer: WtfGlobal.deletedRenderer,
//                summaryRenderer: function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'},
                width: 150,
                pdfwidth: 100
        });
        
        
            colArr.push({
                header: WtfGlobal.getLocaleText("acc.field.TotalInvoiceAmount") + " (" + WtfGlobal.getLocaleText("acc.fixedAssetList.grid.docCur") + ")",
                dataIndex: "totalInvoiceAmountInDoc",
                align: 'right',
//                summaryType: 'sum', SDP-12911 No Summation with Foreign currency required
                hidecurrency: true,
                hidden:true,
                renderer: WtfGlobal.withoutRateCurrencySymbolTransaction,
                width: 220,
                pdfwidth: 150
            },{
                header: WtfGlobal.getLocaleText("acc.field.TotalInvoiceAmount") + " (" + WtfGlobal.getCurrencyName() + ")", // "Total Invoice Amount",
                dataIndex: "totalInvoiceAmount",
                align: 'right',
                summaryType: 'sum',
                hidden:true,
                hidecurrency: true,
                summaryRenderer: function(value, m, rec) {
                    var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                    return retVal;
                },
                renderer: WtfGlobal.currencyDeletedRenderer,
                width: 220,
                pdfwidth: 150
            },{
                header: "Total Commission Amount" + " (" + WtfGlobal.getLocaleText("acc.fixedAssetList.grid.docCur") + ")",
                dataIndex: "totalCommissionAmountInDoc",
                align: 'right',
//                summaryType: 'sum',  SDP-12911 No Summation with Foreign currency required
                hidecurrency: true,
                renderer: WtfGlobal.withoutRateCurrencySymbolTransaction,
                width: 250,
                pdfwidth: 150
            },{
                header: "Total Commission Amount" + " (" + WtfGlobal.getCurrencyName() + ")",
                dataIndex: "totalCommissionAmount",
                align: 'right',
                summaryType: 'sum',
                hidecurrency: true,
                summaryRenderer: function(value, m, rec) {
                    var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                    return retVal;
                },
                renderer: WtfGlobal.currencyDeletedRenderer,
                width: 250,
                pdfwidth: 150
            });
            
        
        var gridSummary = new Wtf.grid.GroupSummary({});
        this.grid = new Wtf.ux.grid.MultiGroupingGrid({
            store: this.salesCommissionDetailStore,
            border: false,
            layout: 'fit',
            cls: 'colWrap',
            view: new  Wtf.ux.grid.MultiGroupingView({
                startCollapsed :true,
                forceFit: false,
                showGroupName: false,
                enableNoGroups: false,
                isGrandTotal: false,
                isGroupTotal: true,
                hideGroupedColumn:false,
             //   emptyText: '<div class="emptyGridText">' + WtfGlobal.getLocaleText('account.common.nodatadisplay') + ' <br></div>',
                emptyText:WtfGlobal.emptyGridRenderer( WtfGlobal.getLocaleText('acc.common.norec') + "<br>" +WtfGlobal.getLocaleText('acc.common.norec.click.fetchbtn')) ,
                groupTextTpl: '{text} : {group}'    
            }),
            plugins: [gridSummary],
            loadMask: true,
            columns: colArr,
            bbar: []
        });
    },
    
    createBtnArr: function() {
        this.tbarArr = [];
        
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.salesCommissionDetailReport.QuickSearchEmptyText"), // "Search by Sales Person, Invoice No., Journal Entry No. ...","
            width: 150,
            field: 'salesPerson'
        });
        this.tbarArr.push(this.quickPanelSearch);
    
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), // 'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), // 'Allows you to add a new search term by clearing existing search terms.',
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.resetBttn.on('click',this.handleResetClick,this);
        this.tbarArr.push(this.resetBttn);
        this.startDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), // 'From',
            name: 'startdate',
            format: WtfGlobal.getOnlyDateFormat(),
//            readOnly: true,
            value: WtfGlobal.getDates(true)
        });
        this.tbarArr.push('-', WtfGlobal.getLocaleText("acc.common.from"), this.startDate);

        this.endDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), // 'To',
            format: WtfGlobal.getOnlyDateFormat(),
//            readOnly: true,
            name: 'enddate',
            value: WtfGlobal.getDates(false)
        });
        this.tbarArr.push(WtfGlobal.getLocaleText("acc.common.to"), this.endDate);
        this.productRec = Wtf.data.Record.create ([
            {name: 'productid'},
            {name: 'pid'},
            {name: 'type'},
            {name: 'productname'},
            {name: 'desc'},
            {name: 'producttype'}
        ]);
        
        this.productStore = new Wtf.data.Store({
//            url: "ACCProduct/getProductsForCombo.do",
            url:"ACCProductCMN/getProductsForDropdownOptimised.do",
            baseParams: {
                mode: 22,
                excludeParent:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productRec)
        });
        this.productStore.load();
          this.ProductComboconfig = {
            hiddenName:"productid",        
            store: this.productStore,
            valueField:'productid',
            displayField:'productname',
            emptyText:WtfGlobal.getLocaleText("acc.msgbox.17"),
            mode: 'remote',
            typeAhead: true,
            selectOnFocus:true,
            isProductCombo: true,//for Product search Start with or Anywhere Match
            triggerAction:'all',
            scope:this
        };
        
    /*
     * Added paging to product combo.
     */
    this.productCombo = new Wtf.common.SelectPaging(Wtf.applyIf({
         multiSelect:true,
         fieldLabel:WtfGlobal.getLocaleText("acc.productList.gridProduct") + '*' ,
         forceSelection:true,
         extraFields:['pid','type'],
         extraComparisionField:'pid',// type ahead search on product id as well.
         listWidth:Wtf.ProductComboListWidth,
         width:100,
    },this.ProductComboconfig));
    
        this.tbarArr.push('-', WtfGlobal.getLocaleText("select product :"), this.productCombo);
        this.personRec = new Wtf.data.Record.create([
            {
                name: 'accid'
            }, {
                name: 'accname'
            }, {
                name: 'acccode'
            },{
                name: 'taxId'
            },{
                name: 'groupname'
            },{
                name: 'hasAccess'
            }
        ]);
           this.customerStore = new Wtf.data.Store({
	   url:"ACCCustomer/getCustomersIdNameForCombo.do",
//            baseParams:{
//                deleted:false,
//                nondeleted:true
//            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.personRec)
        });
        this.customerStore.load();
          this.customComboconfig = {
            hiddenName:"accid",        
            store: this.customerStore,
            valueField:'accid',
            displayField:'accname',
            emptyText:WtfGlobal.getLocaleText("please select customer"),
            mode: 'remote',
            typeAhead: true,
            selectOnFocus:true,
            triggerAction:'all',
            scope:this
        };
    this.customerCombo = new Wtf.common.SelectPaging(Wtf.applyIf({
         multiSelect:true,
         fieldLabel:WtfGlobal.getLocaleText("select a customer") + '*' ,
         forceSelection:true,
         extraFields:['acccode'],
         extraComparisionField:'accname',
         width:100,
    },this.customComboconfig));

    
    this.tbarArr.push('-', WtfGlobal.getLocaleText("Select customer :"), this.customerCombo);
        this.salesPersonStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, Wtf.salesPersonRec),
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 15
            }
        });
        
        this.salesPersonStore.load();
        
        this.salesPersonComboConfig = {
            hiddenName: "salesPerson",         
            store: this.salesPersonStore,
            valueField: 'id',
            hideLabel: true,
            displayField: 'name',
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseselectasalesperson"),
            mode: 'local',
            typeAhead: true,
            selectOnFocus: true,
            triggerAction: 'all',
            scope: this
        };
        
        this.salesPersonCombo = new Wtf.common.Select(Wtf.applyIf({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.invoiceList.salesbyperson"),
            forceSelection: true,
            width: 100
        }, this.salesPersonComboConfig));
        
        this.salesPersonCombo.on('select', function(combo, red) {
            if (red.get('id') == 'All') {
                combo.clearValue();
                combo.setValue('All');
            } else if (combo.getValue().indexOf('All') >= 0) {
                combo.clearValue();
                combo.setValue(red.get('id'));
            }
        } , this);
        
        this.salesPersonStore.on("load", function(store) {
            var record = new Wtf.salesPersonRec({
                id: "All",
                name: "All"
            });
            this.salesPersonCombo.store.insert( 0,record);
            this.salesPersonCombo.setValue("All");
        }, this);
        
        this.tbarArr.push('-', WtfGlobal.getLocaleText("acc.field.SelectSalesPerson1"), this.salesPersonCombo);
    
        
        
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
        this.tbarArr.push(this.fetchBttn);
    
        this.exportButton = new Wtf.exportButton({
            obj: this,
            filename: WtfGlobal.getLocaleText("acc.field.salesCommissionDetailReport")+"_v1",
            id: "exportReports" + this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), // 'Export report details',
            disabled :true,
            hidden:true,
            scope: this,
            menuItem: {
                csv: true,
                pdf: true,
                rowPdf: false,
                xls: true
            },
            params: {
                enddate:  WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue())
            },
            get:Wtf.autoNum.salesCommissionproductDetailReport
        });
        this.tbarArr.push('-', this.exportButton);
        
        this.printButton = new Wtf.exportButton({
            obj: this,
            filename: this.isPaymentTermSalesCommissionDetailReport ? WtfGlobal.getLocaleText("acc.salesComissionSales.ReportPmtTermDetail") : WtfGlobal.getLocaleText("acc.field.salesCommissionDetailReport"),
            text: WtfGlobal.getLocaleText("acc.common.print"),
            tooltip: WtfGlobal.getLocaleText("acc.common.printTT"),
            disabled: true,
            hidden:true,
            menuItem: {
                print: true
            },
            params: {
                startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericDate(this.endDate.getValue())
            },
            get: this.isPaymentTermSalesCommissionDetailReport ? Wtf.autoNum.paymentTermSalesCommissionDetailReport : Wtf.autoNum.salesCommissionDetailReport
        });
        this.tbarArr.push('-', this.printButton);
        
        /*
         * Provided button to expand or collapse all row details. 
         * We display CustomerName,CustomerCode,CashSales/ReceiptNo.,Document Date,Pay in Ref,
         * Amount,Total Amount(In Base Currency)
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
        this.tbarArr.push('-', this.expandCollpseButton);
    },
    quantityWithUOMRenderer:function(store, valueField, displayField) {
        return function(value, meta, record) {
            value=(parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            var uomname = record.data["uomname"] || "";
            return value+" "+uomname;
        }
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
    },
    
    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.fetchStatement();
        }
        if (this.productCombo.getValue()){
            this.productCombo.reset();
            this.fetchStatement();
        }
        
        if(this.customerCombo.getValue()){
            this.customerCombo.reset()
            this.fetchStatement();
        }
           
        
    },
    
    fetchStatement: function() {
        this.sDate = this.startDate.getValue();
        this.eDate = this.endDate.getValue();
        
        if (this.sDate > this.eDate) {
            WtfComMsgBox(1,2);
            return;
        }
        
        var fromdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        var todate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        
        this.salesCommissionDetailStore.load({
            params: {
                startdate: fromdate,
                enddate: todate,
                start: 0,
                limit: this.pP.combo.value,
                ss:this.quickPanelSearch.getValue()
            }
        });
    }
});