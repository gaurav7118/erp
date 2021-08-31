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

function getStockAgeingTabViewDynamicLoad(params) {
   /*
     *Function to add this report in widget report.
     **/
    if (params.isCustomWidgetReport) {
        var panel = new Wtf.account.TransactionListPanelViewStockAgeing({
            border: false,
            layout: 'fit',
            iscustreport: true,
            closable: true,
            isCustomer: true,
            isSalesPersonName: true,
            isCustomWidgetReport: params.isCustomWidgetReport,
            label: WtfGlobal.getLocaleText("acc.accPref.autoInvoice")
        });
        if (params.callbackFn) {
            /*
             *call callback function to add this report to widget.
             **/
            params.callbackFn.call(this, panel);
        }
    } else {
        callStockAgeing();
        callStockAgedSummary();
        this.itemsarr = [];

        this.itemsarr.push(this.stockAgeingDetail);
        this.itemsarr.push(this.stockAgeingSummery);
        var mainTabId = Wtf.getCmp("as");
        this.stockAgingTab = Wtf.getCmp("stockageing");
        if (this.stockAgingTab == null) {
            this.stockAgingTab = new Wtf.TabPanel({
                title: WtfGlobal.getLocaleText("acc.inventoryList.StockAgeing"),
                tabTip: WtfGlobal.getLocaleText("acc.inventoryList.StockAgeing") + " " + WtfGlobal.getLocaleText("acc.field.Report"),
                id: 'stockageing',
                closable: true,
                border: false,
                items: this.itemsarr,
                iconCls: 'accountingbase balancesheet'

            });
            mainTabId.add(this.stockAgingTab);
        }
        stockAgingTab.on('activate', function () {
            this.stockAgingTab.setActiveTab(0);
            this.stockAgingTab.doLayout();
        }, this);
        mainTabId.setActiveTab(this.stockAgingTab);
        mainTabId.doLayout();

    }
}

 function callStockAgedSummary (){
            this.stockAgeingSummery = new Wtf.account.TransactionListPanelViewStockAgeing({
            id : 'stockageingsummary',
            border : false,
            title: WtfGlobal.getLocaleText("acc.inventoryList.StockAgeing")+" " +WtfGlobal.getLocaleText("acc.ccReport.tab1"),
            tabTip: WtfGlobal.getLocaleText("acc.inventoryList.StockAgeing")+" "+WtfGlobal.getLocaleText("acc.field.Report"),
            layout: 'fit',
            iscustreport : true,
            closable : false,
            isCustomer:true,
            isSummary:true,
            isSalesPersonName:true,
            label:WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),
            iconCls:getButtonIconCls(Wtf.etype.inventoryval)
            });
        
    }
            
     function callStockAgeing (){
            this.stockAgeingDetail = new Wtf.account.TransactionListPanelViewStockAgeing({
            id : 'stockageingdetail',
            border : false,
            title: WtfGlobal.getLocaleText("acc.inventoryList.StockAgeing") + " " + WtfGlobal.getLocaleText("acc.ccReport.tab2"),
            tabTip: WtfGlobal.getLocaleText("acc.inventoryList.StockAgeing")+" "+WtfGlobal.getLocaleText("acc.field.Report"),
            layout: 'fit',
            iscustreport : true,
            closable : false,
            isCustomer:true,
            isSalesPersonName:true,
            label:WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),
            iconCls:getButtonIconCls(Wtf.etype.inventoryval)
            });
    }

