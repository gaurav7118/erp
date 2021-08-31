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
Wtf.account.NewTrading=function(config){
    this.total=[0,0];
    this.leftHeading=(config.statementType=="BalanceSheet"?"Liability":"Debit");
    this.rightHeading=(config.statementType=="BalanceSheet"?"Asset":"Credit");
    this.uPermType=Wtf.UPerm.fstatement;
    this.permType=Wtf.Perm.fstatement;
    this.exportPermType=(config.statementType=="BalanceSheet"?this.permType.exportdatabsheet:this.permType.exportdatatradingpnl);
    this.printPermType=(config.statementType=="BalanceSheet"?this.permType.printbsheet:this.permType.printtradingpnl)
    this.toggle=0;
    this.summaryL = new Wtf.ux.grid.GridSummary();
    this.summaryR = new Wtf.ux.grid.GridSummary();
    this.selectedCurrencyRec ="";
    this.isSelectedCurrencyDiff =false;
    this.isCompareGlobal=false;
    this.periodView=config.periodView;
    this.FinalStatementRec = new Wtf.data.Record.create([
        {name: 'accountname'},
        {name: 'accountcode'},
        {name: 'accountid'},
        {name: 'amount'},
        {name: 'amountInSelectedCurrency'},
        {name: 'preamount'},
        {name: 'accountforecast'},
        {name: 'accountbudget'},
        {name: 'accountflag'},
        {name: 'isdebit',type:'boolean'},
        {name: 'level'},
        {name: 'fmt'},
        {name: 'leaf'},
        {name: 'openingamount'},
        {name: 'periodamount'},
        {name: 'endingamount'},
        {name: 'totalFlagAccountsWithchild'}
    ]);

    this.statementType=config.statementType||"Trading";
        
         this.currencyRec = new Wtf.data.Record.create([
            {name: 'currencyid',mapping:'tocurrencyid'},
            {name: 'symbol'},
            {name: 'currencyname',mapping:'tocurrency'},
            {name: 'exchangerate'},
            {name: 'htmlcode'}
         ]);
         
         this.currencyStoreCMB = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.currencyRec),
    //        url:Wtf.req.account+'CompanyManager.jsp'
            url:"ACCCurrency/getCurrencyExchange.do"
         });

    
       this.currencyFilter= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.currency.cur"),  //'Currency',
            hiddenName:'currencyid',
            width : 150,
            store:this.currencyStoreCMB,
            valueField:'currencyid',
            allowBlank : false,
            forceSelection: true,
            displayField:'currencyname',
            scope:this,
            selectOnFocus:true
        });
        
        this.typeStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['All',false],['Exclude Previous Year Balance',true]]
        });
        
         this.excludeTypeCmb= new Wtf.form.ComboBox({
            labelSeparator:'',
            labelWidth:0,
            triggerAction:'all',
            mode: 'local',
            valueField:'value',
            displayField:'name',
            hidden:!this.periodView && this.statementType=="TradingAndProfitLoss",
            store:this.typeStore,
            value:true,
            width:200,
            name:'excludePreviousYear',
            hiddenName:'excludePreviousYear'
        });
        
//        this.currencyFilter.on('select', function(){
//            this.selectedCurrencyRec= this.currencyStoreCMB.getAt(this.currencyStoreCMB.find('currencyid',this.currencyFilter.getValue())!=-1?this.currencyStoreCMB.find('currencyid',this.currencyFilter.getValue()):WtfGlobal.getCurrencyID());   
//            }, this);
        
    this.lStroe = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "left"							// Assets on Left side in Balance Sheet          Neeraj
        },this.FinalStatementRec),
        baseParams:{
            nondeleted:true
        },
        url: Wtf.req.account+'CompanyManager.jsp'
    });
    this.rStroe = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "right"							//  Liabilities on right side in Balance Sheet          Neeraj
        },this.FinalStatementRec),
        baseParams:{
            nondeleted:true
        },
        url: Wtf.req.account+'CompanyManager.jsp'
    });    
    this.lGrid = new Wtf.grid.HirarchicalGridPanel({
        //plugins:[this.summaryL],
        //stripeRows :true,
        autoScroll:true,
        store: this.lStroe,
        hirarchyColNumber:0,
        columns: [{
            header:'<b>'+WtfGlobal.getLocaleText("acc.balanceSheet.particulars")+'</b>',
            dataIndex:'accountname',
            renderer:this.formatAccountName,
            width:150,
            summaryRenderer:function(){return WtfGlobal.summaryRenderer(WtfGlobal.getLocaleText("acc.common.total"));}.createDelegate(this)
        },{
            header:'<b>'+WtfGlobal.getLocaleText("acc.coa.accCode")+'</b>',
            dataIndex:'accountcode',
//            sortable: true,
            hidden:!Wtf.account.companyAccountPref.showaccountcodeinfinancialreport,
//            renderer:this.formatAccountName,
            width:100,
            align: 'center'
        },{
            header:"<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.OpeningAmount")+"</b></div>",
            dataIndex:'openingamount',
            hidecurrency : true,
            hidden:!this.periodView,
            renderer:this.formatMoney,
            summaryRenderer:this.showLastRec.createDelegate(this,[0])
        },{
            header:"<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.PeriodAmount")+"</b></div>",
            dataIndex:'periodamount',
            hidecurrency : true,
            hidden:!this.periodView,
            renderer:this.formatMoney,
            summaryRenderer:this.showLastRec.createDelegate(this,[0])
        },{
            header:"<div align=right><b>"+WtfGlobal.getLocaleText("acc.masterConfig.chequeLayoutSetup.amt")+" ("+WtfGlobal.getCurrencyName()+")</b></div>",
            dataIndex:this.periodView?'endingamount':'amount',
            hidecurrency : true,
            renderer:this.formatMoney,
            summaryRenderer:this.showLastRec.createDelegate(this,[0])
        },{
            header:"<div align=right><b>"+WtfGlobal.getLocaleText("acc.masterConfig.chequeLayoutSetup.amt")+" ("+WtfGlobal.getCurrencyName()+") </b></div>",
            dataIndex:'preamount',
            hidden:((config.statementType!="TradingAndProfitLoss") || config.consolidateFlag),
            renderer:this.formatMoney,
            summaryRenderer:this.showLastRec.createDelegate(this,[2])
        },{
            header:"<div align=right><b>"+WtfGlobal.getLocaleText("acc.masterConfig.chequeLayoutSetup.amt")+" ("+WtfGlobal.getCurrencyName()+")</b></div>",
            dataIndex:'amountInSelectedCurrency',
//            dataIndex:'amount',
            hidecurrency : true,
            renderer:this.formatMoneyInSelectedCurr.createDelegate(this),
            summaryRenderer:this.showLastRec.createDelegate(this,[2])
        }],
        border : false,
        loadMask : true,
        viewConfig: {
            forceFit:true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn")),
            deferEmptyText: false
        }
    });
    this.lGrid.on("render", WtfGlobal.autoApplyHeaderQtip);
    this.lGrid.view.refresh.defer(1, this.lGrid.view); 
    this.lGrid.on('rowclick',this.onRowClickLGrid, this);
    this.rGrid = new Wtf.grid.HirarchicalGridPanel({
        plugins:[this.summaryR],
        //stripeRows :true,
        autoScroll:true,
        store: this.rStroe,
        hirarchyColNumber:0,
        columns: [{
            header:'<b>'+WtfGlobal.getLocaleText("acc.balanceSheet.particulars")+'</b>',
            dataIndex:'accountname',
            width:150,
            renderer:this.formatAccountName,
            summaryRenderer:function(){return WtfGlobal.summaryRenderer(WtfGlobal.getLocaleText("acc.common.total"));}.createDelegate(this)
        },{
            header:'<b>'+WtfGlobal.getLocaleText("acc.coa.accCode")+'</b>',
            dataIndex:'accountcode',
//            sortable: true,
            hidden:!Wtf.account.companyAccountPref.showaccountcodeinfinancialreport,
//            renderer:this.formatAccountName,
            width:100,
            align: 'center'
        },{
            header:"<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.OpeningAmount")+"</b></div>",
            dataIndex:'openingamount',
            hidecurrency : true,
            hidden:!this.periodView,
            renderer:this.formatMoney,
            summaryRenderer:this.showLastRec.createDelegate(this,[0])
        },{
            header:"<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.PeriodAmount")+"</b></div>",
            dataIndex:'periodamount',
            hidecurrency : true,
            hidden: !this.periodView,
            renderer:this.formatMoney,
            summaryRenderer:this.showLastRec.createDelegate(this,[0])
        },{
            header:"<div align=right><b>"+(config.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.Amount(asset)"):WtfGlobal.getLocaleText("acc.P&L.Amount(Credit)"))+" ("+WtfGlobal.getCurrencyName()+")</b></div>",
            dataIndex:this.periodView?'endingamount':'amount',
            renderer:this.formatMoney,
            hidecurrency : true,
            summaryRenderer:this.showLastRec.createDelegate(this,[1])
        },{
            header:"<div align=right><b>"+(config.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.Amount(asset)"):WtfGlobal.getLocaleText("acc.P&L.Amount(Credit)"))+"("+WtfGlobal.getCurrencyName()+")</b></b></div>",
            dataIndex:'preamount',
            renderer:this.formatMoney,
            hidden:((config.statementType!="TradingAndProfitLoss") || config.consolidateFlag),
            summaryRenderer:this.showLastRec.createDelegate(this,[3])
        }],
        border : false,
        loadMask : true,
        viewConfig: {
            forceFit:true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn")),
            deferEmptyText: false
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
        for(var i=0; i< this.lGrid.getStore().data.length; i++){
            this.lGrid.collapseRow(this.lGrid.getView().getRow(i));

        }
    }, this);

    this.rGrid.getStore().on("load", function(){
        for(var i=0; i< this.rGrid.getStore().data.length; i++){
            this.rGrid.collapseRow(this.rGrid.getView().getRow(i));
        }
    }, this);
    
    var sdateSavedSearch;
    var edateSavedSearch;
    if(config.searchJson != undefined && config.searchJson != ""){
        sdateSavedSearch = JSON.parse(config.searchJson).data[0].sdate;
        edateSavedSearch = JSON.parse(config.searchJson).data[0].edate;
    }
    
this.firstTime=true;
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate',
        format:WtfGlobal.getOnlyDateFormat(),
        //readOnly:true,
        hidden:config.statementType=='BalanceSheet',
        value:this.getDates(true, sdateSavedSearch)
    });
     this.startPreDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stpredate',
        format:WtfGlobal.getOnlyDateFormat(),
       // readOnly:true,
        //width : 70,
//        hidden:config.statementType=='BalanceSheet',
        value:this.getPreDates(this.startDate.getValue())
    });    
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:(config.statementType=='BalanceSheet'?WtfGlobal.getLocaleText("acc.balanceSheet.AsOn"):WtfGlobal.getLocaleText("acc.common.to")),
        format:WtfGlobal.getOnlyDateFormat(),
       // readOnly:true,
        name:'enddate',
        value:this.getDates(false, edateSavedSearch)
    });
     this.endPreDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),
        format:WtfGlobal.getOnlyDateFormat(),
        //readOnly:true,
        //width : 70,
        name:'endpredate',
        value:this.getPreDates(this.endDate.getValue())
    });
    this.startDate.on("change",function(){    
     this.startPreDate.setValue(this.startDate.getValue().add(Date.YEAR, -1))
    },this);
    
    this.endDate.on("change",function(){    
     this.endPreDate.setValue(this.endDate.getValue().add(Date.YEAR, -1))
    },this);
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
    mnuBtns.push(csvbtn);
        var xlsbtn=new Wtf.Action({
        iconCls:'pwnd '+'exportcsv',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"</span>",
        scope: this,
        handler:function(){
            this.exportWithTemplate("xls");
        }
    });
    mnuBtns.push(xlsbtn);
     var pdfbtn=new Wtf.Action({
        iconCls:'pwnd '+'exportpdf',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToPDFTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToPDF")+"</span>",
        scope: this,
        handler:function(){
            this.exportPdfTemplate()
        }
    });
   var jasperpdf=new Wtf.Action({
            iconCls:'pwnd '+'exportpdf',
            text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToCSVTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToJasperPdf")+"</span>",
            scope: this,
            handler:function(){
                this.exportPdfTemplate(true);
            }
        });
       mnuBtns.push(jasperpdf)  
    
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
    
    this.currencyStoreCMB.on('load', function(){
                this.currencyFilter.setValue(WtfGlobal.getCurrencyID());
            }, this);
    this.currencyStoreCMB.load();
    var btnArr=[];
    var btnArr1=[];

    btnArr.push(
        (config.statementType=='BalanceSheet'?'':WtfGlobal.getLocaleText("acc.common.from")),this.startDate,
        (config.statementType=='BalanceSheet'?WtfGlobal.getLocaleText("acc.balanceSheet.AsOn"):WtfGlobal.getLocaleText("acc.common.to")),this.endDate
        );

    if(this.statementType=="TradingAndProfitLoss" || this.statementType=="CostCenter"){
        btnArr1.push(WtfGlobal.getLocaleText("acc.common.costCenter"),this.costCenter);
    }
    if(this.statementType=="TradingAndProfitLoss"){
        btnArr1.push("-",WtfGlobal.getLocaleText("acc.common.currencyFilterLable"),this.currencyFilter);
    }

     this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: config.moduleid,
        advSearch: false,
        sdate: this.startDate.getValue(),
        edate: this.endDate.getValue()
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
//        id: 'advanced3', // In use, Do not delete
        scope: this,
//        hidden:(this.moduleid==undefined)?true:false,        
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    var comboReader = new Wtf.data.Record.create([
    {
        name: 'id',
        type: 'string'
    },
    {
        name: 'name',
        type: 'string'
    },
    {
        name: 'templatecode',
        type: 'string'
    }
    ]);
   
    
    this.templateStore = new Wtf.data.Store({
        url :'ACCAccount/getPnLTemplates.do',
        root: 'data',
        remoteSort:true,
        reader: new Wtf.data.KwlJsonReader({
            root: 'data'
        },comboReader),
        baseParams:{
            isdropdown:true
        }
    });
    
    this.templateStore.on("load", function(){
        this.templateStore.insert(0, new Wtf.data.Record({
            'id' : '-1', 
            'name' : 'None',
            'templatecode' : "" 
        }));
        this.templateCombo.setValue('-1');
    }, this);
    this.templateStore.load();
    this.templateCombo= new Wtf.form.ComboBox({
        fieldLabel:(this.isCustomer?WtfGlobal.getLocaleText("acc.het.101"):WtfGlobal.getLocaleText("acc.field.DebitAccount*")),
        width:120,
        store: this.templateStore,
        valueField:'id',
        displayField:'name',
        mode: 'local',
        typeAhead: true,
        forceSelection: true,
        selectOnFocus:true,
        triggerAction:'all',
        scope:this
    });
    
//    btnArr.push('-','Custom Layout ', this.templateCombo);
    
    btnArr.push('-',this.excludeTypeCmb);
    btnArr.push('-',{
        xtype:'button',
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
        tooltip:WtfGlobal.getLocaleText("acc.common.fetchTT"),
        iconCls:'accountingbase fetch',
        tooltip:(config.statementType=='BalanceSheet'?WtfGlobal.getLocaleText("acc.cc.26"):WtfGlobal.getLocaleText("acc.cc.27")),
        scope:this,
        handler:this.fetchStatement
    });
    if ((config.statementType == "BalanceSheet" || config.statementType=="TradingAndProfitLoss")  && !config.consolidateFlag) {
        btnArr.push('-', (!this.periodView?WtfGlobal.getLocaleText("acc.common.compareWith"):""),
                (!this.periodView?WtfGlobal.getLocaleText("acc.common.from"):""),(!this.periodView?this.startPreDate:""),
                (!this.periodView?WtfGlobal.getLocaleText("acc.common.to"):""), (!this.periodView?this.endPreDate:""),{
        xtype:'button',
        text:WtfGlobal.getLocaleText("acc.common.compare"),  //'Fetch',
        iconCls:'accountingbase fetch',
        tooltip:WtfGlobal.getLocaleText("acc.cc.balanceSheet.compare.TT"),
        scope:this,
        hidden:this.periodView,
        style:" margin-left: 5px;",
        handler:this.fetchCompareStatement
    }
                );
    }
    
     btnArr.push('-',this.AdvanceSearchBtn);
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
        btnArr.push("-",this.expButton=new Wtf.Button({
    //        iconCls:'pwnd '+'exportcsv',
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

    btnArr1.push(this.ToggleButton=new Wtf.Button({			// Used for toggling assets and liabilities from Left to Right & Vice versa
            text:WtfGlobal.getLocaleText("acc.balanceSheet.toggle"),  //'Toggle',
            iconCls:'pwnd toggleButtonIcon',
            tooltip :(this.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.toggleTT"):WtfGlobal.getLocaleText("acc.balanceSheet.toggleTT")),
            scope: this,
            handler: this.swapGrids,
            hidden:true//(config.statementType=="BalanceSheet"?false:true)
        }));

//   this.expButton=new Wtf.exportButton({
//        obj:this,
//        id:"exportReports"+config.helpmodeid,
//        tooltip :'Export report details',
//        params:{ stdate:WtfGlobal.convertToGenericDate(this.getDates(true)),
//                 enddate:WtfGlobal.convertToGenericDate(this.getDates(false))/*,
//                 accountid:this.accountID||config.accountID*/
//        },
//        menuItem:{csv:true,pdf:true,rowPdf:false},
//        get:27
//    });

    this.expandCollpseButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.Expand"),
            tooltip:WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
            iconCls:'pwnd toggleButtonIcon',
            scope:this,
            handler: function(){
                this.expandCollapseGrid(this.expandCollpseButton.getText());
            }
        });

    this.newTrading = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.SplitView"),
        tooltip:WtfGlobal.getLocaleText("acc.field.SplitView"),
        iconCls:'pwnd splitViewIcon',
        hidden:false,//(config.statementType=="BalanceSheet"?true:false),
        scope:this,
        handler: function(){
           this.templateid = this.templateCombo.getValue();
           (!(config.statementType=="BalanceSheet")?TradingProfitLoss(config.consolidateFlag, this.templateid):periodViewBalanceSheet(config.consolidateFlag,"", this.templateid));
        }
    });


    btnArr1.push('-', this.expandCollpseButton);
