/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.account.workOrderReport = function(config) {
    this.arr = [];
    this.moduleId = Wtf.MRP_Work_Order_ModuleID;
    this.isCloseWO = false;
    Wtf.apply(this, config);
    /*
     * Create Tool Bar Buttons
     */
    this.creatStores();
    this.createGrid();
    this.addAdvanceSearchComponent();
    this.createTBar();
    /*
     * Create Grid 
     */
    this.createPanel();
    Wtf.account.workOrderReport.superclass.constructor.call(this, config);
    this.addEvents({
        'journalentry':true
    });
}
Wtf.extend(Wtf.account.workOrderReport, Wtf.Panel, {
    closable:true,
    onRender: function(config) {
        /*
         * create panel to show grid
         */
        
        this.add(this.leadpan);
        /*
         * fetch data in report
         */
        this.fetchStatement();
        Wtf.account.workOrderReport.superclass.onRender.call(this, config);
    },
    creatStores:function(){
        this.workOrderStatusRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
            {name: 'defaultMasterItem'}
        ]);
        this.workOrderStatusStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 50
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.workOrderStatusRec)
        });
            this.workOrderStatusStore.on("load", function () {
            var record = new Wtf.data.Record({
                id: "",
                name: "All Records",
                defaultMasterItem: ""
            });
            this.workOrderStatusStore.insert(0, record);
            this.workOrderStatusFilterCombo.setValue("");
        }, this);
        this.workOrderStatusStore.load();
    },
    createPanel: function() {
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [
                this.objsearchComponent,
                {
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
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({
//                            id: "pPageSize_" + this.id
                        }),
                        items: this.bbarBtnArr
                    })
                }]
        });
    },
    createTBar: function() {
        this.btnArr = [];
        this.secondbtnArr = [];
        this.bbarBtnArr = [];
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("mrp.workorder.report.quicksearch"), // "Search by Work Order ID...",
            width: 200,
            id:"quicksearch"  +this.id,
            hidden: false
        });
        this.btnArr.push(this.quickPanelSearch);
        
        this.secondbtnArr.push(WtfGlobal.getLocaleText("acc.common.from"));
        this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), //'From',
            name: 'stdate' + this.id,
            format: WtfGlobal.getOnlyDateFormat(),
            value: WtfGlobal.getDates(true)
        })
        this.secondbtnArr.push(this.startDate);

        this.secondbtnArr.push(WtfGlobal.getLocaleText("acc.common.to"));
        this.endDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), //'To',
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'enddate' + this.id,
            value: WtfGlobal.getDates(false)
        })
        this.secondbtnArr.push(this.endDate);
        
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
        this.addWorkOrder = new Wtf.Action({
            text: WtfGlobal.getLocaleText("mrp.workorder.report.addworkorder"),
            id: "addWorkOrder",
            scope: this,
            hidden:this.hideCRUDButtons,
            tooltip: WtfGlobal.getLocaleText("mrp.workorder.report.addworkorder"),
            iconCls: getButtonIconCls(Wtf.etype.menuadd),
            handler: this.showForm.createDelegate(this, [false])
        });
        this.editWorkOrder= new Wtf.Action({
            text: WtfGlobal.getLocaleText("mrp.workorder.report.editworkorder"),
            id: 'editWorkOrder',
            scope: this,
            hidden:this.hideCRUDButtons,
            tooltip: WtfGlobal.getLocaleText("mrp.workorder.report.editworkorder"),
            disabled: true,
            iconCls: getButtonIconCls(Wtf.etype.menuedit),
            handler: this.showForm.createDelegate(this, [true])
        });
        this.deleteWorkOrder = new Wtf.Action({
            text: WtfGlobal.getLocaleText("mrp.workorder.report.deleteworkorder"),
            id: 'deleteWorkOrder',
            scope: this,
            tooltip: WtfGlobal.getLocaleText("mrp.workorder.report.deleteworkorder"),
            disabled: true,
            iconCls: getButtonIconCls(Wtf.etype.menudelete),
            handler: this.confirmDelete.createDelegate(this, [true, false])
        });
        this.closeWorkOrder = new Wtf.Action({
            text: WtfGlobal.getLocaleText("mrp.workorder.report.closeworkorder"),
            id: 'closeWorkOrder',
            scope: this,
            tooltip: WtfGlobal.getLocaleText("mrp.workorder.report.closeworkorder"),
            disabled: true,
            iconCls: getButtonIconCls(Wtf.etype.copy),
            handler: this.closeWO.createDelegate(this)
        });
        this.startWorkOrder = new Wtf.Action({
            text: WtfGlobal.getLocaleText("mrp.workorder.report.startworkorder"),
            id: 'startWorkOrder',
            scope: this,
            tooltip: WtfGlobal.getLocaleText("mrp.workorder.report.startworkorder"),
            disabled: true,
            iconCls: getButtonIconCls(Wtf.etype.copy),
            handler: this.confirmStart.createDelegate(this)
        });
        this.moveToInProcess = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.mrp.workorder.movetoinprocess"),
            id: 'moveToInProcess',
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.mrp.workorder.movetoinprocess"),
            disabled: true,
            iconCls: getButtonIconCls(Wtf.etype.copy),
            handler: this.confirmStart.createDelegate(this)
        });
        this.moveToBuilt = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.mrp.workorder.movetobuilt"),
            id: 'moveToBuilt',
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.mrp.workorder.movetobuilt"),
            disabled: true,
            iconCls: getButtonIconCls(Wtf.etype.copy),
            handler: this.confirmChangeStatus.createDelegate(this,['built'])
        });
        this.moveToRelease = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.mrp.workorder.movetorelease"),
            id: 'moveToRelease',
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.mrp.workorder.movetorelease"),
            disabled: true,
            iconCls: getButtonIconCls(Wtf.etype.copy),
            handler: this.confirmChangeStatus.createDelegate(this,['release'])
        });
        this.statusMassUpdateArr = [];
        this.statusMassUpdateArr.push(this.moveToInProcess);
        this.statusMassUpdateArr.push(this.moveToBuilt);
        this.statusMassUpdateArr.push(this.moveToRelease);
        this.deleteWorkOrderPermanently = new Wtf.Action({
            text: WtfGlobal.getLocaleText("mrp.workorder.report.deleteworkorderpermanently"),
            id: 'deleteWorkOrderpermanently',
            scope: this,
            tooltip: WtfGlobal.getLocaleText("mrp.workorder.report.deleteworkorderpermanently"),
            disabled: true,
            iconCls: getButtonIconCls(Wtf.etype.menudelete),
            handler: this.confirmDelete.createDelegate(this, [false, true])
        });
        this.statusMassUpdate = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.mrp.workorder.massupdateStatus"),
            id: 'statusMassUpdate',
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.mrp.workorder.massupdateStatus"),
            iconCls: getButtonIconCls(Wtf.etype.copy),
            menu:this.statusMassUpdateArr
        });
        /*Push buttons in manager work order  menubutton*/
        this.manageWorkOrdersArr = [];
       if (!WtfGlobal.EnableDisable(Wtf.UPerm.workorder, Wtf.Perm.workorder.startwo)) {
            this.manageWorkOrdersArr.push(this.startWorkOrder);
        }
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.workorder, Wtf.Perm.workorder.closewo)) {
            this.manageWorkOrdersArr.push(this.closeWorkOrder);
        }
        this.manageWorkOrdersArr.push(this.statusMassUpdate);
        this.manageWorkOrder = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("mrp.menubtn.workorder.manageworkorder.tilte"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("mrp.menubtn.workorder.manageworkorder.tilte"),
            iconCls: getButtonIconCls(Wtf.etype.inventorydst),
            menu: this.manageWorkOrdersArr
        });
        
        this.deleteMenuArr = [];
        /**
         * ERP-30622. Hide temporary delete until functionality is fully functional.
         */
