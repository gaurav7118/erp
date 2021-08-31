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

Wtf.account.setPriceListForBandWindow = function(config) {
    
    this.productStore = config.productStore;
    this.rec = config.record;
    this.isFlatPriceListVolumeDiscount = (config.isFlatPriceListVolumeDiscount!=null && config.isFlatPriceListVolumeDiscount!=undefined)? config.isFlatPriceListVolumeDiscount : false;
    
//================================================== For Applying Config to the Window =========================================================
    
    Wtf.apply(this, {
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.msgbox.ok"),  // 'OK',
            scope: this,
            handler: this.closeForm.createDelegate(this)
        }]
    }, config);
    
    Wtf.account.setPriceListForBandWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.setPriceListForBandWindow, Wtf.Window, {
    
//======================================================= For Rendering Window =================================================================

    onRender: function(config) {
        Wtf.account.setPriceListForBandWindow.superclass.onRender.call(this, config);
        
        this.createFields();
        this.createGrid();
        this.createForm();
        
        this.add(this.northPanel = new Wtf.Panel({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(WtfGlobal.getLocaleText("acc.field.setPriceForPricingBand"),WtfGlobal.getLocaleText("acc.field.setPriceForPricingBandDesc"), "../../images/accounting_image/price-list.gif")
        }));
        
        this.add(this.centerPanel = new Wtf.Panel({
            region: 'center',
            border: false,
            autoScroll: true,
            bodyStyle: 'background:#f1f1f1; font-size:10px; padding:10px',
            baseCls: 'bckgroundcolor',
            layout: 'fit',
            items: [this.setPriceListForm]
        }));
        
        if (this.isFlatPriceListVolumeDiscount) {
            this.radioSetPriceUseFlatPrice.setValue(true);
        }
        this.radioSetPriceUseDiscount.setDisabled(true);
        this.radioSetPriceUseFlatPrice.setDisabled(true);
    },
    
    
//======================================================= For Creating Form Fields ==============================================================

    createFields: function() {
        
        this.setPriceQuickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: this.isFlatPriceListVolumeDiscount? WtfGlobal.getLocaleText("acc.setPriceListForVolumeDiscountWin.QuickSearchEmptyText") : WtfGlobal.getLocaleText("acc.setPriceListForBandWin.QuickSearchEmptyText"), // "Search by Price List - Volume Discount ..." : "Search by Price List - Band Name ...",
            width: 200,
            id: "setPriceQuickSearch" + this.id,
            field: 'bandName'
        });
        
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"),  // 'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"),  // 'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.resetBttn.on('click', this.handleResetClick, this);
        
        this.productCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.contract.product.name"),  // "Product Name",
            id: "productCombo" + this.id,
            store: this.productStore,
            displayField: 'productname',
            extraFields: ['pid'],
            extraComparisionField: 'pid', 
            valueField: 'productid',
            emptyText: WtfGlobal.getLocaleText("acc.prod.comboEmptytext"), // 'Please select product',
            mode: 'local',
            width: 240,
            name: 'name',
            hiddenName:'name',
            triggerAction: 'all',
            forceSelection: true,
            selectOnFocus: true,
            scope: this,
            listeners: {
                'select': {
                    fn: function(combo, record, index) {
                        this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
                        
                        this.setPriceStore.load({
                            params: {
                                pricingBandMasterID: record.data.id,
                                currencyID: Wtf.account.companyAccountPref.currencyid,
                                start: 0,
                                limit: 30
                            }
                        });
                    },
                    scope: this
                }
            }
        });
        this.productCombo.setValue(this.rec.data.productid);
        
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
         this.currencyStore.load();
         
         this.currencyStore.on('load',function(s,o) {
             this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
             this.changeColumnCofig();
        }, this);
        
        this.Currency = new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.currency.tt") + "'>" + WtfGlobal.getLocaleText("acc.currency.cur") + "*" + "</span>", // 'Currency' + '*',
            forceSelection: true
        },{
            name: 'currencyid',
            width: 240,
            store: this.currencyStore,
            valueField:'currencyid',
            displayField:'currencyname'
        }));
        
        this.Currency.on('select', function(combo, record, index) {
            this.changeColumnCofig();
            
            this.setPriceStore.load({
                params: {
                    pricingBandMasterID: this.productCombo.getValue(),
                    currency: this.Currency.getValue(),
                    start: 0,
                    limit: 30
                }
            });
        }, this);
        
        this.Currency.on('clearval', function() {
            this.changeColumnCofig();
        }, this);
        
        this.Currency.on('blur', function() {
            this.changeColumnCofig();
            
            this.setPriceStore.load({
                params: {
                    pricingBandMasterID: this.productCombo.getValue(),
                    currency: this.Currency.getValue(),
                    start: 0,
                    limit: 30
                }
            });
        }, this);
        
        this.applicableDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.applicableDate"),  // 'Applicable Date',
            name: 'applydate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: Wtf.serverDate.clearTime(true),
            width: 240,
            allowBlank: false
        });
        
        this.applicableDate.on('change',this.onDateChange,this);
        
        this.pricePolicyForSetPrice = new Wtf.form.FieldSet({
            xtype: 'fieldset',
            title: WtfGlobal.getLocaleText("acc.field.pricePolicy"), // "Price Policy",
            hidden: !this.isFlatPriceListVolumeDiscount,
            autoHeight: true,
            width: 555,
            labelWidth: 240,
            items:[
            this.radioSetPriceUseDiscount = new Wtf.form.Radio({
                name: 'pricePolicy',
                labelAlign: 'left',
                labelSeparator: '',
                fieldLabel: WtfGlobal.getLocaleText("acc.field.useDiscount")  // 'Use Discount'
            }),
            this.radioSetPriceUseFlatPrice = new Wtf.form.Radio({
                name: 'pricePolicy',
                labelAlign: 'left',
                labelSeparator: '',
                fieldLabel: WtfGlobal.getLocaleText("acc.field.useFlatPrice") // "Use Flat Price"
            })
            ]
        });
    },
    
    onDateChange: function(a,val,oldval) {
        this.setPriceStore.load({
            params: {
                start: 0,
                limit: 30
            }
        });
    },
    