//    btnArr1.push('-', this.newTrading);
    this.customLayoutButton = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.ViewCustomLayout"),
        tooltip:WtfGlobal.getLocaleText("acc.field.Clicktoviewcustomlayout"),
        iconCls:'accountingbase fetch',
        scope:this,
        handler: function(){
            var window = new Wtf.account.selectCustomLayout({
                closable: true,
                templatetype:0
            });
            window.show();
        }
    });
    
    btnArr1.push('-', this.customLayoutButton);
    
    this.periodViewBtn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.HistoricalView"),
        tooltip:WtfGlobal.getLocaleText("acc.field.HistoricalView"),
        iconCls:'pwnd splitViewIcon',
        hidden:(this.periodView),
        scope:this,
        handler: function(){
            this.templateid = this.templateCombo.getValue();
            TradingProfitLossWithOpening(config.consolidateFlag,"", this.templateid);
        }
    });
    btnArr1.push('-',this.periodViewBtn);
    
    this.wrapperPanel = new Wtf.Panel({
        border:false,
        layout:"border",
        scope:this,
        items:[this.objsearchComponent,
            /*this.centerPanel = new Wtf.Panel({
                region:'center',
                layout:'fit',
                width:'49%',
                border:true,
                items:this.statementType=="BalanceSheet"?this.lGrid:this.rGrid
            }),*/
            this.westPanel = new Wtf.Panel({
                width:'60%',
                region:'center',
                layout:'fit',
                //width:'49%',
                border:false,
                //split:true,
                items:this.lGrid
            }),
        {layout:'fit',region:'west',width:'20%'},{layout:'fit',region:'east',width:'20%'}],
        tbar:btnArr1
    });

    Wtf.apply(this,{
        defaults:{border:false,bodyStyle:"background-color:white;"},
        saperate:true,
        statementType:"Trading",
        items:this.wrapperPanel,
        tbar:btnArr
    },config);

     Wtf.account.NewTrading.superclass.constructor.call(this,config);
     this.addEvents({
        'account':true
     });
     if(this.statementType!="CostCenter"){
        //this.fetchStatement();   //ERP-28938
     }
}

Wtf.extend( Wtf.account.NewTrading,Wtf.Panel,{
    onRowClickRGrid:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var accid=this.rGrid.getStore().getAt(i).data['accountid'];
        var searchJson = (this.objsearchComponent.advGrid != undefined) ? this.objsearchComponent.advGrid.getJsonofStore() : "" ;
        this.fireEvent('account',searchJson,this.filterConjuctionCrit,accid,this.startDate.getValue(),this.endDate.getValue(),"",this.excludeTypeCmb.getValue());
    },
    onRowClickLGrid:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var accid=this.lGrid.getStore().getAt(i).data['accountid'];      
        var searchJson = (this.objsearchComponent.advGrid != undefined) ? this.objsearchComponent.advGrid.getJsonofStore() : "" ;
        this.fireEvent('account',searchJson,this.filterConjuctionCrit,accid,this.startDate.getValue(),this.endDate.getValue(),"",this.excludeTypeCmb.getValue());
    },
    expandCollapseGrid : function(btntext){
        if(btntext ==WtfGlobal.getLocaleText("acc.field.Collapse")){
            for(var i=0; i< this.lGrid.getStore().data.length; i++){
                this.lGrid.collapseRow(this.lGrid.getView().getRow(i));

            }

            /*for(var i=0; i< this.rGrid.getStore().data.length; i++){
                this.rGrid.collapseRow(this.rGrid.getView().getRow(i));
            }*/

            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if(btntext == WtfGlobal.getLocaleText("acc.field.Expand")){
            for(var i=0; i< this.lGrid.getStore().data.length; i++){
                this.lGrid.expandRow(this.lGrid.getView().getRow(i));

            }

            /*for(var i=0; i< this.rGrid.getStore().data.length; i++){
                this.rGrid.expandRow(this.rGrid.getView().getRow(i));
            }*/

            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    },
    exportPdfTemplate:function(isJasper){
        var get;
        var fileName;
        var jsonGrid;
        var exportUrl;
        var header;
        var extraParams="";
        var templatecode = '-1';
        var filterConjuctionCriteria="";
        var searchJson="";
        var index = this.templateStore.find('id', this.templateCombo.getValue());
        if(index > -1){
            templatecode = this.templateStore.getAt(index).data.templatecode;
        }
        if(this.statementType=="BalanceSheet"){
//            title = "Particulars,Amount(Asset),Particulars,Amount(Liability)";
            exportUrl = getExportUrl(27, this.consolidateFlag);
            fileName = "BalanceSheet";
            get = 27;
            toggle = this.toggle;
            searchJson= this.searchJson!=undefined?this.searchJson:"";
            filterConjuctionCriteria= this.filterConjuctionCrit;
            jsonGrid = "{data:[{'header':'laccountname','title':'Particulars','width':'150','align':''},"+
                            "{'header':'lamount','title':'Amount(Asset)','width':'150','align':'currency'},"+
                            "{'header':'raccountname','title':'Particulars','width':'150','align':''},"+
                            "{'header':'ramount','title':'Amount(Liability)','width':'150','align':'currency'}]}";
            header =  "Balance Sheet";
        }else{
//            title = "Particulars,Amount(Debit),Particulars,Amount(Credit)";
            var currencyFilterRec=WtfGlobal.searchRecord(this.currencyStoreCMB,this.currencyFilter.getValue(),'currencyid');
            this.isSelectedCurrencyDiff=(this.currencyFilter!=null&&this.currencyFilter!=undefined)?(this.currencyFilter.getValue()!=WtfGlobal.getCurrencyID()):false;
            exportUrl = getExportUrl(28, this.consolidateFlag);
            if(this.statementType=="TradingAndProfitLoss"){
                fileName = "Trading Profit and Loss Statement";
            } else if (this.statementType=="CostCenter"){
                fileName = "Cost Center Report";
            }
            extraParams += "&costcenter="+this.costCenter.getValue();
            extraParams += "&reportView="+this.statementType;
            extraParams += "&filterCurrency="+this.currencyFilter.getValue();
            extraParams += "&externalcurrencyrate="+((currencyFilterRec!=null&&currencyFilterRec!=undefined)?currencyFilterRec.data.exchangerate:0); 
            extraParams += "&isCompareGlobal="+((this.isCompareGlobal!=null&&this.isCompareGlobal!=undefined)?this.isCompareGlobal:false); 
            extraParams += "&isSelectedCurrencyDiff="+((this.isSelectedCurrencyDiff!=undefined&&this.isSelectedCurrencyDiff!=null)?this.isSelectedCurrencyDiff:false); 
            get = 28;
            searchJson= this.searchJson!=undefined?this.searchJson:"";
            filterConjuctionCriteria= this.filterConjuctionCrit;
            jsonGrid = "{data:[{'header':'laccountname','title':'Particulars','width':'150','align':''},"+
                            "{'header':'lamount','title':'Amount(Debit)','width':'150','align':'currency'},"+
                            "{'header':'raccountname','title':'Particulars','width':'150','align':''},"+
                            "{'header':'ramount','title':'Amount(Credit)','width':'150','align':'currency'}]}";
            header =  fileName;
        }
        var configstr = "{%22landscape%22:%22true%22,%22pageBorder%22:%22true%22,%22gridBorder%22:%22true%22,%22title%22:%22Test%20Title%22,%22subtitles%22:%22%22,%22headNote%22:%22Test%20Header%22,%22showLogo%22:%22false%22,%22headDate%22:%22false%22,%22footDate%22:%22true%22,%22footPager%22:%22false%22,%22headPager%22:%22true%22,%22footNote%22:%22Test%20Footer%22,%22textColor%22:%22000000%22,%22bgColor%22:%22FFFFFF%22}"
        if(this.consolidateFlag) {
            var url = exportUrl+"&filename="+encodeURIComponent(fileName)+extraParams+"&config="+configstr+"&header"+header+"&isJasper="+isJasper+"&filetype=pdf&stdate="+this.sdate+"&enddate="+this.edate+"&toggle="+this.toggle
                     +"&searchJson="+searchJson+"&filterConjuctionCriteria="+filterConjuctionCriteria+"&get="+get+"&gridconfig="+encodeURIComponent(jsonGrid)+"&templatecode="+templatecode+"&excludePreviousYear="+ this.excludeTypeCmb.getValue();
        } else {
            url = exportUrl+"?filename="+encodeURIComponent(fileName)+extraParams+"&config="+configstr+"&header"+header+"&isJasper="+isJasper+"&filetype=pdf&stdate="+this.sdate+"&enddate="+this.edate+"&toggle="+this.toggle
                     +"&searchJson="+searchJson+"&filterConjuctionCriteria="+filterConjuctionCriteria+"&get="+get+"&gridconfig="+encodeURIComponent(jsonGrid)+"&templatecode="+templatecode+"&stpredate="+this.presdateCompare+"&endpredate="+this.preedateCompare+"&periodView="+this.periodView+"&excludePreviousYear="+ this.excludeTypeCmb.getValue();
        }
            Wtf.get('downloadframe').dom.src = url;

        /*new Wtf.selectTempWin({
                type:'pdf',
                get:get,
                stdate:this.sdate,
                enddate:this.edate,
                accountid:"",
                extra:{},
                mode:"",
                paramstring:"",
                filename:fileName,
                storeToload:"",//obj.pdfStore,
                gridConfig : jsonGrid,
                grid:"",
                json:""
            });*/
    },
    setCostCenter: function(){
        this.costCenter.setValue("");//Select Default Cost Center as None
        Wtf.CostCenterStore.un("load", this.setCostCenter, this);
    },
    resetFilterAndFetchReport: function(costCenterId, startDate, endDate){
        this.costCenter.setValue(costCenterId);
        this.startDate.setValue(startDate?startDate:this.getDates(true));
        this.startPreDate.setValue(this.startDate.getValue());
        this.endPreDate.setValue(this.endDate.getValue());
        this.endDate.setValue(endDate?endDate:this.getDates(false));
        this.fetchStatement();
    },
    exportWithTemplate:function(type){
        type = type?type:"csv";
        var exportUrl;
        var fileName;
        var reportName;
        var header = "accountname,amount";//,raccountname,ramount";
         var title = "";
         var extraParams = "";
         var templatecode = '-1';
        var index = this.templateStore.find('id', this.templateCombo.getValue());
        if(index > -1){
            templatecode = this.templateStore.getAt(index).data.templatecode;
        }
        if(this.statementType=="BalanceSheet"){
        	if(this.toggle == 0){
                title = WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.18")+","+WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.19");
        	}else{
                title = WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.19")+","+WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.18");
        	}
            header = "raccountname,ramount,laccountname,lamount";
            exportUrl = getExportUrl(27, this.consolidateFlag);
            fileName = WtfGlobal.getLocaleText("acc.balanceSheet");
            reportName = WtfGlobal.getLocaleText("acc.balanceSheet");
             searchJson= this.searchJson!=undefined?this.searchJson:"";
            filterConjuctionCriteria= this.filterConjuctionCrit;
        }else{
             var currencyFilterRec=WtfGlobal.searchRecord(this.currencyStoreCMB,this.currencyFilter.getValue(),'currencyid');
            this.isSelectedCurrencyDiff=(this.currencyFilter!=null&&this.currencyFilter!=undefined)?(this.currencyFilter.getValue()!=WtfGlobal.getCurrencyID()):false;
            if(!this.isCompareGlobal&&!this.isSelectedCurrencyDiff){
                if (this.periodView && Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                    title = WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.coa.accCode") + "," + "Opening Amount, Period Amount, Ending Amount";
                } else if (this.periodView) {
                    title = WtfGlobal.getLocaleText("acc.report.2") + "," + "Opening Amount, Period Amount, Ending Amount";
                } else if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                    title = WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.coa.accCode") + "," + "Amount" + " (" + WtfGlobal.getCurrencyName() + ")";
                } else {
                    title = WtfGlobal.getLocaleText("acc.report.2") + "," + "Amount" + " (" + WtfGlobal.getCurrencyName() + ")";
                }
            }else{
                if (this.isSelectedCurrencyDiff && Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                    title = WtfGlobal.getLocaleText("acc.report.2")+"," + WtfGlobal.getLocaleText("acc.coa.accCode") + "," +"Amount"+ " ("+WtfGlobal.getCurrencyName()+"),"+"Amount"+ " ("+currencyFilterRec.data.currencyname+")";
                } else if(this.isSelectedCurrencyDiff){
                    title = WtfGlobal.getLocaleText("acc.report.2")+","+"Amount"+ " ("+WtfGlobal.getCurrencyName()+"),"+"Amount"+ " ("+currencyFilterRec.data.currencyname+")";//WtfGlobal.getLocaleText("acc.report.3")+","+WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.4");
                } else if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                    title = WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.coa.accCode") +","+"Amount"+ " ("+WtfGlobal.getCurrencyName()+"),"+"Amount"+ " ("+WtfGlobal.getCurrencyName()+")";
                } else{
                    title = WtfGlobal.getLocaleText("acc.report.2")+","+"Amount"+ " ("+WtfGlobal.getCurrencyName()+"),"+"Amount"+ " ("+WtfGlobal.getCurrencyName()+")";//WtfGlobal.getLocaleText("acc.report.3")+","+WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.4");
                }
            }
            
            exportUrl = getExportUrl(28, this.consolidateFlag);
            if(this.statementType=="TradingAndProfitLoss"){
            if(!this.isCompareGlobal&&!this.isSelectedCurrencyDiff){
                    if(this.periodView && Wtf.account.companyAccountPref.showaccountcodeinfinancialreport){
                        header = "accountname,accountcode,openingamount,periodamount,endingamount";
                    } else if (this.periodView) {
                        header = "accountname,openingamount,periodamount,endingamount";
                    } else if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                        header = "accountname,accountcode,amount";
                    } else {
                        header = "accountname,amount";
                    }
            }else{
                    if (this.isSelectedCurrencyDiff) {
                        if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                            header = "accountname,accountcode,amount,amountInSelectedCurrency";
                        } else {
                            header = "accountname,amount,amountInSelectedCurrency";
                        }

                    } else if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                        header = "accountname,accountcode,amount,preamount";
                    } else {
                        header = "accountname,amount,preamount";
                    }
            }
                fileName = WtfGlobal.getLocaleText("acc.P&L.tabTitle");
                reportName = WtfGlobal.getLocaleText("acc.P&L.tabTitle");
            } else if (this.statementType=="CostCenter"){
                fileName = WtfGlobal.getLocaleText("acc.ccReport.tabTitle");
                reportName = WtfGlobal.getLocaleText("acc.ccReport.tabTitle");
            }
            extraParams += "&costcenter="+this.costCenter.getValue();
            extraParams += "&reportView="+this.statementType;
            extraParams += "&filterCurrency="+this.currencyFilter.getValue();
            extraParams += "&externalcurrencyrate="+((currencyFilterRec!=null&&currencyFilterRec!=undefined)?currencyFilterRec.data.exchangerate:0); 
            extraParams += "&isCompareGlobal="+((this.isCompareGlobal!=null&&this.isCompareGlobal!=undefined)?this.isCompareGlobal:false); 
            extraParams += "&isSelectedCurrencyDiff="+((this.isSelectedCurrencyDiff!=undefined&&this.isSelectedCurrencyDiff!=null)?this.isSelectedCurrencyDiff:false); 

             searchJson= this.searchJson!=undefined?this.searchJson:"";
            filterConjuctionCriteria= this.filterConjuctionCrit;
        }

        var align="";
        if(this.periodView && Wtf.account.companyAccountPref.showaccountcodeinfinancialreport){
            align= "none,none,withoutcurrency,withoutcurrency,withoutcurrency";//,none,currency";
        } else if(this.periodView){
            align = "none,withoutcurrency,withoutcurrency,withoutcurrency";//,none,currency";
        } else if(Wtf.account.companyAccountPref.showaccountcodeinfinancialreport){
            align = "none,none,withoutcurrency,withoutcurrency";//,none,currency";
        } else{
            align= "none,withoutcurrency,withoutcurrency";//,none,currency";
        }
        if(this.consolidateFlag) {
            var url = exportUrl+"&filename="+encodeURIComponent(fileName)+extraParams+"&filetype="+type+"&stdate="+this.sdate+"&enddate="+this.edate+"&nondeleted="+true+"&accountid="+"&searchJson="+searchJson+"&filterConjuctionCriteria="+filterConjuctionCriteria
                            +"&header="+header+"&title="+encodeURIComponent(title)+"&name="+encodeURIComponent(reportName)+"&width=150&get=27&align="+align+"&toggle="+this.toggle+"&singleGrid="+true+"&templatecode="+templatecode+"&periodView="+this.periodView+"&excludePreviousYear="+ this.excludeTypeCmb.getValue();
        } else {
            url = exportUrl+"?filename="+encodeURIComponent(fileName)+extraParams+"&filetype="+type+"&stdate="+this.sdate+"&enddate="+this.edate+"&nondeleted="+true+"&accountid="+"&searchJson="+searchJson+"&filterConjuctionCriteria="+filterConjuctionCriteria
                            +"&header="+header+"&title="+encodeURIComponent(title)+"&name="+encodeURIComponent(reportName)+"&width=150&get=27&align="+align+"&toggle="+this.toggle+"&singleGrid="+true+"&templatecode="+templatecode+"&stpredate="+this.presdateCompare+"&endpredate="+this.preedateCompare+"&periodView="+this.periodView+"&excludePreviousYear="+ this.excludeTypeCmb.getValue();
        }

        if(type == "print") {
           url+="&generatedOnTime="+WtfGlobal.getGeneratedOnTimestamp();
            window.open(url, "mywindow","menubar=1,resizable=1,scrollbars=1");
        } else {
            Wtf.get('downloadframe').dom.src  = url;
        }
