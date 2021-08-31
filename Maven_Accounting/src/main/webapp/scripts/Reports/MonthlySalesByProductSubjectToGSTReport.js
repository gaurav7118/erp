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

Wtf.account.monthlySalesByProductSubjectToGSTReport = function(config) {
    this.receivable = config.receivable || false;
    this.withinventory = config.withinventory || false;
    this.isSummary = config.isSummary || false;
    this.isCustomWidgetReport=config.isCustomWidgetReport||false;
    this.isBasedOnProduct = config.isBasedOnProduct != undefined ? config.isBasedOnProduct : false;
    this.MaxAmountColumnCnt = 44;
    this.monthStore = new Wtf.data.SimpleStore({
        fields: [{
            name: 'monthid',
            type: 'int'
        }, 'name'],
        data :[[0,'January'],[1,'February'],[2,'March'],[3,'April'],[4,'May'],[5,'June'],[6,'July'],[7,'August'],[8,'September'],[9,'October'],[10,'November'],[11,'December']]
    });

    var data = WtfGlobal.getBookBeginningYear(true);
    
    this.yearStore= new Wtf.data.SimpleStore({
        fields: [{
            name: 'id',
            type: 'int'
        }, 'yearid'],
        data: data
    });

    this.startMonth = new Wtf.form.ComboBox({
        store: this.monthStore,
        fieldLabel: WtfGlobal.getLocaleText("acc.accPref.month"), // 'Month',
        name: 'startMonth',
        displayField: 'name',
        forceSelection: true,
        width: 90,
        valueField: 'name',
        mode: 'local',
        triggerAction: 'all',
        selectOnFocus: true
    });  

    this.startYear = new Wtf.form.ComboBox({
        store: this.yearStore,
        fieldLabel: WtfGlobal.getLocaleText("acc.accPref.year"),  // 'Year',
        name: 'startYear',
        width: 90,
        displayField: 'yearid',
        valueField: 'yearid',
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        selectOnFocus: true
    });  

    this.endMonth = new Wtf.form.ComboBox({
        store: this.monthStore,
        fieldLabel: WtfGlobal.getLocaleText("acc.accPref.month"), // 'Month',
        name: 'endMonth',
        displayField: 'name',
        forceSelection: true,
        width: 90,
        valueField: 'name',
        mode: 'local',
        triggerAction: 'all',
        selectOnFocus: true
    }); 

    this.endYear = new Wtf.form.ComboBox({
        store: this.yearStore,
        fieldLabel: WtfGlobal.getLocaleText("acc.accPref.year"), // 'Year',
        name: 'endYear',
        displayField: 'yearid',
        width: 90,
        valueField: 'yearid',
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        selectOnFocus: true
    });       

    if (config.sMonth != null && config.sMonth != "") {
        this.startMonth.setValue(config.sMonth);
    }

    if (config.sYear != null && config.sYear != "") {
        this.startYear.setValue(config.sYear);
    }

    if (config.eMonth != null && config.eMonth != "") {
        this.endMonth.setValue(config.eMonth);   
    }

    if (config.eYear != null && config.eYear != "") {
        this.endYear.setValue(config.eYear);
    }
	
    var recordArr = [
	{name: 'customerid'},
	{name: 'customername'},
        {name: 'productid'},
        {name: 'productname'},
        {name: 'pid'},
        {name: 'salesAccountName'}];
    
    for (var cnt = 0; cnt <= this.MaxAmountColumnCnt; cnt++) {
        recordArr.push({name: 'amount_'+cnt});
    }
    recordArr.push({name: 'amountinbase'});
    this.MonthlySalesRecord = new Wtf.data.Record.create(recordArr);
	
    this.expGet = Wtf.autoNum.MonthlySalesByProductSubjectToGSTReport;

    this.MonthlySalesStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'count'
        }, this.MonthlySalesRecord),
        url: this.isBasedOnProduct ? "ACCInvoiceCMN/getMonthlySalesByProductSubjectToGSTReport.do" : "ACCInvoiceCMN/getMonthlySalesReport.do",
        baseParams: {
            creditonly: false,
            nondeleted: true,
            getRepeateInvoice: false,
            consolidateFlag: config.consolidateFlag,
            companyids: companyids,
            gcurrencyid: gcurrencyid,
            userid: loginid				
        }
    });

    this.typeStore = new Wtf.data.SimpleStore({
        fields: [{
            name: 'typeid',
            type: "boolean"
        }, 'name'],
        data: [[true, WtfGlobal.getLocaleText("acc.rem.127")], [false, WtfGlobal.getLocaleText("acc.rem.128")]]
    });

    this.typeEditor = new Wtf.form.ComboBox({
        store: this.typeStore,
        name: 'isdistributive',
        displayField: 'name',
        value: true,
        anchor: "50%",
        valueField: 'typeid',
        mode: 'local',
        triggerAction: 'all'
    });

    this.rowNo = new Wtf.KWLRowNumberer();

    var columnArr = [];  
        
    var columnWidth = 110;
    columnArr.push(this.rowNo, {
        header: WtfGlobal.getLocaleText("acc.cust.name"), // Customer Name
        dataIndex: this.isBasedOnProduct ? 'productid' : 'customerid',
        align: 'right',
        width: 120,
        pdfwidth: 110,
        hidden: true
    });

    columnArr.push({
        header: this.isBasedOnProduct ? WtfGlobal.getLocaleText("acc.invoiceList.expand.pName") : WtfGlobal.getLocaleText("acc.cust.name"), // Customer Name
        dataIndex: this.isBasedOnProduct ? 'productname' :'customername',
        align: 'left',
        width: 120,
        pdfwidth: 110,
        hidden: false
    });
    
   
    
    if (this.isBasedOnProduct) {
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.productList.gridProductID"), // Product ID
            dataIndex: 'pid',
            align: 'left',
            width: 120,
            pdfwidth: 110,
            hidden: false
        });
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.product.salesAccount"), // "Sales Account",
            dataIndex: "salesAccountName",
            align: 'left',
            width: 120,
            pdfwidth: 110,
            hidden: false
        });
    }

    for (var i = 0; i < this.MaxAmountColumnCnt; i++) {
        columnArr.push({
            hidden: false,
            dataIndex: 'amount_' + i,
//            renderer: this.formatMoney,
            renderer: WtfGlobal.withoutRateCurrencyDeletedSymbol,
            width: columnWidth,
            pdfwidth: 110,
            align: 'right',
            style: 'text-align:left'
        });
    }
        columnArr.push({
            header:"<div><b>Total"+ "(" + WtfGlobal.getCurrencyName() + ")<b><div>", 
            dataIndex: "amountinbase",
            align: 'left',
            renderer: WtfGlobal.currencyRenderer,
            width: 120,
            pdfwidth: 110,
            hidden:false
        });

    // column model
    this.gridcm = new Wtf.grid.ColumnModel(columnArr);    

    this.grid = new Wtf.grid.GridPanel({
        store: this.MonthlySalesStore,
        cm: this.gridcm,
        ctCls: 'monthlySalesreport',
        border: false,
        layout: 'fit',
        viewConfig: {
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn")),    //ERP-28938
            deferEmptyText: false
        },
        loadMask: true
    });
    /**
     * Apply Empty Text.
     */
    this.grid.on("afterlayout",function(g,l){
        this.grid.getView().applyEmptyText();
    },this);
    
    this.grid.on("render", WtfGlobal.autoApplyHeaderQtip);
    this.grid.getView().refresh.defer(1, this.grid.getView());    //ERP-28938
    
    this.resetBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.reset"), // 'Reset',
        hidden:this.isCustomWidgetReport,
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), // 'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        disabled: false
    });

    // get date from month & year drop-down lists
    if (this.startMonth.getValue() == "" || this.startYear.getValue() == "") {
        var temp = new Date();
        var year1 = temp.getFullYear();
        this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
        this.startYear.setValue(year1);
        this.endMonth.setValue(this.monthStore.data.items[this.monthStore.data.items.length-1].json[1]);   
        this.endYear.setValue(year1);
    }

    this.sDate = this.startMonth.getValue() + ", " + this.startYear.getValue();
    this.eDate = this.endMonth.getValue() + ", " + this.endYear.getValue();

    if (this.sDate == "" || this.eDate == "") {
        WtfComMsgBox(42,2);
        return;
    }

    var startMonthDate = new Date(this.startMonth.getValue() + " 01, " + this.startYear.getValue());
    var endMonthDate = new Date(this.endMonth.getValue() + " 01, " + this.endYear.getValue());        

    if (this.startYear.getValue() >= this.endYear.getValue() && startMonthDate.getMonth() > endMonthDate.getMonth()) {
        WtfComMsgBox(1,2);
        return;            
    }

    var months;
    months = (endMonthDate.getFullYear() - startMonthDate.getFullYear()) * 12;
    months -= startMonthDate.getMonth();
    months += endMonthDate.getMonth();
    if (months < 0) {
        months = 0;
    }

    if (months > 18) {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Onlymaximum18monthsaresupported") ], 2);
        return;            
    }
	
    this.expButton = new Wtf.exportButton({
        obj: this,
        filename: this.isBasedOnProduct ? "MonthlySalesByProductSubjectToGSTReport_v1" : "MonthlySalesReport",
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip: WtfGlobal.getLocaleText("acc.sales.exportTT"), // 'Export report details',
        disabled: true,
        hidden:this.isCustomWidgetReport,
        params: {
            stdate: this.sDate,
            enddate: this.eDate,			
            accountid: this.accountID || config.accountID,
            isdistributive: this.typeEditor.getValue(),
            isBasedOnProduct : this.isBasedOnProduct
        },
        menuItem: {
            csv: true,
            pdf: true,
            rowPdf: false,
            xls: true
        },
        get: this.expGet
    });
    
    this.printButton = new Wtf.exportButton({
        obj: this,
        text: WtfGlobal.getLocaleText("acc.common.print"),
        tooltip: WtfGlobal.getLocaleText("acc.sales.printTT"), // 'Print report details',
        disabled: true,
        hidden:this.isCustomWidgetReport,
        filename : this.isBasedOnProduct ? WtfGlobal.getLocaleText("acc.field.monthlySalesByProductSubjectToGSTReport"): WtfGlobal.getLocaleText("acc.wtfTrans.monthlySalesReport"),
        params: {
            accountid: this.accountID || config.accountID,
            stdate: this.sDate,
            enddate: this.eDate,				
            isdistributive: this.typeEditor.getValue(),
            name: WtfGlobal.getLocaleText("acc.wtfTrans.monthlySalesReport"),
            isBasedOnProduct: this.isBasedOnProduct
        },
        label: WtfGlobal.getLocaleText("acc.sales.tabTitle"),
        menuItem: {
            print: true
        },
        get: this.expGet
    });
    
    var btnArr = [];
    btnArr.push(
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: this.isBasedOnProduct ? WtfGlobal.getLocaleText("acc.saleByItem.search"): WtfGlobal.getLocaleText("acc.sales.searchcustomer"), // Search by Customer Name
            id:"quickSearch"+config.helpmodeid,
            width: 200,
            hidden:this.isCustomWidgetReport,
            field: 'customername'
        }), this.resetBttn);


    btnArr.push('-', WtfGlobal.getLocaleText("acc.common.from"), 
        this.startMonth, this.startYear
    );
                    
    btnArr.push('-', WtfGlobal.getLocaleText("acc.common.to"),
        this.endMonth, this.endYear
    );
    
    if (this.isBasedOnProduct) {
        this.productTypeRec = Wtf.data.Record.create ([
            {name: 'id'},
            {name: 'name'}
        ]);

        this.productTypeStore = new Wtf.data.Store({
            url: "ACCProduct/getProductTypes.do",
            baseParams: {
                mode: 24,
                common: '1'
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productTypeRec)
        });

        this.productType = new Wtf.form.ComboBox({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.product.productType.tt") + "'>" + WtfGlobal.getLocaleText("acc.invReport.type") + "</span>",
            hiddenName: 'producttype',
            store: this.productTypeStore,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            triggerAction: 'all',
            typeAhead: true,
            forceSelection: true,
            width: 100
        });
        this.productTypeStore.load();

        this.productTypeStore.on("load", function() {
            var record = new Wtf.data.Record({
                id: "",
                name: "All Records"
            });
            this.productTypeStore.insert(0, record);
            this.productType.setValue("");
        }, this);

        btnArr.push('-', "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.product.productType.tt") + "'>" + WtfGlobal.getLocaleText("acc.invReport.type") + "</span>", this.productType);
    }

    btnArr.push("-", {
        xtype: 'button',
        text: WtfGlobal.getLocaleText("acc.sales.fetch"), // 'Fetch',
        iconCls: 'accountingbase fetch',
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.sales.view"), // "Select a date to view Monthly Sales Report
        handler: this.fetchMonthlySalesReport
    });
    
    btnArr.push(this.expButton);
    btnArr.push(this.printButton);
    this.resetBttn.on('click',this.handleResetClick,this);
    
    Wtf.apply(this, {
        border: false,
        layout: "fit",
        tbar: btnArr,
        items:[this.grid],
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.MonthlySalesStore,
            searchField: this.quickPanelSearch,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.sales.norec"), // "No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
            })
        })
    });

    Wtf.account.monthlySalesByProductSubjectToGSTReport.superclass.constructor.call(this,config);
    this.addEvents({
        'salesinvoices': true
    });
    
    this.MonthlySalesStore.on("beforeload", function(s,o) {
        o.params.stdate = this.sDate;
        o.params.enddate = this.eDate;
        o.params.productTypeID = this.isBasedOnProduct? this.productType.getValue() : "";
    },this);

    this.MonthlySalesStore.on("load",this.storeloaded,this);
    
    this.MonthlySalesStore.on('datachanged', function() {
        var p = 30;
        this.quickPanelSearch.setPage(p);
    }, this);
}

