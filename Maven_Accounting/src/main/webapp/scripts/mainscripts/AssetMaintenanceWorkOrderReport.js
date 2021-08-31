Wtf.account.AssetMaintenanceWorkOrderReport = function(config){
    Wtf.apply(this,config);
    Wtf.account.AssetMaintenanceWorkOrderReport.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.AssetMaintenanceWorkOrderReport, Wtf.Panel,{
    onRender:function(config){
        Wtf.account.AssetMaintenanceWorkOrderReport.superclass.onRender.call(this,config);
        
        // Create Grid Panel
        this.createExpander();
        
        // Create Grid Panel
        this.createGridPanel();
        
        this.add(this.centerPanel=new Wtf.Panel({
                border: false,
                region: 'center',
                id: 'centerpan1'+this.id,
                autoScroll:true,
//                bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
                baseCls:'bckgroundcolor',
                layout: 'fit',
                tbar:[this.quickPanelSearch,this.resetBttn,'From ',this.startDate,'To ', this.endDate,
                    {text : WtfGlobal.getLocaleText("acc.agedPay.fetch"),
                        iconCls:'accountingbase fetch',
                        scope : this,
                        handler : function() {
                            this.loaddata(false);
                        }
                    },'-',!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.deletemainworkorder)?this.deleteButton:'','-',!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.exportmainworkorder)?this.exportButton:'', '-', !WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.printmainworkorder)?this.printButton:''],
                items:[this.grid],
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar1" + this.id,
                    store: this.store,
                    searchField: this.quickPanelSearch,
                    displayInfo: true,
//            displayMsg: 'Displaying records {0} - {1} of {2}',
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id})
                })
            })
        );
        
    },
    
    createExpander:function(){
        this.expandRec = Wtf.data.Record.create ([
        {name:'productname'},
        {name:'productdetail'},
        {name:'prdiscount'},
        {name:'amount'},
        {name:'productid'},
        {name:'accountid'},
        {name:'accountname'},
        {name:'partno'},
        {name:'quantity'},
        {name:'dquantity'},
        {name:'unitname'},
        {name:'rate'},
        {name:'rateinbase'},
        {name:'externalcurrencyrate'},
        {name:'prtaxpercent'},
        {name:'orderrate'},
        {name:'desc', convert:WtfGlobal.shortString},
        {name:'productmoved'},
        {name:'currencysymbol'},
        {name:'currencyrate'},
        {name: 'type'},
        {name: 'pid'},
        {name:'carryin'},
        {name:'permit'},
        {name:'description'},
        {name:'remark'},
        {name:'linkto'},
        {name:'customfield'}
    ]);
    
    this.expandStoreUrl = "ACCInvoiceCMN/getAssetMaintenanceWorkOrderRows.do";
    
    this.expandStore = new Wtf.data.Store({
        url:this.expandStoreUrl,
//        url:Wtf.req.account+this.businessPerson+'Manager.jsp',
//        baseParams:{
//            mode:14,
//            dtype : 'report'//Display type report/transaction, used for quotation
//        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });
    
    this.expander = new Wtf.grid.RowExpander({});
    
    this.expandStore.on('load',this.fillExpanderBody,this);
    
    this.expander.on("expand",this.onRowexpand,this);
    
    },
    
    createGridPanel:function(){
        
        this.deleteButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.delete"),
            minWidth: 50,
            scope: this,
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            handler: this.deleteWorkOrder.createDelegate(this)
        });
        
        this.startDate=new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
            name:'stdate' + this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            value:WtfGlobal.getDates(true)
        });
    
        this.endDate=new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
            format:WtfGlobal.getOnlyDateFormat(),
            name:'enddate' + this.id,
            value:WtfGlobal.getDates(false)
        });
        
        this.exportButton=new Wtf.exportButton({
            obj:this,
            isEntrylevel:false,
            id:"exportReports"+this.id,   //+config.id,   //added this.id to avoid dislocation of Export button option i.e. CSV & PDF
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
            disabled :false,
//            menuItem:{csv:true,pdf:true,rowPdf:(this.isSalesCommissionStmt)?false:true,rowPdfTitle:WtfGlobal.getLocaleText("acc.rem.39") + " "+ singlePDFtext},
            menuItem:{csv:true,pdf:true,xls:true},
            get:Wtf.autoNum.assetWorkOrderExport
          });
          
          this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
            disabled :false,
            label:'Asset Maintenance Work Orders',
