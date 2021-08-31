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
function callCostCenterReportTransactionDetailsDynamicLoad(consolidateFlag){
    consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
    var id = 'CostCenterDetails';
    id = consolidateFlag?id+'Merged':id;
    var panel = Wtf.getCmp(id);
    if(panel==null){
        panel = new Wtf.CostCenterDetailsTab({
            id : id,
            consolidateFlag:consolidateFlag,
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.ccReport.tab3"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.ccReport.Tip2"),  //'View Tansactions Report of different Cost Center(s) in your organization from here.',
            topTitle:'<center><font size=4>Cost Center Transactions Report</font></center>',
            border : false,
            closable: false,
            iconCls:'accountingbase balancesheet'
        });
        if(consolidateFlag) {
            Wtf.getCmp('CostCenterReportMerged').add(panel);
        }else {
            Wtf.getCmp('CostCenterReport').add(panel);
        }
    }
    return panel;
}

function callAllCostCenterSummaryDynamicLoad(consolidateFlag){
    consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
    var id = 'allCostCenterSummary';
    id = consolidateFlag?id+'Merged':id;
    var panel = Wtf.getCmp(id);
    if(panel==null){
        consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
        panel = new Wtf.CostCenterSummaryTab({
            id : id,
            consolidateFlag : consolidateFlag,
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.ccReport.tab1"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.ccReport.Tip1"),  //'View Summary Report of different Cost Center(s) in your organization from here.',
            topTitle:'<center><font size=4>'+WtfGlobal.getLocaleText("acc.field.CostCenterSummaryReport")+'</font></center>',
            border : false,
            closable: false,
            iconCls:'accountingbase balancesheet'
        });
        if(consolidateFlag) {
            Wtf.getCmp('CostCenterReportMerged').add(panel);
        } else {
            Wtf.getCmp('CostCenterReport').add(panel);
        }
    }
    return panel;
}

