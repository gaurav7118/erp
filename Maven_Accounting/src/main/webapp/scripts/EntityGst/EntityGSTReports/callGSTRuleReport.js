/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function  callGSTRuleReportDynamicLoad(params) {
    if(params==undefined){
        params = {};
    }
    var id = params.id;
    var tabtitle = params.tabtitle;
    var panel1 = Wtf.getCmp(id);
    if (panel1 == null) {
        var panel1 = new Wtf.account.GSTRuleReport({
            title: WtfGlobal.getLocaleText(tabtitle),
            id: id
        });
        Wtf.getCmp('as').add(panel1);
    }
    Wtf.getCmp('as').setActiveTab(panel1);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.GSTRuleReport = function (config) {
    Wtf.apply(this, config);
    this.closable = true;
    this.createGridNew();
    this.layout = 'fit';
    this.items = this.grid1;
    Wtf.account.GSTRuleReport.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.GSTRuleReport, Wtf.Panel, {
    handleStoreBeforeLoad: function(s, o){
        this.GSTRuleReportStore.groupBy(undefined);
        if (!o.params) {
            o.params = {};
        }
        o.params.startDate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        o.params.endDate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        o.params.entity = this.EntityCombo.getValue();
        o.params.isSales = this.isSales;
        o.params.ss = this.quickPanelSearch.getValue();
    }, 
    createGridNew: function () {
        this.sm = new Wtf.grid.CheckboxSelectionModel({});
        this.GSTRuleReportStore = new Wtf.data.GroupingStore({
            url: "AccEntityGST/getGSTRuleReport.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'totalCount'
            })
        },this);
        
        this.isSales = (this.id==="GSTInputRuleReport"?false:true);
        
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
            fieldLabel: WtfGlobal.getLocaleText("Entity"),
            name: 'entity',
            store: this.EntityComboStore,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            triggerAction: 'all',
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseselectentity"),
            width: 170,
            listWidth: 170
        });
        
        this.startDate = new Wtf.ExDateFieldQtip({
            name: 'stdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.getDates(true)
        });
        this.endDate = new Wtf.ExDateFieldQtip({
            name: 'enddate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.getDates(false)
        });
        
        this.EntityComboStore.on('load', function () {
            var rec = this.EntityComboStore.getAt(0);
            if (rec != null) {
                var id = rec.data.id;
                this.EntityCombo.setValue(id);
            }
            /**
             * Load GSTRuleReportStore after the entity ComboStore is loaded.
             */
            this.GSTRuleReportStore.load({
                params: {
                    start: 0,
                    limit: 30
                }
            });
        }, this);
        
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.gstrr.filterMessage"),
            width: 150,
            field: 'percentage',
            Store: this.GSTRuleReportStore
        });

        this.EntityComboStore.load();
        this.GSTRuleReportStore.on('beforeload', this.handleStoreBeforeLoad,  this);
        this.GSTRuleReportStore.on('load', this.handleStoreOnLoad, this);
        
        this.pt = new Wtf.PagingSearchToolbar({
                pageSize: 30,

                id: "pagingtoolbar" + this.id,
                store: this.GSTRuleReportStore,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                
                plugins: this.pPSetPrice = new Wtf.common.pPageSize({
                    id: "detailspPageSize_" + this.id
                })
            });
        /**
         * Top Toolbar Candies
         */          
        this.deleteButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.delete"),  //"Delete 
            handler: function () {
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.gstrr.confirmationMessage"), function (btn) {
                    if (btn == 'yes') {
                        this.deleteItem();
                    }
                }, this);
            },
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.deletebutton),
            disabled:true
        });
        this.addButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.add"),  //"Add 
            handler:this.addRule,
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.add),
        });
        
        this.termRec =new Wtf.data.Record.create([
        {name: 'id'},
        {name: 'termid'},
        {name:'productentitytermid'},
        {name: 'term'},
        {name: 'termtype'},
        {name: 'glaccount'},
        {name:'payableaccountid'},
        {name:'creditnotavailedaccount'},
        {name:'creditnotavailedaccountname'},
        {name: 'sign'},
        {name: 'formula'},
        {name: 'termformulaids'},
        {name: 'formulaids'},
        {name: 'taxtype'},
        {name: 'taxvalue'},
        {name: 'termamount'},
        {name: 'termpercentage'},
        {name: 'invquantity'},
        {name: 'invAmount'},
        {name: 'recTermAmount'},
        {name: 'OtherTermNonTaxableAmount'},
        {name: 'productid'},
        {name: 'assessablevalue'},
        {name: 'glaccountname'},
        {name:'payableglaccountname'},
        {name: 'accountid'},
        {name: 'isDefault'},
        {name: 'producttermmapid'},
        {name: 'purchasevalueorsalevalue'},
        {name: 'deductionorabatementpercent'},
        {name: 'termsequence'},
        {name: 'formType'},
        {name: 'isadditionaltax'},
        {name: 'includeInTDSCalculation'},
        {name: 'IsOtherTermTaxable'},
        {name: 'isTermTaxable'},
        {name: 'masteritem'}
            ]);
        
        this.termStore = new Wtf.data.Store({
            url: 'ACCAccount/getIndianTermsCompanyLevel.do',
            baseParams:{
                isSalesOrPurchase:this.isSales,                
                isAdditionalTax:false,
                termType:""
            },
            sortInfo: {
                field: 'termsequence',
                direction: 'ASC'
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.termRec)
        });
        
        var extraConfig = {};
        if (this.isSales) {
            extraConfig.url = "AccEntityGST/importOutputGSTRuleSetup.do";
        } else {
            extraConfig.url = "AccEntityGST/importInputGSTRuleSetup.do";
        }
        this.module = 'GSTTerm';
        var extraParams = this.isCustomer;
        extraConfig.isSales = this.isSales;
        extraConfig.isExcludeXLS = true;
        var importBtnArray = Wtf.documentImportMenuArray(this, this.module, this.GSTRuleReportStore, extraParams, extraConfig);

        this.importBtn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.common.import"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.common.import"),
            iconCls: (Wtf.isChrome ? 'pwnd importChrome' : 'pwnd import'),
            menu: importBtnArray
        });        

        this.editButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.edit"),  //"Edit 
            handler:this.editRule,
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.edit),
            disabled:true
        });
        this.fetchButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.loadStore
        });

        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.resetBttn.on('click', this.handleResetClick, this);
        this.tbar1 = [];
        this.tbar1.push(this.quickPanelSearch, "-", this.resetBttn,"-",WtfGlobal.getLocaleText("Entity"),'-', this.EntityCombo,'-',WtfGlobal.getLocaleText("acc.common.from"), '-', this.startDate, "-", WtfGlobal.getLocaleText("acc.common.to"), this.endDate, "-", this.fetchButton, "-",this.addButton, "-", this.editButton, "-",this.deleteButton,"-",this.importBtn);
        this.grid1 = new Wtf.grid.GridPanel({
            header: false,
            closable: true,
            width: 300,
            height: 200,
            store: this.GSTRuleReportStore,
            tbar: this.tbar1,
            bbar: this.pt,
            sm: this.sm,
            columns: [],
            autoScroll: true,
            view: new Wtf.grid.GroupingView({
                forceFit: false
            })
        });
        this.grid1.on('rowclick',this.activateDeactivateButtons, this);
        /**
         * Check to delete and edit button activation on header clicked to select
         */
        this.grid1.on('headerclick',function(object,columnIndex){
            if(columnIndex==1){
                this.activateDeactivateButtons();
            }
        }, this);
        this.GSTRuleReportStore.on('load',this.activateDeactivateButtons, this);
        /**
         * Create CESS Type Store
         */
        this.CESSCalculationTypeRecord = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
        this.CESSCalculationTypeComboStore = new Wtf.data.Store({
            url: "AccEntityGST/getCESSCalculationType.do",
            baseParams: {
                moduleid: Wtf.Acc_EntityGST,
                isMultiEntity: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.CESSCalculationTypeRecord)
        });
        if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
            this.CESSCalculationTypeComboStore.load();
        }
        
    },
    editRule: function () {
        var sm = this.grid1.getSelectionModel().getSelections();
        if (sm.length == 1) {
            var item = this.grid1.getSelectionModel().getSelected();
            this.addRule(undefined, undefined, item,true);
        } else {
            /**
             * This will execute when none of the event is fired.
             */
            this.editButton.disable();
        }
    },
    saveRule: function () {
        var data = "";
        var isFormValid = true;
        /**
         * Make JSON in the format where as used by existing function 
         * saveGSTRuleSetup() in AccEntityGstServiceImpl.java
         */
        Wtf.each(this.addRuleForm.items.items, function (item) {
            /**
             * Need to check typeof item.isValid=='function' because For INDIA GST 
             *  CESS Calcualtion Fieldset added and we dont need to check fieldset is valid, Below already validate this fieldset items
             */
            if (typeof item.isValid=='function' && !item.isValid()) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.setupWizard.note15")], 2); //Enter appropriate value for fields marked in red.
                isFormValid = false;
            } else {
                if (item.name.match("shippedLoc")) {
                    if (item.name == 'shippedLoc1') {
                        data += "\"shiplocation1\": \"" + item.getValue() + "\",";
                    } else if (item.name == 'shippedLoc2') {
                        data += "\"shiplocation2\": \"" + item.getValue() + "\",";
                    } else if (item.name == 'shippedLoc3') {
                        data += "\"shiplocation3\": \"" + item.getValue() + "\",";
                    } else if (item.name == 'shippedLoc4') {
                        data += "\"shiplocation4\": \"" + item.getValue() + "\",";
                    } else if (item.name == 'shippedLoc5') {
                        data += "\"shiplocation5\": \"" + item.getValue() + "\",";
                    }
                } else if (item.name == 'percentage') {
                    data += "\"prodcategory\": \"" + item.getValue() + "\",";
                } else if (item.name == 'applieddate') {
                    var d = item.getValue();
                    var applieddate = d.format('d/m/Y');
                    data += "\"applieddate\": \"" + applieddate + "\",";
                } else if(item.name == Wtf.CESSCalculationFieldSet){
                    /**
                     * Get INDIA GST 'CESS' Term caclulation details 
                     */
                    var CESSItemObj  = item.items.items;
                    for (var cessItem = 0; cessItem < CESSItemObj.length; cessItem++) {
                        if (CESSItemObj[cessItem].getValue() != "") {
                            data += "\"" + CESSItemObj[cessItem].name + "\": \"" + CESSItemObj[cessItem].getValue() + "\",";
                        }
                    }
                } else {
                    if (item.getValue() !== "") {
                        data += "\"" + item.name + "\": \"" + item.getValue() + "\",";
                    }
                }
            }
        }, this);
        if(WtfGlobal.isIndiaCountryAndGSTApplied()){
           isFormValid = this.validateCESSCalculationFields(isFormValid);
        }
        if (isFormValid) {
            var iData = "[{" + data.substring(0, data.length - 1) + "}]";
            Wtf.Ajax.requestEx({
                url: "AccEntityGST/addGSTRule.do",
                scope: this,
                params: {
                    data: iData,
                    isInput: this.isSales,
                    dateFormat: 23
                },
                waitMsg: WtfGlobal.getLocaleText("acc.rem.167"),
            }, this, this.genSuccessResponseClose);
        }
    },
    activateImport: function(object, event){
         this.isSales = (event.data.name=='Input'?false:true);
         this.GSTRuleReportStore.reload();
    },
    activateDeactivateButtons: function(){
        var sm = this.grid1.getSelectionModel().getSelections();
        if((sm.length > 0))
            this.deleteButton.enable();
        else{
            this.deleteButton.disable();
        }
        if (sm.length == 1)
            this.editButton.enable();
        else {
            this.editButton.disable();
        }
    },
    addRule: function(o,e, item,isEdit){
        /**
         * components are created based on column model.
         */
        /**
         * item variable will be undefined in case of adding a rule and 
         * item variable will be defined in case of editing rule.
         */
        this.isUSGST = WtfGlobal.isUSCountryAndGSTApplied();
        if (this.GSTRuleReportStore.reader != undefined) {
            this.addedComponents = [];
            this.addRuleForm = new Wtf.form.FormPanel({
                waitMsgTarget: true,
                border: false,
                region: 'center',
                bodyStyle: "background: transparent;",
                style: "background: transparent;padding:20px;",
                labelWidth: '130',
            });
            //create a config object here
            var headPanelConfig = {
                region: 'north',
                height: 69,
                border: false,
                bodyStyle: 'background:white;border-bottom:1px; solid #bfbfbf;'
            }
            /**
             * html Header will be :
             * 1) Edit GST Rule for edit case
             * 2) Add GST Rule for add case
             */
            if (item == undefined) {
                headPanelConfig.html = getTopHtml(WtfGlobal.getLocaleText("acc.gstrr.addGSTRule"), WtfGlobal.getLocaleText("acc.gstrr.addGSTRule"), "../../images/accounting_image/price-list.gif")
            } else {
                headPanelConfig.html = getTopHtml(WtfGlobal.getLocaleText("acc.gstrr.editGSTRule"), WtfGlobal.getLocaleText("acc.gstrr.editGSTRule"), "../../images/accounting_image/price-list.gif")
            }
            var panel = new Wtf.Panel({
                border: false,
                items: [headPanelConfig, {
                        region: 'center',
                        border: false,
                        bodyStyle: 'background:#f1f1f1;font-size:10px;',
                        autoScroll: true,
                        items: [this.addRuleForm]
                    }]
            });
            this.addRuleWindow = new Wtf.Window({
                modal: true,
                title: (item == undefined ? WtfGlobal.getLocaleText("acc.gstrr.addRule") : WtfGlobal.getLocaleText("acc.gstrr.editRule")),
                iconCls: getButtonIconCls(Wtf.etype.deskera),
                bodyStyle: 'padding:5px;',
                buttonAlign: "right",
                draggable: true,
                width: 460,
                autoHeight: true,
                resizable: false,
                isEdit : isEdit ? true :false,
                scope: this,
                items: [panel],
                buttons: [{
                        text: (item == undefined ? WtfGlobal.getLocaleText("acc.gstrr.addRule") : WtfGlobal.getLocaleText("acc.gstrr.editRule")),
                        scope: this,
                        handler: this.saveRule
                    }, {
                        text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                        scope: this,
                        handler: function () {
                            this.addRuleWindow.close();
                        }
                    }]
            });
            this.addEntityCombo = this.EntityCombo.cloneConfig({
                width: 200,
                allowBlank: false,
                fieldLabel: 'Entity*'
            });
            if (!this.isUSGST) {
                this.addEntityCombo.on('select', this.getTermStateMap, this);
            }
            /**
             * Combo for this store is already loaded, hence we can directly set
             * value.
             */
            if (item != undefined) {
                this.addEntityCombo.setValue(this.EntityCombo.getValue());
                this.addEntityCombo.disable();
            }
            this.addRuleForm.add(this.addEntityCombo);
            /**
             * Add/ Edit GST rule added IS Merchant Exporter check box
             */
            if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
                this.isMerchantExporter = new Wtf.form.Checkbox({
                    name: 'isMerchantExporter',
                    fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.gstrr.gstrule.isMerchantExporter.qtip") + "'>" + WtfGlobal.getLocaleText("acc.gstrr.gstrule.isMerchantExporter") + "</span>",
                    checked: isEdit && item!=undefined && item.data!=undefined && item.data.isMerchantExporter=='Yes' ?  true : false,
                    cls: 'custcheckbox',
                    disabled : isEdit ? true : false,
                    width: 10
                }); 
                this.addRuleForm.add(this.isMerchantExporter);
            }
            this.localStateTerms = [];
            this.otherStateTerms = [];
            this.isCess = false;
            /**
             * Creating Components for form on basis of column model rendered 
             * for grid.
             */
            if (this.GSTRuleReportStore.reader.jsonData != undefined) {
                Wtf.each(this.GSTRuleReportStore.reader.jsonData.columns, function (column) {
                    var temp;
                    if (column.header == WtfGlobal.getLocaleText('acc.address.State') || column.header == WtfGlobal.getLocaleText('acc.gstrr.productTaxClass') || column.header == WtfGlobal.getLocaleText('acc.address.City') || column.header == WtfGlobal.getLocaleText('acc.address.County')) {
                        var termRec = Wtf.data.Record.create([
                            {name: 'id'},
                            {name: 'name'},
                        ]);
                        if (item != undefined) {
                            var value = item.get(column.dataIndex);
                        }
                        var tempStore = new Wtf.data.Store({
                            url: "AccEntityGST/getFieldComboDataForModule.do",
                            baseParams: {
                                moduleid: (column.header != Wtf.GSTProdCategory ? Wtf.Acc_EntityGST : Wtf.Acc_Product_Master_ModuleId),
                                fieldlable: column.header
                            },
                            items: [value],
                            reader: new Wtf.data.KwlJsonReader({
                                root: "data"
                            }, termRec)
                        });
                        temp = new Wtf.form.ComboBox({
                            fieldLabel: column.header + '*',
                            name: column.dataIndex,
                            mode: 'local',
                            typeAhead: true,
                            allowBlank: false,
                            triggerAction: 'all',
                            emptyText: column.header,
                            forceSelection: true,
                            width: 200,
                            listWidth: 200,
                            store: tempStore,
                            valueField: 'id',
                            displayField: 'name'
                        });
                        if (item != undefined) {
                            tempStore.items.push(temp);
                            /**
                             * Assign values in case of edit, when grid is 
                             * loaded.
                             */
                            tempStore.on('load', this.assignComboValues, this);
                        }
                        tempStore.load();
                        if (column.header == 'State' && !this.isUSGST) {
                            temp.on('select', this.getTermStateMap, this);
                            this.addStateCombo = temp;
                        }
                        this.addRuleForm.add(temp);
                    } else if (column.dataIndex == Wtf.applieddate) {
                        temp = new Wtf.ExDateFieldQtip({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.applicableDate") + '*',
                            name: column.dataIndex,
                            width: 200,
                            format: WtfGlobal.getOnlyDateFormat(),
                            value: new Date()
                        });
                        if (item != undefined) {
                            temp.setValue(item.get(column.dataIndex));
                            temp.disable();
                        }
                        this.addRuleForm.add(temp);
                    } else if (this.isUSGST) {
                        if (column.header!=""&&column.hidden!=true) {
                            var temp = new Wtf.form.NumberField({
                                fieldLabel: column.header + '*',
                                name: column.dataIndex,
                                emptyText: column.header,
                                allowBlank: false,
                                allowDecimals: true,
                                width: 200,
                                decimalPrecision: Wtf.account.companyAccountPref.columnPref.gstamountdigitafterdecimal,
                                allowNegative :false,
                                maxValue: 100
                            });
                            /**
                             * In Case of edit if the value for perticular term is blank then don't add perticular component in form
                             */
                            if (item != undefined) {
                                if (item.get((column.dataIndex)) != "") {
                                    temp.setValue(item.get(column.dataIndex));
                                    this.addRuleForm.add(temp);
                                }
                            } else {
                                this.addRuleForm.add(temp);
                            }
                        }
                    } else {
                        if (column.header.match('CGST') || column.header.match('SGST') || column.header.match('CESS')) {
                            var config = {
                                fieldLabel: column.header + '*',
                                name: column.dataIndex,
                                emptyText: column.header,
                                allowDecimals: true,
                                allowBlank: false,
                                width: 200,
                                decimalPrecision: Wtf.account.companyAccountPref.columnPref.gstamountdigitafterdecimal,
                                allowNegative :false,
                                maxValue: 100
                            }
                            if (column.header.match('CESS')) {
                                config.allowBlank = true;
                                config.fieldLabel = column.header;
                            }
                            var temp = new Wtf.form.NumberField(config);
                            /**
                             * In Case of edit if the value for particular term is blank then don't add perticular component in form
                             */
                            if (item != undefined) {
                                if (item.get((column.dataIndex)) != "") {
                                    temp.setValue(item.get(column.dataIndex));
                                    this.localStateTerms.push(temp);
                                    if (column.header.match('CESS')) {
                                       this.isCess = true;
                                    }
                                }
                            } else {
                                this.localStateTerms.push(temp);
                                if (column.header.match('CESS')) {
                                    this.isCess = true;
                                }
                            }
                        }
                        if (column.header.match('IGST') || column.header.match('CESS')) {
                            var config = {
                                fieldLabel: column.header + '*',
                                name: column.dataIndex,
                                emptyText: column.header,
                                allowDecimals: true,
                                allowBlank: false,
                                width: 200,
                                decimalPrecision: Wtf.account.companyAccountPref.columnPref.gstamountdigitafterdecimal,
                                allowNegative :false,
                                maxValue: 100
                            }
                            if (column.header.match('CESS')) {
                                config.allowBlank = true;
                                config.fieldLabel = column.header;
                            }
                            var temp = new Wtf.form.NumberField(config);
                            /**
                             * In Case of edit if the value for perticular term is blank then don't add perticular component in form
                             */
                            if (item != undefined) {
                                if (item.get((column.dataIndex)) != "") {
                                    temp.setValue(item.get(column.dataIndex));
                                    this.otherStateTerms.push(temp);
                                    if (column.header.match('CESS')) {
                                        this.isCess = true;
                                    }
                                }
                            } else {
                                this.otherStateTerms.push(temp);
                                if (column.header.match('CESS')) {
                                    this.isCess = true;
                                }
                            }
                        }
                    }
                }, this);
            }
            this.addRuleWindow.doLayout();
            this.addRuleWindow.show();
        }
    },
    /**
     * 
     *Create CESS Calcualtion fields for INDIA GST 
     */
    createCessFieldSet: function () {
        var Help = WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.field.SelectaCurrencytoprocesstransactionofrequiredcurrency"));
        var CESSCalculationTypeCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.gstrr.gstrule.cess.Calculation.Type"),
            name: Wtf.GST_CESS_TYPE,
            store: this.CESSCalculationTypeComboStore,
            extraFields : [],
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            id : Wtf.GST_CESS_TYPE + this.id,
            typeAhead: true,
            allowBlank: true,
            triggerAction: 'all',
            emptyText: WtfGlobal.getLocaleText("acc.gstrr.gstrule.cess.select.Calculation.Type"),
            width: 200,
            listWidth: 350
        });
        var valuationAmount = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.amount"),
            name: Wtf.GST_CESS_VALUATION_AMOUNT,
            emptyText: WtfGlobal.getLocaleText("acc.gstrr.gstrule.cess.cess.amount"),
            allowBlank: true,
            id: Wtf.GST_CESS_VALUATION_AMOUNT + this.id,
            allowDecimals: true,
            width: 200,
            maxLength: 8,
            allowNegative :false
        });
        CESSCalculationTypeCombo.on('select', function () {
            var CESSTypeCmp = Wtf.getCmp(Wtf.GST_CESS_TYPE + this.id);
            var valuationAmountCmp = Wtf.getCmp(Wtf.GST_CESS_VALUATION_AMOUNT + this.id);
            if (valuationAmountCmp) {
                valuationAmountCmp.setValue(0);
            }
            this.enableDisableComponent(CESSTypeCmp,valuationAmountCmp);
        }, this);
        var CESSCalculationFieldSet = new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.gstrr.gstrule.cess.cess.fieldset"),
            autoHeight: true,
            disabledClass: "newtripcmbss",
            autoWidth: true,
            name : Wtf.CESSCalculationFieldSet,
            collapsed: false,
            autoDestroy :true,
            items: [CESSCalculationTypeCombo,valuationAmount]
        });
        var sm = this.grid1.getSelectionModel().getSelections();
        if (sm.length >= 1 && this.addRuleWindow.isEdit) {
            var item = this.grid1.getSelectionModel().getSelected();
            CESSCalculationTypeCombo.setValue(item.get(Wtf.GST_CESS_TYPE));
            if (item.get(Wtf.GST_CESS_VALUATION_AMOUNT) != undefined && item.get(Wtf.GST_CESS_VALUATION_AMOUNT) != '') {
                valuationAmount.setValue(item.get(Wtf.GST_CESS_VALUATION_AMOUNT));
            }
             this.enableDisableComponent(CESSCalculationTypeCombo,valuationAmount);
        }
        return CESSCalculationFieldSet;
    },
    enableDisableComponent: function (CESSTypeCmp, valuationAmountCmp) {
        valuationAmountCmp.setDisabled(true);
        if (CESSTypeCmp && CESSTypeCmp.getValue() != '') {
            if((CESSTypeCmp.getValue() == Wtf.CESSTYPE.HIGHER_VALUE_OR_CESSPERCENTAGES || CESSTypeCmp.getValue() == Wtf.CESSTYPE.VALUE_AND_CESSPERCENTAGES || CESSTypeCmp.getValue() == Wtf.CESSTYPE.VALUE)){
                valuationAmountCmp.setDisabled(false);
            }
        }
    },
    /**
     * Validate CESS Calculation type details for INDIA GST 
     * @param {type} isFormValid
     * @returns {Boolean}
     */
    validateCESSCalculationFields: function (isFormValid) {
        var CESSAmount = "";
        Wtf.each(this.addRuleForm.items.items, function (item) {
            if (item.fieldLabel!=undefined && item.fieldLabel.match('CESS')) {
                CESSAmount = item.getValue();
            }
        }, this);
        var CESSTypeCmp = Wtf.getCmp(Wtf.GST_CESS_TYPE + this.id);
        var valuationAmountCmp = Wtf.getCmp(Wtf.GST_CESS_VALUATION_AMOUNT + this.id);
        var msg = "";
        if (CESSAmount == '') {
            if (CESSTypeCmp && CESSTypeCmp.getValue() != '' || valuationAmountCmp && valuationAmountCmp.getValue()!='') {
                msg = WtfGlobal.getLocaleText({key: "acc.gstrr.gstrule.cesstype.error.txt5", params: []});
                valuationAmountCmp.setValue(0);
                CESSTypeCmp.setValue('');
            }
        } else if (CESSAmount != '') {
            if (CESSTypeCmp && CESSTypeCmp.getValue() != '' && (CESSTypeCmp.getValue() == Wtf.CESSTYPE.HIGHER_VALUE_OR_CESSPERCENTAGES || CESSTypeCmp.getValue() == Wtf.CESSTYPE.VALUE_AND_CESSPERCENTAGES || CESSTypeCmp.getValue() == Wtf.CESSTYPE.VALUE)
                    && valuationAmountCmp && valuationAmountCmp.getValue()=='') {
                msg = WtfGlobal.getLocaleText({
                    key: "acc.gstrr.gstrule.cesstype.error.txt6",
                    params: [(Wtf.CESSTYPE_NAME.HIGHER_VALUE_OR_CESSPERCENTAGES + ", " + Wtf.CESSTYPE_NAME.VALUE_AND_CESSPERCENTAGES + ", " + Wtf.CESSTYPE_NAME.VALUE)]});

                valuationAmountCmp.setValue(0);
            } else if (CESSTypeCmp && CESSTypeCmp.getValue() != '' && !(CESSTypeCmp.getValue() == Wtf.CESSTYPE.HIGHER_VALUE_OR_CESSPERCENTAGES || CESSTypeCmp.getValue() == Wtf.CESSTYPE.VALUE_AND_CESSPERCENTAGES || CESSTypeCmp.getValue() == Wtf.CESSTYPE.VALUE)
                    && valuationAmountCmp && valuationAmountCmp.getValue() != '') {
                msg = WtfGlobal.getLocaleText({
                    key: "acc.gstrr.gstrule.cesstype.error.txt7",
                    params: [(Wtf.CESSTYPE_NAME.NOT_APPLICABLE + ", " + Wtf.CESSTYPE_NAME.PERCENTAGES)]});
                valuationAmountCmp.setValue(0);
            }
        }
        if (msg != "") {
            isFormValid = false;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        }
        return isFormValid;
    },
    assignComboValues: function(o,e){
        /**
         * value variable is value to be assigned.
         * combo is combobox in which value will be assigned.
         */
        var value = o.items[0];
        var combo = o.items[1];
        for (var i = 0; i < e.length; i++) {
            if (e[i].data.name == value) {
                combo.setValue(e[i].data.id);
                break;
            }
        }
        combo.disable();
        if (combo.name.match('shippedLoc') && !this.isUSGST) {
            this.getTermStateMap();
        }
        this.addRuleWindow.doLayout();
    },
    getTermStateMap:function(){
        /**
         * this function stores a map of entities and its local state in an 
         * object.
         */
        var entity = this.addEntityCombo.getValue() || "";
        var state = this.addStateCombo.getValue() || "";
        if (entity != "" && state != "") {
            Wtf.Ajax.requestEx({
                url: "AccEntityGST/isStateMappedwithEntity.do",
                params: {
                    entity: entity,
                    State: state
                }
            }, this, this.addTerms);
        }
    },
    addTerms: function(response){
        WtfGlobal.resetAjaxTimeOut();
        var entity = this.addEntityCombo.getValue() || "";
        var state = this.addStateCombo.getValue() || "";
        if (entity != "" && state != "") {
            var tempArr = [];
            /*
             * For INDIA GST and CESS Term Presnet then need to add CESS Calcualtion fields in add/ Edit rule
             */
            if (WtfGlobal.isIndiaCountryAndGSTApplied() && this.isCess) {
                var CESSCalculationFieldSet = this.createCessFieldSet();
                var localStateIndex = -1, otherStateIndex = -1;
                if (CESSCalculationFieldSet) {
                    for (var i = 0; i < this.otherStateTerms.length; i++) {
                        if (this.otherStateTerms[i] && this.otherStateTerms[i].name == Wtf.CESSCalculationFieldSet) {
                            localStateIndex = i;
                        }
                    }
                    for (var i = 0; i < this.localStateTerms.length; i++) {
                        if (this.otherStateTerms[i] && this.otherStateTerms[i].name == Wtf.CESSCalculationFieldSet) {
                            otherStateIndex = i
                        }
                    }
                    if (otherStateIndex == -1) {
                        this.otherStateTerms.push(CESSCalculationFieldSet);
                    } else {
                        this.otherStateTerms[otherStateIndex] = CESSCalculationFieldSet;
                    }
                    if (localStateIndex == -1) {
                        this.localStateTerms.push(CESSCalculationFieldSet);
                    } else {
                        this.localStateTerms[localStateIndex] = CESSCalculationFieldSet;
                    }
                }
            }
            /**
             * entityStateMap is Map with entity ID as Key and its corresponding Local state ID as Value.
             */
            if (response.data.success1 == true) {
                tempArr = this.localStateTerms;
            } else {
                tempArr = this.otherStateTerms;
            }
            /**
             * Remove Components if Present Earlier.
             */
            while (this.addedComponents.length > 0) {
                var temp = this.addedComponents.pop();
                this.addRuleForm.remove(temp);
            }
            /**
             * Added Appropriate Components (local components or Others Components)
             */
            for (var i = 0; i < tempArr.length; i++) {
                if (tempArr[i].cloneConfig) {
                    var newComp = tempArr[i].cloneConfig();
                    if (typeof newComp.getValue=='function' && tempArr[i].getValue() != "") {
                        newComp.setValue(tempArr[i].getValue());
                    }
                    this.addedComponents.push(newComp);
                    this.addRuleForm.add(newComp);
                }
            }
        }
        this.addRuleWindow.doLayout();
    },
    deleteItem:function(){
        var sDate = this.startDate.getValue();
        var eDate = this.endDate.getValue();
        if (sDate > eDate || (sDate == '' || eDate == '')) {
            WtfComMsgBox(1, 2);
            return;
        }
        var recArr = this.grid1.getSelectionModel().getSelections();
        var idData = "";
        var data = "["
        for (var i = 0; i < recArr.length; i++) {
            var rec = recArr[i];
            idData = "{";
            if (rec.json.id1 != undefined) {
                idData += "\"id1\":\"" + rec.json.id1 + "\",";
            }
            if (rec.json.id2 != undefined) {
                idData += "\"id2\":\"" + rec.json.id2 + "\",";
            }
            if (rec.json.id3 != undefined) {
                idData += "\"id3\":\"" + rec.json.id3 + "\",";
            }
            /**
             * Remove , Appended at last
             */
            idData = idData.substring(0, idData.length - 1) + "},";
            data += idData;
        }
        if (idData.length > 1) {
            data = data.substring(0, data.length - 1) + ']';
        }
        Wtf.Ajax.requestEx({
            url: "AccEntityGST/deleteGSTRuleReportItem.do",
            params: {
                data: data
            }
        }, this, this.genSuccessResponseClose, this.genFailureResponseClose);
    },
    genSuccessResponseClose: function(response){
        WtfGlobal.resetAjaxTimeOut();
        if (this.addRuleWindow != undefined) {
            this.addRuleWindow.close();
        }
        var action = function (btn) {
            if (btn == "ok") {
                if (response.success) {
                    this.GSTRuleReportStore.reload();
                }
            }
        }
        Wtf.MessageBox.show({
            title: this.title,
            msg: response.msg,
            width: 420,
            fn: action,
            scope: this,
            buttons: Wtf.MessageBox.OK,
            animEl: 'mb9',
            icon: (response.success ? Wtf.MessageBox.INFO : Wtf.MessageBox.WARNING)
        });

    },
    genFailureResponseClose: function(response){
        WtfGlobal.resetAjaxTimeOut();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    loadStore: function () {
        var sDate = this.startDate.getValue();
        var eDate = this.endDate.getValue();
        if (sDate > eDate || (sDate == '' || eDate == '')) {
            WtfComMsgBox(1, 2);
            return;
        }
        this.GSTRuleReportStore.load({
            params: {
                start: 0,
                limit: this.pPSetPrice.combo.value, //this.pPSetPrice.ombo.value,
                ss: this.quickPanelSearch.getValue()
            }
        });
    },
    getDates: function (start) {
        var d = new Date();
        var monthDateStr = d.format('M d');
        if (Wtf.account.companyAccountPref.fyfrom) {
            monthDateStr = Wtf.account.companyAccountPref.fyfrom.format('M d');
        }
        var fd = new Date(monthDateStr + ', ' + d.getFullYear() + ' 12:00:00 AM');
        if (d < fd) {
            fd = new Date(monthDateStr + ', ' + (d.getFullYear() - 1) + ' 12:00:00 AM');
        }
        if (start) {
            return fd;
        }
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    handleResetClick: function () {
        this.quickPanelSearch.reset();
        this.loadStore();
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
        
        Wtf.each(this.GSTRuleReportStore.reader.jsonData.columns, function(column) {
            if (column.renderer) {
                column.renderer = eval('(' + column.renderer + ')');
            }
            else {
                column.renderer = WtfGlobal.deletedRenderer;
            }
            columns.push(column);
        });
        this.grid1.getColumnModel().setConfig(columns);
        this.grid1.getView().refresh();

        
       if (this.GSTRuleReportStore.getCount() < 1) {
            this.grid1.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid1.getView().refresh();
        }else{            
            if (columns.length > 1) {
                this.GSTRuleReportStore.sortInfo = {
                    field: 'shippedLoc1',
                    direction: 'ASC'
                };
                this.grid1.getStore().groupBy('percentage');
                this.grid1.getView().refresh();
            }
        }
    }
});