//        Wtf.get('downloadframe').dom.src = url;
    },
     collapseGrids : function() {
        for(var i=0; i< this.lGrid.getStore().data.length; i++){
            this.lGrid.collapseRow(this.lGrid.getView().getRow(i));

        }
    
    },        
    getRowClass:function(record,grid){
        var colorCss="";
        switch(record.data["fmt"]){
            case "T":colorCss=" grey-background";break;
            case "B":colorCss=" red-background";break;
            case "H":colorCss=" header-background";break;
            case "A":colorCss=" darkyellow-background";break;
        }
        return grid.getRowClass()+colorCss;
    },

    formatAccountName:function(val,m,rec,i,j,s){
        var fmtVal=val;
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

    formatMoney:function(val,m,rec,i,j,s){
        var fmtVal=WtfGlobal.currencyRenderer(val);
        if(rec.data['fmt']){
            fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
        }
        else if(rec.data["level"]==0&&rec.data["accountname"]!="")
            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
        return fmtVal;
    },
    formatMoneyInSelectedCurr:function(val,m,rec,i,j,s){
        var currencyRec;
        var symbol;
        if(this.currencyStoreCMB!=undefined){
//            currencyRec=this.currencyStoreCMB.getAt(this.currencyStoreCMB.find('currencyid',this.currencyFilter.getValue())!=-1?this.currencyStoreCMB.find('currencyid',this.currencyFilter.getValue()):WtfGlobal.getCurrencyID());
            currencyRec=WtfGlobal.searchRecord(this.currencyStoreCMB,this.currencyFilter.getValue(),'currencyid');
            symbol=currencyRec!=null?currencyRec.data.symbol:WtfGlobal.getCurrencySymbol();
        }else{
            symbol=WtfGlobal.getCurrencySymbol();
        }
        var v=parseFloat(val);
        if(isNaN(v)){
            v= val;
        } else {
            v= WtfGlobal.conventInDecimal(v,symbol);
        }
        var fmtVal='<div class="currency">'+v+'</div>';
        if(rec.data['fmt']){
            fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
        }
        else if(rec.data["level"]==0&&rec.data["accountname"]!="")
            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
        return fmtVal;
    },

    fetchStatement:function(isCompare){
       this.isCompareGlobal=(isCompare==true)?isCompare:false;
       this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
       WtfComMsgBox(29,4,true); //Show loading mask
       this.sDate=this.startDate.getValue();
       this.eDate=this.endDate.getValue();

       if(this.sDate=="" || this.eDate=="") {
           WtfComMsgBox(42,2);
           return;
       }

       this.sdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
       this.edate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
         if(this.sDate>this.eDate){
            WtfComMsgBox(1,2);
            return;
        }
        var templatecode = '-1';
        var index = this.templateStore.find('id', this.templateCombo.getValue());
        if(index > -1){
            templatecode = this.templateStore.getAt(index).data.templatecode;
        }
//        var currencyRec=this.currencyStoreCMB.getAt(this.currencyStoreCMB.find('currencyid',this.currencyFilter.getValue())!=-1?this.currencyStoreCMB.find('currencyid',this.currencyFilter.getValue()):WtfGlobal.getCurrencyID());
        var currencyRec=WtfGlobal.searchRecord(this.currencyStoreCMB,this.currencyFilter.getValue(),'currencyid');
        this.presdateCompare = "";
        this.preedateCompare = "";
        this.presdate = WtfGlobal.convertToGenericStartDate(this.startPreDate.getValue());
        this.preedate = WtfGlobal.convertToGenericEndDate(this.endPreDate.getValue());
        if (isCompare == true) {
            this.presdateCompare = this.presdate;
            this.preedateCompare = this.preedate;
            if ((this.statementType == 'BalanceSheet' || this.statementType=="TradingAndProfitLoss")) {
                var leftGrid = this.lGrid.colModel.config[5];
                var rightGrid = this.rGrid.colModel.config[5];
                var leftGrid1 = this.lGrid.colModel.config[4];
                var rightGrid1 = this.rGrid.colModel.config[4];
                var leftGridSelectedCurr = this.lGrid.colModel.config[6];
                var openleft = this.lGrid.colModel.config[2];
                var openright = this.rGrid.colModel.config[2];
                var periodleft = this.lGrid.colModel.config[3];
                var periodright = this.rGrid.colModel.config[3];
                if (rightGrid != undefined){
                    rightGrid.hidden = false;
                    rightGrid1.hidden=false;
                    rightGrid1.dataIndex='amount';
                    openright.hidden=true;
                    periodright.hidden=true;
                    rightGrid.hidden = false;   
                    rightGrid.header="<div align=right><b>"+(this.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.Amount(asset)"):WtfGlobal.getLocaleText("acc.P&L.Amount(Credit)"))+"("+WtfGlobal.getCurrencyName()+")</b><br/>("+WtfGlobal.onlyDateRendererForGridHeader(this.startPreDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endPreDate.getValue())+")</div>";
                    rightGrid1.header="<div align=right><b>"+(this.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.Amount(asset)"):WtfGlobal.getLocaleText("acc.P&L.Amount(Credit)"))+"("+WtfGlobal.getCurrencyName()+")</b><br/>("+WtfGlobal.onlyDateRendererForGridHeader(this.startDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endDate.getValue())+")</div>";
                }
                if (leftGrid != undefined){
                    openleft.hidden= true;
                    periodleft.hidden = true;
                    leftGridSelectedCurr.hidden = true;
                    leftGrid.hidden = false;
                    leftGrid1.hidden = false;
                    leftGrid1.dataIndex='amount';
                    leftGrid.hidden = false;                        
                    leftGrid.header="<div align=right><b>"+"Amount("+WtfGlobal.getCurrencyName()+")</b></br>("+WtfGlobal.onlyDateRendererForGridHeader(this.startPreDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endPreDate.getValue())+")</div>";
                    leftGrid1.header="<div align=right><b>"+"Amount("+WtfGlobal.getCurrencyName()+")</b></br>("+WtfGlobal.onlyDateRendererForGridHeader(this.startDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endDate.getValue())+")</div>";
                    leftGridSelectedCurr.header="<div align=right><b>"+"Amount("+WtfGlobal.getCurrencyName()+")</b></br>("+WtfGlobal.onlyDateRendererForGridHeader(this.startDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endDate.getValue())+")</div>";
                }
                this.lGrid.getView().refresh(true);
//                this.rGrid.getView().refresh(true);
            }
        } else {
            if (!this.firstTime && (this.statementType == 'BalanceSheet' || this.statementType=="TradingAndProfitLoss")) {
                var leftGrid = this.lGrid.colModel.config[5];
                var rightGrid = this.rGrid.colModel.config[5];
                var leftGrid1 = this.lGrid.colModel.config[4];
                var rightGrid1 = this.rGrid.colModel.config[4];
                var leftGridSelectedCurr = this.lGrid.colModel.config[6];
                var openleft = this.lGrid.colModel.config[2];
                var openright = this.rGrid.colModel.config[2];
                var periodleft = this.lGrid.colModel.config[3];
                var periodright = this.rGrid.colModel.config[3];
                if (rightGrid != undefined){
                    if(this.periodView){
                        openright.hidden=false;
                        periodright.hidden=false;
                        rightGrid1.dataIndex='endingamount';
                        rightGrid.hidden = true;
                    }else{
                        rightGrid.hidden = true;
                    }
                    rightGrid1.header="<div align=right><b>"+"Amount"+" ("+WtfGlobal.getCurrencyName()+")</b></div>";
                }
                if (leftGrid != undefined){
                    if(this.currencyFilter.getValue()==WtfGlobal.getCurrencyID()){
                        leftGridSelectedCurr.hidden = true;
                        leftGrid1.hidden=false;
                    }else{
                        leftGridSelectedCurr.hidden = false;
                        leftGrid1.hidden=true;
                    }
                    if(this.periodView){
                        openleft.hidden=false;
                        periodleft.hidden=false;
                        leftGrid.hidden = true;
                    }else{
                        openleft.hidden=true;
                        periodleft.hidden=true;          
                        leftGrid.hidden = true;
                    }
                    leftGrid1.header="<div align=right><b>"+"Amount"+" ("+WtfGlobal.getCurrencyName()+")</b></div>";
                    leftGridSelectedCurr.header="<div align=right><b>"+"Amount ("+((currencyRec!=null&&currencyRec.data.currencyname!=undefined)?currencyRec.data.currencyname:WtfGlobal.getCurrencyName())+")</b></div>";
                }
                this.lGrid.getView().refresh(true);
//                this.rGrid.getView().refresh(true);
            }
        }
        var params={
         //   fordate:WtfGlobal.convertToGenericDate(this.forDate.getValue().add(Date.DAY,1)),
            stdate:this.sdate,
            enddate:this.edate,
            stpredate:this.presdateCompare,
            endpredate:this.preedateCompare,
            templatecode:templatecode,
            periodView:this.periodView
        }        
         params.searchJson= this.searchJson!=undefined?this.searchJson:"",
         params.filterConjuctionCriteria= this.filterConjuctionCrit
        if(this.consolidateFlag && this.consolidateFlag==true) {            
            params.companyids=companyids;
            params.gcurrencyid=gcurrencyid;
            params.userid=loginid;
        }

        this.ajxUrl = Wtf.req.account+'CompanyManager.jsp';
        if(this.statementType=="Trading") {
            params.mode=63;
            params.nondeleted=true;
            this.ajxUrl = this.consolidateFlag?"ACCCombineReports/getTradingMerged.do":"ACCReports/getTrading.do";
        }
        if(this.statementType=="ProfitAndLoss") {
            params.mode=64;
            params.nondeleted=true;
            this.ajxUrl = this.consolidateFlag?"ACCCombineReports/getProfitLossMerged.do":"ACCReports/getProfitLoss.do";
        }
        if(this.statementType=="TradingAndProfitLoss" || this.statementType=="CostCenter") {
            var currencyFilterRec=WtfGlobal.searchRecord(this.currencyStoreCMB,this.currencyFilter.getValue(),'currencyid');
            params.mode=65;
            params.nondeleted=true;
            params.costcenter = this.costCenter.getValue();
            params.reportView = this.statementType;
            params.filterCurrency = this.currencyFilter.getValue();
            params.excludePreviousYear = this.excludeTypeCmb.getValue();
            params.externalcurrencyrate = (currencyFilterRec!=null&&currencyFilterRec!=undefined)?currencyFilterRec.data.exchangerate:0; 
            if(this.statementType != "CostCenter")
              params.singleGrid = true;
            this.ajxUrl = this.consolidateFlag?"ACCCombineReports/getTradingAndProfitLossMerged.do":"ACCReports/getTradingAndProfitLoss.do";
        }
        if(this.statementType=="BalanceSheet") {
            params.mode=66;
            params.nondeleted=true;
            this.ajxUrl = this.consolidateFlag?"ACCCombineReports/getBalanceSheetMerged.do":"ACCReports/getBalanceSheet.do";
        }
//        this.expButton.setParams({
////                    accountid:this.accountID,
//                    stdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
//                    enddate:WtfGlobal.convertToGenericDate(this.endDate.getValue())
//            });
        WtfGlobal.setAjaxTimeOutFor30Minutes();
        Wtf.Ajax.requestEx({url:this.ajxUrl,params:params}, this, this.successCallback, this.failureCallback);
    },

    successCallback:function(response){
        WtfGlobal.resetAjaxTimeOut();
        if(response.success){
            this.total=response.data.total;
            if (response.data.pretotal != undefined) {
                this.total[2] = response.data.pretotal[0];
                this.total[3] = response.data.pretotal[0];
            } 
            this.lGrid.store.loadData(response.data);
            this.rGrid.store.loadData(response.data);
            this.doLayout();
            if((this.statementType!="TradingAndProfitLoss" && this.total && this.total[0]==0 && this.total[1]==0)
                ||(this.statementType=="TradingAndProfitLoss" && this.lGrid.store.getCount()<=3 && this.rGrid.store.getCount()<=3)){
                    if(this.expButton)this.expButton.disable();
                    if(this.printbtn)this.printbtn.disable();
                }else{
                    if(this.expButton)this.expButton.enable();
                    if(this.printbtn)this.printbtn.enable();
            }
            
            if (this.firstTime && !this.isCompareGlobal && (this.statementType == 'BalanceSheet'|| this.statementType=="TradingAndProfitLoss")) {
                var leftGrid = this.lGrid.colModel.config[5];
                var rightGrid = this.rGrid.colModel.config[5];
                 var leftGridSelectedCurr = this.lGrid.colModel.config[6];
              if (rightGrid != undefined)
                    rightGrid.hidden = true;
                if (leftGrid != undefined)
                    leftGrid.hidden = true;
                    if(this.currencyFilter.getValue()==WtfGlobal.getCurrencyID()){
                        leftGridSelectedCurr.hidden = true;
                    }else{
                        leftGridSelectedCurr.hidden = false;
                    }
                    this.lGrid.getView().refresh(true);
//                this.rGrid.getView().refresh(true);
            }
            this.firstTime = false;
            this.collapseGrids();
            
        }
        this.hideLoading();
    },

    failureCallback:function(response){
        WtfGlobal.resetAjaxTimeOut();
        this.hideLoading();
    },

    hideLoading:function(){
        Wtf.MessageBox.hide();
    },

    getDates:function(start, sdateSavedSearch){ //this Works for both Start date and end date
        if(sdateSavedSearch != undefined){            
            return new Date(sdateSavedSearch);
        }
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

   getPreDates:function(start){      
        return start.add(Date.YEAR, -1);
    },
   fetchCompareStatement:function(){
      this.currencyFilter.setValue(WtfGlobal.getCurrencyID());
      this.fetchStatement(true);
    },        
    showLastRec:function(pos){
        return WtfGlobal.currencySummaryRenderer(this.total[pos]);
    },

    swapGrids:function(){			// The summaryRenderer doesnt swap when the grids swap. Only the header and grid data swaps.
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
            this.expandCollapseGrid(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }

    },
             showAdvanceSearch: function() {
        showAdvanceSearch(this, this.searchparam, this.filterAppend);
    },
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
        
    },
    filterStore: function(json, filterConjuctionCriteria) {
        /**
         * ERP-33751 - Start Date Required for saved Search
         */        
        this.objsearchComponent.advGrid.sdate = this.startDate.getValue(); 
        this.objsearchComponent.advGrid.edate = this.endDate.getValue();
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.fetchStatement();
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
       this.fetchStatement();
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();


    },    
     storeLoad: function() {
       this.fetchStatement();
        this.doLayout();


    }     
});


/*Custom layout component for Profit & Loss*/

Wtf.account.TradingCustomLayout=function(config){
    this.total=[0,0];
    this.leftHeading=(config.statementType=="BalanceSheet"?"Liability":"Debit");
    this.uPermType=Wtf.UPerm.fstatement;
    this.permType=Wtf.Perm.fstatement;
    this.isOnlyPeriodBalances = config.isOnlyPeriodBalances;
    this.isOnlyEndingBalances = config.isOnlyEndingBalances;
    this.exportPermType=(config.statementType=="BalanceSheet"?this.permType.exportdatabsheet:this.permType.exportdatatradingpnl);
    this.printPermType=(config.statementType=="BalanceSheet"?this.permType.printbsheet:this.permType.exportdatatradingpnl)
    this.toggle=0;
    this.isCompare=false;
    this.summaryL = new Wtf.ux.grid.GridSummary();
    this.FinalStatementRec = new Wtf.data.Record.create([
        {name: 'accountname'},
        {name: 'accountcode'},
        {name: 'accountid'},
        {name: 'openingamount'},
        {name: 'periodamount'},
        {name: 'amount'},
        {name: 'preopeningamount'},
        {name: 'preperiodamount'},
        {name: 'preamount'},
        {name: 'accountflag'},
        {name: 'isdebit',type:'boolean'},
        {name: 'level'},
        {name: 'fmt'},
        {name: 'leaf'}
    ]);

    this.statementType=config.statementType||"Trading";

    this.lStroe = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "left"							// Assets on Left side in Balance Sheet          Neeraj
        },this.FinalStatementRec),
        baseParams:{
            nondeleted:true
        },
        url: Wtf.req.account+'CompanyManager.jsp'
    });
    this.lGrid = new Wtf.grid.HirarchicalGridPanel({
        //plugins:[this.summaryL],
        //stripeRows :true,
        autoScroll:true,
        store: this.lStroe,
        hirarchyColNumber:0,
        columns: [{
            header:'<b>'+WtfGlobal.getLocaleText("acc.balanceSheet.particulars")+'</b>',
            dataIndex:'accountname',
            renderer:this.formatAccountName,
            width:150,
            sortable: true,
            summaryRenderer:function(){return WtfGlobal.summaryRenderer(WtfGlobal.getLocaleText("acc.common.total"));}.createDelegate(this)
        },{
            header:'<b>'+WtfGlobal.getLocaleText("acc.coa.accCode")+'</b>',
            dataIndex:'accountcode',
            sortable: true,
            hidden:!Wtf.account.companyAccountPref.showaccountcodeinfinancialreport,
            renderer:this.formatAccountName,
            width:50,
            align: 'center'
        },{
            header:"<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.OpeningAmount")+" ("+WtfGlobal.getCurrencyName()+")</b></div>",
            dataIndex:'openingamount',
            hidecurrency : true,
            hideable: !this.isOnlyEndingBalances,    // after hidden true still user can show hided columns through grid columns dropdown option
            hidden:this.isOnlyPeriodBalances || this.isOnlyEndingBalances || Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA,
            renderer:this.formatMoney
//            summaryRenderer:this.showLastRec.createDelegate(this,[0])
        },{
            header:"<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.PeriodAmount")+" ("+WtfGlobal.getCurrencyName()+")</b></div>",
            dataIndex:'periodamount',
            hidecurrency : true,
            hideable: !this.isOnlyEndingBalances,    // fix for - after hidden true still user can show hided columns through grid columns dropdown option
            hidden:Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA || this.isOnlyEndingBalances,
            renderer:this.formatMoney
//            summaryRenderer:this.showLastRec.createDelegate(this,[0])
        },{
            header:Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA?"<div align=right><b>"+ WtfGlobal.getLocaleText("acc.field.currrntYearEndingAmount")+" ("+WtfGlobal.getCurrencyName()+")</b></div>":"<div align=right><b>"+ WtfGlobal.getLocaleText("acc.field.EndingAmount")+" ("+WtfGlobal.getCurrencyName()+")</b></div>",
            dataIndex:'amount',
            hidecurrency : true,
            hidden: this.isOnlyPeriodBalances,
            renderer:this.formatMoney,
            sortable: true,
            summaryRenderer:this.showLastRec.createDelegate(this,[0])
        },{
            header:"<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.OpeningAmount")+" ("+WtfGlobal.getCurrencyName()+") </b></div>",
            dataIndex:'preopeningamount',
            hidecurrency : true,
            hideable: !this.isOnlyEndingBalances,    //fix for - after hidden true still user can show hided columns through grid columns dropdown option
            hidden:(config.statementType=="CashFlowStatement" || Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA?true:false) || this.isOnlyEndingBalances || this.isOnlyPeriodBalances,
//            hidden:((config.statementType!="TradingAndProfitLoss") || config.consolidateFlag) ||this.isOnlyPeriodBalances,
            renderer:this.formatMoney
//            summaryRenderer:this.showLastRec.createDelegate(this,[2])
        },{
            header:"<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.PeriodAmount")+" ("+WtfGlobal.getCurrencyName()+") </b></div>",
            dataIndex:'preperiodamount',
            hidecurrency : true,
            hideable: !this.isOnlyEndingBalances,    // fix for - after hidden true still user can show hided columns through grid columns dropdown option
            hidden:(config.statementType=="CashFlowStatement" || Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA?true:false) || this.isOnlyEndingBalances || this.isOnlyPeriodBalances,
