/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.account.addressWindow=function(config){
    this.record=config.record;
    this.isEdit=config.isEdit;
    this.copyInv=config.copyInv;
    this.moduleid=config.moduleid,
    this.viewGoodReceipt=config.viewGoodReceipt;
    this.isViewTemplate=config.isViewTemplate;
    this.currentaddress=config.currentaddress;
    this.billingComboValueBeforeSelect="";
    this.vendorbillingComboValueBeforeSelect="";
    this.shippingComboValueBeforeSelect="";
    this.vendorShippingComboValueBeforeSelect="";
    this.linkedDocumentComboValueBeforeSelect="";
    this.singleLink=config.singleLink!=undefined?config.singleLink:false;       // true when user link single record
    this.isPOFromSO=config.isPOFromSO!=undefined?config.isPOFromSO:false;      
    this.customeridforshippingaddress=(config.customeridforshippingaddress!==undefined ||config.customeridforshippingaddress!=='') ?config.customeridforshippingaddress:"";      
    this.showCustomerShippingWin=false;//This flag is used hide/show functionality of Customer Shipping Address in Purchase Doc(i.e PO created By SO)
    this.linkedDocuments=config.linkedDocuments;//currently this value only come from Consingment DO and GR 
    this.isLinkedTransaction=config.isLinkedTransaction!=undefined ? config.isLinkedTransaction :false;//isLinkedTransaction->true,If document is linked with any other document as a parent
    this.disabledAddressFields = (this.isLinkedTransaction &&  this.isEdit) ? true :false;
    this.isdropshipDocument=false;
    this.isdropshipTypeDocument =config.isdropshipTypeDocument!=undefined ? config.isdropshipTypeDocument : false;
    
    /*
     * Flag used to confirm if state field will be of drop down type or Text field type
     * If true , field will be drop down
     * If false , field will be text field
     */
    this.stateAsComboFlag = config.stateAsComboFlag!=undefined?config.stateAsComboFlag:false;
    this.loadMask = new Wtf.LoadMask(document.body, {
        msg: WtfGlobal.getLocaleText("acc.msgbox.50")//loadMask Message -> Loading...
    });
    
    var buttonArray = new Array();
    
    Wtf.apply(this,config);
    this.closeButton = new Wtf.Toolbar.Button({
      text: WtfGlobal.getLocaleText("acc.common.close"),
      minWidth: 50,
      scope: this,
      handler: function(){
          this.close();
      }
     });
     
     this.saveButton = new Wtf.Toolbar.Button({
      text:  WtfGlobal.getLocaleText("acc.common.saveBtn"),
      minWidth: 50,
      id:'savebutton'+this.id,
      disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
      scope: this,
      handler: config.avalaraAddressValidation ? this.validateAddressWithAvalara.createDelegate(this, [true]) : this.saveData.createDelegate(this)
     });
     
    buttonArray.push(this.saveButton);
    buttonArray.push(this.closeButton);
    
    if (config.avalaraAddressValidation) {
        this.validateAddressBttn = new Wtf.Toolbar.Button({//Button to validate address with Avalara REST service
            text: WtfGlobal.getLocaleText("acc.common.validateAddresses"),
            tooltip: WtfGlobal.getLocaleText("acc.integration.validateAddressesWithAvalara"),
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            scope: this,
            handler: this.validateAddressWithAvalara.createDelegate(this, [false]),
            iconCls: 'pwnd validate'
        });
        buttonArray.push(this.validateAddressBttn);
    }
    
    Wtf.apply(this,{
       buttons:buttonArray 
    });
    Wtf.account.addressWindow.superclass.constructor.call(this,config);
    
    this.addEvents({
        'update':true
    });
    this.copyAddress.on('check',this.copyBillingAddress ,this);
}

