/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
function callSalesCommissionSchemaMaster() {
    var panel = Wtf.getCmp("salesCommissionSchemaMaster");
    if (panel == null) {
        panel = new Wtf.account.SalesCommissionSchemaMaster({
            title: WtfGlobal.getLocaleText("acc.sales.salescommission.salesCommissionSchema"),
            tabTip: WtfGlobal.getLocaleText("acc.sales.salescommission.salesCommissionSchema"),
            id: 'salesCommissionSchemaMaster',
            closable: true,
            border: false,
            layout: 'fit',
            iconCls: getButtonIconCls(Wtf.etype.product)
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.SalesCommissionSchemaMaster = function (config) {
    Wtf.apply(this, config);
    Wtf.account.SalesCommissionSchemaMaster.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.SalesCommissionSchemaMaster, Wtf.Panel, {
    onRender: function (config) {
        Wtf.account.SalesCommissionSchemaMaster.superclass.onRender.call(this, config);

        //create Grid
        this.creategrid();

        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid],
                    tbar: this.btnArr,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.Store,
                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id}),
                        items: []
                    })
                }]
        });
        this.add(this.leadpan);
    },
    updateGrid: function () {
        this.Store.reload();
    },
    creategrid: function () {
        this.btnArr = [];
        this.addAndConfigure = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.sales.salescommission.schema.addAndConfigure"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.sales.salescommission.schema.addAndConfigure.tooltip"),
            iconCls: getButtonIconCls(Wtf.etype.add),
            handler: function () {
                this.configureCommissonSchemaWindow(false);
            }
        });
        this.viewRulesAndCondition = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.sales.salescommission.schema.viewCommissionSchema"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.sales.salescommission.schema.viewCommissionSchema.tooltip"),
            iconCls: getButtonIconCls(Wtf.etype.copy),
            handler: function () {
                this.configureCommissonSchemaWindow(true);
            }
        });

        this.btnArr.push(this.addAndConfigure);
        this.btnArr.push(this.viewRulesAndCondition);
        this.gridRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'schemaMaster'}
        ]);

        this.msgLmt = 30;
        this.StoreReader = new Wtf.data.KwlJsonReader({
            totalProperty: 'count',
            root: "data"
        }, this.gridRec);

        this.Store = new Wtf.data.Store({
            url: "AccSalesCommission/getSalesCommissionSchemaMasters.do",
            baseParams: {
                mode: 22
            },
            reader: this.StoreReader
        });


        this.loadMask = new Wtf.LoadMask(document.body, {
            msg: WtfGlobal.getLocaleText("acc.msgbox.50")
        });

        this.Store.on('loadexception', function () {
            var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
            this.loadMask.hide();
        }, this);

        this.Store.load();

        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect: true
        });

        this.colModel = new Wtf.grid.ColumnModel([this.sm, {
                header: WtfGlobal.getLocaleText("acc.sales.salescommission.salesCommissionSchema.master"),
                dataIndex: 'schemaMaster',
                align: 'center',
                width: 500
            }, {
                header: WtfGlobal.getLocaleText("acc.common.delete"),
                dataIndex: 'delete',
                align: 'center',
                width: 300,
                renderer: function () {
                    return "<div class='pwnd delete-gridrow' style='margin-left:40px;'> </div>";
                }
            }
        ]);

        this.grid = new Wtf.grid.GridPanel({
            cm: this.colModel,
            store: this.Store,
            sm: this.sm,
            border: false,
            viewConfig: {
                emptyText: '<center>' + WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec")) + '</center>',
                forceFit: false
            }
        });
        this.grid.on("cellclick", this.deleteCommissonSchema, this);
    },
    deleteCommissonSchema: function (gd, ri, ci, e) {
        var event = e;
        if (event.target.className == "pwnd delete-gridrow" && this.grid.getSelectionModel().hasSelection()) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.salesCommissionRules.schema.delete.alert"), function (btn) {
                if (btn == "yes") {
                    var schemaMasterId = this.grid.getSelectionModel().getSelected().data.id;
                    var schemaMaster = this.grid.getSelectionModel().getSelected().data.schemaMaster;
                    Wtf.Ajax.requestEx({
                        url: "AccSalesCommission/deleteSalesCommissionSchema.do",
                        params: {
                            schemaMasterId: schemaMasterId,
                            schemaMaster:schemaMaster
                        }
                    }, this,
                        function (req, res) {
                            if (req.success) {
                                Wtf.getCmp('salesCommissionSchemaMaster').Store.reload();
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), req.msg], 0);
                            } else
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), req.msg], 1);
                        },
                        function (req) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.salesCommissionRules.schema.delete.error")], 1);
                        });
                }
            }, this);
        }
    },
    configureCommissonSchemaWindow: function (isEdit) {
        var schemaMasterId = "";
        var schemaMasterName = "";
        if (isEdit) {
            var rec = this.grid.getSelectionModel().getSelected();
            if (rec != undefined) {
                schemaMasterId = rec.data.id;
                schemaMasterName = rec.data.schemaMaster;
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.salesCommission.view.saleCommissionSchema.alert")], 2);
                return;
            }
        }
        this.commissonSchemaWindow = new Wtf.account.SalesCommissionSchema({
            title: WtfGlobal.getLocaleText("acc.masterConfig.salesCommision"),
            id: 'salescommissionschema',
            scope:this,
            modal: true,
            width: 650,
            height: 700,
            resizable:false,
            isEdit:isEdit,
            schemaMasterId:schemaMasterId,
            schemaMasterName:schemaMasterName
        });
        this.commissonSchemaWindow.show();
    }
});