//            hidden:((config.statementType!="TradingAndProfitLoss") || config.consolidateFlag) ||this.isOnlyPeriodBalances,
            renderer:this.formatMoney
//            summaryRenderer:this.showLastRec.createDelegate(this,[2])
        },{
            header:Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA?"<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.previousYearEndingAmount")+" ("+WtfGlobal.getCurrencyName()+") </b></div>":"<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.EndingAmount")+" ("+WtfGlobal.getCurrencyName()+") </b></div>",
            dataIndex:'preamount',
            hidecurrency : true,
            sortable: true,
            hideable: !this.isOnlyEndingBalances,  // added for - after hidden true still user can show hided columns through grid columns dropdown option
            hidden:(config.statementType=="CashFlowStatement"?true:false) || this.isOnlyEndingBalances || this.isOnlyPeriodBalances,
//            hidden:((config.statementType!="TradingAndProfitLoss") || config.consolidateFlag) ||this.isOnlyPeriodBalances,
            renderer:this.formatMoney,
            summaryRenderer:this.showLastRec.createDelegate(this,[2])
        }],
        border : false,
        loadMask : true,
        viewConfig: {
            forceFit:true,
            emptyText:'<div class="emptyGridText">' + WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText('acc.common.norec')  + ' <br>' + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn"))+'</div>',//Message for user after opening report to fectch data
            deferEmptyText: false
            
        }
    });
    this.lGrid.on("render", WtfGlobal.autoApplyHeaderQtip);
    this.lGrid.on('rowclick',this.onRowClickLGrid, this);
    this.lGrid.on('render',function(){
        this.lGrid.getView().getRowClass=this.getRowClass.createDelegate(this,[this.lGrid],1);
        this.lGrid.getView().applyEmptyText();// Show empty text on grid after opening report
    },this);

    this.lGrid.getStore().on("load", function(){
        for(var i=0; i< this.lGrid.getStore().data.length; i++){
            this.lGrid.collapseRow(this.lGrid.getView().getRow(i));

        }
    }, this);

this.firstTime=true;
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate',
        format:WtfGlobal.getOnlyDateFormat(),
        //readOnly:true,
//        hidden:config.statementType=='BalanceSheet',
        value:this.getDates(true)
    });
     this.startPreDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stpredate',
        format:WtfGlobal.getOnlyDateFormat(),
       // readOnly:true,
        //width : 70,
        hidden: this.isOnlyPeriodBalances,
//        hidden:config.statementType=='BalanceSheet',
        value:this.getPreDates(this.startDate.getValue())
    });    
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),
        format:WtfGlobal.getOnlyDateFormat(),
       // readOnly:true,
        name:'enddate',
        value:this.getDates(false)
    });
    this.monthOrYearToDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),
        format:WtfGlobal.getOnlyDateFormat(),
        name:'yearToDate',
        hidden:true,
        value:new Date()
    });
    this.compareDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),
        format:WtfGlobal.getOnlyDateFormat(),
        name:'monthToDate',
        hidden: true,
        value: new Date()
    });
     this.endPreDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),
        format:WtfGlobal.getOnlyDateFormat(),
       // readOnly:true,
        //width : 70,
        name:'endpredate',
        hidden: this.isOnlyPeriodBalances,
        value:this.getPreDates(this.endDate.getValue())
    });
    this.startDate.on("change",function(){    
     this.startPreDate.setValue(this.startDate.getValue().add(Date.YEAR, -1))
    },this);
    
    this.endDate.on("change",function(){    
     this.endPreDate.setValue(this.endDate.getValue().add(Date.YEAR, -1))
    },this);
    this.grid = this.lGrid;
    var mnuBtns=[];
    var csvbtn=new Wtf.Action({
        iconCls:'pwnd '+'exportcsv',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToCSVTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToCSV")+"</span>",
        scope: this,
        handler:function(){
            this.exportWithTemplate()
        }
    });
    mnuBtns.push(csvbtn);
     var xlsbtn=new Wtf.Action({
        iconCls:'pwnd '+'exportcsv',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToXLSTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"</span>",
        scope: this,
        handler:function(){
            this.exportWithTemplate("xls")
        }
    });
    mnuBtns.push(xlsbtn);
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
        hidden: this.isOnlyPeriodBalances,
        selectOnFocus:true,
        forceSelection: true,
        emptyText:WtfGlobal.getLocaleText("acc.rem.9")  //,"Select a Cost Center"
    });
    
    var btnArr=[];
    var btnArr1=[];

    this.dateFilterStore = new Wtf.data.SimpleStore({
        fields: [{
                name: 'name'
            }, {
                name: 'id'
            }],
        data: [['Custom Dates', '0'], [WtfGlobal.getLocaleText("acc.forecast.tabTitle.monthtodate"), "1"], [WtfGlobal.getLocaleText("acc.forecast.tabTitle.yeartodate"), "2"], ]
    });
    this.dateFilterCombo = new Wtf.form.ComboBox({
        triggerAction: 'all',
        mode: 'local',
        valueField: 'id',
        displayField: 'name',
        store: this.dateFilterStore,
        fieldLabel: "Filter Type",
        allowBlank: false,
        width: 120,
        typeAhead: true,
        forceSelection: true,
        value: "0"
    });
    this.toDate= new Wtf.Toolbar.TextItem("");// WtfGlobal.getLocaleText("acc.reval.toDate")
    
    this.compareWith= new Wtf.Toolbar.TextItem("");
    this.compareWith.getEl().innerHTML = WtfGlobal.getLocaleText("acc.common.compareWith"); // Compare With
    
    
    this.fromDateText = new Wtf.Toolbar.TextItem("");
    this.fromDateText.getEl().innerHTML =WtfGlobal.getLocaleText("acc.common.from"); // From
    this.toDateText= new Wtf.Toolbar.TextItem("");
    this.toDateText.getEl().innerHTML =WtfGlobal.getLocaleText("acc.common.to"); // To
    this.fromCompareDateText = new Wtf.Toolbar.TextItem("");
    this.fromCompareDateText.getEl().innerHTML =WtfGlobal.getLocaleText("acc.common.from"); // From
    this.toCompareDateText= new Wtf.Toolbar.TextItem("");
    this.toCompareDateText.getEl().innerHTML =WtfGlobal.getLocaleText("acc.common.to"); // To
    if (this.statementType == "BalanceSheet" || this.statementType == "TradingAndProfitLoss") { // year to date and month to date filters to be applied for PL & BS Custom Layout
        this.dateFilterCombo.on("select", this.dateFilterChanged, this);
        if(!this.isOnlyEndingBalances){
            btnArr.push("Filter Type", this.dateFilterCombo);
        }
        btnArr.push(this.toDate,this.monthOrYearToDate);
//        this.monthOrYearToDate.on("change",this.setDateFilters.createDelegate(this,[1]),this);
//        this.compareDate.on("change",this.setDateFilters.createDelegate(this,[2]),this);
        this.compareDate.on("change",function(){
            if (this.dateFilterCombo.getValue() == "1") {
                var compareDate = this.compareDate.getValue();
                if (!this.isOnlyPeriodBalances) {
                    var preStDate = new Date(compareDate.getFullYear(), compareDate.getMonth(), 1);
                    var preEndDate = new Date(compareDate.getFullYear(), compareDate.getMonth(), compareDate.getDate());
                    this.startPreDate.setValue(preStDate);
                    this.endPreDate.setValue(preEndDate);
                }
            } else if (this.dateFilterCombo.getValue() == "2") {
                var compareDate = this.compareDate.getValue();
                if (!this.isOnlyPeriodBalances) {
                    var preStDate = new Date(compareDate.getFullYear()-1, compareDate.getMonth(), compareDate.getDate());
                    this.startPreDate.setValue(preStDate);
                    this.endPreDate.setValue(compareDate);
                }
            }
        },this);
        this.monthOrYearToDate.on("change", function() {
            if (this.dateFilterCombo.getValue() == "1") {
                this.setDateFilters(1);
            } else if (this.dateFilterCombo.getValue() == "2") {
                this.setDateFilters(2);
            }
        }, this);
    } 
    
    if(this.isOnlyEndingBalances){
        this.toDateText.getEl().innerHTML =WtfGlobal.getLocaleText("acc.common.asOf"); // As of
        btnArr.push(this.toDateText, this.endDate);
    } else{
        btnArr.push(this.fromDateText, this.startDate,
            this.toDateText, this.endDate
        );
    }
    
    if((this.statementType=="TradingAndProfitLoss" || this.statementType=="CostCenter") && !this.isOnlyPeriodBalances){
        btnArr1.push(WtfGlobal.getLocaleText("acc.common.costCenter"),this.costCenter);
    }

     this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: config.moduleid,
        advSearch: false,
        reportid: config.reportid,
        templateid: config.templateid,
        templatetitle:config.templatetitle
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
//        id: 'advanced3', // In use, Do not delete
        scope: this,
        hidden: this.isOnlyPeriodBalances,
//        hidden:(this.moduleid==undefined)?true:false,        
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    var comboReader = new Wtf.data.Record.create([
    {
        name: 'id',
        type: 'string'
    },
    {
        name: 'name',
        type: 'string'
    },
    {
        name: 'templatecode',
        type: 'string'
    }
    ]);
   
    
    this.templateStore = new Wtf.data.Store({
        url :'ACCAccount/getPnLTemplates.do',
        root: 'data',
        remoteSort:true,
        reader: new Wtf.data.KwlJsonReader({
            root: 'data'
        },comboReader),
        baseParams:{
            isdropdown:true
        }
    });
    
    this.templateStore.on("load", function(){
        this.templateStore.insert(0, new Wtf.data.Record({
            'id' : '-1', 
            'name' : 'None',
            'templatecode' : "" 
        }));
        this.templateCombo.setValue('-1');
    }, this);
    this.templateStore.load();
    this.templateCombo= new Wtf.form.ComboBox({
        fieldLabel:(this.isCustomer?WtfGlobal.getLocaleText("acc.het.101"):WtfGlobal.getLocaleText("acc.field.DebitAccount*")),
        width:120,
        store: this.templateStore,
        valueField:'id',
        displayField:'name',
        mode: 'local',
        typeAhead: true,
        forceSelection: true,
        selectOnFocus:true,
        triggerAction:'all',
        scope:this
    });
    
//    btnArr.push('-','Custom Layout ', this.templateCombo);
   
        this.typeStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['All',false],['Exclude Previous Year Balance',true]]
        });
        
        this.excludeTypeCmb= new Wtf.form.ComboBox({
            labelSeparator:'',
            labelWidth:0,
            triggerAction:'all',
            mode: 'local',
            valueField:'value',
            displayField:'name',
            hidden: !(config.statementType=="TradingAndProfitLoss") || this.isOnlyPeriodBalances,
            store:this.typeStore,
            value:true,
            width:200,
            name:'excludePreviousYear',
            hiddenName:'excludePreviousYear'
        });
    
    btnArr1.push('-',this.excludeTypeCmb);
        
    btnArr.push('-',{
        xtype:'button',
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
        tooltip:WtfGlobal.getLocaleText("acc.common.fetchTT"),
        iconCls:'accountingbase fetch',
        tooltip:(config.statementType=='BalanceSheet'?WtfGlobal.getLocaleText("acc.cc.26"):WtfGlobal.getLocaleText("acc.cc.27")),
        scope:this,
        handler:this.fetchStatement
    });
    if ((config.statementType == "BalanceSheet" || config.statementType=="TradingAndProfitLoss")  && !config.consolidateFlagm &&!this.isOnlyPeriodBalances && !this.isOnlyEndingBalances) {
        btnArr.push('-', this.compareWith,
            this.fromCompareDateText, this.startPreDate,
            this.toCompareDateText, this.endPreDate,this.compareDate,{
                xtype:'button',
                text:WtfGlobal.getLocaleText("acc.common.compare"),  //'Fetch',
                iconCls:'accountingbase fetch',
                tooltip:WtfGlobal.getLocaleText("acc.cc.balanceSheet.compare.TT"),
                scope:this,
                hidden: this.isOnlyPeriodBalances,
                style:" margin-left: 5px;",
                handler:this.fetchCompareStatement
            });
    }
    
     btnArr.push('-',this.AdvanceSearchBtn);
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
        btnArr1.push(this.expButton=new Wtf.Button({
    //        iconCls:'pwnd '+'exportcsv',
            text:WtfGlobal.getLocaleText("acc.common.export"),  //'Export',
            iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export'),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details.',
//                    disabled :true,
//            hidden:true,
            scope: this,
            menu:mnuBtns
        }));
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){

        btnArr1.push(this.printbtn=new Wtf.Button({
            iconCls:'pwnd printButtonIcon',
            text :WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details.',
//            disabled :true,
//            hidden:true,
            scope: this,
            handler:function(){
                this.exportWithTemplate("print")
            }
        }));
    }

