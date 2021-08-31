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

/********************************************************************************/
/******************* Adjust Wastage Quantity Window *****************************/
/********************************************************************************/

Wtf.account.wastageQuanity = function(config) {
    this.title = config.title;
    this.productName = config.productName;
    this.actualQuantity = (config.actualQuantity == undefined) ? 0 : config.actualQuantity;
    this.grid = config.grid,
    this.rowindex = config.rowindex,
    this.record = config.record;
    
    this.submitBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.submit"), // 'Submit',
         minWidth: 70
    });
    this.submitBtn.on('click', this.handleSubmit, this);
    
    this.closeBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.close"), // 'Close',
        minWidth: 70
    });
    this.closeBtn.on('click', this.handleClose, this);

    Wtf.apply(this, {
        title: WtfGlobal.getLocaleText("acc.field.adjustWastageQuantity"), // "Adjust Wastage Quantity",
        buttons: [this.submitBtn, this.closeBtn]
    }, config);

    Wtf.account.wastageQuanity.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.wastageQuanity, Wtf.Window, {
    onRender: function(config) {
        this.createForm();
        var msg = "<b>Product</b> : " + this.productName + "<br>     <b>Actual Quantity</b> : " + this.actualQuantity
        var isGrid = false;
        
        this.add({
            region: 'center',
            border: false,
            baseCls: 'bckgroundcolor',
            items: this.form
        });

        this.add({
            region: 'north',
            height: 85,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(this.title, msg, "../../images/accounting_image/price-list.gif", isGrid)
        }, this.centerPanel = new Wtf.Panel({
            region: 'center',
            border: false,
            autoScroll: true,
            bodyStyle: 'background:#f1f1f1; font-size:10px; padding:10px',
            baseCls: 'bckgroundcolor',
            items: [this.form]
        }));
        Wtf.account.wastageQuanity.superclass.onRender.call(this, config);
    },
    
    createForm: function() {
        this.wastageInventoryQuantity = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.inventory"), // 'Inventory',
            name: 'wastageInventoryQuantity',
            id: "wastageInventoryQuantity"+this.heplmodeid+this.id,
            allowNegative: false,
            defaultValue: 0,
            allowBlank: false,
            maxLength: 10,
            width: 220,
            decimalPrecision: Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            readOnly: true
        });
        
        this.wastageQuantityTypeStore = new Wtf.data.SimpleStore({
            fields: [{name:'typeid', type:'int'}, 'name'],
            data :[[1,'Percentage'], [0,'Flat']]
        });
        this.wastageQuantityType = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.wastageType"), // 'Wastage Quantity Type',
            store: this.wastageQuantityTypeStore,
            width: 220,
            name: 'wastageQuantityType',
            displayField: 'name',
            valueField: 'typeid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus: true,
            allowBlank: false,
            listeners: {
                'select': {
                    fn: this.handleWastageQuantityTypeSelect,
                    scope: this
                }
            }
        });
        
        this.wastageQuantity = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.wastageQuantity"), // 'Wastage Quantity',
            name: 'wastageQuantity',
            id: "wastageQuantity"+this.heplmodeid+this.id,
            allowNegative: false,
            defaultValue: 0,
            allowBlank: false,
            maxLength: 10,
            width: 220,
            decimalPrecision: Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            listeners: {
                'change': {
                    fn: this.handleWastageQuantityChange,
                    scope: this
                }
            }
        });
        
        this.inventoryQuantityFieldSet = new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.product.inventoryQuantity"), // "Inventory Quantity",
            autoHeight: true,
            autoWidth: true,
            width: 380,
            style: 'margin-right:30px',
            items: [this.wastageInventoryQuantity, this.wastageQuantityType, this.wastageQuantity]
        });
        
        this.form = new Wtf.form.FormPanel({
            height: 'auto',
            border: false,
            items: [{
                layout: 'form',
                bodyStyle: "background: transparent; padding: 10px;",
                border: false,
                labelWidth: 150,
                items: [this.inventoryQuantityFieldSet]
            }]
        });
        
        var wastageInventoryQty = (this.record.data.wastageInventoryQuantity=="" || this.record.data.wastageInventoryQuantity==undefined) ? 0 : this.record.data.wastageInventoryQuantity;
        var wastageQtyType = (this.record.data.wastageQuantityType=="" || this.record.data.wastageQuantityType==undefined) ? 0 : this.record.data.wastageQuantityType;
        var wastageQty = (this.record.data.wastageQuantity=="" || this.record.data.wastageQuantity==undefined) ? 0 : this.record.data.wastageQuantity;
        
        this.wastageInventoryQuantity.setValue(wastageInventoryQty);
        this.wastageQuantityType.setValue(wastageQtyType);
        this.wastageQuantity.setValue(wastageQty);
    },
    
    handleSubmit: function(a, b) {
        var valid = this.form.getForm().isValid();
        if (valid == false || !this.wastageQuantity.isValid()) {
            WtfComMsgBox(2, 2);
            return;
        }
        
        var wastageInventoryQuantity = this.wastageInventoryQuantity.getValue();
        var wastageQuantity = this.wastageQuantity.getValue();
        var wastageQuantityType = this.wastageQuantityType.getValue();
        var store = this.grid.getStore();
        var record = store.getAt(this.rowindex);
        record.set("wastageInventoryQuantity", wastageInventoryQuantity);
        record.set("wastageQuantityType", wastageQuantityType);
        record.set("wastageQuantity", wastageQuantity);
        
        this.close();
    },
    
    handleClose: function() {
        this.close();
    },
    
    handleWastageQuantityChange: function(comp, newValue, oldValue) {
        if (this.wastageQuantityType.getValue() == 1 && this.wastageQuantity.getValue() > 100) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invsetup.alert.wtg.quant.greater.than.100")], 2);
            this.wastageQuantity.setValue(oldValue);
            return;
        } else if (this.wastageQuantityType.getValue() == 0 && this.wastageQuantity.getValue() > this.wastageInventoryQuantity.getValue()) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invsetup.alert.wtg.quant.greater.than.inv")], 2);
            this.wastageQuantity.setValue(oldValue);
            return;
        }
    },
    
    handleWastageQuantityTypeSelect: function() {
        this.wastageQuantity.setValue(0);
        return;
    }
});


