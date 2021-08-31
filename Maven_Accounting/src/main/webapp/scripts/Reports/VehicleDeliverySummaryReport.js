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
    
function getVehicleDeliverySummaryReportTabViewDynamicLoad() {
    var reportPanel = Wtf.getCmp('vehicleDeliverySummaryReport');
    if (reportPanel == null) {
        reportPanel = new Wtf.account.TransactionListPanelViewVehicleDeliverySummary({
            id: 'vehicleDeliverySummaryReport',
            border: false,
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.reportList.logVehicalMonthlySummery"), Wtf.TAB_TITLE_LENGTH), // "Vehicle Delivery Report (Summary for the Month)",
            tabTip: WtfGlobal.getLocaleText("acc.reportList.logVehicalMonthlySummery"), // "Vehicle Delivery Report (Summary for the Month)",
            layout: 'fit',
            closable : true,
            iconCls: 'accountingbase deliveryorder',
            isVehicleDeliverySummaryReport: true
        });
        Wtf.getCmp('as').add(reportPanel);
    }
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}

function getIndividualVehicleDeliveryReportTabViewDynamicLoad(record) {
    var reportPanel = Wtf.getCmp('individualVehicleDeliverySummaryReport');
    if (reportPanel == null) {
        reportPanel = new Wtf.account.TransactionListPanelViewVehicleDeliverySummary({
            id: 'individualVehicleDeliverySummaryReport',
            border: false,
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.reportList.logVehicalMonthlyIndividualDetails"), Wtf.TAB_TITLE_LENGTH), // "Individual Vehicle Delivery Report (Detailed for the Month)",
            tabTip: WtfGlobal.getLocaleText("acc.reportList.logVehicalMonthlyIndividualDetails"), // "Individual Vehicle Delivery Report (Detailed for the Month)",
            layout: 'fit',
            closable : true,
            iconCls: 'accountingbase deliveryorder',
            isIndividualVehicleDeliveryReport: true,
            record: record
        });
        Wtf.getCmp('as').add(reportPanel);
    }
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}

function getIndividualVehicleDOPOReportTabViewDynamicLoad(record) {
    var reportPanel = Wtf.getCmp('individualVehicleDOPOSummaryReport');
    if (reportPanel == null) {
        reportPanel = new Wtf.account.TransactionListPanelViewVehicleDeliverySummary({
            id: 'individualVehicleDOPOSummaryReport',
            border: false,
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.reportList.logVehicalDailyIndividualDetails"), Wtf.TAB_TITLE_LENGTH), // "Individual Vehicle Delivery Report (DO/PO for the Day)",
            tabTip: WtfGlobal.getLocaleText("acc.reportList.logVehicalDailyIndividualDetails"), // "Individual Vehicle Delivery Report (DO/PO for the Day)",
            layout: 'fit',
            closable : true,
            iconCls: 'accountingbase deliveryorder',
            isIndividualVehicleDOPOReport: true,
            record: record
        });
        Wtf.getCmp('as').add(reportPanel);
    }
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}

function getDriverDeliverySummaryReportTabViewDynamicLoad() {
    var reportPanel = Wtf.getCmp('driverDeliverySummaryReport');
    if (reportPanel == null) {
        reportPanel = new Wtf.account.TransactionListPanelViewVehicleDeliverySummary({
            id: 'driverDeliverySummaryReport',
            border: false,
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.reportList.logDriverMonthlySummery"), Wtf.TAB_TITLE_LENGTH), // "Driver Delivery Report (Summary for the Month)",
            tabTip: WtfGlobal.getLocaleText("acc.reportList.logDriverMonthlySummery"), // "Driver Delivery Report (Summary for the Month)",
            layout: 'fit',
            closable : true,
            iconCls: 'accountingbase deliveryorder',
            isDriverDeliverySummaryReport: true
        });
        Wtf.getCmp('as').add(reportPanel);
    }
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}


function getIndividualDriverDOPOReportTabViewDynamicLoad(record) {
    var reportPanel = Wtf.getCmp('individualDriverDOPOSummaryReport');
    if (reportPanel == null) {
        reportPanel = new Wtf.account.TransactionListPanelViewVehicleDeliverySummary({
            id: 'individualDriverDOPOSummaryReport',
            border: false,
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.reportList.logDriverDailyIndividualDetails"), Wtf.TAB_TITLE_LENGTH), // "Individual Driver Delivery Report (DO/PO for the Day)",
            tabTip: WtfGlobal.getLocaleText("acc.reportList.logDriverDailyIndividualDetails"), // "Individual Driver Delivery Report (DO/PO for the Day)",
            layout: 'fit',
            closable : true,
            iconCls: 'accountingbase deliveryorder',
            isIndividualDriverDOPOReport: true,
            record: record
        });
        Wtf.getCmp('as').add(reportPanel);
    }
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}