//       if (!WtfGlobal.EnableDisable(Wtf.UPerm.workorder, Wtf.Perm.workorder.deletewo)) {
//            this.deleteMenuArr.push('-', this.deleteWorkOrder);
//        }
       if (!WtfGlobal.EnableDisable(Wtf.UPerm.workorder, Wtf.Perm.workorder.deletewo)) {
            this.deleteMenuArr.push('-', this.deleteWorkOrderPermanently);
        }
        this.deletebtnmenu = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("mrp.workorder.report.delete"),
            tooltip: WtfGlobal.getLocaleText("mrp.workorder.report.delete"),
            iconCls: getButtonIconCls(Wtf.etype.menudelete),
            menu: this.deleteMenuArr
        });
                
        if (!this.hideCRUDButtons) {
           if (!WtfGlobal.EnableDisable(Wtf.UPerm.workorder, Wtf.Perm.workorder.createwo)) {
                this.btnArr.push(this.addWorkOrder);
            }
           if (!WtfGlobal.EnableDisable(Wtf.UPerm.workorder, Wtf.Perm.workorder.modifywo)) {
                this.btnArr.push(this.editWorkOrder);
            }
            if (this.manageWorkOrdersArr.length > 0) {
                this.btnArr.push(this.manageWorkOrder);
            }
            if (this.deleteMenuArr.length > 0) {
                this.btnArr.push(this.deletebtnmenu);
            }
        }
        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), // "Advanced Search",
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), // 'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });
        this.btnArr.push('-', this.AdvanceSearchBtn);

        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch', 
            scope: this,
            handler: this.fetchStatement
        });
        this.secondbtnArr.push('-', this.fetchBttn);
        this.projPlanBttn = new Wtf.Toolbar.Button({
            text: "View Task Progress",//WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: "View Task Progress",//WtfGlobal.getLocaleText("acc.common.fetch"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            disabled: true,
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.viewProjectPlan
        });
        this.secondbtnArr.push('-', this.projPlanBttn);
        
          this.workOrderStatusFilterCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("mrp.workorder.entry.workorderstatus")+"*",
            name: 'workorderstatus',
            id:"workOrderStatus"+this.id,
            store:this.workOrderStatusStore,
            valueField:'id',
            displayField:'name',
            hidden:false,
            width : 180,
            ctCls:"fieldmargin",
            maxLength:50,
            scope:this,
            mode: 'remote',
            extraFields:[]
        });   
        this.workOrderStatusFilterCombo.on('change',this.fetchStatement,this);
        this.secondbtnArr.push("-", WtfGlobal.getLocaleText("acc.masterConfig.50"), this.workOrderStatusFilterCombo);
        
        this.customReportViewBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            handler: this.customizeView,
            iconCls: 'accountingbase fetch'
        });
        this.btnArr.push('->',this.customReportViewBtn);
//        
        var firsttbar = new Wtf.Toolbar(this.btnArr);
        var secondtbar = new Wtf.Toolbar(this.secondbtnArr);

        this.toolbarPanel = new Wtf.Panel({
            border: false,
            items: [firsttbar, secondtbar]
        });
