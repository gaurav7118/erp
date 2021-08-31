
Wtf.account.ShippingDeliveryorder=function(config){	
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
        },]
    });
    Wtf.account.ShippingDeliveryorder.superclass.constructor.call(this,config);
    this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.ShippingDeliveryorder,Wtf.account.ClosablePanel,{
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
            this.incoterms.setValue(data.fob);
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
        Wtf.account.ShippingDeliveryorder.superclass.onRender.call(this, config);
        this.initForClose();   
        if( this.isEdit ){
            this.isClosable=false          // Set Closable flag for edit and copy case
        }
        this.hideFormFields();
    },
    //////////////////////////////////////////////////////////////////
    hideFormFields: function () {
        this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.shipform);

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
                    if(fieldArray.fieldId=="ShowOnlyOneTime" && ((this.isEdit !=undefined ?this.isEdit:false) || (this.copyInv !=undefined ?this.copyInv:false) || (this.isCopyFromTemplate !=undefined ?this.isCopyFromTemplate:false) || (this.isTemplate !=undefined ?this.isTemplate:false))){
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
                            if(fieldArray.isManadatoryField && fieldArray.isFormField )
                                Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel=fieldArray.fieldLabelText +"*";
                            else
                                Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel=fieldArray.fieldLabelText;
                        }
                  }
                }
            }
            
            
            
            
        }
    },
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    
    initComponent:function(config){
        Wtf.account.ShippingDeliveryorder.superclass.initComponent.call(this,config);

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
        
        this.dateOfLc= new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.DateOfLC"),
            id:"dateOfLc"+this.heplmodeid+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'dateoflc',
            anchor:'80%'
//            allowBlank:false
        });    
    
        this.letterOfCreditNote=new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.LetterOfCreditNote"), 
            name: 'letterofcn',
            id:"letterofcn"+this.heplmodeid+this.id,
            anchor:'80%',
            maxLength:50,
            scope:this
//            allowBlank:this.checkin
        });
        
        this.partialShipment=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.PartialShipment"), 
            name: 'partialshipment',
            id:"partialshipment"+this.heplmodeid+this.id,
            anchor:'80%',
            maxLength:50,
            scope:this
//            allowBlank:this.checkin
        });
        
        this.transhipment=new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Transhipment"), 
            name: 'transhipment',
            id:"transhipment"+this.heplmodeid+this.id,
            anchor:'80%',
            maxLength:50,
            scope:this
//            allowBlank:this.checkin
        });
        
        this.portOfLoading=new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.filed.PortOfLoading"), 
            name: 'portofloading',
            id:"portofloading"+this.heplmodeid+this.id,
            anchor:'80%',
            maxLength:50,
            scope:this
//            allowBlank:this.checkin
        });
        
        this.portOfDischarge=new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.PortOfDischarge"), 
            name: 'portofdischarge',
            id:"portofdischarge"+this.heplmodeid+this.id,
            anchor:'80%',
            maxLength:50,
            scope:this
//            allowBlank:this.checkin
        });
        
        this.vessel=new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Vessel"), 
            name: 'vessel',
            id:"vessel"+this.heplmodeid+this.id,
            anchor:'80%',
            maxLength:50,
            scope:this
//            allowBlank:this.checkin
        });
        
        this.incoterms=new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.INCOTERMS"), 
            name: 'incoterms',
            id:"incoterms"+this.heplmodeid+this.id,
            anchor:'80%',
            maxLength:50,
            scope:this
