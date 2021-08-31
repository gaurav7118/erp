/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */





// ------------   Stock Adjustment tab 

function markoutallTab(isJobWorkInReciever){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.stockadjustment, Wtf.Perm.stockadjustment.createstockadj)) {
        var mainTabId = Wtf.getCmp("as");
        var markoutallTab = isJobWorkInReciever?Wtf.getCmp("jobWorkInrecieverTab"):Wtf.getCmp("allmarkoutTab");
        if(markoutallTab == null){
            markoutallTab = new Wtf.makoutallTabPanel({
                layout:"fit",
                title:isJobWorkInReciever?WtfGlobal.getLocaleText("acc.jobworkin.create.JobWorkStockIn"):WtfGlobal.getLocaleText("acc.dimension.invmodule.3"),
                closable:true,
                isJobWorkInReciever:isJobWorkInReciever,
                border:false,
                iconCls:getButtonIconCls(Wtf.etype.inventorysa),
                id:isJobWorkInReciever?"jobWorkInrecieverTab":"allmarkoutTab"
            });
            mainTabId.add(markoutallTab);
        }
        mainTabId.setActiveTab(markoutallTab);
        mainTabId.doLayout();
        markoutallTab.on("close", function () {
            Wtf.stockAdjustmentTempDataHolder = [];
            Wtf.stockAdjustmentProdBatchQtyMapArr= [];
        }, this);
    }else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
    }
}


Wtf.makoutallTabPanel = function (config){
    Wtf.apply(this,config);
    Wtf.makoutallTabPanel.superclass.constructor.call(this);
}

Wtf.extend(Wtf.makoutallTabPanel,Wtf.Panel,{
    //    initComponent:function (){
    //        Wtf.makoutallTabPanel.superclass.initComponent.call(this);
    //        this.getTabpanel();
    //        this.add(this.mainmarkout);
    //         this.add(this.tabPanel);
    //    },
    onRender:function (config) {
        Wtf.makoutallTabPanel.superclass.onRender.call(this,config);
        
        this.stocoutApproval = false;
        
//        Wtf.Ajax.requestEx({
//            url: "INVConfig/getConfig.do"
//        },
//        this,
//        function(result, req){
//            var retstatus = result.data.enableStockAdjustmentApprovalFlow;
//            this.stocoutApproval= retstatus;
//            
            var itemArr=this.getTabpanel();
            
            this.tabPanel = new Wtf.TabPanel({
                activeTab:0,
                id:this.isJobWorkInReciever?"jobWorkInrecieverPanel":"markouttabpanel",
                items:itemArr,
                border:false
            });
        
            this.add(this.tabPanel);
//            this.doLayout();
            
//        },
//        function(result, req){
//            // alert("in failure");
//            });
//        
        
    },
    getTabpanel:function (){
        this.getnormalMarkoutForm();
       
        this.itemsarr = [];
        this.itemsarr.push(this.mainmarkout);
        
        if(this.stocoutApproval){ 
            this.getPendingOrders();
            this.getRejectedOrders();
            this.itemsarr.push(this.stockPendingOrderTab);
            this.itemsarr.push(this.stockRejectedOrderTab);
        }
      
        return this.itemsarr;
    },
    getnormalMarkoutForm:function (){
        this.mainmarkout = new Wtf.markout({
            id:"markout"+this.id,
            layout:'fit',
            title:this.isJobWorkInReciever?WtfGlobal.getLocaleText("acc.jobworkin.create.JobWorkStockIn"):WtfGlobal.getLocaleText("acc.stock.StockAdjustmentForm"),
            closable:false,
            isJobWorkInReciever:this.isJobWorkInReciever,
            type:"markout",
            iconCls:getButtonIconCls(Wtf.etype.inventorysa),
            drafttype: 2,
            border:false
        })
    },
    getPendingOrders:function (){
        this.stockPendingOrderTab =new Wtf.markoutPendingGrid({
            id:"markoutListCmp"+this.id,
            layout:'fit',
            title:"Pending Stock Adjustment",
            closable:false,
            type:"markout",
            drafttype: 2,
            border:false
             
        });
    },
    getRejectedOrders:function (){
        this.stockRejectedOrderTab =new Wtf.markoutRejectedGrid({
            id:"markoutRejListCmp"+this.id,
            layout:'fit',
            title:"Rejected Stock Adjustment",
            closable:false,
            type:"markout",
            drafttype: 3,
            border:false
             
        });
    },
    emeal:function (){
        this.emealtab = new Wtf.markout({
            id:"emeal"+this.id,
            layout:'fit',
            title:"Employee Meal",
            //closable:true,
            type:"emeal",
            drafttype: 3,
            border:false
        })
    },
    sampling:function (){
        this.samplingtab = new Wtf.markout({
            id:"smapling"+this.id,
            layout:'fit',
            title:"Sampling",
            //closable:true,
            type:"sampling",
            drafttype: 4,
            border:false
        })
    }
    
});
