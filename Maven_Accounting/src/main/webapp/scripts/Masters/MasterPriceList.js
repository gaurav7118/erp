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
Wtf.account.MasterPricelistWindow = function(config){
    this.disablePriceType=(config.carryIn!=null);
    this.record = config.record;
    this.ispropagatetochildcompanyflag=false;
    this.isEdit = (config.isEdit != undefined && config.isEdit != null) ? config.isEdit : false;
    this.priceRec = config.priceRec;
    
    Wtf.apply(this,{
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
            scope: this,
            handler: this.confirmBeforeSave
        },{
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
            scope: this,
            handler: function(){
                this.close();
            }
        }]
    },config);
    Wtf.account.MasterPricelistWindow.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true,
        'loadingcomplete':true
    });
}

Wtf.extend(Wtf.account.MasterPricelistWindow, Wtf.Window, {
    carryIn:true,
    applyDate:null,
    loadRecord:function(){
        if (this.priceRec) {
            this.PricelistForm.getForm().loadRecord(this.priceRec);
        }
        this.cmbCarryIn.fireEvent("select", this.cmbCarryIn);
        if(this.applyDate != null || this.applyDate != undefined) {
            this.applydate.setValue(this.applyDate);
        }
        	
    },
    onRender: function(config){
        Wtf.account.PricelistWindow.superclass.onRender.call(this, config);
        this.createCombo();
        this.createFields();
        this.createForm();
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(WtfGlobal.getLocaleText("acc.rem.67"), (this.isEdit ? WtfGlobal.getLocaleText("acc.field.editSpecialPricefor") : WtfGlobal.getLocaleText("acc.field.SetSpecialPricefor")) +'<b>'+this.pricePersonType+'</b>',"../../images/accounting_image/price-list.gif")
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.PricelistForm
        });   
        
    },

    createCombo:function(){
        this.productRec = Wtf.data.Record.create ([
        {
            name:'productid'
        },

        {
            name:'productname'
        },

        {
            name:'pid'
        },
        {
            name:'type'
        }, 
        {
            name:'desc'
        },

        {
            name:'uomid'
        },

        {
            name:'uomname'
        },

        {
            name:'parentid'
        },

        {
            name:'parentname'
        },

        {
            name:'purchaseaccountid'
        },

        {
            name:'salesaccountid'
        },

        {
            name:'purchaseretaccountid'
        },

        {
            name:'salesretaccountid'
        },

        {
            name:'reorderquantity'
        },

        {
            name:'quantity'
        },

        {
            name:'reorderlevel'
        },

        {
            name:'leadtime'
        },

        {
            name:'purchaseprice'
        },

        {
            name:'saleprice'
        },

        {
            name: 'leaf'
        },

        {
            name: 'level'
        },

        {
            name: 'producttype'
        }
        ]);

        this.productStore = new Wtf.data.Store({
            //        url:Wtf.req.account+'CompanyManager.jsp',
//            url:"ACCProduct/getProductsForCombo.do",
            url:"ACCProductCMN/getProductsIdNameforCombo.do",
            baseParams:{
                mode:22,
                excludeParent:true,
                isSettingNewPrice:true,
                isSalesPrice:false//Should not show non sales product if sales price is selected - 
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productRec)
        });
        var baseParams = this.productStore.baseParams;
        var config = {
            fieldLabel: WtfGlobal.getLocaleText("acc.product.productName"),
            multiSelect: true
        }
        this.productname = CommonERPComponent.createProductMultiselectPagingComboBox(200, 450, 30, this, baseParams, config);
        
        this.productname.on("select",function(rec){
              if(this.pricePersonType!="")
            {
                if(this.pricePersonType=="Customer")
                    this.cmbCarryIn.setValue(false);
                else
                    this.cmbCarryIn.setValue(true);
            }  
        },this);
        this.productname.on("change", function (comboField, newValue, OldValue) {
            if (comboField.value != OldValue && comboField.value != undefined) {
                var selectedProducts = comboField.value.split(",");
                this.productStore.clearFilter();
                for (var cnt = 0; cnt < selectedProducts.length; cnt++) {
                    var selectedProductRecord = WtfGlobal.searchRecord(this.productStore, selectedProducts[cnt], "productid");
                    if (!Wtf.isEmpty(selectedProductRecord) && selectedProductRecord != null && selectedProductRecord.data != undefined && selectedProductRecord.data.type.toUpperCase() != "Service".toUpperCase()) {
                        break;
                    }
                }
                if (selectedProducts.length == cnt) {
                    this.UOM.allowBlank = true;
                    this.UOM.setValue("");
                }else{
                    this.UOM.allowBlank = false;
                }
            } else if (comboField.value == "") {
                this.UOM.allowBlank = false;
            }
        }, this);
        this.productStore.on('load', function() {
            if (this.isEdit && this.priceRec) {
                this.productname.setValue(this.priceRec.data.productuuid);
            }
        }, this);
        
        this.uomRec = new Wtf.data.Record.create([
        {
            name: 'uomid'
        },

        {
            name: 'uomname'
        },

        {
            name: 'precision',
            type:'int'
        },

        {
            name: 'uomtype'
        }
        ]);
        this.uomStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.uomRec),
            url: "ACCUoM/getUnitOfMeasure.do",
            baseParams:{
                mode:31
            }
        });
        this.UOM= new Wtf.form.FnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.masterprice.uomname") +"*",
            hiddenName:"uomid",
            id:"uomid"+this.id,
            store: this.uomStore,
            valueField:'uomid',
            displayField:'uomname',
            allowBlank:false,
            hirarchical:true,
            emptyText: WtfGlobal.getLocaleText("acc.msgbox.130"),
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            triggerAction:'all',
            scope: this,
            disabled: (this.isEdit || Wtf.account.companyAccountPref.bandsWithSpecialRateForSales || Wtf.account.companyAccountPref.columnPref.bandsWithSpecialRateForPurchase) ? true : false,
            hidden:Wtf.account.companyAccountPref.bandsWithSpecialRateForSales || Wtf.account.companyAccountPref.columnPref.bandsWithSpecialRateForPurchase,
            hideLabel:Wtf.account.companyAccountPref.bandsWithSpecialRateForSales || Wtf.account.companyAccountPref.columnPref.bandsWithSpecialRateForPurchase
        });
        this.uomStore.load();
        this.UOM.on("select",function(rec){
              if(this.pricePersonType!="")
            {
                if(this.pricePersonType=="Customer")
                    this.cmbCarryIn.setValue(false);
                else
                    this.cmbCarryIn.setValue(true);
            }  
        },this);
    },
    
    createFields:function(){
        var currencyname=Wtf.account.companyAccountPref.productPriceinMultipleCurrency?'':WtfGlobal.getCurrencySymbolForForm();
        this.price= new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.rem.75")+' '+currencyname+'*',
            name: 'price',
            allowBlank:false,
            allowNegative:false,
            decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,
            xtype:'numberfield',
            maxLength:15
        });
