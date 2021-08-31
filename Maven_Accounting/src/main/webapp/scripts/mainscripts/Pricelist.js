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
Wtf.account.PricelistWindow = function(config){
    this.disablePriceType=(config.carryIn!=null);
    this.record = config.record;
    Wtf.apply(this,{
         buttons: [{
                    text: WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
                    scope: this,
                    handler: this.saveForm
                  },{
                    text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
                    scope: this,
                    handler: function(){this.close();}
         }]
    },config);
    Wtf.account.PricelistWindow.superclass.constructor.call(this, config);
     this.addEvents({
        'update':true,
        'loadingcomplete':true
    });
}

Wtf.extend(Wtf.account.PricelistWindow, Wtf.Window, {
    carryIn:true,
    applyDate:null,
    loadRecord:function(){
        this.fireEvent("loadingcomplete",this);
        if(this.record) {
            this.PricelistForm.getForm().loadRecord(this.record);
            if(this.carryIn != undefined) {
                this.cmbCarryIn.fireEvent("select", this.cmbCarryIn);
            }
        }
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
            html: getTopHtml(WtfGlobal.getLocaleText("acc.rem.67"),WtfGlobal.getLocaleText("acc.rem.67")+' for product : <b>'+this.record.data.productname+'</b>',"../../images/accounting_image/price-list.gif")
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
        {name:'productid'},
        {name:'productname'},
        {name:'desc'},
        {name:'uomid'},
        {name:'uomname'},
        {name:'parentid'},
        {name:'parentname'},
        {name:'purchaseaccountid'},
        {name:'salesaccountid'},
        {name:'purchaseretaccountid'},
        {name:'salesretaccountid'},
        {name:'reorderquantity'},
        {name:'quantity'},
        {name:'reorderlevel'},
        {name:'leadtime'},
        {name:'purchaseprice'},
        {name:'saleprice'},
        {name: 'leaf'},
        {name: 'level'},
        {name: 'producttype'}
    ]);

    this.productStore = new Wtf.data.Store({
//        url:Wtf.req.account+'CompanyManager.jsp',
        url:"ACCProduct/getProductsForCombo.do",
        baseParams:{mode:22},
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.productRec)
    });
    
        this.productname= new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.productList.gridProduct") + '*',
            hiddenName:'productid',
            name: 'productid',
            store: this.productStore,
            valueField:'productid',
            displayField:'productname',
            mode: 'local',
            readOnly:true,
            hidden:true,
            hideLabel:true
        });
        
    },
    
    createFields:function(){
        var currencyname=Wtf.account.companyAccountPref.productPriceinMultipleCurrency?'':WtfGlobal.getCurrencySymbolForForm();
        this.price= new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.rem.75") + ' '+currencyname+'*',
            name: 'price',
            allowBlank:false,
            allowNegative:false,
            decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,
            xtype:'numberfield',
            maxLength:15
        });
        this.price.on("blur",this.checkZero,this);				// Event fired for checking if price entered is "0"
        
        this.applydate= new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inventoryList.date"),  //'Date',            
            name: 'applydate',
            //          minValue:new Date().format('Y-m-d'),
            format:WtfGlobal.getOnlyDateFormat(),
            value: Wtf.serverDate.clearTime(true),
            //          minValue: new Date().clearTime(true),
            allowBlank:false
        });
        var carryinStore=new Wtf.data.SimpleStore({
            fields:[{
                name:"id"
            },{
                name:"name"
            }],
            data:[[true,WtfGlobal.getLocaleText("acc.productList.gridPurchasePrice")],[false,WtfGlobal.getLocaleText("acc.productList.gridSalesPrice")]]
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
            value:(this.record.data.producttype == Wtf.producttype.inventoryNonSale)?true:this.carryIn,
            disabled:(this.record.data.producttype == Wtf.producttype.inventoryNonSale)?true:false,		
            readOnly:this.disablePriceType,
            valueField:'id',
            displayField:'name',
            mode: 'local',
            allowBlank:false
        });
        
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
            selectOnFocus:true
        });
