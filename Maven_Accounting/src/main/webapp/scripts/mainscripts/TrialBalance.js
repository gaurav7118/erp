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
Wtf.account.TrialBalance=function(config){
//    this.summary = new Wtf.ux.grid.GridSummary();
    this.uPermType=Wtf.UPerm.fstatement;
    this.permType=Wtf.Perm.fstatement;
    this.exportPermType=this.permType.exportdatatrialbalance;
    this.printPermType=this.permType.printtrialbalance;
    this.accRec = Wtf.data.Record.create ([
    {
        name:'accountname',
        mapping:'accname'
    },{
        name:'accountid',
        mapping:'accid'
    },{
        name:'currencyid',
        mapping:'currencyid'
    },{
        name:'acccode'
    },{
        name:'groupname'
    }]);
                
    this.accStore = new Wtf.data.Store({
        url : "ACCAccountCMN/getAccountsForCombo.do",
        baseParams:{
            mode:2,
            ignorecustomers:true,  
            ignorevendors:true,
            nondeleted:true,
            headerAdded:true,
            isForBS_PL_to_GL:true
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.accRec)
    });
    this.accStore.on('load',function(){
        if(this.isGeneralLedger){
            this.showGeneralLedger(this.accountID,this.startDate.getValue(),this.endDate.getValue());
        }
    },this);
    this.accStore.load();
                
    this.MSComboconfig = {
        hiddenName:'accountmulselectcombo',         
        store: this.accStore,
        valueField:'accountid',
        hideLabel:false,
        hidden : false,
        displayField:'accountname',
        emptyText:WtfGlobal.getLocaleText("acc.fxexposure.all"),
        mode: 'local',
        typeAhead: true,
        selectOnFocus:true,
        triggerAction:'all',
        scope:this
    };          
                
    this.MultiSelectAccCombo = new Wtf.common.Select(Wtf.applyIf({
        id:'mulaccountcombo'+this.id,
        multiSelect:true,
        fieldLabel:WtfGlobal.getLocaleText("acc.field.SelectAccounts") ,
        forceSelection:true,  
        extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
        extraComparisionField:'acccode',// type ahead search on acccode as well.
        listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
        width:125
    },this.MSComboconfig));
                  
    this.TrialBalanceRec = new Wtf.data.Record.create([
        {name: 'accountid'},
        {name: 'acccode'},
        {name: 'aliascode'},
        {name: 'acctype'},
        {name: 'accountname'},
        {name: 'd_amount'},
        {name: 'c_amount'},
        {name: 'd_amount_open'},
        {name: 'c_amount_open'},
        {name: 'd_amount_period'},
        {name: 'c_amount_period'},
        {name: 'ytd_d_amount'},
        {name: 'ytd_c_amount'},
        {name: 'ytd_d_amount_open'},
        {name: 'ytd_c_amount_open'},
        {name: 'ytd_d_amount_period'},
        {name: 'ytd_c_amount_period'},
        {name: 'fmt'},
        {name: 'level'},
        {name: 'leaf'},
        {name: 'openingamount'},
        {name: 'c_period'},
        {name: 'd_period'},
        {name: 'periodBalance'},
        {name: 'endingamount'},
        {name: 'issummaryvalue'}
    ]);
    this.TrialBalanceStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.TrialBalanceRec),
