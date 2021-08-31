/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.jobOrderReport = function (config) {
    Wtf.apply(this, config);
    this.isReport = config.isReport ? config.isReport : false;
    this.arr = [];
    this.moduleId = Wtf.MRP_Job_Work_ModuleID;
    this.creategrid();
    this.addAdvanceSearchComponent();
    this.createTBar();
    this.addEvents({
        'updatejobworkreport': true
    });

    this.on('updatejobworkreport',  this.fetchStatement, this);
    Wtf.jobOrderReport.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.jobOrderReport, Wtf.Panel, {
    onRender: function (config) {
        Wtf.jobOrderReport.superclass.onRender.call(this, config);

        this.createPanel();

        this.add(this.jobOrderpanel);
        this.fetchStatement();
    },
    createPanel: function () {
        this.jobOrderpanel = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [this.objsearchComponent, {
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid],
                    tbar: this.buttonsArr,
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
    createTBar: function () {
        this.buttonsArr = [];
        this.bottomToolbar = [];
        this.quickPanelSearchh = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.rem.5") + " Job Work Number, Job Work name",
            field: 'jobordernumber',
            width: 200,
            Store: this.Store
        })
        this.buttonsArr.push(this.quickPanelSearchh);

        this.buttonsArr.push(WtfGlobal.getLocaleText("acc.common.from"));
        this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), //'From',
            name: 'stdate' + this.id,
            format: WtfGlobal.getOnlyDateFormat(),
            value: WtfGlobal.getDates(true)
        })
        this.buttonsArr.push(this.startDate);

        this.buttonsArr.push(WtfGlobal.getLocaleText("acc.common.to"));
        this.endDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), //'To',
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'enddate' + this.id,
            value: WtfGlobal.getDates(false)
        })
        this.buttonsArr.push(this.endDate);
        
         this.startDate.on('change',function(field,newval,oldval){
            if(field.getValue()!='' && this.endDate.getValue()!=''){
                if(field.getValue().getTime()>this.endDate.getValue().getTime()){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                    field.setValue(oldval);                    
                }
            }
        },this);
        
        this.endDate.on('change',function(field,newval,oldval){
            if(field.getValue()!='' && this.startDate.getValue()!=''){
                if(field.getValue().getTime()<this.startDate.getValue().getTime()){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.ToDateshouldnotbelessthanFromDate")], 2);
                    field.setValue(oldval);
                }
            }
        },this);

        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
        this.buttonsArr.push(this.fetchBttn);

        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            //hidden:this.isSalesCommissionStmt,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.resetBttn.on('click', this.handleResetClick, this);
        this.buttonsArr.push('-',this.resetBttn);


        if (!this.isReport) {
            this.manageBtnArr = [];
            this.createNew = new Wtf.Action({
                text: WtfGlobal.getLocaleText("Add"),
                scope: this,
                tooltip: WtfGlobal.getLocaleText("acc.mrp.field.AddWorkCentre"),
                iconCls: getButtonIconCls(Wtf.etype.add),
                handler: this.showForm.createDelegate(this, [false])
            })

            this.manageBtnArr.push(this.createNew);

            this.editButton = new Wtf.Action({
                text: WtfGlobal.getLocaleText("Edit"), //'Edit',
                scope: this,
                iconCls: getButtonIconCls(Wtf.etype.edit),
                tooltip: WtfGlobal.getLocaleText("acc.mrp.field.EditWorkCentre"),
                disabled: true,
                handler: this.showForm.createDelegate(this, [true])
            });
            this.manageBtnArr.push(this.editButton);

            this.deleteTrans = new Wtf.Action({
                text: WtfGlobal.getLocaleText("Delete"),
                scope: this,
                tooltip: WtfGlobal.getLocaleText("acc.mrp.field.DeleteWorkcentre"),
                iconCls: getButtonIconCls(Wtf.etype.menudelete),
                disabled: true,
                handler: this.confirmDelete.createDelegate(this, [true, false])
            });
            this.manageBtnArr.push(this.deleteTrans);

            this.deleteTransperm = new Wtf.Action({
                text: WtfGlobal.getLocaleText("Delete Permanently"),
                scope: this,
                tooltip: WtfGlobal.getLocaleText("acc.mrp.field.DeleteWorkcentrePermanently"),
                iconCls: getButtonIconCls(Wtf.etype.menudelete),
                disabled: true,
                handler: this.confirmDelete.createDelegate(this, [false, true])
            });

            this.manageBtnArr.push('-',this.deleteTransperm);

            this.manageJobWork = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.mrp.jobwork.actions"),
                scope: this,
                tooltip: WtfGlobal.getLocaleText("acc.mrp.jobwork.actions"),
                iconCls: getButtonIconCls(Wtf.etype.jobwork),
                menu: this.manageBtnArr
            })
            this.buttonsArr.push('-',this.manageJobWork);


        }


        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        })
        this.buttonsArr.push('-',this.AdvanceSearchBtn);

        this.customReportViewBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            handler: this.customizeView,
            iconCls: 'accountingbase fetch'
        });
        this.buttonsArr.push('-',this.customReportViewBtn);

        this.expButton = new Wtf.exportButton({
            obj: this,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.agedPay.exportTT"), //'Export report details',
            filename: WtfGlobal.getLocaleText("mrp.jobwork.export.filename.title"),
            params: {stdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                ss: this.quickPanelSearchh.getValue() != undefined ? this.quickPanelSearchh.getValue() : ''
            },
            menuItem: {csv: true, pdf: true, rowPdf: false, xls: true},
            get: Wtf.autoNum.exporMRPJobWork
        });
        this.bottomToolbar.push(this.expButton)

    },
    unitRenderer:function(value,metadata,record){
        if(record.data['type'] == "Service"){
        	return "N/A";
        }
    	var unit=record.data['uomname'];
            value=parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+" "+unit;
       if(record.data.deleted) {
                value='<del>'+value+'</del>';    
            }         
        return value;
    },
    customizeView: function () {
        this.customizeViewWin = new Wtf.CustomizeReportView({
            title: WtfGlobal.getLocaleText("acc.field.CreateCustomizeView"),
            parentPanel: this,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            grid: this.grid,
            reportId: Wtf.MRP_Job_Work_ModuleID,
            modules: "" + Wtf.MRP_Job_Work_ModuleID
        });
        this.customizeViewWin.show();
    },
    confirmDelete: function (isTempDelete, isPermDelete) {
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"),
            msg: WtfGlobal.getLocaleText("acc.rem.146") + " " + WtfGlobal.getLocaleText("acc.machineMaster.deleteMsgJob"),
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

        if (this.grid.getSelectionModel().hasSelection() === false) {
            WtfComMsgBox(34, 2);
            return;
        }
        
        var arr = [];
        this.recArr = this.grid.getSelectionModel().getSelections();

        for (i = 0; i < this.recArr.length; i++) {
            arr.push(this.recArr[i].data.id);
        }
        var data = {
            root: arr
        }
        var json = Wtf.encode(data);

        var params = {
            jsonObj: json,
            isTempDelete: obj.isTempDelete,
            isPermDelete: obj.isPermDelete
        }
        this.ajxUrl = "ACCJobWorkController/deleteJobWorkOrders.do";
        Wtf.Ajax.requestEx({
            url: this.ajxUrl,
            params: params
        }, this, this.genSuccessResponse, this.genFailureResponse);
    },
    genSuccessResponse: function (response, request) {
        if (response.success) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("mrp.jobwork.delete.success.msg.title")], 0);
            this.fetchStatement();
        }
    },
    genFailureResponse: function (response) {
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if (response.msg){
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    creategrid: function () {

        this.Store = new Wtf.data.Store({
            url: "ACCJobWorkController/getJobWorks.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });


        this.sm = new Wtf.grid.CheckboxSelectionModel({
//            singleSelect: true
        });

        this.sm.on('selectionchange', this.enableDisableButtons, this);

        this.grid = new Wtf.grid.GridPanel({
            store: this.Store,
            columns: [],
            border: false,
            stripeRows :true,
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
    addAdvanceSearchComponent: function () {
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.grid.colModel,
            moduleid: Wtf.MRP_Job_Work_ModuleID,
            advSearch: false,
            parentPanelSearch: this,
            ignoreDefaultFields: false,
            isAvoidRedundent: true
        });

        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
        this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);


    },
    fetchStatement: function () {
      
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value
            }
        });
    },
    enableDisableButtons: function () {
        var selectionModel = this.grid.getSelectionModel();
        if (selectionModel.hasSelection() && selectionModel.getCount() == 1) {
            if (this.editButton) {
                this.editButton.enable();
            }
            if (this.deleteTrans) {
                this.deleteTrans.enable();
            }
            if (this.deleteTransperm) {
                this.deleteTransperm.enable();
            }
        } else if (selectionModel.getCount() > 1) {
            if (this.editButton) {
                this.editButton.disable();
            }
            if (this.deleteTrans) {
                this.deleteTrans.enable();
            }
            if (this.deleteTransperm) {
                this.deleteTransperm.enable();
            }

        } else if (selectionModel.getCount() === 0) {
            if (this.editButton) {
                this.editButton.disable();
            }
            if (this.deleteTrans) {
                this.deleteTrans.disable();
            }
            if (this.deleteTransperm) {
                this.deleteTransperm.disable();
            }
        }
        
        var selectionArr =  this.grid.getSelectionModel().getSelections();
        /* If record is Temporary Deleted then Temporary Delete button and Edit button are disable */
        for(var i=0;i<selectionArr.length;i++){
            if(selectionArr[i]&&selectionArr[i].data.deleted){
                if(this.deleteTrans){
                    this.deleteTrans.disable();
                }
                if(this.editButton){
                    this.editButton.disable();
                }
                break;
            }
        }
    },
    showForm: function (isEdit, isClone) {
        this.record={};
        var winid=undefined;
        if (isEdit) {
            if (this.grid.getSelectionModel().hasSelection()) {
                this.record = this.grid.getSelectionModel().getSelected();
            }
            winid = 'edit_' + this.record.get("id");
        }
        calllJobWorkEntryMasterForm(winid, isEdit, this.record);

    },
//   
    handleStoreBeforeLoad: function () {
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        this.Store.baseParams = currentBaseParams;

    },
    handleStoreOnLoad: function () {
        var columns = [];
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect: true
        });
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));

        columns.push(this.sm);

        Wtf.each(this.Store.reader.jsonData.columns, function (column) {
            if(column.dataIndex == "excisedutychargees"){
                column.renderer=WtfGlobal.currencyRenderer;
            }else if(column.dataIndex == "quantity"){
                column.renderer = this.unitRenderer;
            }else{
            column.renderer = WtfGlobal.deletedRenderer;
        }
            columns.push(column);
        },this);
//        Wtf.each(this.Store.reader.jsonData.columns, function (column) {
//
//            columns.push(column);
//        });
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();

        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
    },
    handleResetClick: function () {
        this.startDate.setValue(WtfGlobal.getDates(true));
        this.endDate.setValue(WtfGlobal.getDates(false));
        if (this.quickPanelSearchh.getValue()) {
            this.quickPanelSearchh.reset();
        } 
        this.fetchStatement();

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
            moduleid: Wtf.MRP_Job_Work_ModuleID,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.Store.load({
            params: {
                ss: this.quickPanelSearchh.getValue(),
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
            moduleid: Wtf.MRP_Job_Work_ModuleID,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.Store.load({
            params: {
                ss: this.quickPanelSearchh.getValue(),
                start: 0,
                limit: this.pP.combo.value
            }
        });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();


    }
});
