/*
 * Copyright (C) 2017 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 * This creates a window on which we can configure a different charts with preview option
 */

Ext.define('ReportBuilder.view.ChartBuilder', {
    extend: 'Ext.window.Window',
    xtype: 'chartbuilder',
    requires: [
        'Ext.window.Toast'
    ],
    buttonAlign: 'left',
    initComponent: function () {
        this.createChartBuilderBtnArr();
        this.createChartListPanel();
        this.createChartPropertiesPanel();
        this.createChartConfigPanel();
        this.createChartPreviewPanel();
        if(this.isEdit) {
            this.loadChartRecord();
        }

        Ext.apply(this, {
            title: 'Chart Builder',
            modal: true,
            iconCls: 'pwnd favwinIcon',
            width: 1000,
            height: 600,
            resizable: false,
            closable: false,
            constrain: true,
            layout: 'border',
            buttonAlign: 'right',
            items: [
                {
                    region: 'west',
                    layout: 'fit',
                    items: [this.chartListGrid]
                },
                {
                    region: 'center',
                    layout: 'border',
                    border: false,
                    items: [
                        {
                            region: 'north',
                            layout: 'fit',
                            items: [this.chartPropertyPanel]
                        },
                        {
                            region: 'center',
                            layout: 'border',
                            border: false,
                            items: [
                                {
                                    region: 'west',
                                    layout: 'fit',
                                    items: [this.chartConfigPanel]
                                },
                                {
                                    region: 'center',
                                    layout: 'fit',
                                    items: [this.chartPreviewPanel]
                                }
                            ]
                        }

                    ]
                }
            ],
            buttons: this.bottomBtnArray
        });
        this.callParent(arguments);
    },
    //creates buttons shown on chart builder windows
    createChartBuilderBtnArr: function () {
        this.bottomBtnArray = new Array();
        
        this.resetBtn = Ext.create('Ext.button.Button', {
            text: ExtGlobal.getLocaleText('acc.common.reset'),
            itemId: 'resetBtn',
            style: 'margin-bottom:5px; margin-left:5px;',
            scope: this,
            disabled: this.isEdit ? true : false,
            handler: function () {
                this.resetToIntialState();
                this.saveBtn.setText(ExtGlobal.getLocaleText('acc.common.saveBtn'));
            }
        });
        this.bottomBtnArray.push(this.resetBtn);
        
        this.saveBtn = Ext.create('Ext.button.Button', {
            text: ExtGlobal.getLocaleText('acc.common.saveBtn'),
            itemId: 'save',
            scope: this,
            handler: this.saveChartDeatils
        });
        this.bottomBtnArray.push(this.saveBtn);

        this.closeBtn = Ext.create('Ext.button.Button', {
            text: ExtGlobal.getLocaleText('acc.common.close'),
            itemId: 'cancel',
            scope: this,
            handler: function () {
                this.close();
            }
        });
        this.bottomBtnArray.push(this.closeBtn);
    },
    
    createChartPropertiesPanel: function () {
        this.nameField = Ext.create('Ext.form.field.Text', {
            name: 'chartname',
            fieldLabel: ExtGlobal.getLocaleText('acc.CustomReport.Chart.chartPropertyPanel.ChartName'),
            allowBlank: false,
            maxLength: 255,
            width: 375,
            regex: /[a-zA-Z0-9]+/
        });

        this.chartTypeStore = Ext.create('Ext.data.Store', {
            fields: ['id', 'type'],
            data: [
                {
                    'id': "Bar",
                    'type': ExtGlobal.getLocaleText('acc.common.BarChart')
                },
                {
                    'id': "Line",
                    'type': ExtGlobal.getLocaleText('acc.common.LineChart')
                },
                {
                    'id': "Pie",
                    'type': ExtGlobal.getLocaleText('acc.common.PieChart')
                }
            ]
        });

        this.chartTypeCombo = Ext.create('Ext.form.ComboBox', {
            fieldLabel: ExtGlobal.getLocaleText('acc.CustomReport.Chart.chartPropertyPanel.ChartType'),
            store: this.chartTypeStore,
            queryMode: 'local',
            displayField: 'type',
            valueField: 'id',
            emptyText: 'Select chart type',
            allowBlank: false,
            forceSelection: true,
            width: 375,            
            listeners: {
                scope: this,
                select: this.chartTypeChangeListener
            }
        });

        this.titleFieldStore = this.getFieldStore();

        this.titleFieldCombo = Ext.create('Ext.form.ComboBox', {
            fieldLabel: ExtGlobal.getLocaleText('acc.CustomReport.Chart.chartPropertyPanel.TitleField'),
            store: this.titleFieldStore,
            queryMode: 'local',
            displayField: 'defaultHeader',
            valueField: 'id',
            emptyText: 'Select title field',
            allowBlank: false,
            scope: this,
            forceSelection: true,
            width: 375,
            listeners: {
                scope: this,
                select: this.titleFieldValidation
            }
        });

        this.valueFieldStore = this.getFieldStore();
        this.valueFieldStore.filterBy(this.isAcceptibleValueField);

        this.valueFieldCombo = Ext.create('Ext.form.ComboBox', {
            fieldLabel: ExtGlobal.getLocaleText('acc.CustomReport.Chart.chartPropertyPanel.ValueField'),
            store: this.valueFieldStore,
            queryMode: 'local',
            displayField: 'defaultHeader',
            valueField: 'id',
            emptyText: 'Select value field',
            allowBlank: false,
            scope: this,
            forceSelection: true,
            width: 375,
            listeners: {
                scope: this,
                select: this.valueFieldValidation
            }
        });

        this.groupbyStore = Ext.create('Ext.data.Store', {
            fields: ['groupby']
        });

        this.groupFieldCombo = Ext.create('Ext.form.ComboBox', {
            fieldLabel: ExtGlobal.getLocaleText('acc.CustomReport.Chart.chartPropertyPanel.GroupField'),
            store: this.groupbyStore,
            queryMode: 'local',
            displayField: 'groupby',
            valueField: 'groupby',
            emptyText: 'Select grouping function',
            allowBlank: false,
            forceSelection: true,
            width: 324
        });

        this.chartPropertyPanel = Ext.create('Ext.form.Panel', {
            title: ExtGlobal.getLocaleText('acc.CustomReport.Chart.chartPropertyPanel'),
            border: false,
            collapsible: true,
            height: 180,
            bodyStyle: 'padding:5px;',
            layout: 'fit',
            items: [
                {
                    xtype: 'fieldset',
                    style: 'padding:5px;',
                    layout: 'column',
                    items: [
                        {
                            columnWidth: 1,
                            border: false,
                            style: 'padding:1px;',
                            items: [this.nameField]
                        },
                        {
                            columnWidth: 1,
                            border: false,
                            style: 'padding:1px;',
                            items: [this.chartTypeCombo]
                        },
                        {
                            columnWidth: 1,
                            border: false,
                            style: 'padding:1px;',
                            items: [this.titleFieldCombo]
                        },
                        {
                            columnWidth: .55,
                            border: false,
                            style: 'padding:1px;',
                            items: [this.valueFieldCombo]
                        },
                        {
                            columnWidth: .45,
                            border: false,
                            style: 'padding:1px;',
                            items: [this.groupFieldCombo]
                        }
                    ]
                }
            ]
        });
    },
    
    createChartConfigPanel: function () {
//appearance panel config----------------------------------------------------------------------------
        var theme = [["light"], /*["dark"], ["black"],*/ ["patterns"], ["chalk"]];
        this.themeStore = new Ext.data.Store({fields: ['theme']});
        this.themeStore.loadData(theme);

        this.themeCombo = Ext.create('Ext.form.ComboBox', {
            store: this.themeStore,
            displayField: 'theme',
            valueField: 'theme',
            queryMode: 'local',
            emptyText: 'select theme for chart',
            allowBlank: false,
            forceSelection: true
        });

        var effect = [["elastic"], ["easeOutSine"], ["easeInSine"], ["bounce"]];
        this.effectStore = new Ext.data.Store({fields: ['effect']});
        this.effectStore.loadData(effect);

        this.animationCombo = Ext.create('Ext.form.ComboBox', {
            store: this.effectStore,
            displayField: 'effect',
            valueField: 'effect',
            queryMode: 'local',
            emptyText: 'select animation effect',
            allowBlank: false,
            forceSelection: true
        });

        var fontFamily = [['Arial'], ['Courier'], ['Georgia'], ['Helvetica'], ['monospace'], ['serif'], ['Times New Roman'], ['Verdana']];
        this.fontFamilyStore = new Ext.data.Store({fields: ['fontFamily']});
        this.fontFamilyStore.loadData(fontFamily);

        this.fontFamilyCombo = Ext.create('Ext.form.ComboBox', {
            store: this.fontFamilyStore,
            displayField: 'fontFamily',
            valueField: 'fontFamily',
            queryMode: 'local',
            emptyText: 'select font family',
            allowBlank: false,
            forceSelection: true
        });
        
        var balloonTextForPie = [
            ["[[title]] : [[value]] ([[percents]]%)"],
            ["[[title]] : <b>[[value]]</b> ([[percents]]%)"],
            ["<b>[[title]]</b> : [[value]] ([[percents]]%)"],
            ["<b>[[title]]</b> : <b>[[value]]</b> ([[percents]]%)"]
        ];
        
        this.balloonTextStoreForPie = new Ext.data.Store({fields: ['balloonText']});
        this.balloonTextStoreForPie.loadData(balloonTextForPie);

        this.balloonTextComboForPie = Ext.create('Ext.form.ComboBox', {
            store: this.balloonTextStoreForPie,
            displayField: 'balloonText',
            valueField: 'balloonText',
            listConfig: {
                minWidth: 260
            },
            queryMode: 'local',
            emptyText: 'select balloon text',
            allowBlank: false,
            forceSelection: true
        });
        
        this.appearancePropertyGridSource = {
            "theme": "light",
            "startEffect": "elastic",
            "startDuration": 1,
            "autoResize": true,
            "depth3D": 0,
            "fontFamily": "Arial",
            "fontSize": 12,
            "color": "#000000",
            "backgroundColor": "#FFFFFF",
            "backgroundAlpha": 100,
            "innerRadius": 0,
            "labelsEnabled": false,
            "outlineColor": "#FFFFFF",
            "outlineThickness": 0,
            "balloonText": "[[title]] : [[value]]"
        };
        
        this.appearancePropertyGrid = Ext.create('Ext.grid.property.Grid', {
            title: ExtGlobal.getLocaleText('acc.CustomReport.Chart.chartConfigPanel.Appearance'),
            scrollable: true,
            border: false,
            source: Ext.clone(this.appearancePropertyGridSource),
            sourceConfig: {
                theme: {
                    displayName: "Theme",
                    editor: this.themeCombo
                },
                startEffect: {
                    displayName: "Animation Effect",
                    editor: this.animationCombo
                },
                startDuration: {
                    displayName: "Start Duration"
                },
                autoResize: {
                    displayName: "Auto Resize"
                },
                depth3D: {
                    displayName: "Depth3D"
                },
                fontFamily: {
                    displayName: "Font Family",
                    editor: this.fontFamilyCombo
                },
                fontSize: {
                    displayName: "Font Size"
                },
                color: {
                    displayName: "Text Color",
                    editor: this.colorEditor.call(this, 'appearancePropertyGrid', 'color'),
                    renderer: this.colorRenderer
                },
                backgroundColor: {
                    displayName: "Background Color",
                    editor: this.colorEditor.call(this, 'appearancePropertyGrid', 'backgroundColor'),
                    renderer: this.colorRenderer
                },
                backgroundAlpha: {
                    displayName: "Background Alpha",
                    renderer: this.alphaIntegerRenderer
                },
                innerRadius: {
                    displayName: "Inner Radius (Pie)"
                },
                labelsEnabled: {
                    displayName: "Enable Labels (Pie)"
                },
                outlineColor: {
                    displayName: "Outline Color (Pie)",
                    editor: this.colorEditor.call(this, 'appearancePropertyGrid', 'outlineColor'),
                    renderer: this.colorRenderer
                },
                outlineThickness: {
                    displayName: "Outline Thickness (Pie)"
                },
                balloonText: {
                    displayName: "Balloon Text (Pie)",
                    editor: this.balloonTextComboForPie
                }
            },
            listeners: {
                scope: this,
                propertychange: function (source, recordId, value, oldValue, eOpts) {
                    this.showPreview();
                }
            }
        });
        
//legends panel config------------------------------------------------------------------------------
        var position = [['top'], ['right'], ['bottom'], ['left']];
        this.positionStore = new Ext.data.Store({fields: ['position']});
        this.positionStore.loadData(position);

        this.positionCombo = Ext.create('Ext.form.ComboBox', {
            store: this.positionStore,
            displayField: 'position',
            valueField: 'position',
            queryMode: 'local',
            emptyText: 'select legends position',
            allowBlank: false,
            forceSelection: true
        });

        var align = [['left'], ['center'], ['right']];
        this.alignStore = new Ext.data.Store({fields: ['align']});
        this.alignStore.loadData(align);

        this.alignCombo = Ext.create('Ext.form.ComboBox', {
            store: this.alignStore,
            displayField: 'align',
            valueField: 'align',
            queryMode: 'local',
            emptyText: 'select legends alignment',
            allowBlank: false,
            forceSelection: true
        });
        
        this.legendsPropertyGridSource = {
            "enabled": true,
            "position": "bottom",
            "align": "center",
            "useGraphSettings": false
        };

        this.legendsPropertyGrid = Ext.create('Ext.grid.property.Grid', {
            title: ExtGlobal.getLocaleText('acc.CustomReport.Chart.chartConfigPanel.legends'),
            scrollable: true,
            border: false,
            source: Ext.clone(this.legendsPropertyGridSource),
            sourceConfig: {
                enabled: {
                    displayName: "Enabled"
                },
                position: {
                    displayName: "Position",
                    editor: this.positionCombo
                },
                align: {
                    displayName: "Align",
                    editor: this.alignCombo
                },
                useGraphSettings: {
                    displayName: "Use Graph Settings"
                }
            },
            listeners: {
                scope: this,
                propertychange: function (source, recordId, value, oldValue, eOpts) {
                    this.showPreview();
                }
            }
        });
        
//title panel config-----------------------------------------------------------------------------------------        
        this.titlePropertyGridSource = {
            "alpha": 100,
            "bold": true,
            "color": "#000000",
            "size": 15,
            "text": "Chart Title"
        };
        
        this.titlePropertyGrid = Ext.create('Ext.grid.property.Grid', {
            title: ExtGlobal.getLocaleText('acc.CustomReport.Chart.chartConfigPanel.Titles'),
            scrollable: true,
            border: false,
            source: Ext.clone(this.titlePropertyGridSource),
            sourceConfig: {
                alpha: {
                    displayName: "Alpha",
                    renderer: this.alphaIntegerRenderer
                },
                bold: {
                    displayName: "Bold"
                },
                color: {
                    displayName: "Color",
                    editor: this.colorEditor.call(this, 'titlePropertyGrid', 'color'),
                    renderer: this.colorRenderer
                },
                size: {
                    displayName: "Size"
                },
                text: {
                    displayName: "Text"
                }
            },
            listeners: {
                scope: this,
                propertychange: function (source, recordId, value, oldValue, eOpts) {
                    this.showPreview();
                }
            }
        });
        
//graphs panel config----------------------------------------------------------------------------------------        
        var type = [["line"], ['column'], ['step'], ['smoothedLine']];
        this.typeStore = new Ext.data.Store({fields: ['type']});
        this.typeStore.loadData(type);

        this.typeCombo = Ext.create('Ext.form.ComboBox', {
            store: this.typeStore,
            displayField: 'type',
            valueField: 'type',
            queryMode: 'local',
            emptyText: 'select graph type',
            allowBlank: false,
            forceSelection: true
        });

        var bullet = [["Not Set"], ['square'], ['bubble'], ['diamond'], ['round'], ['triangleUp'], ['triangleDown'], ['triangleLeft'], ['triangleRight']];
        this.bulletStore = new Ext.data.Store({fields: ['bullet']});
        this.bulletStore.loadData(bullet);

        this.bulletCombo = Ext.create('Ext.form.ComboBox', {
            store: this.bulletStore,
            displayField: 'bullet',
            valueField: 'bullet',
            queryMode: 'local',
            emptyText: 'select bullet type',
            allowBlank: false,
            forceSelection: true
        });
        
        var balloonText = [
            ["[[category]] : [[value]]"],
            ["[[category]] : <b>[[value]]</b>"],
            ["[[title]] - [[category]] : <b>[[value]]</b>"],
            ["[[title]] - <b>[[category]]</b> : <b>[[value]]</b>"],
            ["[[category]] of [[title]] : <b>[[value]]</b>"]
        ];
        
        this.balloonTextStore = new Ext.data.Store({fields: ['balloonText']});
        this.balloonTextStore.loadData(balloonText);

        this.balloonTextCombo = Ext.create('Ext.form.ComboBox', {
            store: this.balloonTextStore,
            displayField: 'balloonText',
            valueField: 'balloonText',
            listConfig: {
                minWidth: 260
            },
            queryMode: 'local',
            emptyText: 'select balloon text',
            allowBlank: false,
            forceSelection: true
        });
        
        this.graphsPropertyGridSource = {
            "type": "column",
            "title": "Graph 1",
            "showBalloon": true,
            "balloonText": "[[category]] : [[value]]",
            "balloonColor": "#FF8000",
            "fillAlphas": 100,
            "fillColors": "#FF8000",
            "lineAlpha": 100,
            "lineThickness": 1,
            "lineColor": "#FF8000",
            "bullet": "",
            "bulletAlpha": 100,
            "bulletColor": "#FF8000",
            "bulletSize": 8
        };
        
        this.graphsPropertyGrid = Ext.create('Ext.grid.property.Grid', {
            title: ExtGlobal.getLocaleText('acc.CustomReport.Chart.chartConfigPanel.Graphs'),
            scrollable: true,
            border: false,
            source: Ext.clone(this.graphsPropertyGridSource),
            sourceConfig: {
                type: {
                    displayName: "Type",
                    editor: this.typeCombo
                },
                title: {
                    displayName: "Title"
                },
                showBalloon: {
                    displayName: "Show Balloon"
                },
                balloonText: {
                    displayName: "Balloon Text",
                    editor: this.balloonTextCombo
                },
                balloonColor: {
                    displayName: "Balloon Color",
                    editor: this.colorEditor.call(this, 'graphsPropertyGrid', 'balloonColor'),
                    renderer: this.colorRenderer
                },
                fillAlphas: {
                    displayName: "Fill Alphas",
                    renderer: this.alphaIntegerRenderer
                },
                fillColors: {
                    displayName: "Fill Colors",
                    editor: this.colorEditor.call(this, 'graphsPropertyGrid', 'fillColors'),
                    renderer: this.colorRenderer
                },
                lineAlpha: {
                    displayName: "Line Alpha",
                    renderer: this.alphaIntegerRenderer
                },
                lineThickness: {
                    displayName: "Line Thickness"
                },
                lineColor: {
                    displayName: "Line Color",
                    editor: this.colorEditor.call(this, 'graphsPropertyGrid', 'lineColor'),
                    renderer: this.colorRenderer
                },
                bullet: {
                    displayName: "Bullet",
                    editor: this.bulletCombo
                },
                bulletAlpha: {
                    displayName: "Bullet Alpha",
                    renderer: this.alphaIntegerRenderer
                },
                bulletColor: {
                    displayName: "Bullet Color",
                    editor: this.colorEditor.call(this, 'graphsPropertyGrid', 'bulletColor'),
                    renderer: this.colorRenderer
                },
                bulletSize: {
                    displayName: "Bullet Size"
                }
            },
            listeners: {
                scope: this,
                propertychange: function (source, recordId, value, oldValue, eOpts) {
                    this.showPreview();
                }
            }
        });
        
//category axis panel config----------------------------------------------------------------------------------------
        this.categoryAxisPropertyGridSource = {
            "position": "bottom",
            "title": "Category Title",
            "titleBold": true,
            "titleFontSize": 12,
            "titleColor": "#000000",
            "autoRotateAngle" : 0,
            "autoRotateCount": 1
        };
        
        this.categoryAxisPropertyGrid = Ext.create('Ext.grid.property.Grid', {
            title: ExtGlobal.getLocaleText('acc.CustomReport.Chart.chartConfigPanel.CategoryAxis'),
            scrollable: true,
            border: false,
            source: Ext.clone(this.categoryAxisPropertyGridSource),
            sourceConfig: {
                position: {
                    displayName: "Position",
                    editor: this.positionCombo
                },
                title: {
                    displayName: "Title"
                },
                titleBold: {
                    displayName: "Title Bold"
                },
                titleFontSize: {
                    displayName: "Title Font Size"
                },
                titleColor: {
                    displayName: "Title Color",
                    editor: this.colorEditor.call(this, 'categoryAxisPropertyGrid', 'titleColor'),
                    renderer: this.colorRenderer
                },
                autoRotateAngle: {
                    displayName: "Auto Rotate Angle"
                },
                autoRotateCount: {
                    displayName: "Auto Rotate Count",
                    editor: {disabled: true}
                }
            },
            listeners: {
                scope: this,
                propertychange: function (source, recordId, value, oldValue, eOpts) {
                    this.showPreview();
                }
            }
        });

//value axis panel config----------------------------------------------------------------------------------------
        var valueAxesType = [["numeric"], ['date']];
        this.valueAxesTypeStore = new Ext.data.Store({fields: ['type']});
        this.valueAxesTypeStore.loadData(valueAxesType);

        this.valueAxesTypeStore = Ext.create('Ext.form.ComboBox', {
            store: this.valueAxesTypeStore,
            displayField: 'type',
            valueField: 'type',
            queryMode: 'local',
            emptyText: 'select value Axes type',
            allowBlank: false,
            forceSelection: true
        });
        
        this.valueAxesPropertyGridSource = {
            "type": "numeric",
            "title": "Axis Title",
            "titleBold": true,
            "titleFontSize": 12,
            "titleColor": "#000000",
            "titleRotation": 270
        };
        
        this.valueAxesPropertyGrid = Ext.create('Ext.grid.property.Grid', {
            title: ExtGlobal.getLocaleText('acc.CustomReport.Chart.chartConfigPanel.ValueAxes'),
            scrollable: true,
            border: false,
            source: Ext.clone(this.valueAxesPropertyGridSource),
            sourceConfig: {
                type: {
                    displayName: "Type",
                    editor: this.valueAxesTypeStore
                },
                title: {
                    displayName: "Title"
                },
                titleBold: {
                    displayName: "Title Bold"
                },
                titleFontSize: {
                    displayName: "Title Font Size"
                },
                titleColor: {
                    displayName: "Title Color",
                    editor: this.colorEditor.call(this, 'valueAxesPropertyGrid', 'titleColor'),
                    renderer: this.colorRenderer
                },
                titleRotation: {
                    displayName: "Title Rotation"
                }
            },
            listeners: {
                scope: this,
                propertychange: function (source, recordId, value, oldValue, eOpts) {
                    this.showPreview();
                }
            }
        });

//general settings panel config----------------------------------------------------------------------------------------
        this.generalSettingsPropertyGridSource = {
            "export": false,
            "chartCursor": false,
            "chartScrollbar": false,
            "valueScrollbar": false,
            "usePrefixes": true,
            "precision": 0,
            "percentPrecision": 0,
            "decimalSeparator": "."
        };
        
        this.generalSettingsPropertyGrid = Ext.create('Ext.grid.property.Grid', {
            title: ExtGlobal.getLocaleText('acc.CustomReport.Chart.chartConfigPanel.GeneralSettings'),
            scrollable: true,
            border: false,
            source: Ext.clone(this.generalSettingsPropertyGridSource),
            sourceConfig: {
                export: {
                    displayName: "Export"
                },
                chartCursor: {
                    displayName: "Chart Cursor"
                },
                chartScrollbar: {
                    displayName: "Chart Scrollbar"
                },
                valueScrollbar: {
                    displayName: "Value Scrollbar"
                },
                usePrefixes: {
                    displayName: "Use Prefixes"
                },
                precision: {
                    displayName: "Precision"
                },
                percentPrecision: {
                    displayName: "Percent Precision"
                },
                decimalSeparator: {
                    displayName: "Decimal Separator"
                }
            },
            listeners: {
                scope: this,
                propertychange: function (source, recordId, value, oldValue, eOpts) {
                    this.showPreview();
                }
            }
        });

        this.chartConfigPanel = Ext.create('Ext.panel.Panel', {
            title: ExtGlobal.getLocaleText('acc.CustomReport.Chart.chartConfigPanel'),
            collapsible: true,
            collapseDirection: 'left',
            border: false,
            width: 250,
            layout: 'accordion',
            autoRender: true,
            items: [
                this.appearancePropertyGrid,
                this.graphsPropertyGrid,
                this.categoryAxisPropertyGrid,
                this.valueAxesPropertyGrid,
                this.legendsPropertyGrid,
                this.titlePropertyGrid,
                this.generalSettingsPropertyGrid
            ]
        });
    },
    
    createChartPreviewPanel: function () {
        this.chartPreviewPanel = Ext.create('Ext.panel.Panel', {
            id: 'chartPreviewPanel',
            title: "Preview Panel",
            border: false,
            layout: "fit"
        });
    },
    
    //create chart list panel having grid and store
    createChartListPanel: function () {
        this.chartDetailsStore = Ext.create('Ext.data.Store', {
            model: 'ReportBuilder.model.CommonModel',
            autoLoad: this.isEdit ? false : true,
            proxy: {
                type: 'ajax',
                url: 'ACCCreateCustomReport/getChartDetails.do',
                actionMethods: getStoreActionMethods(),
                extraParams: {
                    reportId: this.reportId
                },
                reader: {
                    type: 'json',
                    rootProperty: 'data'
                }
            }
        });

        this.chartListGrid = new Ext.create('Ext.grid.Panel', {
            title: ExtGlobal.getLocaleText('acc.CustomReport.Chart.chartListPanel.title'),
            collapsible: true,
            border: false,
            collapseDirection: 'left',
            width: 230,
            layout: 'fit',
            store: this.chartDetailsStore,
            columns: [
                {text: 'Name', dataIndex: 'name', flex: .6},
                {text: 'Type', dataIndex: 'chartType', flex: .15},
                {
                    text: 'Remove',
                    flex: .25,
                    hidden: this.isEdit ? true : false,
                    renderer: function () {
                        return "<div style='margin: 0px auto 0px auto;' class='delete pwnd delete-gridrow'  title='" + ExtGlobal.getLocaleText("acc.common.delete") + "'></div>";
                    }
                }
            ]
        });

        this.chartListGrid.getSelectionModel().on('select', this.chartListGridSelectionModel, this);
        this.chartListGrid.on('cellclick', this.handleCellClick, this);
    },
    
    //copy record from field selection grid store to value field store and title field store
    getFieldStore: function () {
        var store = new Ext.data.Store();
        if (!this.isEdit) {
            this.fieldSelectionGrid.getStore().each(function (record) {
                if (!record.data.showasrowexpander) {
                    store.add(record.copy());
                }
            });
        }

        return store;
    },
    
    //check whether selected value already selected in value field or not
    //return true or false
    titleFieldValidation: function (combo, rec) {
        if (rec.data.id == this.valueFieldCombo.getValue()) {
                Ext.Msg.show({
                    title: ExtGlobal.getLocaleText('acc.common.alert'),
                    message: ExtGlobal.getLocaleText('acc.CustomReport.Chart.chartPropertyPanel.FieldAlert'),
                    buttons: Ext.Msg.OK,
                    icon: Ext.Msg.WARNING,
                    scope: this,
                    fn: function (btn) {
                        if (btn == 'ok') {
                            combo.reset();
                            this.valueFieldCombo.reset();
                        }
                    }
                });
            }
    },
    
    //check whether selected value already selected in title field or not
    //return true or false
    valueFieldValidation: function (combo, rec) {
        this.groupFieldCombo.reset();
        if (rec.data.id == this.titleFieldCombo.getValue()) {
                Ext.Msg.show({
                    title: ExtGlobal.getLocaleText('acc.common.alert'),
                    message: ExtGlobal.getLocaleText('acc.CustomReport.Chart.chartPropertyPanel.FieldAlert'),
                    buttons: Ext.Msg.OK,
                    icon: Ext.Msg.WARNING,
                    scope: this,
                    fn: function (btn) {
                        if (btn == 'ok') {
                            this.valueFieldCombo.reset();
                        }
                    }
                });
            }

        if (rec.data.xtype == Ext.fieldType.numberField) {
            this.groupbyStore.loadData([["SUM"], ["COUNT"]]);
        } else if (rec.data.xtype == Ext.fieldType.textField || rec.data.xtype == Ext.fieldType.textArea) {
            this.groupbyStore.loadData([["COUNT"]]);
        }
    },
    
    //save button handler
    saveChartDeatils: function () {
        var id;
        if (this.chartPropertyPanel.getForm().isValid()) {
            if (this.chartListGrid.getSelectionModel().hasSelection()) {
                id = this.chartListGrid.getSelectionModel().getSelection()[0].data.id;
            } else {
                id = undefined;
            }
            var chartData = Ext.JSON.encode(this.getChartConfig(this.chartTypeCombo.getValue(), false));
            var chartName = this.nameField.getValue().trim();
            var chartType = this.chartTypeCombo.getValue();
            if(this.isEdit) {
                var titleField = this.chartData.titleField;
                var valueField = this.chartData.valueField;
            } else {
                var titleField = Ext.JSON.encode(this.titleFieldCombo.getSelection().data);
                var valueField = Ext.JSON.encode(this.valueFieldCombo.getSelection().data);
            }
            var groupby = this.groupFieldCombo.getValue();
            Ext.Ajax.request({
                scope: this,
                actionMethods: getStoreActionMethods(),
                url: 'ACCCreateCustomReport/saveOrUpdateChartDetails.do',
                params: {
                    id: id,
                    reportId: this.reportId,
                    name: chartName,
                    chartType: chartType,
                    titleField: titleField,
                    valueField: valueField,
                    groupby: groupby,
                    properties: chartData
                },
                success: function (response) {
                    Ext.Msg.show({
                        title: ExtGlobal.getLocaleText('acc.common.success'),
                        message: ExtGlobal.getLocaleText(Ext.JSON.decode(response.responseText).msg),
                        buttons: Ext.Msg.OK,
                        icon: Ext.Msg.INFO,
                        scope: this,
                        fn: function (btn) {
                            if (btn == 'ok') {
                                if(!this.isEdit) {
                                    this.chartDetailsStore.load();
                                    this.resetToIntialState(false);
                                this.saveBtn.setText(ExtGlobal.getLocaleText('acc.common.saveBtn'));
                            }
                                if(Ext.getCmp('toastmsg')) {
                                    Ext.getCmp('toastmsg').destroy();
                        }
                            }
                        }
                    });
                    
                    if(Ext.getCmp("idreportlistgrid")) {
                    Ext.getCmp("idreportlistgrid").getStore().reload();
                    }
                    
                    if(Ext.getCmp("CustomChart_" + id)) {//if updating chart is already in open state then refreshing its view
                        this.refreshChartPanel(id, chartName, chartType, titleField, valueField, groupby, chartData);
                    }
                },
                failure: function () {
                    Ext.Msg.show({
                        title: ExtGlobal.getLocaleText('acc.common.failure'),
                        message: ExtGlobal.getLocaleText('acc.CustomReport.Chart.chartPropertyPanel.saveFailure'),
                        buttons: Ext.Msg.OK,
                        icon: Ext.Msg.ERROR
                    });
                }
            });
        } else {
            Ext.Msg.show({
                title: ExtGlobal.getLocaleText('acc.common.alert'),
                message: ExtGlobal.getLocaleText('acc.CustomReport.Chart.chartPropertyPanel.FormInvalidAlert'),
                buttons: Ext.Msg.OK,
                icon: Ext.Msg.WARNING
            });
        }
    },
    
    
    isAcceptibleValueField: function (record) {
        var isAcceptibleValueField = false;
        if (record.data.xtype == Ext.fieldType.textField || record.data.xtype == Ext.fieldType.numberField || record.data.xtype == Ext.fieldType.textArea) {
            if (!(record.data.isMeasureItem || record.data.showasrowexpander)) {
                isAcceptibleValueField = true;
            }
        }
        return isAcceptibleValueField;
    },
    
    //chart selection handler
    chartListGridSelectionModel: function (sm, record, index) {
        var chartData = record.data;
        this.nameField.setValue(chartData.name);
        this.chartTypeCombo.setValue(chartData.chartType);
        
        var titleField = Ext.JSON.decode(chartData.titleField);
        var valueField = Ext.JSON.decode(chartData.valueField);
        this.titleFieldCombo.setValue(titleField.id);
        this.valueFieldCombo.setValue(valueField.id);
        
        if(valueField.xtype == Ext.fieldType.numberField) {
            this.groupbyStore.loadData([["SUM"], ["COUNT"]]);
        } else if (valueField.xtype == Ext.fieldType.textField || valueField.xtype == Ext.fieldType.textArea) {
            this.groupbyStore.loadData([["COUNT"]]);
        }        
        this.groupFieldCombo.setValue(chartData.groupby);
        
        var chartConfiguration = Ext.JSON.decode(chartData.properties);
        
        var generalSettings = new Object();
        if (chartConfiguration.chartCursor) {
            generalSettings.chartCursor = chartConfiguration.chartCursor.enabled;
        } else {
            generalSettings.chartCursor = false;
        }
        if (chartConfiguration.chartScrollbar) {
            generalSettings.chartScrollbar = chartConfiguration.chartScrollbar.enabled;
        } else {
            generalSettings.chartScrollbar = false;
        }
        if (chartConfiguration.export) {
            generalSettings.export = chartConfiguration.export.enabled;
        } else {
            generalSettings.export = false;
        }
        if (chartConfiguration.valueScrollbar) {
            generalSettings.valueScrollbar = chartConfiguration.valueScrollbar.enabled;
        } else {
            generalSettings.valueScrollbar = false;
        }        
        generalSettings.decimalSeparator = chartConfiguration.decimalSeparator;
        generalSettings.percentPrecision = chartConfiguration.percentPrecision;
        generalSettings.precision = chartConfiguration.precision;
        generalSettings.usePrefixes = chartConfiguration.usePrefixes;

        delete chartConfiguration.type;
        delete chartConfiguration.decimalSeparator;
        delete chartConfiguration.percentPrecision;
        delete chartConfiguration.precision;
        delete chartConfiguration.usePrefixes;

        if (chartData.chartType == 'Bar' || chartData.chartType == 'Line') {
            this.graphsPropertyGrid.enable();
            this.categoryAxisPropertyGrid.enable();
            this.valueAxesPropertyGrid.enable();

            delete chartConfiguration.categoryField;
            delete chartConfiguration.graphs[0].valueField;
            
            chartConfiguration.graphs[0].fillAlphas = chartConfiguration.graphs[0].fillAlphas * 100;
            chartConfiguration.graphs[0].lineAlpha = chartConfiguration.graphs[0].lineAlpha * 100;
            chartConfiguration.graphs[0].bulletAlpha = chartConfiguration.graphs[0].bulletAlpha * 100;

            this.graphsPropertyGrid.setSource(chartConfiguration.graphs[0]);
            this.categoryAxisPropertyGrid.setSource(chartConfiguration.categoryAxis);
            this.valueAxesPropertyGrid.setSource(chartConfiguration.valueAxes[0]);
            
            delete chartConfiguration.graphs;
            delete chartConfiguration.categoryAxis;
            delete chartConfiguration.valueAxes;

        } else if (chartData.chartType == 'Pie') {
            delete chartConfiguration.titleField;
            delete chartConfiguration.valueField;
            
            this.graphsPropertyGrid.disable();
            this.categoryAxisPropertyGrid.disable();
            this.valueAxesPropertyGrid.disable();
        }
        
        this.legendsPropertyGrid.setSource(chartConfiguration.legend);
        
        chartConfiguration.titles[0].alpha = chartConfiguration.titles[0].alpha * 100;
        this.titlePropertyGrid.setSource(chartConfiguration.titles[0]);
        
        this.generalSettingsPropertyGrid.setSource(generalSettings);
                
        delete chartConfiguration.legend;
        delete chartConfiguration.titles;
        
        chartConfiguration.innerRadius = chartConfiguration.innerRadius.substring(0, chartConfiguration.innerRadius.length - 1);
        chartConfiguration.backgroundAlpha = chartConfiguration.backgroundAlpha * 100;
        this.appearancePropertyGrid.setSource(chartConfiguration);
        
        this.chartPreviewPanel.removeAll();
        this.saveBtn.setText(ExtGlobal.getLocaleText('acc.common.update'));
        this.showPreview();
    },
    
    //chart list delete button click handler
    handleCellClick: function (view, cell, cellIndex, record, row, rowIndex, event) {
        if (event.getTarget("div[class='delete pwnd delete-gridrow']")) {
            Ext.Ajax.request({
                scope: this,
                actionMethods: getStoreActionMethods(),
                url: 'ACCCreateCustomReport/deleteChartDetails.do',
                params: {
                    id: record.data.id
                },
                success: function () {
                    this.chartDetailsStore.load();
                    this.chartPropertyPanel.getForm().reset();
                    this.resetToIntialState();
                    this.saveBtn.setText(ExtGlobal.getLocaleText('acc.common.saveBtn'));
                    if(Ext.getCmp("idreportlistgrid")) {
                        Ext.getCmp("idreportlistgrid").getStore().reload();
                    }
                },
                failure: function () {
                    Ext.Msg.show({
                        title: ExtGlobal.getLocaleText('acc.common.failure'),
                        message: ExtGlobal.getLocaleText('acc.CustomReport.Chart.chartPropertyPanel.deleteFailure'),
                        buttons: Ext.Msg.OK,
                        icon: Ext.Msg.ERROR
                    });
                }
            });
        }        
    },
    
    //return chart config configured in chart configuration panel
    getChartConfig: function (chartType, isPreviewCall) {
        var chartConfiguration = new Object();
        var titleField = this.titleFieldCombo.getValue();
        var valueField = this.valueFieldCombo.getValue();

        var appearanceConfig = Ext.clone(this.appearancePropertyGrid.getSource());
        appearanceConfig.backgroundAlpha = this.alphaIntegerRenderer(appearanceConfig.backgroundAlpha);
        
        var graphConfig = Ext.clone(this.graphsPropertyGrid.getSource());
        graphConfig.fillAlphas = this.alphaIntegerRenderer(graphConfig.fillAlphas);
        graphConfig.lineAlpha = this.alphaIntegerRenderer(graphConfig.lineAlpha);
        graphConfig.bulletAlpha = this.alphaIntegerRenderer(graphConfig.bulletAlpha);
        
        var legendsConfig = Ext.clone(this.legendsPropertyGrid.getSource());
        var categoryAxisConfig = Ext.clone(this.categoryAxisPropertyGrid.getSource());
        var valueAxesConfig = Ext.clone(this.valueAxesPropertyGrid.getSource());
        
        var titleConfig = Ext.clone(this.titlePropertyGrid.getSource());
        titleConfig.alpha = this.alphaIntegerRenderer(titleConfig.alpha);
        
        var generalSettingsConfig = Ext.clone(this.generalSettingsPropertyGrid.getSource());

        appearanceConfig.usePrefixes = generalSettingsConfig.usePrefixes;
        appearanceConfig.precision = generalSettingsConfig.precision;
        appearanceConfig.percentPrecision = generalSettingsConfig.percentPrecision;
        appearanceConfig.decimalSeparator = generalSettingsConfig.decimalSeparator;

        chartConfiguration = Ext.clone(appearanceConfig);

        if (chartType == 'Bar' || chartType == 'Line') {
            chartConfiguration.type = "serial";
            chartConfiguration.innerRadius = "0";
            if (!isPreviewCall) {
                chartConfiguration.categoryField = titleField;
                graphConfig.valueField = valueField;
            } else {
                chartConfiguration.categoryField = "category";
                graphConfig.valueField = "column-1";
            }
            if (chartType == 'Bar') {
                if (graphConfig.type == 'line' || graphConfig.type == 'smoothedLine') {
                    graphConfig.type = "column";
                }
            } else if (chartType == 'Line') {
                if (graphConfig.type == 'column') {
                    graphConfig.type = "line";
                }
                graphConfig.fillAlphas = 0;
                if (!graphConfig.bullet) {
                    graphConfig.bullet = "round";
                }
                graphConfig.bulletAlpha = 1;
            }
            chartConfiguration.graphs = new Array(Ext.clone(graphConfig));
            chartConfiguration.categoryAxis = Ext.clone(categoryAxisConfig);
            chartConfiguration.valueAxes = new Array(Ext.clone(valueAxesConfig));

            if (generalSettingsConfig.chartCursor) {
                var chartCursor = new Object();
                chartCursor.enabled = generalSettingsConfig.chartCursor;
                chartConfiguration.chartCursor = Ext.clone(chartCursor);
            }

            if (generalSettingsConfig.chartScrollbar) {
                var chartScrollbar = new Object();
                chartScrollbar.enabled = generalSettingsConfig.chartScrollbar;
                chartConfiguration.chartScrollbar = Ext.clone(chartScrollbar);
            }

            if (generalSettingsConfig.valueScrollbar) {
                var valueScrollbar = new Object();
                valueScrollbar.enabled = generalSettingsConfig.valueScrollbar;
                chartConfiguration.valueScrollbar = Ext.clone(valueScrollbar);
            }

        } else if (chartType == 'Pie') {
            chartConfiguration.type = "pie";
            chartConfiguration.innerRadius = chartConfiguration.innerRadius + "%";
            if (!isPreviewCall) {
                chartConfiguration.titleField = titleField;
                chartConfiguration.valueField = valueField;
            } else {
                chartConfiguration.titleField = "category";
                chartConfiguration.valueField = "column-1";
            }
        }

        chartConfiguration.legend = Ext.clone(legendsConfig);
        chartConfiguration.titles = new Array(Ext.clone(titleConfig));

        if (generalSettingsConfig.export) {
            var exportConfig = new Object();
            exportConfig.enabled = generalSettingsConfig.export;
            chartConfiguration.export = Ext.clone(exportConfig);
        }

        if (isPreviewCall) {
            var dataProvider = '[{"category":"AA","column-1":8},{"category":"AB","column-1":16},{"category":"AC","column-1":2},{"category":"AD","column-1":7},{"category":"AE","column-1":5},{"category":"AF","column-1":9},{"category":"AG","column-1":4},{"category":"AH","column-1":15},{"category":"AI","column-1":12},{"category":"AJ","column-1":17}]';
            chartConfiguration.dataProvider = Ext.JSON.decode(dataProvider);
        }

        return chartConfiguration;
    },
    
    chartTypeChangeListener: function (combo, rec) {
        var newVal = rec.data.id;
        if(!this.isEdit) {
        this.resetToIntialState(true);
        }
        if (newVal == 'Bar' || newVal == 'Line') {
            this.graphsPropertyGrid.enable();
            this.categoryAxisPropertyGrid.enable();
            this.valueAxesPropertyGrid.enable();
        } else if (newVal == 'Pie') {
            this.graphsPropertyGrid.disable();
            this.categoryAxisPropertyGrid.disable();
            this.valueAxesPropertyGrid.disable();
        }
        
        this.saveBtn.setText(ExtGlobal.getLocaleText('acc.common.saveBtn'));
        this.showPreview();
    },
    
    showPreview: function () {
        if (this.chartTypeCombo.getValue() != null) {
            var previewChartConfig = this.getChartConfig(this.chartTypeCombo.getValue(), true);
            if (this.chart != undefined) {
                this.chartPreviewPanel.removeAll();
                this.chart.destroy();
                this.chart = undefined;
            }
            this.chart = new Ext.Chart({
                mode: "local",
                chartConfig: previewChartConfig
            });
            this.chartPreviewPanel.add(this.chart);
        } else {
            Ext.Msg.show({
                title: ExtGlobal.getLocaleText('acc.common.alert'),
                message: ExtGlobal.getLocaleText('acc.CustomReport.Chart.chartConfigPanel.showPreviewBtn.Validation.msg'),
                buttons: Ext.Msg.OK,
                icon: Ext.Msg.WARNING
            });
        }
    },
    
    alphaIntegerRenderer: function (value) {
        if (value > 100) {
            value = 100;            
        } else if (value < 0) {
            value = 0;
        }

        return Ext.util.Format.number(value / 100, '0.00');
    },

    //reset chart builder window to initial state
    resetToIntialState: function (isFromChartTypeChange) {
        if(isFromChartTypeChange) {
            this.titleFieldCombo.reset();
            this.valueFieldCombo.reset();
            this.groupFieldCombo.reset();
        } else {
            this.chartPropertyPanel.getForm().reset();
        }
        
        this.chartListGrid.getSelectionModel().deselectAll();

        this.graphsPropertyGrid.enable();
        this.categoryAxisPropertyGrid.enable();
        this.valueAxesPropertyGrid.enable();

        this.appearancePropertyGrid.setSource(Ext.clone(this.appearancePropertyGridSource));
        this.graphsPropertyGrid.setSource(Ext.clone(this.graphsPropertyGridSource));
        this.categoryAxisPropertyGrid.setSource(Ext.clone(this.categoryAxisPropertyGridSource));
        this.valueAxesPropertyGrid.setSource(Ext.clone(this.valueAxesPropertyGridSource));
        this.legendsPropertyGrid.setSource(Ext.clone(this.legendsPropertyGridSource));
        this.titlePropertyGrid.setSource(Ext.clone(this.titlePropertyGridSource));
        this.generalSettingsPropertyGrid.setSource(Ext.clone(this.generalSettingsPropertyGridSource));
        this.chartPreviewPanel.removeAll();
    },
    
    //color picker
    colorEditor: function (grid, property) {
        var thiz = this;
        this.colorPicker = new Ext.form.field.Text({
            triggers: {
                colorEditor: {
                    handler: function () {
                        if (this.disabled) {
                            return;
                        }
                        this.menu = new Ext.menu.ColorPicker({
                            shadow: true,
                            autoShow: true
                        });
                        this.menu.alignTo(this.inputEl, 'tl-bl?');

                        this.menu.on('select', function (colorPicker, selColor) {
                            thiz[grid].setProperty(property, "#" + selColor);
                        }, this);
                        this.menu.show(this.inputEl);
                    }
                }
            }
        }, this);

        return this.colorPicker;
    },
    
    //color renderer
    colorRenderer: function (value, meta) {
        meta.style = "border-style:solid; border-color:" + value + "; border-width:2px;";
        return value;
    },
    
    //refreshing already open state chart panel
    refreshChartPanel: function (id, chartName, chartType, titleField, valueField, groupby, properties) {
        var chartData = {};
        var chart = Ext.getCmp("CustomChart_" + id).chartData;
        
        chartData.id = id;
        chartData.reportid = chart.reportId;
        chartData.name = chartName;
        chartData.chartType = chartType;
        chartData.iconCls = chart.iconCls;
        chartData.url = chart.url;
        chartData.params = chart.params;
        chartData.titleField = titleField;
        chartData.valueField = valueField;
        chartData.groupby = groupby;
        chartData.properties = properties;
        chartData.params.isChartRequest = true;
        getChartPanelContainer(chartData);
        
        Ext.create('Ext.window.Toast', {
            id: 'toastmsg',
            html: "<b>" + chartName + "</b> " + ExtGlobal.getLocaleText("acc.CustomReport.Chart.viewUpdated"),
            align: 't',
            border: false,
            height: 'auto',
            width: 210
        }).show();
    },
    
    loadChartRecord: function () {
        if(this.chartData) {
            this.chartDetailsStore.loadData([this.chartData]);
            this.titleFieldStore.loadData([Ext.JSON.decode(this.chartData.titleField)]);
            this.valueFieldStore.loadData([Ext.JSON.decode(this.chartData.valueField)]);
            this.chartListGrid.getView().select(0);
            this.titleFieldCombo.setDisabled(true);
            this.valueFieldCombo.setDisabled(true);
        }
    }
});