//        
        this.exportButton = new Wtf.exportButton({
            obj: this,
            id: "exportReports" + this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), // 'Export report details',
            filename: "Work Order List" + "_v1",
            scope: this,
            menuItem: {
                csv: true,
                pdf: true,
                xls: true
            },
            get: 1105
        });
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.workorder, Wtf.Perm.workorder.exportwo)) {
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
        //Document Designer print button
        this.singleRowPrint = new Wtf.exportButton({
            obj:this,
            id:"printSingleRecord"+this.id,
            iconCls: 'pwnd printButtonIcon',
            text: WtfGlobal.getLocaleText("acc.rem.236"),
            tooltip :WtfGlobal.getLocaleText("acc.rem.236.single"),  //'Print Single Record Details',
            disabled :true,
            hidden:this.isSalesCommissionStmt,
            menuItem:{rowPrint:(this.isSalesCommissionStmt)?false:true},
            get:Wtf.autoNum.exportMRPWorkOrder,
            moduleid:this.moduleId
        });
        //put print button in bar
        this.bbarBtnArr.push('-', this.singleRowPrint);
        
        this.bbarBtnArr.push('-', this.importBtn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.common.import"),
            scope: this,
            hidden:true,
            tooltip: WtfGlobal.getLocaleText("acc.common.import"),
            iconCls: (Wtf.isChrome ? 'pwnd importChrome' : 'pwnd import')
//            menu: importBtnArray
        }));
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
         this.ajxUrl = "ACCWorkCentreCMN/deleteWorkCentres.do";
                Wtf.Ajax.requestEx({
                    url: this.ajxUrl,
                    params: params
                }, this, this.genSuccessResponse, this.genFailureResponse);
    },
    createGrid: function() {
        this.expandRec = Wtf.data.Record.create ([
        {name:'machineid'},
        {name:'machinename'},
        {name:'jobworkid'},
        {name:'jobworkcode'},
        {name:'labourid'},
        {name:'labourname'},
        {name:'workcentreid'},
        {name:'workcentrename'},
        {name:'id'},
        {name:'poname'},
        {name:'islabourOrmachine'}
    ]);
                this.expandStore = new Wtf.data.Store({
                url:"ACCWorkOrder/getWorkOrderExpanderDetails.do",
                        baseParams:{
                        },
                        reader: new Wtf.data.KwlJsonReader({
                        root: "data"
            },this.expandRec)
                });
        this.expandStore.on('load',this.fillExpanderBody,this);
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            });
        this.sm.on('selectionchange',this.enableDisableButtons,this);
        this.Store = new Wtf.data.Store({
            url: "ACCWorkOrder/getWorkOrderDataandColumnModel.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
       
        this.expander = new Wtf.grid.RowExpander({});
        this.expander.on("expand",this.onRowexpand,this);
        this.grid = new Wtf.grid.GridPanel({
            layout: 'fit',
            region: "center",
            store: this.Store,
            columns: [],
            border: false,
            loadMask: true,
            plugins:[this.expander],
            sm: this.sm,
            viewConfig: {
                forceFit: false,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
          this.grid.on('cellclick',this.handleCellClick,this);
          this.grid.flag = 0;
          this.grid.on('rowclick', Wtf.callGobalDocFunction, this);
        this.Store.on('load', this.handleStoreOnLoad, this);
        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
    },
    showworkorderstatusGraph: function(){
         workorderStatusGraphMRP(id);
    },
     handleCellClick: function(grid,rowIndex,columnIndex,e) {
        var record = grid.getStore().getAt(rowIndex);  // Get the Record
        var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
        var data = record.get(fieldName);
        var id = record.get("id");
        if(fieldName == "compavailablegraph") {
            componentAvailabiltyGraphMRP(id);
        } else if (fieldName == "wostatusgraph") {
            workorderTasksStatusGraphMRP(id);
        } else if(fieldName=="entryno"){
            var accid=this.Store.getAt(rowIndex).data['journalentryid'];
            if(accid!=undefined && accid!=""){
                this.fireEvent('journalentry',accid,true, this.consolidateFlag,null,null,null,this.startDate.getValue(),this.endDate.getValue());
            }
        }
    },
    handleStoreBeforeLoad: function () {
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        currentBaseParams.wostatus=this.workOrderStatusFilterCombo.getValue();
        this.Store.baseParams = currentBaseParams;
    },
    addAdvanceSearchComponent: function () {
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.grid.colModel,
            moduleid: this.moduleId,
            advSearch: false,
            parentPanelSearch: this,
            showBOMWithProduct:true,
            ignoreDefaultFields: false,
            isAvoidRedundent: true
        });

        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
        this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    },
    fetchStatement: function() {
        if(this.startDate.getValue()>this.endDate.getValue()){
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 3); // "From Date can not be greater than To Date."
             return;
        }
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value,
                ss: this.quickPanelSearch.getValue()
            }
        });
    },
    viewProjectPlan: function(){
//        var rows = this.sm.getSelections();
//        if(rows.length == 1){
//            var row = rows[0];
        //            var projectId = row.get('projectId');
        if (this.grid.getSelectionModel().hasSelection()) {
            this.record = this.grid.getSelectionModel().getSelected();
        }
        var projectId = "";
        var workorderId = "";
        if( this.record) {
            projectId = this.record.data.projectId;
            workorderId = this.record.data.workorderid;
        }
        var panel = Wtf.getCmp("woprojectplan-"+projectId);
        if(!panel){
                panel = new Wtf.Panel({
                    autoEl : {
                        tag : "iframe",
                        height:"100%",
                        src: Wtf.pmURL+"editableprojview.jsp?id="+projectId
                    },
                    id: "woprojectplan -"+projectId,
                    title: "Work Order - "+workorderId,                
                    tabTip: "Work Order - "+workorderId,
                    closable:true,
                    layout:'fit'
                }); 
                mainPanel.add(panel);
            }
            mainPanel.setActiveTab(panel);
//        }
    },
    onRowexpand: function(scope, record) {
        this.expandStore.load({
            params:{
                id:record.data.id
                }
            });
    },
    handleStoreOnLoad: function() {
    var columns = [];
        this.sm = new Wtf.grid.CheckboxSelectionModel({
//            singleSelect: true
        });
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));

        columns.push(this.sm);
        columns.push(this.expander);

        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if (column.dataIndex == "entryno") {
                column.renderer = WtfGlobal.linkDeletedRenderer
            }else{
                column.renderer = WtfGlobal.deletedRenderer;
            }
            columns.push(column);
        });
        
        columns.push(
        {
            header:WtfGlobal.getLocaleText("acc.invoiceList.attachDocuments"),  //"Attach Documents",
            dataIndex:'attachdoc',
            width:150,
            align:'center',
            hidden:(this.isSalesCommissionStmt) ? true : false ,
            renderer : function(val) {
                return "<div style='height:16px;width:16px;'><div class='pwndbar1 uploadDoc' style='cursor:pointer' wtf:qtitle='"
                + WtfGlobal
                .getLocaleText("acc.invoiceList.attachDocuments")
                + "' wtf:qtip='"
                + WtfGlobal
                .getLocaleText("acc.invoiceList.clickToAttachDocuments")
                +"'>&nbsp;</div></div>";
            }
        });
        columns.push(
        {
            header:WtfGlobal.getLocaleText("acc.invoiceList.attachments"),  //"Attachments",
            dataIndex:'attachment',
            width:150,
            hidden:(this.isSalesCommissionStmt) ? true : false ,
            renderer : Wtf.DownloadLink.createDelegate(this)
        });
        
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();

        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        this.quickPanelSearch.StorageChanged(this.Store);
    },
    handleResetClickNew: function (){
        this.startDate.setValue(WtfGlobal.getDates(true));
        this.endDate.setValue(WtfGlobal.getDates(false));
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.fetchStatement();
        }
    },
