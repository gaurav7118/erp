/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



// ------------------- Stockout QA Approval Report


function qaApprovalTab(isJobWorkOrder,isisJobWorkOrderInQA){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.qa, Wtf.Perm.qa.viewqa)&& Wtf.account.companyAccountPref.activateQAApprovalFlow) {
        var mainTabId = Wtf.getCmp("as");
        var qaApprovalTab = isJobWorkOrder?Wtf.getCmp("qaApprovaljobworkorderTab" +isisJobWorkOrderInQA):Wtf.getCmp("qaApprovalTab");
        if(qaApprovalTab == null){
            qaApprovalTab = new Wtf.QAApprovalTab({
                layout:"fit",
                title:WtfGlobal.getLocaleText("acc.up.47"),
                closable:true,
                isJobWorkOrder:isJobWorkOrder,
                isisJobWorkOrderInQA: isisJobWorkOrderInQA,
                iconCls:getButtonIconCls(Wtf.etype.inventoryqa),
                border:false,
                id:isJobWorkOrder?"qaApprovaljobworkorderTab"+isisJobWorkOrderInQA:"qaApprovalTab"
            
            });
            mainTabId.add(qaApprovalTab);
        }
        mainTabId.setActiveTab(qaApprovalTab);
        mainTabId.doLayout();
    }else{
        Wtf.account.companyAccountPref.activateQAApprovalFlow?WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature"):WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.Stockrepair.activate.msg")],3);
    }
}
function stockRepaire(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.stockrepair, Wtf.Perm.stockrepair.viewstockrepair)&& Wtf.account.companyAccountPref.activateQAApprovalFlow) {
        var mainTabId = Wtf.getCmp("as");
        var RepaireApprovalTab = Wtf.getCmp("stockrepaireTab");
        if(RepaireApprovalTab == null){
            RepaireApprovalTab = new Wtf.RepaireApprovalTab({
                layout:"fit",
                title:WtfGlobal.getLocaleText("acc.up.56"),
                closable:true,
                border:false,
                iconCls:getButtonIconCls(Wtf.etype.inventorysrep),
                id:"stockrepaireTab"
            });
            mainTabId.add(RepaireApprovalTab);
        }
        mainTabId.setActiveTab(RepaireApprovalTab);
        mainTabId.doLayout();
    }else{
        Wtf.account.companyAccountPref.activateQAApprovalFlow?WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature"):WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.Stockrepair.activate.msg")],3);
    }
}

Wtf.QAApprovalTab = function (config){
    Wtf.apply(this,config);
    this.isJobWorkOrder= config.isJobWorkOrder;
    this.isisJobWorkOrderInQA=config.isisJobWorkOrderInQA;
    Wtf.QAApprovalTab.superclass.constructor.call(this);
}

Wtf.extend(Wtf.QAApprovalTab,Wtf.Panel,{
    onRender:function (config) {
        Wtf.QAApprovalTab.superclass.onRender.call(this,config);
        this.getTabpanel();
        this.add(this.tabPanel);
    },
    getTabpanel:function (){
        this.getQAAprroval();
        this.getQAAprrovalReport();
        
        this.itemsarr = [];
        
        this.itemsarr.push(this.qa);
        this.itemsarr.push(this.qadoneReport);
            
        this.tabPanel = new Wtf.TabPanel({
            activeTab:0,
            id:"qaApprovalTabb"+this.isisJobWorkOrderInQA,
            items:this.itemsarr
        });
        
        this.on('activate',function(){
            this.tabPanel.setActiveTab(0);
            this.tabPanel.doLayout();
        },this);
    },
    getQAAprroval:function(){
        this.qa =new Wtf.StockoutApproval({
            id:this.isJobWorkOrder?"StockoutQACmpjobworkorder" + 0+this.id:"StockoutQACmp"+this.id,
            layout:'fit',
            isJobWorkOrder:this.isJobWorkOrder,
            isisJobWorkOrderInQA : this.isisJobWorkOrderInQA,
            title:WtfGlobal.getLocaleText("acc.inventory.PendingQAApproval"),
            status:"PENDING",
            iconCls:getButtonIconCls(Wtf.etype.inventoryqa),
            border:false
        });
    },
    getQAAprrovalReport:function (){
        this.qadoneReport =new Wtf.StockoutApproval({
            id:this.isJobWorkOrder?"StockoutQACmpjobworkorder" +3+this.id :"StockoutQACmp"+3+this.id,
            layout:'fit',
            isJobWorkOrder:this.isJobWorkOrder,
            isisJobWorkOrderInQA : this.isisJobWorkOrderInQA,
            title:WtfGlobal.getLocaleText("acc.inventory.QAApprovalReport"),
            iconCls:getButtonIconCls(Wtf.etype.inventoryqa),
            status:"DONE",
            border:false
        });
    }
//    getQAAprrovalReport:function (){
//        this.qaReport =new Wtf.QAApprovalReport({
//            id:"qaapprovalreort",
//            layout:'fit',
//            title:"QA Rejected Items",
//            border:false
//        });
//    }
});

Wtf.RepaireApprovalTab = function (config){
    Wtf.apply(this,config);
    Wtf.RepaireApprovalTab.superclass.constructor.call(this);
}

Wtf.extend(Wtf.RepaireApprovalTab,Wtf.Panel,{
    onRender:function (config) {
        Wtf.RepaireApprovalTab.superclass.onRender.call(this,config);
        this.getTabpanel1();
        this.add(this.tabPanel);
    },
    getTabpanel1:function (){
        this.stockRepaireTab();
        this.stockRepaireReportTab();
        this.itemsarr = [];
        this.itemsarr.push(this.stockRepaire);
        this.itemsarr.push(this.stockRepaireReport);
        
        this.tabPanel = new Wtf.TabPanel({
            activeTab:0,
            //            id:"stockrepairePan",
            items:this.itemsarr
        });
    },
    stockRepaireTab:function (){
        this.stockRepaire =new Wtf.QAApprovalReport({
            id:"stockrepairetab"+0,
            layout:'fit',
            title:WtfGlobal.getLocaleText("acc.up.56"),
            iconCls:getButtonIconCls(Wtf.etype.inventorysrep),
            type:1,
            border:false
        });
    },
    stockRepaireReportTab:function (){
        this.stockRepaireReport =new Wtf.QAApprovalReport({
            id:"stockrepairetab"+1,
            layout:'fit',
            title:WtfGlobal.getLocaleText("acc.stockrepair.StockRepairReport"),
            iconCls:getButtonIconCls(Wtf.etype.inventorysrep),
            type:2,
            border:false
        });
    }
});

function StockoutQA(){
    var demo=Wtf.getCmp("StockoutQACmp"+0)
    var main=Wtf.getCmp("as");
    if(demo==null){
        demo =new Wtf.StockoutApproval({
            id:this.isJobWorkOrder?"StockoutQACmpjobworkorder":"StockoutQACmp",
            layout:'fit',
            module:0,
            isJobWorkOrder:this.isJobWorkOrder,
            title:"QA Approval",
            closable:true,
            border:false
        })
        main.add(demo);
    }
    main.setActiveTab(demo);
    main.doLayout();
}

function consignmentApproval(){
    var mainTabId = Wtf.getCmp("as");
    var projectBudget = Wtf.getCmp("consignmentApproval"+1);
    if(projectBudget == null){
        projectBudget = new Wtf.StockoutApproval({
            layout:"fit",
            title:WtfGlobal.getLocaleText("acc.field.consignmentreturnapproval"),//"Consignment Return Approval ",
            closable:true,
            module:1,
            isJobWorkOrder:this.isJobWorkOrder,
            border:false,
            id:this.isJobWorkOrder?"consignmentApprovaljobworkorder" + 1:"consignmentApproval"+1
        });
        mainTabId.add(projectBudget);
    }
    mainTabId.setActiveTab(projectBudget);
    mainTabId.doLayout();
}
//function stockRepaireTab(){
//    var mainTabId = Wtf.getCmp("as");
//    var stockRepaire = Wtf.getCmp("stockrepairetab"+1);
//    if(stockRepaire == null){
//        stockRepaire =new Wtf.QAApprovalReport({
//            id:"stockrepairetab",
//            layout:'fit',
//            title:"Stock Repaire",
//            closable:true,
//            border:false
//        });
//        mainTabId.add(stockRepaire);
//    }
//    mainTabId.setActiveTab(stockRepaire);
//    mainTabId.doLayout();
//}

function callInvQAApprovalReport(){
    var mainTabId = Wtf.getCmp("as");
    var projectBudget = Wtf.getCmp("qaapprovalreort");
    if(projectBudget == null){
        projectBudget = new Wtf.QAApprovalReport({
            layout:"fit",
            title:"QA Rejected Items ",
            closable:true,
            border:false,
            id:"qaapprovalreort"
        });
        mainTabId.add(projectBudget);
    }
    mainTabId.setActiveTab(projectBudget);
    mainTabId.doLayout();
}