//******************************************
Wtf.account.TransactionListPanelViewStockAgeing = function(config) {
    Wtf.apply(this, config);
     
    this.GridRec = Wtf.data.Record.create([
        {name:'pid'},
        {name:'productname'},
        {name:'productDesc'},
        {name:'transactionNumber'},
        {name:'transactionType'},
        {name:'transactionDate', type:'date'},
        {name:'unit'},
        {name:'transactionNumber'},
        {name:'quantity1', type:'float'},
        {name:'amount1', type:'float'},
        {name:'quantity2', type:'float'},
        {name:'amount2', type:'float'},
        {name:'quantity3', type:'float'},
        {name:'quantitydue1', type:'float'},
        {name:'quantitydue2', type:'float'},
        {name:'quantitydue3', type:'float'},
        {name:'quantitydue4', type:'float'},
        {name:'quantitydue5', type:'float'},
        {name:'quantitydue6', type:'float'},
        {name:'quantitydue7', type:'float'},
        {name:'quantitydue8', type:'float'},
        
        {name:'amount3', type:'float'}
    ]);
    
    this.StockAgeingStore = this.isSummary?new Wtf.data.Store({
        url:"ACCProductCMN/getStockValuation.do",
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:"totalCount",
            root: "data"
        },this.GridRec),
        baseParams:{
            stockAgeing:true,
            isSummary:this.isSummary
        },
//        groupField:"pid",
        sortInfo: {field: 'pid',direction: "ASC"}
    }):new Wtf.data.GroupingStore({
        url:"ACCProductCMN/getStockValuation.do",
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:"totalCount",
            root: "data"
        },this.GridRec),
        baseParams:{
            stockAgeing:true
        },
        groupField:"pid",
        sortInfo: {field: 'pid',direction: "ASC"}
    })
    
    
     this.interval=new Wtf.form.NumberField({
        fieldLabel:WtfGlobal.getLocaleText("acc.agedPay.till"),  //'Till',
        maxLength:2,
        width:30,
        allowDecimal:false,
        allowBlank:true,
        minValue:2,
        maskRe: /[0-9-]/,
        name:'duration',
        value:30
    });
    
     
    this.typeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:"boolean"}, 'name'],
        data :[[true,WtfGlobal.getLocaleText("acc.rem.127")]]
    });
    this.typeEditor = new Wtf.form.ComboBox({
        store: this.typeStore,
        name:'isdistributive',
        displayField:'name',
        value:true,
        anchor:"30%",
        width:100,
        valueField:'typeid',
        mode: 'local',
        triggerAction: 'all'
    });
    
    this.StockAgeingStore.on('beforeload', function(s,o) {
        WtfGlobal.setAjaxTimeOut();
         this.exportButton.enable();
         if(this.pP.combo!=undefined){
            if(this.pP.combo.value=="All"){
                var count = this.StockAgeingStore.getTotalCount();
                var rem = count % 5;
                if(rem == 0){
                    count = count;
                }else{
                    count = count + (5 - rem);
                }
                s.paramNames.limit = count;
                s.paramNames.valuationMethod = this.isLifoCombo.getValue();
//                if(this.isLifoCombo.getValue()){
//                    s.baseParams.isLifo = true;
//                }else{
//                    s.baseParams.isLifo = false;
//                }
                }
            }
            s.baseParams.asofdate = WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue());
            s.baseParams.ss = this.quickPanelSearch.getValue();
            s.baseParams.duration = this.interval.getValue();
    },this);
    
    this.StockAgeingStore.on('load', function(store) {
        WtfGlobal.resetAjaxTimeOut();
        if(this.StockAgeingStore.getCount() < 1) {
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        this.expandButtonClicked = false; 
//        this.quickPanelSearch.StorageChanged(store);
    }, this);
    
//    this.StockAgeingStore.on('datachanged', function() {
//        var p = this.pP.combo.value;
//        this.quickPanelSearch.setPage(p);
//    }, this);
    
    var isLifoStoreRec = new Array();
    isLifoStoreRec.push(["All", ""]);
    isLifoStoreRec.push(["LIFO", "0"]);
    isLifoStoreRec.push(["FIFO", "1"]);
    isLifoStoreRec.push(["Wt. Avg.", "2"]);
    
    this.isLifoStore = new Wtf.data.SimpleStore({
        fields:[{name:'name'},{name:'value'}],
        data:isLifoStoreRec
    });
      
    this.isLifoCombo = new Wtf.form.ComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.stockAgeing.valuation"), // "Select Valuation Method",
        triggerAction:'all',
        name:"isLifo",
        hideLabel:this.isSummery?true:false,
        hidden:this.isSummery?true:false,
        mode: 'local',
        valueField:'value',
        displayField:'name',
        id:'isLifo'+this.heplmodeid+this.id,
        store:this.isLifoStore,
        emptyText: "Select LIFO/FIFO",
        allowBlank:false,
        typeAhead: true,
        width:135,
        forceSelection: true,
        selectOnFocus:true,
        scope:this
    });
    this.isLifoCombo.setValue("");
    
