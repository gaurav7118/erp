Ext.Loader.setConfig({
    enabled: true
});
function _dC(n) {
    document.cookie = n + "=" + ";path=/;expires=Thu, 01-Jan-1970 00:00:01 GMT";
}

function _r(url) {
    window.top.location.href = url;
}
function signOut(type) {

    Ext.DomainPatt = /[ab]\/([^\/]*)\/(.*)/;
    var _out = "";
    if (type !== undefined && typeof type != "object")
        _out = "?type=" + type;
    _dC('lastlogin');
    _dC('featureaccess');
    _dC('username');
    _dC('lid');
    _dC('companyid');
    var m = Ext.DomainPatt.exec(window.location);
    var _u = '../../error.do';
    if (type == "noaccess" || type == "alreadyloggedin") {
        _u += '?e=' + type;
        if (m && m[1]) {
            _u += '&n=' + m[1];
        }
    }
    else {
        if (m && m[1]) {
            _u = '../../b/' + m[1] + '/signOut.do' + _out;
        }
    }
    _r(_u);

}
// Handle session timeout on completion of ajax request
Ext.Ajax.on({
    requestcomplete: function(conn, response, options) {
        if (response != null && response.responseText != null) {
            var resObj = eval("(" + response.responseText + ")");
            if (resObj.msg == "timeout" && (resObj.success == false || resObj.success == "false")) {
                signOut("timeout");
            }
        }
    },
    requestexception: function(conn, response, options, e) {
        Ext.CustomMsg('Error', ExtGlobal.getLocaleText("acc.common.errorOccuratServerSide"), Ext.Msg.ERROR);
    }
});

var recordsToDrop = [];
var reportPreviewPageSize = 10;
var financialYearFromDate;
var financialYearToDate;
Ext.define('Ext.data.ModuleCategoryStore', {
    extend: 'Ext.data.Store',
    id: 'accModuleCategoryStoreId',
    fields: ['moduleCatId', 'moduleCatName'],
    //autoLoad: true,
    proxy: {
        type: 'ajax',
        url: 'ACCCreateCustomReport/getModulesCategories.do',
        actionMethods : getStoreActionMethods(),
        reader: {
            type: 'json',
            rootProperty: "data"
        }
    }
});
Ext.define('Ext.data.ModuleStore', {
    extend: 'Ext.data.Store',
    fields: ['id', 'value']
});

Ext.define('customReportStoreDataModel', {
    extend: 'Ext.data.Model',
    fields: [
    ]
});
var moduleStore = Ext.create('Ext.data.ModuleStore');
// defining a model
Ext.define('ModuleModel', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'string'
        },
        {
            name: 'modulename',
            type: 'string'
        }
    ]
});
// defining a model for field
Ext.define('FieldsModel', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id'
        },
        {
            name: 'mainTable'
        },
        {
            name: 'defaultHeader'
        },
        {
            name: 'dbcolumnname'
        },
        {
            name: 'reftablename'
        },
        {
            name: 'reftablefk'
        },
        {
            name: 'reftabledatacolumn'
        },
        {
            name: 'xtype'
        },
        {
            name: 'columntype'
        },
        {
            name: 'customfield'
        },
        {
            name: 'isgrouping',
            type: 'boolean'
        }, {
            name: 'showasrowexpander',
            type: 'boolean'
        }
    ]
});

Ext.define('ReportBuilder.ListFilterStore', {
    extend: 'Ext.data.Store',
    id: 'idlistfilterstore',
    fields: ['id', 'name'],
    proxy: {
        type: 'ajax',
        url: 'ACCCreateCustomReport/getComboFilterList.do',
        actionMethods : getStoreActionMethods(),
        reader: {
            type: 'json',
            rootProperty: "data"
        }
    }
});

var rowexpander = {
    ptype: 'rowexpander',
    rowBodyTpl : ['<div id="ux-preview-row-expander-box-{billid}-{recordCount}"></div>']
    //    ,insertAt: 0 ,
//    expandOnRender: true
//    expandOnDblClick: true
};

