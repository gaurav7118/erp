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

function callsaleByItemReport(){
    var saleByItemReport=Wtf.getCmp("saleByItemReport");
    if(saleByItemReport==null){
        callSaleByItem();
        saleByItemReport = new Wtf.account.SalesByItemDetail({
            id: 'saleByItemReport',
            border: false,
            layout: 'fit',
            iconCls: 'accountingbase agedpayable',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.saleByItem.detailReport"),Wtf.TAB_TITLE_LENGTH) ,
            tabTip:WtfGlobal.getLocaleText("acc.saleByItem.detailReportTT")  //'View your Sales By Item Detail Report from here.'
        });
        Wtf.getCmp('mainsalebyitem').add(saleByItemReport);
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewinvoice))
            saleByItemReport.on('invoice',callInvoiceList);
    }else{
    }
    Wtf.getCmp('mainsalebyitem').setActiveTab(saleByItemReport);
    Wtf.getCmp('mainsalebyitem').doLayout();
}

function callsaleByItemSummary(){
    var saleByItem=Wtf.getCmp("saleByItem");
    if(saleByItem==null){
        callSaleByItem()
        saleByItem = new Wtf.account.SalesByItem({
            id: 'saleByItem',
            border: false,
            layout: 'fit',
            iconCls: 'accountingbase agedpayable',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.saleByItem.summaryReport"),Wtf.TAB_TITLE_LENGTH) ,
            tabTip:WtfGlobal.getLocaleText("acc.saleByItem.summaryReportTT"),  //'View your Sales By Item Summary Report from here.'
            helpmodeid:21
        });
        Wtf.getCmp('mainsalebyitem').add(saleByItem);
    }else{
    }
    Wtf.getCmp('mainsalebyitem').setActiveTab(saleByItem);
    Wtf.getCmp('mainsalebyitem').doLayout();
}