//********************************************************************************

Wtf.account.SalesCommissionSchema = function (config) {
    this.isEdit = config.isEdit;
    this.schemaMasterId = config.schemaMasterId;
    this.schemaMasterName = config.schemaMasterName;
    this.ruleIndex = 0;
    Wtf.apply(this, config);
    Wtf.account.SalesCommissionSchema.superclass.constructor.call(this, {
        buttons: [this.saveBtn = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.common.saveBtn"), // Save
                scope: this,
                handler: this.saveSchemaMaster.createDelegate(this)
            }), this.cancelBtn = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"), // 'cancelBtn',
                scope: this,
                handler: this.closeWin.createDelegate(this)
            })]
    });
}

Wtf.extend(Wtf.account.SalesCommissionSchema, Wtf.Window, {
    modal: true,
    title: WtfGlobal.getLocaleText("acc.masterConfig.salesCommision"),
    id: 'salescommissionschema',
    width: 650,
    height:700,
    constrain: true,
    resizable: false,
    iconCls: "pwnd deskeralogoposition",
    initComponent: function () {
        Wtf.salesCommisionSchemaWindow.superclass.initComponent.call(this);
        this.getGridPanel();
        this.GetAddEditForm();
        this.add(this.northPanel);
        this.add(this.schemaMasterForm);
        this.add(this.AddEditForm);
        this.add(this.gridPanel);

    },
    getGridPanel: function () {
        this.salesCommissionSchemaRuleRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'ruleIndex'},
            {name: 'schemaMasterId'},
            {name: 'schemaMasterName'},
            {name: 'schemaType'},
            {name: 'schemaTypeId'},
            {name: 'amount'},
            {name: 'ruleid'},
            {name: 'lowerlimit'},
            {name: 'upperlimit'},
            {name: 'commissiontype'},
            {name: 'categoryid'},
            {name: 'categoryname'},
            {name: 'ruledescription'},
            {name: 'conditionArr'}
        ]);

        this.salesCommissionSchemaRuleStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            }, this.salesCommissionSchemaRuleRec),
            url: "AccSalesCommission/getSalesCommissionRules.do"
        });
        if (this.isEdit) {
            this.salesCommissionSchemaRuleStore.load({
                params: {
                    schemaMasterId: this.schemaMasterId
                }
            });
        }
        this.schemaTypeStore = new Wtf.data.SimpleStore({
            fields: [{name: 'id'}, {name: 'name'}],
            data: [['1', 'Percentage'], ['2', 'Flat']]
        });

        this.itemRec = Wtf.data.Record.create([
            {name: "id"},
            {name: "name"},
            {name: "modulename"},
            {name: "fieldtype"},
            {name: "parentid"},
            {name: "leaf"},
            {name: "fieldtype"},
            {name: 'level', type: 'int'}
        ]);
        var baseparam = {
            mode: 112,
            groupid: 19
        };
        this.itemStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: baseparam,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.itemRec)
        });
        this.itemStore.load();
        this.rulesColumnModel = new Wtf.grid.ColumnModel([new Wtf.grid.CheckboxSelectionModel({
                singleSelect: true
            }),{
                header: WtfGlobal.getLocaleText("acc.sales.salescommission.SchemaType"),
                align: 'center',
                dataIndex: 'schemaType',
                width: 100
            },{
                header:  WtfGlobal.getLocaleText("acc.sales.salescommission.Percentage/Amount"),
                align: 'center',
                dataIndex: 'amount',
                width: 100
            },{
                header: "<div style='margin-left:50px;'>" + WtfGlobal.getLocaleText("acc.common.rulesDescription") + "</div>",
                align: 'left',
                dataIndex: 'ruledescription',
                width: 700
            },{
                header: WtfGlobal.getLocaleText("acc.common.delete"),
                align: 'left',
                width: 100,
                renderer: function () {
                    return "<div class='pwnd delete-gridrow' style='margin-left:40px;'> </div>";
                }
            }
        ]);
        this.sm = new Wtf.grid.CheckboxSelectionModel({singleSelect: true});
        this.rulesGrid = new Wtf.grid.GridPanel({
            store: this.salesCommissionSchemaRuleStore,
            cm: this.rulesColumnModel,
            autoScroll:true,
            border: false,
            height: 132,
            sm: this.sm,
            viewConfig: {
                emptyText: '<center>' + WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec")) + '</center>',
                forceFit: false
            }
        });
        this.rulesGrid.on("cellclick", this.deleteCommissionRules, this);
        this.northPanel = new Wtf.Panel({
            region: "north",
            height: 80,
            border: false,
            bodyStyle: "background:white;border-bottom:1px solid #bfbfbf;",
            html: getTopHtml("Sales Commission Schema ", "Manage Sales Commission Schema", '../../images/createuser.png', false, '0px 0px 0px 0px')
        });
        this.gridPanel = new Wtf.Panel({
            region: "south",
            items: [this.rulesGrid]
        });
    },
    GetAddEditForm: function () {
        //**************************************MasterSchemaForm*********************
        this.schemaMaster = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.masterconfig.schemaMaster.text") + "*",
            emptyText: WtfGlobal.getLocaleText("acc.masterconfig.schemaMaster.blankText"),
            name: "schemaMaster",
            labelWidth: 200,
            width: 200,
            maxLength: 50,
            allowBlank: false,
            disabled:this.isEdit
        });
        if(this.isEdit){
            this.schemaMaster.setValue(this.schemaMasterName);
        }
        this.schemaMasterForm = new Wtf.form.FormPanel({
            border: false,
            defaultType: 'textfield',
            labelWidth: 200,
            bodyStyle: "height:29px;background-color:#f1f1f1;padding:15px 35px 35px",
            items: [new Wtf.form.FieldSet({
                    style: "height: 35px;",
                    title: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.sales.salescommission.salesCommissionSchema.master") + "'>" + WtfGlobal.getLocaleText("acc.sales.salescommission.salesCommissionSchema.master") + " </span>",
                    items: [this.schemaMaster]
                })],
        });
        //****************************************Rules form*********************************************************
        this.schemaType = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.salesCommission.commissionSchemaType.fieldLabel") + '*',
            triggerAction: 'all',
            mode: 'local',
            valueField: 'id',
            displayField: 'name',
            store: this.schemaTypeStore,
            typeAhead: true,
            forceSelection: true,
            name: 'schemaType',
            labelWidth: 200,
            width: 200
        });
        this.schemaType.setValue('1');

        this.percentageValue = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.masterconfig.percentageValue") + "*",
            name: "amount",
            labelWidth: 200,
            width: 200,
            maxLength: 50,
            allowBlank: false
        });

        this.schemaType.on('select', function () {//writen for Label Change
            if (this.schemaType.getValue() == 1) {
                WtfGlobal.updateFormLabel(this.percentageValue, WtfGlobal.getLocaleText("acc.masterconfig.percentageValue") + "*:");
            } else {
                WtfGlobal.updateFormLabel(this.percentageValue, WtfGlobal.getLocaleText("acc.masterconfig.amount") + "(" + WtfGlobal.getCurrencyName() + ")*:");
            }
        }, this);
        
        this.commissionTypeStore = new Wtf.data.SimpleStore({
            fields: [{name: 'id'}, {name: 'name'}],
            data: [['1', 'Amount'], ['2', 'Brand/Product Category'], ['3', 'Payment Term'],['4', 'Margin']]
        });
        this.commissionTypeCombo = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.masterconfig.schemaMaster.commissionType") + '*',
            mode: 'local',
            name: "commissiontype",
            triggerAction: 'all',
            valueField: 'id',
            displayField: 'name',
            store: this.commissionTypeStore,
            typeAhead: true,
            forceSelection: true,
            hiddenName: 'type',
            width: 200
        });
        this.commissionTypeCombo.setValue('1');
        
        this.marginConditionalStore = new Wtf.data.SimpleStore({
            fields: [{name: 'id'}, {name: 'name'}],
            data: [['1', 'Between'], ['2', 'Greater than'], ['3', 'Less than']]
        });
        this.marginConditionalCombo = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.salesCommission.margin.marginCondition") + '*',
            mode: 'local',
            name: "margincondition",
            triggerAction: 'all',
            valueField: 'id',
            displayField: 'name',
            store: this.marginConditionalStore,
            typeAhead: true,
            forceSelection: true,
            width: 200
        });
        this.marginConditionalCombo.disable();
        
        this.productCategory = new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.salesCommission.brandProductCategory.fieldLabel"),
            forceSelection: true
        }, {
            triggerAction: 'all',
            mode: 'local',
            valueField: 'id',
            displayField: 'name',
            store: this.itemStore,
            typeAhead: true,
            name: 'productCategory',
            allowBlank: false,
            width: 200
        }));
        this.productCategory.disable();

        this.lowerLimit = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.masterconfig.setlowerlimitValue") + "*",
            name: "lowerlimit",
            labelWidth: 200,
            width: 200,
            maxLength: 50,
            id: "lowerlimitid",
            allowBlank: false
        });

        this.upperLimit = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.masterconfig.setupperlimitValue") + "*",
            name: "upperlimit",
            width: 200,
            labelWidth: 200,
            maxLength: 50,
            id: "upperlimitid",
            allowBlank: false
        });

        this.addRulesBtn = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.addRule"), //Add Rule
            tooltip:WtfGlobal.getLocaleText("acc.common.addRule.tooltip"),
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.add),
            handler: this.addRulesAndSalesCondition.createDelegate(this,[false])
        });
        this.addConditionBtn = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.addCondition"), //Add Condition
            tooltip:WtfGlobal.getLocaleText("acc.common.addCondition.tooltip"),
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.add),
            handler: this.addRulesAndSalesCondition.createDelegate(this,[true])
        });
        
        this.commissionTypeCombo.on('select', this.showHideFields.createDelegate(this), this);
        this.marginConditionalCombo.on('select', this.showHideLimitFields.createDelegate(this), this);

        this.AddEditForm = new Wtf.form.FormPanel({
            border: false,
            autoScroll: true,
            labelWidth: 200,
            defaultType: 'textfield',
            bodyStyle: "height:270px;background-color:#f1f1f1;padding:9px 35px 35px",
            items: [new Wtf.form.FieldSet({
                    style: "height: 270px;",
                    title: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.masterconfig.schemaMaster.addEditForm.title") + "'>" + WtfGlobal.getLocaleText("acc.masterconfig.schemaMaster.addEditForm.title") + " </span>",
                    items: [new Wtf.form.FieldSet({
                            style: "height: 60px;",
                            title: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.salesCommission.commissionSchemaType.define") + "'>" + WtfGlobal.getLocaleText("acc.salesCommission.commissionSchemaType.define") + " </span>",
                            items: [this.schemaType, this.percentageValue]
                        }),new Wtf.form.FieldSet({
                            style: "height: 145px;",
                            title: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.masterconfig.schemaMaster.salesCondition") + "'>" + WtfGlobal.getLocaleText("acc.masterconfig.schemaMaster.salesCondition") + " </span>",
                            items: [ this.commissionTypeCombo, this.productCategory, this.marginConditionalCombo,this.lowerLimit, this.upperLimit]
                        })]
                })],
            bbar: [this.addRulesBtn, this.addConditionBtn]
        });
    },
    showHideFields: function () {
        /*
         * To hide or Show fields and to change label on commission type
         */
        if (this.commissionTypeCombo.getValue() == 1) {
            WtfGlobal.updateFormLabel(this.lowerLimit, WtfGlobal.getLocaleText("acc.masterconfig.setlowerlimitValue") + "*:");
            WtfGlobal.updateFormLabel(this.upperLimit, WtfGlobal.getLocaleText("acc.masterconfig.setupperlimitValue") + "*:");
            this.marginConditionalCombo.reset();
            this.marginConditionalCombo.allowBlank = true;
            this.marginConditionalCombo.disable();
            
            this.productCategory.reset();
            this.productCategory.allowBlank = true;
            this.productCategory.disable();
            
            this.lowerLimit.enable();
//            this.lowerLimit.reset();
            this.lowerLimit.setValue("");
            this.lowerLimit.allowBlank = false;
            
            this.upperLimit.enable();
//            this.upperLimit.reset();
            this.upperLimit.setValue("");
            this.upperLimit.allowBlank = false;
            
        } else if (this.commissionTypeCombo.getValue() == 3) {
            WtfGlobal.updateFormLabel(this.lowerLimit, WtfGlobal.getLocaleText("acc.masterconfig.schemaMaster.minDays") + "*:");
            WtfGlobal.updateFormLabel(this.upperLimit, WtfGlobal.getLocaleText("acc.masterconfig.schemaMaster.manDays") + "*:");
            this.marginConditionalCombo.reset();
            this.marginConditionalCombo.disable();
            
            this.productCategory.reset();
            this.productCategory.disable();
            this.productCategory.allowBlank = true;
            
            this.lowerLimit.enable();
//            this.lowerLimit.reset();
            this.lowerLimit.setValue("");
            this.lowerLimit.allowBlank = false;
            
            this.upperLimit.enable();
//            this.upperLimit.reset();
            this.upperLimit.setValue("");
            this.upperLimit.allowBlank = false;
            
        } else if (this.commissionTypeCombo.getValue() == 4) {
            WtfGlobal.updateFormLabel(this.lowerLimit, WtfGlobal.getLocaleText("acc.masterconfig.setlowerlimitValue") + "*:");
            WtfGlobal.updateFormLabel(this.upperLimit, WtfGlobal.getLocaleText("acc.masterconfig.setupperlimitValue") + "*:");
            this.marginConditionalCombo.enable();
            this.productCategory.allowBlank = true;
            this.productCategory.reset();
            this.productCategory.disable();
            this.marginConditionalCombo.setValue('1');
            this.showHideLimitFields();
        }else {
            this.productCategory.enable();
            this.productCategory.allowBlank = false;
            
            this.marginConditionalCombo.reset();
            this.marginConditionalCombo.disable();
            
            this.lowerLimit.allowBlank = true;
//            this.lowerLimit.reset();
            this.lowerLimit.setValue("");
            this.lowerLimit.disable();
            
            this.upperLimit.allowBlank = true;
//            this.upperLimit.reset();
            this.upperLimit.setValue("");
            this.upperLimit.disable();
        }
    },
    showHideLimitFields: function () {
        if (this.marginConditionalCombo.getValue() == 1) {
            this.lowerLimit.enable();
            this.lowerLimit.allowBlank = false;
            this.lowerLimit.reset();
            
            this.upperLimit.enable();
            this.upperLimit.reset();
            this.upperLimit.allowBlank = false;
            
        } else if (this.marginConditionalCombo.getValue() == 2) {
            this.lowerLimit.enable();
            this.lowerLimit.reset();
            this.lowerLimit.allowBlank = false;
            
//            this.upperLimit.reset();
            this.upperLimit.allowBlank = true;
            this.upperLimit.setValue("");
            this.upperLimit.disable();
        } else {
//            this.lowerLimit.reset();
            this.lowerLimit.allowBlank = true;
            this.lowerLimit.setValue("");
            this.lowerLimit.disable();
            
            this.upperLimit.enable();
            this.upperLimit.reset();
            this.upperLimit.allowBlank = false;
        }

    },
    addRulesAndSalesCondition: function (isCondition) {
        var items = this.salesCommissionSchemaRuleStore.getRange();
        if (!this.AddEditForm.form.isValid()) {
            WtfComMsgBox(2, 2);
            return;
        } else if (isCondition && items.length === 0) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.salesCommission.save.addRules.alert")], 2);
            return;
        } else {
            var schemaTypeId = this.schemaType.getValue();
            var schemaType = this.schemaType.getRawValue();
            var amount = this.percentageValue.getValue();
            var commissionType = this.commissionTypeCombo.getValue();
            var commissionTypeName = this.commissionTypeCombo.getRawValue();
            var marginCondition = this.marginConditionalCombo.getValue();
            var lowerLimit = this.lowerLimit.getValue();
            var upperLimit = this.upperLimit.getValue();
            var productCategoryId = this.productCategory.getValue();
            var categoryName = this.productCategory.getRawValue();

            var ruledescription = "";

            if (commissionType == "2") {
                ruledescription = commissionTypeName + " equal to <b>" + categoryName + " </b>";
                this.productCategory.reset();
            } else if (commissionType == "4" && marginCondition != "1") {
                if (marginCondition == "2") {
                    ruledescription = commissionTypeName + " greater than <b>" + lowerLimit + " </b>";
                    this.lowerLimit.reset();
                } else {
                    ruledescription = commissionTypeName + " less than <b>" + upperLimit + " </b>";
                    this.upperLimit.reset();
                }
            } else {
                if (upperLimit < lowerLimit) {
                    var msg = commissionType == 3 ? WtfGlobal.getLocaleText("acc.salesCommissionRules.addRule.paymentTerm.alert") : WtfGlobal.getLocaleText("acc.salesCommissionRules.addRule.amountLimit.alert");
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                    return;
                } else {
                    ruledescription = commissionTypeName + " between <b>" + lowerLimit + "</b> to <b>" + upperLimit + " </b>";
                    this.lowerLimit.reset();
                    this.upperLimit.reset();
                }
            }

            var items = this.salesCommissionSchemaRuleStore.getRange();
            var length = items.length;
            if (isCondition) {
                ruledescription = items[length - 1].data.ruledescription + " & " + ruledescription;
            }else{
                this.ruleIndex++;
            }

            /*
             * To store conditions
             */
            var condition = {};
            var conditionArr = [];
            condition.commissiontype = commissionType;
            condition.lowerlimit = (lowerLimit !== undefined && lowerLimit !== "") ? lowerLimit : 0;
            condition.upperlimit = (upperLimit !== undefined && upperLimit !== "") ? upperLimit : 0;
            condition.categoryid = (productCategoryId !== undefined && productCategoryId !== "") ? productCategoryId : "";
            condition.marginCondition = marginCondition;
            conditionArr.push(condition);
            
            if (isCondition) {
                /*
                 * Condition appeded to last rule
                 */
                var conditionArr2 = this.salesCommissionSchemaRuleStore.getAt(length - 1).data.conditionArr;
                if (conditionArr2 != undefined && conditionArr2 != "") {
                    for (var conditionIndex = 0; conditionIndex < conditionArr2.length; conditionIndex++) {
                        if (conditionArr2[conditionIndex].commissiontype == commissionType) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.salesCommission.duplicate.condition.alert")], 2);
                            return;
                        }
                    }
                    conditionArr2.push(condition);
                    var rec = this.getSalesCommissionSchemaRuleRec(schemaTypeId, schemaType, amount, ruledescription, conditionArr2);
                    this.salesCommissionSchemaRuleStore.remove(this.salesCommissionSchemaRuleStore.getAt(length - 1));
                    this.salesCommissionSchemaRuleStore.insert(length - 1, rec);
                } else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.salesCommission.add.condition.alert")], 2);
                }
            } else {
                /*
                 * duplicate commission schema not allowed
                 */
                var isDuplicate = false;
                var duplicateRuleIndex = 0;
                var rec = this.salesCommissionSchemaRuleStore.getRange();
                var newRecord = this.getSalesCommissionSchemaRuleRec(schemaTypeId, schemaType, amount, ruledescription, conditionArr);
                if (rec.length > 0) {
                    for (var i = 0; i < rec.length; i++) {
                        if (schemaTypeId == rec[i].data.schemaTypeId && amount == rec[i].data.amount) {
                            isDuplicate = true;
                            duplicateRuleIndex = i;
                            break;
                        }
                    }
                }
                if (isDuplicate) {
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.salesCommission.add.condition.override.alert"), function (btn) {
                    if (btn == "yes") {
                        if (rec[duplicateRuleIndex].data.ruleid != undefined && rec[duplicateRuleIndex].data.ruleid != "") {
                            var ruleid = rec[duplicateRuleIndex].data.ruleid;
                            Wtf.Ajax.requestEx({
                                url: "AccSalesCommission/deleteSalesCommissionRules.do",
                                params: {
                                    ruleid: ruleid
                                }
                            }, this,
                            function (req, res) {
                            if (req.success) {
                                var rec2 = WtfGlobal.searchRecord(this.salesCommissionSchemaRuleStore, ruleid, "id");
                                this.salesCommissionSchemaRuleStore.remove(rec2);
                                this.salesCommissionSchemaRuleStore.insert(duplicateRuleIndex, newRecord);
                            } else{
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), req.msg], 1);
                            }
                            },
                            function (req) {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.salesCommissionRules.delete.error")], 1);
                            });
                        } else {
                            var rec2 = WtfGlobal.searchRecord(this.salesCommissionSchemaRuleStore, rec[duplicateRuleIndex].data.ruleIndex, "id");
                            this.salesCommissionSchemaRuleStore.remove(rec2);
                            this.salesCommissionSchemaRuleStore.insert(duplicateRuleIndex, newRecord);
                        }
                    }
                }, this);
                }else{
                    this.salesCommissionSchemaRuleStore.insert(length, newRecord);
                }
            }
        }
    },
    getSalesCommissionSchemaRuleRec: function (schemaTypeId,schemaType, amount, ruledescription, conditionArr) {
        var rec = new this.salesCommissionSchemaRuleRec({
            id: this.ruleIndex,
            ruleIndex: this.ruleIndex,
            schemaTypeId: schemaTypeId,
            schemaType: schemaType,
            amount: amount,
            ruledescription: ruledescription,
            conditionArr: conditionArr
        });
        return rec;
    },
    saveSchemaMaster: function () {
        var items = this.salesCommissionSchemaRuleStore.getRange();
        this.schemaMaster.setValue(this.schemaMaster.getValue().trim());
        if (!this.schemaMasterForm.form.isValid()) {
            WtfComMsgBox(2, 2);
            return;
        } else if (items.length === 0) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.salesCommission.save.addRules.alert")], 2);
            return;
        } else {
            var finalDataArr = [];
            var isDuplicateMaster = false;
            var schemaMaster = this.schemaMaster.getValue();
            var gridCount = Wtf.getCmp('salesCommissionSchemaMaster').Store.getCount();
            var masterStore = Wtf.getCmp('salesCommissionSchemaMaster').Store;

            for (var cnt = 0; cnt < gridCount; cnt++) {
                if ((schemaMaster == masterStore.getAt(cnt).data.schemaMaster) && !((this.isEdit && schemaMaster == this.schemaMasterName))) {
                    isDuplicateMaster = true;
                    break;
                }
            }
            if (isDuplicateMaster) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.salesCommission.add.schemaMaster.duplicate.alert")], 2);
                return;
            } else {
                this.salesCommissionSchemaRuleStore.each(function (rec) {
                    if (rec.data !== undefined && rec.data.conditionArr != undefined && rec.data.conditionArr != "") {
                        var ruleObj = {};
                        ruleObj.schemaType = rec.data.schemaTypeId;
                        ruleObj.amount = rec.data.amount;
                        ruleObj.ruledescription = rec.data.ruledescription;
                        ruleObj.conditionArr = rec.data.conditionArr;
                        finalDataArr.push(ruleObj);
                    }
                }, this);
                var rec = {};
                rec.schemaMaster = this.schemaMaster.getValue();
                if (this.isEdit) {
                    rec.schemaMasterId = this.schemaMasterId;
                }
                rec.rulesDetail = JSON.stringify(finalDataArr);
                Wtf.Ajax.requestEx({
                    url: "AccSalesCommission/saveSalesCommissionSchemaMaster.do",
                    params: rec
                }, this, this.genSuccessResponse, this.genFailureResponse);
            }
        }
    },
    genSuccessResponse: function (req, res) {
        var restext = req;
        if (restext.success) {
            this.disableField();
            this.close();
            Wtf.getCmp('salesCommissionSchemaMaster').Store.reload();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.master.SalsCommissin.Save")], 0);
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.master.SalsCommissin.error")], 1);
        }
    },
    genFailureResponse: function (req, res) {
        var restext = req;
        if (restext.msg != "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), restext.msg], 1);
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.master.SalsCommissin.error")], 1);
        }
    },
    deleteCommissionRules: function (gd, ri, ci, e) {
        var event = e;
        if (event.target.className == "pwnd delete-gridrow" && this.rulesGrid.getSelectionModel().hasSelection()) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.salesCommissionRules.delete.alert"), function (btn) {
                if (btn == "yes") {
                    var rec = this.rulesGrid.getSelectionModel().getSelected();
                    if (rec.data.ruleid != undefined && rec.data.ruleid!="") {
                        Wtf.Ajax.requestEx({
                            url: "AccSalesCommission/deleteSalesCommissionRules.do",
                            params: {
                                ruleid: rec.data.ruleid
                            }
                        }, this,
                            function (req, res) {
                                if (req.success) {
                                    var rec2 = WtfGlobal.searchRecord(this.salesCommissionSchemaRuleStore,rec.data.ruleid,"id");
                                    this.salesCommissionSchemaRuleStore.remove(rec2);
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), req.msg], 0);
                                } else
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), req.msg], 1);
                            },
                            function (req) {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.salesCommissionRules.schema.delete.error")], 1);
                            });
                    }else{
                        var rec2 = WtfGlobal.searchRecord(this.salesCommissionSchemaRuleStore,rec.data.ruleIndex,"id");
                        this.salesCommissionSchemaRuleStore.remove(rec2);
                    }
                }
            }, this);
        }
    },
    reloadGridStore: function () {
        this.salesCommissionSchemaRuleStore.load();
    },
    disableField: function () {
        this.schemaMaster.disable();
        this.schemaType.disable();
        this.percentageValue.disable();
    },
    closeWin: function () {
        this.close();
    }
});
