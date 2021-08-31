/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 
 */



Wtf.CCStockDetailWin = function (config){
    Wtf.apply(this,{
        title : "Stock Detail",
        modal : true,
        iconCls : 'iconwin',
        minWidth:75,
        width : 950,
        height: 500,
        resizable :true,
        scrollable:true,
        layout:'fit',
        border:false,
        
        WinTitle : "Select Product Inventory Detail",
        WinDetail: "Select Product Inventory Detail",
        MaxAllowedQuantity: 0,
        ProductId:null,
        FromStoreId: null,
        ToStoreId: null,
        isRowForProduct: false,
        isRackForProduct: false,
        isBinForProduct: false,
        isBatchForProduct: false,
        isSerialForProduct : false,
        isSkuForProduct : false,
        GridStoreURL: null,
        GridStoreExtraParams: null,
        StockDetailArray: null,
        DataIndexMapping:{
            detailId:"detailId",
            locationId:"locationId",
            locationName:"locationName",
            rowId:"rowId",
            rowName:"rowName",
            rackId:"rackId",
            rackName:"rackName",
            binId:"binId",
            binName:"binName",
            batchName:"batchName",
            availableSerials:"availableSerials",
            availableSerialsSku:"availableSerialsSku",
            availableQty:"availableQty",
            quantity:"quantity",
            serials:"serials",
            serialsSku:"serialsSku"
        }
    });
    Wtf.CCStockDetailWin.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.CCStockDetailWin,Wtf.Window,{
    
    initComponent:function (){
        Wtf.CCStockDetailWin.superclass.initComponent.call(this);
        
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
        if(this.DataIndexMapping.locationId == undefined){
            this.DataIndexMapping.locationId="locationId"
        }
        if(this.DataIndexMapping.locationName == undefined){
            this.DataIndexMapping.locationName="locationName"
        }
        if(this.DataIndexMapping.rowId == undefined){
            this.DataIndexMapping.rowId="rowId"
        }
        if(this.DataIndexMapping.rowName == undefined){
            this.DataIndexMapping.rowName="rowName"
        }
        if(this.DataIndexMapping.rackId == undefined){
            this.DataIndexMapping.rackId="rackId"
        }
        if(this.DataIndexMapping.rackName == undefined){
            this.DataIndexMapping.rackName="rackName"
        }
        if(this.DataIndexMapping.binId == undefined){
            this.DataIndexMapping.binId="binId"
        }
        if(this.DataIndexMapping.binName == undefined){
            this.DataIndexMapping.binName="binName"
        }
        if(this.DataIndexMapping.batchName == undefined){
            this.DataIndexMapping.batchName="batchName"
        }
        if(this.DataIndexMapping.availableSerials == undefined){
            this.DataIndexMapping.availableSerials="availableSerials"
        }
        if(this.DataIndexMapping.availableSerialsSku == undefined){
            this.DataIndexMapping.availableSerialsSku="availableSerialsSku"
        }
        if(this.DataIndexMapping.availableQty == undefined){
            this.DataIndexMapping.availableQty="availableQty"
        }
        if(this.DataIndexMapping.quantity == undefined){
            this.DataIndexMapping.quantity="quantity"
        }
        if(this.DataIndexMapping.serials == undefined){
            this.DataIndexMapping.serials="serials"
        }
        if(this.DataIndexMapping.serialsSku == undefined){
            this.DataIndexMapping.serialsSku="serialsSku"
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
        
        
        this.gridRec = new Wtf.data.Record.create([
        {
            name:"detailId"
        },

        {
            name:"locationId",
            mapping: "fromLocationId"
        },

        {
            name:"locationName",
            mapping: "fromLocationName"
        },
        {
            name:"rowId",
            mapping: "fromRowId"
        },

        {
            name:"rowName",
            mapping: "fromRowName"
        },
        {
            name:"rackId",
            mapping: "fromRackId"
        },

        {
            name:"rackName",
            mapping: "fromRackName"
        },
        {
            name:"binId",
            mapping: "fromBinId"
        },

        {
            name:"binName",
            mapping: "fromBinName"
        },

        {
            name:"batchName"
        },
        {
            name:"availableSerials"
        },
        {
            name:"availableSerialsSku"
        },

        {
            name:"availableQty"
        },

        {
            name:"quantity"
        },
        {
            name:"serials"
        },
        {
            name:"serialsSku"
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
            this.fillDetails();
            this.addBlankRow()
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
                storeid:this.FromStoreId
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

        this.quantityeditor=new Wtf.form.NumberField({
            scope: this,
            allowBlank:false,
            allowDecimals:true,
            decimalPrecision:4,//Wtf.companyPref.quantityDecimalPrecision,
            allowNegative:false,
            value:0
        })
        this.batcheditor=new Wtf.form.TextField({
            scope: this,
            allowBlank:false
        })
        this.serialeditor=new Wtf.form.TextField({
            scope: this,
            allowBlank:false
        })
        
        this.sm = new Wtf.grid.RowSelectionModel({
            width:25,
            singleSelect:true
        });
        this.cm = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header:"Location",
                dataIndex:"locationId",
                editor: this.locCmb,
                renderer: function(v,m,r){
                    return r.get('locationName')
                }
            },{
                header:"Row",
                dataIndex:"rowId",
                editor: this.rowCmb,
                hidden : !this.isRowForProduct,
                renderer: function(v,m,r){
                    return r.get('rowName')
                }
            },{
                header:"Rack",
                dataIndex:"rackId",
                editor: this.rackCmb,
                hidden : !this.isRackForProduct,
                renderer: function(v,m,r){
                    return r.get('rackName')
                }
            },{
                header:"Bin",
                dataIndex:"binId",
                editor: this.binCmb,
                hidden : !this.isBinForProduct,
                renderer: function(v,m,r){
                    return r.get('binName')
                }
            },{
                header:"Batch",
                dataIndex:"batchName",
                hidden : !this.isBatchForProduct,
                editor: this.batcheditor
            },{
                header:"System Quantity",
                dataIndex:"availableQty",
                renderer:function(v){
                    return (parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) == "NaN") ? parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) : parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);}
            },{
                header:"Actual Quantity*",
                dataIndex:"quantity",
                editor:this.quantityeditor
            },{
                header:"Available Serials",
                dataIndex:"availableSerials",
                hidden : !this.isSerialForProduct
            },{
                header:"Serials *",
                dataIndex:"serials",
                hidden : !this.isSerialForProduct
            //                editor:this.serialeditor
            },{
                header:"Action",
                align:'center',
                dataIndex: "lock",
                width:50,
                renderer: function(v,m,rec){
                    if(rec.get('manual') == true){
                        return "<span class='pwnd delete-gridrow'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>";
                    }
                    return ""
                }
            }]);
        var tbarArr = [];
        
        this.grid=new Wtf.grid.EditorGridPanel({
            region: 'center',
            border: false,
            store: this.gridStore,
            cm: this.cm,
            sm:this.sm,
            loadMask : true,
            layout:'fit',
            viewConfig: {
                forceFit: true
            },
            tbar:tbarArr,
            clicksToEdit: 1
        })
        this.grid.on('rowclick',this.handleRowClick,this);
        this.grid.on('cellclick',this.handleCellClick,this);
        this.grid.on('afteredit',this.handleAfterEdit,this);
        
    },
    ArrangeNumberer: function(currentRow) {                // use currentRow as no. from which you want to change numbering
        var plannerView = this.grid.getView();                      // get Grid View
        var length = this.grid.getStore().getCount();              // get store count or no. of records upto which you want to change numberer
        for (var i = currentRow; i < length; i++){
            plannerView.getCell(i, 0).firstChild.innerHTML = i + 1;
        }
    },
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
            var store=grid.getStore();
            var total=store.getCount();
            if(rowindex==total-1){
                return;
            }
            var rec = store.getAt(rowindex);
            if(rec.get('manual') == true){
                Wtf.MessageBox.confirm('Warning', 'Are you sure you want to remove this item?', function(btn){
                    if(btn!="yes") {
                        return;
                    }
                    store.remove(rec);
                    this.ArrangeNumberer(rowindex);
                }, this);
            }else{
                WtfComMsgBox(["Info", "You can remove only manualy added record"], 2);
            }
        }
    },
    addBlankRow: function(){
        var store = this.grid.store;
        var add = false;
        if(store.getCount() > 0){
            var rec = store.getAt(store.getCount() - 1)
            if(rec.get('locationId')){
                add = true;
            }
        }else{
            add = true;
        }
        if(add){
            var blankRec = new this.gridRec({
                detailId : '',
                locationId : '',
                locationName: '',
                rowId : '',
                rowName: '',
                rackId : '',
                rackName: '',
                binId : '',
                binName: '',
                batchName:'',
                availableSerials:'',
                availableSerialsSku:'',
                availableQty:0,
                quantity:'',
                serials:'',
                serialsSku:'',
                manual:true
            });
            store.add(blankRec)
        }
        
    },

    handleCellClick: function(grid, rowIndex, colIndex, event){
        var store = grid.getStore();
        var rec = store.getAt(rowIndex);
        var dataIndex=grid.getColumnModel().getDataIndex(colIndex);

        if(rec.get('manual')){
            if(dataIndex != 'locationId' && dataIndex != 'serials' && !rec.get('locationId')){
                WtfComMsgBox(["Info", "Please select Location first"], 0);
                return false;
            }
        }else {
            if(dataIndex != 'serials' && dataIndex != 'quantity'){
                return false;
            }
        }
        
        if(dataIndex == 'serials'){
            if(rec.get('quantity') == "" || rec.get('quantity') == 0){
                WtfComMsgBox(["Info", "Please fill actual quantity first"], 0);
                return false;
            }else if(this.isBatchForProduct && !rec.get('batchName')){
                WtfComMsgBox(["Info", "Please fill batch name first"], 0);
                return false;
            }
            this.openSerialSelectionWin(rec);
        }
    },
    handleAfterEdit:function(e) {
        var rec = e.record;
        if(e.field == 'quantity'){
            if(this.isSerialForProduct){ 
                var serials = rec.get('serials').split(',')
                if(rec.get('quantity') == 0 && serials.length != rec.get('quantity')){
                    rec.set('serials', "");
                }
            }
        }
        if(e.field == 'serials'){
            var serials = rec.get('serials').split(',')
            if(this.isSerialForProduct && (serials.length != rec.get('quantity'))){
                return false;
            }

            this.recordSerials(rec);
        }
        if(e.field == 'locationId'){
            rec.set('locationName', this.locCmb.getRawValue())
            this.addBlankRow()
        }
        if(e.field == 'rowId'){
            rec.set('rowName', this.rowCmb.getRawValue())
        }
        if(e.field == 'rackId'){
            rec.set('rackName', this.rackCmb.getRawValue())
        }
        if(e.field == 'binId'){
            rec.set('binName', this.binCmb.getRawValue())
        }
        
    },
    validateSelectedDetails: function(){
        var qtyCount = 0;
        var isEmptyLocation = false;
        var isEmptyBatch = false;
        var isEmptyRow = false;
        var isEmptyRack = false;
        var isEmptyBin = false;
        var isSerialCountMismatch = false;
        var isDuplicateSerialFound = false;
        var usedSerials = [];
        var duplicateRows = [];
        var rowCount = this.gridStore.getCount() -1;
        for(var i=0 ;i< rowCount ; i++){
            var rec = this.gridStore.getAt(i);
            if(rec.get('quantity') > 0){
                qtyCount += rec.get('quantity');
                if(this.isSerialForProduct){
                    var serialArray = rec.get('serials').split(',');
                    if(!serialArray[0] || serialArray.length != rec.get('quantity')){
                        isSerialCountMismatch = true;
                        break;
                    }
                    for(var sc=0 ; sc<serialArray.length ; sc++){
                        usedSerials.push(rec.get('batchName')+serialArray[sc])
                    }
                }
                if(!rec.get('locationId')){
                    isEmptyLocation = true;
                    break;
                }
                if(this.isRowForProduct && !rec.get('rowId')){
                    isEmptyRow = true;
                    break;
                }
                if(this.isRackForProduct && !rec.get('rackId')){
                    isEmptyRack = true;
                    break;
                }
                if(this.isBinForProduct && !rec.get('binId')){
                    isEmptyBin = true;
                    break;
                }
                if(this.isBatchForProduct && !rec.get('batchName')){
                    isEmptyBatch = true;
                    break;
                }
                
            }
            var row = rec.get('locationId')
            +(this.isRowForProduct ? rec.get('rowId'): "")
            +(this.isRackForProduct ? rec.get('rackId'): "")
            +(this.isBinForProduct ? rec.get('binId'): "")
            +(this.isBatchForProduct ? rec.get('batchName'): "")
            if(duplicateRows.indexOf(row) == -1){
                duplicateRows.push(row)
            }
        }
        
        
        if(isEmptyLocation){
            WtfComMsgBox(["Alert", "Please select Location for selected records "], 0);
            return false;
        }
        if(isEmptyRow){
            WtfComMsgBox(["Warning", "Please select Row for selected records "], 2);
            return false;
        }
        if(isEmptyRack){
            WtfComMsgBox(["Warning", "Please select Rack for selected records "], 2);
            return false;
        }
        if(isEmptyBin){
            WtfComMsgBox(["Warning", "Please select Bin for selected records "], 2);
            return false;
        }
        if(isEmptyBatch){
            WtfComMsgBox(["Warning", "Please give Batch Name for selected records "], 2);
            return false;
        }
        if(isSerialCountMismatch){
            WtfComMsgBox(["Alert", "Serial count is not matched with quantity."], 2);
            return false;
        }
        if(duplicateRows.length != rowCount){
            WtfComMsgBox(["Alert", "duplicate rows are not allowed here, please update actual quantity in a single record."], 2);
            return false;
        }
        if(qtyCount !== this.MaxAllowedQuantity){
            WtfComMsgBox(["Info", "Total actual quantity must equal to <b>"+this.MaxAllowedQuantity+"</b>."], 0);
            return false;
        }
        
        for(var c=0; c <usedSerials.length - 1; c++){
            for(var k=c+1; k <usedSerials.length; k++){
                if(usedSerials[c] == usedSerials[k]){
                    isDuplicateSerialFound = true;
                    break;
                }
            }
            if(isDuplicateSerialFound){
                break;
            }
        }
        if(isDuplicateSerialFound){
            WtfComMsgBox(["Alert", "Duplicate Serials not allowed here."], 0);
            return false;
        }
        return true;
    },
    fillDetails: function(){
        if(this.StockDetailArray instanceof Array){
            var locationIdKey = this.DataIndexMapping.locationId;
            var rowIdKey = this.DataIndexMapping.rowId;
            var rackIdKey = this.DataIndexMapping.rackId;
            var binIdKey = this.DataIndexMapping.binId;
            var batchNameKey = this.DataIndexMapping.batchName;
            var quantityKey = this.DataIndexMapping.quantity;
            var serialsKey = this.DataIndexMapping.serials;
            var serialsSkuKey = this.DataIndexMapping.serialsSku;
            for(var i=0 ; i< this.StockDetailArray.length ; i++){
                var givenStockDetail = this.StockDetailArray[i];
                
                var key = givenStockDetail[locationIdKey]
                + (givenStockDetail[rowIdKey] ? givenStockDetail[rowIdKey] : "")
                + (givenStockDetail[rackIdKey] ? givenStockDetail[rackIdKey] : "")
                + (givenStockDetail[binIdKey] ? givenStockDetail[binIdKey] : "")
                + (givenStockDetail[batchNameKey] ? givenStockDetail[batchNameKey] : "");
                var isRowFound = false;
                this.gridStore.each(function(rec){
                    if(!isRowFound){
                        var gridKey = rec.get(locationIdKey)
                        + (rec.get(rowIdKey) ? rec.get(rowIdKey) : "")
                        + (rec.get(rackIdKey) ? rec.get(rackIdKey) : "")
                        + (rec.get(binIdKey) ? rec.get(binIdKey) : "")
                        + (rec.get(batchNameKey) ? rec.get(batchNameKey) : "");
                        if(key === gridKey){
                            rec.set('quantity', givenStockDetail[quantityKey]);
                            rec.set('serials', givenStockDetail[serialsKey]);
                            rec.set('serialsSku', givenStockDetail[serialsSkuKey]);
                            rec.commit();
                            isRowFound = true;
                        }
                    }
                    
                }, this)
                if(!isRowFound){
                    this.addBlankRow();
                    var gridRec = this.gridStore.getAt(this.gridStore.getCount() - 1);
                    gridRec.set('locationId', givenStockDetail[locationIdKey]);
                    gridRec.set('locationName', givenStockDetail[this.DataIndexMapping.locationName]);
                    gridRec.set('rowId', givenStockDetail[rowIdKey]);
                    gridRec.set('rowName', givenStockDetail[this.DataIndexMapping.rowName]);
                    gridRec.set('rackId', givenStockDetail[rackIdKey]);
                    gridRec.set('rackName', givenStockDetail[this.DataIndexMapping.rackName]);
                    gridRec.set('binId', givenStockDetail[binIdKey]);
                    gridRec.set('binName', givenStockDetail[this.DataIndexMapping.binName]);
                    gridRec.set('batchName', givenStockDetail[batchNameKey]);
                    gridRec.set('quantity', givenStockDetail[quantityKey]);
                    gridRec.set('serials', givenStockDetail[serialsKey]);
                    gridRec.set('serialsSku', givenStockDetail[serialsSkuKey]);
                    gridRec.set('manual', true);
                    gridRec.commit();
                }
                
            }
        }
    },
    getSelectedDetails: function(){
        var stockDetailArray = [];
        var count = 0;
        this.gridStore.each(function(rec){
            if(!rec.get('manual') || (rec.get('manual') && rec.get('quantity') > 0) ){
                var stockDetail = {};
                
                stockDetail[this.DataIndexMapping.detailId] = rec.get('detailId');
                stockDetail[this.DataIndexMapping.locationId] = rec.get('locationId');
                stockDetail[this.DataIndexMapping.locationName] = rec.get('locationName');
                stockDetail[this.DataIndexMapping.rowId] = rec.get('rowId');
                stockDetail[this.DataIndexMapping.rowName] = rec.get('rowName');
                stockDetail[this.DataIndexMapping.rackId] = rec.get('rackId');
                stockDetail[this.DataIndexMapping.rackName] = rec.get('rackName');
                stockDetail[this.DataIndexMapping.binId] = rec.get('binId');
                stockDetail[this.DataIndexMapping.binName] = rec.get('binName');
                stockDetail[this.DataIndexMapping.batchName] = rec.get('batchName');
                stockDetail[this.DataIndexMapping.availableQty] = rec.get('availableQty');
                stockDetail[this.DataIndexMapping.availableSerials] = rec.get('availableSerials');
                stockDetail[this.DataIndexMapping.availableSerialsSku] = rec.get('availableSerialsSku');
                stockDetail[this.DataIndexMapping.quantity] = rec.get('quantity');
                stockDetail[this.DataIndexMapping.serials] = rec.get('serials');
                stockDetail[this.DataIndexMapping.serialsSku] = rec.get('serialsSku');
                stockDetail.manual = rec.get('manual');
                stockDetail.recIdx = count;
                
                stockDetailArray.push(stockDetail);
            }
            count++;
        }, this)
        return stockDetailArray
    },
    openSerialSelectionWin: function(rowRecord){
        var serialSelectionWin = new Wtf.CCSerialSelectionWin({
            AvailableSerials :rowRecord.get('availableSerials'),
            AvailableSerialsSku :rowRecord.get('availableSerialsSku'),
            SelectedSerials :rowRecord.get('serials'),
            SelectedSerialsSku :rowRecord.get('serialsSku'),
            SelectedQty :rowRecord.get('quantity'),
            isSkuForProduct: this.isSkuForProduct
        })
        serialSelectionWin.show();
        serialSelectionWin.on('serialSelected', function(serials, serialsSku){
            rowRecord.set('serials', serials);
            rowRecord.set('serialsSku', serialsSku);
            serialSelectionWin.close();
            
        }, this)
    }
});

