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

Wtf.account.BudgetVsCostReport = function (config) {
    this.BudgetVsCostReportRec = new Wtf.data.Record.create([
        {name: 'dimension'},
        {name: 'budget'},
        {name: 'projectcostsales'},
        {name: 'projectcostpurchase'},
        {name: 'actualcost'},
        {name: 'billing'},
        {name: 'projectedfee'},
        {name: 'earnedfee'},
        {name: 'currencySymbol'}
    ]);
    this.expButton = new Wtf.exportButton({
        text: WtfGlobal.getLocaleText("acc.common.export"),
        obj: this,
        filename: "Budget Vs Cost Report_v1",
        tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"),
        disabled:true,
        params: {
            stdate: WtfGlobal.convertToGenericStartDate(this.getDates(true)),
            enddate: WtfGlobal.convertToGenericEndDate(this.getDates(false)),
            name: "Budget Vs Cost Report_v1"
        },
        menuItem: {csv: true, pdf: true, rowPdf: false, xls: true},
        get: 1333
    });
    this.expButton.on("click", function () {
//        this.expButton.setParams({
//            name: "BudgetVsCost_v1",
//            globalDimention: this.globalDimension.getValue(),
//            lineDimension: this.lineDimension.getValue(),
//            fieldlabelline : this.globalDimension.getRawValue(),
//            globalDimensioncombodataname: this.globalDimension.getRawValue(),
//            fieldlabelglobal: this.globalDimension.getRawValue()
//        });
        this.expButton.setParams(this.BudgetVsCostReportStore.baseParams);
    }, this);
    this.printButton = new Wtf.exportButton({
        text: WtfGlobal.getLocaleText("acc.common.print"), //"Print",
        obj: this,
        tooltip: WtfGlobal.getLocaleText("acc.common.printTT"), //"Print Report details.",   
        menuItem: {print: true},
        get: 1333,
        label: WtfGlobal.getLocaleText("acc.ccReport.tab3"),
        disabled:true
    });
    this.printButton.on("click", function () {
        this.printButton.setParams({
            name: "BudgetVsCost_v1",
            globalDimention: this.globalDimension.getValue(),
            lineDimension: this.lineDimension.getValue(),
        });
    }, this);
    this.BudgetVsCostReportStore = new Wtf.data.GroupingStore({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'count'
        }, this.BudgetVsCostReportRec),
        url: "ACCReports/getBudgetVsCostReport.do"
    });
    
    this.BudgetVsCostReportStore.on('beforeload', function (s, o) {// add Request param before request.
        var fieldlabelline = '';
        var lineDimensionRec = WtfGlobal.searchRecord(this.lineDimensionStore, this.lineDimension.getValue(), 'fieldid');
        if (lineDimensionRec != undefined && lineDimensionRec.data != undefined) {
            fieldlabelline = lineDimensionRec.data.fieldlabel;
            var columns = this.getConfigureColumnModel(lineDimensionRec);
            this.grid.reconfigure(this.BudgetVsCostReportStore, columns);
            this.grid.getView().refresh();
        }
        var fieldlabelglobal = '';
        var globalDimensionRec = WtfGlobal.searchRecord(this.globalDimensionStore, this.globalDimension.getValue(), 'fieldid');
        if (globalDimensionRec != undefined && globalDimensionRec.data != undefined) {
            fieldlabelglobal = globalDimensionRec.data.fieldlabel;
        }
        
        var currentBaseParams = this.BudgetVsCostReportStore.baseParams;
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        this.BudgetVsCostReportStore.baseParams = currentBaseParams;
        currentBaseParams.lineleveldimension = this.lineDimension.getValue();
        currentBaseParams.globaldimension = this.globalDimension.getValue();
        currentBaseParams.startDate = this.startDate.getValue().format('Y-m-d');
        currentBaseParams.endDate = this.endDate.getValue().format('Y-m-d');
        currentBaseParams.fieldlabelline = fieldlabelline;
        currentBaseParams.fieldlabelglobal = fieldlabelglobal;
        currentBaseParams.globalDimensioncombodataid= this.globalDimensionComboData.getValue();
        currentBaseParams.globalDimensioncombodataname= this.globalDimensionComboData.getRawValue();
        this.BudgetVsCostReportStore.baseParams = currentBaseParams;
    }, this);

    this.rowNo = new Wtf.grid.RowNumberer({width: 35});
    this.gridcm = this.getConfigureColumnModel();
    
    // Grid Configuration
    this.grid = new Wtf.grid.GridPanel({
        stripeRows: true,
        store: this.BudgetVsCostReportStore,
        cm: this.gridcm,
        border: false,
        id: "BudgetVsCostReport",
        loadMask: true,
        bbar:['-',this.expButton,'-',this.printButton],
//        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
//            pageSize: 30,
//            id: "pagingtoolbar" + this.id,
//            store: this.BudgetVsCostReportStore,
//            displayInfo: true,
//            autoWidth: true,
//            displayMsg: 'Displaying records {0} - {1} of {2}',
//            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
//            plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id}),
//            items: ['-',this.expButton,'-',this.printButton]
//        }),
        viewConfig:{
            emptyText:'<div style="font-size:15px; text-align:center; color:#808080; font-weight:bold; margin-top:1%;">'+ WtfGlobal.getLocaleText("acc.report.budgetvscost.selectRectoLoad")+'</div>',
            forcefit : false
        }
    });
    this.grid.on("render", function () {
        this.grid.getView().applyEmptyText();
    }, this);
    this.BudgetVsCostReportStore.on("beforeload", function (store) {
        WtfGlobal.setAjaxTimeOutFor30Minutes();
    }, this);
    this.BudgetVsCostReportStore.on("loadexception", function (store) {
        WtfGlobal.resetAjaxTimeOut();
    }, this);
    this.BudgetVsCostReportStore.on("load", function (store) {
        WtfGlobal.resetAjaxTimeOut();
        if (store.getCount() == 0) {
            this.grid.getView().emptyText = '<div style="font-size:15px; text-align:center; color:#808080; font-weight:bold; margin-top:1%;">'+ WtfGlobal.getLocaleText("acc.report.budgetvscost.selectRectoLoad")+'</div>';
            this.grid.getView().refresh();
            this.expButton.setDisabled(true);
            this.printButton.setDisabled(true);
        }else{
            this.expButton.setDisabled(false);
            this.printButton.setDisabled(false);
        }
    }, this);
    this.startDate = new Wtf.ExDateFieldQtip({
        fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), //'From',
        name: 'stdate',
        format: WtfGlobal.getOnlyDateFormat(),
        value: this.getDates(true)
    });
    this.endDate = new Wtf.ExDateFieldQtip({
        fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), //'To',
        format: WtfGlobal.getOnlyDateFormat(),
        name: 'enddate',
        value: this.getDates(false)
    });
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: config.moduleid,
        advSearch: false
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    this.globalDimensionRec = new Wtf.data.Record.create([
        {name: "fieldid"},
        {name: "fieldlabel"},
        {name: "column_number"},
        {name: "refcolumn_number"}
    ]);

    this.globalDimension = new Wtf.data.KwlJsonReader({
        root: "data"
    }, this.globalDimensionRec);

    this.globalDimensionStore = new Wtf.data.Store({
        url: "ACCAccountCMN/getFieldParams.do",
        reader: this.globalDimension,
        baseParams: {
            ignoreDefaultFields: true,
            isActivated: 1,
            isAdvanceSearch: false,
            isAvoidRedundent: true,
            iscustomdimension: true,
            iscustomcolumn: false
        }
    });
    this.globalDimensionStore.load();
    this.globalDimension = new Wtf.form.ComboBox({//All/Balance Sheet/Profit & Loss
        store: this.globalDimensionStore,
        name: 'typeid',
        displayField: 'fieldlabel',
        valueField: 'fieldid',
        mode: 'local',
        width: 150,
        listWidth: 150,
        emptyText: WtfGlobal.getLocaleText("acc.report.budgetvscost.selectGblDim"),//"Select Global Dimension",
        triggerAction: 'all',
        typeAhead: true,
        selectOnFocus: true
    });

    this.globalDimensioncombodataRec = new Wtf.data.Record.create([
        {name: "id"},
        {name: "name"}
    ]);

    this.globalDimensioncombodata = new Wtf.data.KwlJsonReader({
        root: "data"
    }, this.globalDimensioncombodataRec);

    this.globalDimensioncombodataStore = new Wtf.data.Store({
        url: "ACCMaster/getMasterItemsForCustomFoHire.do",
        reader: this.globalDimensioncombodata,
        baseParams: {
            mode: 112
        }
    });

    this.globalDimensionComboData = new Wtf.form.ComboBox({//All/Balance Sheet/Profit & Loss
        store: this.globalDimensioncombodataStore,
        name: 'typeid',
        displayField: 'name',
        valueField: 'id',
        mode: 'local',
        width: 150,
        listWidth: 150,
        emptyText: WtfGlobal.getLocaleText("acc.report.budgetvscost.selectGblDimCombodata"),//"Select Global Dimension Combo Data",
        triggerAction: 'all',
        typeAhead: true,
        selectOnFocus: true
    });

    this.lineDimensionRec = new Wtf.data.Record.create([
        {name: "fieldid"},
        {name: "fieldlabel"},
        {name: "column_number"},
        {name: "refcolumn_number"}
    ]);

    this.linelDimensionReder = new Wtf.data.KwlJsonReader({
        root: "data"
    }, this.lineDimensionRec);

    this.lineDimensionStore = new Wtf.data.Store({
        url: "ACCAccountCMN/getFieldParams.do",
        reader: this.linelDimensionReder,
        baseParams: {
            ignoreDefaultFields: true,
            isActivated: 1,
            isAdvanceSearch: false,
            isAvoidRedundent: true,
            iscustomdimension: true,
            iscustomcolumn: true
        }
    });
    this.lineDimensionStore.load();
    this.lineDimension = new Wtf.form.ComboBox({
        labelSeparator: '',
        labelWidth: 0,
        triggerAction: 'all',
        mode: 'local',
        valueField: 'fieldid',
        displayField: 'fieldlabel',
        store: this.lineDimensionStore,
        value: true,
        width: 150,
        emptyText: WtfGlobal.getLocaleText("acc.report.budgetvscost.selectlineDim"),//"Select Line Dimension",
        name: 'lineDimension',
        hiddenName: 'lineDimension'
    });
    this.globalDimension.on('select', function (a, b) {
        this.globalDimensionComboData.reset();
        var fieldLabelGlobal = "";
        var idGlobal = "";
        var globalDimensionRec = WtfGlobal.searchRecord(this.globalDimensionStore, this.globalDimension.getValue(), 'fieldid');
        if (globalDimensionRec != undefined && globalDimensionRec.data != undefined) {
            fieldLabelGlobal = globalDimensionRec.data.fieldlabel;
            idGlobal = globalDimensionRec.data.fieldid;
        }
        this.globalDimensioncombodataStore.load({
            params: {
                fieldlabel: fieldLabelGlobal,
                groupid: idGlobal
            }
        });
    }, this);
    this.fetch = new Wtf.Toolbar.Button({
        scope: this,
        text: WtfGlobal.getLocaleText("acc.common.fetch"),//"Fetch",
        iconCls: "accountingbase fetch",
        handler: function () {
            this.BudgetVsCostReportStore.load();
        }
    });
    this.reset = new Wtf.Toolbar.Button({
        scope: this,
        text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        handler: function () {
            this.startDate.setValue(this.getDates(true));
            this.endDate.setValue(this.getDates(false));
            this.lineDimension.reset();
            this.globalDimension.reset();
            this.globalDimensionComboData.reset();
            var columns = this.getConfigureColumnModel();
            this.grid.reconfigure(this.BudgetVsCostReportStore, columns);
            this.grid.getView().refresh();
            this.BudgetVsCostReportStore.removeAll();
            this.BudgetVsCostReportStore.load();
        }
    });
    var btnArr = [];
    btnArr.push( WtfGlobal.getLocaleText("acc.common.from"), this.startDate, WtfGlobal.getLocaleText("acc.common.to"), this.endDate, '-');
    btnArr.push(WtfGlobal.getLocaleText("acc.report.budgetvscost.globDim"), ':', this.globalDimension, '-', 
    WtfGlobal.getLocaleText("acc.masterConfig.mi"), ":", this.globalDimensionComboData, '-', WtfGlobal.getLocaleText("acc.report.budgetvscost.lineDim"), ':', this.lineDimension, '-');
    btnArr.push(this.fetch, "-",this.reset);
    var firstbar = new Wtf.Toolbar(btnArr);
    this.toolbarPanel = new Wtf.Panel({
        items: [firstbar]
    });

    Wtf.apply(this, {
        items: [{
                layout: 'border',
                border: false,
                scope: this,
                items: [this.objsearchComponent, {
                        region: 'center',
                        layout: 'fit',
                        border: false,
                        items: this.grid
                    }],
                tbar: this.toolbarPanel
            }]
    }, config)

    Wtf.account.BudgetVsCostReport.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.BudgetVsCostReport, Wtf.Panel, {
    onRender: function (config) {
        Wtf.account.BudgetVsCostReport.superclass.onRender.call(this, config);
    },
    getDates: function (start) {
        var d = new Date();
        var monthDateStr = d.format('M d');
        if (Wtf.account.companyAccountPref.fyfrom)
            monthDateStr = Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd = new Date(monthDateStr + ', ' + d.getFullYear() + ' 12:00:00 AM');
        if (d < fd)
            fd = new Date(monthDateStr + ', ' + (d.getFullYear() - 1) + ' 12:00:00 AM');
        if (start)
            return fd;

        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    showAdvanceSearch: function () {
        showAdvanceSearch(this, this.searchparam, this.filterAppend);
    },
    configurAdvancedSearch: function () {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
    },
    filterStore: function (json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
    },
    clearStoreFilter: function () {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },
    getConfigureColumnModel: function (selectedGblDim) {
        var recordArray = [];
        this.rowNo = new Wtf.grid.RowNumberer({width: 35});
        recordArray.push(this.rowNo);
        if (!Wtf.isEmpty(selectedGblDim) && !Wtf.isEmpty(selectedGblDim.data)) {
            recordArray.push({
                header: selectedGblDim.data.fieldlabel,
                dataIndex: 'dimension',
                align: 'left',
                width: 190,
                pdfwidth: 110,
                renderer: function (val, matadata, agruments) {
                    if (agruments.data.dimension == "Total") {
                        return '<b>' + val + '</b>';
                    } else {
                        return val;
                    }
                }
            });
        }
        recordArray.push({
            header:"Currency",
            dataIndex: 'currencySymbol',
            align: 'left',
            width: 190,
            pdfwidth: 110,
            hidden:true,
//            export :true,
            renderer: function () {
                return WtfGlobal.getCurrencySymbol();
            },
        });
        recordArray.push(
        {
            header: WtfGlobal.getLocaleText("acc.report.budgetvscost.Budget"),//"Budget",
            dataIndex: 'budget',
            align: 'right',
            width: 180,
            pdfwidth: 180,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.dimension == "Total") {
                    return '<b>' + WtfGlobal.currencyRenderer(val) + '</b>';
                } else {
                    return WtfGlobal.currencyRenderer(val);
                }
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.budgetvscost.projectcostsales"),//"Project Cost Sales",
            dataIndex: 'projectcostsales',
            align: 'right',
            width: 180,
            pdfwidth: 180,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.dimension == "Total") {
                    return '<b>' + WtfGlobal.currencyRenderer(val) + '</b>';
                } else {
                    return WtfGlobal.currencyRenderer(val);
                }
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.budgetvscost.projectcostpurchase"),//"Project Cost Purchase"
            dataIndex: 'projectcostpurchase',
            align: 'right',
            width: 180,
            pdfwidth: 180,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.dimension == "Total") {
                    return '<b>' + WtfGlobal.currencyRenderer(val) + '</b>';
                } else {
                    return WtfGlobal.currencyRenderer(val);
                }
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.budgetvscost.actualcost"),//"Actual Cost",
            dataIndex: 'actualcost',
            align: 'right',
            pdfwidth: 180,
            width: 180,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.dimension == "Total") {
                    return '<b>' + WtfGlobal.currencyRenderer(val) + '</b>';
                } else {
                    return WtfGlobal.currencyRenderer(val);
                }
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.budgetvscost.billing"),//"Billing",
            dataIndex: 'billing',
            width: 180,
            align: 'right',
            pdfwidth: 180,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.dimension == "Total") {
                    return '<b>' + WtfGlobal.currencyRenderer(val) + '</b>';
                } else {
                    return WtfGlobal.currencyRenderer(val);
                }
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.budgetvscost.projectedfee"),//"Projected Fee",
            dataIndex: 'projectedfee',
            width: 180,
            align: 'right',
            pdfwidth: 180,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.dimension == "Total") {
                    return '<b>' + WtfGlobal.currencyRenderer(val) + '</b>';
                } else {
                    return WtfGlobal.currencyRenderer(val);
                }
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.budgetvscost.earnedfee"),//"Earned Fee",
            dataIndex: 'earnedfee',
            width: 180,
            align: 'right',
            pdfwidth: 180,
            renderer: function (val, matadata, agruments) {
                if (agruments.data.dimension == "Total") {
                    return '<b>' + WtfGlobal.currencyRenderer(val) + '</b>';
                } else {
                    return WtfGlobal.currencyRenderer(val);
                }
            }
        });

        this.gridcmodel = new Wtf.grid.ColumnModel(recordArray);
        return this.gridcmodel;
    }

});