//************************************************************************************************************************************
Wtf.CostCenterDetailsTab = function(config){
    Wtf.apply(this,config);
    Wtf.CostCenterDetailsTab.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.CostCenterDetailsTab, Wtf.Panel, {
    border : false,
    closable: false,
    layout: 'fit',
    initComponent: function(config){
        this.withOutInventory = Wtf.account.companyAccountPref.withoutinventory;
        this.currentFilters = {};
        this.startDate=new Wtf.ExDateFieldQtip({
            name:'stdate',
            format:WtfGlobal.getOnlyDateFormat(),
    //        readOnly:true,
            value:this.getDates(true)
        });
        this.endDate=new Wtf.ExDateFieldQtip({
            name:'enddate',
            format:WtfGlobal.getOnlyDateFormat(),
   //         readOnly:true,
            value:this.getDates(false)
        });

        this.typeStore = new Wtf.data.SimpleStore({
            fields: ["id", "name"],
            data :[["CCR_Invoice","Invoice and Cash Sales "],
                    ["CCR_Vendor_Invoice","Vendor Invoice and Cash Purchase"],
                    ["CCR_Sales_Order","Sales Order"],
                    ["CCR_Purchase_Order","Purchase Order"],
                    ["CCR_Credit_Note","Credit Note"],
                    ["CCR_Debit_Note","Debit Note"],
                    ["CCR_Journal_Entry","Journal Entry"]]
                    //["CCR_Journal_Entry_Fixed_Asset","Fixed Asset"]]
        });
        this.typeEditor = new Wtf.form.ComboBox({
            store: this.typeStore,
            name:'typeid',
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width:210,
            listWidth:210,
            defaultValue:0,
            triggerAction: 'all',
            typeAhead:true,
            emptyText:WtfGlobal.getLocaleText("acc.ccReport.empText"),  //"Please Select Transaction",
            selectOnFocus:true
        });
        
        chkCostCenterload();
        if(Wtf.CostCenterStore.getCount()==0) Wtf.CostCenterStore.on("load", this.setCostCenter, this);
        this.costCenter = new Wtf.form.ComboBox({
            store: Wtf.CostCenterStore,
            name:'costCenterId',
            width:150,
            listWidth:150,
            displayField:'name',
            valueField:'id',
            triggerAction: 'all',
            mode: 'local',
            typeAhead:true,
            value:"",
            selectOnFocus:true,
            forceSelection: true,
            emptyText:WtfGlobal.getLocaleText("acc.rem.9")  //"Select a Cost Center"
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
        this.fetchButton = new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
            tooltip:WtfGlobal.getLocaleText("acc.common.fetchTT"),
            iconCls:'accountingbase fetch',
            scope:this,
            handler:this.fetchReport
        });
    
        this.tbar = [WtfGlobal.getLocaleText("acc.common.costCenter"), this.costCenter, "-", WtfGlobal.getLocaleText("acc.ccReport.trans"), this.typeEditor, "-", this.fetchButton,this.resetBttn]
        Wtf.CostCenterDetailsTab.superclass.initComponent.call(this, config);
    },

    onRender: function(config){
        Wtf.CostCenterDetailsTab.superclass.onRender.call(this, config);
    },

    getDates:function(start){
        var d=new Date();
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom&&this.statementType!='BalanceSheet')
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },

    setCostCenter: function(){
        this.costCenter.setValue("");//Select Default Cost Center as None
        Wtf.CostCenterStore.un("load", this.setCostCenter, this);
    },

    fetchReport: function(){
        var sDate=this.startDate.getValue();
        var eDate=this.endDate.getValue();
        var costCenter= this.costCenter.getValue();
        var transaction=this.typeEditor.getValue();
        var ccrAllRecords=true;

//        if(sDate=="" || eDate=="") {
//            WtfComMsgBox(42,2);
//            return;
//        }

        if(transaction=="") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.nee.7")],2);
            return;
        }

        if(sDate>eDate){
            WtfComMsgBox(1,2);
            return;
        }
        this.sDate = sDate=WtfGlobal.convertToGenericDate(this.startDate.getValue().add(Date.DAY,0));
        this.eDate = eDate=WtfGlobal.convertToGenericDate(this.endDate.getValue().add(Date.DAY,0));

        var params={
            //startdate:sDate,          //Not Need to send date params ERP-20780
            //enddate:eDate,
            costcenter:costCenter,
            transaction: transaction,
            ccrAllRecords : ccrAllRecords,
            groupid : transaction=="CCR_Journal_Entry_Fixed_Asset"?true:false
        }

        var isModified = true;//Bug #17368;//((this.currentFilters.transaction != params.transaction) || (this.currentFilters.startdate != params.startdate) || (this.currentFilters.enddate != params.enddate) || (this.currentFilters.costcenter != params.costcenter));

        if(isModified){
            if(transaction!=""){
                this.remove(Wtf.getCmp(this.currentFilters.transaction));
            }
            this.createReport(transaction, params);
            this.currentFilters = params;
        }
    },
    handleResetClickNew:function()
    {
//       this.startDate.reset();
//       this.endDate.reset();
       this.typeEditor.reset();
       this.costCenter.reset();
       var transaction=this.typeEditor.getValue();
       if(transaction==""){
                this.remove(Wtf.getCmp(this.currentFilters.transaction));
            }
    },
    createReport: function(transaction, params){
        var reportPanel = Wtf.getCmp(transaction);
        if(reportPanel==null) {
            if(transaction=="CCR_Invoice"){
                reportPanel = getInvoiceTab(this.withOutInventory, transaction, "", params, false, this.consolidateFlag);
            } else if(transaction=="CCR_Vendor_Invoice"){
                reportPanel = getVendorInvoiceTab(this.withOutInventory, transaction, "", params, this.consolidateFlag);
            } else if(transaction=="CCR_Sales_Order"){
                reportPanel = getSOTab(this.withOutInventory, transaction, "", params, this.consolidateFlag);
            } else if(transaction=="CCR_Purchase_Order"){
                reportPanel = getPOTab(this.withOutInventory, transaction, "", params, this.consolidateFlag);
            } else if(transaction=="CCR_Credit_Note"){
                reportPanel = getCNTab(this.withOutInventory, transaction, "", params, this.consolidateFlag);
            } else if(transaction=="CCR_Debit_Note"){
                reportPanel = getDNTab(this.withOutInventory, transaction, "", params, this.consolidateFlag);
            } else if(transaction=="CCR_Journal_Entry"){
                reportPanel = getJETab(transaction, "", undefined, params, this.consolidateFlag);
                var jid = undefined;
                var check = false;
                reportPanel.expandJournalEntry(jid,check);
//            } else if(transaction=="CCR_Journal_Entry_Fixed_Asset"){
//                reportPanel = getJETab(transaction, "", undefined, params);
            }
            reportPanel.on('journalentry',callJournalEntryDetails);
        }
        this.add(reportPanel);
        this.doLayout();
    }
});


