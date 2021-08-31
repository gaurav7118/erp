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


function getDriversTrackingReportTabViewDynamicLoad(){
    var panel = Wtf.getCmp("driversTrackingReport");
    if(panel == null) {
        panel = new Wtf.account.driversTrackingReport({
            id: 'driversTrackingReport',
            border: false,
            layout: 'fit',
            closable: true,
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.driversTrackingReport"), Wtf.TAB_TITLE_LENGTH), // "Driver's Tracking Report",
            tabTip: WtfGlobal.getLocaleText("acc.field.driversTrackingReport"),  // "Driver's Tracking Report",
            iconCls: 'accountingbase deliveryorder'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}


//********************************************************************************
Wtf.account.driversTrackingReport = function(config) {
    Wtf.apply(this, config);
     
    this.driversTrackingRec = Wtf.data.Record.create([
        {name:'deliveryOrderRef'},
        {name:'customerName'},
        {name:'deliveryAddress'},
        {name:'driverName'}
    ]);
    
    this.driversTrackingStore = new Wtf.data.Store({
        url:"ACCDeliveryPlanner/getDriversTrackingReport.do",
        reader: new Wtf.data.KwlJsonReader({
            totalProperty: "totalCount",
            root: "data"
        },this.driversTrackingRec)
    });
    
    //    this.driversTrackingStore.on('beforeload', function() {
    //        this.exportButton.enable()
    //    },this);
    
    this.driversTrackingStore.on('load', function(store) {
        if(this.driversTrackingStore.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        this.quickPanelSearch.StorageChanged(store);
    }, this);
    this.driversTrackingStore.on('datachanged', this.handleStoreDataChanged, this);
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    
    this.grid = new Wtf.grid.GridPanel({    
        store: this.driversTrackingStore,
        sm: this.sm,
        autoScroll: true,
        hirarchyColNumber: 0,
        border: false,
        layout: 'fit',
        viewConfig: {
            forceFit: true
        },
        loadMask: true,
        columns:[this.sm,
        {
            header: WtfGlobal.getLocaleText("acc.field.DeliveryOrderRef"), // "Delivery Order Ref",
            dataIndex: "deliveryOrderRef",
            renderer: WtfGlobal.deletedRenderer,
            pdfwidth:150
        },{
            header: WtfGlobal.getLocaleText("acc.invoiceList.cust"), // "Customer",
            dataIndex: "customerName",
            renderer: WtfGlobal.deletedRenderer,
            pdfwidth:150
        },{
            header: WtfGlobal.getLocaleText("acc.field.deliveryAddress"), // "Delivery Address",
            dataIndex: "deliveryAddress",
            pdfwidth:150,
            renderer : function(val) {
                val = val.replace(/(<([^>]+)>)/ig,"");
                return "<div wtf:qtip=\"" + val + "\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.field.deliveryAddress")+"'>" + val + "</div>";
            }
        },{
            header: WtfGlobal.getLocaleText("acc.field.driver"), // "Driver",
            dataIndex: "driverName",
            renderer: WtfGlobal.deletedRenderer,
            pdfwidth:150
        }]
    });
    
    //    this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);
    //    
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.driverTrackingQuickSearch.msg"), // "Search by Delivery Order Ref, Customer, Driver ...",
        width: 300,
        id:"quickSearchOfDriverTracking",
        field: 'deliveryOrderRef'
    });
    
    this.resetBttn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  // 'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  // 'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);
    
//    this.startDate = new Wtf.form.DateField({
//        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  // 'From',
//        name:'startdate',
//        format:WtfGlobal.getOnlyDateFormat(),
//       // readOnly:true,
//        value:WtfGlobal.getDates(true)
//    });
//    
//    this.endDate = new Wtf.form.DateField({
//        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  // 'To',
//        format:WtfGlobal.getOnlyDateFormat(),
//       // readOnly:true,
//        name:'enddate',
//        value:WtfGlobal.getDates(false)
//    });
//    
//    this.fetchBttn = new Wtf.Toolbar.Button({
//        text:WtfGlobal.getLocaleText("acc.common.fetch"),  // 'Fetch',
//        tooltip:WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"), // "Select a time period to view corresponding transactions.",
//        style:"margin-left: 6px;",
//        iconCls:'accountingbase fetch',
//        scope:this,
//        handler:this.fetchStatement                        
//    });
//    
//    this.exportButton = new Wtf.exportButton({
//        obj:this,
//        isEntrylevel:false,
//        id:"exportReports"+config.helpmodeid+config.id,
//        text: WtfGlobal.getLocaleText("acc.common.export"),
//        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
//        disabled :true,
//        menuItem:{
//            csv:true,
//            pdf:true
//        },
//        params:{
//          enddate :  WtfGlobal.convertToGenericDate(this.endDate.getValue()),
//          startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
//          isStockSummary : true
//        },
//        get:200
//    });
    
    this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
        filename: WtfGlobal.getLocaleText("acc.field.driversTrackingReport"),
        menuItem:{
            print:true
        },
        get: Wtf.autoNum.driverTrackingExport
    });
    
    this.exportButton = new Wtf.exportButton({
        obj:this,
        id:"exportReports"+config.helpmodeid+config.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
        filename: WtfGlobal.getLocaleText("acc.field.driversTrackingReport"),
        menuItem:{
            csv:true,
            pdf:true,
            xls: true
        },
        get:Wtf.autoNum.driverTrackingExport
    });
    Wtf.account.driversTrackingReport.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.driversTrackingReport,Wtf.Panel, {
    
    onRender: function(config) {
        this.driversTrackingStore.load({
            params:{
                start: 0,
                limit: 30
            }
        });
        
        this.tbar1 = new Array();
        this.bbar1 = [];
        this.tbar1.push(
            this.quickPanelSearch,
            this.resetBttn
//            '-',
//            WtfGlobal.getLocaleText("acc.common.from"), this.startDate,
//            WtfGlobal.getLocaleText("acc.common.to"), this.endDate,
//            this.fetchBttn,
//            this.viewLocationDetailBtn,
//            this.exportButton
            );
        this.bbar1.push('-',this.exportButton,'-',this.printButton);
        
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                region: 'center',
                layout: 'fit',
                //                bodyStyle: 'background-color:white; padding:0px 200px 0px 200px;',
                border: false,
                items: [this.grid],
                tbar: this.tbar1,
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.driversTrackingStore,
                    searchField: this.quickPanelSearch,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({
                        id : "pPageSize_"+this.id
                }),
                items:this.bbar1
                },this)
            }]
        }); 
        this.add(this.leadpan);
        
        Wtf.account.driversTrackingReport.superclass.onRender.call(this,config);
    },
    
    //    enableDisableButtons:function() {
    //        var rec = this.sm.getSelected();
    //        if(this.sm.getCount() == 1) {
    //            this.viewLocationDetailBtn.enable();
    //        } else {
    //            this.viewLocationDetailBtn.disable();
    //        }
    //    
//    },
//    
    handleResetClick:function() {
        if(this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.fetchStatement();
        }
    },
    
    fetchStatement:function() {
//        this.sDate=this.startDate.getValue();
//        this.eDate=this.endDate.getValue();
//        
//        if(this.sDate > this.eDate){
//            WtfComMsgBox(1,2);
//            return;
//        }
//        
//        var fromdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
//        var todate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        
        this.driversTrackingStore.load({
            params: {
//                startdate:fromdate,
//                enddate:todate,
                start: 0,
                limit: this.pP.combo.value
            }
        });
    },
    handleStoreDataChanged: function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    }
});