//   this.expButton=new Wtf.exportButton({
//        obj:this,
//        id:"exportReports"+config.helpmodeid,
//        tooltip :'Export report details',
//        params:{ stdate:WtfGlobal.convertToGenericDate(this.getDates(true)),
//                 enddate:WtfGlobal.convertToGenericDate(this.getDates(false))/*,
//                 accountid:this.accountID||config.accountID*/
//        },
//        menuItem:{csv:true,pdf:true,rowPdf:false},
//        get:27
//    });

    var reportsMenu = [];
    
    this.expandCollpseButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.Expand"),
            tooltip:WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
            iconCls:'pwnd toggleButtonIcon',
            scope:this,
            handler: function(){
                this.expandCollapseGrid(this.expandCollpseButton.getText());
            }
        });
    
    btnArr1.push('-', this.expandCollpseButton);
            
    this.onlyPeriodBalances = new Wtf.Action({
        text:WtfGlobal.getLocaleText("acc.onlyperiodBalances"),
        tooltip:WtfGlobal.getLocaleText("acc.onlyperiodBalances"),
        iconCls:'pwnd splitViewIcon',
        hidden: this.isOnlyPeriodBalances,
        scope:this,
        handler: function(){
            var id=this.templateid;
//            var templatetitle = config.statementType=='BalanceSheet'?"Balance Sheet":"Profit And Loss";
            var templatetitle = this.templatetitle; //Refer ERP-8701 issue
            var templatetype = config.statementType=='BalanceSheet'?1:(config.statementType=='CashFlowStatement'?3:0);
            callCustomLayoutWithPeriodOrEndingBalances(id,templatetitle,templatetype,true); // fourth param is for 'isOnlyPeriodBalances' flag in this function
        }
    });
    reportsMenu.push(this.onlyPeriodBalances);
    
    this.onlyEndingBalances = new Wtf.Action({
        text:WtfGlobal.getLocaleText("acc.onlyendingBalances"),
        tooltip:WtfGlobal.getLocaleText("acc.onlyendingBalances"),
        iconCls:'pwnd splitViewIcon',
        hidden: this.isOnlyEndingBalances,
        scope:this,
        handler: function(){
            var id=this.templateid;
            var templatetitle = this.templatetitle;
            var templatetype = config.statementType=='BalanceSheet'?1:(config.statementType=='CashFlowStatement'?3:0);
            callCustomLayoutWithPeriodOrEndingBalances(id,templatetitle,templatetype,false);
        }
    });
    reportsMenu.push(this.onlyEndingBalances);
    
    this.dimensionBasedPnL = new Wtf.Action({
        text:(this.statementType === "TradingAndProfitLoss")?WtfGlobal.getLocaleText("erp.DimensionBasedProfitAndLoss"):WtfGlobal.getLocaleText("erp.DimensionBasedBalanceSheet"),
        tooltip:(this.statementType === "TradingAndProfitLoss")?WtfGlobal.getLocaleText("erp.DimensionBasedProfitAndLoss"):WtfGlobal.getLocaleText("erp.DimensionBasedBalanceSheet"),
        iconCls:'pwnd splitViewIcon',
        hidden: this.isOnlyPeriodBalances,
        scope:this,
        handler: function(){
            if (this.statementType === "TradingAndProfitLoss") {
                callDimensionBasedProfitLoss(true, this.templateid);
            } else if (this.statementType === "BalanceSheet") {
                callDimensionBasedBalanceSheet(true, this.templateid);
            }
        }
    });
    if (this.statementType === "TradingAndProfitLoss" || this.statementType === "BalanceSheet") {
        reportsMenu.push(this.dimensionBasedPnL);
    }
    
    if (this.statementType === "TradingAndProfitLoss" || this.statementType === "BalanceSheet") {   //Add button for Dimension Based Monthly PL or BS Report
        this.monthlyCustomLayoutBttn = new Wtf.Action({
            text: this.statementType === "TradingAndProfitLoss" ? WtfGlobal.getLocaleText("acc.monthlyPnLWithBudgetCustomLayout") : WtfGlobal.getLocaleText("acc.MonthlyBalanceSheetTT.tabTitleTT"),
            tooltip: this.statementType === "TradingAndProfitLoss" ? WtfGlobal.getLocaleText("acc.monthlyPnLWithBudgetCustomLayout") : WtfGlobal.getLocaleText("acc.MonthlyBalanceSheetTT.tabTitleTT"),
            iconCls: 'pwnd splitViewIcon',
            hidden: this.isOnlyPeriodBalances,
            scope: this,
            handler: function () {
                var templateid = this.templateid;
                var templatetitle = this.templatetitle;
                var statementType = this.statementType;
                var reportid = (this.statementType === "TradingAndProfitLoss") ? Wtf.autoNum.profitAndLossMonthlyCustomLayout : Wtf.autoNum.balanceSheetMonthlyCustomLayout;
                callmonthlyCustomLayout(templateid, templatetitle, statementType, reportid);
            }
        });
        reportsMenu.push(this.monthlyCustomLayoutBttn);
    }
    if (this.statementType === "TradingAndProfitLoss") {    //Add button for Dimension Based Monthly PL Report
        this.monthlyCustomLayoutBttn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("erp.DimensionBasedMonthlyProfitAndLoss"),
            tooltip: WtfGlobal.getLocaleText("erp.DimensionBasedMonthlyProfitAndLossTT"),
            iconCls: 'pwnd splitViewIcon',
            hidden: this.isOnlyPeriodBalances,
            scope: this,
            handler: function () {
                var templateid = this.templateid;
                var templatetitle = this.templatetitle;
                var statementType = this.statementType;
                var reportid = Wtf.autoNum.dimensionBasedMonthlyPLCustomLayout;//reportid for Dimension Based Monthly PnL defined in WtfSettings.js
                callmonthlyCustomLayout(templateid, templatetitle, statementType, reportid);
            }
        });
        reportsMenu.push(this.monthlyCustomLayoutBttn);
    }
    
    if (this.statementType === "BalanceSheet" && Wtf.account.companyAccountPref.childCompaniesPresent==true) {   //Button For Custom Consolidation Balance Sheet
        this.consolidationBSBttn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.consolidation.consolidationbalancesheet"),
            tooltip: WtfGlobal.getLocaleText("acc.consolidation.consolidationbalancesheet"),
            iconCls: 'consolidationBalanceSheetIcon',
            hidden: this.isOnlyPeriodBalances,
            scope: this,
            handler: function () {
                var templateid = this.templateid;
                var templatetitle = this.templatetitle;
                var statementType = this.statementType;                
                callCustomConsolidationBalanceSheetReportTab(templateid,templatetitle,statementType);
            }
        });
        reportsMenu.push(this.consolidationBSBttn);
    }    
    
    if (this.statementType === "TradingAndProfitLoss" && Wtf.account.companyAccountPref.childCompaniesPresent==true) {   //Button For Custom Consolidation PNL Sheet
        this.consolidationPNLBttn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("Consolidation Profit & Loss Sheet"),
            tooltip: WtfGlobal.getLocaleText("Consolidation Profit & Loss Sheet"),
            iconCls: 'consolidationBalanceSheetIcon',
            hidden: this.isOnlyPeriodBalances,
            scope: this,
            handler: function () {
                var templateid = this.templateid;
                var templatetitle = this.templatetitle;
                var statementType = this.statementType;                
                callCustomConsolidationPNLReportTab(templateid,templatetitle,statementType);
            }
        });
        reportsMenu.push(this.consolidationPNLBttn);
    }
    
    
    this.ReportMenuBtn = new Wtf.Toolbar.Button({
        text:"Reports",
        tooltip: "Reports",
        iconCls: 'pwnd splitViewIcon',
        menu:reportsMenu
        
    });
    
  btnArr1.push('-',this.ReportMenuBtn);
    
    this.wrapperPanel = new Wtf.Panel({
        border:false,
        layout:"border",
        scope:this,
        items:[this.objsearchComponent,
            /*this.centerPanel = new Wtf.Panel({
                region:'center',
                layout:'fit',
                width:'49%',
                border:true,
                items:this.statementType=="BalanceSheet"?this.lGrid:this.rGrid
            }),*/
            this.westPanel = new Wtf.Panel({
                width:'80%',
                region:'center',
                layout:'fit',
                //width:'49%',
                border:false,
                //split:true,
                items:this.lGrid
            }),
        {layout:'fit',region:'west',width:'10%'},{layout:'fit',region:'east',width:'10%'}],
        tbar:btnArr1
    });

    Wtf.apply(this,{
        defaults:{border:false,bodyStyle:"background-color:white;"},
        saperate:true,
        statementType:"Trading",
        items:this.wrapperPanel,
        tbar:btnArr
    },config);

     Wtf.account.TradingCustomLayout.superclass.constructor.call(this,config);
     this.addEvents({
        'account':true
     });
     if(this.statementType!="CostCenter"){
//        this.fetchStatement();
     }
}

