Wtf.account.AssetMaintenanceSchedule = function(config){
    this.isEdit = (config.isEdit)?config.isEdit:false;
    this.isFromCreateEditButton = (config.isFromCreateEditButton)?config.isFromCreateEditButton:false;
    this.isFromSaveAndCreateNewButton = (config.isFromSaveAndCreateNewButton)?config.isFromSaveAndCreateNewButton:false;
    this.isFromCreationForm = (config.isFromCreationForm)?config.isFromCreationForm:false;
    this.isContract = (config.isContract)?config.isContract:false;
    this.schedulerFormInformation="";
    this.fromSaveButtonClosed = false;
    Wtf.apply(this,{
        buttons:[this.closeButton = new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),
                    minWidth: 50,
                    scope: this,
                    handler: this.closeOpenWin.createDelegate(this)
            })]
    },config);
    
    Wtf.account.AssetMaintenanceSchedule.superclass.constructor.call(this, config);
    
    this.addEvents({
        'schedulesaved':true,
        'update':true
    });
    
}

Wtf.extend(Wtf.account.AssetMaintenanceSchedule, Wtf.Window,{
    
    onRender:function(config){
        Wtf.account.AssetMaintenanceSchedule.superclass.onRender.call(this,config);
        
        this.createAssetMaintenanceScheduleForm();
        
        this.createAssetMaintenanceScheduleGrid();
        
        
        if(this.isEdit || this.contractScheduleInformation){// if edit case or schedule information is available on contract(in case of Contract Schedule)
            if(!this.isFromSaveAndCreateNewButton){
                this.loadRecord();
            }
        }
        
        this.add({
            region: 'north',
            height: 410,
            border: false,
            bodyStyle: 'border-bottom:1px solid #bfbfbf;',
            baseCls:'bckgroundcolor',
//            bbar:['->',this.saveButton,this.clearButton],
            items:[this.schedulerInformationForm]
        },{
            region: 'center',
            border: false,
            autoScroll:true,
            baseCls:'bckgroundcolor',
//            layout: 'fit',
            items:[this.assetMaintenanceScheduleGrid]
        }
        );
            
        
    },
    resetComponent:function(){
        if(this.adhocGridPanel){
            this.adhocGridPanel.getStore().removeAll();
        }
        this.schedulerInformationForm.getForm().reset();
        this.assetMaintenanceScheduleGrid.getStore().removeAll();
    },
    loadRecord:function(){
        if(this.isEdit && (this.contractScheduleInformation == null || this.contractScheduleInformation == undefined || this.contractScheduleInformation == "")){// if edit case and schedule information is not available on contract(in case of Contract Schedule)
            if(this.scheduleRecord.get('scheduleNumber')){
                this.scheduleNo.setValue(this.scheduleRecord.get('scheduleNumber'));
                this.scheduleNo.disable();
            }
            if (this.scheduleRecord.get('type')) {
                this.type.setValue(this.scheduleRecord.get('type'));
            }
            if(this.scheduleRecord.get('scheduleStartDate')){
                this.startDate.setValue(this.scheduleRecord.get('scheduleStartDate'));
            }
            if(this.scheduleRecord.get('scheduleEndDate')){
                this.endDate.setValue(this.scheduleRecord.get('scheduleEndDate'));
            }
            if(this.scheduleRecord.get('eventDuration')){
                this.scheduleDuration.setValue(this.scheduleRecord.get('eventDuration'));
            }
//            this.firstScheduleEndDate.setValue(this.scheduleRecord.get('firstScheduleEndDate'));
            if(this.scheduleRecord.get('isAdhoc')){
                this.isAdHocSchedule.setValue(this.scheduleRecord.get('isAdhoc'));
            }
            if(this.scheduleRecord.get('frequency')){
                this.repeatInterval.setValue(this.scheduleRecord.get('frequency'));
            }
            if(this.scheduleRecord.get('frequencyType')){
                this.intervalType.setValue(this.scheduleRecord.get('frequencyType'));
            }
            
            if(this.scheduleRecord.get('scheduleStopCondition')!=undefined){
                if(this.scheduleRecord.get('scheduleStopCondition') == 0 || this.scheduleRecord.get('scheduleStopCondition') == 1){
                   if(this.scheduleRecord.get('totalEvents')!="" && this.scheduleRecord.get('totalEvents')!=undefined){
                      this.totalEvents.setValue(this.scheduleRecord.get('totalEvents'));   
                   }                   
                }else if(this.scheduleRecord.get('scheduleStopCondition') == 2){
                    this.endDateRadio.setValue(true);
                    } 
                }
            if(this.scheduleRecord.get('adHocEventDetails')){
                this.adHocEventDetails = this.scheduleRecord.get('adHocEventDetails');
            }
             /*
              * adHocEventDetailsEdit contain json of event date
                and it is used for to display data in event grid 
              */ 
            if(this.scheduleRecord.get('adHocEventDetailsEdit')){
                this.adHocEventDetails = this.scheduleRecord.get('adHocEventDetailsEdit');
            }
            
            
            
        }else if(this.contractScheduleInformation){// if  case of schedule information is available on contract in case of Contract Schedule
            
//            this.scheduleNo.disable();
            this.scheduleNo.setValue(this.contractScheduleInformation.scheduleNumber);
            this.startDate.setValue(new Date(this.contractScheduleInformation.scheduleStartDate));
            this.endDate.setValue(new Date(this.contractScheduleInformation.scheduleEndDate));
            this.scheduleDuration.setValue(this.contractScheduleInformation.scheduleDuration);
//            this.firstScheduleEndDate.setValue(this.scheduleRecord.get('firstScheduleEndDate'));
            this.isAdHocSchedule.setValue(this.contractScheduleInformation.isAdHocSchedule);
            this.repeatInterval.setValue(this.contractScheduleInformation.repeatInterval);
            this.intervalType.setValue(this.contractScheduleInformation.intervalType);
            if(this.contractScheduleInformation.endDateStopCondition){// if schedule ends on behalf of end adate
                this.endDateRadio.setValue(true);
            }else{
                if(this.contractScheduleInformation.totalEvents!="" && this.contractScheduleInformation.totalEvents!=undefined){
                   this.totalEvents.setValue(this.contractScheduleInformation.totalEvents); 
                }                
            }
            this.adHocEventDetails = this.contractScheduleInformation.adHocEventDetails;
        }
    },
    
    createAssetMaintenanceScheduleGrid:function(){
        
        var scheduleId = "";
        
        if(this.scheduleRecord){
            scheduleId = this.scheduleRecord.get('scheduleId');
        }
        
        this.assetMaintenanceScheduleGrid = new Wtf.account.AssetMaintenanceSchedulerReport({
            title:'',
            id:'assetMaintenanceScheduleGridID'+this.id,
//            autoHeight:true,
//            autoWidth:true,
            assetRec:this.assetRec,
            isContract:this.isContract,
            scheduleId:scheduleId,
            isFromCreateButton:this.isFromCreateButton,
            isFromCreationForm:this.isFromCreationForm,
//            autoScroll:true,
            width:(this.isContract)?680:1000,
            height:205,
//            tabTip:'Asset Maintenance Schedule Report',
//            closable:true,
            layout:'border',
//            iconCls :'accountingbase debitnotereport',
            border : false
        }); 
        
//        this.assetMaintenanceScheduleGrid.doLayout();
        
        
//        this.assetMaintenanceScheduleGrid.on('storeloaded', this.maintenanceScheduleReportLoaded, this);
    },
    
    
    createAssetMaintenanceScheduleForm:function(){
        
        
        this.scheduleNo = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.maintenance.schedule.name") + '*',
//            readOnly:true,
            name:'scheduleNumber',
            allowBlank:false,
//            disabled:this.isEdit,
            width:130
        });
        
        this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.schedule.start.date"),//Schedule Start Date
            id:"startDate"+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'startDate',
            value:(this.isContract)?null:new Date(),
            width : 130
        });
        
        this.hiddenDate = new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.schedule.start.date"),//Schedule Start Date
            format:WtfGlobal.getOnlyDateFormat(),
            hideLabel:true,
            hidden:true,
            name: 'hiddenCurrentDate',
            value:Wtf.serverDate,
            width : 130
        });
        
        this.startDate.on('change',this.startDateChanged,this);
        
        
        
        this.scheduleDuration = new Wtf.form.NumberField({
            allowNegative:false,
            hidden:false,
            hideLabel:false,
            allowDecimals:false,
            allowBlank:false,
            maxLength: 5,
            width:130,
            fieldLabel:WtfGlobal.getLocaleText("acc.asset.maintenance.scheduleDuration")+' (In Days)',//Schedule Duration
            name:'scheduleDuration',
            id:"scheduleDuration"+this.id
        });
        var typeArr = new Array();
        typeArr.push(['Regular', '1']);
        typeArr.push(['Breakdown', '2']);
        this.typeStore = new Wtf.data.SimpleStore({
            fields: [{name: 'name'}, {name: 'value'}],
            data: typeArr
        });
        this.typeConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.maintenance.type") + "'>" + WtfGlobal.getLocaleText("acc.maintenance.type") + "</span>",
            name: 'type',
            hiddenName: 'type',
            hidden: this.isContract,
            id: "type" + this.id,
            value: '1'
        };
        this.type = WtfGlobal.createFnCombobox(this.typeConfig, this.typeStore, 'value', 'name', this);
        this.scheduleDuration.on('change',this.schedurationChanged,this);
        
