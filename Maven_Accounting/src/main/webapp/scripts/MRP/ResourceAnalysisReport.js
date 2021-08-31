/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

Wtf.account.ResourceAnalysisReport=function(config){
    Wtf.apply(this, config);
    
    /*
     * Create Grid 
     */
    this.createGrid();
    
    /*
     * Create Tool Bar Buttons
     */
    this.createTBar();

    Wtf.account.ResourceAnalysisReport.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.ResourceAnalysisReport,Wtf.Panel,{
    
    onRender: function(config){
        //    this.exportPermType = this.permType.exportlqt;
        //    this.uPermType = Wtf.UPerm.leaseorder;
        this.exportPermType = 1;
        this.uPermType = 1;
        /*
         * create panel to show grid
         */
        this.createPanel();
        this.add(this.reportpanel);
        this.fetchStatement();

        Wtf.account.ResourceAnalysisReport.superclass.onRender.call(this,config);
    },
    
    createGrid: function() {
        this.Store = new Wtf.data.Store({
            url: "ACCLabourCMN/getResourceAnalysisColumnModel.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: "totalCount",
                root: "data"
            })
        });
        this.Store.on('beforeload', function() {
            var currentBaseParams = this.Store.baseParams;
            currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            this.Store.baseParams = currentBaseParams;
            if(this.exportButton) {
                this.exportButton.enable();
            }
        }, this);
        WtfGlobal.setAjaxTimeOut();
        this.Store.on('load', this.handleStoreOnLoad, this);
        
        this.sm = new Wtf.grid.CheckboxSelectionModel({});
        this.grid = new Wtf.grid.GridPanel({
            id: "gridmsg",
            stripeRows: true,
            border: false,
            layout: 'fit',
            loadMask: true,
            forceFit: true,
            store: this.Store,
            viewConfig: {forceFit: true, emptyText: WtfGlobal.getLocaleText("acc.field.Norecordstodisplay.")},
            columns: []
        });
    },
    
    createTBar: function() {
        //=========Top TBar Button Array===========
        
        this.btnArr = [];
        this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), //'From',
            name: 'stdate' + this.id,
            format: WtfGlobal.getOnlyDateFormat(),
            value: WtfGlobal.getDates(true)
        });

        this.endDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), //'To',
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'enddate' + this.id,
            value: WtfGlobal.getDates(false)
        });

        this.applyFilter = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch'
            tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: 'accountingbase fetch',
            disabled: false,
            handler: this.fetchStatement
        });

        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), // 'Reset'
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), // 'Allows you to add a new search term by clearing existing search terms.'
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.resetBttn.on('click', this.handleResetClick, this);

        this.btnArr.push(this.startDate, this.endDate);
        this.btnArr.push(this.applyFilter, this.resetBttn, '-');

        var actualWorkMenus = [];
        var actualWorkMenu1 = new Wtf.Action({
            iconCls: 'pwnd exportpdf',
            text: ("<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.common.exportToXLSTT") + "'>" + WtfGlobal.getLocaleText("acc.common.exportToXLS") + "(Summary)</span>"),
            scope: this,
            handler: function() {
                alert("menu1 clicked");
            }
        });
        actualWorkMenus.push(actualWorkMenu1);

        var actualCostMenus = [];
        var actualCostMenu1 = new Wtf.Action({
            iconCls: 'pwnd exportpdf',
            text: ("<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.common.exportToXLSTT") + "'>" + WtfGlobal.getLocaleText("acc.common.exportToXLS") + "(Summary)</span>"),
            scope: this,
            handler: function() {
                alert("menu1 clicked");
            }
        });
        actualCostMenus.push(actualCostMenu1);

        var plannedCostMenus = [];
        var plannedCostMenu1 = new Wtf.Action({
            iconCls: 'pwnd exportpdf',
            text: ("<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.common.exportToXLSTT") + "'>" + WtfGlobal.getLocaleText("acc.common.exportToXLS") + "(Summary)</span>"),
            scope: this,
            handler: function() {
                alert("menu1 clicked");
            }
        });
        plannedCostMenus.push(plannedCostMenu1);

        var plannedLabourRateMenus = [];
        var plannedLabourRateMenu1 = new Wtf.Action({
            iconCls: 'pwnd exportpdf',
            text: ("<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.common.exportToXLSTT") + "'>" + WtfGlobal.getLocaleText("acc.common.exportToXLS") + "(Summary)</span>"),
            scope: this,
            handler: function() {
                alert("menu1 clicked");
            }
        });
        plannedLabourRateMenus.push(plannedLabourRateMenu1);

        this.actualWork = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.resourceanalysis.mainmenu.actualwork"), //Actual Work
            iconCls: (Wtf.isChrome ? 'pwnd exportChrome' : 'pwnd export'),
            menu: actualWorkMenus
        });
        this.actualCost = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.resourceanalysis.mainmenu.actualcost"), //Actual Cost
            iconCls: (Wtf.isChrome ? 'pwnd exportChrome' : 'pwnd export'),
            menu: actualCostMenus
        });
        this.plannedCost = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.resourceanalysis.mainmenu.plannedcost"), //Planned Cost
            iconCls: (Wtf.isChrome ? 'pwnd exportChrome' : 'pwnd export'),
            menu: plannedCostMenus
        });
        this.plannedLabourRate = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.resourceanalysis.mainmenu.plannedlabourrate"), //Planned Labour Rate
            iconCls: (Wtf.isChrome ? 'pwnd exportChrome' : 'pwnd export'),
            menu: plannedLabourRateMenus
        });

        this.btnArr.push(this.actualWork, '-', this.actualCost, '-', this.plannedCost, '-', this.plannedLabourRate, '-');

        //==========================================

        //=========Bottom TBar Button Array===========

        var resourceConflictMenus = [];
        var resourceConflictMenu1 = new Wtf.Action({
            iconCls: 'pwnd exportpdf',
            text: ("<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.common.exportToXLSTT") + "'>" + WtfGlobal.getLocaleText("acc.common.exportToXLS") + "(Summary)</span>"),
            scope: this,
            handler: function() {
                alert("menu1 clicked");
            }
        });
        resourceConflictMenus.push(resourceConflictMenu1);

        var resourceCostMenus = [];
        var resourceCostMenu1 = new Wtf.Action({
            iconCls: 'pwnd exportpdf',
            text: ("<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.common.exportToXLSTT") + "'>" + WtfGlobal.getLocaleText("acc.common.exportToXLS") + "(Summary)</span>"),
            scope: this,
            handler: function() {
                alert("menu1 clicked");
            }
        });
        resourceCostMenus.push(resourceCostMenu1);

        var assignTaskMenus = [];
        var assignTaskMenu1 = new Wtf.Action({
            iconCls: 'pwnd exportpdf',
            text: ("<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.common.exportToXLSTT") + "'>" + WtfGlobal.getLocaleText("acc.common.exportToXLS") + "(Summary)</span>"),
            scope: this,
            handler: function() {
                alert("menu1 clicked");
            }
        });
        assignTaskMenus.push(assignTaskMenu1);

        this.resourceConflict = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.resourceanalysis.mainmenu.resourceconflict"), //Resource Conflict
            iconCls: (Wtf.isChrome ? 'pwnd exportChrome' : 'pwnd export'),
            menu: resourceConflictMenus
        });
        this.resourceCost = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.resourceanalysis.mainmenu.resourcecost"), //Resource Cost
            iconCls: (Wtf.isChrome ? 'pwnd exportChrome' : 'pwnd export'),
            menu: resourceCostMenus
        });
        this.assignTask = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.resourceanalysis.mainmenu.assigntask"), //Assign Task
            iconCls: (Wtf.isChrome ? 'pwnd exportChrome' : 'pwnd export'),
            menu: assignTaskMenus
        });

        this.bottomBtnArr = [];