Wtf.StockoutApproval = function(config){
    this.isJobWorkOrder=config.isJobWorkOrder,
    this.isisJobWorkOrderInQA=config.isisJobWorkOrderInQA;
    Wtf.StockoutApproval.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.StockoutApproval, Wtf.Panel, {
    onRender: function(config) {
        Wtf.StockoutApproval.superclass.onRender.call(this, config);

        //        this.status = 0;
        this.fromdateVal =new Date().getFirstDateOfMonth();
        this.fromdateVal.setDate(new Date().getFirstDateOfMonth().getDate());
        this.frmDate = new Wtf.form.DateField({
            emptyText:'From date...',
            readOnly:true,
            width : 85,
            value:WtfGlobal.getDates(true),
            minValue: Wtf.archivalDate,
            name : 'frmdate',
            format: 'Y-m-d'
        });
        this.todateVal=new Date().getLastDateOfMonth();
        this.todateVal.setDate(new Date().getLastDateOfMonth().getDate());
        this.toDate = new Wtf.form.DateField({
            emptyText:'To date...',
            readOnly:true,
            width : 85,
            name : 'todate',
            value:WtfGlobal.getDates(false),
            minValue: Wtf.archivalDate,
            format: 'Y-m-d'
        });
        var moduleData = [];
        if(Wtf.account.companyAccountPref.activateInventoryTab){
            moduleData.push(["ALL", "ALL"],["STOCK ADJUSTMENT", "Stock Adjustment"],["STOCK REQUEST", "Stock Request"],["INTER STORE TRANSFER", "Inter Store Transfer"],["GOOD RECEIPT", "Good Receipt Note"])
        }
        if(Wtf.account.companyAccountPref.consignmentSalesManagementFlag){
            moduleData.push(["CONSIGNMENT", "Consignment"])
        }
        if (Wtf.account.companyAccountPref.BuildAssemblyApprovalFlow) {
            moduleData.push(["BuildAssemblyQA", "Build Assembly"]);
        }
        if (Wtf.account.companyAccountPref.isQaApprovalFlowInDO) {
            moduleData.push(["DELIVERY ORDER", "Delivery Order"]);
        }
        if (Wtf.account.companyAccountPref.activateMRPManagementFlag && Wtf.account.companyAccountPref.columnPref.isQaApprovalFlowInMRP) {
            moduleData.push(["Work Order", "Work Order"]);
        }
        
        
        this.moduletype = new Wtf.data.SimpleStore({
            fields:["id", "name"],
            data : moduleData
        });
        this.typecmb = new Wtf.form.ComboBox({
            hiddenName : 'statusFilter',
            store : this.moduletype,
            typeAhead:true,
            readOnly: false,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 110,
            triggerAction: 'all',
            emptyText:'Select module...'
        });  
        
        this.moduletype.on('load', function(){
            if(this.moduletype.getCount() > 0){
                
                 if (this.isJobWorkOrder && this.isisJobWorkOrderInQA) {
                        this.typecmb.setValue(this.moduletype.getAt(5).get('id'));
                } else {
                this.typecmb.setValue(this.moduletype.getAt(0).get('id'));
            }
            }
        }, this)
        
        this.moduletype.loadData(moduleData);
        
        this.statusStore = new Wtf.data.SimpleStore({
            fields:["id", "name"],
            data : [["PENDING", "PENDING"],["DONE", "DONE"]]
        });
        this.statuscmb = new Wtf.form.ComboBox({
            hiddenName : 'statusFilter',
            store : this.statusStore,
            typeAhead:true,
            readOnly: false,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 80,
            triggerAction: 'all',
            emptyText:'Status'
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
                byStoreExecutive:"true"
            },
            //            sortInfo: {
            //                field: 'fullname',
            //                direction: 'ASC' // or 'DESC' (case sensitive for local sorting)
            //            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.storeCmbRecord)
        });
        
        this.storeCmbStore.on("load", function(ds, rec, o){
            if(rec.length > 1){
                var newRec=new this.storeCmbRecord({
                    store_id:'',
                    fullname:'ALL'
                })
                this.storeCmbStore.insert(0,newRec);
                this.storeCmbfilter.setValue('');
            }else if(rec.length > 0){
                this.storeCmbfilter.setValue(rec[0].data.store_id, true);
            }
        }, this);
            
        this.storeCmbfilter = new Wtf.form.ComboBox({
            fieldLabel : 'Store*',
            hiddenName : 'store',
            store : this.storeCmbStore,
            forceSelection:true,
            displayField:'fullname',
            valueField:'store_id',
            mode: 'local',
            width : 150,
            listWidth:200, 
            triggerAction: 'all',
            emptyText:'Select store...',
            typeAhead:true,
            listWidth:300,
            tpl: new Wtf.XTemplate(
                '<tpl for=".">',
                '<div wtf:qtip = "{[values.fullname]}" class="x-combo-list-item">',
                '<div>{fullname}</div>',
                '</div>',
                '</tpl>')
        });
        this.storeCmbStore.load();  
        this.search = new Wtf.Button({
            anchor: '90%',
            text: WtfGlobal.getLocaleText("acc.het.163"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.inventory.QAAproval.ClicktoSearchOrders")
            },
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: function() {
                this.initloadgridstore(this.frmDate.getValue(),this.toDate.getValue(),this.storeCmbfilter.getValue(),this.typecmb.getValue(),this.status);
            }
        });
        this.resetQAapproval = new Wtf.Button({
            anchor : '90%',
            text: WtfGlobal.getLocaleText("acc.inventory.QAAproval.ResetFilter"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.inventory.QAAproval.Clickheretoresetfilter")
            },
            iconCls:getButtonIconCls(Wtf.etype.resetbutton),
            scope:this,
            handler:function(){
                this.frmDate.reset();
                this.toDate.reset();
                if(this.typecmb.store.getCount() > 0){
                    this.typecmb.setValue(this.typecmb.store.getAt(0).get('id'));
                }else{
                    this.typecmb.setValue("");
                }
                this.statuscmb.setValue("");
                this.storeCmbfilter.setValue('');
                this.grid.quickSearchTF.setValue('');
                //                this.initloadgridstore(this.fromdateVal,this.todateVal,"","","");
                this.initloadgridstore(this.frmDate.getValue(),this.toDate.getValue(),this.storeCmbfilter.getValue(),this.typecmb.getValue(),this.status);
            }
        });
        var tbarArray = [];
        tbarArray.push(WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.fromdate")+":",this.frmDate,"-",WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.todate")+":",this.toDate,"-",WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore")+":",this.storeCmbfilter,"-",WtfGlobal.getLocaleText("acc.common.moduleName")+":",this.typecmb,"-",this.search,this.resetQAapproval);
       
        var companyDateFormat='y-m-d';
               
        this.record = Wtf.data.Record.create([
        {
            "name":"id"
        },

        {
            "name":"storeid"
        },

        {
            "name":"storename"
        },

        {
            "name":"productcode"
        },

        {
            "name":"productname"
        },
        {
            "name":"productid"
        },

        {
            "name":"quantity"
        },
        {
            "name":"moduletype"
        },
       
        {
            "name":"uomname"
        },
        {
            "name":"transactionno"
        },
        {
            "name":"status"
        },
        {
            "name":"customer"
        },
        {
            "name":"createdon"
        },
        {
            "name":"transactionid"
        },
        {
            "name":"productdescription"
        }

        ]);
        
        
        
        this.ds = new Wtf.data.Store({
            //            url: 'INVApproval/getConsignmentApprovalList.do',
            url: 'INVApproval/getAllQAApprovalList.do',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },
            this.record
            )
        //            remoteSort: true,
        //            remoteGroup: true
        });
        
      
        this.ds.on("load",function(store,rec,opt){
            //            alert(1);
            this.grid.getView().refresh;
        },this);

        //        this.ds.on("beforeload",function(){
        //            this.ds.baseParams = {
        ////                frmDate: this.frmDate.getValue().format('Y-m-d'),
        ////                toDate: this.toDate.getValue().format('Y-m-d'),
        ////                storeid: this.storeCmbfilter.getValue(),
        ////                markouttype: "markout",
        ////                summaryFlag: this.status == 1,
        ////                dmflag:this.dmflag
        //            }
        //        },
        //        this);
        var integrationFeatureFor=true;
        
        var cmDefaultWidth = 125;
        this.cm = new Wtf.grid.ColumnModel([
            new Wtf.KWLRowNumberer(), //0
            //            this.expander, //1
            {
                header:WtfGlobal.getLocaleText("acc.inventory.QAAproval.ReferenceNoId"), //2
                dataIndex:'id',
                pdfwidth:50,
                hidden:true
            },
            {
                header:WtfGlobal.getLocaleText("acc.reval.transaction"), //2
                dataIndex:'transactionno',
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            },
            {
                header:WtfGlobal.getLocaleText("acc.field.TransactionDate"), //2
                dataIndex:'createdon',
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore"),//3
                dataIndex: 'storename',
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            },
            {
                header: WtfGlobal.getLocaleText("mrp.workorder.report.header3"),//3
                dataIndex: 'customer',
                pdfwidth:50,
                width:cmDefaultWidth,
                hidden:this.module ==0?true:false,
                fixed:true
            },
     
            {
                header: WtfGlobal.getLocaleText("acc.contractMasterGrid.header8"),//7
                dataIndex: 'productcode',
                groupable: true,
                pdfwidth:50,
                width:cmDefaultWidth
            },{
                header:WtfGlobal.getLocaleText("mrp.qcreport.gridheader.productname"),//8
                dataIndex: 'productname',
                groupable: true,
                pdfwidth:50,
                width:cmDefaultWidth
            },{
                header:WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"),//8
                dataIndex: 'productdescription',
                groupable: true,
                pdfwidth:50,
                width:cmDefaultWidth
            },
        
            {
                header: WtfGlobal.getLocaleText("acc.invoice.gridUOM"),//14
                dataIndex: 'uomname',
                pdfwidth:50,
                width:cmDefaultWidth
            },
            {
                header: WtfGlobal.getLocaleText("acc.invoice.gridQty"),//15
                dataIndex: 'quantity',
                sortable:false,
                align: 'left',//ERP-36315
                pdfwidth:50,
                width:cmDefaultWidth,
                summaryType: 'sum',
                renderer : function(v) {
                    return '<div align="left">' + parseFloat(v).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + '</div>';//ERP-36315
                }
        
            },
            {
                header: WtfGlobal.getLocaleText("acc.invoiceList.status"),//8
                dataIndex: 'status',
                groupable: true,
                pdfwidth:50,
                width:cmDefaultWidth,
                renderer:function(value){
                    if(value=="DONE"){
                        return "<label style = 'color : green;'>DONE</label>";
                    }else if(value=="REJECTED"){
                        return "<label style = 'color : red;'>REJECTED</label>";
                    }else{
                        return value;
                    }
                }
            }, {
                header: WtfGlobal.getLocaleText("view.pendingapproval.Module"),//19
                dataIndex:'moduletype',
                align:'center',
                width:110,
                pdfwidth:50,
                hidden:true
            },
         
            {
                header: WtfGlobal.getLocaleText("acc.lp.viewccd"),//19
                dataIndex: 'type',
                align:'center',
                width:100,
                //                pdfwidth:50,
                hidden:false,
                renderer: this.serialRenderer.createDelegate(this)
            }]);
        
        

        /**************date *****/
        
        this.exportButton = new Wtf.exportButton({
            obj: this,
            // id: 'stockrepairreportexport',
            tooltip: "Export Report", //"Export Report details.",  
            menuItem:{
                csv:true,
                pdf:true,
                xls:true
            },
            get:Wtf.autoNum.StockQAReport,
            label:"Export"

        });
        
        var bbarArray= new Array();
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.qa, Wtf.Perm.qa.viewqa)&& Wtf.account.companyAccountPref.activateQAApprovalFlow) {
            bbarArray.push(this.exportButton);
        }


        this.summary = new Wtf.grid.GroupSummary({});
        
        this.grid=new Wtf.KwlEditorGridPanel({
//            id:"StockoutApproval"+this.id,
            cm:this.cm,
            store:this.ds,
            loadMask:true,
            // stripeRows : true,
            //            viewConfig: {
            //                forceFit: true
            //            },
            //view: grpView,
            //plugins:[this.expander,this.summary],
            tbar:tbarArray,
            bbar : bbarArray,
            viewConfig: {
                forceFit: false
            },
            searchLabel:WtfGlobal.getLocaleText("acc.dnList.searchText"),
            searchLabelSeparator:":",
            searchEmptyText: WtfGlobal.getLocaleText("acc.inventory.QAAproval.TransactionNoProductCode"),
            serverSideSearch:true,
            qsWidth:90,
            //            noSearch:true,
            //            searchField:"productCode",
            //            clicksToEdit:1,
            displayInfo: true,
            displayMsg: 'Displaying  {0} - {1} of {2}',
            emptyMsg: "No results to display"
        //        
        });
        this.grid.on("cellclick",this.cellClick,this);
        
        Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
            Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
        },this);
        

       
        this.add(this.grid);
        
        //        this.loadgrid();
        
        this.on("activate",function(){
            this.loadgridstore(this.frmDate.getValue(),this.toDate.getValue(),this.storeCmbfilter.getValue(),this.typecmb.getValue(),this.status);
        },this);
    },
    //    loadgrid : function(){
    //     
    //        this.ds.load({
    //            params:{
    //                start:0,
    //                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
    //                ss:  Wtf.getCmp("Quick"+this.grid.id).getValue()//30,//Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
    //       
    //            }
    //        });
    //    
    //    },
    initloadgridstore:function(frm, to,storeid,type,status){
        this.ds.baseParams = {
            type:type,
            status:status,
            frmDate:frm.format('Y-m-d'),
            toDate:to.format('Y-m-d'),
            storeid:storeid,
             isisJobWorkOrderInQA : this.isisJobWorkOrderInQA
        }
        this.ds.load({
            params:{
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
                //                limit:30,
                ss:  Wtf.getCmp("Quick"+this.grid.id).getValue(),
                 isisJobWorkOrderInQA : this.isisJobWorkOrderInQA
            }
        });
    },
    loadgridstore:function(frm, to,storeid,type,status){
        this.ds.baseParams = {
            type:type,
            status:status,
            frmDate:frm.format('Y-m-d'),
            toDate:to.format('Y-m-d'),
            storeid:storeid,
             isisJobWorkOrderInQA : this.isisJobWorkOrderInQA
        }
        this.ds.load({
            params:{
                start:0,
                //                  limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
                limit:30,
                ss:  Wtf.getCmp("Quick"+this.grid.id).getValue(),
                 isisJobWorkOrderInQA : this.isisJobWorkOrderInQA
            }
        });
    },
    serialRenderer:function(v,m,rec){
        if(rec.get('status') == 'PENDING'){
            //                        return "<div  wtf:qtip='Show Details', wtf:qtitle='Show Details' class='x-btn-text pwnd printButtonIcon'></div>";
            return "<a href='#'>Details</a>";
        }else{
            //            return "<div  wtf:qtip='Show Details', wtf:qtitle='Show Details' class='pwnd printButtonIcon'></div>";
            return "<a href='#'>Details</a>";
        }
    },
    cellClick :function(grid, rowIndex, columnIndex, e){
        
        var record = grid.getStore().getAt(rowIndex);  // Get the Record
        var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
        //        var itemId=record.get("productid");
        //        var itemCode=record.get("pid");
        //        var quantity=record.get("quantity");
        //        var UOMName=record.get("uomname");
        //        var fromStoreId = this.parent.fromstoreCombo.getValue();
        
        if(fieldName != undefined && fieldName == "type"){
            StockoutQADetail(record,this.module,this.isJobWorkOrder);
        //            if(fromStoreId == undefined || fromStoreId == ""){
        //                WtfComMsgBox(["Warning", "Please Select Store."],0);
        //                return false;
        //            }
          
        }
    }
    
});







function StockoutQADetail(record,module,isJobWorkOrder){
    var demo=Wtf.getCmp("StockoutQADetailCmp"+record.data.id)
    var main=Wtf.getCmp("as");
    if(demo==null){
        demo =new Wtf.StockoutApprovalDetail({
            id:"StockoutQADetailCmp"+record.data.id,
            layout:'fit',
            rec:record,
            module:module,
            isJobWorkOrder:isJobWorkOrder,
            title:record.get("transactionno")+"-Details",
            closable:true,
            border:false
        })
        main.add(demo);
    }
    main.setActiveTab(demo);
    main.doLayout();
}

