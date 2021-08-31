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


function showStockStatusReportTabDynamicLoad(params) {
    /*
     *Function to add this report in widget report.
     **/
    if (params.isCustomWidgetReport) {
        var panel = new Wtf.account.stockStatusGrid({
            layout: 'fit',
            closable: true,
            isCustomWidgetReport : params.isCustomWidgetReport,
            border: false
        });
        if (params.callbackFn) {
            /*
             *call callback function to add this report to widget.
             **/
            params.callbackFn.call(this, panel);
        }
    } else {
        var stockStatus = Wtf.getCmp("stockStatus");
    if(stockStatus==null){
            stockStatus = new Wtf.account.stockStatusGrid({
            title:WtfGlobal.getLocaleText("acc.stockStatusReport"),  //"Stock Status",
            tabTip:WtfGlobal.getLocaleText("acc.stockStatusReport"),  //"Stock Status",
            id:"stockStatus",
            iconCls :getButtonIconCls(Wtf.etype.inventoryval),
            layout:'fit',
            closable:true,
            border:false
            });
            Wtf.getCmp('as').add(stockStatus);
        }
        Wtf.getCmp('as').setActiveTab(stockStatus);
        Wtf.getCmp('as').doLayout();
    }
}

Wtf.account.stockStatusGrid = function (config){    
    this.isCustomWidgetReport = config.isCustomWidgetReport;
    this.createGrid();

    Wtf.apply(this,{
            border:false,
            layout : "fit",
            tbar:this.btnArr,
            items:[this.grid],
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.store,
            searchField: this.quickPanelSearch,
            displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.sales.norec"),  //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
                            id : "pPageSize_"+this.id
            })
        })
    });
//    this.fetchStatement(); // To Avoid initial page load request

    Wtf.account.stockStatusGrid.superclass.constructor.call(this,config);   

}