Wtf.extend(Wtf.account.addressWindow,Wtf.Window,{
    onRender:function(config){
     Wtf.account.addressWindow.superclass.onRender.call(this,config);
      /*
       * Below if block is used to show Customer Shipping address section while PO is created from SO 
      */
     if (Wtf.account.companyAccountPref.isCustShipAddressInPurchase && this.moduleid === Wtf.Acc_Purchase_Order_ModuleId && this.isPOFromSO && this.customeridforshippingaddress!=='') {
         this.showCustomerShippingWin=true;
     }
     
        if (Wtf.account.companyAccountPref.columnPref.activatedropship && this.isdropshipTypeDocument && (this.moduleid === Wtf.Acc_Purchase_Order_ModuleId || this.moduleid ==Wtf.Acc_Vendor_Invoice_ModuleId)) {
            this.showCustomerShippingWin = true;
            this.isdropshipDocument = true;
        }
          
     this.createStore();
     this.createFields();
     var centerPanelWidth="";
     if((this.isCustomer|| Wtf.account.companyAccountPref.isAddressFromVendorMaster) && !this.isdropshipDocument){
        centerPanelWidth=1105;
     }else if (WtfGlobal.isIndiaCountryAndGSTApplied() && !this.isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster){
         /**
          * For india country and vendor transactions and isAddressFromVendorMaster 
          *  is off then vendor billing address visible in seperate fieldset so increase pane size
          */
         centerPanelWidth = 2200; 
     }else{
         centerPanelWidth=1600; 
     }
     var centerPanel = new Wtf.Panel({
            id:'addressdetails'+this.id,
            bodyStyle:' background: none repeat scroll 0 0 #DFE8F6;',
            layout: 'fit',
            autoScroll:true,
            width:centerPanelWidth,
            items:this.addressDetailForm,
            html:this.disabledAddressFields ? "<span ><span style='margin-left:15px;'><b>Note</b>: You cannot update the address of this document as current document is linked with another document as a parent.For more information, Please check Linking Information details.</span>" : "" 
        });
        this.add(centerPanel);
        this.loadRecord();
        
        this.billingAddrsStore.on('load',this.onBillingAddressLoad,this); 
        if (this.isdropshipDocument) {
            this.vendorbillingAddrsStore.on('load', this.onVendorBillingAddressLoad, this);
        }
        this.ShippingAddrsStore.on('load',this.onShippingAddressLoad,this); 
        this.docStore.on('load',this.onDocStoreLoad,this); 
        this.billingAddrsCombo.on('select',this.setBillingAddressDataOnSelect,this);
        if (this.isdropshipDocument) {
            this.dropshipbillingAddrsCombo.on('select', this.setVendorBillingAddressDataOnSelect, this);
        }
        /**
          * For india country and vendor transactions and isAddressFromVendorMaster 
          *  is off then vendor billing address visible in seperate fieldset , handle this address details
          */
        if(WtfGlobal.isIndiaCountryAndGSTApplied() && !this.isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster){
            this.vendorbillingAddrsStore.on('load', this.onVendorBillingAddressLoadINDIA, this);
            this.vendorbillingAddrsCombo.on('select', this.setVendorBillingAddressDataOnSelectINDIA, this);
            this.vendorbillingAddrsCombo.on('beforeselect',function(combo){this.vendorbillingComboValueBeforeSelect = combo.getValue();},this);
            this.vendorbillingAddrsCombo.addNewFn=this.addBillingAdress.createDelegate(this);
        }    
        this.shippingAddrsCombo.on('select',this.setShippingAddressDataOnSelect,this);
        this.linkedDocumentCombo.on('select',this.onLinkedDocumentSelect,this);
        this.billingAddrsCombo.on('beforeselect',function(combo){this.billingComboValueBeforeSelect=combo.getValue();},this);
        this.shippingAddrsCombo.on('beforeselect',function(combo){this.shippingComboValueBeforeSelect=combo.getValue();},this);
        this.linkedDocumentCombo.on('beforeselect',function(combo){this.linkedDocumentComboValueBeforeSelect=combo.getValue();},this);
        this.shippingAddrsCombo.addNewFn=this.addShippingAdress.createDelegate(this);
        this.billingAddrsCombo.addNewFn=this.addBillingAdress.createDelegate(this);
        
        if(!this.isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster){//only for vendor documents when option "Show Vendor's address in Purchase documents" in master is false 
           this.vendorShippingAddrsStore.on('load',this.onVendorShippingAddressLoad,this);  
           this.vendorShippingAddrsCombo.on('select',this.setVendorShippingAddressDataOnSelect,this);
           this.vendorShippingAddrsCombo.on('beforeselect',function(combo){this.vendorShippingComboValueBeforeSelect=combo.getValue();},this);
           this.vendorShippingAddrsCombo.addNewFn=this.addShippingVendorAdress.createDelegate(this);
        }
        
        /*
         *  If 'this.showCustomerShippingWin' is true the need to add below event
        */
        if (this.showCustomerShippingWin) {
           this.customerShippingAddrsStore.on('load',this.onCustomerShippingAddressLoad,this);  
           this.customerShippingAddrsCombo.on('select',this.setCustomerShippingAddressDataOnSelect,this);
           this.customerShippingAddrsCombo.on('beforeselect',function(combo){this.customerShippingComboValueBeforeSelect=combo.getValue();},this);
           this.customerShippingAddrsCombo.addNewFn=this.addCustomerShippingAdress.createDelegate(this);
        }
    },
    
    /**
     * Function to validate addresses with Avalara REST API
     * Used only when Avalara Integration is enabled
     * @param {type} isCallFromSave
     * @returns {undefined}
     */
    validateAddressWithAvalara: function (isCallFromSave) {//isCallFromSave is true when the call to function comes from Save button's handler
        this.loadMask.show();
        Wtf.Ajax.requestEx({
            url: "Integration/validateAddress.do",
            method: "POST",
            params: {
                addressesForValidationWithAvalara: JSON.stringify(this.addressDetailForm.getForm().getValues()),
                integrationPartyId: Wtf.integrationPartyId.AVALARA,//Identifier for Integration Service owner party. 2 -> Avalara REST Service
                integrationOperationId: Wtf.integrationOperationId.avalara_addressValidation//Identifier for Integration operation which is to be performed
            }
        }, this, function (res, req) {
            this.loadMask.hide();
            if (res.success) {
                if (isCallFromSave) {//If address validation is successful, then save addresses
                    this.saveData();
                } else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), res.msg], 0);
                }
            } else {
                if (isCallFromSave) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.integration.addressValidationFailure") + "<br><br><b>NOTE: </b>" + WtfGlobal.getLocaleText("acc.integration.addressValidationSettingsInfo")], 1);
                } else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), res.msg], 1);
                }
            }
        }, function () {
            this.loadMask.hide();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.field.Anerroroccurredwhileconnectingtoservice")], 1)
        });
    },
    
    addShippingAdress:function(){
        this.close();//closing this address window and switching user to customer/vendor/company address panel for adding multiple addresses
        if(this.isCustomer || Wtf.account.companyAccountPref.isAddressFromVendorMaster){//Add customer/vendor address
            this.addNewCustomerVendorAddress();
        } else{// add company address
            var addAddressFromTransactions=true;
            showAddressWindowForCompany(addAddressFromTransactions);
        }
    },
    addBillingAdress:function(){
        this.close();//closing this address window and switching user to customer/vendor/company address panel for adding multiple addresses
        if(this.isCustomer || Wtf.account.companyAccountPref.isAddressFromVendorMaster){//Add customer/vendor address
            this.addNewCustomerVendorAddress();
        } else if(WtfGlobal.isIndiaCountryAndGSTApplied() && !this.isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster){
            /**
              * For india country and vendor transactions and isAddressFromVendorMaster 
              *  is off then vendor billing address visible in seperate fieldset.
              *  For adding new address of Vendor 
              */
            this.addNewCustomerVendorAddress();
        } else{// add company address
            var addAddressFromTransactions=true;
           showAddressWindowForCompany(addAddressFromTransactions);
        }
    },
    addShippingVendorAdress:function(){
        this.close();//closing this address window and switching user to customer/vendor address panel for adding multiple addresses
        this.addNewCustomerVendorAddress();
    },
    /*
     * Call to add new Customer Shipping Address Window
     */
    addCustomerShippingAdress:function(){
        this.close();//closing this address window and switching user to customer/vendor address panel for adding multiple addresses
        this.addNewCustomerShippingAddress();
    },
    addNewCustomerShippingAddress: function() {
        var url = "";
        var params = {};
        if (this.showCustomerShippingWin) {
            url = "ACCCustomerCMN/getCustomers.do";
            params = {customerid: this.customeridforshippingaddress};
            Wtf.Ajax.requestEx({
                url: url,
                params: params
            }, this, function(response) {
                if (response.success) {
                    var record = new Wtf.data.Record(response.data[0]);
                    if (record.data.addressDetails == undefined) {
                        record.data.addressDetails = "";
                    }
                    record.data.fromCustomerShippingAddress=true;
                    this.addCustomerVendorAddress(record);
                }
            }, function(response) {
            });
        }
    },
    addNewCustomerVendorAddress:function(){
        Wtf.Ajax.requestEx({
            url:this.isCustomer?"ACCCustomerCMN/getCustomers.do":"ACCVendorCMN/getVendors.do",
            params:this.isCustomer?{
                customerid:this.accid
            }:{
                vendorid:this.accid
            }
        },this,function(response){
            if(response.success){  
                var record=new Wtf.data.Record(response.data[0]);   
                if(record.data.addressDetails==undefined){
                    record.data.addressDetails="";
                }
                this.addCustomerVendorAddress(record);
            } 
        },function(response){
            });
    },
    
    
    addCustomerVendorAddress:function(record){
        var tabid="addaddress"+record.data.acccode;
        var panel = Wtf.getCmp(tabid);
        var fromCustomerShippingAddress=false;
        /*
         * if we click the Add New Customer Shipping Address then below flag is used to show the customer shipping address
        */
        if(record.data.fromCustomerShippingAddress!==undefined && record.data.fromCustomerShippingAddress!=null && record.data.fromCustomerShippingAddress){
            fromCustomerShippingAddress=true;
        }
        if(panel==null){
            panel = new Wtf.account.CusVenAddressDetail({
                title:(this.isCustomer || fromCustomerShippingAddress)?WtfGlobal.getLocaleText("acc.field.CustomerContactDetail"):WtfGlobal.getLocaleText("acc.field.VendorContactDetail"),
                tabTip: (this.isCustomer || fromCustomerShippingAddress) ? WtfGlobal.getLocaleText("acc.field.addmultiplecustomeraddresses"):WtfGlobal.getLocaleText("acc.field.addmultiplevendoraddresses"),
                isEdit:false,
                isCopy:false,
                addAddressFromTransactions:true,
                record:record,
                id:tabid,
                closable:true,
                isCustomer:(this.isCustomer || fromCustomerShippingAddress),
                moduleid:(this.isCustomer || fromCustomerShippingAddress)?Wtf.Acc_Customer_ModuleId:Wtf.Acc_Vendor_ModuleId,
                enableCurrency:false,
                iconCls :getButtonIconCls(Wtf.etype.customer),
                layout: 'fit',
                buttonAlign: 'right',
                custVenId:this.stateAsComboFlag
            });
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    },
    
    createStore:function(){
        if(this.isCustomer || this.showCustomerShippingWin){
           Wtf.ShippingRouteStore.load();   
        }
        
        this.addrsRec = new Wtf.data.Record.create([
        {name:'aliasName'},
        {name:'address'},
        {name:'county'},
        {name:'city'},
        {name:'state'},
        {name:'country'},
        {name:'postalCode'},
        {name:'phone'},
        {name:'mobileNumber'},
        {name:'fax'},
        {name:'emailID'},
        {name:'contactPerson'},
        {name:'recipientName'},
        {name:'contactPersonNumber'},
        {name:'contactPersonDesignation'},
        {name:'website'},
        {name:'shippingRoute'},
        {name:'isDefaultAddress'},
        {name:'isBillingAddress'}]);
    
        this.billingAddrsStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.addrsRec),
            url :this.isdropshipDocument ? "ACCCompanyPref/getCompanyAddressDetails.do" : (this.isCustomer?"ACCCustomer/getAddresses.do":(Wtf.account.companyAccountPref.isAddressFromVendorMaster?"ACCVendor/getAddresses.do":"ACCCompanyPref/getCompanyAddressDetails.do")),
            baseParams:{
                isBillingAddress:true,
                customerid:this.accid
            }
        });
        /*---VendorBilling store for Dropship type Doc----  */
        this.vendorbillingAddrsStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            }, this.addrsRec),
            url: "ACCVendor/getAddresses.do",
            baseParams: {
                isBillingAddress: true,
                customerid: this.accid,
            }
        });
        
        
        this.ShippingAddrsStore =  new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.addrsRec),
            url :this.isCustomer?"ACCCustomer/getAddresses.do":(Wtf.account.companyAccountPref.isAddressFromVendorMaster?"ACCVendor/getAddresses.do":"ACCCompanyPref/getCompanyAddressDetails.do"),
            baseParams:{
                isBillingAddress:false,
                customerid:this.accid          
            }
        });
        
        this.vendorShippingAddrsStore =  new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.addrsRec),
            url :"ACCVendor/getAddresses.do",
            baseParams:{
                isBillingAddress:false,
                customerid:this.accid                
            }
        });
        /*
         *  If 'showCustomerShippingWin' flag is true then add below store
         */
        if (this.showCustomerShippingWin) {
            this.customerShippingAddrsStore = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data",
                    totalProperty: "count"
                }, this.addrsRec),
                url: "ACCCustomer/getAddresses.do",
                baseParams: {
                    isBillingAddress: false,
                    customerid: this.customeridforshippingaddress
                }
            });
        }
        
        this.LinkedDocRec = Wtf.data.Record.create ([
            {name: 'billid'},
            {name: 'billno'},
            {name: 'billingAddressType'},
            {name: 'billingAddress'},
            {name: 'billingCountry'},
            {name: 'billingState'},
            {name: 'billingPostal'},
            {name: 'billingEmail'},
            {name: 'billingFax'},
            {name: 'billingMobile'},
            {name: 'billingPhone'},
            {name: 'billingContactPerson'},
            {name: 'billingRecipientName'},
            {name: 'billingContactPersonNumber'},
            {name: 'billingContactPersonDesignation'},
            {name: 'billingWebsite'},
            {name: 'billingCounty'},
            {name: 'billingCity'},
            {name: 'shippingAddressType'},
            {name: 'shippingAddress'},
            {name: 'shippingCountry'},
            {name: 'shippingState'},
            {name: 'shippingCounty'},
            {name: 'shippingCity'},
            {name: 'shippingEmail'},
            {name: 'shippingFax'},
            {name: 'shippingMobile'},
            {name: 'shippingPhone'},
            {name: 'shippingPostal'},
            {name: 'shippingContactPersonNumber'},
            {name: 'shippingContactPersonDesignation'},
            {name: 'shippingWebsite'},
            {name: 'shippingRecipientName'},
            {name: 'shippingContactPerson'},
            {name: 'shippingRoute'},
            {name: 'vendcustShippingAddress'},
            {name: 'vendcustShippingCountry'},
            {name: 'vendcustShippingState'},
            {name: 'vendcustShippingCounty'},
            {name: 'vendcustShippingCity'},
            {name: 'vendcustShippingEmail'},
            {name: 'vendcustShippingFax'},
            {name: 'vendcustShippingMobile'},
            {name: 'vendcustShippingPhone'},
            {name: 'vendcustShippingPostal'},
            {name: 'vendcustShippingContactPersonNumber'},
            {name: 'vendcustShippingContactPersonDesignation'},
            {name: 'vendcustShippingWebsite'},
            {name: 'vendcustShippingContactPerson'},
            {name: 'vendcustShippingRecipientName'},
            {name: 'vendcustShippingAddressType'}
        ]);
        var url="";
        if(this.moduleid==Wtf.Acc_ConsignmentDeliveryOrder_ModuleId){
            url="ACCSalesOrderCMN/getSalesOrders.do";
        } else if(this.moduleid==Wtf.Acc_Consignment_GoodsReceiptOrder_ModuleId){
            url="ACCPurchaseOrderCMN/getPurchaseOrders.do";
        }
        this.docStore = new Wtf.data.Store({
            url:url,
            baseParams:{
                isShowAddress:true,
                isConsignment:true,
                billid:this.linkedDocuments!=undefined?this.linkedDocuments:""
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.LinkedDocRec)
        });
    }, 
    
    createFields:function(){
        /*
         * Check for Tax calculation is based on shipping address or not
         */
        this.isShipping=CompanyPreferenceChecks.getGSTCalCulationType(); 
        /*
        *  If 'showCustomerShippingWin' flag is true then adjust the fieldset width
       */
        this.fieldSetWidht=500;
        if(!(this.isCustomer) && (this.showCustomerShippingWin) && Wtf.account.companyAccountPref.isAddressFromVendorMaster && this.moduleid === Wtf.Acc_Purchase_Order_ModuleId){
            this.fieldSetWidht=330;
        }else if(this.showCustomerShippingWin){
             this.fieldSetWidht=370;
        }
        if(this.isdropshipDocument){
           this.fieldSetWidht=500;   
        }
        
        /*
         * If 'showCustomerShippingWin' flag is true then add Customer Shipping Address related fields
         */
        if(this.showCustomerShippingWin){
          this.createCustomerShippingFields();  
        }
        /*--Creating address fields for Dropship document-- */
        if (this.isdropshipDocument) {
            this.createVendorBillingFields();
        }
        /**
          * For india country and vendor transactions and isAddressFromVendorMaster 
          *  is off then vendor billing address visible in seperate fieldset, Create address Fields 
          */
        if (WtfGlobal.isIndiaCountryAndGSTApplied() && !this.isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster) {
            this.createVendorBiilingAddressFields();
        }
        
        if(!this.isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster){//In Vendor transaction we need extra address fields name Vendor shipping Address when option ""Show Vendor's address in Purchase documents"" false in companypreferences
            this.createVendorShippingFields();
        }
           
        this.linkedDocumentCombo = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            name:'linkedDocumentCombo',
            valueField:'billid',
            displayField:'billno',
            store:this.docStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.address.addressfromdocument") ,
            disabled :this.viewGoodReceipt || this.isViewTemplate,
            hidden : (this.moduleid!=Wtf.Acc_ConsignmentDeliveryOrder_ModuleId && this.moduleid!=Wtf.Acc_Consignment_GoodsReceiptOrder_ModuleId),//it will be hidden in all module except consingment DO and GR
            hideLabel:(this.moduleid!=Wtf.Acc_ConsignmentDeliveryOrder_ModuleId && this.moduleid!=Wtf.Acc_Consignment_GoodsReceiptOrder_ModuleId),
            width:200,
            typeAhead: true,
            forceSelection: true
        });
        
        this.billingAddress = new Wtf.form.TextArea({
            fieldLabel:WtfGlobal.getLocaleText("acc.customer.address"),
            name:"billingAddress",
            id:this.id+'billingAddress',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength:250,
            height:60,
            allowBlank:false,
            allowNegative:false,
            anchor: '98%'
        });
        //Coutry specific address field visible only for US
        if ( WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US 
            this.CountyComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);

            this.CountyComboStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'county'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.CountyComboRec)
            });
            this.billingCounty= new Wtf.form.ExtFnComboBox({
                name:"billingCounty",
                fieldLabel: WtfGlobal.getLocaleText("acc.address.County"),
                store: this.CountyComboStore,
                valueField: 'name',
                displayField: 'name',
                extraFields:'',
                allowBlank:(Wtf.account.companyAccountPref.countryid == Wtf.Country.US && !this.isCustomer)?true:this.isShipping?true:false,  //SDP-12716 making field option if country is US and Purchase side
                extraComparisionField: 'name',
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode:'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth:true,
                disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            });
        } else {

            this.billingCounty = new Wtf.form.TextField({
                fieldLabel:WtfGlobal.getLocaleText("acc.address.County"),
                name:"billingCounty",
                id:this.id+'billingCounty',
                disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
                maxLength:49,
                allowNegative:false,
                anchor: '98%',
                hidden: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.US),
                hideLabel: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.US)
           
        });
      }
              //Coutry specific address field visible only for US
        if ( WtfGlobal.isUSCountryAndGSTApplied()) {
            this.CityComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);

            this.CityComboStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'city'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.CityComboRec)
            });

            this.billingCity= new Wtf.form.ExtFnComboBox({
                name:"billingCity",
                fieldLabel: WtfGlobal.getLocaleText("acc.address.City"),
                store: this.CityComboStore,
                valueField: 'name',
                displayField: 'name',
                extraFields:'',
                allowBlank:(Wtf.account.companyAccountPref.countryid == Wtf.Country.US && !this.isCustomer)?true:this.isShipping?true:false, //SDP-12716 making field option if country is US and Purchase side
                extraComparisionField: 'name',
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode:'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth:true,
                disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields
            });
        } else {

            this.billingCity = new Wtf.form.TextField({
                fieldLabel:WtfGlobal.getLocaleText("acc.address.City"),
                name:"billingCity",
                id:this.id+'billingCity',
                disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
                maxLength:49,
                allowNegative:false,
                anchor: '98%'
            });
        }
         //Coutry specific address field visible only for US or INDIA
        if (( WtfGlobal.isIndiaCountryAndGSTApplied() && this.stateAsComboFlag)||  WtfGlobal.isUSCountryAndGSTApplied()) {
            this.StateComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);

            this.StateComboStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'state'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.StateComboRec)
            });

            this.billingState= new Wtf.form.ExtFnComboBox({
                name:"billingState",
                fieldLabel: WtfGlobal.getLocaleText("acc.address.State"),
                store: this.StateComboStore,
                valueField: 'name',
                displayField: 'name',
                extraFields:'',
                allowBlank:(Wtf.account.companyAccountPref.countryid == Wtf.Country.US && !this.isCustomer) || (WtfGlobal.isIndiaCountryAndGSTApplied() && !this.isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster)?true:this.isShipping?true:false,  //SDP-12716 making field option if country is US and Purchase side
                extraComparisionField: 'name',
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode:'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth:true,
                disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
                anchor: '98%'
            });
        } else {
            this.billingState = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.State"),
            name:"billingState",
            id:this.id+'billingState',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength:49,
            allowNegative:false,
            anchor: '98%'
        });
     }
            this.billingCountry = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Country"),
            name:"billingCountry",
            id:this.id+'billingCountry',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength:49,
            allowNegative:false,
            anchor: '98%'
        });
        
        this.billingPostal = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.PostalCode"),
            name:"billingPostal",
            id:this.id+'billingPostal',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength:50,
            allowNegative:false,
            anchor: '98%'
        });
        
        this.billingPhone= new Wtf.form.TextField({
           fieldLabel:WtfGlobal.getLocaleText("acc.address.Phone"), 
           name: 'billingPhone',
           id:this.id+'billingPhone',
           maxLength:250,
           disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
           anchor: '98%'
        });
        
        this.billingMobile= new Wtf.form.TextField({
           fieldLabel:WtfGlobal.getLocaleText("acc.address.Mobile"),
           name: 'billingMobile',
           id:this.id+'billingMobile',
           disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
           maxLength:250,
           anchor: '98%'
        });
        
        this.billingFax=new Wtf.form.TextField({
           fieldLabel:WtfGlobal.getLocaleText("acc.cust.fax"),
           name: 'billingFax',
           id:this.id+'billingFax',
           maxLength:250,
           disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
           anchor: '98%'
        });
        
        this.billingEmail= new Wtf.form.TextArea({
           fieldLabel: WtfGlobal.getLocaleText("acc.cust.email"),  
           name: 'billingEmail',
           id:this.id+'billingEmail',
           allowBlank:true,
           maxLength:254,
           disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
           validator:WtfGlobal.validateMultipleEmail,
           anchor: '98%'
        });
        
        this.messagePanelBilling = new Wtf.Panel({
            xtype: 'panel', 
            border: false,
            cls: 'emailfieldInfoInContactDetails',
            html: WtfGlobal.getLocaleText("acc.mail.seperator.comma")
        });
        
        this.billingRecipientName=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.RecipientName"),
            name: 'billingRecipientName',
            id:this.id+'billingRecipientName',
            maxLength:200,
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            anchor: '98%'
        });
        
        this.billingContactPerson=new Wtf.form.TextField({
           fieldLabel:WtfGlobal.getLocaleText("acc.address.ContactPerson"),
           name: 'billingContactPerson',
           id:this.id+'billingContactPerson',
           maxLength:200,
           allowBlank:(this.moduleid==Wtf.Acc_ConsignmentRequest_ModuleId&&this.isCustomer)?false:true,
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
           anchor: '98%'
        });
        
       this.billingContactNumber= new Wtf.form.TextField({
           fieldLabel:WtfGlobal.getLocaleText("acc.address.ContactPersonNumber"),
           name: 'billingContactPersonNumber',
           id:this.id+'billingContactNumber',
           disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
           maxLength:250,
           anchor: '98%'
        });
        this.billingContactDesignation = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.ContactPersonDesignation"),
            name: 'billingContactPersonDesignation',
            id: this.id + 'billingContactDesignation',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength: 250,
            anchor: '98%'
        });
        this.billingWebsite = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.website"),
            name: 'billingWebsite',
            id: this.id + 'billingWebsite',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength: 250,
            anchor: '98%'
        }); 
        this.copyAddress= new Wtf.form.Checkbox({
            name:'copyadress',
            id:this.id+'copyadress',
            fieldLabel: WtfGlobal.getLocaleText("acc.cust.sameasbillingadd"),//WtfGlobal.getLocaleText("acc.cust.copyAdd"),  //'Copy Address',
            checked:false,
            cls : 'custcheckbox',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            width: 10,
            disabled:this.isdropshipDocument
        });
        
        this.shippingAddress = new Wtf.form.TextArea({
            fieldLabel:WtfGlobal.getLocaleText("acc.customer.address"),
            name:"shippingAddress",
            id:this.id+'shippingAddress',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength:250,
            height:60,
            allowBlank:false,
            allowNegative:false,
            anchor: '98%'
        });
          if ( WtfGlobal.isUSCountryAndGSTApplied()) {
            this.ShippingCountyComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);

            this.ShippingCountyComboStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'county'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.ShippingCountyComboRec)
            });

          this.shippingCounty= new Wtf.form.ExtFnComboBox({
                name:"shippingCounty",
                fieldLabel: WtfGlobal.getLocaleText("acc.address.County"),
                store: this.ShippingCountyComboStore,
                valueField: 'name',
                displayField: 'name',
                extraFields:'',
                allowBlank:(Wtf.account.companyAccountPref.countryid == Wtf.Country.US && !this.isCustomer)?true:this.isShipping?false:true,  //SDP-12716 making field option if country is US and Purchase side
                extraComparisionField: 'name',
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode:'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth:true,
                disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
                anchor: '98%'
            });
        } else {


           this.shippingCounty = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.County"),
            name:"shippingCounty",
            id:this.id+'shippingCounty',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength:49,
            allowNegative:false,
            anchor: '98%',
            hidden: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.US),
            hideLabel: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.US)
        });
        }
        
         if ( WtfGlobal.isUSCountryAndGSTApplied()) {
            this.ShippingCityComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);

            this.ShippingCityComboStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'city'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.ShippingCityComboRec)
            });


          this.shippingCity= new Wtf.form.ExtFnComboBox({
                name:"shippingCity",
                fieldLabel: WtfGlobal.getLocaleText("acc.address.City"),
                store: this.ShippingCityComboStore,
                valueField: 'name',
                displayField: 'name',
                extraFields:'',
                allowBlank:(Wtf.account.companyAccountPref.countryid == Wtf.Country.US && !this.isCustomer)?true:this.isShipping?false:true,  //SDP-12716 making field option if country is US and Purchase side
                extraComparisionField: 'name',
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode:'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth:true,
                disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
                anchor: '98%'
            });
        } else {


            this.shippingCity = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.City"),
            name:"shippingCity",
            id:this.id+'shippingCity',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength:49,
//            vtype : "alphanum",
            allowNegative:false,
            anchor: '98%'
        });
        }
        
        if (( WtfGlobal.isIndiaCountryAndGSTApplied() && this.stateAsComboFlag)||  WtfGlobal.isUSCountryAndGSTApplied()) {
            this.ShippingStateComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);

            this.ShippingComboStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'state'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.ShippingStateComboRec)
            });

          this.shippingState= new Wtf.form.ExtFnComboBox({
                name:"shippingState",
                fieldLabel: WtfGlobal.getLocaleText("acc.address.State"),
                store: this.ShippingComboStore,
                valueField: 'name',
                displayField: 'name',
                extraFields:'',
                allowBlank:(Wtf.account.companyAccountPref.countryid == Wtf.Country.US && !this.isCustomer)?true:this.isShipping?false:true,   //SDP-12716 making field option if country is US and Purchase side
                extraComparisionField: 'name',
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode:'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth:true,
                disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
                anchor: '98%'
            });
        } else {
            this.shippingState = new Wtf.form.TextField({
               fieldLabel: WtfGlobal.getLocaleText("acc.address.State"),
               name:"shippingState",
               id:this.id+'shippingState',
               disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
               maxLength:49,
   //            vtype : "alphanum",
               allowNegative:false,
               anchor: '98%'
           });
        }
        
        this.shippingCountry = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Country"),
            name:"shippingCountry",
            id:this.id+'shippingCountry',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength:49,
//            vtype : "alphanum",
            allowNegative:false,
            anchor: '98%'
        });
        
        this.shippingPostal = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.PostalCode"),
            name:"shippingPostal",
            id:this.id+'shippingPostal',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength:50,
