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
   Wtf.account.ProductWindow = function(config){
          this.isEdit=config.isEdit;
    Wtf.apply(this,{
        buttons:[{
            text: WtfGlobal.getLocaleText("acc.het.108"),
            scope: this,
            handler:  this.saveForm.createDelegate(this)
        },{
            text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),
            scope: this,
            handler:this.closeForm.createDelegate(this)
        }]
    },config)

    Wtf.account.ProductWindow.superclass.constructor.call(this, config);
}

Wtf.extend(   Wtf.account.ProductWindow, Wtf.Window, {
    loadRecord:function(){
        if(this.record!=null){
            if(this.record.data['parentid']){
                 this.subproduct.toggleCollapse();
            }
            this.ProductForm.getForm().loadRecord(this.record);
        }
   },
   onRender: function(config){
       Wtf.account.ProductWindow.superclass.onRender.call(this, config);
         this.createStore();
         this.createFields();
         this.createForm()
         this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(WtfGlobal.getLocaleText("acc.productList.gridProduct"),(this.isEdit?WtfGlobal.getLocaleText("acc.common.edit"):WtfGlobal.getLocaleText("acc.field.AddNew"))+WtfGlobal.getLocaleText("acc.product.gridProduct"),"../../images/accounting_image/add-Product.gif")
        }, {
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.ProductForm
        });
        this.addEvents({
            'update':true,
            'cancel':true
        });
    },
    createStore:function(){
         this.uomRec = Wtf.data.Record.create ([
                {name:'uomid'},
                {name:'uomname'},
                {name: 'precision'}
            ]);
            this.uomStore=new Wtf.data.Store({
//                url: Wtf.req.account+'CompanyManager.jsp',
                url: "ACCUoM/getUnitOfMeasure.do",
                baseParams:{
                    mode:31
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                },this.uomRec)
            });


        this.salesAccRec = Wtf.data.Record.create ([
            {name: 'accid'},
            {name: 'accname'}
        ]);
        this.salesAccStore=new Wtf.data.Store({
//            url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                 mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,  
                ignorecustomers:true,  
                ignorevendors:true
             },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.salesAccRec)
       });
       this.salesAccStore.load();

       this.purchaseAccRec = Wtf.data.Record.create ([
            {name: 'accid'},
            {name: 'accname'}

       ]);
       this.purchaseAccStore=new Wtf.data.Store({
//           url: Wtf.req.account+'CompanyManager.jsp',
           url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                 mode:2,
                 ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,  
                ignorecustomers:true,  
                ignorevendors:true
           },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.purchaseAccRec)
       });
       this.purchaseAccStore.load();
       this.parentRec=new Wtf.data.Record.create([{
                name:'parentid',mapping:'productid'
            },{
                name:'parentname',mapping:'productname'
            },{
                name:'leaf',type:'boolean'
            },{
                name:'level',type:'int'
            }]);
       this.parentStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.parentRec),
//            url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCProduct/getProducts.do",
            baseParams:{
                mode:22,
                productid:(this.record!=null?this.record.data['productid']:null)
            }
        });
        this.parentStore.load();
        this.uomStore.on('load',this.loadRecord,this);
        this.uomStore.load();
    },
    createFields:function(){
        this.Pname=new Wtf.form.ExtendedTextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.productName"),
            name: 'productname',
            allowBlank:false,
            maskRe: Wtf.productNameCommaMaskRe,
            maxLength:50
         });
        this.uom= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.uom"),
            hiddenName:'uomid',
            store:this.uomStore,
            allowBlank:false,
            valueField:'uomid',
            displayField:'uomname',
            forceSelection: true,
            addNewFn:this.showUom.createDelegate(this)
        });
        this.rQuantity= new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.reorderQty"),
            name: 'reorderquantity',
            allowBlank:false,
            value:0,
            maxLength:15,
           // allowDecimals:true,
            allowNegative:false
        });
        this.reorderLevel= new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.reorderLevel"),
            name: 'reorderlevel',
            value:0,
            allowBlank:false,
            maxLength:15,
          //  allowDecimals:false,
            allowNegative:false
        });
        this.leadtime= new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.leadTime"),
            name: 'leadtime',
            allowBlank:false,
            maxLength:3,
            value:0,
            allowDecimals:false,
            allowNegative:false,
            maxValue:365
        });

        this.salesAcc=new Wtf.form.FnComboBox({
          fieldLabel:WtfGlobal.getLocaleText("acc.product.salesAcc"),
          store:this.salesAccStore,
            name:'salesaccountid',
            hiddenName:'salesaccountid',
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: false,
            addNewFn: this.addAccount.createDelegate(this,[this.salesAccStore,true,false],true),
            listeners:{
                select:{
                    fn:function(c){
                        if(!this.salesReturnAcc.getValue())
                            this.salesReturnAcc.setValue(c.getValue());
                    },
                    scope:this
                }
            }
        });
        this.salesReturnAcc=new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.salesReturnAcc"),
            store:this.salesAccStore,
            name:'salesretaccountid',
            hiddenName:'salesretaccountid',
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: false,
            addNewFn: this.addAccount.createDelegate(this,[this.salesAccStore,true,false],true)
        });
       this.purchaseAcc=new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.purchaseAcc"),
            store:this.purchaseAccStore,
            name:'purchaseaccountid',
            hiddenName:'purchaseaccountid',
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: false,
            addNewFn: this.addAccount.createDelegate(this,[this.purchaseAccStore,false,true],true),
            listeners:{
                select:{
                    fn:function(c){
                        if(!this.purchaseReturnAcc.getValue())
                            this.purchaseReturnAcc.setValue(c.getValue());
                    },
                    scope:this
                }
            }
       });
       this.purchaseReturnAcc=new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.purchaseReturnAcc"),
            store:this.purchaseAccStore,
            name:'purchaseretaccountid',
            hiddenName:'purchaseretaccountid',
            valueField:'accid',
            forceSelection: true,
            displayField:'accname',
            allowBlank: false,
            addNewFn: this.addAccount.createDelegate(this,[this.purchaseAccStore,false,true],true)
        });
        this.parentname= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.parentProduct"),
            hiddenName:'parentid',
            store: this.parentStore,
            valueField:'parentid',
            displayField:'parentname',
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            hirarchical:true,
            triggerAction: 'all'
        });
        this.description = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.gridDesc"),
            name: 'desc',
            height: 50,
            maxLength:255
        });
        this.subproduct=new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.field.Isasubproduct"),
            checkboxToggle: true,
            autoHeight: true,
            autoWidth: true,
            checkboxName: 'subproduct',
            style: 'margin-right:30px',
            collapsed: true,
            items:[this.parentname]
        });
        this.quantity=new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.initialQty"),
            name:'initialquantity',
            maxLength:15,
            allowNegative :false,
