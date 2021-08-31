Wtf.account.AssetMaintenanceSchedulerReport = function(config){
    this.isReport = (config.isReport)?config.isReport:false;
    this.isContract = (config.isContract)?config.isContract:false;
    Wtf.apply(this,config);
    Wtf.account.AssetMaintenanceSchedulerReport.superclass.constructor.call(this,config);
    
//    this.addEvents({
//        'storeloaded':true//event will be fire when data will be saves successfully.
//    });
}

Wtf.extend(Wtf.account.AssetMaintenanceSchedulerReport, Wtf.Panel,{
    
    onRender:function(config){
        Wtf.account.AssetMaintenanceSchedulerReport.superclass.onRender.call(this,config);
        
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
                tbar:this.tbar1,
                items:[this.FADetailsGrid],
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar1" + this.id,
                    store: this.maintenanceStore,
                    searchField: this.quickPanelSearch,
                    displayInfo: true,
//            displayMsg: 'Displaying records {0} - {1} of {2}',
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id})
                })
            })
        );
            
        
    },
    
    createGridPanel:function(){
        //
//        this.deleteButton = new Wtf.Toolbar.Button({
//            text: 'Delete',
//            minWidth: 50,
//            hidden:this.isFromCreationForm,
//            scope: this,
//            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
//            handler: this.deleteMaintenanceScheduleEvents.createDelegate(this)
//        });
        
        
        
        
        
        // create asset combo
        
        this.assetCmbRec = Wtf.data.Record.create ([
            {name:'assetdetailId'},
            {name:'assetGroup'},
            {name:'assetDepreciationMethod'},
            {name:'assetGroupId'},
            {name:'assetId'},
            {name:'installationDate',type:'date'},
            {name:'purchaseDate',type:'date'},
            {name:'salvageRate'},
            {name:'salvageValue'},
            {name:'assetNetBookValue'},
            {name:'deleted',mapping:'isAssetSold'},
            {name:'assetLife'},
            {name:'isDepreciable'},
            {name:'isLeased'},
            {name:'assetUser'},
            {name:'serialno'},
            {name:'location'},
            {name:'warehouse'},
            {name:'batchname'},
            {name:'expstart',type:'date'},
            {name:'expend',type:'date'}
            
        ]);
        
        this.assetStore = new Wtf.data.Store({
            url: "ACCAsset/getAssetDetails.do",
            baseParams: {
                excludeSoldAssets:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                autoLoad: false
            }, this.assetCmbRec)
        });
        
        this.assetCmb = new Wtf.form.ExtFnComboBox({
            fieldLabel: 'Select Asset',
            hiddenName: 'asset',
            id: "asstcmb" + this.id,
            store: this.assetStore,
            valueField: 'assetdetailId',
            displayField: 'assetId',
            extraFields:[],
            listWidth:240,
//            extraComparisionField:'acccode',// type ahead search on acccode as well.
            allowBlank: true,
            hirarchical: true,
            emptyText: 'Select Asset',
            mode: 'local',
            typeAheadDelay:30000,
            minChars:1,
            typeAhead: true,
            forceSelection: true,
            selectOnFocus: true,
            anchor: "50%",
            triggerAction: 'all',
            scope: this,
            width:100
        });
        
        this.assetStore.on("load", function() {
            var record = new Wtf.data.Record({
                assetdetailId: "",
                assetId: "All Records"
            });
            this.assetStore.insert(0, record);
            this.assetCmb.setValue("");
        }, this);
        
        if(this.isReport){
            this.assetStore.load();
        }
        
        
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
        
        
        this.hiddenDate = new Wtf.form.DateField({
            fieldLabel:'',
            format:WtfGlobal.getOnlyDateFormat(),
            hideLabel:true,
            hidden:true,
            name: 'hiddenCurrentDate',
            value:Wtf.serverDate,
            width : 130
        });
        
        Wtf.workOrderStatusStore.load();
        Wtf.assignedToStore.load();
        
        this.assignedTo= new Wtf.form.FnComboBox({
            triggerAction:'all',
            mode: 'local',
//            selectOnFocus:true,
            valueField:'id',
            displayField:'name',
            id:"assignedTo"+this.id,
            store:Wtf.assignedToStore,
//            addNoneRecord: true,
//            anchor: '94%',
            width : 200,
//            typeAhead: true,
            forceSelection: true,
            fieldLabel: 'Assigned To',
            emptyText: 'Select Assigned To...',
//            hideLabel:(this.isOrder && this.quotation),
//            hidden:( this.isOrder && this.quotation),
            name:'assignedTo',
            hiddenName:'assignedTo'            
        });
        
        this.assignedTo.addNewFn=this.addAssignedTo.createDelegate(this);
        
        this.status= new Wtf.form.FnComboBox({
            triggerAction:'all',
            mode: 'local',
//            selectOnFocus:true,
            valueField:'id',
            displayField:'name',
            id:"workOrderstatus"+this.id,
            store:Wtf.workOrderStatusStore,
//            addNoneRecord: true,
//            anchor: '94%',
            width : 200,
//            typeAhead: true,
            forceSelection: true,
            fieldLabel: '',
            emptyText: 'Select Status...',
//            hideLabel:(this.isOrder && this.quotation),
//            hidden:( this.isOrder && this.quotation),
            name:'status',
            hiddenName:'status'            
        });
        
        this.status.addNewFn=this.addStatus.createDelegate(this);
        
        
        var maintenanceRecord = new Wtf.data.Record.create([
            {name:'scheduleId'},
            {name:'assetDetailsId'},
            {name:'assetName'},
            {name:'assetGroupName'},
            {name:'startDate',type:'date'},
            {name:'endDate',type:'date'},
            {name:'actualStartDate',type:'date'},
            {name:'actualEndDate',type:'date'},
            {name:'workJobId'},
            {name:'assignedTo'},
            {name:'workOrderId'},// For Work Order
            {name:'assignedToId'},// For Work Order
            {name:'assignedToName'},
            {name:'currencyid'},// For Work Order
            {name:'billdate',type:'date'},// For Work Order
            {name:'remark'},// For Work Order
            {name:'status'},
            {name:'statusinfo'},
            {name:'scheduleName'},
            {name:'attachdoc'}, //SJ[ERP-16428]
            {name:'attachment'},//SJ[ERP-16428]
            {name:'billid'},//SJ[ERP-16428]
            {name:'action'}
        ]);
        
        
        var maintenanceReader = new Wtf.data.KwlJsonReader({
            root:'data',
            totalProperty: 'totalCount'
        },maintenanceRecord);
        
        //
        this.rowNo=new Wtf.grid.RowNumberer();
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect:true
        });
        
        this.maintenanceStore = new Wtf.data.Store({
            url:'ACCInvoiceCMN/getAssetMaintenanceScheduleReport.do',
            reader:maintenanceReader,
            baseParams:{
                assetId:(this.assetRec)?this.assetRec.get('assetdetailId'):'',
                isContract:this.isContract,
                scheduleId:this.scheduleId,
                isFromCreateButton:this.isFromCreateButton,
                fromDate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true)),
                toDate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false))
            }
        });
        
