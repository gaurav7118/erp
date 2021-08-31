/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

Wtf.WorkcentreCreationTabWindow = function(config) {
  
    this.isEdit = config.isEdit;
    this.isClone = config.isClone;
    this.record = config.record;
    this.createStore();
    this.createFields();
    this.createCustomFields();
    this.createForm();
    this.createButton();

    Wtf.WorkcentreCreationTabWindow.superclass.constructor.call(this, config);

}
Wtf.extend(Wtf.WorkcentreCreationTabWindow, Wtf.Panel, {
    onRender: function(config) {
        var image = "../../images/accounting_image/" + this.businessPerson + ".gif";
        this.createPanel();
        this.add(this.centerPanel);
        
        this.workCenterStore.load();
        this.workCenterTypeStore.load();
        this.workCenterCapacityStore.load();
        this.workCenterManagerStore.load();
        this.workTypeStore.load();
        this.labourStore.load();
        this.machineStore.load();
        this.costCenterStore.load();
        this.productStore.load();
        this.sequenceFormatStore.load();
        Wtf.inventoryLocation.load();
        Wtf.inventoryStore.load();
        
        Wtf.WorkcentreCreationTabWindow.superclass.onRender.call(this, config);

    },
  
    createStore: function() {
        
        this.costCenterRec = new Wtf.data.Record.create([
        {
            name:"id"
        },
        {
            name:"name"
        },
        {
            name:"ccid"
        },
        {
            name:"description"
        }
        ]);
        this.costCenterReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.costCenterRec);

        this.costCenterStore = new Wtf.data.Store({
            url:  'CostCenter/getCostCenter.do',
            reader:this.costCenterReader
        });
        this.materialRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
            {name: 'bomcode'},
            {name: 'pid'},
            {name: 'productname'},
            {name: 'isdefaultbom'}
        ]);
        this.materialStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            }, this.materialRec),
            url: "ACCProductCMN/getBOMforCombo.do",
            baseParams: {
                productid: ""
            }
        });
        this.materialStore.on("load",function() {
            if (this.isEdit) {
                this.mappedMaterialID.setValue(this.record.data.materialid);
            } else {    //ERP-31663 : This 'Else' block has written to Auto Populate BOM code for selected Product.
                this.materialStore.each(function (rec) {
                    if (rec.data.isdefaultbom) {
                        var bomid = this.mappedMaterialID.getValue();
                        if(bomid!=null && bomid!=undefined && bomid!=""){
                            bomid = bomid +","+rec.data.id;
                        } else {
                            bomid = rec.data.id;
                        }
                        this.mappedMaterialID.setValue(bomid);
                    }
                }, this);
            }
        },this);
        this.materialStore.on("beforeload",function(s,o) {
            this.materialStore.baseParams.productid = this.mappedProductID.getValue();
            this.materialStore.params = {
                productid: this.mappedProductID.getValue()
            };
        },this);
        this.machineRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
            {name: 'machineid'}
        ]);
        this.machineStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            }, this.machineRec),
            url: "ACCMachineMaster/getMachinesForCombo.do",
            baseParams: {
            }
        });
        this.machineStore.on("load",function() {
            if (this.isEdit) {
                this.mappedMachine.setValue(this.record.data.machineid);
            }
        },this); 
        this.labourRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
            {name: 'empcode'}
            
        ]);
        this.labourStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            }, this.labourRec),
            url: "ACCLabourCMN/getLabourForCombo.do",
            baseParams: {
            }
        });
        this.labourStore.on("load",function() {
            if (this.isEdit) {
                this.mappedLabour.setValue(this.record.data.labourid);
            }
        },this); 
        this.sequenceFormatStoreRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'value'}
        ]);
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            }, this.sequenceFormatStoreRec),
            url: "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams: {
                mode: Wtf.MRP_Work_Centre_MODULENAME,
                isEdit: this.isEdit
            }
        });
        this.sequenceFormatStore.on('load', this.setNextNumber, this);
       
        this.workCenterRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);

        this.workCenterStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 37
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.workCenterRec)
        });
        
        this.workCenterLocationRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);

        this.workCenterLocationStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 51
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.workCenterLocationRec)
        });
        this.workCenterTypeRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);

        this.workCenterTypeStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 38
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.workCenterTypeRec)
        });
        this.workCenterTypeStore.on("load",function(){
            if (this.isEdit) {
                this.populateForm();
            }
        },this);
        
            this.workCenterCapacityRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);

        this.workCenterCapacityStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 39
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.workCenterCapacityRec)
        });
        
             this.workCentermanagerRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);

        this.workCenterManagerStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 40
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.workCentermanagerRec)
        });
        
        this.workTypeRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);

        this.workTypeStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 41
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.workTypeRec)
        });
        
        this.productRec = Wtf.data.Record.create([
            {name: 'productid'},
            {name: 'pid'},
            {name: 'type'},
            {name: 'productname'},
            {name: 'desc'},
            {name: 'producttype'},
            {name: 'hasAccess',type :"boolean"}
        ]);

        this.productStore = new Wtf.data.Store({
            url: "ACCProduct/getProductsForCombo.do",
            baseParams: {
                mode: 22,
                onlyProduct: true,
                isFixedAsset: false,
                type:Wtf.producttype.assembly,
                includeBothFixedAssetAndProductFlag: false
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.productRec)
        });
        
        /* 
         * Beforeload event is handled on store productStore for flag isProductUsedInWorkOrder and id send to baseParams 
         * 
         * */
        this.productStore.on("beforeload",function(store, options) {
            if (!options.params) {
                options.params = {};
            }
                
            var currentBaseParams = store.baseParams;
            currentBaseParams.isEdit = !(this.isEdit && !this.isClone) ? false : true;
            
            /*
             * This flag is used to check Products mapped in Work Order
             */
            currentBaseParams.isProductUsedInWorkOrder = true;
            if(this.record && this.record.data && this.record.data.id!=undefined && this.record.data.id!="") {
                currentBaseParams.workcenterid = this.record.data.id;
            }
            store.baseParams = currentBaseParams;
        }, this);
        this.productStore.on("load",function() {
            if (this.isEdit) {
                this.mappedProductID.setValue(this.record.data.productid);
                this.materialStore.load();  // Loading material store on product load
            }
        },this); 
        
        
        Wtf.TitleStore.on('load', this.setPersonTitle, this);


    },
    
    createFields: function() {
        this.workCentreName = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.mrp.field.workCentreName") + "'>" + WtfGlobal.getLocaleText("acc.mrp.field.workCentreName") + "*</span>", 
            name: 'name',
            forceSelection: true,
            allowBlank: false,
            width: 200
        });
       
        this.sequenceFormatCombobox = new Wtf.form.ComboBox({
            triggerAction: 'all',
            mode: 'local',
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.Sequenceformat.tip") + "'>" + WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat") + "</span>",
            valueField: 'id',
            displayField: 'value',
            store: this.sequenceFormatStore,
            width: 200,
            typeAhead: true,
            forceSelection: true,
            name: 'seqformat',
            hiddenName: 'sequenceformat',
            allowBlank: false
        });
        this.sequenceFormatStore.load();
        this.sequenceFormatCombobox.on('select', this.getNextSequenceNumber, this);

        this.workcentreID = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.WorkCentreID")+"*",
            name: 'workcenterid',
            id: this.id + 'acccode',
            allowBlank: false,
            width: 200,
            maxLength: 50
        });

        this.workCentreType = new Wtf.form.ExtFnComboBox({
            triggerAction: 'all',
            mode: 'remote',
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.WorkCentreType")+"*",
            valueField: 'id',
            displayField: 'name',
            store: this.workCenterTypeStore,
            width: 200,
            typeAhead: true,
            forceSelection: true,
            name: 'workcentertype',
            addNewFn:this.addWCType.createDelegate(this),
            extraFields:[],
            hiddenName: 'sequenceformat',
            allowBlank: false,
            emptyText: WtfGlobal.getLocaleText("acc.mrp.field.prefix.select") +" "+ WtfGlobal.getLocaleText("acc.mrp.field.WorkCentreType")
        });
        this.mappedProductID = new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.mrp.field.AssemblyProduct") + "'>" + WtfGlobal.getLocaleText("acc.mrp.field.AssemblyProduct") + "</span>",
            forceSelection: true
        }, {
            name: 'product',
            id: "productName" + this.id,
            width: 200,
            emptyText: WtfGlobal.getLocaleText("acc.mrp.field.prefix.select") +" "+WtfGlobal.getLocaleText("acc.mrp.field.AssemblyProduct"),
            store: this.productStore,
            valueField: "productid",
            triggerAction: 'all',
            mode:'remote',
            addCreateOpt:false,
            addNewFn:this.addProduct.createDelegate(this),
            displayField: "productname",
            addNoneRecord: true,
            clearTrigger: (this.isEdit && !this.isClone) ? false : true, // These clearTrigger config is used for remove cross button in combo
            isNotEditable: !(this.isEdit && !this.isClone) ? false : true, // These isNotEditable config is used for editable false text in combo
            isProductCombo: true // These isProductCombo config is used for show topltip and predefined XTemplate.
        }));
        
        
        /* Combo mappedProductID handled beforeselect event- If hasAccess false then record of combo disabled */
        this.mappedProductID.on("beforeselect",function(combo,rec,index){
            return rec.data.hasAccess;
        },this);
        
        
        this.mappedProductID.on("select",function(){
            this.mappedMaterialID.enable();
            this.mappedMaterialID.reset();
            this.loadMaterial(this.mappedProductID.getValue());
        },this);
        
        this.mappedProductID.on("clearval",function(){
            this.mappedMaterialID.reset();
            this.mappedMaterialID.disable();
            this.loadMaterial(this.mappedProductID.getValue());
        },this);
        this.mappedProductID.on("unselect",function(){
            if (!this.mappedProductID.getValue()) {
                this.mappedMaterialID.disable();
            }
            this.mappedMaterialID.reset();
            this.loadMaterial(this.mappedProductID.getValue());
        },this);
        this.mappedMaterialID = new Wtf.common.Select(Wtf.apply({   //ERP-29508 : BOM in Work Centre Form
            multiSelect: true,
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.mrp.field.MaterialOrBOM") + "'>" + WtfGlobal.getLocaleText("acc.mrp.field.MaterialOrBOM") + "</span>",
            forceSelection: true
        }, {
            name: 'materialID',
            width: 200,
            disabled:true,
            emptyText: WtfGlobal.getLocaleText("acc.mrp.field.prefix.select") +" "+WtfGlobal.getLocaleText("acc.mrp.field.MaterialOrBOM"),
            store: this.materialStore,
            valueField: 'id',
            displayField: 'bomcode',
            addNoneRecord: true,
            extraFields: ['name']
//            allowBlank: false
        }));
        
        this.mappedMaterialID.on('beforeclearval',function(){   //ERP-29508
            return !this.isEdit;               
        },this);
        
        this.mappedLabour = new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.mrp.field.MappedLabourID") + "'>" + WtfGlobal.getLocaleText("acc.mrp.field.MappedLabourID")+"*" + "</span>",
            forceSelection: true
        }, {
            name: 'labourname',
            width: 200,
            emptyText: WtfGlobal.getLocaleText("acc.mrp.field.prefix.select") +" "+WtfGlobal.getLocaleText("acc.mrp.field.MappedLabourID"),
            store: this.labourStore,
            triggerAction: 'all',
            mode:'remote',
            addCreateOpt:false,
            addNewFn:this.addLabour.createDelegate(this),
            valueField: 'id',
            displayField: 'name',
            extraFields:['empcode'],
            addNoneRecord: true,
            allowBlank: false
        }));
       this.labourStore.load();
        this.mappedMachine = new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.mrp.field.MappedMachineID") + "'>" + WtfGlobal.getLocaleText("acc.mrp.field.MappedMachineID")+"*" + "</span>",