//         this.Currency.setValue(WtfGlobal.getCurrencyID());
         this.currencyStore.on("load", function(){
              this.Currency.setValue(WtfGlobal.getCurrencyID());
        }, this);
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

        this.customerAccStore.on("load", this.adddefaultRow, this);
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
        
        this.vendorAccStore.on("load", this.adddefaultRow, this);
        this.vendorAccStore.on("load", function(){
//            this.productStore.on('load',this.loadRecord,this);
//            this.productStore.load();
              this.loadRecord();  
        }, this);
        
        
        this.vendorAccStore.load();
        
        this.CustomerCMB= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.invoiceList.cust"),
            hiddenName:"customer",
            id:"customer"+this.id,
            store: this.customerAccStore,
            valueField:'accid',
            displayField:'accname',
            allowBlank:false,
            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.msgbox.11"),
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            anchor:"50%",
            triggerAction:'all',
            scope:this
        });
        
        this.vendorCMB= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.invoiceList.ven"),
            hiddenName:"vendor",
            id:"vendor"+this.id,
            store: this.vendorAccStore,
            valueField:'accid',
            displayField:'accname',
            allowBlank:false,
            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.msgbox.19"),
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            anchor:"50%",
            triggerAction:'all',
            scope:this
        });
        
        this.applydate.on("render", function(){
            WtfGlobal.hideFormElement(this.vendorCMB);
            WtfGlobal.hideFormElement(this.CustomerCMB);
        }, this);
        
        this.cmbCarryIn.on("select", function(combo, record, index){
            if(combo.getValue()){
                WtfGlobal.showFormElement(this.vendorCMB);
                this.vendorCMB.allowBlank = false;
                if (this.record.data.vendor != "" && this.record.data.vendor != undefined) {
                    this.vendorCMB.setValue(this.record.data.vendor);
                } else {
                    this.vendorCMB.setValue("-1");
                }
                
                this.CustomerCMB.clearValue();
                this.CustomerCMB.allowBlank = true;
                WtfGlobal.hideFormElement(this.CustomerCMB);
            } else {
                WtfGlobal.showFormElement(this.CustomerCMB);
                
                if (this.record.data.customer != "" && this.record.data.customer != undefined) {
                    this.vendorCMB.setValue(this.record.data.customer);
                } else {
                    this.CustomerCMB.setValue("-1");
                }
                
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
            defaultType: 'textfield',
            defaults:{
                width:200
            },
             items:[{xtype:'hidden',name:'priceid'}, 
                   this.cmbCarryIn,this.vendorCMB,this.CustomerCMB,this.Currency,this.price,this.applydate]
       });
  
     
   },
   saveForm:function(){
        if(!this.PricelistForm.getForm().isValid()){
            WtfComMsgBox(2,2);
        }
        else{
            var rec=this.PricelistForm.getForm().getValues();
            if(this.record.data.producttype == Wtf.producttype.inventoryNonSale){
            	rec.carryin = true;
            }
            rec.mode=11;
            rec.applydate=WtfGlobal.convertToGenericDate(this.applydate.getValue());
            if(this.record) {
                rec.productid = this.record.data.productid
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
                if(this.record.data.producttype == Wtf.producttype.inventoryNonSale){
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
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.rem.72"),response.msg],response.success*2+1);
            if(response.success) {
                this.fireEvent('update',this);
                 var panel = Wtf.getCmp("ProductReport");
                 if(panel!= undefined && panel!=null){
                     var grid = Wtf.getCmp("ProductReportGrid_one");
                     if(grid!=null){
                         grid.getStore().reload();
                     }
                 }
                Wtf.productStore.reload();
                Wtf.productStoreSales.reload();
            }
            this.close();
        }
    },
    genUpdateSuccessResponse:function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.rem.72"),response.msg],response.success*2+1);
        if(response.success) {
            this.fireEvent('update',this);
            Wtf.productStore.reload();
            Wtf.productStoreSales.reload();
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
