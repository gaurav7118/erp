Wtf.GoodsOrderTransfer = function(config){
    Wtf.GoodsOrderTransfer.superclass.constructor.call(this, config);
    this.configId = config.id;
};
Wtf.extend(Wtf.GoodsOrderTransfer, Wtf.Panel, {
    initComponent: function() {
        Wtf.GoodsOrderTransfer.superclass.initComponent.call(this);
    },
    onRender: function(config) {
        Wtf.GoodsOrderTransfer.superclass.onRender.call(this, config);
        this.dmflag = 1;        
        this.fromdateVal =new Date().getFirstDateOfMonth();
        this.todateVal=new Date().getLastDateOfMonth();

        this.frmDate = new Wtf.form.DateField({
            emptyText:WtfGlobal.getLocaleText("acc.stock.Fromdate"),
            readOnly:true,
            width : 100,
            value:WtfGlobal.getDates(true),
            minValue: Wtf.archivalDate,
            name : 'frmdate',
            format: "Y-m-d"
        });
        this.toDate = new Wtf.form.DateField({
            emptyText:WtfGlobal.getLocaleText("acc.stock.Todate"),
            readOnly:true,
            width : 100,
            name : 'todate',
            minValue: Wtf.archivalDate,
            value:WtfGlobal.getDates(false),
            format: 'Y-m-d'
        });
        this.storeCmbRecord = new Wtf.data.Record.create([
        {
            name: 'store_id'
        },

        {
            name: 'fullname'
        },

        {
            name: 'analysiscode'
        },

        {
            name: 'abbr'
        },

        {
            name: 'dmflag'
        }
        ]);

        this.storeCmbStore = new Wtf.data.Store({
            url:  'INVStore/getStoreList.do',
            baseParams:{
                byStoreManager:"true",
                isFromInvTransaction:true, //ERM-691 do not display repair/scrap stores in regular inventory transactions
                byStoreExecutive:"true"
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.storeCmbRecord)
        });
        this.storeCmbfilter = new Wtf.form.ComboBox({
            fieldLabel : WtfGlobal.getLocaleText("acc.stock.Store*"),
            hiddenName : 'store',
            store : this.storeCmbStore,
            forceSelection:true,
            displayField:'fullname',
            valueField:'store_id',
            mode: 'local',
            width : 125,
            triggerAction: 'all',
            emptyText:WtfGlobal.getLocaleText("acc.je.Selectstore"),
            typeAhead:true,
            listWidth:300,
            tpl: new Wtf.XTemplate(
                '<tpl for=".">',
                '<div wtf:qtip = "{[values.fullname]}" class="x-combo-list-item">',
                '<div>{fullname}</div>',
                '</div>',
                '</tpl>')
        });

        var data =[];
        if(this.type==Wtf.STORE_ORDER){
            data.push([0,"ALL"],[1,"Ordered"],[2,"Ready for Collection"]);
        }else if(this.type==Wtf.FULFILLED_ORDER){
            data.push([0,"ALL"],[1,"Collected"],[2, "Rejected"],[3, "Returned"],[4, "Deleted"]);
        } else if(this.type==Wtf.GOODS_PENDING_ORDER){
            data.push([0,"ALL"],[1,"Ordered"],[2," Ready for Collection"],[3, "Returned Request"]);
        }
        this.statusCmbStore = new Wtf.data.SimpleStore({
            fields: ['id', 'status'],
            data : data
        });

        this.statusCmbfilter = new Wtf.form.ComboBox({
            hiddenName : 'store',
            store : this.statusCmbStore,
            forceSelection:true,
            displayField:'status',
            valueField:'id',
            mode: 'local',
            width : 125,
            triggerAction: 'all',
            value:0,
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectLeaseStatus"),
            typeAhead:true,
            listWidth:150
        });
        
        this.resetBtn = new Wtf.Button({
            anchor : '90%',
            text: WtfGlobal.getLocaleText("acc.inventory.QAAproval.ResetFilter"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.stock.ClicktoResetFilter")
            },
            iconCls:getButtonIconCls(Wtf.etype.resetbutton),
            scope:this,
            handler:function(){
                Wtf.getCmp("Quick"+this.grid.id).setValue("");
                this.frmDate.reset();
                this.toDate.reset();
                this.statusCmbfilter.setValue(this.statusCmbfilter.store.data.items[0].data.id);
                if(this.storeCmbfilter.store.getCount()>0&&this.storeCmbfilter.store.data.items[0] != undefined){
                    this.storeCmbfilter.setValue(this.storeCmbfilter.store.data.items[0].data.store_id);
                }
                this.initloadgridstore(this.frmDate.getValue().format(format), this.toDate.getValue().format(format), this.storeCmbfilter.getValue(),this.statusCmbfilter.getValue());
            }
        });

        this.search = new Wtf.Button({
            anchor: '90%',
            text: WtfGlobal.getLocaleText("acc.common.search"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.inventory.QAAproval.ClicktoSearchOrders")
            },
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: function() {
                var action = 4
                switch(action) {
                    case 4:
//                        var format = WtfGlobal.getDateFormat();
                        this.initloadgridstore(this.frmDate.getValue().format(format), this.toDate.getValue().format(format), this.storeCmbfilter.getValue(),this.statusCmbfilter.getValue());
                        break;
                    default:
                        break;
                }
            }
        });
        
        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });
        
       /*Expand collapse functionality for Stock Request and Stock issue
       the expandall functionality will expand all rows
       and collapseall functionality will collapse all rows.
       */
        this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Expand"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
        scope: this,
        handler: function() {
            if (this.expandCollpseButton.getText() == WtfGlobal.getLocaleText("acc.field.Expand")) {
                this.expandButtonClicked = true;
            }
            /*
             *ExpandAll CollapseAll functions for Grouping and MultiGrouping
             **/
            if(this.configId == "goodsPendingOrdersgoodsOrderParentTabb" || this.configId == "interstocktransferreqgoodsIssueParentTab"){
                this.expandCollapseGrid(this.expandCollpseButton.getText());
            }
            else if (this.configId == "goodsapprovedgoodsIssueParentTab" || this.configId == "goodsapprovedgoodsOrderParentTabb"){
                this.expandCollapseMultiGroupingGrid(this.expandCollpseButton.getText());
            }
         }
        
        });
        
        this.record = Wtf.data.Record.create([
        {
            "name":"id"
        },

        {
            "name":"name"
        },

        {
            "name":"srno"
        },

        {
            "name":"transfernoteno"
        },

        {
            "name":"fromstore"
        },

        {
            "name":"tostore"
        },

        {
            "name":"fromstorename"
        },
        {
            "name":"fromstoreadd"
        },
        {
            "name":"fromstorefax"
        },
        {
            "name":"fromstorephno"
        },

        {
            "name":"tostorename"
        },
        {
            "name":"tostoreadd"
        },
        {
            "name":"tostoredefaultlocationid"
        },
        {
            "name":"tostoredefaultlocationname"
        },
        {
            "name":"tostorefax"
        },
        {
            "name":"tostorephno"
        },

        {
            "name":"itemcode"
        },
        {
            "name":"issuedOn"
        },
        {
            "name":"issuedOnFull"
        },

        {
            "name":"customerpartnumber"
        },

        {
            "name":"customerpartnumber"
        },

        {
            "name":"itemId"
        },
        {
            "name":"itemname"
        },
        {
            "name":"itemdescription"
        },

        {
            "name":"uomname"
        },

        {
            "name":"quantity"
        },

        {
            "name":"date"
//            type:"date", //Formatting it from Java side only.
//            format:"Y-m-d"
        },
        {
            "name":"bussinessdate"
//            type:"date", //Formatting it from Java side only.
//            format:"Y-m-d"
        },
        {
            "name":"statusId"
        },
        {
            "name":"status"
        },
        {
            "name":"isReturnRequest"
        },

        {
            "name":"remark"
        },

        {
            "name":"keyfield"
        },

        {
            "name":"issuenoteno"
        },

        {
            "name":"nwquantity"
        },

        {
            "name":"issuequantity"
        },

        {
            "name":"createdby"
        },

        {
            "name":"closeornot"
        },

        {
            "name":"collectedOn"
        },

        {
            "name":"collectedOnFull"
        },

        {
            "name":"delquantity"
        },

        {
            "name":"reason"
        },

        {
            "name":"availabelQuantity"
        },
        {
            "name":"packaging"
        },

        {
            "name":"tostoreaddress"
        },

        {
            "name":"fromstoreaddress"
        },

        {
            "name":"fromlocationid"
        },

        {
            "name":"tolocationid"
        },

        {
            "name":"costcenterid"
        },

        {
            "name":"costcenter"
        },
        {
            "name":"orderinguomname"
        },

        {
            "name":"transferinguomname"
        },

        {
            "name":"stockuomname"
        },
        {
            "name":"itemdefaultwarehouse"
        },
        {
            "name":"projectnumber"
        },
        {
            "name":"totalOrderedQty"
        },
        {
            "name":"isBatchForProduct"
        },
        {
            "name":"isSerialForProduct"
        },
        {
            name:"isRowForProduct"
        },
        {
            name:"isRackForProduct"
        },
        {
            name:"isBinForProduct"
        },
        {
            "name":"orderToStockUOMFactor"
        },
        {
            "name":"transferToStockUOMFactor"
        },
        {
            "name" : "stockDetailsForIssue"
        },
        {
            "name" : "stockDetailsForCollect"
        },
        {
            "name" : "issueDetails"
        },
        {
            "name" : "collectDetails"
        },
        {
            "name":"defaultStoreName"
        },
        {
            "name":"defaultStoreId"
        },
        {
            "name":"defaultLocName"
        },
        {
            "name":"defaultLocId"
        },
        {
            "name":"defaultCollectionLocId"
        },
        {
            "name":"defaultCollectionLocName"
        },
        {
            "name":"defaultAvailQty"
        },
        {
            "name":"stockDetails"
        },
        {
            "name":"approvedBy"
        },
        {
            "name":"returnReason"
        },
        {
            "name":"hscode"
        },
        {
            "name":"transactiotype"
        },
        {
            "name": "isQAEnable"
        }
        ]);
        var grpView = new Wtf.grid.GroupingView({
            forceFit: false,
            showGroupName: true,
            enableGroupingMenu: true,
            hideGroupedColumn: false,
            /*
             *default records will be collapsed
             **/
            startCollapsed : this.configId == "goodsPendingOrdersgoodsOrderParentTabb" || this.configId == "interstocktransferreqgoodsIssueParentTab" ? true : false
        });
        var format = "Y-m-d";
        this.ds = new Wtf.data.GroupingStore({
            sortInfo: {
                field: 'transfernoteno',
                direction: "DESC"
            },
            groupField:"transfernoteno",
            pruneModifiedRecords:true,
            baseParams: {
                type:this.type,
                frmDate:this.frmDate.getValue().format(format),
                toDate: this.toDate.getValue().format(format),
                storeid:this.storeCmbfilter.getValue(),
                status:this.statusCmbfilter.getValue()
            },
            url: 'INVGoodsTransfer/getStockRequestList.do',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },
            this.record
            )
        });
       
        this.myloadMask = new Wtf.LoadMask(document.body,{
            msg:"Loading..."
        });
        
        this.tmplt =new Wtf.XTemplate(
            '<table cellspacing="1" cellpadding="0" style="margin-top:15px;width:100%;margin-bottom:40px;position:relative" border="0">',
            
            '<tr>',
            
            '<th style="padding-left:50px"><h2><b>No.</b></h2></th>',
            '<th ><h2><b>Issued Location</b></h2></th>',
            '<th ><h2><b>Collected Location</b></h2></th>',
            
            '<tpl for="parent">',
            '<tpl if="this.isTrue(parent.isRowForProduct)==true">',  // row
            '<th><h2><b>Issued Row</b></h2></th>',
            '<th><h2><b>Collected Row</b></h2></th>',
            '</tpl>',
            '</tpl>',
            
            '<tpl for="parent">',
            '<tpl if="this.isTrue(parent.isRackForProduct)==true">',  // rack
            '<th><h2><b>Issued Rack</b></h2></th>',
            '<th><h2><b>Collected Rack</b></h2></th>',
            '</tpl>',
            '</tpl>',
            
            '<tpl for="parent">',
            '<tpl if="this.isTrue(parent.isBinForProduct)==true">',  // bin
            '<th><h2><b>Issued Bin</b></h2></th>',
            '<th><h2><b>Collected Bin</b></h2></th>',
            '</tpl>',
            '</tpl>',
            
            '<tpl for="parent">',
            '<tpl if="this.isTrue(parent.isBatchForProduct)==true">',  // batch
            '<th><h2><b>Batch</b></h2></th>',
            '</tpl>',
            '</tpl>',
            
            '<tpl for="parent">',
            '<tpl if="this.isTrue(parent.isSerialForProduct)==true">',  // issued serial 
            '<th><h2><b>Issued Serials</b></h2></th>',
             '<th><h2><b>Collected Serials</b></h2></th>',
            '</tpl>',
            '</tpl>',
           
            '<th><h2><b>Issued Quantity</b></h2></th>',
            '<th ><h2><b>Collected Quantity</b></h2></th>',
            
            '</tr>',
     
            '<tr><span  class="gridLine" style="width:94%;margin-left:45px;position: relative;top: 33px;"></span></tr>',
            
            
            '<tpl for="stockDetails">',
            '<tr>',
            '<td style="padding-left:50px"><p>{#}</p></td>',
            
            '<td ><p>{issuedLocationName}</p></td>',
            '<td ><p>{collectedLocationName}</p></td>',
            
            '<tpl if="this.isTrue(parent.isRowForProduct)==true">',  // ROW
            '<td ><p>{issuedRowName}</p></td>',
            '<td ><p>{collectedRowName}</p></td>',
            '</tpl>',
            
            '<tpl if="this.isTrue(parent.isRackForProduct)==true">',  // Rack
            '<td ><p>{issuedRackName}</p></td>',
            '<td ><p>{collectedRackName}</p></td>',
            '</tpl>',
            
            '<tpl if="this.isTrue(parent.isBinForProduct)==true">',  // Bin
            '<td ><p>{issuedBinName}</p></td>',
            '<td ><p>{collectedBinName}</p></td>',
            '</tpl>',
            
            '<tpl if="this.isTrue(parent.isBatchForProduct)==true">',  // batch
            '<td ><p>{batchName}</p></td>',
            '</tpl>',
            
            '<tpl if="this.isTrue(parent.isSerialForProduct)==true">',  // issued serial 
            '<td ><p>{issuedSerials}</p></td>',
            '<td ><p>{collectedSerials}</p></td>',
            '</tpl>',
            
            '<td ><p>{issuedQuantity}</p></td>',
            '<td ><p>{collectedQuantity}</p></td>',
            
            '</tr>',
            '</tpl>',
            '</table>',
            {  
                isTrue: function(isSerialForProduct){
                    return isSerialForProduct;
                }
            }
            );    
        
        this.expander = new Wtf.grid.RowExpander({
            tpl :this.tmplt,
            renderer : function(v, p, record){
                // var isBatchForProduct=record.get("isBatchForProduct");
                //  var isSerialForProduct=record.get("isSerialForProduct");
                if(record.get("stockDetails").length>0){ //means has stock detail data
                    return  '<div class="x-grid3-row-expander">&#160;</div>'
                }else{
                    //return '&#160;' 
                    return  '<div class="x-grid3-row-expander">&#160;</div>'
                }
            }
           
        });
        
        
        
        this.sm = new Wtf.grid.CheckboxSelectionModel({});
        this.gridColumnModelArr=[];
        var colArr = [
            new Wtf.KWLRowNumberer(),
            this.sm,
            this.expander,
            {   
                header: WtfGlobal.getLocaleText("acc.field.SerialNo"),
                dataIndex: 'srno',
                hidden:true,
                editor:this.type == 1?new Wtf.form.TextField():null
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.OrderNoteNo"),
                dataIndex: 'transfernoteno',
                pdfwidth:50
            },{
                header: WtfGlobal.getLocaleText("acc.nee.69"),
                dataIndex: 'createdby',
                pdfwidth:100
            },{
                header: WtfGlobal.getLocaleText("acc.stock.IssueNoteNo"),
                dataIndex: 'issuenoteno',
                hidden:true,
                fixed:this.type == 2?false:true,
                pdfwidth:50,
                renderer:function(value,meta,rec){
                    if(value ==""){
                        return "N.A.";
                    }
                    return value;
                }
            },{
                header: WtfGlobal.getLocaleText("acc.stock.FromStore"),
                //sortable:true,
                dataIndex: 'fromstorename',
                pdfwidth:100
            },{            
                header: WtfGlobal.getLocaleText("acc.stock.ToStore"),
                dataIndex: 'tostorename'            ,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.stock.ToStoreDefaultlocationID"),
                hidden:true,
                dataIndex: 'tostoredefaultlocationid',
                pdfwidth:50
            },
            {            
                header: WtfGlobal.getLocaleText("acc.stock.ToStoreDefaultLocationName"),
                hidden:true,
                dataIndex: 'tostoredefaultlocationname',
                pdfwidth:50
            },{
                header: WtfGlobal.getLocaleText("acc.stock.BusinessDate"),
                //sortable:true,
                dataIndex: 'bussinessdate',
                pdfwidth:50
//                renderer: function(v){
//                    var date = v;
//                    if(v != undefined && v != ""){
//                        date = v.format('Y-m-d');
//                    }
//                    return date;
//                }
            },{
                header: WtfGlobal.getLocaleText("acc.contractMasterGrid.header8"),
                dataIndex: 'itemcode',
                pdfwidth:50
            },
            {
                header: WtfGlobal.getLocaleText("acc.contractMasterGrid.header7"),
                //sortable:true,
                dataIndex: 'itemname',
                pdfwidth:100
            },
            {
                header:WtfGlobal.getLocaleText("acc.je.CoilcraftPartNo"),
                //sortable:true,
                dataIndex:"customerpartnumber",
                pdfwidth:50,
                hidden: true
            },
            {
                header: WtfGlobal.getLocaleText("acc.stock.orderToStockUOMFactor"), //16
                dataIndex: 'orderToStockUOMFactor',
                align:"right",
                hidden:true,
                pdfwidth:50
            },
            {
                header: WtfGlobal.getLocaleText("acc.stock.AvailableQuantity(instockUoM)"),
                dataIndex: 'availabelQuantity',
                hidden:true,
                align:"right",
                pdfwidth:50,
                renderer: function(val){
                    return val;
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.stock.TotalOrderedQuantity"),
                dataIndex: 'totalOrderedQty',
                align:"right",
                pdfwidth:50,
                renderer: function(val) {
                    var v = String(val);
                    var ps = v.split('.');
                    var sub = ps[1];
                    if (sub != undefined && sub.length > 0) {
                        return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) == "NaN") ? parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) : parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                    } else
                        return val;
               }
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.OrderedQuantity"),
                dataIndex: 'quantity',
                align:"right",
                pdfwidth:50,
                renderer: function(val) {
                    var v = String(val);
                    var ps = v.split('.');
                    var sub = ps[1];
                    if (sub != undefined && sub.length > 0) {
                        return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) == "NaN") ? parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) : parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                    } else
                        return val;
            }
            },{
                header: WtfGlobal.getLocaleText("acc.stock.IssuedQuantity"),
                align:"right",
                pdfwidth:50,
                hidden:false,
                dataIndex: 'nwquantity',
                editor:(this.type == 1) ? new Wtf.form.NumberField({
                    scope: this,
                    id:"issuedQuantity",
                    allowBlank:false,
                    allowDecimals:true,
                    decimalPrecision:4,
                    allowNegative:false,
                    value:0
                }):null ,
                renderer: function(val,meta,rec){
                    if((rec.get('statusId') === 0 || rec.get('statusId') === 1) && val === 0){
                        return '';
                }else{
                        var v = String(val);
                        var ps = v.split('.');
                        var sub = ps[1];
                        if (sub != undefined && sub.length > 0) {
                            return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) == "NaN") ? parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) : parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                        } else
                            return val;
                  }
                }
            },{
                header: WtfGlobal.getLocaleText("acc.accPref.deliQuant"),
                align:"right",
                dataIndex: 'delquantity',
                scope:this,
                hidden:((this.type === 2 || this.type === 3))?false:true,
                pdfwidth:50,
                editor:this.type == 3?new Wtf.form.NumberField({
                    scope: this,                    
                    allowBlank:false,
                    allowDecimals:true,
                    decimalPrecision:4,
                    allowNegative:false,
                    //value:0,
                    listeners : {
                        'focus': this.setZeroToBlank
                    }
//                    validator : function(val){
//                        var re5digit=/^[0-9]*$/;
//                        if(val.search(re5digit) == -1){
//                            return false;
//                        }else{                            
//                            return true;
//                        }
//                    }
                }):null,
                
                renderer : this.type == 3?function(val, meta, rec, row, col, store){
                    if(val <= rec.get('nwquantity') && rec.get('statusId') === 2){
                        var v = String(val);
                        var ps = v.split('.');
                        var sub = ps[1];
                        if (sub != undefined && sub.length > 0) {
                            return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) == "NaN") ? parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) : parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                        } else
                            return val;
                    }
                    else if (val < rec.get('nwquantity')){
                        return val;
                    }else if(val == 0){
                        return '';
                    }else if(val > rec.get('nwquantity')){
                        return rec.get('nwquantity');
                    }
                }:function(val, meta, rec){
                    if (rec.get('statusId') === 2){
                        return rec.get('nwquantity');
                    }else
                        var v = String(val);
                        var ps = v.split('.');
                        var sub = ps[1];
                        if (sub != undefined && sub.length > 0) {
                            return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) == "NaN") ? parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) : parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                        } else
                            return val;
                }
            },{
                header: '',
                dataIndex:"issueDetails",
                renderer: this.serialRenderer.createDelegate(this),
                hidden:!(this.type === 1),
                width:40
            },{
                header: '',
                dataIndex:"collectDetails",
                renderer: this.serialRenderer.createDelegate(this),
                hidden:!(this.type === 3),
                width:40
            },{
                header: WtfGlobal.getLocaleText("acc.product.packaging"),
                dataIndex: 'packaging',
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.invoice.gridUOM"),
                dataIndex: 'name',
                pdfwidth:50
            },
            {
                header: WtfGlobal.getLocaleText("acc.invoiceList.status"),
                dataIndex: 'status',
                pdfwidth:50,
                renderer : function(val, meta, rec, row, col, store){
                    if(rec.get('statusId') === 2){
                        return "Ready for Collection"
                    }
                    return val;
                }
            },{
                header: WtfGlobal.getLocaleText("acc.field.CostCenter"),
                dataIndex: 'costcenter',
                pdfwidth:50
            },{
                header: WtfGlobal.getLocaleText("acc.stock.ProjectNo"),//26
                dataIndex: 'projectnumber',
                pdfwidth:50
            },{
                header: WtfGlobal.getLocaleText("acc.invoice.gridRemark"), //27
                dataIndex: 'remark',
                pdfwidth:50,
                renderer:function(value,meta,rec){
                    if(value!=""){
                        meta.attr = "Wtf:qtip='"+value+"' Wtf:qtitle='Remark' ";
                    }
                    return value;
                }
            },{
                header: WtfGlobal.getLocaleText("acc.stock.DateOfCollection"),
                dataIndex: "collectedOn",
                hidden:this.type == 2?false:true,
                fixed:this.type == 2?false:true,
                pdfwidth:50
//                renderer: function(v){
//                    var date = new Date(v.replace(/[-]/gi, '/'));
//                    var date1;
//                    if(v != undefined && v != ""){
//                        date1 = date.format('Y-m-d');
//                    }
//                    return date1;
//                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.Approvedby"),  //26
                dataIndex:'approvedBy',
                align:"right",
                pdfwidth:100,
                renderer: function(v,m,rec){
                    if(rec.get('statusId') != 3){
                        return v;
                    }else return "";
                }
                
               
            },
            {
                header: WtfGlobal.getLocaleText("acc.contract.product.replacement.Rejected"),  //26
                dataIndex:'approvedBy',
                align:"right",
                hidden:true,
                pdfwidth:100,
                renderer: function(v,m,rec){
                    if(rec.get('statusId') === 3){
                        return v;
                    }else return "";
                }
            },{
                header: WtfGlobal.getLocaleText("acc.customerList.gridCreationDate"),
                //sortable:true,
                dataIndex: 'date',
                pdfwidth:50
//                renderer: function(v){
//                    var date = v;
//                    if(v != undefined && v != ""){
//                        date = v.format('Y-m-d');
//                    }
//                    return date;
//                }
            }];
            
        // appening custom columns

        if (this.type === 1 || this.type === 2 || this.type === 3) {
            this.moduleid = Wtf.Acc_Stock_Request_ModuleId;    
            colArr = WtfGlobal.appendCustomColumn(colArr, GlobalColumnModelForReports[this.moduleid], true, undefined, true);// appening Stock Request Global custom columns
            var colModelArray = GlobalColumnModelForReports[this.moduleid];
            WtfGlobal.updateStoreConfig(colModelArray, this.ds);

            colArr = WtfGlobal.appendCustomColumn(colArr, GlobalColumnModel[this.moduleid]);// appening Stock Request line custom columns
            colModelArray = GlobalColumnModel[this.moduleid];
            WtfGlobal.updateStoreConfig(colModelArray, this.ds);
        }
        if (this.type === 2) {
            this.moduleid = Wtf.Inventory_ModuleId;    
            colArr = WtfGlobal.appendCustomColumn(colArr, GlobalColumnModelForReports[this.moduleid], true, undefined, true);// appening Stock Issue Global custom columns
            var colModelArray = GlobalColumnModelForReports[this.moduleid];
            WtfGlobal.updateStoreConfig(colModelArray, this.ds);

            colArr = WtfGlobal.appendCustomColumn(colArr, GlobalColumnModel[this.moduleid]);// appening Stock Issue line custom columns
            colModelArray = GlobalColumnModel[this.moduleid];
            WtfGlobal.updateStoreConfig(colModelArray, this.ds);
        }
                
        this.cm = new Wtf.grid.ColumnModel(colArr);
        if (this.type === 2) {
            this.cm = setHiddenDuplicateCustomColumn(this.cm);
        }
        this.approveButton = new Wtf.Button({
            text: 'Approve',
            scope: this,  
            disabled: true,
            tooltip: {
               
                text:"Approve selected items"
            },
            handler:function(){
                if((true)){
                    this.acceptFunction("process","",false);
                }else if(false){
                    this.acceptFunction("approve by Supervisor","");
                }                
            }
        });		
        
        this.remark="";
        
        this.rejectButton = new Wtf.Button({
            text: 'Reject',
            scope: this,
            disabled: true,
            tooltip: {
                
                text:"Reject selected items"
            },
            handler:function(){
                if((true)){
                    this.remarkfunction("process",true);
                }else if(true){
                    this.remarkfunction("reject by Supervisor");
                                
                }                 
            }
        });
        
        this.rejectOrdButton= new Wtf.Button({
            text:'Reject',
            tooltip: {
                text:"Click to Reject Order"
            },
            iconCls:getButtonIconCls(Wtf.etype.menudelete),
            id:'reject',
            handler:function(){
                this.remarkfunction("reject");
            },
            scope:this
        });
        this.deleteButton= new Wtf.Button({
            text:'Delete',
            tooltip: {
                text:"Click to Reject Order"
            },
            iconCls:getButtonIconCls(Wtf.etype.menudelete),
            id:'delete',
            handler:this.deleteHandler,
            scope:this
        });
        this.addButton= new Wtf.Button({
            text:'Add',
            iconCls:getButtonIconCls(Wtf.etype.add),
            id:"add",
            scope:this
        });
        this.processButton= new Wtf.Button({
            text:'Issue',
            tooltip: {
                text:"Click to Issue Item"
            },
            id:"process",
            iconCls:getButtonIconCls(Wtf.etype.sync),
            scope:this,
            handler:function(){
                var selected = this.grid.getSelections();
                
                if(selected.length >= 1){
                    if(!this.isValidRequestSelection(selected)){
                        WtfComMsgBox(["Alert", "You can select multiple requests of same order only."],3);
                        return false;
                    }
                    if(this.isValidDetails(selected)){
                        this.validateDetailsAndSubmit(selected);
                    }else{
                        var confirmMsg = "Do you want to issue stock from default warehouse and location? <br>";
                        Wtf.MessageBox.confirm("Confirm",confirmMsg, function(btn){
                            if(btn == 'yes') {
                                this.issueStockFromDefaultLocationAndWarehouse(selected);
                            }else{
                                this.validateDetailsAndSubmit(selected);
                            }
                        },this);
                    }
                    
//                    var issuedQty=selected[0].get("nwquantity"); 
//                    var orderedQty=selected[0].get("quantity"); //in ordering uom
//                    var orderToStockUOMFactor=selected[0].get("orderToStockUOMFactor");
//                    var availableQty=selected[0].get("availabelQuantity"); // in primary uom
//                    
//                    if(this.type==1 && issuedQty==0){
//                        WtfComMsgBox(["Alert", "Issued quantity cannot be 0."],3);
//                        return;
//                    }
//                    
//                    if(issuedQty!='' && issuedQty != undefined && this.type==1){ //this.type==1 ie. store order ie. stock issue tab
//                        if(issuedQty>orderedQty){
//                            WtfComMsgBox(["Alert", "Issued quantity cannot be greater than Ordered quantity."],3);
//                            return;
//                        }
//                        var convertedIssuedQtyInPrimaryQty= orderToStockUOMFactor * issuedQty; // issueqty in primary uom
//                        var maxAllowed= Math.floor(availableQty/orderToStockUOMFactor);
//                        
//                    }
                    
                    //                    this.showLocationSelectWindow(this.grid);
//                    this.showStockDetailWindowForIssue(selected[0])
                    
//                }else if(selected.length > 1){
//                    WtfComMsgBox(["Alert", "Please select 1 record at a time."],3);
//                    return;
                }else{
                    WtfComMsgBox(["Alert", "Please select a record."],3);
                    return;
                }
            }
        });
        this.printButton= new Wtf.menu.Item({
            text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.inventory.stockrequest.grid.printmenu.ordernoteTTip")+"'>"+WtfGlobal.getLocaleText("acc.inventory.stockrequest.grid.printmenu.ordernote")+"</span>",
            iconCls: 'pwnd printButtonIcon',
            handler:function (){
                var selected= this.sm.getSelections();
                var cnt = selected.length;
                if (cnt > 0) {
                    printout("printorder",this.ds.query("transfernoteno", selected[0].data.transfernoteno));
                }else{
                    return;
                }
            },
            disabled:true,
            scope:this
        });
        this.printDelButton= new Wtf.menu.Item({
            text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.inventory.stockrequest.grid.printmenu.deliverynoteTTip")+"'>"+WtfGlobal.getLocaleText("acc.inventory.stockrequest.grid.printmenu.deliverynote")+"</span>",
            iconCls: 'pwnd printButtonIcon',
            handler:function (){
                var selected= this.sm.getSelections();
                var cnt = selected.length;
                if (cnt > 0) {
                    printout("printissue",this.ds.query("transfernoteno", selected[0].data.transfernoteno));
                }else{
                    return;
                }
            },
            disabled:true,
            scope:this
        });
        this.printRetButton= new Wtf.menu.Item({
            text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.inventory.stockrequest.grid.printmenu.returnnoteTTip")+"'>"+WtfGlobal.getLocaleText("acc.inventory.stockrequest.grid.printmenu.returnnote")+"</span>",
            iconCls: 'pwnd printButtonIcon',
            handler:function (){
                var selected= this.sm.getSelections();
                var cnt = selected.length;
                if (cnt > 0) {
                    printout("printreturn",this.ds.query("transfernoteno", selected[0].data.transfernoteno));
                }else{
                    return;
                }
            },
            disabled:true,
            scope:this
        });
        
        this.printIssueButton= new Wtf.menu.Item({
            text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.inventory.stockrequest.grid.printmenu.issuenoteTTip")+"'>"+WtfGlobal.getLocaleText("acc.inventory.stockrequest.grid.printmenu.issuenote")+"</span>",
            iconCls: 'pwnd printButtonIcon',
            handler:function (){
                var selected= this.sm.getSelections();
                var cnt = selected.length;
                if (cnt > 0) {
                    printout("printissueNote",this.ds.query("transfernoteno", selected[0].data.transfernoteno));
                }else{
                    return;
                }
               
            },
            disabled:true,
            scope:this
        });
        
        this.NotePrintMenu = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.inventory.stockrequest.grid.print"),
            hidden:(WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.printstockreq)),
            iconCls: 'pwnd printButtonIcon',
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.inventory.stockrequest.grid.printTTip")
            },
            scope:this,
            menu: [
            this.printButton,
            this.printDelButton,
            this.printRetButton,
            this.printIssueButton
            ]
        }),
        this.editButton= new Wtf.Button({
            text:'Collect',
            tooltip: {
                text:"Click to Collect Orders"
            },
            iconCls:getButtonIconCls(Wtf.etype.sync),
            id:"accept",
            handler:function(){
                var recs = this.grid.getSelections();
                if(recs.length >= 1){
                    if(!this.isValidRequestSelection(recs)){
                        WtfComMsgBox(["Alert", "You can select multiple requests of same order which are pending 'Ready for Collection' only."],3);
                        return false;
                    }
                    if(this.isValidDetails(recs)){
                        this.validateDetailsAndSubmit(recs);
                    }else{
                        var confirmMsg = "Do you want to collect stock in a single location for requested warehouse of all selected requests ? <br>";
                        Wtf.MessageBox.confirm("Confirm",confirmMsg, function(btn){
                            if(btn == 'yes') {
                                this.selectDefaultLocation(recs);
                                
                            }else{
                                this.validateDetailsAndSubmit(recs);
                            }
                        },this);
                    }
                }else{
                    WtfComMsgBox(["Alert", "Please select a record."],3);
                    return false;
                }
//                var totalSelected=recs.length;
//                if(totalSelected != 1){
//                    WtfComMsgBox(["Warning", "Please select only 1 record at a time."],0);
//                    return ;
//                }else{
//                    //                    this.stockCollectionWindow(this.grid);
//                    this.showStockDetailWindowForCollect(recs[0]);
//
//                }
                
            },
            scope:this
        });
        
        this.acceptRetQtyBtn= new Wtf.Button({
            text:'Accept Return Quantity',
            tooltip: {
                text:"Click to accept Return Quantity"
            },
            iconCls:getButtonIconCls(Wtf.etype.add),
            id:'acceptreturn',
            disabled:true,
            handler:function(){
                this.acceptReturnedItem();
            },
            scope:this
        });
        this.delTransaction= new Wtf.Button({
            text:'Delete',
            tooltip: {
                text:"Click to delete issue note"
            },
            iconCls:getButtonIconCls(Wtf.etype.add),
            id:'deletetransaction',
            disabled:false,
            handler:function(){
                this.deleteRecord();
            },
            scope:this
        });
        this.menuItem={
            csv :true,
            pdf :true,
            xls:true
        };
        if(this.type==Wtf.GOODS_PENDING_ORDER){
            this.menuItem.csv = false,
            this.menuItem.pdf = false
        }
        this.exportButton = new Wtf.exportButton({
            obj: this,
            iconCls : "pwnd exportChrome",
            filename: (this.type == Wtf.GOODS_PENDING_ORDER) ? WtfGlobal.getLocaleText("acc.stock.GoodsPendingOrders"):'Fullfilled Orders',
            tooltip: "Export Report", 
            menuItem:this.menuItem,
            get:(this.type == Wtf.GOODS_PENDING_ORDER)? Wtf.autoNum.GoodsPendingOrdersRegister :Wtf.autoNum.FullfilledOrdersRegister,
            label:"Export"
        });
                 /*Print Record Button*/
        this.printMenu = new Wtf.menu.Menu({
            id: "printmenu" + this.id,
            cls : 'printMenuHeight'
        });
        
        var colModArray=[];
        if(this.type==2){//Fullfilled Orders
            colModArray = GlobalCustomTemplateList[Wtf.Inventory_ModuleId];
        } else{ //Stock Issue-Store Orders,Stock Request -Store Orders
            colModArray = GlobalCustomTemplateList[Wtf.Acc_Stock_Request_ModuleId];
        }
        
        var isTflag=colModArray!=undefined && colModArray.length>0?true:false;
        if(isTflag){
            for (var count = 0; count < colModArray.length; count++) {
                var id1=colModArray[count].templateid;
                var name1=colModArray[count].templatename;           
                Wtf.menu.MenuMgr.get("printmenu" + this.id).add({                  
                    iconCls: 'pwnd printButtonIcon',
                    text: name1,
                    id: id1
                }); 
            }           
        }else{
            Wtf.menu.MenuMgr.get("printmenu" + this.id).add({                  
                iconCls: 'pwnd printButtonIcon',
                text:WtfGlobal.getLocaleText("acc.field.TherearenotemplatesinCustomDesigner"),
                id: Wtf.No_Template_Id
            });
        }
        Wtf.menu.MenuMgr.get("printmenu" + this.id).on('itemclick',function(item) {
            this.printRecordTemplate('print',item);
        }, this);
        
        this.singleRowPrint = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.rem.236"),
            hidden:(WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.printstockreq)),
            iconCls:'pwnd printButtonIcon',
            tooltip:WtfGlobal.getLocaleText("acc.rem.236.single"),
            scope:this,
            disabled:true,
            menu:this.printMenu
        })
        
        
        var tbarArray = [];
        tbarArray.push("-",WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.fromdate")+" : ",this.frmDate,"-",WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.todate")+" : ",this.toDate);
        if(this.type == 2){
            tbarArray.push("-",WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore")+" : ",this.storeCmbfilter);
        }
        tbarArray.push("-",WtfGlobal.getLocaleText("acc.contract.product.replacement.Status"),this.statusCmbfilter,"-",this.search,"-",this.resetBtn,"-",this.AdvanceSearchBtn,"-",this.expandCollpseButton); /*To push a button on tbar to show*/
        
        var bbarArray= new Array();
        
        if(this.type == 1){
            //            if(Wtf.realroles[0]==14 && isIncludeQAapprovalFlow){ // Only if QA user and QA approval flow is included then only see these button
            //bbarArray.push("-",this.approveButton);
            //bbarArray.push("-",this.rejectButton);            
            //            }else{
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.issuestockreq)) {
                bbarArray.push("-",this.processButton);
            }
            // here issuestockreq is taken becoz the person whom issue permision is given can reject order
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.issuestockreq)) {  
                bbarArray.push("-",this.rejectOrdButton);
            }
            
            
            
        //            }                        
        }           
        if(this.type == 3){
            //            if(Wtf.realroles[0]==27 && isIncludeSVapprovalFlow){ // Only if SuperVisor user and SuperVisor approval flow is included then only see these button
            //            bbarArray.push("-",this.approveButton);
            //            bbarArray.push("-",this.rejectButton);            
            //            }else{
            
           
            if (!WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.createstockreq) && Wtf.account.companyAccountPref.deleteTransaction) {
                bbarArray.push(this.deleteButton);
            }
            if (!WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.collectstockreq)) {
                bbarArray.push(this.editButton);
            }
            // here collectstockreq is taken becoz the person whom collect permision is given can return item 
            if (!WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.collectstockreq)) {
                bbarArray.push(this.acceptRetQtyBtn);
            }
            if (!WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.exportstockreq)) {
                bbarArray.push("-", this.exportButton);
            }
        //            }                        
        }            
        if(this.type == 2 && !WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.exportstockreq)){
            bbarArray.push("-",this.exportButton);
              bbarArray.push(this.delTransaction);  
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.printstockreq)){
            bbarArray.push("-",this.NotePrintMenu);
        }
        bbarArray.push( this.singleRowPrint);   //Print Single Record
        
        this.grid=new Wtf.KwlEditorGridPanel({
            id:"goodsEditorGridPanel"+this.id,
            cm:this.cm,
            store:this.ds,
            sm:this.sm,
            loadMask:false,
            viewConfig: {
                forceFit: false
            },
            view: grpView,
            searchLabel:WtfGlobal.getLocaleText("acc.help.title.121"),
            searchEmptyText:WtfGlobal.getLocaleText("acc.stock.Searchby"),
            serverSideSearch:true,
            searchField:"transfernoteno",
            displayInfo: true,
            displayMsg: 'Displaying  {0} - {1} of {2}',
            emptyMsg: "No results to display",
            editable:true,
            clicksToEdit:1,
            tbar:tbarArray,
            bbar:bbarArray,
            plugins:[this.expander, Wtf.ux.grid.plugins.GroupCheckboxSelection]
        });
        
        var colModelArray = GlobalColumnModelForReports[Wtf.Acc_Stock_Request_ModuleId];
        this.updateStoreConfig(colModelArray, this.ds);
        colModelArray = GlobalColumnModel[Wtf.Acc_Stock_Request_ModuleId];
        WtfGlobal.updateStoreConfig(colModelArray, this.ds);
        
        var colModelArray = GlobalColumnModelForReports[Wtf.Inventory_ModuleId];
        this.updateStoreConfig(colModelArray, this.ds);
        colModelArray = GlobalColumnModel[Wtf.Inventory_ModuleId];
        WtfGlobal.updateStoreConfig(colModelArray, this.ds);
        
        Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
            Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
        },this);
        if(this.type==2){ //type 2 = fulfilled orders tab
            this.cm.setHidden(2,false);  //if fulfilled orders tab then show expander
            this.cm.setHidden(1,true);//if fulfilled orders tab then hide checkbox 
            this.cm.setHidden(16,true);//if fulfilled orders tab then hide total ordered quantity
        }else{
            this.cm.setHidden(2,true);
            this.cm.setHidden(1,false);
            this.cm.setHidden(16,false);
            this.cm.setHidden(27,true);
        } 
        
        var arrId = [];
        arrId.push("delete");
        this.ok=1;
        
        this.grid.on("cellclick", this.onCellClick, this);
        this.grid.on("afteredit",function(obj){
            var rec=obj.record;
            var issuedQuantity=rec.data.nwquantity;
            var orderedQuantity=rec.data.quantity;
            if(this.type == 1 && rec.data.status === "Pending QA Approval" && Wtf.realroles[0]==14 && isIncludeQAapprovalFlow){
                if(obj.value <= issuedQuantity){
                    return true;
                }else if(obj.value == 0){
                    return true;
                }else if(obj.value > issuedQuantity){
                    Wtf.Msg.show({
                        title:'Info',
                        msg: 'Entered quantity cannot be greater than Issued quantity.',
                        buttons: Wtf.Msg.OK,
                        animEl: 'elId',
                        icon: Wtf.MessageBox.INFO
                    });
                    rec.set("delquantity",obj.value); 
                }
            }else if(obj.field === "delquantity" && this.type === 3 && (rec.get('status') === "Ready For Collection" || rec.get('status') === "Issued")){
                if(obj.value > issuedQuantity){
                    Wtf.Msg.show({
                        title:'Info',
                        msg: 'Delivered quantity cannot be greater than Issued quantity.',
                        buttons: Wtf.Msg.OK,
                        animEl: 'elId',
                        icon: Wtf.MessageBox.INFO
                    });
                    rec.set("delquantity",issuedQuantity);
                    return false;
                } else if(rec.data.isSerialForProduct){
                    var v = obj.value;
                        v = String(v);
                        var ps = v.split('.');
                        var sub = ps[1];
                        if (sub!=undefined && sub.length > 0) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.exceptionMsgForDecimalQty")], 2);
                            rec.set("delquantity",issuedQuantity);
                            return false;
                        }
                }else{
                    rec.set("delquantity",obj.value);
                    this.grid.getView().refresh();
                    return true;
                }
                    
            }else if(obj.field === "nwquantity" && this.type === 1 && (rec.get('status') === "Ordered")){
                if(obj.value > orderedQuantity){
                    Wtf.Msg.show({
                        title:'Info',
                        msg: 'Issued quantity cannot be greater than Ordered quantity.',
                        buttons: Wtf.Msg.OK,
                        animEl: 'elId',
                        icon: Wtf.MessageBox.INFO
                    });
                    rec.set("nwquantity",orderedQuantity);
                    return false;
                } else if(rec.data.isSerialForProduct){
                    var v = obj.value;
                        v = String(v);
                        var ps = v.split('.');
                        var sub = ps[1];
                        if (sub!=undefined && sub.length > 0) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.exceptionMsgForDecimalQty")], 2);
                            rec.set("nwquantity",orderedQuantity);
                            return false;
                        }
                }else{
                    rec.set("nwquantity",obj.value);
                    this.grid.getView().refresh();
                    return true;
                }
                    
            } else
                return true;
        },this);
        
