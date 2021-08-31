/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

function getVatReportsDynamicLoad(isCommodity,isSales){
    var panel=isCommodity?(isSales?Wtf.getCmp("vatCommoditySalesRegister"):Wtf.getCmp("vatCommodityPurchaseRegister")):(isSales?Wtf.getCmp("vatSalesRegister"):Wtf.getCmp("vatPurchaseRegister"));
    if(panel==null){
        panel=new Wtf.account.vatReports({
            title: isCommodity?(isSales?WtfGlobal.getLocaleText('acc.vcsrreport.title'):WtfGlobal.getLocaleText('acc.vcprreport.title')):(isSales?WtfGlobal.getLocaleText('acc.vsrreport.title'):WtfGlobal.getLocaleText('acc.vprreport.title')),
            tabTip: isCommodity?(isSales?WtfGlobal.getLocaleText('acc.vcsrreport.title'):WtfGlobal.getLocaleText('acc.vcprreport.title')):(isSales?WtfGlobal.getLocaleText('acc.vsrreport.title'):WtfGlobal.getLocaleText('acc.vprreport.title')),
            id:isCommodity?(isSales?'vatCommoditySalesRegister':'vatCommodityPurchaseRegister'):(isSales?'vatSalesRegister':'vatPurchaseRegister'),
            isSales:isSales,
            isCommodity:isCommodity,
            border:false,
            layout:'fit',
            closable:true,
            isvatReports : true,
            iconCls:'accountingbase invoicelist'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}


Wtf.account.vatReports=function(config){
    Wtf.apply(this, config);
    this.isCommodity = config.isCommodity;
    var buttonArray = new Array();
    
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.coa.accountSearchText"), // "Search by Document Account Name
        width: 150,
        id:"quickSearch"+this.id
    });
    
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate' + this.id,
        format:WtfGlobal.getOnlyDateFormat(),
        value:WtfGlobal.getDates(true)
    });
    
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate' + this.id,
        value:WtfGlobal.getDates(false)
    });
  
    
    this.fetchBtn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.agedPay.fetch"),  //'Fetch',           
        scope: this,
        id:'fetchBtn'+this.id,
        tooltip: WtfGlobal.getLocaleText("acc.invReport.fetchTT"), 
        handler: this.fetchData,
        iconCls:'accountingbase fetch'
    });
    
    this.exportBtn = new Wtf.exportButton({
        text: WtfGlobal.getLocaleText("acc.common.export"), //Export
        tooltip: WtfGlobal.getLocaleText("acc.common.export"), //Export
        obj: this,
        iconCls: (Wtf.isChrome ? 'pwnd exportChrome' : 'pwnd export'),
        filename: this.isCommodity?(this.isSales?WtfGlobal.getLocaleText('acc.vcsrreport.title'):WtfGlobal.getLocaleText('acc.vcprreport.title')):(this.isSales?WtfGlobal.getLocaleText('acc.vsrreport.title'):WtfGlobal.getLocaleText('acc.vprreport.title')),
        menuItem: {
            csv:false,
            pdf:false,
            rowPdf:false,
            xls:true
        },
        get: this.isSales ? Wtf.autoNum.VATSalesRegister : Wtf.autoNum.VATPurchaseRegister
    });
    
    this.resetBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
        hidden: this.isSummary,
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        disabled: false
    });
    this.resetBttn.on('click',this.handleResetClickNew,this);
    
    this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        id:"printrg23part1",
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
        menuItem:{
            print:true
        },
        get:Wtf.autoNum.BankBookSummary,
        label:WtfGlobal.getLocaleText("acc.ccReport.tab3")
    });
        
    buttonArray.push(WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),
        this.endDate,"-",this.fetchBtn ,this.resetBttn,'-',this.exportBtn,'-') ;
    
    this.record=new Wtf.data.Record.create([
        {name:'pid'},
        {name:'productDesc'},
        {name:'date'},
        {name:'customername'},
        {name:'transactionnumber'},
        {name:'customertin'},
        {name:'customercst'},
        {name:'quantity'},
        {name:'rate'},
        {name:'value'},
        {name:'totalquantity'},
        {name:'totalrate'},
        {name:'totalvalue'},
        {name:'grosstotal'},
        {name:'totalassessablevalue'},
        {name:'additionalcost'},
        {name:'vatname'},
        {name:'productname'},
        {name:'productquantities'},
        {name:'productrates'},
        {name:'productvalues'},
        {name:'assessablevalues'},
        {name:'taxamounts'},
        {name:'taxamount'}
    ]);  
    
    this.store=new Wtf.data.Store({
        url:this.isSales?"ACCCombineReports/getVatSalesRegister.do":"ACCCombineReports/getVatPurchaseRegister.do",
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:"totalcount",
            root: "data"
        },this.record),     
        sortInfo: {field: 'pid',direction: "ASC"}
    });
    this.store.load({
        params: {
            start: 0,
            limit: 30,
            startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            stdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate: WtfGlobal.convertToGenericDate(this.endDate.getValue())
        }
    });
    
    this.store.on('load',function(store){
        this.grid.getView().refresh();
    },this);
    
    this.store.on('beforeload',function(s,o){
        if(this.startDate && this.endDate){
            var sDate=this.startDate.getValue();
            var eDate=this.endDate.getValue();
            if(sDate > eDate){
                this.startDate.reset();
                this.endDate.reset();
                WtfComMsgBox(1,2);
                return;
            }
        }
        if(this.pP!=undefined){
            if(this.pP.combo.value!="All"){
                o.params.stdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
                o.params.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
            }else{
                var count = this.store.getTotalCount();
                var rem = count % 5;
                if(rem == 0){
                    count = count;
                }else{
                    count = count + (5 - rem);
                }
                o.params.limit = count;
                o.params.stdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
                o.params.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
            }
        }else{
            o.params.stdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
            o.params.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
        }
        this.store.baseParams = o.params;
    },this);
    this.exportBtn.params = this.store.baseParams;
    
    this.rowNo=new Wtf.grid.RowNumberer();
    this.grid = new Wtf.grid.GridPanel({
        store:this.store,
        sm:this.sm,
        border:false,
        layout:'fit',
        viewConfig:{
            forceFit:true, 
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        columns:[this.rowNo,{
            header: WtfGlobal.getLocaleText("acc.het.330"),
            dataIndex: 'date',
            width: 100,
            pdfwidth:75,//To Add in Export Window.
            renderer:function(v,m,rec){
                return boldrenderer(v,m,rec,false); 
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.exciseComputationReport.Particulars"),
            dataIndex: 'customername',
            width: 100,
            pdfwidth:75,//To Add in Export Window.
            renderer:function(v,m,rec){
                return threeLineRenderer(v,m,rec,'productname','',false); 
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.vsrreport.column.voucher"),
            dataIndex: 'transactionnumber',
            pdfwidth:75,//To Add in Export Window.
            width: 100
        }, 
        {
            header: WtfGlobal.getLocaleText("acc.vsrreport.column.tin.no"),
            dataIndex: 'customertin',
            pdfwidth:75,//To Add in Export Window.
            width: 100
        },
        {
            header: WtfGlobal.getLocaleText("acc.vsrreport.column.cst.no"),
            dataIndex: 'customercst',
            pdfwidth:75,//To Add in Export Window.
            width: 100
        },
        {
            header: WtfGlobal.getLocaleText("acc.taskProgressGrid.materialConsumed.header5"),
            dataIndex: 'totalquantity',
            width: 100,
            align:'right',
            pdfwidth:75,//To Add in Export Window.
            renderer:function(v,m,rec){
                return threeLineRenderer(v,m,rec,'productquantities','',false);
            }
        },
        {
            header: WtfGlobal.getLocaleText("acc.fixed.asset.rate"),
            dataIndex: 'totalrate',
            width: 100,
            align:'right',
            pdfwidth:75,//To Add in Export Window.
            renderer:function(v,m,rec){
                return threeLineRenderer("&nbsp;",m,rec,'productrates','',true);
            }
        },
        {
            header: WtfGlobal.getLocaleText("acc.vsrreport.column.value"),
            dataIndex: 'totalvalue',
            width: 100,
            align:'right',
            pdfwidth:75,//To Add in Export Window.
            renderer:function(v,m,rec){
                return threeLineRenderer(v,m,rec,'productvalues','',true);
            }
        },
        {
            header: WtfGlobal.getLocaleText("acc.termselgrid.assessablevalue"),
            dataIndex: 'totalassessablevalue',
            width: 100,
            align:'right',
            pdfwidth:75,//To Add in Export Window.
            renderer:function(v,m,rec){
                return threeLineRenderer(v,m,rec,'assessablevalues','',true);
            }
        },
        {
            header: WtfGlobal.getLocaleText("acc.vsrreport.column.vat.tax"),
            dataIndex: 'vatname',
            width: 100,
            pdfwidth:75,//To Add in Export Window.
            renderer:function(v,m,rec){
                return threeLineRenderer("&nbsp;",m,rec,'vatname','',false);
            }
        },
        {
            header: WtfGlobal.getLocaleText("acc.vsrreport.column.tax.amount"),
            dataIndex: 'taxamount',
            width: 100,
            align:'right',
            pdfwidth:75,//To Add in Export Window.
            renderer:function(v,m,rec){
                return threeLineRenderer(v,m,rec,'taxamounts','',true);
            }
        },
        {
            header: WtfGlobal.getLocaleText("acc.vsrreport.column.addl.cost"),
            dataIndex: 'additionalcost',
            width: 100,
            align:'right',
            pdfwidth:75,//To Add in Export Window.
            renderer:function(v,m,rec){
                return boldrenderer(v,m,rec,true); 
            }
        },
        {
            header: WtfGlobal.getLocaleText("acc.vsrreport.column.gross.total"),
            dataIndex: 'grosstotal',
            width: 100,
            align:'right',
            pdfwidth:75,//To Add in Export Window.
            renderer:function(v,m,rec){
                return boldrenderer(v,m,rec,true); 
            }
        }
        ]
    });
    
    Wtf.apply(this,{
        items:[{
            region:'center',
            layout:'fit',
            border:false,
            items:this.grid
        }],
        tbar:buttonArray,
        bbar:this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.store,           
            displayInfo: true,
            searchField: this.quickPanelSearch,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"),
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
            })
        })
    });
    this.grid.on("render", function(grid) {
        WtfGlobal.autoApplyHeaderQtip(grid);
    },this);
    
    Wtf.account.vatReports.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.vatReports,Wtf.Panel,{
    onRender:function(config){
        Wtf.account.vatReports.superclass.onRender.call(this,config);   
    },        
    fetchData:function(){
        var sDate=this.startDate.getValue();
        var eDate=this.endDate.getValue();
        if(sDate > eDate){
            this.startDate.reset();
            this.endDate.reset();
            WtfComMsgBox(1,2);
            return;
        }
        
        this.store.load({
            params: {
                start:0,
                limit:this.pP.combo.value,
                startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                stdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())
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
    handleResetClickNew:function(){
        this.quickPanelSearch.reset();
        this.startDate.reset();
        this.endDate.reset();
        this.fetchData();   
    },
    productNamerenderer: function(val,m,rec){
        var returnString = "";
        returnString="<div><b>"+val+"</b></div>";
        var products = rec.data.productname;
        var arr = products.split(",");
        for(var i = 0; i < arr.length; i++){
            returnString +="<div style='padding-left:10px;'>"+arr[i]+"</div>";
        }
        return returnString;
    }
});

function boldrenderer(val,m,rec,applycurrency){
    if(val){
        return "<div><b>"+ (applycurrency?WtfGlobal.getCurrencySymbol()+" ":"") +val+"</b></div>";
    }
    return "";
}
function threeLineRenderer(val,m,rec,secondkey,thirdkey,applycurrency){
    var returnString = "";
    if (val && val != "&nbsp;") {
        returnString = "<div><b>" + (applycurrency ? WtfGlobal.getCurrencySymbol() + " " : "") + val + "</b></div>";
    } else {
        returnString = "<div><b>&nbsp;</b></div>";
    }
    if (secondkey) {
        var second = rec.get(secondkey);
        if (second) {
            var secondarr = second.split("!##");
            for (var i = 0; i < secondarr.length; i++) {
                if (secondarr[i]) {
                    returnString += "<div style='padding-left:10px;'>" + (applycurrency ? WtfGlobal.getCurrencySymbol() + " " : "") + secondarr[i] + "</div>";
                } else {
                    returnString += "<div>&nbsp;</div>";
                }
                if (thirdkey) {
                    var third = rec.get(thirdkey);
                    if (third) {
                        var thirdarr = third.split("!##");
                        for (var j = 0; j < thirdarr.length; j++) {
                            if (secondarr[i]) {
                                returnString += "<div style='padding-left:20px;'>" + (applycurrency ? WtfGlobal.getCurrencySymbol() + " " : "") + thirdarr[j] + "</div>";
                            } else {
                                returnString += "<div>&nbsp;</div>";
                            }
                        }
                    }
                }
            }
        }
    }
    return returnString;
}


