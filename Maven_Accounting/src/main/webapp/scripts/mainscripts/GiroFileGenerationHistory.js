/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */




function callGiroFileGenerationHistoryReport() {
    var panel = Wtf.getCmp("giroFileGenerationHistory");
    if (panel == null) {
        panel = new Wtf.account.GiroFileGenerationHistoryReport({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.uob.giroFileGenerationReport"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.uob.giroFileGenerationReport"),
            id: "giroFileGenerationHistory",
            iconCls: 'accountingbase invoicelist',
            layout: 'fit',
            closable: true,
            border: false
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}



Wtf.account.GiroFileGenerationHistoryReport = function(config) {
    Wtf.apply(this, config);
    this.createGrid();
    this.createTBar();
    Wtf.account.GiroFileGenerationHistoryReport.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.GiroFileGenerationHistoryReport, Wtf.Panel, {
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
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({
                            id: "pPageSize_" + this.id
                        })
                    })
                }]
        });

        this.add(this.leadpan);
        this.fetchStatement();

        Wtf.account.GiroFileGenerationHistoryReport.superclass.onRender.call(this, config);
    },
    createTBar: function() {
        this.btnArr = [];
        this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), // 'From',
            name: 'startdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: WtfGlobal.getDates(true)
        });
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.common.from"), this.startDate);

        this.endDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), // 'To',
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'enddate',
            value: WtfGlobal.getDates(false)
        });
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.common.to"), this.endDate);

        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
        this.btnArr.push('-', this.fetchBttn);
    },
    createGrid: function() {
        
        this.Rec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'generationdate',type: 'date'},
            {name: 'filename'}
        ]);
        
        this.Store = new Wtf.data.Store({
            url: "ACCCustomerCMN/getGiroFileGenerationHistory.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            },this.Rec)
        });
        this.cm = new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(), {
                header: WtfGlobal.getLocaleText("acc.importLog.fileName"),
                dataIndex: 'filename',
                width : 200
            }, 
            {
                header:WtfGlobal.getLocaleText("acc.uob.generationDate"),
                dataIndex: 'generationdate',
                align:'center',
                width : 200,
                renderer:WtfGlobal.onlyDateDeletedRenderer
            },
            {
                header:WtfGlobal.getLocaleText("acc.uob.downloadGeneratedFile"),
                dataIndex: '',
                align:'center',
                width : 200,
                renderer:function(val) {
                    return "<div class=\"pwnd downloadIcon submissionfile\" wtf:qtip=\"Download Generated File\" style=\"height:16px;\">&nbsp;</div>";
                }
            }
            ]);
        
        this.sm = new Wtf.grid.CheckboxSelectionModel({singleSelect: true});
        this.grid = new Wtf.grid.GridPanel({
            layout: 'fit',
            region: "center",
            store: this.Store,
            cm : this.cm,
            border: false,
            loadMask: true,
            sm: this.sm,
            viewConfig: {
                forceFit: false,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });

        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
        this.Store.on('load', this.handleStoreOnLoad, this);
        this.grid.on('rowclick', this.onRowClick, this);
    },
    fetchStatement: function() {
        this.sDate = this.startDate.getValue();
        this.eDate = this.endDate.getValue();

        if (this.sDate > this.eDate) {
            WtfComMsgBox(1, 2);
            return;
        }

        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value
            }
        });
    },
    handleStoreBeforeLoad: function() {
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        currentBaseParams.isMultiEntity = Wtf.account.companyAccountPref.isMultiEntity;
        currentBaseParams.bank = Wtf.IBGBanks.UOBBank;
        this.Store.baseParams = currentBaseParams;
    },
    handleStoreOnLoad: function(store) {
        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
    },
    onRowClick:function(grid,rowindex,e){
        if (e.getTarget(".submissionfile")) {
            var rec = this.grid.getSelectionModel().getSelections()[0].data;
            Wtf.get('downloadframe').dom.src = 'ACCCustomerCMN/downloadGiROFile.do?id=' + rec.id;
        }
    }
   
});