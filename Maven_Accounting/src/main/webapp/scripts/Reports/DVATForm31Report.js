
/*DVAT Form 31 : Sales & Outward Branch Transfer Register*/

function getFormDVAT31ReportDynamicLoad() {
    var reportPanel = Wtf.getCmp('formDVAT30Report_id');
    if(reportPanel == null){
        reportPanel = new Wtf.account.DVATForm31Tab({
            id :'formDVAT31Report_id',
            border : false,
            title: WtfGlobal.getLocaleText("DVAT Form 31"),//'DVAT Form 30',
            tabTip: WtfGlobal.getLocaleText("DVAT Form 31"),//'DVAT Form 30',
            isCreditAvailedReport:true,
            layout: 'fit',
            closable : true,
            iconCls:'accountingbase agedrecievable',
            isVHT:false
        });
        Wtf.getCmp('as').add(reportPanel);
    }
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.DVATForm31Tab=function(config){
    Wtf.apply(this, config);
    
    var buttonArray = new Array();
    
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.coa.accountSearchText"), 
        width: 150,
        hidden:true,
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
    Wtf.natureOfStockItemStore.load();
    this.natureOfStockItem= new Wtf.form.FnComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.product.stockitemtype")+"*", 
        name:'natureOfStockItem',
        anchor:'85%',
        store:Wtf.natureOfStockItemStore,
        valueField:'id',
        displayField:'name',
        emptyText:WtfGlobal.getLocaleText("acc.report.annexure10.selectstockitemtype")
    });
    
    this.fetchBtn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.agedPay.fetch"),  //'Fetch',           
        scope: this,
        id:'fetchBtn'+this.id,
        tooltip: WtfGlobal.getLocaleText("acc.invReport.fetchTT"), 
        handler: this.fetchData,
        iconCls:'accountingbase fetch'
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
    this.expButton=new Wtf.exportButton({
        obj:this,
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details.',
        id:"dvatform31_export",
        filename: WtfGlobal.getLocaleText("DVATForm31")+ "_v1",
        menuItem:{
            csv:false,
            pdf:false,
            rowPdf:false,
            xls:true
        },
        params:{
            startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate: WtfGlobal.convertToGenericDate(this.endDate.getValue())            
        },
        get:Wtf.autoNum.DVATForm31,
        label:WtfGlobal.getLocaleText("acc.ccReport.tab3"),
        hidden:false
    });
    
    this.expBtn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.export"),         
        scope: this,
        id:'expBtn'+this.id,
        tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), 
        handler: this.exportData,
        iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export')
    });
    
    
    this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        id:"tdschallancontrol_print",
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
        menuItem:{
            print:true
        },
        get:Wtf.autoNum.DVATForm31,
        label:WtfGlobal.getLocaleText("acc.ccReport.tab3"),
        hidden:true
    });
    buttonArray.push(this.quickPanelSearch,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),
        this.endDate,"-",this.fetchBtn,this.resetBttn,this.expBtn,this.printButton);
    this.record=new Wtf.data.Record.create([
        {name: 'date'}, 
        {name: 'invoiceno'}, 
        {name: 'buyertin'}, 
        {name: 'nameofbuyer'}, 
        {name: 'col5'}, 
        {name: 'col6'}, 
        {name: 'col7'}, 
        {name: 'col8'}, 
        {name: 'col9'}, 
        {name: 'col10'}, 
        {name: 'col11'}, 
        {name: 'col12'}, 
        {name: 'col13'}, 
        {name: 'col14'}, 
        {name: 'col15'}, 
        {name: 'col16'}, 
        {name: 'col17'}, 
        {name: 'col18'}, 
        {name: 'col19'}, 
        {name: 'col20'}, 
        {name: 'col21'}, 
        {name: 'col22'}, 
        {name: 'col23'}, 
        {name: 'col24'}, 
        {name: 'col25'}, 
        {name: 'col26'}, 
        {name: 'col27'}, 
        {name: 'col28'}, 
        {name: 'col29'}, 
        {name: 'col30'} 
    ]);  
    this.store=new Wtf.data.Store({
        reader:new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"  
        },this.record),
        url: "ACCCombineReports/getDVATForm31Report.do",
        baseParams:{
            isExciseInvoice:true
        }
    });
    
    this.store.on('load',function(store){
        this.quickPanelSearch.StorageChanged(store);
    },this);
    
    this.store.on('beforeload',function(s,o){
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
    },this);
    
    
    this.colSpan = new Wtf.GroupHeaderGrid({
        rows: [[{
            align: "center",
            header: "",
            colspan: 0
        }, {
            align: "center",
            header: "",
            colspan: 0
        }, {
            align: "center",
            header: "",
            colspan: 0
        },{
            align: "center",
            header: "",
            colspan: 0
        },{
            align: "center",
            header: "",
            colspan: 0
        },{
            align: "center",
            header: "",
            colspan: 0
        },{
            align: "center",
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.h1"), //"Turnover of Inter-State Sale/Stock Transfer / Export (Deductions)",
            colspan: 10
        }, {
            align: "center",
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.h2"), //"Turnover of Inter-State Sale (Taxable)",
            colspan: 5
        },{
            align: "center",
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.h3"), //"Turnover of Local Sale",
            colspan: 7
        }
        ]],
        hierarchicalColMenu: false
    });
    
    
    WtfGlobal.setAjaxTimeOut();
    this.store.load({
        params: {
            start:0,
            limit:30
        }
    });
    
    this.store.on('datachanged',function(){
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    },this);
    this.summary = new Wtf.grid.GroupSummary();
    this.GridSummary = new Wtf.ux.grid.GridSummary({});
    this.rowNo=new Wtf.grid.RowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.grid = new Wtf.grid.GridPanel({
        store:this.store,
        sm:this.sm,
        border:false,
        layout:'fit',
        loadMask : true,
        plugins: [this.colSpan],
        viewConfig:{
            forceFit:false, 
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        columns:[ this.rowNo,
        {
            header : WtfGlobal.getLocaleText("acc.report.DVATForm31.column.date"), //'Date of Sale/Transfer',
            dataIndex: 'date' ,
            pdfwidth:200
        },{
            header :WtfGlobal.getLocaleText("acc.report.DVATForm31.column.invoiceno"), // Invoice No. Debit/Credit Note No.
            dataIndex: 'invoiceno',
            pdfwidth:200
        },{
            header : WtfGlobal.getLocaleText("acc.report.DVATForm31.column.byertin"), // Buyer’s TIN / Embassy/Organisati on Regn. No.
            dataIndex: 'buyertin' ,
            pdfwidth:200
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.buyername"),//Buyer/Embassy/Organisation Name
            dataIndex: 'nameofbuyer',
            width:100,
            pdfwidth:200,
            align:'center'
        },{
            header :WtfGlobal.getLocaleText("acc.report.DVATForm31.column.TaxRateDVAT"),//"Tax Rate (DVAT)",
            dataIndex: 'col5' ,
            align:'right',
            pdfwidth:200
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.export"),//"Export",
            dataIndex: 'col6',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.HighSeaSale"),//"High Sea Sale",
            dataIndex: 'col7',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.owngoodsaginstform"),//"Own goods transferrd  for Job Work against F-Form",
            dataIndex: 'col8',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.othergoodsaginstform"),//"Other dealers goods returned after Job work against F-Form",
            dataIndex: 'col9',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.stocktransferbrachform"),//"Stock transfer (Branch) against F- Form",
            dataIndex: 'col10',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.stocktransferconsignmentform"),//"Stock transfer (Consignment) against F- Form",
            dataIndex: 'col11',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.SaleagainstHForm"),//"Sale against H- Form",
            dataIndex: 'col12',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.SaleagainstIForm"),//"Sale against I- Form",
            dataIndex: 'col13',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.SaleagainstJForm"),//"Sale against J- Form",
            dataIndex: 'col14',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
                
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.SaleagainstII"),//"Sale against C+E- I/E-II",
            dataIndex: 'col15',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
                
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.SaleofExemptedGoodsSchI"),//"Sale of Exempted Goods [Sch. I]",
            dataIndex: 'col16',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
                
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.sec9and8"),//"Sales covered under proviso to [Sec.9( 1)] Read with Sec.8(4)]",
            dataIndex: 'col17',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
                
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.sec4"),//"Sales of Goods Outside Delhi (Sec. 4)",
            dataIndex: 'col18',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
                
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.RateofTaxCST"),//"Rate of Tax (CST)",
            dataIndex: 'col19',
            width:100,
            align:'right',
            pdfwidth:200
                
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.salecformasset"),//"Sale against C-Form excluding sale of capital assets",
            dataIndex: 'col20',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
                
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.CapitalGoodssoldagainstCForms"),//"Capital Goods sold against C- Forms",
            dataIndex: 'col21',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
                
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.Salewithoutforms"),//"Sale without forms",
            dataIndex: 'col22',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
                
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.TaxCST"),//"Tax (CST)",
            dataIndex: 'col23',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
                
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.TurnoverGoodsexcludingVAT"),//"Turnover (Goods) (excluding VAT)",
            dataIndex: 'col24',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
                
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.TurnoverWCsexcludingVAT"),//"Turnover (WC) (excluding VAT",
            dataIndex: 'col25',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
                
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.OutputTax"),//"Output Tax",
            dataIndex: 'col26',
            width:100,
            align:'right',
            pdfwidth:200
                
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.civilcontract1"),//"Charges towards labour, services and other like charges, in civil works contracts",
            dataIndex: 'col27',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
                
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.civilcontract2"),//"Charges towards cost of land, if any, in civil works contracts",
            dataIndex: 'col28',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
                
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.ddealer"),//"Sale against H-Form to Delhi dealers",
            dataIndex: 'col29',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
                
        },{
            header: WtfGlobal.getLocaleText("acc.report.DVATForm31.column.OMClevel"),//"Sale of Petrol/Diesel suffered tax on full sale price at OMC level",
            dataIndex: 'col30',
            width:100,
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:200
                
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
    
    Wtf.account.DVATForm31Tab.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.DVATForm31Tab,Wtf.Panel,{
    onRender:function(config){
        Wtf.account.DVATForm31Tab.superclass.onRender.call(this,config);   
    },       
    exportData:function(){
        var startdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
        var enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
        var header='date~@invoiceno~@buyertin~@nameofbuyer~@col5~@col6~@col7~@col8~@col9~@col10~@col11~@col12~@col13~@col14~@col15~@col16~@col17~@col18~@col19~@col20~@col21~@col22~@col23~@col24~@col25~@col26~@col27~@col28~@col29~@col30';
        var title='Date of Sale/Transfer~@Invoice No. Debit/Credit Note No.~@Buyer’s TIN / Embassy/Organisati on Regn. No.~@Buyer/Embassy/Organisation Name~@Tax Rate (DVAT) (for all columns)';
        title+='~@Turnover of Inter-State Sale/Stock Transfer / Export (Deductions)~@Turnover of Inter-State Sale (Taxable)~@Turnover of Local Sale';
        var subtitle='Export~@High Sea Sale~@Own goods transfer for Job Work against F-Form~@Other dealers goods returned after Job work against F-Form~@Stock transfer (Branch) against F- Form';
        subtitle+='~@Stock transfer (Consignment) against F- Form~@Sale against H- Form~@Sale against I- Form~@Sale against J- Form~@Sale against C+E- I/E-II';
        subtitle+='~@Sale of Exempted Goods [Sch. I]~@Sales covered under proviso to [Sec.9( 1)] Read with Sec.8(4)]~@Sales of Goods Outside Delhi (Sec. 4)~@Rate of Tax (CST)~@Sale against C-Form excluding sale of capital assets';
        subtitle+='~@Capital Goods sold against C- Forms~@Sale without forms~@Tax (CST)~@Turnover (Goods) (excluding VAT)~@Turnover (WC) (excluding VAT';
        subtitle+='~@Output Tax~@Charges towards labour, services and other like charges, in civil works contracts~@Charges towards cost of land, if any, in civil works contracts~@Sale against H-Form to Delhi dealers~@Sale of Petrol/Diesel suffered tax on full sale price at OMC level';
        var width='500,500,500,500,500,500,500,500,500,500,500,500,500,500,500,500,500,500,500,500,500,500,500,500,500,500,500,500,500,500';
        var align='none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none';
        var url="ACCCombineReports/exportDVATForm31Report.do";
        url+="?isExport="+true+"&filetype=xls&filename=DVAT Form 31"+'&header='+header+'&title='+title+'&subtitle='+subtitle+'&align='+align+'&width='+width+'&DVATForm31='+true+'&stdate='+startdate+'&enddate='+enddate;
        Wtf.get('downloadframe').dom.src=url;
    },
    fetchData:function(){
        var sDate=this.startDate.getValue();
        var eDate=this.endDate.getValue();
        if(sDate > eDate){
            WtfComMsgBox(1,2);
            return;
        }
        
        this.store.load({
            params: {
                start:0,
                limit:this.pP.combo.value,
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
    }
});