Ext.ReportBuilderTab = function(conf) {
    Ext.apply(this, conf);
    this.createFieldSelectionGrid();
    this.createReportPreviewGrid();
    this.createFieldsPanel();
    this.createFieldsPropertyGrid();

    Ext.ReportBuilderTab.superclass.constructor.call(this, {
        autoHeigth: true,
        layout: 'border',
        frame: false,
        items: [{
                region: 'center', // center region is required, no width/height specified
                xtype: 'panel',
                layout: 'fit',
                items: [
                    {
                        autoHeigth: true,
                        layout: 'border',
                        items: [{
                                region: 'center', // center region is required, no width/height specified
                                xtype: 'panel',
                                layout: 'fit',
                                margin: '5 5 0 5',
                                items: [this.fieldSelectionGrid]
                            }, {
                                region: 'south',
                                xtype: 'panel',
                                height: 450,
                                layout: 'fit',
                                split: true, // enable resizing
                                margin: '0 5 5 5',
                                items: [this.reportPreviewGrid]
                            }, {
                                title: ExtGlobal.getLocaleText("acc.CustomReport.propertyPanel"),
                                region: 'east',
                                width: 300,
                                collapsible: true, // make collapsible
                                layout: 'fit',
                                margin: '5 5 0 0',
                                items: [this.fieldsPropertyGrid]
                            }]
                    }]
            }, {
                region: 'west',
                xtype: 'panel',
                collapsible: true,
                border: false,
                title: 'Fields Panel',
                layout: 'fit',
                width: "20%",
                margin: '0 0 5 5',
                items: [this.fieldsGrid]
            }]
    });
}
Ext.extend(Ext.ReportBuilderTab, Ext.panel.Panel, {
    onRender: function(conf) {
        Ext.ReportBuilderTab.superclass.onRender.call(this, conf);
    },
    createFieldSelectionGrid: function() {
        this.fieldSelectionStore = Ext.create('Ext.data.Store', {
            model: 'customReportStoreDataModel'
        });
        
        var saveReportButton = Ext.create('Ext.Button',{
            text: ExtGlobal.getLocaleText("acc.common.saveBtn"),
            dock: 'bottom',
            itemId: 'save',
            iconCls: "pwnd save",
            tooltip:ExtGlobal.getLocaleText("acc.common.saveReport"),
            scope: this,
            handler: function() {
                this.createSaveWindow(false);
            }
        });
        var saveAndCreateNewButton = Ext.create('Ext.Button',{
            text: ExtGlobal.getLocaleText("acc.field.SaveAndCreateNew"),
            dock: 'bottom',
            iconCls: "pwnd save",
            tooltip:ExtGlobal.getLocaleText("acc.field.SaveAndCreateNew")+" Report",
            scope: this,
            handler: function() {
                this.createSaveWindow(true);
            }
        });
        var showPreviewButton = Ext.create('Ext.Button',{
            text: ExtGlobal.getLocaleText("acc.common.showPreview"),
            dock: 'bottom',
            itemId: 'update',
            iconCls: "pwnd update",
            tooltip: ExtGlobal.getLocaleText("acc.common.showPreview"),
            scope: this,
            handler: function() {
                this.fetchReportPreviewGridData();
            }
        });
        var clearAllButton = Ext.create('Ext.Button',{
            text: ExtGlobal.getLocaleText("acc.common.clearAllFilter"),
            dock: 'bottom',
            itemId: 'reset',
            iconCls: "pwnd reset",
            tooltip: ExtGlobal.getLocaleText("acc.common.clearAllFilterToolTip"),
            scope: this,
            handler: function(){
                if(recordsToDrop.length > 0){
                    this.handleResetClick();
                }
            }
        });
        this.notePanel = Ext.create('Ext.Panel',{
            border:false,
            bodyCls:'note-panel', 
            height : 20
        });
        var dockedContent = [{
            xtype: 'toolbar',
            name:"notePanelContainer",
            border:true,
            hidden : true,
            dock: 'bottom',
            items: [this.notePanel]
        },{
            xtype:"toolbar",
            dock : "bottom",
            items:[saveReportButton,saveAndCreateNewButton,showPreviewButton,clearAllButton]
        }]
        this.fieldSelectionGrid = Ext.create('Ext.grid.Panel', {
            itemId: "idfieldselectiongrid",
            title:ExtGlobal.getLocaleText("acc.common.fieldSelectionRegion") ,
            //            forceFit: true,
            border: false,
            scope: this,
            autoHeight: true,
            split: true,
            store: this.fieldSelectionStore,
            plugins: new Ext.grid.plugin.CellEditing({
                clicksToEdit: 1,
                listeners: {
                    beforeedit: function(e, editor){
                        var showasexpander = editor.record.data.showasrowexpander != undefined ? editor.record.data.showasrowexpander:false;
                        if (editor.field == "summaryType" && showasexpander){
                            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), ExtGlobal.getLocaleText("acc.common.linesummarytype"), Ext.Msg.INFO);
                            return false;
                        }
                    }
                }
            }),
            columns: [{
                    text: ExtGlobal.getLocaleText("acc.common.columnName") ,
                    dataIndex: 'defaultHeader',
                    sortable: false,
                    flex: 1,
                    renderer: function(value, metaData, record) {
                        metaData.tdAttr = 'data-qtip="' + value + '"';
                        return value;
                    }
                },
                {
                    text: ExtGlobal.getLocaleText("acc.common.displayName"),
                    dataIndex: 'displayName',
                    sortable: false,
                    flex: 1,
                    editor: {
                        allowBlank: false,
                        vtype: 'reportnamevtype'
                    },
                    renderer: function(value, metaData, record) {
                        metaData.tdAttr = 'data-qtip="' + value + '"';
                        return value;
                    }
                },
                {
                    text:ExtGlobal.getLocaleText("acc.common.moduleName"),
                    dataIndex: 'id',
                    sortable: false,
                    flex: 1,
                    renderer: function() {
                        return Ext.getCmp("idModuleCombo").getRawValue();
                    }
                },
                {
                    text: ExtGlobal.getLocaleText("acc.je.type"),
                    dataIndex: 'xtype',
                    sortable: false,
                    flex: 1,
                    renderer: function(val, cell, row, rowIndex, colIndex, ds) {
                        return Ext.getFieldType(val);
                    }
                },
                {
                    text: ExtGlobal.getLocaleText("acc.common.applyGrouping"),
                    dataIndex: 'isgrouping',
                    xtype: 'checkcolumn',
                    sortable: false,
                    align: 'center',
                    defaultType: 'boolean',
                    flex: 1,
                    renderer:
                            function(val, m, rec) {
                                if (val == '' || val == undefined || val == 'undefined')
                                {
                                    val = false;
                                }

                                var fieldData = rec.data;
                                if (fieldData.isLineItem != 'undefined' && fieldData.isLineItem != undefined) {
                                    return '';
                                } else {
                                    return (new Ext.ux.CheckColumn()).renderer(val);
                                }
                            }
                },
                {
                    text: ExtGlobal.getLocaleText("acc.common.showasExpander"),
                    dataIndex: 'showasrowexpander',
                    xtype: 'checkcolumn',
                    sortable: false,
                    align: 'center',
                    defaultType: 'boolean',
                    flex: 1,
                    renderer:
                            function(val, m, rec) {
                                if (val == '' || val == undefined || val == 'undefined')
                                {
                                    val = false;
                                }

                                var fieldData = rec.data;
                                if (fieldData.isLineItem != 'undefined' && fieldData.isLineItem != undefined) {
                                    return (new Ext.ux.CheckColumn()).renderer(val);
                                } else {
                                    return '';
                                }
                            }
                },
                {
                    text:ExtGlobal.getLocaleText("acc.common.summaryType"),
                    dataIndex: 'summaryType',
                    sortable: false,
                    flex: 1,
                    editor: new Ext.form.field.ComboBox({
                        typeAhead: true,
                        triggerAction: 'all',
                        disabled: true,
                        store: [
                            ['None', 'None'],
                            ['Count', 'Count'],
                            ['Sum', 'Sum'],
                            ['Min', 'Min'],
                            ['Max', 'Max'],
                            ['Average', 'Average']
                        ]
                    })
                },
                {
                    header: ExtGlobal.getLocaleText("acc.field.Remove"),
                    width: 100,
                    align: 'center',
                    sortable: false,
                    flex: 1,
                    renderer: function(value, css, record, row, column, store) {
                        return "<div style='margin: 0px auto 0px auto;' class='delete pwnd delete-gridrow'  title='"+ExtGlobal.getLocaleText("acc.common.delete")+"'></div>";
                    }
            }],
            viewConfig: {
                emptyText: "<div style='text-align:center;font-size:16px;'>"+ExtGlobal.getLocaleText("acc.CustomReport.fieldsGridDisplayMessage")+"</div>",
                deferEmptyText: false,
                plugins: {
                    ptype: 'gridviewdragdrop',
                    containerScroll: true
                },
                listeners: {
                    scope: this,
                    drop: function(node, data, dropRec, dropPosition) {
                        this.updateColumnsArray();
                    }
                }
            },
            dockedItems: dockedContent
        });
        this.fieldSelectionGrid.on('render', function(editor, e, options) {
            var selfDDGroup = 'self-dd-zone-' + this.fieldSelectionGrid.id;
            this.createDragZone(this.fieldSelectionGrid, selfDDGroup);
            this.createDropZone(this.fieldSelectionGrid, selfDDGroup);
        }, this);
        this.fieldSelectionGrid.on('edit', this.validateSummaryType, this);
        this.fieldSelectionGrid.on('selectionchange', this.loadPropertyGrid, this);
        this.fieldSelectionGrid.on('cellclick', this.handleCellClick, this);
    },
    createSaveWindow: function(isSaveAndCreateNew) {
        var globalColumns=0;
        for(var i=0;i<recordsToDrop.length;i++){
            if (!recordsToDrop[i].showasrowexpander)
                globalColumns++; 
        }
        
        if (recordsToDrop.length == 0) {
            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), ExtGlobal.getLocaleText("acc.common.selectAtleastoneColumn"), Ext.Msg.INFO);
            return;
        } else if(globalColumns == 0){
            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), ExtGlobal.getLocaleText("acc.common.selectAtleastoneGlobalColumn"), Ext.Msg.INFO);
        } else {
            if (!this.saveWindow) {
                this.saveWindow = Ext.create('Ext.SaveReportWindow', {
                    moduleCatId: this.moduleCategoryCombo.getValue(),
                    moduleId: this.moduleCombo.getValue(),
                    isSaveAndCreateNew : isSaveAndCreateNew
                });
            }
            this.saveWindow.show();
            this.saveWindow.on('close', function() {
                if (isSaveAndCreateNew) {
                    this.handleResetClick(true);  // true argument is send to hide note panel to restore to initial state
                    this.handleClearPreview();
                }
                this.saveWindow = undefined;
            }, this);
            
            this.saveWindow.on('hide', function() {
                this.saveWindow.destroy();
                this.saveWindow = undefined;
            }, this);
        }
    },
    handleCellClick: function(view, cell, cellIndex, record, row, rowIndex, event) {
        var clickedDataIndex = view.panel.headerCt.getHeaderAtIndex(cellIndex).dataIndex;
        var clickedColumnName = view.panel.headerCt.getHeaderAtIndex(cellIndex).text;
        var clickedCellValue = record.get(clickedDataIndex);
        if (event.getTarget("div[class='delete pwnd delete-gridrow']")) {
            this.deleteRecord(record.get('id'));
        }
        if (clickedDataIndex == 'showasrowexpander') {
            if(record.data.isLineItem != 'undefined' && record.data.isLineItem != undefined && record.data.isLineItem){
                
                record.set(clickedDataIndex, clickedCellValue);
                var recordsDroped = recordsToDrop[rowIndex];
                recordsDroped.showasrowexpander = clickedCellValue;
                // If selected as row expander then clear applies summary type
                if(clickedCellValue){
                    this.fieldSelectionStore.getAt(rowIndex).set("summaryType", "");
                }
                //Show Note panel for updates
                this.hideNotePanel(false,false);
            }else{
                this.fieldSelectionStore.getAt(rowIndex).set("showasrowexpander", false)
            }
        } else if (clickedDataIndex == 'isgrouping') {
            this.validateGroupingField(clickedCellValue, rowIndex)
        } else {
            return;
        }
    },
    deleteRecord: function(id) {
        var store = this.fieldSelectionGrid.getStore();
        var index = store.findExact('id', id);
        var record = store.getAt(index);
        store.remove(record);
        this.updateColumnsArray();
        this.fieldsPropertyGrid.setSource({}); // On Deletion of the selected Row remove the details from Property Panel
    },
    createReportPreviewGrid: function() {
        this.reportPreviewStore = Ext.create('Ext.data.Store', {
            id: 'idReportPreviewStore',
            model: 'customReportStoreDataModel',
            autoLoad: false,
            pageSize: reportPreviewPageSize,
            remoteFilter: true,
            proxy: {
                type: 'ajax',
                url: 'ACCCreateCustomReport/executeCustomReportPreview.do',
                actionMethods : getStoreActionMethods(),
                reader: {
                    type: 'json',
                    rootProperty: "data",
                    keepRawData: true,
                    totalProperty: 'totalCount'
                }
            }
        });
        this.reportPreviewStore.on("beforeload", function() {
//            this.reportPreviewStore.proxy.extraParams = {
                this.reportPreviewStore.proxy.extraParams.moduleID = this.moduleCombo.getValue();
                this.reportPreviewStore.proxy.extraParams.selectedRows = JSON.stringify(recordsToDrop);
                this.reportPreviewStore.proxy.extraParams.deleted = false;
                this.reportPreviewStore.proxy.extraParams.nondeleted =  false;
                this.reportPreviewStore.proxy.extraParams.pendingapproval= false;
                this.reportPreviewStore.proxy.extraParams.showRowLevelFieldsflag = false;
                this.reportPreviewStore.proxy.extraParams.isLeaseFixedAsset = false;
                this.reportPreviewStore.proxy.extraParams.gcurrencyid = Ext.pref.Currencyid;
                this.reportPreviewStore.proxy.extraParams.consolidateFlag = false
//            }
        }, this);
        this.reportPreviewGrid = Ext.create('Ext.grid.Panel', {
            columns: [],
            title:ExtGlobal.getLocaleText("acc.common.reportDataPreviewRegion") ,
            enableLocking: true,
            store: this.reportPreviewStore,
            defaultListenerScope: true,
            viewConfig: {
                emptyText: "<div style='text-align:center;font-size:16px;'>"+ExtGlobal.getLocaleText("acc.CustomReport.reportPreviewEmptyText") +" </div>",
                deferEmptyText: false
            },
            plugins: [rowexpander, 'gridfilters'],
            pluginColumns : 2,   //Specify this config to lock/unlock columns like row expander ,row numberer etc.
            dockedItems: [{
                xtype: 'pagingtoolbar',
                store: this.reportPreviewStore,
                dock: 'bottom',
                displayInfo: true,
                animateShadow: true,
                plugins: [new Ext.ux.grid.PageSize(), new Ext.ux.ProgressBarPager()],
                items: ["-", {
                    text:ExtGlobal.getLocaleText("acc.common.clearFilters") ,
                    tooltip: {
                        text: ExtGlobal.getLocaleText("acc.common.clearFilterToolTip"),
                        mouseOffset: [0, -45]
                    },
                    iconCls: "pwnd remove-filter",
                    handler: 'onClearFilters'
                }, "-", {
                    text:ExtGlobal.getLocaleText("acc.common.clearPreview") ,
                    iconCls: "pwnd reset",
                    tooltip: {
                        text: ExtGlobal.getLocaleText("acc.common.clearPreview")+" Region",
                        mouseOffset: [0, -45]
                    },
                    scope: this,
                    handler: this.handleClearPreview
                }]
            }],
            features: [{
                    groupHeaderTpl: '{columnName}:{name}{defaultHeader} ({rows.length} Item{[values.rows.length > 1 ? "s" : ""]})',
                    ftype: 'groupingsummary'
                }],
            onClearFilters: function() {
                this.filters.clearFilters();
            }
        });
        
        this.reportPreviewGrid.on('lockcolumn',ExtGlobal.lockPluginColumns);
        this.reportPreviewGrid.on('unlockcolumn',ExtGlobal.unlockPluginColumns);
        
        this.reportPreviewGridview = this.reportPreviewGrid.getView();
        this.reportPreviewGridview.on('expandbody', this.expandRow, this);
    },
    expandRow: function(rowNode, record, body) {
        this.expandPreviewStore = Ext.create('Ext.data.Store', {
            id: 'idexpandPreviewStore',
            model: 'customReportStoreDataModel',
            autoLoad: false,
            proxy: {
                type: 'ajax',
                url: 'ACCCreateCustomReport/executeCustomReportPreview.do',
                actionMethods : getStoreActionMethods(),
                reader: {
                    type: 'json',
                    rootProperty: "data",
                    keepRawData: true,
                    totalProperty: 'totalCount'
                }
            }
        });
        var billid = record.data.billid;
//        this.expandPreviewStore.on('load',this.fillExpanderBody,this);
        this.expandPreviewStore.load({
            scope:this,
            params: {
                selectedRows: JSON.stringify(recordsToDrop),
                moduleID: Ext.getCmp("idModuleCombo").getValue(),
                showRowLevelFieldsflag: true,
                billid: billid
            },
            callback: function(records, operation, success) {
                if (success) {
                    var arr = [];
                    this.serialNumber = 0;
                    var resObj = eval("(" + operation.getResponse().responseText + ")");
                    var recordbillid=resObj.billid;
                    var columns = [];
                    for (var i = 0; i < recordsToDrop.length; i++) {
                        var data = recordsToDrop[i];
                        if (data.showasrowexpander) {
                            columns.push(data);
                        }
                    }

                    if (columns.length != 0) {   //if row level column exist
                        var header = setRowExpanderHTML(columns, records);
                        //                        body.innerHTML = header;
                        Ext.get('ux-preview-row-expander-box-'+recordbillid +"-"+ record.data.recordCount ).setHtml(header);
                    //                    
                    } else {
                        body.innerHTML = "<div style='width: 100%;min-width:1900px;margin-left:50px'><span class='gridHeader'>"+ExtGlobal.getLocaleText("account.common.nodatadisplay")+".</span></div>";
                    }
                } else {
                    body.innerHTML = "<div style='width: 100%;min-width:1900px;margin-left:50px'><span class='gridHeader'>"+ExtGlobal.getLocaleText("account.common.nodatadisplay")+".</span></div>";
                }
                
                if (this.reportPreviewGrid.lockedGrid) {
                    this.reportPreviewGrid.lockedGrid.view.refreshSize();
                }
            }
        });
    },
    createFieldsPanel: function() {
        this.moduleCategoryStore = Ext.create('Ext.data.ModuleCategoryStore');
        this.moduleCategoryStore.load();

        this.moduleCategoryStore.on("load", function(store, record, success, opts) {
            this.moduleCategoryCombo.setValue(this.moduleCategoryStore.data.items[0].data.moduleCatId);
            this.accModuleStore.load();
        }, this);

        this.moduleCategoryCombo = Ext.create('Ext.form.field.ComboBox', {
            store: this.moduleCategoryStore,
            fieldLabel: ExtGlobal.getLocaleText("acc.common.moduleCategory"),
//            id: "idModuleCategoryCombo",
            width: 240,
            displayField: 'moduleCatName',
            valueField: 'moduleCatId',
            emptyText: ExtGlobal.getLocaleText("acc.common.selectmoduleCategoryemptyText") ,
            allowBlank :false,
            forceSelection : true,
            queryMode: 'local',
            triggerAction: 'all',
            listeners: {
                scope: this,
                select: function(obj, newVal, oldVal) {
                    this.accModuleStore.reload();
                },
                change : function( newValue, oldValue, eOpts) {
                    if(recordsToDrop.length > 0){
                        this.handleResetClick(true);
                    }
                    this.handleClearPreview();
                }
            }
        });

        this.accModuleStore = Ext.create('Ext.data.JsonStore', {
            proxy: {
                type: 'ajax',
                url: 'ACCCreateCustomReport/getModules.do',
                actionMethods : getStoreActionMethods(),
                reader: {
                    type: 'json',
                    rootProperty: "data"
                }
            }
        });
        this.accModuleStore.on("beforeload", function() {
            this.accModuleStore.proxy.extraParams = {
                moduleCatIdValue: this.moduleCategoryCombo.getValue()
            }
        }, this);
        this.accModuleStore.on("load", function(store, record, success, opts) {
            this.moduleCombo.setValue(this.accModuleStore.data.items[0].data.id);
            this.accFieldsStore.load();
        }, this);
        this.moduleCombo = Ext.create('Ext.form.field.ComboBox', {
            store: this.accModuleStore,
            id: "idModuleCombo",
            fieldLabel: ExtGlobal.getLocaleText("acc.common.moduleName"),
            width: 240,
            displayField: 'modulename',
            valueField: 'id',
            emptyText:ExtGlobal.getLocaleText("acc.common.selectmoduleemptyText") ,
            allowBlank:false,
            forceSelection : true,
            queryMode: 'local',
            triggerAction: 'all',
            listeners: {
                scope: this,
                select: function(obj, newVal, oldVal) {
                    if (newVal != "" && newVal != undefined) {
                        this.accFieldsStore.reload();
                    }
                }
            }
        });
        this.accFieldsStore = Ext.create('Ext.data.Store', {
            id: 'accFieldsStoreId',
            model: 'FieldsModel',
            groupField: "columntype",
            //autoLoad : true,
            proxy: {
                type: 'ajax',
                url: 'ACCCreateCustomReport/getFiledsData.do',
                actionMethods : getStoreActionMethods(),
                reader: {
                    type: 'json',
                    rootProperty: "data"
                }
            }
        });

        this.accFieldsStore.on("beforeload", function() {
            this.accFieldsStore.proxy.extraParams = {
                id: this.moduleCombo.getValue()
            }
        }, this);

        this.searchTextField = Ext.create('Ext.form.field.Text', {
            hidelabel: true,
            width: 180,
            emptyText: ExtGlobal.getLocaleText("acc.common.searchemptyText"),
            listeners: {
                scope: this,
                change: function(field, e) {
                    this.searchField();
                }
            }
        });
        this.resetSearchButton = Ext.create('Ext.Button', {
            iconCls: "pwnd reset",
            text: ExtGlobal.getLocaleText("acc.common.reset"),
            tooltip: ExtGlobal.getLocaleText("acc.common.resetTT"),
            scope: this,
            handler: function() {
                this.searchTextField.setValue("");
                this.fieldsGrid.filters.clearFilters();
            }
        });
        var dockedItems = [];
        dockedItems.push({
            xtype: 'toolbar',
            dock: 'top',
            items: [this.moduleCategoryCombo]
        });
        dockedItems.push({
            xtype: 'toolbar',
            dock: 'top',
            items: [this.moduleCombo]
        });
        dockedItems.push({
            xtype: 'toolbar',
            dock: 'top',
            items: [this.searchTextField, this.resetSearchButton]
        });
        
        var filterStore = Ext.create('Ext.data.ArrayStore',{
            fields:['id','name'],
            data: [
                    [Ext.fieldType.textField, "Text Field"],
                    [Ext.fieldType.numberField, "Number Field"],
                    [Ext.fieldType.dateField, "Date Field"],
                    [Ext.fieldType.comboBox, "Combo Box"],
                    [Ext.fieldType.multiselect, "Multiselect Combo Box"],
                    [Ext.fieldType.checkbox, "Check Box"],
                    [Ext.fieldType.listBox, "List Box"],
                    [Ext.fieldType.textArea, "Text Area"]
                ],
            autoLoad: true
        });
        
        this.fieldsGrid = Ext.create('Ext.grid.Panel', {
            border: true,
            scope: this,
            store: this.accFieldsStore,
            layout: 'fit',
            scrollable: 'y',
            features: Ext.create('Ext.grid.feature.Grouping', {
                groupHeaderTpl: '{name} {defaultHeader} ({rows.length} Item{[values.rows.length > 1 ? "s" : ""]})',
                enableGroupingMenu:false
            }),
            emptyText: 'No Matching Records',
            plugins: 'gridfilters',
            columns: [
            {
                text: ExtGlobal.getLocaleText("acc.common.columnName"),
                dataIndex: 'defaultHeader',
                flex: 1,
                sortable: true,
                filter: {
                    type: 'string',
                    itemDefaults: {
                        emptyText: ExtGlobal.getLocaleText("acc.common.searchemptyText")
                    }
                },
                renderer: function(value, metaData, record) {
                        metaData.tdAttr = 'data-qtip="' + value + '"';
                        return value;
                }
            },
            {
                text:  ExtGlobal.getLocaleText("acc.je.type"),
                dataIndex: 'xtype',
                flex: 1,
                sortable: true,
                renderer: function(val, cell, row, rowIndex, colIndex, ds) {
                    return Ext.getFieldType(val);
                },
                filter: {
                    type: 'customlist',
                    idField: "id",
                    labelField: 'name', 
                    store : filterStore
                }
            }
            ],
            viewConfig: {
                allowCopy: true,
                copy: true,
                plugins: {
                    ptype: 'gridviewdragdrop'
                },
                listeners: {
                    scope: this,
                    drop: function(node, data, dropRec, dropPosition) {
                        this.updateColumnsArray();
                    }
                }
            },
            dockedItems: dockedItems
        });
        this.fieldsGrid.on('render', function(editor, e, options) {
            var selfDDGroup = 'self-dd-zone-' + this.fieldsGrid.id;
            this.createDragZone(this.fieldsGrid, selfDDGroup);
            this.createDropZone(this.fieldsGrid, selfDDGroup);
        }, this);
    },
    createDragZone: function(config, ddgroup) {
        Ext.create('Ext.dd.DragZone', config.getEl(), {
            ddGroup: ddgroup
        });
    },
    createDropZone: function(config, ddgroup) {
        Ext.create('Ext.dd.DropZone', config.el, {
            ddGroup: ddgroup
        });
    },
    updateColumnsArray: function(isHideNotePanel) {
        var showNotePanel = isHideNotePanel ? isHideNotePanel : false;
        var items = this.fieldSelectionGrid.getStore().data.items;
        recordsToDrop = [];
        for (var i = 0; i < items.length; i++) {
            recordsToDrop.push(items[i].data);
        }
        this.hideNotePanel(showNotePanel,false);
    },
    handleResetClick: function(isHideNotePanel) {
        var store1 = this.fieldSelectionGrid.getStore();
        store1.removeAll()
//        this.updateColumnsArray(isHideNotePanel);
         this.fieldsPropertyGrid.setSource({});// On Clear All empty the Property Panel from Property Panel
    },
    handleClearPreview: function() {
        var columns = [];
        this.reportPreviewStore.proxy.extraParams.isClearPreview = true;
        this.reportPreviewGrid.getStore().load();
        this.reportPreviewGrid.getStore().totalCount = 0;
        this.reportPreviewGrid.reconfigure(this.reportPreviewGrid.getStore(), columns);
    },
    searchField: function() {
        var searchValue = this.searchTextField.getValue();
        this.fieldsGrid.columns[0].filter.setValue(searchValue);
    },
    fetchReportPreviewGridData: function() {
        this.reportPreviewStore.proxy.extraParams.isClearPreview = false;
        if(recordsToDrop.length > 0){
            this.reportPreviewStore.loadPage(1);
            this.reportPreviewStore.on("load", function(store, record, success, opts) {
                var elements = [];
                elements = getRecordsforExpander(false);
                this.reportPreviewStore.group(getGroupingFields(elements));
            }, this);
            var headerArray = createColumns(recordsToDrop);
            Ext.suspendLayouts();
            this.reportPreviewGrid.reconfigure(this.reportPreviewStore, headerArray);
            Ext.resumeLayouts(true);
            this.hideNotePanel(true,false);
        }else{
            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), ExtGlobal.getLocaleText("acc.common.selectAtleastoneColumn"), Ext.Msg.INFO); 
        }
    },
    validateGroupingField: function(checked, rowIndex) {
        var recordsDroped = recordsToDrop[rowIndex];
        var check = false;
        if (!recordsDroped.isLineItem) {
            // Check if one of the column checkbox from grid is checked or not. 
            var recordcount = this.fieldSelectionStore.getRange().length;
            if (checked) {
                for (var i = 0; i < recordcount; i++) {
                    if (i != rowIndex) {
                        var active = this.fieldSelectionStore.getAt(i).get("isgrouping");
                        if (active == true) {
                            check = true;
                            break;
                        }
                    }
                }
                if (check === true) {
                    Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), ExtGlobal.getLocaleText("acc.common.groupingonOneField"), Ext.Msg.INFO);
                    this.fieldSelectionStore.getAt(rowIndex).set("isgrouping", false)
                } else {
                    recordsDroped.isgrouping = checked;
                    this.fieldSelectionGrid.down('[dataIndex=summaryType]').getEditor().enable();
                    this.hideNotePanel(false,false);
                }
            } else {
                //If no grouping applied then clear all summary types.
                for (i = 0; i < recordcount; i++) {
                    this.fieldSelectionStore.getAt(i).set("summaryType", "");
                }
                this.fieldSelectionGrid.down('[dataIndex=summaryType]').getEditor().disable();
                this.hideNotePanel(false,false);
            }
        } else {
            this.fieldSelectionStore.getAt(rowIndex).set("isgrouping", false)
        }
    },
    validateSummaryType: function(editor, e, options) {
        var isedited=false;
        var value = e.value!=undefined ? e.value:"";
        var originalValue = e.originalValue !=undefined ? e.originalValue:"";
        if (e.field == "summaryType") {
            if ((value == "Sum" || value == "Min" || value == "Max" || value == "Average") && (e.record.data.xtype != Ext.fieldType.numberField)) {
                Ext.CustomMsg("Info", ExtGlobal.getLocaleText("acc.CustomReport.invalidGroupingMessage"), Ext.Msg.INFO);
                e.record.set("summaryType", originalValue);
            } else if(value!="" && (value.toLowerCase() != originalValue.toLowerCase())){
                var recordsDroped = recordsToDrop[e.rowIdx];
                recordsDroped.summaryType = value ? value.toLowerCase() : "";
                isedited=true;
            }
        } else if (e.field == "displayName") {
            if(value!=originalValue)
            isedited=true;
        }
        if(isedited){
             this.hideNotePanel(false,false);
        }
    },
    createFieldsPropertyGrid: function() {
        this.fieldsPropertyGrid = Ext.create('Ext.grid.property.Grid', {
            width: 300,
            emptyText: "<div style='text-align:center;font-size:12px;'>"+ExtGlobal.getLocaleText("acc.CustomReport.propertyGridDisplayMessage")+" <div>",
            source: {},
            listeners: {
                scope:this,
                beforeedit: function(editor, e, opts) {
                    if (e.record.get('name') == '(Column Name)') {    //Make property (name) non editable.
                        return false;
                    }
                },
                edit: function ( editor, e, eOpts ) {
                    if (e.originalValue != e.value) {
                        this.hideNotePanel(false,true);
                    }
                }
            }
        });
    },
    loadPropertyGrid: function(grid, selected, eOpts) {
        if (selected.length > 0) {
            var source = selected[0].data.properties.source;
            var sourceConfig = selected[0].data.properties.sourceConfig;
            for (var prop in source) {
                if (sourceConfig.hasOwnProperty(prop)) {
                    var property = sourceConfig[prop];
                    if (property.editor != undefined && typeof property.editor === 'string') {
                        sourceConfig[prop].editor = JSON.parse(property.editor);
                    }
                    if (property.renderer != undefined && typeof property.renderer === 'string') {
                        sourceConfig[prop].renderer = eval("(" + property.renderer + ")");
                    }
                }
            }
            this.newPropertySource = [source, sourceConfig];
            this.fieldsPropertyGrid.setSource.apply(this.fieldsPropertyGrid, this.newPropertySource);
        }
    },
    hideNotePanel : function(ishide,ispropertyupdate){
        var notePanelContainer = this.fieldSelectionGrid.dockedItems.items.getIemtByParam({name: "notePanelContainer"});
        
        if(ispropertyupdate) {
            this.notePanel.update(ExtGlobal.getLocaleText("acc.common.propertyGridSelectionhtml"));
        } else {
            this.notePanel.update(ExtGlobal.getLocaleText("acc.common.fieldSelectionhtml"));
        }
        if(ishide){
             notePanelContainer.hide();
        }else{
            notePanelContainer.show();
        }
    }
});