//
//   Cost Center Summary Report Tab
//

Wtf.CostCenterSummaryTab = function(config){
    Wtf.apply(this,config);
    Wtf.CostCenterSummaryTab.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.CostCenterSummaryTab, Wtf.Panel, {
    border : false,
    closable: false,
    layout: 'fit',
    initComponent: function(config){
        this.withOutInventory = Wtf.account.companyAccountPref.withoutinventory;
        this.startDate=new Wtf.ExDateFieldQtip({
            name:'stdate',
            format:WtfGlobal.getOnlyDateFormat(),
   //         readOnly:true,
            value:this.getDates(true)
        });
        
        this.endDate=new Wtf.ExDateFieldQtip({
            name:'enddate',
            format:WtfGlobal.getOnlyDateFormat(),
       //     readOnly:true,
            value:this.getDates(false)
        });

        this.fetchButton = new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
            iconCls:'accountingbase fetch',
            scope:this,
            handler:this.fetchReport
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
        this.pP3 = new Wtf.common.pPageSize({})
       this.resetBttn.on('click',this.handleResetClickNew,this);
        this.tbar = [WtfGlobal.getLocaleText("acc.common.from"), this.startDate, "-", WtfGlobal.getLocaleText("acc.common.to"), this.endDate, "-", this.fetchButton, this.resetBttn ]
        Wtf.CostCenterSummaryTab.superclass.initComponent.call(this, config);
    },

    onRender: function(config){
        Wtf.CostCenterSummaryTab.superclass.onRender.call(this, config);

        this.Record = new Wtf.data.Record.create([
            {name: 'costcenterid'},
            {name: 'costcenterName'},
            {name: 'debitAmount'},
            {name: 'creditAmount'}
        ]);

        this.Store = new Wtf.data.Store({
            url: "ACCReports/getCostCenterSummary.do?consolidateFlag="+this.consolidateFlag+"&companyids="+companyids+"&gcurrencyid="+gcurrencyid+"&userid="+loginid,
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                 totalProperty: "count"     //ERP-13614 [SJ]
            },this.Record),
            baseParams:{
                startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())
            }            
        });
        this.rowNo=new Wtf.KWLRowNumberer();     //ERP-13614 [SJ]
        this.pg = new Wtf.PagingSearchToolbar({
                id: 'pgTbarModule' + this.id,
                pageSize: 20,
                store: this.Store,
                displayInfo: true,
                displayMsg: 'Displaying records {0} - {1} of {2}',
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
                plugins: this.pP3 
            });
       
      
        this.Store.on('beforeload', function(){
            this.Store.baseParams = {
                //ss : this.quickPanelSearch.getValue(),
                startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())
            }
        
    }, this);
    this.Store.on('load',function(store,rec,option){           
            this.storeloaded(store);
        },this); 