//        this.price.on("blur",this.checkZero,this);	// ERP-16804			// Event fired for checking if price entered is "0"
        
        this.applydate= new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.applicableDate"),  //'Applicable Date',
            name: 'applydate',
            //          minValue:new Date().format('Y-m-d'),
            format:WtfGlobal.getOnlyDateFormat(),
//            value: Wtf.serverDate.clearTime(true),
            value: new Date(),
            //          minValue: new Date().clearTime(true),
            allowBlank: false,
            disabled: this.isEdit ? true : false
        });
        
        var carryinStoreDataArr = [];
        if (!Wtf.account.companyAccountPref.productPricingOnBands || Wtf.account.companyAccountPref.columnPref.bandsWithSpecialRateForPurchase || this.isEdit || this.pricePersonType == "") {
            carryinStoreDataArr.push([true,WtfGlobal.getLocaleText("acc.productList.gridPurchasePrice")]);
        }
        if (!Wtf.account.companyAccountPref.productPricingOnBandsForSales || Wtf.account.companyAccountPref.bandsWithSpecialRateForSales || this.isEdit || this.pricePersonType == "") {
            carryinStoreDataArr.push([false,WtfGlobal.getLocaleText("acc.productList.gridSalesPrice")]);
        }
        var carryinStore=new Wtf.data.SimpleStore({
            fields:[{
                name:"id"
            },{
                name:"name"
            }],
            data: carryinStoreDataArr
        });
        this.cmbCarryIn= new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.rem.70") + '*',
            hiddenName:'carryin',
            name: 'carryin',
            store: carryinStore,
            disableKeyFilter:true,
            triggerAction:'all',
            forceSelection:true,
            typeAhead: true,
            //            value:(this.record.data.producttype == Wtf.producttype.inventoryNonSale)?true:this.carryIn,
            //            disabled:(this.record.data.producttype == Wtf.producttype.inventoryNonSale)?true:false,		
            readOnly:this.disablePriceType,
            valueField:'id',
            displayField:'name',
            mode: 'local',
            allowBlank:false,
            hidden:!(this.pricePersonType==""),
            hideLabel: !(this.pricePersonType == ""),
            disabled: this.isEdit ? true : false
        });
        if(this.pricePersonType!="")
        {
            if(this.pricePersonType=="Customer")
                this.cmbCarryIn.setValue(false);
            else
                this.cmbCarryIn.setValue(true);
            }
        this.cmbCarryIn.fireEvent("select", this.cmbCarryIn);
        
        this.personRec = new Wtf.data.Record.create ([
        {
            name:'accid'
        },{
            name:'accname'
        },{
            name: 'termdays'
        },{
            name: 'billto'
        },{
            name: 'currencysymbol'
        },{
            name: 'currencyname'
        },{
            name: 'currencyid'
        },{
            name:'deleted'
        }
        ]);
        
        this.customerAccStore =  new Wtf.data.Store({
            url:"ACCCustomer/getCustomersForCombo.do",
            baseParams:{
                mode:2,
                group:10,
                deleted:false,
                nondeleted:true,
                common:'1'
            },
            reader: new  Wtf.data.KwlJsonReader({
                root: "data",
                autoLoad:false
            },this.personRec)
        });

        if (!Wtf.account.companyAccountPref.bandsWithSpecialRateForSales || this.isEdit) {
            this.customerAccStore.on("load", this.adddefaultRow, this);
        }
        
        this.customerAccStore.on("load", function() {
            if (this.pricePersonType != "" && this.isEdit && this.priceRec) {
                this.CustomerCMB.setValue(this.priceRec.data.affecteduserid);
            }
        }, this);
        
        this.customerAccStore.load();
        
        this.vendorAccStore =  new Wtf.data.Store({
            url:"ACCVendor/getVendorsForCombo.do",
            baseParams:{
                mode:2,
                group:13,
                deleted:false,
                nondeleted:true,
                common:'1'
            },
            reader: new  Wtf.data.KwlJsonReader({
                root: "data",
                autoLoad:false
            },this.personRec)
        });
        if (!Wtf.account.companyAccountPref.columnPref.bandsWithSpecialRateForPurchase || this.isEdit) {
            this.vendorAccStore.on("load", this.adddefaultRow, this);
        }
        this.vendorAccStore.on("load", function(){
            this.loadRecord();  
            if (this.pricePersonType != "" && this.isEdit && this.priceRec) {
                this.vendorCMB.setValue(this.priceRec.data.affecteduserid);
            }
        }, this);
        
        
        this.vendorAccStore.load();
        
        this.CustomerCMB= new Wtf.form.FnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.invoiceList.cust") + (Wtf.account.companyAccountPref.bandsWithSpecialRateForSales? '*' : ""),
            hiddenName:"customer",
            id:"customer"+this.id,
            store: this.customerAccStore,
            valueField:'accid',
            displayField:'accname',
            allowBlank:false,
            hirarchical:true,
            emptyText: WtfGlobal.getLocaleText("acc.msgbox.11"),
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            anchor:"50%",
            triggerAction:'all',
            scope: this,
            disabled: this.isEdit ? true : false
        });
        this.currencyRec = new Wtf.data.Record.create([
            {name: 'currencyid',mapping:'tocurrencyid'},
            {name: 'symbol'},
            {name: 'currencyname',mapping:'tocurrency'},
            {name: 'exchangerate'},
            {name: 'htmlcode'}
         ]);
         this.currencyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.currencyRec),
            url:"ACCCurrency/getCurrencyExchange.do"
         });
         
         this.currencyStore.load();
        
        this.Currency= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.currency.cur"),  //'Currency',
            hiddenName:'currencyid',
            hidden:!Wtf.account.companyAccountPref.productPriceinMultipleCurrency,
            hideLabel:!Wtf.account.companyAccountPref.productPriceinMultipleCurrency, 
            id:"currency"+this.id,
            width : 240,
            store:this.currencyStore,
            valueField:'currencyid',
            allowBlank : false,
            forceSelection: true,
            displayField:'currencyname',
            scope:this,
            selectOnFocus:true,
            listClass:'tax-combo-list'
        });