//            allowBlank:this.checkin
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
                mode:'autoshippingdo'
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
            fieldLabel: 'Shipping Number', //Packing Number
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
            fieldLabel: 'Shipping Date',
            id:"packingDate"+this.heplmodeid+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'packingdate',
            anchor:'80%',
            allowBlank:false
        });
    
        var itemArr={};
        itemArr = [this.Name,this.sequenceFormatCombobox,this.Number,this.billDate,this.portOfLoading,this.portOfDischarge,this.vessel,this.incoterms];
        
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
                        items:[this.dateOfLc,this.letterOfCreditNote,this.DOStatusCombo,this.Memo,this.partialShipment,this.transhipment]
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
    setDefaultStatus: function(){
       var value="Shipped";
            var masterstatus = WtfGlobal.searchRecord(Wtf.DOStatusStore,value, 'name');
            if(masterstatus!=undefined && masterstatus!=null){
                var defaultstatus=masterstatus.data.id;
                this.DOStatusCombo.setValue(defaultstatus);
            }
            if(Wtf.account.companyAccountPref.pickpackship && this.moduleid == Wtf.Acc_Shipping_ModuleId){
                    this.DOStatusCombo.disable();
            }     
    },
    addDOStatus: function(){
        (this.businessPerson == "Customer")? addMasterItemWindow('10') : addMasterItemWindow('11');
    },
    
    onDateChange:function(a,val,oldval){
        this.val=val;
        this.oldval=oldval; 
    },
    hideLoading:function(){
        Wtf.MessageBox.hide();
    },
    
    addGrid:function(){
        this.Grid=new Wtf.account.PackingDoListGrid({
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
            this.callPackageDetailsWindow(i);
        }
    },
    
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".doDetails-gridrow")){
            this.callDoDetailsWindow(rowindex);
        }
        if(e.getTarget(".packingDetails-gridrow")){
             this.callPackageDetailsWindow(rowindex);
        }   
        if(e.getTarget(".serialNo-gridrow")){
            this.callSerialDetailsWindow(rowindex)
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
//                packingDOList:true,
                shippingDOList:true
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
            if (Wtf.account.companyAccountPref.pickpackship && this.DOStatusCombo.getValue() == "") {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.deliveryorder.shipisnotset")]);
                return;
            }
            for(var i=0;i<this.Grid.getStore().getCount();i++){
                var quantityindo=this.Grid.getStore().getAt(i).data['dquantity'];
                var shipquantity=this.Grid.getStore().getAt(i).data['shipquantity'];
                if(shipquantity == '' || shipquantity == undefined || shipquantity<=0){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.YouhavenotsettheshipQuantity")+" "+this.Grid.getStore().getAt(i).data['productname']], 2);
                } 
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
                this.ajxurl = "ACCInvoice/saveShippingDeliveryOrder.do";            
            } 
            var detail = this.Grid.getProductDetails();
            if(detail == undefined || detail == "[]"){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg12")],2);   //"Product(s) details are not valid."
                return;
            }
            var prodLength=this.Grid.getStore().data.items.length;
            for(var i=0;i<prodLength;i++){ 
                var prodID=this.Grid.getStore().getAt(i).data['productid'];
                var doquantity=this.Grid.getStore().getAt(i).data['dquantity'];
                var baseuomrate=this.Grid.getStore().getAt(i).data['baseuomrate'];
                if(doquantity<this.Grid.getStore().getAt(i).data['shipquantity']){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.YouhaveentershipQuantityforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getlocaleText("erp.greaterthanthequantitiesavailableinDO")], 2); 
                    return;
                }
                var dodetails=this.Grid.getStore().getAt(i).data['packingdodetails'];
                var doDetailsLength = 0;
                if(dodetails == undefined || dodetails == ""){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("erp.PleaseFilltheShipQuantitiesasperDOinDODetails")],2);   //Please Fill the Ship Quantities as per DO in DO Details Link
                    return;
                }
                if(dodetails && dodetails !=''){
                    this.doDetailsArr = eval('(' + dodetails + ')');
                }
                if(this.doDetailsArr.length>0){
                    doDetailsLength = this.doDetailsArr.length;
                }
                var shipquantity=0;
                for(var j=0;j<doDetailsLength;j++){
                    if(parseInt(this.doDetailsArr[j].quantityindo * baseuomrate)<parseInt(this.doDetailsArr[j].shipquantity)){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.YouhaveentershipQuantityforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getlocaleText("erp.greaterthanthequantitiesavailableinDO")], 2); 
                        return;
                    }
                    if(this.doDetailsArr[j].shipquantity == undefined || this.doDetailsArr[j].shipquantity==""){
                        this.doDetailsArr[j].shipquantity=0;
                    }
                    shipquantity=shipquantity+parseInt(this.doDetailsArr[j].shipquantity);   
                }
                if(this.Grid.getStore().getAt(i).data['shipquantity'] * baseuomrate!=shipquantity){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.YouhaveentershipQuantityforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("erp.isnotsameasthatofavailablequantityinDOs")], 2); 
                    return;
                }

                var packingdetails=this.Grid.getStore().getAt(i).data['batchdetails'];
                                var packingDetailsLength = 0;
                if(packingdetails && packingdetails !=''){
                    this.packingDetailsArr = eval('(' + packingdetails + ')');
                }
                if(this.packingDetailsArr.length>0){
                    packingDetailsLength = this.packingDetailsArr.length;
                }
                                                var packedshipquantity=0;
                for(var k=0;k<packingDetailsLength;k++){
                    packedshipquantity=packedshipquantity+(parseInt(this.packingDetailsArr[k].quantity));
                }
                                if(this.Grid.getStore().getAt(i).data['shipquantity'] * baseuomrate!=packedshipquantity)
                {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.bsdetail")],2);   //Please Fill the Ship Quantities as per DO in DO Details Link
                    return;
                }