//    confirmClose: function() {
//        
//        this.closeWO();
//        Wtf.MessageBox.show({
//            title: WtfGlobal.getLocaleText("acc.common.confirm"),
//            msg: WtfGlobal.getLocaleText("mrp.workorder.report.closeworkorderconfirm") + '<br/> <br/> <input type="checkbox" id="createrc" style="position: relative; top: 3px;" /> <b>Create Routing Code </b> ',
//            buttons: Wtf.MessageBox.YESNO,
//            icon: Wtf.MessageBox.QUESTION,
//            width: 300,
//            scope: {
//                scopeObject: this
//            },
//            fn: function(btn) {
//                if (btn == "yes") {
//                    var createrc = document.getElementById('createrc').checked
//                    this.scopeObject.closeWO(createrc);
//                } else {
//                    return;
//                }
//            }
//        }, this);
//    },
    confirmChangeStatus: function(status) {  // Start WO function
        var params = [];
        if (status === "built") {
            params.push("In Process");
            params.push("Built");
        } else if (status === "release") {
            params.push("Built");
            params.push("Release");
        }
        params.push()
        
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"),
            msg: WtfGlobal.getLocaleText({key:"acc.mrp.workorder.massstatuschange.comfirm",params:params}) ,
            buttons: Wtf.MessageBox.YESNO,
            icon: Wtf.MessageBox.QUESTION,
            width: 300,
            scope: {
                scopeObject: this
            },
            fn: function(btn) {
                if (btn == "yes") {
                    this.scopeObject.changeStatus(status);
                } else {
                    return;
                }
            }
        }, this);
    },
    changeStatus: function(status) {  // Checking comp availabilty at backend
        var statusRec;
        if (status === "built") {
            statusRec = WtfGlobal.searchRecord(this.workOrderStatusStore, Wtf.WODefaultStatus.built, 'defaultMasterItem');
        } else if (status === "release") {
            statusRec = WtfGlobal.searchRecord(this.workOrderStatusStore, Wtf.WODefaultStatus.released, 'defaultMasterItem');
        }
//        this.createrc = createrc;
        var jsonarr = [];
        var recarr = this.grid.getSelections();
        
        for (var index = 0 ; index < recarr.length; index++) {
            var rec = recarr[index];
            var jsonObj = {};
            var status = statusRec.get("id");
            var id = rec.get("id");
            jsonObj.workorderstatus = status;
            jsonObj.id = id;
            jsonarr.push(jsonObj);
        }
        var obj = {};
        obj.data = jsonarr;
        this.grid.getSelectionModel().clearSelections();
        Wtf.Ajax.requestEx({
            url: "ACCWorkOrder/updateMassStatus.do",
            params: {
                data:JSON.stringify(obj),
                isEdit:true
            }
        }, this, this.genSuccessResponseStartWO, this.genFailureResponseStartWO);
    },
    confirmStart: function() {  // Start WO function

        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"),
            msg: WtfGlobal.getLocaleText("mrp.workorder.report.startworkorder.confirm") ,
            buttons: Wtf.MessageBox.YESNO,
            icon: Wtf.MessageBox.QUESTION,
            width: 300,
            scope: {
                scopeObject: this
            },
            fn: function(btn) {
                if (btn == "yes") {
                    this.scopeObject.startWO();
                } else {
                    return;
                }
            }
        }, this);
    },
    startWO: function(createrc) {  // Checking comp availabilty at backend
//        this.createrc = createrc;
        var jsonarr = [];
        var recarr = this.grid.getSelections();
        for (var index = 0 ; index < recarr.length; index++) {
            var rec = recarr[index];
            jsonarr.push(rec.get("id"));
        }
        this.grid.getSelectionModel().clearSelections();
        Wtf.Ajax.requestEx({
            url: "ACCWorkOrder/checkComponentAvailability.do",
            params: {
                woidArr: JSON.stringify(jsonarr)
            }
        }, this, this.genSuccessResponseStartWO, this.genFailureResponseStartWO);
    },
    closeWO: function() { //createrc
//        this.createrc = createrc;
        
        var rec = this.grid.getSelectionModel().getSelected();
        this.workorderid = "";
        var projectid = "";
        if (rec != undefined) {
            this.workorderid = rec.data.id;
            this.workorderwarehouse = rec.data.orderWarehouse;
            this.workorderlocation = rec.data.orderLocation;
            projectid = rec.data.projectId;
        }
        this.grid.getSelectionModel().clearSelections();     
        Wtf.Ajax.requestEx({
            url: "ACCWorkOrderCMN/sendWOCloseReq.do",
            params: {
                workorderid: this.workorderid,
                projectid: projectid
            }
        }, this, this.genSuccessResponseCloseWO, this.genFailureResponseCloseWO);
    },
    genSuccessResponseCloseWO: function(response) {
        if (response.success) {
            this.finalbuildQty=response.finalBuildQty;
//            this.finalblockdetails = response.finalblockdetails;
            this.isCloseWO = true;
            this.getWorkOrderProductRecord(this.isCloseWO);
            this.isCloseWO = false;
        } else {
            var msg ='';
            if (response) {
                if (response.msg != "") {
                    msg = response.msg;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg],2);
                    this.fetchStatement();
                }
            }
        }
    },
    genSuccessResponseStartWO: function(response) {
        if (response.success) {
                this.fetchStatement();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 0);
            }
    },
    genFailureResponseCloseWO: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.mrppm.failure");
        if (response) {
            if (response.msg != "") {
                msg = response.msg;
            }
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        }
    },
    genFailureResponseStartWO: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.mrp.failure");
        if (response) {
            if (response.msg != "") {
                msg = response.msg;
            }
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        }
    },
