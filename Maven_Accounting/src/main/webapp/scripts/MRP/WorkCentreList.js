/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.workCentreList = function(config) {
    Wtf.apply(this, config);
    this.isReport = config.isReport ? config.isReport : false;
         this.creategrid();
        this.createTBar();
        
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: 4,
        advSearch: false
    });

    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
        
    Wtf.workCentreList.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.workCentreList, Wtf.Panel, {
    onRender: function(config) {
        Wtf.workCentreList.superclass.onRender.call(this, config);

        this.createPanel();
         
        this.add(this.workCentreListPanel);
        this.fetchStatement();
    },
    
    createPanel: function() {
        this.workCentreListPanel = new Wtf.Panel({
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
                        searchField: this.quickPanelSearchh,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id}),
                         items: [this.bottomToolbar]
                    })
                }]
        });
    },
    
    createTBar: function() {
        this.panelbtnarr = [];
        this.bottomToolbar=[];

        this.panelbtnarr.push(this.quickPanelSearchh = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("Quick Search"),
            field: 'bandName'
        }));


        this.bottomToolbar.push(this.exportButton = new Wtf.exportButton({
            obj: this,
            id: 'productlistexport',
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"),
            params: {name: WtfGlobal.getLocaleText("acc.prod.filename")},
            menuItem: {csv: true, pdf: true, rowPdf: false, xls: true},
            get: 198,
            isProductExport: true,
            filename: WtfGlobal.getLocaleText("acc.prod.filename") + "_v1",
            label: WtfGlobal.getLocaleText("acc.field.ProductList1")
        })),
        this.panelbtnarr.push(WtfGlobal.getLocaleText("acc.common.from"));
        this.panelbtnarr.push(this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), //'From',
            name: 'stdate' + this.id,
            format: WtfGlobal.getOnlyDateFormat(),
            value: WtfGlobal.getDates(true)
        }));

        this.panelbtnarr.push(WtfGlobal.getLocaleText("acc.common.to"));

        this.panelbtnarr.push(this.endDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), //'To',
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'enddate' + this.id,
            value: WtfGlobal.getDates(false)
        }));

        this.panelbtnarr.push(this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            //hidden:this.isSalesCommissionStmt,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        }));
        this.resetBttn.on('click', this.handleResetClick, this);

        if(!this.isReport){
            
            this.panelbtnarr.push(this.createNew = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("Add"),
                scope: this,
                tooltip: WtfGlobal.getLocaleText("acc.mrp.field.AddWorkCentre"),
                iconCls: getButtonIconCls(Wtf.etype.add),
                handler: this.showForm.createDelegate(this, [false])
            }));
            
            this.panelbtnarr.push(this.editButton = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("Edit"), //'Edit',
                scope: this,
                iconCls: getButtonIconCls(Wtf.etype.edit),
                tooltip: WtfGlobal.getLocaleText("acc.mrp.field.EditWorkCentre"),
                // disabled:true,
                handler: this.showForm.createDelegate(this, [true])
            }));
        }



        var deletebtnArray = [];

        deletebtnArray.push(this.deleteTrans = new Wtf.Action({
            text: WtfGlobal.getLocaleText("Delete"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.mrp.field.DeleteWorkcentre"),
            iconCls: getButtonIconCls(Wtf.etype.menudelete),
            disabled: true,
            handler: this.confirmDelete.createDelegate(this, [true, false]) 
        }))


        deletebtnArray.push(this.deleteTransperm = new Wtf.Action({
            text: WtfGlobal.getLocaleText("Delete Permanently"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.mrp.field.DeleteWorkcentrePermanently"),
            iconCls: getButtonIconCls(Wtf.etype.menudelete),
            disabled: true
            //handler: this.deleteTransactionCheckBefore.createDelegate(this, this.del = ["delp"])
        }))

        if (deletebtnArray.length > 0) {
            if(!this.isReport){
                
                this.panelbtnarr.push(this.deleteMenu = new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),
                    scope: this,
                    tooltip: WtfGlobal.getLocaleText("acc.field.allowsyoutodeletetherecord"),
                    iconCls: getButtonIconCls(Wtf.etype.deletebutton),
                    menu: deletebtnArray
                }));
            }
        }
        
        this.panelbtnarr.push(this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        }));
        
        this.panelbtnarr.push(this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        }));

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
    
 
    creategrid: function() {
      
        this.Store = new Wtf.data.Store({
            url: "ACCInvoiceCMN/getColumnModelForWorkCentreList.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });


        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect: true
        });

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

        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
        this.Store.on('load', this.handleStoreOnLoad, this);
        // this.grid.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);

    


    },
    
    fetchStatement: function() {
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value
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
        this.WorkcentreForm(isEdit, isClone, recArr);

    },
    WorkcentreForm: function(isEdit, isClone, recArr) {
        var rec = isEdit ? recArr[0] : null;
        var tabid = isEdit ? recArr[0].data.productid : "productwin";
        if (isClone) {
            tabid = isEdit ? "clone" + recArr[0].data.productid : "cloneproductwin";
        }
       
        callWorkcentreWindow(isEdit, rec, isClone);
       
    },
  
    handleStoreBeforeLoad: function() {
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startdate = "";
        currentBaseParams.enddate = "";
        currentBaseParams.salesPersonID = ""
        this.Store.baseParams = currentBaseParams;

    },
    handleStoreOnLoad: function() {
        var columns = [];
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect: true
        });
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));

        columns.push(this.sm);

        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            
            columns.push(column);
        });
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();

        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
    },
    
    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.Store.on('load', this.storeloaded, this);
        } else {
            if (this.isRequisition || this.isRFQ) {
                this.startDate.setValue(WtfGlobal.getDates(true));
                this.endDate.setValue(WtfGlobal.getDates(false));
                this.loadStore();
            }
            else if (this.isSalesCommissionStmt) {
                this.startDate.setValue(WtfGlobal.getDates(true));
                this.endDate.setValue(WtfGlobal.getDates(false));
               
            }
        }
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
            searchJson: this.searchJson,
            moduleid: this.moduleId,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        //this.Store.load({params: {ss: this.quickSearchTF.getValue(), start: 0, limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt}});
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: 3,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        //this.Store.load({params: {ss: this.quickSearchTF.getValue(), start: 0, limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt}});
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();


    },

});
