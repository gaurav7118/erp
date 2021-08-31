/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.account.RoutingMasterList = function(config) {

    this.id = config.id;
    this.arr = [];
    this.moduleId = Wtf.MRP_Route_Code_ModuleID;
    Wtf.apply(this, config);

    this.createGrid();
    this.addAdvanceSearchComponent();
    this.createReportButtons();
    
     this.addEvents({
        'routingtemplateupdate': true
    });

    this.on('routingtemplateupdate', function() {
        this.loadStore();
    }, this);

    Wtf.account.RoutingMasterList.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.RoutingMasterList, Wtf.Panel, {
    onRender: function(config) {

        this.createLeadPanel();
//        this.loadColumnModel();
        this.loadStore();
        Wtf.account.RoutingMasterList.superclass.onRender.call(this, config);
    },
    createLeadPanel: function() {
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
                            tbar: this.toolbarPanel,
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
                                items: this.bottomBbarArr
                            })

                        }
            ]
        });

        this.add(this.leadpan);
    },
    handleStoreOnLoad: function(store) {
        this.grid.getView().refresh();
        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        this.quickPanelSearch.StorageChanged(store);
    },
    loadStore: function() {
        this.Store.load({
            params: {
                start: 0,
                limit: this.pP.combo != undefined ? this.pP.combo.value : 30,
                ss: this.quickPanelSearch.getValue(),
                isRoutingCode:this.serachCombo.getValue()==1?false:true,
                pagingFlag: true
            }
        });

    },
    fetchStatement: function() {
        if(this.startDate.getValue() > this.endDate.getValue()){
           WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 2);
            return; 
        }
        this.Store.load({
            params: {
                start: 0,
                limit: this.pP.combo != undefined ? this.pP.combo.value : 30,
                ss: this.quickPanelSearch.getValue(),
                isRoutingCode:this.serachCombo.getValue()==1?false:true,
                pagingFlag: true
            }
        });
    },
     confirmDelete: function (isTempDelete, isPermDelete) {
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"),
            msg:  WtfGlobal.getLocaleText("acc.mrp.routingtemplate.delete.confirm.msg"),
            buttons: Wtf.MessageBox.YESNO,
            icon: Wtf.MessageBox.QUESTION,
            width: 300,
            scope: {
                scopeObject: this
            },
            fn: function (btn) {
                if (btn == "yes") {
                    var obj = {};
                    obj.isTempDelete = isTempDelete;
                    obj.isPermDelete = isPermDelete;
                    this.scopeObject.handleDelete(obj);
                } else {
                    return;
                }

            }
        }, this);
    },
    handleDelete: function (obj) {

        if (this.grid.getSelectionModel().hasSelection() == false) {
            WtfComMsgBox(34, 2);
            return;
        }
        var data = [];
        var arr = [];
        this.recArr = this.grid.getSelectionModel().getSelections();
        var templateNames='';
        var altewrnateTemplateNames='';
        for (i = 0; i < this.recArr.length; i++) {
            arr.push(this.recArr[i].data.id);
            templateNames+=this.recArr[i].data.routingtname+',';
            if(this.recArr[i].data.routingtname!=undefined){
                altewrnateTemplateNames+=this.recArr[i].data.alternateroutingtname+',';
            }
        }
        templateNames=templateNames.substring(0,(templateNames.length-1));
         altewrnateTemplateNames=altewrnateTemplateNames.substring(0,(altewrnateTemplateNames.length-1));
         
         if(altewrnateTemplateNames!='' && altewrnateTemplateNames.length > 0){
              templateNames=templateNames+', '+altewrnateTemplateNames;
         }
        var data = {
            root: arr
        }
        var json = Wtf.encode(data);

        var params = {
            jsonObj: json,
            templateNames:templateNames,
            isTempDelete: obj.isTempDelete,
            isPermDelete: obj.isPermDelete
        }
        this.ajxUrl = "ACCRoutingManagement/deleteRoutingTemplate.do";
        Wtf.Ajax.requestEx({
            url: this.ajxUrl,
            params: params
        }, this, this.genSuccessResponse, this.genFailureResponse);
    },
    genSuccessResponse: function (response, request) {
        if (response.success) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], 0);
            this.loadStore();
        } else {
            var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            if (response.msg) {
                msg = response.msg;
            }
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        }
    },
    genFailureResponse: function (response) {
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
        }
        this.fetchStatement();
    },
    createGrid: function() {

        this.sm = new Wtf.grid.CheckboxSelectionModel({
        });
         this.sm.on('selectionchange', this.enableDisableButtons, this);
//        this.sm.on("selectionchange", this.enableDisableButtons.createDelegate(this), this);
         this.Store = new Wtf.data.Store({
             url: "ACCRoutingManagement/getRoutingTemplates.do",
             baseParams:{
               isforcombo:false  
             },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
        this.Store.on('load', this.handleStoreOnLoad, this);
          this.Store.on('beforeload', this.handleBeforeLoad, this);
        this.grid = new Wtf.grid.GridPanel({
            store: this.Store,
            border: false,
            columns: [],
            layout: 'fit',
            sm: this.sm,
             viewConfig: {
                forceFit: false,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            },
            loadMask: true
        });
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
    handleBeforeLoad: function (s, o) {
        o.params.ss = this.quickPanelSearch.getValue()!=undefined ? this.quickPanelSearch.getValue() :'';
        o.params.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        o.params.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        o.params.isRoutingCode=this.serachCombo.getValue()==1?false:true;
    },
    handleStoreOnLoad : function () {
        var columns = [];
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect: true
        });
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
    },
    configurAdvancedSearch: function () {
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
    clearStoreFilter: function() {
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
    enableDisableButtons: function() {
          var selectionModel = this.grid.getSelectionModel();
        if (selectionModel.hasSelection() && selectionModel.getCount() == 1) {
            if (this.editBtn) {
                this.editBtn.enable();
            }
           
            if (this.deleteRoutingTemplatePerm) {
                this.deleteRoutingTemplatePerm.enable();
                this.deletelabourTemp.enable();
            }
        } else if (selectionModel.getCount() > 1) {
            if (this.editBtn) {
                this.editBtn.disable();
            }
           
            if (this.deleteRoutingTemplatePerm) {
                this.deleteRoutingTemplatePerm.enable();
                this.deletelabourTemp.enable();
            }

        } else if (selectionModel.getCount() == 0) {
            if (this.editBtn) {
                this.editBtn.disable();
            }
            
            if (this.deleteRoutingTemplatePerm) {
                this.deleteRoutingTemplatePerm.disable();
                this.deletelabourTemp.disable();
            }
        }
         var selectionArr =  this.grid.getSelectionModel().getSelections();
        /* If record is Temporary Deleted then Temporary Delete button and Edit button are disable */
        for(var i=0;i<selectionArr.length;i++){
            if(selectionArr[i]&&selectionArr[i].data.deleted){
                if(this.deletelabourTemp){
                    this.deletelabourTemp.disable();
                }
                if(this.editBtn){
                    this.editBtn.disable();
                }
                break;
            }
        }
    },
    createReportButtons: function() {
        var routeMasterArray = [];
        this.btnArr = [];
        this.secondbtnArr=[];
            
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.routingtemplate, Wtf.Perm.routingtemplate.creatert)) {
            this.createBtn = new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.nee.28") + " New " + WtfGlobal.getLocaleText("acc.mrp.field.route"), //'Add New Machin ',
                tooltip: {text: WtfGlobal.getLocaleText("acc.nee.28") + " New " + WtfGlobal.getLocaleText("acc.mrp.field.route")},
                iconCls: getButtonIconCls(Wtf.etype.menuadd),
                scope: this,
                handler: this.createRoutingMaster.createDelegate(this, [false])
            })
            routeMasterArray.push(this.createBtn);
        }

        if (!WtfGlobal.EnableDisable(Wtf.UPerm.routingtemplate, Wtf.Perm.routingtemplate.modifyrt)) {
            this.editBtn = new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.common.editBtn") + " " + WtfGlobal.getLocaleText("acc.mrp.field.route"), //'Edit Machin Master',
                disabled: true,
                tooltip: {text: WtfGlobal.getLocaleText("acc.productList.editProductTT"), dtext: WtfGlobal.getLocaleText("acc.mrp.field.route")}, //,etext:" Edit selected product details."},
                scope: this,
                iconCls: getButtonIconCls(Wtf.etype.menuedit),
                handler: this.createRoutingMaster.createDelegate(this, [true])
            })
            routeMasterArray.push(this.editBtn);
        }

        if (!WtfGlobal.EnableDisable(Wtf.UPerm.routingtemplate, Wtf.Perm.routingtemplate.deletert)) {

            this.deletelabourTemp = new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.mrp.routingtemplate.deletetemp.title"),
                id: 'deleteLabour',
                scope: this,
                tooltip: WtfGlobal.getLocaleText("acc.mrp.routingtemplate.deletetemp.title"),
                disabled: true,
                iconCls: getButtonIconCls(Wtf.etype.menudelete),
                handler: this.confirmDelete.createDelegate(this, [true, false])
            });

            routeMasterArray.push(this.deletelabourTemp);

            this.deleteRoutingTemplatePerm = new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.common.delete") + " " + WtfGlobal.getLocaleText("acc.mrp.field.route") + " " + WtfGlobal.getLocaleText("acc.common.deletePermanently"),
