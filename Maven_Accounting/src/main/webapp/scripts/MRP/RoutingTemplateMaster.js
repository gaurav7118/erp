/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

Wtf.account.RoutingTemplate = function (config) {
    this.id = config.id;
    this.projectId = config.projectId;
    this.alternateprojectId = config.alternateprojectId;
    this.moduleId = Wtf.MRP_Route_Code_ModuleID;
    Wtf.apply(this, config);
    this.createStore();
    this.createFields();
    this.createCustomFields();
    this.createNorthForm();
    this.createGrid();
    this.createButtons();
    this.addEventsForFields();
    this.selectedProduct = "";
    this.isPlanTaskisClicked = false;
    Wtf.account.RoutingTemplate.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.account.RoutingTemplate, Wtf.account.ClosablePanel, {
    bodyStyle: {background: "#DFE8F6 none repeat scroll 0 0"},
    border: false,
    autoScroll: true,
    loadData: function () {
        if (this.isEdit) {
            this.NorthForm.getForm().loadRecord(this.record);

            this.routingCode.setValue(this.record.data.routingtname ? this.record.data.routingtname : this.record.data.routingtcode);
            this.Duration.setValue(this.record.data.duration);
            this.DurationTyeCombo.setValue(this.record.data.durationtype);
            //this.productNameCombo.setValForRemoteStore(this.record.data.productid, this.record.data.product);
            this.bomCombo.setValForRemoteStore(this.record.data.bomid, this.record.data.bomname);
            this.workCenterCombo.setValForRemoteStore(this.record.data.workcenter, this.record.data.workcentername);
            this.machineCombo.setValForRemoteStore(this.record.data.machineid, this.record.data.machinename);
            this.labourCombo.setValForRemoteStore(this.record.data.labourid, this.record.data.labourname);
            /*
             * set alternate fields data in edit case
             */
            if (this.record.data.alternaterouting && this.record.data.alternaterouting != "") {
                this.alternateRouting.setValue(this.record.data.alternaterouting);
                this.enableFields();
                this.alternateRoutingCode.setValue(this.record.data.alternateroutingtname);
                this.alternateDuration.setValue(this.record.data.alternateduration);
                this.DurationTyeCombo.setValue(this.record.data.alternatedurationtype);
               // this.alternateProductNameCombo.setValForRemoteStore(this.record.data.alternateproductid, this.record.data.alternateproduct);
                this.alternateBomCombo.setValForRemoteStore(this.record.data.alternatebomid, this.record.data.alternatebomname);
                this.alternateWorkCenterCombo.setValForRemoteStore(this.record.data.alternateworkcenter, this.record.data.alternateworkcentername);
                this.alternateMachineCombo.setValForRemoteStore(this.record.data.alternatemachineid, this.record.data.alternatemachinename);
                this.alternateLabourCombo.setValForRemoteStore(this.record.data.alternatelabourid, this.record.data.alternatelabourname);
            }
        }
    },
    onRender: function (config) {
        this.createLeadPanel();
        this.routingMasterGrid.on('datachanged', this.onGridDataChanged, this);
        if (this.isEdit) {
            this.loadData();
        }
        this.on("activate", function () {
            this.doLayout();
        }, this);
        Wtf.account.RoutingMasterList.superclass.onRender.call(this, config);
    },
    createStore: function () {
        /*
         * Creating Record for Sequence Format
         */
        this.sequenceFormatStoreRec = new Wtf.data.Record.create([
            {
                name: 'id'
            },{
                name: 'value'
            },{
                name: 'oldflag'
            }
        ]);
        /*
         * Creating Store for Sequence Format
         */
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.sequenceFormatStoreRec),
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:"autoroutecode",
                isEdit: this.isEdit
            }
        });
        /*
         * Calling setNextNumber function on Sequnce Format store Load
         */
        this.sequenceFormatStore.on('load', this.setNextNumber, this);
        this.productRec = Wtf.data.Record.create([
            {name: 'productid'},
            {name: 'pid'},
            {name: 'type'},
            {name: 'productname'},
            {name: 'desc'},
            {name: 'producttype'}
        ]);
        this.productStore = new Wtf.data.Store({
            url: "ACCWorkOrder/getProductsForCombo.do",
            baseParams: {
                mode: 22,
                onlyProduct: true,
                isFixedAsset: false,
                type: Wtf.producttype.assembly,
                includeBothFixedAssetAndProductFlag: false
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.productRec)
        });


        this.DurationTyeStore = new Wtf.data.SimpleStore({
            fields: [{name: 'id'}, {name: 'name'}],
            data: [["Hours", 0], ["Days", 1]]
        });

        this.alternateDurationTyeStore = new Wtf.data.SimpleStore({
            fields: [{name: 'id'}, {name: 'name'}],
            data: [["Hours", 0], ["Days", 1]]
        });
        this.workCenterRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
            {name: 'workcenterid'}
        ]);

        this.workCenterStore = new Wtf.data.Store({
            url: "ACCWorkCentreCMN/getWorkcentresForCombo.do",
            baseParams: {
                mode: 112,
                groupid: 37
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.workCenterRec)
        });
        this.workCenterStore.on("beforeload", this.beforeLoadOFworkcenterStore, this);
        this.alternateWorkCenterStore = new Wtf.data.Store({
            url: "ACCWorkCentreCMN/getWorkcentresForCombo.do",
            baseParams: {
                mode: 112,
                groupid: 37
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.workCenterRec)
        });
        this.alternateWorkCenterStore.on("beforeload", this.beforeLoadOFAlternateworkcenterStore, this);
        this.machineMasterRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
            {name: 'machineid'}
        ]);

        this.machineStore = new Wtf.data.Store({
            url: "ACCMachineMaster/getMachinesForCombo.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.machineMasterRec)
        });
        this.machineStore.on('beforeload', this.handleBoforeLoadOfMachineStore, this);
        this.alternateMachineStore = new Wtf.data.Store({
            url: "ACCMachineMaster/getMachinesForCombo.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.machineMasterRec)
        });
        this.alternateMachineStore.on('beforeload', this.handleBoforeLoadOfAlternateMachineStore, this);
        this.labourRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
            {name: 'empcode'}
        ]);

        this.labourStore = new Wtf.data.Store({
            url: "ACCLabourCMN/getLabourForCombo.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.labourRec)
        });
        this.labourStore.on('beforeload', this.handleBoforeLoadOfLabourStore, this);
        this.alternateLabourStore = new Wtf.data.Store({
            url: "ACCLabourCMN/getLabourForCombo.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.labourRec)
        });
        this.alternateLabourStore.on('beforeload', this.handleBoforeLoadOfAlternateLabourStore, this);
        this.bomRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
            {name: 'bomcode'},
            {name: 'bomName'}
        ]);

        this.bomStore = new Wtf.data.Store({
            url: "ACCProductCMN/getBOMforCombo.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.bomRec)
        });
        this.bomStore.on('beforeload', this.handleBeforeLoadOfBOMStore, this);
        this.alternateBomStore = new Wtf.data.Store({
            url: "ACCProductCMN/getBOMforCombo.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.bomRec)
        });
        this.alternateBomStore.on('beforeload', this.handleBeforeLoadOfAlternateBOMStore, this);
    },
    handleBeforeLoadOfBOMStore: function (s, o) {
        o.params.productid = this.productNameCombo.getValue();
    },
    beforeLoadOFworkcenterStore: function (s, o) {
        o.params.productid = this.productNameCombo.getValue();
    },
    handleBoforeLoadOfMachineStore: function (s, o) {
        if (this.workCenterCombo && o.params) {
            o.params.workcenterid = this.workCenterCombo.getValue();
        }
    },
    handleBoforeLoadOfLabourStore: function (s, o) {
        if (this.workCenterCombo && o.params) {
            o.params.workcenterid = this.workCenterCombo.getValue();
        }
    },
    handleBeforeLoadOfAlternateBOMStore: function (s, o) {
        o.params.productid = this.alternateProductNameCombo.getValue();
    },
    beforeLoadOFAlternateworkcenterStore: function (s, o) {
        o.params.productid = this.alternateProductNameCombo.getValue();
    },
    handleBoforeLoadOfAlternateMachineStore: function (s, o) {
        if (!o.params){
            o.params = {};
        }
        if (this.alternateWorkCenterCombo && o.params) {
            o.params.workcenterid = this.alternateWorkCenterCombo.getValue();
        }
    },
    handleBoforeLoadOfAlternateLabourStore: function (s, o) {
        if (!o.params){
            o.params = {};
        }
        if (this.alternateWorkCenterCombo && o.params) {
            o.params.workcenterid = this.alternateWorkCenterCombo.getValue();
        }
    },
    refreshReportGrid: function () {
        var comp = null;
        comp = Wtf.getCmp('rountingmasterlist');
        if (comp) {
            comp.fireEvent('routingtemplateupdate');
        }
    },
    createLeadPanel: function () {
        this.centerPanel = new Wtf.Panel({
            region: 'center',
            border: false,
            scope: this,
            autoScroll: true,
            autoHeight: true,
            items: [this.NorthForm, this.GridPanel],
            bbar: [this.saveBttn, this.saveAndCreateNewBttn]
        });
        this.add(this.centerPanel);
    },
    setNextNumber: function (store) {
        if (this.sequenceFormatStore.getCount() > 0) {
            if (this.isEdit) {
                var index = this.sequenceFormatStore.find('id', this.record.data.seqformat);
                if (index != -1) {
                    this.sequenceFormatCombobox.setValue(this.record.data.seqformat);
                    this.sequenceFormatCombobox.disable();
                    this.routingCode.disable();
                } else {
                    this.sequenceFormatCombobox.setValue("NA");
                    this.sequenceFormatCombobox.disable();
                    this.routingCode.enable();
                }
            } else {
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
                } else{
                    this.routingCode.setValue("");
                    this.routingCode.disable();
                }
            }
        }
    },
    createFields: function () {
        /*
         * Creating Combo Field for Sequence Format
         */
        this.sequenceFormatCombobox = new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("mrp.workorder.entry.sequenceformat"),
            name: 'sequenceFormat',
            id:"sequenceFormat"+this.id,
            ctCls:"fieldmargin",
            store:this.sequenceFormatStore,
            valueField:'id',
            displayField:'value',
            hidden:false,
            width : 180,
            maxLength:50,
            scope:this
//            allowBlank:false
        });
        this.sequenceFormatStore.on('load',this.loadrecord,this);
        this.sequenceFormatCombobox.on('select', this.getNextSequenceNumber, this);
        this.sequenceFormatStore.load();
        this.routingCode = new Wtf.form.ExtendedTextField({
            fieldLabel: WtfGlobal.getLocaleText("mrp.workorder.entry.routingcode") + '*', //"Routing Code"
            name: 'routingtname',
            scope: this,
            width: 200,
            allowBlank: false,
            maxLength: 50
        });

        this.alternateRoutingCode = new Wtf.form.ExtendedTextField({
            fieldLabel: WtfGlobal.getLocaleText("mrp.workorder.entry.routingcode") + '*', //"Routing Code"
            name: 'alternateroutingtname',
            scope: this,
            width: 200,
            maxLength: 50,
            disabled: true
        });
        this.DurationTyeCombo = new Wtf.form.ComboBox({
            store: this.DurationTyeStore,
            fieldLabel: WtfGlobal.getLocaleText('acc.field.durationtypecombo.fieldlabel'), //'Duration Type
            name: 'durationtype',
            displayField: 'id',
            forceSelection: true,
            valueField: 'name',
            mode: 'local',
            width: 200,
            triggerAction: 'all',
            value: 0
        });

        this.alternateDurationTyeCombo = new Wtf.form.ComboBox({
            store: this.alternateDurationTyeStore,
            fieldLabel: WtfGlobal.getLocaleText('acc.field.durationtypecombo.fieldlabel'), //'Duration Type
            name: 'alternatedurationtype',
            displayField: 'id',
            forceSelection: true,
            valueField: 'name',
            mode: 'local',
            disabled: true,
            width: 200,
            triggerAction: 'all',
            value: 0
        });

        this.Duration = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText('acc.field.durationtype.fieldlabel'), //Duration
            name: 'duration',
            hidden: false,
            width: 200,
            maxLength: 50,
            value: 0,
            scope: this
        });
        this.alternateDuration = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText('acc.field.durationtype.fieldlabel'), //Duration
            name: 'alternateduration',
            hidden: false,
            width: 200,
            maxLength: 50,
            disabled: true,
            scope: this,
            value: 0
        });

        this.productNameCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.AssemblyProduct") + "*",
            name: 'productid',
            hiddenName: 'productid',
            store: this.productStore,
            typeAhead: true,
            isProductCombo: true,
            selectOnFocus: true,
            maxHeight: 250,
            listAlign: "bl-tl?",
            valueField: 'productid',
            displayField: 'pid',
            extraFields: ['productname', 'type'],
            listWidth: 450,
            extraComparisionField: 'pid',
            extraComparisionFieldArray: ['pid', 'productname'],
            lastQuery: '',
            width: 200,
            hirarchical: true,
            addNewFn: this.addProduct.createDelegate(this),
            forceSelection: true,
            isProductCombo:true,
                    allowBlank: false,
            mode: 'local'
        });
        this.productNameCombo.on("change", this.handleProductOnChange, this);
        this.productStore.load();
         
         this.productStore.on('load', function () {
            if (this.isEdit && this.record) {
                this.productNameCombo.setValue(this.record.data.productid);
                if (this.record.data.alternaterouting && this.record.data.alternaterouting != "") {
                    this.alternateProductNameCombo.setValForRemoteStore(this.record.data.alternateproductid, this.record.data.alternateproduct);
                }
            }
        }, this);

        this.bomCombo = new Wtf.form.ExtFnComboBox({
            multiSelect: true,
            id: "bomCombo" + this.id,
            mode: 'remote',
            triggerAction: 'all',
            name: 'bomid',
            hiddenName: 'bomid',
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.bomcode") + '*',
            typeAhead: true,
            width: 200,
            allowBlank: false,
            scope: this,
            editable: true,
            store: this.bomStore,
            valueField: 'id',
            displayField: 'bomcode',
            msgTarget: 'side',
            extraFields: ['name'],
            emptyText: "Select BOM Code",
            disabled:(!this.isEdit) ? true : false, //ERP-30557
            isBOMCombo: true            
        });

        this.bomCombo.on("change", this.ActivateDeactivatePlanTaskButtonAndPMTab, this);
        this.alternateProductNameCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.AssemblyProduct") + "*",
            name: 'alternateproductid',
            hiddenName: 'alternateproductid',
            store: this.productStore,
            typeAhead: true,
            isProductCombo: true,
            selectOnFocus: true,
            maxHeight: 250,
            listAlign: "bl-tl?",
            valueField: 'productid',
            displayField: 'pid',
            extraFields: ['pid', 'productname', 'type'],
            listWidth: 450,
            extraComparisionField: 'pid',
            extraComparisionFieldArray: ['pid', 'productname'],
            lastQuery: '',
            width: 200,
            hirarchical: true,
            disabled: true,
            readOnly: true,
            addNewFn: this.addProduct.createDelegate(this),
            forceSelection: true,
            isProductCombo:true,
                    mode: 'remote'
        });

        this.alternateBomCombo = new Wtf.form.ExtFnComboBox({
            multiSelect: true,
            id: "alternatebomCombo" + this.id,
            mode: 'remote',
            triggerAction: 'all',
            name: 'alternatebomid',
            hiddenName: 'alternatebomid',
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.bomcode") + '*',
            typeAhead: true,
            width: 200,
            scope: this,
            editable: true,
            store: this.alternateBomStore,
            valueField: 'id',
            displayField: 'name',
            msgTarget: 'side',
            disabled: true,
            extraFields: [],
            emptyText: "Select BOM Code"
        });
        this.workCenterCombo = new Wtf.form.ExtFnComboBox({
            multiSelect: true,
            id: "workcenterCombo" + this.id,
            mode: 'remote',
            triggerAction: 'all',
            name: 'workcenter',
            hiddenName: 'workcenter',
            fieldLabel: WtfGlobal.getLocaleText("acc.resourceanalysis.columns.workcenter") + '*',
            typeAhead: true,
            width: 200,
            allowBlank: false,
            scope: this,
            editable: true,
            store: this.workCenterStore,
            displayField: 'workcenterid',
            valueField: 'id',
            msgTarget: 'side',
            extraFields: ['name'],
            emptyText: WtfGlobal.getLocaleText("acc.resourceanalysis.workcenter"),
            disabled:(!this.isEdit) ? true : false, //ERP-30557
            addNewFn:this.createWC.createDelegate(this)
        });
        this.workCenterCombo.on("change", this.handleWorkCenterOnChange, this);
        this.alternateWorkCenterCombo = new Wtf.form.ExtFnComboBox({
            multiSelect: true,
            id: "alternateworkcenterCombo" + this.id,
            mode: 'remote',
            triggerAction: 'all',
            name: 'alternateworkcenter',
            hiddenName: 'alternateworkcenter',
            fieldLabel: WtfGlobal.getLocaleText("acc.resourceanalysis.columns.workcenter") + '*',
            typeAhead: true,
            width: 200,
            scope: this,
            editable: true,
            disabled:true,
            store: this.alternateWorkCenterStore,
            displayField: 'workcenterid',
            valueField: 'id',
            msgTarget: 'side',
            extraFields: ['name'],
            emptyText: WtfGlobal.getLocaleText("acc.resourceanalysis.workcenter"),
            addNewFn:this.createWC.createDelegate(this)
        });

