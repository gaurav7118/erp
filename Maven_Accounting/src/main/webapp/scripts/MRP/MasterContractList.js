/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

Wtf.account.ContractMasterList = function(config) {
    
    this.arr = [];
    Wtf.apply(this, config);

    this.id = config.id;
    this.isReport = config.isReport ? config.isReport : false;
    this.moduleId = Wtf.MRP_MASTER_CONTRACT_MODULE_ID;

    this.createGrid();
    this.addAdvanceSearchComponent();
    this.CreateReportButtons();

    this.addEvents({
        'mastercontractupdate': true
    });

    this.on('mastercontractupdate', function() {
        this.loadStore();
    }, this);

    Wtf.account.ContractMasterList.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.ContractMasterList, Wtf.Panel, {
    onRender: function(config) {


        var FirstTopToolaBr = new Wtf.Toolbar(this.tbar1);
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [this.objsearchComponent
                        , {
                            region: 'center',
                            layout: 'fit',
                            border: false,
                            items: [this.grid],
                            tbar: FirstTopToolaBr,
                            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                                pageSize: 30,
                                id: "pagingtoolbar" + this.id,
                                store: this.Store,
                                searchField: this.quickPanelSearch,
                                displayInfo: true,
                                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No results to display",
                                plugins: this.pP = new Wtf.common.pPageSize({
                                    id: "pPageSize_" + this.id
                                }),
                                items: this.bottomBtnArr
                            })

                        }
            ]
        });

        this.add(this.leadpan);
        this.loadStore();

        Wtf.account.ContractMasterList.superclass.onRender.call(this, config);
    },

    customizeView: function () {
        this.customizeViewWin = new Wtf.CustomizeReportView({
            title: WtfGlobal.getLocaleText("acc.field.CreateCustomizeView"),
            parentPanel: this,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            grid: this.grid,
            reportId: Wtf.MRP_MASTER_CONTRACT_MODULE_ID,
            modules: "" + Wtf.MRP_MASTER_CONTRACT_MODULE_ID
        });
        this.customizeViewWin.show();
    },
    loadStore: function() {
        this.Store.load({
            params: {
                start: 0,
                limit: this.pP.combo != undefined ? this.pP.combo.value : 30,
                ss: this.quickPanelSearch.getValue(),
                pagingFlag: true
            }
        });

    },
    fetchStatement: function() {
        this.Store.load({
            params: {
                start: 0,
                limit: this.pP.combo != undefined ? this.pP.combo.value : 30,
                ss: this.quickPanelSearch.getValue(),
                pagingFlag: true
            }
        });


    },
    configurAdvancedSearch: function () {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
    },
    filterStore: function (json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.Store.baseParams = {
            flag: 1,
            iscustomcolumndata: 0,
            searchJson: this.searchJson,
            moduleid: this.moduleId,
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
    clearStoreFilter: function () {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.baseParams = {
            flag: 1,
            iscustomcolumndata: 0,
            searchJson: this.searchJson,
            moduleid: this.moduleId,
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
    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.loadStore();
        }
    },
    handleStoreOnLoad: function(store) {
        var columns = [];
        columns.push(this.sm);
        var scope = this;
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if( column.dataIndex == "attachment"){ 
                column.renderer = Wtf.DownloadLink.createDelegate(scope); 
            } else {
                column.renderer = WtfGlobal.deletedRenderer;
            }
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
    createGrid: function() {

        this.sm = new Wtf.grid.CheckboxSelectionModel({
        });
         this.sm.on("selectionchange", this.enableDisableButtons.createDelegate(this), this);
         
        this.Store = new Wtf.data.Store({
            url: "ACCContractMaster/getContractMasterDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: "totalCount",
                root: "data"
            })
        });
        
        this.Store.on('beforeload',this.handleBeforeLoadOFmasterOCntractStore, this);
        
        this.grid = new Wtf.grid.GridPanel({
            store: this.Store,
            border: false,
            columns: [],
            layout: 'fit',
            sm: this.sm,
            viewConfig: {
                forceFit: false
            },
            loadMask: true
        });
    },
    handleBeforeLoadOFmasterOCntractStore: function (s, o) {
        if (!o.params)
            o.params = {};
        o.params.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());   //ERP-8884 : RP/MP Report
        o.params.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        this.exportButton.enable();

    },
    addAdvanceSearchComponent: function () {
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.grid.colModel,
            moduleid: this.moduleId,
            advSearch: false,
            parentPanelSearch: this,
            ignoreDefaultFields: false,
            isAvoidRedundent: true
        });

        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
        this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    },
    CreateReportButtons: function() {

        this.bottomBtnArr = [];
        this.tbar1 = new Array();
        var deletebtnArray = [];

        this.Store.on('load', this.handleStoreOnLoad, this);
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.mastercontract.quicksearch.emptytext"),//"Search by Master Contract Details",
            width: 200,
            id: "quickSearch" + this.id,
            field: 'transactionNumber'

        });
        this.tbar1.push(this.quickPanelSearch);
       
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), 
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false,
            handler: this.handleResetClick
        });

        this.tbar1.push(this.resetBttn);
       
        this.createnewbttn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.rem.138"),
            tooltip: {text: WtfGlobal.getLocaleText("acc.rem.138")+ WtfGlobal.getLocaleText("acc.mastercontract.title")},
            iconCls: getButtonIconCls(Wtf.etype.menuadd),
            scope: this,
            handler:this.showForm.createDelegate(this,[false]),
            hidden:this.isReport
        });
        
         if (!WtfGlobal.EnableDisable(Wtf.UPerm.mastercontract, Wtf.Perm.mastercontract.createmstr)) {
            this.tbar1.push(this.createnewbttn);
        }
       
        this.editBttn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.common.edit"), //'Edit',
            tooltip: WtfGlobal.getLocaleText("acc.contractmasterlist.Edit"),
            id: 'btnEdit' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.edit),
            disabled: true,
            handler:this.showForm.createDelegate(this,[true]),
            hidden:this.isReport
        });
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.mastercontract, Wtf.Perm.mastercontract.modifymstr)) {
            this.tbar1.push(this.editBttn);
        }
       
        deletebtnArray.push(this.deleteContractTemp=new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.common.delete"), //'Delete 
            disabled: true,
            tooltip: WtfGlobal.getLocaleText("acc.common.delete"),
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.menudelete),
            handler: this.confirmDelete.createDelegate(this, [true, false])
        }));

        deletebtnArray.push(this.deleteContractPerm=new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.common.delete") +" " + WtfGlobal.getLocaleText("acc.common.deletePermanently"), //'Delete  permanently',
            disabled: true,
            tooltip: WtfGlobal.getLocaleText("acc.common.delete") + " " + WtfGlobal.getLocaleText("acc.common.deletePermanently"),
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.menudelete),
            handler: this.confirmDelete.createDelegate(this, [false,true])
        }));

        if (deletebtnArray.length > 0) {
            if (!WtfGlobal.EnableDisable(Wtf.UPerm.mastercontract, Wtf.Perm.mastercontract.deletemstr)) {
                this.tbar1.push({
                    text: WtfGlobal.getLocaleText("acc.common.delete"), //'Contract Master Menu',
                    tooltip: WtfGlobal.getLocaleText("acc.common.delete"), //{delete a Contract."},
                    id: "contractMastermenu",
                    iconCls: getButtonIconCls(Wtf.etype.menudelete),
                    menu: deletebtnArray,
                    hidden: this.isReport
                });
            }
        }

        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
