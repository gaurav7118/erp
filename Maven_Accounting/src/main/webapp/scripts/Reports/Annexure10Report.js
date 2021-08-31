function getAnnexure10ReportLoad() {
    var reportPanel = Wtf.getCmp('annexure10report');
    if(reportPanel == null){
        reportPanel = new Wtf.account.annexure10Tab({
            id :'annexure10report',
            border : false,
            title: WtfGlobal.getLocaleText("acc.report.annexure10.title"),//'Annexure 10 Report',
            tabTip: WtfGlobal.getLocaleText("acc.report.annexure10.title"),//'Annexure 10 Report',// 
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

Wtf.account.annexure10Tab=function(config){
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
        id:"annexure10Report_export",
        filename: WtfGlobal.getLocaleText("acc.report.annexure10.title")+ "_v1",
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,
            xls:true
        },
        params:{
            startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate: WtfGlobal.convertToGenericDate(this.endDate.getValue())            
        },
        get:Wtf.autoNum.annexure10Report,
        label:WtfGlobal.getLocaleText("acc.ccReport.tab3"),
        hidden:false
    });
    
    this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        id:"annexure10Report_print",
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
        menuItem:{
            print:true
        },
        get:Wtf.autoNum.BankBookSummary,
        label:WtfGlobal.getLocaleText("acc.ccReport.tab3"),
        hidden:true
    });
        
    var unitStore=new Wtf.data.Store({
        url: "ACCInvoice/getCompanyUnit.do",
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },Wtf.UnitStoreRec)
    });
    unitStore.load();
        
    this.exciseUnit= new Wtf.form.ComboBox({
        store: unitStore,
        typeAhead: true,
        selectOnFocus:true,
        valueField:'id',
        displayField:'name',
        extraComparisionField:'id', 
        extraFields:[],
        hiddenName:"templateunit",
        id:"templateunit"+this.id,
        mode: 'local',
        forceSelection: true,
        width : 150,
        listWidth : 150,
        editable : true,
        triggerAction:'all',
        hirarchical:true,
        scope:this,
        emptyText:WtfGlobal.getLocaleText("acc.field.SelectExciseUnit")
    });
        
    buttonArray.push(this.quickPanelSearch,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),
        this.endDate,"-",WtfGlobal.getLocaleText("acc.lp.companyunitlayout"),this.exciseUnit,"-",WtfGlobal.getLocaleText("acc.report.annexure10.stockitemtype"),this.natureOfStockItem,"-",this.fetchBtn,this.resetBttn,this.expButton,this.printButton);
    
    this.record=new Wtf.data.Record.create([
    {
        name: 'documenttype'
    }, 

    {
        name: 'documentnumber'
    }, 

    {
        name: 'documentnumberdate'
    }, 

    {
        name: 'hsncode'
    }, 

    {
        name: 'eccno'
    }, 

    {
        name: 'credit'
    }, 

    {
        name: 'debit'
    }, 

    {
        name: 'currencysymbol'
    }, 

    {
        name: 'currencyid'
    }, 

    {
        name: 'assessablevalue'
    }, 

    {
        name: 'dateofentry'
    }, 

    {
        name: 'exciseduty'
    }, 

    {
        name: 'suppliername'
    }, 

    {
        name: 'debit'
    }, 

    {
        name: 'jenumber'
    }, 

    {
        name: 'moduleid'
    }, 

    {
        name: 'balance'
    },

    {
        name: 'manufacturertype'
    },
    {
        name: 'quantity'
    },
    {
        name: 'additionalexciseduty'
    }
    ]);  
    
    this.store=new Wtf.data.Store({
        reader:new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"  
        },this.record),
        url: "ACCCombineReports/getAnnexure10Report.do",
        baseParams:{
            isExciseInvoice:true,
            includeAllRec:true
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
                o.params.stockitemtype= this.natureOfStockItem.getValue();
                o.params.exciseunit= this.exciseUnit.getValue();
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
                o.params.stockitemtype= this.natureOfStockItem.getValue();
                o.params.exciseunit= this.exciseUnit.getValue();
            }
        }else{
            o.params.stdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
            o.params.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
            o.params.stockitemtype= this.natureOfStockItem.getValue();
            o.params.exciseunit= this.exciseUnit.getValue();
        }
    },this);
    
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
        plugins:this.GridSummary,
        viewConfig:{
            forceFit:true, 
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            },
        columns:[this.rowNo,
        {
            header :WtfGlobal.getLocaleText("acc.plareport.column.doctype.credit"),
            dataIndex: 'documenttype' ,
            pdfwidth:200
        },{
            header :WtfGlobal.getLocaleText("acc.report.annexure10.nodatedoc"),//'No. and Date of Document',//
            dataIndex: 'documentnumberdate',
            pdfwidth:200
        },{
            header : WtfGlobal.getLocaleText("acc.report.annexure10.NameoftheSupplier"),//'Name of the Supplier',
            dataIndex: 'suppliername' ,
            pdfwidth:200
        },{
            header: WtfGlobal.getLocaleText("acc.report.annexure10.ecc"),//'C.E.No. of the Supplier',
            dataIndex: 'eccno',
            width:100,
            pdfwidth:200,
            align:'center',
            renderer: function(val){
                return "<div wtf:qtip=\"" + val + "\">" + val + "</div>";
            }
        },{
            header : WtfGlobal.getLocaleText("acc.report.annexure10.dateofinput"),//'Date on which principal inputs/Capital Goods received',//,
            dataIndex: 'dateofentry' ,
            pdfwidth:200
        },{
            header : WtfGlobal.getLocaleText("acc.ra.value"),//'Value',
            dataIndex: 'assessablevalue' ,
            pdfwidth:200,
            align:'right',
            renderer: this.QuantityRender.createDelegate(this)
        },{
            header: WtfGlobal.getLocaleText("acc.report.annexure10.CENVAT"), //'CENVAT',//
            dataIndex: 'exciseduty',
            width:100,
            hidden:!this.isCreditAvailedReport,
            pdfwidth:200,
            align:'right',
            summaryType: 'sum',
            renderer: this.QuantityRender.createDelegate(this),
            summaryRenderer: function (value, m, rec) {
                return "<b>Total = " +WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"</b>";
            }
        },{
            header : WtfGlobal.getLocaleText("acc.report.annexure10.AdditionalExciseDuty"),
            dataIndex: 'additionalexciseduty' ,
            align:'center',
            pdfwidth:200,
            renderer: this.QuantityRender.createDelegate(this)
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
    
    Wtf.account.annexure10Tab.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.annexure10Tab,Wtf.Panel,{
    onRender:function(config){
        Wtf.account.annexure10Tab.superclass.onRender.call(this,config);   
    },        
    fetchData:function(){
        var sDate=this.startDate.getValue();
        var eDate=this.endDate.getValue();
        var stockItemType = this.natureOfStockItem.getValue();
        var exciseUnit = this.exciseUnit.getValue();
        if(sDate > eDate){
            WtfComMsgBox(1,2);
            return;
        }
        
        this.store.load({
            params: {
                start:0,
                limit:this.pP.combo.value,
                stdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                stockitemtype:stockItemType,
                exciseunit:exciseUnit
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
        this.natureOfStockItem.reset();
        this.exciseUnit.reset();
        this.fetchData();   
    }
});