Wtf.StockoutApprovalDetail = function(config){
    this.isJobWorkOrder= config.isJobWorkOrder;
    Wtf.StockoutApprovalDetail.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.StockoutApprovalDetail, Wtf.Panel, {
    onRender: function(config) {
        Wtf.StockoutApprovalDetail.superclass.onRender.call(this, config);

        //        this.status = 0;
       
       
        this.record = Wtf.data.Record.create([
        {
            "name":"id"
        },
        {
            "name":"billid"
        },
        {
            "name":"transactionmodule"
        },
        {
            "name":"quantity"
        },
        {
            "name":"actualquantity"
        },
        {
            "name":"returnedqty"
        },
                
        {
            "name":"locationname"
        },
        {
            "name":"locationid"
        },
        {
            "name":"isBatchForProduct"
        },
        {
            "name":"isSerialForProduct"
        },
        {
            "name":"serialname"
        },
        {
            "name":"batchname"
        },
        {
            "name":"status"
        },
        {
            "name":"inspection"
        },{
            "name":"supervisor"
        },
        {
            "name":"modifiedon"
        },
        {
            "name":"inspectionTemplate"
        },
        {
            "name":"productname"
        },
        {
            "name":"isreusable"
        },
        {
            "name":"reusablecount"
        },
        {
            "name":"transactionno"
        },
        {
            "name":"storename"
        },
        {
            "name":"moduletype"
        },
        {
            "name":"productcode"
        },
        {
            "name":"leadtime"
        },
        {
            "name":'attachment'
        },
        {
            "name":'customer'
        },
        {
            "name":'remark'
        },
        {
            "name":'approvepermissioncount'
        },
        {
            "name":"productdescription"
        },
        {
            "name":"repairstore"
        },
        {
            "name":"repairstorelocation"
        }
        ]);
        
        
      
        this.quantityTextField = new Wtf.form.NumberField({
            maxLength:10,
            allowBlank:false,
            decimalPrecision:4,
            allowNegative: false
        // allowNegative: false//(Wtf.realroles[0]==9)?true:false,    // For FM markout amend process
        //            renderer:function(val){
        //                if(!val)
        //                    return 0;
        //                return val;
        //            }
        });
        this.ds = new Wtf.data.Store({
            url: 'INVApproval/getAllQAApprovalDetailList.do',
            //            url: 'INVApproval/getConsignmentApprovalDetailList.do',
            params: {
                moduleid:this.rec.data["moduletype"]
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },
            this.record
            )
        });
        
      
        this.ds.on("load",function(store,rec,opt){
            this.grid.getView().refresh();
        },this);

        this.ds.on("beforeload",function(){
            this.ds.baseParams = {
                saId:this.rec.get('id'),
                moduleid:this.rec.data["moduletype"]
            //                toDate: this.toDate.getValue().format('Y-m-d'),
            //                storeid: this.storeCmbfilter.getValue(),
            //                markouttype: "markout",
            //                //                summaryFlag: this.status == 1,
            //                dmflag:this.dmflag
            }
        },
        this);
        //        this.ds.load();
        var integrationFeatureFor=true;
        
        this.sm = new Wtf.grid.CheckboxSelectionModel({});
        var cmDefaultWidth = 120;
        this.cm = new Wtf.grid.ColumnModel([
            new Wtf.KWLRowNumberer(), 
            this.sm,
            //            this.expander, //1
            {
                header: "Transaction No", //2
                dataIndex: 'transactionno',
                groupable: false,
                width:cmDefaultWidth,
                hidden:true,
                fixed:true
            },
            {
                header: "Store", //2
                dataIndex: 'storename',
                groupable: false,
                width:cmDefaultWidth,
                fixed:true
            },
            {
                header: "Location", //2
                dataIndex: 'locationname',
                groupable: false,
                width:cmDefaultWidth,
                hidden:false,
                fixed:true
            },
            {
                header: "Product ID", //2
                dataIndex: 'productcode',
                groupable: false,
                width:cmDefaultWidth,
                fixed:true
            },
            {
                header: "Product Name", //2
                dataIndex: 'productname',
                groupable: false,
                width:cmDefaultWidth,
                fixed:true
            },
            {
                header: "Product Description",     
                dataIndex: 'productdescription',
                groupable: false,
                width:cmDefaultWidth,
                fixed:true
            },
            {
                header: (Wtf.jobWorkInFlowFlag!= undefined && Wtf.jobWorkInFlowFlag==true && this.isJobWorkOrder==true && this.isJobWorkOrder!= undefined )? "Challan No":"Batch",//3
                dataIndex: 'batchname',
                width:cmDefaultWidth,
                fixed:true
            },
          
            {
                header: "Serial",//7
                dataIndex: 'serialname',
                groupable: true,
                width:cmDefaultWidth
            },
            {
                header: "Inspection Status",//8
                dataIndex: 'status',
                groupable: true,
                width:cmDefaultWidth,
                renderer:function(value){
                    if(value=="APPROVED"){
                        return "<label style = 'color : green;'>APPROVED</label>";
                    }else if(value=="REJECTED"){
                        return "<label style = 'color : red;'>REJECTED</label>";
                    }else{
                        return "PENDING";
                    }
                }
            },{
                header: "Inspected By ",//19
                dataIndex:'supervisor',
                width:80,
                hidden:false
            }, 
            {
                header: "Inspected On",
                dataIndex:'modifiedon',
                width:80,
                hidden:false
            },{
                header:"Quantity",
                dataIndex:"quantity",
                align: 'right',
                editor:this.quantityTextField,
                renderer : function(v) {
                    return '<div align="right">' + parseFloat(v).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + '</div>';
                }
            },{
                header: "Used Count",//15
                dataIndex: 'reusablecount',
                sortable:false,
                align: 'right',
                width:100,
                editable:true,
                hidden:true,
                editor: new Wtf.form.NumberField({
                    //allowBlank: false,
                    allowNegative: false,
                    allowDecimals: true,
                    selectOnFocus: true
                })
            },
            {
                header: "QA Lead Time",//15
                dataIndex: 'leadtime',
                width:100,
                align: 'right',
                hidden:false
            },
            {
                header: "Inspection Form",//19
                dataIndex: 'inspection',
                hidden:false,
                align:'center',
                width:120,
                renderer: this.serialRenderer.createDelegate(this)
            },
            {
                header: "Inspection Details",//19
                dataIndex:'attachment',
                width:80,
                hidden:false,
                renderer : this.DownloadLink.createDelegate(this)
            },
            {
                header: "Remark ",//19
                dataIndex:'remark',
                width:80,
                hidden:false
            }
            ]);
        // this.cm.defaultSortable = true;
        

        /**************date *****/
        this.approveButton = new Wtf.Button({
            text: 'Approve',
            scope: this,
            hidden:this.type==2?true:false,
            disabled: true,
            tooltip: {
               
                text:"Approve selected items"
            },
            //            hidden:((Wtf.realroles[0]==14 && isIncludeQAapprovalFlow)||(Wtf.realroles[0]==27 && isIncludeSVapprovalFlow))?false:true, // Only if QA user and QA approval flow or SuperVisor is included then only see this button
            handler:function(){
               
                Wtf.MessageBox.confirm("Confirm","Are you sure you want to Approve this QA Inspection request?", function(btn){
                    if(btn == 'yes') {  
                        this.btnStatus="approved";
                        this.docuploadhandler();
                        
                    }else if(btn == 'no') {
                        return;           
                    }
                },this);
            }      
        });		
        
        this.remark="";
        this.extraemailids ="";
        
        this.rejectButton = new Wtf.Button({
            text: 'Reject',
            scope: this,
            disabled: true,
            hidden:this.type==2?true:false,
            tooltip: {
                
                text:"Reject selected items"
            },
            //            hidden:((Wtf.realroles[0]==14 && isIncludeQAapprovalFlow)||(Wtf.realroles[0]==27 && isIncludeSVapprovalFlow))?false:true, // Only if QA user and QA approval flow is included then only see this button
            handler:function(){
                
                //ERM-691 Displaying Repair Store Selection Window during QA Stock rejecting for GRN
                if (this.rec.data.moduletype == 'goodsreceipt') {
                    this.repairstorewindow = this.showrepairstorewindow();
                    if (this.repairstorewindow != undefined) {
                            this.repairstorewindow.show();
                    }
                } else {
                    Wtf.MessageBox.confirm("Confirm", "Are you sure you want to Reject this QA Inspection request?", function (btn) {
                        if (btn == 'yes') {
                            this.btnStatus = "rejected";
                            this.docuploadhandler();
                        } else if (btn == 'no') {
                            return;
                        }
                    }, this);
                }
                if (this.repairstorewindow !== undefined) {
                    this.repairstorewindow.buttons[0].on('click', function (obj) { //after save button of repair store window is clicked
                        if (obj !== undefined) {
                            Wtf.MessageBox.confirm("Confirm", "Are you sure you want to Reject this QA Inspection request?", function (btn) {
                                if (btn == 'yes') {
                                    this.btnStatus = "rejected";
                                    this.docuploadhandler();
                                } else if (btn == 'no') {
                                    this.repairstorewindow.close();
                                }
                            }, this);
                        }
                    }, this);
                }
            }
        });
        
       
        var bbarArray= new Array();
        
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.qa, Wtf.Perm.qa.approverejectqa)) {
            bbarArray.push("-",this.approveButton);
            bbarArray.push("-",this.rejectButton);
        }
        //Print Record(s) button for Document Designer templates
        this.singleRowPrint=new Wtf.exportButton({
            obj:this,
            id:"printSingleRecord"+config.helpmodeid+config.id,
            iconCls: 'pwnd printButtonIcon',
            text: WtfGlobal.getLocaleText("acc.rem.236"),
            tooltip :WtfGlobal.getLocaleText("acc.rem.236.single"),  //'Print Single Record Details',
            disabled :true,
            hidden:this.isSalesCommissionStmt,
            menuItem:{rowPrint:(this.isSalesCommissionStmt)?false:true},
            get:Wtf.autoNum.QAApprovalReport,
            moduleid:Wtf.autoNum.QAApprovalReport
        });
        bbarArray.push("-", this.singleRowPrint);
        
        
        this.grid=new Wtf.KwlEditorGridPanel({
            //            id:"StockoutApprovalDetail"+this.id,
            cm:this.cm,
            sm:this.sm,
            store:this.ds,
            loadMask:true,
            //            tbar:tbarArray,
            viewConfig: {
                forceFit: false
            },
            //            searchLabel:"Quick Search",
            //            searchLabelSeparator:":",
            //            searchEmptyText: "Search by Product ID, Product Name",
            //            serverSideSearch:true,
            //            searchField:"productCode",
            clicksToEdit:1,
            noSearch:true,
            displayInfo: true,
            displayMsg: 'Displaying  {0} - {1} of {2}',
            emptyMsg: "No results to display",
            //            serverSideSearch:false,
            bbar:bbarArray
      
        });
        this.grid.on("cellclick",this.cellClick1,this);
        this.grid.on("afteredit", this.validateGridEdit, this);
        this.grid.on("beforeedit",this.gridbeforeEdit,this);
        
        Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
            Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
        },this);
        

        this.add(this.grid);
        this.loadgrid();
        this.sm.on("selectionchange",function(){
            var selected = this.sm.getSelections();
            //            if(selected.length>0 && selected.length === 1){
            //                if(selected[0].data.status === "PENDING"){
            //                    this.approveButton.enable();
            //                    this.rejectButton.enable();
            //                }else{
            //                    this.approveButton.disable();
            //                    this.rejectButton.disable();
            //                }
            //            }else{
            //                this.approveButton.disable();
            //                this.rejectButton.disable();
            //            }

            if(selected.length>0){
                if(this.singleRowPrint){
                    this.singleRowPrint.enable();
                }
                var firstStatus=selected[0].data.status;
                var isAllStatusSame=false;
                
                for(var i=0; i<selected.length ; i++){
                    if(selected[i].data.status === firstStatus){
                        isAllStatusSame = true;
                    }else{
                        isAllStatusSame = false; 
                    }
                }
                
                if(selected[0].data.status === "PENDING" && selected[0].data.approvepermissioncount!=undefined && selected[0].data.approvepermissioncount > 0 && isAllStatusSame == true){
                    this.approveButton.enable();
                    this.rejectButton.enable();
                }else{
                    this.approveButton.disable();
                    this.rejectButton.disable();
                } 
                
            }else{
                if(this.singleRowPrint){
                    this.singleRowPrint.disable();
                }
                this.approveButton.disable();
                this.rejectButton.disable();
            }
        },this)
        
    },
    gridbeforeEdit :function(e){
        
        var rec=e.record;
        
        if(e.record.data.isSerialForProduct == true && (e.field =='quantity')) {
            return false;
        }
        
    },
        
    loadgrid : function(){
     
        this.ds.load({
            params:{
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize//30,//Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
       
            }
        });
    
    },
    serialRenderer:function(v,m,rec){
        if(rec.get('status') == 'PENDING'){
            //                        return "<div  wtf:qtip='Show Details', wtf:qtitle='Show Details' class='x-btn-text pwnd printButtonIcon'></div>";
            return "<a href='#'>Print</a>";
        }else{
            //            return "<div  wtf:qtip='Show Details', wtf:qtitle='Show Details' class='pwnd printButtonIcon'></div>";
            return "<a href='#'>Print</a>";
        }
    },
    cellClick1 :function(grid, rowIndex, columnIndex, e){
        
        var record = grid.getStore().getAt(rowIndex);  // Get the Record
        var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
        //        var itemId=record.get("productid");
        //        var itemCode=record.get("pid");
        //        var quantity=record.get("quantity");
        //        var UOMName=record.get("uomname");
        //        var fromStoreId = this.parent.fromstoreCombo.getValue();
        
        //        if(fieldName != undefined && fieldName == "type"){
        //            alert("view form");
        //            if(fromStoreId == undefined || fromStoreId == ""){
        //                WtfComMsgBox(["Warning", "Please Select Store."],0);
        //                return false;
        //            }

        //        }
        if(fieldName != undefined && fieldName == "inspection"){
            if(record.data.status == "REJECTED" && record.data.moduletype == "deliveryorder"){
                inspectionTab(record,"interstore",this.ds,this.rec.get('id'),this.isJobWorkOrder);
            } else{
                var config = {'isStockOutApproval': true};
                inspectionTab(record,"stockout",this.ds,this.rec.get('id'),this.isJobWorkOrder,config);
            }
        //            this.ds.load();
        }
    },
    validateGridEdit:function(e){
        //        for(var i=0;i<this.ds.getCount();i++){
        //            if(this.ds.getAt(e.row).get("productcode") == e.value){
        if(parseInt(this.ds.getAt(e.row).get("quantity"))>parseInt(this.ds.getAt(e.row).get("actualquantity"))){
            this.ds.getAt(e.row).set("quantity",this.ds.getAt(e.row).get("actualquantity"));
            WtfComMsgBox(["Warning", "Entered quantity can not be greater than actual quantity."],0);
            this.ds.getAt(e.row).set("quantity",this.ds.getAt(e.row).get("actualquantity"));
            return false;
        } else if (parseInt(this.ds.getAt(e.row).get("quantity"))==0) {
            WtfComMsgBox(["Warning", "Entered quantity can not be zero."],0);
            this.ds.getAt(e.row).set("quantity",this.ds.getAt(e.row).get("actualquantity"));
            return false;
        }
    //            }
    //        }
    },

    DownloadLink : function(a, b, c, d, e, f) {        
        var msg = "";
        var url = "INVCommon/getAttachedDocuments.do?modulename=QA_INSPECTION_APPROVAL";            
        if (c.data['attachment']!=0)
            msg ='('+c.data['attachment']+')'+'<div class = "pwnd downloadDoc" wtf:qtitle="'
            + WtfGlobal.getLocaleText("acc.invoiceList.attachments")
            + '" wtf:qtip="'
            + WtfGlobal.getLocaleText("acc.invoiceList.clickToDownloadAttachments")
            + '" onclick="displayInvDocList(\''
            + c.data['id']
            + '\',\''
            + url
            + '\',\''
            + 'invoiceGridId'
            + this.id
            + '\', event,\''
            + ""
            + '\',\''
            + ""
            + '\',\''
            + false
            + '\',\''
            + 0
            + '\',\''
            + 0
            + '\',\''
            + ""
            + '\')" style="width: 16px; height: 16px;cursor:pointer; margin-left: 16px; margin-top: -15px;" id=\''
            + c.data['leaveid'] + '\'>&nbsp;</div>';
        //else
        //  msg = "";
        return msg;
    },
    showrepairstorewindow:function(){
        //ERM-691 for displaying a window regarding the repair store passing default store and location as well
        var records = this.grid.getSelections();
        var defaultstore = "";var defaultloc ="";
        if(records!==undefined && records.length>0){
            var storerec = this.grid.store.getAt(0);
            defaultstore = storerec.data.repairstore;//default store and location being passed
            defaultloc = storerec.data.repairstorelocation;
        }
        var qty = 0;
        if (records.length > 1) {
            for (var i = 0; i < records.length; i++) {
                qty += records[i].data.quantity;
            }
        } else {
            qty = records[0].data.quantity;
        }
        this.qaflowwindow=new Wtf.account.SerialNoWindow({
                    renderTo: document.body,
                    title:WtfGlobal.getLocaleText("acc.qaapproval.repairstoretitle"),
                    productName:records[0].data.productname,
                    noteType:WtfGlobal.getLocaleText("acc.store.multiplerepairstore"),
                    quantity:qty,
                    isForRepairStoreOnly:true, //display the repair stores only
                    includeQAAndRepairStore:true,
                    documentid:records[0].data.id,
                    defaultWarehouse:defaultstore,
                    defaultLocation:defaultloc,
                    isLocationForProduct:true,
                    isWarehouseForProduct:true,
                    isEdit:false,
                    width:750,
                    resizable : false,
                    modal: true,
                    parentGrid:this
                });
        return this.qaflowwindow;
    },
    docuploadhandler : function(e, t) {
        //            if (e.target.className != "pwndbar1 uploadDoc")
        //                return;
        var selected = this.sm.getSelections();            
        //            if (this.grid.flag == 0) {
        this.fileuploadwin = new Wtf.form.FormPanel(
        {                   
            url : "INVCommon/attachDocuments.do",
            waitMsgTarget : true,
            fileUpload : true,
            method : 'POST',
            border : false,
            scope : this,
            // layout:'fit',
            bodyStyle : 'background-color:#f1f1f1;font-size:10px;padding:10px 15px;',
            lableWidth : 50,
            items : [
            this.sendInvoiceId = new Wtf.form.Hidden(
            {
                name : 'modulewisemainid'
            }),
            this.uploadModulename = new Wtf.form.Hidden(
            {
                name : 'modulename'
            }),
            this.tName = new Wtf.form.TextField(
            {
                fieldLabel : WtfGlobal.getLocaleText("acc.invoiceList.filePath") ,
                //allowBlank : false,
                name : 'file',
                inputType : 'file',
                width : 200,
                //emptyText:"Select file to upload..",
                blankText:WtfGlobal.getLocaleText("acc.field.SelectFileFirst"),
                allowBlank:true,
                msgTarget :'qtip'
            }), 
            this.remark = new Wtf.form.TextArea({
                fieldLabel:"Remark",
                readOnly: false,
                name: "remark",
                width: 200,
                maxLength:250
            }),
            this.extraemailids = new Wtf.form.TextArea({
                fieldLabel:"Email IDs",
                hidden: !(this.btnStatus === "rejected"),
                hideLabel:!(this.btnStatus === "rejected"),
                name: "extraemailids",
                emptyText:"Provide email-ids to send mail after rejection separated by ','\nEg. abc@test.com , xyz@test.com",
                width: 200
            })
            //            this.reusableCount = new Wtf.form.NumberField({
            //                fieldLabel:"Used Count",
            //                allowNegative: false,
            //                allowDecimals: false,
            //                hidden: !(selected[0].get("isreusable") === "REUSABLE"),
            //                hideLabel:!(selected[0].get("isreusable") === "REUSABLE"),
            //                name: "reusablecount",
            //                width: 200
            //            })
            ]
        });
        
        this.upwin = new Wtf.Window(
        {
            id : 'upfilewin',
            title : WtfGlobal
            .getLocaleText("acc.invoiceList.uploadfile"),
            closable : true,
            width : 400,
            height : 250,
            plain : true,
            iconCls : 'iconwin',
            resizable : true,
            layout : 'fit',
            scope : this,
            listeners : {
                scope : this,

                close : function() {
                    thisclk = 1;
                        scope: this;
                    this.fileuploadwin.destroy();
                    this.grid.flag = 0
                //                              this.upwin.close();
                }
            },
            items : this.fileuploadwin,
            buttons : [
            {
                anchor : '90%',
                id : 'save',
                text : WtfGlobal.getLocaleText("acc.het.108"),
                scope : this,
                handler : this.upfileHandler
            },
            {
                anchor : '90%',
                id : 'close',
                text : WtfGlobal
                .getLocaleText("acc.invoiceList.bt.cancel"),
                handler : this.close1,
                scope : this
            } ]

        });
        
        var isFirst=true;
        var moduleWiseMainIdList="";
        for(var j=0 ; j < selected.length ; j++){
            if(isFirst){
                moduleWiseMainIdList = selected[j].get('id');
                isFirst=false;
            }else{
                moduleWiseMainIdList += ","+ selected[j].get('id');
            }
        }
        
        this.sendInvoiceId.setValue(moduleWiseMainIdList);
        this.uploadModulename.setValue("QA_INSPECTION_APPROVAL");
        this.upwin.show();
    //                this.grid.flag = 1;
    },
    approveReject:function(operation){
        var jsondata = "";
        var loadingMask = new Wtf.LoadMask(document.body,{
            msg : WtfGlobal.getLocaleText("acc.msgbox.50")
        });
        
        loadingMask.show();
        var repairstoreid = "";
        var repairlocationid= "";
        if (this.repairstorewindow !== undefined) {  //ERP-39355 QA reject window selects default store in GRN QA flow despite selecting a different store 
            var repairstore = this.repairstorewindow.grid.store;
            var repairwindowrecords = repairstore !== undefined ? repairstore.getAt(0) : "";
            if (repairwindowrecords !== "" && repairwindowrecords.data !== undefined) {
                repairstoreid = repairwindowrecords.data.warehouse;
                repairlocationid = repairwindowrecords.data.location;
            }
        }
        var selected = this.sm.getSelections();
        var isFirst=true;
        var saIdsList="";
        var dataArr=new Array();
        for(var i=0;i<selected.length;i++){            
            if(isFirst == true){
                saIdsList += selected[i].get('id');
                isFirst=false;
            }else{
                saIdsList += ","+selected[i].get('id');
            }
            var jObject={};
            var jArray=[];
            jObject.recordid =selected[i].get('id');
            jObject.quantity =selected[i].get('quantity');
            jObject.serialname =selected[i].get('serialname');
            dataArr.push(jObject);
        }
        var trmLen = jsondata.length - 1;
        var finalStr = JSON.stringify(dataArr);
        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url:"INVApproval/saveInspectionData1.do",
            params: {
                saDetailApprovalId:saIdsList,
                jsondata:finalStr,
                operation:operation,
                moduleid:selected[0].get('moduletype'),
                repairStoreId:repairstoreid,
                repairLocationId:repairlocationid,
                transactionno : selected[0].get('transactionno'),
                moduleApprovalId:this.rec.get('id'),
                remark:this.remark.getValue(),
                //                reusableCount:this.reusableCount.getValue(),   
                extraEmail:this.extraemailids.getValue()
            }
        },
        this,
        function(result, req){
            loadingMask.hide();
            var msg=result.msg;
            if(selected[0].get('moduletype')=='deliveryorder' || selected[0].get('moduletype')=='goodsreceipt' || selected[0].get('moduletype')=='Work Order'){
              var istid=result.istid ? result.istid : ''; //getting id in case of good receipt or delivery order
              if(istid!='' && istid!=undefined)
                {
                    this.sendInvoiceId.setValue(istid);
                }
                var title="Success";
                if(result.success){
                    if (this.fileuploadwin.form.isValid()) {
                        Wtf.getCmp('save').disabled = true;
                    }
                    //var selected = this.sm.getSelections();
                    // if (selected[0].get('doccnt') < 3) {
                    if (this.fileuploadwin.form.isValid()) {
                        this.fileuploadwin.form.submit({
                            scope : this,
                            success : function(frm, action) {
                                this.upwin.close();
                                WtfComMsgBox([title,msg],0);
                                this.loadgrid();                                                            
                                
                            },
                            failure : function(frm, action) {
                                this.upwin.close();
                                WtfComMsgBox([title,msg],0);
                                this.loadgrid();
                            //                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), "File not uploaded! File should not be empty.");
                            }
                           
                        });
                        
                    }
                   
                } 
                 else if(result.success==false){
                    title="Warning";
                    WtfComMsgBox([title,msg],2);
                    return false;
                }
            }
            else{
            if(result.success){
                    WtfComMsgBox([title,msg],0);
                    this.loadgrid();
                    
               }
            else if(result.success==false){
                title="Warning";
                WtfComMsgBox([title,msg],2);
                return false;
            }
            }
        },
        function(result, req){
            loadingMask.hide();
            var msg=result.msg;
            WtfComMsgBox(["Failure","Error occured while processing the data."],3);
            //            this.inspectionwin.close();  
            return false;
        });
        WtfGlobal.resetAjaxTimeOut();
    },
    close1 : function() {
        Wtf.getCmp('upfilewin').close();
        this.grid.flag = 0;
    },

    upfileHandler : function() {
         var selected = this.sm.getSelections();
         if(selected[0].data.moduletype!="deliveryorder" && selected[0].data.moduletype!="goodsreceipt" && selected[0].data.moduletype!="Work Order"){
        if (this.fileuploadwin.form.isValid()) {
            Wtf.getCmp('save').disabled = true;
        }
        //var selected = this.sm.getSelections();
        // if (selected[0].get('doccnt') < 3) {
        if (this.fileuploadwin.form.isValid()) {
            this.fileuploadwin.form.submit({
                scope : this,
                success : function(frm, action) {
                    this.upwin.close();
                },
                failure : function(frm, action) {
                    this.upwin.close();
                //                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), "File not uploaded! File should not be empty.");
                }
            });
            
         if(this.btnStatus == 'approved'){
                this.approveReject("Approve");
            }
            else if(this.btnStatus == 'rejected'){
                this.approveReject("Reject"); 
            } 
        }
     
    }
        else{
            if(this.btnStatus == 'approved'){
                this.approveReject("Approve");
            }
            else if(this.btnStatus == 'rejected'){
                this.approveReject("Reject"); 
            } 
        }
    }
});


