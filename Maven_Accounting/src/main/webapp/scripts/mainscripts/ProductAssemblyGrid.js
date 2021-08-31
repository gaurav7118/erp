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
//------------------- Assembly Items Grid ----------------------------
// Used in
// 1. Product Add/Edit Form [this.rendermode="productform"]
// 2. Build Product Assembly Form [this.rendermode="buildproduct"]
////
Wtf.account.productAssemblyGrid = function (config){
    this.bodyBorder=config.bodyBorder;
    this.currentQty=config.currentQuantity;
    this.totalQty=config.totalquantity;
    this.isClone=config.isClone;
    this.parent=config.parent;
    this.buildAssemblyObj=config.buildAssemblyObj;
    this.isForCustomerAssembly=config.isForCustomerAssembly,
    this.jobworkorderid=config.jobworkorderid,
    this.bomid=config.bomid;
    this.isUnbuildAssembly = config.isUnbuildAssembly;
    this.mainassemblyproduct = config.mainassemblyproduct;
    this.modifiedStore = config.modifiedStore;
    this.loadFromMetodSetProductWithBOM=false;
    this.originalStore = "";
    this.productType=config.productType;
    this.isUsedInTransaction=config.isUsedInTransaction;
    this.isEdit=config.isEdit;
    this.isNewBOM = false;     //isNewBOM=true - When we are creating New BOM.Otherwise it is false
    this.selectedRow=-1;
    this.isCloseWO=config.isCloseWO;
    //this.refno=config.refno;
    Wtf.apply(this, config);
    this.productOptimizedFlag = Wtf.account.companyAccountPref.productOptimizedFlag;
    Wtf.account.productAssemblyGrid.superclass.constructor.call(this);
}

