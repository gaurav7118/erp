Wtf.account.WorkOrderEntryForm=function(config){
    this.projectId=config.projectId
    this.isEdit = config.isEdit;   // Take isEdit flag above all function
    this.isBOMChanged = !this.isEdit; // Check to track BOM Change on Edit
    this.workorderreport = config.workorderreport;
    this.productQtyinSalesOrder=0;
    this.addProjectPanel(this.projectId);
    this.createCAPanel();
    this.taskPlanned = this.isEdit;
    this.inProcess = false;    // added a variable to check if the wo is in process
    this.LinkToType={
        NONE:"0",
        SALESORDER:"1",
        SALESCONTRACT:"2"
    };
    Wtf.apply(this, config);
    Wtf.account.WorkOrderEntryForm.superclass.constructor.call(this,config);
};

Wtf.extend(Wtf.account.WorkOrderEntryForm,Wtf.Panel,{
    autoScroll: true,
    bodyStyle: {background:"#DFE8F6 none repeat scroll 0 0"},
    border:'false',
    closable : false,
//    autoHeight:true,
    onRender:function(config){   
        this.add(this.mainPanel);   
        this.configureLinking(undefined,false);
        this.loadStores();
        WtfGlobal.hideFormElement(this.wareHouseCombo);
        WtfGlobal.hideFormElement(this.locationMultiSelect);
        Wtf.account.WorkOrderEntryForm.superclass.onRender.call(this, config);  
    },
    loadStores :function () {
        this.machineStore.load();
        this.materialStore.load();
        this.labourStore.load();
        this.workCentreStore.load();
//        this.workOrderStatusStore.load(); //ERP-40324 : Defined this store globally in WtfSettings.js and loaded while loading MRP
        this.sequenceFormatStore.load(); // Loading sequence format store after load of work Order Status Store as loadrecord function is called on its on load.
        this.productStore.load();
    },
    loadrecord: function () {
        if (this.isEdit) {
            this.workOrderPanel.getForm().loadRecord(this.record);
            this.customerNameCombo.setValForRemoteStore(this.record.data.customerid, this.record.data.customer);
//            this.productNameCombo.setValForRemoteStore(this.record.data.productid, this.record.data.pid);
            
            this.workOrderStatusText.setValForRemoteStore(this.record.data.workorderstatusid, this.record.data.workorderstatus);
            this.routingCodeCombo.setValForRemoteStore(this.record.data.routetemplateid, this.record.data.routetemplatename);
            this.workOrderTypeCombo.setValForRemoteStore(this.record.data.workordertypeid, this.record.data.workordertype);
            this.workCentreCombo.setValForRemoteStore(this.record.data.workcentreid, this.record.data.workcentrename);
            this.machineIDCombo.setValForRemoteStore(this.record.data.machineid, this.record.data.machinename);
            this.labourIDCombo.setValForRemoteStore(this.record.data.labourid, this.record.data.labourname);
            this.fromLinkCombo.setValue(this.record.data.fromLinkCombo);
            this.materialIDCombo.setValForRemoteStore(this.record.data.materialid, this.record.data.materialname);
            this.productQtyinSalesOrder=this.record.data.quantity;
//            if(this.record.data.isWarehouseForProduct ==true && this.record.data.isLocationForProduct ==true){
//               this.wareHouseCombo.setValForRemoteStore(this.record.data.warehouse, this.record.data.warehousename);
//               this.locationMultiSelect.setValForRemoteStore(this.record.data.location, this.record.data.locationname);
//            }
            
            this.loadlinkingDocumentStore();
            // Code to check whether the WO is (In Process,In Built Stage,In Release Stage) at edit time
//            var statusRec = WtfGlobal.searchRecord(this.workOrderStatusStore, Wtf.WODefaultStatus.planned, 'defaultMasterItem');  //ERP-40324 : Defined this store globally in WtfSettings.js
            var statusRec = WtfGlobal.searchRecord(Wtf.WOStatusStore, Wtf.WODefaultStatus.planned, 'defaultMasterItem');
            if (statusRec) {
                var statusid = statusRec.data.id;
                if (statusid != this.record.data.workorderstatusid) {
                    this.inProcess = true;
                    this.enableDisableFields();  // if WO is (In Process,In Built Stage,In Release Stage) then not allowing to edit any field
                }
            }
            if (this.routingTypeCombo.getValue() != 0) {
                this.routingCodeCombo.enable();
            } else {
                this.routingCodeCombo.disable();
            }
        } else {
            //code to set planned status as default at new WO creation time
//            var rec = WtfGlobal.searchRecord(this.workOrderStatusStore, Wtf.WODefaultStatus.planned, 'defaultMasterItem');    //ERP-40324 : Defined this store globally in WtfSettings.js
            var rec = WtfGlobal.searchRecord(Wtf.WOStatusStore, Wtf.WODefaultStatus.planned, 'defaultMasterItem');
                if (rec) {
                    this.workOrderStatusText.setValue(rec.data.id)
                }
            this.quantity.setValue(1); //setting default qty to 1
            this.workOrderDate.setValue(new Date()); //setting default WO Date to current date.
        }
        this.deliveryDateText.minValue = this.workOrderDate.getValue();//Set minValue for Date of Delivery to workorder date
    },
    initComponent:function(config){
        Wtf.account.WorkOrderEntryForm.superclass.initComponent.call(this,config);
        
        this.createCustomFields();
        // Store Records -------------------------------------------------------------------------------------
        this.sequenceFormatStoreRec = new Wtf.data.Record.create([
            {
                name: 'id'
            },{
                name: 'value'
            },{
                name: 'oldflag'
            }
        ]);
//        this.productRec = Wtf.data.Record.create ([
//            {name:'productid'},
//            {name:'pid'},
//            {name:'type'},
//            {name:'productname'},
//            {name:'desc'},
//            {name: 'producttype'}
//        ]);
        this.productRec=Wtf.productRec;
        this.machineRec = Wtf.data.Record.create ([
            {name:'id'},
            {name:'type'},
            {name:'machineid'},
            {name:'name'},
        ]);
        this.materialRec = Wtf.data.Record.create ([
            {name:'id'},
            {name:'type'},
            {name:'name'},
            {name:'bomcode'}
            
        ]);
        this.labourRec = Wtf.data.Record.create ([
            {name:'id'},
            {name:'name'},
            {name:'empcode'}
        ]);
        this.routingRec = Wtf.data.Record.create ([
            {name:'id'},
            {name:'routingcode'},
            {name:'projectId'},
            {name:'routingtname'},
            {name:'projectId'},
            {name:'workcenter'},
            {name:'labourid'},
            {name:'machineid'}
        ]);
        this.workCentreRec = Wtf.data.Record.create ([
            {name:'id'},
            {name:'name'},
            {name: 'workcenterid'}
        ]);
        this.workOrderTypeRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
//        this.workOrderStatusRec = Wtf.data.Record.create([    //ERP-40324 : Defined this store globally in WtfSettings.js
//            {name: 'id'},
//            {name: 'name'},
//            {name: 'defaultMasterItem'}
//        ]);
        this.SOSCrec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
        
        // ---------------------------------------------------------------------------------------------------
        // Stores --------------------------------------------------------------------------------------------
        this.SOSCStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.SOSCrec),
            url : "ACCWorkOrder/getSOSCForCombo.do",
            baseParams:{
                mode:Wtf.MRP_Work_Order_MODULENAME,
                isEdit: this.isEdit
            }
        });
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.sequenceFormatStoreRec),
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:Wtf.MRP_Work_Order_MODULENAME,
                isEdit: this.isEdit
            }
        });
        this.sequenceFormatStore.on('load', this.setNextNumber, this);
        this.workOrderTypeStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid:49
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.workOrderTypeRec)
        });