//            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.MappedMachineID"),
            forceSelection: true
        },{
            name: 'machinename',
            width: 200,
            emptyText: WtfGlobal.getLocaleText("acc.mrp.field.prefix.select") +" "+WtfGlobal.getLocaleText("acc.mrp.field.MappedMachineID"),
            store: this.machineStore,
            valueField: 'id',
            triggerAction: 'all',
            mode:'remote',
            addCreateOpt:false,
            addNewFn:this.addMachine.createDelegate(this),
            displayField: 'name',
            extraFields:['machineid'],
            addNoneRecord: true,
            allowBlank: false
        }));
        this.machineStore.load();
        this.workcentreLocation = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.WorkCentreLocation")+"*",
            store:this.workCenterLocationStore,
            name: 'workcenterlocation',
            displayField: 'name',
            valueField: 'id',
            emptyText: WtfGlobal.getLocaleText("acc.mrp.field.prefix.select") +" "+WtfGlobal.getLocaleText("acc.mrp.field.WorkCentreLocation"),
            mode: 'remote',
            extraFields:[],
            triggerAction: 'all',
            addNewFn:this.addWCLocation.createDelegate(this),
            width:200,
            selectOnFocus: true,
            allowBlank: false
        });
        this.workCenterLocationStore.load();
        
        
        this.warehouseID = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.WarehouseID")+"*",
            store: Wtf.inventoryStore,
            name: 'storeid',
            displayField: 'storedescription',
            valueField: 'storeid',
            extraFields:[],
            emptyText: WtfGlobal.getLocaleText("acc.mrp.field.prefix.select") +" "+WtfGlobal.getLocaleText("acc.mrp.field.WarehouseID"),
            mode: 'remote',
            valueField:'id',
            displayField:'name',
            store:Wtf.inventoryStore,
            width:200,
            typeAhead: true,
            forceSelection: true,
            allowBlank: false,
            addNewFn:this.addWareHouse.createDelegate(this),
            name:'warehouse',
            hiddenName: 'location'
        });
        
        /**
         * (ERP-37673)
         * Remove workCentreCapacity number field from Work Center Form.
         */