Wtf.extend(Wtf.account.productAssemblyGrid,Wtf.Panel,{
    initComponent:function (){
        this.addEvents({
            'updatedcost':true,
            'updatedbuilds':true,
            'updatedqty':true
        });

        Wtf.account.productAssemblyGrid.superclass.initComponent.call(this);
        this.maxbuilds = 0;
        this.totalcost = 0;
        this.assemblyProductJson = [];
        this.globalProductCount = Wtf.productStore.getCount();
       this.gridRec = Wtf.data.Record.create ([
            {name:'id'},
            {name:'productid'},
            {name:'productname'},
            {name:'desc'},
            {name:'purchaseprice'},
            {name:'saleprice'},
            {name:'subbomid'},
            {name:'subbomcode'},
            {name:'producttype'},
            {name:'type'},
            {name:'onhand'},
            {name:'quantity'},
            {name:'uomid'},
            {name:'uomname'},
            {name:'actualquantity', defValue: 1},
            {name:'percentage',defValue:100},
            {name:'crate',defValue:0},
            {name:'componentType'},
            {name:'inventoryquantiy',defValue:0},
            {name:'recylequantity',defValue:0},
            {name:'remainingquantity',defValue:0},
            {name:'availablerecylequantity'},
            {name:'lockquantity'},
            {name:'total'},
            {name: 'isLocationForProduct'},
            {name: 'isWarehouseForProduct'},
            {name: 'isBatchForProduct'},
            {name: 'isSerialForProduct'},
            {name: 'isRowForProduct'},
            {name: 'isRackForProduct'},
            {name: 'isBinForProduct'},
            {name: 'wastageInventoryQuantity', defValue: 0},
            {name: 'wastageQuantityType', defValue: 0},
            {name: 'wastageQuantity', defValue: 0},
            {name: 'isWastageApplicable', type: 'boolean'},
            {name: 'isRecyclable', type: 'boolean'},
            {name: 'hasAccess'},
            {name: 'batchdetails'},
            {name: 'orderdetailid'},
            {name: 'workorderid'},
            {name: 'consumptiondetails'},
            {name: 'parentid'},
            {name: 'batchdetailsnew'},
            {name:'location'},
            {name:'warehouse'}
            
        ]);
        this.gridStore = new Wtf.data.Store({
//            url:Wtf.req.account+'CompanyManager.jsp',
            /*
             * change url for now 
             */
            url:this.isFinishGood==undefined?"ACCProduct/getAssemblyItems.do":"ACCWorkOrderCMN/getAssemblyItemsForWO.do",
            baseParams:{mode:25, bomid:(this.bomid!=undefined?this.bomid:""), rendermode:this.rendermode},
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.gridRec)
        });
        this.originalStore = new Wtf.data.Store({     
            recordType: this.gridStore.recordType 
        });

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
            {name:'producttype'},
            {name:'type'},
            {name:'purchaseprice'},
            {name: 'isparentproduct'},
            {name:'isAsset',type:'boolean'},
            {name: 'isStopPurchase',type:'boolean'},
            {name: 'isLocationForProduct'},
            {name: 'isWarehouseForProduct'},
            {name: 'isBatchForProduct'},
            {name: 'isSerialForProduct'},
            {name: 'isRowForProduct'},
            {name: 'isRackForProduct'},
            {name: 'isBinForProduct'},
            {name:'saleprice'},
            {name:'isRecyclable'},
            {name:'recycleQuantity'},
            {name: 'leaf'},
            {name: 'level'},
            {name:'pid'},
            {name: 'isWastageApplicable'},
            {name:'location'},
            {name:'warehouse'},
            {name:'hasAccess'}
                
        ]);
        
        var productStoreBaseParams = {
            mode: 22,
            common: '1',
            loadPrice: true,
            onlyProduct: true,
            excludeParent: true,
            isAssemblyType: true
        }
        
        this.productStore = new Wtf.data.Store({
            url: "ACCProductCMN/getProductsForCombo.do",
            baseParams: {
                mode: 22,
                excludeParent: true,
                loadPrice: true,
                isAssemblyType: true,
                isClone: this.isClone
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.productRec)
        });
        
        if(this.globalProductCount>0){
            this.cloneProductList();
        }
        Wtf.productStore.on("load", this.cloneProductList, this);
        this.bomRec = new Wtf.data.Record.create([
            {name: 'bomid'},
            {name: 'bomCode'},
            {name: 'bomName'}
        ]);
        this.subBOMStore = new Wtf.data.Store({
            url: "ACCProduct/getBOMDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.bomRec)
        });

        this.subbomid = new Wtf.form.ExtFnComboBox({
            name: 'subbomid',
            store: this.subBOMStore,
            valueField: 'bomid',
            displayField: 'bomCode',
            allowBlank: false,
            mode: 'remote',
            typeAhead: true,
            width: 200,
            triggerAction: 'all',
            listWidth: 300,
            extraFields: [],
            addNoneRecord: false
        });
        /*
         
           displayField   valueField
         * Component    =  1
         * Co-Product   =  2
         * Scrap        =  3
         
         */
        var data = [];
        data = [['Component',1], ['Co-Product',2], ['Scrap',3]];
        
      
        this.componentTypeStore = new Wtf.data.SimpleStore({
            fields:['componentType','value'],
            data:data,
            pruneModifiedRecords:true
        });
        
        this.componentTypeCombo = new Wtf.form.FnComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            forceSelection:true,
            store:this.componentTypeStore,
            displayField:"componentType",
            valueField:"value",
            allowBlank:false,
            editable:false,
            width:100           
        });
         this.Rate=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            defaultValue:0,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
        });

        /*
         
         * On selection of Component option ,rate column cell is disabled and value is set to 0.
         * On selection of Co-Product option ,rate column cell gets Editable and Cursor will directly move into that cell.
         * On selection of Scrap option ,rate column cell gets Editable and Cursor will directly move into that cell.
         
         */
        this.componentTypeCombo.on('select',function(combo,record,index){ 
           
            var crateindex=this.gridcm.findColumnIndex("crate");
            if(record.data.componentType=="Component")
            {
                  this.gridcm.setEditable(crateindex,false);
                  this.itemsgrid.store.getAt(this.selectedRow).set('crate',0);
            }
            else
            {              
                  this.gridcm.setEditable(crateindex,true);
                  this.itemsgrid.startEditing(this.selectedRow, crateindex);
            }
    },this);
        
        this.selectedproduct = "";
        this.subBOMStore.on('beforeload', function(s, o) {
            if (!o.params)
                o.params = {};
            var currentBaseParams = this.subBOMStore.baseParams;
            currentBaseParams.productid =this.selectedproduct;
            this.subBOMStore.baseParams = currentBaseParams;
        }, this);
        
        var comboConfig = {
            name: 'productname',
            valueField: 'productid', //productid
            displayField: 'productname',
            extraFields: ['pid', 'type'],
//            displayField:'pid',
//            extraFields:['productname','type'],
            maxHeight:250,
            emptyText: WtfGlobal.getLocaleText("acc.msgbox.17")
        }
        
        if (this.productOptimizedFlag == Wtf.Show_all_Products) {
            comboConfig.ctCls = 'optimizedclass';
            comboConfig.hideTrigger = true;
        }
        
        this.productEditor = CommonERPComponent.createProductPagingComboBox(250, 550, Wtf.ProductCombopageSize, this, productStoreBaseParams, false, false, comboConfig);
        
        this.productEditor.on('beforeselect', function(combo, record, index) {
                return validateSelection(combo, record, index);
        }, this);
        
        this.productEditor.on('select', function (combo, record, index) {
            this.productStore.load({
                params: {
                    ids: record.data.productid
                },
                scope: this
            });
        }, this);
        
       this.productEditor.addNewFn=this.openProductWindow.createDelegate(this);
       this.transDiscount=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            defaultValue:0,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
        });
        
          this.uomEditor=new Wtf.form.FnComboBox({
            hiddenName:'uomname',
            triggerAction:'all',
            mode: 'local',
            lastQuery:'',
            name:'uomname',
            store:Wtf.uomStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
            typeAhead: true,
            selectOnFocus:true,
            valueField:'uomid',
            displayField:'uomname',
            scope:this,
            forceSelection:true
        });

        this.gridcm = new Wtf.grid.ColumnModel([
                new Wtf.grid.RowNumberer(),
                {
                    header:WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),//"Product",//
                    dataIndex:'productid',
                    width:190,
//                    renderer:(this.productOptimizedFlag=== Wtf.Show_all_Products)?this.getComboNameRenderer(this.productEditor):Wtf.comboBoxRenderer(this.productEditor),
//                  renderer:(this.productOptimizedFlag==true)?(this.readOnly?"":this.getComboNameRenderer(this.productEditor)):(this.readOnly?"":Wtf.comboBoxRenderer(this.productEditor)),
                    renderer:this.getComboNameRenderer(this.productEditor),
                    editor:this.readOnly?"":this.productEditor
                },{
                    header:WtfGlobal.getLocaleText("acc.productList.gridProductDescription"),//"Description",//
                    dataIndex:'desc',
                    width:190,
                    renderer : function(val) {
                        return "<div wtf:qtip=\""+val+"\" wtf:qtitle="+WtfGlobal.getLocaleText("acc.product.gridDesc")+">"+val+"</div>";
                    }
                },{
                    header:WtfGlobal.getLocaleText("acc.invoiceList.expand.pType"),//"Type",//
                    dataIndex:'type',
                    width:130,       
                    renderer: function(val){
                        return val;
                    }
                },{
                    header: WtfGlobal.getLocaleText("Type"),/* this field will be display when 1)MRP is Activated 2)Co-Product and Scrap is Activated 3)this.rendermode=="productform"*/
                    dataIndex:'componentType',
                    hidden: !(Wtf.account.companyAccountPref.activateMRPManagementFlag && Wtf.account.companyAccountPref.mrpProductComponentType && this.productForm),
                    editor:this.readOnly?"":this.componentTypeCombo,
                    renderer:Wtf.comboBoxRenderer(this.componentTypeCombo)
                },{
                    header: WtfGlobal.getLocaleText("acc.mrp.field.bomcode"),//"Sub BOM Code",//
                    dataIndex:'subbomcode',
                    hidden: !(Wtf.account.companyAccountPref.activateMRPManagementFlag && this.productForm),
                    renderer:this.bomComboRenderer(this.subbomid),
                    editor:this.readOnly?"":this.subbomid
                },{
                    header:this.rendermode=="productform"?WtfGlobal.getLocaleText("acc.product.initialPurchasePrice"):WtfGlobal.getLocaleText("acc.product.gridCost"),
                    dataIndex:'purchaseprice',
                    align:'right',
                    width:130,
                    renderer: this.currencyRendererWithPermissionCheck
                },{
                    header:WtfGlobal.getLocaleText("acc.product.gridQtyonHand"),//"Quantity On Hand",
                    dataIndex:'onhand',
                    align:'right',
                    hidden: this.readOnly,
                    renderer: function(a,b,c){
                        if(c.data.producttype != Wtf.producttype.service){//Service Item
                            return parseFloat(getRoundofValue(a)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                        }else{
                            return '';
                        }
                    }
                },{
                    header:this.rendermode=="productform"?WtfGlobal.getLocaleText("acc.product.gridQty"):(this.rendermode=="unbuildproduct"?WtfGlobal.getLocaleText("acc.unbuild.bomqty") : WtfGlobal.getLocaleText("acc.product.gridQtyneeded")),//"Quantity" : ("BOM Quantity" : "Quantity Needed"),
                    align:'right',
                    dataIndex:'quantity',
                    editor:this.readOnly?"":new Wtf.form.NumberField({allowNegative:false, allowDecimals:true,decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL}),
                    renderer:function(val){
//                       return parseFloat(val).toFixed(2);
                       return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?" ":parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                    }
                },{
                   header:WtfGlobal.getLocaleText("acc.invoice.gridUOM"),//"UOM",
                   width:130,
                   dataIndex:'uomname',
                   renderer: function(val){
                    if(val!=undefined || val==null){
                        return val;
                    }
                    else{
                        return " ";
                    }
                        
                  }      
               //    renderer:Wtf.comboBoxRendererwithClearFilter(this.uomEditor)
                //editor:(this.isNote||this.readOnly)?"":this.uomEditor  //||this.UomSchemaType==Wtf.PackegingSchema
                },
                {
                    header:WtfGlobal.getLocaleText("Rate (%)"), /* this field will be display when 1)MRP is Activated 2)Co-Product and Scrap is Activated 3)this.rendermode=="productform"*/
                    align:'right',
                    dataIndex:'crate',
                    hidden: !(Wtf.account.companyAccountPref.activateMRPManagementFlag && Wtf.account.companyAccountPref.mrpProductComponentType && this.productForm),
                    renderer:function(v,m,rec){
                        if(rec.data.crate==""||rec.data.crate==undefined) {
                            v= 0 + "%";
                        }else if(v){
                            v= v + "%";
                        }
                    return'<div class="currency">'+v+'</div>';
                    },
                   editor:this.readOnly?"":this.Rate
                },{
                    header:WtfGlobal.getLocaleText("acc.product.Percentage"), //this.rendermode=="productform"?
                    align:'right',
                    dataIndex:'percentage',
                    width:130,
//                    hidden:true,
//                    id:"percentage",
                    //editor:new Wtf.form.NumberField({allowNegative:false, allowDecimals:true,decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL}),
                    renderer:function(v,m,rec){
                        if(rec.data.percentage==""||rec.data.percentage==undefined) {
                            v=100 + "%";
                        }else if(rec.data.percentage){
                            v= v + "%";
                        }
//                        else {
//                            var symbol = WtfGlobal.getCurrencySymbol();
//                            if(rec.data['currencysymbol']!=undefined && rec.data['currencysymbol']!=""){
//                                symbol = rec.data['currencysymbol'];
//                            }
//
//                            v= WtfGlobal.conventInDecimal(v,symbol)
//                        }
                    return'<div class="currency">'+v+'</div>';
                    },
                   editor:this.readOnly||this.isNote?"":this.transDiscount
                },{
                    header:WtfGlobal.getLocaleText("acc.product.ActualQuantiy"),
                    align:'right',
//                    hidden:true,   //!this.rendermode=="productform",
                    width:130,
                    dataIndex:'actualquantity',
//                    id:"actualquantity"
                    //editor:new Wtf.form.NumberField({allowNegative:false, allowDecimals:true,decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL}),
//                    renderer:function(val){
                    renderer:function(a,b,c){
                        var actualQty=1;
                        if(c.data.percentage!="" || c.data.percentage!=undefined && c.data.quantity!="" || c.data.quantity!=undefined ) {
                           actualQty=(c.data.quantity*c.data.percentage)/100;
                        }
                        var actQty=(parseFloat(getRoundofValue(actualQty)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?" ":parseFloat(getRoundofValue(actualQty)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                        c.data.actualquantity=actQty;
//                       return parseFloat(val).toFixed(2);
//                       return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?" ":parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                       return actQty;
                    }
                },{
                    header:WtfGlobal.getLocaleText("acc.product.gridTotal"),//"Total",//
                    align:'right',
                    dataIndex:'total',
                    width:130,
                    renderer: function(a,b,c){
                        if(!Wtf.dispalyUnitPriceAmountInPurchase){// If no permission to show price then this condition will be true
                            return Wtf.UpriceAndAmountDisplayValue;
                        } else if ( Wtf.account.companyAccountPref.activateMRPManagementFlag && Wtf.account.companyAccountPref.mrpProductComponentType && (c.data.componentType == 2 || c.data.componentType == 3)) {
                            return WtfGlobal.currencyRenderer(a);
                        } else if(c.data.quantity != "") {
//                            var price = (c.data.producttype!=Wtf.producttype.service)?c.data.purchaseprice:c.data.saleprice;//service
                            var price = c.data.purchaseprice
                            var percentage = c.data.percentage;
                            a = price * c.data.actualquantity;
                            if(percentage==""||percentage==undefined){
                               percentage=100;
                            }
                            a=(price*((percentage * c.data.quantity)/100))
                            c.data.total = a;
                            return WtfGlobal.currencyRenderer(a);
                        } else {
                            return "";
                        }
                    }
//                
//                },{                                          //column header
//                    header: '',
//                    align:'center',
//                        dataIndex:'addserial1',
//                    renderer: this.serialRenderer.createDelegate(this),
//                    hidden:!(Wtf.account.companyAccountPref.showprodserial),
//                    width:40
      
                },{
                    header:WtfGlobal.getLocaleText("acc.product.gridAddSerial"),//"Action",//
                    align:'center',
                    dataIndex:'addserial',
                    hidden:!this.isHiddenColumn,
                    //width:30,
                    renderer: this.serialRenderer.createDelegate(this)
                },{
                    header:WtfGlobal.getLocaleText("acc.quantity.desc.title"),              //WtfGlobal.getLocaleText("acc.product.gridAddSerial"),//"Action",//
                    align:'center',
                    dataIndex:'recyclequantity',
                    id:'recyclequantity',
                    hidden : this.rendermode=="unbuildproduct",
                    width:130,
                    renderer: this.RecycleQuantityRenderer.createDelegate(this)
                },{
                    header: WtfGlobal.getLocaleText("acc.field.wastageQuantity"), // "Wastage Quantity",
                    align: 'center',
                    dataIndex: 'wastageQuantity',
                    hidden: (this.isUnbuildAssembly || !Wtf.account.companyAccountPref.activateWastageCalculation),
                    renderer: this.wastageQuantityRenderer.createDelegate(this)
                },{
                    header:WtfGlobal.getLocaleText("acc.product.gridAction"),//"Action",//
                    align:'center',
                    //width:30,
                    hidden: (this.isBuildAssemblyFlag || this.readOnly || this.isCloseWO),  //If isCloseWO flag is true then hide column from grid
                    renderer: this.deleteRenderer.createDelegate(this)    
                }]);


        this.itemsgrid = new Wtf.grid.EditorGridPanel({
            layout:'fit',
            region:"center",
            clicksToEdit:1,
            autoScroll:true,
            store: this.gridStore,
            cm:this.gridcm,
            border : false,
            loadMask : true,
            viewConfig: {
//                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });

            this.tplSummary=new Wtf.XTemplate("<div style='float:right;margin-right:20px'><span>"+WtfGlobal.getLocaleText("acc.product.gridTotalBOMcost")+" </span><span>"+WtfGlobal.getCurrencySymbol()+" <b>{total}</b> </span></div>");

            var productIndex = this.gridcm.findColumnIndex("productid");
            var quantityIndex = this.gridcm.findColumnIndex("quantity");
            var quantityOnHandIndex = this.gridcm.findColumnIndex("onhand");
            var totalIndex = this.gridcm.findColumnIndex("total");
            var typeIndex = this.gridcm.findColumnIndex("type");
            var purchasepriceIndex = this.gridcm.findColumnIndex("purchaseprice");
            var recyclequantityIndex = this.gridcm.findColumnIndex("recyclequantity");
            var percentageIndex = this.gridcm.findColumnIndex("percentage");
            var addserialIndex = this.gridcm.findColumnIndex("addserial");
            
        if(this.rendermode=="productform"){
            this.gridcm.setEditable(productIndex,true); //Product
            this.gridcm.setEditable(quantityIndex,true); //Quantity
            this.gridcm.setHidden(quantityOnHandIndex,true); //Quantity on hand
        }if(this.rendermode=="buildproduct"){ //BOM Grid will be editable for Build Assembly Entry Form   
            this.gridcm.setEditable(1,true); //Product
            this.gridcm.setEditable(7,true); //Quantity
//            this.gridcm.setHidden(4,true); //Cost
            this.gridcm.setHidden(10,true); //TotalCost
//            this.gridcm.setHidden(10,true); //Action            
            this.tplSummary=new Wtf.XTemplate("<div style='float:right;margin-right:20px'><span>"+WtfGlobal.getLocaleText("acc.build.13")+" "+"</span><span> <b>{total}</b> </span></div>");
        }else if(this.rendermode=="unbuildproduct"){
            this.gridcm.setEditable(productIndex,false); //Product
            this.gridcm.setEditable(quantityIndex,false); //Quantity
//            this.gridcm.setHidden(4,true); //Cost
            this.gridcm.setHidden(totalIndex,true); //TotalCost
//            this.gridcm.setHidden(10,true); //Action
            this.tplSummary=new Wtf.XTemplate("<div style='float:right;margin-right:20px'><span>"+WtfGlobal.getLocaleText("acc.build.13")+" "+"</span><span> <b>{total}</b> </span></div>");
        }
        if (this.rendermode == "unbuildproduct") {
            this.tplSummary = new Wtf.XTemplate("<div style='float:right;margin-right:20px'></div>");
        }
        if(this.isHiddenColumn){
             this.gridcm.setHidden(typeIndex,true); //Type
             this.gridcm.setHidden(purchasepriceIndex,true); // Initial Purchase Price
             this.gridcm.setHidden(quantityOnHandIndex,true); //"Quantity On Hand"
             this.gridcm.setHidden(recyclequantityIndex,true); //recycle
        }
        /*
         * If work order is close then isCloseWO flag is true otherwise false
         */
        if(this.isCloseWO)
        {
             this.gridcm.setHidden(quantityIndex,true); //"Quantity Needed"
             this.gridcm.setHidden(percentageIndex,true); //"Percentage" 
             this.gridcm.setHidden(addserialIndex,true); //"Add Serial"    
        }
        this.northHeaderPanel=new Wtf.Panel({
            region:"north",
            height:30,
            border:false,
            bodyStyle:"background-color:#f1f1f1;padding:8px",
            html:"<div>"+this.gridtitle+"</div>"
        });

        this.southSummaryPanel=new Wtf.Panel({
            region:"south",
            height:30,
            border:false,
            bodyStyle:"background-color:#f1f1f1;padding:8px",
            html:this.tplSummary.apply({total:0})
        });

        this.wraperPanel = new Wtf.Panel({
            layout:"border",
            border : this.bodyBorder==true?true:false,
//            height:200,
            items:[
                this.northHeaderPanel,
                this.itemsgrid,
                this.southSummaryPanel
            ]
        });
        var params = {
            productid:this.productid
        };
        if(this.productForm!=undefined && this.productForm){
            params.isdefaultbom=true;
        }
        /*
         * Code has written inside the if part to update the Batch-Serial Window as per updates in Build Assembly Form BOM Grid
         */
        if(this.modifiedStore && this.rendermode=="buildproduct"){  
                for(var i=0;i<this.modifiedStore.getCount();i++){
                    var rec= this.modifiedStore.getAt(i);
                    this.gridStore.add(rec);
                }
                this.gridStore.commitChanges();
                this.updateQtyinAssemblyGrid();
                this.updateNoBuildFooter();
                this.fireEvent('updatedqty', this);
                this.fireEvent('updatedcost', this);
        } else{
            if(this.productid){
                this.gridStore.load({
                    params:params
                });
            }
        }
           
            this.gridStore.on("load",function(){
                //Original BOM Formula has preserved here. We access 'this.originalStore' in BuildAssemblyForm.js
                if(this.loadFromMetodSetProductWithBOM){
                    for(var i=0; this.gridStore.getCount()>i; i++){
                        var rec = this.gridStore.getAt(i);
                        this.originalStore.add(rec.copy());
                    }
                    this.originalStore.commitChanges();
                    this.loadFromMetodSetProductWithBOM = false;   //This varible used to identify first call of the Product Assembly Grid
                }
                this.updateQtyinAssemblyGrid();
                this.fireEvent('updatedqty', this);
                this.fireEvent('updatedcost', this);
                //            var parentindex = this.productStore.find('productid',this.productid);
                //            if(parentindex>=0){ //Remove main assembly product from dropdown
                //                var productrec=this.productStore.getAt(parentindex);
                //                this.productStore.remove(productrec);
                //            }
                if(this.gridStore.getCount()==0){
                    this.itemsgrid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                    this.itemsgrid.getView().refresh();
                }
                if(this.rendermode=="productform"){
                    this.addBlankRecord();
                    this.updateSubtotal();
                }else if(this.rendermode=="buildproduct"){
                    this.addBlankRecord(); //To add new product in BOM Grid, I have added one blank record.
                    this.updateNoBuildFooter();
                }
                this.updateCostinAssemblyGrid();
                if(Wtf.account.companyAccountPref.activateMRPManagementFlag && Wtf.account.companyAccountPref.mrpProductComponentType)
                {
                 this.calcualteCoProductAmounts();
                }
            },this);
            
        this.gridStore.on("updatedqty",this.updateQtyinAssemblyGrid,this);
        this.gridStore.on("updatedcost",this.updateCostinAssemblyGrid,this);
        this.on("updatedqty",this.updateQtyinAssemblyGrid,this);
        this.on("updatedcost",this.updateCostinAssemblyGrid,this);
        this.itemsgrid.on('afteredit',this.updateRecord,this);
        this.itemsgrid.on('validateedit',this.validateRecord,this);
        this.itemsgrid.on('beforeedit',this.beforeEditRecord,this);
        if (!this.readOnly) {
            this.itemsgrid.on('rowclick',this.handleRowClick,this);
        }
        this.add(this.wraperPanel);
        if(this.rendermode=="productform"){
             this.addBlankRecord();
        }
    },
    onRender: function (config) {
        Wtf.account.productAssemblyGrid.superclass.onRender.call(this, config);
         /*
         * If work order is close then isCloseWO flag is true otherwise false
         */
        if(!this.isCloseWO){
            if (this.moduleId!=undefined && (this.moduleId==Wtf.Build_Assembly_Report_ModuleId)){
                WtfGlobal.getGridConfig(this.itemsgrid, this.moduleId, true, false);
            }else{
                WtfGlobal.getGridConfig(this.itemsgrid, Wtf.Acc_Product_Master_ModuleId, true, false);
            }
        }
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
                this.itemsgrid.on('statesave', this.saveGridStateHandler, this);
        }, this);
    },
    currencyRendererWithPermissionCheck:function(v,m,rec){
        if(!Wtf.dispalyUnitPriceAmountInPurchase){
           return Wtf.UpriceAndAmountDisplayValue;
        } else{
           return WtfGlobal.currencyRenderer(v);
        }
    },
    bomComboRenderer: function(combo) {
        return function(value, metadata, record, row, col, store) {
            var idx = WtfGlobal.searchRecordIndex(combo.store, value, combo.valueField);
            var fieldIndex = "subbomcode";
            if (idx == -1) {
                if (record.data["subbomcode"] && record.data[fieldIndex].length > 0) {
                    return record.data[fieldIndex];
                } else {
                    return "";
                }
            }
            var rec = combo.store.getAt(idx);
            var displayField = rec.get("bomCode");
            record.set("subbomid", value);
            record.set("subbomcode", displayField);
            return displayField;
        }
    },
    openProductWindow:function(){
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.create)){
            callProductWindow(false, null, "productWin");
            Wtf.getCmp("productWin").on("update",function(obj,productid){this.productID=productid;},this);
        }
        else{
              WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.creating")+" "+WtfGlobal.getLocaleText("acc.create.products")); 
        }
    },
    updateQtyinAssemblyGrid: function () {
        var storeCount = this.rendermode == "productform" ? this.gridStore.getCount() - 1 : this.gridStore.getCount();
        for (var i = 0; i < storeCount; i++) {
            var rec = this.gridStore.getAt(i);
            var invQty = rec.data['inventoryquantiy'];
            var currentQty = this.currentQty != undefined ? this.currentQty : 1;
            if(rec.data.componentType==2 || rec.data.componentType==3){
                rec.set("actualquantity", invQty);
                rec.set("quantity", invQty);   
            }else{
                rec.set("actualquantity", invQty * currentQty);
                rec.set("quantity", invQty * currentQty);
            }
                /**
                 * set default location and warehouse
                 */ 
               
                if(this.isHiddenColumn && this.isHiddenColumn==true ){
                        WtfGlobal.setDefaultWarehouseLocationforAssembly(rec, rec);
                }
            }
    },
    
    updateCostinAssemblyGrid: function () {
        var storeCount = this.rendermode == "productform" ? this.gridStore.getCount() - 1 : this.gridStore.getCount();
        var unitCost=0;
        for (var i = 0; i < storeCount; i++) {
            var rec = this.gridStore.getAt(i);
            var purchasePrice = rec.data['purchaseprice'];
            var percentage=rec.data['percentage'];
            var qtyNeeded=rec.data['inventoryquantiy'];
            var cost=(purchasePrice*percentage*qtyNeeded)/100;
            unitCost += cost;
        }
        unitCost = parseFloat(getRoundofValue(unitCost)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        if(this.parent != undefined && this.parent != ""){
            this.parent.assemblyProductCost.setValue(unitCost);
            var totalBuildCost=this.parent.BuildQuantity.getValue() * this.parent.assemblyProductCost.getValue();
            this.parent.costToBuild.setValue(parseFloat(getRoundedAmountValue(totalBuildCost)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL));
        }
    },
    
    addBlankRecord:function(isNewBOM){
        //isNewBOM=true - When we are creating New BOM. 
        this.isNewBOM = (isNewBOM != undefined) ? isNewBOM : this.isNewBOM;
        
        var newrec = new this.gridRec({
            productid:"",
            productname:"",
            subbomid:"",
            subbomcode:"",
            desc:"",
            type:"",
            purchaseprice:"",
            saleprice:"",
            onhand:"",
            quantity:"",
            uomid:"", 
            actualquantity:1,
            percentage:100,
            total:"",
            inventoryquantiy:0,
            recylequantity:0,
            remainingquantity: 0,
            wastageInventoryQuantity: 0,
            wastageQuantityType: 0,
            wastageQuantity: 0,
            isWastageApplicable: false,
            isRecyclable : false
        });
        this.gridStore.add(newrec);
    },
    updateRecord: function(e) {            

        if (e.field == "productid") {
            if (e.row == this.gridStore.getCount() - 1) {
                this.addBlankRecord();
            }
            this.selectedproduct = e.value;
            var productrec = this.productStore.getAt(this.productStore.find('productid', e.value));
            Wtf.Ajax.requestEx({
                url: "ACCReports/getPriceCalculationForAsseblySubProduct.do",
                params: {
                    productId: productrec.data["productid"],
                    quantity: e.record.data.actualquantity,
                    buildquantity:1,
                    excluseDateFilters:this.excluseDateFilters, // product creation Form
                    isAssemblySubProduct:true
                }
            }, this, function(response) {
                var datewiseprice = response.data[0]
                e.record.set("productname", productrec.data["productname"]);
                e.record.set("desc", productrec.data["desc"]);
                e.record.set("purchaseprice", datewiseprice.purchaseprice);
                e.record.set("saleprice", datewiseprice.saleprice);
                e.record.set("onhand", datewiseprice.onhand);     //Quantity on Hand in Build Assembly
                e.record.set("type", productrec.data["type"]);
                e.record.set("producttype", productrec.data["producttype"]);
                /* If componentType is empty and undefined then set 'Component' as default value in combo otherwise selected value. */
                e.record.set("componentType", (productrec.data["componentType"]!="" && productrec.data["componentType"]!=undefined) ? productrec.data["componentType"] : Wtf.mrpComponentType.Component);              
                /* If Product contain default BOM then it will be set by default BOM value otherwise selected value set to combo. */
                e.record.set("subbomcode", (productrec.data["subbomcode"]!="" && productrec.data["subbomcode"]!=undefined && productrec.data["subbomcode"]!=null) ? productrec.data["subbomcode"]: productrec.json.defaultbomcode);
                e.record.set("subbomid", (productrec.data["subbomid"]!="" && productrec.data["subbomid"]!=undefined && productrec.data["subbomid"]!=null) ? productrec.data["subbomid"]: productrec.json.defaultbomid);
                e.record.set("isBatchForProduct", productrec.data["isBatchForProduct"]);	//ERP-28794
                e.record.set("isSerialForProduct", productrec.data["isSerialForProduct"]);
                e.record.set("isWarehouseForProduct", productrec.data["isWarehouseForProduct"]);
                e.record.set("isLocationForProduct", productrec.data["isLocationForProduct"]);
                e.record.set("isRowForProduct", productrec.data["isRowForProduct"]);
                e.record.set("isRackForProduct", productrec.data["isRackForProduct"]);
                e.record.set("isBinForProduct", productrec.data["isBinForProduct"]);
                e.record.set("uomid", productrec.data["uomid"]);
                e.record.set("uomname", productrec.data["uomname"]);
                e.record.set("quantity", 1);
                e.record.set("percentage", 100);
                e.record.set("actualquantity", 1);
                e.record.set("inventoryquantiy", 1);
                e.record.set("wastageInventoryQuantity", 1);
                e.record.set("wastageQuantityType", 0);
                e.record.set("wastageQuantity", 0);
                e.record.set("isWastageApplicable", productrec.data["isWastageApplicable"]);
                if (this.rendermode == "productform") { // ERP-30849
                    this.updateSubtotal();
                } else if (this.rendermode == "buildproduct") {
                    this.fireEvent('updatedcost', this);
                    this.updateNoBuildFooter();
                }
            }, function() {

            });
            if(Wtf.account.companyAccountPref.activateMRPManagementFlag && productrec.data["type"]==="Inventory Assembly"){
                this.subBOMStore.load({
                    params: {
                        productid: productrec.data["productid"]
                    }
                }, this);
            }
        } 
        if(e.field == "quantity" ||e.field == "percentage"){
            this.updateSubtotal();
        }
        if(Wtf.account.companyAccountPref.activateMRPManagementFlag && Wtf.account.companyAccountPref.mrpProductComponentType)
        {
            this.calcualteCoProductAmounts();
        }

},

    calcualteCoProductAmounts: function () {
        var arr = [];
        var finaltotal=0;
        this.itemsgrid.store.each(function(rec) {
            if(rec.data.componentType==1)
            {
                finaltotal+=rec.data.total;
            }
            else if(rec.data.componentType==2){
                var temp = {
                    "coproduct":"T",
                    "id":rec.data.productid,
                    "rate":rec.data.crate
                };
                arr.push(temp);
            }else if(rec.data.componentType==3){
                var temp = {
                    "scrap":"T",
                    "id":rec.data.productid,
                    "rate":rec.data.crate
                };
                arr.push(temp);
            }
        }, this);
        
        for(var j=0;j<arr.length;j++){
            if(arr[j].rate!=undefined && arr[j].rate>=0){
                var cstotal=(arr[j].rate/100)*finaltotal;
                this.itemsgrid.store.each(function(rec) {
                    if(((rec.data.productid==arr[j].id) && (rec.data.crate==arr[j].rate) ))
                    {                     
                        rec.set("total", cstotal);
                            
                    }
                }, this);
            }
        }
             
        this.updateSubtotal();
    },
    
    beforeEditRecord:function(e){        
        if (e.field == "subbomcode") {
            var rec = e.record;
            this.selectedproduct = rec.data['productid'];
            if(rec.data['type']!=="Inventory Assembly"){
                e.cancel = true;
                return false;
            }
        }
        if(this.isUsedInTransaction && this.productType==Wtf.producttype.assembly && !this.isClone){
            if(this.isNewBOM){
                return true;                
            } else {
                return false;
            }
        }
        if(this.disableEdit) {
            return false;
        }
        this.selectedRow=e.row;
    },

    validateRecord:function(e){
        if(e.field=="productid"){
            var productRecIndex = WtfGlobal.searchRecordIndex(this.gridStore, e.value, 'productid');
            //if(this.gridStore.find("productid",e.value)>=0){
            if(productRecIndex>=0){
                var productrec=this.productStore.getAt(this.productStore.find('productid',e.value));
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.76")+" "+productrec.data['productname']], 2);
                e.cancel=true;
            }        
                }
//        else if(e.field=="quantity"){
////            if(e.value<=0){
////                e.cancel=true;
////            }
//            var rec=e.record;
//              Wtf.Ajax.requestEx({
//                url: "ACCProduct/getProductAvialbaleQuantity.do",
//                params: {
//                    productid: rec.data.productid
//                }
//            }, this, function(response) {
//                var availableQuantity = response.quantity
//                var originalQuantity = e.originalValue;
//                if(e.value > availableQuantity){
//                    if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
//                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.Quantitygivenareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data.productname+' '+WtfGlobal.getLocaleText("acc.field.is")+availableQuantity+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
//                             rec.set("quantity",originalQuantity);
//                            e.cancel=true;   
//                    }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
//                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Quantitygivenareexceedingthequantityavailabledoyouwish")+'</center>' , function(btn){
//                                if(btn=="yes"){
//                                    e.cancel=false;
//                                }else{
//                                    rec.set("quantity",originalQuantity);
//                                    e.cancel=true;
//                                    return false;
//                                }
//                            },this); 
//                    }
//                }
//            }, function() {
//
//            });
//           
//        }
        else if(e.field == "percentage"){
            if(e.value>100){
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.master.SalsCommissin.perError")], 2);
                e.cancel=true;
            }else if(e.record.data.isSerialForProduct && e.value != 100){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.master.serialPercentageError")], 2);
                e.cancel=true;
            }
        }
        else if(e.field == "crate"){
            if(e.value>100){
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("Rate Value should not be greater than 100")], 2);
                e.cancel=true;
            }
        }
    },
    updateSubtotal:function(){
        var subtotal=0, price = 0,disc = 0;
        var storeCount=this.rendermode=="productform"?this.gridStore.getCount()-1:this.gridStore.getCount();
        storeCount=this.readOnly?this.gridStore.getCount():storeCount;
        for(var i=0;i<storeCount;i++){
            var rec=this.gridStore.getAt(i);
            var priceAfterDiscount=0
//            price = (rec.data['producttype']!=Wtf.producttype.service)?rec.data['purchaseprice']:rec.data['saleprice'];//service
            price = (rec.data['purchaseprice']=== undefined || rec.data['purchaseprice']=="")?0:rec.data['purchaseprice'];
            disc = (rec.data['percentage']=== undefined || rec.data['percentage']=="")? 100 : rec.data['percentage'];
            var Qty=rec.data['quantity'];
//             var lineTotal=Qty*price;
//              var lineTotalDiscount=(disc*lineTotal)/100;
              if ((Wtf.account.companyAccountPref.activateMRPManagementFlag && Wtf.account.companyAccountPref.mrpProductComponentType) && (rec.data.componentType == 2 || rec.data.componentType == 3)) {
                 /* priceAfterDiscount value is String so converted into float*/
                 priceAfterDiscount = parseFloat(getRoundofValue(rec.data.total));
              } else {
                  priceAfterDiscount =parseFloat(getRoundofValue((price*((disc*Qty)/100))));
              }
              rec.set("total",priceAfterDiscount);
              rec.set("actualquantity",(disc*Qty)/100);
              rec.set("inventoryquantiy",(disc*Qty)/100);
              rec.set("wastageInventoryQuantity",(disc*Qty)/100);
            /**
             * set default location and warehouse if qty change from items grid
             */

            if (this.isHiddenColumn && this.isHiddenColumn == true) {
                WtfGlobal.setDefaultWarehouseLocationforAssembly(rec, rec);
            }
            /*  If record contain co-product or scrap type product then it will subtract from total of component type product otherwise addition will be done. */
             if(rec.data['componentType']==2 || rec.data['componentType']==3){
                subtotal-=priceAfterDiscount;
            }else{
                subtotal+=priceAfterDiscount;
            }
       
        }
        subtotal = parseFloat(getRoundofValue(subtotal)).toFixed(Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        this.totalcost = subtotal;
        this.fireEvent('updatedcost',this);
        if(!Wtf.dispalyUnitPriceAmountInPurchase){// If no permission to show price then this condition will be true
            this.tplSummary.overwrite(this.southSummaryPanel.body,{total:Wtf.UpriceAndAmountDisplayValue});
        } else {
            this.tplSummary.overwrite(this.southSummaryPanel.body,{total:getRoundedAmountValue(subtotal)});
        }
    },   
    RecycleQuantityRenderer:function(v,m,rec){  //serial render
        return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.quantity.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.quantity.desc.title")+"' class='"+getButtonIconCls(Wtf.etype.recycleQuantity)+"'></div>";
    },
    
    wastageQuantityRenderer: function(v,m,rec) {
        return "<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.wastageQuantity.desc") + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.field.wastageQuantity") + "' class='" + getButtonIconCls(Wtf.etype.wastageQuantity) + "'></div>";
    },