Ext.ReportListTab = function(conf) {
    Ext.apply(this, conf);
    this.createReportListGrid();
    Ext.ReportListTab.superclass.constructor.call(this, {
        layout: 'fit',
        items: [this.reportListGrid]
    });
}
Ext.extend(Ext.ReportListTab, Ext.panel.Panel, {
    onRender: function(conf) {
        Ext.ReportListTab.superclass.onRender.call(this, conf);
    },
    createReportListGrid: function() {
        this.accreportlistStore = Ext.create('Ext.data.Store', {
            id: 'accreportlistStore',
            model: 'customReportStoreDataModel',
            remoteFilter: true,
            pageSize: 25,
            proxy: {
                type: 'ajax',
                url: 'ACCCreateCustomReport/getCustomReportList.do',
                actionMethods : getStoreActionMethods(),
                reader: {
                    type: 'json',
                    rootProperty: "data",
                    totalProperty: 'totalCount'
                }
            }
        });
        this.accreportlistStore.load({
            params: {
                start: 0,
                limit: 25
            }
        });
        var reportListBtnArr = [];
        this.newReportButton = new Ext.Button({
            text: ExtGlobal.getLocaleText("acc.common.createNewReport"),
            scope: this,
            iconCls: "pwnd create-new",
            tooltip: ExtGlobal.getLocaleText("acc.common.createNewReport"),
            handler: function() {
                createNewReportTab('idnewcustomreporttab');
            }
        });
        this.deleteReportBtn = new Ext.Button({
            text: ExtGlobal.getLocaleText("acc.common.deleteReports"),
            iconCls: "accountingbase delete",
            tooltip: ExtGlobal.getLocaleText("acc.CustomReport.deleteToolTip"),
            disabled: true,
            scope: this,
            handler: function() {
                this.deleteReports();
            }
        });
        reportListBtnArr.push(this.newReportButton, ' ', this.deleteReportBtn);
        
        reportListBtnArr.push({
            text: ExtGlobal.getLocaleText("acc.common.clearFilters"),
            tooltip: ExtGlobal.getLocaleText("acc.common.clearFilterToolTip"),
            iconCls: "pwnd remove-filter",
            handler: 'onClearFilters'
        });

        this.cellEditing = new Ext.grid.plugin.CellEditing({
            clicksToEdit: 1
        });
        
        this.reportListGrid = Ext.create('Ext.grid.Panel', {
            id: 'idreportlistgrid',
            layout: 'fit',
            tbar: reportListBtnArr,
            border :false,
//            frame: true,
            selType: 'checkboxmodel',
            store: this.accreportlistStore,
            defaultListenerScope: true,
            plugins: [this.cellEditing, 'gridfilters'],
            viewConfig: {
                emptyText: "<div style='text-align:center;font-size:16px;'>"+ExtGlobal.getLocaleText("acc.common.norecordstoDisplay") +" </div>",
                deferEmptyText: false
            },
            columns: [
                {
                    text:  ExtGlobal.getLocaleText("acc.common.ReportName"),
                    dataIndex: 'reportname',
                    flex: 1,
                    sortable: true,
                    editor: {
                allowBlank: false,
                        validateBlank: true,
                        vtype: 'reportnamevtype',
                        maxLength: 50
                    },
                    renderer: function(val) {
                        return "<a href = '#' class='newReport'>" + val + "</a>";
                    },
                    filter:"string"
                },
                {
                    text: ExtGlobal.getLocaleText("acc.common.ReportDescription"),
                    dataIndex: 'reportdescription',
                    flex: 2,
                    sortable: true,
                    editor: {
                        maxLength: 255
                    },
                    renderer: function(value, metaData, record) {
                        metaData.tdAttr = 'data-qtip="' + value + '"';
                        return value;
                    },
                    filter:"string"
                },
                {
                    text:  ExtGlobal.getLocaleText("acc.common.moduleName"),
                    dataIndex: 'modules.modulename',
                    flex: 1,
                    sortable: true,
                    filter:"string"
                },
                {
                    text: ExtGlobal.getLocaleText("acc.common.moduleCategory"),
                    dataIndex: 'moduleCategory.moduleCatname',
                    flex: 1,
                    sortable: true,
                    filter:"string"
                },
                {
                    text: ExtGlobal.getLocaleText("acc.common.reportcreatedon"),
                    dataIndex: 'customreports.createdon',
                    flex: 1,
                    sortable: true,
                    hidden : true,
                    filter:{type: "date",dateFormat:'Y-m-d'}
                },
                {
                    text: ExtGlobal.getLocaleText("acc.common.reportupdatedon"),
                    dataIndex: 'customreports.updatedon',
                    flex: 1,
                    sortable: true,
                    hidden : true,
                    filter:{type: "date",dateFormat:'Y-m-d'}
                }],
              dockedItems: [{
                    xtype: 'pagingtoolbar',
                    store: this.accreportlistStore,
                    dock: 'bottom',
                    displayInfo: true,
                    animateShadow: true,
                    plugins: [new Ext.ux.grid.PageSize(), new Ext.ux.ProgressBarPager()]
                }],
            onClearFilters: function() {
                this.filters.clearFilters();
                            }
        });
        this.reportListGrid.on("cellclick", this.afterGridCellClick, this);
        this.reportListGrid.on("selectionchange", this.enableDisableButtons, this);
        this.reportListGrid.on('edit', function(editor, e) {
            if (e.originalValue != e.value) {
                Ext.Ajax.request({
                    url: 'ACCCreateCustomReport/updateCustomReportNameAndDescription.do',
                    method:"POST",
                    params: {
                        reportNo: e.record.data.id,
                        reportNewDesc: e.record.data.reportdescription,
                        reportNewName: e.record.data.reportname,
                        isreportNameFieldEdited: e.field == "reportname" ? true : false
                    },
                    success: function(res, req) {
                        var resObj = eval("(" + res.responseText + ")");
                        if (resObj.success) {
                            Ext.CustomMsg('Success', resObj.msg, Ext.Msg.INFO);
                            e.record.data.updatedon = resObj.updatedon;
                            e.record.commit();
                            } else {
                            e.record.reject();
                            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), resObj.msg, Ext.Msg.INFO);
                            }
                    },
                    failure: function() {
                        Ext.CustomMsg('Error',ExtGlobal.getLocaleText("acc.common.errorOccuratServerSide"), Ext.Msg.ERROR);
                        }
                });
                    }
        });
    },
    afterGridCellClick: function(grid, td, cellIndex, record, tr, rowIndex, e, eOpts) {
        var event = e;
        if (event.getTarget("a[class='newReport']")) {
            createNewReportTab("report" + record.data.reportname.replace(/\s/g, ''), record.data.reportname, record.data.id , record.data.moduleId);
        }
    },
    enableDisableButtons: function(grid, selected, eOpts) {
        if (selected.length > 0) {
            this.deleteReportBtn.enable();
        } else {
            this.deleteReportBtn.disable();
        }
    },
    deleteReports: function() {
        var count = 0;
        var reportIds = "";
        var reportListGrid = this.reportListGrid;
        var selectedReports = reportListGrid.getSelectionModel().getSelected().items;
        for (count = 0; count < selectedReports.length; count++) {
            reportIds += selectedReports[count].data.id + ",";
        }
        reportIds = reportIds.substring(0, reportIds.length - 1);
        Ext.Ajax.request({
            url: 'ACCCreateCustomReport/deleteCustomReport.do',
            method:"POST",
            params: {
                reportIds: reportIds
            },
            success: function(res, req) {
                var resObj = eval("(" + res.responseText + ")");
                if (resObj.success == true) {
                    Ext.CustomMsg('Success', resObj.msg, Ext.Msg.INFO);
                    var reportCount = reportListGrid.getStore().count();
                    var currentPage = reportListGrid.getStore().currentPage;
                    if((reportCount > selectedReports.length  && currentPage > 0)||(currentPage == 1)){
                        reportListGrid.getStore().loadPage(currentPage);
                    }else if(currentPage > 1){
                        reportListGrid.getStore().loadPage(currentPage-1);
                    }
                } else {
                    Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), resObj.msg, Ext.Msg.INFO);
                }
            },
            failure: function() {
                Ext.CustomMsg('Error', ExtGlobal.getLocaleText("acc.common.errorOccuratServerSide"), Ext.Msg.ERROR);
            }
        });
    }
});

