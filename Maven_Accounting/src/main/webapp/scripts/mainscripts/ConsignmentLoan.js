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

Wtf.account.TransactionListPanelViewConsignmentLoan = function(config) {
    this.isConsignmentLoanOutstadingReport = (config.isConsignmentLoanOutstadingReport != null && config.isConsignmentLoanOutstadingReport != undefined)? config.isConsignmentLoanOutstadingReport : false;
    Wtf.apply(this, config);
    this.uPermType=Wtf.UPerm.consignmentsales;
    this.permType= Wtf.Perm.consignmentsales;   
    this.exportPermType=this.isConsignmentLoanOutstadingReport?this.permType.exportoutloan:this.permType.exportloan;

    Wtf.account.TransactionListPanelViewConsignmentLoan.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.TransactionListPanelViewConsignmentLoan,Wtf.Panel, {
    onRender: function(config) {
        Wtf.account.TransactionListPanelViewConsignmentLoan.superclass.onRender.call(this, config);
        Wtf.Ajax.requestEx({
            url: "ACCAccountCMN/getProductCustomFieldsToShow.do"
//            params: {
//                mode: 111,
//                masterid: masterid,
//                isShowCustColumn: true
//            }
        }, this, function (request, response) {
            var customProductField = request.data;
            this.GridRec = Wtf.data.Record.create([
                {name: 'country'},
                {name: 'consigneeName'},
                {name: 'deliverToParty'},
                {name: 'contactperson'},
                {name: 'documentReferenceNo'},
                {name: 'sequence'},
                {name: 'productID'},
                {name: 'productName'},
                {name: 'description'},
                {name: 'quantity'},
                {name: 'batch'},
                {name: 'serialNo'},
                {name: 'stockType'},
                {name: 'returnNo'},
                {name: 'returnqty'},
                {name: 'closedqty'},
                {name: 'dateOfReturn'},
                {name: 'purposeOfLoan'},
                {name: 'dndate' ,type: 'date'},
                {name: 'srcwarehouse'},
                {name: 'srclocation'},
                {name: 'loanfrmdate'},
                {name: 'costcenter'},
                {name: 'salesperson'},
                {name: 'remark'},
                {name: 'dnremark'},
                {name: 'itemasset'},
                {name: 'remarkormemo'},
                {name: 'dnremarkormemo'},
                {name: 'loanDueDate', type: 'date'}
            ]);

            this.consignmentLoanStore = new Wtf.data.Store({
                url: "ACCInvoiceCMN/getConsignmentLoanDetails.do",
                reader: new Wtf.data.KwlJsonReader({
                    totalProperty: "totalCount",
                    root: "data"
                },this.GridRec)
            });
            this.updateStoreConfig(customProductField);
            this.consignmentLoanStore.on('beforeload', function() {
                 WtfGlobal.setAjaxTimeOut();
                var fromdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
                var todate = WtfGlobal.convertToGenericDate(this.endDate.getValue());

                this.consignmentLoanStore.baseParams.startdate = fromdate;
                this.consignmentLoanStore.baseParams.enddate = todate;
                this.consignmentLoanStore.baseParams.isConsignmentLoanOutstadingReport = this.isConsignmentLoanOutstadingReport;
                this.consignmentLoanStore.baseParams.ss=this.quickPanelSearch.getValue()!=undefined ? this.quickPanelSearch.getValue() : '' 
            },this);

            this.consignmentLoanStore.on('load', function (store) {
                WtfGlobal.resetAjaxTimeOut();
                if (this.consignmentLoanStore.getCount() == 0) {
                    this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                    this.grid.getView().refresh();

                    if (this.exportButton) {
                        this.exportButton.disable();
                    }
                    if (this.printButton)
                        this.printButton.disable();
                } else {
                    if (this.exportButton) {
                        this.exportButton.enable();
                    }
                    if (this.printButton)
                        this.printButton.enable();
                }
                this.quickPanelSearch.StorageChanged(store);
            }, this);
            
            this.consignmentLoanStore.on('loadexception', function() {
                 WtfGlobal.resetAjaxTimeOut();
            }, this);
            this.consignmentLoanStore.on('datachanged', function() {
                var p = this.pP.combo.value;
                this.quickPanelSearch.setPage(p);
            }, this);

            this.tbarArr = [];

            this.quickPanelSearch = new Wtf.KWLTagSearch({
                emptyText: WtfGlobal.getLocaleText("acc.consignmentCost.QuickSearchEmptyText"), // "Search by Document no, Description ...",
                width: 300,
                field: 'documentReferenceNo'
            });
            this.tbarArr.push(this.quickPanelSearch);

            this.resetBttn = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.common.reset"), // 'Reset',
                tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), // 'Allows you to add a new search term by clearing existing search terms.',
                id: 'btnRec' + this.id,
                scope: this,
                iconCls: getButtonIconCls(Wtf.etype.resetbutton),
                disabled: false
            });
            this.resetBttn.on('click',this.handleResetClick,this);

            this.startDate = new Wtf.ExDateFieldQtip({
                fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), // 'From',
                name: 'startdate',
                format: WtfGlobal.getOnlyDateFormat(),
                readOnly: true,
                value: WtfGlobal.getDates(true)
            });
            this.tbarArr.push('-', WtfGlobal.getLocaleText("acc.common.from"), this.startDate);

            this.endDate = new Wtf.ExDateFieldQtip({
                fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), // 'To',
                format: WtfGlobal.getOnlyDateFormat(),
                readOnly: true,
                name: 'enddate',
                value: WtfGlobal.getDates(false)
            });
            this.tbarArr.push(WtfGlobal.getLocaleText("acc.common.to"), this.endDate);

            this.fetchBttn = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
                tooltip: WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"), // "Select a time period to view corresponding transactions.",
                style: "margin-left: 6px;",
                iconCls: 'accountingbase fetch',
                scope: this,
                handler: this.fetchStatement
            });
            this.tbarArr.push(this.fetchBttn);
            this.tbarArr.push(this.resetBttn);
            
            var colArr = [new Wtf.grid.RowNumberer(), {
                    header: WtfGlobal.getLocaleText("acc.field.consigneeName"), // "Consignee Name",
                    dataIndex: "consigneeName",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.deliverToParty"), // "Deliver to Party",
                    dataIndex: "deliverToParty",
                     hidden:true,
                    renderer: function (val) {
                        val = val.replace(/(<([^>]+)>)/ig, "");
                        return "<div wtf:qtip=\"" + val + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.field.deliverToParty") + "'>" + val + "</div>";
                    },
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.contactPerson"), // "Deliver to Party",
                    dataIndex: "contactperson",
                    hidden:false,
                    renderer: function (val) {
                        val = val.replace(/(<([^>]+)>)/ig, "");
                        return "<div wtf:qtip=\"" + val + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.field.contactPerson") + "'>" + val + "</div>";
                    },
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.documentReferenceNo"), // "Document Reference No.",
                    dataIndex: "documentReferenceNo",
                    renderer: WtfGlobal.deletedRenderer,
                    sortable: true,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.DNDate"), // "Document Reference No.",
                    dataIndex: "dndate",
                    renderer: WtfGlobal.onlyDateDeletedRenderer,
                    width: 150,
                    pdfwidth: 100
                },{
                    header: WtfGlobal.getLocaleText("acc.field.sequenceNo"), // "Sequence #",
                    dataIndex: 'sequence',
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.product.gridProductID"), // "Product ID",
                    dataIndex: "productID",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"), // "Product Name",
                    dataIndex: "productName",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.description"), // "Description",
                    dataIndex: "description",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.SouceWarehouse"), // "Description",
                    dataIndex: "srcwarehouse",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.SouceLocation"), // "Description",
                    dataIndex: "srclocation",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.Asset"), // "Description",
                    dataIndex: "itemasset",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.rem.187.Mixed"), // "Qty.",
                    dataIndex: "quantity",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.lotBatch"), // "LOT/BATCH #",
                    dataIndex: "batch",
                    renderer: function (val) {
                        val = val.replace(/(<([^>]+)>)/ig, "");
                        return "<div wtf:qtip=\"" + val + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.field.lotBatch") + "'>" + val + "</div>";
                    },
                    width: 150,
                   pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.SerialNo"), // "Serial No",
                    dataIndex: "serialNo",
                    renderer: function (val) {
                        val = val.replace(/(<([^>]+)>)/ig, "");
                        return "<div wtf:qtip=\"" + val + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.field.SerialNo") + "'>" + val + "</div>";
                    },
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.stockType_ReusableOrNonReusable"), // "Stock Type (Reusable or Non Reusable)",
                    dataIndex: "stockType",
                    width: 200,
                    pdfwidth: 100,
                    renderer:function(val, m ,r){
                        if(r.get('stockType') == "R"){
                            return "<div wtf:qtip='Reusable'>"+val+"</div>"
                        }else if(r.get('stockType') == "C"){
                            return "<div wtf:qtip='Consumable'>"+val+"</div>"
                        }else {
                              return "<div wtf:qtip='Consumable'>"+val+"</div>"
                        }
                   }
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.returnNo"), // "Return No.",
                    dataIndex: "returnNo",
//                    renderer: WtfGlobal.deletedRenderer,
                    hidden: this.isConsignmentLoanOutstadingReport,
                    width: 150,
                    pdfwidth: 100,
                    renderer:function(val, m ,r){
                        if(val != "" && val != undefined){
                            return "<div wtf:qtip='"+val+"'>"+val+"</div>";
                        }else {
                            return "";
                        }
                    }
                 },{
                    header: WtfGlobal.getLocaleText("acc.accPref.returnQuant"), // "Return No.",
                    dataIndex: "returnqty",
//                    renderer: WtfGlobal.deletedRenderer,
                    hidden: this.isConsignmentLoanOutstadingReport,
                    width: 150,
                    pdfwidth: 100
                   
                }, {
                    header: WtfGlobal.getLocaleText("acc.accPref.closedQuant"), // "Return No.",
                    dataIndex: "closedqty",
//                    renderer: WtfGlobal.deletedRenderer,
                    hidden: this.isConsignmentLoanOutstadingReport,
                    width: 150,
                    pdfwidth: 100
                   
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.DateOfReturn"), // "Date of return",
                    dataIndex: "dateOfReturn",
                    //renderer: WtfGlobal.onlyDateDeletedRenderer,
                    hidden: this.isConsignmentLoanOutstadingReport,
                    width: 150,
                    pdfwidth: 100,
                    renderer:function(val, m ,r){
                        if(val != "" && val != undefined){
                            return "<div wtf:qtip='"+val+"'>"+val+"</div>";
                        }else {
                            return "";
                        }
                    }
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.purposeOfLoan"), // "Purpose of Loan",
                    dataIndex: "purposeOfLoan",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                },{
                    header: WtfGlobal.getLocaleText("acc.field.Loanfromdate"), // "Purpose of Loan",
                    dataIndex: "loanfrmdate",
                     renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.loanDueDate"), // "Loan due date",
                    dataIndex: "loanDueDate",
                    renderer: WtfGlobal.onlyDateDeletedRenderer,
                    width: 150,
                    pdfwidth: 100
                },  {
                    header: WtfGlobal.getLocaleText("acc.field.CostCenter"), // "Loan due date",
                    dataIndex: "costcenter",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                },  {
                    header: WtfGlobal.getLocaleText("acc.field.SalesPerson"), // "Loan due date",
                    dataIndex: "salesperson",
                   renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.dnRemark"), // "Loan due date",
                    dataIndex: "dnremark",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.dnRemarkMemo"),
                    dataIndex: "dnremarkormemo",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.srRemark"), // "Loan due date",
                    dataIndex: "remark",
                    renderer: WtfGlobal.deletedRenderer,
                    hidden: this.isConsignmentLoanOutstadingReport,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.srRemarkMemo"),
                    dataIndex: "remarkormemo",
                    hidden: this.isConsignmentLoanOutstadingReport,
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }
                ,{
                header:"Country", // "Loan due date",
                dataIndex: "country",
                renderer: WtfGlobal.deletedRenderer,
                width: 150,
                pdfwidth: 100
            }
            ]
            
            if(customProductField && customProductField.length>0) {
                for(var ccnt=0; ccnt<customProductField.length; ccnt++) {
                    colArr.push({
                        header : customProductField[ccnt].columnname,
                        dataIndex: customProductField[ccnt].dataindex,
                        width: 50,
                        hidden:(customProductField[ccnt].dataindex=="Custom_Country Of Origin"||customProductField[ccnt].dataindex=="Custom_Country of Origin")?true:false,
                        pdfwidth: 50,
                        align: 'center'
                    })
                }
            }
            
            var bottombtnArr = [];
            this.exportButton = new Wtf.exportButton({
                obj: this,
                id: "exportReports"+this.id,
                text: WtfGlobal.getLocaleText("acc.common.export"),
                tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), // 'Export report details',
                disabled: true,
                scope: this,
                menuItem: {csv:true, pdf:true, rowPdf:false,xls:true},
                params:{
                    isConsignmentLoanOutstadingReport:this.isConsignmentLoanOutstadingReport
                },
                get: Wtf.autoNum.loanreport
            });
            bottombtnArr.push('-');
            
             this.printButton=new Wtf.exportButton({
                text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
                obj:this,
                tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
                filename : this.isConsignmentLoanOutstadingReport?WtfGlobal.getLocaleText("acc.field.consignmentLoanOutstandingReport"):WtfGlobal.getLocaleText("acc.field.consignmentLoanReport"),
                label:this.isConsignmentLoanOutstadingReport?WtfGlobal.getLocaleText("acc.field.consignmentLoanOutstandingReport"):WtfGlobal.getLocaleText("acc.field.consignmentLoanReport"),
                params:{
                    stdate:WtfGlobal.convertToGenericStartDate(WtfGlobal.getDates(true)),
                    enddate:WtfGlobal.convertToGenericEndDate(WtfGlobal.getDates(false)),
                    ss:this.quickPanelSearch.getValue()!=undefined ? this.quickPanelSearch.getValue() : '' 
                },
                menuItem:{print:true},
                get: Wtf.autoNum.loanreport
            });
          
            
            if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType))
            {
                bottombtnArr.push(this.exportButton);
            }
            bottombtnArr.push(this.printButton);
            
            this.grid = new Wtf.grid.GridPanel({    
                store: this.consignmentLoanStore,
                border: false,
                layout: 'fit',
                viewConfig: {
                    forceFit: false
                },
                loadMask: true,
                columns: colArr,
                bbar: []
            });


            this.leadpan = new Wtf.Panel({
                layout: 'border',
                border: false,
                attachDetailTrigger: true,
                items: [{
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid],
                    tbar: this.tbarArr,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.consignmentLoanStore,
                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({
                            id : "pPageSize_"+this.id
                    }),
                      items : bottombtnArr
                    })
                }]
            }); 
            this.add(this.leadpan);
            this.doLayout();
            this.consignmentLoanStore.load({
                params: {
                    start: 0,
                    limit: 30
                }
            });
        }, function () {

        });
    },
    
    handleResetClick:function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.startDate.reset();
            this.endDate.reset();
            this.fetchStatement();
        }
    },
    
    fetchStatement:function() {
        this.sDate = this.startDate.getValue();
        this.eDate = this.endDate.getValue();
        
        if (this.sDate > this.eDate) {
            WtfComMsgBox(1,2);
            return;
        }
        
        var fromdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        var todate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        
        this.consignmentLoanStore.load({
            params: {
                startdate: fromdate,
                enddate: todate,
                start: 0,
                limit: this.pP.combo.value
            }
        });
    },
    
    updateStoreConfig: function (customProductField) {
        for (var cnt = 0; cnt < customProductField.length; cnt++) {
            var fieldname = customProductField[cnt].dataindex;
            var newField = new Wtf.data.Field({
                name: fieldname
            });
            this.consignmentLoanStore.fields.items.push(newField);
            this.consignmentLoanStore.fields.map[fieldname] = newField;
            this.consignmentLoanStore.fields.keys.push(fieldname);
        }
        this.consignmentLoanStore.reader = new Wtf.data.KwlJsonReader(this.consignmentLoanStore.reader.meta, this.consignmentLoanStore.fields.items);
    }
    
});

