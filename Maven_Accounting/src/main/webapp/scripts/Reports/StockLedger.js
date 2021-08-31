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

//function getStockLedgerTabViewDynamicLoad(searchStr,filterAppend,isStockLedgerDetailedReport,id){
function getStockLedgerTabViewDynamicLoad(params) {
    var searchStr = params.searchStr || "";
    var filterAppend = params.filterAppend || "";
    var isStockLedgerDetailedReport = params.isStockLedgerDetailedReport || false;
    var id = params.id || "";
    
    /*
     *Function to add this report in widget report.
     **/
    if (params.isCustomWidgetReport) {
        var panel = new Wtf.account.TransactionListPanelViewStockLedger({
            border: false,
            layout: 'fit',
            isStockLedgerDetailedReport: isStockLedgerDetailedReport,
            iscustreport: true,
            closable: true,
            isCustomer: true,
            isSalesPersonName: true,
            label: WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),
            isCustomWidgetReport : params.isCustomWidgetReport,
            searchJson: searchStr,
            filterAppend: filterAppend
        });
        if (params.callbackFn) {
            /*
             *call callback function to add this report to widget.
             **/
            params.callbackFn.call(this, panel);
        }
    } else {
        if (id === undefined || id === null || id === "") {
            id = 'stockledger';
        }
        var reportPanel = Wtf.getCmp(id);

        /**
         * isStockLedgerDetailedReport is used for "Stock Ledger Detailed Report
         */
        if (isStockLedgerDetailedReport == undefined || isStockLedgerDetailedReport == null) {
            isStockLedgerDetailedReport = false;
        }
    if(reportPanel == null){
            reportPanel = new Wtf.account.TransactionListPanelViewStockLedger({
            id : id,
            border : false,
                title: isStockLedgerDetailedReport ? WtfGlobal.getLocaleText("acc.reports.stockledger.detailed.report") : WtfGlobal.getLocaleText("acc.field.StockLedger"),
                tabTip: isStockLedgerDetailedReport ? WtfGlobal.getLocaleText("acc.reports.stockledger.detailed.report.tooltip") : WtfGlobal.getLocaleText("acc.field.StockLedgerreport"),
                layout: 'fit',
            isStockLedgerDetailedReport:isStockLedgerDetailedReport,
            iscustreport : true,
            closable : true,
            isCustomer:true,
            isSalesPersonName:true,
            label:WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),
            iconCls:getButtonIconCls(Wtf.etype.inventoryval),
            searchJson:searchStr,
            filterAppend:filterAppend
            });
            Wtf.getCmp('as').add(reportPanel);
        }
        Wtf.getCmp('as').setActiveTab(reportPanel);
    showAdvanceSearch(reportPanel,searchStr, filterAppend);
         Wtf.getCmp('as').doLayout();
    }
}

Wtf.account.TransactionListPanelViewStockLedger = function(config) {
    Wtf.apply(this, config);
    
    //ERM-447 For landed cost custom headers brought from back end hence creating a separate array here and will insert those headers later below
    this.initialRec =  [
        {name:'pid'},
        {name:'productDesc'},
        {name:'transactionDate', type:'date'},
        {name:'transactionNumber'},
        {name:'doctype'},
        {name:'balanceqty'},
        {name:'avgcostll'},
        {name:'personCode'},
        {name:'personName'},
        {name:'memo'},
        {name:'received'},
        {name:'delivered'},
        {name:'location'},
        {name:'productid'},
        {name:'warehouse'},
        {name:'batch'},
        {name:'serial'},
        {name:'stockRate'},
        {name:'value'},
        {name:'avglandedcost'},
        {name:'balance'},
        {name:'transactiontype'},
        {name:'billid'},
        {name:'productcategory'},
        {name:'uom'}]
            
        this.GridRec = Wtf.data.Record.create(this.initialRec);

    this.StockLedgerStore = new Wtf.data.GroupingStore({
        url: "ACCProductCMN/getStockValuation.do",
//        url:"ACCProductCMN/getStockLedger.do",
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:"totalCount",
            root: "data"
        },this.GridRec),     
        groupField:"pid",
        sortInfo: {field: 'pid',direction: "ASC"}
    });