Ext.ReportTab = function(conf) {
    Ext.apply(this, conf);
    this.createReportGrid();
    this.createAdvanceSearchComponent();
    Ext.ReportTab.superclass.constructor.call(this, {
        layout: 'border',
        items: [{
            region: 'north',
            layout:"fit",
            border:false,
            items : [this.advanceSearchGrid]
        }, {
            region: 'center',
            layout:"fit",
        items: [this.reportGrid]
        }]
    });
}
Ext.extend(Ext.ReportTab, Ext.panel.Panel, {
    onRender: function(conf) {
        Ext.ReportTab.superclass.onRender.call(this, conf);
    },
    createReportGrid: function() {
        this.reportStore = Ext.create('Ext.data.Store', {
            model: 'customReportStoreDataModel',
            autoLoad: false,
            pageSize: 25,
            remoteFilter: true,
            proxy: {
                type: 'ajax',
                url: 'ACCCreateCustomReport/executeCustomReport.do',
                actionMethods : getStoreActionMethods(),
                reader: {
                    type: 'json',
                    rootProperty: "data",
                    keepRawData: true,
                    totalProperty: 'totalCount'
                },
                params: {
                    reportID: this.reportId,
                    deleted: false,
                    nondeleted: false,
                    pendingapproval: false,
                    showRowLevelFieldsflag: false
                }
            }
        });
        this.reportStore.load({
            params: {
                reportID: this.reportId,
                start: 0,
                limit: 25
            }
        });
        this.reportStore.on("beforeload", function() {
            this.reportStore.proxy.extraParams.reportID = this.reportId;
            this.reportStore.proxy.extraParams.fromDate = ExtGlobal.convertToGenericDate(this.fromDate.getValue());
            this.reportStore.proxy.extraParams.toDate = ExtGlobal.convertToGenericDate(this.toDate.getValue());
            this.reportStore.proxy.extraParams.gcurrencyid = Ext.pref.Currencyid;
            this.reportStore.proxy.extraParams.consolidateFlag = false;

        }, this);
        this.fromDate = new Ext.form.field.Date({
            fieldLabel:ExtGlobal.getLocaleText("acc.common.from") ,
            id: this.id + 'startdt',
            name: 'startdt',
            labelWidth: 30,
            width: 145,
            format: ExtGlobal.getOnlyDateFormat(),
            value: financialYearFromDate,
            vtype: 'daterange',
            endDateField: this.id + 'enddt'
        });
        this.toDate = new Ext.form.field.Date({
            fieldLabel: ExtGlobal.getLocaleText("acc.common.to") ,
            id: this.id + 'enddt',
            name: 'enddt',
            labelWidth: 20,
            width: 145,
            format: ExtGlobal.getOnlyDateFormat(),
            value: financialYearToDate,
            vtype: 'daterange',
            startDateField: this.id + 'startdt'
        });

        var btnArr = [];

        btnArr.push(this.fromDate, '-', this.toDate, '-', {
            xtype: 'button',
            text: ExtGlobal.getLocaleText("acc.common.fetch") ,
            iconCls: 'accountingbase fetch',
            scope: this,
            tooltip:ExtGlobal.getLocaleText("acc.invReport.fetchTT"),
            handler: function() {
                this.reportStore.reload();
            }
        });
        this.resetBttn = new Ext.Button({
            text:ExtGlobal.getLocaleText("acc.common.reset") ,
            scope: this,
            iconCls: "pwnd reset",
            tooltip: ExtGlobal.getLocaleText("acc.common.reset")+" date",
            disabled: false,
            handler: function() {
                this.fromDate.setValue(financialYearFromDate);
                this.toDate.setValue(financialYearToDate);
                this.reportStore.reload();
            }
        });
        btnArr.push(this.resetBttn);
        btnArr.push({
            text: ExtGlobal.getLocaleText("acc.common.clearFilters"),
            tooltip: ExtGlobal.getLocaleText("acc.common.clearFilterToolTip"),
            iconCls: "pwnd remove-filter",
            handler: 'onClearFilters'
        });
        
        var reportGridExpander = {
            ptype: 'rowexpander',//Ext.grid.plugin.RowExpander
            rowBodyTpl : ['<div id="ux-report-row-expander-box-{billid}-{reportID}-{recordCount}"></div>']
        };

        //Export  Button
        this.exportButton=new Ext.exportReportButton({
            obj:this,
            isEntrylevel:false,
            text:ExtGlobal.getLocaleText("acc.common.export"),
            disabled :false,
            moduleId:20,
            iconCls: 'pwnd export',
            get:1, 
            menuItem:{
                csv:true,
                pdf: true, 
                xlsx:true
            }
        });
        
        this.AdvanceSearchBtn = new Ext.Button({
            text: ExtGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
            scope: this,
            tooltip: ExtGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });
        
        btnArr.push(this.AdvanceSearchBtn);
        this.reportGrid = Ext.create('Ext.grid.Panel', {
            columns: [],
            viewConfig: {
                deferEmptyText: false,
                emptyText: "<div style='text-align:center;font-size:18px;'>"+ExtGlobal.getLocaleText("account.common.nodatadisplay")+"</div>"
            },
            selModel: Ext.create('My.extension.LockedCheckBoxSelModel',{injectCheckbox:'first',lock:false}),
            plugins: [reportGridExpander, 'gridfilters'],
            pluginColumns : 3,     //Specify this config to lock/unlock columns like row expander ,row numberer etc.
            border : false,
//            frame: true,
            enableLocking: true,
            defaultListenerScope: true,
            tbar: btnArr,
            store: this.reportStore,
            features: [{
                    groupHeaderTpl: '{columnName}: {name}{defaultHeader} ({rows.length} Item{[values.rows.length > 1 ? "s" : ""]})',
                    ftype: 'groupingsummary'
                }],
            dockedItems: [{
                    xtype: 'pagingtoolbar',
                    store: this.reportStore,
                    dock: 'bottom',
                    displayInfo: true,
                    animateShadow: true,
                    items: ["-",this.exportButton],
                    plugins: [new Ext.ux.grid.PageSize(), new Ext.ux.ProgressBarPager()]
                }],
            onClearFilters: function() {
                this.filters.clearFilters();
            }
        });
        
        this.reportGrid.on('lockcolumn',ExtGlobal.lockPluginColumns);
        this.reportGrid.on('unlockcolumn',ExtGlobal.unlockPluginColumns);
        
        this.isReportReconfigure = false;
        this.reportStore.on("load", this.handlegridonload, this);
        this.reportReportGridview = this.reportGrid.getView();
        this.reportReportGridview.on('expandbody', this.expandRow, this);
    },
    handlegridonload: function(store, record, success, opts) {
        if (this.reportStore.getProxy().getReader().rawData.metaData != undefined) {
            var userPreferences = this.reportStore.getProxy().getReader().rawData.userPreferences;
            if (financialYearFromDate == undefined && financialYearToDate == undefined) {
                financialYearFromDate = userPreferences.fromdate;
                financialYearToDate = userPreferences.todate;

                this.fromDate.setValue(financialYearFromDate);
                this.toDate.setValue(financialYearToDate);
            }

            this.columns = this.reportStore.getProxy().getReader().rawData.metaData[0].columns;
            if (this.isReportReconfigure == false) {
                this.reportStore.group(getGroupingFields(this.columns));
                this.reportGrid.reconfigure(this.reportStore, createColumns(this.columns));
                this.isReportReconfigure = true;
            }
        } else {
            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), this.reportStore.getProxy().getReader().rawData.msg, Ext.Msg.INFO);
        }
    },
    expandRow: function(rowNode, record, body) {
        this.expandReportStore = Ext.create('Ext.data.Store', {
            id: 'idexpandreportStore',
            model: 'customReportStoreDataModel',
            autoLoad: false,
            proxy: {
                type: 'ajax',
                url: 'ACCCreateCustomReport/executeCustomReport.do',
                actionMethods : getStoreActionMethods(),
                reader: {
                    type: 'json',
                    rootProperty: "data",
                    keepRawData: true,
                    totalProperty: 'totalCount'
                },
                params: {
                    reportID: this.reportId,
                    deleted: false,
                    nondeleted: false,
                    pendingapproval: false,
                    showRowLevelFieldsflag: true
                }
            }
        });
        var billid = record.data.billid;
        this.expandReportStore.load({
            scope: this,
            params: {
                reportID: this.reportId,
                deleted: false,
                nondeleted: false,
                pendingapproval: false,
                showRowLevelFieldsflag: true,
                billid: billid
            },
            callback: function(records, operation, success) {
                if (success) {
                    var billid="";
                    var header="";
                    var reportID="";
                    var recordCount=record.data.recordCount;
                    var resObj = eval("(" + operation.getResponse().responseText + ")");
                    
                    if(resObj.data!='undefined' && resObj.data!=undefined&&resObj.columns!='undefined'&&resObj.columns!=undefined){
                        billid=resObj.data[0].billid;
                        reportID=resObj.reportID;//take the parent billid
                        var columns=resObj.columns;
                        if(columns.length!=0 )  {   //if row level column exist
                            header=setRowExpanderHTML(columns,records);
//                             body.innerHTML = header;
                           //Increasing the colspan of offsetparent of row expander div inorder to render properly.
                            if(Ext.get('ux-report-row-expander-box-'+billid+"-"+reportID+"-"+recordCount).dom.offsetParent){
                                Ext.get('ux-report-row-expander-box-'+billid+"-"+reportID+"-"+recordCount).dom.offsetParent.colSpan +=1;
                            }
                            Ext.get('ux-report-row-expander-box-'+billid+"-"+reportID+"-"+recordCount).setHtml(header);//setting html in rowbodytpl

                        }else {
                            noRecordMsg(billid,header,body,reportID,recordCount);
                        }
                    }else{
                        billid=resObj.billid;//take the parent billid
                        reportID=resObj.reportID;//take the parent billid
                        noRecordMsg(billid,header,body,reportID,recordCount);
                    }
                } else {
                    body.innerHTML = "<div style='width: 100%;min-width:1900px;margin-left:50px'><span class='gridHeader'>"+ExtGlobal.getLocaleText("account.common.nodatadisplay")+".</span></div>";
                }
                
                if (this.reportGrid.lockedGrid) {
                    this.reportGrid.lockedGrid.view.refreshSize();
                }
            }
        });
    },
    createAdvanceSearchComponent : function(){
        this.advanceSearchGrid = new Ext.advancedSearchComponent({
            hidden : true,
            moduleid : this.moduleid,
            advSearch: false,
            customerCustomFieldFlag: this.showCustomerCustomFieldFlag(this.moduleid),
            vendorCustomFieldFlag: this.showVendorCustomField(this.moduleid),
            lineLevelSearch:false
        });
        this.advanceSearchGrid.on("filterStore", this.filterStore, this);
        this.advanceSearchGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    },
    configurAdvancedSearch: function() {
        this.advanceSearchGrid.show();
        this.advanceSearchGrid.advSearch = true;
        this.advanceSearchGrid.getComboData();
        this.AdvanceSearchBtn.disable();
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.reportStore.proxy.extraParams.searchJson = null;
        this.reportStore.proxy.extraParams.moduleid = this.moduleid;
        this.reportStore.proxy.extraParams.filterConjuctionCriteria = null;

        this.reportStore.loadPage(1);
        this.advanceSearchGrid.hide();
        this.AdvanceSearchBtn.enable();
    },
    filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.reportStore.proxy.extraParams.searchJson = this.searchJson;
        this.reportStore.proxy.extraParams.moduleid = this.moduleid;
        this.reportStore.proxy.extraParams.filterConjuctionCriteria = filterConjuctionCriteria;
          
        this.reportStore.loadPage(1);
    },
    showCustomerCustomFieldFlag: function(moduleid){
        var moduleId = parseInt(moduleid);
        var customerCustomFieldFlag = false;
        if(moduleId===Ext.Acc_Invoice_ModuleId || moduleId===Ext.Acc_Sales_Order_ModuleId || moduleId===Ext.Acc_Customer_Quotation_ModuleId 
        || moduleId===Ext.Acc_FixedAssets_DisposalInvoice_ModuleId || moduleId===Ext.Acc_ConsignmentRequest_ModuleId || moduleId===Ext.Acc_ConsignmentInvoice_ModuleId
        || moduleId===Ext.Acc_Lease_Quotation || moduleId===Ext.Acc_Lease_Order || moduleId===Ext.LEASE_INVOICE_MODULEID){
            customerCustomFieldFlag = true;
        }
        return customerCustomFieldFlag;
    },
  
    showVendorCustomField: function(moduleid){
        var moduleId = parseInt(moduleid);
        var vendorCustomFieldFlag = false;
        if(moduleId===Ext.Acc_Vendor_Invoice_ModuleId || moduleId===Ext.Acc_Purchase_Order_ModuleId || moduleId===Ext.Acc_Vendor_Quotation_ModuleId
            || moduleId===Ext.Acc_FixedAssets_Vendor_Quotation_ModuleId || moduleId===Ext.Acc_FixedAssets_Purchase_Order_ModuleId 
            || moduleId===Ext.Acc_FixedAssets_PurchaseInvoice_ModuleId || moduleId===Ext.Acc_ConsignmentVendorRequest_ModuleId
            || moduleId===Ext.Acc_Consignment_GoodsReceipt_ModuleId || moduleId===Ext.Acc_Purchase_Requisition_ModuleId
            || moduleId===Ext.Acc_RFQ_ModuleId){
            vendorCustomFieldFlag = true;
        }
        return vendorCustomFieldFlag;
    }
});

