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

function monthlyBalanceSheetDynamicLoad(){
    var panel = Wtf.getCmp("newMonthlyBalanceSheet");
    if(panel==null){
        panel = new Wtf.account.MonthlyBalanceSheet({
            id : 'newMonthlyBalanceSheet',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.MonthlyBalanceSheet.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.MonthlyBalanceSheetTT.tabTitleTT"),
            topTitle:'<center><font size=4>' + WtfGlobal.getLocaleText("acc.MonthlyBalanceSheet.tabTitle") + '</font></center>',
            statementType:'BalanceSheet',
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
//*******************************************************************************************************

Wtf.account.MonthlyBalanceSheet=function(config){
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
        {name: 'amount_11'},
        {name: 'amount_12'},
        {name: 'amount_13'},
        {name: 'amount_14'},
        {name: 'amount_15'},
        {name: 'amount_16'},
        {name: 'amount_17'},
        {name: 'amount_18'},
        {name: 'accountflag'},
        {name: 'isdebit',type:'boolean'},
        {name: 'level'},
        {name: 'fmt'},
        {name: 'leaf'},
        {name: 'totalFlagAccountsWithchild'}
    ]);

    this.statementType=config.statementType||"Trading";

    this.lStroe = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "left"                            // Assets on Left side in Balance Sheet         
        },this.FinalStatementRec),
        baseParams:{
            nondeleted:true
        },
        url: Wtf.req.account+'CompanyManager.jsp'
    });
//    this.lStroe.on('load', this.hideLoading, this);
//    this.lStroe.on('loadexception', this.hideLoading, this);
    
    this.rStroe = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "right"                           //  Liabilities on right side in Balance Sheet        
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
            summaryRenderer:function(){
                return WtfGlobal.summaryRenderer(WtfGlobal.getLocaleText("acc.common.total"));
            }.createDelegate(this)
        });
        
    if(Wtf.account.companyAccountPref.showaccountcodeinfinancialreport){
        columnArr.push({
            header:'<b>'+WtfGlobal.getLocaleText("acc.coa.accCode")+'</b>',
            dataIndex:'accountcode',
            //            sortable: true,
            //            hidden:!Wtf.account.companyAccountPref.showaccountcodeinfinancialreport,
            //            renderer:this.formatAccountName,
            width:100,
            align: 'center'
        });
    }

    for(var i=0; i<18; i++){

        columnArr.push({
                    hidden: false,
                    dataIndex: 'amount_'+i,      
                    // renderer:this.formatMoney,  
                    renderer:this.formatData,  
                    width: 80,
                    pdfwidth: 80,
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
            forceFit:false,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn")),    //ERP-28938
            deferEmptyText: false
        }
    });
    this.lGrid.on("render", WtfGlobal.autoApplyHeaderQtip);
    this.lGrid.on('rowclick',this.onRowClickLGrid, this);
//    this.lGrid.on('cellclick',this.onCellClick, this);

    this.rGrid = new Wtf.grid.HirarchicalGridPanel({
        plugins:[this.summaryR],
        autoScroll:true,
        store: this.rStroe,
        hirarchyColNumber:0,
        columns: columnArr,      
        border : false,
        loadMask : true,
        viewConfig: {
            forceFit:false,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn")),    //ERP-28938
            deferEmptyText: false
        }
    });
    this.rGrid.on("render", WtfGlobal.autoApplyHeaderQtip);
    this.lGrid.view.refresh.defer(1, this.lGrid.view); 
    this.rGrid.view.refresh.defer(1, this.rGrid.view); 
    this.rGrid.on('rowclick',this.onRowClickRGrid, this);