// Consignment Request on Loan Report

function getStockRequestOnLoanReport(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentsales, Wtf.Perm.consignmentsales.viewconsignmentloan)) {
        var mainTabId = Wtf.getCmp("as");
        var stockloanReportTab = Wtf.getCmp("stockReqOnLoanReportTab");
        if(stockloanReportTab == null){
            stockloanReportTab = new Wtf.StockRequestOnLoanReport({
                layout:"fit",
                title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.requestOnLoanReport"), Wtf.TAB_TITLE_LENGTH), 
                tabTip: WtfGlobal.getLocaleText("acc.field.requestOnLoanReport"),
                closable:true,
                border:false,
//                iconCls:getButtonIconCls(Wtf.etype.inventoryqa),
                id:"stockReqOnLoanReportTab"
            });
            mainTabId.add(stockloanReportTab);
        }
        mainTabId.setActiveTab(stockloanReportTab);
        mainTabId.doLayout();
    }else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
    }
}


Wtf.StockRequestOnLoanReport = function (config){
    Wtf.apply(this,config);
    Wtf.StockRequestOnLoanReport.superclass.constructor.call(this);
}
Wtf.extend(Wtf.StockRequestOnLoanReport,Wtf.Panel,{
    onRender:function (config) {
        Wtf.StockRequestOnLoanReport.superclass.onRender.call(this,config);
        this.getTabpanel();
        this.add(this.tabPanel);
    },
    getTabpanel:function (){
        this.getReqLoanReport();
        
        this.itemsarr = [];
        
        this.itemsarr.push(this.CRLoanReport);
            
        this.tabPanel = new Wtf.TabPanel({
            activeTab:0,
            id:"stockreqonloantab",
            items:this.itemsarr
        });
    },
    
    getReqLoanReport:function (){
        this.CRLoanReport =new Wtf.StockRequestOnLoanReport({
            id:"cr_onloanReport",
            layout:'fit',
            title:WtfGlobal.getLocaleText("acc.field.requestOnLoanReport"),
            iconCls: 'accountingbase salesorder',
            border:false
        });
    }

});


