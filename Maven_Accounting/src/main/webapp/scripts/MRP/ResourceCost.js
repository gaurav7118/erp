/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.account.resourceCost = function(config) {
    /*
    isMachineCost = true (Machine Cost)
    isMachineCost = false (Labour Cost)
     */
    this.isMachineCost = config.isMachineCost ? config.isMachineCost : false;
    Wtf.apply(this, config);
    /*
     * Create Tool Bar Buttons
     */
    this.createTBar();
    /*
     * Create both Resouce and  cost Grid 
     */
    this.createResorceGrid();
    this.createResorceCostGrid();
    Wtf.account.resourceCost.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.resourceCost, Wtf.Panel, {
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
        Wtf.account.resourceCost.superclass.onRender.call(this, config);
    },
    createPanel: function() {
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [
                this.centerPanel = new Wtf.Panel({
                    region: 'center',
                    layout: 'fit',
                    width: '49%',
                    border: true,
                    items: this.resourceCostGrid,
                    tbar: this.rbtnArr
                }),
                this.westPanel = new Wtf.Panel({
                    region: 'west',
                    layout: 'fit',
                    width: '49%',
                    border: false,
                    split: true,
                    items: this.resourceGrid,
                    tbar: this.lbtnArr,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.ResourceStore,
                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({
                            id: "pPageSize_" + this.id
                        })
                    })
                })]
        });
    },
    createTBar: function() {
        this.lbtnArr = [];
        this.rbtnArr = [];
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: this.isMachineCost ? WtfGlobal.getLocaleText("acc.machineList.quickSearch") : WtfGlobal.getLocaleText("acc.labourList.quickSearch"),
            width: 200,
            hidden: false,
            field: 'empname'
        });
        this.lbtnArr.push(this.quickPanelSearch);
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"),
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
        this.lbtnArr.push('-', this.fetchBttn);
        this.syncLabour = [];
        this.syncLabour.push(new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.productList.dataSync"), //'Data Sync',
            iconCls: getButtonIconCls(Wtf.etype.sync),
            tooltip: WtfGlobal.getLocaleText("acc.labour.DataCostsyncftopmTT"),
            menu: [{
                    text: this.isMachineCost ? WtfGlobal.getLocaleText("acc.machine.DataCostsyncftopmSelected") : WtfGlobal.getLocaleText("acc.labour.DataCostsyncftopmSelected"),
                    scope: this,
                    iconCls: getButtonIconCls(Wtf.etype.syncmenuItem),
                    handler: function() { 
                    if (this.resourceGrid.getSelections().length > 0) {
                        this.syncCostToPM(false);
                    }else{
                        if(this.isMachineCost){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.machine.sync.alert.msg")]);
                        }else{
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.127")]);
                        }
                    }
                }
                    
                },
                {
                    text: this.isMachineCost ? WtfGlobal.getLocaleText("acc.machine.DataCostsyncftopmAll") : WtfGlobal.getLocaleText("acc.labour.DataCostsyncftopmAll"),
                    scope: this,
                    iconCls: getButtonIconCls(Wtf.etype.syncmenuItem),
                    handler: function() {
                        this.syncCostToPM(true);
                    }
                    
                }
            ]
        }));
        this.lbtnArr.push('-', this.syncLabour);
        this.addBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.labour.resourcecost.add"),
            tooltip: WtfGlobal.getLocaleText("acc.labour.resourcecost.add"),
            iconCls: getButtonIconCls(Wtf.etype.add),
            scope: this,
            disabled:true,
            handler: this.handleAddEditBtn.createDelegate(this, [false])
        });
        this.rbtnArr.push('-', this.addBttn);
        this.editBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.edit"),
            tooltip: WtfGlobal.getLocaleText("acc.common.edit"),
            iconCls: getButtonIconCls(Wtf.etype.edit),
            scope: this,
            disabled:true,
            handler: this.handleAddEditBtn.createDelegate(this, [true])
        });
        this.rbtnArr.push('-', this.editBttn);
        this.deleteBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),
            tooltip: WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),
            iconCls: getButtonIconCls(Wtf.etype.deletebutton),
            scope: this,
            disabled:true,
            handler: this.handleDelete
        });
        this.rbtnArr.push('-', this.deleteBttn);
    },
    createResorceGrid: function() {
        this.ResourceStore = new Wtf.data.Store({
            url: this.isMachineCost ? "ACCMachineMaster/getMachineList.do" :"ACCLabourCMN/getResourceList.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
        this.sm = new Wtf.grid.RowSelectionModel({
            singleSelect: true,
            hidden:true
        });

        this.resourceGrid = new Wtf.grid.GridPanel({
            layout: 'fit',
            region: "center",
            store: this.ResourceStore,
            columns: [],
            border: false,
            loadMask: true,
            sm: this.sm,
            viewConfig: {
                forceFit: false,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        this.sm.on("selectionchange", this.onResourceChange.createDelegate(this), this);
        this.ResourceStore.on('load', this.handleResourceStoreOnLoad, this);
    },
    createResorceCostGrid: function() {
        this.ResourceCostStore = new Wtf.data.Store({
            url: this.isMachineCost ? "ACCMachineMaster/getMachineCostList.do" : "ACCLabourCMN/getResourceCostList.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
        this.RCSm = new Wtf.grid.RowSelectionModel({
            singleSelect:true
        });
        this.resourceCostGrid = new Wtf.grid.GridPanel({
            layout: 'fit',
            region: "center",
            store: this.ResourceCostStore,
            columns: [],
            sm: this.RCSm,
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: false,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        this.RCSm.on('selectionchange',this.enableDisableButtonsForRCGrid,this);
        this.ResourceCostStore.on('load', this.handleResourceCostStoreOnLoad, this);
    },
    fetchStatement: function() {
        this.ResourceStore.load({
            params: {
                ss: this.quickPanelSearch.getValue(),
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value,
                isMachineCost: this.isMachineCost
            }
        });
    },
    handleResourceStoreOnLoad: function(store) {
        var columns = [];
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));
        columns.push(this.sm);
        Wtf.each(this.ResourceStore.reader.jsonData.columns, function(column) {
            if (column.renderer) {
                column.renderer = eval('(' + column.renderer + ')');
            }
            columns.push(column);
        });
        this.resourceGrid.getColumnModel().setConfig(columns);
        this.resourceGrid.getView().refresh();

        if (this.ResourceStore.getCount() < 1) {
            this.resourceGrid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.resourceGrid.getView().refresh();
        }
        this.quickPanelSearch.StorageChanged(store);
    },
    handleResourceCostStoreOnLoad: function(store) {
        var columns = [];
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));
        Wtf.each(this.ResourceCostStore.reader.jsonData.columns, function(column) {
            if (column.dataIndex == "resourcecost") {
                column.renderer = WtfGlobal.withoutRateCurrencyDeletedSymbol;
                column.header  = WtfGlobal.getLocaleText({key:"acc.labour.resourcecost.costHour", params:[WtfGlobal.getCurrencySymbol()]})
            }

            if (column.renderer) {
                column.renderer = eval('(' + column.renderer + ')');
            }
            columns.push(column);
        });
        this.resourceCostGrid.getColumnModel().setConfig(columns);
        this.resourceCostGrid.getView().refresh();

        if (this.ResourceCostStore.getCount() < 1) {
            this.resourceCostGrid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.resourceCostGrid.getView().refresh();
        }
    },
    onResourceChange: function () {
        var arr = this.resourceGrid.getSelectionModel().getSelected();
        if (this.resourceGrid.getSelections().length > 0) {
            this.addBttn.enable();
        } else {
            this.addBttn.disable();
        }
        this.LabourId = '';
        if (arr != undefined) {
            this.LabourId = arr.data.billid;
            this.FilterResourceCOstStoreBylaoburID(this.LabourId);
        } else {
            this.FilterResourceCOstStoreBylaoburID(this.LabourId);
        }
    },
    FilterResourceCOstStoreBylaoburID: function (LabourId) {
        this.ResourceCostStore.load({
            params: {
                LabourId: LabourId
            }
        });
    },
    syncCostToPM: function(isSyncAllToPM) {
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"),
            msg: WtfGlobal.getLocaleText("acc.labour.AreyousureyouwanttosyncLabour"),
            buttons: Wtf.MessageBox.OKCANCEL,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.QUESTION,
            scope: this,
            fn: function(btn) {
                if (btn == "ok") {
                    if(isSyncAllToPM){
                        this.LabourId = '';
                    }
                    Wtf.Ajax.requestEx({
                        url: this.isMachineCost ? "ACCMachineMaster/syncMachineCostToPM.do" : "ACCLabourCMN/syncLabourCostToPM.do",
                        params: {
                            LabourId: this.LabourId
                        }
                    }, this, this.genSuccessResponseForSyncToPM, this.genFailureResponseForSyncToPM);
                }
            }
        });
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
//                    this.grid.store.reload();
                }
            }
        });
    },
    genFailureResponseForSyncToPM: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    handleAddEditBtn: function(isEdit) {
        /*
         * Create Windor Fields
         */
        this.createWindowField();
        /*
         * Create Window Button
         */
        this.createWinBtn();
        /*
         * Create Window and add form to it
         */
        this.createFormAndWindow(isEdit);
            var rec = this.resourceGrid.getSelectionModel().getSelected();
        if (rec != undefined) {
            this.billid = rec.data.billid;
        }

        if (isEdit) {
            this.setWinFields();

        }else{
            this.resourceCostId="";
        }
    },
    handleDelete: function() {
        if (this.resourceCostGrid.getSelectionModel().hasSelection()) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"), WtfGlobal.getLocaleText("acc.mrp.labour.ResourceCost.confirmDelete"), function(btn) {
                if (btn == "yes") {
                    var arr = [];
                    this.recArr = this.resourceCostGrid.getSelectionModel().getSelections();
                    for (var i = 0; i < this.recArr.length; i++) {
                        arr.push(this.ResourceCostStore.indexOf(this.recArr[i]));
                    }
                    var data = WtfGlobal.getJSONArray(this.resourceCostGrid, true, arr);
                    Wtf.Ajax.requestEx({
                        url: this.isMachineCost ? "ACCMachineMaster/deleteMachineCost.do" : "ACCLabourCMN/deleteLaboursCost.do",
                        params: {
                            data: data
                        }
                    }, this, this.genSuccessResponse, this.genFailureResponse);
                } else {
                    return;
                }
            }, this);
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
            this.ResourceCostStore.reload();
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
    setWinFields: function() {
        var rec = this.resourceCostGrid.getSelectionModel().getSelected();
        this.resourceform.getForm().loadRecord(rec);
        this.resourceCostId = rec.data.resourcecostid
    },
    createWindowField: function() {
        var fieldlabel = "<span wtf:qtip='" +WtfGlobal.getLocaleText({key:"acc.labour.resourcecost.costHour", params:[WtfGlobal.getCurrencySymbol()]}) + "'>" + WtfGlobal.getLocaleText({key:"acc.labour.resourcecost.costHour", params:[WtfGlobal.getCurrencySymbol()]}) + " *"+ "</span>";
        if(this.isMachineCost){
            fieldlabel = "<span wtf:qtip='" +WtfGlobal.getLocaleText({key:"acc.machine.resourcecost.costHour", params:[WtfGlobal.getCurrencySymbol()]}) + "'>" + WtfGlobal.getLocaleText({key:"acc.machine.resourcecost.costHour", params:[WtfGlobal.getCurrencySymbol()]}) + " *"+ "</span>";
        }
        this.resourceCostConfig = {
            fieldLabel: fieldlabel,
            name: 'resourcecost',
            hiddenName: 'resourcecost',
            id: "resourcecost" + this.id,
            allowBlank: false,
            scope: this,
            width: 150,
            disabled: false,
            maxLength: 50
        };
        this.resourceCost = new Wtf.form.NumberField(Wtf.applyIf({
        }, this.resourceCostConfig));
        this.effectiveDateConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labour.resourcecost.effectivedate") + "'>" + WtfGlobal.getLocaleText("acc.labour.resourcecost.effectivedate") + " *" + "</span>",
            name: 'effectivedate',
            hiddenName: 'effectivedate',
            id: "effectivedate" + this.id,
            allowBlank: false,
            format: WtfGlobal.getOnlyDateFormat(),
            width: 150,
            scope: this
        };
        this.effectiveDate = new Wtf.form.DateField(Wtf.applyIf({
        }, this.effectiveDateConfig));
    },
    createWinBtn: function() {

        this.saveCostBtn = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.labour.resourcecost.saveCost"),
            scope: this,
            handler: this.saveCostForm
        });
        this.cancleBtn = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            scope: this,
            handler: function() {
                this.resourceWin.close();
            }
        });
        this.WbtnArr = [];
        this.WbtnArr.push(this.saveCostBtn);
        this.WbtnArr.push(this.cancleBtn);
    },
    createFormAndWindow: function(isEdit) {
        this.resourceform = new Wtf.form.FormPanel({
            url: this.isMachineCost ? 'ACCMachineMaster/saveMachineCost.do' : 'ACCLabourCMN/saveResourceCost.do',
            labelWidth:150,
            region: 'center',
            bodyStyle: "background: transparent;",
            border: false,
            style: "background: transparent;padding:10px;",
            items: [this.resourceCost, this.effectiveDate]
        });
        this.resourceWin = new Wtf.Window({
            title: (isEdit?WtfGlobal.getLocaleText("acc.common.edit"):WtfGlobal.getLocaleText("acc.labour.resourcecost.add"))+" "+WtfGlobal.getLocaleText("acc.labour.resourcecost.cost"),
            closable: true,
            modal: true,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            width: 350,
            height: 170,
            autoScroll: true,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body,
            items: [{
                    region: 'center',
                    border: false,
                    bodyStyle: 'background:#f1f1f1;font-size:10px;',
                    autoScroll: true,
                    items: this.resourceform
                }],
            buttons: [this.saveCostBtn, this.cancleBtn]
        });
        this.resourceWin.show();
    },
    saveCostForm: function() {
        this.saveCostBtn.disable();
        this.resourceform.getForm().submit({
            scope: this,
            params: {
                labourId: this.billid,
                effectivedate: WtfGlobal.convertToGenericDate(this.effectiveDate.getValue()),
                resourceCostId: this.resourceCostId
            },
            success: function(result, action) {
                this.saveCostBtn.enable();
                var resultObj = eval('(' + action.response.responseText + ')');
                if (resultObj.data.success) {
                    this.ResourceCostStore.reload();
                    this.resourceCostId="";
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), resultObj.data.msg], 0);
                    this.resourceWin.close();
                } else {
                    if (resultObj.data.msg)
                        var msg = resultObj.data.msg;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                }
            },
            failure: function(frm, action) {
                this.saveCostBtn.enable();
                var resObj = eval("(" + action.response.responseText + ")");
                if (resObj.data.msg)
                    var msg = resObj.data.msg;
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
            }
        });
    },
    enableDisableButtonsForRCGrid: function () {
        var selectionModel = this.resourceCostGrid.getSelectionModel();
        if (selectionModel.hasSelection() && selectionModel.getCount() == 1) {
            if (this.editBttn) {
                this.editBttn.enable();
            }
            if (this.deleteBttn) {
                this.deleteBttn.enable();
            }
            
        } else if(selectionModel.getCount() > 1) {
            if (this.editBttn) {
                this.editBttn.disable();
            }
            if (this.deleteBttn) {
                this.deleteBttn.enable();
            }
            

        }else if (selectionModel.getCount() == 0) {
            if (this.editBttn) {
                this.editBttn.disable();
            }
            if (this.deleteBttn) {
                this.deleteBttn.disable();
            }
            
        }

    }
});