/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



function callGstFormGenerationHistoryReport() {
    var panel = Wtf.getCmp("gstFormGenerationHistory");
    if (panel == null) {
        panel = new Wtf.account.GstFormGenerationHistoryReport({
            title: Wtf.util.Format.ellipsis("View GST Form Generation History", Wtf.TAB_TITLE_LENGTH), // "GST Form Generation History",
            tabTip: "View GST Form Generation History Report",
            id: "gstFormGenerationHistory",
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



Wtf.account.GstFormGenerationHistoryReport = function(config) {
    Wtf.apply(this, config);
    this.createGrid();
    this.createTBar();
    Wtf.account.GstFormGenerationHistoryReport.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.GstFormGenerationHistoryReport, Wtf.Panel, {
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

        Wtf.account.GstFormGenerationHistoryReport.superclass.onRender.call(this, config);
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
        //ERP-40235 : Delete Button to delete GST Form 03 Generation History
        this.deleteBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.malaysiangst.deleteGSTForm3"),
            tooltip: WtfGlobal.getLocaleText("acc.malaysiangst.deleteGSTForm3ToolTip"),
            iconCls: getButtonIconCls(Wtf.etype.deletebutton),
            handler: this.deleteGenerationHistory.createDelegate(this)
        });
        this.btnArr.push('-', this.fetchBttn, this.deleteBttn);
        
    },
    createGrid: function() {
        this.Store = new Wtf.data.Store({
            url: "AccGST/getGSTFormGenerationHistory.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
        this.sm = new Wtf.grid.CheckboxSelectionModel({singleSelect: true});
        this.grid = new Wtf.grid.GridPanel({
            layout: 'fit',
            region: "center",
            store: this.Store,
            columns: [],
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
    deleteGenerationHistory: function () {
        /*
         * Function to send request to delete GST Form 03 Generation History
         */
        if (this.grid.getSelectionModel().hasSelection()) {
            var rec = this.grid.getSelectionModel().getSelections()[0].data;
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"), WtfGlobal.getLocaleText("acc.gst.comfirmDeleteGenHistory"), function (btn) {
                if (btn == "yes") {
                    Wtf.Ajax.requestEx({
                        method: 'POST',
                        url: "AccGST/deleteGSTFileGenerationHistory.do",
                        params: {
                            id: rec.id,
                            multiEntityId: rec.entityid,
                            startdate: WtfGlobal.convertToGenericDate(rec.startdate),
                            enddate: WtfGlobal.convertToGenericDate(rec.enddate)
                        }
                    },
                    this,
                            function (response, request) {
                                var msg = response.msg;
                                if (response.success) {
                                    this.Store.reload();
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), msg], 3);
                                } else {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                                }
                            },
                            function (response, request) {
                                var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";                                
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                            });
                }
            }, this);
        } else {

            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.gst.selectHistoryToDelete")], 2);
            return;
        }

    },
    handleStoreBeforeLoad: function() {
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        currentBaseParams.isMultiEntity = Wtf.account.companyAccountPref.isMultiEntity;
        this.Store.baseParams = currentBaseParams;
    },
    handleStoreOnLoad: function(store) {
        var columns = [];
        columns.push(this.sm);
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if (column.dataIndex == "filename") {
                column.renderer = function(val) {
                    return "<div class=\"pwnd downloadIcon submissionfile\" wtf:qtip=\"Download Submission File\" style=\"height:16px;\">&nbsp;</div>";
                }
            } else {
                if (column.renderer) {
                    column.renderer = eval('(' + column.renderer + ')');
                }
            }
            columns.push(column);
        });
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();

        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
    },
    onRowClick:function(grid,rowindex,e){
        if (e.getTarget(".submissionfile")) {
            var rec = this.grid.getSelectionModel().getSelections()[0].data;
            Wtf.get('downloadframe').dom.src = 'AccGST/downloadSubmissionFile.do?id=' + rec.id;
        }
    }
   
});