Wtf.StockRequestOnLoanReport = function(config){
    Wtf.StockRequestOnLoanReport.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.StockRequestOnLoanReport, Wtf.Panel, {
    onRender: function(config) {
        Wtf.StockRequestOnLoanReport.superclass.onRender.call(this, config);
         Wtf.Ajax.requestEx({
            url: "ACCAccountCMN/getProductCustomFieldsToShow.do"
//            params: {
//                mode: 111,
//                masterid: masterid,
//                isShowCustColumn: true
//            }
        }, this, function (request, response) {
             var customProductField = request.data;
        this.fromdateVal =new Date().getFirstDateOfMonth();
        this.fromdateVal.setDate(new Date().getFirstDateOfMonth().getDate());
        this.frmDate = new Wtf.form.DateField({
            emptyText:'From date...',
            readOnly:true,
            width : 85,
            value:WtfGlobal.getDates(true),
            minValue: Wtf.archivalDate,
            name : 'frmdate',
            format: 'Y-m-d'
        });
        this.todateVal=new Date().getLastDateOfMonth();
        this.todateVal.setDate(new Date().getLastDateOfMonth().getDate());
        this.toDate = new Wtf.form.DateField({
            emptyText:'To date...',
            readOnly:true,
            width : 85,
            name : 'todate',
            value:WtfGlobal.getDates(false),
            minValue: Wtf.archivalDate,
            format: 'Y-m-d'
        });
       
        
        this.docTypeCmbStore = new Wtf.data.SimpleStore({
            fields:["id", "name"],
            data : [["", "All"],["Request", "Request"],["Stock", "Stock"],["DO", "DO"]]
        });
        this.docTypeCmbfilter = new Wtf.form.ComboBox({
            hiddenName : 'doctype',
            store : this.docTypeCmbStore,
            typeAhead:true,
            readOnly: false,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 110,
            triggerAction: 'all',
            emptyText:'Select document type...'
        });      
        
//        this.customerCmbRecord = new Wtf.data.Record.create([
//        {
//            name: 'accid'
//        },
//
//        {
//            name: 'accname'
//        }
//        ]);
//
//        this.customerCmbStore = new Wtf.data.Store({
//            url:"ACCCustomer/getCustomersForCombo.do",
//            baseParams:{
//                mode:2,
//                group:10,
//                deleted:false,
//                nondeleted:true,
//                common:'1'
//            },
//            reader: new Wtf.data.KwlJsonReader({
//                root: 'data'
//            },this.customerCmbRecord)
//        });
//        
//        this.customerCmbStore.on("load", function(ds, rec, o){
//            if(rec.length > 1){
//                var newRec=new this.customerCmbRecord({
//                    accid:'',
//                    accname:'ALL'
//                })
//                this.customerCmbStore.insert(0,newRec);
//                this.customerCmbStore.setValue('');
//            }else if(rec.length > 0){
//                this.customerCmbfilter.setValue(rec[0].data.accid, true);
//            }
//        }, this);
//            
//        this.docTypeCmbfilter = new Wtf.form.ComboBox({
//            fieldLabel : 'Customer*',
//            hiddenName : 'customerid',
//            store : this.customerCmbStore,
//            forceSelection:true,
//            displayField:'accname',
//            valueField:'accid',
//            mode: 'local',
////            width : 150,
//            listWidth:200, 
//            triggerAction: 'all',
//            emptyText:'Select Customer...',
//            typeAhead:true
//        });
//        this.customerCmbStore.load();  
        this.search = new Wtf.Button({
            anchor: '90%',
            text: WtfGlobal.getLocaleText("acc.common.search"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.advancesearch.searchBTN.ttip")
            },
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: function() {
//                this.initloadgridstore(this.frmDate.getValue(),this.toDate.getValue(),this.docTypeCmbfilter.getValue(),"");
                this.initloadgridstore(this.frmDate.getValue(),this.toDate.getValue(),"","");
            }
        });
        this.resetQAapproval = new Wtf.Button({
            anchor : '90%',
            text: WtfGlobal.getLocaleText("acc.inventory.QAAproval.ResetFilter"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.inventory.QAAproval.Clickheretoresetfilter")
            },
            iconCls:getButtonIconCls(Wtf.etype.resetbutton),
            scope:this,
            handler:function(){
                this.initloadgridstore(this.fromdateVal,this.todateVal,"","");
                Wtf.getCmp("Quick"+this.grid.id).reset();
                this.frmDate.setValue(this.fromdateVal);
                this.toDate.setValue(this.todateVal);
//                this.docTypeCmbfilter.setValue('');
            }
        });
        
         this.exportBttn = new Wtf.exportButton({
            obj: this,
            //            id: 'stocktransferregisterexportid',
            tooltip: WtfGlobal.getLocaleText("acc.cosignmentloan.ExportReport"), //"Export Report details.",  
            menuItem:{
                csv:true,
                pdf:true,
                xls:true
            },
            get:Wtf.autoNum.StockRequestOnLoanReport,
            label:"Export"

        })
        
        var bbarArray=new Array();
        bbarArray.push("-",this.exportBttn);
        
        var tbarArray = [];
//        tbarArray.push("From Date: ",this.frmDate,"-","To Date: ",this.toDate,"-","Document Type:",this.docTypeCmbfilter,"-",this.search,this.resetQAapproval);
        tbarArray.push(WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.fromdate")+" : ",this.frmDate,"-",WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.todate")+" : ",this.toDate,"-",this.search,this.resetQAapproval);
       
        this.record = Wtf.data.Record.create([
        {
            "name":"id"
        },
        {
            "name":"transactionno"
        },
        {
            "name":"productid"
        },
        {
            "name":"productdescription"
        },
        {
            "name":"documenttype"
        },
        {
            "name":"costcenter"
        },
        {
            "name":"createdon"
        },
        {
            "name":"salesperson"
        },
        {
            "name":"status"
        },
        {
            "name":"warehouse"
        },
        {
            "name":"location"
        },
        {
            "name":"assetno"
        },
        {
            "name":"requestqunatity"
        },
        {
            "name":"stockquantity"
        },
        {
            "name":"donumber"
        },
        {
            "name":"doquantity"
        },
        {
            "name":"loanquantity"
        },
        {
            "name":"customer"//customer
        },
        {
            "name":"purpose"
        },
        {
            "name":"country"
        },
        {
            "name":"ccountry"
        },
        {
            "name":"fromdate"
        },
        {
            "name":"todate"
        },
        {
            "name":"transactiontype"
        },
        {
            "name":"remarkormemo"
        }

        ]);
        
        
        
        this.ds = new Wtf.data.Store({
             url: 'ACCSalesReturnCMN/getStockRequestOnLoanReport.do',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },
            this.record
            )
        });
        
      
        this.ds.on("load",function(store,rec,opt){
            this.grid.getView().refresh;
        },this);

        
        this.updateStoreConfig(customProductField);
       var cmDefaultWidth = 125;
       this.colArr=[];
        this.colArr.push(
            new Wtf.KWLRowNumberer(), //0
            {
                header:WtfGlobal.getLocaleText("acc.inventory.QAAproval.ReferenceNoId"), //2
                dataIndex:'id',
                hidden:true
            },
            {
                header:WtfGlobal.getLocaleText("acc.cimb.refNo"), //2 Transaction No
                dataIndex:'transactionno',
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            },
            {
                header:WtfGlobal.getLocaleText("acc.contractMasterGrid.header8"), 
                dataIndex:'productid',
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            },
            {
                header:WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"), 
                dataIndex:'productdescription',
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            },
            {
                header:WtfGlobal.getLocaleText("acc.field.DocumentType"), 
                dataIndex:'documenttype',
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            },
            {
                header:WtfGlobal.getLocaleText("acc.field.CostCenter"), 
                dataIndex:'costcenter',
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            },
            {
                header:WtfGlobal.getLocaleText("acc.common.reportcreatedon"), 
                dataIndex:'createdon',
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            },
            {
                header:WtfGlobal.getLocaleText("acc.service.tax.report.vch.type"), 
                dataIndex:'transactiontype',
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            },
            {
                header:WtfGlobal.getLocaleText("acc.invoiceList.salesPerson"),
                dataIndex:'salesperson',
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            },
            {
                header: WtfGlobal.getLocaleText("acc.GIRO.Status"),
                dataIndex: 'status',
                groupable: true,
                width:250,
                pdfwidth:50,
                renderer:function(value){
                    if(value=="Approved"){
                        return "<label style = 'color : green;'>Approved</label>";
                    }else if(value=="Completed"){
                        return "<label style = 'color : blue;'>Completed</label>";
                    }else if(value=="Rejected"){
                        return "<label style = 'color : red;'>Rejected</label>";
                    }else{
                        return value;
                    }
                }
            },
            {
                header:WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"), 
                dataIndex:'warehouse',
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            },
            {
                header:WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp.add"), 
                dataIndex:'location',
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            },
            {
                header:WtfGlobal.getLocaleText("acc.stockrepair.AssetNo"), 
                dataIndex:'assetno',
                groupable: false,
                width:cmDefaultWidth,
                fixed:true,
                pdfwidth:50,
                hidden:true
            },
            
            {
                header:WtfGlobal.getLocaleText("acc.cosignmentloan.RequestQuantity"), 
                dataIndex:'requestqunatity',
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            },
          
            
            {
                    header:WtfGlobal.getLocaleText("acc.cosignmentloan.Stock"), 
                    dataIndex:'stockquantity',
                    groupable: false,
                    width:cmDefaultWidth,
                    pdfwidth:50,
                    hidden:false,
                    fixed:true,
                    renderer:function(v,m,r){
                    {
                        return parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                    }
                    }
                },
          
            {
                header:'Delivery Order Number',
                dataIndex:"donumber", 
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            },
            
            {
                header:'Delivery Quantity',
                dataIndex:"doquantity", 
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            },
          
             {
                header:WtfGlobal.getLocaleText("acc.field.consigneeName"), 
                dataIndex:'customer',
                groupable: false,
                width:250,
                pdfwidth:50,
                fixed:true
            },
             {
                header:"Country", 
                dataIndex:'ccountry',
                groupable: false,
                width:250,
                pdfwidth:50,
                fixed:true
            },
             {
                  header:WtfGlobal.getLocaleText("acc.field.Purpose"),
                dataIndex:'purpose',
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true,
                renderer: WtfGlobal.deletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.fromdate"), 
                dataIndex:'fromdate',
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            },
             {
                header:WtfGlobal.getLocaleText("acc.field.loanDueDate"), 
                dataIndex:'todate',
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            }
            
           
           );
        
         if(customProductField && customProductField.length>0) {
                for(var ccnt=0; ccnt<customProductField.length; ccnt++) {
                    this.colArr.push({
                        header : customProductField[ccnt].columnname,
                        dataIndex: customProductField[ccnt].dataindex,
                        width: 100,
                        hidden:(customProductField[ccnt].dataindex=="Custom_Country Of Origin"||customProductField[ccnt].dataindex=="Custom_Material Group")?false:true,
                        pdfwidth: 50,
                        align: 'center'
                    })
                }
            }

        this.cm = new Wtf.grid.ColumnModel(this.colArr), 

        /**************date *****/


        this.summary = new Wtf.grid.GroupSummary({});
        
        this.grid=new Wtf.KwlEditorGridPanel({
            id:"consignmentQAReportGrid"+this.id,
            cm:this.cm,
            store:this.ds,
            loadMask:true,
            tbar:tbarArray,
            viewConfig: {
                forceFit: false
            },
            searchLabel:WtfGlobal.getLocaleText("acc.het.806"),
            searchLabelSeparator:":",
            searchEmptyText: WtfGlobal.getLocaleText("acc.inventory.QAAproval.TransactionNoProductCode"),
            serverSideSearch:true,
            qsWidth:200,
            displayInfo: true,
            displayMsg: 'Displaying  {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
            bbar: bbarArray
        });
        this.grid.on("cellclick",this.cellClick,this);
        
        Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
            Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
        },this);
        

       
      //        var colModelArray = GlobalColumnModelForReports[this.moduleid];