//         this.grid.on("beforeedit",function(){ 
//             return false;
//         },this);
        this.grid.on("beforeedit", function(e){
            if(e.record.get("statusId") === 2 && e.field === "nwquantity")
                return false;
            else if(e.record.get("status") === "Ordered" && e.field === "delquantity")
                return false;
            else
                return true;
        },this);
        this.grid.on("validateedit",this.validateeditFunction,this);
        this.grid.on("statesave",this.statesaveFunction,this);
        this.sm.on("selectionchange",function(){
            var selected = this.sm.getSelections();
            if(selected.length>0){
                var test = 0;
                var prvorderNo="NA";
                for(var i1=0;i1<selected.length;i1++){
                    var orderNo=selected[i1].get("transfernoteno");
                    if(prvorderNo!="NA"){
                        if(orderNo!=prvorderNo ){
                            this.printButton.disable();
                            this.printDelButton.disable();
                            this.printRetButton.disable();
                            this.printIssueButton.disable();
                            return;
                        }
                    }else{
                    //                        this.printButton.enable();
                    //                        this.printDelButton.enable();
                    //                        this.printRetButton.enable();
                    }
                    prvorderNo=orderNo;
                }
                prvorderNo="NA";
                this.singleRowPrint.enable();  
                for(var i=0;i<selected.length;i++){
                      
                    if(this.type === 1 && selected[i].get("status") === "Issued" || this.type === 1 && selected[i].get("status") === "Ready For Collection" ){
                        this.processButton.disable();
                        this.rejectOrdButton.disable();
                        this.approveButton.disable();//QA Approve
                        this.rejectButton.disable();//QA Reject
                        this.printButton.enable();
                        this.printDelButton.enable();
                        this.printRetButton.disable();
                        this.printIssueButton.disable();
                        this.acceptRetQtyBtn.disable();
                        return;
                    }else if(this.type === 3 && selected[i].get("status") === "Ordered"){
                        this.editButton.disable();
                        this.approveButton.disable();//QA Approve
                        this.rejectButton.disable();//QA Reject
                        this.printButton.enable();
                        this.printDelButton.disable();
                        this.printRetButton.disable();
                        this.acceptRetQtyBtn.disable();
                        this.printIssueButton.disable();
                        return;
                    }else if((selected[i].get("status") === "Pending QA Approval" && true)||(selected[i].get("status") === "Pending Supervisior Approval" && true)){
                        this.processButton.disable();
                        this.editButton.disable();
                        this.rejectOrdButton.disable();
                        this.approveButton.enable();//QA Approve
                        this.rejectButton.enable();//QA Reject 
                        this.acceptRetQtyBtn.disable();
                        return;
                    }else if(selected[i].get("status") === "Pending Request Approval" && this.type==1){
                        this.processButton.disable();
                        this.editButton.disable();
                        this.rejectOrdButton.disable();
                        this.approveButton.enable();//QA Approve
                        this.rejectButton.enable();//QA Reject
                        this.acceptRetQtyBtn.disable();
                        return;
                    }else if(this.type==3){
                        if(selected[i].get("status") === "Issued" && selected.length==1){
                            this.editButton.enable();
                            this.acceptRetQtyBtn.disable();
                            this.printButton.enable();
                            this.printDelButton.enable();
                        }else if(selected[i].get("status") === "Issued" && selected.length>1){
                            this.editButton.enable();
                        }else if(selected[i].get("status") === "Return Request" && selected.length==1){
                            this.acceptRetQtyBtn.enable();
                            this.editButton.disable();
                        }else{
                            this.editButton.disable();
                            this.acceptRetQtyBtn.disable();
                        }
                        this.processButton.disable();
                        this.rejectOrdButton.disable();
                        this.approveButton.disable();//QA Approve
                        this.rejectButton.disable();//QA Reject   
                        return;
                        
                    }
                    else{
                        this.editButton.enable();//collect
                        this.rejectOrdButton.enable();//reject
                        this.processButton.enable();//issue                         
                        this.approveButton.disable();//QA Approve
                        this.rejectButton.disable();//QA Reject
                        this.acceptRetQtyBtn.disable(); // accept return qty
                    }
                    if(selected[i].get("status") === "Ready For Collection"){
                        this.printButton.enable();
                        this.printDelButton.enable();
                        this.printRetButton.disable();
                        this.printIssueButton.disable();
                    }else if(selected[i].get("status") === "Ordered"){
                        this.printButton.enable();
                        this.printDelButton.disable();
                        this.printRetButton.disable();
                        this.printIssueButton.disable();
                    }else if(selected[i].get("status") === "Collected"){
                        if((selected[i].get("issuedOnFull") != undefined && selected[i].get("collectedOnFull") != undefined)?selected[i].get("issuedOnFull") === selected[i].get("collectedOnFull"):false){
                            this.printButton.disable();
                            this.printDelButton.disable();
                            this.printRetButton.disable();
                            this.printIssueButton.enable();
                        }else{
                            this.printButton.enable();
                            this.printDelButton.enable();
                            if (selected[i].get("isReturnRequest")) {
                                this.printRetButton.disable();
                            } else {
                                this.printRetButton.enable();
                            }
                            this.printIssueButton.disable();
                        }
                    }else if(selected[i].get("status") === "Issued"){
                        this.printButton.enable();
                        this.printDelButton.enable();
                        this.printRetButton.disable();
                        this.printIssueButton.disable();
                    }
                    
                     
                }
            }else{
                this.editButton.disable();
                this.rejectOrdButton.disable();
                this.processButton.disable();
                this.acceptRetQtyBtn.disable();
                this.printButton.disable();
                this.printDelButton.disable();
                this.printRetButton.disable();
                this.printIssueButton.disable();
                this.singleRowPrint.disable();
            }

        },this);
        
        this.ds.on('beforeload',function(){
            WtfGlobal.setAjaxTimeOutFor30Minutes();
        }, this);
        this.ds.on("load",function(s,r){
            WtfGlobal.resetAjaxTimeOut();
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand")); /*sets the button name to Expand after loading new request*/
            this.editButton.disable();
            this.rejectOrdButton.disable();
            this.processButton.disable();
            this.storeloaded(s);
            this.quanarr = [];
            var i;
            for(i=0 ; i<s.getCount() ; i++){
                this.quanarr.push(s.data.items[i].data.quantity);
            }

        },this);
        
           this.ds.on('loadexception',function(){
            WtfGlobal.resetAjaxTimeOut();
        },this);
        //        this.add(this.grid);
        
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.grid.colModel,
            moduleid: Wtf.Acc_Stock_Request_ModuleId,
            advSearch: false
        });

        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
        this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
        
        this.leadpan = new Wtf.Panel({
            border:false,
            layout : "border",
            items:[this.objsearchComponent,{
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid]
            }]
        });

        this.add(this.leadpan);
    
        this.on("activate",function() {
            if(this.type ==2){
             
                this.storeCmbStore.on('load',function(a,rec,m){
                    var storeIdSetPreviously=this.storeCmbfilter.getValue();
                    var index =this.storeCmbStore.find('fullname',"ALL");
                    if(index == -1 && rec.length > 1){
                        var newRec=new this.storeCmbRecord({
                            store_id:'',
                            fullname:'ALL'
                        });
                        this.storeCmbStore.insert(0,newRec);
                        this.storeCmbfilter.setValue("",true);
                    }
                    if(storeIdSetPreviously != undefined && storeIdSetPreviously != ""){
                        this.storeCmbfilter.setValue(storeIdSetPreviously, true);
                    }
                    this.storeCmbfilter.fireEvent('select');
                    this.initloadgridstore(this.frmDate.getValue().format('Y-m-d'),this.toDate.getValue().format('Y-m-d'),this.storeCmbfilter.getValue());
                }, this);
                this.storeCmbStore.load();            
            }
            else {
            
                this.ds.load({
                    params:{
                        start:0,
                        limit:30
                    }
                });
            }
        },this);
        this.storeCmbfilter.on("select", function(cmb, rec, index){
            if(rec != undefined){
                if(rec.data.dmflag =="1") {
                    this.dmflag = 1;
                } else {
                    this.dmflag = 0;
                }
            }
        }, this);
        var action = 3
        switch(action) {
            case 4:
                var format = Wtf.getDateFormat();
                break;
            default:
                break;
        }
    },
    serialRenderer:function(v,m,rec){
        if((this.type ===3 && rec.get('statusId') === 2) || (this.type ===1 && rec.get('statusId') === 0)){
            return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.serial.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.serial.desc.title")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
        }else{
            return "";
        }
        
    },
    onCellClick :function(grid, rowIndex, columnIndex, e){
        
        var record = grid.getStore().getAt(rowIndex);  // Get the Record
        var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
        
        var linedata = [];//disable Stock Request custom column editing on cell click
        linedata = WtfGlobal.appendCustomColumn(linedata, GlobalColumnModelForReports[Wtf.Acc_Stock_Request_ModuleId], true, undefined, true);
        linedata = WtfGlobal.appendCustomColumn(linedata, GlobalColumnModel[Wtf.Acc_Stock_Request_ModuleId]);
        
        if (this.type === 2) {//disable Stock Issue custom column editing on cell click
            linedata = WtfGlobal.appendCustomColumn(linedata, GlobalColumnModelForReports[Wtf.Inventory_ModuleId], true, undefined, true);
            linedata = WtfGlobal.appendCustomColumn(linedata, GlobalColumnModel[Wtf.Inventory_ModuleId]);
        }
        for (var lineFieldCount = 0; lineFieldCount < linedata.length; lineFieldCount++) {
            if (linedata[lineFieldCount].dataIndex === fieldName) {
                return false;
            }
        }
        
        if(e.getTarget('.serialNo-gridrow')){
            if(fieldName == "issueDetails"){
                if(record.data.quantity==0){
                    WtfComMsgBox(["Warning", "Please fill issue quantity."],0);
                    return false;
                }
                this.showStockDetailWindowForIssue(record);
            }else if(fieldName == "collectDetails"){
                if(record.data.quantity==0){
                    WtfComMsgBox(["Warning", "Please fill collect quantity."],0);
                    return false;
                }
                this.showStockDetailWindowForCollect(record);
            }
        }
        
    },
    /*handler function for expandCollpseButton Grouping Grid
     */
     expandCollapseGrid : function(btntext){

        if (btntext == WtfGlobal.getLocaleText("acc.field.Collapse")) {
            /*If button text is collapse then collapse all rows*/
            this.grid.getView().collapseAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if (btntext == WtfGlobal.getLocaleText("acc.field.Expand")) {
            /*If button text is expand then expand all rows*/
            this.grid.getView().expandAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    },
    
    /*handler function for expandCollpseButton MultiGrouping Grid
     */
    expandCollapseMultiGroupingGrid : function(btntext){
        if(btntext == WtfGlobal.getLocaleText("acc.field.Collapse")){
            for(var i=0; i< this.grid.getStore().data.length; i++){
                this.expander.collapseRow(i)
            }
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if(btntext == WtfGlobal.getLocaleText("acc.field.Expand")){
            for(var i=0; i< this.grid.getStore().data.length; i++){
                var rec=this.grid.getStore().getAt(i);
                if(rec.get('status') == "Ordered" || rec.get('status') == "Rejected"){            
                /*
                    *The records having status Ordered or Rejected will not get 
                    * expanded as they don't contain data.
                    *
                    */
                }
                else{
                    this.expander.expandRow(i)
                }
            }
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    },
    isValidRequestSelection: function(selectedRecs){
        var transNo = null;
        var valid = true;
        for(var i=0; i<selectedRecs.length; i++){
            var rec = selectedRecs[i];
            if(i==0){
                transNo = rec.get('transfernoteno');
            }else{
                if(transNo !== rec.get('transfernoteno')){
                    valid = false;
                    break;
                }
            }
            if(this.type === 1){
                if(rec.get('statusId') !== 0){
                    valid = false;
                    break;
                }
            } else if(this.type === 3){
                if(rec.get('statusId') !== 2){
                    valid = false;
                    break;
                }
            }
        }
        return valid;
    },
    isValidDetails: function(selectedRecs){
        var valid = true;
        for(var i=0; i<selectedRecs.length; i++){
            var rec = selectedRecs[i];
            var detailQty = 0;
            var fillQty = 0;
            if(this.type == 1){
                detailQty = rec.get('stockDetailsIssueQuantity');
                fillQty=rec.get("nwquantity");
            }else if(this.type == 3){
                detailQty = rec.get('stockDetailsCollectQuantity');
                fillQty=rec.get("delquantity");
            }
            if(!detailQty || detailQty <= 0 || detailQty !== fillQty){
                valid = false;
            }
        }
        return valid;
    },
    issueStockFromDefaultLocationAndWarehouse: function(selectedRecs){
        var valid = true;
        for(var i=0; i<selectedRecs.length; i++){
            var rec = selectedRecs[i];
            var issuedQty=rec.get("nwquantity");
            var detailQty=rec.get('stockDetailsIssueQuantity')
            if(!detailQty ||detailQty <= 0 || detailQty !== issuedQty ){
                var orderedQty=rec.get("quantity"); //in ordering uom
                var orderToStockUOMFactor=rec.get("orderToStockUOMFactor");
                var availableQty=rec.get("availabelQuantity"); // in primary uom
                var isBatchForProduct=rec.get("isBatchForProduct");
                var isSerialForProduct=rec.get("isSerialForProduct");
                var isRowForProduct=rec.get("isRowForProduct");
                var isRackForProduct=rec.get("isRackForProduct");
                var isBinForProduct=rec.get("isBinForProduct");
                var defaultLocForProd=rec.get("defaultLocId");
                var defaultStoreForProduct=rec.get("defaultStoreId");
                var defaultStoreNameForProduct=rec.get("defaultStoreName");
                var defaultAvailQty=rec.get("defaultAvailQty");
                var isDefaultAllowed = !(isBatchForProduct || isSerialForProduct || isRowForProduct || isRackForProduct || isBinForProduct);
                var isNegativeAllowed = Wtf.account.companyAccountPref.isnegativestockforlocwar && !(isBatchForProduct || isSerialForProduct);
                if(this.type==1 && issuedQty!='' && issuedQty != undefined && issuedQty===0 && issuedQty > orderedQty){
                    WtfGlobal.highLightRowColor(this.grid,rec,true,0,2,true);
                    valid = false;
                    continue;
                }
                if(!isDefaultAllowed){
                    WtfGlobal.highLightRowColor(this.grid,rec,true,0,2,true);
                    valid = false;
                    continue;
                }else{
                    var convertedIssuedQtyInPrimaryQty= orderToStockUOMFactor * issuedQty; // issueqty in primary uom
                    //                var maxAllowed= Math.floor(availableQty/orderToStockUOMFactor);
                    if(!isNegativeAllowed && (defaultAvailQty < convertedIssuedQtyInPrimaryQty) || !defaultStoreForProduct || !defaultLocForProd){
                        WtfGlobal.highLightRowColor(this.grid,rec,true,0,2,true);
                        valid = false;
                    }else{
                        var data = {
                            locationId:defaultLocForProd,
                            quantity:convertedIssuedQtyInPrimaryQty,
                            rowId:"",
                            rackId:"",
                            binId:"",
                            batchName:""
                        }
                        rec.set("stockDetailsForIssue", "");
                        rec.set("stockDetailsIssueQuantity","");
                        rec.set("stockDetailsForIssue", [data]);
                        rec.set("stockDetailsIssueQuantity",issuedQty);
                        rec.set("tostore", defaultStoreForProduct);
                        rec.set("tostorename", defaultStoreNameForProduct);
                        
                    }
                }
            }
        }
        if(!valid){
            WtfComMsgBox(["Warning", "Please fill valid details for highlighted records"], 1);
            this.grid.getSelectionModel().clearSelections();
            return false;
        }else{
            this.acceptFunction("issue","");
        }
    },
    selectDefaultLocation: function(selectedRecs){
        var rec = selectedRecs[0];
        
        this.win = new Wtf.DefaultLoationCollectWin({
            id: "defaultlocationcollectwin",
            border : false,
            title : "Set Collect Location",
            layout : 'fit',
            closable: true,
            width:450,
            height:300,
            modal:true,
            storeId:rec.get('fromstore'),
            defaultLocationId:rec.get('defaultCollectionLocId'),
            resizable:false
        });
        this.win.on('locationSelected', function(locationId, locationName){
            for(var i=0; i<selectedRecs.length; i++){
                var r= selectedRecs[i];
                r.set("defaultCollectionLocId", locationId);
                r.set("defaultCollectionLocName", locationName);
            }
            this.collectInDefaultLocation(selectedRecs);
        },this);
        this.win.show();
        
    },
    collectInDefaultLocation: function(selectedRecs){
        var valid = true;
        for(var i=0; i<selectedRecs.length; i++){
            var rec = selectedRecs[i];
            var collectQty=rec.get("delquantity"); 
            var detailQty=rec.get("stockDetailsCollectQuantity"); 
            if(!detailQty ||detailQty <= 0 || detailQty !== collectQty ){
                var orderedQty=rec.get("quantity"); //in ordering uom
                var orderToStockUOMFactor=rec.get("orderToStockUOMFactor");
                var availableQty=rec.get("availabelQuantity"); // in primary uom
                var isBatchForProduct=rec.get("isBatchForProduct");
                var isSerialForProduct=rec.get("isSerialForProduct");
                var isRowForProduct=rec.get("isRowForProduct");
                var isRackForProduct=rec.get("isRackForProduct");
                var isBinForProduct=rec.get("isBinForProduct");
                var defaultCollectLocId=rec.get("defaultCollectionLocId");
                var defaultCollectLocName=rec.get("defaultCollectionLocName");
                var issuedStockArray=rec.get("stockDetails");
                var isDefaultAllowed = !(isBatchForProduct || isSerialForProduct || isRowForProduct || isRackForProduct || isBinForProduct);
                if(this.type==3 && collectQty!='' && collectQty != undefined && collectQty===0 && collectQty > orderedQty){
                    WtfGlobal.highLightRowColor(this.grid,rec,true,0,2,true);
                    valid = false;
                    continue;
                }
                if(!isDefaultAllowed){
                    WtfGlobal.highLightRowColor(this.grid,rec,true,0,2,true);
                    valid = false;
                    continue;
                }else{
                    var convertedCollectedQtyInPrimaryQty= orderToStockUOMFactor * collectQty; // issueqty in primary uom
                    if(defaultCollectLocId){
                        var collectedData = [];
                        for(var idx=0; idx < issuedStockArray.length; idx++){
                            var issuedData = issuedStockArray[idx];
                            var data = {
                                fromLocationId: issuedData.issuedLocationId,
                                fromRowId: "",
                                fromRackId: "",
                                fromBinId: "",
                                batchName: "",
                                detailId: issuedData.id,
                                locationId: defaultCollectLocId,
                                toLocationName: defaultCollectLocName
                            }
                            if(convertedCollectedQtyInPrimaryQty == 0){
                                break;
                            }else if(issuedData.issuedQuantity && issuedData.issuedQuantity > 0){
                                if(convertedCollectedQtyInPrimaryQty > issuedData.issuedQuantity){
                                    data.quantity = issuedData.issuedQuantity;
                                    convertedCollectedQtyInPrimaryQty -= issuedData.issuedQuantity;
                                }else{
                                    data.quantity = convertedCollectedQtyInPrimaryQty;
                                    convertedCollectedQtyInPrimaryQty = 0;
                                }
                                collectedData.push(data);
                            }
                            
                        }
                        rec.set("stockDetailsForCollect", "");
                        rec.set("stockDetailsCollectQuantity","");
                        rec.set("stockDetailsForCollect", collectedData);
                        rec.set("stockDetailsCollectQuantity",collectQty);
                    }else{
                        WtfGlobal.highLightRowColor(this.grid,rec,true,0,2,true);
                        valid = false;
                        continue;
                    }
                }
            }
        }
        if(!valid){
            WtfComMsgBox(["Warning", "Please fill valid details for highlighted records"], 1);
            this.grid.getSelectionModel().clearSelections();
            return false;
        }else{
            this.acceptFunction("accept","");
        }
    },
    validateDetailsAndSubmit: function(selectedRecs){
        var valid = true;
        for(var i=0; i<selectedRecs.length; i++){
            var rec = selectedRecs[i];
            var detailQty = 0;
            var fillQty = 0;
            if(this.type == 1){
                detailQty = rec.get('stockDetailsIssueQuantity')
                fillQty = rec.get('nwquantity');
            }else if(this.type == 3){
                detailQty = rec.get('stockDetailsCollectQuantity')
                fillQty = rec.get('delquantity');
            }
            if(!detailQty ||detailQty <= 0 || detailQty !== fillQty ){
                WtfGlobal.highLightRowColor(this.grid,rec,true,0,2,true);
                valid = false;
            }
        }
        if(!valid){
            WtfComMsgBox(["Alert", "Please fill valid details for highlighted records"], 1);
            this.grid.getSelectionModel().clearSelections();
            return false;
        }else{
            if(this.type == 1){
                this.acceptFunction("issue","");
            }else if(this.type == 3){
                this.acceptFunction("accept","");
            }
        }
    },
    add1:function(){
    },
    deleteHandler: function(){
        var recs = this.sm.getSelections();
        if(recs.length == 0){
            WtfComMsgBox(["Alert", "Please select at least one record to delete"], 0);
            return false
        }
        var requestIds = [];
        var requestString = "";
        var notDeletingRequests = "";
        var invalidRec  = false;
        var inValidCount = 0;
        for(var i=0 ; i< recs.length; i++){
            var rec = recs[i];
            requestIds.push(rec.get('id'));
            if(rec.get('status') == "Ordered"){
                requestString += "<br><b>"+(i+1)+").</b> ";
                requestString += WtfGlobal.getLocaleText("acc.stockrequest.RequestNo")+ ":"+" <b>"+rec.get('transfernoteno')+"</b>, ";
                requestString += WtfGlobal.getLocaleText("acc.product.gridProduct")+":"+ "<b>"+rec.get('itemcode')+"</b>, ";
                requestString += WtfGlobal.getLocaleText("acc.stockadjustment.ForStore")+":"+" <b>"+rec.get('fromstorename')+"</b>";
            }else{
                invalidRec = true;
                notDeletingRequests += "<br><b>"+(inValidCount++ +1)+").</b> ";
                notDeletingRequests += WtfGlobal.getLocaleText("acc.stockrequest.RequestNo")+":"+" <b>"+rec.get('transfernoteno')+"</b>, ";
                notDeletingRequests += WtfGlobal.getLocaleText("acc.product.gridProduct")+":"+" <b>"+rec.get('itemcode')+"</b>, ";
                notDeletingRequests += WtfGlobal.getLocaleText("acc.stockadjustment.ForStore")+":"+" <b>"+rec.get('fromstorename')+"</b>";
            }
            
        }
        if(invalidRec){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.stockrequest.cannotdeleterequest")+"<br>"+notDeletingRequests], 1);
            return false
        }
        var confirmMsg = WtfGlobal.getLocaleText("acc.stockrequest.deleterequest")+"<br> "+requestString;
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),confirmMsg, function(btn){
            if(btn == 'yes') {
                WtfGlobal.setAjaxTimeOut();
                Wtf.Ajax.requestEx({
                    url: "INVGoodsTransfer/deleteStockRequest.do",
                    params: {
                        requestIds: requestIds.toString()
                    }
                },
                this,
                function(result, req){
                    WtfGlobal.resetAjaxTimeOut();
                    if(result.success) {
                        var msg = result.msg;
                        WtfComMsgBox(["Success", msg], 0);
                        
                    }else{
                        WtfComMsgBox(["Failure", result.msg], 1);
                    }
                    this.ds.reload();
                }, function(){
                    WtfGlobal.resetAjaxTimeOut();
                    WtfComMsgBox(["Error", "Error occurred while processing your request "], 1);
                });
            }
        }, this);
    },
    updateStoreConfig:function(colModelArray, store) {
        if(colModelArray){
            for(var cnt = 0;cnt < colModelArray.length;cnt++){
                var fieldname = colModelArray[cnt].fieldname;
                var newField = new Wtf.data.ExtField({
                    name:fieldname.replace(".",""),
                    sortDir:'ASC',
                    type:colModelArray[cnt].fieldtype == 3 ?  'date' : (colModelArray[cnt].fieldtype == 2?'float':'auto'),
                    dateFormat:colModelArray[cnt].fieldtype == 3 ?  null : undefined
                });
                store.fields.items.push(newField);
                store.fields.map[fieldname]=newField;
                store.fields.keys.push(fieldname);
            }
            store.reader = new Wtf.data.KwlJsonReader(store.reader.meta, store.fields.items);
        } 
    },

    showStockDetailWindowForIssue : function (record){
        var itemId=record.get("itemId");
        var itemCode=record.get("itemcode");
        var quantity=record.get("nwquantity");
        var isBatchEnable = record.get("isBatchForProduct");
        var isSerialEnable = record.get("isSerialForProduct");
        var isNegativeAllowed = Wtf.account.companyAccountPref.isnegativestockforlocwar && !isBatchEnable && !isSerialEnable;
        var isRackEnable = record.get("isRackForProduct");
        var isRowEnable = record.get("isRowForProduct");
        var isBinEnable = record.get("isBinForProduct");
        var orderToStockUOMFactor=record.get("orderToStockUOMFactor")
        var orderingUomName=record.get("orderinguomname");
        var stockUOMName=record.get("stockuomname");
        var requestedStoreName=record.get("fromstorename");
        var maxQtyAllowed=orderToStockUOMFactor * quantity;
        var itemDefaultWarehouse = record.get("itemdefaultwarehouse");
        
        var warehouseComboRec = new Wtf.data.Record.create([
        {
            name: 'store_id'
        },
        {
            name: 'description'
        },
        {
            name: 'fullname'
        }
        ]);

        var warehouseComboReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },warehouseComboRec);

        var warehouseComboStore = new Wtf.data.Store({
            url: 'INVStore/getStoreList.do',
            reader:warehouseComboReader
        });
       
        
        var warehouseCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            hideLabel:false,
            mode:"local",
            typeAhead:true,
            store:warehouseComboStore,
            forceSelection:true,
            displayField:'fullname',
            valueField:'store_id',
            fieldLabel:"Issuing Store*",
            emptyText:WtfGlobal.getLocaleText("acc.je.SelectToStore"),
            width:200
        });
        
        warehouseComboStore.on("load", function(){
            if(itemDefaultWarehouse != "" && itemDefaultWarehouse != undefined && itemDefaultWarehouse != null){
                var idx = warehouseComboStore.find("store_id",itemDefaultWarehouse);
                if(idx != -1){
                    warehouseCombo.setValue(itemDefaultWarehouse);
                    warehouseCombo.fireEvent('select');
                }
            }
        }, this);
        warehouseCombo.on('render', function(){
            warehouseComboStore.load({
                params:{
                    isActive : true,
                    storeTypes : "0,2",
                    byStoreManager:true
                }
            });
        }, this)
         
        
        var winTitle = WtfGlobal.getLocaleText("acc.stockrequest.StockDetailforStockTransfer");
        var winDetail = String.format(WtfGlobal.getLocaleText("acc.stockrequest.SelectStockdetailsforstocktransfer")+'<br> <b>'+WtfGlobal.getLocaleText("acc.product.gridProduct")+" :"+'</b> {0}<br> <b>'+WtfGlobal.getLocaleText("acc.stockadjustment.ForStore")+":"+'</b> {1}<br> <b>'+WtfGlobal.getLocaleText("acc.fixed.asset.quantity")+" :"+'</b> {2} {3} ( {4} {5} )', itemCode, requestedStoreName, quantity, orderingUomName, maxQtyAllowed, stockUOMName);
        var moduleid="";
        if(this.type==2){//Fullfilled Orders
            moduleid =Wtf.Inventory_ModuleId;
        } else{ //Stock Issue-Store Orders,Stock Request -Store Orders
            moduleid =Wtf.Acc_Stock_Request_ModuleId;
        }
        var detailWin = new Wtf.StockTransferDetailWin({
            WinTitle : winTitle,
            WinDetail: winDetail,
            TotalTransferQuantity: maxQtyAllowed,
            ProductId:itemId,
            FromStoreId: itemDefaultWarehouse,
            isBatchForProduct: isBatchEnable,
            isSerialForProduct : isSerialEnable,
            isRowForProduct: isRowEnable,
            isRackForProduct: isRackEnable,
            isBinForProduct: isBinEnable,
            FromStoreCombo : warehouseCombo,
            StockDetailArray:record.get("stockDetailsForIssue"),
            isNegativeAllowed: isNegativeAllowed,
            GridStoreURL:"INVStockLevel/getStoreProductWiseDetailList.do?checkQAReject=true",
            moduleid:moduleid,
            type:this.type,
            DataIndexMapping:{
                fromLocationId:"locationId",
                fromRowId:"rowId",
                fromRackId:"rackId",
                fromBinId:"binId",
                serials:"serialNames"
            },
            buttons:[{
                text:WtfGlobal.getLocaleText("acc.field.Save"),
                handler:function (){
                    if(detailWin.validateSelectedDetails()){
                        var detailArray = detailWin.getSelectedDetails();

                        record.set("stockDetailsForIssue",'');
                        record.set("stockDetailsForIssue",detailArray);
                        record.set("stockDetailsIssueQuantity",quantity);
                        
                        record.set("tostore", warehouseCombo.getValue());
                        var idx = warehouseCombo.store.find("store_id", warehouseCombo.getValue());
                        if(idx > -1){
                            var rec = warehouseCombo.store.getAt(idx);
                            record.set("tostorename", rec.get("description"));
                        }else{
                            record.set("tostorename", warehouseCombo.getRawValue());
                        }
                        detailWin.close();
                    }else{
                        return;
                    }
                },
                scope:this
            },{
                text:WtfGlobal.getLocaleText("acc.field.Cancel"),
                handler:function (){
                    detailWin.close();
                },
                scope:this
            }]
        })
        detailWin.show();
    },
    showStockDetailWindowForCollect : function (record){

        var orderId=record.get("id");
        var itemId=record.get("itemId");
        var itemCode=record.get("itemcode");
        var quantity=record.get("delquantity");
        var isBatchEnable = record.get("isBatchForProduct");
        var isSerialEnable = record.get("isSerialForProduct");
        var isRackEnable = record.get("isRackForProduct");
        var isRowEnable = record.get("isRowForProduct");
        var isBinEnable = record.get("isBinForProduct");
        var issuedStoreId = record.get("tostore");
        var issuedStoreName = record.get("tostorename");
        var collectStoreId = record.get("fromstore");
        var collectStoreName = record.get("fromstorename");
        var orderToStockUOMFactor=record.get("orderToStockUOMFactor")
        var orderingUomName=record.get("orderinguomname");
        var stockUOMName=record.get("stockuomname");
        var maxQtyAllowed=orderToStockUOMFactor * quantity;
        
         
        
        var winTitle = WtfGlobal.getLocaleText("acc.stockrequest.StockDetailforStockTransfer");
        var winDetail = String.format(WtfGlobal.getLocaleText("acc.stockrequest.SelectStockdetailsforstocktransfer")+'<br> <b>'+WtfGlobal.getLocaleText("acc.product.gridProduct")+":"+'</b> {0}<br> <b>'+WtfGlobal.getLocaleText("acc.stockrequest.IssuanceStore")+" :"+'</b> {1}<br><b>'+WtfGlobal.getLocaleText("acc.stockrequest.CollectionStore")+" :"+'</b> {2}<br> <b>'+WtfGlobal.getLocaleText("acc.fixed.asset.quantity")+" :"+'</b> {3} {4} ( {5} {6} )', itemCode, issuedStoreName, collectStoreName, quantity, orderingUomName, maxQtyAllowed, stockUOMName);
        var moduleid="";
        if(this.type==2){//Fullfilled Orders
            moduleid =Wtf.Inventory_ModuleId;
        } else{ //Stock Issue-Store Orders,Stock Request -Store Orders
            moduleid =Wtf.Acc_Stock_Request_ModuleId;
        }
        
        var detailWin = new Wtf.StockTransferDetailWin({
            WinTitle : winTitle,
            WinDetail: winDetail,
            TotalTransferQuantity: maxQtyAllowed,
            ProductId:itemId,
            FromStoreId: issuedStoreId,
            ToStoreId: collectStoreId,
            isBatchForProduct: isBatchEnable,
            isSerialForProduct : isSerialEnable,
            isRowForProduct: isRowEnable,
            isRackForProduct: isRackEnable,
            isBinForProduct: isBinEnable,
            StockDetailArray:record.get("stockDetailsForCollect"),
            GridStoreURL:"INVGoodsTransfer/getSRIssuedDetailList.do",
            moduleid:moduleid,
            type:this.type,
            GridStoreExtraParams: {
                requestId : orderId
            },
            DataIndexMapping:{
                detailId : 'detailId',
                toLocationId:"locationId",
                toRowId:"rowId",
                toRackId:"rackId",
                toBinId:"binId",
                serials:"serialNames"
            },
            buttons:[{
                text:WtfGlobal.getLocaleText("acc.common.saveBtn"),
                handler:function (){
                    if(detailWin.validateSelectedDetails()){
                        var detailArray = detailWin.getSelectedDetails();

                        record.set("stockDetailsForCollect",'');
                        record.set("stockDetailsForCollect",detailArray);
                        record.set("stockDetailsCollectQuantity",quantity);
                        
//                        this.acceptFunction("accept","");
                        detailWin.close();
                    }else{
                        return;
                    }
                },
                scope:this
            },{
                text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                handler:function (){
                    detailWin.close();
                },
                scope:this
            }]
        })
        detailWin.show();
    },
    showLocationSelectWindow : function (grid){
        
        var selected=grid.getSelections();
        
        this.orderId=selected[0].get("id");
        this.transferNoteNo= selected[0].get("transfernoteno");
        this.itemId1= selected[0].get("itemId");
        this.itemcode= selected[0].get("itemcode");
        this.isBatchForProduct=selected[0].get("isBatchForProduct");
        this.isSerialForProduct=selected[0].get("isSerialForProduct");
        this.quantity=selected[0].get("nwquantity");
        this.currentRowNo=grid.store.indexOf(selected[0]);
        this.fromStoreName=selected[0].get("fromstorename");
        this.orderToStockUOMFactor=selected[0].get("orderToStockUOMFactor");
        this.itemDefaultWarehouse=selected[0].get("itemdefaultwarehouse");
        this.stockuomname=selected[0].get("stockuomname");
        this.orderinguomname=selected[0].get("orderinguomname");
        this.qtyToBeFilled=this.quantity*this.orderToStockUOMFactor;
        
        this.storeRec = new Wtf.data.Record.create([
        {
            name: 'store_id'
        },
        {
            name: 'description'
        },
        {
            name: 'fullname'
        }
        ]);

        this.storeReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.storeRec);

        this.Store = new Wtf.data.Store({
            url: 'INVStore/getStoreList.do',
            reader:this.storeReader
        });
        this.Store.load({
            params:{
                isActive : "true",
                storeTypes : "0,2",
                byStoreManager:"true"
            }
        });
        
        this.toStoreCmb = new Wtf.form.ComboBox({
            triggerAction:"all",
            hideLabel:false,
            mode:"local",
            typeAhead:true,
            //            style:"margin-left:25px;",
            store:this.Store,
            forceSelection:true,
            displayField:'fullname',
            valueField:'store_id',
            fieldLabel:"Issuing Store*",
            hiddenName:"tostoreid",
            emptyText:WtfGlobal.getLocaleText("acc.je.SelectToStore"),
            width:200
        });
        
        this.Store.on("load", function(){
            if(this.itemDefaultWarehouse != "" && this.itemDefaultWarehouse != undefined && this.itemDefaultWarehouse != null){
                var idx = this.Store.find("store_id",this.itemDefaultWarehouse);
                if(idx != -1){
                    this.toStoreCmb.setValue(this.itemDefaultWarehouse);
                    this.toStoreCmb.fireEvent("select",this);
                }
            }
        }, this);
       
        this.locCmbRecord = new Wtf.data.Record.create([
        {
            name: 'id'
        },        

        {
            name: 'name'
        }]);

        this.locCmbStore = new Wtf.data.Store({
            url:  'INVStore/getStoreLocations.do',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.locCmbRecord)
        });
        
        this.locCmbStore.on("beforeload", function(){
            this.locCmbStore.removeAll();
        }, this);
            
            
        //        this.toStoreCmb.on("select",function(){
        //            this.locCmbStore.removeAll();
        //            this.locCmbStore.load({
        //                params:{
        //                    storeid:this.toStoreCmb.getValue()
        //                }
        //            });
        //        },this);
            
        this.locCmb = new Wtf.form.ComboBox({
            fieldLabel : 'To Location*',
            hiddenName : 'tolocationid',
            store : this.locCmbStore,
            typeAhead:true,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 200,
            triggerAction: 'all',
            emptyText:'Select location...'
        });
        
        this.batchCmbRecord = new Wtf.data.Record.create([
        {
            name: 'batch'
        },        

        {
            name: 'batchname'
        }]);

        //        this.batchCmbStore = new Wtf.data.Store({
        //            url:  'ACCMaster/getNewBatches.do',
        //            reader: new Wtf.data.KwlJsonReader({
        //                root: 'data'
        //            },this.batchCmbRecord)
        //        });
         
        this.batchCmbStore = new Wtf.data.SimpleStore({
            id:"batchCmbStore"+this.id,
            fields:['batch','batchname']
        });
        
        //        this.batchCmb = new Wtf.form.ComboBox({
        //            fieldLabel : 'Batch',
        //            hiddenName : 'batchid',
        //            store : this.batchCmbStore,
        //            typeAhead:true,
        //            displayField:'batchname',
        //            valueField:'batch',
        //            mode: 'local',
        //            width : 200,
        //            triggerAction: 'all',
        //            emptyText:'Select Batch...'
        //        });
        
        this.batchCmb =new Wtf.common.Select({
            fieldLabel:"<span wtf:qtip='"+"Batch" +"'>"+"Batch"+"</span>",
            hiddenName:'batchid',
            name:'batchid',
            store : this.batchCmbStore,
            xtype:'select',
            valueField:'batch',
            displayField:'batchname',
            selectOnFocus:true,
            forceSelection:true,
            multiSelect:true,
            mode: 'local',
            triggerAction:'all',
            typeAhead: true,
            emptyText:'Select Batch...'
        }); 
        
        this.serialCmbRecord = new Wtf.data.Record.create([
        {
            name: 'serialnoid'
        },        

        {
            name: 'serial'
        }]);

        this.serialCmbStore = new Wtf.data.Store({
            url:  'INVStockLevel/getProductBatchWiseSerialList.do?checkQAReject=true',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.serialCmbRecord)
        });
        
        //        this.serialCmb = new Wtf.form.ComboBox({
        //            fieldLabel : 'Serial',
        //            hiddenName : 'serialid',
        //            store : this.serialCmbStore,
        //            typeAhead:true,
        //            displayField:'serialno',
        //            valueField:'serialnoid',
        //            mode: 'local',
        //            width : 200,
        //            triggerAction: 'all',
        //            emptyText:'Select Serial...'
        //        });
        
        this.serialCmb =new Wtf.common.Select({
            fieldLabel:"<span wtf:qtip='"+"Serial" +"'>"+"Serial"+"</span>",
            hiddenName:'serialid',
            name:'serialid',
            store : this.serialCmbStore,
            xtype:'select',
            valueField:'serial',
            displayField:'serial',
            selectOnFocus:true,
            forceSelection:true,
            multiSelect:true,
            mode: 'local',
            triggerAction:'all',
            typeAhead: true,
            emptyText:'Select Serial...'
        }); 
        
        this.quantityeditor=new Wtf.form.NumberField({
            scope: this,
            allowBlank:false,
            allowDecimals:true,
            decimalPrecision:4,
            allowNegative:false
        })
        
        this.EditorColumn = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header:"Order ID",
                dataIndex:"id",
                hidden :true
            },
            {
                header:"Order Note No.",
                dataIndex:"transfernoteno",
                hidden :true
            },
            {
                header:"Product ID",
                dataIndex:"itemcode",
                hidden :true
            },
            {
                header:"LocationID",
                dataIndex:"tolocationid",
                hidden:true
            },
            {
                header:"Location",
                dataIndex:"tolocationname",
                hidden:false
            },
            {
                header:"Batch",
                dataIndex:"batch",
                hidden : (this.isBatchForProduct==true) ? false :true
            },
            {
                header:"Available Quantity",
                dataIndex:"availableQty"
            },
            {
                header:"Quantity",
                dataIndex:"quantity",
                editor:this.quantityeditor
            },
            {
                header:"isBatchEnable",
                dataIndex:"isBatchForProduct",
                hidden:true
            },
           
            {
                header:"isSerialEnable",
                dataIndex:"isSerialForProduct",
                hidden:true
            },
            {
                header:"Serial",
                dataIndex:"serial",
                editor:this.serialCmb,
                hidden : (this.isSerialForProduct==true) ? false :true,
                renderer:this.getComboRenderer(this.serialCmb)
            }
            
            ]);
        
            
        this.locationGridStore = new Wtf.data.SimpleStore({
            fields:['id','transfernoteno','itemid','itemcode','quantity','tolocationid','tolocationname','isBatchForProduct',
            'isSerialForProduct','batch','serial','availableQty'],
            pruneModifiedRecords:true
        });
          
        this.locationGridStore.on('load', function(){
            this.locationSelectionGrid.getView().refresh();
        },this);
        
        this.toStoreCmb.on("select",function(){

            var callURL = "";
            var caseType="";
        
            if(this.isBatchForProduct == true && this.isSerialForProduct==true){
                callURL="INVStockLevel/getStoreProductWiseLocationBatchList.do?checkQAReject=true";
                caseType=1;
            }else if(this.isBatchForProduct == true && this.isSerialForProduct==false){
                callURL="";
            }else if(this.isBatchForProduct == false && this.isSerialForProduct==true){
                callURL="";
            }else{
                callURL="INVStockLevel/getStockByStoreProduct.do";
                caseType=4;
            }
            this.locationGridStoreArr=[];
            
            Wtf.Ajax.requestEx({
                url:callURL,
                params: {
                    toStoreId: this.toStoreCmb.getValue(),
                    productId: this.itemId1
                }
            },this,
            function(res,action){
                if(res.success==true){
                    var totalRec=res.data.length;
 
                    //     fields:['id','transfernoteno','itemid','itemcode','quantity','tolocationid','tolocationname','isBatchForProduct',
                    // 'isSerialForProduct','batch','serial','availableQty'],
                        
                    //dummy casetype 4 data : 
                    // {"locationId":"ff80808149c1a2660149c2aa85e20006","locationName":"qwe","availableQty":100,"productId":"PR/ID000012"}

                    //dummy casetype 1 data :
                    // {"batchName":"mmob","locationId":"ff8080814a2841a9014a28ba32de000c","batchId":"afee1e00-62f5-44a8-950c-977a469ff77a","locationName":"123","availableQty":2,"productId":"402880094a7066db014a71c4f4cc0005"}                   
               
                    if(caseType==1){
                        this.locationGridStore.removeAll();
                        for(var i=0;i<totalRec;i++){
                            
                            this.locationGridStoreArr.push([this.orderId,this.transferNoteNo,this.itemId1,this.itemcode,
                                0,res.data[i].locationId,res.data[i].locationName,this.isBatchForProduct,this.isSerialForProduct,
                                res.data[i].batchName,"",res.data[i].availableQty]);
                            
                           
                        }
                        this.locationGridStore.loadData(this.locationGridStoreArr);
                    }
                    if(caseType==4){
                        this.locationGridStore.removeAll();
                        for(var i=0;i<totalRec;i++){
                            
                            this.locationGridStoreArr.push([this.orderId,this.transferNoteNo,this.itemId1,this.itemcode,
                                0,res.data[i].locationId,res.data[i].locationName,this.isBatchForProduct,this.isSerialForProduct,
                                "","",res.data[i].availableQty]);
                            
                           
                        }
                        this.locationGridStore.loadData(this.locationGridStoreArr);   
                    }
                  
                }else{
                    WtfComMsgBox(["Error", "Error occurred while fetching data."],0);
                    return;
                }
                
            },
            function() {
                WtfComMsgBox(["Error", "Error occurred while processing"],1);
            }
            );   
            
        },this);
        
        
      
        this.locationSelectionGrid = new Wtf.grid.EditorGridPanel({
            cm:this.EditorColumn,
            region:"center",
            id:"editorgrid2sd",
            autoScroll:true,
            store:this.locationGridStore,
            viewConfig:{
                forceFit:true,
                emptyText:"Stock not available."
            },
            clicksToEdit:1
        });
        
        this.locationSelectionGrid.on("beforeedit",this.beforeEdit,this);
        this.locationSelectionGrid.on("afteredit",this.afterEdit,this);
        //this.locationSelectionGrid.on("afteredit",this.fillGridValue,this);
        
        this.winTitle="Select Quantity,Location"+(this.isBatchForProduct ? ",Batch" : "")+(this.isSerialForProduct ? ",Serial" : "");
        this.winDescriptionTitle= this.winTitle+" for following item<br/>";
        this.winDescription="<b>Order Note No. : </b>"+this.transferNoteNo+"<br/>"
        +"<b>Product Code : </b>"+this.itemcode+"<br/>"
        +"<b>Requesting Store : </b>"+this.fromStoreName+"<br/>"
        +"<b>Quantity : </b>"+ this.quantity +" "+this.orderinguomname+" ( "+ this.quantity*this.orderToStockUOMFactor +" "+ this.stockuomname +" )<br/>" ;
        
        this.locationSelectionWindow = new Wtf.Window({
            title : this.winTitle,
            modal : true,
            iconCls : 'iconwin',
            minWidth:100,
            width : 950,
            height: 500,
            resizable :true,
            scrollable:true,
            scope:this,
            id:"locationwindow"+this.id,
            buttonAlign : 'right',
            layout : 'border',
            items :[{
                region : 'north',
                height : 120,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html : getTopHtml(this.winDescriptionTitle,this.winDescription,'images/accounting_image/add-Product.gif')/*upload52.gif')*/
            },{
                region : 'center',
                border : false,
                bodyStyle : 'background:#f1f1f1;font-size : 10px;padding:20px 0px 0px 0px;',
                layout : 'fit',
                items : [{
                    border : false,
                    bodyStyle : 'background:transparent;',
                    layout : "border",
                    items : [
                    {
                        region : 'north',
                        border : false,
                        height : 30,
                        layout:'form',
                        style : 'margin-left:25px',
                        items  :this.toStoreCmb
                    },
                    {
                        region : 'center',
                        layout : 'fit',
                        border : false,
                        items: this.locationSelectionGrid 
                    }
                       
                    ]
                }]
            }],
            buttons :[{
                text : 'Submit',
                iconCls:'pwnd ReasonSubmiticon caltb',
                scope : this,
                handler: function(){  
                    
                    var isValid=this.validateFilledDataForIssue();
                    
                    if(isValid==true){
                        var jsonData=this.makeJSONDataForIssue();
                        var rec=this.grid.getStore().getAt(this.currentRowNo);
                        rec.set("stockDetailsForIssue",jsonData);
                        this.acceptFunction("issue","");
                    }
                    
                }
            },{
                text : 'Cancel',
                scope : this,
                iconCls:'pwnd rejecticon caltb',
                minWidth:75,
                handler : function() {
                    Wtf.getCmp('locationwindow'+this.id).close();
                }
            }]
        }).show();
        
        Wtf.getCmp("locationwindow"+this.id).doLayout();
            
        Wtf.getCmp("locationwindow"+this.id).on("close",function(){
            // alert("close event on locationwindow");
            },this);
            
    },
    getComboRenderer : function(combo){
        return function(value) {
            var idx = combo.store.find(combo.valueField, value);
            if(idx == -1){
                idx = combo.store.find(combo.displayField, value);
            }
            if(idx == -1)
                return value;
            var rec = combo.store.getAt(idx);
            var valueStr = rec.get(combo.displayField);
            return "<div wtf:qtip=\""+valueStr+"\">"+valueStr+"</div>";
        }
    },
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
            var store=grid.getStore();
            var total=store.getCount();
            if(rowindex==total-1){
                return;
            }
            Wtf.MessageBox.confirm('Warning', 'Are you sure you want to remove this item?', function(btn){
                if(btn!="yes") return;
                store.remove(store.getAt(rowindex));
                this.ArrangeNumberer(rowindex);
            }, this);
        }
    },
    fillGridValue:function (e){
        var productId=this.locationGridStore.getAt(e.row).get("itemid");
        var location=this.locationGridStore.getAt(e.row).get("tolocationid");
        var batch=this.locationGridStore.getAt(e.row).get("batchid");
        var serial=this.locationGridStore.getAt(e.row).get("serialid");
         
        if(location != '' && location != undefined && (batch =='' || batch == undefined)){
            //            this.batchCmbStore.load({
            //                params:{
            //                    warehouse:this.toStoreCmb.getValue(),
            //                    location:this.locCmb.getValue(),
            //                    productid :productId
            //                }
            //            });
            
            var batchId="afee1e00-62f5-44a8-950c-977a469ff77a";
            var batchName="mmob";
            this.batchCmbStoreArr= [];
            this.batchCmbStore.removeAll();
            this.batchCmbStoreArr.push([batchId,batchName]);
            this.batchCmbStore.loadData(this.batchCmbStoreArr);
        }
        
        if(batch !='' && batch != undefined && (serial == '' || serial == undefined)){
            this.serialCmbStore.load({
                params:{
                    batch:this.batchCmb.getValue(),
                    productid :productId
                }
            });
        }
        
    },
    
    storeloaded: function (store) {
        if (this.exportButton) {
            if (store.getCount() == 0) {
                this.exportButton.disable();
            } else {
                this.exportButton.enable();
            }
        }
    },
    beforeEdit :function(e){
        
        var rec=e.record;
        
        if(e.record.data.isBatchForProduct == false && (e.field =='batch')) {
            return false;
        }
        if(e.record.data.isSerialForProduct == false && (e.field =='serial')) {
            return false;
        }
        if(e.field =='serial' &&  e.record.data.quantity==0) {
            WtfComMsgBox(["Warning", "Please Fill quantity first."],0);
            return false;
        }
        if(e.field =='serial' &&  e.record.data.quantity > 0) {
            this.serialCmbStore.load({
                params:{
                    batch:rec.data.batch,
                    productid :rec.data.itemid,
                    locationid:rec.data.tolocationid,
                    storeid:this.toStoreCmb.getValue()
                }
            });
        }
            
    },
    
    afterEdit :function(e){
        if(e.field =='quantity') {
            if(e.record.data.quantity > e.record.data.availableQty){
                var rec=e.record;
                rec.set("quantity",0);
                return false;
            }
            
            if(e.record.data.quantity==0 && e.record.data.isSerialForProduct == true){  //if  edited afterwards case
                var rec=e.record;
                rec.set("serial","");
                return false;
            }
            
            var totalRec=this.locationGridStore.getTotalCount();
            var enteredTotalQty=0;
            
            for(var i=0; i < totalRec;i++){
                var currentRec=this.locationGridStore.getAt(i);
                enteredTotalQty += currentRec.get("quantity");
            }
            if(enteredTotalQty > this.qtyToBeFilled){
                WtfComMsgBox(["Warning", "Total Quantity cannot be greater than Issued Quantity"],0);
                var record=e.record;
                record.set("quantity",0);
                return false;
            }
            
        }
        
        if(e.field =='serial' && (e.record.data.serial !="" && e.record.data.serial !=undefined)) {
            var rowRec=e.record;
            var maxSerialSelectionAllowed=rowRec.data.quantity;
            var selectedSerialList=e.record.data.serial;
            var separatedSerialArr=selectedSerialList.split(",");
            if(separatedSerialArr.length > maxSerialSelectionAllowed){
                rowRec.set("serial","");
                WtfComMsgBox(["Warning", "Quantity and selected serial numbers count must be same"],0);
                return false;
            }
        
        }
    },
    validateFilledDataForIssue : function(){
        var recs=this.locationSelectionGrid.getStore().getModifiedRecords();
        var isSerialForProduct=false;
        var quantity=0;
        var serial="";
        var totalQty=0;
        
        if(recs.length > 0){
            
            for(var k=0;k<recs.length;k++){
            
                isSerialForProduct=this.isSerialForProduct; 
                quantity=recs[k].get("quantity");
                serial=recs[k].get("serial");
                totalQty += quantity;    
                
                if(isSerialForProduct==true && quantity > 0){
                    var serialsArr=[];
                    if(serial !=undefined && serial != ""){
                        serialsArr=serial.split(",");
                    }
                    if(serialsArr.length != quantity && quantity!=0){
                        WtfComMsgBox(["Warning", "Please Select "+quantity+" serials as per quantity."],0);
                        return false;
                    }
                }
            }
            if(totalQty != this.qtyToBeFilled){
                WtfComMsgBox(["Warning", "Please fill <b>"+this.qtyToBeFilled+" </b> Quantity."],0);
                return false;
            }
            this.quantity=totalQty;
            return true;
            
        }else{
            WtfComMsgBox(["Warning", "Please enter quantity."],0);
            return false;
        }
        
    },
    makeJSONDataForIssue : function(){
      
        var recs=this.locationSelectionGrid.getStore().getModifiedRecords();
       
        var jArray=[]; 
        if(recs.length > 0){
            
            for(var k=0;k<recs.length;k++){
                var jsondata = {};
                var batch=recs[k].get("batch");
                var locationid=recs[k].get("tolocationid");  
                var quantity=recs[k].get("quantity");
                var serial=recs[k].get("serial");
                
                jsondata.locationId=locationid;
                jsondata.batchName=batch;
                jsondata.serialNames=serial;
                jsondata.quantity=quantity;
                jArray.push(jsondata);
            }
           
        }
        return jArray;
    },
    stockCollectionWindow : function(grid){
        
        var selected=grid.getSelections();

        this.orderId=selected[0].get("id");
        this.transferNoteNo= selected[0].get("transfernoteno");
        this.itemId1= selected[0].get("itemId");
        this.itemcode= selected[0].get("itemcode");
        this.isBatchForProduct=selected[0].get("isBatchForProduct");
        this.isSerialForProduct=selected[0].get("isSerialForProduct");
        this.issuedQuantity=selected[0].get("nwquantity");
        this.quantity=selected[0].get("delquantity");
        this.fromStoreId=selected[0].get("fromstore");
        this.currentRowNo=grid.store.indexOf(selected[0]);
        this.orderToStockUOMFactor=selected[0].get("orderToStockUOMFactor");
        this.stockuomname=selected[0].get("stockuomname");
        this.orderinguomname=selected[0].get("orderinguomname");
        this.qtyToBeFilled=this.quantity*this.orderToStockUOMFactor;
        this.fromStoreName=selected[0].get("fromstorename");
        this.toStoreName=selected[0].get("tostorename");
        this.toStoreId=selected[0].get("tostore");
        
        this.defaultLocationId=selected[0].get("tostoredefaultlocationid");  // to store default location id
        this.defaultLocationName=selected[0].get("tostoredefaultlocationname");// to store default location name
        
        this.locCmbRecord = new Wtf.data.Record.create([
        {
            name: 'id'
        },        

        {
            name: 'name'
        }]);

        this.locCmbStore = new Wtf.data.Store({
            url:  'INVStore/getStoreLocations.do',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.locCmbRecord)
        });
        
        this.locCmbStore.load({
            params:{
                storeid:this.fromStoreId
            }
        });
            
        this.locCmb = new Wtf.form.ComboBox({
            fieldLabel : 'To Location*',
            hiddenName : 'tolocationid',
            store : this.locCmbStore,
            typeAhead:true,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 200,
            triggerAction: 'all',
            emptyText:'Select location...'
        });
        
        this.quantityeditor=new Wtf.form.NumberField({
            scope: this,
            allowBlank:false,
            allowDecimals:true,
            decimalPrecision:4,
            allowNegative:false
        })
        
        this.serialCmbStore = new Wtf.data.SimpleStore({
            fields:['serialnoid','serial'], 
            pruneModifiedRecords:true
        });
   
        this.serialCmb =new Wtf.common.Select({
            fieldLabel:"<span wtf:qtip='"+"Serial" +"'>"+"Serial"+"</span>",
            hiddenName:'serialid',
            name:'serialid',
            store : this.serialCmbStore,
            xtype:'select',
            valueField:'serial',
            displayField:'serial',
            selectOnFocus:true,
            forceSelection:true,
            multiSelect:true,
            mode: 'local',
            triggerAction:'all',
            typeAhead: true,
            emptyText:'Select Serial...'
        }); 
      
        this.collectionCm = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header:"Order ID",
                dataIndex:"id",
                hidden :true
            },
            {
                header:"Order Note No.",
                dataIndex:"transfernoteno",
                hidden :true
            },
            {
                header:"Product ID",
                dataIndex:"itemcode",
                hidden :true
            },
            {
                header:"Stock Detail ID",
                dataIndex:"stockdetailid",
                hidden:true
            },
            {
                header:"IssuedLocationID",
                dataIndex:"tolocationid",
                hidden:true
            },
            {
                header:"Issued from Location",
                dataIndex:"tolocationname",
                hidden:false
            },
            {
                header:"Batch",
                dataIndex:"batch",
                hidden : (this.isSerialForProduct==true) ? false :true
            },
            {
                header:"Issued Quantity",
                dataIndex:"issuedQty"
            },
            {
                header:"Collect to Location*",
                dataIndex:"fromLocationId",
                editor:this.locCmb,
                renderer:this.getComboRenderer(this.locCmb)
            },
            {
                header:"Collect Quantity(in Primary UOM)*",
                dataIndex:"collectQty",
                editor:this.quantityeditor
            },
            {
                header:"isBatchEnable",
                dataIndex:"isBatchForProduct",
                hidden:true
            },
            
            {
                header:"isSerialEnable",
                dataIndex:"isSerialForProduct",
                hidden:true
            },
            {
                header:"Issued Serials",
                dataIndex:"serial",
                hidden:true
            },
            {
                header:"Serial*",
                dataIndex:"selectedSerials",
                editor:this.serialCmb,
                hidden : (this.isBatchForProduct==true) ? false :true
            } 
            ]);
      
        this.collectionGridStore = new Wtf.data.SimpleStore({
            fields:['id','transfernoteno','itemid','itemcode','stockdetailid','issuedQty','tolocationid','tolocationname','isBatchForProduct',
            'isSerialForProduct','batch','purchasebatchid','serial','selectedSerials','fromLocationId','collectQty'], 
            pruneModifiedRecords:true
        });  //fromLocationId = collect locationid  ,tolocationid = issue locationid
           
    
        //   fields:['id','transfernoteno','itemid','itemcode','stockdetailid''issuedQty','tolocationid','tolocationname','isBatchForProduct',
        //           'isSerialForProduct','batch','serial','selectedSerials','fromLocationId','collectQty'], 
        
    
    
        //now get issued stock detail(its location,issued batch,serials and qty)
           
        Wtf.Ajax.requestEx({
            url:"INVGoodsTransfer/getIssuedStockDetail.do",
            params: {
                orderId : this.orderId
            }
        },this,
        function(res,action){
            if(res.success==true){
                var totalRec=res.data.length;
                
                this.collectionGridStoreArr=[];
                this.collectionGridStore.removeAll(); 
                
                for(var i=0;i<totalRec;i++){
                            
                    this.collectionGridStoreArr.push([this.orderId,this.transferNoteNo,this.itemId1,this.itemcode,res.data[i].id,
                        res.data[i].issuedQuantity,res.data[i].issuedLocationId,res.data[i].issuedLocationName,this.isBatchForProduct,
                        this.isSerialForProduct,res.data[i].issuedBatch,res.data[i].purchasebatchid,res.data[i].issuedSerials,"","",0]);
                            
                }
                this.collectionGridStore.loadData(this.collectionGridStoreArr);
                
                if(this.defaultLocationId != undefined && this.defaultLocationId != ""  && this.defaultLocationName != undefined && this.defaultLocationName != ""){
                    this.setDefaultLocationInGrid();
                }
                 
            }else{
                WtfComMsgBox(["Error", "Error occurred while fetching data."],0);
                return;
            }
                
        },
        function() {
            WtfComMsgBox(["Error", "Error occurred while processing"],1);
        }
        );   
   
        this.stockCollectionGrid = new Wtf.grid.EditorGridPanel({
            cm:this.collectionCm,
            region:"center",
            id:"editorgrid2sd",
            autoScroll:true,
            store:this.collectionGridStore,
            viewConfig:{
                forceFit:true,
                emptyText:"No Data to Show."
            },
            clicksToEdit:1
        });
        
        this.winTitle="Select Quantity,Location"+(this.isBatchForProduct ? ",Batch" : "")+(this.isSerialForProduct ? ",Serial" : "");
        this.winDescriptionTitle= this.winTitle+" for following item<br/>";
        this.winDescription="<b>Order Note No. : </b>"+this.transferNoteNo+"<br/>"
        +"<b>Product Code : </b>"+this.itemcode+"<br/>"
        +"<b>Collecting Store : </b>"+this.fromStoreName+"<br/>"
        +"<b>Issuing Store  : </b>"+this.toStoreName+"<br/>"
        +"<b>Quantity : </b>"+ this.quantity +" "+this.orderinguomname+" ( "+ this.quantity*this.orderToStockUOMFactor +" "+ this.stockuomname +" )<br/>" ;
       
        
      
        this.collectWindow = new Wtf.Window({
            title : "Collect Stock",
            modal : true,
            iconCls : 'iconwin',
            minWidth:75,
            width : 950,
            height: 500,
            resizable :true,
            scrollable:true,
            id:"stockcollectionwindow",
            buttonAlign : 'right',
            layout : 'border',
            items :[{
                region : 'north',
                height : 130,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html : getTopHtml(this.winDescriptionTitle,this.winDescription,'images/accounting_image/add-Product.gif')/*upload52.gif')*/
            },{
                region : 'center',
                border : false,
                bodyStyle : 'background:#f1f1f1;font-size : 10px;padding:0px 0px 0px 0px;',
                layout : 'fit',
                items : [{
                    border : false,
                    bodyStyle : 'background:transparent;',
                    layout : "border",
                    items : [
                    {
                        region : 'center',
                        layout : 'fit',
                        border : false,
                        items: this.stockCollectionGrid 
                    }
        
                    ]
                }]
            }],
            buttons :[{
                text : 'Collect',
                iconCls:'pwnd ReasonSubmiticon caltb',
                scope : this,
                handler: function(){  
                   
                    var isValid=this.validateFilledDataForCollect();
                    
                    if(isValid==true){
                        var jsonData=this.makeJSONDataForCollect();
                        var rec=this.grid.getStore().getAt(this.currentRowNo);
                        rec.set("stockDetailsForCollect",jsonData);
                        
                        this.acceptFunction("accept","");
                    }
                }
            },{
                text : 'Cancel',
                scope : this,
                iconCls:'pwnd rejecticon caltb',
                minWidth:75,
                handler : function() {
                    Wtf.getCmp('stockcollectionwindow').close();
                }
            }]
        }).show();
        
        Wtf.getCmp("stockcollectionwindow").doLayout();
            
        Wtf.getCmp("stockcollectionwindow").on("close",function(){
            //this.showAddToItemMasterWin();
            },this);  
        
        this.stockCollectionGrid.on("beforeedit",this.collectBeforeEdit,this);
        this.stockCollectionGrid.on("afteredit",this.collectAfterEdit,this);
    },
    setDefaultLocationInGrid : function(){
        
        var totalRec=this.collectionGridStore.data.length;
        
        if(totalRec>0){
            for(var i=0; i < totalRec;i++){
                var currentRec=this.collectionGridStore.getAt(i);
                currentRec.set("fromLocationId",this.defaultLocationName);
            }
        }
        this.stockCollectionGrid.fireEvent('afteredit',this);
    },  
    collectBeforeEdit :function(e){
        
        var rec=e.record;
        
        if(e.record.data.isBatchForProduct == false && (e.field =='batch')) {
            return false;
        }
        if(e.record.data.isSerialForProduct == false && (e.field =='selectedSerials')) {
            return false;
        }
        if(e.field =='selectedSerials' &&  e.record.data.collectQty==0) {
            WtfComMsgBox(["Warning", "Please Fill quantity first."],0);
            return false;
        }
        if(e.field =='selectedSerials' &&  e.record.data.collectQty > 0) {
            
            this.serialStoreArr=[];
            
            var issuedSerials=rec.data.serial;
            var SerialArr=[];
            if(issuedSerials != "" && issuedSerials != undefined){
                SerialArr=issuedSerials.split(",");
                SerialArr.sort();
                for(var i=0;i<SerialArr.length;i++){
                    this.serialStoreArr.push([SerialArr[i],SerialArr[i]]);
                }
                this.serialCmbStore.loadData(this.serialStoreArr);
            }
        }
            
    },
    
    collectAfterEdit :function(e){
        if(e.field =='collectQty') {
           
            if(e.record.data.collectQty==0 && e.record.data.isSerialForProduct == true){  //if  edited afterwards case
                var rec=e.record;
                rec.set("selectedSerials","");
                return false;
            }
            
            var totalRec=this.stockCollectionGrid.getStore().getTotalCount();
            var enteredTotalQty=0;
            
            for(var i=0; i < totalRec;i++){
                var currentRec=this.stockCollectionGrid.getStore().getAt(i);
                enteredTotalQty += currentRec.get("collectQty");
            }
            if(enteredTotalQty > this.qtyToBeFilled){
                WtfComMsgBox(["Warning", "Total Quantity cannot be greater than Delivered Quantity."],0);
                var record=e.record;
                record.set("collectQty",0);
                return false;
            }
        
        }
        
        if(e.field =='selectedSerials' && (e.record.data.selectedSerials !="" && e.record.data.selectedSerials !=undefined)) {
            var rowRec=e.record;
            var maxSerialSelectionAllowed=rowRec.data.collectQty;
            var selectedSerialList=e.record.data.selectedSerials;
            var separatedSerialArr=selectedSerialList.split(",");
            if(separatedSerialArr.length > maxSerialSelectionAllowed){
                rowRec.set("selectedSerials","");
                WtfComMsgBox(["Warning", "Quantity and selected serial numbers count must be same"],0);
                return false;
            }
        
        }
    },
    validateFilledDataForCollect : function(){
        var recs=this.stockCollectionGrid.getStore().getModifiedRecords();
        var isSerialForProduct=false;
        var quantity=0;
        var serial="";
        var locationid="";   
        var totalQty=0;
        
        if(recs.length > 0){
            
            for(var k=0;k<recs.length;k++){
            
                isSerialForProduct=this.isSerialForProduct; 
                quantity=recs[k].get("collectQty");
                serial=recs[k].get("selectedSerials");
                locationid=recs[k].get("fromLocationId");
                totalQty += quantity; 
                 
                if(locationid == undefined || locationid == ""){
                    WtfComMsgBox(["Warning", "Please select Location."],0);
                    return false;
                }         
                
                if(locationid != undefined && locationid != "" && quantity==0){
                    WtfComMsgBox(["Warning", "Please enter quantity."],0);
                    return false;
                }
                
                if(quantity>0){ // ie.case for -ve qty .
                    if(isSerialForProduct==true){
                        var serialsArr=[];
                        if(serial !=undefined && serial != ""){
                            serialsArr=serial.split(",");
                        }
                        if(serialsArr.length != quantity && quantity!=0){
                            WtfComMsgBox(["Warning", "Please Select "+quantity+" serials as per quantity."],0);
                            return false;
                        }
                    }
                   
                }
            }
            if(totalQty != this.qtyToBeFilled){
                WtfComMsgBox(["Warning", "Please fill total <b>"+this.qtyToBeFilled+" </b> Quantity."],0);
                return false;
            }
            this.quantity=totalQty;
            return true;
            
        }else{
            WtfComMsgBox(["Warning", "Please Select  Location."],0);
            return false;
        }
        
    },
    makeJSONDataForCollect : function(){
        var recs=this.stockCollectionGrid.getStore().getModifiedRecords();
           
        var jArray=[]; 
        if(recs.length > 0){
            
            for(var k=0;k<recs.length;k++){
                var jsondata = {};
                var detailId=recs[k].get("stockdetailid");
                var batch=recs[k].get("batch");
                var locationid=recs[k].get("fromLocationId");  
                var quantity=recs[k].get("collectQty");
                var serial=recs[k].get("selectedSerials");
                
                //  if id not set (ie. location name is set instead of location id then set its id  case) 
                if(locationid == this.defaultLocationName)  
                {
                    locationid = this.defaultLocationId;
                }  
                
                if(quantity != 0){
                    jsondata.detailId=detailId;
                    jsondata.locationId=locationid;
                    jsondata.batchName=batch;
                    jsondata.serialNames=serial;
                    jsondata.quantity=quantity;
                    jArray.push(jsondata);
                }
            }
           
        }
        return jArray;
       
    },
    showAddToItemMasterWin:  function(){
        var selected = this.sm.getSelections();
        if(selected.length > 0){
            
            this.issueBtn = new Wtf.Button({
                //anchor : '90%',
                text: 'Issue',
                tooltip: {
                    text:"Click to issue Stock"
                },
                scope:this,
                handler:function(){
                    this.acceptFunction("issue","");
                }
            });
        
            this.cancelBtn = new Wtf.Button({
                //anchor : '90%',
                text: 'Cancel',
                tooltip: {
                    text:"Click to cancel"
                },
                scope:this,
                handler:function(){
                    if(this.autoCreateConfigWin != null || this.autoCreateConfigWin.close() != undefined){
                        this.autoCreateConfigWin.close();  
                    }
                    
                }
            });
        
            
            this.autoCreateConfigWin = new Wtf.AutoCreateConfigWin({
                title:"Stock issue",
                layout : 'fit',
                closable : true,
                modal : true,
                width : 400,
                scope:this,
                height: 270,
                autoScroll:true,
                resizable :false,
                border:false,
                tostr:selected[0].get("tostore"),
                buttons:[
                this.issueBtn,this.cancelBtn
                ]
            }); 
            this.autoCreateConfigWin.show();
        }else{
            WtfComMsgBox(["Alert", "Please select a record."],3);
        }
    },

    initloadgridstore:function(frm, to,storeid,status){

        this.ds.baseParams = {
            type:this.type,
            frmDate:frm,
            toDate:to,
            storeid:storeid,
            status:status
        }
        this.ds.load({
            params:{

                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
                ss: Wtf.getCmp("Quick"+this.grid.id).getValue()
                     
            }
        });
    },
    loadgridstore:function(frm, to){

        this.ds.baseParams = {
            type:this.type,
            frmDate:frm,
            toDate:to,
            storeid:this.storeCmbfilter.getValue()
        }
    },
    remarkfunction:function(type,isRejectByQA){
        var selected = this.sm.getSelections();

        if(selected.length>0){
            this.addEditWin = new Wtf.Window({
                title : "Remark",
                modal : true,
                iconCls : 'iconwin',
                minWidth:75,
                width : 400,
                height: 250,
                resizable :false,
                id:"rmrkwindow",
                buttonAlign : 'right',
                layout : 'border',
                items :[{
                    region : 'north',
                    height : 75,
                    border : false,
                    bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                    html : getTopHtml("Please Enter Remark","Fill following information",'images/createuser.png')
                },{
                    region : 'center',
                    border : false,
                    bodyStyle : 'background:#f1f1f1;font-size : 10px;padding:20px 20px 20px 20px;',
                    layout : 'fit',
                    items : [{
                        border : false,
                        bodyStyle : 'background:transparent;',
                        layout : "fit",
                        items : [
                        this.winForm = new Wtf.form.FormPanel({
                            url: "jspfiles/inventory/pettyCash.jsp?flag=2&",
                            waitMsgTarget: true,
                            method : 'POST',
                            border : false,
                            bodyStyle : 'font-size:10px;',
                            labelWidth : 110,
                            items : [
                            this.reqTypeName = new Wtf.form.TextArea({
                                fieldLabel:'Enter Remark',
                                name:'type',
                                width:200,
                                allowBlank:false,
                                regex:Wtf.validateAddress,
                                maxLength:200
                            })
                            ]
                        })
                        ]
                    }]
                }],
                buttons :[{
                    text : 'Submit',
                    iconCls:'pwnd ReasonSubmiticon caltb',
                    scope : this,
                    handler: function(){     
                        var str=this.reqTypeName.getValue();
                        this.acceptFunction(type,this.NewlineRemove(str),isRejectByQA);                                                  
                        Wtf.getCmp('rmrkwindow').close();
                    }
                },{
                    text : 'Cancel',
                    scope : this,
                    iconCls:'pwnd rejecticon caltb',
                    minWidth:75,
                    handler : function() {
                        Wtf.getCmp('rmrkwindow').close();
                    }
                }]
            }).show();
        }else{
        // msgBoxShow(["Alert","Please select a record"], 0);
        }
    },
    NewlineRemove : function(str){
        if (str)
            return str.replace(/\n/g, ' ');
        else
            return str;
    },
    setZeroToBlank : function(field){
        if(field.getValue()==0){
            field.setValue("");
        }
    },
    acceptFunction:function(type,remark,isRejectByQA){
        var finalremark = this.NewlineRemove(remark);
        var selected = this.sm.getSelections();
 
        if(selected.length>0){
            if(this.type == 3 && !(type=== "approve by Supervisor" || type=== "reject by Supervisor")){
                for (var i=0;i<selected.length;i++){
                    if(selected[i].get("status") !== "Issued"){
                        Wtf.Msg.show({
                            title:'Info',
                            msg: 'Selected order is not issued yet.',
                            buttons: Wtf.Msg.OK,
                            animEl: 'elId',
                            icon: Wtf.MessageBox.INFO
                        });
                        return;
                    }  
                    if(selected[i].get("delquantity") === null || selected[i].get("delquantity") === undefined || selected[i].get("delquantity") === ""){
                        Wtf.Msg.show({
                            title:'Info',
                            msg: 'Please enter valid data for Delivered Quantity.',
                            buttons: Wtf.Msg.OK,
                            animEl: 'elId',
                            icon: Wtf.MessageBox.INFO
                        });
                        return;
                    }
                }
            }
            var text = "",msg="";
            if(type === "process"){
                if(isRejectByQA!=undefined && isRejectByQA==true){
                    text = "reject";                    
                }else if(isRejectByQA!=undefined && isRejectByQA==false){
                    text = "approve";
                }                
                msg="Are you sure you want to "+text+" selected items?";
            }else if(type === "approve by Supervisor"){
                text = "approve";
                msg="Are you sure you want to Approve the selected Goods Transfer Request?";
            }else if(type === "reject by Supervisor"){
                text = "reject";
                msg="Are you sure you want to Reject the selected Goods Transfer Request?";
            }else if(type === "issue"){
                text = "issue";
                msg="Are you sure you want to "+text+" selected items?";
            }
            else if(type === "accept"){
                text = WtfGlobal.getLocaleText("acc.stockrequest.collect");
                msg=WtfGlobal.getLocaleText("acc.stockrequest.Areyousureyouwantto")+" "+text+" "+WtfGlobal.getLocaleText("acc.field.selecteditems");
            } 
            else if(type === "reject"){
                text = "reject";
                msg="Are you sure you want to "+text+" selected items?";
            } 
            Wtf.MessageBox.confirm("Confirm",msg, function(btn){
                if(btn == 'yes') {                    
                    if(type === "accept"){
                        var showWinflag=false;
                        // commented below lines because it shows return item window but does not do anything by return window
//                        for (var i=0;i<selected.length;i++){
//                            var returnQty = selected[i].get("nwquantity") - selected[i].get("delquantity");
//                            if(returnQty>0){
//                                showWinflag = true;
//                            }
//                        }
                        if(showWinflag){
                            this.saveReturnReason(selected, type, finalremark, text);
                        }else {
                            this.acceptItems(selected, type, finalremark, text);
                        }
                        
                    } else {
                        this.acceptItems(selected, type, finalremark, text,isRejectByQA);
                    }
                    

                }else if(btn == 'no') {
                    return;
                }
            },this);
        }else{
            WtfComMsgBox(["Alert", "Please select a record."],3);
        }
    },
    acceptItems : function(selected, type, finalremark, text,isRejectByQA){
        var jsondata = "";
        //var sep="";
        var finalStr="";
        var requestIdArray = new Array();
     
        if(text=='approve'|| text=='reject'){
            for (var i=0;i<selected.length;i++){
               
                requestIdArray.push(selected[i].get("id"));
            }
              
            finalStr += requestIdArray.toString();
        }else{
            var jArr = [];
            for (var i=0;i<selected.length;i++){
                var jObj = {}
                jObj.transfernoteno=selected[i].get("transfernoteno");
                jObj.date=selected[i].get("date")//.format("Y-m-d");
                //selected date already in user date format 
                jObj.tostoreid=selected[i].get("tostore"); // ---------------------------->check this
                jObj.costcenter=selected[i].get("costcenterid");
                if(type === "accept"){
                    jObj.delquantity=selected[i].get("delquantity");
                    var returnQty = selected[i].get("nwquantity") - selected[i].get("delquantity");
                    jObj.returnQty=returnQty;
                    jObj.reason=selected[i].get("reason");
                    jObj.stockDetails=selected[i].get("stockDetailsForCollect");
                }
                else if(type === "issue"){
                    jObj.toStore=selected[i].get("toStore");
                    jObj.issueQty=selected[i].get("nwquantity");
                    jObj.stockDetails=selected[i].get("stockDetailsForIssue");
                }
                jObj.id=selected[i].get("id");
                jArr.push(jObj);
            }
            finalStr = JSON.stringify(jArr);
        }
        if(! true&& type === "process" && isRejectByQA === undefined){ 
            this.sendGoodsRequest(type, text, finalStr,isRejectByQA,selected);
        } else{
            this.sendGoodsRequest(type, text, finalStr,isRejectByQA,selected);
        }      
    },
    
    sendGoodsRequest:function(type, text, finalStr,isRejectByQA,selected){
        var selected = this.sm.getSelections();
        this.loadMask = new Wtf.LoadMask(document.body);
        if(type ==="issue"){
        //this.autoCreateConfigWin.close();
        }
        this.loadMask.show();
        var allowNegInv="";
        if(this.allowNegativeInventory != undefined || this.allowNegativeInventory != ""){
            allowNegInv=this.allowNegativeInventory;
        }
        this.allowNegativeInventory="";
        var url="";
        if(type ==="issue"){
            url="INVGoodsTransfer/issueStockOrderRequest.do";
        }else if(type ==="accept"){
            url="INVGoodsTransfer/collectStockOrderRequest.do";
        }else{
            url="INVGoodsTransfer/approveRejectStockOrderRequest.do";
        }
        Wtf.Ajax.requestEx({
            url: url,
            params: {
                type:text,
                isRejectByQA:isRejectByQA,
                warehouse: (type ==="issue") ? this.IssuingStoreId : null,
                jsondata:finalStr,
                allowNegativeInventory: allowNegInv
            }
        },
        this,
        function(result) {
            this.loadMask.hide();
            if(result.success) {
                
                var msg=result.msg;
                if(isRejectByQA!=undefined){
                    if(isRejectByQA){
                        text = "rejected";
                    }else{
                        text = "approved";                
                    }    
                    msg = "Selected items are "+text+" by QA successfully.";
                }else if(type === "approve by Supervisor"){
                    msg = "Goods Order Request approved by Supervisor successfully.";
                }else if(type === "reject by Supervisor"){                    
                    msg = "Goods Order Request rejected by Supervisor successfully.";
                }else{
                    msg =result.msg;
                }         
                
                WtfComMsgBox(["Success",msg],3);
                
                if(Wtf.getCmp('stockcollectionwindow')!=undefined){  // window for collection
                    Wtf.getCmp('stockcollectionwindow').close();
                }
               
                if(Wtf.getCmp('locationwindow'+this.id)!=undefined){  //window for issue
                    Wtf.getCmp('locationwindow'+this.id).close();
                }
                this.ds.reload();
                if(text=="issue"){
                    printout("printissue",selected);
                }
                        if (type === "accept" && (selected[0].get("nwquantity") - selected[0].get("delquantity") > 0)) { //selected[0] because only 1 record will be accepted at a time
                            printout("printreturn", selected);
                        }                
                if(result.printFlag){
                    var flag = 104;
                    var orderArray = eval('('+retstatus.processData+')');
                    for(var count=0 ; count<orderArray.length ; count++){
                        var mapForm = document.createElement("form");
                        mapForm.target = "mywindow";
                        mapForm.method = "POST"; //  used for more data
                        mapForm.action = "jspfiles/inventory/printOut.jsp";

                        var mapInput = document.createElement("input");
                        mapInput.type = "text";
                        mapInput.name = "flag";
                        mapInput.value = flag;
                        mapForm.appendChild(mapInput);

                        var mapInput0 = document.createElement("input");
                        mapInput0.type = "text";
                        mapInput0.name = "type";
                        mapInput0.value = type;
                        mapForm.appendChild(mapInput0);
                    

                        var mapInput1 = document.createElement("input");
                        mapInput1.type = "text";
                        mapInput1.name = "data";
                        mapInput1.value = JSON.stringify(orderArray[count]);
                        mapForm.appendChild(mapInput1);
                    
                        document.body.appendChild(mapForm);

                        var map = window.open("", "mywindow","menubar=1,resizable=1,scrollbars=1");

                        if (map) {
                            mapForm.submit();
                        } else {
                        // alert('You must allow popups for this map to work.');
                        }

                    }                                
                }
            }else if(result.success==false && text=="issue" && (result.currentInventoryLevel != undefined && result.currentInventoryLevel != "")){
                
                if(result.currentInventoryLevel=="warn"){
                        
                    Wtf.MessageBox.confirm("Confirm",result.msg, function(btn){
                        if(btn == 'yes') {        
                            this.allowNegativeInventory=true;
                            this.sendGoodsRequest(type, text, finalStr,isRejectByQA);
                        }
                        else if(btn == 'no') {
                            this.allowNegativeInventory=false;
                            this.ds.reload();
                            return;
                        }
                    },this);
                }
                    
                if(result.currentInventoryLevel=="block"){
                    Wtf.MessageBox.show({
                        msg: result.msg,
                        icon:Wtf.MessageBox.INFO,
                        buttons:Wtf.MessageBox.OK,
                        title:"Warning"
                    });
                }
                 
            }else if(result.success==false && text=="issue"){
                 this.ds.reload();
                 WtfComMsgBox(["Info",result.msg],Wtf.MessageBox.INFO);
            }else{
                this.ds.reload();
                WtfComMsgBox(["Failure",msg],1);
            }

        }, function(){
            this.loadMask.hide();
            WtfComMsgBox(["Error","Error occurred while processing"],1);
        });
    },
    
    acceptReturnedItem:function(){
        
        var selectedRec = this.sm.getSelections()[0];
        var returnStockRequestId = selectedRec.get("id");
            if (Wtf.account.companyAccountPref.activateQAApprovalFlow && Wtf.account.inventoryPref.stockRequestQA && (selectedRec.data.isQAEnable)) {
            Wtf.MessageBox.show({
                title: 'Confirm', //'Master Configuration',
                msg: 'Do you want to accept returned items through QA Inspection Process?',
                buttons: Wtf.MessageBox.YESNOCANCEL,  // used YESNOCANCEL as on closing window it set default btn value to no 
                icon: Wtf.MessageBox.QUESTION,
                width: 400,
                scope: this,
                fn: function (btn) {
                    if (btn == "yes") {
                        Wtf.Ajax.requestEx({
                            url: "INVGoodsTransfer/getIssuedStockDetail.do",
                            params: {
                            orderId:returnStockRequestId
                            }
                        },
                        this,
                            function(result) {
                   
                                    if(result.success) {
                                        var serialArr = [];
                                        var dataArr=result.data;
                                        for(var i=0 ; i< dataArr.length; i++){
                                            var data = dataArr[i];
                                            var serials = data.issuedSerials.split(",");
                                            for(var j=0 ; j< serials.length ; j++){
                                                serialArr.push(serials[j]);
                                            }
                                
                                        }
                                        if(selectedRec.get('isSerialForProduct') && serialArr.length > 0 ){
                                            this.openSerialSelectionWindow(returnStockRequestId, serialArr);
                                        }else{
                                            this.acceptReturnedItemSave(returnStockRequestId,true,[]);
                                        }
                            
                                    }else{
                                        WtfComMsgBox(["Error","Error occurred while processing"],1);
                                    }
                                }, function(){
                                        WtfComMsgBox(["Error","Error occurred while processing"],1);
                                });
                            }else if(btn == "no"){
                                    this.acceptReturnedItemSave(returnStockRequestId,false,[]);
                                }else{
                                    return false;
                                }
                            }
                    });
        }else{
                    Wtf.MessageBox.show({
                        title: 'Confirm',
                        msg: WtfGlobal.getLocaleText("acc.goodspendingorders.acceptreturnquntity"),
                        buttons: Wtf.MessageBox.YESNO,
                        icon: Wtf.MessageBox.QUESTION,
                        width: 400,
                        scope: this,
                        fn: function (btn) {
                            if (btn == "yes") {
                        this.acceptReturnedItemSave(returnStockRequestId,false,[]);
                    }else{
                                return false;
                            }
                        }
                    });
                }
                
    },
    
    deleteRecord:function(){
        var selected =  this.grid.getSelections();
        var arr=[];
        var isdeleteValid=false;
        if(selected.length>0){
            for(var j=0;j<selected.length;j++){
                if(selected[j].get("id") != "" &&selected[j].get("id") != undefined){
                    if(selected[j].get("transactiotype")== "1") //if transaction is from cycle count do not allow the user to delete it
                    {
                        var jObj = {}
                        jObj.requestid= selected[j].get("id");
                        arr.push(jObj);
                        isdeleteValid=true;
                    }
                    else //if user tries to delete cycle count data then display this message 
                    {
                        isdeleteValid=false;
                        WtfComMsgBox(["Info","You cannot delete the data for Stock Request,please select only Stock Issue data for deletion."],2);
                        return;
                    }
                }
                
            }
            if(isdeleteValid==true){
                Wtf.MessageBox.show({
                    title: 'Confirm', //'Master Configuration',
                    msg: 'Do you want to delete the selected records ?',
                    buttons: Wtf.MessageBox.YESNO,
                    icon: Wtf.MessageBox.QUESTION,
                    width: 400,
                    scope: this,
                    fn: function (btn) {
                        if (btn == "yes") {
                            Wtf.Ajax.requestEx({
                                url: "INVGoodsTransfer/deleteStockIssueDetail.do",
                                params: {
                                    orderId:JSON.stringify(arr)
                                }
                            },
                            this,
                            function(result) {
                   
                                if(result.success) {
                                     WtfComMsgBox(["Success", result.msg],3);
                                     this.ds.reload();
                                    
                                }else{
                                    WtfComMsgBox(["Warning", result.msg],2);
                                }
                            }, function(){
                                WtfComMsgBox(["Error","Error occurred while processing"],1);
                            });
                        } 
                    }
                });
            }
        }
            
    },
    
    openSerialSelectionWindow: function(returnStockRequestId, serialArr){
        this.winTitle="Select serial for approval";
        this.winDescriptionTitle= "Select serial for approval";
        this.winDescription="Select serial to send for apprval" ;
        var gridStore = new Wtf.data.SimpleStore({
            fields:['serial']
        });
        if(serialArr != '' && serialArr != null && serialArr != undefined){
            var serialCmbData = [];
            for(var i=0 ; i<serialArr.length ; i++){
                serialCmbData.push([serialArr[i]])
            }
            gridStore.loadData(serialCmbData)
        }
        var sm = new Wtf.grid.CheckboxSelectionModel({
            width:25
        });
        var cm = new Wtf.grid.ColumnModel([
            sm,
            new Wtf.grid.RowNumberer(),
            {
                header:"Serial",
                dataIndex:"serial"
            }]);
        
        this.serialGrid=new Wtf.grid.GridPanel({
            region: 'center',
            border: false,
            store: gridStore,
            cm: cm,
            sm:sm,
            loadMask : true,
            layout:'fit',
            viewConfig: {
                forceFit: true
            }
        })
        
        
        var serialSelectionWindow = new Wtf.Window({
            id:'approvalserialselectionwindowid',
            title : this.winTitle,
            modal : true,
            scope:this,
            iconCls : 'iconwin',
            minWidth:100,
            width : 300,
            height: 300,
            resizable :true,
            scrollable:true,
            buttonAlign : 'right',
            layout : 'border',
            items :[{
                region : 'center',
                border : false,
                layout : 'fit',
                items : [this.serialGrid]
            }],
            buttons :[{
                text : 'Submit',
                iconCls:'pwnd ReasonSubmiticon caltb',
                scope : this,
                handler: function(){  
                    var recs = this.serialGrid.getSelectionModel().getSelections();
                    var serialArr = []
                    for(var i=0 ; i<recs.length ; i++){
                        serialArr.push(recs[i].get('serial'));
                    }
                    Wtf.getCmp('approvalserialselectionwindowid').close();
                    this.acceptReturnedItemSave(returnStockRequestId,true, serialArr);
                }
            },{
                text : 'Cancel',
                iconCls:getButtonIconCls(Wtf.etype.menudelete),
                minWidth:75,
                scope:this,
                handler : function() {
                    Wtf.getCmp('approvalserialselectionwindowid').close();
                }
            }]
        }).show();
    },
    acceptReturnedItemSave :function(returnStockRequestId,sendForQA, serialArr){
        
        this.loadMask = new Wtf.LoadMask(document.body);
        this.loadMask.show();

        Wtf.Ajax.requestEx({
            url: "INVGoodsTransfer/acceptReturnStockRequest.do",
            params: {
                id:returnStockRequestId,
                sendForQAApproval : sendForQA,
                serialNames: serialArr.toString()
            }
        },
        this,
        function(result) {
            this.loadMask.hide();
                   
            if(result.success) {
                var msg=result.msg;
                WtfComMsgBox(["Success",msg],3);
                this.ds.reload();
            }else{
                this.ds.reload();
                WtfComMsgBox(["Failure",msg],1);
            }
        }, function(){
            this.loadMask.hide();
            WtfComMsgBox(["Error","Error occurred while processing"],1);
        });
    },
    
    printout:function(printflg){
        var flg=105;
        if(printflg=="printissue"|| printflg=="printreturn"){
            flg=104;
        }
        var selected= this.sm.getSelections();
        var cnt = selected.length;
        var arr=[];
        for(var i=0;i<cnt;i++){
            var jObj = {}
            jObj.quantity=selected[i].get("nwquantity");
            if(printflg=="printorder"){
                jObj.quantity=selected[i].get("quantity");
            }
            jObj.fromstoreid=selected[i].get("fromstore");
            jObj.srno=selected[i].get("srno");
            jObj.transfernoteno=selected[i].get("transfernoteno");
            jObj.orderQty=selected[i].get("quantity");
            var retqty=selected[i].get("nwquantity") - selected[i].get("delquantity");
            jObj.returnQty=retqty;
            jObj.tostorename=selected[i].get("tostorename");
            jObj.fromstorename=selected[i].get("fromstorename");
            jObj.fromlocationname=selected[i].get("fromlocationname");
            jObj.tolocationname=selected[i].get("tolocationname");
            jObj.fromlocationid=selected[i].get("fromlocationid");
            jObj.tolocationid=selected[i].get("tolocationid");
            jObj.itemName=selected[i].get("itemname");
            jObj.date=selected[i].get("date").format("Y-m-d");
            jObj.tostoreid=selected[i].get("tostore");
            jObj.itemid=selected[i].get("itemid");
            jObj.itemCode=selected[i].get("itemcode");
            var finalremark = NewlineRemove(selected[i].get("remark"));
            jObj.remark=finalremark;
            jObj.uomDisplayValue=selected[i].get("name");
            jObj.costcenter=selected[i].get("costcenterid");
            jObj.returnReason=selected[i].get("reason");
            arr.push(jObj);
        }
        var type="print";
        var mapForm = document.createElement("form");
        mapForm.target = "mywindow";
        mapForm.method = "POST"; //  used for more data
        mapForm.action = "jspfiles/inventory/printOut.jsp";

        var mapInput = document.createElement("input");
        mapInput.type = "text";
        mapInput.name = "flag";
        mapInput.value = flg;
        mapForm.appendChild(mapInput);

        var mapInput0 = document.createElement("input");
        mapInput0.type = "text";
        mapInput0.name = "type";
        mapInput0.value = type;
        mapForm.appendChild(mapInput0);
                    
        var mapInput2 = document.createElement("input");
        mapInput2.type = "text";
        mapInput2.name = "printflag";
        mapInput2.value = printflg;
        mapForm.appendChild(mapInput2);

        var mapInput1 = document.createElement("input");
        mapInput1.type = "text";
        mapInput1.name = "data";
        mapInput1.value = JSON.stringify(arr);
        mapForm.appendChild(mapInput1);
                    
        document.body.appendChild(mapForm);

        var map = window.open("", "mywindow","menubar=1,resizable=1,scrollbars=1");

        if (map) {
            mapForm.submit();
        } else {
        //alert('You must allow popups for this map to work.');
        }
    },
    
    saveReturnReason:function(selected, type, finalremark, text){
        var userid="";
        var emparr=selected;
        var acceptedArr = [];
        var rejectedArr = [];
        for(var cnt = 0; cnt < emparr.length; cnt++){
            var rec = emparr[cnt];
            if(rec.get('nwquantity') !== rec.get('delquantity')){
                rejectedArr.push(rec);
            }else{
                acceptedArr.push(rec);
            }
        }

        this.empSelected =new Wtf.data.Store(this.grid.getStore().initialConfig);
        this.empSelected.add(rejectedArr);

        this.compWindow=new Wtf.AssignReturnReason({
            iconCls : 'iconwin',
            layout:'fit',
            closable:true,
            width:850,
            title:"Return Reason",
            height:500,
            border:false,
            modal:true,
            empGDS:this.empSelected,
            id:'assign_frequency_window',
            scope:this,
            acceptedArr:acceptedArr,
            grid:this.grid
        });
        this.compWindow.show();
        
        this.compWindow.on("acceptItems", function(){
            this.acceptItems(selected, type, finalremark, text);
        }, this);
    },
    
    statesaveFunction:function(){
    },
    validateeditFunction:function(e){
    },
    exportReport: function(reportid, exportType){
        var recordCnt = this.grid.store.getTotalCount();
        if(recordCnt == 0)
        {
            msgBoxShow(["Error", "No records to export"], 0,1);
            return;
        }
        var url="";
        if(this.type==2 || this.type==3 || this.type==1){////fulfilled - Goods Pending - Store orders
            url =  "ExportDataServlet.jsp?" +"mode=" + reportid +
            "&reportname=" + this.title +
            "&exporttype=" + exportType +
            "&type=" + this.type +
            "&frmDate=" + this.frmDate.getValue().format(Wtf.getDateFormat())+
            "&toDate=" + this.toDate.getValue().format(Wtf.getDateFormat()) +
            "&storeid=" +this.storeCmbfilter.getValue();
        }else{
            url =  "ExportDataServlet.jsp?" +"mode=" + reportid +
            "&reportname=" + this.title +
            "&exporttype=" + exportType +
            "&type=" + this.type +
            "&storeid=" +this.storeCmbfilter.getValue();

        }
        setDldUrl(url);
    },
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
       
    },
    filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        var format = "Y-m-d";
        this.loadgridstoreforadvserach(this.frmDate.getValue().format(format), this.toDate.getValue().format(format), this.storeCmbfilter.getValue(), this.searchJson, Wtf.Inventory_ModuleId, this.filterConjuctionCrit);
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        var format = "Y-m-d";
        this.loadgridstoreforadvserach(this.frmDate.getValue().format(format), this.toDate.getValue().format(format), this.storeCmbfilter.getValue(), this.searchJson, Wtf.Inventory_ModuleId, this.filterConjuctionCrit);
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },
    printRecordTemplate:function(printflg,item){
        var recordbillid="";
        var moduleid="";
        var selected= this.sm.getSelections();
        var cnt = selected.length;
        var transfernoteno="";
        for(var i=0;i<cnt;i++){
            transfernoteno=selected[i].get("transfernoteno");
            recordbillid=selected[i].json.id;
        }
        if(this.type==2){//Fullfilled Orders
            moduleid =Wtf.Inventory_ModuleId;
        } else{ //Stock Issue-Store Orders,Stock Request -Store Orders
            moduleid =Wtf.Acc_Stock_Request_ModuleId;
        }
        var params= "myflag=order&order&transactiono="+transfernoteno+"&moduleid="+moduleid+"&templateid="+item.id+"&recordids="+recordbillid+"&filetype="+printflg;  
        var mapForm = document.createElement("form");
        mapForm.target = "mywindow";
        mapForm.method = "post"; 
        mapForm.action = "ACCExportPrintCMN/exportSingleStockRequestIssue.do";
        var inputs =params.split('&');
        for(var i=0;i<inputs.length;i++){
            var KV_pair = inputs[i].split('=');
            var mapInput = document.createElement("input");
            mapInput.type = "text";
            mapInput.name = KV_pair[0];
            mapInput.value = KV_pair[1];
            mapForm.appendChild(mapInput); 
        }
        document.body.appendChild(mapForm);
        mapForm.submit();
        var myWindow = window.open("", "mywindow","menubar=1,resizable=1,scrollbars=1");
        var div =  myWindow.document.createElement("div");
        div.innerHTML = "Loading, Please Wait...";
        myWindow.document.body.appendChild(div);
        mapForm.remove();
    },
    loadgridstoreforadvserach:function(frm, to, storeid, searchJson, moduleid, filterConjuctionCriteria){
        this.ds.baseParams = {
            type:this.type,
            frmDate:frm,
            toDate:to,
            storeid:storeid,
            searchJson:searchJson,
            moduleid:moduleid,
            filterConjuctionCriteria:filterConjuctionCriteria
        }
        this.ds.load({
            params:{
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
                ss: Wtf.getCmp("Quick"+this.grid.id).getValue()
                     
            }
        });
    }
});


