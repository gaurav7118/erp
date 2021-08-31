/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

/*
 *Component to show reports from report builder
 **/
Wtf.reportBuilder.CustomReport = Wtf.extend( Wtf.grid.GridPanel ,{
    initComponent:function() {
        this.createStore();   
        var pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            store: this.store,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores") //"No results to display",
        });
        
        this.summary = new Wtf.grid.GridSummary();
        Wtf.apply(this, {
            border : false,
            columns : [],
            plugins:[this.summary],
            layout : 'fit',
            bbar : pagingToolbar,
            loadMask : true,
            sm: new Wtf.grid.RowSelectionModel({singleSelect:true})
        }); 

        Wtf.reportBuilder.CustomReport.superclass.initComponent.apply(this, arguments);
        this.on("resize", function () {
            this.doLayout();
        });
        this.on('cellclick',this.onCellClick, this);

    },
    
    onCellClick:function(grid ,rowIndex ,columnIndex ,e){
        e.stopEvent();
        
        if(e.getTarget("a[class='jumplink']")){
            var params = {};
            var selectedRec= this.store.getAt(rowIndex);
            params.moduleid = this.reportRec.moduleid;
            params.billid = selectedRec.get("billid");
            params.selectedRec = selectedRec;
            /*
             *On cell click open document in view mode.
             **/
            viewDocumentTab(params);
        }
    },
    
    onDestroy : function(){
        Wtf.reportBuilder.CustomReport.superclass.onDestroy.call(this);
    },
    createStore : function (){
        
        this.store = new Wtf.data.Store({
            url : "ACCCreateCustomReport/executeCustomReport.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: "totalCount",
                root: "data"
            }),
            baseParams:{
                reportID : this.reportRec.id,
                gcurrencyid : gcurrencyid,
                consolidateFlag : false,
                deleted : false,
                nondeleted : false,
                pendingapproval : false,
                showRowLevelFieldsflag : false,
                moduleid : this.reportRec.moduleid,
                isreportloaded : true,
                isCustomWidgetReport : true
            },
            isdefault : false
        });
        
        
        this.store.on("beforeload",function(){
            WtfGlobal.setAjaxTimeOut();
        },this);
        this.store.on("loadexception",function(){
            WtfGlobal.resetAjaxTimeOut();
        },this);
//        handle Store On Load
        this.store.on('load',function(store) {
            WtfGlobal.resetAjaxTimeOut();
            var columns = [];
            columns.push(new Wtf.grid.RowNumberer({width: 25}));
            
            this.columnsConfig = store.reader.jsonData.columns;
            /*
             *Assign new column config to grid.
             **/
            for ( var i=0; i < this.columnsConfig.length; i++){
                var rec = this.columnsConfig[i];
                var column = {
                    dataIndex : rec.dataIndex,
                    header : rec.displayName,
                    width : 130
                }
                if(rec.summaryType != undefined && rec.summaryType != null && rec.summaryType != "" && rec.summaryType != "null" && rec.summaryType !="none"){
                   column.summaryType =  rec.summaryType;                   
                }
                
                if(rec.properties !=undefined && rec.properties.source !=undefined && rec.properties.source.renderer){
                    var rendererType = rec.properties.source.renderer.toLowerCase();
                    if(rendererType == "base currency"){
                        column.renderer = WtfGlobal.currencyDeletedRenderer
                    }else if(rendererType == "transaction currency"){
                        column.renderer = WtfGlobal.withoutRateCurrencyDeletedSymbol
                    }else if(rendererType == "link renderer"){
                        column.renderer = WtfGlobal.linkDeletedRenderer
                    }else if(rendererType == "number"){
                        column.renderer = WtfGlobal.convertInDecimalWithoutCurrencySymbol
                    }
                    if(column.summaryType){
                        column.summaryRenderer = this.getSummaryRenderer(rec);
                    }
                }
                
                columns.push(column);
            }
            this.getColumnModel().setConfig(columns);
            this.getView().refresh();
            
            if (store.getCount() < 1) {
                this.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.getView().refresh();
            }
        },this);
        
    },
    getSummaryRenderer :function(column){
        var renderer = "";
        var xtype =parseInt(column.xtype);
        var summaryType = column.summaryType ? column.summaryType : "";           
        if (summaryType != "" || summaryType != undefined || summaryType != null || summaryType != "null") {            
            summaryType = capitalizeFirstLetter(summaryType);
        }
        if(summaryType == "Sum"){
            summaryType = "Total";
        }
        if(xtype == 2){
            var rendererProperty = column.properties.source.renderer;
            renderer = function(val, metaData, record){
                if(val !="" && val!=undefined){
                val = WtfGlobal.conventInDecimal(val,"");
                if(rendererProperty== "Base Currency"){
                    if(val !="" && val!=undefined){
                        val = Wtf.pref.CurrencySymbol+" "+val;
                    }
                }                
                    val = summaryType+" : "+val;
                    val='<div class="grid-summary-common">'+val+'</div>'
                }
                return val;
            }
        }else{
            renderer = function(val, metaData, record){
                if(val !="" && val!=undefined){
                    val = summaryType+" : "+val;
                    val='<div class="grid-summary-common">'+val+'</div>'
                }
                return val;
            }; 
        }
        return renderer;
    }
});

function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