//        this.updateStoreConfig(colModelArray, this.ds);

       
        this.add(this.grid);
        this.grid.doLayout();
        this.doLayout();
        
        //        this.loadgrid();
        
//        this.on("activate",function(){
//            this.loadgridstore(this.frmDate.getValue(),this.toDate.getValue(),this.docTypeCmbfilter.getValue(),"");
            this.loadgridstore(this.frmDate.getValue(),this.toDate.getValue(),"","");
//        },this);
    }, function () {

        })
    },
   
    initloadgridstore:function(frm, to,documenttype,status){
        this.ds.baseParams = {
//            type:type,
            documenttype:documenttype,
            frmDate:frm.format('Y-m-d'),
            toDate:to.format('Y-m-d')
        }
        this.ds.load({
            params:{
                start:0,
                  limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
//                limit:30,
                ss:  Wtf.getCmp("Quick"+this.grid.id).getValue()
            }
        });
    },
    loadgridstore:function(frm, to,documenttype,status){
        this.ds.baseParams = {
            documenttype:documenttype,
            frmDate:frm.format('Y-m-d'),
            toDate:to.format('Y-m-d')
        }
        this.ds.load({
            params:{
                start:0,
                limit:30,
                ss:  Wtf.getCmp("Quick"+this.grid.id).getValue()
            }
        });
    },
     updateStoreConfig: function (customProductField) {
        for (var cnt = 0; cnt < customProductField.length; cnt++) {
            var fieldname = customProductField[cnt].dataindex;
            var newField = new Wtf.data.Field({
                name: fieldname
            });
            this.ds.fields.items.push(newField);
            this.ds.fields.map[fieldname] = newField;
            this.ds.fields.keys.push(fieldname);
        }
        this.ds.reader = new Wtf.data.KwlJsonReader(this.ds.reader.meta, this.ds.fields.items);
    }
   
});

