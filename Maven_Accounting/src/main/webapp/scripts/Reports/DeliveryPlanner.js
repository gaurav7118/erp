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

function getDeliveryPlannerTabViewDynamicLoad(moduleid, billid) {
    if (!WtfGlobal.EnableDisable(Wtf.UPerm.dplanner, Wtf.Perm.dplanner.viewdp)) {
        if (moduleid == undefined || moduleid == null) {
            moduleid = Wtf.Acc_Invoice_ModuleId;
        }
        var panelID = "";
        var title = "";
        if (moduleid == Wtf.Acc_Purchase_Order_ModuleId) {
            panelID = 'poDeliveryplanner';
            title = "PO " + WtfGlobal.getLocaleText("acc.field.deliveryPlanner");
        } else if (moduleid == Wtf.Acc_Sales_Return_ModuleId) {
            panelID = 'salesReturnDeliveryplanner';
            title = WtfGlobal.getLocaleText("acc.accPref.autoSR") + " " + WtfGlobal.getLocaleText("acc.field.deliveryPlanner");
        } else if (billid != undefined || billid != null) {
            panelID = 'deliveryplanner' + billid;
            title = WtfGlobal.getLocaleText("acc.field.deliveryPlanner");
        } else {
            panelID = 'deliveryplanner';
            title = WtfGlobal.getLocaleText("acc.field.deliveryPlanner");
        }
        
        var deliveryPlannerPanel = Wtf.getCmp(panelID);
        if (deliveryPlannerPanel == null) {
            deliveryPlannerPanel = new Wtf.account.DeliveryPlanner({
                id: panelID,
                border: false,
                title: Wtf.util.Format.ellipsis(title, Wtf.TAB_TITLE_LENGTH),
                tabTip: title,
                moduleid: moduleid,
                billid: billid,
                layout: 'border',
                closable: true,
                iconCls: 'accountingbase deliveryorder'
            });
            Wtf.getCmp('as').add(deliveryPlannerPanel);
        }
        var channelName = "/DeliveryPlanner/gridAutoRefresh";
        deliveryPlannerPanel.on('beforeclose', function() {
            beforeClose(channelName);
        },this);
        Wtf.getCmp('as').setActiveTab(deliveryPlannerPanel);
        Wtf.getCmp('as').doLayout();
    } else {
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.up.60"));
    }
}