//        this.workOrderStatusStore = new Wtf.data.Store({  //ERP-40324 : Defined this store globally in WtfSettings.js
//            url: "ACCMaster/getMasterItems.do",
//            baseParams: {
//                mode: 112,
//                groupid: 50
//            },
//            reader: new Wtf.data.KwlJsonReader({
//                root: "data"
//            }, this.workOrderStatusRec)
//        });
        this.linkStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['Yes',true],['No',false]]
        });
        this.fromlinkStore = new Wtf.data.SimpleStore({
            fields:[{
                name:'name'
            },{
                name:'value'
            }],
            data:[["None",'0'],["Sales Order",'1'],["Sales Contract",'2']]
        });
        this.productStore = new Wtf.data.Store({
            url: "ACCWorkOrder/getProductsForCombo.do",
            baseParams: {
                mode: 22,
                onlyProduct:true,
                isFixedAsset:false,
                type:Wtf.producttype.assembly,
                includeBothFixedAssetAndProductFlag:false,
                salesorderid:"",
                salescontractid:""
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.productRec)
        });
        this.productStore.on('load', function () {
            if (this.isEdit && this.record) {
                this.productNameCombo.setValue(this.record.data.productid);
            }
        }, this);
        
        this.productStore.on('beforeload',function(){
            var linkFrom = this.fromLinkCombo.getValue();
            if (linkFrom == this.LinkToType.SALESORDER) {
                /*When  user selecxt salesorder to link*/
                this.productStore.baseParams.salesorderid = this.linkDocNoText.getValue();
                this.productStore.baseParams.salescontractid = "";
            } else if(linkFrom == this.LinkToType.SALESCONTRACT){
                /*When  user selecxt sales contract to link*/
                this.productStore.baseParams.salescontractid = this.linkDocNoText.getValue();
                this.productStore.baseParams.salesorderid = "";
            }else{
                /*When  there is no linkeing witrh salesorder and salescontract*/
                this.productStore.baseParams.salescontractid = "";
                this.productStore.baseParams.salesorderid = "";
            }
            
        },this);
        this.machineStore = new Wtf.data.Store({
            url: "ACCMachineMaster/getMachinesForCombo.do",
            baseParams: {
                workcenterid:""
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.machineRec)
        });
        this.machineStore.on("beforeload",function(){
            this.machineStore.baseParams.workcenterid = this.workCentreCombo.getValue();
        },this);
        this.materialStore = new Wtf.data.Store({
            url: "ACCProductCMN/getBOMforCombo.do",
            baseParams: {
                productid :'' 
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.materialRec)
        });
        this.materialStore.on("beforeload",function(){
            this.materialStore.baseParams.productid = this.productNameCombo.getValue();
        },this);
        this.labourStore = new Wtf.data.Store({
            url: "ACCLabourCMN/getLabourForCombo.do",
            baseParams: {
                workcenterid:""
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.labourRec)
        });
        this.labourStore.on("beforeload",function(){
            this.labourStore.baseParams.workcenterid = this.workCentreCombo.getValue();
        },this);
        
          this.routingTypeStore = new Wtf.data.SimpleStore({
            fields: [{name: 'id'}, {name: 'name'}],
            data: [[Wtf.MRP_ROUTING_NONE,0],[Wtf.MRP_ROUTING_TEMPLATE,1], [Wtf.MRP_ROUTING_CODE,2]]
        });


        this.routingStore = new Wtf.data.Store({
            url: "ACCRoutingManagement/getRoutingTemplates.do",
            baseParams: {
                isforcombo: true,
                bomid: '',
                routingmastertype: 0
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.routingRec)
        });
        this.workCentreStore = new Wtf.data.Store({
            url: "ACCWorkCentreCMN/getWorkcentresForCombo.do",
            baseParams: {
                productid:""
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.workCentreRec)
        });
        this.workCentreStore.on("beforeload",function(){
            this.workCentreStore.baseParams.productid = this.productNameCombo.getValue();
        },this);
        
        this.warehouseStoreRec = new Wtf.data.Record.create([//  warehouse record
            {name: 'id'},
            {name: 'name'},
            {name: 'parentid'},
            {name: 'company'},
            {name: 'parentname'},
            {name: 'warehouse'},
        ]);
        this.orderWareHouseStore = new Wtf.data.Store({  //  warehouse store for combo box
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"
        },this.warehouseStoreRec),
        url:"ACCMaster/getWarehouseItems.do"
        });
        
        this.orderWareHouseStore.load();
  
    this.locationRec = new Wtf.data.Record.create([
    {name:"id"},
    {name:"name"},
    {name: 'parentid'},
    {name: 'parentname'}
    ]);
    this.locationReader = new Wtf.data.KwlJsonReader({
        root:"data"
    },this.locationRec);
    var locationStoreUrl="ACCMaster/getLocationItems.do"
    if(Wtf.account.companyAccountPref.activateInventoryTab){
        locationStoreUrl="ACCMaster/getLocationItemsFromStore.do";
    }
    this.locationStore = new Wtf.data.Store({
        url:locationStoreUrl,
        reader:this.locationReader
    });
    this.locationStore.load();
       
        //----------------------------------------------------------------------------------------------------
        
        // Fields --------------------------------------------------------------------------------------------
        // -Left Panel Fields --------------------------------------------------------------------------------
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
//        this.sequenceFormatStore.load();
        this.sequenceFormatStore.on('load',this.loadrecord,this);
        this.sequenceFormatCombobox.on('select', this.getNextSequenceNumber, this);
        this.workOrderIDText = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("mrp.workorder.entry.workorderid") + "*",
            ctCls:"fieldmargin",
            name: 'workorderid',
            id:"workOrderID"+this.id,
            hidden:false,
            width : 180,
            maxLength:50,
            scope:this,
            allowBlank:false
        });      
        this.workOrderNameText = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("mrp.workorder.entry.workordername") + "*",
            name: 'workordername',
            id:"workOrderName"+this.id,
            hidden:false,
            width : 180,
            maxLength:50,
            ctCls:"fieldmargin",
            scope:this,
            allowBlank:false
        });      
        this.customerNameCombo= new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("mrp.workorder.entry.customer"),
            id:"customerName"+this.id,
            store: Wtf.customerAccRemoteStore,
            name: "customer",
            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
            valueField:'accid',
            displayField:'accname',
            minChars:1,
