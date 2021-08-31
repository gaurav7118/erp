/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

function interStoreTransfers(obj){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.interstorestocktransfer, Wtf.Perm.interstorestocktransfer.createistreq)) {
        var mainTabId = Wtf.getCmp("as");
        var winid="interStoreTransferTab";
        if(obj!=undefined && obj.isJobWorkStockOut==true){
            winid="jobWorkOutTab";
        }
        var projectBudget = Wtf.getCmp(winid);
        
        if(projectBudget == null){
            projectBudget = new Wtf.StockTransferTabPanel({
                layout:"fit",
                title:obj!=undefined?(obj.isJobWorkStockOut?WtfGlobal.getLocaleText("acc.JobWorkOut.StockTransfer"):WtfGlobal.getLocaleText("acc.up.45")):WtfGlobal.getLocaleText("acc.up.45"),
                closable:true,
                border:false,
                id:winid,
                iconCls:getButtonIconCls(Wtf.etype.inventoryist),
                TransferType: 'STORE',
                isJobWorkStockOut:obj!=undefined?obj.isJobWorkStockOut:false
            });
            mainTabId.add(projectBudget);
        }
        mainTabId.setActiveTab(projectBudget);
        mainTabId.doLayout();
    }else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
    }
}
function interLocationTransfers(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.interlocationstocktransfer, Wtf.Perm.interlocationstocktransfer.createinterlocationtransferreq)) {
        var mainTabId = Wtf.getCmp("as");
        var projectBudget = Wtf.getCmp("interLocationTransferTab");
        if(projectBudget == null){
            projectBudget = new Wtf.StockTransferTabPanel({
                layout:"fit",
                title:WtfGlobal.getLocaleText("acc.inv.interLocationStockTransfer.tabtitle"),
                tabTip:WtfGlobal.getLocaleText("acc.inv.interLocationStockTransfer.tabtitle"),
                closable:true,
                border:false,
                iconCls:getButtonIconCls(Wtf.etype.inventoryilst),
                id:"interLocationTransferTab",
                TransferType: 'LOCATION'
            });
            mainTabId.add(projectBudget);
        }
        mainTabId.setActiveTab(projectBudget);
        mainTabId.doLayout();
    }else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
    }
}
function interLocationTransfersReport(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.interlocationstocktransfer, Wtf.Perm.interlocationstocktransfer.createinterlocationtransferreq)) {
        var mainTabId = Wtf.getCmp("as");
        var projectBudget = Wtf.getCmp("interLocationTransferReportTab");
        if(projectBudget == null){
            projectBudget = new Wtf.InterLocationReportTabPanel({
                layout:"fit",
                title:WtfGlobal.getLocaleText("acc.stockavailability.InterLocationStockTransferDetails"),
                tabTip:WtfGlobal.getLocaleText("acc.stockavailability.InterLocationStockTransferDetails"),
                closable:true,
                border:false,
                iconCls:getButtonIconCls(Wtf.etype.inventoryiltd),
                id:"interLocationTransferReportTab"
            });
            mainTabId.add(projectBudget);
        }
        mainTabId.setActiveTab(projectBudget);
        mainTabId.doLayout();
    }else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
    }
}

Wtf.StockTransferTabPanel = function (config){
    Wtf.apply(this,config);
    Wtf.StockTransferTabPanel.superclass.constructor.call(this);
}

Wtf.extend(Wtf.StockTransferTabPanel,Wtf.Panel,{
    initComponent:function (){
        Wtf.StockTransferTabPanel.superclass.initComponent.call(this);
        if(this.TransferType == 'STORE'){
            this.getInterStockTransferForm();
            this.add(this.InterStockTransferForm);
        }else if(this.TransferType == 'LOCATION'){
            this.getInterLocationTransferForm();
            this.add(this.InterLoationTransferForm);
        }
        
    },
    getInterStockTransferForm:function (){
        this.InterStockTransferForm = new Wtf.transfer({
            id:"interstoretransfer",
            layout:'fit',
            //            title:"Inter Store Transfer",
            // closable:true,
            border:false,
            isJobWorkStockOut:this.isJobWorkStockOut
        });
    },
    getInterLocationTransferForm:function (){
        this.InterLoationTransferForm = new Wtf.interLocationTransfer({
            id:"interlocationtransfer",
            layout:'fit',
            //            title:"Inter Store Transfer",
            // closable:true,
            border:false
        });
    }
});
//--------------------------------------------Transfer Report Panel-----------------------------------------