//        id: 'advanced3', // In use, Do not delete
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });
        this.tbar1.push(this.AdvanceSearchBtn);
       
       

        this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), // 'From',
            name: 'startdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: WtfGlobal.getDates(true)
        });

        this.tbar1.push(WtfGlobal.getLocaleText("acc.common.from"),this.startDate);

        this.endDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), // 'To',
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'enddate',
            value: WtfGlobal.getDates(false)
        });


        this.tbar1.push(WtfGlobal.getLocaleText("acc.common.to"),this.endDate);
       
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"),
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });


        this.tbar1.push(this.fetchBttn);
       
        this.customReportViewBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            handler: this.customizeView,
            hidden:true, //SDP-11260
            iconCls: 'accountingbase fetch'
        });

        this.tbar1.push('->', this.customReportViewBtn);

        this.exportButton = new Wtf.exportButton({
            obj: this,
            id: "exportButton" + this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
            filename: WtfGlobal.getLocaleText("acc.mastercontract.export.filename") + '_v1',
            disabled: false,
            scope: this,
            menuItem: {
                csv: true,
                pdf: true,
                rowPdf: false,
                xls: true
            },
            params: {
                enddate: WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue())
            },
            get: Wtf.autoNum.contractMasterReport
        });
        
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.mastercontract, Wtf.Perm.mastercontract.exportmstr)) {
            this.bottomBtnArr.push('-', this.exportButton);
        }
       
    },
    
    showForm: function(isEdit){
        var winid=(winid==null?"MasterContractTab":winid);
        var panel = Wtf.getCmp(winid);
        if(panel == null){
            var record = this.grid.getSelectionModel().getSelected();
            var title = isEdit ? (WtfGlobal.getLocaleText("acc.common.edit") +" "+ WtfGlobal.getLocaleText("acc.mastercontract.title")) :  (WtfGlobal.getLocaleText("acc.lp.createmstr")+" "+ WtfGlobal.getLocaleText("acc.mastercontract.title"));
            var tabTip = isEdit ? (WtfGlobal.getLocaleText("acc.common.edit") +" "+ WtfGlobal.getLocaleText("acc.mastercontract.title")) :  (WtfGlobal.getLocaleText("acc.lp.createmstr")+" "+ WtfGlobal.getLocaleText("acc.mastercontract.title"));
            panel=new Wtf.account.MasterContract({
                title: title,
                tabTip :tabTip,
                id:winid,//Do not channge as this is used somewhere
                iconCls: 'accountingbase vendor',
                border: false,
                closable:true,
                modeName: 'automc',
                isEdit: isEdit,
                record: record
            });
            panel.on("resize", function(){
                this.doLayoutOfAllTabs();
            },panel);
            Wtf.getCmp('as').add(panel);
        }

        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    },
    enableDisableButtons: function() {
        Wtf.uncheckSelAllCheckbox(this.sm);
        if (true) {
            if (this.deleteContractTemp) {
                this.deleteContractTemp.enable();
            }
            if (this.deleteContractPerm) {
                this.deleteContractPerm.enable();
            }

            var arr = this.grid.getSelectionModel().getSelections();
            if (arr.length == 0) {
                if (this.deleteContractTemp) {
                    this.deleteContractTemp.disable();
                }
                if (this.deleteContractPerm) {
                    this.deleteContractPerm.disable();
                }
            }
            var rec = this.sm.getSelected();

            if (this.sm.getCount() == 1) {
                if (this.editBttn) {
                    this.editBttn.enable();
                }
            } else {
                if (this.editBttn) {
                    this.editBttn.disable();
                }
            }
            /* If record is Temporary Deleted then Temporary Delete button and Edit button are disable */
            for(var i=0;i<arr.length;i++){
                if(arr[i]&&arr[i].data.deleted){
                    if(this.deleteContractTemp){
                        this.deleteContractTemp.disable();
                    }
                    if(this.editBttn){
                        this.editBttn.disable();
                    }
                    break;
                }
            }
        }
    },
    confirmDelete: function(isTempDelete, isPermDelete) {
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"),
            msg: WtfGlobal.getLocaleText("acc.rem.146") + " " + WtfGlobal.getLocaleText("mrp.mastercontract.delete.confirm.msg") ,
            buttons: Wtf.MessageBox.YESNO,
            icon: Wtf.MessageBox.QUESTION,
            width: 300,
            scope: {
                scopeObject: this
            },
            fn: function(btn) {
                if (btn == "yes") {
                    var obj = {};
                    obj.isTempDelete = isTempDelete;
                    obj.isPermDelete = isPermDelete;
                    this.scopeObject.deleteContractMaster(obj);
                } else {
                    return;
                }
            }
        }, this);
    },
    deleteContractMaster: function(obj) {
        this.deleteUrl = "";
        var data = [];
        if (obj != undefined) {
            if (obj.isTempDelete) {
                this.deleteUrl = "ACCContractMaster/deleteMasterContracts.do";
            } else if (obj.isPermDelete) {
                this.deleteUrl = "ACCContractMaster/deleteMasterContractsPermanently.do";
            }

            this.recArr = this.grid.getSelectionModel().getSelections();
            this.grid.getSelectionModel().clearSelections();
            var idData = "";
            for (var i = 0; i < this.recArr.length; i++) {
                var rec = this.recArr[i];
                idData += "{\"id\":\"" + rec.get('id') + "\"},";
            }
            if (idData.length > 1) {
                idData = idData.substring(0, idData.length - 1);
            }
            data = "[" + idData + "]";
            Wtf.Ajax.requestEx({
                url: this.deleteUrl,
                params: {
                    data: data,
                    isdelete: true
                }
            }, this, this.genSuccessResponse, this.genFailureResponse);
        }
    },
    genSuccessResponse: function(response) {
        WtfGlobal.resetAjaxTimeOut();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), response.msg], response.success * 2 + 1);
        this.loadStore();
    },
    genFailureResponse: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if (response.msg) {
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    }

});