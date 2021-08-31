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

Wtf.account.PriceListVolumeDiscount = function(config) {
    Wtf.apply(this, config);
    
    this.btnArr = [];
    this.addPriceListVolumeDiscount = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.addPriceListVolumeDiscount"), // "Add Price List - Volume Discount",
        tooltip : WtfGlobal.getLocaleText("acc.field.addPriceListVolumeDiscount"), // "Add Price List - Volume Discount",
        id: 'btnCreateNewPriceListVolumeDiscount' + this.id,
        scope: this,
        iconCls : getButtonIconCls(Wtf.etype.add),
        handler: this.handleCreateNewPriceListVolumeDiscount
    });
    this.btnArr.push(this.addPriceListVolumeDiscount);
    
    this.editPriceListVolumeDiscount = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.editPriceListVolumeDiscount"), // "Edit Price List - Volume Discount",
        tooltip : WtfGlobal.getLocaleText("acc.field.editPriceListVolumeDiscount"), // "Edit Price List - Volume Discount",
        id: 'btnEditPriceListVolumeDiscount' + this.id,
        scope: this,
        iconCls : getButtonIconCls(Wtf.etype.edit),
        disabled: true,
        handler: this.handleEditPriceListVolumeDiscount
    });
    this.btnArr.push(this.editPriceListVolumeDiscount);
    
    this.deletePriceListVolumeDiscount = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.deletePriceListVolumeDiscount"), // "Delete Price List - Volume Discount",
        tooltip : WtfGlobal.getLocaleText("acc.field.deletePriceListVolumeDiscount"), // "Delete Price List - Volume Discount",
        id: 'btnDeletePriceListVolumeDiscount' + this.id,
        scope: this,
        iconCls : getButtonIconCls(Wtf.etype.deletebutton),
        disabled: true,
        handler: this.handleDeletePriceListVolumeDiscount
    });
    this.btnArr.push(this.deletePriceListVolumeDiscount);
    
    this.setPrice = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.setPriceForPriceListVolumeDiscount"), // "Set Price for Price List - Volume Discount",
        tooltip : WtfGlobal.getLocaleText("acc.field.setPriceForPriceListVolumeDiscount"), // "Set Price for Price List - Volume Discount",
        id: 'btnSetPrice' + this.id,
        scope: this,
        iconCls : getButtonIconCls(Wtf.etype.add),
        disabled: true,
        handler: this.handleSetPriceOfPricingBand
    });
    this.btnArr.push(this.setPrice);
    
    var importbtnArray = [];
    
    importbtnArray.push(this.importProductPrice = new Wtf.Action({
        text: WtfGlobal.getLocaleText("acc.field.importPriceListVolumeDiscount"), // "Import Price - List Volume Discount",
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.import.csv"),
        iconCls: 'pwnd importcsv',
        handler: callProductImportWin.createDelegate(this,[true,false,false,false])
    }));
    
    this.importPriceListVolumeDiscountBtn = new Wtf.Action({
        text: WtfGlobal.getLocaleText("acc.common.import"),
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.common.import"),
        iconCls: (Wtf.isChrome? 'pwnd importChrome' : 'pwnd import'),
        menu: importbtnArray
    });
    
    this.priceListVolumeDiscountRec = Wtf.data.Record.create([
        {name: 'id'},
        {name: 'name'},
        {name: 'pricePolicyValue'},
        {name: 'desc'}
    ]);
    
    this.priceListVolumeDiscountStore = new Wtf.data.Store({
        url: "ACCMaster/getPriceListVolumeDiscount.do",
        reader: new Wtf.data.KwlJsonReader({
            totalProperty: "totalCount",
            root: "data"
        }, this.priceListVolumeDiscountRec)
    });
    
    this.priceListVolumeDiscountStore.on('load', function(store) {
        if (this.priceListVolumeDiscountStore.getCount() < 1) {
            this.priceListVolumeDiscountGrid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.priceListVolumeDiscountGrid.getView().refresh();
        }
    }, this);
    
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.rowNo = new Wtf.grid.RowNumberer();
    
    this.priceListVolumeDiscountGrid = new Wtf.grid.GridPanel({
        store: this.priceListVolumeDiscountStore,
        border: true,
        layout: 'fit',
        loadMask: true,
        sm: this.sm,
        viewConfig: {
            forceFit: true
        },
        columns: [this.rowNo, this.sm,
        {
            header: WtfGlobal.getLocaleText("acc.field.id"), // "id",
            dataIndex: "id",
            hidden: true,
            fixed: true
        },{
            header: WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscountName"), // "Price List - Volume Discount Name",
            dataIndex: 'name',
            renderer: WtfGlobal.deletedRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.field.description"), // "Description",
            dataIndex: 'desc',
            renderer : function(val) {
                val = val.replace(/(<([^>]+)>)/ig,"");
                return "<div wtf:qtip=\"" + val + "\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.field.description")+"'>" + val + "</div>";
            }
        }]
    });
    
    this.sm.on("selectionchange", this.enableDisableButtons.createDelegate(this), this);
    
    Wtf.account.PriceListVolumeDiscount.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.PriceListVolumeDiscount, Wtf.Panel, {
    onRender: function(config) {
        
        this.priceListVolumeDiscountStore.load({
            params: {
                start: 0,
                limit: 30
            }
        });
        
        this.centerPanel = new Wtf.Panel({
            region : 'center',
            layout: 'fit',
            bodyStyle: 'background-color:white; padding:0px 300px 0px 300px;',
            border: false,
            items: [this.priceListVolumeDiscountGrid],
            tbar: this.btnArr,
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.priceListVolumeDiscountStore,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({
                    id : "pPageSize_" + this.id
                }),
                items: [this.importPriceListVolumeDiscountBtn]
            })
        });
        
        this.add(this.centerPanel);
        
        Wtf.account.PriceListVolumeDiscount.superclass.onRender.call(this, config);
    },
    
    handleCreateNewPriceListVolumeDiscount: function() {
        this.createPriceListVolumeDiscountWin(false);
    },
    
    handleEditPriceListVolumeDiscount: function() {
        this.createPriceListVolumeDiscountWin(true);
    },
    
    handleDeletePriceListVolumeDiscount: function() {
        if (this.priceListVolumeDiscountGrid.getSelectionModel().hasSelection() == false) {
            WtfComMsgBox(34,2);
            return;
        }
        var data = [];
        var arr = [];
        this.recArr = this.priceListVolumeDiscountGrid.getSelectionModel().getSelections();
        this.priceListVolumeDiscountGrid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.priceListVolumeDiscountGrid, this.recArr, true, 0, 2);
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.rem.146") + " " + WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + "?", function(btn) {
            if (btn != "yes") {
                for (var i=0; i<this.recArr.length; i++) {
                    var ind = this.priceListVolumeDiscountStore.indexOf(this.recArr[i])
                    var num = ind%2;
                    WtfGlobal.highLightRowColor(this.priceListVolumeDiscountGrid, this.recArr[i], false, num, 2, true);
                }
                return;
            }
            for (i=0; i<this.recArr.length; i++) {
                arr.push(this.priceListVolumeDiscountStore.indexOf(this.recArr[i]));
            }
            data = WtfGlobal.getJSONArray(this.priceListVolumeDiscountGrid, true, arr);
            
            Wtf.Ajax.requestEx({
                url: "ACCMaster/deletePriceListVolumeDiscount.do",
                params: {
                    data: data
                }
            },this, this.genDeleteSuccessResponse, this.genDeleteFailureResponse);
        },this);
    },
    
    genDeleteSuccessResponse: function(response) {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.pricingBandMaster"),response.msg],response.success*2+1);
        for (var i=0; i<this.recArr.length; i++) {
            var ind = this.priceListVolumeDiscountStore.indexOf(this.recArr[i])
            var num = ind % 2;
            WtfGlobal.highLightRowColor(this.priceListVolumeDiscountGrid,this.recArr[i], false, num, 2, true);
        }
        if (response.success) {
            (function() {
                this.priceListVolumeDiscountStore.load();
            }).defer(WtfGlobal.gridReloadDelay(),this);
        }
    },
    
    genDeleteFailureResponse: function(response) {
        for (var i=0; i<this.recArr.length; i++) {
            var ind = this.priceListVolumeDiscountStore.indexOf(this.recArr[i])
            var num = ind % 2;
            WtfGlobal.highLightRowColor(this.priceListVolumeDiscountGrid, this.recArr[i], false, num, 2, true);
        }
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");  // "Failed to make connection with Web Server";
        if (response.msg) {
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg],2);
    },
    
    handleSetPriceOfPricingBand: function() {
        this.createSetPriceOfPricingBandWin(true);
    },
    
    enableDisableButtons: function() {
        var arr = this.sm.getSelections();
        
        if (this.deletePriceListVolumeDiscount) {
            this.deletePriceListVolumeDiscount.enable();
        }
        
        if (arr.length == 0) {
            if (this.deletePriceListVolumeDiscount) {
                this.deletePriceListVolumeDiscount.disable();
            }
        }
        
        if (arr.length == 1) {
            this.editPriceListVolumeDiscount.enable();
            this.setPrice.enable();
        } else {
            this.editPriceListVolumeDiscount.disable();
            this.setPrice.disable();
        }
    },
    
    createPriceListVolumeDiscountWin: function(isEdit) {
        this.rec = this.priceListVolumeDiscountGrid.getSelectionModel().getSelected();
        
        this.priceListVolumeDiscountName = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + "*",  // "Price List - Volume Discount" + "*",
            name: 'priceListVolumeDiscountName',
            id: "priceListVolumeDiscountID",
            style: "margin-left: 50px;",
            width: 250,
            maxLength: 500,
            scope: this,
            allowBlank: false,
            validator: Wtf.ValidatePaidReceiveName
//            value: isEdit? this.rec.data.name : ""
        });
        
        this.desc = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.description"), // "Description",
            style: "margin-left: 50px;",
            width: 250,
            height: 50,