//    createRCWindowField: function() {
//        this.sequenceFormatStoreRec = new Wtf.data.Record.create([
//            {
//                name: 'id'
//            },
//            {
//                name: 'value'
//            },
//            {
//                name: 'oldflag'
//            }
//        ]);
//        this.sequenceFormatStore = new Wtf.data.Store({
//            reader: new Wtf.data.KwlJsonReader({
//                totalProperty: 'count',
//                root: "data"
//            }, this.sequenceFormatStoreRec),
//            url: "ACCCompanyPref/getSequenceFormatStore.do",
//            baseParams: {
//                mode: "autoroutecode",
//                isEdit: this.isEdit
//            }
//        });
//        this.sequenceFormatStore.load();
//        this.sequenceFormatStore.on('load', this.setNextNumber, this);
//        this.routeCode = new Wtf.form.TextField({
//            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.routingcode.Id"),
//            name: 'routecode',
//            id: "routecode" + this.id,
//            anchor: '75%',
//            maxLength: 50,
//            scope: this,
//            allowBlank: false,
//            width: 150
//        });
//        this.sequenceFormatCombobox = new Wtf.form.ComboBox({
//            triggerAction: 'all',
//            mode: 'local',
//            fieldLabel: WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
//            valueField: 'id',
//            displayField: 'value',
//            store: this.sequenceFormatStore,
//            disabled: (this.isEdit ? true : false),
//            anchor: '75%',
//            typeAhead: true,
//            forceSelection: true,
//            width: 150,
//            name: 'sequenceformat',
//            hiddenName: 'sequenceformat',
//            listeners: {
//                'select': {
//                    fn: this.getNextSequenceNumber,
//                    scope: this
//                }
//            }
//        });
//    },
//    getNextSequenceNumber: function(a, val) {
//        if (!(a.getValue() == "NA")) {
//            var rec = WtfGlobal.searchRecord(this.sequenceFormatStore, a.getValue(), 'id');
//            var oldflag = rec != null ? rec.get('oldflag') : true;
//            Wtf.Ajax.requestEx({
//                url: "ACCCompanyPref/getNextAutoNumber.do",
//                params: {
//                    from: Wtf.MRP_Route_Code_ModuleID,
//                    sequenceformat: a.getValue(),
//                    oldflag: oldflag
//                }
//            }, this, function(resp) {
//                if (resp.data == "NA") {
//                    this.routeCode.reset();
//                    this.routeCode.enable();
//                } else {
//                    this.routeCode.setValue(resp.data);
//                    this.routeCode.disable();
//                }
//            });
//        } else {
//            this.routeCode.reset();
//            this.routeCode.enable();
//        }
//    },
//    createRCWinBtn: function() {
//        this.saveRCBtn = new Wtf.Button({
//            text: WtfGlobal.getLocaleText("acc.mrp.routingcode.save"),
//            scope: this,
//            handler: this.saveRCForm
//        });
//        this.cancleBtn = new Wtf.Button({
//            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
//            scope: this,
//            handler: function() {
//                this.routingcodeWin.close();
//            }
//        });
//    },
    
