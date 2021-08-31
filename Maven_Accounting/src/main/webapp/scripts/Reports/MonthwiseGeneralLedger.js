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

function callMonthwiseGeneralLedgerReportDynamicLoad() {
    var panel = Wtf.getCmp("MonthwiseGeneralLedger");
    if (panel == null) {
        panel = new Wtf.account.MonthwiseGeneralLedgerReport({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.reportlist.mglr"), Wtf.TAB_TITLE_LENGTH), // "Monthwise General Ledger Report",
            tabTip: WtfGlobal.getLocaleText("acc.reportlist.mglr"),
            id: "MonthwiseGeneralLedger",
            iconCls:'accountingbase coa',
            layout: 'fit',
            closable: true,
            border: false
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.MonthwiseGeneralLedgerReport = function(config) {
    Wtf.apply(this, config);
   
    Wtf.override(Wtf.data.GroupingStore, {
    applySort : function(){
        Wtf.data.GroupingStore.superclass.applySort.call(this);
        if(!this.groupOnSort && !this.remoteGroup){
            var gs = this.getGroupState();
//new
            var si = this.sortInfo || {};
//change
            if(gs && gs != si.field){
//            if(gs && gs != this.sortInfo.field){
                this.sortData(this.groupField);
            }
        }
    }
});  
    this.createGrid();
    this.createTBar();
    
    Wtf.account.MonthwiseGeneralLedgerReport.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.MonthwiseGeneralLedgerReport, Wtf.Panel, {
    onRender: function(config) {
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar: this.btnArr,
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.Store,
                    searchField: this.quickPanelSearch,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({
                        id : "pPageSize_"+this.id
                    }),
                    items: this.bBarBtnArr
                })
            }]
        });
        
        this.add(this.leadpan);
        this.fetchStatement();
        
        Wtf.account.MonthwiseGeneralLedgerReport.superclass.onRender.call(this,config);
    },
    
    createTBar: function() {
        this.btnArr = [];
        this.bBarBtnArr = [];
        
        // Quick Search Component
        this.quickSearchTF = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.coa.accountSearchText"), //'Search by Name',
            width: 130,
            field: 'accname',
            Store:this.Store
        });
        this.btnArr.push(this.quickSearchTF); 
         
        // Create Reset button 
        this.resetBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            hidden:this.isSummary,
            tooltip :WtfGlobal.getLocaleText("acc.coa.resetTT"), //'Allows you to add a new search account name by clearing existing search account names.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls :getButtonIconCls(Wtf.etype.resetbutton),
            disabled :false
        });
        this.resetBttn.on('click',this.handleResetClick,this);
        this.btnArr.push(this.resetBttn);
        
        // Create Start Date button
        this.startDate=new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
            name:'stdate',
            format:WtfGlobal.getOnlyDateFormat(),
            value:this.getDates(true)
        });
        
        // Create End Date button
        this.endDate=new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
            format:WtfGlobal.getOnlyDateFormat(),
            name:'enddate',
            value:this.getDates(false)
        });
        
        this.btnArr.push("-");
        this.btnArr.push(WtfGlobal.getLocaleText("acc.common.from"));
        this.btnArr.push(this.startDate);
        this.btnArr.push(" ");
        this.btnArr.push(WtfGlobal.getLocaleText("acc.common.to"));
        this.btnArr.push(this.endDate);
        this.btnArr.push("-");
                
        //Create Account Combo Box
        this.accRec = Wtf.data.Record.create ([{
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
        }
        ]);
                
        this.accStore = new Wtf.data.Store({
            url : "ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignorecustomers:true,  
                ignorevendors:true,
                nondeleted:true,
                headerAdded:true,
                controlAccounts:true
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
                                                         
        this.btnArr.push(WtfGlobal.getLocaleText("acc.1099.selAcc"));
        this.btnArr.push(" ");
        this.btnArr.push(this.MultiSelectAccCombo);
                
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement                        
        });
        this.btnArr.push('-', this.fetchBttn);
        
        this.exportButton = new Wtf.exportButton({
            obj: this,
            id: "exportReports" + this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"),  // 'Export report details',
            filename: WtfGlobal.getLocaleText("acc.reportlist.mglr") + "_v1",
            disabled: true,
            scope: this,
            menuItem: {
                csv: true,
                pdf: true,
                xls: true
            },
            get: Wtf.autoNum.monthwiseGeneralLedgerReport
        });
        this.bBarBtnArr.push('-', this.exportButton);
        
        this.exportButton.on("click", function() {
            this.exportButton.setParams({
                startdate : this.sDate,
                stdate : this.sDate,
                enddate : this.eDate,
                accountIds : (this.MultiSelectAccCombo != undefined || this.MultiSelectAccCombo != null)?this.MultiSelectAccCombo.getValue():""
            });
        },this);
        
        this.printButton = new Wtf.exportButton({
            obj: this,
            text: WtfGlobal.getLocaleText("acc.common.print"),
            tooltip: WtfGlobal.getLocaleText("acc.sales.printTT"), // 'Print report details',
            disabled: true,
            filename: WtfGlobal.getLocaleText("acc.reportlist.mglr"),
            menuItem: {
                print: true
            },
            get: Wtf.autoNum.monthwiseGeneralLedgerReport
        });
        this.bBarBtnArr.push('-', this.printButton);
        
        this.printButton.on("click", function() {
            this.printButton.setParams({
                startDate : this.sDate,
                stdate : this.sDate,
                endDate : this.eDate,
                accountIds : (this.MultiSelectAccCombo != undefined || this.MultiSelectAccCombo != null)?this.MultiSelectAccCombo.getValue():""
            });
        },this);
    },
    
    createGrid: function() {
        this.Store = new Wtf.data.GroupingStore({
            url: "ACCReports/getMonthwiseGeneralLedgerReport.do",
            groupField:'accname', 
            remoteSort: true,
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            }),
            sortInfo: {field: 'accname',direction: "ASC"}            
        });
            
        this.grid = new Wtf.grid.GridPanel({
            layout: 'fit',
            region: "center",
            store: this.Store,
            border: false,
            loadMask: true,
            columns: [new Wtf.grid.RowNumberer({
                width: 30,
                pdfwidth:150
            }),{
                header: WtfGlobal.getLocaleText("acc.header.GeneralLedgerCode"),
                dataIndex:'glcode',
                width: 100,
                pdfwidth:150,
                renderer:WtfGlobal.deletedRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.coa.accCode"),
                dataIndex:'acccode',
                width: 150,
                pdfwidth:150,
                renderer:WtfGlobal.deletedRenderer                               
            },{
                header: WtfGlobal.getLocaleText("acc.product.description"),
                dataIndex:'accname',
                width: 150,
                pdfwidth:150,
                renderer:WtfGlobal.deletedRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.header.MTD"),
                dataIndex:'mtd',
                width: 150,
                pdfwidth:150,
                renderer:WtfGlobal.currencyDeletedRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.header.YTD"),
                dataIndex:'ytd',
                width: 150,
                pdfwidth:150,
                renderer:WtfGlobal.currencyDeletedRenderer
            }],
            view: new Wtf.grid.GroupingView({
                forceFit: true
            })
        });

        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
        this.Store.on('load', this.handleStoreOnLoad, this);
        this.Store.on('datachanged', this.handleStoreDataChanged, this);
    },
    
    handleResetClick: function() {
        if (this.quickSearchTF.getValue()) {
            this.quickSearchTF.reset();
            this.fetchStatement();
        }
    },
    
    fetchStatement: function() {
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value
            }
        });
    },
    
    handleStoreBeforeLoad: function() {
        var currentBaseParams = this.Store.baseParams;
        if(this.startDate != undefined || this.startDate != null){
            this.sDate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        }else{
            this.sDate=WtfGlobal.convertToGenericStartDate(this.getDates(true));
        }
        if(this.endDate != undefined || this.endDate != null){
            this.eDate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        }else{
            this.eDate=WtfGlobal.convertToGenericEndDate(this.getDates(false));
        }
            
        currentBaseParams.startDate=this.sDate;
        currentBaseParams.stdate=this.sDate;
        currentBaseParams.endDate=this.eDate;
        currentBaseParams.isGeneralLedger=true;
        currentBaseParams.accountIds=(this.MultiSelectAccCombo != undefined || this.MultiSelectAccCombo != null)?this.MultiSelectAccCombo.getValue():"";
        
        this.exportButton.enable();
        this.printButton.enable();
    },
    
    handleStoreOnLoad: function(store) {           
        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        this.quickSearchTF.StorageChanged(store);
    },
    
    handleStoreDataChanged: function() {
        var p = this.pP.combo.value;
        this.quickSearchTF.setPage(p);
    },
    
    getDates:function(start){
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
    }
});