//        this.maintenanceStore.on('beforeload',function(){
//            
//        },this)
        
//        this.maintenanceStore.on('loadexception',function(){
//            this.FADetailsGrid.loadMask.hide();
//        },this);

        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.rem.5")+" Assigned To, Status",
            width: 150,
            id:"quickSearch"+this.id,
            field: 'workOrderNumber',
            Store:this.maintenanceStore
        });
        
        this.exportButton=new Wtf.exportButton({
                obj:this,
                id:"exportReports"+this.id,
                tooltip:WtfGlobal.getLocaleText("acc.common.exportTT"),  //"Export Report details.",  
                params:{
                    name:WtfGlobal.getLocaleText("acc.field.AssetMaintenanceSchedulerReport"),
                    assetId:(this.assetRec)?this.assetRec.get('assetdetailId'):'',
                    isContract:this.isContract,
                    scheduleId:(this.scheduleId!=undefined)?this.scheduleId:'',
                    isFromCreateButton:this.isFromCreateButton,
                    fromDate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true)),
                    toDate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false))
                },
                menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
                get:Wtf.autoNum.AssetMaintenanceSchedulerReport,
                filename:WtfGlobal.getLocaleText("acc.field.AssetMaintenanceSchedulerReport"),
                label:WtfGlobal.getLocaleText("acc.field.AssetMaintenanceScheduler")
        });
        this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
            params:{
                name:WtfGlobal.getLocaleText("acc.field.AssetMaintenanceSchedulerReport"),
                assetId:(this.assetRec)?this.assetRec.get('assetdetailId'):'',
                isContract:this.isContract,
                scheduleId:this.scheduleId,
                isFromCreateButton:this.isFromCreateButton,
                fromDate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true)),
                toDate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false))
            },
            menuItem:{print:true},
            filename:WtfGlobal.getLocaleText("acc.field.AssetMaintenanceSchedulerReport"),
            get:Wtf.autoNum.AssetMaintenanceSchedulerReport,
            label:WtfGlobal.getLocaleText("acc.field.AssetMaintenanceScheduler")
        });
        
        this.maintenanceStore.on('load',this.storeLoaded,this);