Wtf.extend(Wtf.account.stockStatusGrid,Wtf.Panel,{
    createGrid:function(){
        this.startDate=new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
            name:'stdate',
            format:WtfGlobal.getOnlyDateFormat(),
            value:this.getDates(true)
        });
        this.endDate=new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
            format:WtfGlobal.getOnlyDateFormat(),
            name:'enddate',
            value:this.getDates(false)

        });

       this.gridRec = Wtf.data.Record.create ([
            {name:'productid'},
            {name:'productname'},
            {name:'pid'},
            {name:'productdesc'},
            {name:'productType'},
            {name:'onhand'},
            {name:'poQty'},
            {name:'soQty'},
            {name:'netQty'},
            {name:'uom'},
        ]);

        this.sdate=WtfGlobal.convertToGenericDate(this.startDate.getValue().add(Date.DAY,0));
        this.edate=WtfGlobal.convertToGenericDate(this.endDate.getValue().add(Date.DAY,0));

        this.store = new Wtf.data.Store({
            url : "ACCReports/getStockStatus.do",
            baseParams:{mode:29,isprovalreport:true},
            params:{
                stdate:this.sdate,
                enddate:this.edate,
                start:0,
                limit:30
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.gridRec)
        });

        this.gridcm = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header:WtfGlobal.getLocaleText("acc.invReport.prod"),  //"Product",
                dataIndex:'productname',
                width:100,
                pdfwidth:150
            },{
                header:WtfGlobal.getLocaleText("acc.productList.gridProductID"),  //"Product ID",
                dataIndex:'pid',
                width:100,
                pdfwidth:150
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.type"),  //"Product Type",
                dataIndex:'productType',
                width:100,
                pdfwidth:150
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.desc"),  //"Description",
                dataIndex:'productdesc',
                width:100,
                pdfwidth:150,
                renderer : function(val) {
                    return "<div wtf:qtip=\'"+val+"\' wtf:qtitle='"+WtfGlobal.getLocaleText("acc.invReport.desc")+"'>"+val+"</div>";
                }
            },
            {
                header:WtfGlobal.getLocaleText("acc.invReport.qty"),  //"Quantity On Hand",
                dataIndex:'onhand',
                pdfwidth:75,
                width:100,
                // summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'},
                align:'right',
                 renderer:function(val){
                     val= parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                    return val;
                }
            },
            {
                header:"UOM",  //"Stock UOM",
                dataIndex:'uom',
                pdfwidth:75,
                width:100,
                align:'right'
            },
            {
                header:WtfGlobal.getLocaleText("acc.stockStatusReport.poQty"),  //"Outstanding Purchase Order",
                dataIndex:'poQty',
                pdfwidth:75,
                width:100,
                // summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'},
                align:'right',
                renderer:WtfGlobal.linkDeletedRenderer
//                renderer:function(val){
//                    return parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)
//                }
            },
            {
                header:WtfGlobal.getLocaleText("acc.stockStatusReport.soQty"),  //"Outstanding Sales Order",
                dataIndex:'soQty',
                pdfwidth:75,
                width:100,
                // summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'},
                align:'right',
                 renderer:WtfGlobal.linkDeletedRenderer
//                renderer:function(val){
//                    return parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
//                }
            },
            {
                header:WtfGlobal.getLocaleText("acc.stockStatusReport.netQty"),  //"Net Quantity",
                dataIndex:'netQty',
                width:100,
                pdfwidth:75,
                // summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'},
                align:'right',
                 renderer:function(val){
                    return parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)
                }
            }
        ]);
        this.summary = new Wtf.ux.grid.GridSummary();
        this.btnArr=[];

        this.resetBttn=new Wtf.Toolbar.Button({
		text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
		hidden:this.isCustomWidgetReport,
		tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
                id: 'btnRec' + this.id,
                scope: this,
		iconCls :getButtonIconCls(Wtf.etype.resetbutton),
		disabled :false
        });
        this.resetBttn.on('click',this.handleResetClick,this);

        this.btnArr.push(
                this.quickPanelSearch = new Wtf.KWLTagSearch({
                emptyText:WtfGlobal.getLocaleText("acc.stockStatusReport.searchproduct")+", "+WtfGlobal.getLocaleText("acc.product.gridProductID"), // Search by Customer Name
//                                id:"quickSearch"+config.helpmodeid,
                                width:  this.isCustomWidgetReport ? 150 : 200,
                                hidden:false,
                    field: 'customername'
        }),this.resetBttn);
        this.btnArr.push('-',WtfGlobal.getLocaleText("acc.common.from"),this.startDate,
                  WtfGlobal.getLocaleText("acc.common.to"),this.endDate,'-',{
                xtype:'button',
                text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
                iconCls:'accountingbase fetch',
                tooltip:WtfGlobal.getLocaleText("acc.common.fetchTT"),  //"Select a time period to view corresponding records.",
                scope:this,
                handler:this.fetchStatement
        });

        this.grid = new Wtf.grid.GridPanel({
            layout:'fit',
            region:"center",
            store: this.store,
            cm:this.gridcm,
            border : false,
            loadMask : true,
            plugins:[this.summary],
            viewConfig: {
                //forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer( WtfGlobal.getLocaleText('acc.common.norec') + "<br>" +WtfGlobal.getLocaleText('acc.common.norec.click.fetchbtn'))
            }
        });
        
         this.grid.on('cellclick',this.onCellClick, this);

        this.expGet = Wtf.autoNum.StockStatus;
        this.expButton=new Wtf.exportButton({
		obj:this,
		text:WtfGlobal.getLocaleText("acc.common.export"),
		tooltip :WtfGlobal.getLocaleText("acc.sales.exportTT"),  //'Export report details',
		 hidden : this.isCustomWidgetReport,
                disabled :true,
                params:{ 
                stdate: this.sDate,
                enddate: this.eDate,
                ss:this.quickPanelSearch.getValue()
            },
                filename:WtfGlobal.getLocaleText("acc.stockStatusReport")+"_v1",
		menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
		get:this.expGet
        })