//        this.firstScheduleEndDate = new Wtf.form.DateField({
//            fieldLabel:'First Schedule End Date',
//            id:"firstScheduleEndDate"+this.id,
//            format:WtfGlobal.getOnlyDateFormat(),
//            name: 'firstScheduleEndDate',
//            value:Wtf.serverDate,
//            width : 130
//        });
        
//        this.startDate.on('change',this.updateEndDate,this);
        
        this.isAdHocSchedule= new Wtf.form.Checkbox({
            name:'isAdHocSchedule',
            id:'isAdHocSchedule'+this.id,
            hiddeName:'isAdHocSchedule',
            fieldLabel:WtfGlobal.getLocaleText("acc.so.isadhoc"), //Is ad hoc schedule
            cls : 'custcheckbox',
            width: 10
        });
        
        this.isAdHocSchedule.on('check',this.adHocScheduleHandler,this);
        
        this.repeatInterval = new Wtf.form.NumberField({
            allowNegative:false,
            value:0,
            allowDecimals:false,
            allowBlank:false,
            maxLength: 5,
            width:130,
            fieldLabel:WtfGlobal.getLocaleText("acc.repeat.event.time"),//Repeat Event in every
            name:'repeatInterval',
            id:"eventFrequency"+this.id
        });
        
        this.repeatInterval.on('change',this.setScheduleEndDate,this);