//========================================================== For Creating Grid ====================================================================

    createGrid: function() {
        this.setPriceRec = new Wtf.data.Record.create([
            {name: 'bandUUID'},
            {name: 'bandName'},
            {name: 'desc'},
            {name: 'minimumQty', defaultValue: 0},
            {name: 'maximumQty', defaultValue: 0},
            {name: 'purchasePriceUSD', defaultValue: 0},
            {name: 'salesPriceUSD', defaultValue: 0},
            {name: 'purchasePriceCAD', defaultValue: 0},
            {name: 'salesPriceCAD', defaultValue: 0},
            {name: 'purchasePriceAUD', defaultValue: 0},
            {name: 'salesPriceAUD', defaultValue: 0},
            {name: 'purchasePriceCNY', defaultValue: 0},
            {name: 'salesPriceCNY', defaultValue: 0},
            {name: 'purchasePriceIDR', defaultValue: 0},
            {name: 'salesPriceIDR', defaultValue: 0},
            {name: 'purchasePriceTWD', defaultValue: 0},
            {name: 'salesPriceTWD', defaultValue: 0},
            {name: 'purchasePriceTHB', defaultValue: 0},
            {name: 'salesPriceTHB', defaultValue: 0},
            {name: 'purchasePricePHP', defaultValue: 0},
            {name: 'salesPricePHP', defaultValue: 0},
            {name: 'purchasePriceNZD', defaultValue: 0},
            {name: 'salesPriceNZD', defaultValue: 0},
            {name: 'purchasePriceCHF', defaultValue: 0},
            {name: 'salesPriceCHF', defaultValue: 0},
            {name: 'purchasePriceGBP', defaultValue: 0},
            {name: 'salesPriceGBP', defaultValue: 0},
            {name: 'purchasePriceEUR', defaultValue: 0},
            {name: 'salesPriceEUR', defaultValue: 0},
            {name: 'purchasePriceINR', defaultValue: 0},
            {name: 'salesPriceINR', defaultValue: 0},
            {name: 'purchasePriceSGD', defaultValue: 0},
            {name: 'salesPriceSGD', defaultValue: 0},
            {name: 'purchasePriceMYR', defaultValue: 0},
            {name: 'salesPriceMYR', defaultValue: 0},
            {name: 'purchasePriceCRC', defaultValue: 0},
            {name: 'salesPriceCRC', defaultValue: 0},
            {name: 'purchasePriceUGX', defaultValue: 0},
            {name: 'salesPriceUGX', defaultValue: 0},
            {name: 'purchasePriceKRW', defaultValue: 0},
            {name: 'salesPriceKRW', defaultValue: 0},
            {name: 'purchasePriceAED', defaultValue: 0},
            {name: 'salesPriceAED', defaultValue: 0},
            {name: 'purchasePriceBND', defaultValue: 0},
            {name: 'salesPriceBND', defaultValue: 0},
            {name: 'purchasePriceHKD', defaultValue: 0},
            {name: 'salesPriceHKD', defaultValue: 0},
            {name: 'purchasePriceJPY', defaultValue: 0},
            {name: 'salesPriceJPY', defaultValue: 0},
            {name: 'purchasePriceVND', defaultValue: 0},
            {name: 'salesPriceVND', defaultValue: 0},
            {name: 'purchasePriceOMR', defaultValue: 0},
            {name: 'salesPriceOMR', defaultValue: 0},
            {name: 'purchasePriceCNH', defaultValue: 0},
            {name: 'salesPriceCNH', defaultValue: 0}
        ]);
        
        this.setPriceStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            }, this.setPriceRec),
            url: "ACCMaster/getPricingBandMasterProductDetails.do"
        });
        
        this.setPriceStore.on('beforeload', function() {
            var currentBaseParams = this.setPriceStore.baseParams;
            currentBaseParams.productID = (this.productCombo.getValue()!="")? this.productCombo.getValue() : this.rec.data.productid;
            currentBaseParams.currencyID = (this.Currency.getValue()!="")? this.Currency.getValue() : Wtf.account.companyAccountPref.currencyid;
            currentBaseParams.applicableDate = WtfGlobal.convertToGenericDate(this.applicableDate.getValue());
            currentBaseParams.isFlatPriceListVolumeDiscount = this.isFlatPriceListVolumeDiscount;
            this.setPriceStore.baseParams = currentBaseParams;
        },this);
        
        this.setPriceStore.on('load', function(store) {
            this.changeColumnCofig();
            
            if (this.setPriceStore.getCount() < 1) {
                this.setPriceGrid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.setPriceGrid.getView().refresh();
            }
            this.setPriceQuickPanelSearch.StorageChanged(store);
        }, this);
        
        this.setPriceStore.on('datachanged', function() {
            var p = this.pPSetPrice.combo.value;
            this.setPriceQuickPanelSearch.setPage(p);
        }, this);
        
        this.setPriceStore.load({
            params: {
                start: 0,
                limit: 30
            }
        });
        
        this.columnArr = [];
        
        this.columnArr.push({
            header: this.isFlatPriceListVolumeDiscount? WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") : WtfGlobal.getLocaleText("acc.field.bandName"),  // "Price List - Volume Discount" : "Band Name",
            dataIndex: 'bandName',
            align: 'left',
            width: 220,
            renderer: WtfGlobal.deletedRenderer
        });
        
        if (this.isFlatPriceListVolumeDiscount) {
            this.columnArr.push({
                header: WtfGlobal.getLocaleText("acc.field.description"), // "Description",
                dataIndex: 'desc',
                align: 'left',
                width: 220,
                renderer: WtfGlobal.deletedRenderer
            });
        }
        
        this.setPriceCM = new Wtf.grid.ColumnModel(this.columnArr);
        
        this.initialColumnCnt = this.setPriceCM.getColumnCount();
        
        this.setPriceGrid = new Wtf.grid.EditorGridPanel({
            id: 'userActiveDaysGrid'+this.id,
            layout: 'fit',
            store: this.setPriceStore,
            cm: this.setPriceCM,
            clicksToEdit: 1,
            autoScroll: true,
            height: this.isFlatPriceListVolumeDiscount? 250 : 310,
            width: 1000,
            border: false,
            loadMask: true,
            cls: 'vline-on',
            viewConfig: {
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            },
            tbar: [this.setPriceQuickPanelSearch, this.resetBttn],
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbarofdetails" + this.id,
                store: this.setPriceStore,
                searchField: this.setPriceQuickPanelSearch,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                plugins: this.pPSetPrice = new Wtf.common.pPageSize({
                    id : "detailspPageSize_" + this.id
                })
            })
        });
        
        this.setPriceGrid.on('afteredit', this.saveRecord, this);
    },
    
