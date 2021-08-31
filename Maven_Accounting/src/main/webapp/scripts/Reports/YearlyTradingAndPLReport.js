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

function yearlyTradingProfitLossDynamicLoad(params){
    var panel = Wtf.getCmp("yearlyTradingProfitandLoss");
    if (params.isCustomWidgetReport) {
        /*
         *Implementation to add this report in custom widget report. 
         * */
        panel = new Wtf.account.YearlyTradingAndProfitLoss({
            tabTip:WtfGlobal.getLocaleText("acc.YearlyP&L.tabTitle"),
            topTitle:'<center><font size=4>' + WtfGlobal.getLocaleText("acc.YearlyP&L.tabTitle") + '</font></center>',
            statementType:'MonthlyTradingAndProfitLoss',
            border : false,
            layout: 'fit',
            iconCls:'accountingbase financialreport',
            isCustomWidgetReport : params.isCustomWidgetReport //Flag for widget view
        });
        panel.on('account',callLedger);
        if (params.callbackFn) {
            /*
             *call callback function to add this report to widget.
             **/
            params.callbackFn.call(this, panel);
        }
    } else{
    if(panel==null){
        panel = new Wtf.account.YearlyTradingAndProfitLoss({
            id : 'yearlyTradingProfitandLoss',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.YearlyP&L.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.YearlyP&L.tabTitle"),
            topTitle:'<center><font size=4>' + WtfGlobal.getLocaleText("acc.YearlyP&L.tabTitle") + '</font></center>',
            statementType:'MonthlyTradingAndProfitLoss',
            border : false,
            closable: true,
            layout: 'fit',
            iconCls:'accountingbase financialreport'
        });
        Wtf.getCmp('as').add(panel);
        panel.on('account',callLedger);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    panel.on("activate",function(panel){
        panel.westPanel.setWidth(panel.getInnerWidth()/2);
        panel.doLayout();
    });
    }
}

//**********************************************************************************
Wtf.account.YearlyTradingAndProfitLoss=function(config){
    this.total=[0,0];
    this.leftHeading=(config.statementType=="BalanceSheet"?"Liability":"Debit");
    this.rightHeading=(config.statementType=="BalanceSheet"?"Asset":"Credit");
    
    this.uPermType=Wtf.UPerm.fstatement;
    this.permType=Wtf.Perm.fstatement;
    this.exportPermType=(config.statementType=="BalanceSheet"?this.permType.exportdatabsheet:this.permType.exportdatatradingpnl);
    this.printPermType=(config.statementType=="BalanceSheet"?this.permType.printbsheet:this.permType.printtradingpnl);
    this.toggle=0;

    this.summaryL = new Wtf.ux.grid.GridSummary();
    this.summaryR = new Wtf.ux.grid.GridSummary();
    this.Type=config.type!=undefined ? config.type:0;
    this.isMonthlyRevenue=config.monthlyRevenue!=undefined ? config.monthlyRevenue:false;
    this.isCustomWidgetReport=config.isCustomWidgetReport||false;
    
    this.FinalStatementRec = new Wtf.data.Record.create([
        {name: 'accountname'},
        {name: 'accountcode'},
        {name: 'accountid'},
        {name: 'amount_0'},
        {name: 'amount_1'},
        {name: 'amount_2'},
        {name: 'amount_3'},
        {name: 'amount_4'},
        {name: 'amount_5'},
        {name: 'amount_6'},
        {name: 'amount_7'},
        {name: 'amount_8'},
        {name: 'amount_9'},
        {name: 'amount_10'},        
        {name: 'accountflag'},
        {name: 'isdebit',type:'boolean'},
        {name: 'level'},
        {name: 'fmt'},
        {name: 'leaf'},
        {name: 'totalFlagAccountsWithchild'}
    ]);

    this.statementType=config.statementType||"Trading";

    this.lStroe = new Wtf.data.Store({              //Incomes
        reader: new Wtf.data.KwlJsonReader({
            root: "left"                            
        },this.FinalStatementRec),
        baseParams:{
            nondeleted:true
        },
        url: Wtf.req.account+'CompanyManager.jsp'
    });
    
    this.rStroe = new Wtf.data.Store({              //Expenses
        reader: new Wtf.data.KwlJsonReader({
            root: "right"                           
        },this.FinalStatementRec),
        baseParams:{
            nondeleted:true
        },
        url: Wtf.req.account+'CompanyManager.jsp'
    });
    
    var columnArr = [];
    columnArr.push({
        header:'<b>'+WtfGlobal.getLocaleText("acc.balanceSheet.particulars")+'</b>',
        dataIndex:'accountname',
        renderer:this.formatAccountName,
        width:250,
        pdfwidth: 80,
        summaryRenderer:function(){
            return WtfGlobal.summaryRenderer(WtfGlobal.getLocaleText("acc.common.total"));
        }.createDelegate(this)
    });
    
    if(Wtf.account.companyAccountPref.showaccountcodeinfinancialreport){
        columnArr.push({
            header:'<b>'+WtfGlobal.getLocaleText("acc.coa.accCode")+'</b>',
            dataIndex:'accountcode',
            //            sortable: true,
            hidden:!Wtf.account.companyAccountPref.showaccountcodeinfinancialreport,
            //            renderer:this.formatAccountName,
            width:100,
            align: 'center'
        });
    }

    for(var i=0; i<10; i++){

        columnArr.push({
            hidden: false,
            dataIndex: 'amount_'+i,      
            renderer:this.formatData,  
            width: 100,
            pdfwidth: 90,
            align:'right',
            style: 'text-align:right'
        });
    };

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
        // set column headers based on the months list
        // not sure if we need to change to lGrid.getStore() but so far rGrid.getStore() does work!
        var store = this.rGrid.getStore();

        var monthArray = store.data.items[store.data.length-1].json["months"];
        var startIndex = 1;
        if(Wtf.account.companyAccountPref.showaccountcodeinfinancialreport){
            startIndex = 2;
        }

        for(var i=0; i<monthArray.length; i++){            
            this.lGrid.getColumnModel().setColumnHeader((i+startIndex), '<div align=center><b>'+monthArray[i]["monthname"]+'</b></div>') ;            
            var column = this.lGrid.getColumnModel().getColumnById((i+startIndex));
            column.align= 'right';
            column.style= 'text-align:right';
        }

        var columnCount =  this.lGrid.getColumnModel().getColumnCount();
        var monthCount = monthArray.length;

        // show those months with data
        for(var i=1; i<(startIndex+monthCount); i++){
            this.lGrid.getColumnModel().setHidden(i, false) ;
        }        

        // show those months with data
        for(var i=(monthCount+startIndex); i<columnCount; i++){
            this.lGrid.getColumnModel().setHidden(i,true) ;
        } 
        this.Lcm = this.lGrid.getColumnModel();
        this.Lcm.setRenderer(monthCount, this.formatMoney) ;
        this.lstore1 = this.lGrid.getStore();
        this.lGrid.reconfigure(this.lstore1,this.Lcm);
        this.expandCollapseGrid("Collapse");

    }, this);

    this.rGrid.getStore().on("load", function(){

        // set column headers based on the months list
        var store = this.rGrid.getStore();

        var monthArray = store.data.items[store.data.length-1].json["months"];
        var startIndex = 1;
        if(Wtf.account.companyAccountPref.showaccountcodeinfinancialreport){
            startIndex = 2;
        }

        for(var i=0; i<monthArray.length; i++){            
            this.lGrid.getColumnModel().setColumnHeader((i+startIndex), '<div align=center><b>'+monthArray[i]["monthname"]+'</b></div>') ;            
            var column = this.lGrid.getColumnModel().getColumnById((i+startIndex));
            column.align= 'left';
            column.style= 'text-align:left';
        }

        var columnCount =  this.lGrid.getColumnModel().getColumnCount();
        var monthCount = monthArray.length;

        // show those months with data
        for(var i=1; i<(startIndex+monthCount); i++){
            this.rGrid.getColumnModel().setHidden(i, false) ;
        }                    

        // hide those months without data
        for(var i=(monthCount+startIndex); i<columnCount; i++){
            this.rGrid.getColumnModel().setHidden(i,true) ;
        }       

        this.Lcm = this.lGrid.getColumnModel();
        this.Rcm = this.rGrid.getColumnModel();

        this.Rcm.setRenderer(monthCount, this.formatMoney) ;
        this.Lcm.setRenderer(monthCount, this.formatMoney) ;

        this.lstore1 = this.lGrid.getStore();
        this.rstore1 = this.rGrid.getStore();

        this.lGrid.reconfigure(this.lstore1,this.Lcm);
        this.rGrid.reconfigure(this.rstore1,this.Rcm);  

        this.expandCollapseGrid("Collapse");

    }, this);
    

    var data=WtfGlobal.getBookBeginningYear(true);
    
    this.yearStore= new Wtf.data.SimpleStore({
        fields: [{
            name:'id',
            type:'int'
        }, 'yearid'],
        data :data
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

    this.endYear = new Wtf.form.ComboBox({
        store: this.yearStore,
        fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  //'Year',
        name:'endYear',
        displayField:'yearid',
        anchor:'95%',
        valueField:'yearid',
        forceSelection: true,
        mode: 'local',
        width:90,
        triggerAction: 'all',
        selectOnFocus:true
    });      
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);   
    this.grid = this.rGrid;

    var mnuBtns=[];
    var csvbtn=new Wtf.Action({
        iconCls:'pwnd '+'exportcsv',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToCSVTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToCSV")+"</span>",
        scope: this,
        handler:function(){
            this.exportWithTemplate('csv')
        }
    });
    mnuBtns.push(csvbtn)
    var xlsbtn=new Wtf.Action({
        iconCls:'pwnd '+'exportcsv',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"</span>",
        scope: this,
        handler:function(){
            this.exportWithTemplate('xls')
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
        hidden:this.isCustomWidgetReport,  //hide this button in widget view.
        value:"",
        selectOnFocus:true,
        forceSelection: true,
        emptyText:WtfGlobal.getLocaleText("acc.rem.9")  //,"Select a Cost Center"
    });
    this.PLTypeStore = new Wtf.data.SimpleStore({
        fields: [{
            name: 'typeid', 
            type: 'int'
        }, 'name'],
        data: [
        [0, "All"],
        [3, "Income"],
        [2, "Expense"]
        ]
    });

    this.PLTypeCombo = new Wtf.form.ComboBox({//All/Balance Sheet/Profit & Loss
        store: this.PLTypeStore,
        name: 'typeid',
        displayField: 'name',
        id: 'typeid',
        valueField: 'typeid',
        mode: 'local',
        value: this.Type,
        width: 100,
        listWidth: 200,
        triggerAction: 'all',
        typeAhead: true,
        selectOnFocus: true
    });    
    var btnArr=[];

    btnArr.push(
        (config.statementType=='BalanceSheet'?'':WtfGlobal.getLocaleText("acc.common.from")),
        this.startYear,
        (config.statementType=='BalanceSheet'?WtfGlobal.getLocaleText("acc.balanceSheet.AsOn"):WtfGlobal.getLocaleText("acc.common.to")),
        this.endYear
        );

    if(this.statementType=="MonthlyTradingAndProfitLoss" || this.statementType=="CostCenter"){
        if (!this.isCustomWidgetReport) { //hide this button in widget view.
            btnArr.push(WtfGlobal.getLocaleText("acc.common.costCenter"), this.costCenter);
        }
    }
    if (this.statementType == "MonthlyTradingAndProfitLoss") {
        btnArr.push(WtfGlobal.getLocaleText("acc.common.PLType"), this.PLTypeCombo);
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
    btnArr.push('-',this.resetBttn);
    
    /* Please do not delete this code as we are working on this functionality and 
     * We have created seperate ticket for Export Functionality for Yearly Trading and Profit/Loss Report.  ERP-24922
     */
    
//    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
//        btnArr.push(this.expButton=new Wtf.Button({
//            text:WtfGlobal.getLocaleText("acc.common.export"),  //'Export',
//            iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export'),
//            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details.',
//            disabled :true,
//            hidden :false,
//            scope: this,
//            menu:mnuBtns
//        }));
//    }
//    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
//
//        btnArr.push(this.printbtn=new Wtf.Button({
//            iconCls:'pwnd printButtonIcon',
//            text :WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
//            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details.',
//            disabled :true,
//            hidden :false,
//            scope: this,
//            handler:function(){
//                this.exportWithTemplate("print")
//            }
//        }));
//    }

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

    this.chartButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.chart"),
        iconCls: "accountingbase chart",
        scope: this,
        hidden: this.isCustomWidgetReport,
        menu: [
            {
                xtype: "button",
                text: WtfGlobal.getLocaleText("acc.common.LineChart"),
                iconCls: "x-tool-linechartwizard",
                scope: this,
                handler: function () {
                    var params = {}
                    params.chartType = Wtf.chartType.line;
                    this.showChart(params);
                }
            }
        ]
    })

    btnArr.push('-', this.chartButton);

    this.wrapperPanel = new Wtf.Panel({
        border:false,
        layout:"border",
        scope:this,
        items:[
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

    Wtf.account.YearlyTradingAndProfitLoss.superclass.constructor.call(this,config);
    this.addEvents({
        'account':true
    });
    if(this.statementType!="CostCenter"){
        this.fetchStatement();
    }
}

Wtf.extend( Wtf.account.YearlyTradingAndProfitLoss,Wtf.Panel,{
    onRowClickRGrid:function(g,i,e){
        // e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var accid=this.rGrid.getStore().getAt(i).data['accountid'];

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
        
        var startYear = this.startYear.getValue();

        var startDate = new Date(Date.parse("January" + " 01" + ", "+ startYear));
        
        var endYearName = this.endYear.getValue();
        
        var endDate = new Date(Date.parse("December" + " 31" + ", "+ endYearName));
        
        this.fireEvent('account',accid,startDate,endDate);
    },

    openLedgerForWholeDur:function(i){
        var accid=this.lGrid.getStore().getAt(i).data['accountid'];   

        var startYear = this.startYear.getValue();

        var startDate = new Date(Date.parse("January 01" + ", "+ startYear));

        var endYearName = this.endYear.getValue();

        var endDate = new Date(Date.parse("December 31" + ", "+ endYearName));

        this.fireEvent('account',accid,startDate,endDate);
    },
    
    openLedgerForCellDur:function(i){
        var lGridStore = this.lGrid.getStore();

        var accid=lGridStore.getAt(i).data['accountid'];         
            
        var startYear = this.startYear.getValue();

        var startDate = new Date(Date.parse("January 01" + ", "+ startYear));

        var endYearName = this.endYear.getValue();

        var endDate = new Date(Date.parse("December 31" + ", "+ endYearName));
        
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

        if(this.statementType=="BalanceSheet"){
            exportUrl = getExportUrl(27);
            fileName = "BalanceSheet";
            get = 27;
            toggle = this.toggle;
            jsonGrid = "{data:[{'header':'laccountname','title':'Particulars','width':'150','align':''},"+
            "{'header':'lamount','title':'Amount(Asset)','width':'150','align':'currency'},"+
            "{'header':'raccountname','title':'Particulars','width':'150','align':''},"+
            "{'header':'ramount','title':'Amount(Liability)','width':'150','align':'currency'}]}";
            header =  "Balance Sheet";
        }else{
            exportUrl = getExportUrl(Wtf.autoNum.MonthlyTradingPnl);

            if(this.statementType=="MonthlyTradingAndProfitLoss"){
                fileName =  WtfGlobal.getLocaleText("acc.MonthlyP&L.tabTitle");
                
            } else if (this.statementType=="CostCenter"){
                fileName = WtfGlobal.getLocaleText("acc.ccReport.tabTitle");
            }
            if (this.isMonthlyRevenue) {          //export
                fileName = WtfGlobal.getLocaleText("acc.monthlyRevenue.tabTitle");
            }
            extraParams += "&costcenter="+this.costCenter.getValue();
            extraParams += "&Nature="+this.PLTypeCombo.getValue();
            extraParams += "&reportView="+this.statementType;

            get = 28;
            jsonGrid = "{data:[{'header':'laccountname','title':'Particulars','width':'150','align':''},"+
            "{'header':'lamount','title':'Amount(Debit)','width':'150','align':'currency'},"+
            "{'header':'raccountname','title':'Particulars','width':'150','align':''},"+
            "{'header':'ramount','title':'Amount(Credit)','width':'150','align':'currency'}]}";
            header =  fileName;
        }

        // get date from month & year drop-down lists
        if (this.startYear.getValue() == ""){
            this.startYear.setValue(this.yearStore.data.items[0].json[1]);
            this.endYear.setValue(this.yearStore.data.items[0].json[1]);
        }

        this.sDate = "January" + ", " + this.startYear.getValue();
        this.eDate = "December" + ", " + this.endYear.getValue();

        var configstr = "{%22landscape%22:%22true%22,%22pageBorder%22:%22true%22,%22gridBorder%22:%22true%22,%22title%22:%22Test%20Title%22,%22subtitles%22:%22%22,%22headNote%22:%22Test%20Header%22,%22showLogo%22:%22false%22,%22headDate%22:%22false%22,%22footDate%22:%22true%22,%22footPager%22:%22false%22,%22headPager%22:%22true%22,%22footNote%22:%22Test%20Footer%22,%22textColor%22:%22000000%22,%22bgColor%22:%22FFFFFF%22}"
        var url = exportUrl+"?filename="+encodeURIComponent(fileName)+extraParams+"&config="+configstr+"&header"+header+
        "&filetype=pdf&stdate="+
        this.sDate+"&enddate="+this.eDate+
        "&toggle="+this.toggle+"&get="+get+"&gridconfig="+encodeURIComponent(jsonGrid);

        Wtf.get('downloadframe').dom.src = url;
    },

    setCostCenter: function(){
        this.costCenter.setValue("");//Select Default Cost Center as None
        Wtf.CostCenterStore.un("load", this.setCostCenter, this);
    },

    resetFilterAndFetchReport: function(costCenterId, startDate, endDate){
        this.costCenter.setValue(costCenterId);

        // get date from month & year drop-down lists
        if (this.startYear.getValue() == ""){
            this.startYear.setValue(this.yearStore.data.items[0].json[1]);
            this.endYear.setValue(this.yearStore.data.items[0].json[1]);
        }

        this.sDate = "January" + ", " + this.startYear.getValue();
        this.eDate = "December" + ", " + this.endYear.getValue();

        // check that from date and end date must be available
        if(this.sDate=="" || this.eDate=="") {
            WtfComMsgBox(42,2);
            return;
        }      

        // check that start date must be before end date
        var startMonthDate = new Date("January 01, " + this.startYear.getValue());
        var endMonthDate = new Date("December 31, " + this.endYear.getValue());
        
        if (startMonthDate.getMonth() > endMonthDate.getMonth()){
            WtfComMsgBox(1,2);
            return;            
        }        

        this.fetchStatement();
    },
    
    showChart : function(param) {
        var chartPanelID = "YearlyTradingProfitLossBarChart";
        var chartParams = this.getLineChartParams();
        chartParams.id = chartPanelID;
        chartParams.title = WtfGlobal.getLocaleText("acc.common.YearlyTradingPnL.LineChart");
        chartParams.url = this.getLineChartUrl();
        chartParams.chartConfig = this.getLineChartConfig(chartParams);
        chartParams.tabTipParams = [WtfGlobal.getLocaleText("acc.common.TT.YearlyTradingPnL")];
        var chart = Wtf.getCmp(chartPanelID);
        if(chart){
            Wtf.getCmp('as').remove(chart,true);
        }
        chart = getReportChartPanel(chartParams);
        Wtf.getCmp('as').add(chart);
        Wtf.getCmp('as').setActiveTab(chart);
        Wtf.getCmp('as').doLayout();
    },
    
    getStoreBaseParams: function () {
        var currentBaseParams = this.rStroe.baseParams;
        currentBaseParams.mode = 65;
        currentBaseParams.nondeleted = true;
        currentBaseParams.costcenter = this.costCenter.getValue();
        currentBaseParams.Nature = this.PLTypeCombo.getValue();
        currentBaseParams.reportView = this.statementType;
        currentBaseParams.stdate = this.sDate;
        currentBaseParams.enddate = this.eDate;
        
        if (this.statementType != "CostCenter") {
            currentBaseParams.singleGrid = true;
        }
        
        return currentBaseParams;
    },
    getLineChartParams: function () {
        var chartParams = {
            params: {}
        }
        chartParams.params = this.getStoreBaseParams();
        chartParams.params.chartType = Wtf.chartType.line;
        return chartParams;
    },
    getLineChartUrl: function () {
        return "ACCOtherReports/getYearlyTradingAndProfitLoss.do";
    },
    
    getLineChartConfig :function(chartParams){
        var params = {};
//        var valueFieldArr = ["Total Income", "Opening Stock", "Closing Stock", "Total Cost of Good Sold", "Gross Profit/Loss", "Total Expense", "Net Profit/Loss"];
        var valueFieldArr = ["Total Income", "Total Cost of Good Sold", "Gross Profit/Loss", "Total Expense", "Net Profit/Loss"];
//        var ghraphBulletTypeArr = ["diamond", "square", "bubble", "diamond", "round", "square", "diamond"];
        var ghraphBulletTypeArr = ["diamond", "square", "diamond", "square", "diamond"];
//        var lineColorArr = ["#A25FAC", "#FF0000", "#00D1FF", "#FFFF00", "#FF00FF", "#0066CB", "#993233"];
        var lineColorArr = ["#A25FAC", "#FF0000", "#FF00FF", "#0066CB", "#993233"];
        params.titleField = "monthname";
        params.valueField = valueFieldArr;
        params.ghraphBulletType = ghraphBulletTypeArr;
        params.lineColor = lineColorArr;
//        params.chartColor = "#CC0000", "#009987";
//        params.textColor = "#009987";
        params.valueTitle = "Total ( " + Wtf.pref.CurrencySymbol + " )";
        params.title = "Yearly Trading Profit and Loss Report";
        params.categoryAxisTitle = "Years";
        params.unit = Wtf.pref.CurrencySymbol;
        params.height = 520;
        
        if(chartParams.params.stdate && chartParams.params.enddate){
            params.subTitle = "From : " + chartParams.params.stdate + "\nTo : " + chartParams.params.enddate;
        }
        var chartConfig = getMultiLineChartConfig(params);
        return chartConfig;
    },

    exportWithTemplate:function(type){
        type = type?type:"csv";
        var exportUrl;
        var fileName;
        var reportName;
        var header = "accountname";

        // not sure if we need to change to lGrid.getStore() but so far rGrid.getStore() does work!
        var store = this.rGrid.getStore();
        var monthArray = store.data.items[store.data.length-1].json["months"];

        for(var i=0; i<monthArray.length; i++){            
            header += (",amount_" + i);
        }
        
        var title = "";
        var extraParams = "";

        // SON REFACTOR - NEED TO CENTRALIZE THE STATEMENTTYPE IN ONE PLACE LIKE AutoNum.WtfSettings
        if(this.statementType=="BalanceSheet"){
            if(this.toggle == 0){
                title = WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.18")+","+WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.19");
            }else{
                title = WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.19")+","+WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.18");
            }

            header = "raccountname,ramount,laccountname,lamount";
            exportUrl = getExportUrl(27);
            fileName = WtfGlobal.getLocaleText("acc.balanceSheet");
            reportName = WtfGlobal.getLocaleText("acc.balanceSheet");

        }else {
            title = WtfGlobal.getLocaleText("acc.report.2");
            
            for(var i=0; i<monthArray.length; i++){            
                title += (", " + monthArray[i]["monthname"]);
            }

            exportUrl = getExportUrl(Wtf.autoNum.MonthlyTradingPnl);
            
            if(this.statementType=="MonthlyTradingAndProfitLoss"){

                fileName =  WtfGlobal.getLocaleText("acc.MonthlyP&L.tabTitle")+"_v1";
                reportName = WtfGlobal.getLocaleText("acc.MonthlyP&L.tabTitle");

            } else if (this.statementType=="CostCenter"){
                fileName = WtfGlobal.getLocaleText("acc.ccReport.tabTitle");
                reportName = WtfGlobal.getLocaleText("acc.ccReport.tabTitle");
            }            
        }
        if(this.isMonthlyRevenue){          //print
            fileName =  WtfGlobal.getLocaleText("acc.monthlyRevenue.tabTitle")+"_v1";
            reportName = WtfGlobal.getLocaleText("acc.monthlyRevenue.tabTitle");
        }
        if (this.startYear.getValue() == ""){
            this.startYear.setValue(this.yearStore.data.items[0].json[1]);
            this.endYear.setValue(this.yearStore.data.items[0].json[1]);
        }
        
        this.sDate = "January" + ", " + this.startYear.getValue();
        this.eDate = "December" + ", " + this.endYear.getValue();

        extraParams += "&costcenter="+this.costCenter.getValue();
        extraParams += "&Nature="+this.PLTypeCombo.getValue();
        extraParams += "&reportView="+this.statementType;        

        var align = "none";

        for(var i=0; i<monthArray.length; i++){
            align += ",currency";
        }        

        var url = exportUrl+"?filename="+encodeURIComponent(fileName)+
        extraParams+"&filetype="+type
        +"&stdate="+this.sDate+"&enddate="+this.eDate
        +"&nondeleted="+true+"&accountid="
        +"&header="+header+"&title="+encodeURIComponent(title)+"&name="+encodeURIComponent(reportName)+"&width=150&get=27&align="+align+"&toggle="+this.toggle+"&singleGrid="+true;

        if(type == "print") {
            url+="&generatedOnTime="+WtfGlobal.getGeneratedOnTimestamp();
            window.open(url, "mywindow","menubar=1,resizable=1,scrollbars=1");
        } else {
            Wtf.get('downloadframe').dom.src  = url;
        }
    //        Wtf.get('downloadframe').dom.src = url;
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
        if (this.startYear.getValue() == ""){
            this.startYear.setValue(this.yearStore.data.items[0].json[1]);
            this.endYear.setValue(this.yearStore.data.items[0].json[1]);
        }

        this.sDate = "January" + ", " + this.startYear.getValue();
        this.eDate = "December" + ", " + this.endYear.getValue();

        if(this.sDate=="" || this.eDate=="") {
            WtfComMsgBox(42,2);
            return;
        }      

        if (this.startYear.getValue() > this.endYear.getValue()){
            WtfComMsgBox(1,2);
            return;            
        }

        var params={
            stdate:this.sDate,
            enddate:this.eDate
        };

        this.ajxUrl = Wtf.req.account+'CompanyManager.jsp';

        if(this.statementType=="Trading") {
            params.mode=63;
            params.nondeleted=true;
            this.ajxUrl = "ACCReports/getTrading.do";
        }

        if(this.statementType=="ProfitAndLoss") {
            params.mode=64;
            params.nondeleted=true;
            this.ajxUrl = "ACCReports/getProfitLoss.do";
        }

        if(this.statementType=="MonthlyTradingAndProfitLoss" || this.statementType=="CostCenter") {
            params.mode=65;
            params.nondeleted=true;
            params.costcenter= this.costCenter.getValue();
            params.Nature= this.PLTypeCombo.getValue();
            params.reportView= this.statementType;
            if(this.statementType != "CostCenter")
                params.singleGrid = true;
            this.ajxUrl = "ACCOtherReports/getYearlyTradingAndProfitLoss.do";
        }

        if(this.statementType=="BalanceSheet") {
            params.mode=66;
            params.nondeleted=true;
            this.ajxUrl = "ACCReports/getNewBalanceSheet.do";
        }

        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url:this.ajxUrl,
            params:params
        }, this, this.successCallback, this.failureCallback);
    },
    handleResetClick:function(){
        this.startYear.reset();
        this.endYear.reset();
        this.costCenter.reset();
        this.PLTypeCombo.reset();
        this.fetchStatement();
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

    swapGrids:function(){  // The summaryRenderer doesnt swap when the grids swap. Only the header and grid data swaps.
        this.Lcm = this.lGrid.getColumnModel();
        this.Rcm = this.rGrid.getColumnModel();
        this.lstore1 = this.lGrid.getStore();
        this.rstore1 = this.rGrid.getStore();
        this.lGrid. reconfigure(this.rstore1,this.Rcm);
        this.rGrid. reconfigure(this.lstore1,this.Lcm);
        if(this.toggle==0)
            this.toggle=1;
        else
            this.toggle=0;

        if(this.expandCollpseButton.getText() == WtfGlobal.getLocaleText("acc.field.Expand")){
            this.expandCollapseGrid("Collapse");
        }

    },
    
    collapseGrids : function() {
        for(var i=0; i< this.lGrid.getStore().data.length; i++){
            this.lGrid.collapseRow(this.lGrid.getView().getRow(i));
        }   
        for(var i=0; i< this.rGrid.getStore().data.length; i++){
            this.rGrid.collapseRow(this.rGrid.getView().getRow(i));
        }
    }
});