Wtf.extend( Wtf.account.TradingCustomLayout,Wtf.Panel,{
      onRowClickLGrid:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var accid=this.lGrid.getStore().getAt(i).data['accountid'];      
        var searchJson = (this.objsearchComponent.advGrid != undefined) ? this.objsearchComponent.advGrid.getJsonofStore() : "" ;
        this.fireEvent('account',searchJson,this.filterConjuctionCrit,accid,this.startDate.getValue(),this.endDate.getValue(),undefined,undefined,false);
    },
    setDateFilters: function(type) {
        if (type === 1) { // 1: Month To Date
            var monthToDate = this.monthOrYearToDate.getValue();
            var stdate = new Date(monthToDate.getFullYear(), monthToDate.getMonth(), 1);
            this.startDate.setValue(stdate);
            this.endDate.setValue(monthToDate);
            if (!this.isOnlyPeriodBalances) {
                var preStDate = new Date(stdate.getFullYear() - 1, stdate.getMonth(), 1);
                var preEndDate = new Date(monthToDate.getFullYear() - 1, monthToDate.getMonth(), monthToDate.getDate());
                this.startPreDate.setValue(preStDate);
                this.endPreDate.setValue(preEndDate);
                this.compareDate.setValue(preEndDate);
            }
        } else if (type === 2) { // 2: Year To Date
            if (Wtf.account.companyAccountPref.fyfrom) {
                var finYearDate = Wtf.account.companyAccountPref.fyfrom;
                var yearToDate = this.monthOrYearToDate.getValue();
                this.startDate.setValue(finYearDate);
                this.endDate.setValue(yearToDate);
                if (!this.isOnlyPeriodBalances) {
                    var preStDate = new Date(finYearDate.getFullYear() - 1, finYearDate.getMonth(), 1);
                    var preEndDate = new Date(yearToDate.getFullYear() - 1, yearToDate.getMonth(), yearToDate.getDate());
                    this.startPreDate.setValue(preStDate);
                    this.endPreDate.setValue(preEndDate);
                    this.compareDate.setValue(preEndDate);
                }
            }
        }
    },
    dateFilterChanged: function() {
        if (this.dateFilterCombo.getValue() == "0") { // 0: Custom Dates
            this.startDate.setVisible(true);
            this.endDate.setVisible(true);
            this.monthOrYearToDate.setVisible(false);
            this.compareDate.setVisible(false);
            // reset the filters
            this.startDate.reset();
            this.endDate.reset();
            this.startPreDate.reset();
            this.endPreDate.reset();
            // set enable
            this.startDate.setDisabled(false);
            this.endDate.setDisabled(false);
            // set visibility
            this.startDate.setVisible(true);
            this.endDate.setVisible(true);
            this.startPreDate.setVisible(true);
            this.endPreDate.setVisible(true);
            // label Value
            this.fromDateText.getEl().innerHTML = WtfGlobal.getLocaleText("acc.common.from"); // From
            this.toDateText.getEl().innerHTML = WtfGlobal.getLocaleText("acc.common.to"); // To
            this.fromCompareDateText.getEl().innerHTML = WtfGlobal.getLocaleText("acc.common.from"); // From
            this.toCompareDateText.getEl().innerHTML = WtfGlobal.getLocaleText("acc.common.to"); // To
            this.toDate.getEl().innerHTML = "";
        } else if (this.dateFilterCombo.getValue() == "1") { // 1: Month To Date
            this.monthOrYearToDate.setVisible(true);
            this.compareDate.setVisible(true);
            this.monthOrYearToDate.reset();
            // update the start date and previous year start date according to the month to date
            this.setDateFilters(1);
            // set visibility
            this.startDate.setVisible(false);
            this.endDate.setVisible(false);
            this.startPreDate.setVisible(false);
            this.endPreDate.setVisible(false);
            // label Value
            this.fromDateText.getEl().innerHTML = "";
            this.toDateText.getEl().innerHTML = "";
            this.fromCompareDateText.getEl().innerHTML = "";
            this.toCompareDateText.getEl().innerHTML = "";
            this.toDate.getEl().innerHTML = WtfGlobal.getLocaleText("acc.reval.toDate");
        } else if (this.dateFilterCombo.getValue() == "2") { // 2: Year To Date
            this.monthOrYearToDate.setVisible(true);
            this.compareDate.setVisible(true);
            this.compareDate.reset();
            // update the start date and previous year start date according to the year to date
            this.setDateFilters(2);
            // set visibility
            this.startDate.setVisible(false);
            this.endDate.setVisible(false);
            this.startPreDate.setVisible(false);
            this.endPreDate.setVisible(false);
            // label Value
            this.fromDateText.getEl().innerHTML = "";
            this.toDateText.getEl().innerHTML = "";
            this.fromCompareDateText.getEl().innerHTML = "";
            this.toCompareDateText.getEl().innerHTML = "";
            this.toDate.getEl().innerHTML = WtfGlobal.getLocaleText("acc.reval.toDate");
        }
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
        var templatecode = '-1';
        var filterConjuctionCriteria=""; //added for advance search parametres
        var searchJson="";
        var index = this.templateStore.find('id', this.templateCombo.getValue());
        if(index > -1){
            templatecode = this.templateStore.getAt(index).data.templatecode;
        }
        exportUrl = getExportUrl(Wtf.autoNum.CustomPnlBs, this.consolidateFlag);
        fileName = this.templatetitle;
        extraParams += "&costcenter="+this.costCenter.getValue();
        extraParams += "&reportView="+this.statementType;
        extraParams += "&periodView="+((this.isOnlyPeriodBalances == undefined || this.isOnlyPeriodBalances == null || this.isOnlyPeriodBalances == "") ? false : this.isOnlyPeriodBalances);
        get = 28;
        jsonGrid = "{data:[{'header':'laccountname','title':'Particulars','width':'150','align':''},"+
        "{'header':'lamount','title':'Amount(Debit)','width':'150','align':'currency'},"+
        "{'header':'raccountname','title':'Particulars','width':'150','align':''},"+
        "{'header':'ramount','title':'Amount(Credit)','width':'150','align':'currency'}]}";
        header =  fileName;
        searchJson= this.searchJson!=undefined?this.searchJson:"";
        filterConjuctionCriteria= this.filterConjuctionCrit;
    
        var isBalanceSheet = false;
        if(this.statementType=="BalanceSheet"){
            isBalanceSheet=true;
        }
        var configstr = "{%22landscape%22:%22true%22,%22pageBorder%22:%22true%22,%22gridBorder%22:%22true%22,%22title%22:%22Test%20Title%22,%22subtitles%22:%22%22,%22headNote%22:%22Test%20Header%22,%22showLogo%22:%22false%22,%22headDate%22:%22false%22,%22footDate%22:%22true%22,%22footPager%22:%22false%22,%22headPager%22:%22true%22,%22footNote%22:%22Test%20Footer%22,%22textColor%22:%22000000%22,%22bgColor%22:%22FFFFFF%22}"
        if(this.consolidateFlag) {
            var url = exportUrl+"&filename="+encodeURIComponent(fileName)+extraParams+"&config="+configstr+"&header"+header+"&filetype=pdf&stdate="+this.sdate+"&enddate="+this.edate+"&toggle="+this.toggle+"&searchJson="+searchJson+"&filterConjuctionCriteria="+filterConjuctionCriteria
            +"&isCompare="+this.isComapre+"&excludePreviousYear="+this.excludeTypeCmb.getValue()+"&get="+get+"&gridconfig="+encodeURIComponent(jsonGrid)+"&templatecode="+templatecode+"&templateid="+this.templateid+"&templateheadings="+encodeURIComponent(this.templateheadings)+"&isBalanceSheet="+isBalanceSheet;
        } else {
            url = exportUrl+"?filename="+encodeURIComponent(fileName)+extraParams+"&config="+configstr+"&header"+header+"&filetype=pdf&stdate="+this.sdate+"&enddate="+this.edate+"&toggle="+this.toggle+"&searchJson="+searchJson+"&filterConjuctionCriteria="+filterConjuctionCriteria
            +"&isCompare="+this.isComapre+"&excludePreviousYear="+this.excludeTypeCmb.getValue()+"&get="+get+"&gridconfig="+encodeURIComponent(jsonGrid)+"&templatecode="+templatecode+"&templateheadings="+encodeURIComponent(this.templateheadings)+"&stpredate="+this.presdate+"&endpredate="+this.preedate+"&templateid="+this.templateid+"&isBalanceSheet="+isBalanceSheet+"&isOnlyPeriodBalances="+this.isOnlyPeriodBalances+"&isOnlyEndingBalances="+this.isOnlyEndingBalances;
        }
        Wtf.get('downloadframe').dom.src = url;
    },
    
    setCostCenter: function(){
        this.costCenter.setValue("");//Select Default Cost Center as None
        Wtf.CostCenterStore.un("load", this.setCostCenter, this);
    },
    
    resetFilterAndFetchReport: function(costCenterId, startDate, endDate){
        this.costCenter.setValue(costCenterId);
        this.startDate.setValue(startDate?startDate:this.getDates(true));
        this.startPreDate.setValue(this.startDate.getValue());
        this.endPreDate.setValue(this.endDate.getValue());
        this.endDate.setValue(endDate?endDate:this.getDates(false));
        this.fetchStatement();
    },
    exportWithTemplate:function(type){
        type = type?type:"csv";
        var exportUrl;
        var fileName;
        var reportName;
        var header = [];//,raccountname,ramount";
        var title = [];
        var align = [];
        var extraParams = "";
        var templatecode = '-1';
        var filterConjuctionCriteria="";
        var searchJson="";
        var index = this.templateStore.find('id', this.templateCombo.getValue());
        if(index > -1){
            templatecode = this.templateStore.getAt(index).data.templatecode;
        }
//        title = WtfGlobal.getLocaleText("acc.report.2")+","+"Amount"+ " ("+WtfGlobal.getCurrencyName()+"),"+"Amount"+ " ("+WtfGlobal.getCurrencyName()+")";//WtfGlobal.getLocaleText("acc.report.3")+","+WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.4");
//        if(this.isOnlyPeriodBalances){
//            if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
//                title = WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.coa.accCode") + "," + WtfGlobal.getLocaleText("acc.field.PeriodAmount") + " (" + WtfGlobal.getCurrencyName() + ")";
//                header = "accountname,accountcode,periodamount";
//            } else {
//                title = WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.field.PeriodAmount") + " (" + WtfGlobal.getCurrencyName() + ")";
//                header = "accountname,periodamount";
//            }
//        }else{
//            if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
//                title = WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.coa.accCode") + "," + WtfGlobal.getLocaleText("acc.field.OpeningAmount") + " (" + WtfGlobal.getCurrencyName() + ")," + WtfGlobal.getLocaleText("acc.field.PeriodAmount") + " (" + WtfGlobal.getCurrencyName() + ")," + WtfGlobal.getLocaleText("acc.field.EndingAmount") + " (" + WtfGlobal.getCurrencyName() + ")";
//                header = "accountname,accountcode,openingamount,periodamount,amount";   
//            } else {
//                title = WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.field.OpeningAmount") + " (" + WtfGlobal.getCurrencyName() + ")," + WtfGlobal.getLocaleText("acc.field.PeriodAmount") + " (" + WtfGlobal.getCurrencyName() + ")," + WtfGlobal.getLocaleText("acc.field.EndingAmount") + " (" + WtfGlobal.getCurrencyName() + ")";
//                header = "accountname,openingamount,periodamount,amount";   
//            }        
//        }        
//        if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
//            align = "none,none,withoutcurrency,withoutcurrency,withoutcurrency";//,none,currency";
//        } else {
//            align = "none,withoutcurrency,withoutcurrency,withoutcurrency";//,none,currency";
//        }
//        if(this.lGrid.colModel.config[4].hidden!=undefined && !this.lGrid.colModel.config[4].hidden) {  //ERP-22419
//            title += ","+WtfGlobal.getLocaleText("acc.field.PreOpeningAmount")+ " ("+WtfGlobal.getCurrencyName()+"),"+WtfGlobal.getLocaleText("acc.field.PrePeriodAmount")+ " ("+WtfGlobal.getCurrencyName()+"),"+WtfGlobal.getLocaleText("acc.field.PreEndingAmount")+ " ("+WtfGlobal.getCurrencyName()+")";//WtfGlobal.getLocaleText("acc.report.3")+","+WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.4");
//            header += ",preopeningamount,preperiodamount,preamount";
//            align += ",withoutcurrency,withoutcurrency,withoutcurrency";//,none,currency";
//        }

        this.sDate=this.startDate.getValue();
        this.eDate=this.endDate.getValue();
        this.sdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        this.edate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        this.presdate = WtfGlobal.convertToGenericStartDate(this.startPreDate.getValue());
        this.preedate = WtfGlobal.convertToGenericEndDate(this.endPreDate.getValue());
        /*
         * ERM-1266 in custom profit and loss report when exclude prev year opening then start date is used as current FY year date
         * and when in Profit and Loss template All filter selected then book beginning date used as start date
         * And in all other custom layout reports for From date book beginning date used
         */
        if(this.isOnlyEndingBalances && this.excludeTypeCmb.getValue()==false && this.statementType=="TradingAndProfitLoss"){
           this.sdate = WtfGlobal.convertToGenericStartDate(Wtf.account.companyAccountPref.bbfrom);
        } else if(this.isOnlyEndingBalances && this.statementType!="TradingAndProfitLoss"){
           this.sdate = WtfGlobal.convertToGenericStartDate(Wtf.account.companyAccountPref.bbfrom);
        }
        if(this.isOnlyEndingBalances || this.isOnlyPeriodBalances){
            this.presdate = "";
            this.preedate = "";
        }
        
        var columns = this.lGrid.colModel.config;
        for(var cnt = 0;cnt < columns.length;cnt++){
            var column = columns[cnt];
            if(column.hidden != true){
                title.push(column.header);
                header.push(column.dataIndex);
                if(column.dataIndex == "accountname" || column.dataIndex == "accountcode"){
                    align.push("none");
                }else{
                    align.push("withoutcurrency");
                }
            }
        }
        title = title.join(",");
        header = header.join(",");
        align = align.join(",");
        
        exportUrl = getExportUrl(Wtf.autoNum.CustomPnlBs, this.consolidateFlag);
        searchJson= this.searchJson!=undefined?this.searchJson:"";
        filterConjuctionCriteria= this.filterConjuctionCrit;
        fileName = this.templatetitle;//WtfGlobal.getLocaleText("acc.P&L.tabTitle");
        reportName = this.templatetitle;//WtfGlobal.getLocaleText("acc.P&L.tabTitle");
        extraParams += "&costcenter="+this.costCenter.getValue();
        extraParams += "&reportView="+this.statementType;  
        extraParams += "&periodView="+((this.isOnlyPeriodBalances == undefined || this.isOnlyPeriodBalances == null || this.isOnlyPeriodBalances == "") ? false : this.isOnlyPeriodBalances);
        var isBalanceSheet = false;
        if(this.statementType=="BalanceSheet"){
            isBalanceSheet=true;
        }
        if(this.consolidateFlag) {
            var url = exportUrl+"&filename="+encodeURIComponent(fileName)+extraParams+"&filetype="+type+"&stdate="+this.sdate+"&enddate="+this.edate+"&nondeleted="+true+"&accountid="+"&searchJson="+searchJson+"&filterConjuctionCriteria="+filterConjuctionCriteria
                            +"&excludePreviousYear="+this.excludeTypeCmb.getValue()+"&header="+header+"&title="+encodeURIComponent(title)+"&name="+encodeURIComponent(reportName)+"&width=150&get=27&align="+align+"&toggle="+this.toggle+"&singleGrid="+true+"&templatecode="+templatecode+"&templateid="+this.templateid+"&isBalanceSheet="+isBalanceSheet;;
        } else {
            url = exportUrl+"?filename="+encodeURIComponent(fileName)+extraParams+"&filetype="+type+"&stdate="+this.sdate+"&enddate="+this.edate+"&nondeleted="+true+"&accountid="+"&searchJson="+searchJson+"&filterConjuctionCriteria="+filterConjuctionCriteria
                            +"&excludePreviousYear="+this.excludeTypeCmb.getValue()+"&header="+header+"&title="+encodeURIComponent(title)+"&name="+encodeURIComponent(reportName)+"&width=150&get=27&align="+align+"&toggle="+this.toggle+"&singleGrid="+true+"&templatecode="+templatecode+"&stpredate="+this.presdate+"&endpredate="+this.preedate+"&templateid="+this.templateid+"&isBalanceSheet="+isBalanceSheet;
        }

        if(type == "print") {
            url+="&generatedOnTime="+WtfGlobal.getGeneratedOnTimestamp();
            window.open(url, "mywindow","menubar=1,resizable=1,scrollbars=1");
        } else {
            Wtf.get('downloadframe').dom.src  = url;
        }
//        Wtf.get('downloadframe').dom.src = url;
    },
     collapseGrids : function() {
        for(var i=0; i< this.lGrid.getStore().data.length; i++){
            this.lGrid.collapseRow(this.lGrid.getView().getRow(i));

        }
    
    },        
    getRowClass:function(record,grid){
        var colorCss="";
        switch(record.data["fmt"]){
            case "T":colorCss=" grey-background";break;
            case "B":colorCss=" red-background";break;
            case "H":colorCss=" header-background";break;
            case "A":colorCss=" darkyellow-background";break;
        }
        return grid.getRowClass()+colorCss;
    },

    formatAccountName:function(val,m,rec,i,j,s){
        var fmtVal=val;
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

    formatMoney:function(val,m,rec,i,j,s){
        var fmtVal=WtfGlobal.currencyRenderer(val);
        if(rec.data['fmt']){
            fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
        }
        else if(rec.data["level"]==0&&rec.data["accountname"]!="")
            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
        return fmtVal;
    },

    fetchStatement:function(isCompare){
       this.lGrid.getStore().removeAll();
       this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
       WtfComMsgBox(29,4,true); //Show loading mask
       this.sDate=this.startDate.getValue();
       this.eDate=this.endDate.getValue();
       this.isComapre = typeof(isCompare) == typeof(true) ? true : false;
       if(this.sDate=="" || this.eDate=="") {
           WtfComMsgBox(42,2);
           return;
       }

       this.sdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());       
       this.edate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
         if(this.sDate>this.eDate){
            WtfComMsgBox(1,2);
            return;
        }
        var templatecode = '-1';
        var index = this.templateStore.find('id', this.templateCombo.getValue());
        if(index > -1){
            templatecode = this.templateStore.getAt(index).data.templatecode;
        }
        
        this.presdateCompare = "";
        this.preedateCompare = "";
        this.presdate = WtfGlobal.convertToGenericStartDate(this.startPreDate.getValue());
        this.preedate = WtfGlobal.convertToGenericEndDate(this.endPreDate.getValue());
        /*
         * ERM-1266 in custom profit and loss report when exclude prev year opening then start date is used as current FY year date
         * and when in Profit and Loss template All filter selected then book beginning date used as start date
         * And in all other custom layout reports for From date book beginning date used
         */
        if(this.isOnlyEndingBalances && this.excludeTypeCmb.getValue()==false && this.statementType=="TradingAndProfitLoss"){
           this.sdate = WtfGlobal.convertToGenericStartDate(Wtf.account.companyAccountPref.bbfrom);
        } else if(this.isOnlyEndingBalances && this.statementType!="TradingAndProfitLoss"){
           this.sdate = WtfGlobal.convertToGenericStartDate(Wtf.account.companyAccountPref.bbfrom);
        }
        if(this.isOnlyEndingBalances || this.isOnlyPeriodBalances){
            this.presdate = "";
            this.preedate = "";
        }
        if (isCompare == true) {
            this.presdateCompare = this.presdate;
            this.preedateCompare = this.preedate;
            if ((this.statementType == 'BalanceSheet' || this.statementType=="TradingAndProfitLoss")) {
//                var leftGrid = this.lGrid.colModel.config[2];
//                var leftGrid1 = this.lGrid.colModel.config[1];
                var acccode = this.lGrid.colModel.config[1];
                var openingAmt = this.lGrid.colModel.config[2];
                var periodAmt = this.lGrid.colModel.config[3];                
                var endingAmt = this.lGrid.colModel.config[4];
                
                var preopeningAmt = this.lGrid.colModel.config[5];                
                var preperiodAmt = this.lGrid.colModel.config[6];
                var preendingAmt = this.lGrid.colModel.config[7];
                
                if (preendingAmt != undefined){
                    acccode.header="<div align=right><b>"+WtfGlobal.getLocaleText("acc.coa.accCode")+"</div>";
                    
                    Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA?preopeningAmt.hidden = true:preopeningAmt.hidden = false;
                    preopeningAmt.header="<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.OpeningAmount")+"("+WtfGlobal.getCurrencyName()+")</b></br>("+WtfGlobal.onlyDateRendererForGridHeader(this.startPreDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endPreDate.getValue())+")</div>";
                    openingAmt.header="<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.OpeningAmount")+"("+WtfGlobal.getCurrencyName()+")</b></br>("+WtfGlobal.onlyDateRendererForGridHeader(this.startDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endDate.getValue())+")</div>";
                    
                    Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA?preperiodAmt.hidden = true:preperiodAmt.hidden = false;
                    preperiodAmt.header="<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.PeriodAmount")+"("+WtfGlobal.getCurrencyName()+")</b></br>("+WtfGlobal.onlyDateRendererForGridHeader(this.startPreDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endPreDate.getValue())+")</div>";
                    periodAmt.header="<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.PeriodAmount")+"("+WtfGlobal.getCurrencyName()+")</b></br>("+WtfGlobal.onlyDateRendererForGridHeader(this.startDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endDate.getValue())+")</div>";
                    
                    preendingAmt.hidden = false;
                    if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                        preendingAmt.header="<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.previousYearEndingAmount")+"("+WtfGlobal.getCurrencyName()+")</b></br>("+WtfGlobal.onlyDateRendererForGridHeader(this.startPreDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endPreDate.getValue())+")</div>";
                        endingAmt.header="<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.currrntYearEndingAmount")+"("+WtfGlobal.getCurrencyName()+")</b></br>("+WtfGlobal.onlyDateRendererForGridHeader(this.startDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endDate.getValue())+")</div>";
                    }else{
                        preendingAmt.header="<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.EndingAmount")+"("+WtfGlobal.getCurrencyName()+")</b></br>("+WtfGlobal.onlyDateRendererForGridHeader(this.startPreDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endPreDate.getValue())+")</div>";
                        endingAmt.header="<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.EndingAmount")+"("+WtfGlobal.getCurrencyName()+")</b></br>("+WtfGlobal.onlyDateRendererForGridHeader(this.startDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endDate.getValue())+")</div>";
                    }
                }
                this.lGrid.getView().refresh(true);
            }
        } else {
            if (!this.firstTime && (this.statementType == 'BalanceSheet' || this.statementType=="TradingAndProfitLoss")) {
//                var leftGrid = this.lGrid.colModel.config[2];
//                var leftGrid1 = this.lGrid.colModel.config[1];

                acccode = this.lGrid.colModel.config[1];
                openingAmt = this.lGrid.colModel.config[2];
                periodAmt = this.lGrid.colModel.config[3];                
                endingAmt = this.lGrid.colModel.config[4];
                
                preopeningAmt = this.lGrid.colModel.config[5];                
                preperiodAmt = this.lGrid.colModel.config[6];
                preendingAmt = this.lGrid.colModel.config[7];                

                if (preendingAmt != undefined){
                    acccode.header="<div align=right><b>"+WtfGlobal.getLocaleText("acc.coa.accCode")+"</div>";
                    
                    preopeningAmt.hidden = true;
                    openingAmt.header="<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.OpeningAmount")+"</div>";
                    
                    preperiodAmt.hidden = true;
                    periodAmt.header="<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.PeriodAmount")+"</div>";
                    
                    preendingAmt.hidden = true;
                    if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                        endingAmt.header="<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.currrntYearEndingAmount")+"</div>";
                    }else{
                        endingAmt.header="<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.EndingAmount")+"</div>";
                    }
                }
                this.lGrid.getView().refresh(true);
            }
        }
        var params={
         //   fordate:WtfGlobal.convertToGenericDate(this.forDate.getValue().add(Date.DAY,1)),
            stdate:this.sdate,
            enddate:this.edate,
            stpredate:this.presdateCompare,
            endpredate:this.preedateCompare,
            templatecode:templatecode,
            periodView: (this.isOnlyPeriodBalances == undefined || this.isOnlyPeriodBalances == null || this.isOnlyPeriodBalances == "") ? false : this.isOnlyPeriodBalances,
            excludePreviousYear : this.excludeTypeCmb.getValue()
        }        
         params.searchJson= this.searchJson!=undefined?this.searchJson:"",
         params.filterConjuctionCriteria= this.filterConjuctionCrit
        if(this.consolidateFlag && this.consolidateFlag==true) {            
            params.companyids=companyids;
            params.gcurrencyid=gcurrencyid;
            params.userid=loginid;
        }

        this.ajxUrl = Wtf.req.account+'CompanyManager.jsp';
        if(this.statementType=="Trading") {
            params.mode=63;
            params.nondeleted=true;
            this.ajxUrl = this.consolidateFlag?"ACCCombineReports/getTradingMerged.do":"ACCReports/getTrading.do";
        }
        if(this.statementType=="ProfitAndLoss") {
            params.mode=64;
            params.nondeleted=true;
            this.ajxUrl = this.consolidateFlag?"ACCCombineReports/getProfitLossMerged.do":"ACCReports/getProfitLoss.do";
        }
//        if(this.statementType=="TradingAndProfitLoss" || this.statementType=="CostCenter") {
            params.mode=this.statementType=="BalanceSheet"?66:65;
            params.nondeleted=true;
            params.costcenter = this.costCenter.getValue();
            params.reportView = this.statementType;
            params.templateid = this.templateid;
            if(this.statementType=="BalanceSheet"){
                params.isBalanceSheet=true;
            }
            params.singleGrid = true;
            this.ajxUrl = "ACCReports/getBSorPL_CustomLayout.do";

          WtfGlobal.setAjaxTimeOut();
          Wtf.Ajax.requestEx({url:this.ajxUrl,params:params}, this, this.successCallback, this.failureCallback);
    },

    successCallback:function(response){
        WtfGlobal.resetAjaxTimeOut();
        if(response.success){
            this.total=response.data.total;
            if (response.data.pretotal != undefined) {
                this.total[2] = response.data.pretotal[0];
                this.total[3] = response.data.pretotal[0];
            } 
            this.lGrid.store.loadData(response.data);
            this.doLayout();
            if (this.firstTime && !this.isComapre && (this.statementType == 'BalanceSheet'|| this.statementType=="TradingAndProfitLoss")) {
//                var leftGrid = this.lGrid.colModel.config[2];
                
//                var openingAmt = this.lGrid.colModel.config[1];
//                var periodAmt = this.lGrid.colModel.config[2];                
//                var endingAmt = this.lGrid.colModel.config[3];
//                
                var preopeningAmt = this.lGrid.colModel.config[5];                
                var preperiodAmt = this.lGrid.colModel.config[6];
                var preendingAmt = this.lGrid.colModel.config[7];
                
                if (preendingAmt != undefined) {
                    preopeningAmt.hidden = true;
                    preperiodAmt.hidden = true;
                    preendingAmt.hidden = true;
                }
                this.lGrid.getView().refresh(true);
            }
            this.firstTime = false;
            this.collapseGrids();   
        }
        this.hideLoading();
        var msg=response.msg;
        var dontshowmsg = response.dontshowmsg;
        var dontMsg = "<div id = 'check-"+this.templateid+"' style='padding:10px;'></div>";
        /**
         * Size of alert box is large than desktop screen thats why we are not able to view ERP application and it shows blank screen.
         * Because of this added ellipsis. SDP-10141
         */
            if(!dontshowmsg){
            if(msg==="All accounts are mapped in current Template"){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg+dontMsg],2);
            }else{
                Wtf.MessageBox.show({
                    title : WtfGlobal.getLocaleText("acc.common.alert"),
                    msg : WtfGlobal.getLocaleText("acc.field.Accounts")+"<br/>"+Wtf.util.Format.ellipsis(msg,1500)+"<br/>"+WtfGlobal.getLocaleText("acc.customelayout.arenotmappedincurrentTemplate")+dontMsg,
                    width : 420,
                    fn : function(btnText){
                        if(btnText == "ok" && (this.dontShowMsgCheckbox.getValue() == "on" || this.dontShowMsgCheckbox.getValue() == true)){
                            Wtf.Ajax.requestEx({
                                url :"ACCReports/setDontShowFlagCustomLayout.do",
                                params :{
                                    templateid : this.templateid 
                                }
                            }, this);
                        }
                    }.createDelegate(this),
                    buttons: Wtf.MessageBox.OK,
                    animEl: 'mb9',
                    icon: Wtf.MessageBox.WARNING
                });
                this.dontShowMsgCheckbox = new Wtf.form.Checkbox({
                    name:'dontshow',
                    hideLabel:true,
                    boxLabel :"Don't show this message again."
                });
                this.dontShowMsgCheckboxForm = new Wtf.form.FormPanel({
                    renderTo:"check-"+this.templateid,
                    border : false,
                    items:[this.dontShowMsgCheckbox]
                });
            }
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
//        if(this.statementType=='BalanceSheet'&&start)
//             return new Date('January 1, 1970 00:00:00 AM');
        var monthDateStr=d.format('M d');