//            xtype : "numberfield",
            allowNegative:false,
            anchor: '98%'
        });
        
        this.shippingPhone= new Wtf.form.TextField({
           fieldLabel:WtfGlobal.getLocaleText("acc.address.Phone"), 
           name: 'shippingPhone',
           id:this.id+'shippingPhone',
           disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
           maxLength:250,
           anchor: '98%'
        });
        
        this.shippingMobile= new Wtf.form.TextField({
           fieldLabel:WtfGlobal.getLocaleText("acc.address.Mobile"), 
           name: 'shippingMobile',
           id:this.id+'shippingMobile',
           disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
           maxLength:250,
           anchor: '98%'
        });
        
        this.shippingFax=new Wtf.form.TextField({
           fieldLabel:WtfGlobal.getLocaleText("acc.cust.fax"),
           name:'shippingFax',
           id:this.id+'shippingFax',
           maxLength:250,
           disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
           anchor: '98%'
        });
        
        this.shippingEmail= new Wtf.form.TextArea({
           fieldLabel: WtfGlobal.getLocaleText("acc.cust.email"),  
           name: 'shippingEmail',
           id:this.id+'shippingEmail',
           allowBlank:true,
           maxLength:254,
           disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
           anchor: '98%',
            validator:WtfGlobal.validateMultipleEmail
        });
        
         this.messagePanelShipping = new Wtf.Panel({
            xtype: 'panel', border: false,
            cls: 'emailfieldInfoInContactDetails',
            html: WtfGlobal.getLocaleText("acc.mail.seperator.comma")
        });
        this.shippingRecipientName=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.RecipientName"),
            name: 'shippingRecipientName',
            id:this.id+'shippingRecipientName',
            maxLength:200,
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            anchor: '98%'
        });
        
        this.shippingContactPerson=new Wtf.form.TextField({
           fieldLabel:WtfGlobal.getLocaleText("acc.address.ContactPerson"),
           name: 'shippingContactPerson',
           id:this.id+'shippingContactPerson',
           disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
           maxLength:200,
           anchor: '98%'
        });
        
        this.shippingContactNumber= new Wtf.form.TextField({
           fieldLabel:WtfGlobal.getLocaleText("acc.address.ContactPersonNumber"),
           name: 'shippingContactPersonNumber',
           id:this.id+'shippingContactNumber',
           disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
           maxLength:250,
           anchor: '98%'
         });
         this.shippingContactDesignation= new Wtf.form.TextField({
           fieldLabel:WtfGlobal.getLocaleText("acc.address.ContactPersonDesignation"),
           name: 'shippingContactPersonDesignation',
           id:this.id+'shippingContactDesignation',
           disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
           maxLength:250,
           anchor: '98%'
         });
        this.shippingWebsite = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.website"),
            name: 'shippingWebsite',
            id: this.id + 'shippingWebsite',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength: 250,
            anchor: '98%'
        }); 
        this.shippingRoute= new Wtf.form.ComboBox({
            fieldLabel:'Route',
            name: 'shippingRoute',
            id:this.id+'shippingroute',            
            store:Wtf.ShippingRouteStore,
            valueField:'id',
            displayField:'name',
            mode:'local',
            typeAhead:true,
            triggerAction:'all',
            anchor: '98%',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            hideLabel: !this.isCustomer,
            hidden: !this.isCustomer
        });        
       this.billingAddrsCombo = new Wtf.form.FnComboBox({
            triggerAction:'all',
            mode: 'local',
            name:'billingAddrsCombo',
            valueField:'aliasName',
            displayField:'aliasName',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            store:this.billingAddrsStore,
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectBillingAddress..."), 
            fieldLabel: WtfGlobal.getLocaleText("acc.field.BillingAddress*"),
            anchor: '98%',
            typeAhead: true,
            forceSelection: true,
            btnToolTip:WtfGlobal.getLocaleText("acc.address.addbutton.tooltip"),
            addCreateNewRecord:false
        });
        
        this.shippingAddrsCombo= new Wtf.form.FnComboBox({
            triggerAction:'all',
            name:'shippingAddrsCombo',
            mode: 'local',
            valueField:'aliasName',
            displayField:'aliasName',
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectShippingAddress..."),  
            store:this.ShippingAddrsStore,            
            fieldLabel: WtfGlobal.getLocaleText("acc.field.ShippingAddress*"),
            anchor: '98%',
            typeAhead: true,
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            forceSelection: true,
            btnToolTip:WtfGlobal.getLocaleText("acc.address.addbutton.tooltip"),
            addCreateNewRecord:false
        });
        
        this.billing=new Wtf.form.FieldSet({
            title:(this.isdropshipDocument? WtfGlobal.getLocaleText("acc.address.companyBillingAddress") :(this.isCustomer || Wtf.account.companyAccountPref.isAddressFromVendorMaster)?WtfGlobal.getLocaleText("acc.cust.billingadd"):WtfGlobal.getLocaleText("acc.address.companyBillingAddress")),
            id:this.id+'billing',
            bodyStyle:'padding:5px',
            autoHeight:true,
            width : this.fieldSetWidht,
            items: [this.billingAddrsCombo, this.billingAddress, this.billingCounty, this.billingCity, this.billingState, this.billingCountry, this.billingPostal, this.billingPhone, this.billingMobile, this.billingFax, this.billingEmail, this.messagePanelBilling, this.billingRecipientName, this.billingContactPerson, this.billingContactNumber, this.billingContactDesignation, this.billingWebsite]
        });
        
        /*---Address Fieldset for dropship type Doc----  */
        if (this.isdropshipDocument){
            this.vendorbilling = new Wtf.form.FieldSet({
                title: WtfGlobal.getLocaleText("acc.address.vendorBillingAddress"),
                id: this.id + 'vendorbilling',
                bodyStyle: 'padding:5px',
                autoHeight: true,
                width: this.fieldSetWidht,
                items: [this.dropshipbillingAddrsCombo, this.dropshipbillingAddress, this.dropshipbillingCounty, this.dropshipbillingCity, this.dropshipbillingState, this.dropshipbillingCountry, this.dropshipbillingPostal, this.dropshipbillingPhone, this.dropshipbillingMobile, this.dropshipbillingFax, this.dropshipbillingEmail, this.dropshipmessagePanelBilling, this.dropshipbillingRecipientName, this.dropshipbillingContactPerson, this.dropshipbillingContactNumber, this.dropshipbillingContactDesignation, this.dropshipbillingWebsite]
            });
        }
        /**
          * For india country and vendor transactions and isAddressFromVendorMaster 
          *  is off then create vendor billing address fieldset
          */
        if (WtfGlobal.isIndiaCountryAndGSTApplied() && !this.isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster){
            this.vendorbillingaddressFieldSet = new Wtf.form.FieldSet({
                title: WtfGlobal.getLocaleText("acc.address.vendorBillingAddress"),
                id: this.id + 'vendorbillingaddress_india',
                bodyStyle: 'padding:5px',
                autoHeight: true,
                width: this.fieldSetWidht,
                items: [this.vendorbillingAddrsCombo, this.vendorbillingAddress, this.vendorbillingCounty, this.vendorbillingCity, this.vendorbillingState, this.vendorbillingCountry, this.vendorbillingPostal, this.vendorbillingPhone, this.vendorbillingMobile, this.vendorbillingFax, this.vendorbillingEmail, this.vendormessagePanelBilling, this.vendorbillingRecipientName, this.vendorbillingContactPerson, this.vendorbillingContactNumber, this.vendorbillingContactDesignation, this.vendorbillingWebsite]
            });
        }
                    
        this.shipping=new Wtf.form.FieldSet({
            title: (this.isCustomer || Wtf.account.companyAccountPref.isAddressFromVendorMaster)?WtfGlobal.getLocaleText("acc.cust.shipAdd"):WtfGlobal.getLocaleText("acc.address.companyShippingAddress"),
            id:this.id+'shipping',
            bodyStyle:'padding:5px',
            autoHeight:true,
            width : this.fieldSetWidht,
            items: [this.shippingAddrsCombo, this.shippingAddress, this.shippingCounty, this.shippingCity, this.shippingState, this.shippingCountry, this.shippingPostal, this.shippingPhone, this.shippingMobile, this.shippingFax, this.shippingEmail, this.messagePanelShipping, this.shippingRecipientName, this.shippingContactPerson, this.shippingContactNumber, this.shippingContactDesignation, this.shippingWebsite, this.shippingRoute]
        });
        
        var itemArray = [];
        var columnWidth = 0.33;
       
       /*----Form for dropship type doc----  */
        if (this.isdropshipDocument) {

            columnWidth = 0.33;
            itemArray.push({
                layout: 'form',
                bodyStyle: (this.linkedDocumentCombo.hidden == true) ? 'margin-top:38px' : 'margin-top:0px',
                columnWidth: columnWidth,
                items: [this.linkedDocumentCombo, this.billing]
            }, {
                layout: 'form',
                columnWidth: columnWidth,
                items: [this.copyAddress,this.vendorbilling]
            });
            if (this.showCustomerShippingWin) {
                itemArray.push({
                    layout: 'form',
                    bodyStyle: 'margin-top:38px',
                    columnWidth: columnWidth,
                    items: [this.customerShipping]
                });
            }
            this.addressDetailForm = new Wtf.form.FormPanel({
                border: false,
                bodyStyle: 'margin:5px 5px 5px 5px',
                autoHeight: true,
                autoWidth: true,
                items: [{
                        layout: 'form',
                        baseCls: 'northFormFormat',
                        labelWidth: 110,
                        cls: "visibleDisabled",
                        items: [{
                                layout: 'column',
                                border: false,
                                defaults: {
                                    border: false
                                },
                                items: itemArray
                            }]
                    }]
            });



        } else if (this.isCustomer || Wtf.account.companyAccountPref.isAddressFromVendorMaster) {
            columnWidth = 0.50;
            if (this.showCustomerShippingWin) {
                columnWidth = 0.33;
            }
            
            itemArray.push({
                layout: 'form',
                bodyStyle: (this.linkedDocumentCombo.hidden == true) ? 'margin-top:38px' : 'margin-top:0px',
                columnWidth: columnWidth,
                items: [this.linkedDocumentCombo, this.billing]
            }, {
                layout: 'form',
                columnWidth: columnWidth,
                items: [this.copyAddress, this.shipping]
            });
            if (this.showCustomerShippingWin) {
                itemArray.push({
                    layout: 'form',
                    bodyStyle: 'margin-top:38px',
                    columnWidth: columnWidth,
                    items: [this.customerShipping]
                });
            }
            this.addressDetailForm = new Wtf.form.FormPanel({
                border: false,
                bodyStyle: 'margin:5px 5px 5px 5px',
                autoHeight: true,
                autoWidth: true,
                items: [{
                        layout: 'form',
                        baseCls: 'northFormFormat',
                        labelWidth: 110,
                        cls: "visibleDisabled",
                        items: [{
                                layout: 'column',
                                border: false,
                                defaults: {
                                    border: false
                                },
                                items: itemArray
                            }]
                    }]
            });
        } else {
            columnWidth=0.33;
            if (this.showCustomerShippingWin) {
                columnWidth=0.25;
            }
            /**
             * For india country and vendor transactions and isAddressFromVendorMaster 
             *  is off then create vendor billing address visible in seperate fieldset.
             */
            if(WtfGlobal.isIndiaCountryAndGSTApplied() && !this.isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster){
                columnWidth = 0.25;
                if (this.showCustomerShippingWin) {
                   columnWidth = 0.20;
                }
                itemArray.push({// For IDNIA Country Show Vendor Address if "Wtf.account.companyAccountPref.isAddressFromVendorMaster" check is OFF
                layout: 'form',
                bodyStyle: (this.linkedDocumentCombo.hidden == true) ? 'margin-top:38px' : 'margin-top:0px',
                columnWidth: columnWidth,
                items: [this.linkedDocumentCombo, this.billing]
                }, {
                    layout: 'form',
                    columnWidth: columnWidth,
                    items: [this.copyAddress, this.shipping]
                }, {
                    layout: 'form',
                    columnWidth: columnWidth,
                    bodyStyle: 'margin-top:38px',
                    items: [this.vendorbillingaddressFieldSet]
                }, {
                    layout: 'form',
                    bodyStyle: 'margin-top:38px',
                    columnWidth: columnWidth,
                    items: [this.vendorShipping]
                });
            }else{
                itemArray.push({
                    layout: 'form',
                    bodyStyle: (this.linkedDocumentCombo.hidden == true) ? 'margin-top:38px' : 'margin-top:0px',
                    columnWidth: columnWidth,
                    items: [this.linkedDocumentCombo, this.billing]
                }, {
                    layout: 'form',
                    columnWidth: columnWidth,
                    items: [this.copyAddress, this.shipping]
                }, {
                    layout: 'form',
                    bodyStyle: 'margin-top:38px',
                    columnWidth: columnWidth,
                    items: [this.vendorShipping]
                });
            }
            if (this.showCustomerShippingWin) {
                itemArray.push({
                    layout: 'form',
                    bodyStyle: 'margin-top:38px',
                    columnWidth: columnWidth,
                    items: [this.customerShipping]
                });
            }
                        
            this.addressDetailForm=new Wtf.form.FormPanel({
                border:false,
                bodyStyle:'margin:5px 5px 5px 5px',
                autoHeight:true,
                autoWidth:true,
                items:[{
                    layout:'form',
                    baseCls:'northFormFormat',
                    labelWidth:110,
                    cls:"visibleDisabled",
                    autoScroll:true,
                    items:[{
                        layout:'column',
                        border:false,
                        defaults:{
                            border:false
                        },
                        items:itemArray
                    }]
                }]
            });  
        }    
    },
    createVendorShippingFields:function(){
        this.isShipping=CompanyPreferenceChecks.getGSTCalCulationType();
        this.vendorShippingAddress = new Wtf.form.TextArea({
            fieldLabel:WtfGlobal.getLocaleText("acc.userAdmin.Address"),
            name:"vendorShippingAddress",
            id:this.id+'vendorShippingAddress',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength:250,
            height:60,
            anchor: '98%'
        });
        if ( WtfGlobal.isUSCountryAndGSTApplied()) {
            this.vendorShippingCountyComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);
            this.vendorShippingCountyComboStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'county'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.vendorShippingCountyComboRec)
            });
           this.vendorShippingCounty= new Wtf.form.ExtFnComboBox({
                name:"vendorShippingCounty",
                fieldLabel: WtfGlobal.getLocaleText("acc.address.County"),
                store: this.vendorShippingCountyComboStore,
                valueField: 'name',
                displayField: 'name',
                extraFields:'',
//                allowBlank:this.isShipping?false:true,   // Making changes for SDP-13213 - tax not applied at vendor side, so making this field optional. 
                extraComparisionField: 'name',
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode:'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth:true,
                disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields, 
                anchor: '98%'
            });
        } else {

              this.vendorShippingCounty = new Wtf.form.TextField({
                fieldLabel:WtfGlobal.getLocaleText("acc.address.County"), 
                name:"vendorShippingCounty",
                id:this.id+'vendorShippingCounty',
                disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
                maxLength:49,
                allowNegative:false,
                anchor: '98%',
                hidden: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.US),
                hideLabel: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.US)
            });
        }
      
         if ( WtfGlobal.isUSCountryAndGSTApplied()) {
            this.vendorShippingCityComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);
            this.vendorShippingCityComboStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'city'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.vendorShippingCityComboRec)
            });
           this.vendorShippingCity= new Wtf.form.ExtFnComboBox({
                name:"vendorShippingCity",
                fieldLabel: WtfGlobal.getLocaleText("acc.address.City"),
                store: this.vendorShippingCityComboStore,
                valueField: 'name',
                displayField: 'name',
                extraFields:'',
//                allowBlank:this.isShipping?false:true, // Making changes for SDP-13213 - tax not applied at vendor side, so making this field optional. 
                extraComparisionField: 'name',
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode:'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth:true,
                disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
                anchor: '98%'
            });
        } else {

         this.vendorShippingCity = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.City"), 
            name:"vendorShippingCity",
            id:this.id+'vendorShippingCity',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength:49,
            allowNegative:false,
            anchor: '98%'
        });
        }
       
        
        if (( WtfGlobal.isIndiaCountryAndGSTApplied() && this.stateAsComboFlag)||  WtfGlobal.isUSCountryAndGSTApplied()) {
        this.vendorShippingStateComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);

            this.vendorShippingComboStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'state'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.vendorShippingStateComboRec)
            });

            this.vendorShippingState= new Wtf.form.ExtFnComboBox({
                name:"vendorShippingState",
                fieldLabel: WtfGlobal.getLocaleText("acc.address.State"),
                store: this.vendorShippingComboStore,
                valueField: 'name',
                displayField: 'name',
                extraFields:'',
                allowBlank: (Wtf.account.companyAccountPref.countryid == Wtf.Country.US) ? true : this.isShipping ? false : true,   //// Making changes for SDP-13213 - tax not applied at vendor side, so making this field optional. 
                extraComparisionField: 'name',
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode:'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth:true,
                disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
                anchor: '98%'
            });
        } else {

            this.vendorShippingState = new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText("acc.address.State"),
                name:"vendorShippingState",
                id:this.id+'vendorShippingState',
                disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
                maxLength:49,
                allowNegative:false,
                anchor: '98%'
            });
        }
        
        this.vendorShippingCountry = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Country"),
            name:"vendorShippingCountry",
            id:this.id+'vendorShippingCountry',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength:49,
            allowNegative:false,
            anchor: '98%'
        });
        
        this.vendorShippingPostal = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.PostalCode"),
            name:"vendorShippingPostal",
            id:this.id+'vendorShippingPostal',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength:50,
            allowNegative:false,
            anchor: '98%'
        });
        
        this.vendorShippingPhone= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.Phone"), 
            name: 'vendorShippingPhone',
            id:this.id+'vendorShippingPhone',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength:250,
            anchor: '98%'
        });
        
        this.vendorShippingMobile= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.Mobile"), 
            name: 'vendorShippingMobile',
            id:this.id+'vendorShippingMobile',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength:250,
            anchor: '98%'
        });
        
        this.vendorShippingFax=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.fax"),
            name:'vendorShippingFax',
            id:this.id+'vendorShippingFax',
            maxLength:250,
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            anchor: '98%'
        });
        
        this.vendorShippingEmail= new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.cust.email"),  
            name: 'vendorShippingEmail',
            id:this.id+'vendorShippingEmail',
            allowBlank:true,
            maxLength:254,
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            anchor: '98%',
            validator:WtfGlobal.validateMultipleEmail
        });
        
        this.vendorShippingRecipientName=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.RecipientName"),
            name: 'vendorShippingRecipientName',
            id:this.id+'vendorShippingRecipientName',
            maxLength:200,
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            anchor: '98%'
        });
        
        this.vendorShippingContactPerson=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.ContactPerson"),
            name: 'vendorShippingContactPerson',
            id:this.id+'vendorShippingContactPerson',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength:200,
            anchor: '98%'
        });
        
        this.vendorShippingContactNumber= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.ContactPersonNumber"),
            name: 'vendorShippingContactNumber',
            id:this.id+'vendorShippingContactNumber',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength:250,
            anchor: '98%'
        });  
        this.vendorShippingContactDesignation = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.ContactPersonDesignation"),
            name: 'vendorShippingContactDesignation',
            id: this.id + 'vendorShippingContactDesignation',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength: 250,
            anchor: '98%'
        }); 
        
        this.vendorShippingWebsite= new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.website"),
            name: 'vendorShippingWebsite',
            id: this.id + 'vendorShippingWebsite',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength: 250,
            anchor: '98%'
        }); 
        
        this.vendorShippingAddrsCombo= new Wtf.form.FnComboBox({
            triggerAction:'all',
            name:'vendorShippingAddrsCombo',
            mode: 'local',
            valueField:'aliasName',
            displayField:'aliasName',
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectShippingAddress..."),  
            store:this.vendorShippingAddrsStore,            
            fieldLabel: WtfGlobal.getLocaleText("acc.field.ShippingAddress*"),
            anchor: '98%',
            typeAhead: true,
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            forceSelection: true,
            btnToolTip:WtfGlobal.getLocaleText("acc.address.addbutton.tooltip"),
            addCreateNewRecord:false
        });
         this.messagePanelVendorShipping = new Wtf.Panel({
            xtype: 'panel',
            border: false,
            cls: 'emailfieldInfoInContactDetails',
            html: WtfGlobal.getLocaleText("acc.mail.seperator.comma")
        });
        
        this.vendorShipping=new Wtf.form.FieldSet({
            title: (WtfGlobal.isIndiaCountryAndGSTApplied() && !this.isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster && !this.isdropshipDocument) ? WtfGlobal.getLocaleText("acc.address.vendorShippingAddress") : WtfGlobal.getLocaleText("acc.cust.shipAdd"),
            id:this.id+'vendorShipping',
            bodyStyle:'padding:5px',
            autoHeight:true,
            width : this.fieldSetWidht,
            items: [this.vendorShippingAddrsCombo, this.vendorShippingAddress, this.vendorShippingCounty, this.vendorShippingCity, this.vendorShippingState, this.vendorShippingCountry, this.vendorShippingPostal, this.vendorShippingPhone, this.vendorShippingMobile, this.vendorShippingFax, this.vendorShippingEmail, this.messagePanelVendorShipping, this.vendorShippingRecipientName, this.vendorShippingContactPerson, this.vendorShippingContactNumber, this.vendorShippingContactDesignation, this.vendorShippingWebsite]
        });
    },
    createCustomerShippingFields:function(){
       this.isShipping = CompanyPreferenceChecks.getGSTCalCulationType();
       this.customerShippingAddress = new Wtf.form.TextArea({
            fieldLabel:WtfGlobal.getLocaleText("acc.customer.address"),
            name:"customerShippingAddress",
            id:this.id+'customerShippingAddress',
            maxLength:250,
            height:60,
            disabled: this.isViewTemplate,
            allowBlank:false,
            allowNegative:false,
            anchor: '98%'
        });
        if ( WtfGlobal.isUSCountryAndGSTApplied()) {
            this.customerShippingCountyComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);
            this.customerShippingCountyComboStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'county'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.customerShippingCountyComboRec)
            });
            this.customerShippingCounty = new Wtf.form.ExtFnComboBox({
                name: "customerShippingCounty",
                fieldLabel: WtfGlobal.getLocaleText("acc.address.County"),
                store: this.customerShippingCountyComboStore,
                valueField: 'name',
                displayField: 'name',
                extraFields: '',
                allowBlank:this.isShipping?false:true,
                extraComparisionField: 'name',
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode: 'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth: true,
                disabled: this.isViewTemplate,
                anchor: '98%'
            });
        } else {

            this.customerShippingCounty = new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText("acc.address.County"),
                name: "customerShippingCounty",
                id: this.id + 'customerShippingCounty',
                maxLength: 49,
                allowNegative: false,
                disabled: this.isViewTemplate,
                anchor: '98%',
                hidden: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.US),
                hideLabel: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.US)
            });
        }
        if ( WtfGlobal.isUSCountryAndGSTApplied() ) {
            this.customerShippingCityComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);
            this.customerShippingCityComboStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'city'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.customerShippingCityComboRec)
            });
              this.customerShippingCity = new Wtf.form.ExtFnComboBox({
                name: "customerShippingCity",
                fieldLabel: WtfGlobal.getLocaleText("acc.address.City"),
                store: this.customerShippingCityComboStore,
                valueField: 'name',
                displayField: 'name',
                extraFields: '',
                allowBlank:this.isShipping?false:true,
                extraComparisionField: 'name',
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode: 'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth: true,
                disabled: this.isViewTemplate,
                anchor: '98%'
            });
        } else {

            this.customerShippingCity = new Wtf.form.TextField({
                fieldLabel:WtfGlobal.getLocaleText("acc.address.City"),
                name:"customerShippingCity",
                id:this.id+'customerShippingCity',
                maxLength:49,
                allowNegative:false,
                disabled: this.isViewTemplate,
                anchor: '98%'
            });
        }
       
           if (( WtfGlobal.isIndiaCountryAndGSTApplied() && this.stateAsComboFlag)||  WtfGlobal.isUSCountryAndGSTApplied()) {
            this.customerShippingStateComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);

            this.customerShippingComboStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'state'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.customerShippingStateComboRec)
            });

            this.customerShippingState = new Wtf.form.ExtFnComboBox({
                name: "customerShippingState",
                fieldLabel: WtfGlobal.getLocaleText("acc.address.State"),
                store: this.customerShippingComboStore,
                valueField: 'name',
                displayField: 'name',
                extraFields: '',
                extraComparisionField: 'name',
                allowBlank:this.isShipping?false:true,
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode: 'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth: true,
                disabled: this.isViewTemplate,
                anchor: '98%'
            });
        } else {

            this.customerShippingState = new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText("acc.address.State"),
                name:"customerShippingState",
                id:this.id+'customerShippingState',
                maxLength:49,
                allowNegative:false,
                disabled: this.isViewTemplate,
                anchor: '98%'
        });
        }
        this.customerShippingCountry = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Country"),
            name:"customerShippingCountry",
            id:this.id+'customerShippingCountry',
            maxLength:49,
            allowNegative:false,
            disabled: this.isViewTemplate,
            anchor: '98%'
        });
        
        this.customerShippingPostal = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.PostalCode"),
            name:"customerShippingPostal",
            id:this.id+'customerShippingPostal',
            maxLength:50,
            allowNegative:false,
            disabled: this.isViewTemplate,
            anchor: '98%'
        });
        
        this.customerShippingPhone= new Wtf.form.TextField({
           fieldLabel:WtfGlobal.getLocaleText("acc.address.Phone"), 
           name: 'customerShippingPhone',
           id:this.id+'customerShippingPhone',
           disabled: this.isViewTemplate,
           maxLength:250,
           anchor: '98%'
        });
        
        this.customerShippingMobile= new Wtf.form.TextField({
           fieldLabel:WtfGlobal.getLocaleText("acc.address.Mobile"), 
           name: 'customerShippingMobile',
           id:this.id+'customerShippingMobile',
           disabled: this.isViewTemplate,
           maxLength:250,
           anchor: '98%'
        });
        
        this.customerShippingFax=new Wtf.form.TextField({
           fieldLabel:WtfGlobal.getLocaleText("acc.cust.fax"),
           name:'customerShippingFax',
           id:this.id+'customerShippingFax',
           disabled: this.isViewTemplate,
           maxLength:250,
           anchor: '98%'
        });
        
        this.customerShippingEmail= new Wtf.form.TextArea({
           fieldLabel: WtfGlobal.getLocaleText("acc.cust.email"),  
           name: 'customerShippingEmail',
           id:this.id+'customerShippingEmail',
           allowBlank:true,
           maxLength:254,
           disabled: this.isViewTemplate,  
           anchor: '98%',
           validator:WtfGlobal.validateMultipleEmail
        });
        
         this.messagePanelCustomerShipping = new Wtf.Panel({
            xtype: 'panel', border: false,
            cls: 'emailfieldInfoInContactDetails',
            html: WtfGlobal.getLocaleText("acc.mail.seperator.comma")
        });
        this.customerShippingRecipientName=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.RecipientName"),
            name: 'customerShippingRecipientName',
            id:this.id+'customerShippingRecipientName',
            maxLength:200,
            disabled: this.isViewTemplate,
            anchor: '98%'
        });
        
        this.customerShippingContactPerson=new Wtf.form.TextField({
           fieldLabel:WtfGlobal.getLocaleText("acc.address.ContactPerson"),
           name: 'customerShippingContactPerson',
           id:this.id+'customerShippingContactPerson',
           maxLength:200,
           disabled: this.isViewTemplate,
           anchor: '98%'
        });
        
        this.customerShippingContactNumber= new Wtf.form.TextField({
           fieldLabel:WtfGlobal.getLocaleText("acc.address.ContactPersonNumber"),
           name: 'customerShippingContactNumber',
           id:this.id+'customerShippingContactNumber',
           maxLength:250,
           disabled: this.isViewTemplate,
           anchor: '98%'
         });
         this.customerShippingContactDesignation= new Wtf.form.TextField({
           fieldLabel:WtfGlobal.getLocaleText("acc.address.ContactPersonDesignation"),
           name: 'customerShippingContactDesignation',
           id:this.id+'customerShippingContactDesignation',
           maxLength:250,
           disabled: this.isViewTemplate,
           anchor: '98%'
         });
        this.customerShippingWebsite = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.website"),
            name: 'customerShippingWebsite',
            id: this.id + 'customerShippingWebsite',
            maxLength: 250,
            disabled: this.isViewTemplate,
            anchor: '98%'
        }); 
        this.customerShippingRoute= new Wtf.form.ComboBox({
            fieldLabel:'Route',
            name: 'customerShippingRoute',
            id:this.id+'customerShippingRoute',            
            store:Wtf.ShippingRouteStore,
            valueField:'id',
            displayField:'name',
            mode:'local',
            typeAhead:true,
            disabled: this.isViewTemplate,
            triggerAction:'all',
            anchor: '98%',
        });        
        
        this.customerShippingAddrsCombo= new Wtf.form.FnComboBox({
            triggerAction:'all',
            name:'customerShippingAddrsCombo',
            mode: 'local',
            valueField:'aliasName',
            displayField:'aliasName',
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectShippingAddress..."),  
            store:this.customerShippingAddrsStore,            
            fieldLabel: WtfGlobal.getLocaleText("acc.field.ShippingAddress*"),
            anchor: '98%',
            typeAhead: true,
            disabled: this.isViewTemplate,
            forceSelection: true,
            btnToolTip:WtfGlobal.getLocaleText("acc.address.addbutton.tooltip"),
            addCreateNewRecord:false
        });
        this.customerShipping=new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.invoice.customer")+' '+WtfGlobal.getLocaleText("acc.cust.shipAdd"),
            id:this.id+'customerShipping',
            bodyStyle:'padding:5px',
            autoHeight:true,
            width : this.fieldSetWidht,
            items: [this.customerShippingAddrsCombo, this.customerShippingAddress, this.customerShippingCounty, this.customerShippingCounty, this.customerShippingCity, this.customerShippingState, this.customerShippingCountry, this.customerShippingPostal, this.customerShippingPhone, this.customerShippingMobile, this.customerShippingFax, this.customerShippingEmail, this.messagePanelCustomerShipping, this.customerShippingRecipientName, this.customerShippingContactPerson, this.customerShippingContactNumber, this.customerShippingContactDesignation, this.customerShippingWebsite, this.customerShippingRoute]
        });
    },
    
    /*--Fields for dropship type doc---- */
    createVendorBillingFields: function() {
        
        this.dropshipbillingAddress = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.customer.address"),
            name: "dropshipbillingAddress",
            id: this.id + 'vendorbillingAddress',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength: 250,
            height: 60,
            allowBlank: false,
            allowNegative: false,
            anchor: '98%'
        });

        
        
        
        //County specific address field visible only for US
        if ( WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US 
            this.dropshipCountyComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);

            this.dropshipCountyComboStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'county'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.dropshipCountyComboRec)
            });
            this.dropshipbillingCounty= new Wtf.form.ExtFnComboBox({
                name:"dropshipbillingCounty",
                fieldLabel: WtfGlobal.getLocaleText("acc.address.County"),
                store: this.dropshipCountyComboStore,
                valueField: 'name',
                displayField: 'name',
                extraFields:'',
                allowBlank:false,
                extraComparisionField: 'name',
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode:'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth:true,
                disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            });
        } else {

            this.dropshipbillingCounty = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.County"),
            name: "dropshipbillingCounty",
            id: this.id + 'vendorbillingCounty',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength: 49,
            allowNegative: false,
            anchor: '98%',
            hidden: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.US),
            hideLabel: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.US)
        });
      }

        
        
              //City specific address field visible only for US
        if ( WtfGlobal.isUSCountryAndGSTApplied()) {
            this.dropshipCityComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);

            this.dropshipCityComboStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'city'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.dropshipCityComboRec)
            });

            this.dropshipbillingCity= new Wtf.form.ExtFnComboBox({
                name:"dropshipbillingCity",
                fieldLabel: WtfGlobal.getLocaleText("acc.address.City"),
                store: this.dropshipCityComboStore,
                valueField: 'name',
                displayField: 'name',
                extraFields:'',
                allowBlank:false,
                extraComparisionField: 'name',
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode:'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth:true,
                disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields
            });
        } else {

           this.dropshipbillingCity = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.City"),
            name: "dropshipbillingCity",
            id: this.id + 'vendorbillingCity',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength: 49,
            allowNegative: false,
            anchor: '98%'
        });
        }
                
          //State specific address field visible only for US or INDIA
        if ( WtfGlobal.isIndiaCountryAndGSTApplied() ||  WtfGlobal.isUSCountryAndGSTApplied()) {
            this.dropshipStateComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);

            this.dropshipStateComboStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'state'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.dropshipStateComboRec)
            });

            this.dropshipbillingState= new Wtf.form.ExtFnComboBox({
                name:"dropshipbillingState",
                fieldLabel: WtfGlobal.getLocaleText("acc.address.State"),
                store: this.dropshipStateComboStore,
                valueField: 'name',
                displayField: 'name',
                extraFields:'',
                allowBlank:false,
                extraComparisionField: 'name',
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode:'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth:true,
                disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            });
        } else {
         this.dropshipbillingState = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.State"),
            name: "dropshipbillingState",
            id: this.id + 'vendorbillingState',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength: 49,
            allowNegative: false,
            anchor: '98%'
        });
     }

        this.dropshipbillingCountry = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Country"),
            name: "dropshipbillingCountry",
            id: this.id + 'vendorbillingCountry',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength: 49,
            allowNegative: false,
            anchor: '98%'
        });

        this.dropshipbillingPostal = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.PostalCode"),
            name: "dropshipbillingPostal",
            id: this.id + 'vendorbillingPostal',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength: 50,
            allowNegative: false,
            anchor: '98%'
        });

        this.dropshipbillingPhone = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Phone"),
            name: 'dropshipbillingPhone',
            id: this.id + 'vendorbillingPhone',
            maxLength: 50,
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            anchor: '98%'
        });

        this.dropshipbillingMobile = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Mobile"),
            name: 'dropshipbillingMobile',
            id: this.id + 'vendorbillingMobile',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength: 50,
            anchor: '98%'
        });

        this.dropshipbillingFax = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.cust.fax"),
            name: 'dropshipbillingFax',
            id: this.id + 'vendorbillingFax',
            maxLength: 50,
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            anchor: '98%'
        });

        this.dropshipbillingEmail = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.cust.email"),
            name: 'dropshipbillingEmail',
            id: this.id + 'vendorbillingEmail',
            allowBlank: true,
            maxLength:50,
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            validator: WtfGlobal.validateMultipleEmail,
            anchor: '98%'
        });

        this.dropshipmessagePanelBilling = new Wtf.Panel({
            xtype: 'panel',
            border: false,
            cls: 'emailfieldInfoInContactDetails',
            html: WtfGlobal.getLocaleText("acc.mail.seperator.comma")
        });

        this.dropshipbillingRecipientName = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.RecipientName"),
            name: 'dropshipbillingRecipientName',
            id: this.id + 'dropshipbillingRecipientName',
            maxLength:50,
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            anchor: '98%'
        });

        this.dropshipbillingContactPerson = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.ContactPerson"),
            name: 'dropshipbillingContactPerson',
            id: this.id + 'vendorbillingContactPerson',
            maxLength:50,
            allowBlank: (this.moduleid == Wtf.Acc_ConsignmentRequest_ModuleId && this.isCustomer) ? false : true,
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            anchor: '98%'
        });

        this.dropshipbillingContactNumber = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.ContactPersonNumber"),
            name: 'dropshipbillingContactPersonNumber',
            id: this.id + 'vendorbillingContactNumber',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength:50,
            anchor: '98%'
        });
        this.dropshipbillingContactDesignation = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.ContactPersonDesignation"),
            name: 'dropshipbillingContactPersonDesignation',
            id: this.id + 'vendorbillingContactDesignation',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength:50,
            anchor: '98%'
        });
        this.dropshipbillingWebsite = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.website"),
            name: 'dropshipbillingWebsite',
            id: this.id + 'vendorbillingWebsite',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength:50,
            anchor: '98%'
        });

        this.dropshipbillingAddrsCombo = new Wtf.form.FnComboBox({
            triggerAction: 'all',
            mode: 'local',
            name: 'dropshipbillingAddrsCombo',
            valueField: 'aliasName',
            displayField: 'aliasName',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            store: this.vendorbillingAddrsStore,
            emptyText: WtfGlobal.getLocaleText("acc.field.SelectBillingAddress..."),
            fieldLabel: WtfGlobal.getLocaleText("acc.field.BillingAddress*"),
            anchor: '98%',
            typeAhead: true,
            forceSelection: true,
            btnToolTip: WtfGlobal.getLocaleText("acc.address.addbutton.tooltip"),
            addCreateNewRecord: false
        });

    },
    /**
     * For india country and vendor transactions and isAddressFromVendorMaster 
     *  is off then create vendor billing address fields seperate 
     */
    createVendorBiilingAddressFields: function () {
        this.vendorbillingAddress = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.customer.address"),
            name: "vendorbillingAddressForINDIA",
            id: this.id + 'vendorbillingAddressForINDIA',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength: 250,
            height: 60,
            allowBlank: false,
            allowNegative: false,
            anchor: '98%'
        });
        this.vendorbillingCounty = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.County"),
            name: "vendorbillingCountyForINDIA",
            id: this.id + 'vendorbillingCountyForINDIA',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength: 49,
            allowNegative: false,
            anchor: '98%',
            hidden: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.US),
            hideLabel: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.US)
        });
        this.vendorbillingCity = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.City"),
            name: "vendorbillingCityForINDIA",
            id: this.id + 'vendorbillingCityForINDIA',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength: 49,
            allowNegative: false,
            anchor: '98%'
        });
        //State specific address field visible only for INDIA
        if (WtfGlobal.isIndiaCountryAndGSTApplied() && this.stateAsComboFlag) {
            this.vendorStateComboRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}
            ]);

            this.vendorStateComboStore = new Wtf.data.Store({
                url: "AccEntityGST/getFieldComboDataForModule.do",
                baseParams: {
                    moduleid: Wtf.Acc_EntityGST,
                    fieldlable: 'state'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.vendorStateComboRec)
            });

            this.vendorbillingState = new Wtf.form.ExtFnComboBox({
                name: "vendorbillingStateForINDIA",
                fieldLabel: WtfGlobal.getLocaleText("acc.address.State"),
                store: this.vendorStateComboStore,
                valueField: 'name',
                displayField: 'name',
                extraFields: '',
                allowBlank: false,
                extraComparisionField: 'name',
                minChars: 1,
                typeAhead: true,
                triggerAction: 'all',
                mode: 'remote',
                width: 343,
                forceSelection: true,
                hideTrigger: true,
                customListWidth: true,
                disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
                anchor: '98%'
            });
        } else {
            this.vendorbillingState = new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText("acc.address.State"),
                name: "vendorbillingStateForINDIA",
                id: this.id + 'vendorbillingStateForINDIA',
                disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
                maxLength: 49,
                allowNegative: false,
                anchor: '98%'
            });
        }

        this.vendorbillingCountry = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Country"),
            name: "vendorbillingCountryForINDIA",
            id: this.id + 'vendorbillingCountryForINDIA',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength: 49,
            allowNegative: false,
            anchor: '98%'
        });

        this.vendorbillingPostal = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.PostalCode"),
            name: "vendorbillingPostalForINDIA",
            id: this.id + 'vendorbillingPostalForINDIA',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength: 50,
            allowNegative: false,
            anchor: '98%'
        });

        this.vendorbillingPhone = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Phone"),
            name: 'vendorbillingPhoneForINDIA',
            id: this.id + 'vendorbillingPhoneForINDIA',
            maxLength: 50,
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            anchor: '98%'
        });

        this.vendorbillingMobile = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Mobile"),
            name: 'vendorbillingMobileForINDIA',
            id: this.id + 'vendorbillingMobileForINDIA',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength: 50,
            anchor: '98%'
        });

        this.vendorbillingFax = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.cust.fax"),
            name: 'vendorbillingFaxForINDIA',
            id: this.id + 'vendorbillingFaxForINDIA',
            maxLength: 50,
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            anchor: '98%'
        });
        this.vendorbillingEmail = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.cust.email"),
            name: 'vendorbillingEmailForINDIA',
            id: this.id + 'vendorbillingEmailForINDIA',
            allowBlank: true,
            maxLength: 50,
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            validator: WtfGlobal.validateMultipleEmail,
            anchor: '98%'
        });

        this.vendormessagePanelBilling = new Wtf.Panel({
            xtype: 'panel',
            border: false,
            cls: 'emailfieldInfoInContactDetails',
            html: WtfGlobal.getLocaleText("acc.mail.seperator.comma")
        });

        this.vendorbillingRecipientName = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.RecipientName"),
            name: 'vendorbillingRecipientNameForINDIA',
            id: this.id + 'vendorbillingRecipientNameForINDIA',
            maxLength: 50,
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            anchor: '98%'
        });

        this.vendorbillingContactPerson = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.ContactPerson"),
            name: 'vendorbillingContactPersonForINDIA',
            id: this.id + 'vendorbillingContactPersonForINDIA',
            maxLength: 50,
            allowBlank: (this.moduleid == Wtf.Acc_ConsignmentRequest_ModuleId && this.isCustomer) ? false : true,
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            anchor: '98%'
        });

        this.vendorbillingContactNumber = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.ContactPersonNumber"),
            name: 'vendorbillingContactPersonNumberForINDIA',
            id: this.id + 'vendorbillingContactNumberForINDIA',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength: 50,
            anchor: '98%'
        });
        this.vendorbillingContactDesignation = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.ContactPersonDesignation"),
            name: 'vendorbillingContactPersonDesignationForINDIA',
            id: this.id + 'vendorbillingContactDesignationForINDIA',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength: 50,
            anchor: '98%'
        });
        this.vendorbillingWebsite = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.website"),
            name: 'vendorbillingWebsiteForINDIA',
            id: this.id + 'vendorbillingWebsiteForINDIA',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            maxLength: 50,
            anchor: '98%'
        });
        this.vendorbillingAddrsCombo = new Wtf.form.FnComboBox({
            triggerAction: 'all',
            mode: 'local',
            name: 'vendorbillingAddrsComboForINDIA',
            valueField: 'aliasName',
            displayField: 'aliasName',
            disabled: this.viewGoodReceipt || this.isViewTemplate || this.disabledAddressFields,
            store: this.vendorbillingAddrsStore,
            emptyText: WtfGlobal.getLocaleText("acc.field.SelectBillingAddress..."),
            fieldLabel: WtfGlobal.getLocaleText("acc.field.BillingAddress*"),
            anchor: '98%',
            typeAhead: true,
            forceSelection: true,
            btnToolTip: WtfGlobal.getLocaleText("acc.address.addbutton.tooltip"),
            addCreateNewRecord: false
        });
    },
    loadRecord:function(){
        this.billingAddrsStore.load();
        this.ShippingAddrsStore.load();
        
        if (this.isdropshipDocument) {
            this.vendorbillingAddrsStore.load();
        }else if (WtfGlobal.isIndiaCountryAndGSTApplied() && !this.isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster) {
            this.vendorbillingAddrsStore.load();
        }
        
        if(!this.isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster){
            this.vendorShippingAddrsStore.load();
        }
        if (this.showCustomerShippingWin) {
            this.customerShippingAddrsStore.load();
        }
        if(this.moduleid==Wtf.Acc_ConsignmentDeliveryOrder_ModuleId || this.moduleid==Wtf.Acc_Consignment_GoodsReceiptOrder_ModuleId){
            this.docStore.load();
        }
    },
    closeWindow:function(){
      this.close();
    },
    saveData:function(){
        if(!this.addressDetailForm.getForm().isValid()){
           WtfComMsgBox(2,2);
           return; 
        } else{
            this.currentaddress=this.addressDetailForm.getForm().getValues();
            this.currentaddress.shippingRoute=this.shippingRoute.getValue(); 
            if (this.isdropshipDocument) {
                this.currentaddress.isdropshipDocument = true;//Sending parameter to GST calculation Purpose
            }
            if (this.showCustomerShippingWin) {
             this.currentaddress.customerShippingRoute=this.customerShippingRoute.getValue();    
            }
            this.currentaddress.linkedDocumentCombo=this.linkedDocumentCombo.getValue();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),WtfGlobal.getLocaleText("acc.field.Youhavesuccessfullyaddedyouraddressdetail")],3);
            this.fireEvent("update",this);
            this.close();
        }
    },
   copyBillingAddress:function(checkBox,value){
        if(value==true){
            this.shippingAddress.setValue(this.billingAddress.getValue());
            if ( WtfGlobal.isUSCountryAndGSTApplied()) {
                /*
                 * Here I have changes getValue() to getRawValue() so that if values are not provided for billing address fields, same will be reflected at shipping address.
                 */
                this.shippingCounty.setValForRemoteStore(this.billingCounty.getRawValue(), this.billingCounty.getRawValue()); //cpoying values from billing address fields to shipping address fields
            }
            if ( WtfGlobal.isUSCountryAndGSTApplied()) {
                this.shippingCity.setValForRemoteStore(this.billingCity.getRawValue(), this.billingCity.getRawValue());
            } else {
                this.shippingCity.setValue(this.billingCity.getValue());
            }
            if ((WtfGlobal.isIndiaCountryAndGSTApplied() && this.stateAsComboFlag) ||  WtfGlobal.isUSCountryAndGSTApplied()) {
                this.shippingState.setValForRemoteStore(this.billingState.getRawValue(), this.billingState.getRawValue());
            } else {
                this.shippingState.setValue(this.billingState.getValue());
            }
            this.shippingCountry.setValue(this.billingCountry.getValue());
            this.shippingPostal.setValue(this.billingPostal.getValue());
            this.shippingPhone.setValue(this.billingPhone.getValue());
            this.shippingMobile.setValue(this.billingMobile.getValue());
            this.shippingFax.setValue(this.billingFax.getValue());
            this.shippingEmail.setValue(this.billingEmail.getValue());
            this.shippingRecipientName.setValue(this.billingRecipientName.getValue());
            this.shippingContactPerson.setValue(this.billingContactPerson.getValue());
            this.shippingContactNumber.setValue(this.billingContactNumber.getValue()); 
            this.shippingContactDesignation.setValue(this.billingContactDesignation.getValue());
            this.shippingWebsite.setValue(this.billingWebsite.getValue());
        } else if(value==false){            
            this.shippingAddress.reset();
            this.shippingCounty.reset();
            this.shippingCity.reset();
            this.shippingState.reset();
            this.shippingCountry.reset();
            this.shippingPostal.reset();
            this.shippingPhone.reset();
            this.shippingMobile.reset();
            this.shippingFax.reset();
            this.shippingEmail.reset();
            this.shippingRecipientName.reset();
            this.shippingContactPerson.reset();
            this.shippingContactNumber.reset();
            this.shippingContactDesignation.reset();
            this.shippingWebsite.reset();
        } 
    },
    setBillingAddressDataOnSelect:function(combo,rec){
        if(combo.getValue()==this.billingComboValueBeforeSelect){ //If same name selected no need to do any action 
            return;
        }
        if(this.currentaddress!="" && this.currentaddress.billingAddrsCombo==rec.data.aliasName){
            this.setBillingAddress(this.currentaddress); 
        } else {
            this.setDefaultBillingAddress(rec.data); 
        }       
    },
    
    
    setVendorBillingAddressDataOnSelect: function(combo, rec) {
        if (combo.getValue() == this.billingComboValueBeforeSelect) { //If same name selected no need to do any action 
            return;
        }
        if (this.currentaddress != "" && this.currentaddress.billingAddrsCombo == rec.data.aliasName) {
            this.setVendorBillingAddress(this.currentaddress);
        } else {
            this.setDefaultVendorBillingAddress(rec.data);
        }
    },
    /**
     * For india country and vendor transactions and isAddressFromVendorMaster 
     *  is off then vendor billing address visible in seperate fieldset, Set this fieldset fields data
     */
    setVendorBillingAddressDataOnSelectINDIA: function(combo, rec) {
        if (combo.getValue() == this.vendorbillingComboValueBeforeSelect) { //If same name selected no need to do any action 
            return;
        }
        if (this.currentaddress != "" && this.currentaddress.vendorbillingAddrsComboForINDIA == rec.data.aliasName) {
            this.setVendorBillingAddressINDIA(this.currentaddress);
        } else {
            this.setDefaultVendorBillingAddressINDIA(rec.data);
        }
    },
    
    setShippingAddressDataOnSelect:function(combo,rec){
        if(combo.getValue()==this.shippingComboValueBeforeSelect){ //If same name selected no need to do any action 
            return;
        }
        if(this.currentaddress!="" && this.currentaddress.shippingAddrsCombo==rec.data.aliasName){
            this.setShippingAddress(this.currentaddress);
        } else{
            this.setDefaultShippingAddress(rec.data);
        }        
    },
    
    setVendorShippingAddressDataOnSelect:function(combo,rec){
        if(combo.getValue()==this.vendorShippingComboValueBeforeSelect){ //If same name selected no need to do any action 
            return;
        }
        if(this.currentaddress!="" && this.currentaddress.vendorShippingAddrsCombo==rec.data.aliasName){
            this.setVendorShippingAddress(this.currentaddress);
        } else{
            this.setVendorDefaultShippingAddress(rec.data);
        }        
    },
    setCustomerShippingAddressDataOnSelect:function(combo,rec){
        if(combo.getValue()==this.customerShippingComboValueBeforeSelect){ //If same name selected no need to do any action 
            return;
        }
        if(this.currentaddress!=="" && this.currentaddress.customerShippingAddrsCombo===rec.data.aliasName){
            this.setCustomerShippingAddress(this.currentaddress);
        } else{
            this.setCustomerDefaultShippingAddress(rec.data);
        }        
    },
    
    setShippingAddress:function(addr){
        this.shippingAddress.setValue(addr.shippingAddress);
        if ( WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US 
            this.shippingCounty.setValForRemoteStore(addr.shippingCounty, addr.shippingCounty);//setting default values from record to fields
        }
        if ( WtfGlobal.isUSCountryAndGSTApplied()) {   // Check wheather country is US 
            this.shippingCity.setValForRemoteStore(addr.shippingCity, addr.shippingCity);
        } else {
            this.shippingCity.setValue(addr.shippingCity);
        }
        if ((WtfGlobal.isIndiaCountryAndGSTApplied() && this.stateAsComboFlag)||  WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US or INDIA
            this.shippingState.setValForRemoteStore(addr.shippingState, addr.shippingState);
        } else {
            this.shippingState.setValue(addr.shippingState);
        }
        this.shippingCountry.setValue(addr.shippingCountry);
        this.shippingPostal.setValue(addr.shippingPostal);
        this.shippingPhone.setValue(addr.shippingPhone);
        this.shippingMobile.setValue(addr.shippingMobile);
        this.shippingFax.setValue(addr.shippingFax);
        this.shippingEmail.setValue(addr.shippingEmail);
        this.shippingRecipientName.setValue(addr.shippingRecipientName);
        this.shippingContactPerson.setValue(addr.shippingContactPerson);
        this.shippingContactNumber.setValue(addr.shippingContactPersonNumber);
        this.shippingContactDesignation.setValue(addr.shippingContactPersonDesignation);
        this.shippingWebsite.setValue(addr.shippingWebsite);
        this.shippingRoute.setValue(addr.shippingRoute);
    },
    
    setVendorShippingAddress:function(addr){
        this.vendorShippingAddress.setValue(addr.vendorShippingAddress);
        if ( WtfGlobal.isUSCountryAndGSTApplied()) {  // Check wheather country is US 
            this.vendorShippingCounty.setValForRemoteStore(addr.vendorShippingCounty, addr.vendorShippingCounty); //for setting vendor shipping address from record 
        } else {
            this.vendorShippingCounty.setValue(addr.vendorShippingCounty);
        }
        if ( WtfGlobal.isUSCountryAndGSTApplied()) {   // Check wheather country is US 
            this.vendorShippingCity.setValForRemoteStore(addr.vendorShippingCity, addr.vendorShippingCity);
        } else {
            this.vendorShippingCity.setValue(addr.vendorShippingCity);
        }
        if ((WtfGlobal.isIndiaCountryAndGSTApplied() && this.stateAsComboFlag)||  WtfGlobal.isUSCountryAndGSTApplied()) {  // Check wheather country is US or INDIA
            this.vendorShippingState.setValForRemoteStore(addr.vendorShippingState, addr.vendorShippingState);
        } else {
            this.vendorShippingState.setValue(addr.vendorShippingState);
        }
        this.vendorShippingCountry.setValue(addr.vendorShippingCountry);
        this.vendorShippingPostal.setValue(addr.vendorShippingPostal);
        this.vendorShippingPhone.setValue(addr.vendorShippingPhone);
        this.vendorShippingMobile.setValue(addr.vendorShippingMobile);
        this.vendorShippingFax.setValue(addr.vendorShippingFax);
        this.vendorShippingEmail.setValue(addr.vendorShippingEmail);
        this.vendorShippingRecipientName.setValue(addr.vendorShippingRecipientName);
        this.vendorShippingContactPerson.setValue(addr.vendorShippingContactPerson);
        this.vendorShippingContactNumber.setValue(addr.vendorShippingContactNumber);
        this.vendorShippingContactDesignation.setValue(addr.vendorShippingContactDesignation);
        this.vendorShippingWebsite.setValue(addr.vendorShippingWebsite);
    },
    setCustomerShippingAddress:function(addr){
        this.customerShippingAddress.setValue(addr.customerShippingAddress);
        if ( WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US 
            this.customerShippingCounty.setValForRemoteStore(addr.customerShippingCounty, addr.customerShippingCounty);
        }
        if ( WtfGlobal.isUSCountryAndGSTApplied()) {   // Check wheather country is US 
            this.customerShippingCity.setValForRemoteStore(addr.customerShippingCity, addr.customerShippingCity);
        } else {
            this.customerShippingCity.setValue(addr.customerShippingCity);
        }
        if ((WtfGlobal.isIndiaCountryAndGSTApplied()&& this.stateAsComboFlag) ||  WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US or INDIA
            this.customerShippingState.setValForRemoteStore(addr.customerShippingState, addr.customerShippingState);
        } else {
            this.customerShippingState.setValue(addr.customerShippingState);
        }
        this.customerShippingCountry.setValue(addr.customerShippingCountry);
        this.customerShippingPostal.setValue(addr.customerShippingPostal);
        this.customerShippingPhone.setValue(addr.customerShippingPhone);
        this.customerShippingMobile.setValue(addr.customerShippingMobile);
        this.customerShippingFax.setValue(addr.customerShippingFax);
        this.customerShippingEmail.setValue(addr.customerShippingEmail);
        this.customerShippingRecipientName.setValue(addr.customerShippingRecipientName);
        this.customerShippingContactPerson.setValue(addr.customerShippingContactPerson);
        this.customerShippingContactNumber.setValue(addr.customerShippingContactNumber);
        this.customerShippingContactDesignation.setValue(addr.customerShippingContactDesignation);
        this.customerShippingWebsite.setValue(addr.customerShippingWebsite);
        this.customerShippingRoute.setValue(addr.customerShippingRoute);
    },
    setCustomerDefaultShippingAddress: function(addr) {
        this.customerShippingAddress.setValue(addr.address);
        if ( WtfGlobal.isUSCountryAndGSTApplied()) {      // Check wheather country is US 
            this.customerShippingCounty.setValForRemoteStore(addr.county, addr.county);         //For setting customer default shipping address
        }
        if ( WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US 
            this.customerShippingCity.setValForRemoteStore(addr.city, addr.city);
        } else {
            this.customerShippingCity.setValue(addr.city);
        }
        if ((WtfGlobal.isIndiaCountryAndGSTApplied() && this.stateAsComboFlag) ||  WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US or INDIA
            this.customerShippingState.setValForRemoteStore(addr.state, addr.state);
        } else {
            this.customerShippingState.setValue(addr.state);
        }
        this.customerShippingCountry.setValue(addr.country);
        this.customerShippingPostal.setValue(addr.postalCode);
        this.customerShippingPhone.setValue(addr.phone);
        this.customerShippingMobile.setValue(addr.mobileNumber);
        this.customerShippingFax.setValue(addr.fax);
        this.customerShippingEmail.setValue(addr.emailID);
        this.customerShippingRecipientName.setValue(addr.contactPerson);
        this.customerShippingContactPerson.setValue(addr.recipientName);
        this.customerShippingContactNumber.setValue(addr.contactPersonNumber);
        this.customerShippingContactDesignation.setValue(addr.contactPersonDesignation);
        this.customerShippingWebsite.setValue(addr.website);
        this.customerShippingRoute.setValue(addr.shippingRoute);
        
    },
     setBillingAddress:function(addr){
        this.billingAddress.setValue(addr.billingAddress);
        if ( WtfGlobal.isUSCountryAndGSTApplied()) {   // Check wheather country is US 
            this.billingCounty.setValForRemoteStore(addr.billingCounty, addr.billingCounty); // For setting customer default billing address
        }
        if ( WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US 
            this.billingCity.setValForRemoteStore(addr.billingCity, addr.billingCity);
        } else {
            this.billingCity.setValue(addr.billingCity);
        }
        if ((WtfGlobal.isIndiaCountryAndGSTApplied() && this.stateAsComboFlag)||  WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US or INDIA
            this.billingState.setValForRemoteStore(addr.billingState, addr.billingState);
        } else {
            this.billingState.setValue(addr.billingState);
        }
        this.billingCountry.setValue(addr.billingCountry);
        this.billingPostal.setValue(addr.billingPostal);
        this.billingPhone.setValue(addr.billingPhone);
        this.billingMobile.setValue(addr.billingMobile);
        this.billingFax.setValue(addr.billingFax);
        this.billingEmail.setValue(addr.billingEmail);
        this.billingRecipientName.setValue(addr.billingRecipientName);
        this.billingContactPerson.setValue(addr.billingContactPerson);
        this.billingContactNumber.setValue(addr.billingContactPersonNumber);  
        this.billingContactDesignation.setValue(addr.billingContactPersonDesignation);
        this.billingWebsite.setValue(addr.billingWebsite);
    },
    
    /*---Setting vendor billing address--- */
    setVendorBillingAddress: function(addr) {
        this.dropshipbillingAddress.setValue(addr.dropshipbillingAddress);
        
        if (WtfGlobal.isUSCountryAndGSTApplied()) {   // Check wheather country is US 
            this.dropshipbillingCounty.setValForRemoteStore(addr.dropshipbillingCounty, addr.dropshipbillingCounty); // For setting customer default billing address
        }
        if (WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US 
            this.dropshipbillingCity.setValForRemoteStore(addr.dropshipbillingCity, addr.dropshipbillingCity);
        } else {
            this.dropshipbillingCity.setValue(addr.dropshipbillingCity);
        }
        if (WtfGlobal.isIndiaCountryAndGSTApplied() || WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US or INDIA
            this.dropshipbillingState.setValForRemoteStore(addr.dropshipbillingState, addr.dropshipbillingState);
        } else {
            this.dropshipbillingState.setValue(addr.dropshipbillingState);
        }
        
        this.dropshipbillingCountry.setValue(addr.dropshipbillingCountry);
        this.dropshipbillingPostal.setValue(addr.dropshipbillingPostal);
        this.dropshipbillingPhone.setValue(addr.dropshipbillingPhone);
        this.dropshipbillingMobile.setValue(addr.dropshipbillingMobile);
        this.dropshipbillingFax.setValue(addr.dropshipbillingFax);
        this.dropshipbillingEmail.setValue(addr.dropshipbillingEmail);
        this.dropshipbillingRecipientName.setValue(addr.dropshipbillingRecipientName);
        this.dropshipbillingContactPerson.setValue(addr.dropshipbillingContactPerson);
        this.dropshipbillingContactNumber.setValue(addr.dropshipbillingContactPersonNumber);
        this.dropshipbillingContactDesignation.setValue(addr.dropshipbillingContactPersonDesignation);
        this.dropshipbillingWebsite.setValue(addr.dropshipbillingWebsite);
    },
    /*---Setting vendor billing address--- */
    setVendorBillingAddressINDIA: function (addr) {
        this.vendorbillingAddress.setValue(addr.vendorbillingAddressForINDIA);
        this.vendorbillingCounty.setValue(addr.vendorbillingCountyForINDIA);
        this.vendorbillingCity.setValue(addr.vendorbillingCityForINDIA);
        if (WtfGlobal.isIndiaCountryAndGSTApplied() && this.stateAsComboFlag) { // Check wheather country is US or INDIA
            this.vendorbillingState.setValForRemoteStore(addr.vendorbillingStateForINDIA, addr.vendorbillingStateForINDIA);
        } else {
            this.vendorbillingState.setValue(addr.vendorbillingStateForINDIA);
        }
        this.vendorbillingCountry.setValue(addr.vendorbillingCountryForINDIA);
        this.vendorbillingPostal.setValue(addr.vendorbillingPostalForINDIA);
        this.vendorbillingPhone.setValue(addr.vendorbillingPhoneForINDIA);
        this.vendorbillingMobile.setValue(addr.vendorbillingMobileForINDIA);
        this.vendorbillingFax.setValue(addr.vendorbillingFaxForINDIA);
        this.vendorbillingEmail.setValue(addr.vendorbillingEmailForINDIA);
        this.vendorbillingRecipientName.setValue(addr.vendorbillingRecipientNameForINDIA);
        this.vendorbillingContactPerson.setValue(addr.vendorbillingContactPersonForINDIA);
        this.vendorbillingContactNumber.setValue(addr.vendorbillingContactPersonNumberForINDIA);
        this.vendorbillingContactDesignation.setValue(addr.vendorbillingContactPersonDesignationForINDIA);
        this.vendorbillingWebsite.setValue(addr.vendorbillingWebsiteForINDIA);
    },
    
    setDefaultShippingAddress:function(addr){
        this.shippingAddress.setValue(addr.address);
        if ( WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US 
            this.shippingCounty.setValForRemoteStore(addr.county, addr.county); // for setting default shipping address to fields,from record 
        } 
        if ( WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US 
            this.shippingCity.setValForRemoteStore(addr.city, addr.city);
        } else {
            this.shippingCity.setValue(addr.city);
        }
        if ((WtfGlobal.isIndiaCountryAndGSTApplied() && this.stateAsComboFlag) ||  WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US or INDIA
            this.shippingState.setValForRemoteStore(addr.state, addr.state);
        } else {
            this.shippingState.setValue(addr.state);
        }
        this.shippingCountry.setValue(addr.country);
        this.shippingPostal.setValue(addr.postalCode);
        this.shippingPhone.setValue(addr.phone);
        this.shippingMobile.setValue(addr.mobileNumber);
        this.shippingFax.setValue(addr.fax);
        this.shippingEmail.setValue(addr.emailID);
        this.shippingContactPerson.setValue(addr.contactPerson);
        this.shippingRecipientName.setValue(addr.recipientName);
        this.shippingContactNumber.setValue(addr.contactPersonNumber);
        this.shippingContactDesignation.setValue(addr.contactPersonDesignation);
        this.shippingWebsite.setValue(addr.website);
        this.shippingRoute.setValue(addr.shippingRoute);
    },
    
    setVendorDefaultShippingAddress:function(addr){
        this.vendorShippingAddress.setValue(addr.address);
        if ( WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US 
            this.vendorShippingCounty.setValForRemoteStore(addr.county, addr.county);  // for setting default shipping address fields for vendor 
        } 
        if ( WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US 
            this.vendorShippingCity.setValForRemoteStore(addr.city, addr.city);
        } else {
            this.vendorShippingCity.setValue(addr.city);
        }
        if ((WtfGlobal.isIndiaCountryAndGSTApplied() && this.stateAsComboFlag) ||  WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US or INDIA
            this.vendorShippingState.setValForRemoteStore(addr.state, addr.state);
        } else {
            this.vendorShippingState.setValue(addr.state);
        }
        this.vendorShippingCountry.setValue(addr.country);
        this.vendorShippingPostal.setValue(addr.postalCode);
        this.vendorShippingPhone.setValue(addr.phone);
        this.vendorShippingMobile.setValue(addr.mobileNumber);
        this.vendorShippingFax.setValue(addr.fax);
        this.vendorShippingEmail.setValue(addr.emailID);
        this.vendorShippingContactPerson.setValue(addr.contactPerson);
        this.vendorShippingRecipientName.setValue(addr.recipientName);
        this.vendorShippingContactNumber.setValue(addr.contactPersonNumber);
        this.vendorShippingContactDesignation.setValue(addr.contactPersonDesignation);
        this.vendorShippingWebsite.setValue(addr.website);
    },
   
    setDefaultBillingAddress:function(addr){
        this.billingAddress.setValue(addr.address);
        if ( WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US 
            this.billingCounty.setValForRemoteStore(addr.county, addr.county); // setting default billing address to respective fields from the record 
            this.billingCity.setValForRemoteStore(addr.city, addr.city);
        } else {
            this.billingCity.setValue(addr.city);
        }
       
        if ((WtfGlobal.isIndiaCountryAndGSTApplied() && this.stateAsComboFlag) ||  WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US or INDIA
            this.billingState.setValForRemoteStore(addr.state, addr.state);
        } else {
            this.billingState.setValue(addr.state);
        }
        this.billingCountry.setValue(addr.country);
        this.billingPostal.setValue(addr.postalCode);
        this.billingPhone.setValue(addr.phone);
        this.billingMobile.setValue(addr.mobileNumber);
        this.billingFax.setValue(addr.fax);
        this.billingEmail.setValue(addr.emailID);
        this.billingRecipientName.setValue(addr.recipientName);
        this.billingContactPerson.setValue(addr.contactPerson);
        this.billingContactNumber.setValue(addr.contactPersonNumber);  
        this.billingContactDesignation.setValue(addr.contactPersonDesignation);
        this.billingWebsite.setValue(addr.website);
    },
    
    /*--Setting default vendor billing address-- */
    setDefaultVendorBillingAddress:function(addr) {
        this.dropshipbillingAddress.setValue(addr.address);
        
        if (WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US 
            this.dropshipbillingCounty.setValForRemoteStore(addr.county, addr.county); // setting default billing address to respective fields from the record 
            this.dropshipbillingCity.setValForRemoteStore(addr.city, addr.city);
        } else {
            this.dropshipbillingCity.setValue(addr.city);
        }

        if (WtfGlobal.isIndiaCountryAndGSTApplied() || WtfGlobal.isUSCountryAndGSTApplied()) { // Check wheather country is US or INDIA
            this.dropshipbillingState.setValForRemoteStore(addr.state, addr.state);
        } else {
            this.dropshipbillingState.setValue(addr.state);
        }   
        this.dropshipbillingCountry.setValue(addr.country);
        this.dropshipbillingPostal.setValue(addr.postalCode);
        this.dropshipbillingPhone.setValue(addr.phone);
        this.dropshipbillingMobile.setValue(addr.mobileNumber);
        this.dropshipbillingFax.setValue(addr.fax);
        this.dropshipbillingEmail.setValue(addr.emailID);
        this.dropshipbillingRecipientName.setValue(addr.recipientName);
        this.dropshipbillingContactPerson.setValue(addr.contactPerson);
        this.dropshipbillingContactNumber.setValue(addr.contactPersonNumber);
        this.dropshipbillingContactDesignation.setValue(addr.contactPersonDesignation);
        this.dropshipbillingWebsite.setValue(addr.website);
    },
    /*--Setting default vendor billing address-- */
    setDefaultVendorBillingAddressINDIA:function(addr) {
        this.vendorbillingAddress.setValue(addr.address);
        this.vendorbillingCity.setValue(addr.city);
        this.vendorbillingCounty.setValue(addr.county);
        if (WtfGlobal.isIndiaCountryAndGSTApplied() && this.stateAsComboFlag) { // Check wheather country is INDIA
            this.vendorbillingState.setValForRemoteStore(addr.state, addr.state);
        } else {
            this.vendorbillingState.setValue(addr.state);
        }   
        this.vendorbillingCountry.setValue(addr.country);
        this.vendorbillingPostal.setValue(addr.postalCode);
        this.vendorbillingPhone.setValue(addr.phone);
        this.vendorbillingMobile.setValue(addr.mobileNumber);
        this.vendorbillingFax.setValue(addr.fax);
        this.vendorbillingEmail.setValue(addr.emailID);
        this.vendorbillingRecipientName.setValue(addr.recipientName);
        this.vendorbillingContactPerson.setValue(addr.contactPerson);
        this.vendorbillingContactNumber.setValue(addr.contactPersonNumber);
        this.vendorbillingContactDesignation.setValue(addr.contactPersonDesignation);
        this.vendorbillingWebsite.setValue(addr.website);
    },
    
    addDefaultBillingAddressInStore:function(){
        var storeRec=new Wtf.data.Record({
            aliasName:Wtf.ADDRESS.BILLING_ADDRESS,
            isBillingAddress:true
        });
        this.billingAddrsStore.add(storeRec) 
        this.billingAddrsStore.commitChanges();        
    },
    
    addDefaultVendorBillingAddressInStore: function() {

        var storeRec = new Wtf.data.Record({
            aliasName: Wtf.ADDRESS.BILLING_ADDRESS,
            isBillingAddress: true
        });
        this.vendorbillingAddrsStore.add(storeRec)
        this.vendorbillingAddrsStore.commitChanges();
    },
    addDefaultShippingAddressInStore:function(){
        var storeRec=new Wtf.data.Record({
            aliasName:Wtf.ADDRESS.SHIPPING_ADDRESS,
            isBillingAddress:false
        });
        this.ShippingAddrsStore.add(storeRec) 
        this.ShippingAddrsStore.commitChanges();       
    },
     addDefaultVendorShippingAddressInStore:function(){
        var storeRec=new Wtf.data.Record({
            aliasName:Wtf.ADDRESS.SHIPPING_ADDRESS,
            isBillingAddress:false
        });
        this.vendorShippingAddrsStore.add(storeRec) 
        this.vendorShippingAddrsStore.commitChanges(); 
    },
    addDefaultCustomerShippingAddressInStore:function(){
        var storeRec=new Wtf.data.Record({
            aliasName:Wtf.ADDRESS.SHIPPING_ADDRESS,
            isBillingAddress:false
        });
        this.customerShippingAddrsStore.add(storeRec) 
        this.customerShippingAddrsStore.commitChanges(); 
    },
    
    onBillingAddressLoad:function(){
        if((this.isEdit || this.copyInv || (this.singleLink && !this.showCustomerShippingWin)) && this.record!=null && this.record!=undefined){// edit and copy case need to update or add arress in store           
            this.addOrUpdateTransactionBillingAddressInStore();           
        } 
        
        /*---While Dropship PO linking in PI--------  */
        if((this.isdropshipDocument && this.moduleid ==Wtf.Acc_Vendor_Invoice_ModuleId)  &&  (this.singleLink && this.record!=null && this.record!=undefined)){
          this.addOrUpdateTransactionBillingAddressInStore();       
        }
        if(this.billingAddrsStore.getCount()>0){
            if(this.currentaddress!=""){ //this variable will be not empty when user clicked on save button
                this.billingAddrsCombo.setValue(this.currentaddress.billingAddrsCombo);  
                this.setBillingAddress(this.currentaddress);  
            } else {               
                if((this.isEdit || this.copyInv || (this.singleLink && !this.showCustomerShippingWin))){
                    var tranBillingAddrRec="";
                    if(this.record.data.billingAddress!="" && this.record.data.billingAddress!=undefined){
                       tranBillingAddrRec=this.getTransactionBillingAddressRecord();    
                    }
                    if(tranBillingAddrRec!=""){ //If there transaction address then set it otherwise no address will be set
                        this.billingAddrsCombo.setValue(tranBillingAddrRec.data.aliasName);
                        this.setDefaultBillingAddress(tranBillingAddrRec.data);
                    } 
                } else { //If not transaction address available then set it as 
                    var isDefaultAddressFound=false;
                    for(var i=0;i<this.billingAddrsStore.getCount();i++){
                        var defaultAddress=this.billingAddrsStore.getAt(i);
                        if(defaultAddress.data.isDefaultAddress){
                            this.billingAddrsCombo.setValue(defaultAddress.data.aliasName);
                            this.setDefaultBillingAddress(defaultAddress.data);  
                            isDefaultAddressFound=true;
                            break;
                        }
                    }
                    if(!isDefaultAddressFound){//in case default address not found then set address at index 0
                        var rec=this.billingAddrsStore.getAt(0);
                        this.billingAddrsCombo.setValue(rec.data.aliasName);
                        this.setDefaultBillingAddress(rec.data);
                    }
                }
            }                 
        } else { //If no address available in store then add default address
            this.addDefaultBillingAddressInStore();
            this.billingAddrsCombo.setValue("Billing Address1"); 
            if(this.currentaddress!="" && this.currentaddress!=undefined){ 
                this.setBillingAddress(this.currentaddress);             
            } 
        } 
    },
    
    /*---call on load of vendorbilling store----  */
    onVendorBillingAddressLoad: function() {
        if (((this.isEdit && !this.copyInv) || (this.singleLink && !this.showCustomerShippingWin)) && this.record != null && this.record != undefined) {// edit and copy case need to update or add arress in store           
            this.addOrUpdateTransactionVendorBillingAddressInStore();
        }
        
        /*---While Dropship PO linking in PI--------  */
        if ((this.isdropshipDocument && this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) && (this.singleLink && this.record != null && this.record != undefined)) {
            this.addOrUpdateTransactionVendorBillingAddressInStore();
        }
        
        if (this.vendorbillingAddrsStore.getCount() > 0) {
            if (this.currentaddress != "") { //this variable will be not empty when user clicked on save button
                this.dropshipbillingAddrsCombo.setValue(this.currentaddress.dropshipbillingAddrsCombo);
                this.setVendorBillingAddress(this.currentaddress);
            } else {
                if (((this.isEdit && !this.copyInv) || (this.singleLink && !this.showCustomerShippingWin))) {
                    var tranBillingAddrRec = "";
                    if (this.record.data.billingAddress != "" && this.record.data.billingAddress != undefined) {
                        tranBillingAddrRec = this.getTransactionVendorBillingAddressRecord();
                    }
                    if (tranBillingAddrRec != "") { //If there transaction address then set it otherwise no address will be set
                        this.dropshipbillingAddrsCombo.setValue(tranBillingAddrRec.data.aliasName);
                        this.setDefaultVendorBillingAddress(tranBillingAddrRec.data);
                    }
                } else { //If not transaction address available then set it as 
                    var isDefaultAddressFound = false;
                    for (var i = 0; i < this.vendorbillingAddrsStore.getCount(); i++) {
                        var defaultAddress = this.vendorbillingAddrsStore.getAt(i);
                        if (defaultAddress.data.isDefaultAddress) {
                            this.dropshipbillingAddrsCombo.setValue(defaultAddress.data.aliasName);
                            this.setDefaultVendorBillingAddress(defaultAddress.data);
                            isDefaultAddressFound = true;
                            break;
                        }
                    }
                    if (!isDefaultAddressFound) {//in case default address not found then set address at index 0
                        var rec = this.vendorbillingAddrsStore.getAt(0);
                        this.dropshipbillingAddrsCombo.setValue(rec.data.aliasName);
                        this.setDefaultVendorBillingAddress(rec.data);
                    }
                }
            }
        } else { //If no address available in store then add default address
            this.addDefaultVendorBillingAddressInStore();
            this.dropshipbillingAddrsCombo.setValue("Billing Address1");
            if (this.currentaddress != "" && this.currentaddress != undefined) {
                this.setVendorBillingAddress(this.currentaddress);//change here also name of function
            }
        }
    },
    /**
     * For india country and vendor transactions and isAddressFromVendorMaster 
     *  is off then vendor billing address visible in seperate fieldset.
     *  Set this fields data on show address click in transaction
     */
    onVendorBillingAddressLoadINDIA: function() {
        /**
         * Copy case Get Address from transaction
         */
        if (((this.isEdit || this.copyInv) || (this.singleLink && !this.showCustomerShippingWin)) && this.record != null && this.record != undefined) {// edit and copy case need to update or add arress in store           
            this.addOrUpdateTransactionVendorBillingAddressInStoreINDIA();
        }
        if (this.vendorbillingAddrsStore.getCount() > 0) {
            if (this.currentaddress != "") { //this variable will be not empty when user clicked on save button
                this.vendorbillingAddrsCombo.setValue(this.currentaddress.vendorbillingAddrsComboForINDIA);
                this.setVendorBillingAddressINDIA(this.currentaddress);
            } else {
                /**
                 * Get Address from transaction details
                 */
                if (((this.isEdit || this.copyInv) || (this.singleLink && !this.showCustomerShippingWin))) {
                    var tranBillingAddrRec = "";
                    if (this.record.data.vendorbillingAddressForINDIA != "" && this.record.data.vendorbillingAddressForINDIA != undefined) {
                        tranBillingAddrRec = this.getTransactionVendorBillingAddressRecordINDIA();
                    }
                    if (tranBillingAddrRec != "") { //If there transaction address then set it otherwise no address will be set
                        this.vendorbillingAddrsCombo.setValue(tranBillingAddrRec.data.aliasName);
                        this.setDefaultVendorBillingAddressINDIA(tranBillingAddrRec.data);
                    }
                } else { //If not transaction address available then set it as 
                    var isDefaultAddressFound = false;
                    for (var i = 0; i < this.vendorbillingAddrsStore.getCount(); i++) {
                        var defaultAddress = this.vendorbillingAddrsStore.getAt(i);
                        if (defaultAddress.data.isDefaultAddress) {
                            this.vendorbillingAddrsCombo.setValue(defaultAddress.data.aliasName);
                            this.setDefaultVendorBillingAddressINDIA(defaultAddress.data);
                            isDefaultAddressFound = true;
                            break;
                        }
                    }
                    if (!isDefaultAddressFound) {//in case default address not found then set address at index 0
                        var rec = this.vendorbillingAddrsStore.getAt(0);
                        this.vendorbillingAddrsCombo.setValue(rec.data.aliasName);
                        this.setDefaultVendorBillingAddressINDIA(rec.data);
                    }
                }
            }
        } else { //If no address available in store then add default address
            this.addDefaultVendorBillingAddressInStore();
            this.vendorbillingAddrsCombo.setValue(Wtf.ADDRESS.BILLING_ADDRESS);
            if (this.currentaddress != "" && this.currentaddress != undefined) {
                this.setVendorBillingAddressINDIA(this.currentaddress);//change here also name of function
            }
        }
    },
    
    onShippingAddressLoad:function(){
        if((this.isEdit || this.copyInv || (this.singleLink && !this.showCustomerShippingWin)) && this.record!=null && this.record!=undefined){// edit and copy case need to update or add arress in store           
            this.addOrUpdateTransactionShippingAddressInStore();           
        } 
        if(this.ShippingAddrsStore.getCount()>0){
            if(this.currentaddress!=""){ //this variable will be not empty when user clicked on save button
                this.shippingAddrsCombo.setValue(this.currentaddress.shippingAddrsCombo);  
                this.setShippingAddress(this.currentaddress);  
            } else {                
                if((this.isEdit || this.copyInv || (this.singleLink && !this.showCustomerShippingWin))){
                    var tranShippingAddrRec="";
                    if(this.record.data.shippingAddress!="" && this.record.data.shippingAddress!=undefined){
                        tranShippingAddrRec=this.getTransactionShippingAddressRecord();
                    }
                    if(tranShippingAddrRec!=""){ //If there transaction address then set it otherwise set default address
                        this.shippingAddrsCombo.setValue(tranShippingAddrRec.data.aliasName);
                        this.setDefaultShippingAddress(tranShippingAddrRec.data);
                    }   
                } else { //If not transaction address available then set it as 
                    var isDefaultAddressFound=false;
                    for(var i=0;i<this.ShippingAddrsStore.getCount();i++){
                        var defaultAddress=this.ShippingAddrsStore.getAt(i);
                        if(defaultAddress.data.isDefaultAddress){
                            this.shippingAddrsCombo.setValue(defaultAddress.data.aliasName);
                            this.setDefaultShippingAddress(defaultAddress.data);  
                            isDefaultAddressFound=true;
                            break;
                        }
                    }
                    if(!isDefaultAddressFound){//in case default address not found then set address at index 0
                        var rec=this.ShippingAddrsStore.getAt(0);
                        this.shippingAddrsCombo.setValue(rec.data.aliasName);
                        this.setDefaultShippingAddress(rec.data);
                    }
                }
            }                 
        } else { //If no address available in store then add default address
            this.addDefaultShippingAddressInStore();
            this.shippingAddrsCombo.setValue(Wtf.ADDRESS.SHIPPING_ADDRESS); 
            if(this.currentaddress!="" && this.currentaddress!=undefined){ 
                this.setShippingAddress(this.currentaddress);             
            } 
        }
    },

    onVendorShippingAddressLoad:function(){
        if((this.isEdit || this.copyInv || (this.singleLink && !this.showCustomerShippingWin)) && this.record!=null && this.record!=undefined){// edit and copy case need to update or add arress in store           
            this.addOrUpdateTransactionVendorShippingAddressInStore(); //In this method we added/updated store address with already saved transaction address
        } 
        if(this.vendorShippingAddrsStore.getCount()>0){
            if(this.currentaddress!=""){ //this variable will be not empty when user clicked on save button
                this.vendorShippingAddrsCombo.setValue(this.currentaddress.vendorShippingAddrsCombo);  
                this.setVendorShippingAddress(this.currentaddress);  
            } else {                
                if((this.isEdit || this.copyInv || (this.singleLink && !this.showCustomerShippingWin))){
                    var tranShippingAddrRec="";
                    if(this.record.data.vendcustShippingAddress!="" && this.record.data.vendcustShippingAddress!=undefined){
                        tranShippingAddrRec=this.getTransactionVendorShippingAddressRecord();
                    }
                    if(tranShippingAddrRec!=""){ //If there transaction address then set it otherwise set default address
                        this.vendorShippingAddrsCombo.setValue(tranShippingAddrRec.data.aliasName);
                        this.setVendorDefaultShippingAddress(tranShippingAddrRec.data);
                    }   
                } else { //If not transaction address available then set it as 
                    var isDefaultAddressFound=false;
                    for(var i=0;i<this.vendorShippingAddrsStore.getCount();i++){
                        var defaultAddress=this.vendorShippingAddrsStore.getAt(i);
                        if(defaultAddress.data.isDefaultAddress){
                            this.vendorShippingAddrsCombo.setValue(defaultAddress.data.aliasName);
                            this.setVendorDefaultShippingAddress(defaultAddress.data);  
                            isDefaultAddressFound=true;
                            break;
                        }
                    }
                    if(!isDefaultAddressFound){//in case default address not found then set address at index 0
                        var rec=this.vendorShippingAddrsStore.getAt(0);
                        this.vendorShippingAddrsCombo.setValue(rec.data.aliasName);
                        this.setVendorDefaultShippingAddress(rec.data);
                    }
                }
            }                 
        } else { //If no address available in store then add default address
            this.addDefaultVendorShippingAddressInStore();
            this.vendorShippingAddrsCombo.setValue(Wtf.ADDRESS.SHIPPING_ADDRESS); 
            if(this.currentaddress!="" && this.currentaddress!=undefined){ 
                this.setVendorShippingAddress(this.currentaddress);             
            } 
        }
    },
    onCustomerShippingAddressLoad:function(){
        if((this.isEdit || this.copyInv || this.singleLink) && this.record!=null && this.record!=undefined){// edit and copy case need to update or add arress in store           
            this.addOrUpdateTransactionCustomerShippingAddressInStore(); //In this method we added/updated store address with already saved transaction address
        } 
        if(this.customerShippingAddrsStore.getCount()>0){
            if(this.currentaddress!=""){ //this variable will be not empty when user clicked on save button
                this.customerShippingAddrsCombo.setValue(this.currentaddress.customerShippingAddrsCombo);  
                this.setCustomerShippingAddress(this.currentaddress);  
            } else {                
                if((this.isEdit || this.copyInv || this.singleLink)){
                    var tranShippingAddrRec="";
                    if(this.record.data.customerShippingAddress!="" && this.record.data.customerShippingAddress!=undefined){
                        tranShippingAddrRec=this.getTransactionCustomerShippingAddressRecord();
                    }
                    if(tranShippingAddrRec!=""){ //If there transaction address then set it otherwise set default address
                        this.customerShippingAddrsCombo.setValue(tranShippingAddrRec.data.aliasName);
                        this.setCustomerDefaultShippingAddress(tranShippingAddrRec.data);
                    }   
                } else { //If not transaction address available then set it as 
                    var isDefaultAddressFound=false;
                    for(var i=0;i<this.customerShippingAddrsStore.getCount();i++){
                        var defaultAddress=this.customerShippingAddrsStore.getAt(i);
                        if(defaultAddress.data.isDefaultAddress){
                            this.customerShippingAddrsCombo.setValue(defaultAddress.data.aliasName);
                            this.setCustomerDefaultShippingAddress(defaultAddress.data);  
                            isDefaultAddressFound=true;
                            break;
                        }
                    }
                    if(!isDefaultAddressFound){//in case default address not found then set address at index 0
                        var rec=this.customerShippingAddrsStore.getAt(0);
                        this.customerShippingAddrsCombo.setValue(rec.data.aliasName);
                        this.setCustomerDefaultShippingAddress(rec.data);
                    }
                }
            }                 
        } else { //If no address available in store then add default address
            this.addDefaultCustomerShippingAddressInStore();
            this.customerShippingAddrsCombo.setValue(Wtf.ADDRESS.SHIPPING_ADDRESS); 
            if(this.currentaddress!="" && this.currentaddress!=undefined){ 
                this.setCustomerShippingAddress(this.currentaddress);             
            } 
        }
    },
    addOrUpdateTransactionBillingAddressInStore:function(){  
        var tranBillingAddrRec="";
        if(this.record.data.billingAddress!="" && this.record.data.billingAddress!=undefined){
            tranBillingAddrRec=this.getTransactionBillingAddressRecord();
        }
        
        if(tranBillingAddrRec!=""){//It means there is billing address in transaction and we need to add or update address store
            var isAddresUpdated=false;
            if(this.billingAddrsStore.getCount()>0){                            
                for(var indexCount=0;indexCount<this.billingAddrsStore.getCount();indexCount++){
                    var storerec=this.billingAddrsStore.getAt(indexCount);
                    if(this.record.data.billingAddressType==storerec.data.aliasName){//alias name matches then need to update address store
                        this.billingAddrsStore.remove(storerec);                  
                        this.billingAddrsStore.insert(indexCount,tranBillingAddrRec);
                        this.billingAddrsStore.commitChanges();
                        isAddresUpdated=true;
                        break;
                    }
                }               
            } 
            if(!isAddresUpdated){//If alias name not matches. In this case we need to add address in store 
                this.billingAddrsStore.add(tranBillingAddrRec);
                this.billingAddrsStore.commitChanges();
            }
            this.billingAddrsStore.sort('aliasName','ASC');
        }        
    },
    
    addOrUpdateTransactionVendorBillingAddressInStore: function() {
        var tranBillingAddrRec = "";
        if (this.record.data.billingAddress != "" && this.record.data.billingAddress != undefined) {
            tranBillingAddrRec = this.getTransactionVendorBillingAddressRecord();
        }

        if (tranBillingAddrRec != "") {//It means there is billing address in transaction and we need to add or update address store
            var isAddresUpdated = false;
            if (this.vendorbillingAddrsStore.getCount() > 0) {
                for (var indexCount = 0; indexCount < this.vendorbillingAddrsStore.getCount(); indexCount++) {
                    var storerec = this.vendorbillingAddrsStore.getAt(indexCount);
                    if (this.record.data.dropshipbillingAddressType == storerec.data.aliasName) {//alias name matches then need to update address store
                        this.vendorbillingAddrsStore.remove(storerec);
                        this.vendorbillingAddrsStore.insert(indexCount, tranBillingAddrRec);
                        this.vendorbillingAddrsStore.commitChanges();
                        isAddresUpdated = true;
                        break;
                    }
                }
            }
            if (!isAddresUpdated) {//If alias name not matches. In this case we need to add address in store 
                this.vendorbillingAddrsStore.add(tranBillingAddrRec);
                this.vendorbillingAddrsStore.commitChanges();
            }
            this.vendorbillingAddrsStore.sort('aliasName', 'ASC');
        }
    },
   /**
     * For india country and vendor transactions and isAddressFromVendorMaster 
     *  is off then vendor billing address visible in seperate fieldset.
     *  Set this fields data of transaction address edit
     */
    addOrUpdateTransactionVendorBillingAddressInStoreINDIA: function() {
        var tranBillingAddrRec = "";
        if (this.record.data.vendor_billingAddress != "" && this.record.data.vendorbillingAddressForINDIA != undefined) {
            tranBillingAddrRec = this.getTransactionVendorBillingAddressRecordINDIA();
        }
        if (tranBillingAddrRec != "") {//It means there is billing address in transaction and we need to add or update address store
            var isAddresUpdated = false;
            if (this.vendorbillingAddrsStore.getCount() > 0) {
                for (var indexCount = 0; indexCount < this.vendorbillingAddrsStore.getCount(); indexCount++) {
                    var storerec = this.vendorbillingAddrsStore.getAt(indexCount);
                    if (this.record.data.vendorbillingAddressTypeForINDIA == storerec.data.aliasName) {//alias name matches then need to update address store
                        this.vendorbillingAddrsStore.remove(storerec);
                        this.vendorbillingAddrsStore.insert(indexCount, tranBillingAddrRec);
                        this.vendorbillingAddrsStore.commitChanges();
                        isAddresUpdated = true;
                        break;
                    }
                }
            }
            if (!isAddresUpdated) {//If alias name not matches. In this case we need to add address in store 
                this.vendorbillingAddrsStore.add(tranBillingAddrRec);
                this.vendorbillingAddrsStore.commitChanges();
            }
            this.vendorbillingAddrsStore.sort('aliasName', 'ASC');
        }
    },
    
    addOrUpdateTransactionShippingAddressInStore:function(){
        var tranAddrRec="";
        if(this.record.data.shippingAddress!="" && this.record.data.shippingAddress!=undefined){
            tranAddrRec=this.getTransactionShippingAddressRecord();
        }
        
        if(tranAddrRec!=""){//It means there is billing address in transaction and we need to add or update address store
            var isAddresUpdated=false;
            
            if(this.ShippingAddrsStore.getCount()>0){                            
                for(var indexCount=0;indexCount<this.ShippingAddrsStore.getCount();indexCount++){
                    var storerec=this.ShippingAddrsStore.getAt(indexCount);
                    if(this.record.data.shippingAddressType==storerec.data.aliasName){//alias name matches then need to update address store
                        this.ShippingAddrsStore.remove(storerec);                  
                        this.ShippingAddrsStore.insert(indexCount,tranAddrRec);
                        this.ShippingAddrsStore.commitChanges();
                        isAddresUpdated=true;
                        break;
                    }
                }               
            } 
            
            if(!isAddresUpdated){//If alias name not matches. In this case we need to add address in store 
                this.ShippingAddrsStore.add(tranAddrRec);
                this.ShippingAddrsStore.commitChanges();
            }
            this.ShippingAddrsStore.sort('aliasName','ASC');
        }        
    },
    addOrUpdateTransactionVendorShippingAddressInStore:function(){
        var tranAddrRec="";
        if(this.record.data.vendcustShippingAddress!="" && this.record.data.vendcustShippingAddress!=undefined){
            tranAddrRec=this.getTransactionVendorShippingAddressRecord();
        }
        
        if(tranAddrRec!=""){//It means there is billing address in transaction and we need to add or update address store
            var isAddresUpdated=false;
            
            if(this.vendorShippingAddrsStore.getCount()>0){                            
                for(var indexCount=0;indexCount<this.vendorShippingAddrsStore.getCount();indexCount++){
                    var storerec=this.vendorShippingAddrsStore.getAt(indexCount);
                    if(this.record.data.vendcustShippingAddressType==storerec.data.aliasName){//alias name matches then need to update address store
                        this.vendorShippingAddrsStore.remove(storerec);                  
                        this.vendorShippingAddrsStore.insert(indexCount,tranAddrRec);
                        this.vendorShippingAddrsStore.commitChanges();
                        isAddresUpdated=true;
                        break;
                    }
                }               
            } 
            
            if(!isAddresUpdated){//If alias name not matches. In this case we need to add address in store 
                this.vendorShippingAddrsStore.add(tranAddrRec);
                this.vendorShippingAddrsStore.commitChanges();
            }
            this.vendorShippingAddrsStore.sort('aliasName','ASC');
        }        
    },
    addOrUpdateTransactionCustomerShippingAddressInStore:function(){
        var tranAddrRec="";
        if(this.record.data.customerShippingAddress!="" && this.record.data.customerShippingAddress!=undefined){
            tranAddrRec=this.getTransactionCustomerShippingAddressRecord();
        }
        
        if(tranAddrRec!=""){//It means there is billing address in transaction and we need to add or update address store
            var isAddresUpdated=false;
            
            if(this.customerShippingAddrsStore.getCount()>0){                            
                for(var indexCount=0;indexCount<this.customerShippingAddrsStore.getCount();indexCount++){
                    var storerec=this.customerShippingAddrsStore.getAt(indexCount);
                    if(this.record.data.customerShippingAddressType==storerec.data.aliasName){//alias name matches then need to update address store
                        this.customerShippingAddrsStore.remove(storerec);                  
                        this.customerShippingAddrsStore.insert(indexCount,tranAddrRec);
                        this.customerShippingAddrsStore.commitChanges();
                        isAddresUpdated=true;
                        break;
                    }
                }               
            } 
            
            if(!isAddresUpdated){//If alias name not matches. In this case we need to add address in store 
                this.customerShippingAddrsStore.add(tranAddrRec);
                this.customerShippingAddrsStore.commitChanges();
            }
            this.customerShippingAddrsStore.sort('aliasName','ASC');
        }        
    },
    
    getTransactionBillingAddressRecord:function(){
        var recData=this.record.data;
        var billingRec=new Wtf.data.Record({
            aliasName:recData.billingAddressType,
            address:recData.billingAddress,
            county:recData.billingCounty,
            city:recData.billingCity,
            state:recData.billingState,
            country:recData.billingCountry,
            postalCode:recData.billingPostal,
            phone:recData.billingPhone,
            mobileNumber:recData.billingMobile,
            fax:recData.billingFax,
            emailID:recData.billingEmail,
            recipientName:recData.billingRecipientName, 
            contactPerson:recData.billingContactPerson, 
            contactPersonNumber:recData.billingContactPersonNumber,
            contactPersonDesignation : recData.billingContactPersonDesignation,
            website : recData.billingWebsite,
            isDefaultAddress:true,
            isBillingAddress:true
        });
        return billingRec;
    }, 
    