//            listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
            hirarchical:true,
            addNewFn:this.addCustomer.createDelegate(this),
            emptyText:"Select Customer" , 
            mode: 'remote',
            typeAhead: true,
            extraComparisionField:'acccode',
            typeAheadDelay:30000,
            forceSelection: true,
            selectOnFocus:true,
            isVendor:false,
            isCustomer:true,
            width : 180,
            triggerAction:'all',
            scope:this
        });  
        this.customerNameCombo.on("change", this.handleCustomerChange, this); //on change of customer clear fromlinkcombo,link doc no and product
          
        this.fromLinkCombo= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Linkto"), 
            name:'fromLinkCombo',
            hiddenName:'fromLinkCombo',
            triggerAction:'all',
            mode: 'local',
            valueField:'value',
            id:'fromLinkComboId'+this.id,
            displayField:'name',
            store:this.fromlinkStore,                        
            emptyText: "Select Link To",
//            allowBlank:false,            
            typeAhead: true, 
            width:180,
            forceSelection: true,                        
            ctCls:"fieldmargin",
            selectOnFocus:true,           
            scope:this
        });
//        this.fromLinkCombo.on("blur",function(field,newVal,oldVal){
//            this.configureLinking(newVal,true);
//        },this); 
        
        this.fromLinkCombo.on("change",this.handleOnChangeODformLinkCombo,this); //on change of frommlink combo clear doc no combo and product combo
        this.linkDocNoText = new  Wtf.form.ExtFnComboBox({
            //            triggerAction: 'all',
            mode: 'local',
            fieldLabel:WtfGlobal.getLocaleText("mrp.workorder.entry.linkdocno"),
            valueField: 'id',
            displayField: 'name',
            store:this.SOSCStore,
            name: 'linkDocNo',
            hiddenName: 'linkDocNo',
            id:"linkDocNo"+this.id,
            hidden:false,
            width : 180,
            ctCls:"fieldmargin",
            scope:this,
            extraFields:[]
        });
        this.linkDocNoText.on("change",this.handleLinkDOcOnChange,this); //on change of link doc no clear product combo
        this.linkDocNoText.on("select",this.handleLinkDOcOnSelect,this); //on select of link doc no re-load product store
        this.SOSCStore.on('load',this.setDocumentNumber,this);// in edit case set value of linking document no combo
        this.productNameCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.gridProductID")+"*" ,
            name: 'productid',
            store: this.productStore, 
            typeAhead: true,
            isProductCombo: true,
            selectOnFocus: true,
            maxHeight: 250,
            listAlign: "bl-tl?", 
            valueField: 'productid',
            displayField: 'pid',
            extraFields: ['productname'],
            listWidth: 450,
            extraComparisionField: 'pid', 
            extraComparisionFieldArray: ['pid', 'productname'],
            lastQuery: '',
            width : 180,
            //editable:false,
            //                    scope:this,
            hirarchical: true,
            addNewFn: this.addProduct.createDelegate(this),
            forceSelection: true,
            allowBlank:false,
            mode:'local',
            isShowFullProductname:true
        });
        
        this.wareHouseCombo = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.warhouse"),
            valueField:'id',
            displayField:'name',
            store:this.orderWareHouseStore,
            lastQuery:'',
//            disabled:true,
            allowBlank:true,
            typeAhead: true,
            forceSelection: true,
            name:'orderWarehouse',
            hiddenName:'orderWarehouse',
            width : 180
        });

        if(Wtf.account.companyAccountPref.activateInventoryTab){
            this.wareHouseCombo.on('select',function(){
                this.locationStore.load({
                    params:{
                        storeid:this.wareHouseCombo.getValue()
                    }
                });
                this.locationMultiSelect.enable();
            },this);
        }
      
        this.locationMultiSelect = new Wtf.common.Select({
            triggerAction:'all',
            mode: 'local',
            multiSelect:false,
            fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.12"),
            valueField:'id',
            displayField:'name',
            lastQuery:'',
            store:this.locationStore,
            typeAhead: true,
            allowBlank: true,
//            disabled:true,
            forceSelection: true,
            hirarchical:true,
            name:'orderLocation',
            hiddenName:'orderLocation',
            width: 180
        });  
        this.productNameCombo.on("select", this.onProductSelect, this);
        this.productNameCombo.on("change", this.onProductChange, this);
