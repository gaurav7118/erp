
function callProductMaintenanceReportDynamicLoad(id, isNormalContract,titlelabel){

    if(!Wtf.account.companyAccountPref.activateSalesContrcatManagement && isNormalContract){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.sales.contrcat.management.activate.msg")],3);
        return;
    }

    id=(id==null)?'productmaintenancereportID':id;

    if(isNormalContract){
        id='normalproductmaintenancereportID';
    }

    var panel = Wtf.getCmp(id);

    if(panel==null){
        panel = new Wtf.account.MaintenanceReport({
            id:id,
            title:(titlelabel!=undefined)?titlelabel:WtfGlobal.getLocaleText("acc.contract.product.maintenance.report"),
            tabTip:(titlelabel!=undefined)?titlelabel:WtfGlobal.getLocaleText("acc.contract.product.maintenance.report"),
            closable:true,
            isNormalContract:isNormalContract,
            layout:'border',
            iconCls :'accountingbase debitnotereport',
            border : false
        });
        Wtf.getCmp("as").add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

//**********************************************************************************************


Wtf.account.MaintenanceReport = function(config){
    this.isNormalContract=(config.isNormalContract)?config.isNormalContract:false;
    Wtf.apply(this,config);
    Wtf.account.MaintenanceReport.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.MaintenanceReport, Wtf.Panel,{
    onRender:function(config){
        Wtf.account.MaintenanceReport.superclass.onRender.call(this,config);
        
        this.bottombtnArr=[];

        this.bottombtnArr.push('-', 
            this.exportButton=new Wtf.exportButton({
                obj:this,
                filename:WtfGlobal.getLocaleText("acc.contract.product.maintenance.report")+"_v1",
                id:"exportReports"+this.id,
                text: WtfGlobal.getLocaleText("acc.common.export"),
                tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
                disabled :true,
                scope : this,
                menuItem:{
                    csv:true,
                    pdf:true,
                    xls:true

                },
                get:Wtf.autoNum.productMaintenanceReport
            }));

        this.bottombtnArr.push('-', this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
            disabled :true,
            menuItem:{
                print:true
            },
            get:Wtf.autoNum.productMaintenanceReport
        }));
        

        
        // Create Grid Panel
        this.createGridPanel();
        
        this.add(this.centerPanel = new Wtf.Panel({
            border:false,
            region:'center',
            autoScroll:true,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:[this.grid],
            bbar:this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize:30,
                id:"pagingtoolbar" + this.id,
                store:this.maintenanceStore,
//                searchField: this.quickPanelSearch,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
                plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id}),
                items:this.bottombtnArr
            })
        }))
    },
    
    createGridPanel:function(){
        //
        
        
        var maintenanceRecord = new Wtf.data.Record.create([
            {name:'maintenanceId'},
            {name:'maintenanceNumber'},
            {name:'contractName'},
            {name:'customerName'},
            {name:'attachdoc'}, //SJ[ERP-16428]
            {name:'attachment'},//SJ[ERP-16428]
            {name:'billid'},//SJ[ERP-16428]
            {name:'status'}
        ])
        
        //
        
        var maintenanceReader = new Wtf.data.KwlJsonReader({
            root:'data'
        },maintenanceRecord);
        
        //
        
        this.maintenanceStore = new Wtf.data.Store({
            url:'ACCSalesOrderCMN/getMaintenanceRequestsForReport.do',
            reader:maintenanceReader,
            baseParams:{
                isNormalContract:this.isNormalContract
            }
        });
        
        this.maintenanceStore.on("datachanged", function(store){
            if(this.maintenanceStore.getCount()==0){
//                this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
//                this.grid.getView().refresh();
                if(this.exportButton)this.exportButton.disable();
                if(this.printButton)this.printButton.disable();
            }else{
                if(this.exportButton)this.exportButton.enable();
                if(this.printButton)this.printButton.enable();
            }
        },this);
    
        this.maintenanceStore.load();
        
        // 
         this.sm = new Wtf.grid.CheckboxSelectionModel({    //SJ[ERP-16428]
            singleSelect : false
            });
        this.colModel = new Wtf.grid.ColumnModel([this.sm,
            {
                header:WtfGlobal.getLocaleText("acc.contract.product.maintenance.Number"),
                dataIndex:'maintenanceNumber',
                align:'center',
                width:'auto',
                hidden:true
            },{
                header:WtfGlobal.getLocaleText("acc.contract.product.maintenance.Number"),
                dataIndex:'maintenanceNumber',
                align:'center',
                width:'auto',
                pdfwidth:50
            },{
                header:WtfGlobal.getLocaleText("acc.contract.Name"),
                dataIndex:'contractName',
                align:'center',
                pdfwidth:100,
                width:'auto'
            },{
                header:WtfGlobal.getLocaleText("acc.contract.customer"),
                dataIndex:'customerName',
                align:'center',
                pdfwidth:100,
                width:'auto'
            },{
                header:WtfGlobal.getLocaleText("acc.contract.product.replacement.Status"),
                dataIndex:'status',
                align:'center',
                width:'auto',
                pdfwidth:100,
                renderer:function(value){
                    if(value)
                        return 'Close';
                    else
                        return 'Open';
                }
            },{   //SJ[ERP-16428]
            header:WtfGlobal.getLocaleText("acc.invoiceList.attachDocuments"),  //"Attach Documents",
            dataIndex:'attachdoc',
            width:150,
            align:'center',
            renderer : function(val) {
                        return "<div style='height:16px;width:16px;'><div class='pwndbar1 uploadDoc' style='cursor:pointer' wtf:qtitle='"
                        + WtfGlobal
                        .getLocaleText("acc.invoiceList.attachDocuments")
                        + "' wtf:qtip='"
                        + WtfGlobal
                        .getLocaleText("acc.invoiceList.clickToAttachDocuments")
                        +"'>&nbsp;</div></div>";
                    }
        },{  //SJ[ERP-16428]
            header:WtfGlobal.getLocaleText("acc.invoiceList.attachments"),  //"Attachments",
            dataIndex:'attachment',
            width:150,
            renderer : Wtf.DownloadLink.createDelegate(this)
        }
        ]);
        
        
        //
        
        this.grid = new Wtf.grid.GridPanel({
            store:this.maintenanceStore,
            cm:this.colModel,
             sm:this.sm,  //SJ[ERP-16428]
            border:false,
            stripeRows:true,
            viewConfig:{
                forceFit:true,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        })
        // * Attachment document in Grid SJ[ERP-16428]
            this.grid.flag = 0;
            this.grid.on('rowclick', Wtf.callGobalDocFunction, this);
            // * Attachment document in Grid SJ[ERP-16428]
    }
});