function displayInvDocList(id, url, gridid, event,creatorid,approverid,docReq,cnt,statusid,showleaves,reportGridId) {        
   
    if(Wtf.getCmp('DocListWindow'))
        Wtf.getCmp("DocListWindow").destroy();
    new Wtf.DocListWindow({
        wizard:false,
        closeAction : 'hide',
        layout: 'fit',
        title:WtfGlobal.getLocaleText("acc.invoiceList.attachments"),
        shadow:false,
        bodyStyle: "background-color: white",
        closable: true,
        width : 250,
        heigth:250,
        url: url,
        gridid: gridid,
        modal:true,
        autoScroll:true,
        recid:id,
        delurl: "INVCommon/deleteDocument.do?docid=",
        id:"DocListWindow",
        docCount:cnt,
        isDocReq:docReq,  
        statusID:statusid,  
        showleaves:showleaves,  
        dispto:"pmtabpanel",
        reportGridId:reportGridId    //ERP-13011 [SJ]
    });

    var docListWin = Wtf.getCmp("DocListWindow");
    var leftoffset =event.pageX-200;

    var topoffset = event.pageY+10;
    if (document.all) {
        xMousePos = window.event.x+document.body.scrollLeft;
        yMousePos = window.event.y+document.body.scrollTop;
        xMousePosMax = document.body.clientWidth+document.body.scrollLeft;
        yMousePosMax = document.body.clientHeight+document.body.scrollTop;
        leftoffset=xMousePos-200;//xMousePos;
        topoffset=yMousePos+120;//yMousePos;
        
    }
    if(docListWin.innerpanel==null||docListWin.hidden==true){
        docListWin.setPosition(leftoffset, topoffset);

        docListWin.show();
    }
    else{
        docListWin.hide();

    }
}
    