//    saveRCForm: function() {
//        this.saveRCBtn.disable();
//        this.routingcodeform.getForm().submit({
//            scope: this,
//            params: {
//                workorderid: this.workorderid
//            },
//            success: function(result, action) {
//                this.saveRCBtn.enable();
//                var resultObj = eval('(' + action.response.responseText + ')');
//                if (resultObj.data.success) {
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), resultObj.data.msg], 0);
//                    this.routingcodeWin.close();
//                    this.getWorkOrderProductRecord();
//                    
//                } else {
//                    if (resultObj.data.msg)
//                        var msg = resultObj.data.msg;
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
//                }
//            },
//            failure: function(frm, action) {
//                this.saveRCBtn.enable();
//                var msg = WtfGlobal.getLocaleText("acc.common.msg1");
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
//            }
//        });
//    },
    
    getWorkOrderProductRecord: function(isCloseWO){
        this.productRec=Wtf.productRec;
        this.workOrderproductStore = new Wtf.data.Store({
            url: "ACCWorkOrderCMN/getWorkOrderProductDetail.do", //url: "ACCWorkOrder/getProductsForCombo.do",
            baseParams: {
                id: this.workorderid
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.productRec)
        });
        this.workOrderproductStore.on('load', function (){
            if(this.workOrderproductStore.getCount() > 0){
                var prorec = this.workOrderproductStore.getAt(0);
                if(prorec.data.isLocationForProduct || prorec.data.isWarehouseForProduct || prorec.data.isBatchForProduct || prorec.data.isSerialForProduct || prorec.data.isRowForProduct || prorec.data.isRackForProduct || prorec.data.isBinForProduct) 
                {
                    this.CallSerialnoDetailsWindow(prorec,this.workorderid,this.isEdit, isCloseWO);
                }else{
                    createRCFormAndWindow(this.workorderid,this.isEdit,false); // Call Routing window if Product Don't have batch serial
                }
            }
        }, this);
        this.workOrderproductStore.load();  
    },
    CallSerialnoDetailsWindow:function(record,workorderid,isEdit,isCloseWO){
       this.isCustomer=false;
//       var productid=response.data.productid;
//       var index=this.productComboStore.findBy(function(rec){
//            if(rec.data.productid==productid)
//                return true;
//            else
//                return false;
//        })
                 
        //        var firstRow=index;
        //        if(index== -1){
        //            index=this.store.findBy(function(rec){
        //                if(rec.data.productid==obj.data.productid)
        //                    return true;
        //                else
        //                    return false;
        //            })
        //        }
        //        var deliveredprodquantity = obj.data.dquantity;
        //        deliveredprodquantity = (deliveredprodquantity == "NaN" || deliveredprodquantity == undefined || deliveredprodquantity == null)?0:deliveredprodquantity;
        //
        //      if(deliveredprodquantity<=0){
        //            WtfComMsgBox(["Info","Quantity should be greater than zero. "], 2);
        //            return false;
        //        }
        //        if(index!=-1){ 
        //           
        //           var prorec=this.productComboStore.getAt(index);
        //            if(firstRow==-1){
        //                prorec=this.store.getAt(index);
        //            }
        //            if(prorec == undefined){
        //                prorec=obj;
        //            }
        //           
        //            if(prorec == undefined){
        var prorec = record;
        var quantity = 0;
        quantity=this.finalbuildQty;
          if(prorec.data.isLocationForProduct || prorec.data.isWarehouseForProduct || prorec.data.isBatchForProduct || prorec.data.isSerialForProduct || prorec.data.isRowForProduct || prorec.data.isRackForProduct || prorec.data.isBinForProduct) 
          {
                //            var clearbatchdetailsforCopy=true;
                //            
                //           if(this.copyTrans != undefined && this.copyTrans ==true && prorec.data.isWarehouseForProduct && prorec.data.isLocationForProduct && !prorec.data.isBatchForProduct && !prorec.data.isSerialForProduct && obj.data.isWarehouseLocationsetCopyCase!=undefined && obj.data.isWarehouseLocationsetCopyCase == true){
                //              clearbatchdetailsforCopy=false;
                //           }

                if (prorec.data.type != 'Service' && prorec.data.type != 'Non-Inventory Part') { // serial no for only inventory type of product
                    if(prorec.data.isSerialForProduct== true && prorec.data.isSerialForProduct != undefined) {
                        var v = quantity;
                        v = String(v);
                        var ps = v.split('.');
                        var sub = ps[1];
                        if (sub!=undefined && sub.length > 0) {
                            Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.msg.exceptionMsgForDecimalQty"));
        //                    obj.cancel=true;
                            return;
                        }
                    }
                }
                Wtf.totalQtyAddedInBuild=0;
                this.productidforRC=prorec.data.productid;
                this.batchDetailswin=new Wtf.account.AssemblySerialNoWindow({
                    renderTo: document.body,
                    title:WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber"),
                    productName:prorec.data.productname,
                    uomName:prorec.data.uomname,
                    currentQuantity:quantity,
                    totalquantity:quantity,
                    buildQuantity:quantity,
                    //quantity:obj.data.dquantity,
                    quantity:quantity,
                    isCloseWO:isCloseWO,
                    billid:"", //obj.data.billid,
                    defaultLocation: this.workorderlocation!= undefined ? this.workorderlocation : prorec.data.location,
                    productid:prorec.data.productid,
                    workorderid:workorderid,
                    isSales:this.isCustomer,
                    //            isLinkedFromSO:isLinkedFromSO,
                    //            isLinkedFromCI:isLinkedFromCI,
                    moduleid:this.moduleid,
                    moduleId:this.moduleId,
                    transactionid:(this.isCustomer)?4:5,
                    isDO:this.isCustomer?true:false,
                    documentid:"",            //(this.isEdit)?obj.data.rowid:
                    defaultWarehouse:this.workorderwarehouse != undefined ? this.workorderwarehouse :prorec.data.warehouse,
                    defaultAvailbaleQty:this.AvailableQuantity,
                    batchDetails:"",//  obj.data.batchdetails   in copy case clear the batchdetails  (this.copyTrans && clearbatchdetailsforCopy)?"":
                    warrantyperiod:prorec.data.warrantyperiod,
                    warrantyperiodsal:prorec.data.warrantyperiodsal,  
                    isLocationForProduct:prorec.data.isLocationForProduct,
                    isWarehouseForProduct:prorec.data.isWarehouseForProduct,
                    isRowForProduct:prorec.data.isRowForProduct,
                    isRackForProduct:prorec.data.isRackForProduct,
                    isBinForProduct:prorec.data.isBinForProduct,
                    isBatchForProduct:prorec.data.isBatchForProduct,
                    isSKUForProduct:prorec.data.isSKUForProduct,
                    isSerialForProduct:prorec.data.isSerialForProduct,
                    linkflag:false,//As their no batch details for PO So we Sending the Linking Flag false   isLinkFromPO?false:obj.data.linkflag,
                    isEdit:isEdit,
                    copyTrans:this.copyTrans,
                    readOnly:this.readOnly,
                    width:950,
                    autoScroll:true,
                    height:520,
                    resizable : false,
                    modal: true,
                    isWastageApplicable: prorec.data.isWastageApplicable,
                    parentObj:this,
                    isFinishGood:true
                });
                this.batchDetailswin.on("beforeclose",function(){
                    /*
                     * check newbatchjson is empty or not
                     */
                    var submit=this.batchDetailswin.submit;    
                    if (submit) {
                    var rec=null;
                    if(this.batchDetailswin.routingcodeform.getForm()){
                        rec=this.batchDetailswin.routingcodeform.getForm().getValues();
                        }
                    rec.fgproductbatchDetails=this.batchDetailswin.getBatchDetails();
                    rec.id=workorderid;
                    rec.workorderid=workorderid;
                    rec.quantity=this.finalbuildQty;
                    rec.productid=this.productidforRC;
    //                    rec.subProductAssemblyGridRec=this.batchDetailswin.SubProductAssemblyGrid.getAssemblyJson();
                    rec.assemblygridJson=this.batchDetailswin.SubProductAssemblyGrid.getFGSubProductBatchDetails();
                        Wtf.Ajax.requestEx({
                            url: "ACCWorkOrderCMN/updateInventoryForFinishedGood.do",
                            params: rec
                            }, this, function(response) {
                            var msg = '';
                            if (response.msg != "") {
                                msg = response.msg;
                            }
                            if (response.success) {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), msg], 3);
                                }else{
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg],2); 
                            }
                        });

                    }
                },this);
                this.batchDetailswin.show();
          }else{ //If Assembly Product don't have the Serial/Batch Details
                Wtf.Ajax.requestEx({
                url: "ACCWorkOrderCMN/updateWorkOrderConsumptionDetails.do",
                params: {
                    batchDetails: "",
                    id:workorderid

                }
            }, this, function(response) {
                var msg = '';  
                if (response.msg != "") {
                    msg = response.msg;
                }
                if (response.success) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), msg], 3);
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg],2); 
                }
            });
              
          }  
    },
    setNextNumber: function(config) {
        if (this.sequenceFormatStore.getCount() > 0) {
            var seqRec = this.sequenceFormatStore.getAt(0)
            this.sequenceFormatCombobox.setValue(seqRec.data.id);
            this.getNextSequenceNumber(this.sequenceFormatCombobox);
        }
    },
    confirmDelete: function(isTempDelete, isPermDelete) {
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"),
            msg: WtfGlobal.getLocaleText("acc.rem.146") + " Work Order(s)?",
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
                    this.scopeObject.deleteWO(obj);
                } else {
                    return;
                }

            }
        }, this);
    },
    deleteWO: function(obj) {
        this.deleteUrl = "";
        var data = [];
        if (obj != undefined) {
            if (obj.isTempDelete) {
                this.deleteUrl = "ACCWorkOrder/deleteWorkOrders.do";
            } else if (obj.isPermDelete) {
                this.deleteUrl = "ACCWorkOrder/deleteWorkOrderPermanently.do";
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
    genSuccessResponse: function (response, request) {
        if (response.success) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], 0);
    
        } else {
            var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            if (response.msg) {
                msg = response.msg;
            }
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        }
        this.fetchStatement();
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
            isFixedAsset: false,
            isLeaseFixedAsset: false,
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
         if (this.grid.getSelectionModel().hasSelection()) {
            this.record = this.grid.getSelectionModel().getSelected();

        }
        if(!isEdit){
            createProjectforMRP(Wtf.Project_TemplateId.WORKORDER,undefined, this);
        }else{
            
           var woStatus=this.record.data.workorderdefstatusid;
            if (woStatus === Wtf.WODefaultStatus.closed) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.mrp.wrnmsg.woclosed")], 3); 
                return;
            } else {
                callMRPWorkOrderWindow(null, isEdit, this.record, this, this.record.data.projectId);
            }
        }