//            params:{},
            menuItem:{print:true},
            get:Wtf.autoNum.assetWorkOrderExport
          });
        
        this.resetBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
            tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            hidden:false,
            iconCls :getButtonIconCls(Wtf.etype.resetbutton),
            disabled :false
        });
        
        this.resetBttn.on('click',this.handleResetClick,this);
        
        var maintenanceRecord = new Wtf.data.Record.create([
            {name:'id'},
            {name:'scheduleEventId'},
            {name:'assetName'},
            {name:'assetGroupName'},
            {name:'startDate',type:'date'},
            {name:'endDate',type:'date'},
            {name:'workOrderNumber'},
            {name:'assignedTo'},
            {name:'assignedToId'},
            {name:'currencyid'},
            {name:'totalAmount'},
            {name:'billdate',type:'date'},
            {name:'attachdoc'}, //SJ[ERP-16428]
            {name:'attachment'},//SJ[ERP-16428]
            {name:'billid'},//SJ[ERP-16428]
            {name:'remark'}// For Work Order
        ]);
        
        var maintenanceReader = new Wtf.data.KwlJsonReader({
            root:'data'
        },maintenanceRecord);
        
        this.rowNo=new Wtf.grid.RowNumberer();
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect:true
        });
        
        this.store = new Wtf.data.Store({
            url:'ACCInvoiceCMN/getAssetMaintenanceWorkOrders.do',
            baseParams:{
                fromDate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true)),
                toDate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false))
            },
            reader:maintenanceReader
        });
        
//        this.store.on('load',this.storeLoaded,this);

