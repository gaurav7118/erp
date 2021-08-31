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
Wtf.account.UomSchemaWindow = function(config) {
    //    this.isCustomer = config.isReceipt;
    //    this.accid = config.accid,
    //    this.mode = config.mode,
    //    this.currencyfilterfortrans = config.currencyfilterfortrans,
    //    this.direction = config.direction,
    //    this.isLifoFifo = config.isLifoFifo,
    //    this.isReceipt = config.isReceipt,
    this.uomid = config.uomid,
    this.uomschematype = config.uomschematype,
    this.uomschematypeid = config.uomschematypeid,
    this.butnArr = new Array();
    this.isSubmitBtnClicked = false;
    this.schemaRec = new Wtf.data.Record.create ([
    {
        name:'uomid'
    },

    {
        name:'uomname'
    },

    {
        name:'stockuomname'
    },

    {
        name:'uomschematype'
    }
    ]);


    this.schemaTypeStore =  new Wtf.data.Store({
        url : "ACCUoM/getUOMType.do",
        baseParams:{
            nondeleted:true
        },
        reader: new  Wtf.data.KwlJsonReader({
            root: "data",
            autoLoad:false
        },this.schemaRec)
    });
    
    this.schemaTypeStore.load();
    
    this.schemaName=new Wtf.form.TextField({
        fieldLabel:WtfGlobal.getLocaleText("acc.schema.name"),       //((isEdit ? WtfGlobal.getLocaleText("acc.common.edit") + ' ' : WtfGlobal.getLocaleText("acc.masterConfig.common.enterNew") + ' ') ),
        name: "uomschematype",
        //      msgTarget: 'under',
        //      style: "margin-left:30px;",
        width: 200,
        maxLength:500,
        //      validator:(!this.paidToFlag)?Wtf.ValidateCustomColumnName:"",
        validator:Wtf.ValidatePaidReceiveName,
        disabled :true,
        value:this.uomschematype!=undefined?this.uomschematype:this.uomschematype,                       //(isEdit)?rec.data['name']:'',
        allowBlank: false
    //                        id: "uomschematype"
    })
    this.StockUomCombo =new Wtf.form.FnComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.UOM.StockUOM"),
        hiddenName:'stockuom',
        store:Wtf.uomStore,
        //allowBlank:(this.producttypeval==Wtf.producttype.service) || this.isFixedAsset,
        anchor:'85%',
        valueField:'uomid',
        disabled :true,
        displayField:'uomname',
        forceSelection: true//,
    // addNewFn:this.showUom.createDelegate(this)
    }); 
    
    if(Wtf.uomStore.getCount()>0){
        this.StockUomCombo.setValue(this.uomid);
    }

    Wtf.uomStore.on('load',function(s,o){
        this.StockUomCombo.setValue(this.uomid);
        
    },this);
            
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.submit"), //'Submit',
        scope: this,
        handler: function() {
            this.isSubmitBtnClicked = true;
            this.submitSelectedRecords();
        }
    }, {
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"), //'Cancel',
        scope: this,
        handler: function() {
            this.isSubmitBtnClicked = false;
            this.close();
        }
    });
    Wtf.apply(this, {
        buttons: this.butnArr
    }, config);
    Wtf.account.UomSchemaWindow.superclass.constructor.call(this, config);  
}
Wtf.extend(Wtf.account.UomSchemaWindow, Wtf.Window, {
    height: 680,
    width: 1000,
    modal: true,
    iconCls : 'pwnd deskeralogoposition',
    onRender: function(config) {
        Wtf.account.UomSchemaWindow.superclass.onRender.call(this, config);
        this.createDisplayGrid();
        var msg = WtfGlobal.getLocaleText("acc.UOM.UomSchemaType") ;
        var note = WtfGlobal.getLocaleText("acc.schema.note");
        var isgrid = true; 
        this.NorthForm=new Wtf.form.FormPanel({
            region : 'center',
            //height:200,
            autoHeight:true,
            border:false,
            defaults:{
                border:false
            },
            split:true,
            layout:'form',
            baseCls:'northFormFormat',
            disabledClass:"newtripcmbss",
            hideMode:'display',
            id:'Northform',  //this.id+
            cls:"visibleDisabled",
            labelWidth:120,
            //        disabled:this.readOnly,
            items:[{
                layout:'column',
                defaults:{
                    border:false
                },
                items:[{
                    layout:'form',
                    columnWidth:0.48,
                    items:[this.purchasegrid]
                },{
                    layout:'form',
                    columnWidth:0.04
                // items:[]
                },{
                    layout:'form',
                    columnWidth:0.48,
                    items:[this.salesgrid]
                }]
            }]
    
        });
    
        if(Wtf.account.companyAccountPref.activateInventoryTab){
            this.SouthForm=new Wtf.form.FormPanel({
                region : 'south',
                //            height:200,
                autoHeight:true,
                border:false,
                defaults:{
                    border:false
                },
                split:true,
                layout:'form',
                baseCls:'northFormFormat',
                disabledClass:"newtripcmbss",
                hideMode:'display',
                id:'Southform',  //this.id+
                cls:"visibleDisabled",
                labelWidth:120,
                //        disabled:this.readOnly,
                items:[{
                    layout:'column',
                    defaults:{
                        border:false
                    },
                    items:[{
                        layout:'form',
                        columnWidth:0.48,
                    items:[this.stockgrid]
                    },{
                        layout:'form',
                        columnWidth:0.04
                    // items:[]
                    },{
                        layout:'form',
                        columnWidth:0.48,
                    items:[this.transfergrid]
                    }]
                }]
    
            });
        }
        this.wrapperNorth = new Wtf.Panel({
            region :'north',
            height:40,
            border:false,
            disabledClass:"newtripcmbss",
            defaults:{
                border:false
            },
            layout:'form',
            baseCls:'northFormFormat',
            //        hideMode:'display',
            // id:'Northform',  //this.id+
            //        cls:"visibleDisabled",
            labelWidth:120,
            //        disabled:this.readOnly,
            items:[{
                layout:'column',
                defaults:{
                    border:false
                },
                items:[{
                    layout:'form',
                    columnWidth:0.58,
                    items:[this.schemaName]
                },{
                    layout:'form',
                    columnWidth:0.38,
                    items:[this.StockUomCombo]
                }]
            }]
        });   
        var uomGridArray= new Array();
        uomGridArray.push(this.wrapperNorth);
        uomGridArray.push(this.NorthForm);
        if(Wtf.account.companyAccountPref.activateInventoryTab) {
            
            uomGridArray.push(this.SouthForm);
        }
        this.add({
            region: 'north',
            height: 65,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(msg,note, "../../images/accounting_image/price-list.gif", isgrid)
        }, this.centerPanel = new Wtf.Panel({
            border: false,
            region: 'center',
            id: 'centerpan', // + this.id,
            autoScroll: true,            
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls: 'bckgroundcolor',
            layout: 'border',
            items:uomGridArray
//            items:[this.wrapperNorth,this.NorthForm,this.SouthForm]
        }));
        
    },
    createDisplayGrid: function() {
       
        chkUomload();
        this.purchaseuomEditor=new Wtf.form.FnComboBox({
            name:'uomname',
            store:Wtf.uomStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
            typeAhead: true,
            selectOnFocus:true,
            valueField:'uomid',
            displayField:'uomname',
            scope:this,
            forceSelection:true
        });  
        this.baseuomEditor=new Wtf.form.FnComboBox({
            name:'uomname',
            store:Wtf.uomStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
            typeAhead: true,
            selectOnFocus:true,
            valueField:'uomid',
            displayField:'uomname',
            scope:this,
            forceSelection:true
        });  
        this.baseuomEditor1=new Wtf.form.FnComboBox({
            name:'uomname',
            store:Wtf.uomStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
            typeAhead: true,
            selectOnFocus:true,
            valueField:'uomid',
            displayField:'uomname',
            scope:this,
            forceSelection:true
        });  
        this.baseuomEditor2=new Wtf.form.FnComboBox({
            name:'uomname',
            store:Wtf.uomStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
            typeAhead: true,
            selectOnFocus:true,
            valueField:'uomid',
            displayField:'uomname',
            scope:this,
            forceSelection:true
        });  
        this.baseuomEditor3=new Wtf.form.FnComboBox({
            name:'uomname',
            store:Wtf.uomStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
            typeAhead: true,
            selectOnFocus:true,
            valueField:'uomid',
            displayField:'uomname',
            scope:this,
            forceSelection:true
        });  
        this.salesuomEditor=new Wtf.form.FnComboBox({
            name:'uomname',
            store:Wtf.uomStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
            typeAhead: true,
            selectOnFocus:true,
            valueField:'uomid',
            displayField:'uomname',
            scope:this,
            forceSelection:true
        });  
        this.stockuomEditor=new Wtf.form.FnComboBox({
            name:'uomname',
            store:Wtf.uomStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
            typeAhead: true,
            selectOnFocus:true,
            valueField:'uomid',
            displayField:'uomname',
            scope:this,
            forceSelection:true
        });  
        this.transferuomEditor=new Wtf.form.FnComboBox({
            name:'uomname',
            store:Wtf.uomStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
            typeAhead: true,
            selectOnFocus:true,
            valueField:'uomid',
            displayField:'uomname',
            scope:this,
            forceSelection:true
        });  
        this.purchasetransQuantity=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength:10,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
        });
        this.salestransQuantity=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength:10,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
        });
        this.stocktransQuantity=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength:10,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
        });
        this.transferQuantity=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength:10,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
        });
        this.editpriceSales=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,    
            maxLength:14
        }); 
        this.editpricePurchase=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,    
            maxLength:14
        }); 
       
        this.rowNo=new Wtf.grid.RowNumberer();
        this.cmPurchase = new Wtf.grid.ColumnModel([this.rowNo,{
            dataIndex:'rowid',
            hidelabel:true,
            hidden:true
        },{
            header: WtfGlobal.getLocaleText("acc.product.gridQty"), //"Quantity"
            dataIndex: 'purchaseuomquantiy',
            width: 30,
            align:'right',
            renderer:this.quantityRenderer,
            //              renderer:this.storeRenderer(this.productComboStore,"productid","uomname"),
            editor:""       //:this.transQuantity
        },{
              
            header: WtfGlobal.getLocaleText("acc.uomgrid.purchaseuom"), //"Purchase UOM"
            dataIndex: 'purchaseuom',
            width: 40,
            align:'left',
            renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.purchaseuomEditor),
            editor:(this.readOnly)?"":this.purchaseuomEditor
        //renderer:WtfGlobal.onlyDateDeletedRenderer
        }, {
            header:"", //WtfGlobal.getLocaleText(""), //"="
            dataIndex: 'equalsign',
            width: 10,
            align:'center'
        //renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
            header: WtfGlobal.getLocaleText("acc.product.gridQty"), //"Quantity"
            dataIndex: 'baseuomrate',
            width: 30,
            align:'right',
            renderer:this.quantityRenderer,
            //             renderer:this.storeRenderer(this.productComboStore,"productid","uomname"),
            editor:this.readOnly?"":this.purchasetransQuantity
        },{
            header: WtfGlobal.getLocaleText("acc.uomgrid.baseuom"), //"Base UOM"
            dataIndex: 'baseuom',
            width: 40,
            align:'left',
            renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.baseuomEditor),
            editor:""
        //renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        //             },{
        //                header: WtfGlobal.getLocaleText("acc.schema.ratePerUom"), //"Rate Per UOM"
        //                dataIndex: 'rateperuom',
        //                width: 40,
        //                align:'right',
        //                renderer:WtfGlobal.withCurrencyUnitPriceRenderer,
        ////             renderer:this.storeRenderer(this.productComboStore,"productid","uomname"),
        //               editor:this.readOnly?"":this.editpriceSales
        },{   
            header: WtfGlobal.getLocaleText("acc.invoice.gridAction"), //"Action",
            align: 'center',
            width: 40,
            dataIndex:"",
            renderer: this.deleteRenderer.createDelegate(this)
        }]);
        this.cmSales = new Wtf.grid.ColumnModel([this.rowNo,{
            dataIndex:'rowid',
            hidelabel:true,
            hidden:true
        },{
            header: WtfGlobal.getLocaleText("acc.product.gridQty"), //"Quantity"
            dataIndex: 'purchaseuomquantiy',
            width: 30,
            align:'right',
            renderer:this.quantityRenderer,
            //            renderer:this.storeRenderer(this.productComboStore,"productid","uomname"),
            editor:""   //(this.readOnly)?:this.transQuantity
        },{
            header: WtfGlobal.getLocaleText("acc.uomgrid.salesuom"), //"Purchase UOM"
            dataIndex: 'salesuom',
            width: 40,
            align:'left',
            renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.salesuomEditor),
            editor:(this.readOnly)?"":this.salesuomEditor
        //renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:"",  // WtfGlobal.getLocaleText("="), //"="
            dataIndex: 'equalsign',
            width: 10,
            align:'center'
        //renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
            header: WtfGlobal.getLocaleText("acc.product.gridQty"), //"Quantity"
            width: 30,
            dataIndex: 'baseuomrate',
            align:'right',
            renderer:this.quantityRenderer,
            //             renderer:this.storeRenderer(this.productComboStore,"productid","uomname"),
            editor:(this.readOnly)?"":this.salestransQuantity
                
        },{
            header: WtfGlobal.getLocaleText("acc.uomgrid.baseuom"), //"Base UOM"
            dataIndex: 'baseuom',
            width: 40,
            align:'left',
            renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.baseuomEditor1),
            editor:""
        //renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol   
        //            },{
        //                header: WtfGlobal.getLocaleText("acc.schema.ratePerUom"), //"Rate Per UOM"
        //                dataIndex: 'rateperuom',
        //                width: 40,
        //                align:'right',
        //                renderer:WtfGlobal.withCurrencyUnitPriceRenderer,
        ////             renderer:this.storeRenderer(this.productComboStore,"productid","uomname"),
        //               editor:this.readOnly?"":this.editpricePurchase
           
        },{
            header: WtfGlobal.getLocaleText("acc.invoice.gridAction"), //"Action",
            align: 'center',
            width: 40,
            dataIndex:"",
            renderer: this.deleteRenderer.createDelegate(this)
        }]);
        this.cmStock = new Wtf.grid.ColumnModel([this.rowNo,{
            dataIndex:'rowid',
            hidelabel:true,
            hidden:true
        },{
            header: WtfGlobal.getLocaleText("acc.product.gridQty"), //"Quantity"
            dataIndex: 'purchaseuomquantiy',
            width: 30,
            align:'right',
            renderer:this.quantityRenderer,
            //            renderer:this.storeRenderer(this.productComboStore,"productid","uomname"),
            editor:""   //(this.readOnly)?:this.transQuantity
        },{
            header: WtfGlobal.getLocaleText("acc.uomgrid.stocksuom"), //"Stock UOM"
            dataIndex: 'orderuom',
            width: 40,
            align:'left',
            //            hidden:true,
            renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.stockuomEditor),
            editor:(this.readOnly)?"":this.stockuomEditor
        //renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:"",  // WtfGlobal.getLocaleText("="), //"="
            dataIndex: 'equalsign',
            width: 10,
            align:'center'
        //renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
            header: WtfGlobal.getLocaleText("acc.product.gridQty"), //"Quantity"
            width: 30,
            dataIndex: 'baseuomrate',
            align:'right',
            renderer:this.quantityRenderer,
            //             renderer:this.storeRenderer(this.productComboStore,"productid","uomname"),
            editor:(this.readOnly)?"":this.stocktransQuantity
                
        },{
            header: WtfGlobal.getLocaleText("acc.uomgrid.baseuom"), //"Base UOM"
            dataIndex: 'baseuom',
            width: 40,
            align:'left',
            renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.baseuomEditor2),
            editor:""
        //renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol   
        //            },{
        //                header: WtfGlobal.getLocaleText("acc.schema.ratePerUom"), //"Rate Per UOM"
        //                dataIndex: 'rateperuom',
        //                width: 40,
        //                align:'right',
        //                renderer:WtfGlobal.withCurrencyUnitPriceRenderer,
        ////             renderer:this.storeRenderer(this.productComboStore,"productid","uomname"),
        //               editor:this.readOnly?"":this.editpricePurchase
           
        },{
            header: WtfGlobal.getLocaleText("acc.invoice.gridAction"), //"Action",
            align: 'center',
            width: 40,
            dataIndex:"",
            renderer: this.deleteRenderer.createDelegate(this)
        }]);
        this.cmTransfer = new Wtf.grid.ColumnModel([this.rowNo,{
            dataIndex:'rowid',
            hidelabel:true,
            hidden:true
        },{
            header: WtfGlobal.getLocaleText("acc.product.gridQty"), //"Quantity"
            dataIndex: 'purchaseuomquantiy',
            width: 30,
            align:'right',
            renderer:this.quantityRenderer,
            //            renderer:this.storeRenderer(this.productComboStore,"productid","uomname"),
            editor:""   //(this.readOnly)?:this.transQuantity
        },{
            header: WtfGlobal.getLocaleText("acc.uomgrid.transfersuom"), //"Transfer UOM"
            dataIndex: 'transferuom',
            width: 40,
            align:'left',
            renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.transferuomEditor),
            editor:(this.readOnly)?"":this.transferuomEditor
        //renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:"",  // WtfGlobal.getLocaleText("="), //"="
            dataIndex: 'equalsign',
            width: 10,
            align:'center'
        //renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
            header: WtfGlobal.getLocaleText("acc.product.gridQty"), //"Quantity"
            width: 30,
            dataIndex: 'baseuomrate',
            align:'right',
            renderer:this.quantityRenderer,
            //             renderer:this.storeRenderer(this.productComboStore,"productid","uomname"),
            editor:(this.readOnly)?"":this.transferQuantity
                
        },{
            header: WtfGlobal.getLocaleText("acc.uomgrid.baseuom"), //"Base UOM"
            dataIndex: 'baseuom',
            width: 40,
            align:'left',
            renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.baseuomEditor3),
            editor:""
        //renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol   
        //            },{
        //                header: WtfGlobal.getLocaleText("acc.schema.ratePerUom"), //"Rate Per UOM"
        //                dataIndex: 'rateperuom',
        //                width: 40,
        //                align:'right',
        //                renderer:WtfGlobal.withCurrencyUnitPriceRenderer,
        ////             renderer:this.storeRenderer(this.productComboStore,"productid","uomname"),
        //               editor:this.readOnly?"":this.editpricePurchase
           
        },{
            header: WtfGlobal.getLocaleText("acc.invoice.gridAction"), //"Action",
            align: 'center',
            width: 40,
            dataIndex:"",
            renderer: this.deleteRenderer.createDelegate(this)
        }]);


        this.Rec = Wtf.data.Record.create([
            
        {
                name: 'rowid'
            },

            {
                name: 'purchaseuomquantiy', 
                defValue:'1'
            },

            {
                name: 'purchaseuom', 
                defValue:this.uomid
            },

            {
                name: 'salesuom', 
                defValue:this.uomid
            },
            {
                name: 'orderuom', 
                defValue:this.uomid
            },
            {
                name: 'transferuom', 
                defValue:this.uomid
            },

            {
                name: 'baseuom', 
                defValue:this.uomid
            },

            {
                name: 'equalsign',
                defValue:'='
            },

            {
                name: 'baseuomrate',
                defValue:'1'
            },

            {
                name: 'rateperuom',
                defValue:'0'
            },
            
            ]);
        this.purshaseStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.Rec),
            url: "ACCUoM/getUOMSchema.do",
            baseParams: {            
                stockuomid:this.uomid ,
                uomschematypeid:this.uomschematypeid,
                uomnature:"Purchase",
                deleted: false,
                nondeleted: true
            }
        });
        this.salesStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.Rec),
            url:"ACCUoM/getUOMSchema.do",
            baseParams: {
                stockuomid:this.uomid ,
                uomschematypeid:this.uomschematypeid,
                uomnature:"Sales",
                deleted: false,
                nondeleted: true
            }
        });
        this.stockStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.Rec),
            url:"ACCUoM/getUOMSchema.do",
            baseParams: {
                stockuomid:this.uomid ,
                uomschematypeid:this.uomschematypeid,
                uomnature:"Stock",
                deleted: false,
                nondeleted: true
            }
        });
        this.tranferStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.Rec),
            url:"ACCUoM/getUOMSchema.do",
            baseParams: {
                stockuomid:this.uomid ,
                uomschematypeid:this.uomschematypeid,
                uomnature:"Transfer",
                deleted: false,
                nondeleted: true
            }
        });
        this.purshaseStore.load();
        this.salesStore.load();
        this.stockStore.load();
        this.tranferStore.load();
        this.purchasegrid = new Wtf.grid.EditorGridPanel({
            title: WtfGlobal.getLocaleText("acc.uom.purchaseschema"),
            store: this.purshaseStore,
            height: 200,
            autoScroll: true,
            cm: this.cmPurchase,
            sm: this.sm,
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.salesgrid = new Wtf.grid.EditorGridPanel({
            title: WtfGlobal.getLocaleText("acc.uom.salesschema"),
            store: this.salesStore,
            height: 200,
            autoScroll: true,
            cm: this.cmSales,
            sm: this.sm,
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.stockgrid = new Wtf.grid.EditorGridPanel({
            title: WtfGlobal.getLocaleText("acc.uom.stockschema"),
            store: this.stockStore,
            height: 200,
            autoScroll: true,
            cm: this.cmStock,
            sm: this.sm,
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.transfergrid = new Wtf.grid.EditorGridPanel({
            title: WtfGlobal.getLocaleText("acc.uom.transferschema"),
            store: this.tranferStore,
            height: 200,
            autoScroll: true,
            cm: this.cmTransfer,
            sm: this.sm,
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.purshaseStore.on('load',function(){
            if(this.purchasegrid.getStore().getCount()== 0){
                this.addBlankRow(this.purchasegrid.getStore());
                this.purchasegrid.getView().refresh();
            }
        },this);
        this.salesStore.on('load',function(){
            if(this.salesgrid.getStore().getCount()== 0){
                this.addBlankRow(this.salesgrid.getStore());
                this.salesgrid.getView().refresh();
            }
        },this);
        this.stockStore.on('load',function(){
            if(this.stockgrid.getStore().getCount()== 0){
                this.addBlankRow(this.stockgrid.getStore());
                this.stockgrid.getView().refresh();
            }
        },this);
        this.tranferStore.on('load',function(){
            if(this.transfergrid.getStore().getCount()== 0){
                this.addBlankRow(this.transfergrid.getStore());
                this.transfergrid.getView().refresh();
            }
        },this);
         
        this.purchasegrid.on('cellclick',this.handleCellClick,this);
        this.salesgrid.on('cellclick',this.handleCellClick,this);
        this.stockgrid.on('cellclick',this.handleCellClick,this);
        this.transfergrid.on('cellclick',this.handleCellClick,this);
           
        this.purchasegrid.on('rowclick',this.handleRowClick,this); 
        this.salesgrid.on('rowclick',this.handleRowClick,this);
        this.stockgrid.on('rowclick',this.handleRowClick,this);
        this.transfergrid.on('rowclick',this.handleRowClick,this);
           
        this.purchasegrid.on('afteredit',this.purchaseupdateRow,this);
        this.salesgrid.on('afteredit',this.salesupdateRow,this);
         
        this.stockgrid.on('afteredit',this.stockupdateRow,this);
        this.transfergrid.on('afteredit',this.transferupdateRow,this);
         
        this.salesgrid.on('beforeedit',function(){
            this.copyStore=this.salesStore;  
        });
        this.StockUomCombo.on('blur',function(){
            //this.purchasegrid.getStore().reload;
            // this.salesgrid.getStore().reload;
            //             this.purchasegrid.getView.refresh();
            //             this.salesgrid.getView.refresh();
            },this);
    //         this.StockUomCombo.on('blur',this.salesupdateRow,this);
    },
    handleCellClick:function(grid, rowIndex, columnIndex, e) {
        this.rowIndex = rowIndex;
    },
    
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
            var store=grid.getStore();
            var total=store.getCount();
            if(rowindex==total-1){
                return;
            }
            Wtf.MessageBox.confirm('Warning', 'Are you sure you want to remove this schema?', function(btn){
                if(btn!="yes") return;
                store.remove(store.getAt(rowindex));
                grid.getView().refresh();
            }, this);
        }
    },
    purchaseupdateRow:function(obj){
        if(obj!=null){
            this.purshaseStore.clearFilter(); // Issue 22189
            var rec=obj.record;
            if(obj.field=="purchaseuom" && obj.record.get('purchaseuom') != this.uomid){
                var uomComboIndex = WtfGlobal.searchRecordIndex(this.purshaseStore, obj.record.get('purchaseuom'), 'purchaseuom');
                var flag=0;
                for(var i=0; i<this.purshaseStore.getCount();i++){
                    if(i!=this.rowIndex && obj.value==this.purshaseStore.data.items[i].data.purchaseuom){
                        flag=1;
                        break;
                    }
                }
                if(flag==1){
                    rec.set("purchaseuom",obj.originalValue);
                    return false;
                }
                if(this.rowIndex==(this.purshaseStore.getCount()-1)){
                    this.addBlankRow(this.purshaseStore);
                }else{
                    return false;  
                }
            //                     if(uomComboIndex >=0){
            //                          this.addBlankRow(this.purshaseStore);
            //                     }else{
            //                         return false;
            //                     }
            }else if(obj.field=="purchaseuom"){
                rec.set("purchaseuom",obj.originalValue);
                return false;
            }
             
            if(obj.field=="baseuomrate"){
                if(obj.record.get('purchaseuom') == this.uomid && obj.value > 1){
                    rec.set("baseuomrate",obj.originalValue);
                    return false;
                }
            }
        // var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
        //            var quantity = obj.record.get("quantity");
        //            quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
        //            if(obj.field=="prdiscount" && (rec.data.discountispercent == 1) && obj.value >100){
        //                
        //                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Discountcannotbegreaterthan100")], 2);
        //                    rec.set("prdiscount",0);
        //            } else {
        //                this.fireEvent('datachanged',this);
        //            }
        //            
        //            if(obj.field=="discountispercent" && obj.value == 1 && (rec.data.prdiscount > 100)){
        //                
        //                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Percentdiscountcannotbegreaterthan100")], 2);
        //                    rec.set("discountispercent",0);
        //            } else {
        //                this.fireEvent('datachanged',this);
        //            }
        //            if(obj.field=="baseuomrate"){
        //                  if(this.isCustomer)
        //                    rec.set("changedQuantity",(rec.data.copybaseuomrate-obj.value)*((rec.data.copyquantity==="")?1:rec.data.copyquantity));
        //                  else
        //                    rec.set("changedQuantity",(obj.value-rec.data.copybaseuomrate)*((rec.data.copyquantity==="")?1:rec.data.copyquantity));
        //                
        //                  var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
        //                  var productuomid = "";
        //                  if(productComboIndex >=0){
        //                      prorec = this.productComboStore.getAt(productComboIndex);
        //                      productuomid = prorec.data.uomid;
        //                      if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
        //                            obj.record.set("baseuomquantity", quantity*obj.value);
        //                      } else {
        //                          obj.record.set("baseuomrate", 1);
        //                      }                      
        //                  }
        //                  this.fireEvent('datachanged',this);
        //            }
        }
    },        
    salesupdateRow:function(obj){
        if(obj!=null){
            // this.salesStore.clearFilter(); // Issue 22189
            var rec=obj.record
            //            var modifiedRow=this.salesStore.getModifiedRecords();
            if(obj.field=="salesuom" && obj.record.get('salesuom') != this.uomid){
                //                var uomComboIndex = WtfGlobal.searchRecordIndex(this.salesStore, obj.record.get('salesuom'), 'salesuom');
                var flag=0;
                for(var i=0; i<this.salesStore.getCount();i++){
                    if(i!=this.rowIndex && obj.value==this.salesStore.data.items[i].data.salesuom){
                        flag=1;
                        break;
                    }
                }
                if(flag==1){
                    rec.set("salesuom",obj.originalValue);
                    return false;
                }
                if(this.rowIndex==(this.salesStore.getCount()-1)){
                    this.addBlankRow(this.salesStore);
                }else{
                    return false;  
                }
            //                     if(uomComboIndex >=0){
            //                          this.addBlankRow(this.salesStore);
            //                     }else{
            //                         return false;
            //                     }
            }else if(obj.field=="salesuom"){
                rec.set("salesuom",obj.originalValue);
                return false;
            }
             
            if(obj.field=="baseuomrate"){
                if(obj.record.get('salesuom') == this.uomid && obj.value > 1){
                    rec.set("baseuomrate",obj.originalValue);
                    return false;
                }
            }
        //            var quantity = obj.record.get("quantity");
        //            quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
        //            if(obj.field=="prdiscount" && (rec.data.discountispercent == 1) && obj.value >100){
        //                
        //                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Discountcannotbegreaterthan100")], 2);
        //                    rec.set("prdiscount",0);
        //            } else {
        //                this.fireEvent('datachanged',this);
        //            }
        //            
        //            if(obj.field=="discountispercent" && obj.value == 1 && (rec.data.prdiscount > 100)){
        //                
        //                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Percentdiscountcannotbegreaterthan100")], 2);
        //                    rec.set("discountispercent",0);
        //            } else {
        //                this.fireEvent('datachanged',this);
        //            }
        //            if(obj.field=="baseuomrate"){
        //                  if(this.isCustomer)
        //                    rec.set("changedQuantity",(rec.data.copybaseuomrate-obj.value)*((rec.data.copyquantity==="")?1:rec.data.copyquantity));
        //                  else
        //                    rec.set("changedQuantity",(obj.value-rec.data.copybaseuomrate)*((rec.data.copyquantity==="")?1:rec.data.copyquantity));
        //                
        //                  var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
        //                  var productuomid = "";
        //                  if(productComboIndex >=0){
        //                      prorec = this.productComboStore.getAt(productComboIndex);
        //                      productuomid = prorec.data.uomid;
        //                      if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
        //                            obj.record.set("baseuomquantity", quantity*obj.value);
        //                      } else {
        //                          obj.record.set("baseuomrate", 1);
        //                      }                      
        //                  }
        //                  this.fireEvent('datachanged',this);
        //            }
        }
    },        
    stockupdateRow:function(obj){
        
        if(obj!=null){
            // this.salesStore.clearFilter(); // Issue 22189
            var rec=obj.record
            //            var modifiedRow=this.salesStore.getModifiedRecords();
            if(obj.field=="orderuom" && obj.record.get('orderuom') != this.uomid){
                //                var uomComboIndex = WtfGlobal.searchRecordIndex(this.salesStore, obj.record.get('salesuom'), 'salesuom');
                var flag=0;
                for(var i=0; i<this.stockStore.getCount();i++){
                    if(i!=this.rowIndex && obj.value==this.stockStore.data.items[i].data.orderuom){
                        flag=1;
                        break;
                    }
                }
                if(flag==1){
                    rec.set("orderuom",obj.originalValue);
                    return false;
                }
                if(this.rowIndex==(this.stockStore.getCount()-1)){
                    this.addBlankRow(this.stockStore);
                }else{
                    return false;  
                }
            //                     if(uomComboIndex >=0){
            //                          this.addBlankRow(this.salesStore);
            //                     }else{
            //                         return false;
            //                     }
            }else if(obj.field=="orderuom"){
                rec.set("orderuom",obj.originalValue);
                return false;
            }
             
            if(obj.field=="baseuomrate"){
                if(obj.record.get('orderuom') == this.uomid && obj.value > 1){
                    rec.set("baseuomrate",obj.originalValue);
                    return false;
                }
            }

        }
    },        
    transferupdateRow:function(obj){
        if(obj!=null){
            // this.salesStore.clearFilter(); // Issue 22189
            var rec=obj.record
            //            var modifiedRow=this.salesStore.getModifiedRecords();
            if(obj.field=="transferuom" && obj.record.get('transferuom') != this.uomid){
                //                var uomComboIndex = WtfGlobal.searchRecordIndex(this.salesStore, obj.record.get('salesuom'), 'salesuom');
                var flag=0;
                for(var i=0; i<this.tranferStore.getCount();i++){
                    if(i!=this.rowIndex && obj.value==this.tranferStore.data.items[i].data.transferuom){
                        flag=1;
                        break;
                    }
                }
                if(flag==1){
                    rec.set("transferuom",obj.originalValue);
                    return false;
                }
                if(this.rowIndex==(this.tranferStore.getCount()-1)){
                    this.addBlankRow(this.tranferStore);
                }else{
                    return false;  
                }
            //                     if(uomComboIndex >=0){
            //                          this.addBlankRow(this.salesStore);
            //                     }else{
            //                         return false;
            //                     }
            }else if(obj.field=="transferuom"){
                rec.set("transferuom",obj.originalValue);
                return false;
            }
             
            if(obj.field=="baseuomrate"){
                if(obj.record.get('transferuom') == this.uomid && obj.value > 1){
                    rec.set("baseuomrate",obj.originalValue);
                    return false;
                }
            }
        }
    },        
    submitSelectedRecords: function() {
        var rec=this.NorthForm.getForm().getValues();
        rec.schemaname=this.schemaName.getValue();
        rec.stockuom=this.StockUomCombo.getValue();
        rec.uomschematypeid=this.uomschematypeid;
        var count = this.purchasegrid.store.getCount();
        if (count == 0) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.15")], 2);
            return;
        }
        count = this.salesgrid.store.getCount();
        if (count == 0) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.15")], 2);
            return;
        }
        if(Wtf.account.companyAccountPref.activateInventoryTab){
            count = this.stockgrid.store.getCount();
            if (count == 0) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.15")], 2);
                return;
            }
            count = this.transfergrid.store.getCount();
            if (count == 0) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.15")], 2);
                return;
            }
        }
        
        var purchasedetail = this.getSelectedRecords(this.purchasegrid);
        if(purchasedetail == undefined || purchasedetail == "[]"){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg12")],2);   //"Product(s) details are not valid."
            return;
        }
        var salesdetail = this.getSelectedRecords(this.salesgrid);
        if(salesdetail == undefined || salesdetail == "[]"){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg12")],2);   //"Product(s) details are not valid."
            return;
        }
        var stockdetail="";
        var transferdetail="";
        if(Wtf.account.companyAccountPref.activateInventoryTab){
            stockdetail = this.getSelectedRecords(this.stockgrid);
            if(stockdetail == undefined || stockdetail == "[]"){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg12")],2);   //"Product(s) details are not valid."
                return;
            }
            transferdetail = this.getSelectedRecords(this.transfergrid);
            if(transferdetail == undefined || transferdetail == "[]"){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg12")],2);   //"Product(s) details are not valid."
                return;
            }
        }
        this.showConfirmAndSave(rec,purchasedetail,salesdetail,stockdetail,transferdetail);
        
    },
    deleteRenderer: function(v, m, rec) {
        return "<div class='" + getButtonIconCls(Wtf.etype.deletegridrow) + "'></div>";
    },
    quantityRenderer:function(val,m,rec){
        if(val == ""){
            return val;
        }else{
            return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        }
    },
    getSelectedRecords: function(grid) {
        var arr = [];
        grid.store.each(function(rec){
            if(rec.data.rowid==undefined){
                rec.data.rowid='';
            }
            arr.push(grid.store.indexOf(rec));     
        });
        //        var count=grid.store.getCount();
        //        for( var i=0;i<count;i++){
        //            arr.push(grid.store.indexOf(i));
        //        }
        var jarray = WtfGlobal.getJSONArray(grid, true, arr);
        return jarray;
    },
    addBlank:function(){
        this.addBlankRow(this.purshaseStore);
        this.addBlankRow(this.salesStore);
        this.addBlankRow(this.stockStore);
        this.addBlankRow(this.tranferStore);
      
    },
    addBlankRow:function(store){
        var Record = store.reader.recordType, f = Record.prototype.fields, fi = f.items, fl = f.length;
        var values = {},blankObj={};
        for (var j = 0; j < fl; j++) {
            f = fi[j];
            if(f.name!='rowid') {
                blankObj[f.name]='';
                if(!Wtf.isEmpty(f.defValue))
                    blankObj[f.name]=f.convert((typeof f.defValue == "function"? f.defValue.call() : f.defValue));
            }
        }
        var newrec = new Record(blankObj);
        newrec.endEdit();
        newrec.commit();
        store.add(newrec);
    //        this.getView().refresh();
    },
    updateRow : function(obj) {
    //        if (this..getCount() > 0 ) {//&& this.userActiveDaysGridStore.getAt(this.userActiveDaysGridStore.getCount()-1).data['moduleid'].length <= 0
    //            return;
    //        }
    //        this.addBlankRow();
    },
    showConfirmAndSave: function(rec,purchasedetail,salesdetail,stockdetail,transferdetail){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),this.EditisAutoCreateDO ? ( this.businessPerson=="Customer" ? WtfGlobal.getLocaleText("acc.invoice.msg16") : WtfGlobal.getLocaleText("acc.invoice.msg19") ):WtfGlobal.getLocaleText("acc.invoice.msg7"),function(btn){
            if(btn!="yes") {
                //                    this.saveOnlyFlag=false;
                return;
            }
            this.finalSave(rec,purchasedetail,salesdetail,stockdetail,transferdetail);     
        },this);
    },
    
    finalSave: function (rec,purchasedetail,salesdetail,stockdetail,transferdetail){
                
      
        rec.purchasedetail=purchasedetail;
        rec.salesdetail=salesdetail;
        rec.stockdetail=stockdetail;
        rec.transferdetail=transferdetail;

        Wtf.Ajax.requestEx({
            url:"ACCUoM/saveUOMSchema.do",  
            params: rec
        },this,this.genSuccessResponse,this.genFailureResponse);
        
    },
    genSuccessResponse:function(response, request){
        if(response.success ) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.setupWizard.note39" ),response.msg],response.success*2+1);
              
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.setupWizard.note39" ),response.msg],response.success*2+1);
        }
        this.close();
    },

    genFailureResponse:function(response){
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    }
});