// *****Actual Vs Budget Report*******//



Wtf.account.ActualVsBudgetReport = function (config) {
    this.ActualVsBudgetReportRec = new Wtf.data.Record.create([
        {name: 'dimension'},
        {name: 'currentcostbudget'},
        {name: 'spentperiod'},
        {name: 'spentcommitted'},
        {name: 'pendingquotescost'},
        {name: 'projectedcostbudget'},
        {name: 'margin'},
        {name: 'pendingquotessalesprice'},
        {name: 'projectedrevenuebudget'},
        {name: 'account'},
        {name: 'currencySymbol'}
        
    ]);
    this.expButtonAvsB = new Wtf.exportButton({
        text: WtfGlobal.getLocaleText("acc.common.export"),
        obj: this,
        filename: "Actual Vs Budget Report_v1",
        tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"),
        params: {
            stdate: WtfGlobal.convertToGenericStartDate(this.getDatesAvsB(true)),
            enddate: WtfGlobal.convertToGenericEndDate(this.getDatesAvsB(false)),
            name: "Actual Vs Budget Report_v1"
        },
        menuItem: {csv: true, pdf: true, rowPdf: false, xls: true},
        get: 1334,
        disabled:true
    });
    this.expButtonAvsB.on("click", function () {
        var fieldlabelline = '';
        var lineDimensionRec = WtfGlobal.searchRecord(this.lineDimensionStoreAvsB, this.lineDimensionAvsB.getValue(), 'fieldid');
        if (lineDimensionRec != undefined && lineDimensionRec.data != undefined) {
            fieldlabelline = lineDimensionRec.data.fieldlabel;
        }
        this.expButtonAvsB.setParams({
            name: "ActualVsBudget_v1",
            globalDimention: this.globalDimensionAvsB.getValue(),
            lineDimension: this.lineDimensionAvsB.getValue(),
            fieldlabelline: fieldlabelline,
            cogaAccount : this.cogsAccount.getValue(),
            cogaAccountName : this.cogsAccount.getRawValue(),
            startDate : this.startDateAvsB.getValue().format('Y-m-d'),
            endDate : this.endDateAvsB.getValue().format('Y-m-d'),
            globalDimensioncombodataid : this.globalDimensionComboData.getValue(),
            globalDimensioncombodataname : this.globalDimensionComboData.getRawValue()
        });
    }, this);
    this.printButtonAvsB = new Wtf.exportButton({
        text: WtfGlobal.getLocaleText("acc.common.print"), //"Print",
        obj: this,
        tooltip: WtfGlobal.getLocaleText("acc.common.printTT"), //"Print Report details.",   
        menuItem: {print: true},
        get: 1334,
        label: WtfGlobal.getLocaleText("acc.ccReport.tab3"),
        disabled:true
    });
    this.printButtonAvsB.on("click", function () {
        this.printButtonAvsB.setParams({
            name: "Actual Vs Budget Report_v1",
            globalDimention: this.globalDimensionAvsB.getValue(),
            lineDimension: this.lineDimensionAvsB.getValue()
        });
    }, this);
    this.ActualVsBudgetReportStore = new Wtf.data.GroupingStore({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'count'
        }, this.ActualVsBudgetReportRec),
        url: "ACCReports/getActualVsBudgetReport.do",
        groupField:"account",
        sortInfo: {field: 'account',direction: "ASC"}
    });

    this.ActualVsBudgetReportStore.on('beforeload', function (s, o) {// add Request param before request.
        var fieldlabelline = '';
        var lineDimensionRec = WtfGlobal.searchRecord(this.lineDimensionStoreAvsB, this.lineDimensionAvsB.getValue(), 'fieldid');
        if (lineDimensionRec != undefined && lineDimensionRec.data != undefined) {
            fieldlabelline = lineDimensionRec.data.fieldlabel;
            var columns = this.getConfigureColumnModelAvsB(lineDimensionRec);
            this.grid.reconfigure(this.ActualVsBudgetReportStore, columns);
            this.grid.getView().refresh();
        }
        var fieldlabelglobal = '';
        var globalDimensionRec = WtfGlobal.searchRecord(this.globalDimensionStoreAvsB, this.globalDimensionAvsB.getValue(), 'fieldid');
        if (globalDimensionRec != undefined && globalDimensionRec.data != undefined) {
            fieldlabelglobal = globalDimensionRec.data.fieldlabel;
        }

        var currentBaseParams = this.ActualVsBudgetReportStore.baseParams;
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDateAvsB.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDateAvsB.getValue());
        this.ActualVsBudgetReportStore.baseParams = currentBaseParams;
        currentBaseParams.lineleveldimension = this.lineDimensionAvsB.getValue();
        currentBaseParams.globaldimension = this.globalDimensionAvsB.getValue();
        currentBaseParams.startDate = this.startDateAvsB.getValue().format('Y-m-d');
        currentBaseParams.endDate = this.endDateAvsB.getValue().format('Y-m-d');
        currentBaseParams.fieldlabelline = fieldlabelline;
        currentBaseParams.fieldlabelglobal = fieldlabelglobal;
        currentBaseParams.globalDimensioncombodataid = this.globalDimensionComboData.getValue();
        currentBaseParams.globalDimensioncombodataname = this.globalDimensionComboData.getRawValue();
        currentBaseParams.cogaAccount = this.cogsAccount.getValue();
        currentBaseParams.cogaAccountName = this.cogsAccount.getRawValue();
        this.ActualVsBudgetReportStore.baseParams = currentBaseParams;
    }, this);

    this.rowNoAvsB = new Wtf.grid.RowNumberer({width: 35});
    this.actualVsBudgetGridcm = this.getConfigureColumnModelAvsB();

    // Grid Configuration
    var gridSummary = new Wtf.grid.GroupSummary({});
    this.grid = new Wtf.grid.GridPanel({
        stripeRows: true,
        store: this.ActualVsBudgetReportStore,
        cm: this.actualVsBudgetGridcm,
        border: false,
        id: "ActualVsBudgetReport",
        loadMask: true,
        view: new Wtf.grid.GroupingView({
            startCollapsed :false
        }),
        plugins:[gridSummary],
        bbar: ['-', this.expButtonAvsB,'-',this.printButtonAvsB],
//        this.pagingToolbarAvsB = new Wtf.PagingSearchToolbar({
//            pageSize: 10,
//            id: "pagingtoolbarAvsB" + this.id,
//            store: this.ActualVsBudgetReportStore,
////            displayInfo: true,
//            autoWidth: true,
////            displayMsg: 'Displaying records {0} - {1} of {2}',
//            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
//            plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSizeAvsB_" + this.id}),
//            items: 
//        }),
        viewConfig:{
            emptyText:'<div style="font-size:15px; text-align:center; color:#808080; font-weight:bold; margin-top:1%;">'+ WtfGlobal.getLocaleText("acc.report.budgetvscost.selectRectoLoad")+'</div>',
            forcefit : false
        }
    });
    this.grid.on("render", function () {
        this.grid.getView().applyEmptyText();
    }, this);
    this.ActualVsBudgetReportStore.on("beforeload", function (store) {
        WtfGlobal.setAjaxTimeOutFor30Minutes();
    }, this);
    this.ActualVsBudgetReportStore.on("loadexception", function (store) {
        WtfGlobal.resetAjaxTimeOut();
    }, this);
    this.ActualVsBudgetReportStore.on("load", function (store) {
        WtfGlobal.resetAjaxTimeOut();
        if (store.getCount() == 0) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
            this.expButtonAvsB.setDisabled(true);
            this.printButtonAvsB.setDisabled(true);
        }else{
            this.expButtonAvsB.setDisabled(false);
            this.printButtonAvsB.setDisabled(false);
        }
    }, this);
    this.startDateAvsB = new Wtf.ExDateFieldQtip({
        fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), //'From',
        name: 'stdate',
        format: WtfGlobal.getOnlyDateFormat(),
        value: this.getDatesAvsB(true)
    });
    this.endDateAvsB = new Wtf.ExDateFieldQtip({
        fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), //'To',
        format: WtfGlobal.getOnlyDateFormat(),
        name: 'enddate',
        value: this.getDatesAvsB(false)
    });

    this.globalDimensionRec = new Wtf.data.Record.create([
        {name: "fieldid"},
        {name: "fieldlabel"},
        {name: "column_number"},
        {name: "refcolumn_number"}
    ]);

    this.globalDimensionAvsB = new Wtf.data.KwlJsonReader({
        root: "data"
    }, this.globalDimensionRec);

    this.globalDimensionStoreAvsB = new Wtf.data.Store({
        url: "ACCAccountCMN/getFieldParams.do",
        reader: this.globalDimensionAvsB,
        baseParams: {
            ignoreDefaultFields: true,
            isActivated: 1,
            isAdvanceSearch: false,
            isAvoidRedundent: true,
            iscustomdimension: true,
            iscustomcolumn: false
        }
    });
    this.globalDimensionStoreAvsB.load();
    this.globalDimensionAvsB = new Wtf.form.ComboBox({//All/Balance Sheet/Profit & Loss
        store: this.globalDimensionStoreAvsB,
        name: 'typeid',
        displayField: 'fieldlabel',
        valueField: 'fieldid',
        mode: 'local',
        width: 150,
        listWidth: 150,
        emptyText: WtfGlobal.getLocaleText("acc.report.budgetvscost.selectGblDim"), //"Select Global Dimension",
        triggerAction: 'all',
        typeAhead: true,
        selectOnFocus: true
    });

    this.globalDimensioncombodataRec = new Wtf.data.Record.create([
        {name: "id"},
        {name: "name"}
    ]);

    this.globalDimensioncombodata = new Wtf.data.KwlJsonReader({
        root: "data"
    }, this.globalDimensioncombodataRec);

    this.globalDimensioncombodataStore = new Wtf.data.Store({
        url: "ACCMaster/getMasterItemsForCustomFoHire.do",
        reader: this.globalDimensioncombodata,
        baseParams: {
            mode: 112
        }
    });

    this.globalDimensionComboData = new Wtf.form.ComboBox({//All/Balance Sheet/Profit & Loss
        store: this.globalDimensioncombodataStore,
        name: 'typeid',
        displayField: 'name',
        valueField: 'id',
        mode: 'local',
        width: 150,
        listWidth: 150,
        emptyText: WtfGlobal.getLocaleText("acc.report.budgetvscost.selectGblDimCombodata"), //"Select Global Dimension Combo Data",
        triggerAction: 'all',
        typeAhead: true,
        selectOnFocus: true
    });

    this.purchaseAccRec = Wtf.data.Record.create([
        {name: 'accid'},
        {name: 'accname'},
        {name: 'acccode'},
        {name: 'nature'},
        {name: 'hasAccess'},
        {name: 'groupname'}

    ]);
    this.purchaseAccStoreAvsB = new Wtf.data.Store({
        url: "ACCAccountCMN/getAccountsIdNameForCombo.do",
        baseParams: {
            mode: 2,
            ignorecustomers: true,
            ignorevendors: true,
            nondeleted: true,
            controlAccounts: true
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.purchaseAccRec)
    });
    this.purchaseAccStoreAvsB.load();
    this.cogsAccount = new Wtf.form.ExtFnComboBox({
        emptyText: WtfGlobal.getLocaleText("acc.field.cogs.account"), // Cost of Goods Sold Account*
        store: this.purchaseAccStoreAvsB,
        anchor: '70%',
        extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode','groupname'] : ['groupname'],
        extraComparisionField: 'acccode', // type ahead search on acccode as well.
        typeAheadDelay: 30000,
        typeAhead: true,
        isAccountCombo: true,
        listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 350 : 240,
        valueField: 'accid',
        forceSelection: true,
        displayField: 'accname',
    });
    this.cogsAccount.store.on("load", function () {
        if (this.cogsAccount.getRawValue() == "" || this.cogsAccount.getRawValue() == undefined || this.cogsAccount.getRawValue() == null) {

            var record = new Wtf.data.Record({
                accid:"1",
                accname:"All Accounts",
                acccode:"",
                nature:"",
                hasAccess:"",
                groupname:""
            });
            this.cogsAccount.store.insert(0, record);
        }
    }, this);
    this.lineDimensionRecAvsB = new Wtf.data.Record.create([
        {name: "fieldid"},
        {name: "fieldlabel"},
        {name: "column_number"},
        {name: "refcolumn_number"}
    ]);

    this.linelDimensionRederAvsB = new Wtf.data.KwlJsonReader({
        root: "data"
    }, this.lineDimensionRecAvsB);

    this.lineDimensionStoreAvsB = new Wtf.data.Store({
        url: "ACCAccountCMN/getFieldParams.do",
        reader: this.linelDimensionRederAvsB,
        baseParams: {
            ignoreDefaultFields: true,
            isActivated: 1,
            isAdvanceSearch: false,
            isAvoidRedundent: true,
            iscustomdimension: true,
            iscustomcolumn: true
        }
    });
    this.lineDimensionStoreAvsB.load();
    this.lineDimensionAvsB = new Wtf.form.ComboBox({
        labelSeparator: '',
        labelWidth: 0,
        triggerAction: 'all',
        mode: 'local',
        valueField: 'fieldid',
        displayField: 'fieldlabel',
        store: this.lineDimensionStoreAvsB,
        value: true,
        width: 150,
        emptyText: WtfGlobal.getLocaleText("acc.report.budgetvscost.selectlineDim"), //"Select Line Dimension",
        name: 'lineDimension',
        hiddenName: 'lineDimension'
    });
    this.globalDimensionAvsB.on('select', function (a, b) {
        this.globalDimensionComboData.reset();
        var fieldLabelGlobal = "";
        var idGlobal = "";
        var globalDimensionRec = WtfGlobal.searchRecord(this.globalDimensionStoreAvsB, this.globalDimensionAvsB.getValue(), 'fieldid');
        if (globalDimensionRec != undefined && globalDimensionRec.data != undefined) {
            fieldLabelGlobal = globalDimensionRec.data.fieldlabel;
            idGlobal = globalDimensionRec.data.fieldid;
        }
        this.globalDimensioncombodataStore.load({
            params: {
                fieldlabel: fieldLabelGlobal,
                groupid: idGlobal
            }
        });
    }, this);
    this.fetchAvsB = new Wtf.Toolbar.Button({
        scope: this,
        text: WtfGlobal.getLocaleText("acc.common.fetch"), //"Fetch",
        iconCls: "accountingbase fetch",
        handler: function () {
            this.ActualVsBudgetReportStore.load();
        }
    });
    this.resetAvsB = new Wtf.Toolbar.Button({
        scope: this,
        text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        handler: function () {
            this.startDateAvsB.setValue(this.getDatesAvsB(true));
            this.endDateAvsB.setValue(this.getDatesAvsB(false));
            this.lineDimensionAvsB.reset();
            this.globalDimensionAvsB.reset();
            this.globalDimensionComboData.reset();
            this.cogsAccount.reset();
            var columns = this.getConfigureColumnModelAvsB();
            this.grid.reconfigure(this.ActualVsBudgetReportStore, columns);
            this.grid.getView().refresh();
            this.ActualVsBudgetReportStore.removeAll();
            this.ActualVsBudgetReportStore.load();
        }
    });
    var btnArr = [];
    btnArr.push(WtfGlobal.getLocaleText("acc.common.from"), this.startDateAvsB, WtfGlobal.getLocaleText("acc.common.to"), this.endDateAvsB, '-');
    btnArr.push(WtfGlobal.getLocaleText("acc.report.budgetvscost.globDim"), ':', this.globalDimensionAvsB, '-',
            WtfGlobal.getLocaleText("acc.masterConfig.mi"), ":", this.globalDimensionComboData, '-', 'COGS Account', this.cogsAccount, '-', WtfGlobal.getLocaleText("acc.report.budgetvscost.lineDim"), ':', this.lineDimensionAvsB, '-');
    btnArr.push(this.fetchAvsB, "-", this.resetAvsB);
    var firstbar = new Wtf.Toolbar(btnArr);
    this.toolbarPanelAvsB = new Wtf.Panel({
        id:"toolbarPanelAvsB"+this.id,
        items: [firstbar]
    });

    Wtf.apply(this, {
        items: [{
                layout: 'border',
                border: false,
                scope: this,
                id:"ActualVsBudgetReport_Layout",
                items: [{
                        region: 'center',
                        layout: 'fit',
                        border: false,
                        items: this.grid
                    }],
                tbar: this.toolbarPanelAvsB
            }]
    }, config);

    Wtf.account.ActualVsBudgetReport.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.account.ActualVsBudgetReport, Wtf.Panel, {
    onRender: function (config) {
        Wtf.account.ActualVsBudgetReport.superclass.onRender.call(this, config);
    },
    getDatesAvsB: function (start) {
        var d = new Date();
        var monthDateStr = d.format('M d');
        if (Wtf.account.companyAccountPref.fyfrom)
            monthDateStr = Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd = new Date(monthDateStr + ', ' + d.getFullYear() + ' 12:00:00 AM');
        if (d < fd)
            fd = new Date(monthDateStr + ', ' + (d.getFullYear() - 1) + ' 12:00:00 AM');
        if (start)
            return fd;

        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    getConfigureColumnModelAvsB: function (selectedGblDim) {
        var recordArray = [];
        this.rowNoAvsB = new Wtf.grid.RowNumberer({width: 35});
        recordArray.push(this.rowNoAvsB);
        recordArray.push({
            header: "Account",
            dataIndex: 'account',
            width: 180,
            pdfwidth: 180
//            hidden:true
        });
        
//        if (!Wtf.isEmpty(selectedGblDim) && !Wtf.isEmpty(selectedGblDim.data)) {
            recordArray.push({
                header: selectedGblDim!=undefined && selectedGblDim.data!=undefined ? selectedGblDim.data.fieldlabel:"Line Dimension",
                dataIndex: 'dimension',
                align: 'left',
                width: 190,
                pdfwidth: 110,
                summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'},
            });
//        }
        recordArray.push({
            header:"Currency",
            dataIndex: 'currencySymbol',
            align: 'left',
            width: 190,
            pdfwidth: 110,
            hidden:true,
//            export :true,
            renderer: function () {
                return WtfGlobal.getCurrencySymbol();
            },
        });
        recordArray.push({
            header: WtfGlobal.getLocaleText("acc.report.actualvsbudget.currentcostbudget"),//"Current Cost Budget",
            dataIndex: 'currentcostbudget',
            align: 'right',
            width: 180,
            pdfwidth: 180,
            renderer: WtfGlobal.currencyRenderer,
            summaryType:'sum',
            summaryRenderer: function (value, m, rec) {
                var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                return retVal;
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.actualvsbudget.spentthisperiod"),//"Spent This Period",
            dataIndex: 'spentperiod',
            align: 'right',
            width: 180,
            pdfwidth: 180,
            renderer: WtfGlobal.currencyRenderer,
            summaryType:'sum',
            summaryRenderer: function (value, m, rec) {
                var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                return retVal;
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.actualvsbudget.spentcommitted"),//"Spent/Committed",
            dataIndex: 'spentcommitted',
            align: 'right',
            width: 180,
            pdfwidth: 180,
            renderer: WtfGlobal.currencyRenderer,
            summaryType:'sum',
            summaryRenderer: function (value, m, rec) {
                var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                return retVal;
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.actualvsbudget.pendingQuotecost"),//"Pending Quotes (Total Cost on Quotation)",
            dataIndex: 'pendingquotescost',
            align: 'right',
            pdfwidth: 180,
            width: 180,
            renderer: WtfGlobal.currencyRenderer,
            summaryType:'sum',
            summaryRenderer: function (value, m, rec) {
                var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                return retVal;
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.actualvsbudget.projectedcostbudget"),//"Projected Cost Budget",
            dataIndex: 'projectedcostbudget',
            width: 180,
            align: 'right',
            pdfwidth: 180,
            renderer: WtfGlobal.currencyRenderer,
            summaryType:'sum',
            summaryRenderer: function (value, m, rec) {
                var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                return retVal;
            }
        },{
            header: WtfGlobal.getLocaleText("acc.report.actualvsbudget.Margin"),//"Margin",
            dataIndex: 'margin',
            width: 180,
            align: 'right',
            pdfwidth: 180,
            renderer: WtfGlobal.currencyRenderer,
            summaryType:'sum',
            summaryRenderer: function (value, m, rec) {
                var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                return retVal;
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.actualvsbudget.peningquotessalesprice"),//"Pending Quotes (Total Sales Price)",
            dataIndex: 'pendingquotessalesprice',
            width: 180,
            align: 'right',
            pdfwidth: 180,
            renderer: WtfGlobal.currencyRenderer,
            summaryType:'sum',
            summaryRenderer: function (value, m, rec) {
                var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                return retVal;
            }
            
        }, {
            header: WtfGlobal.getLocaleText("acc.report.actualvsbudget.projectedrevenuebudget"),//"Projected Revenue Budget",
            dataIndex: 'projectedrevenuebudget',
            width: 180,
            align: 'right',
            pdfwidth: 180,
            renderer: WtfGlobal.currencyRenderer,
            summaryType:'sum',
            summaryRenderer: function (value, m, rec) {
                var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                return retVal;
            }
        });

        this.actualVsBudgetGridcmodel = new Wtf.grid.ColumnModel(recordArray);
        return this.actualVsBudgetGridcmodel;
    }

});


