/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.define('ReportBuilder.view.ReportList', {
    extend: 'Ext.panel.Panel',
    xtype: 'reportlist',
    layout: 'fit',
    requires: [
        'ReportBuilder.model.CommonModel',
        'ReportBuilder.view.CreateNewReport',
        'ReportBuilder.view.Report',
        'ReportBuilder.view.PivotReport',
        'ReportBuilder.view.CreateNewReportWin',
        'ReportBuilder.overrides.grid.plugin.RowExpander',
        'ReportBuilder.overrides.util.Collection',
        'ReportBuilder.overrides.pivot.matrix.Base',
        'ReportBuilder.extension.CustomListFilter',
        'ReportBuilder.store.ReportListStore',
        'ReportBuilder.extension.grid.feature.Summary'
    ],
    
    initComponent: function() {
        var me = this;
        me.createReportListGrid();
        me.createPrivilegedUsersGrid();
        
        Ext.apply(me, {
            layout: "border",
            items: [{
                    region: 'center',
                    xtype: 'panel',
                    layout: "fit",
                    items: [this.reportListGrid]
                },
                {title: 'Privileged Users',
                    region: 'south',
                    xtype: 'panel',
                    height: 250,
                    hidden : !(Ext.userReportRole.URole.roleid == Ext.ADMIN_ROLE_ID),
                    collapsible : true,
                    collapsed : true,
                    layout: "fit",
                    items: [this.PrivelegeGrid]}]
        });
        
        this.callParent(arguments);
    },
    
    createReportListGrid: function() {
        var accreportlistStore = Ext.create('ReportBuilder.store.ReportListStore');
        accreportlistStore.load({
            params: {
                start: 0,
                limit: 25
            }
        });
        var reportListBtnArr = [];
        var newReportButton = new Ext.Button({
            text: ExtGlobal.getLocaleText("acc.common.createNewReport"),
            scope: this,
            iconCls: "pwnd create-new",
            tooltip: ExtGlobal.getLocaleText("acc.common.createNewReport"),
            handler: function() {
                var mainTabPanel = Ext.getCmp('mainTabPanel')
                this.createNewWindow = Ext.create('ReportBuilder.view.CreateNewReportWin', {});
                this.createNewWindow.show();                 
            }
        });
        
        this.editReportBtn = new Ext.Button({
            text: ExtGlobal.getLocaleText("acc.common.report.edit"),
            iconCls: "accountingbase editbtn",
            tooltip: ExtGlobal.getLocaleText("acc.common.report.editTT"),
            disabled: true,
            scope: this,
           handler: function () {
                var selectedReports = this.reportListGrid.getSelectionModel().getSelected().items;
               if (selectedReports[0].data.createdby === loginid || Ext.userReportRole.URole.roleid == Ext.ADMIN_ROLE_ID) {
                this.editReportBtnHandler();
                  }else{
                      Ext.CustomMsg('Alert', ExtGlobal.getLocaleText('acc.customreports.editpermission.msg'), Ext.Msg.INFO);
                  }
            }
        });
        this.CopyReportBtn = new Ext.Button({
            text: ExtGlobal.getLocaleText("acc.CustomReport.copyReport"),
            iconCls: "accountingbase copybtn",
            tooltip: ExtGlobal.getLocaleText("acc.CustomReport.copyReportTT"),
            disabled: true,
            scope: this,
           handler: function () {
                     var selectedReports = this.reportListGrid.getSelectionModel().getSelected().items;
                     var selectedReportData = selectedReports[0].data;
                     selectedReportData.createdby
                    this.createNewWindow = Ext.create('ReportBuilder.view.CreateNewReportWin', {
                        isCopy: true,
                        reportId:selectedReportData.id,
                        isEdit:false
                });
                this.createNewWindow.show();
            }
        });
        this.deleteReportBtn = new Ext.Button({
            text: ExtGlobal.getLocaleText("acc.common.deleteReports"),
            iconCls: "accountingbase delete",
            tooltip: ExtGlobal.getLocaleText("acc.CustomReport.deleteToolTip"),
            disabled: true,
            scope: this,
            handler: function() {
                 var selectedReports = this.reportListGrid.getSelectionModel().getSelected().items;
                  if (selectedReports[0].data.createdby === loginid || Ext.userReportRole.URole.roleid == Ext.ADMIN_ROLE_ID) {
                    var msgKey = this.reportListGrid.getSelectionModel().getSelected().length > 1 ? "acc.reportbuilder.deletemultiplereportmsg" : "acc.reportbuilder.deletereportmsg";
                    Ext.MessageBox.confirm(ExtGlobal.getLocaleText('acc.common.confirm'), ExtGlobal.getLocaleText(msgKey) + "</br></br><b>" + ExtGlobal.getLocaleText('acc.customerList.delTT1') + "</b>", function (btn) {
                        if (btn == "yes") {
                        this.deleteReports();
                    }
                }, this);
                  }else{
                      Ext.CustomMsg('Alert', ExtGlobal.getLocaleText('acc.customreports.deletepermission.msg'), Ext.Msg.INFO);
                  }}
        });
        
         reportListBtnArr.push(newReportButton,"-",this.editReportBtn,"-",this.CopyReportBtn,"-",this.deleteReportBtn,"-");
        
        reportListBtnArr.push({
            text: ExtGlobal.getLocaleText("acc.common.clearFilters"),
            tooltip: ExtGlobal.getLocaleText("acc.common.clearFilterToolTip"),
            iconCls: "pwnd remove-filter",
            handler: 'onClearFilters'
        });
       
        this.privilegeBtn = new Ext.Button({
            text: ExtGlobal.getLocaleText("acc.field.SetPrivileges"),
            tooltip: ExtGlobal.getLocaleText("acc.field.Setreportprivileges"),
           
            disabled: true,
            scope: this,
            handler: this.createPrivileges
        });
        //adding admin privilege to privilege button
        if(Ext.userReportRole.URole.roleid == Ext.ADMIN_ROLE_ID){
             reportListBtnArr.push(this.privilegeBtn);
        }
      

        var cellEditing = new Ext.grid.plugin.CellEditing({
            clicksToEdit: 1
        });
        
        this.reportListGrid = Ext.create('Ext.grid.Panel', {
            id: 'idreportlistgrid',
            layout: 'fit',
            tbar: reportListBtnArr,
            border :false,
            //            frame: true,
            selModel: {
                selType: 'checkboxmodel', // XTYPE
                mode: 'SINGLE',
                checkOnly: 'true',
                toggleOnClick: true,
                allowDeselect: true
            },
            store: accreportlistStore,
            defaultListenerScope: true,
            plugins: [cellEditing, 'gridfilters'],
            viewConfig: {
                emptyText: "<div style='text-align:center;font-size:16px;'>"+ExtGlobal.getLocaleText("acc.common.norecordstoDisplay") +" </div>",
                deferEmptyText: false
            },
            columns: [
            {
                text:  ExtGlobal.getLocaleText("acc.common.ReportName"),
                dataIndex: 'name',
                flex: 1,
                sortable: true,
                editor: {
                    allowBlank: false,
                    validateBlank: true,
                    vtype: 'reportnamevtype',
                    maxLength: 50
                },
                renderer: function(val) {
                    return "<a href = '#' class='newReport'>" + val + "</a>";
                },
                filter:"string"
            },
            {
                text: ExtGlobal.getLocaleText("acc.common.ReportDescription"),
                dataIndex: 'description',
                flex: 2,
                sortable: true,
                editor: {
                    maxLength: 255
                },
                renderer: function(value, metaData, record) {
                    metaData.tdAttr = 'data-qtip="' + value + '"';
                    return value;
                },
                filter:"string"
            },
            {
                text:  ExtGlobal.getLocaleText("acc.common.moduleName"),
                dataIndex: 'modules.modulename',
                flex: 1,
                sortable: true,
                filter:"string"
            },
            {
                text: ExtGlobal.getLocaleText("acc.common.moduleCategory"),
                dataIndex: 'modulecategory.modulecatname',
                flex: 1,
                sortable: true,
                filter:"string"
            },
            {
                text: ExtGlobal.getLocaleText("acc.common.reportcreatedon"),
                dataIndex: 'reportmaster.createdon',
                flex: 1,
                sortable: true,
                hidden : true,
                    filter:{type: "date",dateFormat:'Y-m-d'}
            },
            {
                text: ExtGlobal.getLocaleText("acc.common.reportupdatedon"),
                dataIndex: 'reportmaster.updatedon',
                flex: 1,
                sortable: true,
                hidden : true,
                    filter:{type: "date",dateFormat:'Y-m-d'}
            }],
            dockedItems: [{
                xtype: 'pagingtoolbar',
                store: accreportlistStore,
                dock: 'bottom',
                displayInfo: true,
                animateShadow: true,
                plugins: [new Ext.ux.grid.PageSize(), new Ext.ux.ProgressBarPager()]
            }],
            onClearFilters: function() {
                this.filters.clearFilters();
            }
        });
        accreportlistStore.on("load",this.setCompanyExtraPreferences,this);
        this.reportListGrid.on("cellclick", this.afterGridCellClick, this);
        this.reportListGrid.on("selectionchange", this.enableDisableButtons, this);
        this.reportListGrid.on("select", this.loadPrivilegeGrid, this);
        
        this.reportListGrid.on('edit', function(editor, e) {
            if (e.originalValue != e.value) {
                Ext.Ajax.request({
                    url: 'ACCCreateCustomReport/updateCustomReportNameAndDescription.do',
                    method:"POST",
                    params: {
                        reportNo: e.record.data.id,
                        reportNewDesc: e.record.data.description,
                        reportNewName: e.record.data.name,
                        isreportNameFieldEdited: e.field == "name" ? true : false
                    },
                    success: function(res, req) {
                        var resObj = eval("(" + res.responseText + ")");
                        if (resObj.success) {
                            Ext.CustomMsg('Success', resObj.msg, Ext.Msg.INFO);
                            e.record.data.updatedon = resObj.updatedon;
                            e.record.commit();
                        } else {
                            e.record.reject();
                            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), resObj.msg, Ext.Msg.INFO);
                        }
                    },
                    failure: function() {
                        Ext.CustomMsg('Error',ExtGlobal.getLocaleText("acc.common.errorOccuratServerSide"), Ext.Msg.ERROR);
                    }
                });
            }
        });
    },
   
    editReportBtnHandler:function() {
        
//        if (event.getTarget("a[class='newReport']")) {
        var selectedReports = this.reportListGrid.getSelectionModel().getSelected().items;
        var selectedReportData = selectedReports[0].data;
        var record = selectedReports[0];
        var mainTabPanel = Ext.getCmp('mainTabPanel');
        var newTab;
        if (mainTabPanel.getChildByElement("editreport" + selectedReportData.name.replace(/\s/g, '_') + selectedReportData.id)) {
            mainTabPanel.setActiveTab("editreport" + selectedReportData.name.replace(/\s/g, '_') + selectedReportData.id);
        } else if (mainTabPanel.getChildByElement("idnewcustomreporttab" + selectedReportData.name.replace(/\s/g, '_') + selectedReportData.id)) {
            mainTabPanel.setActiveTab("idnewcustomreporttab" + selectedReportData.name.replace(/\s/g, '_') + selectedReportData.id);
        } else {
            newTab = Ext.create('ReportBuilder.view.CreateNewReport', {
                id: "editreport" + selectedReportData.name.replace(/\s/g, '_')+selectedReportData.id,
                title: selectedReportData.name,
                tooltip: ExtGlobal.getLocaleText("acc.common.report.edit") + " - " + selectedReportData.name,
                closable: true,
                isEditFlag: true,
                isPivot: selectedReportData.isPivot == "T" ? true : false,
                reportNo: selectedReportData.id,
                reportName: selectedReportData.name,
                reportDescription: selectedReportData.description,
                moduleCategory: selectedReportData["modulecategory.modulecatname"],
                moduleId: selectedReportData.moduleId,
                moduleName: selectedReportData["modules.modulename"],
                reportjson: selectedReportData.reportjson,
                showGLFlag : JSON.parse(selectedReportData.reportjson).showGLFlag,
                showExpenseTypeTransactionsFlag : JSON.parse(selectedReportData.reportjson).showExpenseTypeTransactionsFlag,
                filterJson: selectedReportData.filterJson,
                recordsToDrop: [],
                iconCls: "accountingbase menu-edit"
            });
            
            mainTabPanel.add(newTab).show();
        }
    },
    
    setCompanyExtraPreferences: function () {
        var rawData = this.reportListGrid.getStore().getProxy().getReader().rawData;
        if (rawData) {
            showPivot = rawData.showPivotInCustomReports;
            countryid = rawData.countryid;
            financialYearFromDate = rawData.userPref.fromdate;
            financialYearToDate = rawData.userPref.todate;
            Ext.UserReportRole = rawData.role;
        }
        /**
         * Open report to view data if opened from Statutory Panel
         */
        var openReportId = sessionStorage.getItem("openReportId");
        if (openReportId != null && openReportId != undefined && openReportId != '') {
            var record = this.reportListGrid.getStore().findRecord('id', openReportId);
            if (record) {
                var params = {
                    isEWayReport: record.data.isEWayReport == "T" ? true : false
                };
                createNewReportTab("report" + record.data.name.replace(/\s/g, '_') + record.data.id, record.data.name, record.data.id, record.data.moduleId, true, record.data.isPivot, record.data.isDefault, undefined, undefined, record.data["modulecategory.modulecatname"], record.data.reportUrl, record.data.parentreportid, undefined, record, params);
            }
            sessionStorage.removeItem("openReportId");
        }
    },
    createPrivilegedUsersGrid : function (){
        
        this.PrivelegeStore=Ext.create('Ext.data.Store', {
            model: 'ReportBuilder.model.CommonModel',
            timeout : 180000,
            pageSize: 5,
            proxy: {
                type: 'ajax',
                url: 'ACCReports/getReportPerm.do',
                actionMethods : getStoreActionMethods(),
                reader: {
                    type: 'json',
                    rootProperty: "data['data']",
                    totalProperty: "count"
                }
            }
        });
        
//        this.PrivelegeStore.load();
        this.PrivelegeGrid = Ext.create('Ext.grid.Panel', {
            id: 'idreportgrid',
            layout: 'fit',
            border :false,
            selModel: Ext.create('Ext.selection.CheckboxModel', {
                mode: 'SINGLE',
                checkOnly: 'true',
                allowDeselect: true
            }),
            store:this.PrivelegeStore,
            defaultListenerScope: true,
            plugins: ['gridfilters'],
             viewConfig: {
                emptyText: "<div style='text-align:center;font-size:16px;'>"+ExtGlobal.getLocaleText("acc.common.norecordstoDisplay") +" </div>",
                deferEmptyText: false
            },
            columns: [
            {
                text:  ExtGlobal.getLocaleText("acc.field.GroupName"),
                dataIndex: 'GroupName',
                flex: 1,
                sortable: true
            },{
                text:  ExtGlobal.getLocaleText("acc.auditTrail.gridUser"),
                dataIndex: 'UseFirst',
                flex: 1,
                sortable: true
            },
            {
                text: ExtGlobal.getLocaleText("acc.field.ReportName"),
                dataIndex: 'ReportName',
                flex: 1,
                sortable: true
            },{
                text: ExtGlobal.getLocaleText("acc.product.gridAction"),
                dataIndex: 'delete',
                flex: 1,
                sortable: true,
                renderer: function(val, mdata, rec, ri, ci) {
                return "<div class='pwnd delete-gridrow'></div>"//"<img id='delete' class='deleteR'  style='height:18px; width:18px;' src='images/cancel_16.png' title='Action '></img>";
            }
            }],
        dockedItems: [{
                xtype: 'pagingtoolbar',
                store: this.PrivelegeStore,
                pageSize: 5,
                dock: 'bottom',
                displayInfo: true,
                animateShadow: true,
                plugins: [new Ext.ux.grid.PageSize(), new Ext.ux.ProgressBarPager()]
            }]
        });
        this.PrivelegeGrid.on("cellclick",this.callDeletePerm,this);
    },
    callDeletePerm :function(view, cell, cellIndex, record, row, rowIndex, event) {
        var selectedReports = this.PrivelegeGrid.getSelectionModel().getSelection();
        if (selectedReports.length > 0) {
            var selectedReport = selectedReports[0];
        }
        if (event.getTarget("div[class='pwnd delete-gridrow']")) {
//        if(e.target.className == 'pwnd delete-gridrow') {
            Ext.Ajax.request({
                method: 'POST',
                url: 'ACCReports/DeleteUserPerm.do',
                scope: this,
                params: {
                    type: 'savereportrolemap',
                    mode: 'delete',
                    reportid: selectedReport.data.ReportID,
                    userid: selectedReport.data.userID,
                    roleid: selectedReport.data.RoleID
                },
                success: function() {
                    this.PrivelegeGrid.getStore().reload();
                }
            });
        }
    },
    createPrivileges: function () {
        
        this.groupStore=Ext.create('Ext.data.Store', {
            model: 'ReportBuilder.model.CommonModel',
            timeout : 180000,
            proxy: {
                type: 'ajax',
                url: 'PermissionHandler/getRoleList.do',
                actionMethods : getStoreActionMethods(),
                reader: {
                    type: 'json',
                    rootProperty: "data['data']",
                    totalProperty: "count"
                }
            }
        });
        
        this.groupStore.load();
        this.groupStore.on('load',function(){
                        var index =this.groupStore.find('roleid','1');
                        if(index != -1){
                            var storerec=this.groupStore.getAt(index);                        
                            this.groupStore.remove(storerec);
                        }
                    } ,this);

        this.groupCombo = new Ext.form.ComboBox({
            fieldLabel: ExtGlobal.getLocaleText("acc.field.Rolename"), // change of name from Group to Roll
            store: this.groupStore,
            queryMode: 'local',
            triggerAction: 'all',
            editable: false,
            emptyText: ExtGlobal.getLocaleText("acc.field.SelectRole..."),
            allowBlank: false,
            width: 200,
            valueField: 'roleid',
            displayField: 'rolename'
        });
        
        this.groupCombo.on('select', this.onGroupComboSelect, this);
        this.roleStore=Ext.create('Ext.data.Store', {
            model: 'ReportBuilder.model.CommonModel',
            timeout : 180000,
            scope: this,
            proxy: {
                type: 'ajax',
                url: 'ACCReports/getUserForCombo.do',
                actionMethods : getStoreActionMethods(),
                reader: {
                    type: 'json',
                    rootProperty: "data[\"data\"]",
                    totalProperty: "count"
                }
            }
        });
        this.roleStore.on("beforeload",function(){
            this.roleStore.proxy.extraParams.groupid=  this.groupCombo.getValue();
        },this);
        
         this.roleCombo = new Ext.form.ComboBox({
            fieldLabel: ExtGlobal.getLocaleText("acc.auditTrail.gridUser"),
            store: this.roleStore,
            forceSelection : true,
            queryMode: 'local',
            triggerAction: 'all',
            editable: false,
            emptyText: ExtGlobal.getLocaleText("acc.field.SelectUser..."),
//            allowBlank: false,
            width: 200,
            valueField: 'userID',
            displayField: 'UserName'
        });
        this.roleStore.on("load", function (combo) {      //for selecting all users while setting privelles
            var record = new ReportBuilder.model.CommonModel({
                userID: "All",
                UserName: "All"
            });
            this.roleStore.insert(0, record);
            this.roleCombo.setValue("All");
        }, this);
        this.roleStore.load();
        this.roleCombo.on('select', function (combo, roleRec, index) {
            if (this.roleCombo.getValue() == 'All') {
                this.roleCombo.clearValue();
                this.roleCombo.setValue("All");
            } else if (this.roleCombo.getValue().indexOf('All') >= 0) {  // case of all after record
                this.roleCombo.clearValue();
                this.roleCombo.setValue(roleRec.get('userID'));
            }
        }, this);
        
        this.privWin = new Ext.Window({
            title: ExtGlobal.getLocaleText("acc.field.Setreportprivileges"),
            resizable: false,
            width: 467,
            height: 250,
            modal: true,
            layout: 'border',
            scope: this,
            buttons: [{
                    text: ExtGlobal.getLocaleText("acc.msgbox.ok"),
                    scope: this,
                    handler: function () {
                        this.savePrivileges();
                    }
                }, {
                    text: ExtGlobal.getLocaleText("acc.msgbox.cancel"),
                    scope: this,
                    handler: function () {
                        this.privWin.close();
                    }
                }],
            items: [{
                    region: 'north',
                    height: 75,
                    border: false,
                    baseCls: 'northWinClass',
           //         html: getTopHtml(ExtGlobal.getLocaleText("acc.field.AssignReportPermissions"), ExtGlobal.getLocaleText("acc.field.Assign") + this.sm.getSelected().data.name + ExtGlobal.getLocaleText("acc.field.Permissionforuser") + "<b></b>", "../../images/createuser.png")
                    html: getTopHtml(ExtGlobal.getLocaleText("acc.field.AssignReportPermissions"), ExtGlobal.getLocaleText("acc.field.Assign") + ExtGlobal.getLocaleText("acc.field.Permissionforuser") + "<b></b>", "../../images/createuser.png")
                }, {
                    region: 'center',
                    layout: 'form',
                    border: false,
                    labelWidth: 70,
                    bodyStyle: 'background:#f1f1f1;padding:15px',
                    items: [
                        this.groupCombo,
                        this.roleCombo
                    ]
                }]
        });
        
        this.privWin.show();

    },
    loadPrivilegeGrid : function( selModel , record , index , eOpts ){
        var store = this.PrivelegeGrid.getStore();
//        if(Wtf.UserReporRole.URole.roleid == 1){
            if(record) {
                store.proxy.extraParams.reportid = record.data.id;
                
                store.load();
            } else {
                store.removeAll();
            }
//        }
    },
    
    afterGridCellClick: function(grid, td, cellIndex, record, tr, rowIndex, e, eOpts) {
        var mainTabPanel = Ext.getCmp('mainTabPanel');
        var event = e;
        if (event.getTarget("a[class='newReport']")) {
            if (mainTabPanel.getChildByElement("report" + record.data.name.replace(/\s/g, '_')+record.data.id)) {
                mainTabPanel.setActiveTab("report" + record.data.name.replace(/\s/g, '_')+record.data.id);
            } else {
                var params = {
                    isEWayReport: record.data.isEWayReport == "T" ? true : false
                };
                createNewReportTab("report" + record.data.name.replace(/\s/g, '_') + record.data.id, record.data.name, record.data.id, record.data.moduleId, true, record.data.isPivot, record.data.isDefault, undefined, undefined, record.data["modulecategory.modulecatname"], record.data.reportUrl, record.data.parentreportid, undefined, record,params);
         }
        }
    },
    enableDisableButtons: function (grid, selected, eOpts) {
        if (selected.length > 0) {
            this.deleteReportBtn.enable();
        } else {
            this.deleteReportBtn.disable();
        }

        if (selected.length === 1) {
            this.privilegeBtn.enable();
            this.editReportBtn.enable();
            this.CopyReportBtn.enable();
        } else {
            this.privilegeBtn.disable();
            this.editReportBtn.disable();
            this.CopyReportBtn.disable();
        }
    },
    onGroupComboSelect: function () {
        var selectedReports = this.reportListGrid.getSelectionModel().getSelection();
        if (selectedReports.length > 0) {
            reportId = selectedReports[0].get("id");
        }
        this.roleStore.load({
            params: {
                reportid : reportId,
                groupid: this.groupCombo.getValue()
            }
        });
    },
    savePrivileges: function () {
        var reportId = "";

        var selectedReports = this.reportListGrid.getSelectionModel().getSelection();
        if (selectedReports.length > 0) {
            reportId = selectedReports[0].get("id");
        }
        if (this.groupCombo.isValid() && this.roleCombo.isValid()) {
            Ext.Ajax.request({
                method: 'POST',
                url: 'ACCReports/AssignUserPerm.do',
                scope: this,
                params: {
                    type: 'savereportrolemap',
                    mode: 'insert',
                    reportid: reportId,
                    roleid: this.groupCombo.getValue(),
                    groupid: this.groupCombo.getValue(),
                    userid: this.roleCombo.getValue(),
                    isCustomReport: true
                },
                success: function () {
                    this.privWin.close();
                    this.PrivelegeStore.reload();
                }
            });
        }
    },
    deleteReports: function() {
        var count = 0;
        var reportIds = "";
        var reportListGrid = this.reportListGrid;
        var selectedReports = reportListGrid.getSelectionModel().getSelected().items;
        for (count = 0; count < selectedReports.length; count++) {
            reportIds += selectedReports[count].data.id + ",";
        }
        reportIds = reportIds.substring(0, reportIds.length - 1);
            Ext.Ajax.request({
                url: 'ACCCreateCustomReport/deleteCustomReport.do',
            method:"POST",
                params: {
                    reportIds: reportIds
                },
            success: function(res, req) {
                    var resObj = eval("(" + res.responseText + ")");
                    if (resObj.success == true) {
                        Ext.CustomMsg('Success', resObj.msg, Ext.Msg.INFO);
                        var reportCount = reportListGrid.getStore().count();
                        var currentPage = reportListGrid.getStore().currentPage;
                    if((reportCount > selectedReports.length  && currentPage > 0)||(currentPage == 1)){
                            reportListGrid.getStore().loadPage(currentPage);
                    }else if(currentPage > 1){
                        reportListGrid.getStore().loadPage(currentPage-1);
                        }
                    } else {
                        Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), resObj.msg, Ext.Msg.INFO);
                    }
                },
            failure: function() {
                    Ext.CustomMsg('Error', ExtGlobal.getLocaleText("acc.common.errorOccuratServerSide"), Ext.Msg.ERROR);
                }
            });
    }    
});