//        this.productStore.load();
        
        this.quantity=new Wtf.form.NumberField({
            allowNegative:false,
            defaultValue:1,
            maxLength: 10,
            minValue :1,
            allowDecimals:true,  //ERP-38383
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL, //ERP-30846
            width:180,
            allowBlank:false,
            fieldLabel:WtfGlobal.getLocaleText("acc.product.gridQty")+"*",  
            name:'quantity',
            id:"quantity"+this.id 
        });
        this.quantity.on("change",function(numberfield,newValue,oldValue){
            var selectedSalesOrderid ="";
            selectedSalesOrderid =this.linkDocNoText.getValue();
            if(selectedSalesOrderid!=undefined && selectedSalesOrderid!=""){
                if(this.productQtyinSalesOrder!=0 && newValue>this.productQtyinSalesOrder){
                    Wtf.MessageBox.alert(WtfGlobal.getLocaleText('acc.common.alert'), WtfGlobal.getLocaleText('acc.mrp.wo.quantity'),function(){                     
                    },this);
                    this.quantity.setValue(oldValue);
                    return false;
                }
                    
            }
            
            if(this.isEdit) {  // change Comp. Avail. grids url at edit time on BOM Change
                this.CAPanel.CAStore.proxy.conn.url = "ACCProduct/getAssemblyItems.do";
                this.isBOMChanged = true;        // setting falg to true in change of BOM
            }
            this.CAPanel.CAStore.load({
                params: {
                    productid: this.productNameCombo.getValue(),
                    mrproductquantity:this.quantity.getValue(),
                    bomdetailid:this.materialIDCombo.getValue()
                }
            }, this);
        },this);
        this.deliveryDateText = new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("mrp.workorder.entry.dateofDelivery")+"*",
            name: 'dateofdelivery',
            id:"deliveryDate"+this.id,
            hidden:false,
            width : 180,
            maxLength:50,
            allowBlank:false,
            scope:this,
            format:WtfGlobal.getOnlyDateFormat(),
            ctCls:"fieldmargin"
        });
        
        /*
        Used showNextMonth() & showPrevMonth() to refresh the date picker as it was not reflecting based on minValue
         */
        this.deliveryDateText.on("focus",function(c){
            this.deliveryDateText.menu.picker.showNextMonth();
            this.deliveryDateText.menu.picker.showPrevMonth();
        },this);
        
        this.workOrderStatusText = new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("mrp.workorder.entry.workorderstatus")+"*",
            name: 'workorderstatus',
            id:"workOrderStatus"+this.id,
//            store:this.workOrderStatusStore,  //ERP-40324 : Defined this store globally in WtfSettings.js
            store:Wtf.WOStatusStore,
            valueField:'id',
            displayField:'name',
            hidden:false,
            width : 180,
            addNewFn:this.addWOStatus.createDelegate(this),
            ctCls:"fieldmargin",
            WOStatus:true,
            isEdit:this.isEdit,
            allowBlank:false,
            scope:this,
            mode: 'remote',
            extraFields:[],
            disabled:true  /* work order status combobox disable (ERP-35110)*/
//            allowBlank:false
        });      
        this.workOrderStatusText.on('beforeselect', function(combo, record, index) {
            this.statusBeforeSelect = combo.getValue();  // Done to Make In Process status disabled at creation time while enable on edit time
            return this.validateSelectionForWOStatus(combo, record, index);
        }, this);
        this.workOrderStatusText.on('change', function(combo, record, index) {
            var index = combo.selectedIndex;
            var rec = combo.store.getAt(index);
            if (rec.data.defaultMasterItem == Wtf.WODefaultStatus.inprocess) {
                this.workorderreport.confirmStart();
            }
        }, this);
