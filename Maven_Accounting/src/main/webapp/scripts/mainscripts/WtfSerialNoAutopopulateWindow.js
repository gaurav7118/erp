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

Wtf.account.SerialNoAutopopulateWindow = function (config){
     this.modeName = config.modeName;
    this.isUnbuildAssembly = config.isUnbuildAssembly;
    this.mainassemblyproduct = config.mainassemblyproduct;
    //this.refno=config.refno;
    this.value="1";
    this.parent=[];
    this.child=[];
    this.levelid=[];
    this.levelNm=[];
    this.serialNumberCMArr=[];
    this.isCN=config.isCN;
    this.isCustBill=config.isCustBill;
    this.isForCustomerAssembly=config.isForCustomerAssembly,
    this.jobworkorderid=config.jobworkorderid,
    this.isJobworkOrder=config.isJobworkOrder,
    this.moduleid = config.moduleid;
    config.quantity = (config.quantity=="NaN" || config.quantity=="") ? 0:parseFloat(config.quantity).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
    this.isfromProductAssembly = (config.isfromProductAssembly)?config.isfromProductAssembly:false;
    this.totalStoreCount=7;//ERP-10478 //Total no. of stores to be loaded while rendering the window. Increase the count if more stores are added to handle load mask.
    this.loadedStoreCount=0;
    this.module=Wtf.SerialWindow_ModuleId ;
    this.cntype = config.cntype;
    this.businessPerson=(this.customerFlag?"Customer":"Vendor");
    this.externalcurrencyrate=0;
    this.noteType=WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber");
    this.custPermType=config.isCN?Wtf.Perm.customer:Wtf.Perm.vendor;
    this.soUPermType=(config.isCN?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
    this.isCustomer=config.isCN;
    this.isfromSubmit=false;
    this.fromPO = (config.fromPO)?config.fromPO:false;
    this.butnArr = new Array();
    this.transactionid=config.transactionid;
    this.remainingQuantity=0;
    this.isBatchForProduct=config.isBatchForProduct;  //product level batch option
    this.isSerialForProduct=config.isSerialForProduct; //product level serial option
    this.isSKUForProduct=config.isSKUForProduct; //product level sku option
    this.allowUserToEditQuantity=config.allowUserToEditQuantity;        //If this flag is true than we allow user to edit Qty in batch and serial window this flag is only used when  Variable Purchase/Sales UOM conversion rate check is enable in company preferences ERM-319
    this.linkflag=config.linkflag;
    this.isEdit=config.isEdit;
    this.isItemReusable=config.isItemReusable ;
    this.copyTrans=config.copyTrans;
    this.billid=config.billid;
    this.workorderid=config.workorderid;
    this.movmentType=config.movmentType;
    this.requestWarehouse=config.requestWarehouse;
    this.requestLocation=config.requestLocation;
    this.isLeaseFixedAsset=config.isLeaseFixedAsset;
    this.responseBatch=false;
    this.recordLength=0;
    this.AvailableQuantity=0;
    this.defaultAvailbaleQty=config.defaultAvailbaleQty;
    this.customerID=config.customerID;
    this.isblokedinso=(config.isblokedinso)?config.isblokedinso:false;
    this.documentid=config.documentid;
    this.readOnly=config.readOnly;
    this.storeLoadCount=0;
    this.linkedFrom=config.linkedFrom;
    this.setValuesToMultipleRec=false;
    this.isWastageApplicable = (config.isWastageApplicable) ? config.isWastageApplicable : false;
    this.isShowStockType=(config.isShowStockType)?config.isShowStockType:false,
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.submit"),   //'Submit',
        scope: this,
        hidden:this.readOnly,
        handler: function() {
            //this.store.removeAll();
            this.addTomainStore();
            if(this.validateBatchSerialDetails()){
                this.isfromSubmit=true;
                this.close();
            }
//            for(var p = 0;p < this.store.data.items.length -1;p++){
//                var rowObject = new Object();
//                rowObject['srnoid'] = this.store.data.items[p].data.serialnoid;
//                Wtf.unqsrno.push(rowObject);
//                                       
//            }
//            for(var q = 0;q < this.store.data.items.length -1;q++){
//                var rowObject = new Object();
//                rowObject['srnoid'] = this.store.data.items[q].data.serialnoid;
//                Wtf.dupsrno.push(rowObject);
//                                       
//            }            
        }
    },{
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),   //'Cancel',
        scope: this,
        handler: function() {
            //On Cancel removed items which are added in duplicate store
//            if(!this.readOnly){
//                for(var p = 0;p < this.store.data.items.length -1;p++){
//                    var rowObject = new Object();
//                    rowObject['srnoid'] = this.store.data.items[p].data.serialnoid;
//                    Wtf.unqsrno.pop(rowObject);
//
//                }
//                for(var q = 0;q < this.store.data.items.length -1;q++){
//                    var rowObject = new Object();
//                    rowObject['srnoid'] = this.store.data.items[q].data.serialnoid;
//                    Wtf.dupsrno.pop(rowObject);
//
//                }         
//            }
            this.hide();
        }
    });

     Wtf.apply(this,{
        buttons: this.butnArr
    },config);
    Wtf.account.SerialNoAutopopulateWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.SerialNoAutopopulateWindow,Wtf.Window,{
    onRender:function(config){
        Wtf.account.SerialNoAutopopulateWindow.superclass.onRender.call(this, config);
       
       var title=this.noteType;//this.isCN?WtfGlobal.getLocaleText("acc.cn.payType"):WtfGlobal.getLocaleText("acc.dn.payType");
        //var msg="<b>Product</b> : "+this.productName+"<br> <b>Quantity</b> : "+this.quantity
        var msg="";
        if(this.uomName!=undefined && this.uomName!=""){
            msg="<b>"+WtfGlobal.getLocaleText("acc.product.gridProduct")+"</b> : "+this.productName+"<br> <b>"+WtfGlobal.getLocaleText("acc.product.gridQty")+"</b> : "+this.quantity+" "+this.uomName;
        }else{
            msg="<b>"+WtfGlobal.getLocaleText("acc.product.gridProduct")+"</b> : "+this.productName+"<br> <b>"+WtfGlobal.getLocaleText("acc.product.gridQty")+"</b> : "+this.quantity;
        }
       var isgrid=true;
       this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(title,msg,"../../images/accounting_image/price-list.gif",isgrid)},
        {
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.grid
           // tbar:this.buttonArray
//            ,
//            bbar:this.pagingToolbar
        });
    },
    initComponent:function(config){
        Wtf.account.SerialNoAutopopulateWindow.superclass.initComponent.call(this, config);
        //Create Store
        this.createStore();
        
        //Create Grid
        this.createGrid();
        
       
        
        this.grid.on('cellclick',this.onCellClick, this);
        this.grid.on('beforeedit',this.checkrecord,this);
        this.grid.on('afteredit',this.updateRow,this);
    },
  
  
    createStore:function(){
        this.record=new Wtf.data.Record.create([
            {
                "name":"id"
            },
            {
                "name":"storeName"
            },
            {
                "name":"productid"
            },
            {
                "name":"locationName"
            },
            {
                "name":"warehouse"
            },
            {
                "name":"packwarehouse"
            },
            {
                "name":"location"
            },
            {
                "name":"row"
            },
            {
                "name":"rack"
            },
            {
                "name":"bin"
            },
            {
                "name":"purchasebatchid"
            },
            {
                "name":"rowName"
            },
            {
                "name":"rackName"
            },
            {
                "name":"binName"
            },
            {
                "name":"avialblequantity"
            },
            {
                "name":"quantity"
            },
            {
                "name":"batchName"
            },
            {
                "name":"serialno"
            },
            {
                "name":"serialNames"
            },
            {
                "name":"serialDetails"
            },
            {
                "name":"batchQty"
            },
            {
                "name":"stockType"
            },
            {
                "name":"stockTypeName"
            },
            {
                "name":"isBatchForProduct"
            },
            {
                "name":"isSerialForProduct"
            },
            {
                "name":"isRowForProduct"
            },
            {
                "name":"isRackForProduct"
            },
            {
                "name":"isBinForProduct"
            },
            {
                "name":"stockType"
            },
            {
                "name":"stockTypeName"
            },
            {
                "name":"mfgdate" , 
                type:'date'
            },
            {
                "name":"expdate" , 
                type:'date'
            }
        ]);  
        
        this.tmpRecord=new Wtf.data.Record.create([
            {
                "name":"id"
            },
            {
                "name":"storeName"
            },
            {
                "name":"productid"
            },
            {
                "name":"locationName"
            },
            {
                "name":"warehouse"
            },
            {
                "name":"packwarehouse"
            },
            {
                "name":"location"
            },
            {
                "name":"row"
            },
            {
                "name":"rack"
            },
            {
                "name":"bin"
            },
            {
                "name":"purchasebatchid"
            },
            {
                "name":"rowName"
            },
            {
                "name":"rackName"
            },
            {
                "name":"binName"
            },
            {
                "name":"avialblequantity"
            },
            {
                "name":"quantity"
            },
            {
                "name":"batchName"
            },
            {
                "name":"serialno"
            },
            {
                "name":"serialNames"
            },
            {
                "name":"serialDetails"
            },
            {
                "name":"batchQty"
            },
            {
                "name":"stockType"
            },
            {
                "name":"stockTypeName"
            },
            {
                "name":"isBatchForProduct"
            },
            {
                "name":"isSerialForProduct"
            },
            {
                "name":"isRowForProduct"
            },
            {
                "name":"isRackForProduct"
            },
            {
                "name":"isBinForProduct"
            },
            {
                "name":"stockType"
            },
            {
                "name":"stockTypeName"
            },
            {
                "name":"mfgdate" , 
                type:'date'
            },
            {
                "name":"expdate" , 
                type:'date'
            }
        ]);  

        this.store=new Wtf.data.Store({
             url: "INVStockLevel/getStockDetailOfProduct.do",
                baseParams:({
                 productId:this.productid,
                 documentid:((this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId) && this.isEdit)?this.docrowid : this.documentid,
                 isEdit:this.isEdit,
                 moduleid : this.moduleid,
                 linkflag:this.linkflag
            }),
            reader:new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"  
            },this.record)
            
        }); 
        
        this.tmpStore=new Wtf.data.Store({
//             url: "INVStockLevel/getStockDetailOfProduct.do",
            reader:new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"  
            },this.tmpRecord)
            
        }); 
    },

    loadStore:function(){
        this.store.load({
            params: {
            productId:this.productid,
            start:0,
            limit:30,
            isShippingDO:this.isShippingDO,
            quantity:this.quantity
            }
        },this);
    },
    createGrid:function(){
        this.addBtn = new Wtf.Button({
            text: WtfGlobal.getLocaleText("Add"),   //'Submit',
            scope: this,
            hidden:this.readOnly,
            handler: function() {
                this.addTotmpStore();
            }
        });
        this.packWarehouseRec = new Wtf.data.Record.create([
        {
            name:"packwarehouse"
        },

        {
            name:"abbr"
        },

        {
            name:"fullname"
        },
        ]);
        
        this.packWarehouseReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.packWarehouseRec);
        
        this.packWarehouseStore = new Wtf.data.Store({
            url :"ACCMaster/getPackingStore.do",
            reader:this.packWarehouseReader
        });
       
        this.packWarehouseStore.load({
            params:{
                isActive : "true",
                isLocation:false
            }
        });

        this.packWarehouse = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:'Pack Warehouse',
            valueField:'packwarehouse',
            displayField:'fullname',
            store:this.packWarehouseStore,
            anchor:'90%',
            lastQuery:'',
            typeAhead: true,
            forceSelection: true,
            name:'packwarehouse',
            value:Wtf.account.companyAccountPref.packingstore,
            hiddenName:'packwarehouse',
            hidden:Wtf.account.companyAccountPref.interloconpick
        //            disabled: true
        });
        
         
        this.packLocationRec = new Wtf.data.Record.create([
        {
            name:"packlocation"
        },

        {
            name:"abbr"
        },

        {
            name:"fullname"
        },
        ]);
        
        this.packLocationReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.packLocationRec);
        
        this.packLocationStore = new Wtf.data.Store({
            url :"ACCMaster/getPackingStore.do",
            reader:this.packLocationReader
        });
       
        this.packLocationStore.load({
            params:{
                isActive : "true",
                isLocation:true
            }
        });
 
        this.packLocation = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:'Pack Location',
            valueField:'packlocation',
            displayField:'fullname',
            lastQuery:'',
            store:this.packLocationStore,
            anchor:'90%',
            typeAhead: true,
            forceSelection: true,
            name:'packlocation',
            value:Wtf.account.companyAccountPref.packinglocation,
            hiddenName:'packlocation',
            hidden:!Wtf.account.companyAccountPref.interloconpick
        //            disabled: true
        });
        this.packLocationStore.on("load",function(){
            this.packLocation.setValue(Wtf.account.companyAccountPref.packinglocation);
        },this);
        
        this.packWarehouseStore.on("load",function(){
            this.packWarehouse.setValue(Wtf.account.companyAccountPref.packingstore);
        },this);
        
       this.packLocationStore.on("load",function(){
            var i = 0;
            if (Wtf.account.companyAccountPref.pickpackship && this.packLocationStore!=null && this.packLocationStore!=undefined && this.packLocationStore.getAt(i)!=null && this.packLocationStore.getAt(i)!=undefined) {
                    var record = this.packLocationStore.getAt(i);
                    if(record!=undefined && record!=null && record.json!=null && record.json!="" && record.json!=undefined){
                    this.parentObj.pickpacklocation = record.json.packinglocationid?record.json.packinglocationid:"";    
                    }                    
                }            
            },this);       
        
        var cmDefaultWidth = 200;
          this.gridColumnModelArr=[];
          
          var isPckWarehouse=(this.moduleid == Wtf.Acc_Delivery_Order_ModuleId && !this.isSalesOrder) && Wtf.account.companyAccountPref.pickpackship && !Wtf.account.companyAccountPref.interloconpick;
          var isPackLoc=(this.moduleid == Wtf.Acc_Delivery_Order_ModuleId && !this.isSalesOrder)  && Wtf.account.companyAccountPref.pickpackship && Wtf.account.companyAccountPref.interloconpick;
        
        this.sm = new Wtf.grid.CheckboxSelectionModel({});
        var tbarArray = new Array();
