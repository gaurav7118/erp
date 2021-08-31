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


function AccountForecastReportDetailsDynamicLoad(){
    var panel = Wtf.getCmp("monthlyforecastreportdetails");
    if(panel==null){
        panel = new Wtf.TabPanel({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.forecast.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.forecast.tabTitle"),  // "Account Forecast Report",
            id:'monthlyforecastreportdetails',
            closable:true,
            border:false,
            iconCls:'accountingbase vendor',
            activeTab:0
        });
        Wtf.getCmp('as').add(panel);
        AccountForecastReport();
        AccountForecastmonthtodateReport();
        AccountForecastyeartodateReport();
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function AccountForecastReport(consolidateFlag, withinventory){
    consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;

    var panel=Wtf.getCmp('monthlyforecastreport');
    if(panel==null){
        panel = new Wtf.account.AccountForecastReport({
            id: 'monthlyforecastreport',
            border: false,
            helpmodeid:70,
            consolidateFlag:consolidateFlag,
            withinventory: withinventory,
            layout: 'fit',
            isYearly:false,
            iconCls: 'accountingbase agedrecievable',
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.forecast.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.forecast.tabTitle"),  // "Account Forecast Report",
            receivable:true,
            monthlysalesreport:true
//            closable: true
        });
        Wtf.getCmp('monthlyforecastreportdetails').add(panel);
    }

    Wtf.getCmp('monthlyforecastreportdetails').setActiveTab(panel);
    Wtf.getCmp('monthlyforecastreportdetails').doLayout();
}

function AccountForecastmonthtodateReport(consolidateFlag, withinventory){
    consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;

    var monthtodatepanel=Wtf.getCmp('monthlyforecastmonthtodatereport');
    if(monthtodatepanel==null){
        monthtodatepanel = new Wtf.account.AccountForecastMonthToDateReport({
            id: 'monthlyforecastmonthtodatereport',
            border: false,
            helpmodeid:71,
            consolidateFlag:consolidateFlag,
            withinventory: withinventory,
            layout: 'fit',
            isYearly:false,
            iconCls: 'accountingbase agedrecievable',
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.forecast.tabTitle.monthtodate"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.forecast.tabTitle.monthtodate"),  // "Month to date",
            receivable:true,
            monthlysalesreport:true
//            closable: true
        });
        Wtf.getCmp('monthlyforecastreportdetails').add(monthtodatepanel);
    }

    Wtf.getCmp('monthlyforecastreportdetails').doLayout();
}

function AccountForecastyeartodateReport(consolidateFlag, withinventory){
    consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;

    var yeartodatepanel=Wtf.getCmp('monthlyforecastyeartodatereport');
    if(yeartodatepanel==null){
        yeartodatepanel = new Wtf.account.AccountForecastMonthToDateReport({
            id: 'monthlyforecastyeartodatereport',
            border: false,
            helpmodeid:72,
            consolidateFlag:consolidateFlag,
            withinventory: withinventory,
            layout: 'fit',
            isYearly:true,
            iconCls: 'accountingbase agedrecievable',
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.forecast.tabTitle.yeartodate"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.forecast.tabTitle.yeartodate"),  // "Year to date",
            receivable:true,
            monthlysalesreport:true
//            closable: true
        });
        Wtf.getCmp('monthlyforecastreportdetails').add(yeartodatepanel);
    }

    Wtf.getCmp('monthlyforecastreportdetails').doLayout();
}
//***********************************************************************************************
Wtf.account.AccountForecastReport=function(config){
    this.receivable=config.receivable||false;
    this.withinventory=config.withinventory||false;
    this.isSummary=config.isSummary||false;

    this.summary = new Wtf.ux.grid.GridSummary();
    this.expander = new Wtf.grid.RowExpander({});
    this.accRec = Wtf.data.Record.create ([
        {name:'accountname',mapping:'accname'},
        {name:'accountid',mapping:'accid'},
        {name:'currencyid',mapping:'currencyid'},
        {name:'acccode'},
        {name:'groupname'}
    ]);

    this.uPermType=Wtf.UPerm.fstatement;
    this.permType=Wtf.Perm.fstatement;
    this.exportPermType=this.permType.exportdataledger;
    this.printPermType=this.permType.printledger;
    this.tabOpenFlag = true;
    this.colCount = 12;
    this.accStore = new Wtf.data.Store({
        url : "ACCAccount/getAccountsForCombo.do",
        baseParams:{
            mode:2,
            nondeleted:true,
            headerAdded:true,
            ignoreTransactionFlag:false
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.accRec)
    });
    this.cmbAccount=new Wtf.form.ExtFnComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.ledger.accName"),  // 'Account Name',
        id:'accountIdForCombo'+config.helpmodeid,
        name:'accountid',
        store:this.accStore,
        valueField:'accountid',
        displayField:'accountname',
        mode: 'local',
        typeAheadDelay:30000,
        extraComparisionField:'acccode', // type ahead search on acccode as well.
        minChars:1,
        width:150,
        listWidth:400,
        hiddenName:'accountid',
        emptyText:'Select Account',
        allowBlank:false,
        forceSelection:true,
        extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
        triggerAction:'all'
    });
    this.accStore.load();
    
    // to change this with the month & year drop-down list
    this.monthStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'monthid',
            type:'int'
        }, 'name'],
        data :[[0,'January'],[1,'February'],[2,'March'],[3,'April'],[4,'May'],[5,'June'],[6,'July'],[7,'August'],[8,'September'],[9,'October'],
        [10,'November'],[11,'December']]
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
        fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  // 'Month',
        name:'startMonth',
        displayField:'name',
        forceSelection: true,
        anchor:'95%',
        valueField:'name',
        mode: 'local',
        triggerAction: 'all',
        selectOnFocus:true,
        width:100
    });  

    this.startYear = new Wtf.form.ComboBox({
        store: this.yearStore,
        fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  // 'Year',
        name:'startYear',
        displayField:'yearid',
        anchor:'95%',
        valueField:'yearid',
        forceSelection: true,
        mode: 'local',
        width:100,
        triggerAction: 'all',
        selectOnFocus:true
    });  

    this.endMonth = new Wtf.form.ComboBox({
        store: this.monthStore,
        fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  // 'Month',
        name:'endMonth',
        displayField:'name',
        forceSelection: true,
        anchor:'95%',
        valueField:'name',
        mode: 'local',
        triggerAction: 'all',
        selectOnFocus:true,
        width:100
    }); 

    this.endYear = new Wtf.form.ComboBox({
        store: this.yearStore,
        fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  // 'Year',
        name:'endYear',
        displayField:'yearid',
        anchor:'95%',
        valueField:'yearid',
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        width:100,
        selectOnFocus:true
    });       

    if (config.sMonth!=null && config.sMonth!= "")
        this.startMonth.setValue(config.sMonth);

    if (config.sYear!=null && config.sYear!= "")
        this.startYear.setValue(config.sYear);

    if (config.eMonth!=null && config.eMonth!= "")
        this.endMonth.setValue(config.eMonth);   

    if (config.eYear!=null && config.eYear!= "")
        this.endYear.setValue(config.eYear);

    this.uPermType=Wtf.UPerm.invoice;
    this.permType=Wtf.Perm.invoice;
    this.exportPermType=(this.receivable?this.permType.exportdataagedreceivable:this.permType.exportdataagedpayable);
    this.printPermType=(this.receivable?this.permType.printagedreceivable:this.permType.printagedpayable);
    this.chartPermType=(this.receivable?this.permType.chartagedreceivable:this.permType.chartagedpayable);
		
    this.MonthlySalesRecord = new Wtf.data.Record.create([
        {name: 'monthname'},
        {name: 'actualbalance'},
        {name: 'monthlybudget'},
        {name: 'forecastbudget'},
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
        {name: 'amount_17'}
    ]);
	
    this.expGet = Wtf.autoNum.AccountForecast;

    this.MonthlySalesStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.MonthlySalesRecord),
        url: "ACCReports/getAccountForecastReport.do",
        baseParams:{
            mode: 18, // TODO - to review this
            creditonly:false,
            nondeleted:true,
            getRepeateInvoice: false,
            consolidateFlag:config.consolidateFlag,
            companyids:companyids,
            gcurrencyid:gcurrencyid,
            userid:loginid				
        }
    });

    this.typeStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'typeid',
            type:"boolean"
        }, 'name'],
        data :[[true,WtfGlobal.getLocaleText("acc.rem.127")],[false,WtfGlobal.getLocaleText("acc.rem.128")]]
    });

    this.typeEditor = new Wtf.form.ComboBox({
        store: this.typeStore,
        name:'isdistributive',
        displayField:'name',
        value:true,
        anchor:"50%",
        valueField:'typeid',
        mode: 'local',
        triggerAction: 'all'
    });
    this.createGrid();
    
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  // 'Reset',
        hidden:false,
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  // 'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });

    this.chart=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.chart"),  //'Chart',
        tooltip :WtfGlobal.getLocaleText("acc.graphTT.view"), // "Get the graphical view of", //+' '+WtfGlobal.getLocaleText("acc.graphTT.monthlySalesReport"),
        id: 'chartRec'+config.helpmodeid, // + this.id,
        scope: this,
        handler:this.getChart,
        iconCls :(Wtf.isChrome?'accountingbase chartChrome':'accountingbase chart')

    });
    
    // get date from month & year drop-down lists
    if (this.startMonth.getValue() == "" || this.startYear.getValue() == "") {
        var d=new Date();
        
        var month=new Array();
        month[0]="January";
        month[1]="February";
        month[2]="March";
        month[3]="April";
        month[4]="May";
        month[5]="June";
        month[6]="July";
        month[7]="August";
        month[8]="September";
        month[9]="October";
        month[10]="November";
        month[11]="December";
        
        var n = month[d.getMonth()]; 
        var startDatVal=this.getDates(true);
        var endDatVal=this.getDates(false);
        var startmonthDateStr=startDatVal.getMonth();
        var startyearDateStr=startDatVal.getFullYear();
        var endmonthDateStr=endDatVal.getMonth();
        var endyearDateStr=endDatVal.getFullYear();
        this.startMonth.setValue(month[startmonthDateStr]);
        this.startYear.setValue(startyearDateStr);
        this.endMonth.setValue(month[endmonthDateStr]);
        this.endYear.setValue(endyearDateStr);
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
    if (months < 0) {
        months = 0;
    }   

    if (months > 18) {
        WtfComMsgBox(["Alert", WtfGlobal.getLocaleText("acc.field.Onlymaximum18monthsaresupported")], 2); // 'Only maximum 18 months are supported!'
        return;            
    }
    
    this.expButton=new Wtf.exportButton({
        obj:this,
        id:'exportReports'+config.helpmodeid,
        text:WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.sales.exportTT"),  // 'Export report details',
        disabled :true,
        params:{ 
            startdate: this.sDate,
            enddate: this.eDate,			
            accountid:this.cmbAccount.getValue(),
            isdistributive:this.typeEditor.getValue()
        },
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,
            xls:true
        },
        get:this.expGet,
        filename: WtfGlobal.getLocaleText("acc.forecast.tabTitle") + "_v1"
    })
    this.printButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.print"),
        tooltip :WtfGlobal.getLocaleText("acc.sales.printTT"),  // 'Print report details',
        disabled :true,
        params:{ 	
            accountid:this.accountID||config.accountID,
            stdate: this.sDate,
            enddate: this.eDate,				
            isdistributive:this.typeEditor.getValue(),
            name: WtfGlobal.getLocaleText("acc.wtfTrans.monthlySalesReport")
        },
        label: WtfGlobal.getLocaleText("acc.sales.tabTitle"), // "Monthly Sales Report",
        menuItem:{
            print:true
        },
        get:this.expGet
    })
    var btnArr=[];
    btnArr.push( WtfGlobal.getLocaleText("acc.ledger.accName"),this.cmbAccount);


    btnArr.push('-',WtfGlobal.getLocaleText("acc.common.from"),
        // this.startDate
        this.startMonth, this.startYear
        );

    btnArr.push('-',WtfGlobal.getLocaleText("acc.common.to"),
        // this.endDate
        this.endMonth, this.endYear
        );

    btnArr.push("-",{
        xtype:'button',
        text:WtfGlobal.getLocaleText("acc.sales.fetch"),  //'Fetch',
        iconCls:'accountingbase fetch',
        scope:this,
        tooltip:WtfGlobal.getLocaleText("acc.sales.view"),  //"Select a date to view Monthly Sales Report
        handler:this.fetchMonthlySalesReport
    });
     btnArr.push(this.resetBttn);