//        this.workOrderStatusStore.load(); //ERP-40324 : Defined this store globally in WtfSettings.js and loaded while loading MRP
        //-------------------------------------------------------------------------------------------------
        //-Right Panel Fields ----------------------------------------------------------------------------- 
        this.workOrderTypeCombo= new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("mrp.workorder.entry.workordertype") + "*",
            id:"workOrderType"+this.id,
            width : 180,
            name:"workordertype",
            store:this.workOrderTypeStore,
            valueField:'id',
            allowBlank : false,
            addNewFn:this.addWOType.createDelegate(this),
            forceSelection: true,
            ctCls:"fieldmargin",
            displayField:'name',
            extraFields:[],
            scope:this,
            selectOnFocus:true,
            mode: 'remote'
        });   
        this.workOrderTypeStore.load();
        this.machineIDCombo =new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            fieldLabel:WtfGlobal.getLocaleText("acc.mrp.field.MappedMachineID") + "*",
            forceSelection: true
        }, {
            name: 'machineid',
            hiddenName:'machineid',
            id:"machineID"+this.id,
            hidden:false,
            width : 180,
            store:this.machineStore,
            disabled:true,
            valueField:"id",
            extraFields:['machineid'],
            displayField:"name",
            ctCls:"fieldmargin",
            mode: 'remote',
            scope:this,
            allowBlank:false,
            addCreateOpt: false,
            addNewFn:this.createMC.createDelegate(this)
        }));    
        this.machineIDCombo.on('blur',this.onResourceSelect.createDelegate(this,[false]));
        this.machineIDCombo.on('focus', this.loadMachineStoreOnFocus, this);
          
        this.materialIDCombo = new Wtf.form.ExtFnComboBox({    //Now Kept as text later on to be made as combo field
            fieldLabel:WtfGlobal.getLocaleText("acc.field.workorder.billofmaterialshortform") + "*",
            name: 'materialid',
            hiddenName:'materialid',
            id:"materialID"+this.id,
            store:this.materialStore,
            valueField:"id",
            mode: 'remote', 
            displayField: 'bomcode',
            extraFields: ['name'],
            disabled:true,
            ctCls:"fieldmargin",
            hidden:false,
            width : 180,
            scope:this,
            allowBlank:false,
            isBOMCombo: true
        });    
        this.materialIDCombo.on("change", function(combo, newvalue, oldvalue) {
            var pid = this.productNameCombo.getValue();
            this.projectPanel.disable();
            this.planTask.enable();
            this.taskPlanned = false;
            this.CAPanel.loadBOMDetails(newvalue,pid);
            this.routingStore.load();
            this.routingCodeCombo.setValue("");  // Flushing all the fields dependent on product and BOM
            this.workCentreCombo.setValue("");
            this.machineIDCombo.setValue("");
            this.labourIDCombo.setValue("");
            if(this.isEdit) {  // change Comp. Avail. grids url at edit time on BOM Change
                this.CAPanel.CAStore.proxy.conn.url = "ACCProduct/getAssemblyItems.do";
                this.isBOMChanged = true;        // setting falg to true in change of BOM
            }
            this.CAPanel.CAStore.load({
                params: {
                    productid: this.productNameCombo.getValue(),
                    mrproductquantity:this.quantity.getValue(),
                    bomdetailid:this.materialIDCombo.getValue()
                }
            }, this);
        }, this);
        this.labourIDCombo = new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            fieldLabel:WtfGlobal.getLocaleText("acc.mrp.field.MappedLabourID") + "*",
            forceSelection: true
        }, {
            name: 'labourid',
            hiddenName:'labourid',
            id:"labourID"+this.id,
            store:this.labourStore,
            valueField:"id",
            displayField:"name",
            extraFields:['empcode'],
            disabled:true,
            mode: 'remote',
            hidden:false,
            width : 180,
            scope:this,
            ctCls:"fieldmargin",
            allowBlank:false,
            addCreateOpt: false,
            addNewFn:this.createLB.createDelegate(this)
        }));    
        this.labourIDCombo.on('blur',this.onResourceSelect.createDelegate(this,[true]));
          this.labourIDCombo.on('focus', this.loadLabourStoreOnFocus, this);

        this.routingTypeCombo = new Wtf.form.ComboBox({
            store: this.routingTypeStore,
            fieldLabel: WtfGlobal.getLocaleText('mrp.fieldlabel.workorder.routingmaster.type'), //Routing master Type  
            name: 'routingtype',
            hiddenName: 'routingtype',
            displayField: 'id',
            forceSelection: true,
            valueField: 'name',
            mode: 'local',
            triggerAction: 'all',
            value:0,
            width : 180
        });
        
        this.routingTypeCombo.on('change',this.hanldeRoutingmasterTypeChange,this);
        this.routingCodeCombo = new Wtf.form.ExtFnComboBox({    //Now Kept as text later on to be made as combo field
            fieldLabel:WtfGlobal.getLocaleText("mrp.fields.workorder.routingtemplateroutingcode.title") ,
            name: 'routetemplatename',
             hiddenName:'routecode',
            id:"routingCode"+this.id,
            store:this.routingStore,
            extraFields:[],
            valueField:"id",
            displayField:"routingtname",
            addNewFn:this.addRouting.createDelegate(this),
            hidden:false,
            width : 180,
            allowBlank:true,
            mode: 'remote',
            ctCls:"fieldmargin",
            scope:this,
            disabled:true
//            allowBlank:false
        });
        this.routingCodeCombo.on('change',this.autoPopulateDataComboValues,this);
        
        this.routingStore.on('beforeload',this.handleBeforeLoadOfroutingTemplateCombo,this);
        
           this.workOrderDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText('acc.field.workorder.workorderdate')+'*' , //work order date
            id:"workOrderDate"+this.id,
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'workorderdate',
            allowBlank: false,
            ctCls:"fieldmargin",
            width : 180,
            maxValue : new Date()
        });
        
             
        
        this.workOrderDate.on("change",this.onWorkOrderDateChange,this);
        