//    this.quickPanelSearch = new Wtf.KWLTagSearch({
//        emptyText:WtfGlobal.getLocaleText("acc.stockAgeing.QuickSearchEmptyText"), // "Search by Document no, Description, Date, Unit ...",
//        width: 300,
//        id:"quickSearch"+config.helpmodeid,
//        field: 'transactionNumber'
//    });
//    
//    this.resetBttn = new Wtf.Toolbar.Button({
//        text:WtfGlobal.getLocaleText("acc.common.reset"),  // 'Reset',
//        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  // 'Allows you to add a new search term by clearing existing search terms.',
//        id: 'btnRec' + this.id,
//        scope: this,
//        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
//        disabled :false
//    });
//    this.resetBttn.on('click',this.handleResetClick,this);
    
//    this.startDate = new Wtf.form.DateField({
//        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  // 'From',
//        name:'startdate',
//        format:WtfGlobal.getOnlyDateFormat(),
//        readOnly:true,
//        value:WtfGlobal.getDates(true)
//    });
//    
//    this.endDate = new Wtf.form.DateField({
//        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  // 'To',
//        format:WtfGlobal.getOnlyDateFormat(),
//        readOnly:true,
//        name:'enddate',
//        value:WtfGlobal.getDates(false)
//    });
    
     this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.stockAgeing.QuickSearchEmptyText"), // "Search by Document no, Description, product id ,product name"
        width: 300,
        Store: this.StockAgeingStore
    });
   
    this.asOfDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.asOf"),  //'As of',
        name:'asofdate',
        id: 'asofdate'+config.id+config.helpmodeid,
        format:WtfGlobal.getOnlyDateFormat(),
        value:new Date(Wtf.serverDate.format('M d, Y')+" 12:00:00 AM")
    });
    this.asOfDate.on("change",this.checkDates,this);
    
    this.fetchBttn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  // 'Fetch',
        tooltip:WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"),  // "Select a time period to view corresponding transactions.",
        style:"margin-left: 6px;",
        iconCls:'accountingbase fetch',
        scope:this,
        handler:this.fetchStatement
    });
    
    this.exportButton=new Wtf.exportButton({
        obj:this,
        id:"exportReports"+this.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
//        disabled :true,
        hidden : this.isCustomWidgetReport,
        scope : this,
        filename:'Stock Ageing_v1',
        menuItem:{
            csv:true,
            pdf:false,
            rowPdf:false,
            xls:true,
            subMenu:true
        },
        params:{
//            enddate :  WtfGlobal.convertToGenericDate(this.endDate.getValue()),
//            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
//            start:0,
//            limit:this.pP.combo.value,
            asofdate : WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()),
            valuationMethod:this.isLifoCombo.getValue(),
            isStockAgeing : true
        },
        get:Wtf.autoNum.StockAgeing
    });
    
    this.exportButton.on("click", function() {
        this.exportButton.setParams({
            ss:this.quickPanelSearch.getValue(),
            asofdate : WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()),
            valuationMethod: this.isLifoCombo.getValue(),
            isStockAgeing : true
        });
    }, this);
    /*
     * Provided button to expand or collapse all row details. 
     */
    this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Collapse"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
        hidden:this.isSummery?true:false,
        scope: this,
        handler: function () {
            this.expandCollapseGrid(this.expandCollpseButton.getText());
        }
    });
    var TbarButtonArray = new Array();
    if (!this.isSummary) {
        TbarButtonArray.push("-");
        TbarButtonArray.push(this.quickPanelSearch);
        TbarButtonArray.push("-");
        TbarButtonArray.push(WtfGlobal.getLocaleText("acc.stockAgeing.valuation"));
        TbarButtonArray.push(this.isLifoCombo);
        TbarButtonArray.push("-");
        TbarButtonArray.push(WtfGlobal.getLocaleText("acc.common.asOf"));
        TbarButtonArray.push(this.asOfDate);
        TbarButtonArray.push(this.fetchBttn);
        TbarButtonArray.push(this.exportButton);
        TbarButtonArray.push(this.expandCollpseButton);
    } else {
        TbarButtonArray.push("-");
        TbarButtonArray.push(this.quickPanelSearch);
        TbarButtonArray.push("-");
        TbarButtonArray.push(WtfGlobal.getLocaleText("acc.common.asOf"));
        TbarButtonArray.push(this.asOfDate);
        TbarButtonArray.push(WtfGlobal.getLocaleText("acc.agedPay.interval"));
        TbarButtonArray.push(this.interval);
        TbarButtonArray.push("-");
        TbarButtonArray.push(this.typeEditor);
        TbarButtonArray.push(this.fetchBttn);
        TbarButtonArray.push(this.exportButton);
    }
        
    var gridSummary = new Wtf.grid.GroupSummary({});
    this.grid = new Wtf.grid.GridPanel({    
        store:this.StockAgeingStore,
        border:false,
        layout:'fit',
        view: this.isSummary?"":new Wtf.grid.GroupingView({
//            forceFit:true,
            startCollapsed :true
        }),
        plugins:this.isSummary?"":[gridSummary],
        loadMask:true,
        tbar: TbarButtonArray,
        columns:[
            {
                header:WtfGlobal.getLocaleText("acc.productList.gridProductID"), // "Product ID",
                dataIndex:"pid",
                hidden: false,
                fixed: true,
                pdfwidth:75,
                width:100,
                renderer:WtfGlobal.deletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"), // "Product Name",
                dataIndex:"productname",
//                renderer : function(val) {
//                    val = val.replace(/(<([^>]+)>)/ig,"");
//                    return "<div wtf:qtip=\""+val+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.productList.gridDescription")+"'>"+val+"</div>";
//                },
                width:100,
                renderer:WtfGlobal.deletedRenderer,
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.productList.gridDescription"), // "Description",
                dataIndex:"productDesc",
                renderer : function(val) {
                    val = val.replace(/(<([^>]+)>)/ig,"");
                    return "<div wtf:qtip=\""+val+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.productList.gridDescription")+"'>"+val+"</div>";
                },
                width:100,
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.stockLedger.DocumentNo"), // "Document #",
                dataIndex:"transactionNumber",
                hidden:this.isSummary?true:false,
                renderer:WtfGlobal.deletedRenderer,
                width:100,
                pdfwidth:75                
            },{
                header:WtfGlobal.getLocaleText("acc.stockAgeing.transactionType"), // "Transaction Type',
                dataIndex:"transactionType",
                hidden:this.isSummary?true:false,
                renderer:WtfGlobal.deletedRenderer,
                width:100,
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.stockLedger.Date"), // "Date",
                dataIndex:'transactionDate',
                align:'center',
                hidden:this.isSummary?true:false,
                summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'},
                renderer:WtfGlobal.onlyDateDeletedRenderer,
                width:100,
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.stockAgeing.Unit"), // "Unit",
                dataIndex:"unit",
                align:'right',
                hidden:this.isSummary?true:false,
                renderer:WtfGlobal.deletedRenderer,
                width:100,
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.stockAgeing.QuantityOfInterval45"), // "Quantity (0 - 45 Days)",
                dataIndex:"quantity1",
                align:'right',
                summaryType:'sum',
                width:100,
                hidden:this.isSummary?true:false,
                summaryRenderer: function(value, m, rec) {
                    return '<b>'+parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+'</b>';
                },
                renderer:this.unitRenderer,
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.stockAgeing.AmountOfInterval45"), // "Amount (0 - 45 Days)",
                dataIndex:"amount1",
                align:'right',
                hidden:this.isSummary?true:false,
                summaryType:'sum',
                width:100,
                hidecurrency : true,
                summaryRenderer: function(value, m, rec) {
                    if (value != 0) {
                        var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                        return retVal;
                    } else {
                        return '';
                    }
                },
                renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.stockAgeing.QuantityOfInterval90"), // "Quantity (45 - 90 Days)",
                dataIndex:"quantity2",
                hidden:this.isSummary?true:false,
                align:'right',
                width:100,
                summaryType:'sum',
                summaryRenderer: function(value, m, rec) {
                    return '<b>'+parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+'</b>';
                },
                renderer:this.unitRenderer,
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.stockAgeing.AmountOfInterval90"), // "Amount (45 - 90 Days)",
                dataIndex:"amount2",
                align:'right',
                hidden:this.isSummary?true:false,
                summaryType:'sum',
                width:100,
                hidecurrency : true,
                summaryRenderer: function(value, m, rec) {
                    var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                    return retVal;
                },
                renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.stockAgeing.QuantityOfIntervalOver90"), // "Quantity (Over 90 Days)",
                dataIndex:"quantity3",
                hidden:this.isSummary?true:false,
                align:'right',
                summaryType:'sum',
                width:100,
                summaryRenderer: function(value, m, rec) {
                    return '<b>'+parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+'</b>';
                },
                renderer:this.unitRenderer,
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.stockAgeing.AmountOfIntervalOver90"), // "Amount (Over 90 Days)",
                dataIndex:"amount3",
                align:'right',
                hidden:this.isSummary?true:false,
                summaryType:'sum',
                width:100,
                hidecurrency : true,
                summaryRenderer: function(value, m, rec) {
                    var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                    return retVal;
                },
                renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
                pdfwidth:75
            },{
            header:(!this.typeEditor.getValue()?"":"1-")+ this.interval.getValue()+" "+WtfGlobal.getLocaleText("acc.agedPay.days")+ (!this.typeEditor.getValue()?" "+WtfGlobal.getLocaleText("acc.agedPay.before")+" ":""),
            dataIndex:'quantitydue1',
            hidden:this.isSummary?false:true,
            width:150,
             pdfwidth:120,
            //summaryRenderer:this.isSummary?WtfGlobal.withoutRateCurrencySymbol:"",
            align:'right',
            renderer:this.unitRenderer,
//            summaryType:'sum',
//            summaryRenderer: function(value,m,rec){
//                var retVal = WtfGlobal.withoutRateCurrencySymbol(value,m,rec)
//                return '<b>'+retVal+'</b>';
//            }
         },{
             header:((!this.typeEditor.getValue()?"":(this.interval.getValue()*1+1)+"-")+(this.interval.getValue()*2))+" "+WtfGlobal.getLocaleText("acc.agedPay.days")+ (!this.typeEditor.getValue()?" "+WtfGlobal.getLocaleText("acc.agedPay.before")+" ":""),
            dataIndex:'quantitydue2',
            hidden:this.isSummary?false:true,
            width:150,
            pdfwidth:120,
//            summaryType:'sum',
//            summaryRenderer:WtfGlobal.withoutRateCurrencySymbol,
            align:'right',
            renderer:this.unitRenderer,
//            summaryType:'sum',
//            summaryRenderer: function(value,m,rec){
//                var retVal = WtfGlobal.withoutRateCurrencySymbol(value,m,rec)
//                return '<b>'+retVal+'</b>';
//            }
         },{
            header:(this.typeEditor.getValue()?((this.interval.getValue()*2+1)+"-"+(this.interval.getValue()*3)):(">"(this.interval.getValue()*2)))+" "+WtfGlobal.getLocaleText("acc.agedPay.days"),
            hidden:this.isSummary?false:true,
//            hidden:!this.isSummary || !this.typeEditor.getValue(),
            width:150,
            pdfwidth:120,
//            summaryType:'sum',
//            summaryRenderer:WtfGlobal.withoutRateCurrencySymbol,
            dataIndex:'quantitydue3',
            align:'right',
            renderer:this.unitRenderer,
//            summaryType:'sum',
//            summaryRenderer: function(value,m,rec){
//                var retVal = WtfGlobal.withoutRateCurrencySymbol(value,m,rec)
//                return '<b>'+retVal+'</b>';
//            }
        },
        {
            header:Wtf.marubishi_Company_Id == companyid? (this.typeEditor.getValue()?(">" + (this.interval.getValue()*3)):"" ) +" "+WtfGlobal.getLocaleText("acc.agedPay.days"):(this.typeEditor.getValue()?((this.interval.getValue()*3+1)+"-"+(this.interval.getValue()*4)):"")+" "+WtfGlobal.getLocaleText("acc.agedPay.days"),
           hidden:this.isSummary?false:true,
            width:150,
            pdfwidth:120,
            dataIndex:'quantitydue4',
            align:'right',
            renderer:this.unitRenderer,
        },{
            header: (this.typeEditor.getValue() ? ((this.interval.getValue() * 4 + 1) + "-" + (this.interval.getValue() * 5)) : "") + " " + WtfGlobal.getLocaleText("acc.agedPay.days"),
           hidden:this.isSummary?false:true,
            width: 150,
            pdfwidth: 120,
            dataIndex: 'quantitydue5',
            align: 'right',
            renderer:this.unitRenderer,
        },
        {
            header: (this.typeEditor.getValue() ? ((this.interval.getValue() * 5 + 1) + "-" + (this.interval.getValue() * 6)) : "") + " " + WtfGlobal.getLocaleText("acc.agedPay.days"),
            hidden:this.isSummary?false:true,
            width: 150,
            pdfwidth: 120,
            dataIndex: 'quantitydue6',
            align: 'right',
            renderer:this.unitRenderer,
        }, {
            header: (this.typeEditor.getValue() ? ( + (this.interval.getValue() * 6)) : " ") + "< 365 " + WtfGlobal.getLocaleText("acc.agedPay.days"),
            hidden:this.isSummary && this.interval.getValue()==30 ?false:true,
            width: 150,
            pdfwidth: 120,
            dataIndex: 'quantitydue7',
            align: 'right',
            renderer:this.unitRenderer,
        },{
            header:">365 " + WtfGlobal.getLocaleText("acc.agedPay.days"),
            hidden:this.isSummary && this.interval.getValue()==30 ?false:true,
            width: 150,
            pdfwidth: 120,
            dataIndex: 'quantitydue8',
            align: 'right',
            renderer:this.unitRenderer,
        }],
                bbar:this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.StockAgeingStore,           
                displayInfo: true,
                searchField: this.isLifoCombo,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"),
                plugins: this.pP = new Wtf.common.pPageSize({
                   id : "pPageSize_"+this.id
                })
            })
    });
    
    Wtf.account.TransactionListPanelViewStockAgeing.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.TransactionListPanelViewStockAgeing,Wtf.Panel, {
    onRender: function(config) {
        this.StockAgeingStore.load({
            params:{
                ss:this.quickPanelSearch.getValue(),
                start:0,
                limit: (this.pP.combo!=undefined) ? this.pP.combo.value:30,
                valuationMethod:this.isLifoCombo.getValue()
            }
        });
        
//        this.leadpan = new Wtf.Panel({
//            layout: 'border',
//            border: false,
//            attachDetailTrigger: true,
//            items: [{
//                region: 'center',
//                layout: 'fit',
//                border: false,
//                items: [this.grid],
//                tbar: [this.quickPanelSearch, this.resetBttn, '-', WtfGlobal.getLocaleText("acc.common.from"), this.startDate, WtfGlobal.getLocaleText("acc.common.to"), this.endDate, this.fetchBttn],
//                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
//                    pageSize: 30,
//                    id: "pagingtoolbar" + this.id,
//                    store: this.StockAgeingStore,
//                    searchField: this.quickPanelSearch,
//                    displayInfo: true,
//                    emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No results to display",
//                    plugins: this.pP = new Wtf.common.pPageSize({
//                        id : "pPageSize_"+this.id
//                    })
//                })
//            }]
//        }); 
//        this.add(this.leadpan);
        this.add(this.grid);
        
        Wtf.account.TransactionListPanelViewStockAgeing.superclass.onRender.call(this,config);
    },
    
//    handleResetClick:function() {
//        if(this.quickPanelSearch.getValue()) {
//            this.quickPanelSearch.reset();
//            this.fetchStatement();
//        }
//    },
    
    fetchStatement:function() {
//        this.sDate=this.startDate.getValue();
//        this.eDate=this.endDate.getValue();
//        
//        if(this.sDate > this.eDate){
//            WtfComMsgBox(1,2);
//            return;
//        }
//        
//        var fromdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
//        var todate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
//        if(this.isLifoCombo.getValue()){
//            this.StockAgeingStore.baseParams.isLifo = true;
//        }else{
//            this.StockAgeingStore.baseParams.isLifo = false;
//        }
        if(this.interval.getValue()==""|| this.interval.getValue()<=1){
              WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"),WtfGlobal.getLocaleText("acc.agedPay.msg1")], 2);   //"Alert","Please enter interval greater than one."], 2);
              return;
        }
        
        this.StockAgeingStore.load({
            params: {
//                startdate:fromdate,
//                enddate:todate,
                ss:this.quickPanelSearch.getValue(),
                asofdate : WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()),
                start:0,
                limit:this.pP.combo.value,
                valuationMethod:this.isLifoCombo.getValue()
            }
        });
        
        if(this.isSummary){ 
            //when interval and comulative/distributive changes we need to change Header name as well. 
            //eg if interval is 30 then header will be 1-30, when interval is 40 then header will be 1-40
            this.cm=this.grid.getColumnModel();
            for(var indexCount=0;indexCount<this.grid.getColumnModel().getColumnCount();indexCount++){
                var dataIndex=this.cm.getDataIndex(indexCount);
                 if(dataIndex=="quantitydue1"){
                    if(this.typeEditor.getValue()){
                        this.cm.setColumnHeader(indexCount,("1-"+ this.interval.getValue()+" "+WtfGlobal.getLocaleText("acc.agedPay.days"))) //1-30 days
                    } else {
                        this.cm.setColumnHeader(indexCount,(this.interval.getValue()+" "+WtfGlobal.getLocaleText("acc.agedPay.days")+ WtfGlobal.getLocaleText("acc.agedPay.before"))); //30 days before 
                    }                        
                }
                
                if(dataIndex=="quantitydue2"){                        
                    this.cm.setColumnHeader(indexCount,((this.interval.getValue()*1+1)+"-"+(this.interval.getValue()*2))+" "+WtfGlobal.getLocaleText("acc.agedPay.days")); //31-60 days                                         
                }
                if(dataIndex=="quantitydue3"){
                    if(this.typeEditor.getValue()){
                        this.cm.setColumnHeader(indexCount,((this.interval.getValue()*2+1)+"-"+(this.interval.getValue()*3))+" "+WtfGlobal.getLocaleText("acc.agedPay.days")); //61-90 days
                    } else {
                        this.cm.setColumnHeader(indexCount,(">"+(this.interval.getValue()*2)+" "+WtfGlobal.getLocaleText("acc.agedPay.days"))); //>60 days
                    }                        
                }
                if (dataIndex == "quantitydue4") {
                    if (this.typeEditor.getValue()) {
                        this.cm.setColumnHeader(indexCount,((this.interval.getValue()*3+1)+"-"+(this.interval.getValue()*4))+" "+WtfGlobal.getLocaleText("acc.agedPay.days")); //61-90 days

                    } else {
                        this.cm.setHidden(indexCount, true);
                    }
                }
                if(dataIndex=="quantitydue5"){
                    if(this.typeEditor.getValue() ){
                        this.cm.setColumnHeader(indexCount,((this.interval.getValue()*4+1)+"-"+(this.interval.getValue()*5))+" "+WtfGlobal.getLocaleText("acc.agedPay.days")); //121-150 days
                    } else {
                        this.cm.setHidden(indexCount, true);
                    }                        
                }
                if(dataIndex=="quantitydue6"){
                    if(this.typeEditor.getValue() ){
                        this.cm.setColumnHeader(indexCount,((this.interval.getValue()*5+1)+"-"+(this.interval.getValue()*6))+" "+WtfGlobal.getLocaleText("acc.agedPay.days")); //151-180 days
                    } else {
                        this.cm.setHidden(indexCount, true);
                    }
                }
                if(dataIndex=="quantitydue7"){
                    if(this.typeEditor.getValue() ){
                        this.cm.setColumnHeader(indexCount,(+(this.interval.getValue()*6)+" <365 "+WtfGlobal.getLocaleText("acc.agedPay.days"))); //>180 days
                    } else {
                        this.cm.setHidden(indexCount, true);
                    }
                }
            }
        }
        this.StockAgeingStore.on('loadexception', function() {
            WtfGlobal.resetAjaxTimeOut();
        }, this);
    },
    
    unitRenderer: function(value,metadata,record) {
        if(value != '') {
            value = parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        }
        return value;
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
    }
});