//////////////////////////////////////////////////////////////

Wtf.CCSerialSelectionWin = function (config){
    Wtf.apply(this,{
        title : "Select "+config.SelectedQty+" Serials",
        modal : true,
        iconCls : 'iconwin',
        minWidth:75,
        width : 350,
        height: 300,
        resizable :true,
        scrollable:true,
        layout:'fit',
        border:false,
        buttons :[{
            text : 'Save',
            //                iconCls:'pwnd ReasonSubmiticon caltb',
            scope : this,
            handler: function(){  
                var recs = this.serialGrid.getSelectionModel().getSelections();
                if(recs.length != this.SelectedQty){
                    WtfComMsgBox(["Alert", "You need to select "+this.SelectedQty+" serial(s)"], 0);
                    return false;
                }
                
                var serials = "";
                var serialsSku = "";
                var invalid = false;
                for(var i=0 ; i<recs.length ; i++){
                    var rec = recs[i];
                    if(!rec.get("serial")){
                        WtfComMsgBox(["Alert", "Please provide serial value for selected rows"], 0);
                        invalid = true;
                        break;
                    }
                    if(this.isSkuForProduct && !rec.get("sku")){
                        WtfComMsgBox(["Alert", "Please provide "+this.skuFieldName+" value for serial"], 0);
                        invalid = true;
                        break;
                    }
                    if(serials == ""){
                        serials = rec.get('serial')
                        serialsSku = rec.get('sku')
                    }else{
                        serials += ","+rec.get('serial')
                        serialsSku += ","+rec.get('sku')
                    }
                }
                if(invalid){
                    return false;
                }
                this.fireEvent('serialSelected', serials, serialsSku);
                
                
            }
        }]
    });
    Wtf.CCSerialSelectionWin.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.CCSerialSelectionWin,Wtf.Window,{
    
    initComponent:function (){
        Wtf.CCSerialSelectionWin.superclass.initComponent.call(this);
        this.skuFieldName = (Wtf.account.companyAccountPref.SKUFieldParm)?(Wtf.account.companyAccountPref.SKUFieldRename!="" && Wtf.account.companyAccountPref.SKUFieldRename!= undefined)?Wtf.account.companyAccountPref.SKUFieldRename:WtfGlobal.getLocaleText("acc.product.sku"):WtfGlobal.getLocaleText("acc.product.sku")
        this.GetAddEditForm();

        this.mainPanel = new Wtf.Panel({
            layout:'border',
            items:[this.serialGrid]
        });
        this.add(this.mainPanel);
        
        this.addEvents({
            serialSelected: true
        })
    },
    GetAddEditForm:function (){
        var availSerials = this.AvailableSerials;
        var selectedSerials = this.SelectedSerials;
        var availSerialsSku = this.AvailableSerialsSku;
        var selectedSerialsSku = this.SelectedSerialsSku;
        
        var serialArr = [], availSerialArr =[], serialsSkuArr = [];
        if(availSerials != "" && availSerials != null && availSerials != undefined){
            serialArr = availSerials.split(",");
            availSerialArr = availSerials.split(",");
            if(this.isSkuForProduct){
                serialsSkuArr = availSerialsSku.split(",");
            }
        }
        if(selectedSerials != "" && selectedSerials != null && selectedSerials != undefined){
            var selSerialArr = selectedSerials.split(",");
            var selectedSerialsSkuArr = [];
            if(this.isSkuForProduct){
                selectedSerialsSkuArr = selectedSerialsSku.split(",");
            }
            for(var i=0 ;i<selSerialArr.length ; i++){
                var seletedSerial = selSerialArr[i];
                var seletedSerialSku = '';
                if(selectedSerialsSkuArr.length > i){
                    seletedSerialSku = selectedSerialsSkuArr[i];
                }
                var exists = false;
                for(var j=0 ;j<serialArr.length ; j++){
                    var availSerial = serialArr[j];
                    if(seletedSerial == availSerial){
                        exists = true;
                    }
                }
                if(!exists){
                    serialArr.push(seletedSerial)
                    serialsSkuArr.push(seletedSerialSku);
                }
            }
        }

        var selectedRowIndexes = [];
        if(selectedSerials != "" && selectedSerials != null && selectedSerials != undefined){
            var selectedSerialArr = selectedSerials.split(",");
            for(var i=0 ;i<selectedSerialArr.length ; i++){
                var seletedSerial = selectedSerialArr[i];
                for(var j=0 ;j<serialArr.length ; j++){
                    var availSerial = serialArr[j];
                    if(seletedSerial == availSerial){
                        selectedRowIndexes.push(j);
                    }
                }
            }
        }
        var gridStore = new Wtf.data.SimpleStore({
            fields:['serial', 'sku', 'added']
        });
        var serialCmbData = [];
        for(var i=0 ; i<serialArr.length ; i++){
            serialCmbData.push([serialArr[i], serialsSkuArr[i]])
        }
        gridStore.loadData(serialCmbData);
        gridStore.each(function(rec){
            if(availSerialArr.indexOf(rec.get("serial")) == -1){
                rec.set("added", true);
            }
        }, this)
        
        
        var sm = new Wtf.grid.CheckboxSelectionModel({
            width:25
        });
        sm.on('rowselect', this.handleSerialSelection, this)
        
        var cm = new Wtf.grid.ColumnModel([
            sm,
            new Wtf.grid.RowNumberer(),
            {
                header:"Serial",
                dataIndex:"serial",
                editor: new Wtf.form.TextField({})
            },{
                header:this.skuFieldName,
                dataIndex:"sku",
                hidden: !this.isSkuForProduct,
                editor: new Wtf.form.TextField({})
            }]);
        
        this.serialGrid=new Wtf.grid.EditorGridPanel({
            region: 'center',
            border: false,
            store: gridStore,
            cm: cm,
            sm:sm,
            loadMask : true,
            clicksToEdit:1,
            layout:'fit',
            viewConfig: {
                forceFit: true
            }
        })
        
        this.serialGrid.on('afteredit', this.handleAfterEdit, this)
        this.serialGrid.on('cellclick', this.handleCellClick, this)
        this.serialGrid.on('render', function(){
            sm.selectRows(selectedRowIndexes);
            this.addNewSerial();
        }, this)
        
    },
    handleSerialSelection: function(sm, ri, r){
        if(this.serialGrid.store.getCount() -1  == ri && !r.get('serial')){
            sm.deselectRow(ri)
        }
    },
    handleCellClick: function(grid, rowIndex, columnIndex, e){
        var rec = grid.getStore().getAt(rowIndex); 
        if(!rec.get("added")){
            return false;
        }
        var fieldName = grid.getColumnModel().getDataIndex(columnIndex); 
        if(fieldName == 'sku'){
            if(!rec.get('serial')){
                WtfComMsgBox(["Warning", "Please add serial first"], 0);
                return false;
            }
            if(!rec.get('added')){
                WtfComMsgBox(["Warning", "You can add "+this.skuFieldName+" for newly added serial"], 0);
                return false;
            }
        }
        
    },
    handleAfterEdit: function(e){
        var rec = e.record;
        if(e.field == 'serial'){
            var valid = true;
            var comma = "," ;
                if ((e.value).match(comma)) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.batchserialwindow.enterSerialNameWithoutComma")], 0);
                    rec.set('serial','');
                    return false;
                }
            for(var i=0; i< this.serialGrid.store.getCount();i++){
                var r = this.serialGrid.store.getAt(i)
                if((i != e.row && r.get('serial') == e.value)){
                    valid = false;
                    break;
                }
            }
            
            if(valid){
                this.addNewSerial()
            }else{
                WtfComMsgBox(["Warning", "Serial <b>"+rec.get('serial')+"</b> already used"], 0);
                rec.set('serial','');
                return false;
            }
        }
    },
    addNewSerial: function(){
        var store = this.serialGrid.store;
        var addSerial = false;
        if(store.getCount() > 0){
            var rec = store.getAt(store.getCount() - 1)
            if(rec.get('serial')){
                addSerial = true;
            }
        }else{
            addSerial = true;
        }
        if(addSerial){
            var p = new store.recordType({
                serial:'',
                sku:'',
                added:true
            });
            store.add(p)
        }
    }
});