function getIndividualDriverDeliveryReportTabViewDynamicLoad(record) {
    var reportPanel = Wtf.getCmp('individualDriverDeliverySummaryReport');
    if (reportPanel == null) {
        reportPanel = new Wtf.account.TransactionListPanelViewVehicleDeliverySummary({
            id: 'individualDriverDeliverySummaryReport',
            border: false,
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.reportList.logDriverMonthlyIndividualDetails"), Wtf.TAB_TITLE_LENGTH), // "Individual Driver Delivery Report (Detailed for the Month)",
            tabTip: WtfGlobal.getLocaleText("acc.reportList.logDriverMonthlyIndividualDetails"), // "Individual Driver Delivery Report (Detailed for the Month)",
            layout: 'fit',
            closable : true,
            iconCls: 'accountingbase deliveryorder',
            isIndividualDriverDeliveryReport: true,
            record: record
        });
        Wtf.getCmp('as').add(reportPanel);
    }
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.TransactionListPanelViewVehicleDeliverySummary = function(config) {
    this.isVehicleDeliverySummaryReport = (config.isVehicleDeliverySummaryReport != null && config.isVehicleDeliverySummaryReport != undefined)? config.isVehicleDeliverySummaryReport : false;
    this.isIndividualVehicleDeliveryReport = (config.isIndividualVehicleDeliveryReport != null && config.isIndividualVehicleDeliveryReport != undefined)? config.isIndividualVehicleDeliveryReport : false;
    this.isIndividualVehicleDOPOReport = (config.isIndividualVehicleDOPOReport != null && config.isIndividualVehicleDOPOReport != undefined)? config.isIndividualVehicleDOPOReport : false;
    
    this.isDriverDeliverySummaryReport = (config.isDriverDeliverySummaryReport != null && config.isDriverDeliverySummaryReport != undefined)? config.isDriverDeliverySummaryReport : false;
    this.isIndividualDriverDeliveryReport = (config.isIndividualDriverDeliveryReport != null && config.isIndividualDriverDeliveryReport != undefined)? config.isIndividualDriverDeliveryReport : false;
    this.isIndividualDriverDOPOReport = (config.isIndividualDriverDOPOReport != null && config.isIndividualDriverDOPOReport != undefined)? config.isIndividualDriverDOPOReport : false;
    
    this.record = config.record;
    
    Wtf.apply(this, config);
    
    // To Toolbar Menu items
    this.createToolbarMenu();
    
    if (this.record) {
        if (this.isIndividualVehicleDeliveryReport || this.isIndividualDriverDeliveryReport) {
            this.startMonth.setValue(this.record.data.startMonth);
            this.startYear.setValue(this.record.data.startYear);
            this.vehicleNo.setValue(this.record.data.id);
            this.modules.setValue(this.record.data.moduleid);
        }
        
        if (this.isIndividualVehicleDOPOReport || this.isIndividualDriverDOPOReport) {
            this.startDate.setValue(this.record.data.pushDate);
            this.vehicleNo.setValue(this.record.data.id);
            this.modules.setValue(this.record.data.moduleid);
        }
    }
    
    // To Create Grid and its Store and Column model
    this.createGridPanel();
    
    this.VehicleDeliverySummaryStore.on('beforeload', function() {
        var fromdate = "";
        if (this.isVehicleDeliverySummaryReport) {
            fromdate = this.startMonth.getValue() + ", " + this.startYear.getValue();
            this.VehicleDeliverySummaryStore.baseParams.startdate = fromdate;
            this.VehicleDeliverySummaryStore.baseParams.isVehicleDeliverySummaryReport = this.isVehicleDeliverySummaryReport;
        } else if (this.isIndividualVehicleDeliveryReport) {
            fromdate = this.startMonth.getValue() + ", " + this.startYear.getValue();
            this.VehicleDeliverySummaryStore.baseParams.startdate = fromdate;
            this.VehicleDeliverySummaryStore.baseParams.vehicleNo = this.vehicleNo.getValue();
            this.VehicleDeliverySummaryStore.baseParams.isIndividualVehicleDeliveryReport = this.isIndividualVehicleDeliveryReport;
        } else if (this.isIndividualVehicleDOPOReport) {
            this.VehicleDeliverySummaryStore.baseParams.startDate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
            this.VehicleDeliverySummaryStore.baseParams.vehicleNo = this.vehicleNo.getValue();
            this.VehicleDeliverySummaryStore.baseParams.isIndividualVehicleDOPOReport = this.isIndividualVehicleDOPOReport;
        } else if (this.isDriverDeliverySummaryReport) {
            fromdate = this.startMonth.getValue() + ", " + this.startYear.getValue();
            this.VehicleDeliverySummaryStore.baseParams.startdate = fromdate;
            this.VehicleDeliverySummaryStore.baseParams.isDriverDeliverySummaryReport = this.isDriverDeliverySummaryReport;
        } else if (this.isIndividualDriverDeliveryReport) {
            fromdate = this.startMonth.getValue() + ", " + this.startYear.getValue();
            this.VehicleDeliverySummaryStore.baseParams.startdate = fromdate;
            this.VehicleDeliverySummaryStore.baseParams.vehicleNo = this.vehicleNo.getValue();
            this.VehicleDeliverySummaryStore.baseParams.isIndividualDriverDeliveryReport = this.isIndividualDriverDeliveryReport;
        } else if(this.isIndividualDriverDOPOReport) {
            this.VehicleDeliverySummaryStore.baseParams.startDate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
            this.VehicleDeliverySummaryStore.baseParams.vehicleNo = this.vehicleNo.getValue();
            this.VehicleDeliverySummaryStore.baseParams.isIndividualDriverDOPOReport = this.isIndividualDriverDOPOReport;
        }
        this.VehicleDeliverySummaryStore.baseParams.moduleid = this.modules.getValue();
    },this);
    
    this.VehicleDeliverySummaryStore.on('load', function(store) {
        if (this.VehicleDeliverySummaryStore.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        if (this.isIndividualVehicleDOPOReport || this.isIndividualDriverDOPOReport) {
            var rec = store.getAt(0);
            if (rec != undefined) {
                this.noOfTrips.setValue(rec.data.noOfTrips);
                this.noOfDoPo.setValue(rec.data.noOfDoPo);
            } else {
                this.noOfTrips.setValue(0);
                this.noOfDoPo.setValue(0);
            }
        }
    }, this);
    
    Wtf.account.TransactionListPanelViewVehicleDeliverySummary.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.TransactionListPanelViewVehicleDeliverySummary, Wtf.Panel, {
    
    onRender: function(config) {
        this.VehicleDeliverySummaryStore.load({
            params: {
                start: 0,
                limit: 30
            }
        });
        
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                    region: 'center',
                    layout: 'fit',
                    bodyStyle: 'background-color:white; padding:0px 200px 0px 200px;',
                    border: false,
                    items: [this.grid],
                    tbar: this.tbar1,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.VehicleDeliverySummaryStore,
//                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), // "No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({
                            id : "pPageSize_"+this.id
                        })
                    })
            }]
        }); 
        this.add(this.leadpan);
        
        Wtf.account.TransactionListPanelViewVehicleDeliverySummary.superclass.onRender.call(this, config);
    },
    
    createToolbarMenu: function() {
        this.tbar1 = new Array();
        
        this.moduleStore = new Wtf.data.SimpleStore({
            fields: [{name:'id', type:'int'}, 'name'],
            data :[[Wtf.Acc_Invoice_ModuleId,'Sales Invoice'], [Wtf.Acc_Purchase_Order_ModuleId,'Purchase Order']]
        });
        this.modules = new Wtf.form.ComboBox({
            store: this.moduleStore,
            width: 100,
            name: 'modules',
            displayField: 'name',
            valueField: 'id',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus: true,
            editable: false
        });
        this.modules.setValue(Wtf.Acc_Invoice_ModuleId);
        this.tbar1.push(this.modules);
        
        this.startDate = new Wtf.ExDateFieldQtip({
            name: 'startdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value:new Date()
        });
        this.startDate.setValue(new Date());
        
        if (this.isIndividualVehicleDOPOReport || this.isIndividualDriverDOPOReport) {
            this.tbar1.push(WtfGlobal.getLocaleText("acc.stockLedger.Date"), this.startDate);
        }
    
        this.monthStore = new Wtf.data.SimpleStore({
            fields: [{name:'monthid', type:'int'}, 'name'],
            data :[
                [0,WtfGlobal.getLocaleText("acc.field.January")],
                [1,WtfGlobal.getLocaleText("acc.field.February")],
                [2,WtfGlobal.getLocaleText("acc.field.March")],
                [3,WtfGlobal.getLocaleText("acc.field.April")],
                [4,WtfGlobal.getLocaleText("acc.field.May")],
                [5,WtfGlobal.getLocaleText("acc.field.June")],
                [6,WtfGlobal.getLocaleText("acc.field.July")],
                [7,WtfGlobal.getLocaleText("acc.field.August")],
                [8,WtfGlobal.getLocaleText("acc.field.September")],
                [9,WtfGlobal.getLocaleText("acc.field.October")],
                [10,WtfGlobal.getLocaleText("acc.field.November")],
                [11,WtfGlobal.getLocaleText("acc.field.December")]
            ]
        });
    
        this.startMonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            fieldLabel: WtfGlobal.getLocaleText("acc.accPref.month"),  // 'Month',
            name: 'startMonth',
            hiddenName: 'startMonth',
            displayField: 'name',
            forceSelection: true,
            anchor: '95%',
            valueField: 'name',
            mode: 'local',
            width: 90,
            triggerAction: 'all',
            selectOnFocus: true,
            typeAhead: true
        });
        
        if (this.isVehicleDeliverySummaryReport || this.isIndividualVehicleDeliveryReport || this.isDriverDeliverySummaryReport || this.isIndividualDriverDeliveryReport) {
            this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
            this.tbar1.push(WtfGlobal.getLocaleText("acc.field.forTheMonthOf"), this.startMonth);
        }
    
        var data = WtfGlobal.getBookBeginningYear(true);
    
        this.yearStore = new Wtf.data.SimpleStore({
            fields: [{name:'id', type:'int'}, 'yearid'],
            data : data
        });
    
        this.startYear = new Wtf.form.ComboBox({
            store: this.yearStore,
            fieldLabel: WtfGlobal.getLocaleText("acc.accPref.year"),  // 'Year',
            name: 'startYear',
            displayField: 'yearid',
            valueField: 'yearid',
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            width: 90,
            selectOnFocus: true
        });
        this.startYear.setValue(this.yearStore.data.items[0].json[1]);
        
        if (this.isVehicleDeliverySummaryReport || this.isIndividualVehicleDeliveryReport || this.isDriverDeliverySummaryReport || this.isIndividualDriverDeliveryReport) {
            this.tbar1.push(this.startYear);
        }
        
        this.vehicleRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);

        this.vehicleStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.vehicleRec),
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode:112,
                groupid: (this.isIndividualVehicleDeliveryReport || this.isIndividualVehicleDOPOReport)? 25 : 26
            }
        });
    
        this.vehicleNo = new Wtf.form.ComboBox({
            name: 'name',
            hiddenName: 'vehicleNo',
            triggerAction: 'all',
            store: this.vehicleStore,
            mode: 'local',
            valueField: 'id',
            displayField: 'name',
            width: 90,
            typeAhead: true,
            forceSelection: true
        });
    
        if (this.isIndividualVehicleDeliveryReport || this.isIndividualVehicleDOPOReport || this.isIndividualDriverDeliveryReport || this.isIndividualDriverDOPOReport) {
            this.vehicleStore.load();
            
            if (this.isIndividualVehicleDeliveryReport || this.isIndividualVehicleDOPOReport) {
                this.tbar1.push('-', WtfGlobal.getLocaleText("acc.field.vehicleNo"));
            } else if (this.isIndividualDriverDeliveryReport || this.isIndividualDriverDOPOReport) {
                this.tbar1.push('-', WtfGlobal.getLocaleText("acc.field.driver"));
            }
            this.tbar1.push(this.vehicleNo);
        
            this.vehicleNo.store.on("load", function() {
                var record = new Wtf.data.Record({
                    id: "",
                    name: "All Records"
                });
                this.vehicleNo.store.insert(0, record);
                this.vehicleNo.setValue("");
        
                if (this.record != undefined) {
                    this.vehicleNo.setValue(this.record.data.id);
                }
            }, this);
        }
    
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"),  // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
        this.tbar1.push(this.fetchBttn);
        
        this.noOfTrips = new Wtf.form.NumberField({
            allowNegative: false,
            defaultValue: 0,
            readOnly: true,
            maxLength: 10,
            width: 50
        });
        if (this.isIndividualVehicleDOPOReport || this.isIndividualDriverDOPOReport) {
            this.tbar1.push('-', WtfGlobal.getLocaleText("acc.field.noOfTrips"), this.noOfTrips);
        }
        
        this.noOfDoPo = new Wtf.form.NumberField({
            allowNegative: false,
            defaultValue: 0,
            readOnly: true,
            maxLength: 10,
            width: 50
        });
        if (this.isIndividualVehicleDOPOReport || this.isIndividualDriverDOPOReport) {
            this.tbar1.push('-', WtfGlobal.getLocaleText("acc.field.noOfDOPO"), this.noOfDoPo);
        }
    },
    
    createGridPanel: function() {
        this.VehicleDeliverySummaryRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'pushDate', type: 'date'},
            {name: 'pushDay'},
            {name: 'doPoName'},
            {name: 'tripNo'},
            {name: 'tripDesc'},
            {name: 'vehicleNo'},
            {name: 'noOfTrips'},
            {name: 'noOfDoPo'},
            {name: 'driver'}
        ]);
        
        var url = "";
        if (this.isVehicleDeliverySummaryReport || this.isDriverDeliverySummaryReport) {
            url = "ACCDeliveryPlanner/getVehicleDliverySummaryReport.do";
        } else if (this.isIndividualVehicleDeliveryReport || this.isIndividualDriverDeliveryReport) {
            url = "ACCDeliveryPlanner/getIndividualVehicleDliveryReport.do";
        } else if (this.isIndividualVehicleDOPOReport || this.isIndividualDriverDOPOReport) {
            url = "ACCDeliveryPlanner/getIndividualVehicleDOPOReport.do";
        }

        this.VehicleDeliverySummaryStore = new Wtf.data.Store({
            url: url,
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: "totalCount",
                root: "data"
            }, this.VehicleDeliverySummaryRec)
        });
        
        // Column's of Grid
        var columnArr = [];
        
