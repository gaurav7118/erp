
function getTradingAndProfitLossWithBudgetDynamicLoad(){
    var panel = Wtf.getCmp("getTradingAndProfitLossWithBudget");
    if(panel==null){
        panel = new Wtf.account.TradingAndProfitLossWithBudget({
            id : 'getTradingAndProfitLossWithBudget',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.TradingAndProfitLossWithBudget"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.field.ViewTradingAndProfitLossWithBudget"),
            topTitle:'<center><font size=4>' + WtfGlobal.getLocaleText("acc.field.TradingAndProfitLossWithBudget") + '</font></center>',
            border : false,
            moduleid:Wtf.financialStatementsModuleIds.tradingProfitAndLoss, //Added module id for Tading Profit and loss search report
            statementType:'MonthlyTradingAndProfitLoss',
            closable: true,
            layout: 'fit',
            iconCls:'accountingbase financialreport'
        });
        Wtf.getCmp('as').add(panel);
        panel.on('account',callGeneralLedger);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    panel.on("activate",function(panel){
        panel.westPanel.setWidth(panel.getInnerWidth()/2);
        panel.doLayout();
    });
}



Wtf.account.TradingAndProfitLossWithBudget=function(config){
    this.total=[0,0];
    this.leftHeading=(config.statementType=="BalanceSheet"?"Liability":"Debit");
    this.rightHeading=(config.statementType=="BalanceSheet"?"Asset":"Credit");
    this.moduleid=config.moduleid; 
    this.uPermType=Wtf.UPerm.fstatement;
    this.permType=Wtf.Perm.fstatement;
    this.exportPermType=(config.statementType=="BalanceSheet"?this.permType.exportdatabsheet:this.permType.exportdatatradingpnl);
    this.printPermType=(config.statementType=="BalanceSheet"?this.permType.printbsheet:this.permType.exportdatatradingpnl)
    this.toggle=0;

    this.summaryL = new Wtf.ux.grid.GridSummary();
    this.summaryR = new Wtf.ux.grid.GridSummary();
    
    this.FinalStatementRec = new Wtf.data.Record.create([
    {
        name: 'accountname'
    },

    {
        name: 'accountid'
    },

    {
        name: 'actualamount'
    },

    {
        name: 'ytdamount'
    },

    {
        name: 'actualbudget'
    },

    {
        name: 'ytdbudget'
    },

    {
        name: 'annualbudget'
    },

    {
        name: 'variance'
    },

    {
        name: 'varianceinpercent'
    },

    {
        name: 'accountflag'
    },

    {
        name: 'isdebit',
        type:'boolean'
    },

    {
        name: 'level'
    },

    {
        name: 'fmt'
    },

    {
        name: 'leaf'
    }
    ]);

    this.statementType=config.statementType||"Trading";

    this.lStroe = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "left"                            // Assets on Left side in Balance Sheet          Neeraj
        },this.FinalStatementRec),
        baseParams:{
            nondeleted:true
        },
        url: Wtf.req.account+'CompanyManager.jsp'
    });

    this.rStroe = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "right"                           //  Liabilities on right side in Balance Sheet          Neeraj
        },this.FinalStatementRec),
        baseParams:{
            nondeleted:true
        },
        url: Wtf.req.account+'CompanyManager.jsp'
    });

    // to change this with the month & year drop-down list
    this.monthStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'monthid',
            type:'int'
        }, 'name'],
        data :[[0,"January"],[1,"February"],[2,"March"],[3,"April"],[4,"May"],[5,"June"],[6,'July'],[7,'August'],[8,"September"],[9,"October"],
        [10,"November"],[11,"December"]]
    });

    var data=WtfGlobal.getBookBeginningYear(true);
       
    this.yearStore= new Wtf.data.SimpleStore({
        fields: [{
            name:'id',
            type:'int'
        }, 'yearid'],
        data :data
    });

    this.startMonth = new Wtf.form.ComboBox({
        store: this.monthStore,
        fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  //'Month',
        name:'startMonth',
        displayField:'name',
        forceSelection: true,
        anchor:'95%',
        valueField:'name',
        mode: 'local',
        triggerAction: 'all',
        width:90,
        selectOnFocus:true
    });  

    this.startYear = new Wtf.form.ComboBox({
        store: this.yearStore,
        fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  //'Year',
        name:'startYear',
        displayField:'yearid',
        anchor:'95%',
        valueField:'yearid',
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        width:90,
        selectOnFocus:true
    });  

    var columnArr = [];
    columnArr.push({
        header:'<b>'+WtfGlobal.getLocaleText("acc.balanceSheet.particulars")+'</b>',
        dataIndex:'accountname',
        renderer:this.formatAccountName,
        width:250,
        summaryRenderer:function(){
            return WtfGlobal.summaryRenderer(WtfGlobal.getLocaleText("acc.common.total"));
        }.createDelegate(this)
    },{
        header:'<b>'+this.startMonth.getValue()+" "+this.startYear.getValue()+" "+WtfGlobal.getLocaleText("acc.field.Actual")+'</b>',
        hidden: false,
        dataIndex: 'actualamount',      
        renderer:this.formatData,  
        width: 180,
        pdfwidth: 180,
        align:'center',
        style: 'text-align:right'
    },{
        header:'<b>'+this.startMonth.getValue()+" "+this.startYear.getValue()+" "+WtfGlobal.getLocaleText("acc.field.actBudget")+'</b>',
        hidden: false,
        dataIndex: 'actualbudget',      
        renderer:this.formatData,  
        width: 180,
        pdfwidth: 180,
        align:'center',
        style: 'text-align:right'
    },{
        header:'<b>'+WtfGlobal.getLocaleText("acc.field.YTDActual")+'</b>',
        hidden: false,
        dataIndex: 'ytdamount',      
        renderer:this.formatData,  
        width: 180,
        pdfwidth: 180,
        align:'center',
        style: 'text-align:right'
    },{
        header:'<b>'+WtfGlobal.getLocaleText("acc.field.YTDBudget")+'</b>',
        hidden: false,
        dataIndex: 'ytdbudget',      
        renderer:this.formatData,  
        width: 180,
        pdfwidth: 180,
        align:'center',
        style: 'text-align:right'
    },{
        header:'<b>'+WtfGlobal.getLocaleText("acc.field.AnnualBudget")+'</b>',
        hidden: false,
        dataIndex: 'annualbudget',      
        renderer:this.formatData,  
        width: 180,
        pdfwidth: 180,
        align:'center',
        style: 'text-align:right'
    },{
        header:'<b>'+WtfGlobal.getLocaleText("acc.field.Variance")+'</b>',
        hidden: false,
        dataIndex: 'variance',      
        renderer:this.formatData,  
        width: 180,
        pdfwidth: 180,
        align:'center',
        style: 'text-align:right'
    },{
        header:'<b>'+WtfGlobal.getLocaleText("acc.field.VarianceInPercent")+'</b>',
        hidden: false,
        dataIndex: 'varianceinpercent',
        renderer:this.formatVariancePercent,
        width: 180,
        pdfwidth: 180,
        align:'center',
        style: 'text-align:right'
    });

    this.lGrid = new Wtf.grid.HirarchicalGridPanel({
        autoScroll:true,
        store: this.lStroe,
        hirarchyColNumber:0,
        columns: columnArr,       
        border : false,
        loadMask : true,
        viewConfig: {
            forceFit:false
        }
    });
    this.lGrid.on("render", WtfGlobal.autoApplyHeaderQtip);
    this.lGrid.on('rowclick',this.onRowClickLGrid, this);
    
    this.rGrid = new Wtf.grid.HirarchicalGridPanel({
        plugins:[this.summaryR],
        autoScroll:true,
        store: this.rStroe,
        hirarchyColNumber:0,
        columns: columnArr,      
        border : false,
        loadMask : true,
        viewConfig: {
            forceFit:false
        }
    });
    this.rGrid.on("render", WtfGlobal.autoApplyHeaderQtip);

    this.rGrid.on('rowclick',this.onRowClickRGrid, this);

    this.lGrid.on('render',function(){
        this.lGrid.getView().getRowClass=this.getRowClass.createDelegate(this,[this.lGrid],1);
    },this);

    this.rGrid.on('render',function(){
        this.rGrid.getView().getRowClass=this.getRowClass.createDelegate(this,[this.rGrid],1);
    },this);

    this.lGrid.getStore().on("load", function(){
        // set column headers based on the months value
       
       this.lGrid.getColumnModel().setColumnHeader(1,'<b>'+this.startMonth.getValue()+this.startYear.getValue()+" "+WtfGlobal.getLocaleText("acc.field.Actual")+'</b>');
       this.lGrid.getColumnModel().setColumnHeader(2,'<b>'+this.startMonth.getValue()+this.startYear.getValue()+" "+WtfGlobal.getLocaleText("acc.field.actBudget")+'</b>');

       this.Lcm = this.lGrid.getColumnModel();
        // this.Rcm = this.rGrid.getColumnModel();

        this.lstore1 = this.lGrid.getStore();
        // this.rstore1 = this.rGrid.getStore();

        this.lGrid.reconfigure(this.lstore1,this.Lcm);
        // this.rGrid.reconfigure(this.rstore1,this.Rcm);        

        this.expandCollapseGrid("Collapse");

    }, this);

    this.rGrid.getStore().on("load", function(){
        this.lstore1 = this.lGrid.getStore();
        this.rstore1 = this.rGrid.getStore();
        this.lGrid.getColumnModel().setColumnHeader(1,'<b>'+this.startMonth.getValue()+" "+this.startYear.getValue()+" "+WtfGlobal.getLocaleText("acc.field.Actual")+'</b>');
        this.lGrid.getColumnModel().setColumnHeader(2,'<b>'+this.startMonth.getValue()+" "+this.startYear.getValue()+" "+WtfGlobal.getLocaleText("acc.field.actBudget")+'</b>');
        this.lGrid.reconfigure(this.lstore1,this.Lcm);
        this.rGrid.reconfigure(this.rstore1,this.Rcm);  
        this.expandCollapseGrid("Collapse");
    }, this);
    
    this.grid = this.rGrid;

    var mnuBtns=[];
    var csvbtn=new Wtf.Action({
        iconCls:'pwnd '+'exportcsv',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToCSVTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToCSV")+"</span>",
        scope: this,
        handler:function(){
            this.exportWithTemplate()
        }
    });
    mnuBtns.push(csvbtn)
     var xlsbtn=new Wtf.Action({
        iconCls:'pwnd '+'exportcsv',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToXLSTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"</span>",
        scope: this,
        handler:function(){
            this.exportWithTemplate("xls")
        }
    });
    mnuBtns.push(xlsbtn)
    var pdfbtn=new Wtf.Action({
        iconCls:'pwnd '+'exportpdf',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToPDFTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToPDF")+"</span>",
        scope: this,
        handler:function(){
            this.exportPdfTemplate()
        }
    });
    mnuBtns.push(pdfbtn)

    chkCostCenterload();
    if(Wtf.CostCenterStore.getCount()==0) Wtf.CostCenterStore.on("load", this.setCostCenter, this);
    this.costCenter = new Wtf.form.ComboBox({
        store: Wtf.CostCenterStore,
        name:'costCenterId',
        width:140,
        displayField:'name',
        valueField:'id',
        triggerAction: 'all',
        mode: 'local',
        typeAhead:true,
        value:"",
        selectOnFocus:true,
        forceSelection: true,
        emptyText:WtfGlobal.getLocaleText("acc.rem.9")  //,"Select a Cost Center"
    });

    var btnArr=[];

    btnArr.push(
        (config.statementType=='BalanceSheet'?'':WtfGlobal.getLocaleText("acc.field.For")),
        this.startMonth, this.startYear
        );

    if(this.statementType=="MonthlyTradingAndProfitLoss" || this.statementType=="CostCenter"){
        btnArr.push(WtfGlobal.getLocaleText("acc.common.costCenter"),this.costCenter);
    }

    btnArr.push('-',{
        xtype:'button',
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
        tooltip:WtfGlobal.getLocaleText("acc.common.fetchTT"),
        iconCls:'accountingbase fetch',
        tooltip:(config.statementType=='BalanceSheet'?WtfGlobal.getLocaleText("acc.cc.26"):WtfGlobal.getLocaleText("acc.cc.27")),
        scope:this,
        handler:this.fetchStatement
    });

    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
        btnArr.push(this.expButton=new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.common.export"),  //'Export',
            iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export'),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details.',
            disabled :true,
            hidden :false,
            scope: this,
            menu:mnuBtns
        }));
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
        btnArr.push(this.printbtn=new Wtf.Button({
            iconCls:'pwnd printButtonIcon',
            text :WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details.',
            disabled :true,
            hidden :false,
            scope: this,
            handler:function(){
                this.exportWithTemplate("print")
            }
        }));
    }

    this.expandCollpseButton = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.Expand"),
        tooltip:WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls:'pwnd toggleButtonIcon',
        scope:this,
        handler: function(){
            this.expandCollapseGrid(this.expandCollpseButton.getText());
        }
    });

    btnArr.push('-', this.expandCollpseButton);
    
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.lGrid.colModel,
        moduleid: this.moduleid,
        splitOpeningBalance:true,
        advSearch: false
    });
    
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    //Advance search button
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    btnArr.push('-', this.AdvanceSearchBtn);
    
    this.wrapperPanel = new Wtf.Panel({
        border:false,
        layout:"border",
        scope:this,
        items:[this.objsearchComponent,
        this.westPanel = new Wtf.Panel({
            width:'100%',
            region:'center',
            layout:'fit',
            border:false,
            items:this.lGrid
        }),
        {
            layout:'fit',
            region:'west',
            width:'0%'
        },

        {
            layout:'fit',
            region:'east',
            width:'0%'
        }
        ]
    });
    
    Wtf.apply(this,{
        defaults:{
            border:false,
            bodyStyle:"background-color:white;"
        },
        saperate:true,
        statementType:"Trading",
        items:this.wrapperPanel,
        tbar:btnArr
    },config);

    Wtf.account.TradingAndProfitLossWithBudget.superclass.constructor.call(this,config);
    this.addEvents({
        'account':true
    });
    if(this.statementType!="CostCenter"){
        this.fetchStatement();
    }
}