//QA Approval Report

Wtf.QAApprovalReport = function(config){
    Wtf.QAApprovalReport.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.QAApprovalReport, Wtf.Panel, {
    onRender: function(config) {
        Wtf.QAApprovalReport.superclass.onRender.call(this, config);
        
        this.fromdateVal =new Date().getFirstDateOfMonth();
        this.fromdateVal.setDate(new Date().getFirstDateOfMonth().getDate());
        this.frmDate = new Wtf.form.DateField({
            emptyText:'From date...',
            readOnly:true,
            width : 90,
            value:WtfGlobal.getDates(true),
            minValue: Wtf.archivalDate,
            name : 'frmdate',
            format: 'Y-m-d'
        });
        this.todateVal=new Date().getLastDateOfMonth();
        this.todateVal.setDate(new Date().getLastDateOfMonth().getDate());
        this.toDate = new Wtf.form.DateField({
            emptyText:'To date...',
            readOnly:true,
            width : 90,
            name : 'todate',
            value:WtfGlobal.getDates(false),
            minValue: Wtf.archivalDate,
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
        },
        {
            name : 'storetypename'
        }
        ]);

        this.storeCmbStore = new Wtf.data.Store({
            url:  'INVStore/getStoreList.do',
            baseParams:{
                byStoreManager:"true",
                byStoreExecutive:"true",
                includeQAAndRepairStore:true
            },
            //            sortInfo: {
            //                field: 'fullname',
            //                direction: 'ASC' // or 'DESC' (case sensitive for local sorting)
            //            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.storeCmbRecord)
        });
        
        this.storeCmbStore.on("load", function(ds, rec, o){
            if(rec.length > 1){
                var newRec=new this.storeCmbRecord({
                    store_id:'',
                    fullname:'ALL'
                })
                this.storeCmbStore.insert(0,newRec);
                this.storeCmbfilter.setValue('');
            }else if(rec.length > 0){
                this.storeCmbfilter.setValue(rec[0].data.store_id, true);
            }
        }, this);
            
        this.storeCmbfilter = new Wtf.form.ComboBox({
            fieldLabel : WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore")+"*",
            hiddenName : 'store',
            store : this.storeCmbStore,
            forceSelection:true,
            displayField:'fullname',
            valueField:'store_id',
            mode: 'local',
            width : 120,
            listWidth:200, 
            triggerAction: 'all',
            emptyText:'Select store...',
            typeAhead:true,
            listWidth:300,
            tpl: new Wtf.XTemplate(
                '<tpl for=".">',
                '<div wtf:qtip = "{[values.fullname]}" class="x-combo-list-item">',
                '<div>{fullname}</div>',
                '</div>',
                '</tpl>')
        });
        
        var moduleData = [];
        if(Wtf.account.companyAccountPref.activateInventoryTab){
            moduleData.push(["ALL", "ALL"],["STOCK ADJUSTMENT", "Stock Adjustment"],["STOCK REQUEST", "Stock Request"],["INTER STORE TRANSFER", "Inter Store Transfer"])
        }
        if(Wtf.account.companyAccountPref.consignmentSalesManagementFlag){
            moduleData.push(["CONSIGNMENT", "Consignment"])
        }
         if(Wtf.account.companyAccountPref.BuildAssemblyApprovalFlow){
            moduleData.push(["BuildAssemblyQA", "Build Assembly"]);
        }
        moduleData.push(["GOOD RECEIPT", "Good Receipt Note"]);
        if (Wtf.account.companyAccountPref.isQaApprovalFlowInDO) {
            moduleData.push(["DELIVERY ORDER", "Delivery Order"]);
        }
        
        if (Wtf.account.companyAccountPref.activateMRPManagementFlag && Wtf.account.companyAccountPref.columnPref.isQaApprovalFlowInMRP) {
            moduleData.push(["Work Order", "Work Order"]);
        } 
        
        this.moduletype = new Wtf.data.SimpleStore({
            fields:["id", "name"],
            data : moduleData
        });
        this.typecmb = new Wtf.form.ComboBox({
            hiddenName : 'statusFilter',
            store : this.moduletype,
            typeAhead:true,
            readOnly: false,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 110,
            triggerAction: 'all',
            emptyText:'Select module...'
        });  
        
        this.moduletype.on('load', function(){
            if(this.moduletype.getCount() > 0){
                this.typecmb.setValue(this.moduletype.getAt(0).get('id'));
            }
        }, this)
        
        this.moduletype.loadData(moduleData);
        
        this.repairStatusStore = new Wtf.data.SimpleStore({
            fields: ["id", "name"],
            data: [["", "ALL"],["REPAIRDONE", "Done"], ["REPAIRREJECT", "Stockout"]]
        });
        this.repairStatusCmb = new Wtf.form.ComboBox({
            hiddenName: 'statusFilter',
            store: this.repairStatusStore,
            typeAhead: true,
            readOnly: false,
            displayField: 'name',
            valueField: 'id',
            value: "",
            mode: 'local',
            width: 120,
            triggerAction: 'all',
            emptyText: 'select Status...'
        });
        
        this.search = new Wtf.Button({
            anchor: '90%',
            text: WtfGlobal.getLocaleText("acc.het.163"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.inventory.QAAproval.ClicktoSearchOrders")
            },
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: function() {
                this.loadgrid(this.frmDate.getValue(),this.toDate.getValue(),this.storeCmbfilter.getValue(),this.repairStatusCmb.getValue());
            }
        });
        
        this.resetBtn = new Wtf.Button({
            anchor : '90%',
            text: WtfGlobal.getLocaleText("acc.inventory.QAAproval.ResetFilter"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.inventory.QAAproval.Clickheretoresetfilter")
            },
            iconCls:getButtonIconCls(Wtf.etype.resetbutton),
            scope:this,
            handler:function(){
                this.storeCmbfilter.setValue(this.storeCmbfilter.store.data.items[0].data.store_id);
                this.repairStatusCmb.setValue("");
                this.frmDate.reset();
                this.toDate.reset();
                this.grid.quickSearchTF.setValue("");
                if(this.typecmb.store.getCount() > 0){
                    this.typecmb.setValue(this.typecmb.store.getAt(0).get('id'));
                }else{
                    this.typecmb.setValue("");
                }
                this.loadgrid(this.frmDate.getValue(),this.toDate.getValue(),this.storeCmbfilter.getValue(),this.repairStatusCmb.getValue(),this.status);
            }
        });
        this.quantityTextField = new Wtf.form.NumberField({
            maxLength:10,
            allowBlank:false,
            decimalPrecision:4,
            allowNegative: false,
            editable:this.type==1?true:false
        // allowNegative: false//(Wtf.realroles[0]==9)?true:false,    // For FM markout amend process
        //            renderer:function(val){
        //                if(!val)
        //                    return 0;
        //                return val;
        //            }
        });
        //        this.movementStatusStore = new Wtf.data.SimpleStore({
        //            fields:["id", "name"],
        //            data : [["0", "PENDING"],["1", "DONE"]]
        //        });
        //        this.movementStatusCmb = new Wtf.form.ComboBox({
        //            hiddenName : 'statusFilter',
        //            store : this.movementStatusStore,
        //            typeAhead:true,
        //            readOnly: false,
        //            displayField:'name',
        //            valueField:'id',
        //            value:"0",
        //            mode: 'local',
        //            width : 80,
        //            triggerAction: 'all',
        //            emptyText:'Select type...'
        //        });
        this.storeCmbStore.load();  
        this.exportButton = new Wtf.exportButton({
            obj: this,
            // id: 'stockrepairreportexport',
            tooltip: "Export Report", //"Export Report details.",  
            menuItem:{
                csv:true,
                pdf:false,
                xls:true
            },
            get:this.type==1?Wtf.autoNum.StockRepairPendingReport:Wtf.autoNum.StockRepairReport,
            label:"Export"

        })
        
        var tbarArray = [];
        
        
        //        tbarArray.push("From Date: ",this.frmDate,"-","To Date: ",this.toDate,"-","Store: ",this.storeCmbfilter,"-","Movement Status: ",this.movementStatusCmb,"-",this.search,"-",this.resetBtn);
        if(this.type==2){
            tbarArray.push(WtfGlobal.getLocaleText("acc.nee.FromDate")+":",this.frmDate,"-",WtfGlobal.getLocaleText("acc.nee.ToDate")+":",this.toDate,"-",WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore")+":",this.storeCmbfilter,"-",WtfGlobal.getLocaleText("acc.GIRO.Status")+":",this.repairStatusCmb,"-",this.search,"-",this.resetBtn);
        }else{
            tbarArray.push(WtfGlobal.getLocaleText("acc.nee.FromDate")+":",this.frmDate,"-",WtfGlobal.getLocaleText("acc.nee.ToDate"),this.toDate,"-",WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore"),this.storeCmbfilter,"-",WtfGlobal.getLocaleText("view.pendingapproval.Module")+":",this.typecmb,"-",this.search,"-",this.resetBtn); 
        }
        
        

        this.record = Wtf.data.Record.create([
        {
            "name":"id"
        },
        {
            "name":"billid"
        },
        {
            "name":"transactionmodule"
        },
        {
            "name":"quantity"
        },
        {
            "name":"returnedqty"
        },
                
        {
            "name":"locationname"
        },
        {
            "name":"locationid"
        },
        {
            "name":"isBatchForProduct"
        },
        {
            "name":"isSerialForProduct"
        },
        {
            "name":"serialname"
        },
        {
            "name":"batchname"
        },
        {
            "name":"status"
        },
        {
            "name":"inspection"
        },
        
        {
            "name":"productname"
        },
        
        {
            "name":"transactionno"
        },
        {
            "name":"storename"
        },
        {
            "name":"storeid"
        },
        {
            "name":"moduletype"
        },
        {
            "name":"productcode"
        },
        {
            "name":"qaapprovalid"
        },
        {
            "name":"moduletype"
        },
        {
            "name":"productid"
        },
        {
            "name":"movementstatus"
        },
        {
            "name":"transactiondate"
        },
        {
            "name":"remark"
        },
        {
            "name":"rdate"
        },
        {
            "name":"reason"
        },
        
        {
            "name":"productdescription"
        }
        
        ]);
        
        
      
        this.ds = new Wtf.data.Store({
            
            url:this.type==1? 'INVApproval/getAllQAApprovalReport.do':'INVApproval/getAllStockRepairList.do',
            params: {
            //            type:this.typecmb.getValue()
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },
            this.record
            )
        });
        
      
        this.ds.on("load",function(store,rec,opt){
            this.grid.getView().refresh();
        },this);

          
              
        this.sm = new Wtf.grid.CheckboxSelectionModel({});
        var cmDefaultWidth = 120;
        this.cm = new Wtf.grid.ColumnModel([
            new Wtf.KWLRowNumberer(), 
            this.sm,
            //            this.expander, //1
            {
                header:  WtfGlobal.getLocaleText("acc.reval.transaction"), //2
                dataIndex: 'transactionno',
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            },
            {
                header:  WtfGlobal.getLocaleText("acc.contractMasterGrid.header8"), //2
                dataIndex: 'productcode',
                groupable: false,
                pdfwidth:50,
                width:cmDefaultWidth,
                fixed:true
            },
            {
                header:  WtfGlobal.getLocaleText("acc.contractMasterGrid.header7"), //2
                dataIndex: 'productname',
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            }, 
            {
                header:  WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"), //2
                dataIndex: 'productdescription',
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            },
            {
                header:  WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore"), //2
                dataIndex: 'storename',
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            },
            {
                header:  WtfGlobal.getLocaleText("acc.contractActivityPanel.Location"), //2
                dataIndex: 'locationname',
                groupable: false,
                width:cmDefaultWidth,
                pdfwidth:50,
                pdfwidth:50,
                hidden:false,
                fixed:true
            },
            {
                header: WtfGlobal.getLocaleText("acc.product.gridQty"),
                dataIndex:"quantity",
                align: 'right',
                pdfwidth:50,
                editor:this.quantityTextField,
                hidden:this.type==1?false:true,
                renderer : function(v) {
                    return '<div align="right">' + parseFloat(v).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + '</div>';
                }
            //                editor:this.quantityTextField
            },
            {
                header: WtfGlobal.getLocaleText("acc.product.gridQty"),
                dataIndex:"quantity",
                align: 'right',
                pdfwidth:50,
                //                editor:this.quantityTextField,
                hidden:this.type==1?true:false,
                renderer : function(v) {
                    return '<div align="right">' + parseFloat(v).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + '</div>';
                }
            //                editor:this.quantityTextField
            },
            {
                header:  'Batch/Challan No',//3
                dataIndex: 'batchname',
                width:cmDefaultWidth,
                pdfwidth:50,
                fixed:true
            },
          
            {
                header:  WtfGlobal.getLocaleText("acc.inventorysetup.serial"),//7
                dataIndex: 'serialname',
                groupable: true,
                pdfwidth:50,
                width:cmDefaultWidth
            },
            {
                header: WtfGlobal.getLocaleText("acc.inventory.QAAproval.RepairStatus"),//8
                dataIndex: 'status',
                groupable: true,
                width:cmDefaultWidth,
                hidden:this.type==2?true:false,
                renderer:function(value){
                    if(value=="APPROVED"){
                        return "<label style = 'color : green;'>APPROVED</label>";
                    }else if(value=="REJECTED"){
                        return "<label style = 'color : red;'>PENDING</label>";
                    }else if(value=="SEND_TO_STORE_TRANSFER"){
                        return "<label style = 'color : green;'>SEND TO STORE TRANSFER</label>";
                    }else{
                        return "PENDING";
                    }
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.inventory.QAAproval.RepairStatus"),//8
                dataIndex: 'status',
                groupable: true,
                width:cmDefaultWidth,
                pdfwidth:50,
                hidden:this.type==1?true:false,
                renderer:function(value){
                    if(value=="Done"){
                        return "<label style = 'color : green;'>Done</label>";
                    }else if(value=="Stockout"){
                        return "<label style = 'color : red;'>Stockout</label>";
                    } 
                }
            },
            {
                header:WtfGlobal.getLocaleText("acc.inventory.QAAproval.StockTransferStatus"),
                //sortable:true,
                dataIndex:'movementstatus',
                Width:100,
                align:'center',
                pdfwidth:50,
                hidden:true,
                renderer:function(value){
                    if(value=="1"){
                        return "<label style = 'color : green;'>DONE</label>";
                    }else{
                        return "<label style = 'color : red;'>PENDING</label>";
                    }
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.inventory.QAAproval.InspectionDate"),  //26
                dataIndex:'transactiondate',
                align:"left",
                pdfwidth:50,
                hidden:false
            //                renderer: function(v,m,rec){
            //                    if(rec.get('status') === 'REJECTED'||this.type==2){
            //                        return v;
            //                    }else return "";
            //                }
            //sortable:false,
               
            }, {
                header: WtfGlobal.getLocaleText("acc.invoice.gridRemark"),  //26
                dataIndex:'remark',
                align:"left",
                pdfwidth:50,
                hidden:this.type==2?true:false
            }, {
                header: WtfGlobal.getLocaleText("acc.masterConfig.29"),  //26
                dataIndex:'reason',
                align:"left",
                pdfwidth:50,
                hidden:this.type==1?true:false
            },
            {
                header: WtfGlobal.getLocaleText("acc.inventory.QAAproval.RepairingDate"),  //26
                dataIndex:'rdate',
                align:"left",
                pdfwidth:50,
                hidden:this.type==1?true:false
            }
            
            ]);
        
        

        /**************date *****/
        this.storeTransferButton = new Wtf.Button({
            text: 'Inter Store Stock Transfer',
            scope: this,
            disabled: true,
            hidden:true,
            iconCls :getButtonIconCls(Wtf.etype.add),
            tooltip: {
               
                text:"Send to store Transfer"
            },
           
            handler:this.sendStoreTransferRequest
        });
        
        this.stockIssueButton = new Wtf.Button({
            text: 'Stock Issue',
            scope: this,
            disabled: true,
            hidden:true,
            iconCls :getButtonIconCls(Wtf.etype.add),
            tooltip: {
               
                text:"Stock Issue"
            },
           
            handler:this.sendStockIssueRequest
        });		
        
        this.rprApprove = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.cc.24"),
            scope: this,
            disabled: true,
            hidden:this.type==2?true:false,
            iconCls :getButtonIconCls(Wtf.etype.add),
            tooltip: {
               
                text:WtfGlobal.getLocaleText("acc.cc.24")
            },
            handler:function(){
                Wtf.MessageBox.confirm("Confirm","Are you sure the selected items are repaired ?", function(btn){
                    if(btn == 'yes') {  
                        //                        this.btnStatus="approved";
                        //                        this.approveRejectStockRepaire("Approve");
                        this.openremarkWin("Approve");
                        
                    }else if(btn == 'no') {
                        return;           
                    }
                },this);
            }      
        });
        
        this.rprReject = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.field.Reject"),
            scope: this,
            disabled: true,
            hidden:this.type==2?true:false,
            iconCls:getButtonIconCls(Wtf.etype.menudelete),
            tooltip: {
               
                text:WtfGlobal.getLocaleText("acc.field.Reject")
            }, //ERM-691 repair store flow for GRN transactions RepairFlowWindow function will be called
            handler:Wtf.account.companyAccountPref.isQaApprovalFlow?this.RepairFlowWindow:function(){
                Wtf.MessageBox.confirm("Confirm","Are you sure the selected item cannot be repaired?", function(btn){
                    if(btn == 'yes') {  
                        this.btnStatus="approved";
                        //                        this.approveRejectStockRepaire("Reject");
                        this.openremarkWin("Reject");
                    }else if(btn == 'no') {
                        return;           
                    }
                },this);
            }      
        });		
        
      
       
        var bbarArray= new Array();
        //        if(!WtfGlobal.EnableDisable(Wtf.UPerm.interstorestocktransfer, Wtf.Perm.interstorestocktransfer.createistreq)) {
        //            bbarArray.push("-",this.storeTransferButton);
        //        }
        //        if(true && (!WtfGlobal.EnableDisable(Wtf.UPerm.issuenote, Wtf.Perm.issuenote.createissuenote))){
        //            bbarArray.push("-",this.stockIssueButton);
        //        }
        if(this.type==1 && !WtfGlobal.EnableDisable(Wtf.UPerm.stockrepair, Wtf.Perm.stockrepair.approverejectstockrepair)){
            bbarArray.push("-",this.rprApprove,"-",this.rprReject);
        }
        //        if(this.type==2){
        bbarArray.push(this.exportButton);
            this.singleRowPrint=new Wtf.exportButton({
                obj:this,
                id:"printSingleRecord"+config.helpmodeid+config.id,
                iconCls: 'pwnd printButtonIcon',
                text: WtfGlobal.getLocaleText("acc.rem.236"),
                tooltip :WtfGlobal.getLocaleText("acc.rem.236.single"),  //'Print Single Record Details',
                disabled :true,
                hidden:this.isSalesCommissionStmt,
                menuItem:{rowPrint:(this.isSalesCommissionStmt)?false:true},
                get:Wtf.autoNum.StockRepairReport,
                moduleid:Wtf.autoNum.StockRepairReport
            });
    
           if(this.type!=1){
                bbarArray.push(this.singleRowPrint);
           }
        //        }
        this.grid=new Wtf.KwlEditorGridPanel({
            //            id:"StockoutApprovalDetailReport",
            cm:this.cm,
            sm:this.sm,
            store:this.ds,
            loadMask:true,
            tbar:tbarArray,
            viewConfig: {
                forceFit: false
            },
            searchLabel:WtfGlobal.getLocaleText("acc.dnList.searchText"),
            searchLabelSeparator:":",
            searchEmptyText: WtfGlobal.getLocaleText("acc.inventory.QAAproval.TransactionNoProductCode"),
            serverSideSearch:true,
            searchField:"productCode",
            clicksToEdit:1,
            //            noSearch:true,
            displayInfo: true,
            displayMsg: 'Displaying  {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
            qsWidth:90,
            //            serverSideSearch:false,
            bbar:bbarArray
      
        });
        
        this.grid.on("beforeedit",this.gridbeforeEdit,this);
        this.grid.on("afteredit", this.validateGridEdit, this);
        Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
            Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
        },this);
        

        this.add(this.grid);
        this.on("activate",function(){
            this.loadgrid(this.frmDate.getValue(),this.toDate.getValue(),this.storeCmbfilter.getValue(),this.repairStatusCmb.getValue(),this.status);
        },this);
        
        this.sm.on("selectionchange",function(){
            var singleStore=true
            var movementStatusFlag=true
            var selected = this.sm.getSelections();
            if(selected.length>0 ){
                if(this.singleRowPrint)this.singleRowPrint.enable();
                var storeid=selected[0].data.storeid
                this.rprReject.enable();
                this.rprApprove.enable();
                for(var i=0;i<selected.length;i++){
                    var movementstatus=selected[i].data.movementstatus;
                    var status=selected[i].data.status;
                    if(storeid != selected[i].data.storeid){
                        singleStore = false;
                    }
                    if(movementstatus =="1" || status !="PENDING"){
                        movementStatusFlag = false;
                    }
                }
                if(movementStatusFlag && singleStore){
                    this.storeTransferButton.enable();
                    this.stockIssueButton.enable();
              
                }else{
                    this.storeTransferButton.disable();
                    this.stockIssueButton.disable();
               
                }
            }else{
                this.storeTransferButton.disable();
                this.stockIssueButton.disable();
                this.rprReject.disable();
                this.rprApprove.disable();
                if(this.singleRowPrint)this.singleRowPrint.disable();
            }
        },this)
        
    },
    //ERM-691 handling repair flow for GRN transactions  
    RepairFlowWindow: function () {
        var title = WtfGlobal.getLocaleText("acc.qaapproval.repairstoretitle");
        var msg = WtfGlobal.getLocaleText("acc.qaapproval.repairstoreflow");
        var selections = this.sm.getSelections();
        this.createqaflowForm();
        this.saveRepairBtn = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.submit"), //'Submit',
            scope: this,
            handler: function () {
                var flowselection = this.TypeForm.getForm().getValues().flowtype;
                if (flowselection == 2) { //type 2 will be IST to scrap store
                    if (this.GRNtransactionsonly(selections)) { //checking for GRN transacations only during QA flow do not allow other trans
                        if (this.scrapStoreCombo!==undefined && this.scrapStoreCombo.getValue()=="") { //check if scrap store has been set
                            WtfComMsgBox(["Warning", WtfGlobal.getLocaleText("acc.store.scrapcreation")], 0);
                        } else {
                            this.repairwindow.close();
                            Wtf.MessageBox.confirm("Confirm", WtfGlobal.getLocaleText("acc.qaapproval.scrapconfirm"), function (btn) {
                                if (btn == 'yes') {
                                    this.btnStatus = "approved";
                                    this.openremarkWin("Scrap");
                                } else if (btn == 'no') {
                                    return;
                                }
                            }, this);
                        }
                    }
                    else {
                        WtfComMsgBox(["Warning", WtfGlobal.getLocaleText("acc.qaapproval.repairstoreflowvalidation")], 0);
                    }
                } else if (flowselection == 1) { //type 1 is existing flow of stock out
                    this.repairwindow.close();
                    Wtf.MessageBox.confirm("Confirm",WtfGlobal.getLocaleText("acc.qaaproval.confirmation"), function (btn) {
                        if (btn == 'yes') {
                            this.btnStatus = "approved";
                            this.openremarkWin("Reject");
                        } else if (btn == 'no') {
                            return;
                        }
                    }, this);
                }
            }
        });
        this.cancelRepairBtn = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"), //'Cancel',
            scope: this
        });
        this.cancelRepairBtn.on("click", function () {
            this.repairwindow.close();
        }, this);
        this.repairwindow= new Wtf.Window({
            title: WtfGlobal.getLocaleText("acc.qaapproval.repairstoretitle"), //
            id: "repairstorewindow",
            closable: true,
            scope: this,
            modal: true,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            width: 520,
            autoScroll: true,
            buttons: [this.saveRepairBtn, this.cancelRepairBtn],
            height: 300,
            items: [{
                    region: 'north',
                    height: 75,
                    border: false,
                    bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                    html: getTopHtml(title, msg, "../../images/createuser.png", true)
                }, {
                    region: 'center',
                    border: false,
                    baseCls: 'bckgroundcolor',
                    layout: 'fit',
                    items: this.TypeForm
                }],
            resizable: false,
            layout: 'border',
            buttonAlign: 'right'
        });
        this.repairwindow.show();
    },
    createqaflowForm:function(){ //ERM-691 creating the options for scrap store and default Stock Out flow
        this.scrapISTflow= new Wtf.form.Checkbox({ //IST to Scrap Store  
            width: 50,
            inputType:'radio',
            inputValue:'2',
            name:'flowtype', 
            fieldLabel:WtfGlobal.getLocaleText("Send Stock to Scrap Warehouse")
        });
        this.stockOutFlow= new Wtf.form.Checkbox({//Create Stock Out default flow
            inputType:'radio',
            name:'flowtype',
            checked:true,
            inputValue:'1',
            width: 50,
            fieldLabel:WtfGlobal.getLocaleText("Create Stock Out")
        });
        
        this.scrapStoreRecord = new Wtf.data.Record.create([{
                    name: 'store_id'
                }, {
                    name: 'name'
                }]);
        
        this.scrapStoreReader = new Wtf.data.KwlJsonReader({
            scope:this,
            root: 'data'
        }, this.scrapStoreRecord);

        this.scrapStore = new Wtf.data.Store({
            scope:this,
            sortInfo: {
                field: 'name',
                direction: "ASC"
            },
            url: 'INVStore/getStoreList.do',
            baseParams:{
                isScrapstoreonly:true
                
            },
            reader: this.scrapStoreReader
        });
        
        this.scrapStoreCombo = new Wtf.form.ComboBox({
            mode: 'local',
            triggerAction: 'all',
            hiddenName:"moduleId",
            fieldLabel : 'Scrapstore',
            typeAhead: true,
            width:160,
            scope:this,
            allowBlank:false,
            store: this.scrapStore,
            disabled:!this.scrapISTflow.getValue(),
            displayField: 'name',
            valueField:'store_id',
            msgTarget: 'side',
            emptyText:"Select Scrap Store"   
        });
        this.flowitems = new Array();
        this.flowitems.push(this.stockOutFlow);
        this.flowitems.push(this.scrapISTflow);
        this.flowitems.push(this.scrapStoreCombo);
        
        this.scrapISTflow.on("check", function (obj, ischecked) {
            if (ischecked) {
                this.scrapStore.load();
                this.stockOutFlow.setValue(false);
                this.scrapStoreCombo.setDisabled(false);
            }
         }, this);
        this.stockOutFlow.on("check",function(obj,ischecked){
            if (ischecked) {
                if (this.scrapStoreCombo.getValue() !== "") {
                    this.scrapStoreCombo.clearValue();
                }
                this.scrapStoreCombo.setDisabled(true);
                this.scrapISTflow.setValue(false);
            }
        },this);
        this.scrapStore.on("load",function(){
            var storeid = this.scrapStore.getAt(0);
            this.scrapStoreCombo.setValue(storeid.data.store_id);
        },this);
        this.TypeForm=new Wtf.form.FormPanel({
            region:'center',
            autoScroll:true,
            border:false,
            labelWidth:245,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
            defaultType: 'textfield',
             items:this.flowitems
       });
    },
    
    GRNtransactionsonly: function (obj) {  //ERM-691 to allow only GRN transactions for the Scrap Store Flow  
        if (obj !== undefined) {
            for (var i = 0; i < obj.length; i++) {
                if (obj[i].data.moduletype !== "goodsreceipt") {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }

    },
    gridbeforeEdit :function(e){
        
        var rec=e.record;
        
        if(e.record.data.isSerialForProduct == true && (e.field =='quantity')) {
            return false;
        }
        
    },
    
    validateGridEdit:function(e){
        if(parseInt(this.ds.getAt(e.row).get("quantity"))>parseInt(this.ds.getAt(e.row).get("actualquantity"))){
            WtfComMsgBox(["Warning", "Entered quantity can not be greater than actual quantity."],0);
            this.ds.getAt(e.row).set("quantity",this.ds.getAt(e.row).get("actualquantity"));
            return false;
        } else if (parseInt(this.ds.getAt(e.row).get("quantity"))==0) {
            WtfComMsgBox(["Warning", "Entered quantity can not be zero."],0);
            this.ds.getAt(e.row).set("quantity",this.ds.getAt(e.row).get("actualquantity"));
            return false;
        }
    },
 
    loadgrid : function(frm,to,storeId,repairStatus){
        this.ds.baseParams = {
            type:this.typecmb.getValue(),
            storeid:storeId,
            repairStatus:(this.type==2)? repairStatus : null,
            frmDate:frm.format('Y-m-d'),
            toDate:to.format('Y-m-d')
        //            type:this.type
        }
        this.ds.load({
            params:{
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,//30,//Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
                ss:  Wtf.getCmp("Quick"+this.grid.id).getValue()
            }
        });
        
    },
    sendStoreTransferRequest : function(){
        var recs = this.grid.selModel.getSelections();
        if(recs.length > 0){
            var prodIds = [];
            var productDataArr = [];
            for(var i= 0; i< recs.length ; i++){
                var rec = recs[i];
                var found = false;
                for(var j=0;j<productDataArr.length ; j++){
                    if(rec.get('productid') == productDataArr[j].productid){
                        found = true;
                        break;
                    }
                }

                if(found){
                    var productData = productDataArr[j];
                    productData.quantity = productData.quantity + rec.get("quantity");
                    
                    var productDetailData = {
                        batchName:rec.get("batchname"),
                        serialNames:rec.get("serialname"),
                        locationId:rec.get("locationid"),
                        quantity:rec.get("quantity"),
                        qaapprovalid:rec.get("qaapprovalid"),
                        qaapprovaldetailid:rec.get("id"),
                        moduletype:rec.get("moduletype")
                    }
                    productData.productDetailDataArr.push(productDetailData);
                    
                }else{
                    prodIds.push(rec.get('productid'));
                    
                    var productData = {
                        storeid: rec.get('storeid'),
                        productid : rec.get('productid'),
                        quantity:rec.get("quantity"),
                        productDetailDataArr: []
                    }
                    
                    var productDetailData = {
                        batchName:rec.get("batchname"),
                        serialNames:rec.get("serialname"),
                        locationId:rec.get("locationid"),
                        quantity:rec.get("quantity"),
                        qaapprovalid:rec.get("qaapprovalid"),
                        qaapprovaldetailid:rec.get("id"),
                        moduletype:rec.get("moduletype")
                    }
                    
                    productData.productDetailDataArr.push(productDetailData);
                    
                    productDataArr.push(productData)
                }
               
            }
            var mainTabId = Wtf.getCmp("as");
            var storeTransferTab = Wtf.getCmp("interStoreTransferTab");
            if(storeTransferTab == null){
                storeTransferTab = new Wtf.Panel({
                    title:"Inter Store Stock Transfer",
                    closable:true,
                    border:false,
                    id:"interStoreTransferTab",
                    layout:"fit",
                    TransferType: 'STORE',
                    items:[this.InterStockTransferForm = new Wtf.transfer({
                        id:"interstoretransfer",
                        layout:'fit',
                        prodIds : prodIds,
                        border:false,
                        qarejected:true
                    
                    })]
                });
                
                mainTabId.add(storeTransferTab);
            }
            mainTabId.setActiveTab(storeTransferTab);
            mainTabId.doLayout();
            this.InterStockTransferForm.fillRequestedItems(productDataArr);
        }else{
            Wtf.Msg.alert('Alert', 'Please select a record', 1)
        }
    },
    openremarkWin:function(operation){
        var selected = this.sm.getSelections(); 
        this.remarkpanel = new Wtf.form.FormPanel(
        {                   
            bodyStyle : 'background-color:#f1f1f1;font-size:10px;padding:10px 15px;',
            lableWidth : 20,
            items : [
            this.reason = new Wtf.form.TextArea({
                fieldLabel:"Reason",
                readOnly: false,
                name: "remark",
                width: 200,
                height:80
            })
            ]
        });
        this.saveRemark = new Wtf.Button({
            text: 'Save',
            scope: this,
            iconCls :getButtonIconCls(Wtf.etype.add),
            tooltip: {
               
                text:"Save remark"
            },
            width : 36,
            handler:function(){
                this.approveRejectStockRepaire(operation);
            }      
        });
        this.cancelBtn = new Wtf.Button({
            text: 'Cancel',
            scope: this,
            //            iconCls :getButtonIconCls(Wtf.etype.add),
            handler:function(){
                this.closeRemarkWin();
            }      
        });
        this.remarkwindow = new Wtf.Window(
        {
            id : 'remWin',
            title :'Reason',
            closable : true,
            width : 360,
            height : 220,
            plain : true,
            iconCls : 'iconwin',
            resizable : true,
            layout : 'fit',
            scope : this,
            listeners : {
                scope : this,
                close : function() {
                    scope: this;
                this.remarkpanel.destroy();
                    this.grid.flag = 0
                }
            },
            items : this.remarkpanel,
            buttons : [
            this.saveRemark,
            this.cancelBtn
            ]

        });  
        this.remarkwindow.show();
    },
    approveRejectStockRepaire:function(operation){        
        var jsondata = "";
        var loadingMask = new Wtf.LoadMask(document.body,{
            msg : WtfGlobal.getLocaleText("acc.msgbox.50")
        });
        var scrapstore = this.scrapStoreCombo ? this.scrapStoreCombo.getValue():""; //ERM-691 passing scrap store id from further QA flow
        loadingMask.show();
        
        var selected = this.sm.getSelections();
        var isFirst=true;
        var saIdsList="";
        var dataArr=new Array();
        for(var i=0;i<selected.length;i++){            
            if(isFirst == true){
                saIdsList += selected[i].get('id');
                isFirst=false;
            }else{
                saIdsList += ","+selected[i].get('id');
            }
            var jObject={};
            var jArray=[];
            jObject.recordid =selected[i].get('id');
            jObject.quantity =selected[i].get('quantity');
            jObject.moduletype =selected[i].get('moduletype');
            jObject.moduleId =selected[i].get('qaapprovalid');
            dataArr.push(jObject);
        }
        var trmLen = jsondata.length - 1;
        var finalStr = JSON.stringify(dataArr);
        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url:"INVApproval/saveStockRepair.do",
            params: {
                saDetailApprovalId:saIdsList,
                jsondata:finalStr,
                operation:operation,
                scrapstore:scrapstore,
                reason:this.reason.getValue()
            }
        },
        this,
        function(result, req){
            loadingMask.hide();
            var msg=result.msg;
            var title="Success";
            if(result.success){
                WtfComMsgBox([title,msg],0);
                Wtf.getCmp('remWin').close();
                this.ds.reload();
            }
            else if(result.success==false){
                title="Error";
                WtfComMsgBox([title,msg],0);
                return false;
            }
        },
        function(result, req){
            loadingMask.hide();
            var msg=result.msg;
            WtfComMsgBox(["Failure","Error occured while processing the data."],3);
            //            this.inspectionwin.close();  
            return false;
        });
        WtfGlobal.resetAjaxTimeOut();
    },
    closeRemarkWin : function() {
        Wtf.getCmp('remWin').close();
        this.grid.flag = 0;
    },
    sendStockIssueRequest : function(){
        var recs = this.grid.selModel.getSelections();
        var isRetailStore = false;
        if(recs.length > 0){
            
            var storeid = recs[0].get('storeid');
            var index = this.storeCmbStore.find('store_id', storeid);
            if(index >= 0){
                var storeRec =  this.storeCmbStore.getAt(index)
                if(storeRec.get('storetypename') == 'Retail'){
                    isRetailStore = true;
                }
            }
            
            var prodIds = [];
            var productDataArr = [];
            for(var i= 0; i< recs.length ; i++){
                var rec = recs[i];
                var found = false;
                for(var j=0;j<productDataArr.length ; j++){
                    if(rec.get('productid') == productDataArr[j].productid){
                        found = true;
                        break;
                    }
                }
                if(found){
                    var productData = productDataArr[j];
                    productData.quantity = productData.quantity + rec.get("quantity");
                    
                    var productDetailData = {
                        batchName:rec.get("batchname"),
                        serialNames:rec.get("serialname"),
                        fromLocationId:rec.get("locationid"),
                        quantity:rec.get("quantity"),
                        qaapprovalid:rec.get("qaapprovalid"),
                        qaapprovaldetailid:rec.get("id"),
                        moduletype:rec.get("moduletype")
                    }
                    productData.productDetailDataArr.push(productDetailData);
                    
                }else{
                    prodIds.push(rec.get('productid'));
                    
                    var productData = {
                        storeid: rec.get('storeid'),
                        productid : rec.get('productid'),
                        quantity:rec.get("quantity"),
                        productDetailDataArr: []
                    }
                    
                    var productDetailData = {
                        batchName:rec.get("batchname"),
                        serialNames:rec.get("serialname"),
                        fromLocationId:rec.get("locationid"),
                        quantity:rec.get("quantity"),
                        qaapprovalid:rec.get("qaapprovalid"),
                        qaapprovaldetailid:rec.get("id"),
                        moduletype:rec.get("moduletype")
                    }
                    
                    productData.productDetailDataArr.push(productDetailData);
                    
                    productDataArr.push(productData)
                }
                
            }
            if(!isRetailStore){
                var mainTabId = Wtf.getCmp("as");
                var stockIssueTab = Wtf.getCmp("goodsIssueParentTab");
                if(stockIssueTab == null){
                    stockIssueTab = new Wtf.Panel({
                        layout:"fit",
                        title:"Stock Issue",
                        closable:true,
                        border:false,
                        id:"goodsIssueParentTab",
                        type:"issue",
                        items:[this.issue =new Wtf.goodIssue({
                            id:"goodissue111"+this.id,
                            layout:'fit',
                            prodIds:prodIds,
                            title:"Issue Note",
                            border:false,
                            qarejected:true
                        })]
                    });
                
                    mainTabId.add(stockIssueTab);
                }
                mainTabId.setActiveTab(stockIssueTab);
                mainTabId.doLayout();

                this.issue.fillRequestedItems(productDataArr);
            }else{
                Wtf.Msg.alert('Error', 'You cannot proceed Retail store items with Stock Issue. Please try with Inter Store Transfer ', 1)
            }
            
        }else{
            Wtf.Msg.alert('Alert', 'Please select a record', 1)
        }
    }
});