//        this.repeatInterval.on('change',this.updateEndDate,this);
        
        this.intervalTypeStore = new Wtf.data.SimpleStore({
	    fields: ["id", "name"],
	    data :[["day",WtfGlobal.getLocaleText("acc.rem.108")],["week",WtfGlobal.getLocaleText("acc.rem.109")],["month",WtfGlobal.getLocaleText("acc.field.Month")],["year",WtfGlobal.getLocaleText("acc.value.year")]]
	});
        
        
        this.intervalType = new Wtf.form.ComboBox({
            store: this.intervalTypeStore,
            hiddenName:'intervalType',
            displayField:'name',
            valueField:'id',
            mode: 'local',
            value: "day",
            triggerAction: 'all',
            typeAhead:true,
            hideLabel: true,
            labelWidth: 5,
            width: 128,
            selectOnFocus:true
        });
        
//        this.intervalType.on('select',this.updateEndDate,this);
        this.intervalType.on('select',this.setScheduleEndDate,this);
        
        this.totalEvents = new Wtf.form.NumberField({
            allowNegative:false,
            hidden:false,
            hideLabel:false,
            allowDecimals:false,
//            value:0,
//            allowBlank:false,
            maxLength: 5,
            width:130,
            labelSeparator:'',
//            fieldLabel:WtfGlobal.getLocaleText("acc.total.events"),//Total Events
            name:'totalEvents',
            id:"totalEvents"+this.id
        });
        
        this.totalEvents.on('change',this.totalEventsChangeHandler,this);
        
        this.endDate = new Wtf.ExDateFieldQtip({
//            fieldLabel:WtfGlobal.getLocaleText("acc.schedule.end.date"),//Schedule end Date
            labelSeparator:'',
            id:"endDate"+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'endDate',
            value:new Date(),
            width : 130
        });
        
        
        this.endDate.on('change',this.calculateEvents,this);
        
        this.adHocEventsButton = new Wtf.Button({
            text: 'Add Ad-hoc Events',
            tooltip: 'Click to Add Ad-hoc Events',
            hidden:true,
            scope: this,
            handler: this.adHocEventsButtonHandler
        }); 
        /*
         * To edit total event date
         */
        this.editButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.entitydateeditclick.promt"),
            tooltip: WtfGlobal.getLocaleText("acc.entitydateeditclick.promt"),
            scope: this,
            disabled:!this.isEdit,
            handler: this.editButtonHandler
        }); 
        
        
        this.totalEventsRadio = new Wtf.form.Radio({
                name:'totalEventsRadio',
//                disabled:this.isEdit,
                labelAlign : 'right',
                inputValue :'false',
                labelSeparator:'',
                checked:true,
                boxLabel:WtfGlobal.getLocaleText("acc.total.events")
            });
            
            this.totalEventsRadio.on('check',this.enableTotalEvents,this);
            /*
             * To display Edit button after change event of  totalEventsRadio button
             */
            this.totalEventsRadio.on('change',this.enableTotalEventsChange,this); 
        
        this.endDateRadio = new Wtf.form.Radio({
                name:'endDateRadio',
//                disabled:this.isEdit,
                labelAlign : 'right',
                inputValue :'false',
                labelSeparator:'',
                checked:false,
                boxLabel:WtfGlobal.getLocaleText("acc.schedule.end.date")
            });
        
        this.endDateRadio.on('check',this.enableEndDate,this);
        
        this.intervalPanel = new Wtf.Panel({
            layout: "column",
//            disabledClass:"newtripcmbss",
            border: false,
            items:[
                new Wtf.Panel({
                    columnWidth: 0.55,
                    layout: "form",
                    border: false,
                    anchor:'100%',
                    items : [this.repeatInterval]
                }),
                new Wtf.Panel({
                    columnWidth: 0.3,
                    layout: "form",
                    border: false,
                    anchor:'10%',
                    items : [this.intervalType]
                })          
            ]
        });

        
        this.totalEventsPanel = new Wtf.Panel({
            layout: "column",
//            disabledClass:"newtripcmbss",
            border: false,
            items:[
                new Wtf.Panel({
                    columnWidth: 0.25,
                    layout: "form",
                    labelWidth:0,
                    border: false,
                    anchor:'10%',
                    items : [this.totalEventsRadio]
                }),
                new Wtf.Panel({
                    columnWidth: 0.25,
                    layout: "form",
                    border: false,
                    labelWidth:1,
                    anchor:'90%',
                    items : [this.totalEvents]
                }),
                new Wtf.Panel({
                    columnWidth: 0.05,
                    layout: "form",
                    border: false,
                    anchor:'90%',
                    items : [{
                            html:WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.field.leasecontact.totalEvent.Help")),
                            border:false
                        }]
                }),
                new Wtf.Panel({
                    columnWidth: 0.3,
                    layout: "form",
                    border: false,
                    anchor:'10%',
                    items : [this.adHocEventsButton,this.editButton]
                })          
            ]
        });
        
        this.endDatePanel = new Wtf.Panel({
            layout: "column",
//            disabledClass:"newtripcmbss",
            border: false,
            items:[
                new Wtf.Panel({
                    columnWidth: 0.25,
                    layout: "form",
                    labelWidth:0,
                    border: false,
                    anchor:'10%',
                    items : [this.endDateRadio]
                }),
                new Wtf.Panel({
                    columnWidth: 0.28,
                    layout: "form",
                    border: false,
                    labelWidth:1,
                    anchor:'90%',
                    items : [this.endDate]
                })          
            ]
        });
        
        
        this.saveButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.het.805"),
            minWidth: 70,
            scope: this,