//        this.machineFilter = ;

        var normalTemplate = true;
        this.machineCombo = new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            id: "machineCombo" + this.id
        }, {
            mode: 'remote',
            triggerAction: 'all',
            fieldLabel: WtfGlobal.getLocaleText("mrp.workorder.entry.machineid") + '*',
            typeAhead: true,
            width: 200,
            name: 'machinemapping',
            hiddenName: 'machinemapping',
            scope: this,
            allowBlank: false,
            editable: true,
            store: this.machineStore,
            displayField: 'machineid',
            extraFields: ['name'],
            valueField: 'id',
            msgTarget: 'side',
            emptyText: "Select Machine",
            addCreateOpt: false,
            disabled:(!this.isEdit) ? true : false, //ERP-30557
            addNewFn:this.createMC.createDelegate(this)
        }));
        this.machineCombo.on('blur', this.onResourceSelect.createDelegate(this, [false, normalTemplate]));
        this.machineCombo.on('focus', this.loadMachineStoreOnFocus, this);
        this.machineCombo.on('change', this.handleMachineComboOnChange, this);   //ERP-30557

        this.alternateMachineFilter = {
            mode: 'remote',
            triggerAction: 'all',
            fieldLabel: WtfGlobal.getLocaleText("mrp.workorder.entry.machineid") + '*',
            typeAhead: true,
            width: 200,
            name: 'alternatemachinemapping',
            hiddenName: 'alternatemachinemapping',
            scope: this,
            editable: true,
            store: this.alternateMachineStore,
            displayField: 'machineid',
            extraFields: ['name'],
            valueField: 'id',
            disabled: true,
            msgTarget: 'side',
            emptyText: "Select Machine",
            addCreateOpt: false,
            addNewFn:this.createMC.createDelegate(this)
        };

        this.alternateMachineCombo = new Wtf.common.Select(Wtf.applyIf({
            multiSelect: true,
            id: "alternatemachineCombo" + this.id
        }, this.alternateMachineFilter));
        this.alternateMachineCombo.on('blur', this.onResourceSelect.createDelegate(this, [false, false]));
         this.alternateMachineCombo.on('focus', this.loadAlternateMachineStoreOnFocus, this);
        this.labourFilter = {
            mode: 'remote',
            triggerAction: 'all',
            name: 'labourmapping',
            hiddenName: 'labourmapping',
            scope: this,
           fieldLabel: WtfGlobal.getLocaleText("mrp.workorder.entry.labourid") + '*',
            typeAhead: true,
            width: 200,
            allowBlank: false,
            editable: true,
            store: this.labourStore,
            valueField: 'id',
            displayField: 'empcode',
            extraFields: ['name'],
            msgTarget: 'side',
            emptyText: "Select Labour",
            addCreateOpt: false,
            addNewFn:this.createLB.createDelegate(this)
        };

        this.labourCombo = new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            disabled:(!this.isEdit) ? true : false,     //ERP-30557
            id: "labourCombo" + this.id
        }, this.labourFilter));
        this.labourCombo.on('blur', this.onResourceSelect.createDelegate(this, [true, normalTemplate]));
          this.labourCombo.on('focus', this.loadLabourStoreOnFocus, this);
        this.labourCombo.on('change', this.handleLabourComboOnChange, this);    //ERP-30557
        
        this.alternateLabourFilter = {
            mode: 'remote',
            triggerAction: 'all',
            name: 'alternatelabourmapping',
            hiddenName: 'alternatelabourmapping',
            scope: this,
            fieldLabel: WtfGlobal.getLocaleText("mrp.workorder.entry.labourid") + '*',
            typeAhead: true,
            width: 200,
            editable: true,
            store: this.alternateLabourStore,
            valueField: 'id',
             displayField: 'empcode',
            extraFields: ['name'],
            disabled: true,
            msgTarget: 'side',
            emptyText: "Select Labour",
             addCreateOpt: false,
            addNewFn:this.createLB.createDelegate(this)
        };

        this.alternateLabourCombo = new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            id: "alternatelabourCombo" + this.id
        }, this.alternateLabourFilter));
        this.alternateLabourCombo.on('blur', this.onResourceSelect.createDelegate(this, [true, false]));
         this.labourCombo.on('focus', this.loadLabourStoreOnFocus, this);
        this.alternateRouting = new Wtf.form.Checkbox({
            name: 'alternateRouting',
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.mrp.field.alternaterouting") + "'>" + WtfGlobal.getLocaleText("acc.mrp.field.alternaterouting") + "</span>",
            id: 'alternateRouting' + this.id,
            checked: false,
            scope: this,
            cls: 'custcheckbox',
            width: 10
        });






        this.planTask = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText('acc.workorder.fieldname.button.plantask.title'),
            tooltip: WtfGlobal.getLocaleText('acc.workorder.fieldname.button.plantask.ttip'),
            scope: this,
