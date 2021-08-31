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
Wtf.account.Inventory = function(config){
     Wtf.apply(this,{
         buttons: [{
                    text: WtfGlobal.getLocaleText("acc.het.108"),
                    scope: this,
                    //iconCls:'save',
                    handler: this.saveInventoryForm.createDelegate(this)
                  },{
                    text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),
                    scope: this,
                    handler: function(){ this.close();}
         }]
    },config);
    Wtf.account.Inventory.superclass.constructor.call(this, config);
     this.addEvents({
                'update':true
            });

}
Wtf.extend(  Wtf.account.Inventory, Wtf.Window, {
    onRender: function(config){
        Wtf.account.Inventory.superclass.onRender.call(this, config);
        this.createProductCombo();
        this.createFields();
        this.createForm();
        this.calculateAmount();
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml('Inventory','Inventory')
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.InventoryForm
        });
        this.rate.on('change',this.calculateAmount,this);//function(rate,val){alert(val)})
        this.quantity.on('change',this.calculateAmount,this);
    },
    calculateAmount:function(){
        this.amount.setValue(this.rate.getValue()*this.quantity.getValue());
    },
    createProductCombo:function(){
        this.CategoryRec = Wtf.data.Record.create ([
            {name:'categoryid'},
            {name:'categoryname'}
       ]);
       this.CategoryStore=new Wtf.data.Store({
           url: Wtf.req.account+'CompanyManager.jsp',
            baseParams:{
                 mode:71
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.CategoryRec)
       });
       this.CategoryStore.load();
       this.productRec=new Wtf.data.Record.create([
            {name:'productname'},
            {name:'categoryid'},
            {name:'categoryname'},
            {name:'specification'},
            {name:'productid'}
        ]);
        this.prodStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productRec),
            url: Wtf.req.account+'CompanyManager.jsp',
            baseParams:{
                mode:104
            }
        });
        this.specRec = Wtf.data.Record.create ([
            {name:'productname'},
            {name:'categoryid'},
            {name:'categoryname'},
            {name:'specification'},
            {name:'productid'}
        ]);
        this.specStore = new Wtf.data.Store({
            url:Wtf.req.account+'CompanyManager.jsp',
            baseParams:{
                mode:105
            },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.specRec)
    });
    this.specStore.load();
    },
    createFields:function(){
         this.productName= new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.productList.gridProduct"),
            hiddenName:'productid',
            store: this.prodStore,
            valueField:'productname',
            displayField:'productname',
            mode: 'local',
            allowBlank:false,
            triggerAction: 'all',
            editable : false,
            listeners:{
                scope:this,
                'select' :this.loadSpecificationStore.createDelegate(this)
            }
        });
        this.category= new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.category"),
            hiddenName:'categoryid',
            store:this.CategoryStore,
            valueField:'categoryid',
            displayField:'categoryname',
            mode: 'local',
            allowBlank:false,
            triggerAction: 'all',
            editable : false,
            listeners:{
                scope:this,
                'select' :this.loadProdStore.createDelegate(this) 
            }
        });
        this.specification =new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Specification"),
            hiddenName:'specification',
            name:'specification',
            store:this.specStore,
            valueField:'specification',
            displayField:'specification',
            mode: 'local',
            allowBlank:false,
            triggerAction:'all',
            forceSelection:true,
            editable : false
        });
        this.quantity= new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.gridQty"),
            name: 'quantity',
            allowBlank:false,
            maxLength:4,
            id:'quantity',
            xtype:'numberfield',
            vtype:'alphanum'
        });
        this.rate= new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Rate"),
            name: 'rate',
            allowBlank:false,            
            id:'rate',
            xtype:'numberfield',
            vtype:'alphanum',
            maxLength:8,
            decimalPrecision:2
        });
        this.amount= new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.dnList.gridAmt"),
            name: 'amount',
            id:'amount',
            readOnly :true,
            xtype:'numberfield',
            vtype:'alphanum'
        });
        this.remark= new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.invoice.gridRemark"),
            name: 'description'
        });
    },
    createForm:function(){
        this.InventoryForm=new Wtf.form.FormPanel({
            region:'center',
            width:'400',
            height:'250',
            border:false,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
            defaultType: 'textfield',
            defaults:{
                width:200
            },
            items:[{xtype:'hidden',name:'inventoryid'},this.category,this.productName,this.specification,this.quantity,this.rate,this.amount,this.remark]
        }); 

    },
    saveInventoryForm:function(){
         if(!this.InventoryForm.getForm().isValid()){
                 WtfComMsgBox(2,2);
         }else{
                var rec=this.InventoryForm.getForm().getValues();
                rec.productid=this.specStore.getAt(this.specStore.find('specification',this.specification.getValue())).data['productid'];
                rec.mode=41;
                Wtf.Ajax.requestEx({
                url: Wtf.req.account+'CompanyManager.jsp',
                params: rec
           },this,this.genSuccessResponse,this.genFailureResponse);
         }

        },
    genSuccessResponse:function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.invoice.inventory"),response.msg],response.success*2+1);
        if(response.success) this.fireEvent('update',this);
        this.close();
    }, 
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.field.FailedtomakeconnectionwithWebServer");
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    },
    loadSpecificationStore:function(v){
         this.specStore.load({
            params:{
                productid:v.getValue(),
                productcategoryid:this.category.getValue()
            }});
        this.specification.setValue("");
    },
    loadProdStore:function(v){
        this.prodStore.load({
            params:{
                productcategoryid:v.getValue()
            }
        });
        this.specification.setValue("");
        this.productName.setValue("");
    }
}); 