Wtf.account.DeliveryPlanner = function(config) {
    this.isUpdatedLocally = false;
    Wtf.apply(this, config);
    
    if (dojoInitCount <= 0) {
        dojo.cometd.init("../../bind");
        dojoInitCount++;
    }
    this.label=config.title;
    
    var quickSearchEmptyText = "";
    if (this.moduleid == Wtf.Acc_Purchase_Order_ModuleId) {
        quickSearchEmptyText = WtfGlobal.getLocaleText("acc.poDeliveryPlanner.QuickSearchEmptyText");
    } else if (this.moduleid == Wtf.Acc_Sales_Return_ModuleId) {
        quickSearchEmptyText = WtfGlobal.getLocaleText("acc.salesReturnDeliveryPlanner.QuickSearchEmptyText");
    } else {
        quickSearchEmptyText = WtfGlobal.getLocaleText("acc.deliveryPlanner.QuickSearchEmptyText");
    }
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText: quickSearchEmptyText,
        width: 300,
        id: "quickSearch" + this.id,
        field: 'referenceNumber'
    });
    
    this.resetBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.reset"),  // 'Reset',
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"),  // 'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        disabled: false
    });
    this.resetBttn.on('click', this.handleResetClick, this);
    
    this.startDate = new Wtf.ExDateFieldQtip({
        fieldLabel: WtfGlobal.getLocaleText("acc.common.from"),  // 'From',
        name: 'startdate',
        format: WtfGlobal.getOnlyDateFormat(),
        value:Wtf.serverDate
    });
    this.startDate.setValue(Wtf.serverDate);
    
    this.endDate = new Wtf.ExDateFieldQtip({
        fieldLabel: WtfGlobal.getLocaleText("acc.common.to"),  // 'To',
        name: 'enddate',
        format: WtfGlobal.getOnlyDateFormat(),
        value:Wtf.serverDate
    });
    this.endDate.setValue(Wtf.serverDate);
   
    this.fetchBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.fetch"),  // 'Fetch',
        tooltip: WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"), // "Select a time period to view corresponding transactions.",
        style: "margin-left: 6px;",
        iconCls: 'accountingbase fetch',
        scope: this,
        handler: this.fetchStatement                        
    });
    
    this.deleteTransperm=new Wtf.Action({
        text: WtfGlobal.getLocaleText("acc.rem.7"),//+' '+this.label+' '+WtfGlobal.getLocaleText("acc.field.Permanently"),
        scope: this,
        tooltip:WtfGlobal.getLocaleText("acc.rem.6")+' '+WtfGlobal.getLocaleText("acc.field.Permanently"), 
        iconCls:getButtonIconCls(Wtf.etype.deletebutton),
        hidden: config.consolidateFlag ||this.isSalesCommissionStmt,
        disabled :true,
        handler:this.handleDelete
       });
   
    this.deliveryLocation = new Wtf.form.TextField({
        name: 'deliveryLocation',
        maxLength: 255
    });
    
    this.deliveryDate = new Wtf.form.DateField({
        format: WtfGlobal.getOnlyDateFormat(),
        name: 'deliveryDate'
    });
    
    this.deliveryTime = new Wtf.form.TextField({
        name: 'deliveryTime',
        maxLength: 50
    });
    
    this.remarksByPlanner = new Wtf.form.TextField({
        name: 'remarksByPlanner',
        maxLength: 50
    });
    
    this.printedBy = new Wtf.form.TextField({
        name: 'printedBy',
        maxLength: 255
    });
    
    this.vehicleNo = new Wtf.form.ComboBox({
        name: 'name',
        hiddenName: 'vehicleNo',
        triggerAction: 'all',
        store: Wtf.vehicleStore,
        mode: 'local',
        valueField: 'id',
        displayField: 'name',
        anchor: '90%',
        typeAhead: true,
        forceSelection: true
    });
    Wtf.vehicleStore.load();
    
    this.driver = new Wtf.form.ComboBox({
        name: 'name',
        hiddenName: 'driver',
        triggerAction: 'all',
        store: Wtf.driverStore,
        mode: 'local',
        valueField: 'id',
        displayField: 'name',
        anchor: '90%',
        typeAhead: true,
        forceSelection: true
    });
    Wtf.driverStore.load();
    
    this.tripNo = new Wtf.form.ComboBox({
        name: 'name',
        hiddenName: 'tripNo',
        triggerAction: 'all',
        store: Wtf.tripStore,
        mode: 'local',
        valueField: 'id',
        displayField: 'name',
        anchor: '90%',
        typeAhead: true,
        forceSelection: true
    });
    Wtf.tripStore.load();
    
    this.tripDesc = new Wtf.form.TextField({
        name: 'tripDesc',
        maxLength: 50
    });
    
    this.createAnnouncement = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.createAnnouncement"), // "Create Announcement",
        tooltip : WtfGlobal.getLocaleText("acc.field.createAnnouncement"), // "Create Announcement",
        id: 'btncreateAnnouncement' + this.id,
        scope: this,
        iconCls : getButtonIconCls(Wtf.etype.add),
        handler: this.handleCreateAnnouncement
    });
    
    this.editAnnouncement = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.editAnnouncement"), // "Edit Announcement",
        tooltip : WtfGlobal.getLocaleText("acc.field.editAnnouncement"), // "Edit Announcement",
        id: 'btneditAnnouncement' + this.id,
        scope: this,
        iconCls : getButtonIconCls(Wtf.etype.edit),
        disabled: true,
        handler: this.handleEditAnnouncement
    });
     
    this.deliveryPlannerRec = Wtf.data.Record.create([
        {name: 'id'},
        {name: 'pushTime'},
        {name: 'referenceNumber'},
        {name: 'doNo'},
        {name: 'doId'},
        {name: 'fromUser'},
        {name: 'deliveryLocation'},
        {name: 'deliveryDate', type: 'date'},
        {name: 'deliveryTime'},
        {name: 'remarksBySales'},
        {name: 'printedBy'},
        {name: 'remarksByPlanner'},
        {name: 'vehicleNo'},
        {name: 'driver'},
        {name: 'tripNo'},
        {name: 'tripDesc'},
        {name: 'occurrenceNo'},
        {name: 'invoiceId'}
    ]);
    
    this.deliveryPlannerStore = new Wtf.data.Store({
        url: "ACCDeliveryPlanner/getDeliveryPlanner.do",
        reader: new Wtf.data.KwlJsonReader({
            totalProperty: "totalCount",
            root: "data"
        }, this.deliveryPlannerRec),
        remoteSort:true
    });
    
    var fileName = "";
    if (this.moduleid == Wtf.Acc_Purchase_Order_ModuleId) {
        fileName = "PO " + WtfGlobal.getLocaleText("acc.field.deliveryPlanner");
    } else if (this.moduleid == Wtf.Acc_Sales_Return_ModuleId) {
        fileName = WtfGlobal.getLocaleText("acc.accPref.autoSR") + " " + WtfGlobal.getLocaleText("acc.field.deliveryPlanner");
    } else {
        fileName = WtfGlobal.getLocaleText("acc.field.deliveryPlanner");
    }
     this.expButton=new Wtf.exportButton({
        obj:this,
        id: "exportReports" + this.id,
        text:WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.ra.exportTT"),  //'Export report details',
        disabled :true,
        filename: fileName + "_v1",
        baseParams:{
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                isFromDeliveryPlannerReport:true,
                isExport:true
        },
        menuItem:{
            csv:true,
            pdf:true,
            xls:true
        },
        get: Wtf.autoNum.DeliveryPlanner
    });  
    
     this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
            disabled :true,
            label:WtfGlobal.getLocaleText("acc.field.deliveryPlanner"),
            filename: fileName,
            baseParams:{
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                isFromDeliveryPlannerReport:true,
                isExport:true
           }, menuItem:{print:true},
            get:Wtf.autoNum.DeliveryPlanner
          })
    this.deliveryPlannerStore.on('beforeload', function(store) {
        var fromdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());  //ERP-10528
        var todate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        this.deliveryPlannerStore.baseParams.startdate = fromdate;
        this.deliveryPlannerStore.baseParams.enddate = todate;
        this.deliveryPlannerStore.baseParams.moduleid = this.moduleid;
        this.deliveryPlannerStore.baseParams.billid = (this.billid != undefined && this.billid != null) ? this.billid : "";
        this.expButton.baseParams.startdate = fromdate;
        this.expButton.baseParams.enddate = todate;
        this.printButton.baseParams.startdate = fromdate;
        this.printButton.baseParams.enddate = todate;
        this.deliveryPlannerStore.baseParams.isFromDeliveryPlannerReport = true;
    },this);
    
    this.deliveryPlannerStore.on('load', function(store) {
        if (this.deliveryPlannerStore.getCount() < 1) {
            if(this.expButton)this.expButton.disable();
            if(this.printButton)this.printButton.disable();
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }else{
            if(this.expButton)this.expButton.enable();
            if(this.printButton)this.printButton.enable();
        }
        this.quickPanelSearch.StorageChanged(store);
    }, this);
    
    this.deliveryPlannerStore.on('datachanged', function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    }, this);
    
    var remarksBySalesTitle = (this.moduleid == Wtf.Acc_Purchase_Order_ModuleId) ? WtfGlobal.getLocaleText("acc.field.RemarksByPurchase") : WtfGlobal.getLocaleText("acc.field.RemarksBySales");
    var referenceNumberHeader = "";
    if (this.moduleid == Wtf.Acc_Purchase_Order_ModuleId) {
        referenceNumberHeader = WtfGlobal.getLocaleText("acc.invoice.PO"); // "PO Number"
    } else if (this.moduleid == Wtf.Acc_Sales_Return_ModuleId) {
        referenceNumberHeader = WtfGlobal.getLocaleText("acc.MailWin.srmsg7"); // "Sales Return Number"
    } else {
        referenceNumberHeader = WtfGlobal.getLocaleText("acc.agedPay.gridIno"); // "Invoice Number"
    }
    
    var userTitle = "<table><tr>";
   
    userTitle += "<td><div style='height:14px;width:14px;background-color:black !important;float:left;margin-top:3px;'></div>&nbsp;";
    userTitle += "<div style='height:14px;float:left;margin-top:3px;'>" + WtfGlobal.getLocaleText("acc.deliveryplanner.1stOccurrence") +"</div></td>";
   
    userTitle += "<td><div style='height:14px;width:14px;background-color:green !important;float:left;margin-top:3px;'></div>&nbsp;";
    userTitle += "<div style='height:14px;float:left;margin-top:3px;'>" + WtfGlobal.getLocaleText("acc.deliveryplanner.2ndOccurrence") +"</div></td>";
   
    userTitle += "<td><div style='height:14px;width:14px;background-color:#FF7E00 !important;float:left;margin-top:3px;'></div>&nbsp;";
    userTitle += "<div style='height:14px;float:left;margin-top:3px;'>" + WtfGlobal.getLocaleText("acc.deliveryplanner.3rdOccurrence") +"</div></td>";
   
    userTitle += "<td><div style='height:14px;width:14px;background-color:red !important;float:left;margin-top:3px;'></div>&nbsp;";
    userTitle += "<div style='height:14px;float:left;margin-top:3px;'>" + WtfGlobal.getLocaleText("acc.deliveryplanner.4thandAboveOccurrence") +"</div></td>";
    
    userTitle += "</tr></table>";
   
    this.rowNo = new Wtf.grid.RowNumberer();
    this.smDeliveryGrid = new Wtf.grid.CheckboxSelectionModel({
            singleSelect:false
        });
   this.grid = new Wtf.grid.EditorGridPanel({
        title: userTitle,
        clicksToEdit: 1,
        store: this.deliveryPlannerStore,
        border: false,
        layout: 'fit',
        moduleId:Wtf.Delivery_Planner_ModuleId,
        loadMask: true,
        sm: this.smDeliveryGrid,
        tbar: [WtfGlobal.getLocaleText("acc.deliveryPlanner.Note")],
        viewConfig: {
            getRowClass: function(record) {
                this.occurrenceNo = record.get('occurrenceNo');
                if (this.occurrenceNo == 1) {
                    return 'occurrenceNo_1'
                } else if (this.occurrenceNo == 2) {
                    return 'occurrenceNo_2'
                } else if (this.occurrenceNo == 3) {
                    return 'occurrenceNo_3'
                } else {
                    return 'occurrenceNo_N'
                }
            }
        },
        cm: new Wtf.grid.ColumnModel([this.smDeliveryGrid,
            this.rowNo,
            {
                header: WtfGlobal.getLocaleText("acc.field.id"), // "id",
                dataIndex: "id",
                hidden: true,
                fixed: true
            },{
                header: WtfGlobal.getLocaleText("acc.field.time"), // "Time",
                dataIndex:'pushTime',
                renderer: WtfGlobal.deletedRenderer,
                width: 170,
                pdfwidth: 150,
                sortable : true
            },{
                header: referenceNumberHeader,
                dataIndex: "referenceNumber",
                renderer: WtfGlobal.linkDeletedRenderer,
                sortable : true,
                width: 100,
                pdfwidth: 150
            },{
                header: (this.moduleid == Wtf.Acc_Purchase_Order_ModuleId) ? "GRN Number" : WtfGlobal.getLocaleText("acc.MailWin.domsg7"), // "GRN Number" : "Delivery Order Number",
                dataIndex: "doNo",
                hidden: (this.moduleid == Wtf.Acc_Sales_Return_ModuleId) ? true : false,
                renderer: WtfGlobal.multipleDOLinkRenderer,
                autoSize : true,
                groupable: true,
                width: 125,
                pdfwidth: 130
            },{
                header: WtfGlobal.getLocaleText("acc.common.from"), // "From",
                dataIndex: "fromUser",
                renderer: WtfGlobal.deletedRenderer,
                sortable : true,
                width: 110,
                pdfwidth: 150
            },{
                header: WtfGlobal.getLocaleText("acc.field.deliveryLocation"), // "Delivery Location",
                dataIndex: "deliveryLocation",
                renderer : function(val) {
                    val = val.replace(/(<([^>]+)>)/ig," ");
                    val = val.replace(/[\n\r\t]/g," ");
                    return "<div wtf:qtip=\"" + val + "\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.field.deliveryLocation")+"'>" + val + "</div>";
                },
                sortable : true,
                editor: this.deliveryLocation,
                width: 120,
                pdfwidth: 150
            },{
                header: WtfGlobal.getLocaleText("acc.field.deliveryDate"), // "Delivery Date",
                dataIndex: "deliveryDate",
                editor: this.deliveryDate,
                width: 90,
                pdfwidth: 150,
                sortable : true,
                renderer: WtfGlobal.onlyDateRendererForDeliveryPlanner
            },{
                header: WtfGlobal.getLocaleText("acc.field.deliveryTime"), // "Delivery Time",
                dataIndex: "deliveryTime",
                renderer: WtfGlobal.deletedRenderer,
                editor: this.deliveryTime,
                sortable : true,
                width: 90,
                pdfwidth: 150
            },{
                header: remarksBySalesTitle, // "Remarks By Purchase" : "Remarks By Sales",
                dataIndex: "remarksBySales",
                renderer : function(val) {
                    val = val.replace(/(<([^>]+)>)/ig,"");
                    return "<div wtf:qtip=\"" + val + "\" wtf:qtitle='"+remarksBySalesTitle+"'>" + val + "</div>";
                },
                width: 110,
                pdfwidth: 150,
                sortable : true
            },{
                header: WtfGlobal.getLocaleText("acc.field.printedBy"), // "Printed By",
                dataIndex: "printedBy",
                renderer: WtfGlobal.deletedRenderer,
                editor: this.printedBy,
                width: 100,
                pdfwidth: 130
            },{
                header: WtfGlobal.getLocaleText("acc.field.remarksByPlanner"), // "Remarks By Planner",
                dataIndex: "remarksByPlanner",
                renderer : function(val) {
                    val = val.replace(/(<([^>]+)>)/ig,"");
                    return "<div wtf:qtip=\"" + val + "\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.field.remarksByPlanner")+"'>" + val + "</div>";
                },
                editor: this.remarksByPlanner,
                width: 125,
                pdfwidth: 150,
                sortable : true
            },{
                header: WtfGlobal.getLocaleText("acc.field.vehicleNo"), // "Vehicle No.",
                dataIndex: "vehicleNo",
                renderer: Wtf.comboBoxRenderer(this.vehicleNo),
                editor: this.vehicleNo,
                sortable : true,
                width: 90,
                pdfwidth: 150
            },{
                header: WtfGlobal.getLocaleText("acc.field.driver"), // "Driver",
                dataIndex: "driver",
                renderer: Wtf.comboBoxRenderer(this.driver),
                editor: this.driver,
                sortable : true,
                width: 90,
                pdfwidth: 150
            },{
                header: WtfGlobal.getLocaleText("acc.field.tripNo"), // "Trip Number",
                dataIndex: "tripNo",
                renderer: Wtf.comboBoxRenderer(this.tripNo),
                editor: this.tripNo,
                sortable : true,
                width: 80,
                pdfwidth: 150
            },{
                header: WtfGlobal.getLocaleText("acc.field.tripDesc"), // "Trip Description",
                dataIndex: "tripDesc",
                renderer : function(val) {
                    val = val.replace(/(<([^>]+)>)/ig,"");
                    return "<div wtf:qtip=\"" + val + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.field.tripDesc") + "'>" + val + "</div>";
                },
                editor: this.tripDesc,
                sortable : true,
                width: 90,
                pdfwidth:100
            },{
                header: WtfGlobal.getLocaleText("acc.field.id"), // "id",
                dataIndex: "invoiceId",
                hidden: true,
                fixed: true
            }])
    });
    
    this.grid.on('cellclick',this.onCellClick, this);
    this.deliveryPlannerAnnouncementRec = Wtf.data.Record.create([
        {name: 'announcementID'},
        {name: 'announcementTime', type: 'date'},
        {name: 'announcementMsg'}
    ]);
    
    this.deliveryPlannerAnnouncementStore = new Wtf.data.Store({
        url: "ACCDeliveryPlanner/getDliveryPlannerAnnouncement.do",
        reader: new Wtf.data.KwlJsonReader({
            totalProperty: "totalCount",
            root: "data"
        }, this.deliveryPlannerAnnouncementRec)
    });
    
    this.deliveryPlannerAnnouncementStore.on('beforeload',function() {
        var currentBaseParams = this.deliveryPlannerAnnouncementStore.baseParams;
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        this.deliveryPlannerAnnouncementStore.baseParams = currentBaseParams;
    },this);
    
    this.deliveryPlannerAnnouncementStore.on('load', function(store) {
        if (this.deliveryPlannerAnnouncementStore.getCount() < 1) {
            this.deliveryPlannerAnnouncementGrid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.deliveryPlannerAnnouncementGrid.getView().refresh();
        }
    }, this);
    
    this.deliveryPlannerAnnouncementStore.load({
        params: {
            start: 0,
            limit: 30
        }
    });
    
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    
    this.deliveryPlannerAnnouncementGrid = new Wtf.grid.GridPanel({
        store: this.deliveryPlannerAnnouncementStore,
        border: false,
        layout: 'fit',
        loadMask: true,
        sm: this.sm,
        moduleId: Wtf.Delivery_Planner_Announcement_ModuleId,
        tbar: [this.createAnnouncement, this.editAnnouncement],
        columns: [this.sm, this.rowNo,
            {
                header: WtfGlobal.getLocaleText("acc.field.id"), // "id",
                dataIndex: "id",
                hidden: true,
                fixed: true
            },{
                header: WtfGlobal.getLocaleText("acc.stockLedger.Date"), // "Date",
                dataIndex: 'announcementTime',
                align: 'center',
                renderer: WtfGlobal.onlyDateRenderer,
                width: 150
            },{
                header: WtfGlobal.getLocaleText("acc.MAilWin.message"), // "Message",
                dataIndex: 'announcementMsg',
                renderer : function(val) {
                    val = val.replace(/(<([^>]+)>)/ig,"");
                    val=val.split("\n").join("<br>");
                    return "<div wtf:qtip=\"" + val + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.MAilWin.message") + "'>" + val + "</div>";
                },
                width: 900
            }],
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "announcementpagingtoolbar" + this.id,
            store: this.deliveryPlannerAnnouncementStore,
//            searchField: this.quickPanelSearch,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
            plugins: this.pP2 = new Wtf.common.pPageSize({
                id : "announcementpPageSize_" + this.id
            })
        })
    });
    
    this.getMyConfig(this.grid);
    this.getMyConfig(this.deliveryPlannerAnnouncementGrid);
    this.grid.on('render', function() {
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.grid.on('statesave', this.saveMyStateHandler, this);
        }, this);
    }, this);
    this.deliveryPlannerAnnouncementGrid.on('render', function() {
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.deliveryPlannerAnnouncementGrid.on('statesave', this.saveMyStateHandler, this);
        }, this);
    }, this);

    this.sm.on("selectionchange", this.enableDisableButtons.createDelegate(this), this);
    this.smDeliveryGrid.on("selectionchange", this.enableDisableButtons.createDelegate(this), this);
    
    this.grid.on('afteredit', this.saveRecord, this);
    
    Wtf.account.DeliveryPlanner.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.DeliveryPlanner, Wtf.Panel, {
    onRender: function(config) {
        if (Wtf.account.companyAccountPref.deliveryPlanner) {
            dojo.cometd.subscribe("/DeliveryPlanner/gridAutoRefresh", this, "globalDeliveryPlannerGridAutoRefreshPublishHandler");    
        }
        
        this.deliveryPlannerStore.load({
            params: {
                start: 0,
                limit: 30,
                isFromDeliveryPlannerReport:true
            }
        });
    
        this.centerPanel = new Wtf.Panel({
            region : 'center',
            layout: 'fit',
            border: false,
            items: [this.grid],
            tbar: [this.quickPanelSearch, this.resetBttn, '-', WtfGlobal.getLocaleText("acc.common.from"), this.startDate, WtfGlobal.getLocaleText("acc.common.to"), this.endDate, this.fetchBttn,!WtfGlobal.EnableDisable(Wtf.UPerm.dplanner, Wtf.Perm.dplanner.deletedp)?this.deleteTransperm:"",'-',this.expButton,'-',this.printButton],
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.deliveryPlannerStore,
                searchField: this.quickPanelSearch,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({
                    id : "pPageSize_" + this.id
                })           
            })
        });
        
        this.southPanel = new Wtf.Panel({
            border: true,
            region: 'south',
            layout: 'fit',
            height: 250,
            plugins : new Wtf.ux.collapsedPanelTitlePlugin(),
            collapsibletitle : WtfGlobal.getLocaleText("acc.field.announcement"), // "Announcement",
            title : WtfGlobal.getLocaleText("acc.field.announcement"), // "Announcement",
            collapsible: true,
            collapsed: false,
            items : [this.deliveryPlannerAnnouncementGrid]
        });
        
        this.add(this.centerPanel, this.southPanel);
        
        Wtf.account.DeliveryPlanner.superclass.onRender.call(this, config);
    },
    
    createAnnouncementWin: function(isEdit, rec) {
        this.announcementWin = new Wtf.Window({
            height: 280,
            width: 475,
            maxLength: 1000,
            title: isEdit? WtfGlobal.getLocaleText("acc.field.editAnnouncement") : WtfGlobal.getLocaleText("acc.field.createAnnouncement"), // "Edit Announcement" : "Create Announcement",
            bodyStyle: 'padding:10px; background-color:#f1f1f1;',
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            autoScroll: true,
            allowBlank: false,
            layout: 'border',
            items: [{
                region: 'north',
                border: false,
                height: 70,
                bodyStyle: 'background-color:#ffffff;border-bottom:1px solid #bfbfbf;',
                html: getTopHtml(isEdit? WtfGlobal.getLocaleText("acc.field.editAnnouncement") : WtfGlobal.getLocaleText("acc.field.createAnnouncement"), "", "../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
            }, {
                region: 'center',
                border: false,
                layout: 'form',
                id: 'announcementForm',
                bodyStyle: 'padding:5px;',
                labelWidth: 150,
                items: [this.messageField = new Wtf.form.TextArea({
                    fieldLabel: WtfGlobal.getLocaleText("acc.MAilWin.message"), // "Message",
                    name: "announcementMsg",
                    width: 240,
                    height: 100,
                    allowBlank: false,
                    maxLength: 1024
                }), this.announcementFromDate = new Wtf.form.DateField({
                    fieldLabel: WtfGlobal.getLocaleText("acc.reval.frmDate"), // "From Date",
                    format: WtfGlobal.getOnlyDateFormat(),
                    name: 'announcementTime',
                    width: 240,
                    allowBlank: false
                })]
            }],
            modal: true,
            buttons: [{
                text: WtfGlobal.getLocaleText("acc.common.saveBtn"), // "Save",
                scope: this,
                handler: function() {
                    if (this.messageField.getValue().trim() == "") {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleaseentermessage")], 2); // "Please enter message."
                        return;
                    }

                    if (!this.messageField.isValid()) {
                        this.messageField.markInvalid(WtfGlobal.getLocaleText("acc.field.Maximumlengthofthisfieldis1024"));
                        return;
                    }
                
                    if (this.announcementFromDate.getValue() == "") {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.PleaseSeletFromDate")], 2); // "Please select From Date."
                        return;
                    }

                    if (!this.announcementFromDate.isValid()) {
                        return;
                    }
                                
                    Wtf.Ajax.requestEx({
                        url: "ACCDeliveryPlanner/saveDliveryPlannerAnnouncement.do",
                        params: {
                            announcementID: isEdit? rec.data.announcementID : "",
                            announcementTime: WtfGlobal.convertToGenericDate(this.announcementFromDate.getValue()),
                            announcementMsg: this.messageField.getValue()
                        }
                    }, this, function (response) {
                        if (response.success) {
                            this.announcementWin.close();
                            this.deliveryPlannerAnnouncementStore.reload();
                            
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.announcement"), WtfGlobal.getLocaleText("acc.field.announcementSavedSuccessfully")], response.success*2+1);
                        } else {
                            var msg = WtfGlobal.getLocaleText("acc.common.msg1"); // "Failed to make connection with Web Server";
                            if (response.msg) {
                                msg = response.msg;
                            }
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                        }
                    }, function (response) {
                        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); // "Failed to make connection with Web Server";
                        if(response.msg) {
                            msg = response.msg;
                        }
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                    });
                }
            }, {
                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                scope: this,
                handler: function() {
                    this.announcementWin.close();
                }
            }]
        });
        this.announcementWin.show();
    },
    
    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.fetchStatement();
        }
    },
    
    fetchStatement: function() {
        this.sDate = this.startDate.getValue();
        this.eDate = this.endDate.getValue();
        
        if (this.sDate > this.eDate) {
            WtfComMsgBox(1, 2);
            return;
        }
        
        var fromdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());  //ERP-10528
        var todate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        
        this.deliveryPlannerStore.load({
            params: {
                startdate: fromdate,
                enddate: todate,
                start: 0,
                isFromDeliveryPlannerReport:true,
                limit: this.pP.combo.value
            }
        });
        
        this.deliveryPlannerAnnouncementStore.load({
            params: {
                startdate: fromdate,
                enddate: todate,
                start: 0,
                limit: this.pP2.combo.value
            }
        });
    },
     handleDelete:function(del){
         this.recArr = this.grid.getSelectionModel().getSelections(); 
         this.grid.getSelectionModel().clearSelections();
         WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
         Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.DeliveryPlannerDeleteWarning"),function(btn){
           if(btn!="yes") {
                var data=[];
                for(var i=0;i<this.recArr.length;i++){
                    var ind=this.deliveryPlannerStore.indexOf(this.recArr[i])
                    var num= ind%2;
                    WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
                }
                return;
            }
            var idData = "";
            for(var i=0;i<this.recArr.length;i++){
                var rec = this.recArr[i];
                idData += "{\"billid\":\""+rec.get('id')+"\"},";
            }
        if(idData.length>1){
            idData=idData.substring(0,idData.length-1);
        }
        data="["+idData+"]";
            Wtf.Ajax.requestEx({
                url: "ACCDeliveryPlanner/deleteDeliveryPlannerPermanently.do",
                params: {
                    billid:data
                }
            }, this,this.genSuccessResponse,this.genFailureResponse);
        },this);
  
         
     },
      genSuccessResponse:function(response){
         WtfGlobal.resetAjaxTimeOut();
         var superThis = this;
         WtfComMsgBox([this.label,response.msg],response.success*2+1,"","",function(btn){
             if(btn=="ok"){
                for(var i=0;i<superThis.recArr.length;i++){
                    var ind=superThis.deliveryPlannerStore.indexOf(superThis.recArr[i])
                    var num= ind%2;
                    WtfGlobal.highLightRowColor(superThis.grid,superThis.recArr[i],false,num,2,true);
                }
                if(response.success){
                (function(){
                this.deliveryPlannerStore.reload();
                }).defer(WtfGlobal.gridReloadDelay(),superThis);
                }
             }
         });
    },
    genFailureResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
         for(var i=0;i<this.recArr.length;i++){
             var ind=this.deliveryPlannerStore.indexOf(this.recArr[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    saveRecord: function(obj) {
        if (obj != undefined && obj != null) {
            this.isUpdatedLocally = true;
            var rec = obj.record;
            if (obj.field == "vehicleNo") {
                var vehicleRecIndex = WtfGlobal.searchRecordIndex(Wtf.vehicleStore, obj.record.get('vehicleNo'), 'id');
                if (vehicleRecIndex >= 0) {
                    var vehicleRec = Wtf.vehicleStore.getAt(vehicleRecIndex);
                    rec.set("driver", vehicleRec.data.driverID);
                }
            }
            
            var params = {
                deliveryPlannerID: obj.record.data.id,
                moduleid: this.moduleid,
                column_Name: obj.field,
                column_Value: (obj.field == "deliveryDate")? WtfGlobal.convertToGenericDate(obj.value) : obj.value
            }
            
            if (obj.field == "vehicleNo") {
                params.mappedDriver = rec.data.driver;
            }
        
            Wtf.Ajax.requestEx({
                url: "ACCDeliveryPlanner/savePushToDeliveryPlanner.do",
                params: params
            }, this, function (response) {
                if (response.success) {
                    obj.record.commit();
                } else {
                    var msg = WtfGlobal.getLocaleText("acc.common.msg1"); // "Failed to make connection with Web Server";
                    if(response.msg) {
                        msg = response.msg;
                    }
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                }
            
            }, function (response) {
                var msg = WtfGlobal.getLocaleText("acc.common.msg1"); // "Failed to make connection with Web Server";
                if(response.msg) {
                    msg = response.msg;
                }
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
            });
        }
    },
    
    onCellClick: function(g,i,j,e) {
        e.stopEvent();
        var el = e.getTarget("a");
        if (el == null) {
            return;
        } else {
            var formrec;
            var type;
            var withoutinventory;
            var dataindex = g.getColumnModel().getDataIndex(j);
            
            if (dataindex == "referenceNumber") {
                formrec = g.getStore().getAt(i);
                withoutinventory = false;
                var incoiceid = formrec.data['invoiceId'];
                if (this.moduleid == Wtf.Acc_Purchase_Order_ModuleId) {
                    type = 'Purchase Order';
                } else if (this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid== Wtf.Acc_Delivery_Order_ModuleId) {
                    type = 'Customer Invoice';
                } else if (this.moduleid == Wtf.Acc_Sales_Return_ModuleId) {
                    type = 'Sales Return';
                }
                viewTransactionTemplate1(type, formrec, withoutinventory, incoiceid);
            } else if (dataindex == "doNo") {
                var doId = g.getStore().getAt(i).data['doId'];
                if (e.target.getAttribute('doId') != undefined && e.target.getAttribute('doId') != "") { // multiple links in single row
                    doId = e.target.getAttribute('doId');
                }
                formrec = g.getStore().getAt(i);
                withoutinventory = false;
                if (this.moduleid == Wtf.Acc_Purchase_Order_ModuleId) {
                    type = 'Goods Receipt Order';
                } else if (this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid== Wtf.Acc_Delivery_Order_ModuleId) {
                    type = 'Delivery Order';
                }
                viewTransactionTemplate1(type, formrec, withoutinventory, doId);
            }
        }
    },
    
    enableDisableButtons: function() {
        var arr = this.sm.getSelections();
        var arr1 = this.smDeliveryGrid.getSelections();
        
        if (arr.length == 1) {
            this.editAnnouncement.enable();
        } else {
            this.editAnnouncement.disable();
        }
        if(arr1.length>0){
            this.deleteTransperm.enable();
        }else{
            this.deleteTransperm.disable();
        }
    },
    
    handleCreateAnnouncement: function() {
        this.createAnnouncementWin();
    },
    
    handleEditAnnouncement: function() {
        var rec = this.sm.getSelected();
        this.createAnnouncementWin(true, rec);
        this.messageField.setValue(rec.data.announcementMsg);
        this.announcementFromDate.setValue(rec.data.announcementTime);
    },
    
    globalDeliveryPlannerGridAutoRefreshPublishHandler: function(response) {
        var res = eval("("+response.data+")");
        
        if (res.success && !this.isUpdatedLocally) {
            this.deliveryPlannerStore.load({
                params: {
                    start: 0,
                    limit: this.pP.combo.value
                }
            });
            
            Wtf.notify.msg("", WtfGlobal.getLocaleText("acc.field.DeliveryPlannerHasBeenUpdatedSuccessfully"));
        }
        this.isUpdatedLocally = false;
    },
    getMyConfig:function (grid){
        WtfGlobal.getGridConfig (grid, grid.moduleId, false, false);
        
        var statusForCrossLinkage = grid.getColumnModel().findColumnIndex("statusforcrosslinkage");
        if (statusForCrossLinkage != -1) {
            grid.getColumnModel().setHidden(statusForCrossLinkage, true);
        }
    },
    saveMyStateHandler: function (grid,state){
        WtfGlobal.saveGridStateHandler(this, grid, state, grid.moduleId, grid.gridConfigId, false);
    }
});