Wtf.AssignReturnReason = function (config){
    Wtf.apply(this,config);
    this.save = true;
    Wtf.AssignReturnReason.superclass.constructor.call(this,{
        buttons:[
        {
            text:"Save",
            id:'btnsave',
            handler:function (){
                this.saveReturnReason();
            },
            scope:this
        },
        {
            text:"Cancel",
            handler:function (){
                this.close();
                this.fireEvent("acceptItems", this)
            },
            scope:this
        }
        ]
    });
}

Wtf.extend(Wtf.AssignReturnReason,Wtf.Window,{
    initComponent:function (){
        
        this.addEvents('acceptItems');
        Wtf.AssignReturnReason.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetCenterPanel();
      

        this.mainPanel = new Wtf.Panel({
            layout:"border",
            items:[
            this.northPanel,
            this.allempGrid
              
            ]
        });

        this.add(this.mainPanel);
    },
    GetNorthPanel:function (){

        this.northPanel = new Wtf.Panel({
            region:"north",
            height:75,
            border:false,
            bodyStyle:"background-color:white;padding:8px;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml("Return Reason","Click on Reason field and provide return reason", "../../images/link2.jpg")
        });
    },
    GetCenterPanel:function (){

        this.itemIssueTextField = new Wtf.form.TextField({
            allowBlank : false,
            width:200
        });
        
        this.cm = new Wtf.grid.ColumnModel(
            [
            new Wtf.grid.RowNumberer(),
            {
                header: "Order Note No.",
                dataIndex: 'transfernoteno'
            },{
                header: "Product ID",
                dataIndex: 'itemcode'
            },{
                header: "Product Name",
                dataIndex: 'itemname'
            },{
                header: "From Store",
                dataIndex: 'fromstorename'

            },{
                header: "To Store",
                dataIndex: 'tostorename'

            },{
                header: "Ordered Quantity",
                dataIndex: 'quantity',
                renderer: function(val){
                    return val;
                }
            },{
                header: "Issued Quantity",
                dataIndex: 'nwquantity',
                renderer: function(val,meta,rec){
                    if(rec.get('statusId') === 0 || rec.get('statusId') === 1){
                        return '';
                    }else{
                        return val
                    }
                }
            },{
                header: "Returned Quantity",
                dataIndex: 'delquantity',                
                renderer:function(val,meta,rec){
                    val=rec.get('nwquantity') - val;
                    return val;
                }

            },{
                header: "Reason",
                dataIndex: 'reason',
                editor:this.itemIssueTextField
            }
            ]);
        this.allempGrid = new Wtf.grid.EditorGridPanel({
            region:'center',
            id:this.id+'qualifiedgr',
            store: this.empGDS,
            cm: this.cm,
            loadMask:false,
            displayInfo:true,
            enableColumnHide: false,
            trackMouseOver: true,
            clicksToEdit:1,
            autoScroll:true,
            stripeRows: true,
            noSearch:true,
            searchLabel:" ",
            searchLabelSeparator:" ",
            nopaging:true,
            viewConfig: {
                forceFit: true
            }

        });

    },
    
    saveReturnReason:function (){
        this.fireEvent("acceptItems", this)            
        this.close();
        this.grid.getStore().reload();
    }
});


