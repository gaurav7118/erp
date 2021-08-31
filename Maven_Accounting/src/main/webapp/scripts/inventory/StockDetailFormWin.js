/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * Sample Code for How to use it
 * 
 * 
 * this.detailWin = new Wtf.StockTransferDetailWin({
            WinTitle : winTitle, // Window Tital
            WinDetail: winDetail, // Window Detail
            TotalTransferQuantity: maxQtyAllowed, // quantity in stock uom for transfer 
            ProductId:itemId,
            FromStoreComboStore: this.parent.fromstoreCombo.store, // FOR ADDING ISSUING STORE COMBO IN FORM to CHANGE STORE 
            FromStoreId: fromStoreId, // FOR BY DEFAULT Loading GRID
            ToStoreComboStore: this.parent.fromstoreCombo.store, // FOR ADDING COLLECT STORE COMBO IN FORM to CHANGE STORE 
            ToStoreId: fromStoreId,  // FOR SHOW USING COLLECT LOCATION
            isBatchForProduct: isBatchEnable,  // true-false  FOR SHOW AND HIDE BATCH  FUNCTIONALITY IN THIS FORM
            isSerialForProduct : isSerialEnable, // true-false FOR SHOW AND HIDE SERIAL FUNCTIONALITY IN THIS FORM
            GridStoreURL:"INVStockLevel/getStoreProductWiseDetailList.do", //  grid url for loading grid
            GridStoreExtraParams:{userId: loginid}  // parameter object Extra parameter for grid loading
            StockDetailArray:record.get("stockDetails"),  // MUST BE ARRAY
            buttons:[{
                text:"Save",
                handler:function (){
                    if(this.detailWin.validateSelectedDetails()){
                        var detailArray = this.detailWin.getSelectedDetails();
                        record.set("stockDetails","");
                        record.set("stockDetails",detailArray);
                        this.detailWin.close();
                    }else{
                        return;
                    }
                },
                scope:this
            },{
                text:"Cancel",
                handler:function (){
                    this.detailWin.close();
                },
                scope:this
            }]
        })
        this.detailWin.show();
 * 
 * 
 */



