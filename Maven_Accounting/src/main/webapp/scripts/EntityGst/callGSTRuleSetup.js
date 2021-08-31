/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//######################## GST Rule Setup ########################
function callGSTRuleSetup(winid,isoutputtax) {
    var cmpId = 'GSTRuleSetup';
    var panel = Wtf.getCmp(cmpId);
    if (panel == null) {
        panel = new Wtf.account.GSTRuleSetup({
            layout: 'fit',
            border: false,
            isoutputtax:isoutputtax!=undefined?isoutputtax:false
        });
    }
    Wtf.getCmp('as').doLayout();

}

Wtf.account.GSTRuleSetup = function(config) {
    this.arr = [];
    Wtf.apply(this, config);

    //********* Product Category **************************
    this.productCategoryRec = Wtf.data.Record.create([
        {name: 'id'},
        {name: 'name'}
    ]);

    this.productCategoryStore = new Wtf.data.Store({
        url: "AccEntityGST/getFieldComboDataForModule.do",
        baseParams: {
            moduleid: Wtf.Acc_Product_Master_ModuleId, // Product Module ID
            fieldlable: Wtf.GSTProdCategory
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.productCategoryRec)
    });

    this.productCategory = new Wtf.form.ComboBox({
        fieldLabel: Wtf.GSTProdCategory+" *",
        name: 'id',
        store: this.productCategoryStore,
        valueField: 'id',
        displayField: 'name',
        mode: 'local',
        typeAhead: true,
        triggerAction: 'all',
        emptyText: WtfGlobal.getLocaleText("acc.field.PleaseselectProductTaxClass"),
        width: 240,
        listWidth: 240
    });

    this.productCategoryStore.load();
    this.productCategory.on('select', function() {
        this.GSTRuleSetupStore.load()
    }, this);

    //********* From Entity **************************
    this.EntityComboRec = Wtf.data.Record.create([
        {name: 'id'},
        {name: 'name'}
    ]);

    this.EntityComboStore = new Wtf.data.Store({
        url: "AccEntityGST/getFieldComboDataForModule.do",
        baseParams: {
            moduleid: Wtf.Acc_EntityGST,
            isMultiEntity: true
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.EntityComboRec)
    });

    this.EntityCombo = new Wtf.form.ComboBox({
        fieldLabel: WtfGlobal.getLocaleText("Entity")+" *",
        hiddenName: 'id',
        name: 'id',
        store: this.EntityComboStore,
        valueField: 'id',
        displayField: 'name',
        mode: 'local',
        typeAhead: true,
        triggerAction: 'all',
        emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseselectentity"),
        width: 240,
        listWidth: 240
    });

    this.EntityComboStore.load();

    this.EntityCombo.on('select', function() {
        this.GSTRuleSetupStore.load()
    }, this);
//    
    //************ Applicable Date *************
    this.applicableDate = new Wtf.form.DateField({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.applicableDate")+" *", // 'Applicable Date',
        name: 'applydate',
        format: WtfGlobal.getOnlyDateFormat(),
        value: new Date(),
        width: 240,
        allowBlank: false
    });

    this.applicableDate.on('change', function() {
        this.GSTRuleSetupStore.load()
    }, this);
    this.applicableDate.on('render', function(c) {
        Wtf.QuickTips.register({
            target: c.getEl(),
            text: ""
        });
    }, this);

    this.isSales = new Wtf.form.Checkbox({
        name: 'issales',
        fieldLabel: WtfGlobal.getLocaleText("acc.field.isSales"),
        checked: config.isoutputtax!=undefined?config.isoutputtax:false,
        cls: 'custcheckbox',
        width: 10
    });
    this.isSales.on('check',function(){
        this.GSTRuleSetupStore.load()
    },this);
        
    //**************** Create Grid *********************
    this.createGrid();

    //*************** Lead Panel ******************************
    this.GSTRuleSetupWin = new Wtf.Window({
        title: WtfGlobal.getLocaleText("GST Rule Setup"), // "GST Rule Setup",
        resizable: false,
        width: 1100,
        height: 625,
        modal: true,
        layout: 'border',
        iconCls: getButtonIconCls(Wtf.etype.deskera),
        scope: this,
        closeAction: "hide",
        buttons: [{
                text: WtfGlobal.getLocaleText("acc.msgbox.ok"), // "OK"
                scope: this,
                handler: function() {
                    this.GSTRuleSetupWin.close();
                }
            }],
        items: [{
                region: 'north',
                height: 65,
                border: false,
                bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                html: getTopHtml(WtfGlobal.getLocaleText("GST Rule Setup"), WtfGlobal.getLocaleText("GST Rule Setup"), "../../images/accounting_image/price-list.gif")
            }, {
                region: 'center',
                layout: 'fit',
                border: false,
                bodyStyle: 'background:#f1f1f1;padding:15px',
                items: [{
                        xtype: "form",
                        border: false,
                        id: this.id + "setpricefrm",
                        autoScroll: true,
                        labelWidth: 150,
                        items: [this.productCategory, this.EntityCombo, this.applicableDate,this.isSales,{
                                xtype: 'fieldset',
                                width: (Wtf.isIE) ? '91%' : '85%',
                                height: '75%',
                                style: 'margin-top: 20px;',
                                title: WtfGlobal.getLocaleText("GST Rule Setup"),
                                border: false,
                                labelWidth: 150,
                                items: [this.grid]
                            }]
                    }]
            }]
    });

    this.GSTRuleSetupWin.show();

    Wtf.account.GSTRuleSetup.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.GSTRuleSetup, Wtf.Panel, {
    handleBeforeLoad: function(s, o) {
        this.GSTRuleSetupStore.groupBy(undefined);
        if (!o.params)
            o.params = {};
        o.params.start = 0;
        o.params.limit = 30;
        o.params.entity = this.EntityCombo.getValue();
        o.params.productcategory = this.productCategory.getValue();
        o.params.transactiondate = WtfGlobal.convertToGenericDate(this.applicableDate.getValue());
        o.params.isSales=this.isSales.getValue();
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


        Wtf.each(this.GSTRuleSetupStore.reader.jsonData.columns, function(column) {
            if (column.renderer) {
                column.renderer = eval('(' + column.renderer + ')');
            }
            else {
            column.renderer = WtfGlobal.deletedRenderer;
            }
            columns.push(column);
        });
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();

        if (this.GSTRuleSetupStore.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }else{
            /**
             * ERP-34044
             */
            if (columns.length > 1 && Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA) {
                this.GSTRuleSetupStore.sortInfo = {
                    field: 'groupTerm',
                    direction: 'ASC'
                }
                this.grid.getStore().groupBy('groupTerm');
                this.grid.getView().refresh();
            }
        }
    },
    createGrid: function() {

        this.sm = new Wtf.grid.CheckboxSelectionModel({
        });

        this.GSTRuleSetupStore =  new Wtf.data.GroupingStore({
            url: "AccEntityGST/getGSTRuleSetup.do",
            baseParams: {
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'count'
            }),
            sortInfo : {
                field : 'groupTerm',
                direction : 'ASC'
            }

        });
        this.GSTRuleSetupStore.on('beforeload', this.handleBeforeLoad, this);
        this.GSTRuleSetupStore.load();
        this.GSTRuleSetupStore.on('load', this.handleStoreOnLoad, this);
        
        var grpView = new Wtf.grid.GroupingView({
            startCollapsed: false,
            forceFit: false,
            showGroupName: true,
            enableGroupingMenu: false,
            hideGroupedColumn: true
        });
        this.grid = new Wtf.grid.EditorGridPanel({
            //id: 'GSTRuleSetup' + this.id,
            store: this.GSTRuleSetupStore,
            border: false,
            columns: [],
            layout: 'fit',
            height: 300,
            width: 1000,
            autoScroll: true,
            sm: this.sm,
            view: grpView,
            viewConfig: {
                forceFit: false,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            },
            loadMask: true,
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbarofdetails" + this.id,
                store: this.GSTRuleSetupStore,
//                searchField: this.setPriceQuickPanelSearch,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                plugins: this.pPSetPrice = new Wtf.common.pPageSize({
                    id: "detailspPageSize_" + this.id
                }),
//                items: [this.exportButton, this.POSSyncBttn]
            })
        });
    }

});



  