//        this.sm = new Wtf.grid.CheckboxSelectionModel();
        this.rowNo = new Wtf.grid.RowNumberer();
        columnArr.push(this.rowNo);
        
        if (this.isIndividualVehicleDOPOReport || this.isIndividualDriverDOPOReport) {
             columnArr.push({
                header: this.isIndividualDriverDOPOReport ? WtfGlobal.getLocaleText("acc.masterConfig.26") : WtfGlobal.getLocaleText("acc.masterConfig.25"), // "Driver",
                dataIndex: "driver",
                align: 'center',
                width: 200
            });
            columnArr.push({
                header: WtfGlobal.getLocaleText("acc.field.DOPO"), // "DO/PO",
                dataIndex: "doPoName",
                renderer: WtfGlobal.deletedRenderer,
                width: 200
            },{
                header: WtfGlobal.getLocaleText("acc.field.tripNo"), // "Trip Number",
                dataIndex: "tripNo",
                renderer: WtfGlobal.deletedRenderer,
                width: 200
            },{
                header: WtfGlobal.getLocaleText("acc.field.tripDesc"), // "Trip Description",
                dataIndex: "tripDesc",
                renderer : function(val) {
                    val = val.replace(/(<([^>]+)>)/ig,"");
                    return "<div wtf:qtip=\"" + val + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.field.tripDesc") + "'>" + val + "</div>";
                },
                width: 200
            });
        }
        
        if (this.isIndividualVehicleDeliveryReport || this.isIndividualDriverDeliveryReport) {
           
           columnArr.push({
                header: this.isIndividualDriverDeliveryReport ? WtfGlobal.getLocaleText("acc.masterConfig.26") : WtfGlobal.getLocaleText("acc.masterConfig.25"), // "Driver",
                dataIndex: "driver",
                align: 'center',
                width: 200
            });
            columnArr.push({
                header: WtfGlobal.getLocaleText("acc.stockLedger.Date"), // "Date",
                dataIndex: "pushDate",
                align: 'center',
                renderer: function (value,meta,rec) {
                    if (!value) {
                        return value;
                    } else {
                        value = value.format(WtfGlobal.getOnlyDateFormat());
                        value = WtfGlobal.linkRenderer(value,meta,rec);
                        return value;
                    }
                },
                editor: this.deliveryDate,
                width: 200
            });
        
            columnArr.push({
                header: WtfGlobal.getLocaleText("acc.field.Day"), // "Day",
                dataIndex: "pushDay",
                renderer: WtfGlobal.deletedRenderer,
                width: 200
            });
        }
        
        if (this.isVehicleDeliverySummaryReport || this.isDriverDeliverySummaryReport) {
            columnArr.push({
                header: this.isVehicleDeliverySummaryReport? WtfGlobal.getLocaleText("acc.field.vehicleNo") : WtfGlobal.getLocaleText("acc.field.driver"), // "Vehicle No." : "Driver",
                dataIndex: this.isVehicleDeliverySummaryReport? "vehicleNo" : "driver",
                renderer: WtfGlobal.linkDeletedRenderer,
                width: 200
            });
        }
        
        if (this.isVehicleDeliverySummaryReport || this.isIndividualVehicleDeliveryReport || this.isDriverDeliverySummaryReport || this.isIndividualDriverDeliveryReport) {
            columnArr.push({
                header: WtfGlobal.getLocaleText("acc.field.noOfTrips"), // "No. of Trips",
                dataIndex: "noOfTrips",
                align: "right",
                width: 200
            });
        
            columnArr.push({
                header: WtfGlobal.getLocaleText("acc.field.noOfDOPO"), // "No. of DO/PO",
                dataIndex: "noOfDoPo",
                align: "right",
                width: 200
            });
        }
        
        this.grid = new Wtf.grid.GridPanel({
            store: this.VehicleDeliverySummaryStore,
//            sm: this.sm,
            autoScroll: true,
            hirarchyColNumber: 0,
            border: true,
            layout: 'fit',
            viewConfig: {
                forceFit: true
            },
            loadMask: true,
            columns: columnArr
        });
        
        this.grid.on('cellclick', this.onCellClick, this);
    },
    
    onCellClick: function(g,i,j,e) {
        e.stopEvent();
        var el = e.getTarget("a");
        if (el == null) {
            return;
        }
        
        var rec = this.grid.getStore().getAt(i);
        var header = g.getColumnModel().getDataIndex(j);
        
        if (this.isVehicleDeliverySummaryReport || this.isDriverDeliverySummaryReport) {
            rec.data.startMonth = this.startMonth.getValue();
            rec.data.startYear = this.startYear.getValue();
        }
        rec.data.moduleid = this.modules.getValue();
        
        if(header == "vehicleNo") {
            getIndividualVehicleDeliveryReportTabView(rec);
        } else if (header == "driver") {
            getIndividualDriverDeliveryReportTabView(rec);
        } else if (header == "pushDate" && this.isIndividualVehicleDeliveryReport) {
            getIndividualVehicleDOPOReportTabView(rec);
        } else if (header == "pushDate" && this.isIndividualDriverDeliveryReport) {
            getIndividualDriverDOPOReportTabView(rec);
        }
    },
    