//        if(Wtf.account.companyAccountPref.fyfrom&&this.statementType!='BalanceSheet')
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
//        if(start||this.statementType=='BalanceSheet')
        if(start)
            return fd;
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },

   getPreDates:function(start){      
        return start.add(Date.YEAR, -1);
    },
    
   fetchCompareStatement:function(){
     this.isCompare = true;
     this.fetchStatement(true);
    },
    
    showLastRec:function(pos){
        return WtfGlobal.currencySummaryRenderer(this.total[pos]);
    },
    
    showAdvanceSearch: function() {
        showAdvanceSearch(this, this.searchparam, this.filterAppend);
    },
    
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();   
    },
    
    filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.fetchStatement();
    },
    
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
       this.fetchStatement();
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },  
    
     storeLoad: function() {
       this.fetchStatement();
        this.doLayout();


    }     
});


Wtf.account.TrialBalanceCustomLayout=function(config){
    this.total=[0,0];
    this.leftHeading=(config.statementType=="BalanceSheet"?"Liability":"Debit");
     this.uPermType=Wtf.UPerm.fstatement;
     this.permType=Wtf.Perm.fstatement;
     this.exportPermType=(config.statementType=="BalanceSheet"?this.permType.exportdatabsheet:this.permType.exportdatatradingpnl);
     this.printPermType=(config.statementType=="BalanceSheet"?this.permType.printbsheet:this.permType.exportdatatradingpnl)
    this.toggle=0;
    this.summaryL = new Wtf.ux.grid.GridSummary();
    this.FinalStatementRec = new Wtf.data.Record.create([
        {name: 'accountname'},
        {name: 'accountcode'},
        {name: 'accountid'},
        {name: 'openingamount'},
        {name: 'periodamount'},
        {name: 'amount'},
        {name: 'openingamountd'},
        {name: 'periodamountd'},
        {name: 'amountd'},
        {name: 'preopeningamount'},
        {name: 'preperiodamount'},
        {name: 'preamount'},
        {name: 'accountflag'},
        {name: 'isdebit',type:'boolean'},
        {name: 'level'},
        {name: 'fmt'},
        {name: 'leaf'},
        {name: 'd_openingamount'},
        {name: 'c_openingamount'},
        {name: 'd_periodamount'},
        {name: 'c_periodamount'},
        {name: 'd_endingamount'},
        {name: 'c_endingamount'}
    ]);

    this.statementType=config.statementType||"Trading";

    this.lStroe = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "left"							// Assets on Left side in Balance Sheet          Neeraj
        },this.FinalStatementRec),
        baseParams:{
            nondeleted:true
        },
        url: Wtf.req.account+'CompanyManager.jsp'
    });
    this.summary = new Wtf.ux.grid.GridSummary();
    this.lGrid = new Wtf.grid.HirarchicalGridPanel({
        //plugins:[this.summaryL],
        //stripeRows :true,
        autoScroll:true,
        store: this.lStroe,
        hirarchyColNumber:0,
        plugins: [new Wtf.GroupHeaderGrid({
            rows: [
                [{align:"center",header:"",colspan:2},
                 {align:"center",header:"<b>"+WtfGlobal.getLocaleText("acc.field.OpeningAmount")+" ("+WtfGlobal.getCurrencyName()+")</b>",colspan:2},
                 {align:"center",header:"<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.PeriodAmount")+" ("+WtfGlobal.getCurrencyName()+")</b></div>",colspan:2},
                 {align:"center",header:"<div align=right><b>"+WtfGlobal.getLocaleText("acc.field.EndingAmount")+" ("+WtfGlobal.getCurrencyName()+")</b></div>",colspan:2}]
            ],
            hierarchicalColMenu: true
        }),this.summary],
        columns: [{
            header:'<b>'+WtfGlobal.getLocaleText("acc.balanceSheet.particulars")+'</b>',
            dataIndex:'accountname',
            renderer:this.formatAccountName,
            width:150,
            summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
        },{
            header:'<b>'+WtfGlobal.getLocaleText("acc.coa.accCode")+'</b>',
            dataIndex:'accountcode',
                hidden: !Wtf.account.companyAccountPref.showaccountcodeinfinancialreport,
            renderer:this.formatAccountName,
            width:50,
            align: 'center'
        },{
            header:"<div align=center><b>"+WtfGlobal.getLocaleText("acc.je.creditAmt")+"</b></div>",
            dataIndex:'c_openingamount',
            hidecurrency : true,
            renderer:this.formatMoney,
            summaryRenderer:this.showLastRec.createDelegate(this,[1])
        },{
            header:"<div align=center><b>"+WtfGlobal.getLocaleText("acc.bankReconcile.gridDebitAmount")+"</b></div>",
            dataIndex:'d_openingamount',
            hidecurrency : true,
            renderer:this.formatMoney,
            summaryRenderer:this.showLastRec.createDelegate(this,[0])
        },{
            header:"<div align=center><b>"+WtfGlobal.getLocaleText("acc.bankReconcile.gridCreditAmount")+"</b></div>",
            dataIndex:'c_periodamount',
            hidecurrency : true,
            renderer:this.formatMoney,
            summaryRenderer:this.showLastRec.createDelegate(this,[3])
        },{
            header:"<div align=center><b>"+WtfGlobal.getLocaleText("acc.bankReconcile.gridDebitAmount")+"</b></div>",
            dataIndex:'d_periodamount',
            hidecurrency : true,
            renderer:this.formatMoney,
            summaryRenderer:this.showLastRec.createDelegate(this,[2])
        },{
            header:"<div align=center><b>"+WtfGlobal.getLocaleText("acc.ccReport.ca")+"</b></div>",
            dataIndex:'c_endingamount',
            hidecurrency : true,
            renderer:this.formatMoney,
            summaryType:'sum',
            summaryRenderer:this.showLastRec.createDelegate(this,[5])
        },{
            header:"<div align=center><b>"+WtfGlobal.getLocaleText("acc.bankBook.gridDebitAmt")+"</b></div>",
            dataIndex:'d_endingamount',
            hidecurrency : true,
            renderer:this.formatMoney,
            summaryType:'sum',
            summaryRenderer:this.showLastRec.createDelegate(this,[4])
        }],
        border : false,
        loadMask : true,
        viewConfig: {
            forceFit:true
        }
    });
    this.lGrid.on("render", WtfGlobal.autoApplyHeaderQtip);
    this.lGrid.on('rowclick',this.onRowClickLGrid, this);
    this.lGrid.on('render',function(){
        this.lGrid.getView().getRowClass=this.getRowClass.createDelegate(this,[this.lGrid],1);
    },this);

    this.lGrid.getStore().on("load", function(){
        for(var i=0; i< this.lGrid.getStore().data.length; i++){
            this.lGrid.collapseRow(this.lGrid.getView().getRow(i));

        }
    }, this);

this.firstTime=true;
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate',
        format:WtfGlobal.getOnlyDateFormat(),
      //  readOnly:true,
//        hidden:config.statementType=='BalanceSheet',
        value:this.getDates(true)
    });
     this.startPreDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stpredate',
        format:WtfGlobal.getOnlyDateFormat(),
       // readOnly:true,
        width : 70,
//        hidden:config.statementType=='BalanceSheet',
        value:this.getPreDates(this.startDate.getValue())
    });    
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),
        format:WtfGlobal.getOnlyDateFormat(),
       // readOnly:true,
        name:'enddate',
        value:this.getDates(false)
    });
     this.endPreDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),
        format:WtfGlobal.getOnlyDateFormat(),
        //readOnly:true,
        width : 70,
        name:'endpredate',
        value:this.getPreDates(this.endDate.getValue())
    });
    this.startDate.on("change",function(){    
     this.startPreDate.setValue(this.startDate.getValue().add(Date.YEAR, -1))
    },this);
    
    this.endDate.on("change",function(){    
     this.endPreDate.setValue(this.endDate.getValue().add(Date.YEAR, -1))
    },this);
    this.grid = this.lGrid;
    var mnuBtns=[];
    var csvbtn=new Wtf.Action({
        iconCls:'pwnd '+'exportcsv',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToCSVTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToCSV")+"</span>",
        scope: this,
        hidden : (SATSCOMPANY_ID==companyid),
        handler:function(){
            this.exportWithTemplate()
        }
    });
    mnuBtns.push(csvbtn);
      var xlsbtn=new Wtf.Action({
        iconCls:'pwnd '+'exportcsv',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToXLSTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"</span>",
        scope: this,
        handler:function(){
            this.exportWithTemplate("xls")
        }
    });
    mnuBtns.push(xlsbtn);
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
    var btnArr1=[];

    btnArr.push(
        WtfGlobal.getLocaleText("acc.common.from"),this.startDate,
        WtfGlobal.getLocaleText("acc.common.to"),this.endDate
        );

    if(this.statementType=="TradingAndProfitLoss" || this.statementType=="CostCenter"){
        btnArr.push(WtfGlobal.getLocaleText("acc.common.costCenter"),this.costCenter);
    }

     this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: config.moduleid,
        advSearch: false,
        reportid: config.reportid,
        templateid: config.templateid,
        templatetitle:config.templatetitle
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
//        id: 'advanced3', // In use, Do not delete
        scope: this,
//        hidden:(this.moduleid==undefined)?true:false,        
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    var comboReader = new Wtf.data.Record.create([
    {
        name: 'id',
        type: 'string'
    },
    {
        name: 'name',
        type: 'string'
    },
    {
        name: 'templatecode',
        type: 'string'
    }
    ]);
   
    
    this.templateStore = new Wtf.data.Store({
        url :'ACCAccount/getPnLTemplates.do',
        root: 'data',
        remoteSort:true,
        reader: new Wtf.data.KwlJsonReader({
            root: 'data'
        },comboReader),
        baseParams:{
            isdropdown:true
        }
    });
    
    this.templateStore.on("load", function(){
        this.templateStore.insert(0, new Wtf.data.Record({
            'id' : '-1', 
            'name' : 'None',
            'templatecode' : "" 
        }));
        this.templateCombo.setValue('-1');
    }, this);
    this.templateStore.load();
    this.templateCombo= new Wtf.form.ComboBox({
        fieldLabel:(this.isCustomer?WtfGlobal.getLocaleText("acc.het.101"):WtfGlobal.getLocaleText("acc.field.DebitAccount*")),
        width:120,
        store: this.templateStore,
        valueField:'id',
        displayField:'name',
        mode: 'local',
        typeAhead: true,
        forceSelection: true,
        selectOnFocus:true,
        triggerAction:'all',
        scope:this
    });
    
//    btnArr.push('-','Custom Layout ', this.templateCombo);
    
    btnArr.push('-',{
        xtype:'button',
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
        tooltip:WtfGlobal.getLocaleText("acc.common.fetchTT"),
        iconCls:'accountingbase fetch',
        tooltip:(config.statementType=='BalanceSheet'?WtfGlobal.getLocaleText("acc.cc.26"):WtfGlobal.getLocaleText("acc.cc.27")),
        scope:this,
        handler:this.fetchStatement
    });
    if ((config.statementType == "BalanceSheet" || config.statementType=="TradingAndProfitLoss")  && !config.consolidateFlag) {
        btnArr.push('-', (WtfGlobal.getLocaleText("acc.common.compareWith")),
                (WtfGlobal.getLocaleText("acc.common.from")), this.startPreDate,
                (WtfGlobal.getLocaleText("acc.common.to")), this.endPreDate,{
        xtype:'button',
        text:WtfGlobal.getLocaleText("acc.common.compare"),  //'Fetch',
        iconCls:'accountingbase fetch',
        tooltip:WtfGlobal.getLocaleText("acc.cc.balanceSheet.compare.TT"),
        scope:this,
        style:" margin-left: 5px;",
        handler:this.fetchCompareStatement
    }
                );
    }
    
     btnArr.push('-',this.AdvanceSearchBtn);
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
        btnArr1.push(this.expButton=new Wtf.Button({
    //        iconCls:'pwnd '+'exportcsv',
            text:WtfGlobal.getLocaleText("acc.common.export"),  //'Export',
            iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export'),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details.',
//                    disabled :true,
//            hidden:true,
            scope: this,
            menu:mnuBtns
        }));
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){

        btnArr1.push(this.printbtn=new Wtf.Button({
            iconCls:'pwnd printButtonIcon',
            text :WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details.',
//            disabled :true,
//            hidden:true,
            scope: this,
            handler:function(){
                this.exportWithTemplate("print")
            }
        }));
    }

//   this.expButton=new Wtf.exportButton({
//        obj:this,
//        id:"exportReports"+config.helpmodeid,
//        tooltip :'Export report details',
//        params:{ stdate:WtfGlobal.convertToGenericDate(this.getDates(true)),
//                 enddate:WtfGlobal.convertToGenericDate(this.getDates(false))/*,
//                 accountid:this.accountID||config.accountID*/
//        },
//        menuItem:{csv:false,pdf:true,rowPdf:false},
//        get:27
//    });
//    
//     btnArr1.push('-', this.expButton);

    this.expandCollpseButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.Expand"),
            tooltip:WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
            iconCls:'pwnd toggleButtonIcon',
            scope:this,
            handler: function(){
                this.expandCollapseGrid(this.expandCollpseButton.getText());
            }
        });
    
    btnArr1.push('-', this.expandCollpseButton);
    
    this.wrapperPanel = new Wtf.Panel({
        border:false,
        layout:"border",
        scope:this,
        items:[this.objsearchComponent,
            /*this.centerPanel = new Wtf.Panel({
                region:'center',
                layout:'fit',
                width:'49%',
                border:true,
                items:this.statementType=="BalanceSheet"?this.lGrid:this.rGrid
            }),*/
            this.westPanel = new Wtf.Panel({
                width:'90%',
                region:'center',
                layout:'fit',
                //width:'49%',
                border:false,
                //split:true,
                items:this.lGrid
            }),
        {layout:'fit',region:'west',width:'5%'},{layout:'fit',region:'east',width:'5%'}],
        tbar:btnArr1
    });

    Wtf.apply(this,{
        defaults:{border:false,bodyStyle:"background-color:white;"},
        saperate:true,
        statementType:"Trading",
        items:this.wrapperPanel,
        tbar:btnArr
    },config);

     Wtf.account.TrialBalanceCustomLayout.superclass.constructor.call(this,config);
     this.addEvents({
        'account':true
     });
     if(this.statementType!="CostCenter"){
        this.fetchStatement();
     }
}

