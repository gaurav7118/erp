/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

/*
 * # Note:
 * # For Budgeting Based On:
 *  1. Wtf.account.companyAccountPref.budgetType = 0 -> Department
 *  2. Wtf.account.companyAccountPref.budgetType = 1 -> Department and Product
 *  3. Wtf.account.companyAccountPref.budgetType = 2 -> Department and Product Category
 *  
 *  # For Budgeting Frequency Type:
 *  1. Wtf.account.companyAccountPref.budgetFreqType == 0 -> Monthly
 *  2. Wtf.account.companyAccountPref.budgetFreqType == 1 -> Bi-Monthly
 *  3. Wtf.account.companyAccountPref.budgetFreqType == 2 -> Quarterly
 *  4. Wtf.account.companyAccountPref.budgetFreqType == 3 -> Half Yearly
 *  5. Wtf.account.companyAccountPref.budgetFreqType == 4 -> Yearly
 */

Wtf.account.Budgeting = function(config) {
    
//================================================== For Applying Config to the Window =========================================================
    
    Wtf.apply(this, {
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.msgbox.ok"),  // 'OK',
            scope: this,
            handler: this.closeForm.createDelegate(this)
        }]
    }, config);
    
    Wtf.account.Budgeting.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.Budgeting, Wtf.Window, {
    
//======================================================= For Rendering Window =================================================================

    onRender: function(config) {
        Wtf.account.Budgeting.superclass.onRender.call(this, config);
        
        this.createFields();
        this.createGrid();
        this.createForm();
        
        this.add(this.centerPanel = new Wtf.Panel({
            region: 'center',
            border: false,
            autoScroll: true,
            bodyStyle: 'background:#f1f1f1; font-size:10px; padding:10px',
            baseCls: 'bckgroundcolor',
            layout: 'fit',
            items: [this.budgetingForm]
        }));
    },
    
    
//======================================================= For Creating Form Fields ==============================================================

    createFields: function() {
        this.dimensionListRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
        
        this.dimensionListStore = new Wtf.data.Store({
            url: "ACCAccountCMN/getDimensionsForCombo.do",
            baseParams: {
                moduleid: Wtf.Acc_Purchase_Requisition_ModuleId
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
        this.dimensionList.on('select', this.loadDimensionValueList, this);
        
        this.dimensionValueListRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
        
        this.dimensionValueListStore = new Wtf.data.Store({
            url: "ACCAccountCMN/getDimensionValuesForCombo.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.dimensionValueListRec)
        });
        
        this.dimensionValueList = new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.budgeting.dimensionValues"), // "Dimension Values",
            emptyText: WtfGlobal.getLocaleText("acc.field.pleaseSelectDimensionValues"), // "Please select dimension values.",
            forceSelection: true
        },{
            width: 300,
            store: this.dimensionValueListStore,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            selectOnFocus: true,
            triggerAction: 'all',
            typeAhead: true,
            scope: this
        }));

        this.productRec = Wtf.data.Record.create ([
            {name:'productid'},
            {name:'pid'},
            {name:'type'},
            {name:'productname'}
        ]);

        this.productStore = new Wtf.data.Store({
            url: "ACCProductCMN/getProductsForCombo.do",
            baseParams: {
                excludeParent:true,
                module_name : "BUDGETING"
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.productRec)
        });
        
        if (Wtf.account.companyAccountPref.budgetType == 1) {
            this.productStore.load();
        }

        this.productList = new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.contract.product.name"), // "Product Name",
            emptyText: WtfGlobal.getLocaleText("acc.prod.comboEmptytext"), // "Please Select Product",
            forceSelection: true,
            hideLabel: Wtf.account.companyAccountPref.budgetType != 1,
            hidden: Wtf.account.companyAccountPref.budgetType != 1
        },{
            name: 'productid',
            width: 300,
            store: this.productStore,
            valueField: 'productid',
            displayField: 'productname',
            mode: 'local',
            selectOnFocus: true,
            triggerAction: 'all',
            typeAhead: true,
            scope: this
        }));

