

Wtf.account.WorkOrderTab=function(config){
    Wtf.apply(this, config);
    this.createButtons();
    this.workorderreport = config.workorderreport
    this.workorderUUDID="";
    Wtf.apply(this, {
        bbar:this.buttonArray
      });
    Wtf.account.WorkOrderTab.superclass.constructor.call(this,config);
};
Wtf.extend(Wtf.account.WorkOrderTab,Wtf.account.ClosablePanel,{
    autoScroll: true,
    bodyStyle: {background:"#DFE8F6 none repeat scroll 0 0"},
    border:'false',
    closable : true,
    onRender:function(config){                
        this.add(this.tabPanel)   
        Wtf.account.WorkOrderTab.superclass.onRender.call(this, config);
    },
    initComponent:function(config){
        Wtf.account.WorkOrderTab.superclass.initComponent.call(this,config);
        this.workOrderEntryForm = new Wtf.account.WorkOrderEntryForm({
            title:this.title,
            tabTip:this.tabTip,
            id:"mrpWorkOrderEntryForm" + this.id,
            border : false,
            closable:false,
            isEdit:this.isEdit,
            layout:"border",
            record:this.record,
            iconCls: 'workordericon',
            workorderreport:this.workorderreport,
            projectId:this.projectId
        });
        this.workOrderEntryForm.on("activate",function(){
            this.tabPanel.doLayout();
             this.saveBttn.setVisible(true);
            this.createJobWorkBtn.setVisible(true);
//            this.saveBttn.enable();
        },this);
        
        this.tabPanel =  new Wtf.TabPanel({
           id:"mrpWorkOrdertabPanel" + this.id, /* Assigned id to tab panel */
           activeTab:0,
//           layout:"fit",
           border:false,
           items:[this.workOrderEntryForm]
        });
        
       
    },
    createButtons: function() {
        this.buttonArray = new Array();
        this.saveBttn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
            tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
            id: "save" + this.id,
            scope: this,
            handler: function(){
                this.save();
            },
            iconCls: 'pwnd save'
        });
        this.buttonArray.push(this.saveBttn);
        
          this.createJobWorkBtn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.mrp.workorder.jobordercreatebtn.joborder.name"), //'Save',
            tooltip: WtfGlobal.getLocaleText("acc.mrp.workorder.jobordercreatebtn.joborder.name"),
            id: "createjobwork" + this.id,
            scope: this,
            disabled:true,
            iconCls: getButtonIconCls(Wtf.etype.add),
            handler: function(){
                this.createJobWork();
                this.createJobWorkBtn.disable();    //ERP-30666 : Disable this button once JWO form is open.
            }
        });
        
         this.buttonArray.push(this.createJobWorkBtn);
         
         this.projPlanBttn = new Wtf.Toolbar.Button({
            text: "View / Modify Tasks",//WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: "View / Modify Tasks",//WtfGlobal.getLocaleText("acc.common.fetch"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            disabled:true,
            handler: this.viewProjectPlan
        });