Wtf.extend( Wtf.account.TrialBalanceCustomLayout,Wtf.Panel,{
    onRowClickLGrid:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var accid=this.lGrid.getStore().getAt(i).data['accountid'];    
        var searchJson = (this.objsearchComponent.advGrid != undefined) ? this.objsearchComponent.advGrid.getJsonofStore() : "" ;
        this.fireEvent('account',searchJson,this.filterConjuctionCrit,accid,this.startDate.getValue(),this.endDate.getValue());
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
        var templatecode = '-1';
        var filterConjuctionCriteria=""; //added for advance search parametres
        var searchJson="";
        var index = this.templateStore.find('id', this.templateCombo.getValue());
        if(index > -1){
            templatecode = this.templateStore.getAt(index).data.templatecode;
        }
            exportUrl = getExportUrl(Wtf.autoNum.CustomPnlBs, this.consolidateFlag);
            fileName = this.templatetitle;
            extraParams += "&costcenter="+this.costCenter.getValue();
            extraParams += "&reportView="+this.statementType;
            get = 28;
            jsonGrid = "{data:[{'header':'laccountname','title':'Particulars','width':'150','align':''},"+
                            "{'header':'lamount','title':'Amount(Debit)','width':'150','align':'currency'},"+
                            "{'header':'raccountname','title':'Particulars','width':'150','align':''},"+
                            "{'header':'ramount','title':'Amount(Credit)','width':'150','align':'currency'}]}";
            header =  fileName;
            searchJson= this.searchJson!=undefined?this.searchJson:"";
            filterConjuctionCriteria= this.filterConjuctionCrit;
//        }
        var isBalanceSheet = false;
        if(this.statementType=="BalanceSheet"){
            isBalanceSheet=true;
        }
         var isTrialBalance = false;
        if(this.statementType=="TrialBalance"){
            isTrialBalance=true;
        }
        var configstr = "{%22landscape%22:%22true%22,%22pageBorder%22:%22true%22,%22gridBorder%22:%22true%22,%22title%22:%22Test%20Title%22,%22subtitles%22:%22%22,%22headNote%22:%22Test%20Header%22,%22showLogo%22:%22false%22,%22headDate%22:%22false%22,%22footDate%22:%22true%22,%22footPager%22:%22false%22,%22headPager%22:%22true%22,%22footNote%22:%22Test%20Footer%22,%22textColor%22:%22000000%22,%22bgColor%22:%22FFFFFF%22}"
        if(this.consolidateFlag) {
            var url = exportUrl+"&filename="+encodeURIComponent(fileName)+extraParams+"&config="+configstr+"&header"+header+"&filetype=pdf&stdate="+this.sdate+"&enddate="+this.edate+"&toggle="+this.toggle+"&searchJson="+searchJson+"&filterConjuctionCriteria="+filterConjuctionCriteria
                     +"&get="+get+"&gridconfig="+encodeURIComponent(jsonGrid)+"&templatecode="+templatecode+"&templateid="+this.templateid+"&isBalanceSheet="+isBalanceSheet+"&isTrialBalance="+isTrialBalance;
        } else {
            url = exportUrl+"?filename="+encodeURIComponent(fileName)+extraParams+"&config="+configstr+"&header"+header+"&filetype=pdf&stdate="+this.sdate+"&enddate="+this.edate+"&toggle="+this.toggle+"&searchJson="+searchJson+"&filterConjuctionCriteria="+filterConjuctionCriteria
                     +"&get="+get+"&gridconfig="+encodeURIComponent(jsonGrid)+"&templatecode="+templatecode+"&templateid="+this.templateid+"&isBalanceSheet="+isBalanceSheet+"&isTrialBalance="+isTrialBalance;
        }
            Wtf.get('downloadframe').dom.src = url;

    },
    setCostCenter: function(){
        this.costCenter.setValue("");//Select Default Cost Center as None
        Wtf.CostCenterStore.un("load", this.setCostCenter, this);
    },
    resetFilterAndFetchReport: function(costCenterId, startDate, endDate){
        this.costCenter.setValue(costCenterId);
        this.startDate.setValue(startDate?startDate:this.getDates(true));
        this.startPreDate.setValue(this.startDate.getValue());
        this.endPreDate.setValue(this.endDate.getValue());
        this.endDate.setValue(endDate?endDate:this.getDates(false));
        this.fetchStatement();
    },
    exportWithTemplate:function(type){
        type = type?type:"csv";
        var exportUrl;
        var fileName;
        var reportName;
        var header = "accountname,amount";//,raccountname,ramount";
         var title = "";
         var extraParams = "";
         var templatecode = '-1';
         var filterConjuctionCriteria="";
        var searchJson="";
        var index = this.templateStore.find('id', this.templateCombo.getValue());
        if(index > -1){
            templatecode = this.templateStore.getAt(index).data.templatecode;
        }
//        title = WtfGlobal.getLocaleText("acc.report.2")+","+"Amount"+ " ("+WtfGlobal.getCurrencyName()+"),"+"Amount"+ " ("+WtfGlobal.getCurrencyName()+")";//WtfGlobal.getLocaleText("acc.report.3")+","+WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.4");
        title = WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.coa.accCode")+","+WtfGlobal.getLocaleText("acc.field.OpeningCreditAmount")+ " ("+WtfGlobal.getCurrencyName()+"),\n\
                "+WtfGlobal.getLocaleText("acc.field.OpeningDebitAmount")+ " ("+WtfGlobal.getCurrencyName()+"),"+WtfGlobal.getLocaleText("acc.field.PeriodCreditAmount")+ " ("+WtfGlobal.getCurrencyName()+")"+","+WtfGlobal.getLocaleText("acc.field.DebitDebitAmount")+ " ("+WtfGlobal.getCurrencyName()+"),\n\
                "+WtfGlobal.getLocaleText("acc.field.EndingCreditAmount")+ " ("+WtfGlobal.getCurrencyName()+"),"+WtfGlobal.getLocaleText("acc.field.EndingDebitAmount")+ " ("+WtfGlobal.getCurrencyName()+")";
//        header = "accountname,openingamount,openingamountd,periodamount,periodamountd,amount,amountd";
        header = "accountname,accountcode,c_openingamount,d_openingamount,c_periodamount,d_periodamount,c_endingamount,d_endingamount";
        var align = "none,none,withoutcurrency,withoutcurrency,withoutcurrency,withoutcurrency,withoutcurrency,withoutcurrency";//,none,currency";
//        if(!this.lGrid.colModel.config[4].hidden) {
//            title += ",Pre Opening Amount"+ " ("+WtfGlobal.getCurrencyName()+"),"+"Pre Period Amount"+ " ("+WtfGlobal.getCurrencyName()+"),"+"Pre Ending Amount"+ " ("+WtfGlobal.getCurrencyName()+")";//WtfGlobal.getLocaleText("acc.report.3")+","+WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.4");
//            header += ",preopeningamount,preperiodamount,preamount";
//            align += ",withoutcurrency,withoutcurrency,withoutcurrency";//,none,currency";
//        }

        exportUrl = getExportUrl(Wtf.autoNum.CustomPnlBs, this.consolidateFlag);
        searchJson= this.searchJson!=undefined?this.searchJson:"";
        filterConjuctionCriteria= this.filterConjuctionCrit;
        fileName = this.templatetitle;//WtfGlobal.getLocaleText("acc.P&L.tabTitle");
        reportName = this.templatetitle;//WtfGlobal.getLocaleText("acc.P&L.tabTitle");
        extraParams += "&costcenter="+this.costCenter.getValue();
        extraParams += "&reportView="+this.statementType;        
        var isBalanceSheet = false;
        if(this.statementType=="BalanceSheet"){
            isBalanceSheet=true;
        }
        var isTrialBalance = false;
        if(this.statementType=="TrialBalance"){
            isTrialBalance=true;
        }
        if(this.consolidateFlag) {
            var url = exportUrl+"&filename="+encodeURIComponent(fileName)+extraParams+"&filetype="+type+"&stdate="+this.sdate+"&enddate="+this.edate+"&nondeleted="+true+"&accountid="+"&searchJson="+searchJson+"&filterConjuctionCriteria="+filterConjuctionCriteria
                            +"&header="+header+"&title="+encodeURIComponent(title)+"&name="+encodeURIComponent(reportName)+"&width=150&get=27&align="+align+"&toggle="+this.toggle+"&singleGrid="+true+"&templatecode="+templatecode+"&templateid="+this.templateid+"&isBalanceSheet="+isBalanceSheet+"&isTrialBalance="+isTrialBalance;
        } else {
            url = exportUrl+"?filename="+encodeURIComponent(fileName)+extraParams+"&filetype="+type+"&stdate="+this.sdate+"&enddate="+this.edate+"&nondeleted="+true+"&accountid="+"&searchJson="+searchJson+"&filterConjuctionCriteria="+filterConjuctionCriteria
                            +"&header="+header+"&title="+encodeURIComponent(title)+"&name="+encodeURIComponent(reportName)+"&width=150&get=27&align="+align+"&toggle="+this.toggle+"&singleGrid="+true+"&templatecode="+templatecode+"&stpredate="+this.presdate+"&endpredate="+this.preedate+"&templateid="+this.templateid+"&isBalanceSheet="+isBalanceSheet+"&isTrialBalance="+isTrialBalance;
        }

        if(type == "print") {
            url+="&generatedOnTime="+WtfGlobal.getGeneratedOnTimestamp();
            window.open(url, "mywindow","menubar=1,resizable=1,scrollbars=1");
        } else {
            Wtf.get('downloadframe').dom.src  = url;
        }
//        Wtf.get('downloadframe').dom.src = url;
    },
     collapseGrids : function() {
        for(var i=0; i< this.lGrid.getStore().data.length; i++){
            this.lGrid.collapseRow(this.lGrid.getView().getRow(i));

        }
    
    },        
    getRowClass:function(record,grid){
        var colorCss="";
        switch(record.data["fmt"]){
            case "T":colorCss=" grey-background";break;
            case "B":colorCss=" red-background";break;
            case "H":colorCss=" header-background";break;
            case "A":colorCss=" darkyellow-background";break;
        }
        return grid.getRowClass()+colorCss;
    },

    formatAccountName:function(val,m,rec,i,j,s){
        var fmtVal=val;
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

    formatMoney:function(val,m,rec,i,j,s){
        var fmtVal=WtfGlobal.currencyRenderer(val);
        if(rec.data['fmt']){
            fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
        }
        else if(rec.data["level"]==0&&rec.data["accountname"]!="")
            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
        return fmtVal;
    },

    fetchStatement:function(isCompare){
       this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
       WtfComMsgBox(29,4,true); //Show loading mask
       this.sDate=this.startDate.getValue();
       this.eDate=this.endDate.getValue();

       if(this.sDate=="" || this.eDate=="") {
           WtfComMsgBox(42,2);
           return;
       }

       this.sdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
       this.edate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
         if(this.sDate>this.eDate){
            WtfComMsgBox(1,2);
            return;
        }
        var templatecode = '-1';
        var index = this.templateStore.find('id', this.templateCombo.getValue());
        if(index > -1){
            templatecode = this.templateStore.getAt(index).data.templatecode;
        }
        
        this.presdateCompare = "";
        this.preedateCompare = "";
        this.presdate = WtfGlobal.convertToGenericStartDate(this.startPreDate.getValue());
        this.preedate = WtfGlobal.convertToGenericEndDate(this.endPreDate.getValue());
        if (isCompare == true) {
            this.presdateCompare = this.presdate;
            this.preedateCompare = this.preedate;
//            if ((this.statementType == 'BalanceSheet' || this.statementType=="TradingAndProfitLoss")) {
////                var leftGrid = this.lGrid.colModel.config[2];
////                var leftGrid1 = this.lGrid.colModel.config[1];
//                
//                var openingAmt = this.lGrid.colModel.config[1];
//                var periodAmt = this.lGrid.colModel.config[2];                
//                var endingAmt = this.lGrid.colModel.config[3];
//                
//                var preopeningAmt = this.lGrid.colModel.config[4];                
//                var preperiodAmt = this.lGrid.colModel.config[5];
//                var preendingAmt = this.lGrid.colModel.config[6];
//                
//                if (preendingAmt != undefined){
//                    preopeningAmt.hidden = false;
//                    preopeningAmt.header="<div align=right><b>"+"Opening Amount("+WtfGlobal.getCurrencyName()+")</b></br>("+WtfGlobal.onlyDateRendererForGridHeader(this.startPreDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endPreDate.getValue())+")</div>";
//                    openingAmt.header="<div align=right><b>"+"Opening Amount("+WtfGlobal.getCurrencyName()+")</b></br>("+WtfGlobal.onlyDateRendererForGridHeader(this.startDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endDate.getValue())+")</div>";
//                    
//                    preperiodAmt.hidden = false;
//                    preperiodAmt.header="<div align=right><b>"+"Period Amount("+WtfGlobal.getCurrencyName()+")</b></br>("+WtfGlobal.onlyDateRendererForGridHeader(this.startPreDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endPreDate.getValue())+")</div>";
//                    periodAmt.header="<div align=right><b>"+"Period Amount("+WtfGlobal.getCurrencyName()+")</b></br>("+WtfGlobal.onlyDateRendererForGridHeader(this.startDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endDate.getValue())+")</div>";
//                    
//                    preendingAmt.hidden = false;
//                    preendingAmt.header="<div align=right><b>"+"Ending Amount("+WtfGlobal.getCurrencyName()+")</b></br>("+WtfGlobal.onlyDateRendererForGridHeader(this.startPreDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endPreDate.getValue())+")</div>";
//                    endingAmt.header="<div align=right><b>"+"Ending Amount("+WtfGlobal.getCurrencyName()+")</b></br>("+WtfGlobal.onlyDateRendererForGridHeader(this.startDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endDate.getValue())+")</div>";
//                }
//                this.lGrid.getView().refresh(true);
//            }
        } else {
//            if (!this.firstTime && (this.statementType == 'BalanceSheet' || this.statementType=="TradingAndProfitLoss")) {
////                var leftGrid = this.lGrid.colModel.config[2];
////                var leftGrid1 = this.lGrid.colModel.config[1];
//
//                openingAmt = this.lGrid.colModel.config[1];
//                periodAmt = this.lGrid.colModel.config[2];                
//                endingAmt = this.lGrid.colModel.config[3];
//                
//                preopeningAmt = this.lGrid.colModel.config[4];                
//                preperiodAmt = this.lGrid.colModel.config[5];
//                preendingAmt = this.lGrid.colModel.config[6];                
//
//                if (preendingAmt != undefined){
//                    preopeningAmt.hidden = true;
//                    openingAmt.header="<div align=right><b>"+"Opening Amount</div>";
//                    
//                    preperiodAmt.hidden = true;
//                    periodAmt.header="<div align=right><b>"+"Period Amount</div>";
//                    
//                    preendingAmt.hidden = true;
//                    endingAmt.header="<div align=right><b>"+"Ending Amount</div>";
//                }
//                this.lGrid.getView().refresh(true);
//            }
        }
        var params={
         //   fordate:WtfGlobal.convertToGenericDate(this.forDate.getValue().add(Date.DAY,1)),
            stdate:this.sdate,
            enddate:this.edate,
            stpredate:this.presdateCompare,
            endpredate:this.preedateCompare,
            templatecode:templatecode
        }        
         params.searchJson=this.searchJson!=undefined?this.searchJson:"",
         params.filterConjuctionCriteria= this.filterConjuctionCrit
        if(this.consolidateFlag && this.consolidateFlag==true) {            
            params.companyids=companyids;
            params.gcurrencyid=gcurrencyid;
            params.userid=loginid;
        }

        this.ajxUrl = Wtf.req.account+'CompanyManager.jsp';
        if(this.statementType=="Trading") {
            params.mode=63;
            params.nondeleted=true;
            this.ajxUrl = this.consolidateFlag?"ACCCombineReports/getTradingMerged.do":"ACCReports/getTrading.do";
        }
        if(this.statementType=="ProfitAndLoss") {
            params.mode=64;
            params.nondeleted=true;
            this.ajxUrl = this.consolidateFlag?"ACCCombineReports/getProfitLossMerged.do":"ACCReports/getProfitLoss.do";
        }
//        if(this.statementType=="TradingAndProfitLoss" || this.statementType=="CostCenter") {
            params.mode=this.statementType=="BalanceSheet"?66:65;
            params.nondeleted=true;
            params.costcenter = this.costCenter.getValue();
            params.reportView = this.statementType;
            params.templateid = this.templateid;
            if(this.statementType=="BalanceSheet"){
                params.isBalanceSheet=true;
            }
            params.singleGrid = true;
            this.ajxUrl = "ACCReports/getBSorPL_CustomLayout.do";

        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({url:this.ajxUrl,params:params}, this, this.successCallback, this.failureCallback);
    },

    successCallback:function(response){
        WtfGlobal.resetAjaxTimeOut();
        if(response.success){
            this.total=response.data.total;
            if (response.data.pretotal != undefined && this.statementType!="TrialBalance") {
                this.total[2] = response.data.pretotal[0];
                this.total[3] = response.data.pretotal[0];
            } 
            this.lGrid.store.loadData(response.data);
            this.doLayout();
//            if (this.firstTime && (this.statementType == 'BalanceSheet'|| this.statementType=="TradingAndProfitLoss")) {
////                var leftGrid = this.lGrid.colModel.config[2];
//                
////                var openingAmt = this.lGrid.colModel.config[1];
////                var periodAmt = this.lGrid.colModel.config[2];                
////                var endingAmt = this.lGrid.colModel.config[3];
////                
//                var preopeningAmt = this.lGrid.colModel.config[4];                
//                var preperiodAmt = this.lGrid.colModel.config[5];
//                var preendingAmt = this.lGrid.colModel.config[6];
//                
//                if (preendingAmt != undefined) {
//                    preopeningAmt.hidden = true;
//                    preperiodAmt.hidden = true;
//                    preendingAmt.hidden = true;
//                }
//                this.lGrid.getView().refresh(true);
//            }
            this.firstTime = false;
            this.collapseGrids();
            
        }
        this.hideLoading();
        
        var msg = "<b>Note:</b> Balances won't matched, if all account's are not mapped. <br>" + response.msg;
        var isdontshowmsg = response.dontshowmsg;
        var dontMsg = "<div id = 'check-"+this.templateid+"' style='padding:10px;'></div>";
        /**
         * Size of alert box is large than desktop screen thats why we are not able to view ERP application and it shows blank screen.
         * Because of this added ellipsis. SDP-10141
         */
        if(!isdontshowmsg){
            if(msg==="All accounts are mapped in current Template"){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg+dontMsg],2);
            }else{
                Wtf.MessageBox.show({
                    title : WtfGlobal.getLocaleText("acc.common.alert"),
                    msg : WtfGlobal.getLocaleText("acc.field.Accounts")+"<br/>"+Wtf.util.Format.ellipsis(msg,1500)+"<br/>"+WtfGlobal.getLocaleText("acc.customelayout.arenotmappedincurrentTemplate")+dontMsg,
                    width : 420,
                    fn : function(btnText){
                        if(btnText == "ok" && (this.dontShowMsgCheckbox.getValue() == "on" || this.dontShowMsgCheckbox.getValue() == true)){
                            Wtf.Ajax.requestEx({
                                url :"ACCReports/setDontShowFlagCustomLayout.do",
                                params :{
                                    templateid : this.templateid 
                                }
                            }, this);
                        }
                    }.createDelegate(this),
                    buttons: Wtf.MessageBox.OK,
                    animEl: 'mb9',
                    icon: Wtf.MessageBox.WARNING
                });
                this.dontShowMsgCheckbox = new Wtf.form.Checkbox({
                    name:'dontshow',
                    hideLabel:true,
                    boxLabel :"Don't show this message again."
                });
                this.dontShowMsgCheckboxForm = new Wtf.form.FormPanel({
                    renderTo:"check-"+this.templateid,
                    border : false,
                    items:[this.dontShowMsgCheckbox]
                });
            }
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
//        if(this.statementType=='BalanceSheet'&&start)
//             return new Date('January 1, 1970 00:00:00 AM');
        var monthDateStr=d.format('M d');
//        if(Wtf.account.companyAccountPref.fyfrom&&this.statementType!='BalanceSheet')
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
//        if(start||this.statementType=='BalanceSheet')
        if(start)
            return fd;
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },

   getPreDates:function(start){      
        return start.add(Date.YEAR, -1);
    },
   fetchCompareStatement:function(){
    this.fetchStatement(true);
    },        
    showLastRec:function(pos){
        return WtfGlobal.currencySummaryRenderer(this.total[pos]);
    },
    showAdvanceSearch: function() {
        showAdvanceSearch(this, this.searchparam, this.filterAppend);
    },
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
        
    },
    filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.fetchStatement();
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
       this.fetchStatement();
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();


    },    
     storeLoad: function() {
       this.fetchStatement();
        this.doLayout();


    }     
});
