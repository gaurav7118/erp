/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.workCentreMaster = function(config) {
    this.arr = [];
    Wtf.apply(this, config);
    this.isReport = config.isReport ? config.isReport : false;
    this.moduleid = Wtf.MRP_Work_Centre_ModuleID;
    this.createTbar();  
    this.creategrid();
    this.addAdvanceSearchComponent();
    
    Wtf.workCentreMaster.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.workCentreMaster, Wtf.Panel, {
    onRender: function(config) {
        Wtf.workCentreMaster.superclass.onRender.call(this, config);

      
         this.createPanel();
       
        this.pageLimit = new Wtf.forumpPageSize({
            ftree: this.grid
        });
   
        this.add(this.workCentrePanel);
        this.fetchStatement();
    },
    
    createPanel: function() {
        this.workCentrePanel = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [this.objsearchComponent,{
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid],
                    tbar: this.panelbtnarr,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.Store,
                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id}),
                        items: [this.exportButton,this.exportselRec]
                    })
                }]
        });
    },
    
    createTbar:function(){
       
        this.btnArr = [];
        this.panelbtnarr = [];
        
        
        this.panelbtnarr.push(this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.mrp.workcentre.searchtext"), 
            width: 200,
            id: "quickSearch" + this.id,
            field: 'workcenterid'
        }));
        
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
        
        this.panelbtnarr.push(this.fetchBttn);
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false,
            handler: this.handleResetClick
        });
        this.panelbtnarr.push(this.resetBttn);
        
        this.wcMasterMenu = [];
        this.createNew = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.mrp.field.AddWorkCentre"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.mrp.field.AddWorkCentre"),
            iconCls: getButtonIconCls(Wtf.etype.add),
            handler: this.showForm.createDelegate(this, [false])
        });
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.workcentre, Wtf.Perm.workcentre.createwc)) {
            this.wcMasterMenu.push(this.createNew);
        }
        this.editButton = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.mrp.field.EditWorkCentre"), //'Edit',
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.edit),
            tooltip: WtfGlobal.getLocaleText("acc.mrp.field.EditWorkCentre"),
            // disabled:true,
            handler: this.showForm.createDelegate(this, [true])
        });
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.workcentre, Wtf.Perm.workcentre.modifywc)) {
            this.wcMasterMenu.push(this.editButton);
        }
         
        this.CloneWorkcentre = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.mrp.field.CloneWorkCentre"),
            scope: this,
            iconCls: 'pwnd menu-clone',
            tooltip: WtfGlobal.getLocaleText("acc.mrp.field.CloneWorkCentre"), //'Delete to selected record',
            // disabled:true,
            handler: this.showForm.createDelegate(this, [true, true])
        });
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.workcentre, Wtf.Perm.workcentre.clonewc)) {
            this.wcMasterMenu.push(this.CloneWorkcentre);
        }
        
        this.deleteTrans = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.mrp.field.DeleteWorkcentre"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.mrp.field.DeleteWorkcentre"),
            iconCls: getButtonIconCls(Wtf.etype.menudelete),
            disabled: true,
            handler: this.confirmDelete.createDelegate(this, [true, false])
        });
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.workcentre, Wtf.Perm.workcentre.deletewc)) {
            this.wcMasterMenu.push(this.deleteTrans);
        }
        this.deleteTransperm = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.mrp.field.DeleteWorkcentrePermanently"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.mrp.field.DeleteWorkcentrePermanently"),
            iconCls: getButtonIconCls(Wtf.etype.menudelete),
            disabled: true,
            handler: this.confirmDelete.createDelegate(this, [false, true])
        });
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.workcentre, Wtf.Perm.workcentre.deletewc)) {
            this.wcMasterMenu.push(this.deleteTransperm);
        }
        this.wcMasterMenuBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.mrp.field.WorkCentreMasterMenu"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.mrp.field.WorkCentreMasterMenu"),