//        this.productList.on('select', this.loadGridData, this);
//        this.productList.on('blur', this.loadGridData, this);
//        this.productList.on('clearval', this.loadGridData, this);
        
        this.productCategory = new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.cust.Productcategory"),  // "Product Category",
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseselectaproductcategory"), // "Please select a product category",
            forceSelection: true,
            hideLabel: Wtf.account.companyAccountPref.budgetType != 2,
            hidden: Wtf.account.companyAccountPref.budgetType != 2
        },{
            name: 'productCategory',
            width: 300,
            store: Wtf.ProductCategoryStore,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            selectOnFocus: true,
            triggerAction: 'all',
            typeAhead: true,
            scope: this
        }));
        
        if (Wtf.account.companyAccountPref.budgetType == 2) {
            Wtf.ProductCategoryStore.load();
        }

        var data = this.getBookBeginningYear(true);

        this.yearStore = new Wtf.data.SimpleStore({
            fields: [{name:'id', type:'int'}, 'yearid'],
            data: data
        });

        this.year = new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.accPref.year"),  // 'Year',
            emptyText: WtfGlobal.getLocaleText("acc.field.pleaseSelectYear"), // "Please select Year",
            forceSelection: true,
            hideLabel: Wtf.account.companyAccountPref.budgetFreqType != 4,
            hidden: Wtf.account.companyAccountPref.budgetFreqType != 4
        },{
            name: 'year',
            width: 300,
            store: this.yearStore,
            valueField: 'yearid',
            displayField: 'yearid',
            mode: 'local',
            selectOnFocus: true,
            triggerAction: 'all',
            typeAhead: true,
            scope: this
        }));
        
        this.fetchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            scope: this,
            handler: this.loadGridData
        });

        this.year.on('select', this.changeYearColumnConfig, this);
        this.year.on('blur', this.changeYearColumnConfig, this);
        this.year.on('clearval', this.changeYearColumnConfig, this);
    },
    
//======================================================= For Getting Years For Year Store ==============================================================    
    
    getBookBeginningYear: function(isfirst) {
        var ffyear;
        if (isfirst) {
            var cfYear = new Date(Wtf.account.companyAccountPref.fyfrom);
            ffyear = new Date(Wtf.account.companyAccountPref.firstfyfrom);
            ffyear = new Date(ffyear.getFullYear(), cfYear.getMonth(), cfYear.getDate()).clearTime();
        } else {
            var fyear = new Date(Wtf.account.companyAccountPref.firstfyfrom).getFullYear();
            ffyear = new Date( fyear,this.fmonth.getValue(),this.fdays.getValue()).clearTime();
        }

        var data = [];
        var newrec;
        if (ffyear == null || ffyear == "NaN") {
            ffyear = new Date(Wtf.account.companyAccountPref.fyfrom);
        }
        var year = ffyear.getFullYear();
        var temp = new Date();
        var year1 = temp.getFullYear();
        data.push([0,year1]);
        var i = 1;
        while (year1 >= year) {
            data.push([i,--year1]);
            i++;
        }
        
        if (!(ffyear.getMonth() == 0 && ffyear.getDate() == 1)) {
            data.push([1,year+1]);
            newrec = new Wtf.data.Record({
                id: 1,
                yearid: year+1
            });
        }
        return data;
    },
    
