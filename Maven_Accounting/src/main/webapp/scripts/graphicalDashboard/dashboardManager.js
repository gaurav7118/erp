/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

function openDashboardManager(loadingMask) {
    
    var panel = Wtf.getCmp("dashboardManager");
            if(panel==null){
        panel = new Wtf.account.DashboardManager({
            id : 'dashboardManager',
            border : false,
            layout: 'fit',
            title: "Dashboard Manager",
            tabTip: "Dashboard Manager", 
            closable: true,
            iconCls:'dashboard-manager'
        });
        Wtf.getCmp('as').add(panel);
        panel.on("resize", function(){
            panel.doLayout();
        },this);
        panel.on("activate", function() {
            
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').doLayout(); 
    }
    if (loadingMask != undefined) {
        loadingMask.hide();
    }
    Wtf.getCmp('as').setActiveTab(panel);
}

Wtf.account.DashboardManager = function(config) {
    Wtf.apply(this, config);
    this.createDashboardList();
    this.items = [this.dashboardList];
    
    this.dashboardList.on("render",function(){
        this.loadingMask = new Wtf.LoadMask(this.dashboardList.body, {
            msg : WtfGlobal.getLocaleText("acc.msgbox.50")
        });
    },this);

    Wtf.account.DashboardManager.superclass.constructor.call(this,config );
}

Wtf.extend(Wtf.account.DashboardManager, Wtf.account.ClosablePanel,{
    createDashboardList : function(){
        var searchRecord = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
            {name: 'description'},
            {name: 'json'},
            {name: 'isactive'},
            {name: 'deleted'},
            {name: 'user'},
            {name: 'createdby'},
            {name: 'modifiedby'},
            {name: 'createdon'},
            {name: 'updatedon'}
        ]);

        var jsonReader = new Wtf.data.JsonReader({
            root: "data",
            totalProperty: 'count'
        }, searchRecord);

        var totalSalesstore = new Wtf.data.Store({
            reader: jsonReader,
            url: "ACCUSDashboard/getDashboard.do",
            baseParams: {
                companyids: companyid,
                consolidateFlag: false,
                creditonly: false,
                dir: 'ASC',
                enddate: 'December, 2016',
                gcurrencyid: Wtf.account.companyAccountPref.currencyid,
                getRepeateInvoice: false,
                mode: 18,
                nondeleted: true,
                stdate: 'January, 2016'
            }
        });
        totalSalesstore.load({
            start : 0,
            limit : 30
        });
        
        totalSalesstore.on("beforeload",function(){
            this.loadingMask.show();
        },this);
        
        totalSalesstore.on("load",function(){
            this.loadingMask.hide();
        },this);
        
        this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: totalSalesstore,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.agedPay.norec"),  //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
            })
        });
        
        this.newTabButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.create.title"),
            scope: this,
            iconCls :getButtonIconCls(Wtf.etype.add),
            handler : function(){
                openCreateDashboard();
            }
        });
        
        this.applyDashboardBtn = new Wtf.Toolbar.Button({
            text:"Apply Dashboard",
            scope: this,
            iconCls :"apply-dashboard",
            handler : function(){
                this.applyDashboard();
            }
        });
        
        this.applyDefaultDashboardBtn = new Wtf.Toolbar.Button({
            text:"Apply Default Dashboard",
            scope: this,
            iconCls :"default-dashboard",
            handler : function(){
                this.applyDefaultDashboard();
            }
        });
        
        this.deleteMenu = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),
            scope: this,
            tooltip:WtfGlobal.getLocaleText("acc.field.allowsyoutodeletetherecord"),
            iconCls:getButtonIconCls(Wtf.etype.deletebutton)
        });
        
        var selModel = new Wtf.grid.CheckboxSelectionModel({
            singleSelect:true
        });
    
        this.dashboardList = new Wtf.grid.GridPanel({
            layout:"fit",
            store : totalSalesstore,
            columns: [selModel,
            {
                header: "Dashboard Name", 
//                width: 200, 
                sortable: true, 
                dataIndex: 'name'
            },

            {
                header: "Description", 
//                width: 120, 
                sortable: true, 
                dataIndex: 'description'
            },

            {
                header: "Created By", 
//                width: 120, 
                sortable: true, 
                dataIndex: 'createdby'
            },

            {
                header: "Active Dashboard", 
//                width: 120, 
                sortable: true, 
                dataIndex: 'isactive'
            },

            {
                header: "Delete", 
//                width: 135, 
                sortable: true, 
                dataIndex: 'deleted'
            }
            ],
            viewConfig: {
                forceFit: true
            },
            sm: selModel,
            bbar: this.pagingToolbar,
            tbar:[this.newTabButton,this.deleteMenu ,this.applyDashboardBtn,this.applyDefaultDashboardBtn]
        });
    },
    applyDashboard : function(){
        var params = this.dashboardList.getSelectionModel().getSelected().data;
        Wtf.Ajax.requestEx({
            url: "ACCUSDashboard/setActiveDashboard.do",
            params :params
        }, this, function(resData,response) {
            Wtf.Msg.alert("Success","Dashboard applied successfully");
            this.dashboardList.store.reload();
            refreshDashboard(resData);
//            getDashboard();
        }, function() {});
    },
    applyDefaultDashboard : function(){
        Wtf.Ajax.requestEx({
            url: "ACCUSDashboard/setActiveDashboard.do",
            params :{
                applyDefault : true
            }
        }, this, function(resData,response) {
            Wtf.Msg.alert("Success","Dashboard applied successfully");
            this.dashboardList.store.reload();
            refreshDashboard(resData);
//            getDashboard();
        }, function() {});
    }
});