Wtf.AutoCreateConfigWin = function (config){
    Wtf.apply(this,config);
    Wtf.AutoCreateConfigWin.superclass.constructor.call(this,{
        
        });
}

Wtf.extend(Wtf.AutoCreateConfigWin,Wtf.Window,{
    initComponent:function (){
        
        Wtf.AutoCreateConfigWin.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();
        
        this.mainPanel = new Wtf.Panel({
            layout:"border",
            items:[
            this.northPanel,
            this.cForm
            ] 
        });
        this.add(this.mainPanel);
    },
    GetNorthPanel:function (){
        var wintitle='Issue Stock';
        var windetail='';
        var image='';
        windetail='Select the store from which you want to issue stock.';
        image='images/project.gif';
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:85,
            border:false,
            bodyStyle:"background-color:white;padding:8px;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(wintitle,windetail,image)
        });
    
    },
    GetAddEditForm:function (){
        
        this.storeRec = new Wtf.data.Record.create([
        {
            name:"store_id"
        },
        
        {
            name:"fullname"
        }
        ]);
        
        this.storeReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.storeRec);
        
        this.fromStore = new Wtf.data.Store({
            url :"INVStore/getStoreList.do",
            reader:this.storeReader
        });
        this.fromStore.load({
            params:{
                isActive:true,
                storeTypes:'0,2'
            }
        });
        this.fromStore.on("load", function(ds, rec, o){
            if(rec.length > 0){
                this.wareHousetoreCombo.setValue(this.tostr);
            }
        }, this);
        this.wareHousetoreCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            layout: 'fit',
            typeAhead:true,
            scope:this,
            align:'center',
            forceSelection:true,
            store:this.fromStore,
            displayField:"fullname",
            valueField:"store_id",
            fieldLabel:"Select Store *",
            hiddenName:"fromstore",
            allowBlank:false,
            readOnly:true,
            editable:true,
            width:140
        });
        
        this.cForm = new Wtf.form.FormPanel({
            region:"center",
            height:122,
            width:300,
            scope:this,
            bodyStyle:"background-color:#f1f1f1;padding:8px",
            items:[{
                border:false,
                columnWidth:1,
                items:[{
                    columnWidth:0.25,
                    border:false,
                    layout:'form',
                    bodyStyle:'padding:13px 13px 0px 13px',
                    labelWidth:90,
                    items:[
                    this.wareHousetoreCombo
                    ]
                }]
            }]
        });
       
    }
});
// default location window
Wtf.DefaultLoationCollectWin = function (config){
    Wtf.apply(this,config);
    Wtf.DefaultLoationCollectWin.superclass.constructor.call(this,{
        buttons:[
        {
            text:"Save",
            handler:function (){
                if(this.AddLocationFormatForm.getForm().isValid()){
                    var locationId=this.locationCombo.getValue();
                    this.fireEvent('locationSelected', locationId, this.locationCombo.getRawValue())
                    this.close();
                } else{
                    this.locationCombo.markInvalid(WtfGlobal.getLocaleText("acc.store.locationemtymsg"));
                    return false; 
                }
            },
            scope:this
        },
        {
            text:"Cancel",
            handler:function (){
                this.close();
            },
            scope:this
        }
        ]
    });
}