function callSaleByItemDynamicLoad(){
    var panel = Wtf.getCmp("mainsalebyitem");
    if(panel==null){
        panel = new Wtf.TabPanel({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.saleByItem.tabTitle"),Wtf.TAB_TITLE_LENGTH) ,
            tabTip:WtfGlobal.getLocaleText("acc.saleByItem.tabTitleTT"), //'View your Item Sales report in Summary and Details from here.',
            id:'mainsalebyitem',
            closable:true,
            border:false,
            iconCls:'accountingbase agedrecievable',
            activeTab:1
        });
        Wtf.getCmp('as').add(panel);
        callsaleByItemSummary();
        callsaleByItemReport();

    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp("mainsalebyitem").setActiveTab("saleByItem");
    Wtf.getCmp('as').doLayout();
}

Wtf.account.SalesByItem=function(config){
    this.gridColumnModelArr=[];
    Wtf.apply(this, config);
    this.receivable=config.receivable||false;
    this.summary = new Wtf.ux.grid.GridSummary();
    this.prodRec = new Wtf.data.Record.create([{
            name:'productid'
        },{
            name:'productname'
        },{
            name:'pid'
        },{
            name:'quantity'
        },{
            name:'baseuomquantity'
        },{
            name:'avgsale'
        },{
            name:'amount'
        },{
            name:'cogs'
        },{
            name:'avgcogs'
        },{
            name:'margin'
        },{
            name:'permargin'
         },{
            name:'uomname'
        }]);
    this.prodStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.prodRec),
//        url: Wtf.req.account+"reporthandler.jsp",
        url : "ACCCombineReports/getSalesByItem.do",
        baseParams:{
            mode:1
        }
    });
    this.rowNo=new Wtf.KWLRowNumberer();
    this.prodStore.on('datachanged', function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    }, this);

    this.prodStore.on('load',this.storeloaded,this);
    
    this.gridColumnModelArr.push(this.rowNo,{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridProduct"),  //' Product Name',
            dataIndex:'productname',
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridProductID"),  //' Product ID',
            dataIndex:'pid',
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridQtySold"),  //"Quantity Sold",
            dataIndex:'quantity',
            align:'right',
            renderer:this.unitRenderer,
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.uomgrid.baseuomqty"),  //"Base UOM Quantity",
            dataIndex:'baseuomquantity',
            align:'right',
            renderer:this.unitRenderer,
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridTotalSales"),  //"Total Sales",
            dataIndex:'amount',
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridPurchaseCost"),  //"Purchase Cost",
            dataIndex:'cogs',
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridAvgPurchaseCost"),  //"Average Purchase Cost",
            dataIndex:'avgcogs',
            align:'right',
            renderer:WtfGlobal.withCurrencyUnitPriceRenderer,
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridProfitMargin"),  //"Profit Margin",
            dataIndex:'margin',
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridPercentageProfitMargin"),  //"Percentage Profit Margin",
            dataIndex:'permargin',
            align:'right',
            renderer:this.decimalRenderer.createDelegate(this),
            pdfwidth:100
        });
    
    
    this.gridColumnModelArr = WtfGlobal.appendCustomColumn(this.gridColumnModelArr,GlobalColumnModelForReports[30],true);
    
    this.grid = new Wtf.grid.GridPanel({
        id:"gridmsg"+config.helpmodeid+config.id,
        stripeRows :true,
        store:this.prodStore,
        border:false,
        layout:'fit',
        viewConfig:{
            forceFit:false,
            enableRowBody: true
        },
        forceFit:true,
        loadMask : true,
        cm:new Wtf.grid.ColumnModel(this.gridColumnModelArr)
    });
    
    var colModelArray = GlobalColumnModelForReports[30];
    WtfGlobal.updateStoreConfig(colModelArray, this.prodStore);
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });

    this.fromDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'stdate',
        value:WtfGlobal.getDates(true)
    });
    this.toDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
        value:WtfGlobal.getDates(false)
    });
    
    this.customReportViewBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
        handler: this.customizeView,
        iconCls: 'accountingbase fetch'
    });
    
    var btnArr=[];
    this.productRec = Wtf.data.Record.create ([
            {name:'productid'},
            {name:'productname'},
            {name:'pid'},
            {name:'desc'},
            {name:'uomid'},
            {name:'uomname'},
            {name:'parentid'},
            {name:'parentname'},
            {name:'purchaseaccountid'},
            {name:'salesaccountid'},
            {name:'purchaseretaccountid'},
            {name:'salesretaccountid'},
            {name:'reorderquantity'},
            {name:'quantity'},
            {name:'reorderlevel'},
            {name:'leadtime'},
            {name:'purchaseprice'},
            {name:'saleprice'},
            {name:'leaf'},
            {name:'currencysymbol'},
            {name:'currencyrate'},
            {name:'level'}
        ]);
        this.productStore = new Wtf.data.Store({
//            url:Wtf.req.account+'CompanyManager.jsp',
            url:"ACCProduct/getProductsForCombo.do",
            baseParams:{mode:22},
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productRec)
        });
//        this.productStore.load();

      btnArr.push(this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.saleByItem.search"),  //'Search by Product Name',
            width: 200,
            field: 'productname'
        }),this.resetBttn
        ,'-','From',this.fromDate,'-','To',this.toDate,'-',{
            xtype:'button',
            text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
            iconCls:'accountingbase fetch',
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.nee.9"),  //"Select a date to view sales by item.",
            handler:this.fetchAgedData
        }
    );
    
    btnArr.push(this.exportbtn = new Wtf.exportButton({
        obj:this,
        disabled:true,
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details.',
        id:"exportSalebyItemSummary",
        filename:WtfGlobal.getLocaleText("acc.dashboard.salesByItem")+"_v1", 
        menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
        get:913,
        isProductExport:true,
        label:WtfGlobal.getLocaleText("acc.saleByItem.summaryReport")
    }));
    
    btnArr.push(this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        disabled:true,
        filename:WtfGlobal.getLocaleText("acc.dashboard.salesByItem"),
        id:"printSalebyItemSummary",
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
        menuItem:{print:true},
        get:913,
        label:WtfGlobal.getLocaleText("acc.saleByItem.summaryReport")
    }),"->",this.customReportViewBtn);
    
    this.resetBttn.on('click',this.handleResetClick,this);
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        tbar:btnArr,
        items:[this.grid],
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.prodStore,
            searchField: this.quickPanelSearch,
            displayInfo: true,