//    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType))
        btnArr.push(this.expButton);
//    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType))
//        btnArr.push(this.printButton);
    this.resetBttn.on('click',this.handleResetClick,this);
    if(config.helpmodeid!=null){
        btnArr.push("->");
        btnArr.push(getHelpButton(this,config.helpmodeid));
    }
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        tbar:btnArr,
        items:[this.grid]
    });

    Wtf.account.MonthlySalesReport.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });

    this.MonthlySalesStore.on("beforeload", function(s,o) {
        o.params.stdate = this.sDate;
        o.params.enddate = this.eDate;
    },this);

    //this.MonthlySalesStore.on("load",this.storeloaded,this);
	
    this.accStore.on('load',function(){            
        var accountRec=this.accStore.getAt(1);
        this.cmbAccount.setValue(accountRec.data.accountid);
        this.MonthlySalesStore.load({
            params:{
                start:0,			
                isdistributive:this.typeEditor.getValue(),
                accountid:this.cmbAccount.getValue(),
                limit:30,
                creditonly:true
            }
        });
   	
    },this);

    
    this.MonthlySalesStore.on("load", function(store){
    
        // get month count from the first element
        var store = this.grid.getStore();
	
        var monthArray = store.data.items[0].json["months"];
        var monthCount = monthArray.length-1;

        var monthToHideStart = 6;
        for(var i=0; i<monthCount; i++){  
            this.grid.getColumnModel().setColumnHeader((i+monthToHideStart), '<div><b>'+monthArray[i]["monthname"]+'</b></div>') ;
        }

        var columnCount =  this.grid.getColumnModel().getColumnCount();

        // show those months with data
        for(var i=monthToHideStart; i<(monthToHideStart+monthCount); i++){
             this.grid.getColumnModel().config[i].hidden=false;
        }        

        // hide those months without data
        for(var i=(monthCount+monthToHideStart); i<columnCount; i++){
          this.grid.getColumnModel().config[i].hidden=true;
        }
        
        this.grid.getView().refresh(true);
        
        if(store.getCount()==0){
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
             
            if(this.expButton)this.expButton.disable();
            if(this.printButton)this.printButton.disable();
        } else {
            if(this.expButton)this.expButton.enable();
            if(this.printButton)this.printButton.enable();
        }
    },this);

    
	
}