function noRecordMsg(billid,header,body,reportID,recordCount) {
      Ext.get('ux-report-row-expander-box-'+billid+"-"+reportID+"-"+recordCount).setHtml("<div style='width: 100%;min-width:1900px;margin-left:50px'><span class='gridHeader'>There are no records to display.</span></div>");//setting html in rowbodytpl
}

function  createColumns(jarray) {
    var columns = [], i = 0;
    var globalColumnsCnt=0;
    
    for(i=0;i<jarray.length;i++){
        if (!jarray[i].showasrowexpander)
            globalColumnsCnt++; 
    }
    if(globalColumnsCnt == 0){
        Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.info"),ExtGlobal.getLocaleText("acc.common.selectAtleastoneGlobalColumn"), Ext.Msg.INFO);
    }else{
        columns.push({
            xtype: 'rownumberer',
            autoLock:false
        });
        for (i = 0; i < jarray.length; i++) {
            var filter='';
            var flex = globalColumnsCnt > 6 ? "":1,
                align = jarray[i].properties.source.align ? jarray[i].properties.source.align.toLowerCase():"",
                displayName = jarray[i].displayName,
                dataIndex = jarray[i].id,
                xtype = parseInt(jarray[i].xtype) === Ext.fieldType.numberField ? 'numbercolumn' : '',
                summaryType = jarray[i].summaryType !== undefined ? jarray[i].summaryType : '',
                rendrer = Ext.getColumnRenderer(jarray[i]),
                summaryRendrer = summaryType == "count" ? "" : Ext.getColumnRenderer(jarray[i]),
                showasrowexpander = jarray[i].showasrowexpander,
                isMeasureItem = jarray[i].isMeasureItem !== undefined ? jarray[i].isMeasureItem : '';
                if(!isMeasureItem) {
                   filter = Ext.getFilterType(jarray[i]);
                } 
            
            if (!showasrowexpander && displayName!="Discountispersent") {
                columns.push({
                    text: displayName,
                    align :align,
                    dataIndex: dataIndex,
                    xtype: xtype,
                    width: 220,
                    cellWrap: true,
                    flex: flex,
                    summaryType: summaryType,
                    renderer: rendrer,
                    summaryRenderer: summaryRendrer,
                    filter: filter
                });
            }//end of lineitem  
        }
    }
    return columns;
}

