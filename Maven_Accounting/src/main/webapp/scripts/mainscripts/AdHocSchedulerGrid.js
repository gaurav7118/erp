
Wtf.account.AdHocSchedulerGrid = function(config){
    Wtf.apply(this,config);
    Wtf.account.AdHocSchedulerGrid.superclass.constructor.call(this,config);
    
//    this.addEvents({
//        'storeloaded':true//event will be fire when data will be saves successfully.
//    });
}

Wtf.extend(Wtf.account.AdHocSchedulerGrid, Wtf.Panel,{
    
    onRender:function(config){
        Wtf.account.AdHocSchedulerGrid.superclass.onRender.call(this,config);
        
        // Create Grid Panel
        this.createGridPanel();
        
//        this.addGridRec();
        
        this.add(this.centerPanel=new Wtf.Panel({
                border: false,
                region: 'center',
                id: 'centerpan1'+this.id,
                autoScroll:true,
//                bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
                baseCls:'bckgroundcolor',
                layout: 'fit',
                items:[this.schedulerEventGrid]
//                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
//                    pageSize: 30,
//                    id: "pagingtoolbar1" + this.id,
//                    store: this.maintenanceStore,
//                    searchField: this.quickPanelSearch,
//                    displayInfo: true,
////            displayMsg: 'Displaying records {0} - {1} of {2}',
//                    emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
//                    plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id})
//                })
            })
        );
        
    },
    
    createGridPanel:function(){
        
        this.adhocRecord = new Wtf.data.Record.create([
//            {name:'scheduleId'},
            {name:'eventStartDate',type:'date'},
            {name:'eventEndDate',type:'date'}
        ]);
        
        this.adhocRecordReader = new Wtf.data.KwlJsonReader({
            root:'data'
        },this.adhocRecord);
        
        this.rowNo=new Wtf.grid.RowNumberer();
        
        this.maintenanceStore = new Wtf.data.Store({
            url:'',
            reader:this.adhocRecordReader
        });
        
        var FixedAssetDetailArr = [];
        
        FixedAssetDetailArr.push(this.rowNo,{
            header:WtfGlobal.getLocaleText("acc.assetworkorder.StartDate"),
            dataIndex:'eventStartDate',
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            editor:new Wtf.form.DateField({
                maxLength:250,
                format:WtfGlobal.getOnlyDateFormat(),
                xtype:'datefield'
            })
        },{
            header:WtfGlobal.getLocaleText("acc.assetworkorder.EndDate"),
            dataIndex:'eventEndDate',
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            editor:new Wtf.form.DateField({
                maxLength:250,
                format:WtfGlobal.getOnlyDateFormat(),
                xtype:'datefield'
            })
        }
     );
         
         this.FACM = new Wtf.grid.ColumnModel(FixedAssetDetailArr);
         
         this.addGridRec();
        
        this.schedulerEventGrid = new Wtf.grid.EditorGridPanel({
//            layout:'fit',
            clicksToEdit:1,
            autoScroll:true,
//            sm:this.sm,
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
        
        this.schedulerEventGrid.on('afteredit',this.updateRow,this);
        
    },
    
    updateRow:function(obj){
        if(obj!=null){
            var rec = obj.record;
            if(this.isContract){
                if(this.contractStartDate == null || this.contractStartDate == undefined || this.contractStartDate == '' || this.contractEndDate == null || this.contractEndDate == undefined || this.contractEndDate == ''){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.contract.startenddate")],0);
                    rec.set('eventStartDate',null);
                    rec.set('eventEndDate',null);
                    return;
                }
            }
            if(obj.field=='eventStartDate'){
                
                if(obj.value<this.scheduleStartDate){
                    WtfComMsgBox(['Information','Start Date must be greater or equal to Schedule Start Date'],0);
                    rec.set('eventStartDate',null)
                    return;
                }

                if(this.isContract){
                    if(!(obj.value.between(this.contractStartDate, this.contractEndDate))){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.contract.adhoc.schedulestartdate")], 3);
                        rec.set('eventStartDate',obj.originalValue);
                        return;
                    }
                }

                var durationToAdd = 0;
                
                if(this.scheduleDuration>0){
                    durationToAdd = this.scheduleDuration-1;
                }
                
                rec.set('eventEndDate',obj.value.add(Date.DAY,durationToAdd))
            } else if(obj.field=='eventEndDate'){
                
                if(rec.get('eventStartDate') == undefined || rec.get('eventStartDate') == null || rec.get('eventStartDate') == ''){
                    WtfComMsgBox(['Information','Please Enter Start Date First'],0);
                    rec.set('eventEndDate',null)
                    return;
                }
                
                if(obj.value<rec.get('eventStartDate')){
                    WtfComMsgBox(['Information','End Date must be greater or equal to Start Date'],0);
                    rec.set('eventEndDate',obj.originalValue)
                    return;
                }

                if(this.isContract){
                    if(!(obj.value.between(this.contractStartDate, this.contractEndDate))){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.contract.adhoc.scheduleenddate")], 3);
                        rec.set('eventEndDate',obj.originalValue);
                        return;
                    }
                }
            }
        }
    },
    
    addGridRec:function(obj){
        
        var eventDetailsArrLength = 0;
        
        if(this.eventDetailsArray && this.eventDetailsArray !=''){
            this.eventDetailArr = eval('(' + this.eventDetailsArray + ')');
        }
        
        var iteratorLength = (this.rowCount>eventDetailsArrLength)?this.rowCount:eventDetailsArrLength;
        
        for(var i=0;i<iteratorLength;i++){
            var rec = this.adhocRecord;
            rec = new rec({});
            rec.beginEdit();
            
            var fields=this.maintenanceStore.fields;
            
            for(var x=0;x<fields.items.length;x++){
                if(this.eventDetailArr)
                    this.eventDetailRec = this.eventDetailArr[i];
                
                var value="";
                
                if(this.eventDetailRec){
                    value = new Date(this.eventDetailRec[fields.get(x).name]);
                }
                rec.set(fields.get(x).name, value);
            }
            
            rec.endEdit();
            rec.commit();
            this.maintenanceStore.add(rec);
        }
        
    }
});