Wtf.extend(Wtf.DefaultLoationCollectWin,Wtf.Window,{
    initComponent:function (){
        Wtf.DefaultLoationCollectWin.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();
        
        this.mainPanel = new Wtf.Panel({
            layout:"border",
            items:[
            this.northPanel,
            this.AddLocationFormatForm
            ]
        });

        this.add(this.mainPanel);
        this.addEvents({
            locationSelected: true
        })
    },
    GetNorthPanel:function (){
        var wintitle = 'Set Default Location';
        var windetail='';
        var image='';
        windetail='Select Location';
        image='images/createuser.png';
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:85,
            border:false,
            bodyStyle:"background-color:white;padding:8px;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(wintitle,windetail,image)
        });
    },
    GetAddEditForm:function (){
        this.locationRecord = new Wtf.data.Record.create([{
            name: 'id'
        },{
            name: 'name'
        }]);
   
        this.locationReader = new Wtf.data.KwlJsonReader({
            root: 'data'
        }, this.locationRecord);

        this.locationStore = new Wtf.data.Store({
            sortInfo: {
                field: 'name',
                direction: "ASC"
            },
            url:'INVStore/getStoreLocations.do',
            
            baseParams:{
                storeid:this.storeId
            },
            reader: this.locationReader
        });
        
        this.locationStore.on('load',function(){
            if(this.defaultLocationId){
                this.locationCombo.setValue(this.defaultLocationId);
            }
        },this)
        
        this.locationCombo = new Wtf.form.ComboBox({
            mode: 'local',
            triggerAction: 'all',
            fieldLabel : 'Collect Location *',
            typeAhead: true,
            width:200,
            allowBlank:false,
            store: this.locationStore,
            displayField: 'name',
            valueField:'id',
            msgTarget: 'side',
            emptyText:"Select Location"
        });
        
        this.locationStore.load();
        
        this.AddLocationFormatForm = new Wtf.form.FormPanel({
            region:"center",
            border:false,
            iconCls:'win',
            bodyStyle:"background-color:#f1f1f1;padding:35px",
            labelWidth:110,
            items:[
            this.locationCombo
            ]
        });
        
    }
});