//                if(packingdetails == undefined || packingdetails == ""){
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.bsdetail")],2);   //Please Fill the Ship Quantities as per DO in DO Details Link
//                    return;
//                }
//                var packingDetailsLength = 0;
//                if(packingdetails && packingdetails !=''){
//                    this.packingDetailsArr = eval('(' + packingdetails + ')');
//                }
//                if(this.packingDetailsArr.length>0){
//                    packingDetailsLength = this.packingDetailsArr.length;
//                }
//                var packedshipquantity=0;
//                for(var k=0;k<packingDetailsLength;k++){
//                    packedshipquantity=packedshipquantity+(parseInt(this.packingDetailsArr[k].packagequantity)*parseInt(this.packingDetailsArr[k].packageperquantity));
//                }      
//                if(this.Grid.getStore().getAt(i).data['shipquantity']!=packedshipquantity)
//                {
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.YouhavesettheshipQuantityforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("erp.isnotsameasthatofavailablequantityinDOs")], 2); 
//                    return;
//                }
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
            rec.dateoflc=WtfGlobal.convertToGenericDate(this.dateOfLc.getValue());
            rec.letterofcn=this.letterOfCreditNote.getValue();
            rec.partialshipment=this.partialShipment.getValue();
            rec.transhipment=this.transhipment.getValue();
            rec.portofloading=this.portOfLoading.getValue();
            rec.portofdischarge=this.portOfDischarge.getValue();
            rec.vessel=this.vessel.getValue();
            rec.incoterms=this.incoterms.getValue();
            rec.billids = this.billids;
            Wtf.Ajax.requestEx({
                url:this.ajxurl,
                params: rec                    
            },this,this.genSuccessResponse,this.genFailureResponse);
        },this);
    },
    
    genSuccessResponse:function(response, request){
        if(response.success){  
            WtfComMsgBox([this.titlel, response.msg], response.success * 2 + 1);
            var rec=this.NorthForm.getForm().getValues();       
            this.Grid.getStore().removeAll();
            this.NorthForm.getForm().reset();
            this.Grid.updateRow(null);
            this.fireEvent('update',this);
            this.isClosable= true;       //Reset Closable flag to avoid unsaved Message.
            this.ownerCt.remove(this);
        } else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 2);
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
    
    callSerialDetailsWindow:function(j){
            var index=this.Grid.getStore().findBy(function(rec){
            if(rec.data.productid)
                return true;
            else
                return false;
        })
        if(index!=-1){             
            var prorec=this.Grid.getStore().getAt(j);
            try {
                var batchdetails = JSON.parse(prorec.data.batchdetails);
                var shipqty = prorec.data.shipquantity * (prorec.data.baseuomrate);//this code in to set the entered shipquantity is the shipping batchdetails window
//                batchdetails[0].quantity = shipqty == "" ? "0" : shipqty;
                batchdetails = JSON.stringify(batchdetails);
            }
            catch (e) {
                batchdetails = prorec.data.batchdetails;
            }
            
            this.batchDetailswin=new Wtf.account.SerialNoAutopopulateWindow({
                    renderTo: document.body,
                    title:WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber"),
                    productName:prorec.data.productname,
                    uomName:prorec.data.productunitname,
                    //quantity:obj.data.dquantity,
                    quantity:prorec.data.shipquantity * prorec.data.baseuomrate,
                    shipquantity:prorec.data.shipquantity * prorec.data.baseuomrate,
                    billid:prorec.data.billid,
                    defaultLocation:prorec.data.location,
                    productid:prorec.data.productid,
                    isSales:this.isCustomer,
                    isShippingDO:true,
//                    isLinkedFromSO:isLinkedFromSO,
//                    isLinkedFromCI:isLinkedFromCI,
                    moduleid:this.moduleid,
                    transactionid:this.moduleid,
                    isDO:this.isCustomer?true:false,
                    documentid:(this.isEdit)?prorec.data.rowid:"",
                    defaultWarehouse:prorec.data.warehouse,
                    defaultAvailbaleQty:this.AvailableQuantity,
                    batchDetails:batchdetails,
                    warrantyperiod:prorec.data.warrantyperiod,
                    warrantyperiodsal:prorec.data.warrantyperiodsal,  
                    isLocationForProduct:prorec.data.isLocationForProduct,
                    isWarehouseForProduct:prorec.data.isWarehouseForProduct,
                    isRowForProduct:prorec.data.isRowForProduct,
                    isRackForProduct:prorec.data.isRackForProduct,
                    isBinForProduct:prorec.data.isBinForProduct,
                    isBatchForProduct:prorec.data.isBatchForProduct,
                    isSKUForProduct:prorec.data.isSKUForProduct,
                    isSerialForProduct:prorec.data.isSerialForProduct,
                    isShowStockType:(this.isCustomer)?true:false,
//                    linkflag:isLinkFromPO?false:obj.data.linkflag,//As their no batch details for PO So we Sending the Linking Flag false
                    isEdit:true,
                    copyTrans:this.copyTrans,
                    readOnly:this.readOnly,
                    width:950,
                    height:400,
                    resizable : false,
                    modal: true,
                    isWastageApplicable: prorec.data.isWastageApplicable,
                    parentObj:this.parentObj,
//                    lineRec:obj,
                    parentGrid:this
                });
         this.batchDetailswin.on("beforeclose",function(){
            this.batchDetails=this.batchDetailswin.getBatchDetails();
            var isfromSubmit=this.batchDetailswin.isfromSubmit;
            if(isfromSubmit){  //as while clicking on the cancel icon it was adding the reocrd in batchdetail json 
//                obj.set("batchdetails",this.batchDetails);
                prorec.set("batchdetails", this.batchDetails);
            }
         },this);
        this.batchDetailswin.show();
        }    
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
            this.doDetailswin=new Wtf.account.DODetailsWindow({
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
    
    callPackageDetailsWindow:function(i){
        var index=this.Grid.getStore().findBy(function(rec){
            if(rec.data.productid)
                return true;
            else
                return false;
        })
        if(index!=-1){ 
            var prorec=this.Grid.getStore().getAt(i); 
            this.packingDetailsWin=new Wtf.account.PackageDetailsWindow({
                renderTo: document.body,
                title:WtfGlobal.getLocaleText("erp.EnterAllPackingDetails"),
                isEdit:this.isEdit,
                copyTrans:this.copyTrans,
                width:950,
                height:400,
                record:this.record,
                productrec:prorec.data,
                resizable : false,
                modal : true
            });
            this.packingDetailsWin.on("beforeclose",function(panel){
                if(panel.isFromSubmitButton){
                    prorec.set("packingdetails", panel.packingDetails);
                }
            },this);
            this.packingDetailsWin.show();
        }
    }
});


// code to display Packing DO's List Grid
Wtf.account.PackingDoListGrid=function(config){
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
    Wtf.account.PackingDoListGrid.superclass.constructor.call(this,config);
    this.addEvents({
        'datachanged':true,
        'productdeleted':true
    });
}
Wtf.extend(Wtf.account.PackingDoListGrid,Wtf.grid.EditorGridPanel,{
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
        Wtf.account.PackingDoListGrid.superclass.onRender.call(this,config);
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
            name:'productid'
        },

        {
            name:'description'
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
            name:'remark'
        },
     
        {
            name:'productweight'
        },

        {
            name:'packingdodetails'
        },
        {
          name:'batchdetails'  
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
        {
            name:'packedqty'
        },
        {
            name:'baseuomrate'
        },
        {
            name:'productunitname'
        },
        {
            name:'ispartialpacked'
        },
        {
            name:'ispackedandshipped'
        },
        {
            name:'isdirectshipped'
        },
        {
            name:'isLocationForProduct'
        },
        {
            name:'isWarehouseForProduct'
        },
        {
            name:'isRowForProduct'
        },
        {
            name:'isRackForProduct'
        },
        {
            name:'isBinForProduct'
        },
        {
            name:'isBatchForProduct'
        },
        {
            name:'isSKUForProduct'
        },
        {
            name:'isSerialForProduct'
        },
        {
            name:'uomid'
        },
        {
          name:'dodid'  
        }
       
        
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
            header:WtfGlobal.getLocaleText("erp.ShipQuantity"),//Ship Quantity
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
            anchor:"50%"
        },{   //added the DO's Details'
            header: WtfGlobal.getLocaleText("erp.DODetails"),
            align:'center',
            anchor:"50%",
            dataIndex:"packingdodetails",
            renderer:function(value,meta,rec){                
                value= "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("erp.AddDoDetailsTT")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("erp.AddDoDetails")+"' class='"+getButtonIconCls(Wtf.etype.doDetails)+"'></div>";  
                return value;
            }
        },{
             header: '',
             dataIndex:"serialrenderer",
             align:'center',
             renderer: this.serialRenderer.createDelegate(this),
             hidden:(!Wtf.account.companyAccountPref.isBatchCompulsory && !Wtf.account.companyAccountPref.isSerialCompulsory && !Wtf.account.companyAccountPref.isLocationCompulsory && !Wtf.account.companyAccountPref.isWarehouseCompulsory && !Wtf.account.companyAccountPref.isRowCompulsory && !Wtf.account.companyAccountPref.isRackCompulsory && !Wtf.account.companyAccountPref.isBinCompulsory),
             width:40
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
        serialRenderer:function(v,m,rec){
        return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.serial.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.serial.desc.title")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
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
            var packedqty = rec.data["packedqty"] == undefined ? 0 : rec.data["packedqty"];
            var remainingqty = duequantity - packedqty;
            if(rec.data["isdirectshipped"])
            {
                packedqty = duequantity - packedqty;
                
            }
            obj.record.set("productname", rec.data["productname"]);
            obj.record.set("description", rec.data["description"]);
            obj.record.set("unitname", rec.data["unitname"]);
            obj.record.set("dquantity", rec.data["dquantity"]);
            if ((rec.data["dquantity"] < rec.data["shipquantity"] || (rec.data["shipquantity"] > packedqty && !rec.data["ispackedandshipped"])) || (rec.data["shipquantity"] > packedqty && rec.data["ispartialpacked"] == true)) {
                if (packedqty == 0 && rec.data["ispartialpacked"] == true)  //DO has been partially packed and shipped before and there is no more packed qty left
                {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("erp.dopartialpackwarning")], 2);
                    rec.set("shipquantity", packedqty);
                }
                else if (packedqty == 0 && rec.data["isdirectshipped"] == true) //DO is being directly shipped for the first time
                {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("erp.dodetailspackingwarning")], 2);
                    obj.record.set("shipquantity", duequantity);
                }
                else   //DO has been partially packed and the entered quantity is greater than the packed quantity
                {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("erp.dodetailspackingwarning")], 2);
                    if (rec.data["isdirectshipped"])
                    {
                        rec.set("shipquantity", remainingqty);
                    }
                    else
                    {
                        rec.set("shipquantity", packedqty);
                    }
                    return;
                }
            } else {
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
Wtf.account.DODetailsWindow = function(config){
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
    Wtf.account.DODetailsWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.DODetailsWindow, Wtf.Window, {

    onRender: function(config){
        Wtf.account.DODetailsWindow.superclass.onRender.call(this, config);
        this.createDisplayGrid();
        var shipqty = this.productrec.shipquantity * this.productrec.baseuomrate;      
        var msg = "<b>Product : </b>" + this.productrec.productname + "<br><b>Quantity : </b>" + shipqty + " " + this.productrec.productunitname + "<br><b>UOM Conversion:</b> 1 "
                + this.productrec.unitname + " = " + this.productrec.baseuomrate + " " + this.productrec.productunitname;        
        var title=this.noteType;
        var isgrid=true;
        this.add({
            region: 'north',
            height: 85,
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
                    dodid:this.productrec.dodid,
                    isShip:true,
                    shipquantity:this.productrec.shipquantity * this.productrec.baseuomrate
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
            header:WtfGlobal.getLocaleText("erp.DueQuantityforShiping"),
            dataIndex:'duequantity',
            anchor:"50%"
        },{
            header:WtfGlobal.getLocaleText("erp.ShipQuantity"),//Ship Quantity
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
        if(shipquantity!=this.productrec.shipquantity*this.productrec.baseuomrate){
           WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.PleaseEnteranappropriatevalueForShipQuantities")],0);
           record.set("shipquantity",duequantity);
        }else{
            return true;
        }
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
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.PleaseentervalidShipingquantity")],2);
                rec.set("shipquantity",duequantity);/*if user enters more quantity than due then set it to due quantity*/
                return;
            }else{
                obj.record.set("shipquantity",rec.data["shipquantity"]);
            }          
        }
    }
});  

//code to add Package Details Window
Wtf.account.PackageDetailsWindow = function(config){
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
            this.hide();
        }
    });
    Wtf.apply(this,{
        buttons: this.butnArr
    },config);
    Wtf.account.PackageDetailsWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.PackageDetailsWindow, Wtf.Window, {
    onRender: function(config){
        Wtf.account.PackageDetailsWindow.superclass.onRender.call(this, config);
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
                productname:this.productrec.productname,
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
                    description:this.packingDetailsArr[i].description!=undefined?this.packingDetailsArr[i].description:"",
                    packagequantity:this.packingDetailsArr[i].packagequantity!=undefined?this.packingDetailsArr[i].packagequantity:"",
                    packageperquantity:this.packingDetailsArr[i].packageperquantity!=undefined?this.packingDetailsArr[i].packageperquantity:"" ,
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
        
        this.packageEditor = new Wtf.form.ComboBox({
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
            scope: this
        });
                
        this.packageEditor.on("blur",function(e,a,b){
            e.store=this.packageStore;  
        },this);
               
        this.packageDetails= new Wtf.grid.ColumnModel([{
            header:WtfGlobal.getLocaleText("erp.PackageNo"),
            width:150,
            dataIndex:'packageno',
            editor:new Wtf.form.NumberField({
                name:'packageno'
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
            dataIndex:'packageweight'
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
            header:WtfGlobal.getLocaleText("erp.PackageQuantity"),
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
    
    validatePackingDetails:function(obj){
        var arr = [];
        var packedshipquantity=0;
        var temprecord;
        var checkPackage=true;
        this.store.each(function(record){
            if(this.productrec.shipquantity<(record.get("packagequantity")*record.get("packageperquantity"))){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.packagequanntitywarning")],0);
            }
            temprecord=record;
            arr.push(this.store.indexOf(record));
            packedshipquantity=packedshipquantity+(record.get("packagequantity")*record.get("packageperquantity"));
            if(record.get("packages")==undefined ||record.get("packages")==''){
                 checkPackage=false;
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.packageselectionalert")],0);
            }
        },this);
        this.packingDetails = this.getJSONArray(arr);
        this.isFromSubmitButton=true;
        if(packedshipquantity==this.productrec.shipquantity && checkPackage){
            return true;
        }else if(this.productrec.shipquantity<packedshipquantity){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.packagequanntitywarning")],0);
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
            var rec=obj.record;
            var packageid=rec.data["packages"];
            var packageComboRecIndex = WtfGlobal.searchRecordIndex(this.packageStore, packageid, 'packageid');
            var pack=this.packageStore.getAt(packageComboRecIndex); 
            obj.record.set("description",rec.data["description"]);
            obj.record.set("packagequantity",rec.data["packagequantity"]);
            obj.record.set("packageperquantity",rec.data["packageperquantity"]);
            obj.record.set("packages",pack.data.packageid);
            obj.record.set("packageweight",pack.data.packageweight);
            obj.record.set("packagemeasurement",pack.data.measurement);  

            if(this.productrec.shipquantity<(rec.data["packagequantity"]*rec.data["packageperquantity"])){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.Pleaseentervalidpackingquantity")],2);
                return;
            }else{
                obj.record.set("totalpackagequantity",rec.data["packagequantity"]==undefined||rec.data["packageperquantity"]==undefined?0:rec.data["packagequantity"]*rec.data["packageperquantity"]);

            }
            obj.record.set("grossweight",(rec.data["packagequantity"]==undefined||rec.data["packageperquantity"]==undefined)?0:((pack.data.packageweight)+((rec.data["packagequantity"]*rec.data["packageperquantity"])*this.productrec.productweight)));
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
        newrec.data.productname=record.data.productname;
        newrec.data.productid=record.data.productid;
        newrec.data.productweight=record.data.productweight;
        newrec.data.description=record.data.description;
        newrec.data.billid=record.data.billid;
        this.store.add(newrec);
    }
});  


/********************** Packing DO List Report *************************/
Wtf.account.PackingDoListReportPanel=function(config){
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
    }
    ]);
    
    this.expandStoreUrl = "ACCInvoiceCMN/getPackingDoListRows.do";
    
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
        name:'grossweight'
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
        name:'withoutinventory',
        type:'boolean'
    },

    {
        name:'isfavourite'
    }
    ]);
    this.StoreUrl = "";
    if(this.businessPerson=="Customer"){
        this.StoreUrl = "ACCInvoiceCMN/getPackingDoListMerged.do";
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

    this.productname = new Wtf.form.ComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.productList.gridProduct") + '*',
        hiddenName: 'productid',
        name: 'productid',
        hidden: this.isCustBill,
        store: this.productStore,
        valueField: 'productid',
        displayField: 'productname',
        mode: 'local',
        typeAhead: true,
        triggerAction: 'all',
        hideLabel: true,
        emptyText: WtfGlobal.getLocaleText("acc.msgbox.17"),
        width:100,
        listWidth:150
    });
    
    this.productStore.load();
    
    this.productStore.on("load", function() {
        var record = new Wtf.data.Record({
            productid: "",
            productname: "All Records"
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
    
    this.tbar2 = new Array();
    this.tbar2.push(WtfGlobal.getLocaleText("acc.common.from"));
    this.tbar2.push(this.startDate);
    this.tbar2.push(WtfGlobal.getLocaleText("acc.common.to"));
    this.tbar2.push(this.endDate);    
    this.tbar2.push("-", WtfGlobal.getLocaleText("acc.invReport.prod"), this.productname);
    this.tbar2.push("-", WtfGlobal.getLocaleText("acc.masterConfig.19"),this.productCategory);
    this.tbar2.push("-");
    this.tbar2.push(this.submitBttn);

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
        // loadMask:true,
        plugins: this.expander,
        viewConfig:this.gridView1,
        forceFit:true,
        columns:[this.sm,this.expander,
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
        }]
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

    bottombtnArr.push('-', this.singlePrint=new Wtf.exportButton({
        obj:this,
        id:"printReports"+config.helpmodeid+config.id,
        iconCls: 'pwnd exportpdfsingle',
        text:WtfGlobal.getLocaleText("acc.rem.39.single"),// + " "+ singlePDFtext,
        tooltip :WtfGlobal.getLocaleText("acc.rem.39.singletooltip"),      //'Export selected record(s)',
        disabled :true,
        isEntrylevel:false,
        menuItem:{
            rowPdf:true,   
            rowPdfTitle:WtfGlobal.getLocaleText("acc.rem.39") + " "+ this.label
        },
        get:tranType,
        moduleid:config.moduleid
    }));
    bottombtnArr.push('-', this.singleRowPrint=new Wtf.exportButton({
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
    Wtf.account.PackingDoListReportPanel.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });
    this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);
    this.grid.on('cellclick',this.onCellClick, this);
}
Wtf.extend(Wtf.account.PackingDoListReportPanel,Wtf.Panel,{
    submitHandler : function(){
        this.loadStore();
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
                    grossweight:this.itempackingDetailsArray[i].grossweight!=undefined?this.itempackingDetailsArray[i].grossweight:""
                }); 
                this.expandItemPackingStore.add(rec);
            }
        }

        var disHtml = "";
        var disHtmlshipingdodetails = "";
        var disHtmlitempackingdetails= "";
        var arrproductdetails=[];
        var arrshipingdetails=[];
        var arritempackingdetails=[];
        
        var productTypeText = this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pTypeNonInv") : WtfGlobal.getLocaleText("acc.invoiceList.expand.pType");
        arrproductdetails=[(this.withInvMode?'':WtfGlobal.getLocaleText("acc.invoiceList.expand.PID")),//PID for Inventory
        (this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetailsNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pName")),//Product Details or Product Name
        WtfGlobal.getLocaleText("acc.product.description"),WtfGlobal.getLocaleText("acc.invoice.gridUOM"),
        (this.isCustomer?
            productTypeText:(Wtf.account.companyAccountPref.countryid == '203' && !this.isQuotation && !this.isOrder)?
            WtfGlobal.getLocaleText("acc.field.PermitNo."):productTypeText),//Product Type
        WtfGlobal.getLocaleText("erp.QuantityinDOs"),//Quantity
        WtfGlobal.getLocaleText("erp.packingdolist.shiippingquantity"),//Delivered Quantity
        " "];
        
        var gridHeaderText = this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pListNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pList");
        var header1 = "<span class='gridHeader'>"+gridHeaderText+"</span>";   //Product List
        header1 += "<span class='gridRow' style='font-weight:bold; width: 12%'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
    
        for(var j=0;j<arrproductdetails.length;j++){
            header1 += "<span class='headerRow' style='width:12%'>" + arrproductdetails[j] + "</span>";
        }
        header1 += "<span class='gridLine'></span><br>"; 
        
        for(var i=0;i<this.expandStore.getCount();i++){
            var rec=this.expandStore.getAt(i);
            var productname=this.withInvMode?rec.data['productdetail']: rec.data['productname'];
            var description= rec.data['description'];

            //Column : S.No.
            header1 += "<span class='gridRow' style='width:12%'>"+(i+1)+".</span>";
                   
            //Column : Product Id for Inventory
            var pid=rec.data['pid'];
            header1 += "<span class='gridRow'  wtf:qtip='"+pid+"' style='width:12%'>"+Wtf.util.Format.ellipsis(pid,15)+"</span>";
        
            //Column : Product Name
            header1 += "<span class='gridRow' style='width:12%'  wtf:qtip='"+productname+"'>"+Wtf.util.Format.ellipsis(productname,15)+"</span>";
        
            //Column : Product Description
            if(description==''){
                header1 += "<span class='gridRow' style='width:12%'>&nbsp;</span>";
            }else{
                header1 += "<span class='gridRow' style='width:12%'  wtf:qtip='"+description+"'>"+Wtf.util.Format.ellipsis(description,15)+"</span>";
            }
        
            //Column : Product UOM
            header1 += "<span class='gridRow' style='width:12%' >"+rec.data['unitname']+"</span>";
        
            if(!this.isCustomer && !this.isQuotation && !this.isOrder && Wtf.account.companyAccountPref.countryid == '203'){
                header1 += "<span class='gridRow' style='width:12%'>"+rec.data['permit']+"&nbsp;</span>";
            }else if(!this.withInvMode){
                var type = "";
                type = rec.data['type']
                header1 += "<span class='gridRow' wtf:qtip='"+type+"' style='width:12%'>"+Wtf.util.Format.ellipsis(type,15)+"</span>";
            }else {
                header1 += "<span class='gridRow' style='width:12%'>&nbsp;</span>";
            }
                
            //Quantity In DO
            header1 += "<span class='gridRow' style='width:12%'>"+rec.data['actualquantity']+" "+rec.data['unitname']+"</span>";
                
            //Ship Quantity
            header1 += "<span class='gridRow' style='width:12%'>"+rec.data['deliveredquantity']+" "+rec.data['unitname']+"</span>";
        }
            
        disHtml += "<div class='expanderContainer' style='width:100%'>" + header1 + "</div><br>";
 
        arrshipingdetails=[WtfGlobal.getLocaleText("erp.DONumber"),//DO Number
        WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),//Product Name
        WtfGlobal.getLocaleText("acc.product.description"),//Description
        WtfGlobal.getLocaleText("acc.invoice.gridUOM"),//UOM
        WtfGlobal.getLocaleText("erp.ActualQuantityinDO"),//Actual Quantity In Do
        WtfGlobal.getLocaleText("erp.DueQuantityforShiping"),// Due Quantity For Shiping
        WtfGlobal.getLocaleText("erp.packingdolist.shiippedquantity"), // Shipped quantity
        " "];
           
        var gridHeaderText1 =WtfGlobal.getLocaleText("erp.ShipingDODetails");
        var header2 = "<span class='gridHeader'>"+gridHeaderText1+"</span>";   //Shiping DO's Details
        header2 += "<span class='gridRow' style='font-weight:bold; width: 12%'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
           
        for(var j=0;j<arrshipingdetails.length;j++){
            header2 += "<span class='headerRow' style='width:12%' wtf:qtip='"+arrshipingdetails[j]+"'>" +Wtf.util.Format.ellipsis(arrshipingdetails[j],20)  + "</span>";
        }
        header2 += "<span class='gridLine'></span><br>"; 
        
        for(i=0;i<this.expandShipingDoStore.getCount();i++){
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
        }
            
        disHtmlshipingdodetails += "<div class='expanderContainer' style='width:100%'>" + header2 + "</div><br>";
        
        arritempackingdetails=[WtfGlobal.getLocaleText("erp.Package"),//Package
        WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),//Product Name
        WtfGlobal.getLocaleText("acc.erp.ProductWeight"),//Product Weight
        WtfGlobal.getLocaleText("erp.PackageQuantity"),//Package Quantity
        WtfGlobal.getLocaleText("erp.PackagePerQuantity"),//Package Per Quantity
        WtfGlobal.getLocaleText("erp.TotalPackageQuantity"),// Total Quantity
        WtfGlobal.getLocaleText("erp.GrossWeight"), // Gross Weight
        " "];
           
        var gridHeaderText2=WtfGlobal.getLocaleText("erp.itempackingdetails");
        var header3 = "<span class='gridHeader'>"+gridHeaderText2+"</span>";   //Product List
        header3 += "<span class='gridRow' style='font-weight:bold; width: 12%'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
           
        for(var j=0;j<arritempackingdetails.length;j++){
            header3 += "<span class='headerRow' style='width:12%' wtf:qtip='"+arritempackingdetails[j]+"'>" + Wtf.util.Format.ellipsis(arritempackingdetails[j],20) + "</span>";
        }
        header3 += "<span class='gridLine'></span><br>"; 
        
        for(i=0;i<this.expandItemPackingStore.getCount();i++){
            var packedrec=this.expandItemPackingStore.getAt(i);
            var productname=this.withInvMode?packedrec.data['productdetail']: packedrec.data['productname'];
            var description= packedrec.data['description'];
        
            //Column : S.No.
            header3 += "<span class='gridRow' style='width:12%'>"+(i+1)+".</span>";
             
            //Column : DO Number
            header3 += "<span class='gridRow'  wtf:qtip='' style='width:12%'>"+packedrec.data['packagename']+"</span>";
        
            //Column : Product Name
            header3 += "<span class='gridRow' style='width:12%'  wtf:qtip='"+productname+"'>"+Wtf.util.Format.ellipsis(productname,15)+"</span>";
        
            //Column : Product Weight
            header3 += "<span class='gridRow' style='width:12%'  wtf:qtip=''>"+packedrec.data['productweight']+"</span>";
        
            //Column : Package Quantity
        
            header3 += "<span class='gridRow' wtf:qtip='' style='width:12%'>"+packedrec.data['packagequantity']+"Packages "+"</span>";
                
            // Package Per Quantity
            header3 += "<span class='gridRow' style='width:12%'>"+packedrec.data['itemperpackage']+" "+packedrec.data['unitname']+"</span>";
                
            //Total Items
            header3 += "<span class='gridRow' style='width:12%'>"+packedrec.data['totalquantity']+" "+packedrec.data['unitname']+"</span>";
        
            //Gross Weight
            header3 += "<span class='gridRow' style='width:12%'>"+packedrec.data['grossweight']+" "+WtfGlobal.getLocaleText("acc.field.weighingunit")+"</span>";
        }
            
        disHtmlitempackingdetails += "<div class='expanderContainer' style='width:100%'>" + header3 + "</div>";
        
        this.expanderBody.innerHTML = disHtml+"<br><br><br><br>"+ disHtmlshipingdodetails+"<br><br><br><br>"+disHtmlitempackingdetails;  
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
                    this.ajxUrl = "ACCInvoice/deletePackingDoListsPermanent.do";  
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
////////////////////////////////// Component for Sipping DO Report/////////////////////////
function ShippingDOReport() {
    var mainTabId = Wtf.getCmp("as");
    var newTab = Wtf.getCmp("ShippingDOReport");
    if (newTab == null) {
        newTab = new Wtf.account.ShippingDOReport({
            layout: "fit",
            title: WtfGlobal.getLocaleText("acc.field.shipping.report"),
            tabTip: WtfGlobal.getLocaleText("acc.field.shipping.report"),
            closable: true,
            border: false,
            iconCls: getButtonIconCls(Wtf.etype.inventorysmr),
            id: "ShippingDOReport"
        });
        mainTabId.add(newTab);
    }
    mainTabId.setActiveTab(newTab);
    mainTabId.doLayout();
}