Wtf.extend( Wtf.account.AccountForecastReport,Wtf.Panel,{  
    sumBaseAmount:function(dataindex,v,m,rec){       
        if(!this.isSummary){
            v=rec.data[dataindex];
            return WtfGlobal.withoutRateCurrencySymbol(v,m,rec)
        }
        return "";
    }, 
    groupDateRender:function(v){

        return v.format(WtfGlobal.getOnlyDateFormat())
    },
      opBalRenderer:function(val,m,rec){
        return WtfGlobal.withoutRateCurrencyDeletedSymbol(Math.abs(val),m,rec);
    },
    totalRender:function(v,m,rec){
        var val=WtfGlobal.withoutRateCurrencySymbol(v,m,rec);
        return "<b>"+val+"</b>"
    },
    formatMoney:function(val,m,rec,i,j,s){
        var fmtVal=WtfGlobal.currencyRenderer(val);
        if(rec.data['fmt']){
            fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
        }
        else if(rec.data["level"]==0&&rec.data["customername"]!="")
            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
        return fmtVal;
    },

    showLastRec:function(pos){
        return WtfGlobal.currencySummaryRenderer(this.total[pos]);
    },
    createGrid: function(pos) {
        
        this.rowNo = new Wtf.grid.RowNumberer();

        var columnArr = [];
        this.summary = new Wtf.ux.grid.GridSummary();
        var columnWidth = 80;
        var pdfWidth = 80;

        columnArr.push(new Wtf.grid.RowNumberer(), {
            hidden: true,
            dataIndex: 'monthname'
        }, {
            header: WtfGlobal.getLocaleText("acc.forecast.name"), // "Month Name""
            dataIndex: 'monthname',
            align: 'right',
            pdfwidth: 100,
            summaryRenderer: function() {
                return '<div class="grid-summary-common">' + WtfGlobal.getLocaleText("acc.common.total") + '</div>'
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.forecast.actualbalance"), // "Actual Balance"
            dataIndex: 'actualbalance',
            align:'right',
            summaryType: 'sum',
            pdfwidth: 100,
            renderer: WtfGlobal.currencyDeletedRenderer,
//        summaryRenderer:WtfGlobal.currencySummaryRenderer,
            summaryRenderer: function(value) {
                return "<div  title=' " + WtfGlobal.conventCurrencyDecimal(value, WtfGlobal.getCurrencySymbol()) + "'>" + WtfGlobal.currencySummaryRenderer(value) + "</div>";
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.forecast.budget"), // "Budget"
            dataIndex: 'monthlybudget',
            align: 'right',
            summaryType: 'sum',
            pdfwidth: 100,
            renderer: WtfGlobal.currencyDeletedRenderer,
            summaryRenderer: function(value) {
                return "<div title=' " + WtfGlobal.conventCurrencyDecimal(value, WtfGlobal.getCurrencySymbol()) + "'>" + WtfGlobal.currencySummaryRenderer(value) + "</div>";
            }
//        summaryRenderer:WtfGlobal.currencySummaryRenderer
        }, {
            header: WtfGlobal.getLocaleText("acc.forecast.forecast"), // "Forecast"
            dataIndex: 'forecastbudget',
            align: 'right',
            summaryType: 'sum',
            pdfwidth: 100,
            renderer: WtfGlobal.currencyDeletedRenderer,
            summaryRenderer: function(value) {
                return "<div title='  " + WtfGlobal.conventCurrencyDecimal(value, WtfGlobal.getCurrencySymbol()) + "'>" + WtfGlobal.currencySummaryRenderer(value) + "</div>";
            }
//        summaryRenderer:WtfGlobal.currencySummaryRenderer
        });

        for (var i = 0; i <= this.colCount; i++) {
            columnArr.push({
                hidden: false,
                dataIndex: 'amount_' + i,
                width: columnWidth,
                resizable: true,
                pdfwidth: 110,
                align: 'right',
                summaryType: 'sum',
                renderer: WtfGlobal.currencyDeletedRenderer,
                summaryRenderer: function(value) {
                    return "<div title='  " + WtfGlobal.conventCurrencyDecimal(value, WtfGlobal.getCurrencySymbol()) + "'>" + WtfGlobal.currencySummaryRenderer(value) + "</div>";
                },
//            summaryRenderer:WtfGlobal.currencySummaryRenderer,
                style: 'text-align:left'
            });
        }

        // column model
        this.gridcm = new Wtf.grid.ColumnModel(columnArr);

        this.grid = new Wtf.grid.GridPanel({
            stripeRows: true,
            store: this.MonthlySalesStore,
            cm: this.gridcm,
            ctCls: 'monthlySalesreport',
            border: false,
            id: 'abc',
            plugins: [this.summary, this.expander],
            layout: 'fit',
            viewConfig: {
                forceFit:true,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            },
            loadMask: true
        });
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
//        var data=[];
//        var newrec;
//        if(ffyear==null||ffyear=="NaN"){
//            ffyear=new Date(Wtf.account.companyAccountPref.fyfrom)
//            }
//        var year=ffyear.getFullYear();
//        var currentyear=new Date();
//        data.push([0,year])
//        if(!(ffyear.getMonth()==0&&ffyear.getDate()==1)){
//            data.push([1,year+1]);
//            newrec = new Wtf.data.Record({
//                id:1,
//                yearid:year+1
//                })
//        }
//        for(var addyear=year+1;addyear<currentyear.getFullYear();addyear++ ){
//         var year=addyear+1;
//         data.push([1,year])
//        }
//        if(!isfirst&&this.yearStore.getCount()<2){
//            this.yearStore.insert(1,newrec)
//        }
//        return data;
//    },     	

    fetchMonthlySalesReport:function(){
        var recNo = this.monthStore.find('name', this.startMonth.getValue());
        var rec = this.monthStore.getAt(recNo);
        var startMonthId = rec.get('monthid');
        
        recNo = this.monthStore.find('name', this.endMonth.getValue());
        rec = this.monthStore.getAt(recNo);
        var endMonthId = rec.get('monthid');
        this.colCount=endMonthId-startMonthId;
        this.grid.destroy();
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

        if (months>12){
            WtfComMsgBox(["Alert", WtfGlobal.getLocaleText("acc.field.Onlymaximum12monthsaresupported")], 2); // "Only maximum 12 months are supported!"
            return;            
        }  
     
     this.MonthlySalesStore.load({
            params:{
                isdistributive:this.typeEditor.getValue(),
                start:0,
                accountid:this.cmbAccount.getValue(),
                limit:30,
                creditonly:true
            }
        });
       this.createGrid();
       this.add(this.grid);
       this.doLayout();
        this.expButton.setParams({
            stdate: this.sDate,
            enddate: this.eDate,			
            isdistributive:this.typeEditor.getValue()
        });

        this.printButton.setParams({
            stdate: this.sDate,
            enddate: this.eDate,			
            isdistributive:this.typeEditor.getValue(),
            name: WtfGlobal.getLocaleText("acc.sales.tabTitle") // "Monthly Sales Report"
        })
    }, 
    onRowClick:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var accid=this.MonthlySalesStore.getAt(i).data['accountid'];
        this.fireEvent('account',accid);
    },

    getChart:function(){
        var chartid="monthlysalesreportchartid";
        var swf1="../../scripts/graph/krwcolumn/krwcolumn/krwcolumn.swf";
        var id1=this.receivable?"receivableid":"payableid"
        var dataflag1=this.receivable?"ACCInvoiceCMN/getAgedReceivableChart":"ACCGoodsReceiptCMN/getAccountPayableChart";
        var mainid=this.receivable?"mainAgedRecievable":"mainAgedPayable";
        var xmlpath1= this.receivable?'../../scripts/graph/krwcolumn/examples/AgesReceivable/agedreceivable_settings.xml':'../../scripts/graph/krwcolumn/examples/AgesPayable/agedpayable_settings.xml';
        var id2=this.receivable?"piereceivableid":"piepayableid"
        var swf2="../../scripts/graph/krwcolumn/krwpie/krwpie.swf";
        var dataflag2=this.receivable?"ACCInvoiceCMN/getAgedReceivablePie":"ACCGoodsReceiptCMN/getAgedReceivablePie";
        var xmlpath2= this.receivable?'../../scripts/graph/krwcolumn/examples/AgesReceivable/pieagedreceivable_settings.xml':'../../scripts/graph/krwcolumn/examples/AgesPayable/pieagedpayable_settings.xml';
        globalAgedChart(chartid,id1,swf1,dataflag1,mainid,xmlpath1,id2,swf2,dataflag2,xmlpath2,this.withinventory,true,false);
    },getDates:function(start){
        var d=new Date();
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
    handleResetClick:function() {
        this.accStore.load();
        this.grid.destroy();
        this.colCount=12;
        this.createGrid();
        this.add(this.grid);
        this.doLayout();
        this.startMonth.reset();
        this.startYear.reset();
        this.endMonth.reset();
        this.endYear.reset();
        this.sDate = this.startMonth.getValue() + ", " + this.startYear.getValue();
        this.eDate = this.endMonth.getValue() + ", " + this.endYear.getValue();
        
    }
          
});




Wtf.account.AccountForecastMonthToDateReport=function(config){
    this.receivable=config.receivable||false;
    this.withinventory=config.withinventory||false;
    this.isSummary=config.isSummary||false;
    this.isYearly=config.isYearly||false;
    this.summary = new Wtf.ux.grid.GridSummary();
    // this.summary = new Wtf.grid.GroupSummary({});
    this.expander = new Wtf.grid.RowExpander({});
    this.accRec = Wtf.data.Record.create ([
        {name:'accountname',mapping:'accname'},
        {name:'accountid',mapping:'accid'},
        {name:'currencyid',mapping:'currencyid'},
        {name:'acccode'},
        {name:'groupname'}
    ]);

    this.uPermType=Wtf.UPerm.fstatement;
    this.permType=Wtf.Perm.fstatement;
    this.exportPermType=this.permType.exportdataledger;
    this.printPermType=this.permType.printledger;
    this.tabOpenFlag = true;
    
    this.accStore = new Wtf.data.Store({
        url : "ACCAccount/getAccountsForCombo.do",
        baseParams:{
            mode:2,
            nondeleted:true,
            headerAdded:true,
            ignoreTransactionFlag:false
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.accRec)
    });
    
    this.cmbAccount=new Wtf.form.ExtFnComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.ledger.accName"),  // 'Account Name',
        id:'accountIdForCombo'+config.helpmodeid ,
        name:'accountid',
        store:this.accStore,
        valueField:'accountid',
        displayField:'accountname',
        mode: 'local',
        typeAheadDelay:30000,
        extraComparisionField:'acccode', // type ahead search on acccode as well.
        minChars:1,
        width:150,
        listWidth:400,
        hiddenName:'accountid',
        emptyText:'Select Account',
        allowBlank:false,
        forceSelection:true,
        extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
        triggerAction:'all'
    });
    this.accStore.load();
    
    // to change this with the month & year drop-down list
    this.monthStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'monthid',
            type:'int'
        }, 'name'],
        data :[[0,'January'],[1,'February'],[2,'March'],[3,'April'],[4,'May'],[5,'June'],[6,'July'],[7,'August'],[8,'September'],[9,'October'],
        [10,'November'],[11,'December']]
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
        fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  // 'Month',
        name:'startMonth',
        displayField:'name',
        forceSelection: true,
        anchor:'95%',
        valueField:'name',
        mode: 'local',
        width:100,
        triggerAction: 'all',
        selectOnFocus:true
    });  

    this.startYear = new Wtf.form.ComboBox({
        store: this.yearStore,
        fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  // 'Year',
        name:'startYear',
        displayField:'yearid',
        anchor:'95%',
        valueField:'yearid',
        forceSelection: true,
        mode: 'local',
        width:100,
        triggerAction: 'all',
        selectOnFocus:true
    });  

    this.endMonth = new Wtf.form.ComboBox({
        store: this.monthStore,
        fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  // 'Month',
        name:'endMonth',
        displayField:'name',
        forceSelection: true,
        anchor:'95%',
        valueField:'name',
        mode: 'local',
        width:100,
        triggerAction: 'all',
        selectOnFocus:true
    }); 

    this.endYear = new Wtf.form.ComboBox({
        store: this.yearStore,
        fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  // 'Year',
        name:'endYear',
        displayField:'yearid',
        anchor:'95%',
        valueField:'yearid',
        forceSelection: true,
        mode: 'local',
        width:100,
        triggerAction: 'all',
        selectOnFocus:true
    });       

    if (config.sMonth!=null && config.sMonth!= "")
        this.startMonth.setValue(config.sMonth);

    if (config.sYear!=null && config.sYear!= "")
        this.startYear.setValue(config.sYear);

    if (config.eMonth!=null && config.eMonth!= "")
        this.endMonth.setValue(config.eMonth);   

    if (config.eYear!=null && config.eYear!= "")
        this.endYear.setValue(config.eYear);

    this.uPermType=Wtf.UPerm.invoice;
    this.permType=Wtf.Perm.invoice;
    this.exportPermType=(this.receivable?this.permType.exportdataagedreceivable:this.permType.exportdataagedpayable);
    this.printPermType=(this.receivable?this.permType.printagedreceivable:this.permType.printagedpayable);
    this.chartPermType=(this.receivable?this.permType.chartagedreceivable:this.permType.chartagedpayable);
		
    this.MonthlySalesRecord = new Wtf.data.Record.create([
        {name: 'monthname'},
        {name: 'actualbalance'},
        {name: 'monthlybudget'},
        {name: 'actvsbudget'},
        {name: 'forecastbudget'},
        {name: 'actvsfct'}
    ]);
	
    this.expGet = Wtf.autoNum.AccountForecast;

    this.MonthlySalesStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.MonthlySalesRecord),
        url: "ACCReports/getAccountForecastReport.do",
        baseParams:{
            mode: 18, // TODO - to review this
            creditonly:false,
            nondeleted:true,
            isYearlyBudget:this.isYearly,
            getRepeateInvoice: false,
            consolidateFlag:config.consolidateFlag,
            companyids:companyids,
            gcurrencyid:gcurrencyid,
            userid:loginid				
        }
    });

    this.typeStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'typeid',
            type:"boolean"
        }, 'name'],
        data :[[true,WtfGlobal.getLocaleText("acc.rem.127")],[false,WtfGlobal.getLocaleText("acc.rem.128")]]
    });

    this.typeEditor = new Wtf.form.ComboBox({
        store: this.typeStore,
        name:'isdistributive',
        displayField:'name',
        value:true,
        anchor:"50%",
        valueField:'typeid',
        mode: 'local',
        triggerAction: 'all'
    });

    this.rowNo=new Wtf.grid.RowNumberer();

    var columnArr = [];  
    this.summary = new Wtf.ux.grid.GridSummary();
    var columnWidth = 80;
    var pdfWidth = 80;

    columnArr.push(new Wtf.grid.RowNumberer(),{
            hidden:true,
            dataIndex:'monthname'
        },{
            header: WtfGlobal.getLocaleText("acc.forecast.name"), // "Month Name"
            dataIndex: 'monthname',
            pdfwidth:100,
            summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
        },{
        header: WtfGlobal.getLocaleText("acc.forecast.actualbalance"), // "Actual Balance"
        dataIndex: 'actualbalance',
        align:'right',
        summaryType:'sum',
        pdfwidth:100,
        renderer : WtfGlobal.currencyDeletedRenderer,
        summaryRenderer:WtfGlobal.currencySummaryRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.forecast.budget"), // "Budget"
        dataIndex: 'monthlybudget',
        align:'right',
        summaryType:'sum',
        pdfwidth:100,
        renderer : WtfGlobal.currencyDeletedRenderer,
        summaryRenderer:WtfGlobal.currencySummaryRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.forecast.actvsbudget"), // "Act vs Budget"
        dataIndex: 'actvsbudget',
        align:'right',
        summaryType:'sum',
        pdfwidth:100,
        renderer : WtfGlobal.currencyDeletedRenderer,
        summaryRenderer:WtfGlobal.currencySummaryRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.forecast.forecast"), // "Forecast"
        dataIndex: 'forecastbudget',
        align:'right',
        summaryType:'sum',
        pdfwidth:100,
        renderer : WtfGlobal.currencyDeletedRenderer,
        summaryRenderer:WtfGlobal.currencySummaryRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.forecast.actvsfct"), // "Act vs Fct"
        dataIndex: 'actvsfct',
        align:'right',
        summaryType:'sum',
        pdfwidth:100,
        renderer : WtfGlobal.currencyDeletedRenderer,
        summaryRenderer:WtfGlobal.currencySummaryRenderer
    }); 	

   
    this.gridcm = new Wtf.grid.ColumnModel(columnArr);    

    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.MonthlySalesStore,
        cm: this.gridcm,
        ctCls : 'monthlySalesreport',
        border:false,
        plugins:[this.summary,this.expander],
        layout:'fit',
        view:new Wtf.grid.GridView({
            forceFit:true
       }),
        viewConfig: {
            forceFit:true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        loadMask : true
    });
	
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  // 'Reset',
        hidden:false,
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  // 'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });

    this.chart=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.chart"),  // 'Chart',
        tooltip :WtfGlobal.getLocaleText("acc.graphTT.view"), // "Get the graphical view of", //+' '+WtfGlobal.getLocaleText("acc.graphTT.monthlySalesReport"),
        id: 'chartRec'+config.helpmodeid, // + this.id,
        scope: this,
        handler:this.getChart,
        iconCls :(Wtf.isChrome?'accountingbase chartChrome':'accountingbase chart')
    });
    
    // get date from month & year drop-down lists
    if (this.startMonth.getValue() == "" || this.startYear.getValue() == ""){
        var d=new Date();
        
        var month=new Array();
        month[0]="January";
        month[1]="February";
        month[2]="March";
        month[3]="April";
        month[4]="May";
        month[5]="June";
        month[6]="July";
        month[7]="August";
        month[8]="September";
        month[9]="October";
        month[10]="November";
        month[11]="December";
        
        var n = month[d.getMonth()]; 
        var startDatVal=this.getDates(true);
        var endDatVal=this.getDates(false);
        var startmonthDateStr=startDatVal.getMonth();
        var startyearDateStr=startDatVal.getFullYear();
        var endmonthDateStr=endDatVal.getMonth();
        var endyearDateStr=endDatVal.getFullYear();
        this.startMonth.setValue(month[startmonthDateStr]);
        this.startYear.setValue(startyearDateStr);
        this.endMonth.setValue(month[endmonthDateStr]);
        this.endYear.setValue(endyearDateStr);
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
        WtfComMsgBox(["Alert", WtfGlobal.getLocaleText("acc.field.Onlymaximum18monthsaresupported")], 2); // 'Only maximum 18 months are supported!'
        return;            
    }
    
    this.expButton=new Wtf.exportButton({
        id:'exportReports'+config.helpmodeid,
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.sales.exportTT"),  // 'Export report details',
        disabled :true,
        filename:config.id=="monthlyforecastmonthtodatereport"?WtfGlobal.getLocaleText("acc.forecast.tabTitle.monthtodate")+"_v1":config.id=="monthlyforecastyeartodatereport"?WtfGlobal.getLocaleText("acc.forecast.tabTitle.yeartodate")+"_v1":WtfGlobal.getLocaleText("acc.forecast.tabTitle")+"_v1",
        params:{ 
            startdate: this.sDate,
            enddate: this.eDate,			
            accountid:this.cmbAccount.getValue(),
            isdistributive:this.typeEditor.getValue()
        },
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,
            xls:true
        },
        get:this.expGet,
        filename: (this.isYearly?WtfGlobal.getLocaleText("acc.forecast.tabTitle.yeartodate"):WtfGlobal.getLocaleText("acc.forecast.tabTitle.monthtodate")) + "_v1"
    })
    
    this.printButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.print"),
        tooltip :WtfGlobal.getLocaleText("acc.sales.printTT"),  // 'Print report details',
        disabled :true,
        params:{ 	
            accountid:this.accountID||config.accountID,
            stdate: this.sDate,
            enddate: this.eDate,				
            isdistributive:this.typeEditor.getValue(),
            name: WtfGlobal.getLocaleText("acc.wtfTrans.monthlySalesReport")
        },
        label: WtfGlobal.getLocaleText("acc.sales.tabTitle"), // "Monthly Sales Report",
        menuItem:{
            print:true
        },
        get:this.expGet
    })
    
    var btnArr=[];
    btnArr.push( WtfGlobal.getLocaleText("acc.ledger.accName"),this.cmbAccount);


    btnArr.push('-',WtfGlobal.getLocaleText("acc.common.from"),
        // this.startDate
        this.startMonth, this.startYear
        );

    btnArr.push('-',WtfGlobal.getLocaleText("acc.common.to"),
        // this.endDate
        this.endMonth, this.endYear
        );
    
    btnArr.push("-",{
        xtype:'button',
        text:WtfGlobal.getLocaleText("acc.sales.fetch"),  // 'Fetch',
        iconCls:'accountingbase fetch',
        scope:this,
        tooltip:WtfGlobal.getLocaleText("acc.sales.view"),  // "Select a date to view Monthly Sales Report
        handler:this.fetchMonthlySalesReport
    });
    btnArr.push(this.resetBttn);