//        this.buttonArray.push(this.projPlanBttn);
        
        this.genPOPRBtn= new Wtf.Toolbar.Button({
            text: Wtf.account.companyAccountPref.autoGenPurchaseType == 0? "Generate Purchase Order" : "Generate Purchase Requsition",
            tooltip: Wtf.account.companyAccountPref.autoGenPurchaseType == 0? "Generate Purchase Order" : "Generate Purchase Requsition",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            disabled:true,
            handler: this.createPOfromWO
        });
        this.buttonArray.push(this.genPOPRBtn);
        this.genPOPRBtn.enable();
    },
    createJobWork: function(){
        if (this.workorderUUDID != "") {
            this.jobOrderEntryForm=Wtf.getCmp('joborderdetails');
            if(this.jobOrderEntryForm==undefined){
                this.jobOrderEntryForm = getJobOrderEntryForm(false, this.workorderUUDID);
            }
            this.tabPanel.add(this.jobOrderEntryForm);
            this.jobOrderEntryForm.on("activate",function(){
            this.tabPanel.setActiveTab(this.jobOrderEntryForm);
            this.saveBttn.setVisible(false);
            this.createJobWorkBtn.setVisible(false);
            this.jobOrderEntryForm.doLayout();
            
//            this.tabPanel.doLayout();
//            this.doLayoutOfAllTabs();
        },this);
        this.saveBttn.disable();
            this.tabPanel.add(this.jobOrderEntryForm);
        }
    },
    doLayoutOfAllTabs : function(){
           this.tabPanel.doLayout(); 
           this.jobOrderEntryForm.doLayout(); 
        
    },
    save: function() {
        this.workOrderForm =this.workOrderEntryForm.workOrderPanel.getForm();
        this.workOrderFormisValid =  this.workOrderForm.isValid();
        this.isValidCustomFields = this.workOrderEntryForm.tagsFieldset.checkMendatoryCombo();
        var caJSON=this.workOrderEntryForm.getCAJSON();
         if (!this.workOrderFormisValid || !this.isValidCustomFields) {
            WtfGlobal.dispalyErrorMessageDetails(this.workOrderEntryForm.id + "requiredfieldmessagepanel", this.getInvalidFields());
            if (this.workOrderEntryForm) {
                this.workOrderEntryForm.doLayout();
            }
            return;
        } else {
            Wtf.getCmp(this.workOrderEntryForm.id + "requiredfieldmessagepanel").hide();
            if (this.workOrderEntryForm) {
                this.workOrderEntryForm.doLayout();
            }
        }
        if (!this.workOrderEntryForm.taskPlanned) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mrp.plantask.validation.err.msg")], 2);
            return;
        }
        
        if (caJSON) {
            var isBatchValid = true;
            var isValidCaJson = true;
            try {
                var caJsonArr = eval(caJSON );
            } catch (err) {
                isValidCaJson = false;
            }
            var warehouseLoactionnotPresent=false;// when warehouse location is not activated for a particular product at that time system is asking user to enter warehouse location details. so I have added this  flag
            if (isValidCaJson) {
                for (var cntInd = 0; cntInd < caJsonArr.length; cntInd++ ) {
                    var warningMsg = "", batchdetailsString = "";
                    var tempObj = caJsonArr[cntInd];
                    if(!tempObj.isWarehouseForProduct || !tempObj.isLocationForProduct){
                            warehouseLoactionnotPresent=true;
                    }
                    var batchDetailArr = tempObj.batchdetails ? eval(tempObj.batchdetails) : [];
                    /*
                     * ERP-37246 : Below if Block has written to form BatchDetails array. It will execute only when -
                     * 1) Product has only Location and/or Warehouse. Do not have Batch/Serial.
                     * 2) Block Quantity should be > 0.
                     * 3) BatchDetails array should be empty for this product. It means, user is not going to fill the Batch Serial Window details.
                     */
                    if(tempObj.blockquantity!=0 && (tempObj.isWarehouseForProduct || tempObj.isLocationForProduct) && (batchDetailArr!=undefined && batchDetailArr.length==0) && (!tempObj.isBatchForProduct && !tempObj.isSerialForProduct && !tempObj.isRowForProduct && !tempObj.isRackForProduct && !tempObj.isBinForProduct)){
//                            var warehouseLocJsonData = {};
//                            var srbatchdetails = [];   
//                            warehouseLocJsonData["id"] = "";
//                            warehouseLocJsonData["location"] = tempObj.location;
//                            warehouseLocJsonData["warehouse"] = tempObj.warehouse;
//                            warehouseLocJsonData["productid"] = tempObj.productid;
//                            warehouseLocJsonData["quantity"] = tempObj.requiredquantity;
//                            warehouseLocJsonData["stocktype"] = "1";    //Need to check this type.
//                            warehouseLocJsonData["row"] = "";
//                            warehouseLocJsonData["rack"] = "";
//                            warehouseLocJsonData["bin"] = "";
//                            warehouseLocJsonData["mfgdate"] = "";
//                            warehouseLocJsonData["expdate"] = "";
//                            warehouseLocJsonData["balance"] = "";
//                            warehouseLocJsonData["serialno"] = "";
//                            warehouseLocJsonData["reusablecount"] = "";
//                            warehouseLocJsonData["serialnoid"] = "";
//                            warehouseLocJsonData["expstart"] = "";
//                            warehouseLocJsonData["expend"] = "";
//                            warehouseLocJsonData["batch"] = "";
//                            warehouseLocJsonData["batchname"] = "";
//                            warehouseLocJsonData["purchasebatchid"] = "";
//                            warehouseLocJsonData["purchaseserialid"] = "";
//                            warehouseLocJsonData["isserialusedinDO"] = "";
//                            warehouseLocJsonData["isreadyonly"] = false;
//                            warehouseLocJsonData["lockquantity"] = "";
//                            warehouseLocJsonData["documentid"] = "";
//                            warehouseLocJsonData["customfield"] = "";
//                            warehouseLocJsonData["packwarehouse"] = "";
//                            warehouseLocJsonData["packlocation"] = "";
//                            warehouseLocJsonData["skufield"] = "";
//                            warehouseLocJsonData["attachment"] = "";
//                            warehouseLocJsonData["attachmentids"] = "";
//                            warehouseLocJsonData["documentbatchid"] = "";
                        
                        //If Product's available quantity at its default location and/or warehouse then we will show the below prompt message.
                        if(tempObj.availQtyofdefaultlocwarehouse < tempObj.requiredquantity){   //In Auto-fill process, block quantity will be equal to Required Quantity.
                                warningMsg = "<b>"+tempObj.productname + "</b> "+WtfGlobal.getLocaleText("acc.mrp.autoblockqty.fail.infomsg");  //<ProductName> do not have sufficient quantity. Please check Location & Warehouse details.
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), warningMsg], 2);
                                warningMsg = "";
                                return;
                            }    
                            
                            //Below code has written to replace the empty BatchDetails array from object with detailed array.
//                            srbatchdetails.push(warehouseLocJsonData);
//                            tempObj.batchdetails = JSON.stringify(srbatchdetails);
                            batchdetailsString = "[{id:\"\",location:\""+tempObj.location+"\",warehouse:\""+tempObj.warehouse+"\",productid:\""+tempObj.productid+"\",quantity:\""+tempObj.requiredquantity+"\",avlquantity:\""+tempObj.availablequantity+"\",stocktype:\""+1+"\",balance:\"\",row:\"\",rack:\"\",bin:\"\",serialno:\"\",reusablecount:\"\",serialnoid:\"\",batch:\"\",batchname:\"\",purchasebatchid:\"\",purchaseserialid:\"\",isserialusedinDO:\"\",lockquantity:\"\",documentid:\"\",customfield:\"\",packwarehouse:\"\",packlocation:\"\",skufield:\"\",wastageQuantityType:\"\",wastageQuantity:\"\",attachment:\"\",attachmentids:\"\",documentbatchid:\"\",mfgdate:'',expdate:'',expstart:'',expend:'',modified:false,isreadyonly:false}]";
                            tempObj.batchdetails = batchdetailsString;
                            caJsonArr[cntInd] = tempObj;    //We send 'caJSON' array through request. So, I have replaced updated tempObj with old tempObj.
                    }
                    batchDetailArr = tempObj.batchdetails ? eval(tempObj.batchdetails) : [];
                    var batchLockQty = 0;
                    for (var batchIndex = 0 ; batchIndex < batchDetailArr.length; batchIndex++ ) {
                        var lockQty = 0;
                        if (batchDetailArr[batchIndex].quantity) {
                            lockQty = getRoundofValue(batchDetailArr[batchIndex].quantity);
                        }
                        batchLockQty += lockQty;
                    }
                    batchLockQty = getRoundofValue(batchLockQty);
                    var roundvalueofblockquantity=getRoundofValue(tempObj.blockquantity);
                    if (!roundvalueofblockquantity && (roundvalueofblockquantity !== 0)) {
                        isValidCaJson = false;
                    }
                    if (tempObj.type!="Inventory Assembly" && batchLockQty !== roundvalueofblockquantity && !warehouseLoactionnotPresent) {
                        isBatchValid = false;
                    }
                }
                caJSON = JSON.stringify(caJsonArr); //Returning final updated JSON Array.
            }
            if (!isValidCaJson) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mrp.blockQuantity.validation.err.msg")], 2);
                return;
            }
            if (!isBatchValid ) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.bsdetail")], 2);
                return;
            }
        }
        /*
         * ERP-40173 : In any cirumstances, if WO Status combo does not load and user is going to save WO then system should
         * prompt user to refresh the page.
         */
        if(this.workOrderEntryForm.workOrderStatusText.getValue()==undefined || this.workOrderEntryForm.workOrderStatusText.getValue()==""){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mrp.wostatus.emptywarning")], 2);
            return;
        }
        
        if (this.workOrderFormisValid && this.isValidCustomFields) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"), WtfGlobal.getLocaleText("acc.je.msg1"), function(btn) {
                if (btn != "yes") {
                    this.isWarnConfirm = false;
                    return;
                }
                WtfComMsgBox(27, 4, true);
                var rec = this.workOrderForm.getValues();                      // to be created later on   deliveryDateText
                var id='';
                if (this.isEdit) {
                    id = this.record.data.id;
                    rec.id = id;
                    rec.isEdit=this.isEdit;
                }
                rec.productid=this.workOrderEntryForm.productNameCombo.getValue();
                rec.projectId=this.projectId;
                rec.labourid = this.workOrderEntryForm.labourIDCombo.getValue();
                rec.routetemplateid = this.workOrderEntryForm.routingCodeCombo.getValue();
                rec.machineid = this.workOrderEntryForm.machineIDCombo.getValue();
                rec.workcentreid = this.workOrderEntryForm.workCentreCombo.getValue();
                rec.workorderstatus=this.workOrderEntryForm.workOrderStatusText.getValue();
                rec.workordertype=this.workOrderEntryForm.workOrderTypeCombo.getValue();
                rec.orderWarehouse=this.workOrderEntryForm.wareHouseCombo.getValue();
                rec.orderLocation=this.workOrderEntryForm.locationMultiSelect.getValue();
                rec.customer=this.workOrderEntryForm.customerNameCombo.getValue();
                rec.seqformat = this.workOrderEntryForm.sequenceFormatCombobox.getValue();
                rec.materialid = this.workOrderEntryForm.materialIDCombo.getValue();
                rec.linkDocNo = this.workOrderEntryForm.linkDocNoText.getValue();
                rec.details=caJSON;
                var seqFormatRec = WtfGlobal.searchRecord(this.workOrderEntryForm.sequenceFormatStore, this.workOrderEntryForm.sequenceFormatCombobox.getValue(), 'id');
                rec.seqformat_oldflag = seqFormatRec != null ? seqFormatRec.get('oldflag') : true;
                //Global custom data
                var custFieldArr = this.workOrderEntryForm.tagsFieldset.createFieldValuesArray();
                if (custFieldArr.length > 0)
                    rec.customfield = custFieldArr;
                
                WtfGlobal.onFormSumbitGetDisableFieldValues(this.workOrderEntryForm.workOrderPanel.form.items, rec);
                rec.dateofdelivery=  WtfGlobal.convertToGenericDate(this.workOrderEntryForm.deliveryDateText.getValue());
                rec.workorderdate=  WtfGlobal.convertToGenericDate(this.workOrderEntryForm.workOrderDate.getValue());
                rec.fromLinkCombo=this.workOrderEntryForm.fromLinkCombo.getValue();
                rec.routingtype = this.workOrderEntryForm.routingTypeCombo.getValue(); //After WO start this feild get DISABLED.Get correct value of this field.