//                url: Wtf.req.account+'CompanyManager.jsp',
                url:"ACCReports/getTrialBalance.do",
                baseParams:{
                    mode:62,
                    consolidateFlag:config.consolidateFlag,
                    companyids:companyids,
                    gcurrencyid:gcurrencyid,
                    userid:loginid
                },
                remoteSort:true
            });
            this.rowNo=new Wtf.grid.RowNumberer();
            this.columnArr = [];
            this.columnArr.push(this.rowNo);
            if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                this.columnArr.push({
                    header: WtfGlobal.getLocaleText("acc.coa.accCode"), //"Account Code",
                dataIndex: 'acccode',
                    width: 100,
                    pdfwidth: 150,
                    sortable: true,
                    renderer: function(v, m, rec) {
                        if (rec.data.issummaryvalue) {
                            return '<div style="height:30px;margin-top:4px;"><font size=5><b>' + v + '</b></font></div>';
                        } else {
                        return v;
                    }
                    
                }
                });
            }
            this.columnArr.push({
                header: WtfGlobal.getLocaleText("acc.coa.aliasCode"), //"Alias Code",
                dataIndex: 'aliascode',
                width:100,
                sortable : true,
                pdfwidth:150
//                renderer:WtfGlobal.deletedRenderer 
            },{
                header: WtfGlobal.getLocaleText("acc.trial.acc"),  //"Account",
                dataIndex: 'accountname',
                id: 'accountname',
                pdfwidth:150,
                sortable : true,
                width:100
                //renderer:this.nameRenderer,
//                summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
            },{
                header: WtfGlobal.getLocaleText("acc.coa.gridAccountType"),  //"Account Type
                dataIndex: 'acctype',
                sortable : true,
                width:100,
                pdfwidth:150
            },{
                header: WtfGlobal.getLocaleText("acc.field.OpeningAmount")+" "+WtfGlobal.getLocaleText("acc.trial.debit")+ " ("+WtfGlobal.getCurrencyName()+")",  //'Debit',
                dataIndex: 'd_amount_open',
                width:200,
                pdfwidth:150,
                align:'right',
                renderer: this.amountRenderer,
//                summaryType:'sum',
                hidecurrency : true
//                summaryRenderer: function(value) {
//                    return "<div title='"+WtfGlobal.getLocaleText("acc.field.OpeningAmount")+" ("+WtfGlobal.getCurrencyName()+") &#13;" +WtfGlobal.getLocaleText("acc.trial.debit")+ " ("+WtfGlobal.getCurrencyName()+") &#13; "+WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"'>"+ WtfGlobal.currencySummaryRenderer(value)+"</div>";
//                }
//                summaryRenderer:WtfGlobal.currencySummaryRenderer
            },{
                header :WtfGlobal.getLocaleText("acc.field.OpeningAmount")+" "+WtfGlobal.getLocaleText("acc.trial.credit")+ " ("+WtfGlobal.getCurrencyName()+")",  //'Credit',
                dataIndex: 'c_amount_open',
                width:200,
                pdfwidth:150,
                align:'right',
                renderer: this.amountRenderer,
//                summaryType:'sum',
                hidecurrency : true
//                summaryRenderer: function(value) {
//                    return "<div title='"+WtfGlobal.getLocaleText("acc.field.OpeningAmount")+" ("+WtfGlobal.getCurrencyName()+") &#13;" +WtfGlobal.getLocaleText("acc.trial.credit")+ " ("+WtfGlobal.getCurrencyName()+") &#13; "+WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"'>"+ WtfGlobal.currencySummaryRenderer(value)+"</div>";
//                }
//                summaryRenderer:WtfGlobal.currencySummaryRenderer
            },{                
                header: WtfGlobal.getLocaleText("acc.field.PeriodAmount")+" "+WtfGlobal.getLocaleText("acc.trial.debit")+ " ("+WtfGlobal.getCurrencyName()+")",  //'Debit',
                dataIndex: 'd_amount_period',
                width:200,
                pdfwidth:150,
                align:'right',
                renderer: this.amountRenderer,
//                summaryType:'sum',
                hidecurrency : true
//                summaryRenderer: function(value) {
//                    return "<div title='"+WtfGlobal.getLocaleText("acc.field.PeriodAmount")+" ("+WtfGlobal.getCurrencyName()+")&#13;" +WtfGlobal.getLocaleText("acc.trial.debit")+ " ("+WtfGlobal.getCurrencyName()+") &#13; "+WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"'>"+ WtfGlobal.currencySummaryRenderer(value)+"</div>";
//                }
//                summaryRenderer:WtfGlobal.currencySummaryRenderer
            },{
                header :WtfGlobal.getLocaleText("acc.field.PeriodAmount")+" "+WtfGlobal.getLocaleText("acc.trial.credit")+ " ("+WtfGlobal.getCurrencyName()+")",  //'Credit',
                dataIndex: 'c_amount_period',
                width:200,
                pdfwidth:150,
                align:'right',
                renderer: this.amountRenderer,
//                summaryType:'sum'
                hidecurrency : true
//                summaryRenderer: function(value) {
//                    return "<div title='"+WtfGlobal.getLocaleText("acc.field.PeriodAmount")+" ("+WtfGlobal.getCurrencyName()+")&#13;" +WtfGlobal.getLocaleText("acc.trial.credit")+ " ("+WtfGlobal.getCurrencyName()+") &#13; "+WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"'>"+ WtfGlobal.currencySummaryRenderer(value)+"</div>";
//                }
//                summaryRenderer:WtfGlobal.currencySummaryRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.field.EndingAmount")+" "+WtfGlobal.getLocaleText("acc.trial.debit")+ " ("+WtfGlobal.getCurrencyName()+")",  //'Debit',
                dataIndex: 'd_amount',
                width:200,
                pdfwidth:150,
                align:'right',
                renderer: this.amountRenderer,
//                summaryType:'sum',
                hidecurrency : true
//                summaryRenderer: function(value) {
//                    return "<div title='"+WtfGlobal.getLocaleText("acc.field.EndingAmount")+" ("+WtfGlobal.getCurrencyName()+")&#13;" +WtfGlobal.getLocaleText("acc.trial.debit")+ " ("+WtfGlobal.getCurrencyName()+") &#13; "+WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"'>"+ WtfGlobal.currencySummaryRenderer(value)+"</div>";
//                }
//                summaryRenderer:WtfGlobal.currencySummaryRenderer
            },{
                header :WtfGlobal.getLocaleText("acc.field.EndingAmount")+" "+WtfGlobal.getLocaleText("acc.trial.credit")+ " ("+WtfGlobal.getCurrencyName()+")",  //'Credit',
                dataIndex: 'c_amount',
                width:200,
                pdfwidth:150,
                align:'right',
                renderer: this.amountRenderer,
//                summaryType:'sum',
                hidecurrency : true
//                summaryRenderer: function(value) {
//                    return "<div title='"+WtfGlobal.getLocaleText("acc.field.EndingAmount")+" ("+WtfGlobal.getCurrencyName()+")&#13;" +WtfGlobal.getLocaleText("acc.trial.credit")+ " ("+WtfGlobal.getCurrencyName()+") &#13; "+WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"'>"+ WtfGlobal.currencySummaryRenderer(value)+"</div>";
//                }
//                summaryRenderer:WtfGlobal.currencySummaryRenderer
            },{
                header: "YTD "+WtfGlobal.getLocaleText("acc.field.OpeningAmount")+" "+WtfGlobal.getLocaleText("acc.trial.debit")+ " ("+WtfGlobal.getCurrencyName()+")",  //'YTD(debit)',
                dataIndex: 'ytd_d_amount_open',
                width:200,
                pdfwidth:150,
                align:'right',
                renderer:this.amountRenderer,
//                summaryType:'sum',
                hidecurrency : true
//                summaryRenderer: function(value) {
//                    return "<div title='YTD "+WtfGlobal.getLocaleText("acc.field.OpeningAmount")+" ("+WtfGlobal.getCurrencyName()+")&#13;" +WtfGlobal.getLocaleText("acc.trial.ytd_debit")+ " ("+WtfGlobal.getCurrencyName()+") &#13; "+WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"'>"+ WtfGlobal.currencySummaryRenderer(value)+"</div>";
//                }
//                summaryRenderer:WtfGlobal.currencySummaryRenderer
            },{
                header :"YTD "+WtfGlobal.getLocaleText("acc.field.OpeningAmount")+" "+WtfGlobal.getLocaleText("acc.trial.credit")+ " ("+WtfGlobal.getCurrencyName()+")",  //'YTD(credit)',
                dataIndex: 'ytd_c_amount_open',
                width:200,
                pdfwidth:150,
                align:'right',
                renderer:this.amountRenderer,
//                summaryType:'sum',
                hidecurrency : true
//                summaryRenderer: function(value) {
//                    return "<div title='YTD "+WtfGlobal.getLocaleText("acc.field.OpeningAmount")+" ("+WtfGlobal.getCurrencyName()+")&#13;" +WtfGlobal.getLocaleText("acc.trial.ytd_credit")+ " ("+WtfGlobal.getCurrencyName()+") &#13; "+WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"'>"+ WtfGlobal.currencySummaryRenderer(value)+"</div>";
//                }
//                summaryRenderer:WtfGlobal.currencySummaryRenderer
            },{
                header: "YTD "+WtfGlobal.getLocaleText("acc.field.PeriodAmount")+" "+WtfGlobal.getLocaleText("acc.trial.debit")+ " ("+WtfGlobal.getCurrencyName()+")",  //'YTD(debit)',
                dataIndex: 'ytd_d_amount_period',
                width:200,
                pdfwidth:150,
                align:'right',
                renderer:this.amountRenderer,
//                summaryType:'sum',
                hidecurrency : true
//                 summaryRenderer: function(value) {
//                    return "<div title='YTD "+WtfGlobal.getLocaleText("acc.field.PeriodAmount")+" ("+WtfGlobal.getCurrencyName()+")&#13;"+WtfGlobal.getLocaleText("acc.trial.ytd_debit")+ " ("+WtfGlobal.getCurrencyName()+") &#13; "+WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"'>"+ WtfGlobal.currencySummaryRenderer(value)+"</div>";
//                }
//                summaryRenderer:WtfGlobal.currencySummaryRenderer
            },{
                header :"YTD "+WtfGlobal.getLocaleText("acc.field.PeriodAmount")+" "+WtfGlobal.getLocaleText("acc.trial.credit")+ " ("+WtfGlobal.getCurrencyName()+")",  //'YTD(credit)',
                dataIndex: 'ytd_c_amount_period',
                width:200,
                pdfwidth:150,
                align:'right',
                renderer:this.amountRenderer,
//                summaryType:'sum'
                hidecurrency : true
//                summaryRenderer: function(value) {
//                    return "<div title='YTD "+WtfGlobal.getLocaleText("acc.field.PeriodAmount")+" ("+WtfGlobal.getCurrencyName()+")&#13;"+WtfGlobal.getLocaleText("acc.trial.ytd_credit")+ " ("+WtfGlobal.getCurrencyName()+") &#13; "+WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"'>"+ WtfGlobal.currencySummaryRenderer(value)+"</div>";
//                }
//                summaryRenderer:WtfGlobal.currencySummaryRenderer
            },{
                header: "YTD "+WtfGlobal.getLocaleText("acc.field.EndingAmount")+" "+WtfGlobal.getLocaleText("acc.trial.debit")+ " ("+WtfGlobal.getCurrencyName()+")",  //'YTD(debit)',
                dataIndex: 'ytd_d_amount',
                width:200,
                pdfwidth:150,
                align:'right',
                renderer:this.amountRenderer,
//                summaryType:'sum',
                hidecurrency : true
//                summaryRenderer: function(value) {
//                    return "<div title='YTD "+WtfGlobal.getLocaleText("acc.field.EndingAmount")+" ("+WtfGlobal.getCurrencyName()+")&#13;"+WtfGlobal.getLocaleText("acc.trial.ytd_debit")+ " ("+WtfGlobal.getCurrencyName()+") &#13; "+WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"'>"+ WtfGlobal.currencySummaryRenderer(value)+"</div>";
//                }
//                summaryRenderer:WtfGlobal.currencySummaryRenderer
            },{
                header :"YTD "+WtfGlobal.getLocaleText("acc.field.EndingAmount")+" "+WtfGlobal.getLocaleText("acc.trial.credit")+ " ("+WtfGlobal.getCurrencyName()+")",  //'YTD(credit)',
                dataIndex: 'ytd_c_amount',
                width:200,
                pdfwidth:150,
                align:'right',
                renderer:this.amountRenderer,
//                summaryType:'sum',
                hidecurrency : true
//                summaryRenderer: function(value) {
//                    return "<div title='YTD "+WtfGlobal.getLocaleText("acc.field.EndingAmount")+" ("+WtfGlobal.getCurrencyName()+")&#13;"+WtfGlobal.getLocaleText("acc.trial.ytd_credit")+ " ("+WtfGlobal.getCurrencyName()+") &#13; "+WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"'>"+ WtfGlobal.currencySummaryRenderer(value)+"</div>";
//                }
//                summaryRenderer:WtfGlobal.currencySummaryRenderer
            });
            this.gridcm= new Wtf.grid.ColumnModel(this.columnArr);

            this.grid = new Wtf.grid.HirarchicalGridPanel({
//            this.grid = new Wtf.grid.GridPanel({
                stripeRows :true,
                store: this.TrialBalanceStore,
                cm: this.gridcm,
                border : false,
                loadMask : true,
//                hirarchyColNumber:4,
//                plugins:[new Wtf.GroupHeaderGrid({
//            rows: [
//                [{align:"center",header:"",colspan:6},
//                 {align:"center",header:"<b>"+WtfGlobal.getLocaleText("acc.field.OpeningAmount")+" ("+WtfGlobal.getCurrencyName()+")</b>",colspan:2},
//                 {align:"center",header:"<div><b>"+WtfGlobal.getLocaleText("acc.field.PeriodAmount")+" ("+WtfGlobal.getCurrencyName()+")</b></div>",colspan:2},
//                 {align:"center",header:"<div><b>"+WtfGlobal.getLocaleText("acc.field.EndingAmount")+" ("+WtfGlobal.getCurrencyName()+")</b></div>",colspan:2},
//                 {align:"center",header:"<b>YTD "+WtfGlobal.getLocaleText("acc.field.OpeningAmount")+" ("+WtfGlobal.getCurrencyName()+")</b>",colspan:2},
//                 {align:"center",header:"<div><b>YTD "+WtfGlobal.getLocaleText("acc.field.PeriodAmount")+" ("+WtfGlobal.getCurrencyName()+")</b></div>",colspan:2},
//                 {align:"center",header:"<div><b>YTD "+WtfGlobal.getLocaleText("acc.field.EndingAmount")+" ("+WtfGlobal.getCurrencyName()+")</b></div>",colspan:2}]
//            ],
//            hierarchicalColMenu: true
//        }),/*this.summary*/],
                viewConfig: {
//                    forceFit:true,
                    //getRowClass:this.getRowClass.createDelegate(this),
                    emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn")),    //ERP-28938
                    deferEmptyText: false
                }
            });
	
            this.grid.on("render", function(grid) {
                WtfGlobal.autoApplyHeaderQtip(grid);
                new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
                    this.grid.on('statesave', this.saveMyStateHandler, this);
                }, this);
            },this);
	    this.grid.view.refresh.defer(1, this.grid.view); 
        //Overrided function of HirarchicalGridPanel
        this.grid.formatAccountName=function(val,m,rec,i,j,store){
                var fmtVal=(this.prevRenderer?this.prevRenderer(val,m,rec,i,j,store):val);
        if (fmtVal === Wtf.Difference_in_Opening_balances) {
            fmtVal = "<a class='jumplink' style='float:none;margin-left:5px;' href='#'>" + fmtVal + "</a>";
        } else if (fmtVal !== Wtf.StockInHand && fmtVal !== Wtf.OpeningStock && fmtVal !== Wtf.AccumulatedProfitAndLoss) {
            fmtVal = (rec.data["fmt"] ? '<b>' + fmtVal + '</b>' : "<a class='jumplink' style='float:none;margin-left:5px;' href='#'>" + fmtVal + "</a>");
        }
                if(val){
                    if(rec.data['leaf']==true) {
                        fmtVal="<div style='margin-left:"+(rec.data['level']*20)+"px;padding-left:20px'>"+fmtVal+"</div>";
                    } else {
                        fmtVal= "<div class='x-grid3-row-expanderacc' style='margin-left:"
                        +(rec.data['level']*20)+"px;width:20px'><div style='margin-left:20px;font-weight:bold'>"+fmtVal+"</div></div>";
                    }
                }
                return fmtVal;
        }

        //Overrided function of HirarchicalGridPanel
        this.grid.getView().getRowClass = function(record){
            var colorCss=" x-grid3-row-expandedacc";
            switch(record.data["fmt"]){
                case "T":colorCss=" grey-background";break;
                case "B":colorCss=" red-background";break;
                case "H":colorCss=" header-background";break;
                case "A":colorCss=" darkyellow-background";break;
            }
            return colorCss;
        }

            this.TrialBalanceStore.on("load", function(store){
                WtfGlobal.resetAjaxTimeOut();
                if(store.getCount()==0){
                    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
                        if(this.expButton){
                            this.expButton.disable();
                        }
                        if(this.expJasperButton){
                            this.expJasperButton.disable();
                        }   
                    }
                    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
                        if(this.printButton)this.printButton.disable();
                    }
                    this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn"));
                    this.grid.getView().refresh();
                }else{
                    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
                        if(this.expButton){
                            this.expButton.enable()
                        }
                    }
                    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
                        if(this.printButton)this.printButton.enable();
                    }
                }
                var accountNameCol = this.grid.getColumnModel().getIndexById("accountname"); 
                if(accountNameCol > -1){
                    this.grid.getColumnModel().setRenderer(accountNameCol,this.grid.formatAccountName.createDelegate(this.grid));
                    this.grid.getView().refresh();
                }
            },this);
            this.TrialBalanceStore.on("beforeload", function(store){
                WtfGlobal.setAjaxTimeOutFor30Minutes();
            }, this);
            this.TrialBalanceStore.on("loadexception", function(store){
                WtfGlobal.resetAjaxTimeOut();
            }, this);

            var sdateSavedSearch;
            var edateSavedSearch;
            if(config.searchJson != undefined && config.searchJson != ""){
                sdateSavedSearch = JSON.parse(config.searchJson).data[0].sdate;
                edateSavedSearch = JSON.parse(config.searchJson).data[0].edate;
            }
    this.startDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate',
        format:WtfGlobal.getOnlyDateFormat(),
       // readOnly:true,
        value:this.getDates(true, sdateSavedSearch)
    });
    this.endDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
       // readOnly:true,
        value:this.getDates(false, edateSavedSearch)
    });
    var btnArr=[];
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
        btnArr.push(this.expButton=new Wtf.exportButton({
            obj:this,
            text:WtfGlobal.getLocaleText("acc.common.export"),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
            disabled :true,
            filename : WtfGlobal.getLocaleText("acc.trial.tabtitle"),
            params:{stdate:WtfGlobal.convertToGenericStartDate(this.getDates(true)),
                   enddate:WtfGlobal.convertToGenericEndDate(this.getDates(false)),
                   accountid:this.accountID,
                   name: WtfGlobal.getLocaleText("acc.trial.tabtitle"),
                   accountIds:this.MultiSelectAccCombo.getValue()
            },
            menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
            get:116
        }));
        
        var pdfselectedbtn=new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.exp.trialbalance"),  //'Export',
            iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export'),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details.',
            scope: this,
            menu: {        
                items: [
                {
                    text: WtfGlobal.getLocaleText("acc.journalentry.exportpdf.portrait"),
                    iconCls:'pwnd '+'exportpdf',
                    scope: this,
                    handler:function(){
                        this.exportPdfTemplate(true);
                    }
                },{
                    text: WtfGlobal.getLocaleText("acc.journalentry.exportpdf.landscape"),
                    iconCls:'pwnd '+'exportpdf',
                    scope: this,
                    handler:function(){
                        this.exportPdfTemplate(false);
                    }
                }
                ]
            }
        });            
        btnArr.push(pdfselectedbtn); // Wtf.templateflag==13 is for BIT group.
     }
     if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
        btnArr.push(this.printButton=new Wtf.exportButton({
            obj:this,
            text:WtfGlobal.getLocaleText("acc.common.print"),
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
            disabled :true,
            filename : WtfGlobal.getLocaleText("acc.trial.tabtitle"),
            params:{stdate:WtfGlobal.convertToGenericStartDate(this.getDates(true)),
                   enddate:WtfGlobal.convertToGenericEndDate(this.getDates(false)),
                   accountid:this.accountID,
                   name: WtfGlobal.getLocaleText("acc.trial.tabtitle"),
                   accountIds:this.MultiSelectAccCombo.getValue()
            },
            label:WtfGlobal.getLocaleText("acc.trial.tabtitle"),
            menuItem:{print:true},
            get:116
        }));
    }
    
    this.viewTypeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:'int'}, 'name'],
        data :[
            [0, "Default view"],
            [1, WtfGlobal.getLocaleText("acc.periodView")]
        ]
    });
    
    this.viewCombo = new Wtf.form.ComboBox({
        store: this.viewTypeStore,
        name:'typeid',
        displayField:'name',
        valueField:'typeid',
        mode: 'local',
        value:0,
        width:110,        
        listWidth:110,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
    this.viewCombo.on('select',this.changeView,this);
    
    if(config.searchJson!=undefined&&config.searchJson!=""){
        var viewComboValue = JSON.parse(config.searchJson).data[0].viewCombo;
        if(viewComboValue){
            this.viewCombo.value = viewComboValue;
            this.changeView(this.viewCombo);
        }
    }
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    
    btnArr.push(this.AdvanceSearchBtn);
    
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid:102, //Wtf.TrialBalance_Moduleid,
        reportid: Wtf.TrialBalance_Moduleid,
        advSearch: false
    });
    
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("afteradd", function(){
        this.objsearchComponent.advGrid.viewCombo = this.viewCombo.getValue();
        this.objsearchComponent.advGrid.sdate = this.startDate.getValue();
        this.objsearchComponent.advGrid.edate = this.endDate.getValue();
    }, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    
//    this.fetchTrialBalance();	//ERP-28938
    Wtf.apply(this,{
        items:[{
            border:false,
            layout : "border",
            scope:this,
            items:[this.objsearchComponent,{
                region:'center',
                layout:'fit',
                border:false,
                items:[this.grid]
            }],
        
            tbar:[WtfGlobal.getLocaleText("acc.1099.selAcc"),this.MultiSelectAccCombo,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,'-',WtfGlobal.getLocaleText("acc.common.to"),this.endDate,'-',{
                xtype:'button',
                text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
                tooltip:WtfGlobal.getLocaleText("acc.trial.fetchTT"),  //"Select a time period to view corresponding trial balance records.",
                iconCls:'accountingbase fetch',
                scope:this,
                handler:function(){
                        this.fetchTrialBalance(false);  //ERP-28938
                    }
            },'-',btnArr,"->",WtfGlobal.getLocaleText("acc.common.view"),' ',this.viewCombo,' ',getHelpButton(this,25)]
        }]

    },config);
//    Wtf.apply(this,{
//        items:this.TrialBalancegrid
//    },config)

    this.getMyConfig();
    Wtf.account.TrialBalance.superclass.constructor.call(this,config);
    this.addEvents({
        'account':true
    });
//    this.grid.addEvents({
//        'savemystate' : true
//    });
    this.grid.on('rowclick',this.onRowClick, this);
}
Wtf.extend( Wtf.account.TrialBalance,Wtf.Panel,{
    nameRenderer:function(v,m,rec){
        return (rec.data["fmt"]?'<b>'+v+'</b>':WtfGlobal.linkRenderer(v));
    },

    currencyRenderer:function(v,m,rec){
        return (rec.data["fmt"]?WtfGlobal.currencySummaryRenderer(v):WtfGlobal.currencyRenderer(v));
    },
    
    changeView : function(combo,rec,index){
        var columnArr = [];
        if(combo.getValue() == 0){
            columnArr = this.columnArr;
        }else if(combo.getValue() == 1){
            var rowNo=new Wtf.grid.RowNumberer({
                width : 23
            });
            columnArr.push(rowNo);
             
            if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                columnArr.push({
                    header: WtfGlobal.getLocaleText("acc.coa.accCode"), //"Account Code",
                    dataIndex: 'acccode',
                    width: 100,
                    pdfwidth: 150,
                    sortable: true,
                    renderer: function(v, m, rec) {
                        if (rec.data.issummaryvalue) {
                            return '<div style="height:30px;margin-top:4px;"><font size=5><b>' + v + '</b></font></div>';
                        } else {
                            return v;
                        }
                    
                    }
                });
            }
            
            columnArr.push({
                header: WtfGlobal.getLocaleText("acc.coa.aliasCode"), //"Alias Code",
                dataIndex: 'aliascode',
                width:100,
                sortable : true,
                pdfwidth:150
            },{
                header: WtfGlobal.getLocaleText("acc.trial.acc"),  //"Account",
                dataIndex: 'accountname',
                renderer : this.grid.formatAccountName.createDelegate(this.grid),
                pdfwidth:150,
                sortable : true,
                width:150
            },{
                header: WtfGlobal.getLocaleText("acc.coa.gridAccountType"),  //"Account Type
                dataIndex: 'acctype',
                sortable : true,
                width:100,
                pdfwidth:150
            },{                
                header: WtfGlobal.getLocaleText("acc.field.OpeningAmount")+ " ("+WtfGlobal.getCurrencyName()+")",  //'Debit',
                dataIndex: 'openingamount',
                width:200,
                pdfwidth:150,
                align:'right',
                renderer: this.amountRenderer,
                hidecurrency : true
            },{
                header: WtfGlobal.getLocaleText("acc.field.PeriodAmount")+" "+WtfGlobal.getLocaleText("acc.trial.debit")+ " ("+WtfGlobal.getCurrencyName()+")",  //'Debit',
                dataIndex: 'd_period',
                width:200,
                pdfwidth:150,
                align:'right',
                renderer: this.amountRenderer,
                hidecurrency : true
            },{
                header : WtfGlobal.getLocaleText("acc.field.PeriodAmount")+" "+WtfGlobal.getLocaleText("acc.trial.credit")+ " ("+WtfGlobal.getCurrencyName()+")",  //'Credit',
                dataIndex: 'c_period',
                width:200,
                pdfwidth:150,
                align:'right',
                renderer: this.amountRenderer,
                hidecurrency : true
            },{
                header : "Period Balance ("+WtfGlobal.getCurrencyName()+")",  
                dataIndex: 'periodBalance',
                width:200,
                pdfwidth:150,
                align:'right',
                renderer: this.amountRenderer,
                hidecurrency : true
            },{
                header :WtfGlobal.getLocaleText("acc.field.EndingAmount")+" ("+WtfGlobal.getCurrencyName()+")",  //'Credit',
                dataIndex: 'endingamount',
                width:200,
                pdfwidth:150,
                align:'right',
                renderer: this.amountRenderer,
                hidecurrency : true
            });
        }
        this.gridcm.setConfig(columnArr);
    },
    
//    getRowClass:function(record,grid){
//        var colorCss="";
//        switch(record.data["fmt"]){
//            case "T":colorCss=" grey-background";break;
//            case "B":colorCss=" red-background";break;
//            case "H":colorCss=" header-background";break;
//            case "A":colorCss=" darkyellow-background";break;
//        }
//        return colorCss;
//    },

    fetchTrialBalance:function(){
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
        this.TrialBalanceStore.load({
            params:{
                  stdate:this.sdate,
                  enddate:this.edate,
                  accountIds:this.MultiSelectAccCombo.getValue()
            }
         });
         if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
             this.expButton.setParams({
                   stdate:this.sdate,
                   enddate:this.edate,
                   accountid:this.accountID,
                   name: WtfGlobal.getLocaleText("acc.trial.tabtitle"),
                   accountIds:this.MultiSelectAccCombo.getValue()
             });
         }
         if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
             this.printButton.setParams({
                   stdate:this.sdate,
                   enddate:this.edate,
                   accountid:this.accountID,
                   name: WtfGlobal.getLocaleText("acc.trial.tabtitle"),
                   accountIds:this.MultiSelectAccCombo.getValue()
             });
         }

    },

    onRowClick:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var accName = this.TrialBalanceStore.getAt(i).data['accountname'];
        if(accName == Wtf.Difference_in_Opening_balances){
            callLedger(accName, this.startDate.getValue(),this.endDate.getValue(), undefined, undefined, undefined, true);
        }else if(accName===Wtf.StockInHand){
            
        }else{
            var accid = this.TrialBalanceStore.getAt(i).data['accountid'];
            var searchJson = (this.objsearchComponent.advGrid != undefined) ? this.objsearchComponent.advGrid.getJsonofStore() : "" ;
            this.fireEvent('account',searchJson,this.filterConjuctionCrit,accid,this.startDate.getValue(),this.endDate.getValue());
        }
    },
    
    getDates:function(start, savedSearchDate){
        if(savedSearchDate != undefined){
            return new Date(savedSearchDate);
        }
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
    
    exportPdfTemplate:function(isPortrait){
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
        var url = "ACCReports/exportTrialBalanceJasper.do?"+"&stdate="+this.sdate+"&enddate="+this.edate+"&accountid="+this.accountID+"&templateflag="+Wtf.templateflag+"&isPortrait="+isPortrait+"&accountIds="+this.MultiSelectAccCombo.getValue()+"&moduleid="+this.moduleid+"&searchJson="+(this.searchJson != undefined ? this.searchJson :"")+"&filterConjuctionCriteria="+(this.filterConjuctionCrit != undefined ? this.filterConjuctionCrit :"");
        Wtf.get('downloadframe').dom.src = url; 
    },
    
    getMyConfig : function(){
        WtfGlobal.getGridConfig (this.grid, Wtf.TrialBalance_Moduleid, false, false);
    },
    
    saveMyStateHandler: function(grid,state){
        if (this.viewCombo.getValue() != 1) { // If not period view then save grid config.
            WtfGlobal.saveGridStateHandler(this, grid, state, Wtf.TrialBalance_Moduleid, grid.gridConfigId, false);
        }
    },
    
//    saveMyState: function(){
//        var state = this.grid.getState();
//        this.grid.fireEvent("savemystate", this, state);
//    },

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
        this.TrialBalanceStore.baseParams = {   
            flag: 1,
            searchJson: this.searchJson,
            moduleid:this.moduleId,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.TrialBalanceStore.load({
            params:{
                  stdate:this.sdate,
                  enddate:this.edate,
                  accountIds:this.MultiSelectAccCombo.getValue()
            }
         });
    },
    
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.TrialBalanceStore.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleId,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.TrialBalanceStore.load({
            params:{
                  stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                  enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                  accountIds:this.MultiSelectAccCombo.getValue()
            }
         });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },
    amountRenderer: function(value, isCheckCenterAlign) {
        var isCenterAlign = (isCheckCenterAlign == undefined ? false : isCheckCenterAlign[0]);
        var v = parseFloat(value);
        if (isNaN(v)) {
            if (!Wtf.account.companyAccountPref.showZeroAmountAsBlank) {
                v = WtfGlobal.conventInDecimal(0.0, WtfGlobal.getCurrencySymbol());
            } else {
                return value;
            }
        } else {
            v = WtfGlobal.conventInDecimal(v, WtfGlobal.getCurrencySymbol());
        }
        if (isCenterAlign) {
            return '<div>' + v + '</div>';
        }
        return '<div class="currency">' + v + '</div>';
    }
    
});