function getConsignmentQAReport(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.qa, Wtf.Perm.qa.viewqa)&& Wtf.account.companyAccountPref.activateQAApprovalFlow) {
        var mainTabId = Wtf.getCmp("as");
        var qaReportTab = Wtf.getCmp("consignmentQAReportTab");
        if(qaReportTab == null){
            qaReportTab = new Wtf.ConsignmentQAReport({
                layout:"fit",
                title:"QA Report",
                closable:true,
                border:false,
                //                iconCls:getButtonIconCls(Wtf.etype.inventoryqa),
                id:"consignmentQAReportTab"
            });
            mainTabId.add(qaReportTab);
        }
        mainTabId.setActiveTab(qaReportTab);
        mainTabId.doLayout();
    }else{
        Wtf.account.companyAccountPref.activateQAApprovalFlow?WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature"):WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.Stockrepair.activate.msg")],3);
    }
}


Wtf.ConsignmentQAReport = function (config){
    Wtf.apply(this,config);
    Wtf.ConsignmentQAReport.superclass.constructor.call(this);
}
Wtf.extend(Wtf.ConsignmentQAReport,Wtf.Panel,{
    onRender:function (config) {
        Wtf.ConsignmentQAReport.superclass.onRender.call(this,config);
        this.getTabpanel();
        this.add(this.tabPanel);
    },
    getTabpanel:function (){
        this.getQAAprrovalReport();
        
        this.itemsarr = [];
        
        this.itemsarr.push(this.qaReport);
            
        this.tabPanel = new Wtf.TabPanel({
            activeTab:0,
            id:"consignqaReportTab",
            items:this.itemsarr
        });
    },
    
    getQAAprrovalReport:function (){
        this.qaReport =new Wtf.ConsignQAReport({
            id:"cr_qaReport",
            layout:'fit',
            title:"Consignment QA Report",
            iconCls:getButtonIconCls(Wtf.etype.inventoryqa),
            status:"DONE",
            border:false
        });
    }

});


