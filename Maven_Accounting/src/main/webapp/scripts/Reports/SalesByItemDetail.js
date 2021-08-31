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
Wtf.account.SalesByItemDetail=function(config){
    this.rec = new Wtf.data.Record.create([{
         name:'productid'
        },{
         name:'invoiceid'
        },{
         name:'billno'
        },{
           name:'personname'
        },{
           name:'productname'
        },{
           name:'pid'
        },{
           name:'productdescription'
        },{
            name:'quantity'
        },{
            name:'promisedDate', type:'date'
        },{
            name:'status'
        },{
            name:'rateinbase'
        },{
            name:'amount'
        },{
            name:'totalsales'
        },{
            name:'date', type:'date'
        },{
            name:'totalquantity'
        },{
            name:'totalsales'
        },{
            name:'memo'

        },{
            name:'isinvoice'

        }]);

        this.store =new Wtf.data.GroupingStore({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.rec),
            groupField:'productname',
            sortInfo: {field: 'productname',direction: "DESC"},
            url : "ACCCombineReports/getDetailedSalesByItem.do"
        });

    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });

    this.startDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'startdate',
        value:WtfGlobal.getDates(true)
    });
    this.endDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
        value:WtfGlobal.getDates(false)
    });
    this.rowNo=new Wtf.KWLRowNumberer();
    this.cm= new Wtf.grid.ColumnModel([this.rowNo,{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridProduct"),  //"Product Name",
            dataIndex:'productname',
            align:'left',
            sortable: true,
            groupable: true,
            groupRenderer: function(v){return v},
            pdfwidth:100            
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridProductID"),  //"Product ID",
            dataIndex:'pid',
            align:'left',
            sortable: true,
            groupable: true,
            groupRenderer: function(v){return v},
            pdfwidth:100            
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"),  //"Product Description",
            dataIndex:'productdescription',
            align:'left',
            sortable: true,
            groupable: true,
            groupRenderer: function(v){return v},
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridInvoice"),  //"Transaction Number",
            dataIndex:'billno',
            sortable: true,
            groupable: true,
            groupRenderer: function(v){return v},
            renderer:WtfGlobal.linkRenderer,
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridDate"),  //"Date",
            dataIndex:'date',
            align:'center',
            groupRenderer:this.groupDateRender.createDelegate(this),
            renderer:WtfGlobal.onlyDateRenderer,
            pdfwidth:100
        },{
            header:Wtf.account.companyAccountPref.descriptionType,  //"Memo",
            align:'left',
            dataIndex:'memo',
            pdfwidth:100
        },{

            header:WtfGlobal.getLocaleText("acc.saleByItem.gridCustName"),  //"Customer Name",
            dataIndex:'personname',
            align:'left',
            sortable: true,
            groupable: true,
            groupRenderer: function(v){return v},
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.agedPay.dueDate"),
            dataIndex:'promisedDate',
            align:'center',
            renderer:WtfGlobal.onlyDateRenderer,
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridQty"),  //"Quantity",
            align:'right',
            dataIndex:'quantity',
            pdfwidth:100,
            renderer:function(val){
                return parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            }
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridSalesPrice"),  //"Sales Price",
            dataIndex:'rateinbase',
            align:'right',
            renderer:WtfGlobal.withCurrencyUnitPriceRenderer,
            pdfwidth:100
          },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridAmount"),  //"Amount",
            dataIndex:'amount',
            align:'right',
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            pdfwidth:100
         },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridBalance"),  //"Balance",
            dataIndex:'totalsales',
            align:'right',
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            pdfwidth:100

    },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.status"),
            align:'center',
            dataIndex:'status',
            pdfwidth:100,
            renderer:function(v){
                if(v && v=="Open")
                    return '<div style="color:blue">'+v+'</div>';
                else
                    return v;
            }
        }]);
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.store,
        cm:this.cm,
        border:false,
        layout:'fit',
        view:new Wtf.grid.GroupingView({
            forceFit:true,
            startCollapsed :true
       }),
        loadMask : true
    })

    var btnArr=[];
    btnArr.push(
       this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.saleByItem.search")+", "+ WtfGlobal.getLocaleText("acc.productList.gridProductID"),  //'Search by Product Name, Product Id' ,
            id:"quickSearch"+config.helpmodeid,
            width: 200,
            field: 'productname'
        }),this.resetBttn,'-',WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),this.endDate, '-',{
            xtype:'button',
             text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
            iconCls:'accountingbase fetch',
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.SalesByItem.ReportView"),//"Select a date to view Sales by Item Detail Report.",
            handler:this.loadStore
        });
    btnArr.push(this.exportbtn = new Wtf.exportButton({
        obj:this,
        disabled:true,
        filename:WtfGlobal.getLocaleText("acc.dashboard.salesByItem")+"_v1",
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details.',
        id:"exportSalebyItemDetail",
        menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
        get:914,
        isProductExport:true,
        label:WtfGlobal.getLocaleText("acc.saleByItem.detailReport")
    }));
    btnArr.push(this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        disabled:true,
        filename:WtfGlobal.getLocaleText("acc.dashboard.salesByItem"),
        id:"printSalebyItemDetail",
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",
        menuItem:{print:true},
        get:914,
        label:WtfGlobal.getLocaleText("acc.saleByItem.detailReport")
    }));
    /*
     * Provided button to expand or collapse all row details. 
     */
    btnArr.push(this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Collapse"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
        scope: this,
        handler: function () {
            this.expandCollapseGrid(this.expandCollpseButton.getText());
        }
    }));
    this.resetBttn.on('click',this.handleResetClick,this);
    if(config.helpmodeid!=null){
        btnArr.push("->");
        btnArr.push(getHelpButton(this,config.helpmodeid));
    }
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        tbar:btnArr,
        items:[this.grid],
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.store,
            searchField: this.quickPanelSearch,
            displayInfo: true,
            //displayMsg: WtfGlobal.getLocaleText("acc.rem.116"),
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
            })
        })
    });

    Wtf.account.SalesByItemDetail.superclass.constructor.call(this,config);
    this.addEvents({
        'invoice':true
    });


    this.store.on('load',this.storeloaded,this);
    this.store.on('datachanged', function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        this.expandButtonClicked = false;
    }, this);
    this.store.on("beforeload", function(s,o){
       WtfGlobal.setAjaxTimeOutFor30Minutes();
       s.baseParams= {
                fromDate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                toDate: WtfGlobal.convertToGenericDate(this.endDate.getValue())
            },
       this.setExportParams(false);
    },this);
    this.store.on('loadexception', function() {
        WtfGlobal.resetAjaxTimeOut();
    }, this);
    this.store.load({
        params:{
            start:0,
            limit:30
        }
    });
    this.grid.on('cellclick',this.onCellClick, this);
}