//        var assetScheduleId=(this.assetRec)?this.assetRec.get('assetdetailId'):'';
//        var contractScheduleId=(this.scheduleId)?this.scheduleId:'';
//        if(!(this.scheduleId=='' && contractScheduleId=='')){
//            this.maintenanceStore.load();
//        }
        
        var FixedAssetDetailArr = [];
        
        FixedAssetDetailArr.push(this.sm,this.rowNo,{
            header:WtfGlobal.getLocaleText("acc.bankReconcile.startDate"),
            dataIndex:'startDate',
            width:200,
            pdfwidth:75,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.bankReconcile.endDate"),
            dataIndex:'endDate',
            width:200,
            pdfwidth:75,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.asset.ActualStartDate"),
            dataIndex:'actualStartDate',
            width:200,
            hidden:this.isContract,
            pdfwidth:75,
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            editor:new Wtf.form.DateField({
                maxLength:250,
                format:WtfGlobal.getOnlyDateFormat(),
                xtype:'datefield'
            })
        },{
            header:WtfGlobal.getLocaleText("acc.asset.ActualEndDate"),
            dataIndex:'actualEndDate',
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            width:200,
            pdfwidth:75,
            hidden:this.isContract,
            editor:new Wtf.form.DateField({
                maxLength:250,
                format:WtfGlobal.getOnlyDateFormat(),
                xtype:'datefield'
            })
        },{
            header:WtfGlobal.getLocaleText("acc.fixed.asset.id"),
            width:200,
            pdfwidth:75,
            hidden:this.isContract,
            dataIndex:'assetName'
        },{
            header:WtfGlobal.getLocaleText("acc.maintenance.schedule.name"),//'Schedule Name',
            width:200,
            pdfwidth:75,
//            hidden:this.isContract,
            dataIndex:'scheduleName'
        },{
            header:WtfGlobal.getLocaleText("acc.asset.WorkJobId"),
            width:200,
            pdfwidth:75,
            hidden:this.isContract,
            dataIndex:'workJobId'
        },{
            header:WtfGlobal.getLocaleText("acc.assetworkorder.AssignedTo"),
            width:200,
            pdfwidth:75,
            dataIndex:'assignedToName',
            hidden:this.isContract,
            renderer:Wtf.comboBoxRenderer(this.assignedTo),
            editor:this.assignedTo
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.status"),
            width:200,
            dataIndex:'statusinfo',
            pdfwidth:75,
            hidden:this.isContract,
            renderer:Wtf.comboBoxRenderer(this.status),
            editor:this.status
        },{
            header:WtfGlobal.getLocaleText("acc.asset.ViewWorkOrder"),
            align:'center',
            dataIndex:'action',
            hidden:this.isReport || this.isContract,
            width:200,
            renderer: this.viewRenderer.createDelegate(this)
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),//"Action",
            align:'center',
            hidden:this.isReport || this.isContract,
            width:100,
            renderer: this.deleteRenderer.createDelegate(this)
        },
        {   //SJ[ERP-16428]
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
        },{//SJ[ERP-16428]
            header:WtfGlobal.getLocaleText("acc.invoiceList.attachments"),  //"Attachments",
            dataIndex:'attachment',
            width:150,
            renderer : Wtf.DownloadLink.createDelegate(this)
        }
    );
        
        this.FACM = new Wtf.grid.ColumnModel(FixedAssetDetailArr);
        
        
        this.FADetailsGrid = new Wtf.grid.EditorGridPanel({
//            layout:'fit',
            clicksToEdit:1,
            autoScroll:true,
            sm:this.sm,
//            height:130,
//            autoHeight:true,
           autoWidth:true,
//            bodyStyle:'margin-top:15px',
            store: this.maintenanceStore,
            cm: this.FACM,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        
        this.FADetailsGrid.on("render",function(){
            this.FADetailsGrid.getView().applyEmptyText(); 
        },this);
        
        this.grid=this.FADetailsGrid;
        
        // * Attachment document in Grid SJ[ERP-16428]
            this.grid.flag = 0;
            this.grid.on('rowclick', Wtf.callGobalDocFunction, this);
            // * Attachment document in Grid SJ[ERP-16428]
        this.FADetailsGrid.on('rowclick',this.handleRowClick,this);
        this.FADetailsGrid.on('afteredit',this.updateRow,this);
        this.FADetailsGrid.on('beforeedit',this.beforeEditHandler,this);
        
        // tbar cration
        this.tbar1 = [];
        
        this.tbar1.push(this.hiddenDate);
        if(this.isReport){
            this.tbar1.push('Select Asset ',this.assetCmb);
        }
        
        this.tbar1.push(WtfGlobal.getLocaleText("acc.common.from"),this.startDate);
        this.tbar1.push(WtfGlobal.getLocaleText("acc.common.to"), this.endDate);
        this.tbar1.push({
                        text : WtfGlobal.getLocaleText("acc.agedPay.fetch"),
                        iconCls:'accountingbase fetch',
                        scope : this,
                        handler : this.loaddata
                    });
                    
        this.tbar1.push('-',this.exportButton);
        this.tbar1.push('-',this.printButton);
        
    },
    
    beforeEditHandler:function(e){
        if(this.isReport){
            e.cancel= true;
            return;
            }
    },
    
    updateRow:function(obj){
        if(obj!=null){
            
            var asdate = null;// actual start date
            var aedate = null;// actual end date
            
            var paramsRec = new Wtf.data.Record({});
            
            var rec=obj.record;
            
            paramsRec.scheduleId = rec.data.scheduleId;
            paramsRec.workOrderId = rec.data.workOrderId;
            
            if(obj.field=="actualStartDate"){
                if(obj.value.between(rec.data.startDate, rec.data.actualEndDate)){
                    asdate = obj.value;
                    aedate = rec.data.actualEndDate;
                    
                    paramsRec.actualStartDate = WtfGlobal.convertToGenericDate(asdate);
                    paramsRec.actualEndDate = WtfGlobal.convertToGenericDate(aedate);
                }else{
                    rec.set("actualStartDate",obj.originalValue);
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.schedule.save.startdate")], 3);
                    return;
                }
            } else if(obj.field=="actualEndDate"){
                if(obj.value.between(rec.data.actualStartDate, rec.data.endDate)){
                    aedate = obj.value;
                    asdate = rec.data.actualStartDate;
                    
                    paramsRec.actualStartDate = WtfGlobal.convertToGenericDate(asdate);
                    paramsRec.actualEndDate = WtfGlobal.convertToGenericDate(aedate);
                    
                }else{
                    rec.set("actualEndDate",obj.originalValue);
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.schedule.save.enddate")], 3);
                    return;
                }
            } else if(obj.field=="statusinfo"){
                paramsRec.status = obj.value;
            } else if(obj.field=="assignedToName"){
                paramsRec.assignedTo = obj.value;
            }
            
            Wtf.Ajax.requestEx({
                    url:"ACCInvoice/updateAssetMaintenanceScheduleAndWorkOrder.do",
                    params: paramsRec                    
            },this,function(){
                
            },function(){
                
            });
            
        }
    },
    
    viewRenderer:function(v,m,rec){
        return "<div class='view pwnd view-gridrow'  title="+WtfGlobal.getLocaleText("acc.create.work.job")+"></div>";
    },
    
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".view-gridrow")){
            var store=grid.getStore(); 
            var total=store.getCount();
            
            var record = store.getAt(rowindex);
            
//            callAssetMaintenanceWorkOrderForm(record)

            this.workOrderForm = new Wtf.account.AssetMaintenanceWorkOrder({
                    title:WtfGlobal.getLocaleText("acc.Workorder.WorkOrder"),
                    layout:'border',
//                    id:'workOrderFormId',
                    resizable:false,
                    iconCls :getButtonIconCls(Wtf.etype.deskera),
                    modal:true,
                    scope:this,
                    scheduleEventRec:record,
                    height:530,
                    width:1000
                });
            this.workOrderForm.show();
            
            this.workOrderForm.on('workOrderSaved',this.reloadStore,this);
            
        } else if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.nee.48"),function(btn){
                if(btn!="yes") {
                    return;
                }
            
                var store=grid.getStore();
                var total=store.getCount();
                var rec = store.getAt(rowindex);
            
                if(rec.get('workJobId') == "" || rec.get('workJobId') == undefined || rec.get('workJobId') == null){
                    Wtf.Ajax.requestEx({
                        url:'ACCInvoice/deleteAssetMaintenanceScheduleEvent.do',
                        params: {
                            scheduleId:rec.get('scheduleId'),
                            hiddenCurrentDate:WtfGlobal.convertToGenericDate(this.hiddenDate.getValue())
                        }
                    },this,this.genSuccessResponse,this.genFailureResponse);
                }else{
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.schedule.event.delete"),function(btn){
                        if(btn!="yes") {
                            return;
                        }
                        
                        Wtf.Ajax.requestEx({
                            url:'ACCInvoice/deleteAssetMaintenanceScheduleEvent.do',
                            params: {
                                scheduleId:rec.get('scheduleId'),
                                hiddenCurrentDate:WtfGlobal.convertToGenericDate(this.hiddenDate.getValue())
                            }
                        },this,this.genSuccessResponse,this.genFailureResponse);
                        
                    },this);
                }
            
            }, this);
        }
    },
    
    storeLoaded:function(){
//        this.fireEvent('storeloaded',this);
    },
    
    reloadStore:function(c){
        this.maintenanceStore.reload();
    },
    
    addStatus:function(){
        addMasterItemWindow('24')
    },
    
    addAssignedTo:function(){
        addMasterItemWindow('23')
    },
    