//            allowBlank: false,
            maxLength: 1024
        });
        
        this.pricePolicy = new Wtf.form.FieldSet({
            xtype: 'fieldset',
            title: WtfGlobal.getLocaleText("acc.field.pricePolicy"), // "Price Policy",
            autoHeight: true,
            width: 465,
            labelWidth: 200,
            items:[
                this.radioUseDiscount = new Wtf.form.Radio({
                    name: 'pricePolicy',
                    labelAlign: 'left',
                    labelSeparator: '',
                    checked: true,
                    fieldLabel: WtfGlobal.getLocaleText("acc.field.useDiscount")  // 'Use Discount'
                }),
                this.radioUseFlatPrice = new Wtf.form.Radio({
                    name: 'pricePolicy',
                    labelAlign: 'left',
                    labelSeparator: '',
                    fieldLabel: WtfGlobal.getLocaleText("acc.field.useFlatPrice") // "Use Flat Price"
                })
            ]
        });
        
        this.addMasterItemForm = new Wtf.form.FormPanel({
            waitMsgTarget: true,
            border: false,
            region: 'center',
            layout: 'form',
            bodyStyle: "background: transparent;",
            style: "background:transparent; padding:20px;",
            labelWidth: 160,
            items: [this.priceListVolumeDiscountName, this.desc, this.pricePolicy]
        });
            
        this.addMasterItemWindow = new Wtf.Window({
            modal: true,
            title: WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount"), // "Price List - Volume Discount",
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            bodyStyle: 'padding:5px;',
            buttonAlign: 'right',
            width: 560,
            scope: this,
            items: [{
                    region: 'center',
                    border: false,
                    bodyStyle: 'background:#f1f1f1;font-size:10px;',
                    autoScroll: true,
                    items: [this.addMasterItemForm]
            }],
        buttons: [{
                text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                id: 'savePriceListVolumeDiscountBtn',
                scope: this,
                handler: function(button) {
                    var itemName = this.priceListVolumeDiscountName.getValue();
                    var desc = this.desc.getValue();
                    var pricePolicyValue;
                    if (this.radioUseDiscount.getValue()) {
                        pricePolicyValue = 1;
                    } else if (this.radioUseFlatPrice.getValue()) {
                        pricePolicyValue = 2;
                    }
                    if (this.addMasterItemForm.form.isValid()) {
                        this.saveMasterGroupItem("ok", itemName, isEdit, (isEdit? this.rec.data.id : ""), desc, pricePolicyValue);
                    } else {
                        return false;
                    }
                }
            }, {
                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                scope: this,
                handler: function() {
                    this.addMasterItemWindow.close();
                }
            }]
        });
        
        if (isEdit) {
            this.priceListVolumeDiscountName.setValue(this.rec.data.name);
            this.desc.setValue(this.rec.data.desc);
            
            if (this.rec.data.pricePolicyValue == 1) {
                this.radioUseDiscount.setValue(true);
            } else if (this.rec.data.pricePolicyValue == 2) {
                this.radioUseFlatPrice.setValue(true);
            }
            this.radioUseDiscount.disable();
            this.radioUseFlatPrice.disable();
        }

        this.addMasterItemWindow.show();
    },
    
    saveMasterGroupItem: function(btn, txt, isEdit, id, desc, pricePolicyValue) {
        var callUrl = "ACCMaster/savePriceListVolumeDiscount.do";
        if (btn == "ok") {
            if (txt.replace(/\s+/g, '') != "") {
                Wtf.getCmp("savePriceListVolumeDiscountBtn").disable();
                Wtf.Ajax.requestEx({
                    url: callUrl,
                    params: {
                        mode: 114,
                        id: id,
                        name: txt,
                        desc: desc,
                        pricePolicyValue: pricePolicyValue,
                        isEdit: isEdit
                    }
                },this, this.genSuccessResponse, this.genFailureResponse);
            } else {
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"),  // 'Master Configuration',
                    msg: WtfGlobal.getLocaleText("acc.masterConfig.common.enterNew") + " " + WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount"),  // "Enter New" + " " + "Price List - Volume Discount",
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.INFO,
                    width: 300,
                    scope: this,
                    fn: function() {
                        if (btn == "ok") {
                            this.createPriceListVolumeDiscountWin(isEdit);
                        }
                    }
                });

            }
        }
    },
    
    genSuccessResponse: function(response) {
        if (response.success) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.MasterConfiguration"), response.msg], response.success*2+1);
//            (function() {
                this.priceListVolumeDiscountStore.reload();
//            }).defer(WtfGlobal.gridReloadDelay(),this);
            if (this.addMasterItemWindow) {
                this.addMasterItemWindow.close();
            }
        } else {
            var msg = WtfGlobal.getLocaleText("acc.common.msg1"); // "Failed to make connection with Web Server";
            if (response.msg) {
                msg = response.msg;
            }
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg],2);
            Wtf.getCmp("savePriceListVolumeDiscountBtn").enable();
        }        
    },
    
    genFailureResponse: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); // "Failed to make connection with Web Server";
        if (response.msg) {
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg],2);
    },
    
    createSetPriceOfPricingBandWin: function() {
        this.rec = this.priceListVolumeDiscountGrid.getSelectionModel().getSelected();
        
        this.setPriceQuickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.setPriceForPriceListBand.QuickSearchEmptyText"), // "Search by Product Name, Product ID ...",
            width: 200,
            id: "setPriceQuickSearch" + this.id,
            field: 'productName'
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
        
        this.priceListVolumeDiscount = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + "*",  // "Price List - Volume Discount" + "*",
            name: 'priceListVolumeDiscount' + this.id,
            id: "priceListVolumeDiscountID" + this.id,
            width: 300,
            maxLength: 500,
            scope: this,
            allowBlank: false,
            validator: Wtf.ValidatePaidReceiveName,
            readOnly: true
        });
        
        this.setPriceDesc = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.description"), // "Description",
            width: 300,
            height: 50,
            maxLength: 1024,
            readOnly: true
        });
        this.pricingBandMasterRec = Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'name'
        }
        ]);
        this.pricingBandMasterStore = new Wtf.data.Store({
            url: "ACCMaster/getPricingBandItems.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
               
                onlypricebandflag:true
            }, this.pricingBandMasterRec)
        });
        this.pricelistbandmapping = new Wtf.common.Select({
            multiSelect:true,
            fieldLabel: WtfGlobal.getLocaleText("acc.field.selectpricelistband")+"*",
            forceSelection:true,
            isProductCombo:true,
            name:'pricelistbandmapping',
            allowBlank: false,
            width:300,
            maxHeight:250,   
            store:this.pricingBandMasterStore,
            listWidth:Wtf.ProductComboListWidth,
            listAlign:"bl-tl?", 
            valueField:'id',
            displayField:'name',
            addNoneRecord: true,
            selectOnFocus:true,
            clearTrigger: this.readOnly ? false : true,
            editable:true,
            triggerAction:'all'
        });
        this.pricingBandMasterStore.load();
        Wtf.Ajax.requestEx({
            url:"ACCMaster/getPricingbandMappedwithvolumeDisc.do",
            params:{
                pricingBandMasterID: this.rec.data.id
            }
        }, this, function(res){
            this.mappingRes=res;
            this.pricelistbandmapping.setValue(this.mappingRes.pricelistbandname);
            if (this.mappingRes && this.mappingRes.pricelistbandname!="") {
                /**
                 * hidding clear trigger button in edit when volume discount is already tagged.
                 */
                if(this.pricelistbandmapping.triggers !=undefined && this.pricelistbandmapping.triggers.length > 1){
                    this.pricelistbandmapping.triggers[0].hide();
                }
                this.pricelistbandmapping.setDisabled(true);
            }
        });
        this.pricingBandMasterStore.on('load',function() {
            this.pricelistbandmapping.setValue(this.mappingRes.pricelistbandname);
             this.changeColumnCofig();
        }, this);
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
            width: 300,
            store: this.currencyStore,
            valueField:'currencyid',
            displayField:'currencyname'
        }));
        
        this.Currency.on('select', function(combo, record, index) {
            this.changeColumnCofig();
            
            this.setPriceStore.load({
                params: {
                    pricingBandMasterID: this.priceListVolumeDiscount.getValue(),
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
                    pricingBandMasterID: this.rec.data.name.id,
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
            value: new Date(),
            width: 300,
            allowBlank: false
        });
        
        this.applicableDate.on('change',this.onDateChange,this);
        
        this.pricePolicyForSetPrice = new Wtf.form.FieldSet({
            xtype: 'fieldset',
            title: WtfGlobal.getLocaleText("acc.field.pricePolicy"), // "Price Policy",
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
        
        this.useCommonDiscount = new Wtf.form.Checkbox({
            name: 'useCommonDiscount',
            fieldLabel: WtfGlobal.getLocaleText("acc.field.useCommonDiscount"), // "Use Common Discount",
            checked: false,
            cls: 'custcheckbox',
            style: "margin-left: 0px;"
        });
        
         this.useCommonDiscount.on('render', function() {
             this.discountPanel.hide();
         },this);
           
        this.useCommonDiscount.on('check', function() {
            if (this.useCommonDiscount.getValue()) {
                this.setPriceGrid.hide();
//                this.Currency.getEl().up('.x-form-item').setDisplayed(false); // to hide label
//                this.Currency.hide();
                this.discountPanel.show();
                
                this.setPriceOfPricingBandWin.setHeight(585);
                this.setPriceOfPricingBandWin.setWidth(1100);
                this.setPriceOfPricingBandWin.doLayout();
                
                this.getPriceListCommonDisocunt();
            } else {
                this.setPriceGrid.show();
//                this.Currency.show();
//                this.Currency.getEl().up('.x-form-item').setDisplayed(true); // to show label
                this.discountPanel.hide();
                
                this.setPriceOfPricingBandWin.setHeight(695);
                this.setPriceOfPricingBandWin.setWidth(1100);
                this.setPriceOfPricingBandWin.doLayout();
            }
        },this);
        
        
        this.setPriceRec = new Wtf.data.Record.create([
            {name: 'productUUID'},
            {name: 'productName'},
            {name: 'productID'},
            {name: 'minimumQty', defaultValue: 0},
            {name: 'maximumQty', defaultValue: 0},
            {name: 'discountType', defaultValue: 0},
            {name: 'disocuntValue', defaultValue: 0},
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
            {name: 'salesPriceCNH', defaultValue: 0},
            {name: 'disocuntValueUSD', defaultValue: 0},
            {name: 'disocuntValueCAD', defaultValue: 0},
            {name: 'disocuntValueAUD', defaultValue: 0},
            {name: 'disocuntValueCNY', defaultValue: 0},
            {name: 'disocuntValueIDR', defaultValue: 0},
            {name: 'disocuntValueTWD', defaultValue: 0},
            {name: 'disocuntValueTHB', defaultValue: 0},
            {name: 'disocuntValuePHP', defaultValue: 0},
            {name: 'disocuntValueNZD', defaultValue: 0},
            {name: 'disocuntValueCHF', defaultValue: 0},
            {name: 'disocuntValueGBP', defaultValue: 0},
            {name: 'disocuntValueEUR', defaultValue: 0},
            {name: 'disocuntValueINR', defaultValue: 0},
            {name: 'disocuntValueSGD', defaultValue: 0},
            {name: 'disocuntValueMYR', defaultValue: 0},
            {name: 'disocuntValueCRC', defaultValue: 0},
            {name: 'disocuntValueUGX', defaultValue: 0},
            {name: 'disocuntValueKRW', defaultValue: 0},
            {name: 'disocuntValueAED', defaultValue: 0},
            {name: 'disocuntValueBND', defaultValue: 0},
            {name: 'disocuntValueHKD', defaultValue: 0},
            {name: 'disocuntValueJPY', defaultValue: 0},
            {name: 'disocuntValueVND', defaultValue: 0},
            {name: 'disocuntValueOMR', defaultValue: 0},
            {name: 'disocuntValueCNH', defaultValue: 0}
        ]);
        
        this.setPriceStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            }, this.setPriceRec),
            url: "ACCMaster/getPricingBandMasterDetails.do"
        });
        
        this.setPriceStore.on('beforeload', function() {
            var currentBaseParams = this.setPriceStore.baseParams;
            currentBaseParams.pricingBandMasterID = this.rec.data.id;
            currentBaseParams.currencyID = (this.Currency.getValue()!="")? this.Currency.getValue() : Wtf.account.companyAccountPref.currencyid;
            currentBaseParams.applicableDate = WtfGlobal.convertToGenericDate(this.applicableDate.getValue());
            currentBaseParams.isPricePolicyUseDiscount = (this.rec.data.pricePolicyValue == 1)? true : false,
            currentBaseParams.useCommonDiscount = this.useCommonDiscount.getValue()
            this.setPriceStore.baseParams = currentBaseParams;
        },this);
        
        this.setPriceStore.on('load', function(store) {
            if (this.rec.data.pricePolicyValue != 1) {
                this.changeColumnCofig();
            }
            
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
        
        this.gridDiscountTypeStore = new Wtf.data.SimpleStore({
            fields: [{name:'typeid',type:'int'}, 'name'],
            data :[[0,'Flat'], [1,'Percentage']]
        });
        
        var columnArr =[];
        
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.contract.product.name"),  // "Product Name",
            dataIndex: 'productName',
            align: 'left',
            width: 150,
            renderer: WtfGlobal.deletedRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.product.gridProductID"),  // "Product ID",
            dataIndex: 'productID',
            align: 'left',
            width: 150,
            renderer: WtfGlobal.deletedRenderer
        });
        
        this.setPriceCM = new Wtf.grid.ColumnModel(columnArr);
    
        this.initialColumnCnt = this.setPriceCM.getColumnCount();
        
        this.setPriceGrid = new Wtf.grid.EditorGridPanel({
            id: 'userActiveDaysGrid'+this.id,
            layout: 'fit',
            store: this.setPriceStore,
            cm: this.setPriceCM,
            clicksToEdit: 1,
            autoScroll: true,
            height: 200,
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
        
        this.minimumQty = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.minimumQty"), // "Minimum Qty",
            emptyText: WtfGlobal.getLocaleText("acc.field.enterMinimumQty"), // "Enter Minimum Qty",
            allowNegative: false,
            maxLength: 50,
            decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,
            allowBlank: false,
            width: 300
        });
        
        this.maximumQty = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.maximumQty"), // "Maximum Qty",
            emptyText: WtfGlobal.getLocaleText("acc.field.enterMaximumQty"), // "Enter Maximum Qty",
            allowNegative: false,
            maxLength: 14,
            decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,
            allowBlank: false,
            width: 300
        });
        
        this.discountTypeStore = new Wtf.data.SimpleStore({
            fields: [{name:'typeid',type:'int'}, 'name'],
            data :[[0,'Flat'], [1,'Percentage']]
        });
        
        this.discountType = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.discountType"), // "Discount Type",
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
            width: 285,
            listWidth: 300
        });
        
        this.disocuntValue = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.disocuntValue"), // "Discount Value",
            emptyText: WtfGlobal.getLocaleText("acc.field.enterDiscountValue"), // "Enter Discount Value",
            allowNegative: false,
            maxLength: 14,
            decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,
            allowBlank: false,
            width: 300
        });

        this.discountPanel = new Wtf.Panel({
            region: 'center',
            layout: 'fit',
            border: false,
            items:[{
                    layout: 'form',
                    border: false,
                    labelWidth: 245,
                    items: [this.minimumQty, this.maximumQty, this.discountType, this.disocuntValue]
            }]
        });
        
        this.setPriceOfPricingBandWin = new Wtf.Window({
            title: WtfGlobal.getLocaleText("acc.field.setPriceForPriceListVolumeDiscount"), // "Set Price for Price List - Volume Discount",
            resizable: false,
            width: 1100,
            height: 680,
            modal: true,
            layout: 'border',
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            scope: this,
            closable:false,
            buttons: [{
                text: WtfGlobal.getLocaleText("acc.msgbox.ok"), // "OK"
                scope: this,
                handler: function() {
                    if (this.useCommonDiscount.getValue()) {
                        this.savePriceListVolumeDisocunt();
                    } else if(this.pricelistbandmapping.getValue()==""){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.priceListBand.mandatory")], 2);
                        return;
                    }else{
                        this.savePricingBandMappingWithVolumedisc();
                        this.setPriceOfPricingBandWin.close();
                    }
                }
            },{
                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"), //"Cancel",
                scope: this,
                handler: function () {
                    this.setPriceOfPricingBandWin.close();
                }
            } ],
            items: [{
                region: 'north',
                height: 75,
                border: false,
                bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                html: getTopHtml(WtfGlobal.getLocaleText("acc.field.setPriceForPriceListVolumeDiscount"),WtfGlobal.getLocaleText("acc.field.setPriceForPriceListVolumeDiscount"), "../../images/accounting_image/price-list.gif")
            },{
                region: 'center',
                layout: 'fit',
                border: false,
                bodyStyle: 'background:#f1f1f1;padding:15px',
                items: [{
                    xtype: "form",
                    border: false,
                    id: this.id + "setpricefrm",
                    autoScroll: true,
                    labelWidth: 250,
                    items:[this.priceListVolumeDiscount, this.setPriceDesc, this.pricelistbandmapping, this.Currency, this.applicableDate, this.pricePolicyForSetPrice, this.useCommonDiscount, {
                        xtype: 'fieldset',
                        width: (Wtf.isIE)? '91%' : '85%',
                        height: '75%',
                        style: 'margin-top: 10px;',
                        title: WtfGlobal.getLocaleText("acc.field.Setprice"),
                        border : false,
                        labelWidth: 150,
                        items: [this.setPriceGrid, this.discountPanel]
                    }]
                }]
            }]
        });
        
        this.setPriceGrid.on('beforeedit', this.checkMinMaxQuantity, this);
        this.setPriceGrid.on('afteredit', this.saveRecord, this);
        
        this.setPriceOfPricingBandWin.on('render', function() {
            this.setPriceGrid.show();
        },this);
        
        // For Show Win
        this.setPriceOfPricingBandWin.show();
        
        // For setting values to the Win
        this.priceListVolumeDiscount.setValue(this.rec.data.name);
        this.setPriceDesc.setValue(this.rec.data.desc);
        
        if (this.rec.data.pricePolicyValue == 1) {
            this.radioSetPriceUseDiscount.setValue(true);
        } else if (this.rec.data.pricePolicyValue == 2) {
            this.radioSetPriceUseFlatPrice.setValue(true);
            
            WtfGlobal.hideFormElement(this.useCommonDiscount);
//            this.useCommonDiscount.hideLabel = true;
//            this.useCommonDiscount.hide();
        }
        this.radioSetPriceUseDiscount.setDisabled(true);
        this.radioSetPriceUseFlatPrice.setDisabled(true);
    },
    
    changeColumnCofig: function() {
        var config = this.setPriceCM.config.slice(0, this.initialColumnCnt);
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
        
        if (this.rec.data.pricePolicyValue == 1) {
            config.push({
                header: WtfGlobal.getLocaleText("acc.field.discountType"), // "Discount Type",
                dataIndex: 'discountType',
                align: 'left',
                width: 170,
                renderer: Wtf.comboBoxRenderer(new Wtf.form.ComboBox({
                    store: this.gridDiscountTypeStore,
                    name: 'discountType',
                    displayField: 'name',
                    valueField: 'typeid',
                    mode: 'local',
                    triggerAction: 'all',
                    forceSelection: true,
                    selectOnFocus: true
                })),
                editor: new Wtf.form.ComboBox({
                    store: this.gridDiscountTypeStore,
                    name: 'discountType',
                    displayField: 'name',
                    valueField: 'typeid',
                    mode: 'local',
                    triggerAction: 'all',
                    forceSelection: true,
                    selectOnFocus: true
                })
            });
        }
        
        var currencyIdsArray = this.Currency.getValue().split(",");
        if (currencyIdsArray != undefined && this.Currency.getValue() != "" && this.rec.data.pricePolicyValue != 1) {
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
                    editor: new Wtf.form.NumberField({
                        allowNegative: false,
                        maxLength: 14,
                        decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                    })
                });
                }

                if (Wtf.account.companyAccountPref.productPricingOnBandsForSales) {
                config.push({
                    header: WtfGlobal.getLocaleText("acc.productList.gridSalesPrice") + " (" + currencyRec.data.currencyname + ")",  // "Sales Price" + "(" + currencyRec.data.currencyname + ")",
                    dataIndex: 'salesPrice' + currencyRec.data.currencycode,
                    align: 'right',
                    width: 233,
                    editor: new Wtf.form.NumberField({
                        allowNegative: false,
                        maxLength: 14,
                        decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                    })
                });
            }
            }
        } else if (currencyIdsArray != undefined && this.Currency.getValue() != "") {
            for (var i=0; i<currencyIdsArray.length; i++) {
                var index = this.currencyStore.find('currencyid',currencyIdsArray[i]);
                var currencyRec = this.currencyStore.getAt(index);
                if (currencyIdsArray[i] == "1") {
                    currencyRec.data.currencyname = "US Dollars (USD)";
                    currencyRec.data.currencycode = "USD";
                }
                
                config.push({
                    header: WtfGlobal.getLocaleText("acc.field.disocuntValue") + " (" + currencyRec.data.currencyname + ")", // "Discount Value" + "(" + currencyRec.data.currencyname + ")",
                    dataIndex: 'disocuntValue' + currencyRec.data.currencycode,
                    align: 'right',
                    width: 200,
                    editor: new Wtf.form.NumberField({
                        allowNegative: false,
                        maxLength: 14,
                        decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                    })
                });
            }
        }
        this.setPriceStore.reader = new Wtf.data.KwlJsonReader(this.setPriceStore.reader.meta, this.setPriceStore.fields.items);
        this.setPriceGrid.getColumnModel().setConfig(config);
        var newcm = this.setPriceGrid.getColumnModel();
        this.setPriceGrid.reconfigure(this.setPriceStore,newcm);
        this.setPriceGrid.getView().refresh(true);
    },
    
    handleResetClick: function() {
        if (this.setPriceQuickPanelSearch.getValue()) {
            this.setPriceQuickPanelSearch.reset();
            this.setPriceStore.load({
                params: {
                    start: 0,
                    limit: 30
                }
            });
        }
    },
    checkMinMaxQuantity:function(obj){
        /**
         * Pop before editing grid:- Please select price band first.
         */
        if(this.pricelistbandmapping && this.pricelistbandmapping.getValue()==""){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.please")+" "+WtfGlobal.getLocaleText("acc.field.selectpricelistband")+" "+"first."], 2);
            return;
        }
        this.checkMinQty=false;
        if(obj.field=="minimumQty"){
           this.checkMinQty=true; 
        }
        if(obj.field=="maximumQty"){
           this.checkMinQty=false; 
        }
    },
    saveRecord: function(obj) {
        var params = {
            isVolumeDiscount: true,
            isPricePolicyUseDiscount: (this.rec.data.pricePolicyValue == 1)? true : false,
            pricingBandMasterID: this.rec.data.id,
            pricebandidsmappedwithvol: this.pricelistbandmapping.getValue(),
            currency: this.Currency.getValue(),
            applicableDate: WtfGlobal.convertToGenericDate(this.applicableDate.getValue()),
            productID: obj.record.data.productUUID,
            minimumQty: obj.record.data.minimumQty,
            maximumQty: obj.record.data.maximumQty,
            discountType: obj.record.data.discountType,
            checkMinQty:this.checkMinQty,
            column_Name: obj.field,
            column_Value: obj.value
        }
        
        var URL = "ACCMaster/savePricingBandMasterDetails.do";
        
        Wtf.Ajax.requestEx({
            url: URL,
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
    
    savePriceListVolumeDisocunt: function() {
        this.rec = this.priceListVolumeDiscountGrid.getSelectionModel().getSelected();
        
        var params = {
            pricingBandMasterID: this.rec.data.id,
            applicableDate: WtfGlobal.convertToGenericDate(this.applicableDate.getValue()),
            isPricePolicyUseDiscount: (this.rec.data.pricePolicyValue == 1)? true : false,
            useCommonDiscount: this.useCommonDiscount.getValue(),
            minimumQty: this.minimumQty.getValue(),
            maximumQty: this.maximumQty.getValue(),
            discountType: this.discountType.getValue(),
            disocuntValue: this.disocuntValue.getValue(),
            currency: this.Currency.getValue()
        }
        
        var URL = "ACCMaster/savePricingBandMasterDetails.do";
        
        Wtf.Ajax.requestEx({
            url: URL,
            params: params
        }, this, function (response) {
            if (response.success) {
                this.setPriceOfPricingBandWin.close();
            } else {
                var msg = WtfGlobal.getLocaleText("acc.common.msg1"); // "Failed to make connection with Web Server";
                if(response.msg) {
                    msg = response.msg;
                }
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
            }
            
        }, this.genFailureResponse);
    },
    
    savePricingBandMappingWithVolumedisc:function() {
        Wtf.Ajax.requestEx({
            url:"ACCMaster/savePricingbandMappedwithvolumeDisc.do",
            params:{
                pricingBandMasterID: this.rec.data.id,
                pricelistbandmapping:this.pricelistbandmapping.getValue()
            }
        }, this, function(res){
            });
    },
    getPriceListCommonDisocunt: function() {
        this.rec = this.priceListVolumeDiscountGrid.getSelectionModel().getSelected();
        
        var params = {
            pricingBandMasterID: this.rec.data.id,
            applicableDate: WtfGlobal.convertToGenericDate(this.applicableDate.getValue()),
            isPricePolicyUseDiscount: (this.rec.data.pricePolicyValue == 1)? true : false,
            useCommonDiscount: this.useCommonDiscount.getValue(),
            currency: this.Currency.getValue()
        }
        
        var URL = "ACCMaster/getPriceListCommonDiscount.do";
        
        Wtf.Ajax.requestEx({
            url: URL,
            params: params
        }, this, function (response) {
            if (response.success) {
                this.minimumQty.setValue(response.minimumQty);
                this.maximumQty.setValue(response.maximumQty);
                this.discountType.setValue(response.discountType);
                this.disocuntValue.setValue(response.disocuntValue);
            } else {
                var msg = WtfGlobal.getLocaleText("acc.common.msg1"); // "Failed to make connection with Web Server";
                if(response.msg) {
                    msg = response.msg;
                }
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
            }
            
        }, function () {
            
        });
    },
    
    onDateChange: function(a,val,oldval) {
        this.setPriceStore.load({
            params: {
                start: 0,
                limit: 30
            }
        });
    }
});