//            iconCls: 'pwnd save',
            handler: this.saveButtonHandler.createDelegate(this)
        });
        
        this.clearButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"),
            minWidth: 70,
//            hidden:true,
            scope: this,
            handler: this.clearButtonHandler.createDelegate(this)
        });
        
        this.buttonPanel = new Wtf.Panel({
            layout: "column",
            border: false,
            items:[
                new Wtf.Panel({
                    columnWidth: 0.3,
                    layout: "form",
                    border: false,
                    anchor:'10%',
                    bodyStyle:'margin:10px 0px 0px 100px',
                    items : [this.saveButton]
                }),
                new Wtf.Panel({
                    columnWidth: 0.3,
                    layout: "form",
                    border: false,
                    anchor:'10%',
                    bodyStyle:'margin:10px 0px 0px 0px',
                    items : [this.clearButton]
                })          
            ]
        });
        
        
        this.scheduleFrequencyBlock = new Wtf.form.FieldSet({
            border:false,
            xtype:'fieldset',
            autoWidth:true,
            autoHeight:true,
            title:WtfGlobal.getLocaleText("acc.report.annexure2A.Frequency"),
            defaults:{border:false},
            items:[this.isAdHocSchedule,this.intervalPanel]
        });
        
        this.stopConditionBlock = new Wtf.form.FieldSet({
            border:false,
            xtype:'fieldset',
            autoWidth:true,
            autoHeight:true,
            title:WtfGlobal.getLocaleText("acc.MaintenanceSchedules.StopCondition"),
            defaults:{border:false},
            items:[this.totalEventsPanel,this.endDatePanel]
        });
        if(this.isContract){
            this.fieldArr=[this.scheduleNo,this.startDate,this.hiddenDate,this.scheduleDuration,this.scheduleFrequencyBlock,this.stopConditionBlock,this.buttonPanel]
        }else{
            this.fieldArr=[this.scheduleNo,this.startDate,this.hiddenDate,this.scheduleDuration,this.type,this.scheduleFrequencyBlock,this.stopConditionBlock,this.buttonPanel]
        }
        
        
        this.schedulerInformationForm = new Wtf.form.FormPanel({
            border:false,
            autoWidth:true,
            height:400,
            labelWidth:150,
            anchor:'100%',
            bodyStyle:'margin:20px 40px 40px 40px',
            items:this.fieldArr
        });
    },
    
    closeOpenWin:function(){
        this.fromSaveButtonClosed = false;
        this.close();
    },
    
    enableEndDate:function(c,newVal,oldVal){
        if(newVal){
            this.endDate.enable();
            this.totalEvents.setValue(0);
            this.totalEvents.disable();
            /*
             * Hide button if schedule end date radio button is checked
             */
            this.editButton.hide();
            this.totalEventsRadio.setValue(false);
        }
    },
    
    enableTotalEvents:function(c,newVal,oldVal){
        if(newVal){
            this.totalEvents.enable();
            this.endDate.disable();
            this.endDateRadio.setValue(false);
        }
    },
    /*
     * Show Edit Button After totalevent is changed
     */
    enableTotalEventsChange:function(c,newVal,oldVal){
        this.editButton.show();        
    },
    checkStartDate:function(finalStartDate,durationType,duration){
        if (durationType == "day") {
            finalStartDate=new Date(finalStartDate).add(Date.DAY,duration);
        } else if (durationType == "week") {
            finalStartDate=new Date(finalStartDate.add(Date.DAY,duration*7));
        } else if (durationType == "month") {
            finalStartDate=new Date(finalStartDate).add(Date.MONTH,duration);
        } else if (durationType == "year") {
            finalStartDate=new Date(finalStartDate).add(Date.YEAR,duration);
        }
        return finalStartDate;
    },
    
    saveButtonHandler:function(){
        
//        if(this.isAdHocSchedule.getValue()){
//            this.repeatInterval.allowBlank=true;
//            
//            this.scheduleDuration.allowBlank=false;
//        }else{
//            this.scheduleDuration.allowBlank=true;
//            
//            this.repeatInterval.allowBlank=false;
//        }
        
        this.scheduleNo.setValue(this.scheduleNo.getValue().trim());
        this.fromSaveButtonClosed = true;
        
        if(this.schedulerInformationForm.getForm().isValid()){
            
            var confirmMsg = WtfGlobal.getLocaleText("acc.invoice.msg7");
            
            if(this.isContract){
                
                if(!(this.startDate.getValue().between(this.contractStartDate, this.contractEndDate))){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.contract.scheduledate")], 3);
                    return;
                }
                
                confirmMsg = WtfGlobal.getLocaleText("acc.invoice.msg7")+'<br>'+WtfGlobal.getLocaleText("acc.activity.save.information");
            }
            
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),confirmMsg,function(btn){
                if(btn!="yes") {
                    return;
                }

                if(this.isAdHocSchedule.getValue()){// if ad hoc event
                    if(this.adHocEventDetails == "" || this.adHocEventDetails == undefined || this.adHocEventDetails == null){
                        WtfComMsgBox(['Information','Please Add Ad-hoc events detail'],0);
                        return;
                    }
                    
                    var adHocEventDetailsArray = eval('(' + this.adHocEventDetails + ')');
                    if(adHocEventDetailsArray.length != this.totalEvents.getValue()){
                        WtfComMsgBox(['Information','Total Events entered by you is not equal to Ad-Hoc Event schedules'],0);
                        return;
                    }
                    
                }
                
                var assetDetailId = "";
                
                if(!this.isContract){
                    assetDetailId = this.assetRec.get('assetdetailId');
                }
                
                var rec = this.schedulerInformationForm.getForm().getValues();
                /*
                 * If Edit total  Event check  total event and enter event
                 */
                if(this.scheduleRecord!=undefined && this.scheduleRecord.get("isScheduleEdit")&&!this.endDateRadio.getValue()){
                    if(this.adHocEventDetails == "" || this.adHocEventDetails == undefined || this.adHocEventDetails == null){
                        WtfComMsgBox(['Information',WtfGlobal.getLocaleText("acc.entitydateedit.promt")],0);
                        return;
                    }
                    
                    var adHocEventDetailsArray = eval('(' + this.adHocEventDetails + ')');
                    if(adHocEventDetailsArray.length != this.totalEvents.getValue()){
                        WtfComMsgBox(['Information',WtfGlobal.getLocaleText("acc.entitydateedittotalevent.promt")],0);
                        return;
                    }
                    
                }
                if(this.isContract){                                         //Event created by total events
                    
                    var schStartDate = this.startDate.getValue();           ////Events stsrtdate should not go out of contract end date
                    var schEndDate = this.endDate.getValue();
                    
                    var finalStartDate = this.startDate.getValue();
                    var durationType = this.intervalType.getValue();
                    var duration = this.repeatInterval.getValue();
                    
                    var endDateRadio= this.endDateRadio.getValue();
                    if(endDateRadio==false){
                        for(var i=1;i<this.totalEvents.getValue();i++){
                            finalStartDate = this.checkStartDate(finalStartDate,durationType,duration);
                        }
                    }  
                    if(!(finalStartDate.between(this.contractStartDate,this.contractEndDate))){
                        var cntratStartDate=WtfGlobal.convertToDateOnly(this.contractStartDate);
                        var cntratEndDate=WtfGlobal.convertToDateOnly(this.contractEndDate);
                        WtfComMsgBox(['Information','Total Events entered cannot be completed between '+ cntratStartDate +' and '+cntratEndDate],0);
                        return;
                    }
                    // Schedule End Date is not in between contract dates.
                    if(endDateRadio && !(schEndDate.between(this.contractStartDate,this.contractEndDate))){
                        var cntratStartDate=WtfGlobal.convertToDateOnly(this.contractStartDate);
                        var cntratEndDate=WtfGlobal.convertToDateOnly(this.contractEndDate);
                        WtfComMsgBox(['Information','Schedule End Date should be in between '+ cntratStartDate +' and '+cntratEndDate],0);
                        return;
                    }
                }
                rec.scheduleNumber=this.scheduleNo.getValue();
                rec.type=this.type.getValue();
                rec.scheduleStartDate=WtfGlobal.convertToGenericDate(this.startDate.getValue());
                rec.hiddenCurrentDate=WtfGlobal.convertToGenericDate(this.hiddenDate.getValue());
                rec.scheduleEndDate=WtfGlobal.convertToGenericDate(this.endDate.getValue());
//                rec.firstScheduleEndDate=WtfGlobal.convertToGenericDate(this.firstScheduleEndDate.getValue());
                    rec.isAdHocSchedule=this.isAdHocSchedule.getValue();
                rec.repeatInterval=this.repeatInterval.getValue();
                rec.intervalType=this.intervalType.getValue();
                rec.totalEvents=this.totalEvents.getValue();
                rec.scheduleDuration=this.scheduleDuration.getValue();
                rec.assetId=assetDetailId;
                /*
                 *this.saveBtn is true if user is edit  total event 
                 */
                rec.isScheduleBtn=this.saveBtn;
                rec.adHocEventDetails=this.adHocEventDetails;
                rec.totalEventsStopCondition=this.totalEventsRadio.getValue();
                rec.endDateStopCondition=this.endDateRadio.getValue();
                
                
                
                
                if(this.isEdit){
                    var scheduleId = "";
        
                    if(this.scheduleRecord){
                        scheduleId = this.scheduleRecord.get('scheduleId');
                    }
                    rec.scheduleId=scheduleId;
                }
                
                if(this.isContract){
                    this.schedulerFormInformation = rec;
                    
                    
                    this.close();
                }else{
                    Wtf.Ajax.requestEx({
                        url:"ACCInvoice/saveAssetMaintenanceSchedule.do",
                        params: rec
                    },this,this.genSuccessResponse,this.genFailureResponse);
                }
                
            },this);
        }
    },
    
    genSuccessResponse:function(response, request){
        if(response.success){
            WtfComMsgBox([this.title,response.msg],response.success*2+1);
//            this.fireEvent('datasaved',this);
//            if(this.assetMaintenanceScheduleGrid &&this.assetMaintenanceScheduleGrid.maintenanceStore){
//                this.assetMaintenanceScheduleGrid.maintenanceStore.reload();
//            }
            this.fireEvent('schedulesaved',this);
            this.close();
        }else {
            WtfComMsgBox([this.titlel,response.msg],response.success*2+1);
        }
    },
    
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    clearButtonHandler:function(){
        this.schedulerInformationForm.getForm().reset();
    },
    
    updateEndDate:function(){
        var stDate = this.startDate.getValue();
        
        var repeatInterval = this.repeatInterval.getValue();
        
        var intervalType = this.intervalType.getValue();
        
        var totalEvents = this.totalEvents.getValue();
        
        var totalAddebleDays = 0;
        
        if(intervalType == 'day'){
            totalAddebleDays = repeatInterval*1;
        } else if(intervalType == 'week'){
            totalAddebleDays = repeatInterval*7;
        } else if(intervalType == 'year'){
            
            var stDateYear = stDate.getFullYear();
            
            for(var i=0; i<repeatInterval; i++){
                
                if(((stDateYear+i)%4)==0){
                    totalAddebleDays+=366;
                }else{
                    totalAddebleDays+=365;
                }
                
            }
        }
        
        this.endDate.setValue(stDate.add(Date.DAY, totalAddebleDays*totalEvents)); 
        
    },
    
    adHocScheduleHandler:function(c,val){
        if(val){
            this.repeatInterval.setValue(0);
            this.intervalType.setValue('day');
            this.repeatInterval.disable();
            this.intervalType.disable();
            
            this.adHocEventsButton.show();
            /*
             *If in adhoc case functianality edit button is hide
             */
            this.editButton.hide();
            
            this.endDateRadio.setValue(false);
            this.endDateRadio.disable();
            this.endDate.disable();
            this.endDate.setValue(null);
            
            this.totalEventsRadio.setValue(true);
            this.totalEventsRadio.enable();
            this.totalEvents.enable();
            
        }else{
            this.repeatInterval.enable();
            this.intervalType.enable();
            
            this.adHocEventsButton.hide();
            /*
             *If in normal case functianality edit button is hide
             */
            this.editButton.show();
            
            this.endDateRadio.enable();
        }
    },
    