//            iconCls: getButtonIconCls(Wtf.etype.deletebutton),
            iconCls: 'accountingbase product',
            menu: this.wcMasterMenu
        });
        if (this.wcMasterMenu.length > 0) {
            this.panelbtnarr.push(this.wcMasterMenuBtn);
        }
    
        
        this.moduleName = WtfGlobal.getLocaleText("mrp.workorder.report.header11");
        var extraConfig = {};
        extraConfig.url = "";
        var extraParams = "";
        var importBtnArr = Wtf.importMenuArray(this, this.moduleName, this.Store, extraParams, extraConfig);

        this.importBtn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.common.import"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.common.import"),
            iconCls: (Wtf.isChrome ? 'pwnd importChrome' : 'pwnd import'),
            menu: importBtnArr
        });

        this.exportButton = new Wtf.exportButton({
            obj: this,
            id: 'productlistexport',
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"),  
            params: {name: WtfGlobal.getLocaleText("acc.prod.filename")},
            menuItem: {csv: true, pdf: true, rowPdf: false, xls: true},
            get: 1102,
            isProductExport: true,
            filename: "Work Centre List" + "_v1",
            label: WtfGlobal.getLocaleText("acc.field.ProductList1")
        });
       
       this.exportselRec = new Wtf.exportButton({
            obj: this,
            id: "selworlcenterlistexport",
            iconCls: 'pwnd exportpdfsingle',
            //        isProductExport: true,
            get: 1102,
            text: WtfGlobal.getLocaleText("acc.field.ExportSelRecord"), // + " "+ singlePDFtext,
            tooltip: WtfGlobal.getLocaleText("acc.field.ExportSelRecord"), //'Export selected record(s)'
            filename: "Work Centre List" + "_v1",
            disabled: true,
            params: {
                selworkCenterIds: [],
                totalWorkCenters:0
            },
            menuItem: {
                csv: true, 
                xls: true,
                pdf:true
            }
        });
        this.exportselRec.on('click', function () {
           var selectionArr =  this.grid.getSelectionModel().getSelections();
            var idsArray = [];
            for (var i = 0; i <selectionArr.length; i++) {
                idsArray.push(selectionArr[i].data['id']);
            }
            this.exportselRec.setParams({
                selworkCenterIds : idsArray,
                totalWorkCenters:idsArray.length
            });
        }, this);
        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        })
        this.panelbtnarr.push(this.AdvanceSearchBtn);
        this.customReportViewBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            handler: this.customizeView,
            iconCls: 'accountingbase fetch'
        });
        
         
        this.panelbtnarr.push('->',this.customReportViewBtn);