Wtf.extend( Wtf.account.TradingAndProfitLossWithBudget,Wtf.Panel,{
    onRowClickRGrid:function(g,i,e){
        var el=e.getTarget("a");
        if(el==null)return;
        var accid=this.rGrid.getStore().getAt(i).data['accountid'];
    },
    filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.fetchStatement();
    },
    showAdvanceSearch: function() {
        showAdvanceSearch(this, this.searchparam, this.filterAppend);
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
       this.fetchStatement();
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },  
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();   
    },
    onRowClickLGrid:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        
        var elementId = el.id;
        
        if(elementId == 'anc'){
            return;
        }
        
        var accid=this.lGrid.getStore().getAt(i).data['accountid'];   
        
        var startMonthName = this.startMonth.getValue();
        var startYear = this.startYear.getValue();

        var startDate = new Date(Date.parse(startMonthName + " 01" + ", "+ startYear));
        
        var endMonthName = this.startMonth.getValue();
        var endYearName = this.startYear.getValue();
        
        var endMonthId = this.monthStore.getAt(this.monthStore.find('name', this.startMonth.getValue())).get('monthid');
        
        var isLeap = ((endYearName % 4) == 0 && ((endYearName % 100) != 0 || (endYearName % 400) == 0));
        var daysInMonth = [31, (isLeap ? 29 : 28), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][endMonthId];

        var endDate = new Date(Date.parse(endMonthName + " " + daysInMonth  + ", "+ endYearName));
        
        this.fireEvent('account',accid,startDate,endDate, this.consolidateFlag, "", "", this.searchJson);
    },

    openLedgerForWholeDur:function(i){
        var accid=this.lGrid.getStore().getAt(i).data['accountid'];   

        var startMonthName = this.startMonth.getValue();
        var startYear = this.startYear.getValue();

        var startDate = new Date(Date.parse(startMonthName + " 01" + ", "+ startYear));

        var endMonthName = this.startMonth.getValue();
        var endYearName = this.startYear.getValue();

        var endMonthId = this.monthStore.getAt(this.monthStore.find('name', this.startMonth.getValue())).get('monthid');

        var isLeap = ((endYearName % 4) == 0 && ((endYearName % 100) != 0 || (endYearName % 400) == 0));
        var daysInMonth = [31, (isLeap ? 29 : 28), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][endMonthId];

        var endDate = new Date(Date.parse(endMonthName + " " + daysInMonth  + ", "+ endYearName));

        this.fireEvent('account',accid,startDate,endDate);
    },
 
    expandCollapseGrid : function(btntext){
        if(btntext == WtfGlobal.getLocaleText("acc.field.Collapse")){
            for(var i=0; i< this.lGrid.getStore().data.length; i++){
                this.lGrid.collapseRow(this.lGrid.getView().getRow(i));
            }
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if(btntext == WtfGlobal.getLocaleText("acc.field.Expand")){
            for(var i=0; i< this.lGrid.getStore().data.length; i++){
                this.lGrid.expandRow(this.lGrid.getView().getRow(i));
            }
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    },
    
    exportPdfTemplate:function(){
        var get;
        var fileName;
        var jsonGrid;
        var exportUrl;
        var header;
        var extraParams="";
        exportUrl = getExportUrl(Wtf.autoNum.TradingAndProfitLossWithBudget);
        if(this.statementType=="MonthlyTradingAndProfitLoss"){
            fileName =  WtfGlobal.getLocaleText("acc.field.TradingAndProfitLossWithBudget");
        } else if (this.statementType=="CostCenter"){
            fileName = WtfGlobal.getLocaleText("acc.field.TradingAndProfitLossWithBudget");
        }

        extraParams += "&costcenter="+this.costCenter.getValue();
        extraParams += "&reportView="+this.statementType;
        get = 28;
        jsonGrid = "{data:[{'header':'laccountname','title':'Particulars','width':'150','align':''},"+
        "{'header':'lamount','title':'Amount(Debit)','width':'150','align':'currency'},"+
        "{'header':'raccountname','title':'Particulars','width':'150','align':''},"+
        "{'header':'ramount','title':'Amount(Credit)','width':'150','align':'currency'}]}";
        header =  fileName;
        
        // get date from month & year drop-down lists
        if (this.startMonth.getValue() == "" || this.startYear.getValue() == ""){
            this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
            this.startYear.setValue(this.yearStore.data.items[0].json[1]);
        }
        
        var startMonthName = this.startMonth.getValue();
        var startYear = this.startYear.getValue();

        var startDate = new Date(Date.parse(startMonthName + " 01" + ", "+ startYear));
        this.sDate = WtfGlobal.convertToGenericStartDate(startDate);

        var endDate = new Date(Date.parse(startMonthName + " " + startDate.getDaysInMonth()  + ", "+ startYear));
        this.eDate = WtfGlobal.convertToGenericEndDate(endDate);

        //ERP-37238
        var searchJsonString = "";
        if(this.searchJson!= null && this.searchJson!= undefined && this.searchJson!= ""){
            searchJsonString = "&searchJson="+this.searchJson+"&filterConjuctionCriteria="+this.filterConjuctionCrit;
        }

        var configstr = "{%22landscape%22:%22true%22,%22pageBorder%22:%22true%22,%22gridBorder%22:%22true%22,%22title%22:%22Test%20Title%22,%22subtitles%22:%22%22,%22headNote%22:%22Test%20Header%22,%22showLogo%22:%22false%22,%22headDate%22:%22false%22,%22footDate%22:%22true%22,%22footPager%22:%22false%22,%22headPager%22:%22true%22,%22footNote%22:%22Test%20Footer%22,%22textColor%22:%22000000%22,%22bgColor%22:%22FFFFFF%22}"
        var url = exportUrl+"?filename="+encodeURIComponent(fileName)+extraParams+"&config="+configstr+"&header"+header+
        "&filetype=pdf&stdate="+this.sDate+"&enddate="+this.eDate+"&withBudget="+true+"&toggle="+this.toggle+"&get="+get+"&gridconfig="+encodeURIComponent(jsonGrid)+"&month="+startMonthName+"&year="+startYear+searchJsonString;

        Wtf.get('downloadframe').dom.src = url;
    },

    setCostCenter: function(){
        this.costCenter.setValue("");//Select Default Cost Center as None
        Wtf.CostCenterStore.un("load", this.setCostCenter, this);
    },

    resetFilterAndFetchReport: function(costCenterId, startDate, endDate){
        this.costCenter.setValue(costCenterId);

        // get date from month & year drop-down lists
        if (this.startMonth.getValue() == "" || this.startYear.getValue() == ""){
            this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
            this.startYear.setValue(this.yearStore.data.items[0].json[1]);
        }

        var startMonthName = this.startMonth.getValue();
        var startYear = this.startYear.getValue();

        var startDate = new Date(Date.parse(startMonthName + " 01" + ", "+ startYear));
        this.sDate = WtfGlobal.convertToGenericStartDate(startDate);

        var endDate = new Date(Date.parse(startMonthName + " " + startDate.getDaysInMonth()  + ", "+ startYear));
        this.eDate = WtfGlobal.convertToGenericEndDate(endDate);

        // check that from date and end date must be available
        if(this.sDate=="" || this.eDate=="") {
            WtfComMsgBox(42,2);
            return;
        }      
        
        this.fetchStatement();
    },

    exportWithTemplate:function(type){
        type = type?type:"csv";
        var exportUrl;
        var fileName;
        var reportName;
        //headers for export
        var header = "accountname,actualamount,actualbudget,ytdamount,ytdbudget,annualbudget,variance,varianceinpercent";

        // not sure if we need to change to lGrid.getStore() but so far rGrid.getStore() does work!
        var store = this.rGrid.getStore();
        var title = "";
        var extraParams = "";
        //titles for export
        title = WtfGlobal.getLocaleText("acc.report.2")+", "+this.startMonth.getValue()+" "+this.startYear.getValue()+" "+WtfGlobal.getLocaleText("acc.field.Actual")+", "+this.startMonth.getValue()+" "+this.startYear.getValue()+" "+WtfGlobal.getLocaleText("acc.field.actBudget")+", "+WtfGlobal.getLocaleText("acc.field.YTDActual")+", "+WtfGlobal.getLocaleText("acc.field.YTDBudget")+", "+WtfGlobal.getLocaleText("acc.field.AnnualBudget")+", "+WtfGlobal.getLocaleText("acc.field.Variance")+", "+WtfGlobal.getLocaleText("acc.field.VarianceInPercent");       
        exportUrl = getExportUrl(Wtf.autoNum.TradingAndProfitLossWithBudget);
        if(this.statementType=="MonthlyTradingAndProfitLoss"){
            fileName =  WtfGlobal.getLocaleText("acc.field.TradingAndProfitLossWithBudget")+"_v1";
            reportName = WtfGlobal.getLocaleText("acc.field.TradingAndProfitLossWithBudget");
        } else if (this.statementType=="CostCenter"){
            fileName = WtfGlobal.getLocaleText("acc.ccReport.tabTitle")+"_v1";
            reportName = WtfGlobal.getLocaleText("acc.ccReport.tabTitle");
        }            

        if (this.startMonth.getValue() == "" || this.startYear.getValue() == ""){
            this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
            this.startYear.setValue(this.yearStore.data.items[0].json[1]);
        }
        
        var startMonthName = this.startMonth.getValue();
        var startYear = this.startYear.getValue();

        var startDate = new Date(Date.parse(startMonthName + " 01" + ", "+ startYear));
        this.sDate = WtfGlobal.convertToGenericStartDate(startDate);

        var endDate = new Date(Date.parse(startMonthName + " " + startDate.getDaysInMonth()  + ", "+ startYear));
        this.eDate = WtfGlobal.convertToGenericEndDate(endDate);

        extraParams += "&costcenter="+this.costCenter.getValue();
        extraParams += "&reportView="+this.statementType;        
       
        var align = "none,currency,currency,currency,currency,currency,currency,none";
        
//        var url = exportUrl+"?filename="+encodeURIComponent(fileName)+
//        extraParams+"&filetype="+type
//        +"&stdate="+this.sDate+"&enddate="+this.eDate+"&withBudget="+true+"&nondeleted="+true+"&accountid="
//        +"&header="+header+"&title="+encodeURIComponent(title)+"&name="+encodeURIComponent(reportName)+"&width=150&get=27&align="+align+"&toggle="+this.toggle+"&singleGrid="+true;
       //ERP-37238: Converted to POST
        var url = exportUrl+"?";
        var urlExcludingParams = "";
        var paramsWithoutURL = "";
        var searchJsonString = "";
        if(this.searchJson!= null && this.searchJson!= undefined && this.searchJson!= ""){
            searchJsonString = "&searchJson="+this.searchJson+"&filterConjuctionCriteria="+this.filterConjuctionCrit;
        }
        
        var parameters ="filename="+encodeURIComponent(fileName)+
        extraParams+"&filetype="+type
        +"&stdate="+this.sDate+"&enddate="+this.eDate+"&withBudget="+true+"&nondeleted="+true+"&accountid="
        +"&header="+header+"&title="+encodeURIComponent(title)+"&name="+encodeURIComponent(reportName)+"&width=150&get=27&align="+align+"&toggle="+this.toggle+"&singleGrid="+true
        +"&month="+startMonthName+"&year="+startYear+searchJsonString;

        var resultStr=removeDuplicateParameters(parameters);
        urlExcludingParams = url;
        paramsWithoutURL = resultStr;
        url+=resultStr;
    
        if(type == "print") {
           url+="&generatedOnTime="+WtfGlobal.getGeneratedOnTimestamp();
            window.open(url, "mywindow","menubar=1,resizable=1,scrollbars=1");
        } else {
            WtfGlobal.postData(urlExcludingParams,paramsWithoutURL);
//            Wtf.get('downloadframe').dom.src  = url;
        }
    },
    
    getRowClass:function(record,grid){
        var colorCss="";
        switch(record.data["fmt"]){
            case "T":
                colorCss=" grey-background";
                break;
            case "B":
                colorCss=" red-background";
                break;
            case "H":
                colorCss=" header-background";
                break;
            case "A":
                colorCss=" darkyellow-background";
                break;
        }
        return grid.getRowClass()+colorCss;
    },

    formatAccountName:function(val,m,rec,i,j,s){
        var fmtVal=WtfGlobal.currencyRenderer(val);
        if(rec.data['fmt']){
            fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
        }
        else if(rec.data["level"]==0&&rec.data["accountname"]!="") {
            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
        }
        if(rec.data.accountflag) {
            fmtVal = WtfGlobal.accountLinkRenderer(fmtVal);
        }
        return fmtVal;
    },
    
    formatData:function(val,m,rec,i,j,s){
        var fmtVal=WtfGlobal.currencyRenderer(val);        
        if(rec.data['fmt']){
            fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
        }
        else if(rec.data["level"]==0&&rec.data["accountname"]!="") {
            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
        }
        return fmtVal;
    },
    /* format and return variance in percentage column value */
    formatVariancePercent:function(val,m,rec,i,j,s){
      var varianceInPercent = '<span style="font-weight:bold">'+val+'</span>';
      return varianceInPercent;
    },
    
    formatMoney:function(val,m,rec,i,j,s){
        var fmtVal=WtfGlobal.currencyRenderer(val);
        if(rec.data['fmt']){
            fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
        }
        else if(rec.data["level"]==0&&rec.data["accountname"]!="")
            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
        return fmtVal;
    },

    fetchStatement:function(){
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        WtfComMsgBox(29,4,true); //Show loading mask

        // get date from month & year drop-down lists
        if (this.startMonth.getValue() == "" || this.startYear.getValue() == ""){
            this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
            this.startYear.setValue(this.yearStore.data.items[0].json[1]);
        }

        var startMonthName = this.startMonth.getValue();
        var startYear = this.startYear.getValue();

        var startDate = new Date(Date.parse(startMonthName + " 01" + ", "+ startYear));
        this.sDate = WtfGlobal.convertToGenericStartDate(startDate);

        var endDate = new Date(Date.parse(startMonthName + " " + startDate.getDaysInMonth()  + ", "+ startYear));
        this.eDate = WtfGlobal.convertToGenericEndDate(endDate);

        if(this.sDate=="" || this.eDate=="") {
            WtfComMsgBox(42,2);
            return;
        }      

        var params={
            stdate:this.sDate,
            enddate:this.eDate,
            withBudget:true
        };
        params.searchJson=this.searchJson!=undefined?this.searchJson:"";
        params.filterConjuctionCriteria= this.filterConjuctionCrit;
        params.year= startYear;
        params.month= startMonthName;
        this.ajxUrl = Wtf.req.account+'CompanyManager.jsp';
        if(this.statementType=="MonthlyTradingAndProfitLoss" || this.statementType=="CostCenter") {
            params.mode=65;
            params.nondeleted=true;
            params.costcenter= this.costCenter.getValue();
            params.reportView= this.statementType;
            if(this.statementType != "CostCenter")
                params.singleGrid = true;
            this.ajxUrl = "ACCReports/getTradingAndProfitLossWithBudget.do";
        }
        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url:this.ajxUrl,
            params:params
        }, this, this.successCallback, this.failureCallback);
    },

    successCallback:function(response){
        WtfGlobal.resetAjaxTimeOut();
        this.hideLoading();
        if(response.success){
            this.total=response.data.total;
            this.lGrid.store.loadData(response.data);
            this.rGrid.store.loadData(response.data);
            this.doLayout();
            if((this.statementType!="MonthlyTradingAndProfitLoss" && this.total && this.total[0]==0 && this.total[1]==0)
                ||(this.statementType=="MonthlyTradingAndProfitLoss" && this.lGrid.store.getCount()<=3 && this.rGrid.store.getCount()<=3)){
                if(this.expButton)this.expButton.disable();
                if(this.printbtn)this.printbtn.disable();
            }else{
                if(this.expButton)this.expButton.enable();
                if(this.printbtn)this.printbtn.enable();
            }
            if (this.firstTime && (this.statementType == 'BalanceSheet'|| this.statementType=="TradingAndProfitLoss")) {
                this.lGrid.getView().refresh(true);
                this.rGrid.getView().refresh(true);
            }
            this.firstTime = false;
            this.collapseGrids();
        }
    },

    failureCallback:function(response){
        WtfGlobal.resetAjaxTimeOut();
        this.hideLoading();
    },

    hideLoading:function(){
        Wtf.MessageBox.hide();
    },

    getDates:function(start){
        var d=Wtf.serverDate;
        if(this.statementType=='BalanceSheet'&&start)
            return new Date('January 1, 1970 00:00:00 AM');
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom&&this.statementType!='BalanceSheet')
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start||this.statementType=='BalanceSheet')
            return fd;
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },

    showLastRec:function(pos){
        return WtfGlobal.currencySummaryRenderer(this.total[pos]);
    },

    // populate month & year drop-down list
    // SON REFACTOR - to move this function to Global / common class