function getGroupingFields(columns) {
    var groupFields = "", i = 0;
    for (i = 0; i < columns.length; i++) {
        if (columns[i].isgrouping) {
            groupFields = columns[i].id
        }
    }
    return groupFields;
}

/*Setting Row EXpander Html for it*/
function setRowExpanderHTML(columns, records) {
    var arr = [];
    this.serialNumber = 0;
    var arrayLength = columns.length + 1;
    var width=(arrayLength * 100)+ 250;
    var widthInPercent = (100 / arrayLength);

    /*Header Section*/
    var gridHeaderText = ExtGlobal.getLocaleText("acc.common.ProductList");
    var header = "<span class='gridHeader'>" + gridHeaderText + "</span>";   //Product List
    header += "<div style='display:table !important;width:" + width + "px'>";
    header += "<span class='gridNo' style='font-weight:bold;'>S.No.&nbsp;&nbsp;&nbsp;</span>";

    for (var i = 0; i < columns.length; i++) {
        if(columns[i].defaultHeader!="discountispercent"){
            var alignTo = columns[i].properties.source.align ? columns[i].properties.source.align.toLowerCase():"left";
            header += "<span class='headerRow' style='width:"+widthInPercent+"%;text-align:"+alignTo+";'>" + columns[i].displayName + "&nbsp;</span>";
            arr.push(columns[i].id);
        }
    }
    header += "</div>";

    //Values Section
    header += "<div style='width:"+width+"px;'><span class='gridLine'></span></div>";
    for (var i = 0; i < records.length; i++) {
        header += " <div style='width:"+width+"px;display:table !important;'>";
        header += "<span class='gridNo'>" + (++this.serialNumber) + ".</span>";
        var recordData = records[i].data;
        for (var j = 0; j < arr.length; j++) {
            var recordvalue = recordData[arr[j]];
            var recordColumn = columns.getIemtByParam({id: arr[j]});
            if (recordvalue == undefined || recordvalue == '') {
                recordvalue = '';
            }
            if (recordColumn.xtype == Ext.fieldType.numberField) {
                recordvalue = Ext.util.Format.number(recordvalue, Ext.getColumnRendererFormat(recordColumn));
                
                var rendererProperty = recordColumn.properties.source.renderer;
                if((recordData.discountispercent=="1"||recordData.discountispercent=="T") && recordColumn.defaultHeader=="Discount"){
                    recordvalue = recordvalue + "%" ;
                }else if(rendererProperty== "Transaction Currency" && recordvalue!=""){     // Transaction level currency renderer
                    recordvalue = recordData.currencysymbol + " " + recordvalue
                }else if(rendererProperty== "Base Currency" && recordvalue!=""){     // Base currency renderer
                    recordvalue = Ext.pref.CurrencySymbol + " " + recordvalue;
                }
            }
            alignTo = recordColumn.properties.source.align ? recordColumn.properties.source.align.toLowerCase():"left";
            header += "<span class='gridRow' style='width:" + widthInPercent + "%;text-align:"+alignTo+";' data-qtip ='"+recordvalue+"'>" + Ext.util.Format.ellipsis(recordvalue.replace(/(<([^>]+)>)/ig,''),30) + "&nbsp;</span>";
        }
        header += "</div>";
    }
    var disHtml = "<div class='expanderContainer'>" + header + "</div>";
    return disHtml;
}