//    this.rGrid.on('cellclick',this.onCellClick, this);

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
        // this.lGrid.getColumnModel().setRenderer(monthCount, this.formatMoney) ;       

        // show those months with data
        for(var i=(monthCount+startIndex); i<columnCount; i++){
            this.lGrid.getColumnModel().setHidden(i,true) ;
        } 

        this.Lcm = this.lGrid.getColumnModel();
        // this.Rcm = this.rGrid.getColumnModel();

        // this.Rcm.setRenderer(monthCount, this.formatMoney) ;
        this.Lcm.setRenderer(monthCount, this.formatMoney) ;

        this.lstore1 = this.lGrid.getStore();
        // this.rstore1 = this.rGrid.getStore();

        this.lGrid.reconfigure(this.lstore1,this.Lcm);
        // this.rGrid.reconfigure(this.rstore1,this.Rcm);        

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
            this.lGrid.getColumnModel().setColumnWidth((i+startIndex), 150) ;            
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

        this.lGrid.loadMask = new Wtf.LoadMask(this.lGrid.body, {
            msg : WtfGlobal.getLocaleText("acc.msgbox.50"),
            msgCls :"loading-mask"
        });
        
        this.lGrid.reconfigure(this.lstore1,this.Lcm);
        
        this.lGrid.getView().onColumnSplitterMoved(1,150);
        
        this.rGrid.reconfigure(this.rstore1,this.Rcm);  
        
        this.expandCollapseGrid("Collapse");

    }, this);
   
    // to change this with the month & year drop-down list
    this.monthStore = new Wtf.data.SimpleStore({
            fields: [{name:'monthid',type:'int'}, 'name'],
            data :[[0,"January"],[1,"February"],[2,"March"],[3,"April"],[4,"May"],[5,"June"],[6,"July"],[7,"August"],[8,"September"],[9,"October"],
                [10,"November"],[11,"December"]]
    });

    var data=WtfGlobal.getBookBeginningYear(true);
    
    this.yearStore= new Wtf.data.SimpleStore({
            fields: [{name:'id',type:'int'}, 'yearid'],
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
            width:90,
            triggerAction: 'all',
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

    this.endMonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  //'Month',
            name:'endMonth',
            displayField:'name',
            forceSelection: true,
            anchor:'95%',
            valueField:'name',
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
            triggerAction: 'all',
            width:90,
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

    var btnArr=[];

    btnArr.push(
        (config.statementType=='BalanceSheet'?'':WtfGlobal.getLocaleText("acc.common.from")),
        this.startMonth, this.startYear,
        (config.statementType=='BalanceSheet'?WtfGlobal.getLocaleText("acc.balanceSheet.AsOn"):WtfGlobal.getLocaleText("acc.common.to")),
        this.endMonth, this.endYear
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
    btnArr.push('-',this.resetBttn);
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
 
    btnArr.push(this.ToggleButton=new Wtf.Button({          // Used for toggling assets and liabilities from Left to Right & Vice versa
            text:WtfGlobal.getLocaleText("acc.balanceSheet.toggle"),  //'Toggle',
            iconCls:'pwnd toggleButtonIcon',
            tooltip :(this.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.toggleTT"):WtfGlobal.getLocaleText("acc.balanceSheet.toggleTT")),
            scope: this,
            handler: this.swapGrids,
            hidden:true//(config.statementType=="BalanceSheet"?false:true)
        }));

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
        cm: this.grid.colModel,
        ignoreDefaultFields : true,
        dimensionOnly : true,
        moduleid:102, //Added module id for balancesheet search report
        advSearch: false
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        scope: this,       
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    btnArr.push('-', this.AdvanceSearchBtn);

    var mnuBtns1=[];
    var xlsbtn1=new Wtf.Action({
        iconCls:'pwnd '+'exportcsv',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"</span>",
        scope: this,
        handler:function(){
            this.exportWithTemplate('dimMonthlyXls')
        }
    });
    mnuBtns1.push(xlsbtn1)
    btnArr.push('-',this.dimBasedExportButton=new Wtf.Button({
        text:WtfGlobal.getLocaleText("acc.report.DimensionBasedExport"),  //'Export',
        iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export'),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details.',
        scope: this,
        menu:mnuBtns1
    }));
    this.dimBasedExportButton.on('click',function(){
        var isExportButtonClicked=true;
        this.objsearchComponent.advGrid.doSearch('',isExportButtonClicked);
        isExportButtonClicked=false;
        
    },this);

    this.wrapperPanel = new Wtf.Panel({
        border:false,
        layout:"border",
        scope:this,
        items:[this.objsearchComponent,
            this.westPanel = new Wtf.Panel({
                title:WtfGlobal.getLocaleText("acc.MonthlyBS.MonthlyBSReportHelpMsg"),
                width:'98%',
                region:'center',
                layout:'fit',
                border:false,
                items:this.lGrid
            }),
            {layout:'fit',region:'west',width:'0%'},
            {layout:'fit',region:'east',width:'0%'}
        ]
    });

    Wtf.apply(this,{
        defaults:{border:false,bodyStyle:"background-color:white;"},
        saperate:true,
        statementType:"Trading",
        items:this.wrapperPanel,
        tbar:btnArr
    },config);

     Wtf.account.MonthlyBalanceSheet.superclass.constructor.call(this,config);
     this.addEvents({
        'account':true
     });
//     if(this.statementType!="CostCenter"){
//        this.fetchStatement();
//     }
     }

Wtf.extend(Wtf.account.MonthlyBalanceSheet,Wtf.Panel,{
    onRowClickRGrid:function(g,i,e){
        // e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var accid=this.rGrid.getStore().getAt(i).data['accountid'];

    },

    onRowClickLGrid:function(g,i,e){
        // alert('onRowClickLGrid');
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        
        var elementId = el.id;
        
        if(elementId == 'anc'){
//            this.openLedgerForCellDur(i);
            return;
        }
        
        var accid=this.lGrid.getStore().getAt(i).data['accountid'];   
        
        var startMonthName = this.startMonth.getValue();
        var startYear = this.startYear.getValue();

        var startDate = new Date(Date.parse(startMonthName + " 01" + ", "+ startYear));
        
        var endMonthName = this.endMonth.getValue();
        var endYearName = this.endYear.getValue();
        
        var endMonthId = this.monthStore.getAt(this.monthStore.find('name', this.endMonth.getValue())).get('monthid');
        
        var isLeap = ((endYearName % 4) == 0 && ((endYearName % 100) != 0 || (endYearName % 400) == 0));
        var daysInMonth = [31, (isLeap ? 29 : 28), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][endMonthId];

        var endDate = new Date(Date.parse(endMonthName + " " + daysInMonth  + ", "+ endYearName));
        
        this.fireEvent('account',accid,startDate,endDate);
    },

    onCellClick:function(g,i,j,e){
        e.stopEvent();
        
        var el=e.getTarget("a");
//        if(el==null)return;

        if (i>1){
            var lGridStore = this.lGrid.getStore();
            var rGridStore = this.rGrid.getStore();

            var accid=lGridStore.getAt(i).data['accountid'];         
            
            var monthArray = rGridStore.data.items[rGridStore.data.length-1].json["months"];
            
            if(j == 0){
                this.openLedgerForWholeDur(i);
                return;
            }
            
            if (j == (monthArray.length))
                return;

            var monthName = monthArray[j-1]["monthname"];

            var dateParts = monthName.split(" ");
            var month = dateParts[0];
            var year = dateParts[1];
            
            var startDate = new Date(Date.parse(month + " 01" + ", "+ year));
            var isLeap = ((year % 4) == 0 && ((year % 100) != 0 || (year % 400) == 0));
            var daysInMonth = [31, (isLeap ? 29 : 28), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][startDate.getMonth()];
            
            var endDate = new Date(Date.parse(month + " " + daysInMonth  + ", "+ year));
            this.fireEvent('account',accid,startDate,endDate);
        }        
    },    
    
    openLedgerForWholeDur:function(i){
        var accid=this.lGrid.getStore().getAt(i).data['accountid'];   

        var startMonthName = this.startMonth.getValue();
        var startYear = this.startYear.getValue();

        var startDate = new Date(Date.parse(startMonthName + " 01" + ", "+ startYear));

        var endMonthName = this.endMonth.getValue();
        var endYearName = this.endYear.getValue();

        var endMonthId = this.monthStore.getAt(this.monthStore.find('name', this.endMonth.getValue())).get('monthid');

        var isLeap = ((endYearName % 4) == 0 && ((endYearName % 100) != 0 || (endYearName % 400) == 0));
        var daysInMonth = [31, (isLeap ? 29 : 28), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][endMonthId];

        var endDate = new Date(Date.parse(endMonthName + " " + daysInMonth  + ", "+ endYearName));

        this.fireEvent('account',accid,startDate,endDate);
    },
    
    openLedgerForCellDur:function(i){
        var lGridStore = this.lGrid.getStore();
        var rGridStore = this.rGrid.getStore();

        var accid=lGridStore.getAt(i).data['accountid'];         
            
        var monthArray = rGridStore.data.items[rGridStore.data.length-1].json["months"];
            
        var monthName = monthArray[j-1]["monthname"];

        var dateParts = monthName.split(" ");
        var month = dateParts[0];
        var year = dateParts[1];
            
        var startDate = new Date(Date.parse(month + " 01" + ", "+ year));
        var isLeap = ((year % 4) == 0 && ((year % 100) != 0 || (year % 400) == 0));
        var daysInMonth = [31, (isLeap ? 29 : 28), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][startDate.getMonth()];
            
        var endDate = new Date(Date.parse(month + " " + daysInMonth  + ", "+ year));
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
        var searchJson = this.searchJson;

        if(this.statementType=="BalanceSheet"){
            exportUrl = getExportUrl(Wtf.autoNum.MonthlyBalanceSheet);
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

            extraParams += "&costcenter="+this.costCenter.getValue();
            extraParams += "&reportView="+this.statementType;

            get = 28;
            jsonGrid = "{data:[{'header':'laccountname','title':'Particulars','width':'150','align':''},"+
            "{'header':'lamount','title':'Amount(Debit)','width':'150','align':'currency'},"+
            "{'header':'raccountname','title':'Particulars','width':'150','align':''},"+
            "{'header':'ramount','title':'Amount(Credit)','width':'150','align':'currency'}]}";
            header =  fileName;
        }
        extraParams+="&searchJson="+(searchJson!=undefined?searchJson:"")+"&filterConjuctionCriteria="+this.filterConjuctionCrit;

        // get date from month & year drop-down lists
        if (this.startMonth.getValue() == "" || this.startYear.getValue() == ""){
            this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
            this.startYear.setValue(this.yearStore.data.items[0].json[1]);
            this.endMonth.setValue(this.monthStore.data.items[this.monthStore.data.items.length-1].json[1]);   
            this.endYear.setValue(this.yearStore.data.items[0].json[1]);
        }

        this.sDate = this.startMonth.getValue() + ", " + this.startYear.getValue();
        this.eDate = this.endMonth.getValue() + ", " + this.endYear.getValue();

        var configstr = "{%22landscape%22:%22true%22,%22pageBorder%22:%22true%22,%22gridBorder%22:%22true%22,%22title%22:%22Test%20Title%22,%22subtitles%22:%22%22,%22headNote%22:%22Test%20Header%22,%22showLogo%22:%22false%22,%22headDate%22:%22false%22,%22footDate%22:%22true%22,%22footPager%22:%22false%22,%22headPager%22:%22true%22,%22footNote%22:%22Test%20Footer%22,%22textColor%22:%22000000%22,%22bgColor%22:%22FFFFFF%22}"
        var url = exportUrl+"?filename="+encodeURIComponent(fileName)+extraParams+"&config="+configstr+"&header"+header+
                "&filetype=pdf&stdate="+
                this.sDate+"&enddate="+this.eDate+
                "&toggle="+this.toggle+"&get="+get+"&gridconfig="+encodeURIComponent(jsonGrid);

        Wtf.get('downloadframe').dom.src = url;
    },

    resetFilterAndFetchReport: function(costCenterId, startDate, endDate){
        this.costCenter.setValue(costCenterId);

        // get date from month & year drop-down lists
        if (this.startMonth.getValue() == "" || this.startYear.getValue() == ""){
            this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
            this.startYear.setValue(this.yearStore.data.items[0].json[1]);
            this.endMonth.setValue(this.monthStore.data.items[this.monthStore.data.items.length-1].json[1]);   
            this.endYear.setValue(this.yearStore.data.items[0].json[1]);
        }

        this.sDate = this.startMonth.getValue() + ", " + this.startYear.getValue();
        this.eDate = this.endMonth.getValue() + ", " + this.endYear.getValue();

        // check that from date and end date must be available
        if(this.sDate=="" || this.eDate=="") {
           WtfComMsgBox(42,2);
           return;
        }      

        // check that start date must be before end date
        var startMonthDate = new Date(this.startMonth.getValue() + " 01, " + this.startYear.getValue());
        var endMonthDate = new Date(this.endMonth.getValue() + " 01, " + this.endYear.getValue());
        
        if (startMonthDate.getMonth() > endMonthDate.getMonth()){
            WtfComMsgBox(1,2);
            return;            
        }        

        this.fetchStatement();
    },

    exportWithTemplate:function(type){
        type = type?type:"csv";
        var exportUrl;
        var fileName;
        var reportName;
        var header = "accountname";
        var isDimMonthlyBS=false;
        var searchJson = this.searchJson;
        if(type=="dimMonthlyXls"){
            searchJson = this.objsearchComponent.advGrid.getStoreSearchJson(true);
            if(searchJson==undefined || searchJson==null || searchJson==""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.responsealert.msg.14")], 2);
                return;
            } 
            isDimMonthlyBS=true;
            type="xls";
        } 
        // not sure if we need to change to lGrid.getStore() but so far rGrid.getStore() does work!
//        var store = this.rGrid.getStore();
//        var monthArray = store.data.items[store.data.length-1].json["months"];

//        for(var i=0; i<monthArray.length; i++){            
//            header += (",amount_" + i);
//        }
        
        var title = "";
        var extraParams = "";

        // SON REFACTOR - NEED TO CENTRALIZE THE STATEMENTTYPE IN ONE PLACE LIKE AutoNum.WtfSettings
        if(this.statementType=="BalanceSheet"){
            if(this.toggle == 0){
                //title = WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.18")+","+WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.19");
//                title = WtfGlobal.getLocaleText("acc.report.2");
//            
//                for(var i=0; i<monthArray.length; i++){            
//                    title += (", " + monthArray[i]["monthname"]);
//                }
            }else{
                title = WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.19")+","+WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.18");
            }
            //header = "raccountname,ramount,laccountname,lamount";
            //header = "accountname,amount_0,amount_1,amount_2,amount_3,amount_4,amount_5,amount_6,amount_7,amount_8,amount_9,amount_10,amount_11,amount_12";
            if(isDimMonthlyBS){
                exportUrl = getExportUrl(Wtf.autoNum.DimensionBasedMonthlyBS);
                fileName =  WtfGlobal.getLocaleText("acc.report.DimBasedMonthlyBSfileName")+"_v1";
                reportName = WtfGlobal.getLocaleText("acc.MonthlyBalanceSheet.tabTitle"); 
            } else {
            exportUrl = getExportUrl(27);   
            fileName = WtfGlobal.getLocaleText("acc.balanceSheet");
            reportName = WtfGlobal.getLocaleText("acc.balanceSheet");
            }

        }else {
//            title = WtfGlobal.getLocaleText("acc.report.2");
//            
//            for(var i=0; i<monthArray.length; i++){            
//                title += (", " + monthArray[i]["monthname"]);
//            }
            
            exportUrl = getExportUrl(Wtf.autoNum.MonthlyTradingPnl);
            
            if(this.statementType=="MonthlyTradingAndProfitLoss"){

                fileName =  WtfGlobal.getLocaleText("acc.MonthlyP&L.tabTitle");
                reportName = WtfGlobal.getLocaleText("acc.MonthlyP&L.tabTitle");

            } else if (this.statementType=="CostCenter"){
                fileName = WtfGlobal.getLocaleText("acc.ccReport.tabTitle");
                reportName = WtfGlobal.getLocaleText("acc.ccReport.tabTitle");
            }            
        }

        if (this.startMonth.getValue() == "" || this.startYear.getValue() == ""){
            this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
            this.startYear.setValue(this.yearStore.data.items[0].json[1]);
            this.endMonth.setValue(this.monthStore.data.items[this.monthStore.data.items.length-1].json[1]);   
            this.endYear.setValue(this.yearStore.data.items[0].json[1]);
        }

        this.sDate = this.startMonth.getValue() + ", " + this.startYear.getValue();
        this.eDate = this.endMonth.getValue() + ", " + this.endYear.getValue();

        extraParams += "&costcenter="+((this.costCenter!=null || this.costCenter!=undefined)?this.costCenter.getValue():"");
        extraParams += "&reportView="+this.statementType;        
        if(this.statementType=="BalanceSheet"){
            if(isDimMonthlyBS){
                extraParams+="&dimensionBasedSearchJson="+(searchJson!=undefined?searchJson:"")+"&filterConjuctionCriteria="+this.filterConjuctionCrit;
            }else{
                extraParams+="&searchJson="+(searchJson!=undefined?searchJson:"")+"&filterConjuctionCriteria="+this.filterConjuctionCrit;
            }
        }

        var align = "none";

//        for(var i=0; i<monthArray.length; i++){
//            align += ",currency";
//        }        

        var url = exportUrl+"?filename="+encodeURIComponent(fileName)+
                    extraParams+"&filetype="+type
                    +"&stdate="+this.sDate+"&enddate="+this.eDate
                    +"&nondeleted="+true+"&accountid="
                    +"&mode=66&monthlyBS=true&header="+header+"&title="+encodeURIComponent(title)+"&name="+encodeURIComponent(reportName)+"&width=150&get=27&toggle="+this.toggle+"&singleGrid="+true+"&dimensionBasedMonthlyBS="+isDimMonthlyBS+"&isMonthlyReport="+true;

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
            case "T":colorCss=" grey-background";break;
            case "B":colorCss=" red-background";break;
            case "H":colorCss=" header-background";break;
            case "A":colorCss=" darkyellow-background";break;
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

//        if(rec.data.accountflag) {
//            fmtVal = '<span style="color: #083772">'+fmtVal+'</span>';
//        }

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

    fetchStatement:function(isExportButtonClicked){
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        if (!(isExportButtonClicked === true)) {
        WtfComMsgBox(29,4,true); //Show loading mask
        }
        
        if(this.startYear.getValue()>this.endYear.getValue()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 3); // "From Date can not be greater than To Date."
            return;
        }
        // get date from month & year drop-down lists
        if (this.startMonth.getValue() == "" || this.startYear.getValue() == ""){
            this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
            this.startYear.setValue(this.yearStore.data.items[0].json[1]);
            this.endMonth.setValue(this.monthStore.data.items[this.monthStore.data.items.length-1].json[1]);   
            this.endYear.setValue(this.yearStore.data.items[0].json[1]);
        }

        this.sDate = this.startMonth.getValue() + ", " + this.startYear.getValue();
        this.eDate = this.endMonth.getValue() + ", " + this.endYear.getValue();

        if(this.sDate=="" || this.eDate=="") {
           WtfComMsgBox(42,2);
           return;
        }      

        var startMonthDate = new Date(this.startMonth.getValue() + " 01, " + this.startYear.getValue());
        var endMonthDate = new Date(this.endMonth.getValue() + " 01, " + this.endYear.getValue());        

        if (this.startYear.getValue() >= this.endYear.getValue() && startMonthDate.getMonth() > endMonthDate.getMonth()){
            WtfComMsgBox(1,2);
            return;            
        }

        var months;
        months = (endMonthDate.getFullYear() - startMonthDate.getFullYear()) * 12;
        months -= startMonthDate.getMonth();
        months += endMonthDate.getMonth();
        if (months<0)
            months=0;

        if (months>18){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Onlymaximum18monthsaresupported") ], 2);
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
            params.reportView= this.statementType;
            if(this.statementType != "CostCenter")
              params.singleGrid = true;
            this.ajxUrl = "ACCReports/getMonthlyTradingAndProfitLoss.do";
        }

        if(this.statementType=="BalanceSheet") {
            //Advance Search Params
            params.searchJson= this.searchJson!=undefined?this.searchJson:"";
            params.filterConjuctionCriteria= this.filterConjuctionCrit
            
            params.mode=66;
            params.nondeleted=true;
            this.ajxUrl = "ACCReports/getNewBalanceSheet.do";
        }
        if (!(isExportButtonClicked === true)) {
        WtfGlobal.setAjaxTimeOut();
            Wtf.Ajax.requestEx({
                url:this.ajxUrl,
                params:params
            }, this, this.successCallback, this.failureCallback);
        }
    },
    handleResetClick:function(){
       this.startYear.reset();
       this.startMonth.reset();
       this.endMonth.reset();
       this.endYear.reset();
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
            if((this.statementType!="BalanceSheet" && this.total && this.total[0]==0 && this.total[1]==0)
                ||(this.statementType=="BalanceSheet" && this.lGrid.store.getCount()<=3 && this.rGrid.store.getCount()<=3)){
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
//
//       var data=[];
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
    },
    
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
    },
    
    filterStore: function(json, filterConjuctionCriteria, isExportButtonClicked) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.fetchStatement(isExportButtonClicked);
    },
    
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.fetchStatement();
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    }
});
