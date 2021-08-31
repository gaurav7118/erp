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

function callMonthlyCommissionOfSalesPersonReportDynamicLoad() {
    var panel = Wtf.getCmp("monthlyCommissionOfSalesPerson");
    if (panel == null) {
        panel = new Wtf.account.monthlyCommissionOfSalesPerson({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.MonthlyCommissionOfSalesPersonReport"), Wtf.TAB_TITLE_LENGTH), // "Monthly Commission of Sales Person Report",
            tabTip: WtfGlobal.getLocaleText("acc.monthlyCommissionOfSalesPersonReport.toolTip"),
            id: "monthlyCommissionOfSalesPerson",
            iconCls: 'accountingbase financialreport',
            layout: 'fit',
            closable: true,
            border: false
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}


Wtf.account.monthlyCommissionOfSalesPerson = function(config) {
    Wtf.apply(this, config);
    this.createTBar();
    this.createGrid();
    
    Wtf.account.monthlyCommissionOfSalesPerson.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.monthlyCommissionOfSalesPerson, Wtf.Panel, {
    onRender: function(config) {
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar: this.btnArr,
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.store,
                    searchField: this.quickPanelSearch,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({
                        id : "pPageSize_"+this.id
                    }),
                    items: this.bBarBtnArr
                })
            }]
        });
        
        this.add(this.leadpan);
        this.fetchStatement();
        
        Wtf.account.monthlyCommissionOfSalesPerson.superclass.onRender.call(this,config);
    },
    
    createTBar: function() {
        this.btnArr = [];
        this.bBarBtnArr = [];
        
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.MonthlyCommissionOfSalesPersonReport.QuickSearchEmptyText"), // "Search by Sales Person, Sales Order No, Invoice No, Customer Name...",
            width: 300,
            hidden: false,
            field: 'salesPersonName'
        });
        this.btnArr.push(this.quickPanelSearch);
        
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            hidden: false,
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), // 'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.resetBttn.on('click',this.handleResetClick,this);
        this.btnArr.push(this.resetBttn);
        
        this.itemRec = Wtf.data.Record.create([
            {name: "id"},
            {name: "name"},
            {name: "modulename"},
            {name: "fieldtype"},
            {name: "parentid"},
            {name: "leaf"},
            {name: "fieldtype"},
            {name: 'level', type: 'int'}
        ]);
        
        this.itemStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 15
            
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.itemRec)
        }); 
    
        this.MSComboconfig = {
            store: this.itemStore,
            valueField: 'id',
            hideLabel: true,
            displayField: 'name',
            mode: 'local',
            typeAhead: true,
            selectOnFocus: true,
            triggerAction: 'all',
            scope: this
        };
    
        this.salesPersonCombo = new Wtf.common.Select(Wtf.applyIf({
            name: 'itemlist',
            multiSelect: true,
            emptyText: WtfGlobal.getLocaleText("acc.field.SelectSalesPerson"), // "Select Sales Person...",
            fieldLabel: WtfGlobal.getLocaleText("acc.masterConfig.15"), // 'Sales Person',
            forceSelection:true,
            listWidth: Wtf.account.companyAccountPref.accountsWithCode?500:400,
            width: 200
        }, this.MSComboconfig));
        this.btnArr.push('-',  WtfGlobal.getLocaleText("acc.field.SelectSalesPerson1"), this.salesPersonCombo);

        this.salesPersonCombo.on('select',function(combo,accRec,index) { // multiselection in case of all 
            if (accRec.get('id') == 'All') { // case of multiple record after all
                combo.clearValue();
                combo.setValue('All');
            } else if (combo.getValue().indexOf('All') >= 0) { // case of all after record
                combo.clearValue();
                combo.setValue(accRec.get('id'));
            }
        }, this);       
    
        this.itemStore.on("load", function() {
            var record1 = new Wtf.data.Record({
                id: "All",
                name: "All"
            });
            this.itemStore.insert(0, record1);
            this.salesPersonCombo.setValue("All");
        }, this);
        this.itemStore.load();
        
        this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), // 'From',
            name: 'startdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: WtfGlobal.getDates(true)
        });
        this.btnArr.push(WtfGlobal.getLocaleText("acc.common.from"), this.startDate);
        
        this.endDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), // 'To',
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'enddate',
            value: WtfGlobal.getDates(false)
        });
        this.btnArr.push(WtfGlobal.getLocaleText("acc.common.to"), this.endDate);
        
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement                        
        });
        this.btnArr.push('-', this.fetchBttn);
        
        this.exportButton = new Wtf.exportButton({
            obj: this,
            id: "exportReports" + this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
            filename: WtfGlobal.getLocaleText("acc.field.MonthlyCommissionOfSalesPersonReport")+"_v1",
            disabled: true,
            scope: this,
            menuItem: {
                csv: true,
                pdf: true,
                xls: true
            },
            get: Wtf.autoNum.MonthlyCommissionOfSalesPerson
        });
        this.bBarBtnArr.push('-', this.exportButton);
        
        this.exportButton.on("click", function() {
            this.exportButton.setParams({
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                itemid: this.salesPersonCombo.getValue()
            });
        },this);
        
        this.printButton = new Wtf.exportButton({
            obj: this,
            text: WtfGlobal.getLocaleText("acc.common.print"),
            tooltip: WtfGlobal.getLocaleText("acc.sales.printTT"), // 'Print report details',
            disabled: true,
            filename: WtfGlobal.getLocaleText("acc.field.MonthlyCommissionOfSalesPersonReport"),
            menuItem: {
                print: true
            },
            get: Wtf.autoNum.MonthlyCommissionOfSalesPerson
        });
        this.bBarBtnArr.push('-', this.printButton);
        
        this.printButton.on("click", function() {
            this.printButton.setParams({
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                itemid: this.salesPersonCombo.getValue()
            });
        },this);
    },
    
    createGrid: function() {
        this.gridRec = Wtf.data.Record.create ([
            {name: 'salesPersonName'},
            {name: 'salesOrderNo'},
            {name: 'salesOrderID'},
            {name: 'invoiceNo'},
            {name: 'doId'},
            {name: 'customerName'},
            {name: 'totalBilledAmount'},
            {name: 'totalCost'},
            {name: 'gp'},
            {name: 'gpPercent'},
            {name: 'Custom_Sales Person 2'},
            {name: 'Custom_Project Manager'},
            {name: 'totalBilledAmountInDoc'},
            {name: 'totalCostInDoc'},
            {name: 'gpInDoc'},
            {name: 'currencysymboltransaction'}
        ]);
        
        this.store = new Wtf.data.Store({
            url: "ACCSalesOrderCMN/getMonthlyCommissionOfSalesPersonReport.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            }, this.gridRec)
        });

        this.gridcm = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header: WtfGlobal.getLocaleText("acc.invoiceList.salesPerson"), // "Sales Person",
                dataIndex: 'salesPersonName',
                renderer: WtfGlobal.deletedRenderer,
                width: 150,
                pdfwidth: 75
            },{
                header:"Sales Person 2", // "Sales Person2.",
                dataIndex: 'Custom_Sales Person 2',
                renderer: WtfGlobal.deletedRenderer,
                width: 150,
                hidden:!(Wtf.account.companyAccountPref.isSBIFlag),
                pdfwidth: 75
            },{
                header:"Project Manager", // "Project manager",
                dataIndex: 'Custom_Project Manager',
                renderer: WtfGlobal.deletedRenderer,
                width: 150,
                hidden:!(Wtf.account.companyAccountPref.isSBIFlag),
                pdfwidth: 75
            },{
                header: WtfGlobal.getLocaleText("acc.field.salesOrderNo"), // "Sales Order No.",
                dataIndex: 'salesOrderNo',
                renderer: WtfGlobal.linkDeletedRenderer,
                width: 150,
                pdfwidth: 75
            },{
                header: WtfGlobal.getLocaleText("acc.invoice.gridInvNo"), // "Invoice No.",
                dataIndex: 'invoiceNo',
                autoSize : true,
                groupable: true,
                renderer: WtfGlobal.multipleDOLinkRenderer,
                width: 150,
                pdfwidth: 75
            },{
                header: WtfGlobal.getLocaleText("acc.cust.name"), // "Customer Name",
                dataIndex: 'customerName',
                renderer: WtfGlobal.deletedRenderer,
                width: 150,
                pdfwidth: 75
            },{
                header: WtfGlobal.getLocaleText("acc.field.totalBilledAmount") + " (" + WtfGlobal.getLocaleText("acc.fixedAssetList.grid.docCur") + ")", // "Total Billed Amount",
                dataIndex: 'totalBilledAmountInDoc',
                align: 'right',
                renderer: WtfGlobal.withoutRateCurrencySymbolTransaction,
                width: 200,
                pdfwidth: 75
            },{
                header: WtfGlobal.getLocaleText("acc.field.totalBilledAmount") + " (" + WtfGlobal.getLocaleText("acc.fixedAssetList.grid.homCur") + ") (" + WtfGlobal.getCurrencyName() + ")", // "Total Billed Amount",
                dataIndex: 'totalBilledAmount',
                align: 'right',
                renderer: WtfGlobal.withoutRateCurrencyDeletedSymbol,
                width: 200,
                pdfwidth: 75
            },{
                header: WtfGlobal.getLocaleText("acc.field.totalCost") + " (" + WtfGlobal.getLocaleText("acc.fixedAssetList.grid.docCur") + ")", // "Total Cost",
                dataIndex: 'totalCostInDoc',
                align: 'right',
                renderer: WtfGlobal.withoutRateCurrencySymbolTransaction,
                width: 200,
                pdfwidth: 75
            },{
                header: WtfGlobal.getLocaleText("acc.field.totalCost") + " (" + WtfGlobal.getLocaleText("acc.fixedAssetList.grid.homCur") + ") (" + WtfGlobal.getCurrencyName() + ")", // "Total Cost",
                dataIndex: 'totalCost',
                align: 'right',
                renderer: WtfGlobal.withoutRateCurrencyDeletedSymbol,
                width: 200,
                pdfwidth: 75
            },{
                header: WtfGlobal.getLocaleText("acc.field.gp") + " (" + WtfGlobal.getLocaleText("acc.fixedAssetList.grid.docCur") + ")", // "GP",
                dataIndex: 'gpInDoc',
                align: 'right',
                renderer: WtfGlobal.withoutRateCurrencySymbolTransaction,
                width: 200,
                pdfwidth: 75
            },{
                header: WtfGlobal.getLocaleText("acc.field.gp") + " (" + WtfGlobal.getLocaleText("acc.fixedAssetList.grid.homCur") + ") (" + WtfGlobal.getCurrencyName() + ")", // "GP",
                dataIndex: 'gp',
                align: 'right',
                renderer: WtfGlobal.withoutRateCurrencyDeletedSymbol,
                width: 200,
                pdfwidth: 75
            },{
                header: "GP %",
                dataIndex: 'gpPercent',
                align: 'right',
                renderer: function(v,m,rec) {
                    v = v + "%";
                    return '<div class="currency">' + v + '</div>';
                },
                width: 100,
                pdfwidth: 75
            }
        ]);
        
        this.summary = new Wtf.ux.grid.GridSummary();
            
        this.grid = new Wtf.grid.GridPanel({
            layout: 'fit',
            region: "center",
            store: this.store,
            cm: this.gridcm,
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: false,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        
        this.grid.on('cellclick',this.onCellClick, this);

        this.store.on('beforeload', function() {
            var currentBaseParams = this.store.baseParams;
            currentBaseParams.itemid = this.salesPersonCombo.getValue();
            currentBaseParams.startdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
            currentBaseParams.enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
            this.store.baseParams = currentBaseParams;
            
            this.exportButton.enable();
            this.printButton.enable();
        }, this);
        
        this.store.on('load', function(store) {
            if (this.store.getCount() < 1) {
                this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.grid.getView().refresh();
            }
            this.quickPanelSearch.StorageChanged(store);
        }, this);
        
        this.store.on('datachanged', function() {
            var p = this.pP.combo.value;
            this.quickPanelSearch.setPage(p);
        }, this);
    },
    
    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
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

        this.store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value
            }
        });
    },
    
    onCellClick: function(g,i,j,e) {
        e.stopEvent();
        var el = e.getTarget("a");
        if (el == null) {
            return;
        } else {
            var formrec;
            var type;
            var withoutinventory;
            var dataindex = g.getColumnModel().getDataIndex(j);
            
            if (dataindex == "salesOrderNo") {
                formrec = g.getStore().getAt(i);
                type = 'Sales Order';
                withoutinventory = false;
                var salesOrderID = formrec.data['salesOrderID'];
                viewTransactionTemplate1(type, formrec, withoutinventory, salesOrderID);            
            } else if (dataindex == "invoiceNo") {
                var invoiceID = g.getStore().getAt(i).data['doId'];
                if (e.target.getAttribute('doId') != undefined && e.target.getAttribute('doId') != "") { // multiple links in single row
                    invoiceID = e.target.getAttribute('doId');
                }
                formrec = g.getStore().getAt(i);
                type = 'Customer Invoice';
                withoutinventory = false;
                viewTransactionTemplate1(type, formrec, withoutinventory, invoiceID);
            }
        }
    }
});