//========================================================== For Creating Grid ====================================================================

    createGrid: function() {
        this.budgetingGridRec = new Wtf.data.Record.create([
            {name: 'productUUID'},
            {name: 'productName'},
            {name: 'dimensionValueUUID'},
            {name: 'dimensionValue'},
            {name: 'productCategoryUUID'},
            {name: 'productCategory'},
            {name: '0'},
            {name: '1'},
            {name: '2'},
            {name: '3'},
            {name: '4'},
            {name: '5'},
            {name: '6'},
            {name: '7'},
            {name: '8'},
            {name: '9'},
            {name: '10'},
            {name: '11'}
        ]);
        
        this.budgetingGridStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            }, this.budgetingGridRec),
            url: "ACCPurchaseOrder/getBudgeting.do"
        });
        
        this.createBudgetingGridCM();
        
        this.budgetingGrid = new Wtf.grid.EditorGridPanel({
            store: this.budgetingGridStore,
            cm: this.budgetingGridCM,
            clicksToEdit: 1,
            autoScroll: true,
            height: 200,
            width: 1030,
            border: false,
            loadMask: true,
            cls: 'vline-on',
            viewConfig: {
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        
        this.budgetingGrid.on('render', function() {
            this.budgetingGrid.getView().refresh();
//            this.budgetingGrid.view.refresh.defer(1, this.budgetingGrid.view); /* alternate way to refresh the empty text of grid without load the store */
        },this);
        
        this.budgetingGrid.on('afteredit', this.saveRecord, this);
    },
    
//======================================================= For Creating Grid Column Model  ============================================================
    
    createBudgetingGridCM: function() {
        this.columnArr = [];
        
        if (Wtf.account.companyAccountPref.budgetType == 0 || Wtf.account.companyAccountPref.budgetType == 1 || Wtf.account.companyAccountPref.budgetType == 2) {
            this.columnArr.push({
                header: WtfGlobal.getLocaleText("acc.budgeting.dimensionValues"),  // "Dimension Values",
                dataIndex: 'dimensionValue',
                align: 'left',
                width: 220,
                renderer: WtfGlobal.deletedRenderer
            });
        }
        
        if (Wtf.account.companyAccountPref.budgetType == 1) {
            this.columnArr.push({
                header: WtfGlobal.getLocaleText("acc.contract.product.name"),  // "Product Name",
                dataIndex: 'productName',
                align: 'left',
                width: 220,
                renderer: WtfGlobal.deletedRenderer
            });
        }
        
        if (Wtf.account.companyAccountPref.budgetType == 2) {
            this.columnArr.push({
                header: WtfGlobal.getLocaleText("acc.cust.Productcategory"),  // "Product Category",
                dataIndex: 'productCategory',
                align: 'left',
                width: 220,
                renderer: WtfGlobal.deletedRenderer
            });
        }
        
        this.createBudgetingGridGenericFrequencyCM();
        
        this.budgetingGridCM = new Wtf.grid.ColumnModel(this.columnArr);
        this.initialColumnCnt = this.budgetingGridCM.getColumnCount();
    },
    
    createBudgetingGridGenericFrequencyCM: function() {
        if (Wtf.account.companyAccountPref.budgetFreqType == 0) {
            this.columnArr.push({
                header: WtfGlobal.getLocaleText("acc.field.January"),  // "January",
                dataIndex: '0',
                align: 'right',
                width: 75,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            },{
                header: WtfGlobal.getLocaleText("acc.field.February"),  // "February",
                dataIndex: '1',
                align: 'right',
                width: 75,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            },{
                header: WtfGlobal.getLocaleText("acc.field.March"),  // "March",
                dataIndex: '2',
                align: 'right',
                width: 75,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            },{
                header: WtfGlobal.getLocaleText("acc.field.April"),  // "April",
                dataIndex: '3',
                align: 'right',
                width: 75,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            },{
                header: WtfGlobal.getLocaleText("acc.field.May"),  // "May",
                dataIndex: '4',
                align: 'right',
                width: 75,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            },{
                header: WtfGlobal.getLocaleText("acc.field.June"),  // "June",
                dataIndex: '5',
                align: 'right',
                width: 75,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            },{
                header: WtfGlobal.getLocaleText("acc.field.July"),  // "July",
                dataIndex: '6',
                align: 'right',
                width: 75,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            },{
                header: WtfGlobal.getLocaleText("acc.field.August"),  // "August",
                dataIndex: '7',
                align: 'right',
                width: 75,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            },{
                header: WtfGlobal.getLocaleText("acc.field.September"),  // "September",
                dataIndex: '8',
                align: 'right',
                width: 80,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            },{
                header: WtfGlobal.getLocaleText("acc.field.October"),  // "October",
                dataIndex: '9',
                align: 'right',
                width: 75,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            },{
                header: WtfGlobal.getLocaleText("acc.field.November"),  // "November",
                dataIndex: '10',
                align: 'right',
                width: 75,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            },{
                header: WtfGlobal.getLocaleText("acc.field.December"),  // "December",
                dataIndex: '11',
                align: 'right',
                width: 75,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            });
        } else if (Wtf.account.companyAccountPref.budgetFreqType == 1) {
            this.columnArr.push({
                header: WtfGlobal.getLocaleText("acc.field.January") + "-" + WtfGlobal.getLocaleText("acc.field.February"),  // "January" + "-" + "February",
                dataIndex: '0',
                align: 'right',
                width: 150,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            },{
                header: WtfGlobal.getLocaleText("acc.field.March") + "-" + WtfGlobal.getLocaleText("acc.field.April"),  // "March" + "-" + "April",
                dataIndex: '1',
                align: 'right',
                width: 150,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            },{
                header: WtfGlobal.getLocaleText("acc.field.May") + "-" + WtfGlobal.getLocaleText("acc.field.June"),  // "May" + "-" + "June",
                dataIndex: '2',
                align: 'right',
                width: 150,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            },{
                header: WtfGlobal.getLocaleText("acc.field.July") + "-" + WtfGlobal.getLocaleText("acc.field.August"),  // "July" + "-" + "August",
                dataIndex: '3',
                align: 'right',
                width: 150,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            },{
                header: WtfGlobal.getLocaleText("acc.field.September") + "-" + WtfGlobal.getLocaleText("acc.field.October"),  // "September" + "-" + "October",
                dataIndex: '4',
                align: 'right',
                width: 150,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            },{
                header: WtfGlobal.getLocaleText("acc.field.November") + "-" + WtfGlobal.getLocaleText("acc.field.December"),  // "November" + "-" + "December",
                dataIndex: '5',
                align: 'right',
                width: 150,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            });
        } else if (Wtf.account.companyAccountPref.budgetFreqType == 2) {
            this.columnArr.push({
                header: WtfGlobal.getLocaleText("acc.field.January") + "-" + WtfGlobal.getLocaleText("acc.field.March"),  // "January" + "-" + "March",
                dataIndex: '0',
                align: 'right',
                width: 150,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            },{
                header: WtfGlobal.getLocaleText("acc.field.April") + "-" + WtfGlobal.getLocaleText("acc.field.June"),  // "April" + "-" + "June",
                dataIndex: '1',
                align: 'right',
                width: 150,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            },{
                header: WtfGlobal.getLocaleText("acc.field.July") + "-" + WtfGlobal.getLocaleText("acc.field.September"),  // "July" + "-" + "September",
                dataIndex: '2',
                align: 'right',
                width: 150,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            },{
                header: WtfGlobal.getLocaleText("acc.field.October") + "-" + WtfGlobal.getLocaleText("acc.field.December"),  // "October" + "-" + "December",
                dataIndex: '3',
                align: 'right',
                width: 150,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            });
        } else if (Wtf.account.companyAccountPref.budgetFreqType == 3) {
            this.columnArr.push({
                header: WtfGlobal.getLocaleText("acc.field.January") + "-" + WtfGlobal.getLocaleText("acc.field.June"),  // "January" + "-" + "June",
                dataIndex: '0',
                align: 'right',
                width: 150,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            },{
                header: WtfGlobal.getLocaleText("acc.field.July") + "-" + WtfGlobal.getLocaleText("acc.field.December"),  // "July" + "-" + "December",
                dataIndex: '1',
                align: 'right',
                width: 150,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            });
        }
    },
    
//============================================ For Changing Grid Column Model On Year Change  ======================================================

    changeYearColumnConfig: function() {
        var config = this.budgetingGridCM.config.slice(0, this.initialColumnCnt);
        var yearsArray = this.year.getValue().split(",");
        
        if (yearsArray != undefined && this.year.getValue() != "") {
            for (var i=0; i<yearsArray.length; i++) {
                var index = this.yearStore.find('yearid', yearsArray[i]);
                var yearRec = this.yearStore.getAt(index);
                
                // for adding column in column cofig
                config.push({
                    header: yearRec.data.yearid, // Year,
                    dataIndex: yearRec.data.yearid,
                    align: 'right',
                    width: 75,
                    editor: new Wtf.form.NumberField({
                        allowNegative: false,
                        maxLength: 14,
                        decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                    })
                });
                
                // for adding field in records
                var fieldname = yearRec.data.yearid;
                var newField = new Wtf.data.Field({
                    name: fieldname,
                    defaultValue: 0
                });
                this.budgetingGridStore.fields.items.push(newField);
                this.budgetingGridStore.fields.map[fieldname] = newField;
                this.budgetingGridStore.fields.keys.push(fieldname);
            }
        }
        
        // for updating config
        this.budgetingGridStore.reader = new Wtf.data.KwlJsonReader(this.budgetingGridStore.reader.meta, this.budgetingGridStore.fields.items);
        this.budgetingGrid.getColumnModel().setConfig(config);
        var newcm = this.budgetingGrid.getColumnModel();
        this.budgetingGrid.reconfigure(this.budgetingGridStore, newcm);
        this.budgetingGrid.getView().refresh(true);
    },
    
    
//======================================================= For Creating Form ========================================================================
    
    createForm: function() {
        this.budgetingForm = new Wtf.form.FormPanel({
            waitMsgTarget: true,
            border: false,
            labelWidth: 200,
            items: [
                this.dimensionList,
                this.dimensionValueList,
                this.productList,
                this.productCategory,
                this.year,
                this.fetchBtn,
                {
                    xtype: 'fieldset',
                    width: (Wtf.isIE)? '91%' : '85%',
                    height: '75%',
                    style: 'margin-top: 10px;',
                    title: WtfGlobal.getLocaleText("acc.field.setBudgeting"), // "Set Budgeting",
                    border : false,
                    items: [this.budgetingGrid]
                }
            ]
        });
    },
    
//======================================================= For Loading Grid Data ======================================================================
    
    loadGridData: function() {
        if (Wtf.account.companyAccountPref.budgetFreqType == 4 && this.year.getValue() == "") {
            return;
        } else {
            if (Wtf.account.companyAccountPref.budgetType == 0) {
                if (this.dimensionValueList.getValue() != "") {
                    this.budgetingGridStore.load({
                        params: {
                            dimensionValue: this.dimensionValueList.getValue(),
                            frequencyType: Wtf.account.companyAccountPref.budgetFreqType
                        }
                    });
                } else {
                    this.budgetingGridStore.removeAll();
                }
            } else if (Wtf.account.companyAccountPref.budgetType == 1) {
                if (this.dimensionValueList.getValue() != "" && this.productList.getValue() != "") {
                    this.budgetingGridStore.load({
                        params: {
                            dimensionValue: this.dimensionValueList.getValue(),
                            product: this.productList.getValue(),
                            frequencyType: Wtf.account.companyAccountPref.budgetFreqType
                        }
                    });
                } else {
                    this.budgetingGridStore.removeAll();
                }
            } else if (Wtf.account.companyAccountPref.budgetType == 2) {
                if (this.dimensionValueList.getValue() != "" && this.productCategory.getValue() != "") {
                    this.budgetingGridStore.load({
                        params: {
                            dimensionValue: this.dimensionValueList.getValue(),
                            productCategory: this.productCategory.getValue(),
                            frequencyType: Wtf.account.companyAccountPref.budgetFreqType
                        }
                    });
                } else {
                    this.budgetingGridStore.removeAll();
                }
            }
        }
        
    },
    
    loadDimensionValueList: function(combo, record, index) {
        this.dimensionValueList.reset();
        this.dimensionValueListStore.load({
            params: {
                groupid: this.dimensionList.getValue()
            }
        });
    },
    
//======================================================= For Saving Data of Form =====================================================================
    
    saveRecord: function(obj) {
        var params;
        if (Wtf.account.companyAccountPref.budgetType == 0) {
            if (Wtf.account.companyAccountPref.budgetFreqType == 4) {
                params = {
                    dimensionValue: obj.record.data.dimensionValueUUID,
                    frequencyType: 4,
                    year: obj.field,
                    amount: obj.value
                }
            } else {
                params = {
                    dimensionValue: obj.record.data.dimensionValueUUID,
                    frequencyType: Wtf.account.companyAccountPref.budgetFreqType,
                    frequencyColumn: obj.field,
                    amount: obj.value
                }
            }
        } else  if (Wtf.account.companyAccountPref.budgetType == 1) {
            if (Wtf.account.companyAccountPref.budgetFreqType == 4) {
                params = {
                    dimensionValue: obj.record.data.dimensionValueUUID,
                    product: obj.record.data.productUUID,
                    frequencyType: 4,
                    year: obj.field,
                    amount: obj.value
                }
            } else {
                params = {
                    dimensionValue: obj.record.data.dimensionValueUUID,
                    product: obj.record.data.productUUID,
                    frequencyType: Wtf.account.companyAccountPref.budgetFreqType,
                    frequencyColumn: obj.field,
                    amount: obj.value
                }
            }
        } else if (Wtf.account.companyAccountPref.budgetType == 2) {
            if (Wtf.account.companyAccountPref.budgetFreqType == 4) {
                params = {
                    dimensionValue: obj.record.data.dimensionValueUUID,
                    productCategory: obj.record.data.productCategoryUUID,
                    frequencyType: 4,
                    year: obj.field,
                    amount: obj.value
                }
            } else {
                params = {
                    dimensionValue: obj.record.data.dimensionValueUUID,
                    productCategory: obj.record.data.productCategoryUUID,
                    frequencyType: Wtf.account.companyAccountPref.budgetFreqType,
                    frequencyColumn: obj.field,
                    amount: obj.value
                }
            }
        }
        
        var URL = "ACCPurchaseOrder/saveBudgeting.do";
        
        Wtf.Ajax.requestEx({
            url: URL,
            params: params
        }, this, this.genSuccessResponse, this.genFailureResponse);
    },
    
//==================================================== For Handle Success Response of Ajax ===========================================================
    
    genSuccessResponse: function(response) {
        if (response.success) {
            this.budgetingGridStore.reload();
        } else {
            this.genFailureResponse();
        }
    },
    
//==================================================== For Handle Failure Response of Ajax ===========================================================    
    
    genFailureResponse: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.field.FailedtomakeconnectionwithWebServer");
        if (response.msg) {
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

//========================================================== For Closing Form ========================================================================

    closeForm: function() {
        this.close();
    }
});