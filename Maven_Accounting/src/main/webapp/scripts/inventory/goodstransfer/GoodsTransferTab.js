/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
function goodsOrderTab(){
    var mainTabId = Wtf.getCmp("as");
    var goodsTransferTab = Wtf.getCmp("goodsOrderParentTabb");
    if(goodsTransferTab == null){
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.createstockreq) || !WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.viewstockreq)){
            goodsTransferTab = new Wtf.GoodsTabPanel({
                layout:"fit",
                title:WtfGlobal.getLocaleText("acc.up.42"),
                closable:true,
                border:false,
                id:"goodsOrderParentTabb",
                type:"order",
                iconCls:getButtonIconCls(Wtf.etype.inventorysr)
            });
            mainTabId.add(goodsTransferTab);
        }else{
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
        }
    }
    if(goodsTransferTab != undefined && goodsTransferTab != null){
        mainTabId.setActiveTab(goodsTransferTab);
        mainTabId.doLayout();
    }
}
/**
 * 
 * @Desc  : Open Stock request form for Template creation 
 * Open same component which used for normal one
 * Send isTemplate = true
 */
function callStockRequestTemplate(winid){
    var mainTabId = Wtf.getCmp("as");
    winid=(winid==null?'goodsOrderTemplateParentTabb':winid);
    var goodsTransferTab = Wtf.getCmp(winid);
    if(goodsTransferTab == null){
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.createstockreq) || !WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.viewstockreq)){
            goodsTransferTab = new Wtf.GoodsTabPanel({
                layout:"fit",
                title:WtfGlobal.getLocaleText("acc.field.inventorytemplate.stockrequest"),
                closable:true,
                border:false,
                id:winid,
                type:"order",
                iconCls:getButtonIconCls(Wtf.etype.inventorysr),
                isTemplate:true
            });
            mainTabId.add(goodsTransferTab);
        }else{
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
        }
    }
    if(goodsTransferTab != undefined && goodsTransferTab != null){
        mainTabId.setActiveTab(goodsTransferTab);
        mainTabId.doLayout();
    }
}
function goodsIssueTab(){
    var mainTabId = Wtf.getCmp("as");
    var goodsTransferTab = Wtf.getCmp("goodsIssueParentTab");
    if(goodsTransferTab == null){
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.issuenote, Wtf.Perm.issuenote.createissuenote) || !WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.viewstockreq)){
            goodsTransferTab = new Wtf.GoodsTabPanel({
                layout:"fit",
                title:WtfGlobal.getLocaleText("acc.dimension.invmodule.1"),
                iconCls:getButtonIconCls(Wtf.etype.inventorysi),
                closable:true,
                border:false,
                id:"goodsIssueParentTab",
                type:"issue"
            });
            mainTabId.add(goodsTransferTab);
        }else{
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
        }
    }
    if(goodsTransferTab != undefined && goodsTransferTab != null){
        mainTabId.setActiveTab(goodsTransferTab);
        mainTabId.doLayout();
    }
}

Wtf.GoodsTabPanel = function (config){
    Wtf.apply(this,config);
    Wtf.GoodsTabPanel.superclass.constructor.call(this);
}

Wtf.extend(Wtf.GoodsTabPanel,Wtf.Panel,{
    onRender:function (config) {
        Wtf.GoodsTabPanel.superclass.onRender.call(this,config);
        this.getTabpanel();
        this.add(this.tabPanel);
        this.isTemplate=config.isTemplate==undefined?false:config.isTemplate;
    },
    getTabpanel:function (){
        this.getTransferHistory();

        this.itemsarr = [];
        if(this.type == "issue"){
            if(true && (!WtfGlobal.EnableDisable(Wtf.UPerm.issuenote, Wtf.Perm.issuenote.createissuenote))){//integrationFeatureFor != Wtf.IF.COILCRAFT){
                this.getGoodsIssueForm();
                this.itemsarr.push( this.issue);
            }
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.createstockreq) || !WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.viewstockreq) 
             || !WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.issuestockreq) || !WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.collectstockreq)){
                this.getInTransitTranfers();
            }
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.viewstockreq)) {
                this.itemsarr.push(this.goodsordertransferRequestTab);
            }
        }else{
            this.getGoodsOrderForm();
            this.getPendingOrders();
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.createstockreq)) {
                    this.itemsarr.push(this.order);
            }
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.viewstockreq)) {
                /**
                 * Stock Request Pending Orders and Fulfilled Order tab should in case of stock request template
                 */
                if(!this.isTemplate){
                    this.itemsarr.push(this.goodsPendingOrderTab);
                }
            }
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.viewstockreq)) {
            if(!this.isTemplate){
                this.itemsarr.push(this.goodsordertransferApprovedTab);
            }
        }
        
        this.tabPanel = new Wtf.TabPanel({
            activeTab:0,
            id:"goodstransfertabpanel"+this.id,
            items:this.itemsarr
        });
    },
    getGoodsOrderForm:function(){
        this.order =new Wtf.order({
            id:"order111"+this.id,
            layout:'fit',
            title:this.isTemplate?WtfGlobal.getLocaleText("acc.field.inventorytemplate.stockrequest"):WtfGlobal.getLocaleText("acc.dimension.invmodule.2"),
            iconCls:getButtonIconCls(Wtf.etype.inventorysr),
            border:false,
            isTemplate:this.isTemplate!=undefined?this.isTemplate:false
        });
    },
    getGoodsIssueForm:function (){
        this.issue =new Wtf.goodIssue({
            id:"goodissue111"+this.id,
            layout:'fit',
            title:WtfGlobal.getLocaleText("acc.inventory.stockrequest.grid.printmenu.issuenote"),
            iconCls:getButtonIconCls(Wtf.etype.inventorysi),
            type:0, 
            border:false
        });
    },
    getInTransitTranfers:function (){
        this.goodsordertransferRequestTab =new Wtf.GoodsOrderTransfer({
            id:"interstocktransferreq"+this.id,
            layout:'fit',
            title:WtfGlobal.getLocaleText("acc.stock.StoreOrders"),
            iconCls:getButtonIconCls(Wtf.etype.inventorysi),
            type:1,
            border:false
        });
    },
    getTransferHistory:function (){
        this.goodsordertransferApprovedTab =new Wtf.GoodsOrderTransfer({
            id:"goodsapproved"+this.id,
            layout:'fit',
            title:WtfGlobal.getLocaleText("acc.inventory.QAAproval.FulfilledOrders"),
            iconCls:getButtonIconCls(Wtf.etype.inventorysr),
            type:2,
            border:false
        });
    },
    getPendingOrders:function (){
        this.goodsPendingOrderTab =new Wtf.GoodsOrderTransfer({
            id:"goodsPendingOrders"+this.id,
            layout:'fit',
            title:WtfGlobal.getLocaleText("acc.stock.GoodsPendingOrders"),
            iconCls:getButtonIconCls(Wtf.etype.inventorysr),
            type:3,
            border:false
        });
    }
});