function interStoreTransfersReport(jobWorkStockOut){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.interstorestocktransfer, Wtf.Perm.interstorestocktransfer.viewistreq)) {
        var mainTabId = Wtf.getCmp("as");
        var title=WtfGlobal.getLocaleText("acc.inventoryList.InterStoreTransferDetails");
        var winid="interStoreTransferReportTab";
        if (jobWorkStockOut != undefined && jobWorkStockOut != "") {
            winid="jobworkstockoutReportTab";
            title=WtfGlobal.getLocaleText("acc.JobWorkOut.StockTransfer");
        }
        var projectBudget = Wtf.getCmp(winid);
        if(projectBudget == null){
            projectBudget = new Wtf.StockTransferReportTabPanel({
                layout:"fit",
                title:title,
                closable:true,
                border:false,
                iconCls:getButtonIconCls(Wtf.etype.inventoryistd),
                id:winid,
                jobWorkStockOut:jobWorkStockOut
            });
            mainTabId.add(projectBudget);
        }
        mainTabId.setActiveTab(projectBudget);
        mainTabId.doLayout();
    }else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
    }
}

Wtf.StockTransferReportTabPanel = function (config){
    Wtf.apply(this,config);
    Wtf.StockTransferReportTabPanel.superclass.constructor.call(this);
}

Wtf.extend(Wtf.StockTransferReportTabPanel,Wtf.Panel,{
    initComponent:function (){
        Wtf.StockTransferReportTabPanel.superclass.initComponent.call(this);
        this.getTabpanel();
        this.add(this.tabPanel);
    },
    getTabpanel:function (){
        var winid="interstoretabpanel";
        var tabArray=new Array();
        if (this.jobWorkStockOut != undefined && this.jobWorkStockOut != "") {
           /**
            * for Job Work Out only history Tab is shown 
            */
           
            this.getTransferHistory(this.jobWorkStockOut);
            winid = "jobworkstockoutpanel";
            tabArray.push(this.TransferHistory);
        } else {
            /**
             * For IST
             */
            this.getInComingTranfers();
            this.getOutGoingTranfers();
            this.getTransferHistory();
            tabArray.push(this.InComingTranfers);
        tabArray.push(this.OutGoingTranfers);
        tabArray.push(this.TransferHistory);
        }
        
        this.tabPanel = new Wtf.TabPanel({
            activeTab:0,
            id:winid,
            items:tabArray
        });
    },
    getInterStockTransferForm:function (){
        this.InterStockTransferForm = new Wtf.transfer({
            id:"interstoretransfer",
            layout:'fit',
            title:WtfGlobal.getLocaleText("acc.accPref.autoInstStore"),
            // closable:true,
            border:false
        });
    },
    getInComingTranfers:function (jobWorkStockOut){
        var winid = "incomingRequest";
        if (jobWorkStockOut != undefined) {
            winid = "jobincomingRequest"
        }
        this.InComingTranfers = new Wtf.interStoreTransformRequest({
            id:winid,
            layout:'fit',
            title:WtfGlobal.getLocaleText("acc.stockavailability.IncomingRequests"),
            iconCls:getButtonIconCls(Wtf.etype.inventoryistd),
            reportType:1,//changes variable name 'type' to 'reportType' because 'type' already used while export data.
            // closable:true,
            border:false,
            jobWorkStockOut:jobWorkStockOut
        });
    },
    getOutGoingTranfers:function (jobWorkStockOut){
        var winid = "outgoingRequest";
        if (jobWorkStockOut != undefined) {
            winid = "joboutgoingRequest"
        }
        this.OutGoingTranfers = new Wtf.interStoreTransformRequest({
            id:winid,
            layout:'fit',
            title:WtfGlobal.getLocaleText("acc.stockavailability.OutgoingRequests"),
            iconCls:getButtonIconCls(Wtf.etype.inventoryistd),
            reportType:3,//changes variable name 'type' to 'reportType' because 'type' already used while export data.
            // closable:true,
            border:false,
            jobWorkStockOut:jobWorkStockOut
        });
    },
    getTransferHistory:function (jobWorkStockOut){
        var winid = "interstoretransferRequest";
        if (jobWorkStockOut != undefined) {
            winid = "jobinterstoretransferRequest"
        }
        var title=(jobWorkStockOut)?WtfGlobal.getLocaleText("acc.jobWorkIn.jobworkoutStockTransferRegister"):WtfGlobal.getLocaleText("acc.stockavailability.StoreTransferHistory");
        this.TransferHistory = new Wtf.interStoreTransformRequest({
            id:winid,
            layout:'fit',
            title:title,
            iconCls:getButtonIconCls(Wtf.etype.inventoryistd),
            reportType:2,//changes variable name 'type' to 'reportType' because 'type' already used while export data.
            // closable:true,
            border:false,
            jobWorkStockOut:jobWorkStockOut,
            moduleid: Wtf.autoNum.JobWorkOutStockTransferModuleID
        });
    }
});
