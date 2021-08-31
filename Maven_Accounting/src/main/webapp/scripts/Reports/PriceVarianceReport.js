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

function callPriceVarianceReportDynamicLoad() {
    var panel = Wtf.getCmp("priceVarianceReport");
    if (panel == null) {
        panel = new Wtf.account.priceVarianceReport({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.priceVarianceReport"), Wtf.TAB_TITLE_LENGTH), // "Price Variance Report",
            tabTip: WtfGlobal.getLocaleText("acc.field.priceVarianceReport"),
            id: "priceVarianceReport",
            iconCls: 'accountingbase receivepayment',
            layout: 'fit',
            closable: true,
            border: false
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.priceVarianceReport = function(config) {
    this.arr = [];
    Wtf.apply(this, config);
    
    this.createGrid();
    this.createTBar();
    
    Wtf.account.priceVarianceReport.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.priceVarianceReport, Wtf.Panel, {
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
        
        Wtf.account.priceVarianceReport.superclass.onRender.call(this,config);
    },
    
    createTBar: function() {
        this.btnArr = [];
        this.bBarBtnArr = [];
        
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.priceVarianceReport.QuickSearchEmptyText"), // "Search by Bill No, File Ref, Name...",
            width: 200,
            hidden: false,
            field: 'billNo'
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
        
        this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), // 'From',
            name: 'startdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: WtfGlobal.getDates(true)
        });
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.common.from"), this.startDate);
        
        this.endDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), // 'To',
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'enddate',
            value: WtfGlobal.getDates(false)
        });
        this.btnArr.push(WtfGlobal.getLocaleText("acc.common.to"), this.endDate);
        
        this.moduleStore = new Wtf.data.SimpleStore({
            fields: [{name:'id', type:'int'}, 'name'],
            data :[[Wtf.Acc_Invoice_ModuleId,'Sales Invoice'], [Wtf.Acc_Sales_Order_ModuleId,'Sales Order']]
        });
        this.modules = new Wtf.form.ComboBox({
            store: this.moduleStore,
            width: 100,
            name: 'modules',
            displayField: 'name',
            valueField: 'id',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus: true,
            editable: false
        });
        this.modules.setValue(Wtf.Acc_Invoice_ModuleId);
        this.btnArr.push('-', this.modules);
        
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
            hidden: this.iscustreport,
            displayField: 'productname',
            emptyText: WtfGlobal.getLocaleText("acc.msgbox.17"),
            mode: 'local',
            typeAhead: true,
            selectOnFocus: true,
            triggerAction: 'all',
            scope: this
        };

        this.productname = new Wtf.common.Select(Wtf.applyIf({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.productList.gridProduct") + '*' ,
            forceSelection: true,
            extraFields: ['pid','type'],
            extraComparisionField: 'pid', // type ahead search on product id as well.
            listWidth: Wtf.ProductComboListWidth,
            width: 240
        },this.ProductComboconfig));
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.field.SelectProduct"), this.productname);
        this.productStore.load();
        this.productStore.on("load", function(store) {
            WtfGlobal.resetAjaxTimeOut();
            var record = new this.productRec({
                productid: "All",
                pid: "",
                type: "",
                productname: "All Products"
            });
            this.productname.store.insert( 0,record);
            this.productname.setValue("All");
        }, this);
        
        this.productStore.on("loadexception", function(store) {
            WtfGlobal.resetAjaxTimeOut();
        },this);
        
        this.productname.on('select',function(combo,productRec) {
            if (productRec.get('productid') == 'All') {
                combo.clearValue();
                combo.setValue('All');
            } else if (combo.getValue().indexOf('All') >= 0) {
                combo.clearValue();
                combo.setValue(productRec.get('productid'));
            }
        }, this);
   
        this.productCategoryRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
        this.productCategoryStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 19
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.productCategoryRec)
        });
        this.productCategory = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.masterConfig.19"),
            hiddenName: 'id',
            name: 'id',
            hidden: this.iscustreport,
            store: this.productCategoryStore,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            typeAhead: true,
            triggerAction: 'all',
            hideLabel: true,
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseselectaproductcategory"),
            width: 100,
            listWidth: 150
        });
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.cust.Productcategory"), this.productCategory);
        this.productCategoryStore.load();
        this.productCategoryStore.on("load", function() {
            var record = new Wtf.data.Record({
                id: "",
                name: "All Records"
            });
            this.productCategoryStore.insert(0, record);
            this.productCategory.setValue("");
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
            filename: WtfGlobal.getLocaleText("acc.field.priceVarianceReport")+"_v1",
            disabled: true,
            scope: this,
            menuItem: {
                csv: true,
                pdf: true,
                xls: true
            },
            get: Wtf.autoNum.priceVarianceReport
        });
        this.bBarBtnArr.push('-', this.exportButton);
        
        this.exportButton.on("click", function() {
            this.exportButton.setParams({
                startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                moduleid: this.modules.getValue()
            });
        },this);
        
        this.printButton = new Wtf.exportButton({
            obj: this,
            text: WtfGlobal.getLocaleText("acc.common.print"),
            tooltip: WtfGlobal.getLocaleText("acc.sales.printTT"), // 'Print report details',
            disabled: true,
            filename: WtfGlobal.getLocaleText("acc.field.priceVarianceReport"),
            menuItem: {
                print: true
            },
            get: Wtf.autoNum.priceVarianceReport
        });
        this.bBarBtnArr.push('-', this.printButton);
        
        this.printButton.on("click", function() {
            this.printButton.setParams({
                startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                moduleid: this.modules.getValue()
            });
        },this);
    },
    
    createGrid: function() {
        this.Store = new Wtf.data.Store({
            url: "ACCInvoiceCMN/getPriceVarianceReport.do",
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
            tbar: [WtfGlobal.getLocaleText("acc.priceVarianceReport.Note")],
            viewConfig: {
                forceFit: false,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });

        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
        this.Store.on('load', this.handleStoreOnLoad, this);
        this.Store.on('datachanged', this.handleStoreDataChanged, this);
    },
    
    percentageRenderer: function(v,m,rec) {
        v = parseFloat(v).toFixed(2);
        v = v + "%";
        return v;
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

        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value
            }
        });
    },
    
    handleStoreBeforeLoad: function() {
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        currentBaseParams.moduleid = this.modules.getValue();
        currentBaseParams.productCategoryid = this.productCategory.getValue();
        currentBaseParams.productid = this.productname.getValue();
        this.Store.baseParams = currentBaseParams;
        
        this.exportButton.enable();
        this.printButton.enable();
    },
    
    handleStoreOnLoad: function(store) {
        var columns = [];
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));
            
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if (column.dataIndex == "billDate") {
                column.renderer = WtfGlobal.onlyDateDeletedRenderer;
            } else if (column.dataIndex == "quantity") {
                column.renderer = WtfGlobal.quantityRenderer;
            } else if (column.dataIndex == "actualVariancePercentage" || column.dataIndex == "configuredVariancePercentage") {
                column.renderer = WtfGlobal.percentageRenderer;
            } else if (column.dataIndex != "custName" && column.dataIndex != "salesPersonName" && column.dataIndex != "productName" && column.dataIndex != "productDesc" && column.dataIndex != "billNo" && column.dataIndex != "currencycode" && column.custom != "true") {
                column.renderer = WtfGlobal.withoutRateCurrencyDeletedSymbol;
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