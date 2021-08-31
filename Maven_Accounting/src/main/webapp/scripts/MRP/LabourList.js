/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.account.labourList = function(config) {
    this.arr = [];
    this.UserpermissionCodes=Wtf.UPerm.labourmaster;
    this.SystemPermissionCodes=Wtf.Perm.labourmaster;
    Wtf.apply(this, config);
    /*
     * Create Tool Bar Buttons
     */
    this.createTBar();
    /*
     * Create Grid 
     */
    this.createGrid();
    /*
     * Add Advance Search
     */
    this.addAdvanceSearchComponent();
    this.addEvents({
        'labourupdate': true
    });

    this.on('labourupdate', function() {
        this.fetchStatement();
    }, this);
    Wtf.account.labourList.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.labourList, Wtf.Panel, {
    onRender: function(config) {
        /*
         * create panel to show grid
         */
        this.createPanel();
        this.add(this.leadpan);
        /*
         * fetch data in report
         */
        this.fetchStatement();
        Wtf.account.labourList.superclass.onRender.call(this, config);
    },
    createPanel: function() {
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [this.objsearchComponent,
                {
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
                            id: "pPageSize_" + this.id
                        }),
                        items: this.bbarBtnArr
                    })
                }]
        });
    },
    createTBar: function() {
        this.btnArr = [];
        this.bbarBtnArr = [];
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.labourList.quickSearch"), 
            width: 200,
            id: "quickSearch" + this.id,
            field: 'empid'
        });
        this.btnArr.push(this.quickPanelSearch);
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
        this.btnArr.push('-', this.fetchBttn);
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.btnArr.push('-', this.resetBttn);
        this.resetBttn.on('click', this.handleResetClickNew, this);
        this.addLabour = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.labour.addLabour"),
            id: "addLabour",
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.labour.addLabour"),
            iconCls: getButtonIconCls(Wtf.etype.menuadd),
            handler: this.showForm.createDelegate(this, [false])
        });
        this.editLabour = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.labour.editLabour"),
            id: 'editLabour',
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.labour.editLabour"),
            disabled: true,
            iconCls: getButtonIconCls(Wtf.etype.menuedit),
            handler: this.showForm.createDelegate(this, [true])
        });
        this.deleteLabour = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.labour.deleteLabour"),
            id: 'deleteLabour',
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.labour.deleteLabour"),
            disabled: true,
            iconCls: getButtonIconCls(Wtf.etype.menudelete),
            handler: this.handleDelete.createDelegate(this, [true, false])
        });
        this.deleteLabourPerm = new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.common.delete") + " " + WtfGlobal.getLocaleText("acc.field.labour") + " " + WtfGlobal.getLocaleText("acc.common.deletePermanently"),
                id: "deleteMachinePerm" + this.id,
                scope: this,
                tooltip: WtfGlobal.getLocaleText("acc.common.delete") + " " + WtfGlobal.getLocaleText("acc.field.labour") + " " + WtfGlobal.getLocaleText("acc.common.deletePermanently"),
                disabled: true,
                iconCls: getButtonIconCls(Wtf.etype.menudelete),
                handler: this.handleDelete.createDelegate(this, [false, true])
            });
        this.personnelArr = [];
        if (!WtfGlobal.EnableDisable(this.UserpermissionCodes, this.SystemPermissionCodes.createlbor)) {
            this.personnelArr.push(this.addLabour);
        }
        if (!WtfGlobal.EnableDisable(this.UserpermissionCodes, this.SystemPermissionCodes.modifylbor)) {
            this.personnelArr.push(this.editLabour);
        }
        if (!WtfGlobal.EnableDisable(this.UserpermissionCodes, this.SystemPermissionCodes.deletelbor)) {
            this.personnelArr.push(this.deleteLabour);
            this.personnelArr.push(this.deleteLabourPerm);
        }
        this.manageLabours = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.labour.PersonnelAction"),
            tooltip: WtfGlobal.getLocaleText("acc.labour.PersonnelAction"),
            iconCls: 'labouricon',
            menu: this.personnelArr
        });
        
        if (this.personnelArr.length > 0) {
            this.btnArr.push(this.manageLabours);
        }
        
        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), // "Advanced Search",
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), // 'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });
        this.btnArr.push('-', this.AdvanceSearchBtn);
        this.customReportViewBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            handler: this.customizeView,
            iconCls: 'accountingbase fetch'
        });
        this.btnArr.push('->', this.customReportViewBtn);
        this.exportButton = new Wtf.exportButton({
            obj: this,
            id: "exportReports" + this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), // 'Export report details',
            filename: WtfGlobal.getLocaleText("acc.field.labourList") + "_v1",
            scope: this,
            menuItem: {
                csv: true,
                pdf: true,
                xls: true
            },
            get: Wtf.autoNum.LabourList
        });
        if (!WtfGlobal.EnableDisable(this.UserpermissionCodes, this.SystemPermissionCodes.exportlbor)) {
            this.bbarBtnArr.push('-', this.exportButton);
        }
        this.exportButton.on("click", function() {
            this.exportButton.setParams({
                type: this.transactionType.getValue(),
                productId: this.productId,
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            });
        }, this);
        this.bbarBtnArr.push('-', this.importBtn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.common.import"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.common.import"),
            iconCls: (Wtf.isChrome ? 'pwnd importChrome' : 'pwnd import')
