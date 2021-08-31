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

Wtf.account.PricingBandMaster = function(config) {
    Wtf.apply(this, config);
    
    this.bandArrEDSingleS = []; // Enable/Disable button's indexes on single select
    this.bandArrEDMultiS = []; // Enable/Disable button's indexes on multi select
    this.btnArr = [];
    this.createNewPricingBand = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.addPricingBand"), // "Add Pricing Band",
        tooltip : WtfGlobal.getLocaleText("acc.field.addPricingBand"), // "Add Pricing Band",
        id: 'btnCreateNewPricingBand' + this.id,
        scope: this,
        iconCls : getButtonIconCls(Wtf.etype.add),
        handler: this.handleCreateNewPricingBand
    });
    this.btnArr.push(this.createNewPricingBand);
    
    this.editPricingBand = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.editPricingBand"), // "Edit Pricing Band",
        tooltip : WtfGlobal.getLocaleText("acc.field.editPricingBand"), // "Edit Pricing Band",
        id: 'btnEditPricingBand' + this.id,
        scope: this,
        iconCls : getButtonIconCls(Wtf.etype.edit),
        disabled: true,
        handler: this.handleEditPricingBand
    });
    this.btnArr.push(this.editPricingBand);
    this.bandArrEDSingleS.push(this.btnArr.length - 1);
    
    this.copyPricingBand = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.copyPriceListBand"), // "Copy Price List - Band",
        tooltip : WtfGlobal.getLocaleText("acc.field.copyPriceListBand"), // "Copy Price List - Band",
        id: 'btnCopyPricingBand' + this.id,
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.edit),
        disabled: true,
        handler: this.handleCopyPricingBand
    });
    this.btnArr.push(this.copyPricingBand);
    this.bandArrEDSingleS.push(this.btnArr.length - 1);
    
    this.deletePricingBand = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.deletePricingBand"), // "Delete Pricing Band",
        tooltip : WtfGlobal.getLocaleText("acc.field.deletePricingBand"), // "Delete Pricing Band",
        id: 'btnDeletePricingBand' + this.id,
        scope: this,
        iconCls : getButtonIconCls(Wtf.etype.deletebutton),
        disabled: true,
        handler: this.handleDeletePricingBand
    });
    this.btnArr.push(this.deletePricingBand);
    this.bandArrEDMultiS.push(this.btnArr.length - 1);
    
    this.setPrice = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.setPriceForBand"), // "Set Price for Band",
        tooltip : WtfGlobal.getLocaleText("acc.field.setPriceForBand"), // "Set Price for Band",
        id: 'btnSetPrice' + this.id,
        scope: this,
        iconCls : getButtonIconCls(Wtf.etype.add),
        disabled: true,
        handler: this.handleSetPriceOfPricingBand
    });
    this.btnArr.push(this.setPrice);
    this.bandArrEDSingleS.push(this.btnArr.length - 1);
    
    this.setProductBrandDiscount = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Set") + " " + WtfGlobal.getLocaleText("acc.invoice.discount"), // "Set Discount",
        tooltip: WtfGlobal.getLocaleText("acc.field.Set") + " " + WtfGlobal.getLocaleText("acc.invoice.discount"),
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.add),
        disabled: true,
        handler: this.handleSetProductBrandDiscount
    });
    this.btnArr.push(this.setProductBrandDiscount);
    this.bandArrEDSingleS.push(this.btnArr.length - 1);
    
    this.btnArr.push(new Wtf.Action({
        text: WtfGlobal.getLocaleText("acc.productList.dataSync"), // 'Data Sync',
        iconCls: getButtonIconCls(Wtf.etype.sync),
        menu : [{
            text: WtfGlobal.getLocaleText("acc.field.DataSyncToPOS"), // "Data Sync To POS",
            tooltip: WtfGlobal.getLocaleText("acc.field.DataSyncToPOS"),
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.sync),
            handler: this.syncPriceListBandPrice.createDelegate(this,this.isOnlySelected=[false])
        }]
    }));
    
    var extraConfig = {};
    extraConfig.url= "ACCProduct/importPriceListBandPrice.do";
    var importbtnArray = Wtf.importMenuArray(this, "Price List - Band", this.pricingBandMasterStore, "", extraConfig);
    
    this.importProductBtn = new Wtf.Action({
        text: WtfGlobal.getLocaleText("acc.common.import"),
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.common.import"),
        iconCls: (Wtf.isChrome? 'pwnd importChrome' : 'pwnd import'),
        menu: importbtnArray
    });
    
    this.pricingBandMasterRec = Wtf.data.Record.create([
        {name: 'id'},
        {name: 'name'},
        {name: 'currencyID'},
        {name:'isincludinggst'},
        {name: 'isDefaultToPOS'}
    ]);
    
    this.pricingBandMasterStore = new Wtf.data.Store({
        url: "ACCMaster/getPricingBandItems.do",
        reader: new Wtf.data.KwlJsonReader({
            totalProperty: "totalCount",
            root: "data"
        }, this.pricingBandMasterRec)
    });
    
    this.pricingBandMasterStore.on('load', function(store) {
        if (this.pricingBandMasterStore.getCount() < 1) {
            this.pricingBandMasterGrid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.pricingBandMasterGrid.getView().refresh();
        }
    }, this);
    
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.rowNo = new Wtf.grid.RowNumberer();
    
    this.discountmasterRec = new Wtf.data.Record.create([
            {
                name: 'discountid'
            }, {
                name: 'discountname'
            }, {
                name: 'discountdescription'
            }, {
                name: 'discounttype'
            }, {
                name:'discountvalue'
            }, {
                name:'discountaccount'
            }
        ]);
        
         this.discountMasterStore = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                        root: "data",
                        totalProperty: "count"
            }, this.discountmasterRec),
            url : "AccDiscountController/getDiscountMaster.do",
            baseParams:{
                        companyid:companyid
            }
        });
        
        this.discountMasterStore.load();
       
        
    this.pricingBandMasterGrid = new Wtf.grid.GridPanel({
        store: this.pricingBandMasterStore,
        border: true,
        layout: 'fit',
        loadMask: true,
        sm: this.sm,
        viewConfig: {
            forceFit:true
        },
        columns: [this.rowNo, this.sm,
        {
            header: WtfGlobal.getLocaleText("acc.field.id"), // "id",
            dataIndex: "id",
            hidden: true,
            fixed: true
        },{
            header: WtfGlobal.getLocaleText("acc.field.bandName"), // "Band Name",
            dataIndex: 'name',
            renderer: WtfGlobal.deletedRenderer
        }]
    });
    
    this.sm.on("selectionchange", this.enableDisableButtons.createDelegate(this), this);
    
    Wtf.account.PricingBandMaster.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.PricingBandMaster, Wtf.Panel, {
    onRender: function(config) {
        
        this.pricingBandMasterStore.load({
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
            items: [this.pricingBandMasterGrid],
            tbar: this.btnArr,
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.pricingBandMasterStore,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({
                    id : "pPageSize_" + this.id
                }),
                items: [this.importProductBtn]
            })
        });
        
        this.add(this.centerPanel);
        
        Wtf.account.PricingBandMaster.superclass.onRender.call(this, config);
    },
    
    handleCreateNewPricingBand: function() {
        this.createPricingBandWin(false, false);
    },
    
    handleEditPricingBand: function() {
        this.createPricingBandWin(true, false);
    },
    
    handleCopyPricingBand: function() {
        this.createPricingBandWin(true, true);
    },
    
    handleDeletePricingBand: function() {
        if (this.pricingBandMasterGrid.getSelectionModel().hasSelection() == false) {
            WtfComMsgBox(34,2);
            return;
        }
        var data = [];
        var arr = [];
        this.recArr = this.pricingBandMasterGrid.getSelectionModel().getSelections();
        this.pricingBandMasterGrid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.pricingBandMasterGrid, this.recArr, true, 0, 2);
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.rem.146") + " " + WtfGlobal.getLocaleText("acc.field.pricingBandMaster") + "?", function(btn) {
            if (btn != "yes") {
                for (var i=0; i<this.recArr.length; i++) {
                    var ind = this.pricingBandMasterStore.indexOf(this.recArr[i])
                    var num = ind%2;
                    WtfGlobal.highLightRowColor(this.pricingBandMasterGrid, this.recArr[i], false, num, 2, true);
                }
                return;
            }
            for (i=0; i<this.recArr.length; i++) {
                arr.push(this.pricingBandMasterStore.indexOf(this.recArr[i]));
            }
            data = WtfGlobal.getJSONArray(this.pricingBandMasterGrid, true, arr);
            
            Wtf.Ajax.requestEx({
                url: "ACCMaster/deletePricingBands.do",
                params: {
                    data: data
                }
            },this, this.genDeleteSuccessResponse, this.genDeleteFailureResponse);
        },this);
    },
    
    genDeleteSuccessResponse: function(response) {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.pricingBandMaster"),response.msg],response.success*2+1);
        for (var i=0; i<this.recArr.length; i++) {
            var ind = this.pricingBandMasterStore.indexOf(this.recArr[i])
            var num = ind % 2;
            WtfGlobal.highLightRowColor(this.pricingBandMasterGrid,this.recArr[i], false, num, 2, true);
        }
        if (response.success) {
            (function() {
                this.pricingBandMasterStore.load();
            }).defer(WtfGlobal.gridReloadDelay(),this);
        }
    },
    
    genDeleteFailureResponse: function(response) {
        for (var i=0; i<this.recArr.length; i++) {
            var ind = this.pricingBandMasterStore.indexOf(this.recArr[i])
            var num = ind % 2;
            WtfGlobal.highLightRowColor(this.pricingBandMasterGrid, this.recArr[i], false, num, 2, true);
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
    
    /**
     * For calling Product Brand Discount Window.
     */
    handleSetProductBrandDiscount: function() {
        var record = this.pricingBandMasterGrid.getSelectionModel().getSelected();
        callProductBrandDiscount(record);
    },
    
    enableDisableButtons: function() {
        WtfGlobal.enableDisableBtnArr(this.btnArr, this.pricingBandMasterGrid, this.bandArrEDSingleS, this.bandArrEDMultiS);
    },
    
    createPricingBandWin: function(isEdit, isCopy) {
        this.rec = this.pricingBandMasterGrid.getSelectionModel().getSelected();
        
        this.bandName = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.masterConfig.common.enterNew") + " " + WtfGlobal.getLocaleText("acc.field.pricingBandMaster") + "*",  // "Enter New" + " " + "Pricing Band" + "*",
            name: 'bandName',
            id: "pricingBandName",
            style: "margin-left:30px;",
            width: 200,
            maxLength: 500,
            scope: this,
            allowBlank: false,
            validator: Wtf.ValidatePaidReceiveName,
            value: (isEdit && !isCopy) ? this.rec.data.name : ""
        });
        
        this.isDefaultToPOS = new Wtf.form.Checkbox({
            name: 'isDefaultToPOS',
            fieldLabel: WtfGlobal.getLocaleText("acc.field.SetToRetailPrice"), // "Set to Retail Price",
            checked: isEdit? this.rec.data.isDefaultToPOS : false,
            cls: 'custcheckbox',
            width: 75
        });
        this.isIncludingGSTMain = new Wtf.form.Checkbox({
            name:'isIncludingGSTMain',
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingGST") +"'>"+ WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingGST")  +"</span>",
            style: 'margin-top: 10px;',
            width: 75,
            hidden : (Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA || Wtf.account.companyAccountPref.avalaraIntegration) ? true : false,
            hideLabel : (Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA || Wtf.account.companyAccountPref.avalaraIntegration) ? true : false,
            checked:isEdit? this.rec.data.isincludinggst : false
        });
        this.addMasterItemForm = new Wtf.form.FormPanel({
            waitMsgTarget: true,
            border: false,
            region: 'center',
            layout: 'form',
            bodyStyle: "background: transparent;",
            style: "background:transparent; padding:20px;",
            labelWidth: 160,
            items: [this.bandName, this.isDefaultToPOS,this.isIncludingGSTMain]
        });
            
        this.addMasterItemWindow = new Wtf.Window({
            modal: true,
            title: isCopy ? WtfGlobal.getLocaleText("acc.field.copyPriceListBand") : WtfGlobal.getLocaleText("acc.field.pricingBandMaster"), // "Pricing Band",
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            bodyStyle: 'padding:5px;',
            buttonAlign: 'right',
            width: 480,
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
                id: 'savePricingBandBtn',
                scope: this,
                handler: function(button) {
                    var itemName = Wtf.getCmp("pricingBandName").getValue();
                    var isDefaultToPOS = this.isDefaultToPOS.getValue();
                    var isIncludingGst = this.isIncludingGSTMain.getValue();
                    if (this.addMasterItemForm.form.isValid()) {
                        this.saveMasterGroupItem("ok", itemName, isEdit, (isEdit? this.rec.data.id : ""), isDefaultToPOS, isIncludingGst, isCopy);
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

        this.addMasterItemWindow.show();
    },
    
    saveMasterGroupItem: function(btn, txt, isEdit, id, isDefaultToPOS, isIncludingGst, isCopy) {
        var callUrl = "ACCMaster/savePricingBand.do";
        if (btn == "ok") {
            if (txt.replace(/\s+/g, '') != "") {
                Wtf.getCmp("savePricingBandBtn").disable();
                Wtf.Ajax.requestEx({
                    url: callUrl,
                    params: {
                        mode: 114,
                        id: id,
                        name: txt,
                        isDefaultToPOS: isDefaultToPOS,
                        isEdit: isEdit,
                        isIncludingGst: isIncludingGst,
                        isCopy: isCopy
                    }
                },this, this.genSuccessResponse, this.genFailureResponse);
            } else {
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.field.pricingBandMaster"),
                    msg: WtfGlobal.getLocaleText("acc.priceListBand.validationMsg"),
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.INFO,
                    width: 300,
                    scope: this
                });

            }
        }
    },
    
    genSuccessResponse: function(response) {
        if (response.success) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.MasterConfiguration"), response.msg], response.success*2+1);
//            (function() {
            this.pricingBandMasterStore.reload();
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
            Wtf.getCmp("savePricingBandBtn").enable();
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
        this.rec = this.pricingBandMasterGrid.getSelectionModel().getSelected();
        
        /**
         * ERP-39166 - changed name because in exposrtinterface.js, search string value is 
         * get as obj.quickSearchTF.getValue().
         */
        this.quickSearchTF = new Wtf.KWLTagSearch({
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
        
        this.bandNameCombo = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.bandName") + "*", // "Band Name" + "*"
            id: "bandNameCombo" + this.id,
            store: this.pricingBandMasterStore,
            displayField: 'name',
            valueField: 'id',
            emptyText: WtfGlobal.getLocaleText("acc.field.selectBandName"), // 'Select Band Name',
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
                        var isIncludingGst = record.data.isincludinggst;
                        this.isIncludingGST.setValue(isIncludingGst);
                    },
                    scope: this
                }
            }
        });
        this.bandNameCombo.setValue(this.rec.data.id);
        
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
                    pricingBandMasterID: this.bandNameCombo.getValue(),
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
                    pricingBandMasterID: this.bandNameCombo.getValue(),
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
            width: 240,
            allowBlank: false
        });
        
        this.applicableDate.on('change',this.onDateChange,this);
        this.applicableDate.on('render', function(c) {
            Wtf.QuickTips.register({
                target: c.getEl(),
                text: "Price for Products will be displayed on selection of Applicable Date"
            });
        }, this);
        
        this.isIncludingGST = new Wtf.form.Checkbox({
            name:'isIncludingGST',
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingGST") +"'>"+ WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingGST")  +"</span>",
            style: 'margin-top: 10px;',
            width: 10,
            disabled:true,
            hidden : (Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA || Wtf.account.companyAccountPref.avalaraIntegration) ? true : false,
            hideLabel : (Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA || Wtf.account.companyAccountPref.avalaraIntegration) ? true : false,
            checked:this.rec.data.isincludinggst
        })
        
        this.exportButton = new Wtf.exportButton({
            obj: this,
            id: "exportReports" + this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), // 'Export report details',
            disabled: true,
            scope: this,
            menuItem: {
                csv: true,
                pdf: true,
                rowPdf: false,
                xls: true
            },
            params: {
                pricingBandMasterID: (this.bandNameCombo.getValue()!="")? this.bandNameCombo.getValue() : this.rec.data.id,
                currencyID: (this.Currency.getValue()!="")? this.Currency.getValue() : Wtf.account.companyAccountPref.currencyid,
                applicableDate: WtfGlobal.convertToGenericDate(this.applicableDate.getValue())
            },
            filename: WtfGlobal.getLocaleText("acc.field.pricingBands") + "_v1",
            get: Wtf.autoNum.pricingBand
        });
        
          
        this.POSSyncBttn=new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.field.DataSyncToPOS"),
            scope: this,
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.field.DataSyncToPOS")
            },
             text: WtfGlobal.getLocaleText("acc.field.DataSyncToPOS"), // "Data Sync To POS",
            tooltip: WtfGlobal.getLocaleText("acc.field.DataSyncToPOS"),
            scope: this,
            hidden:!(!Wtf.account.companyAccountPref.standalone && Wtf.account.companyAccountPref.integrationWithPOS),
            iconCls: getButtonIconCls(Wtf.etype.sync),
            handler: this.syncPriceListBandPrice.createDelegate(this,this.isOnlySelected=[true])

            
        });
        this.setPriceRec = new Wtf.data.Record.create([
            {name: 'productUUID'},
            {name: 'productName'},
            {name: 'productID'},
            {name: 'pricingBandMasterName'},
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
            {name: 'purchasePriceSAR', defaultValue: 0},
            {name: 'purchasePriceZAR', defaultValue: 0},
            {name: 'purchasePriceSEK', defaultValue: 0},
            {name: 'purchasePriceBDT', defaultValue: 0},
            {name: 'purchasePriceMMK', defaultValue: 0},
            {name: 'purchasePriceNGN', defaultValue: 0},
            {name: 'salesPriceOMR', defaultValue: 0},
            {name: 'salesPriceSAR', defaultValue: 0},
            {name: 'salesPriceZAR', defaultValue: 0},
            {name: 'salesPriceSEK', defaultValue: 0},
            {name: 'salesPriceBDT', defaultValue: 0},
            {name: 'salesPriceMMK', defaultValue: 0},
            {name: 'salesPriceNGN', defaultValue: 0},
            {name: 'discountmasterCNH'},
            {name: 'discountmasterUSD'},
            {name: 'discountmasterCAD'},
            {name: 'discountmasterAUD'},
            {name: 'discountmasterCNY'},
            {name: 'discountmasterIDR'},
            {name: 'discountmasterTWD'},
            {name: 'discountmasterTHB'},
            {name: 'discountmasterPHP'},
            {name: 'discountmasterNZD'},
            {name: 'discountmasterCHF'},
            {name: 'discountmasterGBP'},
            {name: 'discountmasterEUR'},
            {name: 'discountmasterINR'},
            {name: 'discountmasterSGD'},
            {name: 'discountmasterMYR'},
            {name: 'discountmasterCRC'},
            {name: 'discountmasterUGX'},
            {name: 'discountmasterKRW'},
            {name: 'discountmasterAED'},
            {name: 'discountmasterBND'},
            {name: 'discountmasterHKD'},
            {name: 'discountmasterJPY'},
            {name: 'discountmasterVND'},
            {name: 'discountmasterOMR'},
            {name: 'discountmasterCNH'},
            {name: 'discountmasterSAR'},
            {name: 'discountmasterZAR'},
            {name: 'discountmasterSEK'},
            {name: 'discountmasterBDT'},
            {name: 'discountmasterMMK'},
            {name: 'discountmasterNGN'}
        ]);
        
        this.setPriceStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            }, this.setPriceRec),
            url: "ACCMaster/getPricingBandMasterDetails.do"
        });
        
        this.discountMasterMultiSelect = new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            forceSelection: true
        }, {
            mode: 'remote',
            store: this.discountMasterStore,
            valueField: 'discountid',
            displayField: 'discountname',
            editable: true,
            triggerAction: 'all'
        }));
        
