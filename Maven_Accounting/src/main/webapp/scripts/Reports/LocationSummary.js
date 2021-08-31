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

function getLocationSummaryReportTabViewDynamicLoad(params) {
    /*
     *Function to add this report in widget report.
     **/
    if (params.isCustomWidgetReport) {
       var panel = new Wtf.account.TransactionListPanelViewLocationSummary({
                border: false,
                layout: 'fit',
                closable: true,
                isCustomWidgetReport : params.isCustomWidgetReport
            });
        if (params.callbackFn) {
            /*
             *call callback function to add this report to widget.
             **/
            params.callbackFn.call(this, panel);
        }
    } else {
        var reportPanel = Wtf.getCmp('locationsummary');
    if(reportPanel == null){
            reportPanel = new Wtf.account.TransactionListPanelViewLocationSummary({
            id : 'locationsummary',
            border : false,
                title: WtfGlobal.getLocaleText("acc.locationSummary.tabtitle"), // "Stock Valuation Summary Report",
                tabTip: WtfGlobal.getLocaleText("acc.locationSummary.tabtitle"), // "Stock Valuation Summary Report",
                layout: 'fit',
            closable : true,
            iconCls:getButtonIconCls(Wtf.etype.stockvaluationSummary)
            });
            Wtf.getCmp('as').add(reportPanel);
        }
        Wtf.getCmp('as').setActiveTab(reportPanel);
        Wtf.getCmp('as').doLayout();
    }
}