//            disabled:this.isEdit,
            cls: "buttonmargin",
            disabled:!this.isEdit ? true : false,   //ERP-30557
            handler: this.PlanTasks.createDelegate(this)
        });



    },
    /*
     * Author: Sayed Kausar Ali
     * Purpose: Fetching Next sequnce Number for selected Sequence Format.
     */
    getNextSequenceNumber: function (combo) {
        /*
         * If Sequence Format is NA then enabling the routingCode Field.
         */
        if (combo.getValue() == "NA") {
            this.routingCode.reset();
            this.routingCode.enable();
        } else { // Else fetching Next Auto number from Backend
            Wtf.Ajax.requestEx({
                url: "ACCCompanyPref/getNextAutoNumber.do",
                params: {
                    from: Wtf.MRP_Route_Code_ModuleID,
                    sequenceformat: combo.getValue()
                }
            }, this, function (resp) {
                if (resp.data == "NA") {
                    this.routingCode.reset();
                    this.routingCode.enable();
                } else {
                    this.routingCode.setValue(resp.data);
                    this.routingCode.disable();
                }
            });
        }
    },
    createLB : function(){
        callLabourInformation(false,"");
    },
    loadLabourStoreOnFocus : function(){
        this.labourStore.load();
    },
    handleMachineComboOnChange : function(combo, newvalue, oldvalue){   //ERP-30557
        this.labourCombo.enable();
    },
    handleLabourComboOnChange : function(combo, newvalue, oldvalue){
        if(newvalue!=undefined && newvalue!=null && newvalue!=""){
            this.planTask.enable(); //ERP-30557 : Enable Plan Task button after Labour change fill-up
        }
    },
    createWC: function () {
        callWorkcentreWindow(false, "", "", "");
    },
    createMC : function(){
        var obj={};
       callMRPMachineMaster(obj);  
    },
    loadMachineStoreOnFocus:function(){
        this.machineStore.on('beforeload', function (s, o) {
            if (!o.params){
                o.params = {};
            }
            if (this.workCenterCombo && o.params) {
                o.params.workcenterid = this.workCenterCombo.getValue();
            }
        }, this);
        this.machineStore.load();
    },
    loadAlternateMachineStoreOnFocus:function(){
        this.alternateMachineStore.on('beforeload', function (s, o) {
            if (!o.params){
                o.params = {};
            }
            if (this.alternateWorkCenterCombo && o.params) {
                o.params.workcenterid = this.alternateWorkCenterCombo.getValue();
            }
        }, this);
        this.alternateMachineStore.load();
    },
    createCustomFields: function () {
        this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId: "northForm" + this.id,
            autoHeight: true,
            moduleid: this.moduleId,
            isEdit: this.isEdit,
            record: this.record,
            isViewMode: this.readOnly,
            parentcompId: this.id
        });
    },
    createNorthForm: function () {
        this.NorthForm = new Wtf.form.FormPanel({
            region: 'north',
            autoHeight: true,
            border: false,
//            autoScroll:true,
            split: true,
            layout: "form",
            baseCls: 'northFormFormat',
            disabledClass: "newtripcmbss",
            cls: "visibleDisabled",
            id: "northForm" + this.id,
            items: [{
                    xtype: 'panel',
                    id: this.id + 'requiredfieldmessagepanel',
                    hidden: true,
                    border: false,
                    cls: 'invalidfieldinfomessage'
                }, {
                    layout: 'column',
                    defaults: {
                        border: false
                    },
                    border: false,
                    labelWidth: 160,
                    items: [{
                            layout: 'form',
                            columnWidth: 0.5,
                            border: false,
                            items: [this.sequenceFormatCombobox,this.routingCode, this.productNameCombo, this.bomCombo, this.workCenterCombo, this.machineCombo, this.labourCombo, this.DurationTyeCombo, this.Duration, this.planTask]
                        },
                        {
                            layout: 'form',
                            border: false,
                            columnWidth: 0.5,
                            items: [this.alternateRouting, this.alternateRoutingCode, this.alternateProductNameCombo, this.alternateBomCombo, this.alternateWorkCenterCombo, this.alternateMachineCombo, this.alternateLabourCombo, this.alternateDurationTyeCombo, this.alternateDuration]
                        }]
                }, this.tagsFieldset]
        });
    },
    PlanTasks: function () {
        var isValidForm = this.NorthForm.getForm().isValid();
        if (isValidForm) {
            this.onPlanTasksButtonClickSyncDataToPM();
        }
    },
    onPlanTasksButtonClickSyncDataToPM: function () {
        var machineids = this.machineCombo.getValue();
        var labourids = this.labourCombo.getValue();
        var productid = this.productNameCombo.getValue();
        var bomid = this.bomCombo.getValue();

        Wtf.Ajax.requestEx({
            url: "ACCRoutingManagement/syncDataToPM.do",
            params: {
                projectId: this.projectId,
                labourids: labourids,
                machineids: machineids,
                bomid: bomid,
                productid: productid,
                isForCompAvailablity: true//this flag is used to get subproducts bom structure
            }
        }, this, this.genSuccessResponseplantask, this.genFailureResponseplantask);

        /*
         * If there is alternate routing present ,sync resource of alternate routing as well into PM.
         */
        if (this.alternateprojectId != undefined || this.alternateprojectId != '') {
            var machineids = this.alternateMachineCombo.getValue();
            var labourids = this.alternateLabourCombo.getValue();
            var productid = this.alternateProductNameCombo.getValue();
            var bomid = this.alternateBomCombo.getValue();

            Wtf.Ajax.requestEx({
                url: "ACCRoutingManagement/syncDataToPM.do",
                params: {
                    projectId: this.alternateprojectId,
                    labourids: labourids,
                    machineids: machineids,
                    bomid: bomid,
                    productid: productid,
                    isForCompAvailablity: true//this flag is used to get subproducts bom structure at java side
                }
            }, this, this.genSuccessResponseplantaskForalternateROuting, this.genFailureResponseplantask);
        }
    },
    genSuccessResponseplantaskForalternateROuting: function (response) {
        if (response.success) {
            /*
             * Plan task button click is already handled  in below function(genSuccessResponseplantask)  i just have to refresh alternate routing grid of project plan to make resource visible.
             */
            document.getElementById("altwoprojectplan-" + this.projectId).src = Wtf.pmURL + "editableprojview.jsp?id=" + this.alternateprojectId;
        }
    },
    genSuccessResponseplantask: function (response) {
        if (response.success) {
            this.isPlanTaskisClicked = true;
            this.planTask.disable();
            this.GridPanel.enable();
//            this.planTask.disable();
            this.taskPlanned = true;
            document.getElementById("woprojectplan-" + this.projectId).src = Wtf.pmURL + "editableprojview.jsp?id=" + this.projectId;
        }
    },
    genFailureResponseplantask: function (response) {
        var msg = WtfGlobal.getLocaleText("acc.routing.failure");  //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    handleProductOnChange: function (combo, newvalue, oldvalue) {   //ERP-30557
        this.bomCombo.enable();
        this.bomCombo.clearValue();
        
        this.workCenterCombo.enable();
        this.workCenterCombo.clearValue();
        
        this.machineCombo.disable();        
        this.machineCombo.clearValue();
        
        this.labourCombo.disable();
        this.labourCombo.clearValue();        
        this.planTask.disable();
        
        if (this.alternateRouting.getValue()) {
            this.alternateProductNameCombo.setValue(newvalue);
            this.alternateProductNameCombo.disable();
        }
        this.ActivateDeactivatePlanTaskButtonAndPMTab();
    },
    handleWorkCenterOnChange: function (combo, newval, oldval) {    //ERP-30557
        this.machineCombo.enable();            
//        this.labourCombo.enable();
        this.machineCombo.clearValue();
        this.labourCombo.disable();
        this.labourCombo.clearValue();        
        this.planTask.disable();
        this.ActivateDeactivatePlanTaskButtonAndPMTab();

    },
    handleAlternateProductOnChange: function (combo, newvalue, oldvalue) {
        this.alternateWorkCenterCombo.enable();
        this.alternateBomCombo.enable();        
        this.alternateWorkCenterCombo.clearValue();
        this.alternateBomCombo.clearValue();        
        this.alternateMachineCombo.clearValue();
        this.alternateLabourCombo.clearValue();
    },
    ActivateDeactivatePlanTaskButtonAndPMTab: function () {
        if (this.isPlanTaskisClicked || this.isEdit) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("mrp.alertmessage.fulshalltask")], 2);
        }

