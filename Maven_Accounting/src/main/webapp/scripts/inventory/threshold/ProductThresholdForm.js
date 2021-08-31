
Wtf.ProductThresholdFormWin = function (config){
    
    Wtf.apply(this,config);
    Wtf.ProductThresholdFormWin.superclass.constructor.call(this,{
        title : WtfGlobal.getLocaleText("acc.product.set.threshold.Limit"),
        modal : true,
        iconCls : 'iconwin',
        minWidth:75,
        width : 700,
        height: 400,
        resizable :true,
        scrollable:true,
        layout:'fit',
        border:false,
        buttons: [{
            text:WtfGlobal.getLocaleText("acc.common.submit"),
            scope:this,
            handler:function(){
                this.submitForm();     
            }
        },{
            text: WtfGlobal.getLocaleText("acc.common.close"),
            scope:this,
            handler: function(){
                this.close();
            }
        }]
        
    });
}

Wtf.extend(Wtf.ProductThresholdFormWin,Wtf.Window,{
    
    initComponent:function (){
        Wtf.ProductThresholdFormWin.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetFormWindow();
        
    
        this.mainPanel = new Wtf.Panel({
            layout:'border',
            items:[
            this.northPanel,
            this.thresholdGrid
            ]
            
        });
    
        this.add(this.mainPanel);
    },
    GetNorthPanel:function (){
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:80,
            border:false,
            bodyStyle:"background-color:white;padding:8px;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(this.windowTitle, this.windowDetail,'images/accounting_image/price-list.gif', true)
        });
    },
    GetFormWindow:function (){
        
        
        this.gridRec = new Wtf.data.Record.create([
        {
            name:"storeId"
        },

        {
            name:"storeCode"
        },

        {
            name:"storeName"
        },

        {
            name:"productId"
        },
        {
            name:"productCode"
        },
        {
            name:"productName"
        },
        {
            name:"thresholdLimit"
        }
        ]);

        this.reader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.gridRec);
            
            
        this.gridStore = new Wtf.data.Store({
            url:"INVThreshold/getProductWiseThresholdList.do",
            reader:this.reader,
            baseParams: {
                productId : this.ProductId
            }
        });
        
        this.gridStore.on('load', function(){
            }, this);
        
        this.quantityeditor=new Wtf.form.NumberField({
            scope: this,
            allowBlank:false,
            allowDecimals:true,
            decimalPrecision:4,//Wtf.companyPref.quantityDecimalPrecision,
            allowNegative:false
        })
        
        this.cm= new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header:WtfGlobal.getLocaleText("acc.product.threshold.grid.storeid"),
                dataIndex:'storeId',
                hidden:true
            },
            {
                header:WtfGlobal.getLocaleText("acc.product.threshold.grid.storecode"),
                dataIndex:'storeCode'
            },
            {
                header:WtfGlobal.getLocaleText("acc.product.threshold.grid.storename"),
                dataIndex:'storeName'
            },

            {
                header:WtfGlobal.getLocaleText("acc.product.threshold.grid.ProductId"),
                dataIndex:'productId',
                hidden: (this.ProductId != "" && this.ProductId != undefined) ? true :false
            },
            {
                header:WtfGlobal.getLocaleText("acc.product.threshold.grid.storeID"),
                dataIndex:'productCode',
                hidden: (this.ProductId != "" && this.ProductId != undefined) ? true :false
            },
            {
                header:WtfGlobal.getLocaleText("acc.product.threshold.grid.productname"),
                dataIndex:'productName',
                hidden: (this.ProductId != "" && this.ProductId != undefined) ? true :false
            },
            {
                header:WtfGlobal.getLocaleText("acc.product.threshold.grid.thresholdlimit"),
                dataIndex:'thresholdLimit',
                editor:this.quantityeditor
            }
            ]);
            
        this.gridStore.load();
            
        this.thresholdGrid= new Wtf.grid.EditorGridPanel({
            layout:'fit',
            region: 'center',
            id:'thresholdgrid'+this.id,
            scope:this,
            clicksToEdit:1,
            autoScroll:true,
            cm: this.cm,
            loadMask : true,
            store: this.gridStore,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        
    },
    getModifiedThresholdData: function(){
        
        var dataArray = [];
        var recs  = this.gridStore.getModifiedRecords();
        for(var i = 0 ; i< recs.length ; i++ ){
            var rec = recs[i];
            var rowDetail = {};
            rowDetail.storeId = rec.get('storeId');
            rowDetail.productId = rec.get('productId');
            rowDetail.thresholdLimit = rec.get('thresholdLimit');
            
            dataArray.push(rowDetail);
        };
        return dataArray;
    },
    submitForm : function (){
        var thresholdDataArray=this.getModifiedThresholdData();
        if(thresholdDataArray.length == 0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.product.threshold.update.record")],0);
            return;
        }
             
        Wtf.Ajax.requestEx({
            url:"INVThreshold/updateProductThreshold.do",
            params: {
                thresholdDataArray:JSON.stringify(thresholdDataArray)
            }
        },
        this,
        function(result, req){

            var msg=result.msg;
            var title=WtfGlobal.getLocaleText("acc.common.error");
            if(result.success){
                                
                title=WtfGlobal.getLocaleText("acc.common.success");
                WtfComMsgBox([title,result.msg],0);
                this.close();
                           
            }
            else if(result.success==false){
                title=WtfGlobal.getLocaleText("acc.common.error");
                WtfComMsgBox([title,result.msg],0);
                return false;
            }
        },
        function(result, req){
            WtfComMsgBox(["Failure", WtfGlobal.getLocaleText("acc.common.error.occurred")],3);
            return false;
        });
    }
    
    
});



