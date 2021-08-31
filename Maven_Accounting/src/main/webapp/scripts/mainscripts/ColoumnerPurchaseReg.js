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

Wtf.account.ColoumnerPurchaseRegister = function (config) {
    Wtf.apply(this, config);
    this.sm = new Wtf.grid.CheckboxSelectionModel({});
    this.InvoiceAmt=0;
    this.gridBbar=new Array();
    this.createGrid();
    this.createTBar();

    Wtf.account.ColoumnerPurchaseRegister.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.ColoumnerPurchaseRegister, Wtf.Panel, {
    onRender: function (config) {
        this.coloumnerPan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                region: 'center',
                layout: 'fit',
                border: false,
                //                autoScroll:true,
                items: [this.grid],
                tbar: this.tbarArray,
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 15,
                    id: "pagingtoolbar" + this.id,
                    store: this.columnStore,
                    searchField: this.quickPanelSearch,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({
                        id : "pPageSize_"+this.id
                    }),
                    items: this.bBarArr
                })
            }]
        });
        
        this.add(this.coloumnerPan);
        this.loadStore();
        Wtf.account.DisbursementReport.superclass.onRender.call(this, config);
    },
    putParams: function () {
//        if (arguments[1].params.todate == undefined || arguments[1].params.fromdate == undefined) {
            this.columnStore.baseParams = {
                todate: WtfGlobal.convertToGenericEndDate(this.toDate.getValue()),
                fromdate: WtfGlobal.convertToGenericDate(this.frmDate.getValue()),
                companyid: companyid
            }
//        }
    },
    setParamertsForExport: function () {
        this.exportButton.setParams({
            obj: this,
            id: "exportReports" + this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
            scope: this,
            startDate: WtfGlobal.convertToGenericDate(this.frmDate.getValue()),
            startdate: WtfGlobal.convertToGenericDate(this.frmDate.getValue()),
            endDate: WtfGlobal.convertToGenericEndDate(this.toDate.getValue()),
            enddate: WtfGlobal.convertToGenericEndDate(this.toDate.getValue()),
            filename: this.action=="SaleReg"? WtfGlobal.getLocaleText("acc.field.ColoumnerSalesRegisters")+"" : this.action=="PurchaseReg"? WtfGlobal.getLocaleText("acc.field.ColoumnerPurchaseRegisters")+"":"",
            menuItem: {
                csv: true, 
                pdf: true, 
                rowPdf: false, 
                xls: true
            },
            get: this.action=="SaleReg"? Wtf.autoNum.columnSalesRegisterReport : this.action=="PurchaseReg"? Wtf.autoNum.columnPurchaseRegisterReprot:""
        });
    },
    handleStoreOnLoad: function (store) {
        if(!(this.frmDate.getValue()>this.toDate.getValue())){
            this.InvoiceAmt=this.columnStore.reader.jsonData.totalInvoiceAmt;
            var currSymb=this.columnStore.reader.jsonData.currSymb;
            var columns = [];
            columns.push(new Wtf.grid.RowNumberer({
                width:20
            }));
                
            Wtf.each(this.columnStore.reader.jsonData.columns, function(column) {            
                column.sortable= true;
                if (column.dataIndex == "rate") {
                    column.summaryRenderer = function(v) {
                        return '<div class="grid-summary-common"; align="right";>' + WtfGlobal.getLocaleText("acc.common.total") + '</div>';
                    }
                } else if (!(column.dataIndex == "date" || column.dataIndex == "invoiceno" || column.dataIndex == "documenttye" || column.dataIndex == "vendor"
                    || column.dataIndex == "customer"
                    || column.dataIndex == "vattin" || column.dataIndex == "csttin" || column.dataIndex == "ecc" || column.dataIndex == "servicetaxregno"
                    || column.dataIndex == "productname" || column.dataIndex == "quantitywithuom" || column.dataIndex == "rate")) {
                    column.summaryType = 'sum';
                    column.renderer = WtfGlobal.amountRendererForExport;
                    column.summaryRenderer = function(v) {
                        if (v != "" && v != undefined && currSymb != undefined) {
                            return '<div class="grid-summary-common" align="right">' + "<b>" + currSymb + " " + parseFloat(v).toFixed(2) + "</b>" + '</div>';
                        } else {
                            return '<div class="grid-summary-common" align="right">' + "<b>" + currSymb + " " + parseFloat(0).toFixed(2) + "</b>" + '</div>';
                        }
                    }
                } 
                columns.push(column);
            });
            var Arr = [];
            Wtf.each(this.columnStore.reader.jsonData.metaData.fields, function(column) {
                Arr.push(column);
            });
            this.groupStore.removeAll();
            this.groupStore.fields = Arr;
            //        this.groupStore.add(this.columnStore.getRange(0, (this.columnStore.data.items.length - 1)));
            this.grid.getColumnModel().setConfig(columns);
            this.groupStore.add(this.columnStore.getRange(0, (this.columnStore.data.items.length - 1)));
            // below code hide sortable option from column menu
            var ms = this.grid.view.hmenu.items;
            ms.get("asc").hide(true);
            ms.get("desc").hide(true);
            // **************####*******************
            this.grid.getView().refresh();  
            
            if (this.columnStore.getCount() < 1) {
                this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.grid.getView().refresh();
            }
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.1")], 2);
            this.frmDate.reset();
            this.toDate.reset();
            return; 
        }
    },
    loadStore: function () {
        this.columnStore.load({
            params: {
                companyid:companyid,
                fromdate: WtfGlobal.convertToGenericDate(this.frmDate.getValue()),
                todate: WtfGlobal.convertToGenericEndDate(this.toDate.getValue()),
                start:0,
                limit:(this.pP.combo == undefined) ? 15 : this.pP.combo.value
            }
        });
    },
    fetchData: function () {
        if(this.frmDate.getValue()>this.toDate.getValue()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.1")], 2);
            this.frmDate.reset();
            this.toDate.reset();
            return;
        }
        if (this.frmDate.isValid() && this.toDate.isValid()) {
            this.columnStore.load({
                params: {
                    companyid: companyid,
                    fromdate: WtfGlobal.convertToGenericDate(this.frmDate.getValue()),
                    todate: WtfGlobal.convertToGenericEndDate(this.toDate.getValue()),
                    start:0,
                    limit:(this.pP.combo == undefined) ? 15 : this.pP.combo.value
                }
            });
        }
    },
    createTBar: function() {        
        this.tbarArray = [];
        this.bBarArr = [];
        
        this.frmDate = new Wtf.form.DateField({
            emptyText:'From date...',
            width : 90,
            allowBlank:false,
            scope: this,
            format:WtfGlobal.getOnlyDateFormat(),
            value:WtfGlobal.getDates(true),
            name : 'frmdate'
        });
        this.frmDate.on('change',this.setParamertsForExport,this);
        this.toDate = new Wtf.form.DateField({
            emptyText:'To date...',
            allowBlank:false,
            width : 90,
            scope: this,
            value:WtfGlobal.getDates(false),
            name : 'todate',
            format:WtfGlobal.getOnlyDateFormat()
        });
        this.toDate.on('change',this.setParamertsForExport,this);
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"),
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchData
        });
        this.tbarArray.push(WtfGlobal.getLocaleText("acc.nee.FromDate")+":",this.frmDate,"-",WtfGlobal.getLocaleText("acc.nee.ToDate")+":",this.toDate,"-",this.fetchBttn);
        
        this.exportButton=new Wtf.exportButton({
            obj: this,
            id: "exportReports" + this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), 
            scope: this,
            disabled:false,   
            filename: this.action=="SaleReg"? WtfGlobal.getLocaleText("acc.field.ColoumnerSalesRegisters")+"" : this.action=="PurchaseReg"? WtfGlobal.getLocaleText("acc.field.ColoumnerPurchaseRegisters")+"":"",
            menuItem: {
                csv: true, 
                pdf: true, 
                rowPdf: false, 
                xls: true
            },
            get: this.action=="SaleReg"? Wtf.autoNum.columnSalesRegisterReport : this.action=="PurchaseReg"? Wtf.autoNum.columnPurchaseRegisterReprot:""
        });
        
        this.exportButton.setParams({
            companyid: companyid,
            startDate: WtfGlobal.convertToGenericDate(this.frmDate.getValue()),
            startdate: WtfGlobal.convertToGenericDate(this.frmDate.getValue()),
            endDate: WtfGlobal.convertToGenericEndDate(this.toDate.getValue()),
            enddate: WtfGlobal.convertToGenericEndDate(this.toDate.getValue()),
            id:this.id
        });
        
        this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            disabled:false,
            tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
            filename: this.action=="SaleReg"? WtfGlobal.getLocaleText("acc.field.ColoumnerSalesRegisters")+"" : this.action=="PurchaseReg"? WtfGlobal.getLocaleText("acc.field.ColoumnerPurchaseRegisters")+"":"",
            menuItem:{
                print:true
            },
            params: {
                companyid: companyid,
                stdate: WtfGlobal.convertToGenericDate(this.frmDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.toDate.getValue()),
                id:this.id
            },
            get: this.action=="SaleReg"? Wtf.autoNum.columnSalesRegisterReport : this.action=="PurchaseReg"? Wtf.autoNum.columnPurchaseRegisterReprot:""
        });
        //        this.printButton.on("click", function() {
        //            this.printButton.setParams({
        //                id:this.id,
        //                companyid: companyid,
        //                startDate: WtfGlobal.convertToGenericDate(this.frmDate.getValue()),
        //                endDate: WtfGlobal.convertToGenericEndDate(this.toDate.getValue()),
        //                reportId:Wtf.autoNum.columnPurchaseRegisterReprot
        //            });
        //        },this);
        
        this.bBarArr.push(this.exportButton);
        this.bBarArr.push(this.printButton);
        
    },
    createGrid: function() {

        this.columnStore = new Wtf.data.GroupingStore({
            url: this.action=="SaleReg"?"ACCInvoiceCMN/getColoumnerSalesRegisterDetails.do":this.action=="PurchaseReg"?"ACCGoodsReceiptCMN/getColoumnerPurchaseRegisterDetails.do":"",
            reader:  new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
        
        this.groupStore = new Wtf.data.GroupingStore({
            groupField: ['invoiceno'],
            sortInfo: {
                field: 'invoiceno',
                direction: "ASC"
            }
        });
        this.columnPurchaseSummary = new Wtf.grid.GroupSummary({});
        this.summary = new Wtf.ux.grid.GridSummary();
        this.columnPurchaseGridSummary = new Wtf.grid.GridSummary({});
        this.grpView = new Wtf.grid.GroupingView({
            forceFit: true,
            showGroupName: false,
            enableGroupingMenu: false,
            hideGroupedColumn: false    
        });    
        
        this.grid = new Wtf.grid.GridPanel({
            layout: 'fit',
            stripeRows: true,
            region: "center",
            store: this.groupStore,
            border: false,
            loadMask: true,
            columns: [{
                dataIndex: "invoiceno"
            }],
            sm: this.sm,
            viewConfig: {
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            },
            plugins: [this.columnPurchaseSummary,this.summary],
            view: this.grpView
        });    
            
        this.columnStore.on('load', this.handleStoreOnLoad, this);
        this.columnStore.on('beforeload',this.putParams,this);
    }
})