Wtf.extend( Wtf.account.SalesByItemDetail,Wtf.Panel,{
    groupDateRender:function(v){
       return v.format(WtfGlobal.getOnlyDateFormat())
    },

    totalRender:function(v,m,rec){
        var val=WtfGlobal.withoutRateCurrencySymbol(v,m,rec);
       return "<b>"+val+"</b>"
    },
    loadStore:function(){
        this.store.load({
            params: {
                start:0,
                limit:this.pP.combo.value,
                ss : this.quickPanelSearch.getValue()
            }
        });
        this.setExportParams(true);
    },
    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.loadStore();
        }
    },

    storeloaded:function(store){
        WtfGlobal.resetAjaxTimeOut();
        this.quickPanelSearch.StorageChanged(store);
        if(store.getCount()==0){
            if(this.exportbtn)this.exportbtn.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.exportbtn)this.exportbtn.enable();
            if(this.printButton)this.printButton.enable();
        }
    },

    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="billno"){
            var invoiceid=this.store.getAt(i).data['invoiceid'];
            this.fireEvent('invoice',invoiceid,true);
        }
    },

    setExportParams:function(limit){
        this.printButton.setParams({
        	fromDate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
        	toDate:WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            start:0,
            limit:limit?this.pP.combo.value:30,
            name: WtfGlobal.getLocaleText("acc.saleByItem.detailReport"),
            filetype: 'print'
        });
        this.exportbtn.setParams({
        	fromDate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
        	toDate:WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            start:0,
            limit:limit?this.pP.combo.value:30,
            name: WtfGlobal.getLocaleText("acc.saleByItem.detailReport")
        });
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