Wtf.ConsignQAReport = function(config){
    Wtf.ConsignQAReport.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.ConsignQAReport, Wtf.Panel, {
    onRender: function(config) {
        Wtf.ConsignQAReport.superclass.onRender.call(this, config);

        this.fromdateVal =new Date().getFirstDateOfMonth();
        this.fromdateVal.setDate(new Date().getFirstDateOfMonth().getDate());
        this.frmDate = new Wtf.form.DateField({
            emptyText:'From date...',
            readOnly:true,
            width : 85,
            value:WtfGlobal.getDates(true),
            minValue: Wtf.archivalDate,
            name : 'frmdate',
            format: 'Y-m-d'
        });
        this.todateVal=new Date().getLastDateOfMonth();
        this.todateVal.setDate(new Date().getLastDateOfMonth().getDate());
        this.toDate = new Wtf.form.DateField({
            emptyText:'To date...',
            readOnly:true,
            width : 85,
            name : 'todate',
            value:WtfGlobal.getDates(false),
            minValue: Wtf.archivalDate,
            format: 'Y-m-d'
        });
       
          
        
        this.customerCmbRecord = new Wtf.data.Record.create([
        {
            name: 'accid'
        },

        {
            name: 'accname'
        }
        ]);

        this.customerCmbStore = new Wtf.data.Store({
            url:"ACCCustomer/getCustomersForCombo.do",
            baseParams:{
                mode:2,
                group:10,
                deleted:false,
                nondeleted:true,
                common:'1'
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.customerCmbRecord)
        });
        
        this.customerCmbStore.on("load", function(ds, rec, o){
            if(rec.length > 1){
                var newRec=new this.customerCmbRecord({
                    accid:'',
                    accname:'ALL'
                })
                this.customerCmbStore.insert(0,newRec);
                this.customerCmbStore.setValue('');
            }else if(rec.length > 0){
                this.customerCmbfilter.setValue(rec[0].data.accid, true);
            }
        }, this);
            
        this.customerCmbfilter = new Wtf.form.ComboBox({
            fieldLabel : 'Customer*',
            hiddenName : 'customerid',
            store : this.customerCmbStore,
            forceSelection:true,
            displayField:'accname',
            valueField:'accid',
            mode: 'local',
            //            width : 150,
            listWidth:200, 
            triggerAction: 'all',
            emptyText:'Select Customer...',
            typeAhead:true
        });
        this.customerCmbStore.load();  
        this.search = new Wtf.Button({
            anchor: '90%',
            text: 'Search',
            tooltip: {
                text:"Click to Search"
            },
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: function() {
                this.initloadgridstore(this.frmDate.getValue(),this.toDate.getValue(),this.customerCmbfilter.getValue(),"CONSIGNMENT",this.status);
            }
        });
        this.resetQAapproval = new Wtf.Button({
            anchor : '90%',
            text: WtfGlobal.getLocaleText("acc.inventory.QAAproval.ResetFilter"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.inventory.QAAproval.Clickheretoresetfilter")
            },
            iconCls:getButtonIconCls(Wtf.etype.resetbutton),
            scope:this,
            handler:function(){
                this.grid.quickSearchTF.setValue("");
                this.frmDate.reset();
                this.toDate.reset();
                this.customerCmbfilter.setValue('');
                this.initloadgridstore(this.fromdateVal,this.todateVal,"","CONSIGNMENT",this.status);
            }
        });
        var tbarArray = [];
        tbarArray.push("From Date: ",this.frmDate,"-","To Date: ",this.toDate,"-","Customer:",this.customerCmbfilter,"-",this.search,this.resetQAapproval);
       
        var companyDateFormat='y-m-d';
               
        this.record = Wtf.data.Record.create([
        {
            "name":"id"
        },

        {
            "name":"transactionno"
        },

        {
            "name":"createdon"
        },

        {
            "name":"productid"
        },

        {
            "name":"productdescription"
        },
        {
            "name":"customername"
        },

        {
            "name":"batch"
        },
        {
            "name":"serial"
        },
       
        {
            "name":"quantity"
        },
        {
            "name":"status"
        },
        {
            "name":"qaremarks"
        },
        {
            "name":"inspectedby"
        },
        {
            "name":"costcenter"
        },
        {
            "name":"salesperson"
        }

        ]);
        
        
        
        this.ds = new Wtf.data.Store({
            url: 'ACCSalesReturnCMN/getConsignmentSalesQAReport.do',
            //            url: 'INVApproval/getAllQAApprovalList.do',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },
            this.record
            )
        //            remoteSort: true,
        //            remoteGroup: true
        });
        
      
        this.ds.on("load",function(store,rec,opt){
            //            alert(1);
            this.grid.getView().refresh;
        },this);

       
        
        var cmDefaultWidth = 125;
        this.cm = new Wtf.grid.ColumnModel([
            new Wtf.KWLRowNumberer(), //0
            //            this.expander, //1
            {
                header:"Reference No. Id", //2
                dataIndex:'id',
                hidden:true
            },
            {
                header:"QA Reference No", //2 Transaction No
                dataIndex:'transactionno',
                groupable: false,
                width:cmDefaultWidth,
                fixed:true
            },
            {
                header:"Return Date", //2 ie. Transaction Date
                dataIndex:'createdon',
                groupable: false,
                width:cmDefaultWidth,
                fixed:true
            },
            {
                header:"Product Id", 
                dataIndex:'productid',
                groupable: false,
                width:cmDefaultWidth,
                fixed:true
            },
            {
                header:"Product Description", 
                dataIndex:'productdescription',
                groupable: false,
                width:cmDefaultWidth,
                fixed:true
            },
            {
                header:"Customer Name", 
                dataIndex:'customername',
                groupable: false,
                width:cmDefaultWidth,
                fixed:true
            },
            {
                header:(Wtf.jobWorkInFlowFlag!= undefined && Wtf.jobWorkInFlowFlag==true )? "Challan No":"Batch No", 
                dataIndex:'batch',
                groupable: false,
                width:cmDefaultWidth,
                fixed:true
            },
            {
                header:"Serial No", 
                dataIndex:'serial',
                groupable: false,
                width:cmDefaultWidth,
                fixed:true
            },
            {
                header:"Quantity", 
                dataIndex:'quantity',
                groupable: false,
                width:cmDefaultWidth,
                fixed:true
            },
            {
                header: "Status",
                dataIndex: 'status',
                groupable: true,
                width:cmDefaultWidth,
                renderer:function(value){
                    if(value=="APPROVED"){
                        return "<label style = 'color : green;'>APPROVED</label>";
                    }else if(value=="REJECTED"){
                        return "<label style = 'color : red;'>REJECTED</label>";
                    }else{
                        return value;
                    }
                }
            },
            {
                header:"QA Remarks", 
                dataIndex:'qaremarks',
                groupable: false,
                width:cmDefaultWidth,
                fixed:true
            },
            {
                header:"Inspected By", 
                dataIndex:'inspectedby',
                groupable: false,
                width:cmDefaultWidth,
                fixed:true
            },
            {
                header:"Cost Center", 
                dataIndex:'costcenter',
                groupable: false,
                width:cmDefaultWidth,
                fixed:true
            },
            {
                header:"Sales Person",
                dataIndex:'salesperson',
                groupable: false,
                width:cmDefaultWidth,
                fixed:true
            },
           
            ]);
        
        

        /**************date *****/


        this.summary = new Wtf.grid.GroupSummary({});
        
        this.grid=new Wtf.KwlEditorGridPanel({
            id:"consignmentQAReportGrid"+this.id,
            cm:this.cm,
            store:this.ds,
            loadMask:true,
            tbar:tbarArray,
            viewConfig: {
                forceFit: false
            },
            searchLabel:WtfGlobal.getLocaleText("acc.dnList.searchText"),
            searchLabelSeparator:":",
            searchEmptyText: WtfGlobal.getLocaleText("acc.inventory.QAAproval.TransactionNoProductCode"),
            serverSideSearch:true,
            qsWidth:200,
            displayInfo: true,
            displayMsg: 'Displaying  {0} - {1} of {2}',
            emptyMsg: "No results to display"
        });
        this.grid.on("cellclick",this.cellClick,this);
        
        Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
            Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
        },this);
        

       
        this.add(this.grid);
        
        //        this.loadgrid();
        
        this.on("activate",function(){
            this.loadgridstore(this.frmDate.getValue(),this.toDate.getValue(),this.customerCmbfilter.getValue(),"CONSIGNMENT",this.status);
        },this);
    },
   
    initloadgridstore:function(frm, to,customerid,type,status){
        this.ds.baseParams = {
            type:type,
            status:status,
            frmDate:frm.format('Y-m-d'),
            toDate:to.format('Y-m-d'),
            customerid:customerid
        }
        this.ds.load({
            params:{
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
                //                limit:30,
                ss:  Wtf.getCmp("Quick"+this.grid.id).getValue()
            }
        });
    },
    loadgridstore:function(frm, to,customerid,type,status){
        this.ds.baseParams = {
            type:type,
            status:status,
            frmDate:frm.format('Y-m-d'),
            toDate:to.format('Y-m-d'),
            customerid:customerid,
            isJobWorkOrder: this.isJobWorkOrder,
            isisJobWorkOrderInQA: this.isisJobWorkOrderInQA
        }
        this.ds.load({
            params:{
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
                //                limit:30,
                ss:  Wtf.getCmp("Quick"+this.grid.id).getValue()
            }
        });
    }
   
});