//ERM-447 temporarily hiding this check and its fields for landed cost feature
//    this.isActivateLandedInvoiceAmt=false;
    this.StockLedgerStore.on('beforeload', function() {
//        this.isActivateLandedInvoiceAmt=false;
//        if(this.isActivateLandedInvAmt.getValue())
//        this.isActivateLandedInvoiceAmt=this.isActivateLandedInvAmt.getValue();
        var currentBaseParams = this.StockLedgerStore.baseParams;
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
        currentBaseParams.isactivatelandedinvamt=Wtf.account.companyAccountPref.isActiveLandingCostOfItem,
        currentBaseParams.isStockLedgerDetailedReport=this.isStockLedgerDetailedReport,
        currentBaseParams.searchOnField=this.isStockLedgerDetailedReport ? this.searchFieldSelectionCmb.getValue() : "",
        currentBaseParams.isStockLedger=true,
                currentBaseParams.productCategoryid = this.productCategory.getValue();
        currentBaseParams.productType = this.productTypeCombo.getValue();
        this.StockLedgerStore.baseParams=currentBaseParams; 
        this.exportButton.enable()
        this.printButton.enable()
        WtfGlobal.setAjaxTimeOut();

    },this);
    WtfGlobal.setAjaxTimeOut();
    this.StockLedgerStore.on('load', function(store) {
        if(this.StockLedgerStore.getCount() < 1) {
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
            WtfGlobal.resetAjaxTimeOut();
        }
        this.quickPanelSearch.StorageChanged(store);
        Wtf.MessageBox.hide();
    }, this);

     this.GrandTotalReport=new Wtf.XTemplate(// to display the grand total Ref ERP-8925
        '<div style="padding: 5px; border: 1px solid rgb(153, 187, 232);">',            
            
        '<div>',
        '<table width="100%">'+
        '<tr>'+
        '<td style="width:25%;"><b>'+WtfGlobal.getLocaleText("acc.common.pagedtotalinbase")+':</b></td><td style="width:55%;"><span><b>'+Wtf.util.Format.ellipsis('{pagedTotal}',60)+'</b></span></td>'+                   
        '</tr>'+
        '<tr>'+
        '<td style="width:25%;"><b>'+WtfGlobal.getLocaleText("acc.common.grandtotalinbase")+':</b></td><td style="width:55%;"><span><b>'+Wtf.util.Format.ellipsis('{grandTotal}',60)+'</b></span></td>'+                   
        '</tr>'+
        '</table>'+
        '</div>',            
                                 
        '</div>'
        );
            
    this.GrandTotalReportTPL=new Wtf.Panel({
        id:this.isSummary?'GrandTotalSummaryTPL'+this.id:'GrandTotalReportTPL'+this.id,
        border:false,
        width:310,
        baseCls:'tempbackgroundview',
        html:this.GrandTotalReport.apply({
            pagedTotal:WtfGlobal.currencyRenderer(0),
            grandTotal:WtfGlobal.currencyRenderer(0)                    
        })
    });
    
    
   
    this.bbar1 = new Array();

    this.bbar1.push("->",this.GrandTotalReportTPL);

    this.StockLedgerStore.on('datachanged', function(store) {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
        var grandTotalInBaseCurrency= 0;  
        var pageTotalInBaseCurrency= 0;  
        if(store.data.length>0){
            var recordindex = store.findBy(function (record) {
                if(record.json.pagetotal != undefined && record.json.grandTotalInBase != undefined) {
                    return true;
                }
            }, this);
            if(store.getAt(recordindex).json.pagetotal!=undefined && store.getAt(recordindex).json.grandTotalInBase!=undefined){
                pageTotalInBaseCurrency=parseFloat(grandTotalInBaseCurrency) +  parseFloat(store.getAt(recordindex).json.pagetotal);
                grandTotalInBaseCurrency=parseFloat(grandTotalInBaseCurrency) +  parseFloat(store.getAt(recordindex).json.grandTotalInBase);
            }
        }
        this.GrandTotalReport.overwrite(this.GrandTotalReportTPL.body,{
            pagedTotal:WtfGlobal.conventInDecimal(pageTotalInBaseCurrency,WtfGlobal.getCurrencySymbol()),
            grandTotal:WtfGlobal.conventInDecimal(grandTotalInBaseCurrency,WtfGlobal.getCurrencySymbol())
        });
//        for(var i=0;i <= recordindex;i++){
//            if(store.getAt(i).json.ledgerFinalValuation!=undefined){
//                grandTotalInBaseCurrency=parseFloat(grandTotalInBaseCurrency) +  parseFloat(store.getAt(i).json.ledgerFinalValuation);
//            }
//        }
//            this.totalValuationValue.getEl().innerHTML ="<B>" + WtfGlobal.conventInDecimal(pageTotalInBaseCurrency,WtfGlobal.getCurrencySymbol()) +"</B>"; 
//            this.totalValuationValue1.getEl().innerHTML ="<B>" + WtfGlobal.conventInDecimal(grandTotalInBaseCurrency,WtfGlobal.getCurrencySymbol()) +"</B>"; 
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        this.expandButtonClicked = false;
    }, this);
    this.columnArr = [];
    this.columnArr.push(
//    {
//        header:"",
//        dataIndex:"pid",
//        hidden : true
//    },
    {
        header:WtfGlobal.getLocaleText("acc.productList.gridProductID"), // "Product ID",
        dataIndex:"pid",
        pdfwidth:75,
//        fixed: true,
        sortable: true,
        width: 100,
        renderer:WtfGlobal.deletedRenderer
    },{
        header:WtfGlobal.getLocaleText("acc.productList.gridDescription"), // "Description",
        dataIndex:"productDesc",
        width: 100,
        renderer : function(val) {
                /*
                Commenting below line as it is replacing all the html : SDP-12808
                 */
                //val = val.replace(/(<([^>]+)>)/ig,"");
                return "<div wtf:qtip=\'"+val+"\' wtf:qtitle='"+WtfGlobal.getLocaleText("acc.productList.gridDescription")+"'>"+val+"</div>";
        },
        pdfwidth:75
    },{
        header:WtfGlobal.getLocaleText("acc.common.memo"), // "Memo",
        dataIndex:"memo",
        pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.stockLedger.Date"), // "Date",
                dataIndex:'transactionDate',
                renderer:WtfGlobal.onlyDateDeletedRenderer,
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.field.DocumentType"), // "Document Type",
                dataIndex:'doctype',
                hidden:true,
//                renderer:WtfGlobal.onlyDateDeletedRenderer,
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.stockLedger.DocumentNo"), // "Document #",
                dataIndex:"transactionNumber",
                renderer: function(v, m, rec) {
                if (rec.data.transactionNumber=='Opening' ||rec.data.transactionNumber=='Initial Quantity' || rec.data.transactiontype==0  || rec.data.transactiontype==5 ) {
                v = "<span style='float:left;margin:-2px 14px;'>" + v + "</span>";
                }else{
                v = "<a class='jumplink' href='#'>" + v + "</a>";
            }
            return v;
        },
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.field.stockledger.personcode"), // "Person Code",
                dataIndex:"personCode",
                width: 100,
                renderer:WtfGlobal.deletedRenderer,
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.stockLedger.partyORcostCenter"), // "Party / Cost Center",
                dataIndex:"personName",
                width: 100,
                renderer:WtfGlobal.deletedRenderer,
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.stockLedger.Received"), // "Received",
                dataIndex:"received",
                width: 100,
                renderer:this.unitRenderer,                                 // To show quantity upto 4 decimal.
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.stockLedger.Delivered"), // "Delivered",
                dataIndex:"delivered",
                width: 100,
                renderer:this.unitRenderer,
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.field.BalanceQty"), // "Balance Qty",
                dataIndex:"balanceqty",
                width: 100,
                hidden:true,
                renderer:this.unitRenderer,
                pdfwidth:75
            },{
            header:WtfGlobal.getLocaleText("acc.cc.2"), // uom,
            dataIndex:"uom",
            width: 100,
            pdfwidth:80
            },{
                header:WtfGlobal.getLocaleText("acc.cust.Productcategory"), // "Product Category",
                dataIndex:'productcategory',
                hidden:true,
//                renderer:WtfGlobal.onlyDateDeletedRenderer,
                pdfwidth:75,
                renderer: function (val) {
                return "<div wtf:qtip=" +val + ">" + val + "</div>";
                }
            });
    if (config.isStockLedgerDetailedReport) {
        this.columnArr.push({
            header: "Location", // Location,
            dataIndex: "location",
            pdfwidth: 80,
            width: 100,
            renderer: function (val) {
                return "<div wtf:qtip=\"" + unescape(val) + "\"wtf:qtitle=" + WtfGlobal.getLocaleText("acc.auditTrail.gridDetails") + ">" + unescape(val) + "</div>";
            }
        }, {
            header: "Warehouse", // warehouse,
            dataIndex: "warehouse",
            width: 100,
            pdfwidth: 80, renderer: function (val) {
                return "<div wtf:qtip=\"" + unescape(val) + "\"wtf:qtitle=" + WtfGlobal.getLocaleText("acc.auditTrail.gridDetails") + ">" + unescape(val) + "</div>";
            }
        }, {
            header: "Batch", // batch,
            dataIndex: "batch",
            width: 100,
            renderer: function (val) {
                return "<div wtf:qtip=\"" + unescape(val) + "\"wtf:qtitle=" + WtfGlobal.getLocaleText("acc.auditTrail.gridDetails") + ">" + unescape(val) + "</div>";
            },
            pdfwidth: 80
        }, {
            header: "Serial", // serial,
            dataIndex: "serial",
            width: 100,
            renderer: function (val) {
                return "<div wtf:qtip=\"" + unescape(val) + "\"wtf:qtitle=" + WtfGlobal.getLocaleText("acc.auditTrail.gridDetails") + ">" + unescape(val) + "</div>";
            },
            pdfwidth: 80
        });
    }
    
    if (GlobalColumnModelForLandedCostCategory && GlobalColumnModelForLandedCostCategory.columnModel) {
        var landedCostCM = GlobalColumnModelForLandedCostCategory.columnModel;
        for (var ccnt = 0; ccnt < landedCostCM.length; ccnt++) {
            this.columnArr.push({
                header: landedCostCM[ccnt].header,
                dataIndex: landedCostCM[ccnt].dataIndex,
                width: 150,
                pdfwidth: 50,
                renderer: landedCostCM.renderer
            });
        }
        if (GlobalColumnModelForLandedCostCategory.rec) {
            this.updateStoreConfig(GlobalColumnModelForLandedCostCategory.rec, this.StockLedgerStore);
        }
    }
    
    this.columnArr.push({
        header: Wtf.account.companyAccountPref.isActiveLandingCostOfItem ? WtfGlobal.getLocaleText("acc.stockLedger.averagecost"): WtfGlobal.getLocaleText("acc.stockLedger.StockRate") + '(' + WtfGlobal.getCurrencySymbol() + ')', // "Stock Rate",
        dataIndex: "stockRate",
        pdfrenderer: "rowcurrency",
        renderer: this.QuantityRender.createDelegate(this),
        width: 150,
//                renderer:WtfGlobal.withCurrencyUnitPriceRenderer,         // To show rate upto 3 decimal
        pdfwidth: 75
    });
    
    if (Wtf.account.companyAccountPref.isActiveLandingCostOfItem) {
        this.columnArr.push({
            header: WtfGlobal.getLocaleText("acc.landingcost.avglandedcost"),
            dataIndex: "avglandedcost",
            pdfrenderer: "rowcurrency",
            renderer: this.QuantityRender.createDelegate(this),
            width: 150,
            pdfwidth: 75
        });
    }
    this.columnArr.push({
        header:WtfGlobal.getLocaleText("acc.stockLedger.Value") +'('+WtfGlobal.getCurrencyName()+')', //ERP-39499 // "Value(Currency Name)", 
        dataIndex:"value",
        width: 150,
        pdfrenderer:"rowcurrency",
//                renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
        renderer: this.AmountRender.createDelegate(this),
                pdfwidth:75
    },{
        header:  WtfGlobal.getLocaleText("acc.stockLedger.averagecost") + '(' + WtfGlobal.getCurrencyName() + ')',//ERP-39499  "Average Cost(Currency Name)", 
        dataIndex: "avgcostll",
        pdfrenderer: "rowcurrency",
        hidden:true,
        renderer: this.QuantityRender.createDelegate(this),
        width: 150,
//                renderer:WtfGlobal.withCurrencyUnitPriceRenderer,         // To show rate upto 3 decimal
        pdfwidth: 75
    });
    this.gridTbar =[];
    this.gridTbar.push(WtfGlobal.getLocaleText("acc.stockLedgerReport.Disclaimer.text"));
    
    this.grid = new Wtf.grid.GridPanel({
        store:this.StockLedgerStore,
        border:false,
        tbar :this.gridTbar,
        bbar:this.bbar1,
        layout:'fit',
        view: new Wtf.grid.GroupingView({
            startCollapsed :true,
            //forceFit: true,
            showGroupName: false,
            enableGroupingMenu: false,
            emptyText:WtfGlobal.emptyGridRenderer( WtfGlobal.getLocaleText('acc.common.norec') + "<br>" +WtfGlobal.getLocaleText('acc.common.norec.click.fetchbtn')) ,
//            hideGroupedColumn: true
        }),
        loadMask:true,
        columns:this.columnArr
    });
    this.grid.on("render",function(){
        this.grid.getView().applyEmptyText(); 
    },this);
    this.GridRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'companyid'},
        {name:'companyname'},
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'billto'},
        {name:'orderamount'},
        {name:'shipto'},
        {name:'mode'},
        {name:'billno'},
        {name:'date', type:'date'},
        {name:'shipdate', type:'date'},
        {name:'termid'},
        {name:'termdetails'},
        {name:'termamount'},
        {name:'personname'},
        {name:'aliasname'},
        {name:'customerporefno'},
        {name:'personemail'},
        {name:'personid'},
        {name:'shipping'},
        {name:'deleted'},
        {name:'memo'},
        {name:'costcenterid'},
        {name:'costcenterName'},
        {name:'statusID'},
        {name:'shipvia'},
        {name:'fob'},
        {name: 'isLinkedTransaction'},
        {name:'permitNumber'},
        {name:'externalcurrencyrate'},
        {name:'status'},
        {name:'isautogenerateddo'},
        {name:'invoiceno'},
        {name:'approvalstatusinfo'},
        {name:'approvalstatus'},
        {name:'withoutinventory',type:'boolean'},
        {name:'isfavourite'},
        {name:'isprinted'},
        {name:'currencysymbol'},
        {name:'currencyid'},
        {name:'amount'},
        {name:'amountinbase'},
        {name:'invoicenumber'},
        {name:'sequenceformatid'},
        {name:'lasteditedby'},
        {name:'isConsignment'},
        {name:'isLeaseFixedAsset'},
        {name:'isFixedAsset'},
        {name:'custWarehouse'},
        {name:'movementtype'},
        {name:'movementtypename'},
        {name:'driver'},
        {name:'billingAddressType'},
        {name:'billingAddress'},
        {name:'billingCountry'},
        {name:'billingState'},
        {name:'billingPostal'},
        {name:'billingEmail'},
        {name:'billingFax'},
        {name:'billingMobile'},
        {name:'billingPhone'},
        {name:'billingContactPerson'},
        {name:'billingRecipientName'},
        {name:'billingContactPersonNumber'},
        {name:'billingContactPersonDesignation'},
        {name:'billingWebsite'},
        {name:'billingCounty'},
        {name:'billingCity'},
        {name:'shippingAddressType'},
        {name:'shippingAddress'},
        {name:'shippingCountry'},
        {name:'shippingState'},
        {name:'shippingCounty'},
        {name:'shippingCity'},
        {name:'shippingEmail'},
        {name:'shippingFax'},
        {name:'shippingMobile'},
        {name:'shippingPhone'},
        {name:'shippingPostal'},
        {name:'shippingContactPersonNumber'},
        {name:'shippingContactPersonDesignation'},
        {name:'shippingWebsite'},
        {name:'shippingRecipientName'},
        {name:'shippingContactPerson'},
        {name:'shippingRoute'},  
        {name: 'vendcustShippingAddress'},
        {name: 'vendcustShippingCountry'},
        {name: 'vendcustShippingState'},
        {name: 'vendcustShippingCounty'},
        {name: 'vendcustShippingCity'},
        {name: 'vendcustShippingEmail'},
        {name: 'vendcustShippingFax'},
        {name: 'vendcustShippingMobile'},
        {name: 'vendcustShippingPhone'},
        {name: 'vendcustShippingPostal'},
        {name: 'vendcustShippingContactPersonNumber'},
        {name: 'vendcustShippingContactPersonDesignation'},
        {name: 'vendcustShippingWebsite'},
        {name: 'vendcustShippingContactPerson'},
        {name: 'vendcustShippingRecipientName'},
        {name: 'vendcustShippingAddressType'},
        {name:'mapSalesPersonName'},
        {name:'salesPerson'},
        {name:'agent'},
        {name:'agentname'},
        {name:'isTaxRowLvlAndFromTaxGlobalLvl'},
        {name:'taxid'},
        {name:'includeprotax'},
        {name:'isFromPOS'},
        {name:'attachdoc'},
        {name:'attachment'},
        {name:'islinkedtransaction'},
        {name:'hasAccess',type:'boolean'},
        {name:'currencyCode'},
        {name:'discount'},
        {"name":"id"},
        {"name":"store_id"},
        {"name":"storeAbbr"},
        {"name":"storeDesc"},
        {"name":"productCode"},
        {"name":"productName"},
        {"name":"productDescription"},
        {"name":"quantity"},
        {"name":"packaging"},
        {"name":"uomName"},
        {"name":"createdBy"},
        {"name":"markouttype"},
        {"name":"reason"},
        {"name":"costcenter"},
        {"name":"cost"},
        {"name":"amount"},
        {"name":"date"},
        {"name":"type"},
        {"name":"createdon"},
        {"name":"adjustmentType"},
        {"name":"remark"},
        {"name":"partnumber"},
        {"name":"seqNumber"},
        {"name":"locationname"},
        {"name":"locationid"},
        {"name":"isBatchForProduct"},
        {"name":"isSerialForProduct"},
        {name:"isRowForProduct"},
        {name:"isRackForProduct"},
        {name:"isBinForProduct"},
        {"name":"stockDetails"},
        {"name":"memo"},
        {"name":"adjustmentreason"},
        {"name":"adjustmentreasonid"},
        {"name":"reasonid"},
        {name: 'productid'},
        {name: 'productname'},
        {name: 'productrefno'},
        {name: 'description'},
        {name: 'mainproductid'},
        {name: 'bomdetailid'},
        {name: 'bomCode'}
    ]);

    this.grid.on('cellclick', onCellClick, this);

    function onCellClick(g, i, j, e) {
        var el = e.getTarget("a");
        if (el == null)
            return;
        var formrec = this.grid.getStore().getAt(i);
        var header = g.getColumnModel().getDataIndex(j);
        if (header == "transactionNumber") {
            var transactiontype = formrec.data.transactiontype;
            if(transactiontype == 1){                                                       //Goodsreceipt-Order
                this.StoreUrl=" ACCGoodsReceiptCMN/getGoodsReceiptOrdersMerged.do";
            }else if(transactiontype == 2){                                                 //Purchase Return
                this.StoreUrl=" ACCGoodsReceiptCMN/getPurchaseReturn.do";
            }else if (transactiontype == 3) {                                               //Delivery-Order
                this.StoreUrl="ACCInvoiceCMN/getDeliveryOrdersMerged.do";
            }else if(transactiontype == 4){                                                 //Sales-Return
                this.StoreUrl="ACCInvoiceCMN/getSalesReturn.do";
            }else if(transactiontype == 7 || transactiontype == 8 ){                                                 //Sales-Return
                this.StoreUrl="INVStockAdjustment/getStockAdjustmentList.do";
            } else if (transactiontype == 6) { //Build Assembly
                this.StoreUrl = "ACCProduct/getAssemblyProducts.do";
    }
    
            this.recStore = new Wtf.data.Store({
                url:this.StoreUrl,
                remoteSort: true,
                baseParams:{
                    billid: formrec.data.billid,
                    productid: formrec.data.productid,
                    stockAdjustmentID:formrec.data.billid,
                    summaryFlag	:true
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data",
                    totalProperty:'count'
                },this.GridRec)
            });
            this.recStore.on('load', function(store) {
                var rec = this.recStore.getAt(0);
                if (transactiontype == 1) {                                                         //Goodsreceipt-Order
                    callViewGoodsReceiptDelivery(true,rec,formrec.data.billid,false,this.isFixedAsset)
                }else if(transactiontype == 2){                                                     //Purchase Return
                    callViewPurchaseReturn(true,rec,formrec.data.billid,false,formrec.data.isNoteAlso);
                }else if (transactiontype == 3) {                                                   //Delivery-Order
                    callViewDeliveryOrder(true,rec,formrec.data.billid,false,this.isFixedAsset)
                }else if(transactiontype == 4){                                                     //Sales-Return
                    callViewSalesReturn(true,rec,formrec.data.billid,false,formrec.data.isNoteAlso);
                }else if(transactiontype == 7 || transactiontype == 8 ){                                                     //Stock Adjustment
                    callViewmarkout(true,rec,formrec.data.billid,1);
                } else if (transactiontype == 6) {
                    callBuildAssemblyForm(formrec.data.productid, rec, true, false);
                }
            }, this);

            this.recStore.load();

        }
    }
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.stockLedger.QuickSearchEmptyText"), // "Search by Document no, Description, Code, Party / Cost Center ...",
        width: this.isStockLedgerDetailedReport || this.isCustomWidgetReport ? 150 : 300,
        id:"quickSearch"+config.helpmodeid,
        field: 'transactionNumber'
    });
    this.searchFieldStore = new Wtf.data.SimpleStore({
        fields: ["id", "name","toolTip"],
        data: [["OTHERS", "OTHERS", "Search by Document no, Description, Code, Party / Cost Center"], ["BATCH", "BATCH", "Search on Batch Name"], ["SERIAL", "SERIAL", "Search on Serial"]]
    });
    this.searchFieldSelectionCmb = new Wtf.form.ExtFnComboBox({
        hiddenName: 'searchOnField',
        store: this.searchFieldStore,
        typeAhead: true,
        displayField: 'name',
        extraFields: ['toolTip'],
        searchOnField:true,
        valueField: 'id',
        mode: 'local',
        listWidth:500,
        width: 100,
        value: "OTHERS",
        triggerAction: 'all',
        emptyText: WtfGlobal.getLocaleText("acc.stockavailability.SelectFieldtoSearch")
    });

    this.resetBttn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  // 'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  // 'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        hidden : this.isCustomWidgetReport,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);

    this.startDate = new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  // 'From',
        name:'startdate',
        format:WtfGlobal.getOnlyDateFormat(),
        //    readOnly:true,
        value:WtfGlobal.getDates(true)
    });

    this.endDate = new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  // 'To',
        format:WtfGlobal.getOnlyDateFormat(),
        //  readOnly:true,
        name:'enddate',
        value:WtfGlobal.getDates(false)
    });

    this.fetchBttn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  // 'Fetch',
        tooltip:WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"), // "Select a time period to view corresponding transactions.",
        style:"margin-left: 6px;",
        iconCls:'accountingbase fetch',
        scope:this,
        handler:this.fetchStatement                        
    });

    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        hidden : this.isCustomWidgetReport,
        iconCls: "advanceSearchButton"
    });
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleidarray: '30,27,28,29,31'.split(','),
        advSearch: false,
        isAvoidRedundent:true,
        reportid:Wtf.autoNum.StockLedger        //Used for remember search
    });

    this.productCategoryRec = Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'name'
        }
    ]);

    this.productCategoryStore = new Wtf.data.Store({
        url: "ACCMaster/getMasterItems.do",
        baseParams: {
            mode:112,
            groupid:19
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.productCategoryRec)
    });
    
    this.CategoryComboConfig = {
        hiddenName: "id",
        store: this.productCategoryStore,
        valueField: 'id',
        hideLabel: false,
        displayField: 'name',
        emptyText: WtfGlobal.getLocaleText("acc.msgbox.17"),
        mode: 'local',
        typeAhead: true,
        selectOnFocus: true,
        triggerAction: 'all',
        scope: this
    };
    this.productCategory = new Wtf.common.Select(Wtf.applyIf({
        multiSelect: true,
        fieldLabel: WtfGlobal.getLocaleText("acc.productList.gridProduct") + '*' ,
        forceSelection: true,
//        extraFields: ['name'],
        extraComparisionField: 'id', // type ahead search on product id as well.
        listWidth: Wtf.ProductComboListWidth,
        width: 150
    },this.CategoryComboConfig));

    this.productCategoryStore.load();

    this.productCategoryStore.on("load", function(store) {
        WtfGlobal.resetAjaxTimeOut();
        var record = new this.productCategoryRec({
            id: "All",
            name:"All"

        });
        this.productCategory.store.insert(0, record);
        this.productCategory.setValue("All");
    }, this);

    this.productCategory.on('select',function(combo, productRec) {
        if (productRec.get('id') == 'All') {
            combo.clearValue();
            combo.setValue('All');
        } else if (combo.getValue().indexOf('All') >= 0) {
            combo.clearValue();
            combo.setValue(productRec.get('id'));
        }
    }, this);

    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);


     this.productTypeStore= new Wtf.data.SimpleStore({
        fields: ['id', 'name'],
            data : [['All','All'],['d8a50d12-515c-102d-8de6-001cc0794cfa','Inventory Part'],['e4611696-515c-102d-8de6-001cc0794cfa','Inventory Assembly'],['ff8080812f5c78bb012f5cfe7edb000c9cfa','Inventory Non-Sale']]
    });


    this.productTypeComboConfig = {
        hiddenName: "id",
        store: this.productTypeStore,
        valueField: 'id',
        hideLabel: false,
        displayField: 'name',
        mode: 'local',
        typeAhead: true,
        selectOnFocus: true,
        triggerAction: 'all',
        scope: this
    };

    this.productTypeCombo = new Wtf.common.Select(Wtf.applyIf({
        multiSelect: true,
        fieldLabel: WtfGlobal.getLocaleText("acc.productList.gridProduct") ,
        forceSelection: true,
//        extraFields: ['name'],
        value:'All',
        extraComparisionField: 'id', // type ahead search on product id as well.
        listWidth: Wtf.ProductComboListWidth,
        width: 150
    }, this.productTypeComboConfig));

    this.productTypeCombo.on('select', function (combo, productRec) {
        if (productRec.get('id') == 'All') {
            combo.clearValue;
            combo.setValue('All');
        } else if ( combo.getValue().indexOf('All') >= 0) {
            combo.clearValue();
            combo.setValue(productRec.get('id'));
        }
    }, this);

    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
     this.exportButton=new Wtf.exportButton({
        obj:this,
        id:"exportReports"+this.id,
        hidden : this.isCustomWidgetReport,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
        disabled :true,
        scope : this,
        filename: (this.isStockLedgerDetailedReport ? WtfGlobal.getLocaleText("acc.reports.stockledger.detailed.report") : WtfGlobal.getLocaleText("acc.field.StockLedger")) + "_v1",
        menuItem:{
            csv:true,
            pdf:false,
            rowPdf:false,
            xls:true,
            subMenu:this.isStockLedgerDetailedReport?false:true
        },
        params:{
            enddate :  WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            isExport:true,
            isStockLedgerDetailedReport:this.isStockLedgerDetailedReport,
//            start:0,
//            limit:this.pP.combo.value,
            isStockLedger : true,
            searchJson:this.searchJson==undefined?"":this.searchJson,
            filterConjuctionCriteria: this.filterConjuctionCrit==undefined?"":this.filterConjuctionCrit
        },
        get:Wtf.autoNum.StockLedger
    });
     this.exportButton.on("click",function(){
        this.exportButton.setParams({
            ss:this.quickPanelSearch.getValue(),
            searchOnField: this.isStockLedgerDetailedReport ? this.searchFieldSelectionCmb.getValue() : "",
            startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            isStockLedger : true,
            isExport:true,
            isStockLedgerDetailedReport:this.isStockLedgerDetailedReport,
            searchJson:this.searchJson==undefined?"":this.searchJson,
            filterConjuctionCriteria: this.filterConjuctionCrit==undefined?"":this.filterConjuctionCrit
        });
    },this);

    this.printButton=new Wtf.exportButton({
        obj:this,
        hidden : this.isCustomWidgetReport,
        text:WtfGlobal.getLocaleText("acc.common.print"),
        tooltip :WtfGlobal.getLocaleText("acc.sales.printTT"),  //'Print report details',
        disabled :true,
        params:{ 	
            searchOnField: this.isStockLedgerDetailedReport ? this.searchFieldSelectionCmb.getValue() : "",
            stdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate:  WtfGlobal.convertToGenericDate(this.endDate.getValue()),		
            name: (this.isStockLedgerDetailedReport ? WtfGlobal.getLocaleText("acc.reports.stockledger.detailed.report") : WtfGlobal.getLocaleText("acc.field.StockLedger")),
            isStockLedgerDetailedReport:this.isStockLedgerDetailedReport,
            isStockLedger : true
        },
        label: (this.isStockLedgerDetailedReport ? WtfGlobal.getLocaleText("acc.reports.stockledger.detailed.report") : WtfGlobal.getLocaleText("acc.field.StockLedger")),
        menuItem:{
            print:true
        },
        get:Wtf.autoNum.StockLedger
    })
    /*
     * Provided button to expand or collapse all row details. 
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
    
    Wtf.account.TransactionListPanelViewStockLedger.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.TransactionListPanelViewStockLedger,Wtf.Panel, {
        onRender: function(config){
//        this.StockLedgerStore.load({
//            params:{
//                start:0,
//                limit:30,
//                isprovalreport:true,
//                isStockLedgerDetailedReport:this.isStockLedgerDetailedReport
//            }
//        });

        var tbbar = [];
        tbbar.push(this.quickPanelSearch);
        if (this.isStockLedgerDetailedReport && this.isCustomWidgetReport !== true ) {
            tbbar.push(WtfGlobal.getLocaleText("acc.stockavailability.SearchOnField") + ": ", this.searchFieldSelectionCmb);
        }
        tbbar.push(this.AdvanceSearchBtn, this.resetBttn, '-', WtfGlobal.getLocaleText("acc.common.from"), this.startDate, WtfGlobal.getLocaleText("acc.common.to"),this.endDate , this.fetchBttn)
        if (this.isCustomWidgetReport !== true) {
            tbbar.push(WtfGlobal.getLocaleText("acc.cust.Productcategory"),this.productCategory, WtfGlobal.getLocaleText("acc.invReport.type")
                      ,this.productTypeCombo);
        }
        tbbar.push(this.exportButton, this.printButton, this.expandCollpseButton);
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [this.objsearchComponent,{
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid],
                    tbar: tbbar,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.StockLedgerStore,
                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({
                        id : "pPageSize_"+this.id
                        })
                    })
                }]
        });
        this.add(this.leadpan);
        
        if (!this.isStockLedgerDetailedReport) {
            this.getMyConfig();
            new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
                this.grid.on('statesave', this.saveMyStateHandler, this);
            }, this);
        }
        
        this.grid.on('afterlayout', function () {
            if (this.grid && this.grid.view && this.grid.view.innerHd) {
                Wtf.fly(this.grid.view.innerHd).on("click", function () {
                    if (this.grid.view.hmenu) {
                        this.grid.view.hmenu.items.get("asc").setDisabled(true);
                        this.grid.view.hmenu.items.get("desc").setDisabled(true);
                    }
                }, this);
            }
        }, this);
        
        this.grid.on('headerclick', function (grid, index, e) {
            return false;
        }, this);
        
        Wtf.account.TransactionListPanelViewStockLedger.superclass.onRender.call(this,config);
    },
    updateStoreConfig: function (data, store) {
        for (var fieldcnt = 0; fieldcnt < data.length; fieldcnt++) {
            var fieldname = data[fieldcnt].dataIndex;
            var newField = new Wtf.data.Field({
                name: fieldname,
                type: 'auto'
            });
            store.fields.items.push(newField);
            store.fields.map[fieldname] = newField;
            store.fields.keys.push(fieldname);
        }
        store.reader = new Wtf.data.KwlJsonReader(store.reader.meta, store.fields.items);
    },
    
    handleResetClick:function() {
        if (this.searchFieldSelectionCmb) {
            this.searchFieldSelectionCmb.reset();
        }
        if(this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.fetchStatement();
        }
    },
 
    fetchStatement:function() {
        this.sDate=this.startDate.getValue();
        this.eDate=this.endDate.getValue();
                 
        if(this.sDate > this.eDate){
            WtfComMsgBox(1,2);
            return;
        }

        var fromdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        var todate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());

        this.StockLedgerStore.load({
            params: {
                startdate:fromdate,
                enddate:todate,
                start:0,
                searchOnField: this.isStockLedgerDetailedReport ? this.searchFieldSelectionCmb.getValue() : "",
                ss:this.quickPanelSearch.getValue(),
                limit:this.pP.combo.value,
                isprovalreport:true
            }
        });
    },
    QuantityRender: function(v,m,rec){
        var val = WtfGlobal.withCurrencyUnitPriceRenderer(v,m,rec)
        if(rec.data.transactionNumber==""){
            return '<b>'+val+'</b>';
        }else{
            return val;
        }
    },
    AmountRender: function(v,m,rec){
        var val = WtfGlobal.currencyRenderer1(v,m,rec)
        if(rec.data.transactionNumber==""){
            return  '<b>'+val+'</b>';
        }else{
            return val;
        }
    },
    unitRenderer: function(value,metadata,record) {
        if(value != '') {
            value = parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        }
        return value;
    },
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();

    },
        filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.StockLedgerStore.baseParams = {
            pendingApproval:this.pendingApproval,
            flag: 1,
            searchJson: this.searchJson,
            isStockLedgerDetailedReport: this.isStockLedgerDetailedReport,
            moduleid: Wtf.Acc_GENERAL_LEDGER_ModuleId,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.StockLedgerStore.load({
            params: {
                searchOnField: this.isStockLedgerDetailedReport ? this.searchFieldSelectionCmb.getValue() : "",
                ss: this.quickPanelSearch.getValue(),
                start: 0,
                limit: this.pP.combo.value
            }
        });
    },
    
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.StockLedgerStore.baseParams = {
            pendingApproval:this.pendingApproval,
            flag: 1,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_GENERAL_LEDGER_ModuleId,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.StockLedgerStore.load({
            params: {
                ss: this.quickPanelSearch.getValue(),
                searchOnField: this.isStockLedgerDetailedReport ? this.searchFieldSelectionCmb.getValue() : "",
                start: 0,
                limit: this.pP.combo.value
            }
        });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
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
    
    getMyConfig: function () {
        WtfGlobal.getGridConfig (this.grid, Wtf.Acc_GENERAL_LEDGER_ModuleId, false, false);
    },
    
    saveMyStateHandler: function (grid, state) {
        WtfGlobal.saveGridStateHandler(this, grid, state, Wtf.Acc_GENERAL_LEDGER_ModuleId, grid.gridConfigId, false);
    }
});


