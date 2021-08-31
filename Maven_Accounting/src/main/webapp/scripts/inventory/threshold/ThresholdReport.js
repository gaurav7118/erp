function callProductThresholdReport(){
    var productTresholdReportGrid=Wtf.getCmp("ProductTresholdReportGridId");
   if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.viewproductthreshold)) {
    if(productTresholdReportGrid==null){
        productTresholdReportGrid = new Wtf.TresholdReportGrid({
            id: "ProductTresholdReportGridId",
            border : false,
            title : WtfGlobal.getLocaleText("acc.stockavailability.ProductTresholdReportGrid"), //Changed
            layout : 'fit',
            style:'backgroud-color:white',
            closable: true,
            iconCls:getButtonIconCls(Wtf.etype.inventoryptr),
            modal:true
        });
        Wtf.getCmp('as').add(productTresholdReportGrid);
    }
    Wtf.getCmp('as').setActiveTab(productTresholdReportGrid);
    productTresholdReportGrid.doLayout();
   }
   else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.lp.viewproductthreshold"));
   }
}

Wtf.TresholdReportGrid = function(config){
    Wtf.TresholdReportGrid.superclass.constructor.call(this,config);
};
Wtf.extend(Wtf.TresholdReportGrid, Wtf.Panel,{
    onRender : function(config){
        Wtf.TresholdReportGrid.superclass.onRender.call(this,config);

        this.storeCmbRecord = new Wtf.data.Record.create([
        {
            name: 'store_id'
        },
        {
            name: 'abbrev'
        },
        {
            name: 'description'
        },
        {
            name: 'fullname'
        }
        ]);

        this.storeCmbStore = new Wtf.data.Store({
            url:  'INVStore/getStoreList.do',
            baseParams:{
//                isActive:true,    //ERP-40021 :To get all Stores.
                byStoreExecutive:"true",
                byStoreManager:"true",
                includePickandPackStore:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.storeCmbRecord)
        });

        this.storeCmbStore.on('load', function(){
            if(this.storeCmbStore.getCount() > 0){
                var rec= this.storeCmbStore.getAt(0);
                this.storeCmb.setValue(rec.get('store_id'))
                this.storeCmb.fireEvent('select');
            }
        }, this)
        this.storeCmbStore.load();
        
        this.storeCmb = new Wtf.form.ComboBox({
            store : this.storeCmbStore,
            typeAhead:true,
            displayField:'fullname',
            valueField:'store_id',
            mode: 'local',
            width : 150,
            triggerAction: 'all',
            emptyText:'Select Store...',
            listWidth:300,
            tpl: new Wtf.XTemplate(
                '<tpl for=".">',
                '<div wtf:qtip = "{[values.fullname]}" class="x-combo-list-item">',
                '<div>{fullname}</div>',
                '</div>',
                '</tpl>')
        });
        this.storeCmb.on('select', function(){
            this.ds.baseParams.storeId = this.storeCmb.getValue();
            this.ds.load({
                params: {
                    start:0,
                    limit:pag.pageSize
                }
            });
        }, this)
        
        this.sm2 = new Wtf.grid.CheckboxSelectionModel({
            width:25
        });
        this.thresholdRecord = Wtf.data.Record.create([
        {
            name: 'storeId'
        },
        {
            name: 'storeCode'
        },
        {
            name: 'storeName'
        },
        {
            name: 'productId'
        },
        {
            name: 'productCode'
        },
        {
            name: 'productName'
        },
        {
            name: 'thresholdLimit'
        },
        {
            name: 'stockInHand'
        }
        ]);

        this.ds = new Wtf.data.Store({
            url:  'INVThreshold/getThresholdStockReport.do',
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root:'data'
            }, this.thresholdRecord)
        });
        var cmDefaultWidth = 300;
        this.cm = new Wtf.grid.ColumnModel([
            this.sm2,
            {
                header: WtfGlobal.getLocaleText("acc.product.threshold.grid.storename"),
                dataIndex: 'storeName',
                width: cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.product.threshold.grid.productID"),
                dataIndex: 'productCode',
                width: cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.product.threshold.grid.productname"),
                dataIndex: 'productName',
                width: cmDefaultWidth
            },{
                header:WtfGlobal.getLocaleText("acc.product.threshold.grid.thresholdlimit"),
                dataIndex: 'thresholdLimit',
                width: cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.field.StockinHand"),
                dataIndex:"stockInHand",
                width: cmDefaultWidth
            }
            ]);

        this.newBtn = new Wtf.Button({
            anchor : '90%',
            text: WtfGlobal.getLocaleText("acc.stockavailability.SendStockRequest"),
            iconCls :getButtonIconCls(Wtf.etype.add),
            id: 'newlocation',
            tooltip: {
                text: WtfGlobal.getLocaleText("acc.stockavailability.SendStockRequesttooltip")
            },
            handler: this.sendGoodsRequest,
            scope:this
        });
        
        var TbarButtonArray = [];
        var BbarButtonArray = [];
        
        BbarButtonArray.push("-",this.newBtn);
        TbarButtonArray.push("-",WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore")+": ", this.storeCmb);

        this.grid1 = new Wtf.KwlEditorGridPanel({
            store: this.ds,
            cm: this.cm,
            sm: this.sm2,
            loadMask : true,
            layout:'fit',
            viewConfig: {
                forceFit: false
            },
            split: true,
            region: 'center',
            displayInfo:true,
            searchLabel:WtfGlobal.getLocaleText("acc.het.806"),
            searchEmptyText:WtfGlobal.getLocaleText("acc.common.search.byNames"),
            serverSideSearch:true,
            tbar:TbarButtonArray,
            bbar:BbarButtonArray
        });

        this.add(this.grid1);

    },
    
    sendGoodsRequest : function(){
        var recs = this.grid1.selModel.getSelections();
        if(recs.length > 0){
            
            var products = [];
            for(var i=0 ; i< recs.length; i++){
                products.push(recs[i].get('productId'))
            }
            
            var mainTabId = Wtf.getCmp("as");
            var goodsTransferTab = Wtf.getCmp("goodsOrderParentTabb");
            if(goodsTransferTab == null){
                goodsTransferTab = new Wtf.Panel({
                    layout:"fit",
                    title:"Stock Request",
                    closable:true,
                    border:false,
                    id:"goodsOrderParentTabb",
                    type:"order",
                    items:[
                        this.order =new Wtf.order({
                        id:"order111"+this.id,
                        layout:'fit',
                        FromStoreId: this.storeCmb.getValue(),
                        border:false,
                        prodIds:products
                    })]
                });
                
                mainTabId.add(goodsTransferTab);
            }
            mainTabId.setActiveTab(goodsTransferTab);
            mainTabId.doLayout();
            
            this.order.fillRequestedItems(products);
        }else{
            Wtf.Msg.alert('Alert', 'Please select a record', 1)
        }
    }
});