/****************************************************************************************/
/***************          UOM Schema Report            **********************************/
/****************************************************************************************/


//Wtf.account.UOMschemaMaster=function(config){
////    this.GridRec = Wtf.data.Record.create ([
////    {
////        name:'uomid'
////    },{
////        name:'purchaseuom'
////    },{
////        name:'salesuom'
////    },{
////        name:'baseuomrate'
////    },{
////        name:'uomnature'
////    },{
////        name:'uomname'
////    },{
////        name:'stockuomname'
////    },{
////        name:'uomschematype'
////    }
////    ]);
//    
////    this.startDate=new Wtf.form.DateField({
////        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
////        name:'stdate' + this.id,
////        format:WtfGlobal.getOnlyDateFormat(),
////        value:WtfGlobal.getDates(true)
////    });
////    this.endDate=new Wtf.form.DateField({
////        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
////        format:WtfGlobal.getOnlyDateFormat(),
////        name:'enddate' + this.id,
////        value:WtfGlobal.getDates(false)
////    });
//      
//        this.createNew = new Wtf.Toolbar.Button({
//            text:WtfGlobal.getLocaleText("acc.schema.addschema"),
//            scope:this,
//            tooltip:WtfGlobal.getLocaleText("acc.schema.addschema.tooltip"),
//            iconCls :getButtonIconCls(Wtf.etype.add),
////             disabled :true,
////             hidden:true, 
//            handler:function (){
//                this.AddUOMSchemaType(false);
//             }
//        });
//  
//        this.editButton = new Wtf.Toolbar.Button({
//            text:WtfGlobal.getLocaleText("acc.schema.Editschema"),//'Edit',
//            scope:this,
//            iconCls :getButtonIconCls(Wtf.etype.edit), 
//            tooltip:WtfGlobal.getLocaleText("acc.schema.Editschema.tooltip"),
//             handler:function (){
//               this.AddUOMSchemaType(true);
//            }
//        });
//        
//        this.deleteButton = new Wtf.Toolbar.Button({
//            text:WtfGlobal.getLocaleText("acc.DELETEBUTTON"),//'Delete',
//            scope:this,
//            iconCls :getButtonIconCls(Wtf.etype.menudelete), 
//            tooltip:WtfGlobal.getLocaleText("acc.fixed.asset.delete.selected"),//'Delete to selected record',
//            handler:function (){
//                this.DeleteUOMSchemaType();
//            }
//        });
//     this.gridRec = Wtf.data.Record.create ([
//                {name:'uomid'},
//                {name:'purchaseuom'},
//                {name:'salesuom'},
//                {name:'baseuomrate'},
//                {name:'uomnature'},
//                {name:'uomname'},
//                {name:'stockuomname'},
//                {name:'uomschematype'}
//                
//       ]);
//       
//    this.Store = new Wtf.data.Store({
//        url: "ACCUoM/getUOMType.do",
//        reader: new Wtf.data.KwlJsonReader({
//            root: "data",
//            totalProperty:'count'
//        },this.gridRec)       
//    });
//    
//     this.quickPanelSearch = new Wtf.KWLTagSearch({
//        emptyText:'UOM Name...',//             WtfGlobal.getLocaleText("acc.rem.5")+WtfGlobal.getLocaleText("acc.field.JENumber"),
//        width: 150,
//        //      id:"quickSearch"+config.helpmodeid+config.id,
//        field: 'stockuomname',
//        Store:this.Store
//    });
//    
//    this.pagingToolbar = new Wtf.PagingSearchToolbar({
//        pageSize: 15,
//        id: "pagingtoolbar" + this.id,
//        store: this.Store,
//        searchField: this.quickPanelSearch,
//        displayInfo: true,
//        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
//        plugins: this.pP = new Wtf.common.pPageSize({
//            id : "pPageSize_"+this.id
//            })
//    });
//        
//    this.Store.on('datachanged', function() {
//        var p = this.pP.combo.value;
//        this.quickPanelSearch.setPage(p);
//    }, this);
//       
//      
//       
//       this.Store.on('loadexception', function() {
//           alert("loadexception");
//    }, this);
//     
//    this.Store.on('beforeload', function(){
//        this.Store.baseParams = {
//            ss : this.quickPanelSearch.getValue()
////            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
////            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())
//        }
//        
//    }, this);
//   
//
//    
//    
//     this.sm = new Wtf.grid.CheckboxSelectionModel({
//            singleSelect:true
//        });
//        
//           
//        this.colModel = new Wtf.grid.ColumnModel([this.sm,{
//             header:'Customer Code',
//            dataIndex:'uomname',//dataIndex:'accnamecode',
//            renderer:WtfGlobal.deletedRenderer,
//            sortable: true,
//            pdfwidth:75
//        },{
//            header:WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),  //"warehouse",
//            dataIndex:'stockuomname',//dataIndex:'accnamecode',
//            renderer:WtfGlobal.deletedRenderer,
//            sortable: true,
//            pdfwidth:75
////        },{
////          header:WtfGlobal.getLocaleText("acc.customerList.gridName"),  //" customer Name",
////            dataIndex:'customerName',
////            renderer:WtfGlobal.deletedRenderer,
////            sortable: true,
////            pdfwidth:75
//         }]);
//        
//           this.grid = new Wtf.grid.GridPanel({
//            cm:this.colModel,
//            store:this.Store,
//            sm:this.sm,
//            stripeRows :true,
//            border:false,
//            viewConfig:{
//                emptyText:'<center>'+WtfGlobal.getLocaleText("erp.emptytext.norectodisplay")+'</center>',
//                forceFit:true
//            }
//        });
//        //this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);
//    
//     this.Store.load();
//    
//    
//    this.resetBttn=new Wtf.Toolbar.Button({
//        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
//        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
//        id: 'btnRec' + this.id,
//        scope: this,
//        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
//        disabled :false
//    });
//    this.resetBttn.on('click',this.handleResetClickNew,this);
//   
//                
//    Wtf.apply(this,{
//        items:[{
//            region:'center',
//            layout:'fit',
//            border:false,
//            items:this.grid
//        }],
//        tbar : [this.quickPanelSearch,{   //WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"), this.endDate,'-',
//            text : WtfGlobal.getLocaleText("acc.agedPay.fetch"),
//            iconCls:'accountingbase fetch',
//            scope : this,
//            handler : this.loaddata
//        },'-',this.resetBttn,'-',this.createNew,'-',this.editButton,'-',this.deleteButton],                                    //,'-',this.exportButton, '-', this.printButton
//        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
//            pageSize: 30,
//            id: "pagingtoolbar" + this.id,
//            store: this.Store,
//           // searchField: this.quickPanelSearch,
//            displayInfo: true,
//            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"),
//            plugins: this.pP = new Wtf.common.pPageSize({
//                id : "pPageSize_"+this.id
//                })
//        })
//    });
//    
//    Wtf.account.UOMschemaMaster.superclass.constructor.call(this,config);
//}
//Wtf.extend(Wtf.account.UOMschemaMaster,Wtf.Panel,{
//  
//    hideLoading:function(){
//        Wtf.MessageBox.hide();
//    },
//    loaddata : function(){
//     
//        if (this.Store.baseParams && this.Store.baseParams.searchJson) {
//            this.Store.baseParams.searchJson = "";
//        }
//        this.Store.load({
//            params : {
//                start:0,
//                limit:15
//                            
//            }
//        });
//    },
//    handleResetClickNew:function(){ 
//
//        //this.quickPanelSearch.reset();
////        this.startDate.setValue(WtfGlobal.getDates(true));
////        this.endDate.setValue(WtfGlobal.getDates(false));
//
//        this.Store.load({
//            params: {
//                start:0,
//                limit:this.pP.combo.value
//            }
//        });
//       
//    },
//     AddUOMSchemaType:function (isEdit){
////        var recArray = this.grid.getSelectionModel().getSelections();                
//        if(isEdit && (this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1)){
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
//            return;
//        }
//          var rec = this.grid.getSelectionModel().getSelected();
//
//             chkUomload(); 
//            this.stockUom=new Wtf.form.FnComboBox({
//                name:'uomname',
//                store:Wtf.uomStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
//                typeAhead: true,
//                selectOnFocus:true,
//                fieldLabel:WtfGlobal.getLocaleText("acc.product.stockUoMLabel"),
//                valueField:'uomid',
//                displayField:'uomname',
//                scope:this,
//                forceSelection:true
//            });  
//        
//             this.addUOMSchemaItemForm = new Wtf.form.FormPanel({
//                waitMsgTarget: true,
//                border: false,
//                region: 'center',
//                layout:'form',
//                bodyStyle: "background: transparent;",
//                style: "background: transparent;padding:20px;",
//                labelWidth: 107,
//                items: [{
//                        xtype: 'textfield',
//                        fieldLabel:WtfGlobal.getLocaleText("acc.schema.name"),       //((isEdit ? WtfGlobal.getLocaleText("acc.common.edit") + ' ' : WtfGlobal.getLocaleText("acc.masterConfig.common.enterNew") + ' ') ),
//                        name: "uomschematype",
////                        msgTarget: 'under',
//                        style: "margin-left:30px;",
//                        width: 200,
//                        maxLength:500,
////                        validator:(!this.paidToFlag)?Wtf.ValidateCustomColumnName:"",
//                        validator:Wtf.ValidatePaidReceiveName,
//                        value:(isEdit)?rec.data['name']:'',
//                        allowBlank: false,
//                        id: "uomschematype"
//                    },this.stockUom]
//            });     
//             
////            this.customerComboStore.on('load',function(s,o){
////                if(isEdit){
////                    this.CustomerCombo.setValue(rec.data['customerid']);
////                }
////            },this);
//
//            this.saveLocationBtn = new Wtf.Button({
//                text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
//                scope: this,
//                handler: function(button) {
//                    var schemaName=Wtf.getCmp("uomschematype").getValue();
//                        var stockuomid=this.stockUom.getValue();
//                    if (this.addUOMSchemaItemForm.form.isValid()) {
//                         this.saveMasterGroupItem("ok",schemaName,isEdit,(isEdit?rec.data['id']:""),stockuomid) 
//                    } else {
//                        return false;
//                    }
//                }
//            });
//            
//            this.addUOMSchemaType = new Wtf.Window({
//                modal: true,
//                title: "Add UOM Schema Type",
//                iconCls :getButtonIconCls(Wtf.etype.deskera),
//                bodyStyle: 'padding:5px;',
//                buttonAlign: 'right',
//                width: 425,
////        height: 115,
//                scope: this,
//                items: [{
//                        region: 'center',
//                        border: false,
//                        bodyStyle: 'background:#f1f1f1;font-size:10px;',
//                        autoScroll: true,
//                        items: [this.addUOMSchemaItemForm]
//                    }],
//                buttons: [this.saveLocationBtn, {
//                        text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
//                        scope: this,
//                        handler: function() {
//                            this.addUOMSchemaType.close();
//                        }
//                    }]
//            });
//
//            this.addUOMSchemaType.show();
//
////        }
//    },
//    saveMasterGroupItem: function(btn, schemaName, isEdit, rowid,stockuomid){   
//       if(btn=="ok"){
//           if(schemaName.replace(/\s+/g, '')!=""){
//                this.saveLocationBtn.disable();
//                Wtf.Ajax.requestEx({
////                    url: Wtf.req.account+'CompanyManager.jsp',
//                     url:"ACCUoM/saveUOMSchemaType.do",
//                    params: {
//                        schemaName:schemaName,
//                        rowid:rowid,
//                        stockuomid:stockuomid,
//                        isEdit:isEdit
//                        
//                    }
//                },this,this.genSuccessResponse,this.genFailureResponse);
//           }
//        }
//   },
//    DeleteUOMSchemaType:function (){
//          if(this.grid.getSelectionModel().hasSelection()){
//           var arrID=[];
//           var arrName=[];
//           var rec = this.grid.getSelectionModel().getSelections();
//           for(var i=0;i<this.grid.getSelectionModel().getCount();i++){
//                arrID.push(rec[i].data['id']);
//                arrName.push(rec[i].data['name'])
//           }
//       }
//       
//        
//
//        Wtf.MessageBox.show({
//           title: WtfGlobal.getLocaleText("acc.common.warning"),  //"Warning",
//           msg: WtfGlobal.getLocaleText("acc.masterConfig.msg1"),  ///+"<div><b>"+WtfGlobal.getLocaleText("acc.masterConfig.msg1")+"</b></div>",
//           width: 380,
//           buttons: Wtf.MessageBox.OKCANCEL,
//           animEl: 'upbtn',
//           icon: Wtf.MessageBox.QUESTION,
//           scope:this,
//           fn:function(btn){
//               if(btn=="ok"){
//                    Wtf.Ajax.requestEx({
//                        url:"ACCUoM/deleteUOMSchemaType.do",
//                        params: {
//                                ids:arrID,
//                                name:arrName
//                        }
//                    },this,this.genSuccessResponse,this.genFailureResponse);
//                }
//              
//            }
//
//        });
//    },
//     genSuccessResponse:function(response){
//        Wtf.Ajax.timeout = 30000;
//        if(response.success){
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.MasterConfiguration"),response.msg],response.success*2+1);
//            if(response.success){
//                (function(){
////                    this.gridStore.reload();                
//                }).defer(WtfGlobal.gridReloadDelay(),this);
//            }
//             if(this.addUOMSchemaType) 
//            this.addUOMSchemaType.close();
//        }
//        else{
//            if(response.isused){
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg]);
//            }
//        }        
//    },
//    genFailureResponse:function(response){
//        Wtf.Ajax.timeout = 30000;
//        if(response.msg)
//            var msg=response.msg;
//        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
//    }
//   
//});


