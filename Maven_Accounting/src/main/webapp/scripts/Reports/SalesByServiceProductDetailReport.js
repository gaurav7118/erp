///*
// * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
// * All rights reserved.
// * 
// * This program is free software; you can redistribute it and/or
// * modify it under the terms of the GNU General Public License
// * as published by the Free Software Foundation; either version 2
// * of the License, or (at your option) any later version.
// * 
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU General Public License
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
// */
//
//
//function callSalesByServiceProductDetailReportDynamicLoad(searchStr, filterAppend) {
//    var panel = Wtf.getCmp("salesByServiceProductDetailReport");
//    if (panel == null) {
//        panel = new Wtf.account.salesByServiceProductDetailReport({
//            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.salesByServiceProductDetailReport"), Wtf.TAB_TITLE_LENGTH), // "Sales by Service Product Detail Report",
//            tabTip: WtfGlobal.getLocaleText("acc.salesByServiceProductDetailReport.toolTip"),
//            id: "salesByServiceProductDetailReport",
//            iconCls: 'accountingbase invoicelist',
//            layout: 'fit',
//            closable: true,
//            border: false
//        });
//        Wtf.getCmp('as').add(panel);
//    }
//    Wtf.getCmp('as').setActiveTab(panel);
//    showAdvanceSearch(panel, searchStr, filterAppend);
//    Wtf.getCmp('as').doLayout();
//}
//
//
//Wtf.account.salesByServiceProductDetailReport = function(config) {
//    this.arr = [];
//    Wtf.apply(this, config);
//    
//    this.createGrid();
//    this.createTBar();
//    
//    Wtf.account.salesByServiceProductDetailReport.superclass.constructor.call(this,config);
//}
//
//Wtf.extend(Wtf.account.salesByServiceProductDetailReport, Wtf.Panel, {
//    onRender: function(config) {
//        this.leadpan = new Wtf.Panel({
//            layout: 'border',
//            border: false,
//            attachDetailTrigger: true,
//            items: [this.objsearchComponent, 
//                {
//                    region: 'center',
//                    layout: 'fit',
//                    border: false,
//                    items: [this.grid],
//                    tbar: this.btnArr,
//                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
//                        pageSize: 30,
//                        id: "pagingtoolbar" + this.id,
//                        store: this.Store,
//                        searchField: this.quickPanelSearch,
//                        displayInfo: true,
//                        emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
//                        plugins: this.pP = new Wtf.common.pPageSize({
//                            id : "pPageSize_"+this.id
//                        }),
//                        items: this.bBarBtnArr
//                    })
//                }]
//        });
//        
//        this.add(this.leadpan);
//        this.fetchStatement();
//        
//        Wtf.account.salesByServiceProductDetailReport.superclass.onRender.call(this,config);
//    },
//    
//    createTBar: function() {
//        this.btnArr = [];
//        this.bBarBtnArr = [];
//        
//        this.quickPanelSearch = new Wtf.KWLTagSearch({
//            emptyText: WtfGlobal.getLocaleText("acc.salesByServiceProductDetailReport.QuickSearchEmptyText"), // "Search by Bill No, File Ref, Name...",
//            width: 200,
//            hidden: false,
//            field: 'billNo'
//        });
//        this.btnArr.push(this.quickPanelSearch);
//        
//        this.resetBttn = new Wtf.Toolbar.Button({
//            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
//            hidden: false,
//            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), // 'Allows you to add a new search term by clearing existing search terms.',
//            id: 'btnRec' + this.id,
//            scope: this,
//            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
//            disabled: false
//        });
//        this.resetBttn.on('click',this.handleResetClick,this);
//        this.btnArr.push(this.resetBttn);
//        
//        this.startDate = new Wtf.ExDateFieldQtip({
//            fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), // 'From',
//            name: 'startdate',
//            format: WtfGlobal.getOnlyDateFormat(),
//            value: WtfGlobal.getDates(true)
//        });
//        this.btnArr.push(WtfGlobal.getLocaleText("acc.common.from"), this.startDate);
//        
//        this.endDate = new Wtf.ExDateFieldQtip({
//            fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), // 'To',
//            format: WtfGlobal.getOnlyDateFormat(),
//            name: 'enddate',
//            value: WtfGlobal.getDates(false)
//        });
//        this.btnArr.push(WtfGlobal.getLocaleText("acc.common.to"), this.endDate);
//        
//        this.fetchBttn = new Wtf.Toolbar.Button({
//            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
//            tooltip: WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"), // "Select a time period to view corresponding transactions.",
//            style: "margin-left: 6px;",
//            iconCls: 'accountingbase fetch',
//            scope: this,
//            handler: this.fetchStatement                        
//        });
//        this.btnArr.push('-', this.fetchBttn);
//        
//        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
//            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), // "Advanced Search",
//            scope: this,
//            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), // 'Search for multiple terms in multiple fields.',
//            handler: this.configurAdvancedSearch,
//            iconCls: "advanceSearchButton"
//        });
//        this.btnArr.push('-', this.AdvanceSearchBtn);
//        
//        this.objsearchComponent = new Wtf.advancedSearchComponent({
//            cm: this.grid.colModel,
//            moduleid: Wtf.Acc_Invoice_ModuleId,
//            advSearch: false,
//            parentPanelSearch: this,
////            isOnlyGlobalCustomColumn: true,
//            customerCustomFieldFlag: true,
//            reportid: Wtf.autoNum.SalesByServiceProductDetailReport,
//            ignoreDefaultFields:true
//        });
//        
//        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
//        this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
//        
//        this.customReportViewBtn = new Wtf.Toolbar.Button({
//            text: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
//            scope: this,
//            tooltip: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
//            handler: this.customizeView,
//            iconCls: 'accountingbase fetch'
//        });
//        this.btnArr.push('->', this.customReportViewBtn);
//        
//        this.exportButton = new Wtf.exportButton({
//            obj: this,
//            id: "exportReports" + this.id,
//            text: WtfGlobal.getLocaleText("acc.common.export"),
//            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"),  // 'Export report details',
//            filename: WtfGlobal.getLocaleText("acc.field.salesByServiceProductDetailReport")+"_v1",
//            disabled: true,
//            scope: this,
//            menuItem: {
//                csv: true,
//                pdf: true,
//                xls: true
//            },
//            get: Wtf.autoNum.SalesByServiceProductDetailReport
//        });
//        this.bBarBtnArr.push('-', this.exportButton);
//        
//        this.exportButton.on("click", function() {
//            this.exportButton.setParams({
//                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
//                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
//            });
//        },this);
//        
//        this.printButton = new Wtf.exportButton({
//            obj: this,
//            text: WtfGlobal.getLocaleText("acc.common.print"),
//            tooltip: WtfGlobal.getLocaleText("acc.sales.printTT"), // 'Print report details',
//            disabled: true,
//            filename: WtfGlobal.getLocaleText("acc.field.salesByServiceProductDetailReport"),
//            menuItem: {
//                print: true
//            },
//            get: Wtf.autoNum.SalesByServiceProductDetailReport
//        });
//        this.bBarBtnArr.push('-', this.printButton);
//        
//        this.printButton.on("click", function() {
//            this.printButton.setParams({
//                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
//                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
//            });
//        },this);
//    },
//    
//    createGrid: function() {
//        this.Store = new Wtf.data.Store({
//            url: "ACCInvoiceCMN/getSalesByServiceProductDetailReport.do",
//            reader: new Wtf.data.KwlJsonReader({
//                root: "data",
//                totalProperty: "totalCount"
//            })
//        });
//            
//        this.grid = new Wtf.grid.GridPanel({
//            layout: 'fit',
//            region: "center",
//            store: this.Store,
//            columns: [],
//            border: false,
//            loadMask: true,
//            viewConfig: {
//                forceFit: false,
//                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
//            }
//        });
//
//        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
//        this.Store.on('load', this.handleStoreOnLoad, this);
//        this.Store.on('datachanged', this.handleStoreDataChanged, this);
//    },
//    
//    handleResetClick: function() {
//        if (this.quickPanelSearch.getValue()) {
//            this.quickPanelSearch.reset();
//            this.fetchStatement();
//        }
//    },
//    
//    fetchStatement: function() {
//        this.sDate = this.startDate.getValue();
//        this.eDate = this.endDate.getValue();
//        
//        if (this.sDate > this.eDate) {
//            WtfComMsgBox(1,2);
//            return;
//        }
//
//        this.Store.load({
//            params: {
//                start: 0,
//                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value
//            }
//        });
//    },
//    
//    handleStoreBeforeLoad: function() {
//        var currentBaseParams = this.Store.baseParams;
//        currentBaseParams.startdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
//        currentBaseParams.enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
//        currentBaseParams.reportId = Wtf.autoNum.SalesByServiceProductDetailReport
//        this.Store.baseParams = currentBaseParams;
//        
//        this.exportButton.enable();
//        this.printButton.enable();
//    },
//    
//    handleStoreOnLoad: function(store) {
//        var columns = [];
//        columns.push(new Wtf.grid.RowNumberer({
//            width: 30
//        }));
//            
//        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
//            if (column.dataIndex == "billDate") {
//                column.renderer = WtfGlobal.onlyDateDeletedRenderer;
//            } else if (column.dataIndex != "billNo" && column.dataIndex != "fileRef" && column.dataIndex != "name" && column.dataIndex != "currencycode" && column.custom != "true"  && column.dataIndex != "amountinbase") {
//                column.renderer = WtfGlobal.withoutRateCurrencyDeletedSymbol;
//            } else if( column.dataIndex == "amountinbase"){
//                column.renderer = WtfGlobal.currencyDeletedRenderer; 
//            }
//            columns.push(column);
//        });
//        this.grid.getColumnModel().setConfig(columns);
//        this.grid.getView().refresh();
//            
//        if (this.Store.getCount() < 1) {
//            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
//            this.grid.getView().refresh();
//        }
//        this.quickPanelSearch.StorageChanged(store);
//    },
//    
//    handleStoreDataChanged: function() {
//        var p = this.pP.combo.value;
//        this.quickPanelSearch.setPage(p);
//    },
//    
//    configurAdvancedSearch: function() {
//        this.objsearchComponent.show();
//        this.objsearchComponent.advGrid.advSearch = true;
//        this.objsearchComponent.advGrid.getComboData();
//        this.AdvanceSearchBtn.disable();
//        this.doLayout();
//    },
//    
//    filterStore: function(json, filterConjuctionCriteria) {
//        this.searchJson = json;
//        this.filterConjuctionCrit = filterConjuctionCriteria;
//        this.Store.baseParams = {
//            flag: 1,
//            iscustomcolumndata: 0,
//            searchJson: this.searchJson,
//            moduleid: Wtf.Acc_Invoice_ModuleId,
//            isFixedAsset: false,
//            isLeaseFixedAsset: false,
//            filterConjuctionCriteria: filterConjuctionCriteria
//        }
//        
//        this.Store.load({
//            params: {
//                ss: this.quickPanelSearch.getValue(), 
//                start: 0, 
//                limit: this.pP.combo.value
//            }
//        });
//    },
//    
//    clearStoreFilter: function() {
//        this.searchJson = "";
//        this.filterConjuctionCrit = "";
//        this.Store.baseParams = {
//            flag: 1,
//            iscustomcolumndata: 0,
//            searchJson: this.searchJson,
//            moduleid: Wtf.Acc_Invoice_ModuleId,
//            isFixedAsset: false,
//            isLeaseFixedAsset: false,
//            filterConjuctionCriteria: this.filterConjuctionCrit
//        }
//        
//        this.Store.load({
//            params: {
//                ss: this.quickPanelSearch.getValue(), 
//                start: 0, 
//                limit: this.pP.combo.value
//            }
//        });
//        
//        this.objsearchComponent.hide();
//        this.AdvanceSearchBtn.enable();
//        this.doLayout();
//    },
//    
//    customizeView: function() {
//        this.customizeViewWin = new Wtf.CustomizeReportView({
//            title: WtfGlobal.getLocaleText("acc.field.CreateCustomizeView"),
//            parentPanel: this,
//            iconCls: getButtonIconCls(Wtf.etype.deskera),
//            grid: this.grid,
//            reportId: Wtf.autoNum.SalesByServiceProductDetailReport,
//            modules:'2,25'
//        });
//        this.customizeViewWin.show();
//    }
//});