//        var rec = null;
//        callMRPWorkOrderWindow("",false, rec);
    },
    handleResorceCost:function(){
        /*
         * call to Open Resource Cost tab
         */
        callResourceCost();
    },
    handleTaskAssign:function(){
        /*
         * call to Open Resource Analysis
         */
        callAssignTaskList();
    },
    
    
    customizeView: function () {
        this.customizeViewWin = new Wtf.CustomizeReportView({
            title: WtfGlobal.getLocaleText("acc.field.CreateCustomizeView"),
            parentPanel: this,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            grid: this.grid,
            reportId: this.moduleId,
            modules: ""+this.moduleId
        });
        this.customizeViewWin.show();
    },
    enableDisableButtons: function () {
        var selectionModel = this.grid.getSelectionModel();
        if (selectionModel.hasSelection() && selectionModel.getCount() == 1) {
            if (this.editWorkOrder) {
                this.editWorkOrder.enable();
            }
            if (this.startWorkOrder) {  //disabling start order button if it is already started
                var rec = selectionModel.getSelections()[0].data;
                if (rec.workorderdefstatusid == Wtf.WODefaultStatus.planned ) {
                    this.startWorkOrder.enable();
                } else {
                    this.startWorkOrder.disable();
                }
            }
            if (this.deleteWorkOrder) {
                this.deleteWorkOrder.enable();
            }
            if (this.projPlanBttn) {
                this.projPlanBttn.enable();
            }
            if (this.deleteWorkOrderPermanently) {
                this.deleteWorkOrderPermanently.enable();
            }
            if(this.closeWorkOrder){
                var rec = selectionModel.getSelections()[0].data;
                if(rec.workorderdefstatusid!=Wtf.WODefaultStatus.planned && rec.workorderdefstatusid!=Wtf.WODefaultStatus.closed ){
                    this.closeWorkOrder.enable();
                }
            }
            //enable print button after selection
            if (this.singleRowPrint) {
                this.singleRowPrint.enable();
            }
            
        } else if(selectionModel.getCount() > 1) {
            if (this.editWorkOrder) {
                this.editWorkOrder.disable();
            }
            if (this.deleteWorkOrder) {
                this.deleteWorkOrder.enable();
            }
            if (this.deleteWorkOrderPermanently) {
                this.deleteWorkOrderPermanently.enable();
            }
            if (this.closeWorkOrder) {
                this.closeWorkOrder.disable();
            }
            if (this.startWorkOrder) {
                this.startWorkOrder.disable();
            }
            if (this.projPlanBttn) {
                this.projPlanBttn.disable();
            }

        }else if (selectionModel.getCount() == 0) {
            if (this.editWorkOrder) {
                this.editWorkOrder.disable();
            }
            if (this.deleteWorkOrder) {
                this.deleteWorkOrder.disable();
            }
            if (this.deleteWorkOrderPermanently) {
                this.deleteWorkOrderPermanently.disable();
            }
            if (this.closeWorkOrder) {
                this.closeWorkOrder.disable();
            }
            if (this.startWorkOrder) {
                this.startWorkOrder.disable();
            }
            if (this.projPlanBttn) {
                this.projPlanBttn.disable();
            }
            //disable print button after selection
            if (this.singleRowPrint) {
                this.singleRowPrint.disable();
            }
        }
        this.enableDisableMassUpdateButtons()

    },
    enableDisableMassUpdateButtons: function () {
        var selectionModel = this.grid.getSelectionModel();
        if (selectionModel.hasSelection() && selectionModel.getCount() === 1) {
            var defstatusid = selectionModel.selections.items[0].get("workorderdefstatusid");
            if (defstatusid === Wtf.WODefaultStatus.planned) {
                if (this.moveToInProcess) {
                    this.moveToInProcess.enable();
                }
                if (this.moveToBuilt) {
                    this.moveToBuilt.disable();
                }
                if (this.moveToRelease) {
                    this.moveToRelease.disable();
                }
            } else if (defstatusid === Wtf.WODefaultStatus.inprocess) {
                if (this.moveToInProcess) {
                    this.moveToInProcess.disable();
                }
                if (this.moveToBuilt) {
                    this.moveToBuilt.enable();
                }
                if (this.moveToRelease) {
                    this.moveToRelease.disable();
                }
            } else if (defstatusid === Wtf.WODefaultStatus.built) { //enable moveToRelease option when WO current status is built
                if (this.moveToInProcess) {
                    this.moveToInProcess.disable();
                }
                if (this.moveToBuilt) {
                    this.moveToBuilt.disable();
                }
                if (this.moveToRelease) {
                    this.moveToRelease.enable();
                }
            } else {
                if (this.moveToInProcess) {
                    this.moveToInProcess.disable();
                }
                if (this.moveToBuilt) {
                    this.moveToBuilt.disable();
                }
                if (this.moveToRelease) {
                    this.moveToRelease.disable();
                }
            }
        } else if(selectionModel.getCount() > 1) {
            var defStatusID = "";
            var singleStatus = true;
            for (var index = 0 ; index<selectionModel.getCount(); index++) {
                if (defStatusID === "") {
                    defStatusID = selectionModel.selections.items[index].get("workorderdefstatusid");
                } else if (defStatusID != selectionModel.selections.items[index].get("workorderdefstatusid") ) {
                    singleStatus = false;
                    break;
                }
            }  
            if (singleStatus) {
                if (defStatusID === Wtf.WODefaultStatus.planned) {
                    if (this.moveToInProcess) {
                        this.moveToInProcess.enable();
                    }
                    if (this.moveToBuilt) {
                        this.moveToBuilt.disable();
                    }
                    if (this.moveToRelease) {
                        this.moveToRelease.disable();
                    }
                } else if (defStatusID === Wtf.WODefaultStatus.inprocess) {
                    if (this.moveToInProcess) {
                        this.moveToInProcess.disable();
                    }
                    if (this.moveToBuilt) {
                        this.moveToBuilt.enable();
                    }
                    if (this.moveToRelease) {
                        this.moveToRelease.disable();
                    }
                } else if (defStatusID === Wtf.WODefaultStatus.built) {
                    if (this.moveToInProcess) {
                        this.moveToInProcess.disable();
                    }
                    if (this.moveToBuilt) {
                        this.moveToBuilt.disable();
                    }
                    if (this.moveToRelease) {
                        this.moveToRelease.enable();
                    }
                } else {
                    if (this.moveToInProcess) {
                        this.moveToInProcess.disable();
                    }
                    if (this.moveToBuilt) {
                        this.moveToBuilt.disable();
                    }
                    if (this.moveToRelease) {
                        this.moveToRelease.disable();
                    }
                }
            } else {
                if (this.moveToInProcess) {
                    this.moveToInProcess.disable();
                }
                if (this.moveToBuilt) {
                    this.moveToBuilt.disable();
                }
                if (this.moveToRelease) {
                    this.moveToRelease.disable();
                }
            }
            
        } else if (selectionModel.getCount() === 0) {
            if (this.moveToInProcess) {
                this.moveToInProcess.disable();
            }
            if (this.moveToBuilt) {
                this.moveToBuilt.disable();
            }
            if (this.moveToRelease) {
                this.moveToRelease.disable();
            }
        }

    },
