/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.define('ReportBuilder.view.CreateNewReport', {
    extend: 'ReportBuilder.extension.ClosablePanel',
    xtype: 'createnewreport',
    autoHeigth: true,
    layout: 'border',
    frame: false,
    selectedSortOrders : [],
    selectedSortSequences : [],
    requires: [
        'ReportBuilder.extension.ClosablePanel',
        'ReportBuilder.view.SaveReportWin',
        'ReportBuilder.extension.CustomListFilter',
        'ReportBuilder.model.FieldsModel',
        'ReportBuilder.store.ModuleCategoryStore',
        'ReportBuilder.store.ModuleStore',
        'ReportBuilder.store.ReportPreviewStore',
        'ReportBuilder.view.FormulaBuilder',
        'ReportBuilder.view.ChartBuilder'
    ],

    initComponent: function() {
        var me = this;
        me.createFieldSelectionGrid();
        me.createFieldsPanel();
        me.createFieldsPropertyGrid();
        me.selectedSortOrders = [];
        me.selectedSortSequences = [];

        var previewItems;

        if (this.isPivot) {
            me.createReportPreviewPivotGrid();
            previewItems = this.reportPreviewPivotGrid;
        }else {
            me.createReportPreviewGrid();
            previewItems = this.reportPreviewGrid;
        }

        Ext.apply(me, {
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
                                    items: [previewItems]
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

        this.callParent(arguments);
    },
    //    onDestroy : function(){
    //        
    //        this.fieldSelectionGrid.destroy();
    //        this.reportPreviewGrid.destroy();
    //        this.fieldsPropertyGrid.destroy();
    //        this.fieldsGrid.destroy();
    //        this.moduleCategoryCombo.destroy();
    //        this.moduleCombo.destroy();
    //        
    //        this.callParent(arguments);
    //    },
    createFieldSelectionGrid: function() {
        var fieldSelectionStore = Ext.create('Ext.data.Store', {
            model: 'ReportBuilder.model.FieldsModel'
        });
        
        var bArr = [];

        var saveReportButton = Ext.create('Ext.Button',{
            text: ExtGlobal.getLocaleText("acc.common.saveBtn"),
            dock: 'bottom',
            itemId: 'save',
            iconCls: "pwnd save",
            tooltip:ExtGlobal.getLocaleText("acc.common.saveReport"),
            scope: this,
            handler: function () {
                if (this.moduleCombo.getValue() == Ext.Acc_Receive_Payment_ModuleId) {
                    this.showGLConfirmationBeforeSave(false);
                } else if (this.moduleCombo.getValue() == Ext.Acc_Vendor_Invoice_ModuleId || this.moduleCombo.getValue() == Ext.Acc_Purchase_Order_ModuleId) {
                    this.showExpenseTypeTransactionsConfirmationBeforeSave(false);
                } else {
                    this.saveReport(false, this.recordsToDrop);
                }
            }
        });
        bArr.push(saveReportButton);

        var saveAndCreateNewButton = Ext.create('Ext.Button',{
            text: ExtGlobal.getLocaleText("acc.field.SaveAndCreateNew"),
            dock: 'bottom',
            iconCls: "pwnd save",
            tooltip:ExtGlobal.getLocaleText("acc.field.SaveAndCreateNew")+" Report",
            scope: this,
            handler: function() {
                if (this.moduleCombo.getValue() == Ext.Acc_Receive_Payment_ModuleId) {
                    this.showGLConfirmationBeforeSave(true);
                } else if (this.moduleCombo.getValue() == Ext.Acc_Vendor_Invoice_ModuleId || this.moduleCombo.getValue() == Ext.Acc_Purchase_Order_ModuleId) {
                    this.showExpenseTypeTransactionsConfirmationBeforeSave(false);
                } else {
                    this.saveReport(true, this.recordsToDrop);
                }
            }
        });
        bArr.push(saveAndCreateNewButton);

        var showPreviewButton = Ext.create('Ext.Button',{
            text: ExtGlobal.getLocaleText("acc.common.showPreview"),
            dock: 'bottom',
            itemId: 'update',
            iconCls: "pwnd update",
            tooltip: ExtGlobal.getLocaleText("acc.common.showPreview"),
            scope: this,
            handler: function() {
                if(this.isPivot == true) {
                    this.fetchReportPreviewPivotGridData();
                } else {
                    this.fetchReportPreviewGridData();
                }
            }
        });
        bArr.push(showPreviewButton);

        var clearAllButton = Ext.create('Ext.Button', {
            text: ExtGlobal.getLocaleText("acc.common.clearAllFilter"),
            dock: 'bottom',
            itemId: 'reset',
            iconCls: "pwnd reset",
            tooltip: ExtGlobal.getLocaleText("acc.common.clearAllFilterToolTip"),
            scope: this,
            handler: function(){
                this.selectedSortOrders = [];
                this.selectedSortSequences = [];
                if(this.recordsToDrop.length > 0){
                    this.handleResetClick();
                }
            }
        });
        bArr.push(clearAllButton);

        this.createFormulaButton = Ext.create('Ext.Button', {
            text: ExtGlobal.getLocaleText("acc.common.createFormula"),//ExtGlobal.getLocaleText("acc.common.reset"),
            tooltip: ExtGlobal.getLocaleText("acc.common.createFormula"),//ExtGlobal.getLocaleText("acc.common.resetTT"),
            scope: this,
            iconCls:"pwnd create-new",
            handler: function() {
                this.saveFormulaWindow = Ext.create('ReportBuilder.view.FormulaBuilder', {
                    isSaveAndCreateNew : true,
                    moduleId: this.moduleCombo.getValue(),
                    moduleName:this.moduleCombo.getRawValue(),
                    moduleCategory: this.moduleCategoryCombo.getRawValue(),
                    fieldSelectionGrid:this.fieldSelectionGrid,
                    recordsToDrop: this.recordsToDrop
                });
                this.saveFormulaWindow.show();
            }
        });
        bArr.push(this.createFormulaButton);
        
        this.editFormulaButton = Ext.create('Ext.Button', {
            text: ExtGlobal.getLocaleText("acc.common.editFormula"),
            tooltip: ExtGlobal.getLocaleText("acc.common.editFormula"),
            scope: this,
            iconCls: "accountingbase menu-edit",
            disabled: true,
            handler: function() {
            var selected=this.fieldSelectionGrid.getSelectionModel().getSelected().items;
            var formulaIndex = this.fieldSelectionGrid.getStore().indexOf(selected[0]);
            this.saveFormulaWindow = Ext.create('ReportBuilder.view.FormulaBuilder', {
                    isSaveAndCreateNew : true,
                    moduleId: this.moduleCombo.getValue(),
                    moduleName:this.moduleCombo.getRawValue(),
                    moduleCategory: this.moduleCategoryCombo.getRawValue(),
                    fieldSelectionGrid:this.fieldSelectionGrid,
                    recordsToDrop: this.recordsToDrop,
                    formulaExpressionValue: selected[0].data.expressionValue,
                    formulaExpression:selected[0].data.expression,
                    measurefieldstoDrop:JSON.parse(selected[0].data.measurefieldjsonArray),
                    formulaName:selected[0].data.defaultHeader,
                    isEditFlag:true,
                    operatormeasurefieldsDrop:selected[0].data.operatormeasurefieldsDrop != undefined ? JSON.parse(selected[0].data.operatormeasurefieldsDrop) : undefined,
                    formulaIndex: formulaIndex
                });
                this.saveFormulaWindow.show();
            }
        });
        bArr.push(this.editFormulaButton);
       
      
        
        this.createChartButton = Ext.create('Ext.Button', {
            text: ExtGlobal.getLocaleText("acc.CustomReport.Chart.ConfigChartBtn"),
            tooltip: ExtGlobal.getLocaleText("acc.CustomReport.TT.Chart.ConfigChartBtn"),
            scope: this,
            iconCls: "accountingbase chart",
            disabled: this.isEditFlag ? false : true,
            handler: this.createChartBtnHandler
        });
        bArr.push(this.createChartButton);
        
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
                //            items:[saveReportButton,saveAndCreateNewButton,showPreviewButton,showPivotPreviewButton,clearAllButton,this.createFormulaButton]
            items : bArr
            }]
        
        var fieldSelectionGridColumns = [{  //Array containing columns for Field Selection Grid
                text: ExtGlobal.getLocaleText("acc.common.columnName"),
                dataIndex: 'defaultHeader',
                sortable: false,
                flex: 1,
                renderer: function (value, metaData, record) {
                    if (metaData != undefined && metaData.record != undefined && metaData.record.data != undefined && metaData.record.data.isforformulabuilder != undefined && metaData.record.data.isforformulabuilder) {
                        metaData.tdAttr = 'data-qtip="' + metaData.record.data.expressionValue + '"';
                    } else {
                        metaData.tdAttr = 'data-qtip="' + value + '"';
                    }
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
                renderer: function (value, metaData, record) {
                    var crossModuleName = record.data.moduleName.substring(record.data.moduleName.indexOf("-") + 2, record.data.moduleName.length);
                    var crossModuleColumnName = metaData.record.data.defaultHeader + " _ " + crossModuleName;//Appending module name after cross module field to avoid two fields with same names
                    if (!metaData.record.data.editDisplayNameFlag && record.data.allowcrossmodule === true) {//Rendering the cross module field's Display name with Module Name appended when dragged
                        metaData.tdAttr = 'data-qtip="' + crossModuleColumnName + '"';
                        metaData.record.data.displayName = crossModuleColumnName;
                        return crossModuleColumnName;
                    } else {
                        metaData.tdAttr = 'data-qtip="' + value + '"';
                        return value;
                    }
                }
            },
            {
                text: ExtGlobal.getLocaleText("acc.common.moduleName"),
                dataIndex: 'moduleName',
                sortable: false,
                flex: 1
                        //                renderer: function() {
                        //                    return Ext.getCmp("idModuleCombo").getRawValue();
                        //                }
            },
            {
                text: ExtGlobal.getLocaleText("acc.je.type"),
                dataIndex: 'xtype',
                sortable: false,
                flex: 1,
                renderer: function (val, cell, row, rowIndex, colIndex, ds) {
                    return ExtGlobal.getFieldType(val);
                }
            }];
        
        if (!this.isPivot) {//Add Applying Grouping, Show As Expander, and Summary type columns, only if report is not a pivot report (ERP-32495)
            fieldSelectionGridColumns.push({
                text: ExtGlobal.getLocaleText("acc.common.applyGrouping"),
                dataIndex: 'isgrouping',
                xtype: 'checkcolumn',
                sortable: false,
                align: 'center',
                defaultType: 'boolean',
                flex: 1,
                renderer:function (val, m, rec) {
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
            });
            fieldSelectionGridColumns.push({
                text: ExtGlobal.getLocaleText("acc.common.showasExpander"),
                dataIndex: 'showasrowexpander',
                xtype: 'checkcolumn',
                sortable: false,
                align: 'center',
                defaultType: 'boolean',
                flex: 1,
                renderer: function (val, m, rec) {
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
            });
            fieldSelectionGridColumns.push({
                text: ExtGlobal.getLocaleText("acc.common.summaryType"),
                dataIndex: 'summaryType',
                sortable: false,
                flex: 1,
                editor: new Ext.form.field.ComboBox({
                    typeAhead: true,
                    triggerAction: 'all',
                    //                    disabled: true,
                    store: [
                            ['None', 'None'],
                            ['Count', 'Count'],
                            ['Sum', 'Sum'],
                            ['Min', 'Min'],
                            ['Max', 'Max'],
                            ['Average', 'Average']
                    ]
                })
            });
        }
        fieldSelectionGridColumns.push({//Add remove column
            header: ExtGlobal.getLocaleText("acc.field.Remove"),
            width: 100,
            align: 'center',
            sortable: false,
            flex: 1,
            renderer: function (value, css, record, row, column, store) {
                return "<div style='margin: 0px auto 0px auto;' class='delete pwnd delete-gridrow'  title='" + ExtGlobal.getLocaleText("acc.common.delete") + "'></div>";
            }
        });
            
        this.fieldSelectionGrid = Ext.create('Ext.grid.Panel', {
            itemId: "idfieldselectiongrid",
            title:ExtGlobal.getLocaleText("acc.common.fieldSelectionRegion") ,
            //            forceFit: true,
            border: false,
            scope: this,
            autoHeight: true,
            split: true,
            store: fieldSelectionStore,
            plugins: new Ext.grid.plugin.CellEditing({
                clicksToEdit: 1,
                listeners: {
                    beforeedit: function(e, editor){
                        var showasexpander = editor.record.data.showasrowexpander != undefined ? editor.record.data.showasrowexpander:false;
                        if (editor.field == "summaryType" && showasexpander){
                            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), ExtGlobal.getLocaleText("acc.common.linesummarytype"), Ext.Msg.INFO);
                            return false;
                        }
                        if (editor.field == "displayName") {//Flag used in rendere for Display Name to check if the cell is being edited
                            editor.record.data.editDisplayNameFlag = true;
                        }
                    }
                }
            }),
            columns: fieldSelectionGridColumns,
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
        this.fieldSelectionGrid.on('beforeedit', this.getEditorForSummary, this);
        this.fieldSelectionGrid.on('selectionchange', this.loadPropertyGrid, this);
        this.fieldSelectionGrid.on('cellclick', this.handleCellClick, this);
    },
    getEditorForSummary : function( editor, context, eOpts){
        if(context.field == "summaryType"){
            var xtype = context.record.get('xtype');
            var numberStore = [
                ['None', 'None'],
                ['Count', 'Count'],
                ['Sum', 'Sum'],
                ['Min', 'Min'],
                ['Max', 'Max'],
                ['Average', 'Average']
            ]
            var otherStore = [
                ['None', 'None'],
                ['Count', 'Count']
            ]
            var store = xtype == 2 ? numberStore : otherStore;
            this.fieldSelectionGrid.down('[dataIndex=summaryType]').getEditor().setStore(store);
        }
    },
    saveReport: function(isSaveAndCreateNew,recordsToDrop) {
        var globalColumns=0;
        for(var i=0;i<this.recordsToDrop.length;i++){
            if (!this.recordsToDrop[i].showasrowexpander){
                globalColumns++;
            }
        }//end of for loop

        //saving json array
        this.filterArray =[];
        if(this.reportPreviewGrid!=undefined && this.reportPreviewGrid!="undefined" && this.reportPreviewGrid!=null && this.reportPreviewGrid.filters!=null&& this.reportPreviewGrid.filters!=undefined){
            if(this.reportPreviewGrid.getStore().isFiltered()){//check whether the store is filtered or not
                var recordheaders=this.reportPreviewGrid.store.getFilters().items;
                for(var j=0;j<recordheaders.length;j++){
                    var value=""; 
                    var recordsdetails=this.recordsToDrop.getIemtByParam({
                        id:recordheaders[j].getProperty()
                    })
                    if(recordsdetails.xtype==Ext.fieldType.dateField){
                        value=ExtGlobal.convertToGenericDate(recordheaders[j].getValue());
                    }else{
                        value=recordheaders[j].getValue();
                    }
                    var recordjson={
                        "property":recordheaders[j].getProperty(),
                        "value":value,
                        "operator":recordheaders[j].getOperator(),
                        "allowcrossmodule":recordsdetails.allowcrossmodule,
                        "crossJoinMainTable":recordsdetails.crossJoinMainTable
                    };
                    this.filterArray.push(recordjson);
                }
            }//end of filtered check
        }

        if(this.isPivot) {
            this.pivotConfig = {};
            if(this.reportPreviewPivotGrid && (this.reportPreviewPivotGrid.leftAxis.length > 0 || this.reportPreviewPivotGrid.topAxis.length > 0) && this.reportPreviewPivotGrid.aggregate.length > 0){
                this.pivotConfig.leftAxis = this.reportPreviewPivotGrid.leftAxis;
                this.pivotConfig.topAxis = this.reportPreviewPivotGrid.topAxis;
                this.pivotConfig.aggregate = this.reportPreviewPivotGrid.aggregate;
            } else {
                this.isPivotConfigInvalid = true;
            }
        }

        if (this.recordsToDrop.length == 0) {
            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), ExtGlobal.getLocaleText("acc.common.selectAtleastoneColumn"), Ext.Msg.INFO);
            return;
        } else if(globalColumns == 0){
            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), ExtGlobal.getLocaleText("acc.common.selectAtleastoneGlobalColumn"), Ext.Msg.INFO);
        } else if(this.isPivotConfigInvalid) {
            this.isPivotConfigInvalid = false;
            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), ExtGlobal.getLocaleText("acc.common.pivotConfigInvalid"), Ext.Msg.INFO);
        } else {
            this.saveOrUpdateReport(isSaveAndCreateNew)
        }
        this.createChartButton.enable();
    },
    
    saveOrUpdateReport : function(isSaveAndCreateNew){
        var selectedRowJson = {}
        selectedRowJson.columnConfig =this.recordsToDrop;
        if (this.moduleCombo.getValue() == Ext.Acc_Receive_Payment_ModuleId) {
            selectedRowJson.showGLFlag = this.showGLFlag;
        } else if (this.moduleCombo.getValue() == Ext.Acc_Vendor_Invoice_ModuleId || this.moduleCombo.getValue() == Ext.Acc_Purchase_Order_ModuleId) {            
            selectedRowJson.showExpenseTypeTransactionsFlag = this.showExpenseTypeTransactionsFlag;
        }
        if(this.isPivot){
            selectedRowJson.pivotConfig =this.pivotConfig;
        }
        //Added below code to fix cylic error issue while saving the edited report without clicking show preview button on UI
        // Removing the listener part which added on change event of the property grid 
        if (this.recordsToDrop != undefined) {
            for (var i = 0; i < this.recordsToDrop.length; i++) {
                if (this.recordsToDrop[i].filter != undefined) {
                    this.recordsToDrop[i].filter = undefined;
                }
                if (this.recordsToDrop[i].properties.sourceConfig != undefined && this.recordsToDrop[i].properties.sourceConfig.sortSequence != undefined) {
                    if (this.recordsToDrop[i].properties.sourceConfig.sortSequence.editor != undefined) {
                        if (this.recordsToDrop[i].properties.sourceConfig.sortSequence.editor.listeners != null) {
                            this.recordsToDrop[i].properties.sourceConfig.sortSequence.editor.listeners = {};
                        }
                    }
                }
            }
        }
        var selectedRow = JSON.stringify(selectedRowJson);
        
        var reportUrl = "";
        var isEdit= false;
        if(this.moduleCategoryCombo.getRawValue() == Ext.moduleCategoryType.Reports) {
            reportUrl = this.accFieldsStore.getProxy().getReader().rawData.reportUrl;
        }
        if(this.isEditFlag){
            isEdit=true;
        }
        Ext.Ajax.request({
            url: 'ACCCreateCustomReport/saveOrUpdateCustomReport.do',
            method:"POST",
            scope: this,
            params: {
                isEdit:isEdit,
                reportUrl: reportUrl,
                parentReportId: this.moduleCombo.getValue(),
                selectedRows: selectedRow,
                isPivot : this.isPivot,
                isEWayReport : this.isEWayReport,
                pivotConfig: this.pivotConfig,
                reportName: this.reportName,
                reportNo: this.reportNo,
                reportDesc: this.reportDescription,
                moduleCategory: this.moduleCategoryCombo.getValue(),
                moduleCategoryName: this.moduleCategoryCombo.getRawValue(),
                moduleID: this.moduleCombo.getValue(),
                deleted: false,
                nondeleted: false,
                pendingapproval: false,
                showGLFlag : this.showGLFlag,
                showExpenseTypeTransactionsFlag : this.showExpenseTypeTransactionsFlag,
                filter:JSON.stringify(this.filterArray)
            },
            success: function(res, req) {
                var resObj = eval("(" + res.responseText + ")");
                this.reportNo = resObj.data.reportNo;
                if (resObj.success == true) {
                    this.isClosable = true;
                    if(!isSaveAndCreateNew) {
                        Ext.CustomMsg('Success', resObj.msg, Ext.Msg.INFO);
                    } else {
                        Ext.Msg.alert('Success', resObj.msg, function(btn){
                            if (btn == 'ok'){
                                if (this.isEditFlag) {
                                    this.moduleCombo.enable();
                                    this.moduleCategoryCombo.enable();
                                    this.isEditFlag = undefined;    
                                }
                                this.createNewWindow = Ext.create('ReportBuilder.view.CreateNewReportWin', {
                                    newreportpanelscope : this,
                                    isSaveAndCreateNew : isSaveAndCreateNew
                                });
                                this.createNewWindow.show();
                            }
                        },this);
                    }
                    Ext.getCmp("idreportlistgrid").getStore().reload();
                } else {
                    Ext.Msg.alert('Success', resObj.msg, function(btn){
                        if (btn == 'ok'){
                            this.saveReportWindow = Ext.create('ReportBuilder.view.SaveReportWin', {
                                newreportpanelscope : this,
                                isSaveAndCreateNew : isSaveAndCreateNew
                            });
                            this.saveReportWindow.show();
                        }
                    },this);
                }
            },
            failure: function() {
                Ext.CustomMsg('Error',ExtGlobal.getLocaleText("acc.common.errorOccuratServerSide"), Ext.Msg.ERROR);
                this.unmask();
            }
        });
    },
 
    handleCellClick: function(view, cell, cellIndex, record, row, rowIndex, event) {
        var clickedDataIndex = view.panel.headerCt.getHeaderAtIndex(cellIndex).dataIndex;
        var clickedColumnName = view.panel.headerCt.getHeaderAtIndex(cellIndex).text;
        var clickedCellValue = record.get(clickedDataIndex);
        var fieldSelectionStore = this.fieldSelectionGrid.getStore();
        if (event.getTarget("div[class='delete pwnd delete-gridrow']")) {
            this.deleteRecord(record.get('id'));
        }
        if (clickedDataIndex == 'showasrowexpander') {
            if(record.data.isLineItem != 'undefined' && record.data.isLineItem != undefined && record.data.isLineItem){

                record.set(clickedDataIndex, clickedCellValue);
                var recordsDroped = this.recordsToDrop[rowIndex];
                recordsDroped.showasrowexpander = clickedCellValue;
                // If selected as row expander then clear applied summary type
                if(clickedCellValue){
                    fieldSelectionStore.getAt(rowIndex).set("summaryType", "");
                }
                //Show Note panel for updates
                this.hideNotePanel(false,false);
            }else{
                fieldSelectionStore.getAt(rowIndex).set("showasrowexpander", false)
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
        var index = this.selectedSortOrders.indexOf(record.data.properties.source["(Column Name)"]);
        if (index > -1) {
            this.selectedSortOrders.splice(index, 1);
            this.selectedSortSequences.splice(index, 1);
        }
    },
    createReportPreviewGrid: function() {
        this.reportPreviewStore = Ext.create('ReportBuilder.store.ReportPreviewStore');

        this.reportPreviewStore.on("beforeload", function() {
            this.reportPreviewStore.proxy.extraParams.moduleID = this.moduleCombo.getValue();
            this.filterArray =[];            
            if(this.recordsToDrop != undefined) {
                for (var i = 0; i < this.recordsToDrop.length; i++) {
                    if (this.recordsToDrop[i].filter != undefined) {
                        this.recordsToDrop[i].filter = undefined;
                    }
                    if (this.recordsToDrop[i].properties.sourceConfig != undefined && this.recordsToDrop[i].properties.sourceConfig.sortSequence != undefined) {
                        if (this.recordsToDrop[i].properties.sourceConfig.sortSequence.editor != undefined) {
                            if (this.recordsToDrop[i].properties.sourceConfig.sortSequence.editor.listeners != null) {
                                this.recordsToDrop[i].properties.sourceConfig.sortSequence.editor.listeners = {};
                            }
                        }
                    }
                }
            }
            if(this.applyFilterFlag) {
                var filterArr = [];
                if(this.filterJson != undefined && this.filterJson != null && this.filterJson != "") {
                    filterArr = JSON.parse(this.filterJson);
                }
                this.filterArray = filterArr;
                this.applyFilterFlag = undefined;
                this.reportPreviewGrid.getStore().setFilters(filterArr);
            } else {
                if(this.reportPreviewGrid!=undefined && this.reportPreviewGrid!="undefined" && this.reportPreviewGrid!=null && this.reportPreviewGrid.filters!=null&& this.reportPreviewGrid.filters!=undefined){
                if(this.reportPreviewGrid.getStore().isFiltered()){//check whether the store is filtered or not
                    var recordheaders=this.reportPreviewGrid.store.getFilters().items;
                    for(var j=0;j<recordheaders.length;j++){
                        var value=""; 
                        var recordsdetails=this.recordsToDrop.getIemtByParam({
                            id:recordheaders[j].getProperty()
                            })
                        if(recordsdetails!=undefined) {
                        if(recordsdetails.xtype==Ext.fieldType.dateField){
                            value=ExtGlobal.convertToGenericDate(recordheaders[j].getValue());
                        }else{
                            value=recordheaders[j].getValue();
                                }
                        var recordjson={
                            "property":recordheaders[j].getProperty(),
                            "value":value,
                            "operator":recordheaders[j].getOperator(),
                            "allowcrossmodule":recordsdetails.allowcrossmodule,
                            "crossJoinMainTable":recordsdetails.crossJoinMainTable
                                };
                                this.filterArray.push(recordjson);
                            }
                        }
                    }//end of filtered check
                }
            }
            this.reportPreviewStore.proxy.extraParams.selectedRows = JSON.stringify(this.recordsToDrop);
            this.reportPreviewStore.proxy.extraParams.deleted = false;
            this.reportPreviewStore.proxy.extraParams.nondeleted =  false;
            this.reportPreviewStore.proxy.extraParams.pendingapproval= false;
            this.reportPreviewStore.proxy.extraParams.showRowLevelFieldsflag = false;
            this.reportPreviewStore.proxy.extraParams.isLeaseFixedAsset = false;
            this.reportPreviewStore.proxy.extraParams.gcurrencyid = Ext.pref.Currencyid;
            this.reportPreviewStore.proxy.extraParams.consolidateFlag = false;
            this.reportPreviewStore.proxy.extraParams.filter=JSON.stringify(this.filterArray)
        }, this);
        var rowexpander = {
            ptype: 'rowexpander',
            rowBodyTpl : ['<div id="ux-preview-row-expander-box-{billid}-{recordCount}"></div>']
        };
        this.reportPreviewGrid = Ext.create('Ext.grid.Panel', {
            columns: [],
            bufferedRenderer:false,
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
                    ftype: 'groupingsummary',
                    startCollapsed : true
            },{ 
                ftype: 'summary',
                    dock: 'bottom'
                }],
            onClearFilters: function() {
                this.filters.clearFilters();
                this.store.clearFilter(true);
            }
        });

        this.reportPreviewGrid.on('lockcolumn',ExtGlobal.lockPluginColumns);
        this.reportPreviewGrid.on('unlockcolumn',ExtGlobal.unlockPluginColumns);

        this.reportPreviewGridview = this.reportPreviewGrid.getView();
        this.reportPreviewGridview.on('expandbody', this.expandRow, this);
    },
    createReportPreviewPivotGrid: function() {
        this.reportPreviewStore = Ext.create('ReportBuilder.store.ReportPreviewStore');

        this.reportPreviewStore.on("beforeload", function() {
            this.reportPreviewStore.proxy.extraParams.moduleID = this.moduleCombo.getValue();
            this.filterArray = [];
            if (this.recordsToDrop != undefined) {
                for (var i = 0; i < this.recordsToDrop.length; i++) {
                    if (this.recordsToDrop[i].filter != undefined) {
                        this.recordsToDrop[i].filter = undefined;
                    }
                    if (this.recordsToDrop[i].properties.sourceConfig.sortSequence != undefined) {
                        if (this.recordsToDrop[i].properties.sourceConfig.sortSequence.editor != undefined) {
                            if (this.recordsToDrop[i].properties.sourceConfig.sortSequence.editor.listeners != null) {
                                this.recordsToDrop[i].properties.sourceConfig.sortSequence.editor.listeners = {};
                            }
                        }
                    }
                }
            }
            this.reportPreviewStore.proxy.extraParams.selectedRows = JSON.stringify(this.recordsToDrop);
            this.reportPreviewStore.proxy.extraParams.deleted = false;
            this.reportPreviewStore.proxy.extraParams.nondeleted = false;
            this.reportPreviewStore.proxy.extraParams.pendingapproval = false;
            this.reportPreviewStore.proxy.extraParams.showRowLevelFieldsflag = false;
            this.reportPreviewStore.proxy.extraParams.isLeaseFixedAsset = false;
            this.reportPreviewStore.proxy.extraParams.gcurrencyid = Ext.pref.Currencyid;
            this.reportPreviewStore.proxy.extraParams.consolidateFlag = false;
        }, this);

        this.reportPreviewPivotGrid = Ext.create('Ext.pivot.Grid', {
            title: 'Report Data Pivot Preview Region ',
            height: 1000,
            width: 1000,
            enableGrouping: true,
            leftAxis: [],
            topAxis: [],
            plugins: [{
                ptype:      'pivotconfigurator',
                pluginId:   'configurator',
                    // It is possible to configure a list of fields that can be used to configure the pivot grid
                    // If no fields list is supplied then all fields from the Store model are fetched automatically
                    fields: [{}]
                }],
            dockedItems: [{
                    xtype: 'toolbar',
                    dock: 'bottom',
                    items: ["-", {
                            text: ExtGlobal.getLocaleText("acc.common.clearPreview"),
                            iconCls: "pwnd reset",
                            tooltip: {
                                text: ExtGlobal.getLocaleText("acc.common.clearPreview") + " Region",
                                mouseOffset: [0, -45]
                            },
                            scope: this,
                            handler: this.handleClearPreview
                        }]
                }],
            viewConfig: {
                trackOver: true,
                stripeRows: false
            },
            store: this.reportPreviewStore,
            selModel: {
                type: 'rowmodel'
            },
            matrixConfig: {
                store: this.reportPreviewStore
            }
        });
    },
    expandRow: function(rowNode, record, body) {
        this.expandPreviewStore = Ext.create('ReportBuilder.store.ReportPreviewStore');

        var billid = record.data.billid;
        var linkedbillid = record.data.linkedbillid;
        this.expandPreviewStore.load({
            scope:this,
            params: {
                selectedRows: JSON.stringify(this.recordsToDrop),
                moduleID: this.moduleCombo.getValue(),
                showRowLevelFieldsflag: true,
                billid: billid,
                linkedbillid : linkedbillid
            },
            callback: function(records, operation, success) {
                if (success) {
                    var arr = [];
                    this.serialNumber = 0;
                    var isDataPresent = false;
                    var resObj = eval("(" + operation.getResponse().responseText + ")");
                    var recordbillid=resObj.billid;
                    var columns = [];
                    var header= "";
                    var columnHeader="";

                    if(this.moduleCombo.getValue() == Ext.Acc_Make_Payment_ModuleId || this.moduleCombo.getValue() == Ext.Acc_Receive_Payment_ModuleId || this.moduleCombo.getValue() == Ext.Acc_Debit_Note_ModuleId || this.moduleCombo.getValue() == Ext.Acc_Credit_Note_ModuleId){
                        columns = resObj.columns;
                    } else{
                        for (var i = 0; i < this.recordsToDrop.length; i++) {
                            var data = this.recordsToDrop[i];
                            if (data.showasrowexpander) {
                                columns.push(data);
                            }
                        }
                    }

                    if (columns.length != 0) {   //if row level column exist
                        if(this.moduleCombo.getValue()== Ext.Acc_Debit_Note_ModuleId  || this.moduleCombo.getValue()== Ext.Acc_Credit_Note_ModuleId ){
//                            header = setRowExpanderForNotesHTML(columns, records,resObj);
                            if (columns.accountColumns != undefined && columns.accountColumns.length > 0) {
                                columnHeader = this.moduleCombo.getValue() == Ext.Acc_Debit_Note_ModuleId ? "Account Details" : "Account Details";
                                if (records[0].data.accountData != undefined && records[0].data.accountData.length > 0) {
                                    header += setRowExpanderHTML(columns.accountColumns, records[0].data.accountData, columnHeader, this.moduleCombo.getValue());
                                }
                                isDataPresent = true;
                            }
                            if (columns.invoiceColumns != undefined && columns.invoiceColumns.length > 0) {
                                columnHeader = this.moduleCombo.getValue() == Ext.Acc_Debit_Note_ModuleId ? "Invoice Details" : "Invoice Details";
                                if (records[0].data.invoiceData != undefined && records[0].data.invoiceData.length > 0) {
                                    header += setRowExpanderHTML(columns.invoiceColumns, records[0].data.invoiceData, columnHeader, this.moduleCombo.getValue());
                                }
                                isDataPresent = true;
                            }
                        } else if(this.moduleCombo.getValue() == Ext.Acc_Make_Payment_ModuleId || this.moduleCombo.getValue() == Ext.Acc_Receive_Payment_ModuleId){
                            if(columns.invoiceColumns.length > 0 && records[0].data.invoiceData.length >0){
                                columnHeader = this.moduleCombo.getValue() == Ext.Acc_Make_Payment_ModuleId ? "Payment Against Vendor Invoice":"Payment Against Customer Invoice";
                                header += setRowExpanderHTML(columns.invoiceColumns, records[0].data.invoiceData , columnHeader,this.moduleCombo.getValue());
                                isDataPresent = true;
                            }
                            if(columns.GLColumns.length > 0 && records[0].data.GLData.length > 0){
                                header += setRowExpanderHTML(columns.GLColumns, records[0].data.GLData , "Payment Against GL",this.moduleCombo.getValue());
                                isDataPresent = true;
                            }
                            if(columns.creditNoteColumns.length > 0 && records[0].data.creditNoteData.length > 0){
                                columnHeader = this.moduleCombo.getValue() == Ext.Acc_Make_Payment_ModuleId ? "Payment Against Credit Note":"Payment Against Debit Note";
                                header += setRowExpanderHTML(columns.creditNoteColumns, records[0].data.creditNoteData , columnHeader,this.moduleCombo.getValue());
                                isDataPresent = true;
                            }
                            if(columns.advancePaymentColumns.length > 0 && records[0].data.advancePaymentData.length >0){
                                header += setRowExpanderHTML(columns.advancePaymentColumns, records[0].data.advancePaymentData , "Advance Payment",this.moduleCombo.getValue());
                                isDataPresent = true;
                            }
                            if(columns.linkedInvoiceColumns.length > 0 && records[0].data.linkedInvoiceData.length > 0){
                                header += setRowExpanderHTML(columns.linkedInvoiceColumns, records[0].data.linkedInvoiceData ,"Used Advance Payment Against Invoices",this.moduleCombo.getValue());
                                isDataPresent = true;
                            }
                            if(columns.linkedCreditNoteColumns.length > 0 && records[0].data.linkedCreditNoteData.length > 0){
                                columnHeader = this.moduleCombo.getValue() == Ext.Acc_Make_Payment_ModuleId ? "Used Advance Payment Against Credit Note":"Used Advance Payment Against Debit Note";
                                header += setRowExpanderHTML(columns.linkedCreditNoteColumns, records[0].data.linkedCreditNoteData ,columnHeader,this.moduleCombo.getValue());
                                isDataPresent = true;
                            }
                            if(columns.refundColumns.length > 0 && records[0].data.refundData.length > 0){
                                header += setRowExpanderHTML(columns.refundColumns, records[0].data.refundData , "Refund/ Deposit",this.moduleCombo.getValue());
                                isDataPresent = true;
                            }
                            if(columns.linkedRefundColumns.length > 0 && records[0].data.linkedRefundData.length > 0){
                                header += setRowExpanderHTML(columns.linkedRefundColumns, records[0].data.linkedRefundData , "Used Refund/ Deposit Against Advance Payment",this.moduleCombo.getValue());
                                isDataPresent = true;
                            }
                            if(columns.loanColumns.length > 0 && records[0].data.loanData.length > 0){
                                header += setRowExpanderHTML(columns.loanColumns, records[0].data.loanData , "Payment Against Disbursement",this.moduleCombo.getValue());
                                isDataPresent = true;
                            }
                            if(!isDataPresent){
                                header = "<div style='width: 100%;min-width:1900px;margin-left:50px'><span class='gridHeader'>"+ExtGlobal.getLocaleText("account.common.nodatadisplay")+".</span></div>";
                            }
                        } else{ 
                            header = setRowExpanderHTML(columns, records,ExtGlobal.getLocaleText("acc.common.ProductList"),this.moduleCombo.getValue());
                        }
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
        var moduleCategoryStore = Ext.create('ReportBuilder.store.ModuleCategoryStore');
        var accModuleStore = Ext.create('ReportBuilder.store.ModuleStore');

        if(this.isPivot != undefined) { //to Disable Reports Category for non-pivot report
            moduleCategoryStore.on("beforeload", function() {
                moduleCategoryStore.proxy.extraParams = {
                    isPivot: this.isPivot
                }
            }, this);
        }

        moduleCategoryStore.on("load", function(store, record, success, opts) {
            if(this.isEditFlag) {
                this.recordsToDrop = [];
                var modulecatid = "";
                var modulecats = this.moduleCategoryCombo.getStore().getProxy().getReader().rawData.data;
                for (var i = 0; i < modulecats.length; i++) {
                    if (modulecats[i].moduleCatName === this.moduleCategory) {
                        modulecatid = modulecats[i].moduleCatId;
                    }
                }
                this.moduleCategoryCombo.setValue(modulecatid);
                this.applyFilterFlag = true;
                /** 
                 * isPivotPreviewCallFromEdit flag is used to handle pivot grid's reconfigure call when a pivot report is edited
                 * We need this flag because when a report is opened in edit mode we need to show report preview as it was in original report
                 * Therefore we must not reconfigure pivot when opening the report edit tab
                 */
                this.isPivotPreviewCallFromEdit = this.isPivot ? true : undefined;
            } else {
                this.moduleCategoryCombo.setValue(moduleCategoryStore.data.items[0].data.moduleCatId);
            }
            accModuleStore.load();
        }, this);

        accModuleStore.on("beforeload", function() {
            accModuleStore.proxy.extraParams = {
                moduleCatName: this.moduleCategoryCombo.getRawValue(),
                moduleCatIdValue: this.moduleCategoryCombo.getValue()
            }
        }, this);
        accModuleStore.on("load", function(store, record, success, opts) {
            if(this.isEditFlag) {
                this.moduleCombo.setValue(this.moduleId);
            } else {
                this.moduleCombo.setValue(accModuleStore.data.items[0].data.id);
            }
            this.accFieldsStore.load();
        }, this);

        moduleCategoryStore.load();



        this.moduleCategoryCombo = Ext.create('Ext.form.field.ComboBox', {
            store: moduleCategoryStore,
            fieldLabel: ExtGlobal.getLocaleText("acc.common.moduleCategory"),
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
                    accModuleStore.reload();
                },
                change : function( newValue, oldValue, eOpts) {
                    if (this.isEditFlag === true) {
                        this.moduleCategoryCombo.disable();
                    } else {
                        this.resetFieldsPanel();
                    }
                }
            }
        });

        this.moduleCategoryCombo.on("select",function(combo , record , eOpts){ //hide formula builder button for 'Reports' Module-Category
            if(combo.getRawValue() == Ext.moduleCategoryType.Reports){
                this.createFormulaButton.hide();
            } else {
                this.createFormulaButton.show();
            }
        },this);

        this.moduleCombo = Ext.create('Ext.form.field.ComboBox', {
            store: accModuleStore,
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
                },
                change : function( newValue, oldValue, eOpts) {
                    if (this.isEditFlag === true) {
                        this.moduleCombo.disable();
                    } else {
                        this.resetFieldsPanel();
                    }
                }
            }
        });
        this.accFieldsStore = Ext.create('Ext.data.Store', {
            id: 'accFieldsStoreId',
            model: 'ReportBuilder.model.FieldsModel',
            groupField: "columntype",
            timeout : 180000,
            //autoLoad : true,
            proxy: {
                type: 'ajax',
                url: 'ACCCreateCustomReport/getFiledsData.do',
                actionMethods : getStoreActionMethods(),
                reader: {
                    type: 'json',
                    keepRawData: true,
                    rootProperty: "data"
                }
            }
        });
        
        this.accFieldsStore.on("load", function() {
            if (this.isEditFlag === true) {
                var reportJsonWithPivotConfig = JSON.parse(this.reportjson);
                var reportJsonWithoutPivotConfig = [];
                if (this.isPivot) {
                    reportJsonWithoutPivotConfig = reportJsonWithPivotConfig.columnConfig;
                    var previewGrid = this.reportPreviewPivotGrid;
                    var previewStore = this.reportPreviewStore
                    var fieldsStore = this.fieldSelectionGrid.getStore();
                    
                    previewGrid.setStore(previewStore);
                    var headerArray = createPivotColumns(this.recordsToDrop,this.moduleCombo.getValue());
                    reportJsonWithPivotConfig.pivotConfig.leftAxis = ExtGlobal.setRenderersToPivotColumns(reportJsonWithPivotConfig.pivotConfig.leftAxis);
                    reportJsonWithPivotConfig.pivotConfig.aggregate = ExtGlobal.setRenderersToPivotColumns(reportJsonWithPivotConfig.pivotConfig.aggregate);
                    reportJsonWithPivotConfig.pivotConfig.topAxis = ExtGlobal.setRenderersToPivotColumns(reportJsonWithPivotConfig.pivotConfig.topAxis);
                    previewGrid.reconfigurePivot({
                        topAxis: reportJsonWithPivotConfig.pivotConfig.topAxis,
                        leftAxis: reportJsonWithPivotConfig.pivotConfig.leftAxis,
                        aggregate: reportJsonWithPivotConfig.pivotConfig.aggregate
                        
                    });
                    previewGrid.getPlugin("configurator").fields = Ext.Array.from(headerArray);
                    previewGrid.getPlugin("configurator").setDock("right");
                    
                } else {
                    reportJsonWithoutPivotConfig = reportJsonWithPivotConfig.columnConfig;
                }
                var reportJsonWithFilter = [];
                    if (this.filterJson != undefined && this.filterJson != null && this.filterJson != "") {
                        var filterJson = [];
                        filterJson = JSON.parse(this.filterJson);
                        for (var i = 0; i < reportJsonWithoutPivotConfig.length; i++) {
                            var columnFilterArr = [];
                            var columnJson = reportJsonWithoutPivotConfig[i];
                            for (var j = 0; j < filterJson.length; j++) {
                                if (columnJson.id == filterJson[j].property){
                                    columnFilterArr.push(filterJson[j]);
                                }
                            }
                            columnJson.filter = columnFilterArr;
                            reportJsonWithFilter.push(columnJson);
                        }
                    } else {
                        reportJsonWithFilter = reportJsonWithoutPivotConfig;
                    }
                this.fieldSelectionGrid.getStore().loadRawData(reportJsonWithFilter);
                this.updateColumnsArray();
                if(this.isPivot){
                    this.fetchReportPreviewPivotGridData();
                } else {
                    this.fetchReportPreviewGridData();
                }
            }
            this.fieldsGrid.fireEvent('storeloadsuccess',this);
        }, this);

        this.accFieldsStore.on("beforeload", function() {
            this.accFieldsStore.proxy.extraParams = {
                id: this.moduleCombo.getValue(),
                moduleCategory: this.moduleCategoryCombo.getRawValue()
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
                id: 'groupingFeature',
                groupHeaderTpl: '{name} {defaultHeader} ({rows.length} Item{[values.rows.length > 1 ? "s" : ""]})',
                enableGroupingMenu:false,
                startCollapsed : true
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
                        return ExtGlobal.getFieldType(val);
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
            dockedItems: dockedItems,
            listeners:{
                storeloadsuccess: function() {
                    this.view.getFeature('groupingFeature').startCollapsed = true;
          }
     }
        });
        this.fieldsGrid.on('render', function(editor, e, options) {
            var selfDDGroup = 'self-dd-zone-' + this.fieldsGrid.id;
            this.createDragZone(this.fieldsGrid, selfDDGroup);
            this.createDropZone(this.fieldsGrid, selfDDGroup);
        }, this);
    },
    
    resetFieldsPanel :function(){
        if(this.recordsToDrop != undefined && this.recordsToDrop.length > 0){
            this.handleResetClick(true);
        }
        this.handleClearPreview();
        this.searchTextField.setValue("");
        this.fieldsGrid.filters.clearFilters();
        if(this.isPivot == false) {
            this.reportPreviewGrid.filters.clearFilters();
        }
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
        this.recordsToDrop = [];
        for (var i = 0; i < items.length; i++) {
            //Added to fix ERP-35561;adding columnid explicitly as changed getGroupingFields method to fetch grouping field based on columnid;keeping id field was giving issue on UI
            var columnid = items[i].data.id;
            items[i].data.columnid=columnid;
            this.recordsToDrop.push(items[i].data);
        }
        this.hideNotePanel(showNotePanel,false);
    },
    handleResetClick: function(isHideNotePanel) {
        var store1 = this.fieldSelectionGrid.getStore();
        store1.removeAll()
        this.fieldsPropertyGrid.setSource({});// On Clear All empty the Property Panel from Property Panel
        store1.moduleNames =[];
        this.recordsToDrop = [];
    },
    handleClearPreview: function() {
        var reportPreviewStore;
        if(this.isPivot){
            this.reportPreviewPivotGrid.reconfigurePivot({
                topAxis: [],
                leftAxis: [],
                aggregate: []
            });
            this.reportPreviewPivotGrid.getPlugin("configurator").fields = [{}];
            this.reportPreviewPivotGrid.getPlugin("configurator").setDock("right");
        } else {
            var msg = ExtGlobal.getLocaleText("acc.CustomReport.reportPreviewEmptyText");
            this.setPreviewGridEmptyText(msg);
            reportPreviewStore = this.reportPreviewGrid.getStore();
            reportPreviewStore.proxy.extraParams.isClearPreview = true;
            reportPreviewStore.load();
            reportPreviewStore.totalCount = 0;
            this.reportPreviewGrid.reconfigure(reportPreviewStore, []);
        }
    },
    searchField: function() {
        var searchValue = this.searchTextField.getValue();
        this.fieldsGrid.columns[0].filter.setValue(searchValue);
    },
    setPreviewGridEmptyText: function (msg) {
        var normalGridView = this.reportPreviewGrid.normalGrid.view;
        normalGridView.emptyText = "<div class='x-grid-empty'>" + "<div style='text-align:center;font-size:16px;'>" + msg + "</div>" + "</div>";
    },
    
    fetchReportPreviewGridData: function() {
        var me = this;
        var previewGrid = me.reportPreviewGrid;
        var previewStore = this.reportPreviewStore;
        var fieldsStore = me.fieldSelectionGrid.getStore();

        previewStore.proxy.extraParams.isClearPreview = false;

        if(this.moduleCategoryCombo.getRawValue() == Ext.moduleCategoryType.Reports) {
            previewStore.proxy.url = this.accFieldsStore.getProxy().getReader().rawData.reportUrl;
            me.customizedReportPreviewStore = Ext.create('Ext.data.Store', {
                model: 'ReportBuilder.model.CommonModel',
                autoLoad: false,
                pageSize: reportPreviewPageSize,
                remoteFilter: true,
                proxy: {
                    type: 'ajax',
                    timeout : 180000,
                    url: 'ACCCreateCustomReport/executeCustomizedReportPreview.do',
                    actionMethods : getStoreActionMethods(),
                    reader: {
                        type: 'json',
                        rootProperty: "data",
                        keepRawData: true,
                        totalProperty: 'totalCount'
                    }
                }
            });
        } else {
            previewStore.proxy.url = 'ACCCreateCustomReport/executeCustomReportPreview.do';
        }

        if(fieldsStore.getRange().length > 0){
//            var msg = ExtGlobal.getLocaleText("acc.common.norecordstoDisplay");
//            this.setPreviewGridEmptyText(msg);
            var msg="";
            previewGrid.filters.clearFilters();
            previewStore.loadPage(1);
            previewStore.on("load", function(store, record, success, opts) {
                if (!success)
                {
                    var response = eval('(' + opts._response.responseText + ")");
                    msg = response.msg;
//                    Ext.CustomMsg('Error', ExtGlobal.getLocaleText("acc.common.errorOccuratServerSide"), Ext.Msg.ERROR);
                    Ext.CustomMsg('Error', msg, Ext.Msg.ERROR);

                } else {
                    msg = ExtGlobal.getLocaleText("acc.common.norecordstoDisplay");
                    this.setPreviewGridEmptyText(msg);
                }
                var elements = [];
                elements = getRecordsforExpander(false, this.recordsToDrop);
                previewStore.group(getGroupingFields(elements));
                if (me.moduleCategoryCombo.getRawValue() == Ext.moduleCategoryType.Reports) {
                    me.customizedReportPreviewStore.on("beforeload", function () {
                        me.customizedReportPreviewStore.proxy.extraParams.parentReportId = me.moduleCombo.getValue();
                        for (var i = 0; i < this.recordsToDrop.length; i++) {
                            if (this.recordsToDrop[i].properties.sourceConfig.sortSequence != undefined) {
                                if (this.recordsToDrop[i].properties.sourceConfig.sortSequence.editor != undefined) {
                                    if (this.recordsToDrop[i].properties.sourceConfig.sortSequence.editor.listeners != null) {
                                        this.recordsToDrop[i].properties.sourceConfig.sortSequence.editor.listeners = {};
                                    }
                                }
                            }
                        }
                        me.customizedReportPreviewStore.proxy.extraParams.selectedRows = JSON.stringify(this.recordsToDrop);
                        me.customizedReportPreviewStore.proxy.extraParams.deleted = false;
                        me.customizedReportPreviewStore.proxy.extraParams.nondeleted = false;
                        me.customizedReportPreviewStore.proxy.extraParams.pendingapproval = false;
                        me.customizedReportPreviewStore.proxy.extraParams.showRowLevelFieldsflag = false;
                        me.customizedReportPreviewStore.proxy.extraParams.isLeaseFixedAsset = false;
                        me.customizedReportPreviewStore.proxy.extraParams.gcurrencyid = Ext.pref.Currencyid;
                        me.customizedReportPreviewStore.proxy.extraParams.consolidateFlag = false
                        me.customizedReportPreviewStore.proxy.extraParams.isClearPreview = false;
                        me.customizedReportPreviewStore.proxy.extraParams.reportData = JSON.stringify(previewStore.getProxy().getReader().rawData.data.data);
                    }, this);
                    me.customizedReportPreviewStore.loadPage(1);
                    me.customizedReportPreviewStore.on("load", function (store, record, success, opts) {
                        var elements = [];
                        elements = getRecordsforExpander(false, this.recordsToDrop);
                        this.customizedReportPreviewStore.group(getGroupingFields(elements));
                    }, this);
                }
                //                this.sort(this.getProxy().getReader().rawData.sortConfigArray);          
            }, this);

            var headerArray = createColumns(this.recordsToDrop,me.moduleCombo.getValue());
            if(this.moduleCategoryCombo.getRawValue() == Ext.moduleCategoryType.Reports) {
                Ext.suspendLayouts();
                previewGrid.reconfigure(me.customizedReportPreviewStore, headerArray);
            } else {
                Ext.suspendLayouts();
                previewGrid.reconfigure(previewStore, headerArray);
            }
            Ext.resumeLayouts(true);
            me.hideNotePanel(true,false);
        }else{
            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), ExtGlobal.getLocaleText("acc.common.selectAtleastoneColumn"), Ext.Msg.INFO);
        }
    },
    fetchReportPreviewPivotGridData: function() {
        var me = this;
        var previewGrid = me.reportPreviewPivotGrid;
        var previewStore = this.reportPreviewStore
        var fieldsStore = me.fieldSelectionGrid.getStore();

        previewStore.proxy.extraParams.isClearPreview = false;

        if(this.moduleCategoryCombo.getRawValue() == Ext.moduleCategoryType.Reports) {
            previewStore.proxy.url = this.accFieldsStore.getProxy().getReader().rawData.reportUrl;
            me.customizedReportPreviewStore = Ext.create('Ext.data.Store', {
                model: 'ReportBuilder.model.CommonModel',
                autoLoad: false,
                pageSize: reportPreviewPageSize,
                remoteFilter: true,
                proxy: {
                    type: 'ajax',
                    timeout : 180000,
                    url: 'ACCCreateCustomReport/executeCustomizedReportPreview.do',
                    actionMethods : getStoreActionMethods(),
                    reader: {
                        type: 'json',
                        rootProperty: "data",
                        keepRawData: true,
                        totalProperty: 'totalCount'
                    }
                }
            });
        } else {
            previewStore.proxy.url = 'ACCCreateCustomReport/executeCustomReportPreview.do';
        }

        if (fieldsStore.getRange().length > 0) {
            previewStore.loadPage(1);
            var msg="";
            previewStore.on("load", function(store, record, success, opts) {
                if (!success)
                {
                    var response = eval('(' + opts._response.responseText + ")");
                    msg = response.msg;
                    Ext.CustomMsg('Error', msg, Ext.Msg.ERROR);
//                    Ext.CustomMsg('Error', ExtGlobal.getLocaleText("acc.common.errorOccuratServerSide"), Ext.Msg.ERROR);

                } else if(!this.isPivot){
                    msg = ExtGlobal.getLocaleText("acc.common.norecordstoDisplay");
                    this.setPreviewGridEmptyText(msg);
                }
                var elements = [];
                //elements = getRecordsforExpander(false);
                //this.group(getGroupingFields(elements));

                if(me.moduleCategoryCombo.getRawValue() == Ext.moduleCategoryType.Reports) {
                    me.customizedReportPreviewStore.on("beforeload", function() {
                        me.customizedReportPreviewStore.proxy.extraParams.parentReportId = me.moduleCombo.getValue();
                        for (var i = 0; i < this.recordsToDrop.length; i++) {
                            if (this.recordsToDrop[i].properties.sourceConfig.sortSequence != undefined) {
                                if (this.recordsToDrop[i].properties.sourceConfig.sortSequence.editor != undefined) {
                                    if (this.recordsToDrop[i].properties.sourceConfig.sortSequence.editor.listeners != null) {
                                        this.recordsToDrop[i].properties.sourceConfig.sortSequence.editor.listeners = {};
                                    }
                                }
                            }
                        }
                        me.customizedReportPreviewStore.proxy.extraParams.selectedRows = JSON.stringify(this.recordsToDrop);
                        me.customizedReportPreviewStore.proxy.extraParams.deleted = false;
                        me.customizedReportPreviewStore.proxy.extraParams.nondeleted =  false;
                        me.customizedReportPreviewStore.proxy.extraParams.pendingapproval= false;
                        me.customizedReportPreviewStore.proxy.extraParams.showRowLevelFieldsflag = false;
                        me.customizedReportPreviewStore.proxy.extraParams.isLeaseFixedAsset = false;
                        me.customizedReportPreviewStore.proxy.extraParams.gcurrencyid = Ext.pref.Currencyid;
                        me.customizedReportPreviewStore.proxy.extraParams.consolidateFlag = false
                        me.customizedReportPreviewStore.proxy.extraParams.isClearPreview = false;
                        me.customizedReportPreviewStore.proxy.extraParams.reportData = JSON.stringify(previewStore.getProxy().getReader().rawData.data.data);
                    }, this);

                    previewGrid.setStore(me.customizedReportPreviewStore);
                    me.customizedReportPreviewStore.loadPage(1);
                    me.customizedReportPreviewStore.on("load", function(store, record, success, opts) {     
                        var headerArray = createPivotColumns(this.recordsToDrop,me.moduleCombo.getValue());
                        previewGrid.getPlugin("configurator").fields = Ext.Array.from(headerArray);
                        previewGrid.getPlugin("configurator").setDock("right");
                        me.hideNotePanel(true, false);
                    }, this);
                } else {
                    previewGrid.setStore(previewStore);

                    var headerArray = createPivotColumns(this.recordsToDrop,me.moduleCombo.getValue());
                    
                    //When a report is edited, pivot grid is not to be configured when edit report tab opens
                    if (!this.isPivotPreviewCallFromEdit) {
                        previewGrid.reconfigurePivot({
                            topAxis: [],
                            leftAxis: [],
                            aggregate: []
                        });
                    } else {
                        //After opening the report in edit mode, make the flag undefined 
                        //so that pivot grid is reconfigured on each of the following preview calls
                        this.isPivotPreviewCallFromEdit = undefined;
                    }
                    previewGrid.getPlugin("configurator").fields = Ext.Array.from(headerArray);
                    previewGrid.getPlugin("configurator").setDock("right");

                    me.hideNotePanel(true, false);
                }
            },this);
        } else {
            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), ExtGlobal.getLocaleText("acc.common.selectAtleastoneColumn"), Ext.Msg.INFO);
        }
    },
    validateGroupingField: function(checked, rowIndex) {
        var recordsDroped = this.recordsToDrop[rowIndex];
        var fieldSelectionStore = this.fieldSelectionGrid.getStore();
        var check = false;
        if (!recordsDroped.isLineItem) {
            // Check if one of the column checkbox from grid is checked or not. 
            var recordcount = fieldSelectionStore.getRange().length;
            if (checked) {
                for (var i = 0; i < recordcount; i++) {
                    if (i != rowIndex) {
                        var active = fieldSelectionStore.getAt(i).get("isgrouping");
                        if (active == true) {
                            check = true;
                            break;
                        }
                    }
                }
                if (check === true) {
                    Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), ExtGlobal.getLocaleText("acc.common.groupingonOneField"), Ext.Msg.INFO);
                    fieldSelectionStore.getAt(rowIndex).set("isgrouping", false)
                } else {
                    recordsDroped.isgrouping = checked;
                    this.hideNotePanel(false,false);
                }
            } else {
                this.hideNotePanel(false,false);
            }
        } else {
            fieldSelectionStore.getAt(rowIndex).set("isgrouping", false)
        }
    },
    validateSummaryType: function(editor, e, options) {
        var isedited=false;
        var value = e.value!=undefined ? e.value:"";
        var originalValue = e.originalValue !=undefined ? e.originalValue:"";
        if (e.field == "summaryType") {
            if ((value == "Sum" || value == "Min" || value == "Max" || value == "Average") && (e.record.data.properties.source.renderer == "Transaction Currency")) {
                Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), ExtGlobal.getLocaleText("acc.reportbuilder.summaryinvalidcurrency"), Ext.Msg.INFO);
                e.record.set("summaryType", originalValue);
            } else if(value!="" && (value.toLowerCase() != originalValue.toLowerCase())){
                var recordsDroped = this.recordsToDrop[e.rowIdx];
                recordsDroped.summaryType = value ? value : "";
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
                    var isforformulabuilder = this.fieldSelectionGrid.getSelectionModel().getSelected().items[0].data.isforformulabuilder
                    if (e.record.get('name') == 'sortOrder' && isforformulabuilder) {    //Make property (name) non editable.
                        return false;
                    }
                },
                edit: function ( editor, e, eOpts ) {
                    if (e.originalValue != e.value) {
                        this.hideNotePanel(false,true);
                    }
                    if (e.record.get('name') == 'renderer') {
                        var summaryType = this.fieldSelectionGrid.getSelectionModel().getSelected().items[0].data.summaryType;
                        if((summaryType == "Sum" || summaryType == "Min" || summaryType == "Max" || summaryType == "Average") && e.value=="Transaction Currency"){
                            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"),ExtGlobal.getLocaleText("acc.reportbuilder.cutrrencyinvalidsummary"), Ext.Msg.INFO);
                            e.record.set('value',e.originalValue);
                        }
                    }

                    if (e.record.get('name') == 'sortOrder') {
                        var data = this.fieldSelectionGrid.getSelectionModel().getSelected().items[0].data;
                        var source = data.properties.source;
                        var sourceConfig = data.properties.sourceConfig;
                        if (e.value == "ASC" || e.value == "DESC")
                        {
                            if (e.originalValue == "None") {
                                for (var i = 0; i < this.recordsToDrop.length; i++) {
                                    if (this.recordsToDrop[i].defaultHeader == this.fieldSelectionGrid.getSelectionModel().getSelected().items[0].data.properties.source["(Column Name)"]) {
                                        break;
                                    }
                                }
                                source.sortSequence = i+1;
                            }
                            sourceConfig.sortSequence = {
                                "displayName": "Sorting  Sequence",
                                "editor": {
                                    xtype: "numberfield",
                                    minValue: 1,
                                    listeners: {
                                        scope: this,
                                        change: function(combo,newValue, oldValue, eOpts) {
                                            var addedIndex = this.selectedSortSequences.indexOf(newValue);
                                            if (addedIndex > -1) {
                                                if (this.selectedSortOrders[addedIndex] != data.properties.source["(Column Name)"]) {
                                                    Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), ExtGlobal.getLocaleText("acc.common.sortSeqMesureFieldAlertMsgPart1") + this.selectedSortOrders[addedIndex] + ExtGlobal.getLocaleText("acc.common.sortSeqMesureFieldAlertMsgPart2"), Ext.Msg.INFO);
                                                }
                                            }
                                        }
                                    }
                                }

                            };
                            this.fieldsPropertyGrid.setSource.apply(this.fieldsPropertyGrid, [source, sourceConfig]);
                            if (this.fieldSelectionGrid.getSelectionModel().getSelected().items[0].data.isMeasureItem == true) {
                                Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), ExtGlobal.getLocaleText("acc.common.sortOrderMesureFieldAlert"), Ext.Msg.INFO);
                            }
                            if (!(e.originalValue == "DESC" || e.originalValue == "ASC")) {
                                var addedIndex = this.selectedSortSequences.indexOf(data.properties.source.sortSequence);
                                if (addedIndex > -1) {
                                    Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), ExtGlobal.getLocaleText("acc.common.sortSeqMesureFieldAlertMsgPart1") + this.selectedSortOrders[addedIndex] + ExtGlobal.getLocaleText("acc.common.sortSeqMesureFieldAlertMsgPart2"), Ext.Msg.INFO);
                                } else {
                                    this.selectedSortOrders.push(data.properties.source["(Column Name)"]);
                                    this.selectedSortSequences.push(data.properties.source.sortSequence);
                                }
                            }
                        } else if (e.value == "None") {
                            source.sortSequence = {};
                            sourceConfig.sortSequence = {};
                            var index = this.selectedSortOrders.indexOf(data.properties.source["(Column Name)"]);
                            if (index > -1) {
                                this.selectedSortOrders.splice(index, 1);
                                this.selectedSortSequences.splice(index, 1);
                            }
                            this.fieldsPropertyGrid.setSource.apply(this.fieldsPropertyGrid, [source, sourceConfig]);
                        }
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
        
        if (selected.length == 1 && selected[0].data.isforformulabuilder) {
            this.editFormulaButton.enable();
        } else {
            this.editFormulaButton.disable();
        }
    },
    hideNotePanel : function(ishide,ispropertyupdate){
        this.isClosable = false;
        var notePanelContainer = this.fieldSelectionGrid.dockedItems.items.getIemtByParam({
            name: "notePanelContainer"
        });

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
    },
    
    createChartBtnHandler: function () {
        this.createChartWindow = Ext.create('ReportBuilder.view.ChartBuilder', {
            isCreateChart: true,
            parentObj : this,
            reportId: this.reportNo,
            fieldSelectionGrid: this.fieldSelectionGrid
        });
        this.createChartWindow.show();
    },
    showGLConfirmationBeforeSave: function (isSaveAndCreateNew) {
        var itemsArr = [];
        var showGLFlag = this.showGLFlag
        this.showGLCheckBox = new Ext.form.field.Checkbox({
            fieldLabel: ExtGlobal.getLocaleText("acc.common.customreport.showGLFieldCheckbox"),
            defaultType: 'checkboxfield',
            labelWidth: 350,
            value: showGLFlag,
            name: 'reportType'
        });
        itemsArr.push(this.showGLCheckBox);
        this.form = new Ext.form.FormPanel({
            padding: '15 15 15 15',
            bodyStyle: "background: transparent;",
            border: false,
            style: "background: transparent;padding:20px;",
            labelWidth: 150,
            items: itemsArr,
            align: 'center'
        });
        this.okBtn = Ext.create('Ext.Button', {
            text: 'OK',
            itemId: 'OK',
            scope: this,
            handler: function () {
                if (this.showGLCheckBox != undefined && this.showGLCheckBox.getValue() != undefined && this.showGLCheckBox.getValue() == true) {
                    this.showGLFlag = true;
                } else {
                    this.showGLFlag = false;
                }
                this.saveReport(isSaveAndCreateNew, this.recordsToDrop);
                this.GLPopUpWindow.destroy();
            }
        });
        this.cancelBtn = Ext.create('Ext.Button', {
            text: ExtGlobal.getLocaleText("acc.common.cancelBtn"),
            itemId: 'cancel',
            scope: this,
            handler: function () {
                this.GLPopUpWindow.destroy();
            }
        });
        this.GLPopUpWindow = Ext.create('Ext.window.Window', {
            title: ExtGlobal.getLocaleText("acc.common.customreport.showGLFieldMsgBoxTitle"),
            modal: true,
            iconCls: "pwnd favwinIcon",
            width: 450,
            height: 250,
            resizable: false,
            closable: false,
            constrain: true,
            layout: 'border',
            buttonAlign: 'center',
            items: [{
                    region: 'north',
                    height: 75,
                    bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                    html: getTopHtmlReqField(ExtGlobal.getLocaleText("acc.common.customreport.showGLFieldMsgBoxTitle"), ExtGlobal.getLocaleText("acc.field.CreateCustomReportList"), '../../images/save.png', 'HTML code and "\\\" character are not allowed')
                }, {
                    region: 'center',
                    border: false,
                    bodyStyle: 'background:#f1f1f1;font-size:10px;border-top:1px solid #bfbfbf;',
                    autoScroll: true,
                    items: [this.form]
                }],
            buttons: [this.okBtn, this.cancelBtn]
        });
        this.GLPopUpWindow.show();
    },
    showExpenseTypeTransactionsConfirmationBeforeSave: function (isSaveAndCreateNew) {
        var itemsArr = [];
        var showExpenseTypeTransactionsFlag = this.showExpenseTypeTransactionsFlag
        this.isExpenseTypeTransactionsCheckBox = new Ext.form.field.Checkbox({
            fieldLabel: ExtGlobal.getLocaleText("acc.common.customreport.showExpenseTypeTransactionsCheckbox"),
            defaultType: 'checkboxfield',
            labelWidth: 350,
            value: showExpenseTypeTransactionsFlag,
            name: 'reportType'
        });
        itemsArr.push(this.isExpenseTypeTransactionsCheckBox);
        this.form = new Ext.form.FormPanel({
            padding: '15 15 15 15',
            bodyStyle: "background: transparent;",
            border: false,
            style: "background: transparent;padding:20px;",
            labelWidth: 150,
            items: itemsArr,
            align: 'center'
        });
        this.okBtn = Ext.create('Ext.Button', {
            text: 'OK',
            itemId: 'OK',
            scope: this,
            handler: function () {
                if (this.isExpenseTypeTransactionsCheckBox != undefined && this.isExpenseTypeTransactionsCheckBox.getValue() != undefined && this.isExpenseTypeTransactionsCheckBox.getValue() == true) {
                    this.showExpenseTypeTransactionsFlag = true;
                } else {
                    this.showExpenseTypeTransactionsFlag = false;
    }
                this.saveReport(isSaveAndCreateNew, this.recordsToDrop);
                this.showExpenseTypeTransactionsCheckBoxPopUpWindow.destroy();
            }
});
        this.cancelBtn = Ext.create('Ext.Button', {
            text: ExtGlobal.getLocaleText("acc.common.cancelBtn"),
            itemId: 'cancel',
            scope: this,
            handler: function () {
                this.showExpenseTypeTransactionsCheckBoxPopUpWindow.destroy();
            }
        });
        this.showExpenseTypeTransactionsCheckBoxPopUpWindow = Ext.create('Ext.window.Window', {
            title: ExtGlobal.getLocaleText("acc.common.customreport.showExpenseTypeTransactionsMsgBoxTitle"),
            modal: true,
            iconCls: "pwnd favwinIcon",
            width: 450,
            height: 250,
            resizable: false,
            closable: false,
            constrain: true,
            layout: 'border',
            buttonAlign: 'center',
            items: [{
                    region: 'north',
                    height: 75,
                    bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                    html: getTopHtmlReqField(ExtGlobal.getLocaleText("acc.common.customreport.showExpenseTypeTransactionsMsgBoxTitle"), ExtGlobal.getLocaleText("acc.field.CreateCustomReportList"), '../../images/save.png', 'HTML code and "\\\" character are not allowed')
                }, {
                    region: 'center',
                    border: false,
                    bodyStyle: 'background:#f1f1f1;font-size:10px;border-top:1px solid #bfbfbf;',
                    autoScroll: true,
                    items: [this.form]
                }],
            buttons: [this.okBtn, this.cancelBtn]
        });
        this.showExpenseTypeTransactionsCheckBoxPopUpWindow.show();
    }
});
