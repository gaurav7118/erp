/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.account.MachineMasterList = function(config) {

    this.arr = [];
    this.id = config.id;
    this.btnArr = [];
    this.btnArr2 = [];
    this.bottombtnArr=[];
    this.machineMasterArray = [];
    this.isActiveMachine=false;
    this.isSubstituteMachine=false;
    this.isLeaseMachine=false;
    this.moduleId = Wtf.MACHINE_MANAGEMENT_MODULE_ID;
    Wtf.apply(this, config);

    this.createFilterComboForReport();
    this.createGrid();
    this.CreateReportButtons();
    this.addButtonInArray();

    this.exportButton = new Wtf.exportButton({
        obj: this,
        id: "exportButton" + this.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
        filename:  WtfGlobal.getLocaleText("mrp.machinemasterexport.exportTT"),
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
        get: Wtf.autoNum.machineMasterReport 
    });
    
    if (!WtfGlobal.EnableDisable(Wtf.UPerm.machinemaster, Wtf.Perm.machinemaster.exportmc)) {
        this.bottombtnArr.push(this.exportButton);
    }
    this.bottombtnArr.push('-');
    
     this.exportMachineUsagesButton = new Wtf.Toolbar.Button({
        iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export'),
        text: WtfGlobal.getLocaleText("mrp.machine.export.allocation"),
        scope: this,
        tooltip: WtfGlobal.getLocaleText("mrp.machine.export.allocation"),
        handler: this.exportMachineUsages
    });

    if (!WtfGlobal.EnableDisable(Wtf.UPerm.machinemaster, Wtf.Perm.machinemaster.exportmc)) {
        this.bottombtnArr.push(this.exportMachineUsagesButton);
    }


    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: this.moduleId,
        advSearch: false,
        parentPanelSearch: this,
        ignoreDefaultFields: false,
        isAvoidRedundent: true
    });


    this.sm.on("selectionchange", this.enableDisableButtons.createDelegate(this), this);

    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);

    this.Store.on('beforeload', this.setFitleringParametersBeforeLoadingStore, this);

    this.addEvents({
        'machineupdate': true
    });

    this.on('machineupdate', function() {
        this.loadStore();
    }, this);

    this.viewMachineMasterCombo.on('select', this.loadTypeStore, this);

    Wtf.account.MachineMasterList.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.MachineMasterList, Wtf.Panel, {
    onRender: function(config) {

        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [this.objsearchComponent
                        , {
                            region: 'center',
                            layout: 'fit',
                            border: false,
                            items: [this.gridtbarPanel],
                            tbar: [this.firstToolBar],
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
                                items: [this.exportButton,'-',this.exportMachineUsagesButton]
                            })

                        }
            ]
        });

        this.add(this.leadpan);
        this.loadStore();
        Wtf.account.MachineMasterList.superclass.onRender.call(this, config);
    },
    setFitleringParametersBeforeLoadingStore: function (s, o) {
        //Before loading machine store set all necessary fitlering paramters to machine master report store
        if (!o.params)
            o.params = {};
        if (this.isActiveMachine) {
            o.params.isactivemachine = this.isActiveMachine;
        } else if (this.isSubstituteMachine) {
            o.params.issubstitutemachine = this.isSubstituteMachine;
        } else if (this.isLeaseMachine) {
            o.params.isleasemachine = this.isLeaseMachine;
        }
        o.params.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue()); 
        o.params.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        o.params.workcenterid = (this.workCenterCombo.getValue() !== undefined || this.workCenterCombo.getValue() !== '') ? this.workCenterCombo.getValue() : '';
        o.params.machineids = (this.machineids !== undefined || this.machineids !== '') ? this.machineids : '';// when user clic on machine from work order report - this parameter will be sent to java side
        this.exportButton.enable();
        
         this.exportButton.setParams({
             startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
             enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
             workcenterid:(this.workCenterCombo.getValue() !== undefined || this.workCenterCombo.getValue() !== '') ? this.workCenterCombo.getValue() : '',
             machineids:(this.machineids !== undefined || this.machineids !== '') ? this.machineids : ''
        });
    },
    setWorkCenterID: function (objParams) {
        this.wcid = objParams.wcid;
        this.isFromWC = objParams.isFromWC;
        this.setWorkCenterTofitlerMachines();
    },
    setmachineIDs: function (objParams) {
        this.machineids = objParams.machineids;
    },
    customizeView: function () {
        this.customizeViewWin = new Wtf.CustomizeReportView({
            title: WtfGlobal.getLocaleText("acc.field.CreateCustomizeView"),
            parentPanel: this,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            grid: this.grid,
            reportId: Wtf.MACHINE_MANAGEMENT_MODULE_ID,
            modules: "" + Wtf.MACHINE_MANAGEMENT_MODULE_ID
        });
        this.customizeViewWin.show();
    },
    loadTypeStore: function(a, rec) {
        if (this.startDate)
            this.startDate.setValue(WtfGlobal.getDates(true));
        if (this.endDate)
            this.endDate.setValue(WtfGlobal.getDates(false));
        this.recordType = undefined;
        this.isActiveMachine=false;
        this.isSubstituteMachine=false;  
        this.isLeaseMachine=false;  
        var index = rec.data.typeid;

        if (index == 1) {
         this.isActiveMachine=true;
        } else if (index == 2) {
         this.isSubstituteMachine=true;   
        } else if (index == 3) {
            this.isLeaseMachine=true;
        } 
        this.Store.load({
            params: {
                start: 0,
                limit: this.pP.combo.value,
                ss: this.quickPanelSearch.getValue()
            }
        });
        WtfComMsgBox(29, 4, true);
        this.Store.on('load', this.storeloaded, this);
    },
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();

    },
    handleStoreOnLoad: function(store) {
        var columns = [];
        columns.push(this.sm);
        columns.push(this.expander);  // Added expander button
        var scope = this;
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if(column.dataIndex == "workcenter"){
                 column.renderer = function (value, meta, rec) {
                    var value=rec.data.workcenter;
                  
                       return "<span wtf:qtip='"+value+"'>"+value  +"</span>";  
                    } 
                
            }else if (column.dataIndex == "dateofpurchase" || column.dataIndex == "dateofinstallation" ||column.dataIndex == "startdateoflease" ||column.dataIndex == "enddateoflease" ||column.dataIndex == "maintenanceschedule") {
                column.renderer = WtfGlobal.onlyDateDeletedRenderer;
            } else if (column.dataIndex !== "dateofpurchase" || column.dataIndex !== "dateofinstallation") {
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
    storeloaded:function(store){
        WtfGlobal.resetAjaxTimeOut();
        if(store.getCount()==0){
            if(this.exportButton)this.exportButton.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.exportButton)this.exportButton.enable();
            if(this.printButton)this.printButton.enable();
        }
        Wtf.MessageBox.hide();
        this.quickPanelSearch.StorageChanged(store);
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
    filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.Store.baseParams = {
            flag: 1,
            iscustomcolumndata: 0,
            searchJson: this.searchJson,
            moduleid: this.moduleId,
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
    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.loadStore();
        }
    },
    createFilterComboForReport: function() {

        this.viewMachineMasterStore = new Wtf.data.SimpleStore({
            fields: [{name: 'typeid', type: 'int'}, 'name'],
            data: [[0, WtfGlobal.getLocaleText("acc.machineMasterGrid.viewFilter.allmachine")], [1, WtfGlobal.getLocaleText("acc.machineMasterGrid.viewFilter.active")], [2, WtfGlobal.getLocaleText("acc.machineMasterGrid.viewFilter.substitute")],[3, WtfGlobal.getLocaleText("acc.machineMasterGrid.viewFilter.lease")]]
        });

        this.viewMachineMasterCombo = new Wtf.form.ComboBox({
            store: this.viewMachineMasterStore,
            name: 'viewMachineMasterCombo',
            displayField: 'name',
            valueField: 'typeid',
            mode: 'local',
            value: 0,
            width: 150,
            triggerAction: 'all',
            typeAhead: true,
            selectOnFocus: true
        });

        this.workCenterRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);

        this.workCenterStore = new Wtf.data.Store({
            url: "ACCWorkCentreCMN/getWorkCentreForCombo.do",
            baseParams: {
               // mode: 112,
                //groupid: 7
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.workCenterRec)
        });

        this.workCenterStore.on("load", function() {
            var record = new Wtf.data.Record({
                id: "",
                name: "All Records"
            });
            this.workCenterStore.insert(0, record);
            this.setWorkCenterTofitlerMachines();
            
        }, this);



        this.workCenterStore.load();
        this.workCenterCombo = new Wtf.form.ExtFnComboBox({
           fieldLabel: WtfGlobal.getLocaleText("acc.machine.workCenter"),
            name: 'workcenter',
            hiddenName: 'workcenter',
            store: this.workCenterStore,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            typeAhead: true,
            triggerAction: 'all',
            width:200,
            extraFields:[],
            addCreateOpt:false
        });

    },
    setWorkCenterTofitlerMachines: function () {
        /*Set value to wworkcenter filter combo and theen load machine master list store*/
        if (this.isFromWC) {
            this.workCenterCombo.setValue(this.wcid);
        } else {
            this.workCenterCombo.setValue("");
        }
        this.loadStore();
    },
    createGrid: function() {
        this.expandRec = Wtf.data.Record.create ([  // creating expander rec
            {name:'wocode'},
            {name:'woname'},
            {name:'routingcode'},
            {name:'routingname'},
            {name:'id'}
        ]);
        this.expandStore = new Wtf.data.Store({
            url:"ACCMachineMaster/getMachineExpanderDetails.do",
            baseParams:{
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.expandRec)
        });
        this.expandStore.on('load',this.fillExpanderBody,this);
        this.sm = new Wtf.grid.CheckboxSelectionModel({
        });
        this.Store = new Wtf.data.Store({
            url: "ACCMachineMaster/getMachineMasterDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: "totalCount",
                root: "data"
            })
        });
        this.expander = new Wtf.grid.RowExpander({});
        this.expander.on("expand",this.onRowexpand,this);
        this.grid = new Wtf.grid.GridPanel({
            store: this.Store,
            border: false,
            columns: [],
            layout: 'fit',
            sm: this.sm,
            plugins:[this.expander],
            viewConfig: {
                forceFit: false
            },
            loadMask: true
        });
        
        this.createSecondToolBar(); //Create Second Tool Bar
        this.gridtbarPanel = new Wtf.Panel({
            border: false,
            layout: 'border',
            items: [{
                    region: 'center',
                    layout: 'fit',
                    tbar: this.secondToolBar,
                    border: false,
                    items: [this.grid]
                }]
    });
        
        
        
    },
    CreateReportButtons: function() {

        this.Store.on('load', this.handleStoreOnLoad, this);
       if (!WtfGlobal.EnableDisable(Wtf.UPerm.machinemaster, Wtf.Perm.machinemaster.createmc)) {
            this.machineMasterArray.push(new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.machine.addNew") + " " + WtfGlobal.getLocaleText("acc.machineMaster.addNewActive"), //'Add New Machin ',
                tooltip: {text: WtfGlobal.getLocaleText("acc.machine.addNew") + " " + WtfGlobal.getLocaleText("acc.machineMaster.addNewActive")},
                iconCls: getButtonIconCls(Wtf.etype.menuadd),
                scope: this,
                handler: this.showForm.createDelegate(this, [false, false])
            }));
        }
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.machinemaster, Wtf.Perm.machinemaster.createmc)) {
            this.machineMasterArray.push(new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.machine.addNew") + " " + WtfGlobal.getLocaleText("acc.machineMaster.addNewSubstitute"), //'Add New Substitue Machin ',
                tooltip: {text: WtfGlobal.getLocaleText("acc.machine.addNew") + " " + WtfGlobal.getLocaleText("acc.machineMaster.addNewSubstitute")},
                iconCls: getButtonIconCls(Wtf.etype.menuadd),
                scope: this,
                handler: this.showForm.createDelegate(this, [true, false])
            }));
        }

        if (!WtfGlobal.EnableDisable(Wtf.UPerm.machinemaster, Wtf.Perm.machinemaster.modifymc)) {
            this.machineMasterArray.push(this.editBttn = new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.common.editBtn") + " " + WtfGlobal.getLocaleText("acc.common.mrp.name"), //'Edit Machin Master',
                disabled: true,
                tooltip:{text: WtfGlobal.getLocaleText("acc.machineMaster.EditMachine")},
                scope: this,
                iconCls: getButtonIconCls(Wtf.etype.menuedit),
                handler: this.showForm.createDelegate(this, [false, true])
            }));
        }

        if (!WtfGlobal.EnableDisable(Wtf.UPerm.machinemaster, Wtf.Perm.machinemaster.deletemc)) {
            this.machineMasterArray.push(this.deleteMachineTemp = new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.common.delete") + " " + WtfGlobal.getLocaleText("acc.common.mrp.name"),
                id: "deleteMachineTemp" + this.id,
                scope: this,
                tooltip: WtfGlobal.getLocaleText("acc.common.delete"),
                disabled: true,
                iconCls: getButtonIconCls(Wtf.etype.menudelete),
                handler: this.confirmDelete.createDelegate(this, [true, false])
            }))
        }
         if (!WtfGlobal.EnableDisable(Wtf.UPerm.machinemaster, Wtf.Perm.machinemaster.deletemc)) {
            this.machineMasterArray.push(this.deleteMachinePerm = new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.common.delete") + " " + WtfGlobal.getLocaleText("acc.common.mrp.name") + " " + WtfGlobal.getLocaleText("acc.common.deletePermanently"),
                id: "deleteMachinePerm" + this.id,
                scope: this,
                tooltip: WtfGlobal.getLocaleText("acc.common.deletePermanently"),
                disabled: true,
                iconCls: getButtonIconCls(Wtf.etype.menudelete),
                handler: this.confirmDelete.createDelegate(this, [false, true])
            }))
        }

        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.machineList.quickSearch"), //WtfGlobal.getLocaleText("acc.stockLedger.QuickSearchEmptyText"), // "Search by Document no, Description, Code, Party / Cost Center ...",
            width: 200,
            id: "quickSearch" + this.id,
            field: 'transactionNumber'

        });

        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"),
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false,
            handler: this.handleResetClick
        });

        

        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });

        this.customReportViewBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            scope: this,
            // hidden:!(this.isOrder && this.isCustomer),
            tooltip: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            handler: this.customizeView,
            iconCls: 'accountingbase fetch'
        });
    },
    createSecondToolBar:function(){
        
        this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), // 'From',
            name: 'startdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: WtfGlobal.getDates(true)
        });

        this.endDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), // 'To',
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'enddate',
            value: WtfGlobal.getDates(false)
        });

        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"),
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
        
        this.btnArr2.push(WtfGlobal.getLocaleText("acc.common.from"));
        this.btnArr2.push(this.startDate);
        this.btnArr2.push(WtfGlobal.getLocaleText("acc.common.to"));
        this.btnArr2.push(this.endDate);
        this.btnArr2.push(this.fetchBttn);
        this.secondToolBar=new Wtf.Toolbar(this.btnArr2);
        
        
    },
    addButtonInArray: function() {
        this.btnArr.push(this.quickPanelSearch);
        this.btnArr.push(this.resetBttn);

        if (!this.isFromWC) {
            if (this.machineMasterArray.length > 0) {
                this.btnArr.push({
                    text: WtfGlobal.getLocaleText("acc.machineMasterList.machineMenu"), //'Products And Services Menu',
                    tooltip: WtfGlobal.getLocaleText("acc.machineMasterList.machineMenu"), //{text:"Click here to add, edit, clone or delete a product."},
                    id: "machinemastermenu",
                    iconCls: 'machineicon',
                    menu: this.machineMasterArray
                });
            }
        }
        this.btnArr.push("&nbsp;View", this.viewMachineMasterCombo);
        this.btnArr.push("-", WtfGlobal.getLocaleText("acc.machineMasterGrid.workcenterFilter"), this.workCenterCombo);
       
        
        this.btnArr.push(this.AdvanceSearchBtn);
        
         this.btnArr.push('-');
        this.btnArr.push('->',this.customReportViewBtn);
        this.firstToolBar=new Wtf.Toolbar(this.btnArr);
        

    },
    showForm: function(isSubstituteMachine, isEdit) {
        var obj = {};
        obj.isSubstituteMachine = isSubstituteMachine;
        obj.isEdit = isEdit;
        if (isEdit) {
            obj.record = this.grid.getSelectionModel().getSelected();
            obj.isSubstituteMachine = obj.record.data.issubstitute;
        }
        callMRPMachineMaster(obj);

    },
    enableDisableButtons: function() {
        Wtf.uncheckSelAllCheckbox(this.sm);
        if (true) {
            if (this.deleteMachineTemp) {
                this.deleteMachineTemp.enable();
            }
            if (this.deleteMachinePerm) {
                this.deleteMachinePerm.enable();
            }

            var arr = this.grid.getSelectionModel().getSelections();
            if (arr.length == 0) {
                if (this.deleteMachineTemp) {
                    this.deleteMachineTemp.disable();
                }
                if (this.deleteMachinePerm) {
                    this.deleteMachinePerm.disable();
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
                    if(this.deleteMachineTemp){
                        this.deleteMachineTemp.disable();
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
            msg: WtfGlobal.getLocaleText("acc.rem.146") + " " + WtfGlobal.getLocaleText("acc.machineMaster.deleteMsg"),
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
                    this.scopeObject.deleteMachineMaster(obj);
                } else {
                    return;
                }

            }
        }, this);
    },
    deleteMachineMaster: function(obj) {
        this.deleteUrl = "";
        var data = [];
        if (obj != undefined) {
            if (obj.isTempDelete) {
                this.deleteUrl = "ACCMachineMaster/deleteMachineMaster.do";
            } else if (obj.isPermDelete) {
                this.deleteUrl = "ACCMachineMaster/deleteMachineMasterPermanently.do";
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
        if (response.success) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.mrp.machinemasterReport.reportName"),
                msg: response.msg,
                width: 500,
                scope: {
                    scopeObj: this
                },
                buttons: Wtf.MessageBox.OK,
                fn: function(btn ,text, option) {
                    this.scopeObj.refreshReportGrid();
                },
                animEl: 'mb9',
                icon: Wtf.MessageBox.INFO
            });
        }
    },
    genFailureResponse: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if (response.msg) {
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    refreshReportGrid: function(){
        var comp = null;
        comp = Wtf.getCmp('machinemasterlist');
        if(comp){
        comp.fireEvent('machineupdate');
        }  
    },
    syncMachineDataToPM: function() {
        this.syncUrl = "ACCMachineMaster/syncMachineDataToPM.do";
        
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"),
            msg: WtfGlobal.getLocaleText("acc.machineMaster.syncMachineDoYouWantToContinue"),
            buttons: Wtf.MessageBox.YESNO,
            icon: Wtf.MessageBox.QUESTION,
            width: 350,
            scope: {
                scopeObject: this
            },
            fn: function(btn) {
                if (btn == "yes") {
                    Wtf.Ajax.requestEx({
                        url: this.scopeObject.syncUrl,
                        params: {
                            synctopm: true
                        }
                    }, this, this.scopeObject.genSyncSuccessResponse, this.scopeObject.genSyncFailureResponse);
                } else {
                    return;
                }
            }
        }, this);
        
        
        
     
    },
    exportMachineUsages: function() {
        var arr = this.grid.getSelectionModel().getSelections();
        if (arr.length > 0) {
            var resourceIds = [];
            for(var i=0; i<arr.length ; i++){
                resourceIds.push(arr[i].get("id"));
            }
            resourceUsagesExportWindow(resourceIds, "Machine");
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("mrp.machine.export.allocationselect")], 2);
        }
    },
    genSyncSuccessResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        if (response.success) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.mrp.machinemasterReport.reportName"),
                msg: response.msg,
                width: 500,
                scope: {
                    scopeObj: this
                },
                buttons: Wtf.MessageBox.OK,
                animEl: 'mb9',
                icon: Wtf.MessageBox.INFO
            });
        }else {        
            var msg = WtfGlobal.getLocaleText("acc.mrppm.failure");//"Failed to make connection with Web Server";
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg]);         
        }
    },
    genSyncFailureResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
   },
   // Function to fill expander body
   fillExpanderBody: function() {
        var disHtml = "";
        this.custArr = [];
        var previd = "";
        var sameParent = false;
        var woHeader = this.woheader();
        
        for (var i = 0; i < this.expandStore.getCount(); i++) {
            var header = "";
            var rec = this.expandStore.getAt(i);
            
            var currentid = rec.data['id'];
            if (previd != currentid) {             
                previd = currentid;
                sameParent = false;
            } else {
                sameParent = true;
            }
            
            header = this.getExpanderData(rec, sameParent, woHeader[1], woHeader[2]);
            var moreIndex = this.grid.getStore().findBy(
                function(record, id) {
                    if (record.get('id') === rec.data['id']) {
                        return true;  // a record with this data exists 
                    }
                    return false;  // there is no record in the store with this data
                }, this);
            if (moreIndex != -1) {
//            if (true) {
                var body = Wtf.DomQuery.selectNode('tr:nth(2) div.x-grid3-row-body', this.grid.getView().getRow(moreIndex));
                disHtml = "<div class='expanderContainer1'>" + woHeader[0] + header + "</div>";
                body.innerHTML = disHtml;
                if (this.expandButtonClicked) {
                    this.expander.suspendEvents('expand');              //suspend 'expand' event of RowExpander only in case of ExpandAll.
                    this.expander.expandRow(moreIndex);                // After data set to Grid Row, expand row forcefully.
                }
            }
        }
    },
    //creating rows it data comes in the request
    getExpanderData: function (rec, sameParent, minWidth, widthInPercent) {
        if (!sameParent) {
            this.Repeatheader = "";
            this.serialNumber = 0;
        }
        this.Repeatheader += "<div style='width: 100%;min-width:" + minWidth + "px'>";
        var wocode = rec.data['wocode'];
        var woname = rec.data['woname'];

        this.Repeatheader += "<span class='gridNo'>" + (++this.serialNumber) + ".</span>";

        //Column : Machine Name
        this.Repeatheader += "<span class='gridRow'  wtf:qtip='" + wocode + "' style='width: " + widthInPercent + "% ! important;'><a class='jumplink' wtf:qtip='" + wocode + "' href='#' onClick='javascript:callMachineMasterList(\"" + '' + "\",\"" + false + "\",\"" + '' + "\",\"" + '' + "\",\"" + wocode + "\")'>" + Wtf.util.Format.ellipsis(wocode, 10) + "&nbsp;</a></span>";
        this.Repeatheader += "<span class='gridRow'  wtf:qtip='" + woname + "' style='width: " + widthInPercent + "% ! important;'><a class='jumplink' wtf:qtip='" + woname + "' href='#' onClick='javascript:callMachineMasterList(\"" + '' + "\",\"" + false + "\",\"" + '' + "\",\"" + '' + "\",\"" + woname + "\")'>" + Wtf.util.Format.ellipsis(woname, 10) + "&nbsp;</a></span>";

        var routecode = rec.data['routingcode'];

        this.Repeatheader += "<span class='gridRow'  wtf:qtip='" + routecode + "' style='width: " + widthInPercent + "% ! important;'><a class='jumplink' wtf:qtip='" + routecode + "' href='#' onClick='javascript:callLabourDetails(\"" + "" + "\",\"" + "" + "\",\"" + "" + "\",\"" + "" + "\",\"" + routecode + "\")'>" + Wtf.util.Format.ellipsis(routecode, 10) + "&nbsp;</a></span>";
        this.Repeatheader += "<br>";
        this.Repeatheader += "</div>";
        return this.Repeatheader;

    },
    // Creating header for expander
    woheader: function() {
        var arr=[];
        var headerArray = [];
        arr=["Work Order ID", "Work Order Name","Route Code","           "];
        var header = "<span class='gridHeader'>Other Details</span>";   //Account List
       
        var arrayLength=arr.length;
        var count=0;
        for(var i=0;i<arr.length;i++){
            if(arr[i] != ""){
                count++;
            }
        }
        var widthInPercent=100/count;
        var minWidth = count*100 + 40;
        header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
        header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
        for(var i=0;i<arr.length;i++){
            header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[i] + "</span>";
        }
        header += "</div><div style='width: 100%;min-width:"+minWidth+"px'><span class='gridLine'></span></div>";  
        headerArray.push(header);
        headerArray.push(minWidth);
        headerArray.push(widthInPercent);
        return headerArray;
    },
    // loading expander data on clicking expander button
    onRowexpand: function(scope, record) {
        this.expandStore.load({
            params:{
                id:record.data.id
                }
            });
    }
});