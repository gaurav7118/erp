
Wtf.account.PackingDeliveryorder=function(config){	
    this.isEdit=config.isEdit;
    this.label=config.label;
    this.copyInv = config.copyInv;
    this.handleEmptyText=false; //To handle empty text after clicking on save button
    this.heplmodeid = config.heplmodeid;
    this.record=config.record;
    this.businessPerson=(config.isCustomer?'Customer':'Vendor');
    this.modeName = config.modeName;
    this.moduleid=config.moduleid;
    this.readOnly=config.readOnly;
    this.customerid="";
    (this.businessPerson == "Customer")? Wtf.DOStatusStore.load() : Wtf.GROStatusStore.load();
    Wtf.apply(this, config);
    Wtf.apply(this,{
        bbar:[{
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
            toolTip:WtfGlobal.getLocaleText("acc.rem.175"),
            id:"save"+config.heplmodeid+this.id,
            hidden:this.readOnly,
            scope:this,
            handler:function(){
                this.mailFlag = true;
                this.save();
            },
            iconCls :'pwnd save'
        }]
    });
    Wtf.account.PackingDeliveryorder.superclass.constructor.call(this,config);
    this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.PackingDeliveryorder,Wtf.account.ClosablePanel,{
    autoScroll: true,
    bodyStyle: {
        background:"#DFE8F6 none repeat scroll 0 0"
    },
    border:'false',
    custdatechange:false,
    closable : true,
    cash:false,
    
    loadRecord:function(){
        if(this.record!=null&&!this.dataLoaded){
            var data=this.record[0].data;
            this.NorthForm.getForm().loadRecord(this.record);
        
            this.Name.setValue(data.personname);
            this.customerid=data.personid;
            this.dataLoaded=true;
            if(this.Grid){
                this.Grid.affecteduser=data.personid;
                this.Grid.billDate=data.date;
            }          
        }
    },
    
    onRender:function(config){                
        this.add(this.NorthForm,this.Grid);                       
        Wtf.account.PackingDeliveryorder.superclass.onRender.call(this, config);
        this.initForClose();   
        if( this.isEdit ){
            this.isClosable=false          // Set Closable flag for edit and copy case
        }
        this.hideFormFields();
        
    },
    //////////////////////////////////////////////////////////////
    hideFormFields: function () {
        this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.packform);

    }
    ,
    /**
     * 
     * @param {type} array
     * @returns {undefined}
     * @Desc : function to hide formfields according to company preferences
     */
     hideTransactionFormFields:function(array){
        if(array){
            var isHiddenDiscount = false;
            var isHiddenDiscountType = false;
            for(var i=0;i<array.length;i++){
                var fieldArray = array[i];
                if(Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id)){
                    if(fieldArray.fieldId=="ShowOnlyOneTime" ){
//                        if(fieldArray.fieldId=="ShowOnlyOneTime" && ((this.isEdit !=undefined ?this.isEdit:false) || (this.copyInv !=undefined ?this.copyInv:false) || (this.isCopyFromTemplate !=undefined ?this.isCopyFromTemplate:false) || (this.isTemplate !=undefined ?this.isTemplate:false))){
                        continue;
                    }
                    if(fieldArray.isHidden){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hideLabel = fieldArray.isHidden;
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hidden = fieldArray.isHidden;
                    }

                    if(fieldArray.isReadOnly){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).disabled = fieldArray.isReadOnly;
                    }
                    if(fieldArray.isUserManadatoryField && Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel != undefined){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).allowBlank = !fieldArray.isUserManadatoryField;
                        var fieldLabel="";
                        if(fieldArray.fieldLabelText!="" && fieldArray.fieldLabelText!=null && fieldArray.fieldLabelText!=undefined){
                            fieldLabel= fieldArray.fieldLabelText+" *";
                        }else{
                            fieldLabel=(Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel) + " *";
                        }
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel = fieldLabel;
                    }else{
                        if( fieldArray.fieldLabelText!=null && fieldArray.fieldLabelText!=undefined && fieldArray.fieldLabelText!=""){
                            if (fieldArray.isManadatoryField && fieldArray.isFormField)
                                Wtf.getCmp(fieldArray.fieldId + this.heplmodeid + this.id).fieldLabel = fieldArray.fieldLabelText + "*";
                            else
                                Wtf.getCmp(fieldArray.fieldId + this.heplmodeid + this.id).fieldLabel = fieldArray.fieldLabelText;
                        }
                    }
                }
            }
        }
    },
    ///////////////////////////////////////////////////////////
    
    
    initComponent:function(config){
        Wtf.account.PackingDeliveryorder.superclass.initComponent.call(this,config);

        this.Name=new Wtf.form.TextField({
            fieldLabel:this.isCustomer?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven") , //this.businessPerson+"*",
            hiddenName:this.businessPerson.toLowerCase(),
            name: 'customername',
            id:"customer"+this.heplmodeid+this.id,
            anchor:'80%',
            maxLength:50,
            scope:this,
            allowBlank:this.checkin
        });

        //ERM-999 allowing bulk packing of DO products through this check
        this.bulkPackingCheck = new Wtf.form.Checkbox({
            name:'isaddressfromvendormaster',
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.bulkpackingmsg")+"'>"+WtfGlobal.getLocaleText("acc.field.bulkpacking")+"</span>",
            hideLabel:Wtf.account.companyAccountPref.upsIntegration,
            id:"bulkpacking"+this.heplmodeid+this.id,
            hidden:Wtf.account.companyAccountPref.upsIntegration,
            width:100
        });
        this.DOStatusCombo =  new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Status*"), //Status
            name:"statuscombo",     
            id:'statuscomboId'+this.heplmodeid+this.id,
            store:(this.businessPerson == "Customer")? Wtf.DOStatusStore : Wtf.GROStatusStore,
            anchor:"80%",
            allowBlank:false,
            valueField:'id',
            displayField:'name',
            mode: 'local',
            triggerAction:'all',
            forceSelection:true
        });
        this.setDefaultStatus();
        this.bulkPackageTypeRec = new Wtf.data.Record.create([
        {
            name: 'packageid'
        },

        {
            name: 'packagename'
        },

        {
            name: 'measurement'
        },

        {
            name: 'packageweight'
        }
        ]);
        this.bulkPackagetypeStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.bulkPackageTypeRec),
            url : "ACCMaster/getPackages.do",
            baseParams:{
                mode:100
            }
        });
        this.bulkPackagetypeStore.load();
        this.bulkPackagetypeStore.on("load",function(combo){
            this.packageDetailsGrid.getView().refresh();
        },this);
        this.packageTypeCombo =  new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("erp.Package"), //ERM-999 package selection combo for Bulk Packing
            hideLabel:Wtf.account.companyAccountPref.upsIntegration,
            name:"statuscombo",     
            id:'packagetype'+this.heplmodeid+this.id,
            store:this.bulkPackagetypeStore,
            anchor:"80%",
            disabled:true,
            hidden:Wtf.account.companyAccountPref.upsIntegration,
            allowBlank:!this.bulkPackingCheck,
            valueField:'packageid',
            displayField:'packagename',
            mode: 'local',
            triggerAction:'all',
            forceSelection:true
        });
        this.PackageNumber=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("erp.PackageNo"), //Packing Number for Bulk Packing
            name: 'packingnumber',
            disabled:true,
            hidden:Wtf.account.companyAccountPref.upsIntegration,
            hideLabel:Wtf.account.companyAccountPref.upsIntegration,
            id:"packageNumber"+this.heplmodeid+this.id,
            anchor:'80%',
            maxLength:50,
            scope:this,
            allowBlank:!this.bulkPackingCheck
        });
        
        this.PackageNumber.on('blur', function (obj) {
            if (this.PackageNumber.getValue() == '' && this.packageTypeCombo.getValue() !== '') {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.field.bulkpackingalert")], 0);
            } else if (this.PackageNumber.getValue() !== '' && this.packageTypeCombo.getValue() !== '') {
                this.createBulkPacking();
            }
        }, this);
        this.bulkPackingCheck.on('check', function (checkbox, ischecked) {
            if (ischecked) {
                if (this.packageTypeCombo.getValue !== "" && this.PackageNumber.getValue() !== "") {
                    this.createBulkPacking();
                } else {
                    this.packageTypeCombo.setDisabled(false);
                    this.PackageNumber.setDisabled(false);
                }
            } else {
                this.packageTypeCombo.reset();
                this.packageTypeCombo.setDisabled(true);
                this.PackageNumber.reset();
                this.PackageNumber.setDisabled(true);
            }
        }, this);
        this.packageTypeCombo.on('select', function (combo, rec, index) {
            if (this.PackageNumber.getValue() == "") {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.bulkpackingalert")], 2);
            }
            else {
                this.createBulkPacking();
            }
        }, this);
        Wtf.DOStatusStore.on('load',this.setDefaultStatus,this);      
        this.DOStatusCombo.addNewFn=this.addDOStatus.createDelegate(this);   
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
                totalProperty:'count',
                root: "data"
            },this.sequenceFormatStoreRec),
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:'autopackingdo'
            }
        });
        this.sequenceFormatStore.load();
        this.sequenceFormatCombobox = new Wtf.form.ComboBox({
            triggerAction: 'all',
            mode: 'local',
            id: 'sequenceFormatCombobox' + this.heplmodeid + this.id,
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.Sequenceformat.tip") + "'>" + WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat") + "</span>",
            valueField: 'id',
            displayField: 'value',
            store: this.sequenceFormatStore,
            anchor:'80%',
            typeAhead: true,
            forceSelection: true,
            name: 'sequenceformat',
            hiddenName: 'sequenceformat',
            allowBlank: false,
            listeners: {
                'select': {
                    fn: this.getNextSequenceNumber,
                    scope: this
                }
            }

        });
        
        this.sequenceFormatStore.on('load',this.setNextNumber,this);
        
        this.Number=new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("erp.packingnumber"), //Packing Number
            name: 'packingnumber',
            disabled:(this.isEdit&&!this.copyInv?true:false),
            id:"packingNumber"+this.heplmodeid+this.id,
            anchor:'80%',
            maxLength:50,
            scope:this,
            allowBlank:true
        });
    
        this.Memo=new Wtf.form.TextArea({
            fieldLabel:Wtf.account.companyAccountPref.descriptionType,  //'Memo',
            name: 'memo',
            id:"memo"+this.heplmodeid+this.id,
            height:40,
            anchor:'80%',
            maxLength:2048
        });
        
        this.PackingListRec = Wtf.data.Record.create ([
        {
            name:'billid'
        },

        {
            name:'packingnumber'
        },

        {
            name:'customerid'
        },
        
        {
            name:'customername'
        },
        
        {
            name:'statuscombo'
        },

        {
            name:'memo'
        },

        {
            name:'packingDate',
            type:'date'
        }
        ]);
        this.PackinListStoreUrl = "";
        if(this.businessPerson=="Customer"){
            this.PackinListStoreUrl = "ACCInvoiceCMN/getPackingDoList.do";
        }
        this.PackingListStore = new Wtf.data.Store({
            url:this.PackinListStoreUrl,
            baseParams:{
                mode:42,
                closeflag:true,
                doflag : true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.PackingListRec)
        });                              
              
        this.billDate= new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("erp.packingdate"),
            id:"packingDate"+this.heplmodeid+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'packingdate',
            anchor:'80%',
            allowBlank:false
        });
    
        var itemArr={};
        itemArr = [this.Name,this.sequenceFormatCombobox,this.Number,this.billDate];
        
        this.NorthForm=new Wtf.form.FormPanel({
            region:'north',
            autoHeight:true,
            autoWidth:true,
            id:"northForm"+this.id,
            disabled:this.readOnly,
            disabledClass:"newtripcmbss",
            border:false,
            items:[{
                layout:'form',
                baseCls:'northFormFormat',
                labelWidth:155,
                cls:"visibleDisabled",
                items:[{
                    layout:'column',
                    border:false,
                    defaults:{
                        border:false
                    },
                    items:[{
                        layout:'form',
                        columnWidth:0.50,
                        border:false,
                        items:itemArr
                    },{
                        layout:'form',
                        columnWidth:0.50,
                        border:false,
                        items:[this.DOStatusCombo,this.Memo,this.bulkPackingCheck,this.PackageNumber,this.packageTypeCombo] // ERM-999 bulk packing feature for pick pack ship do
                    }]
                }]
            }]
        });
        this.addGrid();
        this.NorthForm.doLayout();
        this.billDate.on('change',this.onDateChange,this);
        this.NorthForm.doLayout();
        this.loadEditableGrid();
    },
    
    addDOStatus: function(){
        (this.businessPerson == "Customer")? addMasterItemWindow('10') : addMasterItemWindow('11');
    },
        setDefaultStatus: function(){
       var value="Packed";
            var masterstatus = WtfGlobal.searchRecord(Wtf.DOStatusStore,value, 'name');
            if(masterstatus!=undefined && masterstatus!=null){
                var defaultstatus=masterstatus.data.id;
                this.DOStatusCombo.setValue(defaultstatus);
             }
             if(Wtf.account.companyAccountPref.pickpackship && this.moduleid == Wtf.Acc_Packing_ModuleId){
                this.DOStatusCombo.disable();
             }            
    },
    createBulkPacking: function () { //ERM-999 function for bulk assigning package no and type to all products of DO
        var count = this.Grid.store.getCount();
        for (var i = 0; i < count; i++) {
            var prorec = this.Grid.getStore().getAt(i);
            var packagenum = this.PackageNumber.getValue();
            var totalpackageqty = 1 * prorec.data.shipquantity;
            var packageindex = this.packageTypeCombo.selectedIndex;
            var packageobj = this.packageTypeCombo.store.getAt(packageindex);
            var grossweight = 0;
            var packageweight = (packageobj.data.packageweight * 1);
            var productweight = ((prorec.data.baseuomrate != undefined || prorec.data.baseuomrate != '') ? prorec.data.baseuomrate * (prorec.data.shipquantity * 1) * prorec.data.productweight : prorec.data.productweight);
            productweight = productweight != productweight ? "" : productweight.toFixed(1);
            grossweight = parseFloat(packageweight) + parseFloat(productweight);
            grossweight = grossweight != grossweight ? "" : grossweight.toFixed(1);

            var rec = {//creating rec for bulk packing and assigning individually productwise 
                id: packageobj.data.packageid != undefined ? packageobj.data.packageid : "",
                packages: packageobj.data.packageid != undefined ? packageobj.data.packageid : "",
                productname: prorec.data.productname != undefined ? prorec.data.productname : "",
                productid: prorec.data.productid != undefined ? prorec.data.productid : "",
                packageno: packagenum != undefined ? packagenum : "",
                packagemeasurement: packageobj.data.measurement != undefined ? packageobj.data.measurement : "",
                baseuomquantity: prorec.data.baseuomquantity != undefined ? prorec.data.baseuomquantity : "",
                baseuomrate: prorec.data.baseuomrate != undefined ? prorec.data.baseuomrate : "",
                description: prorec.data.description != undefined ? prorec.data.description : "",
                packagequantity: 1, //number of packages will be 1 
                packageperquantity: prorec.data.shipquantity != undefined ? prorec.data.shipquantity : "", //total qty into single package
                grossweight: grossweight, //gross weight and net weigh will be processed
                netweight: productweight,
                packageweight: packageobj.data.packageweight != undefined ? packageobj.data.packageweight : "",
                productweight: prorec.data.productweight != undefined ? prorec.data.productweight : "",
                totalpackagequantity: totalpackageqty != undefined ? totalpackageqty : "",
                billid: prorec.data.billid != undefined ? prorec.data.billid : ""
            };
            prorec.data.packingdetails = "[" + JSON.stringify(rec) + "]";
            this.callPackageDetailsWindow(i, true); //true flag is sent when call is from bulk packing feature
        }
    },
    onDateChange:function(a,val,oldval){
        this.val=val;
        this.oldval=oldval; 
    },
    hideLoading:function(){
        Wtf.MessageBox.hide();
    },
    
    addGrid:function(){
        this.Grid=new Wtf.account.PackingDeliveryorderGrid({
            region:'north',
            autoWidth:true,
            height: 400,
            cls:'gridFormat',
            layout:'fit',
            viewConfig:{
                forceFit:true
            },
            isCustomer:this.isCustomer,
            editTransaction:this.isEdit,
            disabled:this.readOnly,
            disabledClass:"newtripcmbss",
            id:this.id+"billingproductdetailsgrid",
            isOrder:this.isOrder,
            isEdit:this.isEdit,
            copyTrans:this.copyInv, 
            loadMask : true,
            heplmodeid:this.heplmodeid
        });
        this.Grid.on("productdeleted", this.removeTransStore, this);
        this.Grid.on("datachanged", function(){
            this.isClosable=false          // Set Closable flag on grid data change
        },this);
        this.NorthForm.on('render',this.setDate,this);        
        this.Grid.getStore().on('load',function(store){            
            this.Grid.addBlank(store);
            this.updateFormCurrency();
        }.createDelegate(this),this);
        this.Grid.on('cellclick',this.onCellClick, this);
        this.Grid.on('rowclick',this.handleRowClick,this);
        this.Grid.getStore().on('update',function(store,record,opr){            
                                           
            },this);
    },
    
    onCellClick:function(g,i,j,e){      
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var dataindex=g.getColumnModel().getDataIndex(j);
        if(dataindex == "packingdodetails"){
            this.callDoDetailsWindow(i);
        }else if(dataindex=='packagedetails'){
            this.callPackageDetailsWindow(i,false); //ERM-999 passing second flag as hardcoded false for bulkpacking 
        }
    },
    
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".doDetails-gridrow")){
            this.callDoDetailsWindow(rowindex);
        }
        if(e.getTarget(".packingDetails-gridrow")){
             this.callPackageDetailsWindow(rowindex,false); //ERM-999 passing second flag as hardcoded false for bulkpacking
        }        
    },   
    getNextSequenceNumber:function(a,val){
       if(!(a.getValue()=="NA")){
         this.Number.allowBlank = true;  
         WtfGlobal.hideFormElement(this.Number);
       } else {
           WtfGlobal.showFormElement(this.Number);
           this.Number.allowBlank = false;
           this.Number.reset();
       }
    },
    setNextNumber: function (config) {
        if (this.sequenceFormatStore.getCount() > 1) {
            var count = this.sequenceFormatStore.getCount();
            for (var i = 0; i < count; i++) {
                var seqRec = this.sequenceFormatStore.getAt(i)
                if (seqRec.json.isdefaultformat == "Yes") {
                    this.sequenceFormatCombobox.setValue(seqRec.data.id);
                    WtfGlobal.hideFormElement(this.Number);
                    break;
                }
            }
        }else{
            var seqRec = this.sequenceFormatStore.getAt(0)
            this.sequenceFormatCombobox.setValue(seqRec.data.id);
            if (seqRec.data.id == "NA") {
                this.Number.allowBlank = false;
            }
            else {
                this.Number.allowBlank = true;
            }
        }
    },
    enableNumber:function(c,rec){      
        this.PO.clearValue();
        this.DOStatusCombo.clearValue();
        this.Memo.setValue("");
        this.Grid.getStore().removeAll();            
    },
    
    loadEditableGrid:function(){
        this.loadRecord();
        this.subGridStoreUrl = "";
        this.subGridStoreUrl =  "ACCInvoiceCMN/getDeliveryOrderRows.do" ;
        this.billids="";
        for(var i=0;i<this.record.length;i++){
            this.billids=this.billids+this.record[i].data.billid+",";
        }
        this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;
        this.Grid.getStore().on("load", function(){
            this.loadRecord();
        }, this);
        this.Grid.getStore().load({
            params:{
                bills:this.billids,
                packingDOList:true
            }
        });
    },

    getDates:function(start){
        var d=new Date();
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;

        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },

    setDate:function(){
        var height = 0;
        if(this.isOrder)
            height=140;
        if(!this.isEdit){            
            this.billDate.setValue(new Date());//(new Date());            
        }
    },        
    
    addPerson:function(isEdit,rec,winid,isCustomer){
        callBusinessContactWindow(isEdit, rec, winid, isCustomer);
        var tabid=isCustomer?'contactDetailCustomerTab':'contactDetailVendorTab';
        Wtf.getCmp(tabid).on('update', function(){
            this.isCustomer?Wtf.customerAccStore.reload():Wtf.vendorAccStore.reload();
        }, this);
    },
    
    changeTemplateSymbol:function(){        
        if(this.currencyStore.getCount()==0){
            callCurrencyExchangeWindow();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+" <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
            this.billDate.setValue("");
        } else
            this.updateFormCurrency();
    },

    initForClose:function(){
        this.cascade(function(comp){
            if(comp.isXType('field')){
                comp.on('change', function(){
                    this.isClosable=false;
                },this);
            }
        },this);
    },
    
    save:function(){
        this.Number.setValue(this.Number.getValue());
        if(this.NorthForm.getForm().isValid()){
            this.Number.setValue(this.Number.getValue());
            if (Wtf.account.companyAccountPref.pickpackship && this.DOStatusCombo.getValue() == "") {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.deliveryorder.packedisnotset")]);
                return;
            }
            for(var i=0;i<this.Grid.getStore().getCount();i++){
                var quantityindo=this.Grid.getStore().getAt(i).data['dquantity'];
                var shipquantity=this.Grid.getStore().getAt(i).data['shipquantity'];
//                if(shipquantity == '' || shipquantity == undefined || shipquantity<=0){
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.YouhavenotsettheshipQuantity")+" "+this.Grid.getStore().getAt(i).data['productname']], 2);
//                } 
                if(quantityindo<shipquantity){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.YouhaveentershipQuantityforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+WtfGlobal.getLocaleText("erp.greaterthanthequantitiesavailableinDO")], 2);
                    return;
                }
            }
            var count=this.Grid.getStore().getCount();
            if(count<1){
                WtfComMsgBox(33, 2);
                return;
            }
            var rec=this.NorthForm.getForm().getValues();
            this.ajxurl = "";
            if(this.businessPerson=="Customer") {
                this.ajxurl = "ACCInvoice/savePacking.do";            
            } 
            var detail = this.Grid.getProductDetails();
            if(detail == undefined || detail == "[]"){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg12")],2);   //"Product(s) details are not valid."
                return;
            }
            var prodLength=this.Grid.getStore().data.items.length;
            for(var i=0;i<prodLength;i++){
                var packingRowRec = this.Grid.getStore().getAt(i);
                var doquantity=packingRowRec.data['dquantity'];
                var shipquantity = packingRowRec.data['shipquantity'];
                if(doquantity<shipquantity){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.YouhaveentershipQuantityforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getlocaleText("erp.greaterthanthequantitiesavailableinDO")], 2); 
                    return;
                }
