/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

Wtf.MachineForm = function(config) {
    this.moduleid = config.moduleid;
    this.id = config.id;
    this.isSubstituteMachine = config.isSubstituteMachine;
    this.isEdit = config.isEdit;
    this.record = config.record;
    Wtf.apply(this, config);
    this.createStore();
    this.createFields();
    this.createCustomFields();
    this.createForm();
    this.sequenceFormatStore.on('load',this.loadData,this);
    this.isAssetMachine.on('check',this.hideShowAssetorLease,this);
    this.hasMachineOnLease.on('expand',this.resetLeaseFieldsValues,this);
    this.hasMachineOnLease.on('collapse',this.resetLeaseFieldsValues,this);
    Wtf.MachineForm.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.MachineForm, Wtf.Panel, {
    loadData: function() {
        if (this.record != null && this.isEdit) {
            var data = this.record.data;
            this.machineForm.getForm().loadRecord(this.record);
            this.setComboValues(data);
        }
    },
    onRender: function(config) {
        Wtf.MachineForm.superclass.onRender.call(this, config);

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
        this.sequenceFormatStoreRec = new Wtf.data.Record.create([
            {
                name: 'id'
            },
            {
                name: 'value'
            },
            {
                name: 'oldflag'
            }
        ]);
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            }, this.sequenceFormatStoreRec),
            url: "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams: {
                mode: "automachineid",
                isEdit: this.isEdit
            }
        });
        this.sequenceFormatStore.load();
        this.sequenceFormatStore.on('load', this.setNextNumber, this);

        this.purchaseAccRec = Wtf.data.Record.create([
            {name: 'accid'},
            {name: 'accname'},
            {name: 'acccode'},
            {name: 'hasAccess'}

        ]);

        this.purchaseAccStore = new Wtf.data.Store({
            url: "ACCAccountCMN/getAccountsForCombo.do",
            baseParams: {
                mode: 2,
                ignorecustomers: true,
                ignorevendors: true,
                nondeleted: true,
                controlAccounts: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.purchaseAccRec)
        });


        this.processRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);

        this.workCenterRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
        
        this.activeMachineRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'machinename'}
        ]);
       
        this.workCenterStore = new Wtf.data.Store({
            url: "ACCWorkCentreCMN/getWorkCentreForCombo.do",
            baseParams: {
                //mode: 112,
                //groupid:371
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.workCenterRec)
        });
        
        this.processStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 36
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.processRec)
        });
        
       this.activeMachineStore = new Wtf.data.Store({
            url: "ACCMachineMaster/getActiveSubstituteMachines.do",
            baseParams: {
                isactivemachine:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.activeMachineRec)
        });

        this.assetGroupStore = Wtf.FixedAssetStore;
        this.assetsComboRec = Wtf.data.Record.create([
            {
                name: 'assetdetailId'
            }, {
                name: 'assetGroup'
            }, {
                name: 'assetGroupId'
            }, {
                name: 'assetId'
            }]);

        this.assetsComboStore = new Wtf.data.Store({
            url: "ACCAsset/getAssetDetails.do",
            baseParams: {
            },
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'totalCount',
                root: "data"
            }, this.assetsComboRec)
        });
        
        /* Lease Management Store */
        
         this.depreciationStore = new Wtf.data.SimpleStore({
            fields : ['depreciationmethodvalue', 'depreciationMethodType'],
            data : [
                [1, WtfGlobal.getLocaleText("acc.asset.depreciation.method.slm")],
                [2, WtfGlobal.getLocaleText('acc.asset.depreciation.method.dd')],
                [3, WtfGlobal.getLocaleText('acc.asset.depreciation.method.wdv')],
                [4, WtfGlobal.getLocaleText('acc.asset.depreciation.method.none')]
            ]
        }); 
    },
    createFields: function() {

         this.machineName=new Wtf.form.ExtendedTextField({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.machine.machinename.toolTip") +"'>"+ WtfGlobal.getLocaleText("acc.machine.machinename")+"*" +"</span>",
            name: 'machinename',
            id:'machinename'+this.id,
            allowBlank:false,
            anchor:'75%',
            maskRe: Wtf.productNameCommaMaskRe,
            invalidText : 'This field should not be blank or should not contain %, ", \\ characters.',
            maxLength:150
        });

        this.machineID = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machine.machineid"),
            name: 'machineid',
            id: "machineid" + this.id,
            anchor: '75%',
            maxLength: 50,
            scope: this,
            allowBlank: false
        });
        this.machineSerialNo = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machine.serialNumber"),
            name: 'machineserialno',
            id: "machineserialno" + this.id,
            anchor: '75%',
            maxLength: 50,
            scope: this,
            allowBlank: false
        });


        this.sequenceFormatCombobox = new Wtf.form.ComboBox({
            triggerAction: 'all',
            mode: 'local',
            fieldLabel: WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
            valueField: 'id',
            displayField: 'value',
            store: this.sequenceFormatStore,
            disabled: (this.isEdit ? true : false),
            anchor: '75%',
            typeAhead: true,
            forceSelection: true,
            name: 'sequenceformat',
            hiddenName: 'sequenceformat',
            listeners: {
                'select': {
                    fn: this.getNextSequenceNumber,
                    scope: this
                }
            }

        });

        this.process = new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.machine.process"),
            forceSelection: true
        }, {
            name: 'process',
            hiddenName: 'process',
            emptyText: WtfGlobal.getLocaleText("acc.machine.selectProcess"),
            store: this.processStore,
            valueField: 'id',
            mode: 'remote',
            displayField: 'name',
            triggerAction: 'all',
            anchor: '75%',
            allowBlank: false,
            addCreateOpt:false,
            addNewFn:this.addProcess.createDelegate(this)
        }));
        
        this.process.on('focus', this.loadProcessStoreOnFocus, this);
        
        this.machineOperatingCapacity = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machine.operatingCapacity"),
            name: 'machineoperatingcapacity',
            maxLength: 10,
            anchor: '75%',
            scope: this,
            allowBlank: true,
            allowNegative: false,
            allowDecimals:false,
            defaultValue: 0
        });

        this.machineUsesCount = new Wtf.form.NumberField({
            allowNegative: false,
            defaultValue: 0,
            maxLength: 10,
            fieldLabel: WtfGlobal.getLocaleText("acc.machine.usageCount"),
            anchor: '75%',
            name: 'machineusescount',
            hiddenName: 'machineusescount'
        });


         this.machineVendor= new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.invoiceList.ven.tt") +"'>"+ WtfGlobal.getLocaleText("acc.machine.vendor") +"</span>",
            name:'vendorid',
            hiddenName:'vendorid',
            id:'vendorname'+this.id,
            store: Wtf.vendorAccRemoteStore, // remote store
            valueField:'accid',
            displayField:'accname',
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            extraFields:[],
            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.inv.ven") , 
            mode: 'remote', 
            typeAheadDelay:30000,
            minChars:1,
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            anchor: '75%',
            triggerAction:'all'
        });

        this.dateOfInstallation = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machine.dateofinstallation"),
            name: 'dateofinstallation',
            hiddenName: 'dateofinstallation',
            format: WtfGlobal.getOnlyDateFormat(),
            maxLength: 255,
            anchor: '75%',
            scope: this
        });
        this.dateOfInstallation.on("change",function(){
            var dateofpurchase = this.dateOfPurchase.getValue();  // getting purchase date
            var dateofinstallation = this.dateOfInstallation.getValue(); // getting installation date
            if (dateofinstallation < dateofpurchase ) { // checking if installation date is less than purchase
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mrp.machine.installation.isvalid.msg")], 2);
                this.dateOfInstallation.setValue("");  // if yes, then showing msg and assigning it to blank
            }
        },this);
        this.insuranceDueDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machine.insuranceDueDate"),
            name: 'insuranceduedate',
            hiddenName: 'insuranceduedate',
            format: WtfGlobal.getOnlyDateFormat(),
            maxLength: 255,
            anchor: '75%',
            scope: this
        });
        this.dateOfPurchase = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machine.dateofpurchase")+"*",
            name: 'dateofpurchase',
            hiddenName: 'dateofpurchase',
            format: WtfGlobal.getOnlyDateFormat(),
            maxLength: 255,
            anchor: '75%',
            value: new Date(),
            scope: this,
            allowBlank: false
        });


        this.ageOfMachine = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machine.ageofmachine"),
            name: 'ageofmachine',
            maxLength: 100,
            anchor: '75%',
            scope: this,
            allowBlank: true
        });


        this.workCenterCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.machine.workCenter"),
            name: 'workcenter',
            hiddenName: 'workcenter',
            store: this.workCenterStore,
            valueField: 'id',
            displayField: 'name',
            allowBlank: true,
            mode: 'remote',
            typeAhead: true,
            triggerAction: 'all',
            anchor: '75%',
            listWidth: 300,
            extraFields:[],
            addCreateOpt:false
        });

        this.workCenterCombo.addNewFn=this.addWorkCentre.createDelegate(this);
        
        this.purchaseAccount = new Wtf.form.ExtFnComboBox({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.product.purchaseAcc.tt") + "'>" + WtfGlobal.getLocaleText("acc.product.purchaseAcc") + "</span>", //WtfGlobal.getLocaleText("acc.product.purchaseAcc"),//'Purchase Account*',
            store: this.purchaseAccStore,
            anchor: '75%',
            name: 'purchaseaccountid',
            id: 'purchaseaccountid' + this.id,
            hiddenName: 'purchaseaccountid',
            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
            extraComparisionField: 'acccode', // type ahead search on acccode as well.
            typeAheadDelay: 30000,
            typeAhead: true,
            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 350 : 240,
            valueField: 'accid',
            displayField: 'accname',
            forceSelection: true,
            allowBlank: false,
            mode: 'remote'
        });

        this.activeMachineAssigned = new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.machine.activemachineid")+"*",
            hideLabel:!this.isSubstituteMachine,
            forceSelection: true
        }, {
            name: 'activemachineid',
            hiddenName: 'activemachineid',
            emptyText: WtfGlobal.getLocaleText("acc.machine.selectActiveMachine"),
            store: this.activeMachineStore,
            valueField: 'id',
            mode: 'remote',
            displayField: 'machinename',
            anchor: '75%',
            allowBlank: !this.isSubstituteMachine,
            hidden: !this.isSubstituteMachine,
            disabled: !this.isSubstituteMachine
        }));
        
        
        this.isAssetMachine= new Wtf.form.Checkbox({
            name:'isassetmachine',
            id:'isassetmachine'+this.id,
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.machine.fieldLabel.isAssetMachine")+"'>"+ WtfGlobal.getLocaleText("acc.machine.fieldLabel.isAssetMachine")+"</span>",
            checked:(this.isEdit && this.record.data.isassetmachine != undefined)?this.record.data.isassetmachine:true,
            itemCls:"chkboxalign"
        });
        
        this.assetGroupCombo = new Wtf.form.ExtFnComboBox({
            emptyText: WtfGlobal.getLocaleText("acc.machine.field.emptytext.selectAssetGroup"),
            name: 'productname',
            store: this.assetGroupStore,
            typeAhead: true,
            selectOnFocus: true,
            valueField: 'productid',
            displayField: 'productname',
            extraFields: ['pid'],
            extraComparisionField: 'pid', // type ahead search on acccode as well.
            lastQuery: '',
            triggerAction: 'all',
            scope: this,
            hirarchical: true,
            mode: 'remote',
            fieldLabel: WtfGlobal.getLocaleText("acc.fixed.asset.group")+"*",
            forceSelection: true,
            listWidth: 300,
            width: 180,
            anchor: '75%',
            addNewFn:this.openAssetGroupWindow.createDelegate(this)
        });
  
        this.assetsComboStore.on('beforeload', function() {
            this.assetsComboStore.baseParams.assetGroupIds = this.assetGroupCombo.getValue();
            this.assetsComboStore.baseParams.isMachineMapped = true;
        }, this);
        this.assetGroupCombo.on('select',function(){
            this.Assets.enable();
            this.Assets.setValue("");
        },this);
        this.Assets = new Wtf.form.ExtFnComboBox({
            store: this.assetsComboStore,
            valueField: 'assetdetailId',
            hideLabel: false,
            displayField: 'assetId',
            emptyText: WtfGlobal.getLocaleText("acc.machine.field.emptytext.selectAsset"),
            mode: 'local',
            typeAhead: true,
            selectOnFocus: true,
            triggerAction: 'all',
            scope: this,
            mode: 'remote',
            disabled:true,
//                    multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.machine.asset")+"*",
            forceSelection: true,
            extraFields: ['assetGroup'],
            extraComparisionField: 'assetGroup', // type ahead search on acccode as well.
            listWidth: 250,
            width: 240,
            anchor: '75%',
            name: 'assetdetailId',
            hiddenName: 'assetdetailId',
            addNewFn:this.openingHandler.createDelegate(this)
        });
        
        this.leaseFieldWidth=320;
        this.startDateOfLease = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machineLease.field.startDateOfLease")+"*",
            name: 'startdateoflease',
            hiddenName: 'startdateoflease',
            format: WtfGlobal.getOnlyDateFormat(),
            maxLength: 255,
            scope: this,
            value: new Date(),
            allowBlank: true,
            width:this.leaseFieldWidth-15
        });
        
        this.endDateOfLease = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machineLease.field.endDateOfLease")+"*",
            name: 'enddateoflease',
            hiddenName: 'enddateoflease',
            format: WtfGlobal.getOnlyDateFormat(),
            maxLength: 255,
            scope: this,
            value: new Date(),
            allowBlank: true,
            width:this.leaseFieldWidth-15
        });
        
        this.depreciationMethodCombo= new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.machineLease.field.depreciationMethod")+"*",
            name: 'depreciationmethod',
            hiddenName: 'depreciationmethod',
            forceSelection:true,
            triggerAction:'all',
            allowBlank:false,
            editable:false,
            displayField:'depreciationMethodType',
            valueField:'depreciationmethodvalue',
            store:this.depreciationStore,
            disabledClass:"newtripcmbss",
            mode:'local',
            value:1,
            width:this.leaseFieldWidth-15
        });
        
         this.machinePrice = new Wtf.form.NumberField({
            fieldLabel : WtfGlobal.getLocaleText("acc.machineLease.field.machinePrice")+"*",
            name:'machineprice',
            allowBlank:false,
            allowNegative:false,
            decimalPrecision:2,
            value:0,
            width:this.leaseFieldWidth
        });
         this.depreciationRate = new Wtf.form.NumberField({
            fieldLabel : WtfGlobal.getLocaleText("acc.machineLease.field.depreciationRate")+"*",
            name:'depreciationrate',
            maxValue : 100,
            allowBlank:false,
            allowNegative:false,
            decimalPrecision:2,
            value:0,
            width:this.leaseFieldWidth
        });
         this.leaseYears = new Wtf.form.NumberField({
            fieldLabel : WtfGlobal.getLocaleText("acc.machineLease.field.leaseYears")+"*",
            name:'leaseyears',
            maxValue : 50,
            allowBlank:false,
            allowNegative:false,
            decimalPrecision:2,
            value:0,
            width:this.leaseFieldWidth
        });
        
        this.hasMachineOnLease = new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.machine.isonlease"),
            checkboxToggle: true,
            width: 480,
            hidden:true,
            checkboxName: 'hasmachineonlease',
            style: 'margin-right:30px',
            collapsible: true,
            collapsed: (this.isEdit && this.record.data.hasmachineonlease != undefined && this.record.data.hasmachineonlease == true)?false:true,
            items: [this.startDateOfLease,this.endDateOfLease,this.leaseYears,this.machinePrice,this.depreciationMethodCombo,this.depreciationRate]
        });
        
        
          this.fullMachineTime = new Wtf.form.TextField({  //Machine Time  
            fieldLabel: WtfGlobal.getLocaleText("acc.machineManRatio.machineTime"),
            name: 'fullMachineTime',
            width:200,
            maxLength: 50,
            scope: this
//            allowBlank: false
        });
        
         this.fullManTime = new Wtf.form.TextField({  //Man Time  
            fieldLabel: WtfGlobal.getLocaleText("acc.machineManRatio.manTime"),
            name: 'fullManTime',
            width:200,
            maxLength: 50,
            scope: this
//            allowBlank: false
        });
     
        this.partMachineTime = new Wtf.form.TextField({  //Machine Time  
            fieldLabel: WtfGlobal.getLocaleText("acc.machineManRatio.machineTime"),
            name: 'partMachineTime',
            width:200,
            maxLength: 50,
            scope: this
//            allowBlank: false
        });
        
         this.partManTime = new Wtf.form.TextField({  //Man Time  
            fieldLabel: WtfGlobal.getLocaleText("acc.machineManRatio.manTime"),
            name: 'partManTime',
            width:200,
            maxLength: 50,
            scope: this
//            allowBlank: false
        });
      
    this.fullTimeRatioSet = new Wtf.form.FieldSet({
        title: WtfGlobal.getLocaleText("acc.machineManRatio.fullTime"),
        width: 450,
        checkboxName: 'fullTimeRatio',
        items: [ this.fullMachineTime,this.fullManTime]
    });
        
    this.partTimeRatioSet = new Wtf.form.FieldSet({
        title: WtfGlobal.getLocaleText("acc.machineManRatio.partTime"),
        width: 450,
        checkboxName: 'partTimeRatio',
        items: [this.partMachineTime,this.partManTime]
    });
    
    /*
    Shift Timings - Capture machine's shift timings for calculation of machine cost
     */
    var reg=/^(([0-1][0-9]|[2][0-3]):([0-5][0-9]))|(([2][4]:[0][0]))|(([0-1][0-9]|[2][0-3]):([0-6][0]))$/;
    this.shiftTimingConfig = {
        fieldLabel:WtfGlobal.getLocaleText("acc.labour.shifttiming")+ ' *'+WtfGlobal.addLabelHelp("<ul style='list-style-type:disc; margin-left:10px;'><li>Timing should be in 24 Hours format i.e <b>hh:mm</b> format.</li><li> Hours value should be less than 24.</li> <li>Minutes value should be less than 60.</li></ul>"),
        name: 'shifttiming',
        hiddenName: 'shifttiming',
        regex:reg,
        id: "shifttiming" + this.id
    };
    this.shiftTiming = WtfGlobal.createTextfield(this.shiftTimingConfig, false, false, 5, this);

