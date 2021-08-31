/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 */

function callProductBrandDiscountDynamicLoad(record) {
    var win = new Wtf.account.productBrandDiscount({
        title: WtfGlobal.getLocaleText("acc.field.Set") + " " + WtfGlobal.getLocaleText("acc.invoice.discount"), // "Set Discount",
        tabTip: WtfGlobal.getLocaleText("acc.field.Set") + " " + WtfGlobal.getLocaleText("acc.invoice.discount"),
        id: "productBrandDiscount",
        layout: 'border',
        modal: true,
        width: 1100,
        height: 620,
        iconCls: getButtonIconCls(Wtf.etype.deskera),
        closable: true,
        resizable: false,
        record: record
    });
    
    win.show();
}

/**
 * Component of Product Brand Discount Window.
 */
Wtf.account.productBrandDiscount = function(config) {
    Wtf.apply(this, config);
    
    Wtf.apply(this,{
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.OK"), // 'OK',
            scope: this,
            handler: this.closeWin
        }]
    },config);
    
    Wtf.account.productBrandDiscount.superclass.constructor.call(this, config);
}

/**
 * Extend component of Product Brand Discount Window.
 */
Wtf.extend(Wtf.account.productBrandDiscount, Wtf.Window, {
    /**
     * Logic for rendering Product Brand Discount Window.
     */
    onRender: function(config) {
        Wtf.account.productBrandDiscount.superclass.onRender.call(this, config);
        
        var northPanel =  new Wtf.Panel({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white; border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(WtfGlobal.getLocaleText("acc.field.Set") + " " + WtfGlobal.getLocaleText("acc.invoice.discount"), WtfGlobal.getLocaleText("acc.setProductBrandDiscount.desc"), "../../images/accounting_image/price-list.gif")
        });
        
        var centerPanel = new Wtf.Panel({
            region: 'center',
            layout: 'fit',
            autoScroll: true,
            autoHeight: true,
            border: false,
            bodyStyle: 'background:#f1f1f1; font-size:10px; padding:10px',
            baseCls: 'bckgroundcolor',
            items: [this.discountForm, this.discountFieldSet]
        });
        
        this.add(northPanel);
        this.add(centerPanel);
        
        this.loadGridStore();
    },
    
    /**
     * For initializing Product Brand Discount Window fields .
     */
    initComponent: function(config) {
        Wtf.account.productBrandDiscount.superclass.initComponent.call(this, config);
        
        this.createFields();
        this.createGrid();
        this.createForm();
        this.createFieldSet();
    },
    
    /**
     * Create Product Brand Discount Window fields .
     */
    createFields: function() {
        this.pricingBandRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);

        this.pricingBandStore = new Wtf.data.Store({
            url: "ACCMaster/getPricingBandItems.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: "totalCount",
                root: "data"
            }, this.pricingBandRec)
        });
        
        this.bandCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.bandName") + "*", // "Band Name" + "*"
            id: "bandNameCombo" + this.id,
            store: this.pricingBandStore,
            displayField: 'name',
            valueField: 'id',
            emptyText: WtfGlobal.getLocaleText("acc.field.selectBandName"), // 'Select Band Name',
            mode: 'remote',
            width: 300,
            name: 'name',
            hiddenName:'name',
            extraFields: '',
            triggerAction: 'all',
            allowBlank: false,
            editable: false,
            forceSelection: true,
            selectOnFocus: true,
            scope: this
        });
        if (this.record != undefined) {
            this.bandCombo.setValForRemoteStore(this.record.data.id, this.record.data.name);
        }
        this.bandCombo.on('select', this.handleBandComboOnSelect, this);
        
        this.discountTypeStore = new Wtf.data.SimpleStore({
            fields: [{name:'typeid',type:'int'}, 'name'],
            data: [[0,'Flat'], [1,'Percentage']]
        });
        
        this.discountTypeCombo = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.discountType") + "*", // "Discount Type" + '*',
            emptyText: WtfGlobal.getLocaleText("acc.field.selectDiscountType"), // "Select Discount Type",
            store: this.discountTypeStore,
            name: 'typeid',
            displayField: 'name',
            valueField: 'typeid',
            mode: 'local',
            triggerAction: 'all',
            forceSelection: true,
            selectOnFocus: true,
            scope: this,
            allowBlank: false,
            editable: false,
            width: 300,
            listWidth: 300
        });
        this.discountTypeCombo.setValue(0);
        this.discountTypeCombo.on('select', this.handleDiscountTypeOnSelect, this);
        
        this.currencyRec = new Wtf.data.Record.create([
            {name: 'currencyid', mapping:'tocurrencyid'},
            {name: 'symbol'},
            {name: 'currencycode'},
            {name: 'currencyname', mapping:'tocurrency'},
            {name: 'exchangerate'},
            {name: 'htmlcode'}
         ]);
         
         this.currencyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            },this.currencyRec),
            url: "ACCCurrency/getCurrencyExchange.do"
         });
         
         this.currencyCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.currency.tt") + "'>" + WtfGlobal.getLocaleText("acc.currency.cur") + "*" + "</span>", // 'Currency' + '*',
            emptyText: WtfGlobal.getLocaleText("acc.field.SelectCurrency"),
            allowBlank: false,
            typeAhead: true,
            forceSelection: true,
            triggerAction: 'all',
            id: 'currencyField',
            store: this.currencyStore,
            displayField: "currencyname",
            valueField: 'currencyid',
            mode: 'remote',
            editable: false,
            hiddenName: 'currency',
            extraFields: '',
            width: 300
        });
        this.currencyCombo.setValForRemoteStore(WtfGlobal.getCurrencyID(), WtfGlobal.getCurrencyName());
        this.currencyCombo.on('select', this.handleCurrencyOnSelect, this);
        
        this.applicableDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.applicableDate") + "*", // 'Applicable Date' + '*',
            name: 'applydate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: Wtf.serverDate.clearTime(true),
            width: 300,
            allowBlank: false
        });
        this.applicableDate.on('change',this.onDateChange, this);
        /**
         * Dimension field for selection 
         */
        
        this.dimensionListRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
        
        this.dimensionListStore = new Wtf.data.Store({
            url: "ACCAccountCMN/getDimensionsForCombo.do",
            baseParams: {
                moduleid: Wtf.Acc_Product_Master_ModuleId
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.dimensionListRec)
        });
        this.dimensionListStore.load();
        
        this.dimensionList = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.budgeting.dimension"), // "Dimension",
            emptyText: WtfGlobal.getLocaleText("acc.field.pleaseSelectDimension"), // "Please select dimension.",
            store: this.dimensionListStore,
            forceSelection: true,
            width: 300,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            selectOnFocus: true,
            triggerAction: 'all',
            typeAhead: true,
            scope: this
        });
        this.dimensionList.on('select', this.createColumnFromDim, this);
        
        this.isCustomerCategory = new Wtf.form.Checkbox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.isCustomerCategory"), // "Is Customer Category?",
            name: 'useCommonDiscount',
            checked: false,
            cls: 'custcheckbox',
            style: "margin-left: 0px;"
        });
        this.isCustomerCategory.on('check',this.handleCustomerCategoryCheck, this);
    },
    
    /**
     * Create Product Brand Discount Window Grid Panel .
     */
    createGrid: function() {
        this.createGridTBar();
        
        this.gridStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            }),
            url: "ACCProductCMN/getProductBrandDiscountDetails.do"
        });
        
        this.gridStore.on('beforeload', this.handleGridBeforeLoad, this);
        this.gridStore.on('load', this.handleGridOnLoad, this);
        this.gridStore.on('datachanged', this.handleGridDatachanged, this);
        
        this.grid = new Wtf.grid.EditorGridPanel({
            layout: 'fit',
            store: this.gridStore,
            columns: [],
            clicksToEdit: 1,
            autoScroll: true,
            height: 280,
            width: 1000,
            border: false,
            loadMask: true,
            cls: 'vline-on',
            viewConfig: {
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            },
            tbar: this.gridTBar,
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbarofdetails" + this.id,
                store: this.gridStore,
                searchField: this.quickPanelSearch,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                plugins: this.pP = new Wtf.common.pPageSize()
            })
        });
        
        this.grid.on('afteredit', this.saveRecord, this);
    },
    /**
     * Create column model for dimension values
     */
    createColumnFromDim: function(){
      this.gridStore.load();
    },
    /**
     * Create grid tool bar.
     */
    createGridTBar: function() {
        this.gridTBar = [];
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.setProductBrandDiscount.QuickSearchEmptyText"), // "Search by Customer Name or Customer Category ...",
            width: 200,
            id: "setPriceQuickSearch" + this.id,
            field: 'productName'
        });
        this.gridTBar.push(this.quickPanelSearch);
        
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), // 'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), // 'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.resetBttn.on('click', this.handleResetClick, this);
        this.gridTBar.push(this.resetBttn);
    },
    
    /**
     * for creating field set of set Discount
     */
    createFieldSet: function() {
        this.discountFieldSet = new Wtf.form.FieldSet({
            xtype: 'fieldset',
            style: 'margin-top: 10px',
            cls: "visibleDisabled",
            title: WtfGlobal.getLocaleText("acc.field.Set") + " " + WtfGlobal.getLocaleText("acc.invoice.discount"), // "Set Discount",
            layout: 'fit',
            autoHeight: true,
            border: false,
            items: [this.grid]
        });
    },
    
    /**
     * Create Product Brand Discount Window Form Panel.
     */
    createForm: function() {
        this.discountForm = new Wtf.form.FormPanel({
            waitMsgTarget: true,
            border: false,
            autoHeight: true,
            labelWidth: 200,
            disabledClass: "newtripcmbss",
            items: [
                this.bandCombo,
                this.discountTypeCombo,
                this.currencyCombo,
                this.applicableDate,
                this.dimensionList,
                this.isCustomerCategory
            ]
        });
    },
    
    /**
     * For handling Reset click event
     */
    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.loadGridStore();
        }
    },
    
    /**
     * For handling Band Combo change event
     */
    handleBandComboOnSelect: function(combo, record, index) {
        this.loadGridStore();
    },
    
    /**
     * For handling Discount Type change event
     */
    handleDiscountTypeOnSelect: function(combo, record, index) {
        this.loadGridStore();
    },
    
    /**
     * For handling Currency change event
     */
    handleCurrencyOnSelect: function(combo, record, index) {
        this.loadGridStore();
    },
    
    /**
     * For handling Date change event
     */
    onDateChange: function(a,val,oldval) {
        this.loadGridStore();
    },
    
    /**
     * For handling Customer Category check event
     */
    handleCustomerCategoryCheck: function() {
        this.loadGridStore();
    },
    
    /**
     * For loading grid store
     */
    loadGridStore: function() {
        this.gridStore.load({
            params: {
                start: 0,
                limit: 30
            }
        });
    },
    
    /**
     * For handling grid before load event
     */
    handleGridBeforeLoad: function() {
        var currentBaseParams = this.gridStore.baseParams;
        currentBaseParams.bandID = this.bandCombo.getValue();
        currentBaseParams.discountType = this.discountTypeCombo.getValue();
        currentBaseParams.currencyID = (this.currencyCombo.getValue()!="")? this.currencyCombo.getValue() : Wtf.account.companyAccountPref.currencyid;
        currentBaseParams.applicableDate = WtfGlobal.convertToGenericDate(this.applicableDate.getValue());
        currentBaseParams.isCustomerCategory = this.isCustomerCategory.getValue();
        currentBaseParams.ss = this.quickPanelSearch.getValue();
        currentBaseParams.dimension = this.dimensionList.getValue();
        this.gridStore.baseParams = currentBaseParams;
        
        if (this.discountForm) {
            this.discountForm.disable();
        }
    },
    
    /**
     * For handling grid on load event
     */
    handleGridOnLoad: function(store) {
        var columns = [];
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));
        
        Wtf.each(this.gridStore.reader.jsonData.columns, function(column) {
            if (column.isBrand == true) {
                column.editor = new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 10,
                    decimalPrecision: Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
                });
            }
            columns.push(column);
        });
        
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();
        
        if (this.discountForm) {
            this.discountForm.enable();
        }
        
        if (this.gridStore.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        } else {
            var rec = this.gridStore.getAt(0);
            if (rec != undefined && rec.data.isCustomerCategoryRecordExist == true) {
                this.isCustomerCategory.removeListener('check',this.handleCustomerCategoryCheck, this);
                this.isCustomerCategory.setValue(true);
                this.isCustomerCategory.addListener('check',this.handleCustomerCategoryCheck, this);
            }
            if (rec != undefined && rec.data.isAnyRuleRecordExist == true) {
                this.isCustomerCategory.setDisabled(true);
            }
        }
        this.quickPanelSearch.StorageChanged(store);
    },
    
    /**
     * For handling grid data changed event
     */
    handleGridDatachanged: function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    },
    
    /**
     * For saving data on after edit event of grid
     */
    saveRecord: function(obj) {
        var params = {
            bandID: this.bandCombo.getValue(),
            discountType: this.discountTypeCombo.getValue(),
            currencyID: this.currencyCombo.getValue(),
            applicableDate: WtfGlobal.convertToGenericDate(this.applicableDate.getValue()),
            isCustomerCategory: this.isCustomerCategory.getValue(),
            customerID: obj.record.data.customerUUID,
            column_Name: obj.field,
            column_Value: obj.value,
            dimension:this.dimensionList.getValue()
        };
        
        var URL = "ACCProductCMN/saveProductBrandDiscountDetails.do";
        
        Wtf.Ajax.requestEx({
            url: URL,
            params: params
        }, this, function (response) {
            if (response.success) {
                obj.record.commit();
                this.gridStore.reload();
                if (response.msg) {
                    msg = response.msg;
                }
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Success"), msg], 4);
            } else {
                var msg = WtfGlobal.getLocaleText("acc.common.msg1"); // "Failed to make connection with Web Server";
                if(response.msg) {
                    msg = response.msg;
                }
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
            }
            
        }, function (response) {
            var msg = WtfGlobal.getLocaleText("acc.common.msg1"); // "Failed to make connection with Web Server";
            if(response.msg) {
                msg = response.msg;
            }
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        });
    },
    
    /**
     * For closing Product Brand Discount Window.
     */
    closeWin: function() {
        this.close();
    }
});