//        if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType))
        this.btnArr.push(this.expButton);

        this.store.on('load', function(store) {
            WtfGlobal.resetAjaxTimeOut();
            if(this.store.getCount()<1) {
                this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.grid.getView().refresh();
                if(this.expButton)this.expButton.disable();
            } else {
                if(this.expButton)this.expButton.enable();
            }
            this.quickPanelSearch.StorageChanged(store);
        }, this);

        this.store.on('datachanged', function() {
            var p = this.pP.combo.value;
            this.quickPanelSearch.setPage(p);
        }, this);
        
        this.grid.on("render",function(){
            this.grid.getView().applyEmptyText(); 
        },this);
    },
    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.fetchStatement();
        }
    },
    summaryRenderer:function(val){
        return WtfGlobal.currencyRenderer(Math.abs(val));
    },
    fetchStatement:function(){
        this.sDate=this.startDate.getValue();
        this.eDate=this.endDate.getValue();

        if(this.sDate > this.eDate){
            WtfComMsgBox(1,2);
            return;
        }
        this.sdate=WtfGlobal.convertToGenericDate(this.startDate.getValue().add(Date.DAY,0));
        this.edate=WtfGlobal.convertToGenericDate(this.endDate.getValue().add(Date.DAY,0));
        if(this.sDate>this.eDate){
            WtfComMsgBox(2,2);
            return;
        }
        this.store.on('beforeload', function() {
            WtfGlobal.setAjaxTimeOut();
            var currentBaseParams = this.store.baseParams != undefined ? this.store.baseParams : {};
            currentBaseParams.stdate = this.sdate;
            currentBaseParams.enddate = this.edate;
            this.store.baseParams=currentBaseParams;
        }, this);
        this.store.load({params:{
            stdate:this.sdate,
            enddate:this.edate,
            start:0,
            limit:(this.pP.combo==undefined?30:this.pP.combo.value),
            ss:this.quickPanelSearch.getValue()
        }});

        this.expButton.setParams({
            stdate: this.sDate,
            enddate: this.eDate,
            ss:this.quickPanelSearch.getValue()
        });
    this.store.on('loadexception', function() {
            WtfGlobal.resetAjaxTimeOut();
        }, this);
    },

    getDates:function(start){
        var d=new Date();
        if(this.statementType=='BalanceSheet'){
            if(start){
                return new Date('January 1, 1970 00:00:00 AM');
            }else
                return d;

        }
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    
onCellClick:function(g,i,j,e){
 
    var header=g.getColumnModel().getDataIndex(j);
    if(header=="soQty"||header=="poQty"){
        this.viewOutStandingTransection(g,i,e,header);
    }
},

viewOutStandingTransection: function(grid, rowIndex, columnIndex,header){
     this.storURL="";
    this.expandRec = Wtf.data.Record.create ([
            {
                "name":"baseuomquantity"
            },
            {
                "name":"pid"
            },
            {
                "name":"balanceQuantity"
            },
            {
                "name":"productid"
            }
            ]);
            
            this.winTitle="Purchase Order Outstading ";
            
            this.expandStoreUrl = 'ACCPurchaseOrderCMN/getPurchaseOrderRows.do';
            if(header=="soQty"){
                this.winTitle="Sales Order Outstading ";
                this.expandStoreUrl="ACCSalesOrderCMN/getSalesOrderRows.do";
            }
    
            this.expandStore = new Wtf.data.Store({
                url:this.expandStoreUrl ,
                baseParams:{
                    mode:43,
                    dtype : 'report', // Display type report/transaction, used for quotation
                    isNormalContract:this.isNormalContract
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                },this.expandRec)
            });
            
    this.sdate=WtfGlobal.convertToGenericDate(this.startDate.getValue().add(Date.DAY,0));
    this.edate=WtfGlobal.convertToGenericDate(this.endDate.getValue().add(Date.DAY,0));
    var formrec = this.grid.getStore().getAt(rowIndex);
    
       this.expander = new Wtf.grid.RowExpander({
//                tpl :this.tmplt
            });
    this.expander.on("expand",this.onRowexpand,this);
    this.expandStore.on('load',this.fillExpanderBody,this);
    this.gridrec = new Wtf.data.Record.create([
        {
            name:"productid"
        },
        {
            name:"productname"
        },

        {
            name:"billno"
        },
        {
            name:"billid"
        },
        {
            name:"status"
        },

        {
            name:"poQty"
        },
        {
            name:"soQty"
        }
        ]);
        
        this.gridReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.gridrec);
        
        if(header=="poQty"){
            this.storURL="ACCPurchaseOrderCMN/getPurchaseOrdersMerged.do";
        }else{
            this.storURL="ACCSalesOrderCMN/getSalesOrdersMerged.do";
        }
        
        this.gridStore = new Wtf.data.Store({
        url :this.storURL,
        reader:this.gridReader,
           
        baseParams:{
            archieve:0,
            deleted:false,
            nondeleted:false,
            cashonly:(this.cash == undefined)?false:this.cash,
            creditonly:false,
            consolidateFlag:false,
            companyids:companyids,
            enddate:this.edate,
            gcurrencyid:gcurrencyid,
            userid:loginid,
            isfavourite:false,
            startdate:this.sdate,
//            ss:"",
            isOutstanding:true,
            productid:formrec.data.productid
                
        }
    });
 
 
    this.gridStore.load();
 
    var sm = new Wtf.grid.CheckboxSelectionModel({
        width:25
    });
    var cm = new Wtf.grid.ColumnModel([
        sm,
        this.expander,
        new Wtf.grid.RowNumberer(),
        {
            header:WtfGlobal.getLocaleText("acc.invReport.prod"),  //"Product",
            dataIndex:'productname',
            width:100,
            pdfwidth:150,
            hidden:true
        },{
            header:WtfGlobal.getLocaleText("Transaction Number"),  //"Product",
            dataIndex:'billno',
            width:100,
            pdfwidth:150,
            renderer:WtfGlobal.linkDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.stockStatusReport.poQty"),  //"Outstanding Purchase Order",
            dataIndex:'poQty',
            pdfwidth:75,
            width:100,
            align:'right',
            hidden:true
        },{
            header:WtfGlobal.getLocaleText("acc.stockStatusReport.soQty"),  //"Outstanding Sales Order",
            dataIndex:'soQty',
            pdfwidth:75,
            width:100,
            align:'right',
            hidden:true
        },{
            header:WtfGlobal.getLocaleText("Status"),  
            dataIndex:'status',                       
            pdfwidth:75,
            width:100
        }]);
        
    this.SoPOGrid=new Wtf.KwlEditorGridPanel({
        region: 'center',
        border: false,
        store: this.gridStore,
        cm: cm,
        sm:sm,
        loadMask : true,
        serverSideSearch:true,
        displayInfo: true,
        plugins : this.expander,
        searchField:"billno",
        layout:'fit',
        viewConfig: {
            forceFit: true
        }
    })

    var SoPOOutstandingWindow = new Wtf.Window({
        id:'soutstandingwindowid',
        title : this.winTitle,
        modal : true,
        scope:this,
        iconCls : 'iconwin',
        minWidth:100,
        width : 800,
        height: 400,
        resizable :true,
        scrollable:true,
        buttonAlign : 'right',
        layout : 'border',
        items :[{
            region : 'center',
            border : false,
            //                bodyStyle : 'background:#f1f1f1;font-size : 10px;padding:20px 0px 0px 0px;',
            layout : 'fit',
            items : [this.SoPOGrid]
        }]
        
    }).show();  
    
       
},
onRowexpand:function(scope, record, body){
    var colModelArray = GlobalColumnModel[this.moduleid];
    WtfGlobal.updateStoreConfig(colModelArray,this.expandStore);
    this.expanderBody=body;
    this.expandStore.load({
        params: {
            productid:record.get("productid"),
            bills:record.get("billid"),
            isOutstanding:true,
            mode:43
        }
    });
},
     
    fillExpanderBody: function() {
        if(this.expandStore.getCount() > 0) {
                this.custArr = [];
            this.custArr = WtfGlobal.appendCustomColumn(this.custArr, GlobalColumnModel[this.moduleid]);
            var colModelArray = GlobalColumnModel[this.moduleid];
            WtfGlobal.updateStoreConfig(colModelArray, this.expandStore);
            
            var arr = [
            "Product ID", // "Product ID"
            "Quantity",
            "Balance Quantity"];
        
            var gridHeaderText = WtfGlobal.getLocaleText("acc.invoiceList.expand.pList"); // "Product List";
        
            var header = "<span class='gridHeader'>"+gridHeaderText+"</span>"; // "Product List"
            
            var count=0;
            for(var i=0;i<arr.length;i++){
                if(arr[i] != ""){
                    count++;
                }
            }
            var widthInPercent=100/(count+1);
            var minWidth = count*100;
            header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
            header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
            for(i=0;i<arr.length;i++){
                header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[i] + "</span>";
            }
            header += "</div><div style='width: 100%;min-width:"+minWidth+"px'><span class='gridLine'></span></div>";   
            header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
        
            for(i=0; i<this.expandStore.getCount(); i++) {
                var rec=this.expandStore.getAt(i);
            
            header += "<span class='gridNo'>"+(i+1)+".</span>";           
            header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'' wtf:qtip='"+rec.data['pid']+"'>&nbsp;"+Wtf.util.Format.ellipsis(rec.data['pid'],20)+"</span>"; 
            header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'' wtf:qtip='"+rec.data['baseuomquantity']+"'>&nbsp;"+Wtf.util.Format.ellipsis(rec.data['baseuomquantity'],20)+"</span>"; 
            header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'' wtf:qtip='"+rec.data['balanceQuantity']+"'>"+Wtf.util.Format.ellipsis(rec.data['balanceQuantity'],20)+"</span>"; 
             
            header +="<br>";
            
            }
            header += "</div>";
            var disHtml = "<div class='expanderContainer' style='width:100%'>" + header + "</div>";
            this.expanderBody.innerHTML = disHtml;
        } else {
            this.expanderBody.innerHTML = "<div class='expanderContainer' style='width:100%'>" + WtfGlobal.getLocaleText("acc.field.Nodatatodisplay") + "</div>"; // "No data to display"
        }
    }

});