//        this.panelbtnarr.push('->',this.customReportViewBtn);
    },
    setWorkCenterID: function (objParams) {
        this.wcid = objParams.wcid;
    },
    confirmDelete: function(isTempDelete, isPermDelete) {
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"),
            msg: WtfGlobal.getLocaleText("acc.rem.146") + " " + WtfGlobal.getLocaleText("acc.workcentreMaster.deleteMsg"),
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
                    this.scopeObject.deleteWorkCentre(obj);
                } else {
                    return;
                }

            }
        }, this);
    },
    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.fetchStatement();
        }
    },
    deleteWorkCentre: function(obj) {
        this.deleteUrl = "";
        var data = [];
        if (obj != undefined) {
            if (obj.isTempDelete) {
                this.deleteUrl = "ACCWorkCentreCMN/deleteWorkCentres.do";
            } else if (obj.isPermDelete) {
                this.deleteUrl = "ACCWorkCentreCMN/deleteWorkCentrePermanently.do";
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
            }, this, this.genDeleteSuccessResponse, this.genFailureResponse);

        }
        this.fetchStatement();
    },
    genDeleteSuccessResponse: function(response, request) {
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
    creategrid: function() {

        this.msgLmt = 30;

        this.Store = new Wtf.data.Store({
            url: "ACCWorkCentreCMN/getWorkCentreDataandColumnModel.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });


        this.sm = new Wtf.grid.CheckboxSelectionModel({
//            singleSelect: true
        });
        this.sm.on('selectionchange',this.enableDisableButtons,this);
        if (this.isReport) {
            this.grid = new Wtf.grid.GridPanel({
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
        } else {
            this.grid = new Wtf.grid.GridPanel({
                store: this.Store,
                columns: [],
                border: false,
                loadMask: true,
                sm: this.sm,
                viewConfig: {
                    forceFit: false,
                    emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
                },
                tbar: this.btnArr
            });
        }
        this.grid.on('cellclick',this.afteredit,this);
        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
        this.Store.on('load', this.handleStoreOnLoad, this);
        // this.grid.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);


    },
    addAdvanceSearchComponent: function () {
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.grid.colModel,
            moduleid: this.moduleid,
            advSearch: false,
            parentPanelSearch: this,
            ignoreDefaultFields: false,
            isAvoidRedundent: true
        });
        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
        this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    },
    afteredit: function(grid,rowIndex,columnIndex,e) {
        var record = grid.getStore().getAt(rowIndex);  // Get the Record
        var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
        var data = record.get(fieldName);
        var id = record.get("id");
        if(fieldName == "machinename") {
            callMachineMasterList(id,true);
        } else if (fieldName == "labourname") {
            callLabourDetails("","",id,true);
        } else if (fieldName == "productname") {
            callProductDetails("","",id,true);
//            callProductReport("","",true,id);
        }
    },
    handleDelete:function(del){
        var delFlag=del;
        if(this.grid.getSelectionModel().hasSelection()==false){
            WtfComMsgBox(34,2);
            return;
        }
        var data=[];
        var arr=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        
        for(i=0;i<this.recArr.length;i++){
                arr.push(this.recArr[i].data.id);
        }
        var data = {
            root:arr
        } 
        var json=Wtf.encode(data);
        
        var params = {
            jsonObj: json
        }
         this.ajxUrl = "ACCWorkOrder/deleteWorkOrders.do";
                Wtf.Ajax.requestEx({
                    url: this.ajxUrl,
                    params: params
                }, this, this.genSuccessResponse, this.genFailureResponse);
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
    showForm: function(isEdit, isClone) {
        var recArr = [];
        this.isEdit = isEdit;
        if (isEdit) {
            if (this.grid.getSelectionModel().getCount() < 1) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")], 0);
                return;
            }
            recArr = this.grid.getSelectionModel().getSelections();
            this.grid.getSelectionModel().clearSelections();

        }
        this.WorkcentreForm(isEdit, isClone, recArr,this);

    },
    WorkcentreForm: function(isEdit, isClone, recArr , scope) {
        var rec = isEdit ? recArr[0] : null;
        var tabid = isEdit ? recArr[0].data.productid : "productwin";
        if (isClone) {
            tabid = isEdit ? "clone" + recArr[0].data.productid : "cloneproductwin";
        }
       
        callWorkcentreWindow(isEdit, rec, isClone, scope);
       
    },
  
    handleStoreBeforeLoad: function() {
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.workcenterids =(this.wcid !== undefined || this.wcid !== '') ? this.wcid : '';
        this.Store.baseParams = currentBaseParams;
        
        this.exportButton.setParams({
            workcenterids: (this.wcid !== undefined || this.wcid !== '') ? this.wcid : ''
        });

    },
    handleStoreOnLoad: function(store) {
        var columns = [];
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect: true
        });
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));

        columns.push(this.sm);

        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if (column.dataIndex == "materialname" || column.dataIndex == "machinename" || column.dataIndex == "productname" || column.dataIndex == "labourname") {
                column.renderer = function(v,m,rec) {
                    return "<a href='#'>View Details</a>";
                };
            } else  {
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
        this.quickPanelSearch.StorageChanged(this.Store);
    },
    
    configurAdvancedSearch: function() {
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
            moduleid: this.moduleid,
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
            moduleid: this.moduleid,
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
    customizeView: function() {
        this.customizeViewWin = new Wtf.CustomizeReportView({
            title: WtfGlobal.getLocaleText("acc.field.CreateCustomizeView"),
            parentPanel: this,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            grid: this.grid,
            reportId: Wtf.MRP_Work_Centre_ModuleID,
            modules: "" + Wtf.MRP_Work_Centre_ModuleID
        });
        this.customizeViewWin.show();
    },
    
    Addimage: function() {
        var s = this.grid.getSelectionModel().getSelections();
        if (s.length == 1) {
            var productid = s[0].data.productid;
            var uploadImageWin = new Wtf.UploadImage({
                idX: this.id2,
                grid: this.grid,
                recid: productid,
                keyid: this.keyid,
                mapid: this.mapid,
                scope: this,
                moduleName: this.moduleName,
                selectedRec: this.selectedRec,
                isDetailPanel: false
            });
            uploadImageWin.show();
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.uploadImageWarnigMessage")]);
        }
    },
    enableDisableButtons: function () {
        var selectionModel = this.grid.getSelectionModel();
        if (selectionModel.hasSelection() && selectionModel.getCount() == 1) {
            if (this.editButton) {
                this.editButton.enable();
            }
            if (this.CloneWorkcentre) {
                this.CloneWorkcentre.enable();
            }
            if (this.deleteTrans) {
                this.deleteTrans.enable();
            }
            if (this.deleteTransperm) {
                this.deleteTransperm.enable();
            }   
        } else if(selectionModel.getCount() > 1) {
            if (this.editButton) {
                this.editButton.disable();
            }
            if (this.CloneWorkcentre) {
                this.CloneWorkcentre.disable();
            }
            if (this.deleteTrans) {
                this.deleteTrans.enable();
            }
            if (this.deleteTransperm) {
                this.deleteTransperm.enable();
            }

        }else if (selectionModel.getCount() == 0) {
            if (this.editButton) {
                this.editButton.disable();
            }
            if (this.CloneWorkcentre) {
                this.CloneWorkcentre.disable();
            }
            if (this.deleteTrans) {
                this.deleteTrans.disable();
            }
            if (this.deleteTransperm) {
                this.deleteTransperm.disable();
            }           
        }
                 
        var selectionArr =  this.grid.getSelectionModel().getSelections();
        /* If record is Temporary Deleted then Temporary Delete button, Edit button and Clone button are disable */
        for(var i=0;i<selectionArr.length;i++){
            if(selectionArr[i]&&selectionArr[i].data.deleted){
                if(this.deleteTrans){
                    this.deleteTrans.disable();
                }
                if(this.editButton){
                    this.editButton.disable();
                }
                if(this.CloneWorkcentre){
                    this.CloneWorkcentre.disable();
                }
                break;
            }
        }
        var idsArray = [];
        for (var i = 0; i <selectionArr.length; i++) {
            idsArray.push(selectionArr[i].data['id']);
        }
        this.exportselRec.setParams({
            selworkCenterIds : idsArray,
            totalWorkCenters:idsArray.length
        });
         
        if (selectionArr.length >= 1) {
            if (this.exportselRec)
                this.exportselRec.enable();
        } else {
            if (this.exportselRec)
                this.exportselRec.disable();
        }

    }

});