//                id: "deleteMachinePerm" + this.id,
                scope: this,
                tooltip: WtfGlobal.getLocaleText("acc.common.deletePermanently"),
                disabled: true,
                iconCls: getButtonIconCls(Wtf.etype.menudelete),
                handler: this.confirmDelete.createDelegate(this, [false, true])
            })
            routeMasterArray.push(this.deleteRoutingTemplatePerm);
        }

        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: "Search by Routing template name", 
            width: 150,
            id: "quickSearch" + this.id,
            field: 'routingtemplate',
             Store: this.Store

        });

        this.btnArr.push(this.quickPanelSearch);
        var tranStore = new Wtf.data.SimpleStore({
            fields: ['id', 'name'],
            data: [["1", "Routing Template"],
                ["2", "Routing Code"]
            ],
            autoLoad: true
        });
        this.searchCaseComboValue = "1";
        this.transationData = "View : ";
        this.serachCombo = new Wtf.form.ComboBox({
            selectOnFocus: true,
            triggerAction: 'all',
            mode: 'local',
            store: tranStore,
            useDefault: true,
            displayField: 'name',
            typeAhead: true,
            valueField: 'id',
            anchor: '100%',
            value: this.searchCaseComboValue
        });
        
        this.serachCombo.on('change',this.fetchStatement,this);
      
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"),
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false,
            handler: this.handleResetClick
        });

        this.btnArr.push(this.resetBttn);

        if (routeMasterArray.length > 0) {
            this.btnArr.push({
                text: WtfGlobal.getLocaleText("acc.machineMasterList.routingMenu"), // Routing Master Menu
                tooltip: WtfGlobal.getLocaleText("acc.machineMasterList.routingMenu"),
//                id: "machinemastermenu",
                  iconCls: 'routingicon',
                menu: routeMasterArray
            });
        }

        this.secondbtnArr.push('-', WtfGlobal.getLocaleText("acc.common.from"));

        this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), // 'From',
            name: 'startdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: WtfGlobal.getDates(true)
        });

        this.secondbtnArr.push(this.startDate);
        this.secondbtnArr.push(WtfGlobal.getLocaleText("acc.common.to"));
        
        this.endDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), // 'To',
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'enddate',
            value: WtfGlobal.getDates(false)
        });


        this.secondbtnArr.push(this.endDate);


        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"),
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
        this.secondbtnArr.push(this.fetchBttn);
        
        this.btnArr.push(this.transationData);
        this.btnArr.push(this.serachCombo);

        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });

        this.btnArr.push(this.AdvanceSearchBtn);
        this.customReportViewBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            handler: this.customizeView,
            iconCls: 'accountingbase fetch'
        });
        this.btnArr.push('->',this.customReportViewBtn);
        
        var firsttbar = new Wtf.Toolbar(this.btnArr);
        var secondtbar = new Wtf.Toolbar(this.secondbtnArr);

        this.toolbarPanel = new Wtf.Panel({
            border: false,
            items: [firsttbar, secondtbar]
        });
        
        this.bottomBbarArr=[];
        
        this.exportButton = new Wtf.exportButton({
            obj: this,
            id: "exportButton" + this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
            filename: WtfGlobal.getLocaleText("mrp.routingtemplate.export.filename.title"),
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
            get: Wtf.autoNum.exportRoutingTemplate
        });
        
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.routingtemplate, Wtf.Perm.routingtemplate.exportrt)) {
            this.bottomBbarArr.push('-', this.exportButton);
        }
    },
    customizeView: function () {
        this.customizeViewWin = new Wtf.CustomizeReportView({
            title: WtfGlobal.getLocaleText("acc.field.CreateCustomizeView"),
            parentPanel: this,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            grid: this.grid,
            reportId: this.moduleId,
            modules: "" + this.moduleId
        });
        this.customizeViewWin.show();
    },
    createRoutingMaster: function(isEdit) {
        if (!isEdit) {
            createProjectforMRP(Wtf.Project_TemplateId.ROUTINGMASTER);
        } else {
            var formRecord = this.grid.getSelectionModel().getSelected();
            var id = formRecord.get("id");
            var projectId = formRecord.get("projectId");
            var alternateprojectId = formRecord.get("alternateprojectId");
            callRoutingTemplateMaster(projectId,"edit_" + id, formRecord,isEdit,alternateprojectId);
    }
    }

});