//         this.Store.load({
//            params:{
//                start:0,          //ERP-13614 [SJ]
//                limit:this.pP3?this.pP3.combo.value:20 ,
//                startdate:this.startDate.getValue(),
//                enddate:this.endDate.getValue()
//            }
//        });
        this.grid = new Wtf.grid.GridPanel({
            autoScroll:true,
            store: this.Store,
            columns: [this.rowNo,{
                header:WtfGlobal.getLocaleText("acc.common.costCenter"),  // "Cost Center",
                dataIndex: "costcenterName",
                renderer:this.consolidateFlag?WtfGlobal.deletedRenderer:WtfGlobal.linkDeletedRenderer,
                pdfwidth:200
            },{
                header:WtfGlobal.getLocaleText("acc.ccReport.da") + " ("+WtfGlobal.getCurrencyName()+")",  //"Debit Amount",
                dataIndex:'debitAmount',
                renderer: WtfGlobal.currencyRenderer,
                hidecurrency : true,
                pdfwidth:200
            },{
                header:WtfGlobal.getLocaleText("acc.ccReport.ca") + " ("+WtfGlobal.getCurrencyName()+")",  //"Credit Amount",
                dataIndex:'creditAmount',
                renderer: WtfGlobal.currencyRenderer,
                hidecurrency : true,
                pdfwidth:200
            }],
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });   
        
        this.grid.on('cellclick',this.onCellClick, this);
        
        this.costCenterPanel=new Wtf.Panel({  //ERP-13614 [SJ]
            layout:'fit',
            border:false,
            items:[this.grid],
            bbar:this.pg
        });
        
        
        this.add(this.costCenterPanel);  //ERP-13614 [SJ]
        
        this.expButton=new Wtf.exportButton({
            obj:this,
            disabled:true,
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details.',
            id:"exportCostCenterSummary",
            filename: WtfGlobal.getLocaleText("acc.ccReport.tab1")+"_v1",
            menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
            get:912,
            label:WtfGlobal.getLocaleText("acc.ccReport.tab3")
        });
        
        this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            disabled:true,
            id:"printCostCenterSummary",
            tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
            menuItem:{print:true},
            get:912,
            label:WtfGlobal.getLocaleText("acc.ccReport.tab3")
        });
        
        this.fetchReport();
        
        this.temp = this.getTopToolbar();
        this.temp.add(this.expButton);
        this.temp.add(this.printButton);
    },

    getDates:function(start){
        var d=new Date();
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom){
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        }
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd){
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        }
        if(start){
            return fd;
        }
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },

    fetchReport: function(){
        var sDate=this.startDate.getValue();
        var eDate=this.endDate.getValue();

        if(sDate=="" || eDate=="") {
            WtfComMsgBox(42,2);
            return;
        }

        if(sDate>eDate){
            WtfComMsgBox(1,2);
            return;
        }
        sDate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        eDate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());

        this.Store.load({
            params:{
                start:0,    //ERP-13614 [SJ]
                limit:this.pP3.combo?this.pP3.combo.value:20 ,                
                startdate:sDate,
                enddate:eDate
            }
        });
        
        this.printButton.setParams({
            startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            name: WtfGlobal.getLocaleText("acc.ccReport.tabTitle"),
            fileType: 'print'
        });
        this.expButton.setParams({
            startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            name: WtfGlobal.getLocaleText("acc.ccReport.tabTitle")
        });
    },

    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="costcenterName"){
            this.viewTransaction(g,i,e);
        }
    },
    
    viewTransaction:function(){
        var formrec=null;
        var selModel = this.grid.getSelectionModel();
        if(!selModel.hasSelection() || selModel.getCount()>1){
            return;
        }
        formrec = selModel.getSelected();
        var detailsPanel = callCostCenterDetailsReport();
        detailsPanel.resetFilterAndFetchReport(formrec.data.costcenterid, this.startDate.getValue(), this.endDate.getValue());
        detailsPanel.LoadedOnActivate = true;
        var panel = Wtf.getCmp("CostCenterReport");
        if(panel!=null){
            Wtf.getCmp('CostCenterReport').setActiveTab(detailsPanel);
        }
    },
    
    storeloaded:function(store){
        this.grid.loadMask.hide();
        if(store.getCount()==0){
            if(this.expButton)this.expButton.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.expButton)this.expButton.enable();
            if(this.printButton)this.printButton.enable();
        }
    },
     handleResetClickNew:function()
    {
       var sDate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
       var eDate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
       this.startDate.reset();
       this.endDate.reset();
        this.Store.load({
            params:{
                start:0,   //ERP-13614 [SJ]
                limit:this.pP3.combo?this.pP3.combo.value:20 ,
                startdate:sDate,
                enddate:eDate
            }
        });
    }
});