//        this.store.load();
        
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.rem.5")+ " "+WtfGlobal.getLocaleText("acc.assetworkorder.WorkOrderNumber"),
            width: 150,
            id:"quickSearch"+this.id,
            field: 'workOrderNumber',
            Store:this.store
        });
        
        var FixedAssetDetailArr = [];
        
        FixedAssetDetailArr.push(this.sm,this.expander,{
            header:WtfGlobal.getLocaleText("acc.assetworkorder.WorkOrderNumber"),
            dataIndex:'workOrderNumber',
            pdfwidth:75,
            width:200
        },{
            header:WtfGlobal.getLocaleText("acc.assetworkorder.CreationDate"),
            dataIndex:'billdate',
            width:200,
            pdfwidth:75,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.assetworkorder.AssetId"),
            dataIndex:'assetName',
            pdfwidth:75,
            width:200
        },{
            header:WtfGlobal.getLocaleText("acc.assetworkorder.AssetGroup"),
            dataIndex:'assetGroupName',
            pdfwidth:75,
            width:200
        },{
            header:WtfGlobal.getLocaleText("acc.assetworkorder.AssignedTo"),
            width:200,
            pdfwidth:75,
            dataIndex:'assignedTo'
        },{
            header:WtfGlobal.getLocaleText("acc.assetworkorder.StartDate"),
            width:200,
            pdfwidth:75,
            dataIndex:'startDate',
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.assetworkorder.EndDate"),
            width:200,
            pdfwidth:75,
            dataIndex:'endDate',
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },
        {
            header:WtfGlobal.getLocaleText("acc.assetworkorder.TotalAmount"),
            dataIndex:'totalAmount',
            pdfwidth:75,
            width:200,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.assetworkorder.Remark"),
            dataIndex:'remark',
            pdfwidth:75,
            width:200
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.attachDocuments"),  //"Attach Documents",
            dataIndex:'attachdoc',
            width:200,
            align:'center',
            hidden: false ,
            renderer : function(val) {
                        return "<div style='height:16px;width:16px;'><div class='pwndbar1 uploadDoc' style='cursor:pointer' wtf:qtitle='"
                        + WtfGlobal
                        .getLocaleText("acc.invoiceList.attachDocuments")
                        + "' wtf:qtip='"
                        + WtfGlobal
                        .getLocaleText("acc.invoiceList.clickToAttachDocuments")
                        +"'>&nbsp;</div></div>";
                    }
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.attachments"),  //"Attachments",
            dataIndex:'attachments',
            width:150,
            hidden:false ,
            renderer : Wtf.DownloadLink.createDelegate(this) //SJ[ERP-16428]
        }
    );
        
        this.FACM = new Wtf.grid.ColumnModel(FixedAssetDetailArr);
        
        this.grid = new Wtf.grid.GridPanel({
            //            layout:'fit',
            autoScroll:true,
            sm:this.sm,
            //            height:130,
            //            autoHeight:true,
            autoWidth:true,
            //            bodyStyle:'margin-top:15px',
            store: this.store,
            cm: this.FACM,
            border : false,
            loadMask : true,
            plugins: this.expander,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        
        this.grid.flag = 0;
        
        this.grid.on('rowclick', Wtf.callGobalDocFunction, this); //SJ[ERP-16428]
        
        this.grid.on("render",function(){
            this.grid.getView().applyEmptyText(); 
        },this);
    },
    
    loaddata:function(isReset){
        if(isReset!=true && this.startDate.getValue()>this.endDate.getValue()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 3); // "From Date can not be greater than To Date."
            return;
        }
        this.store.baseParams.fromDate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        this.store.baseParams.toDate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        
        this.store.load({params: {ss: this.quickPanelSearch.getValue(), start: 0, limit: this.pP.combo.value,pagingFlag:true}});
        
    },
    
    onRowexpand:function(scope, record, body){
        this.expanderBody=body;
        this.expandStore.load({params:{bills:record.data.id}});
    },
    
    fillExpanderBody:function(){
        
        var disHtml = "";
        var arr=[];
        
        var header = "";
        
        var productTypeText = WtfGlobal.getLocaleText("acc.invoiceList.expand.pType");
        arr=[WtfGlobal.getLocaleText("acc.invoiceList.expand.PID"),
             WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),
             productTypeText,
             WtfGlobal.getLocaleText("acc.do.partno"),
             WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"),
             WtfGlobal.getLocaleText("acc.invoice.gridRemark"),
            ];
            
        var gridHeaderText = WtfGlobal.getLocaleText("acc.invoiceList.expand.pList");
        
        header = "<span class='gridHeader'>"+gridHeaderText+"</span>";   //Product List
        header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
        
        for(var i=0;i<arr.length;i++){
            header += "<span class='headerRow' style='width: 10.2%'>" + arr[i] + "</span>";
        }
        
        header += "<span class='gridLine'></span>"; 
        
        for(i=0;i<this.expandStore.getCount();i++){
            var rec=this.expandStore.getAt(i);
            var productname=rec.data['productname'];
            
            //Column : S.No.
            header += "<div style='width:100%;float:left;'><span class='gridNo'>"+(i+1)+".</span>";
            
            var pid=rec.data['pid'];
            
            header += "<span class='gridRow'  wtf:qtip='"+pid+"' style='width:10% ! important;'>"+Wtf.util.Format.ellipsis(pid,15)+"</span>";
            
            //Column : Product Name
            header += "<span class='gridRow' style='width: 10.5%'  wtf:qtip='"+productname+"'>"+Wtf.util.Format.ellipsis(productname,15)+"</span>";
            
            var type = rec.data['type']
            
            header += "<span class='gridRow' wtf:qtip='"+type+"' style='width: 10.5%% ! important;'>"+Wtf.util.Format.ellipsis(type,15)+"</span>";
            
            //Part No
            if(rec.data['partno'] != ""){
                header += "<span class='gridRow' style='word-wrap:break-word;width: 10.5%'>"+rec.data['partno']+"</span>";
            } else {
                header += "<span class='gridRow' style='width:10.5%'>&nbsp;</span>";
            }
            
            //Quantity
            header += "<span class='gridRow' style='width: 10.5%'>"+rec.data['quantity']+" "+rec.data['unitname']+"</span>";
            
            if(rec.data['remark']!="")
                header += "<span class='gridRow' style='width: 10.5%'>"+rec.data['remark']+"</span>";
            
            header +="</div>";
        }
        
        disHtml += "<div class='expanderContainer' style='width:100%'>" + header + "</div>";
        
        this.expanderBody.innerHTML = disHtml;
        
    },
    
    DownloadLink : function(a, b, c, d, e, f) {        
            var msg = "";
            var url = "ACCInvoiceCMN/getAttachDocuments.do";
            //if (c.data['doccount'])
                msg = '<div class = "pwnd downloadDoc" wtf:qtitle="'
                + WtfGlobal.getLocaleText("acc.invoiceList.attachments")
                + '" wtf:qtip="'
                + WtfGlobal
                .getLocaleText("acc.invoiceList.clickToDownloadAttachments")
                + '" onclick="displayDocList(\''
                + c.data['id']
                + '\',\''
                + url
                + '\',\''
                + 'invoiceGridId1'
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
                + '\')" style="width: 16px; height: 16px;cursor:pointer" id=\''
                + c.data['leaveid'] + '\'>&nbsp;</div>';
            //else
              //  msg = "";
            return msg;
        },
        
        docuploadhandler : function(e, t) {
            if (e.target.className != "pwndbar1 uploadDoc")
                return;
            var selected = this.sm.getSelections();            
            if (this.grid.flag == 0) {
                this.fileuploadwin = new Wtf.form.FormPanel(
                {                   
                    url : "ACCInvoiceCMN/attachDocuments.do",
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
                        name : 'invoiceid'
                    }),
                    this.tName = new Wtf.form.TextField(
                    {
                        fieldLabel : WtfGlobal.getLocaleText("acc.invoiceList.filePath") + '*',
                        //allowBlank : false,
                        name : 'file',
                        inputType : 'file',
                        width : 200,
                        //emptyText:"Select file to upload..",
                        blankText:WtfGlobal.getLocaleText("acc.field.SelectFileFirst"),
                        allowBlank:false,
                        msgTarget :'qtip'
                    }) ]
                });

                this.upwin = new Wtf.Window(
                {
                    id : 'upfilewin',
                    title : WtfGlobal
                    .getLocaleText("acc.invoiceList.uploadfile"),
                    closable : true,
                    width : 450,
                    height : 120,
                    plain : true,
                    iconCls : 'iconwin',
                    resizable : false,
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
                        text : WtfGlobal
                        .getLocaleText("acc.invoiceList.bt.upload"),
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
                this.sendInvoiceId.setValue(selected[0].get('id'));
                this.upwin.show();
                this.grid.flag = 1;
            }
        },
        close1 : function() {
            Wtf.getCmp('upfilewin').close();
            this.grid.flag = 0;
        },
        
        upfileHandler : function() {
            if (this.fileuploadwin.form.isValid()) {
                Wtf.getCmp('save').disabled = true;
            }
            //var selected = this.sm.getSelections();
           // if (selected[0].get('doccnt') < 3) {
                if (this.fileuploadwin.form.isValid()) {
                    this.fileuploadwin.form.submit({
                        scope : this,
                        failure : function(frm, action) {
                            this.upwin.close();
                            //this.genSaveSuccessResponse(eval('('+action.response.responseText+')'));
                        },
                        success : function(frm, action) {
                            this.upwin.close();                            
                            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), "File uploaded successfully.");
                        }
                    })
                }
