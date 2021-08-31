/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

function callBankBookSummaryPanel(){
    var panel=Wtf.getCmp("bankbooksummarypanel");
    if(panel==null){
        panel=new Wtf.account.BankBookSumaryPanel({
            title:WtfGlobal.getLocaleText("acc.bankBook.bankbooksummary"),
            tabTip:WtfGlobal.getLocaleText("acc.bankBook.bankbooksummary"),
            id:'bankbooksummarypanel',
            border:false,
            layout:'fit',
            closable:true,
            iconCls:'accountingbase invoicelist'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.BankBookSumaryPanel=function(config){
    Wtf.apply(this, config);
    
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
            id:"exportBankBookSummary",
            filename: WtfGlobal.getLocaleText("acc.bankBook.bankbooksummary") + "_v1",
            menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
            get:Wtf.autoNum.BankBookSummary,
            label:WtfGlobal.getLocaleText("acc.ccReport.tab3")
    });
    
    this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            id:"printBankBookSummary",
            tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
            menuItem:{print:true},
            get:Wtf.autoNum.BankBookSummary,
            label:WtfGlobal.getLocaleText("acc.ccReport.tab3")
        });
        
    buttonArray.push(this.quickPanelSearch,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),
    this.endDate,"-",this.fetchBtn,this.resetBttn,this.expButton,this.printButton);
    
    this.record=new Wtf.data.Record.create([
        {name:"accname"},
        {name:"currencyname"},
        {name:"currencyid"},
        {name:"currencysymbol"},
        {name:"amount"},
        {name:"receipt"},
        {name:"payment"}
    ]);  
    
    this.store=new Wtf.data.Store({
        reader:new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"  
        },this.record),
        url: "ACCReports/getBankBookSummary.do",
        baseParams:{
            ignoreCashAccounts:true,
            ignoreGLAccounts:true,
            ignoreGSTAccounts:true,
            ignorecustomers:true,
            ignorevendors:true,
            mode:2,
            nondeleted:true,
            accountid:"All",
            bankBookSumarryReport:true
        }
    });
    
    this.store.on('load',function(store){
        this.quickPanelSearch.StorageChanged(store);
    },this);
    
    this.store.on('beforeload',function(s,o){
        if(this.pP!=undefined){
            if(this.pP.combo.value!="All"){
                o.params.stdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
                o.params.startdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
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
                o.params.startdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
                o.params.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
            }
        }else{
            o.params.stdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
            o.params.startdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
            o.params.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
        }
    },this);
    
    WtfGlobal.setAjaxTimeOut();
    this.store.load({
        params: {
            ss: this.quickPanelSearch.getValue(), 
            start:0,
            limit:30
        }
    });
    
    this.store.on('datachanged',function(){
      var p = this.pP.combo.value;
      this.quickPanelSearch.setPage(p);
    },this);
    
    this.rowNo=new Wtf.KWLRowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.grid = new Wtf.grid.GridPanel({
         store:this.store,
         sm:this.sm,
         border:false,
         layout:'fit',
         loadMask : true,
         viewConfig:{forceFit:true, emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))},
         columns:[this.sm,this.rowNo,{
            header :WtfGlobal.getLocaleText("acc.coa.gridAccountName"),
            renderer:WtfGlobal.deletedRenderer,        
            dataIndex: 'accname' ,
            align:'left',
            pdfwidth:200
         },{
            header :WtfGlobal.getLocaleText("acc.coa.gridCurrency"), //'Currency',
                renderer:WtfGlobal.deletedRenderer,
            dataIndex: 'currencyname',
            pdfwidth:200,
            align:'left'
         },{
            header: WtfGlobal.getLocaleText("acc.receipt.1"),  //" Recipt",
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            dataIndex: 'receipt',
            width:100,
            pdfwidth:200,
            align:'right'
         },{
            header: WtfGlobal.getLocaleText("acc.receipt.2"),  //" Payment",
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            dataIndex: 'payment',
            width:100,
            pdfwidth:200,
            align:'right'
         },{
            header: WtfGlobal.getLocaleText("acc.endingbalance"),  //" Ending Balance",
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            dataIndex: 'amount',
            width:100,
            pdfwidth:200,
            align:'right'
         }]
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
    
    Wtf.account.BankBookSumaryPanel.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.BankBookSumaryPanel,Wtf.Panel,{
    onRender:function(config){
      Wtf.account.BankBookSumaryPanel.superclass.onRender.call(this,config);   
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
                startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                ss: this.quickPanelSearch.getValue()
            }
        });
    },
    handleResetClickNew:function(){
      this.quickPanelSearch.reset();
      this.startDate.reset();
      this.endDate.reset();
      this.fetchData();   
    }
});