Wtf.StockTransferDetailWin = function (config){
    Wtf.apply(this,{
        title : WtfGlobal.getLocaleText("acc.stockrequest.StockDetail"),
        modal : true,
        iconCls : 'iconwin',
        minWidth:75,
        width : 950,
        height: 500,
        resizable :true,
        scrollable:true,
        layout:'fit',
        border:false,
        listeners: {
            move: function(theWin,xP,yP,theOp) {
                if((xP >=730) || (yP <=1)) {
                theWin.setPosition(0,0);
//                    alert(xP+","+yP);
                }
            }
        },
        WinTitle : "Select Product Inventory Detail",
        WinDetail: "Select Product Inventory Detail",
        TotalTransferQuantity: 0,
        ProductId:null,
        FromStoreId: null,
        ToStoreId: null,
        isBatchForProduct: false,
        isSerialForProduct : false,
        isRowForProduct : false,
        isRackForProduct : false,
        isBinForProduct : false,
        FromStoreCombo: null,
        ToStoreComboStore: null,
        GridStoreURL: null,
        GridStoreExtraParams: null,
        StockDetailArray: null,
        isNegativeAllowed: false,
        DataIndexMapping:{
            detailId:"detailId",
            orderId:"orderId",
            fromLocationId:"fromLocationId",
            fromLocationName:"fromLocationName",
            fromRowId:"fromRowId",
            fromRowName:"fromRowName",
            fromRackId:"fromRackId",
            fromRackName:"fromRackName",
            fromBinId:"fromBinId",
            fromBinName:"fromBinName",
            batchName:"batchName",
            purchasebatchid:"purchasebatchid",
            location:"location",
            warehouse:"warehouse",
            availableSerials:"availableSerials",
            availableSKUs:"availableSKUs",
            availableQty:"availableQty",
            quantity:"quantity",
            toLocationId:"toLocationId",
            toLocationName:"toLocationName",
            toRowId:"toRowId",
            toRowName:"toRowName",
            toRackId:"toRackId",
            toRackName:"toRackName",
            toBinId:"toBinId",
            toBinName:"toBinName",
            serials:"serials",
            serialsId:'serialsId',
            skus:"skus"
        }
    });
    Wtf.StockTransferDetailWin.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.StockTransferDetailWin,Wtf.Window,{
    
    initComponent:function (){
        Wtf.StockTransferDetailWin.superclass.initComponent.call(this);
        this.validateMappings();
        this.GetNorthPanel();
        this.GetAddEditForm();

        this.mainPanel = new Wtf.Panel({
            layout:'border',
            items:[
            this.northPanel,
            this.grid
            ]
        });

        this.add(this.mainPanel);
    },
    validateMappings: function(){
        // filling undefined mappings
        if(this.DataIndexMapping.detailId == undefined){
            this.DataIndexMapping.detailId="detailId"
        }
        if(this.DataIndexMapping.orderId == undefined){
            this.DataIndexMapping.orderId="orderId"
        }
        if(this.DataIndexMapping.fromLocationId == undefined){
            this.DataIndexMapping.fromLocationId="fromLocationId"
        }
        if(this.DataIndexMapping.fromLocationName == undefined){
            this.DataIndexMapping.fromLocationName="fromLocationName"
        }
        if(this.DataIndexMapping.fromRowId == undefined){
            this.DataIndexMapping.fromRowId="fromRowId"
        }
        if(this.DataIndexMapping.fromRowName == undefined){
            this.DataIndexMapping.fromRowName="fromRowName"
        }
        if(this.DataIndexMapping.fromRackId == undefined){
            this.DataIndexMapping.fromRackId="fromRackId"
        }
        if(this.DataIndexMapping.fromRackName == undefined){
            this.DataIndexMapping.fromRackName="fromRackName"
        }
        if(this.DataIndexMapping.fromBinId == undefined){
            this.DataIndexMapping.fromBinId="fromBinId"
        }
        if(this.DataIndexMapping.fromBinName == undefined){
            this.DataIndexMapping.fromBinName="fromBinName"
        }
        if(this.DataIndexMapping.batchName == undefined){
            this.DataIndexMapping.batchName="batchName"
        }
        if(this.DataIndexMapping.purchasebatchid == undefined){
            this.DataIndexMapping.purchasebatchid="purchasebatchid"
        }
        if(this.DataIndexMapping.location == undefined){
            this.DataIndexMapping.location="location"
        }
        if(this.DataIndexMapping.warehouse == undefined){
            this.DataIndexMapping.warehouse="warehouse"
        }
        if(this.DataIndexMapping.availableSerials == undefined){
            this.DataIndexMapping.availableSerials="availableSerials"
        }
        if(this.DataIndexMapping.availableQty == undefined){
            this.DataIndexMapping.availableQty="availableQty"
        }
        if(this.DataIndexMapping.quantity == undefined){
            this.DataIndexMapping.quantity="quantity"
        }
        if(this.DataIndexMapping.toLocationId == undefined){
            this.DataIndexMapping.toLocationId="toLocationId"
        }
        if(this.DataIndexMapping.toLocationName == undefined){
            this.DataIndexMapping.toLocationName="toLocationName"
        }
        if(this.DataIndexMapping.toRowId == undefined){
            this.DataIndexMapping.toRowId="toRowId"
        }
        if(this.DataIndexMapping.toRowName == undefined){
            this.DataIndexMapping.toRowName="toRowName"
        }
        if(this.DataIndexMapping.toRackId == undefined){
            this.DataIndexMapping.toRackId="toRackId"
        }
        if(this.DataIndexMapping.toRackName == undefined){
            this.DataIndexMapping.toRackName="toRackName"
        }
        if(this.DataIndexMapping.toBinId == undefined){
            this.DataIndexMapping.toBinId="toBinId"
        }
        if(this.DataIndexMapping.toBinName == undefined){
            this.DataIndexMapping.toBinName="toBinName"
        }
        if(this.DataIndexMapping.serials == undefined){
            this.DataIndexMapping.serials="serials"
        }
        if(this.DataIndexMapping.serialsId == undefined){
            this.DataIndexMapping.serialsId="serialsId"
        }
        
    },
    
    GetNorthPanel:function (){
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:120,
            border:false,
            bodyStyle:"background-color:white;padding:8px;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(this.WinTitle, this.WinDetail,'images/accounting_image/price-list.gif', true)
        });
    },
    GetAddEditForm:function (){
        
        if(this.FromStoreCombo){
            this.FromStore = this.FromStoreCombo.getValue();
            
            this.FromStoreCombo.on('select', function(){
                this.FromStoreId = this.FromStoreCombo.getValue();
                this.gridStore.baseParams.storeId = this.FromStoreId,
                this.fromLocCmbStore.baseParams.storeid = this.FromStoreId
                this.gridStore.load();
                this.fromLocCmbStore.load();
            }, this)
        }
        
        this.gridRec = new Wtf.data.Record.create([
        {
            name:"detailId"
        },
        {
            name:"orderId"
        },
        {
            name:"fromLocationId"
        },
        {
            name:"fromLocationName"
        },
        {
            name:"fromRowId"
        },
        {
            name:"fromRowName"
        },
        {
            name:"fromRackId"
        },
        {
            name:"fromRackName"
        },
        {
            name:"fromBinId"
        },
        {
            name:"fromBinName"
        },
        {
            name:"batchName"
        },
        {
            name:"purchasebatchid"
        },
        {
            name:"warehouse"
        },
        {
            name:"location"
        },
        {
            name:"availableSerials"
        },
        {
            name:"availableSKUs"
        },
        {
            name:"availableQty"
        },
        {
            name:"quantity"
        },
        {
            name:"toLocationId"
        },
        {
            name:"toLocationName"
        },
        {
            name:"toRowId"
        },
        {
            name:"toRowName"
        },
        {
            name:"toRackId"
        },
        {
            name:"toRackName"
        },
        {
            name:"toBinId"
        },
        {
            name:"toBinName"
        },
        {
            name:"serials"
        },
        {
            name:"serialsId"
        },
        {
            name:"skus"
        },
        {
            name:"tempQuantity"
        },
        {
            name:"manual"
        }
        ]);
        
        this.reader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.gridRec);
        
        var params = {};
        if(this.GridStoreExtraParams){
            params = this.GridStoreExtraParams;
        }
        params.storeId =this.FromStoreId,
        params.productId = this.ProductId
        this.gridStore = new Wtf.data.Store({
            url:this.GridStoreURL,
            reader:this.reader,
            baseParams: params
        });
        
        this.gridStore.on('load', function(){
            if(this.isValidToStoreId()){
                this.locCmbStore.load();
                this.locCmbStore.on('load', function(){
                    this.fillDetails();
                    if(this.isNegativeAllowed){
                        this.addBlankRow();
                    }
                }, this);
                
            }else{
                this.fillDetails();
                if(this.isNegativeAllowed){
                    this.addBlankRow();
                }
            }
        }, this);
        
        this.gridStore.load();
        
        this.locCmbRecord = new Wtf.data.Record.create([
        {
            name: 'id'
        },{
            name: 'name'
        }
        ]);

        this.locCmbStore = new Wtf.data.Store({
            url:  'INVStore/getStoreLocations.do',
            baseParams:{
                storeid:this.ToStoreId
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.locCmbRecord)
        });
        
        this.locCmbStore.load();
            
        this.locCmb = new Wtf.form.ComboBox({
            store : this.locCmbStore,
            typeAhead:true,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 200,
            triggerAction: 'all',
            emptyText:'Select location...'
        });
        
        this.fromLocCmbStore = new Wtf.data.Store({
            url:  'INVStore/getStoreLocations.do',
            baseParams:{
                storeid:this.FromStoreId
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.locCmbRecord)
        });
        
        this.fromLocCmbStore.load();
            
        this.fromLocCmb = new Wtf.form.ComboBox({
            store : this.fromLocCmbStore,
            typeAhead:true,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 200,
            triggerAction: 'all',
            emptyText:'Select location...'
        });
        
        this.rowCmbRecord = new Wtf.data.Record.create([
        {
            name: 'id'
        },{
            name: 'name'
        },{
            name: 'parentid'
        }
        ]);

        this.rowCmbStore = new Wtf.data.Store({
            url:  'ACCMaster/getStoreMasters.do',
            baseParams:{
                transType:'row'
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.rowCmbRecord)
        });
        
        this.rowCmbStore.load();
            
        this.rowCmb = new Wtf.form.ComboBox({
            store : this.rowCmbStore,
            typeAhead:true,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 200,
            triggerAction: 'all',
            emptyText:'Select Row...'
        });

        this.fromRowCmb = new Wtf.form.ComboBox({
            store : this.rowCmbStore,
            typeAhead:true,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 200,
            triggerAction: 'all',
            emptyText:'Select Row...'
        });
        
        this.rackCmbRecord = new Wtf.data.Record.create([
        {
            name: 'id'
        },{
            name: 'name'
        },{
            name: 'parentid'
        }
        ]);

        this.rackCmbStore = new Wtf.data.Store({
            url:  'ACCMaster/getStoreMasters.do',
            baseParams:{
                transType:'rack'
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.rackCmbRecord)
        });
        
        this.rackCmbStore.load();
            
        this.rackCmb = new Wtf.form.ComboBox({
            store : this.rackCmbStore,
            typeAhead:true,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 200,
            triggerAction: 'all',
            emptyText:'Select rack...'
        });
        
        this.fromRackCmb = new Wtf.form.ComboBox({
            store : this.rackCmbStore,
            typeAhead:true,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 200,
            triggerAction: 'all',
            emptyText:'Select rack...'
        });
        this.binCmbRecord = new Wtf.data.Record.create([
        {
            name: 'id'
        },{
            name: 'name'
        },{
            name: 'parentid'
        }
        ]);

        this.binCmbStore = new Wtf.data.Store({
            url:  'ACCMaster/getStoreMasters.do',
            baseParams:{
                transType:'bin'
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.binCmbRecord)
        });
        
        this.binCmbStore.load();
            
        this.binCmb = new Wtf.form.ComboBox({
            store : this.binCmbStore,
            typeAhead:true,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 200,
            triggerAction: 'all',
            emptyText:'Select bin...'
        });
        
        this.fromBinCmb = new Wtf.form.ComboBox({
            store : this.binCmbStore,
            typeAhead:true,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 200,
            triggerAction: 'all',
            emptyText:'Select bin...'
        });

        this.quantityeditor=new Wtf.form.NumberField({
            scope: this,
            allowBlank:false,
            allowDecimals:true,
            decimalPrecision:4,//Wtf.companyPref.quantityDecimalPrecision,
            allowNegative:false
        })
        
        this.sm = new Wtf.grid.RowSelectionModel({
            width:25,
            singleSelect:true
        });
        var defaultWidth = 150
        this.cm = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header:WtfGlobal.getLocaleText("acc.stockrequest.IssueLocation"),
                dataIndex:"fromLocationName",
                width: defaultWidth,
                editor:this.fromLocCmb,
                renderer: function(v){
                    return "<div wtf:qtip='"+v+"'>"+v+"</div>"
                },
                hidden:false
            },{
                header:WtfGlobal.getLocaleText("acc.stockrequest.IssueRow"),
                dataIndex:"fromRowName",
                width: defaultWidth,
                editor:this.fromRowCmb,
                renderer: function(v){
                    return "<div wtf:qtip='"+v+"'>"+v+"</div>"
                },
                hidden : !this.isRowForProduct
            },{
                header:WtfGlobal.getLocaleText("acc.stockrequest.IssueRack"),
                dataIndex:"fromRackName",
                width: defaultWidth,
                editor:this.fromRackCmb,
                renderer: function(v){
                    return "<div wtf:qtip='"+v+"'>"+v+"</div>"
                },
                hidden : !this.isRackForProduct
            },{
                header:WtfGlobal.getLocaleText("acc.stockrequest.IssueBin"),
                dataIndex:"fromBinName",
                width: defaultWidth,
                editor:this.fromBinCmb,
                renderer: function(v){
                    return "<div wtf:qtip='"+v+"'>"+v+"</div>"
                },
                hidden : !this.isBinForProduct
            },{
                header:WtfGlobal.getLocaleText("acc.inventorysetup.batch"),
                dataIndex:"batchName",
                width: defaultWidth,
                renderer: function(v){
                    return "<div wtf:qtip='"+v+"'>"+v+"</div>"
                },
                hidden : !this.isBatchForProduct
            },{
                header:WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header5"),
                dataIndex:"availableQty",
                width: defaultWidth,
                 renderer: function(v){
                    if(v){
                    return parseFloat(v).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)
                    }
                }
            },{
                header:WtfGlobal.getLocaleText("acc.fixed.asset.quantity")+"*",
                dataIndex:"quantity",
                width: defaultWidth,
                editor:this.quantityeditor
            },{
                header:WtfGlobal.getLocaleText("acc.stockrequest.Serials")+"*",
                dataIndex:"serials",
                width: defaultWidth,
                editor:this.readOnly?"":new Wtf.form.TextField({
                    name:'serialName'
                }),
                renderer: function(v,m,r){
                    if(v){
                        var replacedV = WtfGlobal.replaceAll(v, ",", "<br>");
                        return "<div wtf:qtip='"+replacedV+"'>"+v+"</div>"
                    }
                },
                hidden : !this.isSerialForProduct
            },{
                header:(Wtf.account.companyAccountPref.SKUFieldParm)?(Wtf.account.companyAccountPref.SKUFieldRename!="" && Wtf.account.companyAccountPref.SKUFieldRename!= undefined)?Wtf.account.companyAccountPref.SKUFieldRename:WtfGlobal.getLocaleText("acc.product.sku"):WtfGlobal.getLocaleText("acc.product.sku"),
                dataIndex:"skus",
                width: defaultWidth,
                renderer: function(v,m,r){
                    if(v){
                        var replacedV = WtfGlobal.replaceAll(v, ",", "<br>");
                        return "<div wtf:qtip='"+replacedV+"'>"+v+"</div>"
                    }
                },
                readOnly:true,
                hidden:!(Wtf.account.companyAccountPref.SKUFieldParm && this.isSKUForProduct)
            },{
                header:WtfGlobal.getLocaleText("acc.stockrequest.CollectLocation")+"*",
                dataIndex:"toLocationId",
                width: defaultWidth,
                hidden: !this.isValidToStoreId(),
                editor:this.locCmb,
                renderer:function(v,m,r){
                    var val = r.get('toLocationName')
                    return "<div wtf:qtip='"+val+"'>"+val+"</div>"
                }
            },{
                header:WtfGlobal.getLocaleText("acc.stockrequest.CollectRow")+"*",
                dataIndex:"toRowId",
                width: defaultWidth,
                hidden: !this.isRowForProduct || !this.isValidToStoreId(),
                editor:this.rowCmb,
                renderer:function(v,m,r){
                    var val = r.get('toRowName')
                    return "<div wtf:qtip='"+val+"'>"+val+"</div>"
                }
            },{
                header:WtfGlobal.getLocaleText("acc.stockrequest.CollectRack")+"*",
                dataIndex:"toRackId",
                width: defaultWidth,
                hidden: !this.isRackForProduct || !this.isValidToStoreId(),
                editor:this.rackCmb,
                renderer:function(v,m,r){
                    var val = r.get('toRackName')
                    return "<div wtf:qtip='"+val+"'>"+val+"</div>"
                }
            },{
                header:WtfGlobal.getLocaleText("acc.stockrequest.CollectBin")+"*",
                dataIndex:"toBinId",
                width: defaultWidth,
                hidden: !this.isBinForProduct || !this.isValidToStoreId(),
                editor:this.binCmb,
                renderer:function(v,m,r){
                    var val = r.get('toBinName')
                    return "<div wtf:qtip='"+val+"'>"+val+"</div>"
                }
            }]);
        var tbarArr = [];
        if(this.FromStoreCombo){
            tbarArr.push(WtfGlobal.getLocaleText("acc.stockrequest.IssueStore")+": ", this.FromStoreCombo)
        }
        var totalWidth = this.cm.getTotalWidth(false);
        var forceFit = false;
        if(totalWidth/defaultWidth < 7){
            forceFit = true;
        }
        this.grid=new Wtf.grid.EditorGridPanel({
            region: 'center',
            border: false,
            store: this.gridStore,
            cm: this.cm,
            sm:this.sm,
            loadMask : true,
            layout:'fit',
            scrollable: true,
            viewConfig: {
                forceFit: forceFit
            },
            tbar:tbarArr,
            clicksToEdit: 1
        })
        this.grid.on('cellclick',this.handleCellClick,this);
        this.grid.on('afteredit',this.handleAfterEdit,this);
        this.grid.on('beforeedit',this.handleBeforeEdit,this);
        
    },
    addBlankRow: function(){
        var store = this.grid.store;
        var add = false;
        if(store.getCount() > 0){
            var rec = store.getAt(store.getCount() - 1)
            if(rec.get('fromLocationId')){
                add = true;
            }
        }else{
            add = true;
        }
        if(add){
            var blankRec = new this.gridRec({
                detailId:'',
                orderId:'',
                fromLocationId:'',
                fromLocationName:'',
                fromRowId:'',
                fromRowName:'',
                fromRackId:'',
                fromRackName:'',
                fromBinId:'',
                fromBinName:'',
                toLocationId:'',
                toLocationName:'',
                toRowId:'',
                toRowName:'',
                toRackId:'',
                toRackName:'',
                toBinId:'',
                toBinName:'',
                availableQty:0,
                tempQuantity:0,
                quantity :0,
                manual:true
            });
            store.add(blankRec);
        }
    },
    isValidToStoreId: function() {              
        if(this.ToStoreId == '' || this.ToStoreId == null || this.ToStoreId == undefined){
            return false;
        }
        return true;
    },
    isValidFromStoreComboStore: function() {      
        if(this.FromStoreComboStore == '' || this.FromStoreComboStore == null || this.FromStoreComboStore == undefined){
            return false;
        }
        return true;
    },
    isValidToStoreComboStore: function() {       
        if(this.ToStoreComboStore == '' || this.ToStoreComboStore == null || this.ToStoreComboStore == undefined){
            return false;
        }
        return true;
    },
    handleCellClick: function(grid, rowIndex, colIndex, event){
        var store = grid.getStore();
        var rec = store.getAt(rowIndex);
        var dataIndex=grid.getColumnModel().getDataIndex(colIndex);
        if(!rec.get('manual') && (dataIndex == 'fromLocationName' || dataIndex == 'fromRowName'|| dataIndex == 'fromRackName' || dataIndex == 'fromBinName')){
            return false;
        }
        if(dataIndex == 'serials'){
            if(rec.get('quantity') == "" || rec.get('quantity') == 0){
                WtfComMsgBox(["Info", "Please select quantity first"], 0);
                return;
            }
//            this.openSerialSelectionWin(rec);
            
//            this.openSerialWindow(rec,rec.get('expstartdate'),rec.get('expendate')); 
        }
    },
    handleBeforeEdit:function(e) {
        var rec = e.record;
        var value = e.value;
        if(e.field == 'quantity'){
            if(e.value == "" && rec.get('availableQty') == 0){ //this is to check for add case(1st time entry) if avaialble qty is 0 and user tries to fill its quantity then do not allow to change it
                if(!rec.get('manual')){
                    WtfComMsgBox(["Info", "Quantity is not available."], 0);
                    rec.set('quantity', 0);
                    rec.set('serials', '');
                    return false;
                }
            }else{
                var totalAvailableQty=rec.get('availableQty')+ (e.value == "" ? 0 : e.value);
                rec.set('tempQuantity',totalAvailableQty);
            }
        }else if(e.field == 'serials'){
            if(rec.get('quantity') == "" || rec.get('quantity') == 0){
                WtfComMsgBox(["Info", "Please select quantity first"], 0);
                return;
            }
            this.openSerialWindow(e,rec.get('expstartdate'),rec.get('expendate')); 
        }
    },
    handleAfterEdit:function(e) {
        var rec = e.record;
        var value = e.value;
        if(e.field == 'quantity'){
            if (!rec.get('manual') && !this.isNegativeAllowed && e.value > e.record.get('availableQty') && (rec.get("tempQuantity") != "" || rec.get("tempQuantity") != undefined)) {
                if (e.value > rec.get("tempQuantity")) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.stockrequest.Quantitycannotbemorethanavailablequantity")], 0);
                    rec.set('quantity', 0)
                    rec.set('serials', '')
                    return false;
                }
            }
            
            if(!rec.get('manual') && !this.isNegativeAllowed && e.value > e.record.get('availableQty') && (rec.get("tempQuantity")=="" || rec.get("tempQuantity")==undefined)){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.stockrequest.Quantitycannotbemorethanavailablequantity")], 0);
                rec.set('quantity', 0)
                rec.set('serials', '')
                return false;
            }
            
        //            if (Wtf.stockAdjustmentProdBatchQtyMapArr.length > 0) {
        //                for (var x = 0; x < Wtf.stockAdjustmentProdBatchQtyMapArr.length; x++) {
        //                    var recObj = Wtf.stockAdjustmentProdBatchQtyMapArr[x];
        //                    if (recObj != "" && recObj != undefined) {
        //                        if (recObj.productId == this.ProductId && recObj.fromLocationId == rec.get("fromLocationId") && recObj.batchName == rec.get("batchName")) {
        //                            var arrQty = recObj.quantity;
        //                            var filledQty = e.value;
        //                            var availableQty = rec.get("availableQty");
        //                            if (filledQty > availableQty) {
        //                                WtfComMsgBox(["Info", "Quantity can't be more than available quantity"], 0);
        //                                rec.set('quantity', 0)
        //                                rec.set('serials', '')
        //                                return false;
        //                            }
        //                        }
        //                    }
        //                }
        //            }
        //            
        //            rec.set('serials', '');
        }
        //        if(e.field == 'toLocationName'){
        //            rec.set('toLocationId', value)
        //        }
        //        if(e.field == 'toRowName'){
        //            rec.set('toRowId', value)
        //        }
        //        if(e.field == 'toRackName'){
        //            rec.set('toRackId', value)
        //        }
        //        if(e.field == 'toBinName'){
        //            rec.set('toBinId', value)
        //        }
        if(e.field == 'fromLocationName'){
            rec.set('fromLocationName', this.fromLocCmb.getRawValue())
            rec.set('fromLocationId', this.fromLocCmb.getValue())
            this.addBlankRow();
        }
        if(e.field == 'fromRowName'){
            rec.set('fromRowName', this.fromRowCmb.getRawValue())
            rec.set('fromRowId', this.fromRowCmb.getValue())
        }
        if(e.field == 'fromRackName'){
            rec.set('fromRackName', this.fromRackCmb.getRawValue())
            rec.set('fromRackId', this.fromRackCmb.getValue())
        }
        if(e.field == 'fromBinName'){
            rec.set('fromBinName', this.fromBinCmb.getRawValue())
            rec.set('fromBinId', this.fromBinCmb.getValue())
        }
        if(e.field == 'toLocationId'){
            if(rec.get('toLocationId') == rec.get('fromLocationId')){
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.select.diffrentlocation")], 0);
                 rec.set('toLocationName', '');
            }else{
                rec.set('toLocationName', this.locCmb.getRawValue());
            }
        }
        if(e.field == 'toRowId'){
            rec.set('toRowName', this.rowCmb.getRawValue())
        }
        if(e.field == 'toRackId'){
            rec.set('toRackName', this.rackCmb.getRawValue())
        }
        if(e.field == 'toBinId'){
            rec.set('toBinName', this.binCmb.getRawValue())
        }
    },
    validateSelectedDetails: function(){
        var qtyCount = 0;
        var isEmptyCollectLocation = false;
        var isSerialCountMismatch = false;
        var isEmptyCollectRow = false;
        var isEmptyCollectRack = false;
        var isEmptyCollectBin = false;
        this.gridStore.each(function(rec){
            if(rec.get('quantity') > 0){
                qtyCount += rec.get('quantity');
                
                if(this.isSerialForProduct){ 
                    var s = rec.get('serials').split(',')
                    if(!s[0] || s.length != rec.get('quantity')){
                        isSerialCountMismatch = true;
                        return false;
                    }
                }
                if(this.isValidToStoreId()){
                    if(!rec.get('toLocationId') || rec.get('toLocationId') == rec.get('fromLocationId')){
                        isEmptyCollectLocation = true;
                        return false;
                    }
                    if(this.isRowForProduct && !rec.get('toRowId')){
                        isEmptyCollectRow = true;
                        return false;
                    }
                    if(this.isRackForProduct && !rec.get('toRackId')){
                        isEmptyCollectRack = true;
                        return false;
                    }
                    if(this.isBinForProduct && !rec.get('toBinId')){
                        isEmptyCollectBin = true;
                        return false;
                    }
                }
            }
        }, this)
        
        if(isSerialCountMismatch){
            WtfComMsgBox(["Warning", "Serial count is not matched with quantity."], 2);
            return false;
        }
        if(isEmptyCollectLocation){
            WtfComMsgBox(["Warning", "Please select Collect Location for selected records "], 2);
            return false;
        }
        if(isEmptyCollectRow){
            WtfComMsgBox(["Warning", "Please select Collect Row for selected records "], 2);
            return false;
        }
        if(isEmptyCollectRack){
            WtfComMsgBox(["Warning", "Please select Collect Rack for selected records "], 2);
            return false;
        }
        if(isEmptyCollectBin){
            WtfComMsgBox(["Warning", "Please select Collect Bin for selected records "], 2);
            return false;
        }
        if(parseFloat(getRoundofValue(qtyCount).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)) != parseFloat(getRoundofValue(this.TotalTransferQuantity)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.stockrequest.Totalselectedquantitymustequaltotransferquantity")], 2);
            return false;
        }
        
        return true;
    },
    fillDetails: function(){
        if(this.StockDetailArray instanceof Array){
            for(var i=0 ; i< this.StockDetailArray.length ; i++){
                var givenStockDetail = this.StockDetailArray[i];
                var fLocationId = givenStockDetail[this.DataIndexMapping.fromLocationId];
                var fRowId = givenStockDetail[this.DataIndexMapping.fromRowId];
                var fRackId = givenStockDetail[this.DataIndexMapping.fromRackId];
                var fBinId = givenStockDetail[this.DataIndexMapping.fromBinId];
                var batchName = givenStockDetail[this.DataIndexMapping.batchName];
                var manual = givenStockDetail['manual'];
                if(manual){
                    this.addBlankRow();
                    var rec = this.gridStore.getAt(this.gridStore.getCount() - 1);
                    rec.set('quantity', givenStockDetail[this.DataIndexMapping.quantity]);
                    rec.set('fromLocationId', givenStockDetail[this.DataIndexMapping.fromLocationId]);
                    rec.set('location', givenStockDetail[this.DataIndexMapping.fromLocationId]);
                    rec.set('fromLocationName', givenStockDetail[this.DataIndexMapping.fromLocationName]);
                    rec.set('fromRowId', givenStockDetail[this.DataIndexMapping.fromRowId]);
                    rec.set('fromRowName', givenStockDetail[this.DataIndexMapping.fromRowName]);
                    rec.set('fromRackId', givenStockDetail[this.DataIndexMapping.fromRackId]);
                    rec.set('fromRackName', givenStockDetail[this.DataIndexMapping.fromRackName]);
                    rec.set('fromBinId', givenStockDetail[this.DataIndexMapping.fromBinId]);
                    rec.set('fromBinName', givenStockDetail[this.DataIndexMapping.fromBinName]);
                    rec.set('toLocationId', givenStockDetail[this.DataIndexMapping.toLocationId]);
                    rec.set('toLocationName', givenStockDetail[this.DataIndexMapping.toLocationName]);
                    rec.set('toRowId', givenStockDetail[this.DataIndexMapping.toRowId]);
                    rec.set('toRowName', givenStockDetail[this.DataIndexMapping.toRowName]);
                    rec.set('toRackId', givenStockDetail[this.DataIndexMapping.toRackId]);
                    rec.set('toRackName', givenStockDetail[this.DataIndexMapping.toRackName]);
                    rec.set('toBinId', givenStockDetail[this.DataIndexMapping.toBinId]);
                    rec.set('toBinName', givenStockDetail[this.DataIndexMapping.toBinName]);
                    rec.set('manual', true);
                    rec.commit();
                }else{
                    this.gridStore.each(function(rec){
                        if(fLocationId == rec.get('fromLocationId') && fRowId == rec.get('fromRowId') && fRackId == rec.get('fromRackId') && fBinId == rec.get('fromBinId') &&  batchName == rec.get('batchName')){
                            rec.set('quantity', givenStockDetail[this.DataIndexMapping.quantity]);
                            rec.set('toLocationId', givenStockDetail[this.DataIndexMapping.toLocationId]);
                            rec.set('toLocationName', givenStockDetail[this.DataIndexMapping.toLocationName]);
                            rec.set('toRowId', givenStockDetail[this.DataIndexMapping.toRowId]);
                            rec.set('toRowName', givenStockDetail[this.DataIndexMapping.toRowName]);
                            rec.set('toRackId', givenStockDetail[this.DataIndexMapping.toRackId]);
                            rec.set('toRackName', givenStockDetail[this.DataIndexMapping.toRackName]);
                            rec.set('toBinId', givenStockDetail[this.DataIndexMapping.toBinId]);
                            rec.set('toBinName', givenStockDetail[this.DataIndexMapping.toBinName]);
                            rec.set('serials', givenStockDetail[this.DataIndexMapping.serials]);
                            rec.set('serialsId', givenStockDetail[this.DataIndexMapping.serialsId]);
                            rec.commit();
                        }
                    }, this)
                }
            }
        }
        
        this.gridStore.each(function (rec) {
            var prodId = this.ProductId;
            var availableQty = rec.get("availableQty");
            var availableBatch = rec.get("batchName");
            var availableSerials = rec.get("availableSerials");

            if (availableSerials != undefined && availableSerials != "") {
                var serialArr = availableSerials.split(",");
                for (var x = 0; x < serialArr.length; x++) {
                    var curRec = prodId + "#" + availableBatch + "#" + serialArr[x];
                    if (Wtf.stockAdjustmentTempDataHolder.indexOf(curRec) != -1) {
                        availableQty = availableQty - 1;
                    }
                }
            } else {
                if (Wtf.stockAdjustmentProdBatchQtyMapArr.length > 0) {
                    for (var x = 0; x < Wtf.stockAdjustmentProdBatchQtyMapArr.length; x++) {
                        var recObj = Wtf.stockAdjustmentProdBatchQtyMapArr[x];
                        if (recObj != "" && recObj != undefined) {
                            if (recObj.productId == prodId && recObj.fromLocationId == rec.get("fromLocationId") && recObj.batchName == availableBatch) {
                                var alreadySelectedQty = recObj.quantity;
                                availableQty = availableQty - alreadySelectedQty;
                            }
                        }
                    }
                }
            }
            if(availableQty < 0){
                availableQty=0;
            }
            rec.set('availableQty', availableQty);
            rec.commit();
        }, this);
        
    },
    getFromStoreComboValue: function(){
        var val;
        if(this.FromStoreCombo){
            val = this.FromStoreCombo.getValue();
        }
        return val;
    },
    getSelectedDetails: function(){
        var stockDetailArray = [];
        this.gridStore.each(function(rec){
            if(rec.get('quantity') > 0){
                var stockDetail = {};
                stockDetail[this.DataIndexMapping.detailId] = rec.get('detailId');
                stockDetail[this.DataIndexMapping.orderId] = rec.get('orderId');
                stockDetail[this.DataIndexMapping.fromLocationId] = rec.get('fromLocationId');
                stockDetail[this.DataIndexMapping.fromLocationName] = rec.get('fromLocationName');
                stockDetail[this.DataIndexMapping.fromRowId] = rec.get('fromRowId');
                stockDetail[this.DataIndexMapping.fromRowName] = rec.get('fromRowName');
                stockDetail[this.DataIndexMapping.fromRackId] = rec.get('fromRackId');
                stockDetail[this.DataIndexMapping.fromRackName] = rec.get('fromRackName');
                stockDetail[this.DataIndexMapping.fromBinId] = rec.get('fromBinId');
                stockDetail[this.DataIndexMapping.fromBinName] = rec.get('fromBinName');
                stockDetail[this.DataIndexMapping.batchName] = rec.get('batchName');
                stockDetail[this.DataIndexMapping.quantity] = rec.get('quantity');
                stockDetail[this.DataIndexMapping.toLocationId] = rec.get('toLocationId');
                stockDetail[this.DataIndexMapping.toLocationName] = rec.get('toLocationName');
                stockDetail['manual'] = rec.get('manual');
                if(rec.get('toRowId')){ // this for automatic assign in other transaction if rack row bin window only implemented in ILT
                    stockDetail[this.DataIndexMapping.toRowId] = rec.get('toRowId');
                    stockDetail[this.DataIndexMapping.toRowName] = rec.get('toRowName');
                }else{
                    stockDetail[this.DataIndexMapping.toRowId] = rec.get('fromRowId');
                    stockDetail[this.DataIndexMapping.toRowName] = rec.get('fromRowName');
                }
                if(rec.get('toRackId')){
                    stockDetail[this.DataIndexMapping.toRackId] = rec.get('toRackId');
                    stockDetail[this.DataIndexMapping.toRackName] = rec.get('toRackName');
                }else{
                    stockDetail[this.DataIndexMapping.toRackId] = rec.get('fromRackId');
                    stockDetail[this.DataIndexMapping.toRackName] = rec.get('fromRackName');
                    
                }
                if(rec.get('toBinId')){
                    stockDetail[this.DataIndexMapping.toBinId] = rec.get('toBinId');
                    stockDetail[this.DataIndexMapping.toBinName] = rec.get('toBinName');
                }else{
                    stockDetail[this.DataIndexMapping.toBinId] = rec.get('fromBinId');
                    stockDetail[this.DataIndexMapping.toBinName] = rec.get('fromBinName');
                    
                }
                
                stockDetail[this.DataIndexMapping.serials] = rec.get('serials');
                stockDetail[this.DataIndexMapping.serialsId] = rec.get('serialsId');
                
                stockDetailArray.push(stockDetail);
            }
        }, this)
        if(this.isNegativeAllowed){
            stockDetailArray = this.mergeDetails(stockDetailArray);
        }
        return stockDetailArray
    },
    mergeDetails: function(stockDetailArray){
        var details = [];
        if(stockDetailArray instanceof Array){
            var obj = {};
            for(var i=0; i<stockDetailArray.length; i++){
                var detail = stockDetailArray[i];
                var key =  detail[this.DataIndexMapping.fromLocationId]
                +detail[this.DataIndexMapping.fromRowId]
                +detail[this.DataIndexMapping.fromRowName]
                +detail[this.DataIndexMapping.fromRackId]
                +detail[this.DataIndexMapping.toLocationId]
                +detail[this.DataIndexMapping.toRowId]
                +detail[this.DataIndexMapping.toRowName]
                +detail[this.DataIndexMapping.toRackId];
            
                if(obj[key]){
                    var val = obj[key];
                    val[this.DataIndexMapping.quantity] += detail[this.DataIndexMapping.quantity]
                }else{
                    obj[key] = detail; 
                }
            }
            for(key in obj){
                details.push(obj[key]);
            }
        }
        return details;
    },
     openSerialWindow:function(obj,expstartdate,expendate){ 
        var featchIssuedSerial=false;
        var featchIssuedSerialURL="";
        var billid="";
        if(this.moduleid ==Wtf.Acc_Stock_Request_ModuleId && this.type != undefined && this.type == 3 ){ //In Collect Case only we are featching Issued serials
            featchIssuedSerial=true;
            featchIssuedSerialURL="INVGoodsTransfer/getIssuedStockSerialDetail.do";
            billid=obj.record.get('orderId');
        }else if(this.moduleid ==Wtf.Acc_InterStore_ModuleId && this.type != undefined && this.type == 1){
            featchIssuedSerial=true;
            featchIssuedSerialURL="INVGoodsTransfer/getISTIssuedSerialDetailList.do";
            billid=obj.record.get('detailId');
        }
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
            isUnbuildAssembly : false, 
            subproduct : this.ProductId,               
            linkflag:false,
            documentid:this.documentid,
            transactionid:this.transactionid,
            productid:this.ProductId,
            isDO:false,
            copyTrans:false,
            isForconsignment:false,
            billid:billid,
            quantity:obj.record.get("quantity"),
            expstartdate:expstartdate,
            expendate:expendate,
            serialDataReturnTO:'serials',
            serialIdDataReturnTo:'serialsId',
            skuDataReturnTo:'skus',
            isBatchForProduct:this.isBatchForProduct,
            loadedStoreCount:1, // no need to pass from inventory components
            isAutoFillBatchDetail:true,
            featchIssuedSerial:featchIssuedSerial,
            featchIssuedSerialURL:featchIssuedSerialURL,
            store:this.gridStore,
            grid:this.grid,
            layout:'border'
            
        }).show(); 
     }