//        this.planTask.enable();   //ERP-30557
        this.isPlanTaskisClicked = false;
        this.GridPanel.disable();
    },
    handleAlternateWorkCenterOnChange: function (combo, newval, oldval) {
        this.alternateMachineCombo.enable();
        this.alternateLabourCombo.enable();
        this.alternateMachineCombo.clearValue();
        this.alternateLabourCombo.clearValue();
    },
    getInvalidFields: function () {
        var invalidFields = []
        this.NorthForm.getForm().items.filterBy(function (field) {
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
    createGrid: function () {

//            var projectId = "af503d3a-af71-4913-a6df-b418c2aff111";
        this.projectPanel = Wtf.getCmp("woprojectplan-" + this.projectId);
        if (!this.projectPanel) {
            this.projectPanel = new Wtf.Panel({
                autoEl: {
                    tag: "iframe",
                    height: "100%",
                    closable:false,
                    id: "woprojectplan-" + this.projectId,
                    src: Wtf.pmURL + "editableprojview.jsp?id=" + this.projectId
                },
                title: "Route Code",
                closable: false,
                layout: 'fit'
            });

        }

//            var projectId = "af503d3a-af71-4913-a6df-b418c2aff111";
        this.altprojectPanel = Wtf.getCmp("altwoprojectplan-" + this.projectId);
        if (!this.altprojectPanel) {
            this.altprojectPanel = new Wtf.Panel({
                autoEl: {
                    tag: "iframe",
                    height: "100%",
                    closable:false,
                    id: "altwoprojectplan-" + this.projectId,
                    src: Wtf.pmURL + "editableprojview.jsp?id=" + this.alternateprojectId
                },
                title: "Alternate Route Code",
                closable: false,
                layout: 'fit'
            });

        }
        this.altprojectPanel.disable();
//        }

        this.routingMasterGrid = new Wtf.account.RoutingTemplateMasterGrid({
            region: 'center',
            height: 400,
            border: false,
            scope: this,
//            viewConfig: {forceFit: true},
            title: WtfGlobal.getLocaleText("acc.mrp.field.routecode"),
            editTransaction: this.isEdit,
            id: this.id + "routingtemplategrid",
            closable: false,
            forceFit: true,
            loadMask: true
        });
        this.alternateRoutingMasterGrid = new Wtf.account.RoutingTemplateMasterGrid({
            region: 'center',
            height: 400,
            border: false,
            scope: this,
            title: WtfGlobal.getLocaleText("acc.mrp.field.alternateroutingcode"),
            editTransaction: this.isEdit,
            id: this.id + "alternateroutingtemplategrid",
            closable: false,
            forceFit: true,
            loadMask: true
        });

        this.GridPanel = new Wtf.TabPanel({
            id: this.id + 'routingmasterPanel',
            iconCls: 'accountingbase coa',
            border: false,
            style: 'padding:10px;',
            scope: this,
            activeTab: 0,
            disabled: this.isEdit?false:true,
            height: 550,
            items: [this.projectPanel, this.altprojectPanel]
        });
    },
    createButtons: function () {
        this.saveBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
            tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
            id: "save" + this.id,
            scope: this,
            iconCls: 'pwnd save',
            handler: function () {
                this.saveAndCreateNewFlag = false;
                this.save();
            }
        });

        this.saveAndCreateNewBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNew"),
            tooltip: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNewToolTip"),
            id: "savencreate" + this.id,
            scope: this,
            handler: function () {
                this.saveAndCreateNewFlag = true;                    // This flag is used to differentiate between Save button and Save and Create New button
                this.save();
            },
            hidden:true,
            iconCls: 'pwnd save'
        });
    },
    addEventsForFields: function () {
//      
        this.bomStore.on('beforeload', function (s, o) {
            if (!o.params)
                o.params = {};
            var currentBaseParams = this.bomStore.baseParams;
//            currentBaseParams.productid =this.productcombo.getValue();
            this.bomStore.baseParams = currentBaseParams;
        }, this);
        this.alternateRouting.on("check", this.onAlternateRoutingChange, this);
         this.alternateRouting.on("change", this.handleOnChangeOfAlternateRoutingCheckBOx, this);
        this.GridPanel.on("beforetabchange", this.checkIfAlternateRoutingIsEnable, this);
    },
    handleOnChangeOfAlternateRoutingCheckBOx: function (checkbox,neVal,oldVal) {
        if (this.isEdit) {
            var alternateRouting = this.record.data.alternaterouting;
            if (alternateRouting) {
                this.alternateRouting.setValue(oldVal);
                return false;
            }
        }
    },
    onAlternateRoutingChange: function (obj, newval) {
        if (!this.isEdit) {
            /*
             * Control comes only  here in add routing template case
             */
            if (newval === true) {
                this.createAlternateProject();
                this.resetFields();
                this.enableFields();
            } else {
                deleteDirtyProject(this.alternateprojectId);
                this.resetFields();
                this.disableFields();
            }
        }
        
        if (this.isEdit && newval) {
            /*
             * Control comes here only in edit case of routing template and alternate routing was not created while saving main routing template previously .
             * The aim of this if block is to allow user to create alternate routing in edit case of main routing template.
             */
            var alternateIdpresent = this.record.data.alternateid;
            if (alternateIdpresent === undefined || alternateIdpresent === '' ) {
                if (newval === true) {
                    this.createAlternateProject();
                    this.resetFields();
                    this.enableFields();
                } else {
            deleteDirtyProject(this.alternateprojectId);
            this.resetFields();
            this.disableFields();
        }
            }
        
        }
        
        if (newval) {
            this.alternateProductNameCombo.setValue(this.productNameCombo.getValue());
            this.alternateProductNameCombo.disable();
        }

    },
    createAlternateProject: function () {
        Wtf.Ajax.requestEx({
            url: "ACCRoutingManagement/createProject.do",
            params: {
                isMasterProject: true,
                isNewProject: !this.isEdit,  
                projectId:this.alternateprojectId	
            }
        }, this,
                function (responseObj) {
                    if (responseObj.success == true && responseObj.projectId != undefined && responseObj.projectId != "") { //&& responseObj.isNewProject==true
                        this.alternateprojectId = responseObj.projectId
                        document.getElementById("altwoprojectplan-" + this.projectId).src = Wtf.pmURL + "editableprojview.jsp?id=" + this.alternateprojectId;
                    } else {
                        var msg = WtfGlobal.getLocaleText("acc.mrppm.failure");
                        if (responseObj) {
                            if (responseObj.msg != "") {
                                msg = responseObj.msg;
                            }
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                        }
                    }
                }, function (responseObj) {
            var msg = WtfGlobal.getLocaleText("acc.mrppm.failure");
            if (responseObj) {
                if (responseObj.msg != "") {
                    msg = responseObj.msg;
                }
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
            }
        }
        );
    },
    resetFields: function () {
        this.alternateProductNameCombo.reset();
        this.alternateBomCombo.reset();
        this.alternateWorkCenterCombo.reset();
//        this.alternateMachineCombo.reset();
//        this.alternateLabourCombo.reset();
        this.alternateMachineCombo.clearValue();
        this.alternateLabourCombo.clearValue();
        this.alternateDurationTyeCombo.reset();
        this.alternateDuration.reset();
        this.alternateRoutingCode.reset();
    },
    enableFields: function () {
        this.altprojectPanel.enable();
        this.GridPanel.activate(1);
        this.alternateProductNameCombo.enable();
        this.alternateBomCombo.enable();
        this.alternateWorkCenterCombo.enable();
        this.alternateMachineCombo.enable();
        this.alternateLabourCombo.enable();
        this.alternateDuration.enable();
        this.alternateDurationTyeCombo.enable();
        this.alternateRoutingCode.enable();
        this.alternateProductNameCombo.allowBlank = false;
        this.alternateBomCombo.allowBlank = false;
        this.alternateWorkCenterCombo.allowBlank = false;
        this.alternateMachineCombo.allowBlank = false;
        this.alternateLabourCombo.allowBlank = false;
        this.alternateRoutingCode.allowBlank = false;
    },
    disableFields: function () {
        this.GridPanel.activate(0);
        this.alternateProductNameCombo.disable();
        this.alternateBomCombo.disable();
        this.alternateWorkCenterCombo.disable();
        this.alternateMachineCombo.disable();
        this.alternateLabourCombo.disable();
        this.alternateDuration.disable();
        this.alternateDurationTyeCombo.disable();
        this.alternateRoutingCode.disable();
        this.alternateProductNameCombo.allowBlank = true;
        this.alternateWorkCenterCombo.allowBlank = true;
        this.alternateMachineCombo.allowBlank = true;
        this.alternateLabourCombo.allowBlank = true;
        this.alternateRoutingCode.allowBlank = true;
        if (this.productcombo != undefined && this.productcombo.getValue() != "") {
            this.alternateRoutingMasterGrid.getStore().removeAll();
            if (this.GridPanel) {
                this.GridPanel.setActiveTab(0);
            }
        }
    },
    checkIfAlternateRoutingIsEnable: function (obj, newTab, currentTab) {
        if (newTab != undefined && newTab.id == (this.id + "alternateroutingtemplategrid")) {
            if (!this.alternateRouting.getValue()) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Alternate routing is not enabled"], 2);
                return false;
            }
        }
    },
    save: function () {
        var isValidForm = this.NorthForm.getForm().isValid();
        var isValidCustomFields = this.tagsFieldset.checkMendatoryCombo();

        if (!this.isPlanTaskisClicked) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mrp.routingtemplateplantask.validation.err.msg")], 2);
            return;
        }

        if (this.routingCode.getValue() === this.alternateRoutingCode.getValue()) {
            /*
             * Routing code and alternate routing code should not be same. Please use diffrent names. 
             */
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mrp.routingtemplatecode.validation.err.msg")], 2);
            return;
        }
        
        if (!isValidForm || !isValidCustomFields) {
            WtfGlobal.dispalyErrorMessageDetails(this.id + 'requiredfieldmessagepanel', this.getInvalidFields());
            this.NorthForm.doLayout();
            this.NorthForm.getForm().markInvalid();
            WtfComMsgBox(2, 2);
            return;
        } else {
            Wtf.getCmp(this.id + 'requiredfieldmessagepanel').hide();
        }
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"), WtfGlobal.getLocaleText("acc.je.msg1"), function (btn) {
            if (btn != "yes") {
                this.isWarnConfirm = false;
                return;
            }
            WtfComMsgBox(27, 4, true);
            var rec = this.NorthForm.getForm().getValues();
            //ERP-30557
            rec.bomid = this.bomCombo.getValue();
            rec.workcenter = this.workCenterCombo.getValue();
            rec.machinemapping = this.machineCombo.getValue();
            rec.labourmapping = this.labourCombo.getValue();
            
            this.addAlternateParam(rec);
            if (this.isEdit) {
                rec.id = this.record.data.id
                rec.alternateid = this.record.data.alternateid;
            }
            rec.isEdit = this.isEdit;
            rec.projectId = this.projectId;
            rec.alternateprojectId = this.alternateprojectId;
            rec.routingtname = this.routingCode.getValue();
            rec.seqformat = this.sequenceFormatCombobox.getValue();

            var custFieldArr = this.tagsFieldset.createFieldValuesArray();
            if (custFieldArr.length > 0)
                rec.customfield = JSON.stringify(custFieldArr);
            var url = "";
            url = "ACCRoutingManagement/saveRoutingTemplate.do";
            Wtf.Ajax.requestEx({
                url: url,
                params: rec
            }, this, this.genSuccessResponse, this.genFailureResponse);
        }, this);
    },
    addAlternateParam: function (rec) {
        rec.alternateproduct = this.alternateProductNameCombo.getValue();
        rec.alternaterouting = this.alternateRouting.getValue();
        rec.alternatebomid = this.alternateBomCombo.getValue();
        rec.alternateworkcenter = this.alternateWorkCenterCombo.getValue();
        rec.alternatemachinemapping = this.alternateMachineCombo.getValue();
        rec.alternatelabourmapping = this.alternateLabourCombo.getValue();
        rec.alternatedurationtype = this.alternateDurationTyeCombo.getRawValue();
        rec.alternateduration = this.alternateDuration.getValue();
        rec.alternateroutingtname = this.alternateRoutingCode.getValue();
    },
    genSuccessResponse: function (response, request) {
        if (response.success) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.success"),
                msg: response.msg,
                width: 450,
                scope: {
                    scopeObj: this
                },
                buttons: Wtf.MessageBox.OK,
                animEl: 'mb9',
                fn: function (btn, text, option) {
                    this.scopeObj.refreshReportGrid();
                },
                icon: Wtf.MessageBox.INFO
            });
            if (this.saveAndCreateNewFlag) {
                this.resetComponents();
            } else {
                this.disableComponent();
            }
        } else {
            this.showFailureMsg(response);
        }
    },
    enableComponent: function () {
        if (this.NorthForm) {
            this.NorthForm.enable();
        }
    },
    resetComponents: function () {
        if (this.NorthForm) {
            this.NorthForm.getForm().reset();
        }
        if (this.tagsFieldset) {
            this.tagsFieldset.resetCustomComponents();
        }
    },
    disableComponent: function () {
        if (this.saveBttn) {
            this.saveBttn.disable();
        }
        if (this.saveAndCreateNewBttn) {
            this.saveAndCreateNewBttn.disable();
        }
        if (this.NorthForm) {
            this.NorthForm.disable();
        }
        if (this.GridPanel) {
            this.GridPanel.disable();
        }

    },
    genFailureResponse: function (response) {
        this.showFailureMsg(response);
    },
    showFailureMsg: function (response) {
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    loadRecord: function (rec) {
        if (this.record != undefined) {
            this.setComboValues();
            this.NorthForm.form.loadRecord(rec);
        }
    },
    setComboValues: function () {

    },
    onResourceSelect: function (isLabour, normalTemplate) {
        var projectid = this.projectId;
        if (this.isPlanTaskisClicked) {
            if (normalTemplate) {
                if (isLabour) {
                    this.resourceId = this.labourCombo.getValue();
                } else {
                    this.resourceId = this.machineCombo.getValue();
                }
            } else {
                projectid = this.alternateprojectId;
                if (isLabour) {
                    this.resourceId = this.alternateLabourCombo.getValue();
                } else {
                    this.resourceId = this.alternateMachineCombo.getValue();
                }
            }
            Wtf.Ajax.requestEx({
                url: "ACCRoutingManagement/syncResourceToPM.do",
                params: {
                    resourceId: this.resourceId,
                    projectId: projectid,
                    isLabour: isLabour
                }
            }, this, this.genSuccessResponseForSyncToPM, this.genFailureResponseForSyncToPM);
        }
    },
    addProduct: function () {
        callProductDetails();
    },
    genSuccessResponseForSyncToPM: function (response) {
        if (response.success) {
            document.getElementById("woprojectplan-" + this.projectId).src = Wtf.pmURL + "editableprojview.jsp?id=" + this.projectId;
        }
    },
    genFailureResponseForSyncToPM: function (response) {
    }

});