//                if (Wtf.account.companyAccountPref.upsIntegration) { //ERM-999 removing do details mandatory column requirement
                var detailArr = eval('(' + detail + ')');
                var dodetails = packingRowRec.data['packingdodetails'];
                if (dodetails == undefined || dodetails == "") {
                    dodetails = [];
                    var dodetailObj = {
                        id: "",
                        productname: packingRowRec.data['productname'],
                        dono: packingRowRec.data['billno'],
                        billid: packingRowRec.data['billid'],
                        productid: packingRowRec.data['productid'],
                        quantityindo: doquantity,
                        duequantity: detailArr[i].packedqty,
                        shipquantity: shipquantity,
                        modified: false
                    };
                    dodetails.push(dodetailObj);
                    detailArr[i].packingdodetails = dodetails;
                    detail = JSON.stringify(detailArr);
                }
//                } else { ERM-999 removing DO details mandatory case hence commenting this section
//                    var dodetails=packingRowRec.data['packingdodetails'];
//                    var doDetailsLength = 0;
//                    if(dodetails == undefined || dodetails == ""){
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("erp.PleaseFilltheShipQuantitiesasperDOinDODetails")],2);   //Please Fill the Ship Quantities as per DO in DO Details Link
//                        return;
//                    }
//                    if(dodetails && dodetails !=''){
//                        this.doDetailsArr = eval('(' + dodetails + ')');
//                    }
//                    if(this.doDetailsArr.length>0){
//                        doDetailsLength = this.doDetailsArr.length;
//                    }
//                    var shipquantity=0;
//                    for(var j=0;j<doDetailsLength;j++){
//                        if(parseInt(this.doDetailsArr[j].quantityindo)<parseInt(this.doDetailsArr[j].shipquantity)){
//                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.YouhaveentershipQuantityforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getlocaleText("erp.greaterthanthequantitiesavailableinDO")], 2); 
//                            return;
//                        }
//                        if(this.doDetailsArr[j].shipquantity == undefined || this.doDetailsArr[j].shipquantity==""){
//                            this.doDetailsArr[j].shipquantity=0;
//                        }
//                        shipquantity=shipquantity+parseInt(this.doDetailsArr[j].shipquantity);   
//                    }
//    //                if(this.Grid.getStore().getAt(i).data['shipquantity']!=shipquantity){
//    //                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.YouhaveentershipQuantityforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("erp.isnotsameasthatofavailablequantityinDOs")], 2); 
//    //                    return;
//    //                }
                if (!Wtf.account.companyAccountPref.upsIntegration) {
                    var packingdetails = packingRowRec.data['packingdetails'];
                    if (packingdetails == undefined || packingdetails == "") {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("erp.PleaseFillthePackingDetailsbyclickingonPackingDetails")], 2);   //Please Fill the Ship Quantities as per DO in DO Details Link
                        return;
                    }
                    var packingDetailsLength = 0;
                    if (packingdetails && packingdetails != '') {
                        this.packingDetailsArr = eval('(' + packingdetails + ')');
                    }
                    if (this.packingDetailsArr.length > 0) {
                        packingDetailsLength = this.packingDetailsArr.length;
                    }
                    var packedshipquantity = 0;
                    for (var k = 0; k < packingDetailsLength; k++) {
                            packedshipquantity = packedshipquantity + (parseInt(this.packingDetailsArr[k].packagequantity) * parseInt(this.packingDetailsArr[k].packageperquantity));
                        }                        
                    if (this.Grid.getStore().getAt(i).data['shipquantity'] != packedshipquantity)
                    {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("erp.YouhavesettheshipQuantityforProduct") + " " + this.Grid.getStore().getAt(i).data['productname'] + " " + WtfGlobal.getLocaleText("erp.isnotsameasthatofavailablequantityinDOs")], 2);
                        return;
                    }
                }
            }
            this.showConfirmAndSave(rec,detail);
        }else{
            WtfComMsgBox(2, 2);
        } 
    },   
       
    showConfirmAndSave: function(rec,detail){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.invoice.msg7"),function(btn){
            if(btn!="yes") {
                return;
            }
            rec.detail=detail;
            this.msg= WtfComMsgBox(27,4,true);
            rec.number=this.Number.getValue();
            rec.customer=this.Name.getValue();
            rec.statuscombo=this.DOStatusCombo.getValue();
            if(this.Grid != undefined && this.Grid.deleteStore!=undefined && this.Grid.deleteStore.data.length>0){  //for geting store to delete.
                rec.deletedData = this.getJSONArray(this.Grid.deleteStore,false,0);
            }
            rec.packingDate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
            rec.billid=this.copyInv?"":this.billid;                
            rec.isEdit=this.isEdit;
            rec.copyInv=this.copyInv;
            rec.customerid=this.customerid;
            Wtf.Ajax.requestEx({
                url:this.ajxurl,
                params: rec                    
            },this,this.genSuccessResponse,this.genFailureResponse);
        },this);
    },
    
    genSuccessResponse:function(response, request){
        WtfComMsgBox([this.titlel,response.msg],response.success*2+1);
        if(response.success){  
            var rec=this.NorthForm.getForm().getValues();       
            this.Grid.getStore().removeAll();
            this.NorthForm.getForm().reset();
            this.Grid.updateRow(null);
            this.fireEvent('update',this);
            this.isClosable= true;       //Reset Closable flag to avoid unsaved Message.
            this.ownerCt.remove(this);
        }
    },

    genFailureResponse:function(response){
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

    getJSONArray:function(store, includeLast, idxArr){
        var indices="";
        if(idxArr)
            indices=":"+idxArr.join(":")+":";        
        var arr=[];
        var fields=store.fields;
        var len=store.getCount();
        
        for(var i=0;i<len;i++){
            if(idxArr&&indices.indexOf(":"+i+":")<0) continue;
            var rec=store.getAt(i);
            var recarr=[];
            for(var j=0;j<fields.length;j++){
                var value=rec.data[fields.get(j).name];
                switch(fields.get(j).type){
                    case "auto":
                        if(value!=undefined){
                            value=(value+"").trim();
                        }
                        value=encodeURI(value);
                        value="\""+value+"\"";
                        break;
                    case "date":
                        value="'"+WtfGlobal.convertToGenericDate(value)+"'";
                        break;
                }
                recarr.push(fields.get(j).name+":"+value);
            }
            recarr.push("modified:"+rec.dirty);
            arr.push("{"+recarr.join(",")+"}");
        }
        return "["+arr.join(',')+"]";
    },
    
    callDoDetailsWindow:function(j){
        var index=this.Grid.getStore().findBy(function(rec){
            if(rec.data.productid)
                return true;
            else
                return false;
        })
        if(index!=-1){             
            var prorec=this.Grid.getStore().getAt(j); 
            this.doDetailswin=new Wtf.account.DeliveryorderDetailsWindow({
                renderTo: document.body,
                title:WtfGlobal.getLocaleText("erp.EnterAllDOsDetails"),
                isEdit:this.isEdit,
                copyTrans:this.copyTrans,
                width:950,
                record:this.record,
                height:400,
                productrec:prorec.data,
                resizable : false,
                modal : true
            });
            this.doDetailswin.on("beforeclose",function(panel){
                if(panel.isFromSubmitButton){
                    prorec.set("packingdodetails", panel.doDetails);
                }
            },this);
            this.doDetailswin.show();
        }
    },
    
    callPackageDetailsWindow:function(i,isFromBulkPacking){ //ERM-999 isFromBulkPacking flag for bulk packing feature
        var index=this.Grid.getStore().findBy(function(rec){
            if(rec.data.productid)
                return true;
            else
                return false;
        })
        if(index!=-1){
            var prorec=this.Grid.getStore().getAt(i);
            this.packingDetailsWin=new Wtf.account.PackDetailsWindow({
                renderTo: document.body,
                title:WtfGlobal.getLocaleText("erp.EnterAllPackingDetails"),
                isEdit:this.isEdit,
                copyTrans:this.copyTrans,
                width:1200,
                height:400,
                isFromBulkpacking:isFromBulkPacking,
                record:this.record,
                productrec:prorec.data,
                resizable : true,
                modal : true
            });
            this.packingDetailsWin.on("beforeclose",function(panel){
                if(panel.isFromSubmitButton){
                    prorec.set("packingdetails", panel.packingDetails);
                }
            },this);
            if (!isFromBulkPacking) { //ERM-999 if call is not from bulkpacking feature then show window
                this.packingDetailsWin.show();
            }
        }
    }
});