//    getBookBeginningYear:function(isfirst){
//        var ffyear;
//        if(isfirst){
//            var cfYear=new Date(Wtf.account.companyAccountPref.fyfrom)
//            ffyear=new Date(Wtf.account.companyAccountPref.firstfyfrom)
//            ffyear=new Date( ffyear.getFullYear(),cfYear.getMonth(),cfYear.getDate()).clearTime()
//        }
//        else{
//            var fyear=new Date(Wtf.account.companyAccountPref.firstfyfrom).getFullYear()
//            ffyear=new Date( fyear,this.fmonth.getValue(),this.fdays.getValue()).clearTime()
//        }
//        var data=[];
//        var newrec;
//        if(ffyear==null||ffyear=="NaN"){
//            ffyear=new Date(Wtf.account.companyAccountPref.fyfrom)
//        }
//        var year=ffyear.getFullYear();
//        var temp=new Date();
//        var year1=temp.getFullYear();
//        data.push([0,year1]);
//        var i=1;
//        while(year1>=year){
//            data.push([i,--year1]);
//            i++;
//        }
//        if(!(ffyear.getMonth()==0&&ffyear.getDate()==1)){
//            data.push([1,year+1]);
//            newrec = new Wtf.data.Record({
//                id:1,
//                yearid:year+1
//            });
//        }
//        return data;
//    }, 

    collapseGrids : function() {
        for(var i=0; i< this.lGrid.getStore().data.length; i++){
            this.lGrid.collapseRow(this.lGrid.getView().getRow(i));
        }   
        for(var i=0; i< this.rGrid.getStore().data.length; i++){
            this.rGrid.collapseRow(this.rGrid.getView().getRow(i));
        }
    }
});