//            menu: importBtnArray
        }));
        this.resourceConflictBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.labourProfile.resorceConflict"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.labourProfile.resorceConflict"),
            handler: this.handleResolveConflict,
            iconCls: 'accountingbase fetch'
        });
//        this.bbarBtnArr.push('-', this.resourceConflictBtn);
//        this.resourcecostBtn = new Wtf.Toolbar.Button({
//            text: WtfGlobal.getLocaleText("acc.labourProfile.resourcecost"),
//            scope: this,
//            tooltip: WtfGlobal.getLocaleText("acc.labourProfile.resourcecost"),
//            handler: this.handleResorceCost,
//            iconCls: 'accountingbase fetch',
//           // hidden:true
//        });
       // this.bbarBtnArr.push('-', this.resourcecostBtn);
        this.assigntaskBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.labourProfile.assigntask"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.labourProfile.assigntask"),
            handler: this.handleTaskAssign,
            iconCls: 'accountingbase fetch'
        });
        this.syncLabour = [];
        this.syncLabour.push(new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.productList.dataSync"), //'Data Sync',
            iconCls: getButtonIconCls(Wtf.etype.sync),
            hidden:true,
            menu: [{
                    text: WtfGlobal.getLocaleText("acc.labour.Datasyncftopm"),
                    scope: this,
                    tooltip: WtfGlobal.getLocaleText("acc.labour.DatasyncftopmTT"),
                    iconCls: getButtonIconCls(Wtf.etype.syncmenuItem),
                    handler: function() {
                        this.syncLabourTOPM();
                    }
                }]
        }));
        this.bbarBtnArr.push('-', this.syncLabour);
        
        this.exportLabourUsagesBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("mrp.labour.export.allocation"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("mrp.labour.export.allocation"),
            handler: this.exportLabourUsages,
            iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export')
        });
        this.bbarBtnArr.push(this.exportLabourUsagesBtn);
    },
    createGrid: function() {
        this.Store = new Wtf.data.Store({
            url: "ACCLabourCMN/getLaboursMerge.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
        this.Store.on('beforeload',this.addFilteringParametersBeforeLaodingLabourStore , this);
        this.sm = new Wtf.grid.CheckboxSelectionModel({
        });
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

        this.Store.on('load', this.handleStoreOnLoad, this);
        this.sm.on('selectionchange', this.onSelectionEvent, this);
    },
      
    addFilteringParametersBeforeLaodingLabourStore: function (s, o) {
        if (!o.params)
            o.params = {};
        o.params.wcid = (this.wcid !== undefined || this.wcid !== '') ? this.wcid : '';
        o.params.labourids = (this.labourids !== undefined || this.labourids !== '') ? this.labourids : '';
        this.exportButton.enable();

        this.exportButton.setParams({
            wcid: (this.wcid !== undefined || this.wcid !== '') ? this.wcid : '',
            labourids: (this.labourids !== undefined || this.labourids !== '') ? this.labourids : ''
        });

    },
    setWorkCenterID: function (objParams) {
        this.wcid = objParams.wcid;
    },
    setLabouridsIDs: function (objParams) {
        this.labourids = objParams.labourids;
    },
    addAdvanceSearchComponent: function() {
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.grid.colModel,
            moduleid: Wtf.labourMaster,
            advSearch: false,
            parentPanelSearch: this,
            ignoreDefaultFields: false,
            isAvoidRedundent: true
        });

        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
        this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    },
    fetchStatement: function() {
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value,
                ss: this.quickPanelSearch.getValue()
            }
        });
    },
    handleStoreOnLoad: function (store) {
        var columns = [];
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));
        columns.push(this.sm);
        Wtf.each(this.Store.reader.jsonData.columns, function (column) {
           
                column.renderer = WtfGlobal.deletedRenderer;
           
            columns.push(column);
        });
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();

        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        this.quickPanelSearch.StorageChanged(store);
    },
    handleResetClickNew: function()
    {
        this.quickPanelSearch.reset();
        this.Store.load({
            params: {
                start: 0,
                limit: 30
            }
        })
    },
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
        this.Store.baseParams = {
            flag: 1,
            iscustomcolumndata: 0,
            searchJson: this.searchJson,
            moduleid: Wtf.labourMaster,
            isFixedAsset: false,
            isLeaseFixedAsset: false,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.Store.load({
            params: {
                ss: this.quickPanelSearch.getValue(),
                start: 0,
                limit: this.pP.combo.value
            }
        });
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.baseParams = {
            flag: 1,
            iscustomcolumndata: 0,
            searchJson: this.searchJson,
            moduleid: Wtf.labourMaster,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.Store.load({
            params: {
                ss: this.quickPanelSearch.getValue(),
                start: 0,
                limit: this.pP.combo.value
            }
        });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },
    showForm: function(isEdit) {
        var rec = null;
        if (isEdit) {
            /*
             * Open Edit case 
             */
            this.recArr = this.grid.getSelectionModel().getSelections();
            if (this.grid.getSelectionModel().hasSelection() == false || this.grid.getSelectionModel().getCount() > 1) {
                WtfComMsgBox(127, 2);
                return;
            }
            rec = this.recArr[0];
            callLabourInformation(isEdit, rec);
        } else {
            /*
             * call Create new labour = Open tab labour master form 
             */
            callLabourInformation(isEdit, rec);
        }

    },
    handleDelete: function(isTemp,isPerm) {
        if (this.grid.getSelectionModel().hasSelection()) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"), WtfGlobal.getLocaleText("acc.mrp.labour.confirmDelete"), function(btn) {
                if (btn == "yes") {
                    var arr = [];
                    this.recArr = this.grid.getSelectionModel().getSelections();
                    for (i = 0; i < this.recArr.length; i++) {
                        arr.push(this.Store.indexOf(this.recArr[i]));
                    }
                    var data = WtfGlobal.getJSONArray(this.grid, true, arr);
                    Wtf.Ajax.requestEx({
                        url: "ACCLabourCMN/deleteLabours.do",
                        params: {
                            data: data,
                            isPerm:isPerm
                        }
                    }, this, this.genSuccessResponse, this.genFailureResponse);
                } else {
                    return;
                }
            }, this);
        } else {
            WtfComMsgBox(127, 2);
            return;
        }

    },
    genSuccessResponse: function(response, request) {
        if (response.success) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.success"),
                msg: response.msg,
                width: 450,
                scope: {
                    scopeObj: this
                },
                buttons: Wtf.MessageBox.OK,
                animEl: 'mb9',
                icon: Wtf.MessageBox.INFO
            });
            this.Store.reload();
        } else {
            this.showFailureMsg(response);
        }
    },
    genFailureResponse: function(response) {
        this.showFailureMsg(response);
    },
    showFailureMsg: function(response) {
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    onSelectionEvent: function() {
        var arr = this.grid.getSelectionModel().getSelections();
        if (arr.length == 1) {
                this.editLabour.enable();
                this.deleteLabour.enable();
                this.deleteLabourPerm.enable();
        } else if (arr.length > 1) {
            this.editLabour.disable();
            this.deleteLabour.enable();
            this.deleteLabourPerm.enable();
        } 
        /* If record is Temporary Deleted then Temporary Delete button and Edit  button are disable */
        for(var i=0;i<arr.length;i++){
            if(arr[i]&&arr[i].data.deleted){
                if(this.deleteLabour){
                    this.deleteLabour.disable();
                }
                if(this.editLabour){
                    this.editLabour.disable();
                }
                break;
            }
        }
        if(arr.length == 0){
            this.editLabour.disable();
            this.deleteLabour.disable();
            this.deleteLabourPerm.disable();
                       
        }
    },
    customizeView: function() {
        this.customizeViewWin = new Wtf.CustomizeReportView({
            title: WtfGlobal.getLocaleText("acc.field.CreateCustomizeView"),
            parentPanel: this,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            grid: this.grid,
            reportId: Wtf.labourMaster,
            modules: "" + Wtf.labourMaster
        });
        this.customizeViewWin.show();
    },
//    handleResorceCost: function() {
//        /*
//         * call to Open Resource Cost tab
//         */
//        var isMachineCost = false;
//        callResourceCost(isMachineCost);
//    },
    handleTaskAssign: function() {
        /*
         * call to Open Resource Analysis
         */
        callAssignTaskList();
    },
    handleResolveConflict: function() {
        /*
         * call to open resolve conflict Tab
         */
        callResolveConflictLabourMachineDynamicLoad();
    },
    syncLabourTOPM: function() {
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"),
            msg: WtfGlobal.getLocaleText("acc.labour.AreyousureyouwanttosyncLabour"), 
            buttons: Wtf.MessageBox.OKCANCEL,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.QUESTION,
            scope: this,
            fn: function(btn) {
                if (btn == "ok") {
                    Wtf.Ajax.requestEx({
                        url: "ACCLabourCMN/syncLabourToPM.do"
                    }, this, this.genSuccessResponseForSyncToPM, this.genFailureResponseForSyncToPM);
                }
            }
        });
    },
    exportLabourUsages: function() {
        var arr = this.grid.getSelectionModel().getSelections();
        if (arr.length > 0) {
            var resourceIds = [];
            for(var i=0; i<arr.length ; i++){
                resourceIds.push(arr[i].get("billid"));
            }
            resourceUsagesExportWindow(resourceIds, "Labour");
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("mrp.labour.export.allocationselect")], 2);
        }
    },
    genSuccessResponseForSyncToPM: function(response) {
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.info"),
            msg: response.msg,
            buttons: Wtf.MessageBox.OK,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.INFO,
            scope: this,
            fn: function(btn) {
                if (btn == "ok") {
                    this.grid.store.reload();
                }
            }
        });
    },
    genFailureResponseForSyncToPM: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    }
});