//        this.discountMasterMultiSelect = new Wtf.common.Select(Wtf.apply({
//            multiSelect: true,
//            forceSelection: true,
//            name: 'discountMasterId',
//            store: this.discountMasterStore,
//            valueField: 'discountid',
//            displayField: 'discountname',
//            editable: true,
//            triggerAction: 'all'
//        }));
        this.setPriceStore.on('beforeload', function() {
            var currentBaseParams = this.setPriceStore.baseParams;
            currentBaseParams.pricingBandMasterID = (this.bandNameCombo.getValue()!="")? this.bandNameCombo.getValue() : this.rec.data.id;
            currentBaseParams.currencyID = (this.Currency.getValue()!="")? this.Currency.getValue() : Wtf.account.companyAccountPref.currencyid;
            currentBaseParams.applicableDate = WtfGlobal.convertToGenericDate(this.applicableDate.getValue());
            this.setPriceStore.baseParams = currentBaseParams;
        },this);
        
        this.setPriceStore.on('load', function(store) {
            this.changeColumnCofig();
            
            if (this.setPriceStore.getCount() < 1) {
                this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.grid.getView().refresh();
            }
            this.quickSearchTF.StorageChanged(store);
            this.exportButton.enable();
        }, this);
        
        this.setPriceStore.on('datachanged', function() {
            var p = this.pPSetPrice.combo.value;
            this.quickSearchTF.setPage(p);
        }, this);
        
        this.setPriceStore.load({
            params: {
                start: 0,
                limit: 30
            }
        });
        this.sm= new Wtf.grid.CheckboxSelectionModel({
            // singleSelect:true
        });
        this.setPriceCM = new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(), this.sm, {
            header: WtfGlobal.getLocaleText("acc.contract.product.name"),  // "Product Name",
            dataIndex: 'productName',
            align: 'left',
            width: 220,
            pdfwidth: 150,
            sortable:true,
            renderer: WtfGlobal.deletedRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.product.gridProductID"),  // "Product ID",
            dataIndex: 'productID',
            align: 'left',
            width: 220,
            pdfwidth: 150,
            sortable:true,
            renderer: WtfGlobal.deletedRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.field.bandName"), // "Band Name",
            dataIndex: 'pricingBandMasterName',
            align: 'left',
            width: 220,
            pdfwidth: 150,
            renderer: WtfGlobal.deletedRenderer
        }]);
        
        this.initialColumnCnt = this.setPriceCM.getColumnCount();
        
        this.grid = new Wtf.grid.EditorGridPanel({
            id: 'userActiveDaysGrid'+this.id,
            layout: 'fit',
            store: this.setPriceStore,
            cm: this.setPriceCM,
            sm:this.sm,
            clicksToEdit: 1,
            autoScroll: true,
            height: 310,
            width: 1000,
            border: false,
            loadMask: true,
            cls: 'vline-on',
            viewConfig: {
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            },
            tbar: [this.quickSearchTF, this.resetBttn],
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbarofdetails" + this.id,
                store: this.setPriceStore,
                searchField: this.quickSearchTF,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                plugins: this.pPSetPrice = new Wtf.common.pPageSize({
                    id : "detailspPageSize_" + this.id
                }),
                items: [this.exportButton,this.POSSyncBttn]
            })
        });
        
        this.setPriceOfPricingBandWin = new Wtf.Window({
            title: WtfGlobal.getLocaleText("acc.field.setPriceForPricingBand"),  // "Set Price for Pricing Band",
            resizable: false,
            width: 1100,
            height: 625,
            modal: true,
            layout: 'border',
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            scope: this,
            buttons: [{
                text: WtfGlobal.getLocaleText("acc.msgbox.ok"), // "OK"
                scope: this,
                handler: function() {
                    this.setPriceOfPricingBandWin.close();
                }
            }],
            items: [{
                region: 'north',
                height: 75,
                border: false,
                bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                html: getTopHtml(WtfGlobal.getLocaleText("acc.field.setPriceForPricingBand"),WtfGlobal.getLocaleText("acc.field.setPriceForPricingBandDesc"), "../../images/accounting_image/price-list.gif")
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
                    labelWidth: 150,
                    items:[this.bandNameCombo, this.Currency, this.applicableDate,this.isIncludingGST, {
                        xtype: 'fieldset',
                        width: (Wtf.isIE)?'91%' : '85%',
                        height: '75%',
                        style: 'margin-top: 20px;',
                        title: WtfGlobal.getLocaleText("acc.field.Setprice"),
                        border : false,
                        labelWidth: 150,
                        items: [this.grid]
                    }]
                }]
            }]
        });
            
        this.grid.on('afteredit', this.saveRecord, this);
        
        this.setPriceOfPricingBandWin.show();
    },

    changeColumnCofig: function() {
        var config = this.setPriceCM.config.slice(0, this.initialColumnCnt);
        var currencyIdsArray = this.Currency.getValue().split(",");
        if (currencyIdsArray != undefined && this.Currency.getValue() != "") {
            for (var i=0; i<currencyIdsArray.length; i++) {
                var index = this.currencyStore.find('currencyid',currencyIdsArray[i]);
                var currencyRec = this.currencyStore.getAt(index);
                if (currencyIdsArray[i] == "1") {
                    currencyRec.data.currencyname = "US Dollars (USD)";
                    currencyRec.data.currencycode = "USD";
                }
                if (Wtf.account.companyAccountPref.productPricingOnBands && Wtf.dispalyUnitPriceAmountInPurchase) {// When product Pricing On Bands true and Permission is given for dispaly unit price
                config.push({
                    header: WtfGlobal.getLocaleText("acc.productList.gridPurchasePrice") + " (" + currencyRec.data.currencyname + ")", // "Purchase Price" + "(" + currencyRec.data.currencyname + ")",
                    dataIndex: 'purchasePrice' + currencyRec.data.currencycode,
                    align: 'right',
                    width: 233,
                    pdfwidth: 150,
                    editor: new Wtf.form.NumberField({
                        allowNegative: false,
                        maxLength: 14,
                        decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                    })
                });
                }
                    
                if (Wtf.account.companyAccountPref.productPricingOnBandsForSales && Wtf.dispalyUnitPriceAmountInSales) {// When product Pricing On Bands true and Permission is given for dispaly unit price
                config.push({
                    header: WtfGlobal.getLocaleText("acc.productList.gridSalesPrice") + " (" + currencyRec.data.currencyname + ")",  // "Sales Price" + "(" + currencyRec.data.currencyname + ")",
                    dataIndex: 'salesPrice' + currencyRec.data.currencycode,
                    align: 'right',
                    width: 233,
                    pdfwidth: 150,
                    editor: new Wtf.form.NumberField({
                        allowNegative: false,
                        maxLength: 14,
                        decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                    })
                });
                }
                if (CompanyPreferenceChecks.discountMaster()) {                 //When multiple discount master check is enabled in company preferences settings then only we need to display mapping columns in price band screen
                    config.push({
                        header: WtfGlobal.getLocaleText("acc.field.discount") + " (" + currencyRec.data.currencyname + ")", // "Discount",
                        dataIndex: 'discountmaster' + currencyRec.data.currencycode,
                        align: 'left',
                        width: 220,
                        editor: new Wtf.common.Select(Wtf.apply({
                            multiSelect: true,
                            forceSelection: true
                        }, {
                            name: 'discountMasterId',
                            mode: 'remote',
                            store: this.discountMasterStore,
                            valueField: 'discountid',
                            displayField: 'discountname',
                            editable: true,
                            triggerAction: 'all'
                        })),
                        renderer: Wtf.MulticomboBoxRenderer(this.discountMasterMultiSelect)
                    });
                }
            }
        }
        if (CompanyPreferenceChecks.discountMaster()) {
            this.discountMasterStore.load();
            this.discountMasterStore.reader = new Wtf.data.KwlJsonReader(this.discountMasterStore.reader.meta, this.discountMasterStore.fields.items);
        }
        this.setPriceStore.reader = new Wtf.data.KwlJsonReader(this.setPriceStore.reader.meta, this.setPriceStore.fields.items);
        this.grid.getColumnModel().setConfig(config);
        var newcm = this.grid.getColumnModel();
        this.grid.reconfigure(this.setPriceStore,newcm);
        this.grid.getView().refresh(true);
    },

    handleResetClick: function() {
        if (this.quickSearchTF.getValue()) {
            this.quickSearchTF.reset();
            this.setPriceStore.load({
                params: {
                    start: 0,
                    limit: 30
                }
            });
        }
    },
    
    saveRecord: function(obj) {
        var columnName=obj.field;
        columnName = columnName.substring(0,14);
        var isDiscountMasterUpdated=columnName.localeCompare("discountmaster")==0;
        var params = {
            pricingBandMasterID: this.bandNameCombo.getValue(),
            currency: this.Currency.getValue(),
            applicableDate: WtfGlobal.convertToGenericDate(this.applicableDate.getValue()),
            productID: obj.record.data.productUUID,
            productName: obj.record.data.productName,
            pricingBandMasterName: obj.record.data.pricingBandMasterName,
            column_Name: obj.field,
            column_Value: obj.value,
            isDiscountMasterUpdated:isDiscountMasterUpdated                     //passing a flag to indentify wether the discount column is changed for any currency
        }
        
        Wtf.Ajax.requestEx({
            url: "ACCMaster/savePricingBandMasterDetails.do",
            params: params
        }, this, function (response) {
            if (response.success) {
                obj.record.commit();
                this.setPriceStore.reload();
                if(response.msg) {
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
    
    syncPriceListBandPrice: function(isOnlySelected) {
        var productIds="";
        if(isOnlySelected){
            this.isOnlySelected=false;
             this.selected= this.sm.getSelections();
                var cnt = this.selected.length;
                if (cnt > 0) {
                var isFirst=true;
                for(var k=0;k< this.selected.length;k++){
                    if(isFirst ){
                        productIds=this.selected[k].data.productUUID;
                        isFirst=false;
                    }else {
                        productIds+=","+this.selected[k].data.productUUID;
                    }
                }
            }else{
                return;
            }
        }
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"),
            msg: WtfGlobal.getLocaleText("acc.rem.246"),  // "Price List - Band product(s) price will be synchronized with pos. Are you sure you want to synchronize the price?",
            width: 560,
            buttons: Wtf.MessageBox.OKCANCEL,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.QUESTION,
            scope: this,
            fn: function(btn) {
                if (btn != "ok") {
                    return;
                } else {
                    var URL = "ACCMaster/sendProductsPriceToPOS.do";
                    Wtf.Ajax.timeout = 1800000;
                    Wtf.Ajax.requestEx({
                        url: URL,
                        params: {
                            isSyncToPOS: true,
                            productIds:productIds
                        }
                    },this,this.genSyncSuccessResponse,this.genSyncFailureResponse);
                }
            }
        });
    },
    
    genSyncSuccessResponse: function(response) {
        if (response.success) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.pricingBands"),response.msg],response.success*2+1);
        } else {
            var msg = WtfGlobal.getLocaleText("acc.common.msg1"); // "Failed to make connection with Web Server";
            if (response.msg) {
                msg = response.msg;
            }
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        }
    },
    
    genSyncFailureResponse: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); // "Failed to make connection with Web Server";
        if (response.msg) {
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
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