function getRecordsforExpander(isrowexpander) {
    var elements = [];
    if (isrowexpander) {
        for (var i = 0; i < recordsToDrop.length; i++) {
            var data = recordsToDrop[i];
            if (data.showasrowexpander) {
                elements.push(data);
            }
        }
    } else {
        for (var i = 0; i < recordsToDrop.length; i++) {
            var data = recordsToDrop[i];
            if (!data.showasrowexpander) {
                elements.push(data);
            }
        }
    }
    return elements;
}

function createNewReportTab(id, title, reportid ,moduleid) {
    var customReportListPanel = Ext.getCmp('customReportListTabPanel');
    var isTabExists = customReportListPanel.getChildByElement(id);
    if (isTabExists) {
        customReportListPanel.setActiveTab(id);
    } else {
        var newTab;
        if (id == 'idnewcustomreporttab') {
            newTab = Ext.create('Ext.ReportBuilderTab', {
                id: id,
                title: ExtGlobal.getLocaleText("acc.common.createnewCustomReport"),
                tooltip: ExtGlobal.getLocaleText("acc.common.createnewCustomReport"),
                closable: true,
                iconCls: 'accountingbase create-report'
            });
        } else {
            newTab = Ext.create('Ext.ReportTab', {
                id: id,
                title: title,
                tooltip: title,
                closable: true,
                reportId: reportid,
                moduleid: moduleid,
                iconCls: 'accountingbase new-report'
            });
        }
        customReportListPanel.add(newTab).show();
    }
}

