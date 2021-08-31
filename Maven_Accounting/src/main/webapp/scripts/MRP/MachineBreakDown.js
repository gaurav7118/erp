/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.MachineBreakDown = function(config) {
    
    this.moduleid = config.moduleid;
    this.id = config.id;
    this.isEdit = false;
    this.fieldWidth=300;
    this.listWidth=200;
    Wtf.apply(this, config);
    this.createStore();
    this.createFields();
    this.createForm();
    Wtf.MachineBreakDown.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.MachineBreakDown, Wtf.Panel, {
    loadRecord: function() {



    },
    onRender: function(config) {
        Wtf.MachineBreakDown.superclass.onRender.call(this, config);
        this.saveBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
            tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
            scope: this,
            hidden: this.readOnly,
            iconCls: getButtonIconCls(Wtf.etype.save),
            handler: function() {
                this.saveAndCreateNewFlag = false;
                this.save();
            }
        });
        this.btnArr = [];
        this.btnArr.push(this.saveBttn);
        if (!this.isEdit) {
            this.savencreateBttn = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNew"),
                tooltip: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNewToolTip"),
                scope: this,
                hidden: this.readOnly,
                handler: function() {
                    this.saveAndCreateNewFlag = true;                    // This flag is used to differentiate between Save button and Save and Create New button
                    this.save();
                },
                iconCls: 'pwnd save'
            });
            this.btnArr.push(this.savencreateBttn);
        }
        this.newPanel = new Wtf.Panel({
            autoScroll: true,
            bodyStyle: ' background: none repeat scroll 0 0 #DFE8F6;',
            region: 'center',
            items: [this.machineForm],
            bbar: this.btnArr

        });
        this.add(this.newPanel);
    },
    createStore: function() {
        
        this.workCenterRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
       
        this.workCenterStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid:37
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.workCenterRec)
        });
        
    },
    createFields: function() {

         this.machineName= new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.machine.machinename.toolTip") +"'>"+ WtfGlobal.getLocaleText("acc.machine.machinename")+"*" +"</span>",
            hiddenName:'mastermachinename',
            name:'machinename',
            id:"machinename"+this.id,
            store:Wtf.vendorAccRemoteStore, // remote store
            valueField:'accid',
            displayField:'accname',
            allowBlank:false,
            extraFields:[],
            listWidth:this.listWidth,
            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.inv.ven") , 
            mode: 'remote', 
            typeAheadDelay:30000,
            minChars:1,
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            width:this.fieldWidth,
            triggerAction:'all',
        });
         this.machineID= new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.machinebreakdown.field.machineID") +"'>"+ WtfGlobal.getLocaleText("acc.machinebreakdown.field.machineID")+"*" +"</span>",
            hiddenName:'machineid',
            name:'machineid',
            id:"machineid"+this.id,
            store:Wtf.vendorAccRemoteStore, // remote store
            valueField:'accid',
            displayField:'accname',
            allowBlank:false,
            extraFields:[],
            listWidth:this.listWidth,
            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.inv.ven") , 
            mode: 'remote', 
            typeAheadDelay:30000,
            minChars:1,
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            width:this.fieldWidth,
            triggerAction:'all',
        });
         this.breakDownType= new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.machinebreakdown.field.breakDownType") +"'>"+ WtfGlobal.getLocaleText("acc.machinebreakdown.field.breakDownType")+"*" +"</span>",
            hiddenName:'breakdowntypemachine',
            name:'breakdowntypemachine',
            id:"breakdowntypemachine"+this.id,
            store:Wtf.vendorAccRemoteStore, // remote store
            valueField:'accid',
            displayField:'accname',
            allowBlank:false,
            extraFields:[],
            listWidth:this.listWidth,
            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.inv.ven") , 
            mode: 'remote', 
            typeAheadDelay:30000,
            minChars:1,
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            width:this.fieldWidth,
            triggerAction:'all',
        });
        
         this.repairType= new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.machinebreakdown.field.repairType") +"'>"+ WtfGlobal.getLocaleText("acc.machinebreakdown.field.repairType")+"*" +"</span>",
            hiddenName:'repairtypemachine',
            name:'repairtype',
            id:"repairtype"+this.id,
            store:Wtf.vendorAccRemoteStore, // remote store
            valueField:'accid',
            displayField:'accname',
            allowBlank:false,
            extraFields:[],
            listWidth:this.listWidth,
            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.inv.ven") , 
            mode: 'remote', 
            typeAheadDelay:30000,
            minChars:1,
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            width:this.fieldWidth,
            triggerAction:'all',
        });
        
        this.gatePass = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machinebreakdown.field.gatePass"),
            name: 'gatepass',
            maxLength: 100,
            width:this.fieldWidth,
            scope: this,
            allowBlank: true
        });
        this.previousRepair = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machinebreakdown.field.previousRepair"),
            name: 'previousrepair',
            maxLength: 100,
            width:this.fieldWidth,
            scope: this,
            allowBlank: true
        });
        
         this.dateOfMachineShipment = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machinebreakdown.dateOfMachineShipment"),
            name: 'dateofmachineshipment',
            hiddenName: 'dateofmachineshipmentdate',
            format: WtfGlobal.getOnlyDateFormat(),
            maxLength: 255,
            width:this.fieldWidth,
            scope: this
        });
        
         this.dateOfMachineArrival = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machinebreakdown.dateOfMachineArrival"),
            name: 'dateofmachinearrival',
            hiddenName: 'dateofmachinearrivaldate',
            format: WtfGlobal.getOnlyDateFormat(),
            maxLength: 255,
             width:this.fieldWidth,
            scope: this
        });
         this.ongoingWorkOrder = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machinebreakdown.field.onGoingWorkOrder"),
            name: 'ongoingworkorder',
            hiddenName: 'ongoingworkorderdate',
            format: WtfGlobal.getOnlyDateFormat(),
            maxLength: 255,
            width:this.fieldWidth,
            scope: this
        });

        this.workCenter = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.machine.workCenter"),
            name: 'workcentercombo',
            store: this.workCenterStore,
            valueField: 'id',
            displayField: 'name',
            allowBlank: false,
            mode: 'local',
            typeAhead: true,
            triggerAction: 'all',
            width:this.fieldWidth,
            listWidth:this.listWidth,
            extraFields:[],
            addNoneRecord: true
        });
        //this.workCenterCombo.addNewFn=this.addWorkCentre.createDelegate(this);
        this.machineLocation = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machinebreakdown.field.machineLocation"),
            name: 'machinelocation',
            maxLength: 100,
             width:this.fieldWidth,
            scope: this,
            allowBlank: true
        });
        this.inWarrenty = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machinebreakdown.field.inWarrenty"),
            name: 'inwarrenty',
            maxLength: 100,
            width:this.fieldWidth,
            scope: this,
            allowBlank: true
        });
        
        this.otherRemarks=new Wtf.form.TextArea({
            fieldLabel:"Other Remarks",
            name: 'memo',
            id:"memo"+this.heplmodeid+this.id,
            height:40,
            width:this.fieldWidth,
            readOnly:this.isViewTemplate,
            maxLength:2048,
            listeners: {
                render: function(c){
                    Wtf.QuickTips.register({
                        target: c.getEl(),
                        text: c.qtip
                        
                    });
                }
            }
        });


    },
    setNextNumber: function(config) {

    },
    getNextSequenceNumber: function(a, val) {
    },
    createForm: function() {
        this.machineForm = new Wtf.form.FormPanel({
            region: "north",
            autoHeight: true,
            border: false,
            defaults: {
                border: false
            },
            split: true,
            layout: 'form',
            baseCls: 'northFormFormat',
            disabledClass: "newtripcmbss",
            hideMode: 'display',
            id: this.id + 'Northform',
            cls: "visibleDisabled",
            labelWidth: 140,
            items: [
                {
                    xtype: 'panel',
                    id: this.id + 'requiredfieldmessagepanel',
                    hidden: true,
                    cls: 'invalidfieldinfomessage'
                }, {
                    layout: 'column',
                    defaults: {
                        border: false
                    },
                    items: [{
                            layout: 'form',
                            columnWidth: 0.49,
                            items: [this.machineName,
                                this.machineID,
                                this.breakDownType,
                                this.repairType,
                                this.dateOfMachineShipment,
                                this.dateOfMachineArrival,
                                this.workCenter
                                
                            ]
                        }, {
                            layout: 'form',
                            columnWidth: 0.49,
                            items: [this.ongoingWorkOrder,
                                    this.machineLocation,
                                    this.inWarrenty,
                                    this.otherRemarks
                             
                            ]
                        }]
                }
            ]
        });

    },
    save: function() {

        var isValidForm = this.machineForm.getForm().isValid();

        if (!isValidForm) {
            WtfGlobal.dispalyErrorMessageDetails(this.id + "requiredfieldmessagepanel", this.getInvalidFields());
            this.machineForm.doLayout();
            return;
        } else {
            Wtf.getCmp(this.id + "requiredfieldmessagepanel").hide();
        }
        if (isValidForm) {
            this.saveData();
        }

    },
    genSuccessResponse: function(response, request) {

        if (response.success) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.success"),
                msg: WtfGlobal.getLocaleText("acc.machine.savemsg"),
                width: 450,
                scope: {
                    scopeObj: this
                },
                buttons: Wtf.MessageBox.OK,
                animEl: 'mb9',
                icon: Wtf.MessageBox.INFO
            });
        }

        if (this.saveAndCreateNewFlag) {
            this.enableSaveButton();
            this.resetComponents();

        } else {
            this.disableComponents();
        }


    },
    genFailureResponse: function(response) {
        Wtf.MessageBox.hide();
        this.enableSaveButton();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if (response.msg) {
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);

    },
    getInvalidFields: function() {
        var invalidFields = []
        this.machineForm.getForm().items.filterBy(function(field) {
            if (field.validate())
                return;
            invalidFields.push(field);
        });
        return invalidFields;
    },
    saveData: function() {
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.savdat"),
            msg: WtfGlobal.getLocaleText("acc.je.msg1"),
            scope: this,
            buttons: Wtf.MessageBox.YESNO,
            icon: Wtf.MessageBox.INFO,
            width: 300,
            fn: function(btn) {
                if (btn != "yes") {
                    this.enableSaveButton();
                    return;
                }
                var jsonObject = this.machineForm.form.getValues();
                WtfGlobal.onFormSumbitGetDisableFieldValues(this.machineForm.form.items, jsonObject);
                this.disableSaveButton();
                WtfComMsgBox(27, 4, true);
                this.ajxUrl = "ACCMachineMasterCMN/getMachineMasterDetails.do";
                Wtf.Ajax.requestEx({
                    url: this.ajxUrl,
                    params: jsonObject
                }, this, this.genSuccessResponse, this.genFailureResponse);
            }
        }, this);
    },
    enableSaveButton: function() {
        this.saveBttn.enable();
        this.savencreateBttn.enable();
    },
    disableSaveButton: function() {
        this.saveBttn.disable();
        this.savencreateBttn.disable();
    },
    disableComponents: function() {
        if (this.machineForm) {
            this.machineForm.disable();
        }
    },
    resetComponents: function() {
        if (this.machineForm) {
            try {
                this.machineForm.getForm().reset();
            } catch (e) {
            }
        }
    },
    addWorkCentre:function(){
       addMasterItemWindow('37');
    }

});