//        this.routingCodeCombo.on('select',this.onRountingTemplateSelection,this);
        this.workCentreCombo = new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            forceSelection: true
        }, {
            fieldLabel: WtfGlobal.getLocaleText("mrp.workorder.entry.workcentre")+"*",
            name: 'workcentreid',
            hiddenName:'workcentreid',
            id: "workCentre" + this.id,
            hidden: false,
            width: 180,
            store: this.workCentreStore,
            disabled:true,
            scope: this,
            allowBlank:false,
            mode: 'remote',
            ctCls: "fieldmargin",
            displayField: 'workcenterid',
            valueField: 'id',
            msgTarget: 'side',
            extraFields: ['name'],
            emptyText: "Select Work Centre",
            addCreateOpt: false, 
            addNewFn:this.createWC.createDelegate(this)
        }));
        this.workCentreCombo.on('beforeclearval',function(){
            return !this.isEdit;               
        },this);
        this.workCentreCombo.on("change",function(combo, newval, oldval){
            this.loadmachine(this.workCentreCombo.getValue());
            this.loadlabour(this.workCentreCombo.getValue());
            this.machineIDCombo.enable();
            this.labourIDCombo.enable();
        },this);
        
         this.planTask = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText('acc.workorder.fieldname.button.plantask.title'),
            tooltip: WtfGlobal.getLocaleText('acc.workorder.fieldname.button.plantask.ttip'),
            scope: this,
            disabled:this.isEdit,
              cls: "buttonmargin",
            handler: function () {
                this.PlanTasks();
            }
        });
        //----------------------------------------------------------------------------------------------------
        
        // Panels --------------------------------------------------------------------------------------------
        this.leftWorkOrderPanel = new Wtf.Panel({
            columnWidth:0.48,
            id:"leftWorkOrderPanel"+this.id,
            layout:"form",
            labelWidth:150,
            bodyStyle: {
                margin: "15px 0px 0px 20px"
            },
            border:false,
            items:[this.workOrderNameText,this.sequenceFormatCombobox,this.workOrderIDText,this.customerNameCombo,this.fromLinkCombo,this.linkDocNoText, this.planTask]
        });
        this.middleForm = new Wtf.Panel({
            columnWidth:0.48,
            id:"middleForm"+this.id,
            layout:"form",
            labelWidth:150,
            border:false,
            bodyStyle: {
                margin: "15px 0px 0px 20px"
            },
            items:[this.productNameCombo,this.materialIDCombo,this.quantity,this.workOrderDate,this.deliveryDateText,this.workOrderTypeCombo,this.wareHouseCombo,this.locationMultiSelect]
        });
         this.leftform = new Wtf.Panel({
            columnWidth:0.48,
            id:"leftform"+this.id,
            layout:"form",
            labelWidth:150,
            border:false,
            bodyStyle: {
                margin: "15px 0px 0px 20px"
            },
            items:[this.routingTypeCombo,this.routingCodeCombo,this.workCentreCombo,this.machineIDCombo,this.labourIDCombo,this.workOrderStatusText]
        });
        this.workOrderPanel = new Wtf.form.FormPanel({
            region:'north',
            autoHeight: true,
            border:false,
             autoScroll: true,
            defaults: {
                border: false
            },
            split: true,
            layout:"form",
            bodyStyle: {
                background: "#f1f1f1 none repeat scroll 0 0",
                margin: "10px",
                borderColor: "#99bbe8",
                border: "1px solid #99bbe8"
            },
            disabledClass: "newtripcmbss",
            hideMode: 'display',
            id:"workOrderPanel"+this.id,
            cls: "visibleDisabled",
            items: [{
                    xtype: 'panel',
                    id: this.id +'requiredfieldmessagepanel',
                    hidden: true,
                    cls: 'invalidfieldinfomessage'
                },{
                    layout: 'column',
                    border:false,
                    defaults: {
                        border: false
                    },
                     columnWidth: 0.32,
                    items: [{
                                    layout: 'form',
                                    columnWidth: 0.32,
                                    items: this.leftWorkOrderPanel
                                }, {
                                    layout: 'form',
                                    columnWidth: 0.32,
                                    baseCls:"workorderentrymidform",
                                    items: this.middleForm
                                },{
                                    layout: 'form',
                                    columnWidth: 0.32,
                                     baseCls:"workorderentrymidform",
                                    items: this.leftform
                            }]
                        
                },this.tagsFieldset]
        });
        this.mainPanel = new Wtf.Panel({
            region: 'center',
            border: false,
            scope: this,
            autoScroll: true,
            items: [ this.workOrderPanel,  this.CATabPanel]
        });
        
        //-----------------------------------------------------------------------------------------------------
    },
    createLB: function () {
        callLabourInformation(false, "");
    },
    loadLabourStoreOnFocus: function () {
        this.labourStore.load();
    },
    createWC: function () {
        callWorkcentreWindow(false, "", "", "");
    },
    createMC: function () {
        var obj = {};
        callMRPMachineMaster(obj);
    },
    loadMachineStoreOnFocus: function () {
        this.machineStore.load();
    },
    onProductSelect: function (combo, record, index) {
        /*set quantity of product ginen in sleas order or sales contract*/
        var quantity = record.data.quantity;
        this.productQtyinSalesOrder=quantity;
        this.quantity.setValue(quantity);
    },
    onProductChange: function (combo, newvalue, oldvalue) {
        if (newvalue) {
            this.loadMaterial(this.productNameCombo.getValue());
            this.loadWorkcentre(this.productNameCombo.getValue());
            var index = combo.selectedIndex;
            var rec = combo.store.getAt(index).data;
            var id = newvalue;
            var productType = rec.type;
            var productid = newvalue;
            var pid = rec.pid;
            var pname = rec.productname;
            var arr = [];
            arr.push(id);
            arr.push(productType);
            arr.push(productid);
            arr.push(pid);
            arr.push(pname);
            this.CAPanel.createWestPanel(arr);
            this.workCentreCombo.enable();
            this.materialIDCombo.enable();
//            this.quantity.setValue(1);
            this.materialIDCombo.setValue("");
            this.routingCodeCombo.setValue("");  // Flushing all the fields dependent on product and BOM
            this.workCentreCombo.setValue("");
            this.machineIDCombo.setValue("");
            this.labourIDCombo.setValue("");
            this.CAPanel.CAStore.load({
                params: {
                    productid: this.productNameCombo.getValue(),
                    mrproductquantity: this.quantity.getValue(),
                    bomdetailid: this.materialIDCombo.getValue()
                }
            }, this);
            if(rec.isWarehouseForProduct== true  && rec.isLocationForProduct == true){
                    WtfGlobal.showFormElement(this.wareHouseCombo);
                    WtfGlobal.showFormElement(this.locationMultiSelect);
                }else{
                    WtfGlobal.hideFormElement(this.wareHouseCombo);
                    WtfGlobal.hideFormElement(this.locationMultiSelect);
                }    
        }
    },
    handleLinkDOcOnChange : function(){
        this.productNameCombo.clearValue();// clear product combo value when customer,from linkcombo,link doc is changed
        this.materialIDCombo.reset();// clear product combo value when customer,from linkcombo,link doc is changed
    },
    handleLinkDOcOnSelect : function(){
        this.productNameCombo.clearValue();// clear product combo value when link doc is selected
        this.materialIDCombo.reset();// clear BOM combo value when link doc is selected
        this.productStore.load();// reload productstore based on link doc value when link doc is selected
    },
    setDocumentNumber : function(){
      if(this.isEdit){
          this.linkDocNoText.setValue(this.record.data.linkdocid);
          
      }  
    },
    loadlinkingDocumentStore: function () {
        this.SOSCStore.load({
            params: {
                linkfrom: this.fromLinkCombo.getValue(),
                customerid: this.customerNameCombo.getValue()
            }
        });
    },
    handleOnChangeODformLinkCombo:function(combo,newval,oldval){
        //on change of fromlink clear linking documentno and product values and reload linking document no store
        this.handleLinkTOOnChange();// clear document no combo
        this.handleLinkDOcOnChange();// clear product  combo 
        this.loadlinkingDocumentStore();
        this.configureLinking(newval,true); //When user change link to  combo value then handle link document no combo
    },
    handleCustomerChange: function (combo, newcval, oldval) {
        this.linkDocNoText.clearValue();
        this.fromLinkCombo.clearValue();
        this.SOSCStore.removeAll();
        this.handleLinkDOcOnChange();
    },
    handleLinkTOOnChange:function(){
        this.linkDocNoText.clearValue();
        this.SOSCStore.removeAll();
    },
    hanldeRoutingmasterTypeChange: function () {
        if (this.routingTypeCombo.getValue() != 0) {
            this.routingCodeCombo.enable();
        }else{
            this.routingCodeCombo.disable();
        }
        this.routingCodeCombo.clearValue();
        /*ERP-36537 : ComboBox value do not get removed on clearValue function call. It just clears the display value. 
        * So 'change' event do not get call on selecting the combo box value from empty state(for display) to some value(the same value selected again).
        */
        this.routingCodeCombo.setValue("");
        
        this.clearData();
    },
    clearData:function(){
        this.workCentreCombo.clearValue();
        this.machineIDCombo.clearValue();
        this.labourIDCombo.clearValue();
        
    },
    autoPopulateDataComboValues: function (combo, newvalue, oldvalue) {
        this.clearData();
        var rec = WtfGlobal.searchRecord(combo.store, combo.getValue(), 'id');
        if (rec != undefined) {
            if (rec.data.workcenter != undefined) {
                this.workCentreCombo.setValue(rec.data.workcenter);
            }
            if (rec.data.machineid != undefined) {
                this.machineIDCombo.setValue(rec.data.machineid);
            }
            if (rec.data.labourid != undefined) {
                this.labourIDCombo.setValue(rec.data.labourid);
            }
        }
        /*on Routing template code/template change enable plank tasks button*/
        this.planTask.enable();
        this.taskPlanned = false; 
    },    
    onWorkOrderDateChange:function(context,newvalue,oldvalue){ 
        /*
         * Updated minValue of Date of Delivery to work order date
         * If plan task button is already clicked but user changed workorder date then send sync request. 
         * sync work order date as project date to PM side 
         * isShiftProjectStartDate is true then project date and task start date will shift to workorder date.
         * Otherwise only project date will shift to workorder date. 
         */
       
        var workorderdate = newvalue;
        this.deliveryDateText.minValue = workorderdate;
        var projectId = this.projectId;
        if(this.taskPlanned){
            Wtf.Ajax.requestEx({
                url:"ACCWorkOrder/syncWorkOrderDateToPM.do",
                params:{
                    projectId:projectId,
                    workorderdate:workorderdate.format('Y-m-d H:i:s'),
                    isShiftProjectStartDate:true                    
                }
            }, this, this.genSuccessResponseForSyncDateToPM, this.genFailureResponseForSyncDateToPM)
            
        }
        
    },
    genSuccessResponseForSyncDateToPM: function(response){    
     if (response.success) {
            document.getElementById("woprojectplan-" + this.projectId).src = Wtf.pmURL + "editableprojview.jsp?id=" + this.projectId;
        }    
    },
    genFailureResponseForSyncDateToPM: function(response){
    
    
    },
    onResourceSelect: function (isLabour) {
        /*If plan task button is already clicked but user changed labour or machine later on then send sync request */
        var projectid = this.projectId;
        if (this.taskPlanned) {
            if (isLabour) {
                this.resourceId = this.labourIDCombo.getValue();
            } else {
                this.resourceId = this.machineIDCombo.getValue();
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
    genSuccessResponseForSyncToPM: function (response) {
        if (response.success) {
            document.getElementById("woprojectplan-" + this.projectId).src = Wtf.pmURL + "editableprojview.jsp?id=" + this.projectId;
        }
    },
    genFailureResponseForSyncToPM: function (response) {

    },
    handleBeforeLoadOfroutingTemplateCombo : function(s,o){
         s.baseParams.bomid=this.materialIDCombo.getValue();
         s.baseParams.routingmastertype=this.routingTypeCombo.getValue();
    },
    createCustomFields: function () {
        this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId: "workOrderPanel" + this.id,
            autoHeight: true,
            bodyStyle: 'margin: 0 10px;',
            moduleid: Wtf.MRP_Work_Order_ModuleID,
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
    loadWorkcentre: function(val) {
      this.workCentreStore.load({
        params:{
            productid : val
        }  
      });
    },
    loadmachine: function(val) {
      this.machineStore.load({
        params:{
            workcenterid : val
        }  
      });
    },
    loadlabour: function(val) {
      this.labourStore.load({
        params:{
            workcenterid : val
        }  
      });
    },
    setNextNumber: function (store) {
        if (this.sequenceFormatStore.getCount() > 0) {
            if (this.isEdit) {
                var index = this.sequenceFormatStore.find('id', this.record.data.seqformat);
                if (index != -1) {
                    this.sequenceFormatCombobox.setValue(this.record.data.seqformat);
                    this.sequenceFormatCombobox.disable();
                    this.workOrderIDText.disable();
                } else {
                    this.sequenceFormatCombobox.setValue("NA");
                    this.sequenceFormatCombobox.disable();
                    this.workOrderIDText.enable();
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
                    this.workOrderIDText.setValue("");
                    this.workOrderIDText.disable();
                }
            }
        }
    },
    getNextSequenceNumber: function (combo) {
        if (combo.getValue() == "NA") {
            this.workOrderIDText.reset();
            this.workOrderIDText.enable();
        } else {
            Wtf.Ajax.requestEx({
                url: "ACCCompanyPref/getNextAutoNumber.do",
                params: {
                    from: Wtf.MRP_Work_Order_ModuleID,
                    sequenceformat: combo.getValue()
                }
            }, this, function (resp) {
                if (resp.data == "NA") {
                    this.workOrderIDText.reset();
                    this.workOrderIDText.enable();
                } else {
                    this.workOrderIDText.setValue(resp.data);
                    this.workOrderIDText.disable();
                }
            });
        }
    },
    configureLinking:function(val,onSelect) {
        if (onSelect) {
            if (val == this.LinkToType.NONE) {
                this.linkDocNoText.disable();
                this.linkDocNoText.setValue("");
                this.linkDocNoText.allowBlank=true;
            }else {
                this.linkDocNoText.enable();
                this.linkDocNoText.allowBlank=false;
            }
        } else {
            if(!this.isEdit) {
                this.fromLinkCombo.setValue(this.LinkToType.NONE);
                this.linkDocNoText.disable();
            }
        }
    },
    createCAPanel: function(){
        this.CAPanel = new Wtf.account.MRPAssemblyProductDetails({
           isCAComponent:true,
           woScope: this,
           autoScroll:true,
           title:"Component Availability",
           scope:this
        });
        this.CATabPanel = new Wtf.TabPanel({
            border:false,
//            height:400,
//            layout:"fit",
            activeTab: 1,
            scope:this,
            height:550,
            autoScroll: true,
            title:"Component Availability",
            items:[this.projectPanel,this.CAPanel]
        });
        this.CAPanel.on("render",function() {
            this.CATabPanel.setActiveTab(0);
        },this);
        this.CAPanel.on("activate",function(){
            this.CAPanel.westBOMTab.doLayout();
            
            /*Column headers and data were mismatching in  component availability grid. To solve this issue I have refreshed component availability grid by calling refreshCAGrid */
              this.CAPanel.refreshCAGrid(false);
            this.CAPanel.doLayout();
            this.CATabPanel.doLayout();
        },this);
    },
    getCAJSON:function(){
        return this.CAPanel.getCAJSON();  
    },
    addProjectPanel: function(project) {
        this.tempProjectid = project;
        this.projectPanel = Wtf.getCmp("woprojectplan-" + project);
        if (!this.projectPanel) {
            this.projectPanel = new Wtf.Panel({
                title: "Work Order - Project Plan",  // done to disable only project panle tab
                closable: false,
                items:[{
                        xtype:"panel",
                        autoEl: {
                            tag: "iframe",
                            id: "woprojectplan-" + project,
                            height: "100%",
                            src: Wtf.pmURL + "editableprojview.jsp?id=" + project
                        }
//                        disabled:true
                }],
                disabled:!this.isEdit,
                layout: 'fit'
            });
        }
        this.projectPanel.on("afterrender",function(){
            this.projectPanel.doLayout();
            Wtf.getCmp("subtabpanelcomprojectTabs_" +this.tempProjectid).doLayout();
        },this);
    },
    PlanTasks: function () {
        var isValidForm = this.workOrderPanel.getForm().isValid();
        var isvalidCustomData = this.tagsFieldset.checkMendatoryCombo();
        if (isValidForm && isvalidCustomData) {
            this.onPlanTasksButtonClickSyncDataToPM();
        }
    },
    onPlanTasksButtonClickSyncDataToPM: function () {
        //this.projectId - It refers to projectid which is generated when clicks on workorder form.
        var routingTemplateRec = WtfGlobal.searchRecord(this.routingCodeCombo.store, this.routingCodeCombo.getValue(), 'id');
        if (routingTemplateRec != undefined) {
            var masterProjectID = routingTemplateRec.data.projectId;// Projectid generated in pm while creating Routing template. 
        }
        var labourids = this.labourIDCombo.getValue();
        var machineids = this.machineIDCombo.getValue();
        var productid = this.productNameCombo.getValue();
        var bomid = this.materialIDCombo.getValue();
        var salesorderid="";
        var salescontractid="";
        var linkFrom = this.fromLinkCombo.getValue();
        if (linkFrom == this.LinkToType.SALESORDER) {
            /*
             When  user select salesorder to link
             */
            salesorderid = this.linkDocNoText.getValue();
            salescontractid = "";
        } else if(linkFrom == this.LinkToType.SALESCONTRACT){
            /*When  user select sales contract to link*/
            salescontractid = this.linkDocNoText.getValue();
            salesorderid = "";
        }else{
            /*When  there is no linking with salesorder and salescontract*/
            salescontractid = "";
            salesorderid = "";
        }
        
        var workorderdate = this.workOrderDate.getValue(); // Work Order Date 
        /**
         * sync work order date as project date to PM side 
         * isShiftProjectStartDate is true then project date and task start date will shift to workorder date.
         * Otherwise only project date will shift to workorder date. 
         */

        Wtf.Ajax.requestEx({
            url: "ACCWorkOrder/syncDataToPM.do",
            params: {
                masterprojectid: masterProjectID, //
                projectId: this.projectId,
                labourids: labourids,
                machineids: machineids,
                bomid:bomid,
                productid:productid,
                isForCompAvailablity:true, //this flag is used to get subproducts bom structure
                salesorderid:salesorderid,
                workorderdate:workorderdate.format('Y-m-d H:i:s'),
                isShiftProjectStartDate:true
            }
        }, this, this.genSuccessResponse, this.genFailureResponse);
    },
    genSuccessResponse: function(response) {
        if (response.success) {
//            this.planTask.disable();
            this.projectPanel.enable();
            this.planTask.disable();
            this.taskPlanned = true;
            
            document.getElementById("woprojectplan-" + this.projectId).src=Wtf.pmURL + "editableprojview.jsp?id=" +this.projectId;
        }
    },
    genFailureResponse: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.routing.failure");  //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },

    addRouting:function() {
//        callRoutingTemplateMaster();
        createProjectforMRP(Wtf.Project_TemplateId.ROUTINGMASTER);
    },
    addWOType : function() {
        addMasterItemWindow('49');
    },
    addWOStatus: function() {
        addMasterItemWindow('50');
    },
    addProduct: function() {
        callProductDetails();
    },
    addCustomer: function() {
        callCustomerDetails();
    },
    validateSelectionForWOStatus: function(combo,record,index) {  // validation function for WO status
        if(record!=null && record!=undefined) {
            if ((record.data.defaultMasterItem === Wtf.WODefaultStatus.inprocess) && !this.isEdit) {
                return false;
            }
        }
        return true;
    },
    enableDisableFields:function() {
        if (this.inProcess) {
            this.workOrderNameText.disable();
            this.workOrderIDText.disable();
            this.customerNameCombo.disable();
            this.fromLinkCombo.disable();
            this.linkDocNoText.disable();
            this.productNameCombo.disable();
            this.quantity.disable();
            this.deliveryDateText.disable();
            this.workOrderStatusText.disable();
            this.workOrderTypeCombo.disable();
            this.routingCodeCombo.disable();
            this.workOrderDate.disable();
            this.workCentreCombo.disable();
            this.machineIDCombo.disable();
            this.labourIDCombo.disable();
            this.routingTypeCombo.disable();
            this.wareHouseCombo.disable();
            this.locationMultiSelect.disable();   
        }
                }
});