// code to display Packing DO's List Grid
Wtf.account.PackingDeliveryorderGrid=function(config){
    this.isCustomer=config.isCustomer;
    this.productComboStore=this.isCustomer?Wtf.productStoreSales:Wtf.productStore;
    this.currencyid=config.currencyid;
    this.productID=null;
    this.id=config.id;
    this.tempStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },Wtf.productRec)
    });
    this.record=config.record;
    this.billDate=new Date();
    this.dateChange=false;
    this.isNegativeStock=false;
    this.pronamearr=[];
    this.fromPO=config.fromPO;
    this.readOnly=config.readOnly;
    this.copyTrans=config.copyTrans;
    this.editTransaction=config.editTransaction;
    this.parentid=config.parentid;
    this.noteTemp=config.noteTemp;
    this.editLinkedTransactionQuantity= Wtf.account.companyAccountPref.editLinkedTransactionQuantity;
    this.editLinkedTransactionPrice= Wtf.account.companyAccountPref.editLinkedTransactionPrice;
    this.isEdit=config.isEdit;
    this.isDeferredRevenueRecognition=Wtf.account.companyAccountPref.isDeferredRevenueRecognition&&this.isCustomer;
    this.createStore();
    this.createColumnModel();
    Wtf.account.PackingDeliveryorderGrid.superclass.constructor.call(this,config);
    this.addEvents({
        'datachanged':true,
        'productdeleted':true
    });
}
Wtf.extend(Wtf.account.PackingDeliveryorderGrid,Wtf.grid.EditorGridPanel,{
    clicksToEdit:1,
    stripeRows :true,
    rate:1,
    symbol:null,
    layout:'fit',
    viewConfig:{
        forceFit:true
    },
    forceFit:true,
    loadMask:true,
    onRender:function(config){
        Wtf.account.PackingDeliveryorderGrid.superclass.onRender.call(this,config);
        this.isValidEdit = true;
        this.on('afteredit',this.updateRow,this);
        this.on('rowclick',this.handleRowClick,this);
        this.on('beforeedit',function(e){             
            if(e.field == "productid" && e.grid.colModel.config[3].dataIndex=="productid"){                
                if(Wtf.account.companyAccountPref.invAccIntegration && !this.isCustomer){                
                    var store = e.grid.colModel.config[3].editor.field.store;
                    if(store!=undefined && store.data.length>0){                    
                        this.tempStore.removeAll();
                        this.tempStore.add(store.getRange());                
                        this.tempStore.each(function(record){
                            },this);                                
                        e.grid.colModel.config[3].editor.field.store=this.tempStore;
                    }                
                }                        
            } 
             
        },this);       
    },
       
    createStore:function(){         
         
        this.deleteRec = new Wtf.data.Record.create([
        {
            name: 'productid'
        },

        {
            name: 'productname'
        },

        {
            name: 'productquantity'
        },
        {
            name: 'productbaseuomrate'
        },
        {
            name: 'productbaseuomquantity'
        },
        {
            name: 'productuomid'
        },
        {
            name: 'productinvstore'
        },
        {
            name: 'productinvlocation'
        },
        {
            name: 'productrate'
        }
        ]);
        this.deleteStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.deleteRec)                
        });
        
           
        this.storeRec = Wtf.data.Record.create([
        {
            name:'rowid'
        },

        {
            name:'productname'
        },

        {
            name:'billid'
        },

        {
            name:'billno'
        },
        {
            name:'baseuomquantity'
        },
        {
            name:'baseuomrate'
        },

        {
            name:'productid'
        },

        {
            name:'description'
        },
        
        {
            name:'packageNumber'
        },

        {
            name:'quantityindo'
        },

        {
            name:'shipquantity'
        },

        {
            name:'packedqty'
        },
        {
            name:'isdirectshipped'
        },
        {
            name:'unitname'
        },

        {
            name:'remark'
        },
     
        {
            name:'productweight'
        },

        {
            name:'packingdodetails'
        },

        {
            name:'packingdetails'
        },

        {
            name:'remainingquantity'
        },

        {
            name:'oldcurrencyrate'
        },
        {
            name: 'externalcurrencyrate'
        },

        {
            name:'orignalamount'
        },

        {
            name:'typeid',
            defValue:0
        },

        {
            name:'isNewRecord',
            defValue:"0"
        },

        {
            name: 'changedQuantity'
        },

        {
            name:'dquantity'
        },

        {
            name:'permit'
        },
        ]);
        var url=Wtf.req.account+((this.fromOrder||this.readOnly)?((this.isCustomer)?'CustomerManager.jsp':'VendorManager.jsp'):((this.isCN)?'CustomerManager.jsp':'VendorManager.jsp'));

        this.store = new Wtf.data.Store({
            url:url,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.storeRec)
        });
        this.store.on('load',this);
    },

    createColumnModel:function(){                
        this.summary = new Wtf.ux.grid.GridSummary();
        this.rowno=new Wtf.grid.CheckboxSelectionModel();
        var columnArr =[];
        if(!this.readOnly){
            columnArr.push(this.rowno);
        }            
        columnArr.push({
            dataIndex:'rowid',
            hidelabel:true,
            hidden:true
        },{
            dataIndex:'billid',
            hidelabel:true,
            hidden:true
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridProduct"),//"Product",
            anchor:"50%",
            dataIndex:'productname'
        },{
            header:WtfGlobal.getLocaleText("erp.QuantityinDOs"),//Quantity in DO's,
            anchor:"50%",
            dataIndex:'dquantity'
        },{
            header:'Pack Quantity',//Ship Quantity
            dataIndex:"shipquantity",
            anchor:"50%",
            editor:new Wtf.form.NumberField({
                name:'shipquantity'
            })
        });
        columnArr.push({
            header:WtfGlobal.getLocaleText("acc.invoice.gridUOM"),//"UOM",
            dataIndex:"unitname",
            anchor:"50%"
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridDescription"),  //"Remark",
            dataIndex:"description",
            anchor:"50%",
            editor:new Wtf.form.TextField({
                name:'description'
            })
        },{
            header:WtfGlobal.getLocaleText("acc.pickpackship.packageNumber"),  //Package Number (used in UPS integration only),
            dataIndex:"packageNumber",
            anchor:"50%",
            hidden: !Wtf.account.companyAccountPref.upsIntegration,
            editor:new Wtf.form.TextField({
                name:'packageNumber'
            })
        },{   //added the DO's Details'
            header: WtfGlobal.getLocaleText("erp.DODetails"),
            align:'center',
            anchor:"50%",
            dataIndex:"packingdodetails",
            renderer:function(value,meta,rec){                
                value= "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("erp.AddDoDetailsTT")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("erp.AddDoDetails")+"' class='"+getButtonIconCls(Wtf.etype.doDetails)+"'></div>";  
                return value;
            }
        },{   //added the Package Details
            header: WtfGlobal.getLocaleText("erp.PackageDetails"),
            align:'center',
            anchor:"50%",
            hidden: Wtf.account.companyAccountPref.upsIntegration,//Hide package details window in case of UPS integration
            dataIndex:"packagedetails",
            renderer:function(value,meta,rec){                
                value= "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("erp.addpackingdetailsTT")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("erp.addpackingdetails")+"' class='"+getButtonIconCls(Wtf.etype.packingDetails)+"'></div>"; 
                return value;
            }
        });
        if(!this.isNote && !this.readOnly) {
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),//"Action",
                align:'center',
                anchor:"50%",
                renderer: this.deleteRenderer.createDelegate(this)
            });
        }
        this.cm=new Wtf.grid.ColumnModel(columnArr);
    },
    

    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.nee.48"), function(btn){
                if(btn!="yes") return;
                var store=grid.getStore();
                var total=store.getCount();
                var record = store.getAt(rowindex);
                
                var deliverdproqty = record.data.dquantity;
                deliverdproqty = (deliverdproqty == "NaN" || deliverdproqty == undefined || deliverdproqty == null)?0:deliverdproqty;
                
                if(record.data.copyquantity!=undefined){                    
                    var deletedData=[];
                    var newRec=new this.deleteRec({
                        productid:record.data.productid,
                        productname:record.data.productname,    
                        productquantity:deliverdproqty,
                        productbaseuomrate:record.data.baseuomrate,                                
                        productbaseuomquantity:record.data.baseuomquantity,
                        productuomid:record.data.uomid,
                        productinvstore:record.data.invstore                          
                    });                            
                    deletedData.push(newRec);
                    this.deleteStore.add(deletedData);                            
                }
                store.remove(store.getAt(rowindex));
                this.fireEvent('productdeleted',this);
                this.fireEvent('datachanged',this);
            }, this);
        }
    },   

    storeRenderer:function(store, valueField, displayField) {
        return function(value, meta, record) {
            var idx = store.find(valueField, record.data[valueField]);
            if(idx == -1)
                return value;
            var rec = store.getAt(idx);
            return value+" "+rec.data[displayField];
        }
    },
    
    updateRow:function(obj){
        if (obj != null) {
            var rec = obj.record;
            var duequantity = rec.data["dquantity"] == undefined ? 0 : rec.data["dquantity"];
            var packedqty = rec.data["packedqty"] == undefined ? 0 : rec.data["packedqty"]; //get previously packed quantity
            var remainingqty = duequantity - packedqty;
            obj.record.set("productname", rec.data["productname"]);
            obj.record.set("description", rec.data["description"]);
            obj.record.set("unitname", rec.data["unitname"]);
            obj.record.set("dquantity", rec.data["dquantity"]);
            //check previously packed quantity in DO to allow only remaining quantity to be packed
            if (rec.data["dquantity"] < rec.data["shipquantity"] || rec.data["shipquantity"] > (remainingqty) && packedqty != 0) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("erp.dodetailspackwarning")], 2);
                if (packedqty == 0)
                {
                    rec.set("shipquantity", duequantity);
                }
                else
                {
                    rec.set("shipquantity", remainingqty);
                }
                return;
            }//if user has directly shipped do without packing then do not allow anymore packing
            if (packedqty == 0 && rec.data["isdirectshipped"]) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("erp.dodirectshippedwarning")], 2);
                rec.set("shipquantity", 0);
                return;
            }
            else {
                obj.record.set("shipquantity", rec.data["shipquantity"]);
            }
        }
        this.fireEvent('datachanged',this);
    },

    getProductDetails:function(){
        var arr=[];
        this.store.each(function(rec){
            if(rec.data.productid!=""){
                arr.push(this.store.indexOf(rec));
            }
        },this)
        var jarray=WtfGlobal.getJSONArray(this,true,arr);
        return jarray;
    }       
});

//Code to Call Do Details Window
Wtf.account.DeliveryorderDetailsWindow = function(config){
    this.businessPerson=(this.customerFlag?"Customer":"Vendor");
    this.noteType=WtfGlobal.getLocaleText("erp.PleaseEnterItemDetailsasperDOs");
    this.butnArr = new Array();
    this.transactionid=config.transactionid;
    this.isEdit=config.isEdit;
    this.copyTrans=config.copyTrans;
    this.productrec=config.productrec;
    this.recordLength=0;
    this.record=config.record;
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.submit"),   //'Submit',
        scope: this,
        handler:function() {
            if(this.validateDoDetails()){
                this.close();
            }
        }
    },{
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),   //'Cancel',
        scope: this,
        handler: function() {
            this.hide();
        }
    });

    Wtf.apply(this,{
        buttons: this.butnArr
    },config);
    Wtf.account.DeliveryorderDetailsWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.DeliveryorderDetailsWindow, Wtf.Window, {

    onRender: function(config){
        Wtf.account.DeliveryorderDetailsWindow.superclass.onRender.call(this, config);
        this.createDisplayGrid();
        var msg="";        
        var title=this.noteType;
        var isgrid=true;
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(title,msg,"../../images/accounting_image/price-list.gif",isgrid)
        }, this.centerPanel=new Wtf.Panel({
            border: false,
            region: 'center',
            id: 'centerpan'+this.id,
            autoScroll:true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:[this.DODetailsGrid]
        }))
        this.loadEditableDoDetailsGrid();
    },
    
    loadEditableDoDetailsGrid:function(){
        this.doDetailsArray=this.productrec.packingdodetails;
        if(this.doDetailsArray==undefined||this.doDetailsArray==""){
            this.subGridStoreUrl =  "ACCInvoiceCMN/getDeliveryOrderRowsForpackingDoDetails.do" ;
            this.billids="";
            for(var i=0;i<this.record.length;i++){
                this.billids=this.billids+this.record[i].data.billid+",";
            }
            this.DODetailsGrid.getStore().proxy.conn.url = this.subGridStoreUrl;
            this.DODetailsGrid.getStore().on("load", function(){
            
                }, this);
            this.DODetailsGrid.getStore().load({
                params:{
                    bills:this.billids,
                    productid:this.productrec.productid,
                    shipquantity:this.productrec.shipquantity
                }
            });
        }else{
            var doDetailsLength = 0;
            if(this.doDetailsArray && this.doDetailsArray !=''){
                this.doDetailsArr = eval('(' + this.doDetailsArray + ')');
            }
            if(this.doDetailsArr.length>0){
                doDetailsLength = this.doDetailsArr.length;
            }
            for(var i=0;i<doDetailsLength;i++){
                var rec=new this.accRec({
                    id:this.doDetailsArr[i].id!=undefined?this.doDetailsArr[i].id:"",
                    productname:this.doDetailsArr[i].productname!=undefined?this.doDetailsArr[i].productname:"",
                    dono:this.doDetailsArr[i].dono!=undefined?this.doDetailsArr[i].dono:"",
                    amount:this.doDetailsArr[i].select!=undefined?true:"",
                    productid:this.doDetailsArr[i].productid!=undefined?this.doDetailsArr[i].productid:"",
                    quantityindo:this.doDetailsArr[i].quantityindo!=undefined?this.doDetailsArr[i].quantityindo:"",
                    duequantity:this.doDetailsArr[i].duequantity!=undefined?this.doDetailsArr[i].duequantity:"",
                    shipquantity:this.doDetailsArr[i].shipquantity!=undefined?this.doDetailsArr[i].shipquantity:"",
                    billid:this.doDetailsArr[i].billid!=undefined?this.doDetailsArr[i].billid:""  
                }); 
                this.DODetailsStore.add(rec);
            }
            this.DODetailsGrid.getStore().load();
        }
    },
    
    createDisplayGrid:function(){
        this.DoDetailsCM= new Wtf.grid.ColumnModel([{
            header:WtfGlobal.getLocaleText("erp.DONumber"),
            anchor:"50%",
            dataIndex:'dono'
        },{
            header:WtfGlobal.getLocaleText("erp.ActualQuantityinDO"),
            dataIndex:'quantityindo',
            anchor:"50%"
        },{
            header:'Due Quantity for Packing',
            dataIndex:'duequantity',
            anchor:"50%"
        },{
            header:'Pack Quantity',//Ship Quantity
            dataIndex:"shipquantity",
            anchor:"50%",
            editor:new Wtf.form.NumberField({
                name:'shipquantity'    
            })
        }]);   
       
        this.accRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'productname'
        },

        {
            name: 'dono'
        },
        
        {
            name:'billid'
        },
        
        {
            name: 'productid'
        },
        
        {
            name: 'quantityindo'
        },
        
        {
            name:'duequantity',defaultValue:0.0
        },
        
        {
            name: 'shipquantity'
        }
        ]);
        var url=Wtf.req.account+((this.fromOrder||this.readOnly)?((this.isCustomer)?'CustomerManager.jsp':'VendorManager.jsp'):((this.isCN)?'CustomerManager.jsp':'VendorManager.jsp'));
        this.DODetailsStore = new Wtf.data.Store({
            url:url,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });

        this.DODetailsGrid = new Wtf.grid.EditorGridPanel({
            plugins:this.gridPlugins,
            clicksToEdit:1,
            height:230,
            width:'97%',
            store: this.DODetailsStore,
            cm:this.DoDetailsCM,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.DODetailsGrid.on('afteredit',this.updateRow ,this);
    },
    
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    }, 
    
    getJSONArray:function(arr){
        return WtfGlobal.getJSONArray(this.DODetailsGrid,true,arr);
    },        
    
    validateDoDetails:function(obj){
        var arr = [];
        var shipquantity=0;
        var duequantity = "";
        this.DODetailsStore.each(function(record){
            duequantity=record.get("duequantity");
            if(record.get("shipquantity")==undefined||record.get("shipquantity")==''){
                record.set("shipquantity",0);
            }
            if(parseInt(record.get("duequantity"))<parseInt(record.get("shipquantity"))){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.PleaseEnteranappropriatevalueForShipQuantities")],0);
                record.set("shipquantity",duequantity);
            }
            shipquantity=shipquantity+(parseInt(record.get("shipquantity")));
            arr.push(this.DODetailsStore.indexOf(record));
        },this);
        this.doDetails = this.getJSONArray(arr);
        this.isFromSubmitButton=true;
//        if(shipquantity!=this.productrec.shipquantity){
//           WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.PleaseEnteranappropriatevalueForShipQuantities")],0);
//        }else{
//        }
            return true;
    },
    
    genSuccessResponse : function(response){
        this.remainingQuantity=response.quantity;
        if(response.success){ 
            this.close();
        }       
    },
    
    genFailureResponse : function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    closeWin:function(){ /*this.fireEvent('update',this,this.value);*/
        this.isFromSubmitButton=false;
        this.close();
    },
    
    updateRow:function(obj){
        if(obj!=null){            
            var rec=obj.record;
            var duequantity=rec.data["duequantity"]==undefined?0:rec.data["duequantity"];
            obj.record.set("description",rec.data["description"]);
            obj.record.set("quantityindo",rec.data["quantityindo"]);
            obj.record.set("duequantity",rec.data["duequantity"]);
            if(rec.data["shipquantity"]>rec.data["duequantity"]){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.Pleaseentervalidpackingquantity")],2);
                rec.set("shipquantity",duequantity);/*if user enters more quantity than due then set it equal to due quantity*/
                return;
            }else{
                obj.record.set("shipquantity",rec.data["shipquantity"]);
            }          
        }
    }
});  