this.manmachineRation = new Wtf.form.FieldSet({
            border: false,
            xtype: 'fieldset',
            autoHeight: true,
            width: 480,
            disabledClass: "newtripcmbss",
            title: WtfGlobal.getLocaleText("mrp.manmachinerationfielset.title"),
            defaults: {border: false},
            items: [this.fullTimeRatioSet, this.partTimeRatioSet, this.shiftTiming]
        });
        
       
    },
    loadProcessStoreOnFocus:function(){
        this.processStore.load();
    },
    createCustomFields: function () {
        this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId: this.id + 'Northform',
            autoHeight: true,
            moduleid: Wtf.MACHINE_MANAGEMENT_MODULE_ID,
            isEdit: this.isEdit,
            record: this.record,
            isViewMode: this.readOnly,
            parentcompId: this.id
        });
    },
    setNextNumber: function(config) {
        
         if(this.sequenceFormatStore.getCount()>0){
            if(this.isEdit ){ //only edit case 
                var index=this.sequenceFormatStore.find('id',this.record.data.sequenceformatid);   
                if(index!=-1){
                    this.sequenceFormatCombobox.setValue(this.record.data.sequenceformatid); 
                    this.sequenceFormatCombobox.disable();
                    this.machineID.disable();   
                } else {
                    this.sequenceFormatCombobox.setValue("NA"); 
                    this.sequenceFormatCombobox.disable();
                    this.machineID.enable();  
                }
            }
            if(!this.isEdit ){   //|| this.copyInv|| this.GENERATE_PO||this.GENERATE_SO){// create new,copy,generate so and po case
                var count = this.sequenceFormatStore.getCount();
                for (var i = 0; i < count; i++) {
                    var seqRec = this.sequenceFormatStore.getAt(i)
                    if (seqRec.json.isdefaultformat == "Yes") {
                        this.sequenceFormatCombobox.setValue(seqRec.data.id)
                        break;
                    }
                }
                if(this.sequenceFormatCombobox.getValue()!=""){
                    this.getNextSequenceNumber(this.sequenceFormatCombobox); 
                } else {
                    this.machineID.setValue("");
                    this.machineID.disable();
                }
            }         
        }
        

    },
    getNextSequenceNumber: function(a, val) {
        if(!(a.getValue()=="NA")){
            var rec=WtfGlobal.searchRecord(this.sequenceFormatStore, a.getValue(), 'id');
            var oldflag=rec!=null?rec.get('oldflag'):true;
            Wtf.Ajax.requestEx({
                url:"ACCCompanyPref/getNextAutoNumber.do",
                params:{
                    from:Wtf.MACHINE_MANAGEMENT_MODULE_ID,
                    sequenceformat:a.getValue(),
                    oldflag:oldflag
                }
            }, this,function(resp){
                if(resp.data=="NA"){
                    this.machineID.reset();
                    this.machineID.enable();
                }else {
                    this.machineID.setValue(resp.data);
                    this.machineID.disable();
                }
            
            });
        } else {
            this.machineID.reset();
            this.machineID.enable();
        }
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
                            items: [
                                this.machineName,
                                this.sequenceFormatCombobox, this.machineID,
                                this.machineSerialNo,
                                this.activeMachineAssigned,
                                this.dateOfPurchase,
                                this.process,
                                this.machineOperatingCapacity,
                                this.machineUsesCount,
                                this.machineVendor,
                                this.dateOfInstallation,
                                this.insuranceDueDate,
                                this.ageOfMachine,
                                this.workCenterCombo
                            ]
                        }, {
                            layout: 'form',
                            columnWidth: 0.49,
                            items: [
                                this.purchaseAccount,
                                this.isAssetMachine,
                                this.assetGroupCombo,
                                this.Assets,
                                this.hasMachineOnLease,
                                this.manmachineRation
                            ]
                        }]
                }, {
                    xtype: "panel",
                    items: [this.tagsFieldset]
                }
            ]
        });

    },
    save: function() {

        var isValidNorthForm = this.machineForm.getForm().isValid();
        var isValidCustomFields = this.tagsFieldset.checkMendatoryCombo();

        if (!isValidNorthForm || !isValidCustomFields) {
            WtfGlobal.dispalyErrorMessageDetails(this.id + "requiredfieldmessagepanel", this.getInvalidFields());
            this.machineForm.doLayout();
            return;
        } else {
            Wtf.getCmp(this.id + "requiredfieldmessagepanel").hide();
        }
        if (isValidNorthForm) {
            this.saveData();
        }

    },
    genSuccessResponse: function(response, request) {

        if (response.success) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.success"),
                msg: response.msg,//WtfGlobal.getLocaleText("acc.machine.savemsg"),
                width: 450,
                scope: {
                    scopeObj: this
                },
                buttons: Wtf.MessageBox.OK,
                fn: function(btn ,text, option) {
                    this.scopeObj.refreshReportGrid();
                },
                animEl: 'mb9',
                icon: Wtf.MessageBox.INFO
            });
            if (this.saveAndCreateNewFlag) {
                this.enableSaveButton();
                this.enableComponent();
                this.resetComponents();

            } else {
                this.disableComponents();
            }
            
        }else{
            this.genFailureResponse(response);
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
        var invalidCustomFieldsArray = this.tagsFieldset.getInvalidCustomFields();// Function for getting invalid custom fields and dimensions 
        for (var i = 0; i < invalidCustomFieldsArray.length; i++) {
            invalidFields.push(invalidCustomFieldsArray[i]);
        }
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
                jsonObject.dateofinstallation=WtfGlobal.convertToGenericDate(this.dateOfInstallation.getValue());
                jsonObject.dateofpurchase=WtfGlobal.convertToGenericDate(this.dateOfPurchase.getValue());
                jsonObject.insuranceduedate=WtfGlobal.convertToGenericDate(this.insuranceDueDate.getValue());
                jsonObject.startdateoflease=WtfGlobal.convertToGenericDate(this.startDateOfLease.getValue());
                jsonObject.enddateoflease=WtfGlobal.convertToGenericDate(this.endDateOfLease.getValue());
                jsonObject.issubstitutemachine=this.isSubstituteMachine;
                jsonObject.isEdit=this.isEdit;
                if (jsonObject.hasmachineonlease == "on") {
                    jsonObject.hasmachineonlease = true;
                } else {
                    jsonObject.hasmachineonlease = false;
                }
                if (jsonObject.isassetmachine == "on") {
                    jsonObject.assetdetailId = this.Assets.getValue();
                }
                WtfGlobal.onFormSumbitGetDisableFieldValues(this.machineForm.form.items, jsonObject);
                if (this.isEdit) {
                    jsonObject.id = this.record.data.id;
                    jsonObject.issubstitutemachine=this.record.data.issubstitute;
                }
                var seqFormatRec = WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                jsonObject.seqformat_oldflag = seqFormatRec != null ? seqFormatRec.get('oldflag') : true;
                var custFieldArr = this.tagsFieldset.createFieldValuesArray();
                if (custFieldArr.length > 0)
                    jsonObject.customfield = JSON.stringify(custFieldArr);
                this.disableSaveButton();
                WtfComMsgBox(27, 4, true);
                this.ajxUrl = "ACCMachineMaster/saveMachineMaster.do";
                Wtf.Ajax.requestEx({
                    url: this.ajxUrl,
                    params: jsonObject
                }, this, this.genSuccessResponse, this.genFailureResponse);
            }
        }, this);
    },
    enableSaveButton: function() {
        if (this.saveBttn) {
            this.saveBttn.enable();
        }
        if (this.savencreateBttn) {
            this.savencreateBttn.enable();
        }
    },
    disableSaveButton: function() {
        if (this.saveBttn) {
            this.saveBttn.disable();
        }
        if (this.savencreateBttn) {
            this.savencreateBttn.disable();
        }
    },
     enableComponent: function () {
        if (this.machineForm) {
            this.machineForm.enable();
        }
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
                this.tagsFieldset.resetCustomComponents();
            } catch (e) {
            }
        }
     this.setSequenceFormatForCreateNewCase();           // when form is reset on 'save and create new' case, default sequence format will be set to combobox again.
    },
    addProcess:function(){
       addMasterItemWindow('36');
    },
    addWorkCentre:function(){
       callWorkcentreWindow(false, null, false);
    },
    setComboValues: function(data) {
     this.process.setValForRemoteStore(data.processid, data.process);
     this.workCenterCombo.setValForRemoteStore(data.workcenterid, data.workcenter);
     this.purchaseAccount.setValForRemoteStore(data.purchaseaccountid, data.purchaseaccount);
     this.assetGroupCombo.setValForRemoteStore(data.productname, data.product);
     this.Assets.setValForRemoteStore(data.assetdetailId, data.assetid);
     this.machineVendor.setValForRemoteStore(data.vendorid, data.vendorname);
     this.activeMachineAssigned.setValForRemoteStore(data.activemachineids, data.activemachinenames);
    },
    refreshReportGrid: function(){
        var comp = null;
        comp = Wtf.getCmp('machinemasterlist');
        if(comp){
        comp.fireEvent('machineupdate');
        }  
    },
    resetLeaseFieldsValues: function(){
        this.startDateOfLease.setValue(new Date());
        this.endDateOfLease.setValue(new Date());
        this.leaseYears.setValue(0);
        this.machinePrice.setValue(0);
        this.depreciationMethodCombo.setValue(1);
        this.depreciationRate.setValue(0);
       
    },
    setSequenceFormatForCreateNewCase:function(){
        var seqRec = this.sequenceFormatStore.getAt(0)
        this.sequenceFormatCombobox.setValue(seqRec.data.id);
        var count = this.sequenceFormatStore.getCount();
        for (var i = 0; i < count; i++) {
            var seqRec = this.sequenceFormatStore.getAt(i)
            if (seqRec.json.isdefaultformat == "Yes") {
                this.sequenceFormatCombobox.setValue(seqRec.data.id)
                break;
    }

        }
        this.getNextSequenceNumber(this.sequenceFormatCombobox); 
    },
    openAssetGroupWindow: function() {
        /* ERP-30503
         If Activate Assets Management is true then it allows to create new asset otherwise gives pop up alert message as Please Activate Assets Management from System Controls. 
         */
        if(Wtf.account.companyAccountPref.assetManagementFlag){
            createFixedAsset();
            Wtf.getCmp("productassetwindow").on("update", function(obj, productid) {
                this.productID = productid;
            }, this);
        }else{
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.alert"),
                msg:WtfGlobal.getLocaleText("mrp.assets.activation.msg"),
                width: 450,
                buttons: Wtf.MessageBox.OK,
                icon: Wtf.MessageBox.INFO
            });
        }
    },
    openingHandler: function() {
        var rec = Wtf.data.Record.create([]);
        var rec = new rec({});
        rec.beginEdit();
        rec.data.pid = this.assetGroupCombo.getRawValue();
        rec.data.productname = this.assetGroupCombo.getRawValue();
        rec.data.productid = this.assetGroupCombo.getValue();
        if(Wtf.account.companyAccountPref.assetManagementFlag){
            callFixedAssetOpeningWindow(rec);
        }else{
             Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.alert"),
                msg:WtfGlobal.getLocaleText("mrp.assets.activation.msg"),
                width: 450,
                buttons: Wtf.MessageBox.OK,
                icon: Wtf.MessageBox.INFO
            });
        }
    },
    hideShowAssetorLease: function() {
        if (this.isAssetMachine.getValue()) {
            WtfGlobal.showFormElement(this.assetGroupCombo);
            WtfGlobal.showFormElement(this.Assets);
            this.assetGroupCombo.allowBlank=false;
            this.Assets.allowBlank=false;
            this.Assets.disable();
            this.hasMachineOnLease.hide();
        } else {
            this.Assets.setValue("");
            this.assetGroupCombo.setValue("");
            this.assetGroupCombo.allowBlank=true;
            this.Assets.allowBlank=true;
            WtfGlobal.hideFormElement(this.assetGroupCombo);
            WtfGlobal.hideFormElement(this.Assets);
            this.hasMachineOnLease.show();
            
        }
    }
});