Ext.define('customReportListTabPanel', {
    extend: 'Ext.tab.Panel',
    fullscreen: true,
    id: 'customReportListTabPanel',
    defaults: {
        styleHtmlContent: true
    },
    items: [
        Ext.create('Ext.ReportListTab', {
            title:  ExtGlobal.getLocaleText("acc.common.CustomReports"),
            iconCls: 'pwnd report',
            tooltip: ExtGlobal.getLocaleText("acc.common.CustomReports"),
            layout: 'fit'
        })
    ]
});


Ext.SaveReportWindow = function(config) {
    Ext.apply(this, config);
    this.form = new Ext.form.FormPanel({
        padding: '15 15 15 15',
        bodyStyle: "background: transparent;",
        border: false,
        style: "background: transparent;padding:20px;",
        labelWidth: 165,
        items: [{
                fieldLabel: ExtGlobal.getLocaleText("acc.common.ReportName")+"*",
                xtype: 'textfield',
                name: 'reportName',
                labelWidth: 165,
                width: 385,
                maxLength: 50,
                allowBlank: false,
                validateBlank: true,
                vtype: 'reportnamevtype'
            },
            {
                fieldLabel:ExtGlobal.getLocaleText("acc.common.ReportDescription"),
                name: 'reportDesc',
                xtype: 'textarea',
                labelWidth: 165,
                width: 385,
                maxLength: 255
            }]
    });

    Ext.SaveReportWindow.superclass.constructor.call(this, {
        title: ExtGlobal.getLocaleText("acc.common.saveReport"),
        modal: true,
        iconCls: "pwnd favwinIcon",
        width: 540,
        height: 290,
        resizable: false,
        closable: false,
        layout: 'border',
        buttonAlign: 'right',
        items: [{
                region: 'north',
                height: 75,
                bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                html: getTopHtmlReqField( ExtGlobal.getLocaleText("acc.common.saveReport"),  ExtGlobal.getLocaleText("acc.common.saveReport"), '../../images/save.png', 'HTML code and "\\\" character are not allowed')
            }, {
                region: 'center',
                border: false,
                bodyStyle: 'background:#f1f1f1;font-size:10px;border-top:1px solid #bfbfbf;',
                autoScroll: true,
                items: [this.form]
            }],
        buttons: [{
                text: ExtGlobal.getLocaleText("acc.common.saveBtn"), //'Save',,
                itemId: 'save',
                scope: this,
                handler: function() {
                    if (this.form.isValid()) {
                        this.form.submit({
                            url: 'ACCCreateCustomReport/saveCustomReport.do',
                            scope: this,
                            params: {
                                selectedRows: JSON.stringify(recordsToDrop),
                                moduleCategory: this.moduleCatId,
                                moduleID: this.moduleId,
                                deleted: false,
                                nondeleted: false,
                                pendingapproval: false
                            },
                            success: function(req, res) {
                                var resObj = eval("(" + res.response.responseText + ")");
                                if (resObj.success == "true") {
                                    this.close();
                                    Ext.CustomMsg('Success', resObj.msg, Ext.Msg.INFO);
                                    Ext.getCmp("idreportlistgrid").getStore().reload();
                                } else {
                                    Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), resObj.msg, Ext.Msg.INFO);
                                }
                            },
                            failure: function() {
                                Ext.CustomMsg('Error',ExtGlobal.getLocaleText("acc.common.errorOccuratServerSide"), Ext.Msg.ERROR);
                            }
                        })
                    } else { // display error alert if the data is invalid
                        Ext.CustomMsg('Invalid Data', ExtGlobal.getLocaleText("acc.common.providevalidData"), Ext.Msg.INFO);
                    }
                }
            },
            {
                text: ExtGlobal.getLocaleText("acc.common.cancelBtn"),
                itemId: 'cancel',
                scope: this,
                handler: function() {
                    this.hide();
                }
            }]
    });
}

Ext.extend(Ext.SaveReportWindow, Ext.Window, {
    onRender: function(config) {
        Ext.SaveReportWindow.superclass.onRender.call(this, config);
    }
});

Ext.onReady(function() {   // On ready Function. Loads on document loading
    Ext.tip.QuickTipManager.init();
    Ext.apply(Ext.tip.QuickTipManager.getQuickTip(), {
        dismissDelay: 0
    });
    var customReportList = Ext.create('customReportListTabPanel');
    var rootCenter = {
        xtype: "panel",
        id: "rootCenter",
        region: "center",
        layout: 'fit',
        height: '100%',
        autoHeight: true,
        autoScroll: true,
        border:false,
        items: [customReportList]
    };
    var topPanel = {
        xtype: "panel",
        region: "north",
        id: 'idtopPanel',
        border:false,
//        height: 30,
        bodyStyle:"background:transparent",
//        bodyCls: 'app-header',
        width: "100%",
        contentEl:"header"
//        html: "<div class = 'app-header-data'>Custom Report Builder</div>"
    };
    var viewport = Ext.create('Ext.Viewport', {
        id: 'reportViewport',
        layout: 'border',
        resizable: false,
//        scrollable: true,
        items: [rootCenter, topPanel],
        renderTo: Ext.getBody()
    });
});