//        tbarArray.push(this.addBtn);
        
        
           this.gridColumnModelArr.push(new Wtf.grid.RowNumberer(),
            this.sm, 
        {
                header: WtfGlobal.getLocaleText("acc.product.threshold.grid.storecode"),
                dataIndex: 'storeName',
                width:cmDefaultWidth,
                hidden:!this.convertStringToBoolean(this.isWarehouseForProduct),
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp.add"),
                dataIndex: 'locationName',
                width:cmDefaultWidth,
                hidden:!this.convertStringToBoolean(this.isLocationForProduct),
                pdfwidth:100
            },
            {
                header: 'Pack Wareshouse',
                width: 200,
                hidden:!isPckWarehouse|| Wtf.account.companyAccountPref.interloconpick ,  //if without batch then hidden batch column
                dataIndex: 'packwarehouse',
                renderer: Wtf.comboBoxRenderer(this.packWarehouse),
                editor: this.readOnly ? "" : this.packWarehouse
            },
            {
                header: 'Pack Location',
                width: 200,
                hidden: !isPackLoc||!Wtf.account.companyAccountPref.interloconpick , //if without batch then hidden batch column
                dataIndex: 'packlocation',
                renderer: Wtf.comboBoxRenderer(this.packLocation),
                editor: this.readOnly ? "" : this.packLocation
            },
            {
                header: WtfGlobal.getLocaleText("acc.inventorysetup.row"),
                dataIndex: 'rowName',
                width:cmDefaultWidth,
                hidden:!this.convertStringToBoolean(this.isRowForProduct),
                pdfwidth:100
            },{
                header: WtfGlobal.getLocaleText("acc.inventorysetup.rack"),
                dataIndex: 'rackName',
                width:cmDefaultWidth,
                hidden:!this.convertStringToBoolean(this.isRackForProduct),
                pdfwidth:100
            },{
                header: WtfGlobal.getLocaleText("acc.inventorysetup.bin"),
                dataIndex: 'binName',
                width:cmDefaultWidth,
                hidden:!this.convertStringToBoolean(this.isBinForProduct),
                pdfwidth:100,
                summaryRenderer:function(v){
                    return "<div style = 'float:right;'><b>Balance :</b></div>" //WtfGlobal.getCurrencyFormatWithoutSymbol(v,  Wtf.companyPref.quantityDecimalPrecision)
                }
            },{
                header:(Wtf.jobWorkInFlowFlag!= undefined && Wtf.jobWorkInFlowFlag==true)? "Challan No": WtfGlobal.getLocaleText("acc.field.lotBatch"),
                dataIndex: 'batchName',
                width:cmDefaultWidth,
                sortable:false,
                hidden:!this.convertStringToBoolean(this.isBatchForProduct),
                pdfwidth:50    
             },{
                header:WtfGlobal.getLocaleText("acc.field.MfgDate"),
                dataIndex:'mfgdate',
                hidden:!this.convertStringToBoolean(this.isBatchForProduct),
                width:cmDefaultWidth,
                renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.field.ExpDate"),
                dataIndex:'expdate',
                hidden:!this.convertStringToBoolean(this.isBatchForProduct),
                width:cmDefaultWidth,
                renderer:WtfGlobal.onlyDateDeletedRenderer    
            },{
                header: WtfGlobal.getLocaleText("acc.productList.gridAvailableQty"),
                dataIndex: 'avialblequantity',
                sortable:false,
//                align:"right",
                width:cmDefaultWidth,
                summaryType : 'sum',
                pdfwidth:50,
                renderer:function(val){
                    return parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) // WtfGlobal.getCurrencyFormatWithoutSymbol(v, Wtf.companyPref.quantityDecimalPrecision)
                }
            },{     
                header:WtfGlobal.getLocaleText("acc.product.gridQty"),
                dataIndex:"quantity",
                hidden:(!this.isLocationForProduct && !this.isWarehouseForProduct && !this.isBatchForProduct && this.isSerialForProduct && !this.isRowForProduct && !this.isRackForProduct && !this.isBinForProduct),
                width:100,
                editor:this.readOnly?"":this.serialQty=new Wtf.form.NumberField({
                    allowNegative: false,
                    decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
                }),
                renderer:function(val){
                     if(val == ""){
                        return val;
                     }else{
                        return parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) // WtfGlobal.getCurrencyFormatWithoutSymbol(v, Wtf.companyPref.quantityDecimalPrecision)
                    }
                }
          
            },{
                header: WtfGlobal.getLocaleText("acc.stockrequest.Serials"),
                dataIndex: 'serialno',
                width:cmDefaultWidth,
                sortable:false,
                hidden:!this.convertStringToBoolean(this.isSerialForProduct),
                editor:this.readOnly?"":new Wtf.form.TextField({
                name:'memo'
            
            })
                
            });
        this.gridcm= new Wtf.grid.ColumnModel(this.gridColumnModelArr);
        this.grid=null; 
        if(this.isBatchForProduct && (this.moduleid==Wtf.Acc_Delivery_Order_ModuleId || this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_Sales_Order_ModuleId
            || this.moduleid==Wtf.Acc_Cash_Sales_ModuleId)){
            this.grid = new Wtf.KwlEditorGridPanel({
                autoScroll:true,
                bodyStyle: 'padding: 15px; overflow: auto;',
                clicksToEdit:1,
                height:300,
                //                tbar:tbarArray,
                store: this.store,
                cm: this.gridcm,
                sm:this.sm,
                border : false,
                loadMask : true,
                serverSideSearch:true,
                displayInfo: true,
                searchField:"batchName",
                viewConfig: {
                    emptyText:WtfGlobal.getLocaleText("acc.common.norec")
                }
            });
        }
        else{
            this.grid = new Wtf.grid.EditorGridPanel({
                autoScroll:true,
                clicksToEdit:1,
                height:250,
                store: this.store,
                cm: this.gridcm,
                border : false,
                loadMask : true,
                viewConfig: {
                    emptyText:WtfGlobal.getLocaleText("acc.common.norec")
                }
            });
        }
            this.loadStore();
                       
          this.store.on('load', function () {
              this.LoadConfigurationAfterStoreLoad();
              this.LoadCustomFields();
              this.grid.getView().refresh();
            if (this.moduleid == Wtf.Acc_Delivery_Order_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId) {
                this.addTotmpStore(null, false);
            }
         },this)
         
    },
        LoadConfigurationAfterStoreLoad:function(){
        var batchrecords="";
        if(this.batchDetails!=undefined && this.batchDetails.length>1){
            batchrecords= eval('(' + this.batchDetails + ')');
        }
        var recordQuantity=batchrecords.length;
        if(batchrecords.length!=0){
            for(var i=0;i<recordQuantity;i++){
                var batchObj=batchrecords[i];
                this.addGridRec(batchObj);
            }
        }
        var rowsCnt=this.grid.getStore().getCount();
        
        for(var i=0;i<rowsCnt;i++){
           var record = this.grid.getStore().getAt(i);
           if(record.data.quantity>0){
               this.sm. selectRow(i,true);
           }

        }
    },
    addGridRec: function (record) {
        var rec = this.record;
        var size = this.store.getCount();
            
        var Record = this.store.reader.recordType,f = Record.prototype.fields, fi = f.items, fl = f.length;
        var values = {},blankObj={};
        for(var j = 0; j < fl; j++){
            f = fi[j];
            if(f.name!='rowid') {
                blankObj[f.name]='';
            }
        }
        rec = new rec(blankObj);
       
        rec.beginEdit();
        //   var fields = this.store.fields;
        var Record = this.store.reader.recordType,fields = Record.prototype.fields, fi = fields.items, fl = fields.length;
        for (var x = 0; x < fl; x++) {
            var value = "";
            if (record != undefined && record != "") {
                if (fields.get(x).type == "date") {
                    value = record[fields.get(x).name];
                    if (value != "" && value!=undefined)
                        value = new Date(value);
                }
                else {
                    if(record && record[fields.get(x).name] != "" && record[fields.get(x).name] != undefined)
                        value = unescape(record[fields.get(x).name]); //for saving the serial no. its showing %20 in space
                }
            }
            
            if ((record == "" || record == undefined)) {
                if (fields.get(x).name == "location" && (this.defaultLocation != undefined && this.defaultLocation != "" && this.isLocationForProduct))
                    rec.set(fields.get(x).name, this.defaultLocation);
                else if (fields.get(x).name == "warehouse" && (this.defaultWarehouse != undefined && this.defaultWarehouse != "" && this.isWarehouseForProduct))
                    rec.set(fields.get(x).name, this.defaultWarehouse);
                else if (fields.get(x).name == "packwarehouse" && Wtf.account.companyAccountPref.pickpackship && !Wtf.account.companyAccountPref.interloconpick && Wtf.account.companyAccountPref.packingstore != undefined)
                    rec.set(fields.get(x).name, Wtf.account.companyAccountPref.packingstore);  
                else if (fields.get(x).name == "packlocation" && Wtf.account.companyAccountPref.pickpackship && Wtf.account.companyAccountPref.interloconpick && Wtf.account.companyAccountPref.packinglocation != undefined)
                    rec.set(fields.get(x).name, Wtf.account.companyAccountPref.packinglocation);  //set  serialno as assetid
                else if (fields.get(x).name == "serialno" && (this.assetId != undefined && this.assetId != ""))
                    rec.set(fields.get(x).name, this.assetId);  //set  serialno as assetid
                else
                    rec.set(fields.get(x).name, value);
            } else {
                rec.set(fields.get(x).name, value);
                rec.set('packwarehouse', Wtf.account.companyAccountPref.packingstore);
                if (fields.get(x).name == "packwarehouse" && Wtf.account.companyAccountPref.pickpackship && !Wtf.account.companyAccountPref.interloconpick && Wtf.account.companyAccountPref.packingstore != undefined)
                    rec.set(fields.get(x).name, Wtf.account.companyAccountPref.packingstore);  
                else if (fields.get(x).name == "packlocation" && Wtf.account.companyAccountPref.pickpackship && Wtf.account.companyAccountPref.interloconpick && Wtf.account.companyAccountPref.packinglocation != undefined)
                    rec.set(fields.get(x).name, Wtf.account.companyAccountPref.packinglocation);  //set  serialno as assetid
            }
            if (fields.get(x).name == 'quantity'){
                var remaningQty=0;
                if (record != undefined){
                            if(!this.isSerialForProduct){
                                value = parseFloat(value).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                            }else{
                                value = parseInt(value);
                            }
                            rec.set(fields.get(x).name, (parseFloat(this.quantity)<=parseFloat(value) && !this.isSerialForProduct)? this.quantity:value);
                            }
                        else{
                            if(!this.isSerialForProduct){
                                rec.set(fields.get(x).name,parseFloat(this.quantity-remaningQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
                            }else{
                                rec.set(fields.get(x).name,parseInt(this.quantity-remaningQty));
                            }
                        }
            }
           

        }
//        for (var key in record) {
//            if (key.indexOf('Custom') != -1 && record[key] != undefined) { // 'Custom' prefixed already used for custom fields/ dimensions
//                //  recObj[key] = record[key+"_Value"];
//                if (record[key] != "null" && record[key] != "" && record[key] != "undefined" && record[key] != "NaN") {
//                    rec.set(key, record[key]);
//                } else {
//                    rec.set(key, "");
//                }
//
//            }
//        }
//        if (record != "" && record != undefined) {
//            var data = record.customfield;
//            if (data != undefined & data != "") {
//                for (var i = 0; i < data.length; i++) {
//                    var value = data[i].fieldname;
//                    value = data[i][value];
//                    value = data[i][value];
//                    if (value != "" && value != "undefined" && value != "NaN" && value != "null") {
//                        if (data[i].xtype == "3") {
//                            value = parseInt(value);
//                            value = new Date(value);
//                        }
//                        rec.set(data[i].fieldname, value);
//                    } else {
//                        rec.set(data[i].fieldname, "");
//                    }
//                }
//            }
//        }
        rec.endEdit();
        rec.commit();
        var recordFound=false;    
       
        
        this.store.each(function(record){
            if(record.data.warehouse == rec.get('warehouse') && record.data.location == rec.get('location') && record.data.row == rec.get('row') && record.data.rack == rec.get('rack') && record.data.bin == rec.get('bin') && record.data.purchasebatchid == rec.get('purchasebatchid')){
               record.set('quantity',rec.get('quantity')*1);
                record.set('serialNames',rec.get('serialNames'));
                record.set('serialno',rec.get('serialno'));
                record.set('serialDetails',rec.get('serialDetails'));
                if(this.isEdit && (this.moduleid!=Wtf.Acc_Cash_Sales_ModuleId && this.moduleid!=Wtf.Acc_Invoice_ModuleId && this.moduleid!=Wtf.Acc_Delivery_Order_ModuleId)){
                    record.set('avialblequantity',(this.isSerialForProduct ? (rec.get('avialblequantity')*1) :(rec.get('quantity')*1))+(record.get('avialblequantity')*1));
                 }
                recordFound=true;
                return;

            }
//Commenting this code due to an issue in SDP-15661 issue will be handled in ERP-39951
//            else if (this.isEdit && (this.moduleid==Wtf.Acc_Cash_Sales_ModuleId) && !this.isSerialForProduct && !this.isBatchForProduct && ((rec.get("location") !=undefined && rec.get("location") !="") || (rec.get("warehouse") !=undefined && rec.get("warehouse") !=""))) {
//                    record.set('avialblequantity', this.defaultAvailbaleQty);
//                }
            
        },this);
        
        if(!recordFound && this.moduleid!=Wtf.Acc_Sales_Order_ModuleId && this.moduleid!=Wtf.Acc_Delivery_Order_ModuleId && this.moduleid!=Wtf.Acc_Invoice_ModuleId && this.moduleid!=Wtf.Acc_Cash_Sales_ModuleId){  
            this.store.add(rec);
        }
     
     
    },
    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var rec=this.store.getAt(i);
    },
    updateRow:function(obj){  
        if(obj!=null){
            var rec=obj.record;
            if(obj.field == "quantity"){
                if(obj.value > obj.record.get("avialblequantity") ){
                    var isNegativeStockAllowedforProduct=((this.isLocationForProduct || this.isWarehouseForProduct) && !this.isSerialForProduct && !this.isBatchForProduct);
                    if(!(isNegativeStockAllowedforProduct && Wtf.account.companyAccountPref.isnegativestockforlocwar)){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.stockrequest.Quantitycannotbemorethanavailablequantity")], 2);
                        rec.set("quantity", obj.originalValue);
                        return;
                    }
                    
                }
                if(!this.isSerialForProduct && this.isBatchForProduct && (this.moduleid==Wtf.Acc_Delivery_Order_ModuleId || this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_Sales_Order_ModuleId || this.moduleid==Wtf.Acc_Cash_Sales_ModuleId)){
                    this.addTotmpStore(obj,true);
                }
            } 
        }    
            
    },
    checkrecord:function(obj){
       
            if(obj!=null){
                
                 /*
                 * We can select value of custom field only in case of product opening.
                 */
                if(this.moduleid != Wtf.Acc_Product_Master_ModuleId && obj.field.search('Custom_')>=0){
                    return false;
                }
                
                if(obj.field=="quantity"){
                     obj.record.set("serialno", "");
                     return; 
                }
                if(obj.field=="serialno"){
                    if(obj.record.get("quantity")=="" ||obj.record.get("quantity")==undefined){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inventorysetup.warning.quantity.empty")], 2);
                        return false;
                    }
                    obj.cancel=true;
                    var expstartdate=null; 
                    var expendate=null;   
                    if(obj.record.get("expstart")=="" ||obj.record.get("expstart")==undefined);
                    {
                        obj.record.set("expstart", Wtf.serverDate.clearTime(true));
                        expstartdate=Wtf.serverDate.clearTime(true);
                    }
                    if(this.warrantyperiodsal=="" || this.warrantyperiodsal==undefined) {
                        obj.record.set("expend", Wtf.serverDate.clearTime(true));
                        expendate=Wtf.serverDate.clearTime(true);
                    }else
                    {
                        expendate = new Date(obj.record.get("expstart")).add(Date.DAY,this.warrantyperiodsal);
                        obj.record.set("expend",expendate.clearTime(true));
                        
                    }
                    this.openSerialWindow(obj,expstartdate,expendate);
//                    this.addTotmpStore(obj);
                    return; 
                   
                }
            }
           
    },
    
     openSerialWindow:function(obj,expstartdate,expendate){ 
        //      if(true){
        this.serialSelectWindow = new Wtf.account.SerialSelectWindow({
            id: 'serialSelectWindow',
            title:WtfGlobal.getLocaleText("acc.field.SelectSerialNo"),
            border: false,
            obj:obj,
            modal : true,
            moduleid:this.moduleid,
            isConsignment:this.isConsignment,
            isEdit:this.isEdit,
            //refno:this.refno,
            mainproduct : this.mainassemblyproduct,     
            isUnbuildAssembly : this.isUnbuildAssembly, 
            subproduct : this.productid,               
            linkflag:this.linkflag,
            documentid:this.documentid,
            customerID:this.customerID,
            transactionid:this.transactionid,
            productid:this.productid,
            isDO:this.isDO,
            copyTrans:this.copyTrans,
            isForconsignment:this.isForconsignment,
            billid:this.billid,
            linkedFrom:this.linkedFrom,
            quantity:obj.record.get("quantity"),
            expstartdate:expstartdate,
            expendate:expendate,
            isBatchForProduct:this.isBatchForProduct,
            loadedStoreCount:this.loadedStoreCount,
            isAutoFillBatchDetail:true,
            store:this.store,
            grid:this.grid,
            layout:'border',
            docrowid:((this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId) && this.isEdit)?this.docrowid : ""
        });
            
        this.serialSelectWindow.show(); 
        
        this.serialSelectWindow.on("beforeclose",function(){
            
            var isfromSubmit=this.serialSelectWindow.isfromSubmit;
            if(this.isBatchForProduct && !this.isShippingDO){  //as while clicking on the cancel icon it was adding the reocrd in batchdetail json 
                 this.addTotmpStore(obj);
            }
        },this);
     },
     validateBatchSerialDetails:function(){
         var store=this.grid.getStore();
         var enteredQuantity=0;
         var isInvalidRecordFound= false;
         store.each(function(rec){
            if(rec.data.quantity !=undefined && rec.data.quantity != ""){
               enteredQuantity+=parseFloat(rec.data.quantity);                    
            }
            if(rec.data.quantity !=undefined && rec.data.quantity != "" && rec.data.quantity > 0 && rec.data['isSerialForProduct'] == true && (rec.data.serialno == "" || rec.data.quantity!=rec.data.serialno.split(",").length)){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.batchserial.serialdetail")],2);
                isInvalidRecordFound=true;
                return false; 
            }
            
            else if(this.packWarehouse && this.moduleid==Wtf.Acc_Delivery_Order_ModuleId){
                if(Wtf.account.companyAccountPref.pickpackship && !Wtf.account.companyAccountPref.interloconpick){
                    if(Wtf.account.companyAccountPref.packingstore==undefined || Wtf.account.companyAccountPref.packingstore==""){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.pickpackship.msg4")+" "+"in"+" "+WtfGlobal.getLocaleText("acc.field.SystemControls") ], 2);
                         return false;
                    }else if(rec.data['packwarehouse']=="" || rec.data['packwarehouse'] == undefined){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.packwarehouse.empty") + " " + (i + 1)], 2);
                        //alert("packwarehouse Should not empty. Please check row " +i+1)    
                         return false;
                    } 
                }
            }
        });
        /**
         * Checking wether the current module belongs to the module in which this functionality is implemented.If yes then setting modulecondition=true ERM-319
         * @type Boolean|Boolean
         */
        var modulecondition = false;
        if (CompanyPreferenceChecks.differentUOM()) {
            var moduleArray = [Wtf.Acc_Sales_Order_ModuleId, Wtf.Acc_Purchase_Order_ModuleId, Wtf.Acc_Goods_Receipt_ModuleId, Wtf.Acc_Sales_Return_ModuleId, Wtf.Acc_Purchase_Return_ModuleId, Wtf.Acc_Delivery_Order_ModuleId, Wtf.Acc_Invoice_ModuleId, Wtf.Acc_Vendor_Invoice_ModuleId, Wtf.Acc_Cash_Purchase_ModuleId,Wtf.Acc_Cash_Sales_ModuleId];
            if (moduleArray.indexOf(this.moduleid) != -1) {
                modulecondition = true;
            }
        }
        if (this.isShippingDO && this.isShippingDO == true && (!isInvalidRecordFound && getRoundofValue(enteredQuantity) != getRoundofValue(this.shipquantity))) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inventorysetup.warning.quantity.equal")], 2);
                return false;            
        } else if (!isInvalidRecordFound && getRoundofValue(enteredQuantity).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) != this.quantity && (!CompanyPreferenceChecks.differentUOM() || (CompanyPreferenceChecks.differentUOM() && !modulecondition && !Wtf.isEmpty(this.allowUserToEditQuantity) && !this.allowUserToEditQuantity))) {            //not displaying "Quantity Should be equal to entered quantity for product." warning message when "Variable UOM" check is enabled and moduledid belongs to the module in which this functionallity is implemented.ERM-319
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inventorysetup.warning.quantity.equal")], 2);
            return false;
        } else if(this.packWarehouse && Wtf.account.companyAccountPref.pickpackship && this.moduleid==Wtf.Acc_Delivery_Order_ModuleId && (this.parentObj.pickpacklocation=="" || this.parentObj.pickpacklocation==undefined)){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.product.packagingwarehouse")],2);
                        return false;
                    }
        else if(isInvalidRecordFound){
            return false;
        }
        return true;
     },
     addTotmpStore:function(obj,isreload){
//        var selected = this.sm.getSelections();
//        var cnt = selected.length;
        if(obj!=null && obj.field == "quantity" || obj!=null && obj.field == "serialno"){
            //            for(var i=0;i<cnt;i++){ //getting all selected records id and transaction no
            var rec=obj.record;
            var indx= this.tmpStore.find('id',rec.data.id);
            if(indx==-1){
                this.tmpStore.insert(0,rec);
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Record is already selected "], 2);
                return false;
            }
            //            }
        }
//            if(!isreload){
//            this.store.removeAll();
//            this.store.load();
            
//            this.store.on("load",function(){
                var rowsCnt=this.store.getCount();
                for(var i=0;i<rowsCnt;i++){
                    var record = this.store.getAt(i);
                    var indx1= this.tmpStore.find('id',record.data.id);
                    if(indx1!=-1){
                        var recordTmp = this.tmpStore.getAt(indx1);
                        if(recordTmp.data.quantity>0){
                            record.set("quantity", recordTmp.data.quantity);
                            record.set("serialno", recordTmp.data.serialno);
                            record.set("serialDetails", recordTmp.data.serialDetails);
                            this.sm. selectRow(i,true);
                        }
                    }
//                }
//            },this);
            
            this.grid.getView().refresh(); 
        }
//        else{
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Please select record to add "], 2);
//            return false;
//        }
    },
     addTomainStore:function(){
      
        var  cnt=0;
        var store=this.tmpStore;
        var mnStore=this.grid.getStore();
        store.each(function(rec){
            var indx=mnStore.find('id',rec.data.id);
            if(indx==-1){
                mnStore.insert(cnt,rec);
                cnt++; 
            }
        });
    //alert(this.store.getCount());
    },
     convertStringToBoolean:function(value){
        if(typeof value=="string")
        {
            if(value=="true")
                return true;
            else
                return false;
         
        }
        else
            return value;
    },
      getBatchDetails:function(){
        this.store.each(function(rec){
            if(rec.data.rowid==undefined){
                rec.data.rowid='';
            }
        },this);
        this.store.each(function(rec){
            if((rec.data.documentid==undefined||rec.data.documentid=="")&&this.moduleid==53){
                rec.data.documentid=this.documentid;
            }
        },this);
        var arr=[];
        var inculelast=true;
        this.store.each(function(rec){
           
            if (rec.data.serialno != "") {
                rec.data[CUSTOM_FIELD_KEY] = Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, this.module).substring(13));
            }
            // var transdate=(this.isEdit?WtfGlobal.convertToGenericDate(this.record.data.date):WtfGlobal.convertToGenericDate(new Date()));
              if(rec.data.quantity !=undefined && rec.data.quantity != ""){
                  arr.push(this.store.indexOf(rec));
              }
            
        }, this);
        var jarray=WtfGlobal.getJSONArray(this.grid,inculelast,arr);      
        return jarray;
    },
     /**
     * Auto populate custom field value from Line level to serial window level.
     */
    populateCustomFieldValue: function(grid) {
        var GlobalcolumnModel = GlobalColumnModel[this.module];
        if (GlobalcolumnModel) {
            for (var cnt = 0; cnt < GlobalcolumnModel.length; cnt++) {
                var fieldname = GlobalcolumnModel[cnt].fieldname;
                var iscustomfield = GlobalcolumnModel[cnt].iscustomfield;
                var value = this.lineRec.data[fieldname];
                var dropDowntype = false;
                if (value != undefined && value != "") {
                    if (GlobalcolumnModel[cnt].fieldtype == Wtf.CustomFieldType.SingleSelectDropdown || GlobalcolumnModel[cnt].fieldtype == Wtf.CustomFieldType.MultiSelectDropdown) {
                        value = this.getValueForDimension(fieldname, value);
                        dropDowntype = true;
                    }
                    var array = grid.store.data.items;
                    if (array.length > 0) {
                        for (var i = 0; i < array.length - 0; i++) {
                            for (var k = 0; k < grid.colModel.config.length; k++) {
                                if (grid.colModel.config[k].dataIndex == fieldname) {
                                    var gridRecord = grid.store.getAt(i);
                                    if (dropDowntype && grid.colModel.config[k].editor && grid.colModel.config[k].editor.field.store) {
                                        var store = grid.colModel.config[k].editor.field.store;
                                        var valArr = value.split(',');
                                        var ComboValueID = "";
                                        for (var index = 0; index < valArr.length; index++) {
                                            var recCustomCombo = WtfGlobal.searchRecord(store, valArr[index], "name");
                                            if (recCustomCombo)
                                                ComboValueID += recCustomCombo.data.id + ',';
                                        }
                                        if (ComboValueID.length > 0)
                                            ComboValueID = ComboValueID.substring(0, ComboValueID.length - 1);
                                        gridRecord.set(fieldname, ComboValueID);
                                    } else {
                                        gridRecord.set(fieldname, value);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    },
    /*
     * Returns value for drop down custom fields from group grid 
     */
    getValueForDimension: function(fieldName, value) {
        var grid = this.parentGrid;
        if (grid) {
            var array = grid.store.data.items;
            if (array.length > 1) {
                for (var i = 0; i < array.length - 1; i++) {
                    for (var k = 0; k < grid.colModel.config.length; k++) {
                        if (grid.colModel.config[k].editor && grid.colModel.config[k].editor.field.store && grid.colModel.config[k].dataIndex == fieldName) {
                            var store = grid.colModel.config[k].editor.field.store;
                            var gridRecord = grid.store.getAt(i);
                            var valArr = value.split(',');
                            var returnData = "";
                            for (var index = 0; index < valArr.length; index++) {
                                var recCustomCombo = WtfGlobal.searchRecord(store, valArr[index], "id");
                                if (recCustomCombo)
                                    returnData += recCustomCombo.data.name + ',';
                            }
                            return returnData;
                        }
                    }
                }
            }
        }
    },
    LoadCustomFields: function() {
        if (this.isSerialForProduct) {
            this.gridColumnModelArr = WtfGlobal.appendCustomColumn(this.gridColumnModelArr, GlobalColumnModel[this.module], undefined, undefined, this.readOnly);
        }
        this.gridcm = new Wtf.grid.ColumnModel(this.gridColumnModelArr);
        this.grid.reconfigure(this.grid.getStore(), this.gridcm);

        var CustomtotalStoreCount = 0;
        var CustomloadedStoreCount = 0;

        for (var j = 0; j < this.gridColumnModelArr.length; j++) {
            if (this.gridColumnModelArr[j].dataIndex.indexOf('Custom_') != -1 && (this.gridColumnModelArr[j].fieldtype === 4 || this.gridColumnModelArr[j].fieldtype === 7)) {
                CustomtotalStoreCount++;
                this.gridColumnModelArr[j].editor.field.store.on('load', function() {
                    CustomloadedStoreCount++;
                    if (CustomtotalStoreCount === CustomloadedStoreCount && this.grid != undefined) {
                        if (this.moduleid == Wtf.Acc_Goods_Receipt_ModuleId || this.moduleid == Wtf.Acc_Delivery_Order_ModuleId ||
                                this.moduleid == Wtf.Acc_Sales_Return_ModuleId || this.moduleid == Wtf.Acc_Purchase_Return_ModuleId ||
                                this.moduleid == Wtf.Acc_ConsignmentRequest_ModuleId || this.moduleid == Wtf.Acc_ConsignmentDeliveryOrder_ModuleId ||
                                this.moduleid == Wtf.Acc_ConsignmentInvoice_ModuleId || this.moduleid == Wtf.Acc_ConsignmentSalesReturn_ModuleId ||
                                this.moduleid == Wtf.Acc_Consignment_GoodsReceiptOrder_ModuleId || this.moduleid == Wtf.Acc_Consignment_GoodsReceipt_ModuleId || this.moduleid == Wtf.Acc_ConsignmentPurchaseReturn_ModuleId || this.moduleid == Wtf.Acc_ConsignmentVendorRequest_ModuleId ||
                                this.moduleid == Wtf.Acc_Lease_DO || this.moduleid == Wtf.Acc_Lease_Return) {
                            this.populateCustomFieldValue(this.grid);
                        }
//                            this.grid.getView().refresh(); 
                    }
                }, this)
            }

        }
    }
});