//     showRecycleQuantiy:function(val,rec,c){
//        var hide=(val==null||undefined?false:val) ;
//        var id=this.itemsgrid.getId()
//        var recyclequantity=this.itemsgrid.getColumnModel().getIndexById("recyclequantity");
//        var actualquantity=this.itemsgrid.getColumnModel().getIndexById("actualquantity");
//        var percentage=this.itemsgrid.getColumnModel().getIndexById("percentage");  
//        this.itemsgrid.getColumnModel().setHidden(recyclequantity,!hide);
//        this.itemsgrid.getColumnModel().setHidden(actualquantity,!hide);
//        this.itemsgrid.getColumnModel().setHidden(percentage,!hide);
//
//     },
    updateNoBuildFooter: function(){
        var maxbuilds = 0, k=0 ,maxBuildsForChildObj=0;
        for(var j=0;j<this.gridStore.getCount();j++){
            if(this.gridStore.getAt(j).data['id']=="" || this.gridStore.getAt(j).data['id']==undefined){
                continue;   //To avoide 'NaN' issue for footer
            }
            var producttype = this.gridStore.getAt(j).data['producttype'];
            if(producttype!="4efb0286-5627-102d-8de6-001cc0794cfa"){//service
                var onhand = this.gridStore.getAt(j).data['onhand'];
                onhand = (onhand<0)?0:onhand;
                var mx = onhand/this.gridStore.getAt(j).data['quantity'];
                var mx1=mx;
                mx = Math.floor(mx);
                if(k==0){
                    maxbuilds = mx;
                    maxBuildsForChildObj=mx1;
                }else{
                    maxbuilds = (maxbuilds<mx)?maxbuilds:mx;
                    maxBuildsForChildObj = (maxBuildsForChildObj<mx1)?maxBuildsForChildObj:mx1;
                    
                }
                k++;
            }
        }
        this.maxbuilds = maxbuilds;
        this.fireEvent('updatedbuilds',this);
        if (this.southSummaryPanel.body) {
            this.tplSummary.overwrite(this.southSummaryPanel.body, {total: maxbuilds});
        } else {
            this.southSummaryPanel.html = this.tplSummary.apply({total: (maxBuildsForChildObj*this.currentQuantity)});
        }
    },
    getAssemblyJson:function(){
        var cnt = this.gridStore.getCount()-1;
        if(this.rendermode=="buildproduct" || this.rendermode=="unbuildproduct"){
            cnt = this.gridStore.getCount();
        }
        
        var jsonstring="";
        if(this.gridStore.getCount()>0){
            for(var i=0;i<cnt;i++){
                var rec = this.gridStore.getAt(i);
                if (rec.data.producttype === Wtf.producttype.assembly && Wtf.account.companyAccountPref.activateMRPManagementFlag) { // Show message if BOM is not defined for the asssembly product
                    if (!rec.data.subbomid) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.product.BOM.not.provided.inassemble")],2); 
                        return;
                    }
                }
                var recylequantity=(rec.data['recylequantity']==""||rec.data['recylequantity']==undefined)?0:rec.data['recylequantity'];
                var subbomid = (rec.data['subbomid'] == "" || rec.data['subbomid'] == undefined) ? "''" : rec.data['subbomid'];
                var remainingquantity=(rec.data['remainingquantity']==""||rec.data['remainingquantity']==undefined)?0:rec.data['remainingquantity'];
                var percentage=(rec.data['percentage']==""||rec.data['percentage']==undefined)?100:rec.data['percentage'];
                var rate=(rec.data['purchaseprice']==""||rec.data['purchaseprice']==undefined)?0:rec.data['purchaseprice'];
                var quantity=(rec.data['quantity']==""||rec.data['quantity']==undefined)?0:rec.data['quantity'];
                jsonstring += "{product:\""+rec.data['productid']+"\","+
//                                "rate:"+(rec.data['producttype']!=Wtf.producttype.service?rec.data['purchaseprice']:rec.data['saleprice'])+","+
                                "rate:"+rate+","+ "inventoryquantiy:"+rec.data['inventoryquantiy']+","+ "remainingquantity:"+remainingquantity+","+
                                "actualquantity:"+rec.data['actualquantity']+","+ "percentage:"+percentage+","+
                                 "recylequantity:"+recylequantity+","+ "quantity:"+quantity + "," +
                                 "wastageInventoryQuantity:" + rec.data['wastageInventoryQuantity'] + "," + "wastageQuantityType:" + rec.data['wastageQuantityType'] + "," +
                                 "wastageQuantity:" + rec.data['wastageQuantity'] + "," +"subbomid:" + subbomid+"},";
            }
            jsonstring = jsonstring.substr(0, jsonstring.length-1);
        }
        return jsonstring;
    },
    setProduct:function(productid,workorderid){
        if(productid){
            this.gridStore.load({
                params:{
                    productid:productid,
                    workorderid:workorderid
                }
            });
        }
    },
    setProductWithDefaultBOM:function(productid){
        if(productid){
            this.gridStore.load({
                params:{
                    productid: productid,
                    isdefaultbom: true
                }
            });
        }
    },
    setProductWithBOM:function(productid,bomid){
        if(productid){
            this.loadFromMetodSetProductWithBOM = true;   //This varible used to identify first call of the Product Assembly Grid
            this.gridStore.load({
                params:{
                    productid: productid,
                    bomdetailid: bomid
                }
            });
        }
    },
    setParams:function(url,productid,mode,mainproduct){
        if(productid){
            this.gridStore.proxy.conn.url=url,
            this.gridStore.reload({
                params:{
                    productid:productid,
                    mainproduct:mainproduct,
                    isEdit:true
                }
            });
            this.gridStore.proxy.conn.url='ACCProduct/getAssemblyItems.do';
        }
    },
        serialRenderer:function(v,m,rec){
        return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.serial.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.serial.desc.title")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
    },
    deleteRenderer:function(v,m,rec){
        var flag=false;
        var cm=this.itemsgrid.getColumnModel();
        var count = cm.getColumnCount();
        for (var i=0; i<count-3; i++) {
            if(cm.getDataIndex(i).length > 0 && !cm.isHidden(i)) {
                if(rec.data[cm.getDataIndex(i)]!=undefined && rec.data[cm.getDataIndex(i)].length>0){
                    flag=true;
                    break;
                }
            }
        }
        if(flag){
            return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
        }
        return "";
    },
    handleRowClick:function(grid,rowindex,e){
        
        if(Wtf.account.companyAccountPref.activateMRPManagementFlag && Wtf.account.companyAccountPref.mrpProductComponentType){
            var crateindex=this.gridcm.findColumnIndex("crate");
            var store=grid.getStore();
            var record=store.getAt(rowindex);
            if(record!=undefined && record.data.componentType==1)
            {
                this.gridcm.setEditable(crateindex,false);
                 
            }
            else
            {
                this.gridcm.setEditable(crateindex,true);
            }
        }
            if(this.disableEdit) {
                return false;
            }
            if(e.getTarget(".delete-gridrow")){
                if(this.isUsedInTransaction && this.productType==Wtf.producttype.assembly && !this.isNewBOM && this.isEdit && !this.isClone){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Product is used in Transaction.So you can not edit/delete this record"],2);
                }else{
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.setupWizard.note27"), function(btn){
                    if(btn!="yes") return;
                    var store=grid.getStore();
                    var total=store.getCount();
                    store.remove(store.getAt(rowindex));
                    grid.getView().refresh();
                    if(rowindex==total-1){
                        this.addBlankRecord();
                    }
                    if(this.rendermode=="productform"){
                        this.updateSubtotal();
                    }
                }, this);
            }
             } else if(e.getTarget(".serialNo-gridrow")){
                var store=grid.getStore();
                var obj = store.getAt(rowindex);
               var index=this.productStore.findBy(function(rec){
                if(rec.data.productid==obj.data.productid)
                    return true;
                else
                    return false;
            })
            var prorec=this.productStore.getAt(index) == undefined ? obj : this.productStore.getAt(index) ; 
             if(index==-1){
                index=WtfGlobal.searchRecordIndex(store, obj.data.productid, 'productid');
                if(index >=0){
                    prorec=store.getAt(index);
                }
            }
            if((obj.data.batchdetails == "" || obj.data.batchdetails == undefined) && this.serialNoDetails != undefined){
                var temp = this.serialNoDetails.split("\",\"");
                obj.data.batchdetails = temp[index];
            }
            
            if(prorec.data.isLocationForProduct || prorec.data.isWarehouseForProduct || prorec.data.isBatchForProduct || prorec.data.isSerialForProduct || prorec.data.isRowForProduct || prorec.data.isRackForProduct || prorec.data.isBinForProduct) {
                if (this.isFinishGood) {
                    this.isEdit = true;
                }
                   this.batchDetailswin=new Wtf.account.SerialNoWindow({
                renderTo: document.body,
                title:WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber"),
                productName:prorec.data.productname,
                uomName:prorec.data.uomname,
                quantity:obj.data.actualquantity,
                defaultLocation:prorec.data.location,
                productid:prorec.data.productid,
                isSales:true,
                moduleid:this.moduleid, 
                defaultWarehouse:prorec.data.warehouse,
                batchDetails:obj.data.batchdetails,
                isForCustomerAssembly:this.isForCustomerAssembly,
                jobworkorderid:this.jobworkorderid,
                isLocationForProduct:prorec.data.isLocationForProduct,
                isWarehouseForProduct:prorec.data.isWarehouseForProduct,
                isRowForProduct:prorec.data.isRowForProduct,
                isRackForProduct:prorec.data.isRackForProduct,
                isBinForProduct:prorec.data.isBinForProduct,
                isBatchForProduct:prorec.data.isBatchForProduct,
                isSerialForProduct:prorec.data.isSerialForProduct,
                isSKUForProduct:prorec.data.isSKUForProduct,
                isUnbuildAssembly : (this.isUnbuildAssembly !=undefined && this.isUnbuildAssembly) ? true : false,
                mainassemblyproduct : this.mainassemblyproduct,     //Parent Product in Assembly Product
                //refno:this.refno,
                transactionid:4,
                 isDO:true,
                isEdit:this.isEdit,
                 copyTrans:this.copyTrans,
                isfromProductAssembly:true,
                width:950,
                modal:true,
                height:400,
                resizable : false
            });

            this.batchDetailswin.on("beforeclose",function(){
                this.batchDetails=this.batchDetailswin.getBatchDetails();
                var isfromSubmit=this.batchDetailswin.isfromSubmit;
                if(isfromSubmit){  //as while clicking on the cancel icon it was adding the reocrd in batchdetail json 
                    obj.set("batchdetails",this.batchDetails);
                    obj.set("batchdetailsnew",this.batchDetails);
                    if (this.isForCustomerAssembly) { // Onl; for Job wor Assembly type of Products
                        this.batchDetailsArr = eval(this.batchDetails);
                        
                        for (var index = 0; index < this.batchDetailsArr.length; index++ ) {
                            this.batchDetailsObj = this.batchDetailsArr[index];
                            var challanName = this.batchDetailsObj.batchname;
                            
                            if (this.buildAssemblyObj.challanName) { // Challan Name already Present
                                if (this.buildAssemblyObj.challanName.indexOf(challanName) == -1) { // Adding when Challan is not present in that String
                                    this.buildAssemblyObj.challanName += "," +challanName;
                }
                            } else {
                                this.buildAssemblyObj.challanName = decodeURI(challanName);
                            }
                        }
                        if (this.buildAssemblyObj.grid && this.buildAssemblyObj.grid.getStore()) {
                            var count = this.buildAssemblyObj.grid.getStore().getCount();
                            var recs =  this.buildAssemblyObj.grid.getStore().data.items;
                            /*
                             * Changing grid challan Name 
                             */
                            for (var index2  = 0; index2 < count; index2++) {
                                var rec = recs[index2];
                                rec.set("batch",this.buildAssemblyObj.challanName);
                            }
                        }
                        
                    }
                }
                   this.assemblyProductJson[rowindex]=this.batchDetails;
            },this);

            this.batchDetailswin.show();
            }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.batchserial.Functinality")],2);   //Batch and serial no details are not valid.
                    return;
                }
        }else if(e.getTarget(".recycleQuantity-gridrow") && (grid.getStore().getAt(rowindex).data.componentType != undefined && !((grid.getStore().getAt(rowindex).data.componentType=="2") || (grid.getStore().getAt(rowindex).data.componentType=="3")))){
              if(this.isInitialQuatiy){
                    var store=grid.getStore();
                    var record = store.getAt(rowindex);
                    var productid = record.get('productid');
                    var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productStore, productid, 'productid');   
                    if(productComboRecIndex >=0){
                        var proRecord = this.productStore.getAt(productComboRecIndex);
                    }else{
                        var index=WtfGlobal.searchRecordIndex(store, productid, 'productid');
                        if(index >=0){
                             proRecord=store.getAt(index);
                        }
                    }
                            
                    if(proRecord.data.type!='Service' && proRecord.data.type!='Non-Inventory Part'){  //serial no for only inventory type of product
                        if(proRecord.data.isRecyclable) //isBatchForProduct || proRecord.data.isSerialForProduct
                        {
                            this.callRecycleQuantiyWindow(record,grid,rowindex,proRecord);
                        }else{
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.product.warningRecycleProduct")], 2);
                            return;
                        }

                    }else{
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.batchserial.funforInventoryitems")],2);   //Batch and serial no details are not valid.
                        return;
                    }

                }else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.assemblyproductform.recyleqtycheck")],2);   //  Initial Quantity of Product is 0. So cannot adjust Recycle Quantity.
                    return;
                }  

        } else if (e.getTarget(".wastageQuantity-gridrow")) {
            var store = grid.getStore();
            var record = store.getAt(rowindex);
            if (record.data.isWastageApplicable) {
                this.callWastageQuantiyWindow(record,grid,rowindex);
            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.product.warningWastageApplicableProduct")], 2);
                return;
            }
        }
    },
    getFGSubProductBatchDetails: function() {
        this.gridStore.each(function(rec) {
            if (rec.data.rowid == undefined) {
                rec.data.rowid = '';

            }
        }, this);
        this.gridStore.each(function(rec) {
            if ((rec.data.documentid == undefined || rec.data.documentid == "") && this.moduleid == 53) {
                rec.data.documentid = this.documentid;
            }
        }, this);
        var arr = [];
        var inculelast = false;
//        if(this.isFixedAsset || (this.isfromProductAssembly && this.includeLastRowInProdAssembly))
//        {
        inculelast = true;
        this.includeLastRowInProdAssembly = false;
//        }
        this.gridStore.each(function(rec) {

            arr.push(this.gridStore.indexOf(rec));
        }, this);
        var jarray = WtfGlobal.getJSONArray(this.itemsgrid, inculelast, arr);
        return jarray;
    },
      callRecycleQuantiyWindow:function(obj,grid,rowindex,prorec){
          
        var recycleQuantity = obj.data.actualquantity;
        recycleQuantity = (recycleQuantity == "NaN" || recycleQuantity == undefined || recycleQuantity == null)?0:recycleQuantity;
       this.RecycleQuantiyWin=new Wtf.account.RecycleQuanity({
            renderTo: document.body,
            title:WtfGlobal.getLocaleText("acc.field.AdjustRecycleQuantity"),
            productName:prorec.data.productname,
            inventoryquantity:recycleQuantity,
            billid:obj.data.billid,
            grid:grid,
            record:obj,
            recycleQuantity:prorec.data.recycleQuantity,
            rowindex:rowindex,
            productid:prorec.data.productid,
            isSales:this.isCustomer,
            moduleid:this.moduleid,
//            transactionid:(this.isCustomer)?4:5,         
//            isEdit:this.isEdit,
            width:600,
            height:400,
            resizable : false,
            modal : true
        });
        this.RecycleQuantiyWin.show();
    },
    cloneProductList: function(){
        if(Wtf.getCmp(this.id)){//Product Assembly Grid.
            Wtf.productStore.each(function(rec){
                if(this.productid){//Edit Case
                    if(rec.data.productid!=this.productid){
                        var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productStore, rec.data.productid, 'productid');
                        if (productComboRecIndex == -1){ // if productid not present in store then only add the record
                            this.productStore.add(rec);
                        }
                    }
                } else {
                    if (rec.data.productid!=undefined) {
                        var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productStore, rec.data.productid, 'productid');
                        if (productComboRecIndex == -1) { // if productid not present in store then only add the record
                            this.productStore.add(rec);
                        }
                    }
                }
            },this);
        }