function getConsignmentReturnListTabView(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentsales, Wtf.Perm.consignmentsales.viewconsignmentloan)) {
        var mainTabId = Wtf.getCmp("as");
        var stockloanReportTab = Wtf.getCmp("ConsignmentReturnListRptTab");
        if(stockloanReportTab == null){
            stockloanReportTab = new Wtf.ConsignmentReturnList({
                layout:"fit",
                title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.ConsignmentReturnListRpt"), Wtf.TAB_TITLE_LENGTH), 
                tabTip: WtfGlobal.getLocaleText("acc.field.ConsignmentReturnListRpt"),
                closable:true,
                border:false,
                //                iconCls:getButtonIconCls(Wtf.etype.inventoryqa),
                id:"ConsignmentReturnListRptTab"
            });
            mainTabId.add(stockloanReportTab);
        }
        mainTabId.setActiveTab(stockloanReportTab);
        mainTabId.doLayout();
    }else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
    }
}
Wtf.ConsignmentReturnList = function (config){
    Wtf.apply(this,config);
    Wtf.ConsignmentReturnList.superclass.constructor.call(this);
}
Wtf.extend(Wtf.ConsignmentReturnList,Wtf.Panel,{
    onRender:function (config) {
        Wtf.ConsignmentReturnList.superclass.onRender.call(this,config);
        this.getTabpanel();
        this.add(this.tabPanel);
    },
    getTabpanel:function (){
        this.ConsignmentReturnListRpt();
        
        this.itemsarr = [];
        
        this.itemsarr.push(this.CRReport);
            
        this.tabPanel = new Wtf.TabPanel({
            activeTab:0,
            id:"consignmentreturntab",
            items:this.itemsarr
        });
    },
    
    ConsignmentReturnListRpt:function (){
        this.CRReport =new Wtf.ConsignmentReturnListRepot({
            id:"cr_onloanReport",
            layout:'fit',
            title:WtfGlobal.getLocaleText("acc.field.ConsignmentReturnListRpt"),
            iconCls: 'accountingbase salesorder',
            border:false
        });
    }

});
Wtf.ConsignmentReturnList = function(config){
    Wtf.ConsignmentReturnList.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.ConsignmentReturnList, Wtf.Panel, {
    onRender: function(config) {
        Wtf.ConsignmentReturnList.superclass.onRender.call(this, config);
        Wtf.Ajax.requestEx({
            url: "ACCAccountCMN/getProductCustomFieldsToShow.do"
        //            params: {
        //                mode: 111,
        //                masterid: masterid,
        //                isShowCustColumn: true
        //            }
        }, this, function (request, response) {
            var customProductField = request.data;
            this.fromdateVal =new Date().getFirstDateOfMonth();
            this.fromdateVal.setDate(new Date().getFirstDateOfMonth().getDate());
            this.frmDate = new Wtf.form.DateField({
                emptyText:'From date...',
                readOnly:true,
                width : 85,
                value:WtfGlobal.getDates(true),
                minValue: Wtf.archivalDate,
                name : 'frmdate',
                format: 'Y-m-d'
            });
            this.todateVal=new Date().getLastDateOfMonth();
            this.todateVal.setDate(new Date().getLastDateOfMonth().getDate());
            this.toDate = new Wtf.form.DateField({
                emptyText:'To date...',
                readOnly:true,
                width : 85,
                name : 'todate',
                value:WtfGlobal.getDates(false),
                minValue: Wtf.archivalDate,
                format: 'Y-m-d'
            });
       
        
            this.docTypeCmbStore = new Wtf.data.SimpleStore({
                fields:["id", "name"],
                data : [["", "All"],["Request", "Request"],["Stock", "Stock"],["DO", "DO"]]
            });
            this.docTypeCmbfilter = new Wtf.form.ComboBox({
                hiddenName : 'doctype',
                store : this.docTypeCmbStore,
                typeAhead:true,
                readOnly: false,
                displayField:'name',
                valueField:'id',
                mode: 'local',
                width : 110,
                triggerAction: 'all',
                emptyText:'Select document type...'
            });      

            this.search = new Wtf.Button({
                anchor: '90%',
                text: WtfGlobal.getLocaleText("acc.common.search"),
                tooltip: {
                    text:WtfGlobal.getLocaleText("acc.advancesearch.searchBTN.ttip")
                },
                iconCls: 'accountingbase fetch',
                scope: this,
                handler: function() {
                    //                this.initloadgridstore(this.frmDate.getValue(),this.toDate.getValue(),this.docTypeCmbfilter.getValue(),"");
                    this.initloadgridstore(this.frmDate.getValue(),this.toDate.getValue(),"","");
                }
            });
            this.resetQAapproval = new Wtf.Button({
                anchor : '90%',
                text: WtfGlobal.getLocaleText("acc.inventory.QAAproval.ResetFilter"),
                tooltip: {
                    text:WtfGlobal.getLocaleText("acc.inventory.QAAproval.Clickheretoresetfilter")
                },
                iconCls:getButtonIconCls(Wtf.etype.resetbutton),
                scope:this,
                handler:function(){
                    this.initloadgridstore(this.fromdateVal,this.todateVal,"","");
                    this.frmDate.setValue(this.fromdateVal);
                    this.toDate.setValue(this.todateVal);
                //                this.docTypeCmbfilter.setValue('');
                }
            });
        
            this.exportBttn = new Wtf.exportButton({
                obj: this,
                //            id: 'stocktransferregisterexportid',
                tooltip: WtfGlobal.getLocaleText("acc.cosignmentloan.ExportReport"), //"Export Report details.",  
                menuItem:{
                    csv:true,
                    pdf:true,
                    xls:true
                },
                get:Wtf.autoNum.ConsignmentReturnList,
                label:"Export"

            })
        
            var bbarArray=new Array();
            bbarArray.push("-",this.exportBttn);
        
            var tbarArray = [];
            //        tbarArray.push("From Date: ",this.frmDate,"-","To Date: ",this.toDate,"-","Document Type:",this.docTypeCmbfilter,"-",this.search,this.resetQAapproval);
            tbarArray.push(WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.fromdate")+" : ",this.frmDate,"-",WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.todate")+" : ",this.toDate,"-",this.search,this.resetQAapproval);
       
            this.record = Wtf.data.Record.create([
        
            {
                    name: 'documentReferenceNo'
                },

                {
                    name: 'dateOfReturn',
                    type: 'date'
                },

                {
                    name: 'consigneeName'
                },

                {
                    name: 'contactperson'
                },

                {
                    name: 'dnnumber'
                },

                {
                    name: 'dndate' ,
                    type: 'date'
                },

                {
                    name: 'sequence'
                },

                {
                    name: 'productID'
                },

                {
                    name: 'productName'
                },

                {
                    name: 'description'
                },

                {
                    name: 'srcwarehouse'
                },

                {
                    name: 'srclocation'
                },

                {
                    name: 'itemasset'
                },

                {
                    name: 'quantity'
                },

                {
                    name: 'batch'
                },

                {
                    name: 'serialNo'
                },

                {
                    name: 'reusable'
                },

                {
                    name: 'purposeOfLoan'
                },

                {
                    name: 'loanfrmdate',
                    type: 'date'
                },

                {
                    name: 'loanDueDate', 
                    type: 'date'
                },

                {
                    name: 'costcenter'
                },

                {
                    name: 'salesperson'
                },

                {
                    name: 'dnremark'
                },

                {
                    name: 'dnremarkormemo'
                },

                {
                    name: 'remark'
                },

                {
                    name: 'remarkormemo'
                },

                {
                    name: 'country'
                },
        
                ]);
        
        
        
            this.ds = new Wtf.data.Store({
                url: 'ACCInvoiceCMN/getConsignmentReturnDetails.do',
                reader: new Wtf.data.KwlJsonReader({
                    root: 'data',
                    totalProperty:'count'
                },
                this.record
                )
            });
            
           
        
            this.ds.on('beforeload', function() {
                WtfGlobal.setAjaxTimeOut();
                var fromdate = WtfGlobal.convertToGenericDate(this.frmDate.getValue());
                var todate = WtfGlobal.convertToGenericDate(this.toDate.getValue());

                this.ds.baseParams.startdate = fromdate;
                this.ds.baseParams.enddate = todate;
                this.ds.baseParams.ss= Wtf.getCmp("Quick"+this.grid.id).getValue()
            },this);
            this.ds.on("load",function(store,rec,opt){
                this.grid.getView().refresh;
            },this);

        
            this.updateStoreConfig(customProductField);
            var cmDefaultWidth = 125;
            this.colArr=[];
            this.colArr.push(
                new Wtf.KWLRowNumberer(), //0
                 {
                    header: WtfGlobal.getLocaleText("acc.field.ReturnDocRef"), // "Document Reference No.",
                    dataIndex: "documentReferenceNo",
                    renderer: WtfGlobal.deletedRenderer,
                    sortable: true,
                    width: 150,
                    pdfwidth: 100
                },
                {
                    header: WtfGlobal.getLocaleText("acc.field.DateOfReturn"), // "Date of return",
                    dataIndex: "dateOfReturn",
                    renderer: WtfGlobal.onlyDateDeletedRenderer,
                    hidden: this.isConsignmentLoanOutstadingReport,
                    width: 150,
                    pdfwidth: 100
//                    renderer:function(val, m ,r){
//                        if(val != "" && val != undefined){
//                            return "<div wtf:qtip='"+val+"'>"+val+"</div>";
//                        }else {
//                            return "";
//                        }
//                    }
                },
                {
                    header: WtfGlobal.getLocaleText("acc.field.consigneeName"), // "Consignee Name",
                    dataIndex: "consigneeName",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                } , {
                    header: WtfGlobal.getLocaleText("acc.field.contactPerson"), // "Deliver to Party",
                    dataIndex: "contactperson",
                    hidden:false,
                    renderer: function (val) {
                        val = val.replace(/(<([^>]+)>)/ig, "");
                        return "<div wtf:qtip=\"" + val + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.field.contactPerson") + "'>" + val + "</div>";
                    },
                    width: 150,
                    pdfwidth: 100
                },
                {
                    header: WtfGlobal.getLocaleText("acc.field.DNDocumentRef#"), // "Return No.",
                    dataIndex: "dnnumber",
                    //                    renderer: WtfGlobal.deletedRenderer,
                    hidden: this.isConsignmentLoanOutstadingReport,
                    width: 150,
                    pdfwidth: 100,
                    renderer:function(val, m ,r){
                        if(val != "" && val != undefined){
                            return "<div wtf:qtip='"+val+"'>"+val+"</div>";
                        }else {
                            return "";
                        }
                    }
                },
                {
                    header: WtfGlobal.getLocaleText("acc.field.DNDate"), // "Document Reference No.",
                    dataIndex: "dndate",
                    renderer: WtfGlobal.onlyDateDeletedRenderer,
                    width: 150,
                    pdfwidth: 100
                },{
                    header: WtfGlobal.getLocaleText("acc.field.sequenceNo"), // "Sequence #",
                    dataIndex: 'sequence',
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.product.gridProductID"), // "Product ID",
                    dataIndex: "productID",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"), // "Product Name",
                    dataIndex: "productName",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.description"), // "Description",
                    dataIndex: "description",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.SouceWarehouse"), // "Description",
                    dataIndex: "srcwarehouse",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.SouceLocation"), // "Description",
                    dataIndex: "srclocation",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.Asset"), // "Description",
                    dataIndex: "itemasset",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.rem.187.Mixed"), // "Qty.",
                    dataIndex: "quantity",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.lotBatch"), // "LOT/BATCH #",
                    dataIndex: "batch",
                    renderer: function (val) {
                        val = val.replace(/(<([^>]+)>)/ig, "");
                        return "<div wtf:qtip=\"" + val + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.field.lotBatch") + "'>" + val + "</div>";
                    },
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.SerialNo"), // "Serial No",
                    dataIndex: "serialNo",
                    renderer: function (val) {
                        val = val.replace(/(<([^>]+)>)/ig, "");
                        return "<div wtf:qtip=\"" + val + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.field.SerialNo") + "'>" + val + "</div>";
                    },
                    width: 150,
                    pdfwidth: 100
                }, 
                {
                    header: WtfGlobal.getLocaleText("acc.field.stockType_ReusableOrNonReusable"), // "Purpose of Loan",
                    dataIndex: "reusable",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                },
                {
                    header: WtfGlobal.getLocaleText("acc.field.purposeOfLoan"), // "Purpose of Loan",
                    dataIndex: "purposeOfLoan",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }
                ,{
                    header: WtfGlobal.getLocaleText("acc.field.Loanfromdate"), // "Purpose of Loan",
                    dataIndex: "loanfrmdate",
                    renderer: WtfGlobal.onlyDateDeletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.loanDueDate"), // "Loan due date",
                    dataIndex: "loanDueDate",
                    renderer: WtfGlobal.onlyDateDeletedRenderer,
                    width: 150,
                    pdfwidth: 100
                },  {
                    header: WtfGlobal.getLocaleText("acc.field.CostCenter"), // "Loan due date",
                    dataIndex: "costcenter",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                },  {
                    header: WtfGlobal.getLocaleText("acc.field.SalesPerson"), // "Loan due date",
                    dataIndex: "salesperson",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.dnRemark"), // "Loan due date",
                    dataIndex: "dnremark",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.dnRemarkMemo"),
                    dataIndex: "dnremarkormemo",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.srRemark"), // "Loan due date",
                    dataIndex: "remark",
                    renderer: WtfGlobal.deletedRenderer,
                    hidden: this.isConsignmentLoanOutstadingReport,
                    width: 150,
                    pdfwidth: 100
                }, {
                    header: WtfGlobal.getLocaleText("acc.field.srRemarkMemo"),
                    dataIndex: "remarkormemo",
                    hidden: this.isConsignmentLoanOutstadingReport,
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }
                ,{
                    header:"Country", // "Loan due date",
                    dataIndex: "country",
                    renderer: WtfGlobal.deletedRenderer,
                    width: 150,
                    pdfwidth: 100
                }
                );
        
            if(customProductField && customProductField.length>0) {
                for(var ccnt=0; ccnt<customProductField.length; ccnt++) {
                    this.colArr.push({
                        header : customProductField[ccnt].columnname,
                        dataIndex: customProductField[ccnt].dataindex,
                        width: 100,
                        hidden:(customProductField[ccnt].dataindex=="Custom_Country Of Origin"||customProductField[ccnt].dataindex=="Custom_Country of Origin")?false:true,
                        pdfwidth: 50,
                        align: 'center'
                    })
                }
            }

            this.cm = new Wtf.grid.ColumnModel(this.colArr), 

            /**************date *****/


            this.summary = new Wtf.grid.GroupSummary({});
        
            this.grid=new Wtf.KwlEditorGridPanel({
                id:"consignmentQAReportGrid"+this.id,
                cm:this.cm,
                store:this.ds,
                loadMask:true,
                tbar:tbarArray,
                viewConfig: {
                    forceFit: false
                },
                searchLabel:WtfGlobal.getLocaleText("acc.het.806"),
                searchLabelSeparator:":",
                searchEmptyText: WtfGlobal.getLocaleText("acc.inventory.QAAproval.TransactionNoProductCode"),
                serverSideSearch:true,
                qsWidth:200,
                displayInfo: true,
                displayMsg: 'Displaying  {0} - {1} of {2}',
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
                bbar: bbarArray
            });
            this.grid.on("cellclick",this.cellClick,this);
        
            Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
                Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
            },this);
        

       
            //        var colModelArray = GlobalColumnModelForReports[this.moduleid];
            //        this.updateStoreConfig(colModelArray, this.ds);

       
            this.add(this.grid);
            this.grid.doLayout();
            this.doLayout();
        
            //        this.loadgrid();
        
            //        this.on("activate",function(){
            //            this.loadgridstore(this.frmDate.getValue(),this.toDate.getValue(),this.docTypeCmbfilter.getValue(),"");
            this.loadgridstore(this.frmDate.getValue(),this.toDate.getValue(),"","");
        //        },this);
        }, function () {

            })
    },
   
    initloadgridstore:function(frm, to,documenttype,status){
        this.ds.baseParams = {
            //            type:type,
            documenttype:documenttype,
            frmDate:frm.format('Y-m-d'),
            toDate:to.format('Y-m-d'),
            isConsignment:true,
            moduleid:53
        }
        this.ds.load({
            params:{
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
                //                limit:30,
                ss:  Wtf.getCmp("Quick"+this.grid.id).getValue()
            }
        });
    },
    loadgridstore:function(frm, to,documenttype,status){
        this.ds.baseParams = {
            documenttype:documenttype,
            frmDate:frm.format('Y-m-d'),
            toDate:to.format('Y-m-d'),
            isConsignment:true,
            moduleid:53
        }
        this.ds.load({
            params:{
                start:0,
                limit:30,
                ss:  Wtf.getCmp("Quick"+this.grid.id).getValue()
            }
        });
    },
    updateStoreConfig: function (customProductField) {
        for (var cnt = 0; cnt < customProductField.length; cnt++) {
            var fieldname = customProductField[cnt].dataindex;
            var newField = new Wtf.data.Field({
                name: fieldname
            });
            this.ds.fields.items.push(newField);
            this.ds.fields.map[fieldname] = newField;
            this.ds.fields.keys.push(fieldname);
        }
        this.ds.reader = new Wtf.data.KwlJsonReader(this.ds.reader.meta, this.ds.fields.items);
    }
   
});