function viewDocumentTab(params){
    var moduleId = params.moduleid;
    
    switch(moduleId){
        case Wtf.Acc_Sales_Order_ModuleId + "" :
            viewSODocumentTab(params);
            break;
        case Wtf.Acc_Invoice_ModuleId + "" :
            viewSIDocumentTab(params);
            break;
        case Wtf.Acc_Purchase_Order_ModuleId + "" :
            viewPODocumentTab(params);
            break;
        case Wtf.Acc_Vendor_Invoice_ModuleId + "":
            viewPIDocumentTab(params);
            break;
        case Wtf.Acc_Customer_ModuleUUID :
            viewCustomerDocumentTab(params);
            break;
        default :
            Wtf.Msg.alert("Alert","Implementation for link is not provided for this report.");
            break;
    }
}

/*
 *Function to open Sales Order Record in view mode
 */
function viewSODocumentTab(params){
    Wtf.Ajax.requestEx({
        url : "ACCSalesOrderCMN/getSalesOrdersMerged.do",
        params : {
            billid : params.billid
        }
    }, this, function(response,request){
        if(response.data[0]){
            var selectedRec = {};
            selectedRec.data = response.data[0];
            selectedRec.json = response.data[0];
            callViewSalesOrder(true , selectedRec , params.billid , false);
        }
    });
}
/*
 *Function to open Delivery Order Record in view mode
 */
function viewDODocumentTab(params){
    Wtf.Ajax.requestEx({
        url : "ACCInvoiceCMN/getDeliveryOrdersMerged.do",
        params : {
            billid : params.billid
        }
    }, this, function(response,request){
        if(response.data[0]){
            var selectedRec = {};
            selectedRec.data = response.data[0];
            selectedRec.json = response.data[0];
            callViewDeliveryOrder(true , selectedRec , params.billid , false, false);
        }
    });
}

/*
 *Function to open Purchase Order Record in view mode
 */
function viewPODocumentTab(params){
    Wtf.Ajax.requestEx({
        url : "ACCPurchaseOrderCMN/getPurchaseOrdersMerged.do",
        params : {
            billid : params.billid
        }
    }, this, function(response,request){
        if(response.data[0]){
            var selectedRec = {};
            selectedRec.data = response.data[0];
            selectedRec.json = response.data[0];
            var newtranType = 5;
            
            callViewPurchaseOrder(true , selectedRec , params.billid , false , this , newtranType , true);
        }
    });
}

/*
 *Function to open Sales Invoice record in view mode
 */
function viewSIDocumentTab(params){
    Wtf.Ajax.requestEx({
        url : "ACCInvoiceCMN/getInvoicesMerged.do",
        params : {
            CashAndInvoice : true,
            billid : params.billid
        }
    }, this, function(response,request){
        if(response.data[0]){
            var selectedRec = {};
            selectedRec.data = response.data[0];
            selectedRec.json = response.data[0];
            if(selectedRec.data.cashtransaction){
                callViewCashReceipt(selectedRec, 'ViewInvoice');
            }else{
                callViewInvoice(selectedRec, 'ViewCashReceipt');
            }
        }
    });
}

/*
 *Function to open Purchase Invoice record in view mode
 */
function viewPIDocumentTab(params){
    Wtf.Ajax.requestEx({
        url : "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do",
        params : {
            CashAndInvoice : true,
            billid : params.billid
        }
    }, this, function(response,request){
        if(response.data[0]){
            var selectedRec = {};
            selectedRec.data = response.data[0];
            selectedRec.json = response.data[0];
            
            var isExpensiveInv = selectedRec.data.isexpenseinv;
            if(selectedRec.data.cashtransaction){
                callViewPaymentReceipt(selectedRec, 'ViewPaymentReceipt',isExpensiveInv);
            }else{
                callViewGoodsReceipt(selectedRec , 'ViewGoodsReceipt',isExpensiveInv);
            }
        }
    });
}

/*
 *Function to open Sales Return record in view mode
 */
function viewSRDocumentTab(params){
    Wtf.Ajax.requestEx({
        url : "ACCInvoiceCMN/getSalesReturn.do",
        params : {
            billid : params.billid
        }
    }, this, function(response,request){
        if(response.data[0]){
            var selectedRec = {};
            selectedRec.data = response.data[0];
            selectedRec.json = response.data[0];
            
            callViewSalesReturn(true,selectedRec,selectedRec.data.billid,false,selectedRec.data.isNoteAlso,selectedRec.data.isPaymentAlso);
        }
    });
}
/*
 *Function to open Credit Note record in view mode
 */
function viewCNDocumentTab(params){
    Wtf.Ajax.requestEx({
        url : "ACCCreditNote/getCreditNoteMerged.do",
        params : {
            noteid : params.noteid
        }
    }, this, function(response,request){
        if(response.data[0]){
            var selectedRec = {};
            selectedRec.data = response.data[0];
            selectedRec.json = response.data[0];
            if (selectedRec != undefined && selectedRec.data.cntype == Wtf.NoteForOvercharge) {
                var winid = 'creditnoteForOverchargeView' + selectedRec.data.noteno;
                callEditNoteForOvercharge(winid, selectedRec, true, true, true, true);//cntype=6 - CN/DN for Overcharge
            } else {
                callViewCreditNote("ViewcreditNote" + selectedRec.data.noteno, true, true, selectedRec.data.cntype, selectedRec, null);
            }
        }
    });
}

/*
 *Function to open Customer record in view mode
 */
function viewCustomerDocumentTab(params){
    Wtf.Ajax.requestEx({
        url : "ACCCustomerCMN/getCustomers.do",
        params : {
            customerid : params.billid
        }
    }, this, function(response,request){
        if(response.data[0]){
            var selectedRec = {};
            selectedRec.data = response.data[0];
            selectedRec.json = response.data[0];
            
            var isCustomer = true;
            callViewBusinessContactWindow(selectedRec, isCustomer);
        }
    });
}