//            } else {
//                msgBoxShow(
//                    [
//                    WtfGlobal
//                    .getLocaleText("el.msg.head.info"),
//                    WtfGlobal
//                    .getLocaleText("el.msg.threemax") ],
//                    1, 1);
//                this.upwin.close();
//            }
        },
        
        handleResetClick:function(){
            if(this.quickPanelSearch.getValue()){
                this.quickPanelSearch.reset();
                this.loaddata(true);
                this.store.on('load',this.storeloaded,this);
            }
//            else{
//                if(this.isRequisition || this.isRFQ){//for Purchase Requisition,RFQ
//                    this.startDate.setValue(WtfGlobal.getDates(true));
//                    this.endDate.setValue(WtfGlobal.getDates(false));
//                    this.loadStore();
//                }
//            }
        },
        
        storeloaded:function(store){
            this.quickPanelSearch.StorageChanged(store);
        },
        
        deleteWorkOrder:function(){
            var recArray=this.grid.getSelectionModel().getSelections();
            if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
                WtfComMsgBox(['Information',WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
                return;
            }

            var rec = recArray[0];

            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.nee.48"),function(btn){
                if(btn!="yes") {
                    return;
                }

            Wtf.Ajax.requestEx({
                url:'ACCInvoice/deleteAssetMaintenanceWorkOrder.do',
                params: {
                    workOrderId:rec.get('id')
                }
            },this,this.genSuccessResponse,this.genFailureResponse);

            },this);

        },
        
        genSuccessResponse:function(response){
            WtfComMsgBox(['Work Order',response.msg],response.success*2+1);
            this.store.reload();
        },

        genFailureResponse:function(response){
            var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
            if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        }
});