Wtf.account.TransactionListPanelViewLocationSummary = function(config) {
    Wtf.apply(this, config);

    Wtf.account.TransactionListPanelViewLocationSummary.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.TransactionListPanelViewLocationSummary,Wtf.Panel, {
        onRender: function(config){
        this.GridRec = Wtf.data.Record.create([
        {name:'locationid'},
        {name:'locationName'},
        {name:'value'}
        ]);

        this.LocationSummaryStore = new Wtf.data.Store({
        url:"ACCProductCMN/getLocationSummary.do",
            reader: new Wtf.data.KwlJsonReader({
            totalProperty:"totalCount",
                root: "data"
        },this.GridRec)
        });

    this.LocationSummaryStore.on('beforeload', function() {
            WtfGlobal.setAjaxTimeOut();
            this.exportButton.enable()
            this.LocationSummaryStore.baseParams.startdate=WtfGlobal.convertToGenericDate(this.startDate.getValue());
            this.LocationSummaryStore.baseParams.type=this.locTypeEditor.getValue();
            this.LocationSummaryStore.baseParams.enddate=WtfGlobal.convertToGenericDate(this.endDate.getValue());

        },this);
        this.LocationSummaryStore.on('loadexception', function() {
            WtfGlobal.resetAjaxTimeOut();
        }, this);
        this.LocationSummaryStore.on('load', function(store) {
            WtfGlobal.resetAjaxTimeOut();
            if(this.LocationSummaryStore.getCount() < 1) {
                this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.grid.getView().refresh();
            }
            this.quickPanelSearch.StorageChanged(store);
        }, this);

        this.sm = new Wtf.grid.CheckboxSelectionModel();

        this.grid = new Wtf.grid.GridPanel({
        store:this.LocationSummaryStore,
        sm:this.sm,
        autoScroll:true,
        hirarchyColNumber:0,
        border:true,
        layout:'fit',
            viewConfig: {
                //forceFit:true
            },
        loadMask:true,
        columns:[this.sm,
                {
                header:WtfGlobal.getLocaleText("acc.masterConfig.12"), // "Location",
                dataIndex:"locationName",
                pdfwidth:200
            },{
                header:WtfGlobal.getLocaleText("acc.stockLedger.Value"), // "Value",
                dataIndex:"value",
                align:"right",
                pdfwidth:200,
                renderer:WtfGlobal.currencyDeletedRenderer
                }]
        });

    this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);

        this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.locationSummary.QuickSearchEmptyText"), // "Search by Location ...",
        width: this.isCustomWidgetReport ? 100 : 300,
        id:"quickSearch"+config.helpmodeid,
            field: 'locationName'
        });

        this.resetBttn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  // 'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  // 'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        hidden : this.isCustomWidgetReport,
        disabled :false
        });
    this.resetBttn.on('click',this.handleResetClick,this);

        this.startDate = new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  // 'From',
        name:'startdate',
        format:WtfGlobal.getOnlyDateFormat(),
            // readOnly:true,
        value:WtfGlobal.getDates(true)
        });

        this.endDate = new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  // 'To',
        format:WtfGlobal.getOnlyDateFormat(),
            // readOnly:true,
        name:'enddate',
        value:WtfGlobal.getDates(false)
        });

        this.viewLocationDetailBtn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.lp.viewccd"), // 'View Details',
        tooltip :WtfGlobal.getLocaleText("acc.lp.viewccd"), // 'View Details',
            id: 'btnviewLocationDetailBtn' + this.id,
            scope: this,
        iconCls :getButtonIconCls(Wtf.etype.reorderreport),
        hidden :  this.isCustomWidgetReport,
        disabled :true
        });

        this.fetchBttn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  // 'Fetch',
        tooltip:WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"), // "Select a time period to view corresponding transactions.",
        style:"margin-left: 6px;",
        iconCls:'accountingbase fetch',
        scope:this,
        handler:this.fetchStatement                        
        });

    this.viewLocationDetailBtn.on('click', function(){
            var rec = this.sm.getSelected();
        var type=this.locTypeEditor.getValue();
        getStockValuationDetailReportTabView(rec,type);
        }, this);
        this.locTypeRec = new Wtf.data.Record.create([
        {name:"levelId"},
        {name:"levelName"},
        ]);

        this.locTypeReader = new Wtf.data.KwlJsonReader({
        root:"data"
    },this.locTypeRec);

        this.locTypeStore = new Wtf.data.Store({
        url:"ACCMaster/getLevelsCombo.do",
        reader:this.locTypeReader
        });
        this.locTypeStore.load();
        this.locTypeEditor = new Wtf.form.ComboBox({
        triggerAction:'all',
        mode: 'local',
        fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.12"),
        valueField:'levelId',
        displayField:'levelName',
        store:this.locTypeStore,
        emptyText:WtfGlobal.getLocaleText("acc.stockValuationDetail.LocEmptyText"),
        anchor:'90%',
        width : this.isCustomWidgetReport ? 75 :200,
        value:this.record ? this.record.data.locationid : '',
            typeAhead: true,
            forceSelection: true,
        name:'locationtp'
        });

        this.exportButton = new Wtf.exportButton({
        obj:this,
        isEntrylevel:false,
        id:"exportReports"+config.helpmodeid+config.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
        disabled :true,
        hidden :  this.isCustomWidgetReport,
        filename : WtfGlobal.getLocaleText("acc.locationSummary.tabtitle")+"_v1",
        menuItem:{
            csv:true,
            pdf:true,
            xls:true
            },
        params:{
          enddate :  WtfGlobal.convertToGenericDate(this.endDate.getValue()),
          startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
          locationType:this.locTypeEditor.getValue(),
          isStockSummary : true,
          ss:this.quickPanelSearch.getValue()
            },
          get:Wtf.autoNum.StockValuationSummary
        });

        this.exportButton.on("click", function () {
            this.exportButton.setParams({
                ss: this.quickPanelSearch.getValue(),
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                locationType: this.locTypeEditor.getValue(),
                isStockSummary: true,
                isExport: true,
            });
        }, this);

        this.LocationSummaryStore.load({
            params:{
                type:1,
                start:0,
                limit:30
            }
        });

        this.tbar1 = new Array();
        this.tbar1.push(
                this.quickPanelSearch,
                this.resetBttn,
                '-',
                WtfGlobal.getLocaleText("acc.common.from"), this.startDate,
            WtfGlobal.getLocaleText("acc.common.to"), this.endDate,this.locTypeEditor,
                this.fetchBttn,
                this.viewLocationDetailBtn,
                this.exportButton
                );
        var bodyStyle = "background-color:white;";
        if(this.isCustomWidgetReport !== true){
            bodyStyle += "padding:0px 200px 0px 200px;";
        }

        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                    region: 'center',
                    layout: 'fit',
                    bodyStyle:bodyStyle,
                    border: false,
                    items: [this.grid],
                    tbar: this.tbar1,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.LocationSummaryStore,
                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({
                        id : "pPageSize_"+this.id
                        })
                    })
                }]
        });
        this.add(this.leadpan);

        Wtf.account.TransactionListPanelViewLocationSummary.superclass.onRender.call(this,config);
    },
    
    enableDisableButtons:function() {
        var rec = this.sm.getSelected();
        if(this.sm.getCount() == 1) {
            this.viewLocationDetailBtn.enable();
        } else {
            this.viewLocationDetailBtn.disable();
        }

    },
    
    handleResetClick:function() {
        if(this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.fetchStatement();
        }
    },
        fetchStatement:function() {
        this.colmodel = this.grid.getColumnModel();
        if(this.locTypeEditor.getRawValue()!=""){
            this.colmodel.setColumnHeader(1,this.locTypeEditor.getRawValue());
        }
        this.sDate=this.startDate.getValue();
        this.eDate=this.endDate.getValue();

        if(this.sDate > this.eDate){
            WtfComMsgBox(1,2);
            return;
        }

        var fromdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        var todate = WtfGlobal.convertToGenericDate(this.endDate.getValue());

        this.LocationSummaryStore.load({
            params: {
                startdate:fromdate,
                enddate:todate,
                start:0,
                limit:this.pP.combo.value,
                type:this.locTypeEditor.getValue(),
                ss:this.quickPanelSearch.getValue()
            }
        });
    }
});