//code to add Package Details Window
Wtf.account.PackDetailsWindow = function(config){
    this.noteType=WtfGlobal.getLocaleText("erp.PleaseEnterAllPackingDetails");
    this.butnArr = new Array();
    this.transactionid=config.transactionid;
    this.isEdit=config.isEdit;
    this.copyTrans=config.copyTrans;
    this.billid=config.billid;
    this.record=config.record;
    this.productrec=config.productrec;
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.submit"),   //'Submit',
        scope: this,
        handler: function() {
            if(this.validatePackingDetails()){
                this.close();
            }
        }
    },{
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),   //'Cancel',
        scope: this,
        handler: function() {
            this.close();
        }
    });
    Wtf.apply(this,{
        buttons: this.butnArr
    },config);
    Wtf.account.PackDetailsWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.PackDetailsWindow, Wtf.Window, {
    onRender: function(config){
        Wtf.account.PackDetailsWindow.superclass.onRender.call(this, config);
        this.createDisplayGrid();   
        var title=this.noteType;
        var msg="";
        var isgrid=true;
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(title,msg,"../../images/accounting_image/price-list.gif",isgrid)
        }, this.centerPanel=new Wtf.Panel({
            border: false,
            region: 'center',
            id: 'centerpan'+this.id,
            autoScroll:true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:[this.packageDetailsGrid]
        }))
        this.loadPackingDetailGrid();
    },
    
    loadPackingDetailGrid:function(){
        this.packingDetailsArray=this.productrec.packingdetails;
        if(this.packingDetailsArray==undefined||this.packingDetailsArray==""){
            var rec=new this.accRec({
                packageno:"",
                productname:this.productrec.productname,
                baseuomquantity:this.productrec.baseuomquantity,
                baseuomrate:this.productrec.baseuomrate,
                productid:this.productrec.productid,
                description:this.productrec.description,
                productweight:this.productrec.productweight,
                billid:this.productrec.billid
            });
            this.packageDetailsGrid.getStore().add(rec);
            this.packageDetailsGrid.getStore().load();
         }else{
            var packingDetailsLength = 0;
            if(this.packingDetailsArray && this.packingDetailsArray !=''){
                this.packingDetailsArr = eval('(' + this.packingDetailsArray + ')');
            }
           if(this.packingDetailsArr.length>0){
                packingDetailsLength = this.packingDetailsArr.length;
            }
            for(var i=0;i<packingDetailsLength;i++){
                var rec=new this.accRec({
                    id:this.packingDetailsArr[i].id!=undefined?this.packingDetailsArr[i].id:"",
                    packages:this.packingDetailsArr[i].packages!=undefined?this.packingDetailsArr[i].packages:"",
                    productname:this.packingDetailsArr[i].productname!=undefined?this.packingDetailsArr[i].productname:"",
                    productid:this.packingDetailsArr[i].productid!=undefined?this.packingDetailsArr[i].productid:"",
                    packageno:this.packingDetailsArr[i].packageno!=undefined?this.packingDetailsArr[i].packageno:"",
                    packagemeasurement:this.packingDetailsArr[i].packagemeasurement!=undefined?this.packingDetailsArr[i].packagemeasurement:"",
                    baseuomquantity:this.packingDetailsArr[i].baseuomquantity!=undefined?this.packingDetailsArr[i].baseuomquantity:"",
                    baseuomrate:this.packingDetailsArr[i].baseuomrate!=undefined?this.packingDetailsArr[i].baseuomrate:"",
                    description:this.packingDetailsArr[i].description!=undefined?this.packingDetailsArr[i].description:"",
                    packagequantity:this.packingDetailsArr[i].packagequantity!=undefined?this.packingDetailsArr[i].packagequantity:"",
                    packageperquantity:this.packingDetailsArr[i].packageperquantity!=undefined?this.packingDetailsArr[i].packageperquantity:"" ,
                    netweight:this.packingDetailsArr[i].netweight!=undefined?this.packingDetailsArr[i].netweight:"" , //ERM-999 bulk packing feature
                    grossweight:this.packingDetailsArr[i].grossweight!=undefined?this.packingDetailsArr[i].grossweight:"" ,
                    packageweight:this.packingDetailsArr[i].packageweight!=undefined?this.packingDetailsArr[i].packageweight:"" ,
                    productweight:this.packingDetailsArr[i].productweight!=undefined?this.packingDetailsArr[i].productweight:"" ,
                    totalpackagequantity:this.packingDetailsArr[i].totalpackagequantity!=undefined?this.packingDetailsArr[i].totalpackagequantity:"" ,
                    billid:this.packingDetailsArr[i].billid!=undefined?this.packingDetailsArr[i].billid:"" 
                }); 
                this.packageDetailsGrid.getStore().add(rec);
            }
            this.packageDetailsGrid.getStore().load();
        }
    },

    createDisplayGrid:function(){
        this.sm = new Wtf.grid.CheckboxSelectionModel({    
            });
        
        this.packageRec = new Wtf.data.Record.create([
        {
            name: 'packageid'
        },

        {
            name: 'packagename'
        },

        {
            name: 'measurement'
        },

        {
            name: 'packageweight'
        }
        ]);
        
        this.packageStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.packageRec),
            url : "ACCMaster/getPackages.do",
            baseParams:{
                mode:100
            }
        });
        this.packageStore.load();
        this.packageStore.on("load",function(combo){
            this.packageDetailsGrid.getView().refresh();
            this.doLayout();
        },this);
        
        this.packageEditor = new Wtf.form.FnComboBox({
            id:'packageComboEditor',
            triggerAction:'all',
            mode: 'local',
            fieldLabel:WtfGlobal.getLocaleText("erp.package"),
            valueField:'packageid',
            displayField:'packagename',
            store:this.packageStore,
            anchor:'90%',
            typeAhead: true,
            selectOnFocus:true,
            forceSelection: true,
            name:'packageid',
            hiddenName:'packageid',
            scope: this,
            addNewFn: this.callpackingWindow
        });
                
        this.packageEditor.on("blur",function(e,a,b){
            e.store=this.packageStore;  
        },this);
               
        this.packageDetails= new Wtf.grid.ColumnModel([{
            header:WtfGlobal.getLocaleText("erp.PackageNo"),
            width:150,
            dataIndex:'packageno',
            editor:new Wtf.form.TextField({
                name:'packageno',
                maxLength: 100
            }) 
        },{
            header:WtfGlobal.getLocaleText("erp.Package"),
            width:150,
            dataIndex:'packages',
            renderer:Wtf.comboBoxRenderer(this.packageEditor),
            editor:this.packageEditor
        },{
            header:WtfGlobal.getLocaleText("erp.PackageWeightinKg"),
             width:150,
            dataIndex:'packageweight',
            editor:new Wtf.form.NumberField({
                name:'packageweight'
            })
        },{
            header:WtfGlobal.getLocaleText("erp.PackageMeasurementlenghtweidthheight"),
            width:300,
            dataIndex:'packagemeasurement'
        },{
            header:WtfGlobal.getLocaleText("acc.product.gridProduct"),
             width:150,
            dataIndex:'productname'
        },{
            header:WtfGlobal.getLocaleText("acc.erp.ProductWeight"),
            width:150,
            dataIndex:'productweight'
        },{
            header:WtfGlobal.getLocaleText("acc.product.description"),
            dataIndex:'description',
             width:150
        },{
            header:WtfGlobal.getLocaleText("erp.NoOfPackage"),
            dataIndex:'packagequantity',
            width:150,
            editor:new Wtf.form.NumberField({
                name:'packagequantity'
            })     
        },{
            header:WtfGlobal.getLocaleText("erp.QuantityPerPackage"),
            dataIndex:'packageperquantity',
             width:150,
            editor:new Wtf.form.NumberField({
                name:'packageperquantity'
            }) 
        },{
            header:WtfGlobal.getLocaleText("erp.TotalPackageQuantity"),
            dataIndex:'totalpackagequantity',
             width:150
        },{
            header:WtfGlobal.getLocaleText("erp.NetWeight"),
            dataIndex:'netweight',
             width:150
        },{
            header:WtfGlobal.getLocaleText("erp.GrossWeight"),
            dataIndex:"grossweight",
             width:150
        }]);
              
        this.accRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'packages'
        },

        {
            name: 'productname'
        },
        {
            name: 'productid'
        },
        {
            name: 'packageno'  
        },
        {
            name: 'packagemeasurement'  
        },
        {
            name: 'description'
        },
        {
            name: 'packagequantity'
        },
        {
            name: 'packageperquantity'
        },
        {
            name: 'grossweight'
        },
        {
            name: 'netweight'
        }, 
        {
            name: 'packageweight'
        },
        {
            name: 'productweight'
        },
        {
            name:'totalpackagequantity'
        },
        {
            billid:'billid'
        }
        ]);
        
        var url=Wtf.req.account+((this.fromOrder||this.readOnly)?((this.isCustomer)?'CustomerManager.jsp':'VendorManager.jsp'):((this.isCN)?'CustomerManager.jsp':'VendorManager.jsp'));
        this.store = new Wtf.data.Store({
            url:url,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
       
        this.packageDetailsGrid = new Wtf.grid.EditorGridPanel({
            plugins:this.gridPlugins,
            clicksToEdit:1,
            cls:'gridFormat',
            autoScroll:true,
            height:230,
            width:'97%',
            store: this.store,
            cm: this.packageDetails,
            border : false,
            loadMask : true, 
            viewConfig: {
                forceFit:false,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.packageDetailsGrid.on('afteredit',this.updateRow,this);
    },
    
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },    
                  
    getJSONArray:function(arr){
        return WtfGlobal.getJSONArray(this.packageDetailsGrid,true,arr);
    },        
    callpackingWindow:function(){
        callPackageWindow();
        Wtf.getCmp('packagewindow').on('update', function(){
            Wtf.getCmp('packageComboEditor').store.reload();
            Wtf.getCmp('packagewindow').close();
        }, this);
    },        
    
    validatePackingDetails:function(obj){
        var arr = [];
        var packedshipquantity=0;
        var temprecord;
        var checkPackage=true;
        this.store.each(function(record){
//            if(this.productrec.shipquantity<(record.get("packagequantity")*record.get("packageperquantity"))){
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.packagequanntitywarning")],0);
//            }
            temprecord=record;
            arr.push(this.store.indexOf(record));
            packedshipquantity=packedshipquantity+(record.get("packagequantity")*record.get("packageperquantity"));
//            if(record.get("packages")==undefined ||record.get("packages")==''){
//                 checkPackage=false;
//                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.packageselectionalert")],0);
//            }
        },this);
        this.packingDetails = this.getJSONArray(arr);
        this.isFromSubmitButton=true;
        if(packedshipquantity==this.productrec.shipquantity && checkPackage){
            return true;
        }else if(checkPackage){
            Wtf.MessageBox.alert(WtfGlobal.getLocaleText("erp.PackingDetails"),WtfGlobal.getLocaleText("erp.Youhavenotpackedallquantitiespleasetakeanotherpackage"),function(btn){
                if(btn=="ok") {
                    this.addBlankRow(temprecord);
                } 
            },this);
        }
    },
    
    genSuccessResponse : function(response){
        if(response.success){
            this.close();
        }
    },
    
    genFailureResponse : function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

    closeWin:function(){ /*this.fireEvent('update',this,this.value);*/
        this.close();
    },
    
    updateRow:function(obj){
        if(obj!=null){
                this.packageStore.clearFilter();
                var rec = obj.record;
                var packageid = rec.data["packages"];
                var packageComboRecIndex = WtfGlobal.searchRecordIndex(this.packageStore, packageid, 'packageid');
                var pack = this.packageStore.getAt(packageComboRecIndex);
                if (obj.field=='packages') {
                    obj.record.set("description", rec.data["description"]);
                    obj.record.set("packagequantity", rec.data["packagequantity"]);
                    obj.record.set("packageperquantity", rec.data["packageperquantity"]);
                    obj.record.set("packages", pack.data.packageid);
                    obj.record.set("packagemeasurement", pack.data.measurement);
                    obj.record.set("packageweight", pack.data.packageweight);
                }
            if(this.productrec.shipquantity<(rec.data["packagequantity"]*rec.data["packageperquantity"])){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.Pleaseentervalidpackingquantity")],2);
                return;
            }else{
                obj.record.set("totalpackagequantity",rec.data["packagequantity"]==undefined||rec.data["packageperquantity"]==undefined?0:rec.data["packagequantity"]*rec.data["packageperquantity"]);

            }
            //SDP-8991 Calculating gross weight of DO grossweight = productweight + packageweight.  
            //the net weight will be (productweight - packageweight).
            var grossweight = 0;
            var packageweight = (rec.data["packagequantity"]==undefined||rec.data["packageperquantity"]==undefined)?0:(rec.data["packageweight"]*rec.data["packagequantity"]);
            var productweight = ((this.productrec.baseuomrate!=undefined || this.productrec.baseuomrate!='')?this.productrec.baseuomrate * (rec.data['packageperquantity']*rec.data['packagequantity']) * this.productrec.productweight:this.productrec.productweight);
            productweight = productweight!=productweight ? "" : productweight.toFixed(1);
            grossweight = parseFloat(packageweight) + parseFloat(productweight);
            grossweight = grossweight!=grossweight ? "" : grossweight.toFixed(1);
            obj.record.set("netweight",(productweight==undefined || productweight!=productweight)?"":productweight);
            obj.record.set("grossweight",(grossweight==undefined || grossweight!=grossweight)?"":grossweight);  //grossweight may return NaN
        }
        this.fireEvent('datachanged',this);
    },
     
     addBlankRow:function(record){
         var Record = this.store.reader.recordType,f = Record.prototype.fields, fi = f.items, fl = f.length;
        var values = {},blankObj={};
        for(var j = 0; j < fl; j++){
            f = fi[j];
        if(f.name!='rowid') {
                blankObj[f.name]='';
                if(!Wtf.isEmpty(f.defValue))
                    blankObj[f.name]=f.convert((typeof f.defValue == "function"?f.defValue.call():f.defValue));
            }
        }
        var newrec = new Record(blankObj);
        newrec.data.packageno="";
        newrec.data.productname=record.data.productname;
        newrec.data.productid=record.data.productid;
        newrec.data.productweight=record.data.productweight;
        newrec.data.description=record.data.description;
        newrec.data.billid=record.data.billid;
        this.store.add(newrec);
    }
});  


/********************** Packing DO List Report *************************/
Wtf.account.PackingReportPanel=function(config){
    this.invID=null;
    this.recArr=[];
    this.isCash=true;
    this.businessPerson=(config.isCustomer?'Customer':'Vendor');
    this.isfavourite=false;
    this.moduleid=config.moduleid;
    this.isUnInvoiced=false;
    this.consolidateFlag = config.consolidateFlag;
    this.index = "";
    this.extraFilters = config.extraFilters;
    Wtf.apply(this, config);
    (this.businessPerson == "Customer")? Wtf.DOStatusStore.load() : Wtf.GROStatusStore.load();
    this.isLeaseFixedAsset = (config.isLeaseFixedAsset==null || config.isLeaseFixedAsset==undefined)?false:config.isLeaseFixedAsset;
    this.label = config.label;
    this.nondeleted=false;
    this.deleted=false;
    this.uPermType=(config.isCustomer?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
    this.permType=(config.isCustomer?Wtf.Perm.invoice:Wtf.Perm.vendorinvoice);
    this.moduleid=config.moduleid;
    this.exportPermType=true;//(config.isCustomer?(this.isOrder?this.permType.exportdataso:this.permType.exportdatainvoice):(this.isOrder?this.permType.exportdatapo:this.permType.exportdatavendorinvoice));
    this.printPermType=true;//(config.isCustomer?(this.isOrder?this.permType.printso:this.permType.printinvoice):(this.isOrder?this.permType.printpo:this.permType.printvendorinvoice));
    this.removePermType=true;//(config.isCustomer?(this.isOrder?this.permType.removeso:this.permType.removeinvoice):(this.isOrder?this.permType.removepo:this.permType.removevendorinvoice));
    this.editPermType=true;//(config.isCustomer?(this.isOrder?this.permType.editso:this.permType.editinvoice):(this.isOrder?this.permType.editpo:this.permType.editvendorinvoice));
    this.emailPermType=true;//(config.isCustomer?this.permType.emailinvoice:this.permType.emailvendorinvoice);
    this.reportbtnshwFlag=config.reportbtnshwFlag;
    
    this.accExpandRec=Wtf.data.Record.create ([
    {
        name:'packingdolistdetails'
    },
    
    {
        name:'shipingdodetails'
    },
    
    {
        name:'itempackingdetails'
    },
    
    {
        name:'upsPackageDetails'
    }
    ]);
    
    this.expandStoreUrl = "ACCInvoiceCMN/getPackingRows.do";
    
    this.expandStore1 = new Wtf.data.Store({
        url:this.expandStoreUrl,
        baseParams:{
            mode:14,
            dtype : 'report'
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.accExpandRec)
    });
    
    this.expandProductRec = Wtf.data.Record.create ([
    {
        name:'productname'
    },
    
    {
        name:'productid'
    },

    {
        name:'actualquantity'
    },

    {
        name:'deliveredquantity'
    },

    {
        name:'unitname'
    },

    {
        name: 'type'
    },

    {
        name: 'pid'
    },

    {
        name:'desc'
    },

    {
        name:'description'
    },

    {
        name:'remark'
    }
    ]);
   
    this.expandStore = new Wtf.data.Store({
        url:"",
        baseParams:{
            mode:14,
            dtype : 'report'
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "packingdolistdetails"
        },this.expandProductRec)
    });
    
    this.expandShipingDoRec = Wtf.data.Record.create ([
    {
        name:'productname'
    },
 
    {
        name:'productid'
    },

    {
        name:'quantityindo'
    },

    {
        name:'shipquantity'
    },

    {
        name:'unitname'
    },

    {
        name:'duequantity'
    },

    {
        name:'desc', 
        convert:WtfGlobal.shortString
    },

    {
        name: 'type'
    },

    {
        name: 'pid'
    },
    
    {
        name:'dono'
    },

    {
        name:'actualquantity'
    },

    {
        name:'description'
    },

    {
        name:'remark'
    }
    ]);
    
    this.expandShipingDoStore = new Wtf.data.Store({
        url:"",
        baseParams:{
            mode:14,
            dtype : 'report'
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "shipingdodetails"
        },this.expandShipingDoRec)
    });
    
    this.expandUpsPackageDetailsRec = Wtf.data.Record.create ([
    {
        name:'rowid'
    },
    {
        name:'billid'
    },
    {
        name:'srno'
    },

    {
        name:'packageNumber'
    },

    {
        name:'packagingType'
    },

    {
        name:'packageWeight'
    },

    {
        name:'packageDimensions'
    },

    {
        name:'declaredValue'
    },

    {
        name:'deliveryConfirmationType'
    },

    {
        name: 'additionalHandling'
    },

    {
        name: 'trackingNumber'
    }
    ]);
    
    this.expandUpsPackageDetailsStore = new Wtf.data.Store({
        url:"",
        baseParams:{
            mode:14,
            dtype : 'report'
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "upsPackageDetails"
        },this.expandUpsPackageDetailsRec)
    });
    
    this.expandItemPackingRec = Wtf.data.Record.create ([
    {
        name:'productname'
    },

    {
        name:'productid'
    },

    {
        name:'packageid'
    },

    {
        name:'packagename'
    },

    {
        name:'packagequantity'
    },

    {
        name:'itemperpackage'
    },

    {
        name:'unitname'
    },

    {
        name: 'type'
    },

    {
        name: 'pid'
    },

    {
        name:'totalquantity'
    },

    {
        name:'description'
    },

    {
        name:'remark'
    },
    
    {
        name:'productweight'
    },
    
    {
        name:'packageweight'
    },
    
    {
        name:'upstrackingnumber'//To display tracking number received from UPS on Item Details rows
    },
    
    {
        name:'grossweight'
    },
    
    {
        name:'measurement'  //to display measurement
    }    
    ]);
    
    this.expandItemPackingStore = new Wtf.data.Store({
        url:"",
        baseParams:{
            mode:14,
            dtype : 'report'
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "itempackingdetails"
        },this.expandItemPackingRec)
    });
    
    this.GridRec = Wtf.data.Record.create ([
    {
        name:'billid'
    },

    {
        name:'companyid'
    },

    {
        name:'companyname'
    },

    {
        name:'entryno'
    },

    {
        name:'billno'
    },

    {
        name:'date', 
        type:'date'
    },

    {
        name:'personname'
    },

    {
        name:'personid'
    },

    {
        name:'dono'
    },
    {
        name:'deleted'
    },

    {
        name:'memo'
    },

    {
        name:'statusID'
    },

    {
        name:'status'
    },

    {
        name:'totalShippingCost'
    },
    
    {
        name:'shippedWithUPS'
    },
    
    {
        name:'withoutinventory',
        type:'boolean'
    },

    {
        name:'isfavourite'
    }
    ]);
    this.StoreUrl = "";
    if(this.businessPerson=="Customer"){
        this.StoreUrl = "ACCInvoiceCMN/getPackingMerged.do";
    }

    if(config.consolidateFlag) {
        this.Store = new Wtf.data.GroupingStore({
            url:this.StoreUrl,
            remoteSort: true,
            baseParams:{
                deleted:false,
                nondeleted:false,
                consolidateFlag:config.consolidateFlag,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                isfavourite:false,
                userid:loginid
            },
            sortInfo : {
                field : 'companyname',
                direction : 'ASC'
            },
            groupField : 'companyname',
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                upsErrorJSON:'upsErrorJSON',
                totalProperty:'count'
            },this.GridRec)
        });
    } else {
        this.Store = new Wtf.data.Store({
            url:this.StoreUrl,
            remoteSort: true,
            baseParams:{
                deleted:false,
                nondeleted:false,
                consolidateFlag:config.consolidateFlag,
                companyids:companyids,
                isfavourite:false,
                gcurrencyid:gcurrencyid,
                userid:loginid
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                upsErrorJSON:'upsErrorJSON',
                totalProperty:'count'
            },this.GridRec)
        });
    }
    
    this.startDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate' + this.id,
        format:WtfGlobal.getOnlyDateFormat(),
        value:WtfGlobal.getDates(true)
    });
    
    this.endDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate' + this.id,
        value:WtfGlobal.getDates(false)
    });
    
    this.productRec = Wtf.data.Record.create([
        {
            name: 'productid'
        },

        {
            name: 'productname'
        },

        {
            name: 'desc'
        },
        {
            name: 'producttype'
        },        
        {
            name: 'pid'
        }
    ]);
    
    this.productStore = new Wtf.data.Store({
        url: "ACCProduct/getProductsForCombo.do",
        baseParams: {
            mode: 22,
            onlyProduct:true
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.productRec)
    });

    this.productname = new Wtf.form.ExtFnComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.productList.gridProduct") + '*',
        hiddenName: 'productid',
        name: 'productid',
        hidden: this.isCustBill,
        store: this.productStore,
        valueField: 'productid',
        displayField: 'productname',
        mode: 'local',
        extraFields:['pid'],
        typeAhead: true,
        triggerAction: 'all',
        hideLabel: true,
        emptyText: WtfGlobal.getLocaleText("acc.msgbox.17"),
        width:150,
        listWidth: 150
    });
    
    this.productStore.load();
    
    this.productStore.on("load", function() {
        var record = new Wtf.data.Record({
            productid: "",
            productname: "All Records",
            pid: ""
        });
        this.productStore.insert(0, record);
        this.productname.setValue("");
    }, this);
    
    this.productCategoryRec = Wtf.data.Record.create([
    {
        name: 'id'
    },

    {
        name: 'name'
    }
    ]);
    
    this.productCategoryStore = new Wtf.data.Store({
        url: "ACCMaster/getMasterItems.do",
        baseParams: {
            mode:112,
            groupid:19
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.productCategoryRec)
    });
    
    this.productCategory = new Wtf.form.ComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.masterConfig.19"),
        hiddenName: 'id',
        name: 'id',
        hidden: this.isCustBill,
        store: this.productCategoryStore,
        valueField: 'id',
        displayField: 'name',
        mode: 'local',
        typeAhead: true,
        triggerAction: 'all',
        hideLabel: true,
        emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseselectaproductcategory"),
        width:100,
        listWidth:150
    });
    
    this.productCategoryStore.load();
    this.productCategoryStore.on("load", function() {
        var record = new Wtf.data.Record({
            id: "",
            name: "All Records"
        });
        this.productCategoryStore.insert(0, record);
        this.productCategory.setValue("");
    }, this);    
   
    this.submitBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),
        tooltip :WtfGlobal.getLocaleText("acc.invReport.fetchTT"),  
        id: 'submitRec' + this.id,
        scope: this,
        iconCls:'accountingbase fetch',
        disabled :false
    });    
    
    this.submitBttn.on("click", this.submitHandler, this);
    
    this.calculateTotalCostBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.invoiceList.calculateTotalCost"),
        tooltip : WtfGlobal.getLocaleText("acc.invoiceList.calculateTotalCostTT"),  
        id: 'calculateTotalCost' + this.id,
        scope: this,
        iconCls:'accountingbase fetch',
        disabled :true
    });
    
    this.calculateTotalCostBttn.on("click", this.calculateTotalCostBttnHandler, this);
    
    this.tbar2 = new Array();
    this.tbar2.push(WtfGlobal.getLocaleText("acc.common.from"));
    this.tbar2.push(this.startDate);
    this.tbar2.push(WtfGlobal.getLocaleText("acc.common.to"));
    this.tbar2.push(this.endDate);    
    this.tbar2.push("-", WtfGlobal.getLocaleText("acc.invReport.prod"), this.productname);
    this.tbar2.push("-", WtfGlobal.getLocaleText("acc.masterConfig.19"),this.productCategory);
    this.tbar2.push("-");
    this.tbar2.push(this.submitBttn);
    if(Wtf.account.companyAccountPref.upsIntegration) {
        this.tbar2.push(this.calculateTotalCostBttn);
//        this.tbar2.push(this.downloadLabelBttn);
    }

    this.expander = new Wtf.grid.RowExpander({});
    this.sm = new Wtf.grid.CheckboxSelectionModel({
        singleSelect : false
    });
    this.gridView1 = (config.consolidateFlag||this.isSalesCommissionStmt)?new Wtf.grid.GroupingView({
        forceFit:false,
        showGroupName: true,
        enableNoGroups:false, // REQUIRED!
        hideGroupedColumn: true,
        emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
    }):{
        forceFit:false,
        emptyText:WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript: callDeliveryOrder(false,null)'>"+" "+WtfGlobal.getLocaleText("acc.rem.147")+" "+this.label+" "+WtfGlobal.getLocaleText("acc.rem.148")+"</a>")  
    };
    
    var columns = [this.sm,this.expander,
        {
            hidden:true,
            header: "",
            dataIndex:'billid'
        },{
            header: WtfGlobal.getLocaleText("acc.field.Company"),  
            dataIndex:'companyname',
            width:20,
            pdfwidth:150,
            hidden:true
        },{
            header:this.label+" "+WtfGlobal.getLocaleText("acc.cn.9"),
            dataIndex:'billno',
            width:200,
            pdfwidth:75,
            sortable:true
        //            renderer:WtfGlobal.linkDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("erp.DONumber"),
            dataIndex:'dono',
            align:'center',
            width:200,
            pdfwidth:80,
            sortable:true
         }
        ,{
            header:this.label+" "+WtfGlobal.getLocaleText("acc.inventoryList.date"),
            dataIndex:'date',
            align:'center',
            width:200,
            pdfwidth:80,
            sortable:true,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:(config.isCustomer?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven")),  //this.businessPerson,
            width:200,
            pdfwidth:75,
            renderer:WtfGlobal.deletedRenderer,
            sortable:true,
            dataIndex:'personname'
        },{
            header:Wtf.account.companyAccountPref.descriptionType,  //"Memo",
            dataIndex:'memo',
            width:200,
            renderer:function(value){
                var res = "<span class='gridRow' style='width:200px;'  wtf:qtip='"+value+"'>"+Wtf.util.Format.ellipsis(value,20)+"</span>";
                return res;
            },
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.status"),  //"Status",
            dataIndex:'status',
            width:200,
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:100
        }];
    if(Wtf.account.companyAccountPref.upsIntegration) {
        columns.push({
            header: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.invoiceList.totalShippingCost") + " (USD)" + "'>" + WtfGlobal.getLocaleText("acc.invoiceList.totalShippingCost") + " (USD)" + "</span>",  //"Total Shipping Cost", Added only if UPS Integration is activated
            dataIndex:'totalShippingCost',
            align: 'right',
            width:200,
            renderer: function (value, metaData, record) {
                if (value != undefined && value != '' && value != 0) {
                    return "<span wtf:qtip='" + "USD " + value + "'>" + "USD " + value + "</span>";
                } else {
                    return "";
                }
            },
            pdfwidth:100
        });
    }
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.Store,
        id:"gridmsg"+config.helpmodeid+config.id,
        border:false,
        sm:this.sm,
        tbar: this.tbar2,
        disabled:this.readOnly,
        disabledClass:"newtripcmbss",
        layout:'fit',
        loadMask:true,
        plugins: this.expander,
        viewConfig:this.gridView1,
        forceFit:true,
        columns:columns
    });
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);  
    var btnArr=[];
    var bottombtnArr=[];
    var tranType=Wtf.autoNum.PackingDoList;
        
    if(!WtfGlobal.EnableDisable(this.uPermType, this.editPermType) && Wtf.account.companyAccountPref.editTransaction){				//!this.isOrder&&
        btnArr.push(this.editBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.edit"),  //'Edit',
            tooltip :(this.isOrder)?WtfGlobal.getLocaleText("acc.invoiceList.editO"):WtfGlobal.getLocaleText("acc.invoiceList.editI"),  //'Allows you to edit Order.':'Allows you to edit Invoice.',
            id: 'btnEdit' + this.id,
            scope: this,
            hidden:true,
            iconCls :getButtonIconCls(Wtf.etype.edit),
            disabled :true
        }));
        this.editBttn.on('click',this.editOrderTransaction,this);
    }
   
    var deletebtnArray=[];

        deletebtnArray.push(this.deleteTransPerm=new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.rem.7")+' '+this.label+' '+WtfGlobal.getLocaleText("acc.field.Permanently"),
            scope: this,
            tooltip:WtfGlobal.getLocaleText("acc.rem.6")+' '+WtfGlobal.getLocaleText("acc.field.Permanently"),  //{text:"Select a "+this.label+" to delete.",dtext:"Select a "+this.label+" to delete.", etext:"Delete selected "+this.label+" details."},
            iconCls:getButtonIconCls(Wtf.etype.menudelete),
            disabled :true,
            handler:this.handleDelete.createDelegate(this,this.del=["delp"])
        }))

    if(deletebtnArray.length>0) {
        btnArr.push({
            text:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"), 
            scope: this,
            tooltip:WtfGlobal.getLocaleText("acc.field.allowsyoutodeletetherecord"), 
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            menu:deletebtnArray
        });
    }

    this.operationType = tranType;
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: this.moduleid,
        advSearch: false
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    //Export record button //ERP-33141
    bottombtnArr.push('-', this.expButton = new Wtf.exportButton({
        obj: this,
        filename: WtfGlobal.getLocaleText("erp.PackingReport"),
        text: WtfGlobal.getLocaleText("acc.common.export"),
        disabled :false,
        tooltip: WtfGlobal.getLocaleText("acc.sales.exportTT"), //'Export report details'
        usePostMethod: true,
        isEntrylevel:false,
        menuItem: {
            csv: true,
            pdf: true,
            xls: true,
            detailedXls: true
        },
        get: Wtf.autoNum.PackingReport
    }));    
    bottombtnArr.push(this.singlePrint=new Wtf.exportButton({
        obj:this,
        id:"printReports"+config.helpmodeid+config.id,
        iconCls: 'pwnd exportpdfsingle',
        text:WtfGlobal.getLocaleText("acc.rem.39.single"),// + " "+ singlePDFtext,
        tooltip :WtfGlobal.getLocaleText("acc.rem.39.singletooltip"),      //'Export selected record(s)',
        disabled :true,
        hidden:true,
        isEntrylevel:false,
        menuItem:{
            rowPdf:true,   
            rowPdfTitle:WtfGlobal.getLocaleText("acc.rem.39") + " "+ this.label
        },
        get:tranType,
        moduleid:config.moduleid
    }));
    bottombtnArr.push(this.singleRowPrint=new Wtf.exportButton({
        obj:this,
        id:"printSingleRecord"+config.helpmodeid+config.id,
        iconCls: 'pwnd printButtonIcon',
        text:WtfGlobal.getLocaleText("acc.rem.236"),
        tooltip :WtfGlobal.getLocaleText("acc.rem.236.single"),//'Print Single Record Details',
        disabled :true,
        hidden:true,
        isEntrylevel:false,
        menuItem:{
            rowPrint:true
        },
        get:tranType,
        moduleid:config.moduleid
    }));
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.rem.5")+" "+this.label,
        width: 150,
        id:"quickSearch"+config.helpmodeid+config.id,
        field: 'billno',
        Store:this.Store
    })
    this.tbar1 = new Array();
    this.tbar1.push(this.quickPanelSearch, this.resetBttn, btnArr);    
    this.leadpan = new Wtf.Panel({
        layout: 'border',
        border: false,
        attachDetailTrigger: true,
        items: [this.objsearchComponent
        , {
            region: 'center',
            border:false,
            layout : "fit",
            tbar: this.tbar1,//this.quickPanelSearch,
            items:[this.grid],
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.Store,
                searchField: this.quickPanelSearch,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                }),
                items:bottombtnArr
            })
        }]
    }); 
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        items:[ this.leadpan]
    });
    this.Store.on('beforeload',function(s,o){
        if(!o.params)o.params={};
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.deleted=this.deleted;
        currentBaseParams.nondeleted=this.nondeleted;
        currentBaseParams.startdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        currentBaseParams.isfavourite=this.isfavourite;
        currentBaseParams.isUnInvoiced=this.isUnInvoiced;
        currentBaseParams.productid = this.productname.getValue();
        currentBaseParams.productCategoryid = this.productCategory.getValue();
        currentBaseParams.isFixedAsset=this.isFixedAsset;
        this.Store.baseParams=currentBaseParams;
    },this);
    this.loadParmStore();
    this.expandStore1.on('load',this.fillExpanderBody,this);
    this.expander.on("expand",this.onRowexpand,this);
    this.Store.on('load',this.expandRow, this);
    this.Store.on('load',this.hideLoading, this);
    this.Store.on('loadexception',this.hideLoading, this);
    Wtf.account.PackingReportPanel.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });
    this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);
    this.grid.on('cellclick',this.onCellClick, this);
}
Wtf.extend(Wtf.account.PackingReportPanel,Wtf.Panel,{
    submitHandler : function(){
        this.loadStore();
    },
    
    calculateTotalCostBttnHandler : function(){
        var previouslySelectedRecordID = this.recordIDForCostCalculation;
        var selectedRecordsArr = this.grid.getSelectionModel().getSelections();
        var selectedRecordsIndexArr = [];
        for(var i=0; i<selectedRecordsArr.length; i++) {
            selectedRecordsIndexArr.push(this.Store.indexOf(selectedRecordsArr[i]));
        }
        var recordsForCostCalculation= WtfGlobal.getJSONArrayWithoutEncodingNew(this.grid, true, selectedRecordsIndexArr);
        var recordsForCostCalculationArr = JSON.parse(recordsForCostCalculation);
        this.recordIDForCostCalculation = recordsForCostCalculationArr[0].billid;
        
        //if currently selected record is not same as previously selected record, then we destroy existing component and create new one
        if (this.calculateTotalCostWindow && this.recordIDForCostCalculation != previouslySelectedRecordID) {
            this.calculateTotalCostWindow.destroy();
            this.calculateTotalCostWindow = undefined;
        }
        
        if (!this.calculateTotalCostWindow) {
            //request to fetch ship-from and ship-to addresses for shipment
            Wtf.Ajax.requestEx({
                url: "Integration/getAddressesForUps.do",
                params: {
                    billid : this.recordIDForCostCalculation//sending recordID in key bills because it is required in this key by method getPackingRows on javaside
                }
            }, this, this.genAddressesSuccessResponse, this.genAddressesFailureResponse);
        } else {
            this.calculateTotalCostWindow.show();
        }
        
        this.Store.on('load',this.storeloaded,this);
    },
    
    genAddressesSuccessResponse: function (response, request) {
        if (response.success) {
            //create shipping cost calculation window on successful retrieval of addresses
            this.calculateTotalCostWindow = new Wtf.UpsShipmentDetailsWindow({
                title: WtfGlobal.getLocaleText("acc.pickpackship.shipmentDetails"),
                parentCmpId: this.id,
                recordIDForCostCalculation: this.recordIDForCostCalculation,
                start: 0,
                limit: this.pP.combo.value,
                ss: this.quickPanelSearch.getValue(),
                addressesJson: response != undefined ? (response.addressesJson != undefined ? response.addressesJson : {}) : {}
            });
            this.calculateTotalCostWindow.show();
        }
    },
    
    genAddressesFailureResponse: function (response) {
        Wtf.MessageBox.hide();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if (response.msg) {
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    
    hideLoading:function(){
        Wtf.MessageBox.hide();
    },
    loadTypeStore:function(a,rec){
        this.deleted=false;
        this.nondeleted=false;
        this.index=rec.data.typeid;
        this.isfavourite=false;
        this.isUnInvoiced=false;
        if(this.index==1){
            this.isfavourite=true;
            
            if (this.editBttn)
                this.editBttn.disable();

            if (this.deleteTrans)
                this.deleteTrans.disable();
           
            if (this.deleteTransPerm)
                this.deleteTransPerm.disable();       
           
        }else if (this.index==2){
            this.isUnInvoiced = true;

            if (this.editBttn)
                this.editBttn.disable();

            if (this.deleteTrans)
                this.deleteTrans.disable();
           
            if (this.deleteTransPerm)
                this.deleteTransPerm.disable();
        }
        this.Store.on('load',this.storeloaded,this);
        this.loadStore();
        WtfComMsgBox(29,4,true);

    },
    
    enableDisableButtons:function(){
        if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType)){
            if(this.deleteTrans){
                this.deleteTrans.enable();
            }
            if(this.deleteTransPerm){
                this.deleteTransPerm.enable();
            }
        }
        var arr=this.grid.getSelectionModel().getSelections();
        if(arr.length==0&&!WtfGlobal.EnableDisable(this.uPermType,this.removePermType)){
            if(this.deleteTrans){
                this.deleteTrans.disable();
            }
            if(this.deleteTransPerm){
                this.deleteTransPerm.disable();
            }
        }
        if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType)){
            for(var i=0;i<arr.length;arr++){
                if(arr[i]&&arr[i].data.deleted)
                    if(this.deleteTrans){
                        this.deleteTrans.disable();
                    }
                if(this.deleteTransPerm){
                    this.deleteTransPerm.enable();
                }
            }
        }

        var rec = this.sm.getSelected();
        if((this.sm.getCount()==1 && rec.data.deleted != true)){
            if(this.email)this.email.enable();
            if(this.approveInvoiceBttn)this.approveInvoiceBttn.enable();
            if(this.editBttn){
                this.editBttn.enable();
            }
            
            if (this.deleteTransPerm)
                this.deleteTransPerm.enable();
        }else{
            if(this.email)this.email.disable();
            if(this.approveInvoiceBttn)this.approveInvoiceBttn.disable();
            if(this.editBttn){
                this.editBttn.disable();
            }
        }
        if(this.sm.getCount()>=1 ){
            if(this.singlePrint)this.singlePrint.enable();
            if(this.singleRowPrint)this.singleRowPrint.enable();
        }else{
            if(this.singlePrint)this.singlePrint.disable();
            if(this.singleRowPrint)this.singleRowPrint.disable();
        }
        if (this.sm.getCount() == 1) {//If single record is select then enable/disable shipping cost calculation button (UPS Integration)
            if (rec.data.shippedWithUPS) {//If shipment has already been created for the record, then disable cost calculation button (UPS Integration)
                if (this.calculateTotalCostBttn)
                    this.calculateTotalCostBttn.disable();
            } else {
                if (this.calculateTotalCostBttn)
                    this.calculateTotalCostBttn.enable();
            }
        } else {
            if (this.calculateTotalCostBttn)
                this.calculateTotalCostBttn.disable();
        }
    },
    
    starCellClickHandler : function(grid, rowIndex, columnIndex,e){
        var event=e;
        if(event.getTarget('img[class="favourite"]')) {    
            var formrec = grid.getSelectionModel().getSelected();
            var isfavourite = formrec.get('isfavourite');
            if(!formrec.data.deleted && !this.consolidateFlag){
                if(isfavourite){
                    this.markUnFavouriteHandler(formrec);
                }else{
                    this.markFavouriteHandler(formrec);
                }
            }
        }
    },
    
    markFavouriteHandler : function(formrec){
        var url = (this.businessPerson == "Customer")?"ACCInvoice/updateDeliveryOrderFavourite.do":"ACCGoodsReceipt/updateGoodsReceiptOrderFavourite.do";        
        Wtf.Ajax.requestEx({
            url:url,
            params:{
                date: WtfGlobal.convertToGenericDate(formrec.data.date),//used as transaction date
                id:formrec.get('billid'),
                isfavourite:true
            }
        },this,
        function(){
            formrec.set('isfavourite', true);
        },function(){                
            });
    },

    markUnFavouriteHandler : function(formrec){
        var url = (this.businessPerson == "Customer")?"ACCInvoice/updateDeliveryOrderFavourite.do":"ACCGoodsReceipt/updateGoodsReceiptOrderFavourite.do";
        
        Wtf.Ajax.requestEx({
            url:url,
            params:{
                date: WtfGlobal.convertToGenericDate(formrec.data.date),//uswed as transaction date
                id:formrec.get('billid'),
                isfavourite:false
            }
        },this,
        function(){
            if(this.index == 1){
                this.grid.getStore().remove(formrec);
            }else{
                formrec.set('isfavourite', false);
            }
        },function(){
                
            });
    },
    
    loadParmStore:function(){
        this.Store.on('load',this.expandRow, this);
        if(this.invID==null)
            this.Store.load({
                params:{
                    start:0,
                    limit:30
                }
            });
        this.Store.on('datachanged', function() {
            if(this.invID==null){
                var p = this.pP.combo.value;
                this.quickPanelSearch.setPage(p);
            }
        }, this);
        WtfComMsgBox(29,4,true);
    },
    
    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.loadStore();
            this.Store.on('load',this.storeloaded,this);
        }
    },

    genSuccessResponseStat : function(response){
        this.winDO.close();
        Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), response.msg, function(){
            this.Store.reload();
        }, this);
    },
    
    genFailureResponseStat : function(response){
        this.winDO.close();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileupdatingstatus")],2);
    },
    
    storeloaded:function(store){
        this.quickPanelSearch.StorageChanged(store);
        
        if(this.Store.reader.jsonData.upsErrorJSON && this.Store.reader.jsonData.upsErrorJSON.ErrorCode) {
            var errormsg = WtfGlobal.getLocaleText("acc.pickpackship.upsErrorMsg")+": </br><br><b>"+WtfGlobal.getLocaleText("acc.pickpackship.upsErrorCode")+": </b>"+ this.Store.reader.jsonData.upsErrorJSON.ErrorCode;
            if(this.Store.reader.jsonData.upsErrorJSON.ErrorDescription) {
                errormsg = errormsg + "</br><br><b>"+WtfGlobal.getLocaleText("acc.pickpackship.upsErrorDescription")+": </b>"+ this.Store.reader.jsonData.upsErrorJSON.ErrorDescription;
            }
            if(this.Store.reader.jsonData.upsErrorJSON.ErrorSeverity) {
                errormsg = errormsg + "</br><br><b>"+WtfGlobal.getLocaleText("acc.pickpackship.upsErrorSeverity")+": </b>"+ this.Store.reader.jsonData.upsErrorJSON.ErrorSeverity;
            }
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), errormsg],2);
        }
    },
    
    viewTransection:function(grid, rowIndex, columnIndex){
        var formrec=null;
    if(rowIndex<0&&this.grid.getStore().getAt(rowIndex)==undefined ||this.grid.getStore().getAt(rowIndex)==null ){
            WtfComMsgBox(15,2);
            return;
        }
    formrec = this.grid.getStore().getAt(rowIndex);
        var incash=formrec.get("incash");
        this.withInvMode = formrec.get("withoutinventory");
        if(this.isCustomer)
            callViewDeliveryOrder(true,formrec,formrec.data.billid,false,this.isFixedAsset)
        else
            callViewGoodsReceiptDelivery(true,formrec,formrec.data.billid,false,this.isFixedAsset)
    },

    editOrderTransaction:function(){			// Editing Sales and Purchase Order with Inventory and Without Inventory
        var formRecord = null;
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
            WtfComMsgBox(15,2);
            return;
        }
        formRecord = this.grid.getSelectionModel().getSelected();
        var billid=formRecord.get("billid");   
        (this.businessPerson == "Customer")?callEditDeliveryOrder(true,formRecord,billid,false,this.isFixedAsset) : callGoodsReceiptDelivery(true,formRecord,billid,false,this.isFixedAsset)
    },
        
    onRowexpand:function(scope, record, body){
        var colModelArray = GlobalColumnModel[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray, this.expandStore1);
        this.expanderBody=body;
        this.isexpenseinv=!this.isCustomer&&record.data.isexpenseinv;
        this.withInvMode = record.data.withoutinventory;
        this.expandStore1.load({
            params:{
                bills:record.data.billid,
                isexpenseinv:(!this.isCustomer&&record.data.isexpenseinv)
            }
        });
    },
    
    fillExpanderBody:function(){ 
        this.expandStore.removeAll();
        this.expandShipingDoStore.removeAll();
        this.expandItemPackingStore.removeAll();
        this.expandUpsPackageDetailsStore.removeAll();
        for(var j=0;j<this.expandStore1.getCount();j++){
            var rec1=this.expandStore1.getAt(j);
            this.productDetailsArray=rec1.data['packingdolistdetails'];
            var productDetailsLength = 0;
            productDetailsLength = this.productDetailsArray.length;           
            for(var i=0;i<productDetailsLength;i++){              
                var rec=new this.expandProductRec({
                    productname:this.productDetailsArray[i].productname!=undefined?this.productDetailsArray[i].productname:"",
                    productid:this.productDetailsArray[i].productid!=undefined?this.productDetailsArray[i].productid:"",
                    actualquantity:this.productDetailsArray[i].actualquantity!=undefined?this.productDetailsArray[i].actualquantity:"0",
                    deliveredquantity:this.productDetailsArray[i].deliveredquantity!=undefined?this.productDetailsArray[i].deliveredquantity:"0",
                    unitname:this.productDetailsArray[i].unitname!=undefined?this.productDetailsArray[i].unitname:"",
                    type:this.productDetailsArray[i].type!=undefined?this.productDetailsArray[i].type:"",
                    pid:this.productDetailsArray[i].pid!=undefined?this.productDetailsArray[i].pid:"",
                    desc:this.productDetailsArray[i].desc!=undefined?this.productDetailsArray[i].desc:"",
                    description:this.productDetailsArray[i].description!=undefined?this.productDetailsArray[i].description:"",
                    packageNumber:this.productDetailsArray[i].packageNumber!=undefined?this.productDetailsArray[i].packageNumber:"",
                    remark:this.productDetailsArray[i].remark!=undefined?this.productDetailsArray[i].remark:""  
                }); 
                this.expandStore.add(rec);
            }
            
            this.shipingDoDetailsArray=rec1.data['shipingdodetails'];
            var shipingDoDetailsLength = 0;
            shipingDoDetailsLength = this.shipingDoDetailsArray.length;           
            for(var i=0;i<shipingDoDetailsLength;i++){              
                var rec=new this.expandShipingDoRec({
                    productname:this.shipingDoDetailsArray[i].productname!=undefined?this.shipingDoDetailsArray[i].productname:"",
                    productid:this.shipingDoDetailsArray[i].productid!=undefined?this.shipingDoDetailsArray[i].productid:"",
                    quantityindo:this.shipingDoDetailsArray[i].quantityindo!=undefined?this.shipingDoDetailsArray[i].quantityindo:"",
                    shipquantity:this.shipingDoDetailsArray[i].shipquantity!=undefined?this.shipingDoDetailsArray[i].shipquantity:"",
                    unitname:this.shipingDoDetailsArray[i].unitname!=undefined?this.shipingDoDetailsArray[i].unitname:"",
                    duequantity:this.shipingDoDetailsArray[i].duequantity!=undefined?this.shipingDoDetailsArray[i].duequantity:"",
                    pid:this.shipingDoDetailsArray[i].pid!=undefined?this.shipingDoDetailsArray[i].pid:"",
                    desc:this.shipingDoDetailsArray[i].desc!=undefined?this.shipingDoDetailsArray[i].desc:"",
                    description:this.shipingDoDetailsArray[i].description!=undefined?this.shipingDoDetailsArray[i].description:"",
                    remark:this.shipingDoDetailsArray[i].remark!=undefined?this.shipingDoDetailsArray[i].remark:""  ,
                    type:this.shipingDoDetailsArray[i].type!=undefined?this.shipingDoDetailsArray[i].type:""  ,
                    dono:this.shipingDoDetailsArray[i].dono!=undefined?this.shipingDoDetailsArray[i].dono:""  ,
                    actualquantity:this.shipingDoDetailsArray[i].actualquantity!=undefined?this.shipingDoDetailsArray[i].actualquantity:""      
                }); 
                this.expandShipingDoStore.add(rec);
            }

            //If UPS Integration is enabled, add data to this.expandUpsPackageDetailsStore from packingRow response 
            if (Wtf.account.companyAccountPref.upsIntegration) {
                this.upsPackageDetailsArr = rec1.data.upsPackageDetails;
                var upsPackageDetailsLength = 0;
                upsPackageDetailsLength = this.upsPackageDetailsArr.length;
                for (var i = 0; i < upsPackageDetailsLength; i++) {
                    var upsPackageDetailsObj = this.upsPackageDetailsArr[i];
                    var packagingType = "";
                    var packagingTypeCode = upsPackageDetailsObj.packagingType;
                    if (packagingTypeCode != undefined) {
                        for (var k=0; k<Wtf.integration.upsPackagingTypeArr.length; k++) {
                            if (Wtf.integration.upsPackagingTypeArr[k].id == packagingTypeCode){
                                packagingType = Wtf.integration.upsPackagingTypeArr[k].name;
                                break;
                            }
                        }
                    }
                    var deliveryConfirmationType = "";
                    var deliveryConfirmationTypeCode = upsPackageDetailsObj.deliveryConfirmationType;
                    if (deliveryConfirmationTypeCode != undefined) {
                        for (var k=0; k<Wtf.integration.upsDeliveryConfirmationTypeArr.length; k++) {
                            if (Wtf.integration.upsDeliveryConfirmationTypeArr[k].id == deliveryConfirmationTypeCode){
                                deliveryConfirmationType = Wtf.integration.upsDeliveryConfirmationTypeArr[k].name;
                                break;
                            }
                        }
                    }
                    var rec = new this.expandUpsPackageDetailsRec({
                        rowid: upsPackageDetailsObj.rowid != undefined ? upsPackageDetailsObj.rowid : "",
                        billid: upsPackageDetailsObj.billid != undefined ? upsPackageDetailsObj.billid : "",
                        srno: upsPackageDetailsObj.srno != undefined ? upsPackageDetailsObj.srno : "",
                        packageNumber: upsPackageDetailsObj.packageNumber != undefined ? upsPackageDetailsObj.packageNumber : "",
                        packagingType: packagingType,
                        packageWeight: upsPackageDetailsObj.packageWeight != undefined ? upsPackageDetailsObj.packageWeight : "",
                        packageDimensions: upsPackageDetailsObj.packageDimensions != undefined ? upsPackageDetailsObj.packageDimensions : "",
                        declaredValue: (upsPackageDetailsObj.declaredValue != undefined && upsPackageDetailsObj.declaredValue != 0) ? upsPackageDetailsObj.declaredValue : "",
                        deliveryConfirmationType: deliveryConfirmationType,
                        additionalHandling: (upsPackageDetailsObj.additionalHandling != undefined && upsPackageDetailsObj.additionalHandling != "") ? (upsPackageDetailsObj.additionalHandling == "1" ? "Yes" : "No") : "",
                        trackingNumber: upsPackageDetailsObj.trackingNumber != undefined ? upsPackageDetailsObj.trackingNumber : ""
                    });
                    this.expandUpsPackageDetailsStore.add(rec);
                }
            } else {//If UPS Integration is disabled, add data to this.expandItemPackingStore from packingRow response 
                this.itempackingDetailsArray=rec1.data.itempackingdetails;
                var itempackingDetailsLength = 0;
                itempackingDetailsLength = this.itempackingDetailsArray.length;           
                for(var i=0;i<itempackingDetailsLength;i++){
                    var rec=new this.expandItemPackingRec({
                        productname:this.itempackingDetailsArray[i].productname!=undefined?this.itempackingDetailsArray[i].productname:"",
                        productid:this.itempackingDetailsArray[i].productid!=undefined?this.itempackingDetailsArray[i].productid:"",
                        packageid:this.itempackingDetailsArray[i].packageid!=undefined?this.itempackingDetailsArray[i].packageid:"",
                        packagename:this.itempackingDetailsArray[i].packagename!=undefined?this.itempackingDetailsArray[i].packagename:"",
                        packagequantity:this.itempackingDetailsArray[i].packagequantity!=undefined?this.itempackingDetailsArray[i].packagequantity:"",
                        itemperpackage:this.itempackingDetailsArray[i].itemperpackage!=undefined?this.itempackingDetailsArray[i].itemperpackage:"",
                        unitname:this.itempackingDetailsArray[i].unitname!=undefined?this.itempackingDetailsArray[i].unitname:"",
                        type:this.itempackingDetailsArray[i].type!=undefined?this.itempackingDetailsArray[i].type:"",
                        description:this.itempackingDetailsArray[i].description!=undefined?this.itempackingDetailsArray[i].description:"",
                        remark:this.itempackingDetailsArray[i].remark!=undefined?this.itempackingDetailsArray[i].remark:"" , 
                        pid:this.itempackingDetailsArray[i].pid!=undefined?this.itempackingDetailsArray[i].pid:"" , 
                        totalquantity:this.itempackingDetailsArray[i].totalquantity!=undefined?this.itempackingDetailsArray[i].totalquantity:"" , 
                        productweight:this.itempackingDetailsArray[i].productweight!=undefined?this.itempackingDetailsArray[i].productweight:"" , 
                        packageweight:this.itempackingDetailsArray[i].packageweight!=undefined?this.itempackingDetailsArray[i].packageweight:"",
                        netweight:this.itempackingDetailsArray[i].totalquantity!=undefined? this.itempackingDetailsArray[i].productweight!=undefined? this.itempackingDetailsArray[i].totalquantity * this.itempackingDetailsArray[i].productweight : "" : "",
                        grossweight:this.itempackingDetailsArray[i].grossweight!=undefined?this.itempackingDetailsArray[i].grossweight:"",
                        rowid:this.itempackingDetailsArray[i].rowid!=undefined?this.itempackingDetailsArray[i].rowid:"",//rowid is used in request which is sent to download shipping label
                        upstrackingnumber:this.itempackingDetailsArray[i].upstrackingnumber!=undefined?this.itempackingDetailsArray[i].upstrackingnumber:"NA",//UPS tracking number
                        measurement:this.itempackingDetailsArray[i].measurement!=undefined?this.itempackingDetailsArray[i].measurement:""    
                    }); 
                    this.expandItemPackingStore.add(rec);
                }
            }
        }

        var disHtml = "";
        var disHtmlshipingdodetails = "";
        var disHtmlitempackingdetails= "";
        var disHtmlUpsPackageDetails= "";
        var arrproductdetails=[];
        var arrshipingdetails=[];
        var arritempackingdetails=[];
        
        var productTypeText = this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pTypeNonInv") : WtfGlobal.getLocaleText("acc.invoiceList.expand.pType");
        arrproductdetails=[(this.withInvMode?'':WtfGlobal.getLocaleText("acc.invoiceList.expand.PID")),//PID for Inventory
        (this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetailsNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pName")),//Product Details or Product Name
        WtfGlobal.getLocaleText("acc.product.description"),
        WtfGlobal.getLocaleText("acc.invoice.gridUOM"),
        (this.isCustomer?
            productTypeText:(Wtf.account.companyAccountPref.countryid == '203' && !this.isQuotation && !this.isOrder)?
            WtfGlobal.getLocaleText("acc.field.PermitNo."):productTypeText),//Product Type
        WtfGlobal.getLocaleText("erp.QuantityinDOs"),//Quantity
        WtfGlobal.getLocaleText("erp.packingdolist.packingquantity")//Delivered Quantity
        ];
        if (Wtf.account.companyAccountPref.upsIntegration) {
            arrproductdetails.push(WtfGlobal.getLocaleText("acc.pickpackship.packageNumber"));//Package Number (Visible only when UPS integration is enabled)
        }
        arrproductdetails.push(" ");
        
        //Width for expander body for scroll bar (ERP-37879)
        var expanderWidth = (arrproductdetails.length * 140) + 250;
        if(this.grid.el && (this.grid.el.dom.clientWidth || this.grid.el.dom.offsetWidth)){
            expanderWidth =  this.grid.el.dom.clientWidth || this.grid.el.dom.offsetWidth;
        }
        expanderWidth += "px";
        
        var gridHeaderText = this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pListNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pList");
        var header1 = "<span class='gridHeader'>"+gridHeaderText+"</span>";   //Product List
        header1 += "<div style='display:table !important;width:" + expanderWidth + ";'>";
        
        var widthInPercent1 = 100/(arrproductdetails.length + 1);
                
        header1 += "<span class='gridRow' style='font-weight:bold; width: " + widthInPercent1 + "%'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
    
        for(var j=0;j<arrproductdetails.length;j++){
            header1 += "<span class='headerRow' style='width: " + widthInPercent1 + "%'>" + arrproductdetails[j] + "</span>";
        }
        
        header1 += "</div>";
        header1 += "<div style='width:" + expanderWidth + ";'><span class='gridLine'></span></div>";
        
        for(var i=0;i<this.expandStore.getCount();i++){
            header1 += " <div style='width:" + expanderWidth + ";display:table !important;height: 22px;'>";
            var rec=this.expandStore.getAt(i);
            var productname=this.withInvMode?rec.data['productdetail']: rec.data['productname'];
            var description= rec.data['description'];
            var packageNumber= rec.data['packageNumber'];

            //Column : S.No.
            header1 += "<span class='gridRow' style='width: " + widthInPercent1 + "%'>"+(i+1)+".</span>";
                   
            //Column : Product Id for Inventory
            var pid=rec.data['pid'];
            header1 += "<span class='gridRow'  wtf:qtip='"+pid+"' style='width: " + widthInPercent1 + "%'>"+Wtf.util.Format.ellipsis(pid,15)+"</span>";
        
            //Column : Product Name
            header1 += "<span class='gridRow' style='width: " + widthInPercent1 + "%'  wtf:qtip='"+productname+"'>"+Wtf.util.Format.ellipsis(productname,15)+"</span>";
        
            //Column : Product Description
            if(description==''){
                header1 += "<span class='gridRow' style='width: " + widthInPercent1 + "%'>&nbsp;</span>";
            }else{
                header1 += "<span class='gridRow' style='width: " + widthInPercent1 + "%'  wtf:qtip='"+description+"'>"+Wtf.util.Format.ellipsis(description,15)+"</span>";
            }
            
            //Column : Product UOM
            header1 += "<span class='gridRow' style='width: " + widthInPercent1 + "%' >"+rec.data['unitname']+"</span>";
        
            if(!this.isCustomer && !this.isQuotation && !this.isOrder && Wtf.account.companyAccountPref.countryid == '203'){
                header1 += "<span class='gridRow' style='width: " + widthInPercent1 + "%'>"+rec.data['permit']+"&nbsp;</span>";
            }else if(!this.withInvMode){
                var type = "";
                type = rec.data['type']
                header1 += "<span class='gridRow' wtf:qtip='"+type+"' style='width: " + widthInPercent1 + "%'>"+Wtf.util.Format.ellipsis(type,15)+"</span>";
            }else {
                header1 += "<span class='gridRow' style='width: " + widthInPercent1 + "%'>&nbsp;</span>";
            }
                
            //Quantity In DO
            header1 += "<span class='gridRow' style='width: " + widthInPercent1 + "%'>"+rec.data['actualquantity']+" "+rec.data['unitname']+"</span>";
                
            //Ship Quantity
            header1 += "<span class='gridRow' style='width: " + widthInPercent1 + "%'>"+rec.data['deliveredquantity']+" "+rec.data['unitname']+"</span>";
            
            if (Wtf.account.companyAccountPref.upsIntegration) {
                //Package Number (Visible only when UPS integration is enabled)
                if(packageNumber==''){
                    header1 += "<span class='gridRow' style='width: " + widthInPercent1 + "%'>&nbsp;</span>";
                }else{
                    header1 += "<span class='gridRow' style='width: " + widthInPercent1 + "%'  wtf:qtip='"+packageNumber+"'>"+Wtf.util.Format.ellipsis(packageNumber,15)+"</span>";
                }
            }
            
            header1 += "</div>";
        }
            
        disHtml += "<div class='expanderContainer' style='width:95%;overflow:auto;'>" + header1 + "</div>";
 
        arrshipingdetails=[WtfGlobal.getLocaleText("erp.DONumber"),//DO Number
        WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),//Product Name
        WtfGlobal.getLocaleText("acc.product.description"),//Description
        WtfGlobal.getLocaleText("acc.invoice.gridUOM"),//UOM
        WtfGlobal.getLocaleText("erp.ActualQuantityinDO"),//Actual Quantity In Do
        WtfGlobal.getLocaleText("erp.DueQuantityforPacking"),// Due Quantity For Packing
        WtfGlobal.getLocaleText("erp.packingdolist.packingquantity"), // Shipped quantity
        " "];
           
        var gridHeaderText1 =WtfGlobal.getLocaleText("erp.PackingDODetails");
        var header2 = "<span class='gridHeader'>"+gridHeaderText1+"</span>";   //Shiping DO's Details
        header2 += "<div style='display:table !important;width:" + expanderWidth + ";'>";
        
        header2 += "<span class='gridRow' style='font-weight:bold; width: 12%'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
           
        for(var j=0;j<arrshipingdetails.length;j++){
            header2 += "<span class='headerRow' style='width:12%' wtf:qtip='"+arrshipingdetails[j]+"'>" +Wtf.util.Format.ellipsis(arrshipingdetails[j],20)  + "</span>";
        }
        
        header2 += "</div>";
        header2 += "<div style='width:" + expanderWidth + ";'><span class='gridLine'></span></div>";
        
        for(i=0;i<this.expandShipingDoStore.getCount();i++){
            header2 += " <div style='width:" + expanderWidth + ";display:table !important;height: 22px;'>";
            var rec=this.expandShipingDoStore.getAt(i);
            var productname=this.withInvMode?rec.data['productdetail']: rec.data['productname'];
            var description= rec.data['description'];
            //Column : S.No.
            header2 += "<span class='gridRow' style='width:12%'>"+(i+1)+".</span>";
            
            //Column : DO Number
            header2 += "<span class='gridRow'  wtf:qtip='' style='width:12%'>"+rec.data['dono']+"</span>";
        
            //Column : Product Name
            header2 += "<span class='gridRow' style='width:12%'  wtf:qtip='"+productname+"'>"+Wtf.util.Format.ellipsis(productname,15)+"</span>";
        
            //Column : Product Description
            if(description==''){
                header2 += "<span class='gridRow' style='width:12%'>&nbsp;</span>";
            }else{
                header2 += "<span class='gridRow' style='width:12%'  wtf:qtip='"+description+"'>"+Wtf.util.Format.ellipsis(description,15)+"</span>";
            }
        
            //Column : Product UOM
            if(rec.data['unitname']==''){
                header2 += "<span class='gridRow' style='width:12%'>&nbsp;</span>";
            }else{
                header2 += "<span class='gridRow' style='width:12%' >"+rec.data['unitname']+"</span>"; 
            }
                
            // Actual Quantity In Do
            header2 += "<span class='gridRow' style='width:12%'>"+rec.data['actualquantity']+" "+rec.data['unitname']+"</span>";
                
            //Due Quantity For Shiping
            header2 += "<span class='gridRow' style='width:12%'>"+rec.data['duequantity']+" "+rec.data['unitname']+"</span>"; 
        
            //Ship quantity
            header2 += "<span class='gridRow' style='width:12%'>"+rec.data['shipquantity']+" "+rec.data['unitname']+"</span>";
            
            header2 += "</div>";
        }
            
        disHtmlshipingdodetails += "<div class='expanderContainer' style='width:95%;overflow:auto;'>" + header2 + "</div>";
        
        var header3 = "";
        //If UPS Integration is enabled, create and show UPS Shipment Details in expander.
        //Else, show Packing Item Details.
        //Packing Item Details are not created and saved when UPS Integration is enabled. Thus there is nothing to show under Item Details.
        if (Wtf.account.companyAccountPref.upsIntegration) {
            var upaPackageDetailsRowCount = this.expandUpsPackageDetailsStore.getCount();
            //Add UPS Shipment Details only if shipment has been created on UPS side i.e. only if package details exists in database.
            //Else, show only Packing Details and Packing DO details
            if (upaPackageDetailsRowCount > 0) {
                var upsPackageDetailsColArr=[WtfGlobal.getLocaleText("acc.pickpackship.packageNumber"),//Package Number
                    WtfGlobal.getLocaleText("acc.pickpackship.upsPackagingType"),//Packaging Type
                    WtfGlobal.getLocaleText("erp.PackageWeightinLbs"),//Package Weight (LBS)
                    WtfGlobal.getLocaleText("acc.pickpackship.packageDimensions"),//Package Dimensions (IN*IN*IN)
                    WtfGlobal.getLocaleText("acc.pickpackship.declaredValue"),//Declared Value (USD)
                    WtfGlobal.getLocaleText("acc.pickpackship.deliveryConfirmation"),//Delivery Confirmation
                    WtfGlobal.getLocaleText("acc.pickpackship.upsAdditionalHandling"),//Additional Handling Required
                    WtfGlobal.getLocaleText("acc.invoiceList.upsTrackingNumber"),//UPS Tracking Number
                    WtfGlobal.getLocaleText("acc.invoiceList.downloadShippingLabel"),//Download Label
                " "];

                header3 += "<span class='gridHeader'>"+WtfGlobal.getLocaleText("acc.pickpackship.ups") + " " + WtfGlobal.getLocaleText("acc.pickpackship.shipmentDetails") +"</span>";//UPS Shipment Details
                header3 += "<div style='display:table !important;width:" + expanderWidth + ";'>";

                var widthInPercent = 100/(upsPackageDetailsColArr.length + 1);//Width of Packing Item details headers and rows
                var displayCharLength = 18;//Number of characters to show when value is too long (Used in ellipsis function call)

                header3 += "<span class='gridRow' style='font-weight:bold; width: " + widthInPercent +"%'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";

                for(var j=0;j<upsPackageDetailsColArr.length;j++){//Add headers of columns
                    header3 += "<span class='headerRow' style='width: "+widthInPercent+"%' wtf:qtip='"+upsPackageDetailsColArr[j]+"'>" + Wtf.util.Format.ellipsis(upsPackageDetailsColArr[j],displayCharLength) + "</span>";
                }
                
                header3 += "</div>";
                header3 += "<div style='width:" + expanderWidth + ";'><span class='gridLine'></span></div>";

                for(i=0;i<upaPackageDetailsRowCount;i++){//Add data from this.expandUpsPackageDetailsStore
                    header3 += " <div style='width:" + expanderWidth + ";display:table !important;height: 22px;'>";
                    var upsPackagedetailsRec=this.expandUpsPackageDetailsStore.getAt(i);

                    //Column : S.No.
                    var srno = upsPackagedetailsRec.data['srno'];
                    header3 += "<span class='gridRow' style='width: "+widthInPercent+"%'>"+srno+".</span>";

                    //Column : Package Number
                    var packageNumber = upsPackagedetailsRec.data['packageNumber'];
                    if (packageNumber == "") {
                        header3 += "<span class='gridRow' style='width: "+widthInPercent+"%'>&nbsp;</span>";
                    } else {
                        header3 += "<span class='gridRow' style='width: "+widthInPercent+"%'  wtf:qtip='"+packageNumber+"'>"+Wtf.util.Format.ellipsis(packageNumber,displayCharLength)+"</span>";
                    }

                    //Column : Packaging Type
                    var packagingType = upsPackagedetailsRec.data['packagingType'];
                    header3 += "<span class='gridRow' style='width: "+widthInPercent+"%'  wtf:qtip='"+packagingType+"'>"+Wtf.util.Format.ellipsis(packagingType,displayCharLength)+"</span>";

                   //Column : Weight
                    var packageWeight = upsPackagedetailsRec.data['packageWeight'] + " " + WtfGlobal.getLocaleText("acc.field.weightUnitLbs");
                    header3 += "<span class='gridRow' style='width: "+widthInPercent+"%'  wtf:qtip='"+packageWeight+"'>"+Wtf.util.Format.ellipsis(packageWeight,displayCharLength)+"</span>";

                    //Column : Dimensions
                    var packageDimensions = upsPackagedetailsRec.data['packageDimensions'] + " " + WtfGlobal.getLocaleText("acc.field.cubicInch");
                    header3 += "<span class='gridRow' style='width: "+widthInPercent+"%'  wtf:qtip='"+packageDimensions+"'>"+Wtf.util.Format.ellipsis(packageDimensions,displayCharLength)+"</span>";

                    //Column : Declared Value
                    var declaredValue = upsPackagedetailsRec.data['declaredValue'] != "" ? "USD " + upsPackagedetailsRec.data['declaredValue'] : "";
                    if (declaredValue == "") {
                        header3 += "<span class='gridRow' style='width: "+widthInPercent+"%'>&nbsp;</span>";
                    } else {
                        header3 += "<span class='gridRow' style='width: "+widthInPercent+"%'  wtf:qtip='"+declaredValue+"'>"+Wtf.util.Format.ellipsis(declaredValue,displayCharLength)+"</span>";
                    }

                    //Column : Delivery Confirmation Type
                    var deliveryConfirmationType = upsPackagedetailsRec.data['deliveryConfirmationType'];
                    header3 += "<span class='gridRow' style='width: "+widthInPercent+"%'  wtf:qtip='"+deliveryConfirmationType+"'>"+Wtf.util.Format.ellipsis(deliveryConfirmationType,displayCharLength)+"</span>";

                    // Column : Additional Handling
                    var additionalHandling = upsPackagedetailsRec.data['additionalHandling'];
                    if (additionalHandling == "") {
                        header3 += "<span class='gridRow' style='width: "+widthInPercent+"%'>&nbsp;</span>";
                    } else {
                        header3 += "<span class='gridRow' style='width: "+widthInPercent+"%'  wtf:qtip='"+additionalHandling+"'>"+Wtf.util.Format.ellipsis(additionalHandling,displayCharLength)+"</span>";
                    }

                    //Column : Tracking Number
                    var trackingNumber = upsPackagedetailsRec.data['trackingNumber'];
                    header3 += "<span class='gridRow' style='width: "+widthInPercent+"%'  wtf:qtip='"+trackingNumber+"'>"+Wtf.util.Format.ellipsis(trackingNumber,displayCharLength)+"</span>";

                    //Column : Download Label
                    var downloadURL = "ACCInvoiceCMN/downloadLabel.do?recordIDForLabelPrinting=" + upsPackagedetailsRec.data['rowid'] + "&trackingNumber=" + upsPackagedetailsRec.data['trackingNumber'];//URL to be hit when Download Label button is pressed
                    var labelDownloadBttnName = WtfGlobal.getLocaleText("acc.invoiceList.downloadShippingLabel");//Tooltip for Download Label button
                    header3 += "<span class='gridRow' style='text-align:center; width: " + widthInPercent + "%'><div class='jumplink'><img wtf:qtip='" + labelDownloadBttnName + "' src='../../images/down.png' style='cursor: pointer;' width='12px' href='#' onClick='javascript:WtfGlobal.getDownloadFrame(\"" + downloadURL + "\")'></div></span>";

                    header3 += "</div>";
                }
                disHtmlUpsPackageDetails += "<div class='expanderContainer' style='width:95%;overflow:auto;'>" + header3 + "</div>";
                
                this.expanderBody.innerHTML = disHtml+"<br><br><br>"+ disHtmlshipingdodetails+"<br><br><br>"+disHtmlUpsPackageDetails+"<br>";  
                
            } else {
                this.expanderBody.innerHTML = disHtml+"<br><br><br>"+ disHtmlshipingdodetails+"<br>";  
            }
        } else {//Create and add Packing Item Details to expander
            var packingItemDetailsRowCount = this.expandItemPackingStore.getCount();
            //Add Packing Item Details only if number of rows is more that zero
            if (packingItemDetailsRowCount > 0) {
                arritempackingdetails=[WtfGlobal.getLocaleText("erp.Package"),//Package
                WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),//Product Name
                WtfGlobal.getLocaleText("erp.PackageWeightinKg"),//Package Weight
                WtfGlobal.getLocaleText("erp.PackageMeasurementlenghtweidthheight"),//Package Measurement
                WtfGlobal.getLocaleText("acc.erp.ProductWeight"),//Product Weight
                WtfGlobal.getLocaleText("erp.NoOfPackage"),//No of Package
                WtfGlobal.getLocaleText("erp.QuantityPerPackage"),//Quantity Per Package
                WtfGlobal.getLocaleText("erp.TotalPackageQuantity"),// Total Quantity
                WtfGlobal.getLocaleText("erp.NetWeight"), // Net Weight
                WtfGlobal.getLocaleText("erp.GrossWeight"), // Gross Weight
                " "];

                var gridHeaderText2=WtfGlobal.getLocaleText("erp.itempackingdetails");
                header3 += "<span class='gridHeader'>"+gridHeaderText2+"</span>";   //Product List
                header3 += "<div style='display:table !important;width:" + expanderWidth + ";'>";

                var widthInPercent = 100/(arritempackingdetails.length + 1);//Width of Packing Item details headers and rows

                header3 += "<span class='gridRow' style='font-weight:bold; width: " + widthInPercent +"%'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";

                for(var j=0;j<arritempackingdetails.length;j++){
                    header3 += "<span class='headerRow' style='width: "+widthInPercent+"%' wtf:qtip='"+arritempackingdetails[j]+"'>" + Wtf.util.Format.ellipsis(arritempackingdetails[j],15) + "</span>";
                }
                
                header3 += "</div>";
                header3 += "<div style='width:" + expanderWidth + ";'><span class='gridLine'></span></div>";

                for(i=0;i<packingItemDetailsRowCount;i++){
                    header3 += " <div style='width:" + expanderWidth + ";display:table !important;height: 22px;'>";
                    var packedrec=this.expandItemPackingStore.getAt(i);
                    var productname=this.withInvMode?packedrec.data['productdetail']: packedrec.data['productname'];
                    var description= packedrec.data['description'];

                    //Column : S.No.
                    header3 += "<span class='gridRow' style='width: " + widthInPercent +"%'>"+(i+1)+".</span>";

                    //Column : DO Number
                    header3 += "<span class='gridRow'  wtf:qtip='' style='width: "+widthInPercent+"%'>"+packedrec.data['packagename']+"</span>";

                    //Column : Product Name
                    header3 += "<span class='gridRow' style='width: "+widthInPercent+"%'  wtf:qtip='"+productname+"'>"+Wtf.util.Format.ellipsis(productname,15)+"</span>";

                   //Column : Package Weight
                    header3 += "<span class='gridRow' style='width: "+widthInPercent+"%'  wtf:qtip=''>"+packedrec.data['packageweight']+" "+WtfGlobal.getLocaleText("acc.field.weighingunit")+"</span>";

                    //Package Measurement
                    header3 += "<span class='gridRow' style='width: "+widthInPercent+"%'>"+packedrec.data['measurement']+"</span>";

                    //Column : Product Weight
                    header3 += "<span class='gridRow' style='width: "+widthInPercent+"%'  wtf:qtip=''>"+packedrec.data['productweight']+" "+WtfGlobal.getLocaleText("acc.field.weighingunit")+"</span>";

                    //Column : Package Quantity

                    header3 += "<span class='gridRow' wtf:qtip='' style='width: "+widthInPercent+"%'>"+packedrec.data['packagequantity']+" Packages "+"</span>";

                    // Package Per Quantity
                    header3 += "<span class='gridRow' style='width: "+widthInPercent+"%'>"+packedrec.data['itemperpackage']+" "+packedrec.data['unitname']+"</span>";

                    //Total Items
                    header3 += "<span class='gridRow' style='width: "+widthInPercent+"%'>"+packedrec.data['totalquantity']+" "+packedrec.data['unitname']+"</span>";

                    //Net Weight
                    header3 += "<span class='gridRow' style='width: "+widthInPercent+"%'>"+packedrec.data['netweight'].toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+" "+WtfGlobal.getLocaleText("acc.field.weighingunit")+"</span>";

                    //Gross Weight
                    header3 += "<span class='gridRow' style='width: "+widthInPercent+"%'>"+packedrec.data['grossweight']+" "+WtfGlobal.getLocaleText("acc.field.weighingunit")+"</span>";

                    header3 += "</div>";
                }
                disHtmlitempackingdetails += "<div class='expanderContainer' style='width:95%;overflow:auto;'>" + header3 + "</div>";

                this.expanderBody.innerHTML = disHtml+"<br><br><br>"+ disHtmlshipingdodetails+"<br><br><br>"+disHtmlitempackingdetails+"<br>";
                
            }  else {
                this.expanderBody.innerHTML = disHtml+"<br><br><br>"+ disHtmlshipingdodetails+"<br>";  
            }
        }
    },

    onCellClick:function(g,i,j,e){
        this.starCellClickHandler(g,i,j,e);
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="entryno"){
            var accid=this.Store.getAt(i).data['journalentryid'];
            this.fireEvent('journalentry',accid,true);
        }
        if(header=="billno"){
            this.viewTransection(g,i,e)
        }
    },
    
    expandRow:function(){
        if(this.Store.getCount()==0){
            if(this.exportButton)this.exportButton.disable();
            if(this.printButton)this.printButton.disable();
            var emptyTxt = "";

        }else{
            if(this.exportButton)this.exportButton.enable();
            if(this.printButton)this.printButton.enable();
        }
        this.Store.filter('billid',this.invID);
    },
     
    loadStore:function(){
        this.Store.load({
            params : {
                start : 0,
                limit : this.pP.combo.value,
                ss : this.quickPanelSearch.getValue()
            }
        });
        this.Store.on('load',this.storeloaded,this);
    },
    
    handleDelete:function(del){
        var delFlag=del;
        if(this.grid.getSelectionModel().hasSelection()==false){
            WtfComMsgBox(34,2);
            return;
        }
        var data=[];
        var arr=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.withInvMode = this.recArr[0].data.withoutinventory;
        this.grid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.rem.146")+" "+this.label+"?",function(btn){
            if(btn!="yes") {
                for(var i=0;i<this.recArr.length;i++){
                    var ind=this.Store.indexOf(this.recArr[i])
                    var num= ind%2;
                    WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
                }
                return;
            }
            for(i=0;i<this.recArr.length;i++){
                arr.push(this.Store.indexOf(this.recArr[i]));
            }
            var mode=(this.withInvMode?23:15);
            if(this.isOrder){
                mode=(this.withInvMode?54:44);
            }
            data= WtfGlobal.getJSONArray(this.grid,true,arr);
            if(this.businessPerson=="Customer"){
                if(delFlag=='delp' ){
                    this.ajxUrl = "ACCInvoice/deletePackingPermanent.do";  
                }
            } 
            Wtf.Ajax.requestEx({
                url:this.ajxUrl,
                params:{
                    data:data,
                    mode:mode
                }
            },this,this.genSuccessResponse,this.genFailureResponse);
        },this);
    },
    
    genSuccessResponse:function(response){
        WtfComMsgBox([this.label,response.msg],response.success*2+1);
        for(var i=0;i<this.recArr.length;i++){
            var ind=this.Store.indexOf(this.recArr[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        if(response.success){
            (function(){
                this.loadStore();
            }).defer(WtfGlobal.gridReloadDelay(),this);
            Wtf.productStore.reload();
            Wtf.productStoreSales.reload();
        }
    },
    
    genFailureResponse:function(response){
        for(var i=0;i<this.recArr.length;i++){
            var ind=this.Store.indexOf(this.recArr[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.doLayout();
    },
    
    filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.Store.baseParams = {
            pendingapproval:this.pendingapproval,
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleid,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.Store.load({
            params: {
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pP.combo.value
            }
        });
    },
    
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.baseParams = {
            pendingapproval:this.pendingapproval,
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleid,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.Store.load({
            params: {
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pP.combo.value
            }
        });
        this.objsearchComponent.hide();
        this.doLayout();
    }
});