function printout(printflg, selected) {


    var title = "";
    var pcase = "";
    var moduleId = Wtf.Acc_Stock_Request_ModuleId;
    
    if (printflg == "printreturn") {
        title = "Goods Return Note";
        pcase = "Return"
    } else if (printflg == "printorder") {
        title = "Goods Order Note";
        pcase = "Order";
    } else if (printflg == "printissue") {
        title = "Goods Delivery Note";
        pcase = "Delivery";
    } else if (printflg == "printissueNote") {
        moduleId = Wtf.Inventory_ModuleId;
        title = "Goods Issue Note";
        pcase = "Issue";
    } else if (printflg == "interstore") {
        moduleId = Wtf.Acc_InterStore_ModuleId;
        title = "Store Transfer Note";
        pcase = "Transfer";
    }
    else {
        title = "Sample";
        pcase = "sample";
    }

    var cnt = selected.length;

    var isBatchDataPresent = false;
    var isSerialDataPresent = false;
    var isLocationDataPresent = false;
    var isMultipleToStore = false;
    var toStoreArr = [];
    toStoreArr.push(selected.items[0].get("tostorename"));
    // Create the hashmap
    var storeWiseRowIndexGrouping = {};


    for (var i = 0; i < cnt; i++) {
        if (selected.items[i].data.isBatchForProduct == true && isBatchDataPresent == false) {
            isBatchDataPresent = true;
        }
        if (selected.items[i].data.isSerialForProduct == true && isSerialDataPresent == false) {
            isSerialDataPresent = true;
        }

        // Add key and value to the hashmap
        if (selected.items[i].get("tostorename") in storeWiseRowIndexGrouping) {
            var arr = storeWiseRowIndexGrouping[selected.items[i].get("tostorename")];
            arr.push(i);
        } else {
            storeWiseRowIndexGrouping[selected.items[i].get("tostorename")] = [];
            storeWiseRowIndexGrouping[selected.items[i].get("tostorename")].push(i);
        }
        if (selected.items[i].get("tostorename") != selected.items[0].get("tostorename")) {
            isMultipleToStore = true;
            if (toStoreArr.indexOf(selected.items[i].get("tostorename")) == -1) {
                toStoreArr.push(selected.items[i].get("tostorename"));

            }
        }

    }

    for (var storeIndex = 0; storeIndex < toStoreArr.length; storeIndex++) {
        var storeName = toStoreArr[storeIndex];
        var rowIndexArr = storeWiseRowIndexGrouping[storeName];

        var htmlString = "<html>"
                + "<title>" + title + "</title>"
                + "<head>"
                + "<STYLE TYPE='text/css'>"
                + "<!--"
                + "TD{font-family: Arial; font-size: 10pt;}"
                + "--->"
                + "</STYLE>"
                + "</head>"
                + "<body>"
                + "<h2 align = 'center' style='font-family:arial; padding: 2%;'> " + title + " </h2>";

        htmlString += "<table border='0' width='95%'>"
        htmlString += "<tr><td style='width:53%'><b>&nbsp;</b>" + "</td><td>" + (printflg === "printorder" || printflg === "printissueNote" || printflg === "interstore" || printflg === "printreturn" ? "" : "<b>Order ID : </b>" + selected.items[rowIndexArr[0]].get("transfernoteno")) + "</td></tr>"
        htmlString += "<tr><td style='width:53%'><b>&nbsp;</b>" + "</td><td>" + "<b>" + pcase + " Note No : </b>" + (printflg === "printreturn" ? "R" + selected.items[rowIndexArr[0]].get("transfernoteno") : (printflg === "printissue" ? "D" + selected.items[rowIndexArr[0]].get("transfernoteno") : selected.items[rowIndexArr[0]].get("transfernoteno"))) + "</td></tr>"
        htmlString += (printflg == "interstore") ? "<tr><td style='width:53%'><b>&nbsp;</b></td><td><b>Created By :</b> " + selected.items[rowIndexArr[0]].get("createdby") + "</td></tr>" : ""
        htmlString += "<tr><td style='width:53%'><b>&nbsp;</b>" + "</td><td>" + (printflg === "printorder" || printflg === "printissueNote" || printflg === "interstore" || printflg === "printreturn" ? "<b>Date : </b>" : "<b>Shipping Date : </b>") + (printflg === "interstore" ? selected.items[rowIndexArr[0]].get("date") : (printflg === "printorder" || printflg === "printissueNote" || printflg === "interstore" ? selected.items[rowIndexArr[0]].get("date") : selected.items[rowIndexArr[0]].get("collectedOn"))) + "</td></tr>"
        htmlString += "</table>"

        htmlString += "<div style='margin-top:85px;width: 95%;'>";
        if (printflg === "accept" || printflg == "printissue" || (printflg == "printorder" && selected.items[rowIndexArr[0]].get("transfernoteno").startsWith("R"))) {
            htmlString += "<span style='margin-left:1%; border-left:solid 1px black;border-bottom:solid 1px black;border-top:solid 1px black; border-right:solid 1px black; width:45%; padding: 1%; float: left;text-align: left;'><b>To : </b></br>&nbsp;&nbsp;&nbsp;&nbsp;" + selected.items[rowIndexArr[0]].get("tostorename") + "</br>&nbsp;&nbsp;&nbsp;&nbsp;" + selected.items[rowIndexArr[0]].get("tostoreadd") + "</br>"
                    + "&nbsp;&nbsp;&nbsp;&nbsp;<b>Tel : </b>" + selected.items[rowIndexArr[0]].get("tostorephno") + "</br>&nbsp;&nbsp;&nbsp;&nbsp;<b>Fax : </b>" + selected.items[rowIndexArr[0]].get("tostorefax") + "</br></span>"
                    + "<span style='border-bottom:solid 1px black;border-top:solid 1px black;border-left:solid 1px black; border-right:solid 1px black; width:45%; padding: 1%; float: left;text-align: left;'><b>From : </b></br>&nbsp;&nbsp;&nbsp;&nbsp;" + selected.items[rowIndexArr[0]].get("fromstorename") + "</br>&nbsp;&nbsp;&nbsp;&nbsp;" + selected.items[rowIndexArr[0]].get("fromstoreadd") + "</br>"
                    + "&nbsp;&nbsp;&nbsp;&nbsp;<b>Tel : </b>" + selected.items[rowIndexArr[0]].get("fromstorephno") + "</br>&nbsp;&nbsp;&nbsp;&nbsp;<b>Fax : </b>" + selected.items[rowIndexArr[0]].get("fromstorefax") + "</br></span>";
        } else {
            htmlString += "<span style='margin-left:1%; border-left:solid 1px black;border-bottom:solid 1px black;border-top:solid 1px black; border-right:solid 1px black; width:45%; padding: 1%; float: right;text-align: left;'><b>To : </b></br>&nbsp;&nbsp;&nbsp;&nbsp;" + selected.items[rowIndexArr[0]].get("tostorename") + "</br>&nbsp;&nbsp;&nbsp;&nbsp;" + selected.items[rowIndexArr[0]].get("tostoreadd")
                    + "</br></span>"
                    + "<span style='border-bottom:solid 1px black;border-top:solid 1px black;border-left:solid 1px black; border-right:solid 1px black; width:45%; padding: 1%; float: left;text-align: left;'><b>From : </b></br>&nbsp;&nbsp;&nbsp;&nbsp;" + selected.items[rowIndexArr[0]].get("fromstorename") + "</br>&nbsp;&nbsp;&nbsp;&nbsp;" + selected.items[rowIndexArr[0]].get("fromstoreadd")
                    + "</br></span>";
        }
        htmlString += "</div><br/><br style='clear:both'/><br/>";
        var pgbrkstr1 = "<DIV style='page-break-after:always'></DIV>";

        if (i != 0) {
            htmlString += "<br/><br/></br>";
        }
        htmlString += "<center>";
        htmlString += "<table cellspacing=0 border=1 cellpadding=2 width='95%'>";
        if (printflg === "accept" || (printflg != null && printflg === "printreturn")) {
            htmlString += "<tr><th>S/N</th><th>Product ID</th><th>Product Name</th><th>Return Qty</th>" + (isBatchDataPresent == true ? "<th>Return Batch</th>" : "") + (isSerialDataPresent == true ? "<th>Return Serial</th>" : "");
        } else if (printflg === "printorder" || printflg === "printissueNote" || printflg === "interstore") {
            htmlString += "<tr><th>S/N</th>"
                    + "<th>Product ID</th>"
                    + "<th>Product Name</th>"
                    //            +(printflg === "printissueNote" || printflg === "interstore"?"<th> HS Code</th>":"")
                    + "<th>Uom</th>"
                    + "<th>Quantity</th>";
            // htmlString += (isLocationDataPresent == true ? "<th>Location</th>" : "" );
            if (printflg === "printissueNote" || printflg === "interstore") {
                htmlString += "<th>From Location</th>";
                //htmlString += (printflg === "interstore" ? "<th>To Location</th>" : "");
                htmlString += (isBatchDataPresent == true ? "<th>Batch No.</th>" : "");
                htmlString += (isSerialDataPresent == true ? "<th>Issued Serial No.</th>" : "");
                //htmlString += (isSerialDataPresent == true &&  printflg === "interstore" ? "<th>Collected Serial No.</th>" : "" );
            }
            htmlString += "<th>Remark</th>";
        } else if (printflg === "printissue") {
            htmlString += "<tr><th>S/N</th><th>Product ID</th><th>Product Name</th><th>Uom</th><th>Order Qty</th><th>Ship Qty</th><th> Remark</th>";
        } else {
            htmlString += "<tr><th>S/N</th><th>Product ID</th><th>Product Name</th><th>Cost Center</th><th>Uom</th><th>Order Qty</th><th>Ship Qty</th><th>Remarks</th>";
        }

        /*
         * line level custom column
         */
        var linedata = [];
        linedata = WtfGlobal.appendCustomColumn(linedata, GlobalColumnModel[moduleId]);
        for (var lineFieldCount = 0; lineFieldCount < linedata.length; lineFieldCount++) {
            if (linedata[lineFieldCount].header !== undefined) {
                htmlString += "<th>" + linedata[lineFieldCount].header + "</th>";
            }
        }
        htmlString += "</tr>";
        
        var count = 1;
        for (var i = 0; i < rowIndexArr.length; i++) {
                /**
                 * while print from Stock Request and Stock Issue.
                 * Deleted and Rejected recores not shown. 
                 */
                if(selected.items[rowIndexArr[i]].data.status === "Deleted" || selected.items[rowIndexArr[i]].data.status === "Rejected"){
                        continue;
                }
                var saDetail = selected.items[rowIndexArr[i]].data.stockDetails;
                var returnDetail = selected.items[rowIndexArr[i]].data.stockDetailsForCollect;
                var batchDtl = "";
                var issuedSerialDtl = "";
                var collectedSerialDtl = "";
                var returnBatchDtl = "";
                var returnSerialDtl = "";
                var fromLocationDtl = "";
                var toLocationDtl = "";
                for (var sad = 0; sad < saDetail.length; sad++) {

                    batchDtl += saDetail[sad].batchName;
                    batchDtl += (((sad != saDetail.length - 1 && saDetail[sad].batchName == "") || sad == saDetail.length - 1) ? "" : "<hr/>");

                    issuedSerialDtl += saDetail[sad].issuedSerials;
                    issuedSerialDtl += (((sad != saDetail.length - 1 && saDetail[sad].issuedSerials == "") || sad == saDetail.length - 1) ? "" : "<hr/>");

                    collectedSerialDtl += saDetail[sad].collectedSerials;
                    collectedSerialDtl += (((sad != saDetail.length - 1 && saDetail[sad].collectedSerials == "") || sad == saDetail.length - 1) ? "" : "<hr/>");

                    fromLocationDtl += saDetail[sad].issuedLocationName;
                    fromLocationDtl += (((sad != saDetail.length - 1 && saDetail[sad].issuedLocationName == "") || sad == saDetail.length - 1) ? "" : "<hr/>");

                    toLocationDtl += saDetail[sad].collectedLocationName;
                    toLocationDtl += (((sad != saDetail.length - 1 && saDetail[sad].collectedLocationName == "") || sad == saDetail.length - 1) ? "" : "<hr/>");

                    returnBatchDtl += saDetail[sad].batchName;
                    if (returnDetail != "" && returnDetail != undefined) {
                        returnSerialDtl += getReturnSerialsName(saDetail[sad].issuedSerials, returnDetail[sad].serialNames);
                    } else if (returnDetail == "" && isSerialDataPresent && saDetail[sad].collectedQuantity == 0) {
                        returnSerialDtl += getReturnSerialsName(saDetail[sad].issuedSerials, "");
                    }

                }


                if (printflg === "printreturn") {
                    htmlString += "<tr><td>" + count + "&nbsp;</td><td>" + selected.items[rowIndexArr[i]].get("itemcode") + "&nbsp;</td><td>" + selected.items[rowIndexArr[i]].get("itemname") + "&nbsp;</td><td align=right>" + (Number(selected.items[rowIndexArr[i]].get("nwquantity")) - Number(selected.items[rowIndexArr[i]].get("delquantity"))) + "&nbsp;</td>" + (isBatchDataPresent == true ? "<td>" + returnBatchDtl + "&nbsp;</td>" : "") + (isSerialDataPresent == true ? "<td>" + returnSerialDtl + "&nbsp;</td>" : "") + "&nbsp;";

                } else if (printflg === "printorder" || printflg === "printissueNote" || printflg === "interstore") {
                    htmlString += "<tr>"
                            + "<td>" + count + "&nbsp;</td>"
                            + "<td>" + selected.items[rowIndexArr[i]].get("itemcode") + "&nbsp;</td>"
                            + "<td>" + selected.items[rowIndexArr[i]].get("itemname") + "&nbsp;</td>"
                            //                +(printflg === "printissueNote" || printflg === "interstore"?"<td>" + (selected.items[rowIndexArr[i]].get("hscode") == undefined ? "": selected.items[0].get("hscode")) + "&nbsp;</td>":"")
                            + "<td>" + selected.items[rowIndexArr[i]].get("name") + "&nbsp;</td>"
                            + "<td align=right>" + selected.items[rowIndexArr[i]].get((printflg === "printissueNote" ? "nwquantity" : "quantity")) + "&nbsp;</td>";

                    if (printflg === "printissueNote" || printflg === "interstore") {
                        htmlString += "<td align='center'>" + fromLocationDtl + "</td>" +
                                (isBatchDataPresent == true ? "<td align='center'>" + batchDtl + "</td>" : "") +
                                (isSerialDataPresent == true ? "<td align='center'>" + issuedSerialDtl + "</td>" : "");

                    }
                    htmlString += "<td>" + selected.items[rowIndexArr[i]].get("remark") + "&nbsp;</td>";

                } else if (printflg === "printissue") {
                    htmlString += "<tr><td>" + count + "&nbsp;</td><td>" + selected.items[rowIndexArr[i]].get("itemcode") + "&nbsp;</td><td>" + selected.items[rowIndexArr[i]].get("itemname") + "&nbsp;</td><td>" + selected.items[rowIndexArr[i]].get("name") + "&nbsp;</td><td align=right>" + selected.items[rowIndexArr[i]].get("quantity") + "&nbsp;</td><td align=right>" + selected.items[rowIndexArr[i]].get("nwquantity") + "&nbsp;</td><td>" + selected.items[rowIndexArr[i]].get("remark") + "&nbsp;</td>";
                } else {
                    htmlString += "<tr><td align='center'>" + count + "&nbsp;</td><td align='center'>" + selected.items[rowIndexArr[i]].get("itemcode") + "&nbsp;</td><td align='center'>" + selected.items[rowIndexArr[i]].get("itemcode") + "&nbsp;</td><td align='center'>" + selected.items[rowIndexArr[i]].get("itemcode") + "&nbsp;</td><td align='center'>" + selected.items[rowIndexArr[i]].get("costcenterid");
                    +"&nbsp;</td><td align='center'>" + selected.items[rowIndexArr[i]].get("name") + "</td><td align='center'>" + "</td><td align='center'>" + selected.items[rowIndexArr[i]].get("quantity") + "</td><td align='center'>" + selected.items[rowIndexArr[i]].get("remark") + "&nbsp;</td>";
                }
                
                /*
                 * line level custom column data
                 */
                for (var lineFieldCount = 0; lineFieldCount < linedata.length; lineFieldCount++) {
                    if (linedata[lineFieldCount].header !== undefined && selected.items[rowIndexArr[i]].get(linedata[lineFieldCount].dataIndex) !== "") {
                        if (linedata[lineFieldCount].xtype === "datefield") {
                            var lineleveldate = WtfGlobal.onlyDateRendererTZ(new Date(selected.items[rowIndexArr[i]].get(linedata[lineFieldCount].dataIndex)));//Date converted long format to date data type
                            htmlString += "<td>" + lineleveldate + "</td>";
                        } else
                            htmlString += "<td>" + selected.items[rowIndexArr[i]].get(linedata[lineFieldCount].dataIndex) + "</td>";
                    } else {
                        htmlString += "<td>" + "" + "</td>";
                    }
                }
                htmlString += "</tr>";
                count++;
        }

        htmlString += "</table>";
        htmlString += "</center><br><br>";
//        if (i != cnt - 1) { // Removed b/c of unused pagebreak
//            htmlString += pgbrkstr1;
//        }

        htmlString +=
                //        "<div>"
                //    + "<span style='width:270px; padding: 3%; float: left;text-align:left'><b>Prepared By </b><br></br>Sign:</br>Name:&nbsp;&nbsp;&nbsp;&nbsp;" +createdBy + "</br></br></span>"
                //    + "<span style='width:270px; padding: 3%; float: right;text-align:left'><b>Collected By </b></br></br>Sign:</br>Name:&nbsp;&nbsp;&nbsp;&nbsp;" + collectedBy + "</br></br></span>"
                //    + "</div><br style='clear:both'/>"
                "<div style='float: right; padding-top: 3px; padding-right: 5px;'>"
                + "<button id = 'print' title='Print Invoice' onclick='window.print();' style='color: rgb(8, 55, 114);' href='#'>Print</button>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
        htmlString +="<style>@media print {button#print{display:none;}}</style>";
        var disp_setting = "toolbar=yes,location=no,";
        disp_setting += "directories=yes,menubar=yes,";
        disp_setting += "scrollbars=yes,width=650, height=600, left=100, top=25";
        var docprint = window.open("", "", disp_setting);
        docprint.document.open();
        docprint.document.write('<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"');
        docprint.document.write('"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">');
        docprint.document.write('<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">');
        docprint.document.write('<head><title></title>');
        docprint.document.write('<style type="text/css">body{ margin:0px;');
        docprint.document.write('font-family:verdana,Arial;color:#000;');
        docprint.document.write('font-family:Verdana, Geneva, sans-serif; font-size:12px;}');
        docprint.document.write('a{color:#000;text-decoration:none;} </style>');
        docprint.document.write('</head><body onLoad="self.print()"><center>');
        docprint.document.write(htmlString);
        docprint.document.write('</center></body></html>');
        docprint.document.close();
    }

}

function getReturnSerialsName(issuedSerials, collectedSerials) { //commaSeparated
    var issuedArr = issuedSerials.split(",");
    var collectedArr = collectedSerials.split(",");
    var returnedSerials = "";
    for (var x = 0; x < issuedArr.length; x++) {
        if (collectedArr.indexOf(issuedArr[x]) == -1) {
            if (returnedSerials == "") {
                returnedSerials += issuedArr[x];
            } else {
                returnedSerials += "," + issuedArr[x];
            }

        }
    }
    return returnedSerials;
}

function setHiddenDuplicateCustomColumn(columnModel) {

    var columnModelLength = columnModel.config.length;

    for (var i = 30; i < columnModelLength; i++) {
        for (var j = i + 1; j < columnModelLength; j++) {
            if (columnModel.config[i].dataIndex === columnModel.config[j].dataIndex) {
                columnModel.setHidden(i, true);
            }
        }
    }
    return columnModel;
}