//         this.Currency.setValue(WtfGlobal.getCurrencyID());
        this.currencyStore.on("load", function() {
            if (this.isEdit && this.priceRec) {
                this.Currency.setValue(this.priceRec.data.currencyid);
            } else {
                this.Currency.setValue(WtfGlobal.getCurrencyID());
            }
        }, this);
        this.vendorCMB= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.invoiceList.ven"),
            hiddenName:"vendor",
            id:"vendor"+this.id,
            store: this.vendorAccStore,
            valueField:'accid',
            displayField:'accname',
            allowBlank:false,
            hirarchical:true,
            emptyText: WtfGlobal.getLocaleText("acc.msgbox.19"),
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            anchor:"50%",
            triggerAction:'all',
            scope: this,
            disabled: this.isEdit ? true : false
        });
        
        this.applydate.on("render", function(){
            WtfGlobal.hideFormElement(this.vendorCMB);
            WtfGlobal.hideFormElement(this.CustomerCMB);
        }, this);
        
        this.cmbCarryIn.on("select", function(combo, record, index){
              
            var currentBaseParams = this.productname.store.baseParams             /// Flag to Reload the Product Store Depend on Price selected
            currentBaseParams.isSalesPrice = combo.getValue()?false:true;
            this.productname.store.baseParams=currentBaseParams;
            this.productname.store.reload();
            this.productname.clearValue();
            /** 
             * If price list band is activated/Deactivate and person type is empty then don't show vendor and customer combo. ERP-32054
             */
            if (this.pricePersonType == "") {
                this.CustomerCMB.clearValue();
                this.CustomerCMB.allowBlank = true;
                WtfGlobal.hideFormElement(this.CustomerCMB);
                
                this.vendorCMB.clearValue();
                this.vendorCMB.allowBlank = true;
                WtfGlobal.hideFormElement(this.vendorCMB);
            } else if (combo.getValue() == "") {
                if (Wtf.account.companyAccountPref.bandsWithSpecialRateForSales) {
                    WtfGlobal.showFormElement(this.CustomerCMB);
                    this.CustomerCMB.allowBlank = false;
                    
                    this.vendorCMB.clearValue();
                    this.vendorCMB.allowBlank = true;
                    WtfGlobal.hideFormElement(this.vendorCMB);
                } else if (!Wtf.account.companyAccountPref.productPricingOnBandsForSales) {
                    WtfGlobal.showFormElement(this.CustomerCMB);
                    if (!Wtf.isEmpty(this.priceRec) && !Wtf.isEmpty(this.priceRec.data)) {
                        this.CustomerCMB.setValue(this.priceRec.data.affecteduserid);
                    }
//                    this.CustomerCMB.setValue("-1");
                    this.CustomerCMB.allowBlank = false;
                    
                    this.vendorCMB.clearValue();
                    this.vendorCMB.allowBlank = true;
                    WtfGlobal.hideFormElement(this.vendorCMB);
                } else if (!Wtf.account.companyAccountPref.productPricingOnBands) {
                    WtfGlobal.showFormElement(this.vendorCMB);
                    if (!Wtf.isEmpty(this.priceRec) && !Wtf.isEmpty(this.priceRec.data)) {
                        this.vendorCMB.setValue(this.priceRec.data.affecteduserid);
                    }
//                    this.vendorCMB.setValue("-1");
                    this.vendorCMB.allowBlank = false;

                    this.CustomerCMB.clearValue();
                    this.CustomerCMB.allowBlank = true;
                    WtfGlobal.hideFormElement(this.CustomerCMB);
                }
            } else if (combo.getValue()) {
                WtfGlobal.showFormElement(this.vendorCMB);
                this.vendorCMB.allowBlank = false;
                this.vendorCMB.setValue("-1");
                
                this.CustomerCMB.clearValue();
                this.CustomerCMB.allowBlank = true;
                WtfGlobal.hideFormElement(this.CustomerCMB);
            } else {
                WtfGlobal.showFormElement(this.CustomerCMB);
                this.CustomerCMB.setValue("-1");
                this.CustomerCMB.allowBlank = false;
                
                this.vendorCMB.clearValue();
                this.vendorCMB.allowBlank = true;
                WtfGlobal.hideFormElement(this.vendorCMB);
                
            }
        }, this);
        
        
    },
    
    adddefaultRow:function(store, recs, option){
        var rec1 = new this.personRec({
            accid : '-1',
            accname : 'All',
            termdays : 0,
            billto : '-1',
            currencysymbol : '-1',
            currencyname : '-1',
            currencyid : '-1',
            deleted : false
        });
        
        store.insert(0, rec1);
    },
    checkZero:function(){							// Method for checking if price in pricelist form is "0"
        if(this.price.getValue() == '0'){
            Wtf.MessageBox.show({
                title:WtfGlobal.getLocaleText("acc.common.warning"),
                msg:WtfGlobal.getLocaleText("acc.rem.71"), //'Cannot set Purchase price or Sales price as \'0\'',
                buttons:Wtf.Msg.OK
            });
            this.price.setValue("");
        }
    },
    
    createForm:function(){
        this.PricelistForm=new Wtf.form.FormPanel({
            region:'center',
            autoScroll:true,
            border:false,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
            cls: "visibleDisabled",
            defaultType: 'textfield',
            defaults:{
                width:200
            },
            items:[{
                xtype:'hidden',
                name:'priceid'
            },  
            this.cmbCarryIn,this.productname,this.vendorCMB,this.CustomerCMB,this.UOM,this.Currency,this.price,this.applydate]
        });
  
     
    },
     confirmBeforeSave: function () {
        if (Wtf.account.companyAccountPref.propagateToChildCompanies) {

            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.confirm"),
                msg: WtfGlobal.getLocaleText({key: "acc.savecustomer.propagate.confirmmessage", params: [" PriceList "]}),
                buttons: Wtf.MessageBox.YESNO,
                icon: Wtf.MessageBox.QUESTION,
                width: 300,
                scope: {
                    scopeObject: this
                },
                fn: function (btn) {
                    if (btn == "yes") {
                        this.scopeObject.ispropagatetochildcompanyflag = true;
                    }
                    this.scopeObject.saveForm();
                }
            }, this);

        } else {
            this.saveForm();
        }
    }, 
    saveForm:function(){
        if(!this.PricelistForm.getForm().isValid()){
            WtfComMsgBox(2,2);
        }
        else{
            
            //  Check valid Products Selected or Not
            
//            var isInvalidProductsSelected = WtfGlobal.isInvalidProductsSelected(this.productname);
//            if(isInvalidProductsSelected){
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.select.validproduct")],3);
//                return;
//            }
            
            var rec=this.PricelistForm.getForm().getValues();
            rec.mode=11;
            rec.applydate=WtfGlobal.convertToGenericDate(this.applydate.getValue());
            rec.ispropagatetochildcompanyflag = this.ispropagatetochildcompanyflag;
            if (this.isEdit) {
                rec.changeprice = true;
                rec.productid = this.productname.getValue();
                rec.carryin = this.cmbCarryIn.getValue();
                rec.uomid = this.UOM.getValue();
                if (this.pricePersonType != "") {
                    if (this.cmbCarryIn.getValue()) {
                        rec.vendor = this.vendorCMB.getValue();
                    } else {
                        rec.customer = this.CustomerCMB.getValue();
                    }
                } else {
                    if (this.cmbCarryIn.getValue()) {
                        rec.vendor = "";
                    } else {
                        rec.customer = "";
                    }
                }
            }
            rec.isEdit = this.isEdit
            if(rec.productid === "" || rec.productid === null || rec.productid === undefined){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.productPriceWindow.selectproducttosetprice")],2);
                return;
            }
            Wtf.Ajax.requestEx({
                //                url: Wtf.req.account+'CompanyManager.jsp',
                url:"ACCProduct/setNewPrice.do",
                params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);
        }
    },
    genSuccessResponse:function(response){
        if(response.dateexist)
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.currency.update"),(this.cmbCarryIn.getValue()?WtfGlobal.getLocaleText("acc.productList.gridPurchasePrice"):WtfGlobal.getLocaleText("acc.productList.gridSalesPrice"))+ " "+WtfGlobal.getLocaleText("acc.rem.141"),function(btn){
                if(btn!="yes") {
                    return;
                }
                var rec=this.PricelistForm.getForm().getValues();
                if(this.cmbCarryIn.getValue()){
                    rec.carryin = true;
                }
                rec.mode=11;
                rec.changeprice=true;
                rec.applydate=WtfGlobal.convertToGenericDate(this.applydate.getValue());
                if(this.record) {
                    rec.productid = this.record.data.productid
                }
                Wtf.Ajax.requestEx({
                    //                    url: Wtf.req.account+'CompanyManager.jsp',
                    url:"ACCProduct/setNewPrice.do",
                    params: rec
                },this,this.genUpdateSuccessResponse,this.genFailureResponse);
            },this)
        else{
            if (Wtf.getCmp("ProductReport") != undefined) {
                Wtf.getCmp("ProductReport").productStore.on('load', function() {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.rem.72"),response.msg],response.success*2+1);
                }, Wtf.getCmp("ProductReport").productStore, {
                    single: true
                });
            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.rem.72"),response.msg],response.success*2+1);
            }
            
            if(response.success) {
                this.fireEvent('update',this);
//                Wtf.productStore.reload();
//                Wtf.productStoreSales.reload();
            }
            this.close();
        }
    },
    genUpdateSuccessResponse:function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.rem.72"),response.msg],response.success*2+1);
        if(response.success) {
            this.fireEvent('update',this);
//            Wtf.productStore.reload();
//            Wtf.productStoreSales.reload();
        }
        this.close();
    },
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    }
});