//======================================================= For Changing Grid Column Model  ============================================================
    
    changeColumnCofig: function() {
        var config = this.setPriceCM.config.slice(0, this.initialColumnCnt);
        
        if (this.isFlatPriceListVolumeDiscount) {
            config.push({
                header: WtfGlobal.getLocaleText("acc.field.minimumQty"),  // "Minimum Qty",
                dataIndex: 'minimumQty',
                align: 'right',
                width: 100,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            });
        
            config.push({
                header: WtfGlobal.getLocaleText("acc.field.maximumQty"),  // "Maximum Qty",
                dataIndex: 'maximumQty',
                align: 'right',
                width: 100,
                editor: new Wtf.form.NumberField({
                    allowNegative: false,
                    maxLength: 14,
                    decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                })
            });
        }
        
        var currencyIdsArray = this.Currency.getValue().split(",");
        if (currencyIdsArray != undefined && this.Currency.getValue() != "") {
            for (var i=0; i<currencyIdsArray.length; i++) {
                var index = this.currencyStore.find('currencyid',currencyIdsArray[i]);
                var currencyRec = this.currencyStore.getAt(index);
                if (currencyIdsArray[i] == "1") {
                    currencyRec.data.currencyname = "US Dollars (USD)";
                    currencyRec.data.currencycode = "USD";
                }
                
                if (Wtf.account.companyAccountPref.productPricingOnBands) {
                config.push({
                    header: WtfGlobal.getLocaleText("acc.productList.gridPurchasePrice") + " (" + currencyRec.data.currencyname + ")", // "Purchase Price" + "(" + currencyRec.data.currencyname + ")",
                    dataIndex: 'purchasePrice' + currencyRec.data.currencycode,
                    align: 'right',
                    width: 233,
                    renderer: function(v,m,rec){
                      if(Wtf.dispalyUnitPriceAmountInPurchase){
                          return v;
                      } else{
                          return Wtf.UpriceAndAmountDisplayValue;
                      } 
                    },
                    editor: Wtf.dispalyUnitPriceAmountInPurchase?new Wtf.form.NumberField({
                        allowNegative: false,
                        maxLength: 14,
                        decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                    }):""
                });
                }

                if (Wtf.account.companyAccountPref.productPricingOnBandsForSales) {
                config.push({
                    header: WtfGlobal.getLocaleText("acc.productList.gridSalesPrice") + " (" + currencyRec.data.currencyname + ")",  // "Sales Price" + "(" + currencyRec.data.currencyname + ")",
                    dataIndex: 'salesPrice' + currencyRec.data.currencycode,
                    align: 'right',
                    width: 233,
                    renderer: function(v,m,rec){
                        if(Wtf.dispalyUnitPriceAmountInSales){
                            return v;
                        } else{
                            return Wtf.UpriceAndAmountDisplayValue;
                        } 
                    },
                    editor: Wtf.dispalyUnitPriceAmountInSales?new Wtf.form.NumberField({
                        allowNegative: false,
                        maxLength: 14,
                        decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                    }):""
                });
            }
        }
        }
        this.setPriceStore.reader = new Wtf.data.KwlJsonReader(this.setPriceStore.reader.meta, this.setPriceStore.fields.items);
        this.setPriceGrid.getColumnModel().setConfig(config);
        var newcm = this.setPriceGrid.getColumnModel();
        this.setPriceGrid.reconfigure(this.setPriceStore,newcm);
        this.setPriceGrid.getView().refresh(true);
    },    
    