Wtf.account.ShippingDOReport = function(config) {
    this.arr = [];
    this.title = config.title;
    Wtf.apply(this, config);
    /*
     * Create Tool Bar Buttons
     */
    this.createTBar();
    /*
     * Create Grid 
     */
    this.createGrid();

    Wtf.account.ShippingDOReport.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.ShippingDOReport, Wtf.Panel, {
    onRender: function(config) {
        /*
         * create panel to show grid
         */
        this.createPanel();
        this.add(this.leadpan);
        /*
         * fetch data in report
         */
        this.fetchStatement();
        Wtf.account.ShippingDOReport.superclass.onRender.call(this, config);
    },
    createPanel: function() {
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [
                {
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid],
                    tbar: this.btnArr,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.Store,
                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({
                            id: "pPageSize_" + this.id
                        }),
                        items: this.bbarBtnArr
                    })
                }]
        });
    },
    createTBar: function() {
        this.btnArr = [];
        this.bbarBtnArr = [];
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.rem.5")+" "+this.title,
            width: 175,
            id: "quickSearch" + this.id,
            field: 'empid',
            hidden: false,
            Store:this.Store
        });
        this.startDate = new Wtf.ExDateFieldQtip({
            name: 'stdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.getDates(true)
        });
        this.endDate = new Wtf.ExDateFieldQtip({
            name: 'enddate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.getDates(false)
        });
        this.personRec = new Wtf.data.Record.create([
            {
                name: 'accid'
            }, {
                name: 'accname'
            }, {
                name: 'acccode'
            }, {
                name: 'taxId'
            }
        ]);
        this.customerAccStore = new Wtf.data.Store({
            url: "ACCCustomer/getCustomersForCombo.do",
            baseParams: {
                mode: 2,
                group: 10,
                deleted: false,
                nondeleted: true,
                common: '1'
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                autoLoad: false
            }, this.personRec)
        });
        this.customerAccStore.on("load", function() {
            var record = new Wtf.data.Record({
                accid: "",
                accname: "All Records",
                acccode: ""
            });
            this.customerAccStore.insert(0, record);
            this.custmerCmb.setValue("");
        }, this);
        this.btnArr.push(this.quickPanelSearch);
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.common.from"), this.startDate);
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.common.to"), this.endDate);
        this.custmerCmb = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.invoiceList.cust"),
            hiddenName: 'customerid',
            id: "customer" + this.id,
            store: this.customerAccStore,
            valueField: 'accid',
            displayField: 'accname',