Wtf.extend(Wtf.account.monthlySalesByProductSubjectToGSTReport, Wtf.Panel, {
    onRender: function(config) {
//        this.MonthlySalesStore.load({
//            params:{
//                start: 0,			
//                isdistributive: this.typeEditor.getValue(),
//                limit: 30,
//                creditonly: true
//            }
//        });

        Wtf.account.monthlySalesByProductSubjectToGSTReport.superclass.onRender.call(this,config);
    },
    
    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.MonthlySalesStore.load({
                params: {
                    start: 0,					
                    isdistributive: this.typeEditor.getValue(),
                    limit: this.pP.combo.value,
                    aged: true,
                    creditonly: true
                }
            });
        }
    },
        
    storeloaded: function(store) {
        // get month count from the first element
        var store = this.grid.getStore();

        var monthArray = store.data.items[0].json["months"];
        var monthCount = monthArray.length;

        var monthToHideStart;
        if (this.isBasedOnProduct) {
            monthToHideStart = 5;
        } else {
            monthToHideStart = 3;
        }
        for (var i=0; i<monthArray.length; i++) {
            if (monthArray[i]["monthname"] == 'Total') { // Removed Link Form Total Column
                this.grid.getColumnModel().setRenderer((i+monthToHideStart),WtfGlobal.currencyRenderer);
                this.grid.getView().refresh();
            }
            this.grid.getColumnModel().setColumnHeader((i+monthToHideStart), '<div><b>'+monthArray[i]["monthname"]+ "(" + WtfGlobal.getCurrencyName() + ")" +'</b></div>');        
            //            this.grid.getColumnModel().setRenderer((i+monthToHideStart), this.formatMoney);
            this.grid.getColumnModel().setRenderer((i+monthToHideStart), WtfGlobal.withoutRateCurrencyDeletedSymbol);
        }   

        var columnCount =  this.grid.getColumnModel().getColumnCount();

        // show those months with data
        for (var i=monthToHideStart; i<(monthToHideStart+monthCount); i++) {
            this.grid.getColumnModel().setHidden(i, false) ;
        }

        // hide those months without data
        for (var i=(monthCount+monthToHideStart); i<columnCount-1; i++) {
            this.grid.getColumnModel().setHidden(i,true) ;
        } 
        
        if (store.getCount() == 0) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();

            if (this.expButton) {
                this.expButton.disable();
            }
            if (this.printButton) {
                this.printButton.disable();
            }
        } else {
            if (this.expButton) {
                this.expButton.enable();
            }
            if (this.printButton) {
                this.printButton.enable();
            }
        }
        this.quickPanelSearch.StorageChanged(store);
    },

    fetchMonthlySalesReport: function() {
            
        if (this.startYear.getValue() > this.endYear.getValue() ){
            WtfComMsgBox(1,2);
            return;            
        }
        // get date from month & year drop-down lists
        if (this.startMonth.getValue() == "" || this.startYear.getValue() == "") {
            this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
            this.startYear.setValue(this.yearStore.data.items[0].json[1]);
            this.endMonth.setValue(this.monthStore.data.items[this.monthStore.data.items.length-1].json[1]);   
            this.endYear.setValue(this.yearStore.data.items[0].json[1]);
        }

        this.sDate = this.startMonth.getValue() + ", " + this.startYear.getValue();
        this.eDate = this.endMonth.getValue() + ", " + this.endYear.getValue();

        if (this.sDate == "" || this.eDate == "") {
            WtfComMsgBox(42,2);
            return;
        }

        var startMonthDate = new Date(this.startMonth.getValue() + " 01, " + this.startYear.getValue());
        var endMonthDate = new Date(this.endMonth.getValue() + " 01, " + this.endYear.getValue());        

        if (this.startYear.getValue() >= this.endYear.getValue() && startMonthDate.getMonth() > endMonthDate.getMonth()) {
            WtfComMsgBox(1,2);
            return;            
        }

        var months;
        months = (endMonthDate.getFullYear() - startMonthDate.getFullYear()) * 12;
        months -= startMonthDate.getMonth();
        months += endMonthDate.getMonth();
        if (months < 0) {
            months=0;
        }

        if (months > 18) {
            WtfComMsgBox(["Alert",'Only maximum 18 months are supported!' ], 2);
            return;            
        }  

        this.MonthlySalesStore.load({
            params: {
                isdistributive: this.typeEditor.getValue(),
                start: 0,
                limit: this.pP.combo.value,
                creditonly: true
            }
        });

        this.expButton.setParams({
            stdate: this.sDate,
            enddate: this.eDate,			
            isdistributive: this.typeEditor.getValue()
        });

        this.printButton.setParams({
            stdate: this.sDate,
            enddate: this.eDate,			
            isdistributive: this.typeEditor.getValue(),
            name: this.isBasedOnProduct ? "Monthly Sales By Product Report Subject to GST Report" : "Monthly Sales Report"
        });
    }
});