//    getBookBeginningYear: function(isfirst) {
//        var ffyear;
//        if (isfirst) {
//            var cfYear = new Date(Wtf.account.companyAccountPref.fyfrom);
//            ffyear = new Date(Wtf.account.companyAccountPref.firstfyfrom);
//            ffyear = new Date(ffyear.getFullYear(), cfYear.getMonth(), cfYear.getDate()).clearTime();
//        } else {
//            var fyear = new Date(Wtf.account.companyAccountPref.firstfyfrom).getFullYear();
//            ffyear = new Date( fyear,this.fmonth.getValue(),this.fdays.getValue()).clearTime();
//        }
//
//        var data = [];
//        var newrec;
//        if (ffyear == null || ffyear == "NaN") {
//            ffyear = new Date(Wtf.account.companyAccountPref.fyfrom);
//        }
//        var year = ffyear.getFullYear();
//        var temp = new Date();
//        var year1 = temp.getFullYear();
//        data.push([0,year1]);
//        var i = 1;
//        while (year1 >= year) {
//            data.push([i,--year1]);
//            i++;
//        }
//        if(!(ffyear.getMonth() == 0 && ffyear.getDate() == 1)) {
//            data.push([1,year+1]);
//            newrec = new Wtf.data.Record({
//                id: 1,
//                yearid: year+1
//            });
//        }
//        return data;
//    },
    
    fetchStatement:function() {
        this.VehicleDeliverySummaryStore.load({
            params: {
                start: 0,
                limit: this.pP.combo.value
            }
        });
    }
});