//        this.productStore.load();
    },
      getComboNameRenderer : function(combo){
        return function(value,metadata,record,row,col,store) {
            var idx = WtfGlobal.searchRecordIndex(combo.store,value,combo.valueField);
            var fieldIndex = "productname";
            if(idx == -1) {
                if(record.data["productname"] && record.data[fieldIndex].length>0) {
                    return record.data[fieldIndex];
                }
                else
                    return "";
            }
            var rec = combo.store.getAt(idx);
            var displayField = rec.get(combo.displayField);
            record.set("productid", value);
            record.set("productname", displayField);
            return displayField;
        }
    },
    
    callWastageQuantiyWindow: function(obj,grid,rowindex) {
        var actualQuantity = obj.data.actualquantity;
        actualQuantity = (actualQuantity == "NaN" || actualQuantity == undefined || actualQuantity == null) ? 0 : actualQuantity;

        this.wastageQuantityWin = new Wtf.account.wastageQuanity({
            renderTo: document.body,
            title: WtfGlobal.getLocaleText("acc.field.adjustWastageQuantity"),
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            productName: obj.data.productname,
            actualQuantity: actualQuantity,
            grid: grid,
            record: obj,
            rowindex: rowindex,
            productid: obj.data.productid,
            width: 600,
            height: 310,
            resizable: false,
            modal: true
        });
        this.wastageQuantityWin.show();
    },
    
    saveGridStateHandler: function (grid, state) {
        if (state && state.columns) {
            for (var i = 0; i < state.columns.length; i++) {
                delete state.columns[i].hidden;
            }
        }
         /*
         * If work order is close then isCloseWO flag is true otherwise false
         */
        if(!this.isCloseWO){
            if (this.moduleId!=undefined && (this.moduleId==Wtf.Build_Assembly_Report_ModuleId)){
                WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleId, grid.gridConfigId, true);
            }else{
                WtfGlobal.saveGridStateHandler(this, grid, state, Wtf.Acc_Product_Master_ModuleId, grid.gridConfigId, true);
            }
        }
}
});
