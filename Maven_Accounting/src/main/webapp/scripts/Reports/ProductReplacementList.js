
function callProductReplacementReportDynamicLoad(id, isNormalContract,titlelabel){

    if(!Wtf.account.companyAccountPref.activateSalesContrcatManagement && isNormalContract){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.sales.contrcat.management.activate.msg")],3);
        return;
    }

    id=(id==null)?'productreplacementreportID':id;

    if(isNormalContract){
        id='salesproductreplacementreportID';
    }

    var panel = Wtf.getCmp(id);

    if(panel==null){
        panel = new Wtf.account.ProductReplacementList({
            id:id,
            isNormalContract:isNormalContract,
            title:(titlelabel!=undefined)?titlelabel:WtfGlobal.getLocaleText("acc.contract.product.replacement.report"),
            tabTip:(titlelabel!=undefined)?titlelabel:WtfGlobal.getLocaleText("acc.contract.product.replacement.report"),
            closable:true,
            layout:'border',
            iconCls :'accountingbase debitnotereport',
            border : false
        });
        Wtf.getCmp("as").add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.ProductReplacementList = function(config){
    this.isNormalContract = (config.isNormalContract) ? config.isNormalContract:false;
    
    Wtf.apply(this,config);
    Wtf.account.ProductReplacementList.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.ProductReplacementList,Wtf.Panel,{
    onRender:function(config){
        Wtf.account.ProductReplacementList.superclass.onRender.call(this,config);
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
                store:this.replacementStore,
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
        
        
        var replacementRecord = new Wtf.data.Record.create([
            {name:'replacementId'},
            {name:'replacementNumber'},
            {name:'contractName'},
            {name:'customerName'},
            {name:'attachdoc'}, //SJ[ERP-16428]
            {name:'attachment'},//SJ[ERP-16428]
            {name:'billid'},//SJ[ERP-16428]
            {name:'status'},
            {name:'description'}
        ])
        
        //
        
        var replacementReader = new Wtf.data.KwlJsonReader({
            root:'data'
        },replacementRecord);
        
        //
        
        this.replacementStore = new Wtf.data.Store({
            url:'ACCSalesOrderCMN/getReplacementRequestsForReport.do',
            reader:replacementReader,
            baseParams:{
                isNormalContract:this.isNormalContract
            }
        });
        
        this.replacementStore.load();
        
        //
        this.replacementStore.on("datachanged", function(store){
        if(this.replacementStore.getCount()==0){
//            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
//            this.grid.getView().refresh();
            if(this.exportButton)this.exportButton.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.exportButton)this.exportButton.enable();
            if(this.printButton)this.printButton.enable();
        }
    },this);
        this.expander = new Wtf.grid.RowExpander({});
        this.expander.on("expand",this.onRowexpand,this);
        
        //
        
        var expanderRec = new Wtf.data.Record.create([
            {name:'productName'},
            {name:'replacementQuantity'},
            {name:'replacedQuantity'}
        ]);
        
        this.expandStore = new Wtf.data.Store({
            url:'ACCSalesOrderCMN/getReplacementRequestsDetails.do',
            reader:new Wtf.data.KwlJsonReader({
                root:'data'
            },expanderRec)
        });
        
        this.expandStore.on('load',this.fillExpanderBody,this);
        
        // 
       
        this.bottombtnArr=[];

        this.bottombtnArr.push('-', 
            this.exportButton=new Wtf.exportButton({
                obj:this,
                  filename:WtfGlobal.getLocaleText("acc.contract.product.replacement.report")+"_v1",
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
                get:Wtf.autoNum.productReplacementReport
            }));

        this.bottombtnArr.push('-', this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
            disabled :true,
            menuItem:{
                print:true
            },
            get:Wtf.autoNum.productReplacementReport
        }));
        
       this.sm = new Wtf.grid.CheckboxSelectionModel({ //SJ[ERP-16428]
            singleSelect : false
            });
       
        this.colModel = new Wtf.grid.ColumnModel([this.expander,this.sm,
            {
                header:WtfGlobal.getLocaleText("acc.contract.product.replacement.Number"),
                dataIndex:'replacementNumber',
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
            },{  //SJ[ERP-16428]
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
        },{ //SJ[ERP-16428]
            header:WtfGlobal.getLocaleText("acc.invoiceList.attachments"),  //"Attachments",
            dataIndex:'attachment',
            width:150,
            renderer : Wtf.DownloadLink.createDelegate(this)
        },{
            header:WtfGlobal.getLocaleText("acc.contractActivityPanel.Description"),
            dataIndex:'description',
            width:150,
            align:'center',
            pdfwidth:150,
            
        }
        ]);
            

        //
        
        this.grid = new Wtf.grid.GridPanel({
            store:this.replacementStore,
            cm:this.colModel,
            sm:this.sm, //SJ[ERP-16428]
            border:false,
            stripeRows:true,
            plugins:this.expander,
            loadMask:true,
            viewConfig:{
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
                })
            // * Attachment document in Grid SJ[ERP-16428]
            this.grid.flag = 0;
            this.grid.on('rowclick', Wtf.callGobalDocFunction, this);
            // * Attachment document in Grid SJ[ERP-16428]
        
    },
    onRowexpand:function(scope, record, body){
        this.expanderBody=body;
        this.expandStore.load({params:{replacementId:record.get('replacementId')}});
    },
    fillExpanderBody:function(){
        var disHtml = "";
        var headerArray = [];
        headerArray.push(WtfGlobal.getLocaleText("acc.contract.product.name"));
        headerArray.push(WtfGlobal.getLocaleText("acc.contract.product.replacement.quantity"));
        headerArray.push(WtfGlobal.getLocaleText("acc.contract.product.replaced.quantity"));
        
        var gridHeaderText = WtfGlobal.getLocaleText("acc.contract.product.replacement.detail");
        
        var header = "<span class='gridHeader'>"+gridHeaderText+"</span>";
        header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
        
        for(var i=0;i<headerArray.length;i++){
            header += "<span class='headerRowNew'>" + headerArray[i] + "</span>";
        }
        header += "<span class='gridLine'></span>";
        
        for(var storeCount=0;storeCount<this.expandStore.getCount();storeCount++){
            var rec=this.expandStore.getAt(storeCount);
            var productName = rec.get('productName');
            var replacementQuantity = rec.get('replacementQuantity');
            var replacedQuantity = rec.get('replacedQuantity');
            
            //Column : S.No.
                header += "<span class='gridNo'>"+(storeCount+1)+".</span>";
                
            //Column : Product Name
                header += "<span class='gridRow'  wtf:qtip='"+productName+"' style='width: 20% ! important;'>"+Wtf.util.Format.ellipsis(productName,15)+"</span>";
                
            //Column : Replacement Quantity
                header += "<span class='gridRow'  wtf:qtip='"+replacementQuantity+"' style='width: 15% ! important;'>"+Wtf.util.Format.ellipsis(replacementQuantity,15)+"</span>";
                
            //Column : Replaced Quantity
                header += "<span class='gridRow'  wtf:qtip='"+replacedQuantity+"' style='width: 15% ! important;'>"+Wtf.util.Format.ellipsis(replacedQuantity,15)+"</span>";
                
            header +="<br>";  
        }
        if(this.expandStore.getCount()==0){
            header = "<span class='gridHeader'>"+gridHeaderText+"</span>";
            header += "<span class='headerRow'>"+WtfGlobal.getLocaleText("acc.field.Nodatatodisplay")+"</span>"
        }
        disHtml += "<div class='expanderContainer' style='width:100%'>" + header + "</div>";
        this.expanderBody.innerHTML = disHtml;
    }
})