//======================================================= For Creating Form ========================================================================
    
    createForm: function() {
        this.setPriceListForm = new Wtf.form.FormPanel({
            waitMsgTarget: true,
            border: false,
            labelWidth: 200,
            items: [this.productCombo, this.Currency, this.applicableDate, this.pricePolicyForSetPrice, {
                xtype: 'fieldset',
                width: (Wtf.isIE)?'91%' : '85%',
                height: '75%',
                style: 'margin-top: 20px;',
                title: WtfGlobal.getLocaleText("acc.field.Setprice"),
                border : false,
                labelWidth: 150,
                items: [this.setPriceGrid]
            }]
        });
    },
    
//======================================================= For Saving Data of Form =====================================================================
    
    saveRecord: function(obj) {
        var params;
        
        if (this.isFlatPriceListVolumeDiscount) {
            params = {
                isVolumeDiscount: true,
                isPricePolicyUseDiscount: false,
                pricingBandMasterID: obj.record.data.bandUUID,
                currency: this.Currency.getValue(),
                applicableDate: WtfGlobal.convertToGenericDate(this.applicableDate.getValue()),
                productID: this.productCombo.getValue(),
                minimumQty: obj.record.data.minimumQty,
                maximumQty: obj.record.data.maximumQty,
                column_Name: obj.field,
                column_Value: obj.value
            }
        } else {
            params = {
                pricingBandMasterID: obj.record.data.bandUUID,
                currency: this.Currency.getValue(),
                applicableDate: WtfGlobal.convertToGenericDate(this.applicableDate.getValue()),
                productID: this.productCombo.getValue(),
                column_Name: obj.field,
                column_Value: obj.value
            }
        }
        
        Wtf.Ajax.requestEx({
            url: "ACCMaster/savePricingBandMasterDetails.do",
            params: params
        }, this, function (response) {
            if (response.success) {
                obj.record.commit();
                this.setPriceStore.reload();
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

//========================================================== For Closing Form ========================================================================

    closeForm: function() {
        this.close();
    }
});