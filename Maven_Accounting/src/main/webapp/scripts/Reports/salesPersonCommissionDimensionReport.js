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


function callSalesPersonCommissionDimensionReportDynamicLoad(){
    var panel = Wtf.getCmp("salesPersonCommissionDimensionReport");
    if (panel == null) {
        panel = new Wtf.account.salesPersonCommissionDimensionReport({
            id: 'salesPersonCommissionDimensionReport',
            title: WtfGlobal.getLocaleText("acc.field.salesPersonCommissionDimensionReport"),
            tabTip: WtfGlobal.getLocaleText("acc.field.salesPersonCommissionDimensionReport"),
            topTitle: '<center><font size=4>' + WtfGlobal.getLocaleText("acc.field.salesPersonCommissionDimensionReport") + '</font></center>',
            statementType: 'BalanceSheet',
            border: false,
            closable: true,
            layout: 'fit',
            iconCls: 'accountingbase financialreport'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

//********************************************************************************

Wtf.account.salesPersonCommissionDimensionReport = function(config) {
    Wtf.apply(this, config);
    
    this.createGrid();
    this.createTBar();
    
    Wtf.account.salesPersonCommissionDimensionReport.superclass.constructor.call(this,config);
}

Wtf.extend( Wtf.account.salesPersonCommissionDimensionReport, Wtf.Panel, {
    onRender: function(config) {
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
                tbar: this.btnArr,
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.Store,
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
        
        Wtf.account.salesPersonCommissionDimensionReport.superclass.onRender.call(this,config);
    },
    
    createGrid: function() {
        this.Store = new Wtf.data.Store({
            url: "ACCInvoiceCMN/getSalesPersonCommissionDimensionReport.do",
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
        this.Store.on('loadexception', function() {
            WtfGlobal.resetAjaxTimeOut();
        }, this);
    },
    
    createTBar: function() {
        this.btnArr = [];
        this.bBarBtnArr = [];
        
        this.personRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
        this.salesPersonStore =  new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode:112,
                groupid:15
            },
            reader: new  Wtf.data.KwlJsonReader({
                root: "data"
            },this.personRec)
        });
        
        this.salesPersonStore.on("load", function(store) {
            var storeNewRecord = new this.personRec({
                id: 'All',
                name: 'All'
            });
            this.salesPerson.store.insert(0, storeNewRecord);
            this.salesPerson.setValue("All");
        },this);
        this.salesPersonStore.load();
        
        this.salesPersonComboconfig = {
            store: this.salesPersonStore,
            valueField: 'id',
            hideLabel: true,
            displayField: 'name',
            emptyText: WtfGlobal.getLocaleText("acc.agedPay.customerSelect"),
            mode: 'local',
            typeAhead: true,
            selectOnFocus: true,
            triggerAction: 'all',
            scope: this
        };
        this.salesPerson = new Wtf.common.Select(Wtf.applyIf({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.agedPay.searchcus") + '*' ,
            forceSelection: true,
            width: 240
        }, this.salesPersonComboconfig));
        
        this.salesPerson.on('select', function(combo,personRec) {
            if (personRec.get('id') == 'All') {
                combo.clearValue();
                combo.setValue('All');
            } else if (combo.getValue().indexOf('All') >= 0) {
                combo.clearValue();
                combo.setValue(personRec.get('id'));
            }
        }, this);
        this.btnArr.push(WtfGlobal.getLocaleText("acc.invoiceList.salesPerson"), this.salesPerson);
        
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
        
        this.addDimensionBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("erp.report.SelectDimensions"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("erp.report.SelectDimensionsTT"),
            handler: this.configurAdvancedSearch,
            iconCls: getButtonIconCls(Wtf.etype.add)
        });
        this.btnArr.push('-', this.addDimensionBtn);
        
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.grid.colModel,
            moduleid: 101,
            dimensionBasedComparisionReport: true,
            advSearch: false,
            reportid: Wtf.autoNum.salesPersonCommissionDimensionReport
        });
        
        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
        this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
        
        
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchData                        
        });
        this.btnArr.push('-', this.fetchBttn);
        
        this.exportButton = new Wtf.exportButton({
            obj: this,
            id: "exportReports" + this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"),  // 'Export report details',
            filename: WtfGlobal.getLocaleText("acc.field.salesPersonCommissionDimensionReport")+"_v1",
            disabled: true,
            scope: this,
            menuItem: {
                csv: true,
                pdf: true,
                xls: true
            },
            get: Wtf.autoNum.salesPersonCommissionDimensionReport
        });
        this.bBarBtnArr.push('-', this.exportButton);
        
        this.exportButton.on("click", function() {
            this.exportButton.setParams({
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            });
        },this);
        
        this.printButton = new Wtf.exportButton({
            obj: this,
            text: WtfGlobal.getLocaleText("acc.common.print"),
            tooltip: WtfGlobal.getLocaleText("acc.sales.printTT"), // 'Print report details',
            disabled: true,
            filename: WtfGlobal.getLocaleText("acc.field.salesPersonCommissionDimensionReport"),
            menuItem: {
                print: true
            },
            get: Wtf.autoNum.salesPersonCommissionDimensionReport
        });
        this.bBarBtnArr.push('-', this.printButton);
        
        this.printButton.on("click", function() {
            this.printButton.setParams({
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            });
        },this);
    },
    
    fetchData: function() {
        if (!(this.objsearchComponent.advGrid.searchStore.getCount() > 0)) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),  WtfGlobal.getLocaleText("acc.field.pleseselectdimension")], 3);
        } else {
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
        WtfGlobal.setAjaxTimeOut();
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        currentBaseParams.salesPersonID = this.salesPerson.getValue()
        this.Store.baseParams = currentBaseParams;
        
        this.exportButton.enable();
        this.printButton.enable();
    },
    
    handleStoreOnLoad: function(store) {
        WtfGlobal.resetAjaxTimeOut();
        var columns = [];
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));
            
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if (column.dataIndex != "salesPersonName") {
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
    },
    
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.addDimensionBtn.disable();
        this.doLayout();
    },
    
    filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.Store.baseParams = {
            flag: 1,
            iscustomcolumndata: 0,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_Invoice_ModuleId,
            isFixedAsset: false,
            isLeaseFixedAsset: false,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        
        this.Store.load({
            params: {
                start: 0, 
                limit: this.pP.combo.value
            }
        });
    },
    
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.baseParams = {
            flag: 1,
            iscustomcolumndata: 0,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_Invoice_ModuleId,
            isFixedAsset: false,
            isLeaseFixedAsset: false,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        
        this.Store.load({
            params: {
                start: 0, 
                limit: this.pP.combo.value
            }
        });
        
        this.objsearchComponent.hide();
        this.addDimensionBtn.enable();
        this.doLayout();
    }
});