//        
//        this.workCentreCapacity = new Wtf.form.NumberField({
////            triggerAction: 'all',
////            mode: 'local',
//            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.WorkCentreCapacity")+"*",
////            valueField: 'id',
////            displayField: 'value',
////            store: this.workCenterCapacityStore,
//            width: 200,
////            typeAhead: true,
////            forceSelection: true,
//            name: 'workcentercapacity',
////            hiddenName: 'sequenceformat',s
//            allowBlank: false
//        });

        this.workType = new Wtf.form.ExtFnComboBox({
            triggerAction: 'all',
            mode: 'remote',
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.WorkType")+"*",
            valueField: 'id',
            displayField: 'name',
            emptyText: WtfGlobal.getLocaleText("acc.mrp.field.prefix.select") +" "+WtfGlobal.getLocaleText("acc.mrp.field.WorkType"),
            store: this.workTypeStore,
            width: 200,
            extraFields:[],
            typeAhead: true,
            forceSelection: true,
            name: 'worktype',
            hiddenName: 'sequenceformat',
            addNewFn:this.addWorkType.createDelegate(this),
            allowBlank: false
            
        });


//        this.routingCode = new Wtf.form.ComboBox({
//            triggerAction: 'all',
//            mode: 'local',
//            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.RoutingCode"),
//            valueField: 'id',
//            displayField: 'value',
//            store: this.sequenceFormatStore,
//            width: 200,
//            typeAhead: true,
//            forceSelection: true,
//            name: 'routingcode',
//            hiddenName: 'sequenceformat',
//            allowBlank: false
//        });

        this.workCentreManager = new Wtf.form.ExtFnComboBox({
            triggerAction: 'all',
            mode: 'remote',
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.WorkCentreManager")+"*",
            valueField: 'id',
            displayField: 'name',
            emptyText: WtfGlobal.getLocaleText("acc.mrp.field.prefix.select") +" "+WtfGlobal.getLocaleText("acc.mrp.field.WorkCentreManager"),
            store: this.workCenterManagerStore,
            width: 200,
            extraFields:[],
            typeAhead: true,
            forceSelection: true,
            name: 'workcentermanager',
            hiddenName: 'sequenceformat',
            addNewFn:this.addWCmanager.createDelegate(this),
            allowBlank: false
        });
        
        this.costCentre = new Wtf.form.ExtFnComboBox({
            triggerAction: 'all',
            mode: 'remote',
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.CostCenter"),
            valueField: 'id',
            displayField: 'name',
            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['ccid'] : [],
            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 500 : 400,
            emptyText: WtfGlobal.getLocaleText("acc.mrp.field.prefix.select") + " " + WtfGlobal.getLocaleText("acc.mrp.field.CostCenter"),
            store: this.costCenterStore,
            width: 200,
            typeAhead: true,
            forceSelection: true,
            addNewFn: this.addCostCenter.createDelegate(this),
            name: 'costcenter',
            isProductCombo: true,
            extraComparisionField: 'ccid',
            hiddenName: "costcenter",
            hirarchical: true
        });
        this.costCenterStore.load();
        
              
        
    },
    createCustomFields: function () {
        this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId: this.id + 'Northform',
            autoHeight: true,
            moduleid: Wtf.MRP_Work_Centre_ModuleID,
            isEdit: this.isEdit,
            record: this.record,
            isViewMode: this.readOnly,
            parentcompId: this.id
        });
    },
    
    loadMaterial: function(val) {
      this.materialStore.load({
        params:{
            productid : val
        }  
      });
    },
    
    createForm: function() {

          this.WorkCentreInfoForm = new Wtf.form.FormPanel({
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
                    id:  'workcentreformvalidationid',
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
                                this.workCentreName,
                                this.sequenceFormatCombobox,
                                this.workcentreID,
                                this.workCentreType,
                                this.mappedProductID,
                                this.mappedMaterialID,
                                this.mappedLabour
                                
                            ]
                        }, {
                            layout: 'form',
                            columnWidth: 0.49,
                            items: [
                                this.mappedMachine,
                                this.workcentreLocation,
                                this.warehouseID,
                            //  this.workCentreCapacity,
                                this.workType,
                                this.workCentreManager,
                                this.costCentre
                                
                            ]
                        }]
                },this.tagsFieldset]
        });

     
    },
    populateForm: function() {
        this.workCentreName.setValue(this.record.data.name);
        this.sequenceFormatCombobox.setValue(this.record.data.seqformat);
        this.workcentreID.setValue(this.record.data.workcenterid);
        this.workCentreType.setValForRemoteStore(this.record.data.workcentertypeid, this.record.data.workcentertype);
//        this.workCentreType.setValue(this.record.data.workcentertypeid);
        this.mappedProductID.setValue(this.record.data.productid);
        this.mappedMaterialID.setValue(this.record.data.materialid);
        this.mappedLabour.setValue(this.record.data.labourid);
        this.mappedMachine.setValue("this.record.data");
        this.workcentreLocation.setValForRemoteStore(this.record.data.workcenterlocationid, this.record.data.workcenterlocation);
//        this.workcentreLocation.setValue(this.record.data.workcenterlocationid);
        this.warehouseID.setValForRemoteStore(this.record.data.warehouseid, this.record.data.warehouse);
//        this.warehouseID.setValue(this.record.data.warehouseid);
//       this.workCentreCapacity.setValue(this.record.data.workcentercapacity);
        this.workType.setValForRemoteStore(this.record.data.worktypeid, this.record.data.worktype);
//        this.workType.setValue(this.record.data.worktypeid);
        this.workCentreManager.setValForRemoteStore(this.record.data.workcentermanagerid, this.record.data.workcentermanager);
//        this.workCentreManager.setValue(this.record.data.workcentermanagerid);
        this.costCentre.setValForRemoteStore(this.record.data.costcenterid, this.record.data.costcenter);
//        this.costCentre.setValue(this.record.data.costcenterid);
        this.id = this.record.data.id;
    },
    
    createButton: function() {
        this.saveBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
            tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
            id: "save" + this.heplmodeid + this.id,
            scope: this,
            handler: function() {
                this.save();
            },
            iconCls: 'pwnd save'
        });
        
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
        this.buttonArray = new Array();
        this.buttonArray.push(this.saveBttn);
    },
    
    createPanel: function() {
        this.centerPanel = new Wtf.Panel({
            region: 'center',
            autoScroll: true,
            bodyStyle: ' background: none repeat scroll 0 0 #DFE8F6;',
            items: [this.WorkCentreInfoForm],
             bbar: this.buttonArray
        });
    },
    
    addMaster: function(id, store) {
        addMasterItemWindow(id);
        Wtf.getCmp('masterconfiguration').on('update', function() {
            store.reload();
        }, this);
    },
    
    
    addSelectedDocument: function() {
        var url = "";

        if (this.fromLinkCombo.getValue() == 5) {
            url = "ACCPurchaseOrderCMN/getRequisitions.do";
        }

    },
    
    save: function() {
        var isValidNorthForm = this.WorkCentreInfoForm.getForm().isValid();
        var isValidCustomFields = this.tagsFieldset.checkMendatoryCombo();
        if((this.mappedProductID.getValue()!=null && this.mappedProductID.getValue()!=undefined && this.mappedProductID.getValue()!="") && ((this.mappedMaterialID.getValue()==null || this.mappedMaterialID.getValue()==undefined || this.mappedMaterialID.getValue()=="") && this.mappedMaterialID.getRawValue()!="")){
            //ERP-31663 : BOM Code is mandatory, if user has selected Assembly Product while creating Work Center.
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mrp.workcentre.field.bomcode.mandatory")], 2);   //Please select BOM Code for selected Product(s).
            return ;            
        }
        if (!isValidNorthForm || !isValidCustomFields) {
            WtfGlobal.dispalyErrorMessageDetails("workcentreformvalidationid", this.getInvalidFields());
            this.WorkCentreInfoForm.doLayout();
            return;
        } else {
            Wtf.getCmp("workcentreformvalidationid").hide();
        }

        if (isValidNorthForm) {
            this.saveData();
        }

    },
    getInvalidFields: function() {
        var invalidFields = []
        this.WorkCentreInfoForm.getForm().items.filterBy(function(field) {
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
    setNextNumber: function (store) {
        if (this.sequenceFormatStore.getCount() > 0) {
            if (this.isEdit && !this.isClone) {
            var index = this.sequenceFormatStore.find('id', this.record.data.seqformat);  
                if (index != -1) {
                    this.sequenceFormatCombobox.setValue(this.record.data.seqformat);
                    this.sequenceFormatCombobox.disable();
                    this.workcentreID.disable();
                } else {
                    this.sequenceFormatCombobox.setValue("NA");
                    this.sequenceFormatCombobox.disable();
                    this.workcentreID.enable();
                }
            } else if (this.isClone) {
                var index = this.sequenceFormatStore.find('id', this.record.data.seqformat);
                if (index != -1) {
                    var count = this.sequenceFormatStore.getCount();
                    var seqRec = this.sequenceFormatStore.getAt(0);
                    this.sequenceFormatCombobox.setValue(seqRec.data.id);
                    for (var i = 0; i < count; i++) {
                        var seqRec = this.sequenceFormatStore.getAt(i)
                        if (seqRec.json.isdefaultformat == "Yes") {
                            this.sequenceFormatCombobox.setValue(seqRec.data.id)
                            break;
                        }
                    }

                    this.getNextSequenceNumber(this.sequenceFormatCombobox);
                } else {
                    this.sequenceFormatCombobox.setValue("NA");
                    this.sequenceFormatCombobox.enable();
                    this.workcentreID.setValue("");
                    this.workcentreID.enable();
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
                if(this.sequenceFormatCombobox.getValue()!=""){// If any sequence format assigned to sequenceFormatCombobox then It will go to fetch next sequence number
                    this.getNextSequenceNumber(this.sequenceFormatCombobox);
                } else{// other wise document number will remain hidden
                    this.workcentreID.setValue("");
                    this.workcentreID.disable();
                }
            }
        }
    },
    getNextSequenceNumber: function (combo) {
        if (combo.getValue() == "NA" || combo.getValue() == "") {
            this.workcentreID.reset();
            this.workcentreID.enable();
        } else {
            Wtf.Ajax.requestEx({
                url: "ACCCompanyPref/getNextAutoNumber.do",
                params: {
                    from: Wtf.MRP_Work_Centre_ModuleID,
                    sequenceformat: combo.getValue()
                }
            }, this, function (resp) {
                if (resp.data == "NA") {
                    this.workcentreID.reset();
                    this.workcentreID.enable();
                } else {
                    this.workcentreID.setValue(resp.data);
                    this.workcentreID.disable();
                }
            });
        }
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
                var jsonObject = this.WorkCentreInfoForm.form.getValues();
                jsonObject.workcentertypeid = this.workCentreType.getValue()
                jsonObject.warehouseid =this.warehouseID.getValue()
                jsonObject.worktypeid =this.workType.getValue()
                jsonObject.workcentermanagerid =this.workCentreManager.getValue();
                jsonObject.costcenterid = this.costCentre.getValue();
                jsonObject.labourid = this.mappedLabour.getValue();
                jsonObject.productid = this.mappedProductID.getValue();
                jsonObject.materialid = this.mappedMaterialID.getValue();
                jsonObject.workcenterlocationid = this.workcentreLocation.getValue();
                jsonObject.machineid = this.mappedMachine.getValue();
                jsonObject.seqformat = this.sequenceFormatCombobox.getValue();
                var seqFormatRec = WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                jsonObject.seqformat_oldflag = seqFormatRec != null ? seqFormatRec.get('oldflag') : true;
                var custFieldArr = this.tagsFieldset.createFieldValuesArray();
                if (custFieldArr.length > 0)
                    jsonObject.customfield = JSON.stringify(custFieldArr);
                if (this.isEdit && !this.isClone) {
                    jsonObject.id = this.id;
                }
                WtfGlobal.onFormSumbitGetDisableFieldValues(this.WorkCentreInfoForm.form.items, jsonObject);
                this.disableComponent();
                var array = [];
                array.push(jsonObject);
                var obj = {};
                obj.data  = array;
//                WtfComMsgBox(27, 4, true);
                this.ajxUrl = "ACCWorkCentreCMN/saveWorkCentre.do";
                Wtf.Ajax.requestEx({
                    url: this.ajxUrl,
                    params: {
                        data:JSON.stringify(obj),
                        isEdit:this.isEdit && !this.isClone
                    }
                }, this, this.genSuccessResponse, this.genFailureResponse);
            }
        }, this);
    },
    
    enableSaveButton: function() {
        this.saveBttn.enable();
        this.savencreateBttn.enable();
    },
    enableComponent: function() {
      if (this.WorkCentreInfoForm  ) {
          this.WorkCentreInfoForm.enable()
      }
    },
    disableComponent: function() {
        if (this.saveBttn) {
            this.saveBttn.disable();
            if (this.WorkCentreInfoForm) {
                this.WorkCentreInfoForm.disable();
            }
        }
    },
    genSuccessResponse: function(response, request) {
         Wtf.updateProgress();
        if (response.success) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], 0);
            this.workcentreReport.fetchStatement();
            this.disableComponent();
        }
        if (this.saveAndCreateNewFlag) {
            this.enableSaveButton();
            this.resetComponents();
            this.tagsFieldset.resetCustomComponents();
            
        } else {
            this.disableComponent();
        }
        if (response.success) {
            this.disableComponent();
        }
        
        if (!response.success) {    // Handling the messages
            this.enableSaveButton();
            this.enableComponent();
            var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
            if (response.msg) {
                msg = response.msg;
            }
            if ( response.isDuplicateExe ) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
            } else {
                
            this.newdowin = new Wtf.Window({
                    title: WtfGlobal.getLocaleText("acc.common.success"),
                    closable: true,
                    iconCls: getButtonIconCls(Wtf.etype.deskera),
                    width: 330,
                    autoHeight: true,
                    modal: true,
                    bodyStyle: "background-color:#f1f1f1;",
                    closable:false,
                    buttonAlign: 'right',
                    items: [new Wtf.Panel({
                        border: false,
                        html: (response.msg.length>60)?response.msg:"<br>"+response.msg,
                        height: 50,
                        bodyStyle: "background-color:white; padding: 7px; font-size: 11px; border-bottom: 1px solid #bfbfbf;"
                    }),
                    this.newdoForm = new Wtf.form.FormPanel({
                        labelWidth: 190,
                        border: false,
                        autoHeight: true,
                        bodyStyle: 'padding:10px 5px 3px; ',
                        autoWidth: true,
                        defaultType: 'textfield',
                        items: [this.newdono = new Wtf.form.TextField({
                            fieldLabel: WtfGlobal.getLocaleText("acc.WC.newWorkCentreno"),
                            allowBlank: false,
                            labelSeparator: '',
                            width: 90,
                            itemCls: 'nextlinetextfield',
                            name: 'newdono',
                            id: 'newdono'
                        })],
                        buttons: [{
                            text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                            handler: function () {
                                if (this.newdono.validate()) {
                                                    
                                    this.workcentreID.setValue(this.newdono.getValue());
                                                            
                                    this.save();
                                    this.newdowin.close();
                                }
                            },
                            scope: this
                        }, {
                            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"), //"Cancel",
                            scope: this,
                            handler: function () {
                                this.newdowin.close();
                            }
                        }]
                    })]
                });
                this.newdowin.show();
            }
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        }
        
        
    },
    genFailureResponse: function(response) {
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        
    },
    addWCType: function() {
        addMasterItemWindow('38');
    },
    addProduct: function() {
        callProductDetails();
    },
    addLabour: function() {
       callLabourInformation(false, null); 
    },
    addMachine: function() {
        var obj = {};
        obj.isSubstituteMachine = false;
        obj.isEdit = false;
        callMRPMachineMaster(obj);
    },
    addWCLocation: function() {
        addMasterItemWindow('51');
    },
    addWareHouse: function() {
        callStoreMaster();
    },
    addWorkType: function() {
        addMasterItemWindow('41');
    },
    addWCmanager: function() {
        addMasterItemWindow('40');
    },
    addCostCenter: function() {
        callCostCenter();
    }
});