Wtf.account.MaintenanceSchedulers = function(config){
    Wtf.apply(this,config);
    
    
    Wtf.account.MaintenanceSchedulers.superclass.constructor.call(this,config);
    
}

Wtf.extend(Wtf.account.MaintenanceSchedulers, Wtf.Window,{
    scope: this,
    layout: 'border',
    modal:true,
    width: 800,
    height: 500,
    iconCls: getButtonIconCls(Wtf.etype.deskera),  //'pwnd favwinIcon',
//    id: 'maintenance_schedulerId',
    title: WtfGlobal.getLocaleText("acc.MaintenanceSchedules.MaintenanceSchedules"),
    
    initComponent: function() {
        Wtf.account.MaintenanceSchedulers.superclass.initComponent.call(this);
        
        this.addButton({text:WtfGlobal.getLocaleText("acc.CANCELBUTTON"), id:'closebtn'+this.id}, function(e){
            this.close();
        },this);
        
    },
    
    onRender:function(config){
        Wtf.account.MaintenanceSchedulers.superclass.onRender.call(this,config);
        
        // Create Grid Panel
        this.createGridPanel();
        
        this.add(this.centerPanel=new Wtf.Panel({
                border: false,
                region: 'center',
                id: 'centerpan12'+this.id,
                autoScroll:true,
//                bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
                baseCls:'bckgroundcolor',
                layout: 'fit',
                items:[this.FADetailsGrid],
                tbar:[this.quickPanelSearch,this.resetBttn,this.createNewButton, (!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.updateamaint))?this.editButton:"",(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.deleteamaint))?this.deleteButton:"",this.hiddenDate],
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar12" + this.id,
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
    
    closeOpenWin:function(){
        this.close();
    },
    
    createGridPanel:function(){
        this.schedulerRecord = new Wtf.data.Record.create([
            {name:'scheduleId'},
            {name:'scheduleNumber'},
            {name:'assetDetailsId'},
            {name:'assetName'},
            {name:'scheduleStartDate',type:'date'},
            {name:'firstScheduleEndDate',type:'date'},
            {name:'scheduleEndDate',type:'date'},
            {name:'frequency'},
            {name:'isAdhoc'},
            {name:'frequencyType'},
            {name:'totalEvents'},
            {name:'adHocEventDetails'},
            {name:'scheduleStopCondition'},
            {name:'maintenancetype'},
            {name:'type'},
            {name:'eventDuration'}
        ]);
        
        this.schedulerReader = new Wtf.data.KwlJsonReader({
            root:'data'
        },this.schedulerRecord);
        
        this.rowNo=new Wtf.grid.RowNumberer();
        this.sm = new Wtf.grid.CheckboxSelectionModel({
             singleSelect:true
        });
        
        this.maintenanceStore = new Wtf.data.Store({
            url:'ACCInvoiceCMN/getMaintenanceSchedule.do',
            reader:this.schedulerReader,
            baseParams:{
                assetId:this.assetRec.get('assetdetailId')
            }
        });
        
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.rem.5")+" "+WtfGlobal.getLocaleText("acc.maintenance.schedule.name"),
            width: 150,
            id:"quickSearch"+this.id,
            field: 'scheduleNumber',
            Store:this.maintenanceStore
        });
        
        this.maintenanceStore.load();
        
        var FixedAssetDetailArr = [];
        
        FixedAssetDetailArr.push(this.sm,this.rowNo,{
                header:WtfGlobal.getLocaleText("acc.maintenance.schedule.name"),
                dataIndex:'scheduleNumber'
            },{
                header:WtfGlobal.getLocaleText("acc.Contract.startDate"),
                dataIndex:'scheduleStartDate',
                renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.contractActivityPanel.EndDate"),
                dataIndex:'scheduleEndDate',
                renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.total.events"),
                dataIndex:'totalEvents'
            },{
                header:WtfGlobal.getLocaleText("acc.MaintenanceSchedules.EventDuration"),
                dataIndex:'eventDuration'
            },
            {
                header:WtfGlobal.getLocaleText("acc.machineMaintenance.MaintenanceType"),
                dataIndex:'maintenancetype'
            }
        );
            
            this.FACM = new Wtf.grid.ColumnModel(FixedAssetDetailArr);
            
            this.FADetailsGrid = new Wtf.grid.GridPanel({
    //            layout:'fit',
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
                    emptyText:'No Record to display'
                }
            });
            
            
        this.createNewButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("mrp.workorder.report.addworkorder"),
            minWidth: 50,
            scope: this,
            iconCls :getButtonIconCls(Wtf.etype.add),
            handler: this.createMaintenanceSchedule.createDelegate(this)
        });
            
        this.editButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.MaintenanceSchedules.EditSchedule"),
            minWidth: 50,
            iconCls :getButtonIconCls(Wtf.etype.edit),
            scope: this,
            handler: this.editSchedule.createDelegate(this)
        });
            
        this.deleteButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.lp.deletesalescondo"),
            minWidth: 50,
            scope: this,
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            handler: this.deleteMaintenanceSchedule.createDelegate(this)
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
            
    },
    
    deleteMaintenanceSchedule:function(){
        var recArray=this.FADetailsGrid.getSelectionModel().getSelections();
        if(this.FADetailsGrid.getSelectionModel().hasSelection()==false||this.FADetailsGrid.getSelectionModel().getCount()>1){
            WtfComMsgBox(['Information','Please select a record first.'],0);
            return;
        }
        
        var rec = recArray[0];
        
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.nee.48"),function(btn){
            if(btn!="yes") {
                return;
            }
            
            Wtf.Ajax.requestEx({
                url:'ACCInvoiceCMN/isMaintenanceScheduleHasWorkerOrderLinkedWithIt.do',
                params: {
                    schedulerObjectId:rec.get('scheduleId')
                }
            },this,function(res){
                
                if(res.isMaintenanceScheduleHasWorkerOrderLinkedWithIt){
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.schedule.delete"),function(btn){
                        if(btn!="yes") {
                            return;
                        }
                        
                        Wtf.Ajax.requestEx({
                            url:'ACCInvoice/deleteAssetMaintenanceScheduleObject.do',
                            params: {
                                scheduleId:rec.get('scheduleId'),
                                hiddenCurrentDate:WtfGlobal.convertToGenericDate(this.hiddenDate.getValue())
                            }
                        },this,this.genSuccessResponse,this.genFailureResponse);
                        
                    },this);
                }else{
                    Wtf.Ajax.requestEx({
                        url:'ACCInvoice/deleteAssetMaintenanceScheduleObject.do',
                        params: {
                            scheduleId:rec.get('scheduleId'),
                            hiddenCurrentDate:WtfGlobal.convertToGenericDate(this.hiddenDate.getValue())
                        }
                    },this,this.genSuccessResponse,this.genFailureResponse);
                }
                
            },function(){
                
            });
            
            
        },this);
    },
    
    genSuccessResponse:function(response){
        WtfComMsgBox(['Maintenance Schedule',response.msg],response.success*2+1);
        this.maintenanceStore.reload();
    },
    
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    createMaintenanceSchedule:function(){
        
        
        this.maintenanceScheduler = new Wtf.account.AssetMaintenanceSchedule({
            title:WtfGlobal.getLocaleText("acc.asset.maintenance.scheduler"),
            layout:'border',
//            id:'maintenanceSchedulerId',
            resizable:false,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            modal:true,
            assetRec:this.assetRec,
            isFromCreateButton:true,
            isFromCreationForm:true,
//            schedulerRec:rec,
            autoScroll:true,    
            height:600,
            width:700
        });
        
        this.maintenanceScheduler.on('schedulesaved',this.reloadStore,this);
        
        this.maintenanceScheduler.show();
    },
    
    reloadStore:function(){
        this.maintenanceStore.reload();
    },
    
    editSchedule:function(){
        
        var recArray=this.FADetailsGrid.getSelectionModel().getSelections();
        if(this.FADetailsGrid.getSelectionModel().hasSelection()==false||this.FADetailsGrid.getSelectionModel().getCount()>1){
            WtfComMsgBox(['Information','Please select a record first.'],0);
            return;
        }
        
        var rec = recArray[0];
        
        this.maintenanceScheduler = new Wtf.account.AssetMaintenanceSchedule({
            title:WtfGlobal.getLocaleText("acc.asset.maintenance.scheduler"),
            layout:'border',
            id:'maintenanceSchedulerId',
            resizable:false,
            isEdit:true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            modal:true,
            assetRec:this.assetRec,
            isFromCreationForm:true,
            scheduleRecord:rec,
            height:700,
            width:700
        });
    
        this.maintenanceScheduler.on('schedulesaved',this.reloadStore,this);
        
        this.maintenanceScheduler.show();
    },
    
    loaddata:function(){
        this.maintenanceStore.load({params: {ss: this.quickPanelSearch.getValue(), start: 0, limit: this.pP.combo.value,pagingFlag:true}});
    },
    
    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.loaddata();
            this.maintenanceStore.on('load',this.storeloaded,this);
        }
    },
        
    storeloaded:function(store){
        this.quickPanelSearch.StorageChanged(store);
    }
    
});