//                var jobWorkArr = this.getJobWorkOrders();
                WtfComMsgBox(27, 4, true);
                var array = [];
                array.push(rec);
                this.savedRec = {};
                this.savedRec.data = rec; 
                var obj = {};
                obj.data  = array;
//                obj.joborderdata = jobWorkArr;
//                this.disable();
                var url = "ACCWorkOrder/saveWorkOrder.do";
                Wtf.Ajax.requestEx({
                    url: url,
                    params: {
                        data:JSON.stringify(obj),
                        isEdit:this.isEdit,
                        isBOMChanged:this.workOrderEntryForm.isBOMChanged,
                        isMassCreate:false
                    }
                }, this, this.genSuccessResponse, this.genFailureResponse);
            }, this);
        } else {
            WtfComMsgBox(2, 2);
        }
    },
     viewProjectPlan: function(){
//        var rows = this.sm.getSelections();
//        if(rows.length == 1){
//            var row = rows[0];
//            var projectId = row.get('projectId');
//            var projectId = "a030af9f-c278-4073-b70d-77fd9224b2df";
            var panel = Wtf.getCmp("woprojectplan-"+this.projectId);
            if(!panel){
                panel = new Wtf.Panel({
                    autoEl : {
                        tag : "iframe",
                        height:"100%",
                        src: Wtf.pmURL+"editableprojview.jsp?id="+this.projectId
                    },
                    id: "woprojectplan -"+this.projectId,
                    title: "Work Order - Project Plan",
                    closable:true,
                    layout:'fit'
                }); 
                mainPanel.add(panel);
            }
            mainPanel.setActiveTab(panel);
//        }
    },
   
    getInvalidFields: function () {
        var invalidFields = []
        this.workOrderEntryForm.workOrderPanel.getForm().items.filterBy(function (field) {
            if (field.validate())
                return;
            invalidFields.push(field);
        });
        var invalidCustomFieldsArray = this.workOrderEntryForm.tagsFieldset.getInvalidCustomFields();// Function for getting invalid custom fields and dimensions 
        for (var i = 0; i < invalidCustomFieldsArray.length; i++) {
            invalidFields.push(invalidCustomFieldsArray[i]);
        }
       return invalidFields;
    },
   genSuccessResponse: function (response, request) {
         Wtf.MessageBox.hide();
        if (response.success) {
            this.workorderUUDID=response.workorderid;
            var msg = "";
            /*
             * Commented following  code as message is now snet from backend. 
             */
//            if (this.isEdit) {
//                msg = WtfGlobal.getLocaleText("mrp.workorder.form.successfullyupdatemsg") + "<br/>"+ response.msg;
//            } else {
                msg = response.msg;
//            }
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), msg], 0);
            var genpocheck=response.genpocheck; 
            Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.common.success"),msg,function(btn){
                if(btn=='ok'){
                    if(genpocheck){
                        if(Wtf.account.companyAccountPref.autoGenPurchaseType == 0){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("mrp.workorder.form.nowyoucangeneratepo")], 3);
                        }else{
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("mrp.workorder.form.nowyoucangeneratepr")], 3);
                        }
                    }
                }
                        
            },this);
            
            this.workorderreport.fetchStatement();
            this.disableComponent();
            this.createJobWorkBtn.enable();
            this.projPlanBttn.enable();
            this.genPOPRBtn.enable();
        }
        
        if (!response.success) {    // Handling the messages
//            this.enableSaveButton();
//            this.enableComponent();
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
                            fieldLabel: WtfGlobal.getLocaleText("acc.WC.newWorkOrderno"),
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
                                                    
                                    this.workOrderEntryForm.workOrderIDText.setValue(this.newdono.getValue());
                                                            
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
    disableComponent: function () {
        if (this.saveBttn) {
            this.saveBttn.disable();
        }
        if (this.workOrderEntryForm.workOrderPanel) {
                this.workOrderEntryForm.workOrderPanel.disable();
        }
        /*
         * Disable project and component availaibility panel. 
         */
        this.workOrderEntryForm.projectPanel.disable();
        this.workOrderEntryForm.CAPanel.disable();
    },
    genFailureResponse: function (response) {
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        Wtf.updateProgress();
    },
    createPOfromWO:function(){   
        
        var gridDataArr = this.workOrderEntryForm.CAPanel.CAStore.data.items;
        var productidstr = "";
        for (var cnt =0; cnt < gridDataArr.length; cnt++) {
            if (gridDataArr[cnt].data.genpocheck) {
                productidstr += gridDataArr[cnt].data.productid+",";
            }
        }
        
        if(productidstr==""){
                
            if(Wtf.account.companyAccountPref.autoGenPurchaseType == 0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("mrp.workorder.form.firstselectcheckboxofGeneratePO")], 2);
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("mrp.workorder.form.firstselectcheckboxofGeneratePR")], 2);
            }
            return;
        }
        var formRecord = this.savedRec;
        var billid = this.workorderUUDID;
        if(formRecord==undefined && billid == ""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("mrp.workorder.form.firstsaveworkorder")], 2);
                return;
        }
        else{
        formRecord.data.billid = this.workorderUUDID;
        formRecord.data.productidstr = productidstr;
        if (Wtf.account.companyAccountPref.autoGenPurchaseType == 0) {
            callEditPurchaseOrder(true,formRecord,"Generate_PO"+billid,true,this,null, false,undefined,false,false,true);
        } else {
            callPurchaseReq(false, formRecord,"Generate_PReq"+billid, false,this,false,false,true);
        }
        }
        
    }
});