//        this.bottomBtnArr.push('-', this.resourceConflict, '-', this.resourceCost, '-', this.assignTask);

        var tranType = 1;
        if (!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)) {
            this.exportButton = new Wtf.exportButton({
                obj: this,
                isEntrylevel: false,
                id: "exportReports" + config.helpmodeid + this.id, //+config.id,   //added this.id to avoid dislocation of Export button option i.e. CSV & PDF
                text: WtfGlobal.getLocaleText("acc.common.export"),
                tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
                disabled: true,
                filename: this.filename,
                excludeCustomHeaders: this.moduleId == 2 ? true : false,
                moduleId: this.moduleId,
//            menuItem:{csv:true,pdf:true,rowPdf:(this.isSalesCommissionStmt)?false:true,rowPdfTitle:WtfGlobal.getLocaleText("acc.rem.39") + " "+ singlePDFtext},
                menuItem: {
                    csv: true,
                    pdf: true,
                    xls: true
                },
                get: tranType
            });
            this.bottomBtnArr.push(this.exportButton);
        }

        this.graphViewBttn = new Wtf.Toolbar.Button({
            text: "Graph View",
            tooltip: "Click here to view Graph",
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false,
            handler: function() {
                alert("view graph clicked");
            }
        });
        this.bottomBtnArr.push(this.graphViewBttn);

        //==========================================
    
        //=========Paging TBar===========
        
        this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 15,
            id: "pagingtoolbar" + this.id,
            store: this.Store,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), // "No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
                id: "pPageSize_" + this.id
            }),
            items: this.bottomBtnArr
        });
        
        //==========================================
    },
    
    createPanel: function() {
        this.reportpanel = new Wtf.Panel({
            border: false,
            layout: "border",
            items: [{
                    region : 'center',
                    layout : 'fit',
                    border : false,
                    tbar   : this.btnArr,
                    items  : [this.grid],
                    bbar   : this.pagingToolbar
                }]
        });
    },
    
    fetchStatement: function() {
        this.Store.load({
            params:{
                start:0,
                limit:30
            }
        });
    },
    
    handleStoreOnLoad: function(store) {
        var columns = [];
        columns.push(this.sm);
        var scope =this;
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
    
    handleResetClick:function() {
        this.startDate.reset();
        this.endDate.reset();
        this.Store.load();
    }
});