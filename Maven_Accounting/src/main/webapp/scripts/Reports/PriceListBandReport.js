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

function callPriceListBandReportDynamicLoad() {
    var panel = Wtf.getCmp("priceListBandReport");
    if (panel == null) {
        panel = new Wtf.account.priceListBandReport({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.priceListBandReport"), Wtf.TAB_TITLE_LENGTH), // "Price List - Band Report",
            tabTip: WtfGlobal.getLocaleText("acc.field.priceListBandReport"),
            id: "priceListBandReport",
            iconCls: 'accountingbase pricelistreport',
            layout: 'fit',
            closable: true,
            border: false
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.priceListBandReport = function(config) {
    Wtf.apply(this, config);
    
    this.createGrid();
    this.createTBar();
    
    Wtf.account.priceListBandReport.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.priceListBandReport, Wtf.Panel, {
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
                        store: this.Store,
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
        
        Wtf.account.priceListBandReport.superclass.onRender.call(this,config);
    },
    
    createTBar: function() {
        this.btnArr = [];
        this.bBarBtnArr = [];
        
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.priceReport.QuickSearchEmptyText"), // "Search by Product ID, Product Name...",
            width: 200,
            hidden: false,
            field: 'billNo'
        });
        this.btnArr.push(this.quickPanelSearch);
        
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            hidden: false,
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), // 'Allows you to add a new search term by clearing existing search terms.',
            id: 'resetBttn' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.resetBttn.on('click',this.handleResetClick,this);
        this.btnArr.push(this.resetBttn);
        
        var priceTypeStoreDataArr = [];
        priceTypeStoreDataArr.push(['1', 'Sales']);
        priceTypeStoreDataArr.push(['2', 'Purchase']);
        
        this.priceTypeStore = new Wtf.data.SimpleStore({
            fields:[
                {name:'id'},
                {name:'name'}
            ],
            data: priceTypeStoreDataArr
        });
        
        this.priceTypeCombo = new Wtf.form.ComboBox({
            triggerAction: 'all',
            mode: 'local',
            valueField: 'id',
            displayField: 'name',
            store: this.priceTypeStore,
            fieldLabel: WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.PriceTypeCombo"),
            typeAhead: true,
            width: 150,
            forceSelection: true,
            hiddenName: 'type',
            editable: false
        });
        this.priceTypeCombo.setValue('1');
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.PriceTypeCombo"), this.priceTypeCombo);
        
        this.currencyRec = new Wtf.data.Record.create([
            {name: 'currencyid', mapping: 'tocurrencyid'},
            {name: 'symbol'},
            {name: 'currencyname', mapping: 'tocurrency'},
            {name: 'exchangerate'},
            {name: 'htmlcode'}
         ]);
         this.currencyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            },this.currencyRec),
            url:"ACCCurrency/getCurrencyExchange.do"
         });
        
        this.currencyCombo = new Wtf.form.ComboBox({
            allowBlank: false,
            emptyText: WtfGlobal.getLocaleText("acc.field.Selectcurrency"),
            typeAhead: true,
            forceSelection: true,
            triggerAction: 'all',
            id: 'currencyField',
            store: this.currencyStore,
            displayField: "currencyname",
            valueField: 'currencyid',
            mode: 'local',
            editable: false,
            hiddenName: 'currency',
            fieldLabel: WtfGlobal.getLocaleText("acc.field.SelectCurrency"),
            width: 150
        });
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.field.SelectCurrency"), this.currencyCombo);
        this.currencyStore.load();
        this.currencyStore.on('load', function() {
            this.currencyCombo.setValue(WtfGlobal.getCurrencyID());
        }, this);
        
        this.productRec = Wtf.data.Record.create ([
            {name: 'productid'},
            {name: 'pid'},
            {name: 'type'},
            {name: 'productname'},
            {name: 'desc'},
            {name: 'producttype'}
        ]);
   
        this.productStore = new Wtf.data.Store({
            url: "ACCProduct/getProductsForCombo.do",
            baseParams: {
                mode: 22
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productRec)
        });
   
        this.ProductComboconfig = {
            hiddenName: "productid",
            store: this.productStore,
            valueField: 'productid',
            hideLabel: true,
            displayField: 'productname',
            emptyText: WtfGlobal.getLocaleText("acc.msgbox.17"),
            mode: 'local',
            typeAhead: true,
            selectOnFocus: true,
            triggerAction: 'all',
            scope: this
        };

        this.productCombo = new Wtf.common.Select(Wtf.applyIf({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.productList.gridProduct") + '*' ,
            forceSelection: true,
            extraFields: ['pid','type'],
            extraComparisionField: 'pid', // type ahead search on product id as well.
            listWidth: Wtf.ProductComboListWidth,
            width: 150
        },this.ProductComboconfig));
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.field.SelectProduct"), this.productCombo);
        WtfGlobal.setAjaxTimeOut();
        this.productStore.load();
        
        this.productStore.on("load", function(store) {
            WtfGlobal.resetAjaxTimeOut();
            var record = new this.productRec({
                productid: "All",
                pid: "",
                type: "",
                productname: "All Records"
            });
            this.productCombo.store.insert( 0,record);
            this.productCombo.setValue("All");
        }, this);
        
        this.productStore.on("loadexception", function(store) {
            WtfGlobal.resetAjaxTimeOut();
        },this);
        
        this.productCombo.on('select',function(combo,productRec) {
            if (productRec.get('productid') == 'All') {
                combo.clearValue();
                combo.setValue('All');
            } else if (combo.getValue().indexOf('All') >= 0) {
                combo.clearValue();
                combo.setValue(productRec.get('productid'));
            }
        }, this);
        
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
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"),  // 'Export report details',
            filename: WtfGlobal.getLocaleText("acc.field.priceListBandReport") + "_v1",
            disabled: true,
            scope: this,
            menuItem: {
                csv: true,
                pdf: true,
                xls: true
            },
            get: Wtf.autoNum.priceListBandReport
        });
        this.bBarBtnArr.push('-', this.exportButton);
        
        this.exportButton.on("click", function() {
            this.exportButton.setParams({
                productid: this.productCombo.getValue(),
                ss: this.quickPanelSearch.getValue(),
                priceType: this.priceTypeCombo.getValue(),
                currencyid: (this.currencyCombo.getValue() != "") ? this.currencyCombo.getValue() : WtfGlobal.getCurrencyID()
            });
        },this);
        
        this.printButton = new Wtf.exportButton({
            obj: this,
            text: WtfGlobal.getLocaleText("acc.common.print"),
            tooltip: WtfGlobal.getLocaleText("acc.sales.printTT"), // 'Print report details',
            disabled: true,
            filename: WtfGlobal.getLocaleText("acc.field.priceListBandReport"),
            menuItem: {
                print: true
            },
            get: Wtf.autoNum.priceListBandReport
        });
        this.bBarBtnArr.push('-', this.printButton);
        
        this.printButton.on("click", function() {
            this.printButton.setParams({
                productid: this.productCombo.getValue(),
                ss: this.quickPanelSearch.getValue(),
                priceType: this.priceTypeCombo.getValue(),
                currencyid: (this.currencyCombo.getValue() != "") ? this.currencyCombo.getValue() : WtfGlobal.getCurrencyID()
            });
        },this);
    },
    
    createGrid: function() {
        WtfGlobal.resetAjaxTimeOut();
        this.Store = new Wtf.data.Store({
            url: "ACCProductCMN/getPriceListBandReport.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
            
        this.grid = new Wtf.grid.GridPanel({
            layout: 'fit',
            region: "center",
            store: this.Store,
            columns: [],
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: false,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });

        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
        this.Store.on('load', this.handleStoreOnLoad, this);
        this.Store.on('datachanged', this.handleStoreDataChanged, this);
    },
    
    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.fetchStatement();
        }
    },
    
    fetchStatement: function() {
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value
            }
        });
    },
    
    handleStoreBeforeLoad: function() {
        WtfGlobal.resetAjaxTimeOut();
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.productid = this.productCombo.getValue();
        currentBaseParams.ss = this.quickPanelSearch.getValue();
        currentBaseParams.priceType = this.priceTypeCombo.getValue();
        currentBaseParams.currencyid = (this.currencyCombo.getValue() != "") ? this.currencyCombo.getValue() : WtfGlobal.getCurrencyID();
        this.Store.baseParams = currentBaseParams;
        
        this.exportButton.enable();
        this.printButton.enable();
    },
    
    handleStoreOnLoad: function(store) {
        var columns = [];
        WtfGlobal.resetAjaxTimeOut();
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));
            
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if (column.dataIndex != "productID" && column.dataIndex != "productName" && column.dataIndex != "currencycode") {
                column.renderer = WtfGlobal.withoutRateCurrencyDeletedSymbol;
            } else {
                column.renderer = WtfGlobal.deletedRenderer;
            }
            
            columns.push(column);
        });
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();
            
        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        this.quickPanelSearch.StorageChanged(store);
    },
    
    handleStoreDataChanged: function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    }
});