//    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType))
        btnArr.push(this.expButton);
    //	if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType))
    //		btnArr.push(this.printButton);
    this.resetBttn.on('click',this.handleResetClick,this);
    
    if(config.helpmodeid!=null){
        btnArr.push("->");
        btnArr.push(getHelpButton(this,config.helpmodeid));
    }
    
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        tbar:btnArr,
        items:[this.grid]
    });

    Wtf.account.MonthlySalesReport.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });

    this.MonthlySalesStore.on("beforeload", function(s,o) {
        o.params.stdate = this.sDate;
        o.params.enddate = this.eDate;
    },this);

    this.MonthlySalesStore.on("load",this.storeloaded,this);
	
    this.accStore.on('load',function(){            
        var accountRec=this.accStore.getAt(1);
        this.cmbAccount.setValue(accountRec.data.accountid);
        this.MonthlySalesStore.load({
            params:{
                start:0,			
                isdistributive:this.typeEditor.getValue(),
                accountid:this.cmbAccount.getValue(),
                limit:30,
                creditonly:true
            }
        });
            
    },this);
}

Wtf.extend( Wtf.account.AccountForecastMonthToDateReport,Wtf.Panel,{  
    sumBaseAmount:function(dataindex,v,m,rec){       
        if(!this.isSummary){
            v=rec.data[dataindex];
            return WtfGlobal.withoutRateCurrencySymbol(v,m,rec)
        }
        return "";
    },
    
    groupDateRender:function(v){
        return v.format(WtfGlobal.getOnlyDateFormat())
    },
    
    opBalRenderer:function(val,m,rec){
        return WtfGlobal.withoutRateCurrencyDeletedSymbol(Math.abs(val),m,rec);
    },
    
    totalRender:function(v,m,rec){
        var val=WtfGlobal.withoutRateCurrencySymbol(v,m,rec);
        return "<b>"+val+"</b>"
    },
	
    storeloaded:function(store){
        if(store.getCount()==0){
            if(this.expButton)this.expButton.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.expButton)this.expButton.enable();
            if(this.printButton)this.printButton.enable();
        }	
    },

    formatMoney:function(val,m,rec,i,j,s){
        var fmtVal=WtfGlobal.currencyRenderer(val);
        if(rec.data['fmt']){
            fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
        }
        else if(rec.data["level"]==0&&rec.data["customername"]!="")
            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
        return fmtVal;
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
//        var data=[];
//        var newrec;
//        if(ffyear==null||ffyear=="NaN"){
//            ffyear=new Date(Wtf.account.companyAccountPref.fyfrom)
//            }
//        var year=ffyear.getFullYear();
//        var currentyear=new Date();
//        data.push([0,year])
//        if(!(ffyear.getMonth()==0&&ffyear.getDate()==1)){
//            data.push([1,year+1]);
//            newrec = new Wtf.data.Record({
//                id:1,
//                yearid:year+1
//                })
//        }
//        for(var addyear=year+1;addyear<currentyear.getFullYear();addyear++ ){
//         var year=addyear+1;
//         data.push([1,year])
//        }
//        if(!isfirst&&this.yearStore.getCount()<2){
//            this.yearStore.insert(1,newrec)
//        }
//        return data;
//    },     	

    fetchMonthlySalesReport:function(){
        
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

        if (months>12){
            WtfComMsgBox(["Alert",WtfGlobal.getLocaleText("acc.field.Onlymaximum12monthsaresupported")], 2); // 'Only maximum 12 months are supported!'
            return;            
        }  

        this.MonthlySalesStore.load({
            params:{
                isdistributive:this.typeEditor.getValue(),
                start:0,
                accountid:this.cmbAccount.getValue(),
                limit:15,
                creditonly:true
            }
        });

        this.expButton.setParams({
            stdate: this.sDate,
            enddate: this.eDate,			
            isdistributive:this.typeEditor.getValue()
        });

        this.printButton.setParams({
            stdate: this.sDate,
            enddate: this.eDate,			
            isdistributive:this.typeEditor.getValue(),
            name: WtfGlobal.getLocaleText("acc.sales.tabTitle") // "Monthly Sales Report"
        })
    }, 
    onRowClick:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var accid=this.MonthlySalesStore.getAt(i).data['accountid'];
        this.fireEvent('account',accid);
    },

    getChart:function(){
        var chartid="monthlysalesreportchartid";
        var swf1="../../scripts/graph/krwcolumn/krwcolumn/krwcolumn.swf";
        var id1=this.receivable?"receivableid":"payableid"
        var dataflag1=this.receivable?"ACCInvoiceCMN/getAgedReceivableChart":"ACCGoodsReceiptCMN/getAccountPayableChart";
        var mainid=this.receivable?"mainAgedRecievable":"mainAgedPayable";
        var xmlpath1= this.receivable?'../../scripts/graph/krwcolumn/examples/AgesReceivable/agedreceivable_settings.xml':'../../scripts/graph/krwcolumn/examples/AgesPayable/agedpayable_settings.xml';
        var id2=this.receivable?"piereceivableid":"piepayableid"
        var swf2="../../scripts/graph/krwcolumn/krwpie/krwpie.swf";
        var dataflag2=this.receivable?"ACCInvoiceCMN/getAgedReceivablePie":"ACCGoodsReceiptCMN/getAgedReceivablePie";
        var xmlpath2= this.receivable?'../../scripts/graph/krwcolumn/examples/AgesReceivable/pieagedreceivable_settings.xml':'../../scripts/graph/krwcolumn/examples/AgesPayable/pieagedpayable_settings.xml';
        globalAgedChart(chartid,id1,swf1,dataflag1,mainid,xmlpath1,id2,swf2,dataflag2,xmlpath2,this.withinventory,true,false);
    },getDates:function(start){
        var d=new Date();
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
     handleResetClick:function()
    {
        this.accStore.load();
        this.startMonth.reset();
        this.startYear.reset();
        this.endMonth.reset();
        this.endYear.reset();
        this.sDate = this.startMonth.getValue() + ", " + this.startYear.getValue();
        this.eDate = this.endMonth.getValue() + ", " + this.endYear.getValue();
        this.MonthlySalesStore.load({
            params:{
                isdistributive:this.typeEditor.getValue(),
                start:0,
                accountid:this.cmbAccount.getValue(),
                limit:30,
                creditonly:true
    }
});
    }
});