//            allowDecimals:true,
            value:0
        });
        this.initialprice=new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.InitialPrice"),
            name:'initialprice',
            maxLength:15,
            hideLabel:this.isEdit,
            hidden:this.isEdit,
            allowNegative :false,
            allowDecimals:false,
            value:0
        });
        this.subproduct.on("beforeexpand",this.checkParent,this)
    },
    showUom:function(){
       callUOM('uomReportWin');
       Wtf.getCmp('uomReportWin').on('update', function(){
           this.uomStore.reload();
       }, this);
    },
    createForm:function(){
        this.ProductForm=new Wtf.form.FormPanel({
            region:'center',
            width:'420',
            height:'250',
            labelWidth: 150,
            id:'Productfrm',
            border:false,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
            defaultType: 'textfield',
            defaults:{
                width:200
            },
             items:[{xtype:'hidden',name:'productid'},
                this.Pname,
                this.subproduct,
                this.description,
                this.uom,
                this.rQuantity,
                this.reorderLevel,
                this.leadtime,
                this.salesAcc,
                this.salesReturnAcc,
                this.purchaseAcc,
                this.purchaseReturnAcc,
                this.quantity,
                this.initialprice
           ]

       });
    },
    checkParent:function(){
        if(this.parentStore.getCount()==0)
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.product.msg9")],1);
    },
    closeForm:function(){        
        this.fireEvent('cancel',this);
        this.close();    
     },
     saveForm:function(){
        this.parentStore.clearFilter(true)
        var FIND = this.Pname.getValue().trim();
        FIND =FIND.replace(/\s+/g, '');
        var index=this.parentStore.findBy( function(rec){
            var parentname=rec.data['parentname'].trim();
             parentname=parentname.replace(/\s+/g, '');
            if(parentname==FIND)
                return true;
                 else
                return false
            })

            if(index>=0){
                 WtfComMsgBox(36,2);
                 return;
            }
            if(!this.ProductForm.getForm().isValid()){
               WtfComMsgBox(2,2);
        }else{
            if(this.isEdit){
                 var rec=this.ProductForm.getForm().getValues();
                    rec.mode=21;
                    rec.quantity=this.quantity.getValue();
                    Wtf.Ajax.requestEx({
//                        url: Wtf.req.account+'CompanyManager.jsp',
                        url: "ACCProductCMN/saveProduct.do",
                        params: rec
                    },this,this.genSuccessResponse,this.genFailureResponse);
            }else{
              if(this.initialprice.getValue()==0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"),WtfGlobal.getLocaleText("acc.field.Initialpriceoftheproductcannotbezero")],2);
                return;
            }

           Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.field.YoucannotedittheinitialquantityoftheproductAresurewantto"),function(btn){
                if(btn!="yes") { return; }
                WtfComMsgBox(27,4,true);
                var rec=this.ProductForm.getForm().getValues();
                rec.mode=21;
                rec.quantity=this.quantity.getValue();
                Wtf.Ajax.requestEx({
//                    url: Wtf.req.account+'CompanyManager.jsp',
                    url: "ACCProductCMN/saveProduct.do",
                    params: rec
                },this,this.genSuccessResponse,this.genFailureResponse);
              },this)
            }
        }
    }, 

    genSuccessResponse:function(response){
        var pricerec=[];
        WtfComMsgBox(['Product',response.msg],response.success*2+1);          
          if(!this.isEdit){
            pricerec.carryin=true;
            pricerec.productid=response.productID;
            pricerec.price=this.initialprice.getValue();
            pricerec.mode=11;
            pricerec.applydate=WtfGlobal.convertToGenericDate(new Date().clearTime(true));
            Wtf.Ajax.requestEx({
//                url: Wtf.req.account+'CompanyManager.jsp',
                url:"ACCProduct/setNewPrice.do",
                params: pricerec
            },this,this.genPriceSuccessResponse,this.genPriceFailureResponse);
        }
        if(response.success) {
            this.fireEvent('update',this,response.productID);
            Wtf.productStore.reload();
            Wtf.productStoreSales.reload();
        }
        this.close();
    },

    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    },

    addAccount: function(store,issales,ispurchase){
        callCOAWindow(false,null,"coaWin",issales,ispurchase);
        Wtf.getCmp("coaWin").on('update',function(){store.reload();},this);
    },
     genPriceSuccessResponse:function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.rem.72"),response.msg],response.success*2+1);
        if(response.success) this.fireEvent('update',this);
        this.close();
    },
    genPriceFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.field.FailedtomakeconnectionwithWebServer");
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    }
});