//    maintenanceScheduleReportLoaded:function(){
//        WtfGlobal.hideFormElement(this.scheduleDuration);
//    },
    
    
    createAdhocEventSchedule : function(){
        
        this.eventCnt = this.totalEvents.getValue();
        var scheduleDuration = this.scheduleDuration.getValue();
        if(scheduleDuration == '' || scheduleDuration == undefined || scheduleDuration == null){
            scheduleDuration = 0;
        }
        
        this.adhocGridPanel = new Wtf.account.AdHocSchedulerGrid({
            id:'adhoc-scheduleGridId',
//            title:'Asset Maintenance Schedule Report',
//            tabTip:'Asset Maintenance Schedule Report',
//            closable:true,
//            assetRec:assetRec,
            layout:'border',
            rowCount:this.eventCnt,
            scheduleStartDate:this.startDate.getValue(),
            scheduleDuration:scheduleDuration,
            eventDetailsArray:this.adHocEventDetails,
            iconCls :'accountingbase debitnotereport',
            border : false,
            contractStartDate:this.contractStartDate,
            contractEndDate: this.contractEndDate,
            isContract:this.isContract
        });
        
        
        this.adHocDetaisWin = new Wtf.Window({
            name:'adhocGridWin',
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            title: this.isAdHocSchedule.getValue()?WtfGlobal.getLocaleText("acc.MaintenanceSchedules.AdhocEventDetails"):WtfGlobal.getLocaleText("acc.entitydateeditclick.promt"),
            buttonAlign: 'right',
            width: 300,
            height:300,
            layout:'fit',
            scope: this,
            draggable:false,
            items:this.adhocGridPanel,
            buttons: [{
                text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                scope: this,
                handler: this.saveAdhocDetails
            },{
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            scope: this,
            handler: function() {
                this.adHocDetaisWin.close();
                /*
                 * If click on cancel button saveBtn is false
                 */
                this.saveBtn=false;
            }
        }]
        });
        
        this.adHocDetaisWin.show();
    },
    
    totalEventsChangeHandler:function(){
        this.setScheduleEndDate();
        if(this.isAdHocSchedule.getValue()){
            this.adHocEventsButtonHandler();
        }
    },
    
    adHocEventsButtonHandler:function(){
        if(this.isAdHocSchedule.getValue()){
            this.createAdhocEventSchedule();
        }
    },
    editButtonHandler:function(){
            this.createAdhocEventSchedule();
    },
    
    saveAdhocDetails:function(){
        
        var returnFlag = false;
        var arr = [];
        /*
         * If event is edit then this.saveBtn is true
         */
        this.saveBtn=true;
        this.adhocGridPanel.maintenanceStore.each(function(record){
            if(record.get('eventStartDate') == ""){
                WtfComMsgBox(['Information',WtfGlobal.getLocaleText("acc.MaintenanceSchedules.PleaseEnteranappropriatevalueforStartDate")],0);
                returnFlag = true;
                return;
            }
            arr.push(this.adhocGridPanel.maintenanceStore.indexOf(record));
        },this);
        
        if(returnFlag){
            return;
        }
        
        if(arr.length != this.eventCnt){
            WtfComMsgBox(['Information','Total events entered by you is <b>'+this.eventCnt+'</b>. which does not match with dates entered.'],0);
            return;
        }
        
        
        this.adHocEventDetails = this.getJSONArray(arr);
        
        this.adHocDetaisWin.close();
    },
    
    getJSONArray:function(arr){
        return WtfGlobal.getJSONArray(this.adhocGridPanel.schedulerEventGrid,true,arr);
    },
    
    schedurationChanged:function(){
        
        this.setScheduleEndDate();
        
//        var dateToSet = this.startDate.getValue();
//        var durationToAdd = 0;
//        
//        if(this.scheduleDuration.getValue()>0){
//            durationToAdd = this.scheduleDuration.getValue()-1;
//        }
//        
//        this.firstScheduleEndDate.setValue(dateToSet.add(Date.DAY,durationToAdd));
    },
    
    
    setScheduleEndDate : function(){
        if(this.isAdHocSchedule.getValue()){// if adhoc schedule then no need to calculate schhedule end date
            return;
        }
        
        if(this.endDateRadio.getValue() == true){// if schedule ends on end date then no need to calculate schhedule end date
            return;
        }
        
        var totalEvents = this.totalEvents.getValue();
        var frequency = this.repeatInterval.getValue();
        if(frequency == "" || frequency == undefined || frequency == null){
            frequency = 0;
        }
        
        var frequencyType = this.intervalType.getValue();
        
        var scheduleStartDate = this.startDate.getValue();
        var scheduleEndDate = this.startDate.getValue();
        var eventEndDate = this.startDate.getValue();
        
        var scheduleDuration = this.scheduleDuration.getValue();
        
        if(totalEvents != undefined && totalEvents>0){
            for(var i=0;i<totalEvents;i++){
                scheduleStartDate = this.startDate.getValue();
                if(frequencyType == 'day'){
                    scheduleStartDate = scheduleStartDate.add(Date.DAY,i*frequency);
                } else if(frequencyType == 'week'){
                    scheduleStartDate = scheduleStartDate.add(Date.DAY,i*frequency*7);
                } else if(frequencyType == 'month'){
                    scheduleStartDate = scheduleStartDate.add(Date.MONTH,i*frequency);
                } else if(frequencyType == 'year'){
                    scheduleStartDate = scheduleStartDate.add(Date.YEAR,i*frequency);
                }
                
                var eventStartDate = scheduleStartDate;
                
                eventEndDate = eventStartDate.add(Date.DAY,scheduleDuration-1);//inclusing both days
                
            }
            scheduleEndDate = eventEndDate;
        }
        
        this.endDate.setValue(scheduleEndDate);
        
    },
    
    calculateEvents:function(c,newVal,oldVal){
        
        
        if(newVal<this.startDate.getValue()){
            WtfComMsgBox(['Information','Schedule End Date cannot be greator than Schedule Start Date'],0);
            this.endDate.setValue(oldVal);
            return;
        }
        
        
//        var startDate = this.startDate.getValue();
//        
//        var endDate = newVal;
//        
//        var timeDiff = Math.abs(endDate.getTime() - startDate.getTime());
//        
//        var diffDays = Math.ceil(timeDiff / (1000 * 3600 * 24));
//        
//        var frequency = this.repeatInterval.getValue();
//        
//        var frequencyType = this.intervalType.getValue();
        
        
        
    },
    
    startDateChanged:function(c,newVal,oldVal){
        if(this.isContract){
            if(this.contractStartDate == null || this.contractStartDate == undefined || this.contractStartDate == '' || this.contractEndDate == null || this.contractEndDate == undefined || this.contractEndDate == ''){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.contract.startenddate")],0);
                this.startDate.setValue(oldVal);
                return;
            }
            
            if(!(newVal.between(this.contractStartDate, this.contractEndDate))){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.contract.scheduledate")], 3);
                this.startDate.setValue(oldVal);
            }
            
        }
        this.setScheduleEndDate();
            }
    
});