/******************************************************************************************************/
/*********************** Wastage Quantity Window in DO form to calculate wastage **********************/
/******************************************************************************************************/

Wtf.account.calculateWastageAtDO = function(config) {
    this.record = config.record;
    this.readOnly = config.readOnly;
    this.isWastageApplicable = config.isWastageApplicable; // this flag used in case of filling wastage details for Inventory type product
    
    this.submitBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.submit"), // 'Submit',
        hidden: this.readOnly,
        minWidth: 70
    });
    this.submitBtn.on('click', this.handleSubmit, this);
    
    this.closeBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.close"), // 'Close',
        minWidth: 70
    });
    this.closeBtn.on('click', this.handleClose, this);

    Wtf.apply(this, {
        title: WtfGlobal.getLocaleText("acc.field.adjustWastageQuantity"), // "Adjust Wastage Quantity",
        buttons: [this.submitBtn, this.closeBtn]
    }, config);
  
    Wtf.account.calculateWastageAtDO.superclass.constructor.call(this, config);
    
    this.addEvents({
        'submit': true
    });
}

Wtf.extend(Wtf.account.calculateWastageAtDO, Wtf.Window, {
    onRender: function(config) {
        Wtf.account.calculateWastageAtDO.superclass.onRender.call(this, config);
        this.createEditor();
        this.createGrid();
        
        this.add(this.grid);
    },
    
    createEditor: function() {
        this.wastageQuantityTypeStore = new Wtf.data.SimpleStore({
            fields: [{name:'typeid', type:'int'}, 'name'],
            data :[[1,'Percentage'], [0,'Flat']]
        });
        this.wastageQuantityType = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.wastageType"), // 'Wastage Quantity Type',
            store: this.wastageQuantityTypeStore,
            width: 220,
            name: 'wastageQuantityType',
            displayField: 'name',
            valueField: 'typeid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus: true,
            allowBlank: false
        });
        
        this.wastageQuantity = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.wastageQuantity"), // 'Wastage Quantity',
            name: 'wastageQuantity',
            allowNegative: false,
            defaultValue: 0,
            allowBlank: false,
            maxLength: 10,
            width: 220,
            decimalPrecision: Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
        });
    },
    
    createGrid: function() {
        this.gridRec = Wtf.data.Record.create ([
            {name: 'id'},
            {name: 'productid'},
            {name: 'productname'},
            {name: 'desc'},
            {name: 'producttype'},
            {name: 'type'},
            {name: 'quantity'},
            {name: 'actualquantity'},
            {name: 'percentage', defValue: 100},
            {name: 'wastageInventoryQuantity', defValue: 0},
            {name: 'wastageQuantityType', defValue: 0},
            {name: 'wastageQuantity', defValue: 0},
            {name: 'isWastageApplicable', type: 'boolean'}
        ]);
        
        this.gridStore = new Wtf.data.Store({
            url: "ACCProduct/getAssemblyItems.do",
            baseParams: {
                mode: 25,
                productid: this.record.data.productid
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.gridRec)
        });
        
        if (this.record.data.wastageDetails != undefined && this.record.data.wastageDetails != "") {
            this.loadSavedWastageDetails();
        } else if (this.record.data.type == "Inventory Part") {
            this.loadInventoryPartWastageDetails();
        } else {
            this.gridStore.load();
        }
        
        this.gridStore.on("load", function() {
            if (this.gridStore.getCount() == 0) {
                this.itemsgrid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.itemsgrid.getView().refresh();
            }
            this.updateSubtotal();
        },this);
        this.createGridCM();
        
        this.grid = new Wtf.grid.EditorGridPanel({
            store: this.gridStore,
            cm: this.gridCM,
            clicksToEdit: 1,
            border: false,
            loadMask: true,
            height: 250,
            viewConfig: {
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        
        this.grid.on('rowclick', this.handleRowClick, this);
        this.grid.on('validateedit',this.checkRow, this);
        this.grid.on('afteredit',this.updateRow, this);
        
        this.grid.on('render', function() {
            this.grid.getView().refresh();
        },this);
    },
    
    createGridCM: function() {
        this.columnArr = [];
        
        this.columnArr.push(new Wtf.grid.RowNumberer(),{
            header: WtfGlobal.getLocaleText("acc.rem.prodName"),  // "Product Name",
            dataIndex: 'productname',
            align: 'left',
            width: 110
        },{
            header: WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"),  // "Product Description",
            dataIndex: 'desc',
            align: 'left',
            width: 120
        },{
            header: WtfGlobal.getLocaleText("acc.product.gridQtyneeded"), // "Quantity Needed",
            dataIndex: 'quantity',
            align: 'right',
            width: 110
        },{
            header: WtfGlobal.getLocaleText("acc.field.Percentage"), // "Percentage",
            dataIndex: 'percentage',
            align: 'right',
            width: 110,
            renderer: function(v,m,rec) {
                v = v + "%";
                return '<div class="currency">' + v + '</div>';
            }
        },{
            header: WtfGlobal.getLocaleText("acc.field.ActualQuantity"), // "Actual Quantity",
            dataIndex: 'actualquantity',
            align: 'right',
            width: 110
        },{
            header: WtfGlobal.getLocaleText("acc.field.wastageType"), // "Wastage Quantity Type",
            dataIndex: 'wastageQuantityType',
            align: 'left',
            width: 140,
            renderer: Wtf.comboBoxRenderer(this.wastageQuantityType),
            editor: this.readOnly ? "" : this.wastageQuantityType
        },{
            header: WtfGlobal.getLocaleText("acc.field.wastageQuantity"), // "Wastage Quantity",
            dataIndex: 'wastageQuantity',
            align: 'right',
            width: 110,
            editor: this.readOnly ? "" : this.wastageQuantity
        },{
            header: WtfGlobal.getLocaleText("acc.product.gridAction"), // "Action",
            align: 'center',
            width: 110,
            hidden: this.readOnly,
            renderer: this.deleteRenderer.createDelegate(this)    
        });
        
        this.gridCM = new Wtf.grid.ColumnModel(this.columnArr);
    },
    
    deleteRenderer: function(v,m,rec) {
        return "<div class='" + getButtonIconCls(Wtf.etype.deletegridrow) + "'> </div>";
    },
    
    handleSubmit: function(a, b) {
        this.fireEvent("submit", this);
        this.close();
    },

    handleClose: function() {
        this.close();
    },
    
    handleRowClick: function(grid,rowindex,e) {
        if (e.getTarget(".delete-gridrow")) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.nee.48"), function(btn) {
                if (btn != "yes") {
                    return;
                }
                var store = grid.getStore();
                store.remove(store.getAt(rowindex));
            }, this);
        }
    },
    
    checkRow: function(obj) {
        var rec = obj.record;
        if (obj.field == "wastageQuantityType") {
            if (!rec.data.isWastageApplicable) {
                obj.cancel = true;
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.product.warningWastageApplicableProduct")], 2);
                return;
            }
        } else if (obj.field == "wastageQuantity") {
            if (!rec.data.isWastageApplicable) {
                obj.cancel = true;
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.product.warningWastageApplicableProduct")], 2);
                return;
            }
        }
    },
    
    updateRow: function(obj) {
        if (obj != null) {
            var rec = obj.record;
            if (obj.field == "wastageQuantityType") {
                rec.set("wastageQuantity", 0);
                return;
            } else if (obj.field == "wastageQuantity") {
                if (rec.data.wastageQuantityType == 1 && rec.data.wastageQuantity > 100) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invsetup.alert.wtg.quant.greater.than.100")], 2);
                    rec.set("wastageQuantity", obj.originalValue);
                    return;
                } else if (rec.data.wastageQuantityType == 0 && rec.data.wastageQuantity > rec.data.wastageInventoryQuantity) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invsetup.alert.wtg.quant.greater.than.inv")], 2);
                    rec.set("wastageQuantity", obj.originalValue);
                    return;
                }
            }
        }
    },
    
    updateSubtotal: function() {
        var storeCount = this.gridStore.getCount();
        for (var i=0; i<storeCount; i++) {
            var rec = this.gridStore.getAt(i);
            var disc = (rec.data['percentage']=== undefined || rec.data['percentage']=="") ? 100 : rec.data['percentage'];
            var Qty = rec.data['quantity'];
            rec.set("actualquantity", (disc*Qty)/100);
            rec.set("wastageInventoryQuantity", (disc*Qty)/100);
        }
    },
    
    getWastageDetails: function() {
        var arr = [];
        var includeLast = true;
        this.gridStore.each(function(rec) {
            arr.push(this.gridStore.indexOf(rec));
        }, this);
        var jarray = WtfGlobal.getJSONArray(this.grid, includeLast, arr);
        return jarray;
    },
    
    loadSavedWastageDetails: function() {
        var wastageDetailRecords = "";
        if (this.record.data.wastageDetails != undefined && this.record.data.wastageDetails.length>1) {
            wastageDetailRecords = eval('(' + this.record.data.wastageDetails + ')');
        }
        var recordQuantity = wastageDetailRecords.length;
        
        if (recordQuantity != 0) {
            for (var i=0; i<recordQuantity; i++) {
                var wastageDetailRecord = wastageDetailRecords[i];
                var rec = new this.gridRec(wastageDetailRecord);
                rec.beginEdit();
                var fields = this.gridStore.fields;
                
                for (var x=0; x<fields.items.length; x++) {
                    var value = wastageDetailRecord[fields.get(x).name];
                    if (fields.get(x).name == 'type' && value && value != '') {
                        value = decodeURI(value);
                    }
                    if (fields.get(x).name == 'productname' && value && value != '') {
                        value = decodeURI(value);
                    }
                    if (fields.get(x).name == 'desc' && value && value != '') {
                        value = decodeURI(value);
                    }
                    rec.set(fields.get(x).name, value);
                }
                
                rec.endEdit();
                rec.commit();
                this.gridStore.add(rec);
            }
        }
    },
    
    loadInventoryPartWastageDetails: function() {
        var record = this.gridStore.reader.recordType, f = record.prototype.fields, fi = f.items, fl = f.length;
        var blankObj = {};
        for (var j = 0; j < fl; j++) {
            f = fi[j];
            if (f.name != 'id') {
                blankObj[f.name] = '';
            }
        }
        
        var rec = new this.gridRec(blankObj);
        rec.beginEdit();
        rec.set("productid", this.record.data.productid);
        rec.set("productname", this.record.data.productname);
        rec.set("desc", this.record.data.desc);
        rec.set("type", this.record.data.type);
        rec.set("quantity", this.record.data.dquantity);
        rec.set("percentage", 100);
        rec.set("actualquantity", this.record.data.dquantity);
        rec.set("wastageInventoryQuantity", this.record.data.dquantity);
        rec.set("wastageQuantityType", 0);
        rec.set("wastageQuantity", 0);
        rec.set("isWastageApplicable", this.isWastageApplicable);
        rec.endEdit();
        rec.commit();
        this.gridStore.add(rec);
    }
});