//    openSerialSelectionWin: function(rowRecord){
//        var currentRowMap=this.ProductId+"#"+rowRecord.get("batchName");
//        var serialSelectionWin = new Wtf.SerialSelectionWin({
//            AvailableSerials :rowRecord.get('availableSerials'),
//            SelectedSerials :rowRecord.get('serials'),
//            AvailableSKUs :rowRecord.get('availableSKUs'),
//            SelectedQty :rowRecord.get('quantity'),
//            currentRowMap:currentRowMap
//        })
//        serialSelectionWin.show();
//        serialSelectionWin.on('serialSelected', function(serials,skus){
//            rowRecord.set('serials', serials);
//            rowRecord.set('skus', skus);
//        }, this)
//    }
});

//////////////////////////////////////////////////////////////

//Wtf.SerialSelectionWin = function (config){
//    Wtf.apply(this,{
//        title : "Select "+config.SelectedQty+" Serials",
//        modal : true,
//        iconCls : 'iconwin',
//        minWidth:75,
//        width : 300,
//        height: 300,
//        resizable :true,
//        scrollable:true,
//        layout:'fit',
//        border:false,
//        buttons :[{
//            text : 'Save',
//            //                iconCls:'pwnd ReasonSubmiticon caltb',
//            scope : this,
//            handler: function(){  
//                var recs = this.serialGrid.getSelectionModel().getSelections();
//                if(recs.length != this.SelectedQty){
//                    WtfComMsgBox(["Alert", "You need to select "+this.SelectedQty+" serial(s)"], 0);
//                    return false;
//                }
//                var serials = "";
//                var skus = "";
//                for (var i = 0; i < recs.length; i++) {
//                    var rec = recs[i];
//                    var srl = rec.get('serial');
//                    var recMap = this.currentRowMap;
//
//                    //                    if (recMap != undefined && recMap != "") {
//                    //                        var fullRecMap = recMap + "#" + srl;
//                    //
//                    //                        if (Wtf.stockAdjustmentTempDataHolder != undefined && Wtf.stockAdjustmentTempDataHolder.length > 0) {
//                    //                            if (Wtf.stockAdjustmentTempDataHolder.indexOf(fullRecMap) != -1) {
//                    //                                WtfComMsgBox(["Alert", "Serial already selected."], 3);
//                    //                                return false;
//                    //                            }
//                    //                        }
//                    //                    }
//
//                    if (serials == "") {
//                        serials = srl;
//                        skus = rec.get('sku')
//                    } else {
//                        serials += "," + srl;
//                        skus += "," + rec.get('sku');
//                    }
//                }
//                this.fireEvent('serialSelected', serials,skus);
//                this.close();
//                
//            }
//        }]
//    });
//    Wtf.SerialSelectionWin.superclass.constructor.call(this,config);
//}
//
//Wtf.extend(Wtf.SerialSelectionWin,Wtf.Window,{
//    
//    initComponent:function (){
//        Wtf.SerialSelectionWin.superclass.initComponent.call(this);
//        this.GetAddEditForm();
//        
//        this.mainPanel = new Wtf.Panel({
//            layout:'border',
//            items:[this.serialGrid]
//        });
//        this.add(this.mainPanel);
//        
//        this.addEvents({
//            serialSelected: true
//        })
//    },
//    GetAddEditForm:function (){
//        var availSerials = this.AvailableSerials;
//        var selectedSerials = this.SelectedSerials;
//        var availSKUs = this.AvailableSKUs;
//        
//        var serialArr = [];
//        var skuArr = [];
//        if(availSerials != "" && availSerials != null && availSerials != undefined){
//            serialArr = availSerials.split(",");
//            skuArr = availSKUs.split(",");
//        }
//        
//        var recTobeRemovedFromAvailableSerialArr=new Array();
//        var recMap = this.currentRowMap;
//        if (recMap != undefined && recMap != "" && serialArr.length > 0 && Wtf.stockAdjustmentTempDataHolder.length > 0) {
//            
//            var selectedSerialArr = new Array();
//            if (selectedSerials != "" && selectedSerials != null && selectedSerials != undefined) {
//                selectedSerialArr=selectedSerials.split(",");
//            }
//            
//            for (var x = 0; x < availSerials.length; x++) {
//                var fullRecMap = recMap + "#" + serialArr[x];
//                if (Wtf.stockAdjustmentTempDataHolder.indexOf(fullRecMap) != -1) {
//                    if(selectedSerialArr.length > 0 && selectedSerialArr.indexOf(serialArr[x]) != -1){
//                    // do nothing if serial isalready selected and came here to edit then do not remove it in order to change serial for edit
//                    }else{
//                        recTobeRemovedFromAvailableSerialArr.push(serialArr[x]);
//                    }
//                    
//                }
//            }
//
//            for(var y = 0; y < recTobeRemovedFromAvailableSerialArr.length ; y++){
//                serialArr.splice(serialArr.indexOf(recTobeRemovedFromAvailableSerialArr[y]),1);
//            }
//        }
//        
//        var selectedRowIndexes = [];
//        if(selectedSerials != "" && selectedSerials != null && selectedSerials != undefined){
//            var selectedSerialArr = selectedSerials.split(",");
//            for(var i=0 ;i<selectedSerialArr.length ; i++){
//                var seletedSerial = selectedSerialArr[i];
//                for(var j=0 ;j<serialArr.length ; j++){
//                    var availSerial = serialArr[j];
//                    if(seletedSerial == availSerial){
//                        selectedRowIndexes.push(j);
//                    }
//                }
//            }
//        }
//        var gridStore = new Wtf.data.SimpleStore({
//            fields:['serial','sku']
//        });
//        if(serialArr != '' && serialArr != null && serialArr != undefined){
//            var serialCmbData = [];
//            for(var i=0 ; i<serialArr.length ; i++){
//                serialCmbData.push([serialArr[i],skuArr[i]])
//            }
//            gridStore.loadData(serialCmbData)
//        }
//        var sm = new Wtf.grid.CheckboxSelectionModel({
//            width:25
//        });
//        
//        //        sm.on("selectionchange", function () {
//        //            var selected = sm.getSelections();
//        //            var recMap = this.currentRowMap;
//        //            if (selected.length > 0) {
//        //                for (var i = 0; i < selected.length; i++) {
//        //                    var serialname = selected[i].get("serial");
//        //                    var fullRecMap = recMap + "#" + serialname;
//        //                    if (Wtf.stockAdjustmentTempDataHolder.length >0 && Wtf.stockAdjustmentTempDataHolder.indexOf(fullRecMap) != -1) {
//        //                        if (selectedSerialArr.length > 0 && selectedSerialArr.indexOf(serialArr[x]) != -1) {
//        //                           this.recToRemoveFromWTfstockAdjustmentTempDataHolder.push(fullRecMap);
//        //                        }
//        //
//        //                    }
//        //                }
//        //            }
//        //        }, this);
//        
//        var cm = new Wtf.grid.ColumnModel([
//            sm,
//            new Wtf.grid.RowNumberer(),
//            {
//                header:"Serial",
//                dataIndex:"serial"
//            },
//            {
//                header:(Wtf.account.companyAccountPref.SKUFieldParm)?(Wtf.account.companyAccountPref.SKUFieldRename!="" && Wtf.account.companyAccountPref.SKUFieldRename!= undefined)?Wtf.account.companyAccountPref.SKUFieldRename:WtfGlobal.getLocaleText("acc.product.sku"):WtfGlobal.getLocaleText("acc.product.sku"),
//                dataIndex:"sku",
//                hidden:!(Wtf.account.companyAccountPref.SKUFieldParm)
//            }]);
//        
//        this.serialGrid=new Wtf.grid.GridPanel({
//            region: 'center',
//            border: false,
//            store: gridStore,
//            cm: cm,
//            sm:sm,
//            loadMask : true,
//            layout:'fit',
//            viewConfig: {
//                forceFit: true
//            }
//        })
//        this.serialGrid.on('render', function(){
//            sm.selectRows(selectedRowIndexes);
//        }, this)
//        
//    }
//});