//            displayMsg: 'Displaying records {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
            id : "pPageSize_"+this.id
            })
        })
    });

    Wtf.account.SalesByItem.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });

    this.prodStore.on("beforeload", function(s, o) {
        o.params.fromDate = WtfGlobal.convertToGenericDate(this.fromDate.getValue());
        o.params.toDate = WtfGlobal.convertToGenericDate(this.toDate.getValue());
        Wtf.Ajax.requestEx({
            url: "ACCAccountCMN/getCustomizedReportFields.do",
            params: {
                flag: 34,
                moduleid: 30,
                reportId: 1,
                isFormField: false,
                isLineField: false
            }
        }, this, function(action, response) {
            if (action.success && action.data != undefined) {
                this.customizeData = action.data;
                var cm = this.grid.getColumnModel();
                for (var i = 0; i < action.data.length; i++) {
                    for (var j = 0; j < cm.config.length; j++) {
                        if (cm.config[j].header == action.data[i].fieldDataIndex || (cm.config[j].dataIndex == action.data[i].fieldDataIndex && cm.config[j].header == action.data[i].fieldname)) {
                            cm.setHidden(j, action.data[i].hidecol);
                        }

                    }
                }
                this.grid.reconfigure(s, cm);
            }
        }, function() {
        });
        this.fetchAgedData1(false);
    }, this);
    
    this.prodStore.load({
        params:{
            start:0,
            limit:30,
            creditonly:true
        }
    });
}

Wtf.extend( Wtf.account.SalesByItem,Wtf.Panel,{
    decimalRenderer:function(val){
        return'<div class="currency">'+WtfGlobal.conventInDecimal(val,"")+'%</div>';
    },
    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.prodStore.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value,
                    aged:true,
                    creditonly:true
                }
            });
        }
    },

    changeRowColor:function(record){
        var dueDate = record.data['duedate'];
        var currentDate=new Date(new Date().format('M d, Y'))
        if(currentDate>dueDate)
            return 'red-background';
        return 'yellow-background';
    },

    storeloaded:function(store){
        this.quickPanelSearch.StorageChanged(store);
        if(store.getCount()==0){
            if(this.exportbtn)this.exportbtn.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.exportbtn)this.exportbtn.enable();
            if(this.printButton)this.printButton.enable();
        }
    },

    fetchAgedData:function(){
        this.prodStore.load({
            params:{
                start:0,
                limit:this.pP.combo.value,
                creditonly:true,
                ss : this.quickPanelSearch.getValue()
            }
        });
        this.fetchAgedData1(true);   
    },
    
    fetchAgedData1:function(limit){
        this.printButton.setParams({
        	fromDate:WtfGlobal.convertToGenericDate(this.fromDate.getValue()),
        	toDate:WtfGlobal.convertToGenericDate(this.toDate.getValue()),
            start:0,
            limit:limit?this.pP.combo.value:30,
            creditonly:true,
            name: WtfGlobal.getLocaleText("acc.saleByItem.summaryReport"),
            filetype: 'print'
        });
        this.exportbtn.setParams({
        	fromDate:WtfGlobal.convertToGenericDate(this.fromDate.getValue()),
        	toDate:WtfGlobal.convertToGenericDate(this.toDate.getValue()),
            start:0,
            limit:limit?this.pP.combo.value:30,
            creditonly:true,
            name: WtfGlobal.getLocaleText("acc.saleByItem.summaryReport")
        });
    },
     unitRenderer:function(value,metadata,record){
        var unit=record.data['uomname'];
            value=parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+" "+unit;
        return value;
    },
    customizeView: function() {
        this.customizeViewWin = new Wtf.customizeView({
            scope: this,
            moduleid: 30,
            parentHelpModeId: this.helpmodeid,
            parentId: this.id,
            isForFormFields: false
        });
        this.customizeViewWin.show();
    }
    
});