fillExpanderBody:function(){
    var disHtml = "";
    this.custArr = [];
    var header = "";    
    var labourheaderArr=[];
    var AccHeaderArray = "";
    var AccHeader = "";

    var widthInPercent = 0;
        var widthInPercent1 = 0;

    var prevBillid = "";
    var sameParent = false;
    
    for (var i = 0; i < this.expandStore.getCount(); i++) {
        var rec = this.expandStore.getAt(i);

        if (Wtf.isEmpty(header)) {
            labourheaderArr = this.getHeader();
            header=labourheaderArr[0];
            widthInPercent1=labourheaderArr[1];
        }
//        if (Wtf.isEmpty(AccHeaderArray)) {
            AccHeaderArray = this.getAccHeader(rec);
            AccHeader = AccHeaderArray[0];
            widthInPercent = AccHeaderArray[1];
//        }
        
            
           
        var currentBillid = rec.data['id'];
        if (prevBillid != currentBillid) {             // Check if last record also has same 'billid'.  
            prevBillid = currentBillid;
            sameParent = false;
            var AccHeaderData = "";
            var headerData = "";
            this.AccCount = 1;
            this.invCnt = 1;
        } else {
            sameParent = true;
        }

            if (rec.data['islabourOrmachine'] === 1) {
                AccHeaderData = this.getAccHeaderData(rec, sameParent, widthInPercent);
            } else {
                headerData = this.getHeaderData(rec, sameParent,widthInPercent1);
            }

        var moreIndex = this.grid.getStore().findBy(
            function(record, id) {
                if (record.get('id') === rec.data['id']) {
                    return true;  // a record with this data exists 
                }
                return false;  // there is no record in the store with this data
            }, this);
        if (moreIndex != -1) {
            var body = Wtf.DomQuery.selectNode('tr:nth(2) div.x-grid3-row-body', this.grid.getView().getRow(moreIndex));

            var disHtmlAccHeader = "<div class='expanderContainer' style='width:100%'>" + AccHeader + AccHeaderData + "</div>";
            var disHtml = "<div class='expanderContainer' style='width:100%'>" + header + headerData + "</div>";
            body.innerHTML = disHtml +disHtmlAccHeader ;
                
            if (this.expandButtonClicked) {
                this.expander.suspendEvents('expand');              //suspend 'expand' event of RowExpander only in case of ExpandAll.
                this.expander.expandRow(moreIndex);                // After data set to Grid Row, expand row forcefully.
            }
        }
    }
},
getHeader:function(){
        var arr = [];
        var lbourheaderArr=[];
        arr = ["Labour","Work Center"]
        var gridHeaderText = "Labour Details";
        var header = "<span class='gridHeader'>" + gridHeaderText + "</span>"; 
        header += "<span class='gridNo' style='font-weight:bold;'>" + WtfGlobal.getLocaleText("acc.cnList.Sno") + "</span>";    
        
        var count = 0;
         for (var custArrcount = 0; custArrcount < arr.length; custArrcount++) {
            if (arr[custArrcount] != "") {
                count++;
            }
        }
        
        var widthInPercent = 80 / count;
        var minWidth = count * 100 + 40;
        for (var j = 0; j < arr.length; j++) {
            header += "<span class='headerRow' style='width:" + widthInPercent + "% ! important;'>" + arr[j] + "</span>";
        }
        header += "<span class='gridLine'></span>";
        lbourheaderArr.push(header);
        lbourheaderArr.push(widthInPercent);
        return lbourheaderArr;
},
getAccHeader:function(rec){
        //code for account details
        var AccHeaderArray = [];
        var type = "";
        var AccArr = ["Machine","Work Center"];
       
        var count = 0;
        for (var custArrcount = 0; custArrcount < AccArr.length; custArrcount++) {
            if (AccArr[custArrcount] != "") {
                count++;
            }
        }
        var widthInPercent = 80 / count;
        var minWidth = count * 100 + 40;
        var AccGridHeaderText = "Machine Details";
        var AccHeader = "<span class='gridHeader'>" + AccGridHeaderText + "</span>";
        AccHeader += "<span class='gridNo' style='font-weight:bold;'>" + WtfGlobal.getLocaleText("acc.cnList.Sno") + "</span>";    //S.No.
        for (var j = 0; j < AccArr.length; j++) {
            AccHeader += "<span class='headerRow' style='width:" + widthInPercent + "% ! important;'>" + AccArr[j] + "</span>";
        }
        AccHeader += "<span class='gridLine'></span>";

        AccHeaderArray.push(AccHeader);
        AccHeaderArray.push(widthInPercent);
        return AccHeaderArray;
},
getAccHeaderData:function(accountDetailsRec,sameParent,widthInPercent){
        if (!sameParent || this.AccCount == 1) {
            this.AccHeader = "";
            this.AccCount = 1;
        }

        this.AccHeader += "<span class='gridNo' >" + (this.AccCount) + ".</span>";
        this.AccHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" +accountDetailsRec.data['machinename'] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec.data['machinename'], 20) + "</span>";
        this.AccHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" + accountDetailsRec.data['workcentrename'] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec.data['workcentrename'], 20) + "</span>";
       
        this.AccHeader += "<br>";
        this.AccCount++;
        return this.AccHeader;
},

getHeaderData:function(rec,sameParent,widthInPercent){
        if (!sameParent || this.invCnt == 1) {
            this.header = "";
            this.invCnt = 1;
        }
        this.header += "<span class='gridNo'>" + (this.invCnt) + ".</span>";
        this.header += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" + rec.data['labourname'] + "'>" +rec.data['labourname'] + "</span>";
        this.header += "<span class='gridRow'style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" + rec.data['workcentrename'] + "'>" + rec.data['workcentrename'] + "</span>";
       
        this.header += "<br>";
        this.invCnt++;
        return this.header;
}
});