/*----preparing rec in case of Edit/View----  */
    getTransactionVendorBillingAddressRecord:function() {
        var recData = this.record.data;
        var billingRec = new Wtf.data.Record({
            aliasName: recData.dropshipbillingAddressType,
            address: recData.dropshipbillingAddress,
            county: recData.dropshipbillingCounty,
            city: recData.dropshipbillingCity,
            state: recData.dropshipbillingState,
            country: recData.dropshipbillingCountry,
            postalCode: recData.dropshipbillingPostal,
            phone: recData.dropshipbillingPhone,
            mobileNumber: recData.dropshipbillingMobile,
            fax: recData.dropshipbillingFax,
            emailID: recData.dropshipbillingEmail,
            recipientName: recData.dropshipbillingRecipientName,
            contactPerson: recData.dropshipbillingContactPerson,
            contactPersonNumber: recData.dropshipbillingContactPersonNumber,
            contactPersonDesignation: recData.dropshipbillingContactPersonDesignation,
            website: recData.dropshipbillingWebsite,
            isDefaultAddress: true,
            isBillingAddress: true
        });
        return billingRec;
    }, 
     /**
     * For india country and vendor transactions and isAddressFromVendorMaster 
     *  is off then vendor billing address visible in seperate fieldset.
     *  get this biling address details
     */
    getTransactionVendorBillingAddressRecordINDIA:function() {
        var recData = this.record.data;
        var billingRec = new Wtf.data.Record({
            aliasName: recData.vendorbillingAddressTypeForINDIA,
            address: recData.vendorbillingAddressForINDIA,
            county: recData.vendorbillingCountyForINDIA,
            city: recData.vendorbillingCityForINDIA,
            state: recData.vendorbillingStateForINDIA,
            country: recData.vendorbillingCountryForINDIA,
            postalCode: recData.vendorbillingPostalForINDIA,
            phone: recData.vendorbillingPhoneForINDIA,
            mobileNumber: recData.vendorbillingMobileForINDIA,
            fax: recData.vendorbillingFaxForINDIA,
            emailID: recData.vendorbillingEmailForINDIA,
            recipientName: recData.vendorbillingRecipientNameForINDIA,
            contactPerson: recData.vendorbillingContactPersonForINDIA,
            contactPersonNumber: recData.vendorbillingContactPersonNumberForINDIA,
            contactPersonDesignation: recData.vendorbillingContactPersonDesignationForINDIA,
            website: recData.vendorbillingWebsiteForINDIA,
            isDefaultAddress: true,
            isBillingAddress: true
        });
        return billingRec;
    }, 
    
    getTransactionShippingAddressRecord:function(){
        var recData=this.record.data;
        var shippingRec=new Wtf.data.Record({
            aliasName:recData.shippingAddressType,
            address:recData.shippingAddress,
            county:recData.shippingCounty,
            city:recData.shippingCity,
            state:recData.shippingState,
            country:recData.shippingCountry,
            postalCode:recData.shippingPostal,
            phone:recData.shippingPhone,
            mobileNumber:recData.shippingMobile,
            fax:recData.shippingFax,
            emailID:recData.shippingEmail,
            recipientName:recData.shippingRecipientName,  
            contactPerson:recData.shippingContactPerson,  
            shippingRoute:recData.shippingRoute,
            contactPersonNumber:recData.shippingContactPersonNumber,
            contactPersonDesignation: recData.shippingContactPersonDesignation,
            website: recData.shippingWebsite,
            isDefaultAddress:true,
            isBillingAddress:false
        });
        return shippingRec;
    }, 
    getTransactionVendorShippingAddressRecord:function(){
        var recData=this.record.data;
        var shippingRec=new Wtf.data.Record({
            aliasName:recData.vendcustShippingAddressType,
            address:recData.vendcustShippingAddress,
            county:recData.vendcustShippingCounty,
            city:recData.vendcustShippingCity,
            state:recData.vendcustShippingState,
            country:recData.vendcustShippingCountry,
            postalCode:recData.vendcustShippingPostal,
            phone:recData.vendcustShippingPhone,
            mobileNumber:recData.vendcustShippingMobile,
            fax:recData.vendcustShippingFax,
            emailID:recData.vendcustShippingEmail,
            recipientName:recData.vendcustShippingRecipientName,  
            contactPerson:recData.vendcustShippingContactPerson,  
            contactPersonNumber:recData.vendcustShippingContactPersonNumber,
            contactPersonDesignation: recData.vendcustShippingContactPersonDesignation,
            website: recData.vendcustShippingWebsite,
            isDefaultAddress:true,
            isBillingAddress:false
        });
        return shippingRec;
    },
    getTransactionCustomerShippingAddressRecord:function(){
        var recData=this.record.data;
        var shippingRec=new Wtf.data.Record({
            aliasName:recData.customerShippingAddressType,
            address:recData.customerShippingAddress,
            county:recData.customerShippingCounty,
            city:recData.customerShippingCity,
            state:recData.customerShippingState,
            country:recData.customerShippingCountry,
            postalCode:recData.customerShippingPostal,
            phone:recData.customerShippingPhone,
            mobileNumber:recData.customerShippingMobile,
            fax:recData.customerShippingFax,
            emailID:recData.customerShippingEmail,
            recipientName:recData.customerShippingRecipientName,  
            contactPerson:recData.customerShippingContactPerson,  
            contactPersonNumber:recData.customerShippingContactPersonNumber,
            contactPersonDesignation: recData.customerShippingContactPersonDesignation,
            website: recData.customerShippingWebsite,
            shippingRoute:recData.customerShippingRoute,
            isDefaultAddress:true,
            isBillingAddress:false
        });
        return shippingRec;
    },
    onDocStoreLoad:function(){
        var storeRec=new Wtf.data.Record({
            billid:"defaultaddress",
            billno:"Default Address"
        }); 
        this.docStore.insert(0,storeRec);
        this.docStore.commitChanges();
        
        if(this.currentaddress!="" && this.currentaddress.linkedDocumentCombo!="" && this.currentaddress.linkedDocumentCombo!=undefined){//when address saved with linked transaction
            var searchRecordIndex= WtfGlobal.searchRecordIndex(this.docStore,this.currentaddress.linkedDocumentCombo,'billid');
            if(searchRecordIndex!=-1){
                this.linkedDocumentCombo.setValue(this.currentaddress.linkedDocumentCombo);
            }
        } else if(this.singleLink && this.linkedDocuments!=undefined){
            var selectedDocumentArr = this.linkedDocuments.split(',');
            this.linkedDocumentCombo.setValue(selectedDocumentArr[0]);
        } 
        
        if(this.currentaddress!="" && this.linkedDocumentCombo.getValue()!=""){
            var index= WtfGlobal.searchRecordIndex(this.ShippingAddrsStore,this.currentaddress.shippingAddrsCombo,'aliasName');
            if(index==-1){// alias name not found that is need to add in store
                var shippingRec=new Wtf.data.Record({
                    aliasName:this.currentaddress.shippingAddrsCombo
                });
                this.ShippingAddrsStore.add(shippingRec);
                this.ShippingAddrsStore.commitChanges();
                this.ShippingAddrsStore.sort('aliasName','ASC');
                this.shippingAddrsCombo.setValue(this.currentaddress.shippingAddrsCombo);
            } 
            
            
            index= WtfGlobal.searchRecordIndex(this.billingAddrsStore,this.currentaddress.billingAddrsCombo,'aliasName');
            if(index==-1){// alias name not found that is need to add in store
                var billingRec=new Wtf.data.Record({
                    aliasName:this.currentaddress.billingAddrsCombo
                });
                this.billingAddrsStore.add(billingRec);
                this.billingAddrsStore.commitChanges();
                this.billingAddrsStore.sort('aliasName','ASC');
                this.billingAddrsCombo.setValue(this.currentaddress.billingAddrsCombo);
            }
           
            if(!this.isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster){
                index= WtfGlobal.searchRecordIndex(this.vendorShippingAddrsStore,this.currentaddress.vendorShippingAddrsCombo,'aliasName');
                if(index==-1){// alias name not found that is need to add in store
                    var vendorbillingshippingRec=new Wtf.data.Record({
                        aliasName:this.currentaddress.vendorShippingAddrsCombo
                    });
                    this.vendorShippingAddrsStore.add(vendorbillingshippingRec);
                    this.vendorShippingAddrsStore.commitChanges();
                    this.vendorShippingAddrsStore.sort('aliasName','ASC');
                    this.vendorShippingAddrsCombo.setValue(this.currentaddress.vendorShippingAddrsCombo);
                } 
            }
        }   
    },
    onLinkedDocumentSelect:function(combo,rec){
        if(combo.getValue()==this.linkedDocumentComboValueBeforeSelect){ //If same name selected no need to do any action 
            return;
        } else {
            if(combo.getValue()=="defaultaddress"){//customer/vendor/company address
                //once the user select default Address then linking addresses will removed and default address will be set
                this.isEdit=false;
                this.copyInv=false;
                this.singleLink=false;
                this.record=null;
                this.currentaddress="";
                this.billingAddrsStore.load();
                this.ShippingAddrsStore.load();
                if(!this.isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster){
                    this.vendorShippingAddrsStore.load();
                }
            } else {
                // once the user select other than default Address then then we need to update billing shipping address store and set value of address fields
                // below code does these things
                this.record=rec;
            
                var tranBillingAddrRec=this.getTransactionBillingAddressRecord(); 
                this.addOrUpdateTransactionBillingAddressInStore();
                this.billingAddrsCombo.setValue(tranBillingAddrRec.data.aliasName);
                this.setDefaultBillingAddress(tranBillingAddrRec.data);
            
                var tranShippingAddrRec=this.getTransactionShippingAddressRecord();
                this.addOrUpdateTransactionShippingAddressInStore();
                this.shippingAddrsCombo.setValue(tranShippingAddrRec.data.aliasName);
                this.setDefaultShippingAddress(tranShippingAddrRec.data);
            
                if(!this.isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster){
                    var vendTranShippingAddrRec=this.getTransactionVendorShippingAddressRecord();
                    this.addOrUpdateTransactionVendorShippingAddressInStore();
                    this.vendorShippingAddrsCombo.setValue(vendTranShippingAddrRec.data.aliasName);
                    this.setVendorDefaultShippingAddress(vendTranShippingAddrRec.data); 
                } 
            }
        }
    }
});