//    deleteMaintenanceScheduleEvents:function(){
//        var recArray=this.FADetailsGrid.getSelectionModel().getSelections();
//        if(this.FADetailsGrid.getSelectionModel().hasSelection()==false||this.FADetailsGrid.getSelectionModel().getCount()>1){
//            WtfComMsgBox(['Information','Please select a record first.'],0);
//            return;
//        }
//        
//        var rec = recArray[0];
//        
//        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.nee.48"),function(btn){
//            if(btn!="yes") {
//                return;
//            }
//            
//            if(rec.get('workJobId') == "" || rec.get('workJobId') == undefined || rec.get('workJobId') == null){
//                Wtf.Ajax.requestEx({
//                    url:'ACCInvoice/deleteAssetMaintenanceScheduleEvent.do',
//                    params: {
//                        scheduleId:rec.get('scheduleId'),
//                        hiddenCurrentDate:WtfGlobal.convertToGenericDate(this.hiddenDate.getValue())
//                    }
//                },this,this.genSuccessResponse,this.genFailureResponse);
//            }else{
//                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.schedule.event.delete"),function(btn){
//                    if(btn!="yes") {
//                        return;
//                    }
//                        
//                    Wtf.Ajax.requestEx({
//                        url:'ACCInvoice/deleteAssetMaintenanceScheduleEvent.do',
//                        params: {
//                            scheduleId:rec.get('scheduleId'),
//                            hiddenCurrentDate:WtfGlobal.convertToGenericDate(this.hiddenDate.getValue())
//                        }
//                    },this,this.genSuccessResponse,this.genFailureResponse);
//                        
//                },this);
//            }
//            
//            
//        },this);
//    },
    
    genSuccessResponse:function(response){
        WtfComMsgBox(['Maintenance Schedule',response.msg],response.success*2+1);
        this.maintenanceStore.reload();
    },
    
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.loaddata();
            this.maintenanceStore.on('load',this.storeloaded,this);
        }
    },
    
    loaddata:function(){
        
        this.maintenanceStore.baseParams.fromDate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        this.maintenanceStore.baseParams.toDate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        
        if(this.isReport){
            this.maintenanceStore.baseParams.assetId = this.assetCmb.getValue();
        }
        
//        var assetScheduleId=(this.assetRec)?this.assetRec.get('assetdetailId'):'';
//        var contractScheduleId=(this.scheduleId)?this.scheduleId:'';
//        if(!(this.scheduleId=='' && contractScheduleId=='')){
            this.maintenanceStore.load({params:{start: 0, limit: this.pP.combo.value,pagingFlag:true}});
//        }
        
    },
    
    storeloaded:function(store){
        this.quickPanelSearch.StorageChanged(store);
    }
    
    
});