//            allowBlank: false,
            typeAhead: true,
            emptyText: WtfGlobal.getLocaleText("acc.inv.cus"),
            mode: 'remote',
            anchor: "50%",
            triggerAction: 'all',
            scope: this,
            width: 150
        });

        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.up.3"), this.custmerCmb);
        this.productRec = Wtf.data.Record.create([
            {name: 'productid'},
            {name: 'productname'},
            {name: 'desc'},
            {name: 'producttype'},
            {name: 'pid'}
        ]);
        this.productStore = new Wtf.data.Store({
            url: "ACCProduct/getProductsForCombo.do",
            baseParams: {mode: 22,
                onlyProduct: true,
                isFixedAsset: false,
                includeBothFixedAssetAndProductFlag: false,
                excludeParent: true
//                type: Wtf.producttype.customerInventory
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.productRec)
        });
        this.productStore.on("load", function() {
            var record = new Wtf.data.Record({
                productid: "",
                productname: "All Records",            
                pid : ""
            });
            this.productStore.insert(0, record);
            this.productcmb.setValue("");
        }, this);
        this.productcmb = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.productList.gridProduct") + '*',
            hiddenName: 'productid',
            name: 'productid',
            hidden: this.isCustBill,
            store: this.productStore,
            valueField: 'productid',
            displayField: 'productname',
            extraFields:['pid'],
            mode: 'remote',
            typeAhead: true,
            triggerAction: 'all',
            hideLabel: true,
            emptyText: WtfGlobal.getLocaleText("acc.msgbox.17"),
            width: 150
        });
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.invReport.prod"), this.productcmb);
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
        this.btnArr.push('-', this.fetchBttn);
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.btnArr.push('-', this.resetBttn);
        this.resetBttn.on('click', this.handleResetClickNew, this);
        this.exportButton = new Wtf.exportButton({
            obj: this,
            id: "exportReports" + this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), // 'Export report details',
            filename: WtfGlobal.getLocaleText("acc.field.shipping.shippingdo") + "_v1",
            scope: this,
            menuItem: {
                csv: true,
                pdf: true,
                xls: true
            },
            get: Wtf.autoNum.ShippingDOReport
        });
        this.bbarBtnArr.push('-', this.exportButton);
        this.exportButton.on("click", function() {
            this.exportButton.setParams({
                productid: this.productcmb.getValue(),
                customerid: this.custmerCmb.getValue(),
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            });
        }, this);

    },
    createGrid: function() {
        this.expander = new Wtf.grid.RowExpander({});
        this.Store = new Wtf.data.Store({
            url: "ACCInvoiceCMN/getShippingDeliveryOrders.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
        this.sm = new Wtf.grid.CheckboxSelectionModel({
        });
        this.grid = new Wtf.grid.GridPanel({
            layout: 'fit',
            region: "center",
            store: this.Store,
            columns: [],
            border: false,
            plugins: this.expander,
            loadMask: true,
            sm: this.sm,
            viewConfig: {
                forceFit: false,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        
        this.Store.on('beforeload',function(s,o){
            if(!o.params)o.params={};
            var currentBaseParams = this.Store.baseParams;
            currentBaseParams.startdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
            currentBaseParams.enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
            currentBaseParams.ss=this.quickPanelSearch.getValue();
            currentBaseParams.productid=this.productcmb.getValue();
            currentBaseParams.customerid=this.custmerCmb.getValue();
            this.Store.baseParams=currentBaseParams;
        },this);

        this.Store.on('load', this.handleStoreOnLoad, this);
        this.expander.on("expand",this.onRowexpand,this);
    },
    fetchStatement: function() {
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value
            }
        }, this);
    },
    handleStoreOnLoad: function(store) {
        var columns = [];
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));
        columns.push(this.sm);
        columns.push(this.expander);
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if(column.dataIndex === 'upsTrackingNumbers') {
                column.renderer = function (value, metaData, record) {
                        return "<span wtf:qtip='" + value + "'>" + value + "</span>";
                    };
            } else {
                column.renderer = WtfGlobal.deletedRenderer;
            }
            columns.push(column);
        });
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();

        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        this.quickPanelSearch.StorageChanged(store);
    },
    handleResetClickNew: function()
    {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
        }
        this.productcmb.setValue("");
        this.custmerCmb.setValue("");
        this.startDate.setValue(this.getDates(true));
        this.endDate.setValue(this.getDates(false));
        this.fetchStatement();
    },
    getDates: function(start) {
        var d = new Date();
        var monthDateStr = d.format('M d');
        if (Wtf.account.companyAccountPref.fyfrom) {
            monthDateStr = Wtf.account.companyAccountPref.fyfrom.format('M d');
        }
        var fd = new Date(monthDateStr + ', ' + d.getFullYear() + ' 12:00:00 AM');
        if (d < fd) {
            fd = new Date(monthDateStr + ', ' + (d.getFullYear() - 1) + ' 12:00:00 AM');
        }
        if (start) {
            return fd;
        }
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    onRowexpand:function(scope, record, body, rowIndex){
        this.expanderBody=body;
        this.fillExpanderBody(record);
    },
/**
 * Fill expander data 
 */
    fillExpanderBody:function(record){
        var disHtml = "";
        var arr=[];
        arr=[WtfGlobal.getLocaleText("acc.invoiceList.expand.PID"),WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),WtfGlobal.getLocaleText("erp.QuantityinDOs"),WtfGlobal.getLocaleText("acc.field.shipping.qty"),WtfGlobal.getLocaleText("acc.up.44")];
        var count=0;
        for(var i=0;i<arr.length;i++){
            if(arr[i] != ""){
                count++;
            }
        }
        count=count+2;
        var widthInPercent=100/count;
        var minWidth = count*100;
        var gridHeaderText = WtfGlobal.getLocaleText("acc.invoiceList.expand.pList");
        var header = "<span class='gridHeader'>"+gridHeaderText+"</span>";   //Product List
        header += "<span class='gridRow' style='font-weight:bold; width: 12%'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
        for(var j=0;j<arr.length;j++){
            header+= "<span class='headerRow' style='width:15%'>" + arr[j] + "</span>";
        }
        header += "<span class='gridLine'></span><br>"; 
        var shippingdetails = record.data.shippingdetails;
        for(i=0;i<shippingdetails.length;i++){
            var rec=shippingdetails[i];
            var productid=rec.productid;
            
            header += "<span class='gridRow' style='width:15%'>"+(i+1)+".</span>";
            
            header += "<span class='gridRow'  wtf:qtip='"+productid+"' style='width:15%'>"+Wtf.util.Format.ellipsis(productid,15)+"</span>";
        
            header += "<span class='gridRow' style='width:15%'  wtf:qtip='"+rec.productname+"'>"+Wtf.util.Format.ellipsis(rec.productname,15)+"</span>";
            
            header += "<span class='gridRow' style='width:15%'>"+rec.actualquantity+" "+rec.unitname+"</span>";
                
            header += "<span class='gridRow' style='width:15%'>"+rec.shipquantity+" "+rec.unitname+"</span>";
            
            header += "<span class='gridRow'  wtf:qtip='"+rec.stockadjustment+"' style='width:15%'>"+Wtf.util.Format.ellipsis(rec.stockadjustment,15)+"</span>";
        }
        disHtml += "<div class='expanderContainer' style='width:100%'>" + header + "</div><br>";
        this.expanderBody.innerHTML = disHtml;
    }
});